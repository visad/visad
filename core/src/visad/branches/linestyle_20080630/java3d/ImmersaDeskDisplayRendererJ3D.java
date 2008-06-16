//
// ImmersaDeskDisplayRendererJ3D.java
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

package visad.java3d;

import visad.*;

import javax.media.j3d.*;
import javax.vecmath.*;

/*
WandBehaviorJ3D extends MouseBehaviorJ3D:
  how does it get wake up? must poll trackd

  draw ray from wand for direct manipulation (right button)
  also to drive cursor location from wand (center button)
  translate (left button)

connect head tracker to DisplayRendererJ3D.vpTrans
*/

/**
 * <CODE>ImmersaDeskDisplayRendererJ3D</CODE> is the VisAD class for the
 * ImmersaDesk background and metadata rendering algorithm under Java3D.<P>
 */
public class ImmersaDeskDisplayRendererJ3D extends DisplayRendererJ3D {

  /** color of box and cursor */
  private ColoringAttributes box_color = null;
  private ColoringAttributes cursor_color = null;

  /** line of box and cursor */
  private LineAttributes box_line = null;
  private LineAttributes cursor_line = null;

  /** ray information */
  private LineArray ray_geometry = null;
  private ColoringAttributes ray_color = null;
  private Switch ray_switch = null;
  private BranchGroup ray_on = null, ray_off = null;
  /** on / off state of ray */
  private boolean rayOn = false;

  private WandBehaviorJ3D wand = null; // for wand interactions

  /**
   * This is the ImmersaDesk <CODE>DisplayRenderer</CODE>
   * for <CODE>DisplayImplJ3D</CODE>.
   * It draws a 3-D cube around the scene.<P>
   * The left wand button controls the projection by
   * translating in direction wand is pointing
   * The center wand button activates and controls the
   * 3-D cursor by translating the cursor with wand
   * The right wand button is used for direct manipulation by clicking on
   * the depiction of a <CODE>Data</CODE> object and dragging or re-drawing
   * it.<P>
   * Cursor and direct manipulation locations are displayed in RealType
   * values.<P>
   * <CODE>BadMappingExceptions</CODE> and
   * <CODE>UnimplementedExceptions</CODE> are displayed<P>
   */
  public ImmersaDeskDisplayRendererJ3D (int tracker_shmkey, int controller_shmkey)
         throws VisADException {
    super();
    // create WandBehaviorJ3D for wand interactions
    wand = new WandBehaviorJ3D(this, tracker_shmkey, controller_shmkey);
  }

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create 3-D box, lights and <CODE>MouseBehaviorJ3D</CODE> for
   * embedded user interface.
   * @param v
   * @param vpt
   * @param c
   * @return Scene graph root.
   */
  public BranchGroup createSceneGraph(View v, TransformGroup vpt,
                                      VisADCanvasJ3D c) {
    BranchGroup root = getRoot();
    if (root != null) return root;

    getDisplay().setMouseBehavior(wand); // OK - just for transforms
    box_color = new ColoringAttributes();
    cursor_color = new ColoringAttributes();
    root = createBasicSceneGraph(v, vpt, c, wand, box_color, cursor_color); // OK
    TransformGroup trans = getTrans();
    wand.initialize();

    // create the box containing data depictions
    LineArray box_geometry = new LineArray(24, LineArray.COORDINATES);

    // WLH 24 Nov 2000
    box_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
    box_geometry.setCapability(GeometryArray.ALLOW_COLOR_READ);
    box_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    box_geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
    box_geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
    box_geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
    // box_geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
    box_geometry.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);

    box_geometry.setCoordinates(0, box_verts);
    Appearance box_appearance = new Appearance();

    // WLH 2 Dec 2002 in response to qomo2.txt
    box_line = new LineAttributes();
    box_line.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
    box_appearance.setLineAttributes(box_line);

    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    float[] ctlBox = getRendererControl().getBoxColor();
    box_color.setColor(ctlBox[0], ctlBox[1], ctlBox[2]);
    box_appearance.setColoringAttributes(box_color);
    Shape3D box = new Shape3D(box_geometry, box_appearance);
    box.setCapability(Shape3D.ALLOW_GEOMETRY_READ); // WLH 24 Nov 2000
    box.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    BranchGroup box_on = getBoxOnBranch();
    box_on.addChild(box);

    Appearance cursor_appearance = new Appearance();

    // WLH 2 Dec 2002 in response to qomo2.txt
    cursor_line = new LineAttributes();
    cursor_line.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
    cursor_appearance.setLineAttributes(cursor_line);

    cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    float[] ctlCursor = getRendererControl().getCursorColor();
    cursor_color.setColor(ctlCursor[0], ctlCursor[1], ctlCursor[2]);
    cursor_appearance.setColoringAttributes(cursor_color);

    BranchGroup cursor_on = getCursorOnBranch();
    LineArray cursor_geometry = new LineArray(6, LineArray.COORDINATES);
    cursor_geometry.setCapability(GeometryArray.ALLOW_COLOR_READ);
    cursor_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    cursor_geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
    cursor_geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
    cursor_geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
    // cursor_geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
    cursor_geometry.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
    
    cursor_geometry.setCoordinates(0, cursor_verts);
    Shape3D cursor = new Shape3D(cursor_geometry, cursor_appearance);
    cursor.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    cursor.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    cursor_on.addChild(cursor);

    // create the ray
    ray_switch = new Switch();
    ray_switch.setCapability(Switch.ALLOW_SWITCH_READ);
    ray_switch.setCapability(Switch.ALLOW_SWITCH_WRITE);
    trans.addChild(ray_switch);
    ray_on = new BranchGroup();
    ray_on.setCapability(Group.ALLOW_CHILDREN_READ);
    ray_on.setCapability(Group.ALLOW_CHILDREN_WRITE);
    ray_off = new BranchGroup();
    ray_off.setCapability(Group.ALLOW_CHILDREN_READ);
    ray_switch.addChild(ray_off);
    ray_switch.addChild(ray_on);
    ray_switch.setWhichChild(1); // initially on
    ray_geometry = new LineArray(2, LineArray.COORDINATES);
    ray_geometry.setCapability(GeometryArray.ALLOW_COLOR_READ);
    ray_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    ray_geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
    ray_geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
    ray_geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
    // ray_geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
    ray_geometry.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
    ray_geometry.setCoordinates(0, init_ray_verts);
    ray_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    ray_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
    Appearance ray_appearance = new Appearance();
    ray_color = new ColoringAttributes();
    ray_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    ray_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    ray_color.setColor(1.0f, 1.0f, 1.0f); // white ray
    ray_appearance.setColoringAttributes(ray_color);
    Shape3D ray = new Shape3D(ray_geometry, ray_appearance);
    ray.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    ray.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    ray_on.addChild(ray);

    // create ambient light, directly under root (not transformed)
/* WLH 27 Jan 98
    Color3f color = new Color3f(0.4f, 0.4f, 0.4f);
*/
    Color3f color = new Color3f(0.6f, 0.6f, 0.6f);
    AmbientLight light = new AmbientLight(color);
    BoundingSphere bounds =
      new BoundingSphere(new Point3d(0.0,0.0,0.0), 2000000.0);
    light.setInfluencingBounds(bounds);
    root.addChild(light);

    // create directional lights, directly under root (not transformed)
    Color3f dcolor = new Color3f(0.9f, 0.9f, 0.9f);
    Vector3f direction1 = new Vector3f(0.0f, 0.0f, 1.0f);
    Vector3f direction2 = new Vector3f(0.0f, 0.0f, -1.0f);
    DirectionalLight light1 =
      new DirectionalLight(true, dcolor, direction1);
    light1.setInfluencingBounds(bounds);
    DirectionalLight light2 =
      new DirectionalLight(true, dcolor, direction2);
    light2.setInfluencingBounds(bounds);
    root.addChild(light1);
    root.addChild(light2);

    return root;
  }

  public void setRayOn(boolean on, float[] ray_verts) {
    rayOn = on;
    if (ray_switch != null) {
      if (on) {
        ray_geometry.setCoordinates(0, ray_verts);
        ray_switch.setWhichChild(1); // set ray on
      }
      else {
        ray_switch.setWhichChild(0); // set ray off
      }
    }
  }

  // WLH 24 Nov 2000
  public void setBoxAspect(double[] aspect) {
    float[] new_verts = new float[box_verts.length];
    for (int i=0; i<box_verts.length; i+=3) {
      new_verts[i] = (float) (box_verts[i] * aspect[0]);
      new_verts[i+1] = (float) (box_verts[i+1] * aspect[1]);
      new_verts[i+2] = (float) (box_verts[i+2] * aspect[2]);
    }
    BranchGroup box_on = getBoxOnBranch();
    Shape3D box = (Shape3D) box_on.getChild(0);
    LineArray box_geometry = (LineArray) box.getGeometry();
    box_geometry.setCoordinates(0, new_verts);
  }

  // WLH 2 Dec 2002 in response to qomo2.txt
  public void setLineWidth(float width) {
    box_line.setLineWidth(width);
    cursor_line.setLineWidth(width);
  }

  private static final float[] box_verts = {
     // front face
         -1.0f, -1.0f,  1.0f,                       -1.0f,  1.0f,  1.0f,
         -1.0f,  1.0f,  1.0f,                        1.0f,  1.0f,  1.0f,
          1.0f,  1.0f,  1.0f,                        1.0f, -1.0f,  1.0f,
          1.0f, -1.0f,  1.0f,                       -1.0f, -1.0f,  1.0f,
     // back face
         -1.0f, -1.0f, -1.0f,                       -1.0f,  1.0f, -1.0f,
         -1.0f,  1.0f, -1.0f,                        1.0f,  1.0f, -1.0f,
          1.0f,  1.0f, -1.0f,                        1.0f, -1.0f, -1.0f,
          1.0f, -1.0f, -1.0f,                       -1.0f, -1.0f, -1.0f,
     // connectors
         -1.0f, -1.0f,  1.0f,                       -1.0f, -1.0f, -1.0f,
         -1.0f,  1.0f,  1.0f,                       -1.0f,  1.0f, -1.0f,
          1.0f,  1.0f,  1.0f,                        1.0f,  1.0f, -1.0f,
          1.0f, -1.0f,  1.0f,                        1.0f, -1.0f, -1.0f
  };

  private static final float[] cursor_verts = {
          0.0f,  0.0f,  0.1f,                        0.0f,  0.0f, -0.1f,
          0.0f,  0.1f,  0.0f,                        0.0f, -0.1f,  0.0f,
          0.1f,  0.0f,  0.0f,                       -0.1f,  0.0f,  0.0f
  };

  private static final float[] init_ray_verts = {
          0.0f,  0.0f,  0.0f,                        0.0f,  0.0f, -10.0f
  };

}

