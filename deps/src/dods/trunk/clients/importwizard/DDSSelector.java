/*
 * DDSSelector.java
 *
 * Created on December 21, 2001, 11:37 PM
 */

package dods.clients.importwizard;

import dods.dap.*;
import gnu.regexp.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author  Honhart
 */
public class DDSSelector extends VariableSelector {

    private JPanel mainPanel;
    
    /** Creates a new instance of DDSSelector */
    public DDSSelector(DDS dds) {
        mainPanel = new JPanel();
        addVariables(dds.getVariables(), mainPanel);
        add(mainPanel);
    }
    
    public String generateCE(String prefix) {
        Enumeration children = getChildren();
        VariableSelector temp;
        String ce = "";
        
        while(children.hasMoreElements()) {
            temp = (VariableSelector)children.nextElement();
            if(temp.isEnabled()) {
                if(ce != "") {
                    ce = ce + ",";
                }
                ce = ce + temp.generateCE("");
            }
        }
        
        return ce;
    }
    
    public void applyCE(String ce) {

        REMatchEnumeration matches;
        VariableSelector sel;
        Hashtable vars = new Hashtable();
        
        //System.out.println(ce);
        
        matches = splitVars.getMatchEnumeration(ce);
        
        while(matches.hasMoreMatches()) {
            REMatch temp = matches.nextMatch();
            REMatch varName = extractName.getMatch(temp.toString());
            //System.out.println(varName);
            //System.out.println(varName.getEndIndex());
            if( (sel = getChild(varName.toString())) != null) {
                sel.applyCE(temp.toString());
                sel.setSelected(true);
                vars.put(varName.toString(), new Boolean(true));
            }
        }
        //UTMGrid.Data%20Fields.Pollution[0:4][0:5][0:6]
        /*Enumeration children = getChildren();
        while(children.hasMoreElements()) {
            VariableSelector temp = (VariableSelector)children.nextElement();
            if(!vars.containsKey(temp.getName())) {
                temp.setSelected(false);
                //System.out.println("Vars doesn't contain " + temp.getName());
            }
            else {
                //System.out.println("Vars contains " + temp.getName());
                temp.setSelected(true);
            }
        }  
         */      
    }

    public void deselectAll() {
        Enumeration children = getChildren();
        while(children.hasMoreElements()) {
            ((VariableSelector)children.nextElement()).deselectAll();
        }
    }
}
