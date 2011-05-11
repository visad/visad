//
// ScreenAnnotatorJ3D.java
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

package visad.bom.annotations;

import visad.DisplayImpl;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;
import visad.VisADException;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;

import java.util.Iterator;

/**
 * Take descriptions of geometry primatives in Screen Coordinates
 * and draw them onto a VisAD display directly. The objects should then
 * stay on the same screen position.
 *
 * Enables thing like scale bars, tables, logos, etc to be independent
 * of data manipulation tranformations.
 */
public class ScreenAnnotatorJ3D extends ScreenAnnotator
{
  private DisplayImplJ3D display;
  private DisplayRendererJ3D renderer;
  private BranchGroup group;

  /**
   *  Construct a ScreenAnnotatorJ3D for the given {@link DisplayImpl}.
   *
   *  @param display  the VisAD Display for the ScreenAnnotatorJ3D.
   */
  public ScreenAnnotatorJ3D(DisplayImpl display)
  {
    this.display = (DisplayImplJ3D)display;
    renderer = (DisplayRendererJ3D)display.getDisplayRenderer();
    group = new BranchGroup();
    // What set of capabilities is really required?
    group.setCapability(BranchGroup.ALLOW_DETACH);
    group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
  }

  /**
   *  Set the visibility flag.
   *
   *  @param on  <code>true</code> to display, <code>false</code> to undisplay.
   */
  public void makeVisible(boolean on)
  {
    if (on) { // check if already attached - or is Live?
      if (!group.isLive()) {
        renderer.getRoot().addChild(group);
      }
    } else {
      if (group.isLive()) {
        group.detach();
      }
    }
  } // makeVisible

  /**
   *  Traverses all the data objects and transforms them into
   *  {@link Shape3D} objects and adds them to a BranchGoup which it 
   *  then attaches to the scene graph.
   *
   *  @throws VisADException if {@link LabelJ3D} throws the exception.
   */
  public void draw()
    throws VisADException
  {
    synchronized (group) {
      Node node = null;
      Iterator it = null;
      makeVisible(false);
      // removeAllChildren() not in my version of java3d ?
      for (int i=group.numChildren()-1; i>=0; i--) {
        group.removeChild(i);
      }
  
      ScreenAnnotation sa;
      if (things != null) {
        it = things.iterator();
        while (it.hasNext()) {
          sa = (ScreenAnnotation)it.next();
          node = (Node)sa.toDrawable(display);
          group.addChild(node);
        }
      }
  
      renderer.getRoot().addChild(group);
      makeVisible(true);
    }
  } // draw()
} // class ScreenAnnotatorJ3D
