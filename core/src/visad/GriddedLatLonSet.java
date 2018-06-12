//
// GriddedLatLonSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.io.*;

/**
   GriddedLatLonSet represents a finite set of samples of R^2.<P>
*/
public class GriddedLatLonSet extends Gridded2DSet {

  int LengthX, LengthY, TrackLen, latI, lonI;
  float LowX, HiX, LowY, HiY;
  float[] lons, lats;
  float[][] mySamples;
  
  GriddedLatLonSet[] granules;
  int[] yStart;
  int[] lgxy;

  /** a 2-D set whose topology is a lengthX x lengthY grid, with
      null errors, CoordinateSystem and Units are defaults from type */
  public GriddedLatLonSet(MathType type, float[][] samples, int lengthX, int lengthY)
         throws VisADException {
    this(type, samples, lengthX, lengthY, null, null, null, false, false);
  }

  /** a 2-D set whose topology is a lengthX x lengthY grid;
      samples array is organized float[2][number_of_samples] where
      lengthX * lengthY = number_of_samples; samples must form a
      non-degenerate 2-D grid (no bow-tie-shaped grid boxes); the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public GriddedLatLonSet(MathType type, float[][] samples, int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units, errors,
         true, true);
  }

  public GriddedLatLonSet(MathType type, float[][] samples, int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units, errors,
         copy, true);
  }

  public GriddedLatLonSet(MathType type, float[][] samples, int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy, boolean test)
               throws VisADException {
    super(type, samples, lengthX, lengthY, coord_sys, units, errors, copy, test);
    
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];
    TrackLen = LengthY;
    lgxy = new int[] {-1, -1};
    
    if (!(type.equals(RealTupleType.LatitudeLongitudeTuple) || type.equals(RealTupleType.SpatialEarth2DTuple))) {
       throw new VisADException("type must 2D with Latitude and Longitude");
    }
    
    MathType  type0 = ((SetType)getType()).getDomain().getComponent(0);

    latI = RealType.Latitude.equals(type0) ? 0 : 1;
    lonI = (latI == 0) ? 1 : 0;


    mySamples = getMySamples();
    lons = mySamples[lonI];
    lats = mySamples[latI];
    
    // Check for wrap around the globe in the fastest varying dimension, other dim is TODO
    double accum = 0;
    for (int k=0; k<TrackLen-1; k++) {
       int idx0 = k*LengthX;
       int idx1 = (k+1)*LengthX;
       float lonA = lons[idx1];
       float latA = lats[idx1];
       float lonB = lons[idx0];
       float latB = lats[idx0];
       if ((!(Float.isNaN(latA) || Float.isNaN(latB))) && Math.abs(latA) <= 90 && Math.abs(latB) <= 90) {
           double angle = greatCircleAngle(lons[idx1], lats[idx1], lons[idx0], lats[idx0]);
           accum += angle;
       }
    }
    
    // if accum > 270 make 2 separate GriddedLatLonSets for valueToGrid
    if (accum > 270*Data.DEGREES_TO_RADIANS) {
    
       granules = new GriddedLatLonSet[2];
       yStart = new int[2];

       yStart[0] = 0;
       yStart[1] = TrackLen/2;

       int TrackLen0 = TrackLen/2;
       int TrackLen1 = TrackLen - TrackLen0;

       float[] lons0 = new float[LengthX*TrackLen0];
       float[] lats0 = new float[LengthX*TrackLen0];
       System.arraycopy(lons, 0, lons0, 0, lons0.length);
       System.arraycopy(lats, 0, lats0, 0, lats0.length);
       float[] lons1 = new float[LengthX*TrackLen1];
       float[] lats1 = new float[LengthX*TrackLen1];
       System.arraycopy(lons, lons0.length, lons1, 0, lons1.length);
       System.arraycopy(lats, lats0.length, lats1, 0, lats1.length);

       granules[0] = new GriddedLatLonSet(type, new float[][] {lons0, lats0}, LengthX, TrackLen0, coord_sys, units, errors, copy, test);
       granules[1] = new GriddedLatLonSet(type, new float[][] {lons1, lats1}, LengthX, TrackLen1, coord_sys, units, errors, copy, test);
    }

  }


  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Gridded2DSet.gridToValue: grid dimension " +
                             grid.length +
                             " not equal to Manifold dimension " +
                             ManifoldDimension);
    }
    if (ManifoldDimension < 2) {
      throw new SetException("Gridded2DSet.gridToValue: Manifold dimension " +
                             "must be 2, not " + ManifoldDimension);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded2DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    float[][] value = new float[2][length];
    for (int i=0; i<length; i++) {
      // let gx and gy by the current grid values
      float gx = grid[0][i];
      float gy = grid[1][i];
      if ( (gx < -0.5)        || (gy < -0.5) ||
           (gx > LengthX-0.5) || (gy > TrackLen-0.5) ) {
        value[0][i] = value[1][i] = Float.NaN;
      } else if (Length == 1) {
        value[0][i] = mySamples[0][0];
        value[1][i] = mySamples[1][0];
      } else {
        // calculate closest integer variables
        int igx = (int) gx;
        int igy = (int) gy;
        if (igx < 0) igx = 0;
        if (igx > LengthX-2) igx = LengthX-2;
        if (igy < 0) igy = 0;
        if (igy > TrackLen-2) igy = TrackLen-2;
        
        int idx = igy*LengthX + igx;

        float min =  Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        // look at grid cell corners
        float lon = mySamples[lonI][idx];
        if (lon < min) min = lon;
        if (lon > max) max = lon;

        if ((gx+1) < LengthX) {
           lon = mySamples[lonI][idx+1];
           if (lon < min) min = lon;
           if (lon > max) max = lon;
        }

        if ((gy+1) < TrackLen) {
           lon = mySamples[lonI][idx + LengthX];
           if (lon < min) min = lon;
           if (lon > max) max = lon;
        }

        if (((gx+1) < LengthX) && ((gy+1) < TrackLen)) {
           lon = mySamples[lonI][idx + LengthX + 1];
           if (lon < min) min = lon;
           if (lon > max) max = lon;
        }
        
        if ((max - min) > 300) { // grid cell probably stradles the dateline so force nearest neighbor
           gx = (float) Math.floor(gx + 0.5);
           gy = (float) Math.floor(gy + 0.5);
        }        
        
        
        // set up conversion to 1D Samples array
        int[][] s = { {LengthX*igy+igx,           // (0, 0)
                       LengthX*(igy+1)+igx},      // (0, 1)
                      {LengthX*igy+igx+1,         // (1, 0)
                       LengthX*(igy+1)+igx+1} };  // (1, 1)
        if (gx+gy-igx-igy-1 <= 0) {
          // point is in LOWER triangle
          for (int j=0; j<2; j++) {
            value[j][i] = mySamples[j][s[0][0]]
              + (gx-igx)*(mySamples[j][s[1][0]]-mySamples[j][s[0][0]])
              + (gy-igy)*(mySamples[j][s[0][1]]-mySamples[j][s[0][0]]);
          }
        }
        else {
          // point is in UPPER triangle
          for (int j=0; j<2; j++) {
            value[j][i] = mySamples[j][s[1][1]]
              + (1+igx-gx)*(mySamples[j][s[0][1]]-mySamples[j][s[1][1]])
              + (1+igy-gy)*(mySamples[j][s[1][0]]-mySamples[j][s[1][1]]);
          }
        }
      }
    }
    return value;
  }

  
  public static int[] UL = new int[] {-1,1};
  public static int[] UR = new int[] {1,1};
  public static int[] DL = new int[] {-1,-1};
  public static int[] DR = new int[] {1,-1};
  public static int[] UU = new int[] {0,1};
  public static int[] DD = new int[] {0,-1};
  public static int[] LL = new int[] {-1,0};
  public static int[] RR = new int[] {1,0};
  public static int[] CC = new int[] {0,0};
  
  @Override
  public float[][] valueToGrid(float[][] value) throws VisADException {
    return valueToGrid(value, null);
  }
  
  @Override
  public synchronized float[][] valueToGrid(float[][] value, int[] guess) throws VisADException {
     
    // Check the individual segments comprising the whole
    if (granules != null) {
       float[][] grid = granules[0].valueToGrid(value, guess);
       if (Float.isNaN(grid[0][0])) {
          grid = granules[1].valueToGrid(value, guess);
          grid[1][0] += yStart[1];
       }
       
       return grid;
    }
     
    if (value.length < DomainDimension) {
      throw new SetException("Gridded2DSet.valueToGrid: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (ManifoldDimension < 2) {
      throw new SetException("Gridded2DSet.valueToGrid: Manifold dimension " +
                             "must be 2, not " + ManifoldDimension);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded2DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    if (guess != null && guess.length != 2) {
      throw new SetException("Gridded2DSet.valueToGrid: guess length " 
          + guess.length + " must equal 2");
    }
    
    
    int length = Math.min(value[0].length, value[1].length);
    float[][] grid = new float[ManifoldDimension][length];

    // (gx, gy) is the current grid box guess
    int gx = (LengthX-1)/2;
    int gy = (TrackLen-1)/2;
    
    if (guess != null && guess[0] >= 0 && guess[1] >= 0) {
      gx = guess[0];
      gy = guess[1];
    }
    else if (lgxy[0] != -1 && lgxy[1] != -1) {
       gx = lgxy[0];
       gy = lgxy[1];
    }
    else {
       int[] gg = findValid(gx, gy);
       if (gg != null) {
          gx = gg[0];
          gy = gg[1];
       }
    }
    
    int idx = gy*LengthX+gx;
    if (Float.isNaN(lons[idx]) || Float.isNaN(lats[idx]) || (Math.abs(lats[idx]) > 90)) {
       throw new VisADException("initial grid box guess cannot be invalid or missing");
    }

    for (int i=0; i<length; i++) {

      if (Length == 1) {
        if (Float.isNaN(value[0][i]) || Float.isNaN(value[1][i])) {
           grid[0][i] = grid[1][i] = Float.NaN;
        } else {
           grid[0][i] = 0;
           grid[1][i] = 0;
        }
        continue;
      }

      // test for missing
      if ( (i != 0) && grid[0][i-1] != grid[0][i-1] ) {
        // use last valid
        if (lgxy[0] != -1 && lgxy[1] != -1) {
          gx = lgxy[0];
          gy = lgxy[1];
        }
      }
      
      // if the loop doesn't find the answer, the result should be NaN
      grid[0][i] = grid[1][i] = Float.NaN;
      
      float angleToTarget;
      int igx=0;
      int igy=0;
      int last_igx=0;
      int last_igy=0;
      
      for (int itnum=0; itnum<2*(LengthX+TrackLen); itnum++) {
         
        float targetLon = value[lonI][i];
        float targetLat = value[latI][i];
         
        float glon = lons[gy*LengthX+gx];
        float glat = lats[gy*LengthX+gx];
        
        angleToTarget = (float) greatCircleAngle(targetLon, targetLat, glon, glat);
        

          float angle;
          igx = 0;
          igy = 0;
          
          idx = (gy+1)*LengthX+gx;
          if ((gy+1) < TrackLen) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = 0;
                igy = 1;
             }
          }
          
          idx = (gy-1)*LengthX+gx;
          if ((gy-1) >= 0) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = 0;
                igy = -1;
             }
          }
          
          idx = gy*LengthX+gx+1;
          if ((gx+1) < LengthX) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = 1;
                igy = 0;
             }
          }
          
          idx = gy*LengthX+gx-1;
          if ((gx-1) >= 0) {   
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = -1;
                igy = 0;
             }
          }
          
          idx = (gy+1)*LengthX+gx-1;
          if ((gy+1) < TrackLen && (gx-1) >= 0) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = -1;
                igy = 1;
             }
          } 
          
          idx = (gy+1)*LengthX+gx+1;
          if ((gx+1) < LengthX && (gy+1) < TrackLen) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
              
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = 1;
                igy = 1;
             }
          }
          
          idx = (gy-1)*LengthX+gx-1;
          if ((gy-1) >= 0 && (gx-1) >= 0) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = -1;
                igy = -1;
             }
          }
           
          idx = (gy-1)*LengthX+gx+1;
          if ((gy-1) >= 0 && (gx+1) < LengthX) {
             float lat = lats[idx];
             float lon = lons[idx];
             if (Math.abs(lat) > 90) { // Use last step to try and walk over invalid geo info
                gx += last_igx;
                gy += last_igy;
                continue;
             }             
             
             angle = (float) greatCircleAngle(lon, lat, targetLon, targetLat);
             if (angle < angleToTarget) {
                angleToTarget = angle;
                igx = +1;
                igy = -1;
             }
          }
          
          gx += igx;
          gy += igy;
          
          last_igx = igx;
          last_igy = igy;
          
          if (igx == 0 && igy == 0) {
             boolean offGrid = false;
             
             int[] gg = new int[2];
             gg[0] = gx;
             gg[1] = gy;
             
             float[] tt = toXYZ(new float[] {targetLat, targetLon, 1f});
             
             
             if (gx == 0 && gy == 0) {
                  offGrid = !insideTriangle(gg, CC, UU, RR, tt);
             }
             else if (gx == 0 && gy == TrackLen-1) {
                  offGrid = !insideTriangle(gg, CC, DD, RR, tt);
             }
             else if (gx == LengthX-1 && gy == 0) {
                  offGrid = !insideTriangle(gg, CC, UU, LL, tt);
             }
             else if (gx == LengthX-1 && gy == TrackLen-1) {
                  offGrid = !insideTriangle(gg, CC, LL, DD, tt);
             }
             else if (gy == 0) {
                  offGrid = !insideTriangle(gg, LL, UU, RR, tt);
             }
             else if (gy == TrackLen-1) {
                  offGrid = !insideTriangle(gg, LL, DD, RR, tt);
             }
             else if (gx == 0) {
                  offGrid = !insideTriangle(gg, UU, RR, DD, tt);
             }
             else if (gx == LengthX-1) {
                  offGrid = !insideTriangle(gg, UU, LL, DD, tt);               
             }
             
             if (!offGrid) {
                Tri tri = whichTriangle(gg, tt);
                if (tri != null) { // should not happen?
                   float[] gxy = tri.reverseInterpolate(new float[] {value[lonI][i], value[latI][i]});
                   grid[0][i] = gxy[0];
                   grid[1][i] = gxy[1];
                   lgxy[0] = gx;
                   lgxy[1] = gy;
                }
             }
             break;
          }
      }
    }
    //TDR: use last found as guess for next locate request
    if (guess != null) {
      guess[0] = gx;
      guess[1] = gy;
    }
    
    
    return grid;
  }
  
  private int[] findValid(int gx, int gy) {
     int cnt = 0;
      while (cnt < (Math.min(LengthX,TrackLen)/2-2)) {
         int idx = (gy+cnt)*LengthX + gx;
         if (Math.abs(lats[idx]) <= 90) {
           return new int[] {gx, gy+cnt};
         }
         idx = (gy-cnt)*LengthX + gx;
         if (Math.abs(lats[idx]) <= 90) {
           return new int[] {gx, gy-cnt};
         }     
         idx = gy*LengthX + (gx+cnt);
         if (Math.abs(lats[idx]) <= 90) {
           return new int[] {gx+cnt, gy};
         }   
         idx = gy*LengthX + (gx-cnt);
         if (Math.abs(lats[idx]) <= 90) {
           return new int[] {gx-cnt, gy};
         }
         cnt++;
     }
     return null;
  }
  
  public static float[] toXYZ(float[] latlonrad) {
    float[] value = new float[3];

    float coslat = (float) Math.cos(Data.DEGREES_TO_RADIANS * latlonrad[0]);
    float sinlat = (float) Math.sin(Data.DEGREES_TO_RADIANS * latlonrad[0]);
    float coslon = (float) Math.cos(Data.DEGREES_TO_RADIANS * latlonrad[1]);
    float sinlon = (float) Math.sin(Data.DEGREES_TO_RADIANS * latlonrad[1]);
    value[0] = latlonrad[2] * coslon * coslat;
    value[1] = latlonrad[2] * sinlon * coslat;
    value[2] = latlonrad[2] * sinlat;
    
    return value;
  }
  
  public static double greatCircleAngle(double lonA, double latA, double lonB, double latB) {
       double degToRad = Math.PI/180;
       lonA *= degToRad;
       latA *= degToRad;
       lonB *= degToRad;
       latB *= degToRad;
       
       double S = Math.sin((latA-latB)/2);
       double Spow2 = S*S;
       
       double T = Math.sin((lonA-lonB)/2);
       double Tpow2 = T*T;
       
       double arc = 2*Math.asin(Math.sqrt(Spow2 + Math.cos(latA)*Math.cos(latB)*Tpow2));
       
       arc = Math.abs(arc);
       
       return arc;
  }
  
   
   public static boolean insideTriangle(float[] v0, float[] v1, float[] v2, float[] pt) {
      float[] triNorm = TrajectoryManager.AxB(new float[] {v1[0]-v0[0], v1[1]-v0[1], v1[2]-v0[2]}, new float[] {v2[0]-v0[0], v2[1]-v0[1], v2[2]-v0[2]}, true);
      
      double[] coeffs = TrajectoryManager.getPlaneCoeffsFromNormalAndPoint(new double[] {(float)triNorm[0], (float)triNorm[1], (float)triNorm[2]}, new double[]{(float)v0[0], (float)v0[1], (float)v0[2]});
      
      float mag = TrajectoryManager.vecMag(pt);
      double[] uvec = new double[] {pt[0]/mag, pt[1]/mag, pt[2]/mag};
      
      double[] p = TrajectoryManager.getLinePlaneIntersect(coeffs, uvec, new double[] {0,0,0});
      
      float[] ab = new float[] {v1[0]-v0[0], v1[1]-v0[1], v1[2]-v0[2]};
      float[] bc = new float[] {v2[0]-v1[0], v2[1]-v1[1], v2[2]-v1[2]};
      float[] ca = new float[] {v0[0]-v2[0], v0[1]-v2[1], v0[2]-v2[2]};
      
      float[] pa = new float[] {pt[0]-v0[0], pt[1]-v0[1], pt[2]-v0[2]};
      float[] pb = new float[] {pt[0]-v1[0], pt[1]-v1[1], pt[2]-v1[2]};
      float[] pc = new float[] {pt[0]-v2[0], pt[1]-v2[1], pt[2]-v2[2]};
      
      boolean test0 = (TrajectoryManager.AdotB(TrajectoryManager.AxB(pa, ab), triNorm) > 0);
      boolean test1 = (TrajectoryManager.AdotB(TrajectoryManager.AxB(pb, bc), triNorm) > 0);
      boolean test2 = (TrajectoryManager.AdotB(TrajectoryManager.AxB(pc, ca), triNorm) > 0);
      
      if ((test0 && test1 && test2) || (!test0 && !test1 && !test2)) {
         return true;
      }
      else {
         return false;
      }
      
   }
   
   public boolean insideTriangle(int[] gg, int[] v0, int[] v1, int[] v2, float[] pt) {
     int gx = gg[0] + v0[0];
     int gy = gg[1] + v0[1];
     if ((gx < 0 || gx > LengthX-1) || (gy < 0 || gy > TrackLen-1)) {
        return false;
     }
     int idx0 = gy*LengthX+gx;
     
     gx = gg[0] + v1[0];
     gy = gg[1] + v1[1];
     if ((gx < 0 || gx > LengthX-1) || (gy < 0 || gy > TrackLen-1)) {
        return false;
     }     
     int idx1 = gy*LengthX+gx;
     
     gx = gg[0] + v2[0];
     gy = gg[1] + v2[1];
     if ((gx < 0 || gx > LengthX-1) || (gy < 0 || gy > TrackLen-1)) {
        return false;
     }
     int idx2 = gy*LengthX+gx;
     
     float[] aa = toXYZ(new float[] {lats[idx0], lons[idx0], 1f});
     float[] bb = toXYZ(new float[] {lats[idx1], lons[idx1], 1f});
     float[] cc = toXYZ(new float[] {lats[idx2], lons[idx2], 1f});
     
     return insideTriangle(aa, bb, cc, pt);
   }
   
   public Tri whichTriangle(int[] gg, float[] pt) {
      Tri tri = null;
      float[] v0 = new float[2];
      float[] v1 = new float[2];
      float[] v2 = new float[2];
      float[] v3 = new float[2];
      int[] ngg = new int[2];
      
      
      if (insideTriangle(gg, CC, UU, RR, pt)) {
         int idx = gg[1]*LengthX+gg[0];
         v0[0] = lons[idx];
         v0[1] = lats[idx];
         
         idx = (gg[1]+RR[1])*LengthX + (gg[0]+RR[0]);
         v1[0] = lons[idx];
         v1[1] = lats[idx];
         
         idx = (gg[1]+UU[1])*LengthX + (gg[0]+UU[0]);
         v2[0] = lons[idx];
         v2[1] = lats[idx];         
         
         tri = new Tri(gg, v0, v1, v2, null, true);
      }
      else if (insideTriangle(new int[] {gg[0]-1, gg[1]-1}, UU, UR, RR, pt)) {
         int gx = gg[0]-1;
         int gy = gg[1]-1;
         
         int idx = (gy+RR[1])*LengthX+(gx+RR[0]);
         v1[0] = lons[idx];
         v1[1] = lats[idx];
         
         idx = (gy+UU[1])*LengthX + (gx+UU[0]);
         v2[0] = lons[idx];
         v2[1] = lats[idx];
         
         idx = (gy+UR[1])*LengthX + (gx+UR[0]);
         v3[0] = lons[idx];
         v3[1] = lats[idx];
         
         ngg[0] = gx;
         ngg[1] = gy;
         
         tri = new Tri(ngg, null, v1, v2, v3, false);
      }
      else if (insideTriangle(new int[] {gg[0], gg[1]-1}, CC, UU, RR, pt)) {
         int gx = gg[0];
         int gy = gg[1]-1;
         
         int idx = (gy+CC[1])*LengthX+(gx+CC[0]);
         v0[0] = lons[idx];
         v0[1] = lats[idx];
         
         idx = (gy+RR[1])*LengthX + (gx+RR[0]);
         v1[0] = lons[idx];
         v1[1] = lats[idx];
         
         idx = (gy+UU[1])*LengthX + (gx+UU[0]);
         v2[0] = lons[idx];
         v2[1] = lats[idx];
         
         ngg[0] = gx;
         ngg[1] = gy;
         
         tri = new Tri(ngg, v0, v1, v2, null, true);
      }
      else if (insideTriangle(new int[] {gg[0], gg[1]-1}, UU, UR, RR, pt)) {
         int gx = gg[0];
         int gy = gg[1]-1;
         
         int idx = (gy+RR[1])*LengthX+(gx+RR[0]);
         v1[0] = lons[idx];
         v1[1] = lats[idx];
         
         idx = (gy+UU[1])*LengthX + (gx+UU[0]);
         v2[0] = lons[idx];
         v2[1] = lats[idx];
         
         idx = (gy+UR[1])*LengthX + (gx+UR[0]);
         v3[0] = lons[idx];
         v3[1] = lats[idx];
         
         ngg[0] = gx;
         ngg[1] = gy;
         
         tri = new Tri(ngg, null, v1, v2, v3, false);         
      }
      else if (insideTriangle(new int[] {gg[0]-1, gg[1]}, CC, UU, RR, pt)) {
         int gx = gg[0]-1;
         int gy = gg[1];
         
         int idx = (gy+CC[1])*LengthX+(gx+CC[0]);
         v0[0] = lons[idx];
         v0[1] = lats[idx];
         
         idx = (gy+RR[1])*LengthX + (gx+RR[0]);
         v1[0] = lons[idx];
         v1[1] = lats[idx];
         
         idx = (gy+UU[1])*LengthX + (gx+UU[0]);
         v2[0] = lons[idx];
         v2[1] = lats[idx];
         
         ngg[0] = gx;
         ngg[1] = gy;
         
         tri = new Tri(ngg, v0, v1, v2, null, true);         
      }
      else if (insideTriangle(new int[] {gg[0]-1, gg[1]}, UU, UR, RR, pt)) {
         int gx = gg[0]-1;
         int gy = gg[1];
         
         int idx = (gy+RR[1])*LengthX+(gx+RR[0]);
         v1[0] = lons[idx];
         v1[1] = lats[idx];
         
         idx = (gy+UU[1])*LengthX + (gx+UU[0]);
         v2[0] = lons[idx];
         v2[1] = lats[idx];
         
         idx = (gy+UR[1])*LengthX + (gx+UR[0]);
         v3[0] = lons[idx];
         v3[1] = lats[idx];
         
         ngg[0] = gx;
         ngg[1] = gy;
         
         tri = new Tri(ngg, null, v1, v2, v3, false);                  
      }
      
      
      return tri;
   }

}

class Tri {
   public int[] gg;
   public float[] v0;
   public float[] v1;
   public float[] v2;
   public float[] v3;
   public boolean lower;
   public boolean spansDL = false;
   
   
   Tri(int[] gg, float[] v0, float[] v1, float[] v2, float[] v3, boolean lower) {
      this.gg = gg;
      this.v0 = v0;
      this.v1 = v1;
      this.v2 = v2;
      this.v3 = v3;
      this.lower = lower;
      
      float lonMin = 360;
      float lonMax = -360;
      
      if (v0 != null) {
         float lon = v0[0];
         if (lon < lonMin) lonMin = lon;
         if (lon > lonMax) lonMax = lon;
      }
      if (v1 != null) {
         float lon = v1[0];
         if (lon < lonMin) lonMin = lon;
         if (lon > lonMax) lonMax = lon;
      }
      if (v2 != null) {
         float lon = v2[0];
         if (lon < lonMin) lonMin = lon;
         if (lon > lonMax) lonMax = lon;
      }
      if (v3 != null) {
         float lon = v3[0];
         if (lon < lonMin) lonMin = lon;
         if (lon > lonMax) lonMax = lon;
      }
      
      if ((lonMax - lonMin) > 300) { // grid cell probably stradles the dateline
         spansDL = true;
      }        
      
   }
   
   float[] reverseInterpolate(float[] value) {
      float[] grid = new float[2];
      if (spansDL) {
         grid[0] = gg[0];
         grid[1] = gg[1];
         return grid;
      }
      if (lower) {
            grid[0] = ((value[0]-v0[0])*(v2[1]-v0[1])
                        + (v0[1]-value[1])*(v2[0]-v0[0]))
                       / ((v1[0]-v0[0])*(v2[1]-v0[1])
                        + (v0[1]-v1[1])*(v2[0]-v0[0])) + gg[0];
            grid[1] = ((value[0]-v0[0])*(v1[1]-v0[1])
                        + (v0[1]-value[1])*(v1[0]-v0[0]))
                       / ((v2[0]-v0[0])*(v1[1]-v0[1])
                        + (v0[1]-v2[1])*(v1[0]-v0[0])) + gg[1];
      }
      else {
            grid[0] = ((v3[0]-value[0])*(v1[1]-v3[1])
                        + (value[1]-v3[1])*(v1[0]-v3[0]))
                       / ((v2[0]-v3[0])*(v1[1]-v3[1])
                        - (v2[1]-v3[1])*(v1[0]-v3[0])) + gg[0] + 1;
            grid[1] = ((v2[1]-v3[1])*(v3[0]-value[0])
                        + (v2[0]-v3[0])*(value[1]-v3[1]))
                       / ((v1[0]-v3[0])*(v2[1]-v3[1])
                        - (v2[0]-v3[0])*(v1[1]-v3[1])) + gg[1] + 1;         
      }
      
      return grid;
   }
}

