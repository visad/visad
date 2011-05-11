//
// PointJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
 *  Meant to encapsulate information representing a Point which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the label should
 *  stick in Screen Coordinates.
 */
public class PointJ3D implements ScreenAnnotation
{
  private int style;
  private int x1, y1;    // point in screen coordinates
  private float[] colour;
  private double zValue;
  private double thickness;      // for point size

  /**
   *  Simple constructor which makes a point at (0, 0)
   *  coloured white. More meaningful values can be set 
   *  after construction.
   */
  public PointJ3D()
  {
    this(0, 0, new float[] {1,1,1}, 0, 1.0);
  }

  /** 
   *  Constructs a PointJ3D from specified values in screen coordinates.
   *
   *  @param x1  x screen coordinate of the point.
   *  @param y1  y screen coordinate of the point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param thickness  used for the size of the point.
   */
  public PointJ3D(int x1, int y1,
    float[] colour, double zValue, double thickness)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.colour = colour;
    this.zValue = zValue;
    this.thickness = thickness;
  }

  /**
   *  Constructs a PointJ3D from specified values in screen coordinates.
   *
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 1 point (column).
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param thickness  used for the size of the point.
   */
  public PointJ3D(int[][] points,
    float[] colour, double zValue, double thickness)
  {
    this(points[0][0], points[1][0],
      colour, zValue, thickness);  
  }

  /**
   *  Set coordinates for the PointJ3D.
   *
   *  @param x1  x screen coordinate of the point.
   *  @param y1  y screen coordinate of the point.
   */
  public void setPointJ3Ds(int x1, int y1)
  {
    this.x1 = x1;
    this.y1 = y1;
  } 

  /**
   *  Set coordinates for the PointJ3D.
   *
   *  @param points  2 rows with each column containing a point
   *         in screen coordinates; requires 1 point (column).
   */
  public void setPointJ3Ds(int[][] points)
  {
    setPointJ3Ds(points[0][0], points[1][0]);
  }

  /**
   *  Set colour for the PointJ3D.
   *
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   */
  public void setColour(float[] colour)
  {
    this.colour = colour;
  }

  /**
   *  Set Z position for the PointJ3D.
   *
   *  @param zValue  Virtual world value; larger z is in front.
   */
  public void setZValue(double zValue)
  {
    this.zValue = zValue;
  }

  /**
   *  Make the PointJ3D into a {@link Shape3D}.
   *
   *  @param display  the VisAD display for this Point.
   *
   *  @return the Triangle description as a Shape3D object.
   */
  public Object toDrawable(DisplayImpl display)
  {
    return ScreenAnnotatorUtils.makePointShape3D(
      (DisplayImplJ3D)display,
      x1, y1, colour, zValue, thickness);
  }
} // class PointJ3D
