//
// MeasureLine.java
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
import visad.java2d.DirectManipulationRendererJ2D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

/**
 * MeasureLine maintains a (p1, line, p2) triple of
 * DataReferences for measuring distances in a field.
 */
public class MeasureLine {

  /** Data reference for first endpoint. */
  private DataReferenceImpl ref_p1;

  /** Data reference for second endpoint. */
  private DataReferenceImpl ref_p2;

  /** Data reference for connecting line. */
  private DataReferenceImpl ref_line;

  /** Cell that ties line to endpoints. */
  private CellImpl cell;

  /** Associated measurement. */
  private Measurement m;

  /** Domain type. */
  private RealTupleType dtype;

  /** Domain component types. */
  private RealType[] ptypes;

  /** Current Data value for first point. */
  private RealTuple p1;

  /** Current Data value for second point. */
  private RealTuple p2;

  /** Current values of endpoints. */
  private double[][] values;

  /** Synchronization object for DataReferences. */
  private Object dataLock = new Object();

  /** Synchronization object for CellImpl. */
  private Object cellLock = new Object();

  /** Constructs a measurement object to match the given field. */
  public MeasureLine() throws VisADException, RemoteException {
    ref_p1 = new DataReferenceImpl("p1");
    ref_p2 = new DataReferenceImpl("p2");
    ref_line = new DataReferenceImpl("line");

    cell = new CellImpl() {
      public void doAction() {
        if (dtype != null) {
          updateValues();
          if (values == null) return;

          // convert doubles to floats
          float[][] samps = new float[values.length][values[0].length];
          for (int i=0; i<values.length; i++) {
            for (int j=0; j<values[0].length; j++) {
              samps[i][j] = (float) values[i][j];
            }
          }

          // set line data
          try {
            ref_line.setData(new GriddedSet(dtype, samps, new int[] {2}));
          }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
      }
    };
    cell.addReference(ref_p1);
    cell.addReference(ref_p2);
  }

  /** Adds the distance measuring data to the given display. */
  public void addToDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    boolean j3d = d instanceof DisplayImplJ3D;
    d.getGraphicsModeControl().setPointSize(5.0f);
    d.getDisplayRenderer().setPickThreshhold(Float.MAX_VALUE / 4);

    // add first endpoint
    DataRenderer renderer = j3d ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref_p1}, null);

    // add second endpoint
    renderer = j3d ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref_p2}, null);

    // add connecting line
    d.addReference(ref_line);
  }

  /** Removes this distance measuring data from the given display. */
  public void removeFromDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    d.removeReference(ref_p1);
    d.removeReference(ref_p2);
    d.removeReference(ref_line);
  }

  /** Hides the endpoints of this line. */
  public void hide() {
    setMeasurement(null);
    if (p1 != null && p2 != null) {
      int len = ptypes.length;
      double[][] values = new double[len][2];
      for (int i=0; i<len; i++) {
        values[i][0] = Double.MAX_VALUE / 2;
        values[i][1] = Double.MAX_VALUE / 2;
      }
      setValues(values);
    }
  }

  /** Links the given measurement with this line. */
  public void setMeasurement(Measurement m) {
    this.m = m;
    if (m != null) setValues(m.values[0], m.values[1]);
  }

  /** Sets the values of the endpoints. */
  public void setValues(double[][] values) {
    int len = values.length;
    if (len != ptypes.length) {
      System.err.println("MeasureLine.setValues: lengths don't match!");
      return;
    }
    Real[] p1r = new Real[len];
    Real[] p2r = new Real[len];
    try {
      for (int i=0; i<len; i++) {
        p1r[i] = new Real(ptypes[i], values[i][0]);
        p2r[i] = new Real(ptypes[i], values[i][1]);
      }
      setValues(new RealTuple(p1r), new RealTuple(p2r), false);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Sets the values of the endpoints to match the given RealTuples. */
  public void setValues(RealTuple v1, RealTuple v2) {
    setValues(v1, v2, true);
  }

  private void setValues(RealTuple v1, RealTuple v2, boolean getTypes) {
    try {
      if (getTypes) {
        // initialize MathTypes
        RealTupleType domain = (RealTupleType) v1.getType();
        dtype = domain;
        int dim = domain.getDimension();
        ptypes = new RealType[dim];
        for (int i=0; i<dim; i++) {
          ptypes[i] = (RealType) domain.getComponent(i);
        }
      }
      // set data
      synchronized (cellLock) {
        cell.disableAction();
        synchronized (dataLock) {
          ref_p1.setData(v1);
          ref_p2.setData(v2);
        }
        cell.enableAction();
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Updates various internal fields. */
  private void updateValues() {
    synchronized (dataLock) {
      p1 = (RealTuple) ref_p1.getData();
      p2 = (RealTuple) ref_p2.getData();
      values = null;
      if (m != null) m.values = new RealTuple[] {p1, p2};
      if (p1 != null && p2 != null) {
        int len = p1.getDimension();
        values = new double[len][2];
        try {
          for (int i=0; i<len; i++) {
            Real r1 = (Real) p1.getComponent(i);
            Real r2 = (Real) p2.getComponent(i);
            values[i][0] = r1.getValue();
            values[i][1] = r2.getValue();
          }
        }
        catch (VisADException exc) {
          exc.printStackTrace();
          values = null;
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
          values = null;
        }
      }
    }
  }

  /** Gets the values of the endpoints. */
  public RealTuple[] getValues() { return new RealTuple[] {p1, p2}; }

}
