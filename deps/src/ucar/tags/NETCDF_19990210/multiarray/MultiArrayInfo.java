/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;

/**
 *  Inquiry or introspection interface for abstract
 *  multidimensional arrays. The MultiArray interface
 *  extends this by adding data access operations.
 *
 * @see MultiArray
 * @see ucar.netcdf.ProtoVariable
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:45:45 $
 */
public interface MultiArrayInfo {
    /**
     * Returns the Class object representing the component
     * type of the array.
     * @return Class the component type
     * @see java.lang.Class#getComponentType
     */
    public Class getComponentType();

    /**
     * Returns the number of dimensions of the array.
     * @return int number of dimensions of the array
     */
    public int getRank();

    /**
     * Discover the dimensions of this MultiArray.
     *
     * @return int array whose length is the rank of this
     * MultiArray and whose elements represent the
     * length of each of it's dimensions
     */
    public int [] getLengths();

    /**
     * Returns <code>true</code> if and only if the effective dimension
     * lengths can change. For example, if this were implemented by
     * a java.util.Vector.
     * @return boolean <code>true</code> iff this can grow
     */
    public boolean isUnlimited();

    /**
     * Convenience interface; return <code>true</code>
     * if and only if the rank is zero.
     * @return boolean <code>true</code> iff rank == 0
     */
    public boolean isScalar();

}
