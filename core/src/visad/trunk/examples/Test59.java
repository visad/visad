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

  DisplayImpl[] setupData()
    throws RemoteException, VisADException
  {
    RealType index = new RealType("index", null, null);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
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

    DisplayImpl display1 =
      new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(ir_radiance, Display.IsoContour);
    display1.addMap(map1contour);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "VisAD irregular iso-level controls"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
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
    Test59 t = new Test59(args);
  }
}
