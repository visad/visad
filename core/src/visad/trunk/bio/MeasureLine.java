//
// MeasureLine.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

  /** Associated measurement pool. */
  private MeasurePool pool;

  /** Data reference for connecting line. */
  private DataReferenceImpl ref_line;

  /** Data reference for X. */
  private DataReferenceImpl ref_x;

  /** Cell that ties line to endpoints. */
  private CellImpl lineCell;


  /** Constructs a measurement line. */
  public MeasureLine(int dim) throws VisADException, RemoteException {
    this(dim, null);
  }

  /** Constructs a measurement line with an associated measurement pool. */
  public MeasureLine(int dim, MeasurePool pool)
    throws VisADException, RemoteException
  {
    super(2, dim);
    ref_line = new DataReferenceImpl("line");
    if (dim == 2) ref_x = new DataReferenceImpl("x");
    this.pool = pool;
    id = pool == null ? 0 : pool.maxLnId++;

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
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
    };
    lineCell.addReference(refs[0]);
    lineCell.addReference(refs[1]);

    lines.add(this);
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
      if (dim == 2) display.removeReference(ref_x);
    }
    display = d;
    if (d == null) return;

    // configure display appropriately
    DisplayRenderer dr = d.getDisplayRenderer();
    d.getGraphicsModeControl().setPointSize(5.0f);
    dr.setPickThreshhold(5.0f);

    // add endpoints
    renderers = new DataRenderer[dim == 2 ? 4 : 3];
    renderers[0] = addDirectManipRef(d, refs[0]);
    renderers[1] = addDirectManipRef(d, refs[1]);

    // add connecting line
    renderers[2] = dr.makeDefaultRenderer();
    d.addReferences(renderers[2], new DataReference[] {ref_line}, null);

    if (dim == 2) {
      // add X lines
      renderers[3] = dr.makeDefaultRenderer();
      d.addReferences(renderers[3], new DataReference[] {ref_x}, null);
    }

    // hide null data warnings
    for (int i=0; i<renderers.length; i++) {
      renderers[i].suppressExceptions(true);
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
  public void setMeasurement(Measurement m) {
    super.setMeasurement(m);
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
