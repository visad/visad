package dods.clients.importwizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;  
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.*;

/** 
 * This class creates and displays various interfaces to search for Dods 
 * datasets. The names of the interfaces are read from the <SearchWindow> tag 
 * in dodsimport.conf.xml.  Each interface is defined by the tag <Interface>.
 * As <code>SearchWindow</code> goes through the XML file, it will create a tab
 * in a JTabbedPane to hold the interface.
 * 
 * An example of a dodsimport.conf.xml: 
 *
 * <SearchWindow>
 *   <Interface shortname="displayName" url="url">ClassName</Interface>
 * </SearchWindow>
 *
 * This file would create a tab with the text <code>displayName<code> which 
 * held a class of type <code>ClassName</code> that was initialized with the 
 * String <code>url</code>.
 *
 * shortname is required, while url is optional provided the class has 
 * a default constructor
 *
 * @author rhonhart
 */

public class SearchWindow extends JFrame 
    implements ActionListener
{
    private JTabbedPane interfaceTabs;
    private JPanel bottomPanel;
    private JButton cancelButton;
    private JButton getButton;

    private Vector interfaces;
    private Vector actionListeners;
    private String actionCommand;
    private DodsURL[] urls;

    public SearchWindow() 
	throws FileNotFoundException
    {
	super("Search For DODS Datasets");
	
	interfaces = new Vector();
	interfaceTabs = new JTabbedPane();
	actionListeners = new Vector();
	actionCommand = "";
	bottomPanel = new JPanel();
	getButton = new JButton("Get URLs");
	cancelButton = new JButton("Cancel");
	urls = new DodsURL[0];

	bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
	bottomPanel.add(Box.createHorizontalGlue());

	cancelButton.addActionListener(this);
	cancelButton.setActionCommand("cancel");
	bottomPanel.add(cancelButton);

	getButton.addActionListener(this);
	getButton.setActionCommand("geturls");
	bottomPanel.add(getButton);

	bottomPanel.setBackground(Color.gray);

	DefaultHandler handler = new ConfFileHandler();
	SAXParserFactory factory = SAXParserFactory.newInstance();
	File xmlFile = new File("dodsimport.conf.xml");
	if(!xmlFile.exists()) 
	    xmlFile = new File("~/.dodsimport/dodsimport.conf.xml");

	if(!xmlFile.exists()) {
	    throw new FileNotFoundException("Could not find "
					    + "dodsimport.conf.xml in either "
					    + "the current directory or in "
					    + "~/.dodsimport/");
	}
	else {
	    try {
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse( xmlFile, handler ); 
	    } catch(Throwable t) {}
	    
	    getContentPane().setLayout(new BorderLayout());
	    getContentPane().add(interfaceTabs, BorderLayout.CENTER);
	    getContentPane().add(bottomPanel, BorderLayout.SOUTH);

	    pack();
	}

    }

    public void actionPerformed(ActionEvent e) {

	if(e.getActionCommand().equals("geturls")) {
	    SearchInterface search = (SearchInterface)interfaceTabs.getSelectedComponent();
	    int option = JOptionPane.YES_OPTION;
	    urls = search.getURLs();

	    if(urls.length == 0) {
		option = JOptionPane.showConfirmDialog(this, "You currently have no URLs selected to download, Proceed?", "Proceed?",  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	    }

	    if(option != JOptionPane.NO_OPTION) {
		ActionEvent a = new ActionEvent(this, 0, actionCommand);
		for(int i=0;i<actionListeners.size();i++) {
		    ((ActionListener)actionListeners.elementAt(i)).actionPerformed(a);
		}
	    }
	}

	else if(e.getActionCommand().equals("cancel")) {
	    ActionEvent a = new ActionEvent(this, 0, actionCommand);
	    for(int i=0;i<actionListeners.size();i++) {
		((ActionListener)actionListeners.elementAt(i)).actionPerformed(a);
	    }
	}
    }

    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }
    public void setActionCommand(String command) {
	actionCommand = command;
    }

    public DodsURL[] getURLs() {
	return urls;
    }

    public class ConfFileHandler extends DefaultHandler {
	boolean insideInterface;
	
	String className;
	String shortName;
	String parameter;
	
	public ConfFileHandler() {
	    insideInterface = false;
	    shortName = parameter = "";
	    className = "";
	}
	
	public void startElement(String namespaceURI,
				 String lName, // local name
				 String qName, // qualified name
				 Attributes attrs)
	    throws SAXException
	{
	    insideInterface = false;
	    
	    if(qName.equals("Interface")) {
		insideInterface = true;
		shortName = attrs.getValue("shortname");
		parameter = attrs.getValue("url");
	    }
	}
	
	public void endElement(String namespaceURI,
			       String sName, // simple name
			       String qName  // qualified name
			       )
	    throws SAXException
	{
	    insideInterface = false;
	    
	    if(qName.equals("Interface")) {
		Class searchClass;
		Class[] paramTypes;
		Object[] params = { };
		java.lang.reflect.Constructor[] searchConstructors;
		
		try {
		    searchClass = Class.forName(className);
		    searchConstructors = searchClass.getConstructors();
		    
		    paramTypes = searchConstructors[0].getParameterTypes();
		    if(paramTypes.length == 1
		       && paramTypes[0].getName().equals("java.lang.String"))
			{
			    params = new Object[1];
			    params[0] = parameter;
			    interfaceTabs.addTab(shortName, (JComponent)searchConstructors[0].newInstance(params));
			}
		    else if(paramTypes.length == 0) {
			interfaceTabs.addTab(shortName, (JComponent)searchConstructors[0].newInstance(params));
		    }
		}
		catch(ClassNotFoundException err) { 
		    System.out.println("Could not find class "
				       + className + ".  Check to see that "
				       + "your classpath is setup correctly "
				       + "and that your dodsimport.conf.xml "
				       + "is configured correctly");
		}
		catch(InstantiationException err) { err.printStackTrace(); }
		catch(IllegalAccessException err) { err.printStackTrace(); }
		catch(java.lang.reflect.InvocationTargetException err) { 
		    System.out.println("The interface class " + className
				       + " Generated the following exception "
				       + "during invocation:");
		    err.getTargetException().printStackTrace(); 
		}
	    }
	}
	
	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	    String s = new String(buf, offset, len);
	    if(insideInterface)
		if(!s.equals(""))
		    className = s;
	}
    }
}
