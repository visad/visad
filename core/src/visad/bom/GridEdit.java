//
// GridEdit.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bom;

import visad.*;
import visad.util.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

/**
</pre>
   GridEdit is the VisAD class for warping and modifying fields.<p>
   Construct a GridEdit object linked to a 2-D grid [a FlatField
   with MathType ((x, y) -> range)]) or a sequence of 2-D grids [a
   FieldImpl with MathType (t -> ((x, y) -> range))], and a DisplayImpl.
   The grid or grids must all have the same domain Set, which must be a
   Gridded2DSet or a GriddedSet with domain dimension = 2.
   The domain must be mapped to two spatial DisplayRealTypes. If a
   sequence of grids, the sequence domain must be mapped to Animation.
   The grid may have any number of range RealTypes.

   The GridEdit object operates in a sequence:
   1. Invokes its start() method to start.
   2. User drags grid warp motion lines with the right mouse button.
      These lines must lie inside the grid.
   3. If user presses SHIFT while dragging a grid warp motion line, the
      program prompts for increments for values at the dragged location.
   4. User can delete lines by clicking the right button on their end
      points, with CTRL pressed.
   5. At any point after start(), the application can invoke stop() to
      stop the dragging process, and warp the grid.
   6. After the grid has been warped, the application can invoke undo()
      to undo the paste and stop the process.
   7. The process can be restarted by invoking start(), any number of times.

   The main() method illustrates a simple GUI and test case with a sequnece
   of grids. Run 'java visad.bom.GridEdit' to test with contour
   values, and run 'java visad.bom.GridEdit 1' to test with color
   values.
</pre>
*/
public class GridEdit extends Object implements ActionListener {

  private boolean debug = false;

  private Field grids = null;
  private DisplayImpl display = null;

  private Object lock = new Object();

  private RealType t = null; // non-null if animation
  private RealType x = null;
  private RealType y = null;
  private RealTupleType xy = null;
  private MathType range = null; // RealType or RealTupleType
  private int rangedim = 0;
  private int nts = 0; // number of steps in sequence

  Set tset = null; // t domain Set
  GriddedSet xyset = null; // (x, y) domain Set
  FunctionType grid_type;

  FlatField replacedff = null;
  FlatField savedff = null;

  AnimationControl acontrol = null;

  ScalarMap tmap = null;
  ScalarMap xmap = null;
  ScalarMap ymap = null;
  DisplayTupleType xtuple = null;
  DisplayTupleType ytuple = null;

  private CellImpl cell_rbl = null;
  private DataReferenceImpl ref_rbl = null;
  private RubberBandLineRendererJ3D rblr = null;

  private final static int NPICKS = 50;
  private int npicks = 0;
  private PickCell[] cell_picks = null;
  private DataReferenceImpl[] ref_picks = null;
  private PickManipulationRendererJ3D[] rend_picks = null;
  private float[][] delta_picks = null;

  private GridEdit thiscp = null;

  private int renderer_mask;
  private int dialog_mask;

  /**
<pre>
     gs has MathType (t -> ((x, y) -> v)) or ((x, y) -> v)
     conditions on gs and display:
     1. x and y mapped to XAxis, YAxis, ZAxis
     2. (x, y) domain GriddedSet
     3. if (t -> ...), then t is mapped to Animation
</pre>
  */
  public GridEdit(Field gs, DisplayImplJ3D d)
         throws VisADException, RemoteException {
    grids = gs;
    display = d;
    renderer_mask = InputEvent.CTRL_MASK;
    dialog_mask = InputEvent.SHIFT_MASK;

    thiscp = this;

    FunctionType gstype = (FunctionType) gs.getType();
    RealTupleType domain = gstype.getDomain();
    int domdim = domain.getDimension();
    if (domdim == 1) {
      t = (RealType) domain.getComponent(0);
      tset = gs.getDomainSet();
      grid_type = (FunctionType) gstype.getRange();
      xy = grid_type.getDomain();
      int dim = xy.getDimension();
      if (dim != 2) {
        throw new VisADException("bad grid Field domain dimension: " + dim);
      }
      range = grid_type.getRange();
      nts = tset.getLength();
      for (int i=0; i<nts; i++) {
        FlatField ff = (FlatField) gs.getSample(i);
        Set s = ff.getDomainSet();
        if (!(s instanceof GriddedSet)) {
          throw new VisADException("grid set must be GriddedSet");
        }
        if (xyset == null) {
          xyset = (GriddedSet) s;
        }
        else {
          if (!xyset.equals(s)) {
            throw new VisADException("grid sets must match in animation");
          }
        }
      }
    }
    else if (domdim == 2) {
      t = null;
      tset = null;
      xy = domain;
      grid_type = gstype;
      range = gstype.getRange();
      Set s = gs.getDomainSet();
      if (!(s instanceof GriddedSet)) {
        throw new VisADException("grid set must be GriddedSet");
      }
      xyset = (GriddedSet) s;
    }
    else {
      throw new VisADException("bad grid Field domain dimension: " + domdim);
    }
    x = (RealType) xy.getComponent(0);
    y = (RealType) xy.getComponent(1);

    if (range instanceof RealType) {
      rangedim = 1;
    }
    else if (range instanceof RealTupleType) {
      rangedim = ((RealTupleType) range).getDimension();
    }
    else {
      throw new VisADException("bad grid Field range type: " + range);
    }

    Vector scalar_map_vector = display.getMapVector();
    Enumeration en = scalar_map_vector.elements();
    while (en.hasMoreElements()) {
      ScalarMap map = (ScalarMap) en.nextElement();
      ScalarType scalar = map.getScalar();
      DisplayRealType dreal = map.getDisplayScalar();
      DisplayTupleType tuple = dreal.getTuple();
      if (scalar.equals(t)) {
        if (Display.Animation.equals(dreal)) {
          tmap = map;
          acontrol = (AnimationControl) tmap.getControl();
        }
      }
      else if (tuple != null &&
               (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
                (tuple.getCoordinateSystem() != null &&
                 tuple.getCoordinateSystem().getReference().equals(
                 Display.DisplaySpatialCartesianTuple)))) { // spatial
        if (scalar.equals(x)) {
          xmap = map;
          xtuple = tuple;
        }
        else if (scalar.equals(y)) {
          ymap = map;
          ytuple = tuple;
        }
      }
    }
    if (xmap == null || ymap == null || xtuple != ytuple) {
      throw new VisADException("grid domain RealTypes must be mapped to " +
                               "spatial DisplayRealTypes from the same DisplayTupleType");
    }
    if (t != null && tmap == null) {
      throw new VisADException("grid sequence must be mapped to Animation");
    }

    makePicks(NPICKS);

    ref_rbl = new DataReferenceImpl("rbl");

    rblr = new RubberBandLineRendererJ3D(x, y, renderer_mask, 0);
    display.addReferences(rblr, ref_rbl);
    rblr.suppressExceptions(true);
    rblr.toggle(false);

    // rubber band line release
    cell_rbl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        synchronized (lock) {
          Set set = (Set) ref_rbl.getData();
          if (set == null) return;
          float[][] samples = set.getSamples();
          if (samples == null) return;
          // make sure both ends of set are within grid domain
          int[] indices = xyset.valueToIndex(samples);
          if (indices[0] < 0 || indices[1] < 0) return;

          // find an available PickCell
          int index = -1;
          for (int i=0; i<npicks; i++) {
            if (ref_picks[i].getData() == null) {
              // put stroke in available PickCell
              index = i;
              cell_picks[index].setSkip();
              ref_picks[index].setData(set);
              rend_picks[index].toggle(true);
              break;
            }
          }
          if (index < 0) {
            // no PickCell available, so create NPICKS more
            index = npicks;
            makePicks(npicks + NPICKS);
            cell_picks[index].setSkip();
            ref_picks[index].setData(set);
            rend_picks[index].toggle(true);
          }

          int modifiers = rblr.getLastMouseModifiers();
          if ((modifiers & dialog_mask) != 0) { // yes
            FlatField ff = null;
            int tindex;
            if (t != null) {
              tindex = getAnimationIndex();
              if (tindex < 0 || tindex >= nts) return;
              ff = (FlatField) grids.getSample(tindex);
            }
            else {
              ff = (FlatField) grids;
            }
            Data range = ff.getSample(indices[0]);
            if (range instanceof Real) {
              float value = (float) ((Real) range).getValue();
              String name = ((RealType) range.getType()).getName();
              String question = "increment " + name + " (current: " + value + ")";
              String newvs = JOptionPane.showInputDialog(null, question);
              float newvalue = 0.0f;
              try {
                newvalue = Float.parseFloat(newvs);
              }
              catch (NumberFormatException e) {
              }
              delta_picks[0][index] = newvalue;
              // System.out.println(name + " increment = " + newvalue);
            }
            else if (range instanceof RealTuple) {
              for (int j=0; j<rangedim; j++) {
                Real real = (Real) ((RealTuple) range).getComponent(j);
                float value = (float) real.getValue();
                String name = ((RealType) real.getType()).getName();
                String question = "increment " + name + " (default: " + value + ")";
                String newvs = JOptionPane.showInputDialog(null, question);
                float newvalue = 0.0f;
                try {
                  newvalue = Float.parseFloat(newvs);
                }
                catch (NumberFormatException e) {
                }
                delta_picks[j][index] = newvalue;
                // System.out.println(name + " increment = " + newvalue);
              }
            }
          }
        } // end synchronized (lock)
      }
    };

  }

  // make PickCell's etc up from npicks through n-1
  private void makePicks(int n) throws VisADException, RemoteException  {
    if (n <= npicks) return;
    PickCell[] tcell = null;
    DataReferenceImpl[] tref = null;
    PickManipulationRendererJ3D[] trend = null;
    float[][] tdelta = null;
    if (npicks > 0) {
      tcell = cell_picks;
      tref = ref_picks;
      trend = rend_picks;
      tdelta = delta_picks;
    }
    cell_picks = new PickCell[n];
    ref_picks = new DataReferenceImpl[n];
    rend_picks = new PickManipulationRendererJ3D[n];
    delta_picks = new float[rangedim][n];
    if (npicks > 0) {
      System.arraycopy(tcell, 0, cell_picks, 0, npicks);
      System.arraycopy(tref, 0, ref_picks, 0, npicks);
      System.arraycopy(trend, 0, rend_picks, 0, npicks);
      for (int j=0; j<rangedim; j++) {
        System.arraycopy(tdelta[j], 0, delta_picks[j], 0, npicks);
      }
    }
    display.disableAction();
    for (int i=npicks; i<n; i++) makePick(i);
    display.enableAction();
    npicks = n;
  }

  // make PickCell etc for index i
  private void makePick(int i) throws VisADException, RemoteException  {
    ref_picks[i] = new DataReferenceImpl("pick" + i);
    rend_picks[i] = new PickManipulationRendererJ3D(renderer_mask, renderer_mask);
    rend_picks[i].suppressExceptions(true);
    rend_picks[i].toggle(false);
    cell_picks[i] = new PickCell(ref_picks[i],  rend_picks[i]);
    cell_picks[i].setSkip();
    cell_picks[i].addReference(ref_picks[i]);
    display.addReferences(rend_picks[i], ref_picks[i]);
    for (int j=0; j<rangedim; j++) delta_picks[j][i] = 0.0f;
  }

  /**
   * enable user to draw move vectors with optional deltas
   */
  public void start() throws VisADException, RemoteException {
    synchronized (lock) {
      cell_rbl.addReference(ref_rbl);
      Gridded2DSet dummy_set = new Gridded2DSet(xy, null, 1);
      ref_rbl.setData(dummy_set);
      rblr.toggle(true);
    }
  }

  /**
   * warp grid according to move vectors drawn by user
   * also interpolate user defined delta values at move points
   */
  public void stop() throws VisADException, RemoteException {
    synchronized (lock) {
      int np = 0; // number of pick points
      for (int i=0; i<npicks; i++) {
        if (ref_picks[i].getData() != null) np++;
      }

      if (np != 0) {

        FlatField ff = null;
        int index;
        if (t != null) {
          index = getAnimationIndex();
          if (index < 0 || index >= nts) return;
          ff = (FlatField) grids.getSample(index);
        }
        else {
          ff = (FlatField) grids;
        }
        savedff = new FlatField(grid_type, xyset);
        savedff.setSamples(ff.getFloats(false), false);

        float[][][] set_samples = new float[np][][];
        float[][] deltas = new float[rangedim][np];
        int k = 0;
        for (int i=0; i<npicks; i++) {
          if (ref_picks[i].getData() != null) {
            for (int j=0; j<rangedim; j++) deltas[j][k] = delta_picks[j][i];
            set_samples[k++] = ((Set) ref_picks[i].getData()).getSamples(false);
          }
        }

        // TO DO: set deltas = null if all 0.0f
        FlatField newff = warpGrid(ff, set_samples, deltas);

        ff.setSamples(newff.getFloats(false), false);

        replacedff = ff;
      } // end if (np != 0)

      display.disableAction();
      rblr.toggle(false);
      for (int i=0; i<npicks; i++) {
        cell_picks[i].setSkip();
        ref_picks[i].setData(null);
        rend_picks[i].toggle(true);
      }
      for (int j=0; j<rangedim; j++) {
        for (int i=0; i<npicks; i++) delta_picks[j][i] = 0.0f;
      }
      display.enableAction();
 
      try { cell_rbl.removeReference(ref_rbl); }
      catch (ReferenceException e) { }

    }
  }

  /**
   * warpGrid is the workhorse of GridEdit and can be used independently
   * of any instances of the class
   * @param ff          A FlatField containing the 2-D grid to be warped.
   * @param set_samples The move vectors, dimensioned [number_of_moves][2][2]
   *                 where the second index enumerates x and y and the third
   *                 index enumerates the "from" and "to" ends of the move.
   *                 These values are x and y data values
   * @param deltas   Increments for moved values, to be interpolated over the
   *       grid, dimensioned [number_of_grid_range_values][number_of_moves].
   *                 May be null.
   * @return         Warped grid, with motion vectors and deltas applied.
   * @throws VisADException bad parameters
   */
  public static FlatField warpGrid(FlatField ff, float[][][] set_samples,
                                   float[][] deltas)
         throws VisADException, RemoteException {

    if (ff == null || set_samples == null || set_samples.length == 0) {
      throw new VisADException("null parameter");
    }
    FunctionType fft = (FunctionType) ff.getType();
    RealTupleType xy = fft.getDomain();
    GriddedSet xyset = (GriddedSet) ff.getDomainSet();
    MathType range = fft.getRange();
    int range_dim = -1;
    int np = set_samples.length; // number of move points
    if (range instanceof RealType) {
      range_dim = 1;
    }
    else if (range instanceof RealTupleType) {
      range_dim = ((RealTupleType) range).getDimension();
    }
    if (deltas != null) {
      if (deltas.length != range_dim) {
        throw new VisADException("deltas bad length");
      }
      for (int j=0; j<range_dim; j++) {
        if (deltas[j] == null || deltas[j].length != np) {
          throw new VisADException("deltas bad length");
        }
      }
    }

    int nx = xyset.getLength(0);
    int ny = xyset.getLength(1);
    // get locations of grid border points, in order
/*
    // every point on border
    int nb = 2 * (nx + ny) - 4; // number of grid border points
    int[] indicesb = new int[nb];
    int k = 0;
    for (int i=0; i<nx; i++) {
      indicesb[k++] = i;
    }
    for (int i=1; i<(ny-1); i++) {
      indicesb[k++] = (i + 1) * nx - 1; 
    }
    for (int i=nx-1; i>=0; i--) {
      indicesb[k++] = (ny - 1) * nx + i; 
    }
    for (int i=ny-2; i>=1; i--) {
      indicesb[k++] = i * nx; 
    }
*/

    // just 4 corner points on border
    // int nb = 4;
    // int[] indicesb = {0, (ny - 1) * nx, ny * nx - 1, nx - 1};

    // limit number of points on border
    int NB = 32;
    // int NB = 4;
    int nxb = (nx > NB) ? NB : nx;
    int nyb = (ny > NB) ? NB : ny;
    float xb = ((float) nx) / ((float) nxb);
    float yb = ((float) ny) / ((float) nyb);
    int nb = 2 * (nxb + nyb) - 4; // number of grid border points
    int[] indicesb = new int[nb];
    int k = 0;
    for (int i=0; i<nxb; i++) {
      int ii = Math.round(i * xb);
      if (ii < 0) ii = 0;
      if (ii > (nx - 1)) ii = nx - 1;
      indicesb[k++] = ii;
    }
    for (int i=1; i<(nyb-1); i++) {
      int ii = Math.round(i * yb);
      if (ii < 1) ii = 1;
      if (ii > (ny - 2)) ii = ny - 2;
      indicesb[k++] = (ii + 1) * nx - 1; 
    }
    for (int i=nxb-1; i>=0; i--) {
      int ii = Math.round(i * xb);
      if (ii < 0) ii = 0;
      if (ii > (nx - 1)) ii = nx - 1;
      indicesb[k++] = (ny - 1) * nx + ii; 
    }
    for (int i=nyb-2; i>=1; i--) {
      int ii = Math.round(i * yb);
      if (ii < 1) ii = 1;
      if (ii > (ny - 2)) ii = ny - 2;
      indicesb[k++] = ii * nx; 
    }

    float[][] samplesb = xyset.indexToValue(indicesb); // locations of grid border points

    // check if all move "to" points are inside 0.8 radius of circle
    float[][] set_locs = new float[2][np];
    for (int i=0; i<np; i++) {
      set_locs[0][i] = set_samples[i][0][1];
      set_locs[1][i] = set_samples[i][1][1];
    }
    float[][] set_grid = xyset.valueToGrid(set_locs);
    float nx2 = (nx - 1.0f) / 2.0f;
    float ny2 = (ny - 1.0f) / 2.0f;
    float nm2 = Math.min(nx2, ny2);
    float nm22 = nm2 * nm2;
    float nx22 = nx2 * nx2;
    float ny22 = ny2 * ny2;
    boolean all_in = true;
    for (int i=0; i<np; i++) {
      float d = (set_grid[0][i] - nx2) * (set_grid[0][i] - nx2) / nm22 +
                (set_grid[1][i] - ny2) * (set_grid[1][i] - ny2) / nm22;
      if (d > 0.64) {
        all_in = false;
        // System.out.println("not all_in d = " + d);
        break;
      }
    }

    if (all_in) {
      // all move "to" points inside 0.8 radius, so add circle boundary points
      int ne = 32; // weird bug for ne proportional to nx and ny
      int new_nb = nb + ne;
      float[][] new_samplesb = new float[2][new_nb];
      System.arraycopy(samplesb[0], 0, new_samplesb[0], 0, nb);
      System.arraycopy(samplesb[1], 0, new_samplesb[1], 0, nb);
      float[][] circle_grid = new float[2][ne];
      for (int i=0; i<ne; i++) {
        double ang = 2.0 * Math.PI * i / (ne);
        circle_grid[0][i] = nx2 + nm2 * 0.90f * ((float) Math.sin(ang) );
        circle_grid[1][i] = ny2 + nm2 * 0.90f * ((float) Math.cos(ang) );
      }
      float[][] circle_locs = xyset.gridToValue(circle_grid);
      System.arraycopy(circle_locs[0], 0, new_samplesb[0], nb, ne);
      System.arraycopy(circle_locs[1], 0, new_samplesb[1], nb, ne);
      samplesb = new_samplesb;
      nb = new_nb;
    }


    RealType xmove = RealType.getRealType("xmove");
    RealType ymove = RealType.getRealType("ymove");
    RealTupleType xymove = new RealTupleType(xmove, ymove); 
    FunctionType move_type = new FunctionType(xy, xymove);

    // combine stationary border points with move points
    int ns = nb + np;
    float[][] mover = new float[2][ns];
    float[][] moved = new float[2][ns];
    for (int i=0; i<nb; i++) {
      // located at border point
      moved[0][i] = samplesb[0][i];
      moved[1][i] = samplesb[1][i];
      // zero move vector
      mover[0][i] = 0.0f;
      mover[1][i] = 0.0f;
    }

    // float[][] ireg = new float[2][np]; // for non-Delaunay IrregularSet
    for (int i=0; i<np; i++) {
      int ip = nb + i;
      float[][] samples = set_samples[i];
      // located at "to" end of vector user drew
      moved[0][ip] = samples[0][1];
      moved[1][ip] = samples[1][1];
      // move vector is negative of vector user drew
      mover[0][ip] = samples[0][0] - samples[0][1];
      mover[1][ip] = samples[1][0] - samples[1][1];
      // for non-Delaunay IrregularSet
      // ireg[0][i] = samples[0][1];
      // ireg[1][i] = samples[1][1];
    }

/*
a nice idea, but this accentuates corner effects
    // compute Irregular2DSet without any triangles whose vertices
    // are all 3 on the border
    Irregular2DSet nset = new Irregular2DSet(xy, ireg);
    Delaunay del = nset.Delan;
    int[][] dtris = null;
    if (del != null) dtris = del.Tri;

    // find closest internal point to each border point
    int[] closest = new int[nb];
    for (int i=0; i<nb; i++) {
      float dist = Float.MAX_VALUE;
      for (int j=0; j<np; j++) {
        float d = (moved[0][i] - ireg[0][j]) * (moved[0][i] - ireg[0][j]) +
                  (moved[1][i] - ireg[1][j]) * (moved[1][i] - ireg[1][j]);
        if (d < dist) {
          dist = d;
          closest[i] = j + nb;
        }
      }
    }

    // compute triangles for borders (no triangles with all 3 vertices on border)
    int[][] btris = new int[2 * nb][3];
    int ntris = 0;
    for (int i=0; i<nb; i++) {
      int ip = i + 1;
      if (ip == nb) ip = 0;
      btris[ntris][0] = i;
      btris[ntris][1] = ip;
      btris[ntris][2] = closest[i];
// System.out.println("a " + btris[ntris][0] + " " + btris[ntris][1] + " " + btris[ntris][2]);
      ntris++;
      if (closest[i] != closest[ip]) {
        btris[ntris][0] = ip;
        btris[ntris][1] = closest[i];
        btris[ntris][2] = closest[ip];
// System.out.println("b " + btris[ntris][0] + " " + btris[ntris][1] + " " + btris[ntris][2]);
        ntris++;
      }
    }
    // add internal triangles
    if (dtris != null) {
      int nd = dtris.length;
      for (int i=0; i<nd; i++) {
        btris[ntris][0] = dtris[i][0] + nb;
        btris[ntris][1] = dtris[i][1] + nb;
        btris[ntris][2] = dtris[i][2] + nb;
// System.out.println("c " + btris[ntris][0] + " " + btris[ntris][1] + " " + btris[ntris][2]);
        ntris++;
      }
    }
    int[][] tris = new int[ntris][3];
    for (int i=0; i<ntris; i++) {
      int a = btris[i][0];
      int b = btris[i][1];
      int c = btris[i][2];
      float cross = (moved[0][b]-moved[0][a]) * (moved[1][c]-moved[1][a]) -
                    (moved[0][c]-moved[0][a]) * (moved[1][b]-moved[1][a]);
      // ensure uniform rotation direction of triangles
      if (cross > 0.0f) {
        tris[i][0] = a;
        tris[i][1] = b;
        tris[i][2] = c;
      }
      else {
        tris[i][0] = a; 
        tris[i][1] = c; 
        tris[i][2] = b; 
      }
// System.out.println("d " + tris[i][0] + " " + tris[i][1] + " " + tris[i][2]);
    }
    DelaunayCustom dc = new DelaunayCustom(moved, tris);
    Irregular2DSet iset = new Irregular2DSet(xy, moved, null, null, null, dc);
    // end of compute Irregular2DSet without any triangles whose vertices
    // are all 3 on the border
*/

    // compute Irregular2DSet via simple Delaunay
    IrregularSet iset = new Irregular2DSet(xy, moved);

    // moveff is vector Field of motions, at Irregular2DSet combining
    // stationary border points with inverses of user drawn vectors
    FlatField moveff = new FlatField(move_type, iset);
    moveff.setSamples(mover);

    // interpolate inverse motions to grid locations
    FlatField move_interp = (FlatField) moveff.resample(xyset);

    // form a new "warped" Gridded2DSet of the locations
    // that grid move to under interpolate inverse motions
    float[][] bases = xyset.getSamples(true); // copy
    float[][] offsets = move_interp.getFloats(false);
    for (int i=0; i<nx*ny; i++) {
      bases[0][i] += offsets[0][i];
      bases[1][i] += offsets[1][i];
    }
    Gridded2DSet warpset =
      new Gridded2DSet(xy, bases, nx, ny, null, null, null, false, false);

    // interpolated grid values at locations of warped grid
    FlatField warpff = (FlatField) ff.resample(warpset);

    // put interpolated values at warped grid into a new grid
    // at the original grid locations, and return it
    FlatField newff = new FlatField(fft, xyset);
    newff.setSamples(warpff.getFloats(false), false);

    if (deltas != null) {
      // form irregular Field of delta values
      FlatField deltaff = new FlatField(fft, iset);
      float[][] ds = new float[range_dim][ns];
      for (int j=0; j<range_dim; j++) {
        for (int i=0; i<nb; i++) ds[j][i] = 0.0f;
        System.arraycopy(deltas[j], 0, ds[j], nb, np);
      }
      deltaff.setSamples(ds, false);
      newff = (FlatField) newff.add(deltaff, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
    }

    // replace any missing newff values with ff values
    float[][] ff_values = ff.getFloats(false);
    float[][] newff_values = newff.getFloats(false);
    boolean any_missing = false;
    int nff = ff_values[0].length;
    for (int j=0; j<range_dim; j++) {
      for (int i=0; i<nff; i++) {
        if (newff_values[j][i] != newff_values[j][i]) {
          newff_values[j][i] = ff_values[j][i];
          any_missing = true;
        }
      }
    }
    if (any_missing) newff.setSamples(newff_values);

    return newff;
  }

  /**
   * undo action of last call to stop()
   */
  public void undo() throws VisADException, RemoteException {
    synchronized (lock) {
      if (replacedff != null) {
        float[][] samples = savedff.getFloats(false);
        replacedff.setSamples(samples, false);
      }
      replacedff = null;
    }
    stop();
  }

  // return the index of the current animation step
  private int getAnimationIndex() throws VisADException {
    int[] indices = {acontrol.getCurrent()};
    Set aset = acontrol.getSet();
    double[][] values = aset.indexToDouble(indices);
    int[] tindices = tset.doubleToIndex(values);
    return tindices[0];
  }

  private static final int NX = 64;
  private static final int NY = 64;
  private static final int NTIMES = 4;

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealTupleType xy = new RealTupleType(x, y);
    RealType windx = RealType.getRealType("windx",
                          CommonUnit.meterPerSecond);     
    RealType windy = RealType.getRealType("windy",
                          CommonUnit.meterPerSecond);     
    RealType red = RealType.getRealType("red");
    RealType green = RealType.getRealType("green");

    // EarthVectorType extends RealTupleType and says that its
    // components are vectors in m/s with components parallel
    // to Longitude (positive east) and Latitude (positive north)
    EarthVectorType windxy = new EarthVectorType(windx, windy);

    RealType time = RealType.Time;
    double startt = new DateTime(1999, 122, 57060).getValue();
    Linear1DSet time_set = new Linear1DSet(time, startt, startt + 2700.0, NTIMES);

    Linear2DSet grid_set = new Integer2DSet(xy, NX, NY);

    RealTupleType tuple_type = new RealTupleType(new RealType[]
             {lon, lat, windx, windy, red, green});

    FunctionType field_type = new FunctionType(xy, tuple_type);
    FunctionType seq_type = new FunctionType(time, field_type);

    // construct first Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImplJ3D display1 =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    double NM = Math.max(NX, NY);
    ScalarMap ymap = new ScalarMap(y, Display.YAxis);
    display1.addMap(ymap);
    ScalarMap xmap = new ScalarMap(x, Display.XAxis);
    display1.addMap(xmap);
    ymap.setRange(0.0, NM);
    xmap.setRange(0.0, NM);

    ScalarMap cmap = null;
    if (args.length > 0) {
      cmap = new ScalarMap(windy, Display.RGB);
    }
    else {
      cmap = new ScalarMap(windy, Display.IsoContour);
    }
    display1.addMap(cmap);

    ScalarMap amap = new ScalarMap(time, Display.Animation);
    display1.addMap(amap);
    AnimationControl acontrol = (AnimationControl) amap.getControl();
    acontrol.setStep(500);

    // create an array of NX by NY winds
    FieldImpl field = new FieldImpl(seq_type, time_set);
    double[][] values = new double[6][NX * NY];
    for (int k=0; k<NTIMES; k++) {
      FlatField ff = new FlatField(field_type, grid_set);
      int m = 0;
      double NX2 = NX / 2.0;
      double NY2 = NY / 2.0;
      for (int j=0; j<NY; j++) {
        for (int i=0; i<NX; i++) {

          double u = 2.0 * (i - NX2) / (NM - 1.0);
          double v = 2.0 * (j - NY2) / (NM - 1.0);
  
          // each wind record is a Tuple (lon, lat, (windx, windy), red, green)
          // set colors by wind components, just for grins
          values[0][m] = 10.0 * u;
          values[1][m] = 10.0 * v - 40.0;
          double fx = k + 30.0 * u;
          double fy = 30.0 * v;
          double fd =
            Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy) + k * 15.0;
          double fs = Math.sqrt(fx * fx + fy * fy);
          values[2][m] = fd;
          values[3][m] = fs;
          values[4][m] = u;
          values[5][m] = v;
          m++;
        }
      }
      ff.setSamples(values);
      field.setSample(k, ff);
    }

    DataReferenceImpl seq_ref = new DataReferenceImpl("seq");
    seq_ref.setData(field);
    display1.addReference(seq_ref);

    final GridEdit cp = new GridEdit(field, display1);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test CollectiveBarbManipulation");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));


    panel1.add(display1.getComponent());
    panel1.setMaximumSize(new Dimension(400, 600));

    JPanel panel3 = new JPanel();
    panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
    final JButton start = new JButton("start");
    start.addActionListener(cp);
    start.setActionCommand("start");
    final JButton stop = new JButton("stop");
    stop.addActionListener(cp);
    stop.setActionCommand("stop");
    final JButton undo = new JButton("undo");
    undo.addActionListener(cp);
    undo.setActionCommand("undo");
    panel3.add(start);
    panel3.add(stop);
    panel3.add(undo);

    panel2.add(new AnimationWidget(amap));
    panel2.add(new JLabel(" "));
    if (args.length > 0) {
      LabeledColorWidget lcw = new LabeledColorWidget(cmap);
      lcw.setMaximumSize(new Dimension(400, 200));
      panel2.add(lcw);
    }
    else {
      ContourWidget cw = new ContourWidget(cmap);
      cw.setMaximumSize(new Dimension(400, 200));
      panel2.add(cw);
    }
    panel2.add(new JLabel(" "));
    panel2.add(panel3);
    panel2.setMaximumSize(new Dimension(400, 600));

    panel.add(panel1);
    panel.add(panel2);
    frame.getContentPane().add(panel);

    // set size of JFrame and make it visible
    frame.setSize(800, 600);
    frame.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("start")) {
      try {
        start();
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
    else if (cmd.equals("stop")) {
      try {
        stop();
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
    else if (cmd.equals("undo")) {
      try {
        undo();
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
  }

}

/**
 * CellImpl for user clicks to delete move vectors
 * via setData(null)
*/
class PickCell extends CellImpl {

  private boolean skip;
  private DataRenderer rend;
  private DataReferenceImpl ref;

  public PickCell(DataReferenceImpl rf, DataRenderer rd) {
    rend = rd;
    ref = rf;
    skip = true;
  }

  public void doAction() throws VisADException, RemoteException {
    if (skip) {
       skip = false;
    }
    else {
      rend.toggle(false);
      skip = true;
      ref.setData(null);
    }
  }

  public void setSkip() {
    skip = true;
  }
}

