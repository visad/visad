//
// MeasureLine.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/**
 * MeasureLine maintains a (p1, line, p2) triple of
 * DataReferences for measuring distances in a field.
 */
public class MeasureLine extends MeasureThing {

  /** List of all measurement lines. */
  private static final Vector lines = new Vector();

  /** Id number for the line. */
  private int id;

  /** Associated line pool. */
  private LinePool pool;

  /** Data reference for connecting line. */
  private DataReferenceImpl ref_line;

  /** Data references for X's. */
  private DataReferenceImpl ref_x1, ref_x2;

  /** Cell that ties line to endpoints. */
  private CellImpl lineCell;

  /** Vector of listeners that receive line changed events. */
  private Vector listeners;

  /** Constructs a measurement line. */
  public MeasureLine(int dim) throws VisADException, RemoteException {
    this(dim, null);
  }

  /** Constructs a measurement line with an associated line pool. */
  public MeasureLine(int dim, LinePool pool)
    throws VisADException, RemoteException
  {
    super(2, dim);
    ref_line = new DataReferenceImpl("line");
    if (dim == 2) {
      ref_x1 = new DataReferenceImpl("x1");
      ref_x2 = new DataReferenceImpl("x2");
    }
    this.pool = pool;
    id = pool == null ? 0 : pool.maxLnId++;
    listeners = new Vector();

    final MeasureLine line = this;
    final int fdim = dim;
    lineCell = new CellImpl() {
      public void doAction() {
        if (dtype == null) return;
        RealTuple p1, p2;
        synchronized (dataLock) {
          p1 = (RealTuple) refs[0].getData();
          p2 = (RealTuple) refs[1].getData();
        }
        if (p1 == null || p2 == null) return;
        int rdim = p1.getDimension();

        // extract samples
        float[][] samps = new float[fdim][2];
        try {
          for (int i=0; i<fdim; i++) {
            Real r1 = (Real) p1.getComponent(i);
            Real r2 = (Real) p2.getComponent(i);
            samps[i][0] = (float) r1.getValue();
            samps[i][1] = (float) r2.getValue();
          }
        }
        catch (VisADException exc) {
          exc.printStackTrace();
          samps = null;
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
          samps = null;
        }
        if (samps == null) return;

        try {
          // set line data
          FunctionType ftype =
            new FunctionType(dtype, FileSeriesWidget.COLOR_TYPE);
          GriddedSet set;
          if (fdim == 2) set = new Gridded2DSet(dtype, samps, 2);
          else if (fdim == 3) set = new Gridded3DSet(dtype, samps, 2);
          else set = new GriddedSet(dtype, samps, new int[] {2});
          FlatField field = new FlatField(ftype, set);
          double[][] values = new double[][] {{id, id}};
          field.setSamples(values);
          ref_line.setData(field);

/*
          if (fdim == 2) {
            // extract slice numbers
            int dim1 = p1.getDimension();
            int dim2 = p2.getDimension();
            Real r1 = (Real) p1.getComponent(dim1 - 1);
            Real r2 = (Real) p2.getComponent(dim2 - 1);
            float slice1 = (float) r1.getValue();
            float slice2 = (float) r2.getValue();

            // check if slices match
            if (slice1 != slice || slice2 != slice) {
              // add X over non-matching endpoint
              float sx, sy;
              if (slice1 != slice) {
                sx = samps[0][0];
                sy = samps[1][0];
              }
              else { // slice2 != slice
                sx = samps[0][1];
                sy = samps[1][1];
              }
              float c = SelectionBox.DISTANCE;
              float[][] pts1 = { {sx - c, sx + c}, {sy - c, sy + c} };
              float[][] pts2 = { {sx - c, sx + c}, {sy + c, sy - c} };
              Gridded2DSet x_set1 = new Gridded2DSet(dtype, pts1, 2);
              Gridded2DSet x_set2 = new Gridded2DSet(dtype, pts2, 2);
              FlatField x_field1 = new FlatField(ftype, x_set1);
              FlatField x_field2 = new FlatField(ftype, x_set2);
              x_field1.setSamples(values);
              x_field2.setSamples(values);
              ref_x1.setData(x_field1);
              ref_x2.setData(x_field2);
            }
          }
*/
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }

        // notify listeners
        LineChangeListener[] l = new LineChangeListener[listeners.size()];
        listeners.copyInto(l);
        LineChangedEvent e = new LineChangedEvent(line, samps);
        for (int i=0; i<l.length; i++) l[i].lineChanged(e);
      }
    };
    lineCell.addReference(refs[0]);
    lineCell.addReference(refs[1]);

    lines.add(this);
  }

  /**
   * Adds a listener that receives notification
   * of changes to the line's endpoints.
   */
  public void addLineChangeListener(LineChangeListener l) {
    if (listeners.indexOf(l) < 0) listeners.add(l);
  }

  /** Removes a line change listener from this line. */
  public void removeLineChangeListener(LineChangeListener l) {
    if (listeners.indexOf(l) >= 0) listeners.remove(l);
  }

  /** Removes all line change listeners from this line. */
  public void removeAllLineChangeListeners() {
    if (listeners.size() > 0) listeners.removeAllElements();
  }

  /** Adds the distance measuring data to the given display. */
  public void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    if (display != null) {
      // remove measuring data from old display
      display.removeReference(refs[0]);
      display.removeReference(refs[1]);
      display.removeReference(ref_line);
      if (dim == 2) {
        display.removeReference(ref_x1);
        display.removeReference(ref_x2);
      }
    }
    display = d;
    if (d == null) return;

    // configure display appropriately
    DisplayRenderer dr = d.getDisplayRenderer();
    d.getGraphicsModeControl().setPointSize(5.0f);
    dr.setPickThreshhold(5.0f);

    // add endpoints
    renderers = new DataRenderer[dim == 2 ? 5 : 3];
    renderers[0] = addDirectManipRef(d, refs[0]);
    renderers[1] = addDirectManipRef(d, refs[1]);

    // add connecting line
    renderers[2] = dr.makeDefaultRenderer();
    d.addReferences(renderers[2], new DataReference[] {ref_line}, null);

    if (dim == 2) {
      // add X lines
      renderers[3] = dr.makeDefaultRenderer();
      d.addReferences(renderers[3], new DataReference[] {ref_x1}, null);
      renderers[4] = dr.makeDefaultRenderer();
      d.addReferences(renderers[4], new DataReference[] {ref_x2}, null);
    }
  }

  /** Sets the line's color. */
  public void setColor(Color color) {
    if (display == null) return;
    Vector v = display.getControls(ColorControl.class);
    ColorControl cc = (ColorControl) v.lastElement();
    if (cc == null) return;
    float[][] table = cc.getTable();
    int len = table[0].length;
    if (pool != null && len < pool.maxLnId) {
      if (pool.maxLnId >= 256) {
        System.err.println("Warning: cannot handle more than 256 lines.");
        return;
      }
      // adjust table as necessary
      float[][] t = new float[3][pool.maxLnId];
      System.arraycopy(table[0], 0, t[0], 0, len);
      System.arraycopy(table[1], 0, t[1], 0, len);
      System.arraycopy(table[2], 0, t[2], 0, len);
      table = t;
    }
    float[] cvals = color.getColorComponents(null);
    table[0][id] = cvals[0];
    table[1][id] = cvals[1];
    table[2][id] = cvals[2];
    if (m != null) m.setColor(color);
    try {
      cc.setTable(table);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Links the given measurement. */
  public void setMeasurement(Measurement m) { setMeasurement(m, -1); }

  /** Links the given measurement at the specified slice. */
  public void setMeasurement(Measurement m, int slice) {
    super.setMeasurement(m, slice);
    if (m != null) setColor(m.getColor());
  }

  protected void setValues(RealTuple[] v, boolean getTypes) {
    lineCell.disableAction();
    super.setValues(v, getTypes);
    lineCell.enableAction();
  }

  /** Gets the id number of the line. */
  public int getId() { return id; }

}
