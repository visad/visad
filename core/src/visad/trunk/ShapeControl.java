//
// ShapeControl.java
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

package visad;

import java.rmi.*;

/**
   ShapeControl is the VisAD class for controlling Shape display scalars.<P>
*/
public class ShapeControl extends Control {

  private SimpleSet shapeSet = null;
  private VisADGeometryArray[] shapes = null;

  public ShapeControl(DisplayImpl d) {
    super(d);
  }

  /** set the SimpleSet that defines the mapping from RealType
      values to indices into an array of shapes;
      the domain dimension of set must be 1 */
  public synchronized void setShapeSet(SimpleSet set)
         throws VisADException, RemoteException {
    if (set == null) {
      shapeSet = null;
      shapes = null;
      return;
    }
    if (set.getDimension() != 1) {
      throw new DisplayException("ShapeControl.setShapeSet: domain " +
                                 "dimension must be 1");
    }
    shapeSet = set;
    shapes = new VisADGeometryArray[shapeSet.getLength()];
    changeControl(true);
  }

  /** set the shape associated with index;
      the VisADGeometryArray class hierarchy defines various
      kinds of shapes */
  public synchronized void setShape(int index, VisADGeometryArray shape)
         throws VisADException, RemoteException {
    if (shapes == null) return;
    if (0 <= index && index < shapes.length) {
      shapes[index] = shape;
    }
    changeControl(true);
  }
 
  /** set the array of shapes associated with indices 0
      through shapes.length; the VisADGeometryArray class
      hierarchy defines various kinds of shapes */
  public synchronized void setShapes(VisADGeometryArray[] shs)
         throws VisADException, RemoteException {
    if (shapeSet == null) return;
    if (shs != null && shs.length > 0) {
      int len = Math.max(shs.length, shapes.length);
      for (int i=0; i<len; i++) {
        shapes[i] = shs[i];
      }
    }
    changeControl(true);
  }

  public synchronized VisADGeometryArray[] getShapes(float[] values)
         throws VisADException {
    if (values == null || values.length < 1) return null;
    VisADGeometryArray[] sh = new VisADGeometryArray[values.length];
    if (shapeSet == null) return sh;
    float[][] set_values = new float[1][];
    set_values[0] = values;
    int[] indices = null;
    if (shapeSet.getLength() < 2) {
      indices = new int[values.length];
      for (int i=0; i<indices.length; i++) indices[i] = 0;
    }
    else {
      indices = shapeSet.valueToIndex(set_values);
    }
    if (shapes == null) {
      for (int i=0; i<indices.length; i++) sh[i] = null;
    }
    else {
      for (int i=0; i<indices.length; i++) {
        if (0 <= indices[i] && indices[i] < shapes.length) {
          sh[i] = (VisADGeometryArray) shapes[indices[i]].clone();
        }
        else {
          sh[i] = null;
        }
      }
    }
    return sh;
  }

  private boolean shapeSetEquals(SimpleSet newShapeSet)
  {
    if (shapeSet == null) {
      if (newShapeSet != null) {
        return false;
      }
    } else if (newShapeSet == null) {
      return false;
    } else if (!shapeSet.equals(newShapeSet)) {
      return false;
    }

    return true;
  }

  private boolean shapesEquals(VisADGeometryArray[] newShapes)
  {
    if (shapes == null) {
      if (newShapes != null) {
        return false;
      }
    } else if (newShapes == null) {
      return false;
    } else {
      if (shapes.length != newShapes.length) {
        return false;
      }

      for (int i = 0; i < shapes.length; i++) {
        if (shapes[i] == null) {
          if (newShapes[i] != null) {
            return false;
          }
        } else if (newShapes[i] == null) {
          return false;
        } else if (!shapes[i].equals(newShapes[i])) {
          return false;
        }
      }
    }

    return true;
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws RemoteException, VisADException
  {
    if (rmt == null) {
      throw new RemoteException("Cannot synchronize " + getClass().getName() +
                                " with null Control object");
    }

    if (!(rmt instanceof ShapeControl)) {
      throw new RemoteException("Cannot synchronize " + getClass().getName() +
                                " with " + rmt.getClass().getName());
    }

    ShapeControl sc = (ShapeControl )rmt;

    boolean changed = false;

    if (!shapeSetEquals(sc.shapeSet)) {
      changed = true;
      shapeSet = sc.shapeSet;
    }

    if (!shapesEquals(sc.shapes)) {
      changed = true;
      shapes = null;
    }

    if (changed) {
      changeControl(true);
    }
  }

  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof ShapeControl)) {
      return false;
    }

    ShapeControl sc = (ShapeControl )o;

    return shapeSetEquals(sc.shapeSet) && shapesEquals(sc.shapes);
  }

}

