//
// MeasurePoint.java
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

/** MeasurePoint maintains a DataReference for measuring points in a field. */
public class MeasurePoint {

  /** First free id number for points. */
  private static int firstFreeId = 1;
  
  /** Id number for the point. */
  int id;

  /** Data reference for the point. */
  DataReferenceImpl ref;

  /** Associated measurement. */
  private Measurement m;

  /** Domain type. */
  private RealTupleType dtype;

  /** Domain component types. */
  RealType[] ptypes;

  /** Current Data value for the point. */
  RealTuple p;

  /** Synchronization object for DataReferences. */
  private Object dataLock = new Object();

  /** Constructs a measurement object to match the given field. */
  public MeasurePoint() throws VisADException, RemoteException {
    ref = new DataReferenceImpl("ref");
    id = firstFreeId++;
  }

  /** Adds the distance measuring data to the given display. */
  public void addToDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    boolean j3d = d instanceof DisplayImplJ3D;
    d.getGraphicsModeControl().setPointSize(5.0f);
    d.getDisplayRenderer().setPickThreshhold(5.0f);

    // add point
    DataRenderer renderer = j3d ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref}, null);
  }

  /** Removes this distance measuring data from the given display. */
  public void removeFromDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    d.removeReference(ref);
  }

  /** Hides this point. */
  public void hide() {
    setMeasurement(null);
    if (p != null) {
      int len = ptypes.length;
      double[][] values = new double[len][1];
      for (int i=0; i<len; i++) values[i][0] = Double.NaN;
      setValues(values);
    }
  }

  /** Initializes the point's MathType. */
  public void setType(RealTupleType domain)
    throws VisADException, RemoteException
  {
    setType(domain, true);
  }

  private void setType(RealTupleType domain, boolean fillVals)
    throws VisADException, RemoteException
  {
    dtype = domain;
    int dim = domain.getDimension();
    ptypes = new RealType[dim];
    Real[] r = new Real[dim];
    for (int i=0; i<dim; i++) {
      ptypes[i] = (RealType) domain.getComponent(i);
      r[i] = new Real(ptypes[i], Double.NaN);
    }
    if (fillVals) {
      p = new RealTuple(r);
      setValues(p, false);
    }
  }

  /** Links the given measurement with this point. */
  public void setMeasurement(Measurement m) {
    this.m = m;
    if (m != null) setValues(m.values[0]);
  }

  /** Sets the values of the endpoints. */
  public void setValues(double[][] values) {
    int len = values.length;
    if (len != ptypes.length) {
      System.err.println("MeasurePoint.setValues: lengths don't match!");
      return;
    }
    Real[] r = new Real[len];
    try {
      for (int i=0; i<len; i++) r[i] = new Real(ptypes[i], values[i][0]);
      setValues(new RealTuple(r), false);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Sets the value of the point to match the given RealTuple. */
  public void setValues(RealTuple v) { setValues(v, true); }

  private void setValues(RealTuple v, boolean getTypes) {
    try {
      if (getTypes) setType((RealTupleType) v.getType(), false);
      synchronized (dataLock) { ref.setData(v); }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Gets the point's associated measurement. */
  public Measurement getMeasurement() { return m; }

  /** Gets the value of the point. */
  public RealTuple getValue() { return p; }

  /** Gets the domain type for the point's value. */
  public RealTupleType getDomain() { return dtype; }

}
