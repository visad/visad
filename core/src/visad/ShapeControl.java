//
// ShapeControl.java
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

package visad;

import java.rmi.*;

import visad.util.Util;

/**
   ShapeControl is the VisAD class for controlling Shape display scalars.<P>
*/
public class ShapeControl extends Control {

  private SimpleSet shapeSet = null;
  private VisADGeometryArray[] shapes = null;

  // WLH 31 May 2000
  private float scale = 1.0f;

  // WLH 6 Aug 2001
  private boolean autoScale = false;
  private ProjectionControlListener pcl = null;

  public ShapeControl(DisplayImpl d) {
    super(d);
  }

  /**
   * Sets the SimpleSet that defines the mapping from RealType values to indices
   * into an array of shapes.  This method <em>must be called before</em> the
   * setShapes() method to avoid a subsequent NullPointerException in the
   * running of the display.
   *
   * @param set			1-D set of values that defines the mapping from
   *				RealType values to indices into the array of
   *				shapes.  The domain dimension shall be 1.
   */
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

  /**
   * Sets the array of shapes.  The array is accessed by index determined from
   * the value-to-index mapping established by the <code>setShapeSet()</code>
   * method.  The VisADGeometryArray class hierarchy defines various kinds of
   * shapes.  This method <em>must be called after</em> the setShapeSet()
   * method to avoid a subsequent NullPointerException in the running of the
   * display.
   *
   * @param shs			The array of shapes.
   */
  public synchronized void setShapes(VisADGeometryArray[] shs)
         throws VisADException, RemoteException {
    if (shapeSet == null) return;
    if (shs != null && shs.length > 0) {
      final int len = Math.min(shs.length, shapes.length);
      for (int i=0; i<len; i++) {
        shapes[i] = shs[i];
      }
      if (shapes.length > shs.length) {
        for (int i=shs.length; i < shapes.length; i++) {
          shapes[i] = null;
        }
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
        if (0 <= indices[i] && indices[i] < shapes.length &&
            shapes[indices[i]] != null)
        {
          sh[i] = (VisADGeometryArray) shapes[indices[i]].clone();
        }
        else {
          sh[i] = null;
        }
      }
    }
    return sh;
  }

  // WLH 31 May 2000
  public void setScale(float s)
         throws VisADException, RemoteException {
    if (s == s) {
      scale = s;
      changeControl(true);
    }
  }

  // WLH 31 May 2000
  public float getScale() {
    return scale;
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

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return null;
  }

  /** reconstruct this control using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    throw new UnimplementedException(
      "Cannot setSaveString on this type of control");
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof ShapeControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
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
      shapes = sc.shapes;
    }

    // WLH 31 May 2000
    if (!Util.isApproximatelyEqual(scale, sc.scale)) {
      changed = true;
      scale = sc.scale;
    }

    // WLH 6 Aug 2001
    if (autoScale != sc.autoScale) {
      // changed = true;
      setAutoScale(sc.autoScale);
    }

    if (changed) {
      try {
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
  }

  public void setAutoScale(boolean auto)
         throws VisADException {
    if (auto == autoScale) return;
    DisplayImpl display = getDisplay();
    DisplayRenderer dr = display.getDisplayRenderer();
    MouseBehavior mouse = dr.getMouseBehavior();
    ProjectionControl pc = display.getProjectionControl();
    if (auto) {
      pcl = new ProjectionControlListener(mouse, this, pc);
      pc.addControlListener(pcl);
    }
    else {
      pc.removeControlListener(pcl);
    }
    autoScale = auto;
    try {
      changeControl(true);
    }
    catch (RemoteException e) {
    }
  }

  public void nullControl() {
    try {
      setAutoScale(false);
    }
    catch (VisADException e) {
    }
    super.nullControl();
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    ShapeControl sc = (ShapeControl )o;

    if (!shapeSetEquals(sc.shapeSet) || !shapesEquals(sc.shapes)) {
      return false;
    }

    // WLH 31 May 2000
    if (!Util.isApproximatelyEqual(scale, sc.scale)) {
      return false;
    }

    return true;
  }

  public Object clone()
  {
    ShapeControl sc = (ShapeControl )super.clone();
    if (shapes != null) {
      sc.shapes = (VisADGeometryArray[] )shapes.clone();
    }
    sc.scale = scale;
    return sc;
  }


  class ProjectionControlListener implements ControlListener {
    private boolean pfirst = true;
    private MouseBehavior mouse;
    private ProjectionControl pcontrol;
    private ShapeControl shapeControl;
    private double base_scale = 1.0;
    private float last_cscale = 1.0f;
    private double base_size = 1.0;

    ProjectionControlListener(MouseBehavior m, ShapeControl s,
                              ProjectionControl p) {
      mouse = m;
      shapeControl = s;
      pcontrol = p;
    }

    public void controlChanged(ControlEvent e)
           throws VisADException, RemoteException {
      double[] matrix = pcontrol.getMatrix();
      double[] rot = new double[3];
      double[] scale = new double[3];
      double[] trans = new double[3];
      mouse.instance_unmake_matrix(rot, scale, trans, matrix);

      if (pfirst) {
        pfirst = false;
        base_scale = scale[0];
        last_cscale = 1.0f;
        base_size = shapeControl.getScale();
      }
      else {
        float cscale = (float) (base_scale / scale[0]);
        float ratio = cscale / last_cscale;
        if (ratio < 0.95f || 1.05f < ratio) {
          last_cscale = cscale;
          shapeControl.setScale((float) base_size * cscale);
        }
      }
    }
  }
}
