//
// BarGraph.java
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

package visad.util;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import visad.*;
import visad.java3d.*;

/** BarGraph provides methods for plotting colored bar graphs in 2D or 3D. */
public class BarGraph {

  // -- Constants --

  protected static final RealType BAR_X = RealType.getRealType("Bar_X");
  protected static final RealType BAR_Y = RealType.getRealType("Bar_Y");
  protected static final RealType BAR_Z = RealType.getRealType("Bar_Z");
  protected static final RealType BAR_R = RealType.getRealType("Bar_Red");
  protected static final RealType BAR_G = RealType.getRealType("Bar_Green");
  protected static final RealType BAR_B = RealType.getRealType("Bar_Blue");
  protected static final FunctionType BOX_2D;
  protected static final FunctionType BOX_3D;
  static {
    FunctionType func2d = null, func3d = null;
    try {
      RealTupleType rgb = new RealTupleType(BAR_R, BAR_G, BAR_B);
      func2d = new FunctionType(new RealTupleType(BAR_X, BAR_Y), rgb);
      func3d = new FunctionType(new RealTupleType(BAR_X, BAR_Y, BAR_Z), rgb);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    BOX_2D = func2d;
    BOX_3D = func3d;
  }

  // -- 2D bar graph method signatures --

  /**
   * Constructs a 2D bar graph.
   * @param height Height of each bar
   * @param spacing Spacing between bars (at least 0, and less than 1)
   * @param colors Color of each bar
   */
  public static FlatField makeBarGraph2D(float[] heights, float spacing,
    Color[] colors) throws VisADException, RemoteException
  {
    return makeBarGraph2D(BOX_2D, heights, spacing, colors);
  }

  /**
   * Constructs a 2D bar graph.
   * @param type MathType to use, of the form ((X, Y) -&gt; (R, G, B))
   * @param height Height of each bar
   * @param spacing Spacing between bars (at least 0, and less than 1)
   * @param colors Color of each bar
   */
  public static FlatField makeBarGraph2D(FunctionType type, float[] heights,
    float spacing, Color[] colors) throws VisADException, RemoteException
  {
    if (heights == null) throw new VisADException("Heights is null");
    int len = heights.length;
    float[] x1 = new float[len], y1 = new float[len];
    float[] x2 = new float[len], y2 = new float[len];
    float s = spacing / 2;
    for (int i=0; i<len; i++) {
      x1[i] = i + s;
      y1[i] = 0;
      x2[i] = i + 1 - s;
      y2[i] = heights[i];
    }
    return makeBoxes2D(type, x1, y1, x2, y2, colors);
  }

  public static FlatField makeBoxes2D(float[] x1, float[] y1,
    float[] x2, float[] y2, Color[] c) throws VisADException, RemoteException
  {
    return makeBoxes2D(BOX_2D, x1, y1, x2, y2, c);
  }

  public static FlatField makeBoxes2D(float[] x1, float[] y1,
    float[] x2, float[] y2, float[] r, float[] g, float[] b)
    throws VisADException, RemoteException
  {
    return makeBoxes2D(BOX_2D, x1, y1, x2, y2, r, g, b);
  }

  public static FlatField makeBoxes2D(FunctionType type,
    float[] x1, float[] y1, float[] x2, float[] y2, Color[] c)
    throws VisADException, RemoteException
  {
    float[][] rgb = extractColors(c);
    return makeBoxes2D(BOX_2D, x1, y1, x2, y2, rgb[0], rgb[1], rgb[2]);
  }

  public static FlatField makeBoxes2D(FunctionType type,
    float[] x1, float[] y1, float[] x2, float[] y2,
    float[] r, float[] g, float[] b) throws VisADException, RemoteException
  {
    if (type == null) throw new VisADException("Type is null");
    if (x1 == null || y1 == null || x2 == null || y2 == null) {
      throw new VisADException("Coordinates are null");
    }
    if (r == null || g == null || b == null) {
      throw new VisADException("Color values are null");
    }
    int len = x1.length;
    if (len != y1.length || len != x2.length || len != y2.length ||
      len != r.length || len != g.length || len != b.length)
    {
      throw new VisADException("Lengths do not match");
    }
    RealTupleType domain = type.getDomain();
    Gridded2DSet[] sets = new Gridded2DSet[len];
    float[][] colors = new float[3][4 * len];
    for (int i=0; i<len; i++) {
      float[][] samples = {
        {x1[i], x2[i], x1[i], x2[i]},
        {y1[i], y1[i], y2[i], y2[i]}
      };
      sets[i] = new Gridded2DSet(domain,
        samples, 2, 2, null, null, null, false);
      for (int j=0; j<4; j++) {
        int ndx = 4 * i + j;
        colors[0][ndx] = r[i];
        colors[1][ndx] = g[i];
        colors[2][ndx] = b[i];
      }
    }
    UnionSet uset = new UnionSet(domain, sets);
    FlatField ff = new FlatField(type, uset);
    ff.setSamples(colors, false);
    return ff;
  }

  // -- 3D bar graph method signatures --

  /**
   * Constructs a 3D bar graph.
   * @param height Height of each bar (dimensioned cols X rows)
   * @param spacing Spacing between bars (at least 0, and less than 1)
   * @param colors Color of each bar (dimensioned cols X rows)
   */
  public static FlatField makeBarGraph3D(float[][] heights, float spacing,
    Color[][] colors) throws VisADException, RemoteException
  {
    return makeBarGraph3D(BOX_2D, heights, spacing, colors);
  }

  /**
   * Constructs a 3D bar graph.
   * @param type MathType to use, of the form ((X, Y, Z) -&gt; (R, G, B))
   * @param height Height of each bar (dimensioned cols X rows)
   * @param spacing Spacing between bars (at least 0, and less than 1)
   * @param colors Color of each bar (dimensioned cols X rows)
   */
  public static FlatField makeBarGraph3D(FunctionType type, float[][] heights,
    float spacing, Color[][] colors) throws VisADException, RemoteException
  {
    if (heights == null) throw new VisADException("Heights is null");
    if (colors == null) throw new VisADException("Colors are null");
    int lenX = heights.length;
    if (lenX < 1) throw new VisADException("Not enough bars");
    if (lenX != colors.length) {
      throw new VisADException("Lengths do not match");
    }
    int lenY = heights[0].length;
    for (int c=0; c<lenX; c++) {
      if (heights[c] == null) {
        throw new VisADException("Heights[" + c + "] is null");
      }
      if (colors[c] == null) {
        throw new VisADException("Colors[" + c + "] is null");
      }
      if (lenY != heights[c].length || lenY != colors[c].length) {
        throw new VisADException("Lengths do not match");
      }
    }
    int len = lenX * lenY;
    float[] x1 = new float[len], y1 = new float[len], z1 = new float[len];
    float[] x2 = new float[len], y2 = new float[len], z2 = new float[len];
    Color[] cols = new Color[len];
    float s = spacing / 2;
    for (int r=0; r<lenY; r++) {
      for (int c=0; c<lenX; c++) {
        int i = lenX * r + c;
        x1[i] = c + s;
        y1[i] = r + s;
        z1[i] = 0;
        x2[i] = c + 1 - s;
        y2[i] = r + 1 - s;
        z2[i] = heights[c][r];
        cols[i] = colors[c][r];
      }
    }
    return makeBoxes3D(type, x1, y1, z1, x2, y2, z2, cols);
  }

  public static FlatField makeBoxes3D(float[] x1, float[] y1, float[] z1,
    float[] x2, float[] y2, float[] z2, Color[] c)
    throws VisADException, RemoteException
  {
    return makeBoxes3D(BOX_3D, x1, y1, z1, x2, y2, z2, c);
  }

  public static FlatField makeBoxes3D(float[] x1, float[] y1, float[] z1,
    float[] x2, float[] y2, float[] z2, float[] r, float[] g, float[] b)
    throws VisADException, RemoteException
  {
    return makeBoxes3D(BOX_3D, x1, y1, z1, x2, y2, z2, r, g, b);
  }

  public static FlatField makeBoxes3D(FunctionType type,
    float[] x1, float[] y1, float[] z1, float[] x2, float[] y2, float[] z2,
    Color[] c) throws VisADException, RemoteException
  {
    float[][] rgb = extractColors(c);
    return makeBoxes3D(BOX_3D,
      x1, y1, z1, x2, y2, z2, rgb[0], rgb[1], rgb[2]);
  }

  public static FlatField makeBoxes3D(FunctionType type,
    float[] x1, float[] y1, float[] z1, float[] x2, float[] y2, float[] z2,
    float[] r, float[] g, float[] b) throws VisADException, RemoteException
  {
    if (type == null) throw new VisADException("Type is null");
    if (x1 == null || y1 == null || z1 == null ||
      x2 == null || y2 == null || z2 == null)
    {
      throw new VisADException("Coordinates are null");
    }
    if (r == null || g == null || b == null) {
      throw new VisADException("Color values are null");
    }
    int len = x1.length;
    if (len != y1.length || len != z1.length ||
      len != x2.length || len != y2.length || len != z2.length ||
      len != r.length || len != g.length || len != b.length)
    {
      throw new VisADException("Lengths do not match");
    }

    RealTupleType domain = type.getDomain();
    UnionSet[] sets = new UnionSet[len];
    float[][] colors = new float[3][4 * 6 * len];
    for (int i=0; i<len; i++) {
      float[][] bottomSamples = {
        {x1[i], x2[i], x1[i], x2[i]},
        {y1[i], y1[i], y2[i], y2[i]},
        {z1[i], z1[i], z1[i], z1[i]}
      };
      Gridded3DSet bottom = new Gridded3DSet(domain,
        bottomSamples, 2, 2, null, null, null, false);

      float[][] topSamples = {
        {x1[i], x2[i], x1[i], x2[i]},
        {y1[i], y1[i], y2[i], y2[i]},
        {z2[i], z2[i], z2[i], z2[i]}
      };
      Gridded3DSet top = new Gridded3DSet(domain,
        topSamples, 2, 2, null, null, null, false);

      float[][] frontSamples = {
        {x1[i], x2[i], x1[i], x2[i]},
        {y1[i], y1[i], y1[i], y1[i]},
        {z1[i], z1[i], z2[i], z2[i]}
      };
      Gridded3DSet front = new Gridded3DSet(domain,
        frontSamples, 2, 2, null, null, null, false);

      float[][] backSamples = {
        {x1[i], x2[i], x1[i], x2[i]},
        {y2[i], y2[i], y2[i], y2[i]},
        {z1[i], z1[i], z2[i], z2[i]}
      };
      Gridded3DSet back = new Gridded3DSet(domain,
        backSamples, 2, 2, null, null, null, false);

      float[][] leftSamples = {
        {x1[i], x1[i], x1[i], x1[i]},
        {y1[i], y2[i], y1[i], y2[i]},
        {z1[i], z1[i], z2[i], z2[i]}
      };
      Gridded3DSet left = new Gridded3DSet(domain,
        leftSamples, 2, 2, null, null, null, false);

      float[][] rightSamples = {
        {x2[i], x2[i], x2[i], x2[i]},
        {y1[i], y2[i], y1[i], y2[i]},
        {z1[i], z1[i], z2[i], z2[i]}
      };
      Gridded3DSet right = new Gridded3DSet(domain,
        rightSamples, 2, 2, null, null, null, false);

      sets[i] = new UnionSet(domain,
        new SampledSet[] {bottom, top, front, back, left, right});

      for (int j=0; j<24; j++) {
        int ndx = 24 * i + j;
        colors[0][ndx] = r[i];
        colors[1][ndx] = g[i];
        colors[2][ndx] = b[i];
      }
    }
    UnionSet uset = new UnionSet(domain, sets);
    FlatField ff = new FlatField(type, uset);
    ff.setSamples(colors, false);
    return ff;
  }

  // -- Helper methods --

  /** Converts java.awt.Color objects to floating point RGB values. */
  public static float[][] extractColors(Color[] c) {
    if (c == null) return new float[][] {null, null, null};
    float[][] rgb = new float[3][c.length];
    for (int i=0; i<c.length; i++) {
      rgb[0][i] = c[i].getRed() / 255f;
      rgb[1][i] = c[i].getGreen() / 255f;
      rgb[2][i] = c[i].getBlue() / 255f;
    }
    return rgb;
  }

  // -- Main method --

  /** Run 'java visad.util.BarGraph' to test bar graphing. */
  public static void main(String[] argv)
    throws VisADException, RemoteException
  {
    JFrame frame = new JFrame("Bar Graphs in VisAD");
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
    frame.setContentPane(pane);

    // 2D bar graph
    float[] heights2 = {4, 7, 5, 11, 9};
    float max2 = 12; // top of graph
    Color[] colors2 = {Color.red, Color.yellow,
      Color.green, Color.gray, Color.magenta};
    FlatField barGraph2 = makeBarGraph2D(heights2, 0.2f, colors2);

    DisplayImplJ3D d2 = new DisplayImplJ3D("d2", new TwoDDisplayRendererJ3D());
    ScalarMap xMap2 = new ScalarMap(BAR_X, Display.XAxis);
    xMap2.setRange(0, heights2.length);
    d2.addMap(xMap2);
    ScalarMap yMap2 = new ScalarMap(BAR_Y, Display.YAxis);
    yMap2.setRange(0, max2);
    d2.addMap(yMap2);
    d2.addMap(new ScalarMap(BAR_R, Display.Red));
    d2.addMap(new ScalarMap(BAR_G, Display.Green));
    d2.addMap(new ScalarMap(BAR_B, Display.Blue));
    DataReferenceImpl ref2 = new DataReferenceImpl("ref2");
    ref2.setData(barGraph2);
    d2.addReference(ref2);
    pane.add(d2.getComponent());
    d2.getGraphicsModeControl().setScaleEnable(true);

    // 3D bar graph
    float[][] heights3 = {
      {8, 7, 5, 14, 9},
      {13, 1, 19, 7, 16},
      {6, 11, 12, 13, 4}
    };
    float max3 = 20; // top of graph
    Color darkPink = Color.pink.darker();
    Color darkYellow = Color.yellow.darker();
    Color darkMagenta = Color.magenta.darker();
    Color[][] colors3 = {
      {Color.red, Color.yellow, Color.green, Color.gray, Color.magenta},
      {Color.blue, Color.white, Color.orange, Color.pink, Color.lightGray},
      {Color.cyan, Color.darkGray, darkYellow, darkPink, darkMagenta}
    };
    FlatField barGraph3 = makeBarGraph3D(heights3, 0.2f, colors3);

    DisplayImplJ3D d3 = new DisplayImplJ3D("d3");
    ScalarMap xMap3 = new ScalarMap(BAR_X, Display.XAxis);
    xMap3.setRange(0, heights3.length);
    d3.addMap(xMap3);
    ScalarMap yMap3 = new ScalarMap(BAR_Y, Display.YAxis);
    yMap3.setRange(0, heights3[0].length);
    d3.addMap(yMap3);
    ScalarMap zMap3 = new ScalarMap(BAR_Z, Display.ZAxis);
    zMap3.setRange(0, max3);
    d3.addMap(zMap3);
    d3.addMap(new ScalarMap(BAR_R, Display.Red));
    d3.addMap(new ScalarMap(BAR_G, Display.Green));
    d3.addMap(new ScalarMap(BAR_B, Display.Blue));
    DataReferenceImpl ref3 = new DataReferenceImpl("ref3");
    ref3.setData(barGraph3);
    d3.addReference(ref3);
    pane.add(d3.getComponent());
    d3.getGraphicsModeControl().setScaleEnable(true);

    Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
    int w = ss.width - 100;
    frame.setBounds(50, 50, w, w / 2);
    frame.show();
  }

}
