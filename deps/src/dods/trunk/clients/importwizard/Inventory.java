/**
 * Inventory.java
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
 * This class provides the base structure for
 * the Inventory application.
 *
 * @version     1.00 16 Aug 2001
 * @author      Kashan A. Shaikh
 */
public class Inventory extends InventoryURLProcessor
            implements ActionListener
{
    private InventoryGatherInfo info;

    private int lowYear,lowMonth,lowDay,highYear,highMonth,highDay;
    private String datasetName;
    private String[] varNames;
    private String[][] varContents;

    private Vector actionListeners;
    private String actionCommand;

    // String of URLs
    private DodsURL[] URLs;

    private String DODS_url;

    // DODS DAP objects
    private dods.dap.DConnect connect;
    private dods.dap.DAS dasObject;
    private dods.dap.DDS ddsObject;

    // DAS objects
    dods.dap.AttributeTable dods_global;

    // Title panel
    private JPanel titlePanel;

    // DateRange panel
    private DateRange dateRangePanel;

    // Variable panel
    private JPanel varPanel;

    // Gather URL" panel & button
    private JPanel gatherURLPanel;
    private JButton gatherURLButton;



    // *** Constructor ***
    public Inventory(DodsURL url) {

        // initialize variables
        DODS_url = url.getBaseURL();

        actionListeners = new Vector();
        actionCommand = "";

        // Remove the constraint expression from the url
        int endOfURL = DODS_url.indexOf(".ascii?");
        if(endOfURL != -1) {
            DODS_url = DODS_url.substring(0, endOfURL);
        }

        info = new InventoryGatherInfo(DODS_url);

        // This is a thread, but currently it is not being used as such
        info.run();		// gather the info


        drawGUI();
    }


    // Draw the GUI interface
    private void drawGUI()
    {
        if ( info.isFileserver() ) {

            // format the panel
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);
            GridBagConstraints c = new GridBagConstraints();
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	    setBorder(
		      BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Inventory Search"), this.getBorder())
		  );
            // create dataset title
            datasetName = info.getDatasetName();
            if (datasetName == null) {
                try {
                    if (ddsObject == null) {
                        connect = new dods.dap.DConnect(DODS_url);
                        ddsObject = connect.getDDS();
                    }
                } catch(Exception e) {System.out.println("\nERROR:dds");}
                datasetName = ddsObject.getName();
            }
            JLabel title = new JLabel("<html><font color=\"black\">"+datasetName+"</font></html>");
            titlePanel = new JPanel();
            titlePanel.setBorder(BorderFactory.createCompoundBorder(
                                     BorderFactory.createEtchedBorder(),
                                     BorderFactory.createEmptyBorder(5,5,2,2)));
            titlePanel.add(title);
            c.gridy = 0;
            c.insets = new Insets(0,0,20,20);  //padding
            gridbag.setConstraints(titlePanel,c);
            c.gridy = 1;
            c.insets = new Insets(5,5,5,5);  //padding
            add(titlePanel);

            // create variable selection panels
            if (info.variablesExist()) {
                varNames = info.getVariableNames();
                varContents = info.getVariableContents();
                if (varNames != null) {
                    varPanel = new JPanel();
                    varPanel.setBorder(BorderFactory.createCompoundBorder(
                                           BorderFactory.createTitledBorder("Variable Constraints"),
                                           BorderFactory.createEmptyBorder(5,5,5,5)));
                    varPanel.setLayout(new FlowLayout());
                    c.anchor = GridBagConstraints.NORTH;
                    c.gridwidth = 1;
                    for (int i = 0; i < varNames.length; i++) {
                        Variable tvar = new Variable(varNames[i],varContents[i]);
                        varPanel.add(tvar);
                    }
                    gridbag.setConstraints(varPanel,c);
                    add(varPanel);
                    c.gridy += 1;
                }
            }

            // create date range selection panel
            if (info.timeExists()) {
                lowYear = info.getLowYear();
                lowMonth = info.getLowMonth();
                lowDay = info.getLowDay();
                highYear = info.getHighYear();
                highMonth = info.getHighMonth();
                highDay = info.getHighDay();

                int t_lowYear = getMin(lowYear,highYear);
                if (t_lowYear != lowYear) {
                    int t_lowMonth = highMonth;
                    int t_lowDay = highDay;
                    highYear = lowYear;
                    highMonth = lowMonth;
                    highDay = lowDay;
                    lowYear = t_lowYear; lowMonth = t_lowMonth; lowDay = t_lowDay;
                }

                if ( (lowYear == highYear) && (lowYear == 1) ) {
                    if ( (lowDay == highDay) ) {	// monthly dataset
                        dateRangePanel = new DateRange(lowMonth,highMonth);
                    }
                } else if ( (lowDay == highDay) ) { 	// multi-year monthly
                    dateRangePanel = new DateRange(lowYear,lowMonth,highYear,highMonth);
                } else { 	// yearly
                    dateRangePanel = new DateRange(lowYear,lowMonth,lowDay,highYear,highMonth,highDay);
                }

                c.gridx = 0;
                gridbag.setConstraints(dateRangePanel,c);
                c.gridy += 1;
                add(dateRangePanel);
            }

            // create "gather URL" button
            gatherURLButton = new JButton("Gather URLs");
            gatherURLButton.setVerticalTextPosition(AbstractButton.CENTER);
            gatherURLButton.setHorizontalTextPosition(AbstractButton.LEFT);
            gatherURLButton.setToolTipText("Gather the URLs that meet the specified constraints.");
            gatherURLButton.setActionCommand("gather");
            gatherURLButton.addActionListener(this);
            // create a panel to hold it
            gatherURLPanel = new JPanel();
            gatherURLPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            gatherURLPanel.add(gatherURLButton);
            c.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(gatherURLPanel,c);
            // add it to the main panel
            add(gatherURLPanel);
        } else {
            System.out.println("Not a Fileserver");
        }
    }




    // --- Implementation of ActionListener interface ---
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("gather")) {
            String CE = formConstraintExpression();
            DataDDS data = null;

	    if (CE.equals("")) {
		System.out.println("Updating Interface...");
		JLabel error = new JLabel("<html><font color=\"black\"><b> Error: negative date range! Please select again. </font></b></html>");
		LayoutManager gridbag = getLayout();
		GridBagConstraints c = ((GridBagLayout)gridbag).getConstraints(gatherURLPanel);
		
		c.gridx = 0;
                c.gridy += 1;
		c.anchor = GridBagConstraints.WEST;
		((GridBagLayout)gridbag).setConstraints(error, c);
		add(error);
		validate();
		//getRootPane().getContentPane().repaint();
	    }

	    else {
		try {
		    connect = new dods.dap.DConnect(DODS_url);
		    data = connect.getData(CE,null);
		} catch(Exception e) {e.printStackTrace();}
		
		try {
		    if (data != null) {
			Enumeration tvar = data.getVariables();
			DSequence seq = (DSequence) tvar.nextElement();
			tvar = seq.getVariables();
			
			String urlName = ((DString) tvar.nextElement()).getName();
			String dateName = "";
			if (tvar.hasMoreElements()) { 	// get date field
			    dateName = ((DString) tvar.nextElement()).getName();
			}
			
			URLs = new DodsURL[seq.getRowCount()];
			for (int i = 0; i < seq.getRowCount(); i++) {
			    URLs[i] = new DodsURL(((DString) seq.getVariable(i,urlName)).getValue(), DodsURL.DATA_URL);
			    if (dateName != "") {
				URLs[i].setTitle( ((DString) seq.getVariable(i,dateName)).getValue()
						  + " - " + datasetName
						  + " - " + URLs[i].getBaseURL() );
			    }
			}
			
			ActionEvent evt = new ActionEvent(this, 0, actionCommand);
			
			for(int i=0;i<actionListeners.size();i++) {
			    ((ActionListener)actionListeners.elementAt(i)).actionPerformed(evt);
			}
		    }
		} catch(Exception e) {e.printStackTrace();}
	    }
	}
    }



    public void setActionCommand(String command) {
        actionCommand = command;
    }

    public void addActionListener(ActionListener a) {
        actionListeners.addElement(a);
	System.out.println(actionListeners.size());
    }



    // --- Form the constraint expression ---
    private String formConstraintExpression() {
        String ce = "?DODS_URL";
        String fields = "";
        String varChoose = "";
        String dateChoose = "";
        if (info.getDateFields() != "") {
            fields = "," + info.getDateFields();
        } else { 	// last hope
            fields = ",DODS_Date(" + info.getSequenceName() + ")";
        }

        if (info.variablesExist()) {
            for (int i = 0; i < info.getNumVariables(); i++) {
                String[] selVars = ((Variable) varPanel.getComponent(i)).getSelectedItems();
                if (selVars.length > 0) {
                    varChoose += "&" + varNames[i] + "={";
                    for (int z = 0; z < selVars.length; z++) {
                        if (z != 0) { varChoose += ","; }
                        varChoose += "\"" + selVars[z] + "\"";
                    }
                    varChoose += "}";
                }
            }
        }
        if (info.timeExists()) {
	    //
	    // Deal with negative date ranges
	    //
	    if (dateRangePanel.getLowYear() > dateRangePanel.getHighYear()
		|| (dateRangePanel.getLowYear() == dateRangePanel.getHighYear()&& (dateRangePanel.getLowMonth() > dateRangePanel.getHighMonth() || (dateRangePanel.getLowMonth() == dateRangePanel.getHighMonth() && dateRangePanel.getLowDay() > dateRangePanel.getHighDay())))) 
	    {
		System.out.println("Error: negative date range!");
		
		//Set ce to empty string. This will cause an error message 
		//in GUI with "gather" command.
		ce = "";
	    }
	    //if date range is not negative
	    else {
            dateChoose += "&date(\""
                          + Integer.toString(dateRangePanel.getLowYear()) + "/"
                          + Integer.toString(dateRangePanel.getLowMonth()) + "/"
                          + Integer.toString(dateRangePanel.getLowDay()) + "\",\""
                          + Integer.toString(dateRangePanel.getHighYear()) + "/"
                          + Integer.toString(dateRangePanel.getHighMonth()) + "/"
                          + Integer.toString(dateRangePanel.getHighDay()) + "\")";
	    ce += fields + varChoose + dateChoose;
	    }
	}
        //ce += fields + varChoose + dateChoose;
        return ce;
    }



    // --- Returns true if this dataset is a fileserver ---
    public boolean isFileserver() {
        return info.isFileserver();
    }


    // --- URL access method ---
    public DodsURL[] getURLs() {
        return URLs;
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



    // main method
    public static void main(String[] args) {
        if (args.length > 0) {
            Inventory inv = new Inventory(new DodsURL(args[0], DodsURL.DATA_URL));

            if (inv != null) {
                // Create a frame and container for the panels.
                JFrame inventoryFrame = new JFrame("Inventory");

                // Set the look and feel.
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch(Exception e) {}

                inventoryFrame.setContentPane(inv);

                // Exit when the window is closed.
                // inventoryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                inventoryFrame.setLocation(50,50);
                inventoryFrame.pack();
                inventoryFrame.setVisible(true);
            }
        } else {
            System.out.println("\nUsage: java Inventory [URL]");
        }
    }
}
