package nom.tam.fits;

import java.io.IOException;
import nom.tam.util.BufferedDataInputStream;

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

/** FITS ASCII table header/data unit
  * ASCII tables are not currently suppoted.
  */
public class AsciiTableHDU
	extends TableHDU
{
 /** Create an ascii table header/data unit.
   * @param header the template specifying the ascii table.
   * @exception FitsException if there was a problem with the header.
   */
   public AsciiTableHDU(Header header)
  	throws FitsException
    {
      super(header);
      if (!isHeader()) {
        throw new FitsException("Not a valid ascii table header");
      }
    }


  /** Check that this is a valid ascii table header.
    * @param header to validate.
    * @return <CODE>true</CODE> if this is an ascii table header.
    */
  public static boolean isHeader(Header header)
  {
    String card0 = header.getCard(0);
    return (card0 != null && card0.startsWith("XTENSION= 'TABLE   '"));
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
      * @exception FitsException if the Data object could not be created
      *				from this HDU's Header
      */
    Data manufactureData() throws FitsException {
      throw new FitsException("ASCII tables are currently not supported");
    }

    /** Skip the ASCII table and throw an exception.
      * @param stream the stream from which the data is read.
      * @return nothing since an exception is always thrown.
      * @exception FitsException because ASCII tables are not yet supported.
      */
    public void readData(BufferedDataInputStream stream)
  	throws FitsException
    {
      try {
        skipData(stream);
      } catch (IOException e) {
     }

      throw new FitsException("ASCII tables are currently not supported");
    }

    public void info() {
      System.out.println("ASCII Table: unimplemented");
    }
}

