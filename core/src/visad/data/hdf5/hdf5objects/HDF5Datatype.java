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

import ncsa.hdf.hdf5lib.*;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.*;

/**
 *  <p>
 *  This class provides a mechanism to describe the storage format of individual
 *  data points of a data set.
 *  <p>
 *  A data type is a collection of data type properties, all of which can be
 *  stored on disk, and which when taken as a whole, provide complete information
 *  for data conversion to or from that data type. The interface provides
 *  functions to set and query properties of a data type.
 *  <P>
 *  A data point is an instance of a data type, which is an instance of a type
 *  class. We have defined a set of type classes and properties which can be
 *  extended at a later time. The atomic type classes are those which describe
 *  types which cannot be decomposed at the data type interface level; all other
 *  classes are compound.
 *  <P>
 *  For details of the HDF5 libraries, see the HDF5 Documentation at:
 *  <a href="http://hdf.ncsa.uiuc.edu/HDF5/doc/">http://hdf.ncsa.uiuc.edu/HDF5/doc/</a>
 */

public class HDF5Datatype extends HDF5Object
{

	private static final long serialVersionUID = 1L;

	/** Construct an HDF5Datatype*/
	public HDF5Datatype ()
	{
		super();

		type = DATATYPE;
	}

	/**
	 * Creates a new HDF5Datatype
	 * @param datatype_class Class of datatype to create.
	 * @param size The number of bytes in the datatype to create.
	 */
	public HDF5Datatype (int datatype_class, int size)
	{
		super();

		type = DATATYPE;

		try {
			id = H5.H5Tcreate(datatype_class, size);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Datatype: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) {
			System.err.println("HDF5Datatype.init(): "+e);
		}
	}

	/**
	 * Opens a named HDF5Datatype
	 * @param loc_id A file, group, or datatype identifier.
	 * @param type_name A datatype name.
	 */
	public HDF5Datatype (int loc_id, String type_name)
	{
		super(type_name);

		type = DATATYPE;

		try {
			id = H5.H5Topen(loc_id, name);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Datatype: "+e);
			id = -1;
		}

	}

	/**

	 *  H5Tcommit commits a transient datatype to a file, turned it
	 *  into a named datatype.
	 *
	 *  @param loc_id A file or group identifier.
	 *  @param type_name A datatype name.
	 *  @return a non-negative value if successful; otherwise returns a negative value.
	**/
	public int H5Tcommit(int loc_id, String type_name)
		throws HDF5LibraryException, NullPointerException
	{
		name = type_name;
		return H5.H5Tcommit(loc_id, type_name, id);
	}


	/** Returns the datatype class identifier */
	public int get_class()
		throws HDF5LibraryException
	{
		return H5.H5Tget_class(id);
	}

	/** Returns  the size of a datatype in bytes*/
	public int get_size()
		throws HDF5LibraryException
	{
		return H5.H5Tget_size(id);
	}

	/** define the data with specified data type.
	 *  The maximum selected data size is limited to
	 *  Integer.MAX_VALUE, which is 2,147,483,647 bytes
	 *
	 *  @param count the number of points of data
	 */
	public Object defineData(long[] count) throws HDF5Exception
	{
		Object data = null;
		int size = 0;
		long lsize = 1;

		if (count == null ) return null;

		for (int i=0; i<count.length; i++)
		{
			lsize *= count[i];
			if (lsize > Integer.MAX_VALUE)
				throw (new OutOfMemoryError("the size of data array > "+Integer.MAX_VALUE));
		}

		size = (int)lsize;

		// data type information
		int class_t = H5.H5Tget_class(id);
		int class_s = H5.H5Tget_size(id);

		if (class_t == HDF5Constants.H5T_INTEGER) {
			if (class_s == 1) {
				data = new byte[size];
			}
			else if (class_s == 2) {
				data = new short[size];
			}
			else if (class_s == 4) {
				data = new int[size];
			}
			else if (class_s == 8) {
				data = new long[size];
			}
		}
		else if (class_t == HDF5Constants.H5T_FLOAT) {
			if (class_s == 4) {
				data = new float[size];
			}
			else if (class_s == 8) {
				data = new double[size];
			}
		}
		else if (class_t == HDF5Constants.H5T_STRING) {
			data = new String[size];
		}
		else if (class_t == HDF5Constants.H5T_COMPOUND) {
		} // end of switch (class_t)

		return data;
	}

	/** Gets the string representation of the data type
	 *  @param data_type the type of the data.
	 *  @return the string of the data type
	 */
	public static String getDatatype(int data_type)
	{
		if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_B16 ) ) return "HDF5CDataTypes.JH5T_ALPHA_B16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_B32 ) ) return "HDF5CDataTypes.JH5T_ALPHA_B32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_B64 ) ) return "HDF5CDataTypes.JH5T_ALPHA_B64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_B8 ) ) return "HDF5CDataTypes.JH5T_ALPHA_B8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_F32 ) ) return "HDF5CDataTypes.JH5T_ALPHA_F32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_F64 ) ) return "HDF5CDataTypes.JH5T_ALPHA_F64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_I16 ) ) return "HDF5CDataTypes.JH5T_ALPHA_I16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_I32 ) ) return "HDF5CDataTypes.JH5T_ALPHA_I32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_I64 ) ) return "HDF5CDataTypes.JH5T_ALPHA_I64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_I8 ) ) return "HDF5CDataTypes.JH5T_ALPHA_I8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_U16 ) ) return "HDF5CDataTypes.JH5T_ALPHA_U16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_U32 ) ) return "HDF5CDataTypes.JH5T_ALPHA_U32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_U64 ) ) return "HDF5CDataTypes.JH5T_ALPHA_U64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_ALPHA_U8 ) ) return "HDF5CDataTypes.JH5T_ALPHA_U8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_C_S1 ) ) return "HDF5CDataTypes.JH5T_C_S1";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_FORTRAN_S1 ) ) return "HDF5CDataTypes.JH5T_FORTRAN_S1";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_IEEE_F32BE ) ) return "HDF5CDataTypes.JH5T_IEEE_F32BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_IEEE_F32LE ) ) return "HDF5CDataTypes.JH5T_IEEE_F32LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_IEEE_F64BE ) ) return "HDF5CDataTypes.JH5T_IEEE_F64BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_IEEE_F64LE ) ) return "HDF5CDataTypes.JH5T_IEEE_F64LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_B16 ) ) return "HDF5CDataTypes.JH5T_INTEL_B16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_B32 ) ) return "HDF5CDataTypes.JH5T_INTEL_B32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_B64 ) ) return "HDF5CDataTypes.JH5T_INTEL_B64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_B8 ) ) return "HDF5CDataTypes.JH5T_INTEL_B8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_F32 ) ) return "HDF5CDataTypes.JH5T_INTEL_F32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_F64 ) ) return "HDF5CDataTypes.JH5T_INTEL_F64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_I16 ) ) return "HDF5CDataTypes.JH5T_INTEL_I16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_I32 ) ) return "HDF5CDataTypes.JH5T_INTEL_I32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_I64 ) ) return "HDF5CDataTypes.JH5T_INTEL_I64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_I8 ) ) return "HDF5CDataTypes.JH5T_INTEL_I8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_U16 ) ) return "HDF5CDataTypes.JH5T_INTEL_U16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_U32 ) ) return "HDF5CDataTypes.JH5T_INTEL_U32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_U64 ) ) return "HDF5CDataTypes.JH5T_INTEL_U64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_INTEL_U8 ) ) return "HDF5CDataTypes.JH5T_INTEL_U8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_B16 ) ) return "HDF5CDataTypes.JH5T_MIPS_B16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_B32 ) ) return "HDF5CDataTypes.JH5T_MIPS_B32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_B64 ) ) return "HDF5CDataTypes.JH5T_MIPS_B64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_B8 ) ) return "HDF5CDataTypes.JH5T_MIPS_B8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_F32 ) ) return "HDF5CDataTypes.JH5T_MIPS_F32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_F64 ) ) return "HDF5CDataTypes.JH5T_MIPS_F64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_I16 ) ) return "HDF5CDataTypes.JH5T_MIPS_I16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_I32 ) ) return "HDF5CDataTypes.JH5T_MIPS_I32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_I64 ) ) return "HDF5CDataTypes.JH5T_MIPS_I64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_I8 ) ) return "HDF5CDataTypes.JH5T_MIPS_I8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_U16 ) ) return "HDF5CDataTypes.JH5T_MIPS_U16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_U32 ) ) return "HDF5CDataTypes.JH5T_MIPS_U32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_U64  ) ) return "HDF5CDataTypes.JH5T_MIPS_U64 ";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_MIPS_U8 ) ) return "HDF5CDataTypes.JH5T_MIPS_U8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_B16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_B16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_B32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_B32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_B64 ) ) return "HDF5CDataTypes.JH5T_NATIVE_B64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_B8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_B8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_CHAR ) ) return "HDF5CDataTypes.JH5T_NATIVE_CHAR";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_DOUBLE ) ) return "HDF5CDataTypes.JH5T_NATIVE_DOUBLE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_FLOAT ) ) return "HDF5CDataTypes.JH5T_NATIVE_FLOAT";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_HBOOL ) ) return "HDF5CDataTypes.JH5T_NATIVE_HBOOL";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_HERR ) ) return "HDF5CDataTypes.JH5T_NATIVE_HERR";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_HSIZE ) ) return "HDF5CDataTypes.JH5T_NATIVE_HSIZE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_HSSIZE ) ) return "HDF5CDataTypes.JH5T_NATIVE_HSSIZE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_FAST16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_FAST16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_FAST32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_FAST32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_FAST64 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_FAST64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_FAST8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_FAST8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_LEAST16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_LEAST16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_LEAST32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_LEAST32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_LEAST64 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_LEAST64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT_LEAST8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT_LEAST8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT64 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_INT8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_INT8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_LDOUBLE ) ) return "HDF5CDataTypes.JH5T_NATIVE_LDOUBLE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_LLONG ) ) return "HDF5CDataTypes.JH5T_NATIVE_LLONG";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_LONG ) ) return "HDF5CDataTypes.JH5T_NATIVE_LONG";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_OPAQUE ) ) return "HDF5CDataTypes.JH5T_NATIVE_OPAQUE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_SCHAR ) ) return "HDF5CDataTypes.JH5T_NATIVE_SCHAR";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_SHORT ) ) return "HDF5CDataTypes.JH5T_NATIVE_SHORT";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UCHAR ) ) return "HDF5CDataTypes.JH5T_NATIVE_UCHAR";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_FAST16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_FAST16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_FAST32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_FAST32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_FAST64 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_FAST64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_FAST8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_FAST8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST64 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST64";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT_LEAST8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT16 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT16";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT32 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT32";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT64  ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT64 ";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_UINT8 ) ) return "HDF5CDataTypes.JH5T_NATIVE_UINT8";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_ULLONG ) ) return "HDF5CDataTypes.JH5T_NATIVE_ULLONG";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_ULONG ) ) return "HDF5CDataTypes.JH5T_NATIVE_ULONG";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NATIVE_USHORT ) ) return "HDF5CDataTypes.JH5T_NATIVE_USHORT";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NCSET ) ) return "HDF5CDataTypes.JH5T_NCSET";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_NSTR ) ) return "HDF5CDataTypes.JH5T_NSTR";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B16BE ) ) return "HDF5CDataTypes.JH5T_STD_B16BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B16LE ) ) return "HDF5CDataTypes.JH5T_STD_B16LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B32BE ) ) return "HDF5CDataTypes.JH5T_STD_B32BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B32LE ) ) return "HDF5CDataTypes.JH5T_STD_B32LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B64BE ) ) return "HDF5CDataTypes.JH5T_STD_B64BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B64LE ) ) return "HDF5CDataTypes.JH5T_STD_B64LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B8BE ) ) return "HDF5CDataTypes.JH5T_STD_B8BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_B8LE ) ) return "HDF5CDataTypes.JH5T_STD_B8LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I16BE ) ) return "HDF5CDataTypes.JH5T_STD_I16BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I16LE ) ) return "HDF5CDataTypes.JH5T_STD_I16LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I32BE ) ) return "HDF5CDataTypes.JH5T_STD_I32BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I32LE ) ) return "HDF5CDataTypes.JH5T_STD_I32LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I64BE ) ) return "HDF5CDataTypes.JH5T_STD_I64BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I64LE ) ) return "HDF5CDataTypes.JH5T_STD_I64LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I8BE ) ) return "HDF5CDataTypes.JH5T_STD_I8BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_I8LE ) ) return "HDF5CDataTypes.JH5T_STD_I8LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_REF_DSETREG ) ) return "HDF5CDataTypes.JH5T_STD_REF_DSETREG";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_REF_OBJ ) ) return "HDF5CDataTypes.JH5T_STD_REF_OBJ";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U16BE ) ) return "HDF5CDataTypes.JH5T_STD_U16BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U16LE ) ) return "HDF5CDataTypes.JH5T_STD_U16LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U32BE ) ) return "HDF5CDataTypes.JH5T_STD_U32BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U32LE ) ) return "HDF5CDataTypes.JH5T_STD_U32LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U64BE ) ) return "HDF5CDataTypes.JH5T_STD_U64BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U64LE ) ) return "HDF5CDataTypes.JH5T_STD_U64LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U8BE ) ) return "HDF5CDataTypes.JH5T_STD_U8BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_STD_U8LE ) ) return "HDF5CDataTypes.JH5T_STD_U8LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_UNIX_D32BE ) ) return "HDF5CDataTypes.JH5T_UNIX_D32BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_UNIX_D32LE ) ) return "HDF5CDataTypes.JH5T_UNIX_D32LE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_UNIX_D64BE ) ) return "HDF5CDataTypes.JH5T_UNIX_D64BE";
		else if ( data_type == H5.J2C( HDF5CDataTypes.JH5T_UNIX_D64LE ) ) return "HDF5CDataTypes.JH5T_UNIX_D64LE";
		else if ( data_type == H5.J2C(HDF5CDataTypes.JH5T_NATIVE_OPAQUE)) return "H5T_NATIVE_OPAQUE";
		else return "Unknown";
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
		finally { H5.H5Tclose(id); }
	}

}







