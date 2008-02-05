//
// VisADGroup.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Vector;

/**
   VisADGroup stands in for j3d.Group
   and is Serializable.<P>
*/
public class VisADGroup extends VisADSceneGraphObject {

  private Vector children = new Vector();

  public VisADGroup() {
  }

  public synchronized void addChild(VisADSceneGraphObject child)
         throws DisplayException {
    if (child == null) return;
    synchronized(child) {
      if (child.parent != null) {
        throw new DisplayException("VisADGroup.addChild: already has parent");
      }
      children.addElement(child);
      child.parent = this;
    }
  }

  public synchronized int numChildren() {
    return children.size();
  }

  public synchronized VisADSceneGraphObject getChild(int index) {
    return (VisADSceneGraphObject) children.elementAt(index);
  }

  public synchronized void setChild(VisADSceneGraphObject child, int index)
         throws DisplayException {
    if (child == null) return;
    synchronized(child) {
      if (child.parent != null) {
        throw new DisplayException("VisADGroup.setChild: already has parent");
      }
      VisADSceneGraphObject c = null;
      if (children.size() > index) {
        c = (VisADSceneGraphObject) children.elementAt(index);
        if (c != null) {
          synchronized(c) {
            // nested VisADSceneGraphObject synchronized cannot
            // deadlock: parent-less first, with-parent second
            c.parent = null;
            children.setElementAt(child, index);
          }
        }
        children.setElementAt(child, index);
      }
      else {
        children.addElement(child);
      }
      child.parent = this;
    }
  }

  public synchronized void removeChild(int index) {
    VisADSceneGraphObject c =
      (VisADSceneGraphObject) children.elementAt(index);
    if (c != null) {
      synchronized(c) {
        c.parent = null;
        children.removeElementAt(index);
      }
    }
  }

  void detachChild(VisADSceneGraphObject child) {
    children.removeElement(child);
  }

  public synchronized Vector getChildren() {
    return (Vector) children.clone();
  }

  public String toString() {
    String s = getClass().getName() + " ";
    synchronized (children) {
      int n = children.size();
      s = s + n + " ";
      for (int i=0; i<n; i++) {
        s = s + children.elementAt(i).toString();
      }
    }
    return s;
  }

}

