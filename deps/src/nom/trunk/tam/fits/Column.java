package nom.tam.fits;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 *
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */

/** Keep header and data information for a column from a FITS table.
  */
public class Column {

    private Object[] columnData=null;
    private java.util.Vector headerKeys = new java.util.Vector(5);


    /** Create an empty column */
    public Column() {}

    /** Initialize the data segment.  Each element of data should be an N-dimensional
      * primitive array.
      * @param data The column data.
      */
    public void setData(Object[] data) {
         columnData = data;
    }

    /** Return the data.
      */
    public Object[] getData() {
         return columnData;
    }

    /** Set the FITS keywords associated with this column.
      * These may have just the base of the keyword (e.g, "TFORM"), or
      * have a column number appended.  The correct column number
      * will be put in by getKeys when the header information is retrieved.
      */
    public void setKeys(String[] keys) {
        for (int i=0; i<keys.length; i += 1) {
            addKey(keys[i]);
        }
    }

    /** Get the keywords associated with the column.
      *
      * @param colNumber the FITS column number that will be associated
      *                  with this column.
      */
    public String[] getKeys (int colNumber) {

         if (headerKeys.size() <= 0) {
             return null;
         }
         String[] keys = new String[headerKeys.size()];

         for(int i=0; i<headerKeys.size(); i += 1) {
              String card = (String) headerKeys.elementAt(i);
              StringBuffer newKey = new StringBuffer();

              for (int j=0; j<8; j += 1) {

                  char c = card.charAt(j);
                  if (!Character.isDigit(c) && c != ' ') {
                      newKey.append(card.charAt(j));
                  } else {
                      break;
                  }
              }
              newKey.append(colNumber);
              newKey.append("        ");
              keys[i] = newKey.toString().substring(0,8) + card.substring(8);
         }

         return keys;
    }

    /** Add a key to the keys associated with this column.
      * @param key The new key.
      */
    public void addKey(String key) {
         headerKeys.addElement(key);
    }

}
