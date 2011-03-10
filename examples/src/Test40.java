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

import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;

public class Test40
  extends UISkeleton
{
  public Test40() { }

  public Test40(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = new DisplayImplJ2D("display1");
    dpys[1] = new DisplayImplJ2D("display2");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    Unit super_degree = CommonUnit.degree.scale(2.5);
    RealType lon = RealType.getRealType("lon", super_degree);
    RealType radius = RealType.getRealType("radius");
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealTupleType cartesian = new RealTupleType(x, y);
    PolarCoordinateSystem polar_coord_sys = new PolarCoordinateSystem(cartesian);
    RealTupleType polar = new RealTupleType(lon, radius, polar_coord_sys, null);
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    RealType vis_radiance = RealType.getRealType("vis_radiance");

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real[] reals3;
    reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                         new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(reals3);

    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Radius));
    dpys[0].addMap(new ScalarMap(count, Display.Longitude));
    dpys[0].addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);

    DataReferenceImpl ref_direct_tuple;
    ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = new DataReference[] {ref_direct_tuple};
    dpys[0].addReferences(new DirectManipulationRendererJ2D(), refs2, null);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = new DataReference[] {ref_histogram1};
    dpys[0].addReferences(new DirectManipulationRendererJ2D(), refs3, null);

    dpys[1].addMap(new ScalarMap(ir_radiance, Display.XAxis));
    dpys[1].addMap(new ScalarMap(count, Display.YAxis));
    dpys[1].addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode2 = dpys[1].getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    dpys[1].addReferences(new DirectManipulationRendererJ2D(), refs2, null);
    dpys[1].addReferences(new DirectManipulationRendererJ2D(), refs3, null);
  }

  String getFrameTitle() { return "Java2D direct manipulation"; }

  public String toString() { return ": polar direct manipulation in Java2D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test40(args);
  }
}
