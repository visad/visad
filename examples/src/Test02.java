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

// JFC packages
import javax.swing.*;

// AWT packages
import java.awt.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;
import visad.util.LabeledColorWidget;

public class Test02
  extends UISkeleton
{
  ScalarMap map1color = null;
  ScalarMap map1contour = null;
  int size3d;

  public Test02() { }

  public Test02(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { size3d = 6; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    try {
      size3d = Integer.parseInt(args[0]);
      if (size3d < 1) size3d = 6;
    }
    catch(NumberFormatException e) {
      size3d = 6;
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
    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    float level = 2.5f;
    FlatField grid3d = FlatField.makeField(grid_tuple, size3d, true);

    if ((size3d % 2) != 0) {
      double last = size3d - 1.0;
      Linear3DSet set =
        new Linear3DSet(earth_location3d, 0.0, last, size3d,
                                          0.0, last, size3d,
                                          0.0, last, size3d);
      grid3d = (FlatField)
        grid3d.resample(set, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
        // grid3d.resample(set, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
    }

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
    map1color = new ScalarMap(ir_radiance, Display.RGB);
    dpys[0].addMap(map1color);
    map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    dpys[0].addMap(map1contour);

    DataReferenceImpl ref_grid3d = new DataReferenceImpl("ref_grid3d");
    ref_grid3d.setData(grid3d);
    dpys[0].addReference(ref_grid3d, null);
  }

  String getFrameTitle() { return "VisAD irregular iso-level controls"; }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    panel.add(new ContourWidget(map1contour));
    panel.add(new LabeledColorWidget(map1color));
    return panel;
  }

  public String toString()
  {
    return ": colored iso-surfaces from irregular grids and ContourWidget";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test02(args);
  }
}
