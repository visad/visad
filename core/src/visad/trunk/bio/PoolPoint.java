//
// PoolPoint.java
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
import visad.java2d.*;
import visad.java3d.*;

/** PoolPoint is a single measurement pool endpoint. */
public class PoolPoint {

  // -- FIELDS --

  /** VisBio frame. */
  private VisBio bio;

  /** Associated data reference. */
  DataReferenceImpl ref;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Associated direct manipulation renderer. */
  private DataRenderer renderer;

  /** Dimensionality of the pool point's display. */
  private int dim;

  /** Measurement endpoint currently linked to this pool point. */
  MeasurePoint point;


  // -- CONSTRUCTOR --

  /** Constructs a point for use in the measurement pool. */
  public PoolPoint(VisBio biovis, DisplayImpl display,
    String name, int dimension)
  {
    bio = biovis;
    this.display = display;
    try { ref = new DataReferenceImpl("bio_" + name); }
    catch (VisADException exc) { exc.printStackTrace(); }
    dim = dimension;

    // update linked measurement endpoint when pool point changes
    final PoolPoint pt = this;
    CellImpl cell = new CellImpl() {
      public void doAction() {
        if (point == null) return;
        RealTuple tuple = (RealTuple) ref.getData();
        if (tuple == null) return;
        double[] v = tuple.getValues();

        // snap Z-coordinate to nearest slice
        if (dim == 3 && bio.sm.getSnap()) {
          int numSlices = bio.sm.getNumberOfSlices();
          int slice = (int) (v[2] + 0.5);
          if (slice < 0) slice = 0;
          else if (slice >= numSlices) slice = numSlices - 1;
          v[2] = slice;
        }

        point.setCoordinates(pt, v[0], v[1], dim == 3 ? v[2] : point.z);
      }
    };
    try { cell.addReference(ref); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }


  // -- API METHODS --

  /** Adds the point to the associated display. */
  public void init() throws VisADException, RemoteException {
    renderer = display instanceof DisplayImplJ3D ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    renderer.setPickCrawlToCursor(false);
    renderer.suppressExceptions(true);
    renderer.toggle(false);
    display.addReferences(renderer, ref);
  }

  /** Toggles the renderer visible or invisible. */
  public void toggle(boolean on) { renderer.toggle(on); }

  /** Refreshes the point's coordinates to match the linked endpoint. */
  public void refresh() {
    if (point == null) {
      renderer.toggle(false);
      return;
    }
    renderer.toggle(true);
    try {
      RealTuple tuple = (RealTuple) ref.getData();

      if (tuple != null) {
        double[] v = tuple.getValues();
        if (v[0] == point.x && v[1] == point.y) {
          if (dim == 2 || v[2] == point.z) return;
        }
      }
      if (dim == 3) {
        ref.setData(new RealTuple(new Real[] {
          new Real(bio.sm.dtypes[0], point.x),
          new Real(bio.sm.dtypes[1], point.y),
          new Real(bio.sm.dtypes[2], point.z)
        }));
      }
      else { // dim == 2
        ref.setData(new RealTuple(new Real[] {
          new Real(bio.sm.dtypes[0], point.x),
          new Real(bio.sm.dtypes[1], point.y)
        }));
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

}
