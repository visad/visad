
//
// DefaultRenderer.java
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

package visad;

import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;
import java.rmi.*;


/**
   DefaultRenderer is the VisAD class for the default graphics rendering
   algorithm.<P>
*/
public class DefaultRenderer extends Renderer {

  public DefaultRenderer () {
  }

  void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DefaultRenderer.setLinks: must be " +
                                 "exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
  }

  /** create a BranchGroup scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    DataDisplayLink link = Links[0];
    Data data = link.getData();
    ShadowType type = link.getShadow();

    double[] valueArray = new double[display.valueArrayLength];
    for (int i=0; i<display.valueArrayLength; i++) {
      valueArray[i] = link.DefaultValues[display.valueToScalar[i]];
    }

    type.preProcess();
    boolean post_process = type.doTransform(branch, data, valueArray);
    if (post_process) type.postProcess(branch);

    return branch;
  }

}

