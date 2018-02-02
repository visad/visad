//
// TrajectoryAVHandlerJ3D.java
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

package visad.java3d;

import java.util.ArrayList;
import javax.media.j3d.Switch;
import visad.AVHandler;

/**
 *
 * @author rink
 */
public class TrajectoryAVHandlerJ3D implements AVHandler {
    int numChildren;
    int[] whichVisible;
    int numVisible;
    int direction;
    java.util.BitSet bits;
    Switch swit;

    ArrayList<Integer> allOffBelow = new ArrayList<Integer>();

    ArrayList<Integer> allOffAbove = new ArrayList<Integer>();
   
   public TrajectoryAVHandlerJ3D(Switch swit, int numChildren, int[] whichVisible, int direction) {
     this.swit = swit;
     this.numChildren = numChildren;
     this.whichVisible = whichVisible;
     this.numVisible = whichVisible.length;
     this.direction = direction;
     this.bits = new java.util.BitSet(numChildren);
   }
   
   public void setWhichChild(int index) {
      if (index == Switch.CHILD_NONE) {
        bits.clear();
        swit.setChildMask(bits);
      }
      else if (index >= 0) {
        bits.clear();
        for (int t=0; t<whichVisible.length; t++) {
          int k_set = index + whichVisible[t];
          if (k_set >= 0) {
            bits.set(k_set);
          }
        }

        int offBelow = 0;
        for (int k=0; k<allOffBelow.size(); k++) {
          int idx = allOffBelow.get(k).intValue();
          if (index >= idx) {
            offBelow = idx;
          }
        }
        if (offBelow > 0) {
          bits.clear(0, offBelow);
        }

        /* TODO: not working
        bits.set(0, numChildren-1);
        int offAbove = 0;
        for (int k=0; k<allOffAbove.size(); k++) {
          int idx = allOffAbove.get(k).intValue();
          if (index <= idx) {
            offAbove = idx;
          }
        }
        if (offAbove > 0) {
          bits.clear(offAbove, numChildren-1);
        }
        */

        swit.setChildMask(bits);
      }
   }
   
   public int getWindowSize() {
      return numVisible;
   }
   
   public void setNoneVisibleIndex(int index) {
      if (direction > 0) {
         allOffBelow.add(index);
      }
      else {
         // TODO: see above
      }
   }
   
}
