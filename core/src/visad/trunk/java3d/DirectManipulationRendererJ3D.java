
//
// DirectManipulationRendererJ3D.java
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

package visad.java3d;
 
import visad.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.*;
import java.rmi.*;


/**
   DirectManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering under Java3D.<P>
*/
public class DirectManipulationRendererJ3D extends RendererJ3D {

  BranchGroup branch = null;

  public DirectManipulationRendererJ3D () {
    super();
  }
 
  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DirectManipulationRendererJ3D.setLinks: " +
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
    DisplayImplJ3D display = (DisplayImplJ3D) getDisplay();
    GeometryArray geometry = display.makeGeometry(array);

    DataDisplayLink link = getLinks()[0];
    float[] default_values = link.getDefaultValues();
    GraphicsModeControl mode = (GraphicsModeControl)
      display.getGraphicsModeControl().clone();
    float pointSize = 
      default_values[display.getDisplayScalarIndex(Display.PointSize)];
    float lineWidth =
      default_values[display.getDisplayScalarIndex(Display.LineWidth)];
    mode.setPointSize(pointSize, true);
    mode.setLineWidth(lineWidth, true);
    Appearance appearance =
      ShadowTypeJ3D.makeAppearance(mode, null, null, geometry);

    Shape3D shape = new Shape3D(geometry, appearance);
    BranchGroup group = new BranchGroup();
    group.addChild(shape);
    branch.addChild(group);
  }

  /** create a BranchGroup scene graph for Data in links[0] */
  public synchronized BranchGroup doTransform()
         throws VisADException, RemoteException {
    branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(Group.ALLOW_CHILDREN_READ);
    branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
    branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
 
    DataDisplayLink link = getLinks()[0];
    // values needed by drag_direct, which cannot throw Exceptions
    ShadowTypeJ3D shadow = (ShadowTypeJ3D) link.getShadow();
 
    // check type and maps for valid direct manipulation
    if (!getIsDirectManipulation()) {
      throw new BadDirectManipulationException(getWhyNotDirect() +
        ": DirectManipulationRendererJ3D.doTransform");
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
      addException(
        new DisplayException("Data is null: DirectManipulationRendererJ3D." +
                             "doTransform"));
    }
    else {
      // no preProcess or postProcess for direct manipulation */
      shadow.doTransform(branch, data, valueArray,
                       link.getDefaultValues(), this);
    }
    return branch;
  }
 
  void addSwitch(DisplayRendererJ3D displayRenderer, BranchGroup branch) {
    displayRenderer.addDirectManipulationSceneGraphComponent(branch, this);
  }

}

