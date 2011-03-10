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

public class Test00
  extends UISkeleton
{
  public Test00() { }

  public Test00(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = new DisplayImplJ3D("display1");
    dpys[1] = new DisplayImplJ3D("display2");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    GraphicsModeControl mode;

    final RealType ir_radiance =
      RealType.getRealType("ir_radiance", CommonUnit.degree);
    Unit cycles = CommonUnit.dimensionless.divide(CommonUnit.second);
    Unit hz = cycles.clone("Hz");
    final RealType count = RealType.getRealType("count", hz);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    final RealType vis_radiance = RealType.getRealType("vis_radiance");

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real direct = new Real(ir_radiance, 2.0);
    Real[] reals3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                     new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(reals3);

    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    ScalarMap irmap = new ScalarMap(ir_radiance, Display.XAxis);
    dpys[0].addMap(irmap);
    irmap.setOverrideUnit(CommonUnit.radian);
    dpys[0].addMap(new ScalarMap(count, Display.YAxis));
    dpys[0].addMap(new ScalarMap(count, Display.Green));

    mode = dpys[0].getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);
    mode.setScaleEnable(true);

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refs1 = {ref_direct};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refs1, null);

    DataReferenceImpl ref_direct_tuple =
      new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = {ref_direct_tuple};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refs2, null);

    DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = {ref_histogram1};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    dpys[1].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    dpys[1].addMap(new ScalarMap(ir_radiance, Display.XAxis));
    dpys[1].addMap(new ScalarMap(count, Display.YAxis));
    dpys[1].addMap(new ScalarMap(count, Display.Green));
    final DisplayRenderer dr0 = dpys[0].getDisplayRenderer();
    final DisplayRenderer dr1 = dpys[1].getDisplayRenderer();
    dr0.setCursorStringOn(true);
    dr1.setCursorStringOn(false);

    mode = dpys[1].getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);
    mode.setScaleEnable(true);

    dpys[1].addReferences(new DirectManipulationRendererJ3D(), refs1, null);
    dpys[1].addReferences(new DirectManipulationRendererJ3D(), refs2, null);
    dpys[1].addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    MouseHelper helper = dr1.getMouseBehavior().getMouseHelper();
    helper.setFunctionMap(new int[][][]
      {{{MouseHelper.DIRECT, MouseHelper.DIRECT},
        {MouseHelper.DIRECT, MouseHelper.DIRECT}},
       {{MouseHelper.ROTATE, MouseHelper.NONE},
        {MouseHelper.NONE, MouseHelper.NONE}},
       {{MouseHelper.NONE, MouseHelper.NONE},
        {MouseHelper.NONE, MouseHelper.NONE}}});

    CellImpl cell = new CellImpl() {
      public void doAction() throws RemoteException, VisADException {
        double vir = dr1.getDirectAxisValue(ir_radiance);
        double vvis = dr1.getDirectAxisValue(vis_radiance);
        double vc = dr1.getDirectAxisValue(count);
        //System.out.println("ir_radiance = " + vir + " count = " + vc + " vis_radiance = " + vvis);

        java.util.Vector csv = dr1.getCursorStringVectorUnconditional();
        for (int i=0; i<csv.size(); i++) {
          System.out.println((String) csv.elementAt(i));
        }
      }
    };
    cell.addReference(ref_direct);
    cell.addReference(ref_direct_tuple);
    cell.addReference(ref_histogram1);

  }

  String getFrameTitle() { return "Java3D direct manipulation"; }

  public String toString() { return ": direct manipulation and Mouse options"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test00(args);
  }
}
