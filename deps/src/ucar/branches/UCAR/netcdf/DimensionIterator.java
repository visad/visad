/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;

/**
 * Type specific Iterator.
 * Use the Iterator methods to fetch elements sequentially.
 * @see java.util.Iterator
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public interface DimensionIterator {
    /**
     * Returns <code>true</code> if there are more elements.
     */
    boolean hasNext();
    /**
     * Returns the next element. Calls to this
     * method will step through successive elements.
     * @exception java.util.NoSuchElementException If no more elements exist.
     */
    Dimension next();
}
