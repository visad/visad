package dods.clients.importwizard;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.*;

//import java.util.Timer;
//import java.util.TimerTask;

/**
 * This is the main interface for DodsImport.  It allows the user to enter or
 * select Dods URLs and then further constrain them using other classes
 *
 * @author rhonhart
 */
public class URLBuilder extends JPanel
    implements TreeSelectionListener
{
    
    Hashtable panels;   
    ActionHandler handler;
    Object[] oldSelectedValues;

    JPanel currentPanel;
    SearchWindow search;
    JPanel urlPanel;
    JTextField urlField;
    JButton urlSelectorButton;
    
    //JPanel inventoryListPanel;
    JPanel urlListPanel;
    URLList urlList;
    
    JSplitPane splitPane;
    
    javax.swing.Timer timer;
    DodsURL baseURL;

    /**
     * Construct a <code>URLBuilder</code>
     */
    public URLBuilder() {
	super();

	panels = new Hashtable();
	handler = new ActionHandler();
	initGUI();
    }
    
    /**
     * Initialize the GUI components of the class.
     */
    private void initGUI() {
	urlList = new URLList();
	//inventoryScroller = new JScrollPane(inventoryList);
	urlListPanel = new JPanel();
	urlField = new JTextField("http://dodsdev.gso.uri.edu/cgi-bin/dods-3.1/nph-ff/avhrr.catalog");
	urlSelectorButton = new JButton("Search...");
	urlSelectorButton.setToolTipText("Click this button to enter the search interface for different database.");
	urlPanel = new JPanel();
	search = null;
	setLayout(new BorderLayout());
	
	currentPanel = new JPanel();
	currentPanel.setPreferredSize(new Dimension(480, 300));
	currentPanel.setMinimumSize(new Dimension(20,200));
	//
	// Setup the URL List.
	//
	//?GridBagLayout gridbag = new GridBagLayout();
	//?GridBagConstraints c = new GridBagConstraints();

	//?urlListPanel.setLayout(gridbag);
	//?c.fill = GridBagConstraints.BOTH; 
	urlListPanel.setLayout(new BorderLayout());

	urlList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "DODS URLs"));
	urlList.addTreeSelectionListener(this);
	
	urlListPanel.setPreferredSize(new Dimension(280,300));
	urlListPanel.setMinimumSize(new Dimension(20,200));
	urlListPanel.add(urlList, BorderLayout.CENTER);

	// 
	// Setup the field to enter the url
	//
	urlPanel.setLayout(new BoxLayout(urlPanel, BoxLayout.X_AXIS));
	urlPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
	urlPanel.add(new JLabel("DODS URL: "));
	
	urlField.addActionListener(handler);
	urlField.setActionCommand("urlField");
	urlPanel.add(urlField);
	
	urlPanel.add(Box.createRigidArea(new Dimension(30, 0)));
	
	urlSelectorButton.setActionCommand("select");
	urlSelectorButton.addActionListener(handler);
	urlPanel.add(urlSelectorButton);

	//
	//set up the split panel at the buttom
	//
	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                         currentPanel, urlListPanel);
	splitPane.setOneTouchExpandable(true);
	//splitPane.setDividerLocation(0.75);
	splitPane.resetToPreferredSizes();
	
	// 
	// Add all the panels to the main window
	//
	add(urlPanel, BorderLayout.NORTH);

	//add(urlListPanel, BorderLayout.EAST);      
	add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Find the indices of <code>objects</code> in <code>ListModel list</code>.
     * @param objects A Vector of objects whos index you want to find.
     * @param list The ListModel to look for the elements of 
     *             <code>objects</code> in.
     */
    protected static int[] getIndicesOfObjects(Vector objects, 
					       ListModel list) 
    {
	int[] indices = new int[0];
	int lastIndex = -1;
	int index = 0;

	if(objects != null && objects.size() > 0) {
	    indices = new int[objects.size()];

	    for(int i=0; i<objects.size(); i++) {
		for(int j=lastIndex+1; j<list.getSize(); j++) {
		    if(list.getElementAt(j).equals(objects.elementAt(i))) {
			lastIndex = j;
			indices[index++] = j;
			break;
		    }
		}
	    }
	}

	if(indices.length != index) {
	    int[] newIndices = new int[index];
	    for(int i=0;i<index;i++)
		newIndices[i] = indices[i];
	    indices = newIndices;
	}

	return indices;
    }

    /**
     * Applies the constraint expression for the current URL to all of 
     * the URLs selected in the URLList.
     */
    public void applyToSelectedURLs() {
	if(currentPanel instanceof DataURLProcessor) {
	    String ce = ((DataURLProcessor)currentPanel).getURL().getConstraintExpression();
	    for(int i=0;i<oldSelectedValues.length;i++) {
		if(oldSelectedValues[i] instanceof DodsURL)
		    ((DodsURL)oldSelectedValues[i]).setConstraintExpression(ce);
	    }
	}
    }

    /** 
     * The function that gets called for TreeSelectionEvents.
     */
    public void valueChanged(TreeSelectionEvent e) {

	JTree activeTree = (JTree)e.getSource();
	TreePath t = activeTree.getSelectionPath();

	// Make sure we don't call this twice for the same selection
	// and make sure that there is in fact something selected.
	if(t != null && t.getPathCount() > 0
	   && ((DefaultMutableTreeNode)t.getLastPathComponent()).getUserObject()
	   instanceof DodsURL) 
	    {
	    
	    baseURL = (DodsURL)((DefaultMutableTreeNode)t.getLastPathComponent()).getUserObject();
	    JPanel proc;
	    
	    // Check to see if we've already created an interface for this
	    // url.  If we haven't, create an instance of the class defined
	    // by baseURL.getProcessorName().
	    
	    if((proc = (JPanel)panels.get(baseURL)) == null) {
		InterfaceCreator creator = new InterfaceCreator(baseURL);
		
		// Create the message the user sees while the 
		// interface is being created.
		proc = creator.createTempPanel();

		//getSize may cause trouble -- sometimes it's 0
		//proc.setPreferredSize(currentPanel.getSize());
		//proc.setMinimumSize(currentPanel.getSize());
		proc.setPreferredSize(currentPanel.getPreferredSize());
		proc.setMinimumSize(currentPanel.getMinimumSize());
		
		splitPane.setVisible(false);
		proc.setVisible(true);
		//currentPanel.setVisible(false);
		//remove(currentPanel);
		currentPanel = proc;
		splitPane.setLeftComponent(proc);
		splitPane.setVisible(true);
		//add(proc, BorderLayout.CENTER);
		
		// Allows 30 seconds to create the interface
		timer = new javax.swing.Timer(30000, new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
                      /*
		       * Create a "time's up" panel that can be displayed 
		       * when the interface creating has taken too long and 
		       * might have failed.
		       */
		      
		      //Change to "time's up" panel only if the temporary panel is currently shown
		      if (currentPanel.getName() != null && 
			           currentPanel.getName().equals("temp")) {  
			  System.out.println("Time's up!");
		    
			  //
			  // create a panel to notify the failure
			  //
			  JPanel temp = new JPanel();
			  temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
			  temp.setBorder(BorderFactory.createEmptyBorder(10,20,10,10));
			  temp.add(Box.createVerticalGlue());
			  JLabel text = new JLabel("<html><font color=\"black\"><center><p>"
						   + "<b>Time is up for this request!</b><p>"
						   
						   + "Requested to create an interface "
						   + "for the Dods Inventory:<p>" 
						   + baseURL.toString() 
						   + "</center></font>"
						   );
			  
			  text.setAlignmentX(Component.CENTER_ALIGNMENT);
			  temp.add(text);
			  temp.add(Box.createVerticalGlue());
			  //set size
			  temp.setPreferredSize(currentPanel.getPreferredSize());
			  temp.setMinimumSize(currentPanel.getMinimumSize());
		    
			  //
			  //add temp 
			  //
			  splitPane.setVisible(false);
			  temp.setVisible(true);
			  currentPanel = temp;
			  splitPane.setLeftComponent(temp);
			  splitPane.setVisible(true);
			  getRootPane().getContentPane().validate();
		      }
		      timer.stop();
                  }
              });

		timer.start();

		//timer.schedule(new failMessage(baseURL), 30000);

		// Start up the thread to create the interface
		creator.start();
	    }
	    
	    else {
		if(proc instanceof DataURLProcessor) 
		    ((CEGenerator)proc).updateCE();
		
		//getSize may cause trouble -- sometimes it's 0
		//proc.setPreferredSize(currentPanel.getSize());
		//proc.setMinimumSize(currentPanel.getSize());
		proc.setPreferredSize(currentPanel.getPreferredSize());
		proc.setMinimumSize(currentPanel.getMinimumSize());
		
		//currentPanel.setVisible(false);
		splitPane.setVisible(false);
		proc.setVisible(true);
		//remove(currentPanel);
		currentPanel = proc;
		splitPane.setLeftComponent(proc);
		splitPane.setVisible(true);
		//add(proc, BorderLayout.CENTER);
	    }
	    
	    getRootPane().getContentPane().repaint();
	}

	else if(t == null) {

	    // This means that there are no urls selected, and we should
	    // get rid of whatever interface is currently open
	    splitPane.setVisible(false);
	    //currentPanel.setVisible(false);
	    //remove(currentPanel);

	    JPanel temp = new JPanel();
	    //getSize may cause trouble -- sometimes it's 0
	    //temp.setPreferredSize(currentPanel.getSize());
	    //temp.setMinimumSize(currentPanel.getSize());
	    temp.setPreferredSize(currentPanel.getPreferredSize());
	    temp.setMinimumSize(currentPanel.getMinimumSize());
	  
	    add(temp, BorderLayout.CENTER);
	    currentPanel = temp;
	    splitPane.setLeftComponent(temp);
	    splitPane.setVisible(true);
	    validate();
	}

	oldSelectedValues = urlList.getSelectedValues();
    }

      /** 
       * Returns an array of fully constrained URLs.
       * @return an array of fully constrained URLs.
       */
      public DodsURL[] getURLs() {
  	return urlList.getURLs();
      }

    /**
     * This class handles actions thrown by the various GUI componentes
     * of <code>URLBuilder</code>
     */

    private class ActionHandler extends Object implements ActionListener {

	public ActionHandler() {

	}
	
	/**
	 * This function must be defined by all action listeners
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    
  	    // Action to gather urls
  	    if(command.equals("doneProcessing")) {
  		doneProcessing(e);
  	    }
  	    // Action to add a url from the url field
  	    else if(command.equals("urlField")) {
  		urlField(e);
  	    }
  	    // Bring up the selection box
  	    else if(command.equals("select")) {
  		select(e);
  	    }
    	    // Get urls from the search window
    	    else if(command.equals("foundURLs")) {
    		foundURLs(e);
    	    }
	}

	/** 
	 * Called when a URLProcessor is done processing.  If the URLProcessor
	 * is a CEGenerator, get the CE from it.  Otherwise get the URLs and
	 * add them to the urlList.
	 * @param e The event.
	 */
	protected void doneProcessing(ActionEvent e) {
	    Object proc = e.getSource();

	    if(proc instanceof DataURLProcessor) 
		applyToSelectedURLs();

	    else if(proc instanceof InventoryURLProcessor) {
		DodsURL invURL = (DodsURL)urlList.getSelectedValue();
		DodsURL[] urls = ((InventoryURLProcessor)proc).getURLs();
		Vector urlVector;

		invURL.setProcessed(true);

		if(urlList.getURLCount(invURL) != 0)	{
		    int option = JOptionPane.showConfirmDialog(URLBuilder.this, "Do you want to replace the old urls selected from this inventory?", "Replace old URLs?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		    if(option == JOptionPane.YES_OPTION) {
			urlList.removeAllURLsFromInventory(invURL);
		    }
		}
		
		urlList.addURLsToInventory(urls, invURL);
		
	    }

	    getRootPane().getContentPane().repaint();
	}
	
	/**
	 * Get a URL from the textField at the top of the screen, figure
	 * out what type of URL it is, and add it to the appropriate list.
	 * @param e The event.
	 */
	public void urlField(ActionEvent e) {
	    if(urlField.getText().endsWith("/")) {
		DodsURL url = new DodsURL(urlField.getText(), DodsURL.DIRECTORY_URL);
		DodsDirectory dir = new DodsDirectory(url);
		urlList.addInventory(url);
		dir.setActionCommand("doneProcessing");
		dir.addActionListener(handler);
		panels.put(url, dir);
		
		// Selecting the url in the inventory list will tell the
		// program to create that interface and display it.
		// inventoryList.setSelectedIndex(inventoryModel.size() - 1);
	    }
	    else {
		DodsURL url = new DodsURL(urlField.getText(), DodsURL.CATALOG_URL);
		Inventory inv = new Inventory(url);
		if(inv.isFileserver()) {
		    panels.put(url, inv);
		    urlList.addInventory(url);
		    inv.setActionCommand("doneProcessing");
		    inv.addActionListener(handler);
		}
		else {
		    urlList.addURL(new DodsURL(urlField.getText(), DodsURL.DATA_URL));
		}
	    }
	}

	/**
	 * Open up the search window.  If this is the first time the button
	 * was pressed, a new search window will be created.  Otherwise the 
	 * old search window will simply be made visible.
	 * @param e The event.
	 */
	protected void select(ActionEvent e) {
	    if(search == null) {
		try {
		    search = new SearchWindow();
		    search.addActionListener(handler);
		    search.setActionCommand("foundURLs");
		    search.setLocation(50,50);
		    search.setVisible(true);
		}
		catch(java.io.FileNotFoundException excp) {
		    JOptionPane.showMessageDialog(URLBuilder.this, 
						  excp.getMessage(), "Error", 
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	    else 
		search.setVisible(true);
	}

	/** 
	 * Get URLs back from the search window and add them to the correct
	 * list.  Bring up the interface for the first URL that was returned.
	 * @param e The event.
	 */
	protected void foundURLs(ActionEvent e) {
	    DodsURL[] urls = search.getURLs();
	    search.setVisible(false);

	    if(urls != null) {
		for(int i=0;i<urls.length;i++) {
		    if(urls[i].getType() == DodsURL.CATALOG_URL
		       || urls[i].getType() == DodsURL.DIRECTORY_URL) 
			{
			    urlList.addInventory(urls[i]);
			}
		    else {
			urlList.addURL(urls[i]);
		    }
		}
	    }
	}

    }

    
    /** 
     * This class is used to create interfaces to dods inventory URLs in 
     * a non-blocking manner.
     */
    public class InterfaceCreator extends Thread 
	implements ActionListener
    {
	private DodsURL baseURL;
	private boolean drawPanel;

	/** 
	 * Create an <code>InterfaceCreator</code>
	 * @param url The url to create an interface for.
	 */
	public InterfaceCreator(DodsURL url) {
	    baseURL = url;
	    drawPanel = true;
	}

	/**
	 * Creates the interface, and adds it to the main panel when 
	 * it's done.
	 */
	public void run() {
	    Class procClass;
	    Class[] paramTypes;
	    Object[] params = { };
	    java.lang.reflect.Constructor procConstructors[];
	    JPanel proc = null;

	    try {
		System.out.println("Creating a " + baseURL.getProcessorName());
		procClass = Class.forName(baseURL.getProcessorName());
		procConstructors = procClass.getConstructors();
		
		// The first constructor declared in a URLProcessor
		// is the one that will be called
		paramTypes = procConstructors[0].getParameterTypes();
		if(paramTypes.length == 1 
		   && paramTypes[0].getName().endsWith("DodsURL")) 
		    {
			params = new Object[1];
			params[0] = baseURL;
			proc = (JPanel)procConstructors[0].newInstance(params);
		    }
		else if(paramTypes.length == 0) {
		    proc = (JPanel)procConstructors[0].newInstance(params);
		}
	    } 
	    catch(ClassNotFoundException err) { err.printStackTrace(); }
	    catch(InstantiationException err) { err.printStackTrace(); }
	    catch(IllegalAccessException err) { err.printStackTrace(); }
	    catch(java.lang.reflect.InvocationTargetException err) { 
		err.getTargetException().printStackTrace(); 
	    }
	    
	    // Make sure that proc has in fact been created and the user
	    // still wants to see it before drawing it or adding it to any
	    // hash tables.
	    if(proc != null && drawPanel) {
		//set size before adding to hash table
		//Use currentPanel.getSize may cause trouble-- sometimes it's 0
		proc.setPreferredSize(currentPanel.getPreferredSize());
		proc.setMinimumSize(currentPanel.getMinimumSize());
		if(proc instanceof InventoryURLProcessor) {
		    ((InventoryURLProcessor)proc).setActionCommand("doneProcessing");
		    ((InventoryURLProcessor)proc).addActionListener(handler);
		}

		panels.put(baseURL, proc);
		proc.setOpaque(true);
		//proc.setPreferredSize(currentPanel.getSize());
		//proc.setMinimumSize(currentPanel.getSize());
		//currentPanel.setVisible(false);
		splitPane.setVisible(false);
		proc.setVisible(true);
		//remove(currentPanel);
		currentPanel = proc;
		splitPane.setLeftComponent(proc);
		splitPane.setVisible(true);
		//add(proc, BorderLayout.CENTER);
		getRootPane().getContentPane().validate();
	    }	    
	}

	/**
	 * The function to handle action events.
	 */
	public void actionPerformed(ActionEvent e) {
	   
	    // Instead of actually killing the thread when the user hits 
	    // cancel (which is really messy), just set a boolean telling
	    // the object not to display and let the thread die quietly.
	    if(e.getActionCommand().equals("cancel")) {
		drawPanel = false;
		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));

		temp.add(Box.createVerticalGlue());

		JLabel text = new JLabel("<html><font color=\"black\"><b>"
					 + " Request cancelled</b></font>");
		text.setAlignmentX(Component.CENTER_ALIGNMENT);
		temp.add(text);

		temp.add(Box.createVerticalGlue());
		
		splitPane.setVisible(false);
		//currentPanel.setVisible(false);
		//remove(currentPanel);
		currentPanel = temp;
		splitPane.setLeftComponent(temp);
		splitPane.setVisible(true);
		//add(temp, BorderLayout.CENTER);
		
		validate();

	    }
	}

	/** 
	 * Create a temporary panel that can be displayed while the interface
	 * is being created.
	 * @return A temporary panel to show while the interface is being
	 *         created.
	 */
	public JPanel createTempPanel() {
	    JPanel temp = new JPanel();
	    //Set name for later recognization
	    temp.setName("temp");
	    JButton cancelButton = new JButton("Cancel");
	    temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
	    temp.setBorder(BorderFactory.createEmptyBorder(10,20,10,10));
	    temp.add(Box.createVerticalGlue());
	    JLabel text = new JLabel("<html><font color=\"black\"><center><p> "
				     + "Please wait, creating an interface "
				     + "for the Dods Inventory:<p>" 
				     + baseURL.toString() 
				     + "</center></font>"
				     );
	
	    text.setAlignmentX(Component.CENTER_ALIGNMENT);
	    temp.add(text);
	    cancelButton.addActionListener(this);
	    cancelButton.setActionCommand("cancel");
	    cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    temp.add(cancelButton);
	    temp.add(Box.createVerticalGlue());
	    return temp;
	}
    }	
}
