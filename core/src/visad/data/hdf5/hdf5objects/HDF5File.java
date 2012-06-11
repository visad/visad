/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.File;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 *  <p>
 *  An HDF5File is designed to provide file-level access to HDF5 files.
 *  <P>
 *  For details of the HDF5 libraries, see the HDF5 Documentation at:
 *  <a href="http://hdf.ncsa.uiuc.edu/HDF5/doc/">http://hdf.ncsa.uiuc.edu/HDF5/doc/</a>
 */

public class HDF5File extends HDF5Object
{

	private static final long serialVersionUID = 1L;

	/** Creates a new HDF5 file.
	 *  @param filename The name of the HDF5 file.
	 *  @param flags File access flags.
	 *  @param create_id File creation property list identifier.
	 *  @param access_id File access property list identifier.
	 */
	public HDF5File(String filename, int flags, int create_id, int access_id)
	throws HDF5Exception
	{
		super(filename);

		type = HDF5FILE;

		try {
			id = H5.H5Fcreate(filename, flags, create_id, access_id);
		} catch (HDF5Exception e) {
			id = -1;
			throw new HDF5Exception("HDF5File: "+e);
		}
	}

	/** Opens an existing HDF5 file.
	 *  @param filename The name of the HDF5 file.
	 *  @param flags File access flags.
	 *  @param access_id File access property list identifier.
	 */
	public HDF5File(String filename, int flags, int access_id)
	throws HDF5Exception
	{
		super(filename);

		type = HDF5FILE;

		try {
			id = H5.H5Fopen(filename, flags, access_id);
		} catch (HDF5Exception e) {
			id = -1;
			throw new HDF5Exception("HDF5File: "+e);
		}
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
		finally { H5.H5Fclose(id); }
	}


	public HDF5TreeNode loadTree()	{

		int file;
		int ret;
		HDF5TreeNode root = null;

		try {
			file = H5.H5Fopen(name,
				HDF5Constants.H5F_ACC_RDWR,
				HDF5Constants.H5P_DEFAULT);
		} catch (HDF5Exception ex) {
				System.err.println("H5Fopen failed "+ex);
				return root;
		}

		HDF5Group rootGroup = new HDF5Group(file, "/");
		rootGroup.setShortName((new File(name)).getName());
		root = new HDF5TreeNode(rootGroup);

		depth_first(file,"/", root);

		try {
			ret = H5.H5Fclose(file);
		} catch (HDF5Exception ex) {
			System.err.println("H5Fopen failed "+ex);
			return root;
		}
		return root;
	}

	private boolean depth_first( int pid, String gname, HDF5TreeNode pnode)
	{
		int nelems = 0;
		int [] oType = new int[1];
		String [] oName = new String[1];
		oName[0] = new String(" ");
		int i, ret;
		HDF5TreeNode node;
		HDF5Object o;
		HDF5Group g;
		HDF5Dataset d;
		HDF5Datatype t;

		HDF5Group pObject = (HDF5Group)(pnode.getUserObject());

		String pPath = pObject.getName();
		if (pPath.length() > 1) pPath += "/"; //do not need add "/" for the root

 		//Iterate through the file to see members of groups
 		nelems = 0;
		try {
			nelems = H5.H5Gn_members(pid, gname);
		} catch (HDF5Exception ex) {
			System.err.println("HDF5File.depth_first(): H5Gn_members() Failed, "+ex);
			return false;
		}
		if (nelems < 0 ) {
			return false;
		}

		for ( i = 0; i < nelems; i++) {
			try {
				ret = H5.H5Gget_obj_info_idx(pid, gname, i, oName, oType );
			} catch (HDF5Exception ex) {
				System.err.println("HDF5File.depth_first(): H5Gn_members() Failed, "+ex);
				return false;
			}

			if (ret < 0)  {
				continue;
			}

			if (oType[0] == HDF5Constants.H5G_GROUP) {
				g = new HDF5Group(pid,  pPath+oName[0]);
				g.setParent(pObject);
				pObject.addMember(g);
				node = new HDF5TreeNode(g);
				pnode.add( node );
				int pgroup = -1;
				try {
					pgroup = H5.H5Gopen(pid,gname);
					depth_first(pgroup, oName[0], node);
				} catch (HDF5Exception ex) {
					System.err.println("HDF5File.depth_first(): H5Gopen() Failed, "+ex);
				}
			}
			else if (oType[0] == HDF5Constants.H5G_DATASET) {
				d = new HDF5Dataset(pid, pPath+oName[0]);
				pObject.addMember(d);
				node = new HDF5TreeNode(d);
				pnode.add( node );
			}
			else if (oType[0] == HDF5Constants.H5G_TYPE) {
				t = new HDF5Datatype(pid, pPath+oName[0]);
				pObject.addMember(t);
				node = new HDF5TreeNode(t);
				pnode.add( node );
			}
			else {
				o = new HDF5Object(pPath+oName[0]);
				pObject.addMember(o);
				node = new HDF5TreeNode(o);
				pnode.add( node );
			} // end of switch (oType[0])
			oName[0] = null;
			oType[0] = -1;
		} // for ( i = 0; i < nelems; i++) {

		return true;
	} // private boolean depth_first

}

