package dods.clients.importwizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

import gnu.regexp.*;

/**
 * This class will gather the URLs from a dods directory and allow the user
 * to choose which ones they want.  It does not support directories inside
 * directories or most directories with non-standard output.
 *
 * @author rhonhart
 */
public class DodsDirectory extends InventoryURLProcessor
    implements ActionListener
{
    private URL directory;
    private String directoryName;
    private RE dataset;
    private Vector actionListeners;
    private String actionCommand;

    private JList urlList;
    private JLabel directoryLabel;
    private JScrollPane urlListScroller;
    private JPanel buttonPanel;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private JButton gatherButton;

    private DefaultListModel urls;

    /**
     * Create a new DodsDirectory.
     * @param directoryName the URL of the directory.
     */
    public DodsDirectory(DodsURL directoryName) {
	actionListeners = new Vector();
	actionCommand = "";
	
	try{
	    directory = new URL(directoryName.getBaseURL());
	}
	catch (MalformedURLException e) { e.printStackTrace(); }
	    
	urls = getDataURLs();

	initGUI();
    }

    /** 
     * Initialize the GUI components of the class.
     */
    private void initGUI() {
	urlList = new JList(urls);
	urlListScroller = new JScrollPane(urlList);

	buttonPanel = new JPanel();
	selectAllButton = new JButton("Select All");
	deselectAllButton = new JButton("Deselect All");
	gatherButton = new JButton("Gather URLs");
	directoryLabel = new JLabel(directory.toString());

	directoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	
	urlListScroller.setPreferredSize(new Dimension(400,200));

	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	buttonPanel.add(Box.createHorizontalGlue());
	selectAllButton.addActionListener(this);
	selectAllButton.setActionCommand("selectAll");
	buttonPanel.add(selectAllButton);
	deselectAllButton.addActionListener(this);
	deselectAllButton.setActionCommand("deselectAll");
	buttonPanel.add(deselectAllButton);
	buttonPanel.add(Box.createHorizontalGlue());

	gatherButton.addActionListener(this);
	gatherButton.setActionCommand("gather");
	gatherButton.setAlignmentX(Component.LEFT_ALIGNMENT);

	setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	add(Box.createVerticalGlue());
	add(directoryLabel);
	add(Box.createRigidArea(new Dimension(10,10)));
	add(urlListScroller);
	add(buttonPanel);
	add(Box.createRigidArea(new Dimension(10,10)));
	add(gatherButton);
	add(Box.createVerticalGlue());

	if(urls.size() == 0) {
	    JOptionPane.showMessageDialog(this,"<html><center><p>DodsImport "
				    + " was able to connect to "
				    + "the URL, however no Dods URLs were " 
				    + "found.  <p>This means that either the "
				    + "directory contained only "
				    + "subdirectories, <p>or listed the "
				    + "directory in a non-standard way ",
				    "Error", JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Read the html representation of a dods directory, and look for 
     * any dods URLs.
     */
    protected DefaultListModel getDataURLs() {
	BufferedReader in;
	DefaultListModel urls = new DefaultListModel();

	try {
	    in = new BufferedReader(new InputStreamReader(directory.openStream()));
	    String line;    
	    while( (line = in.readLine()) != null ) {
		
		// 
		// gnu.regexp just isn't fast enough to handle this regexp
		//
		//REMatch match = dataset.getMatch(line);
		//if(match != null) 
		//    urls.addElement(new DodsURL(match.toString(1) 
		//                                + match.toString(2),
		//				  DodsURL.DATA_URL));
		String url;
		if( (url = getDataURL(line)) != null) {
		    urls.addElement(new DodsURL(url, DodsURL.DATA_URL));
		}
	    }
	}
	catch(IOException e) {
	    e.printStackTrace();
	}

	return urls;
    }

    /** 
     * Check to see if a line of html represent a link to a dods URL.
     * if it does, return the URL, if not, return <code>null</code>.
     * @param line The line of html.
     */
    protected String getDataURL(String line) {
	String dataURL = null;
	int startIndex;
	int endIndex;

	startIndex = line.indexOf("HREF=") + 5;
	endIndex = line.indexOf("</A>");

	if(startIndex != 4 && endIndex != -1) {
	    String url = line.substring(startIndex,endIndex);
	    String name = url.substring(url.lastIndexOf(">") + 1);
	    int nameIndex;
	    if( (nameIndex = url.indexOf(name + ".html")) != -1) {
		dataURL = url.substring(0,nameIndex + name.length());
	    }
	}	    
	
	return dataURL;
    }

    /** 
     * The function to handle action events.
     */
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();

	if(command.equals("cancel")) {
	    
	}
	else if(command.equals("selectAll")) {
	    urlList.setSelectionInterval(0,urls.size() - 1);
	}
	else if(command.equals("deselectAll")) {
	    urlList.clearSelection();
	}
	else if(command.equals("gather")) {
	    ActionEvent a = new ActionEvent(this, 0, actionCommand);
	    for(int i=0;i<actionListeners.size();i++) {
		((ActionListener)actionListeners.elementAt(i)).actionPerformed(a);
	    }
	}
    }

    public void setActionCommand(String command) {
	actionCommand = command;
    }

    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }

    /** 
     * Return whatever urls have been selected by the user.
     * @return all selected urls.
     */
    public DodsURL[] getURLs() {
	int[] inds = urlList.getSelectedIndices();
	DodsURL[] retValue = new DodsURL[inds.length];

	for(int i=0;i<inds.length;i++) {
	    retValue[i] = (DodsURL)urls.elementAt(inds[i]);
	}

	return retValue;
    }

    public static void main(String[] args) {
	new DodsDirectory(new DodsURL(args[0],0));
    }


}
