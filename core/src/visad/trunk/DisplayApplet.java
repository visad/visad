
//
// DisplayApplet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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
 
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.media.j3d.*;
import java.vecmath.*;

import java.awt.*;

public class DisplayApplet extends Applet {

  private DisplayImpl display;
  private DisplayRenderer renderer;

  public DisplayApplet(DisplayImpl d) {
    display = d;
    renderer = display.getDisplayRenderer();
    setLayout(new BorderLayout());
    Canvas3D canvas = new Canvas3D(null);
    add("Center", canvas);
 
    BranchGroup scene = renderer.createSceneGraph();
    UniverseBuilder universe = new UniverseBuilder(canvas);
    universe.addBranchGraph(scene);
  }

}

