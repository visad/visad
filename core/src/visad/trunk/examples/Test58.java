import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test58
	extends UISkeleton
{
  public Test58() { }

  public Test58(String args[])
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
    display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "fly-through in Java2D"; }
 
  void setupUI(DisplayImpl[] dpys)
        throws VisADException, RemoteException
  {
    JFrame jframe  = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
 
    jframe.setContentPane((JPanel) dpys[0].getComponent());
    jframe.pack();
    jframe.setVisible(true);
 
    ProjectionControl control = dpys[0].getProjectionControl();
    double[] matrix = control.getMatrix();
 
    double sa = Math.sin(0.01);
    double ca = Math.cos(0.01);
 
    while (true) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      System.out.println("\ndelay\n");
      double a =  ca * matrix[0] + sa * matrix[3];
      double b =  ca * matrix[1] + sa * matrix[4];
      double c = -sa * matrix[0] + ca * matrix[3];
      double d = -sa * matrix[1] + ca * matrix[4];
      matrix[0] = a;
      matrix[1] = b;
      matrix[3] = c;
      matrix[4] = d;
      control.setMatrix(matrix);
    }
/*
if this doesn't work, send DisplayEvents (FRAME_DONE) from
VisADCanvasJ3D.postSwap and from VisADCanvasJ2D.paint
*/
  }

  public String toString() { return ": scripted fly-through in Java2D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test58 t = new Test58(args);
  }
}
