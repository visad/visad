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
            if (bio.sm.dtypes == null) return;
            double vx = i < 2 ? bio.sm.min_x : bio.sm.max_x;
            double vy = i < 2 ? bio.sm.min_y : bio.sm.max_y;
            double vz = i == 0 ? bio.sm.min_z : bio.sm.max_z;
            setData(i, vx, vy, vz);
            return;
          }
        }
        MathType type = t[0].getType();
        float[][] samples = new float[3][len + 1];
        for (int j=0; j<len; j++) {
          double[] values = t[j].getValues();
          double vx = values[0];
          double vy = values[1];
          double vz = values[2];
          boolean cx1 = vx < bio.sm.min_x;
          boolean cx2 = vx > bio.sm.max_x;
          boolean cy1 = vy < bio.sm.min_y;
          boolean cy2 = vy > bio.sm.max_y;
          boolean cz1 = vz < bio.sm.min_z;
          boolean cz2 = vz > bio.sm.max_z;
          if (cx1) vx = bio.sm.min_x;
          else if (cx2) vx = bio.sm.max_x;
          if (cy1) vy = bio.sm.min_y;
          else if (cy2) vy = bio.sm.max_y;
          if (cz1) vz = bio.sm.min_z;
          else if (cz2) vz = bio.sm.max_z;
          boolean changed = cx1 || cx2 || cy1 || cy2 || cz1 || cz2;

          // snap values to nearest box edge (must touch two of three axes)
          double rx = Math.abs(bio.sm.max_x - bio.sm.min_x);
          double ry = Math.abs(bio.sm.max_y - bio.sm.min_y);
          double rz = Math.abs(bio.sm.max_z - bio.sm.min_z);
          double dx1 = Math.abs(vx - bio.sm.min_x) / rx;
          double dx2 = Math.abs(vx - bio.sm.max_x) / rx;
          double dy1 = Math.abs(vy - bio.sm.min_y) / ry;
          double dy2 = Math.abs(vy - bio.sm.max_y) / ry;
          double dz1 = Math.abs(vz - bio.sm.min_z) / rz;
          double dz2 = Math.abs(vz - bio.sm.max_z) / rz;
          boolean xm = dx1 == 0 || dx2 == 0;
          boolean ym = dy1 == 0 || dy2 == 0;
          boolean zm = dz1 == 0 || dz2 == 0;
          if (!xm && !ym || !xm && !zm || !ym && !zm) {
            boolean axisx = dx1 < dx2;
            boolean axisy = dy1 < dy2;
            boolean axisz = dz1 < dz2;
            double distx = axisx ? dx1 : dx2;
            double disty = axisy ? dy1 : dy2;
            double distz = axisz ? dz1 : dz2;
            boolean snapx = distx <= distz || distx <= disty;
            boolean snapy = disty <= distx || disty <= distz;
            boolean snapz = !snapx || !snapy;
            if (snapx) vx = axisx ? bio.sm.min_x : bio.sm.max_x;
            if (snapy) vy = axisy ? bio.sm.min_y : bio.sm.max_y;
            if (snapz) vz = axisz ? bio.sm.min_z : bio.sm.max_z;
            changed = true;
          }
          if (changed) {
            setData(j, vx, vy, vz);
            return;
          }

          for (int i=0; i<3; i++) samples[i][j] = (float) values[i];
        }
        for (int i=0; i<3; i++) {
          samples[i][len] = samples[i][1] + samples[i][2] - samples[i][0];
        }
        try {
          Gridded3DSet plane = new Gridded3DSet(type, samples, 2, 2);
          refs[0].setData(plane);
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
          new ConstantMap(0.5f, Display.Alpha),
          new ConstantMap(15.0f, Display.PointSize)
        };
      }
      renderers[i].suppressExceptions(true);
      renderers[i].toggle(false);
      bio.display3.addReferences(renderers[i], refs[i], maps);
    }
  }


  // -- HELPER METHODS --

  /** Moves the given reference point. */
  private void setData(int i, double x, double y, double z) {
    try {
      refs[i + 1].setData(new RealTuple(new Real[] {
        new Real(bio.sm.dtypes[0], x),
        new Real(bio.sm.dtypes[1], y),
        new Real(bio.sm.dtypes[2], z)
      }));
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

}
