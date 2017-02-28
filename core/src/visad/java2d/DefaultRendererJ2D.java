//
// DefaultRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java2d;

import visad.*;

import java.rmi.*;

/**
   DefaultRendererJ2D is the VisAD class for the default graphics
   rendering algorithm under Java2D.<P>
*/
public class DefaultRendererJ2D extends RendererJ2D {

  DataDisplayLink link;

  /** this is the default DataRenderer used by the addReference method
      for DisplayImplJ2D */
  public DefaultRendererJ2D () {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DefaultRendererJ2D.setLinks: must be " +
                                 "exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
  }

  /** create a VisADGroup scene graph for Data in links[0] */
  public VisADGroup doTransform() throws VisADException, RemoteException { // J2D
    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      link = null;
      return null;
    }
    link = Links[0];

    ShadowTypeJ2D type = (ShadowTypeJ2D) link.getShadow();

    VisADGroup branch = new VisADGroup();

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
        new DisplayException("Data is null: DefaultRendererJ2D.doTransform"));
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

  void addSwitch(DisplayRendererJ2D displayRenderer, VisADGroup branch)
       throws VisADException {
    displayRenderer.addSceneGraphComponent(branch);
  }

  public DataDisplayLink getLink() {
    return link;
  }

  public Object clone() {
    return new DefaultRendererJ2D();
  }

}

