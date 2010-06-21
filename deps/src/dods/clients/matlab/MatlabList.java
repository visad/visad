package dods.clients.matlab;

import dods.dap.*;
import java.lang.*;

/** 
 * An extention of the DList class which provides methods to return
 * the data held inside the list as atomic types, limiting the necessary
 * interaction between matlab and java.
 *
 * @see DList
 */

public class MatlabList extends DList {

    /**
     * Constructs a new <code>MatlabList</code>.
     */
    public MatlabList() {
	super();
    }
    
    /**
     * Constructs a new <code>MatlabList</code> with name <code>name</code>.
     * @param name The name of the array
     */
    public MatlabList(String name) {
	super(name);
    }

    /**
     * Returns the type of data held in the array as a <code>String</code>.
     * @return The type of data held in the array.
     */
    public String getArrayTypeName() {
	PrimitiveVector pv = getPrimitiveVector();
	BaseType varTemplate = pv.getTemplate();
	return varTemplate.getTypeName();
    }

    /** 
     * If the data held inside the list is either an atomic type or a 
     * String, this function will return that data as a single-dimensional
     * array (or a two-dimensional char array in the case of String).
     * @return The data.
     */
    public Object getData() {
	PrimitiveVector pv = getPrimitiveVector();
	if( (pv instanceof BaseTypePrimitiveVector) == false) 
	    return pv.getInternalStorage();
	
	else {
	    BaseTypePrimitiveVector basePV = (BaseTypePrimitiveVector)pv;
	    BaseType varTemplate = (BaseType)basePV.getValue(0);
	    if(varTemplate instanceof MatlabString) {
		char[][] arrayData = new char[basePV.getLength()][];
		for(int i=0;i<pv.getLength();i++) {
		    arrayData[i] = ((MatlabString)basePV.getValue(i)).getValue().toCharArray();
		}
		return arrayData;
	    }
	    else if(varTemplate instanceof MatlabURL) {
		char[][] arrayData = new char[basePV.getLength()][];
		for(int i=0;i<pv.getLength();i++) {
		    arrayData[i] = ((MatlabURL)basePV.getValue(i)).getValue().toCharArray();
		}
		return arrayData;
	    }
	    else return null;
	}
    }
}
