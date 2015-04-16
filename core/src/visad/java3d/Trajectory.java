package visad.java3d;

import java.util.ArrayList;
import java.util.Iterator;
import visad.CommonUnit;
import visad.Data;
import visad.FlowInfo;
import visad.Gridded1DDoubleSet;
import visad.Gridded1DSet;
import visad.Gridded3DSet;
import visad.GriddedSet;
import visad.Set;
import visad.ShadowType;
import visad.TrajectoryParams;
import visad.Unit;
import visad.VisADException;
import visad.VisADGeometryArray;
import visad.VisADLineArray;
import visad.VisADTriangleArray;


public class Trajectory {
     float startX;
     float startY;
     float startZ;

     float[] startPts = new float[3];
     float[]  stopPts = new float[3];
     
     int[] startCell;
     float[] cellWeights;

     byte[] startColor;
     byte[] stopColor;

     float[][] startPts2D = new float[2][1];
     float[][] startPts3D = new float[3][1];

     static int coordCnt = 0;
     static int colorCnt = 0;
     static int vertCnt = 0;

     int clrDim;

     boolean offGrid = false;

     private static float[] coordinates = null;
     private static byte[] colors = null;
     
     public static boolean[] markGrid;
     public static int[] markGridTime;

     public static int cnt=0;
     public static int[] o_j = new int[] {0, 0, 1, 1}; 
     public static int[] o_i = new int[] {0, 1, 0, 1}; 

     public int initialTimeIndex = 0;
     public int currentTimeIndex = 0;
 
     public double initialTime = 0;
     public double currentTime = 0;

     float[] uVecPath = new float[] {Float.NaN, Float.NaN, Float.NaN};

     public Trajectory(float startX, float startY, float startZ, int[] startCell, float[] cellWeights, byte[] startColor) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;

        startPts[0] = startX;
        startPts[1] = startY;
        startPts[2] = startZ;
        this.startCell = startCell;
        this.cellWeights = cellWeights;

        clrDim = startColor.length;
        stopColor = new byte[clrDim];

        this.startColor = startColor;
     }

     public static void makeTrajectories(double time, ArrayList<Trajectory> trajectories, float[][] startPts, byte[][] color_values, GriddedSet spatial_set) throws VisADException  {
        int num = startPts[0].length;
        int clrDim = color_values.length;
        
        // determine grid relative positions of start points
        int manifoldDimension = spatial_set.getManifoldDimension();
        int[][] indices = new int[num][];
        float[][] weights = new float[num][];
        
        if (manifoldDimension == 2) {
          spatial_set.valueToInterp(new float[][] {startPts[0], startPts[1]}, indices, weights);
        }
        else if (manifoldDimension == 3) {
          spatial_set.valueToInterp(new float[][] {startPts[0], startPts[1], startPts[2]}, indices, weights);
        }

        for (int k=0; k<num; k++) {
           // initialize a new trajectory
           float startX = startPts[0][k];
           float startY = startPts[1][k];
           float startZ = startPts[2][k];

           byte[] startColor = new byte[clrDim];
           startColor[0] = color_values[0][k];
           startColor[1] = color_values[1][k];
           startColor[2] = color_values[2][k];
           if (clrDim == 4) {
              startColor[3] = color_values[3][k];
           }
           
           Trajectory traj = new Trajectory(startX, startY, startZ, indices[k], weights[k], startColor);
           traj.initialTime = time;
           trajectories.add(traj);
        }
     }
     
     public static void getStartPointsFromDomain(int skip, Gridded3DSet spatial_set, byte[][] color_values, float[][] startPts, byte[][] startClrs) throws VisADException {
         int manifoldDim = spatial_set.getManifoldDimension();
         int[] lens = spatial_set.getLengths();
         int lenX = lens[0];
         int lenY = lens[1];
         int lenZ;
         if (manifoldDim == 3) {
             lenZ = lens[2];
             getStartPointsFromDomain3D(skip, spatial_set.getSamples(false), lenX, lenY, lenZ, color_values, startPts, startClrs);
         }
         else if (manifoldDim == 2) {
             getStartPointsFromDomain2D(skip, spatial_set.getSamples(false), lenX, lenY, color_values, startPts, startClrs);
         }
     }
     
     public static void getStartPointsFromDomain3D(int skip, float[][] locs, int lenX, int lenY, int lenZ, byte[][] color_values, float[][] startPts, byte[][] startClrs) throws VisADException {
         int len2D = lenX*lenY;
         
         float[][] locs2D = new float[3][len2D];
         float[][] pts = new float[3][];
         byte[][] clrs = new byte[startClrs.length][];
         
         int lenA = 0;
         
         for (int k=0; k<lenZ-skip*2; k+=skip*2) {
             System.arraycopy(locs[0], k*len2D, locs2D[0], 0, len2D);
             System.arraycopy(locs[1], k*len2D, locs2D[1], 0, len2D);
             System.arraycopy(locs[2], k*len2D, locs2D[2], 0, len2D);
             
             getStartPointsFromDomain2D(skip, locs2D, lenX, lenY, color_values, pts, clrs);
             
             int lenB = pts[0].length;
             float[][] tmpPts = new float[3][lenA+lenB];
             byte[][] tmpClrs = new byte[clrs.length][lenA+lenB];
            
             if (lenA > 0) {
                System.arraycopy(startPts[0], 0, tmpPts[0], 0, lenA);
                System.arraycopy(startClrs[0], 0, tmpClrs[0], 0, lenA);
             }
             System.arraycopy(pts[0], 0, tmpPts[0], lenA, lenB);
             System.arraycopy(clrs[0], 0, tmpClrs[0], lenA, lenB);
             startPts[0] = tmpPts[0];
             startClrs[0] = tmpClrs[0];

             if (lenA > 0) {
                System.arraycopy(startPts[1], 0, tmpPts[1], 0, lenA);
                System.arraycopy(startClrs[1], 0, tmpClrs[1], 0, lenA);
             }
             System.arraycopy(pts[1], 0, tmpPts[1], lenA, lenB);
             System.arraycopy(clrs[1], 0, tmpClrs[1], lenA, lenB);
             startPts[1] = tmpPts[1];
             startClrs[1] = tmpClrs[1];
             
             if (lenA > 0) {
                System.arraycopy(startPts[2], 0, tmpPts[2], 0, lenA);
                System.arraycopy(startClrs[2], 0, tmpClrs[2], 0, lenA);
             }
             System.arraycopy(pts[2], 0, tmpPts[2], lenA, lenB);
             System.arraycopy(clrs[2], 0, tmpClrs[2], lenA, lenB);
             startPts[2] = tmpPts[2];
             startClrs[2] = tmpClrs[2];
             
             if (lenA > 0) {
                 if (startClrs.length == 4) {
                     System.arraycopy(startClrs[3], 0, tmpClrs[3], 0, lenA);
                     System.arraycopy(clrs[3], 0, tmpClrs[3], lenA, lenB);
                     startClrs[3] = tmpClrs[3];
                 }
             }
             
             lenA = startPts[0].length;
         }
     }

     public static void getStartPointsFromDomain2D(int skip, float[][] setLocs, int lenX, int lenY, byte[][] color_values, float[][] startPts, byte[][] startClrs) throws VisADException {
        int clrDim = color_values.length;
        int m = cnt % 4;
        cnt++;

        int jA = 1+o_j[m]*(skip/2);
        int jB = lenY-skip;
        int iA = 1+o_i[m]*(skip/2);
        int iB = lenX-skip;

        int numJ = 1 + ((jB-1)-jA)/skip;
        int numI = 1 + ((iB-1)-iA)/skip;
        int num = numJ*numI;

        startPts[0] = new float[num];
        startPts[1] = new float[num];
        startPts[2] = new float[num];

        startClrs[0] = new byte[num];
        startClrs[1] = new byte[num];
        startClrs[2] = new byte[num];
        if (clrDim == 4) {
           startClrs[3] = new byte[num];
        }

        num = 0;
        for (int j=1+o_j[m]*(skip/2); j<lenY-skip; j+=skip) {
          for (int i=1+o_i[m]*(skip/2); i<lenX-skip; i+=skip) {

            int k = j*lenX + i;

            if (!markGrid[k]) {
              startPts[0][num] = setLocs[0][k];
              startPts[1][num] = setLocs[1][k];
              startPts[2][num] = setLocs[2][k];

              startClrs[0][num] = color_values[0][k];
              startClrs[1][num] = color_values[1][k];
              startClrs[2][num] = color_values[2][k];
              if (clrDim == 4) {
                startClrs[3][num] = color_values[3][k];
              }
            }
          
            num++;
          }
        }

        /*
        for (int k=0; k<markGrid.length; k++) {
           markGrid[k] = false;
        }
        */
     }

     public static void makeTrajectories(double time, ArrayList<Trajectory> trajectories, int skip, byte[][] color_values, float[][] startPts, int[] setLens) {
        int lenX = setLens[0];
        int lenY = setLens[1];

        int clrDim = color_values.length;
        int m = cnt % 4;
        cnt++;

        for (int j=1+o_j[m]*(skip/2); j<lenY-skip; j+=skip) {
          for (int i=1+o_i[m]*(skip/2); i<lenX-skip; i+=skip) {

            int k = j*lenX + i;

            if (!markGrid[k]) {
              // initialize a new trajectory
              float startX = startPts[0][k];
              float startY = startPts[1][k];
              float startZ = startPts[2][k];

              byte[] startColor = new byte[clrDim];
              startColor[0] = color_values[0][k];
              startColor[1] = color_values[1][k];
              startColor[2] = color_values[2][k];
              if (clrDim == 4) {
                startColor[3] = color_values[3][k];
              }

              /*
              Trajectory traj = new Trajectory(startX, startY, startZ, startColor);
              traj.initialTime = time;
              trajectories.add(traj);
              */
            }

          }
        }

        /*
        for (int k=0; k<markGrid.length; k++) {
           markGrid[k] = false;
        }
        */
     }

     /* For steady-state trajectories (animated streamlines) only */
     public static void checkTime(int timeIdx) {
       for (int k=0; k<markGridTime.length; k++) {
         if ((timeIdx - markGridTime[k]) > 4) {
           markGridTime[k] = timeIdx;
           markGrid[k] = false;
         }
       }
     }

     /* Remove trajectories from list:
          (1) That have left the grid (marked offGrid).
          (2) That have time length (duration) greater than some threshold.
      */
     public static ArrayList<Trajectory> clean(ArrayList<Trajectory> trajectories, double threshold) {
       ArrayList<Trajectory> newList = new ArrayList<Trajectory>();
       Iterator<Trajectory> iter = trajectories.iterator();
       while (iter.hasNext() ) {
         Trajectory traj = iter.next();
         if (!traj.offGrid && ((traj.currentTime - traj.initialTime) < threshold)) {
           newList.add(traj);
         }
       }
       return newList;
     }

     public static ArrayList<Trajectory> clean(ArrayList<Trajectory> trajectories) {
        return Trajectory.clean(trajectories, -1.0);
     }

     /* Set internal counters to zero. Replace internal arrays and initialize to NaN. */
     public static void reset(int maxNumVerts, int clrDim) {
        coordCnt = 0; 
        colorCnt = 0;
        vertCnt = 0;
        maxNumVerts *= 2; // one each for start and stop
        coordinates = new float[3*maxNumVerts];
        colors = new byte[clrDim*maxNumVerts];
        java.util.Arrays.fill(coordinates, Float.NaN);
     }
     
     public static void reset(ArrayList<Trajectory> trajectories, int numIntrpPts, int clrDim) {
        for (int k=0; k<trajectories.size(); k++) {
            Trajectory traj = trajectories.get(k);
             //traj.reset();
        }
     }

     public static VisADLineArray makeGeometry() {
       VisADLineArray array = new VisADLineArray();
       float[] newCoords = new float[coordCnt];
       byte[] newColors = new byte[colorCnt];
       System.arraycopy(coordinates, 0, newCoords, 0, newCoords.length);
       System.arraycopy(colors, 0, newColors, 0, newColors.length);
       array.coordinates = newCoords;
       array.colors = newColors;
       array.vertexCount = vertCnt;

       return array;
     }
     
     public static VisADGeometryArray makeGeometry(ArrayList<Trajectory> trajectories) {
         return null;
     }

     public static VisADGeometryArray makeTracerGeometry(ArrayList<Trajectory> trajectories, 
             ArrayList<VisADGeometryArray> arrays, ArrayList<float[]> anchors, int direction, float trcrSize, double[] scale, boolean fill) {
       int numTrajs = trajectories.size();
       VisADGeometryArray array = null;
       float[] coords = null;
       byte[] colors = null;
       float[] normals = null;
       int numPts;
       int numVerts;

       double barblen = (0.7/scale[0])*trcrSize*0.034;

       float[] norm = new float[] {0, 0, 1f};
       float[] trj_u = new float[3];

       for (int k=0; k<numTrajs; k++) {
           
         if (!fill) { // make simple arrow ---------
           numPts = 2*4;
           numVerts = numPts*1;
           coords =  new float[3*numVerts];
           colors = new byte[3*numVerts];
           array = new VisADLineArray();
         }
         else { // filled arrow head -------------
           numPts = 2*6;
           numVerts = numPts*1;
           coords = new float[3*numVerts];
           colors = new byte[3*numVerts];
           //normals = new float[3*numVerts];
           array = new VisADTriangleArray();
         }              

           
         Trajectory traj = trajectories.get(k);
         trj_u[0] = traj.uVecPath[0];
         trj_u[1] = traj.uVecPath[1];
         trj_u[2] = traj.uVecPath[2];

         float[] endPt = new float[3];
         endPt[0] = traj.startPts[0];
         endPt[1] = traj.startPts[1];
         endPt[2] = traj.startPts[2];

         float[] norm_x_trj = new float[] {
                    norm[1] * trj_u[2] - norm[2] * trj_u[1],
                  -(norm[0] * trj_u[2] - norm[2] * trj_u[0]),
                    norm[0] * trj_u[1] - norm[1] * trj_u[0] };

         float mag = (float) Math.sqrt(norm_x_trj[0] * norm_x_trj[0] +
                                       norm_x_trj[1] * norm_x_trj[1] +
		                       norm_x_trj[2] * norm_x_trj[2]);

	 // - normalize vector
         norm_x_trj[0] /= mag;
         norm_x_trj[1] /= mag;
         norm_x_trj[2] /= mag;
         
         float[] norm_x_trj_x_trj = new float[] {
                    norm_x_trj[1] * trj_u[2] - norm_x_trj[2] * trj_u[1],
                  -(norm_x_trj[0] * trj_u[2] - norm_x_trj[2] * trj_u[0]),
                    norm_x_trj[0] * trj_u[1] - norm_x_trj[1] * trj_u[0] };
       
         mag = (float) Math.sqrt(norm_x_trj_x_trj[0] * norm_x_trj_x_trj[0] +
                                       norm_x_trj_x_trj[1] * norm_x_trj_x_trj[1] +
		                       norm_x_trj_x_trj[2] * norm_x_trj_x_trj[2]);
         
         norm_x_trj_x_trj[0] /= mag;
         norm_x_trj_x_trj[1] /= mag;
         norm_x_trj_x_trj[2] /= mag;
        
         float[] ptOnPath = new float[3];
         
         float len = (float) (barblen*Math.cos(Data.DEGREES_TO_RADIANS*22.0));
         ptOnPath[0] = -len*trj_u[0];
         ptOnPath[1] = -len*trj_u[1];
         ptOnPath[2] = -len*trj_u[2];
         
         ptOnPath[0] += endPt[0];
         ptOnPath[1] += endPt[1];
         ptOnPath[2] += endPt[2];
         
         float[] barbPtA = new float[3];
         float[] barbPtB = new float[3];
         float[] barbPtC = new float[3];
         float[] barbPtD = new float[3];
         len = (float) (barblen*Math.sin(Data.DEGREES_TO_RADIANS*22.0));
         
         barbPtA[0] = len*norm_x_trj[0];
         barbPtA[1] = len*norm_x_trj[1];
         barbPtA[2] = len*norm_x_trj[2];
         
         barbPtB[0] = -len*norm_x_trj[0];
         barbPtB[1] = -len*norm_x_trj[1];
         barbPtB[2] = -len*norm_x_trj[2];
         
         barbPtA[0] += ptOnPath[0];
         barbPtA[1] += ptOnPath[1]; 
         barbPtA[2] += ptOnPath[2]; 
         
         barbPtB[0] += ptOnPath[0];
         barbPtB[1] += ptOnPath[1];
         barbPtB[2] += ptOnPath[2];
         
         len *= scale[0]/scale[2]; // simple adjust for anistropic display scale
         
         barbPtC[0] = len*norm_x_trj_x_trj[0];
         barbPtC[1] = len*norm_x_trj_x_trj[1];
         barbPtC[2] = len*norm_x_trj_x_trj[2];
         
         barbPtD[0] = -len*norm_x_trj_x_trj[0];
         barbPtD[1] = -len*norm_x_trj_x_trj[1];
         barbPtD[2] = -len*norm_x_trj_x_trj[2];
         
         barbPtC[0] += ptOnPath[0];
         barbPtC[1] += ptOnPath[1]; 
         barbPtC[2] += ptOnPath[2]; 
         
         barbPtD[0] += ptOnPath[0];
         barbPtD[1] += ptOnPath[1];
         barbPtD[2] += ptOnPath[2];
         	  
         int t = 0;
         int c = 0;
         
         coords[t] = traj.startPts[0];
         coords[t+=1] = traj.startPts[1];
         coords[t+=1] = traj.startPts[2];
         
         colors[c] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         	     
         coords[t+=1] = barbPtA[0];
         coords[t+=1] = barbPtA[1];
         coords[t+=1] = barbPtA[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         if (fill) {
           coords[t+=1] = ptOnPath[0];
           coords[t+=1] = ptOnPath[1];
           coords[t+=1] = ptOnPath[2];
           
           colors[c+=1] = traj.startColor[0];
           colors[c+=1] = traj.startColor[1];
           colors[c+=1] = traj.startColor[2];
         }
         
         coords[t+=1] = traj.startPts[0];
         coords[t+=1] = traj.startPts[1];
         coords[t+=1] = traj.startPts[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         coords[t+=1] = barbPtB[0];
         coords[t+=1] = barbPtB[1];
         coords[t+=1] = barbPtB[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         if (fill) {
           coords[t+=1] = ptOnPath[0];
           coords[t+=1] = ptOnPath[1];
           coords[t+=1] = ptOnPath[2];
           
           colors[c+=1] = traj.startColor[0];
           colors[c+=1] = traj.startColor[1];
           colors[c+=1] = traj.startColor[2];
         }
         
         coords[t+=1] = traj.startPts[0];
         coords[t+=1] = traj.startPts[1];
         coords[t+=1] = traj.startPts[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         	     
         coords[t+=1] = barbPtC[0];
         coords[t+=1] = barbPtC[1];
         coords[t+=1] = barbPtC[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         if (fill) {
           coords[t+=1] = ptOnPath[0];
           coords[t+=1] = ptOnPath[1];
           coords[t+=1] = ptOnPath[2];
           
           colors[c+=1] = traj.startColor[0];
           colors[c+=1] = traj.startColor[1];
           colors[c+=1] = traj.startColor[2];
         }
         
         coords[t+=1] = traj.startPts[0];
         coords[t+=1] = traj.startPts[1];
         coords[t+=1] = traj.startPts[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         coords[t+=1] = barbPtD[0];
         coords[t+=1] = barbPtD[1];
         coords[t+=1] = barbPtD[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         if (fill) {
           coords[t+=1] = ptOnPath[0];
           coords[t+=1] = ptOnPath[1];
           coords[t+=1] = ptOnPath[2];
           
           colors[c+=1] = traj.startColor[0];
           colors[c+=1] = traj.startColor[1];
           colors[c+=1] = traj.startColor[2];
         }
         
         array.vertexCount = numVerts;
         array.coordinates = coords;
         array.colors = colors;   
         arrays.add(array);
         float[] anchrPts = new float[] {traj.startPts[0], traj.startPts[1], traj.startPts[2]};
         anchors.add(anchrPts);
       }

       return array;
     }
 

     public void forward(float[][] flow_values, byte[][] color_values, GriddedSet spatial_set, int direction)
           throws VisADException {
        if (offGrid) return;

        int[][] indices = new int[1][];
        float[][] weights = new float[1][];
        float[] intrpFlow = new float[3];
        int clrDim = color_values.length;
        float[] intrpClr = new float[clrDim];

        int manifoldDimension = spatial_set.getManifoldDimension();

        indices[0] = startCell;
        weights[0] = cellWeights;

        if (indices[0] != null) {
           java.util.Arrays.fill(intrpFlow, 0f);
           java.util.Arrays.fill(intrpClr, 0);
           for (int j=0; j<indices[0].length; j++) {
              int idx = indices[0][j];
              intrpFlow[0] += weights[0][j]*(direction)*flow_values[0][idx];
              intrpFlow[1] += weights[0][j]*(direction)*flow_values[1][idx];
              intrpFlow[2] += weights[0][j]*(direction)*flow_values[2][idx];

              intrpClr[0] += weights[0][j]*color_values[0][idx];
              intrpClr[1] += weights[0][j]*color_values[1][idx];
              intrpClr[2] += weights[0][j]*color_values[2][idx];
              if (clrDim == 4) {
                intrpClr[3] += weights[0][j]*color_values[3][idx];
              }
              
              //markGrid[idx] = true;
              markGridTime[idx] = currentTimeIndex;
           }

           stopPts[0] = startPts[0] + intrpFlow[0];
           stopPts[1] = startPts[1] + intrpFlow[1];
           stopPts[2] = startPts[2] + intrpFlow[2];

           stopColor[0] = (byte) intrpClr[0];
           stopColor[1] = (byte) intrpClr[1];
           stopColor[2] = (byte) intrpClr[2];
           if (clrDim == 4) {
             stopColor[3] = (byte) intrpClr[3];
           }

           // interpolated color not working yet. Use constant color from the start
           addPair(startPts, stopPts, startColor, startColor);

           uVecPath[0] = stopPts[0] - startPts[0];
           uVecPath[1] = stopPts[1] - startPts[1];
           uVecPath[2] = stopPts[2] - startPts[2];

           float mag = (float) Math.sqrt(uVecPath[0]*uVecPath[0] + uVecPath[1]*uVecPath[1] + uVecPath[2]*uVecPath[2]);
           uVecPath[0] /= mag;
           uVecPath[1] /= mag;
           uVecPath[2] /= mag;

           startPts[0] = stopPts[0];
           startPts[1] = stopPts[1];
           startPts[2] = stopPts[2];
          

           if (manifoldDimension == 2) {
              startPts2D[0][0] = startPts[0];
              startPts2D[1][0] = startPts[1];
              spatial_set.valueToInterp(startPts2D, indices, weights);
           }
           else if (manifoldDimension == 3) {
              startPts3D[0][0] = startPts[0];
              startPts3D[1][0] = startPts[1];
              startPts3D[2][0] = startPts[2];
              spatial_set.valueToInterp(startPts3D, indices, weights);
           }
           
           startCell = indices[0];
           cellWeights = weights[0];
           if (indices[0] == null) {
              offGrid = true;
           }
        }

     }

     private void addPair(float[] startPt, float[] stopPt, byte[] startColor, byte[] stopColor) {
        coordinates[coordCnt++] = startPt[0];
        coordinates[coordCnt++] = startPt[1];
        coordinates[coordCnt++] = startPt[2];
        vertCnt++;

        coordinates[coordCnt++] =  stopPt[0];
        coordinates[coordCnt++] =  stopPt[1];
        coordinates[coordCnt++] =  stopPt[2];
        vertCnt++;

        int clrDim = startColor.length;

        colors[colorCnt++] = startColor[0];
        colors[colorCnt++] = startColor[1];
        colors[colorCnt++] = startColor[2];
        if (clrDim == 4) {
          colors[colorCnt++] = startColor[3];
        }

        colors[colorCnt++] = stopColor[0];
        colors[colorCnt++] = stopColor[1];
        colors[colorCnt++] = stopColor[2];
        if (clrDim == 4) {
          colors[colorCnt++] = stopColor[3];
        }
     }

     public static float[][] adjustFlow(FlowInfo info, float[][] flow_values, float timeStep) throws VisADException {
        return ShadowType.adjustFlowToEarth(info.which, flow_values, info.spatial_values, info.flowScale,
                               info.renderer, false, true, timeStep);
     }

     public static float[][] smooth(float[][] values0, float[][] values1, float[][] values2, TrajectoryParams.SmoothParams smoothParams) {

       float w0 = smoothParams.w0;
       float w1 = smoothParams.w1;
       float w2 = smoothParams.w2;

       int numPts = values0[0].length;
       float[][] new_values = new float[3][numPts];

       for (int k=0; k<numPts; k++) {
         new_values[0][k] = w0*values0[0][k] + w1*values1[0][k] + w2*values2[0][k];
         new_values[1][k] = w0*values0[1][k] + w1*values1[1][k] + w2*values2[1][k];
         new_values[2][k] = w0*values0[2][k] + w1*values1[2][k] + w2*values2[2][k];
       }

       return new_values;
     }

     public static double[] getTimeSteps(Gridded1DSet timeSet) throws VisADException {
        double[] timePts;
        if (timeSet instanceof Gridded1DDoubleSet) {
           timePts = (timeSet.getDoubles())[0];
        }
        else {
           timePts = (Set.floatToDouble(timeSet.getSamples()))[0];
        }

        double[] timeSteps = new double[timePts.length];
        Unit[] setUnits = timeSet.getSetUnits();
        timePts = CommonUnit.secondsSinceTheEpoch.toThis(timePts, setUnits[0]);
        for (int t=0; t<timePts.length-1; t++) { 
           timeSteps[t] = timePts[t+1]-timePts[t];
        }
        timeSteps[timePts.length-1] = timeSteps[timePts.length-2];
        return timeSteps;
     }

     public static double[] getTimes(Gridded1DSet timeSet) throws VisADException {
        double[] timePts;
        if (timeSet instanceof Gridded1DDoubleSet) {
           timePts = (timeSet.getDoubles())[0];
        }
        else {
           timePts = (Set.floatToDouble(timeSet.getSamples()))[0];
        }

        Unit[] setUnits = timeSet.getSetUnits();
        timePts = CommonUnit.secondsSinceTheEpoch.toThis(timePts, setUnits[0]);
        return timePts;
     }


     public static float[][] convertFlowUnit(float[][] values, Unit[] units) throws VisADException {

       float[] valsX = values[0];
       if (!CommonUnit.meterPerSecond.equals(units[0])) {
         valsX = CommonUnit.meterPerSecond.toThis(values[0], units[0]);
       }

       float[] valsY = values[1];
       if (!CommonUnit.meterPerSecond.equals(units[1])) {
         valsY = CommonUnit.meterPerSecond.toThis(values[1], units[1]);
       }

      /* 
       * FlowZ will have to be meters/second: Application will have to convert
       * or supply, maybe through a RangeCoordinateSystem, a transform to do this.
       *
       */

       return new float[][] {valsX, valsY, values[2]};
     }
     
     public static void updateInterpolators(ArrayList<Trajectory>trajectories, int numSpatialPts, Interpolation uInterp, Interpolation vInterp, Interpolation wInterp) {
         boolean[] needed = new boolean[numSpatialPts];
         java.util.Arrays.fill(needed, false);
         for (int k=0; k<trajectories.size(); k++) {
             Trajectory traj = trajectories.get(k);
             if (!traj.offGrid) {
                int[] cell = traj.startCell;
                for (int t=0; t<cell.length; t++) {
                   needed[cell[t]] = true;
                }
             }
         }
         uInterp.update(needed);
         vInterp.update(needed);
         wInterp.update(needed);
     }
  }
