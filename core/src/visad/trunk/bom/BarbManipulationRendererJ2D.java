
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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
    setIsDirectManipulation(true);
  }

  public void addPoint(float[] x) throws VisADException {
/** is needed
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
*/
  }

/* customize the following methods from visad.DataRenderer
  setSpatialValues
  checkClose
  drag_direct
  release_direct
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

  /** mouse button released, ending direct manipulation */
  public synchronized void release_direct() {
  }

  public synchronized void drag_direct(VisADRay ray, boolean first) {
    // System.out.println("drag_direct " + first + " " + type);
    if (spatialValues == null || ref == null || shadow == null) return;
    // . . .
  }

  static final int N = 5;

  /** test BarbManipulationRendererJ2D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType x = new RealType("x");
    RealType y = new RealType("y");
    RealType flowx = new RealType("flowx");
    RealType flowy = new RealType("flowy");
    RealType red = new RealType("red");
    RealType green = new RealType("green");
    RealType blue = new RealType("blue");

    DisplayImpl display = new DisplayImplJ2D("display1");
    ScalarMap xmap = new ScalarMap(x, Display.XAxis);
    display.addMap(xmap);
    xmap.setRange(-1.0, 1.0);
    ScalarMap ymap = new ScalarMap(y, Display.YAxis);
    display.addMap(ymap);
    ymap.setRange(-1.0, 1.0);
    ScalarMap flowx_map = new ScalarMap(flowx, Display.Flow1X);
    display.addMap(flowx_map);
    flowx_map.setRange(-1.0, 1.0);
    ScalarMap flowy_map = new ScalarMap(flowy, Display.Flow1Y);
    display.addMap(flowy_map);
    flowy_map.setRange(-1.0, 1.0);
    FlowControl flow_control = (FlowControl) flowy_map.getControl();
    flow_control.setFlowScale(0.15f);

    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;
        RealTuple tuple = new RealTuple(new Real[]
          {new Real(x, u), new Real(y, v),
           new Real(flowx, 30.0 * u), new Real(flowy, 30.0 * v),
           new Real(red, 1.0), new Real(green, 1.0), new Real(blue, 0.0)});
    
        DataReferenceImpl ref = new DataReferenceImpl("ref");
        ref.setData(tuple);
        display.addReferences(new BarbManipulationRendererJ2D(), ref);
      }
    }

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test BarbManipulationRendererJ2D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
 
    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);
 
    // add display to JPanel
    panel.add(display.getComponent());
 
    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }

}

