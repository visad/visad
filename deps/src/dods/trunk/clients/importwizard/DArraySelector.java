/*
 * DArraySelector.java
 *
 * Created on December 21, 2001, 8:53 PM
 */

package dods.clients.importwizard;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import dods.dap.*;
import gnu.regexp.*;

/**
 *
 * @author Rich Honhart
 */
public class DArraySelector extends VariableSelector 
    implements ActionListener
{

    private Vector fields;
    private Vector defaults;
    protected static RE getDimensions;

    /** Creates a new instance of DArraySelector */
    public DArraySelector(DArray arr) {
        Enumeration dimensions = arr.getDimensions();
        JTextField field1;
        JTextField field2;
        fields = new Vector();
        defaults = new Vector();

        if(getDimensions == null) {
            try {
                getDimensions = new RE("\\[(\\d+):(\\d+)\\]");
            }
            catch(REException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        setName(arr.getName());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(new JLabel(getName() + ": "));
        
        while(dimensions.hasMoreElements()) {
            DArrayDimension temp = (DArrayDimension)dimensions.nextElement();

            field1 = new JTextField(String.valueOf(temp.getStart()));
            field1.setPreferredSize(new Dimension(37, 0));
	    field1.addActionListener(this);
	    field1.setActionCommand("textField");
	    fields.addElement(field1);
	    defaults.addElement(field1.getText());

	    field2 = new JTextField(String.valueOf(temp.getStop()));
	    field2.setPreferredSize(new Dimension(37, 0));
	    field2.addActionListener(this);
	    field2.setActionCommand("textField");
	    fields.addElement(field2);
	    defaults.addElement(field2.getText());

	    BoundsVerifier bound = new BoundsVerifier(temp.getStart(), temp.getStop());
	    field1.addFocusListener(bound);
	    field2.addFocusListener(bound);

            if(temp.getName() != null) {
		try {
		    add(new JLabel(URLDecoder.decode(temp.getName()) + "=["));
		}
		//catch(NoClassDefFoundError e) {
		catch(Exception e) {  
		    add(new JLabel(temp.getName() + "=["));
		}
	    }
	    else {
		add(new JLabel("["));
	    }

            add(field1);
            add(new JLabel(":"));
            add(field2);
            add(new JLabel("]"));

        }

        add(Box.createHorizontalGlue());
    }

    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);

	if(e.getActionCommand().equals("textField")) {
	    javax.swing.FocusManager focuser = javax.swing.FocusManager.getCurrentManager();
	    focuser.focusNextComponent((Component)e.getSource());	    
	}
    }

    public void addField(JTextField field) {
        fields.addElement(field);
    }
    
    public void applyCE(String ce) {
        if(ce.startsWith(getName())) {
            REMatch[] matches = getDimensions.getAllMatches(ce);
            if(matches.length == fields.size() / 2) {
                for(int i=0;i<fields.size();i+=2) {
                    ((JTextField)fields.elementAt(i)).setText(matches[i/2].toString(1));
                    ((JTextField)fields.elementAt(i+1)).setText(matches[i/2].toString(2));
                }
            }
        }
    }

    public String generateCE(String prefix) {
        String ce = prefix + getName();
        for(int i=0;i<fields.size();i+=2) {
            ce = ce + "[" + ((JTextField)fields.elementAt(i)).getText() 
                    + ":" + ((JTextField)fields.elementAt(i+1)).getText() + "]";
        }
        return ce;
    }

    public void reset() {
	setSelected(true);
	for(int i=0;i<fields.size();i++) {
	    ((JTextField)fields.elementAt(i)).setText((String)defaults.elementAt(i));
	}
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

	for(int i=0;i<fields.size();i++) {
            ((JTextField)fields.elementAt(i)).setEnabled(enabled);
        }
    }

    public class BoundsVerifier extends FocusAdapter {
        
        private int upper;
        private int lower;
        
        public BoundsVerifier(int lowerBound, int upperBound) {
            lower = lowerBound;
            upper = upperBound;
        }
        
        public void focusLost(FocusEvent evt) {
            JTextField tf = (JTextField)evt.getComponent();
            int value;

	    try {
		value = new Integer(tf.getText()).intValue();
	    }
	    catch(java.lang.NumberFormatException exc) {
		// If the user enters something that can't be parsed as a
		// number, set value to -1 so the field will be reset to
		// the lower bound.
		value = -1;
	    }

	    boolean retVal = false;

            if(value > upper) {
		tf.setText(String.valueOf(upper));
            }
            else if(value < lower) {
                tf.setText(String.valueOf(lower));
            }

	    fireActionEvent();
        }
        
    } 
}
