
//
// ShapeControl.java
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

/**
   ShapeControl is the VisAD class for controlling Shape display scalars.<P>
*/
public class ShapeControl extends Control {

  private SimpleSet shapeSet;
  private VisADGeometryArray[] shapes;

  public ShapeControl(DisplayImpl d) {
    super(d);
  }

  public void setShapeSet(SimpleSet set)
         throws VisADException {
    if (set.getDimension() != 1) {
      throw new DisplayException("ShapeControl.setShapeSet: domain " +
                                 "dimension must be 1");
    }
    shapeSet = set;
    shapes = new VisADGeometryArray[shapeSet.getLength()];
  }

  public void setShape(int index, VisADGeometryArray shape) {
    if (0 <= index && index < shapes.length) {
      shapes[index] = shape;
    }
  }
 
  public VisADGeometryArray[] getShapes(float[] values)
         throws VisADException {
    if (values == null) return null;
    VisADGeometryArray[] sh = new VisADGeometryArray[values.length];
    if (shapeSet == null) return sh;
    float[][] set_values = new float[1][];
    set_values[0] = values;
    int[] indices = shapeSet.valueToIndex(set_values);
    for (int i=0; i<indices.length; i++) {
      if (0 <= indices[i] && indices[i] < shapes.length) {
        sh[i] = shapes[indices[i]];
      }
      else {
        sh[i] = null;
      }
    }
    return sh;
  }

}

