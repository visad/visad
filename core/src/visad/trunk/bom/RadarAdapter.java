
//
// RadarAdapter.java
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

/** 
 * RadarDisplay
 *
 * @authors - James Kelly : J.Kelly@bom.gov.au
 *          - Bill Hibbard (mainly, while working at BOM August 1999) 
 *            
 * 
 */
public class RadarAdapter {
  public RadarFile rf;
  public class PolarData {
    public double azimuth;
    public double range;
  }
  private Vector pvector = new Vector();
  private PolarData pdata;
  public PolarData[] polar;
  public int numVectors;

  FlatField radar;

  public RadarAdapter(float centlat, float centlon, String radarSource)
         throws IOException, VisADException {
    try {
      rf = new RadarFile(radarSource);
      // buildFlatField(rf);
    } catch (IOException e) {throw new
          VisADException("Problem with Radar file: " + e);
    }
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

    RealTupleType ref = new RealTupleType
                  (RealType.Latitude, RealType.Longitude);
    Radar2DCoordinateSystem rcs =
      new Radar2DCoordinateSystem(ref, centlat, centlon, radlow, radres,
                                  azlow, azres);

    RealType azimuth = new RealType("azimuth", CommonUnit.degree, null);
    RealType range = new RealType("range", CommonUnit.meter, null);
    RealType[] domain_components = {range, azimuth};
    RealTupleType radaz = new RealTupleType(domain_components, rcs, null);
    RealType reflection = new RealType("reflection");
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

    // Gridded2DSet set = new Gridded2DSet(radaz, samples, nrad, naz);
    Integer2DSet set = new Integer2DSet(radaz, nrad, bignaz); // WLH - 21 Sept 99
    radar = new FlatField(radar_image, set);
    radar.setSamples(values);

  }

  public FlatField getData() {
    return radar;
  }


  public static void main(String[] args) throws VisADException, RemoteException {
    String radarSource = "radar.dat";
    RadarAdapter ra = null;
    try {
        ra = new RadarAdapter(-30.0f, 140.0f, radarSource);

    } catch (Exception e) {
      System.err.println("Caught Exception for \"" + radarSource + "\": " +
                           e);
      System.exit(1);
    }

    FlatField radar = ra.getData();
    FunctionType radar_image = (FunctionType) radar.getType();
    RealTupleType radaz = radar_image.getDomain();
    RealType reflection = (RealType) radar_image.getRange();
    RealType azimuth = (RealType) radaz.getComponent(1); // bug jk: was (0)
    RealType range = (RealType) radaz.getComponent(0);

    DisplayImplJ3D display = new DisplayImplJ3D("radar");
    ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    lonmap.setRange(157.0, 163.0);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-33.0, -27.0);
    // ScalarMap reflectionmap = new ScalarMap(reflection, Display.ZAxis);
    // display.addMap(reflectionmap);
    // reflectionmap.setRange(0, 6);
    // display.addMap(new ScalarMap(reflection, Display.RGB));
    ScalarMap rgbMap = new ScalarMap(reflection, Display.RGB);
	  // rgbMap.setRange(0.,6.);
	display.addMap(rgbMap);
				

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
    mode.setTextureEnable(false);

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

