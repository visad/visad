//
// CutAndPasteFields.java
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

package visad.bom;

import visad.*;
import visad.util.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

/*
sequence of operation
1. method (may be called by JButton) to start process
2. enable RubberBandBoxRendererJ3D
3. user selects rectangle
4. disable RubberBandBoxRendererJ3D
   draw draggable rectangle (drag at corner?)
5. user may drag or change time step
   drag release, updates grid at location/time
   may repeat drag or change time step
6. method (may be called by JButton) to stop process

constructor input:
1. FieldImpl: (Time -> ((x, y) -> value)) or
   FlatField: ((x, y) -> value)
2. DisplayImpl (check ScalarMaps)
*/

/**
   CutAndPasteFields is the VisAD class for cutting and pasting
   regions of fields.<p>
*/
public class CutAndPasteFields extends Object {

  private DataImpl grids = null;
  private DisplayImpl display = null;

  private boolean anim = false;

  private boolean debug = true;

  /**
     gs has MathType (Time -> ((x, y) -> value)) or ((x, y) -> value)
  */
  public CutAndPasteFields(DataImpl gs, DisplayImplJ3D d)
         throws VisADException, RemoteException {
    grids = gs;
    display = d;
  }

  // BoxDragRendererJ3D button release
  public void release() {
/*
    try {
    }
    catch (VisADException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
    catch (RemoteException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
*/
  }

}

class BoxDragRendererJ3D extends DirectManipulationRendererJ3D {

  CutAndPasteFields cp;

  BoxDragRendererJ3D(CutAndPasteFields c) {
    super();
    cp = c;
  }

  /** mouse button released, ending direct manipulation */
  public void release_direct() {
    cp.release();
  }
}

