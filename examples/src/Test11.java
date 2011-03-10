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

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test11
  extends TestSkeleton
{
  public Test11() { }

  public Test11(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
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
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    Unit super_degree = CommonUnit.degree.scale(2.5);
    RealType lon = RealType.getRealType("lon", super_degree);
    RealType radius = RealType.getRealType("radius");
    RealTupleType cartesian = new RealTupleType(x, y);
    PolarCoordinateSystem polar_coord_sys =
      new PolarCoordinateSystem(cartesian);
    RealTupleType polar =
      new RealTupleType(lon, radius, polar_coord_sys, null);

    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);

    FunctionType image_polar = new FunctionType(polar, radiance);
    Unit[] units = {super_degree, null};
    Linear2DSet domain_set =
      new Linear2DSet(polar, 0.0, 60.0, 61, 0.0, 60.0, 61,
                      polar_coord_sys, units, null);
    FlatField imaget1 = new FlatField(image_polar, domain_set);
    FlatField.fillField(imaget1, 1.0, 30.0);

    dpys[0].addMap(new ScalarMap(x, Display.XAxis));
    dpys[0].addMap(new ScalarMap(y, Display.YAxis));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    dpys[0].addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  public String toString() { return ": CoordinateSystem and Unit"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test11(args);
  }
}
