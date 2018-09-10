//
// JLabelJ3D.java
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

import visad.VisADException;

import visad.DisplayImpl;
import visad.java3d.DisplayImplJ3D;

import java.awt.Font;

import org.jogamp.java3d.Text3D;

/**
 *  Meant to encapsulate information representing a label which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the label should
 *  stick in Screen Coordinates.
 *
 *  It should render using the Java3D Text3D class.
 *  Java3D dependent only in needing Text3D.
 */
public class JLabelJ3D implements ScreenAnnotation
{
  private String text;
  private int xLocation;      // screen coordinates
  private int yLocation;      // screen coordinates
  private float[] colour;
  private Font font;
  private int align;
  private int path;
  private double zValue;
  private double fontSizeInPixels;

  /**
   *  It constructs a JLabelJ3D with text at the
   *  origin, coloured white.
   *
   *  @param text  text of the JLabelJ3D.
   */
  public JLabelJ3D(String text)
        {
    // need a default Font
    this(text, 0, 0, new float[] {1, 1, 1},
      new Font("TestFont", Font.PLAIN, 1),
      0.0, 12.0, Text3D.ALIGN_FIRST, Text3D.PATH_RIGHT);
  }

  /**
   *  Constructor for a JLabel3D - It is filled and should have non
         *  null Font.
   *
   *  @param text  text of the JLabelJ3D.
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param font  {@link java.awt.Font} to use.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param fontSizeInPixels  font size.
   *  @param align  one of: <ul>
   *         <li>Text3D.ALIGN_FIRST
   *         <li>Text3D.ALIGN_CENTER
   *         <li>Text3D.ALIGN_LAST  </ul>
   *  @param path  one of: <ul>
   *         <li>Text3D.PATH_RIGHT
   *         <li>Text3D.PATH_LEFT
   *         <li>Text3D.PATH_DOWN
   *         <li>Text3D.PATH_UP  </ul>
   */
  public JLabelJ3D(String text, int xLocation,
    int yLocation, float[] colour, Font font, 
    double zValue, double fontSizeInPixels, int align, int path)
  {
    this.text = text;
    this.xLocation = xLocation;
    this.yLocation = yLocation;
    this.colour = colour;
    this.font = font;
    this.zValue = zValue;
    this.fontSizeInPixels = fontSizeInPixels;
    this.align = align;
    this.path = path;
  }

  /**
   *  @param text  text of the JLabelJ3D.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   */
  public void setLocation(int xLocation, int yLocation)
  {
    this.xLocation = xLocation;
    this.yLocation = yLocation;
  }

  /**
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   */
  public void setColour(float[] colour)
  {
                this.colour = colour;
  }

  /**
   *  @param font  {@link java.awt.Font} to use.
   */
  public void setFont(Font font)
  {
                this.font = font;
  }

  /**
   *  @param zValue  Virtual world value; larger z is in front.
   */
  public void setZValue(double zValue)
  {
                this.zValue = zValue;
  }

  /**
   *  @param fontSizeInPixels  font size; by default characters
   *         are 12 pixels in size.
   */
  public void setFontSize(double fontSizeInPixels)
  {
    this.fontSizeInPixels = fontSizeInPixels;
  }

  /**
   *  @param align  one of: <ul>
   *         <li>Text3D.ALIGN_FIRST
   *         <li>Text3D.ALIGN_CENTER
   *         <li>Text3D.ALIGN_LAST  </ul>
   */
  public void setAlign(int align)
  {
                this.align = align;
  }

  /**
   *  @param path  one of: <ul>
   *         <li>Text3D.PATH_RIGHT
   *         <li>Text3D.PATH_LEFT
   *         <li>Text3D.PATH_DOWN
   *         <li>Text3D.PATH_UP  </ul>
   */
  public void setPath(int path)
  {
                this.path = path;
  }

  /**
   *  Make the JLabelJ3D into a {@link org.jogamp.java3d.Shape3D}.
   *
   *  @param display  the VisAD display for this Label.
   *
   *  @return the JLabelJ3D description as a {@link org.jogamp.java3d.Shape3D}.
   *  
   *  @throws VisADException - VisAD couldn't make the geometry array.
   */
  public Object toDrawable(DisplayImpl display)
    throws VisADException
  {
    return ScreenAnnotatorUtils.makeJLabelShape3D(
      (DisplayImplJ3D)display, text,
      xLocation, yLocation, colour, font, 
      zValue, fontSizeInPixels, align, path);
  }

} // class JLabelJ3D
