/**
 * DListSearchGroup.java
 *
 * 1.00 2001/8/3
 *
 */

package dods.clients.importwizard.DatasetList;

import dods.clients.importwizard.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 * This class creates a group of search boxes
 *
 * @version     1.00 3 Aug 2001
 * @author      Kashan A. Shaikh
 */
public class DListSearchGroup extends JPanel implements ActionListener {
    static public String STATE_ENABLED="enabled";
    static public String STATE_DISABLED="disabled";

    static public String LOGIC_AND = "AND";
    static public String LOGIC_OR = "OR";

    private Vector searchPanels;
    private JComboBox logicTypeBox;
    private String initialState;


    // Default Constructor
    public DListSearchGroup() {
        initialState = STATE_ENABLED;
        initGUI();
    }

    // Constructor
    public DListSearchGroup(String state) {
        initialState = state;
        initGUI();
    }


    //
    // initialize the GUI components
    //
    public void initGUI() {
        searchPanels = new Vector();

        // create logic type selector
        JLabel logicTypeLabel = new JLabel("Logic: ");
        String[] logicTypes = { LOGIC_AND, LOGIC_OR };
        logicTypeBox = new JComboBox(logicTypes);
        logicTypeBox.setPreferredSize(new Dimension(60,25));
        logicTypeBox.setMaximumSize(new Dimension(60,25));
        JPanel logicPanel = new JPanel();
        logicPanel.setLayout(new BoxLayout(logicPanel,BoxLayout.X_AXIS));
        logicPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logicPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        logicPanel.add(logicTypeLabel);
        logicPanel.add(logicTypeBox);
        add(logicPanel);

        // create first element
        InputPanel g1 = new InputPanel();
        g1.addActionListener(this);
        g1.setActionCommands("togglePanel", "searchText");
        g1.setAlignmentX(Component.CENTER_ALIGNMENT);
        g1.setAlignmentY(Component.TOP_ALIGNMENT);
        g1.setPreferredSize(new Dimension(200,30));
        g1.setMaximumSize(new Dimension(32768,30));
        if (initialState == STATE_ENABLED) {
            g1.setEnabled(true);
        } else {
            g1.setEnabled(false);
        }
        searchPanels.addElement(g1);
        add(g1);

        if (initialState == STATE_ENABLED) {
            // create second element
            InputPanel g2 = new InputPanel();
            g2.addActionListener(this);
            g2.setActionCommands("togglePanel", "searchText");
            g2.setAlignmentX(Component.CENTER_ALIGNMENT);
            g2.setAlignmentY(Component.TOP_ALIGNMENT);
            g2.setPreferredSize(new Dimension(200,30));
            g2.setMaximumSize(new Dimension(32768,30));
            g2.setEnabled(false);
            searchPanels.addElement(g2);
            add(g2);
        }

        // set layout
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEmptyBorder(2,2,2,2)));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.TOP_ALIGNMENT);
    }


    //
    // Retrieve the search keywords
    //
    public String[] getSearchKeywords() {
        String[] keywords = new String[searchPanels.size()-1];
        for (int i=0; i < searchPanels.size()-1; i++) {
            keywords[i] = ((InputPanel) searchPanels.elementAt(i)).getSearchString();
        }
        return keywords;
    }


    //
    // Retrieve the logic type
    //
    public String getLogicType() {
        return logicTypeBox.getSelectedItem().toString();
    }


    //
    // Clear all the keywords, reset view
    //
    public void clearSearchKeywords() {
        int pos = 0;
        int count = searchPanels.size();
        for (int i=0; i < count; i++) {
            ((InputPanel) searchPanels.elementAt(pos)).clearSearchString();
            if (initialState == STATE_DISABLED) {
                ((InputPanel) searchPanels.elementAt(pos)).setEnabled(false);
                if (i > 0) {
                    remove((InputPanel) searchPanels.elementAt(pos));
                    searchPanels.removeElementAt(pos);
                } else {
                    pos++;
                }
            } else {
                if (i > 1) {
                    remove((InputPanel) searchPanels.elementAt(pos));
                    searchPanels.removeElementAt(pos);
                } else if (i > 0) {
                    ((InputPanel) searchPanels.elementAt(pos)).setEnabled(false);
                    pos++;
                } else {
                    pos++;
                }
            }
        }
        getRootPane().getContentPane().repaint();
    }


    //
    // Implementation of ActionListener interface.
    //
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("togglePanel")) {
            if(searchPanels.lastElement().equals(event.getSource())
                    && ((InputPanel)event.getSource()).isEnabled() == false)
            {
                InputPanel search = new InputPanel();
                search.setEnabled(false);
                search.setPreferredSize(new Dimension(200,30));
                search.setMaximumSize(new Dimension(32768, 30));
                search.addActionListener(this);
                search.setActionCommands("togglePanel", "searchText");
                search.setAlignmentX(Component.CENTER_ALIGNMENT);
                search.setAlignmentY(Component.TOP_ALIGNMENT);
                searchPanels.addElement(search);
                add(search);
                getRootPane().getContentPane().validate();
            }
            ((InputPanel)event.getSource()).toggleEnabled();
        }
    }



    //
    // InputPanel class
    //
    public class InputPanel extends JPanel implements ActionListener {
        private JCheckBox enableBox;
        private JTextField searchField;
        private Vector actionListeners;
        private boolean enabled;

        //
        // constructor
        //
        public InputPanel() {
            super();
            actionListeners = new Vector();
            enabled = false;
            initGUI();
        }

        public void initGUI() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2,2,2,2)));

            enableBox = new JCheckBox();
            searchField = new JTextField();

            enableBox.addActionListener(this);
            searchField.addActionListener(this);

            add(enableBox);
            add(searchField);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enable) {
            enabled = enable;
            enableBox.setSelected(enabled);
            searchField.setEnabled(enabled);
            super.setEnabled(enabled);
        }

        public void toggleEnabled() {
            enabled = !enabled;
            searchField.setEnabled(enabled);
            super.setEnabled(enabled);
        }

        public void addActionListener(ActionListener a) {
            actionListeners.addElement(a);
        }

        public void setActionCommands(String enabledCommand, String textCommand) {
            enableBox.setActionCommand(enabledCommand);
            searchField.setActionCommand(textCommand);
        }

        public void actionPerformed(ActionEvent e) {
            ActionEvent evt = new ActionEvent(this, 0, e.getActionCommand());
            for(int i=0;i<actionListeners.size();i++) {
                ((ActionListener)actionListeners.elementAt(i)).actionPerformed(evt);
            }
        }

        public String getSearchString() {
            return searchField.getText();
        }

        public void clearSearchString() {
            searchField.setText("");
        }

    }	// InputPanel
}
