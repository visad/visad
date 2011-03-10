//
// ShadowRealTypeJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

/**
   The ShadowRealTypeJ2D class shadows the RealType class,
   within a DataDisplayLink.<P>
*/
public class ShadowRealTypeJ2D extends ShadowScalarTypeJ2D {

  private Vector AccumulationVector = new Vector();

  public ShadowRealTypeJ2D(MathType type, DataDisplayLink link,
                           ShadowType parent)
         throws VisADException, RemoteException {
    super(type, link, parent);
    adaptedShadowType =
      new ShadowRealType(type, link, getAdaptedParent(parent));
  }

  /** clear AccumulationVector */
  void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
  }

  /** transform data into a Java2D VisADSceneGraphObject;
      return true if need post-process */
  public boolean doTransform(VisADGroup group, Data data, float[] value_array,
                      float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return ((ShadowRealType) adaptedShadowType).
                       doTransform(group, data, value_array,
                                   default_values, renderer, this);
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  void postProcess(VisADGroup group) throws VisADException {
    if (adaptedShadowType.getIsTerminal()) {
      int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        VisADGroup data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowRealTypeJ2D.postProcess");
      }
      else {
        // nothing to do
      }
    }
    else {
      // nothing to do
    }
    AccumulationVector.removeAllElements();
  }

}

