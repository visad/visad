package dods.clients.importwizard;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/**
 * This class display a hierarchial list of Dods URLs.  Inventories are shown
 * at the top level, and Data URLs selected from those inventories are shown
 * as children of the inventories.  The list supports basic operations such
 * as removing Data URLs or inventories, and throws events as 
 * <code>TreeSelectionEvent</code>s to allow for more complex manipulation
 * of URLs.
 *
 * @author Rich Honhart <rhonhart@po.gso.uri.edu>
 */

public class URLList extends JPanel 
    implements ActionListener
{
    private JTree urlTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode top;
    private DefaultMutableTreeNode single;
    private Vector listeners;
    private JScrollPane scroller;
    private Hashtable nodes;
    private int urlCount;

    /**
     * Create a new URLList.
     */
    URLList() {
	setLayout(new BorderLayout());
	top = new DefaultMutableTreeNode("Datasets");	
	nodes = new Hashtable();
	urlTree = new JTree(top);
	treeModel = (DefaultTreeModel)urlTree.getModel();
	scroller = new JScrollPane(urlTree);
	listeners = new Vector();
	urlCount = 0;

	// Create the popup menu for items in the list.
	JMenuItem menuItem;
	JPopupMenu urlPopup = new JPopupMenu();
        menuItem = new JMenuItem("Delete Selected");
        menuItem.addActionListener(this);
	menuItem.setActionCommand("deleteSelected");
        urlPopup.add(menuItem);

	// Create the popup menu for inventories in the list.
	JPopupMenu invPopup = new JPopupMenu();
	menuItem = new JMenuItem("Remove Inventory");
        menuItem.addActionListener(this);
	menuItem.setActionCommand("removeInventory");
        invPopup.add(menuItem);
	invPopup.addSeparator();
        menuItem = new JMenuItem("Select data URLs");
        menuItem.addActionListener(this);
	menuItem.setActionCommand("selectURLs");
        invPopup.add(menuItem);
	menuItem = new JMenuItem("Remove data URLs");
        menuItem.addActionListener(this);
	menuItem.setActionCommand("deleteURLs");
        invPopup.add(menuItem);

	MouseListener popupListener = new TreePopupListener(urlTree, urlPopup,
							    invPopup);
	urlTree.addMouseListener(popupListener);

	add(scroller, BorderLayout.CENTER);
    }

    /**
     * adjust the viewport so that it's all the way to the left.
     */
    private void adjustViewport() {
	JViewport viewport = scroller.getViewport();
	Point p = viewport.getViewPosition();
	p.x = 0;
	viewport.setViewPosition(p);
	scroller.setViewport(viewport);
    }

    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();

	if(command.equals("deleteSelected")) {
	    removeSelectedURLs();
	}
	else if(command.equals("removeInventory")) {
	    TreePath t = urlTree.getSelectionPath();
	    if(t != null) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastPathComponent();
		urlCount -= n.getChildCount();
		n.removeAllChildren();
		treeModel.removeNodeFromParent(n);
	    }
	}
	else if(command.equals("selectURLs")) {
	    TreePath parent = urlTree.getSelectionPath();
	    if(parent != null) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)parent.getLastPathComponent();
		DefaultMutableTreeNode temp;
		int ind = 0;
		TreePath[] t = new TreePath[n.getChildCount()];
		Enumeration children = n.children();

		while(children.hasMoreElements()) {
		    temp = (DefaultMutableTreeNode)children.nextElement();
		    t[ind++] = parent.pathByAddingChild(temp);
		}
		
		urlTree.setSelectionPaths(t);
	    }
	    
	}
	else if(command.equals("deleteURLs")) {
	    TreePath t = urlTree.getSelectionPath();
	    if(t != null) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastPathComponent();
		urlCount -= n.getChildCount();
		n.removeAllChildren();		    
	    }
	    treeModel.reload();
	}
    }

    /** 
     * Add an inventory to the URLList.
     * @param inv The <code>DodsURL</code> where the inventory can be found.
     */
    public void addInventory(DodsURL inv) {
	DefaultMutableTreeNode temp = new DefaultMutableTreeNode(inv);
	treeModel.insertNodeInto(temp, top, top.getChildCount());
	TreePath t = new TreePath(treeModel.getPathToRoot(top));

	nodes.put(inv, temp);

	t = t.pathByAddingChild(temp);
	urlTree.setSelectionPath(t);
	urlTree.scrollPathToVisible(t);
	adjustViewport();
    }

    /**
     * Add a <code>TreeSelectionListener</code>
     * @param t The TreeSelectionListener
     */
    public void addTreeSelectionListener(TreeSelectionListener t) {
	urlTree.addTreeSelectionListener(t);
	listeners.addElement(t);
    }

    /**
     * Add a URL to the URLList.  This URL will be added to the group
     * "Single URL's".
     * @param url The URL.
     */
    public void addURL(DodsURL url) {
	if(single == null) {
	    single = new DefaultMutableTreeNode("Single URLs");
	    treeModel.insertNodeInto(single, top, top.getChildCount());
	}
	DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(url);
	TreePath t = new TreePath(treeModel.getPathToRoot(single));
	treeModel.insertNodeInto(new DefaultMutableTreeNode(url),
				 single, single.getChildCount());

	urlCount++;

	t = t.pathByAddingChild(leafNode);
	urlTree.setSelectionPath(t);
	urlTree.scrollPathToVisible(t);
	adjustViewport();
    }

    /**
     * Adds a URL to the URLList to be associated with the inventory
     * inv.  If inv hasn't been added to the list yet, or was not added
     * as an inventory, this method will do nothing.
     * @param url The URL to add.
     * @param inv The inventory to add it to.
     */
    public void addURLToInventory(DodsURL url, DodsURL inv) {
	if(nodes.containsKey(inv)) {
	    DefaultMutableTreeNode invNode = (DefaultMutableTreeNode)nodes.get(inv);
	    TreePath t = new TreePath(treeModel.getPathToRoot(invNode));
	    DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(url);

	    treeModel.insertNodeInto(leafNode, invNode, invNode.getChildCount());
	    urlCount++;

	    t = t.pathByAddingChild(leafNode);
	    urlTree.setSelectionPath(t);
	    urlTree.scrollPathToVisible(t);
	    adjustViewport();
	}
    }

    /**
     * Add multiple URLs to an inventory.  If that inventory doesn't exist,
     * this method won't do anything
     * @param newURLs The URLs to add.
     * @param inv The inventory to add it to.
     */
    public void addURLsToInventory(DodsURL[] newURLs, DodsURL inv) {
	if(nodes.containsKey(inv)) {
	    DefaultMutableTreeNode invNode = (DefaultMutableTreeNode)nodes.get(inv);
	    TreePath parentPath = new TreePath(treeModel.getPathToRoot(invNode));
	    TreePath[] t = new TreePath[newURLs.length];

	    for(int i=0;i<newURLs.length;i++) {
		DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(newURLs[i]);
		treeModel.insertNodeInto(leafNode, invNode, invNode.getChildCount());
		t[i] = parentPath.pathByAddingChild(leafNode);
	    }

	    urlCount += newURLs.length;

	    urlTree.setSelectionPaths(t);
	    urlTree.scrollPathToVisible(t[0]);
	    adjustViewport();
	}
    }

    /**
     * Return all the Data URLs from the URLList
     * @return All the Data URLs from the URLList.
     */
    public DodsURL[] getURLs() {
	// Can't use nodes here, because we want to get the single urls
	Enumeration inventories = top.children();
	DodsURL[] ret = new DodsURL[urlCount];
	int ind = 0;

	// This makes the assumption that there are only two levels to 
	// the tree, based on the assumption that there are never going
	// to be inventories of inventories.  If support for that is ever
	// added, then this will have to change.
	while(inventories.hasMoreElements()) {
	    DefaultMutableTreeNode inv = (DefaultMutableTreeNode)inventories.nextElement();
	    Enumeration children = inv.children();
	    while(children.hasMoreElements()) {
		DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
		ret[ind++] = (DodsURL)child.getUserObject();
	    }
	}

	return ret;
    }

    /**
     * Returns the number of Data URLs in the list.
     * @return the number of Data URLs in the list.
     */
    public int getURLCount() {
	return urlCount;
    }

    /**
     * Returns the number of Data URLs associated with an inventory.
     * @param inv The inventory URL.
     * @return The number of URLs associated with inv.
     */
    public int getURLCount(DodsURL inv) {
	if(nodes.containsKey(inv)) {
	    MutableTreeNode n = (MutableTreeNode)nodes.get(inv);
	    return n.getChildCount();
	}

	return 0;
    }

    /**
     * Returns the first selected URL from the list.
     * @return the first selected URL from the list.
     */
    public Object getSelectedValue() {
	TreePath t = urlTree.getSelectionPath();
	if(t != null) {
	    return ((DefaultMutableTreeNode)t.getLastPathComponent()).getUserObject();
	}
	else
	    return null;
    }

    /**
     * Returns all the selected URLs from the list.
     * @return all the selected URLs from the list.
     */
    public Object[] getSelectedValues() {
	TreePath[] t = urlTree.getSelectionPaths();
	Object[] ret = null;

	if(t != null) {
	    ret = new Object[t.length];

	    for(int i=0;i<t.length;i++) {
		ret[i] = ((DefaultMutableTreeNode)t[i].getLastPathComponent()).getUserObject();
	    }
	}

	return ret;
    }

    /** 
     * Removes all the Data URLs that have been selected from an inventory.
     * @param inv The inventory whose URLs will be removed.
     */
    public void removeAllURLsFromInventory(DodsURL inv) {
	DefaultMutableTreeNode n = (DefaultMutableTreeNode)nodes.get(inv);

	if(n != null) {
	    urlCount -= n.getChildCount();
	    n.removeAllChildren();
	}

	treeModel.reload();
    }

    /**
     * Removes all the selected URLs.
     */
    public void removeSelectedURLs() {
	// FIXME: This is really slow for any significant number of URLs.

	TreePath[] t = urlTree.getSelectionPaths();
	TreeNode parent = null;

	if(t.length > 0) {
	    parent = (TreeNode) t[0].getPathComponent(t[0].getPathCount() - 2);
	}

	// Remove all the TreeSelectionListeners before removing the 
	// nodes so that URLBuilder doesn't try to update the interface
	// for every node that's deleted.
	for(int i=0;i<listeners.size();i++)
	    urlTree.removeTreeSelectionListener((TreeSelectionListener)listeners.elementAt(i));

	for(int i=0;i<t.length-1;i++) {
	    treeModel.removeNodeFromParent((MutableTreeNode)t[i].getLastPathComponent());
	    urlCount--;
	}

	// Before we delete the last node, add the selection listener back
	// so that one event will be generated.
	for(int i=0;i<listeners.size();i++)
	    urlTree.addTreeSelectionListener((TreeSelectionListener)listeners.elementAt(i));
	
	if(t.length > 0) {
	    treeModel.removeNodeFromParent((MutableTreeNode)t[t.length-1].getLastPathComponent());
	    urlCount--;
	}


	if(parent == single && single.getChildCount() == 0) {
	    treeModel.removeNodeFromParent(single);
	    single = null;
	}

    }
}
