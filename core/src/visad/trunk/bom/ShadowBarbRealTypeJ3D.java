//
// ShadowBarbRealTypeJ3D.java
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

/**
   The ShadowBarbRealTypeJ3D class shadows the RealType class for
   BarbRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowBarbRealTypeJ3D extends ShadowRealTypeJ3D {

  public ShadowBarbRealTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public VisADGeometryArray[] makeFlow(int which, float[][] flow_values,
                float flowScale, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException {
    DataRenderer renderer = getLink().getRenderer();
    return ShadowBarbRealTupleTypeJ3D.staticMakeFlow(getDisplay(), which,
               flow_values, flowScale, spatial_values, color_values,
               range_select, renderer, false);
  }

}

