/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test47
  extends UISkeleton
{
  public Test47() { }

  public Test47(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    TextType text = new TextType("text");

    float[][] values;
    values = new float[][] {{0.0f, 1.0f, 2.0f, 3.0f, 0.0f, 1.0f}};
    int size = values[0].length;
    Integer1DSet ir_set = new Integer1DSet(size);
    FlatField histogram1 = new FlatField(ir_histogram, ir_set);
    histogram1.setSamples(values);

    dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.ShapeScale));
    dpys[0].addMap(new ScalarMap(count, Display.Green));
    dpys[0].addMap(new ConstantMap(1.0, Display.Blue));
    dpys[0].addMap(new ConstantMap(1.0, Display.Red));
    ScalarMap shape_map = new ScalarMap(count, Display.Shape);
    dpys[0].addMap(shape_map);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    dpys[0].addReference(ref_histogram1, null);
  }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    ScalarMap shape_map = (ScalarMap )dpys[0].getMapVector().lastElement();

    RealType count = (RealType )shape_map.getScalar();

    float[][] counts = new float[][] {{0.0f, 1.0f, 2.0f, 3.0f}};

    Gridded1DSet count_set = new Gridded1DSet(count, counts, counts[0].length);

    VisADLineArray cross = new VisADLineArray();
    cross.coordinates = new float[]
      {0.1f,  0.1f, 0.0f,    -0.1f, -0.1f, 0.0f,
       0.1f, -0.1f, 0.0f,    -0.1f,  0.1f, 0.0f};
    cross.vertexCount = cross.coordinates.length / 3;

    VisADQuadArray cube = new VisADQuadArray();
    cube.coordinates = new float[]
      {0.1f,  0.1f, -0.1f,     0.1f, -0.1f, -0.1f,
       0.1f, -0.1f, -0.1f,    -0.1f, -0.1f, -0.1f,
      -0.1f, -0.1f, -0.1f,    -0.1f,  0.1f, -0.1f,
      -0.1f,  0.1f, -0.1f,     0.1f,  0.1f, -0.1f,

       0.1f,  0.1f,  0.1f,     0.1f, -0.1f,  0.1f,
       0.1f, -0.1f,  0.1f,    -0.1f, -0.1f,  0.1f,
      -0.1f, -0.1f,  0.1f,    -0.1f,  0.1f,  0.1f,
      -0.1f,  0.1f,  0.1f,     0.1f,  0.1f,  0.1f,

       0.1f,  0.1f,  0.1f,     0.1f,  0.1f, -0.1f,
       0.1f,  0.1f, -0.1f,     0.1f, -0.1f, -0.1f,
       0.1f, -0.1f, -0.1f,     0.1f, -0.1f,  0.1f,
       0.1f, -0.1f,  0.1f,     0.1f,  0.1f,  0.1f,

      -0.1f,  0.1f,  0.1f,    -0.1f,  0.1f, -0.1f,
      -0.1f,  0.1f, -0.1f,    -0.1f, -0.1f, -0.1f,
      -0.1f, -0.1f, -0.1f,    -0.1f, -0.1f,  0.1f,
      -0.1f, -0.1f,  0.1f,    -0.1f,  0.1f,  0.1f,

       0.1f,  0.1f,  0.1f,     0.1f,  0.1f, -0.1f,
       0.1f,  0.1f, -0.1f,    -0.1f,  0.1f, -0.1f,
      -0.1f,  0.1f, -0.1f,    -0.1f,  0.1f,  0.1f,
      -0.1f,  0.1f,  0.1f,     0.1f,  0.1f,  0.1f,

       0.1f, -0.1f,  0.1f,     0.1f, -0.1f, -0.1f,
       0.1f, -0.1f, -0.1f,    -0.1f, -0.1f, -0.1f,
      -0.1f, -0.1f, -0.1f,    -0.1f, -0.1f,  0.1f,
      -0.1f, -0.1f,  0.1f,     0.1f, -0.1f,  0.1f};

    cube.vertexCount = cube.coordinates.length / 3;
    cube.normals = new float[144];
    for (int i=0; i<24; i+=3) {
      cube.normals[i]     =  0.0f;
      cube.normals[i+1]   =  0.0f;
      cube.normals[i+2]   = -1.0f;

      cube.normals[i+24]  =  0.0f;
      cube.normals[i+25]  =  0.0f;
      cube.normals[i+26]  =  1.0f;

      cube.normals[i+48]  =  1.0f;
      cube.normals[i+49]  =  0.0f;
      cube.normals[i+50]  =  0.0f;

      cube.normals[i+72]  = -1.0f;
      cube.normals[i+73]  =  0.0f;
      cube.normals[i+74]  =  0.0f;

      cube.normals[i+96]  =  0.0f;
      cube.normals[i+97]  =  1.0f;
      cube.normals[i+98]  =  0.0f;

      cube.normals[i+120] =  0.0f;
      cube.normals[i+121] = -1.0f;
      cube.normals[i+122] =  0.0f;
    }

    double[] start = {0.0, 0.0, 0.0}; // text at origin
    double[] base = {0.1, 0.0, 0.0};  // text out along XAxis
    double[] up = {0.0, 0.1, 0.0};    // character up along YAxis
    boolean center = true;            // center text
    VisADLineArray one_two =
      PlotText.render_label("1.2", start, base, up, center);

    VisADGeometryArray[] shapes;
    shapes = new VisADGeometryArray[] {one_two, cube, cross, cube};

    ShapeControl shape_control = (ShapeControl) shape_map.getControl();
    shape_control.setShapeSet(count_set);
    shape_control.setShapes(shapes);

    return null;
  }

  String getFrameTitle() { return "shape in Java3D"; }

  public String toString() { return ": shape in Java3D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test47(args);
  }
}
