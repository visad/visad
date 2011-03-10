//
// UsgsDemAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.gis;

import java.io.*;
import java.awt.geom.Rectangle2D;
import java.util.zip.GZIPInputStream;
import visad.*;
import visad.georef.*;
import visad.data.units.Parser;
import visad.util.ReflectedUniverse;
import java.util.Arrays;
import java.rmi.RemoteException;

/**
 * UsgsDemAdapter converts the data in a USGS DEM file to a VisAD object.
 * The specification for * the format can be found at 
 * <a href="http://rockyweb.cr.usgs.gov/nmpstds/acrodocs/dem/2DEM0198.PDF">
 * http://rockyweb.cr.usgs.gov/nmpstds/acrodocs/dem/2DEM0198.PDF</a>. Refer
 * to this document for further explanation of the data elements of a DEM.
 * <p>
 * To simplify things, class UsgsDem changes the format of a USGS 7.5 minute
 * DEM to a simpler "row by column" raster form rather than the 
 * "row by column by subcolumn" format in the specification.
 * </p>
 * @author Don Murray
 * @author Jeffrey Albert Bergamini 
 *    (http://srproj.lib.calpoly.edu/projects/csc/jbergam/reports/final.html)
 * @version $Revision: 1.10 $ $Date: 2009-03-02 23:35:48 $
 */
public class UsgsDemAdapter {

  /**************************************************************
   * Fields related to java implementation
   **************************************************************/

  /** Southwest corner index constant */
  private static final int SW = 0;
  /** Northwest corner index constant */
  private static final int NW = 1;
  /** Northeast corner index constant */
  private static final int NE = 2;
  /** Southeast corner index constant */
  private static final int SE = 3;
   
  /** Geographic type reference system */
  private static final int GEOGRAPHIC = 0;
  /** UTM type reference system */
  private static final int UTM = 1;
  /** State type reference system */
  private static final int STATE = 2;

  /** Temporary storage string */
  private String tempString;
   
  /** Total number of points (vertices) in the DEM */
  private int numPoints = 0;

  /** A BufferedReader used to read from DEM files */
  private BufferedReader in;

  /** Marker to keep track of position within a record */
  private int pos;

  /** Record block size */
  private static final int BLOCK_SIZE = 1024;

  /** Temporary storage array */
  private char[] array = new char[BLOCK_SIZE];

  /**************************************************************
   * Variables related to USGS DEM format
   **************************************************************/
  
  /** Measurement definition constant */
  private static final int
     RADIANS = 0,
     FEET = 1,
     METERS = 2,
     ARCSECONDS = 3;
     
  private static RealType EASTING = UTMCoordinate.EASTING;
  private static RealType NORTHING = UTMCoordinate.NORTHING;

  private static Unit[] measureUnits = new Unit[4];
  static {
      measureUnits[RADIANS] = CommonUnit.radian;
      measureUnits[METERS] = CommonUnit.meter;
      try {
          measureUnits[FEET] = Parser.parse("foot");
          measureUnits[ARCSECONDS] = Parser.parse("arcseconds");
      } catch (Exception e) {}
  }

  private static int MISSING_VALUE = -32767;

  /**
   * File Name -- USGS DEM Logical Record Type A, Data Element 1
   * <br>
   * The authorized digital cell name followed by a
   * comma, space, and the two-character State
   * designator(s) separated by hyphens. Abbreviations
   * for other countries, such as Canada and Mexico,
   * shall not be represented in the DEM header.
   */
  private String fileName;
  
  /**
   * Free Format Text -- USGS DEM Logical Record Type A, Data Element 1
   * <br>
   * Free format descriptor field, contains useful information
   * related to digital process such as digitizing instrument,
   * photo codes, slot widths, etc.
   */
  private String freeFormatText;
  
  /**
   * Origin Code -- USGS DEM Logical Record Type A, Data Element 2
   * <br><pre>
   * 1=Autocorrelation RESAMPLE Simple bilinear
   * 2=Manual profile GRIDEM Simple bilinear
   * 3=DLG/hypsography CTOG 8-direction linear
   * 4=Interpolation from photogrammetric system contours DCASS 4-direction linear
   * 5=DLG/hypsography LINETRACE, LT4X Complex linear
   * 6=DLG/hypsography CPS-3, ANUDEM, GRASS Complex polynomial
   * 7=Electronic imaging (non-photogrametric), active or passive, sensor systems.
   * </pre>
   */
  private String originCode;
   
  /**
   * DEM Level Code -- USGS DEM Logical Record Type A, Data Element 3
   * <br><pre>
   * Code 1=DEM-1
   *      2=DEM-2
   *      3=DEM-3
   *      4=DEM-4
   * </pre>
   */
  private int level;
   
  /**
   * Code defining ground planimetric reference system -- USGS DEM Logical Record Type A, Data Element 5
   * <br><pre>
   * Code 0=Geographic
   *      1=UTM
   *      2=State plane
   * For codes 3-20, see Appendix 2-G.
   * Code 0 represents the geographic
   * (latitude/longitude) system for 30-minute,
   * 1-degree and Alaska DEM's. Code 1 represents
   * the current use of the UTM coordinate system for
   * 7.5-minute DEM's
   * </pre>
   */
  private int referenceSystem;
  
  /** 
   * Code defining zone in ground planimetric reference system.
   * USGS DEM Logical Record Type A, Data Element 6 
   */
  private int zone;
  
  /**
   * Code defining unit of measure for ground planimetric coordinates.
   * USGS DEM Logical Record Type A, Data Element 8
   * <pre>
   * 0 = radians
   * 1 = feet
   * 2 = meters
   * 3 = arc-seconds
   */
  private int groundMeasure;
  
  /**
   * Code defining unit of measure for elevation coordinates.
   * USGS DEM Logical Record Type A, Data Element 9
   */
  private int elevationMeasure;
  
  /**
   * A 4-element array containing the ground coordinates of the quadrangle
   * boundary for the DEM.
   *
   * USGS DEM Logical Record Type A, Data Element 11
   */
  private RealTuple[] corner = new RealTuple[4];
   
  /** Minimum elevation */
  private float minElevation;
  /** Maximum elevation */
  private float maxElevation;

  /** Rotation angle elevation */
  private float rotationAngle = 0;

  /** Minimum easting */
  private float minX = Float.POSITIVE_INFINITY;
  /** Maximum easting */
  private float maxX = Float.NEGATIVE_INFINITY;

  /** Minimum northing */
  private float minY = Float.POSITIVE_INFINITY;
  /** Maximum northing */
  private float maxY = Float.NEGATIVE_INFINITY;

  /** Range of elevation */
  private float elevationRange;
  /** Range of easting */
  private float xRange;
  /** Range of northing */
  private float yRange;
  
  /** 
   * Elevation accuracy code
   * USGS DEM Logical Record Type A, Data Element 14
   */
  private int elevationAccuracy;
  
  /** 
   * X (easting) spatial resolution
   * USGS DEM Logical Record Type A, Data Element 15
   */
  private float xResolution;
  /** 
   * Y (northing) spatial resolution
   * USGS DEM Logical Record Type A, Data Element 15
   */
  private float yResolution;
  /** 
   * Z (elevation) spatial resolution
   * USGS DEM Logical Record Type A, Data Element 15
   */
  private float zResolution;
  
  /** Number of rows of profiles in the DEM */
  // this is always 1 and superfluous
  private int numRows;
  
  /** 
   * Number of columns (in the northing dimension)
   * of profiles in the DEM 
   */
  private int numColumns;
  
  /**
   * Maximum number of data points per column (easting dimension)
   */
  private int maxRows;

  /** 
   * float array of points in the form [xyz][numpoints]
   */
  private float[][] rawCoords;

  /**
   * Vertical Datum code
   */
  private int verticalDatum;

  /**
   * Horizontal Datum code
   */
  private int horizontalDatum;

  private Linear2DSet domainSet;
  private FlatField data;
  private boolean canDoGeotransform = true;
  private ReflectedUniverse reflectUni;

  /**
   * Default constructor, everything remains null. Call load() to load a file.
   */
  public UsgsDemAdapter () {
    try {
      reflectUni = new ReflectedUniverse();
      reflectUni.exec("import ucar.visad.UTMCoordinateSystem");
      reflectUni.exec("import geotransform.ellipsoids.Ellipsoid");
    } catch (VisADException exc) { canDoGeotransform = false; }
  }

  /**
   * Constructs a new UsgsDemAdapter object with data read from the given
   * (native format, non-SDTS) USGS DEM file
   *
   * @param filename the name of the DEM file
   */
  public UsgsDemAdapter (String filename) throws IOException, VisADException {
    try {
      reflectUni = new ReflectedUniverse();
      reflectUni.exec("import ucar.visad.UTMCoordinateSystem");
      reflectUni.exec("import geotransform.ellipsoids.Ellipsoid");
    } catch (VisADException exc) { canDoGeotransform = false; }
    if (filename != null) load(filename);
  }

  /**
   * Reinitializes this UsgsDemAdapter object with data read from the given
   * (non-SDTS) USGS DEM file.
   *
   * @param filename the name of the DEM file
   * @throws IOException - If an I/O error occurs (invalid file)
   */
  public void load(String filename) throws IOException, VisADException {
      
    numPoints = 0;
    data = null;
    domainSet = null;
    
    try {
      //ungzip
      if (filename.endsWith(".gz"))
        in = new BufferedReader(
          new InputStreamReader(
            new GZIPInputStream(new FileInputStream(filename))));
      else
        in = new BufferedReader(new FileReader(filename));
    } catch (Exception e) {
      throw new IOException("Couldn't open file: " + filename);
    }
     
    processRecordTypeA();
     
    for (int j=0; j<numColumns; j++)  {
      // System.out.println("Processing column " + j);
      processRecordTypeB();
    }
     
    elevationRange = maxElevation - minElevation;
    xRange = maxX - minX;
    yRange = maxY - minY;
    // makeFlatField();
     
  }
  
  /**
   * Reads in the data from a USGS DEM Logical Record Type A and
   * sets the appropriate fields for this DEM object
   */   
  private void processRecordTypeA() throws IOException, VisADException {
     
    pos = 1;
                                     // Elements:
    fileName = getString(40);        // 1 get file name
    freeFormatText = getString(40);  // 1 get format text
    skip(29);                        // 1 skip filler
    skip(26);                        // 1 skip SE geo corner
    skip(1);                         // 1 skip process code
    skip(1);                         // 1 skip filler
    skip(3);                         // 1 skip sectional indicator
    originCode = getString(4);       // 2 get origin code
    level = parseInt(6);             // 3 get level code
    skip(6);                         // 4 skip code defining elevation pattern (always 1)
    referenceSystem = parseInt(6);   // 5 get code defining ground planimetric reference system
    if (!(referenceSystem == GEOGRAPHIC ||
          referenceSystem == UTM)) {
      throw new VisADException(
        "Unimplemented reference system " + referenceSystem);
    }
    zone = parseInt(6);              // 6 get code defining zone in ground planimetric ref. system
    skip(360);                       // 7 skip map projection parameters (all zero)
    groundMeasure = parseInt(6);     // 8 get code defining unit of measure for ground planimetric coords
    elevationMeasure = parseInt(6);  // 9 get code defining unit of measure for elevation coords
    skip(6);                         // 10 skip number of sides of polygon defining DEM coverage (always 4)
    corner[SW] = parseCoordinate();  // 11 get SW corner of this quadrangle
    corner[NW] = parseCoordinate();  // 11 get NW corner of this quadrangle
    corner[NE] = parseCoordinate();  // 11 get NE corner of this quadrangle
    corner[SE] = parseCoordinate();  // 11 get SE corner of this quadrangle
    minElevation = parseFloat(24);   // 12 get minimum elevation
    maxElevation = parseFloat(24);   // 12 get maximum elevation
    rotationAngle = parseFloat(24);  // 13 skip angle thing which isn't used
    elevationAccuracy = parseInt(6); // 14 get accuracy code for elevations
    xResolution = parseFloat(12);    // 15 get x resolution (spacing)
    yResolution = parseFloat(12);    // 15 get y resolution (spacing)
    zResolution = parseFloat(12);    // 15 get z resolution (spacing)
    numRows = parseInt(6);           // 16 get number of rows (always 1 -- that's kind of dumb)
    if (numRows != 1) throw new VisADException("Can't handle " + numRows + " rows");
    numColumns = parseInt(6);        // 16 get number of columns
    skip(24);
    try {
      verticalDatum = parseInt(2);     // 17 get vertical datum
      horizontalDatum = parseInt(2);   // 18 get horizontal datum
    } catch (NumberFormatException nfe) { // if old format
      verticalDatum = 2;
      horizontalDatum = (referenceSystem == UTM)?1:2;
    }
    skip(BLOCK_SIZE-pos+1);          // skip to end of block
    
    // since numRows is always 1,
    // coordinate array should be size (numColumns)x(numRows)
    rawCoords = 
      (referenceSystem == GEOGRAPHIC)
         ? new float[3][numColumns*1201]
         : new float[3][numColumns*2000];
           
    //System.out.println("DONE WITH A"); 
  }
   
  /**
   * Processes a Type B USGS DEM record
   * (see specification
   */  
  private void processRecordTypeB() throws IOException, VisADException {
     
    pos = 1;
     
    float Xp, Yp, Xgp, Ygp, elevation;
     
    int row = parseInt(6);                 // 1 row number
    int column = parseInt(6)-1;            // 1 column number (adjusted >= 0)
    int m = parseInt(6);                   // 2 num elevations
    int n = parseInt(6);                   // 2 n is always 1 -- hmm, useful
    float Xgo = parseFloat(24);            // x coord of starting point
    float Ygo = parseFloat(24);            // y coord of starting point
    float localElevation = parseFloat(24); // elevation of local datum
    float min = parseFloat(24);            // min elevation for the profile
    float max = parseFloat(24);            // max elevation for the profile

    if (m > maxRows) maxRows = m;

    float costheta = (float) Math.cos(rotationAngle);
    float sintheta = (float) Math.sin(rotationAngle);

    /*
     * Calculate profile coordinates
     */
    int block = 0;
    for (int j=0; j<m; j++) {
      for (int i = 0; i<n; i++) {

        // see Figure 5 in docs for equation explanation
        Xp = i*xResolution;
        Yp = j*yResolution;
        Xgp = Xgo + Xp*costheta + Yp*sintheta;
        Ygp = Ygo + Xp*sintheta + Yp*costheta;

        int rawElev = parseInt(6);
        if (rawElev == MISSING_VALUE) {
            elevation =  Float.NaN;
        } else {
            elevation = rawElev*zResolution+localElevation;
        }
        //update min and max x,y
        minX = Math.min(Xgp, minX);
        maxX = Math.max(Xgp, maxX);
        minY = Math.min(Ygp, minY);
        maxY = Math.max(Ygp, maxY);
        if (!Float.isNaN(elevation)) {
           minElevation = Math.min(minElevation, elevation);
           maxElevation = Math.max(maxElevation, elevation);
        }
        /* 
        if( j == m-1)  {
          System.out.println(
              "Start for column " + column + " = " + Xgo + "," + Ygo);
          System.out.print("number of rows = " + m);
          System.out.print(" number of columns = " + n);
          System.out.print(" theta = " + rotationAngle);
          System.out.print(" costheta = " + costheta);
          System.out.print(" sintheta = " + sintheta);
          System.out.print(" Xgp = " + Xgp);
          System.out.println(" Ygp = " + Ygp);
          System.out.print("minX = " + minX);
          System.out.print(" maxX = " + maxX);
          System.out.print(" minY = " + minY);
          System.out.println(" maxY = " + maxY);
        }
        */
        rawCoords[0][numPoints] = Xgp;
        rawCoords[1][numPoints] = Ygp;
        rawCoords[2][numPoints] = elevation;
        numPoints++;
        // make new size if we need to.
        if (numPoints == rawCoords[0].length) {
           int oldSize = rawCoords[0].length;
           int newSize = oldSize + oldSize/2;
           // System.out.println("old size = " + oldSize + " new = " + newSize);
           float[][] tempCoords = rawCoords;
           rawCoords = new float[3][newSize];
           for (int l = 0; l < rawCoords.length; l++) {
               System.arraycopy(tempCoords[l], 0, rawCoords[l], 0, oldSize);
           }
           tempCoords = null;
        }
        int numLeft = BLOCK_SIZE-(pos%BLOCK_SIZE)+1;
        // System.out.println("numleft = " + numLeft);
        if (numLeft < 6) skip(numLeft);
      }
    }
    // System.out.println("done with loop, skipping " + (BLOCK_SIZE-(pos%BLOCK_SIZE)+1) + " chars");
    int numToNextRecord = BLOCK_SIZE-(pos%BLOCK_SIZE)+1;
    if (numToNextRecord > 0 && numToNextRecord < BLOCK_SIZE) {
        skip(numToNextRecord);
    }

    // System.out.println("Size was " + pos);
  
    // System.out.println("DONE WITH B"); 
  }

  /**
   * Multi-line string representation
   * @return An info string suitable for printing to the console.
   */
  public String toString() {

    StringBuffer s = new StringBuffer();
    s.append(printElement("File name", fileName));
    s.append(printElement("Free Format Text", freeFormatText));
    s.append(printElement("Origin code", originCode));
    s.append(printElement("Level code", level));
    s.append(printElement("Reference system code", referenceSystem));
    s.append(printElement("Reference system zone code", zone));
    s.append(printElement("Ground measurement units", measureUnits[groundMeasure]));
    s.append(printElement("Elev. measurement units", measureUnits[elevationMeasure]));
    s.append(printElement("SW corner", corner[SW]));
    s.append(printElement("NW corner", corner[NW]));
    s.append(printElement("NE corner", corner[NE]));
    s.append(printElement("SE corner", corner[SE]));
    s.append(printElement("Min x", minX));
    s.append(printElement("Max x", maxX));
    s.append(printElement("Min y", minY));
    s.append(printElement("Max y", maxY));
    s.append(printElement("Min elevation", minElevation));
    s.append(printElement("Max elevation", maxElevation));
    s.append(printElement("Elevation accuracy code", elevationAccuracy));
    s.append(printElement("X resolution", xResolution));
    s.append(printElement("Y resolution", yResolution));
    s.append(printElement("Z resolution", zResolution));
    s.append(printElement("Columns of profiles", numColumns));
    s.append(printElement("horizontal units", horizontalDatum));
    s.append(printElement("vertical units", verticalDatum));
    s.append(printElement("Max m in columns", maxRows));
    s.append(printElement("Total data", numPoints));
    return s.toString();
  }

  /**************************************************************
   * Methods for gathering data elements from the file
   **************************************************************/

  /**
   * Skips the specified number of bytes in the current file
   */
  private void skip (int length) throws IOException {
     
    try {
      in.skip(length);
    } catch (IOException e) {
      throw new IOException("Could not skip " + length + " characters");
    }
     
    pos += length;
  }

  /**
   * Reads in and returns a string of the specified length from the current
   * position
   */
  private String getString (int length) throws IOException {
     
    if (length > 1024)
      throw new IOException("Attempt to read more than 1024 bytes from file");
     
    try {
      in.read (array, 0, length);
    } catch (IOException e) {
      throw new IOException("Couldn't read string from file");
    }
     
    pos += length;
     
    return (new String (array, 0 , length)).trim();
  }

  /**
   * Reads in and returns a DEM-style integer from the current file position
   * (taking up the specified number of bytes) as an int
   */
  private int parseInt (int digits) throws IOException {
           
    if (digits > 1024)
       throw new IOException("Attempt to read more than 1024 bytes from file");

    try {
      in.read(array, 0, digits);
    } catch (IOException e) {
      throw new IOException("Couldn't read integer from file");
    }
     
    pos += digits;

    // System.out.println("\"" + new String(array, 0, digits) + "\"");
     
    return Integer.parseInt((new String(array, 0 , digits)).trim());
  }

  /**
   * Reads in and returns a DEM-style integer of unknown length from the
   * current position in the file as an int (necessary for non-standard DEMs)
   */
  private int parseInt () throws IOException {
     
    tempString = "";
    char c;
     
    try {
      do {
        c = (char) in.read();
        pos++;
      } while(Character.isWhitespace(c));

      tempString += c;
        
      do {
        c = (char) in.read();
        pos++;
        tempString += c;
      } while(!(Character.isWhitespace(c)));
        
    } catch (IOException e) {
      throw new IOException("Couldn't read integer from file");
    }
     
    tempString = tempString.substring(0,tempString.length()-1);
    // System.out.println("tempString = " + tempString);
    return Integer.parseInt(tempString);
  }

  /**
   * Reads in and returns a DEM-style float from the current position in the
   * file.
   * @throws IOException If unable to read from file
   */   
  private float parseFloat (int digits) throws IOException {

    //System.out.println("POS = " + pos);
           
    if (digits > 1024)
      throw new IOException("Attempt to read more than 1024 bytes from file");

    try {
      in.read(array, 0, digits);
    } catch (IOException e) {
      throw new IOException("Couldn't read integer from file");
    }
     
    pos += digits;

    // System.out.println("\"" + new String(array, 0, digits) + "\"");
     
    return translateReal((new String(array, 0 , digits)).trim());
  }
  
  /**
   * Translates the String representation of a DEM-style real number
   * into a float
   */
  private float translateReal (String real) {
     
    // System.out.println("\"" + real + "\"");
     
    int plus, exp;
     
    plus = real.indexOf('+');
     
    if (plus > 0)
      exp = Integer.parseInt(real.substring(plus+1,plus+3));
    else
      return Float.parseFloat(real);
     
    if (real.charAt(0) != '-') {
      tempString = 
        real.charAt(0) + real.substring(2,exp+2) + "." + 
          real.substring(exp+2,plus-1);
    } else {
      tempString = real.substring(0,2) + real.substring(3,exp+3) + 
          "." + real.substring(exp+2,plus-1);
    }

    return Float.parseFloat(tempString);
  }
  
  /**
   * Loads a UTMCoordinate from the current position in the file
   */
  private RealTuple parseCoordinate() throws IOException, VisADException {
           
    if  (referenceSystem == GEOGRAPHIC) {
       // LatLonTuple is lat,lon order
       float lon = parseFloat(24);
       return (RealTuple) new LatLonTuple (parseFloat(24), lon);
    } else if (referenceSystem == UTM) {
       return (RealTuple) 
         new UTMCoordinate (parseFloat(24), parseFloat(24), zone);
    } else {
       throw new VisADException(
         "Can't handle referenceSystem " + referenceSystem);
    }
  }
  
  /** formatting stuff */
  private String printElement(String title, Object value) {
    return padRight(title,30) + ": " + value + "\n";
  }

  /** formatting stuff */
  private String printElement(String title, int value) {
    return printElement(title, new Integer(value));
  }

  /** formatting stuff */
  private String printElement(String title, float value) {
    return printElement(title, new Float(value));
  }

  /**
   * This method takes any Object and using its String representation
   * provided by its toString() method, pads it with blank characters
   * on the right, to a specified length.
   * @param obj Object to be padded
   * @param i padding length
   */
  public static String padRight(Object obj, int i) {
    char filler = ' ';
    String s = new String(obj.toString());
    int j = s.length();
    StringBuffer buf = new StringBuffer(s);
    for(int k = 0; k < i - j; k++) buf.append(filler);
    return buf.toString();
  }

  private Linear2DSet makeDomainSet() throws VisADException {
    RealTupleType rtt = null;
    Unit[] units = null;
    switch (groundMeasure) {
      case RADIANS:
        units = new Unit[] {measureUnits[RADIANS], measureUnits[RADIANS]};
        break;
      case FEET:
        units = new Unit[] {measureUnits[FEET], measureUnits[FEET]};
        break;
      case METERS:
        units = new Unit[] {measureUnits[METERS], measureUnits[METERS]};
        break;
      case ARCSECONDS:
        units = new Unit[] {measureUnits[ARCSECONDS], measureUnits[ARCSECONDS]};
        break;
    }
    if (referenceSystem == UTM) {
      // check to see if we can do uncompression
      CoordinateSystem cs = null;
      if (canDoGeotransform) {
        try {
          // the true says NH.  Can we figure this out somehow?
          switch (horizontalDatum) {
            case 1:
              reflectUni.setVar(
                "ellipsoid", reflectUni.getVar("UTMCoordinateSystem.CC"));
              break;
            case 2:
              reflectUni.setVar(
                "ellipsoid", reflectUni.getVar("UTMCoordinateSystem.WD"));
              break;
            case 3:
              reflectUni.setVar(
                "ellipsoid", reflectUni.getVar("UTMCoordinateSystem.WE"));
              break;
            case 4:
              reflectUni.setVar(
                "ellipsoid", reflectUni.getVar("UTMCoordinateSystem.RF"));
              break;
            default:
              reflectUni.setVar(
                "ellipsoid", reflectUni.getVar("UTMCoordinateSystem.RF"));
          }
          reflectUni.setVar(
            "bounds", new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY));
          reflectUni.setVar("zone", zone);
          reflectUni.setVar("north", true);
          cs = (CoordinateSystem)
            reflectUni.exec(
              "cs = new UTMCoordinateSystem(ellipsoid, zone, north, bounds)");
           cs = new CachingCoordinateSystem(cs);
         } catch (VisADException ve) {
           System.err.println(
           "ucar.visad.UTMCoordinateSystem not found for UTM->lat/lon conversion" + ve);
           cs = null;
         }
         rtt = new RealTupleType(EASTING, NORTHING, cs, null);
       }
    } else {
      rtt = RealTupleType.SpatialEarth2DTuple;
    }
    domainSet =
      (referenceSystem == UTM)
         ?  new Linear2DSet(rtt, 
                            //minX, maxX, ((int)(xRange/xResolution))+1,
                            minX, minX+(xResolution*(numColumns-1)), numColumns,
                            minY, maxY, ((int)(yRange/yResolution))+1,
                            (CoordinateSystem) null,  /* in rtt if used */
                            units, (ErrorEstimate[]) null, 
                            true /*cache*/)
         /* GEOGRAPHIC */
         : new LinearLatLonSet(rtt, 
                               minX, minX+(xResolution*(numColumns-1)), numColumns,
                               minY, minY+(yResolution*(maxRows-1)), maxRows,
                               (CoordinateSystem) null,  /* in rtt if used */
                               units, (ErrorEstimate[]) null, 
                               true /*cache*/);
    // System.out.println("domainSet = " + domainSet);
    return domainSet;
  }

  private FlatField makeFlatField() throws VisADException {
    makeDomainSet();
    float[][] altitudes = new float[1][domainSet.getLength()];
    Arrays.fill(altitudes[0], Float.NaN);
    FunctionType ft = 
      new FunctionType(
        ((SetType)domainSet.getType()).getDomain(), RealType.Altitude);
    FlatField ff = new FlatField(ft, domainSet, 
                                 (CoordinateSystem) null, 
                                 (Set[]) null,
                                 new Unit[] {measureUnits[elevationMeasure]});
    RealTuple coord = null;
    int index = 0;
    int[] indices = 
      domainSet.valueToIndex(new float[][] {rawCoords[0], rawCoords[1]});
    float alt;
    int numMissing = 0;
    for (int i = 0; i < numPoints; i++) {
        alt = rawCoords[2][i];
        if (indices[i] < 0) {
           numMissing++;
           altitudes[0][indices[i]] = Float.NaN;
        } else {
           altitudes[0][indices[i]] = rawCoords[2][i];
        }
    }
    // System.out.println("Out of " +numPoints+ " points, " + numMissing + " were missing");
    try {
      ff.setSamples(altitudes, false);
    } catch (RemoteException re) {} // can't happen
    data = ff;
    return data;
  }

  /**
   * Get the DEM as a VisAD data object
   * @return a FlatField of type ((x,y) -> altitude) where x,y is either
   *         (Longitude, Latitude) or (EASTING, NORTHING)
   */
  public FieldImpl getData() throws VisADException {
    return (data == null) ? makeFlatField(): data;
  }
  
  /**
   * Get the domain set for this DEM
   * @return Gridded2DSet for domain
   */
  public Gridded2DSet getDomain() throws VisADException {
    return (domainSet == null) ? makeDomainSet() : domainSet;
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Need to supply a filename");
      System.exit(1);
    }
    UsgsDemAdapter uda = new UsgsDemAdapter(args[0]);
    System.out.println(uda);
    System.out.println(uda.getData().getDomainSet());
  }
}
