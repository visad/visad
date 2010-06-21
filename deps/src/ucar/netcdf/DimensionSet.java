// $Id: DimensionSet.java,v 1.4 2002-05-29 18:31:33 steve Exp $
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
 * DimensionSet is an inquiry interface
 * for a collection of Dimensions.
 * Uses naming conventions of Collection framework.
 * @see java.util.Collection
 * @see Dimension
 *
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:33 $
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
