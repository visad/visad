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

/** PoolPoint is a single, direct-manipulation DataReference. */
public class PoolPoint {

  // -- FIELDS --

  /** Associated data reference. */
  public DataReferenceImpl ref;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Associated direct manipulation renderer. */
  private DataRenderer renderer;


  // -- CONSTRUCTOR --

  /** Constructs a pool of measurements. */
  public PoolPoint(DisplayImpl display, String name) {
    this.display = display;
    try {
      ref = new DataReferenceImpl("bio_" + name);
      renderer = display instanceof DisplayImplJ3D ?
        (DataRenderer) new DirectManipulationRendererJ3D() :
        (DataRenderer) new DirectManipulationRendererJ2D();
      renderer.suppressExceptions(true);
      renderer.toggle(false);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
  }


  // -- API METHODS --

  /** Toggles the visibility of the point in the display. */
  public void toggle(boolean visible) { renderer.toggle(visible); }

  /** Adds the point to the associated display. */
  public void init() throws VisADException, RemoteException {
    display.addReferences(renderer, ref);
  }

}
