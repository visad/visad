package nom.tam.fits;
/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */

import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;

/** FITS image header/data unit */
public class ImageHDU
	extends ExtensionHDU
{
  /** Create an image header/data unit.
    * @param header the template specifying the image.
    * @exception FitsException if there was a problem with the header.
    */
  public ImageHDU(Header header)
	throws FitsException
  {
    super(header);
    if (!isHeader()) {
      throw new BadHeaderException("Not a valid image header");
    }
  }

  /** Build an image HDU from a RandomGroupsHDU.
    * @param primary the RandomGroupsHDU containing the image data.
    * @exception FitsException if there was a problem with the data.
    */
  public ImageHDU(PrimaryHDU primary)
	throws FitsException
  {
    // this is currently a hack; should clone RandomGroupsHDU header and data

    super(primary.myHeader);

    if (myHeader != null) {
      if (!myHeader.primaryToImage()) {
	throw new FitsException("Couldn't create ImageHDU from PrimaryHDU");
      }
    }

    if (!isHeader()) {
      throw new FitsException("Header was not converted to a valid image header");
    }

    myData = primary.myData;
  }

  /** Build an image HDU using the supplied data.
    * @param obj the data used to build the image.
    * @exception FitsException if there was a problem with the data.
    */
  public ImageHDU(Object obj)
	throws FitsException
  {
    super(new Header());

    myData = new ImageData(obj);
    myHeader.pointToData(myData);

    if (!myHeader.primaryToImage()) {
      throw new FitsException("Default header was not converted to a valid image header");
    }
  }

  /** Check that this is a valid image extension header.
    * @param header to validate.
    * @return <CODE>true</CODE> if this is an image extension header.
    */
  public static boolean isHeader(Header header)
  {
    String card0 = header.getCard(0);
    return (card0 != null && card0.startsWith("XTENSION= 'IMAGE   '"));
  }

  /** Check that this HDU has a valid header.
    * @return <CODE>true</CODE> if this HDU has a valid header.
    */
  public boolean isHeader()
  {
    return isHeader(myHeader);
  }

  /** Create a Data object to correspond to the header description.
    * @return An unfilled Data object which can be used to read
    *         in the data for this HDU.
    * @exception FitsException if the image extension could not be created.
    */
  public Data manufactureData()
	throws FitsException
  {
    return new ImageData(myHeader);
  }

  /** Print out some information about this HDU.
    */
  public void info() {
    if (isHeader()) {
      System.out.println("  Image Extension");
    } else {
      System.out.println("  Image Extension (bad header)");
    }

    System.out.println("      Header Information:");
    System.out.println("         BITPIX="+myHeader.getIntValue("BITPIX",-1));
    int naxis = myHeader.getIntValue("NAXIS", -1);
    System.out.println("         NAXIS="+naxis);
    for (int i=1; i<=naxis; i += 1) {
      System.out.println("         NAXIS"+i+"="+
			 myHeader.getIntValue("NAXIS"+i,-1));
    }

    System.out.println("      Data information:");
    if (myData.getData() == null) {
      System.out.println("        No Data");
    } else {
      System.out.println("         "+
			 ArrayFuncs.arrayDescription(myData.getData()));
    }
  }
}
