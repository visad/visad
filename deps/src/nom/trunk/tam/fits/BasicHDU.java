package nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 *
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */


import java.io.IOException;

import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;

import java.util.Date;

/** This abstract class is the parent of all HDU types.
  * It provides basic functionality for an HDU.
  */
public abstract class BasicHDU
{
  public static final int BITPIX_BYTE  =	 8;
  public static final int BITPIX_SHORT =	16;
  public static final int BITPIX_INT =  	32;
  public static final int BITPIX_LONG =  	64;
  public static final int BITPIX_FLOAT =	-32;
  public static final int BITPIX_DOUBLE =	-64;

  /** The associated header. */
  Header myHeader = null;

  /** The associated data unit. */
  Data myData = null;

  /** Create an HDU with the specified Header and an empty Data section
    * @param header the Header
    */
  public BasicHDU(Header header)
  {
    myHeader = header;
    if (myHeader != null) {
      myHeader.unsetMark();
    }
  }

  /** Create a Data object to correspond to the header description.
    * @return An unfilled Data object which can be used to read
    *         in the data for this HDU.
    * @exception FitsException if the Data object could not be created
    *				from this HDU's Header
    */
  abstract Data manufactureData() throws FitsException;

  /** Skip the Data object immediately after the given Header object on
    * the given stream object.
    * @param stream the stream which contains the data.
    * @param Header template indicating length of Data section
    * @exception IOException if the Data object could not be skipped.
    */
  public static void skipData(BufferedDataInputStream stream, Header hdr)
	throws IOException
  {
    stream.skipBytes(hdr.paddedDataSize());
  }

  /** Skip the Data object for this HDU.
    * @param stream the stream which contains the data.
    * @exception IOException if the Data object could not be skipped.
    */
  public void skipData(BufferedDataInputStream stream)
	throws IOException
  {
    skipData(stream, myHeader);
  }

  /** Read in the Data object for this HDU.
    * @param stream the stream from which the data is read.
    * @exception FitsException if the Data object could not be created
    *				from this HDU's Header
    */
  public void readData(BufferedDataInputStream stream)
	throws FitsException
  {
    myData = null;
    try {
      myData = manufactureData();
    } finally {
      // if we can't build a Data object, skip this section
      if (myData == null) {
	try {
	  skipData(stream, myHeader);
	} catch (Exception e) {
	}
      }
    }

    myData.read(stream);
  }

  /** Get the associated header */
  public Header getHeader() {
    return myHeader;
  }

  /** Get the associated Data object*/
  public Data getData() {
    return myData;
  }

  /** Get the total size in bytes of the HDU.
    * @return The size in bytes.
    */
  public int getSize() {
    int size = 0;

    if (myHeader != null) {
      size += myHeader.headerSize();
    }
    if (myData != null) {
      size += myData.getPaddedSize();
    }
    return size;
  }

  /** Check that this is a valid header for the HDU.
    * @param header to validate.
    * @return <CODE>true</CODE> if this is a valid header.
    */
  public static boolean isHeader(Header header) { return false; }

  /** Print out some information about this HDU.
    */
  public abstract void info();

  /** Check if a field is present and if so print it out.
    * @param The header keyword.
    * @param Was it found in the header?
    */
  boolean checkField(String name) {
    String value = myHeader.getStringValue(name);
    if (value == null) {
      return false;
    }

    System.out.print(" "+name+"="+value+";");
    return true;
  }

  /* Write out the HDU
   * @param stream The data stream to be written to.
   */
  public void write(BufferedDataOutputStream stream)
	throws FitsException
  {
    if (myHeader != null) {
      myHeader.write(stream);
    }
    if (myData != null) {
      myData.write(stream);
    }
    try {
      stream.flush();
    } catch (java.io.IOException e) {
      throw new FitsException("Error flushing at end of HDU: " +
			      e.getMessage());
    }
  }

  /**
    * Get the String value associated with <CODE>keyword</CODE>.
    * @param hdr	the header piece of an HDU
    * @param keyword	the FITS keyword
    * @return	either <CODE>null</CODE> or a String with leading/trailing
    * 		blanks stripped.
    */
  public String getTrimmedString(String keyword)
  {
    String s = myHeader.getStringValue(keyword);
    if (s != null) {
      s = s.trim();
    }
    return s;
  }

  public int getBitPix()
	throws FitsException
  {
    int bitpix = myHeader.getIntValue("BITPIX", -1);
    switch (bitpix) {
    case BITPIX_BYTE:
    case BITPIX_SHORT:
    case BITPIX_INT:
    case BITPIX_FLOAT:
    case BITPIX_DOUBLE:
      break;
    default:
      throw new FitsException("Unknown BITPIX type " + bitpix);
    }

    return bitpix;
  }

  public int[] getAxes()
	throws FitsException
  {
    int nAxis = myHeader.getIntValue("NAXIS", 0);
    if (nAxis < 0) {
      throw new FitsException("Negative NAXIS value " + nAxis);
    }
    if (nAxis > 999) {
      throw new FitsException("NAXIS value " + nAxis + " too large");
    }

    if (nAxis == 0) {
      return null;
    }

    int[] axes = new int[nAxis];
    for (int i = 1; i <= nAxis; i++) {
      axes[nAxis - i] = myHeader.getIntValue("NAXIS" + i, 0);
    }

    return axes;
  }

  public int getParameterCount()
  {
    return myHeader.getIntValue("PCOUNT", 0);
  }

  public int getGroupCount()
  {
    return myHeader.getIntValue("GCOUNT", 1);
  }

  public double getBScale()
  {
    return myHeader.getDoubleValue("BSCALE", 1.0);
  }

  public double getBZero()
  {
    return myHeader.getDoubleValue("BZERO", 0.0);
  }

  public String getBUnit()
  {
    return getTrimmedString("BUNIT");
  }

  public int getBlankValue()
	throws FitsException
  {
    if (!myHeader.containsKey("BLANK")) {
      throw new FitsException("BLANK undefined");
    }
    return myHeader.getIntValue("BLANK");
  }

  /**
    * Get the FITS file creation date as a <CODE>Date</CODE> object.
    * @return	either <CODE>null</CODE> or a Date object
    */
  public Date getCreationDate()
  {
    try {
      return new FitsDate(myHeader.getStringValue("DATE")).toDate();
    } catch (FitsException e) {
      return null;
    }
  }

  /**
    * Get the FITS file observation date as a <CODE>Date</CODE> object.
    * @return	either <CODE>null</CODE> or a Date object
    */
  public Date getObservationDate()
  {
    try {
      return new FitsDate(myHeader.getStringValue("DATE-OBS")).toDate();
    } catch (FitsException e) {
      return null;
    }
  }

  /**
    * Get the name of the organization which created this FITS file.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getOrigin()
  {
    return getTrimmedString("ORIGIN");
  }

  /**
    * Get the name of the telescope which was used to acquire the data in
    * this FITS file.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getTelescope()
  {
    return getTrimmedString("TELESCOP");
  }

  /**
    * Get the name of the instrument which was used to acquire the data in
    * this FITS file.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getInstrument()
  {
    return getTrimmedString("INSTRUME");
  }

  /**
    * Get the name of the person who acquired the data in this FITS file.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getObserver()
  {
    return getTrimmedString("OBSERVER");
  }

  /**
    * Get the name of the observed object in this FITS file.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getObject()
  {
    return getTrimmedString("OBJECT");
  }

  /**
    * Get the equinox in years for the celestial coordinate system in which
    * positions given in either the header or data are expressed.
    * @return	either <CODE>null</CODE> or a String object
    */
  public double getEquinox()
  {
    return myHeader.getDoubleValue("EQUINOX", -1.0);
  }

  /**
    * Get the equinox in years for the celestial coordinate system in which
    * positions given in either the header or data are expressed.
    * @return	either <CODE>null</CODE> or a String object
    * @deprecated	Replaced by getEquinox
    * @see	#getEquinox()
    */
  public double getEpoch()
  {
    return myHeader.getDoubleValue("EPOCH", -1.0);
  }

  /**
    * Return the name of the person who compiled the information in
    * the data associated with this header.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getAuthor()
  {
    return getTrimmedString("AUTHOR");
  }

  /**
    * Return the citation of a reference where the data associated with
    * this header are published.
    * @return	either <CODE>null</CODE> or a String object
    */
  public String getReference()
  {
    return getTrimmedString("REFERENC");
  }

  /**
    * Return the minimum valid value in the array.
    * @return	minimum value.
    */
  public double getMaximumValue()
  {
    return myHeader.getDoubleValue("DATAMAX");
  }

  /**
    * Return the minimum valid value in the array.
    * @return	minimum value.
    */
  public double getMinimumValue()
  {
    return myHeader.getDoubleValue("DATAMIN");
  }
}
