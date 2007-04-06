//
// TiffForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.tiff;

import java.util.Hashtable;
import java.io.IOException;
import loci.formats.FormatException;
import loci.formats.in.TiffReader;
import loci.formats.out.TiffWriter;
import visad.FlatField;
import visad.data.BadFormException;
import visad.util.DataUtility;

/**
 * TiffForm is the VisAD data adapter for the TIFF file format.
 *
 * This class is just a wrapper for the TIFF logic in the loci.formats packages.
 */
public class TiffForm extends visad.data.bio.LociForm {

  public TiffForm() {
    super(new TiffReader(), new TiffWriter());
  }

  public void saveImage(String id, FlatField image, Hashtable ifd,
    boolean last) throws BadFormException, IOException
  {
    try {
      ((TiffWriter) writer).saveImage(id,
        DataUtility.extractImage(image, false), ifd, last);
    }
    catch (FormatException e) { throw new BadFormException(e); }
  }

  public static void main(String[] args) throws Exception {
    new TiffForm().testRead(args);
  }

}
