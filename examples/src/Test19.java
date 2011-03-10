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

import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.util.VisADSlider;
import visad.java3d.DisplayImplJ3D;

public class Test19
  extends UISkeleton
{
  private DataReference value_ref;

  public Test19() { }

  public Test19(String[] args)
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
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);
    FunctionType time_bee = new FunctionType(time_type, image_bumble);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);
    FlatField wasp = FlatField.makeField(image_bumble, size, false);

    int ntimes1 = 4;
    int ntimes2 = 6;
    // different time resolutions for test
    Set time_set =
      new Linear1DSet(time_type, 0.0, 1.0, ntimes1);
    Set time_hornet =
      new Linear1DSet(time_type, 0.0, 1.0, ntimes2);

    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FieldImpl image_stinger = new FieldImpl(time_bee, time_hornet);
    FlatField temp = imaget1;
    FlatField tempw = wasp;
    Real[] reals19 = {new Real(vis_radiance, (float) size / 4.0f),
                      new Real(ir_radiance, (float) size / 8.0f)};
    RealTuple val = new RealTuple(reals19);
    for (int i=0; i<ntimes1; i++) {
      image_sequence.setSample(i, temp);
      temp = (FlatField) temp.add(val);
    }
    for (int i=0; i<ntimes2; i++) {
      image_stinger.setSample(i, tempw);
      tempw = (FlatField) tempw.add(val);
    }
    FieldImpl[] images19 = {image_sequence, image_stinger};
    Tuple big_tuple = new Tuple(images19);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1value = new ScalarMap(RealType.Time, Display.SelectValue);
    dpys[0].addMap(map1value);

    DataReferenceImpl ref_big_tuple;
    ref_big_tuple = new DataReferenceImpl("ref_big_tuple");
    ref_big_tuple.setData(big_tuple);
    dpys[0].addReference(ref_big_tuple, null);

  }

  void finishClientSetup(RemoteServer client)
    throws RemoteException, VisADException
  {
    value_ref = (DataReference )client.getDataReference(0);
  }

  void setServerDataReferences(RemoteServerImpl server)
    throws RemoteException, VisADException
  {
    DataReferenceImpl dref = new DataReferenceImpl("value");
    RemoteDataReferenceImpl ref = new RemoteDataReferenceImpl(dref);
    if (server != null) {
      server.addDataReference(ref);
    }
    value_ref = dref;
  }

  String getFrameTitle() { return "VisAD select slider"; }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    ScalarMap map1value = (ScalarMap )dpys[0].getMapVector().lastElement();

    final ValueControl value1control =
      (ValueControl) map1value.getControl();

    VisADSlider slider =
      new VisADSlider("value", 0, 100, 0, 0.01, value_ref, RealType.Generic);

    if (value_ref instanceof ThingReferenceImpl) {
      final DataReference cell_ref = value_ref;

      CellImpl cell = new CellImpl() {
        public void doAction() throws RemoteException, VisADException {
          value1control.setValue(((Real) cell_ref.getData()).getValue());
        }
      };
      cell.addReference(cell_ref);
    }

    return slider;
  }


  public String toString() { return ": SelectValue"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test19(args);
  }
}
