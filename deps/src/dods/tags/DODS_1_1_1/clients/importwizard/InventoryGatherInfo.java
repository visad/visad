/**
 * InventoryGatherInfo.java
 *
 * 1.00 2001/8/16
 *
 */

package dods.clients.importwizard;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import dods.dap.*;
import gnu.regexp.*;

/**
 * This class gathers the dataset information.
 *
 * @version     1.00 16 Aug 2001
 * @author      Kashan A. Shaikh
 */
public class InventoryGatherInfo extends Thread
{
	private String datasetURL;
		
	private int lowYear,lowMonth,lowDay,highYear,highMonth,highDay;
    private int numVars;
    private String datasetName;
    private String[] varNames;
    private String[][] varContents;
    private String sequenceName;
    private boolean fileserver;
	
	// DODS DAP objects
    private dods.dap.DConnect connect;
    private dods.dap.DAS dasObject;
    private dods.dap.DDS ddsObject;

    // DAS objects
    dods.dap.AttributeTable dods_global;

    private String dateFields,dateFunction,showFields;
    private boolean showTime,showVars;


// ---------- Constructor ------------
	public InventoryGatherInfo(String url) {
	 	datasetURL = url;
	 	fileserver = false;
	 	
	 	sequenceName = "";
	 	showVars = false;
		showTime = true;
	
		lowYear = 0;
		lowMonth = 0;
		lowDay = 0;
		highYear = 0;
		highMonth = 0;
		highDay = 0;
		
		dateFields = "";
		dateFunction = "";
		showFields = "";
		
		numVars = 0;		
	 	
	 	// get the DODS DAS object
		try {
			connect = new dods.dap.DConnect(datasetURL);
	   		dasObject = connect.getDAS();
	   		ddsObject = connect.getDDS();
		} catch(Exception e) {System.out.println("\nERROR:connecting to dataset");}
	}
	
	// Execute the thread
	public void run() {
		determineIfFileserver();
		gatherInfo();
	}
	
	
	
	// Returns true if this dataset is a fileserver
    public boolean isFileserver() {
    	return fileserver;
    }


	
	// Gather the dataset information
    private boolean gatherInfo() {

		dods.dap.AttributeTable dods_inventory = null;
		dods.dap.Attribute attr = null;

        String temp = null;
		boolean dateSpecified = false;
	
		RE spaces = null;
		RE quotes = null;
		try {
	    	spaces = new RE(" ");
	    	quotes = new RE("\"");
		} catch(Exception e) {}
	 	
 		if ( isFileserver() ) {
	    	// try to determine the dataset name
	    	try {
			attr = dods_global.getAttribute("DODS_Title");
			if (attr != null) {
		    	datasetName = quotes.substituteAll(attr.getValueAt(0),"");
			}
	    	} catch(Exception e) {System.out.println("\nDODS_TITLE not defined");}
	
	
	    	// try to get the sequence name (if it contains a sequence)
	    	try {
	    		sequenceName = ((DSequence) ddsObject.getVariables().nextElement()).getName();
	    	} catch(Exception e) {}
	    	
	    	
	    	// is the date range already specified?
	    	try {
			RE exp = new RE("\"?(\\d+)/(\\d+)/(\\d+)\"?");
			REMatch m1;
			// get the low date
			attr = dods_global.getAttribute("DODS_StartDate");
			if (attr != null) {
		    	temp = quotes.substituteAll(attr.getValueAt(0),"");
		    	m1 = exp.getMatch(temp);
		    	lowYear = Integer.parseInt(m1.toString(1));
		    	lowMonth = Integer.parseInt(m1.toString(2));
		    	lowDay = Integer.parseInt(m1.toString(3));
		    	dateSpecified = true;
		    	showTime = true;
			}
		
			// get the high date
			attr = dods_global.getAttribute("DODS_EndDate");
			if (attr != null) {
		   		temp = quotes.substituteAll(attr.getValueAt(0),"");
		    	m1 = exp.getMatch(temp);
		   		highYear = Integer.parseInt(m1.toString(1));
		   		highMonth = Integer.parseInt(m1.toString(2));
		   		highDay = Integer.parseInt(m1.toString(3));
		    	dateSpecified = true;
		    	showTime = true;
			}
	    	} catch(Exception e) {System.out.println("\nDODS_StartDate/DODS_EndDate not defined");}
	
	
	    	// look for date specs
	    	try {
			dods_inventory = dasObject.getAttributeTable("DODS_Inventory");
			if (dods_inventory != null) {
		    	attr = dods_inventory.getAttribute("Inventory_DateFields");
		    	if (attr != null) {
				dateFields = quotes.substituteAll(attr.getValueAt(0),"");
		    	}
		
		    	attr = dods_inventory.getAttribute("Inventory_DateFunction");
		    	if (attr != null) {
				dateFunction = quotes.substituteAll(attr.getValueAt(0),"");
		    	}
			}
	 	  	} catch(Exception e) {System.out.println("\nInventory date field/function not defined");}
	
	
	    	// do we want to use the date range selection?
	    	try {
			if (dods_inventory != null) {
		    	attr = dods_inventory.getAttribute("Inventory_SelectTime");
		    	if (attr != null) {
					temp = quotes.substituteAll(attr.getValueAt(0),"");
					if (temp.equals("false")) {
			    		showTime = false;	// no date selection
					} else {
			    		showTime = true;
					}
		    	}
			}
	    	} catch(Exception e) {System.out.println("\nInventory_SelectTime not defined");}
	
	    	// are there date fields specified?
	    	try {
			if (dods_inventory != null) {
		    	attr = dods_inventory.getAttribute("Inventory_DateFields");
		    	if (attr != null) {
					dateFields = quotes.substituteAll(attr.getValueAt(0),"");
		    	}
			}
	    	} catch(Exception e) {System.out.println("\nInventory_DateFields not defined");}
	
	    	// is there a date function specified?
	    	try {
			if (dods_inventory != null) {
		    	attr = dods_inventory.getAttribute("Inventory_DateFunction");
		   		if (attr != null) {
					dateFunction = quotes.substituteAll(attr.getValueAt(0),"");
		    	}
			}
	    	} catch(Exception e) {System.out.println("\nInventory_DateFunction not defined");}
	
	
	    	// determine which fields should be included
	    	try {
			if (dods_inventory != null) {
		    	attr = dods_inventory.getAttribute("Inventory_ShowFields");
		    	if (attr != null) {
					showFields = quotes.substituteAll(attr.getValueAt(0),"");
		    	}
			}
	    	} catch(Exception e) {System.out.println("\nInventory_ShowFields not defined");}
	
	
	    	// are there variables to be selected?
	    	try {
			if (dods_inventory != null) {
		    	attr = dods_inventory.getAttribute("Inventory_SelectVars");
		    	if (attr != null) {
					showVars = true;
					String val = attr.getValueAt(0);
					val = spaces.substituteAll(val,"");
					val = quotes.substituteAll(val,"");
					RE exp = new RE(",?([^,.]*)");
					REMatch[] matches = exp.getAllMatches(val);
					varNames = new String[matches.length];
					varContents = new String[matches.length][];
					DataDDS data = null;
					Enumeration tvars = null;
					Hashtable thash = new Hashtable();
					Vector tvect = new Vector();
					DSequence seq = null;
					numVars = matches.length;
					for (int i=0; i < numVars; i++) {
			    		varNames[i] = matches[i].toString(1);
			    		data = connect.getData("?"+varNames[i],null);
			    		tvars = data.getVariables();
			    		seq = (DSequence) tvars.nextElement();
			    		for (int j = 0; j < seq.getRowCount(); j++) {
							String tmp = ((DString) seq.getVariable(j,varNames[i])).getValue();
							if ( ! thash.contains(tmp) ) {
				    			tvect.addElement(tmp);
				    			thash.put(tmp,tmp);
							}
			    		}
			    		varContents[i] = new String[tvect.size()];
			    		for (int j = 0; j < tvect.size(); j++) {
							varContents[i][j] = (String) tvect.elementAt(j);
			    		}
			    		thash.clear();
			    		tvect.removeAllElements();
					}
		    	}
			}
			} catch(Exception e) {System.out.println("\nInventory_SelectVars not defined");}
	
	
	    	// Let's try to determine the date range
	    	try {
			if ( (showTime) && (! dateSpecified) ) {
		    	if ( (dateFields != "") && (dateFunction != "") ) {
				String CE = "?" + dateFields;
				DataDDS data = null;
				try {
			    	data = connect.getData(CE,null);
				} catch(Exception e) {System.out.println("\nERROR:forming constraint");}
			
				if (data != null) {
			    	RE exp = new RE("(\\d+)/(\\d+)/(\\d+)");
			    	REMatch m1;
			
			    	Enumeration tvar = data.getVariables();
			    	DSequence seq = (DSequence) tvar.nextElement();
			    	tvar = seq.getVariables();
			
			    	// get low date
			    	String vnme = ((DString) tvar.nextElement()).getName();
			    	temp = ((DString) seq.getVariable(0,vnme)).getValue();
			    	m1 = exp.getMatch(temp);
			    	lowYear = Integer.parseInt(m1.toString(1));
			    	lowMonth = Integer.parseInt(m1.toString(2));
			    	lowDay = Integer.parseInt(m1.toString(3));
			
			    	// get high date
			    	temp = ((DString) seq.getVariable(seq.getRowCount()-1,vnme)).getValue();
			    	m1 = exp.getMatch(temp);
			    	highYear = Integer.parseInt(m1.toString(1));
			    	highMonth = Integer.parseInt(m1.toString(2));
			    	highDay = Integer.parseInt(m1.toString(3));
			
			    	if (seq.elementCount() > 1) {  // start/end date type
					vnme = ((DString) tvar.nextElement()).getName();
				
					// determine low date
					temp = ((DString) seq.getVariable(0,vnme)).getValue();
					m1 = exp.getMatch(temp);
					lowYear = getMin( lowYear, Integer.parseInt(m1.toString(1)) );
					lowMonth = getMin( lowMonth, Integer.parseInt(m1.toString(2)) );
					lowDay = getMin( lowDay, Integer.parseInt(m1.toString(3)) );
				
					// determine high date
					temp = ((DString) seq.getVariable(seq.getRowCount()-1,vnme)).getValue();
					m1 = exp.getMatch(temp);
					highYear = getMax( highYear, Integer.parseInt(m1.toString(1)) );
					highMonth = getMax( highMonth, Integer.parseInt(m1.toString(2)) );
					highDay = getMax( highDay, Integer.parseInt(m1.toString(3)) );
			    }
			}				
		    } else { 		// ** last hope
				String CE = "?DODS_Date(" + sequenceName + ")";
				DataDDS data = null;
				try {
			    	data = connect.getData(CE,null);
				} catch(Exception e) {showTime = false;}
			
				if (data != null) {
			    	RE exp = new RE("(\\d+)/(\\d+)/(\\d+)");
			    	REMatch m1;
			
			    	Enumeration tvar = data.getVariables();
			    	DSequence seq = (DSequence) tvar.nextElement();
			    	tvar = seq.getVariables();
			
			    	// get low date
			   		String vnme = ((DString) tvar.nextElement()).getName();
			    	temp = ((DString) seq.getVariable(0,vnme)).getValue();
			    	m1 = exp.getMatch(temp);
			    	lowYear = Integer.parseInt(m1.toString(1));
			    	lowMonth = Integer.parseInt(m1.toString(2));
			    	lowDay = Integer.parseInt(m1.toString(3));
			
			    	// get high date
			    	temp = ((DString) seq.getVariable(seq.getRowCount()-1,vnme)).getValue();
			    	m1 = exp.getMatch(temp);
			    	highYear = Integer.parseInt(m1.toString(1));
			    	highMonth = Integer.parseInt(m1.toString(2));
			    	highDay = Integer.parseInt(m1.toString(3));
			    	
			    	showTime = true;
				}				
		    }
			}
	    	} catch(Exception e) {showTime = false;}
	
	    	if ((showTime != false)||(showVars != false)) {
				fileserver = true;
				return true;
	    	} else {
				fileserver = false;
				return false;
	   		}
		} else {
	    	return false;
		}
    }
	
	
    // --- Returns the minimum of two integers ---
    private int getMin(int v1, int v2) {
    	if (v1 < v2) {
	    	return v1;
    	} else {
	    	return v2;
    	}
    }

    // --- Returns the maximum of two integers ---
    private int getMax(int v1, int v2) {
    	if (v1 > v2) {
	    	return v1;
    	} else {
	   		return v2;
    	}
    }



     // --- Determine if the dataset is a fileserver ---
    private void determineIfFileserver() {
    	dods.dap.AttributeTable ff_global;
    	dods.dap.Attribute attr = null;
    	String temp = null;
    	RE spaces = null;
		RE quotes = null;
		try {
	    	spaces = new RE(" ");
	    	quotes = new RE("\"");
		} catch(Exception e) {}
	
    	try {
	    	dods_global = dasObject.getAttributeTable("DODS_Global");
	    	if (dods_global != null) {
				attr = dods_global.getAttribute("DODS_Filetype");
				if (attr == null) {
		    		attr = dods_global.getAttribute("DODS_FileType");	// check for case
				}
				if (attr != null) {
		    		temp = quotes.substituteAll(attr.getValueAt(0),"");
		    		if (temp.toLowerCase().compareTo("fileserver") == 0) {
					fileserver = true;
		    		}
				}
	    	}
	
	    	if (fileserver == false) {
	    		// *** this is a temporary solution
	    		ff_global = dasObject.getAttributeTable("FF_GLOBAL");
	   			if (ff_global != null) {
					attr = ff_global.getAttribute("Native_file");
					if (attr == null) {
		 				fileserver = false;
					}
					if (attr != null) {
		 				fileserver = true;
					}
	    		}
	  		}		
		} catch(Exception e) {System.out.println("\nERROR:DODS_Filetype not defined");}
    }
	

// ----------- Access Functions --------------
    public String getDatasetName() {
    	return datasetName;
    }

    public boolean variablesExist() {
     	return showVars;
    }

    public boolean timeExists() {
     	return showTime;
    }

    public String getSequenceName() {
     	return sequenceName;
    }

    // get date information
    public int getLowYear() {
     	return lowYear;
    }
    public int getLowMonth() {
    	return lowMonth;
    }
    public int getLowDay() {
     	return lowDay;
    }
    public int getHighYear() {
    	return highYear;
    }
    public int getHighMonth() {
    	return highMonth;
    }
    public int getHighDay() {
     	return highDay;
    }

    public String getDateFields() {
     	return dateFields;
    }
    public String getDateFunction() {
    	return dateFunction;
    }


    // get variable information
    public int getNumVariables() {
     	return numVars;
    }
    public String[] getVariableNames() {
     	return varNames;
    }
    public String[][] getVariableContents() {
     	return varContents;
    }

}
