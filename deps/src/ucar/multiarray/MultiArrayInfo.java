// $Id: MultiArrayInfo.java,v 1.2 2002-05-29 20:32:40 steve Exp $
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
package ucar.multiarray;

/**
 *  Inquiry or introspection interface for abstract
 *  multidimensional arrays. The MultiArray interface
 *  extends this by adding data access operations.
 *
 * @see MultiArray
 * @see ucar.netcdf.ProtoVariable
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:40 $
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
