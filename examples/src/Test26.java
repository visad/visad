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

import java.util.Vector;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test26
  extends TestSkeleton
{
  public Test26() { }

  public Test26(String[] args)
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

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    ScalarMap map1lat = new ScalarMap(RealType.Latitude, Display.YAxis);
    dpys[0].addMap(map1lat);
    /* Old way  DRM 17-Nov-2000
    map1lat.setScalarName("Distance to Wall (m)");
    map1lat.setScaleColor(new float[] {0.0f, 1.0f, 0.0f});
    */
    // New Way
    AxisScale latScale = map1lat.getAxisScale();
    latScale.setTitle("Distance to Wall (m)");
    latScale.setColor(java.awt.Color.green);
    latScale.setFont(java.awt.Font.decode("serif"));

    ScalarMap map1lon = new ScalarMap(RealType.Longitude, Display.XAxis);
    //map1lon.setScaleEnable(false);  Old way DRM: 2001-08-09
    map1lon.getAxisScale().setVisible(false);
    dpys[0].addMap(map1lon);

    ScalarMap map1vis = new ScalarMap(vis_radiance, Display.ZAxis);
    map1vis.setUnderscoreToBlank(true);
    // could also use map1vis.getAxisScale().setLabel("vis radiance") above
    map1vis.getAxisScale().setColor(new float[] {1.0f, 0.0f, 0.0f});
    dpys[0].addMap(map1vis);
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setScaleEnable(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    Vector v = dpys[0].getMapVector();

    ScalarMap map1lat = (ScalarMap )v.elementAt(0);
    ScalarMap map1lon = (ScalarMap )v.elementAt(1);
    ScalarMap map1vis = (ScalarMap )v.elementAt(2);

    boolean forever = true;
    while (forever) {
      // delay(5000);
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
      }
      System.out.println("\ndelay\n");
      double[] range1lat = map1lat.getRange();
      double[] range1lon = map1lon.getRange();
      double[] range1vis = map1vis.getRange();
      double inclat = 0.05 * (range1lat[1] - range1lat[0]);
      double inclon = 0.05 * (range1lon[1] - range1lon[0]);
      double incvis = 0.05 * (range1vis[1] - range1vis[0]);
      map1lat.setRange(range1lat[1] + inclat, range1lat[0] - inclat);
      map1lat.getAxisScale().setMinorTickSpacing(
        map1lat.getAxisScale().getMajorTickSpacing()/2);
      map1lon.setRange(range1lon[1] + inclon, range1lon[0] - inclon);
      boolean visible = !map1lon.getAxisScale().isVisible();
      map1lon.getAxisScale().setVisible(visible);
      map1vis.setRange(range1vis[1] + incvis, range1vis[0] - incvis);
    }

  }

  public String toString() { return ": scale"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test26(args);
  }
}
