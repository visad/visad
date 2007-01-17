//
// ShadowFunctionOrSetTypeJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Vector;
import java.rmi.*;

import java.awt.image.*;

/**
   The ShadowFunctionOrSetTypeJ2D is an abstract parent for
   ShadowFunctionTypeJ2D and ShadowSetTypeJ2D.<P>
*/
public class ShadowFunctionOrSetTypeJ2D extends ShadowTypeJ2D {

  ShadowRealTupleTypeJ2D Domain;
  ShadowTypeJ2D Range; // null for ShadowSetTypeJ2D

  private Vector AccumulationVector = new Vector();

  public ShadowFunctionOrSetTypeJ2D(MathType t, DataDisplayLink link,
                                    ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
    if (this instanceof ShadowFunctionTypeJ2D) {
      Domain = (ShadowRealTupleTypeJ2D)
               ((FunctionType) Type).getDomain().buildShadowType(link, this);
      Range = (ShadowTypeJ2D)
              ((FunctionType) Type).getRange().buildShadowType(link, this);
      adaptedShadowType =
        new ShadowFunctionType(t, link, getAdaptedParent(parent),
                       (ShadowRealTupleType) Domain.getAdaptedShadowType(),
                       Range.getAdaptedShadowType());
    }
    else {
      Domain = (ShadowRealTupleTypeJ2D)
               ((SetType) Type).getDomain().buildShadowType(Link, this);
      Range = null;
      adaptedShadowType =
        new ShadowSetType(t, link, getAdaptedParent(parent),
                       (ShadowRealTupleType) Domain.getAdaptedShadowType());
    }
  }

  public ShadowRealTupleTypeJ2D getDomain() {
    return Domain;
  }

  public ShadowTypeJ2D getRange() {
    return Range;
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
    if (this instanceof ShadowFunctionTypeJ2D) {
      Range.preProcess();
    }
  }


  /** transform data into a VisADSceneGraphObject;
      add generated scene graph components as children of group;
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process */
  public boolean doTransform(VisADGroup group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    return ((ShadowFunctionOrSetType) adaptedShadowType).
                             doTransform(group, data, value_array,
                                         default_values, renderer, this);
  }

  public void setTexCoords(float[] texCoords, float ratiow, float ratioh) {
    // corner 0
    texCoords[0] = 0.0f;
    texCoords[1] = 1.0f - ratioh;  // = 0.0f
    // corner 1
    texCoords[2] = ratiow;         // = 1.0f
    texCoords[3] = 1.0f - ratioh;  // = 0.0f
    // corner 2
    texCoords[4] = ratiow;         // = 1.0f
    texCoords[5] = 1.0f;
    // corner 3
    texCoords[6] = 0.0f;
    texCoords[7] = 1.0f;
  }

  public Vector getTextMaps(int i, int[] textIndices) {
    if (i < 0) {
      return ((ShadowTextTypeJ2D) Range).getSelectedMapVector();
    }
    else {
      ShadowTextTypeJ2D text = (ShadowTextTypeJ2D)
        ((ShadowTupleTypeJ2D) Range).getComponent(textIndices[i]);
      return text.getSelectedMapVector();
    }
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height)
         throws VisADException {
    // create basic Appearance
    VisADAppearance appearance =
      makeAppearance(mode, constant_alpha, constant_color, array);
    appearance.image = image;
    ((VisADGroup) group).addChild(appearance);
  }

  public Object makeSwitch() {
    return new VisADSwitch();
  }

  public Object makeBranch() {
    VisADGroup branch = new VisADGroup();
    return branch;
  }

  public void addToGroup(Object group, Object branch)
         throws VisADException {
    ((VisADGroup) group).addChild((VisADGroup) branch);
  }

  public void addToSwitch(Object swit, Object branch)
         throws VisADException {
    ((VisADSwitch) swit).addChild((VisADGroup) branch);
  }

  public void addSwitch(Object group, Object swit, Control control,
                        Set domain_set, DataRenderer renderer)
         throws VisADException {
    ((AVControlJ2D) control).addPair((VisADSwitch) swit, domain_set, renderer);
    ((AVControlJ2D) control).init();
    ((VisADGroup) group).addChild((VisADSwitch) swit);
  }

  public boolean recurseRange(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return Range.doTransform((VisADGroup) group, data, value_array,
                             default_values, renderer);
  }

  public boolean wantIndexed() {
    return true;
  }


  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(VisADGroup group) throws VisADException {
    if (((ShadowFunctionOrSetType) adaptedShadowType).getFlat()) {
      int LevelOfDifficulty = getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        VisADGroup data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowFunctionOrSetTypeJ2D.postProcess");
      }
      else {
        // includes !isTerminal
        // nothing to do
      }
    }
    else {
      if (this instanceof ShadowFunctionTypeJ2D) {
        Range.postProcess(group);
      }
    }
    AccumulationVector.removeAllElements();
  }

}

