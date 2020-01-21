//
// DirectManipulationRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
   DirectManipulationRendererJ2D is the VisAD class for direct
   manipulation rendering under Java2D.<P>

   This DataRenderer supports direct manipulation for Real, RealTuple
   and Field Data objects (Field data objects must have RealType or
   RealTupleType ranges and Gridded1DSet domain Sets); no RealType may
   be mapped to multiple spatial DisplayRealTypes; the RealType of a
   Real object must be mapped to XAxis, YAxis or YAxis; at least one
   of the RealType components of a RealTuple object must be mapped to
   XAxis, YAxis or YAxis; the domain RealType and at least one RealType
   range component of a Field object must be mapped to XAxis or YAxis
*/
public class DirectManipulationRendererJ2D extends RendererJ2D {

  VisADGroup branch = null;
  VisADGroup extra_branch = null;

  /** this DataRenderer supports direct manipulation for Real,
      RealTuple and Field Data objects (Field data objects must
      have RealType or RealTupleType ranges and Gridded1DSet
      domain Sets); no RealType may be mapped to multiple spatial
      DisplayRealTypes; the RealType of a Real object must be
      mapped to XAxis, YAxis or YAxis; at least one of the
      RealType components of a RealTuple object must be mapped
      to XAxis, YAxis or YAxis; the domain RealType and at
      least one RealType range component of a Field object
      must be mapped to XAxis, YAxis or YAxis */
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

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      return;
    }
    DataDisplayLink link = Links[0];

    float[] default_values = link.getDefaultValues();
    DisplayImpl display = getDisplay();
    appearance.pointSize =
      default_values[display.getDisplayScalarIndex(Display.PointSize)];
    appearance.lineWidth =
      default_values[display.getDisplayScalarIndex(Display.LineWidth)];
    appearance.lineStyle = (int)
      default_values[display.getDisplayScalarIndex(Display.LineStyle)];
/* WLH 21 Aug 98
    GraphicsModeControl mode = getDisplay().getGraphicsModeControl();
    appearance.pointSize = mode.getPointSize();
    appearance.lineWidth = mode.getLineWidth();
    appearance.lineStyle = mode.getLineStyle();
*/
    appearance.red = 1.0f;
    appearance.green = 1.0f;
    appearance.blue = 1.0f;
    appearance.array = array;
    extra_branch.addChild(appearance);
  }

  public VisADGroup getExtraBranch() {
    return extra_branch;
  }

  /** create a VisADGroup scene graph for Data in links[0] */
  public synchronized VisADGroup doTransform()
         throws VisADException, RemoteException {
    branch = new VisADGroup();
    extra_branch = new VisADGroup();

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      return null;
    }
    DataDisplayLink link = Links[0];

    // values needed by drag_direct, which cannot throw Exceptions
    ShadowTypeJ2D shadow = (ShadowTypeJ2D) link.getShadow();

    // check type and maps for valid direct manipulation
    if (!getIsDirectManipulation()) {
      throw new BadDirectManipulationException(getWhyNotDirect() +
        ": DirectManipulationRendererJ2D.doTransform");
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
      extra_branch = null;
      addException(
        new DisplayException("Data is null: DirectManipulationRendererJ2D." +
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

  void addSwitch(DisplayRendererJ2D displayRenderer, VisADGroup branch)
       throws VisADException {
    displayRenderer.addDirectManipulationSceneGraphComponent(branch, this);
  }

  public boolean isLegalTextureMap() {
    return false;
  }

  public Object clone() {
    return new DirectManipulationRendererJ2D();
  }

}

