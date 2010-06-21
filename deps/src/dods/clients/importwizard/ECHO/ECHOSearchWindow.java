package dods.clients.importwizard.ECHO;

//import dods.clients.importwizard.SearchInterface;
import dods.clients.importwizard.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gnu.regexp.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;

/** 
 * This class displays a window for initiating a search query
 *
 * @author Zhifang(Sheila Jiang)
 */
public class ECHOSearchWindow extends SearchInterface 
    implements ActionListener 
{
    private Vector actionListeners;
    private String actionCommand;
    private JTabbedPane tabbedPane;
    private JSplitPane discoveryPanel;
    private JSplitPane granulePanel;
    private JPanel spatialPanel;
    private JPanel temporalPanel; 
    private JPanel buttonPanel;
    private JScrollPane resultValidsPanel;
    private JButton nextButton;
    private JButton previousButton;
    private JButton submitButton;
    private JButton cancelButton;
    private DodsURL[] urls;
       
    /**
     * Create a new <code>DiscoverySearchPanel/code>
     */
    public ECHOSearchWindow() {
	//super("ECHO Search Wizard");
	actionListeners = new Vector();
	resultValidsPanel = new ResultValidsPanel(true);//default is discovery 
	discoveryPanel = new DiscoverySearchPanel("/home/DODS/Java-DODS/ECHO_static_valids.xml");

	tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Discovery", discoveryPanel);
        tabbedPane.setSelectedIndex(0);
	//granulePanel = makeTextPanel("Blah blah");//to be changed
	granulePanel = new GranuleSearchPanel("/home/DODS/Java-DODS/ECHO_static_valids.xml");
	tabbedPane.addTab("Granule", granulePanel);
	spatialPanel = new SpatialPanel();
        tabbedPane.addTab("Spatial", spatialPanel);
	temporalPanel = new JPanel();//temp
        tabbedPane.addTab("Temporal", temporalPanel);

	nextButton = new JButton("Next >");
	previousButton = new JButton("< Previous");
	submitButton = new JButton("Submit");
	cancelButton = new JButton("Cancel");
	
        //
	// Setup the button bar at the bottom
	//
        buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

	buttonPanel.add(Box.createHorizontalGlue());
	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

	cancelButton.setActionCommand("cancel");
	cancelButton.addActionListener(this);
	buttonPanel.add(cancelButton);

	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

	previousButton.setActionCommand("previous");
	previousButton.addActionListener(this);
	previousButton.setEnabled(false);
	buttonPanel.add(previousButton);

	nextButton.setActionCommand("next");
	nextButton.addActionListener(this);
	buttonPanel.add(nextButton);

	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	submitButton.setActionCommand("submit");
	submitButton.addActionListener(this);
	submitButton.setEnabled(false);
	buttonPanel.add(submitButton);

	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	buttonPanel.add(Box.createHorizontalGlue());
	
	//add title info
	tabbedPane.setBorder(BorderFactory.createTitledBorder("EOS ClearingHOuse Search"));
	//add tabbed panel and button panel
	setLayout(new BorderLayout());
	add(tabbedPane, BorderLayout.CENTER);
	add(buttonPanel, BorderLayout.SOUTH);

	//pack();
	
    }
           
    /**
     * Add an action listener to each combo box.  It will receive action events
     * when an item is selected.
     * @param a The <code>ActionListener</code>.
     */
    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }

    /** 
     * Set the action command.
     * @param command The command used when the button is clicked.
     */
    public void setActionCommand(String command) {
	actionCommand = command;
    }

    /**
     * Catch events from the GUI components and pass them on to the 
     * action listeners.
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	if(command.equals("next")) {
	    if (tabbedPane.getSelectedIndex() == 0)
		resultValidsPanel = new ResultValidsPanel(true);
	    else if (tabbedPane.getSelectedIndex() == 1) 
		resultValidsPanel = new ResultValidsPanel(false);
	    tabbedPane.setVisible(false);
	    resultValidsPanel.setVisible(true);
	    remove(tabbedPane);	    
	    add(resultValidsPanel, BorderLayout.CENTER);	    
	    //pack();
	    submitButton.setEnabled(true);
	    previousButton.setEnabled(true);
	    nextButton.setEnabled(false);
	    getRootPane().getContentPane().repaint();
	}
	else if(command.equals("previous")) {
	    tabbedPane.setVisible(true);
	    resultValidsPanel.setVisible(false);
	    remove(resultValidsPanel);	    
	    add(tabbedPane, BorderLayout.CENTER);	    
	    //pack();
	    submitButton.setEnabled(false);
	    previousButton.setEnabled(false);
	    nextButton.setEnabled(true);
	    getRootPane().getContentPane().repaint();
	}
	else if (command.equals("submit")) {
	    if (tabbedPane.getSelectedIndex() == 0) {
		Vector temp = ((DiscoverySearchPanel)discoveryPanel).getQueryValids();
		Vector resultValids = ((ResultValidsPanel)resultValidsPanel).getResultValids();

	    /*debug
	    for (int i=0;i<temp.size();i++){
		CollectionValids theList = (CollectionValids)temp.elementAt(i);
		System.out.println("\n" + theList.getName() + "\t" + theList.isSelected());    
		for (int j=0;j<theList.getValids().length;j++){
		    System.out.println(theList.getValids()[j] + "   " + theList.getSelection(j));
		}
	    }*/

		// call methods to build and submit query
		SpatialQuery spatial = new SpatialQuery();
		spatial.buildSpatialQuery(((SpatialPanel)spatialPanel).getEasternmost(), ((SpatialPanel)spatialPanel).getWesternmost(), ((SpatialPanel)spatialPanel).getNorthernmost(), ((SpatialPanel)spatialPanel).getSouthernmost(), ((SpatialPanel)spatialPanel).getKeywords());

		TemporalQuery temporal = new TemporalQuery();
		XMLOutputter myXMLOutputter = new XMLOutputter();
		SOAPMessanger myMessanger = new SOAPMessanger();
	     
		DiscoveryQuery myQuery = new DiscoveryQuery();
		//Document outDoc = myQuery.buildQueryRequest(spatial, temporal, temp, resultValids);
		Document outDoc = myMessanger.exeQuery(myQuery.buildQueryRequest(spatial, temporal, temp, resultValids));
		//display result		
		myQuery.getPresentResult().displayResult(outDoc, true);
	
		//popup granule window
//		System.out.println("Before generate granule panel...");
		granulePanel = new GranuleSearchPanel("/home/DODS/Java-DODS/ECHO_static_valids.xml", outDoc);
//		System.out.println("after generate granule panel...");	
		//resultValidsPanel = new ResultValidsPanel(false);
		tabbedPane.setVisible(true);
		resultValidsPanel.setVisible(false);
		remove(resultValidsPanel);
		tabbedPane.setComponentAt(1, (Component)granulePanel);
		tabbedPane.setSelectedIndex(1);
		add(tabbedPane, BorderLayout.CENTER);	    
		//pack();
		submitButton.setEnabled(false);
		previousButton.setEnabled(true);
		nextButton.setEnabled(true);
		getRootPane().getContentPane().repaint();

		//output to screen (debug)
		String myXML = myXMLOutputter.outputString(outDoc); 
		System.out.println("The following output is converted from a JDOM Document.");
		System.out.println(myXML);	
	    }
	    else if (tabbedPane.getSelectedIndex() == 1) {
		System.out.println("\nGranule Search");
		Vector temp = ((GranuleSearchPanel)granulePanel).getQueryValids();
		Vector resultValids = ((ResultValidsPanel)resultValidsPanel).getResultValids();

	    	// call methods to build and submit query
		SpatialQuery spatial = new SpatialQuery();
		spatial.buildSpatialQuery(((SpatialPanel)spatialPanel).getEasternmost(), ((SpatialPanel)spatialPanel).getWesternmost(), ((SpatialPanel)spatialPanel).getNorthernmost(), ((SpatialPanel)spatialPanel).getSouthernmost(), ((SpatialPanel)spatialPanel).getKeywords());

		TemporalQuery temporal = new TemporalQuery();
		XMLOutputter myXMLOutputter = new XMLOutputter();
		SOAPMessanger myMessanger = new SOAPMessanger();
	     
		GranuleQuery myQuery = new GranuleQuery();
		//Document outDoc = myQuery.buildQueryRequest(spatial, temporal, temp, resultValids);
		Document outDoc = myMessanger.exeQuery(myQuery.buildQueryRequest(spatial, temporal, temp, resultValids));
		//output to screen
		String myXML = myXMLOutputter.outputString(outDoc); 
		System.out.println("The following output is converted from a JDOM Document.");
		System.out.println(myXML);
		//display result
		myQuery.getPresentResult().displayResult(outDoc, false);
	    }
	    getRootPane().getContentPane().repaint();
	}
	else if(command.equals("cancel")) {
	    setVisible(false);
	}    
    }

    //Temporally used for granule panel
    protected Component makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    public static void main(String args[]) {
	JFrame window = new JFrame("ECHO Search Wizard");
	ECHOSearchWindow ECHOWin = new ECHOSearchWindow();
	window.getContentPane().add(ECHOWin);
	window.pack();
	window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
	window.setLocation(50,50);

	window.setVisible(true);
    }

    //To be implemented
    public DodsURL[] getURLs(){
	return urls;
    }

}


