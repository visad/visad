//
// MeasureThing.java
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
import visad.*;
import visad.java2d.*;
import visad.java3d.*;

/**
 * MeasureThing maintains a collection of line-connected
 * points for measuring distances in a field.
 */
public class MeasureThing {

  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Associated pool of measurements. */
  private MeasurePool pool;

  /** Associated measurement. */
  private Measurement m;

  /** Associated measurement pool points. */
  private PoolPoint[] pts;

  /** Cell that ties endpoint values to measurement values. */
  private CellImpl cell;

  /** Flag marking whether to ignore next cell update. */
  private boolean ignoreNext;


  // -- CONSTRUCTOR --

  /** Constructs a MeasureThing. */
  public MeasureThing(BioVisAD biovis, MeasurePool pool, Measurement mm) {
    this.bio = biovis;
    this.pool = pool;
    this.m = mm;
    pts = pool.lease(this);
    m.addThing(this);

    // cell for updating Measurement whenever endpoints change
    final MeasureThing thing = this;
    final MeasurePool mpool = pool;
    final int dim = pool.getDimension();
    final int slices = bio.sm.getNumberOfSlices();
    ignoreNext = true;
    cell = new CellImpl() {
      public void doAction() {
        if (ignoreNext) ignoreNext = false;
        else {
          RealTuple[] values = getValues();
          for (int i=0; i<values.length; i++) if (values[i] == null) return;
          MeasureThing t;
          if (dim == 2) {
            values = BioVisAD.copy(values);
            t = thing;
          }
          else {
            // snap measurement endpoints to nearest slice plane
            try {
              for (int i=0; i<values.length; i++) {
                Real[] reals = values[i].getRealComponents();
                for (int j=0; j<reals.length-1; j++) {
                  reals[j] = (Real) reals[j].clone();
                }
                Real r = reals[reals.length - 1];
                double value;
                if (m.stdId >= 0) {
                  RealTuple[] mvals = m.getValues();
                  Real[] mreals = mvals[i].getRealComponents();
                  value = mreals[reals.length - 1].getValue();
                }
                else {
                  value = Math.round(r.getValue());
                  if (value < 0) value = 0;
                  if (value >= slices) value = slices - 1;
                }
                reals[reals.length - 1] = new Real((RealType) r.getType(),
                  value, r.getUnit(), r.getError());
                values[i] = new RealTuple(reals);
              }
            }
            catch (VisADException exc) { exc.printStackTrace(); }
            catch (RemoteException exc) { exc.printStackTrace(); }
            t = null;
          }
          m.setValues(values, t);
        }
        mpool.valuesChanged(thing);
        bio.mm.changed = true;
      }
    };
    cell.disableAction();
    try { for (int i=0; i<pts.length; i++) cell.addReference(pts[i].ref); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    ignoreNext = true;
    cell.enableAction();
    refresh();
  }


  // -- API METHODS --

  /** Sets the color. */
  public void setColor(Color color) {
    m.setColor(color);
    bio.mm.changed = true;
  }

  /** Sets the group. */
  public void setGroup(MeasureGroup group) {
    m.setGroup(group);
    bio.mm.changed = true;
  }

  /** Prepares this measurement object to cease being used. */
  public void destroy() {
    try { cell.removeAllReferences(); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    m.removeThing(this);
  }

  /**
   * Terminates this measurement object's measurement, causing all linked
   * measurement objects to return their points to the measurement pool.
   */
  public void kill() { m.kill(); }

  /** Updates the endpoint values to match the linked measurement. */
  public void refresh() {
    if (cell == null) return;
    if (m.killed) pool.release(this);
    else {
      RealTuple[] values = BioVisAD.copy(m.getValues());
      try {
        int dim = pool.getDimension();
        int slice = pool.getSlice();
        cell.disableAction();
        for (int i=0; i<values.length; i++) {
          // for 2-D, toggle point on only if slice matches
          double[] s = values[i].getValues();
          pts[i].ref.setData(values[i]);
          pts[i].toggle(dim != 2 || s[2] == slice);
        }
        ignoreNext = true;
        cell.enableAction();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

  /** Gets the associated measurement. */
  public Measurement getMeasurement() { return m; }

  /** Gets the measurement color. */
  public Color getColor() { return m.getColor(); }

  /** Gets the number of endpoints. */
  public int getLength() { return m.getLength(); }

  /** Gets the data references for the measurement. */
  public PoolPoint[] getPoints() { return pts; }

  /** Gets the endpoint values. */
  public RealTuple[] getValues() {
    RealTuple[] values = new RealTuple[pts.length];
    for (int i=0; i<pts.length; i++) {
      values[i] = (RealTuple) pts[i].ref.getData();
    }
    return values;
  }

}
