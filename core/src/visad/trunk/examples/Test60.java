import java.rmi.RemoteException;

import visad.*;
import java.util.Random;

import visad.java2d.DisplayImplJ2D;

public class Test60
	extends UISkeleton
{
  public Test60() { }

  public Test60(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType index = new RealType("index", null, null);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types = {RealType.Latitude, RealType.Longitude,
                        vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types);
    FunctionType image_tuple = new FunctionType(index, radiance);

    int size = 216;
    Set domain_set = new Integer1DSet(size);
    FlatField imaget1 = new FlatField(image_tuple, domain_set);
    float[][] values = new float[4][size];
    Random random = new Random();
    for (int i=0; i<size; i++) {
      values[0][i] = 2.0f * random.nextFloat() - 1.0f;
      values[1][i] = 2.0f * random.nextFloat() - 1.0f;
      values[2][i] = 2.0f * random.nextFloat() - 1.0f;
      values[3][i] = (float) Math.sqrt(values[0][i] * values[0][i] +
                                       values[1][i] * values[1][i]);
    }
    imaget1.setSamples(values);

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Latitude, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(ir_radiance, Display.IsoContour);
    display1.addMap(map1contour);
    ContourControl control1contour;
    control1contour = (ContourControl) map1contour.getControl();
    control1contour.enableContours(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "contours from scatter data in Java2D"; }

  public String toString()
  {
    return ": colored contours from scatter data in Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test60 t = new Test60(args);
  }
}
