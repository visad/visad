
//
// DataShadow.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   DataShadow is the VIsAD class for gathering RealType value
   ranges, Animation sampling Sets, etc for Data display.<P>
*/
public class DataShadow extends Object implements java.io.Serializable {

  double[][] ranges;

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

}

