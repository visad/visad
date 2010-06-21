//
// VisADSwitch.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

/**
   VisADSwitch stands in for j3d.Switch
   and is Serializable.<P>
*/
public class VisADSwitch extends VisADGroup {

  private int which = -1;

  // used by visad.cluster.ShadowNodeFunctionTypeJ3D to store
  // Set for Animation VisADSwitch
  // null to indicate volume rendering VisADSwitch
  private Set set = null;

  public VisADSwitch() {
  }

  public int getWhichChild() {
    return which;
  }

  public synchronized void setWhichChild(int index) {
    if (index >= 0 && index < numChildren()) {
      which = index;
    }
  }

  public synchronized VisADSceneGraphObject getSelectedChild() {
    if (which >= 0 && which < numChildren()) {
      return getChild(which);
    }
    else {
      return null;
    }
  }

  public void setSet(Set s) {
    set = s;
  }

  public Set getSet() {
    return set;
  }

}

