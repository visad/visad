//
// MeasureThing.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

  /** Associated data references. */
  protected DataReferenceImpl[] refs;

  /** Associated measurement. */
  protected Measurement m;

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

  /** Constructs a MeasureThing. */
  public MeasureThing(int length, int dimension)
    throws VisADException, RemoteException
  {
    this.len = length;
    this.dim = dimension;
    refs = new DataReferenceImpl[len];
    values = new RealTuple[len];
    cell = new CellImpl() {
      public void doAction() {
        for (int i=0; i<len; i++) values[i] = (RealTuple) refs[i].getData();
        if (m != null) m.values = values;
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
  public static void addDirectManipRef(DisplayImpl d, DataReferenceImpl ref)
    throws VisADException, RemoteException
  {
    DataRenderer renderer = d instanceof DisplayImplJ3D ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref}, null);
  }

  /** Adds the distance measuring data to the given display. */
  public abstract void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException;

  /** Sets the group. */
  public void setGroup(LineGroup group) {
    if (m != null) m.setGroup(group);
  }

  /** Hides the endpoints. */
  public void hide() {
    setMeasurement(null);
    double[][] vals = new double[dim][len];
    for (int i=0; i<dim; i++) {
      for (int j=0; j<len; j++) vals[i][j] = Double.NaN;
    }
    setValues(vals);
  }

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
    Real[] r = new Real[dim];
    for (int i=0; i<dim; i++) {
      ptypes[i] = (RealType) domain.getComponent(i);
      r[i] = new Real(ptypes[i], Double.NaN);
    }
    if (fillVals) {
      RealTuple tuple = new RealTuple(r);
      RealTuple[] vals = new RealTuple[len];
      for (int i=0; i<len; i++) vals[i] = tuple;
      setValues(vals, false);
    }
  }

  /** Links the given measurement. */
  public void setMeasurement(Measurement m) {
    this.m = m;
    if (m != null) setValues(m.values);
  }

  /** Sets the values of the endpoints. */
  public void setValues(double[][] values) {
    if (values.length != dim) {
      System.err.println("MeasureThing.setValues: invalid dimension");
      return;
    }
    for (int i=0; i<len; i++) {
      if (values[i].length != len) {
        System.err.println("MeasureThing.setValues: invalid length");
        return;
      }
    }
    Real[][] reals = new Real[len][dim];
    try {
      RealTuple[] tuples = new RealTuple[len];
      for (int j=0; j<len; j++) {
        for (int i=0; i<dim; i++) {
          reals[j][i] = new Real(ptypes[i], values[i][j]);
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
    try {
      if (getTypes) setType((RealTupleType) v[0].getType(), false);
      for (int i=0; i<len; i++) refs[i].setData(v[i]);
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
