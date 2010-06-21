package nom.tam.util;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */


/** This interface defines the properties that
  * a generic table should have.
  */

public interface DataTable {

    void   setRow(int row, Object newRow)
        throws TableException;
    Object getRow(int row);

    void   setColumn(int column, Object newColumn)
        throws TableException;
    Object getColumn(int column);

    void   setElement(int row, int col, Object newElement)
        throws TableException;
    Object getElement(int row, int col);

    int getNrow();
    int getNcol();

}
