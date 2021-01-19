//
// XTrackScanLatLonSet
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

/**
 *
 * @author rink
 * 
 * Specialized extension to GriddedLatLonSet for a contiguous collection of spatially overlapping sets
 * wherein it's trusted that samples of an individual set are spatially coherent (non-degenerative: no bow-ties).
 * Examples are MODIS and VIIRS whose granules are comprised of multiple scans, each with a fixed number of
 * detectors, and perpendicular to the polar orbit track, but may overlap when navigated to the Earth's surface.
 * The primary purpose of this class is to override valueToGrid.
 * 
 */
public class XTrackScanLatLonSet extends GriddedLatLonSet {
   
   GriddedLatLonSet[] scanSets;
   int numOfScans;
   int linesPerScan;
   
   int lastSetIdx;
   
   public XTrackScanLatLonSet(MathType type, float[][] samples, int XTrackLen, int TrackLen, int linesPerScan) throws VisADException {
      super(type, samples, XTrackLen, TrackLen, null, null, null, false);
      
      if ((TrackLen % linesPerScan) != 0) {
         throw new VisADException("There must be an intergral number of scans with detectorsPerScan: "+linesPerScan+" per "
                 + "TrackLen: "+TrackLen);
      }
      
      this.linesPerScan = linesPerScan;
      numOfScans = TrackLen/linesPerScan;
      scanSets = new GriddedLatLonSet[numOfScans];
      int scanLen = linesPerScan*XTrackLen;
      
      for (int k=0; k<numOfScans; k++) {
         float[] scanLonArray = new float[linesPerScan*XTrackLen];
         float[] scanLatArray = new float[linesPerScan*XTrackLen];         
         System.arraycopy(lons, k*scanLen, scanLonArray, 0, scanLen);
         System.arraycopy(lats, k*scanLen, scanLatArray, 0, scanLen);
         
         scanSets[k] = new GriddedLatLonSet(RealTupleType.SpatialEarth2DTuple, new float[][] {scanLonArray, scanLatArray}, XTrackLen, linesPerScan);
      }
   }
   
   public float[][] valueToGrid(float[][] value) throws VisADException {
      return valueToGrid(value, null);
   }
   
   public float[][] valueToGrid(float[][] value, int[] guess) throws VisADException {
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


    int setIdx = gy/linesPerScan;
    //int gy_set = gy % linesPerScan;
    lastSetIdx = setIdx;
    
    int idx = gy*LengthX+gx;
    if (Float.isNaN(lons[idx]) || Float.isNaN(lats[idx]) || (Math.abs(lats[idx]) > 90)) {
       throw new VisADException("initial grid box guess cannot be invalid or missing");
    }
    
    float[][] lonlat = new float[2][1];
    int[] guess_set = new int[2];
    guess_set[0] = gx;
    guess_set[1] = linesPerScan/2;
    
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
      
      float targetLon = value[lonI][i];
      float targetLat = value[latI][i];
      lonlat[lonI][0] = targetLon;
      lonlat[latI][0] = targetLat;
      float[][] gxgy;
      
      /* Should never exceed numOfScans */   
      int cnt = 0;
      
      /* Used to detect infinite stepping back and forth between consecutive scans.
         This may happen if the target falls in a gap between two adjacent scans,
         for example, near MODIS, VIIRS Nadir. In this situation, NaN is returned but
         may consider other options in the future.
      */
      int dir = 0;
      
      /* Beginnig with lastSetIdx start walking through the GriddedLatLonSet to find grid
         cell which contains the target. If none found, use last test position (must be
         on an edge) as guess position for the adjacent scan.
      */
      while ((lastSetIdx >= 0 && lastSetIdx < numOfScans) && cnt < numOfScans) {
         
        GriddedLatLonSet scanSet = scanSets[lastSetIdx];
        gxgy = scanSet.valueToGrid(lonlat, guess_set);
      
        if (Float.isNaN(gxgy[0][0]) || Float.isNaN(gxgy[1][0])) {
           int gx_set = guess_set[0];
           int gy_set = guess_set[1];
           if (gy_set == 0) {
              lastSetIdx -= 1;
              guess_set[0] = gx_set;
              guess_set[1] = linesPerScan-1;
              if (dir == 1) {
                 break;
              }
              dir = -1;
           }
           else if (gy_set == linesPerScan-1) {
              lastSetIdx += 1;
              guess_set[0] = gx_set;
              guess_set[1] = 0;
              if (dir == -1) {
                 break;
              }
              dir = 1;
           }
           else if (gx_set == 0 || gx_set == LengthX-1) {
              break;
           }
           cnt++;
        }
        else {
           grid[0][i] = gxgy[0][0];
           grid[1][i] = lastSetIdx*linesPerScan + gxgy[1][0];
           lgxy[0] = scanSet.lgxy[0];
           lgxy[1] = scanSet.lgxy[1] + lastSetIdx*linesPerScan;
           break;
        }
      }
      
    }
    
    return grid;
  }
   
}
