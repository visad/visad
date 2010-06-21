/**
 * DatasetList.java
 *
 * 1.00 2001/7/26
 *
 */

package dods.clients.importwizard.DatasetList;

import dods.clients.importwizard.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;


/**
 * This class provides the base structure for
 * the DatasetList application.
 *
 * @version     1.00 26 Jul 2001 
 * @author      Kashan A. Shaikh
 *
 * @modified by Sheila Jiang 12 Mar 2002
 */
public class DatasetList extends SearchInterface
            implements ActionListener
{
    private String xmlFile;

    private DOMTree xmlDOMTree;

    // Dimensions
    static final int windowHeight = 400;
    static final int leftWidth = 400;
    static final int rightWidth = 400;
    static final int windowWidth = leftWidth + rightWidth;

    private JPanel treePanel;
    private JSplitPane windowSplitPane;
    private JPanel treeSelectionInfoPanel;
    private DListSearch searchPanel;



    //
    // Constructor
    //
    public DatasetList(String fname) {
        xmlFile = fname;

        xmlDOMTree = new DOMTree(xmlFile);

        // Selection window
        JPanel outerInfoPanel = new JPanel();
        outerInfoPanel.setLayout(new BoxLayout(outerInfoPanel,BoxLayout.Y_AXIS));
        outerInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        outerInfoPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        outerInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                                     BorderFactory.createTitledBorder("Selected Datasets"),
                                     BorderFactory.createEmptyBorder(2,2,2,2)));
        treeSelectionInfoPanel = new JPanel();
        treeSelectionInfoPanel.setLayout(new BoxLayout(treeSelectionInfoPanel,BoxLayout.Y_AXIS));
        addTreeSelectionInterface();
        JScrollPane treeSelectionInfoScrollPane = new JScrollPane(treeSelectionInfoPanel);
        treeSelectionInfoScrollPane.setPreferredSize(new Dimension( rightWidth, windowHeight ));
        outerInfoPanel.add(treeSelectionInfoScrollPane);


        // Build xml Tree view
        JScrollPane treeView = new JScrollPane(xmlDOMTree);
        treeView.setPreferredSize(
            new Dimension( leftWidth, windowHeight ));
        treePanel = new JPanel();
        treePanel.setLayout(new BoxLayout(treePanel,BoxLayout.Y_AXIS));
        treePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        treePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        EmptyBorder eb = new EmptyBorder(5,5,5,5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);
        treePanel.setBorder(new CompoundBorder(cb,eb));
        treePanel.setLayout(new BorderLayout());
        treePanel.add(treeView,BorderLayout.CENTER);

        // Put tree view and selection view into a splitPane
        windowSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,treePanel,outerInfoPanel);
        windowSplitPane.setContinuousLayout( true );
        windowSplitPane.setDividerLocation( leftWidth );
        windowSplitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        windowSplitPane.setAlignmentY(Component.TOP_ALIGNMENT);
        windowSplitPane.setPreferredSize(
            new Dimension( windowWidth+10, windowHeight+10 ));
        windowSplitPane.setOneTouchExpandable(true);

        // Search Panel
        searchPanel = new DListSearch();
        searchPanel.setBorder(BorderFactory.createTitledBorder("DODS Dataset List Search"));
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        searchPanel.addActionListener(this);

        // Add components
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.TOP_ALIGNMENT);
        add(searchPanel);
        add(windowSplitPane);
    }


    //
    // Update Selection Pane
    //
    private void addTreeSelectionInterface() {
        xmlDOMTree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    Object[] nodes = xmlDOMTree.getSelection();
		    if (nodes != null) {
			treeSelectionInfoPanel.removeAll();
			for (int i=0; i < nodes.length; i++) {
			    DOMTree.AdapterNode thisnode = (DOMTree.AdapterNode) nodes[i];
			    JLabel label = new JLabel();
			    String text = "";
			    if (thisnode.isLeaf()) {
				if (xmlDOMTree.numDesiredURLAttributes(thisnode) > 0) {
				    text = "<html>";
				    text += "<div style=\"color:black; font-size:12pt; text-align:left\">";
				    text += "<b>Title: ";
				    text += thisnode.getAttributes().getNamedItem(DOMTree.ATTR_NAME).getNodeValue();
				    text += "</b>";
				    for (int u=0; u < xmlDOMTree.desiredURLAttributes.length; u++) {
					if (thisnode.getAttributes().getNamedItem(DOMTree.desiredURLAttributes[u]) != null) {
					    text += "<br>";
					    text += xmlDOMTree.desiredURLAttributes[u] + ": ";
					    text += thisnode.getAttributes().getNamedItem(DOMTree.desiredURLAttributes[u]).getNodeValue();
					}
				    }
				    text += "</div></html>";
				}
			    } else {		// aggregated dataset item
				text = "<html>";
				text += "<div style=\"color:black; font-size:12pt; text-align:left\">";
				text += "<b>";
				text += thisnode.getAttributes().getNamedItem(DOMTree.ATTR_NAME).getNodeValue();
				text += "</b>";
				text += "</div></html>";
			    }
			    
			    label.setText(text);
			    treeSelectionInfoPanel.add(label);
			}
			updateUI();
		    }
		}
	    });
    }


    //
    // Returns DodsUrl object with selected URLs
    //
    public DodsURL[] getURLs() {
        Hashtable urlshash = new Hashtable();
        Vector urlsvect = new Vector();
        Object[] nodes = xmlDOMTree.getSelection();
	if (nodes != null) {
            for (int i=0; i < nodes.length; i++) {
                DOMTree.AdapterNode thisnode = (DOMTree.AdapterNode) nodes[i];
                if (thisnode.isLeaf()) {
                    if (thisnode.getAttributes().getNamedItem(DOMTree.ATTR_BASE_URL) != null) {
                        String key = thisnode.getAttributes().getNamedItem(DOMTree.ATTR_BASE_URL).getNodeValue();
                        if (! urlshash.containsKey(key)) {
                            urlshash.put(key,key);
                            urlsvect.addElement(thisnode);
                        }
                    }
                } else {	// aggregated entry
                    for (int s=0; s < thisnode.childCount(); s++) {
                        if (thisnode.getChild(s).getAttributes().getNamedItem(DOMTree.ATTR_BASE_URL) != null) {
                            String key = thisnode.getChild(s).getAttributes().getNamedItem(DOMTree.ATTR_BASE_URL).getNodeValue();
                            if (! urlshash.containsKey(key)) {
                                urlshash.put(key,key);
                                urlsvect.addElement(thisnode.getChild(s));
                            }
                        }
                    }
                }
            }
        }

        if (urlsvect.size() > 0) {
            DodsURL[] urls = new DodsURL[urlsvect.size()];
            for (int i=0; i < urlsvect.size(); i++) {
                urls[i] = new DodsURL();
                DOMTree.AdapterNode thisnode = (DOMTree.AdapterNode) urlsvect.elementAt(i);
                urls[i].setURL(thisnode.getAttributes().getNamedItem(DOMTree.ATTR_BASE_URL).getNodeValue());
                if (thisnode.getAttributes().getNamedItem(DOMTree.ATTR_CATALOG) != null) {
                    urls[i].setType(DodsURL.CATALOG_URL);
                } else if (thisnode.getAttributes().getNamedItem(DOMTree.ATTR_DIR) != null) {
                    urls[i].setType(DodsURL.DIRECTORY_URL);
                } else {
                    urls[i].setType(DodsURL.DATA_URL);
                }

                urls[i].setTitle(thisnode.getAttributes().getNamedItem(DOMTree.ATTR_NAME).getNodeValue());
            }
            return urls;
        } else {
            return null;
        }
    }



    //
    // Implementation of ActionListener interface.
    //
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(DListSearch.EVENT_SEARCH)) {
            Vector keywords = searchPanel.getSearchKeywords();
            Vector types_group = searchPanel.getGroupLogicTypes();
            Vector types_global = searchPanel.getGlobalLogicTypes();
            int searchResult = xmlDOMTree.constrainTree(
                                   (Object[]) keywords.elementAt(0), (String) types_group.elementAt(0),
                                   (String) types_global.elementAt(0),
                                   (Object[]) keywords.elementAt(1), (String) types_group.elementAt(1) );
            switch(searchResult)
            {
            case DOMTree.SEARCH_NULL:
                JOptionPane.showMessageDialog(this,"Invalid Search.");
                break;
            case DOMTree.SEARCH_NO_MATCH:
                JOptionPane.showMessageDialog(this,"The search returned no matches.");
                break;
            case DOMTree.SEARCH_ERROR:
                JOptionPane.showMessageDialog(this,"An error was encountered during the search.");
                break;
            }
        } else if (event.getActionCommand().equals(DListSearch.EVENT_SHOW_ALL)) {
            xmlDOMTree.removeConstraints();
        }
    }


    //
    // main method
    //
    public static void main(String[] args) {
        if (args.length > 0) {
            DatasetList datalist = new DatasetList(args[0]);

            if (datalist != null) {
                // Create a frame and container for the panels.
                JFrame datasetListFrame = new JFrame("DatasetList");

                // Set the look and feel.
                try {
                    UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
                } catch(Exception e) {}

                datasetListFrame.setContentPane(datalist);

                // Exit when the window is closed.
                // This constant doesn't exist in JDK1.1.  rph 08/15/01
                // datasetListFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                datasetListFrame.pack();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int w = windowWidth + 10;
                int h = windowHeight + 10;
                datasetListFrame.setLocation(screenSize.width/3 - w/2, screenSize.height/2 - h/2);
                datasetListFrame.setSize(w, h);
                datasetListFrame.setVisible(true);
            }
        } else {
            System.out.println("\nUsage: java DatasetList xmlfile");
        }
    }

}

