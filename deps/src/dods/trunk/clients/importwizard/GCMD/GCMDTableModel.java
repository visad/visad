package dods.clients.importwizard.GCMD;

import dods.clients.importwizard.*;
import javax.swing.*;
import javax.swing.table.*;
import java.lang.*;

public class GCMDTableModel extends AbstractTableModel {
    final String[] columnNames = { "Download", "Entry ID" };
    
    private Object[][] data;
    
    public GCMDTableModel(Object[] selected, Object[] id) {
	data = new Object[selected.length][2];
	for(int i=0;i<selected.length;i++) {
	    data[i][0] = selected[i];
	    data[i][1] = id[i];
	}
    }
    
    public int getColumnCount() {
	return columnNames.length;
    }
    
    public int getRowCount() {
	return data.length;
    }
    
    public String getColumnName(int col) {
	return columnNames[col];
    }
    
    public Object getValueAt(int row, int col) {
	return data[row][col];
    }
    
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }
    
    public boolean isCellEditable(int row, int col) {
	//Note that the data/cell address is constant,
	//no matter where the cell appears onscreen.
	if (col == 0) { 
	    return true;
	} else {
	    return false;
	}
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
	
	if (data[0][col] instanceof Integer                        
	    && !(value instanceof Integer)) {                  
	    //With JFC/Swing 1.1 and JDK 1.2, we need to create    
	    //an Integer from the value; otherwise, the column     
	    //switches to contain Strings.  Starting with v 1.3,   
	    //the table automatically converts value to an Integer,
	    //so you only need the code in the 'else' part of this 
	    //'if' block.                                          
	    //XXX: See TableEditDemo.java for a better solution!!!
	    try {
		data[row][col] = new Integer(value.toString());
		fireTableCellUpdated(row, col);
	    } catch (NumberFormatException e) {}
	} else {
	    data[row][col] = value;
	    fireTableCellUpdated(row, col);
	}
    }
}
