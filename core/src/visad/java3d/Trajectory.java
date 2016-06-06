package visad.java3d;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import javax.media.j3d.BranchGroup;
import visad.CommonUnit;
import visad.Data;
import visad.FlowInfo;
import visad.Gridded1DDoubleSet;
import visad.Gridded1DSet;
import visad.Gridded2DSet;
import visad.Gridded3DSet;
import visad.GriddedSet;
import visad.RealTupleType;
import visad.Set;
import visad.ShadowType;
import visad.Unit;
import visad.VisADException;
import visad.VisADGeometryArray;
import visad.VisADLineArray;
import visad.VisADTriangleArray;
import visad.VisADTriangleStripArray;
import visad.FlowControl;
import visad.ProjectionControl;
import visad.ControlListener;
import visad.ScalarMap;
import visad.ScalarMapControlEvent;
import visad.ScalarMapEvent;
import visad.TrajectoryParams;
import visad.ScalarMapListener;
import visad.DisplayListener;
import visad.DisplayEvent;
import visad.DisplayImpl;
import visad.GraphicsModeControl;

public class Trajectory {
     /* Current location (spatial set) of massless tracer particle */
     float[] startPts = new float[3];
     
     /* grid point neighbors and interp weights for current location */
     int[] startCell;
     float[] cellWeights;
     
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
     
     int npairs = 0;
     int[] indexes = new int[60];
     float lastx = Float.NaN;
     float lasty = Float.NaN;
     float lastz = Float.NaN;
     float[] lastPtD = null;
     float[] lastPtC = null;
     float[] lastPtDD = null;
     float[] lastPtCC = null;
     static int totNpairs = 0;
     
     static float[][] circle;
     float[][] circleXYZ;
     float[][] last_circleXYZ;
     
     static int coordCnt = 0;
     static int colorCnt = 0;
     static int vertCnt = 0;

     private static float[] coordinates = null;
     private static byte[] colors = null;
     private static int clrDim = 3;
     
     public static boolean[] markGrid;
     public static int[] markGridTime;

     public static boolean doStartOffset = false;
     public static int cnt=0;
     public static int[] o_j = new int[] {0, 0, 1, 1}; 
     public static int[] o_i = new int[] {0, 1, 0, 1}; 
     
     //- Hold classes which need to persist between doTransforms:
     
     //- Listener per FlowControl for ProjectionControl events to auto resize tracer geometry.
     static HashMap<FlowControl, ControlListener> scaleChangeListeners = new HashMap<FlowControl, ControlListener>();
     
     //- Holds trajectory geometry which may or may not be reused.
     static HashMap<FlowControl, Object> trajCacheMap = new HashMap<FlowControl, Object>();
     
     //- For detecting parameter changes.
     static HashMap<FlowControl, TrajectoryParams> trajParamMap = new HashMap<FlowControl, TrajectoryParams>();
     
     // Listener for ScalarMapControlEvent.CONTROL_REMOVED to remove above classes for garbage collection.
     static HashMap<ScalarMap, ScalarMapListener> removeListeners = new HashMap<ScalarMap, ScalarMapListener>();

     
     public Trajectory(float startX, float startY, float startZ, int[] startCell, float[] cellWeights, byte[] startColor) {
        startPts[0] = startX;
        startPts[1] = startY;
        startPts[2] = startZ;
        this.startCell = startCell;
        this.cellWeights = cellWeights;

        int clrDim = startColor.length;
        stopColor = new byte[clrDim];

        this.startColor = startColor;
     }

     public static void makeTrajectories(double time, ArrayList<Trajectory> trajectories, float[][] startPts, byte[][] color_values, GriddedSet spatial_set) throws VisADException  {
        int num = startPts[0].length;
        clrDim = color_values.length;
        
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
           
           if (indices[k] != null) {
             Trajectory traj = new Trajectory(startX, startY, startZ, indices[k], weights[k], startColor);
             traj.initialTime = time;
             trajectories.add(traj);
           }
        }
     }
     
     public static GriddedSet makeSpatialSetTraj(Gridded3DSet spatial_set) throws VisADException {
       int manifoldDim = spatial_set.getManifoldDimension();
       int[] lens = spatial_set.getLengths();
       float[][] setLocs = spatial_set.getSamples(false);
       GriddedSet spatialSetTraj;
       if (manifoldDim == 2) {
         spatialSetTraj = new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple,
                new float[][] {setLocs[0], setLocs[1]}, lens[0], lens[1]);
       } else {
         spatialSetTraj = spatial_set;
       }
       return spatialSetTraj;
     }
     
     public static void getStartPointsFromDomain(int trajForm, int skip, int zstart, int zskip, Gridded3DSet spatial_set, byte[][] color_values, float[][] startPts, byte[][] startClrs, float[][] flowValues, float ribbonWidthFac) throws VisADException {
         int manifoldDim = spatial_set.getManifoldDimension();
         int[] lens = spatial_set.getLengths();
         int lenX = lens[0];
         int lenY = lens[1];
         int lenZ;
         if (manifoldDim == 3) {
             lenZ = lens[2];
             if (zskip <= 0) {
               zskip = 1;
             }
             getStartPointsFromDomain3D(trajForm, skip, zstart, zskip, spatial_set.getSamples(false), lenX, lenY, lenZ, color_values, startPts, startClrs, flowValues, ribbonWidthFac);
         }
         else if (manifoldDim == 2) {
             getStartPointsFromDomain2D(trajForm, skip, spatial_set.getSamples(false), lenX, lenY, color_values, startPts, startClrs, flowValues, ribbonWidthFac);
         }
     }
     
     public static void getStartPointsFromDomain3D(int trajForm, int skip, int zstart, int skipZ, float[][] locs, int lenX, int lenY, int lenZ, byte[][] color_values, float[][] startPts, byte[][] startClrs, float[][] flowValues, float ribbonWidthFac) throws VisADException {
         int len2D = lenX*lenY;
         
         float[][] locs2D = new float[3][len2D];
         float[][] pts = new float[3][];
         
         int clrDim = startClrs.length;
         byte[][] clrs2D = new byte[clrDim][len2D];
         byte[][] clrs = new byte[clrDim][];
         
         int lenA = 0;
         
         for (int k=zstart; k<lenZ; k+=skipZ) {
             System.arraycopy(locs[0], k*len2D, locs2D[0], 0, len2D);
             System.arraycopy(locs[1], k*len2D, locs2D[1], 0, len2D);
             System.arraycopy(locs[2], k*len2D, locs2D[2], 0, len2D);
             
             System.arraycopy(color_values[0], k*len2D, clrs2D[0], 0, len2D);
             System.arraycopy(color_values[1], k*len2D, clrs2D[1], 0, len2D);
             System.arraycopy(color_values[2], k*len2D, clrs2D[2], 0, len2D);
             if (clrDim == 4) {
               System.arraycopy(color_values[3], k*len2D, clrs2D[3], 0, len2D);                
             }
             
             getStartPointsFromDomain2D(trajForm, skip, locs2D, lenX, lenY, clrs2D, pts, clrs, flowValues, ribbonWidthFac);
             
             int lenB = pts[0].length;
             float[][] tmpPts = new float[3][lenA+lenB];
             byte[][] tmpClrs = new byte[clrDim][lenA+lenB];
 
             if  (lenA > 0) {
                System.arraycopy(startPts[0], 0, tmpPts[0], 0, lenA);   
                System.arraycopy(startPts[1], 0, tmpPts[1], 0, lenA);
                System.arraycopy(startPts[2], 0, tmpPts[2], 0, lenA);     
                System.arraycopy(startClrs[0], 0, tmpClrs[0], 0, lenA);
                System.arraycopy(startClrs[1], 0, tmpClrs[1], 0, lenA);
                System.arraycopy(startClrs[2], 0, tmpClrs[2], 0, lenA);    
                if (clrDim == 4) {
                   System.arraycopy(startClrs[3], 0, tmpClrs[3], 0, lenA);                  
                }
             }
             System.arraycopy(pts[0], 0, tmpPts[0], lenA, lenB);
             System.arraycopy(pts[1], 0, tmpPts[1], lenA, lenB);            
             System.arraycopy(pts[2], 0, tmpPts[2], lenA, lenB);
             
             System.arraycopy(clrs[0], 0, tmpClrs[0], lenA, lenB);
             System.arraycopy(clrs[1], 0, tmpClrs[1], lenA, lenB);
             System.arraycopy(clrs[2], 0, tmpClrs[2], lenA, lenB);       
             if (clrDim == 4) {
                System.arraycopy(clrs[3], 0, tmpClrs[3], lenA, lenB);
             }
             
             startPts[0] = tmpPts[0];
             startPts[1] = tmpPts[1];
             startPts[2] = tmpPts[2];
             
             startClrs[0] = tmpClrs[0];
             startClrs[1] = tmpClrs[1];
             startClrs[2] = tmpClrs[2];
             if (clrDim == 4) {
                startClrs[3] = tmpClrs[3];
             }
             
             lenA = startPts[0].length;
         }
     }

     public static void getStartPointsFromDomain2D(int trajForm, int skip, float[][] setLocs, int lenX, int lenY, byte[][] color_values, float[][] startPts, byte[][] startClrs, float[][] flowValues, float ribbonWidthFac) throws VisADException {
        int clrDim = color_values.length;
        int m = 0;
        if (doStartOffset) {
          m = cnt % 4;
          cnt++;
        }

        int jA = 1+o_j[m]*(skip/2);
        int jB = lenY-skip;
        int iA = 1+o_i[m]*(skip/2);
        int iB = lenX-skip;

        int numJ = 1 + ((jB-1)-jA)/skip;
        int numI = 1 + ((iB-1)-iA)/skip;
        int num = numJ*numI;
  
        if (trajForm == TrajectoryParams.DEFORM_RIBBON) {
          num *= 2;
        }

        startPts[0] = new float[num];
        startPts[1] = new float[num];
        startPts[2] = new float[num];

        startClrs[0] = new byte[num];
        startClrs[1] = new byte[num];
        startClrs[2] = new byte[num];
        if (clrDim == 4) {
           startClrs[3] = new byte[num];
        }
        
        float[] norm = new float[] {0f, 0f, 1f};
        float[] traj = new float[3];
        float width = ribbonWidthFac*0.006f;

        num = 0;
        for (int j=1+o_j[m]*(skip/2); j<lenY-skip; j+=skip) {
          for (int i=1+o_i[m]*(skip/2); i<lenX-skip; i+=skip) {

            int k = j*lenX + i;

            
            if (trajForm == TrajectoryParams.DEFORM_RIBBON) {         
              float u = flowValues[0][k];
              float v = flowValues[1][k];

              traj[0] = u;
              traj[1] = v;
              traj[2] = 0f;
              float mag = (float) Math.sqrt(u*u+v*v);
              traj[0] /= mag;
              traj[1] /= mag;
              float[] norm_x_traj = AxB(norm, traj);

              if (!markGrid[k]) {
                startPts[0][num] = width*norm_x_traj[0] + setLocs[0][k];
                startPts[1][num] = width*norm_x_traj[1] + setLocs[1][k];
                startPts[2][num] = width*norm_x_traj[2] + setLocs[2][k];

                startClrs[0][num] = color_values[0][k];
                startClrs[1][num] = color_values[1][k];
                startClrs[2][num] = color_values[2][k];
                if (clrDim == 4) {
                  startClrs[3][num] = color_values[3][k];
                }
                num++;
                
                startPts[0][num] = -width*norm_x_traj[0] + setLocs[0][k];
                startPts[1][num] = -width*norm_x_traj[1] + setLocs[1][k];
                startPts[2][num] = -width*norm_x_traj[2] + setLocs[2][k];

                startClrs[0][num] = color_values[0][k];
                startClrs[1][num] = color_values[1][k];
                startClrs[2][num] = color_values[2][k];
                if (clrDim == 4) {
                  startClrs[3][num] = color_values[3][k];
                }
                num++;
              }
            }
            else {
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
                 num++;
               }
            }
            
          }
        }

        /* For animated Streamllines TODO
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

              /* For animated streamlines
              Trajectory traj = new Trajectory(startX, startY, startZ, startColor);
              traj.initialTime = time;
              trajectories.add(traj);
              */
            }

          }
        }

        /* For animated streamlines
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
     
     public static ArrayList<Trajectory> clean(int trajType, ArrayList<Trajectory> trajectories, double threshold) {
        ArrayList<Trajectory> newList = null;
        switch (trajType) {
           case 0:
              newList = clean(trajectories, threshold);
              break;
           case 1:
              newList = clean(trajectories, threshold);
              break;
           case 2:
              newList = clean(trajectories, threshold);
              break;
           case 3:
              newList = cleanDefStrp(trajectories, threshold);
              break;
        }
        return newList;
     }
     
     public static ArrayList<Trajectory> cleanDefStrp(ArrayList<Trajectory> trajectories, double threshold) {
       Iterator<Trajectory> iter = trajectories.iterator();
       ArrayList<Trajectory>  removeList = new ArrayList<Trajectory>();
       
       while (iter.hasNext() ) {
         Trajectory traj = iter.next();
         if (traj.offGrid || ((traj.currentTime - traj.initialTime) > threshold)) {
           int idxA;
           int idxB;
           int idx = trajectories.indexOf(traj);
           if ((idx % 2) == 0) {
              idxA = idx;
              idxB = idx+1;
           }
           else {
              idxB = idx;
              idxA = idx-1;
           }
           removeList.add(trajectories.get(idxA));
           removeList.add(trajectories.get(idxB));
         }
       }
       for (int t=0; t<removeList.size(); t++) {
          trajectories.remove(removeList.get(t));
       }
       
       return trajectories;
     }

     public static ArrayList<Trajectory> clean(ArrayList<Trajectory> trajectories) {
        return Trajectory.clean(trajectories, -1.0);
     }

     /* Set internal counters to zero. Replace internal arrays and initialize to NaN. */
     public static void reset(int maxNumVerts, int clrDim) {
        coordCnt = 0; 
        colorCnt = 0;
        vertCnt = 0;
        totNpairs = 0;
        maxNumVerts *= 2; // one each for start and stop
        
        if (coordinates == null || coordinates.length != 3*maxNumVerts) {
          coordinates = new float[3*maxNumVerts];
        }
        if (colors == null || colors.length != clrDim*maxNumVerts) {
          colors = new byte[clrDim*maxNumVerts];
        }
        java.util.Arrays.fill(coordinates, Float.NaN);
        java.util.Arrays.fill(colors, (byte)0);
     }
     
     public static void reset(ArrayList<Trajectory> trajectories, int maxNumVerts, int clrDim) {
        reset(maxNumVerts, clrDim);
        
        /* reset instance pair counter to zero */
        for (int k=0; k<trajectories.size(); k++) {
            Trajectory traj = trajectories.get(k);
            traj.npairs = 0;
        }
     }

     public static VisADGeometryArray makeGeometry() {
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
     
     public static VisADGeometryArray makeCylinder(ArrayList<Trajectory> trajectories, VisADGeometryArray[] auxArray, float cylWidth) {
        VisADTriangleStripArray array = new VisADTriangleStripArray();
        VisADTriangleArray coneArray = new VisADTriangleArray();
        
        int ntrajs = trajectories.size();
        
        int numSides = 20;
        
        int numv = totNpairs*(numSides+1)*2;
        
        float[] coords = new float[numv*3];
        byte[] newColors = new byte[numv*clrDim];
        float[] normals = new float[numv*3];
        int[] strips = new int[totNpairs];
        
        float[] coneCoords = new float[ntrajs*(numSides+1)*3*3];
        byte[] coneColors = new byte[ntrajs*(numSides+1)*3*clrDim];
        float[] coneNormals = new float[ntrajs*(numSides+1)*3*3];
        
        float[] uvecPath = new float[3];
        float[] norm = new float[] {0f, 0f, 1f};
        byte[][] clr0 = new byte[clrDim][1];
        byte[][] clr1 = new byte[clrDim][1];
        float[] pt0 = new float[3];
        float[] pt1 = new float[3];
        float[][] basePts = new float[3][numSides+1];
        
        
        int[] idx = new int[] {0};
        int strpCnt = 0;
        int[] coneIdx = new int[] {0};
        byte r0,g0,b0,r1,g1,b1;
        byte a0 = -1;
        byte a1 = -1;
        
        for (int t=0; t<ntrajs; t++) {
          Trajectory traj = trajectories.get(t);
          for (int k=0; k<traj.npairs; k++) {
        
            int i = traj.indexes[k];
            int ci = 2*clrDim*i/6;
            
            float x0 = coordinates[i];
            float y0 = coordinates[i+1];
            float z0 = coordinates[i+2];
            float x1 = coordinates[i+3];
            float y1 = coordinates[i+4];
            float z1 = coordinates[i+5];
            
            if (clrDim == 3) {
               r0 = colors[ci];
               g0 = colors[ci+1];
               b0 = colors[ci+2];
               r1 = colors[ci+3];
               g1 = colors[ci+4];
               b1 = colors[ci+5];
            }
            else {
               r0 = colors[ci];
               g0 = colors[ci+1];
               b0 = colors[ci+2];
               a0 = colors[ci+3];
               r1 = colors[ci+4];
               g1 = colors[ci+5];
               b1 = colors[ci+6];
               a1 = colors[ci+7];
            }
            
            float mag = (x1-x0)*(x1-x0) + (y1-y0)*(y1-y0) + (z1-z0)*(z1-z0);
            mag = (float) Math.sqrt(mag);
            uvecPath[0] = (x1-x0)/mag;
            uvecPath[1] = (y1-y0)/mag;
            uvecPath[2] = (z1-z0)/mag;
            
            float[] norm_x_trj = AxB(norm, uvecPath);
            
            float[] trj_x_norm_x_trj = AxB(uvecPath, norm_x_trj);
            
            pt0[0] = x0;
            pt0[1] = y0;
            pt0[2] = z0;
            pt1[0] = x1;
            pt1[1] = y1;
            pt1[2] = z1;   
            
            
            clr0[0][0] = r0;
            clr0[1][0] = g0;
            clr0[2][0] = b0;
            if (clrDim == 4) clr0[3][0] = a0;
            clr1[0][0] = r1;
            clr1[1][0] = g1;
            clr1[2][0] = b1;
            if (clrDim == 4) clr1[3][0] = a1;        
            
            traj.makeCylinderStrip(trj_x_norm_x_trj, norm_x_trj, pt0, pt1, clr0, clr1, cylWidth, (numSides+1), coords, newColors, normals, idx);
            strips[strpCnt++] = (numSides+1)*2;
          }
          
          float[] vertex = new float[3];
          vertex[0] = pt1[0] + uvecPath[0]*0.006f;
          vertex[1] = pt1[1] + uvecPath[1]*0.006f;
          vertex[2] = pt1[2] + uvecPath[2]*0.006f;
          
          // build cone here. add to coneArray
          makeCone(traj.last_circleXYZ, vertex, clr0, coneCoords, coneColors, coneNormals, coneIdx);
        }
        
        array.coordinates = coords;
        array.normals = normals;
        array.colors = newColors;
        array.vertexCount = idx[0];
        array.stripVertexCounts = strips;
        
        coneArray.coordinates = coneCoords;
        coneArray.normals = coneNormals;
        coneArray.colors = coneColors;
        coneArray.vertexCount = coneIdx[0];
        
        auxArray[0] = coneArray;
        
        return array; 
     }
     
     public static void makeCone(float[][] basePts, float[] vertex, byte[][] color, float[] coords, byte[] colors, float[] normals, int[] vertCnt) {
       int nPts = basePts[0].length;
       
       float[] ptA = new float[3];
       float[] ptB = new float[3];
       float[] AV = new float[3];
       float[] BV = new float[3];
       
       int vcnt = vertCnt[0];
       int idx = 3*vcnt; 
       int cidx = clrDim*vcnt;
       
       for (int k=0; k<nPts; k++) {
         // A--vertex--B
         int ia = k;
         int ib = (k==(nPts-1)) ? 0 : (k+1);
         
         ptA[0] = basePts[0][ia];
         ptA[1] = basePts[1][ia];
         ptA[2] = basePts[2][ia];
         
         ptB[0] = basePts[0][ib];
         ptB[1] = basePts[1][ib];
         ptB[2] = basePts[2][ib];
         
         AV[0] = ptA[0] - vertex[0];
         AV[1] = ptA[1] - vertex[1];
         AV[2] = ptA[2] - vertex[2];
         BV[0] = ptB[0] - vertex[0];
         BV[1] = ptB[1] - vertex[1];
         BV[2] = ptB[2] - vertex[2];
         
         float[] norm = AxB(AV, BV);
         float mag = (float) Math.sqrt(norm[0]*norm[0]+norm[1]*norm[1]+norm[2]*norm[2]);
         norm[0] /= mag;
         norm[1] /= mag;
         norm[2] /= mag;
         
         
         normals[idx] = norm[0];
         coords[idx++] = ptA[0];  
         normals[idx] = norm[1];
         coords[idx++] = ptA[1];  
         normals[idx] = norm[2];
         coords[idx++] = ptA[2];  
         colors[cidx++] = color[0][0];
         colors[cidx++] = color[1][0];
         colors[cidx++] = color[2][0];     
         if (clrDim == 4) colors[cidx++] = color[3][0];
         vcnt++;
         
         normals[idx] = norm[0];
         coords[idx++] = ptB[0];
         normals[idx] = norm[1];
         coords[idx++] = ptB[1];  
         normals[idx] = norm[2];
         coords[idx++] = ptB[2];  
         colors[cidx++] = color[0][0];
         colors[cidx++] = color[1][0];
         colors[cidx++] = color[2][0];      
         if (clrDim == 4) colors[cidx++] = color[3][0];
         vcnt++;   
         
         normals[idx] = norm[0];
         coords[idx++] = vertex[0]; 
         normals[idx] = norm[1];
         coords[idx++] = vertex[1];
         normals[idx] = norm[2];
         coords[idx++] = vertex[2];  
         colors[cidx++] = color[0][0];
         colors[cidx++] = color[1][0];
         colors[cidx++] = color[2][0];  
         if (clrDim == 4) colors[cidx++] = color[3][0];
         vcnt++; 
       }
       vertCnt[0] = vcnt;
     }
     
     public static VisADGeometryArray makeDeformableRibbon(ArrayList<Trajectory> trajectories) {
       VisADTriangleArray array = new VisADTriangleArray();
       
       int ntrajs = trajectories.size();
       int ntris = (totNpairs/2)*2;
       int num = ntris*3;
       
       float[] newCoords = new float[num*3];
       java.util.Arrays.fill(newCoords, Float.NaN);
       byte[] newColors = new byte[num*clrDim];
       float[] newNormals = new float[num*3];
       
       float[] A0 = new float[3];
       float[] A1 = new float[3];
       float[] B0 = new float[3];
       float[] B1 = new float[3];
       
       byte ar0,ag0,ab0,ar1,ag1,ab1,br0,bg0,bb0,br1,bg1,bb1;
       byte aa0 = -1;
       byte aa1 = -1;
       byte ba0 = -1;
       byte ba1 = -1;
       
       int numVerts = 0;
       
       for (int k=0; k<ntrajs/2; k++) {
          int t = k*2;
          Trajectory trajA = trajectories.get(t);
          Trajectory trajB = trajectories.get(t+1);
          
          int npairs = Math.min(trajA.npairs, trajB.npairs);
          
          for (int n=0; n<npairs; n++) {
             int ia = trajA.indexes[n];
             int ib = trajB.indexes[n];
             int cia = 2*clrDim*ia/6;
             int cib = 2*clrDim*ib/6;
             
             A0[0] = coordinates[ia];
             A0[1] = coordinates[ia+1];
             A0[2] = coordinates[ia+2];
             A1[0] = coordinates[ia+3];
             A1[1] = coordinates[ia+4];
             A1[2] = coordinates[ia+5];
             
             B0[0] = coordinates[ib];
             B0[1] = coordinates[ib+1];
             B0[2] = coordinates[ib+2];
             B1[0] = coordinates[ib+3];
             B1[1] = coordinates[ib+4];
             B1[2] = coordinates[ib+5];            
             
             if (clrDim == 3) {
                ar0 = colors[cia];
                ag0 = colors[cia+1];
                ab0 = colors[cia+2];
                ar1 = colors[cia+3];
                ag1 = colors[cia+4];
                ab1 = colors[cia+5];
             }
             else {
                ar0 = colors[cia];
                ag0 = colors[cia+1];
                ab0 = colors[cia+2];
                aa0 = colors[cia+3];
                ar1 = colors[cia+4];
                ag1 = colors[cia+5];
                ab1 = colors[cia+6];   
                aa1 = colors[cia+7];
             }
             
             if (clrDim == 3) {
                br0 = colors[cib];
                bg0 = colors[cib+1];
                bb0 = colors[cib+2];
                br1 = colors[cib+3];
                bg1 = colors[cib+4];
                bb1 = colors[cib+5];  
             }
             else {
                br0 = colors[cib];
                bg0 = colors[cib+1];
                bb0 = colors[cib+2];
                ba0 = colors[cib+3];
                br1 = colors[cib+4];
                bg1 = colors[cib+5];
                bb1 = colors[cib+6];
                ba1 = colors[cib+7];
             }
             
             int idx = numVerts*3;
             int cidx = numVerts*clrDim;
             
             newCoords[idx] = A0[0];
             newCoords[idx+1] = A0[1];
             newCoords[idx+2] = A0[2];
             newColors[cidx] = ar0;
             newColors[cidx+1] = ag0;
             newColors[cidx+2] = ab0;
             if (clrDim == 4) newColors[cidx+3] = aa0;
             numVerts++;

             idx = numVerts*3;
             cidx = numVerts*clrDim;
             newCoords[idx] = B0[0];
             newCoords[idx+1] = B0[1];
             newCoords[idx+2] = B0[2];
             newColors[cidx] = br0;
             newColors[cidx+1] = bg0;
             newColors[cidx+2] = bb0;   
             if (clrDim == 4) newColors[cidx+3] = ba0;             
             numVerts++; 
             
             idx = numVerts*3;
             cidx = numVerts*clrDim;
             newCoords[idx] = B1[0];
             newCoords[idx+1] = B1[1];
             newCoords[idx+2] = B1[2];
             newColors[cidx] = br1;
             newColors[cidx+1] = bg1;
             newColors[cidx+2] = bb1;  
             if (clrDim == 4) newColors[cidx+3] = ba1;             
             numVerts++;  
             
             idx = numVerts*3;
             cidx = numVerts*clrDim;
             newCoords[idx] = B1[0];
             newCoords[idx+1] = B1[1];
             newCoords[idx+2] = B1[2];
             newColors[cidx] = br1;
             newColors[cidx+1] = bg1;
             newColors[cidx+2] = bb1;    
             if (clrDim == 4) newColors[cidx+3] = ba1;             
             numVerts++;

             idx = numVerts*3;
             cidx = numVerts*clrDim;
             newCoords[idx] = A1[0];
             newCoords[idx+1] = A1[1];
             newCoords[idx+2] = A1[2];
             newColors[cidx] = ar1;
             newColors[cidx+1] = ag1;
             newColors[cidx+2] = ab1;   
             if (clrDim == 4) newColors[cidx+3] = aa1;             
             numVerts++; 
             
             idx = numVerts*3;
             cidx = numVerts*clrDim;
             newCoords[idx] = A0[0];
             newCoords[idx+1] = A0[1];
             newCoords[idx+2] = A0[2];
             newColors[cidx] = ar0;
             newColors[cidx+1] = ag0;
             newColors[cidx+2] = ab0; 
             if (clrDim == 4) newColors[cidx+3] = aa0;             
             numVerts++;                       
          }
          
       }
       // remove missing:
       float[] coords = new float[numVerts*3];
       byte[] colors = new byte[numVerts*clrDim];
       System.arraycopy(newCoords, 0, coords, 0, coords.length);
       System.arraycopy(newColors, 0, colors, 0, colors.length);
       
       array.coordinates = coords;
       array.colors = colors;       
       array.vertexCount = numVerts;
       
       return array;
     }
     
     public static VisADGeometryArray makeDeformableRibbonNew(ArrayList<Trajectory> trajectories) {
       VisADTriangleArray array = new VisADTriangleArray();
       
       int ntrajs = trajectories.size();
       int ntris = (totNpairs/3)*2;
       int num = ntris*3;
       
       float[] newCoords = new float[num*3];
       java.util.Arrays.fill(newCoords, Float.NaN);
       byte[] newColors = new byte[num*clrDim];
       float[] newNormals = new float[num*3];      
       
       float[] A0 = new float[3];
       float[] A1 = new float[3];
       
       float[] B0 = new float[3];
       float[] B1 = new float[3];
       
       float[] C0 = new float[3];
       float[] C1 = new float[3];
       
       // colors
       byte ar0,ag0,ab0,ar1,ag1,ab1,br0,bg0,bb0,br1,bg1,bb1,cr0,cg0,cb0,cr1,cg1,cb1;
       byte aa0 = -1;
       byte aa1 = -1;
       byte ba0 = -1;
       byte ba1 = -1;
       byte ca0 = -1;
       byte ca1 = -1;

       int numVerts = 0;
       
       for (int k=0; k<ntrajs/3; k++) {
          int t = k*3;
          Trajectory trajA = trajectories.get(t);
          Trajectory trajB = trajectories.get(t+1);
          Trajectory trajC = trajectories.get(t+2);
          
          int npairs = Math.min(trajA.npairs, trajB.npairs);
          npairs = Math.min(npairs, trajC.npairs);
          
          for (int n=0; n<npairs; n++) {
             int ia = trajA.indexes[n];
             int ib = trajB.indexes[n];
             int ic = trajC.indexes[n];
             
             int cia = 2*clrDim*ia/6;
             int cib = 2*clrDim*ib/6;
             int cic = 2*clrDim*ic/6;
             
             A0[0] = coordinates[ia];
             A0[1] = coordinates[ia+1];
             A0[2] = coordinates[ia+2];
             A1[0] = coordinates[ia+3];
             A1[1] = coordinates[ia+4];
             A1[2] = coordinates[ia+5];
             
             B0[0] = coordinates[ib];
             B0[1] = coordinates[ib+1];
             B0[2] = coordinates[ib+2];
             B1[0] = coordinates[ib+3];
             B1[1] = coordinates[ib+4];
             B1[2] = coordinates[ib+5];
             
             C0[0] = coordinates[ic];
             C0[1] = coordinates[ic+1];
             C0[2] = coordinates[ic+2];
             B1[0] = coordinates[ic+3];
             B1[1] = coordinates[ic+4];
             B1[2] = coordinates[ic+5];            
             
             if (clrDim == 3) {
                ar0 = colors[cia];
                ag0 = colors[cia+1];
                ab0 = colors[cia+2];
                ar1 = colors[cia+3];
                ag1 = colors[cia+4];
                ab1 = colors[cia+5];
             }
             else {
                ar0 = colors[cia];
                ag0 = colors[cia+1];
                ab0 = colors[cia+2];
                aa0 = colors[cia+3];
                ar1 = colors[cia+4];
                ag1 = colors[cia+5];
                ab1 = colors[cia+6];   
                aa1 = colors[cia+7];
             }
             
             if (clrDim == 3) {
                br0 = colors[cib];
                bg0 = colors[cib+1];
                bb0 = colors[cib+2];
                br1 = colors[cib+3];
                bg1 = colors[cib+4];
                bb1 = colors[cib+5];  
             }
             else {
                br0 = colors[cib];
                bg0 = colors[cib+1];
                bb0 = colors[cib+2];
                ba0 = colors[cib+3];
                br1 = colors[cib+4];
                bg1 = colors[cib+5];
                bb1 = colors[cib+6];
                ba1 = colors[cib+7];
             }            
             
             if (clrDim == 3) {
                cr0 = colors[cib];
                cg0 = colors[cib+1];
                cb0 = colors[cib+2];
                cr1 = colors[cib+3];
                cg1 = colors[cib+4];
                cb1 = colors[cib+5];  
             }
             else {
                cr0 = colors[cic];
                cg0 = colors[cic+1];
                cb0 = colors[cic+2];
                ca0 = colors[cic+3];
                cr1 = colors[cic+4];
                cg1 = colors[cic+5];
                cb1 = colors[cic+6];
                ca1 = colors[cic+7];
             } 
             
             int idx = numVerts*3;
             int cidx = numVerts*clrDim;            
             
             
             
             
             
             
          }
       }
       return array;
     }
     
     public static VisADGeometryArray makeFixedWidthRibbon(ArrayList<Trajectory> trajectories, float widthFac) {
        VisADTriangleArray array = new VisADTriangleArray();
        
        int ntrajs = trajectories.size();
        
        int num = totNpairs*6;
        
        float[] newCoords = new float[num*3*2];
        byte[] newColors = new byte[num*clrDim*2];
        float[] newNormals = new float[num*3*2];
        
        float[] uvecPath = new float[3];
        float[] ptA = new float[3];
        float[] ptB = new float[3];
        float[] ptC = new float[3];
        float[] ptD = new float[3];
        float[] ptAA = new float[3];
        float[] ptBB = new float[3];
        float[] ptCC = new float[3];
        float[] ptDD = new float[3];
        float[] norm = new float[] {0f, 0f, 1f};
        
        
        int numVert=0;
        byte r0,g0,b0,r1,g1,b1;
        byte a0 = -1;
        byte a1 = -1;
        
        float width = widthFac*0.006f;
        
        for (int t=0; t<ntrajs; t++) {
          Trajectory traj = trajectories.get(t);
          for (int k=0; k<traj.npairs; k++) {
        
            int i = traj.indexes[k];
            int ci = 2*clrDim*i/6;
            
            float x0 = coordinates[i];
            float y0 = coordinates[i+1];
            float z0 = coordinates[i+2];
            float x1 = coordinates[i+3];
            float y1 = coordinates[i+4];
            float z1 = coordinates[i+5];
            
            if (clrDim == 3) {
              r0 = colors[ci];
              g0 = colors[ci+1];
              b0 = colors[ci+2];
              r1 = colors[ci+3];
              g1 = colors[ci+4];
              b1 = colors[ci+5];
            }
            else {
              r0 = colors[ci];
              g0 = colors[ci+1];
              b0 = colors[ci+2];
              a0 = colors[ci+3];
              r1 = colors[ci+4];
              g1 = colors[ci+5];
              b1 = colors[ci+6];    
              a1 = colors[ci+7];
            }
            
            float mag = (x1-x0)*(x1-x0) + (y1-y0)*(y1-y0) + (z1-z0)*(z1-z0);
            mag = (float) Math.sqrt(mag);
            uvecPath[0] = (x1-x0)/mag;
            uvecPath[1] = (y1-y0)/mag;
            uvecPath[2] = (z1-z0)/mag;
            
            float[] norm_x_trj = AxB(norm, uvecPath);
            
            float[] trj_x_norm_x_trj = AxB(uvecPath, norm_x_trj);
            
            // fixed width ribbon. Horz: A,B,C,D Vert: AA,BB,CC,DD ----------------------
            if (k==0) {
              if (traj.lastPtC == null) {
                ptA[0] = width*norm_x_trj[0] + x0;
                ptA[1] = width*norm_x_trj[1] + y0;
                ptA[2] = width*norm_x_trj[2] + z0;
                ptB[0] = -width*norm_x_trj[0] + x0;
                ptB[1] = -width*norm_x_trj[1] + y0;
                ptB[2] = -width*norm_x_trj[2] + z0;
                
                ptAA[0] = width*trj_x_norm_x_trj[0] + x0;
                ptAA[1] = width*trj_x_norm_x_trj[1] + y0;
                ptAA[2] = width*trj_x_norm_x_trj[2] + z0;
                ptBB[0] = -width*trj_x_norm_x_trj[0] + x0;
                ptBB[1] = -width*trj_x_norm_x_trj[1] + y0;
                ptBB[2] = -width*trj_x_norm_x_trj[2] + z0;
              }
              else {
                ptA[0] = traj.lastPtD[0];
                ptA[1] = traj.lastPtD[1];
                ptA[2] = traj.lastPtD[2];
                ptB[0] = traj.lastPtC[0];
                ptB[1] = traj.lastPtC[1];
                ptB[2] = traj.lastPtC[2];
                
                ptAA[0] = traj.lastPtDD[0];
                ptAA[1] = traj.lastPtDD[1];
                ptAA[2] = traj.lastPtDD[2];
                ptBB[0] = traj.lastPtCC[0];
                ptBB[1] = traj.lastPtCC[1];
                ptBB[2] = traj.lastPtCC[2];
              }
            }
            else {
              ptA[0] = ptD[0];
              ptA[1] = ptD[1];
              ptA[2] = ptD[2];
              ptB[0] = ptC[0];
              ptB[1] = ptC[1];
              ptB[2] = ptC[2];
              
              ptAA[0] = ptDD[0];
              ptAA[1] = ptDD[1];
              ptAA[2] = ptDD[2];
              ptBB[0] = ptCC[0];
              ptBB[1] = ptCC[1];
              ptBB[2] = ptCC[2];
            }
                 
            
            ptD[0] = width*norm_x_trj[0] + x1;
            ptD[1] = width*norm_x_trj[1] + y1;
            ptD[2] = width*norm_x_trj[2] + z1;
            ptC[0] = -width*norm_x_trj[0] + x1;
            ptC[1] = -width*norm_x_trj[1] + y1;
            ptC[2] = -width*norm_x_trj[2] + z1;
            
            ptDD[0] = width*trj_x_norm_x_trj[0] + x1;
            ptDD[1] = width*trj_x_norm_x_trj[1] + y1;
            ptDD[2] = width*trj_x_norm_x_trj[2] + z1;
            ptCC[0] = -width*trj_x_norm_x_trj[0] + x1;
            ptCC[1] = -width*trj_x_norm_x_trj[1] + y1;
            ptCC[2] = -width*trj_x_norm_x_trj[2] + z1;
            
            if (traj.lastPtD == null) {
              traj.lastPtC = new float[] {ptC[0], ptC[1], ptC[2]}; 
              traj.lastPtD = new float[] {ptD[0], ptD[1], ptD[2]}; 
              traj.lastPtCC = new float[] {ptCC[0], ptCC[1], ptCC[2]}; 
              traj.lastPtDD = new float[] {ptDD[0], ptDD[1], ptDD[2]}; 
            }
            else {
              traj.lastPtC[0] = ptC[0];
              traj.lastPtC[1] = ptC[1];
              traj.lastPtC[2] = ptC[2];
              traj.lastPtD[0] = ptD[0];
              traj.lastPtD[1] = ptD[1];
              traj.lastPtD[2] = ptD[2];
              
              traj.lastPtCC[0] = ptCC[0];
              traj.lastPtCC[1] = ptCC[1];
              traj.lastPtCC[2] = ptCC[2];
              traj.lastPtDD[0] = ptDD[0];
              traj.lastPtDD[1] = ptDD[1];
              traj.lastPtDD[2] = ptDD[2];
            }
            
            int idx = numVert*3;
            int cidx = numVert*clrDim;
            newCoords[idx] = ptA[0];
            newCoords[idx+1] = ptA[1];
            newCoords[idx+2] = ptA[2];
            newColors[cidx] = r0;
            newColors[cidx+1] = g0;
            newColors[cidx+2] = b0;
            if (clrDim == 4) newColors[cidx+3] = a0;
            newNormals[idx] = 0f;
            newNormals[idx+1] = 0f;
            newNormals[idx+2] = 1f;
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptB[0];
            newCoords[idx+1] = ptB[1];
            newCoords[idx+2] = ptB[2];
            newColors[cidx] = r0;
            newColors[cidx+1] = g0;
            newColors[cidx+2] = b0;  
            if (clrDim == 4) newColors[cidx+3] = a0;            
            newNormals[idx] = 0f;
            newNormals[idx+1] = 0f;
            newNormals[idx+2] = 1f;
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptC[0];
            newCoords[idx+1] = ptC[1];
            newCoords[idx+2] = ptC[2];
            newColors[cidx] = r1;
            newColors[cidx+1] = g1;
            newColors[cidx+2] = b1;  
            if (clrDim == 4) newColors[cidx+3] = a1;            
            newNormals[idx] = 0f;
            newNormals[idx+1] = 0f;
            newNormals[idx+2] = 1f;
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptAA[0];
            newCoords[idx+1] = ptAA[1];
            newCoords[idx+2] = ptAA[2];
            newColors[cidx] = r0;
            newColors[cidx+1] = g0;
            newColors[cidx+2] = b0;
            if (clrDim == 4) newColors[cidx+3] = a0;            
            newNormals[idx] = norm_x_trj[0];
            newNormals[idx+1] = norm_x_trj[1];
            newNormals[idx+2] = norm_x_trj[2];
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptBB[0];
            newCoords[idx+1] = ptBB[1];
            newCoords[idx+2] = ptBB[2];
            newColors[cidx] = r0;
            newColors[cidx+1] = g0;
            newColors[cidx+2] = b0;   
            if (clrDim == 4) newColors[cidx+3] = a0;            
            newNormals[idx] = norm_x_trj[0];
            newNormals[idx+1] = norm_x_trj[1];
            newNormals[idx+2] = norm_x_trj[2];
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptCC[0];
            newCoords[idx+1] = ptCC[1];
            newCoords[idx+2] = ptCC[2];
            newColors[cidx] = r1;
            newColors[cidx+1] = g1;
            newColors[cidx+2] = b1; 
            if (clrDim == 4) newColors[cidx+3] = a1;            
            newNormals[idx] = norm_x_trj[0];
            newNormals[idx+1] = norm_x_trj[1];
            newNormals[idx+2] = norm_x_trj[2];
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptC[0];
            newCoords[idx+1] = ptC[1];
            newCoords[idx+2] = ptC[2];
            newColors[cidx] = r1;
            newColors[cidx+1] = g1;
            newColors[cidx+2] = b1;  
            if (clrDim == 4) newColors[cidx+3] = a1;            
            newNormals[idx] = 0f;
            newNormals[idx+1] = 0f;
            newNormals[idx+2] = 1f;
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptA[0];
            newCoords[idx+1] = ptA[1];
            newCoords[idx+2] = ptA[2];
            newColors[cidx] = r0;
            newColors[cidx+1] = g0;
            newColors[cidx+2] = b0;     
            if (clrDim == 4) newColors[cidx+3] = a0;            
            newNormals[idx] = 0f;
            newNormals[idx+1] = 0f;
            newNormals[idx+2] = 1f;
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptD[0];
            newCoords[idx+1] = ptD[1];
            newCoords[idx+2] = ptD[2];
            newColors[cidx] = r1;
            newColors[cidx+1] = g1;
            newColors[cidx+2] = b1;  
            if (clrDim == 4) newColors[cidx+3] = a1;            
            newNormals[idx] = 0f;
            newNormals[idx+1] = 0f;
            newNormals[idx+2] = 1f;
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptCC[0];
            newCoords[idx+1] = ptCC[1];
            newCoords[idx+2] = ptCC[2];
            newColors[cidx] = r1;
            newColors[cidx+1] = g1;
            newColors[cidx+2] = b1; 
            if (clrDim == 4) newColors[cidx+3] = a1;            
            newNormals[idx] = norm_x_trj[0];
            newNormals[idx+1] = norm_x_trj[1];
            newNormals[idx+2] = norm_x_trj[2];
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptAA[0];
            newCoords[idx+1] = ptAA[1];
            newCoords[idx+2] = ptAA[2];
            newColors[cidx] = r0;
            newColors[cidx+1] = g0;
            newColors[cidx+2] = b0; 
            if (clrDim == 4) newColors[cidx+3] = a0;            
            newNormals[idx] = norm_x_trj[0];
            newNormals[idx+1] = norm_x_trj[1];
            newNormals[idx+2] = norm_x_trj[2];
            numVert++;
            
            idx = numVert*3;
            cidx = numVert*clrDim;
            newCoords[idx] = ptDD[0];
            newCoords[idx+1] = ptDD[1];
            newCoords[idx+2] = ptDD[2];
            newColors[cidx] = r1;
            newColors[cidx+1] = g1;
            newColors[cidx+2] = b1; 
            if (clrDim == 4) newColors[cidx+3] = a1;            
            newNormals[idx] = norm_x_trj[0];
            newNormals[idx+1] = norm_x_trj[1];
            newNormals[idx+2] = norm_x_trj[2];
            numVert++;
            // end ribbon construction  -----------------------------
            
          }
        }
        
        
        array.coordinates = newCoords;
        array.normals = newNormals;
        array.colors = newColors;
        array.vertexCount = numVert;
        
        return array;
     }
     
     public static float[] AxB(float[] A, float[] B) {
       float[] axb = new float[3];
       
       axb[0] =   A[1] * B[2] - A[2] * B[1];
       axb[1] = -(A[0] * B[2] - A[2] * B[0]);
       axb[2] =   A[0] * B[1] - A[1] * B[0];
       
       return axb;
     }
     
     public static float AdotB(float[] A, float[] B) {
       float ab = A[0]*B[0] + A[1]*B[1] + A[2]*B[2];
       return ab;
     }
     
     public static double[] getRotatedVecInPlane(double[] T, double[] S, double[] P, double[] V, double theta, double[] rotV) {
        if (rotV == null) rotV = new double[3];
        
        double s = V[0]*Math.cos(theta) + V[1]*Math.sin(theta);
        double t = V[0]*Math.sin(theta) + V[1]*Math.cos(theta);
        
        double x = P[0] + s*S[0] + t*T[0];
        double y = P[1] + s*S[1] + t*T[1];
        double z = P[2] + s*S[2] + t*T[2];
        
        rotV[0] = x;
        rotV[1] = y;
        rotV[2] = z;
        
        return rotV;
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
     
     public static VisADGeometryArray scaleGeometry(VisADGeometryArray array, ArrayList<float[]> anchors, float scale) {
        int nShapes = anchors.size();
        int numVertsPerShape = array.vertexCount/nShapes;
        
        VisADGeometryArray scldArray = new VisADTriangleArray();
        scldArray.coordinates = new float[3*array.vertexCount];
        scldArray.colors = array.colors;
        scldArray.normals = array.normals;
        scldArray.vertexCount = array.vertexCount;
        
        for (int k=0; k<nShapes; k++) {
           float[] ancrPt = anchors.get(k);
           for (int t=0; t<numVertsPerShape; t++) {
              int idx = k*numVertsPerShape*3 + 3*t;
              float x0 = array.coordinates[idx];
              float y0 = array.coordinates[idx + 1];
              float z0 = array.coordinates[idx + 2];
              
              float x1 = (x0 - ancrPt[0])*scale;
              float y1 = (y0 - ancrPt[1])*scale;
              float z1 = (z0 - ancrPt[2])*scale;
              
              scldArray.coordinates[idx] = x1 + ancrPt[0];
              scldArray.coordinates[idx+1] = y1 + ancrPt[1];
              scldArray.coordinates[idx+2] = z1 + ancrPt[2];
           }           
        }
        
        return scldArray;
     }
     
     public static double getScaleX(visad.ProjectionControl pCntrl) {
       double[] matrix = pCntrl.getMatrix();
       double[] rot_a = new double[3];
       double[] trans_a = new double[3];
       double[] scale_a = new double[1];

       MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);
       return scale_a[0];
     }
     
     public static VisADGeometryArray makeGeometry(ArrayList<Trajectory> trajectories) {
         return null;
     }

     public static VisADGeometryArray makeTracerGeometry(ArrayList<Trajectory> trajectories, 
             ArrayList<VisADGeometryArray> arrays, ArrayList<float[]> anchors, int direction, float trcrSize, double[] scale, boolean fill) {
       int numTrajs = trajectories.size();
       VisADGeometryArray array = null;
       float[] coords;
       byte[] colors;
       float[] normals;
       int numPts;
       int numVerts;
       
       float[] allCoords = new float[3*2*6*numTrajs];
       byte[] allColors = new byte[3*2*6*numTrajs];

       double barblen = 0.02*trcrSize;

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
         float[] anchrPts = new float[] {traj.startPts[0], traj.startPts[1], traj.startPts[2]};
         anchors.add(anchrPts);
         
         System.arraycopy(coords, 0, allCoords, k*3*2*6, coords.length);
         System.arraycopy(colors, 0, allColors, k*3*2*6, colors.length);
       }
       
       VisADTriangleArray allarray = new VisADTriangleArray();
       allarray.vertexCount = 2*6*numTrajs;
       allarray.coordinates = allCoords;
       allarray.colors = allColors;
       
       return allarray;
     }
 

     public void forward(FlowInfo info, float[][] flow_values, byte[][] color_values, GriddedSet spatial_set, int direction, float timeStep)
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
        
        float[][] flowLoc = new float[3][1];
        float[][] flowVec = new float[3][1];

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
              
              float[][] del = Trajectory.adjustFlow(info, flowLoc, flowVec, timeStep);
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
              markGridTime[idx] = currentTimeIndex;
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
           
           // interpolated color not working yet. Use constant color from the start
           //addPair(startPts, stopPts, startColor, startColor);
           addPair(startPts, stopPts, startColor, stopColor);

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

     }

     private void addPair(float[] startPt, float[] stopPt, byte[] startColor, byte[] stopColor) {
        // new stuff 
        indexes[npairs] = coordCnt;
        
        coordinates[coordCnt++] = startPt[0];
        coordinates[coordCnt++] = startPt[1];
        coordinates[coordCnt++] = startPt[2];
        vertCnt++;

        coordinates[coordCnt++] =  stopPt[0];
        coordinates[coordCnt++] =  stopPt[1];
        coordinates[coordCnt++] =  stopPt[2];
        vertCnt++;
        
        npairs++;
        totNpairs++;
        lastx = stopPt[0];
        lasty = stopPt[1];
        lastz = stopPt[2];

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
        
        // grow arrays
        if (indexes.length == npairs) {
           int[] tmp = new int[npairs+40];
           System.arraycopy(indexes, 0, tmp, 0, npairs);
           indexes = tmp;
           tmp = new int[npairs+40];
        }
     }

     public static float[][] adjustFlow(FlowInfo info, float[][] spatial_values, float[][] flow_values, float timeStep) throws VisADException {
        return ShadowType.adjustFlowToEarth(info.which, flow_values, spatial_values, info.flowScale,
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
     
     public static int getNumIntrpPts(FlowInfo info, float maxSpd, double timeStep) throws VisADException {
       int numIntrpPts;
         
       float[][] del = Trajectory.adjustFlow(info, new float[][] {{0f}, {0f}, {0f}}, new float[][] {{50f},{0f},{0f}}, (float)timeStep);
       double intrvl = (del[0][0]/0.10);
       
       if (intrvl < 2) {
         numIntrpPts = 2;
       }
       else {
         numIntrpPts = (int) intrvl;
       }
          
       return numIntrpPts;
     }


     public static float[][] convertFlowUnit(float[][] values, Unit[] units) throws VisADException {
       // Flow units must be convertible to m s-1 for trajectory computation
       Unit meterPerSecond = CommonUnit.meterPerSecond;

       float[] valsX = values[0];
       if (Unit.canConvert(units[0], meterPerSecond)) {
          valsX = meterPerSecond.toThis(values[0], units[0]);
       }

       float[] valsY = values[1];
       if (Unit.canConvert(units[1], meterPerSecond)) {
          valsY = meterPerSecond.toThis(values[1], units[1]);
       }

       float[] valsZ = values[2];
       if (Unit.canConvert(units[2], meterPerSecond)) {
          valsZ = meterPerSecond.toThis(values[2], units[2]);
       }

       return new float[][] {valsX, valsY, valsZ};
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
     
     public static void setListener(ProjectionControl pCntrl, ControlListener listener, FlowControl flowCntrl) {
       if (scaleChangeListeners.containsKey(flowCntrl)) {
          ControlListener value = scaleChangeListeners.get(flowCntrl);
          pCntrl.removeControlListener(value);
          scaleChangeListeners.put(flowCntrl, listener);
       }
       else {
          scaleChangeListeners.put(flowCntrl, listener);
       }
       pCntrl.addControlListener(listener);
     }
     
     public static TrajectoryParams getLastTrajParams(FlowControl flowCntrl, TrajectoryParams params) {
        TrajectoryParams lastParams = null;
        if (trajParamMap.containsKey(flowCntrl)) {
           lastParams = trajParamMap.get(flowCntrl);
        }
        trajParamMap.put(flowCntrl, new TrajectoryParams(params));
        return lastParams;
     }
     
     public static Object setCache(FlowControl flowCntrl, boolean useCache) {
        Object cache = null;
        if (useCache) {
           cache = trajCacheMap.get(flowCntrl);
        }
        else {
           cache = new TrajCache();
           trajCacheMap.put(flowCntrl, cache);
        }
        return cache;
     }
     
     public static void initCleanUp(ScalarMap scalarMap, FlowControl flowCntrl, ProjectionControl pCntrl, DisplayImpl display) {
        if (!removeListeners.containsKey(scalarMap)) {
          removeListeners.put(scalarMap, new ListenForRemove(scalarMap, flowCntrl, pCntrl, display));
        }
     }
     
     public static void cacheTrajArray(Object cache, VisADGeometryArray array) {
        ((TrajCache)cache).trajArrayCache.add(array);
     }
     
     public static void cacheArray(Object cache, VisADGeometryArray array) {
        ((TrajCache)cache).arrayCache.add(array);
     }
     
     public static void cacheTrcrArray(Object cache, VisADGeometryArray array, ArrayList<float[]> anchors) {
        ((TrajCache)cache).trcrArrayCache.add(array);
        ((TrajCache)cache).ancrArrayCache.add(anchors);
     }
     
     public static void cacheTrcrArray(Object cache, int idx, VisADGeometryArray array, ArrayList<float[]> anchors) {
        ((TrajCache)cache).trcrArrayCache.set(idx, array);
        ((TrajCache)cache).ancrArrayCache.set(idx, anchors);
     }
     
     public static VisADGeometryArray getCachedTraj(Object cache, int idx) {
        return ((TrajCache)cache).trajArrayCache.get(idx);
     }
     
     public static VisADGeometryArray getCachedTrcr(Object cache, int idx) {
        return ((TrajCache)cache).trcrArrayCache.get(idx);
     }
     
     public static ArrayList<float[]> getCachedAncr(Object cache, int idx) {
        return ((TrajCache)cache).ancrArrayCache.get(idx);
     }
     
     public static VisADGeometryArray getCachedArray(Object cache, int idx) {
        return ((TrajCache)cache).arrayCache.get(idx);
     }     
     
     public static BranchGroup makeTracerBranch(ShadowType shadow, VisADGeometryArray trcrArray, ArrayList<float[]> achrArrays, GraphicsModeControl mode, float constant_alpha, float[] constant_color) throws VisADException {
        BranchGroup branch = (BranchGroup) shadow.makeBranch();
        branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        
        /* This branch is detached from 'branch' during auto resizing. 
           New resized trcrArray is then added back to 'branch' */
        BranchGroup trcrBranch = (BranchGroup) shadow.makeBranch();

        shadow.addToGroup(trcrBranch, trcrArray, mode, constant_alpha, constant_color);
        branch.addChild(trcrBranch);
        
        return branch;
     }
  }

  class TrajCache {
     ArrayList<VisADGeometryArray> trajArrayCache = new ArrayList<VisADGeometryArray>();
     ArrayList<VisADGeometryArray> trcrArrayCache = new ArrayList<VisADGeometryArray>();
     ArrayList<ArrayList<float[]>> ancrArrayCache = new ArrayList<ArrayList<float[]>>();
     ArrayList<VisADGeometryArray> arrayCache = new ArrayList<VisADGeometryArray>();     
  }

  class ListenForRemove implements ScalarMapListener, DisplayListener {
    ScalarMap theMap;
    FlowControl flowCntrl;
    ProjectionControl pCntrl;
    DisplayImpl display;
   
   public ListenForRemove(ScalarMap scalarMap, FlowControl control, ProjectionControl pCntrl, DisplayImpl display) {
     theMap = scalarMap;
     flowCntrl = control;
     this.pCntrl = pCntrl;
     this.display = display;
     scalarMap.addScalarMapListener(this);
     display.addDisplayListener(this);
   }
   
   public void displayChanged(DisplayEvent evt) {
      int id = evt.getId();
      if (id == DisplayEvent.DESTROYED) {
         cleanUp();
         display.removeDisplayListener(this);
      }
   }

   public void mapChanged(ScalarMapEvent evt) throws VisADException, RemoteException {
   }

   public void controlChanged(ScalarMapControlEvent evt) throws VisADException, RemoteException {
     int id = evt.getId();
     if (id == ScalarMapEvent.CONTROL_REMOVED || id == ScalarMapEvent.CONTROL_REPLACED) {
       cleanUp();
       theMap.removeScalarMapListener(this);
     }
   }
   
   private void cleanUp() {
     ControlListener listener = Trajectory.scaleChangeListeners.get(flowCntrl);
     pCntrl.removeControlListener(listener);
     Trajectory.scaleChangeListeners.remove(flowCntrl);
       
     Trajectory.trajParamMap.remove(flowCntrl);
     Trajectory.trajCacheMap.remove(flowCntrl);
     Trajectory.removeListeners.remove(theMap);
   }
     
  }
