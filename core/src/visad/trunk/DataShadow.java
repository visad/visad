//
// DataShadow.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   DataShadow is the VisAD class for gathering RealType value
   ranges and Animation sampling Sets for auto-scaling Displays.<P>
*/
public class DataShadow extends Object implements java.io.Serializable {

  /** ranges of RealType values, dimensioned [2][num_RealTypes] where
      first index = 0 for min, = 1 for max */
  double[][] ranges;

  /** default Set for Animation sampling, from Field domains in data */
  Set animationSampling;

  /**
   * construct a new DataShadow with given ranges array and
   * null animationSampling
   * @param r ranges array
   */
  DataShadow(double[][] r) {
    ranges = r;
    animationSampling = null;
  }

  /**
   * @param domain flag indicating if this call is for Field domain
   * @return flag indicating that animationSampling is non-null
   *         and this call is not for a Field domain
   */
  boolean isAnimationSampling(boolean domain) {
    if (domain) {
      return false;
    }
    else {
      return (animationSampling != null);
    }
  }

  /**
   * set or merge a Set into animationSampling, as long as Set is
   * neither FloatSet nor DoubleSet, and this call is for a Field
   * domain
   * @param set Set to merge
   * @param domain flag indicating if this call is for Field domain
   * @throws VisADException a VisAD error occurred
   */
  void setAnimationSampling(Set set, boolean domain)
       throws VisADException {
    if (set instanceof DoubleSet || set instanceof FloatSet) return;
    if (domain) {
      if (animationSampling == null) {
        animationSampling = set;
      }
      else {
        animationSampling = animationSampling.merge1DSets(set);
      }
    }
  }

  /**
   * merge argument DataShadow into this DataShadow
   * @param shadow DataShadow to merge
   * @throws VisADException a VisAD error occurred
   */
  public void merge(DataShadow shadow) throws VisADException {
    int n = ranges[0].length;
    for (int i=0; i<n; i++) {
      if (shadow.ranges[0][i] < ranges[0][i]) {
        ranges[0][i] = shadow.ranges[0][i];
      }
      if (shadow.ranges[1][i] > ranges[1][i]) {
        ranges[1][i] = shadow.ranges[1][i];
      }
    }
    setAnimationSampling(shadow.animationSampling, true);
  }

}

