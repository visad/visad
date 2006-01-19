//
// DefaultRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

  DataDisplayLink link = null;

  /** this is the default DataRenderer used by the addReference method
      for DisplayImplJ3D */
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
    link = links[0];
  }

  /** create a BranchGroup scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {
    if (link == null) return null;
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();

    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    Data data;
    try {
      data = link.getData();
    } catch (RemoteException re) {
      if (visad.collab.CollabUtil.isDisconnectException(re)) {
        getDisplay().connectionFailed(this, link);
        removeLink(link);
        return null;
      }
      throw re;
    }

    if (data == null) {
      branch = null;
      addException(
        new DisplayException("Data is null: DefaultRendererJ3D.doTransform"));
    }
    else {
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
      type.preProcess();
      boolean post_process;
      try {
        post_process =
          type.doTransform(branch, data, valueArray,
                           link.getDefaultValues(), this);
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return null;
        }
        throw re;
      }
      if (post_process) type.postProcess(branch);
    }
    link.clearData();
    return branch;
  }

  public void addSwitch(DisplayRendererJ3D displayRenderer,
                        BranchGroup branch) {
    displayRenderer.addSceneGraphComponent(branch);
  }

  public DataDisplayLink getLink() {
    return link;
  }

  public void clearScene() {
    link = null;
    super.clearScene();
  }

  public Object clone() throws CloneNotSupportedException {
    return new DefaultRendererJ3D();
  }

}

