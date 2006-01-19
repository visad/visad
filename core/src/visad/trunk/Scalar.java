//
// Scalar.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
 * Scalar is the superclass of the VisAD hierarchy of scalar data.<P>
 */
public abstract class Scalar
  extends	DataImpl
  implements	ScalarIface, Comparable
{

  public Scalar(ScalarType type) {
    super(type);
  }

  /**
   * Adds a listener for changes to this instance.  Because instances of this
   * class don't change, this method does nothing.
   *
   * @param listener                     The listener for changes.
   */
  public final void addReference(ThingReference listener) {
  }

  /**
   * Removes a listener for changes to this instance.  Because instances of this
   * class don't change, this method does nothing.
   *
   * @param listener                    The change listener to be removed.
   */
  public final void removeReference(ThingReference listener) {
  }

  /**
   * Indicates if this scalar is semantically identical to an object.
   * @param obj			The object.
   * @return			<code>true</code> if and only if this scalar
   *				is semantically identical to the object.
   */
  public abstract boolean equals(Object obj);

  /**
   * Clones this instance.
   *
   * @return                      A clone of this instance.
   */
  public final Object clone() {
      /*
       * Steve Emmerson believes that this implementation should return
       * "this" to reduce resouce-usage but Bill believes that doing so is
       * counter-intuitive and might harm applications.
       */
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException("Assertion failure");
    }
  }
}

