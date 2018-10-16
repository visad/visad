/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visad;

import java.rmi.RemoteException;

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
  float[][] lastFwdFace;
  float[] lastCntr;
  
  double[] lastMinDistPt = new double[3];
  
  float last_cellTerrain = Float.NaN;
  
  static float[][] circle;
  
  boolean conserveColor = false;
  
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

    this.startColor = new byte[clrDim];
    this.startColor[0] = startColor[0];
    this.startColor[1] = startColor[1];
    this.startColor[2] = startColor[2];
    if (clrDim == 4) this.startColor[3] = startColor[3];
    
    this.initialTime = initialTime;
    this.trajMan = trajMan;
    this.conserveColor = trajMan.conserveColor;
  }
  
  public void forward(FlowInfo info, float[][] flow_values, byte[][] color_values, GriddedSet spatial_set, FlatField terrain, int direction, float timeStep)
           throws VisADException, RemoteException {
     
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

           if (!conserveColor) {
             intrpClr[0] += weights[0][j]*ShadowType.byteToFloat(color_values[0][idx]);
             intrpClr[1] += weights[0][j]*ShadowType.byteToFloat(color_values[1][idx]);
             intrpClr[2] += weights[0][j]*ShadowType.byteToFloat(color_values[2][idx]);
             if (clrDim == 4) {
               intrpClr[3] += weights[0][j]*ShadowType.byteToFloat(color_values[3][idx]);
             }
           }

           //markGrid[idx] = true;
           //markGridTime[idx] = currentTimeIndex;
        }

        stopPts[0] = startPts[0] + intrpFlow[0];
        stopPts[1] = startPts[1] + intrpFlow[1];
        stopPts[2] = startPts[2] + intrpFlow[2];

        if (!conserveColor) {
          stopColor[0] = ShadowType.floatToByte(intrpClr[0]);
          stopColor[1] = ShadowType.floatToByte(intrpClr[1]);
          stopColor[2] = ShadowType.floatToByte(intrpClr[2]);
          if (clrDim == 4) {
            stopColor[3] = ShadowType.floatToByte(intrpClr[3]);
          }
        }
        else {
          stopColor[0] = startColor[0];
          stopColor[1] = startColor[1];
          stopColor[2] = startColor[2];
          if (clrDim == 4) {
            stopColor[3] = startColor[3];
          }           
        }
        
        // need to do terrain adjust here

        if (manifoldDimension == 2) {
           float[][] pts2D = new float[2][1];
           pts2D[0][0] = stopPts[0];
           pts2D[1][0] = stopPts[1];
           spatial_set.valueToInterp(pts2D, indices, weights, guess2D);
        }
        else if (manifoldDimension == 3) {
           float[][] pts3D = new float[3][1];
           pts3D[0][0] = stopPts[0];
           pts3D[1][0] = stopPts[1];
           pts3D[2][0] = stopPts[2];
           spatial_set.valueToInterp(pts3D, indices, weights, guess3D);

           if (terrain != null) {
             adjustFlowAtTerrain(terrain, color_values);
           }
        }
        
        if (indices[0] == null) {
           offGrid = true;
        }
        else {
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

  }
  
    public void forwardRK4(FlowInfo info, float[][] flow_values, float[][] flow_valuesB, float[][] flow_valuesC, byte[][] color_values, GriddedSet spatial_set, FlatField terrain, int direction, float timeStep)
           throws VisADException, RemoteException {
     
     if (offGrid) return;

     clrDim = color_values.length;
     float[] intrpClr = new float[clrDim];
     this.spatial_set = spatial_set;
     this.manifoldDimension = spatial_set.getManifoldDimension();


     
     float[][] flowLoc = new float[3][1];
     float[][] flowVec = new float[3][1];
     float[] intrpFlow = new float[3];
     
     float[] k1 = new float[3];
     float[] k2 = new float[3];
     float[] k3 = new float[3];
     float[] k4 = new float[3];
     float[] xyz = new float[3];

     if (indices[0] != null) {
        java.util.Arrays.fill(intrpFlow, 0f);
        java.util.Arrays.fill(intrpClr, 0);
 
        // k1 -------------------------------------------
        k1[0] = 0;
        k1[1] = 0;
        k1[2] = 0;
        for (int j=0; j<indices[0].length; j++) {
           int idx = indices[0][j];
           flowLoc[0][0] = info.spatial_values[0][idx];
           flowLoc[1][0] = info.spatial_values[1][idx];
           flowLoc[2][0] = info.spatial_values[2][idx];

           flowVec[0][0] = flow_values[0][idx];
           flowVec[1][0] = flow_values[1][idx];
           flowVec[2][0] = flow_values[2][idx];

           float[][] del = TrajectoryManager.computeDisplacement(info, flowLoc, flowVec, timeStep);
           k1[0] += weights[0][j]*(direction)*del[0][0];
           k1[1] += weights[0][j]*(direction)*del[1][0];
           k1[2] += weights[0][j]*(direction)*del[2][0];              
        }
        
        // k2 ----------------------------------
        xyz[0] = startPts[0] + k1[0]/2;
        xyz[1] = startPts[1] + k1[1]/2;
        xyz[2] = startPts[2] + k1[2]/2;
        int[][] indicesK = new int[1][];
        float[][] weightsK = new float[1][];
        if (manifoldDimension == 2) {
           float[][] pts2D = new float[2][1];
           pts2D[0][0] = xyz[0];
           pts2D[1][0] = xyz[1];
           spatial_set.valueToInterp(pts2D, indicesK, weightsK, guess2D);
        }
        else if (manifoldDimension == 3) {
           float[][] pts3D = new float[3][1];
           pts3D[0][0] = xyz[0];
           pts3D[1][0] = xyz[1];
           pts3D[2][0] = xyz[2];
           spatial_set.valueToInterp(pts3D, indicesK, weightsK, guess3D);
        }    
        if (indicesK[0] != null) {
           k2[0] = 0;
           k2[1] = 0;
           k2[2] = 0;
           for (int j=0; j<indicesK[0].length; j++) {
              int idx = indicesK[0][j];
              flowLoc[0][0] = info.spatial_values[0][idx];
              flowLoc[1][0] = info.spatial_values[1][idx];
              flowLoc[2][0] = info.spatial_values[2][idx];

              flowVec[0][0] = flow_valuesB[0][idx];
              flowVec[1][0] = flow_valuesB[1][idx];
              flowVec[2][0] = flow_valuesB[2][idx];

              float[][] del = TrajectoryManager.computeDisplacement(info, flowLoc, flowVec, timeStep);
              k2[0] += weightsK[0][j]*(direction)*del[0][0];
              k2[1] += weightsK[0][j]*(direction)*del[1][0];
              k2[2] += weightsK[0][j]*(direction)*del[2][0];                            
           }
        }
        
        // k3 -----------------------------------
        xyz[0] = startPts[0] + k2[0]/2;
        xyz[1] = startPts[1] + k2[1]/2;
        xyz[2] = startPts[2] + k2[2]/2;
        indicesK = new int[1][];
        weightsK = new float[1][];
        if (manifoldDimension == 2) {
           float[][] pts2D = new float[2][1];
           pts2D[0][0] = xyz[0];
           pts2D[1][0] = xyz[1];
           spatial_set.valueToInterp(pts2D, indicesK, weightsK, guess2D);
        }
        else if (manifoldDimension == 3) {
           float[][] pts3D = new float[3][1];
           pts3D[0][0] = xyz[0];
           pts3D[1][0] = xyz[1];
           pts3D[2][0] = xyz[2];
           spatial_set.valueToInterp(pts3D, indicesK, weightsK, guess3D);
        }    
        if (indicesK[0] != null) {
           k3[0] = 0;
           k3[1] = 0;
           k3[2] = 0;
           for (int j=0; j<indicesK[0].length; j++) {
              int idx = indicesK[0][j];
              flowLoc[0][0] = info.spatial_values[0][idx];
              flowLoc[1][0] = info.spatial_values[1][idx];
              flowLoc[2][0] = info.spatial_values[2][idx];

              flowVec[0][0] = flow_valuesB[0][idx];
              flowVec[1][0] = flow_valuesB[1][idx];
              flowVec[2][0] = flow_valuesB[2][idx];

              float[][] del = TrajectoryManager.computeDisplacement(info, flowLoc, flowVec, timeStep);
              k3[0] += weightsK[0][j]*(direction)*del[0][0];
              k3[1] += weightsK[0][j]*(direction)*del[1][0];
              k3[2] += weightsK[0][j]*(direction)*del[2][0];                            
           }
        }
        
        // k4 ----------------------------------------
        xyz[0] = startPts[0] + k3[0];
        xyz[1] = startPts[1] + k3[1];
        xyz[2] = startPts[2] + k3[2];
        indicesK = new int[1][];
        weightsK = new float[1][];
        if (manifoldDimension == 2) {
           float[][] pts2D = new float[2][1];
           pts2D[0][0] = xyz[0];
           pts2D[1][0] = xyz[1];
           spatial_set.valueToInterp(pts2D, indicesK, weightsK, guess2D);
        }
        else if (manifoldDimension == 3) {
           float[][] pts3D = new float[3][1];
           pts3D[0][0] = xyz[0];
           pts3D[1][0] = xyz[1];
           pts3D[2][0] = xyz[2];
           spatial_set.valueToInterp(pts3D, indicesK, weightsK, guess3D);
        }            
        if (indicesK[0] != null) {
           k4[0] = 0;
           k4[1] = 0;
           k4[2] = 0;
           for (int j=0; j<indicesK[0].length; j++) {
              int idx = indicesK[0][j];
              flowLoc[0][0] = info.spatial_values[0][idx];
              flowLoc[1][0] = info.spatial_values[1][idx];
              flowLoc[2][0] = info.spatial_values[2][idx];

              flowVec[0][0] = flow_valuesC[0][idx];
              flowVec[1][0] = flow_valuesC[1][idx];
              flowVec[2][0] = flow_valuesC[2][idx];

              float[][] del = TrajectoryManager.computeDisplacement(info, flowLoc, flowVec, timeStep);
              k4[0] += weightsK[0][j]*(direction)*del[0][0];
              k4[1] += weightsK[0][j]*(direction)*del[1][0];
              k4[2] += weightsK[0][j]*(direction)*del[2][0];                            
           }
        }                
        
        stopPts[0] = startPts[0] + (1f/6)*k1[0] + (1f/3)*k2[0] + (1f/3)*k3[0] + (1f/6)*k4[0];
        stopPts[1] = startPts[1] + (1f/6)*k1[1] + (1f/3)*k2[1] + (1f/3)*k3[1] + (1f/6)*k4[1];
        stopPts[2] = startPts[2] + (1f/6)*k1[2] + (1f/3)*k2[2] + (1f/3)*k3[2] + (1f/6)*k4[2];
        
        xyz[0] = stopPts[0];
        xyz[1] = stopPts[1];
        xyz[2] = stopPts[2];
        if (manifoldDimension == 2) {
           float[][] pts2D = new float[2][1];
           pts2D[0][0] = xyz[0];
           pts2D[1][0] = xyz[1];
           spatial_set.valueToInterp(pts2D, indices, weights, guess2D);
        }
        else if (manifoldDimension == 3) {
           float[][] pts3D = new float[3][1];
           pts3D[0][0] = xyz[0];
           pts3D[1][0] = xyz[1];
           pts3D[2][0] = xyz[2];
           spatial_set.valueToInterp(pts3D, indices, weights, guess3D);
           
           if (terrain != null) {
             adjustFlowAtTerrain(terrain, color_values);
           }
        }
        
        if (indices[0] != null) {
           if (!conserveColor) {
             for (int j=0; j<indices[0].length; j++) {
               int idx = indices[0][j];
               intrpClr[0] += weights[0][j]*ShadowType.byteToFloat(color_values[0][idx]);
               intrpClr[1] += weights[0][j]*ShadowType.byteToFloat(color_values[1][idx]);
               intrpClr[2] += weights[0][j]*ShadowType.byteToFloat(color_values[2][idx]);
               if (clrDim == 4) {
                 intrpClr[3] += weights[0][j]*ShadowType.byteToFloat(color_values[3][idx]);
               }
             }
        
             stopColor[0] = ShadowType.floatToByte(intrpClr[0]);
             stopColor[1] = ShadowType.floatToByte(intrpClr[1]);
             stopColor[2] = ShadowType.floatToByte(intrpClr[2]);
             if (clrDim == 4) {
               stopColor[3] = ShadowType.floatToByte(intrpClr[3]);
             }
           }
           else {
             stopColor[0] = startColor[0];
             stopColor[1] = startColor[1];
             stopColor[2] = startColor[2];
             if (clrDim == 4) {
              stopColor[3] = startColor[3];
             }             
           }
        }
        
        if (indices[0] == null) {
           offGrid = true;
        }
        else {
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

  }
  
  /*
    Add start/stop pair segment to the TrajectoryManager. 
   */
  private void addPair(float[] startPt, float[] stopPt, byte[] startColor, byte[] stopColor) {

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
  
  /* 
    Advance forecast (stop) point location, color and intrp info to the start for the next displacement interval.
  */
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

     startCell = indices[0];
     cellWeights = weights[0];
     if (indices[0] == null) {
        offGrid = true;
     }     
  }
  
  private void adjustFlowAtTerrain(FlatField terrain, byte[][] color_values) throws VisADException, RemoteException {
     // Do terrain adjustment here
     float[] intrpClr = new float[clrDim];
     
     if (terrain != null && indices[0] != null ) {
        int[] lens = spatial_set.getLengths();
        float[][] spatial_values = spatial_set.getSamples(false);
        int dir = (spatial_values[2][0] < spatial_values[2][lens[0]*lens[1]]) ? 1 : -1;

        // get interpolated terrain and parcel height at this grid cell
        float parcelHgt = stopPts[2];
        float parcelX = stopPts[0];
        float parcelY = stopPts[1];
        RealTuple xy = new RealTuple(((FunctionType)terrain.getType()).getDomain(), new double[] {parcelX, parcelY});
        float cellTerrain = (float) ((Real)terrain.evaluate(xy, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS)).getValue();
        

        float diff = parcelHgt - cellTerrain;

        if (diff < 0f) {
           
           float zUp = stopPts[2] - diff + 0.0015f;
           if (!Float.isNaN(last_cellTerrain) && (Math.abs(last_cellTerrain - cellTerrain) < 0.0005)) {
              zUp = startPts[2];
           }
           float[][] pts3D = new float[3][1];
           pts3D[0][0] = stopPts[0];
           pts3D[1][0] = stopPts[1];
           pts3D[2][0] = zUp;
           
           spatial_set.valueToInterp(pts3D, indices, weights, guess3D);
           if (indices[0] != null) {
             stopPts[2] = zUp;

             java.util.Arrays.fill(intrpClr, 0);
             for (int k=0; k<indices[0].length; k++) {
               int idx = indices[0][k];
               intrpClr[0] += weights[0][k]*ShadowType.byteToFloat(color_values[0][idx]);
               intrpClr[1] += weights[0][k]*ShadowType.byteToFloat(color_values[1][idx]);
               intrpClr[2] += weights[0][k]*ShadowType.byteToFloat(color_values[2][idx]);
               if (clrDim == 4) {
                 intrpClr[3] += weights[0][k]*ShadowType.byteToFloat(color_values[3][idx]);
               }                         
             }
           
             stopColor[0] = ShadowType.floatToByte(intrpClr[0]);
             stopColor[1] = ShadowType.floatToByte(intrpClr[1]);
             stopColor[2] = ShadowType.floatToByte(intrpClr[2]);
             if (clrDim == 4) {
               stopColor[3] = ShadowType.floatToByte(intrpClr[3]);
             }
           }
        }
        last_cellTerrain = cellTerrain;
     
     }   
  }
  
  public void makeCylinderStrip(int q, float[] uvecPath, float[] uvecPathNext, float[] pt0, float[] pt1, byte[][] clr0, byte[][] clr1, float size,
              int npts, float[] coords, byte[] colors, float[] normls, float[] elbowCoords, byte[] elbowColors, float[] elbowNormals, int[] vertCnt, int[] elbowVertCnt, int[] elbowStrips, int[] elbowStripCnt) {
     
     int clrDim = clr0.length;
     
     float[] cntr = new float[3];
     
     boolean doElbow = true;
     double[] planeNormal = new double[3];
     if ( q == npairs-1) { 
       planeNormal[0] = uvecPath[0];
       planeNormal[1] = uvecPath[1];
       planeNormal[2] = uvecPath[2];
       doElbow = false;
     }
     else {
       planeNormal = TrajectoryManager.getBisectPlaneNormal(uvecPath, uvecPathNext);
     }
     double[] bisectPlaneCoeffs = TrajectoryManager.getPlaneCoeffsFromNormalAndPoint(planeNormal, new double[] {pt1[0], pt1[1], pt1[2]});

     
     if (circleXYZ == null) {
        circleXYZ = new float[3][npts];
     }
     if (last_circleXYZ == null) {
        last_circleXYZ = new float[3][npts];        
     }
     if (q == 0) { // first time
        float[] norm = new float[] {0f, 0f, 1f};
        float[][] ptsXYZ = makeCircle(size, uvecPath, norm ,npts, pt0);
        last_circleXYZ = new float[3][npts];
        System.arraycopy(ptsXYZ[0], 0, last_circleXYZ[0], 0, npts);
        System.arraycopy(ptsXYZ[1], 0, last_circleXYZ[1], 0, npts);
        System.arraycopy(ptsXYZ[2], 0, last_circleXYZ[2], 0, npts);
        lastCntr = new float[3];
        lastCntr[0] = pt0[0];
        lastCntr[1] = pt0[1];
        lastCntr[2] = pt0[2];
        if (lastFwdFace == null) {
           lastFwdFace = new float[3][npts];
        }
        else {
          makeCylinderSegment(lastFwdFace, pt0, clr0, last_circleXYZ, pt0, clr0, elbowCoords, elbowNormals, elbowColors, elbowVertCnt);
          elbowStrips[elbowStripCnt[0]++] = npts*2;
        }
     }
     else {
       double[] coeffs = TrajectoryManager.getPlaneCoeffsFromNormalAndPoint(new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, lastMinDistPt);
       double[] P = TrajectoryManager.getLinePlaneIntersect(coeffs, new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, new double[] {pt0[0], pt0[1], pt0[2]});
       float[] Pf = new float[] {(float)P[0], (float)P[1], (float)P[2]};
       lastCntr[0] = (float) P[0];
       lastCntr[1] = (float) P[1];
       lastCntr[2] = (float) P[2];
       
       float[] norm = new float[] {0f, 0f, 1f};
       float[][] ptsXYZ = makeCircle(size, uvecPath, norm ,npts, lastCntr);
       last_circleXYZ = new float[3][npts];
       System.arraycopy(ptsXYZ[0], 0, last_circleXYZ[0], 0, npts);
       System.arraycopy(ptsXYZ[1], 0, last_circleXYZ[1], 0, npts);
       System.arraycopy(ptsXYZ[2], 0, last_circleXYZ[2], 0, npts);        
     }
     
     
     double minDist = Double.MAX_VALUE;
     double[] minDistPt = new double[3];
     int minDistIdx;
     for (int k=0; k<npts; k++) {
        double[] pt = new double[] {last_circleXYZ[0][k], last_circleXYZ[1][k], last_circleXYZ[2][k]};
        double[] P = TrajectoryManager.getLinePlaneIntersect(bisectPlaneCoeffs, new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, pt);
        double delx = pt[0] - P[0];
        double dely = pt[1] - P[1];
        double delz = pt[2] - P[2];
        double dist = Math.sqrt(delx*delx + dely*dely + delz*delz);
        if (dist < minDist) {
           minDist = dist;
           minDistPt[0] = P[0];
           minDistPt[1] = P[1];
           minDistPt[2] = P[2];
           minDistIdx = k;
        }
     }
     lastMinDistPt[0] = minDistPt[0];
     lastMinDistPt[1] = minDistPt[1];
     lastMinDistPt[2] = minDistPt[2];
     
     double[] coeffs;
     if (q == npairs-1) {
       coeffs = TrajectoryManager.getPlaneCoeffsFromNormalAndPoint(new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, new double[] {pt1[0], pt1[1], pt1[2]});
     }
     else {
       coeffs = TrajectoryManager.getPlaneCoeffsFromNormalAndPoint(new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, minDistPt);        
     }
     
     for (int k=0; k<npts; k++) {
        double[] pt = new double[] {last_circleXYZ[0][k], last_circleXYZ[1][k], last_circleXYZ[2][k]};
        double[] P = TrajectoryManager.getLinePlaneIntersect(coeffs, new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, pt);
        circleXYZ[0][k] = (float) P[0];
        circleXYZ[1][k] = (float) P[1];
        circleXYZ[2][k] = (float) P[2];        
     }
     double[] P  = TrajectoryManager.getLinePlaneIntersect(coeffs, new double[] {uvecPath[0], uvecPath[1], uvecPath[2]}, new double[] {pt0[0], pt0[1], pt0[2]});
     cntr[0] = (float) P[0];
     cntr[1] = (float) P[1];
     cntr[2] = (float) P[2];

     makeCylinderSegment(last_circleXYZ, lastCntr, clr0, circleXYZ, cntr, clr1, coords, normls, colors, vertCnt);
  
     
    
    /* this is the next cylinder back face */
    coeffs = TrajectoryManager.getPlaneCoeffsFromNormalAndPoint(new double[] {uvecPathNext[0], uvecPathNext[1], uvecPathNext[2]}, minDistPt);
    P = TrajectoryManager.getLinePlaneIntersect(coeffs, new double[] {uvecPathNext[0], uvecPathNext[1], uvecPathNext[2]}, new double[] {pt1[0], pt1[1], pt1[2]});
    float[] Pfnext = new float[] {(float)P[0], (float)P[1], (float)P[2]};
    lastCntr[0] = (float) P[0];
    lastCntr[1] = (float) P[1];
    lastCntr[2] = (float) P[2];
       
    float[] norm = new float[] {0f, 0f, 1f};
    float[][] ptsXYZ = makeCircle(size, uvecPathNext, norm ,npts, lastCntr);
    System.arraycopy(ptsXYZ[0], 0, last_circleXYZ[0], 0, npts);
    System.arraycopy(ptsXYZ[1], 0, last_circleXYZ[1], 0, npts);
    System.arraycopy(ptsXYZ[2], 0, last_circleXYZ[2], 0, npts);
    
    
    /* construct elbow */
    if (doElbow) {
      makeCylinderSegment(circleXYZ, cntr, clr1, last_circleXYZ, lastCntr, clr1, elbowCoords, elbowNormals, elbowColors, elbowVertCnt);
      elbowStrips[elbowStripCnt[0]++] = (npts*2);
    }
    
    System.arraycopy(circleXYZ[0], 0, lastFwdFace[0], 0, npts);
    System.arraycopy(circleXYZ[1], 0, lastFwdFace[1], 0, npts);
    System.arraycopy(circleXYZ[2], 0, lastFwdFace[2], 0, npts);
    
  }
  
  /**
   * 
   * @param radius
   * @param uvec unit vector normal to plane containing circle
   * @param norm unit vector perpendicular to uvec
   * @param npts number of points around
   * @param center
   * @return 
   */  
  public static float[][] makeCircle(float radius, float[] uvec, float[] norm, int npts, float[] center) {
    if (circle == null) { // static because only need to do this once
       circle = new float[2][npts];
       float intrvl = (float) (2*Math.PI)/(npts-1);
       for (int i=0; i<npts; i++) {
         circle[0][i] = (float) Math.cos(intrvl*i);  // s
         circle[1][i] = (float) Math.sin(intrvl*i);  // t
       }
     }
    //float[] norm = new float[] {0f, 0f, 1f};
    float[] norm_x_trj;
    float[] trj_x_norm_x_trj;
    norm_x_trj = TrajectoryManager.AxB(norm, uvec);
    trj_x_norm_x_trj = TrajectoryManager.AxB(uvec, norm_x_trj);
    float[] T = trj_x_norm_x_trj;
    float[] S = norm_x_trj;
    float[][] ptsXYZ = new float[3][npts];
    for (int k=0; k<npts; k++) {
      float s = radius*circle[0][k];
      float t = radius*circle[1][k];
      ptsXYZ[0][k] = center[0] + s*S[0] + t*T[0];
      ptsXYZ[1][k] = center[1] + s*S[1] + t*T[1];
      ptsXYZ[2][k] = center[2] + s*S[2] + t*T[2];
    }
    return ptsXYZ;
  }
  
  
  public static void makeCylinderSegment(float[][] last_circleXYZ, float[] pt0, byte[][] clr0, float[][] circleXYZ, float[] pt1, byte[][] clr1, float[] coords, float[] normls, byte[] colors, int[] vcnt_a) {
     int clrDim = clr0.length;
     int npts = last_circleXYZ[0].length;
     int vcnt = vcnt_a[0];
     
     for (int k=0; k<npts; k++) {
       float x = last_circleXYZ[0][k];
       float y = last_circleXYZ[1][k];
       float z = last_circleXYZ[2][k];

       float delx = x - pt0[0];
       float dely = y - pt0[1];
       float delz = z - pt0[2];

       float mag = (float) Math.sqrt(delx*delx+dely*dely+delz*delz);

       int idx = vcnt*3;
       int cidx = vcnt*clrDim;
       
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
    vcnt_a[0] = vcnt;
  }
   
}
