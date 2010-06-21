/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import ncsa.hdf.hdf5lib.*;
import ncsa.hdf.hdf5lib.exceptions.*;

/**
 *  <p>
 *  An HDF5 attribute is a small datasets attached to primary datasets as
 *  metadata information. Because attributes are intended to be small objects,
 *  large datasets intended as additional information for a primary dataset
 *  should be stored as supplemental datasets in a group with the primary
 *  dataset.
 *  <P>
 *  Attributes are not seperate objects in the file, they are always contained
 *  in the object header of the object they are attached to.
 *  <P>
 *  For details of the HDF5 libraries, see the HDF5 Documentation at:
 *  <a href="http://hdf.ncsa.uiuc.edu/HDF5/doc/">http://hdf.ncsa.uiuc.edu/HDF5/doc/</a>
 */

public class HDF5Attribute extends HDF5Dataset
{
	/** cosntruct an HDF5Attribute */
	public HDF5Attribute() {
		super ();
		type = ATTRIBUTE;
	}

	/**
	 * Creates a new HDF5Attribute
	 * @param loc_id The identifier of the object the attribute is attached to.
	 * @param attr_name Name of attribute to create.
	 * @param type_id The identifier of datatype for attribute.
	 * @param space_id The identifier of dataspace for attribute.
	 * @param create_plist The identifier of creation property list.
	 */
	public HDF5Attribute (int loc_id, String attr_name, int type_id, int space_id,
	int create_plist)
	{
		super(attr_name);

		type = ATTRIBUTE;

		try {
			id = H5.H5Acreate(loc_id,  attr_name, type_id, space_id, create_plist);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Attribute: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) {
			System.err.println("HDF5Attribute.init(): "+e);
		}
	}

	/**
	 * Opens an HDF5Attribute specified by its name
	 * @param loc_id The identifier of the object the attribute is attached to.
	 * @param attr_name Name of attribute to create.
	 */
	public HDF5Attribute (int loc_id, String attr_name)
	{
		super(attr_name);

		type = ATTRIBUTE;

		try {
			id = H5.H5Aopen_name(loc_id,  attr_name);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Attribute: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) {
			System.err.println("HDF5Attribute.init(): "+e);
		}
	}

	/**
	 * Opens an HDF5Attribute specified by its index.
	 * @param loc_id The identifier of the object the attribute is attached to.
	 * @param idx The index of the attribute to open.
	 */
	public HDF5Attribute (int loc_id, int idx)
	{
		super();

		type = ATTRIBUTE;

		try {
			id = H5.H5Aopen_idx(loc_id,  idx);
			String n[] = {""};
			H5.H5Aget_name(id, 80, n);
			name = n[0];
		} catch (Exception e) {
			System.err.println("HDF5Attribute: "+e);
			id = -1;
			name = null;
		}

		try { init(); }
		catch (HDF5Exception e) {
			System.err.println("HDF5Attribute.init(): "+e);
		}
	}

	/** initialize the HDF5Attribute:
	    <OL>
			<LI> Set up datatype and dataspace.
			<LI> Set up data ranks and dimensions.
		</OL>
	 */
	public void init () throws HDF5Exception
	{
		if (id < 0) return;

		datatype = new HDF5Datatype();
		datatype.setID(H5.H5Aget_type(id));
		datatype.init();

		dataspace = new HDF5Dataspace();
		dataspace.setID(H5.H5Aget_space(id));
		dataspace.init();

		rank = dataspace.getRank();
		dims = dataspace.getDims();
		maxdims = dataspace.getMaxdims();
		count = dataspace.getCount();
	}

	/**
	 * finalize() is called by the garbage collector on the object when garbage
	 * collection determines that there are no more references to the object. It
	 * is used to dispose of system resources or to perform other cleanup as C++
	 * destructors
	 */
	protected void finalize() throws Throwable
	{
		try { super.finalize(); }
		finally { H5.H5Aclose(id); }
	}
}




