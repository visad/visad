//
// DisplayImplJ3D.java
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

/*

VisAD display logic efficiencies:

1. Special cases of MathTypes, ScalarMaps and Sets
   for more efficient memory use

2. Unify java2d and java3d logic

3. Make scene graph 'live' during build

4. Attach 'ShadowData' tree of scene graph nodes to DataDisplayLink,
   use it to replace scene graph components during replace

*/

package visad.java3d;

import visad.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.*;

import java.awt.*;

import javax.media.j3d.*;
import com.sun.j3d.utils.applet.MainFrame;
// import com.sun.j3d.utils.applet.AppletFrame;

/**
   DisplayImplJ3D is the VisAD class for displays that use
   Java 3D.  It is runnable.<P>

   DisplayImplJ3D is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImplJ3D extends DisplayImpl {

  /** distance behind for surfaces in 2-D mode */
  public static final float BACK2D = -2.0f;

  /**
   * Use a parallel projection view
   * @see GraphicsModeControlJ3D#setProjectionPolicy
   */
  public static final int PARALLEL_PROJECTION =
    javax.media.j3d.View.PARALLEL_PROJECTION;

  /**
   * Use a perspective projection view. This is the default.
   * @see GraphicsModeControlJ3D#setProjectionPolicy
   */
  public static final int PERSPECTIVE_PROJECTION =
    javax.media.j3d.View.PERSPECTIVE_PROJECTION;

  /** Render polygonal primitives by filling the interior of the polygon
      @see GraphicsModeControlJ3D#setPolygonMode */
  public static final int POLYGON_FILL =
    javax.media.j3d.PolygonAttributes.POLYGON_FILL;

  /**
   * Render polygonal primitives as lines drawn between consecutive vertices
   * of the polygon.
   * @see GraphicsModeControlJ3D#setPolygonMode
   */
  public static final int POLYGON_LINE =
    javax.media.j3d.PolygonAttributes.POLYGON_LINE;

  /**
   * Render polygonal primitives as points drawn at the vertices of
   * the polygon.
   * @see GraphicsModeControlJ3D#setPolygonMode
   */
  public static final int POLYGON_POINT =
    javax.media.j3d.PolygonAttributes.POLYGON_POINT;

  /**
   * Use the nicest available method for transparency.
   * @see GraphicsModeControlJ3D#setTransparencyMode
   */
  public static final int NICEST =
    javax.media.j3d.TransparencyAttributes.NICEST;

  /**
   * Use the fastest available method for transparency.
   * @see GraphicsModeControlJ3D#setTransparencyMode
   */
  public static final int FASTEST =
    javax.media.j3d.TransparencyAttributes.FASTEST;

  /** Field for specifying unknown API type */
  public static final int UNKNOWN = 0;
  /** Field for specifying that the DisplayImpl be created in a JPanel */
  public static final int JPANEL = 1;
  /** Field for specifying that the DisplayImpl be created in an Applet */
  public static final int APPLETFRAME = 2;
  /** Field for specifying that the DisplayImpl transforms but does not render */
  public static final int TRANSFORM_ONLY = 3;

  /** this is used for APPLETFRAME */
  private DisplayAppletJ3D applet = null;

  private ProjectionControlJ3D projection = null;
  private GraphicsModeControlJ3D mode = null;
  private int apiValue = UNKNOWN;

  /** construct a DisplayImpl for Java3D with the
      default DisplayRenderer, in a JFC JPanel */
  public DisplayImplJ3D(String name)
         throws VisADException, RemoteException {
    this(name, null, JPANEL, null);
  }

  /** construct a DisplayImpl for Java3D with a non-default
      DisplayRenderer, in a JFC JPanel */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL, null);
  }

  /** constructor with default DisplayRenderer */
  public DisplayImplJ3D(String name, int api)
         throws VisADException, RemoteException {
    this(name, null, api, null);
  }

  /** construct a DisplayImpl for Java3D with a non-default
      GraphicsConfiguration, in a JFC JPanel */
  public DisplayImplJ3D(String name, GraphicsConfiguration config)
         throws VisADException, RemoteException {
    this(name, null, JPANEL, config);
  }

  /** construct a DisplayImpl for Java3D with a non-default
      DisplayRenderer;
      in a JFC JPanel if api == DisplayImplJ3D.JPANEL and
      in an AppletFrame if api == DisplayImplJ3D.APPLETFRAME */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer, int api)
         throws VisADException, RemoteException {
    this(name, renderer, api, null);
  }

  /** construct a DisplayImpl for Java3D with a non-default
      DisplayRenderer and GraphicsConfiguration, in a JFC JPanel */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer,
                        GraphicsConfiguration config)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL, config);
  }

  /** constructor with default DisplayRenderer and a non-default
      GraphicsConfiguration */
  public DisplayImplJ3D(String name, int api, GraphicsConfiguration config)
         throws VisADException, RemoteException {
    this(name, null, api, config);
  }

  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer, int api,
                        GraphicsConfiguration config)
         throws VisADException, RemoteException {
    super(name, renderer);

    initialize(api, config);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy)
         throws VisADException, RemoteException {
    this(rmtDpy, null, rmtDpy.getDisplayAPI(), null);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, DisplayRendererJ3D renderer)
         throws VisADException, RemoteException {
    this(rmtDpy, renderer, rmtDpy.getDisplayAPI(), null);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, int api)
         throws VisADException, RemoteException {
    this(rmtDpy, null, api, null);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, GraphicsConfiguration config)
         throws VisADException, RemoteException {
    this(rmtDpy, null, rmtDpy.getDisplayAPI(), config);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, DisplayRendererJ3D renderer,
			int api)
         throws VisADException, RemoteException {
    this(rmtDpy, renderer, api, null);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, DisplayRendererJ3D renderer,
                        GraphicsConfiguration config)
         throws VisADException, RemoteException {
    this(rmtDpy, renderer, rmtDpy.getDisplayAPI(), config);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, int api,
                        GraphicsConfiguration config)
         throws VisADException, RemoteException {
    this(rmtDpy, null, api, config);
  }

  public DisplayImplJ3D(RemoteDisplay rmtDpy, DisplayRendererJ3D renderer,
                        int api, GraphicsConfiguration config)
         throws VisADException, RemoteException {
    super(rmtDpy,
          ((renderer == null && api == TRANSFORM_ONLY) ?
             new TransformOnlyDisplayRendererJ3D() : renderer));

    // use this for testing cluster = true in ordinary collab programs
    // super(rmtDpy,
    //       ((renderer == null && api == TRANSFORM_ONLY) ?
    //          new TransformOnlyDisplayRendererJ3D() : renderer), true);

    initialize(api, config);

    syncRemoteData(rmtDpy);
  }

  /** special constructor for cluster */
  public DisplayImplJ3D(RemoteDisplay rmtDpy,
                        TransformOnlyDisplayRendererJ3D renderer,
                        GraphicsConfiguration config)
         throws VisADException, RemoteException {
    super(rmtDpy, renderer, true); // cluster = true

    initialize(TRANSFORM_ONLY, config);

    syncRemoteData(rmtDpy);
  }

  private void initialize(int api, GraphicsConfiguration config)
	throws VisADException, RemoteException {
    // a ProjectionControl always exists
    projection = new ProjectionControlJ3D(this);
    addControl(projection);

    if (api == APPLETFRAME) {
      applet = new DisplayAppletJ3D(this, config);
      Component component = new MainFrame(applet, 256, 256);
      // Component component = new AppletFrame(applet, 256, 256);
      setComponent(component);
      // component.setTitle(name);
      apiValue = api;
    }
    else if (api == JPANEL) {
      Component component = new DisplayPanelJ3D(this, config);
      setComponent(component);
      apiValue = api;
    }
    else if (api == TRANSFORM_ONLY) {
      if (!(getDisplayRenderer() instanceof TransformOnlyDisplayRendererJ3D)) {
        throw new DisplayException("must be TransformOnlyDisplayRendererJ3D " +
                                   "for api = TRANSFORM_ONLY");
      }
      setComponent(null);
      apiValue = api;
    }
    else {
      throw new DisplayException("DisplayImplJ3D: bad graphics API " + api);
    }
    // initialize projection
    projection.setAspect(new double[] {1.0, 1.0, 1.0});

    // a GraphicsModeControl always exists
    mode = new GraphicsModeControlJ3D(this);
    addControl(mode);
  }

  /** return a DefaultDisplayRendererJ3D */
  protected DisplayRenderer getDefaultDisplayRenderer() {
    return new DefaultDisplayRendererJ3D();
  }

  public void setScreenAspect(double height, double width) {
    DisplayRendererJ3D dr = (DisplayRendererJ3D) getDisplayRenderer();
    Screen3D screen = dr.getCanvas().getScreen3D();
    screen.setPhysicalScreenHeight(height);
    screen.setPhysicalScreenWidth(width);
  }

  /**
   * Get the projection control associated with this display
   * @see ProjectionControlJ3D
   *
   * @return  this display's projection control
   */
  public ProjectionControl getProjectionControl() {
    return projection;
  }

  /**
   * Get the graphics mode control associated with this display
   * @see GraphicsModeControlJ3D
   *
   * @return  this display's graphics mode control
   */
  public GraphicsModeControl getGraphicsModeControl() {
    return mode;
  }

  /**
   * Return the applet associated with this display
   *
   * @return the applet or null if API != APPLETFRAME
   */
  public DisplayAppletJ3D getApplet() {
    return applet;
  }

  /**
   * Return the API used for this display
   *
   * @return  the mode being used (UNKNOWN, JPANEL, APPLETFRAME, TRANSFORM_ONLY)
   * @throws  VisADException
   */
  public int getAPI()
	throws VisADException
  {
    return apiValue;
  }

  public GeometryArray makeGeometry(VisADGeometryArray vga)
         throws VisADException {
    if (vga == null) return null;

    boolean mode2d = getDisplayRenderer().getMode2D();

    if (vga instanceof VisADIndexedTriangleStripArray) {
      /* this is the 'normal' makeGeometry */
      VisADIndexedTriangleStripArray vgb = (VisADIndexedTriangleStripArray) vga;
      if (vga.vertexCount == 0) return null;
      IndexedTriangleStripArray array =
        new IndexedTriangleStripArray(vga.vertexCount, makeFormat(vga),
                                      vgb.indexCount, vgb.stripVertexCounts);
      basicGeometry(vga, array, mode2d);
      if (vga.coordinates != null) {
        array.setCoordinateIndices(0, vgb.indices);
      }
      if (vga.colors != null) {
        array.setColorIndices(0, vgb.indices);
      }
      if (vga.normals != null) {
        array.setNormalIndices(0, vgb.indices);
      }
      if (vga.texCoords != null) {
        array.setTextureCoordinateIndices(0, vgb.indices);
      }
      return array;

/* this expands indices
      if (vga.vertexCount == 0) return null;
      //
      // expand vga.coordinates, vga.colors, vga.normals and vga.texCoords
      //
      int count = vga.indices.length;
      int len = 3 * count;

      int sum = 0;
      for (int i=0; i<vga.stripVertexCounts.length; i++) sum += vga.stripVertexCounts[i];
      System.out.println("vga.indexCount = " + vga.indexCount + " sum = " + sum +
                         " count = " + count + " vga.stripVertexCounts.length = " +
                         vga.stripVertexCounts.length);
      // int[] strip_counts = new int[1];
      // strip_counts[0] = count;
      // TriangleStripArray array =
      //   new TriangleStripArray(count, makeFormat(vga), strip_counts);

      TriangleStripArray array =
        new TriangleStripArray(count, makeFormat(vga), vga.stripVertexCounts);

      if (vga.coordinates != null) {
        System.out.println("expand vga.coordinates");
        float[] coords = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          coords[i] = vga.coordinates[j];
          coords[i + 1] = vga.coordinates[j + 1];
          coords[i + 2] = vga.coordinates[j + 2];
        }
        array.setCoordinates(0, coords);
      }
      if (vga.colors != null) {
        System.out.println("expand vga.colors");
        byte[] cols = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          cols[i] = vga.colors[j];
          cols[i + 1] = vga.colors[j + 1];
          cols[i + 2] = vga.colors[j + 2];
        }
        array.setColors(0, cols);
      }
      if (vga.normals != null) {
        System.out.println("expand vga.normals");
        float[] norms = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          norms[i] = vga.normals[j];
          norms[i + 1] = vga.normals[j + 1];
          norms[i + 2] = vga.normals[j + 2];
        }
        array.setNormals(0, norms);
      }
      if (vga.texCoords != null) {
        System.out.println("expand vga.texCoords");
        float[] tex = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          tex[i] = vga.texCoords[j];
          tex[i + 1] = vga.texCoords[j + 1];
          tex[i + 2] = vga.texCoords[j + 2];
        }
        array.setTextureCoordinates(0, tex);
      }
      return array;
*/

/* this draws normal vectors
      if (vga.vertexCount == 0) return null;
      LineArray array = new LineArray(2 * vga.vertexCount, LineArray.COORDINATES);
      float[] new_coords = new float[6 * vga.vertexCount];
      int i = 0;
      int j = 0;
      for (int k=0; k<vga.vertexCount; k++) {
        new_coords[j] = vga.coordinates[i];
        new_coords[j+1] = vga.coordinates[i+1];
        new_coords[j+2] = vga.coordinates[i+2];
        j += 3;
        new_coords[j] = vga.coordinates[i] + 0.05f * vga.normals[i];
        new_coords[j+1] = vga.coordinates[i+1] + 0.05f * vga.normals[i+1];
        new_coords[j+2] = vga.coordinates[i+2] + 0.05f * vga.normals[i+2];
        i += 3;
        j += 3;
      }
      array.setCoordinates(0, new_coords);
      return array;
*/

/* this draws the 'dots'
      if (vga.vertexCount == 0) return null;
      PointArray array =
        new PointArray(vga.vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array, false);
      return array;
*/
    }
    if (vga instanceof VisADTriangleStripArray) {
      VisADTriangleStripArray vgb = (VisADTriangleStripArray) vga;
      if (vga.vertexCount == 0) return null;
      TriangleStripArray array =
        new TriangleStripArray(vga.vertexCount, makeFormat(vga),
                               vgb.stripVertexCounts);
      basicGeometry(vga, array, mode2d);
      return array;
    }
    else if (vga instanceof VisADLineArray) {
      if (vga.vertexCount == 0) return null;
      LineArray array = new LineArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array, false);
      return array;
    }
    else if (vga instanceof VisADLineStripArray) {
      if (vga.vertexCount == 0) return null;
      VisADLineStripArray vgb = (VisADLineStripArray) vga;
      LineStripArray array =
        new LineStripArray(vga.vertexCount, makeFormat(vga),
                           vgb.stripVertexCounts);
      basicGeometry(vga, array, false);
      return array;
    }
    else if (vga instanceof VisADPointArray) {
      if (vga.vertexCount == 0) return null;
      PointArray array = new PointArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array, false);
      return array;
    }
    else if (vga instanceof VisADTriangleArray) {
      if (vga.vertexCount == 0) return null;
      TriangleArray array = new TriangleArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array, mode2d);
      return array;
    }
    else if (vga instanceof VisADQuadArray) {
      if (vga.vertexCount == 0) return null;
      QuadArray array = new QuadArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array, mode2d);
      return array;
    }
    else {
      throw new DisplayException("DisplayImplJ3D.makeGeometry");
    }
  }

  private void basicGeometry(VisADGeometryArray vga,
                             GeometryArray array, boolean mode2d) {
    if (mode2d) {
      if (vga.coordinates != null) {
        int len = vga.coordinates.length;
        float[] coords = new float[len];
        System.arraycopy(vga.coordinates, 0, coords, 0, len);
        for (int i=2; i<len; i+=3) coords[i] = BACK2D;
        array.setCoordinates(0, coords);
      }
    }
    else {
      if (vga.coordinates != null) array.setCoordinates(0, vga.coordinates);
    }
    if (vga.colors != null) array.setColors(0, vga.colors);
    if (vga.normals != null) array.setNormals(0, vga.normals);
    if (vga.texCoords != null) array.setTextureCoordinates(0, vga.texCoords);
  }

  private static int makeFormat(VisADGeometryArray vga) {
    int format = 0;
    if (vga.coordinates != null) format |= GeometryArray.COORDINATES;
    if (vga.colors != null) {
      if (vga.colors.length == 3 * vga.vertexCount) {
        format |= GeometryArray.COLOR_3;
      }
      else {
        format |= GeometryArray.COLOR_4;
      }
    }
    if (vga.normals != null) format |= GeometryArray.NORMALS;
    if (vga.texCoords != null) {
      if (vga.texCoords.length == 2 * vga.vertexCount) {
        format |= GeometryArray.TEXTURE_COORDINATE_2;
      }
      else {
        format |= GeometryArray.TEXTURE_COORDINATE_3;
      }
    }
    return format;
  }

}

