
//
// ShadowTupleTypeJ3D.java
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
   The ShadowTupleTypeJ3D class shadows the TupleType class,
   within a DataDisplayLink.<P>
*/
public class ShadowTupleTypeJ3D extends ShadowTypeJ3D {

  ShadowTypeJ3D[] tupleComponents;
  private Vector AccumulationVector = new Vector();

  public ShadowTupleTypeJ3D(MathType t, DataDisplayLink link,
                            ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
    if (this instanceof ShadowRealTupleTypeJ3D) return;

    int n = ((TupleType) t).getDimension();
    tupleComponents = new ShadowTypeJ3D[n];
    ShadowType[] components = new ShadowType[n];
    for (int i=0; i<n; i++) {
      ShadowTypeJ3D shadow = (ShadowTypeJ3D)
        ((TupleType) Type).getComponent(i).buildShadowType(Link, this);
      tupleComponents[i] = shadow;
      components[i] = shadow.getAdaptedShadowType();
    }
    adaptedShadowType =
      new ShadowTupleType(t, link, getAdaptedParent(parent),
                          components);
  }

  /** get number of components */
  public int getDimension() {
    return tupleComponents.length;
  }

  public ShadowType getComponent(int i) {
    return tupleComponents[i];
  }

  boolean isFlat() {
    return ((ShadowTupleType) adaptedShadowType).isFlat();
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
/*
    for (int i=0; i<num_components; i++) {
      component_type.preProcess();
    }
*/
  }

  /** transform data into a Java3D scene graph;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    boolean post = ((ShadowTupleType) adaptedShadowType).
                        doTransform(group, data, value_array,
                                    default_values, renderer, this);
    ensureNotEmpty(group);
    return post;
  }

  public boolean recurseComponent(int i, Object group, Data data,
             float[] value_array, float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return tupleComponents[i].doTransform((Group) group, data, value_array,
                                          default_values, renderer);
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
    if (adaptedShadowType.getIsTerminal()) {
      int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        Group data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowTupleTypeJ3D.postProcess");
      }
      else {
        // nothing to do
      }
    }
    else {
/*
      for (int i=0; i<num_components; i++) {
        component_type.postProcess(group);
      }
*/
    }
    AccumulationVector.removeAllElements();
  }

}

