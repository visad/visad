//
// SelectionBox.java
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

import java.rmi.RemoteException;
import visad.*;

/** SelectionBox is an object for visibly selecting a MeasureThing. */
public class SelectionBox {

  // -- FIELDS --

  /** Data reference for the selection object. */
  private DataReferenceImpl ref;

  /** Data renderer for the data reference. */
  private DataRenderer renderer;

  /** Currently selected thing. */
  private MeasureThing thing;

  /** Associated display. */
  private DisplayImpl display;

  /** Computation cell for linking selection with measurement object. */
  private CellImpl cell;


  // -- CONSTRUCTOR --

  /** Constructs a selection box. */
  public SelectionBox(DisplayImpl d) {
    display = d;

    // construct data references
    try { ref = new DataReferenceImpl("bio_box"); }
    catch (VisADException exc) { exc.printStackTrace(); }

    // set up cell that links selection box with selected object
    cell = new CellImpl() {
      public void doAction() {
        synchronized (cell) {
          DataImpl box = null;
          if (thing == null) {
            // no measurement (no selection)
          }
          else if (thing.getLength() == 1) {
            // measurement is a point
            RealTuple[] values = thing.getValues();
            box = values[0];
          }
          else {
            // measurement is one or more connected lines
            RealTuple[] values = thing.getValues();
            int len = values.length == 2 ? 2 : values.length + 1;
            int dim = values[0].getDimension();
            float[][] samples = new float[dim][len];
            for (int j=0; j<len; j++) {
              double[] v = values[j % values.length].getValues();
              for (int i=0; i<dim; i++) samples[i][j] = (float) v[i];
            }
            MathType type = values[0].getType();

            try {
              if (dim == 3) {
                box = (GriddedSet) new Gridded3DSet(type,
                  samples, len, null, null, null, false);
              }
              else {
                box = (GriddedSet) new Gridded2DSet(type,
                  samples, len, null, null, null, false);
              }
            }
            catch (VisADException exc) { exc.printStackTrace(); }
          }

          if (box == null) return;
          try { ref.setData(box); }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
      }
    };
  }


  // -- API METHODS --

  /**
   * Selects the given measurement object.
   *
   * Currently, only measurement objects with one or two endpoints
   * are supported.
   */
  public void select(MeasureThing thing) {
    synchronized (cell) {
      try {
        cell.disableAction();
        this.thing = thing;
        cell.removeAllReferences();
        if (thing == null) {
          // hide selection box
          renderer.toggle(false);
        }
        else {
          // select given object
          PoolPoint[] pts = thing.getPoints();
          for (int i=0; i<pts.length; i++) cell.addReference(pts[i].ref);
          renderer.toggle(true);
        }
        cell.enableAction();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

  /** Adds the selection object to its display. */
  public void init() throws VisADException, RemoteException {
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    renderer = displayRenderer.makeDefaultRenderer();
    renderer.suppressExceptions(true);
    renderer.toggle(false);
    ConstantMap[] maps = {
      new ConstantMap(1.0f, Display.Red),
      new ConstantMap(1.0f, Display.Green),
      new ConstantMap(0.0f, Display.Blue),
      new ConstantMap(3.0f, Display.LineWidth),
      new ConstantMap(8.0f, Display.PointSize)
    };
    display.addReferences(renderer, ref, maps);
  }

  /** Gets the currently selected measurement object. */
  public MeasureThing getSelection() { return thing; }

}
