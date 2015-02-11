//
// LALOnav.java
//

/*
This source file is part of the edu.wisc.ssec.mcidas package and is
Copyright (C) 1998 - 2015 by Tom Whittaker, Tommy Jasmin, Tom Rink,
Don Murray, James Kelly, Bill Hibbard, Dave Glowacki, Curtis Rueden
and others.
 
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

package edu.wisc.ssec.mcidas;

import java.lang.Float;
import java.lang.Double;
import visad.Gridded2DSet;

/**
 * Navigation class for Radar (RECT) type nav. This code was modified
 * from the original FORTRAN code (nvxrect.dlm) on the McIDAS system. It
 * only supports latitude/longitude to line/element transformations (LL) 
 * and vice/versa. Transform to 'XYZ' not implemented.
 * @see <A HREF="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
 *      McIDAS Programmer's Manual</A>
 *
 * @author  Don Murray
 */

public final class LALOnav extends AREAnav 
{

	private static final long serialVersionUID = 1L;
	int rows, cols, latres, lonres, latpoint, lonpoint, numPoints;
    int ulline, ulelem, aux_size, lat_aux_offset, lon_aux_offset;
    int lrlin, lrele;
    double minlat, maxlat, minlon, maxlon;
    float[] latData, lonData;
    float LAT_MISSING = 0.f;
    float LON_MISSING = 0.f;
    boolean debug = false;
    int count = 0;
    Gridded2DSet gs = null;

    /**
     * Set up for the real math work.  Must pass in the int array
     * of the RECT nav 'codicil'.
     *
     * @param iparms  the nav block from the image file
     * @throws IllegalArgumentException
     *           if the nav block is not a RECT type.
     */
    public LALOnav (int[] iparms, int[] auxBlock) 
        throws IllegalArgumentException
    {

        if (iparms[0] != LALO ) 
            throw new IllegalArgumentException("Invalid navigation type" + 
                                                iparms[0]);
        if (debug) {
          System.out.println("Len of nav = "+iparms.length);
          for (int i=0; i< iparms.length; i++) {
            System.out.println("i="+i+"  iparm="+iparms[i]);

          }
        }

        rows = iparms[65];
        cols = iparms[66];
        /*
        minlat = McIDASUtil.integerLatLonToDouble(iparms[67]);
        minlon = McIDASUtil.integerLatLonToDouble(iparms[68]);
        maxlat = McIDASUtil.integerLatLonToDouble(iparms[69]);
        maxlon = McIDASUtil.integerLatLonToDouble(iparms[70]);
        */
        minlat = (double) Float.intBitsToFloat(iparms[67]);
        minlon = (double) Float.intBitsToFloat(iparms[68]);
        maxlat = (double) Float.intBitsToFloat(iparms[69]);
        maxlon = (double) Float.intBitsToFloat(iparms[70]);

        latres = iparms[71];
        lonres = iparms[72];
        latpoint = iparms[73];
        lonpoint = iparms[74];
        ulline = iparms[75];
        ulelem = iparms[76];
        aux_size = iparms[77];
        lat_aux_offset = iparms[78];
        lon_aux_offset = iparms[79];


        int begLat = lat_aux_offset/4;
        int begLon = lon_aux_offset/4;

        lrlin = ulline + (rows -1) * latres;
        lrele = ulelem + (cols - 1) * lonres;

        if (debug) {
          System.out.println("rows, cols="+rows+" "+cols);
          System.out.println("min/max lat, lon="+minlat+" "+maxlat+" / "+minlon+" "+maxlon);

          System.out.println("latres, lonres="+latres+" "+lonres);
          System.out.println("latpoint, lonpoint="+latpoint+" "+lonpoint);
          System.out.println("ulline, ulelem="+ulline+" "+ulelem);
          System.out.println("size_aux, lat_aux, lon_aux="+aux_size+" "+lat_aux_offset+" "+lon_aux_offset);
          System.out.println("len of auxBlock"+auxBlock.length);

          System.out.println("begLat/Lon="+begLat+" "+begLon);
          System.out.println("len of auxBlock="+auxBlock.length);
        }

        /*
        float[][] lats = new float[cols][rows];
        float[][] lons = new float[cols][rows];
        */

        numPoints = cols * rows;
        latData = new float[numPoints];
        lonData = new float[numPoints];
        float[][] lalo = new float[2][numPoints];

        for (int k=0; k<numPoints; k++) {
          latData[k] = Float.intBitsToFloat(auxBlock[k + begLat]);
          lonData[k] = Float.intBitsToFloat(auxBlock[k + begLon]);
          lalo[0][k] = lonData[k];
          lalo[1][k] = latData[k];

          if (latData[k] < -90.f || latData[k] > 90.f
                  || lonData[k] < -180.f || lonData[k] > 360.f) {

            latData[k] = LAT_MISSING;
            lonData[k] = LON_MISSING;
            lalo[0][k] = Float.NaN;
            lalo[1][k] = Float.NaN;
          }
        }

        count = 0;
        // look up VisAD stuff...if available...
  try {
    
    gs = new Gridded2DSet(visad.RealTupleType.SpatialEarth2DTuple,
       lalo, cols, rows, null, null, null, false, false);

  } catch (Exception ge) {
    System.out.println("####  The VisAD library visad.jar is needed for this operation");
    ge.printStackTrace();
  }

        if (debug) System.out.println("done coverting");

    }

    /** converts from satellite coordinates to latitude/longitude
     *
     * @param  linele	  array of line/element pairs.  Where 
     *                     linele[indexLine][] is a 'line' and 
     *                     linele[indexEle][] is an element. These are in 
     *                     'file' coordinates (not "image" coordinates.)
     *
     * @return latlon[][]  array of lat/long pairs. Output array is 
     *                     latlon[indexLat][] of latitudes and 
     *                     latlon[indexLon][] of longitudes.
     *
     * this code cobbled from McIDAS laloutil.c
     */
    public float[][] toLatLon(float[][] linele) {

      int number = linele[0].length;
      double rlin, rele;
      int  tl_entry, tr_entry, bl_entry, br_entry;
      float  tl_lats, tr_lats, bl_lats, br_lats;
      float  tl_lons, tr_lons, bl_lons, br_lons;
      float  frac_row, frac_col;
      float  ax, bx, cx;

      float[][] latlon = new float[2][number];

      // Convert array to Image coordinates for computations
      float[][] imglinele = areaCoordToImageCoord(linele);

      for (int point=0; point < number; point++) 
      {
          rlin = imglinele[indexLine][point];
          rele = imglinele[indexEle][point];

          if (debug) {
            count ++;
            //if (count < 20) {
              System.out.println(" floats...ulline, lrlin, ulelem, lrele="+ulline+" "+lrlin+" "+ulelem+" "+lrele);
              System.out.println(" rlin, rele="+" "+rlin+" "+rele);
            //}
          }

          if (rlin < ulline || rlin > lrlin ||
                         rele < ulelem || rele > lrele) {
            latlon[indexLat][point] = Float.NaN;
            latlon[indexLon][point] = Float.NaN;

          } else {


           /* offset to the top_left (tl) corner lat/lon */
           tl_entry = (((((int)rlin-ulline)/latres) * cols) +
                      ((int)rele-ulelem) / lonres);


           /* offsets for top_right, bottom_left and bottom_left lat/lon
        corners */
           tr_entry = tl_entry + 1;
           bl_entry = tl_entry + cols;
           br_entry = bl_entry + 1;

           // check to see if on last row or column...
           if ( (((int)rlin - ulline)/latres) >= rows-1) {
             bl_entry = tl_entry;
             br_entry = bl_entry + 1;
           }

           if ( (((int)rele -ulelem)/lonres) >= cols-1) {
             tr_entry = tl_entry;
             br_entry = bl_entry;
           }

           if (debug) {
             System.out.println(" tl_entry="+tl_entry+ " bl_entry="+bl_entry);
           }

           /* read the 4 corner latitudes */
           tl_lats = latData[tl_entry];
           tr_lats = latData[tr_entry];
           bl_lats = latData[bl_entry];
           br_lats = latData[br_entry];

           /* read the 4 corner longitudes */
           tl_lons = lonData[tl_entry];
           tr_lons = lonData[tr_entry];
           bl_lons = lonData[bl_entry];
           br_lons = lonData[br_entry];
         
           /* Check for missing lat/lon */

           if ( (tl_lats == LAT_MISSING && tl_lons == LON_MISSING)  ||
                (tr_lats == LAT_MISSING && tr_lons == LON_MISSING) ||
                (bl_lats == LAT_MISSING && bl_lons == LON_MISSING) ||
                (br_lats == LAT_MISSING && br_lons == LON_MISSING) ) {

                  latlon[indexLat][point] = Float.NaN;
                  latlon[indexLon][point] = Float.NaN;

           } else {

             /* compute the fractional part of the row and column */
             frac_row = (float)(((int)rlin-ulline) % latres) / (float)latres;
             frac_col = (float)(((int)rele-ulelem) % lonres) / (float)lonres;

           if (debug && count < 20) {
             if (linele[indexLine][point] < .1) {
               System.out.println(" lats: tl, tr, bl, br="+tl_lats+" "+tr_lats+" "+bl_lats+" "+br_lats);
               System.out.println(" frac_row="+frac_row+" frac_col="+frac_col);
             }
           }

             /*  Calc the real lat */
             ax = tr_lats - tl_lats;
             bx = bl_lats - tl_lats;
             cx = (tl_lats + br_lats) - (bl_lats + tr_lats);

             latlon[indexLat][point] = (ax * frac_col) + (bx * frac_row) + 
                        (cx * frac_row * frac_col) + tl_lats;

             /*  Calc the real lon */
             ax = tr_lons - tl_lons;
             bx = bl_lons - tl_lons;
             cx = (tl_lons + br_lons) - (bl_lons + tr_lons);

             latlon[indexLon][point] = (ax * frac_col) + (bx * frac_row) + 
                        (cx * frac_row * frac_col) + tl_lons;
           }
            
          }

          if (debug && count < 20) System.out.println(" line/ele = "+linele[indexLine][point]+"/"+linele[indexEle][point]+"  rlin/rele="+rlin+"/"+rele+" Lat/Lon="+latlon[indexLat][point]+"/"+latlon[indexLon][point]);

      } // end point for loop

      return latlon;

    }



    public double[][] toLatLon(double[][] linele) {

      int number = linele[0].length;
      double[][] latlon = new double[2][number];
      double rlin, rele;
      int  tl_entry, tr_entry, bl_entry, br_entry;
      float  tl_lats, tr_lats, bl_lats, br_lats;
      float  tl_lons, tr_lons, bl_lons, br_lons;
      float  frac_row, frac_col;
      float  ax, bx, cx;

      // Convert array to Image coordinates for computations
      double[][] imglinele = areaCoordToImageCoord(linele);

      for (int point=0; point < number; point++) 
      {
          rlin = imglinele[indexLine][point];
          rele = imglinele[indexEle][point];

          if (debug) {
            count ++;
            if (count < 20) {
              System.out.println(" double....ulline, lrlin, ulelem, lrele="+ulline+" "+lrlin+" "+ulelem+" "+lrele);
              System.out.println(" rlin, rele="+" "+rlin+" "+rele);
            }
          }

          if (rlin < ulline || rlin > lrlin ||
                         rele < ulelem || rele > lrele) {
            latlon[indexLat][point] = Double.NaN;
            latlon[indexLon][point] = Double.NaN;

          } else {



           /* offset to the top_left (tl) corner lat/lon */
           tl_entry = (((((int)rlin-ulline)/latres) * cols) +
                      ((int)rele-ulelem) / lonres);


           /* offsets for top_right, bottom_left and bottom_left lat/lon
        corners */
           tr_entry = tl_entry + 1;
           bl_entry = tl_entry + cols;
           br_entry = bl_entry + 1;

           // check to see if on last row or column...
           if ( (((int)rlin - ulline)/latres) >= rows-1) {
             bl_entry = tl_entry;
             br_entry = bl_entry + 1;
           }

           if ( (((int)rele -ulelem)/lonres) >= cols-1) {
             tr_entry = tl_entry;
             br_entry = bl_entry;
           }

           if (debug && count < 20) {
             System.out.println(" tl_entry="+tl_entry+ " bl_entry="+bl_entry);
           }

           /* read the 4 corner latitudes */
           tl_lats = latData[tl_entry];
           tr_lats = latData[tr_entry];
           bl_lats = latData[bl_entry];
           br_lats = latData[br_entry];

           /* read the 4 corner longitudes */
           tl_lons = lonData[tl_entry];
           tr_lons = lonData[tr_entry];
           bl_lons = lonData[bl_entry];
           br_lons = lonData[br_entry];
         
           /* Check for missing lat/lon */

           if ( (tl_lats == LAT_MISSING && tl_lons == LON_MISSING)  ||
                (tr_lats == LAT_MISSING && tr_lons == LON_MISSING) ||
                (bl_lats == LAT_MISSING && bl_lons == LON_MISSING) ||
                (br_lats == LAT_MISSING && br_lons == LON_MISSING) ) {

                  latlon[indexLat][point] = Double.NaN;
                  latlon[indexLon][point] = Double.NaN;

           } else {

             /* compute the fractional part of the row and column */
             frac_row = (float)(((int)rlin-ulline) % latres) / (float)latres;
             frac_col = (float)(((int)rele-ulelem) % lonres) / (float)lonres;

           if (debug && count < 20) {
             System.out.println(" tl_entry="+tl_entry);
             if (linele[indexLine][point] < .1) {
               System.out.println(" lats: tl, tr, bl, br="+tl_lats+" "+tr_lats+" "+bl_lats+" "+br_lats);
               System.out.println(" frac_row="+frac_row+" frac_col="+frac_col);
             }
           }

             /*  Calc the real lat */
             ax = tr_lats - tl_lats;
             bx = bl_lats - tl_lats;
             cx = (tl_lats + br_lats) - (bl_lats + tr_lats);

             latlon[indexLat][point] = (ax * frac_col) + (bx * frac_row) + 
                        (cx * frac_row * frac_col) + tl_lats;

             /*  Calc the real lon */
             ax = tr_lons - tl_lons;
             bx = bl_lons - tl_lons;
             cx = (tl_lons + br_lons) - (bl_lons + tr_lons);

             latlon[indexLon][point] = (ax * frac_col) + (bx * frac_row) + 
                        (cx * frac_row * frac_col) + tl_lons;
           }
            
          }

          if (debug && count < 20) System.out.println(" line/ele = "+linele[indexLine][point]+"/"+linele[indexEle][point]+"  rlin/rele="+rlin+"/"+rele+" Lat/Lon="+latlon[indexLat][point]+"/"+latlon[indexLon][point]);

      } // end point for loop

      return latlon;

    }

  /**
   * toLinEle converts lat/long to satellite line/element
   * transform an array of values in R^DomainDimension to an array
   * of non-integer grid coordinates
   *
   * @param  latlon array of lat/long pairs. Where latlon[indexLat][]
   *                are latitudes and latlon[indexLon][] are longitudes.
   *
   * @return linele[][] array of line/element pairs.  Where
   *                    linele[indexLine][] is a line and linele[indexEle][]
   *                    is an element.  These are in 'file' coordinates
   *                    (not "image" coordinates);
   */

  public float[][] toLinEle(float[][] latlon) {
    try {
      float[][] ll = new float[2][latlon[0].length];
      for (int k=0; k<ll[0].length; k++) {
        ll[0][k] = latlon[indexLon][k];
        ll[1][k] = latlon[indexLat][k];
      }

      float[][] linele = gs.valueToGrid(ll);

      for (int k=0; k<linele[0].length; k++) {
        linele[indexLine][k] = ulline + latres*linele[1][k];
        linele[indexEle][k] = ulelem + lonres*linele[0][k];
      }

      return imageCoordToAreaCoord(linele,linele);
    } catch (Exception e) {
      return null;
    }
  }

  public double[][] toLinEle(double[][] latlon) {
    try {
      float[][] ll = new float[2][latlon[0].length];
      for (int k=0; k<ll[0].length; k++) {
        ll[0][k] = (float)latlon[indexLon][k];
        ll[1][k] = (float)latlon[indexLat][k];
      }

      float[][] xy = gs.valueToGrid(ll);

      double[][] linele = new double[2][xy[0].length];
      for (int k=0; k<xy[0].length; k++) {
        linele[indexLine][k] = ulline + latres*xy[1][k];
        linele[indexEle][k] = ulelem + lonres*xy[0][k];
      }

      return imageCoordToAreaCoord(linele,linele);
    } catch (Exception e) {
      return null;
    }
  }

}
