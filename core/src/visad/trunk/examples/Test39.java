import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import java.awt.Dimension;
import visad.java2d.DisplayRendererJ2D;
import visad.util.LabeledRGBWidget;
import visad.java2d.DisplayImplJ2D;

public class Test39
	extends TestSkeleton
{
  public Test39() { }

  public Test39(String args[])
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

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));

    ScalarMap color1map = new ScalarMap(vis_radiance, Display.RGB);
    display1.addMap(color1map);

    LabeledRGBWidget lw = new LabeledRGBWidget(color1map, 0.0f, 32.0f);

    ((DisplayRendererJ2D) display1.getDisplayRenderer()).getCanvas().
      setPreferredSize(new Dimension(256, 256));

    JFrame jframe = new JFrame("VisAD Color Widget in Java2D");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.Y_AXIS));
    big_panel.add(lw);
    JPanel lil_panel = new JPanel();
    lil_panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    lil_panel.setLayout(new BorderLayout());
    lil_panel.add("Center", display1.getComponent());
    big_panel.add(lil_panel);
    jframe.setContentPane(big_panel);
    jframe.setSize(400, 600);
    jframe.setVisible(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return ": color array and ColorWidget in Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test39 t = new Test39(args);
  }
}
