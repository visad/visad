
//
// BarbManipulationRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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

package visad.bom;
 
import visad.*;
import visad.java2d.*;

import java.util.*;
import java.rmi.*;


/**
   BarbManipulationRendererJ2D is the VisAD class for direct
   manipulation rendering of wind barbs under Java2D
*/
public class BarbManipulationRendererJ2D extends DirectManipulationRendererJ2D {

  /** this DataRenderer supports direct manipulation for RealTuple
      representations of wind barbs; four of the RealTuple's Real
      components must be mapped to XAxis, YAxis, Flow1X and Flow1Y */
  public BarbManipulationRendererJ2D () {
    super();
  }
 
  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTupleTypeJ2D(type, link, parent);
  }

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;
  private final static String notRealTupleType =
    "not RealTuple";

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient DataReference ref = null;
  private transient MathType type = null;
  private transient ShadowType shadow = null;
  /** spatial DisplayTupleType other than
      DisplaySpatialCartesianTuple */
  private DisplayTupleType tuple;

  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  public void checkDirect() throws VisADException, RemoteException {
    // realCheckDirect();
    //
    // must customize
    setIsDirectManipulation(false);

    link = getLinks()[0];
    ref = link.getDataReference();
    shadow = link.getShadow().getAdaptedShadowType();
    type = link.getType();
    tuple = null;
    if (!(type instanceof RealTupleType)) {
      whyNotDirect = notRealTupleType;
    }
    // . . .
  }

  public void addPoint(float[] x) throws VisADException {
    // must customize
    int count = x.length / 3;
    VisADGeometryArray array = null;
    if (count == 1) {
      array = new VisADPointArray();
    }
    else if (count == 2) {
      array = new VisADLineArray();
    }
    else {
      return;
    }
    array.coordinates = x;
    array.vertexCount = count;
    VisADAppearance appearance = new VisADAppearance();
    DataDisplayLink link = getLinks()[0];
    float[] default_values = link.getDefaultValues();
    DisplayImpl display = getDisplay();
    appearance.pointSize =
      default_values[display.getDisplayScalarIndex(Display.PointSize)];
    appearance.lineWidth = 
      default_values[display.getDisplayScalarIndex(Display.LineWidth)];
    appearance.red = 1.0f;
    appearance.green = 1.0f;
    appearance.blue = 1.0f;
    appearance.array = array;
    //
    VisADGroup extra_branch = getExtraBranch();
    //
    // want replace rather than add
    extra_branch.addChild(appearance);
  }

/* customize the following methods from visad.DataRenderer
  setSpatialValues
  checkClose
  drag_direct
*/

  private float[][] spatialValues = null;

  /** set spatialValues from ShadowType.doTransform */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    // these are X, Y, Z values
    spatialValues = spatial_values;
  }

  /** find minimum distance from ray to spatialValues */
  public synchronized float checkClose(double[] origin, double[] direction) {
    float distance = Float.MAX_VALUE;
    // . . .
    return distance;
  }

  public synchronized void drag_direct(VisADRay ray, boolean first) {
    // System.out.println("drag_direct " + first + " " + type);
    if (spatialValues == null || ref == null || shadow == null) return;
    // . . .
  }

}

