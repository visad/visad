package nom.tam.fits;
/* Copyright: Thomas McGlynn 1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */

import nom.tam.fits.*;

/** This class instantiates FITS Random Groups data.
  * Random groups are instantiated as a two-dimensional
  * array of objects.  The first dimension of the array
  * is the number of groups.  The second dimension is 2.
  * The first object in every row is a one dimensional
  * parameter array.  The second element is the n-dimensional
  * data array.
  */
public class RandomGroupsData extends Data {

    /** Create a random groups array from a header description.
      * This is typically how data will be created when reading
      * FITS data from a file where the header is read first.
      * This creates an empty array.
      * @param h header to be used as a template.
      * @exception FitsException if there was a problem with the header description.
      */
    public RandomGroupsData(Header h) throws FitsException {


    Class baseClass;

    int gcount = h.getIntValue("GCOUNT", -1);
    int pcount = h.getIntValue("PCOUNT", -1);

    if (!h.getBooleanValue("GROUPS") ||
         h.getIntValue("NAXIS1", -1) != 0 ||
         gcount < 0 || pcount < 0  ||
         h.getIntValue("NAXIS")< 2) {
        throw new FitsException("Invalid Random Groups Parameters");
    }

	// Allocate the object.
	if (gcount > 0) {
	    dataArray = new Object[gcount][2];
	} else {
	    dataArray = new Object[0][];
	}

	Object[] sampleRow = generateSampleRow(h);
	for (int i=0; i<gcount; i += 1) {
	    ((Object[])dataArray)[i] = nom.tam.util.ArrayFuncs.deepClone(sampleRow);
	}
}

public static Object[] generateSampleRow (Header h)
                                         throws FitsException {

	int ndim = h.getIntValue("NAXIS", 0) - 1;
	int[] dims = new int[ndim];

	int bitpix =  h.getIntValue("BITPIX", 0);
	Class baseClass;

	if (bitpix == 8) {
	    baseClass = Byte.TYPE;
	} else if (bitpix == 16) {
	    baseClass = Short.TYPE;
	} else if (bitpix == 32) {
	    baseClass = Integer.TYPE;
	} else if (bitpix == 64) {  /* This isn't a standard for FITS yet...*/
	    baseClass = Long.TYPE;
	} else if (bitpix == -32) {
	    baseClass = Float.TYPE;
	} else if (bitpix == -64) {
	    baseClass = Double.TYPE;
	} else {
	    throw new FitsException("Invalid BITPIX:"+bitpix);
	}

    // Note that we have to invert the order of the axes
	// for the FITS file to get the order in the array we
    // are generating.  Also recall that NAXIS1=0, so that
    // we have an 'extra' dimension.

	for (int i=0; i<ndim; i += 1) {
	    long cdim = h.getIntValue("NAXIS"+(i+2), 0);
        if (cdim < 0) {
            throw new FitsException("Invalid array dimension:"+cdim);
        }
	    dims[ndim-i-1] = (int) cdim;
	}

	Object[] sample = new Object[2];
	sample[0] = java.lang.reflect.Array.newInstance(baseClass, h.getIntValue("PCOUNT"));
	sample[1] = java.lang.reflect.Array.newInstance(baseClass, dims);
	return sample;

}



/** Create the equivalent of a null data element.
  */
public RandomGroupsData() {
    dataArray = new Object[0][];
}

/** Create a RandomGroupsData object using the specified object to
  * initialize the data array.
  * @param x The initial data array.  This should a two-d
  *          array of objects as described above.
  */
public RandomGroupsData(Object[][] x) {
    dataArray = x;
}

public int getPadding() {

    Object par = ((Object[][]) dataArray)[0][0];
    Object dat = ((Object[][]) dataArray)[0][1];

    int nbyte = nom.tam.util.ArrayFuncs.getBaseLength(par);
    int npar = java.lang.reflect.Array.getLength(par);
    int[] ndims = nom.tam.util.ArrayFuncs.getDimensions(dat);
    int ngrp = ((Object[][])dataArray).length;

    int total = 1;
    for (int i=0; i < ndims.length; i += 1) {
        total *= ndims[i];
    }

    total += npar;
    total = nbyte*ngrp*total;

    if (total % 2880 == 0) {
        return 0;
    } else {
        return 2880 - total%2880;
    }
}

}
