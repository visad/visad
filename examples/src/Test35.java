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
import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.java3d.DisplayRendererJ3D;

import visad.util.Delay;

public class Test35
  extends UISkeleton
  implements DisplayListener
{
  public Test35() { }

  public Test35(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = new DisplayImplJ3D("display1");
    dpys[1] = new DisplayImplJ2D("display2");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    RealType vis_radiance = RealType.getRealType("vis_radiance");

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real direct = new Real(ir_radiance, 2.0);
    Real[] reals3;
    reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                         new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(reals3);

    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));
    dpys[0].addMap(new ScalarMap(count, Display.YAxis));
    dpys[0].addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);
    mode.setScaleEnable(true);
    DisplayRendererJ3D dr = (DisplayRendererJ3D) dpys[0].getDisplayRenderer();
    dr.setClip(0, true,  1.0f,  0.0f,  0.0f, -1.0f);
    dr.setClip(1, true, -1.0f,  0.0f,  0.0f, -1.0f);
    dr.setClip(2, true,  0.0f,  1.0f,  0.0f, -1.0f);
    dr.setClip(3, true,  0.0f, -1.0f,  0.0f, -1.0f);
    dr.setClip(4, true,  0.0f,  0.0f,  1.0f, -1.0f);
    dr.setClip(5, true,  0.0f,  0.0f, -1.0f, -1.0f);

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refs1 = new DataReferenceImpl[] {ref_direct};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refs1, null);

    DataReferenceImpl ref_direct_tuple;
    ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = new DataReference[] {ref_direct_tuple};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refs2, null);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = new DataReference[] {ref_histogram1};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    new Delay(500);

    dpys[1].addMap(new ScalarMap(ir_radiance, Display.XAxis));
    dpys[1].addMap(new ScalarMap(count, Display.YAxis));
    dpys[1].addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode2 = dpys[1].getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    dpys[1].addReferences(new DirectManipulationRendererJ2D(), refs1, null);
    dpys[1].addReferences(new DirectManipulationRendererJ2D(), refs2, null);
    dpys[1].addReferences(new DirectManipulationRendererJ2D(), refs3, null);

    dpys[0].addDisplayListener(this);
    dpys[1].addDisplayListener(this);
  }

  public void displayChanged(DisplayEvent e)
    throws RemoteException, VisADException {
    if (e.getId() == DisplayEvent.FRAME_DONE) {
      DisplayImpl display = (DisplayImpl) e.getDisplay();
      DisplayRenderer dr = display.getDisplayRenderer();
      MouseBehavior mb = dr.getMouseBehavior();
      double[] position1 = null;
      double[] position2 = null;
      if (display instanceof DisplayImplJ3D) {
        position1 = new double[] { 1.0,  1.0,  1.0};
        position2 = new double[] {-1.0, -1.0, -1.0};
      }
      else {
        position1 = new double[] { 1.0,  1.0};
        position2 = new double[] {-1.0, -1.0};
      }
      int[] screen1 = mb.getScreenCoords(position1);
      int[] screen2 = mb.getScreenCoords(position2);
      //System.out.println("screen1 = (" + screen1[0] + ", " + screen1[1] +")");
      //System.out.println("screen2 = (" + screen2[0] + ", " + screen2[1] +")");
    }
  }

  String getFrameTitle() { return "clipped Java3D -- Java2D direct manipulation"; }

  public String toString()
  {
    return ": direct manipulation linking Java2D and clipped Java3D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test35(args);
  }
}
