//
// ImageRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bom;

import visad.*;
import visad.java3d.*;

import javax.media.j3d.*;

import java.util.*;
import java.rmi.*;


/**
   ImageRendererJ3D is the VisAD class for fast loading of images
   and image sequences under Java3D.<P>
*/
public class ImageRendererJ3D extends DefaultRendererJ3D {

  private MathType image_sequence_type;
  private MathType image_type;
  private boolean sequence;

  /** this DataRenderer supports fast loading of images and image
      sequences for DisplayImplJ3D */
  public ImageRendererJ3D () {
    super();
    try {
      image_type =
        MathType.stringToType("((ImageElement, ImageLine) -> ImageValue)");
      image_sequence_type = new FunctionType(RealType.Time, image_type);
    }
    catch (VisADException e) {
      throw new VisADError(e.getMessage());
    }
  }

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowImageFunctionTypeJ3D(type, link, parent);
  }

  public boolean getSequence() {
    return sequence;
  }

  public BranchGroup doTransform() throws VisADException, RemoteException { 
    DataDisplayLink link = getLinks()[0];
    MathType mtype = link.getType();
    if (image_sequence_type.equalsExceptName(mtype)) { 
      sequence = true;
    }
    else if (image_type.equalsExceptName(mtype)) {
      sequence = false;
    }
    else {
      throw new BadMappingException("must be image or image sequence");
    }

    BranchGroup branch = getBranch();
    if (branch == null) {
      branch = new BranchGroup();
      branch.setCapability(BranchGroup.ALLOW_DETACH);
    }
    link = getLinks()[0];
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();
 
    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }
 
    Data data = link.getData();
    if (data == null) {
      branch = null;
      addException(
        new DisplayException("Data is null: DefaultRendererJ3D.doTransform"));
    }
    else {
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
      type.doTransform(branch, data, valueArray,
                       link.getDefaultValues(), this);
    }
    link.clearData();
    return branch;
  }

}

