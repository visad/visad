package nom.tam.fits;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */


/** This class instantiates FITS primary HDU and IMAGE extension data.
  * Essentially these data are a primitive multi-dimensional array.
  */
public class ImageData extends Data {

    /** Create an array from a header description.
      * This is typically how data will be created when reading
      * FITS data from a file where the header is read first.
      * This creates an empty array.
      * @param h header to be used as a template.
      * @exception FitsException if there was a problem with the header description.
      */
    public ImageData(Header h) throws FitsException {

 	int bitpix;
    	int type;
	int ndim;
	int[] dims;

	long trueSize;
	int i;

      Class baseClass;

	int gCount = h.getIntValue("GCOUNT",1);
	int pCount = h.getIntValue("PCOUNT",0);
	if (gCount > 1  || pCount != 0) {
              throw new FitsException("Currently unable to handle GROUPed data"+
                "  GCOUNT="+gCount+"; PCOUNT="+pCount);
      }

	bitpix = (int) h.getIntValue("BITPIX", 0);
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

	ndim = h.getIntValue("NAXIS   ", 0) ;
	dims = new int[ndim];


     	// Note that we have to invert the order of the axes
	// for the FITS file to get the order in the array we
      // are generating.

	for (i=0; i<ndim; i += 1) {
	    long cdim = h.getIntValue("NAXIS"+(i+1), 0);
          if (cdim < 0) {
              throw new FitsException("Invalid array dimension:"+cdim);
          }
	    dims[ndim-i-1] = (int) cdim;
	}

      if (ndim > 0) {
          // dataArray is inherited from Data
          dataArray = java.lang.reflect.Array.newInstance(baseClass, dims);
      } else {
          int[] dim = new int[1];
          dim[0] = 0;
          dataArray = java.lang.reflect.Array.newInstance(baseClass, dim);
      }
    }

    /** Create the equivalent of a null data element.
      */
    public ImageData() {
        dataArray = new byte[0];
    }

    /** Create an ImageData object using the specified object to
      * initialize the data array.
      * @param x The initial data array.  This should be a primitive
      *          array but this is not checked currently.
      */
    public ImageData(Object x) {
        dataArray = x;
    }
}
