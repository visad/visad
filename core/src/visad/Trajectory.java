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
public class Trajectory {
  
  /* The manager to which this trajectory belongs */
  TrajectoryManager trajMan;
  
  
  /* Current location (spatial set) of massless tracer particle */
  float[] startPts = new float[3];

  /* grid point neighbors and interp weights for current location */
  int[] startCell;
  float[] cellWeights;
  
  int[][] indices = new int[1][];
  float[][] weights = new float[1][];

  /* unit vector from last to current location*/
  float[] uVecPath = new float[] {Float.NaN, Float.NaN, Float.NaN};

  int[] guess3D = new int[] {-1,-1,-1};
  int[] guess2D = new int[] {-1,-1};

  byte[] startColor;
  byte[] stopColor;

  float[] stopPts = new float[3];
  float[][] startPts2D = new float[2][1];
  float[][] startPts3D = new float[3][1];
  
  /* first and current time and associated set indices */
  public int initialTimeIndex = 0;
  public int currentTimeIndex = 0;

  public double initialTime = 0;
  public double currentTime = 0;

  /* Flag indicating particle has moved out of the grid, or position 
     cannot be determined - Trajectory obj will be removed from list.
  */
  boolean offGrid = false;
  
  int clrDim;
  
  GriddedSet spatial_set;
  
  int manifoldDimension;

  int npairs = 0;
  int[] indexes = new int[60];
  
  float[] lastPtD = null;
  float[] lastPtC = null;
  float[] lastPtDD = null;
  float[] lastPtCC = null;
  
  float[][] circleXYZ;
  float[][] last_circleXYZ;  
  
  static float[][] circle;
  
  public Trajectory(TrajectoryManager trajMan, float startX, float startY, float startZ, int[] startCell, float[] cellWeights, byte[] startColor, double initialTime) {
    startPts[0] = startX;
    startPts[1] = startY;
    startPts[2] = startZ;
    this.startCell = startCell;
    this.cellWeights = cellWeights;
    this.indices[0] = startCell;
    this.weights[0] = cellWeights;

    clrDim = startColor.length;
    stopColor = new byte[clrDim];

    this.startColor = startColor;   
    this.initialTime = initialTime;
    this.trajMan = trajMan;
  }
  
  public void forward(FlowInfo info, float[][] flow_values, byte[][] color_values, GriddedSet spatial_set, int direction, float timeStep)
           throws VisADException {
     
     if (offGrid) return;

     clrDim = color_values.length;
     float[] intrpClr = new float[clrDim];
     this.spatial_set = spatial_set;
     this.manifoldDimension = spatial_set.getManifoldDimension();


     float[][] flowLoc = new float[3][1];
     float[][] flowVec = new float[3][1];
     float[] intrpFlow = new float[3];

     if (indices[0] != null) {
        java.util.Arrays.fill(intrpFlow, 0f);
        java.util.Arrays.fill(intrpClr, 0);
        
        for (int j=0; j<indices[0].length; j++) {
           int idx = indices[0][j];
           flowLoc[0][0] = info.spatial_values[0][idx];
           flowLoc[1][0] = info.spatial_values[1][idx];
           flowLoc[2][0] = info.spatial_values[2][idx];

           flowVec[0][0] = flow_values[0][idx];
           flowVec[1][0] = flow_values[1][idx];
           flowVec[2][0] = flow_values[2][idx];

           float[][] del = TrajectoryManager.computeDisplacement(info, flowLoc, flowVec, timeStep);
           intrpFlow[0] += weights[0][j]*(direction)*del[0][0];
           intrpFlow[1] += weights[0][j]*(direction)*del[1][0];
           intrpFlow[2] += weights[0][j]*(direction)*del[2][0];              

           intrpClr[0] += weights[0][j]*ShadowType.byteToFloat(color_values[0][idx]);
           intrpClr[1] += weights[0][j]*ShadowType.byteToFloat(color_values[1][idx]);
           intrpClr[2] += weights[0][j]*ShadowType.byteToFloat(color_values[2][idx]);
           if (clrDim == 4) {
             intrpClr[3] += weights[0][j]*ShadowType.byteToFloat(color_values[3][idx]);
           }

           //markGrid[idx] = true;
           //markGridTime[idx] = currentTimeIndex;
        }

        stopPts[0] = startPts[0] + intrpFlow[0];
        stopPts[1] = startPts[1] + intrpFlow[1];
        stopPts[2] = startPts[2] + intrpFlow[2];

        stopColor[0] = ShadowType.floatToByte(intrpClr[0]);
        stopColor[1] = ShadowType.floatToByte(intrpClr[1]);
        stopColor[2] = ShadowType.floatToByte(intrpClr[2]);
        if (clrDim == 4) {
          stopColor[3] = ShadowType.floatToByte(intrpClr[3]);
        }

        addPair(startPts, stopPts, startColor, stopColor);

        uVecPath[0] = stopPts[0] - startPts[0];
        uVecPath[1] = stopPts[1] - startPts[1];
        uVecPath[2] = stopPts[2] - startPts[2];

        float mag = (float) Math.sqrt(uVecPath[0]*uVecPath[0] + uVecPath[1]*uVecPath[1] + uVecPath[2]*uVecPath[2]);
        uVecPath[0] /= mag;
        uVecPath[1] /= mag;
        uVecPath[2] /= mag;
        
        update();
     }

  }
  
  public void addPair(float[] startPt, float[] stopPt, byte[] startColor, byte[] stopColor) {

     indexes[npairs] = trajMan.getCoordinateCount();

     trajMan.addPair(startPt, stopPt, startColor, stopColor);    

     npairs++;

     int clrDim = startColor.length;

     // grow arrays
     if (indexes.length == npairs) {
        int[] tmp = new int[npairs+40];
        System.arraycopy(indexes, 0, tmp, 0, npairs);
        indexes = tmp;
     }     
  }
  
  private void update() throws VisADException {
     
     startPts[0] = stopPts[0];
     startPts[1] = stopPts[1];
     startPts[2] = stopPts[2];

     startColor[0] = stopColor[0];
     startColor[1] = stopColor[1];
     startColor[2] = stopColor[2];
     if (clrDim == 4) {
       startColor[3] = stopColor[3];
     }

     if (manifoldDimension == 2) {
        startPts2D[0][0] = startPts[0];
        startPts2D[1][0] = startPts[1];
        spatial_set.valueToInterp(startPts2D, indices, weights, guess2D);
     }
     else if (manifoldDimension == 3) {
        startPts3D[0][0] = startPts[0];
        startPts3D[1][0] = startPts[1];
        startPts3D[2][0] = startPts[2];
        spatial_set.valueToInterp(startPts3D, indices, weights, guess3D);
     }

     startCell = indices[0];
     cellWeights = weights[0];
     if (indices[0] == null) {
        offGrid = true;
     }     
  }
  
  public VisADGeometryArray makeCylinderStrip(float[] T, float[] S, float[] pt0, float[] pt1, byte[][] clr0, byte[][] clr1, float size,
              int npts, float[] coords, byte[] colors, float[] normls, int[] vertCnt) {
     VisADTriangleStripArray array = new VisADTriangleStripArray();

     int clrDim = clr0.length;

      if (circle == null) {
        circle = new float[2][npts];
        float intrvl = (float) (2*Math.PI)/(npts-1);
        for (int i=0; i<npts; i++) {
           circle[0][i] = (float) Math.cos(intrvl*i);  // s
           circle[1][i] = (float) Math.sin(intrvl*i);  // t
        }
     }

     int vcnt = vertCnt[0];
     int idx = 3*vcnt;
     int cidx = clrDim*vcnt;

     if (circleXYZ == null) {
        circleXYZ = new float[3][npts];
     }
     for (int k=0; k<npts; k++) {
        float s = size*circle[0][k];
        float t = size*circle[1][k];
        circleXYZ[0][k] = pt1[0] + s*S[0] + t*T[0];
        circleXYZ[1][k] = pt1[1] + s*S[1] + t*T[1];
        circleXYZ[2][k] = pt1[2] + s*S[2] + t*T[2];
     }
     if (last_circleXYZ == null) { // first time
        float[][] ptsXYZ = new float[3][npts];
        for (int k=0; k<npts; k++) {
           float s = size*circle[0][k];
           float t = size*circle[1][k];
           ptsXYZ[0][k] = pt0[0] + s*S[0] + t*T[0];
           ptsXYZ[1][k] = pt0[1] + s*S[1] + t*T[1];
           ptsXYZ[2][k] = pt0[2] + s*S[2] + t*T[2];
        }
        last_circleXYZ = new float[3][npts];
        System.arraycopy(ptsXYZ[0], 0, last_circleXYZ[0], 0, npts);
        System.arraycopy(ptsXYZ[1], 0, last_circleXYZ[1], 0, npts);
        System.arraycopy(ptsXYZ[2], 0, last_circleXYZ[2], 0, npts);
     }

    for (int k=0; k<npts; k++) {
       float x = last_circleXYZ[0][k];
       float y = last_circleXYZ[1][k];
       float z = last_circleXYZ[2][k];

       float delx = x - pt0[0];
       float dely = y - pt0[1];
       float delz = z - pt0[2];

       float mag = (float) Math.sqrt(delx*delx+dely*dely+delz*delz);

       normls[idx] = delx/mag;
       coords[idx++] = x;
       normls[idx] = dely/mag;
       coords[idx++] = y;
       normls[idx] = delz/mag;
       coords[idx++] = z;

       if (clrDim == 3) {
          colors[cidx++] = clr0[0][0];
          colors[cidx++] = clr0[1][0];
          colors[cidx++] = clr0[2][0];
       }
       else { // must be four
          colors[cidx++] = clr0[0][0];
          colors[cidx++] = clr0[1][0];
          colors[cidx++] = clr0[2][0];
          colors[cidx++] = clr0[3][0];
       }
       vcnt++;

       x = circleXYZ[0][k];
       y = circleXYZ[1][k];
       z = circleXYZ[2][k];

       delx = x - pt1[0];
       dely = y - pt1[1];
       delz = z - pt1[2];

       mag = (float) Math.sqrt(delx*delx+dely*dely+delz*delz);

       normls[idx] = delx/mag;              
       coords[idx++] = x;
       normls[idx] = dely/mag;              
       coords[idx++] = y;
       normls[idx] = delz/mag;             
       coords[idx++] = z;

       if (clrDim == 3) {
          colors[cidx++] = clr1[0][0];
          colors[cidx++] = clr1[1][0];
          colors[cidx++] = clr1[2][0];
       }
       else { // must be four
          colors[cidx++] = clr1[0][0];
          colors[cidx++] = clr1[1][0];
          colors[cidx++] = clr1[2][0];
          colors[cidx++] = clr1[3][0];
       }
       vcnt++;
    }


     System.arraycopy(circleXYZ[0], 0, last_circleXYZ[0], 0, npts);
     System.arraycopy(circleXYZ[1], 0, last_circleXYZ[1], 0, npts);
     System.arraycopy(circleXYZ[2], 0, last_circleXYZ[2], 0, npts);

     vertCnt[0] = vcnt;
     return array;
  }  
   
}
