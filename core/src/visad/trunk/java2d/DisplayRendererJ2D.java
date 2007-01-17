//
// DisplayRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.image.*;
import java.awt.geom.AffineTransform;

import java.rmi.*;
import java.util.*;
import java.io.*;
import com.sun.image.codec.jpeg.*;


import visad.util.Util;

/**
 * <CODE>DisplayRendererJ2D</CODE> is the VisAD abstract super-class for
 * background and metadata rendering algorithms.  These complement
 * depictions of <CODE>Data</CODE> objects created by
 * <CODE>DataRenderer</CODE> objects.<P>
 *
 * <CODE>DisplayRendererJ2D</CODE> also manages the overall relation of
 * <CODE>DataRenderer</CODE> output to Java2D and manages the VisAD scene
 * graph.<P>
 *
 * It creates the binding between <CODE>Control</CODE> objects and scene
 * graph <CODE>Behavior</CODE> objects for direct manipulation of
 * <CODE>Control</CODE> objects.<P>
 *
 * <CODE>DisplayRendererJ2D</CODE> is not <CODE>Serializable</CODE> and
 * should not be copied between JVMs.<P>
*/
public abstract class DisplayRendererJ2D
  extends DisplayRenderer
  implements RendererSourceListener
{

  private VisADCanvasJ2D canvas;

  /** root VisADGroup of scene graph under Locale */
  private VisADGroup root = null;
  /** single AffineTransform applied to all
   *  Data depictions */
  private AffineTransform trans = null;
  /** VisADGroup between root and all direct manipulation
   *  Data depictions */
  private VisADGroup direct = null;
  /** VisADGroup between root and all non-direct-manipulation
   *  Data depictions */
  private VisADGroup non_direct = null;

  /** MouseBehaviorJ2D */
  private MouseBehaviorJ2D mouse = null;

  /** box outline for data */
  private VisADAppearance box = null;
  /** cursor */
  private VisADAppearance cursor = null;

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

  /**
   * Specify <CODE>DisplayImpl</CODE> to be rendered.
   * @param dpy <CODE>Display</CODE> to render.
   * @exception VisADException If a <CODE>DisplayImpl</CODE> has already
   *                           been specified.
   */
  public void setDisplay(DisplayImpl dpy)
    throws VisADException
  {
    super.setDisplay(dpy);
    dpy.addRendererSourceListener(this);
    boxOn = getRendererControl().getBoxOn();
  }

  public VisADGroup getRoot() {
    return root;
  }

  public void setClip(float xlow, float xhi, float ylow, float yhi) {
    canvas.setClip(xlow, xhi, ylow, yhi);
  }

  public void unsetClip() {
    canvas.unsetClip();
  }

  /**
   * Internal method used to initialize newly created
   * <CODE>RendererControl</CODE> with current renderer settings
   * before it is actually connected to the renderer.  This
   * means that changes will not generate <CODE>MonitorEvent</CODE>s.
   * @param ctl RendererControl to initialize
   */
  public void initControl(RendererControl ctl)
  {
    // initialize box colors
    if (box != null) {
      try {
        ctl.setBoxColor(box.red, box.green, box.blue);
      } catch (Throwable t) {
        // ignore any initialization problems
      }
    }

    // initialize cursor colors
    if (cursor != null) {
      try {
        ctl.setBoxColor(cursor.red, cursor.green, cursor.blue);
      } catch (Throwable t) {
        // ignore any initialization problems
      }
    }

    // initialize background colors
    if (canvas != null) {
      float[] ca = canvas.getBackgroundColor();
      try {
        ctl.setBackgroundColor(ca[0], ca[1], ca[2]);
      } catch (Throwable t) {
        // ignore any initialization problems
      }
    }

    // update box visibility
    try {
      ctl.setBoxOn(boxOn);
    } catch (Throwable t) {
      // ignore any initialization problems
    }
  }

  /**
   * Utility routine which updates a <CODE>VisADAppearance</CODE> object
   * to use the colors specified in the <CODE>float[3]</CODE> array.
   * @param appear Currently used colors.
   * @param colors New colors.
   * @return <CODE>true</CODE> if any color was updated.
   */
  private final boolean updateColors(VisADAppearance appear, float[] colors)
  {
    if (appear == null) {
      return false;
    }

    boolean fixed = false;
    for (int i = 0; i < 3; i++) {

      float a;
      switch (i) {
      case 0: a = appear.red; break;
      case 1: a = appear.green; break;
      default:
      case 2: a = appear.blue; break;
      }

      if (!Util.isApproximatelyEqual(a, colors[i])) {
        switch (i) {
        case 0: appear.red = colors[i]; break;
        case 1: appear.green = colors[i]; break;
        default:
        case 2: appear.blue = colors[i]; break;
        }
        fixed = true;
      }
    }

    return fixed;
  }

  /**
   * Update internal values from those in the <CODE>RendererControl</CODE>.
   * @param evt <CODE>ControlEvent</CODE> generated by a change to the
   *            <CODE>RendererControl</CODE>
   */
  public void controlChanged(ControlEvent evt)
    throws VisADException, RemoteException
  {
    RendererControl ctl = (RendererControl )evt.getControl();

    float[] color;

    // update box colors
    color = ctl.getBoxColor();
    if (updateColors(box, color)) {
      getCanvas().scratchImages();
    }

    // update cursor colors
    color = ctl.getCursorColor();
    if (updateColors(cursor, color)) {
      render_trigger();
    }

    // update background colors
    float[] ca = canvas.getBackgroundColor();
    float[] ct = ctl.getBackgroundColor();
    if (!Util.isApproximatelyEqual(ca[0], ct[0]) ||
        !Util.isApproximatelyEqual(ca[1], ct[1]) ||
        !Util.isApproximatelyEqual(ca[2], ct[2]))
    {
      canvas.setBackgroundColor(ct[0], ct[1], ct[2]);
      canvas.scratchImages();
    }

    // update box visibility
    boolean on = ctl.getBoxOn();
    if (on != boxOn) {
      boxOn = on;
      box_switch.setWhichChild(boxOn ? 1 : 0);
      canvas.scratchImages();
    }
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

    if (getDisplay().getComponent() == null) {
      // offscreen
      // this is a total hack; works for reasons not understood
      for (int i=0; i<2; i++) {
        try {
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          // FileOutputStream bout = new FileOutputStream("junk");
          JPEGEncodeParam jepar =
            JPEGCodec.getDefaultJPEGEncodeParam(image);
          jepar.setQuality( 1.0f, true);
          JPEGImageEncoder jpege = JPEGCodec.createJPEGEncoder(bout);
          jpege.encode(image, jepar);
          bout.flush();
          bout.close();
        }
        catch (IOException e) {
        }
      }
// System.out.println("ByteArrayOutputStream done");
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

  /**
   * Create scene graph root, if none exists, with Transform,
   * direct manipulation root, and non-direct-manipulation root;
   * create special graphics (e.g., 3-D box, SkewT background),
   * any lights, any user interface embedded in scene.
   * @param c
   * @return Scene graph root.
   * @exception DisplayException
   */
  public abstract VisADGroup createSceneGraph(VisADCanvasJ2D c)
         throws DisplayException;

  /** @deprecated use createBasicSceneGraph(VisADCanvasJ2D c,
         MouseBehaviorJ2D m, VisADAppearance bx, VisADAppearance cr)
      instead */
  public VisADGroup createBasicSceneGraph(VisADCanvasJ2D c,
         MouseBehaviorJ2D m) throws DisplayException {
    VisADAppearance box = new VisADAppearance();
    VisADAppearance cursor = new VisADAppearance();
    return createBasicSceneGraph(c, m, box, cursor);
  }

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root.
   * @param c
   * @param m
   * @return Scene graph root.
   * @exception DisplayException
   */
  public VisADGroup createBasicSceneGraph(VisADCanvasJ2D c,
         MouseBehaviorJ2D m, VisADAppearance bx, VisADAppearance cr)
         throws DisplayException {
    if (root != null) return root;
    mouse = m;
    canvas = c;
    box = bx;
    cursor = cr;
    canvas.addMouseBehavior(mouse);
    // Create the root of the branch graph
    root = new VisADGroup();

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
    try {
      setBoxOn(true);
    } catch (Exception e) {
    }

    scale_switch = new VisADSwitch();
    root.addChild(scale_switch);
    scale_on = new VisADGroup();
    scale_off = new VisADGroup();
    scale_switch.addChild(scale_off);
    scale_switch.addChild(scale_on);
    scale_switch.setWhichChild(0); // initially off
    scaleOn = false;

    // set background color
    float[] ctlBg = getRendererControl().getBackgroundColor();
    canvas.setBackgroundColor(ctlBg[0], ctlBg[1], ctlBg[2]);

    return root;
  }

  public MouseBehavior getMouseBehavior() {
    return mouse;
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

  /**
   * Returns the location of the last unmodified, middle mouse button press.
   * @return			The location of the last unmodified, middle
   *				mouse button press as (Display.XAxis,
   *				Display.YAxis, 0.0).
   */
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

    if (getWaitFlag() && getWaitMessageVisible()) return true;
    return false;
  }

  /**
   * Whenever <CODE>cursorOn</CODE> or <CODE>directOn</CODE> is
   * <CODE>true</CODE>, display Strings in cursorStringVector.
   * @param graphics
   * @param tgeometry
   * @param width
   * @param height
   */
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

    // set cursor color
    float[] c3;
    try {
      c3 = getCursorColor();
    } catch (Exception e) {
      System.err.println("Yikes!  Couldn't get cursor color");
      // default to white
      c3 = new float[] {1.0f, 1.0f, 1.0f};
    }
    graphics.setColor(new Color(c3[0], c3[1], c3[2]));

    // draw cursor strings in upper left corner of screen
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
    if (getWaitFlag() && getWaitMessageVisible()) {
      graphics.drawString("please wait . . .", x, y);
      y -= 12;
    }
  }

  public DataRenderer findDirect(VisADRay ray, int mouseModifiers) {
    DirectManipulationRendererJ2D renderer = null;
    float distance = Float.MAX_VALUE;
    Enumeration renderers = ((Vector) directs.clone()).elements();
    while (renderers.hasMoreElements()) {
      DirectManipulationRendererJ2D r =
        (DirectManipulationRendererJ2D) renderers.nextElement();
      if (r.getEnabled()) {
        r.setLastMouseModifiers(mouseModifiers);
        float d = r.checkClose(ray.position, ray.vector);
        if (d < distance) {
          distance = d;
          renderer = r;
        }
      }
    }
    if (distance < getPickThreshhold()) {
      return renderer;
    }
    else {
      return null;
    }
  }

  public boolean anyDirects() {
    return !directs.isEmpty();
  }

  /**
   * Allow scales to be displayed if they are set on.
   * @param  on   true to turn them on, false to set them invisible
   */
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

  /**
   * Set the scale for the appropriate axis.
   * @param  axisScale  AxisScale for this scale
   * @throws  VisADException  couldn't set the scale
   */
  public void setScale(AxisScale axisScale)
         throws VisADException {
    setScale(axisScale.getAxis(),
             axisScale.getAxisOrdinal(),
             axisScale.getScaleArray(),
             axisScale.getLabelArray(),
             axisScale.getColor().getColorComponents(null));
  }

  /**
   * Set the scale for the appropriate axis.
   * @param  axis  axis for this scale (0 = XAxis, 1 = YAxis, 2 = ZAxis)
   * @param  axis_ordinal  position along the axis
   * @param  array   <CODE>VisADLineArray</CODE> representing the scale plot
   * @param  scale_color   array (dim 3) representing the red, green and blue
   *                       color values.
   * @throws  VisADException  couldn't set the scale
   */
  public void setScale(int axis, int axis_ordinal,
              VisADLineArray array, float[] scale_color)
         throws VisADException {
    setScale(axis, axis_ordinal, array, null, scale_color);
  }

  /**
   * Set the scale for the appropriate axis.
   * @param  axis  axis for this scale (0 = XAxis, 1 = YAxis, 2 = ZAxis)
   * @param  axis_ordinal  position along the axis
   * @param  array   <CODE>VisADLineArray</CODE> representing the scale plot
   * @param  labels  <CODE>VisADTriangleArray</CODE> representing the labels
   *                 created using a font (can be null)
   * @param  scale_color   array (dim 3) representing the red, green and blue
   *                       color values.
   * @throws  VisADException  couldn't set the scale
   */
  public void setScale(int axis, int axis_ordinal,
              VisADLineArray array, VisADTriangleArray labels,
              float[] scale_color)
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
    if (labels != null)
    {
      VisADAppearance labelAppearance = new VisADAppearance();
      labelAppearance.red = scale_color[0];
      labelAppearance.green = scale_color[1];
      labelAppearance.blue = scale_color[2];
      labelAppearance.color_flag = true;
      labelAppearance.array = labels;
      group.addChild(labelAppearance);
    }

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
    canvas.scratchImages(); // WLH 26 Jan 2001
  }

  /**
   * Remove all the scales being rendered.
   */
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

  /**
   * Remove a particular scale being rendered.
   * @param axisScale  scale to be removed
   */
  public void clearScale(AxisScale axisScale) {
    int axis = axisScale.getAxis();
    int axis_ordinal = axisScale.getAxisOrdinal();
    int dim = getMode2D() ? 2 : 3;
    try {
      synchronized (scale_on) {
        int n = scale_on.numChildren();
        int m = dim * axis_ordinal + axis;
        if (m >= n) {
          for (int i=n; i<=m; i++) {
            VisADGroup empty = new VisADGroup();
            scale_on.addChild(empty);
          }
        }
        VisADGroup empty = new VisADGroup();
        scale_on.setChild(empty, m);
        canvas.scratchImages(); 
      }
    } catch (VisADException ve) {;}
  }

  public void setTransform2D(AffineTransform t) {
    trans = new AffineTransform(t);
  }

  /**
   * Factory for constructing a subclass of <CODE>Control</CODE>
   * appropriate for the graphics API and for this
   * <CODE>DisplayRenderer</CODE>; invoked by <CODE>ScalarMap</CODE>
   * when it is <CODE>addMap()</CODE>ed to a <CODE>Display</CODE>.
   * @param map The <CODE>ScalarMap</CODE> for which a <CODE>Control</CODE>
   *            should be built.
   * @return The appropriate <CODE>Control</CODE>.
   */
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
             type.equals(Display.Flow1Z) ||
             type.equals(Display.Flow1Elevation) ||
             type.equals(Display.Flow1Azimuth) ||
             type.equals(Display.Flow1Radial)) {
      Control control = display.getControl(Flow1Control.class);
      if (control != null) return control;
      else return new Flow1Control(display);
    }
    else if (type.equals(Display.Flow2X) ||
             type.equals(Display.Flow2Y) ||
             type.equals(Display.Flow2Z) ||
             type.equals(Display.Flow2Elevation) ||
             type.equals(Display.Flow2Azimuth) ||
             type.equals(Display.Flow2Radial)) {
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

  public void rendererDeleted(DataRenderer renderer)
  {
    clearScene(renderer);
  }

  public void setLineWidth(float width) {
  }

  /**
   * Add a <CODE>KeyboardBehavior</CODE> for keyboard control of translation 
   * and zoom.  This adds a <CODE>KeyListener</CODE> to the VisADCanvasJ2D to 
   * handle the behaviors for the arrow keys.  Do not use this in conjunction 
   * with other <CODE>KeyListener</CODE>s that handle events for the arrow keys.
   * @param  behavior  keyboard behavior to add
   */
  public void addKeyboardBehavior(KeyboardBehaviorJ2D behavior)
  {
    getCanvas().addKeyboardBehavior(behavior);
  }

  public void setWaitFlag(boolean b) {
    boolean old = getWaitFlag();
    super.setWaitFlag(b);
    if (b != old) {
      if (canvas != null) canvas.renderTrigger();
    }
  }

  public int getTextureWidthMax() {
    return Integer.MAX_VALUE;
  }

  public int getTextureHeightMax() {
    return Integer.MAX_VALUE;
  }

}
