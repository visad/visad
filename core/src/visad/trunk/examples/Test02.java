import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;

public class Test02
	extends TestSkeleton
{
  public Test02() { }

  public Test02(String args[])
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

    int size3d = 6;
    float level = 2.5f;
    FlatField grid3d = FlatField.makeField(grid_tuple, size3d, true);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    display1.addMap(map1contour);

    ContourWidget cw = new ContourWidget(map1contour);
    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BorderLayout());
    big_panel.add("Center", cw);

    DataReferenceImpl ref_grid3d = new DataReferenceImpl("ref_grid3d");
    ref_grid3d.setData(grid3d);
    display1.addReference(ref_grid3d, null);

    JFrame jframe = new JFrame("VisAD iso-level controls");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe.setContentPane(big_panel);
    jframe.pack();
    jframe.setVisible(true);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return ": colored iso-surfaces from irregular grids and ContourWidget";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test02 t = new Test02(args);
  }
}
