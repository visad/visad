import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

public class Test13
	extends TestSkeleton
{
  public Test13() { }

  public Test13(String args[])
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
    Real[] realsx3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                      new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(realsx3);

    // these ScalarMap should generate 3 Exceptions
    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(vis_radiance, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.RGB));
    display1.addMap(new ScalarMap(count, Display.Animation));

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refsx1 = {ref_direct};
    display1.addReferences(new DirectManipulationRendererJ3D(), refsx1, null);

    DataReferenceImpl ref_direct_tuple;
    ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refsx2 = {ref_direct_tuple};
    display1.addReferences(new DirectManipulationRendererJ3D(), refsx2, null);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refsx3 = {ref_histogram1};
    display1.addReferences(new DirectManipulationRendererJ3D(), refsx3, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": Exception display"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test13 t = new Test13(args);
  }
}
