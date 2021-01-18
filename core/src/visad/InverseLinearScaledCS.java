//
// InverseLinearScaledCS.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

/**
   An interface which CoordinateSystems can implement to indicate that 
   they invert another CS, but the transforms linearly scale the CS being
   inverted.  For example, this.toReference linearly scales CS.fromReference
   according to coeffs returned by the getScale/Offset methods. 
   This could be used by a DataRenderer when the Display 
   CoordinateSystem: (Lon,Lat->Display.X,Y) inverts a Data CoordinateSystem:
   (line,elem -> Lon, Lat).
*/


public interface InverseLinearScaledCS {

  /**
   * The linear scale/offset coefficients
   */
  public double[] getScale();

  public double[] getOffset();

  /**
   *  The CoordinateSystem being inverted
   *
   */
  public CoordinateSystem getInvertedCoordinateSystem();
}
