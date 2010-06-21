package dods.clients.importwizard;

import javax.swing.*;
import java.awt.event.*;

class ListPopupListener extends MouseAdapter {
    private JList list;
    private JPopupMenu popup;

    public ListPopupListener(JList listenTo, JPopupMenu menu) {
	list = listenTo;
	popup = menu;
    }

    public void mousePressed(MouseEvent e) {
	maybeShowPopup(e);
    }
    
    public void mouseReleased(MouseEvent e) {
	maybeShowPopup(e);
    }
    
    private void maybeShowPopup(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    int index = list.locationToIndex(e.getPoint());
	    if(!list.isSelectedIndex(index)) {
		list.clearSelection();
		list.setSelectedIndex(index);
	    }
	    popup.show(e.getComponent(), e.getX(), e.getY());
	}
    }
}

