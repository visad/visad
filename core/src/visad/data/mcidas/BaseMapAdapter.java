//
// BaseMapAdapter.java
//

/*
The software in this file is Copyright(C) 2011 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.mcidas;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import visad.CoordinateSystem;
import visad.Gridded2DSet;
import visad.Linear2DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.UnionSet;
import visad.VisADException;
import java.awt.geom.Rectangle2D;

/** this is an adapter for McIDAS Base Map files */

public class BaseMapAdapter {
  private boolean isCoordinateSystem = false;
  private int latMax=900000, latMin=-900000;
  private int lonMax=1800000, lonMin=-1800000;
  private int segmentPointer = 0;  // index into array of segments
  private int numEles=0;
  private CoordinateSystem cs=null;
  private DataInputStream din;
  private MathType coordMathType;
  private int position;    // position (bytes) where we are reading in the file
  private int numSegments = 0;
  private int[][] segList;
  private boolean isEastPositive = true;
  private int xfirst = 0;
  private int xlast = 0;
  private int yfirst = 0;
  private int ylast = 0;
  private int MAX_SEGMENTS = 100000;  // docs say 1000, but they lie!

  /**
   * Create a VisAD UnionSet from a local McIDAS Base Map file
   *
   * @param filename name of local file.
   * @exception IOException if there was a problem reading the file.
   * @exception VisADException if an unexpected problem occurs.
   */
  public BaseMapAdapter(String filename) throws IOException, VisADException {
    this(new FileInputStream(filename), null);
  }


  /**
   * Create a VisAD UnionSet from a McIDAS Base Map file on the Web
   *
   * @param filename name of local file.
   * @param bbox  lat/lon bounding box of map lines to include
   *
   * @exception IOException if there was a problem reading the file.
   * @exception VisADException if an unexpected problem occurs.
   */
  public BaseMapAdapter(String filename, Rectangle2D bbox)
    throws IOException, VisADException {
      this(new FileInputStream(filename), null);
  }

  /**
   * Create a VisAD UnionSet from a McIDAS Base Map file on the Web
   *
   * @param url URL & filename name of remote file
   *
   * @exception IOException if there was a problem reading the URL.
   * @exception VisADException if an unexpected problem occurs.
   */
  public BaseMapAdapter(URL url) throws IOException, VisADException {
    this (url.openStream(), null);
  }

  /**
   * Create a VisAD UnionSet from a McIDAS Base Map file on the Web
   *
   * @param url  URL of remote file
   * @param bbox  lat/lon bounding box of map lines to include
   *
   * @exception IOException if there was a problem reading the URL.
   * @exception VisADException if an unexpected problem occurs.
   */
  public BaseMapAdapter(URL url, Rectangle2D bbox)
    throws IOException, VisADException {
      this (url.openStream(), bbox);
  }

  /**
   * Create a VisAD UnionSet from a McIDAS Base Map file inputstream
   *
   * @param is input stream of mapfile
   *
   * @exception IOException if there was a problem reading the inputstream
   * @exception VisADException if an unexpected problem occurs.
   */
  public BaseMapAdapter(InputStream is)
    throws IOException, VisADException {
    this(is, null);
  }

  /**
   * Create a VisAD UnionSet from a McIDAS Base Map file inputstream
   *
   * @param is input stream of mapfile
   * @param bbox  lat/lon bounding box
   *
   * @exception IOException if there was a problem reading the inputstream
   * @exception VisADException if an unexpected problem occurs.
   */
  public BaseMapAdapter(InputStream is, Rectangle2D bbox)
    throws IOException, VisADException {

    din = new DataInputStream (new BufferedInputStream(is));
    InitFile();
    if (bbox != null)
        setLatLonLimits((float) bbox.getMinY(), (float) bbox.getMaxY(),
                        (float) bbox.getMinX(), (float) bbox.getMaxX());
  }

  /** set the limits of Lats and Lons; without this, the getData()
   * will return ALL the points in the file.  When this method is
   * used, the feature of the McIDAS map files that has the
   * lat/lon extremes for each line segment will be used to
   * coarsely cull points out of the returned VisAD UnionSet.<P>
   *
   * This may be used along with any other domain-setting routine,
   * but should be invoked last.  Alternatively, pass in the
   * bounding box in the constructor.
   *
   * @param bbox Rectangle2D representing the bounding box
   */
  public void setLatLonLimits(Rectangle2D bbox)
  {
      setLatLonLimits((float) bbox.getMinY(), (float) bbox.getMaxY(),
                      (float) bbox.getMinX(), (float) bbox.getMaxX());
  }

  /** set the limits of Lats and Lons; without this, the getData()
   * will return ALL the points in the file.  When this method is
   * used, the feature of the McIDAS map files that has the
   * lat/lon extremes for each line segment will be used to
   * coarsely cull points out of the returned VisAD UnionSet.<P>
   *
   * This may be used along with any other domain-setting routine,
   * but should be invoked last.
   *
   * @param latmin the minimum Latitude value
   * @param latmax the maximum Latitude value
   * @param lonmin the minimum Longitude value (-180 -- 180)
   * @param lonmax the maximum Longitude value
   *
   */
  public void setLatLonLimits(float latmin,
                              float latmax,
                              float lonmin,
                              float lonmax) {
    latMin = (latmin == Float.NaN)
                ? -900000
                : (int) (latmin * 10000.f);
    latMax = (latmax == Float.NaN)
                ? 900000
                : (int) (latmax * 10000.f);
    lonMin = (lonmin == Float.NaN)
                ? -1800000
                : (int) (lonmin * 10000.f);
    lonMax = (lonmax == Float.NaN)
                ? 1800000
                : (int) (lonmax * 10000.f);
    //System.out.println("Lat min/max = "+latMin+" "+latMax);
    //System.out.println("Lon min/max = "+lonMin+" "+lonMax);
    return;
  }

  /**
   * Using the domain_set of the FlatField of an image (when
   * one is available), extract the elements required.  This
   * implies that a CoordinateSystem is available with a
   * reference coordinate of Latitude,Longitude.
   *
   * @param domainSet The VisAD Linear2DSet domain_set used when the
   *                  associated image FlatField was created
   *
   * @throws  VisADException  necessary VisAD object cannot be created
   */

  public void setDomainSet(Linear2DSet domainSet)
     throws VisADException
  {
      coordMathType = domainSet.getType();
      cs = domainSet.getCoordinateSystem();
      numEles = ((Linear2DSet) domainSet).getX().getLength();

      xfirst = (int) ((Linear2DSet) domainSet).getX().getFirst();
      xlast = (int) ((Linear2DSet) domainSet).getX().getLast();
      yfirst = (int) ((Linear2DSet) domainSet).getY().getFirst();
      ylast = (int) ((Linear2DSet) domainSet).getY().getLast();

      /*
      System.out.println("coordMathType="+coordMathType);
      System.out.println("cs="+cs);
      System.out.println("numEles="+numEles);
      System.out.println("numLines="+numLines);
      System.out.println("xfirst="+xfirst);
      System.out.println("xlast="+xlast);
      System.out.println("yfirst="+yfirst);
      System.out.println("ylast="+ylast);
      */

      computeLimits();
  }

  /**
   * Define a CoordinateSystem whose fromReference() will
   * be used to transform points from latitude/longitude
   * into element,line.
   *
   * @param cs is that
   * @param numEles is number of elements (x)
   * @param numLines is number of lines (y)
   * @param domain is the desired domain (ordered element, line)
   *
   * @exception  VisADException  a necessary VisAD object could not be created
   */
  public void setCoordinateSystem(CoordinateSystem cs, int numEles,
                          int numLines, RealTupleType domain)
                          throws VisADException {

    this.numEles = numEles;
    this.cs = cs;
    coordMathType = domain;
    xlast = numEles - 1;
    ylast = numLines - 1;

    computeLimits();
  }

  /**
   * Set the MathType of the UnionSet to be lat/lon.
   */
  public void doByLatLon() {
    isCoordinateSystem = false;
    try {
      coordMathType = new RealTupleType(RealType.Latitude, RealType.Longitude);
    } catch (Exception ert) {;}
  }

  // compute the lat/long limits for fetching data; invert the Y axis, too.
  private void computeLimits() {

    // Now set lat/lon limits...

    float[][] linele =
        { {(float) xfirst, (float) xlast, (float)xlast, (float)xfirst} ,
        {(float) yfirst, (float) yfirst, (float)ylast, (float)ylast} };

    float[][] latlon;

    try {

      latlon = cs.toReference(linele);

      if (Float.isNaN(latlon[0][0])) latlon[0][0] = 90.f;
      if (Float.isNaN(latlon[1][0])) latlon[1][0] = 180.f;

      if (Float.isNaN(latlon[0][1])) latlon[0][1] = 90.f;
      if (Float.isNaN(latlon[1][1])) latlon[1][1] = -180.f;

      if (Float.isNaN(latlon[0][2])) latlon[0][2] = -90.f;
      if (Float.isNaN(latlon[1][2])) latlon[1][2] = 180.f;

      if (Float.isNaN(latlon[0][3])) latlon[0][3] = -90.f;
      if (Float.isNaN(latlon[1][3])) latlon[1][3] = -180.f;


      /*
      for (int i=0; i<4; i++) {
        System.out.println("Point "+i+"  Line/Ele="+linele[0][i]+" "+
          linele[1][i]+" Lat/long="+ latlon[0][i]+" "+latlon[1][i]);
      }
      */

      setLatLonLimits(

        Math.min(latlon[0][0],Math.min(latlon[0][1],
                         Math.min(latlon[0][2],latlon[0][3]))),
        Math.max(latlon[0][0],Math.max(latlon[0][1],
                         Math.max(latlon[0][2],latlon[0][3]))),
        Math.min(latlon[1][0],Math.min(latlon[1][1],
                         Math.min(latlon[1][2],latlon[1][3]))),
        Math.max(latlon[1][0],Math.max(latlon[1][1],
                       Math.max(latlon[1][2],latlon[1][3])))
      );
    } catch (Exception ell) {System.out.println(ell);}

    isCoordinateSystem = true;

  }

  // InitFile will initialize the file reading
  private void InitFile() throws VisADException {
    coordMathType = RealTupleType.LatitudeLongitudeTuple;

    try {
      numSegments = din.readInt();
    } catch (IOException e) {
      throw new VisADException("Error reading map file " + e);
    }

    // Begin - DRM 04-20-2000
    if (numSegments <= 0 || numSegments > MAX_SEGMENTS)
    {
      throw new VisADException(
          "McIDAS map file format error: number of segments = " + 
          numSegments);
    }
    // End - DRM 04-20-2000

    // already read first word, so move pointer to start of second word (4 bytes)
    position = 4;
    segList = new int[numSegments][6];

    // read in the segment info
    for (int i=0; i<numSegments; i++) {
      try {
        /* Each segement directory has 6 words:
             0 - min lat
             1 - max lat
             2 - min lon
             3 - max lon
             4 - pointer (words) to start of data for segment
             5 - number of words to read for the segment 
                 (should be even lat/lon)
        */
        for (int j=0; j<6; j++) {
          segList[i][j] = din.readInt();
          // Begin - DRM 04-20-2000
          if (j == 4 && segList[i][4] < 0)  // bad pointer to data
          {
              throw new VisADException(
                  "McIDAS map file format error: Negative pointer (" +
                  segList[i][4] + ") to start of data for segment " + i);
          }
          //bad number of words
          if (j == 5 && (segList[i][5] < 0 || segList[i][5]%2 != 0)) 
          {
              throw new VisADException(
                  "McIDAS map file format error: Wrong number of words (" + 
                  segList[i][5] + ") to read for segment " + i);
          }
          // End - DRM 04-20-2000
          position = position + 4;   // move the pointer to next word
        }
      } catch (IOException e) {
        throw new VisADException("Base Map: Error reading map file: "+e);
      }
    }
    segmentPointer = -1;   // HACK!  set to -1 so first segment is not skipped
                           // in findNextSegment()
    return;
  }

  /* 
   * locate the next valid segment (based on lat/lon extremes)
   * the return is 0 if we've read all the segments otherwise it is
   * the number of pairs of lat/lon points
   */
  private int findNextSegment() throws VisADException {
    while (true) {
      segmentPointer++;   
      // exit if we've read all the points
      if (segmentPointer >= numSegments) {
        return 0;
      }

      // check for lat/lon bounds...
      if (segList[segmentPointer][0] > latMax ||
            segList[segmentPointer][1] < latMin) {continue;}

      if (isEastPositive) {
        int mx = -segList[segmentPointer][2];
        int mn = -segList[segmentPointer][3];

        if (lonMax > 1800000 ) {
          if ( mx < 0 && mx < lonMin) mx = mx + 3600000;
          if ( mn < 0 && mn < lonMin) mn = mn + 3600000;
        }

        if ( mx > lonMax ) {continue;}
        if ( mn < lonMin ) {continue;}

      } else {
        if (segList[segmentPointer][2] > lonMax ||
            segList[segmentPointer][3] < lonMin) {continue; }
      }

      return segList[segmentPointer][5] / 2;

    } // end while...
  }

  /* 
   * Get the lat/lons for the current segment
   */
  private float[][] getLatLons() throws VisADException {

    int numPairs = segList[segmentPointer][5] / 2;

    // DRM 4/20/2000
    if (numPairs < 0)
        throw new VisADException(
            "Error in map file: Negative number of lat/lon pairs");
            
    int lat;
    int lon;
    int skipByte;
    long rc;
    float[][] lalo;

    float dLonMin = (float)lonMin/10000.0f;

    try {
      skipByte = segList[segmentPointer][4] * 4 - position;
      try {
        din.skipBytes(skipByte);
      } catch (Exception e) {
        throw new VisADException("Base Map: IOException in skip" + e);
      }

      lalo = new float[2][numPairs];
      for (int i=0; i<numPairs; i++) {
        lat = din.readInt();
        lon = din.readInt();
        lalo[0][i] = (float) lat/10000.f;
        lalo[1][i] = (float) lon/10000.f;
        if (isEastPositive) {
          lalo[1][i] = -lalo[1][i];
          if (lalo[1][i] < 0.0 && lalo[1][i] < dLonMin && lonMax > 1800000)
              lalo[1][i] = 360.f+lalo[1][i];
        }
      }
    } catch (IOException e) {
          throw new VisADException("Base Map: read past EOF");
    }
    position = position + skipByte + (8 * numPairs);
    return lalo;
  }

  /**
    * getData creates a VisAD UnionSet type with the MathType
    * specified thru one of the other methods.  By default,
    * the MathType is a RealTupleType of Latitude,Longitude,
    * so the UnionSet (a union of Gridded2DSets) will have
    * lat/lon values.  Each Gridded2DSet is a line segment that
    * is supposed to be drawn as a continuous line.  This should
    * only be called once after construction.
    *
    * @return  UnionSet of maplines or null if there are no maplines
    *          in the domain of the display.
    */
  public UnionSet getData() {

    UnionSet maplines=null;
    Gridded2DSet gs;

    int st=1;
    float[][] lalo, linele, llout;
    int ll;

    Vector sets = new Vector();
    float maxEle = (float)numEles/2.0f;

    try {
      int inum = 0;
      while (true) {
        st = findNextSegment();
        if (st == 0) break;   // we've read all the segments
        lalo = getLatLons();
        ll = lalo[0].length;
        int lbeg = 0;
        int lnum = 0;

        if (isCoordinateSystem) {
          linele = cs.fromReference(lalo);
          boolean missing = false;

          for (int i=0; i<ll; i++) {
            if (Float.isNaN(linele[0][i])) {
              missing=true;
              break;
            }

            if (i > 0 && Math.abs(linele[0][i] - linele[0][i-1]) > maxEle) {

              if (lnum > 1) {

               llout = new float[2][lnum];
               System.arraycopy(linele[0],lbeg,llout[0],0,lnum);
               System.arraycopy(linele[1],lbeg,llout[1],0,lnum);
               gs = new Gridded2DSet(coordMathType,llout,lnum);
               sets.addElement(gs);
              }

              lnum = 0;
              lbeg = i;
            }

            lnum ++;
          }

          if (missing) continue;

          if (lnum == ll) {
            gs = new Gridded2DSet(coordMathType,linele,ll);
            sets.addElement(gs);
          } else if (lnum > 1) {
             llout = new float[2][lnum];
             System.arraycopy(linele[0],lbeg,llout[0],0,lnum);
             System.arraycopy(linele[1],lbeg,llout[1],0,lnum);

             gs = new Gridded2DSet(coordMathType,llout,lnum);
             sets.addElement(gs);
          }


        } else {

          gs = new Gridded2DSet(coordMathType,lalo,ll);
          sets.addElement(gs);
        }
        inum += ll;

      }
      //System.out.println("number of points = " + inum);
      //System.out.println("number of sets = " + sets.size());

      /* DRM - 03-Jan-2000
      if (sets.size() > 1)  // need at least two for a UnionSet
      */
      if (!sets.isEmpty())  // need at least one for a UnionSet
      {
        Gridded2DSet[] basemaplines = new Gridded2DSet[sets.size()];
        sets.copyInto(basemaplines);

        maplines = new UnionSet(coordMathType,basemaplines, 
                                null, null, null, false);
      }

    } catch (Exception em) {em.printStackTrace(); return null;}

    return maplines;

  }

  /**
   * set the sign of longitude convention.  By default, the
   * longitudes are positive eastward
   *
   * @param value set to true for positive eastward, set to
   *              false for positive westward.
   */
  public void setEastPositive(boolean value) {
    isEastPositive = value;
  }

  /**
   * determine what sign convention for longitude is currently
   * being used.
   *
   * @return true if the convention is positive eastward; false if
   *         positive westward.
   */
  public boolean isEastPositive() {
    return (isEastPositive);
  }
}
