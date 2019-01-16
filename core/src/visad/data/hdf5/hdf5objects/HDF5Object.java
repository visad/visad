/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/

package visad.data.hdf5.hdf5objects;

import java.io.Serializable;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 *  HDF5Object is an HDF5Object which is the super class of all the
 *  HDF5 objects. Each HDF5 object inherits all the methods and fields of the
 *  HDF5Object and may override inherited methods.
 *  <P>
 *  HDF5 files are organized in a hierarchical structure, with two primary
 *  structures (or objects): groups and datasets.
 *  <pre>
 *    HDF5Group: a grouping structure containing instances of zero or more
 *         groups or datasets, together with supporting metadata.
 *    HDF5Dataset: a multidimensional array of data elements, together with
 *         supporting metadata.
 *  </pre>
 *  Other HDF5 objects include
 *  <ul>
 *  <li>HDF5Attribute: small datasets to be attached to primary datasets as
 *    metadata information.
 *  <li>HDF5Dataspace: a dataspace describes the locations that dataset elements
 *    are located at.
 *  <li>HDF5Datatype: a data type is a collection of data type properties.
 *  <li>HDF5Propertylist: a property list is a collection of name/value pairs
 *    which can be passed to various other HDF5 functions.
 *  </ul>
 */

public class HDF5Object implements Serializable
{
	/** Unknown object type */
	public static final int UNKNOWN = -1;

	/** Object is a symbolic link */
	public static final int LINK = 0;

	/** Object is a group */
	public static final int GROUP = 1;

	/** Object is a dataset */
	public static final int DATASET = 2;

	/* Object is a named data type */
	public static final int DATATYPE = 3;

	/* Object is a named data type */
	public static final int DATASPACE = 4;

	/* Object is a named data type */
	public static final int ATTRIBUTE = 5;

	/* HDF5 file */
	public static final int HDF5FILE = 6;

	/** the type of the object */
	protected int type;

	/** the full path name of the HDF5 object */
	protected String name;

	/** the short name for display: name without path */
	protected String shortName;

	/** the identifier of the HDF5 object */
	protected int id;

	/** the short description of the HDF5 object */
	protected String description;

	/** construct an HDF5 object with defaults */
	public HDF5Object() { this (null); }

	/** cosntruct an HDF5Object object name
	 *  @param objName the name of the HDF5 data object
	 */
	public HDF5Object(String objName) {
		name = objName;
		type = UNKNOWN;
		description = "";

		if (name != null && !name.equals("/"))
		{
			int idx = name.lastIndexOf('/')+1;
			shortName = name.substring(idx);
		} else
			shortName = name;
	}

	/** initialize the HDF5Object: open the HDF5 library.
	 *  A subclass of HDF5Object should override this method
	 *  if it has initialization to perform.
	 */
	public void init() throws HDF5Exception
	{
		if (id < 0) return;
	}

	/** reset the HDF5Object for a given id
	 *  @param new_id the id of the object
	 */
	public void setID(int new_id) throws HDF5Exception
	{
		id = new_id;
		init();
	}

	/** Sets the short name of the HDF5Object */
	public void setShortName(String sname) { shortName = sname; }

	/** Returns the type of the object */
	public int getType() { return type; }

	/** Sets the type of the object */
	public void setType(int t) {type = t;}

	/** Returns the full name of the HDF5Object */
	public String getName() { return name; }

	/** Returns the short name of the HDF5Object */
	public String getShortName() { return shortName; }

	/** Returns the description of the HDF5Object */
	public String getDescription() { return description; }

	/** Returns the identifier of the HDF5Object */
	public int getID() { return id; }

	/**
	 * Converts this object to a String representation.
	 * @return a string representation of this object
	 */
	public String toString() {
		if (name == null)
			return super.toString();
		else
			return getClass().getName() + "[name=" + name + "]";
	}


}
