import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;

public class Test05
	extends UISkeleton
{
  public Test05() { }

  public Test05(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    display1.addMap(map1contour);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "VisAD contour controls"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    ScalarMap map1contour = (ScalarMap )dpys[0].getMapVector().lastElement();
    return new ContourWidget(map1contour);
  }

  public String toString()
  {
    return ": colored 2-D contours from regular grids and ContourWidget";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test05 t = new Test05(args);
  }
}
