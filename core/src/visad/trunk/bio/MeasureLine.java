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

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.java2d.*;
import visad.java3d.*;

/**
 * MeasureLine maintains a (p1, line, p2) triple of
 * DataReferences for measuring distances in a field.
 */
public class MeasureLine {

  /** Debugging flag. */
  private static final boolean DEBUG = false;

  /** List of all measurement lines. */
  private static final Vector lines = new Vector();

  /** First free id number for lines. */
  private static int maxId = 0;
  
  /** Id number for the line. */
  int id;

  /** Data reference for first endpoint. */
  DataReferenceImpl ref_p1;

  /** Data reference for second endpoint. */
  DataReferenceImpl ref_p2;

  /** Data reference for connecting line. */
  private DataReferenceImpl ref_line;

  /** Cell that ties line to endpoints. */
  private CellImpl cell;

  /** Associated display. */
  private DisplayImpl display;

  /** Associated measurement. */
  private Measurement m;

  /** Domain type. */
  private RealTupleType dtype;

  /** Domain component types. */
  RealType[] ptypes;

  /** Current Data value for first point. */
  RealTuple p1;

  /** Current Data value for second point. */
  RealTuple p2;

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
          double[][] values = updateValues();
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
            FunctionType ftype =
              new FunctionType(dtype, FileSeriesWidget.COLOR_TYPE);
            Gridded2DSet set = new Gridded2DSet(dtype, samps, 2);
            FlatField field = new FlatField(ftype, set);
            field.setSamples(new double[][] {{id, id}});
            ref_line.setData(field);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
      }
    };
    cell.addReference(ref_p1);
    cell.addReference(ref_p2);

    id = maxId++;
    lines.add(this);
    if (DEBUG) System.out.println("Line " + id + ": created.");
  }

  /** Adds the distance measuring data to the given display. */
  public void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    if (display != null) {
      // remove measuring data from old display
      display.removeReference(ref_p1);
      display.removeReference(ref_p2);
      display.removeReference(ref_line);
    }
    display = d;
    if (d == null) return;

    // add measuring data to new display
    boolean j3d = d instanceof DisplayImplJ3D;
    d.getGraphicsModeControl().setPointSize(5.0f);
    d.getDisplayRenderer().setPickThreshhold(5.0f);

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

  /** Sets the line's color for the given display to the specified values. */
  public void setColor(Color color) {
    if (display == null) return;
    Vector v = display.getControls(ColorControl.class);
    ColorControl cc = (ColorControl) v.lastElement();
    if (cc == null) return;
    float[][] table = cc.getTable();
    int len = table[0].length;
    if (len != maxId) {
      // adjust table as necessary
      float[][] t = new float[3][maxId];
      int s = len < maxId ? len : maxId;
      System.arraycopy(table[0], 0, t[0], 0, s);
      System.arraycopy(table[1], 0, t[1], 0, s);
      System.arraycopy(table[2], 0, t[2], 0, s);
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

  /** Hides the endpoints of this line. */
  public void hide() {
    String debug = "";
    setMeasurement(null);
    if (DEBUG) debug = debug + "Line " + id + ": hiding line: ";
    if (p1 != null && p2 != null) {
      int len = ptypes.length;
      double[][] values = new double[len][2];
      for (int i=0; i<len; i++) {
        values[i][0] = Double.NaN;
        values[i][1] = Double.NaN;
      }
      setValues(values);
      if (DEBUG) debug = debug + "values set.";
    }
    else if (DEBUG) debug = debug + "cannot hide.";
    if (DEBUG) System.out.println(debug);
  }

  /** Initializes the line's MathType. */
  public void setType(RealTupleType domain)
    throws VisADException, RemoteException
  {
    setType(domain, true);
    if (DEBUG) System.out.println("Line " + id + ": type initialized.");
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
      p1 = p2 = new RealTuple(r);
      setValues(p1, p2, false);
    }
  }

  /** Links the given measurement with this line. */
  public void setMeasurement(Measurement m) {
    this.m = m;
    if (m != null) {
      setValues(m.values[0], m.values[1]);
      setColor(m.color);
      if (DEBUG) System.out.println("Line " + id + ": measurement set.");
    }
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
      if (getTypes) setType((RealTupleType) v1.getType(), false);
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
  private double[][] updateValues() {
    double[][] values = null;
    String debug = "";
    synchronized (dataLock) {
      p1 = (RealTuple) ref_p1.getData();
      p2 = (RealTuple) ref_p2.getData();
      if (DEBUG) debug = debug + "Line " + id + ": updating values: ";
      if (m != null) {
        m.values = new RealTuple[] {p1, p2};
        if (DEBUG) debug = debug + "measurement values set, ";
      }
      else if (DEBUG) debug = debug + "measurement is null, ";
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
        if (DEBUG) debug = debug + "values extracted from data.";
      }
      else if (DEBUG) debug = debug + "data is null.";
    }
    if (DEBUG) System.out.println(debug);
    return values;
  }

  /** Gets the line's associated measurement. */
  public Measurement getMeasurement() { return m; }

  /** Gets the values of the endpoints. */
  public RealTuple[] getValues() { return new RealTuple[] {p1, p2}; }

  /** Gets the domain type for the line's values. */
  public RealTupleType getDomain() { return dtype; }

}
