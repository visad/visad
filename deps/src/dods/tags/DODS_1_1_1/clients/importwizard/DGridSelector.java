/*
 * DGridSelector.java
 *
 * Created on December 27, 2001, 9:29 PM
 */

package dods.clients.importwizard;

import dods.dap.*;
import gnu.regexp.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class allows the user to constrain a variable of type DGrid.
 * It's the same a DConstructorSelector except that it keeps the 
 * ranges on the map vectors in sync with the ranges on the main array.
 *
 * @author Rich Honhart
 */
public class DGridSelector extends DConstructorSelector {

    private DArraySelector gridArr;
    private Vector mapVars;
    private RE dims;

    /** Creates a new instance of GridSelector */
    public DGridSelector(DGrid grid) {
	super((DConstructor)grid);

	try {
	    dims = new RE("\\[\\d+:\\d+\\]");
	}
	catch(gnu.regexp.REException exc) {
	    exc.printStackTrace();
	    System.exit(1);
	}

	mapVars = new Vector();
	Enumeration children = getChildren();

	// Store the selectors for the main array and the map vars
	// locally so we can keep them in sync.
	if(children.hasMoreElements()) {
	    gridArr = (DArraySelector)children.nextElement();
	    gridArr.addActionListener(this);
	    gridArr.setActionCommand("updatedce");
	}
	else {
	    System.err.println("Internal Error: Can't find the array "
			       + "and map vectors for grid " + grid.getName());
	}
	while(children.hasMoreElements()) {
	    DArraySelector temp = (DArraySelector)children.nextElement();
	    temp.addActionListener(this);
	    temp.setActionCommand("updatedmap");
	    mapVars.addElement(temp);
	}
    }

    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);

	if(e.getActionCommand() == "updatedce") {
	    String ce = gridArr.generateCE("");
	    REMatchEnumeration matches = dims.getMatchEnumeration(ce);
	    
	    for(int i=0;i<mapVars.size() && matches.hasMoreElements();i++) {
		DArraySelector mapVar = (DArraySelector)mapVars.elementAt(i);
		REMatch dim = matches.nextMatch();
		mapVar.applyCE(mapVar.getName() + dim.toString());
	    }
	    
	}

	if(e.getActionCommand() == "updatedmap") {
	    String ce = getName();

	    for(int i=0;i<mapVars.size();i++) {
		REMatch dim = dims.getMatch(((DArraySelector)mapVars.elementAt(i)).generateCE(""));
		ce = ce + dim.toString();
	    }

	    gridArr.applyCE(ce);
	}
    }

}


