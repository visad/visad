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

  private double xlow, xhi, ylow, yhi; // rect boundaries

  private RealType x = null;
  private RealType y = null;
  private RealTupleType xy = null;

  private CellImpl cell_rbb = null;
  private CellImpl cell_xlyl = null;
  private CellImpl cell_xlyh = null;
  private CellImpl cell_xhyl = null;
  private CellImpl cell_xhyh = null;

  private DataReferenceImpl ref_rbb = null;
  private DataReferenceImpl ref_xlyl = null;
  private DataReferenceImpl ref_xlyh = null;
  private DataReferenceImpl ref_xhyl = null;
  private DataReferenceImpl ref_xhyh = null;
  private DataReferenceImpl ref_rect = null;

  private CutAndPasteFields thiscp = null;

  /**
     gs has MathType (Time -> ((x, y) -> value)) or ((x, y) -> value)
     conditions:
     1. x and y mapped to XAxis, YAxis, ZAxis
     2. (x, y) domain LinearSet
     3. if Time, it is mapped to Animation
  */
  public CutAndPasteFields(DataImpl gs, DisplayImplJ3D d)
         throws VisADException, RemoteException {
    grids = gs;
    display = d;
    thiscp = this;

    ref_rbb = new DataReferenceImpl("rbb");
    ref_xlyl = new DataReferenceImpl("xlyl");
    ref_xlyh = new DataReferenceImpl("xlyh");
    ref_xhyl = new DataReferenceImpl("xhyl");
    ref_xhyh = new DataReferenceImpl("xhyh");
    ref_rect = new DataReferenceImpl("rect");

    cell_xlyl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xlyl.getData();
        double xl = ((Real) rt.getComponent(0)).getValue();
        double yl = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xl, xlow) ||
            !Util.isApproximatelyEqual(yl, ylow)) {
          xlow = xl;
          ylow = yl;
          drag();
        }
      }
    };

    cell_xlyh = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xlyh.getData();
        double xl = ((Real) rt.getComponent(0)).getValue();
        double yh = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xl, xlow) ||
            !Util.isApproximatelyEqual(yh, yhi)) {
          xlow = xl;
          yhi = yh;
          drag();
        }
      }
    };

    cell_xhyl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xhyl.getData();
        double xh = ((Real) rt.getComponent(0)).getValue();
        double yl = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xh, xhi) ||
            !Util.isApproximatelyEqual(yl, ylow)) {
          xhi = xh;
          ylow = yl;
          drag();
        }
      }
    };

    cell_xhyh = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xhyh.getData();
        double xh = ((Real) rt.getComponent(0)).getValue();
        double yh = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xh, xhi) ||
            !Util.isApproximatelyEqual(yh, yhi)) {
          xhi = xh;
          yhi = yh;
          drag();
        }
      }
    };

    // rubber band box release
    cell_rbb = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        Set set = (Set) ref_rbb.getData();
        float[][] samples = set.getSamples();
        if (samples != null) {
          cell_rbb.removeReference(ref_rbb);
          xlow = samples[0][0];
          ylow = samples[1][0];
          xhi = samples[0][1];
          yhi = samples[1][1];
          drag();
          cell_xlyl.addReference(ref_xlyl);
          cell_xlyh.addReference(ref_xlyh);
          cell_xhyl.addReference(ref_xhyl);
          cell_xhyh.addReference(ref_xhyh);

          display.disableAction();
          display.addReferences(new BoxDragRendererJ3D(thiscp), ref_xlyl);
          display.addReferences(new BoxDragRendererJ3D(thiscp), ref_xlyh);
          display.addReferences(new BoxDragRendererJ3D(thiscp), ref_xhyl);
          display.addReferences(new BoxDragRendererJ3D(thiscp), ref_xhyh);
          display.removeReference(ref_rbb);
          display.enableAction();
        }
      }
    };


  }

  public void start() throws VisADException, RemoteException {
    cell_rbb.addReference(ref_rbb);
    Gridded2DSet dummy_set = new Gridded2DSet(xy, null, 1);
    ref_rbb.setData(dummy_set);
    display.addReferences(new RubberBandBoxRendererJ3D(x, y), ref_rbb);
  }

  public void drag() throws VisADException, RemoteException {
    display.disableAction();
    ref_xlyl.setData(new RealTuple(xy, new double[] {xlow, ylow}));
    ref_xlyh.setData(new RealTuple(xy, new double[] {xlow, yhi}));
    ref_xhyl.setData(new RealTuple(xy, new double[] {xhi, ylow}));
    ref_xhyh.setData(new RealTuple(xy, new double[] {xhi, yhi}));
    float[][] samples = {{(float) xlow, (float) xlow, (float) xhi, (float) xhi},
                         {(float) ylow, (float) ylow, (float) yhi, (float) yhi}};
    ref_rect.setData(new Gridded2DSet(xy, samples, 4));
    display.enableAction();
  }

  // BoxDragRendererJ3D button release
  public void drag_release() {
/*
    try {

// need to cut and paste grid, and save replaced section

    }
    catch (VisADException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
    catch (RemoteException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
*/
  }

  public void stop() throws VisADException, RemoteException {
    display.disableAction();
    display.removeReference(ref_rbb);
    display.removeReference(ref_xlyl);
    display.removeReference(ref_xlyh);
    display.removeReference(ref_xhyl);
    display.removeReference(ref_xhyh);
    display.enableAction();

    try { cell_rbb.removeReference(ref_rbb); }
    catch (ReferenceException e) { }
    try { cell_xlyl.removeReference(ref_xlyl); }
    catch (ReferenceException e) { }
    try { cell_xlyh.removeReference(ref_xlyh); }
    catch (ReferenceException e) { }
    try { cell_xhyl.removeReference(ref_xhyl); }
    catch (ReferenceException e) { }
    try { cell_xhyh.removeReference(ref_xhyh); }
    catch (ReferenceException e) { }
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
    cp.drag_release();
  }
}

