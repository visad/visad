import java.rmi.Naming;
import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DirectManipulationRendererJ2D;
import visad.java2d.DisplayImplJ2D;

public class Test55
	extends UISkeleton
{
  boolean hasClientServerMode() { return false; }

  public Test55() { }

  public Test55(String args[])
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

    DisplayImpl[] dpys;
    try {

      int size = 64;
      FlatField histogram1;
      histogram1 = FlatField.makeField(ir_histogram, size, false);
      Real direct = new Real(ir_radiance, 2.0);
      Real[] reals14 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                       new Real(vis_radiance, 1.0)};
      RealTuple direct_tuple = new RealTuple(reals14);

      DisplayImpl display1;
      display1 = new DisplayImplJ2D("display1");
      display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
      display1.addMap(new ScalarMap(count, Display.YAxis));

      GraphicsModeControl mode = display1.getGraphicsModeControl();
      mode.setPointSize(5.0f);
      mode.setPointMode(false);

      DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
      ref_direct.setData(direct);
      DataReference[] refs141 = {ref_direct};
      display1.addReferences(new DirectManipulationRendererJ2D(), refs141, null);

      DataReferenceImpl ref_direct_tuple;
      ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
      ref_direct_tuple.setData(direct_tuple);
      DataReference[] refs142 = {ref_direct_tuple};
      display1.addReferences(new DirectManipulationRendererJ2D(), refs142, null);

      DataReferenceImpl ref_histogram1;
      ref_histogram1 = new DataReferenceImpl("ref_histogram1");
      ref_histogram1.setData(histogram1);
      DataReference[] refs143 = {ref_histogram1};
      display1.addReferences(new DirectManipulationRendererJ2D(), refs143, null);

      // create local DataReferenceImpls
      DataReferenceImpl[] data_refs = new DataReferenceImpl[3];
      data_refs[0] = ref_histogram1;
      data_refs[1] = ref_direct;
      data_refs[2] = ref_direct_tuple;

      // create RemoteDataReferences
      RemoteDataReferenceImpl[] rem_data_refs;
      rem_data_refs = new RemoteDataReferenceImpl[3];
      rem_data_refs[0] = new RemoteDataReferenceImpl(data_refs[0]);
      rem_data_refs[1] = new RemoteDataReferenceImpl(data_refs[1]);
      rem_data_refs[2] = new RemoteDataReferenceImpl(data_refs[2]);

      RemoteServerImpl obj = new RemoteServerImpl(rem_data_refs);
      Naming.rebind("//:/RemoteServerTest", obj);

      System.out.println("RemoteServer bound in registry");

      dpys = new DisplayImpl[1];
      dpys[0] = display1;
    }
    catch (Exception e) {
      System.out.println("\n\nDid you run 'rmiregistry &' first?\n\n");
      System.out.println("collaboration server exception: " + e.getMessage());
      e.printStackTrace();
      dpys = null;
    }

    return dpys;
  }

  public String toString()
  {
    return ": collaborative direct manipulation server" +
		"\n\trun rmiregistry first" +
		"\n\tany number of clients may connect";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test55 t = new Test55(args);
  }
}
