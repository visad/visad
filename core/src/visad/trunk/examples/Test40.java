import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;

public class Test40
	extends UISkeleton
{
  public Test40() { }

  public Test40(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    Unit super_degree = CommonUnit.degree.scale(2.5);
    RealType lon = new RealType("lon", super_degree, null);
    RealType radius = new RealType("radius", null, null);
    RealType x = new RealType("x", null, null);
    RealType y = new RealType("y", null, null);
    RealTupleType cartesian = new RealTupleType(x, y);
    PolarCoordinateSystem polar_coord_sys = new PolarCoordinateSystem(cartesian);
    RealTupleType polar = new RealTupleType(lon, radius, polar_coord_sys, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    RealType vis_radiance = new RealType("vis_radiance", null, null);

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real[] reals3;
    reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                         new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(reals3);

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display1.addMap(new ScalarMap(count, Display.Longitude));
    display1.addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);

    DataReferenceImpl ref_direct_tuple;
    ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = new DataReference[] {ref_direct_tuple};
    display1.addReferences(new DirectManipulationRendererJ2D(), refs2, null);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = new DataReference[] {ref_histogram1};
    display1.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

    DisplayImpl display2 = new DisplayImplJ2D("display2");
    display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display2.addMap(new ScalarMap(count, Display.YAxis));
    display2.addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    display2.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
    display2.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = display1;
    dpys[1] = display2;

    return dpys;
  }

  String getFrameTitle() { return "Java2D direct manipulation"; }

  public String toString() { return ": polar direct manipulation in Java2D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test40 t = new Test40(args);
  }
}
