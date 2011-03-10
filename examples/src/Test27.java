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

import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

public class Test27
  extends TestSkeleton
{
  static int no_self = 0;
  RealType vis_radiance;

  boolean hasClientServerMode() { return false; }

  public Test27() { }

  public Test27(String[] args)
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
    RealType[] types = {RealType.Latitude, RealType.Altitude};
    RealTupleType earth_location = new RealTupleType(types);
    vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    final RealType junk = RealType.getRealType("junk");

    System.out.println("  drag yellow points with right mouse button");
    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    final ScalarMap map2lat = new ScalarMap(RealType.Latitude, Display.YAxis);
    dpys[0].addMap(map2lat);
    final ScalarMap map2lon = new ScalarMap(RealType.Altitude, Display.XAxis);
    dpys[0].addMap(map2lon);
    final ScalarMap map2vis = new ScalarMap(vis_radiance, Display.ZAxis);
    dpys[0].addMap(map2vis);
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));

    ScalarMap smap = new ScalarMap(junk, Display.Shape);
    dpys[0].addMap(smap);

    Gridded1DSet count_set =
      new Gridded1DSet(RealType.Latitude, new float[][] {{0.0f}}, 1);
    ShapeControl shape_control = (ShapeControl) smap.getControl();
    shape_control.setShapeSet(count_set);
    VisADLineArray cross = new VisADLineArray();
    cross.coordinates = new float[]
      {0.1f,  0.0f,  0.0f,    -0.1f,  0.0f,  0.0f,
       0.0f, -0.1f,  0.0f,     0.0f,  0.1f,  0.0f,
       0.0f,  0.0f,  0.1f,     0.0f,  0.0f, -0.1f};
    cross.vertexCount = cross.coordinates.length / 3;
    VisADGeometryArray[] shapes = {cross};
    shape_control.setShapes(shapes);

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setScaleEnable(true);
    mode.setPointMode(false);

    mode.setProjectionPolicy(DisplayImplJ3D.PARALLEL_PROJECTION);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);

    try {
      Thread.sleep(2000);
    }
    catch (InterruptedException e) {
    }
    double[] range1lat = map2lat.getRange();
    double[] range1lon = map2lon.getRange();
    double[] range1vis = map2vis.getRange();

    RealTuple direct_low = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, range1lat[0]),
                      new Real(RealType.Altitude, range1lon[0]),
                      new Real(vis_radiance, range1vis[0]),
                      new Real(junk, 0.0)});
    RealTuple direct_hi = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, range1lat[1]),
                      new Real(RealType.Altitude, range1lon[1]),
                      new Real(vis_radiance, range1vis[1]),
                      new Real(junk, 0.0)});

    final DataReferenceImpl ref_direct_low =
      new DataReferenceImpl("ref_direct_low");
    ref_direct_low.setData(direct_low);
    // color low and hi tuples yellow
    ConstantMap[][] maps = {{new ConstantMap(1.0f, Display.Red),
                             new ConstantMap(1.0f, Display.Green),
                             new ConstantMap(0.0f, Display.Blue),
                             new ConstantMap(3.0f, Display.LineWidth)}};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(),
                           new DataReference[] {ref_direct_low}, maps);

    final DataReferenceImpl ref_direct_hi =
      new DataReferenceImpl("ref_direct_hi");
    ref_direct_hi.setData(direct_hi);
    maps = new ConstantMap[][] {{new ConstantMap(1.0f, Display.Red),
                                 new ConstantMap(1.0f, Display.Green),
                                 new ConstantMap(0.0f, Display.Blue),
                                 new ConstantMap(3.0f, Display.LineWidth)}};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(),
                           new DataReference[] {ref_direct_hi}, maps);

    no_self = 0;

    CellImpl cell = new CellImpl() {
      public synchronized void doAction()
        throws RemoteException, VisADException {
        if (no_self > 0) {
          no_self--;
          if (no_self > 0) return;
        }
        RealTuple low = (RealTuple) ref_direct_low.getData();
        RealTuple hi = (RealTuple) ref_direct_hi.getData();
        double[] lows = {((Real) low.getComponent(0)).getValue(),
                         ((Real) low.getComponent(1)).getValue(),
                         ((Real) low.getComponent(2)).getValue()};
        double[] his = {((Real) hi.getComponent(0)).getValue(),
                        ((Real) hi.getComponent(1)).getValue(),
                        ((Real) hi.getComponent(2)).getValue()};
        boolean changed = false;
        for (int i=0; i<3; i++) {
          if (his[i] < lows[i] + 0.00001) {
            double m = 0.5 * (lows[i] + his[i]);
            lows[i] = m - 0.000005;
            his[i] = m + 0.000005;
            changed = true;
          }
        }

        if (changed) {
          RealTuple dlow = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, lows[0]),
                      new Real(RealType.Altitude, lows[1]),
                      new Real(vis_radiance, lows[2]),
                      new Real(junk, 0.0)});
          RealTuple dhi = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, his[0]),
                      new Real(RealType.Altitude, his[1]),
                      new Real(vis_radiance, his[2]),
                      new Real(junk, 0.0)});
          ref_direct_low.setData(dlow);
          ref_direct_hi.setData(dhi);
          no_self += 2;
        }

        map2lat.setRange(lows[0], his[0]);
        map2lon.setRange(lows[1], his[1]);
        map2vis.setRange(lows[2], his[2]);
      }
    };
    cell.addReference(ref_direct_low);
    cell.addReference(ref_direct_hi);
  }

  public String toString() { return ": interactive scale"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test27(args);
  }
}
