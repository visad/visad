import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

public class Test27
	extends TestSkeleton
{
  static int no_self = 0;
  RealType vis_radiance;

  boolean hasClientServerMode() { return false; }

  public Test27() { }

  public Test27(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    System.out.println("  drag yellow points with right mouse button");
    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    final ScalarMap map2lat = new ScalarMap(RealType.Latitude, Display.YAxis);
    display1.addMap(map2lat);
    final ScalarMap map2lon = new ScalarMap(RealType.Longitude, Display.XAxis);
    display1.addMap(map2lon);
    final ScalarMap map2vis = new ScalarMap(vis_radiance, Display.ZAxis);
    display1.addMap(map2vis);
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setScaleEnable(true);
    mode.setPointSize(5.0f);
    mode.setPointMode(false);

    mode.setProjectionPolicy(DisplayImplJ3D.PARALLEL_PROJECTION);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    try {
      Thread.sleep(2000);
    }
    catch (InterruptedException e) {
    }
    double[] range1lat = map2lat.getRange();
    double[] range1lon = map2lon.getRange();
    double[] range1vis = map2vis.getRange();

    RealTuple direct_low = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, range1lat[0]),
                      new Real(RealType.Longitude, range1lon[0]),
                      new Real(vis_radiance, range1vis[0])});
    RealTuple direct_hi = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, range1lat[1]),
                      new Real(RealType.Longitude, range1lon[1]),
                      new Real(vis_radiance, range1vis[1])});

    final DataReferenceImpl ref_direct_low =
      new DataReferenceImpl("ref_direct_low");
    ref_direct_low.setData(direct_low);
    // color low and hi tuples yellow
    ConstantMap[][] maps = {{new ConstantMap(1.0f, Display.Red),
                             new ConstantMap(1.0f, Display.Green),
                             new ConstantMap(0.0f, Display.Blue)}};
    display1.addReferences(new DirectManipulationRendererJ3D(),
                           new DataReference[] {ref_direct_low}, maps);

    final DataReferenceImpl ref_direct_hi =
      new DataReferenceImpl("ref_direct_hi");
    ref_direct_hi.setData(direct_hi);
    display1.addReferences(new DirectManipulationRendererJ3D(),
                           new DataReference[] {ref_direct_hi}, maps);

    no_self = 0;

    CellImpl cell = new CellImpl() {
      public synchronized void doAction()
             throws VisADException, RemoteException {
        if (no_self > 0) {
          no_self--;
          if (no_self > 0) return;
        }
        RealTuple low = (RealTuple) ref_direct_low.getData();
        RealTuple hi = (RealTuple) ref_direct_hi.getData();
        double[] lows = {((Real) low.getComponent(0)).getValue(),
                         ((Real) low.getComponent(1)).getValue(),
                         ((Real) low.getComponent(2)).getValue()};
        double[] his = {((Real) hi.getComponent(0)).getValue(),
                        ((Real) hi.getComponent(1)).getValue(),
                        ((Real) hi.getComponent(2)).getValue()};
        boolean changed = false;
        for (int i=0; i<3; i++) {
          if (his[i] < lows[i] + 0.00001) {
            double m = 0.5 * (lows[i] + his[i]);
            lows[i] = m - 0.000005;
            his[i] = m + 0.000005;
            changed = true;
          }
        }

        if (changed) {
          RealTuple dlow = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, lows[0]),
                      new Real(RealType.Longitude, lows[1]),
                      new Real(vis_radiance, lows[2])});
          RealTuple dhi = new RealTuple(new Real[]
                     {new Real(RealType.Latitude, his[0]),
                      new Real(RealType.Longitude, his[1]),
                      new Real(vis_radiance, his[2])});
          ref_direct_low.setData(dlow);
          ref_direct_hi.setData(dhi);
          no_self += 2;
        }

        map2lat.setRange(lows[0], his[0]);
        map2lon.setRange(lows[1], his[1]);
        map2vis.setRange(lows[2], his[2]);
      }
    };
    cell.addReference(ref_direct_low);
    cell.addReference(ref_direct_hi);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": interactive scale"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test27 t = new Test27(args);
  }
}
