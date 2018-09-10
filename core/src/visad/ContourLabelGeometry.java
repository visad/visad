//
// ContourLabelGeometry.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.util.*;

/**
   VisADGeometryArray stands in for j3d.GeometryArray
   and is Serializable.<P>
*/
public class ContourLabelGeometry extends VisADGeometryArray
       implements Cloneable {

  public VisADGeometryArray label;
  public VisADLineArray labelAnchor;
  public VisADLineArray expSegLeft;
  public VisADLineArray segLeftAnchor;
  public float[] segLeftScaleInfo;
  public VisADLineArray expSegRight;
  public VisADLineArray segRightAnchor;
  public float[] segRightScaleInfo;

  public boolean isStyled = false;

  public ContourLabelGeometry(VisADGeometryArray label, VisADLineArray labelAnchor,
                    VisADLineArray expSegLeft, VisADLineArray segLeftAnchor, float[] segLeftScaleInfo,
                    VisADLineArray expSegRight, VisADLineArray segRightAnchor, float[] segRightScaleInfo) {
    this.label = label;
    this.labelAnchor = labelAnchor;
    this.expSegLeft = expSegLeft;
    this.segLeftAnchor = segLeftAnchor;
    this.segLeftScaleInfo = segLeftScaleInfo;
    this.expSegRight = expSegRight;
    this.segRightAnchor = segRightAnchor;
    this.segRightScaleInfo = segRightScaleInfo;
  }


  /** eliminate any vectors or triangles crossing seams of
      map projections, defined by display-side CoordinateSystems;
      this default implementation does nothing */
  public ContourLabelGeometry adjustSeam(DataRenderer renderer)
         throws VisADException {
    ContourLabelGeometry cntr = new ContourLabelGeometry(label.adjustSeam(renderer), labelAnchor,
            (VisADLineArray)expSegLeft.adjustSeam(renderer), segLeftAnchor, segLeftScaleInfo,
            (VisADLineArray)expSegRight.adjustSeam(renderer), segRightAnchor, segRightScaleInfo);
    cntr.isStyled = isStyled;
    return cntr;
  }

  /** split any vectors or triangles crossing crossing longitude
      seams when Longitude is mapped to a Cartesian display axis;
      default implementation: rotate if necessary, then return points */
  public ContourLabelGeometry adjustLongitude(DataRenderer renderer)
         throws VisADException {
    ContourLabelGeometry cntr = new  ContourLabelGeometry(label.adjustLongitude(renderer), labelAnchor,
              (VisADLineArray)expSegLeft.adjustLongitude(renderer),
              segLeftAnchor, segLeftScaleInfo,
              (VisADLineArray)expSegRight.adjustLongitude(renderer),
              segRightAnchor, segRightScaleInfo);
    cntr.isStyled = isStyled;
    return cntr;
  }

  public ContourLabelGeometry removeMissing() {
    ContourLabelGeometry cntr = new  ContourLabelGeometry(label.removeMissing(), labelAnchor,
              (VisADLineArray)expSegLeft.removeMissing(),
              segLeftAnchor, segLeftScaleInfo,
              (VisADLineArray)expSegRight.removeMissing(),
              segRightAnchor, segRightScaleInfo);
    cntr.isStyled = isStyled;
    return cntr;
  }

  public Object clone() {
    return this;
  }
}
