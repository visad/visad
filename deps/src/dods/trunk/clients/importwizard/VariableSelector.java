/*
 * VariableSelector.java
 *
 * Created on December 21, 2001, 8:52 PM
 */

package dods.clients.importwizard;

import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.net.*;
import dods.dap.*;
import gnu.regexp.*;

/**
 * This is the base class for the classes used by CEGenerator to make
 * a form to allow the user to constrain the data.
 *
 * @author Rich Honhart <rhonhart@virginia.edu>
 */
public abstract class VariableSelector extends JPanel 
    implements ActionListener
{
    private boolean enabled;
    private boolean selected;
    private Vector children;
    private String name;
    private JRadioButton rbutton;
    private Vector actionListeners;
    private String actionCommand;

    protected static RE splitVars;   
    protected static RE extractName;
    
    /** Creates a new instance of VariableSelector */
    public VariableSelector() {
        children = new Vector();
	actionListeners = new Vector();
	actionCommand = "";
        rbutton = new JRadioButton();
        name = "Unnamed";
        enabled = true;
        selected = true;
        if(splitVars == null) {
            try {
                splitVars = new RE("[^,]+");
                extractName = new RE("[^.\\[\\]]+");
            }
            catch(REException exp) {
                exp.printStackTrace();
                System.exit(1);
            }        
        }
    }

    /**
     * Add an action listener
     * @param a The ActionListener
     */
    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }

    /** 
     * For an <code>Enumeration</code> of <code>BaseType</code>'s, this 
     * function creates the appropriate Swing objects to select parts of the 
     * variables which it both places in <code>panel</code> and returns as a
     * <code>Vector</code>.
     *
     * @param variables The DODS variables to create Selectors for.
     * @param panel The panel to put these Selectors in
     */
    protected void addVariables(Enumeration variables, JPanel panel) {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        panel.setLayout(gridbag);
        
        int gridy = 0;
        
        while(variables.hasMoreElements()) {
            BaseType var = (BaseType)variables.nextElement();
            JRadioButton button = new JRadioButton();
            
            // Add the radiobutton next to each entry
            c.gridx = 0;
            c.gridy = gridy++;
            c.weightx = 0.0;
            c.anchor = GridBagConstraints.NORTHWEST;
            
            gridbag.setConstraints(button, c);
            panel.add(button);
            
            // Move over to the next spot on the grid
            c.gridx = 1;
            c.weightx = 1.0;
            
            if(var instanceof DArray) {            
                DArraySelector arr = new DArraySelector((DArray)var);
                gridbag.setConstraints(arr, c);
                panel.add(arr);
                ((VariableSelector)arr).connectButton(button);
                this.addChild((VariableSelector)arr);
            }
            
            else if(var instanceof DStructure || var instanceof DSequence) {
                DConstructorSelector str = new DConstructorSelector((DConstructor)var);
                gridbag.setConstraints(str, c);
                panel.add(str);
                ((VariableSelector)str).connectButton(button);
                this.addChild((VariableSelector)str);
            }

            else if(var instanceof DGrid) {
                
		DGridSelector grid = new DGridSelector((DGrid)var);
		gridbag.setConstraints(grid, c);
		panel.add(grid);
		((VariableSelector)grid).connectButton(button);
		this.addChild((VariableSelector)grid);
		
            }
            
            else {
                GenericSelector gen = new GenericSelector((BaseType)var);
                gridbag.setConstraints(gen, c);
                panel.add(gen);
                ((VariableSelector)gen).connectButton(button);
                this.addChild((VariableSelector)gen);
            }
        }    
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if(actionEvent.getActionCommand() == "toggle") {
            this.setSelected(!selected);
        }
    }
    
    /**
     * Add a child to the VariableSelector.  This child is a VariableSelector
     * which should represent a sub-variable.  Not all specializations
     * of VariableSelector will display children, only the ones for whom
     * it is logical to have sub-variables such as DDSSelector and 
     * DConstructorSelector.
     * @param child The child.
     */
    public void addChild(VariableSelector child) {
        children.addElement(child);
    }
   
    /**
     * Update the components on the screen to match a given constraint
     * expression.  This function does not work with any Dods compatible
     * constraint expression, only the subset that can be generated by
     * generateCE().  
     * @param ce The constraint expression.
     */
    public void applyCE(String ce) {             
	// If a specialization doesn't overload this function,
	// it is assumed that it has no components which need to be
	// updated, so this is left empty.
    }

    /**
     * Connect a radio button to the VariableSelector.  When this button
     * is selected, the VariableSelector will be active, when it's 
     * deselected, the VariableSelector will be disabled
     * @param button The button to connect to it.
     */
    public void connectButton(JRadioButton button) {
        rbutton = button;
        button.setSelected(true);
        button.setActionCommand("toggle");
        button.addActionListener(this);
    }
    
    /**
     * Deselect this variable selector and all it's children.
     */
    public void deselectAll() {
        this.setSelected(false);
        for(int i=0;i<children.size();i++) {
            ((VariableSelector)children.elementAt(i)).deselectAll();
        }
    }

    /**
     * Select this variable and all it's children
     */
    public void selectAll() {
        this.setSelected(true);
        for(int i=0;i<children.size();i++) {
            ((VariableSelector)children.elementAt(i)).selectAll();
        }
    }

    /**
     * Reset the everything in this <code>VariableSelector</code> and
     * all it's children.
     */
    public void reset() {
	this.setSelected(true);
        for(int i=0;i<children.size();i++) {
            ((VariableSelector)children.elementAt(i)).reset();
        }
    }

    /**
     * Send an action event to all the classes that have been added as
     * listeners.
     */
    protected void fireActionEvent() {
	//System.out.println("Sending an ActionEvent");
	ActionEvent e = new ActionEvent(this, 0, actionCommand);
	for(int i=0;i<actionListeners.size();i++) {
	    ((ActionListener)actionListeners.elementAt(i)).actionPerformed(e);
	}
    }

    /**
     * Return the radiobutton connected to the VariableSelector
     */
    public JRadioButton getButton() {
        return rbutton;
    }
    
    /**
     * Get child <code>name</code> if it exists.  If not, return null.
     * @param name The name of the child
     * @return The child (a <code>VariableSelector</code>) if it exists.
     */
    public VariableSelector getChild(String name) {
        //This is by no means efficient, but it works
        for(int i=0;i<children.size();i++) {
            if(((VariableSelector)children.elementAt(i)).getName().equals(name)) {
                return (VariableSelector)children.elementAt(i);
            }
        }
        
        return null;
    }
    
    /**
     * @return All the children as an <code>Enumeration</code>.
     */
    public Enumeration getChildren() {
        return children.elements();
    }

    /**
     * @return The name of the VariableSelector.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Generate a DODS constraint expression for the variable.
     * @param prefix Anything that needs to come before the constraint
     *               expression.  This is usually a Structure, Sequence,
     *               or other container class.  If a '.' is needed between
     *               the prefix and the part of the CE this class generates,
     *               it must be included at the end of the prefix.
     * @return a DODS constraint expression that will return the variable
     *         represented by this VariableSelector.
     */
    public String generateCE(String prefix) {
        return prefix + name;
    }
    
    /**
     * @return Whether or not this VariableSelector is enabled.
     *         (whether or not the user can modify it).
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * @return Whether or not this VariableSelector is selected.
     *         (whether or not it should be included in a generated CE)
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Set the action command.
     * @param command The action command.
     */
    public void setActionCommand(String command) {
	actionCommand = command;
    }

    /**
     * Enable or disable the VariableSelector and any selected
     * children.
     * @param enable Enable (true) or Disable (false).
     */
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);

        for(int i=0;i<children.size();i++) {
            ((VariableSelector)children.elementAt(i)).getButton().setEnabled(enable);
            if(enable) {
                if( ((VariableSelector)children.elementAt(i)).isSelected() ) {
                    ((VariableSelector)children.elementAt(i)).setEnabled(true);
                }
            }
            else {
                ((VariableSelector)children.elementAt(i)).setEnabled(false);
            }
        }
        
        enabled = enable;
    }   
    
    /**
     * Set the name of the VariableSelector (this is usually set to
     * the name of the <code>BaseType</code> it's being used to
     * constrain.
     * @param newName The name.
     */
    public void setName(String newName) {
        try {
	    name = URLDecoder.decode(newName);
	}
	//catch(NoClassDefFoundError err) {
	catch(Exception e) {   
	    name = newName;
	}
    }

    /**
     * Select or deselect the VariableSelector.  (Select the associated 
     * button, and enable or disable the selector itself).
     * @param select Select(true) or Deselect(false).
     */
    public void setSelected(boolean select) {
        selected = select;
        rbutton.setSelected(select);
        
        this.setEnabled(select);
    }
}


