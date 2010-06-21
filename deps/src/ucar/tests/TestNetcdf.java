/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */


package ucar.tests;
import ucar.netcdf.*;
import java.io.IOException;
import java.lang.reflect.Array;
import ucar.multiarray.MultiArray;
import ucar.multiarray.MultiArrayImpl;
import ucar.multiarray.ArrayMultiArray;
import ucar.multiarray.MultiArrayProxy;
import ucar.multiarray.SliceMap;


/**
 * @author $Author: dglo $
 * @version $Revision: 1.2 $ $Date: 2000-08-30 18:55:52 $
 */

public class TestNetcdf {

public static String
MultiArrayToString(MultiArray ma) {
	StringBuffer buf = new StringBuffer();
	final int rank = ma.getRank();
	if (rank > 0)
	{
		buf.append("{\n\t");
		final int [] dims = ma.getLengths();
		final int last = dims[0] -1;
		for(int ii = 0; ii <= last; ii++)
		{
			final MultiArray inner =
				new MultiArrayProxy(ma,
					new SliceMap(0, ii));
			buf.append(MultiArrayToString(inner));
			if(ii != last)
				buf.append(", ");
		}
		buf.append("\n}");
	}
	else
	{
		try {
		buf.append(ma.get((int [])null));
		} catch (IOException ee) {}
	}
	return buf.toString();
}


public static void
main(String[] args) {

	String fname;
	if(args.length == 0) {
		fname = new String("t.nc");
	}
	else
	{
		fname = args[args.length -1];
	}
	System.out.print("path: ");
	System.out.println(fname);

	try {
		Schema schema = new Schema();

		// scalar variable
		schema.put( new ProtoVariable("t1",
			Integer.TYPE,
			(Dimension [])null));
 

		UnlimitedDimension timeD = new UnlimitedDimension("time");
		Dimension latD = new Dimension("lat", 45);
		Dimension lonD = new Dimension("lon", 90);
		Dimension hgtD = new Dimension("level", 12);

		schema.put( new ProtoVariable(timeD.getName(), Double.TYPE,
			 timeD));
		schema.put( new ProtoVariable(latD.getName(), Byte.TYPE,
			 latD));
		schema.put( new ProtoVariable(lonD.getName(), Short.TYPE,
			 lonD));
		schema.put(new ProtoVariable(hgtD.getName(), Integer.TYPE,
			 hgtD));

		ProtoVariable temperatureV;
		{
		Dimension[] Tdims = new Dimension[4];
		Tdims[0] = timeD;
		Tdims[1] = hgtD;
		Tdims[2] = latD;
		Tdims[3] = lonD;

		temperatureV = new ProtoVariable("temperature",
				Float.TYPE,
				Tdims);

		AttributeSet Vattrs = temperatureV.getAttributes();
		Vattrs.put( new Attribute("units", "degree_Celsius"));
		double [] vr = { -100., 200.};
		Vattrs.put( new Attribute("valid_range", vr));
		Vattrs.put( new Attribute("missing_value", new Double(-999)));
		Vattrs.put( new Attribute("_FillValue", -9999));

		schema.put(temperatureV);
		}

		schema.putAttribute(new Attribute("conventions", "none"));

		NetcdfFile nc =
			 new NetcdfFile(fname, true, true, schema);
	/*	System.out.println(nc); */

		{
			final int nlats = latD.getLength();
			byte [] lats = new byte[nlats];
			byte lat = (byte)(-nlats/2);
			for(int ii = 0; ii < nlats; ii++, lat++)
				lats[ii] = lat;
			int [] origin = {0};
			nc.get(latD.getName()).copyin(origin,
				new ArrayMultiArray(lats));
		}
		{
			final int nlons = lonD.getLength();
			short [] lons = new short[nlons];
			short lon = (short)(-(nlons/2 -1));
			for(int ii = 0; ii < nlons; ii++, lon++)
				lons[ii] = lon;
			int [] origin = {0};
			nc.get(lonD.getName()).copyin(origin,
				new ArrayMultiArray(lons));
		}
		{
			int [] hgts = {1000, 925, 850, 700, 500,
				 400, 300, 250, 200, 150, 100, 50};
			int [] origin = {0};
			nc.get(hgtD.getName()).copyin(origin,
				new ArrayMultiArray(hgts));
		}

		{
			Variable tao = nc.get(timeD.getName());
			double time = 0.;
			int [] index = { 0, 0, 0, 0};
			for(; index[0] < 5; index[0]++, time += 60.) {
				if(index[0] == 1) // don't fill record 1
					nc.setFill(false);
				tao.setDouble(index, time);
				if(index[0] == 1)
					nc.setFill(true);
			}
		}

		nc.close();


	} catch (Exception ee) {
		System.out.println(ee);
	}

	try {
		NetcdfFile nc =
			 new NetcdfFile(fname, false);
		System.out.println(nc);

		Variable timeV = nc.get("time");
		int[] origin = new int[timeV.getRank()];
		int[] extent  = timeV.getLengths();
		MultiArray times = timeV.copyout(origin, extent);
		System.out.println(MultiArrayToString(times));

		nc.close();

	} catch (Exception ee) {
		System.out.println(ee);
	}

// System.exit(0);

	try {
	fname = new String("test.nc");
		NetcdfFile nc =
			 new NetcdfFile(fname, false);
	System.out.println(nc);

	
		Variable ma = nc.get("Float");
		int [] origin = {1, 1, 1};
		int [] extent = {2, 3, 6};
		MultiArray flts = ma.copyout(origin, extent);
// System.out.println(MultiArrayToString(flts));

		nc.close();
		
		final Schema sc2 = new Schema(nc);
		final NetcdfFile clone = new NetcdfFile("clone.nc", true, true,
				sc2);
		ma = clone.get(ma.getName());
		ma.copyin(origin, flts);
		MultiArray cflts = ma.copyout(origin, extent);
System.out.println(MultiArrayToString(cflts));
		MultiArray cflts2 = new MultiArrayImpl(extent,
			ma.toArray(new float[36], origin, extent));
System.out.println(MultiArrayToString(cflts2));

	} catch (Exception ee) {
		System.out.println(ee);
	}

}
}
