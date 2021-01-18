//
// OffsetUnit.java
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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A class that represents a scaled unit with an offset.
 * 
 * Instances are immutable.
 * 
 * @author Steven R. Emmerson
 * 
 *         This is part of Steve Emerson's Unit package that has been
 *         incorporated into VisAD.
 * 
 *         Instances are immutable.
 */
public final class OffsetUnit extends Unit implements Serializable {
    private static final long       serialVersionUID    = 1L;

    /**
     * The associated (unoffset) underlying unit.
     */
    final Unit                      underUnit;

    /**
     * The offset for this unit (e.g. 273.15 for the celsius unit when the
     * kelvin unit is associated scaled unit).
     */
    final double                    offset;

    /**
     * The date formatter.
     * 
     * @serial
     */
    private static SimpleDateFormat dateFormat;

    /**
     * The arbitrary time origin.
     * 
     * @serial
     */
    private static double           offsetUnitOrigin;

    /**
     * The millisecond unit.
     */
    private static Unit             millisecond;

    static {
        try {
            dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(
                    DateFormat.SHORT, Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.SSS 'UTC'");
            final Calendar calendar = new GregorianCalendar(TimeZone
                    .getTimeZone("UTC"));
            calendar.clear();
            calendar.set(2001, 0, 1, 0, 0, 0); // data.netcdf.units.UnitParser
            offsetUnitOrigin = calendar.getTime().getTime();
            millisecond = SI.second.scale(0.001).clone("ms");
        }
        catch (final Exception e) {
            System.err
                    .println("OffsetUnit.<clinit>: Couldn't initialize class: "
                            + e);
            System.exit(1);
        }
    }

    /**
     * Construct an offset, dimensionless unit. The identifier will be empty.
     * 
     * @param offset
     *            The amount of offset.
     */
    public OffsetUnit(final double offset) {
        this(offset, "");
    }

    /**
     * Construct an offset, dimensionless unit with an identifier.
     * 
     * @param offset
     *            The amount of offset.
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     * 
     */
    public OffsetUnit(final double offset, final String identifier) {
        super(identifier);
        this.offset = offset;
        underUnit = new ScaledUnit(1.0);
    }

    /**
     * Construct an offset unit from another unit. The identifier will be that
     * of the base unit if the offset is zero; otherwise, the identifier will be
     * <code>null</code>.
     * 
     * @param offset
     *            The amount of offset.
     * @param unit
     *            The other unit.
     */
    public OffsetUnit(final double offset, final Unit unit) {
        this(offset, unit, null);
    }

    /**
     * Construct an offset unit from another unit and an identifier.
     * 
     * @param offset
     *            The amount of offset.
     * @param unit
     *            The other unit.
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     */
    public OffsetUnit(final double offset, final Unit unit,
            final String identifier) {
        super(identifier != null
                ? identifier
                : offset == 0
                        ? unit.getIdentifier()
                        : null);
        this.offset = offset;
        underUnit = unit;
    }

    /**
     * Returns an instance based on an offset and an underlying unit.
     * 
     * @param offset
     *            The offset.
     * @param unit
     *            The underlying unit.
     * @return An instance corresponding to the input.
     */
    static Unit getInstance(final double offset, final Unit unit) {
        return offset == 0
                ? unit
                : new OffsetUnit(offset, unit);
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
     * Indicates if this instance is a unit of time.
     * 
     * @return <code>true</code> if and only if this instance is a unit of time.
     */
    protected boolean isTime() {
        return SI.second.isConvertible(underUnit);
    }

    /**
     * Clones this unit, changing the identifier.
     * 
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     * @return A unit equal to this unit but with the given identifier.
     */
    @Override
    protected Unit protectedClone(final String identifier) {
        return new OffsetUnit(0, this, identifier);
    }

    @Override
    public Unit scale(final double amount) throws UnitException {
        return OffsetUnit.getInstance(offset / amount, underUnit.scale(amount));
    }

    @Override
    public Unit shift(final double offset) throws UnitException {
        return OffsetUnit.getInstance(offset + this.offset, underUnit);
    }

    @Override
    public Unit log(final double base) throws UnitException {
        return LogarithmicUnit.getInstance(base, this);
    }

    /**
     * Raises an offset unit to a power. Raising an offset unit to a power is
     * equivalent to first stripping-off the offset amount and then raising the
     * resulting non-offset unit to the power.
     * 
     * @param power
     *            The power to raise this unit by.
     * @return A corresponding unit.
     * @throws UnitException
     *             if the underlying unit can't be raised to the given power.
     */
    @Override
    public Unit pow(final int power) throws UnitException {
        return underUnit.pow(power);
    }

    /**
     * Raises an offset unit to a power. Raising an offset unit to a power is
     * equivalent to first stripping-off the offset amount and then raising the
     * resulting non-offset unit to the power.
     * 
     * @param power
     *            The power to raise this unit by.
     * @return A corresponding unit.
     * @throws UnitException
     *             if the underlying unit can't be raised to the given power.
     */
    @Override
    public Unit pow(final double power) throws UnitException {
        return underUnit.pow(power);
    }

    /**
     * Returns the N-th root of this unit. Taking the root of an offset unit is
     * equivalent to first stripping-off the offset amount and then taking the
     * root of the resulting non-offset unit.
     * 
     * @param root
     *            The root to take (e.g. 2 means square root). May not be zero.
     * @return The unit corresponding to the <code>root</code>-th root of this
     *         unit.
     * @throws IllegalArgumentException
     *             The root value is zero or the resulting unit would have a
     *             non-integral unit dimension.
     * @throws UnitException
     *             if the underlying unit can't have the given root taken.
     */
    @Override
    public Unit root(final int root) throws IllegalArgumentException,
            UnitException {
        return underUnit.root(root);
    }

    /**
     * Return the definition of this unit.
     * 
     * @return The definition of this unit (e.g. "K @ 273.15" for degree
     *         celsius).
     */
    @Override
    public String getDefinition() {
        String definition;
        String scaledString = underUnit.toString();

        if (scaledString.indexOf(' ') != -1) {
            scaledString = "(" + scaledString + ")";
        }
        if (!isTime()) {
            definition = scaledString + " @ " + offset;
        }
        else {
            try {
                definition = scaledString
                        + " since "
                        + dateFormat
                                .format(new Date((long) (millisecond.toThis(
                                        offset, underUnit) + offsetUnitOrigin)));
            }
            catch (final UnitException e) {
                definition = e.toString();
            } // can't happen
        }
        return definition;
    }

    /**
     * Multiply an offset unit by another unit.
     * 
     * @param that
     *            The unit with which to multiply this unit.
     * @return A unit equal to this instance multiplied by the given unit.
     * @exception UnitException
     *                Can't multiply units.
     */
    @Override
    public Unit multiply(final Unit that) throws UnitException {
        return that.multiply(underUnit);
    }

    /**
     * Divide an offset unit by another unit.
     * 
     * @param that
     *            The unit to divide into this unit.
     * @return A unit equal to this instance divided by the given unit.
     * @exception UnitException
     *                Can't divide units.
     */
    @Override
    public Unit divide(final Unit that) throws UnitException {
        return that.divideInto(underUnit);
    }

    /**
     * Divide an offset unit into another unit.
     * 
     * @param that
     *            The unit to be divide by this unit.
     * @return The quotient of the two units.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             Meaningless operation.
     */
    @Override
    protected Unit divideInto(final Unit that) throws UnitException {
        return that.divide(underUnit);
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
                    newValues[i] -= offset;
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
     *            if false, convert values in place.
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
                    newValues[i] -= offset;
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
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public double[] toThat(final double values[], final Unit that)
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
     * require: The units are convertible.
     * promise: Neither unit has been modified.
     * @throws UnitException
     *             The units are not convertible.
     */
    @Override
    public float[] toThat(final float values[], final Unit that)
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
     *            if false and <code>that</code> equals this, return a
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
                if (newValues[i] == newValues[i]) {
                    newValues[i] += offset;
                }
            }
            newValues = that.toThis(newValues, underUnit, copy);
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
     *            if false and <code>that</code> equals this, return a
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
                if (newValues[i] == newValues[i]) {
                    newValues[i] += offset;
                }
            }
            newValues = that.toThis(newValues, underUnit, copy);
        }
        return newValues;
    }

    /**
     * Gets the absolute unit of this unit. An interval in the underlying
     * physical quantity has the same numeric value in an absolute unit of a
     * unit as in the unit itself -- but an absolute unit is always referenced
     * to the physical origin of the underlying physical quantity. For example,
     * the absolute unit corresponding to degrees celsius is degrees kelvin --
     * and calling this method on a degrees celsius unit obtains a degrees
     * kelvin unit.
     * 
     * @return The absolute unit corresponding to this unit.
     */
    @Override
    public Unit getAbsoluteUnit() {
        return underUnit.getAbsoluteUnit();
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
        return underUnit.isConvertible(unit);
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
        final BaseUnit degK = SI.kelvin;
        final Unit degC = new OffsetUnit(273.15, degK);
        final ScaledUnit degR = new ScaledUnit(1 / 1.8, degK);
        final Unit degF = new OffsetUnit(459.67, degR);

        myAssert(degC, degC);
        myAssert(!degC.equals(degK));
        myAssert(!degC.equals(degF));
        myAssert(degC.isConvertible(degC));
        myAssert(degC.isConvertible(degK));
        myAssert(degC.isConvertible(degF));
        myAssert(degF, degF);

        myAssert(degF.toThis(0, degC), degC.toThat(0, degF));
        myAssert(degC.toThis(32, degF), degF.toThat(32, degC));
        myAssert(degC.toThis(degF.toThis(0, degC), degF), 0);
        myAssert(degC.toThat(degF.toThat(32, degC), degF), 32);

        double[] values = { 0, 100 };

        myAssert(degF.toThis(values, degC), degC.toThat(values, degF));
        myAssert(degC.toThis(degF.toThis(values, degC), degF), values);

        values = new double[] { 32, 212 };

        myAssert(degC.toThis(values, degF), degF.toThat(values, degC));

        myAssert(degF.pow(2), degR.pow(2));
        myAssert(degF.multiply(degC), degC.multiply(degF));
        myAssert(degF.multiply(degC), degR.multiply(degK));
        myAssert(degF.divide(degC), degR.divide(degK));
        myAssert(degC.divide(degF), degK.divide(degR));

        System.out.println("Done");
    }

    /**
     * Indicates if this instance equals a unit.
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
        if (offset == 0)
            return underUnit.equals(unit);
        if (!(unit instanceof OffsetUnit)) {
            return false;
        }
        final OffsetUnit that = (OffsetUnit) unit;
        return offset == that.offset && underUnit.equals(that.underUnit);
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
            hashCode = offset == 0
                ? underUnit.hashCode()
                : underUnit.hashCode() ^ Double.valueOf(offset).hashCode();
        }
        return hashCode;
    }

    @Override
    public DerivedUnit getDerivedUnit() {
        return underUnit.getDerivedUnit();
    }
}
