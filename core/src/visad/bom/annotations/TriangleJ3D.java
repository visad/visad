//
// TriangleJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
 *  Meant to encapsulate information representing a Triangle which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the label should
 *  stick in Screen Coordinates.
 */
public class TriangleJ3D implements ScreenAnnotation
{
  /** fill style POINT */
  public static final int POINT = 1;
  /** fill style LINE */
  public static final int LINE = 2;
  /** fill style FILL */
  public static final int FILL = 3;

  private int style;
  private int x1, y1;   // points of triangle in screen coordinates
  private int x2, y2;
  private int x3, y3;
  private float[] colour;
  private double zValue;
  private double thickness;          // for line and point style

  /**
   *  Simple constructor which makes a zero size triangle at (0, 0)
   *  coloured white. Set more meaningful values after construction.
   */
  public TriangleJ3D()
  {
    this(FILL, 0, 0, 0, 0, 0, 0, new float[] {1,1,1}, 0, 1.0);
  }

  /**
   *  Constructs a TriangleJ3D from 3 points specified in
   *  screen coordinates.
   *
   *  @param style  one of <ul>
   *         <li> TriangleJ3D.FILL, </li>
   *         <li> TriangleJ3D.LINE, </li>
   *         <li> TriangleJ3D.POINT.</li> <ul>
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param x3  x screen coordinate of the third point.
   *  @param y3  y screen coordinate of the third point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param thickness  used for outline thickness and point size.
   */
  public TriangleJ3D(int style,
    int x1, int y1, int x2, int y2, int x3, int y3,
    float[] colour, double zValue, double thickness)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.x3 = x3;
    this.y3 = y3;
    this.style = style;
    this.colour = colour;
    this.zValue = zValue;
    this.thickness = thickness;
  }

  /**
   *  Constructs a TriangleJ3D from 3 points specified in
   *  screen coordinates.
   *
   *  @param style  one of <ul>
   *         <li> TriangleJ3D.FILL, </li>
   *         <li> TriangleJ3D.LINE, </li>
   *         <li> TriangleJ3D.POINT.</li> <ul>
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 3 points (columns).
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param thickness  used for outline thickness and point size.
   */
  public TriangleJ3D(int style, int[][] points,
    float[] colour, double zValue, double thickness)
  {
    this(FILL, points[0][0], points[1][0], points[0][1],
      points[1][1], points[0][2], points[1][2],
      colour, zValue, thickness);  
  }

  /**
   *  Sets TriangleJ3D from 3 points specified in screen coordinates.
   *
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param x3  x screen coordinate of the third point.
   *  @param y3  y screen coordinate of the third point.
   */
  public void setPoints(int x1, int y1, int x2, int y2, int x3, int y3)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.x3 = x3;
    this.y3 = y3;
  } 

  /**
   *  Sets TriangleJ3D from the given array of 3 points specified
   *  in screen coordinates.
   *
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 3 points (columns).
   */
  public void setPoints(int[][] points)
  {
    setPoints(points[0][0], points[1][0], points[0][1],
      points[1][1], points[0][2], points[1][2]);
  }

  /**
   *  Set the drawing style of the triangle.
   *
   *  @param style  one of <ul>
   *         <li> TriangleJ3D.FILL, </li>
   *         <li> TriangleJ3D.LINE, </li>
   *         <li> TriangleJ3D.POINT.</li> <ul>
   */
  public void setStyle(int style)
  {
    this.style = style;
  }

  /**
   *  Set the colour of the triangle.
   *
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   */
  public void setColour(float[] colour)
  {
    this.colour = colour;
  }

  /**
   *  Set the Z value of the triangle.
   *
   *  @param zValue  Virtual world value; larger zValue is in front.
   */
  public void setZValue(double zValue)
  {
    this.zValue = zValue;
  }

  /**
   *  Make the TriangleJ3D into a {@link javax.media.j3d.Shape3D}.
   *
   *  @param display  the VisAD display for this Triangle.
   *
   *  @return the Triangle description as a Shape3D object.
   */
  public Object toDrawable(DisplayImpl display)
  {
    return ScreenAnnotatorUtils.makeTriangleShape3D(
      (DisplayImplJ3D)display, style,
      x1, y1, x2, y2, x3, y3, colour, zValue, thickness);
  }
} // class TriangleJ3D

