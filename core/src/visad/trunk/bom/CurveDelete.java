//
// CurveDelete.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java2d.*;
import visad.java3d.*;

import java.awt.event.*;
import java.rmi.*;


public class CurveDelete implements ActionListener {

  DataReferenceImpl ref;
  DisplayImpl display;
  boolean lines = false;
  DataReferenceImpl new_ref;

  CurveDelete(DataReferenceImpl r, DisplayImpl d) {
    ref = r;
    display = d;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("del")) {
      try {
        UnionSet set = (UnionSet) ref.getData();
        SampledSet[] sets = set.getSets();
        SampledSet[] new_sets = new SampledSet[sets.length - 1];
        System.arraycopy(sets, 0, new_sets, 0, sets.length - 1);
        ref.setData(new UnionSet(set.getType(), new_sets));
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
    else if (cmd.equals("fill")) {
      UnionSet set = null;
      try {
        set = (UnionSet) ref.getData();
        System.out.println("area = " + DelaunayCustom.computeArea(set));
      }
      catch (VisADException ex) {
        System.out.println(ex.getMessage());
      }
      try {
        // Irregular2DSet new_set = DelaunayCustom.fill(set);
        Irregular2DSet new_set = DelaunayCustom.fillCheck(set, false);
        if (new_ref == null) {
          new_ref = new DataReferenceImpl("fill");
          ConstantMap[] cmaps = new ConstantMap[]
            {new ConstantMap(1.0, Display.Blue),
             new ConstantMap(1.0, Display.Red),
             new ConstantMap(0.0, Display.Green)};
          DataRenderer renderer = 
              (display instanceof DisplayImplJ3D)
                  ? (DataRenderer) new DefaultRendererJ3D()
                  : (DataRenderer) new DefaultRendererJ2D();
          renderer.suppressExceptions(true);
          display.addReferences(renderer, new_ref, cmaps);
        }
        new_ref.setData(new_set);
      }
      catch (VisADException ex) {
        System.out.println(ex.getMessage());
      }
      catch (RemoteException ex) {
        System.out.println(ex.getMessage());
      }
    }
    else if (cmd.equals("lines")) {
      try {
        lines = !lines;
        GraphicsModeControl mode = display.getGraphicsModeControl();
        if (lines) {
          mode.setPolygonMode(DisplayImplJ3D.POLYGON_LINE);
        }
        else {
          mode.setPolygonMode(DisplayImplJ3D.POLYGON_FILL);
        }
      }
      catch (VisADException ex) {
        System.out.println(ex.getMessage());
      }
      catch (RemoteException ex) {
        System.out.println(ex.getMessage());
      }
    }
  }
}

