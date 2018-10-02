//
// GIFAdapter.java
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

package visad.data.gif;

import java.awt.Image;
import java.awt.Toolkit;

import java.awt.image.ImageProducer;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import visad.FlatField;
import visad.VisADException;

import visad.util.DataUtility;

/** this is an adapter for GIF and other images */
public class GIFAdapter {

  private FlatField field = null;

  /** Create a VisAD FlatField from a local GIF, JPEG or PNG file
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public GIFAdapter(String filename)
	throws IOException, VisADException
  {
    Image image = Toolkit.getDefaultToolkit().getImage(filename);
    field = DataUtility.makeField(image);
  }

  /** Create a VisAD FlatField from a GIF, JPEG or PNG on the Web.
    * @param url File URL.
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
    ImageProducer producer = (ImageProducer) object;
    Image image = Toolkit.getDefaultToolkit().createImage(producer);
    field = DataUtility.makeField(image);
  }

  public FlatField getData() {
    return field;
  }
}
