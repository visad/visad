package nom.tam.fits;


 /*
  * Copyright: Thomas McGlynn 1997-1998.
  * This code may be used for any purpose, non-commercial
  * or commercial so long as this copyright notice is retained
  * in the source code or included in or referred to in any
  * derived software.
  * Many thanks to David Glowacki (U. Wisconsin) for substantial
  * improvements, enhancements and bug fixes.
  */


/** Generic FITS extension methods */
public abstract class ExtensionHDU
	extends BasicHDU
{
  /** Create an HDU with the specified Header and an empty Data section
    * @param header the Header
    */
  public ExtensionHDU(Header header)
  {
    super(header);
  }

  public String getExtensionType()
	throws FitsException
  {
    String xStr = myHeader.getStringValue("XTENSION");
    if (xStr == null) {
      throw new FitsException("Missing EXTENDed FITS file type");
    }

    xStr = xStr.trim();
    if (xStr.length() < 1) {
      throw new FitsException("Empty EXTENDed FITS file type");
    }

    return xStr;
  }

  public String getExtensionName()
  {
    return getTrimmedString("EXTNAME");
  }

  public int getExtensionVersion()
  {
    return myHeader.getIntValue("EXTVER", 1);
  }

  public int getExtensionLevel()
  {
    return myHeader.getIntValue("EXTLEVEL", 1);
  }
}
