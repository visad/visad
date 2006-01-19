//
// ShadowRealTupleTypeJ2D.java
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

package visad.java2d;

import visad.*;

import java.util.*;
import java.rmi.*;

/**
   The ShadowRealTupleTypeJ2D class shadows the RealTupleType class,
   within a DataDisplayLink, under Java2D.<P>
*/
public class ShadowRealTupleTypeJ2D extends ShadowTupleTypeJ2D {

  public ShadowRealTupleTypeJ2D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
        int n = ((TupleType) t).getDimension();
    tupleComponents = new ShadowRealTypeJ2D[n];
    ShadowRealType[] components = new ShadowRealType[n];
    for (int i=0; i<n; i++) {
      ShadowRealTypeJ2D shadow = (ShadowRealTypeJ2D)
        ((TupleType) Type).getComponent(i).buildShadowType(Link, this);
      tupleComponents[i] = shadow;
      components[i] = (ShadowRealType) shadow.getAdaptedShadowType();
    }
    adaptedShadowType =
      new ShadowRealTupleType(t, link, getAdaptedParent(parent),
                              components, this);
  }

  public ShadowRealTupleType getReference() {
    return ((ShadowRealTupleType) adaptedShadowType).getReference();
  }

}

