//
// GIFAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.gif;

import java.awt.Image;
import java.awt.Toolkit;

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

  /** Create a VisAD FlatField from a local GIF or JPEG file
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public GIFAdapter(String filename)
	throws IOException, VisADException
  {
    loadImage(Toolkit.getDefaultToolkit().getImage(filename).getSource());
  }

  /** Create a VisAD FlatField from a GIF or JPEG on the Web.
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
  private void buildFlatField(float[] pixels, int width, int height)
	throws VisADException
  {
    RealType line;
    try {
      line = new RealType("ImageLine");
    } catch (TypeException e) {
      line =  RealType.getRealTypeByName("ImageLine");
    }

    RealType element;
    try {
      element = new RealType("ImageElement");
    } catch (TypeException e) {
      element =  RealType.getRealTypeByName("ImageElement");
    }

    RealType radiance;
    try {
      radiance = new RealType("ImageRadiance");
    } catch (TypeException e) {
      radiance =  RealType.getRealTypeByName("ImageRadiance");
    }

    RealType[] domain_components = {line, element};
    RealTupleType image_domain =
			new RealTupleType(domain_components);
    Linear2DSet domain_set =
			new Linear2DSet(image_domain,
					0.0, (float) (width - 1.0), width,
					0.0, (float) (height - 1.0), height);
    FunctionType image_type =
			new FunctionType(image_domain, radiance);

    field = new FlatField(image_type, domain_set);

    float[][] samples = new float[1][];
    samples[0] = pixels;
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
    float[] pixels = new float[numPixels];

    PixelGrabber grabber;
    grabber = new PixelGrabber(producer, 0, 0, width, height, words, 0, width);

    try {
      grabber.grabPixels();
    } catch (InterruptedException e) {
    }

    for (int i = 0; i < numPixels; i++) {
      pixels[i] = words[i] & 0xff;
    }

    buildFlatField(pixels, width, height);
  }

  public FlatField getData()
  {
    return field;
  }
}
