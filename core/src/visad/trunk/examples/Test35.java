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

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DirectManipulationRendererJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java2d.DirectManipulationRendererJ2D;

import visad.util.Delay;

public class Test35
	extends UISkeleton
{
  public Test35() { }

  public Test35(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    RealType vis_radiance = new RealType("vis_radiance", null, null);

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real direct = new Real(ir_radiance, 2.0);
    Real[] reals3;
    reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                         new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(reals3);

    DisplayImpl display1 = new DisplayImplJ3D("display1");
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display1.addMap(new ScalarMap(count, Display.YAxis));
    display1.addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refs1 = new DataReferenceImpl[] {ref_direct};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs1, null);

    DataReferenceImpl ref_direct_tuple;
    ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = new DataReference[] {ref_direct_tuple};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs2, null);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = new DataReference[] {ref_histogram1};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    new Delay(500);

    DisplayImpl display2 = new DisplayImplJ2D("display2");
    display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display2.addMap(new ScalarMap(count, Display.YAxis));
    display2.addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    display2.addReferences(new DirectManipulationRendererJ2D(), refs1, null);
    display2.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
    display2.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = display1;
    dpys[1] = display2;

    return dpys;
  }

  String getFrameTitle() { return "Java3D -- Java2D direct manipulation"; }

  public String toString()
  {
    return ": direct manipulation linking Java2D and Java3D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test35 t = new Test35(args);
  }
}
