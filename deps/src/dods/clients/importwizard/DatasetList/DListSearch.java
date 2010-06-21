/**
 * DListSearch.java
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
 * This class creates a search interface
 *
 * @version     1.00 3 Aug 2001
 * @author      Kashan A. Shaikh
 */
public class DListSearch extends JPanel implements ActionListener {
    public static String EVENT_SEARCH="searchButton";
    public static String EVENT_SHOW_ALL="showAllButton";
    public static String EVENT_CLEAR="clearButton";
    static public String LOGIC_AND = "AND";
    static public String LOGIC_OR = "OR";

    // A group consists of two DListSearchGroup blocks and a logic block
    private int MAX_NUM_GROUPS;

    private JScrollPane searchScroller;
    private JPanel searchPanel;
    private JPanel buttonPanel;
    private Vector searchPanels;
    private JComboBox logicTypeBox;
    private Vector actionListeners;


    //
    // Default Constructor
    //
    public DListSearch() {
        MAX_NUM_GROUPS = 1;
        initGUI();
    }

    //
    // Constructor
    //
    public DListSearch(int numGroups) {
        MAX_NUM_GROUPS = numGroups;
        initGUI();
    }


    //
    // initialize the GUI components
    //
    public void initGUI() {
        actionListeners = new Vector();
        searchPanels = new Vector();
        searchPanel = new JPanel();
        buttonPanel = new JPanel();
        searchScroller = new JScrollPane(searchPanel);
        searchScroller.setPreferredSize(new Dimension(600,100));

        // create first group
        DListSearchGroup g1 = new DListSearchGroup();
        searchPanels.addElement(g1);

        // create second group
        DListSearchGroup g2 = new DListSearchGroup(DListSearchGroup.STATE_DISABLED);
        searchPanels.addElement(g2);

        // create logic type selector
        JLabel logicTypeLabel = new JLabel("Logic: ");
        logicTypeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logicTypeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        String[] logicTypes = { LOGIC_AND, LOGIC_OR };
        logicTypeBox = new JComboBox(logicTypes);
        logicTypeBox.setSelectedIndex(1);
        logicTypeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        logicTypeBox.setAlignmentY(Component.TOP_ALIGNMENT);
        logicTypeBox.setPreferredSize(new Dimension(60,25));
        logicTypeBox.setMaximumSize(new Dimension(60,25));
        JPanel logicPanel = new JPanel();
        logicPanel.setLayout(new BoxLayout(logicPanel,BoxLayout.Y_AXIS));
        logicPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        logicPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logicPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        logicPanel.add(logicTypeLabel);
        logicPanel.add(logicTypeBox);

        // set the searchPanel layout
        searchPanel.setLayout(new BoxLayout(searchPanel,BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // add the groups
        searchPanel.add(g1);
        searchPanel.add(logicPanel);
        searchPanel.add(g2);


        // Setup the button panel
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2,2,2,2)));
        buttonPanel.add(Box.createVerticalGlue());

        JPanel upper = new JPanel();
        upper.setBorder(BorderFactory.createEtchedBorder());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        clearButton.setActionCommand(EVENT_CLEAR);
        clearButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        upper.add(clearButton);
        buttonPanel.add(upper);

        JPanel lower = new JPanel();
        lower.setBorder(BorderFactory.createEtchedBorder());
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(this);
        searchButton.setActionCommand(EVENT_SEARCH);
        searchButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        lower.add(searchButton);
        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(this);
        showAllButton.setActionCommand(EVENT_SHOW_ALL);
        showAllButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        lower.add(showAllButton);
        buttonPanel.add(lower);
        buttonPanel.add(Box.createVerticalGlue());


        // add everything
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2,2,2,2)));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.TOP_ALIGNMENT);
        add(searchScroller);
        add(buttonPanel);
    }


    //
    // Retrieve the Search Keywords
    //
    public Vector getSearchKeywords() {
        Vector keywords = new Vector();
        for (int i=0; i < searchPanels.size(); i++) {
            keywords.addElement(((DListSearchGroup) searchPanels.elementAt(i)).getSearchKeywords());
        }
        return keywords;
    }

    //
    // Retrieve the Logic Types for each group
    //
    public Vector getGroupLogicTypes() {
        Vector types = new Vector();
        for (int i=0; i < searchPanels.size(); i++) {
            types.addElement(((DListSearchGroup) searchPanels.elementAt(i)).getLogicType());
        }
        return types;
    }

    // Retrieve the Logic Types between groups
    public Vector getGlobalLogicTypes() {
        Vector types = new Vector();
        //for (int i=0; i < MAX_NUM_GROUPS; i++) {
        types.addElement(logicTypeBox.getSelectedItem());
        //}
        return types;
    }

    //
    // Clears all of the keyword entries
    //
    public void clearSearchKeywords() {
        for (int i=0; i < searchPanels.size(); i++) {
            ((DListSearchGroup) searchPanels.elementAt(i)).clearSearchKeywords();
        }
    }


    //
    // Sets the maximum number of groups
    //
    public void setMaxNumGroups(int num) {
        MAX_NUM_GROUPS = num;
    }


    //
    // Add a listener
    //
    public void addActionListener(ActionListener a) {
        actionListeners.addElement(a);
    }


    //
    // Implementation of ActionListener interface.
    //
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(EVENT_CLEAR)) {
            clearSearchKeywords();
        }

        // make sure everyone else knows
        ActionEvent evt = new ActionEvent(this, 0, event.getActionCommand());
        for(int i=0;i<actionListeners.size();i++) {
            ((ActionListener)actionListeners.elementAt(i)).actionPerformed(evt);
        }
    }

}
