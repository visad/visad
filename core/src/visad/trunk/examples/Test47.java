import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test47
	extends UISkeleton
{
  public Test47() { }

  public Test47(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    TextType text = new TextType("text");

    float[][] values;
    values = new float[][] {{0.0f, 1.0f, 2.0f, 3.0f, 0.0f, 1.0f}};
    int size = values[0].length;
    Integer1DSet ir_set = new Integer1DSet(size);
    FlatField histogram1 = new FlatField(ir_histogram, ir_set);
    histogram1.setSamples(values);

    float[][] counts = new float[][] {{0.0f, 1.0f, 2.0f, 3.0f}};
    Gridded1DSet count_set;
    count_set = new Gridded1DSet(count, counts, counts[0].length);

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

    DisplayImpl display1 = new DisplayImplJ3D("display1");

    display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.ShapeScale));
    display1.addMap(new ScalarMap(count, Display.Green));
    display1.addMap(new ConstantMap(1.0, Display.Blue));
    display1.addMap(new ConstantMap(1.0, Display.Red));
    ScalarMap shape_map = new ScalarMap(count, Display.Shape);
    display1.addMap(shape_map);
    ShapeControl shape_control = (ShapeControl) shape_map.getControl();
    shape_control.setShapeSet(count_set);
    shape_control.setShapes(shapes);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    display1.addReference(ref_histogram1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "shape in Java3D"; }

  public String toString() { return ": shape in Java3D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test47 t = new Test47(args);
  }
}
