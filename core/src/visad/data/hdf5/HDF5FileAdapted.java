/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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


package visad.data.hdf5;

import visad.data.hdf5.hdf5objects.*;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

public class HDF5FileAdapted extends HDF5File
{
	/** data objects in the file */
	private java.util.Vector datas = null;

	/** Creates a new HDF5 file.
	 *  @param filename The name of the HDF5 file.
	 *  @param flags File access flags.
	 *  @param create_id File creation property list identifier.
	 *  @param access_id File access property list identifier.
	 */
	public HDF5FileAdapted(String filename, int flags, int create_id, int access_id)
	throws HDF5Exception
	{
		super(filename, flags, create_id, access_id);

		try { init(); }
		catch (HDF5Exception e) {
			throw new HDF5Exception("HDF5FileAdapted: "+e); }
	}

	/** Opens an existing HDF5 file.
	 *  @param filename The name of the HDF5 file.
	 *  @param flags File access flags.
	 *  @param access_id File access property list identifier.
	 */
	public HDF5FileAdapted(String filename, int flags, int access_id)
	throws HDF5Exception	{
		super(filename, flags, access_id);

		try { init(); }
		catch (HDF5Exception e) {
			throw new HDF5Exception("HDF5FileAdapted: "+e); }
	}

	/** initialize the HDF5FileAdapted:
		load the table of content of for the top level objects.
	 */
	public void init () throws HDF5Exception
	{
		if (id < 0) return;

		datas = new java.util.Vector();

		int nelems = H5.H5Gn_members(id, "/");
		if (nelems <=0) return;

		int pid = id;
		String gname = "/";  // for the root only
		int [] oType = {0};
		HDF5GroupAdapted g;
		HDF5DatasetAdapted d;

		String [] oName = {" "};
		for ( int i = 0; i < nelems; i++) {
			H5.H5Gget_obj_info_idx(pid, gname, i, oName, oType );

			if (oType[0] == HDF5Constants.H5G_GROUP) {
				g = new HDF5GroupAdapted(pid,  "/"+oName[0]);
				datas.add(g);
			}
			else if (oType[0] == HDF5Constants.H5G_DATASET) {
				d = new HDF5DatasetAdapted(pid, "/"+oName[0]);
				datas.add(d);
			}
			else {
				// do not know what to do with other objects in visad
			} // end of switch (oType[0])
			oName[0] = null;
			oType[0] = -1;
		} // for ( i = 0; i < nelems; i++) {
	}


	/** Returns the number of the data objects in the file */
	public int getObjectCount()
	{
		if (datas == null)
			return -1;
		else
			return datas.size();
	}

	/** Returns the data object with specified index */
	public Object getDataObject(int index)
	{
		if (datas == null)
			return null;
		else
        	return datas.elementAt(index);
	}
}
