/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

public class Test30
	extends TestSkeleton
{
  public Test30() { }

  public Test30(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] time = {RealType.Time};
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    RealType[] types4 = {ir_radiance, vis_radiance};
    RealTupleType ecnaidar = new RealTupleType(types4);
    FunctionType image_bumble = new FunctionType(earth_location, ecnaidar);
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);
    FlatField wasp = FlatField.makeField(image_bumble, size, false);

    int ntimes1 = 4;

    double start = new DateTime(1999, 122, 57060).getValue();
    Set time_set = new Linear1DSet(time_type, start,
                              start + 3600.0 * (ntimes1 - 1.0), ntimes1);

    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FlatField temp = imaget1;
    Real[] reals30 = {new Real(vis_radiance, (float) size / 4.0f),
                      new Real(ir_radiance, (float) size / 8.0f)};
    RealTuple val = new RealTuple(reals30);
    for (int i=0; i<ntimes1; i++) {
      image_sequence.setSample(i, temp);
      temp = (FlatField) temp.add(val);
    }

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.Red));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ScalarMap(RealType.Time, Display.ZAxis));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setScaleEnable(true);

    DataReference ref_image_sequence = new DataReferenceImpl("ref_big_tuple");
    ref_image_sequence.setData(image_sequence);
    display1.addReference(ref_image_sequence, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": time stack and time axis label"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test30 t = new Test30(args);
  }
}
