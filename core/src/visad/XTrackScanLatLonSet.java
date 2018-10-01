/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visad;

/**
 *
 * @author rink
 */
public class XTrackScanLatLonSet extends GriddedLatLonSet {
   
   GriddedLatLonSet[] scanSets;
   int numOfScans;
   int linesPerScan;
   
   int lastSetIdx;
   
   public XTrackScanLatLonSet(MathType type, float[][] samples, int lengthX, int lengthY, int linesPerScan) throws VisADException {
      super(type, samples, lengthX, lengthY, null, null, null, false, false);
      
      this.linesPerScan = linesPerScan;
      int numOfScans = lengthY/linesPerScan;
      scanSets = new GriddedLatLonSet[numOfScans];
      int scanLen = linesPerScan*lengthX;
      
      float[] scanLonArray = new float[linesPerScan*lengthX];
      float[] scanLatArray = new float[linesPerScan*lengthX];
      
      for (int k=0; k<numOfScans; k++) {
         System.arraycopy(lons[k*scanLen], 0, scanLonArray, 0, scanLen);
         System.arraycopy(lats[k*scanLen], 0, scanLatArray, 0, scanLen);
         
         scanSets[k] = new GriddedLatLonSet(RealTupleType.SpatialEarth2DTuple, new float[][] {scanLonArray, scanLatArray}, lengthX, linesPerScan);
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
    else {
//       int[] gg = findValid(gx, gy);
//       if (gg != null) {
//          gx = gg[0];
//          gy = gg[1];
//       }
    }
    
    lastSetIdx = numOfScans/2;
    
    int idx = gy*LengthX+gx;
    if (Float.isNaN(lons[idx]) || Float.isNaN(lats[idx]) || (Math.abs(lats[idx]) > 90)) {
       throw new VisADException("initial grid box guess cannot be invalid or missing");
    }
    
    float[][] lonlat = new float[2][1];
    
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
      
//      int setIdx = gy/linesPerScan;
//      int gy_set = gy % linesPerScan;
      
      
      
      
      // if the loop doesn't find the answer, the result should be NaN
      grid[0][i] = grid[1][i] = Float.NaN;
      
      float targetLon = value[lonI][i];
      float targetLat = value[latI][i];
      lonlat[lonI][0] = targetLon;
      lonlat[latI][0] = targetLat;
      float[][] gxgy;
      
      while (lastSetIdx >= 0 && lastSetIdx < numOfScans) {
      
        gxgy = scanSets[lastSetIdx].valueToGrid(lonlat, guess);
      
        if (gxgy[0][0] == Float.NaN || gxgy[1][0] == Float.NaN) {
           int gy_set = guess[1];
           int gx_set = guess[0];
           if (gy_set == 0) {
              lastSetIdx -= 1;
           }
           else if (gy_set == linesPerScan-1) {
              lastSetIdx += 1;
           }
           else if (gx_set == 0 || gx_set == LengthX-1) {
              break;
           }
        }
        else {
           grid[0][i] = gxgy[0][0];
           grid[1][i] = gxgy[1][0];
           break;
        }
      }
      
    }
    
    return grid;
  }
   
}
