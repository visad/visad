//
// ShadowScalarTypeJ2D.java
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
   The ShadowScalarTypeJ2D class shadows the ScalarType class,
   within a DataDisplayLink.<P>
*/
public abstract class ShadowScalarTypeJ2D extends ShadowTypeJ2D {

  public ShadowScalarTypeJ2D(MathType type, DataDisplayLink link,
                           ShadowType parent)
         throws VisADException, RemoteException {
    super(type, link, parent);
  }

  public boolean getMappedDisplayScalar() {
    return adaptedShadowType.getMappedDisplayScalar();
  }

  public DisplayTupleType getDisplaySpatialTuple() {
    return ((ShadowScalarType) adaptedShadowType).getDisplaySpatialTuple();
  }

  public int[] getDisplaySpatialTupleIndex() {
    return ((ShadowScalarType) adaptedShadowType).getDisplaySpatialTupleIndex();
  }

  public int getIndex() {
    return ((ShadowScalarType) adaptedShadowType).getIndex();
  }

  public Vector getSelectedMapVector() {
    return ((ShadowScalarType) adaptedShadowType).getSelectedMapVector();
  }

}

