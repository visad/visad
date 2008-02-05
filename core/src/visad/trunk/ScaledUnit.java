//
// ScaledUnit.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
 * A class that represents a certain amount of a derived unit.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class ScaledUnit
    extends     Unit
    implements  Serializable
{
    /**
     * The amount of the associated derived unit.
     */
    final double        amount;

    /**
     * The associated derived unit.
     */
    final DerivedUnit   derivedUnit;


    /**
     * Construct a dimensionless scaled unit.  The identifier will be empty.
     *
     * @param amount    The given amount of this unit.
     */
    public ScaledUnit(double amount)
    {
        this(amount, "");
    }

    /**
     * Construct a dimensionless scaled unit with an identifier.
     *
     * @param amount            The given amount of this unit.
     * @param identifier        Name or abbreviation for the unit.  May be
     *                          <code>null</code> or empty.
     */
    public ScaledUnit(double amount, String identifier)
    {
        super(identifier);
        this.amount = amount;
        derivedUnit = new DerivedUnit();
    }

    /**
     * Construct a scaled unit from a base unit.  The identifier will be
     * that of the base unit if the amount is 1; otherwise, the identifier
     * will be <code>null</code>.
     *
     * @param amount    The given amount of the base unit (e.g. 0.9144 to
     *                  create a yard unit if <code>unit</code> represents a
     *                  meter).
     * @param that      The given base unit.
     */
    public ScaledUnit(double amount, BaseUnit that)
    {
        this(amount, that, amount == 1 ? that.getIdentifier() : null);
    }

    /**
     * Construct a scaled unit from a base unit and an identifier.
     *
     * @param amount            The given amount of the base unit (e.g. 0.9144
     *                          to create a yard unit if <code>unit</code>
     *                          represents a meter).
     * @param that              The given base unit.
     * @param identifier        Name or abbreviation for the unit.  May be
     *                          <code>null</code> or empty.
     */
    public ScaledUnit(double amount, BaseUnit that, String identifier)
    {
        super(identifier);
        this.amount = amount;
        derivedUnit = new DerivedUnit(that);
    }

    /**
     * Construct a scaled unit from a derived unit.  The identifier will be
     * that of the derived unit if the amount is 1; otherwise, the identifier
     * will be <code>null</code>.
     *
     * @param amount    The given amount of the derived unit (e.g. 0.44704 to
     *                  create a mile/hour unit if <code>unit</code> represents
     *                  a meter/second.
     * @param that      The given derived unit.
     */
    public ScaledUnit(double amount, DerivedUnit that)
    {
        this(amount, that, amount == 1 ? that.getIdentifier() : null);
    }

    /**
     * Construct a scaled unit from a derived unit and an identifier.
     *
     * @param amount            The given amount of the derived unit
     *                          (e.g. 0.44704 to create a mile/hour unit if
     *                          <code>unit</code> represents a meter/second.
     * @param that              The given derived unit.
     * @param identifier        Name or abbreviation for the unit.  May be
     *                          <code>null</code> or empty.
     */
    public ScaledUnit(double amount, DerivedUnit that, String identifier)
    {
        super(identifier);
        this.amount = amount;
        derivedUnit = that;
    }

    /**
     * Construct a scaled unit from a scaled unit.  The identifier will be that
     * of the scaled unit if both amounts are 1; otherwise, the identifier will
     * be <code>null</code>.
     *
     * @param amount    The given amount of the scaled unit (e.g. 3.0 to
     *                  create a yard unit if <code>unit</code> represents
     *                  a foot.
     * @param unit      The given scaled unit.
     */
    public ScaledUnit(double amount, ScaledUnit unit)
    {
        this(amount, unit,
          amount == 1 && unit.amount == 1 ? unit.getIdentifier() : null);
    }

    /**
     * Construct a scaled unit from a scaled unit and an identifier.
     *
     * @param amount            The given amount of the scaled unit (e.g. 3.0
     *                          to create a yard unit if <code>unit</code>
     *                          represents a foot.
     * @param that              The given scaled unit.
     * @param identifier        Name or abbreviation for the unit.  May be
     *                          <code>null</code> or empty.
     */
    public ScaledUnit(double amount, ScaledUnit that, String identifier)
    {
        super(identifier);
        this.amount = amount*that.amount;
        derivedUnit = that.derivedUnit;
    }

    /**
     * Factory method for creating a scaled unit.  The identifier will be that
     * of the input unit if both amounts are 1; otherwise, the identifier will
     * be <code>null</code>.
     *
     * @param amount    The given amount of the scaled unit (e.g. 3.0 to
     *                  create a yard unit if <code>unit</code> represents
     *                  a foot.
     * @param unit      The given unit.
     * @return          A corresponding scaled unit.
     * @throws UnitException    Can't create Scaled Unit from <code>unit</code>.
     */
    public static ScaledUnit
    create(double amount, Unit unit)
       throws UnitException
    {
        ScaledUnit      result;
        if (unit instanceof BaseUnit)
          result = new ScaledUnit(amount, (BaseUnit)unit);
        else if (unit instanceof DerivedUnit)
          result = new ScaledUnit(amount, (DerivedUnit)unit);
        else if (unit instanceof ScaledUnit)
          result = new ScaledUnit(amount, (ScaledUnit)unit);
        else
          throw new UnitException("Can't create Scaled Unit from " + unit);
        return result;
    }

    /**
     * <p>Indicates if this instance is dimensionless.  A unit is dimensionless
     * if it is a measure of a dimensionless quantity like angle or 
     * concentration.  Examples of dimensionless units include radian, degree,
     * steradian, and "g/kg".</p>
     *
     * @return                  True if an only if this unit is dimensionless.
     */
    public boolean isDimensionless() {
      return derivedUnit.isDimensionless();
    }

    /**
     * Clones this unit, changing the identifier.
     *
     * @param identifier        The name or abbreviation for the cloned unit.
     *                          May be <code>null</code> or empty.
     * @return                  A unit equal to this instance but with the given
     *                          identifier.
     */
    protected Unit protectedClone(String identifier)
    {
        return new ScaledUnit(amount, derivedUnit, identifier);
    }

    /**
     * Raises this unit to a power.
     *
     * @param power     The power to raise this unit by.
     * @return          The unit resulting from raising this unit to
     *                  <code>power</code>.
     * @promise         This unit has not been modified.
     */
    public Unit pow(int power)
    {
        return new ScaledUnit(Math.pow(amount, power),
                              (DerivedUnit)derivedUnit.pow(power));
    }

    /**
     * Returns the N-th root of this unit.
     *
     * @param root      The root to take (e.g. 2 means square root).  May not
     *                  be zero.
     * @return          The unit corresponding to the <code>root</code>-th root
     *                  of this unit.
     * @throws IllegalArgumentException
     *                  The root value is zero or the resulting unit would have
     *                  a non-integral unit dimension.
     * @promise         This unit has not been modified.
     */
    public Unit root(int root)
        throws IllegalArgumentException
    {
        return new ScaledUnit(Math.pow(amount, 1./root),
                              (DerivedUnit)derivedUnit.root(root));
    }

    /**
     * Raises this unit to a power.
     *
     * @param power     The power to raise this unit by.  If this unit is
     *                  not dimensionless, then the value must be integral.
     * @return          The unit resulting from raising this unit to
     *                  <code>power</code>.
     * @throws IllegalArgumentException
     *                  This unit is not dimensionless and <code>power</code>
     *                  has a non-integral value.
     * @promise         The unit has not been modified.
     */
    public Unit pow(double power)
        throws IllegalArgumentException
    {
        return new ScaledUnit(Math.pow(amount, power),
                              (DerivedUnit)derivedUnit.pow(power));
    }

    /**
     * Returns the definition of this unit.
     *
     * @return          The definition of this unit (e.g. "0.9144 m" for a
     *                  yard).
     */
    public String getDefinition()
    {
        String  definition;
        if (derivedUnit == null)
        {
            /* Probably exception thrown during construction */
            definition = "<unconstructed ScaledUnit>";
        }
        else
        {
            String      derivedString = derivedUnit.toString();
            definition =
                amount == 1
                    ? derivedString
                    : derivedString.length() == 0
                        ? Double.toString(amount)
                        : Double.toString(amount) + " " + derivedString;
        }
        return definition;
    }

    /**
     * Multiplies this unit by another unit.
     *
     * @param that      The unit with which to multiply this unit.
     * @return          The product of the two units.
     * @promise         Neither unit has been modified.
     * @throws UnitException    Meaningless operation.
     */
    public Unit multiply(Unit that)
        throws UnitException
    {
        return create(amount, derivedUnit.multiply(that));
    }

    /**
     * Divides this unit by another unit.
     *
     * @param that      The unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise         Neither unit has been modified.
     * @throws UnitException    Meaningless operation.
     */
    public Unit divide(Unit that)
        throws UnitException
    {
        return create(amount, derivedUnit.divide(that));
    }

    /**
     * Divides this unit into another unit.
     *
     * @param that      The unit to be divided by this unit.
     * @return          The quotient of the two units.
     * @promise         Neither unit has been modified.
     * @throws UnitException    Meaningless operation.
     */
    protected Unit divideInto(Unit that)
        throws UnitException
    {
        return create(1./amount, derivedUnit.divideInto(that));
    }

    /**
     * Convert values to this unit from another unit.
     *
     * @param values    The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require         The units are convertible.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public double[] toThis(double[] values, Unit that)
        throws UnitException
    {
        return toThis(values, that, true);
    }

    /**
     * Convert values to this unit from another unit.
     *
     * @param values    The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require         The units are convertible.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public float[] toThis(float[] values, Unit that)
        throws UnitException
    {
        return toThis(values, that, true);
    }

    /**
     * Convert values to this unit from another unit.
     *
     * @param values    The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @param copy      if false and <code>that</code> equals this, 
     *                  return <code>values</code>, else return a new array
     * @return          The converted values in units of this unit.
     * @require         The units are convertible.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public double[] toThis(double[] values, Unit that, boolean copy)
        throws UnitException
    {
        double[] newValues;
        if (equals(that) || that instanceof PromiscuousUnit) {
            newValues = (copy) ? (double[])values.clone() : values;
        }
        else {
            newValues = that.toThat(values, derivedUnit, copy);
            for (int i = 0; i < newValues.length; ++i) {
                if (newValues[i] == newValues[i]) newValues[i] /= amount;
            }
        }
        return newValues;
    }

    /**
     * Convert values to this unit from another unit.
     *
     * @param values    The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @param copy      if false and <code>that</code> equals this, 
     *                  return <code>values</code>, else return a new array
     * @return          The converted values in units of this unit.
     * @require         The units are convertible.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public float[] toThis(float[] values, Unit that, boolean copy)
        throws UnitException
    {
        float[] newValues;
        if (equals(that) || that instanceof PromiscuousUnit) {
            newValues = (copy) ? (float[])values.clone() : values;
        }
        else {
            newValues = that.toThat(values, derivedUnit, copy);
            for (int i = 0; i < newValues.length; ++i) {
                if (newValues[i] == newValues[i]) newValues[i] /= amount;
            }
        }
        return newValues;
    }

    /**
     * Convert values from this unit to another unit.
     *
     * @param values    The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require         The units are identical.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public double[] toThat(double[] values, Unit that)
        throws UnitException
    {
        return toThat(values, that, true);
    }

    /**
     * Convert values from this unit to another unit.
     *
     * @param values    The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require         The units are identical.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public float[] toThat(float[] values, Unit that)
        throws UnitException
    {
        return toThat(values, that, true);
    }

    /**
     * Convert values from this unit to another unit.
     *
     * @param values    The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @param copy      if false and <code>that</code> equals this, 
     *                  return <code>values</code>, else return a new array
     * @return          The converted values.
     * @require         The units are convertible.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public double[] toThat(double values[], Unit that, boolean copy)
        throws UnitException
    {
        double[] newValues = (copy) ? (double[])values.clone() : values;
        if (!(equals(that) || that instanceof PromiscuousUnit)) {
            for (int i = 0; i < newValues.length; ++i)
                newValues[i] *= amount;
            newValues = that.toThis(newValues, derivedUnit);
        }
        return newValues;
    }

    /**
     * Convert values from this unit to another unit.
     *
     * @param values    The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @param copy      if false and <code>that</code> equals this, 
     *                  return <code>values</code>, else return a new array
     * @return          The converted values.
     * @require         The units are convertible.
     * @promise         Neither unit has been modified.
     * @throws UnitException    The units are not convertible.
     */
    public float[] toThat(float values[], Unit that, boolean copy)
        throws UnitException
    {
        float[] newValues = (copy) ? (float[])values.clone() : values;
        if (!(equals(that) || that instanceof PromiscuousUnit)) {
            for (int i = 0; i < newValues.length; ++i)
                newValues[i] *= amount;
            newValues = that.toThis(newValues, derivedUnit);
        }
        return newValues;
    }

    /**
     * Indicate whether this unit is convertible with another unit.  If one unit
     * is convertible with another, then the <code>toThis(...)</code>/ and
     * <code>toThat(...)</code> methods will not throw a UnitException.  Unit A
     * is convertible with unit B if and only if unit B is convertible with unit
     * A; hence, calling-order is irrelevant.
     *
     * @param unit      The other unit.
     * @return          True if and only if this unit is convertible with the
     *                  other unit.
     */
    public boolean isConvertible(Unit unit)
    {
      return unit == null ? false : derivedUnit.isConvertible(unit);
    }

    /**
     * Test this class.
     *
     * @param args              Arguments (ignored).
     * @exception UnitException A problem occurred.
     */
    public static void main(String[] args)
        throws UnitException
    {
        BaseUnit        meter = BaseUnit.addBaseUnit("Length", "meter");
        BaseUnit        second = BaseUnit.addBaseUnit("Time", "second");
        DerivedUnit     meterPerSec = new DerivedUnit(
                            new BaseUnit[] {meter, second}, new int[] {1, -1});
        Unit            milePerHour = new ScaledUnit(0.44704, meterPerSec);
        Unit            milePerHour2 = milePerHour.pow(2);

        BaseUnit        kg = BaseUnit.addBaseUnit("Mass", "kilogram");
        DerivedUnit     kgPerSec = new DerivedUnit(new BaseUnit[] {kg, second},
                                                   new int[] {1, -1});
        Unit            poundPerSec = new ScaledUnit(0.453592, kgPerSec);

        System.out.println("milePerHour=\"" + milePerHour + "\"");
        System.out.println("milePerHour.pow(2)=\"" + milePerHour.pow(2) + "\"");
        System.out.println("milePerHour2.root(2)=\"" + milePerHour2.root(2) +
          "\"");

        System.out.println("poundPerSec=\"" + poundPerSec + "\"");

        System.out.println("milePerHour*poundPerSec=\"" +
            milePerHour.multiply(poundPerSec) + "\"");
        System.out.println("poundPerSec*milePerHour=\"" +
            poundPerSec.multiply(milePerHour) + "\"");

        System.out.println("milePerHour/poundPerSec=\"" +
            milePerHour.divide(poundPerSec) + "\"");
        System.out.println("poundPerSec/milePerHour=\"" +
            poundPerSec.divide(milePerHour) + "\"");

        System.out.println("milePerHour.toThis(1,meterPerSec)=" +
            milePerHour.toThis(1,meterPerSec));

        System.out.println("milePerHour.toThat(1,meterPerSec)=" +
            milePerHour.toThat(1,meterPerSec));

        double[] values;

        values = milePerHour.toThis(new double[] {1,2},meterPerSec);
        System.out.println("milePerHour.toThis({1,2},meterPerSec)=" +
            values[0] + "," + values[1]);

        values = milePerHour.toThat(new double[] {1,2},meterPerSec);
        System.out.println("milePerHour.toThat({1,2},meterPerSec)=" +
            values[0] + "," + values[1]);

        System.out.println(
            "new ScaledUnit(0.5).isConvertible(new ScaledUnit(2.0)) = " +
             new ScaledUnit(0.5).isConvertible(new ScaledUnit(2.0)));

        System.out.println("Checking exceptions:");
        try
        {
            milePerHour.toThis(5,poundPerSec);
            System.err.println("ERROR: poundPerSec -> milePerHour");
            System.exit(1);
        }
        catch (UnitException e)
        {
            System.out.println(e.getMessage());
        }
    }

  /**
   * Indicates if this instance is equal to a unit.
   *
   * @param unit             The unit.
   * @return                 <code>true</code> if and only if this instance
   *                         equals the unit.
   */
  public boolean equals(Unit unit) {
    if (unit == null) return false;

    if (unit instanceof BaseUnit) {
      return equals(new ScaledUnit(1, (BaseUnit)unit));
    }

    if (unit instanceof DerivedUnit) {
      return equals(new ScaledUnit(1, (DerivedUnit)unit));
    }

    if (!(unit instanceof ScaledUnit)) {
      return unit.equals(this);
    }

    return derivedUnit.equals(((ScaledUnit) unit).derivedUnit) &&
           amount == ((ScaledUnit) unit).amount;
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
      hashCode ^= derivedUnit.hashCode() ^ new Double(amount).hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

}

