//
// DirectManipulationRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import org.jogamp.java3d.*;

import java.rmi.*;


/**
   DirectManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering under Java3D.<P>
*/
public class DirectManipulationRendererJ3D extends RendererJ3D {

  BranchGroup branch = null;

  /** this DataRenderer supports direct manipulation for Real,
      RealTuple and Field Data objects (Field data objects must
      have RealType or RealTupleType ranges and Gridded1DSet
      domain Sets); no RealType may be mapped to multiple spatial
      DisplayRealTypes; the RealType of a Real object must be
      mapped to XAxis, YAxis or YAxis; at least one of the
      RealType components of a RealTuple object must be mapped
      to XAxis, YAxis or YAxis; the domain RealType and at
      least one RealType range component of a Field object
      must be mapped to XAxis, YAxis or ZAxis
*/
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
    if (branch == null) return;
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
    if (display == null) return;
    GeometryArray geometry = display.makeGeometry(array);

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      return;
    }
    DataDisplayLink link = Links[0];

    float[] default_values = link.getDefaultValues();
    GraphicsModeControl mode = (GraphicsModeControl)
      display.getGraphicsModeControl().clone();
    float pointSize =
      default_values[display.getDisplayScalarIndex(Display.PointSize)];
    float lineWidth =
      default_values[display.getDisplayScalarIndex(Display.LineWidth)];
    int lineStyle = (int)
      default_values[display.getDisplayScalarIndex(Display.LineStyle)];
    mode.setPointSize(pointSize, true);
    mode.setLineWidth(lineWidth, true);
    mode.setLineStyle(lineStyle, true);
    Appearance appearance =
      ShadowTypeJ3D.staticMakeAppearance(mode, null, null, geometry, false);

    Shape3D shape = new Shape3D(geometry, appearance);
    shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    BranchGroup group = new BranchGroup();
    group.setCapability(Group.ALLOW_CHILDREN_READ);
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

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      return null;
    }
    DataDisplayLink link = Links[0];

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
        new DisplayException("Data is null: DirectManipulationRendererJ3D." +
                             "doTransform"));
    }
    else {
      try {
        // no preProcess or postProcess for direct manipulation */
        shadow.doTransform(branch, data, valueArray,
                           link.getDefaultValues(), this);
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return null;
        }
        throw re;
      }
    }
    return branch;
  }

  /** for use by sub-classes that override doTransform() */
  public void setBranch(BranchGroup b) {
    branch = b;
  }

  void addSwitch(DisplayRendererJ3D displayRenderer, BranchGroup branch) {
    displayRenderer.addDirectManipulationSceneGraphComponent(branch, this);
  }

  public boolean isLegalTextureMap() {
    return false;
  }

  public void clearScene() {
    branch = null;
    super.clearScene();
  }

  public Object clone() {
    return new DirectManipulationRendererJ3D();
  }

}

