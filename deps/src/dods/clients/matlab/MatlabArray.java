package dods.clients.matlab;

import dods.dap.*;
import java.lang.*;

/** 
 * An extention of the DArray class, with optimizations for Matlab.
 *
 * @see DArray
 */

public class MatlabArray extends DArray {
    
    /**
     * Construct a new <code>MatlabArray</code>.
     */
    public MatlabArray() {
	super();
    }
    
    /**
     * Construct a new <code>MatlabArray</code> with name <code>name</code>.
     * @param name The name of the array
     */
    public MatlabArray(String name) {
	super(name);
    }

    /**
     * Return the type of data held in the array as a <code>String</code>.
     * @return The type of data held in the array.
     */
    public String getArrayTypeName() {
	PrimitiveVector pv = getPrimitiveVector();
	BaseType varTemplate = pv.getTemplate();
	return varTemplate.getTypeName();
    }

    /** 
     * Return the data held in the MatlabArray as an array of 
     * atomic types.
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

    /** 
     * The following functions are taken almost verbatim from extend.c
     * in C++ matlab client distribution.
     */
	
    /** 
     * Given an index into, and the dimensions of the array, return the offset
     * needed to access the referenced element assuming row-major storage 
     * order.
     * @param current_index The index to retrieve the offset for
     * @param ndims The number of dimensions
     * @param dims An array containing The dimensions of the array
     * @return The offset needed to retrieve <code>current_index</code> from
     *         a single-dimension array in row-major order.
     */
    protected static int get_rm_offset(int[] current_index, int ndims, 
				     int[] dims)
    {
	int offset = 0;
	int j, k, t;
	for (j = 0; j < ndims; ++j) {
	    t = 1;
	    for (k = j+1; k < ndims; ++k)
		t *= dims[k];
	    offset += current_index[j] * t;
	}
	
	return offset;
    }

    /** 
     * Given an index into, and the dimensions of the array, return the offset
     * needed to access the referenced element assuming column-major storage
     * order. 
     * @param current_index The index to retrieve the offset for
     * @param ndims The number of dimensions
     * @param dims An array containing the dimensions of the array
     * @return The offset needed to retrieve <code>current_index</code> from
     *         a single-dimension array in column-major order.
     */
    protected static int get_cm_offset(int[] current_index, int ndims, 
				     int[] dims)
    {
	int offset = 0;
	int j, k, t;
	for (j = 0; j < ndims; ++j) {
	    t = 1;
	    for (k = 0; k < j; ++k)
		t *= dims[k];
	    offset += current_index[j] * t;
	}
	
	return offset;
    }

    /** 
     * Given the double array described by rm_dims[ndims], transform that array
     * from row-major to column-major order. Put the result in dest.
     * <p>
     * Assume that the rm_dims array contains the dimension information in
     * row-major order.
     *
     * @param src The column-major array to convert to row-major
     * @param ndims The number of dimensions in the array
     * @param dims An array containing the dimensions of the array
     */
    public static double[] rm2cm(double[] src, int ndims, int rm_dims[])
    {
        int[] cm_index;
	int[] rm_index;
	int[] cm_dims;
	int num_elements = 1;
	int rm_offset, cm_offset;
	double[] dest;

	for(int i=0;i<ndims;i++) {
	    num_elements *= rm_dims[i];
	}

	dest = new double[num_elements];

	if (ndims <= 2) {
	     
	    /**
	     * An optimization: when ndims is 0, 1 or 2 then the rm and cm 
	     * index order is the same. Only transform the indices when 
	     * ndims is > 2. 
	     */
    
	    rm_index = new int[ndims];
	    do {
		rm_offset = get_rm_offset(rm_index, ndims, rm_dims);
		cm_offset = get_cm_offset(rm_index, ndims, rm_dims);
		
		dest[cm_offset] = src[rm_offset];
 	    } while (get_next_rm_index(rm_index, ndims, rm_dims));
	    
	}
	else {
	    cm_index = new int[ndims];
	    rm_index = new int[ndims];
	    cm_dims = new int[ndims];
	    rm_index2cm_index(cm_dims, rm_dims, ndims);
	    
	    do {
		rm_offset = get_rm_offset(rm_index, ndims, rm_dims);
		rm_index2cm_index(cm_index, rm_index, ndims);
		cm_offset = get_cm_offset(cm_index, ndims, cm_dims);
		
		dest[cm_offset] = src[rm_offset];
	    } while (get_next_rm_index(rm_index, ndims, rm_dims));
	   
        }
    
	return dest;
    }
    
  /** 
   * Given the row-major index array (with ndims-1 elements), load values into
   * cm_dims so that the dimensions will be listed in Matlab's n-major order. 
   * <p>
   *  Assume that enough storage has already been allocated to cm_dims. 
   * <p><code>
   * Algorithm: RM order: Plane, Row,   Column<br>
   *            NM order: Row,   Column, Plane.
   * </code><p>
   * Move the last two dimensions from the RM order to the first two of the NM
   * order. Then copy each plane dimension from the front of the RM ordering
   * to the back of the NM ordering reversing those entries as they are
   * copied. 
   * <p>
   * Note that if ndims is less then three then this function just copies the
   * values and, in that case, it is better to not use this function. 
   *
   * @param cm_dims An array for which the appropriate memory has been already
   *                allocated to store the column-major dimensions in
   * @param rm_dims An array containing the row-major dimensions to be
   *                converted into column-major 
   * @param ndims The number of dimensions
   */
    protected static void rm_index2cm_index(int[] cm_dims, int[] rm_dims, 
					  int ndims)
    {
	switch(ndims) {
	case 1:
	    cm_dims[0] = rm_dims[0];
	    break;
	    
	case 2:
	    cm_dims[0] = rm_dims[0];
	    cm_dims[1] = rm_dims[1];
	    break;
	    
	default: 
	    {		/* ndims >= 3 */
		int i, j;
		cm_dims[0] = rm_dims[ndims-2];
		cm_dims[1] = rm_dims[ndims-1];
		
		/* Copy and reverse the plane (or page) entries */
		/* What this loop does: P0 P1 P2 R C
		   <--  ^
		   | i
		   Start i at P2 and move it towards the front of the rm_dims
		   array. Tack each plane index P* onto the end of cm_dims so
		   that the final ordering looks like: R C P2 P1 P0 */
		j = 2;
		for (i = ndims-3; i >= 0; i--)
		    cm_dims[j++] = rm_dims[i];
		break;
	    }
	}
    }
  
    /** 
     * A public interface to rm_index2cm_index which doesn't require
     * memory to be allocated for the array containing the column-major
     * dimensions
     *
     * @param rm_dims An array containing the row-major dimensions to be
     *                converted into column-major 
     * @param ndims The number of dimensions
     * @return An array containing the column-major dimensions
     */
    public static int[] rm_index2cm_index(int[] rm_dims, int ndims)
    {
	int[] cm_dims = new int[ndims];
	rm_index2cm_index(cm_dims, rm_dims, ndims);
	return cm_dims;
    }

    /** 
     * Given a current index tuple and the dimensionality of the data, compute 
     * the next tuple. Vary the rightmost element of the tuple the fastest.
     * Return true if there *is* a next tuple (which is passed back in
     * current_index), false otherwise.
     *
     * @param current_index An array containing the rm index to be incremented
     * @param ndims The number of dimensions in the array
     * @param dims An array containing the dimensions of the array.
     */
    protected static boolean get_next_rm_index(int[] current_index, int ndims, 
					       int[] dims)
    {
	int i;
	for (i = ndims-1; i >= 0; --i) {
	    current_index[i]++;
	    if (current_index[i] < dims[i])
		return true;
	    else
		current_index[i] = 0;
	}
	
	return false;
    }
};
