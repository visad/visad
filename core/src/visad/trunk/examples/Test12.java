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

import visad.util.LabeledColorWidget;
import visad.java3d.DisplayImplJ3D;

public class Test12
  extends UISkeleton
{
  ScalarMap color1map = null;
  boolean dynamic = false;

  public Test12() { }

  public Test12(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  int checkExtraKeyword(int argc, String[] args)
  {
    dynamic = true;
    return 1;
  }

  DisplayImpl[] setupData()
    throws RemoteException, VisADException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    color1map = new ScalarMap(ir_radiance, Display.RGB);
    display1.addMap(color1map);

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setTextureEnable(false);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  void setupUI(DisplayImpl[] dpys)
    throws RemoteException, VisADException
  {
    super.setupUI(dpys);

    if (dynamic) {
      ColorControl control = (ColorControl) color1map.getControl();
      boolean forever = true;
      int size = 512;
      while (forever) {
        try {
          Thread.sleep(5000);
        }
        catch (InterruptedException e) {
        }
        System.out.println("\ndelay\n");
        float[][] table = new float[3][size];
        float scale = 1.0f / (size - 1.0f);
        for (int i=0; i<size; i++) {
          table[0][i] = scale * i;
          table[1][i] = scale * i;
          table[2][i] = scale * i;
        }
        size *= 2;
        control.setTable(table);
      }
    }
  }

  String getFrameTitle() { return "VisAD Color Widget"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
    throws RemoteException, VisADException
  {
    ScalarMap color1map = (ScalarMap )dpys[0].getMapVector().lastElement();
    return new LabeledColorWidget(color1map);
  }

  public String toString() { return ": 2-D surface and ColorWidget"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    Test12 t = new Test12(args);
  }
}
