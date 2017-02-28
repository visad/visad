//
// LineJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
 *  Meant to encapsulate information representing a Line which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the label should
 *  stick in Screen Coordinates.
 */
public class LineJ3D implements ScreenAnnotation
{
  /** line style SOLID */
  public static final int SOLID = 4;

  /** line style DASH */
  public static final int DASH = 5;

  /** line style DOT */
  public static final int DOT = 6;

  /** line style DASH_DOT */
  public static final int DASH_DOT = 7;

  private int style;
  private int x1, y1;   // points of line in screen coordinates
  private int x2, y2;
  private float[] colour;
  private double zValue;
  private double thickness;    // for line and point style

  /**
   *  Simple constructor which makes a zero length line at (0, 0)
   *  coloured white. More meaningful values can be set
   *  after construction.
   */
  public LineJ3D()
  {
    this(SOLID, 0, 0, 0, 0,
      new float[] {1,1,1}, 0.0, 1.0);
  }

  /**
   *  Constructs a solid LineJ3D from 2 points specified
   *  in screen coordinates.
   *  
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param thickness  of the line.
   */
  public LineJ3D(int x1, int y1, int x2, int y2,
    float[] colour, double zValue, double thickness)
  {
    this(SOLID, x1,y1, x2,y2, 
      colour, zValue, thickness);  
  }

  /**
   *  Constructs a LineJ3D from 2 points specified in screen coordinates.
   *
   *  @param style  one of: <ul>
   *                 <li> LineJ3D.SOLID
   *                 <li> LineJ3D.DASH
   *                 <li> LineJ3D.DOT 
   *                 <li> LineJ3D.DASH_DOT </ul>
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 2 points (columns).
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front
   *  @param thickness  of the line.
   */
  public LineJ3D(int style, int[][] points,
    float[] colour, double zValue, double thickness)
  {
    this(style, points[0][0], points[1][0],
      points[0][1], points[1][1],
      colour, zValue, thickness);  
  }

  /**
   *  Constructs a LineJ3D from 2 points specified
   *  in screen coordinates.
   *  
   *  @param style  one of: <ul>
   *                 <li> LineJ3D.SOLID
   *                 <li> LineJ3D.DASH
   *                 <li> LineJ3D.DOT 
   *                 <li> LineJ3D.DASH_DOT </ul>
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param thickness  of the line.
   */
  public LineJ3D(int style,
    int x1, int y1, int x2, int y2,
    float[] colour, double zValue, double thickness)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.style = style;
    this.colour = colour;
    this.zValue = zValue;
    this.thickness = thickness;
  }

  /**
   *  Set the coordinates for the start and end points of the LineJ3D.
   *
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   */
  public void setPoints(int x1, int y1, int x2, int y2)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  } 

  /**
   *  Set the coordinates for the start and end points of the LineJ3D.
   *
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 2 points (columns).
   */
  public void setPoints(int[][] points)
  {
    setPoints(points[0][0], points[1][0],
        points[0][1], points[1][1]);
  }

  /**
   *  Set the line style for the LineJ3D.
   *
   *  @param style  one of: <ul>
   *                 <li> LineJ3D.SOLID
   *                 <li> LineJ3D.DASH
   *                 <li> LineJ3D.DOT
   *                 <li> LineJ3D.DASH_DOT </ul>
   */
  public void setStyle(int style)
  {
    this.style = style;
  }

  /**
   *  Set the colour for the LineJ3D.
   *
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   */
  public void setColour(float[] colour)
  {
    this.colour = colour;
  }

  /**
   *  Set the Z value for the LineJ3D.
   *
   *  @param zValue  Virtual world value; larger z is in front.
   */
  public void setZValue(double zValue)
  {
    this.zValue = zValue;
  }

  /**
   *  Make the LineJ3D into a {@link javax.media.j3d.Shape3D}.
   *
   *  @param display  the VisAD display for this Line.
   *
   *  @return the LineJ3D description as a Shape3D object.
   */
  public Object toDrawable(DisplayImpl display)
  {
    return ScreenAnnotatorUtils.makeLineShape3D(
      (DisplayImplJ3D)display, style,
      x1, y1, x2, y2, colour, zValue, thickness);
  }
} // class LineJ3D
