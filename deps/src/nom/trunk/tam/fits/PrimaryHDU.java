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

/** FITS primary array header/data unit */
public class PrimaryHDU
	extends BasicHDU
{
  /** Create a primary array unit.
    * @param header the template specifying the primary array.
    * @exception FitsException if there was a problem with the header.
    */
  public PrimaryHDU(Header header)
	throws FitsException
  {
    super(header);
    if (!isHeader()) {
      throw new BadHeaderException("Not a valid primary header or uses random groups");
    }
  }

  /** Build a primary HDU from an image HDU.
    * @param img the ImageHDU containing the data.
    * @exception FitsException if there was a problem with the data.
    */
  public PrimaryHDU(ImageHDU img)
	throws FitsException
  {

    super(img.myHeader);

    if (myHeader != null) {
      if (!myHeader.imageToPrimary()) {
	throw new FitsException("Couldn't create PrimaryHDU from ImageHDU");
      }
    }

    myData = img.myData;
  }

  /** Build an empty primary HDU.
    * @exception FitsException if there was a problem creating the HDU.
    */
  public PrimaryHDU()
	throws FitsException
  {
    super(new Header());

    myData = new ImageData();
    myHeader.pointToData(myData);
  }

  /** Build a primary HDU using the supplied data.
    * @param obj the data used to build the primary HDU.
    * @exception FitsException if there was a problem with the data.
    */
  public PrimaryHDU(Object obj)
	throws FitsException
  {
    super(new Header());

    myData = new ImageData(obj);
    myHeader.pointToData(myData);
  }

  /** Check that this is a valid primary/non-random groups header.
    * @param header to validate.
    * @return <CODE>true</CODE> if this is a simple primary header.
    */
  public static boolean isHeader(Header header)
  {
    // We don't handle random-groups format.
    if (header.getBooleanValue("GROUPS")) {
	if (header.getIntValue("GCOUNT",1) > 1 ||
	    header.getIntValue("PCOUNT",0) != 0)
	{
	    return false;
	}
    }

    String card0 = header.getCard(0);
    return (card0 != null && card0.startsWith("SIMPLE  "));
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
    * @exception FitsException if the image data could not be created.
    */
  public Data manufactureData()
	throws FitsException
  {
    return new ImageData(myHeader);
  }

  /** Print out some information about this HDU.
    */
  public void info() {
    System.out.println("  Primary Image");

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
