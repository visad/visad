package dods.clients.importwizard.ECHO;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import gnu.regexp.*;

/** 
 * This class displays a panel for result valids display 
 *
 * @author Zhifang(Sheila Jiang)
 */
public class ResultValuesPanel extends JPanel 
    implements ActionListener
{
    private final int SIZE = 7;

    private JTextField[] valueFields;
    private Vector actionListeners;
    private boolean[] isShown;

    /**
     * Create a new <code>ResultValidsPane</code>
     */
    public ResultValuesPanel() {
	actionListeners = new Vector();
	valueFields = new JTextField[SIZE];
	for (int i=0; i<SIZE; i++){
	    valueFields[i] = new JTextField(20);    
	    valueFields[i].addActionListener(this);
        }

	//set all check boxes visible
	isShown = new boolean[SIZE];
	for (int i=0; i<SIZE; i++){
	    isShown[i] = true;
        }

	initGUI();
	
	//set all invisible, only selected will be shown later
	for (int i=0; i<SIZE; i++){
	    isShown[i] = false;
        }
    }

    /**
     * Initialize the GUI components.
     */
    public void initGUI() {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(20,20,10,20)));
        
	add(new JLabel("Query Result"));
	add(Box.createRigidArea(new Dimension(0,10)));
	for (int i=0; i<SIZE; i++){
 	    if(isShown[i]) {
		add(valueFields[i]);
		add(Box.createVerticalGlue());
	    }
	    else 
	    {
		remove(valueFields[i]);
		remove(Box.createVerticalGlue());
	    }
        }
    }
    
        
    /**
     * Add an action listener to each checkbox.  It will receive action events
     * when the checkbox is clicked.
     * @param a The <code>ActionListener</code>.
     */
    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }

    /** 
     * Set the action commands.
     * @param enabledCommand The command used when the check box is clicked.
     */
    public void setActionCommands(String[] command) {
	for (int i=0; i<SIZE; i++){
	   valueFields[i].setActionCommand(command[i]);
        }
    }

    /**
     * Catch events from the GUI components and pass them on to the 
     * action listeners.
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
	/*
	ActionEvent evt = new ActionEvent(this, 0, e.getActionCommand());
	for(int i=0;i<actionListeners.size();i++) {
	((ActionListener)actionListeners.elementAt(i)).actionPerformed(evt);
	}*/
	
	Object o = e.getSource();
	/*	for (int i=0; i<SIZE; i++){
	    if (o == enableBox[i]) isShown[i] = enableBox[i].isSelected();
        }*/
    }

    /**
     * Returns a Vector of strings  of chosen result valids
     * @return The string Vector for the result valids being chosen 
     *
    public Vector getResultValids() {
	Vector valids = new Vector();
	for(int i=0; i<SIZE; i++){
	     if(enableBox[i].isSelected()) valids.addElement(enableBox[i].getText());
        }    
	
	return valids;
    }*/
}


