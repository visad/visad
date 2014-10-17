//
// DisplayPanelJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

// GUI handling
import javax.swing.*;

public class DisplayPanelJ2D extends JPanel {

  private DisplayImplJ2D display;
  private DisplayRendererJ2D renderer;

  public DisplayPanelJ2D(DisplayImplJ2D d)
         throws VisADException {
    display = d;
    renderer = (DisplayRendererJ2D) display.getDisplayRenderer();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
/* WLH 19 April 99
    setAlignmentY(TOP_ALIGNMENT);
    setAlignmentX(LEFT_ALIGNMENT);
*/
    VisADCanvasJ2D canvas = new VisADCanvasJ2D(renderer, this);
    add(canvas);
    VisADGroup scene = renderer.createSceneGraph(canvas);

    setPreferredSize(new java.awt.Dimension(256, 256));
    setMinimumSize(new java.awt.Dimension(0, 0));
  }

}

