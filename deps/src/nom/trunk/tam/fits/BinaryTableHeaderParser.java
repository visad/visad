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

import nom.tam.util.ArrayFuncs;

/** This class defines the methods for accessing FITS binary table header
  * information.
  */

public class BinaryTableHeaderParser {

    Header	myHeader;

    /** Parse an existing header.
      * @param The existing header -- likely read from a file.
      */
    public BinaryTableHeaderParser(Header myHeader) throws FitsException {

	int naxis1 = myHeader.getIntValue ("NAXIS1", 0);
      checkLT0(naxis1, "NAXIS1 < 0 for binary table");

	int naxis2 = myHeader.getIntValue ("NAXIS2", 0);
      checkLT0(naxis2, "NAXIS2 < 0 for binary table");

	int nfields =    myHeader.getIntValue("TFIELDS",0);
      checkLT0(nfields, "NFIELDS < 0 for binary table");

      this.myHeader = myHeader;
    }

    /** Create a model row for a binary table given a
      * describing header.
      * @return A model row for the table.
      */
    public Object[] getModelRow() throws FitsException {

      int nfields = myHeader.getIntValue("TFIELDS");
      Object[] row = new Object[nfields];

	for (int i=0; i < nfields; i += 1) {
	    Object column = getColumnDef(i+1);
	    if (column == null) {
		throw new FitsException("Invalid TFORM for column "+(i+1));
	    } else {
		row[i] = column;
	    }
	}
      return row;
    }



    /** Check if a value is less than 0 and throw an error if so.
      * @param i The value to be checked.
      * @param errmst The message to be associated with the FitsException thrown.
      */
    protected void checkLT0(int i, String errmsg) throws FitsException {
        if (i < 0) {
            throw new FitsException(errmsg);
        }
    }

    /** Get the format for a given column
      * @param col      the column being examined.
      * @return         an object of the type described in the column
      *                 header info.  Note that only the TFORM and TDIM keywords
      *                 are examined.
      */
    protected Object getColumnDef(int col) throws FitsException{

	int i;
	Class baseType;
	int arrsiz;

	String format = myHeader.getStringValue("TFORM"+col);

	if (format == null) {
	    throw new FitsException("No TFORM for column "+col);
	}

	// Skip initial white space
	for (i=0; i<format.length(); i += 1) {
	    if (!Character.isSpaceChar(format.charAt(i))){
	        break;
	    }
	}
	// Skip numbers
	for ( ;i<format.length(); i += 1) {
	    if (!Character.isDigit(format.charAt(i))) {
		break;
	    }
	}

      boolean complex = false;
      boolean bitData = false;
      boolean varData = false;
	if (i >= format.length() ) {
          throw new FitsException("Invalid TFORM value for column "+col);
      }

      if (i > 0) {
	    arrsiz = Integer.parseInt(format.substring(0,i));
	} else {
          arrsiz = 1;
	}

	switch (format.charAt(i)){
	     case 'X':
		baseType = Byte.TYPE;
		bitData = true;
		break;
	     case 'B':
	     case 'A':
	     case 'L':
		baseType = Byte.TYPE;
		break;
	     case 'I':
		baseType = Short.TYPE;
		break;
	     case 'J':
		baseType = Integer.TYPE;
		break;
           case 'K':
            baseType = Long.TYPE;
            break;
	     case 'E':
		baseType = Float.TYPE;
		break;
	     case 'D':
		baseType = Double.TYPE;
		break;
	     case 'C':
		baseType = Float.TYPE;
		complex = true;
		break;
	     case 'M':
		baseType = Double.TYPE;
		complex = true;
		break;
	     case 'P':
		baseType = Integer.TYPE;
            varData = true;
     		if (arrsiz > 0) {
		    arrsiz = 2;
		} else {
		    arrsiz = 0;
		}
            break;
	     default:
		throw new FitsException("Invalid TFORM code '"+format.charAt(i)+"' for column "+col);
	}

      String tdims = myHeader.getStringValue("TDIM"+col);
      int[] dims;

      if (tdims != null && !varData && !bitData) {
          dims = getTDims(tdims, arrsiz);
      } else {
          if (bitData) arrsiz /= 8;
          dims = new int[1];
          dims[0] = arrsiz;
      }

      // Add in a dimension for complex data.
      if (complex) {
          int[] ndims = new int[dims.length+1];
          ndims[0] = 2;
          for (i=1; i<=dims.length; i += 1) {
              ndims[i] = dims[i-1];
          }
          dims = ndims;
      }

      try {
          return java.lang.reflect.Array.newInstance(baseType, dims);
      } catch (IllegalArgumentException e) {
          throw new FitsException("Invalid datatype");
      } catch (NegativeArraySizeException e) {
          throw new FitsException("Negative dimensions");
      }
    }

    /** Parse the TDIMS value.
      *
      * If the TDIMS value cannot be deciphered a one-d
      * array with the size given in arrsiz is returned.
      *
      * @param tdims   The value of the TDIMSn card.
      * @param arrsiz  The size field found on the TFORMn card.
      * @return        An int array of the desired dimensions.
      *                Note that the order of the tdims is the inverser
      *                of the order in the TDIMS key.
      */
    public static int[] getTDims(String tdims, int arrsiz) {

        // The TDIMS value should be of the form: "(iiii,jjjj,kkk,...)"

        int[] backup = {arrsiz};

        // Count the commas in the tdims field.
        int ncomma = 0;
        for (int i=0; i<tdims.length(); i += 1) {
             if (tdims.charAt(i) == ',') {
                  ncomma += 1;
             }
        }

        int[] dims = new int[ncomma+1];

        int starter = tdims.indexOf('(') + 1;
        if (starter < 0) {
            return backup;
        }

        int ender;
        for (int i=0; i < ncomma; i += 1) {
            ender= tdims.indexOf(',', starter);
            if (ender < 0) {
                return backup;
            }
            dims[i] = Integer.parseInt(tdims.substring(starter,ender));
            starter = ender + 1;
        }

        ender = tdims.indexOf(')', starter);
        if (ender < 0) {
            return backup;
        }

        dims[ncomma] = Integer.parseInt(tdims.substring(starter,ender));

        // Now invert the order of the tdims.

        int[] newdims = new int[dims.length];
        for (int i=0; i<dims.length; i += 1) {
            newdims[i] = dims[dims.length-i-1];
        }

        return newdims;
    }

     /** Make the header describe the a table where we give only.
       * a single row of the table and the number of rows.
       *
       * @exception FitsException if the table was not valid.
       */
     public static Header pointToTable(BinaryTable table) throws FitsException {

         if (table == null) {
             throw new FitsException("Cannot create header for null table");
         }

         Header myHeader = new Header();
         return pointToTable(table, myHeader);
    }


    /** Make the header describe a specified table and included
      * existing header information.
      * @param table    The binary table data.
      * @param myHeader An existing header for this data.  It will be modified
      *                 as needed, but excess keywords will not be pruned.
      */
    public static Header pointToTable(BinaryTable table, Header myHeader)
                         throws FitsException {

         myHeader.setXtension("BINTABLE");
         myHeader.setBitpix(8);
         myHeader.setNaxes(2);

         myHeader.setNaxis(1, 0);     // This is just a place holder
         myHeader.setNaxis(2, table.getNrow());

         myHeader.setPcount(0);
         myHeader.setGcount(1);

         int[][] dimens = table.getDimens();
         int[] sizes = table.getSizes();
         char[] types = table.getTypes();

         myHeader.addIntValue("TFIELDS", dimens.length, "Number of fields in table");

         int mark = myHeader.getMark();

         String card = myHeader.getCard(mark);

         if (card != null && (card.substring(0,8).equals("COMMENT ") ||
                              card.substring(0,8).equals("        ") )) {
             while(card.substring(0,8).equals("COMMENT ") ||
                   card.substring(0,8).equals("        ")) {
                  mark += 1;
                  card = myHeader.getCard(mark);
             }
         } else {

	     myHeader.insertCommentStyle("","");
             myHeader.insertComment("End of required structural keywords");
             myHeader.insertCommentStyle("","");
         }

         int rowsize = 0;
         for (int col=0; col < dimens.length; col += 1) {
             pointToCol(myHeader, col, sizes[col], dimens[col], types[col]);
             int colsiz = sizes[col];
             if (types[col] == 'S') {
                 colsiz *= 2;
             } else if (types[col] == 'I' ||
                types[col] == 'F') {
                 colsiz *= 4;
             } else if (types[col] == 'L'  ||
                types[col] == 'D') {
                 colsiz *= 8;
             }
             rowsize += colsiz;
         }

         // Overwrite previous values.
         myHeader.addIntValue("NAXIS1", rowsize, "Number of bytes in row");
         return myHeader;
     }

     /** Add a column to the header information.
       *
       * @param column   The column index for the new column.
       * @param col      The column data.
       * @param myHeader The existing header for the table.
       */
     public static void addColumn(int column, Object[] col, Header myHeader) throws FitsException {

         int size;
         int[] dimens = ArrayFuncs.getDimensions(col[0]);
         if (dimens.length == 0) {
             size = 1;
             dimens = new int[1];
             dimens[0] = 1;
         } else {
             size = 1;
             for (int i=0; i<dimens.length; i += 1) {
                 size *= dimens[i];
             }
         }

         char type;
         int bsize;
         Class base = ArrayFuncs.getBaseClass(col[0]);
         if (base == Boolean.TYPE) {
             bsize = 1;
             type = 'Z';
         } else if (base == Byte.TYPE) {
             bsize = 1;
             type = 'B';
         } else if (base == Short.TYPE || base == Character.TYPE) {
             type = 'S';
             bsize = 2;
         } else if (base == Integer.TYPE) {
             type = 'I';
             bsize=4;
         } else if (base == Long.TYPE) {
             type = 'J';
             bsize=8;
         } else if (base == Float.TYPE) {
             type = 'F';
             bsize=4;
         } else if (base == Double.TYPE) {
             type = 'D';
             bsize = 8;
         } else {
             throw new FitsException("Invalid Column type");
         }

         pointToCol(myHeader, column, size, dimens, type);
         myHeader.addIntValue("TFIELDS", myHeader.getIntValue("TFIELDS")+1, "Number of columns");
         myHeader.addIntValue("NAXIS1", myHeader.getIntValue("NAXIS1")+bsize*size, "Bytes per row");
         if (column == 0) {
             myHeader.addIntValue("NAXIS2", col.length, "Number of rows");
         }
    }



    /** Add information in the header to describe a single column.
      *
      * @param myHeader The header to be updated.
      * @param col      The column index.
      * @param size     The number of elements in the column per row
      * @param dimens   The dimensions of the column.
      * @param type     A character indicating the type of data.
      */
    static void pointToCol(Header myHeader, int col, int size,
                   int[] dimens, char type)
                   throws FitsException {

         char desc;

         switch(type) {
           case 'Z':
               desc = 'L';
               break;
           case 'B':
               desc = 'B';
               break;
           case 'I':
               desc = 'J';
               break;
           case 'J':
               desc = 'K';
               break;
           case 'S':
               desc = 'I';
               break;
           case 'F':
               desc = 'E';
               break;
           case 'D':
               desc = 'D';
               break;
           default:
               throw new FitsException("Invalid data type at column:"+col);
         }

         StringBuffer tdim = new StringBuffer("(");
         for (int i=0; i < dimens.length; i += 1) {
             int dim = dimens[dimens.length-i-1];
             if (i > 0) {
                 tdim.append(",");
             }
             tdim.append(dim);
         }
         tdim.append(")");

         // Don't overwrite a variable length column...

         if (size == 2 &&  desc == 'J') {
             String colTform = myHeader.getStringValue("TFORM"+(col+1));
             if (colTform != null &&
                ( (colTform.length() > 1 && colTform.charAt(0) == 'P') ||
                  (colTform.length() > 2 && colTform.substring(0,2).equals("1P") ) ) ){
                 return;
             }
         }

         myHeader.addStringValue("TFORM"+(col+1), ""+size+desc, null);
         myHeader.addStringValue("TDIM"+(col+1),  new String(tdim), null);

    }
}
