/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;

/**
 * DimensionSet is an inquiry interface
 * for a collection of Dimensions.
 * Uses naming conventions of Collection framework.
 * @see java.util.Collection
 * @see Dimension
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public interface DimensionSet {

    /**
     * Returns the number of elements in the set
     * @return int number of elements in the set
     */
    int size();

    /**
     * Returns DimensionIterator for the elements.
     * @return DimensionIterator for the elements.
     * @see DimensionIterator
     */
    DimensionIterator iterator();

    /**
     * Returns a new Array containing the elements of this set.
     * @return a new Array containing the elements of this set.
     */
    Dimension [] toArray();

    /**
     * Retrieve the dimension associated with the specified name.
     * @param name String which identifies the desired dimension
     * @return the dimension, or null if not found
     */
    public Dimension get(String name);

    /**
     * Tests if the Dimension identified by <code>name</code>
     * is in this set.
     * @param name String which identifies the desired dimension
     * @return <code>true</code> if and only if this set contains
     * the named Dimension.
     */
    public boolean contains(String name);

    /**
     * Tests if the argument is in this set.
     * @param oo some Object
     * @return <code>true</code> if and only if this set contains
     * <code>oo</code>
     */
    public boolean contains(Object oo);
}
