//
// RemoveBehaviorJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
 
import visad.*;
 
import java.awt.event.*;

import javax.media.j3d.*;

import java.awt.*;
import java.util.*;

/**
   RemoveBehaviorJ3D is the VisAD class for Java3D behaviors that
   remove BranchGroups after waiting for at least 1 frame.<P>
*/

public class RemoveBehaviorJ3D extends Behavior { // J3D

  /** DisplayRenderer for Display */
  DisplayRendererJ3D display_renderer;

/* WLH 3 Aug 98
  boolean waiting = false;
*/

  Vector removeVector = new Vector();

  public RemoveBehaviorJ3D(DisplayRendererJ3D r) {
    display_renderer = r;
/* WLH 3 Aug 98
    waiting = false;
*/
  }

  public synchronized void addRemove(RendererJ3D renderer, int index) {
    removeVector.addElement(new RendererIndexCount(renderer, index, 1));
/* WLH 3 Aug 98
    if (!waiting) {
      setWakeup();
      waiting = true;
    }
*/
  }

  public void initialize() {
/* WLH 18 Nov 98
    setWakeup();
*/
  }

  public synchronized void processStimulus(Enumeration criteria) {
    Enumeration removes = removeVector.elements();
    while (removes.hasMoreElements()) {
      RendererIndexCount ric = (RendererIndexCount) removes.nextElement();
      ric.count--;
      if (ric.count <= 0) {
        if (ric.renderer.switchTransition(ric.index)) {
          ric.count = 1;
        }
        else {
          removeVector.removeElement(ric);
        }
      }
    }
/* WLH 3 Aug 98
    if (!removeVector.isEmpty()) {
      setWakeup();
    }
    else {
      waiting = false;
    }
*/
/* WLH 18 Nov 98
    setWakeup();
*/
  }

  void setWakeup() {
    wakeupOn(new WakeupOnElapsedFrames(0));
    // wakeupOn(new WakeupOnElapsedFrames(1));
  }

  private class RendererIndexCount extends Object {
    RendererJ3D renderer;
    int index;
    int count;

    RendererIndexCount(RendererJ3D r, int i, int c) {
      renderer = r;
      index = i;
      count = c;
    }
  }

}

