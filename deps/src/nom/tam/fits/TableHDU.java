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

import java.util.Vector;

import nom.tam.util.BufferedDataInputStream;

/** Generic FITS table methods */
public abstract class TableHDU
	extends ExtensionHDU
{
  /** An array containing the base keys that a user may anticipate
    * being associated with a given column.  This array is used
    * to find and extract keys that belong to a given column.
    */
  Vector columnStrings = new Vector();

  /** Build a table from the specified FITS header.
    * @param header to use as a template.
    * @exception FitsException if the header was not valid.
    */
  public TableHDU(Header header)
	throws FitsException
  {
    super(header);
  }

  /** Add a base keyword to the keys to be looked for
    * in describing a column.  E.g., if a user wishes to
    * make sure the TLMINn keywords are associated with columns
    * he/she might call addColumnString("TLMIN");
    * This does not mean that the software will generate these
    * columns, only that they will be seen as associated
    * with the appropriate table data.
    */
  public void addColumnString(String keyBase) {
    columnStrings.addElement(keyBase);
  }

  /** Ensure that keywords for the current column are placed after
    * the keywords for the last column.
    * @param The minimum index to be used.
    * @param The column that is being added (so we are looking for the keywords
    *        from the previous column.
    */
  void setLastMark (int lastMark, int colNumber) {

    for (int i=0; i<columnStrings.size(); i += 1) {
      myHeader.findKey((String)columnStrings.elementAt(i) + (colNumber-1));
      if (myHeader.markSet() && myHeader.getMark() > lastMark) {
	lastMark = myHeader.getMark();
      }
    }

    myHeader.setMark(lastMark);
  }
}
