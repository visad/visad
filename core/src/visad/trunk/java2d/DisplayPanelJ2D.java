
//
// DisplayPanelJ3D.java
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
 
// GUI handling
import java.awt.swing.*;
import java.awt.swing.border.*;
// import com.sun.java.swing.*;
// import com.sun.java.swing.border.*;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.media.j3d.*;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

public class DisplayPanelJ3D extends JPanel {

  private DisplayImplJ3D display;
  private DisplayRendererJ3D renderer;

  public DisplayPanelJ3D(DisplayImplJ3D d) {
    display = d;
    renderer = (DisplayRendererJ3D) display.getDisplayRenderer();
    setLayout(new BorderLayout());
    Canvas3D canvas = new VisADCanvasJ3D(renderer, this);
    add("Center", canvas);
 
    UniverseBuilderJ3D universe = new UniverseBuilderJ3D(canvas);
    BranchGroup scene = renderer.createSceneGraph(universe.view, canvas); // J3D
    universe.addBranchGraph(scene);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setAlignmentX(LEFT_ALIGNMENT);

  }

}

