package dods.clients.importwizard;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;

class TreePopupListener extends MouseAdapter {
    private JTree tree;
    private JPopupMenu leafPopup;
    private JPopupMenu nodePopup;

    public TreePopupListener(JTree listenTo, JPopupMenu leafMenu, JPopupMenu nodeMenu) {
        tree = listenTo;
	leafPopup = leafMenu;
        nodePopup = nodeMenu;
    }

    public void mousePressed(MouseEvent e) {
	maybeShowPopup(e);
    }
    
    public void mouseReleased(MouseEvent e) {
	maybeShowPopup(e);
    }
    
    private void maybeShowPopup(MouseEvent e) {
	if (e.isPopupTrigger()) {
            TreePath t = tree.getPathForLocation(e.getX(), e.getY());
	    if(!tree.isPathSelected(t))
                tree.setSelectionPath(t);
            if(t != null) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastPathComponent();
                if(n.isLeaf())
                    leafPopup.show(e.getComponent(), e.getX(), e.getY());
                else if(n.getUserObject() instanceof DodsURL)
                    nodePopup.show(e.getComponent(), e.getX(), e.getY());
            }
	}
    }
    
}
