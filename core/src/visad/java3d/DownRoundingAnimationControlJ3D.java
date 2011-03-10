//
// DownRoundingAnimationControlJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;

import visad.*;

import javax.media.j3d.*;

import java.util.Vector;
import java.util.Enumeration;

import java.rmi.*;

/**
   DownRoundingAnimationControlJ3D extends AnimationControlJ3D to provide a
   different sampling behavior. Instead of nearest neighbor, the nearest sample
   LESS THAN the current value will be displayed.<P>
   Doug Lindholm (DML) - Dec 2001
*/
public class DownRoundingAnimationControlJ3D extends AnimationControlJ3D
       implements Runnable, AnimationControl {

  public DownRoundingAnimationControlJ3D(DisplayImplJ3D d, RealType r) {
    super(d, r);
  }

  DownRoundingAnimationControlJ3D() {
    this(null, null);
  }

  //override to get down rounding behavior
  public void setCurrent( int c ) throws VisADException, RemoteException {
    Set set = getSet();

    if ( set == null ) {
      current = -1;
    }
    else {
      int n = set.getLength();
      if ( c >= n ) c = n-1;
      current = c;
    }

    init();
    changeControl(false);
  }

  //override to get down rounding behavior
  public void setCurrent( double value ) throws VisADException, RemoteException {
    Set set = getSet();
    current = getIndexLessThanValue( set, value );
    init();
    changeControl(false);
  }

  /**
   * Return the index of the sample with the nearest value less than or equal
   * to the given value, -1 if no earlier samples.
   */
  protected int getIndexLessThanValue( Set set, double value )
         throws VisADException {
    int index = -1;
    if ( set != null ) {
      double[][] values = set.getDoubles();
      int n = values[0].length;
      for ( int i=0; i<n; i++ ) {
        if ( values[0][i] > value ) break;//gone too far, stick with previous
        index = i;
      }
    }

    return index;
  }

  //override - superclass will clip current - add support for current = -1
  public void init() throws VisADException {
    Set set = getSet();
    if ( set != null ) {
      double value = Double.NaN;
      if ( current != -1 ) {
        value = (set.indexToDouble( new int[] {current} ))[0][0];
      }

      RealType real = getRealType();
      animation_string(real, set, value, current);
      selectSwitches(value, set);
    }
  }

  //overrides AVControlJ3D to get down rounding behavior
  // if value is NaN, show nothing
  public void selectSwitches(double value, Set animation_set)
       throws VisADException {

    double[][] fvalues = new double[1][1];
    fvalues[0][0] = value;
    Enumeration pairs = ((Vector) getSwitches().clone()).elements();
    while (pairs.hasMoreElements()) {
      SwitchSet ss = (SwitchSet) pairs.nextElement();

      if ( value != value ) { //if value is NaN
        ss.swit.setWhichChild( Switch.CHILD_NONE );
        continue;
      }

      Set set = ss.set;
      double[][] values = null;
      RealTupleType out = ((SetType) set.getType()).getDomain();
      if (animation_set != null) {
        RealTupleType in =
          ((SetType) animation_set.getType()).getDomain();
        values = CoordinateSystem.transformCoordinates(
                             out, set.getCoordinateSystem(),
                             set.getSetUnits(), null /* errors */,
                             in, animation_set.getCoordinateSystem(),
                             animation_set.getSetUnits(),
                             null /* errors */, fvalues);
      }
      else {
        // use RealType for value Unit and CoordinateSystem
        // for SelectValue
        values = CoordinateSystem.transformCoordinates(
                             out, set.getCoordinateSystem(),
                             set.getSetUnits(), null /* errors */,
                             out, out.getCoordinateSystem(),
                             out.getDefaultUnits(), null /* errors */,
                             fvalues);
      }

      int index = getIndexLessThanValue( set, values[0][0] );

      int numc = ss.swit.numChildren();
      if ( index >= numc ) index = numc-1;

      if ( index == -1 ) ss.swit.setWhichChild( Switch.CHILD_NONE );
      else ss.swit.setWhichChild( index );

    } // end while (pairs.hasMoreElements())
  }

}
