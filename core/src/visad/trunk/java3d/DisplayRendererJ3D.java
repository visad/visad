//
// DisplayRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;

import visad.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.rmi.*;
import java.util.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import visad.util.Util;

/**
 * <CODE>DisplayRendererJ3D</CODE> is the VisAD abstract super-class for
 * background and metadata rendering algorithms.  These complement
 * depictions of <CODE>Data</CODE> objects created by
 * <CODE>DataRenderer</CODE> objects.<P>
 *
 * <CODE>DisplayRendererJ3D</CODE> also manages the overall relation of
 * <CODE>DataRenderer</CODE> output to Java3D and manages the scene graph.<P>
 *
 * It creates the binding between <CODE>Control</CODE> objects and scene
 * graph <CODE>Behavior</CODE> objects for direct manipulation of
 * <CODE>Control</CODE> objects.<P>
 *
 * <CODE>DisplayRendererJ3D</CODE> is not <CODE>Serializable</CODE> and
 * should not be copied between JVMs.<P>
*/
public abstract class DisplayRendererJ3D
  extends DisplayRenderer
  implements RendererSourceListener
{

  /** View associated with this VirtualUniverse */
  private View view;
  /** VisADCanvasJ3D associated with this VirtualUniverse */
  private VisADCanvasJ3D canvas;

  /** root BranchGroup of scene graph under Locale */
  private BranchGroup root = null;
  /** single TransformGroup between root and BranchGroups for all
   *  Data depictions */
  private TransformGroup trans = null;
  /** BranchGroup between trans and all direct manipulation
   *  Data depictions */
  // WLH 13 March 2000
  // private BranchGroup direct = null;

  // WLH 10 March 2000
  private OrderedGroup non_direct = null;

  /** TransformGroup for ViewPlatform */
  private TransformGroup vpTrans = null;

  /** MouseBehaviorJ3D */
  private MouseBehaviorJ3D mouse = null;
  private double back_clip = 0.0;
  private double front_clip = 0.0;

  /** color of box and cursor */
  private ColoringAttributes box_color = null;
  private ColoringAttributes cursor_color = null;

  /** background attached to root */
  private Background background = null;

  /** TransformGroup between trans and cursor */
  private TransformGroup cursor_trans = null;
  /** single Switch between cursor_trans and cursor */
  private Switch cursor_switch = null;
  /** children of cursor_switch */
  private BranchGroup cursor_on = null, cursor_off = null;
  /** on / off state of cursor */
  private boolean cursorOn = false;
  /** on / off state of direct manipulation location display */
  private boolean directOn = false;

  /** single Switch between trans and box */
  private Switch box_switch = null;
  /** children of box_switch */
  private BranchGroup box_on = null, box_off = null;
  /** on / off state of box */
  private boolean boxOn = false;

  /** single Switch between trans and scales */
  private Switch scale_switch = null;
  /** children of scale_switch */
  private BranchGroup scale_on = null, scale_off = null;
  /** on / off state of cursor in GraphicsModeControl */

  /** Vector of DirectManipulationRenderers */
  private Vector directs = new Vector();

  /** cursor location */
  private float cursorX, cursorY, cursorZ;
  /** normalized direction perpendicular to current cursor plane */
  private float line_x, line_y, line_z;
  /** start value for cursor */
  private float point_x, point_y, point_z;

  public DisplayRendererJ3D () {
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

  public View getView() {
    return view;
  }

  public TransformGroup getViewTrans() {
    return vpTrans;
  }

  /**
   * Get the canvas for this renderer
   * @return  <CODE>VisADCanvasJ3D</CODE> that this renderer uses.
   */
  public VisADCanvasJ3D getCanvas() {
    return canvas;
  }

  /**
   * Capture the display rendition as an image.
   * @param  image of the display.
   */
  public BufferedImage getImage() {
    BufferedImage image = null;
    while (image == null) {
      try {
        ProjectionControl proj = getDisplay().getProjectionControl();
        synchronized (this) {
          canvas.captureFlag = true;
          try {
            proj.setMatrix(proj.getMatrix());
          }
          catch (RemoteException e) { }
          catch (VisADException e) { }
          wait();
        }
      }
      catch(InterruptedException e) {
        // note notify generates a normal return from wait rather
        // than an Exception - control doesn't normally come here
      }
      image = canvas.captureImage;
      canvas.captureImage = null;
    }
    return image;
  }

  void notifyCapture() {
    synchronized (this) {
      notify();
    }
  }

  public BranchGroup getRoot() {
    return root;
  }

  /**
   * Internal method used to initialize newly created
   * <CODE>RendererControl</CODE> with current renderer settings
   * before it is actually connected to the renderer.  This
   * means that changes will not generate <CODE>MonitorEvent</CODE>s.
   */
  public void initControl(RendererControl ctl)
  {
    Color3f c3f = new Color3f();

    // initialize box colors
    if (box_color != null) {
      box_color.getColor(c3f);
      try {
        ctl.setBoxColor(c3f.x, c3f.y, c3f.z);
      } catch (Throwable t) {
        // ignore any initialization problems
      }
    }

    // initialize cursor colors
    if (cursor_color != null) {
      cursor_color.getColor(c3f);
      try {
        ctl.setCursorColor(c3f.x, c3f.y, c3f.z);
      } catch (Throwable t) {
        // ignore any initialization problems
      }
    }

    // initialize background colors
    if (background != null) {
      background.getColor(c3f);
      try {
        ctl.setBackgroundColor(c3f.x, c3f.y, c3f.z);
      } catch (Throwable t) {
        // ignore any initialization problems
      }
    }

    // initialize box visibility
    try {
      ctl.setBoxOn(boxOn);
    } catch (Throwable t) {
      // ignore any initialization problems
    }
  }

  /**
   * Update internal values from those in the <CODE>RendererControl</CODE>.
   * @param evt <CODE>ControlEvent</CODE> generated by a change to the
   *            <CODE>RendererControl</CODE>
   */
  public void controlChanged(ControlEvent evt)
  {
    RendererControl ctl = (RendererControl )evt.getControl();

    float[] ct;
    Color3f c3f = new Color3f();

    // update box colors
    if (box_color != null) {
      ct = ctl.getBoxColor();
      box_color.getColor(c3f);
      if (!Util.isApproximatelyEqual(ct[0], c3f.x) ||
          !Util.isApproximatelyEqual(ct[1], c3f.y) ||
          !Util.isApproximatelyEqual(ct[2], c3f.z))
      {
        box_color.setColor(ct[0], ct[1], ct[2]);
      }
    }

    // update cursor colors
    if (cursor_color != null) {
      ct = ctl.getCursorColor();
      cursor_color.getColor(c3f);
      if (!Util.isApproximatelyEqual(ct[0], c3f.x) ||
          !Util.isApproximatelyEqual(ct[1], c3f.y) ||
          !Util.isApproximatelyEqual(ct[2], c3f.z))
      {
        cursor_color.setColor(ct[0], ct[1], ct[2]);
      }
    }

    // update background colors
    ct = ctl.getBackgroundColor();
    background.getColor(c3f);
    if (!Util.isApproximatelyEqual(ct[0], c3f.x) ||
        !Util.isApproximatelyEqual(ct[1], c3f.y) ||
        !Util.isApproximatelyEqual(ct[2], c3f.z))
    {
      background.setColor(ct[0], ct[1], ct[2]);
    }

    // update box visibility
    boolean on = ctl.getBoxOn();
    if (on != boxOn) {
      boxOn = on;
      box_switch.setWhichChild(boxOn ? 1 : 0);
    }
  }

  public TransformGroup getTrans() {
    return trans;
  }

  public BranchGroup getCursorOnBranch() {
    return cursor_on;
  }

  public BranchGroup getBoxOnBranch() {
    return box_on;
  }

  /**
   * Toggle the cursor in the display
   * @param  on   true to display the cursor, false to hide it.
   */
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

  /**
   * Set the flag for direct manipulation
   * @param  on  true for enabling direct manipulation, false to disable
   */
  public void setDirectOn(boolean on) {
    directOn = on;
    if (!on) {
      setCursorStringVector(null);
    }
  }

/* WLH 13 March 2000
  public BranchGroup getDirect() {
    return direct;
  }
*/

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create special graphics (e.g., 3-D box, SkewT background),
   * any lights, any user interface embedded in scene.
   * @param v
   * @param vpt
   * @param c
   * @return Scene graph root.
   */
  public abstract BranchGroup createSceneGraph(View v, TransformGroup vpt,
                                               VisADCanvasJ3D c);

  /** @deprecated use createBasicSceneGraph(View v, TransformGroup vpt,
         VisADCanvasJ3D c, MouseBehaviorJ3D m, ColoringAttributes bc,
         ColoringAttributes cc)
      instead */
  public BranchGroup createBasicSceneGraph(View v, TransformGroup vpt,
         VisADCanvasJ3D c, MouseBehaviorJ3D m) {
    box_color = new ColoringAttributes();
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    cursor_color = new ColoringAttributes();
    cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    return createBasicSceneGraph(v, vpt, c, m, box_color, cursor_color);
  }

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root.
   * @param v
   * @param vpt
   * @param c
   * @param m
   * @return Scene graph root.
   */
  public BranchGroup createBasicSceneGraph(View v, TransformGroup vpt,
         VisADCanvasJ3D c, MouseBehaviorJ3D m, ColoringAttributes bc,
         ColoringAttributes cc) {
    if (root != null) return root;

    mouse = m;
    view = v;
    vpTrans = vpt;
    box_color = bc;
    cursor_color = cc;
    back_clip = view.getBackClipDistance();
    front_clip = view.getFrontClipDistance();
    // System.out.println("back_clip = " + back_clip +
    //                    " front_clip = " + front_clip);
    // back_clip = 10.0 front_clip = 0.1

    // WLH 14 April 98
    v.setDepthBufferFreezeTransparent(false);
    canvas = c;
    // Create the root of the branch graph
    root = new BranchGroup();
    root.setCapability(Group.ALLOW_CHILDREN_READ);
    root.setCapability(Group.ALLOW_CHILDREN_WRITE);
    root.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    // create the TransformGroup that is the parent of
    // Data object Group objects
    setTransform3D(null);
    root.addChild(trans);

    // create background
    background = new Background();
    background.setCapability(Background.ALLOW_COLOR_WRITE);
    background.setCapability(Background.ALLOW_COLOR_READ);
    float[] ctlBg = getRendererControl().getBackgroundColor();
    background.setColor(ctlBg[0], ctlBg[1], ctlBg[2]);
    BoundingSphere bound2 = new BoundingSphere(new Point3d(0.0,0.0,0.0),2000000.0);
    background.setApplicationBounds(bound2);
    root.addChild(background);

/* WLH 13 April 99 - does nothing
    BoundingBox boundingbox =
      new BoundingBox(new Point3d(-1.0, -1.0, -1.0),
                      new Point3d(1.0, 1.0, 1.0));
    trans.addChild(new BoundingLeaf(boundingbox));
*/

/* WLH 13 Macrh 2000
    // create the BranchGroup that is the parent of direct
    // manipulation Data object BranchGroup objects
    direct = new BranchGroup();
    direct.setCapability(Group.ALLOW_CHILDREN_READ);
    direct.setCapability(Group.ALLOW_CHILDREN_WRITE);
    direct.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    direct.setCapability(Node.ENABLE_PICK_REPORTING);
    trans.addChild(direct);
*/

    // WLH 10 March 2000
    non_direct = new OrderedGroup();
    non_direct.setCapability(Group.ALLOW_CHILDREN_READ);
    non_direct.setCapability(Group.ALLOW_CHILDREN_WRITE);
    non_direct.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    non_direct.setCapability(Node.ENABLE_PICK_REPORTING);
    trans.addChild(non_direct);

    cursor_trans = new TransformGroup();
    cursor_trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    cursor_trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    cursor_trans.setCapability(Group.ALLOW_CHILDREN_READ);
    cursor_trans.setCapability(Group.ALLOW_CHILDREN_WRITE);
    cursor_trans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    trans.addChild(cursor_trans);
    cursor_switch = new Switch();
    cursor_switch.setCapability(Switch.ALLOW_SWITCH_READ);
    cursor_switch.setCapability(Switch.ALLOW_SWITCH_WRITE);
    cursor_trans.addChild(cursor_switch);
    cursor_on = new BranchGroup();
    cursor_on.setCapability(Group.ALLOW_CHILDREN_READ);
    cursor_on.setCapability(Group.ALLOW_CHILDREN_WRITE);
    cursor_off = new BranchGroup();
    cursor_switch.addChild(cursor_off);
    cursor_switch.addChild(cursor_on);
    cursor_switch.setWhichChild(0); // initially off
    cursorOn = false;

    box_switch = new Switch();
    box_switch.setCapability(Switch.ALLOW_SWITCH_READ);
    box_switch.setCapability(Switch.ALLOW_SWITCH_WRITE);
    trans.addChild(box_switch);
    box_on = new BranchGroup();
    box_on.setCapability(Group.ALLOW_CHILDREN_READ);
    box_on.setCapability(Group.ALLOW_CHILDREN_WRITE);
    box_off = new BranchGroup();
    box_switch.addChild(box_off);
    box_switch.addChild(box_on);
    box_switch.setWhichChild(1); // initially on
    try {
      setBoxOn(true);
    } catch (Exception e) {
    }

    scale_switch = new Switch();
    scale_switch.setCapability(Switch.ALLOW_SWITCH_READ);
    scale_switch.setCapability(Switch.ALLOW_SWITCH_WRITE);
    trans.addChild(scale_switch);
    scale_on = new BranchGroup();
    scale_on.setCapability(Group.ALLOW_CHILDREN_READ);
    scale_on.setCapability(Group.ALLOW_CHILDREN_WRITE);
    scale_on.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    scale_off = new BranchGroup();
    scale_switch.addChild(scale_off);
    scale_switch.addChild(scale_on);
    scale_switch.setWhichChild(0); // initially off

    return root;
  }

  /**
   * Get the <CODE>MouseBehavior</CODE> associated with this renderer.
   * @return  the <CODE>MouseBehavior</CODE> used by this renderer to handle
   *          mouse events.
   */
  public MouseBehavior getMouseBehavior() {
    return mouse;
  }

  public void addSceneGraphComponent(Group group) {
    // WLH 10 March 2000
    // trans.addChild(group);
    non_direct.addChild(group);
  }

  public void addDirectManipulationSceneGraphComponent(Group group,
                         DirectManipulationRendererJ3D renderer) {
    // WLH 13 March 2000
    // direct.addChild(group);
    non_direct.addChild(group);
    directs.addElement(renderer);
  }


  public void clearScene(DataRenderer renderer) {
    directs.removeElement(renderer);
  }

  /** 
   * Get the cusor location.
   * @return  cursor location as an array of x, y, and z values
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
/*
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();
    ray.get(origin, direction);
    float o_x = (float) origin.x;
    float o_y = (float) origin.y;
    float o_z = (float) origin.z;
    float d_x = (float) direction.x;
    float d_y = (float) direction.y;
    float d_z = (float) direction.z;
*/
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
    Transform3D t = new Transform3D();
    t.setTranslation(new Vector3f(cursorX, cursorY, cursorZ));
    cursor_trans.setTransform(t);
    if (cursorOn) {
      setCursorStringVector();
    }
  }

  /**
   * Set the cursor location
   * @param  x  x location
   * @param  y  y location
   * @param  z  z location
   */
  public void setCursorLoc(float x, float y, float z) {
    Transform3D t = new Transform3D();
    t.setTranslation(new Vector3f(x, y, z));
    cursor_trans.setTransform(t);
    if (cursorOn) {
      setCursorStringVector();
    }
  }

  /**
   * Whenever <CODE>cursorOn</CODE> or <CODE>directOn</CODE> is true,
   * display Strings in cursorStringVector.
   * @param canvas
   */
  public void drawCursorStringVector(VisADCanvasJ3D canvas) {
    GraphicsContext3D graphics = canvas.getGraphicsContext3D();

    // set cursor color, if possible
    try {
      float[] c3 = getCursorColor();
      Appearance appearance = new Appearance();
      ColoringAttributes color = new ColoringAttributes();
      color.setColor(new Color3f(c3));
      appearance.setColoringAttributes(color);
      graphics.setAppearance(appearance);
    } catch (Exception e) {
    }

    Point3d position1 = new Point3d();
    Point3d position2 = new Point3d();
    Point3d position3 = new Point3d();
    canvas.getPixelLocationInImagePlate(1, 10, position1);
    canvas.getPixelLocationInImagePlate(10, 10, position2);
    canvas.getPixelLocationInImagePlate(1, 1, position3);

    DisplayImpl display = getDisplay();
    if (display != null && display.getGraphicsModeControl() != null) {
      // hack to move text closer to eye
      if (getDisplay().getGraphicsModeControl().getProjectionPolicy() ==
          View.PERSPECTIVE_PROJECTION) {
        Point3d left_eye = new Point3d();
        Point3d right_eye = new Point3d();
        canvas.getLeftEyeInImagePlate(left_eye);
        canvas.getRightEyeInImagePlate(right_eye);
        Point3d eye = new Point3d((left_eye.x + right_eye.x)/2.0,
                                  (left_eye.y + right_eye.y)/2.0,
                                  (left_eye.z + right_eye.z)/2.0);
        double alpha = 0.3;
        position1.x = alpha * position1.x + (1.0 - alpha) * eye.x;
        position1.y = alpha * position1.y + (1.0 - alpha) * eye.y;
        position1.z = alpha * position1.z + (1.0 - alpha) * eye.z;
        position2.x = alpha * position2.x + (1.0 - alpha) * eye.x;
        position2.y = alpha * position2.y + (1.0 - alpha) * eye.y;
        position2.z = alpha * position2.z + (1.0 - alpha) * eye.z;
        position3.x = alpha * position3.x + (1.0 - alpha) * eye.x;
        position3.y = alpha * position3.y + (1.0 - alpha) * eye.y;
        position3.z = alpha * position3.z + (1.0 - alpha) * eye.z;
      }
    }
// end of hack to move text closer to eye

    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    t.transform(position1);
    t.transform(position2);
    t.transform(position3);

    // draw cursor strings in upper left corner of screen
    double[] start = {(double) position1.x,
                      (double) position1.y,
                      (double) position1.z};
    double[] base =  {(double) (position2.x - position1.x),
                      (double) (position2.y - position1.y),
                      (double) (position2.z - position1.z)};
    double[] up =    {(double) (position3.x - position1.x),
                      (double) (position3.y - position1.y),
                      (double) (position3.z - position1.z)};
    if (cursorOn || directOn) {
      Enumeration strings = getCursorStringVector().elements();
      while(strings.hasMoreElements()) {
        String string = (String) strings.nextElement();
        try {
          VisADLineArray array =
            PlotText.render_label(string, start, base, up, false);
          graphics.draw(((DisplayImplJ3D) getDisplay()).makeGeometry(array));
          start[1] -= 1.2 * up[1];
        }
        catch (VisADException e) {
        }
      }
    }

    // draw Exception strings in lower left corner of screen
    double[] startl = {(double) position3.x,
                       (double) -position3.y,
                       (double) position3.z};
    Vector rendererVector = getDisplay().getRendererVector();
    Enumeration renderers = rendererVector.elements();
    while (renderers.hasMoreElements()) {
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
      Vector exceptionVector = renderer.getExceptionVector();
      Enumeration exceptions = exceptionVector.elements();
      while (exceptions.hasMoreElements()) {
        Exception error = (Exception) exceptions.nextElement();
        String string = error.getMessage();
        try {
          VisADLineArray array =
            PlotText.render_label(string, startl, base, up, false);
          graphics.draw(((DisplayImplJ3D) getDisplay()).makeGeometry(array));
          startl[1] += 1.2 * up[1];
        }
        catch (VisADException e) {
        }
      }
    }

    // draw wait flag in lower left corner of screen
    if (getWaitFlag()) {
      try {
        VisADLineArray array =
          PlotText.render_label("please wait . . .", startl, base, up, false);
        graphics.draw(((DisplayImplJ3D) getDisplay()).makeGeometry(array));
        startl[1] += 1.2 * up[1];
      }
      catch (VisADException e) {
      }
    }

    // draw Animation string in lower right corner of screen
    String[] animation_string = getAnimationString();
    if (animation_string[0] != null) {
      int nchars = animation_string[0].length();
      if (nchars < 12) nchars = 12;
      double[] starta = {(double) (-position2.x - nchars *
                                        (position2.x - position1.x)),
                         (double) -position3.y + 1.2 * up[1],
                         // (double) position2.y, WLH 30 April 99
                         (double) position2.z};
      try {
        VisADLineArray array =
          PlotText.render_label(animation_string[0], starta, base, up, false);
        graphics.draw(((DisplayImplJ3D) getDisplay()).makeGeometry(array));
        starta[1] -= 1.2 * up[1];
        if (animation_string[1] != null) {
          array =
            PlotText.render_label(animation_string[1], starta, base, up, false);
          graphics.draw(((DisplayImplJ3D) getDisplay()).makeGeometry(array));
          starta[1] -= 1.2 * up[1];
        }
      }
      catch (VisADException e) {
      }
    }
  }

  /**
   * Find the <CODE>DataRenderer</CODE> that is closest to the ray and
   * uses the specified mouse modifiers for direct manipulation.
   * @param  ray  position to check
   * @param  mouseModifiers  modifiers for mouse clicks
   * @return  closest DataRenderer that uses the specified mouse click
   *          modifiers for direct manipulation or null if there is none.
   */
  public DataRenderer findDirect(VisADRay ray, int mouseModifiers) {
    DirectManipulationRendererJ3D renderer = null;
    float distance = Float.MAX_VALUE;
    Enumeration renderers = ((Vector) directs.clone()).elements();
    while (renderers.hasMoreElements()) {
      DirectManipulationRendererJ3D r =
        (DirectManipulationRendererJ3D) renderers.nextElement();
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

  /**
   * Check to see if there are any <CODE>DirectManipulationRenderer</CODE>s
   * in this display.
   * @return  true if there are any
   */
  public boolean anyDirects() {
    return !directs.isEmpty();
  }

  /**
   * Set the scales on.
   * @param  on   turn on if true, otherwise turn them off
   */
  public void setScaleOn(boolean on) {
    if (on) {
      scale_switch.setWhichChild(1); // on
    }
    else {
      scale_switch.setWhichChild(0); // off
    }
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
    DisplayImplJ3D display = (DisplayImplJ3D) getDisplay();
    GeometryArray geometry = display.makeGeometry(array);
    GraphicsModeControl mode = display.getGraphicsModeControl();
    ColoringAttributes color = new ColoringAttributes();
    color.setColor(scale_color[0], scale_color[1], scale_color[2]);
    Appearance appearance =
      ShadowTypeJ3D.makeAppearance(mode, null, color, geometry, false);
    Shape3D shape = new Shape3D(geometry, appearance);
    BranchGroup group = new BranchGroup();
    group.setCapability(BranchGroup.ALLOW_DETACH);
    group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    group.addChild(shape);
    if (labels != null)
    {
        GeometryArray labelGeometry = display.makeGeometry(labels);
        Appearance labelAppearance =
          ShadowTypeJ3D.makeAppearance(mode, null, null, labelGeometry, true);
        Shape3D labelShape = new Shape3D(labelGeometry, labelAppearance);
        group.addChild(labelShape);
    }
    // may only add BranchGroup to 'live' scale_on
    int dim = getMode2D() ? 2 : 3;
    synchronized (scale_on) {
      int n = scale_on.numChildren();
      int m = dim * axis_ordinal + axis;
      if (m >= n) {
        for (int i=n; i<=m; i++) {
          BranchGroup empty = new BranchGroup();
          empty.setCapability(BranchGroup.ALLOW_DETACH);
          empty.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          scale_on.addChild(empty);
        }
      }
      scale_on.setChild(group, m);
    }
  }

  /**
   * Remove all the scales.
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

  public void setTransform3D(Transform3D t) {
    if (trans == null) {
      trans = new TransformGroup();
      trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      trans.setCapability(Group.ALLOW_CHILDREN_READ);
      trans.setCapability(Group.ALLOW_CHILDREN_WRITE);
      trans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    }
    if (t != null) {
      trans.setTransform(t);
    }
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
    DisplayImplJ3D display = (DisplayImplJ3D) getDisplay();
    if (type == null) return null;
    if (type.equals(Display.XAxis) ||
        type.equals(Display.YAxis) ||
        type.equals(Display.ZAxis) ||
        type.equals(Display.Latitude) ||
        type.equals(Display.Longitude) ||
        type.equals(Display.Radius)) {
      return (ProjectionControlJ3D) display.getProjectionControl();
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
      Control control = display.getControl(AnimationControlJ3D.class);
      if (control != null) return control;
      else return new AnimationControlJ3D(display, (RealType) map.getScalar());
    }
    else if (type.equals(Display.SelectValue)) {
      return new ValueControlJ3D(display);
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

  /**
   * Create the default <CODE>DataRenderer</CODE> for this type of 
   * <CODE>DisplayRenderer</CODE>
   * @return  new default renderer
   */
  public DataRenderer makeDefaultRenderer() {
    return new DefaultRendererJ3D();
  }

  /**
   * Check if the <CODE>DataRenderer</CODE> in question is legal for this
   * <CODE>DisplayRenderer</CODE>
   * @param renderer  <CODE>DataRenderer</CODE> to check
   * @return  true if renderer is a subclass of <CODE>RendererJ3D</CODE>
   */
  public boolean legalDataRenderer(DataRenderer renderer) {
    return (renderer instanceof RendererJ3D);
  }

  public void rendererDeleted(DataRenderer renderer)
  {
    clearScene(renderer);
  }

  /**
   * Add a <CODE>KeyboardBehavior</CODE> for keyboard control of rotation,
   * translation and zoom.  
   * @param  behavior  keyboard behavior to add
   */
  public void addKeyboardBehavior(KeyboardBehaviorJ3D behavior)
  {
    BranchGroup bg = new BranchGroup();
    bg.addChild(behavior);
    trans.addChild(bg);
  }

  public void setWaitFlag(boolean b) {
    boolean old = getWaitFlag();
    super.setWaitFlag(b);
    if (b != old) {
      ProjectionControl proj = getDisplay().getProjectionControl();
      try {
        if (proj != null) proj.setMatrix(proj.getMatrix());
      }
      catch (VisADException e) { }
      catch (RemoteException e) { }
    }
  }

}

