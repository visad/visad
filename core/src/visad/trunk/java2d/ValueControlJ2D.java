//
// ValueControlJ2D.java
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

package visad.java2d;

import visad.*;

import java.rmi.*;

/**
   ValueControlJ2D is the VisAD class for controlling SelectValue
   display scalars under Java2D.<P>
*/
public class ValueControlJ2D extends AVControlJ2D
       implements ValueControl {

  private double Value;

  private VisADCanvasJ2D canvas;

  public ValueControlJ2D(DisplayImplJ2D d) {
    super(d);
    Value = 0.0;
    if (d != null) {
      canvas = ((DisplayRendererJ2D) d.getDisplayRenderer()).getCanvas();
    }
  }
 
  public void setValue(double value)
         throws VisADException, RemoteException {
    Value = value;
    selectSwitches(Value, null);
    canvas.scratchImages();
    changeControl(true);
  }

  public void init() throws VisADException {
    selectSwitches(Value, null);
  }

  public double getValue() {
    return Value;
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
        throws RemoteException, VisADException
  {
    if (rmt == null) {
      throw new RemoteException("Cannot synchronize " + getClass().getName() +
                                " with null Control object");
    }

    if (!(rmt instanceof ValueControl)) {
      throw new RemoteException("Cannot synchronize " + getClass().getName() +
                                " with " + rmt.getClass().getName());
    }

    ValueControl vc = (ValueControl )rmt;

    boolean changed = false;

    double v = getValue();
    double rv = vc.getValue();
    if (Math.abs(v - rv) > 0.001) {
      setValue(rv);
    }
  }

  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof ValueControlJ2D)) {
      return false;
    }

    ValueControlJ2D vc = (ValueControlJ2D )o;

    double v = getValue();
    double rv = vc.getValue();
    if (Math.abs(v - rv) > 0.001) {
      return false;
    }

    return super.equals(o);
  }
}
