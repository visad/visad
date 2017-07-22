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

import java.lang.reflect.Array;
import java.awt.image.*;
import java.rmi.RemoteException;
import visad.data.hdf5.hdf5objects.*;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import visad.*;

/**
	HDF5DatasetAdapted is the implementation of mapping from HDFDataset to
    VisAD Data object. HDF5 Dataset with compound data type is mapped to
    VisAD FieldImpl and HDF5 Dataset with simple data type is mapped to
    FlatField. The current implementation does not include compound datatype.
    <P> 
 */
public class HDF5DatasetAdapted
	extends HDF5Dataset
	implements HDF5DataAdaptable
{
	MathType field_type;
	RealTupleType domain, range;
	DataImpl dataField;

	/** Constructs a HDF5DatasetAdapted */
	public HDF5DatasetAdapted()
	{
		super();
	}

	/** Creates a dataset at the specified location
	 *  @param loc_id Identifier of the file or group to create the dataset within.
	 *  @param set_name The name of the dataset to create.
	 *  @param type_id Identifier of the datatype to use when creating the dataset.
	 *  @param space_id Identifier of the dataspace to use when creating the dataset.
	 *  @param create_plist_id Identifier of the set creation property list.
	 */
	public HDF5DatasetAdapted(int loc_id, String set_name, int type_id, int space_id,
		int create_plist_id)
	{
		super(loc_id, set_name, type_id, space_id, create_plist_id);
	}

	/**
	 * Opens a HDF5DatasetAdapted
	 * @param loc_id A file, group, or datatype identifier.
	 * @param set_name A datatset name.
	 */
	public HDF5DatasetAdapted (int loc_id, String set_name)
	{
		super(loc_id, set_name);
	}

	/** initialize the HDF5DatasetAdapte:
	    <OL>
			<LI> Set the domain.
			<LI> Set the range.
			<LI> Set function type.
		</OL>
	 */
	public void init () throws HDF5Exception
	{
		super.init();

		if (rank <= 0) return;

		if (data == null)
		{
			try { data = readData(); }
			catch (Exception e) { System.err.println(e); }
		}


		// check if the dataset is an image
		boolean isImage = false;
		try {
			int pal_id = H5.H5Aopen_name(id, "PALETTE");
			int pal_type = H5.H5Aget_type( pal_id );
			int pal_class = H5.H5Tget_class( pal_type );
			if (pal_class == HDF5Constants.H5T_REFERENCE) isImage = true;
			H5.H5Tclose(pal_type);
			H5.H5Aclose(pal_id);
		} catch (Exception e) { 
		}

		if (data.getClass().getName().endsWith("Ljava.lang.String;")) {

			mapToText();
		} else if (isImage)
		{
			try { mapToImage(); }
			catch (Exception e ) { throw new HDF5Exception(e.toString()); }
		}
		else {
			mapToFlatField();
		}
	}

	/** map the HDF5 dataset into an Image */
	private void mapToImage()  throws HDF5Exception, VisADException
	{

		int w = (int)dims[0];
		int h = (int)dims[1];

		int aid = H5.H5Aopen_name(id, "PALETTE");
		byte [] ref_buf = new byte[8];
		int atype = H5.H5Aget_type(aid);
		H5.H5Aread( aid, atype, ref_buf);
		H5.H5Tclose(atype);
 		int pal_id =  H5.H5Rdereference(id, HDF5Constants.H5R_OBJECT, ref_buf);
		HDF5Datatype pal_datatype = new HDF5Datatype();
		pal_datatype.setID(H5.H5Dget_type(pal_id));

		HDF5Dataspace pal_dataspace = new HDF5Dataspace();
		pal_dataspace.setID(H5.H5Dget_space(pal_id));

		// load the image palette
		byte[] palette = new byte[3*256];
		H5.H5Dread(pal_id,
			H5.H5Dget_type(pal_id),
			HDF5Constants.H5S_ALL,
			HDF5Constants.H5S_ALL,
			HDF5Constants.H5P_DEFAULT,
			palette);

		// red, green and blue
		byte[] r   = new byte[256];
		byte[] g = new byte[256];
		byte[] b  = new byte[256];

		for (int i = 0; i < 256; i++)
		{
			r[i] = palette[3*i];
			g[i] = palette[3*i+1];
			b[i] = palette[3*i+2];
		}

		IndexColorModel icm = new IndexColorModel (8, 256, r, g, b,0);

		int num_pixels = w*h;
		int pixel_val = -1;
		float[][] pixel_rgb = new float[3][num_pixels];
		for (int i=0; i<num_pixels; i++)
		{
			pixel_val =  Integer.parseInt(Array.get(data,i).toString());
			pixel_rgb[0][i] = (float)icm.getRed(pixel_val);
			pixel_rgb[1][i] = (float)icm.getGreen(pixel_val);
			pixel_rgb[2][i] = (float)icm.getBlue(pixel_val);
		}

		// construct FlatField for the image data

		RealType line, element, c_red, c_green, c_blue;

                line = RealType.getRealType("ImageLine");
                element = RealType.getRealType("ImageElement");
                c_red = RealType.getRealType("Red");
                c_green = RealType.getRealType("Green");
                c_blue = RealType.getRealType("Blue");

		RealType[] c_all = {c_red, c_green, c_blue};
		RealTupleType radiance = new RealTupleType(c_all);

		RealType[] domain_components = {element, line};
		RealTupleType image_domain = new RealTupleType(domain_components);
		Linear2DSet domain_set = new Linear2DSet(image_domain,
			0.0, (float) (w - 1.0), w, (float) (h - 1.0), 0.0, h);
		FunctionType image_type = new FunctionType(image_domain, radiance);

		FlatField image_field = new FlatField(image_type, domain_set);
		try { image_field.setSamples(pixel_rgb, false); }
		catch (RemoteException e) { throw new HDF5Exception("setSamples for image failed."); }

		field_type = image_type;
		domain = image_domain;
		range = radiance;
		dataField = image_field;

		H5.H5Aclose(aid);
	}

	/** map the HDF5 String to ViasAD Text */
	private void mapToText()  throws HDF5Exception
	{
		String text = "";
		if (data.getClass().isArray())
		{
			int no_lines = Array.getLength(data);
			for (int i=0; i<no_lines; i++)
			{
				text += "\n"+Array.get(data, i).toString();
			}
		}
		else
			text = data.toString();

		TextType tt = null;

		String tname = (new String(shortName)).replace('-', '_');
		try {
			tt = new TextType( tname );
		}
		catch (TypeException e) {
			tt = (TextType)TextType.getScalarTypeByName(tname);
		}
	   	catch (VisADException e) {
			throw new HDF5Exception(e.toString());
		}

		try {
			dataField = new Text(tt, text);
			field_type = dataField.getType();
		}  catch (VisADException e) {{ throw new HDF5Exception(e.toString());}}
	}

	/** map HDF dataset to FlatField */
	private void mapToFlatField() throws HDF5Exception
	{
		// make the domain for the dataset
		try
		{
			RealType dimension_types[] = new RealType[rank];
			String dname = "";

			for (int i=0; i<rank; i++)
			{
				dname = "dim"+String.valueOf(i);
                                dimension_types[i] = RealType.getRealType(dname);
			}
			domain = new RealTupleType(dimension_types);
		} catch (VisADException e)
		{
			throw new HDF5Exception("Constructing the domain of HDF5DatasetAdapted failed. "+e);
		}

		// make the range for the data set
		try
		{
			RealType[] range_types = null;
			String rname = "";

			// compound datatype
			if (data instanceof java.util.Vector)
			{
				int num_members = member_names.size();
				range_types = new RealType[num_members];
				for (int i=0; i<num_members; i++)
				{
					rname = (String)member_names.elementAt(i);
                                        range_types[i] = RealType.getRealType(rname);
				}
			}
			else
			{
				range_types = new RealType[1];
				rname = (new String(shortName)).replace('-', '_');
                                range_types[0] = RealType.getRealType(rname);
			}
			range = new RealTupleType(range_types);
		} catch (VisADException e)
		{
			throw new HDF5Exception("Constructing the range of HDF5DatasetAdapted failed. "+e);
		}

		// set the function type
		try
		{
			field_type = new FunctionType(domain, range);
 		} catch (VisADException e)
		{
			throw new HDF5Exception("Constructing the field_type of HDF5DatasetAdapted failed. "+e);
		}

		// set the data field
		try { dataField = defineDataField(); }
		catch (Exception e)
		{
			throw new HDF5Exception("Constructing the data field of HDF5DatasetAdapted failed. "+e);
		}
	}

	private FieldImpl defineDataField()
	throws HDF5Exception, VisADException, RemoteException
	{
		FlatField ff = null;
		boolean isSupportedType = false;
		IntegerNDSet domain_set = null;
		int data_type = datatype.get_class();

		isSupportedType = (data_type == HDF5Constants.H5T_INTEGER ||
			  data_type == HDF5Constants.H5T_FLOAT ||
			  (data_type == HDF5Constants.H5T_COMPOUND && member_names.size()>0));

		if ( isSupportedType)
		{
			int[] d = new int[rank];
			for (int i=0; i<rank; i++) d[i] = (int)dims[i];
			domain_set = new IntegerNDSet(domain, d);
			ff = new FlatField((FunctionType)field_type, domain_set);
		}
		else
		{
			throw new HDF5AdapterException("constructing data field.");
		}

		if (data != null)
		{
			int number_of_range_components = 0;
			int number_of_range_samples = 0;

			if (data_type == HDF5Constants.H5T_COMPOUND)
			{
				java.util.Vector cdata = (java.util.Vector)data;
				number_of_range_components = member_names.size();
				number_of_range_samples = Array.getLength(cdata.elementAt(0));
			}
			else
			{
				number_of_range_components = 1;
				number_of_range_samples = Array.getLength(data);
			}

			float[][] theRange = new float[number_of_range_components][number_of_range_samples];
			Object theData = null;
			for (int i=0; i<number_of_range_samples; i++)
			{
				if (data_type == HDF5Constants.H5T_COMPOUND)
				{
					java.util.Vector cdata = (java.util.Vector)data;
					for (int k=0; k<number_of_range_components; k++)
					{
						theData = cdata.elementAt(k);
						theRange[k][i] = Float.parseFloat(Array.get(theData, i).toString());
					}
				}
				else
					theRange[0][i] = Float.parseFloat(Array.get(data, i).toString());
			}
			ff.setSamples(theRange, false);
		}

		return ff;
	}

	public MathType getMathType() throws VisADException
	{
		return field_type;
	}

	public DataImpl getAdaptedData() throws VisADException, RemoteException
	{
		return (DataImpl)dataField;
	}

	public DataImpl getAdaptedData(int[] indexes) throws VisADException, RemoteException
	{
		return (DataImpl)dataField;
	}

}
