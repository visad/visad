import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.LabeledRGBAWidget;

public class Test61
	extends UISkeleton
{
  public Test61() { }

  public Test61(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType xr = new RealType("xr");
    RealType yr = new RealType("yr");
    RealType zr = new RealType("zr");
    RealType wr = new RealType("wr");
    RealType[] types3d = {xr, yr, zr};
    RealTupleType earth_location3d = new RealTupleType(types3d);
    FunctionType grid_tuple = new FunctionType(earth_location3d, wr);

    int NX = 32;
    int NY = 32;
    int NZ = 32;
    Integer3DSet set = new Integer3DSet(NX, NY, NZ);
    FlatField grid3d = new FlatField(grid_tuple, set);

    float[][] values = new float[1][NX * NY * NZ];
    int k = 0;
    for (int iz=0; iz<NZ; iz++) {
      double z = Math.PI * (-1.0 + 2.0 * iz / (NZ - 1.0));
      for (int iy=0; iy<NY; iy++) {
        double y = -1.0 + 2.0 * iy / (NY - 1.0);
        for (int ix=0; ix<NX; ix++) {
          double x = -1.0 + 2.0 * ix / (NX - 1.0);
          double r = 2.0 * Math.sqrt(x * x + y * y) - 1.0;
          double s = z - Math.atan2(y, x);
          double dist = Math.sqrt(r * r + s * s);
          values[0][k] = (float) ((dist < 0.1) ? 10.0 : 1.0 / dist);
          k++;
        }
      }
    }
    grid3d.setSamples(values);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

    display1.addMap(new ScalarMap(xr, Display.XAxis));
    display1.addMap(new ScalarMap(yr, Display.YAxis));
    display1.addMap(new ScalarMap(zr, Display.ZAxis));
    ScalarMap map1color = new ScalarMap(wr, Display.RGBA);
    display1.addMap(map1color);

    DataReferenceImpl ref_grid3d = new DataReferenceImpl("ref_grid3d");
    ref_grid3d.setData(grid3d);
    display1.addReference(ref_grid3d, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle0() { return "VisAD Color Alpha Widget"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    ScalarMap map1color = (ScalarMap )dpys[0].getMapVector().lastElement();
    LabeledRGBAWidget widget = new LabeledRGBAWidget(map1color);

    // reverse the red & blue ends of the table for purely
    // esthetic reasons, and taper the alpha
    ColorAlphaControl control = (ColorAlphaControl) map1color.getControl();
    float[][] table = control.getTable();
    int length = table[0].length;
    // float[][] newtable = new float[table.length][length];
    for (int i=0; i<length; i++) {
      float a = ((float) i) / ((float) (table[3].length - 1));
      table[3][i] = a;
    }
    // control.setTable(newtable);
    control.setTable(table);

    return widget;
  }

  public String toString()
  {
    return ": volume rendering and ColorAlphaWidget";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test61 t = new Test61(args);
  }
}
