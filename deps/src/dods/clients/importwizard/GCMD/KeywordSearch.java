package dods.clients.importwizard.GCMD;

// Import all of java...
import dods.clients.importwizard.*;
import dods.clients.importwizard.ECHO.*;
import java.lang.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;  
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.swing.table.*;
import gnu.regexp.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.input.DOMBuilder;

/**
 * 
 */
public class KeywordSearch extends SearchInterface
    implements ActionListener, ListSelectionListener
{
    String url;

    DefaultHandler handler;
    Hashtable difs;
    Hashtable difPanels;
    Vector searchPanels;

    GridBagLayout gridbag;
    GridBagConstraints c;

    JPanel centerPanel;
    JPanel tempPanel;

    JTable idTable;
    JScrollPane idTableScroller;

    JPanel topPanel;
    JScrollPane topScroller;
    JPanel buttonPanel;
    //JComboBox logicTypeBox;
    JButton searchButton;
    JButton resetButton;
    //JButton showAllButton;
    JPanel bottomPanel;
    JPanel infoPanel;
    //JButton returnButton;    
    JComboBox category;
    JList topicList;
    JList termAndVar;
    Document outXMLDoc;
    
    public KeywordSearch(String baseURL) {
	url = baseURL;
	difs = new Hashtable();
	difPanels = new Hashtable();
	searchPanels = new Vector();
	handler = new DifHandler();
	
	try {//extract info from an xml file on the web
	    //xml = new File("http://gcmd.nasa.gov/servlets/md/get_valids.py?type=parametersvalid");
	    //xml = new File("get_valids.py");
	    // convert a file to a JDOM Document
	    DOMBuilder domBuilder = new DOMBuilder(false);
	    outXMLDoc = domBuilder.build(new URL("http://gcmd.nasa.gov/servlets/md/get_valids.py?type=parametersvalid"));
	} catch(NullPointerException ex){
	    System.err.println("\n File doesn't exist.");
	    System.err.println(ex.getMessage());
	} catch(JDOMException ex){
	    System.err.println("\nXML file convertion to Document failed.");
	    System.err.println(ex.getMessage());
	} catch(Exception ex){
	    System.err.println(ex.getMessage());
	}
	
	initGUI();
    }

    private void initGUI() {
	idTable = new JTable();
	idTableScroller = new JScrollPane(idTable);

	topPanel = new JPanel();
	topScroller = new JScrollPane(topPanel);
	infoPanel = new JPanel();
	centerPanel = new JPanel();
	buttonPanel = new JPanel();
	//String[] logicTypes = { "AND", "OR" };
	//logicTypeBox = new JComboBox(logicTypes);
	searchButton = new JButton("Search");
	resetButton = new JButton("Reset");

	gridbag = new GridBagLayout();
	c = new GridBagConstraints();

	//
	// Setup the search panel
	//
	topScroller.setPreferredSize(new Dimension(600,97));
	topScroller.setMinimumSize(new Dimension(600,97));
	topPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

	Vector s = new Vector();
	s.add(new String("EARTH SCIENCE"));
	category = new JComboBox(s);
	category.setPreferredSize(new Dimension(100,20));
	category.setMinimumSize(new Dimension(100,20));
	
	//SearchPanel search = new SearchPanel();
	//search.addActionListener(this);
	//search.setActionCommands("togglePanel", "search");
	//search.setMaximumSize(new Dimension(32768,30));
	//searchPanels.addElement(search);
	topPanel.add(category);
	//search.setEnabled(true);
	
	String[] topics = {"Agriculture", "Atmosphere", "Biosphere", "Cryosphere", "Human  Dimensions", "Hydrosphere", "Land Surface", "Oceans", "Paleoclimate", "Radiance Or Imagery", "Solid Earth", "Sun Earth Interactions"};
	
	topicList = new JList(topics);
	topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	topicList.addListSelectionListener(this);

	JScrollPane topicScroller = new JScrollPane(topicList);
	topicScroller.setPreferredSize(new Dimension(150, 30));
	topicScroller.setMinimumSize(new Dimension(150,30));

	topPanel.add(topicScroller);

	termAndVar = new JList();
	JScrollPane termScroller = new JScrollPane(termAndVar);
	termScroller.setPreferredSize(new Dimension(300, 30));
	termScroller.setMinimumSize(new Dimension(300,30));

	topPanel.add(termScroller);
	//search = new SearchPanel();
	//search.addActionListener(this);
	//search.setActionCommands("togglePanel", "search");
	//search.setMaximumSize(new Dimension(32768,30));
	//searchPanels.addElement(search);
	//topPanel.add(search);
	//search.setEnabled(false);
	
	

	//
	// Setup the button panel
	//
		
	JPanel temp = new JPanel();

	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
	buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2,2,2,2)));

	//temp.add(new JLabel("Logic:"));
	//temp.add(logicTypeBox);
	buttonPanel.add(temp);
	buttonPanel.add(Box.createVerticalGlue());
	searchButton.addActionListener(this);
	searchButton.setActionCommand("search");
	searchButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
	buttonPanel.add(searchButton);

	resetButton.addActionListener(this);
	resetButton.setActionCommand("reset");
	resetButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
	buttonPanel.add(resetButton);

	buttonPanel.add(Box.createVerticalGlue());
	
	//
	// Setup the ID list
	// 
	centerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Matching Datasets"));
	centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
	idTableScroller.setBackground(java.awt.Color.white);
	
	Object[] selected = { };
	Object[] ids = { };
	idTable.setPreferredScrollableViewportSize(new Dimension(600, 300));
	idTable.setModel(new GCMDTableModel(selected, ids));
	idTable.getColumnModel().getColumn(0).setMaxWidth(80);
	idTable.getSelectionModel().addListSelectionListener(this);
	centerPanel.add(idTableScroller);

	infoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dataset Information"));
	infoPanel.setPreferredSize(new Dimension(600,50));

	//
	// Add the components into the main panel
	//
	setLayout(gridbag);
	
	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 1;
	c.weighty = 0;
	c.fill = GridBagConstraints.BOTH;
	gridbag.setConstraints(topScroller, c);
	add(topScroller);

	c.gridx = 1;
	c.gridy = 0;
	c.weightx = 0;
	c.weighty = 0;
	c.fill = GridBagConstraints.BOTH;
	gridbag.setConstraints(buttonPanel, c);
	add(buttonPanel);

	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 2;
	c.weightx = 1;
	c.weighty = 1;
	gridbag.setConstraints(centerPanel, c);
	add(centerPanel);

	c.gridx = 0;
	c.gridy = 2;
	c.gridwidth = 2;
	c.weightx = 1;
	c.weighty = 0;
	gridbag.setConstraints(infoPanel, c);
	add(infoPanel);

    }

    protected Dif getDif(String difName, String sections) {
	//DefaultHandler handler = new DifHandler();
	//SAXParserFactory factory = SAXParserFactory.newInstance();
	/*
	  Extracts info from Dif returned from the first query. No needs to make another query.

	try {
	    //URL xmlFile = new URL(url + "/getdif.py?entry_id=" + difName
	    URL xmlFile = new URL(url + "/getdifs.py?query=" 
	    		  + URLEncoder.encode("[Project:Short_Name='DODS']") + "&entry_id=" + difName + "&format=xml&sections=" + sections);
	    System.out.println(url + "/getdifs.py?query=" 
	    	       + URLEncoder.encode("[Project:Short_Name='DODS']") + "&entry_id=" + difName + "&format=xml&sections=" + sections);

	    //System.out.println(url + "/getdif.py?entry_id=" + difName
	    //	       + "&format=xml&sections=" + sections);
	    InputStream urlStream = xmlFile.openStream();
	    SAXParser saxParser = factory.newSAXParser();
	    saxParser.parse( urlStream, handler );

	}
	catch(Throwable t) {
	    t.printStackTrace();
	    }
	*/

	return (Dif)((DifHandler)handler).getDifs().elementAt(0);
    }

    /**
     * The function to handle action events.
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	/*
	// This event is generated when the user clicks the checkbox next
	// to a search field.  Enable the search panel if it's disabled and
	// if it's the last one, create a new disabled search panel.
	if(command.equals("togglePanel")) {
	    if(searchPanels.lastElement().equals(e.getSource())
	       && ((SearchPanel)e.getSource()).isEnabled() == false)
		{
		    SearchPanel search = new SearchPanel();
		    search.setEnabled(false);
		    search.setMaximumSize(new Dimension(32768, 30));
		    search.addActionListener(this);
		    search.setActionCommands("togglePanel", "search");
		    searchPanels.addElement(search);
		    topPanel.add(search);
		    validate();
		}
	    
	    ((SearchPanel)e.getSource()).toggleEnabled();
	    }*/

	// 
	if(command.equals("reset")) {
	    topicList.clearSelection();
	    termAndVar.setListData(new Vector());
	}

	// Triggered by either the search button, or by hitting enter
	// inside one of the text fields.  Build a search string from
	// the enabled search panels, and get all the datasets that match.
	else if(command.equals("search")) {
	    String searchString = "";
	    String category = new String("EARTH SCIENCE");
	    
	    //if no <code>topic</code> selected, show all
	    if (topicList.getSelectedValue() == null) {
		try { 
			RE spaces = new RE(" ");
			category = spaces.substituteAll(category, "%20");
		} catch(Exception ex) {}
		searchString += "[Parameters:Category='" + category + "'";
	    }

	    else
		{
		    String topic = topicList.getSelectedValue().toString().toUpperCase();
		    try { 
			RE spaces = new RE(" ");
			category = spaces.substituteAll(category, "%20");
			topic = spaces.substituteAll(topic, "%20");
		    } catch(Exception ex) {} 

		    searchString += "[Parameters:Category='" + category + "',Topic='" + topic + "'";

		    Object[] select = termAndVar.getSelectedValues();

		    // if no <code>term</code> selected, show all with this
		    // <code>topic</code>
		    if (select.length > 0) {
		
			int indexOfSep = select[0].toString().indexOf('>');
			String term = select[0].toString().substring(0, indexOfSep-1);
			String var = select[0].toString().substring(indexOfSep+2);
			// if variable is empty, show all with this 
			// <code>term</code>
			if (var.equals("")) {
			    try { 
				RE spaces = new RE(" ");
				term = spaces.substituteAll(term, "%20");
			    } catch(Exception ex) {}
			    searchString +=  ",Term='" + term + "'";
			}
			else {
			    try { 
				RE spaces = new RE(" ");
				term = spaces.substituteAll(term, "%20");
				var = spaces.substituteAll(var, "%20");
			    } catch(Exception ex) {}
			    
			    searchString +=  ",Term='" + term + "',Variable='" + var + "'";
			    // if more than one selected
			    for(int i=1;i<select.length;i++) {
				
				indexOfSep = select[i].toString().indexOf('>');
				term = select[i].toString().substring(0, indexOfSep-1);
				var = select[i].toString().substring(indexOfSep+2);
				
				try { 
				    RE spaces = new RE(" ");
				    term = spaces.substituteAll(term, "%20");
				    var = spaces.substituteAll(var, "%20");
				} catch(Exception ex) {}
				
				searchString += "] OR " + "[Parameters:Category='" + category + "',Topic='" + topic + "',Term='" + term + "',Variable='" + var + "'"; 
			    }
			}
		    }
		}
	    
	    searchString += "]"; 
	    
	    //add spatial query
	    JPanel spatial = ((GCMDSearch)getParent().getParent()).getSpatial();
	    if (((SpatialPanel)spatial).spatialIsSet()) {
		//southernmost
		String value = ((SpatialPanel)spatial).getSouthernmost().toUpperCase();
		try { 
		    RE spaces = new RE(" ");
		    value = spaces.substituteAll(value, "%20");
		} catch(Exception ex) {}
		searchString += " AND " + "[Spatial_Coverage:Southernmost_Latitude='" + value + "',";

		//northernmost
		value = ((SpatialPanel)spatial).getNorthernmost().toUpperCase();
		try { 
		    RE spaces = new RE(" ");
		    value = spaces.substituteAll(value, "%20");
		} catch(Exception ex) {}
		searchString += "Northernmost_Latitude='" + value + "',";

		//westernmost
		value = ((SpatialPanel)spatial).getWesternmost().toUpperCase();
		try { 
		    RE spaces = new RE(" ");
		    value = spaces.substituteAll(value, "%20");
		} catch(Exception ex) {}
		searchString += "Westernmost_Longitude='" + value + "',";

		//easternmost
		value = ((SpatialPanel)spatial).getEasternmost().toUpperCase();
		try { 
		    RE spaces = new RE(" ");
		    value = spaces.substituteAll(value, "%20");
		} catch(Exception ex) {}
		searchString += "Easternmost_Longitude='" + value + "']";
	    }

	    SearchThread search = new SearchThread(searchString);

	    tempPanel = search.createTempPanel();
	    tempPanel.setMinimumSize(centerPanel.getSize());
	    tempPanel.setPreferredSize(centerPanel.getSize());
	    centerPanel.setVisible(false);

	    c.gridx = 0;
	    c.gridy = 1;
	    c.gridwidth = 2;
	    c.weightx = 1;
	    c.weighty = 1;
	    gridbag.setConstraints(tempPanel, c);

	    remove(centerPanel);
	    add(tempPanel);

	    search.start();
	}

	else if(command.equals("getSummary")) {
	    JFrame summaryWindow = new JFrame();
	    JTextArea summaryText = new JTextArea();
	    JScrollPane summaryScroller = new JScrollPane(summaryText);
	    int index = idTable.getSelectionModel().getMinSelectionIndex();
	    Dif dif = (Dif)idTable.getValueAt(index,1);

	    summaryText.setEditable(false);

	    if(dif.getSummary().equals("")) {
		//Dif temp = getDif(dif.getID(), "Summary");
		//dif.setSummary(temp.getSummary());
		//if(dif.getSummary().equals(""))
		    dif.setSummary("No Summary Available");
	    }

	    summaryText.setText(dif.getSummary());
	    summaryWindow.setTitle(dif.getTitle());
	    summaryWindow.getContentPane().add(summaryScroller);
	    summaryWindow.setSize(new Dimension(550,275));
	    summaryWindow.setVisible(true);
	}

	else if(command.equals("getGeneralInfo")) {
	    JFrame infoWindow = new JFrame();
	    JTextArea infoText = new JTextArea();
	    JScrollPane infoScroller = new JScrollPane(infoText);
	    int index = idTable.getSelectionModel().getMinSelectionIndex();
	    Dif dif = (Dif)idTable.getValueAt(index,1);

	    infoText.setEditable(false);
	    
	    String generalInfo = "";
	    if(dif.getPersonnels() == null) {
		//Dif temp = getDif(dif.getID(), "Summary");
		//dif.setSummary(temp.getSummary());
		//if(dif.getSummary().equals(""))
		generalInfo = generalInfo + "No Contact Infomation Available";
	    }
	    else {
		generalInfo = generalInfo + "Contact Information";
		for(int i=0; i<dif.getPersonnels().size(); i++) {
		    generalInfo = generalInfo + "\n\nRole   ";
		    generalInfo = generalInfo + dif.getRole(i);
		    generalInfo = generalInfo + "\nName   ";
		    generalInfo = generalInfo + dif.getName(i);
		    generalInfo = generalInfo + "\nEmail   ";
		    generalInfo = generalInfo + dif.getEmail(i);
		    generalInfo = generalInfo + "\nPhone   ";
		    generalInfo = generalInfo + dif.getPhone(i);
		    generalInfo = generalInfo + "\nFax   ";
		    generalInfo = generalInfo + dif.getFax(i);
		    generalInfo = generalInfo + "\nAddress   ";
		    generalInfo = generalInfo + dif.getAddress(i);
		}
	    }
    
	    if(dif.getParameters() == null) {
		generalInfo = generalInfo + "No Parameters Available";
	    }
	    else { 
		generalInfo = generalInfo + "\n\nParameters\n";
		for(int i=0; i<dif.getParameters().size(); i++) {
		    generalInfo = generalInfo + "\n";
		    generalInfo = generalInfo + dif.getCategory(i) + " > " 
			+ dif.getTopic(i) + " > " + dif.getTerm(i) + " > "
			+ dif.getVariable(i);
		}
	    }

	    if(dif.getSpatialCoverage() == null) {
		//Dif temp = getDif(dif.getID(), "Summary");
		//dif.setSummary(temp.getSummary());
		//if(dif.getSummary().equals(""))
		generalInfo = generalInfo + "No Spatial Coverage Infomation Available";
	    }
	    else { 
		generalInfo = generalInfo + "\n\nSpatial Coverage";
		generalInfo = generalInfo + "\n\nSouthernmost Latitude: ";
		generalInfo = generalInfo + dif.getSouthernmost();
		generalInfo = generalInfo + "\nNorthernmost Latitude: ";
		generalInfo = generalInfo + dif.getNorthernmost();
		generalInfo = generalInfo + "\nWesternmost Longitude: ";
		generalInfo = generalInfo + dif.getWesternmost();
		generalInfo = generalInfo + "\nEasternmost Longitude: ";
		generalInfo = generalInfo + dif.getEasternmost();
	    }

	    if(dif.getTemporalCoverage() == null) {
		//Dif temp = getDif(dif.getID(), "Summary");
		//dif.setSummary(temp.getSummary());
		//if(dif.getSummary().equals(""))
		generalInfo = generalInfo + "No Temporal Coverage Infomation Available";
	    }
	    else { 
		generalInfo = generalInfo + "\n\nTemporal Coverage";
		generalInfo = generalInfo + "\n\nStart Date: ";
		generalInfo = generalInfo + dif.getStartDate();
		generalInfo = generalInfo + "\nStop Date: ";
		generalInfo = generalInfo + dif.getStopDate();
		
	    }
	    
	    if(dif.getDataResolution() == null) {
		//Dif temp = getDif(dif.getID(), "Summary");
		//dif.setSummary(temp.getSummary());
		//if(dif.getSummary().equals(""))
		generalInfo = generalInfo + "No Data Resolution Infomation Available";
	    }
	    else { 
		generalInfo = generalInfo + "\n\nData Resolution";
		generalInfo = generalInfo + "\n\nLatitude Resolution: ";
		generalInfo = generalInfo + dif.getLatResolution();
		generalInfo = generalInfo + "\nLongitude Resolution: ";
		generalInfo = generalInfo + dif.getLongResolution();
		generalInfo = generalInfo + "\nTemporal Resolution: ";
		generalInfo = generalInfo + dif.getTemporalResolution();
	    }

	    if(dif.getSummary().equals("")) {
		//Dif temp = getDif(dif.getID(), "Summary");
		//dif.setSummary(temp.getSummary());
		//if(dif.getSummary().equals(""))
		//dif.setSummary("No Summary Available");
		generalInfo = generalInfo + "No Summary Infomation Available";
	    }
	    else 
		generalInfo = generalInfo + "\n\nSummary" + dif.getSummary();

	    infoText.setText(generalInfo);
	    infoWindow.setTitle(dif.getTitle());
	    infoWindow.getContentPane().add(infoScroller);
	    infoWindow.setSize(new Dimension(550,275));
	    infoWindow.setVisible(true);
	}
    }

    /**
     * The function to handle list selection events.
     * @param e The event.
     */
    public void valueChanged(ListSelectionEvent e) {
	if(!e.getValueIsAdjusting()) {
	    Object o = e.getSource();
	    if (o == topicList) {
		if (topicList.getSelectedValue() != null) {
		    Vector valids = new Vector();

		    try {//get the desired valids
			Element root = outXMLDoc.getRootElement();
			java.util.List parameters = root.getChildren(); 
			String select = topicList.getSelectedValue().toString().toUpperCase();
			for (int i=0; i<parameters.size(); i++) {
			    
			    if (((Element)parameters.get(i)).getChildText("Category").equals("EARTH SCIENCE") && ((Element)parameters.get(i)).getChildText("Topic").equals(select)) {
				String temp = ((Element)parameters.get(i)).getChildText("Term") + " > " + ((Element)parameters.get(i)).getChildText("Variable");  
				valids.add(temp);
			    }
			}
			
		    } catch(NullPointerException ex){
			System.err.println("\n File doesn't exist.");
			System.err.println(ex.getMessage());
		    } catch(Exception ex){
			System.err.println(ex.getMessage());
		    }
		    
		    termAndVar.setListData(valids);
		}
	    }
	    else {
		int index = ((ListSelectionModel)e.getSource()).getMinSelectionIndex();
		if(index != -1) {
		    
		    JPanel difPanel;
		    JPanel innerPanel;
		    Dif dif = (Dif)idTable.getModel().getValueAt(index,1);
		    
		    if( (difPanel = (JPanel)difPanels.get(dif)) == null) {
			
			difPanel = new JPanel();
			difPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dataset Information"));
			difPanel.setLayout(new BoxLayout(difPanel, BoxLayout.Y_AXIS));
			difPanel.add(new JLabel("<html><table style=\"color:black\"><tr><td valign=\"top\">Title:</td><td>" + dif.getTitle() + "</td></tr><tr><td>URL:</td><td>" + dif.getDodsURL().getBaseURL() + "</td></tr></table>"));
			
			innerPanel = new JPanel();
			innerPanel.setBorder(BorderFactory.createEtchedBorder());
			innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
			JButton summary = new JButton("Get Summary");
			summary.addActionListener(this);
			summary.setActionCommand("getSummary");
			innerPanel.add(summary);
			
			JButton general = new JButton("Get General Info");
			general.addActionListener(this);
			general.setActionCommand("getGeneralInfo");
			innerPanel.add(general);
			
			innerPanel.setAlignmentX(LEFT_ALIGNMENT);
			difPanel.add(innerPanel);
			difPanels.put(dif, difPanel);
		    }
		    
		    infoPanel.setVisible(false);
		    difPanel.setVisible(true);
		    remove(infoPanel);
		    infoPanel = difPanel;
		    c.gridx = 0;
		    c.gridy = 2;
		    c.weightx = 1;
		    c.gridwidth = 2;
		    c.weighty = 0;
		    gridbag.setConstraints(infoPanel,c);
		    add(infoPanel);
		}
	    }
	    
	}
	
    }
    

    
    /**
     * Returns all the urls that have the "Download" box checked.
     * @return all the urls that have the "Download" box checked.
     */
    public DodsURL[] getURLs() {
	int numURLs = 0;
	for(int i=0;i<idTable.getRowCount();i++) {
	    if(((Boolean)idTable.getValueAt(i,0)).booleanValue())
		numURLs++;
	}

	DodsURL[] urls = new DodsURL[numURLs];
	int urlIndex = 0;

	for(int i=0;i<idTable.getRowCount();i++) {
	    if(((Boolean)idTable.getValueAt(i,0)).booleanValue()) {
		Dif dif = (Dif)idTable.getValueAt(i,1);
		urls[urlIndex++] = dif.getDodsURL();
	    }
	}

	return urls;
    }
    /*
    public static void main(String args[]) {
	
	JFrame frame = new JFrame("GCMD");
	frame.getContentPane().add(new GCMDSearch("http://128.183.164.60/servlets/md/"));
	frame.pack();
	frame.setVisible(true);
    }
    */
    
    /** 
     * This class makes a request to the GCMD servlets and 
     * displays the results when it's done.
     */
    public class SearchThread extends Thread 
	implements ActionListener
    {
	String query;
	boolean drawTable;

	public SearchThread(String queryString) {
	    query = queryString;
	    drawTable = true;
	}

	public void run() {
	    handler = new DifHandler();
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    
	    try {
		URL xmlFile;

		if(query.equals(""))
		    xmlFile = new URL(url + "/getdifs.py?query="
				      + URLEncoder.encode("[Project:Short_Name='DODS']")); // + "&sections=Entry_ID+Entry_Title+Related_URL+Personnel+Spatial_Coverage+Summary");
		else
		    xmlFile = new URL(url + "/getdifs.py?query="
		       + URLEncoder.encode("[Project:Short_Name='DODS'] AND (" 
					   + query + ")"));// + "&sections=Entry_ID+Entry_Title+Related_URL+Personnel+Spatial_Coverage+Summary");

		System.out.println(url + "/getdifs.py?query="
		     + URLEncoder.encode("[Project:Short_Name='DODS'] AND (" 
				   + query + ")")); 
				   // + "&sections=Entry_ID+Entry_Title+Related_URL+Summary");
		InputStream urlStream = xmlFile.openStream();

		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse( urlStream, handler );
	       	       
		// If the request has been canceled, drawTable will be false
		// at this point.
		if(drawTable) {
		    Vector idVector = ((DifHandler)handler).getDifs();
		    Object[] selected = new Boolean[idVector.size()];
		    Object[] ids = new Object[idVector.size()];
		    idVector.copyInto(ids);
		    for(int i=0;i<ids.length;i++) {
			selected[i] = new Boolean(false);
		    }
		    
		    c.gridx = 0;
		    c.gridy = 1;
		    c.gridwidth = 2;
		    c.weightx = 1;
		    c.weighty = 1;
		    gridbag.setConstraints(centerPanel, c);
		    
		    centerPanel.setMinimumSize(tempPanel.getSize());
		    centerPanel.setPreferredSize(tempPanel.getSize());

		    int size0=idTable.getColumnModel().getColumn(0).getWidth();
		    int size1=idTable.getColumnModel().getColumn(1).getWidth();
		    
		    // This is a rather clunky way of doing things, but for 
		    // some reason JDK1.1 won't size the columns correctly
		    // unless I create a new table.
		    idTable = new JTable();
		    idTable.setModel(new GCMDTableModel(selected, ids));
		    idTable.getColumnModel().getColumn(0).setPreferredWidth(size0);
		    idTable.getColumnModel().getColumn(0).setMaxWidth(80);
		    idTable.getColumnModel().getColumn(1).setPreferredWidth(size1);
		    idTableScroller.setViewportView(idTable);
		    idTable.getSelectionModel().addListSelectionListener(KeywordSearch.this);

		    tempPanel.setVisible(false);
		    add(centerPanel);
		    centerPanel.setVisible(true);
		    remove(tempPanel);
		    
		    getRootPane().getContentPane().validate();
		}
	    }
	    catch(Throwable t) {
		t.printStackTrace();
	    }
	}

	/**
	 * Create a temporary panel that can be used to cancel the search
	 * request.
	 * @return a temporary panel to show while searching.
	 */
	public JPanel createTempPanel() {
	    JPanel temp = new JPanel();
	    JButton cancelButton = new JButton("Cancel");
	    temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
	    temp.setBorder(BorderFactory.createEmptyBorder(10,20,10,10));
	    temp.add(Box.createVerticalGlue());
	    JLabel text = new JLabel("Searching, please wait...");
	
	    text.setAlignmentX(Component.CENTER_ALIGNMENT);
	    temp.add(text);
	    cancelButton.addActionListener(this);
	    cancelButton.setActionCommand("cancel");
	    cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    temp.add(cancelButton);
	    temp.add(Box.createVerticalGlue());
	    return temp;
	} 

	public void actionPerformed(ActionEvent e) {
	    if(e.getActionCommand().equals("cancel")) {
		drawTable = false;
		    
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weightx = 1;
		c.weighty = 1;
		gridbag.setConstraints(centerPanel, c);

		centerPanel.setVisible(true);
		centerPanel.setMinimumSize(tempPanel.getSize());
		centerPanel.setPreferredSize(tempPanel.getSize());
		tempPanel.setVisible(false);

		remove(tempPanel);
		add(centerPanel);
	    }
	}
    }

}
