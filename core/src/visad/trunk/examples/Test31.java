import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test31
	extends TestSkeleton
{
  public Test31() { }

  public Test31(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    RealType[] scatter_list = {vis_radiance, ir_radiance, count, RealType.Latitude,
                               RealType.Longitude, RealType.Radius};
    RealTupleType scatter = new RealTupleType(scatter_list);
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType scatter_function = new FunctionType(time_type, scatter);

    int size = 64;

    FlatField imaget1;
    imaget1 = FlatField.makeField(scatter_function, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.Green));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));

    // WLH 28 April 99 - test alpha with points
    display1.addMap(new ScalarMap(vis_radiance, Display.Alpha));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setPointSize(5.0f);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": scatter diagram"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test31 t = new Test31(args);
  }
}
