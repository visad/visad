
//
// DisplayRendererJ2D.java
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

package visad.java2d;

import visad.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;

import java.util.*;
import java.rmi.*;


/**
   DisplayRendererJ2D is the VisAD abstract super-class for background and
   metadata rendering algorithms.  These complement depictions of Data
   objects created by DataRenderer objects.<P>

   DisplayRendererJ2D also manages the overall relation of DataRenderer
   output to Java2D and manages the VisAD scene graph.<P>

   It creates the binding between Control objects and scene graph
   Behavior objects for direct manipulation of Control objects.<P>

   DisplayRendererJ2D is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DisplayRendererJ2D extends DisplayRenderer {

  private VisADCanvasJ2D canvas;

  /** root VisADGroup of scene graph under Locale */
  private VisADGroup root = null;
  /** single AffineTransform applied to all
      Data depictions */
  private AffineTransform trans = null;
  /** VisADGroup between root and all direct manipulation
      Data depictions */
  private VisADGroup direct = null;

  /** AffineTransform between trans and cursor */
  private AffineTransform cursor_trans = null;
  /** single VisADSwitch between root and cursor */
  private VisADSwitch cursor_switch = null;
  /** children of cursor_switch */
  private VisADGroup cursor_on = null, cursor_off = null;
  /** on / off state of cursor */
  private boolean cursorOn = false;
  /** on / off state of direct manipulation location display */
  private boolean directOn = false;

  /** single VisADSwitch between root and scales */
  private VisADSwitch scale_switch = null;
  /** children of scale_switch */
  private VisADGroup scale_on = null, scale_off = null;
  /** on / off state of cursor in GraphicsModeControl */

  /** distance threshhold for successful pick */
  private static final float PICK_THRESHHOLD = 0.05f;
  /** Vector of DirectManipulationRenderers */
  private Vector directs = new Vector();

  /** cursor location */
  private float cursorX, cursorY, cursorZ;
  /** normalized direction perpendicular to current cursor plane */
  private float line_x, line_y, line_z;
  /** start value for cursor */
  private float point_x, point_y, point_z;

  public DisplayRendererJ2D () {
    super();
  }

  public VisADGroup getRoot() {
    return root;
  }

  public AffineTransform getTrans() {
    return trans;
  }

  public VisADCanvasJ2D getCanvas() {
    return canvas;
  }

  public VisADGroup getCursorOnBranch() {
    return cursor_on;
  }

  public void setCursorOn(boolean on) {
    cursorOn = on;
    if (on) {
      cursor_switch.setWhichChild(1); // set cursor on
      setCursorStringVector();
    }
    else {
      cursor_switch.setWhichChild(0); // set cursor off
      setCursorStringVector(null);
    }
  }

  public void setDirectOn(boolean on) {
    directOn = on;
    if (!on) {
      setCursorStringVector(null);
    }
  }

  public VisADGroup getDirect() {
    return direct;
  }

  /** create scene graph root, if none exists, with Transform
      and direct manipulation root;
      create special graphics (e.g., 3-D box, SkewT background),
      any lights, any user interface embedded in scene */
  public abstract VisADGroup createSceneGraph(VisADCanvasJ2D c)
         throws DisplayException;

  /** create scene graph root, if none exists, with Transform
      and direct manipulation root */
  public VisADGroup createBasicSceneGraph(VisADCanvasJ2D c,
         MouseBehaviorJ2D mouse) throws DisplayException {
    if (root != null) return root;
    canvas = c;
    canvas.addMouseBehavior(mouse);
    // Create the root of the branch graph
    root = new VisADGroup();
    // create the AffineTransform that is applied to
    // Data object Group objects
    trans = new AffineTransform();

    // initialize scale
    // XXX - for Java2D, scale is controlled in VisADCanvasJ2D
    // double scale = 0.5;
    double scale = 1.0;
    ProjectionControl proj = getDisplay().getProjectionControl();
    AffineTransform tstart = new AffineTransform(proj.getMatrix());
    AffineTransform t1 = new AffineTransform(
      mouse.make_matrix(0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0) );
    t1.concatenate(tstart);
    double[] matrix = new double[6];
    t1.getMatrix(matrix);
    try {
      proj.setMatrix(matrix);
    }
    catch (VisADException e) {
    }
    catch (RemoteException e) {
    }
 
    // create the VisADGroup that is the parent of direct
    // manipulation Data object VisADGroup objects
    direct = new VisADGroup();
    root.addChild(direct);

    cursor_trans = new AffineTransform();
    cursor_switch = new VisADSwitch();
    root.addChild(cursor_switch);
    cursor_on = new VisADGroup();
    cursor_off = new VisADGroup();
    cursor_switch.addChild(cursor_off);
    cursor_switch.addChild(cursor_on);
    cursor_switch.setWhichChild(0); // initially off
    cursorOn = false;

    scale_switch = new VisADSwitch();
    root.addChild(scale_switch);
    scale_on = new VisADGroup();
    scale_off = new VisADGroup();
    scale_switch.addChild(scale_off);
    scale_switch.addChild(scale_on);
    scale_switch.setWhichChild(0); // initially off

    return root;
  }

  public void addSceneGraphComponent(VisADGroup group)
         throws DisplayException {
    root.addChild(group);
  }

  public void addDirectManipulationSceneGraphComponent(VisADGroup group,
         DirectManipulationRendererJ2D renderer) throws DisplayException {
    direct.addChild(group);
    directs.addElement(renderer);
  }

  public void clearScene(DataRenderer renderer) {
    directs.removeElement(renderer);
  }

  public double[] getCursor() {
    double[] cursor = new double[3];
    cursor[0] = cursorX;
    cursor[1] = cursorY;
    cursor[2] = cursorZ;
    return cursor;
  }

  public void depth_cursor(VisADRay ray) {
    line_x = (float) ray.vector[0];
    line_y = (float) ray.vector[1];
    line_z = (float) ray.vector[2];
    point_x = cursorX;
    point_y = cursorY;
    point_z = cursorZ;
  }

  public void drag_depth(float diff) {
    cursorX = point_x + diff * line_x;
    cursorY = point_y + diff * line_y;
    cursorZ = point_z + diff * line_z;
    setCursorLoc();
  }

  public void drag_cursor(VisADRay ray, boolean first) {
    float o_x = (float) ray.position[0];
    float o_y = (float) ray.position[1];
    float o_z = (float) ray.position[2];
    float d_x = (float) ray.vector[0];
    float d_y = (float) ray.vector[1];
    float d_z = (float) ray.vector[2];
    if (first) {
      line_x = d_x;
      line_y = d_y;
      line_z = d_z;
    }
    float dot = (cursorX - o_x) * line_x +
                (cursorY - o_y) * line_y +
                (cursorZ - o_z) * line_z;
    float dot2 = d_x * line_x + d_y * line_y + d_z * line_z;
    if (dot2 == 0.0) return;
    dot = dot / dot2;
    // new cursor location is intersection
    cursorX = o_x + dot * d_x;
    cursorY = o_y + dot * d_y;
    cursorZ = o_z + dot * d_z;
    setCursorLoc();
  }

  private void setCursorLoc() {
    cursor_trans.setToTranslation((double) cursorX, (double) cursorY);
    if (cursorOn) {
      setCursorStringVector();
    }
  }

  /** this assumes constant color, and only VisADPointArray or
      VisADLineArray */
  private void drawAppearance(Graphics graphics, VisADAppearance appearance,
                              AffineTransform t) {
    VisADGeometryArray array = appearance.array;
    if (array == null) return;
    graphics.setColor(new Color(appearance.red, appearance.green,
                                appearance.blue));
    int count = array.vertexCount;
    float[] coordinates = array.coordinates;
    float[] oldcoords = new float[2*count];
    int j = 0;
    for (int i=0; i<3*count; i += 3) {
      oldcoords[j++] = coordinates[i];
      oldcoords[j++] = coordinates[i+1];
    }
    float[] newcoords = new float[2 * count];
    t.transform(oldcoords, 0, newcoords, 0, count);
    if (array instanceof VisADPointArray) {
      for (int i=0; i<2*count; i += 2) {
        graphics.drawLine((int) newcoords[i], (int) newcoords[i+1],
                          (int) newcoords[i], (int) newcoords[i+1]);
      }
    }
    else if (array instanceof VisADLineArray) {
      for (int i=0; i<2*count; i += 4) {
        graphics.drawLine((int) newcoords[i], (int) newcoords[i+1],
                          (int) newcoords[i+2], (int) newcoords[i+3]);
      }
    }
    else {
      throw new VisADError("DisplayRendererJ2D.drawAppearance: " +
                           "bad VisADGeometryArray type");
    }
  }

  /** whenever cursorOn or directOn is true, display
      Strings in cursorStringVector */
  public void drawCursorStringVector(Graphics graphics,
              AffineTransform tgeometry, int width, int height) {
    // draw cursor
    AffineTransform t = new AffineTransform(tgeometry);
    if (cursorOn) {
      t.concatenate(cursor_trans);
      VisADAppearance appearance = null;
      VisADLineArray array = null;
      appearance = (VisADAppearance) cursor_on.getChild(0);
      if (appearance != null) drawAppearance(graphics, appearance, t);
      t = new AffineTransform(tgeometry);
    }

    // draw direct manipulation extra_branch's
    Enumeration renderers = directs.elements();
    while (renderers.hasMoreElements()) {
      DirectManipulationRendererJ2D r =
        (DirectManipulationRendererJ2D) renderers.nextElement();
      VisADGroup extra_branch = r.getExtraBranch();
      if (extra_branch != null) {
        Vector children = ((VisADGroup) extra_branch).getChildren();
        Enumeration childs = children.elements();
        while (childs.hasMoreElements()) {
          VisADAppearance child =
            (VisADAppearance) childs.nextElement();
          drawAppearance(graphics, child, t);
        }
      }
    }

    // draw cursor strings in upper left corner of screen
    graphics.setColor(new Color(1.0f, 1.0f, 1.0f));
    graphics.setFont(new Font("Times New Roman", Font.PLAIN, 10));
    int x = 1;
    int y = 10;
    if (cursorOn || directOn) {
      Enumeration strings = getCursorStringVector().elements();
      while(strings.hasMoreElements()) {
        String string = (String) strings.nextElement();
        graphics.drawString(string, x, y);
        y += 12;
      }
    }

    // draw Exception strings in lower left corner of screen
    x = 1;
    y = height - 2;
    Vector rendererVector = getDisplay().getRendererVector();
    renderers = rendererVector.elements();
    while (renderers.hasMoreElements()) {
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
      Vector exceptionVector = renderer.getExceptionVector();
      Enumeration exceptions = exceptionVector.elements();
      while (exceptions.hasMoreElements()) {
        Exception error = (Exception) exceptions.nextElement();
        String string = error.getMessage();
        graphics.drawString(string, x, y);
        y -= 12;
      }
    }

    // draw wait flag in lower left corner of screen
    if (getWaitFlag()) {
      graphics.drawString("please wait . . .", x, y);
      y -= 12;
    }

    // draw Animation string in upper right corner of screen
    String animation_string = getAnimationString();
    if (animation_string != null) {
      int nchars = animation_string.length();
      if (nchars < 12) nchars = 12;
      x = width - 9 * nchars;
      y = 10;
      graphics.drawString(animation_string, x, y);
      y += 10;
    }
  }

  public DataRenderer findDirect(VisADRay ray) {
    DirectManipulationRendererJ2D renderer = null;
    float distance = Float.MAX_VALUE;
    Enumeration renderers = directs.elements();
    while (renderers.hasMoreElements()) {
      DirectManipulationRendererJ2D r =
        (DirectManipulationRendererJ2D) renderers.nextElement();
      float d = r.checkClose(ray.position, ray.vector);
      if (d < distance) {
        distance = d;
        renderer = r;
      }
    }
    if (distance < PICK_THRESHHOLD) {
      return renderer;
    }
    else {
      return null;
    }
  }

  public boolean anyDirects() {
    return !directs.isEmpty();
  }

  public void setScaleOn(boolean on) {
    if (on) {
      scale_switch.setWhichChild(1); // on
    }
    else {
      scale_switch.setWhichChild(0); // off
    }
  }

  public void setScale(int axis, int axis_ordinal,
              VisADLineArray array, float[] scale_color)
         throws VisADException {
    // add array to scale_on
    // replace any existing at axis, axis_ordinal
    VisADAppearance appearance = new VisADAppearance();
    appearance.red = scale_color[0];
    appearance.green = scale_color[1];
    appearance.blue = scale_color[2];
    appearance.array = array;

    VisADGroup group = new VisADGroup();
    group.addChild(appearance);
    // may only add VisADGroup to 'live' scale_on
    int dim = getMode2D() ? 2 : 3;
    synchronized (scale_on) {
      int n = scale_on.numChildren();
      int m = dim * axis_ordinal + axis;
      if (m >= n) {
        for (int i=n; i<=m; i++) {
          VisADGroup empty = new VisADGroup();
          scale_on.addChild(empty);
        }
      }
      scale_on.setChild(group, m);
    }
  }

  public void clearScales() {
    if (scale_on != null) {
      synchronized (scale_on) {
        int n = scale_on.numChildren();
        for (int i=n-1; i>=0; i--) {
          scale_on.removeChild(i);
        }
      }
    }
  }

  public void setTransform2D(AffineTransform t) {
    trans = new AffineTransform(t);
  }

  public Control makeControl(ScalarMap map) {
    DisplayRealType type = map.getDisplayScalar();
    DisplayImplJ2D display = (DisplayImplJ2D) getDisplay();
    if (type == null) return null;
    if (type.equals(Display.XAxis) ||
        type.equals(Display.YAxis) ||
        type.equals(Display.ZAxis) ||
        type.equals(Display.Latitude) ||
        type.equals(Display.Longitude) ||
        type.equals(Display.Radius)) {
      return (ProjectionControlJ2D) display.getProjectionControl();
    }
    else if (type.equals(Display.RGB) ||
             type.equals(Display.HSV) ||
             type.equals(Display.CMY)) {
      return new ColorControl(display);
    }
    else if (type.equals(Display.RGBA)) {
      return new ColorAlphaControl(display);
    }
    else if (type.equals(Display.Animation)) {
      // note only one RealType may be mapped to Animation
      // so control must be null
      Control control = display.getControl(AnimationControlJ2D.class);
      if (control != null) return control;
      else return new AnimationControlJ2D(display, map.getScalar());
    }
    else if (type.equals(Display.SelectValue)) {
      return new ValueControlJ2D(display);
    }
    else if (type.equals(Display.SelectRange)) {
      return new RangeControl(display);
    }
    else if (type.equals(Display.IsoContour)) {
      return new ContourControl(display);
    }
    else if (type.equals(Display.Flow1X) ||
             type.equals(Display.Flow1Y) ||
             type.equals(Display.Flow1Z)) {
      Control control = display.getControl(Flow1Control.class);
      if (control != null) return control;
      else return new Flow1Control(display);
    }
    else if (type.equals(Display.Flow2X) ||
             type.equals(Display.Flow2Y) ||
             type.equals(Display.Flow2Z)) {
      Control control = display.getControl(Flow2Control.class);
      if (control != null) return control;
      else return new Flow2Control(display);
    }
    else if (type.equals(Display.Shape)) {
      return new ShapeControl(display);
    }
    else {
      return null;
    }
  }

  public DataRenderer makeDefaultRenderer() {
    return new DefaultRendererJ2D();
  }

}

