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

import java.util.Vector;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test46
  extends UISkeleton
{
  public Test46() { }

  public Test46(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ2D("display");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    float[][] values = {{0.0f, 1.0f, 2.0f, 3.0f, 0.0f, 1.0f}};
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

    ScalarMap shape_map2 = new ScalarMap(count, Display.Shape);
    dpys[0].addMap(shape_map2);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    dpys[0].addReference(ref_histogram1, null);
  }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    Vector v = dpys[0].getMapVector();
    ScalarMap shape_map = (ScalarMap )v.elementAt(3);
    ScalarMap shape_map2 = (ScalarMap )v.elementAt(4);

    RealType count = (RealType )shape_map.getScalar();

    VisADLineArray cross = new VisADLineArray();
    cross.coordinates = new float[]
      {0.2f,  0.2f, 0.0f,    -0.2f, -0.2f, 0.0f,
       0.2f, -0.2f, 0.0f,    -0.2f,  0.2f, 0.0f};
    cross.vertexCount = cross.coordinates.length / 3;

    VisADLineArray box = new VisADLineArray();
    box.coordinates = new float[]
      {0.1f,  0.1f, 0.0f,     0.1f, -0.1f, 0.0f,
       0.1f, -0.1f, 0.0f,    -0.1f, -0.1f, 0.0f,
      -0.1f, -0.1f, 0.0f,    -0.1f,  0.1f, 0.0f,
      -0.1f,  0.1f, 0.0f,     0.1f,  0.1f, 0.0f};
    box.vertexCount = box.coordinates.length / 3;

    VisADTriangleArray tri = new VisADTriangleArray();
    tri.coordinates = new float[]
      {-0.1f, -0.05f, 0.0f,    0.1f, -0.05f, 0.0f,
        0.0f,  0.1f,  0.0f};
    tri.vertexCount = tri.coordinates.length / 3;
    // explicitly set colors in tri to override any color ScalarMaps
    tri.colors = new byte[]
      {-1, -1, 0,  -1, -1, 0,  -1, -1, 0};

    VisADQuadArray square = new VisADQuadArray();
    square.coordinates = new float[]
      {0.1f,  0.1f, 0.0f,     0.1f, -0.1f, 0.0f,
      -0.1f, -0.1f, 0.0f,    -0.1f,  0.1f, 0.0f};
    square.vertexCount = square.coordinates.length / 3;

    float[][] counts = {{0.0f, 1.0f, 2.0f, 3.0f}};
    Gridded1DSet count_set =
      new Gridded1DSet(count, counts, counts[0].length);

    VisADGeometryArray[] shapes = {cross, box, tri, square};
    ShapeControl shape_control = (ShapeControl) shape_map.getControl();
    shape_control.setShapeSet(count_set);
    shape_control.setShapes(shapes);

    VisADGeometryArray[] shapes2 = {square, tri, box, cross};
    ShapeControl shape_control2 = (ShapeControl) shape_map2.getControl();
    shape_control2.setShapeSet(count_set);
    shape_control2.setShapes(shapes2);

    return null;
  }

  String getFrameTitle() { return "shape in Java2D"; }

  public String toString() { return ": shape in Java2D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test46(args);
  }
}
