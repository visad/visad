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
    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    int size3d = 16;
    FlatField grid3d = FlatField.makeField(grid_tuple, size3d, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
    ScalarMap map1color = new ScalarMap(vis_radiance, Display.RGBA);
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
    float[][] newtable = new float[table.length][length];
    for (int i=0; i<length; i++) {
      float a = ((float) i) / ((float) (table[3].length - 1));
      newtable[3][i] = (1.0f - a) * (1.0f - a) * (1.0f - a);
      newtable[0][i] = table[0][(length - 1) - i];
      newtable[1][i] = table[1][(length - 1) - i];
      newtable[2][i] = table[2][(length - 1) - i];
    }
    control.setTable(newtable);

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
