import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.util.LabeledRGBWidget;
import visad.java3d.DisplayImplJ3D;

public class Test33
	extends UISkeleton
{
  LabeledRGBWidget lw;

  public Test33() { }

  public Test33(String args[])
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
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    ScalarMap color1map = new ScalarMap(ir_radiance, Display.RGB);
    display1.addMap(color1map);

    float[][] table = new float[3][256];
    for (int i=0; i<256; i++) {
      float a = ((float) i) / 256.0f;
      table[0][i] = a;
      table[1][i] = 1.0f - a;
      table[2][i] = 0.5f;
    }

    lw = new LabeledRGBWidget(color1map, 0.0f, 32.0f, table);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "VisAD Color Widget"; }

  Component getSpecialComponent() { return lw; }

  public String toString() { return ": ColorWidget with non-default table"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test33 t = new Test33(args);
  }
}
