
//
// DefaultRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

package visad.java3d;

import visad.*;

import javax.media.j3d.*;

import java.util.*;
import java.rmi.*;


/**
   DefaultRendererJ3D is the VisAD class for the default graphics
   rendering algorithm under Java3D.<P>
*/
public class DefaultRendererJ3D extends RendererJ3D {

  public DefaultRendererJ3D () {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DefaultRendererJ3D.setLinks: must be " +
                                 "exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
  }

  /** create a BranchGroup scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException { // J3D
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    DataDisplayLink link = Links[0];
    Data data = link.getData();
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();

    // initialize valueArray to missing
    int valueArrayLength = display.getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    type.preProcess();
    boolean post_process =
      type.doTransform(branch, data, valueArray, link.getDefaultValues(), this);
    if (post_process) type.postProcess(branch);
    return branch;
  }

  void addSwitch(DisplayRendererJ3D displayRenderer, BranchGroup branch) {
    displayRenderer.addSceneGraphComponent(branch);
  }

}

