import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Graphics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test51
	extends TestSkeleton
{
  public Test51() { }

  public Test51(String args[])
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

    DisplayImpl display1 = new DisplayImplJ3D("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.RGB));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "capture image in Java3D"; }

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

    JFrame jframe1 = new JFrame("captured image from Java3D");
    jframe1.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
    panel1.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel1.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    jframe1.setContentPane(panel1);
    jframe1.pack();
    jframe1.setVisible(true);
    jframe1.setSize(jframe.getSize().width, jframe.getSize().height);

    while (true) {
      Graphics gp = panel1.getGraphics();
      BufferedImage image = dpys[0].getImage();
      gp.drawImage(image, 0, 0, panel1);
      gp.dispose();
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
      }
    }
  }

  public String toString() { return ": test image capture in Java3D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test51 t = new Test51(args);
  }
}
