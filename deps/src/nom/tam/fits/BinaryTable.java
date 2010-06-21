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

import java.io.*;
import nom.tam.util.*;
import java.lang.reflect.Array;
import java.util.Vector;

/** This class defines the methods for accessing FITS binary table data.
  */

public class BinaryTable extends Data {

    /** This is the area in which variable length column data lives.
      */
    byte[] hashArea = new byte[0];

    /** Pointer to the next available byte in the hashArea.
      */
    int hashPtr = 0;

    /** The columns of data in the binary table.
      */
    Vector columns;

    /** The sizes of each column (in number of entries per row)
      */
    int[] sizes;

    /** The dimensions of each column.
      */
    int[][] dimens;

    /** An example of the structure of a row
      */
    Object[] modelRow;

    /** Where the data is actually stored.
      */
    ColumnTable table;

    /** Create a null binary table data segment.
      */
    public BinaryTable() throws FitsException {
         columns = new Vector();
         try {
             table = new ColumnTable(new Object[0], new int[0]);
         } catch (TableException e) {
             throw new FitsException("Unable to create table:"+e);
         }
         dataArray = table;
         dimens = new int[0][];
         sizes = new int[0];
         modelRow = new Object[0];
    }

    /** Create a binary table from given header information.
      *
      * @param header	A header describing what the binary
      *                 table should look like.
      */
    public BinaryTable(Header myHeader) throws FitsException {

      BinaryTableHeaderParser parser = new BinaryTableHeaderParser(myHeader);

      modelRow     = parser.getModelRow();
      int naxis2   = myHeader.getIntValue("NAXIS2");
      int nfields  = myHeader.getIntValue("TFIELDS");

      columns = new Vector(nfields);
      sizes   = new int[nfields];
      dimens  = new int[nfields][];

      modelRow = parser.getModelRow();

      int size = useModelRow(modelRow, naxis2);

      Object[] arrCol= new Object[nfields];
      columns.copyInto(arrCol);

      try {
          table = new ColumnTable(arrCol, sizes);
      } catch (TableException e) {
          throw new FitsException("Unable to create table:"+e);
      }


      int hsize = myHeader.trueDataSize();

      // If the computed size of the data array is less than the size that
      // the header indicates for the table, then there must be a hash
      // area for variable length data.

      if (size < hsize) {
          hashArea = new byte[hsize-size];
          hashPtr  = hashArea.length;

     } else if (size > hsize) {

          // This implies an error in the user's value of NAXIS1 (or a bug!)
          throw new FitsException("Size inconsistency in header and data: Check NAXIS1 and PCOUNT");
     }
    }

    /** Use an example row to determine what the table
      * should look like.
      * @param model An example of a row.  Each element of model should
      *              have the structure of the corresponding element of the row.
      * @param nrow  The number of rows in the table.
      * @return The size of the table in bytes.
      */
    protected int useModelRow(Object[] model, int nrow) {

      int totalsize = 0;

      for (int col=0; col < model.length; col += 1) {

           Class base = ArrayFuncs.getBaseClass(model[col]);
           dimens[col] = ArrayFuncs.getDimensions(model[col]);

           int size = 1;
           for (int dim=0; dim < dimens[col].length; dim += 1) {
               size *= dimens[col][dim];
           }
           sizes[col] = size;


           Object array = Array.newInstance(base, size*nrow);
           columns.addElement(array);

           totalsize += size*nrow*ArrayFuncs.getBaseLength(model[col]);
       }
       return totalsize;
    }

    /** Create a binary table from existing data in row order.
      *
      * @param data The data used to initialize the binary table.
      */
    public BinaryTable(Object[][] data) throws FitsException {

        modelRow = data[0];

        dimens = new int[modelRow.length][];
        sizes = new int[modelRow.length];
        columns = new Vector(modelRow.length);

        useModelRow(modelRow, data.length);
        hashPtr = 0;

        // We've set up the structure but now we need to go through and
        // move information from data to the arrays in
        // column.
        rowToColumn(data);
        Object[] ocols = new Object[columns.size()];
        columns.copyInto(ocols);
        try {
            table = new ColumnTable(ocols, sizes);
        } catch (TableException e) {
            throw new FitsException("Error creating ColumnTable");
        }
    }

    /** Convert the data from a row to a flattened column format.
      * @param data The table data in row/column format.
      */
    private void rowToColumn(Object[][] data) {

        for (int col=0; col<modelRow.length; col += 1) {

            Object column = columns.elementAt(col);
            for (int row=0; row<data.length; row += 1) {
                System.arraycopy(
                   ArrayFuncs.flatten(data[row][col]), 0,
                   column, row*sizes[col], sizes[col]
                );
            }
        }
    }

    /** Get a given row
      * @param row The index of the row to be returned.
      * @return A row of data.
      */
    public Object[] getRow(int row) throws FitsException {

        if (!validRow(row)) {
            throw new FitsException("Invalid row");
        }

        Object[] data = new Object[modelRow.length];
        for (int col=0; col<modelRow.length; col += 1) {
             data[col] = ArrayFuncs.curl(table.getElement(row,col), dimens[col]);
        }
        return data;
    }

    /** Replace a row in the table.
      * @param row  The index of the row to be replaced.
      * @param data The new values for the row.
      * @exception FitsException Thrown if the new row cannot
      *                          match the existing data.
      */
    public void setRow(int row, Object data[]) throws FitsException {


         if (data.length != getNcol()) {
             throw new FitsException("Updated row size does not agree with table");
         }

         Object[] ydata = new Object[data.length];

         for (int col=0; col<data.length; col += 1) {
              ydata[col] = ArrayFuncs.flatten(data[col]);
         }
         try {
             table.setRow(row, ydata);
         } catch (TableException e) {
             throw new FitsException("Error modifying table: "+e);
         }
     }

     /** Replace a column in the table.
       * @param col The index of the column to be replaced.
       * @param xcol The new data for the column
       * @exception FitsException Thrown if the data does not match
       *                          the current column description.
       */
     public void setColumn(int col, Object[] xcol) throws FitsException {


         int nrow = xcol.length;
         if (nrow != getNrow()) {
             throw new FitsException("Replacement column had wrong number of rows");
         }

         int[] dims = ArrayFuncs.getDimensions(xcol[0]);
         int size = 1;
         for (int dim=0; dim<dims.length; dim += 1) {
             size *= dims[dim];
         }

         if (size != sizes[col]) {
             throw new FitsException("Replacement column has size mismatch");
         }

         Object x = columns.elementAt(col);
         if (ArrayFuncs.getBaseClass(xcol) != ArrayFuncs.getBaseClass(x)) {
             throw new FitsException("Replactment column has type mismatch");
         }

         for (int row=0; row < nrow;  row += 1) {

             System.arraycopy(ArrayFuncs.flatten(xcol[row]), 0,
                              x, row*size, size);
         }

     }


     /** Set a column with the data already flattened.
       *
       * @param col  The index of the column to be replaced.
       * @param data The new data array.  This should be a one-d
       *             primitive array.
       * @exception FitsException Thrown if the type of length of
       *                         the replacement data differs from the
       *                         original.
       */
      public void setFlattenedColumn (int col, Object data) throws FitsException {

          Object x = columns.elementAt(col);
          if ((x.getClass() != data.getClass()) || (Array.getLength(x) != Array.getLength(data))) {
              throw new FitsException("Replacement column mismatch");
          }

          // Copy the new data into the array.
          System.arraycopy(data, 0, x, 0, sizes[col]);
      }




    /** Get a given column
      * @param col The index of the column.
      */
    public Object[] getColumn(int col) throws FitsException {

         if (!validColumn(col)) {
             throw new FitsException("Invalid column");
         }

         int[] dims = new int[dimens[col].length+1];
         System.arraycopy(dimens[col], 0, dims, 0, dimens[col].length);
         dims[dimens[col].length] = getNrow();
         return (Object[]) ArrayFuncs.curl(columns.elementAt(col),dims);

     }

     /** Get a column in flattened format.
       * For large tables getting a column in standard format can be
       * inefficient because a separate object is needed for
       * each row.  Leaving the data in flattened format means
       * that only a single object is created.
       * @param col
       */

     public Object getFlattenedColumn(int col) throws FitsException {
         if (!validColumn(col) ) {
             throw new FitsException("Invalid column");
         }

         return columns.elementAt(col);
     }

     /** Get a particular element from the table.
       * @param i The row of the element.
       * @param j The column of the element.
       */
     public Object getElement(int i, int j) throws FitsException {

         if (!validRow(i) || !validColumn(j)) {
             throw new FitsException("No such element");
         }
         return table.getElement(i,j);
     }

     /** Add a row at the end of the table.
       * @param o An array of objects instantiating the data.  These
       *          should have the same structure as any existing rows.
       */
     public void addRow(Object[] o) throws FitsException {

         Vector newColumns = new Vector(columns.size());
         for (int col=0; col<columns.size(); col += 1) {
             Object oldArray = columns.elementAt(col);
             int olen = Array.getLength(oldArray);
             Class obase = ArrayFuncs.getBaseClass(oldArray);
             Object newArray = Array.newInstance(obase,olen+sizes[col]);
             System.arraycopy(oldArray, 0, newArray, 0, olen);
             System.arraycopy(
                ArrayFuncs.flatten(o[col]), 0, newArray, olen, sizes[col]
             );

         }

         columns = newColumns;
         Object[] arrCol = new Object[columns.size()];
         columns.copyInto(arrCol);
         try {
             table = new ColumnTable(arrCol, sizes);
         } catch (TableException e) {
             throw new FitsException("Unable to modify table:"+e);
         }

     }

     /** Add a column to the end of a table.
       * @param o An array of identically structured objects with the
       *          same number of elements as other columns in the table.
       */
     public void addColumn(Object[] o) throws FitsException {

         int[] dims = ArrayFuncs.getDimensions(o[0]);
         addFlattenedColumn(ArrayFuncs.flatten(o), dims);

     }

     /** Add a column where the data is already flattened.
       * @param o      The new column data.  This should be a one-dimensional
       *               primitive array.
       * @param dimens The dimensions of one row of the column.
       */



     public void addFlattenedColumn(Object o, int[] dims)  throws FitsException {

         int[] newsizes = new int[sizes.length + 1];
         int[][] newdimens = new int[dimens.length + 1][];
         Object[] newmodel = new Object[modelRow.length + 1];

         int size = 1;
         for (int dim=0; dim < dims.length; dim += 1) {
             size *= dims[dim];
         }

         for (int col=0; col< sizes.length; col += 1) {
              newsizes[col] = sizes[col];
              newdimens[col] = dimens[col];
              newmodel[col] = modelRow[col];
         }


         sizes    = newsizes;
         dimens   = newdimens;
         modelRow = newmodel;

         sizes[sizes.length-1]    = size;
         dimens[sizes.length-1]   = dims;
         modelRow[sizes.length-1] = Array.newInstance(ArrayFuncs.getBaseClass(o), dims);

         columns.addElement(o);
         Object[] arrCol = new Object[columns.size()];
         columns.copyInto(arrCol);

         try {
             table = new ColumnTable(arrCol, sizes);
         } catch (TableException e) {
              throw new FitsException("Unable to modify table:"+e);
         }
         dataArray  = table;

     }

     /** Get the number of rows in the table
       */
     public int getNrow() {
         return table.getNrow();
     }

     /** Get the number of columns in the table.
       */
     public int getNcol() {
         return table.getNcol();
     }

     /** Check to see if this is a valid row.
       * @param i The Java index (first=0) of the row to check.
       */
     protected boolean validRow(int i) {
         if (getNrow() > 0 && i >= 0 && i <getNrow()) {
             return true;
         } else {
             return false;
         }
     }

     /** Check if the column number is valid.
       *
       * @param j The Java index (first=0) of the column to check.
       */
     protected boolean validColumn(int j) {
         return (j >= 0 && j < getNcol());
     }

     /** Replace a single element within the table.
       *
       * @param i The row of the data.
       * @param j The column of the data.
       * @param o The replacement data.
       */
    public void setElement(int i, int j, Object o) throws FitsException{

        try {
            table.setElement(i, j, ArrayFuncs.flatten(o));
        } catch (TableException e) {
            throw new FitsException("Error modifying table:"+e);
        }
    }

    /** This routine makes sure the hash area is large
      * enough to fill a given request.  If not it reallocates
      * the hash area, and copies the old data into the new
      * area.
      * @param need The number of bytes needed for the current
      *             hash area request.
      */
    protected void expandHashArea(int need) {

        if (hashPtr+need > hashArea.length) {
            int newlen = (int)((hashPtr+need)*1.5);
            if (newlen < 16384) {
                newlen = 16384;
            }
            byte[] newHash = new byte[newlen];
            System.arraycopy(hashArea, 0, newHash, 0, hashPtr);
            hashArea = newHash;
        }
    }

    /** Write the data including any hash data.
      *
      * @param o The output stream.
      */
    protected void writeTrueData(BufferedDataOutputStream o) throws FitsException {

        try {
            table.write(o);
            o.write(hashArea, 0, hashPtr);
        } catch (IOException e) {
            throw new FitsException("Error writing binary table data:"+ e);
        }
    }

    public void read(BufferedDataInputStream i) throws FitsException {


    /** Read the data associated with the HDU including the hash area if present.
      * @param i The input stream
      */
        readTrueData(i);
    }

    protected void readTrueData(BufferedDataInputStream i) throws FitsException {

         int len;

         try {
             len = table.read(i);

             if (hashArea.length > 0) {
                 i.readPrimitiveArray(hashArea);
             }
             len += hashArea.length;


             if (len %2880 != 0) {
                 byte[] padding = new byte[2880-len%2880];
                 i.readPrimitiveArray(padding);
             }

         } catch (IOException e) {
             throw new FitsException("Error reading binary table data:"+e);
         }
    }

    /** Get the size of the data in the HDU sans padding.
      */
    public int getTrueSize() {
        return super.getTrueSize() + hashPtr;
    }


    /** Add a variable length column to the data.
      *
      * @param data    The data comprising the variable length column.
      *                This should be a two dimensional primitive array
      *                (3D for complex data).
      *                Note that it is declared as a one dimensional object
      *                array to make access convenient.  Any 2-d array can
      *                be cast to a 1-d array of objects.
      * @return        A column describing the variable format data.
      *                This column can then be added to the table using
      *                other functions.
      */

    public Column addVarData(Object[] data) throws FitsException {

        int size = ArrayFuncs.computeSize(data);
        int baseLength = ArrayFuncs.getBaseLength(data);

        // Check if the data is complex.  If so then the third dimension
        // should be two.
        if (data instanceof Object[][]) {
            baseLength *= 2;
        }

        int offset = hashPtr;
        expandHashArea(size);
        ByteArrayOutputStream bo = new ByteArrayOutputStream(size);

        try {
             BufferedDataOutputStream o = new BufferedDataOutputStream(bo);
             o.writePrimitiveArray(data);
             o.flush();
             o.close();
        } catch (IOException e) {
             throw new FitsException("Unable to write variable column length data");
        }

        System.arraycopy(bo.toByteArray(), 0, hashArea, hashPtr, size);
        hashPtr += size;

        int nrow = data.length;
        int[][] pointers = new int[nrow][2];
        int myMax = 0;

        for (int i=0; i<nrow; i += 1) {
             int rowLength = Array.getLength(data[i]);
             pointers[i][0] = rowLength;
             pointers[i][1] = offset;
             offset += rowLength*baseLength;
             if (rowLength > myMax) {
                 myMax = rowLength;
             }
        }

        Column newColumn = new Column();
        newColumn.setData(pointers);
        return newColumn;

    }

    /** Get the data from a variable length column as a two-d primitive array.
      *
      * @param col       The  index of the column to be returned.
      * @param newArray  The array to be filled with variable length data.
      *                  This is passed to the BinaryTable class rather than
      *                  created here so that we can handle complex data properly.
      *                  This will be a two or three dimensional array where the
      *                  first dimension is the number of rows in the table.
      * @param baseClass The base class of the array.  It should be one
      *               of the primitive types, e.g, Integer.TYPE.
      */

    public Object getVarData(int col, Class baseClass, boolean complex) throws FitsException {

        if (col < 0 || col >= getNcol()) {
             throw new FitsException("Invalid column specified for variable length extraction");
        }

        int[] dims;

        if (complex) {
            dims = new int[3];
            dims[2] = 0;
        } else {
            dims = new int[2];
        }

        dims[0] = getNrow();
        dims[1] = 0;

        Object[] newArray = (Object[])Array.newInstance(baseClass, dims);
        int baseLength = ArrayFuncs.getBaseLength(newArray);
        if (complex) {
            baseLength *= 2;
        }

        int offset = 0;
        BufferedDataInputStream inp = new BufferedDataInputStream(
                                          new ByteArrayInputStream(hashArea));

        int[] ptrs = (int[])table.getColumn(col);

        for (int i=0; i<getNrow(); i += 1) {

             if (ptrs[2*i+1] < offset) {
                 inp = new BufferedDataInputStream(new ByteArrayInputStream(hashArea));
                 offset = 0;
             }
             try {

                 inp.skipBytes(ptrs[2*i+1]-offset);
                 int[] xdims;
                 if (complex) {
                     xdims = new int[2];
                     xdims[0] = ptrs[2*i];
                     xdims[1] = 2;
                 } else {
                     xdims = new int[1];
                     xdims[0] = ptrs[2*i];
                 }
                 newArray[i] = Array.newInstance(baseClass,xdims);
                 inp.readPrimitiveArray(newArray[i]);

                 offset += baseLength * ptrs[2*i];

             } catch (IOException e) {
                 throw new FitsException("Error decoding hash area at offset="+offset+
                    ".  Exception: Exception "+e);
             }
        }
        return newArray;
    }

    public void write(BufferedDataOutputStream os) throws FitsException {
      int len;
      try {

        // First write the table.
        len = table.write(os);

        // Now any variable length data.
        if (hashPtr> 0) {
            os.write(hashArea);

        }
        len += hashPtr;

        // Check and see if any padding needs to be appended.
        if (len%2880  != 0) {
            byte[] pad = new byte[2880 - len%2880];
            for (int i=0; i<pad.length; i += 1) {
                pad[i] = 0;
            }
            os.write(pad);
        }
      } catch (IOException e) {
          throw new FitsException("Unable to write table:"+e);
      }


    }

    public int getHeapSize() {
        return hashPtr;
    }

    public int[][] getDimens() {
        return dimens;
    }


    public Class[] getBases() {
        return table.getBases();
    }

    public char[] getTypes() {
        return table.getTypes();
    }

    public int[] getSizes() {
        return sizes;
    }


}
