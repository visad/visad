//
// PickManipulationRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.event.*;
import javax.swing.*;
import java.rmi.RemoteException;


/**
 * PickManipulationRendererJ2D is the VisAD class for picking
 * data in 2D.
 */
// from  "Heftrich, Torsten, AVENTIS/DE" <Heftrich@crt.hoechst.com>
public class PickManipulationRendererJ2D extends
DirectManipulationRendererJ2D {

  private int mouseModifiersMask = 0;
  private int mouseModifiersValue = 0;

  /**
   * Default constructor
   */
  public PickManipulationRendererJ2D () {
    this (0, 0);
  }

  /** 
   * Construct a new PickManipulationRenderer using the mouseModifiers
   * supplied.  mmm and mmv determine whehter SHIFT or CTRL keys are 
   * required - This is needed since this is a greedy 
   * DirectManipulationRenderer that will grab any right mouse click 
   * (that intersects its 2-D sub-manifold).
   * @param mmm  mouse modifiers mask.
   * @param mmv  mouse modifiers value.
   */
  public PickManipulationRendererJ2D (int mmm, int mmv) {
    super();
    mouseModifiersMask = mmm;
    mouseModifiersValue = mmv;
  }

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient DataReference ref = null;

  private float[][] spatialValues = null;
  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  private int directManifoldDimension = -1;

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;

  /** possible values for whyNotDirect */
  private final static String notSimpleField =
    "not simple field";
  private final static String notSimpleTuple =
    "not simple tuple";

  private boolean stop = false;

  /**
   * Check if direct manipulation is possible.  
   */
  public void checkDirect() throws VisADException, RemoteException {
    setIsDirectManipulation(false);

    DisplayImpl display = getDisplay();

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      link = null;
      return;
    }
    link = Links[0];

    ref = link.getDataReference();

    ShadowType shadow = link.getShadow().getAdaptedShadowType();
    MathType type = link.getType();

    if (type instanceof FunctionType) {
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_FIELD) {
        whyNotDirect = notSimpleField;
        return;
      }
    }
    else if (type instanceof SetType) {
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_FIELD) {
        whyNotDirect = notSimpleField;
        return;
      }
    }
    else {
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_TUPLE) {
        whyNotDirect = notSimpleTuple;
        return;
      }
    }

    setIsDirectManipulation(true);
  }

  private int getDirectManifoldDimension() {
    return directManifoldDimension;
  }

  /**
   * If direct manipulation is not possible, get the error message
   * explaining why.
   * @return error message. Will be null if no errors.
   */
  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  /**
   * Add a point.  a no-op at this point.  
   * @param x  point value.
   */
  public void addPoint(float[] x) throws VisADException {
    // may need to do this for performance
  }

// methods customized from DataRenderer:

  /**
   * Get the CoordinateSystem for the display side.
   * @return  null for this DataRenderer
   */
  public CoordinateSystem getDisplayCoordinateSystem() {
    return null;
  }

  /** 
   * Set spatialValues from ShadowType.doTransform 
   * @param spatial_values  X, Y, Z values
   */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    // these are X, Y, Z values
    spatialValues = spatial_values;
  }

  /** 
   * Check if ray intersects sub-manifold.  
   * @param origin  x,y,z values of the ray
   * @param direction x,y,z values of the ray?
   * @return distance from the spatial values.
   */
  public synchronized float checkClose(double[] origin, double[]
direction)
{
    int mouseModifiers = getLastMouseModifiers();
    if ((mouseModifiers & mouseModifiersMask) != mouseModifiersValue) {
      return Float.MAX_VALUE;
    }

    float distance = Float.MAX_VALUE;
    if (spatialValues == null) return distance;
    float o_x = (float) origin[0];
    float o_y = (float) origin[1];
    float o_z = (float) origin[2];
    float d_x = (float) direction[0];
    float d_y = (float) direction[1];
    float d_z = (float) direction[2];
 /*
System.out.println("origin = " + o_x + " " + o_y + " " + o_z);
System.out.println("direction = " + d_x + " " + d_y + " " + d_z);
  */
    for (int i=0; i<spatialValues[0].length; i++) {
      float x = spatialValues[0][i] - o_x;
      float y = spatialValues[1][i] - o_y;
      float z = spatialValues[2][i] - o_z;
      float dot = x * d_x + y * d_y + z * d_z;
      x = x - dot * d_x;
      y = y - dot * d_y;
      z = z - dot * d_z;
      float d = (float) Math.sqrt(x * x + y * y + z * z);
      if (d < distance) {
        distance = d;
        closeIndex = i;
      }
/*
System.out.println("spatialValues["+i+"] = " + spatialValues[0][i] + " "
+
spatialValues[1][i] + " " + spatialValues[2][i] + " d = " + d);
*/
    }
/*
System.out.println("checkClose: distance = " + distance);
*/
    return distance;
  }

  /**
   * Return the index of the closes point.
   * @return index of closest point
   */
  public int getCloseIndex() {
    return closeIndex;
  }

  /**
   * Actual workhorse method of manipulation renderer. It's what
   * gets called when the click is done.
   * @param ray ray of point where click is.
   * @param first  if this is the first time.
   * @param mouseModifiers  modifiers used with the mouse.
   */
  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    if (ref == null) return;

    if (first) {
      try {
        ref.setData(ref.getData());
      }
      catch (VisADException e) {
      }
      catch (RemoteException e) {
      }
    }
  }

  /** test PickManipulationRendererJ2D */
  public static void main(String args[])
         throws VisADException, RemoteException {

    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    FunctionType f1d = new FunctionType(x, y);
    RealTupleType xy = new RealTupleType(x, y);
    TextType t = new TextType("text");
    RealType s = RealType.getRealType("shape");

    Data[] td = {new Real(x, 0.5),
                 new Real(y, 0.5),
                 new Text(t, "text")};
    Tuple text = new Tuple(td);

    Real[] sd = {new Real(x, -0.5),
                 new Real(y, -0.5),
                 new Real(s, 0.0)};
    RealTuple shape = new RealTuple(sd);

    Real real = new Real(x, -0.5);

    Real[] rtd = {new Real(x, 0.5),
                 new Real(y, -0.5)};
    RealTuple real_tuple = new RealTuple(rtd);

    FlatField field1d = new FlatField(f1d, new Linear1DSet(x, -1.0, -0.5, 64));
    double[][] values = new double[1][64];
    for (int i=0; i<64; i++) values[0][i] = 0.5 + Math.abs(i - 31.5) / 63.0;
    field1d.setSamples(values);

    Set set2d = new Linear2DSet(xy, 0.5, 1.0, 32, -0.25, 0.25, 32);

    // construct Java2D display and mappings
    DisplayImpl display = new DisplayImplJ2D("display");
    DisplayRenderer dr = display.getDisplayRenderer();
    dr.setPickThreshhold(0.2f); // allow sloppy picking

    ScalarMap xmap = new ScalarMap(x, Display.XAxis);
    display.addMap(xmap);
    xmap.setRange(-1.0, 1.0);

    ScalarMap ymap = new ScalarMap(y, Display.YAxis);
    display.addMap(ymap);
    ymap.setRange(-1.0, 1.0);

    ScalarMap tmap = new ScalarMap(t, Display.Text);
    display.addMap(tmap);
    TextControl tcontrol = (TextControl) tmap.getControl();
    tcontrol.setCenter(true);

    ScalarMap smap = new ScalarMap(s, Display.Shape);
    display.addMap(smap);
    ShapeControl scontrol = (ShapeControl) smap.getControl();
    scontrol.setShapeSet(new Integer1DSet(s, 1));
    VisADLineArray cross = new VisADLineArray();
    cross.coordinates = new float[]
      {0.1f,  0.1f, 0.0f,    -0.1f, -0.1f, 0.0f,
       0.1f, -0.1f, 0.0f,    -0.1f,  0.1f, 0.0f};
    cross.vertexCount = cross.coordinates.length / 3;
    scontrol.setShapes(new VisADGeometryArray[] {cross});

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);

    DataReferenceImpl tref = new DataReferenceImpl("text");
    tref.setData(text);
    display.addReferences(new PickManipulationRendererJ2D(), tref);
    CellImpl cellt = new CellImpl() {
      private boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (first) first = false;
        else System.out.println("text picked");
      }
    };
    cellt.addReference(tref);

    DataReferenceImpl sref = new DataReferenceImpl("shape");
    sref.setData(shape);
    display.addReferences(new PickManipulationRendererJ2D(), sref);
    CellImpl cells = new CellImpl() {
      private boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (first) first = false;
        else System.out.println("shape picked");
      }
    };
    cells.addReference(sref);

    DataReferenceImpl rref = new DataReferenceImpl("Real");
    rref.setData(real);
    ConstantMap[] rmaps = {new ConstantMap(5.0, Display.PointSize)};
    display.addReferences(new PickManipulationRendererJ2D(), rref, rmaps);
    CellImpl cellr = new CellImpl() {
      private boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (first) first = false;
        else System.out.println("Real picked");
      }
    };
    cellr.addReference(rref);

    DataReferenceImpl rtref = new DataReferenceImpl("RealTuple");
    rtref.setData(real_tuple);
    ConstantMap[] rtmaps = {new ConstantMap(5.0, Display.PointSize)};
    display.addReferences(new PickManipulationRendererJ2D(), rtref, rtmaps);
    CellImpl cellrt = new CellImpl() {
      private boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (first) first = false;
        else System.out.println("RealTuple picked");
      }
    };
    cellrt.addReference(rtref);

    DataReferenceImpl field1dref = new DataReferenceImpl("field1d");
    field1dref.setData(field1d);
    final PickManipulationRendererJ2D pmr1d = new PickManipulationRendererJ2D();
    display.addReferences(pmr1d, field1dref);
    CellImpl cellfield1d = new CellImpl() {
      private boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (first) first = false;
        else {
          int i = pmr1d.getCloseIndex();
          System.out.println("1-D Field picked, index = " + i);
        }
      }
    };
    cellfield1d.addReference(field1dref);

    DataReferenceImpl setref = new DataReferenceImpl("set");
    setref.setData(set2d);
    final PickManipulationRendererJ2D pmrset = new PickManipulationRendererJ2D();
    display.addReferences(pmrset, setref);
    CellImpl cellset = new CellImpl() {
      private boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (first) first = false;
        else {
          int i = pmrset.getCloseIndex();
          System.out.println("set picked, index = " + i);
        }
      }
    };
    cellset.addReference(setref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test PickManipulationRendererJ2D");
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
