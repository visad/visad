import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;
import visad.util.ContourWidget;

public class Test37
	extends TestSkeleton
{
  private boolean reverse = false;

  public Test37() { }

  public Test37(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  int checkExtraKeyword(int argc, String args[])
  {
    reverse = true;
    return 1;
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
    RealType[] typesxx = {RealType.Longitude, RealType.Latitude};
    RealTupleType earth_locationxx = new RealTupleType(typesxx);
    FunctionType image_tuplexx = new FunctionType(earth_locationxx, radiance);

    int size = 64;
    FlatField imaget1;
    if (!reverse) {
      imaget1 = FlatField.makeField(image_tuple, size, false);
    }
    else {
      imaget1 = FlatField.makeField(image_tuplexx, size, false);
    }

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    display1.addMap(map1contour);
    ContourWidget cw = new ContourWidget(map1contour);

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setScaleEnable(true);

    JFrame jframe = new JFrame("regular contours in Java2D");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) display1.getComponent());
    jframe.pack();
    jframe.setVisible(true);

    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BorderLayout());
    big_panel.add("Center", cw);

    JFrame jframe2 = new JFrame("VisAD contour controls");
    jframe2.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe2.setContentPane(big_panel);
    jframe2.pack();
    jframe2.setVisible(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return " swap: colored contours from regular grids and ContourWidget in Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test37 t = new Test37(args);
  }
}
