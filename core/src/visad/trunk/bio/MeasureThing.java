//
// MeasureThing.java
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
import visad.*;
import visad.java2d.*;
import visad.java3d.*;

/**
 * MeasureThing maintains a collection of endpoints
 * for measuring distances in a field.
 */
public abstract class MeasureThing {

  /** Associated display. */
  protected DisplayImpl display;

  /** Associated data renderers. */
  protected DataRenderer[] renderers;

  /** Associated data references. */
  protected DataReferenceImpl[] refs;

  /** Flag marking visibility. */
  protected boolean visible;

  /** Associated measurement. */
  protected Measurement m;

  /** Associated slice number. */
  protected int slice;

  /** Cell that ties endpoint values to measurement values. */
  protected CellImpl cell;

  /** Domain type. */
  protected RealTupleType dtype;

  /** Domain component type for each endpoint. */
  protected RealType[] ptypes;

  /** Endpoint values. */
  protected RealTuple[] values;

  /** Number of endpoints. */
  protected int len;

  /** Dimensionality of endpoints. */
  protected int dim;

  /** Synchronization object for data references. */
  protected Object dataLock = new Object();

  /** Constructs a MeasureThing. */
  public MeasureThing(int length, int dimension)
    throws VisADException, RemoteException
  {
    this.len = length;
    this.dim = dimension;
    refs = new DataReferenceImpl[len];
    visible = true;
    values = new RealTuple[len];
    cell = new CellImpl() {
      public void doAction() {
        synchronized (dataLock) {
          for (int i=0; i<len; i++) values[i] = (RealTuple) refs[i].getData();
        }
        if (m != null) {
          RealTuple[] vals = new RealTuple[values.length];
          System.arraycopy(values, 0, vals, 0, values.length);
          m.setValues(vals);
        }
      }
    };
    cell.disableAction();
    for (int i=0; i<len; i++) {
      refs[i] = new DataReferenceImpl("p" + i);
      cell.addReference(refs[i]);
    }
    cell.enableAction();
  }

  /**
   * Adds the given data reference to the specified display
   * using a direct manipulation renderer.
   */
  public static DataRenderer addDirectManipRef(DisplayImpl d,
    DataReferenceImpl ref) throws VisADException, RemoteException
  {
    DataRenderer renderer = d instanceof DisplayImplJ3D ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref}, null);
    return renderer;
  }

  /** Adds the distance measuring data to the given display. */
  public abstract void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException;

  /** Sets the color. */
  public abstract void setColor(Color color);

  /** Sets the group. */
  public void setGroup(LineGroup group) {
    if (m != null) m.setGroup(group);
  }

  /** Shows or hides the endpoints. */
  protected void setVisible(boolean visible) {
    if (this.visible == visible) return;
    this.visible = visible;
    if (renderers != null) {
      for (int i=0; i<renderers.length; i++) renderers[i].toggle(visible);
    }
  }

  /** Hides the endpoints. */
  public void hide() { setMeasurement(null); }

  /** Initializes the MathType. */
  public void setType(RealTupleType domain)
    throws VisADException, RemoteException
  {
    setType(domain, true);
  }

  private void setType(RealTupleType domain, boolean fillVals)
    throws VisADException, RemoteException
  {
    dtype = domain;
    ptypes = new RealType[dim];
    for (int i=0; i<dim; i++) ptypes[i] = (RealType) domain.getComponent(i);
    if (fillVals) {
      Real[] r = new Real[dim];
      for (int i=0; i<dim; i++) r[i] = new Real(ptypes[i], Double.NaN);
      RealTuple tuple = new RealTuple(r);
      RealTuple[] vals = new RealTuple[len];
      for (int i=0; i<len; i++) vals[i] = tuple;
      setValues(vals, false);
    }
  }

  /** Links the given measurement. */
  public void setMeasurement(Measurement m) { setMeasurement(m, -1); }

  /** Links the given measurement. */
  public void setMeasurement(Measurement m, int slice) {
    if (this.m != m && this.m != null) this.m.removeThing(this);
    if (m != null) m.addThing(this);
    this.m = m;
    this.slice = slice;
    refresh();
  }

  /** Updates the endpoint values to match the linked measurement. */
  public void refresh() {
    if (m == null) setVisible(false);
    else {
      double[][] values = m.doubleValues();
      double[][] vals = new double[dim][len];
      int size = values.length < dim ? values.length : dim;
      for (int i=0; i<size; i++) {
        System.arraycopy(values[i], 0, vals[i], 0, len);
      }
      if (slice >= 0) {
        for (int j=0; j<len; j++) vals[dim - 1][j] = slice;
      }
      setValues(vals);
      setColor(m.getColor());
      setVisible(true);
    }
  }

  /** Sets the values of the endpoints. */
  public void setValues(double[][] vals) {
    if (vals.length != dim) {
      System.err.println("MeasureThing.setValues: invalid dimension");
      return;
    }
    for (int i=0; i<dim; i++) {
      if (vals[i].length != len) {
        System.err.println("MeasureThing.setValues: invalid length");
        return;
      }
    }
    Real[][] reals = new Real[len][dim];
    try {
      RealTuple[] tuples = new RealTuple[len];
      for (int j=0; j<len; j++) {
        for (int i=0; i<dim; i++) {
          reals[j][i] = new Real(ptypes[i], vals[i][j]);
        }
        tuples[j] = new RealTuple(reals[j]);
      }
      setValues(tuples, false);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Sets the values of the endpoints to match the given RealTuples. */
  public void setValues(RealTuple[] v) { setValues(v, true); }

  protected void setValues(RealTuple[] v, boolean getTypes) {
    if (v.length != len) {
      System.err.println("MeasureThing.setValues: invalid length");
      return;
    }

    // verify that new data is distinct from old data
    boolean equal = true;
    for (int i=0; i<len; i++) {
      if (!v[i].equals(values[i])) {
        equal = false;
        break;
      }
    }
    if (equal) return;

    try {
      if (getTypes) setType((RealTupleType) v[0].getType(), false);
      cell.disableAction();
      synchronized (dataLock) {
        for (int i=0; i<len; i++) {
          int vdim = v[i].getDimension();
          if (vdim != dim) {
            System.err.println("MeasureThing.setValues: " +
              "dimension doesn't match (" + vdim + " != " + dim + ")");
          }
          refs[i].setData(v[i]);
        }
      }
      cell.enableAction();
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Gets the associated measurement. */
  public Measurement getMeasurement() { return m; }

  /** Gets the values of the endpoints. */
  public RealTuple[] getValues() { return values; }

  /** Gets the domain type for the values. */
  public RealTupleType getDomain() { return dtype; }

  /** Gets the data references for the measurement. */
  public DataReference[] getReferences() { return refs; }

}
