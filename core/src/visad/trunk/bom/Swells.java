
//
// Swells.java
//

/*
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

/**
 * Swells class for setting up display of swells
 */

package visad.bom;

import visad.*;
import java.rmi.RemoteException;

public class Swells extends Exception {

  private static final int NDIRS = 361;
  private static final int NHEIGHTS = 51;

  // size scale for shapes
  private static final float SIZE = 0.05f;

  private static WindPolarCoordinateSystem wcs = null;
  // four points of a zero-degree arrow in polar coordinates
  private static float[][] arrow_zero =
    {{0.0f, 0.0f, 10.0f, -10.0f}, {1.5f*SIZE, 2.5f*SIZE, 2.3f*SIZE, 2.3f*SIZE}};
  
  public static void setupSwellDisplay(RealType swellDir, RealType swellHeight, 
                DisplayImpl display) throws VisADException, RemoteException {

    if (wcs == null) wcs = new WindPolarCoordinateSystem();

    // construct dir_set and dir_shapes
    Integer1DSet dir_set = new Integer1DSet(swellDir, NDIRS);
    VisADGeometryArray[] dir_shapes = new VisADGeometryArray[NDIRS];
    for (int i=0; i<NDIRS; i++) {
      dir_shapes[i] = new VisADLineArray();
      dir_shapes[i].vertexCount = 6;
      // copy arrow at zero degrees
      float[][] arrow = (float[][]) arrow_zero.clone();
      // rotate arrow by "i" degrees
      for (int j=0; j<arrow[0].length; j++) {
        arrow[0][j] += i;
      }
      // convert arrow form polar to cartesian
      arrow = wcs.toReference(arrow);
      // draw arraow as three line segments
      dir_shapes[i].coordinates = new float[]
        {arrow[0][1], arrow[1][1], 0.0f,
         arrow[0][0], arrow[1][0], 0.0f,
         arrow[0][1], arrow[1][1], 0.0f,
         arrow[0][2], arrow[1][2], 0.0f,
         arrow[0][1], arrow[1][1], 0.0f,
         arrow[0][3], arrow[1][3], 0.0f};
    }

    // construct height_set and height_shapes
    Integer1DSet height_set = new Integer1DSet(swellHeight, NHEIGHTS);
    double[] start = {0.0, -0.5*SIZE, 0.0};
    double[] base = {SIZE, 0.0, 0.0};
    double[] up = {0.0, SIZE, 0.0};
    VisADGeometryArray[] height_shapes = new VisADGeometryArray[NDIRS];
    for (int i=0; i<NDIRS; i++) {
      height_shapes[i] =
        PlotText.render_label(Integer.toString(i), start, base, up, true);
    }

    ScalarMap dir_map = new ScalarMap(swellDir, Display.Shape);
    display.addMap(dir_map);
    ShapeControl dir_control = (ShapeControl) dir_map.getControl();
    dir_control.setShapeSet(dir_set);
    dir_control.setShapes(dir_shapes);

    ScalarMap height_map = new ScalarMap(swellHeight, Display.Shape);
    display.addMap(height_map);
    ShapeControl height_control = (ShapeControl) height_map.getControl();
    height_control.setShapeSet(height_set);
    height_control.setShapes(height_shapes);

    return;
  }

  public static void main(String args[])
         throws VisADException, RemoteException {
  }

}
