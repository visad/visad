 
//
// VisADGroup.java
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

}

