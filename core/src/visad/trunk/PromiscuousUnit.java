//
// PromiscuousUnit.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
   PromiscuousUnit is the VisAD class for units
   that are convertable with any other Unit.<P>
*/
public class PromiscuousUnit extends Unit {

  static final Unit promiscuous = new PromiscuousUnit();

  private PromiscuousUnit()
  {
    super("UniversalUnit");
  }

  /**
   * <p>Indicates if this instance is dimensionless.  A unit is dimensionless
   * if it is a measure of a dimensionless quantity like angle or 
   * concentration.  Examples of dimensionless units include radian, degree,
   * steradian, and "g/kg".</p>
   *
   * <p>This implementation always returns <code>false</code> because the
   * typical use of this method is to determine whether or not a
   * function of dimensionless values (e.g. sin(), log()) may be
   * applied to values in this unit and such functions shouldn't be applied
   * to values in this unit.  Instead, the client should ensure that the
   * values are in a true, dimensionless unit.</p>
   *
   * @return                               True if an only if this unit is
   *                                       dimensionless.
   */
  public boolean isDimensionless() {
    return false;
  }

  /**
   * Clones this unit, changing the identifier.  This method always throws
   * an exception because promiscuous units may not be cloned.
   * @param identifier          The name or abbreviation for the cloned unit.
   *                            May be <code>null</code> or empty.
   * @throws UnitException      Promiscuous units may not be cloned.  Always
   *                            thrown.
   */
  protected Unit protectedClone(String identifier)
    throws UnitException
  {
    throw new UnitException("Promiscuous units may not be cloned");
  }

  /**
   * Returns the definition of this unit.  For promiscuous units, this is the
   * same as the identifier.
   * @return            The definition of this unit.  Won't be <code>null
   *                    </code> but may be empty.
   */
  public String getDefinition()
  {
    return getIdentifier();
  }

  public Unit pow(int power) {
    return this;
  }

  public Unit root(int root) {
    return this;
  }

  public Unit pow(double power) {
    return this;
  }

  public Unit multiply(Unit that) {
    return that;
  }

  public Unit divide(Unit that)
        throws UnitException {
    return CommonUnit.dimensionless.divide(that);
  }

  public Unit divide(PromiscuousUnit that) {
    return that;
  }

  protected Unit divideInto(Unit that)
       throws UnitException {
    return that;
  }


  public double[] toThis(double[] values, Unit that) {
    return values;
  }

  public double[] toThat(double[] values, Unit that) {
    return values;
  }

  public float[] toThis(float[] values, Unit that) {
    return values;
  }

  public float[] toThat(float[] values, Unit that) {
    return values;
  }

  /**
   * Indicate whether this unit is convertible with another unit.  A
   * PromiscuousUnit is always convertible with another unit.
   *
   * @param unit        The other unit.
   * @return            True, always.
   */
  public boolean isConvertible(Unit unit)
  {
    return true;
  }

  public boolean equals(Unit unit) {
    return (unit instanceof PromiscuousUnit);
  }

  /**
   * Returns the hash code of this instance. {@link Object#hashCode()} should be
   * overridden whenever {@link Object#equals(Object)} is.
   * @return                    The hash code of this instance (includes the
   *                            values).
   */
  public int hashCode()
  {
    if (!hashCodeSet)
    {
      hashCode ^= System.identityHashCode(promiscuous);
      hashCodeSet = true;
    }
    return hashCode;
  }
}
