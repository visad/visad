//
// GIFAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.gif;

import java.awt.Image;
import java.awt.Toolkit;

import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.TypeException;
import visad.VisADException;

/** this is an adapter for GIF and other images */
public class GIFAdapter
	implements ImageObserver
{
  private boolean badImage = false;
  private FlatField field = null;

  /** Create a VisAD FlatField from a local GIF, JPEG or PNG file
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public GIFAdapter(String filename)
	throws IOException, VisADException
  {
    loadImage(Toolkit.getDefaultToolkit().getImage(filename).getSource());
  }

  /** Create a VisAD FlatField from a GIF, JPEG or PNG on the Web.
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public GIFAdapter(URL url)
	throws IOException, VisADException
  {
    Object object = url.getContent();
    if (object == null || !(object instanceof ImageProducer)) {
      throw new MalformedURLException("URL does not point to an image");
    }

    loadImage((ImageProducer )object);
  }

  /** Helper function which keeps track of the image load.
    */
  public boolean imageUpdate(Image i, int f, int x, int y, int w, int h)
  {
    boolean rtnval = true;

    if ((f & ABORT) != 0) {
      badImage = true;
      rtnval = false;
    }
    if ((f & ALLBITS) != 0) {
      rtnval = false;
    }
    if ((f & ERROR) != 0) {
      badImage = true;
      rtnval = false;
    }

    return rtnval;
  }

  /** Build a FlatField from the image pixels
    * @param pixels image pixels.
    * @param width image width.
    * @param height image height.
    * @exception VisADException if an unexpected problem occurs.
    */
  private void buildFlatField(float[] red_pix, float[] green_pix, float[] blue_pix,
                              int width, int height) throws VisADException {
    RealType line = RealType.getRealType("ImageLine");
    RealType element = RealType.getRealType("ImageElement");
    RealType c_red = RealType.getRealType("Red");
    RealType c_green = RealType.getRealType("Green");
    RealType c_blue = RealType.getRealType("Blue");

    RealType[] c_all = {c_red, c_green, c_blue};
    RealTupleType radiance = new RealTupleType(c_all);

    RealType[] domain_components = {element, line};
    RealTupleType image_domain =
			new RealTupleType(domain_components);
    Linear2DSet domain_set =
			new Linear2DSet(image_domain,
					0.0, (float) (width - 1.0), width,
					(float) (height - 1.0), 0.0, height);
    FunctionType image_type =
			new FunctionType(image_domain, radiance);

    field = new FlatField(image_type, domain_set);

    float[][] samples = new float[3][];
    samples[0] = red_pix;
    samples[1] = green_pix;
    samples[2] = blue_pix;
    try {
      field.setSamples(samples, false);
    } catch (RemoteException e) {
      throw new VisADException("Couldn't finish image initialization");
    }
  }

  /** Load an image from an ImageProducer.
    * @exception IOException if there is a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  private void loadImage(ImageProducer producer)
	throws IOException, VisADException
  {
    Image image = Toolkit.getDefaultToolkit().createImage(producer);
    badImage = false;

    int width = -1;
    int height = -1;
    do {
      try { Thread.sleep(100); } catch (InterruptedException e) { }
      if (width < 0) {
	width = image.getWidth(this);
      }
      if (height < 0) {
	height = image.getHeight(this);
      }
    } while (!badImage && (width < 0 || height < 0));

    if (badImage) {
      throw new IOException("Not an image");
    }

    int numPixels = width * height;

    int[] words = new int[numPixels];
    float[] red_pix = new float[numPixels];
    float[] green_pix = new float[numPixels];
    float[] blue_pix = new float[numPixels];

    PixelGrabber grabber;
    grabber = new PixelGrabber(producer, 0, 0, width, height, words, 0, width);

    try {
      grabber.grabPixels();
    } catch (InterruptedException e) {
    }

    ColorModel cm = grabber.getColorModel();
    for (int i=0; i<numPixels; i++) {
      red_pix[i] = cm.getRed(words[i]);
      green_pix[i] = cm.getGreen(words[i]);
      blue_pix[i] = cm.getBlue(words[i]);
    }

    buildFlatField(red_pix, green_pix, blue_pix, width, height);
  }

  public FlatField getData()
  {
    return field;
  }
}
