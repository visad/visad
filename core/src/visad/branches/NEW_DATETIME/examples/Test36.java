import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test36
	extends UISkeleton
{
  public Test36() { }

  public Test36(String args[])
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
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.Radius));
    ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.Longitude);
    lonmap.setRangeByUnits();
    display1.addMap(lonmap);
    // display1.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    display1.addMap(new ScalarMap(vis_radiance, Display.RGB));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "polar coordinates in Java2D"; }

  public String toString() { return ": polar coordinates in Java2D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test36 t = new Test36(args);
  }
}
