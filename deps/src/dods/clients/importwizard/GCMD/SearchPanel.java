package dods.clients.importwizard.GCMD;

import dods.clients.importwizard.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import gnu.regexp.*;

/** 
 * This class displays a search panel and has functions to turn the 
 * values entered in the search panel into a GCMD search string.
 *
 * @author rhonhart
 */
public class SearchPanel extends JPanel 
    implements ActionListener
{
    private JCheckBox enableBox;
    private JComboBox category;
    private JComboBox matchType;
    private JTextField searchField;
    private Vector actionListeners;
    private boolean enabled;

    /**
     * Create a new <code>SearchPanel</code>.
     */
    public SearchPanel() {
	actionListeners = new Vector();
	enabled = false;
	initGUI();
    }

    /**
     * Initialize the GUI components.
     */
    public void initGUI() {
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2,2,2,2)));

	enableBox = new JCheckBox();
	String[] choices = { "ID", "Category", "Keywords", "Topic", "Term", "Title", "Variables" };
	category = new JComboBox(choices);
	String[] matchTypes = { "Contains", "Equals" };
	matchType = new JComboBox(matchTypes);

	searchField = new JTextField();

	category.setSelectedIndex(2);
	
	enableBox.addActionListener(this);
	searchField.addActionListener(this);

	add(enableBox);
	add(category);
	add(matchType);
	add(searchField);
    }

    /**
     * Returns whether or not the panel is enabled.
     * @return whether or not the panel is enabled.
     */
    public boolean isEnabled() {
	return enabled;
    }

    /**
     * Enable or disable the panel based on <code>enable</code>.
     * @param enable whether the panel is on or off.
     */
    public void setEnabled(boolean enable) {
	enabled = enable;
	enableBox.setSelected(enabled);
	category.setEnabled(enabled);
	matchType.setEnabled(enabled);
	searchField.setEnabled(enabled);
	super.setEnabled(enabled);
    }

    /**
     * Toggle whether the panel is enabled or not
     */
    public void toggleEnabled() {
	enabled = !enabled;
	category.setEnabled(enabled);
	matchType.setEnabled(enabled);
	searchField.setEnabled(enabled);
	super.setEnabled(enabled);
     }

    /**
     * Add an action listener to the panel.  It will receive action events
     * when enter is pressed inside the text field, and when the checkbox 
     * is clicked.
     * @param a The <code>ActionListener</code>.
     */
    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }

    /** 
     * Set the action commands.
     * @param enabledCommand The command used when the check box is clicked.
     * @param textCommand The command used when the user hits enter inside
     *                    the checkbox.
     */
    public void setActionCommands(String enabledCommand, String textCommand) {
	enableBox.setActionCommand(enabledCommand);
	searchField.setActionCommand(textCommand);
    }

    /**
     * Catch events from the GUI components and pass them on to the 
     * action listeners.
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
	ActionEvent evt = new ActionEvent(this, 0, e.getActionCommand());
	for(int i=0;i<actionListeners.size();i++) {
	    ((ActionListener)actionListeners.elementAt(i)).actionPerformed(evt);
	}
    }

    /**
     * Returns a GCMD search string based on the input in the text field, 
     * the search category, and the match type.
     * @return The GCMD search string for the panel.
     */
    public String getSearchString() {
	String searchString = "";
	String value = searchField.getText().toUpperCase();
	RE spaces;
	String categoryName = (String)category.getSelectedItem();
	String wildcard = ( ((String)matchType.getSelectedItem()).equals("Contains") ? "*" : "");

	try { 
	    spaces = new RE(" ");
	    value = spaces.substituteAll(value, "%20");
	} catch(Exception e) {}

	if(categoryName.equals("Keywords")) {
	    searchString = "[Keyword='" + wildcard + value
		+ wildcard + "']";
	}
	else if(categoryName.equals("Variables")) {
	    searchString = "[Parameters:Variable='" + wildcard 
		+ value + wildcard + "']";
	}
	else if(categoryName.equals("ID")) {
	    searchString = "[Entry_ID='" + wildcard + value 
		+ wildcard + "']";
	}
	else if(categoryName.equals("Title")) {
	    searchString = "[Entry_Title='" + wildcard + value 
		+ wildcard + "']";
	}
	else if(categoryName.equals("Category")) {
	    searchString = "[Parameters:Category='" + wildcard 
		+ value + wildcard + "']";
	}
	else if(categoryName.equals("Topic")) {
	    searchString = "[Parameters:Topic='" + wildcard 
		+ value + wildcard + "']";
	}
	else if(categoryName.equals("Term")) {
	    searchString = "[Parameters:Term='" + wildcard 
		+ value + wildcard + "']";
	}
	return searchString;
    }
}
