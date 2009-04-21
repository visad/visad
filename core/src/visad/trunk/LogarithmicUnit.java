//
// OffsetUnit.java
//

/*
 VisAD system for interactive analysis and visualization of numerical
 data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
public final class LogarithmicUnit extends Unit implements Serializable {
    private static final long       serialVersionUID    = 1L;
    private static final Unit       ONE                 = new DerivedUnit();
    /**
     * The reference level.
     * 
     * @serial
     */
    private final Unit              reference;
    /**
     * The logarithmic base.
     * 
     * @serial
     */
    private final double            base;
    /**
     * The natural logarithm of the base (for computational efficiency).
     */
    private final transient double  lnBase;

    /**
     * Constructs from a reference level and a logarithmic base. The identifier
     * will be empty.
     * 
     * @param base
     *            The logarithmic base. Must be 2, {@link Math#E}, or 10.
     * @param reference
     *            The reference level.
     * @throws IllegalArgumentException
     *             if {@code base} isn't one of the allowed values.
     * @throws NullPointerException
     *             if {@code reference} is {@code null}.
     */
    private LogarithmicUnit(final double base, final Unit reference) {
        this(reference, base, "");
    }

    /**
     * Constructs from a reference level, a logarithmic base, and an identifier.
     * 
     * @param reference
     *            The reference level.
     * @param base
     *            The logarithmic base. Must be 2, {@link Math#E}, or 10.
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     * @throws IllegalArgumentException
     *             if {@code base} isn't one of the allowed values.
     * @throws NullPointerException
     *             if {@code reference} is {@code null}.
     */
    private LogarithmicUnit(final Unit reference, final double base,
            final String identifier) {
        super(identifier);
        if (reference == null) {
            throw new NullPointerException("Null reference level");
        }
        if (base != 2 && base != Math.E && base != 10) {
            throw new IllegalArgumentException("Invalid logarithmic base: "
                    + base);
        }
        this.reference = reference;
        this.base = base;
        lnBase = base == Math.E
                ? 1
                : Math.log(base);
    }

    static Unit getInstance(final double base, final Unit reference) {
        return new LogarithmicUnit(base, reference);
    }

    /**
     * Indicates if this instance is dimensionless. Logarithmic units are
     * dimensionless by definition.
     * 
     * @return {@code true} always.
     */
    @Override
    public boolean isDimensionless() {
        return true;
    }

    /**
     * Indicates if this instance is a unit of time.
     * 
     * @return {@code true} if and only if values in this unit are convertible
     *         with seconds.
     */
    protected boolean isTime() {
        return SI.second.isConvertible(reference);
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
        return new LogarithmicUnit(reference, base, identifier);
    }

    @Override
    public Unit scale(final double scale) throws UnitException {
        return ScaledUnit.getInstance(scale, this);
    }

    @Override
    public Unit shift(final double offset) throws UnitException {
        return OffsetUnit.getInstance(offset, this);
    }

    @Override
    public Unit log(final double base) throws UnitException {
        throw new UnitException(
                "Can't form logarithmic unit from logarithmic unit: " + this);
    }

    /**
     * Raises this unit to a power. Only certain powers are meaningful.
     * 
     * @param power
     *            The power to raise this unit by. The only meaningful values
     *            are {@cocde 0} and {@code 1}.
     * @return The result of raising this unit to the given power.
     * @throws IllegalArgumentException
     *             if {@code power} isn't {@code 0} or {@code 1}.
     */
    @Override
    public Unit pow(final int power) {
        Unit result;
        if (power == 0) {
            result = ONE;
        }
        else if (power == 1) {
            result = this;
        }
        else {
            throw new IllegalArgumentException("Invalid power: " + power);
        }
        return result;
    }

    /**
     * Raises this unit to a power. Only certain powers are meaningful.
     * 
     * @param power
     *            The power to raise this unit by. The only meaningful values
     *            are 0 and 1.
     * @return The result of raising this unit to the given power.
     */
    @Override
    public Unit pow(final double power) {
        if (power != 0 || power != 1) {
            throw new IllegalArgumentException("Invalid power: " + power);
        }
        return pow((int) Math.round(power));
    }

    /**
     * Returns the N-th root of this unit. Only the 1st root is meaningful.
     * 
     * @param root
     *            The root to take. Must be {@code 1}.
     * @return This instance.
     * @throws IllegalArgumentException
     *             if {@code root} isn't {@code 1}.
     */
    @Override
    public Unit root(final int root) throws IllegalArgumentException {
        if (root != 1) {
            throw new IllegalArgumentException("Invalid root: " + root);
        }
        return this;
    }

    /**
     * Return the definition of this unit as a string.
     * 
     * @return The definition of this unit (e.g., {@code "lg(re 0.001 W)"}) for
     *         a Bel unit with a milliwatt reference level.
     */
    @Override
    public String getDefinition() {
        final StringBuilder buf = new StringBuilder(40);
        if (base == 2) {
            buf.append("lb");
        }
        else if (base == Math.E) {
            buf.append("ln");
        }
        else if (base == 10) {
            buf.append("lg");
        }
        else {
            throw new AssertionError("Invalid base: " + base);
        }
        buf.append("(re ");
        buf.append(reference.toString());
        buf.append(")");
        return buf.toString();
    }

    /**
     * Multiply this unit by another unit. Only certain other units are
     * meaningful.
     * 
     * @param that
     *            The unit with which to multiply this unit. Must be
     *            dimensionless.
     * @return A unit equal to this instance multiplied by the given unit.
     * @throws IllegalArgumentException
     *             if {@code that} isn't dimensionless.
     * @throws UnitException
     *             Can't multiply units.
     */
    @Override
    public Unit multiply(final Unit that) throws UnitException {
        if (!that.isDimensionless()) {
            throw new IllegalArgumentException("Not dimensionless: " + that);
        }
        final Unit result;
        if (that.equals(ONE)) {
            result = this;
        }
        else if (that instanceof LogarithmicUnit) {
            throw new UnitException("Can't multiply by: " + that);
        }
        else {
            result = that.multiply(this);
        }
        return result;
    }

    /**
     * Divide this unit by another unit. Only certain other units are
     * meaningful.
     * 
     * @param that
     *            The unit to divide into this unit. Must be dimensionless.
     * @return A unit equal to this instance divided by the given unit.
     * @exception UnitException
     *                Can't divide units.
     */
    @Override
    public Unit divide(final Unit that) throws UnitException {
        if (!that.isDimensionless()) {
            throw new IllegalArgumentException("Not dimensionless: " + that);
        }
        final Unit result;
        if (that.equals(ONE)) {
            result = this;
        }
        else if (that instanceof LogarithmicUnit) {
            throw new UnitException("Can't divide by: " + that);
        }
        else {
            result = that.divideInto(this);
        }
        return result;
    }

    /**
     * Divide this unit into another unit. This operation isn't meaningful.
     * 
     * @param that
     *            The unit to be divide by this unit.
     * @return Never
     * @throws UnitException
     *             if this method is called.
     */
    @Override
    protected Unit divideInto(final Unit that) throws UnitException {
        throw new UnitException("Can't divide into: " + that);
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
            newValues = that.toThat(values, reference, copy);
            for (int i = 0; i < newValues.length; ++i) {
                newValues[i] = Math.log(newValues[i]) / lnBase;
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
            newValues = that.toThat(values, reference, copy);
            for (int i = 0; i < newValues.length; ++i) {
                newValues[i] = (float) (Math.log(newValues[i]) / lnBase);
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
     * @param copy
     *            if false and <code>that</code> equals this, return a
     *            <code>values</code>, else return a new array
     * @return The converted values.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
                newValues[i] = Math.exp(newValues[i] * lnBase);
            }
            newValues = that.toThis(newValues, reference, copy);
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
     * @require The units are convertible.
     * @promise Neither unit has been modified.
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
                newValues[i] = (float) Math.exp(newValues[i] * lnBase);
            }
            newValues = that.toThis(newValues, reference, copy);
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
        return reference.getAbsoluteUnit();
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
        return reference.isConvertible(unit);
    }

    private static void myAssert(final boolean bool) {
        if (!bool) {
            throw new AssertionError();
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
        final BaseUnit meter = SI.meter;
        final ScaledUnit micron = new ScaledUnit(1e-6, meter);
        final Unit cubicMicron = micron.pow(3);
        final LogarithmicUnit Bz = new LogarithmicUnit(10, cubicMicron);
        myAssert(Bz.isDimensionless());
        myAssert(Bz.equals(Bz));
        myAssert(Bz.getAbsoluteUnit().equals(cubicMicron));
        myAssert(!Bz.equals(cubicMicron));
        myAssert(!Bz.equals(micron));
        myAssert(!Bz.equals(meter));
        try {
            Bz.multiply(meter);
            myAssert(false);
        }
        catch (final Exception e) {
        }
        try {
            Bz.divide(meter);
            myAssert(false);
        }
        catch (final Exception e) {
        }
        try {
            Bz.pow(2);
            myAssert(false);
        }
        catch (final Exception e) {
        }
        double value = Bz.toThat(0, Bz.getAbsoluteUnit());
        myAssert(0.9 < value && value < 1.1);
        value = Bz.toThat(1, Bz.getAbsoluteUnit());
        myAssert(9 < value && value < 11);
        value = Bz.toThis(1, Bz.getAbsoluteUnit());
        myAssert(-0.1 < value && value < 0.1);
        value = Bz.toThis(10, Bz.getAbsoluteUnit());
        myAssert(0.9 < value && value < 1.1);
        final String string = Bz.toString();
        myAssert(string.equals("lg(re 9.999999999999999E-19 m3)"));
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
        if (!(unit instanceof LogarithmicUnit)) {
            return false;
        }
        final LogarithmicUnit that = (LogarithmicUnit) unit;
        return base == that.base && reference.equals(that.reference);
    }

    /**
     * Returns the hash code of this instance. {@link Object#hashCode()} should
     * be overridden whenever {@link Object#equals(Object)} is.
     * 
     * @return The hash code of this instance.
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = reference.hashCode() ^ new Double(base).hashCode();
        }
        return hashCode;
    }

    @Override
    public DerivedUnit getDerivedUnit() {
        return reference.getDerivedUnit();
    }
}
