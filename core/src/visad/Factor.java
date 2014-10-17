//
// Factor.java
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

import java.io.Serializable;

/**
 * A class that represents a factor in the dimensionality of a unit.
 * A factor is a base unit and a power.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
final class Factor
    implements Serializable
{
    /**
     * The power of the base unit.
     */
    final int           power;

    /**
     * The base unit.
     */
    final BaseUnit      baseUnit;

    private transient int hashCode = 0;

    /**
     * Construct a factor from a base unit and a power.
     *
     * @param baseUnit  The base unit.
     * @param power     The power to raise the base unit by.
     */
    Factor(BaseUnit baseUnit, int power)
    {
        this.power = power;
        this.baseUnit = baseUnit;
    }

    /**
     * Return a string representation of this factor.
     *
     * @return  A string representation of this factor (e.g. "m-2").
     */
    public String toString()
    {
        return power == 1
                ? baseUnit.toString()
                : baseUnit.toString() + power;
    }

  public boolean equals(Factor factor) {
    return baseUnit.equals(factor.baseUnit) &&
           (power == factor.power);
  }

  /**
   * Indicates whether or not this instance is equal to an object.
   *
   * @param that               The object in question.
   * @return                  <code>true</code> if and only if this instance
   *                          equals the unit.
   */
  public boolean equals (Object that) {
    if (this == that)
        return true;
    if (!(that instanceof Factor)) {
      return false;
    }
    return equals ((Factor)that);
  }

  /**
   * Returns the hash code of this instance. {@link Object#hashCode()} should be
   * overridden whenever {@link Object#equals(Object)} is.
   * @return                    The hash code of this instance (includes the
   *                            values).
   */
  public int hashCode()
  {
    if (hashCode == 0)
    {
      hashCode = baseUnit.hashCode() ^ Integer.valueOf(power).hashCode();
    }
    return hashCode;
  }

}

