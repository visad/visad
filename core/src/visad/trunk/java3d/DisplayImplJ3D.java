
//
// DisplayImplJ3D.java
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

package visad.java3d;

import visad.*;
import visad.util.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.media.j3d.*;
import com.sun.j3d.utils.applet.AppletFrame;

// GUI handling
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

// import visad.data.netcdf.Plain;

/**
   DisplayImplJ3D is the VisAD class for displays that use
   Java 3D.  It is runnable.<P>

   DisplayImplJ3D is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImplJ3D extends DisplayImpl {

  /** legal values for api */
  public static final int JPANEL = 1;
  public static final int APPLETFRAME = 2;
  /** this is used for APPLETFRAME */
  private DisplayApplet applet = null;

  private ProjectionControlJ3D projection = null;
  private GraphicsModeControlJ3D mode = null;
/*
  private AnimationControlJ3D animation = null;
  private Flow1Control flow1 = null;
  private Flow2Control flow2 = null;
*/

  /** constructor with DefaultDisplayRendererJ3D */
  public DisplayImplJ3D(String name)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRendererJ3D(), JPANEL);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL);
  }

  /** constructor with DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, int api)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRendererJ3D(), api);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer, int api)
         throws VisADException, RemoteException {
    super(name, renderer);

    // a GraphicsModeControl always exists
    mode = new GraphicsModeControlJ3D(this);
    addControl(mode);
    // a ProjectionControl always exists
    projection = new ProjectionControlJ3D(this);
    addControl(projection);

    if (api == APPLETFRAME) {
      applet = new DisplayApplet(this);
      Component component = new AppletFrame(applet, 256, 256);
      setComponent(component);
      // component.setTitle(name);
    }
    else if (api == JPANEL) {
      Component component = new DisplayPanel(this);
      setComponent(component);
    }
    else {
      throw new DisplayException("DisplayImplJ3D: bad graphicsApi");
    }
  }

  public ProjectionControl getProjectionControl() {
    return projection;
  }

  public GraphicsModeControl getGraphicsModeControl() {
    return mode;
  }

  public DisplayApplet getApplet() {
    return applet;
  }

  public GeometryArray makeGeometry(VisADGeometryArray vga)
         throws VisADException {
    if (vga == null) return null;
    if (vga instanceof VisADIndexedTriangleStripArray) {
      /* this is the 'normal' makeGeometry */
      VisADIndexedTriangleStripArray vgb = (VisADIndexedTriangleStripArray) vga;
      if (vga.vertexCount == 0) return null;
      IndexedTriangleStripArray array =
        new IndexedTriangleStripArray(vga.vertexCount, makeFormat(vga),
                                      vgb.indexCount, vgb.stripVertexCounts);
      basicGeometry(vga, array);
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
        float[] cols = new float[len];
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
      basicGeometry(vga, array);
      return array;
*/
    }
    else if (vga instanceof VisADLineArray) {
      if (vga.vertexCount == 0) return null;
      LineArray array = new LineArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
    }
    else if (vga instanceof VisADLineStripArray) {
      if (vga.vertexCount == 0) return null;
      VisADLineStripArray vgb = (VisADLineStripArray) vga;
      LineStripArray array =
        new LineStripArray(vga.vertexCount, makeFormat(vga),
                           vgb.stripVertexCounts);
      basicGeometry(vga, array);
      return array;
    }
    else if (vga instanceof VisADPointArray) {
      if (vga.vertexCount == 0) return null;
      PointArray array = new PointArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
    }
    else if (vga instanceof VisADTriangleArray) {
      if (vga.vertexCount == 0) return null;
      TriangleArray array = new TriangleArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
    }
    else {
      throw new DisplayException("DisplayImplJ3D.makeGeometry");
    }
  }

  private void basicGeometry(VisADGeometryArray vga,
                             GeometryArray array) {
    if (vga.coordinates != null) array.setCoordinates(0, vga.coordinates);
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

  /** used to test the DisplayImplJ3D class
      run 'java visad.java3d.DisplayImplJ3D to test list options */
  public static void main(String args[])
         throws IOException, VisADException, RemoteException {


    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);

    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);

    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    RealType[] types4 = {ir_radiance, vis_radiance};
    RealTupleType ecnaidar = new RealTupleType(types4);

    FunctionType image_bumble = new FunctionType(earth_location, ecnaidar);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    FunctionType image_vis = new FunctionType(earth_location, vis_radiance);
    FunctionType image_ir = new FunctionType(earth_location, ir_radiance);

    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);
    FunctionType time_bee = new FunctionType(time_type, image_bumble);

    int test_case = -1;
    if (args.length > 0) {
      try {
        test_case = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException e) {
        test_case = -1;
      }
    }

    DisplayImpl display1;
    int size;
    FlatField imaget1;
    DataReferenceImpl ref_imaget1;

    switch(test_case) {
      default:
 
        System.out.println("to test the DisplayImplJ3D class run\n");
        System.out.println("  java visad.java3d.DisplayImplJ3D N, where N =\n");
        System.out.println("  0: direct manipulation");
        System.out.println("  1: iso-surfaces from regular grids");
        System.out.println("  2: iso-surfaces from irregular grids");
        System.out.println("  3: animation");
        System.out.println("  4: spherical coordinates");
        System.out.println("  5: 2-D contours from regular grids");
        System.out.println("  6: 2-D contours from irregular grids");
        System.out.println("  7: transparency");
        System.out.println("  8: offset");
        System.out.println("  9: GIF reader and ColorWidget");
        System.out.println("  10: netCDF adapter");
        System.out.println("  11: CoordinateSystem and Unit");
        System.out.println("  12: 2-D surface and ColorWidget");
 
        return;

      case 0:

        System.out.println(test_case + ": test direct manipulation");
        size = 64;
        FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
        Real direct = new Real(ir_radiance, 2.0);
        Real[] reals3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                         new Real(vis_radiance, 1.0)};
        RealTuple direct_tuple = new RealTuple(reals3);
    
        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(vis_radiance, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.YAxis));
        display1.addMap(new ScalarMap(count, Display.ZAxis));
    
        GraphicsModeControl mode = display1.getGraphicsModeControl();
        mode.setPointSize(5.0f);
        mode.setPointMode(false);

        // mode.setProjectionPolicy(View.PARALLEL_PROJECTION);
        // gives:
        // java.lang.RuntimeException: PARALLEL_PROJECTION is not yet implemented
        // at javax.media.j3d.View.setProjectionPolicy(View.java:423)

        DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
        ref_direct.setData(direct);
        DataReference[] refs1 = {ref_direct};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs1, null);
     
        DataReferenceImpl ref_direct_tuple =
          new DataReferenceImpl("ref_direct_tuple");
        ref_direct_tuple.setData(direct_tuple);
        DataReference[] refs2 = {ref_direct_tuple};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
     
        DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
        ref_histogram1.setData(histogram1);
        DataReference[] refs3 = {ref_histogram1};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

        DisplayImpl display2 = new DisplayImplJ3D("display2", APPLETFRAME);
        display2.addMap(new ScalarMap(vis_radiance, Display.XAxis));
        display2.addMap(new ScalarMap(ir_radiance, Display.YAxis));
        display2.addMap(new ScalarMap(count, Display.ZAxis));
     
        GraphicsModeControl mode2 = display2.getGraphicsModeControl();
        mode2.setPointSize(5.0f);
        mode2.setPointMode(false);
     
        display2.addReferences(new DirectManipulationRendererJ3D(), refs1, null);
        display2.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
        display2.addReferences(new DirectManipulationRendererJ3D(), refs3, null);
        break;

      case 1:

        System.out.println(test_case + ": test iso-surfaces from regular grids");
        int size3d = 6;
        float level = 2.5f;
        FlatField grid3d = FlatField.makeField(grid_tuple, size3d, false);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);

        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
        ScalarMap map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        ContourControl control1contour = (ContourControl) map1contour.getControl();
        control1contour.setSurfaceValue(level);

        DataReferenceImpl ref_grid3d = new DataReferenceImpl("ref_grid3d");
        ref_grid3d.setData(grid3d);
        display1.addReference(ref_grid3d, null);

        break;

      case 2:
 
        System.out.println(test_case + ": test iso-surfaces from irregular grids");
        size3d = 6;
        level = 2.5f;
        grid3d = FlatField.makeField(grid_tuple, size3d, true);
 
        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
 
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
        map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        control1contour = (ContourControl) map1contour.getControl();
        control1contour.setSurfaceValue(level);
 
        ref_grid3d = new DataReferenceImpl("ref_grid3d");
        ref_grid3d.setData(grid3d);
        display1.addReference(ref_grid3d, null);
 
        break;

      case 3:

        System.out.println(test_case + ": test animation");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
        FlatField wasp = FlatField.makeField(image_bumble, size, false);

        int ntimes1 = 4;
        int ntimes2 = 6;
/*
        // different time extents test
        Set time_set =
          new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);
        Set time_hornet =
          new Linear1DSet(time_type, 0.0, (double) (ntimes2 - 1.0), ntimes2);
*/
        // different time resolution test
        Set time_set =
          new Linear1DSet(time_type, 0.0, 1.0, ntimes1);
        Set time_hornet =
          new Linear1DSet(time_type, 0.0, 1.0, ntimes2);

        FieldImpl image_sequence = new FieldImpl(time_images, time_set);
        FieldImpl image_stinger = new FieldImpl(time_bee, time_hornet);
        FlatField temp = imaget1;
        FlatField tempw = wasp;
        Real[] reals = {new Real(vis_radiance, (float) size / 4.0f),
                        new Real(ir_radiance, (float) size / 8.0f)};
        RealTuple val = new RealTuple(reals);
        for (int i=0; i<ntimes1; i++) {
          image_sequence.setSample(i, temp);
          temp = (FlatField) temp.add(val);
        }
        for (int i=0; i<ntimes2; i++) {
          image_stinger.setSample(i, tempw);
          tempw = (FlatField) tempw.add(val);
        }
        FieldImpl[] images = {image_sequence, image_stinger};
        Tuple big_tuple = new Tuple(images);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
     
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        ScalarMap map1animation = new ScalarMap(RealType.Time, Display.Animation);
        display1.addMap(map1animation);
        AnimationControl animation1control =
          (AnimationControl) map1animation.getControl();
        animation1control.setOn(true);
        animation1control.setStep(3000);

        DataReferenceImpl ref_big_tuple =
          new DataReferenceImpl("ref_big_tuple");
        ref_big_tuple.setData(big_tuple);
        display1.addReference(ref_big_tuple, null);

        break;

      case 4:

        System.out.println(test_case + ": test spherical coordinates");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
        display1.addMap(new ScalarMap(vis_radiance, Display.RGB));
        // display1.addMap(new ScalarMap(vis_radiance, Display.Radius));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 5:

        System.out.println(test_case + ": test 2-D contours from regular grids");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.IsoContour));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 6:
 
        System.out.println(test_case + ": test 2-D contours from irregular grids");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, true);
 
        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.IsoContour));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 7:

        System.out.println(test_case + ": test transparency");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Alpha));
        // display1.addMap(new ConstantMap(0.5, Display.Alpha));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 8:
 
        System.out.println(test_case + ": test Offset");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxisOffset));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxisOffset));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;
 
      case 9:
 
        System.out.println(test_case + ": test GIF reader");
        display1 = new DisplayImplJ3D("display1", APPLETFRAME);

        imaget1 = display1.getImage("file:/home/billh/java/visad/bill.gif");

        // compute ScalarMaps from type components
        FunctionType ftype = (FunctionType) imaget1.getType();
        RealTupleType dtype = ftype.getDomain();
        RealType rtype = (RealType) ftype.getRange();
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                      Display.XAxis));
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                      Display.YAxis));
        display1.addMap(new ScalarMap(rtype, Display.RGB));

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;
 
      case 10:
 
        System.out.println(test_case + ": test netCDF adapter");
/*
        Plain plain = new Plain();
        FlatField netcdf_data = (FlatField) plain.open("pmsl.nc");
        // System.out.println("netcdf_data = " + netcdf_data);
        // prints: FunctionType (Real): (lon, lat) -> P_msl

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        // compute ScalarMaps from type components
        FunctionType ftype = (FunctionType) netcdf_data.getType();
        RealTupleType dtype = ftype.getDomain();
        MathType rtype = ftype.getRange();
        int n = dtype.getDimension();
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                      Display.XAxis));
        if (n > 1) {
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                        Display.YAxis));
        }
        if (n > 2) {
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                        Display.ZAxis));
        }
        if (rtype instanceof RealType) {
          display1.addMap(new ScalarMap((RealType) rtype, Display.Green));
          if (n <= 2) {
            display1.addMap(new ScalarMap((RealType) rtype, Display.ZAxis));
          }
        }
        else if (rtype instanceof RealTupleType) {
          int m = ((RealTupleType) rtype).getDimension();
          RealType rr = (RealType) ((RealTupleType) rtype).getComponent(0);
          display1.addMap(new ScalarMap(rr, Display.Green));
          if (n >= 2) {
            if (m > 1) {
              rr = (RealType) ((RealTupleType) rtype).getComponent(1);
            }
            display1.addMap(new ScalarMap(rr, Display.ZAxis));
          }
        }
        display1.addMap(new ConstantMap(0.5, Display.Red));
        display1.addMap(new ConstantMap(0.0, Display.Blue));

        DataReferenceImpl ref_netcdf = new DataReferenceImpl("ref_netcdf");
        ref_netcdf.setData(netcdf_data);
        display1.addReference(ref_netcdf, null);
*/

        break;

      case 11:
 
        System.out.println(test_case + ": test CoordinateSystem and Unit");
        RealType x = new RealType("x", null, null);
        RealType y = new RealType("y", null, null);
        Unit super_degree = CommonUnit.degree.scale(2.5);
        RealType lon = new RealType("lon", super_degree, null);
        RealType radius = new RealType("radius", null, null);
        RealTupleType cartesian = new RealTupleType(x, y);
        PolarCoordinateSystem polar_coord_sys =
          new PolarCoordinateSystem(cartesian);
        RealTupleType polar =
          new RealTupleType(lon, radius, polar_coord_sys, null);

        FunctionType image_polar = new FunctionType(polar, radiance);
        Unit[] units = {super_degree, null};
        Linear2DSet domain_set =
          new Linear2DSet(polar, 0.0, 60.0, 61, 0.0, 60.0, 61,
                          polar_coord_sys, units, null);
        imaget1 = new FlatField(image_polar, domain_set);
        FlatField.fillField(imaget1, 1.0, 30.0);

        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(x, Display.XAxis));
        display1.addMap(new ScalarMap(y, Display.YAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        display1.addMap(new ConstantMap(0.0, Display.Blue));

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 12:
 
        System.out.println(test_case + ": test 2-D surface and ColorWidget");

        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
 
        ScalarMap color1map = new ScalarMap(ir_radiance, Display.RGB);
        display1.addMap(color1map);
        final ColorControl color1control = (ColorControl) color1map.getControl();

        final int table_size = 64;
        final float[][] table = new float[3][table_size];
        final float scale = 1.0f / (table_size - 1.0f);

        Frame frame = new Frame("VisAD Color Widget");
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        final ColorWidget w = new ColorWidget();
        ColorMap map = w.getColorMap();
        for (int i=0; i<table_size; i++) {
          float[] t = map.getRGBTuple(scale * i);
          table[0][i] = t[0];
          table[1][i] = t[1];
          table[2][i] = t[2];
        }
        color1control.setTable(table);

        frame.add(w);
        frame.setSize(w.getPreferredSize());
        frame.setVisible(true);

        w.addColorChangeListener(new ColorChangeListener() {
          public void colorChanged(ColorChangeEvent e) {
            ColorMap map_e = w.getColorMap();
            float[][] table_e = new float[3][table_size];
            for (int i=0; i<table_size; i++) {
              float[] t = map_e.getRGBTuple(scale * i);
              table_e[0][i] = t[0];
              table_e[1][i] = t[1];
              table_e[2][i] = t[2];
            }
            try {
              color1control.setTable(table_e);
            }
            catch (VisADException f) {
            }
            catch (RemoteException f) {
            }
          }
        });
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

    }

    while (true) {
      delay(5000);
      System.out.println("\ndelay\n");
    }

    // Applications that export remote objects may not exit (according
    // to the JDK 1.1 release notes).  Here's the work around:
    //
    // System.exit(0);

  }

}

