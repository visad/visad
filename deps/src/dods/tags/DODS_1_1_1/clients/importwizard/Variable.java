/**
 * Variable.java
 *
 * 1.00 2001/7/19
 *
 */
package dods.clients.importwizard;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class creates a panel with a
 * variable selection box
 *
 * @version     1.00 19 Jul 2001
 * @author      Kashan A. Shaikh
 */
public class Variable extends JPanel{
	final static int VISIBLE_ROW_COUNT = 8;
	private String varName;
	private String[] varContents;
	
	private JList selList;
		
	
	// Constructor
	public Variable(String name, String[] contents) {
		varName = name;
		varContents = contents;
		
		setForeground(Color.orange);
	
		// format the panel
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));    		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		JLabel lbl = new JLabel(varName);
		lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lbl);
		
		// create & add the selection box
		createSelection();
	}

    // Create & add the selection box to the panel
    private void createSelection() {
		selList = new JList(varContents);
		if (varContents.length < VISIBLE_ROW_COUNT) {
			selList.setVisibleRowCount(varContents.length);
		}
		selList.setAlignmentX(Component.CENTER_ALIGNMENT);
		JScrollPane tspane = new JScrollPane(selList);
		add(tspane);
	}
	

    // Get the current selection
    public String[] getSelectedItems() {
		Object[] tobj = selList.getSelectedValues();
		String[] sel = new String[tobj.length];
		for (int i = 0; i < tobj.length; i++) {
			sel[i] = (String) tobj[i];
		}
		return sel;
    }
	
	// Get the variable name
    public String getName() {
		return varName;
    }
}
