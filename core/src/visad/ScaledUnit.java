//
// ScaledUnit.java
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
import java.util.Arrays;

/**
 * A class that represents a certain amount of a derived unit.
 * 
 * Instances are immutable.
 * 
 * @author Steven R. Emmerson
 * 
 *         This is part of Steve Emerson's Unit package that has been
 *         incorporated into VisAD.
 */
public final class ScaledUnit extends Unit implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The amount of the associated derived unit.
     */
    final double              amount;

    /**
     * The underlying unit that's scaled.
     */
    final Unit                underUnit;

    /**
     * Construct a dimensionless scaled unit. The identifier will be empty.
     * 
     * @param amount
     *            The given amount of this unit.
     */
    public ScaledUnit(final double amount) {
        this(amount, "");
    }

    /**
     * Construct a dimensionless scaled unit with an identifier.
     * 
     * @param amount
     *            The given amount of this unit.
     * @param identifier
     *            Name or abbreviation for the unit. May be <code>null</code> or
     *            empty.
     */
    public ScaledUnit(final double amount, final String identifier) {
        super(identifier);
        this.amount = amount;
        underUnit = new DerivedUnit();
    }

    /**
     * Constructs a scaled unit from another unit. The identifier will be that
     * of the other unit if the amount is 1; otherwise, the identifier will be
     * <code>null</code>.
     * 
     * @param amount
     *            The given amount of the other unit (e.g. 0.9144 to create a
     *            yard unit if <code>unit</code> represents a meter).
     * @param unit
     *            The other unit.
     */
    public ScaledUnit(final double amount, final Unit unit) {
        this(amount, unit, null);
    }

    /**
     * Constructs a scaled unit from another unit and an identifier.
     * 
     * @param amount
     *            The given amount of the base unit (e.g. 0.9144 to create a
     *            yard unit if <code>unit</code> represents a meter).
     * @param unit
     *            The other unit.
     * @param identifier
     *            Name or abbreviation for the unit. May be <code>null</code> or
     *            empty. If {@code null} and if {@code amount} is {@code 1},
     *            then the identifier of {@code unit} is used.
     */
    public ScaledUnit(final double amount, final Unit unit,
            final String identifier) {
        super(identifier != null
                ? identifier
                : amount == 1
                        ? unit.getIdentifier()
                        : null);
        if (unit instanceof ScaledUnit) {
            final ScaledUnit that = (ScaledUnit) unit;
            this.amount = amount * that.amount;
            this.underUnit = that.underUnit;
        }
        else {
            this.amount = amount;
            this.underUnit = unit;
        }
    }

    public ScaledUnit(final double amount, final BaseUnit unit) {
        this(amount, (Unit) unit);
    }

    public ScaledUnit(final double amount, final BaseUnit unit, final String id) {
        this(amount, (Unit) unit, id);
    }

    public ScaledUnit(final double amount, final DerivedUnit unit) {
        this(amount, (Unit) unit);
    }

    public ScaledUnit(final double amount, final DerivedUnit unit,
            final String id) {
        this(amount, (Unit) unit, id);
    }

    /**
     * Factory method for creating a scaled unit. The identifier will be that of
     * the input unit if the scaling amount is 1; otherwise, the identifier will
     * be <code>null</code>.
     * 
     * @param amount
     *            The given amount of the scaled unit (e.g. 3.0 to create a yard
     *            unit if <code>unit</code> represents a foot.
     * @param unit
     *            The given unit.
     * @return A corresponding scaled unit.
     * @throws UnitException
     *             Can't create Scaled Unit from <code>unit</code>.
     */
    public static ScaledUnit create(final double amount, final Unit unit)
            throws UnitException {
        return (amount == 1 && unit instanceof ScaledUnit)
                ? (ScaledUnit) unit
                : new ScaledUnit(amount, unit);
    }

    /**
     * Returns an instance based on a scale amount and an underlying unit.
     * 
     * @param amount
     *            The amount of {@code unit}
     * @param unit
     *            The underlying unit.
     * @return An instance corresponding to the input.
     * @throws UnitException
     *             If the instance can't be created.
     */
    static Unit getInstance(final double amount, final Unit unit)
            throws UnitException {
        if (amount == 0) {
            throw new IllegalArgumentException("Zero amount argument");
        }
        return (amount == 1)
                ? unit
                : new ScaledUnit(amount, unit);
    }

    /**
     * <p>
     * Indicates if this instance is dimensionless. A unit is dimensionless if
     * it is a measure of a dimensionless quantity like angle or concentration.
     * Examples of dimensionless units include radian, degree, steradian, and
     * "g/kg".
     * </p>
     * 
     * @return True if an only if this unit is dimensionless.
     */
    @Override
    public boolean isDimensionless() {
        return underUnit.isDimensionless();
    }

    /**
     * Clones this unit, changing the identifier.
     * 
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     * @return A unit equal to this instance but with the given identifier.
     */
    @Override
    protected Unit protectedClone(final String identifier) {
        return new ScaledUnit(amount, underUnit, identifier);
    }

    @Override
    public Unit scale(final double amount) throws UnitException {
        return ScaledUnit.getInstance(amount * this.amount, underUnit);
    }

    @Override
    public Unit shift(final double offset) throws UnitException {
        return OffsetUnit.getInstance(offset, this);
    }

    @Override
    public Unit log(final double base) {
        return LogarithmicUnit.getInstance(base, this);
    }

    /**
     * Raises this unit to a power.
     * 
     * @param power
     *            The power to raise this unit by.
     * @return The unit resulting from raising this unit to <code>power</code>.
     * @throws UnitException
     *             if the underlying unit can't be raised to the given power.
     * promise: This unit has not been modified.
     */
    @Override
    public Unit pow(final int power) throws UnitException {
        return new ScaledUnit(Math.pow(amount, power), underUnit.pow(power));
    }

    /**
     * Raises this unit to a power.
     * 
     * @param power
     *            The power to raise this unit by. If this unit is not
     *            dimensionless, then the value must be integral.
     * @return The unit resulting from raising this unit to <code>power</code>.
     * @throws IllegalArgumentException
     *             This unit is not dimensionless and <code>power</code> has a
     *             non-integral value.
     * @throws UnitException
     *             if the underlying unit can't be raised to the given power.
     * promise: The unit has not been modified.
     */
    @Override
    public Unit pow(final double power) throws IllegalArgumentException,
            UnitException {
        return new ScaledUnit(Math.pow(amount, power), underUnit.pow(power));
    }

    /**
     * Returns the N-th root of this unit.
     * 
     * @param root
     *            The root to take (e.g. 2 means square root). May not be zero.
     * @return The unit corresponding to the <code>root</code>-th root of this
     *         unit.
     * @throws IllegalArgumentException
     *             The root value is zero or the resulting unit would have a
     *             non-integral unit dimension.
     * @throws UnitException
     *             if the underlying unit given can't have the given root taken.
     * promise: This unit has not been modified.
     */
    @Override
    public Unit root(final int root) throws IllegalArgumentException,
            UnitException {
        return new ScaledUnit(Math.pow(amount, 1. / root), underUnit.root(root));
    }

    /**
     * Returns the definition of this unit.
     * 
     * @return The definition of this unit (e.g. "0.9144 m" for a yard).
     */
    @Override
    public String getDefinition() {
        String definition;
        if (underUnit == null) {
            /* Probably exception thrown during construction */
            definition = "<unconstructed ScaledUnit>";
        }
        else {
            final String derivedString = underUnit.toString();
            definition = amount == 1
                    ? derivedString
                    : derivedString.length() == 0
                            ? Double.toString(amount)
                            : Double.toString(amount) + " " + derivedString;
        }
        return definition;
    }

    /**
     * Get the scale amount
     * 
     * @return The scale amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Get the underlying unit
     * 
     * @return The underlying unit
     */
    public Unit getUnit() {
        return underUnit;
    }

    /**
     * Multiplies this unit by another unit.
     * 
     * @param that
     *            The unit with which to multiply this unit.
     * @return The product of the two units.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    public Unit multiply(final Unit that) throws UnitException {
        return create(amount, underUnit.multiply(that));
    }

    /**
     * Divides this unit by another unit.
     * 
     * @param that
     *            The unit to divide into this unit.
     * @return The quotient of the two units.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    public Unit divide(final Unit that) throws UnitException {
        return create(amount, underUnit.divide(that));
    }

    /**
     * Divides this unit into another unit.
     * 
     * @param that
     *            The unit to be divided by this unit.
     * @return The quotient of the two units.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    protected Unit divideInto(final Unit that) throws UnitException {
        return create(1. / amount, underUnit.divideInto(that));
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @return The converted values in units of this unit.
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThis(final double[] values, final Unit that)
            throws UnitException {
        return toThis(values, that, true);
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @return The converted values in units of this unit.
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThis(final float[] values, final Unit that)
            throws UnitException {
        return toThis(values, that, true);
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values in units of this unit.
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThis(final double[] values, final Unit that,
            final boolean copy) throws UnitException {
        double[] newValues;
        if (equals(that) || that instanceof PromiscuousUnit) {
            newValues = (copy)
                    ? (double[]) values.clone()
                    : values;
        }
        else {
            newValues = that.toThat(values, underUnit, copy);
            for (int i = 0; i < newValues.length; ++i) {
                if (newValues[i] == newValues[i]) {
                    newValues[i] /= amount;
                }
            }
        }
        return newValues;
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values in units of this unit.
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThis(final float[] values, final Unit that,
            final boolean copy) throws UnitException {
        float[] newValues;
        if (equals(that) || that instanceof PromiscuousUnit) {
            newValues = (copy)
                    ? (float[]) values.clone()
                    : values;
        }
        else {
            newValues = that.toThat(values, underUnit, copy);
            for (int i = 0; i < newValues.length; ++i) {
                if (newValues[i] == newValues[i]) {
                    newValues[i] /= amount;
                }
            }
        }
        return newValues;
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @return The converted values.
     * require: The units are identical.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThat(final double[] values, final Unit that)
            throws UnitException {
        return toThat(values, that, true);
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @return The converted values.
     * require: The units are identical.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThat(final float[] values, final Unit that)
            throws UnitException {
        return toThat(values, that, true);
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values.
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThat(final double values[], final Unit that,
            final boolean copy) throws UnitException {
        double[] newValues = (copy)
                ? (double[]) values.clone()
                : values;
        if (!(equals(that) || that instanceof PromiscuousUnit)) {
            for (int i = 0; i < newValues.length; ++i) {
                newValues[i] *= amount;
            }
            newValues = that.toThis(newValues, underUnit);
        }
        return newValues;
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values.
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThat(final float values[], final Unit that,
            final boolean copy) throws UnitException {
        float[] newValues = (copy)
                ? (float[]) values.clone()
                : values;
        if (!(equals(that) || that instanceof PromiscuousUnit)) {
            for (int i = 0; i < newValues.length; ++i) {
                newValues[i] *= amount;
            }
            newValues = that.toThis(newValues, underUnit);
        }
        return newValues;
    }

    /**
     * Indicate whether this unit is convertible with another unit. If one unit
     * is convertible with another, then the <code>toThis(...)</code>/ and
     * <code>toThat(...)</code> methods will not throw a UnitException. Unit A
     * is convertible with unit B if and only if unit B is convertible with unit
     * A; hence, calling-order is irrelevant.
     * 
     * @param unit
     *            The other unit.
     * @return True if and only if this unit is convertible with the other unit.
     */
    @Override
    public boolean isConvertible(final Unit unit) {
        return unit == null
                ? false
                : underUnit.isConvertible(unit);
    }

    private static void myAssert(final boolean assertion) {
        if (!assertion) {
            throw new AssertionError();
        }
    }

    private static void myAssert(final Unit have, final Unit expect) {
        if (!have.equals(expect)) {
            throw new AssertionError(have.toString() + " != " + expect);
        }
    }

    private static void myAssert(final double have, final double expect) {
        if (have != expect) {
            throw new AssertionError("" + have + " != " + expect);
        }
    }

    private static void myAssert(final double[] have, final double[] expect) {
        if (!Arrays.equals(have, expect)) {
            throw new AssertionError("" + have + " != " + expect);
        }
    }

    /**
     * Test this class.
     * 
     * @param args
     *            Arguments (ignored).
     * @exception UnitException
     *                A problem occurred.
     */
    public static void main(final String[] args) throws UnitException {
        final BaseUnit meter = BaseUnit.addBaseUnit("Length", "meter");
        final BaseUnit second = BaseUnit.addBaseUnit("Time", "second");
        final DerivedUnit meterPerSec = new DerivedUnit(new BaseUnit[] { meter,
                second }, new int[] { 1, -1 });
        final Unit milePerHour = new ScaledUnit(0.44704, meterPerSec);
        final Unit milePerHour2 = milePerHour.pow(2);

        myAssert(milePerHour, milePerHour);
        myAssert(!milePerHour.equals(meterPerSec));
        myAssert(milePerHour.isConvertible(milePerHour));
        myAssert(milePerHour.isConvertible(meterPerSec));
        myAssert(!milePerHour.equals(milePerHour2));
        myAssert(!milePerHour.isConvertible(milePerHour2));
        myAssert(!milePerHour.equals(meter));
        myAssert(!milePerHour.isConvertible(meter));
        myAssert(milePerHour2, milePerHour2);
        myAssert(milePerHour2.isConvertible(milePerHour2));
        myAssert(milePerHour2.sqrt(), milePerHour);

        final BaseUnit kg = BaseUnit.addBaseUnit("Mass", "kilogram");
        final DerivedUnit kgPerSec = new DerivedUnit(new BaseUnit[] { kg,
                second }, new int[] { 1, -1 });
        final Unit poundPerSec = new ScaledUnit(0.453592, kgPerSec);

        myAssert(milePerHour.multiply(poundPerSec), poundPerSec
                .multiply(milePerHour));
        myAssert(milePerHour.divide(poundPerSec), poundPerSec.divide(
                milePerHour).pow(-1));

        myAssert(milePerHour.toThis(1, meterPerSec) != 1);
        myAssert(milePerHour.toThat(1, meterPerSec) != 1);

        myAssert(milePerHour.toThis(1, meterPerSec), meterPerSec.toThat(1,
                milePerHour));
        myAssert(meterPerSec.toThat(milePerHour.toThat(1, meterPerSec),
                milePerHour), 1);

        final double[] values = { 1, 2 };

        myAssert(milePerHour.toThis(values, meterPerSec), meterPerSec.toThat(
                values, milePerHour));
        myAssert(meterPerSec.toThat(milePerHour.toThat(values, meterPerSec),
                milePerHour), values);

        System.out.println("Checking exceptions:");
        try {
            milePerHour.toThis(5, poundPerSec);
            throw new AssertionError();
        }
        catch (final UnitException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done");
    }

    /**
     * Indicates if this instance is equal to a unit.
     * 
     * @param unit
     *            The unit.
     * @return <code>true</code> if and only if this instance equals the unit.
     */
    @Override
    public boolean equals(final Unit unit) {
        if (this == unit) {
            return true;
        }
        if (amount == 1)
            return underUnit.equals(unit);
        if (!(unit instanceof ScaledUnit)) {
            return false;
        }
        final ScaledUnit that = (ScaledUnit) unit;
        return amount == that.amount && underUnit.equals(that.underUnit);
    }

    /**
     * Returns the hash code of this instance. {@link Object#hashCode()} should
     * be overridden whenever {@link Object#equals(Object)} is.
     * 
     * @return The hash code of this instance (includes the values).
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = amount == 1
                ? underUnit.hashCode()
                : underUnit.hashCode() ^ Double.valueOf(amount).hashCode();
        }
        return hashCode;
    }

    @Override
    public DerivedUnit getDerivedUnit() {
        return underUnit.getDerivedUnit();
    }

}
