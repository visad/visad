import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;
import visad.java2d.MouseBehaviorJ2D;

public class Test58
	extends UISkeleton implements DisplayListener
{

  ProjectionControl control;

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
 
    control = dpys[0].getProjectionControl();
    dpys[0].addDisplayListener(this);
    rotate(dpys[0]);
  }

  public void displayChanged(DisplayEvent e)
         throws VisADException, RemoteException {
    if (e.getId() == DisplayEvent.FRAME_DONE) {
      rotate((DisplayImpl) e.getDisplay());
    }
  }

  public void rotate(DisplayImpl display)
         throws VisADException, RemoteException {
    double[] matrix = control.getMatrix();
    double[] mult = display.make_matrix(0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0);
    control.setMatrix(display.multiply_matrix(mult, matrix));
  }

  public String toString() { return ": scripted fly-through in Java2D"; }

  public static void main(String args[])
         throws VisADException, RemoteException
  {
    Test58 t = new Test58(args);
  }
}
