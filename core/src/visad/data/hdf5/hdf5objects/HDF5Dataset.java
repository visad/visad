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

import java.util.*;
import java.lang.reflect.Array;
import ncsa.hdf.hdf5lib.*;
import ncsa.hdf.hdf5lib.exceptions.*;

/**
 *  <p>
 *  This class provides a mechanism to describe properties of datasets and to
 *  transfer data between memory and disk. A dataset is composed of a collection
 *  of raw data points and four classes of meta data to describe the data points.
 *  <p>
 *  The four classes of meta data are: 
 *  <pre>
	Constant Meta Data
     	Meta data that is created when the dataset is created and exists unchanged
		for the life of the dataset. For instance, the data type of stored array
		elements is defined when the dataset is created and cannot be subsequently
		changed.
	Persistent Meta Data
		Meta data that is an integral and permanent part of a dataset but can
		change over time. For instance, the size in any dimension can increase
		over time if such an increase is allowed when the dataset was created.
	Memory Meta Data
		Meta data that exists to describe how raw data is organized in the
		application's memory space. For instance, the data type of elements in
		an application array might not be the same as the data type of those
		elements as stored in the HDF5 file.
	Transport Meta Data
		Meta data that is used only during the transfer of raw data from one
		location to another. For instance, the number of processes participating
		in a collective I/O request or hints to the library to control caching
		of raw data.
 *  </pre>
 *  For details of the HDF5 libraries, see the HDF5 Documentation at:
 *  <a href="http://hdf.ncsa.uiuc.edu/HDF5/doc/">http://hdf.ncsa.uiuc.edu/HDF5/doc/</a>
 */

public class HDF5Dataset extends HDF5Object
{
	/** the data array */
	protected Object data;

	/** the datatype */
	protected HDF5Datatype datatype;

	/** the dataspace */
	protected HDF5Dataspace dataspace;

	/** the rank of the dataset */
	protected int rank;

	/** the dimensions of the dataset */
	protected long[] dims;

	/** the maximum dimensions of the dataset */
	protected long[] maxdims;

	/** the selected subset of the dataset */
	protected long[] count;

	/** a list of member names of compound data */
	protected Vector member_names;


	/** Constructs a HDF5Dataset */
	public HDF5Dataset()
	{
		super();

		type = DATASET;
	}

	/** Constructs a HDF5Dataset */
	public HDF5Dataset(String name)
	{
		super(name);

		type = DATASET;
	}

	/** Creates a dataset at the specified location
	 *  @param loc_id Identifier of the file or group to create the dataset within.
	 *  @param set_name The name of the dataset to create.
	 *  @param type_id Identifier of the datatype to use when creating the dataset.
	 *  @param space_id Identifier of the dataspace to use when creating the dataset.
	 *  @param create_plist_id Identifier of the set creation property list.
	 */
	public HDF5Dataset(int loc_id, String set_name, int type_id, int space_id,
		int create_plist_id)
	{
		super(set_name);

		type = DATASET;

		try {
			id = H5.H5Dcreate(loc_id, set_name, type_id, space_id, create_plist_id);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Dataset: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) { System.err.println("HDF5Dataset: "+e); }
	}

	/**
	 * Opens a HDF5Dataset
	 * @param loc_id A file, group, or datatype identifier.
	 * @param set_name A datatset name.
	 */
	public HDF5Dataset (int loc_id, String set_name)
	{
		super(set_name);

		type = DATASET;

		try {
			id = H5.H5Dopen(loc_id, set_name);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Dataset: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) { System.err.println("HDF5Dataset.init(): "+e); }

	}

	/** initialize the HDF5Dataset:
	    <OL>
			<LI> open HDF5 library.
			<LI> Set up datatype and dataspace.
			<LI> Set up data ranks and dimensions.
		</OL>
	 */
	public void init () throws HDF5Exception
	{
		if (id < 0) return;

		datatype = new HDF5Datatype();
		datatype.setID(H5.H5Dget_type(id));

		dataspace = new HDF5Dataspace();
		dataspace.setID(H5.H5Dget_space(id));

		rank = dataspace.getRank();
		dims = dataspace.getDims();
		maxdims = dataspace.getMaxdims();
		count = dataspace.getCount();
	}

	/** Read the entire dataset from file
	 * @return the data array
	 */
	public Object readData() throws HDF5Exception, NullPointerException
	{
		int space = HDF5Constants.H5S_ALL;
		return readData(space, space);
	}

	/** Read the data with specified memory and file space
	 * <P>
	 * @param mspace the memory space id
	 * @param fspace the file space id
	 * @return the data array
	 */
	public Object readData(int mspace, int fspace)
		throws HDF5Exception, NullPointerException
	{
		// clean the old data
		data = null;
		System.gc();

		if (H5.H5Tget_class(datatype.getID())==HDF5Constants.H5T_COMPOUND)
		{
			data = readCompoundData(mspace, fspace);
			return data;
		}

		// read dataset
		data = datatype.defineData(count);

		if (data == null) return null;

		// read strings
		if (data.getClass().getName().endsWith("Ljava.lang.String;"))
		{
			int no_lines = Array.getLength(data);
			long tsize = H5.H5Dget_storage_size(id);
			int max_length = (int)(tsize/no_lines);
			byte [][] bdata = new byte[no_lines][max_length];

 			H5.H5Dread(id,
				H5.H5Dget_type(id),
 				mspace,
 				fspace,
 				HDF5Constants.H5P_DEFAULT,
 				bdata);
			for (int i=0; i < no_lines; i++)
			{
				Array.set(data, i, (new String(bdata[i])));
			}
		}
		else
		{
			H5.H5Dread(id,
				H5.H5Dget_type(id),//datatype.getDatatype(),
				mspace,
				fspace,
				HDF5Constants.H5P_DEFAULT,
				data);

			// convert unsigned data because Java does not support unsigned integers
			boolean isUnsigned = false;
			int tid = datatype.getID();
			int class_t = H5.H5Tget_class(tid);
			if ( class_t == HDF5Constants.H5T_INTEGER)
			{
        		if (H5.H5Tget_sign(tid)==HDF5Constants.H5T_SGN_NONE)
				{
					Object new_data = convertUnsignedData(data);
					data = new_data;
				}
			}
		}

		return data;
	}

	/** readCompoundData only works for flat compound, i.e.
		all structure members are primitive data type. It does
		not work for that case that member structures are arrays
		or compound data types
	 */
	private Object readCompoundData(int mspace, int fspace)
		throws HDF5Exception, NullPointerException
	{
		//System.out.println("HDF5Dataset.readCompoundData called.");

		String member_name = "";
		int member_tid = -1;
		int read_tid = -1;
		int member_class_t = -1;
		int member_class_s = -1;
		int member_sign = -1;
		int p = HDF5Constants.H5P_DEFAULT;
		Object member_data = null;

		int size = 0;
		long lsize = 1;
		if (count == null ) return null;
		for (int i=0; i<count.length; i++) lsize *= count[i];
		size = (int)lsize;

		int tid = datatype.getID();
		int num_members = H5.H5Tget_nmembers(tid);

		Vector theData = new Vector();
		member_names = new Vector();

		for (int i=0; i<num_members; i++)
		{
			member_data = null;
			member_sign = -1;
			member_name = H5.H5Tget_member_name(tid, i);
			member_tid = H5.H5Tget_member_type(tid, i);
			member_class_t = H5.H5Tget_class(member_tid);
			member_class_s = H5.H5Tget_size(member_tid);
	   	read_tid = H5.H5Tcreate(HDF5Constants.H5T_COMPOUND,member_class_s);

			if (member_class_t == HDF5Constants.H5T_INTEGER) {
				member_sign = H5.H5Tget_sign(member_tid);
	  		if (member_class_s == 1) {
	  			byte[] bdata = new byte[size];
					H5.H5Tinsert(read_tid, member_name, 0, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT8));
					H5.H5Dread(id, read_tid, mspace, fspace, p, bdata);
					member_data = bdata;
	  		}
	  		else if (member_class_s == 2) {
	  			short[] sdata = new short[size];
					H5.H5Tinsert(read_tid, member_name, 0, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT16));
					H5.H5Dread(id, read_tid, mspace, fspace, p, sdata);
					member_data = sdata;
	  		}
	  		else if (member_class_s == 4) {
	  			int[] idata = new int[size];
					H5.H5Tinsert(read_tid, member_name, 0, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT32));
					H5.H5Dread(id, read_tid, mspace, fspace, p, idata);
					member_data = idata;
	  		}
	  		else if (member_class_s == 8) {
	  			long[] ldata = new long[size];
					H5.H5Tinsert(read_tid, member_name, 0, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT64));
					H5.H5Dread(id, read_tid, mspace, fspace, p, ldata);
					member_data = ldata;
	  		}
			}
			else if (member_class_t == HDF5Constants.H5T_FLOAT) {
  			if (member_class_s == 4) {
  				float[] fdata = new float[size];
					H5.H5Tinsert(read_tid, member_name, 0, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_FLOAT));
					H5.H5Dread(id, read_tid, mspace, fspace, p, fdata);
					member_data = fdata;
  			}
  			else if (member_class_s == 8) {
  				double[] ddata = new double[size];
					H5.H5Tinsert(read_tid, member_name, 0, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_DOUBLE));
					H5.H5Dread(id, read_tid, mspace, fspace, p, ddata);
					member_data = ddata;
  			}
			}
			else {
				member_data = null;
	  	} // end of switch (member_class_t)

			if (member_data != null)
			{
				member_names.add(member_name);
				if (member_sign == HDF5Constants.H5T_SGN_NONE)
			  		theData.add(convertUnsignedData(member_data));
			  	else
		  			theData.add(member_data);
			}

  	} // end of for (int i=0; i<num_members; i++)

		return theData;
	}

	/**
	  convert unsigned data because Java does not support unsigned integers.
	 */
	public static Object convertUnsignedData(Object data_in)
		throws HDF5Exception
	{
		Object data_out = null;
		String cname = data_in.getClass().getName();
		char dname = cname.charAt(cname.lastIndexOf("[")+1);
		int size = Array.getLength(data_in);

		if (dname == 'B') {
			short[] sdata = new short[size];
			short value = 0;
			for (int i=0; i<size; i++)
			{
				value = (short)Array.getByte(data_in, i);
				if (value < 0) value += 256;
				sdata[i] = value;
			}
			data_out = sdata;
			data_in = null;
		}
		else if (dname == 'S') {
			int[] idata = new int[size];
			int value = 0;
			for (int i=0; i<size; i++)
			{
				value = (int)Array.getShort(data_in, i);
				if (value < 0) value += 65536;
				idata[i] = value;
			}
			data_out = idata;
			data_in = null;
		}
		else if (dname == 'I') {
			long[] ldata = new long[size];
			long value = 0;
			for (int i=0; i<size; i++)
			{
				value = (long)Array.getInt(data_in, i);
				if (value < 0) value += 4294967296L;
				ldata[i] = value;
			}
			data_out = ldata;
			data_in = null;
		}
		else data_out = data_in;
		// Java does not support unsigned long

		return data_out;
	}

	/** write the entire dataset from file */
	public void writeData(Object buf) throws HDF5Exception, NullPointerException
	{
		int space = HDF5Constants.H5S_ALL;
		writeData(space, space, buf);
	}

	/** write the data with specified memory and file space */
	public void writeData(int mspace, int fspace, Object buf)
		throws HDF5Exception, NullPointerException
	{
		// write dataset
		int num_type=-1, plist=HDF5Constants.H5P_DEFAULT;
		String cname = buf.getClass().getName();
		char dname = cname.charAt(cname.lastIndexOf("[")+1);

		if (dname == 'B')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT8);
		else if (dname == 'C')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_CHAR);
		else if (dname == 'D')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_DOUBLE);
		else if (dname == 'F')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_FLOAT);
		else if (dname == 'I')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT32);
		else if (dname == 'J')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT64);
		else if (dname == 'S')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_INT16);
		else if (dname == 'Z')
			num_type = H5.J2C(HDF5CDataTypes.JH5T_NATIVE_HBOOL);
		else
			num_type = H5.H5Dget_type(id);

		int status = H5.H5Dwrite(id, num_type, mspace, fspace,
			plist, buf);
	}

	/** Returns the data array */
	public Object getData() { return data; }

	/** Returns the datatype */
	public HDF5Datatype getDatatype() { return datatype; }

	/** Returns the dataspace */
	public HDF5Dataspace getDataspace() { return dataspace; }

	/** Returns the rank of the dataset */
	public int getRank() { return rank; }

	/** Returns the dimensions of the dataset */
	public long[] getDims() { return dims; }

	/** Returns the maximum dimensions of the dataspace */
	public long[] getMaxdims() { return maxdims; }

	/** Returns the selected counts of the data */
	public long[] getCount() { return count; }

	/**

	 * Converts this object to a String representation.
	 * @return a string representation of this object
	 */
	public String toString() {
		if (datatype==null || dataspace==null)
			return super.toString();

		String d_str="";
		for (int i=0; i<rank; i++)
			d_str += dims[i]+"x";
		int l = d_str.length();
		if (l > 1) d_str = d_str.substring(0, l-1);

		return getClass().getName() +
			"[name=" + name +
			",type=" + datatype+
			",dimensions=" + d_str + "]";
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
		finally { H5.H5Dclose(id); }
	}

}


