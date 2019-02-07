//
// MouseBehavior.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

  /**
   * Get the helper class used by this MouseBehavior.  
   * The <CODE>MouseHelper</CODE> defines the actions taken based
   * on <CODE>MouseEvent</CODE>s.
   * @return  <CODE>MouseHelper</CODE> being used.
   */
  MouseHelper getMouseHelper();

  /**
   * Return the VisAD ray corresponding to the VisAD cursor coordinates.
   * @param  cursor  array (x,y,z) of cursor location
   * @return  corresponding VisADRay
   * @see visad.VisADRay
   * @see visad.DisplayRenderer#getCursor()
   */
  VisADRay cursorRay(double[] cursor);

  /**
   * Return the VisAD ray corresponding to the component coordinates.
   * @param  screen_x  x coordinate of the component
   * @param  screen_y  y coordinate of the component
   * @return  corresponding VisADRay
   * @see visad.VisADRay
   * @see visad.LocalDisplay#getComponent()
   */
  VisADRay findRay(int screen_x, int screen_y);

  /**
   * Return the screen coordinates corresponding to the VisAD coordinates.
   * @param  position  array of VisAD coordinates
   * @return  corresponding (x, y) screen coordinates
   */
  int[] getScreenCoords(double[] position);

  /**
   * Multiply the two matrices together.
   * @param  a  first matrix
   * @param  b  second matrix
   * @return  new resulting matrix
   */
  double[] multiply_matrix(double[] a, double[] b);

  /**
   * Make a transformation matrix to perform the given rotation, scale and
   * translation.  
   * @param rotx  x rotation
   * @param roty  y rotation
   * @param rotz  z rotation
   * @param scale  scaling factor
   * @param transx  x translation
   * @param transy  y translation
   * @param transz  z translation
   * @return  new matrix
   */
  double[] make_matrix(double rotx, double roty,
      double rotz, double scale, double transx, double transy, double transz);

  /**
   * Make a transformation matrix to perform the given rotation, scale and
   * translation.  
   * @param rotx  x rotation
   * @param roty  y rotation
   * @param rotz  z rotation
   * @param scalex  x scaling factor
   * @param scaley  y scaling factor
   * @param scalez  z scaling factor
   * @param transx  x translation
   * @param transy  y translation
   * @param transz  z translation
   * @return  new matrix
   */
  double[] make_matrix(double rotx, double roty,
      double rotz, double scalex, double scaley, double scalez, double transx, double transy, double transz);

  /**
   * Get the rotation, scale and translation parameters for the specified
   * matrix.  Results are not valid for non-uniform aspect (scale).
   * @param  rot  array to hold x,y,z rotation values
   * @param  scale  array to hold scale value(s). If length == 1, assumes
   *                uniform scaling.
   * @param  trans  array to hold x,y,z translation values
   */
  void instance_unmake_matrix(double[] rot, double[] scale,
                              double[] trans, double[] matrix);

  /**
   * Create a translation matrix.  no translation in Z direction (useful
   * for 2D)
   * @param  transx   x translation amount
   * @param  transy   y translation amount
   * @return  new translation matrix.  This can be used to translate
   *          the current matrix
   * @see #multiply_matrix(double[] a, double[] b)
   */
  double[] make_translate(double transx, double transy);

  /**
   * Create a translation matrix.
   * @param  transx   x translation amount
   * @param  transy   y translation amount
   * @param  transz   z translation amount
   * @return  new translation matrix.  This can be used to translate
   *          the current matrix
   * @see #multiply_matrix(double[] a, double[] b)
   */
  double[] make_translate(double transx, double transy, double transz);

}

