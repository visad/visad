/*
 * StructSelector.java
 *
 * Created on December 21, 2001, 8:59 PM
 */

package dods.clients.importwizard;

import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import dods.dap.*;
import gnu.regexp.*;

/**
 *
 * @author  Honhart
 */

public class DConstructorSelector extends VariableSelector
{

    /** Creates a new instance of StructSelector */
    public DConstructorSelector(DConstructor str) {
        JPanel topPanel = new JPanel();
        JPanel varPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        setName(str.getName());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(new JLabel(str.getTypeName() + " {"));
        topPanel.add(Box.createHorizontalGlue());

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(new JLabel("} " + getName() + ";"));
        bottomPanel.add(Box.createHorizontalGlue());

        addVariables(str.getVariables(), varPanel);

        add(topPanel);
        add(varPanel);
        add(bottomPanel);
    }

    public void applyCE(String ce) {
        REMatchEnumeration matches;
        VariableSelector sel;
        Hashtable vars = new Hashtable();
        
        if(ce.startsWith(getName())) {
            ce = ce.substring(getName().length() + 1);
        }
        
        matches = splitVars.getMatchEnumeration(ce);
        
        while(matches.hasMoreMatches()) {
            REMatch temp = matches.nextMatch();
            REMatch varName = extractName.getMatch(temp.toString());

            if( (sel = getChild(varName.toString())) != null) {
                sel.applyCE(temp.toString());
                sel.setSelected(true);
                vars.put(varName.toString(), new Boolean(true));
            }
        }
    }

    /**
     * Generate a DODS compatible constraint expression.
     * @param prefix The location of this particular variable in the DDS 
     *               heirarchy.  Note that prefix should end in a dot if 
     *               one is needed.
     * @return The constraint expression.
     */
    public String generateCE(String prefix) {
        String ce = "";
        Enumeration children = getChildren();
        VariableSelector temp;
        while(children.hasMoreElements()) {
            temp = (VariableSelector)children.nextElement();
            if(temp.isEnabled()) {
                if(ce != "") 
                    ce = ce + ",";
                ce = ce + temp.generateCE(prefix + getName() + ".");
            }
        }

        return ce;
    }
}   
