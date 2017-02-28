//
// AnimationSetControlJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

/**
   AnimationSetControlJ2D is the VisAD class for sampling Animation
   steps under Java2D.<P>
*/
public class AnimationSetControlJ2D extends AnimationSetControl {

  private transient VisADCanvasJ2D canvas;

  public AnimationSetControlJ2D(DisplayImpl d, AnimationControl p) {
    super(d, p);
    if (d != null) {
      canvas = ((DisplayRendererJ2D) d.getDisplayRenderer()).getCanvas();
    }
  }

  public void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException {
    super.setSet(s, noChange);
    canvas.createImages((s==null)? -1 : s.getLength());
  }

}

