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

  private static final int DISTANCE = 15;

  /** Data reference for first endpoint. */
  private DataReferenceImpl[] refs = new DataReferenceImpl[6];

  /** Currently selected thing. */
  private MeasureThing thing;

  /** Computation cell for linking selection with measurement object. */
  private CellImpl cell;

  /** Associated display. */
  private DisplayImpl display;

  /** Constructs a selection box. */
  public SelectionBox() throws VisADException, RemoteException {
    for (int i=0; i<refs.length; i++) {
      refs[i] = new DataReferenceImpl("box" + i);
      refs[i].setData(new Real(Double.NaN));
    }
    cell = new CellImpl() {
      public void doAction() {
        synchronized (cell) {
          Real[][] reals = null;
          if (thing == null) {
            // no measurement (no selection)
            reals = new Real[refs.length][2];
            for (int i=0; i<refs.length; i++) {
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
                  {new Real(Double.NaN), new Real(Double.NaN)},
                  {new Real(Double.NaN), new Real(Double.NaN)}
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

                double b1x = p1x - vx;
                double b1y = p1y - vy;
                double b2x = p2x - vx;
                double b2y = p2y - vy;
                double b3x = p1x + vx;
                double b3y = p1y + vy;
                double b4x = p2x + vx;
                double b4y = p2y + vy;
                double b5x = (b1x + b2x) / 2;
                double b5y = (b1y + b2y) / 2;
                double b6x = (b3x + b4x) / 2;
                double b6y = (b3y + b4y) / 2;

                reals = new Real[][] {
                  {new Real(rtx, b1x), new Real(rty, b1y)},
                  {new Real(rtx, b2x), new Real(rty, b2y)},
                  {new Real(rtx, b3x), new Real(rty, b3y)},
                  {new Real(rtx, b4x), new Real(rty, b4y)},
                  {new Real(rtx, b5x), new Real(rty, b5y)},
                  {new Real(rtx, b6x), new Real(rty, b6y)}
                };
              }
              catch (VisADException exc) { exc.printStackTrace(); }
              catch (RemoteException exc) { exc.printStackTrace(); }
            }
          }

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
      for (int i=0; i<refs.length; i++) display.removeReference(refs[i]);
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
    for (int i=0; i<refs.length; i++) d.addReference(refs[i], maps);
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
