
//
// DisplayImplJ2D.java
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

/*

visad.java2d design:
 
0. common code in ClassNameJ2D and ClassNameJ3D
     DirectManipulationRendererJ2/3D
       common methods in visad.DataRenderer
     GraphicsModeControlJ2/3D
       why isn't GraphicsModeControl a class
       where we can put common methods ? ? ? ?
       all *ControlJ3D extend Control and implement interfaces
       --> looks like interfaces could be abstract classes
             extending Control
       ** done for GraphicsModeControl and ProjectionControl
       ** not for AnimationControl and ValueControl
     MouseBehaviorJ2/3D
       extend visad.MouseBehavior
       visad.MouseHelper.processEvents
       remove Transform3D constructor from make_matrix
     DefaultDisplayRendererJ2D/TwoDDisplayRendererJ3D
       common methods in visad.DisplayRenderer
     ShadowFunctionOrSetTypeJ2/3D, ShadowRealTypeJ2/3D and
       ShadowTupleTypeJ2/3D ShadowTypeJ2/3D adapt common
       methods in visad.Shadow*Type

1. add VisAD-specific scene graph classes:
 
     canvas, root, trans, direct, cursor_trans & other
       scene graph stuff in DisplayRendererJ2D

     ** done
     VisADRay
     VisADSceneGraphObject
       VisADGroup
         VisADSwitch
       VisADAppearance
         incl VisADGeometryArray
         incl Image "texture"
         incl red, green, blue, alpha
         linewidth and pointsize in GraphicsModeControl
       (VisADTexture2D not needed; Image in VisADAppearance)
         texture.setImage(0, image2d);
         new Shape3D(geometry, appearance);
         appearance.setTexture(texture);
       (VisADShape not needed; VisADGeometryArray in VisADAppearance)
       (VisADTransform not needed; trans in DisplayRendererJnD)
       (hence VisADBranchGroup not needed; a VisADBranchGroup
        is a VisADGroup that is not a VisADSwitch)
 
 
2. add VisADSceneGraphObject as parent of
   existing VisAD-specific scene graph classes:

     ** done
     VisADSceneGraphObject
       VisADGeometryArray
         VisADIndexedTriangleStripArray
         VisADLineArray
         VisADLineStripArray
         VisADPointArray
         VisADTriangleArray
         VisADQuadArray
 
3. VisADCanvasJ2D
     add BufferedImage[] array with element for each animation step
     AnimationControlJ2D.init() invoked in paint

4. DisplayImplJ2D.doAction
     super.doAction()
     if any scene graph changes canvas.scratchImages()

5. AnimationControlJ2D.selectSwitches()
     invoke canvas.renderTrigger() instead of init()
 
6. ValueControlJ2D.setValue()
     invoke canvas.scratchImages()

7. ProjectionControlJ2D.setMatrix()
     invoke canvas.scratchImages()
 
8. VisADCanvasJ2D.paint()
     invokes DisplayRendererJ2D.drawCursorStringVector()
     which draws cursor strings, Exception strings,
       WaitFlag & Animation string
     add draw of extra_branch from
       DirectManipulationRendererJ2D.addPoint
 
9. DirectManipulationRendererJ2D
     doTransform: create branch and extra_branch
     addPoint: add to extra_branch

10. MouseBehaviorJ2D
     just do AWTEvent's

11. DefaultDisplayRendererJ2D = TwoDDisplayRendererJ2D
     legalDisplayScalar?

12. DisplayAppletJ2D delete

13. DisplayImplJ2D.makeGeometry() delete

14. RemoveBehaviorJ2D delete

15. UniverseBuilderJ2D delete

16. resize event on Display component, and
      rebuild BufferedImage[] array

17. AnimationSetControlJ2D.setSet() (new class)
      canvas.createImages(s.getLength())

18. VisADCanvasJ2D renderThread

*/

package visad.java2d;

import visad.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.*;

import java.awt.*;

/**
   DisplayImplJ2D is the VisAD class for displays that use
   Java 3D.  It is runnable.<P>

   DisplayImplJ2D is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImplJ2D extends DisplayImpl {

  /** legal values for api */
  public static final int JPANEL = 1;
  public static final int OFFSCREEN = 2;

  private ProjectionControlJ2D projection = null;
  private GraphicsModeControlJ2D mode = null;

  /** flag to scratch images in VisADCanvasJ2D */
  private boolean scratch;

  /** construct a DisplayImpl for Java2D with the
      default DisplayRenderer, in a JFC JPanel */
  public DisplayImplJ2D(String name)
         throws VisADException, RemoteException {
    this(name, null, JPANEL);
  }

  /** construct a DisplayImpl for Java2D with a non-default
      DisplayRenderer, in a JFC JPanel */
  public DisplayImplJ2D(String name, DisplayRendererJ2D renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL);
  }

  /** constructor with default DisplayRenderer */
  public DisplayImplJ2D(String name, int api)
         throws VisADException, RemoteException {
    this(name, null, api);
  }

  /** construct a DisplayImpl for Java2D with a non-default
      DisplayRenderer;
      in a JFC JPanel if api == DisplayImplJ2D.JPANEL */
  public DisplayImplJ2D(String name, DisplayRendererJ2D renderer, int api)
         throws VisADException, RemoteException {
    this(name, renderer, api, 300, 300);
  }

  /** construct a DisplayImpl for Java2D for offscreen rendering,
      with size geiven by width and height; getComponent() of this
      returns null, but display is accesible via getImage() */
  public DisplayImplJ2D(String name, int width, int height)
         throws VisADException, RemoteException {
    this(name, null, OFFSCREEN, width, height);
  }

  /** offscreen constructor with non-default DisplayRenderer */
  public DisplayImplJ2D(String name, DisplayRendererJ2D renderer,
                        int width, int height)
         throws VisADException, RemoteException {
    this(name, renderer, OFFSCREEN, width, height);
  }

  /** most general constructor */
  public DisplayImplJ2D(String name, DisplayRendererJ2D renderer, int api,
                        int width, int height)
         throws VisADException, RemoteException {
    super(name, renderer);

    // a GraphicsModeControl always exists
    mode = new GraphicsModeControlJ2D(this);
    addControl(mode);
    // a ProjectionControl always exists
    projection = new ProjectionControlJ2D(this);
    addControl(projection);

    if (api == JPANEL) {
      Component component = new DisplayPanelJ2D(this);
      setComponent(component);
    }
    else if (api == OFFSCREEN) {
      Component component = null;
      VisADCanvasJ2D canvas = new VisADCanvasJ2D(renderer, width, height);
      VisADGroup scene = renderer.createSceneGraph(canvas);
    }
    else {
      throw new DisplayException("DisplayImplJ2D: bad graphicsApi");
    }

    // a GraphicsModeControl always exists
    mode = new GraphicsModeControlJ2D(this);
    addControl(mode);
    // a ProjectionControl always exists
    projection = new ProjectionControlJ2D(this);
    addControl(projection);

  }

  protected DisplayRenderer getDefaultDisplayRenderer() {
    return new DefaultDisplayRendererJ2D();
  }

  public ProjectionControl getProjectionControl() {
    return projection;
  }

  public GraphicsModeControl getGraphicsModeControl() {
    return mode;
  }

  public void setScratch() {
    scratch = true;
  }

  public void clearMaps() throws VisADException, RemoteException {
    super.clearMaps();
    DisplayRendererJ2D displayRenderer =
      (DisplayRendererJ2D) getDisplayRenderer();
    VisADCanvasJ2D canvas = displayRenderer.getCanvas();
    if (canvas != null) canvas.scratchImages();
  }

  public void doAction() throws VisADException, RemoteException {
    scratch = false;
    super.doAction();
    if (scratch) {
/*
System.out.println("DisplayImplJ2D.doAction: scratch = " + scratch);
*/
      VisADCanvasJ2D canvas =
        ((DisplayRendererJ2D) getDisplayRenderer()).getCanvas();
      canvas.scratchImages();
    }
  }

}

