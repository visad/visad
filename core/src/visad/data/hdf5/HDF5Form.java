/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import visad.*;
import visad.data.*;
import visad.UnimplementedException;
import java.rmi.*;
import java.net.URL;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.HDF5CDataTypes;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
	HDF5Form is a data form adapter for HDF5 files. 
 */
public class HDF5Form
	extends Form
	implements FormFileInformer
{
	public HDF5Form()
	{
		this( "HDF5 Data" );
	}

	public HDF5Form(String name )
	{
		super( name );
	}

	public boolean isThisType(String name)
	{
		boolean retVal = false;

		try { retVal = H5.H5Fis_hdf5(name); }
		catch (Exception ex) {;}

		return retVal;
	}

	public boolean isThisType(byte[] block)
	{
		int firstByte = (new Byte(block[0]).intValue());
		if (firstByte != 137)
			return false;

		String bytes2to4 = new String( block, 1, 4 );
		return bytes2to4.startsWith("HDF");
	}

	public String[] getDefaultSuffixes()
	{
		String[] suffs = { "hdf", "h5"};
		return suffs;
	}

	public FormNode getForms( Data data )
	{
		return this;
	}

	public DataImpl open( String file_path )
		throws VisADException, RemoteException
	{
		HDF5FileAdapted file = null;

		try {
			file = new HDF5FileAdapted(
				file_path,
				HDF5Constants.H5F_ACC_RDONLY,
				HDF5Constants.H5P_DEFAULT );
		} catch (HDF5Exception e) {System.err.println(e); }

		return getFileData( file );
	}

	public DataImpl open( URL url )
		throws VisADException
	{
		throw new UnimplementedException( "HDF5Form.open( URL )" );
	}

	public void add( String id, Data data, boolean replace )
		throws BadFormException
	{
		throw new BadFormException( "HDF5Form.add( String, Data, boolean )" );
	}

	public void save( String filename, Data data, boolean replace )
		throws BadFormException, RemoteException, VisADException
	{
		//System.out.println("\n\nHDF5Form.save called.");

		int fid=0, did=0, gid=0;

		try {
			fid = H5.H5Fcreate(filename,
				HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT,
				HDF5Constants.H5P_DEFAULT);
		} catch (HDF5Exception e) {
			throw new HDF5AdapterException(
			"HDF5Form.save() failed: cannot create file "+filename);
		} catch (NoClassDefFoundError e) {
			throw new HDF5AdapterException(
			"HDF5Form.save() failed: cannot create file "+filename);
                }

		try { save(fid, data, 0, 0); }
		catch (BadFormException e) {
			throw e;
		}
		catch (RemoteException e) {
			throw e;
		}
		catch (VisADException e) {
			throw e;
		}
		catch (HDF5Exception e) {
			throw new HDF5AdapterException(e.toString());
		}
		finally {
			try{H5.H5Fclose(fid);} catch (Exception e) {}
		}
	}

	/** Save only tuple and field.
		Tuple is mapped to HDF5 groups and Field is mapped to
		HDF5 dataset. Only the first range value, i.e. value[][0]
		is written to the output file. We don't know how to deal
		with compound data.
	 */
	private void save( int pid, Data data, int level, int index)
		throws BadFormException, RemoteException, VisADException, HDF5Exception
	{
		if (data instanceof Tuple)
		{
			int g_idx=0, new_pid=0;
			Data d = null;
			Tuple tuple = (Tuple)data;

			String gname = "Group"+index+"at"+level;

			if (level==0 )
			{
				new_pid=pid;
                g_idx = -1;
			}
			else
			{
				new_pid = H5.H5Gcreate(pid, gname, -1);
			}

			int	n = tuple.getDimension();
			for (int i = 0; i < n; i++)
			{
				d = tuple.getComponent(i);
				//if (data instanceof Tuple) g_idx++;
				save(new_pid, d, level+1, g_idx++);
			}
		}
		else if (data instanceof Field)
		{
			Field field = (Field)data;
			RealType[] rTypes = ((FunctionType) field.getType()).getRealComponents();

			Set dset = field.getDomainSet();
			if (!(dset instanceof GriddedSet) ||
				rTypes == null)
				return;

			GriddedSet domain = (GriddedSet)dset;
			RealType rangeType = (RealType) rTypes[0];

			int sid=0, did=0, tid=0;
			int l = domain.getLength();
			int[] ddims = domain.getLengths();
			int rank = ddims.length;
			long[] dims = new long[rank];
			for (int i=0; i<rank; i++)
				dims[i] = ddims[i];
			sid = H5.H5Screate_simple(rank, dims, null);

			int number_of_range_components = 1;
			if (field.isFlatField())
				number_of_range_components = ((FlatField)field).getRangeDimension();
			else
				number_of_range_components = ((Unit[]) field.getDefaultRangeUnits()).length;
			float[] rangeValues = new float[l];
			float[][] rValue = field.getFloats(false);

 			if (number_of_range_components==1)
			{
				for (int i=0; i<l; i++)
					rangeValues[i] = rValue[0][i];
				try {
					did = H5.H5Dcreate(pid, rangeType.getName(),
						H5.J2C(HDF5CDataTypes.JH5T_NATIVE_FLOAT),
						sid, HDF5Constants.H5P_DEFAULT);
					H5.H5Dwrite(did,
						H5.J2C(HDF5CDataTypes.JH5T_NATIVE_FLOAT),
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT,
						rangeValues);
				} finally {
					H5.H5Dclose(did);
					H5.H5Sclose(sid);
				}
			}
			else // write compound data
			{
				float[][] fValue = new float[l][number_of_range_components];
				for (int i=0; i<l; i++)
				{
					for (int j=0; j<number_of_range_components; j++)
						fValue[i][j] = rValue[j][i];
				}

				try {
					tid = H5.H5Tcreate(HDF5Constants.H5T_COMPOUND,number_of_range_components*4);
					for (int j=0; j<number_of_range_components; j++)
					{
						rangeType = (RealType) rTypes[j];
						H5.H5Tinsert(tid, rangeType.getName(), j*4, H5.J2C(HDF5CDataTypes.JH5T_NATIVE_FLOAT));
					}
					String dname = "Compound"+index+"at"+level;
					did = H5.H5Dcreate(pid, dname, tid, sid, HDF5Constants.H5P_DEFAULT);
					H5.H5Dwrite(did, tid, sid, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,fValue);
				} finally {
					H5.H5Tclose(tid);
					H5.H5Dclose(did);
					H5.H5Sclose(sid);
				}
			}
		}
		else if (data instanceof Text)
		{
			Text text = (Text) data;
			String text_value = text.getValue();
			TextType tt = (TextType)text.getType();
			String text_name = tt.getName();

			int max_length=text_value.length();
			long[] dims_str = {1};
			int dataspace = H5.H5Screate_simple(1, dims_str, null);

			int datatype = H5.H5Tcopy(H5.J2C(HDF5CDataTypes.JH5T_C_S1));
			H5.H5Tset_size(datatype,max_length);
			H5.H5Tset_strpad(datatype,HDF5Constants.H5T_STR_NULLPAD);
			int dataset = H5.H5Dcreate(pid, text_name,
				datatype, dataspace, HDF5Constants.H5P_DEFAULT);
			byte [][] bnotes = new byte[1][max_length];
				bnotes[0] = text_value.getBytes();
			H5.H5Dwrite(dataset,
				datatype,
				HDF5Constants.H5S_ALL,
				HDF5Constants.H5S_ALL,
				HDF5Constants.H5P_DEFAULT,
				bnotes);
			H5.H5Dclose(dataset);
			bnotes = null;
		}
	}

	public MathType getMathType( HDF5FileAdapted file )
		throws VisADException, RemoteException
	{
		MathType mathType = null;
		HDF5DataAdaptable data = null;

		int n_structs = file.getObjectCount();
		if ( n_structs <= 0 )
		{
			throw new HDF5AdapterException("no data object in file: "+file.getName());
		}

		MathType[] types = new MathType[ n_structs ];

		for ( int i = 0; i < n_structs; i++ )
		{
			Object obj = file.getDataObject(i);

			if ( obj instanceof HDF5GroupAdapted )
			{
				data = (HDF5GroupAdapted)obj;
			}
			else if ( obj instanceof HDF5DatasetAdapted )
			{
				data = (HDF5DatasetAdapted)obj;
			}

			mathType = data.getMathType();
			types[i] = mathType;
		}

		TupleType t_type = new TupleType( types );

		return (MathType) t_type;
	}

	public DataImpl getFileData( HDF5FileAdapted file )
		throws VisADException, RemoteException
	{
//System.out.println("HDF5Form.getFileData() called");
		DataImpl data = null;
		HDF5DataAdaptable h5Data = null;

		int n_structs = file.getObjectCount();
		if ( n_structs <= 0 )
		{
			throw new HDF5AdapterException("no data object in file: "+file.getName());
		}

		HDF5DataAdaptable[] datas = new HDF5DataAdaptable[ n_structs ];

		// only deal with Groups and datasets
		int ndatas=0;
		for ( int i = 0; i < n_structs; i++ )
		{
			Object obj = file.getDataObject(i);

			if ( obj instanceof HDF5GroupAdapted )
			{
				datas[ndatas++] = (HDF5GroupAdapted)obj;
			}
			else if ( obj instanceof HDF5DatasetAdapted )
			{
				datas[ndatas++] = (HDF5DatasetAdapted)obj;
			}
		}

		return assembleStructs( datas );
	}

	private DataImpl assembleStructs( HDF5DataAdaptable[] h_datas )
		throws VisADException, RemoteException
	{
//System.out.println("HDF5Form.assembleStructs() called");
		DataImpl fileData = null;
		int n_structs = h_datas.length;

		if ( n_structs == 1 )
			return getVisADDataObject( h_datas[0] );

		boolean types_equal = true;
		MathType first_type = null;
		MathType[] types = new MathType[ n_structs ];
		DataImpl[] datas = new DataImpl[ n_structs ];

		datas[0] = getVisADDataObject( h_datas[0] );
		types[0] = datas[0].getType();
		first_type = types[0];

		for ( int i = 1; i < n_structs; i++ )
		{
			datas[i] = getVisADDataObject( h_datas[i] );
			types[i] = datas[i].getType();
			types_equal = types[i].equals(first_type);
		}

		if ( types_equal )
		{
			RealType struct_id = RealType.getRealType("struct_id");
			Integer1DSet domain = new Integer1DSet(struct_id, n_structs);
			FunctionType fType = new FunctionType((MathType) struct_id, first_type);
			FieldImpl field = new FieldImpl(fType, domain);

			for ( int i = 0; i < n_structs; i++ )
				field.setSample(i, datas[i]);

			fileData = field;
		} else {
			TupleType t_type = new TupleType( types );
			fileData = new Tuple( t_type, datas, false );
		}

		return fileData;
	}

	public DataImpl getVisADDataObject( HDF5DataAdaptable h_data )
		throws VisADException, RemoteException
	{
		return h_data.getAdaptedData();
	}

}
