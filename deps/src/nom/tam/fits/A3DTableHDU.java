package nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */


import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;

/** FITS binary table header/data unit */
public class A3DTableHDU
      extends BinaryTableHDU
{
  /** Create a binary table header/data unit.
    * @param header the template specifying the binary table.
    * @exception FitsException if there was a problem with the header.
    * @deprecated
    */
  public A3DTableHDU(Header header)
	throws FitsException
  {
    super(header);
    if (!isHeader()) {
      throw new FitsException("Not a valid A3D table header");
    }
  }

  /** Check that this is a valid binary table header.
    * @param header to validate.
    * @return <CODE>true</CODE> if this is a binary table header.
    * @deprecated
    */
  public static boolean isHeader(Header header)
  {
    String card0 = header.getCard(0);
    return (card0 != null && card0.startsWith("XTENSION= 'A3DTABLE'"));
  }

  /** Check that this HDU has a valid header.
    * @return <CODE>true</CODE> if this HDU has a valid header.
    */
  public boolean isHeader()
  {
    return isHeader(myHeader);
  }
}
