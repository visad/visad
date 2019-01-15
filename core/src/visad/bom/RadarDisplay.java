
//
// RadarDisplay.java
//

/*
This sofware is part of the Australian Integrated Forecast System (AIFS)
Copyright (C) 2019 Bureau of Meteorology
*/

package visad.bom;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

import javax.swing.JFrame;

import visad.ConstantMap;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.FlatField;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.data.mcidas.BaseMapAdapter;
import visad.java3d.DisplayImplJ3D;
import visad.util.LabeledColorWidget;

/**
 * RadarDisplay
 * 
 * @author - James Kelly : J.Kelly@bom.gov.au based on RadarAdapter.java,
 *         largely written by Bill Hibbard
 * 
 * 
 */

public class RadarDisplay {

	/*
     * Set the color map.
     */
    private static void
    setColorMap(ScalarMap colorMap, float min, float max)
			throws VisADException, RemoteException
    {

            float[][] table = new float[3][256];
            for (int i=0; i<256; i++) {
                    if (i <= 15)
                    {
                        table[0][i] = 0.0f;
                        table[1][i] = 0.0f;
                        table[2][i] = 0.0f;
										}
                    else if (i > 15 && i <= 31)
                    {
                        table[0][i] = 0.0f;
                        table[1][i] = 240.0f/255.0f;
                        table[2][i] = 240.0f/255.0f;
                    }
                    else if (i > 31 && i <= 47)
										{
                       table[0][i] = 0.0f;
                        table[1][i] = 144.0f/255.0f;
                        table[2][i] = 144.0f/255.0f;
                    }
                    else if (i > 47 && i <= 63)
                    {
                        table[0][i] = 128.0f/255.0f;
												table[1][i] = 224.0f/255.0f;
                        table[2][i] = 80.0f/255.0f;
                    }
                    else if (i > 63 && i <= 79)
                    {
                        table[0][i] = 100.0f/255.0f;
                        table[1][i] = 184.0f/255.0f;
                        table[2][i] = 64.0f/255.0f;
                    }
                    else if (i > 79 && i <= 95)
                    {
                        table[0][i] = 72.0f/255.0f;
                        table[1][i] = 144.0f/255.0f;
                        table[2][i] = 48.0f/255.0f;
                    }
                    else if (i > 95 && i <= 111)
                    {
                        table[0][i] = 44.0f/255.0f;
                        table[1][i] = 104.0f/255.0f;
                        table[2][i] = 32.0f/255.0f;
                    }
                    else if (i > 111 && i <= 127)
                    {
                        table[0][i] = 16.0f/255.0f;
                        table[1][i] = 64.0f/255.0f;
                        table[2][i] = 16.0f/255.0f;
                    }
                    else if (i > 127 && i <= 143)
                    {
                        table[0][i] = 240.0f/255.0f;
                        table[1][i] = 192.0f/255.0f;
                        table[2][i] = 16.0f/255.0f;
                    }
                    else if (i > 143 && i <= 159)
                    {
                        table[0][i] = 240.0f/255.0f;
                        table[1][i] = 128.0f/255.0f;
                        table[2][i] = 32.0f/255.0f;
                    }
                    else if (i > 159 && i <= 175)
                    {
                        table[0][i] = 240.0f/255.0f;
                        table[1][i] = 16.0f/255.0f;
                        table[2][i] = 32.0f/255.0f;
                    }
                    else if (i > 175 && i <= 191)
                    {
                        table[0][i] = 144.0f/255.0f;
                        table[1][i] = 0.0f/255.0f;
                        table[2][i] = 0.0f/255.0f;
                    }
                    else if (i > 191 && i <= 207)
                    {
                        table[0][i] = 176.0f/255.0f;
                        table[1][i] = 32.0f/255.0f;
                        table[2][i] = 128.0f/255.0f;
                    }
                    else if (i > 207 && i <= 223)
                    {
                        table[0][i] = 202.0f/255.0f;
                        table[1][i] = 64.0f/255.0f;
                        table[2][i] = 160.0f/255.0f;
                    }
                    else if (i > 223 && i <= 239)
                    {
                        table[0][i] = 255.0f/255.0f;
                        table[1][i] = 255.0f/255.0f;
                        table[2][i] = 255.0f/255.0f;
                    }
                    else if (i > 239 && i <= 255)
                    {
                        table[0][i] = 255.0f/255.0f;
                        table[1][i] = 128.0f/255.0f;
                        table[2][i] = 224.0f/255.0f;
                    }

            }
      LabeledColorWidget lw =
	  	new LabeledColorWidget(colorMap, table);

      Frame frame = new Frame("VisAD Color Widget");
      frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      });
      frame.add(lw);
      frame.setSize(lw.getPreferredSize());
      frame.setVisible(true);
    }

		private static void mapDisplay(DisplayImplJ3D d, String mapFile)
		{
		BaseMapAdapter baseMap;

		try {
			baseMap = new BaseMapAdapter(mapFile);
		  // lat_map = new ScalarMap(RealType.Latitude, Display.YAxis);
      // lon_map = new ScalarMap(RealType.Longitude, Display.XAxis);

	    DataReference maplines_ref = new DataReferenceImpl("MapLines");
      maplines_ref.setData(baseMap.getData());

      ConstantMap[] colMap;
      colMap = new ConstantMap[4];
      colMap[0] = new ConstantMap(1., Display.Green);
      colMap[1] = new ConstantMap(1., Display.Red);
      colMap[2] = new ConstantMap(0., Display.Blue);
      colMap[3] = new ConstantMap(-0.99, Display.ZAxis);
	    d.addReference(maplines_ref, colMap);

		} catch (Exception ne) {ne.printStackTrace(); System.exit(1); }
   	}

  public static void main(String[] args) throws VisADException, RemoteException {
		// Adelaide Airport: location of example radar data file radar.dat
	  float centlat = -34.9581f;
	  float centlon = 138.5342f;
		float radius = 6.0f; // degrees
    String radarSource = "radar.dat";
    RadarAdapter ra = null;
    boolean d3d = (args.length > 0);
    try {
        ra = new RadarAdapter(centlat, centlon, 0.0f, radarSource, d3d);
    } catch (Exception e) {
      System.err.println("Caught Exception for \"" + radarSource + "\": " +
                           e);
      System.exit(1);
    }

    FlatField radar = ra.getData();

/* WLH
DumpType.dumpDataType(radar, System.out);
VisAD Data analysis
    FlatField of length = 54000
    ((range, azimuth) -> reflection)
      Domain has 2 components:
        Integer2DSet: Length = 54000
          0. Integer1DSet (range) Range = 0 to 149
          1. Integer1DSet (azimuth) Range = 0 to 359
      Range has 1 components:
        0. FloatSet (reflection) Dimension = 1
        0. number missing = 0
*/

    FunctionType radar_image = (FunctionType) radar.getType();
    RealType reflection = (RealType) radar_image.getRange();

    DisplayImplJ3D display = new DisplayImplJ3D("radar");
    ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.XAxis);
		// centre lon 140
    lonmap.setRange(centlon - radius, centlon + radius);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(latmap);
		// centre lat -32
    latmap.setRange(centlat - radius, centlat + radius);
    if (d3d) {
      ScalarMap altitudemap = new ScalarMap(RealType.Altitude, Display.ZAxis);
      altitudemap.setRange(0, 30000);
      display.addMap(altitudemap);
    }
    else {
      ScalarMap reflectionmap = new ScalarMap(reflection, Display.ZAxis);
      reflectionmap.setRange(0.f, 6.f);
      display.addMap(reflectionmap);
    }
    ScalarMap rgbMap = new ScalarMap(reflection, Display.RGB);

	  display.addMap(rgbMap);
		setColorMap(rgbMap, 0.f, 6.f);
		mapDisplay(display, "OUTLAUST");

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
    // mode.setTextureEnable(false); WLH - 21 Sept 99

    DataReference ref = new DataReferenceImpl("radar_ref");
    ref.setData(radar);
    display.addReference(ref);

    JFrame frame = new JFrame("VisAD BOM radar image");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    frame.getContentPane().add(display.getComponent());
    int WIDTH = 500;
    int HEIGHT = 600;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);

  }
}

