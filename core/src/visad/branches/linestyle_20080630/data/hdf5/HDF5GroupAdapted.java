/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import visad.data.hdf5.hdf5objects.HDF5Group;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import visad.*;

public class HDF5GroupAdapted
	extends HDF5Group
	implements HDF5DataAdaptable
{
	private MathType mathtype = null;
	private DataImpl tuple = null;
	private DataImpl[] datas = null;

	/** Constructs an HDF5Group */
	public HDF5GroupAdapted()
	{
		super();
	}

	/** Creates a new HDF5 Group.
	 *  @param loc_id The file or group identifier.
	 *  @param gname The absolute or relative name of the new group.
	 *  @param name_length The maximum length of the name.
	 */
	public HDF5GroupAdapted(int loc_id, String gname, int name_length)
	throws HDF5Exception
	{
		super(loc_id, gname, name_length);

		try { init(); }
		catch (HDF5Exception e) {
			throw new HDF5Exception("HDF5GroupAdapted: "+e); }
	}

	/** Opens an existing HDF5 Group.
	 *  @param loc_id The file or group identifier..
	 *  @param name The absolute or relative name of the new group..
	 */
	public HDF5GroupAdapted(int loc_id, String name)
	throws HDF5Exception
	{
		super(loc_id, name);

		try { init(); }
		catch (HDF5Exception e) {
			throw new HDF5Exception("HDF5GroupAdapted: "+e); }
	}

	/** initailize the HDF5GroupAdapted: fill the members of the group */
	public void init () throws HDF5Exception
	{
		if (id < 0) return;

		int pid=id, pgroup=-1;
		String gname = name;
		int [] oType = {0};
		HDF5GroupAdapted g;
		HDF5DatasetAdapted d;

		int nelems = H5.H5Gn_members(pid, gname);
		if (nelems <=0) return;

		String [] oName = {" "};
		for ( int i = 0; i < nelems; i++) {
			H5.H5Gget_obj_info_idx(pid, gname, i, oName, oType );

			switch (oType[0]) {
				case HDF5Constants.H5G_GROUP:
					pgroup = H5.H5Gopen(pid,gname);
					g = new HDF5GroupAdapted(pgroup, name+"/"+oName[0]);
					addMember(g);
					break;
				case HDF5Constants.H5G_DATASET:
					d = new HDF5DatasetAdapted(pid, name+"/"+oName[0]);
					addMember(d);
					break;
				default:
					// do not know what to do with other objects in visad
					break;
			} // switch (oType[0]) {
			oName[0] = null;
			oType[0] = -1;
		} // for ( i = 0; i < nelems; i++) {

		try { getMathType(); }
		catch (VisADException e) { throw new HDF5Exception("HDF5GroupAdapted: "+e); }
	}

	public MathType getMathType() throws VisADException
	{
		MathType mt = null;
		HDF5DataAdaptable theMember = null;
		int size = getMemberCount();
		int new_size = 0;

		if (size <= 0 ) return (mathtype = null);

		if (mathtype == null)
		{
    		MathType[] m_types = new MathType[size];
    		for ( int i = 0; i < size; i++ ) {
				theMember = (HDF5DataAdaptable)getMemberAt(i);
				mt = theMember.getMathType();
				if (mt == null)
					removeMember(theMember);
				else
      				m_types[new_size++] = mt;
	    	}

			new_size = getMemberCount();
			MathType[] new_types = m_types;

			if (new_size < size)
			{
				if (new_size <= 0 ) return (mathtype = null);
    			new_types = new MathType[new_size];
   				for ( int i = 0; i < new_size; i++ )
					new_types[i] = m_types[i];
			}

    		mathtype = (MathType) new TupleType( new_types );
		}

		return mathtype;
	}

   	public DataImpl getAdaptedData() throws VisADException, RemoteException
	{
		int size = getMemberCount();

		if (size <= 0) return null;

		if (datas == null)
			datas = new DataImpl[size];

		HDF5DataAdaptable theData = null;
		if ( tuple == null )
		{
			for ( int i = 0; i < size; i++ ) {
				theData = (HDF5DataAdaptable)getMemberAt(i);
				datas[i] = theData.getAdaptedData();
			}
			tuple = (DataImpl) new Tuple( (TupleType)mathtype, datas, false );
		}

		return tuple;
	}

	public DataImpl getAdaptedData( int[] indexes ) throws VisADException, RemoteException
	{
		int size = getMemberCount();

		if (size <= 0) return null;

		for ( int i = 0; i < size; i++ ) {
			datas[i] = ((HDF5DataAdaptable)getMemberAt(i)).getAdaptedData( indexes );
		}

		Tuple tuple = new Tuple( (TupleType)mathtype, datas, false );

		return tuple;
	}

	public HDF5DataAdaptable getElement( int i )
	{
		return (HDF5DataAdaptable)getMemberAt(i);
	}

}
