//
// ImageJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Image;
import java.awt.Toolkit;

import visad.DisplayImpl;
import visad.VisADException;

import visad.java3d.DisplayImplJ3D;

import visad.util.ImageHelper;

/**
 *  Meant to encapsulate information representing an Image which is
 *  going to be rendered on a VisAD display without being
 *  subject to the usual VisAD transformations. Thus the Image should
 *  stick in Screen Coordinates.
 */
public class ImageJ3D implements ScreenAnnotation
{
  /** Image top left positioned on x, y */
  public static final int TOP_LEFT = 20;

  /** Image top right positioned on x, y */
  public static final int TOP_RIGHT = 21;

  /** Image bottom right positioned on x, y */
  public static final int BOTTOM_RIGHT = 22;

  /** Image bottom left positioned on x, y */
  public static final int BOTTOM_LEFT = 23;

  /** Image centre positioned on x, y */
  public static final int CENTER = 24;
  
  private Image image;
  private int position;
  private int x, y;    // location in screen coordinates
  private int width = -1, height = -1; // of the image, in pixels
  private double zValue;
  private double scaleFactor;      // for point size

  /** 
   *  Constructs a ImageJ3D from specified values in screen coordinates.
   *
   *  @param filename  of a valid GIF, JPEG or PNG file.
   *  @param position  how to place the image relative to (x, y);
   *         one of Image.TOP_LEFT (default), Image.TOP_RIGHT,
   *         Image.BOTTOM_RIGHT, Image.BOTTOM_LEFT, Image.CENTER
   *  @param x  x screen coordinate of image location.
   *  @param y  y screen coordinate of image location.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param scaleFactor  scale factor for image magnification; greater
   *         than 0.0.
   *
   *  @throws VisADException if the Image is bad.
   */
  public ImageJ3D(String filename, int position, int x, int y,
    double zValue, double scaleFactor)
        throws VisADException
  {
    this(Toolkit.getDefaultToolkit().getImage(filename),
      position, x, y, zValue, scaleFactor);
  }

  /** 
   *  Constructs a ImageJ3D from specified values in screen coordinates.
   *
   *  @param image  base java.awt.Image object to represent.
   *  @param position  how to place the image relative to (x, y);
   *         one of Image.TOP_LEFT (default), Image.TOP_RIGHT,
   *         Image.BOTTOM_RIGHT, Image.BOTTOM_LEFT, Image.CENTER
   *  @param x  x screen coordinate of image location.
   *  @param y  y screen coordinate of image location.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param scaleFactor  scale factor for image magnification; greater
   *         than 0.0.
   *
   *  @throws VisADException if the Image is bad.
   */
  public ImageJ3D(Image image, int position, int x, int y,
    double zValue, double scaleFactor)
        throws VisADException
  {
    this.position = position;
    this.x = x;
    this.y = y;
    this.zValue = zValue;
    this.scaleFactor = scaleFactor;
    setImage(image);
  }

  /**
   *  Set the Image for this object.
   *
   *  @param image  base java.awt.Image object to represent.
   *
   *  @throws VisADException if the Image is bad.
   */
  public void setImage(Image image)
        throws VisADException
  {
    this.image = image;
    // get the image width and height
    ImageHelper ih = new ImageHelper();
    do {
      if (width < 0) { width = image.getWidth(ih); }
      if (height < 0) { height = image.getHeight(ih); }
      if (ih.badImage || (width >=0 && height >= 0)) {
        break;
      }
      try {
        Thread.currentThread().sleep(100);
      } catch (InterruptedException e) {
        throw new VisADException("ImageJ3D: Interrupted!!");
      }
    } while (true);
    if (ih.badImage) {
      throw new VisADException("ImageJ3D: not an image");
    }
  }

  /**
   *  Set the relative position for this object.
   *
   *  @param position  how to place the image relative to (x, y);
   *         one of Image.TOP_LEFT (default), Image.TOP_RIGHT,
   *         Image.BOTTOM_RIGHT, Image.BOTTOM_LEFT, Image.CENTER
   */
  public void setPosition(int position)
  {
    this.position = position;
  }

  /**
   *  Set the amount to magnify the image.
   *
   *  @param scaleFactor  scale factor for image magnification; greater
   *         than 0.0.
   */
  public void setScaleFactor(double scaleFactor)
  {
    this.scaleFactor = scaleFactor;
  }

  /**
   *  Set coordinates for the ImageJ3D.
   *
   *  @param x  x screen coordinate of image location.
   *  @param y  y screen coordinate of image location.
   */
  public void setImageJ3Ds(int x, int y)
  {
    this.x = x;
    this.y = y;
  } 

  /**
   *  @param zValue  Virtual world value; larger z is in front.
   */
  public void setZValue(double zValue)
  {
    this.zValue = zValue;
  }

  /**
   *  @param display  the VisAD display for this Image.
   *
   *  @return the Image description as a Shape3D object.
   *
   *  @throws VisADException if there is a VisAD problem.
   */
  public Object toDrawable(DisplayImpl display)
        throws VisADException
  {
    return ScreenAnnotatorUtils.makeImageShape3D(
      (DisplayImplJ3D)display, image, position,
      x, y, width, height, zValue, scaleFactor);
  }
} // class ImageJ3D

