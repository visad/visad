//
// MouseBehavior.java
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
 
/**
   MouseBehavior is the VisAD interface for mouse behaviors
   for Java3D and Java2D
*/

public interface MouseBehavior {

  public abstract MouseHelper getMouseHelper();

  public abstract VisADRay cursorRay(double[] cursor);

  public abstract VisADRay findRay(int screen_x, int screen_y);

  public abstract double[] multiply_matrix(double[] a, double[] b);

  public abstract double[] make_matrix(double rotx, double roty,
         double rotz, double scale, double transx, double transy, double transz);

  public abstract double[] make_translate(double transx, double transy);

}

