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
import java.util.*;
import nom.tam.util.*;

/** Methods to read/write FITS Header/Data unit (HDU).
  * This class is generally used either to get access to the Header and Data
  * objects, or to perform manipulations which affect both the Header and Data.
  */
public class HDU
{
  /** Create an HDU which points to the given object.  This may be either
    * a primitive array or an 2-d array of objects.
    * @param x The data to which the HDU points.
    * @return the appropriate HDU object
    * @exception FitsException if the HDU could not be created.
    */
  public static BasicHDU create(Object x)
	throws FitsException
  {
    String className = x.getClass().getName();
    if (className.equals("[[Ljava.lang.Object;") ) {
      return new BinaryTableHDU((Object[][] )x);
    }

    if (className.startsWith("[")) {
      return new ImageHDU(x);
    }

    throw new FitsException("Expected an array of some sort, not " +
			    className);
  }

  public static RandomGroupsHDU createRandomGroups(Object[][] x)
                                throws FitsException
  {
      return new RandomGroupsHDU(x);
  }

  public static AsciiTableHDU createAsciiTable(Object[][] x)
                                throws FitsException
  {
      throw new FitsException("ASCII tables not yet supported");
  }

  /** Create an HDU from the supplied Header object.
    * @param header the Header for the HDU to be created.
    * @return the appropriate HDU object
    * @exception FitsException if the HDU could not be created.
    */
  public static BasicHDU create(Header header)
    throws FitsException
  {
    BasicHDU hdu;
    if (PrimaryHDU.isHeader(header)) {
       return new PrimaryHDU(header);
    }

    if (ImageHDU.isHeader(header)) {
       return new ImageHDU(header);
    }

    if (BinaryTableHDU.isHeader(header)) {
       return new BinaryTableHDU(header);
    }

    if (AsciiTableHDU.isHeader(header)) {
       return new AsciiTableHDU(header);
    }

    if (RandomGroupsHDU.isHeader(header)) {
       return new RandomGroupsHDU(header);
    }

    if (A3DTableHDU.isHeader(header)) {
      return new A3DTableHDU(header);
    }

    throw new BadHeaderException("Unknown FITS header: Card 1=" +
      header.getCard(0).trim() + ')');
  }

  /** Read an HDU.
    * This is the usual method by which the Fits class reads an HDU.
    * @param stream The data stream the FITS data is to be found on.
    * @return The HDU that has been read.
    * @exception FitsException if there was a problem with the data.
    */
  public static BasicHDU readHDU(BufferedDataInputStream stream)
	throws FitsException, IOException
  {
    // Read the FITS header.  If we are at EOF, just return null.
    Header hdr = Header.readHeader(stream);
    if (hdr == null || !hdr.isValidHeader()) {
      return null;
    }

    // create the appropriate HDU object for this Header
    BasicHDU hdu;

    try {
      hdu = create(hdr);
    } catch (BadHeaderException e) {
      try {
        BasicHDU.skipData(stream, hdr);
        // Rethrow the original error after skipping data.
        throw e;
      } catch (FitsException fe) {
        // Tell the user we failed to skip data.
        throw new BadHeaderException(
          "Could not skip Data section of unknown FITS header (Card 1="+
          hdr.getCard(0).trim() + ").  Error is: " + fe.getMessage());
      }
    }

    hdu.readData(stream);

    return hdu;
  }


  /** Skip an HDU
    * @return true if the HDU was skipped.
    * @exception FitsException if the data could not be skipped.
    */
  public static boolean skipHDU(BufferedDataInputStream input)
	throws FitsException, IOException
  {
    Header nextHeader = Header.readHeader(input);
    if (nextHeader == null || !nextHeader.isValidHeader()) {
      return false;
    }

    try {
      BasicHDU.skipData(input, nextHeader);
    } catch (IOException e) {
      throw new FitsException("Error skipping data section:" + e.getMessage());
    }

    return true;
  }
}
