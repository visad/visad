
//
// RadarDisplay.java
//

/*
This sofware is part of the Australian Integrated Forecast System (AIFS)
Copyright (C) 1999 Bureau of Meteorology 
*/

package visad.bom;

import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import visad.*;
import visad.java3d.*;
import visad.util.LabeledColorWidget;
import visad.data.mcidas.BaseMapAdapter;
import visad.jmet.DumpType;

/** 
 * RadarDisplay
 *
 * @authors - James Kelly : J.Kelly@bom.gov.au
 *            based on RadarAdapter.java, largely written by Bill Hibbard
 *            
 * 
 */

public class RadarDisplay {

	/*
     * Set the color map.
     */
    private static void
    setColorMap(ScalarMap colorMap, double min, double max)
			throws VisADException, RemoteException
    {
      LabeledColorWidget lw =
	  	new LabeledColorWidget(colorMap);

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
		ScalarMap lat_map;
        ScalarMap lon_map;
        ScalarMap xaxis;
        ScalarMap yaxis;

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
/*
	 private static void doBaseMap(DisplayImplJ3D d, String mapFile) {
		BaseMapAdapter baseMap;
		RealType enableMap = new RealType("enableMap");

    try {
      baseMap = new BaseMapAdapter(mapFile);
      // baseMap.setDomainSet(ng.getDomainSet() );
      Data mapData = baseMap.getData();

      // set up so map can be toggled on/off

			FunctionType mapType = new FunctionType(mapData.getType(), reflection);

      Integer1DSet mapSet= new Integer1DSet(enableMap, 2);
      mapField = new FieldImpl(mapType, mapSet);
      mapField.setSample(0,mapData);
      mapControl = (ValueControl) mapMap.getControl();
      // if (mapRef != null) di.removeReference(mapRef);
      mapRef = new DataReferenceImpl("mapData");
      mapRef.setData(mapField);
      ConstantMap[] rendMap;
      rendMap = new ConstantMap[1];
      rendMap[0] = new ConstantMap(-.99, Display.ZAxis);
      d.addReference(mapRef, rendMap);
      //di.addReference(mapRef);
      mapControl.setValue(0.0);

    } catch (Exception mapop) {mapop.printStackTrace(); System.exit(1); }

  }

*/
  public static void main(String[] args) throws VisADException, RemoteException {
		// Adelaide Airport: location of example radar data file radar.dat
	  float centlat = -34.9581f;
	  float centlon = 138.5342f;
		float radius = 3.0f; // degrees 
    String radarSource = "radar.dat";
    RadarDisplay rd = null;
    RadarAdapter ra = null;
    try {
        ra = new RadarAdapter(centlat, centlon, radarSource, false);
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
    RealTupleType radaz = radar_image.getDomain();
    RealType reflection = (RealType) radar_image.getRange();
    RealType azimuth = (RealType) radaz.getComponent(1); // bug jk: was (0)
    RealType range = (RealType) radaz.getComponent(0);

    DisplayImplJ3D display = new DisplayImplJ3D("radar");
    ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.XAxis);
		// centre lon 140
    lonmap.setRange(centlon - radius, centlon + radius);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(latmap);
		// centre lat -32
    latmap.setRange(centlat - radius, centlat + radius);
    ScalarMap reflectionmap = new ScalarMap(reflection, Display.ZAxis);
    reflectionmap.setRange(0, 10);
    display.addMap(reflectionmap);
    ScalarMap rgbMap = new ScalarMap(reflection, Display.RGB);

	  display.addMap(rgbMap);
		setColorMap(rgbMap, 0., 6.);
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

