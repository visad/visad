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

public class Test16
  extends TestSkeleton
{
  private int size = 47;

  public Test16() { }

  public Test16(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { size = 47; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    try {
      size = Integer.parseInt(args[0]);
      if (size < 1) size = 47;
    }
    catch(NumberFormatException e) {
      size = 47;
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
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    // int size = 47;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setTextureEnable(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  public String toString() { return ": opaque texture mapping"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test16(args);
  }
}
