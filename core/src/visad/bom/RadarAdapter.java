
//
// RadarAdapter.java
//

/*
This sofware is part of the Australian Integrated Forecast System (AIFS)
Copyright (C) 2023 Bureau of Meteorology
*/

package visad.bom;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.JFrame;

import visad.CommonUnit;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.FlatField;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.Gridded3DSet;
import visad.Integer2DSet;
import visad.Integer3DSet;
import visad.QuickSort;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.java3d.DisplayImplJ3D;

/**
 * RadarAdapter
 * 
 * @author - James Kelly : J.Kelly@bom.gov.au - Bill Hibbard (mainly, while
 *         working at BOM August 1999)
 * 
 * 
 */

public class RadarAdapter {
	
  public RadarFile rf;
  public class PolarData {
    public double azimuth;
    public double range;
  }

  public PolarData[] polar;
  public int numVectors;

  FlatField radar;

  /**
   * @deprecated
   */
  public RadarAdapter(float centlat, float centlon, String radarSource,
                      boolean d3d)
         throws IOException, VisADException {
    this(centlat, centlon, 0.0f, radarSource, d3d);
  }

  public RadarAdapter(float centlat, float centlon, float centalt,
                      String radarSource, boolean d3d)
         throws IOException, VisADException {
    try {
      rf = new RadarFile(radarSource);
      // buildFlatField(rf);
    } catch (IOException e) {throw new
          VisADException("Problem with Radar file: " + e);
    }
    System.out.println("Radar Adapter : dtTime = " + rf.dtTime);
    int naz = rf.pbdataArray.length;
    float[] azs = new float[naz];
    int nrad = 0;
    //
    // create an array "azs" containing all the azimuth values
    // example:
  	//    azs[0] = 61 degrees    = radlow +   0
		//    azs[1] = 183 degrees   = radlow + 122
    //    azs[2] = 261 degrees   = radlow + 200
    //    azs[3] = 262 degrees   = radlow + 201
    //    azs[4] = 262 degrees   = radlow + 202
    //
    for (int i=0; i<naz; i++) {
      int n = rf.pbdataArray[i].bdata.length;
      if (n > nrad) nrad = n;
      azs[i] = (float) rf.pbdataArray[i].azimuth;
      // System.out.println("i, azs = " + i + " " + azs[i]);
    }
    int[] sortToOld = QuickSort.sort(azs);
    //    radlow : distance from radar of 1st "echo" (radial), in metres eg 4000m
    float radlow = rf.startrng;
    //    radres : distance between subsequent radials eg 500m
    float radres = rf.rngres;
    float azlow = azs[0];
    float azres = rf.azimuthres;
    //
	//
	// azs above is the "old" array
	// prepare to create a "new" array containing a continuous set of
    // azimuth values between azs[0] and azs[naz-1] degrees
	// newnaz is the number of continuous azimuth values
    //

    int newnaz = 1 + (int) ((azs[naz-1] - azs[0]) / azres);
    int[] newToOld = new int[newnaz];
    // for (int i=0; i<newnaz; i++) newToOld[i] = -1;
    for (int i=0; i<newnaz; i++) newToOld[i] = 0;
	//
	// The old array above (azs) has only got "azimuth" values for
	// radials with non-null data
	// We want to fill in the array with these nulls
    // So using the example above, newToOld will contain:
	//    newToOld[0] = 0
	//    newToOld[1] = -1 ......
	//    newToOld[122] = 1
	//    newToOld[123] = -1 ......
    //    newToOld[200] = 2
    //    newToOld[201] = 3
    //    newToOld[202] = 4
    //    newToOld[203] = -1.......
	//
    for (int i=0; i<naz; i++) {
      int k = (int) ((azs[i] - azs[0]) / azres);
      if (k < 0) k = 0;
      if (k > (newnaz-1)) k = newnaz-1;
      newToOld[k] = sortToOld[i];
      // System.out.print("k, newToOld = " + k + " " + newToOld[k] + " ");
    }
    // System.out.println(" ");

    Radar2DCoordinateSystem rcs2d = null;
    Radar3DCoordinateSystem rcs3d = null;
    float elevlow = 0.5f; // degrees
    float elevres = 0.1f; // degrees
    int nelev = 1;
    if (d3d) {
      //ref = new RealTupleType
      //        (RealType.Latitude, RealType.Longitude, RealType.Altitude);
      rcs3d = new Radar3DCoordinateSystem(centlat, centlon, centalt,
                   radlow, radres, azlow, azres, elevlow, elevres);
    }
    else {
      //ref = new RealTupleType (RealType.Latitude, RealType.Longitude);
      rcs2d = new Radar2DCoordinateSystem(centlat, centlon,
                             radlow, radres, azlow, azres);
    }

    RealType azimuth =
      RealType.getRealType("azimuth", CommonUnit.degree, null);
    RealType range = RealType.getRealType("range", CommonUnit.meter, null);
    // WLH 14 Oct 99
    // RealType elevation =
    //   RealType.getRealType("elevation", CommonUnit.meter, null);
    RealType elevation =
      RealType.getRealType("elevation", CommonUnit.degree, null);

    RealTupleType radaz = null;
    if (d3d) {
      RealType[] domain_components = { range, azimuth, elevation};
      radaz = new RealTupleType(domain_components, rcs3d, null);
    }
    else {
      RealType[] domain_components = {range, azimuth};
      radaz = new RealTupleType(domain_components, rcs2d, null);
    }

    RealType reflection = RealType.getRealType("reflection");

    FunctionType radar_image = new FunctionType(radaz, reflection);
	  //
    //    newnaz = 203 using example above
	  //
    // System.out.println("newnaz = " + newnaz + "  nrad = " + nrad);

    // float[][] samples = new float[2][nrad * naz];
	//
	// For convenience, the "values" array has all the
	// > 0 data values at the start of the array (indexed by k)
	// while null data values are stored together at the end of the array
	//

    // WLH - 21 Sept 99
    int bignaz = newnaz;
    if (newnaz == 360) bignaz = 361;

    float[][] values = new float[1][nrad * bignaz]; // WLH - 21 Sept 99
    int m = 0;
    for (int i=0; i<newnaz; i++) {
      int k = newToOld[i];
      if (k >= 0) {
	    // there is data for this azimuth
        byte[] bd = rf.pbdataArray[newToOld[i]].bdata;
        for (int j=0; j<nrad; j++) {
          values[0][m] = bd[j];
   		  // if (bd[j] > 0) System.out.println("i, j = " + i + " " + j + " values = " + values[0][m] );
          m++;
        }
      }
      else { // k < 0
	    //
	    // fill the rest of the array with NaNs
	    //
        for (int j=0; j<nrad; j++) {
          values[0][m] = Float.NaN;
          m++;
        }
      }
    }

    // WLH - 21 Sept 99
    if (newnaz == 360) {
      int offset = nrad * newnaz;
      for (int j=0; j<nrad; j++) values[0][offset + j] = values[0][j];
    }

    if (d3d) {
      if (nelev == 1) {
        float[][] samples = new float[3][nrad * bignaz];
        int k = 0;
        for (int j=0; j<bignaz; j++) {
          for (int i=0; i<nrad; i++) {
            samples[0][k] = i;
            samples[1][k] = j;
            samples[2][k] = 0;
            k++;
          }
        }
        Gridded3DSet set = new Gridded3DSet(radaz, samples, nrad, bignaz);
        radar = new FlatField(radar_image, set);
      }
      else {
        Integer3DSet set = new Integer3DSet(radaz, nrad, bignaz, nelev);
        radar = new FlatField(radar_image, set);
      }
    }
    else {
      // Gridded2DSet set = new Gridded2DSet(radaz, samples, nrad, naz);
      Integer2DSet set = new Integer2DSet(radaz, nrad, bignaz); // WLH - 21 Sept 99
      radar = new FlatField(radar_image, set);
    }
    radar.setSamples(values);

  }

  public FlatField getData() {
    return radar;
  }


  public static void main(String[] args) throws VisADException, RemoteException {
    String radarSource = "radar.dat";
    RadarAdapter ra = null;
    try {
        ra = new RadarAdapter(-34.9f, 138.5f, 4.0f, radarSource, false);

    } catch (Exception e) {
      System.err.println("Caught Exception for \"" + radarSource + "\": " +
                           e);
      System.exit(1);
    }

    FlatField radar = ra.getData();
    FunctionType radar_image = (FunctionType) radar.getType();
    RealTupleType radaz = radar_image.getDomain();
    RealType reflection = (RealType) radar_image.getRange();

    DisplayImplJ3D display = new DisplayImplJ3D("radar");
    ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    //lonmap.setRange(130.0, 150.0);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(latmap);
    display.addMap(new ScalarMap(RealType.Altitude, Display.ZAxis));
    //latmap.setRange(-45.0, -25.0);
    //ScalarMap reflectionmap = new ScalarMap(reflection, Display.ZAxis);
    //display.addMap(reflectionmap);
    //reflectionmap.setRange(0, 6);
    // display.addMap(new ScalarMap(reflection, Display.RGB));
    ScalarMap rgbMap = new ScalarMap(reflection, Display.RGB);
    //ScalarMap rgbMap = new ScalarMap(reflection, Display.RGBA);
	  // rgbMap.setRange(0.,6.);
	display.addMap(rgbMap);



    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
    // mode.setTextureEnable(false); WLH - 22 Sept 99

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
    /*
    LabeledColorWidget lw = new LabeledColorWidget(rgbMap);
    JFrame widgetFrame = new JFrame("VisAD Color Widget");
    widgetFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    widgetFrame.getContentPane().add(lw);
    widgetFrame.setSize(lw.getPreferredSize());
    widgetFrame.setVisible(true);
    */
  }
}

