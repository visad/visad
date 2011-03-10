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
import java.util.Random;

import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;

public class Test59
  extends UISkeleton
{
  public Test59() { }

  public Test59(String[] args)
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
    RealType index = RealType.getRealType("index");
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types = {RealType.Latitude, RealType.Longitude,
                        vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types);
    FunctionType image_tuple = new FunctionType(index, radiance);

    int size = 216;
    Set domain_set = new Integer1DSet(size);
    FlatField imaget1 = new FlatField(image_tuple, domain_set);
    float[][] values = new float[4][size];
    Random random = new Random();
    for (int i=0; i<size; i++) {
      values[0][i] = 2.0f * random.nextFloat() - 1.0f;
      values[1][i] = 2.0f * random.nextFloat() - 1.0f;
      values[2][i] = 2.0f * random.nextFloat() - 1.0f;
      values[3][i] = (float) Math.sqrt(values[0][i] * values[0][i] +
                                       values[1][i] * values[1][i] +
                                       values[2][i] * values[2][i]);
    }
    imaget1.setSamples(values);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    dpys[0].addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(ir_radiance, Display.IsoContour);
    dpys[0].addMap(map1contour);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  String getFrameTitle() { return "VisAD irregular iso-level controls"; }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    ScalarMap map1contour = (ScalarMap )dpys[0].getMapVector().lastElement();
    return new ContourWidget(map1contour);
  }

  public String toString()
  {
    return ": colored iso-surfaces from scatter data and ContourWidget";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test59(args);
  }
}
