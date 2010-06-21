package dods.clients.matlab;
import java.lang.*;
import java.util.*;
import dods.dap.*;

/**
 * This class takes a MatlabSequence object, and provides methods to return
 * the columns of the sequence as arrays of atomic types.  I wrote this before
 * I subclassed DSequence, so I may end up moving these functions into 
 * MatlabSequence and doing away with this class.
 *
 * Note: Java doesn't have any unsigned types, so the getU* functions return
 *       a signed variable
 * 
 */

class DodsSequenceProcessor extends Object {
    private MatlabSequence dodsSeq;

    public DodsSequenceProcessor(MatlabSequence seq) {
	dodsSeq = seq;
    }

    /**
     * Get a column of DStrings from the sequence and return it as 
     * a 2D char array.
     * @param name The name of the column
     * @return A 2D character array holding the values of the strings
     */
    public char[][] getString(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	char[][] values = new char[dodsSeq.getRowCount()][];
	BaseType[] temp = null;


	try {
	    temp = dodsSeq.getColumn(name);
	} 
	catch(NoSuchVariableException e) { 
	    throw(e); 
	}

	if(temp[0] instanceof DString) {
	    for(int i=0; i<dodsSeq.getRowCount(); i++) {
		values[i] = ((DString)temp[i]).getValue().toCharArray();
	    }
	}
	return values;
    }

    /** 
     * Get a column of DBytes from the sequence and return it as an 
     * array of bytes
     * @param name The name of the column
     * @return an array of bytes containing the data.
     */
    public byte[] getByte(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	byte[] values = new byte[numVars];
	BaseType temp[] = null;

	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DByte) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DByte)temp[i]).getValue();
	    }
	}
	return values;
    }

    /** 
     * Get a column of DInt16s from the sequence and return it as an 
     * array of shorts
     * @param name The name of the column
     * @return an array of shorts containing the data.
     */
    public short[] getInt16(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	short[] values = new short[numVars];
	BaseType temp[] = null;

	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DInt16) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DInt16)temp[i]).getValue();
	    }
	}


	return values;
    }

    /** 
     * Get a column of DUInt16s from the sequence and return it as an 
     * array of shorts.
     *   Note: This function returns signed types which must be convertered
     *         in matlab.
     * @param name The name of the column
     * @return an array of bytes containing the data.
     */
    public short[] getUInt16(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	short[] values = new short[numVars];
	BaseType temp[] = null;

	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DUInt16) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DUInt16)temp[i]).getValue();
	    }
	}

	return values;
    }

    /** 
     * Get a column of DInt32s from the sequence and return it as an 
     * array of ints
     * @param name The name of the column
     * @return an array of ints containing the data.
     */
    public int[] getInt32(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	int[] values = new int[numVars];
	BaseType temp[] = null;

	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DInt32) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DInt32)temp[i]).getValue();
	    }
	}

	return values;
    }

     /** 
     * Get a column of DUInt32s from the sequence and return it as an 
     * array of ints.
     *   Note: This function returns signed types which must be convertered
     *         in matlab.
     * @param name The name of the column
     * @return an array of ints containing the data.
     */
    public int[] getUInt32(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	int[] values = new int[numVars];
	BaseType temp[] = null;
	
	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DUInt32) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DUInt32)temp[i]).getValue();
	    }
	}

	return values;
    }

    /** 
     * Get a column of DFloat32s from the sequence and return it as an 
     * array of floats
     * @param name The name of the column
     * @return an array of floats containing the data.
     */
    public float[] getFloat32(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	float[] values = new float[numVars];
	BaseType temp[] = null;

	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DFloat32) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DFloat32)temp[i]).getValue();
	    }
	}

	return values;
    }

    /** 
     * Get a column of DFloat64s from the sequence and return it as an 
     * array of doubles
     * @param name The name of the column
     * @return an array of doubles containing the data.
     */
    public double[] getFloat64(String name) 
	throws NoSuchVariableException 
    {
	int numVars = dodsSeq.getRowCount();
	double[] values = new double[numVars];
	BaseType temp[] = null;

	try {
	    temp = dodsSeq.getColumn(name);
	}
	catch(NoSuchVariableException e) {
	    throw(e);
	}
	
	if(temp[0] instanceof DFloat64) {
	    for(int i=0; i<numVars; i++) {
		values[i] = ((DFloat64)temp[i]).getValue();
	    }
	}

	return values;
    }
};
