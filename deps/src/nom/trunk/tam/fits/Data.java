package nom.tam.fits;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */

import java.io.*;
import nom.tam.util.*;

/** This class provides methods to access the data segment of an
  * HDU.
  */

public abstract class Data {

    /** This is the object which contains the actual data for the HDU.
      * <ul>
      *  <li> For images and primary data this is a simple (but possibly
      *       multi-dimensional) primitive array.  When group data is
      *       supported it will be a possibly multidimensional array
      *       of group objects.
      *  <li> For ASCII data it is a two dimensional Object array where
      *       each of the constituent objects is a primitive array of length 1.
      *  <li> For Binary data it is a two dimensional Object array where
      *       each of the constituent objects is a primitive array of arbitrary
      *       (more or less) dimensionality.
      *  </ul>
      */
    protected Object	dataArray;

    /** Write the data -- including any buffering needed
      * @param o  The output stream on which to write the data.
      */
    public void write(BufferedDataOutputStream o) throws FitsException {

         this.writeTrueData(o);
         byte[] padding = new byte[getPadding()];
         try {
             o.writePrimitiveArray(padding);
         } catch (IOException e) {
             throw new FitsException ("Error writing padding: "+e);
         }

    }

    /** Read a data array into the current object and if needed position
      * to the beginning of the next FITS block.
      * @param i The input data stream
      */
    public void read(BufferedDataInputStream i) throws FitsException {

         readTrueData(i);
         int pad = getPadding();
         try {
             byte[] buf = new byte[pad];
             while(pad > 0) {
                 int len = i.read(buf, 0, pad);
                 if (len == 0) {
                     throw new FitsException("Data Padding EOF");
                 }
                 pad -= len;
             }
         //    i.skipBytes(getPadding());
         } catch (EOFException e) {
	   // ignore EOF messages while reading padded data
         } catch (IOException e) {
             throw new FitsException("Error skipping padding:"+e);
         }

    }

    /** Write only the actual data.
      * @param o  The output stream on which to write the data.
      */
    protected void writeTrueData(BufferedDataOutputStream o) throws FitsException {

        try {
            o.writePrimitiveArray(dataArray);
        } catch (IOException e) {
            throw new FitsException("FITS Output Error: "+e);
        }
    }

    /** Read in the actual data portion.  This method needs to be
      * overriden for ASCII tables and for binary tables with
      * variable length data.
      * @param i The input stream.
      */
    protected void readTrueData(BufferedDataInputStream i) throws FitsException {
        try {
            i.readPrimitiveArray(dataArray);
        } catch (IOException e) {
            throw new FitsException("FITS Input Error: "+e);
        }
    }

    /** Get the amount of padding needed to fill in or skip to the beginning
      * of the next FITS block.
      */
    public int getPadding() {

        int len = getTrueSize() % 2880;

        if (len == 0) {
            return 0;
        }

        return 2880 - len;
    }

    /** Get the size of the actual data elements.
      */
    public int getTrueSize() {
         int len = ArrayFuncs.computeSize(dataArray);
         return len;
    }

    /** Get the size of the entire data area including any padding.
      */
    public int getPaddedSize() {
         return getTrueSize() + getPadding();
    }

    /** Return the data array object.
      */
    public Object getData() {
         return dataArray;
    }

}
