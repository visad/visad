//
// LabelJ3D.java
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

import visad.TextControl;
import visad.VisADException;

import visad.DisplayImpl;
import visad.java3d.DisplayImplJ3D;

import visad.util.HersheyFont;

import java.awt.Font;

/**
 *  Meant to encapsulate information representing a label which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the label should
 *  stick in Screen Coordinates. Uses VisAD fonts.
 *
 *  Java3D dependent only in needing Shape3D.
 */
public class LabelJ3D implements ScreenAnnotation
{
  private String text;
  private int xLocation;      // screen coordinates
  private int yLocation;      // screen coordinates
  private float[] colour;
  // Only one of these may be non null
  private Font font;           // null for the default
  private HersheyFont hfont;   // null for the default
  private TextControl.Justification horizontalJustification;
  private TextControl.Justification verticalJustification;
  private double fontSizeInPixels;
  private double zValue;
  private boolean filled;
  private double thickness;
  private double orientation;  // anticlockwise from x-axis in degrees.
  private double charRotation;

  /**
   *  It constructs a LabelJ3D with the given text at the
   *  origin, coloured white, left justified, horizontally aligned
   *  with no character rotation.
   *
   *  @param text  text of the LabelJ3D.
   */
  public LabelJ3D(String text)
        {
    this(text, 0, 0, new float[] {1, 1, 1},
      null, null, 0.0, 12.0, true, 1.0, 0.0,
      TextControl.Justification.LEFT,
      TextControl.Justification.BOTTOM, 0.0);
  }

  /**
   *  It constructs a LabelJ3D with the given text at the
   *  specified location, coloured white, left justified,
   *  horizontally aligned with no character rotation.
   *
   *  @param text  text of the LabelJ3D.
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   */
  public LabelJ3D(String text, int xLocation, int yLocation)
        {
    this(text, xLocation, yLocation, new float[] {1, 1, 1},
      null, null, 0.0, 12.0, true, 1.0, 0.0,
      TextControl.Justification.LEFT,
      TextControl.Justification.BOTTOM,
      0.0);
  }

  /**
   *  Constructor for a filled font. It uses the java.awt.Font if it
   *  is not null, otherwise it will use the Hershey font. If both are
   *  null it will use the VisAD line font.
   *
   *  @param text  text of the LabelJ3D.
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param font  {@link java.awt.Font} to use.
   *  @param hfont  Hershey font to use; if both fonts are null
   *         then use the default VisAD line font.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param fontSizeInPixels  font size; by default characters
   *         are 12 pixels in size.
   *  @param horizontalJustification  one of <ul>
   *         <li> TextControl.Justification.LEFT
   *         <li> TextControl.Justification.CENTER
   *         <li> TextControl.Justification.RIGHT  </ul>
   *  @param verticalJustification  one of <ul>
   *         <li> TextControl.Justification.BOTTOM
   *         <li> TextControl.Justification.CENTER
   *         <li> TextControl.Justification.TOP  </ul>
   */
  public LabelJ3D(String text, int xLocation,
    int yLocation, float[] colour, Font font, HersheyFont hfont,
    double zValue, double fontSizeInPixels,
    TextControl.Justification horizontalJustification,
    TextControl.Justification verticalJustification)
  {
    this(text, xLocation, yLocation, colour, font, hfont,
      zValue, fontSizeInPixels, true, 1.0, 0.0,
      horizontalJustification, verticalJustification,
      0.0);
  }

  /**
   *  Constructor for a filled or unfilled font. It uses the java.awt.Font
   *  if it is not null otherwise it will use the Hershey font. If both
   *  are null it will use the VisAD line font.
   *
   *  @param text  text of the LabelJ3D.
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param font  java.awt.Font to use.
   *  @param hfont  Hershey font to use; if both fonts are null
   *         then use the default VisAD line font.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param fontSizeInPixels  font size; by default characters
   *         are 12 pixels in size.
   *  @param filled  if <code>true</code> the font is rendered as filled,
   *         if <code>false</code> just the triangles are drawn.
   *  @param thickness  line width to use if just drawing triangles;
   *          usually 1.0 is the most useful.
   *  @param horizontalJustification  one of <ul>
   *         <li> TextControl.Justification.LEFT
   *         <li> TextControl.Justification.CENTER
   *         <li> TextControl.Justification.RIGHT  </ul>
   *  @param verticalJustification  one of <ul>
   *         <li> TextControl.Justification.BOTTOM
   *         <li> TextControl.Justification.CENTER
   *         <li> TextControl.Justification.TOP  </ul>
   */
  public LabelJ3D(String text, int xLocation,
    int yLocation, float[] colour, Font font, HersheyFont hfont,
    double zValue, double fontSizeInPixels, boolean filled,
    double thickness,
    TextControl.Justification horizontalJustification,
    TextControl.Justification verticalJustification)
  {
    this(text, xLocation, yLocation, colour, font, hfont,
      zValue, fontSizeInPixels, filled, thickness, 0.0,
      horizontalJustification, verticalJustification,
      0.0);
  }

  /**
   *  Constructor for LabelJ3D. It uses the {@link java.awt.Font} if it
   *  is not null, otherwise it will use the Hershey font. If both are
   *  null it will use the VisAD line font.
   *
   *  @param text  text of the LabelJ3D.
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param font  {@link java.awt.Font} to use.
   *  @param hfont  Hershey font to use; if both fonts are null
   *         then use the default VisAD line font.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param fontSizeInPixels  font size; by default characters
   *         are 12 pixels in size.
   *  @param filled  if <code>true</code> the font is rendered as filled,
   *         if <code>false</code> just the triangles are drawn.
   *  @param thickness  line width to use if just drawing triangles;
   *          usually 1.0 is the most useful.
   *  @param orientation  anticlockwise from x-axis in degrees.
   *  @param horizontalJustification  one of <ul>
   *         <li> TextControl.Justification.LEFT
   *         <li> TextControl.Justification.CENTER
   *         <li> TextControl.Justification.RIGHT 
   *  @param verticalJustification  one of <ul>
   *         <li> TextControl.Justification.BOTTOM
   *         <li> TextControl.Justification.CENTER
   *         <li> TextControl.Justification.TOP </ul>
   *  @param charRotation  rotate each character
   *         <code>charRotation</code> degrees clockwise
   *         from base line.
   */
  public LabelJ3D(String text, int xLocation,
    int yLocation, float[] colour, Font font, HersheyFont hfont,
    double zValue, double fontSizeInPixels, boolean filled,
    double thickness, double orientation,
    TextControl.Justification horizontalJustification,
    TextControl.Justification verticalJustification,
    double charRotation)
  {
    this.text = text;
    this.xLocation = xLocation;
    this.yLocation = yLocation;
    this.colour = colour;
    this.font = font;
    if (font != null) {
      hfont = null;
    } else {
      this.hfont = hfont;
    }
    this.zValue = zValue;
    this.fontSizeInPixels = fontSizeInPixels;
    this.filled = filled;
    this.thickness = thickness;
    this.orientation = orientation;
    this.horizontalJustification = horizontalJustification;
    this.verticalJustification = verticalJustification;
    this.charRotation = charRotation;
  }

  /**
   *  Set the text to render.
   *
   *  @param text  text of the LabelJ3D.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   *  Set the position on the screen at which the text is rendered.
   *
   *  @param xLocation  x position in screen coordinates.
   *  @param yLocation  y position in screen coordinates.
   */
  public void setLocation(int xLocation, int yLocation)
  {
    this.xLocation = xLocation;
    this.yLocation = yLocation;
  }

  /**
   *  Set the colour used for the text.
   *
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   */
  public void setColour(float[] colour)
  {
                this.colour = colour;
  }

  /**
   *  Set the font to use for the text.
   *
   *  @param font  {@link java.awt.Font} to use.
   */
  public void setFont(Font font)
  {
                this.font = font;
    this.hfont = null;
  }

  /**
   *  Set the Hershey font to use for the text.
   *
   *  @param hfont  Hershey font to use; any java.awt.Font is ignored.
   */
  public void setHersheyFont(HersheyFont hfont)
  {
                this.hfont = hfont;
                this.font = null;
  }

  /**
   *  Set the virtual world z value.
   *
   *  @param zValue  Virtual world value; larger z is in front.
   */
  public void setZValue(double zValue)
  {
                this.zValue = zValue;
  }

  /**
   *  Set the font size in pixels.
   *
   *  @param fontSizeInPixels  font size; by default characters
   *         are 12 pixels in size.
   */
  public void setFontSize(double fontSizeInPixels)
  {
                this.fontSizeInPixels = fontSizeInPixels;
  }

  /**
   *  Set the line thickness for rendering the font. Useful in 
   *  unfilled text.
   *
   *  @param thickness  line width to use if just drawing triangles;
   *          usually 1.0 is the most useful.
   */
  public void setThickness(double thickness)
  {
                this.thickness = thickness;
  }

  /**
   *  Set the flag to control whether the text is filled or not.
   *
   *  @param filled  if <code>true</code> the font is rendered as filled,
   *         if <code>false</code> just the triangles are drawn.
   */
  public void setFilled(boolean filled)
  {
                this.filled = filled;
  }

  /**
   *  Set the orientation of the text (in degrees).
   *
   *  @param orientation  anticlockwise from x-axis in degrees.
   */
  public void setOrientation(double orientation)
  {
                this.orientation = orientation;
  }

  /**
   *  Set the horizontal justification of the text.
   *
   *  @param justification  one of TextControl.Justification.LEFT
   *         TextControl.Justification.CENTER
   *         TextControl.Justification.RIGHT 
   */
  public void setHorizontalJustification(TextControl.Justification 
    justification)
  {
                this.horizontalJustification = justification;
  }

  /**
   *  Set the vertical justification of the text.
   *
   *  @param justification  one of TextControl.Justification.BOTTOM
   *         TextControl.Justification.CENTER
   *         TextControl.Justification.TOP 
   */
  public void setVerticalJustification(TextControl.Justification 
    justification)
  {
                this.verticalJustification = justification;
  }

  /**
   *  Set the amount each character is rotated.
   *
   *  @param charRotation  rotate each character
   *         <code>charRotation</code> degrees clockwise
   *         from base line.
   */
  public void setCharRotation(double charRotation)
  {
    this.charRotation = charRotation;
  }

  /**
   *  Make the LabelJ3D into a {@link javax.media.j3d.Shape3D}.
   *
   *  @param display  the VisAD display for this Label.
   *
   *  @return the LabelJ3D description as a Shape3D.
   *  
   *  @throws VisADException - VisAD couldn't make the geometry array.
   */
  public Object toDrawable(DisplayImpl display)
    throws VisADException
  {
    return ScreenAnnotatorUtils.makeLabelShape3D(
      (DisplayImplJ3D)display, text,
      xLocation, yLocation, colour, font, hfont,
      zValue, fontSizeInPixels, filled, thickness,
      orientation,
      horizontalJustification, verticalJustification,
      charRotation);
  }

} // class LabelJ3D
