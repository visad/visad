/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Component;

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

  public Test10(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  int checkExtraKeyword(String testName, int argc, String[] args)
  {
    if (fileName == null) {
      fileName = args[argc];
    } else {
      System.err.println(testName + ": Ignoring extra filename \"" +
                         args[argc] + "\"");
    }

    return 1;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    Unit super_degree = CommonUnit.degree.scale(2.5);
    RealType lon = new RealType("lon", super_degree, null);

    if (fileName == null) {
      System.err.println("must specify netCDF file name");
      System.exit(1);
      return;
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
      return;
    }
    // System.out.println("netcdf_data type = " + netcdf_data.getType());
    // prints: FunctionType (Real): (lon, lat) -> P_msl

    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) netcdf_data.getType();
    RealTupleType dtype = ftype.getDomain();
    MathType rntype = ftype.getRange();
    int n = dtype.getDimension();
    dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                  Display.XAxis));
    if (n > 1) {
      dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                    Display.YAxis));
    }
    if (n > 2) {
      dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                    Display.ZAxis));
    }
    if (rntype instanceof RealType) {
      dpys[0].addMap(new ScalarMap((RealType) rntype, Display.Green));
      if (n <= 2) {
        dpys[0].addMap(new ScalarMap((RealType) rntype, Display.ZAxis));
      }
    }
    else if (rntype instanceof RealTupleType) {
      int m = ((RealTupleType) rntype).getDimension();
      RealType rr = (RealType) ((RealTupleType) rntype).getComponent(0);
      dpys[0].addMap(new ScalarMap(rr, Display.Green));
      if (n <= 2) {
        if (m > 1) {
          rr = (RealType) ((RealTupleType) rntype).getComponent(1);
        }
        dpys[0].addMap(new ScalarMap(rr, Display.ZAxis));
      }
    }
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    dpys[0].addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_netcdf = new DataReferenceImpl("ref_netcdf");
    ref_netcdf.setData(netcdf_data);
    dpys[0].addReference(ref_netcdf, null);

    System.out.println("now save and re-read data");
    try {
      plain.save("save.nc", netcdf_data, true);
      netcdf_data = (FlatField) plain.open("save.nc");
    } catch (IOException e) {
      System.err.println("Couldn't open \"save.nc\": " + e.getMessage());
      System.exit(1);
      return;
    }
  }

  public String toString() { return " file_name: netCDF adapter"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test10(args);
  }
}
