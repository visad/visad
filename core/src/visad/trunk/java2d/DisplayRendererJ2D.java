//
// DisplayRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
  /** VisADGroup between root and all non-direct-manipulation
      Data depictions */
  private VisADGroup non_direct = null;

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
  /** on / off state of axis scale */
  private boolean scaleOn = false;

  /** single VisADSwitch between root and box */
  private VisADSwitch box_switch = null;
  /** children of box_switch */
  private VisADGroup box_on = null, box_off = null;
  /** on / off state of box */
  private boolean boxOn = false;

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

  public void setClip(float xlow, float xhi, float ylow, float yhi) {
    canvas.setClip(xlow, xhi, ylow, yhi);
  }

  public void setBackgroundColor(float r, float g, float b) {
    canvas.setBackgroundColor(r, g, b);
    canvas.scratchImages();
  }

  public AffineTransform getTrans() {
    return trans;
  }

  public VisADCanvasJ2D getCanvas() {
    return canvas;
  }

  public BufferedImage getImage() {
    BufferedImage image = null;
    while (image == null) {
      try {
        synchronized (this) {
          canvas.captureFlag = true;
          canvas.renderTrigger();
// System.out.println("getImage wait");
          wait();
        }
      }
      catch(InterruptedException e) {
        // note notify generates a normal return from wait rather
        // than an Exception - control doesn't normally come here
      }
      image = canvas.captureImage;
      canvas.captureImage = null;
// System.out.println("getImage (image == null) = " + (image == null));
    }
    return image;
  }

  void notifyCapture() {
    synchronized (this) {
      notify();
    }
  }

  public VisADGroup getCursorOnBranch() {
    return cursor_on;
  }

  public VisADGroup getBoxOnBranch() {
    return box_on;
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
    render_trigger();
  }

  public void setBoxOn(boolean on) {
    boxOn = on;
    if (on) {
      box_switch.setWhichChild(1); // set box on
    }
    else {
      box_switch.setWhichChild(0); // set box off
    }
    canvas.scratchImages();
  } 

  public void setDirectOn(boolean on) {
    directOn = on;
    if (!on) {
      setCursorStringVector(null);
      render_trigger();
    }
  }

  public VisADGroup getDirect() {
    return direct;
  }

  public VisADGroup getNonDirect() {
    return non_direct;
  }

  /** create scene graph root, if none exists, with Transform,
      direct manipulation root, and non-direct-manipulation root;
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

/* WLH 5 April 99 - moved to ProjectionControlJ2D.java
    // initialize scale
    // XXX - for Java2D, scale is controlled in VisADCanvasJ2D
    ProjectionControl proj = getDisplay().getProjectionControl();
    AffineTransform tstart = new AffineTransform(proj.getMatrix());
    // SWAP flip y
    AffineTransform t1 = new AffineTransform(1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
    t1.concatenate(tstart);
    double[] matrix = new double[6];
    t1.getMatrix(matrix);
    try {
      // sets trans
      proj.setMatrix(matrix);
    }
    catch (VisADException e) {
    }
    catch (RemoteException e) {
    }
*/
 
    // create the VisADGroup that is the parent of direct
    // manipulation Data object VisADGroup objects
    direct = new VisADGroup();
    root.addChild(direct);
    directOn = false;

    // create the VisADGroup that is the parent of non-direct-
    // manipulation Data object VisADGroup objects
    non_direct = new VisADGroup();
    root.addChild(non_direct);

    canvas.setDirect(direct, non_direct);

    cursor_trans = new AffineTransform();
    cursor_switch = new VisADSwitch();
    cursor_on = new VisADGroup();
    cursor_off = new VisADGroup();
    cursor_switch.addChild(cursor_off);
    cursor_switch.addChild(cursor_on);
    cursor_switch.setWhichChild(0); // initially off
    cursorOn = false;

    box_switch = new VisADSwitch();
    box_on = new VisADGroup();
    box_off = new VisADGroup();
    box_switch.addChild(box_off);
    box_switch.addChild(box_on);
    box_switch.setWhichChild(1); // initially on
    root.addChild(box_switch);
    boxOn = true;

    scale_switch = new VisADSwitch();
    root.addChild(scale_switch);
    scale_on = new VisADGroup();
    scale_off = new VisADGroup();
    scale_switch.addChild(scale_off);
    scale_switch.addChild(scale_on);
    scale_switch.setWhichChild(0); // initially off
    scaleOn = false;

    return root;
  }

  public void addSceneGraphComponent(VisADGroup group)
         throws DisplayException {
    non_direct.addChild(group);
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
    else {
      render_trigger();
    }
  }

  public void render_trigger() {
    canvas.renderTrigger();
  }

  abstract float[] getCursorColor();

  public boolean anyCursorStringVector() {
    if (cursorOn) return true;

    Enumeration renderers = ((Vector) directs.clone()).elements();
    while (renderers.hasMoreElements()) {
      DirectManipulationRendererJ2D r =
        (DirectManipulationRendererJ2D) renderers.nextElement();
      VisADGroup extra_branch = r.getExtraBranch();
      if (extra_branch != null) return true;
    }

    if (cursorOn || directOn) {
      if (!getCursorStringVector().isEmpty()) return true;
    }

    Vector rendererVector = getDisplay().getRendererVector();
    renderers = rendererVector.elements();
    while (renderers.hasMoreElements()) {
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
      if (!renderer.getExceptionVector().isEmpty()) return true;
    }

    if (getWaitFlag()) return true;
    return false;
  }

  /** whenever cursorOn or directOn is true, display
      Strings in cursorStringVector */
  public void drawCursorStringVector(Graphics graphics,
              AffineTransform tgeometry, int width, int height) {
    // draw cursor
    AffineTransform t = new AffineTransform(tgeometry);
    if (cursorOn) {
      t.concatenate(cursor_trans);
      VisADAppearance appearance = (VisADAppearance) cursor_on.getChild(0);
      if (appearance != null) {
        VisADCanvasJ2D.drawAppearance(graphics, appearance, t, null);
      }
      t = new AffineTransform(tgeometry);
    }

    // draw direct manipulation extra_branch's
    Enumeration renderers = ((Vector) directs.clone()).elements();
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
          VisADCanvasJ2D.drawAppearance(graphics, child, t, null);
        }
      }
    }

    // draw cursor strings in upper left corner of screen
    float[] c3 = getCursorColor();
    graphics.setColor(new Color(c3[0], c3[1], c3[2]));
/* WLH 4 Feb 99
    graphics.setColor(new Color(1.0f, 1.0f, 1.0f));
*/
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
  }

  public DataRenderer findDirect(VisADRay ray) {
    DirectManipulationRendererJ2D renderer = null;
    float distance = Float.MAX_VALUE;
    Enumeration renderers = ((Vector) directs.clone()).elements();
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
    boolean oldOn = scaleOn;
    scaleOn = on;
    if (on) {
      scale_switch.setWhichChild(1); // on
    }
    else {
      scale_switch.setWhichChild(0); // off
    }
    if (scaleOn != oldOn) {
      canvas.scratchImages();
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
    appearance.color_flag = true;
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
      else return new AnimationControlJ2D(display, (RealType) map.getScalar());
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
    else if (type.equals(Display.Text)) {
      return new TextControl(display);
    }
    else {
      return null;
    }
  }

  public DataRenderer makeDefaultRenderer() {
    return new DefaultRendererJ2D();
  }

  public boolean legalDataRenderer(DataRenderer renderer) {
    return (renderer instanceof RendererJ2D);
  }

}

