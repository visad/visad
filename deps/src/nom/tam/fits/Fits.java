package nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import nom.tam.util.*;


abstract class FauxHDU
	extends BasicHDU
{
  public FauxHDU() { super(null); }
  public abstract void throwException() throws FitsException;
  Data manufactureData() throws FitsException { throwException(); return null; }
}

class SkippedHDU
	extends FauxHDU
{
  public SkippedHDU() { }
  public void throwException()
	throws FitsException
  {
    throw new FitsException("Skipped HDU");
  }
  public void info() { System.out.println("Skipped HDU"); }
}

class BadHDU
	extends FauxHDU
{
  private FitsException exception;

  public BadHDU(FitsException e) { exception = e; }
  public void throwException()
	throws FitsException
  {
    throw exception;
  }
  public void info() {
    System.out.println("Bad HDU: " + exception.getMessage());
  }
}

/** This class provides access to routines to allow users
  * to read and write FITS files.
  * <p>
  * @version 0.6  March 22, 1998
  *
  * This version of the Java FITS library incorporates many changes
  * made by Dave Glowacki who greatly enhanced the handling of
  * header records and created the hierarchical organization of
  * FITS HDU types.
  * <p>
  * <b> Description of the Package </b>
  * <p>
  * This FITS package attempts to make using FITS files easy,
  * but does not do exhaustive error checking.  Users should
  * not assume that just because a FITS file can be read
  * and written that it is necessarily legal FITS.
  *
  *
  * <ul>
  * <li> The Fits class provides capabilities to
  *      read and write data at the HDU level, and to
  *      add and delete HDU's from the current  Fits object.
  *      A large number of constructors are provided which
  *      allow users to associate the Fits object with
  *      some form of external data.  This external
  *      data may be in a compressed format.
  * <li> The HDU class is a factory class which is used to
  *      create HDUs.  HDU's can be of a number of types
  *      derived from the abstract class BasicHDU.
  *      The hierarchy of HDUs is:
  *      <ul>
  *       <li>BasicHDU
  *           <ul>
  *           <li> PrimaryHDU
  *           <li> RandomGroupsHDU
  *           <li> ExtensionHDU
  *                <ul>
  *                <li> ImageHDU
  *                <li> TableHDU
  *                    <ul>
  *                    <li> BinaryTableHDU
  *                    <li> AsciiTableHDU (unimplemented)
  *                    </ul>
  *                </ul>
  *           <ul>
  *       </ul>
  *
  * <li> The Header class provides many functions to
  *      add, delete and read header keywords in a variety
  *      of formats.
  * <li> The HeaderCard class provides access to the structure
  *      of a FITS header card.
  * <li> The Data class is an abstract class which provides
  *      the basic methods for reading and writing FITS data.
  *      Users will likely only be interested in the getData
  *      method which returns that actual FITS data.
  * <li> The BinaryTable class provides a large number of
  *      methods to access and modify information in Binary
  *      tables.  Modifications to columns are best done
  *      done using the methods of HDU, but row manipulations
  *      are reasonably done directly using the BinaryTable
  *      class.  General users may find it convenient to
  *      use the getElement, Row and Column methods.
  * <li> The Column class
  *      combines the Header information and Data corresponding to
  *      a given column.
  * </ul>
  *
  * Copyright: Thomas McGlynn 1997-1998.
  * This code may be used for any purpose, non-commercial
  * or commercial so long as this copyright notice is retained
  * in the source code or included in or referred to in any
  * derived software.
  *
  */
public class Fits {

    /** The input stream associated with this Fits object.
      */
    private BufferedDataInputStream dataStr;

    /** A vector of HDUs that have been added to this
      * Fits object.
      */
    private Vector HDUList = new Vector(10);

    /** Has the input stream reached the EOF?
      */
    private boolean atEOF;

    /** Indicate the version of these classes */
    public static String version() {

         // Version 0.1: Original test FITS classes -- 9/96
         // Version 0.2: Pre-alpha release 10/97
         //              Complete rewrite using BufferedData*** and
         //              ArrayFuncs utilities.
         // Version 0.3: Pre-alpha release  1/98
         //              Incorporation of HDU hierarchy developed
         //              by Dave Glowacki and various bug fixes.
         // Version 0.4: Alpha-release 2/98
         //              BinaryTable classes revised to use
         //              ColumnTable classes.
         // Version 0.5: Random Groups Data 3/98
         // Version 0.6: Handling of bad/skipped FITS, FitsDate (D. Glowacki) 3/98

         return "0.6";
    }

    /** Create an empty Fits object which is not
      * associated with an input stream.
      */
    public Fits() {}

    /** Create a Fits object associated with
      * the given uncompressed data stream.
      * @param str The data stream.
      */
    public Fits(InputStream str) throws FitsException {
        this(str, false);
    }

    /** Create a Fits object associated with a possibly
      * compressed data stream.
      * @param str The data stream.
      * @param compressed Is the stream compressed?
      */
    public Fits(InputStream str, boolean compressed)
                throws FitsException {
        streamInit(str, compressed);
    }

    /** Do the stream initialization.
      *
      * @param str The input stream.  The Fits class
      *            uses a BufferedDataInputStream internally
      *            and will use this instance if
      *            it's already of that class.
      * @param compressed Is this data compressed?  If so,
      *            then the GZIPInputStream class will be
      *            used to inflate it.
      */
    protected void streamInit(InputStream str, boolean compressed)
                              throws FitsException {

      if (str == null) {
          throw new FitsException("Null input stream");
      }

      if (compressed) {
          try {
              str = new GZIPInputStream(str);
          } catch (IOException e) {
              throw new FitsException("Cannot inflate input stream"+e);
          }
      }

      if (str instanceof BufferedDataInputStream) {
          dataStr = (BufferedDataInputStream) str;
      } else {
          dataStr = new BufferedDataInputStream(str);
      }
    }

    /** Associate the Fits object with a File
      * @param myFile The File object.
      * @param compressed Is the data compressed?
      */
    public Fits(File myFile, boolean compressed) throws FitsException {
        fileInit(myFile, compressed);
    }

    /** Associate FITS object with an uncompressed File
      * @param myFile The File object.
      */
    public Fits(File myFile) throws FitsException {
        this(myFile, false);
    }

    /** Get a stream from the file and then use the stream initialization.
      * @param myFile  The File to be associated.
      * @param compressed Is the data compressed?
      */
    protected void fileInit(File myFile, boolean compressed) throws FitsException {

        try {
            FileInputStream str = new FileInputStream(myFile);
            streamInit(str, compressed);
        } catch (IOException e) {
              throw new FitsException("Unable to create Input Stream from File: "+myFile);
        }
    }

    private static boolean isCompressed(String filename)
    {
      int len = filename.length();
      return (len > 2 && (filename.substring(len-3).equalsIgnoreCase(".gz")));
    }

    /** Associate the FITS object with a file or URL.
      *
      * The string is assumed to be a URL if it begins with
      * http:  otherwise it is treated as a file name.
      * If the string ends in .gz it is assumed that
      * the data is in a compressed format.
      * All string comparisons are case insensitive.
      *
      * @param filename  The name of the file or URL to be processed.
      * @exception FitsException Thrown if unable to find or open
      *                          a file or URL from the string given.
      **/
    public Fits(String filename) throws FitsException {

      InputStream inp;

      if (filename == null) {
          throw new FitsException("Null FITS Identifier String");
      }

      boolean compressed = isCompressed(filename);

      int len = filename.length();
      if (len > 4 && filename.substring(0,5).equalsIgnoreCase("http:") ) {
          // This seems to be a URL.
          URL myURL;
          try {
               myURL = new URL(filename);
          } catch (IOException e) {
               throw new FitsException ("Unable to convert string to URL: "+filename);
          }
          try {
              InputStream is = myURL.openStream();
              streamInit(is, compressed);
          } catch (IOException e) {
              throw new FitsException ("Unable to open stream from URL:"+filename+" Exception="+e);
          }
      } else {
          fileInit(new File(filename), compressed);

      }

    }

    /** Associate the FITS object with a given URL
      * @param myURL  The URL to be associated with the FITS file.
      * @param compressed Is the data compressed?
      * @exception FitsException Thrown if unable to find or open
      *                          a file or URL from the string given.
      */
    public Fits (URL myURL, boolean compressed) throws FitsException {
	  try {
            streamInit(myURL.openStream(), compressed);
        } catch (IOException e) {
            throw new FitsException("Unable to open input from URL:"+myURL);
        }
    }

    /** Associate the FITS object with a given uncompressed URL
      * @param myURL  The URL to be associated with the FITS file.
      * @exception FitsException Thrown if unable to use the specified URL.
      */
    public Fits (URL myURL) throws FitsException {
        this(myURL, isCompressed(myURL.getFile()));
    }

    /** Return all HDUs for the Fits object.   If the
      * FITS file is associated with an external stream make
      * sure that we have exhausted the stream.
      * @return an array of all HDUs in the Fits object.
      */

    public BasicHDU[] read() throws FitsException {

      readToEnd();

      int size = currentSize();
      if (size == 0) {
          return null;
      }

      BasicHDU[] hdus = new BasicHDU[size];
      HDUList.copyInto(hdus);
      return hdus;
    }

    /** Read the next HDU on the default input stream.
      * @return The HDU read, or null if an EOF was detected.
      * Note that null is only returned when the EOF is detected immediately
      * at the beginning of reading the HDU (i.e., the first card image in the header).
      */
    public BasicHDU readHDU() throws FitsException, IOException {

      if (dataStr == null || atEOF) {
          return null;
      }

      BasicHDU nextHDU;
      try {
	nextHDU = HDU.readHDU(dataStr);
      } catch (FitsException e) {
	nextHDU = new BadHDU(e);
      }
      if (nextHDU == null) {
          atEOF = true;
      } else {
          HDUList.addElement(nextHDU);
      }

      if (nextHDU instanceof FauxHDU) {
	((FauxHDU )nextHDU).throwException();
      }
      return nextHDU;
    }

    /** Skip HDUs on the associate input stream.
      * @param n The number of HDUs to be skipped.
      */
    public void skipHDU(int n) throws FitsException, IOException {
        for (int i=0; i<n; i += 1) {
            skipHDU();
        }
    }

    /** Skip the next HDU on the default input stream.
      */
    public void skipHDU() throws FitsException, IOException {

        if (!atEOF && !HDU.skipHDU(dataStr)) {
            atEOF = true;
        } else {
            HDUList.addElement(new SkippedHDU());
        }
    }

   /** Return the n'th HDU.
     * If the HDU is already read simply return a pointer to the
     * cached data.  Otherwise read the associated stream
     * until the n'th HDU is read.
     * @param n The index of the HDU to be read.  The primary HDU is index 0.
     * @return The n'th HDU or null if it could not be found.
     */
    public BasicHDU getHDU(int n) throws FitsException, IOException {

      int size = currentSize();
      if (size > n) {
	 BasicHDU hdu;
         try {
             hdu = (BasicHDU) HDUList.elementAt(n);
         } catch (NoSuchElementException e) {
             throw new FitsException("Internal Error: Vector mismatch");
         }
	 if (hdu instanceof FauxHDU) {
	     ((FauxHDU )hdu).throwException();
	 }
	 return hdu;
      }

      for (int i=size; i <= n; i += 1) {
	  BasicHDU hdu;
	  try {
	    hdu = readHDU();
	  } catch (FitsException e) {
	    hdu = new BadHDU(e);
	  }
          if (hdu == null) {
              return null;
          }
      }

      try {
          return (BasicHDU) HDUList.elementAt(n);
      } catch (NoSuchElementException e) {
          throw new FitsException("Internal Error: HDUList build failed");
      }
    }

    /** Read to the end of the associated input stream */
    private void readToEnd() throws FitsException {
      while (dataStr != null && !atEOF) {
          try {
	      if (readHDU() == null) {
                  break;
              }
          } catch (IOException e) {
              throw new FitsException("IO error: "+e);
          }
      }
    }


    /** Return the number of HDUs in the Fits object.   If the
      * FITS file is associated with an external stream make
      * sure that we have exhausted the stream.
      * @return number of HDUs.
      */
    public int size() throws FitsException {
      readToEnd();
      return currentSize();
    }

    /** Add an HDU to the Fits object.  Users may intermix
      * calls to functions which read HDUs from an associated
      * input stream with the addHDU and insertHDU calls,
      * but should be careful to understand the consequences.
      *
      * @param myHDU  The HDU to be added to the end of the FITS object.
      */
    public void addHDU(BasicHDU myHDU)
	throws FitsException
    {

      if (myHDU == null) {
          return;
      }

      if (currentSize() == 0) {
          if (myHDU instanceof ImageHDU) {
	    myHDU = new PrimaryHDU((ImageHDU )myHDU);
	  } else if (!(myHDU instanceof PrimaryHDU)) {
	    HDUList.addElement(new PrimaryHDU());
	  }
      } else if (myHDU instanceof PrimaryHDU) {
	  myHDU = new ImageHDU((PrimaryHDU )myHDU);
      }

      HDUList.addElement(myHDU);
    }

    /** Insert a FITS object into the list of HDUs.
      *
      * @param myHDU The HDU to be inserted into the list of HDUs.
      * @param n     The location at which the HDU is to be inserted.
      *              If n is 0, then the previous initial HDU will
      *              be converted into an IMAGE extension.
      */

    public void insertHDU(BasicHDU myHDU, int n)
	throws FitsException
    {

      if (myHDU == null) {
          return;
      }

      if (n < 0 || n >= currentSize()) {
          throw new FitsException("Attempt to insert HDU at invalid location: "+n);
      }
      try {
          HDUList.insertElementAt(myHDU, n);
          if (n == 0) {
	      PrimaryHDU old = (PrimaryHDU )HDUList.elementAt(1);
              HDUList.setElementAt(new ImageHDU(old), 1);
          }
      } catch (NoSuchElementException e) {
          throw new FitsException("Internal Error: HDUList Vector Inconsistency");
      }
    }

    /** Delete an HDU from the HDU list.
      *
      * @param n  The index of the HDU to be deleted.
      *           If n is 0 and there is more than one HDU present, then
      *           the next HDU will be converted from an image to
      *           primary HDU if possible.  If not a dummy header HDU
      *           will then be inserted.
      */
    public void deleteHDU(int n) throws FitsException  {
      int size = currentSize();
      if (n < 0 || n >= size) {
          throw new FitsException("Attempt to delete non-existent HDU:"+n);
      }
      try {
          HDUList.removeElementAt(n);
          if (n == 0 && size > 0) {
              if (! (HDUList.elementAt(0) instanceof PrimaryHDU)) {
                  insertHDU(new PrimaryHDU(), 0);
              }
          }
      } catch (NoSuchElementException e) {
          throw new FitsException("Internal Error: HDUList Vector Inconsitency");
      }
    }

    /** Write a Fits Object to an external Stream.
      *
      * @param dos  A DataOutput stream.
      */
    public void write(OutputStream os) throws FitsException {

      BufferedDataOutputStream obs;

      if (os instanceof BufferedDataOutputStream) {
          obs = (BufferedDataOutputStream) os;
      } else {
          obs = new BufferedDataOutputStream(os);
      }

	BasicHDU  hh;
	for (int i=0; i<currentSize(); i += 1) {
	    try {
		hh = (BasicHDU) HDUList.elementAt(i);
	      hh.write(obs);
	    } catch (ArrayIndexOutOfBoundsException e) {
		throw new FitsException("Internal Error: Vector Inconsistency");
	    }
	}

    }

    /** Read a FITS file from an InputStream object.
      *
      * @param is The InputStream stream whence the FITS information
      *            is found.
      */
    public void read(InputStream is) throws FitsException, IOException {

      if (is instanceof BufferedDataInputStream) {
	    dataStr = (BufferedDataInputStream) is;
      } else {
          dataStr = new BufferedDataInputStream(is);
      }
      read();
    }

   /** Get the current number of HDUs in the Fits object.
     * @return The number of HDU's in the object.
     */
    public int currentSize() {
        return HDUList.size();
    }

    /** Get the data stream used for the Fits Data.
      * @return The associated data stream.  Users may wish to
      *         call this function after opening a Fits object when
      *         they wish detailed control for writing some part of the FITS file.
      */

    public BufferedDataInputStream getStream() {
        return dataStr;
    }

    /** Set the data stream to be used for future input.
      *
      * @param stream The data stream to be used.
      */
    public void setStream(BufferedDataInputStream stream) {
        dataStr = stream;
        atEOF = false;
    }

    public static void main(String args[])
	throws FitsException
    {
      if (args.length != 1) {
	System.err.println("Usage: Fits file");
	System.exit(1);
	return;
      }

      Fits fits = new Fits(args[0]);

      try {
	System.out.println("Fits: " + fits);
      } catch (Exception e) {
	System.err.println(args[0] + " print threw " + e.getMessage());
	e.printStackTrace(System.err);
	System.exit(1);
	return;
      }

      for (int n = 0; true; n++) {
	BasicHDU hdu;
	try {
	  hdu = fits.getHDU(n);
	  if (hdu == null) {
	    break;
	  }
	} catch (Exception e) {
	  System.err.println(args[0] + "#" + n + " fetch threw " +
			     e.getMessage());
	  e.printStackTrace(System.err);
	  System.exit(1);
	  return;
	}
	try {
	  System.out.println("Fits: " + args[0] + "#" + n + "= " + hdu);
	} catch (Exception e) {
	  System.err.println(args[0] + "#" + n + " print threw " +
			     e.getMessage());
	  e.printStackTrace(System.err);
	  System.exit(1);
	  return;
	}
      }
    }
}
