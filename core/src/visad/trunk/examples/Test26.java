import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test26
	extends TestSkeleton
{
  public Test26() { }

  public Test26(String args[])
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

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    ScalarMap map1lat = new ScalarMap(RealType.Latitude, Display.YAxis);
    display1.addMap(map1lat);
    ScalarMap map1lon = new ScalarMap(RealType.Longitude, Display.XAxis);
    display1.addMap(map1lon);
    ScalarMap map1vis = new ScalarMap(vis_radiance, Display.ZAxis);
    display1.addMap(map1vis);
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setScaleEnable(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    boolean forever = true;
    while (forever) {
      // delay(5000);
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
      }
      System.out.println("\ndelay\n");
      double[] range1lat = map1lat.getRange();
      double[] range1lon = map1lon.getRange();
      double[] range1vis = map1vis.getRange();
      double inclat = 0.05 * (range1lat[1] - range1lat[0]);
      double inclon = 0.05 * (range1lon[1] - range1lon[0]);
      double incvis = 0.05 * (range1vis[1] - range1vis[0]);
      map1lat.setRange(range1lat[1] + inclat, range1lat[0] - inclat);
      map1lon.setRange(range1lon[1] + inclon, range1lon[0] - inclon);
      map1vis.setRange(range1vis[1] + incvis, range1vis[0] - incvis);
    }

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": scale"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test26 t = new Test26(args);
  }
}
