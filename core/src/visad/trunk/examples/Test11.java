import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test11
	extends TestSkeleton
{
  public Test11() { }

  public Test11(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType x = new RealType("x", null, null);
    RealType y = new RealType("y", null, null);
    Unit super_degree = CommonUnit.degree.scale(2.5);
    RealType lon = new RealType("lon", super_degree, null);
    RealType radius = new RealType("radius", null, null);
    RealTupleType cartesian = new RealTupleType(x, y);
    PolarCoordinateSystem polar_coord_sys =
      new PolarCoordinateSystem(cartesian);
    RealTupleType polar =
      new RealTupleType(lon, radius, polar_coord_sys, null);

    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);

    FunctionType image_polar = new FunctionType(polar, radiance);
    Unit[] units = {super_degree, null};
    Linear2DSet domain_set =
      new Linear2DSet(polar, 0.0, 60.0, 61, 0.0, 60.0, 61,
                      polar_coord_sys, units, null);
    FlatField imaget1 = new FlatField(image_polar, domain_set);
    FlatField.fillField(imaget1, 1.0, 30.0);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(x, Display.XAxis));
    display1.addMap(new ScalarMap(y, Display.YAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    display1.addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": CoordinateSystem and Unit"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test11 t = new Test11(args);
  }
}
