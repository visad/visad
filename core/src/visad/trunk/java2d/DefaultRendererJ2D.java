
//
// DefaultRendererJ2D.java
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

package visad.java2d;

import visad.*;

import java.util.*;
import java.rmi.*;


/**
   DefaultRendererJ2D is the VisAD class for the default graphics
   rendering algorithm under Java2D.<P>
*/
public class DefaultRendererJ2D extends RendererJ2D {

  // System.currentTimeMillis() when doTransform started
  long start_time;
  boolean time_flag;

  DataDisplayLink link;

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
    VisADGroup branch = new VisADGroup();
    link = getLinks()[0];
    ShadowTypeJ2D type = (ShadowTypeJ2D) link.getShadow();

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
        new DisplayException("Data is null: DefaultRendererJ2D.doTransform"));
    }
    else {
      start_time = System.currentTimeMillis();
      time_flag = false;
      type.preProcess();
      boolean post_process =
        type.doTransform(branch, data, valueArray,
                         link.getDefaultValues(), this);
      if (post_process) type.postProcess(branch);
    }
    link.clearData();
    return branch;
  }

  void addSwitch(DisplayRendererJ2D displayRenderer, VisADGroup branch)
       throws VisADException {
    displayRenderer.addSceneGraphComponent(branch);
  }

}

