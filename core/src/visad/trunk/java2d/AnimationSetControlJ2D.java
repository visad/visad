
//
// AnimationSetControlJ2D.java
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

package visad.java2d;
 
import visad.*;

import java.rmi.*;

/**
   AnimationSetControlJ2D is the VisAD class for sampling Animation
   steps under Java2D.<P>
*/
public class AnimationSetControlJ2D extends AnimationSetControl {

  public AnimationSetControlJ2D(DisplayImpl d, AnimationControl p) {
    super(d, p);
  }

  public void setSet(Set s) throws VisADException, RemoteException {
    super.setSet(s);
    // XXX rebuild BufferedImage[] array in DisplayRendererJ2D
  }

}

