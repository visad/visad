
//
// VisADCanvasJ3D.java
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
 
package visad.java3d;
 
import visad.*;
 
import javax.media.j3d.*;

import java.awt.*;
 
/**
   VisADCanvasJ3D is the VisAD extension of Canvas3D
*/

public class VisADCanvasJ3D extends Canvas3D { // J3D

  private DisplayRendererJ3D displayRenderer;
  private Component component;
  Dimension prefSize = new Dimension(0, 0);

  VisADCanvasJ3D(DisplayRendererJ3D renderer, Component c) {
    super(null);
    displayRenderer = renderer;
    component = c;
  }

  public void renderField(int i) {
    displayRenderer.drawCursorStringVector(this);
  }

  public Dimension getPreferredSize() {
    return prefSize;
  }

}

