
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

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.*;

import java.awt.*;

import javax.media.j3d.*;
import com.sun.j3d.utils.applet.AppletFrame;

// GUI handling
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

// import visad.data.netcdf.plain.Plain;

/**
   DisplayImplJ3D is the VisAD class for displays that use
   Java 3D.  It is runnable.<P>

   DisplayImplJ3D is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImplJ3D extends DisplayImpl {

  /** legal values for api */
  public static final int JPANEL_JAVA3D = 1;
  public static final int APPLETFRAME_JAVA3D = 2;
  /** this is used for APPLETFRAME_JAVA3D */
  private DisplayApplet applet;

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
    this(name, new DefaultDisplayRendererJ3D(), JPANEL_JAVA3D);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL_JAVA3D);
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

    if (api == APPLETFRAME_JAVA3D) {
      applet = new DisplayApplet(this);
      Component component = new AppletFrame(applet, 256, 256);
      setComponent(component);
      // component.setTitle(name);
    }
    else if (api == JPANEL_JAVA3D) {
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

  /** run 'java visad.DisplayImplJ3D' to test the DisplayImplJ3D class */
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

    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    FunctionType image_vis = new FunctionType(earth_location, vis_radiance);
    FunctionType image_ir = new FunctionType(earth_location, ir_radiance);

    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    // FunctionType grid_tuple = new FunctionType(earth_location, radiance);
    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);

    System.out.println(time_images);
    System.out.println(grid_tuple);
    System.out.println(image_tuple);
    System.out.println(ir_histogram);

    FlatField imagev1 = FlatField.makeField(image_vis, 4, false);
    FlatField imager1 = FlatField.makeField(image_ir, 4, false);

    // use 'java visad.DisplayImplJ3D' for size = 256 (implicit -mx16m)
    // use 'java -mx40m visad.DisplayImplJ3D' for size = 512
    int size = 64;
    // int size3d = 2;
    // float level = 0.5f;
    int size3d = 6;
    float level = 2.0f;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);
    FlatField grid3d = FlatField.makeField(grid_tuple, size3d, true);

    int ntimes = 4;
    Set time_set =
      new Linear1DSet(time_type, 0.0, (double) (ntimes - 1.0), ntimes);
    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FlatField temp = imaget1;
    Real[] reals = {new Real(vis_radiance, 1.0), new Real(ir_radiance, 2.0)};
    RealTuple val = new RealTuple(reals);
    for (int i=0; i<ntimes; i++) {
      image_sequence.setSample(i, imaget1);
      temp = (FlatField) temp.add(val);
    }
    Real[] reals2 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                     new Real(vis_radiance, 1.0)};
    // RealTuple direct = new RealTuple(reals2);
    Real direct = new Real(ir_radiance, 2.0);


    DisplayImpl display1 = new DisplayImplJ3D("display1", APPLETFRAME_JAVA3D);
/*
    display1.addMap(new ScalarMap(vis_radiance, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display1.addMap(new ScalarMap(count, Display.ZAxis));
*/

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
    ScalarMap map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    display1.addMap(map1contour);
    ContourControl control1contour = (ContourControl) map1contour.getControl();
    control1contour.setSurfaceValue(level);

/*
    display1.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    display1.addMap(new ScalarMap(vis_radiance, Display.Radius));
*/
/*
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.Green));
    display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
    // display1.addMap(new ScalarMap(vis_radiance, Display.IsoContour));
    display1.addMap(new ScalarMap(ir_radiance, Display.Alpha));
    // display1.addMap(new ConstantMap(0.5, Display.Alpha));
*/


/* code to load a GIF image into imaget1 */
/*
    double[][] data = imaget1.getValues();
    DisplayApplet applet = new DisplayApplet();
    data[1] = applet.getValues("file:/home/billh/java/visad/billh.gif", size);
    imaget1.setSamples(data);
*/
/*
    Plain plain = new Plain();
    FlatField netcdf_data = (FlatField) plain.open("pmsl.nc");
    // System.out.println("netcdf_data = " + netcdf_data);
    // prints: FunctionType (Real): (lon, lat) -> P_msl
    //
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
*/


    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);
/*
    mode.setProjectionPolicy(View.PARALLEL_PROJECTION);
java.lang.RuntimeException: PARALLEL_PROJECTION is not yet implemented
        at javax.media.j3d.View.setProjectionPolicy(View.java:423)
*/

    System.out.println(display1);

/*
    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);
*/

/*
    DataReferenceImpl ref_val = new DataReferenceImpl("ref_val");
    ref_val.setData(val);
    DataReference[] refs = {ref_val};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs, null);
*/

/*
    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refs2 = {ref_direct};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs2, null);

    DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = {ref_histogram1};
    display1.addReferences(new DirectManipulationRendererJ3D(), refs3, null);
*/

/*
    DataReferenceImpl ref_netcdf = new DataReferenceImpl("ref_netcdf");
    ref_netcdf.setData(netcdf_data);
    display1.addReference(ref_netcdf, null);
*/

/*
    DataReferenceImpl ref_image_sequence =
      new DataReferenceImpl("ref_image_sequence");
    ref_image_sequence.setData(image_sequence);
    display1.addReference(ref_image_sequence, null);
*/

    DataReferenceImpl ref_grid3d = new DataReferenceImpl("ref_grid3d");
    ref_grid3d.setData(grid3d);
    display1.addReference(ref_grid3d, null);

/*
    DisplayImpl display2 = new DisplayImplJ3D("display2", APPLETFRAME_JAVA3D);
    display2.addMap(new ScalarMap(vis_radiance, Display.XAxis));
    display2.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display2.addMap(new ScalarMap(count, Display.ZAxis));

    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    display2.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
    display2.addReferences(new DirectManipulationRendererJ3D(), refs3, null);
*/

/*
    DisplayImpl display5 = new DisplayImplJ3D("display5", APPLETFRAME_JAVA3D);
    display5.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display5.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    display5.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display5.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display5);
    display5.addReference(ref_imaget1, null);

    DisplayImpl display3 = new DisplayImplJ3D("display3", APPLETFRAME_JAVA3D);
    display3.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display3.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));
    display3.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display3.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display3);
    display3.addReference(ref_imaget1, null);

    DisplayImpl display4 = new DisplayImplJ3D("display4", APPLETFRAME_JAVA3D);
    display4.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display4.addMap(new ScalarMap(RealType.Longitude, Display.Radius));
    display4.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display4.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display4);
    display4.addReference(ref_imaget1, null);

    delay(1000);
    System.out.println("\ndelay\n");

    ref_imaget1.incTick();

    delay(1000);
    System.out.println("\ndelay\n");

    ref_imaget1.incTick();

    delay(1000);
    System.out.println("\ndelay\n");

    ref_imaget1.incTick();
 
    System.out.println("\nno delay\n");
 
    ref_imaget1.incTick();
 
    System.out.println("\nno delay\n");
 
    ref_imaget1.incTick();
 
    delay(2000);
    System.out.println("\ndelay\n");


    display1.removeReference(ref_imaget1);
    display5.removeReference(ref_imaget1);
    display3.removeReference(ref_imaget1);
    display4.removeReference(ref_imaget1);

    display1.stop();
    display5.stop();
    display3.stop();
    display4.stop();
*/

    while (true) {
      delay(5000);
      System.out.println("\ndelay\n");
    }

    // Applications that export remote objects may not exit (according
    // to the JDK 1.1 release notes).  Here's the work around:
    //
    // System.exit(0);

  }

/* Here's the output:

110% java visad.Display
FunctionType (Real): (Latitude(degrees), Longitude(degrees)) -> (vis_radiance, ir_radiance)
FunctionType (Real): (ir_radiance) -> count
FlatField  missing

FlatField  missing

Display
    ScalarMap: Latitude(degrees) -> DisplayXAxis
    ScalarMap: Longitude(degrees) -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayZAxis
    ScalarMap: vis_radiance -> DisplayRGB
    ConstantMap: 0.5 -> DisplayAlpha

Display
    ScalarMap: Latitude(degrees) -> DisplayLatitude
    ScalarMap: Longitude(degrees) -> DisplayLongitude
    ScalarMap: ir_radiance -> DisplayRadius
    ScalarMap: vis_radiance -> DisplayRGB

Display
    ScalarMap: Latitude(degrees) -> DisplayXAxis
    ScalarMap: Longitude(degrees) -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayRadius
    ScalarMap: vis_radiance -> DisplayRGB

Display
    ScalarMap: Latitude(degrees) -> DisplayXAxis
    ScalarMap: Longitude(degrees) -> DisplayRadius
    ScalarMap: ir_radiance -> DisplayYAxis
    ScalarMap: vis_radiance -> DisplayRGB

LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s

delay

LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s

delay

LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s

delay

 
no delay
 
 
no delay
 
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s
 
delay

111%

*/

}

