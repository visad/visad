import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.io.IOException;
import java.rmi.RemoteException;

import visad.*;

import visad.data.netcdf.Plain;

import visad.java3d.DisplayImplJ3D;

public class Test10
	extends TestSkeleton
{
  private String fileName = null;

  public Test10() { }

  public Test10(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  int checkExtraKeyword(int argc, String args[])
  {
    if (fileName == null) {
      fileName = args[argc];
    } else {
      System.err.println("Ignoring extra filename \"" + args[argc] + "\"");
    }

    return 1;
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    Unit super_degree = CommonUnit.degree.scale(2.5);
    RealType lon = new RealType("lon", super_degree, null);

    if (fileName == null) {
      System.err.println("must specify netCDF file name");
      System.exit(1);
      return null;
    }
    // "pmsl.nc"

    Plain plain = new Plain();
    FlatField netcdf_data;
    try {
      netcdf_data = (FlatField) plain.open(fileName);
    } catch (IOException e) {
      System.err.println("Couldn't open \"" + fileName + "\": " +
			 e.getMessage());
      System.exit(1);
      return null;
    }
    // System.out.println("netcdf_data type = " + netcdf_data.getType());
    // prints: FunctionType (Real): (lon, lat) -> P_msl

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) netcdf_data.getType();
    RealTupleType dtype = ftype.getDomain();
    MathType rntype = ftype.getRange();
    int n = dtype.getDimension();
    display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                  Display.XAxis));
    if (n > 1) {
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                    Display.YAxis));
    }
    if (n > 2) {
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                    Display.ZAxis));
    }
    if (rntype instanceof RealType) {
      display1.addMap(new ScalarMap((RealType) rntype, Display.Green));
      if (n <= 2) {
        display1.addMap(new ScalarMap((RealType) rntype, Display.ZAxis));
      }
    }
    else if (rntype instanceof RealTupleType) {
      int m = ((RealTupleType) rntype).getDimension();
      RealType rr = (RealType) ((RealTupleType) rntype).getComponent(0);
      display1.addMap(new ScalarMap(rr, Display.Green));
      if (n <= 2) {
        if (m > 1) {
          rr = (RealType) ((RealTupleType) rntype).getComponent(1);
        }
        display1.addMap(new ScalarMap(rr, Display.ZAxis));
      }
    }
    display1.addMap(new ConstantMap(0.5, Display.Red));
    display1.addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_netcdf = new DataReferenceImpl("ref_netcdf");
    ref_netcdf.setData(netcdf_data);
    display1.addReference(ref_netcdf, null);

    System.out.println("now save and re-read data");
    try {
      plain.save("save.nc", netcdf_data, true);
      netcdf_data = (FlatField) plain.open("save.nc");
    } catch (IOException e) {
      System.err.println("Couldn't open \"save.nc\": " + e.getMessage());
      return null;
    }

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return " file_name: netCDF adapter";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test10 t = new Test10(args);
  }
}
