/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;

/**
 * AttributeSet is an inquiry interface
 * for a collection of Attributes.
 * Uses naming conventions of Collection framework.
 * @see java.util.Collection
 * @see Attribute
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:07 $
 */
public interface AttributeSet {

    /**
     * Returns the number of elements in the set
     * @return int number of elements in the set
     */
    int size();

    /**
     * Returns AttributeIterator for the elements.
     * @return AttributeIterator for the elements.
     * @see AttributeIterator
     */
    AttributeIterator iterator();

    /**
     * Returns a new Array containing the elements of this set.
     * @return a new Array containing the elements of this set.
     */
    Attribute [] toArray();

    /**
     * Retrieve the attribute associated with the specified name.
     * @param name String which identifies the desired attribute
     * @return the attribute, or null if not found
     */
    public Attribute get(String name);
    
    /**
     * Tests if the Attribute identified by <code>name</code>
     * is in this set.
     * @param name String which identifies the desired attribute
     * @return <code>true</code> if and only if this set contains
     * the named Attribute.
     */
    public boolean contains(String name);

    /**
     * Tests if the argument is in this set.
     * @param oo some Object
     * @return <code>true</code> if and only if this set contains
     * <code>oo</code>
     */
    public boolean contains(Object oo);

    /**
     * Ensures that this set contains the specified Attribute.
     * If a different Attribute with the same name, was in the set,
     * it is returned, otherwise null is returned.
     * <p>
     * This is an "optional operation" in the sense of the Collections
     * framework. In the context of this package, this method will throw
     * UnsupportedOperationException when the set is unmodifiable.
     * This will be the case when this AttributeSet is associated with
     * a Netcdf or a Variable. The AttributeSet will be modifiable when
     * it is associated with a Schema or ProtoVariable.
     *
     * @param attr the Attribute to be added to this set.
     * @return Attribute replaced or null if not a replacement
     */
    public Attribute put(Attribute attr);

    
    /**
     * Delete the Attribute specified by name from this set.
     * <p>
     * This is an "optional operation" in the sense of the Collections
     * framework. In the context of this package, this method will throw
     * UnsupportedOperationException when the set is unmodifiable.
     * This will be the case when this AttributeSet is associated with
     * a Netcdf or a Variable. The AttributeSet will be modifiable when
     * it is associated with a Schema or ProtoVariable.
     *
     * @param name String identifying the Attribute to be removed.
     * @return true if the Set changed as a result of this call.
     */
    public boolean remove(String name);


    /**
     * Delete the Attribute specified from this set.
     * <p>
     * This is an "optional operation" in the sense of the Collections
     * framework. In the context of this package, this method will throw
     * UnsupportedOperationException when the Set is unmodifiable.s
     * This will be the case when this AttributeSet is associated with
     * a Netcdf or a Variable. The AttributeSet will be modifiable when
     * it is associated with a Schema or ProtoVariable.
     *
     * @param oo Attribute to be removed.
     * @return true if the Set changed as a result of this call.
     */
    public boolean remove(Object oo);
}
