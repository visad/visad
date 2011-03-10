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

import visad.util.ColorMapWidget;
import visad.util.LabeledColorWidget;

import visad.java3d.DisplayImplJ3D;

public class Test12
  extends UISkeleton
{
  boolean dynamic;

  public Test12() { }

  public Test12(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { dynamic = false; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    dynamic = true;
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

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    dpys[0].addMap(new ScalarMap(ir_radiance, Display.RGB));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setTextureEnable(false);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    super.setupUI(dpys);

    if (dynamic) {
      ScalarMap colorMap = (ScalarMap )dpys[0].getMapVector().lastElement();
      ColorControl control = (ColorControl) colorMap.getControl();

      final int CEILING = 1024;

      boolean growing = true;
      while (true) {
        try {
          Thread.sleep(5000);
        }
        catch (InterruptedException e) {
        }

        int size;
        while (true) {
          if (growing) {
            size = control.getNumberOfColors() * 2;
          } else {
            size = control.getNumberOfColors() / 2;
          }

          if (size > 4 && size <= CEILING) {
            break;
          }

          growing = !growing;
        }

        System.out.println("\n" + size + " colors\n");

        float[][] table = new float[3][size];
        final float scale = 1.0f / (size - 1.0f);
        for (int i=0; i<size; i++) {
          table[0][i] = scale * i;
          table[1][i] = scale * i;
          table[2][i] = scale * i;
        }
        control.setTable(table);
      }
    }
  }

  String getFrameTitle() { return "VisAD Color Widget"; }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    ScalarMap colorMap = (ScalarMap )dpys[0].getMapVector().lastElement();
    if (dynamic) {
      return new LabeledColorWidget(colorMap);
    }
    else {
      return new LabeledColorWidget(new ColorMapWidget(colorMap, false));
    }
  }

  public String toString() { return ": 2-D surface and ColorWidget"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test12(args);
  }
}
