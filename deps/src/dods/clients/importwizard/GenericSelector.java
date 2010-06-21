/*
 * GenericSelector.java
 *
 * Created on December 23, 2001, 2:55 PM
 */

package dods.clients.importwizard;

import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import dods.dap.*;

/**
 *
 * @author  Honhart
 */
public class GenericSelector extends VariableSelector {

    /** Creates a new instance of GenericSelector */
    public GenericSelector(BaseType var) {
        setName(var.getName());
        add(new JLabel(var.getName()));
    }

}
