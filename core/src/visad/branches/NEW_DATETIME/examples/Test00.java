import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

public class Test00
	extends UISkeleton
{
  public Test00() { }

  public Test00(String args[])
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
    Real[] reals3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
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
    DataReference[] refs1 = {ref_direct};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs1, null);

    DataReferenceImpl ref_direct_tuple =
      new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = {ref_direct_tuple};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs2, null);

    DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = {ref_histogram1};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    DisplayImpl display2 = new DisplayImplJ3D("display2");
    display2.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display2.addMap(new ScalarMap(count, Display.YAxis));
    display2.addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    display2.addReferences(new DirectManipulationRendererJ3D(), refs1, null);
    display2.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
    display2.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = display1;
    dpys[1] = display2;

    return dpys;
  }

  String getFrameTitle() { return "Java3D direct manipulation"; }

  public String toString() { return ": direct manipulation"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test00 t = new Test00(args);
  }
}
