//
// ShadowCurveSetTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bom;

import visad.*;
import visad.java3d.*;

import java.util.*;
import java.rmi.*;

import javax.media.j3d.*;

/**
   The ShadowCurveSetTypeJ3D class shadows the SetType class for
   CurveManipulationRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowCurveSetTypeJ3D extends ShadowSetTypeJ3D {

  /**
   * Construct a new ShadowCurveSetTypeJ3D.
   * @param    t      MathType of data (must be a SetType)
   * @param    link   DataDisplayLink to DataReference
   * @param    parent parent ShadowType.
   * @throws VisADException  problem creating ShadowType
   * @throws RemoteException  problem with remote object creation.
   */
  public ShadowCurveSetTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  /** 
   * Transform data into a Java3D scene graph.
   * @param  group           group to add generated scene graph components 
   *                         (children) to
   * @param  value_array     inherited valueArray values;
   * @param  default_values  defaults for each display.DisplayRealTypeVector;
   * @return true if need to post-process 
   * @throws VisADException  illegal data or some other VisAD error
   * @throws RemoteException illegal data or some other remote error
   */
  public boolean doTransform(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    boolean data_ok = true;
    if (data == null) data_ok = false;
    if (!(data instanceof UnionSet)) data_ok = false;
    if (((UnionSet) data).getManifoldDimension() != 1) data_ok = false; 
    SampledSet[] sets = ((UnionSet) data).getSets();
    for (int i=0; i<sets.length; i++) {
      if (!(sets[i] instanceof Gridded2DSet)) {
        data_ok = false;
        break;
      }
    }
    if (!data_ok) {
      throw new DisplayException("data must be UnionSet of Gridded2DSets " +
                                 "with manifold dimension = 1");
    }

    ((CurveManipulationRendererJ3D) renderer).default_values = default_values;

    boolean post = ((ShadowFunctionOrSetType) getAdaptedShadowType()).
                        doTransform(group, data, value_array,
                                    default_values, renderer, this);
    ensureNotEmpty(group);
    return post;
  }

}

