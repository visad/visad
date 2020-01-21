//
// QuadrilateralJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bom.annotations;

import visad.DisplayImpl;
import visad.java3d.DisplayImplJ3D;

/**
 *  Meant to encapsulate information representing a Quadrilateral which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the label should
 *  stick in Screen Coordinates.
 */
public class QuadrilateralJ3D implements ScreenAnnotation
{
  /** fill style POINT */
  public static final int POINT = 1;
  /** fill style LINE */
  public static final int LINE = 2;
  /** fill style FILL */
  public static final int FILL = 3;

  private int style;
  // For a "box" (x1, y1) is top left, (x2-x1) is width, (y3-y2) is height
  private int x1, y1;
  private int x2, y2;
  private int x3, y3;
  private int x4, y4;
  private float[] colour;
  private double zValue;
  private double thickness;          // for line and point style

  /**
   *  Simple constructor which makes a zero size "box" at (0, 0)
   *  coloured white. More meaningful values can be set
   *  after construction.
   */
  public QuadrilateralJ3D()
  {
    this(FILL, 0, 0, 0, 0, new float[] {1,1,1}, 0, 1.0);
  }

  /**
   *  Constructor to make an upright "box" with the given specifications;
   *  the box should not be self intesecting.
   *
   *  @param style  one of <ul>
   *                <li> QuadrilateralJ3D.FILL, </li>
   *                <li> QuadrilateralJ3D.LINE, </li>
   *                <li> QuadrilateralJ3D.POINT.</li> </ul>
   *  @param x  top left x value in screen coordinates.
   *  @param y  top left y value in screen coordinates.
   *  @param width  width, in pixels, of the "box".
   *  @param height  height, in pixels, of the "box".
   *  @param colour  red, green blue triple each in the range [0.0, 1.0].
   *  @param zValue  Virtual world value - larger z is closer to eye.
   *  @param thickness  used for outline thickness and point size.
   */
  public QuadrilateralJ3D(int style, int x, int y,
    int width, int height, float[] colour, double zValue,
    double thickness)
  {
    this(style, x, y, x + width, y, x + width,
      y + height, x, y + height, colour, zValue, thickness);
  }

  /**
   *  Constructor to make an arbitrary rectangle with the given
   *  specifications; it should not be self intersecting.
   *
   *  @param style  one of <ul>
   *                <li> QuadrilateralJ3D.FILL, </li>
   *                <li> QuadrilateralJ3D.LINE, </li>
   *                <li> QuadrilateralJ3D.POINT.</li> </ul>
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 4 points (columns).
   *  @param colour  red, green blue triple each in the range [0.0, 1.0]
   *  @param zValue  Virtual world value - larger z is closer to eye.
   *  @param thickness  used for outline thickness and point size.
   */
  public QuadrilateralJ3D(int style,
    int[][] points,
    float[] colour, double zValue, double thickness)
  {
    this(style, points[0][0], points[1][0],
      points[0][1], points[1][1], points[0][2], points[1][2],
      points[0][3], points[1][3], colour, zValue, thickness);
  }

  /**
   *  Constructor to make an arbitrary rectangle with the given
   *  specifications; it should not be self intersecting.
   *
   *  @param style  one of <ul>
   *                <li> QuadrilateralJ3D.FILL, </li>
   *                <li> QuadrilateralJ3D.LINE, </li>
   *                <li> QuadrilateralJ3D.POINT.</li> </ul>
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param x3  x screen coordinate of the third point.
   *  @param y3  y screen coordinate of the third point.
   *  @param x4  x screen coordinate of the fourth point.
   *  @param y4  y screen coordinate of the fourth point.
   *  @param colour  red, green blue triple each in the range [0.0, 1.0].
   *  @param zValue  Virtual world value - larger z is closer to eye.
   *  @param thickness  used for outline thickness and point size.
   */
  public QuadrilateralJ3D(int style,
    int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4,
    float[] colour, double zValue, double thickness)
  {
    this.style = style;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.x3 = x3;
    this.y3 = y3;
    this.x4 = x4;
    this.y4 = y4;
    this.colour = colour;
    this.zValue = zValue;
    this.thickness = thickness;
  }

  /**
   *  Set the drawing style of the quadrilateral.
   *
   *  @param style  one of <ul>
   *                <li> QuadrilateralJ3D.FILL, </li>
   *                <li> QuadrilateralJ3D.LINE, </li>
   *                <li> QuadrilateralJ3D.POINT.</li> </ul>
   */
  public void setStyle(int style)
  {
    this.style = style;
  }

  /**
   *  Applies a shift to the quadrilateral to place the first point
   *  on the given coordinates.
   *  <p>
   *  Expects a "box" i.e. a vertically aligned rectangle
   *  with points enumerated clockwise. This sets top left to (x, y)
   *  with other points adjusted accordingly. If it is not a "box"
   *  then all points are shifted as though moving the first
   *  point to (x, y).
   *
   *  @param x  new top left x pixel value.
   *  @param y  new top left y pixel value.
   */
  public void setLocation(int x, int y)
  {
    int w = x1 - x;
    int h = y1 - y;
    x1 = x;
    y1 = y;
    x2 = x2 - w;
    y2 = y2 - h;
    x3 = x3 - w;
    y3 = y3 - h;
    x4 = x4 - w;
    y4 = y4 - h;
  }

  /**
   * Constructs a rectangular box using the existing first point
   * as the top left point and the input width and height.
   *
   * @param width  width, in pixels, of the "box".
   * @param height  height, in pixels, of the "box".
   */
  public void setSize(int width, int height)
  {
    x2 = x1 + width;
    y2 = y1;
    x3 = x2;
    y3 = y1 + height;
    x4 = x1;
    y4 = y2;
  }

  /**
   *  Sets the points for an arbitrary quadrilateral; should not
   *  be self intersecting.
   *
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 4 points (columns).
   */
  public void setPoints(int[][] points)
  {
    setPoints(points[0][0], points[1][0],
        points[0][1], points[1][1],
        points[0][2], points[1][2],
        points[0][3], points[1][3]);
  }

  /**
   *  Makes an upright "box".
   *
   * @param x  top left x value in screen coordinates.
   * @param y  top left y value in screen coordinates.
   * @param width  width, in pixels, of the "box".
   * @param height  height, in pixels, of the "box".
   */
  public void setPoints(int x, int y, int width, int height)
  {
    x1 = x;
    y1 = y;
    setSize(width, height);
  }

  /**
   *  Sets the 4 points for an arbitrary quadrilateral; should not
   *  be self intersecting.
   *
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param x3  x screen coordinate of the third point.
   *  @param y3  y screen coordinate of the third point.
   *  @param x4  x screen coordinate of the fourth point.
   *  @param y4  y screen coordinate of the fourth point.
   */
  public void setPoints(
    int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.x3 = x3;
    this.y3 = y3;
    this.x4 = x4;
    this.y4 = y4;
  }

  /**
   *  Set the colour for the quadrilateral.
   *
   *  @param colour  red, green blue triple each in the range [0.0, 1.0]
   */
  public void setColour(float[] colour)
  {
    this.colour = colour;
  }

  /**
   *  Set the Z value for the quadrilateral.
   *
   *  @param zValue  Virtual world value - larger z is closer to eye.
   */
  public void setZValue(double zValue)
  {
    this.zValue = zValue;
  }

  /**
   *  Set the thickness for the quadrilateral.
   *
   *  @param thickness  used for outline thickness and point size.
   */
  public void setThickness(double thickness)
  {
    this.thickness = thickness;
  }

  /**
   *  Make the QuadrilateralJ3D into a {@link javax.media.j3d.Shape3D}.
   *
   *  @param display  the VisAD display for this Quadrilateral.
   *
   *  @return the QuadrilateralJ3D description as a Shape3D object
   */
  public Object toDrawable(DisplayImpl display)
  {
    return ScreenAnnotatorUtils.makeQuadrilateralShape3D(
      (DisplayImplJ3D)display, style,
      x1, y1, x2, y2, x3, y3, x4, y4, colour,
      zValue, thickness);
  }
} // class QuadrilateralJ3D
