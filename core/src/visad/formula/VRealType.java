//
// VRealType.java
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

package visad.formula;

import java.util.Vector;
import visad.*;

/** Thing wrapper for visad.RealType.<P> */
public class VRealType extends ThingImpl {

  private static Vector realTypes = new Vector();

  private RealType realType;

  /** gets the VRealType corresponding to the specified RealType,
      creating it if necessary */
  public static VRealType get(RealType rt) {
    synchronized (realTypes) {
      int len = realTypes.size();
      for (int i=0; i<len; i++) {
        VRealType vrt = (VRealType) realTypes.elementAt(i);
        if (vrt.getRealType() == rt) return vrt;
      }
      return new VRealType(rt);
    }
  }

  /** constructor */
  public VRealType(RealType rt) {
    realType = rt;
    synchronized (realTypes) {
      realTypes.add(this);
    }
  }

  /** return the wrapper's RealType */
  public RealType getRealType() {
    return realType;
  }

}

