//
// BaseUnit.java
//

/*
 VisAD system for interactive analysis and visualization of numerical
 data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Vector;

/**
 * A class that represents the base units of a system of units.
 * 
 * Instances are immutable.
 * 
 * @author Steven R. Emmerson
 * 
 *         This is part of Steve Emerson's Unit package that has been
 *         incorporated into VisAD.
 */
public final class BaseUnit extends Unit implements Serializable {
    private static final long               serialVersionUID    = 1L;

    /**
     * Name of the unit (e.g. "meter").
     */
    private final String                    unitName;

    /**
     * Quantity of the unit (e.g. "Length").
     */
    private final String                    quantityName;

    /**
     * Derived unit associated with base unit (for computational efficiency).
     */
    final DerivedUnit                       derivedUnit;

    final boolean                           isDimless;

    /**
     * Global database of base units (to prevent multiple base units for the
     * same quantity).
     */
    private static final Vector<BaseUnit>   baseUnits           = new Vector<BaseUnit>(
                                                                        9);

    /**
     * Constructs a base unit from the names for the quantity and unit, a the
     * unit abbreviation, and whether or not the unit is dimensionless.
     * 
     * @param unitName
     *            Name of the unit (e.g. "meter").
     * @param abbreviation
     *            The abbreviation for the unit (e.g. "m").
     * @param quantityName
     *            Name of the quantity (e.g. "Length").
     * @param isDimless
     *            Whether or not the unit is dimensionless.
     * @throws UnitException
     *             Name, abbreviation, or quantity name is <code>
     *                          null</code>
     *             .
     */
    private BaseUnit(final String unitName, final String abbreviation,
            final String quantityName, final boolean isDimless)
            throws UnitException {
        super(abbreviation);
        if (unitName == null || abbreviation == null || quantityName == null) {
            throw new UnitException(
                    "Base unit name, abbreviation, or quantity name is null");
        }
        this.unitName = unitName;
        this.quantityName = quantityName;
        baseUnits.addElement(this);
        derivedUnit = new DerivedUnit(this);
        this.isDimless = isDimless;
    }

    /**
     * <p>
     * Indicates if this instance is dimensionless. A unit is dimensionless if
     * it is a measure of a dimensionless quantity like angle or concentration.
     * Examples of dimensionless base units include radian, degree, and
     * steradian.
     * </p>
     * 
     * @return True if an only if this unit is dimensionless.
     */
    @Override
    public boolean isDimensionless() {
        return isDimless;
    }

    @Override
    public Unit scale(final double amount) throws UnitException {
        return ScaledUnit.getInstance(amount, this);
    }

    @Override
    public Unit shift(final double offset) throws UnitException {
        return OffsetUnit.getInstance(offset, this);
    }

    @Override
    public Unit log(final double base) {
        return derivedUnit.log(base);
    }

    /**
     * Raise a base unit to a power.
     * 
     * @param power
     *            The power to raise this unit by.
     * @return The unit resulting from raising this unit to <code>power</code>.
     * @promise This unit has not been modified.
     */
    @Override
    public Unit pow(final int power) {
        return derivedUnit.pow(power);
    }

    /**
     * Raise a unit to a power.
     * 
     * @param power
     *            The power to raise this unit by. The value must be integral or
     *            reciprocal integral.
     * @return The unit resulting from raising this unit to <code>power</code>.
     * @throws IllegalArgumentException
     *             <code>power</code> has a non-integral or non-reciprocal
     *             integral value.
     * @promise The unit has not been modified.
     */
    @Override
    public Unit pow(final double power) throws IllegalArgumentException {
        return derivedUnit.pow(power);
    }

    /**
     * Returns the N-th root of this unit.
     * 
     * @param root
     *            The root to take (e.g. 2 means square root). May not be zero.
     * @return The unit corresponding to the <code>root</code>-th root of this
     *         unit.
     * @promise This unit has not been modified.
     * @throws IllegalArgumentException
     *             The root value is zero or the resulting unit would have a
     *             non-integral unit dimension.
     */
    @Override
    public Unit root(final int root) throws IllegalArgumentException {
        return derivedUnit.root(root);
    }

    /**
     * Return the name of this unit.
     * 
     * @return The name of this unit (e.g. "meter").
     */
    public String unitName() {
        return unitName;
    }

    /**
     * Return the symbol of this unit. This is the same as the identifier.
     * 
     * @return The symbol of this unit (e.g. "m").
     */
    public String unitSymbol() {
        return getIdentifier();
    }

    /**
     * Return the name of the quantity associated with this unit.
     * 
     * @return The name this units quantity (e.g. "Length").
     */
    public String quantityName() {
        return quantityName;
    }

    /**
     * Create a new base unit from the name of a quantity and the name of a
     * unit. The unit abbreviation will be the same as the unit name. The unit
     * will not be dimensionless.
     * 
     * @param quantityName
     *            The name of the associated quantity (e.g. "Length").
     * @param unitName
     *            The name for the unit (e.g. "meter").
     * @return A new base unit or the previously created one with the same
     *         names.
     * @require The arguments are non-null. The quantity name has not been used
     *          before or the unit name is the same as before.
     * @promise The new quantity and unit has been added to the database.
     * @throws UnitException
     *             Name, abbreviation, or quantity name is <code>
     *                          null</code>
     *             or attempt to redefine the base unit associated with
     *             <code>quantityName</code>.
     */
    public static BaseUnit addBaseUnit(final String quantityName,
            final String unitName) throws UnitException {
        return addBaseUnit(quantityName, unitName, unitName);
    }

    /**
     * Create a new base unit from from the name of a quantity, the name of a
     * unit, and the unit's abbreviation. The unit will not be dimensionless.
     * 
     * @param quantityName
     *            The name of the associated quantity (e.g. "Length").
     * @param unitName
     *            The name for the unit (e.g. "meter").
     * @param abbreviation
     *            The abbreviation for the unit (e.g. "m").
     * @return A new base unit or the previously created one with the same
     *         names.
     * @require The arguments are non-null. The quantity name has not been used
     *          before or the unit name is the same as before.
     * @promise The new quantity and unit has been added to the database.
     * @throws UnitException
     *             Name, abbreviation, or quantity name is <code>
     *                          null</code>
     *             or attempt to redefine the base unit associated with
     *             <code>quantityName</code>.
     */
    public static synchronized BaseUnit addBaseUnit(final String quantityName,
            final String unitName, final String abbreviation)
            throws UnitException {
        return addBaseUnit(quantityName, unitName, abbreviation, false);
    }

    /**
     * Create a new base unit from from the name of a quantity, the name of a
     * unit, the unit's abbreviation, and whether or not the unit is
     * dimensionless.
     * 
     * @param quantityName
     *            The name of the associated quantity (e.g. "Length").
     * @param unitName
     *            The name for the unit (e.g. "meter").
     * @param abbreviation
     *            The abbreviation for the unit (e.g. "m").
     * @param isDimless
     *            Whether or not the unit is dimensionless.
     * @return A new base unit or the previously created one with the same
     *         names.
     * @require The arguments are non-null. The quantity name has not been used
     *          before or the unit name is the same as before.
     * @promise The new quantity and unit has been added to the database.
     * @throws UnitException
     *             Name, abbreviation, or quantity name is <code>
     *                          null</code>
     *             or attempt to redefine the base unit associated with
     *             <code>quantityName</code>.
     */
    public static synchronized BaseUnit addBaseUnit(final String quantityName,
            final String unitName, final String abbreviation,
            final boolean isDimless) throws UnitException {
        final BaseUnit baseUnit = quantityNameToUnit(quantityName);

        if (baseUnit == null) {
            return new BaseUnit(unitName, abbreviation, quantityName, isDimless);
        }

        if (baseUnit.unitName.equals(unitName)
                && baseUnit.getIdentifier().equals(abbreviation)
                && baseUnit.isDimless == isDimless) {
            return baseUnit;
        }

        throw new UnitException("Attempt to redefine quantity \""
                + quantityName + "\" base unit from \"" + baseUnit.unitName
                + "(" + baseUnit.getIdentifier() + ")" + "\" to \"" + unitName
                + "(" + abbreviation + ")" + "\"");
    }

    /**
     * Find the base unit with the given name.
     * 
     * @param unitName
     *            The name of the unit (e.g. "meter").
     * @return The existing base unit with the given name or <code>null</code>
     *         if no such units exists.
     * @require The argument is non-null.
     */
    public static synchronized BaseUnit unitNameToUnit(final String unitName) {
        for (int i = 0; i < baseUnits.size(); ++i) {
            final BaseUnit baseUnit = baseUnits.elementAt(i);

            if (baseUnit.unitName.equals(unitName)) {
                return baseUnit;
            }
        }

        return null;
    }

    /**
     * Find the base unit for the given quantity.
     * 
     * @param quantityName
     *            The name of the quantity (e.g. "Length").
     * @return The existing base unit for the given quantity or
     *         <code>null</code> if no such unit exists.
     * @require The argument is non-null.
     */
    public static synchronized BaseUnit quantityNameToUnit(
            final String quantityName) {
        for (int i = 0; i < baseUnits.size(); ++i) {
            final BaseUnit baseUnit = baseUnits.elementAt(i);

            if (baseUnit.quantityName.equals(quantityName)) {
                return baseUnit;
            }
        }

        return null;
    }

    private static void myAssert(final boolean assertion) {
        if (!assertion) {
            throw new AssertionError();
        }
    }

    private static void myAssert(final String have, final String expect) {
        if (!have.equals(expect)) {
            throw new AssertionError(have + " != " + expect);
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
     * @throws UnitException
     *             A problem occurred.
     */
    public static void main(final String[] args) throws UnitException {
        final BaseUnit meter = BaseUnit.addBaseUnit("Length", "meter", "m");

        myAssert(meter, meter);
        myAssert(meter.isConvertible(meter));
        myAssert(meter.toString(), "m");
        myAssert(meter.pow(2), meter.multiply(meter));
        myAssert(meter.pow(2).sqrt(), meter);

        final BaseUnit second = BaseUnit.addBaseUnit("Time", "second", "s");

        myAssert(!meter.equals(second));
        myAssert(!meter.isConvertible(second));

        Unit unit = meter.multiply(second);
        myAssert(unit, second.multiply(meter));
        unit = meter.divide(second);
        myAssert(unit, second.divide(meter).pow(-1));
        myAssert(!unit.equals(meter));
        myAssert(!unit.equals(second));

        myAssert(meter.toThis(5, meter), 5);
        myAssert(meter.toThat(5, meter), 5);

        final double[] values = { 1, 2 };
        myAssert(meter.toThis(values, meter), values);
        myAssert(meter.toThat(values, meter), values);

        System.out.println("Checking exceptions:");
        try {
            meter.toThis(5, second);
            throw new AssertionError();
        }
        catch (final UnitException e) {
            System.out.println(e.getMessage());
        }
        try {
            meter.toThat(5, second);
            throw new AssertionError();
        }
        catch (final UnitException e) {
            System.out.println(e.getMessage());
        }
        try {
            BaseUnit.addBaseUnit("Length", "foot", "ft");
            throw new AssertionError();
        }
        catch (final UnitException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Done");
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @return The converted values in units of this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    double[] toThis(final double[] values, final BaseUnit that)
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    float[] toThis(final float[] values, final BaseUnit that)
            throws UnitException {
        return toThis(values, that, true);
    }

    /**
     * Convert values to this unit from a base unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values in units of this unit.
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    double[] toThis(final double[] values, final BaseUnit that,
            final boolean copy) throws UnitException {
        if (equals(that)) {
            final double[] newValues = (copy)
                    ? (double[]) values.clone()
                    : values;
            return newValues;
        }

        throw new UnitException("Attempt to convert from unit \"" + that
                + "\" to unit \"" + this + "\"");
    }

    /**
     * Convert values to this unit from a base unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values in units of this unit.
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    float[] toThis(final float[] values, final BaseUnit that, final boolean copy)
            throws UnitException {
        if (equals(that)) {
            final float[] newValues = (copy)
                    ? (float[]) values.clone()
                    : values;

            return newValues;
        }

        throw new UnitException("Attempt to convert from unit \"" + that
                + "\" to unit \"" + this + "\"");
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            The values to be converted.
     * @param that
     *            The unit of <code>values</code>.
     * @return The converted values in units of this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThis(final double[] values, final Unit that,
            final boolean copy) throws UnitException {
        return that.toThat(values, derivedUnit, copy);
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
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThis(final float[] values, final Unit that,
            final boolean copy) throws UnitException {
        return that.toThat(values, derivedUnit, copy);
    }

    /**
     * Convert values from this unit to a base unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @return The converted values.
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    double[] toThat(final double[] values, final BaseUnit that)
            throws UnitException {
        return toThat(values, that, true);
    }

    /**
     * Convert values from this unit to a base unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @return The converted values.
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    float[] toThat(final float[] values, final BaseUnit that)
            throws UnitException {
        return toThat(values, that, true);
    }

    /**
     * Convert values from this unit to a base unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values.
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    double[] toThat(final double[] values, final BaseUnit that,
            final boolean copy) throws UnitException {
        if (equals(that)) {
            final double[] newValues = (copy)
                    ? (double[]) values.clone()
                    : values;
            return newValues;
        }

        throw new UnitException("Attempt to convert from unit \"" + this
                + "\" to unit \"" + that + "\"");
    }

    /**
     * Convert values from this unit to a base unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @param copy
     *            if false and <code>that</code> equals this, return
     *            <code>values</code>, else return a new array
     * @return The converted values.
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    float[] toThat(final float[] values, final BaseUnit that, final boolean copy)
            throws UnitException {
        if (equals(that)) {
            final float[] newValues = (copy)
                    ? (float[]) values.clone()
                    : values;
            return newValues;
        }

        throw new UnitException("Attempt to convert from unit \"" + this
                + "\" to unit \"" + that + "\"");
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values to be converted in units of this unit.
     * @param that
     *            The unit to which to convert the values.
     * @return The converted values.
     * @require The units are identical.
     * @promise Neither unit has been modified.
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
     * @require The units are identical.
     * @promise Neither unit has been modified.
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
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThat(final double[] values, final Unit that,
            final boolean copy) throws UnitException {
        return that.toThis(values, derivedUnit, copy);
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
     * @require The units are identical.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThat(final float[] values, final Unit that,
            final boolean copy) throws UnitException {
        return that.toThis(values, derivedUnit, copy);
    }

    /**
     * Returns the definition of this unit. The definition of a BaseUnit is the
     * same as the BaseUnit's identifier.
     * 
     * @return The definition of this unit. Won't be <code>null
     *                  </code>
     *         but may be empty.
     */
    @Override
    public String getDefinition() {
        return getIdentifier();
    }

    /**
     * Clones this unit, changing the identifier. This method always throws an
     * exception because base units may not be cloned.
     * 
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     * @return A unit equal this this instance but with the given identifier.
     * @throws UnitException
     *             Base units may not be cloned. Always thrown.
     */
    @Override
    protected Unit protectedClone(final String identifier) throws UnitException {
        throw new UnitException("Base units may not be cloned");
    }

    /**
     * Indicates whether or not this instance equals a unit.
     * 
     * @param unit
     *            A unit.
     * @return <code>true</code> if and only if this instance is equal to the
     *         unit.
     */
    @Override
    public boolean equals(final Unit unit) {
        if (this == unit) {
            return true;
        }
        if (!(unit instanceof BaseUnit)) {
            return  derivedUnit.equals(unit);
        }
        final BaseUnit that = (BaseUnit) unit;
        return unitName.equals(that.unitName)
                && quantityName.equals(that.quantityName)
                && isDimless == that.isDimless;
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
            hashCode = unitName.hashCode() ^ quantityName.hashCode()
                    ^ Boolean.valueOf(isDimless).hashCode();
        }
        return hashCode;
    }

    /**
     * Multiply this unit by another unit.
     * 
     * @param that
     *            The unit with which to multiply this unit.
     * @return The product of the two units.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    public Unit multiply(final Unit that) throws UnitException {
        return derivedUnit.multiply(that);
    }

    /**
     * Divide this unit by another unit.
     * 
     * @param that
     *            The unit to divide into this unit.
     * @return The quotient of the two units.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    public Unit divide(final Unit that) throws UnitException {
        return derivedUnit.divide(that);
    }

    /**
     * Divide this unit into another unit.
     * 
     * @param that
     *            The unit to divided this unit.
     * @return The quotient of the two units.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    protected Unit divideInto(final Unit that) throws UnitException {
        return derivedUnit.divideInto(that);
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
                : derivedUnit.isConvertible(unit);
    }

    @Override
    public DerivedUnit getDerivedUnit() {
        return derivedUnit;
    }
}
