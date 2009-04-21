//
// Unit.java
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
 * A class that represents a unit of a quantity.
 * 
 * Instances are immutable.
 * 
 * @author Steven R. Emmerson
 * 
 *         This is part of Steve Emmerson's Unit package that has been
 *         incorporated into VisAD.
 */
public abstract class Unit implements Serializable {
    private static final long   serialVersionUID    = 1L;

    /**
     * The identifier (name or abbreviation) for this unit.
     * 
     * @serial
     */
    private final String        identifier;

    protected transient int     hashCode            = 0;

    /*
     * added by Bill Hibbard for VisAD
     */

    /**
     * <p>
     * Converts a tuple of double value arrays; returning a new tuple.
     * </p>
     * 
     * <p>
     * This implementation uses {@link #toThis(double[], Unit)} to convert the
     * individual arrays.
     * </p>
     * 
     * @param value
     *            The tuple of numeric value arrays to convert.
     *            <code> value[i][j]</code> is the value of the <code> i</code>
     *            th component of sample-point <code>j </code>.
     * @param units_in
     *            The units of the input numeric values.
     *            <code>units_in[i]</code> is the unit of the <code>i</code>th
     *            conponent.
     * @param units_out
     *            The units of the output numeric values.
     *            <code>units_out[i]</code> is the unit for the <code>i</code>th
     *            conponent.
     * @return Returns the converted values in a new array where RETURN_VALUE
     *         <code>[i][j]</code> is the converted value of
     *         <code>value[i][j]</code>.
     * @throws UnitException
     *             If an ouput unit is <code>null</code> and the corresponding
     *             input unit is neither <code>null</code> nor a
     *             {@link PromiscuousUnit}, or if an input unit is not
     *             convertible with its corresponding output unit.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static double[][] convertTuple(final double[][] value,
            final Unit[] units_in, final Unit[] units_out)
            throws UnitException, VisADException {
        return convertTuple(value, units_in, units_out, true);
    }

    /**
     * <p>
     * Converts a tuple of double value arrays, optionally returning a new tuple
     * depending on the value of <code>copy</code>.
     * </p>
     * 
     * <p>
     * This implementation uses {@link #toThis(double[], Unit)} to convert the
     * individual arrays.
     * </p>
     * 
     * @param value
     *            The tuple of numeric value arrays to convert.
     *            <code> value[i][j]</code> is the value of the <code> i</code>
     *            th component of sample-point <code>j </code>.
     * @param units_in
     *            The units of the input numeric values.
     *            <code>units_in[i]</code> is the unit of the <code>i</code>th
     *            conponent.
     * @param units_out
     *            The units of the output numeric values.
     *            <code>units_out[i]</code> is the unit for the <code>i</code>th
     *            conponent.
     * @param copy
     *            If true, a new array is created, otherwise if a unit in
     *            <code>units_in</code> equals the unit at the corresponding
     *            index in the <code>units_out</code>, the input value array at
     *            that index is returned instead of a new array.
     * @return If <code>units_in</code> equals <code>units_out
   *                           </code>
     *         copy is false, then this just returns the value argument.
     *         Otherwise, returns the the converted values in a new array where
     *         RETURN_VALUE <code>[i][j]</code> is the converted value of
     *         <code>value[i][j]</code>.
     * @throws UnitException
     *             If an ouput unit is <code>null</code> and the corresponding
     *             input unit is neither <code>null</code> nor a
     *             {@link PromiscuousUnit}, or if an input unit is not
     *             convertible with its corresponding output unit.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static double[][] convertTuple(final double[][] value,
            final Unit[] units_in, final Unit[] units_out, final boolean copy)
            throws UnitException, VisADException {

        // If the input array equals the output array then simply return the
        // value array
        if (java.util.Arrays.equals(units_in, units_out) && !copy) {
            return value;
        }
        final double[][] new_value = new double[value.length][];
        for (int i = 0; i < value.length; i++) {
            if (units_out[i] == null) {
                if (units_in[i] != null
                        && !(units_in[i] instanceof PromiscuousUnit)) {
                    throw new UnitException(
                            "Unit.convertTuple: illegal Unit conversion");
                }
                new_value[i] = (copy)
                        ? (double[]) value[i].clone()
                        : value[i];
            }
            else {
                // If they are equal just do an assignment
                if (units_out[i].equals(units_in[i]) && !copy) {
                    new_value[i] = value[i];
                }
                else {
                    // else do the conversion (creates a new array)
                    new_value[i] = units_out[i].toThis(value[i], units_in[i]);
                }
            }
        }
        return new_value;
    }

    /**
     * <p>
     * Converts a tuple of float value arrays; returning a new tuple.
     * </p>
     * 
     * <p>
     * This implementation uses {@link #toThis(float[], Unit)} to convert the
     * individual arrays.
     * </p>
     * 
     * @param value
     *            The tuple of numeric value arrays to convert.
     *            <code> value[i][j]</code> is the value of the <code> i</code>
     *            th component of sample-point <code>j </code>.
     * @param units_in
     *            The units of the input numeric values.
     *            <code>units_in[i]</code> is the unit of the <code>i</code>th
     *            conponent.
     * @param units_out
     *            The units of the output numeric values.
     *            <code>units_out[i]</code> is the unit for the <code>i</code>th
     *            conponent.
     * @return Returns the converted values in a new array where RETURN_VALUE
     *         <code>[i][j]</code> is the converted value of
     *         <code>value[i][j]</code>.
     * @throws UnitException
     *             If an ouput unit is <code>null</code> and the corresponding
     *             input unit is neither <code>null</code> nor a
     *             {@link PromiscuousUnit}, or if an input unit is not
     *             convertible with its corresponding output unit.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static float[][] convertTuple(final float[][] value,
            final Unit[] units_in, final Unit[] units_out)
            throws UnitException, VisADException {
        return convertTuple(value, units_in, units_out, true);
    }

    /**
     * <p>
     * Converts a tuple of float value arrays, optionally returning a new tuple
     * depending on the value of <code>copy</code>.
     * </p>
     * 
     * <p>
     * This implementation uses {@link #toThis(float[], Unit)} to convert the
     * individual arrays.
     * </p>
     * 
     * @param value
     *            The tuple of numeric value arrays to convert.
     *            <code> value[i][j]</code> is the value of the <code> i</code>
     *            th component of sample-point <code>j </code>.
     * @param units_in
     *            The units of the input numeric values.
     *            <code>units_in[i]</code> is the unit of the <code>i</code>th
     *            conponent.
     * @param units_out
     *            The units of the output numeric values.
     *            <code>units_out[i]</code> is the unit for the <code>i</code>th
     *            conponent.
     * @param copy
     *            If true, a new array is created, otherwise if a unit in
     *            <code>units_in</code> equals the unit at the corresponding
     *            index in the <code>units_out</code>, the input value array at
     *            that index is returned instead of a new array.
     * @return If <code>units_in</code> equals <code>units_out
   *                           </code>
     *         copy is false, then this just returns the value argument.
     *         Otherwise, returns the the converted values in a new array where
     *         RETURN_VALUE <code>[i][j]</code> is the converted value of
     *         <code>value[i][j]</code>.
     * @throws UnitException
     *             If an ouput unit is <code>null</code> and the corresponding
     *             input unit is neither <code>null</code> nor a
     *             {@link PromiscuousUnit}, or if an input unit is not
     *             convertible with its corresponding output unit.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static float[][] convertTuple(final float[][] value,
            final Unit[] units_in, final Unit[] units_out, final boolean copy)
            throws UnitException, VisADException {

        // If the input array equals the output array then simply return the
        // value array
        if (java.util.Arrays.equals(units_in, units_out) && !copy) {
            return value;
        }
        final float[][] new_value = new float[value.length][];
        for (int i = 0; i < value.length; i++) {
            if (units_out[i] == null) {
                if (units_in[i] != null
                        && !(units_in[i] instanceof PromiscuousUnit)) {
                    throw new UnitException(
                            "Unit.convertTuple: illegal Unit conversion");
                }
                new_value[i] = (copy)
                        ? (float[]) value[i].clone()
                        : value[i];
            }
            else {
                // If they are equal just do an assignment
                if (units_out[i].equals(units_in[i]) && !copy) {
                    new_value[i] = value[i];
                }
                else {
                    // else do the conversion
                    new_value[i] = units_out[i].toThis(value[i], units_in[i]);
                }
            }
        }
        return new_value;
    }

    /**
     * Indicates if values in two units are convertible. The values of two units
     * are convertible if each unit is either <code>null</code> or the
     * promiscuous unit, or if one unit is neither <code>null</code> nor the
     * promiscuous unit and the other unit is either the promiscuous unit or a
     * convertible unit.
     * 
     * @param unita
     *            One unit.
     * @param unitb
     *            The other unit.
     * @return <code>true</code> if and only if values in the the two units are
     *         convertible.
     * @see #isConvertible(Unit)
     */
    public static boolean canConvert(Unit unita, Unit unitb) {
        if (CommonUnit.promiscuous.equals(unita)) {
            unita = null;
        }
        if (CommonUnit.promiscuous.equals(unitb)) {
            unitb = null;
        }
        if (unita == null && unitb == null) {
            return true;
        }
        if (unita == null || unitb == null) {
            return false;
        }
        // WLH - real logic goes here
        return unita.isConvertible(unitb);
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
    public abstract boolean isConvertible(Unit unit);

    /**
     * <p>
     * Indicates whether or not two unit arrays are convertible. Two such arrays
     * are convertible if and only if the units of their corresponding elements
     * are convertible.
     * </p>
     * 
     * <p>
     * This implementation uses {@link #canConvert(Unit, Unit)} to determine
     * convertibility of the element pairs.
     * 
     * @param unita
     *            One array of units. May be <code>null</code>.
     * @param unitb
     *            The other array of units. May be <code>null</code>.
     * @return <code>true</code> if and only if both unit arrays are
     *         <code>null</code> or the two unit arrays are element-by-element
     *         convertible.
     */
    public static boolean canConvertArray(Unit[] unita, Unit[] unitb) {
        if (unita == null && unitb == null) {
            return true;
        }
        if (unita == null) {
            unita = new Unit[unitb.length];
        }
        if (unitb == null) {
            unitb = new Unit[unita.length];
        }
        final int n = unita.length;
        if (n != unitb.length) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (!canConvert(unita[i], unitb[i])) {
                // System.out.println("i = " + i + " " + unita[i] + " != " +
                // unitb[i]);
                return false;
            }
        }
        return true;
    }

    /**
     * Copys an array of units. This is a helper method for {@link Set},
     * {@link RealTupleType}, {@link CoordinateSystem}, etc.
     * 
     * @param units
     *            The array of units or <code>null</code>.
     * @return A copy of the array of units or <code>null</code>. if the input
     *         array is <code>null</code>.
     */
    public static Unit[] copyUnitsArray(final Unit[] units) {
        return units == null
                ? null
                : (Unit[]) units.clone();
    }

    /**
     * Indicates whether or not this instance is equal to an object.
     * 
     * @param that
     *            The object in question.
     * @return <code>true</code> if and only if this instance equals the unit.
     */
    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Unit)) {
            return false;
        }
        return equals((Unit) that);
    }

    /**
     * Abstract method for computing hashcode
     */
    @Override
    public abstract int hashCode();

    /**
     * Indicates whether or not this instance is equal to a unit.
     * 
     * @param unit
     *            The unit.
     * @return <code>true</code> if and only if this instance equals the unit.
     */
    public abstract boolean equals(Unit unit);

    /**
     * Transform double values and (optionally) error estimates.
     * 
     * @param unit_out
     *            The unit of the output numeric values or <code>null</code>.
     * @param errors_out
     *            The output error estimate. <code>errors_out[0]
   *                          </code>
     *            will contain the output error estimate, which may be
     *            <code>null</code>.
     * @param unit_in
     *            The unit of the input numeric values.
     * @param error_in
     *            The input error estimate or <code>null</code>.
     * @param value
     *            The input numeric value.
     * @return The corresponding, transformed numeric values in the same array
     *         only if the input and output units were <code>null</code>;
     *         otherwise, a new array is returned.
     * @throws NullPointerException
     *             if <code>errors_out</code> is <code>null
   *                              </code>
     *             .
     * @throws UnitException
     *             if the input and output unit aren't convertible.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static double[] transformUnits(final Unit unit_out,
            final ErrorEstimate[] errors_out, final Unit unit_in,
            final ErrorEstimate error_in, final double[] value)
            throws UnitException, VisADException {

        return transformUnits(unit_out, errors_out, unit_in, error_in, value,
                true);
    }

    /**
     * Transform float values and (optionally) error estimates.
     * 
     * @param unit_out
     *            The unit of the output numeric values or <code>null</code>.
     * @param errors_out
     *            The output error estimate. <code>errors_out[0]
   *                          </code>
     *            will contain the output error estimate, which may be
     *            <code>null</code>.
     * @param unit_in
     *            The unit of the input numeric values.
     * @param error_in
     *            The input error estimate or <code>null</code>.
     * @param value
     *            The input numeric value.
     * @return The corresponding, transformed numeric values in the same array
     *         only if the input and output units were <code>null</code>;
     *         otherwise, a new array is returned.
     * @throws NullPointerException
     *             if <code>errors_out</code> is <code>null
   *                              </code>
     *             .
     * @throws UnitException
     *             if the input and output unit aren't convertible.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static float[] transformUnits(final Unit unit_out,
            final ErrorEstimate[] errors_out, final Unit unit_in,
            final ErrorEstimate error_in, final float[] value)
            throws UnitException, VisADException {

        return transformUnits(unit_out, errors_out, unit_in, error_in, value,
                true);
    }

    /**
     * Transform double values and (optionally) error estimates.
     * 
     * @param unit_out
     *            The unit of the output numeric values or <code>null</code>.
     * @param errors_out
     *            The output error estimate. <code>errors_out[0]
   *                          </code>
     *            will contain the output error estimate, which may be
     *            <code>null</code>.
     * @param unit_in
     *            The unit of the input numeric values.
     * @param error_in
     *            The input error estimate or <code>null</code>.
     * @param value
     *            The input numeric value.
     * @param copy
     *            if false and <code>unit_out</code> equals <code>unit_in</code>
     *            , transform values in place.
     * @return The corresponding, transformed numeric values in the same array
     *         only if the input and output units were <code>null</code>;
     *         otherwise, a new array is returned.
     * @throws NullPointerException
     *             if <code>errors_out</code> is <code>null
   *                              </code>
     *             .
     * @throws UnitException
     *             if the input and output unit aren't convertible.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static double[] transformUnits(final Unit unit_out,
            final ErrorEstimate[] errors_out, final Unit unit_in,
            final ErrorEstimate error_in, final double[] value,
            final boolean copy) throws UnitException, VisADException {

        if (unit_out == null || unit_in == null) {
            errors_out[0] = error_in;
            return (copy)
                    ? (double[]) value.clone()
                    : value;
        }
        else {
            // convert value array
            final double[] val = unit_out.toThis(value, unit_in, copy);

            // construct new ErrorEstimate, if needed
            if (error_in == null) {
                errors_out[0] = null;
            }
            else {
                // scale data.ErrorEstimate for Unit.toThis
                final double error = 0.5 * error_in.getErrorValue();
                final double mean = error_in.getMean();
                final double new_error = Math.abs(unit_out.toThis(mean + error,
                        unit_in)
                        - unit_out.toThis(mean - error, unit_in));
                errors_out[0] = new ErrorEstimate(val, new_error, unit_out);
            }

            // return value array
            return val;
        }
    }

    /**
     * Transform float values and (optionally) error estimates.
     * 
     * @param unit_out
     *            The unit of the output numeric values or <code>null</code>.
     * @param errors_out
     *            The output error estimate. <code>errors_out[0]
   *                          </code>
     *            will contain the output error estimate, which may be
     *            <code>null</code>.
     * @param unit_in
     *            The unit of the input numeric values.
     * @param error_in
     *            The input error estimate or <code>null</code>.
     * @param value
     *            The input numeric value.
     * @param copy
     *            if false and <code>unit_out</code> equals <code>unit_in</code>
     *            , transform values in place.
     * @return The corresponding, transformed numeric values in the same array
     *         only if the input and output units were <code>null</code>;
     *         otherwise, a new array is returned.
     * @throws NullPointerException
     *             if <code>errors_out</code> is <code>null
   *                              </code>
     *             .
     * @throws UnitException
     *             if the input and output unit aren't convertible.
     * @throws VisADException
     *             if a VisAD failure occurs.
     */
    public static float[] transformUnits(final Unit unit_out,
            final ErrorEstimate[] errors_out, final Unit unit_in,
            final ErrorEstimate error_in, final float[] value,
            final boolean copy) throws UnitException, VisADException {

        if (unit_out == null || unit_in == null) {
            errors_out[0] = error_in;
            return (copy)
                    ? (float[]) value.clone()
                    : value;
        }
        else {
            // convert value array
            final float[] val = unit_out.toThis(value, unit_in, copy);

            // construct new ErrorEstimate, if needed
            if (error_in == null) {
                errors_out[0] = null;
            }
            else {
                // scale data.ErrorEstimate for Unit.toThis
                final double error = 0.5 * error_in.getErrorValue();
                final double mean = error_in.getMean();
                final double new_error = Math.abs(unit_out.toThis(mean + error,
                        unit_in)
                        - unit_out.toThis(mean - error, unit_in));
                errors_out[0] = new ErrorEstimate(val, new_error, unit_out);
            }

            // return value array
            return val;
        }
    }

    Unit scale(final double amount, final boolean b) {
        return new ScaledUnit(amount, this);
    }

    /*
     * end of added by Bill Hibbard for VisAD
     */

    /**
     * Constructs from nothing.
     */
    protected Unit() {
        this.identifier = null;
    }

    /**
     * Constructs from an identifier.
     * 
     * @param identifier
     *            Name or abbreviation for the unit. May be <code>null</code> or
     *            empty.
     */
    protected Unit(String identifier) {
        try {
            identifier = adjustCheckAndCache(identifier);
        }
        catch (final UnitExistsException e) {
            System.err.println("WARNING: " + e);
        }
        this.identifier = identifier;
    }

    /**
     * Adjusts, checks, and caches a unit identifier and its unit.
     * 
     * @param identifier
     *            Name or abbreviation for the unit. May be <code>null</code> or
     *            empty.
     * @return The identifier adjusted as necessary in order to be valid (e.g.
     *         whitespace replacement).
     * @throws UnitExistsException
     *             A different unit with the same, non-null and non-empty
     *             identifier already exists. The identifier and unit are not
     *             cached.
     */
    protected final String adjustCheckAndCache(final String identifier)
            throws UnitExistsException {
        if (identifier != null && identifier.length() > 0) {
            /*
             * identifier = identifier.replace(' ', '_'); // ensure no
             * whitespace synchronized(identifierMap) { Unit previous =
             * (Unit)identifierMap.get(identifier); if (previous != null) throw
             * new UnitExistsException(identifier);
             * identifierMap.put(identifier, this); }
             */
        }
        return identifier;
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
    public abstract boolean isDimensionless();

    /**
     * <p>
     * Clones this unit, changing the identifier.
     * </p>
     * 
     * <p>
     * This implementation uses the {@link #protectedClone(String)} method.
     * </p>
     * 
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty.
     * @return A unit equal to this instance but with the given identifier
     *         (adjusted if necessary).
     * @throws UnitException
     *             The unit may not be cloned. This will only occur if
     *             <code>getIdentifier()!=null</code>.
     * @see #adjustCheckAndCache(String)
     */
    public Unit clone(final String identifier) throws UnitException {
        return protectedClone(adjustCheckAndCache(identifier));
    }

    /**
     * Clones this unit, changing the identifier.
     * 
     * @param identifier
     *            The name or abbreviation for the cloned unit. May be
     *            <code>null</code> or empty. It shall have already passed the
     *            {@link #adjustCheckAndCache(String)} method.
     * @return A unit equal to this instance but with the given identifier.
     * @throws UnitException
     *             if the unit may not be cloned. This will only occur if
     *             <code>getIdentifier()!=null</code>.
     */
    protected abstract Unit protectedClone(String identifier)
            throws UnitException;

    /**
     * Raise this unit to a power.
     * 
     * @param power
     *            The power to raise this unit by.
     * @return The resulting unit.
     * @require The unit is not an offset unit.
     * @promise The unit has not been modified.
     * @exception UnitException
     *                It's meaningless to raise this unit by a power.
     */
    public abstract Unit pow(int power) throws UnitException;

    /**
     * Returns the N-th root of this unit.
     * 
     * @param root
     *            The root to take (e.g. 2 means square root). Must not be zero.
     * @return The unit corresponding to the <code>root</code>-th root of this
     *         unit.
     * @require The unit is not an offset unit.
     * @promise The unit has not been modified.
     * @exception UnitException
     *                It's meaningless to raise this unit by a power.
     * @throws IllegalArgumentException
     *             The root value is zero or the resulting unit would have a
     *             non-integral unit dimension.
     */
    public abstract Unit root(int root) throws IllegalArgumentException,
            UnitException;

    /**
     * Returns the square-root of this unit. This method is identical to
     * {@link #root(int root)} with a value of <code>2</code>.
     * 
     * @return The unit corresponding to the square-root of this unit.
     * @promise This unit has not been modified.
     * @throws IllegalArgumentException
     *             The resulting unit would have a non-integral unit dimension.
     * @throws UnitException
     *             It is meaningless to take a root of this unit.
     */
    public Unit sqrt() throws IllegalArgumentException, UnitException {
        return root(2);
    }

    /**
     * Raise a unit to a power.
     * 
     * @param power
     *            The power to raise this unit by. If this unit is not
     *            dimensionless, then the value must be integral.
     * @return The unit resulting from raising this unit to <code>power</code>.
     * @throws UnitException
     *             It's meaningless to raise this unit by a power.
     * @throws IllegalArgumentException
     *             This unit is not dimensionless and <code>power</code> has a
     *             non-integral value.
     * @promise The unit has not been modified.
     */
    public abstract Unit pow(double power) throws UnitException,
            IllegalArgumentException;

    /**
     * Scale this unit by an amount.
     * 
     * @param amount
     *            The amount by which to scale this unit. E.g. Unit yard =
     *            meter.scale(0.9144);
     * @return A unit equal to this instance scaled by the given amount.
     * @exception UnitException
     *                This unit cannot be scaled.
     */
    public abstract Unit scale(final double amount) throws UnitException;

    /**
     * Shift this unit by an amount.
     * 
     * @param offset
     *            The amount by which to shift this unit. E.g. Unit celsius =
     *            kelvin.shift(273.15);
     * @return A unit equal to this instance with the origin shifted by the
     *         given amount.
     * @exception UnitException
     *                The unit subclass is unknown.
     */
    public abstract Unit shift(final double offset) throws UnitException;

    /**
     * Returns the logarithmic unit that has this unit as its reference level.
     * Not all units can be used as a reference level.
     * 
     * @param base
     *            The logarithmic base: one of {@code 2}, {@link Math#E}, or
     *            {@code 10}.
     * @return The logarithmic unit that has this instance as its reference
     *         level.
     * @throws IllegalArgumentException
     *             if {@code base} isn't one of the allowed values.
     * @throws UnitException
     *             if this unit can't be used as a reference level for a
     *             logarithmic unit.
     */
    public abstract Unit log(final double base) throws UnitException;

    /**
     * Multiply this unit by another unit.
     * 
     * @param that
     *            The given unit to multiply this unit by.
     * @return The resulting unit.
     * @throws UnitException
     *             It's meaningless to multiply these units.
     */
    public abstract Unit multiply(Unit that) throws UnitException;

    /**
     * Divide this unit by another unit.
     * 
     * @param that
     *            The unit to divide into this unit.
     * @return The quotient of the two units.
     * @promise Neither unit has been modified.
     * @throws UnitException
     *             It's meaningless to divide these units.
     */
    public abstract Unit divide(Unit that) throws UnitException;

    /**
     * Divide this unit into another unit.
     * 
     * @param that
     *            The unit to be divided by this unit.
     * @return The quotient of the two units.
     * @throws UnitException
     *             It's meaningless to divide these units.
     */
    protected abstract Unit divideInto(Unit that) throws UnitException;

    /**
     * Convert a value to this unit from another unit.
     * 
     * @param value
     *            The value in units of the other unit.
     * @param that
     *            The other unit.
     * @return The value converted from the other unit to this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public double toThis(final double value, final Unit that)
            throws UnitException {
        return toThis(new double[] { value }, that, false)[0];
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            Values in units of the other unit.
     * @param that
     *            The other unit.
     * @return Values in this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public abstract double[] toThis(double[] values, Unit that)
            throws UnitException;

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            Values in units of the other unit.
     * @param that
     *            The other unit.
     * @return Values in this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public abstract float[] toThis(float[] values, Unit that)
            throws UnitException;

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            Values in units of the other unit.
     * @param that
     *            The other unit.
     * @param copy
     *            true to make a copy if units are not equal. Ignored in this
     *            class.
     * @return Values in this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public double[] toThis(final double[] values, final Unit that,
            final boolean copy) throws UnitException {
        return toThis(values, that);
    }

    /**
     * Convert values to this unit from another unit.
     * 
     * @param values
     *            Values in units of the other unit.
     * @param that
     *            The other unit.
     * @param copy
     *            true to make a copy if units are not equal. Ignored in this
     *            class.
     * @return Values in this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public float[] toThis(final float[] values, final Unit that,
            final boolean copy) throws UnitException {
        return toThis(values, that);
    }

    /**
     * Convert a value from this unit to another unit.
     * 
     * @param value
     *            The value in this unit.
     * @param that
     *            The other unit.
     * @return The value in units of the other unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public double toThat(final double value, final Unit that)
            throws UnitException {
        return toThat(new double[] { value }, that, false)[0];
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values in this unit.
     * @param that
     *            The other unit.
     * @return Values converted to the other unit from this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public abstract double[] toThat(double[] values, Unit that)
            throws UnitException;

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values in this unit.
     * @param that
     *            The other unit.
     * @return Values converted to the other unit from this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public abstract float[] toThat(float[] values, Unit that)
            throws UnitException;

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values in this unit.
     * @param that
     *            The other unit.
     * @param copy
     *            true to make a copy if units are not equal. Ignored in this
     *            class.
     * @return Values converted to the other unit from this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public double[] toThat(final double[] values, final Unit that,
            final boolean copy) throws UnitException {
        return toThat(values, that);
    }

    /**
     * Convert values from this unit to another unit.
     * 
     * @param values
     *            The values in this unit.
     * @param that
     *            The other unit.
     * @param copy
     *            true to make a copy if units are not equal. Ignored in this
     *            class.
     * @return Values converted to the other unit from this unit.
     * @require The units are convertible.
     * @promise Neither unit has been modified.
     * @exception UnitException
     *                The units are not convertible.
     */
    public float[] toThat(final float[] values, final Unit that,
            final boolean copy) throws UnitException {
        return toThat(values, that);
    }

    /**
     * Returns a string representation of this unit.
     * 
     * @return The string representation of this unit. Won't be
     *         <code>null</code> but may be empty.
     */
    @Override
    public final String toString() {
        String s = getIdentifier();
        if (s == null || s.length() == 0) {
            s = getDefinition();
        }
        return s;
    }

    /**
     * Returns the identifier (name or abbreviation) of this unit.
     * 
     * @return The identifier of this unit. May be <code>null</code> but won't
     *         be empty.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the definition of this unit.
     * 
     * @return The definition of this unit. Won't be <code>null
     *                  </code>
     *         but may be empty.
     */
    public abstract String getDefinition();

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
    public Unit getAbsoluteUnit() {
        return this;
    }

    /**
     * Returns the derived unit that underlies this unit.
     * 
     * @return The derived unit that underlies this unit.
     */
    public abstract DerivedUnit getDerivedUnit();
}
