// $Id: AttributeSet.java,v 1.4 2002-05-29 18:31:33 steve Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package ucar.netcdf;

/**
 * AttributeSet is an inquiry interface
 * for a collection of Attributes.
 * Uses naming conventions of Collection framework.
 * @see java.util.Collection
 * @see Attribute
 *
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:33 $
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
