package visad.test;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.ContourControl;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.Field;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.RealType;
import visad.ScalarMap;
import visad.data.netcdf.Plain;
import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;

public class ContourHardTest {

	public static void main(String[] args) throws Exception {

	  String fn = "";
	  try {
	    fn = args[0];
	  } catch (Exception e) {
	    System.err.println("Must provide a netcdf file readable by visad.data.netcdf.Plain");
	    System.exit(1);
	  }
	  
	  System.err.println("Reading data...");
	  Plain plain = new Plain();
	  Field field = (Field) plain.open(args[0]);
		
		DisplayImpl display = new DisplayImplJ3D("display");
		GraphicsModeControl gmc = display.getGraphicsModeControl();
		gmc.setTextureEnable(false);
		//gmc.setAdjustProjectionSeam(true);
		
		DataReference ref = new DataReferenceImpl("ref");
		ref.setData(field);
		display.addReference(ref);

		FunctionType fcn = (FunctionType) field.getType();
		RealType xType = (RealType) fcn.getDomain().getComponent(0);
		RealType yType = (RealType) fcn.getDomain().getComponent(1);
		
		display.addMap(new ScalarMap(xType, Display.XAxis));
		display.addMap(new ScalarMap(yType, Display.YAxis));
		//display.addMap(new ScalarMap(RealType.Altitude, Display.RGB));
		display.addMap(new ScalarMap(RealType.Altitude, Display.IsoContour));
		
		System.err.println("Setting up GUI...");
		
		JFrame jframe = new JFrame();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.add(display.getComponent());
		jframe.pack();
		jframe.setVisible(true);

		ScalarMap map = (ScalarMap) display.getMapVector().lastElement();
		ContourControl ctl = (ContourControl) map.getControl();
		ctl.enableLabels(true);
		ctl.setDashedStyle(GraphicsModeControl.DASH_STYLE);
		ContourWidget cw = new ContourWidget(map);

		JPanel big_panel = new JPanel();
		big_panel.setLayout(new BorderLayout());
		big_panel.add("Center", cw);

		JFrame jframe2 = new JFrame();
		jframe2.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jframe2.setContentPane(big_panel);
		jframe2.pack();
		jframe2.setVisible(true);

		System.err.println("Init done");
	}
}
