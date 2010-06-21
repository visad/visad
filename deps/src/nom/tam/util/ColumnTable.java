package nom.tam.util;

 /*
  * Copyright: Thomas McGlynn 1997-1998.
  * This code may be used for any purpose, non-commercial
  * or commercial so long as this copyright notice is retained
  * in the source code or included in or referred to in any
  * derived software.
  */
import  java.io.*;
import  java.lang.reflect.Array;

/** A data table is conventionally considered to consist of rows and
  * columns, where the structure within each column is constant, but
  * different columns may have different structures.  I.e., structurally
  * columns may differ but rows are identical.
  * Typically tabular data is usually stored in row order which can
  * make it extremely difficult to access efficiently using Java.
  * This class provides efficient
  * access to data which is stored in row order and allows users to
  * get and set the elements of the table.
  * The table can consist only of arrays of primitive types.
  * Data stored in column order can
  * be efficiently read and written using the
  * BufferedDataXputStream classes.
  *
  * The table is represented entirely as a set of one-dimensional primitive
  * arrays.  For a given column, a row consists of some number of
  * contiguous elements of the array.  Each column is required to have
  * the same number of rows.
  */

public class ColumnTable implements DataTable {


    /** The columns to be read/written */
    private Object[] arrays;

    /** The number of elements in a row for each column */
    private int[] sizes;

    /** The number of rows */
    private int nrow;

    /** The number or rows to read/write in one I/O. */
    private int chunk;

    /** The size of a row in bytes */
    private int rowSize;

    /** The base type of each row (using the second character
      * of the [x class names of the arrays.
      */
    private char[] types;
    private Class[] bases;

    // The following arrays are used to avoid having to check
    // casts during the I/O loops.
    // They point to elements of arrays.
    private byte[][]      bytePointers;
    private short[][]     shortPointers;
    private int[][]       intPointers;
    private long[][]      longPointers;
    private float[][]     floatPointers;
    private double[][]    doublePointers;
    private char[][]      charPointers;
    private boolean[][]   booleanPointers;


    /** Create the object after checking consistency.
      * @param arrays  An array of one-d primitive arrays.
      * @param sizes   The number of elements in each row
      *                for the corresponding column
      */
    public ColumnTable(Object[] arrays, int[] sizes) throws TableException {
        setup(arrays, sizes);
    }

    /** Actually perform the initialization.
      */
    protected void setup(Object[] arrays, int[] sizes) throws TableException {

        checkArrayConsistency(arrays, sizes);
        getNumberOfRows();
        initializePointers();

    }

    /** Get the number of rows in the table.
      */
    public int getNrow() {
        return nrow;
    }

    /** Get the number of columns in the table.
      */
    public int getNcol() {
        return arrays.length;
    }


    /** Get a particular column.
      * @param col The column desired.
      * @return an object containing the column data desired.
      *         This will be an instance of a 1-d primitive array.
      */
    public Object getColumn(int col) {
        return arrays[col];
    }

    /** Set the values in a particular column.
      * The new values must match the old in length but not necessarily in type.
      * @param col The column to modify.
      * @param newColumn The new column data.  This should be a primitive array.
      * @exception TableException Thrown when the new data is not commenserable with
      *                           informaiton in the table.
      */
    public void setColumn(int col, Object newColumn) throws TableException {
        arrays[col] = newColumn;
        setup(arrays, sizes);
    }

    /** Get a element of the table.
      * @param row The row desired.
      * @param col The column desired.
      * @return A primitive array containing the information.  Note
      *         that an array will be returned even if the element
      *         is a scalar.
      */
    public Object getElement(int row, int col) {

        Object x = Array.newInstance(bases[col], sizes[col]);
        System.arraycopy(arrays[col], sizes[col]*row, x, 0, sizes[col]);
        return x;
    }

    /** Modify an element of the table.
      * @param row The row containing the element.
      * @param col The column containing the element.
      * @param x   The new datum.  This should be 1-d primitive
      *            array.
      * @exception TableException Thrown when the new data
      *                           is not of the same type as
      *                           the data it replaces.
      */
    public void setElement(int row, int col, Object x)
                           throws TableException {

        String classname = x.getClass().getName();

        if (!classname.equals("["+types[col])) {
            throw new TableException("setElement: Incompatible element type");
        }

        if (Array.getLength(x) != sizes[col]) {
            throw new TableException("setElement: Incompatible element size");
        }

        System.arraycopy(x, 0, arrays[col], sizes[col]*row, sizes[col]);
    }

    /** Get a row of data.
      * @param The row desired.
      * @return An array of objects each containing a primitive array.
      */
    public Object getRow(int row) {

        Object[] x = new Object[arrays.length];
        for (int col=0; col<arrays.length; col += 1) {
             x[col] = getElement(row, col);
        }
        return x;
    }

    /** Modify a row of data.
      * @param row The row to be modified.
      * @param x   The data to be modified.  This should be an
      *            array of objects.  It is described as an Object
      *            here since other table implementations may
      *            use other methods to store the data (e.g.,
      *            @see ColumnTable.getColumn.
      */
    public void setRow(int row, Object x) throws TableException {

        if (! (x instanceof Object[])) {
            throw new TableException("setRow: Incompatible row");
        }

        for (int col=0; col<arrays.length; col += 1) {
            setElement(row, col, ((Object[]) x)[col]);
        }
    }

    /** Check that the columns and sizes are consistent.
      * Inconsistencies include:
      * <ul>
      * <li> arrays and sizes have different lengths.
      * <li> an element of arrays is not a primitive array.
      * <li> the size of an array is not divisible by the sizes entry.
      * <li> the number of rows differs for the columns.
      * </ul>
      * @param arrays The arrays defining the columns.
      * @param sizes  The number of elements in each row for the column.
      */
    protected void checkArrayConsistency(Object[] arrays, int[] sizes)
                                         throws TableException {

        // This routine throws an error if it detects an inconsistency
        // between the arrays being read in.

        // First check that the lengths of the two arrays are the same.
        if (arrays.length != sizes.length) {
          throw new TableException ("readArraysAsColumns: Incompatible arrays and sizes.");
        }

        // Now check that we'll fill up all of the arrays exactly.
        int ratio = 0;
        int rowSize = 0;

        this.types = new char[arrays.length];
        this.bases = new Class[arrays.length];

        // Check for a null table.
        boolean nullTable = true;

        for (int i=0; i<arrays.length; i += 1) {
            if (Array.getLength(arrays[i]) > 0) {
                nullTable = false;
                break;
            }
        }


        for (int i=0; i<arrays.length; i += 1) {

            String classname = arrays[i].getClass().getName();

            if (classname.charAt(0) != '['  || classname.length() != 2) {
                throw new TableException("Non-primitive array");
            }

            int thisSize = Array.getLength(arrays[i]);
            if (thisSize == 0 && sizes[i] == 0) {
                continue;   // It's allowed to have zero length arrays if we don't
                            // ask to put any data in them.
            }

            // ...but if both are not 0, then neither can be 0 individually.
            if ( (thisSize == 0 || sizes[i] <= 0) && !nullTable) {
                throw new TableException("Invalid size for array: index="+i);
            }

            // The row size must evenly divide the size of the array.
            if (thisSize % sizes[i] != 0) {
                throw new TableException("Row size does not divide array: index="+i);
            }

            // Finally the ratio of sizes must be the same for all rows.
            if (sizes[i] > 0) {
                int thisRatio = thisSize/sizes[i];

                if (ratio != 0 && (thisRatio != ratio)) {
                    throw new TableException("Different number of rows in different columns");
                }

                ratio = thisRatio;
            }

            rowSize += sizes[i]*ArrayFuncs.getBaseLength(arrays[i]);
            types[i] = classname.charAt(1);
            bases[i] = ArrayFuncs.getBaseClass(arrays[i]);
        }

        this.nrow = ratio;
        this.rowSize = rowSize;
        this.arrays = arrays;
        this.sizes = sizes;
    }

    /** Calculate the number of rows to read/write at a time.
      * @param rowSize The size of a row in bytes.
      * @param nrows   The number of rows in the table.
      */
    protected void getNumberOfRows() {

        int bufSize=65536;

        // If a row is larger than bufSize, then read one row at a time.
        if (rowSize == 0) {
            this.chunk = 0;

        } else if (rowSize > bufSize) {
            this.chunk = 1;

        // If the entire set isn't too big, just read it all.
        } else if (bufSize/rowSize >= nrow) {
            this.chunk = nrow;
        } else {
            this.chunk = bufSize/rowSize + 1;
        }

    }

    /** Set the pointer arrays for the eight primitive types
      * to point to the appropriate elements of arrays.
      */
    protected void initializePointers() {

        int nbyte, nshort, nint, nlong, nfloat, ndouble, nchar, nboolean;

        // Count how many of each type we have.
        nbyte=0; nshort=0; nint = 0; nlong = 0;
        nfloat = 0; ndouble=0; nchar = 0; nboolean = 0;

        for (int col=0; col<arrays.length; col += 1) {
            switch (types[col]) {

               case 'B':
                   nbyte += 1;
                   break;
               case 'S':
                   nshort += 1;
                   break;
               case 'I':
                   nint += 1;
                   break;
               case 'L':
                   nlong += 1;
                   break;
               case 'F':
                   nfloat += 1;
                   break;
               case 'D':
                   ndouble += 1;
                   break;
               case 'C':
                   nchar += 1;
                   break;
               case 'Z':
                   nboolean += 1;
                   break;
            }
        }

        // Allocate the pointer arrays.  Note that many will be
        // zero-length.

        bytePointers     = new byte[nbyte][];
        shortPointers    = new short[nshort][];
        intPointers      = new int[nint][];
        longPointers     = new long[nlong][];
        floatPointers    = new float[nfloat][];
        doublePointers   = new double[ndouble][];
        charPointers     = new char[nchar][];
        booleanPointers  = new boolean[nboolean][];

        // Now set the pointers.
        nbyte=0; nshort=0; nint = 0; nlong = 0;
        nfloat = 0; ndouble=0; nchar = 0; nboolean = 0;

        for (int col=0; col<arrays.length; col += 1) {
            switch (types[col]) {

               case 'B':
                   bytePointers[nbyte] = (byte[]) arrays[col];
                   nbyte += 1;
                   break;
               case 'S':
                   shortPointers[nshort] = (short[]) arrays[col];
                   nshort += 1;
                   break;
               case 'I':
                   intPointers[nint] = (int[]) arrays[col];
                   nint += 1;
                   break;
               case 'L':
                   longPointers[nlong] = (long[]) arrays[col];
                   nlong += 1;
                   break;
               case 'F':
                   floatPointers[nfloat] = (float[]) arrays[col];
                   nfloat += 1;
                   break;
               case 'D':
                   doublePointers[ndouble] = (double[]) arrays[col];
                   ndouble += 1;
                   break;
               case 'C':
                   charPointers[nchar] = (char[]) arrays[col];
                   nchar += 1;
                   break;
               case 'Z':
                   booleanPointers[nboolean] = (boolean[]) arrays[col];
                   nboolean += 1;
                   break;
            }
        }
    }


    /** Read a table.
      * @param is The input stream to read from.
      */
    public int read(InputStream is) throws IOException {

        byte[] buffer = new byte[chunk*rowSize];

        if (rowSize == 0) {
            return 0;
        }
        int currRow = 0;

        // While we haven't finished reading the table..
        while (currRow < nrow) {

            // The last chunk might not have as many rows.
            int drow = chunk;
            if (currRow+drow > nrow) {
                drow = nrow - currRow;
            }

            // Read in the entire buffer.  The loop is not needed
            // for the BufferedDataInputStream, but checking
            // allows us to use any kind of stream.

            int need = drow * rowSize;
            int got = 0;

            while (need > 0) {
                int len = is.read(buffer, got, need);
                if (len <= 0) {
                    throw new EOFException("EOF reached in ColumnarIO.read");
                }
                need -= len;
                got += len;
            }
            int bufOffset = 0;

            // Loop over the rows in this buffer
            for (int row=currRow; row<currRow+drow; row += 1) {

              int ibyte=0;
              int ishort = 0;
              int iint = 0;
              int ilong = 0;
              int ichar = 0;
              int ifloat = 0;
              int idouble = 0;
              int iboolean = 0;

              // Loop over the columns within the row.
              for (int col=0; col < arrays.length; col += 1) {

                int arrOffset = sizes[col]*row;
                int size = sizes[col];
                int i,i1,i2,tmp;

                switch(types[col]) {
                  // In anticpated order of use.
                  case 'I':
                    int[] ia = intPointers[iint];
                    iint += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {
                      ia[i] =   buffer[bufOffset]         << 24 |
                               (buffer[bufOffset+1]&0xFF) << 16 |
                               (buffer[bufOffset+2]&0xFF) <<  8 |
                               (buffer[bufOffset+3]&0xFF);
                      bufOffset += 4;
                    }
                    break;

                  case 'S':
                    short[] s = shortPointers[ishort];
                    ishort += 1;

                    for(i=arrOffset; i<arrOffset+size; i += 1) {
                      s[i] = (short) (buffer[bufOffset] << 8 |
                                     (buffer[bufOffset+1]&0xFF) );
                      bufOffset += 2;
                    }
                    break;

                  case 'B':
                    byte[] b = bytePointers[ibyte];
                    ibyte += 1;

                    for(i=arrOffset; i<arrOffset+size; i += 1) {
                       b[i] = buffer[bufOffset];
                       bufOffset += 1;
                    }
                    break;

                  case 'F':
                    float[] f = floatPointers[ifloat];
                    ifloat += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {
                      tmp = buffer[bufOffset]         << 24 |
                               (buffer[bufOffset+1]&0xFF) << 16 |
                               (buffer[bufOffset+2]&0xFF) <<  8 |
                               (buffer[bufOffset+3]&0xFF);
                      f[i] = Float.intBitsToFloat(tmp);
                      bufOffset += 4;
                    }
                    break;

                  case 'D':
                    double[] d = doublePointers[idouble];
                    idouble += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {
                      i1  = buffer[bufOffset]         << 24 |
                               (buffer[bufOffset+1]&0xff) << 16 |
                               (buffer[bufOffset+2]&0xff) <<  8 |
                               (buffer[bufOffset+3]&0xff);

                      bufOffset += 4;
                      i2  = buffer[bufOffset]         << 24 |
                               (buffer[bufOffset+1]&0xff) << 16 |
                               (buffer[bufOffset+2]&0xff) <<  8 |
                               (buffer[bufOffset+3]&0xff);
                      bufOffset += 4;

                      d[i] = Double.longBitsToDouble(
                              ((long) i1)                   << 32 |
                              ((long) i2&0x00000000ffffffffL)    );
                    }

                    break;
                  case 'C':
                    char[] c = charPointers[ichar];
                    ichar += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {

                      tmp = ((buffer[bufOffset]         << 8 ) |
                             (buffer[bufOffset+1]&0xFF       ) );
                      c[i] = (char) tmp;
                      bufOffset += 2;
                    }
                    break;

                  case 'L':
                    long[] l = longPointers[ilong];
                    ilong += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {
                      i1  = buffer[bufOffset]         << 24 |
                               (buffer[bufOffset+1]&0xff) << 16 |
                               (buffer[bufOffset+2]&0xff) <<  8 |
                               (buffer[bufOffset+3]&0xff);

                      bufOffset += 4;
                      i2  = buffer[bufOffset]         << 24 |
                               (buffer[bufOffset+1]&0xff) << 16 |
                               (buffer[bufOffset+2]&0xff) <<  8 |
                               (buffer[bufOffset+3]&0xff);
                      bufOffset += 4;

                      l[i] = (( (long) i1) << 32) |
                             ((long)i2&0x00000000ffffffffL);
                    }

                    break;
                  case 'Z':

                    boolean[] bool = booleanPointers[iboolean];
                    iboolean += 1;

                    for (i=arrOffset; i < arrOffset+size; i += 1) {
                      if (buffer[bufOffset] == 1) {
                         bool[i] = true;
                      } else {
                         bool[i] = false;
                      }
                      bufOffset += 1;
                    }
                    break;
                }
              }
            }

            currRow += drow;
        }

        // All done if we get here...
        return rowSize*nrow;
    }

    /** Write a table.
      * @param os the output stream to write to.
      */
    public int write(OutputStream os) throws IOException {

        byte[] buffer = new byte[chunk*rowSize];

        if (rowSize == 0) {
            return 0;
        }
        int currRow = 0;

        // While we haven't finished writing the table..
        while (currRow < nrow) {

            // The last chunk might not have as many rows.
            int drow = chunk;
            if (currRow+drow > nrow) {
                drow = nrow - currRow;
            }

            int bufOffset = 0;

            // Loop over the rows in this buffer
            for (int row=currRow; row<currRow+drow; row += 1) {

              int ibyte=0;
              int ishort = 0;
              int iint = 0;
              int ilong = 0;
              int ichar = 0;
              int ifloat = 0;
              int idouble = 0;
              int iboolean = 0;

              // Loop over the columns within the row.
              for (int col=0; col < arrays.length; col += 1) {

                int arrOffset = sizes[col]*row;
                int size = sizes[col];
                int i,i1,i2,tmp;

                switch(types[col]) {
                  // In anticpated order of use.
                  case 'I':
                    int[] ia = intPointers[iint];
                    iint += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {

                      buffer[bufOffset]  = (byte) (ia[i]>>>24);
                      buffer[bufOffset+1]= (byte) (ia[i]>>>16);
                      buffer[bufOffset+2]= (byte) (ia[i]>>> 8);
                      buffer[bufOffset+3]= (byte) (ia[i]);
                      bufOffset += 4;
                    }
                    break;

                  case 'S':
                    short[] s = shortPointers[ishort];
                    ishort += 1;

                    for(i=arrOffset; i<arrOffset+size; i += 1) {
                      buffer[bufOffset]  = (byte) (s[i]>>>8);
                      buffer[bufOffset+1]= (byte) (s[i]);
                      bufOffset += 2;
                    }
                    break;

                  case 'B':
                    byte[] b = bytePointers[ibyte];
                    ibyte += 1;

                    System.arraycopy(b,arrOffset,buffer,bufOffset, size);
                    bufOffset += size;

                    break;

                  case 'F':
                    float[] f = floatPointers[ifloat];
                    ifloat += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {
                      tmp = Float.floatToIntBits(f[i]);
                      buffer[bufOffset]   = (byte) (tmp >>> 24);
                      buffer[bufOffset+1] = (byte) (tmp >>> 16);
                      buffer[bufOffset+2] = (byte) (tmp >>>  8);
                      buffer[bufOffset+3] = (byte) (tmp);
                      bufOffset += 4;
                    }
                    break;

                  case 'D':
                    double[] d = doublePointers[idouble];
                    idouble += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {

                      long lng = Double.doubleToLongBits(d[i]);
                      i1 = (int) (lng >>> 32);
                      i2 = (int) (lng);

                      buffer[bufOffset]   = (byte) (i1 >>> 24);
                      buffer[bufOffset+1] = (byte) (i1 >>> 16);
                      buffer[bufOffset+2] = (byte) (i1 >>>  8);
                      buffer[bufOffset+3] = (byte) (i1);

                      bufOffset += 4;

                      buffer[bufOffset]   = (byte) (i2 >>> 24);
                      buffer[bufOffset+1] = (byte) (i2 >>> 16);
                      buffer[bufOffset+2] = (byte) (i2 >>>  8);
                      buffer[bufOffset+3] = (byte) (i2);

                      bufOffset += 4;
                    }

                    break;

                  case 'C':
                    char[] c = charPointers[ichar];
                    ichar += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {

                      buffer[bufOffset]  = (byte) (c[i] >>> 8);
                      buffer[bufOffset+1]= (byte) (c[i]);

                      bufOffset += 2;
                    }
                    break;

                  case 'L':
                    long[] l = longPointers[ilong];
                    ilong += 1;

                    for (i=arrOffset; i<arrOffset+size; i += 1) {

                      i1 = (int) (l[i] >>> 32);
                      i2 = (int) (l[i]);

                      buffer[bufOffset]   = (byte) (i1 >>> 24);
                      buffer[bufOffset+1] = (byte) (i1 >>> 16);
                      buffer[bufOffset+2] = (byte) (i1 >>>  8);
                      buffer[bufOffset+3] = (byte) (i1);

                      bufOffset += 4;

                      buffer[bufOffset]   = (byte) (i2 >>> 24);
                      buffer[bufOffset+1] = (byte) (i2 >>> 16);
                      buffer[bufOffset+2] = (byte) (i2 >>>  8);
                      buffer[bufOffset+3] = (byte) (i2);

                      bufOffset += 4;
                    }

                    break;
                  case 'Z':

                    boolean[] bool = booleanPointers[iboolean];
                    iboolean += 1;

                    for (i=arrOffset; i < arrOffset+size; i += 1) {
                      if (bool[i]) {
                         buffer[bufOffset] = 1;
                      } else {
                         buffer[bufOffset] = 0;
                      }
                      bufOffset += 1;
                    }
                    break;
                }
              }
            }

            // Write the entire buffer.
            os.write(buffer, 0, rowSize*drow);

            currRow += drow;
        }

        // All done if we get here...
        return rowSize*nrow;
    }

    /** Get the base classes of the columns.
      * @return An array of Class objects, one for each column.
      */
    public Class[] getBases() {
        return bases;
    }

    /** Get the characters describing the base classes of the columns.
      * @return An array of char's, one for each column.
      */
    public char[] getTypes() {
        return types;
    }


}


