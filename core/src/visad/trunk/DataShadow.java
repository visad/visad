//
// DataShadow.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   DataShadow is the VIsAD class for gathering RealType value
   ranges, Animation sampling Sets, etc for Data display.<P>
*/
public class DataShadow extends Object implements java.io.Serializable {

  double[][] ranges; // [2][num_RealTypes] - 0=min, 1=max

  /** default Set for Animation sampling;
      order of precedence is:
      1. animationSampling
      2. animationRangeSampling
      3. new Linear1DSet(type, low_range, hi_range, 100) */
  Set animationSampling; // from a Field domain in data
  Set animationRangeSampling; // from a range value in data (should never happen)

  DataShadow(double[][] r) {
    ranges = r;
    animationSampling = null;
    animationRangeSampling = null;
  }

  boolean isAnimationSampling(boolean domain) {
    if (domain) {
      return false;
      // for first set: return (animationSampling != null);
    }
    else {
      return (animationSampling != null || animationRangeSampling != null);
    }
  }

  void setAnimationSampling(Set set, boolean domain)
       throws VisADException {
    if (set instanceof DoubleSet || set instanceof FloatSet) return;
    if (domain) {
      if (animationSampling == null) {
        animationSampling = set;
      }
      else {
        animationSampling = animationSampling.merge1DSets(set);
        // for first set: return;
      }
    }
    else {
      if (animationSampling == null && animationRangeSampling == null) {
        animationRangeSampling = set;
      }
    }
  }

  public void merge(DataShadow shadow)
         throws VisADException {
    int n = ranges[0].length;
    for (int i=0; i<n; i++) {
      if (shadow.ranges[0][i] < ranges[0][i]) ranges[0][i] = shadow.ranges[0][i];
      if (shadow.ranges[1][i] > ranges[1][i]) ranges[1][i] = shadow.ranges[1][i];
    }
    setAnimationSampling(shadow.animationSampling, true);
  }

}

