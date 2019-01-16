//
// ShadowNodeRealTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.cluster;

import visad.*;
import visad.java3d.*;

import java.rmi.*;

/**
   The ShadowNodeRealTypeJ3D class shadows the RealType class for
   NodeRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowNodeRealTypeJ3D extends ShadowRealTypeJ3D {

  public ShadowNodeRealTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    return ShadowNodeFunctionTypeJ3D.staticAddToGroup(group, array, mode, 
                                                      constant_alpha, constant_color);
  }

}

