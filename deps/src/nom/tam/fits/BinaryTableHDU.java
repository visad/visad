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


import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;

/** FITS binary table header/data unit */
public class BinaryTableHDU
      extends TableHDU
{
  /** Create a binary table header/data unit.
    * @param header the template specifying the binary table.
    * @exception FitsException if there was a problem with the header.
    */

  public BinaryTableHDU(Header header)
	throws FitsException
  {
    super(header);
    if (!isHeader()) {
      throw new BadHeaderException("Not a valid binary table header");
    }
  }

  /** Build a binary table HDU from the supplied data.
    * @param table the array used to build the binary table.
    * @exception FitsException if there was a problem with the data.
    */
  public BinaryTableHDU(Object[][] table)
	throws FitsException
  {
    super(null);


    if (table == null) {
      myData = new BinaryTable();
    } else {
      myData = new BinaryTable(table);
    }
    myHeader = BinaryTableHeaderParser.pointToTable((BinaryTable)myData);

    setColumnStrings();

  }

  /** Build an empty binary table HDU.
    * @exception FitsException if there was a problem building the empty HDU.
    */
  public BinaryTableHDU()
	throws FitsException
  {
    this((Object[][] )null);
  }

  /** Check that this is a valid binary table header.
    * @param header to validate.
    * @return <CODE>true</CODE> if this is a binary table header.
    */
  public static boolean isHeader(Header header)
  {
    String card0 = header.getCard(0);

    // Note that characters after the first 8 aren't significant.
    // BINTABLE may be followed by one or more blanks.
    return (card0 != null && card0.startsWith("XTENSION= 'BINTABLE'"));
  }

  /** Check that this HDU has a valid header.
    * @return <CODE>true</CODE> if this HDU has a valid header.
    */
  public boolean isHeader()
  {
    return isHeader(myHeader);
  }

  /** Set the default base keys which are expected to be associated
    * with a binary table.
    * The user can add to these by calling addColumnString directly.
    */
  protected void setColumnStrings() {
    addColumnString("TTYPE");
    addColumnString("TFORM");
    addColumnString("TDIM");
    addColumnString("TSCAL");
    addColumnString("TZERO");
  }

  /** Add a column without any associated header information.
    *
    * @param data The column data to be added.  Data should be an Object[] where
    *             type of all of the constituents is identical.  The length
    *             of data should match the other columns.  <b> Note:</b> It is
    *             valid for data to be a 2 or higher dimensionality primitive
    *             array.  In this case the column index is the first (in Java speak)
    *             index of the array.  E.g., if called with int[30][20][10], the
    *             number of rows in the table should be 30 and this column
    *             will have elements which are 2-d integer arrays with TDIM = (10,20).
    * @exception FitsException the column could not be added.
    */
  public void addColumn(Object[] data)
	throws FitsException
  {
    BinaryTable myData = (BinaryTable) this.myData;
    myData.addColumn(data);

    // Make sure we can point to an appropriate place in the Header.

    int ncol = myData.getNcol();

    if (ncol > 1) {
        int lastMark = -2;
        for (int j=0; j<columnStrings.size(); j += 1) {
            String key = (String)columnStrings.elementAt(j) + (ncol-1);
            myHeader.findKey(key);
            if (myHeader.getMark() > lastMark) {
               lastMark = myHeader.getMark();
            }
        }
        myHeader.setMark(lastMark);
    } else {
        myHeader.findKey("TFIELDS");
        int lastMark=myHeader.getMark();
        int j=1;

        while (true) {
             String card = myHeader.getCard(lastMark+j);

             if (card == null) {
                 myHeader.unsetMark();
                 break;
             } else if ( !(card.substring(0,8).equals("COMMENT ") ||
                           card.substring(0,8).equals("        "))) {
                 myHeader.setMark(lastMark+j);
                 break;
             }
             j += 1;
        }
    }


    BinaryTableHeaderParser.addColumn(ncol-1, data, myHeader);
  }

  /** Find the column which has the given name (i.e., TTYPE)
    * @param name The desired name.
    * @return The Fits index of the column (first column = 1);
    */
  public int findColumn(String name)
  {
    for (int i=1; i <= myHeader.getIntValue("TFIELDS", 0); i += 1) {
      String tform = myHeader.getStringValue("TTYPE"+i);
      if (tform != null && tform.equals(name)) {
	return i-1;
      }
    }

    return -1;
  }


  /** Get the header and data information for a given column.
    * @param name The name (TTYPE) of  column desired.
    * @return A Column object with the desired information or
    *         null if the column could not be found.
    * @exception FitsException if <CODE>colNumber</CODE> could not be deleted.
    */
  public Column getColumn(String name)
	throws FitsException
  {
    int col = findColumn(name);
    if (col < 0) {
      return null;
    }

    return getColumn(col);
  }

  /** Get the header and data associated with the given column.
    * @param colNumber The Fits (first=1) index of the desired column.
    * @return The associated information.
    * @exception FitsException if <CODE>colNumber</CODE> could not be found.
    */
  public Column getColumn(int colNumber)
	throws FitsException
  {

    Column thisCol = new Column();
    Object[] col = (Object[])((BinaryTable)myData).getColumn(colNumber);
    thisCol.setData(col);

    for (int i=0; i<columnStrings.size(); i += 1) {
      String card = myHeader.findKey((String)columnStrings.elementAt(i)+colNumber);
      if (card != null) {
	thisCol.addKey(card);
      }
    }
    return thisCol;
  }


  /** Add a column to tabular data.
    * @param col The column to be added.  It should have the same
    *          dimension as all of the other columns.
    * @exception FitsException if <CODE>col</CODE> could not be added.
    */
  public void addColumn(Column col)
	throws FitsException
  {
    BinaryTable myData = (BinaryTable) this.myData;

    addColumn(col.getData());
    int ncol = myData.getNcol();

    String[] keys = col.getKeys(ncol);

    // Now add the pointers that were stored in this column.
    // Set the mark to the TFORMn keyword before we start.

    // Note that this will override the TFORM value for
    // the variable length column we had.

    myHeader.deleteKey("TDIMS"+ncol);
    myHeader.getStringValue("TFORM"+ncol);

    for (int i=0; i<keys.length; i += 1) {
      if (keys[i].substring(0,5).equals("TFORM") ) {
          HeaderCard card = new HeaderCard(keys[i]);
          myHeader.addStringValue(card.getKey(), card.getValue(), card.getComment());
      } else {
          myHeader.addLine(keys[i]);
      }
    }
  }

  /** Create a variable column from the supplied data.
    * @param data The column of data to be added.  It should have the same
    *          dimension as all of the other columns.
    * @exception FitsException if column could not be added.
    */
  public Column makeVarColumn(Object [] data)
	throws FitsException
  {
    return makeVarColumn(data, null, null);
  }

  /** Create a variable column from the supplied data.
    * @param data The column of data to be added.  It should have the same
    *          dimension as all of the other columns.
    * @param type The Fits type for this column (S, L, B, etc.)
    * @exception FitsException if column could not be added.
    */
  public Column makeVarColumn(Object[] data, String type)
	throws FitsException
  {
    return makeVarColumn(data, type, null);
  }

  /** Create a variable column from the supplied data.
    * @param data The column of data to be added.  It should have the same
    *          dimension as all of the other columns.
    * @param type The Fits type for this column (S, L, B, etc.)
    * @param keys The list of keys for this column (may be null).
    * @exception FitsException if column could not be added.
    */
  public Column makeVarColumn(Object[] data, String type, String [] keys)
	throws FitsException
  {
    Class baseClass = ArrayFuncs.getBaseClass(data);
    char classChar;

    // Byte type data can have several kinds of data encoded.
    if (baseClass == Byte.TYPE) {
      if (type != null && type.equals("S")) {
	classChar = 'S';
      } else if (type != null && type.equals("L")) {
	classChar = 'L';
      } else {
	classChar = 'B';
      }
    } else if (baseClass == Integer.TYPE) {
      classChar = 'J';
    } else if (baseClass == Short.TYPE) {
      classChar = 'I';
    } else if (baseClass == Float.TYPE) {
      if (type != null && type.equals("C")) {
	classChar = 'C';
      } else {
	classChar = 'E';
      }
    } else if (baseClass == Double.TYPE) {
      if (type != null && type.equals("M")) {
	classChar = 'M';
      } else {
	classChar = 'D';
      }
    } else {
      throw new FitsException("Invalid Base class for variable column");
    }

    Column varColumn = ((BinaryTable)myData).addVarData(data);
    varColumn.addKey(myHeader.formatFields(
      "TFORM", "'1P"+classChar+"     '", "VariableLength Column"));


    // Adjust the PCOUNT variable to indicate the existence of a heap.
    myHeader.addIntValue("PCOUNT", ((BinaryTable)myData).getHeapSize(), "Size of Heap Area");

    if (keys != null) {
      for (int i=0; i<keys.length; i += 1) {
        varColumn.addKey(keys[i]);
      }
    }

    return varColumn;
  }

  /** Return a variable column.
    * @param name The name of the column to fetch.
    * @return either null if <CODE>name</CODE> was not found, or an
    *		array of data (as an Object).
    * @exception FitsException if the column could not be found or returned.
    */
  public Object getVarData(String name)
	throws FitsException
  {
    int colNum = findColumn(name);
    if (colNum < 0) {
      return null;
    }

    return getVarData(colNum);
  }

  /** Return a variable column.
    * @param col The column number to fetch.
    * @return an array of data (as an Object).
    * @exception FitsException if <CODE>col</CODE> was not a valid column
    *				number, was not a variable column, or had
    *				an invalid Fits type.
    */
  public Object getVarData(int col)
	throws FitsException
  {
    String tform = myHeader.getStringValue("TFORM"+(col+1));
    if (tform == null)  {
      throw new FitsException("TFORM not found for column(0 indexed):"+col);
    }

    char  typeChar;
    Class baseClass;

    if (tform.substring(0,2).equals("1P") ) {
      typeChar = tform.charAt(2);
    } else if (tform.charAt(0) == 'P') {
      typeChar = tform.charAt(1);
    } else {
      throw new FitsException("Requested column does not seem to be variable: TFORM="+tform);
    }

    boolean complex = false;
    switch (typeChar) {
    case 'L':
    case 'B':
    case 'S':
      baseClass = Byte.TYPE;
      break;
    case 'I':
      baseClass = Short.TYPE;
      break;
    case 'J':
      baseClass = Integer.TYPE;
      break;
    case 'K':
      baseClass = Long.TYPE;
      break;
    case 'E':
      baseClass = Float.TYPE;
      break;
    case 'D':
      baseClass = Double.TYPE;
      break;
    case 'C':
      complex = true;
      baseClass = Float.TYPE;
      break;
    case 'M':
      complex = true;
      baseClass = Double.TYPE;
      break;
    default:
      throw new FitsException("Unable to understand variable column format:"+tform);
    }

    return ((BinaryTable)myData).getVarData(col, baseClass, complex);
  }

  /** Create a Data object to correspond to the header description.
    * @return An unfilled Data object which can be used to read
    *         in the data for this HDU.
    * @exception FitsException if the binary table could not be created.
    */
  public Data manufactureData()
	throws FitsException
  {
    setColumnStrings();
    return new BinaryTable(myHeader);
  }

  /** Get the number of columns for this table
    * @return The number of columns in the table.
    */
  public int getNumColumns()
  {
    return myHeader.getIntValue("TFIELDS", 0);
  }

  /** Get the number of rows for this table
    * @return The number of rows in the table.
    */
  public int getNumRows()
  {
    return myHeader.getIntValue("NAXIS2", 0);
  }

  /** Get the name of a column in the table.
    * @return The column name.
    * @exception FitsException if an invalid index was requested.
    */
  public String getColumnName(int index)
	throws FitsException
  {
    int flds = myHeader.getIntValue("TFIELDS", 0);
    if (index < 0 || index >= flds) {
      throw new FitsException("Bad column index " + index + " (only " + flds +
			      " columns)");
    }

    return getTrimmedString("TTYPE" + (index + 1));
  }

  /** Get the FITS type of a column in the table.
    * @return The FITS type.
    * @exception FitsException if an invalid index was requested.
    */
  public String getColumnFITSType(int index)
	throws FitsException
  {
    int flds = myHeader.getIntValue("TFIELDS", 0);
    if (index < 0 || index >= flds) {
      throw new FitsException("Bad column index " + index + " (only " + flds +
			      " columns)");
    }

    return getTrimmedString("TFORM" + (index + 1));
  }

  /** Print out some information about this HDU.
    */
  public void info() {

    BinaryTable myData = (BinaryTable) this.myData;

    System.out.println("  Binary Table");
    System.out.println("      Header Information:");

    int nhcol = myHeader.getIntValue("TFIELDS", -1);
    int nrow = myHeader.getIntValue("NAXIS2", -1);
    int rowsize = myHeader.getIntValue("NAXIS1", -1);
    System.out.print("          "+nhcol+" fields");
    System.out.println(", "+nrow+" rows of length "+rowsize);
    for (int i=1; i <= nhcol; i += 1) {
      System.out.print("           "+i+":");
      checkField("TTYPE"+i);
      checkField("TFORM"+i);
      checkField("TDIM"+i);
      System.out.println(" ");
    }

    System.out.println("      Data Information:");
    if (myData == null ||
        myData.getNrow() == 0 || myData.getNcol() == 0) {
        System.out.println("         No data present");
        if (myData.getHeapSize() > 0) {
            System.out.println("         Heap size is: "+myData.getHeapSize()+" bytes");
        }
    } else {

      System.out.println("         Number of rows="+myData.getNrow());
      System.out.println("         Number of columns="+myData.getNcol());
      if (myData.getHeapSize() > 0) {
          System.out.println("         Heap size is: "+myData.getHeapSize()+" bytes");
      }

      int[][] dimens = myData.getDimens();
      char[]  types = myData.getTypes();

      for (int i=0; i<myData.getNcol(); i += 1) {
	  System.out.print("         "+(i+1)+
			   ":"+types[i] + " [");
          char comma= ' ';
          for (int dim=0; dim < dimens[i].length; dim += 1) {
               System.out.print(""+comma+dimens[i][dim]);
               comma = ',';
          }
          System.out.println(" ]");
      }
    }
  }
}
