
//
// ShadowTextTypeJ3D.java
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

import java.util.*;
import java.rmi.*;

/**
   The ShadowTextTypeJ3D class shadows the TextType class,
   within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowTextTypeJ3D extends ShadowScalarTypeJ3D {

  private Vector AccumulationVector = new Vector();

  public ShadowTextTypeJ3D(MathType t, DataDisplayLink link,
                           ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
    adaptedShadowType =
      new ShadowTextType(t, link, getAdaptedParent(parent));
  }

  /** clear AccumulationVector */
  void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
  }
 
  /** transform data into a Java3D scene graph;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, float[] value_array,
                      float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    boolean post = ((ShadowTextType) adaptedShadowType).
                        doTransform(group, data, value_array,
                                    default_values, renderer, this);
    ensureNotEmpty(group);
    return post;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  void postProcess(Group group) throws VisADException {
    if (adaptedShadowType.getIsTerminal()) {
      int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        Group data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowTextTypeJ3D.postProcess");
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

