// $Id: MultiArray.java,v 1.3 2003-02-03 20:09:04 donm Exp $
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
import java.io.IOException;

/**
 *  Interface for multidimensional arrays.
 *  Includes introspection by extending MultiArrayInfo and
 *  data access by extending Accessor.
 *  <p>
 *  These are more general and abstract than Netcdf Variables.
 *  Netcdf Variables implement this, but more general objects,
 *  such as java arrays, can be simply wrapped to provide
 *  this interface.
 *
 * @see MultiArrayInfo
 * @see Accessor
 * @see ucar.netcdf.Variable
 * @see MultiArrayImpl
 * @see ArrayMultiArray
 * @see ScalarMultiArray
 * @see MultiArrayProxy
 * @author $Author: donm $
 * @version $Revision: 1.3 $ $Date: 2003-02-03 20:09:04 $
 */
public interface
MultiArray
	extends MultiArrayInfo, Accessor
{
	/* The super interfaces say it all */
	/**
	 * @return a the original one dimensional Array containing all the elements
	 * in this MultiArray
	 */
	public Object
	    getStorage();



}
