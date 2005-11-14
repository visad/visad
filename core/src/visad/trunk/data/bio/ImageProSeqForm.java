//
// ImageProSeqForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.bio;

import java.io.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.tiff.BaseTiffForm;
import visad.data.tiff.TiffTools;
import visad.data.tiff.TiffForm;
import java.util.StringTokenizer;

/**
 * ImageProSeqForm is the VisAD data adapter for Image-Pro SEQ files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class ImageProSeqForm extends TiffForm {

  // -- Static fields --

  /**
   * An array of shorts (length 12) with identical values in all of our
   * samples; assuming this is some sort of format identifier.
   */
  private static final int IMAGE_PRO_TAG_1 = 50288;

  /** Frame rate. */
  private static final int IMAGE_PRO_TAG_2 = 40105;

  /** Guessing this is thumbnail pixel data. */
  private static final int IMAGE_PRO_TAG_3 = 40106;


  // -- Internal BaseTiffForm API methods --

  /** Overridden to include the three SEQ-specific tags. */
  protected void initStandardMetadata() {
    super.initStandardMetadata();

    int imageCount = 0;
    for (int j=0; j<ifds.length; j++) {
      short[] tag1 = (short[]) TiffTools.getIFDValue(ifds[j], IMAGE_PRO_TAG_1);

      if (tag1 != null) {
        String seqId = "";
        for (int i=0; i<tag1.length; i++) seqId = seqId + tag1[i];
        metadata.put("Image-Pro SEQ ID", seqId);
      }

      int tag2 = TiffTools.getIFDIntValue(ifds[0], IMAGE_PRO_TAG_2);

      if (tag2 != -1) {
        // should be one of these for every image plane
        imageCount++;
        metadata.put("Frame Rate", new Integer(tag2));
      }
      else {
        imageCount = 1;
      }
      metadata.put("Number of images", new Integer(imageCount));
    }

    String description = (String)
      TiffTools.getIFDValue(ifds[0], TiffTools.IMAGE_DESCRIPTION);

    // default values
    metadata.put("slices", "1");
    metadata.put("channels", "1");
    metadata.put("frames", new Integer(imageCount));

    // parse the description to get channels/slices/times where applicable
    if (description != null) {
      StringTokenizer tokenizer = new StringTokenizer(description, "\n");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        String label = token.substring(0, token.indexOf("="));
        String data = token.substring(token.indexOf("=")+1);
        metadata.put(label, data);
      }
    }
  }

  /** Overridden to include the three SEQ-specific tags. */
  protected void initOMEMetadata() {
    super.initOMEMetadata();

    if (ome != null) {
      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" +
        metadata.get("slices"));
      OMETools.setAttribute(ome, "Pixels", "SizeC", "" +
        metadata.get("channels"));
      OMETools.setAttribute(ome, "Pixels", "SizeT", "" +
        metadata.get("frames"));
    }
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for an Image-Pro file. */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".seq");
  }

  /** Returns the default file suffixes for the Image-Pro SEQ file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"seq"};
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.ImageProSeqForm in_file' to test read
   * an Image-Pro SEQ file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new ImageProSeqForm(), "Image-Pro SEQ", args);
  }

}
