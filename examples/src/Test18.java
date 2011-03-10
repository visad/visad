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

public class Test18
  extends TestSkeleton
{
  public Test18() { }

  public Test18(String[] args)
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
    RealType[] time = {RealType.Time};
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    RealType[] types4 = {ir_radiance, vis_radiance};
    RealTupleType ecnaidar = new RealTupleType(types4);
    FunctionType image_bumble = new FunctionType(earth_location, ecnaidar);
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);
    FunctionType time_bee = new FunctionType(time_type, image_bumble);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);
    FlatField wasp = FlatField.makeField(image_bumble, size, false);

    int ntimes1 = 4;
    int ntimes2 = 6;

    // different time extents test
    // 2 May 99, 15:51:00
    double start = new DateTime(1999, 122, 57060).getValue();
    Set time_set = new Linear1DSet(time_type, start,
                              start + 3600.0 * (ntimes1 - 1.0), ntimes1);
    Set time_hornet = new Linear1DSet(time_type, start,
                              start + 3600.0 * (ntimes2 - 1.0), ntimes2);

    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FieldImpl image_stinger = new FieldImpl(time_bee, time_hornet);
    FlatField temp = imaget1;
    FlatField tempw = wasp;
    Real[] reals18 = {new Real(vis_radiance, (float) size / 4.0f),
                      new Real(ir_radiance, (float) size / 8.0f)};
    RealTuple val = new RealTuple(reals18);
    for (int i=0; i<ntimes1; i++) {
      image_sequence.setSample(i, temp);
      temp = (FlatField) temp.add(val);
    }
    for (int i=0; i<ntimes2; i++) {
      image_stinger.setSample(i, tempw);
      tempw = (FlatField) tempw.add(val);
    }
    FieldImpl[] images18 = {image_sequence, image_stinger};
    Tuple big_tuple = new Tuple(images18);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1animation;
    map1animation = new ScalarMap(RealType.Time, Display.Animation);
    dpys[0].addMap(map1animation);

    DataReferenceImpl ref_big_tuple;
    ref_big_tuple = new DataReferenceImpl("ref_big_tuple");
    ref_big_tuple.setData(big_tuple);
    dpys[0].addReference(ref_big_tuple, null);
  }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    ScalarMap map1animation = (ScalarMap )dpys[0].getMapVector().lastElement();

    AnimationControl animation1control =
      (AnimationControl) map1animation.getControl();
    animation1control.setOn(true);
    animation1control.setStep(3000);
  }

  public String toString() { return ": Animation different time extents"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test18(args);
  }
}
