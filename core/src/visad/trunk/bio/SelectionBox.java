//
// SelectionBox.java
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

import java.rmi.RemoteException;
import visad.*;

/**
 * SelectionBox maintains a collection of points
 * for selecting a MeasureLine object.
 */
public class SelectionBox {

  private static final int DISTANCE = 10;

  /** Data reference for first endpoint. */
  private DataReferenceImpl[] refs = new DataReferenceImpl[4];

  /** Currently selected thing. */
  private MeasureThing thing;

  /** Computation cell for linking selection with measurement object. */
  private CellImpl cell;

  /** Associated display. */
  private DisplayImpl display;

  /** Constructs a selection box. */
  public SelectionBox() throws VisADException, RemoteException {
    for (int i=0; i<4; i++) {
      refs[i] = new DataReferenceImpl("box" + i);
      refs[i].setData(new Real(Double.NaN));
    }
    cell = new CellImpl() {
      public void doAction() {
        synchronized (this) {
          Real[][] reals = null;
          if (thing == null) {
            // no measurement (no selection)
            reals = new Real[4][2];
            for (int i=0; i<4; i++) {
               for (int j=0; j<2; j++) reals[i][j] = new Real(Double.NaN);
            }
          }
          else if (thing instanceof MeasurePoint) {
            // measurement is a point
            RealTuple[] values = thing.getValues();
            RealTuple p = values[0];

            if (p != null) {
              try {
                Real rx = (Real) p.getComponent(0);
                Real ry = (Real) p.getComponent(1);

                RealType rtx = (RealType) rx.getType();
                RealType rty = (RealType) ry.getType();

                double px = rx.getValue();
                double py = ry.getValue();

                double vx = DISTANCE;
                double vy = DISTANCE;

                reals = new Real[][] {
                  {new Real(rtx, px - vx), new Real(rty, py - vy)},
                  {new Real(rtx, px + vx), new Real(rty, py - vy)},
                  {new Real(rtx, px - vx), new Real(rty, py + vy)},
                  {new Real(rtx, px + vx), new Real(rty, py + vy)},
                };
              }
              catch (VisADException exc) { exc.printStackTrace(); }
              catch (RemoteException exc) { exc.printStackTrace(); }
            }
          }
          else if (thing instanceof MeasureLine) {
            // measurement is a line
            RealTuple[] values = thing.getValues();
            RealTuple p1 = values[0];
            RealTuple p2 = values[1];

            if (p1 != null && p2 != null) {
              try {
                Real r1x = (Real) p1.getComponent(0);
                Real r1y = (Real) p1.getComponent(1);
                Real r2x = (Real) p2.getComponent(0);
                Real r2y = (Real) p2.getComponent(1);

                RealType rtx = (RealType) r1x.getType();
                RealType rty = (RealType) r1y.getType();

                double p1x = r1x.getValue();
                double p1y = r1y.getValue();
                double p2x = r2x.getValue();
                double p2y = r2y.getValue();

                double slope = (p1x - p2x) / (p2y - p1y);
                double vx = DISTANCE / Math.sqrt(slope * slope + 1);
                double vy = slope * vx;

                reals = new Real[][] {
                  {new Real(rtx, p1x - vx), new Real(rty, p1y - vy)},
                  {new Real(rtx, p1x + vx), new Real(rty, p1y + vy)},
                  {new Real(rtx, p2x - vx), new Real(rty, p2y - vy)},
                  {new Real(rtx, p2x + vx), new Real(rty, p2y + vy)}
                };
              }
              catch (VisADException exc) { exc.printStackTrace(); }
              catch (RemoteException exc) { exc.printStackTrace(); }
            }
          }

          // CTR: TODO: figure out why box never becomes visible
          if (reals == null) return;
          RealTuple[] tuples = new RealTuple[refs.length];
          for (int i=0; i<refs.length; i++) {
            try {
              tuples[i] = new RealTuple(reals[i]);
              refs[i].setData(tuples[i]);
            }
            catch (VisADException exc) { exc.printStackTrace(); }
            catch (RemoteException exc) { exc.printStackTrace(); }
          }
        }
      }
    };
  }

  /** Adds the selection box to the given display. */
  public void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    if (display != null) {
      // remove selection box from old display
      display.disableAction();
      for (int i=0; i<4; i++) display.removeReference(refs[i]);
      display.enableAction();
    }
    display = d;
    if (d == null) return;

    // add selection box to new display
    final ConstantMap[] maps = {
      new ConstantMap(1.0f, Display.Red),
      new ConstantMap(1.0f, Display.Green),
      new ConstantMap(0.0f, Display.Blue),
      new ConstantMap(3.0f, Display.PointSize)
    };
    d.disableAction();
    for (int i=0; i<4; i++) d.addReference(refs[i], maps);
    d.enableAction();
  }

  /** Selects the given measurement object. */
  public void select(MeasureThing thing) {
    synchronized (cell) {
      try {
        cell.disableAction();
        this.thing = thing;
        cell.removeAllReferences();
        if (thing != null) {
          DataReference[] refs = thing.getReferences();
          for (int i=0; i<refs.length; i++) cell.addReference(refs[i]);
        }
        cell.enableAction();
        if (thing == null) cell.doAction();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

}
