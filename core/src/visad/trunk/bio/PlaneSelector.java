//
// PlaneSelector.java
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
import visad.java3d.DirectManipulationRendererJ3D;

/**
 * PlaneSelector maintains a data structure that can be
 * manipulated by the user to specify an arbitrary plane.
 */
public class PlaneSelector {

  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Data references for the endpoints and linked plane. */
  private DataReferenceImpl[] refs = new DataReferenceImpl[4];

  /** Data renderers for the endpoints and linked plane. */
  private DataRenderer[] renderers = new DataRenderer[4];

  /** Computation cell for linking plane with endpoints. */
  private CellImpl cell;


  // -- CONSTRUCTOR --

  /** Constructs a selection box. */
  public PlaneSelector(BioVisAD biovis) {
    bio = biovis;

    // set up cell that links plane with endpoints
    cell = new CellImpl() {
      public void doAction() {
        int len = refs.length - 1;
        RealTuple[] t = new RealTuple[len];
        for (int i=0; i<len; i++) {
          t[i] = (RealTuple) refs[i + 1].getData();
          if (t[i] == null) {
            // CTR - FIXME - use better values than 0.0
            try {
              refs[i + 1].setData(new RealTuple(new Real[] {
                new Real(bio.sm.dtypes[0], 0.0),
                new Real(bio.sm.dtypes[1], 0.0),
                new Real(bio.sm.dtypes[2], 0.0)
              }));
            }
            catch (VisADException exc) { exc.printStackTrace(); }
            catch (RemoteException exc) { exc.printStackTrace(); }
            return;
          }
        }
        MathType type = t[0].getType();
        float[][] samples = new float[3][len];
        for (int j=0; j<len; j++) {
          double[] values = t[j].getValues();
          for (int i=0; i<3; i++) samples[i][j] = (float) values[i];
        }
        try {
          Gridded3DSet[] sets = new Gridded3DSet[len];
          for (int i=0; i<len; i++) {
            int i1 = (i + 1) % len;
            float[][] pts = {
              {samples[0][i], samples[0][i1]},
              {samples[1][i], samples[1][i1]},
              {samples[2][i], samples[2][i1]}
            };
            sets[i] = new Gridded3DSet(type, pts, 2);
          }
          UnionSet lines = new UnionSet(type, sets);
          refs[0].setData(lines);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
    };

    // construct data references
    cell.disableAction();
    try {
      for (int i=0; i<refs.length; i++) {
        if (i > 0) {
          refs[i] = new DataReferenceImpl("bio_plane" + i);
          cell.addReference(refs[i]);
        }
        else refs[i] = new DataReferenceImpl("bio_plane");
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    cell.enableAction();
  }


  // -- API METHODS --

  /** Toggles the plane selector's visibility. */
  public void toggle(boolean visible) {
    for (int i=0; i<renderers.length; i++) renderers[i].toggle(visible);
  }

  /** Adds the plane selector to its display. */
  public void init() throws VisADException, RemoteException {
    DisplayRenderer displayRenderer = bio.display3.getDisplayRenderer();
    for (int i=0; i<refs.length; i++) {
      ConstantMap[] maps;
      if (i > 0) {
        renderers[i] = new DirectManipulationRendererJ3D();
        renderers[i].setPickCrawlToCursor(false);
        maps = new ConstantMap[] {
          new ConstantMap(1.0f, Display.Red),
          new ConstantMap(1.0f, Display.Green),
          new ConstantMap(0.0f, Display.Blue),
          new ConstantMap(15.0f, Display.PointSize)
        };
      }
      else {
        renderers[i] = displayRenderer.makeDefaultRenderer();
        maps = new ConstantMap[] {
          new ConstantMap(1.0f, Display.Red),
          new ConstantMap(1.0f, Display.Green),
          new ConstantMap(1.0f, Display.Blue),
          //new ConstantMap(0.5f, Display.Alpha),
          new ConstantMap(15.0f, Display.PointSize)
        };
      }
      renderers[i].suppressExceptions(true);
      renderers[i].toggle(false);
      bio.display3.addReferences(renderers[i], refs[i], maps);
    }
  }

}
