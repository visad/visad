/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;

/**
 * This is the interface for Netcdf objects.
 * A Netcdf is seen as a set of Variables, the DimensionSet
 * which is the union of the Dimensions used by the Variables,
 * and any global Attributes. The Variable interface has
 * data i/o (get/set) functionality.
 * <p>
 * This set, the associated DimensionSet and AttributeSet are immutable.
 * <p>
 * The portions of this interface which do not have to
 * do with i/o capable Variables are available in Schema.
 * 
 * @see Variable
 * @see DimensionSet
 * @see AttributeSet
 * @see Schema
 * @see java.util.Collection
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */

public interface Netcdf {

    /**
     * Returns the number of variables
     * @return int number of variables
     */
    int size();

    /**
     * Returns VariableIterator for the elements.
     * @return VariableIterator for the elements.
     * @see VariableIterator
     */
    VariableIterator iterator();

    /**
     * Retrieve the variable associated with the specified name.
     * @param name String which identifies the desired variable
     * @return the variable, or null if not found
     */
    Variable get(String name);
    
    /**
     * Tests if the Variable identified by <code>name</code>
     * is in this set.
     * @param name String which identifies the desired variable
     * @return <code>true</code> if and only if this set contains
     * the named variable.
     */
    boolean contains(String name);

    /**
     * Tests if the argument is in this set.
     * @param oo some Object
     * @return <code>true</code> if and only if this set contains
     * <code>oo</code>
     */
    boolean contains(Object oo);

    /**
     * Returns the set of dimensions associated with this, 
     * the union of those used by each of the variables.
     *
     * @return DimensionSet containing dimensions used
     * by any of the variables. May be empty. Won't be null.
     */
    public DimensionSet getDimensions();

    /**
     * Returns the set of attributes associated with this, 
     * also know as the "global" attributes.
     * 
     * @return AttributeSet. May be empty. Won't be null.
     */
    public AttributeSet getAttributes();

    /**
     * Convenience function; look up global Attribute by name.
     *
     * @param name the name of the attribute
     * @return the attribute, or null if not found
     */
    public Attribute getAttribute(String name);
}
