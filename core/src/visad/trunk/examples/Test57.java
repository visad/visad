import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.util.LabeledRGBWidget;
import visad.java3d.DisplayImplJ3D;

public class Test57
	extends UISkeleton
{
  public Test57() { }

  public Test57(String args[])
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
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    ProjectionControl control = display1.getProjectionControl();
    double[] matrix = control.getMatrix();

    double sa = Math.sin(0.01);
    double ca = Math.cos(0.01);

    boolean forever = true;
    while (forever) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      System.out.println("\ndelay\n");
      double a =  ca * matrix[0] + sa * matrix[4];
      double b =  ca * matrix[1] + sa * matrix[5];
      double c = -sa * matrix[0] + ca * matrix[4];
      double d = -sa * matrix[1] + ca * matrix[5];
      matrix[0] = a;
      matrix[1] = b;
      matrix[4] = c;
      matrix[5] = d;
      control.setMatrix(matrix);
    }

/*
if this doesn't work, send DisplayEvents (FRAME_DONE) from
VisADCanvasJ3D.postSwap and from VisADCanvasJ2D.paint
*/

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return ": scripted fly-through in Java3D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test57 t = new Test57(args);
  }
}
