
//
// DirectManipulationRendererJ2D.java
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
   DirectManipulationRendererJ2D is the VisAD class for direct
   manipulation rendering under Java2D.<P>
*/
public class DirectManipulationRendererJ2D extends RendererJ2D {

  VisADGroup branch = null;
  VisADGroup extra_branch = null;

  public DirectManipulationRendererJ2D () {
    super();
  }
 
  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DirectManipulationRendererJ2D.setLinks: " +
                                 "must be exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
  }

  public void checkDirect() throws VisADException, RemoteException {
    realCheckDirect();
  }

  public void addPoint(float[] x) throws VisADException {
    int count = x.length / 3;
    VisADGeometryArray array = null;
    if (count == 1) {
      array = new VisADPointArray();
    }
    else if (count == 2) {
      array = new VisADLineArray();
    }
    else {
      return;
    }
    array.coordinates = x;
    array.vertexCount = count;
    VisADAppearance appearance = new VisADAppearance();
    appearance.red = 1.0f;
    appearance.green = 1.0f;
    appearance.blue = 1.0f;
    appearance.array = array;
    extra_branch.addChild(appearance);
  }

  VisADGroup getExtraBranch() {
    return extra_branch;
  }

  /** create a VisADGroup scene graph for Data in links[0] */
  public synchronized VisADGroup doTransform()
         throws VisADException, RemoteException {
    branch = new VisADGroup();
    extra_branch = new VisADGroup();
 
    DataDisplayLink link = getLinks()[0];
    // values needed by drag_direct, which cannot throw Exceptions
    ShadowTypeJ2D shadow = (ShadowTypeJ2D) link.getShadow();
 
    // check type and maps for valid direct manipulation
    if (!getIsDirectManipulation()) {
      throw new BadDirectManipulationException(
        "DirectManipulationRendererJ2D.doTransform: " + getWhyNotDirect());
    }
 
    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }
 
    Data data = link.getData();
    if (data == null) {
      branch = null;
      extra_branch = null;
      addException(
        new DisplayException("DirectManipulationRendererJ2D." +
                             "doTransform: Data is null"));
    }
    else {
      // no preProcess or postProcess for direct manipulation */
      shadow.doTransform(branch, data, valueArray,
                       link.getDefaultValues(), this);
    }
    return branch;
  }
 
  void addSwitch(DisplayRendererJ2D displayRenderer, VisADGroup branch)
       throws VisADException {
    displayRenderer.addDirectManipulationSceneGraphComponent(branch, this);
  }

}

