//
// DerivedUnit.java
//

/*
 VisAD system for interactive analysis and visualization of numerical
 data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
import java.text.ChoiceFormat;
import java.util.Arrays;
import java.util.Vector;

/**
 * A class that represents a unit consisting of zero or more base units.
 * 
 * Instances are immutable.
 * 
 * @author Steven R. Emmerson
 * 
 *         This is part of Steve Emmerson's Unit package that has been
 *         incorporated into VisAD.
 */
public final class DerivedUnit extends Unit implements Serializable {
	private static final long	serialVersionUID	= 1L;
	/**
	 * The array of individual base-unit factors that make up this unit.
	 * 
	 * The following is effectively "final" in that it is only set in
	 * constructors and never altered. Unfortunately, the JDK 1.1.2 javac(1)
	 * compiler on our SunOS 5.5 systems doesn't recognize this fact; hence, the
	 * "final" is commented-out.
	 */
	/* final */Factor[]			factors;

	/**
	 * Construct a dimensionless derived unit. The identifier of the unit will
	 * be empty.
	 */
	public DerivedUnit() {
		this(new BaseUnit[] {}, new int[] {}, "");
	}

	/**
	 * Construct a dimensionless derived unit with an identifier.
	 * 
	 * @param identifier
	 *            Name or abbreviation for the unit. May be <code>null</code> or
	 *            empty.
	 */
	public DerivedUnit(final String identifier) {
		this(new BaseUnit[] {}, new int[] {}, identifier);
	}

	/**
	 * Construct a derived unit from a base unit. The identifier of the unit
	 * will be that of the base unit.
	 * 
	 * @param baseUnit
	 *            The base unit.
	 */
	public DerivedUnit(final BaseUnit baseUnit) {
		this(new BaseUnit[] { baseUnit }, new int[] { 1 }, baseUnit
				.getIdentifier());
	}

	/**
	 * Construct a derived unit from an array base units and powers. The
	 * identifier of the unit with be that of the base unit if there's only one
	 * base unit; otherwise, the identifier will be <code>null</code>.
	 * 
	 * @param baseUnits
	 *            The array of base units (e.g. {m, s}).
	 * @param powers
	 *            The array of powers (e.g. {1, -1} to create a m/s unit).
	 */
	public DerivedUnit(final BaseUnit[] baseUnits, final int[] powers) {
		this(newFactors(baseUnits, powers), baseUnits.length == 1
				? baseUnits[0].getIdentifier()
				: null);
	}

	/**
	 * Construct a derived unit from an array base units, powers, and an
	 * identifier.
	 * 
	 * @param baseUnits
	 *            The array of base units (e.g. {m, s}).
	 * @param powers
	 *            The array of powers (e.g. {1, -1} to create a m/s unit).
	 * @param identifier
	 *            Name or abbreviation for the unit. May be <code>null</code> or
	 *            empty.
	 */
	public DerivedUnit(final BaseUnit[] baseUnits, final int[] powers,
			final String identifier) {
		this(newFactors(baseUnits, powers), identifier);
	}

	/**
	 * Creates an array of Factor-s from arrays of base units and powers.
	 * 
	 * @param baseUnits
	 *            The array of base units (e.g. {m, s}).
	 * @param powers
	 *            The array of powers (e.g. {1, -1} to create a m/s unit).
	 * @return An array of {@link Factor}s equivalent to the given base units
	 *         and powers.
	 */
	protected static Factor[] newFactors(final BaseUnit[] baseUnits,
			final int[] powers) {
		final Factor[] factors = new Factor[baseUnits.length];
		for (int i = 0; i < baseUnits.length; ++i) {
			factors[i] = new Factor(baseUnits[i], powers[i]);
		}
		return factors;
	}

	/**
	 * Construct a derived unit from a derived unit and an identifier.
	 * 
	 * @param that
	 *            The derived unit.
	 * @param identifier
	 *            Name or abbreviation for the unit. May be <code>null</code> or
	 *            empty.
	 */
	public DerivedUnit(final DerivedUnit that, final String identifier) {
		this(that.factors, identifier);
	}

	/**
	 * Constructs from an array of factors. The identifier of the unit will be
	 * <code>null</code>.
	 * 
	 * @param facts
	 *            The factors for the DerivedUnit. Every factor with a power of
	 *            zero will be ignored.
	 */
	private DerivedUnit(final Factor[] facts) {
		this(facts, null);
	}

	/**
	 * Constructs from an array of factors and an identifier.
	 * 
	 * @param facts
	 *            The factors for the DerivedUnit. Every factor with a power of
	 *            zero will be ignored.
	 * @param identifier
	 *            Name or abbreviation for the unit. May be <code>null</code> or
	 *            empty.
	 */
	private DerivedUnit(final Factor[] facts, final String identifier) {
		super(identifier);
		int n = 0;
		for (int i = 0; i < facts.length; ++i) {
			final Factor fact = facts[i];
			if (fact != null && fact.power != 0) {
				n++;
			}
		}
		factors = new Factor[n];
		n = 0;
		for (int i = 0; i < facts.length; ++i) {
			final Factor fact = facts[i];
			if (fact != null && fact.power != 0) {
				factors[n++] = fact;
			}
		}
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
		for (int i = 0; i < factors.length; i++) {
			if (factors[i].power != 0 && !factors[i].baseUnit.isDimensionless()) {
				return false;
			}
		}
		return true;
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
		return new DerivedUnit(this, identifier);
	}

	/**
	 * Scale this unit by an amount.
	 * 
	 * @param amount
	 *            The amount by which to scale this unit. E.g. Unit yard =
	 *            meter.scale(0.9144);
	 * @return A unit equal this this unit scaled by the given amount.
	 * @exception UnitException
	 *                This unit cannot be scaled.
	 */
	@Override
	public Unit scale(final double amount) throws UnitException {
		return ScaledUnit.getInstance(amount, this);
	}

	/**
	 * Shift this unit by an amount.
	 * 
	 * @param offset
	 *            The amount by which to shift this unit. E.g. Unit celsius =
	 *            kelvin.shift(273.15);
	 * @return A unit equal to this unit with the origin shifted to the given
	 *         point.
	 * @exception UnitException
	 *                The unit subclass is unknown.
	 */
	@Override
	public Unit shift(final double offset) throws UnitException {
		return OffsetUnit.getInstance(offset, this);
	}

	@Override
	public Unit log(final double base) {
		return LogarithmicUnit.getInstance(base, this);
	}

	/**
	 * Raise a derived unit to a power.
	 * 
	 * @param power
	 *            The power to raise this unit by.
	 * @return The unit resulting from raising this unit to <code>power</code>.
	 * @promise The unit has not been modified.
	 */
	@Override
	public Unit pow(final int power) {
		DerivedUnit result;
		if (power == 1) {
			result = this;
		}
		else {
			final Factor[] newFactors = new Factor[factors.length];
			for (int i = 0; i < factors.length; ++i) {
				final Factor factor = factors[i];
				newFactors[i] = new Factor(factor.baseUnit, factor.power
						* power);
			}
			result = new DerivedUnit(newFactors);
		}
		return result;
	}

	/**
	 * Returns the N-th root of this unit.
	 * 
	 * @param root
	 *            The root to take (e.g. 2 means square root). May not be zero.
	 * @return The unit corresponding to the <code>root</code>-th root of this
	 *         unit.
	 * @promise The unit has not been modified.
	 * @throws IllegalArgumentException
	 *             <code>root</code> is zero or the result would have a
	 *             non-integral unit dimension.
	 */
	@Override
	public Unit root(final int root) throws IllegalArgumentException {
		if (root == 0) {
			throw new IllegalArgumentException(getClass().getName()
					+ ".root(int): zero root");
		}
		DerivedUnit result;
		if (root == 1) {
			result = this;
		}
		else {
			final Factor[] newFactors = new Factor[factors.length];
			for (int i = 0; i < factors.length; ++i) {
				final Factor factor = factors[i];
				if (factor.power % root != 0) {
					throw new IllegalArgumentException(getClass().getName()
							+ ".root(int): "
							+ "Non-integral resulting dimension");
				}
				newFactors[i] = new Factor(factor.baseUnit, factor.power / root);
			}
			result = new DerivedUnit(newFactors);
		}
		return result;
	}

	/**
	 * Raise a derived unit to a power.
	 * 
	 * @param power
	 *            The power to raise this unit by. If this unit is not
	 *            dimensionless, then the value must be an integer or the
	 *            reciprocal of an integer.
	 * @return The unit resulting from raising this unit to <code>power</code>.
	 * @throws IllegalArgumentException
	 *             This unit is not dimensionless and <code>power
     *                          </code>
	 *             is neither an integer nor the reciprocal of an integer.
	 * @promise The unit has not been modified.
	 */
	@Override
	public Unit pow(final double power) throws IllegalArgumentException {
		Unit result;
		if (factors.length == 0) {
			result = this;
		}
		else {
			if (Math.abs(power) > 1) {
				final double intVal = Math.rint(power);
				if (power < ChoiceFormat.previousDouble(intVal)
						|| power > ChoiceFormat.nextDouble(intVal)) {
					throw new IllegalArgumentException(this.getClass()
							.getName()
							+ ".pow(double): " + "Non-integral power");
				}
				result = pow((int) intVal);
			}
			else {
				final double root = 1. / power;
				final double intVal = Math.rint(root);
				if (root < ChoiceFormat.previousDouble(intVal)
						|| root > ChoiceFormat.nextDouble(intVal)) {
					throw new IllegalArgumentException(this.getClass()
							.getName()
							+ ".pow(double): "
							+ "Non-integral reciprocal power");
				}
				result = root((int) intVal);
			}
		}
		return result;
	}

	/**
	 * Return the definition of this unit.
	 * 
	 * @return The definition of this unit (e.g. "m.s-1").
	 */
	@Override
	public String getDefinition() {
		String definition;

		if (factors == null) {
			/* Probably exception thrown during construction */
			definition = "<unconstructed DerivedUnit>";
		}
		else {
			final StringBuffer buf = new StringBuffer(80);

			for (int i = 0; i < factors.length; ++i) {
				if (factors[i].power == 1) {
					buf.append(factors[i].baseUnit.toString() + ".");
				}
				else if (factors[i].power != 0) {
					buf.append(factors[i].baseUnit.toString()
							+ factors[i].power + ".");
				}
			}

			if (buf.length() > 0) {
				buf.setLength(buf.length() - 1);
			}

			definition = buf.toString();
		}
		return definition;
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
		final BaseUnit meter = SI.meter;
		final BaseUnit second = SI.second;
		final DerivedUnit speed = new DerivedUnit(new BaseUnit[] { meter,
				second }, new int[] { 1, -1 });
		final Unit speed2 = speed.pow(2);

		myAssert(!speed.equals(meter));
		myAssert(!speed.equals(second));
		myAssert(!speed.isConvertible(meter));
		myAssert(!speed.isConvertible(second));

		myAssert(speed, meter.divide(second));
		myAssert(!speed.equals(speed2));
		myAssert(!speed.isConvertible(speed2));
		myAssert(speed, speed2.sqrt());
		myAssert(speed, speed2.root(2));
		myAssert(speed.pow(2.0 + Math.ulp(2.0)), speed2);
		myAssert(speed2.pow(1 / (2.0 + Math.ulp(2.0))), speed);

		myAssert(meter.divide(speed), second);
		myAssert(speed.divide(meter), second.pow(-1));

		myAssert(speed.toThis(5, speed), 5);
		myAssert(speed.toThat(5, speed), 5);

		final double[] values = { 1, 2 };

		myAssert(speed.toThis(values, speed), values);
		myAssert(speed.toThat(values, speed), values);

		final DerivedUnit joule = (DerivedUnit) speed.pow(2).multiply(
				SI.kilogram);

		myAssert(joule.equals(joule));
		myAssert(joule.isConvertible(joule));
		myAssert(!joule.equals(meter));
		myAssert(!joule.isConvertible(meter));

		System.out.println("Checking exceptions:");
		try {
			speed.toThis(5, joule);
			throw new AssertionError();
		}
		catch (final UnitException e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("speed.pow(2+2*ULP)=\""
					+ speed.pow(ChoiceFormat.nextDouble(ChoiceFormat
							.nextDouble(2.0))) + "\"");
			throw new AssertionError();
		}
		catch (final IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("speed2.pow(1/(2+2*ULP))=\""
					+ speed2.pow(1 / ChoiceFormat.nextDouble(ChoiceFormat
							.nextDouble(2.0))) + "\"");
			throw new AssertionError();
		}
		catch (final IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done");
	}

	/*
	 * PROMISE: None of the returned factors will have a power of zero and input
	 * dimensionless factors will be ignored and not appear in the output.
	 */
	private Vector[] common(final DerivedUnit that) {
		final Vector[] vector = new Vector[3];
		final int max = factors.length + that.factors.length;

		vector[0] = new Vector(max);
		vector[1] = new Vector(max);
		vector[2] = new Vector(max);

		for (int i = 0; i < factors.length; ++i) {
			if (factors[i].power != 0 && !factors[i].baseUnit.isDimensionless()) {
				int j;

				for (j = 0; j < that.factors.length; ++j) {
					if (that.factors[j].power != 0
							&& factors[i].baseUnit
									.equals(that.factors[j].baseUnit)) {
						vector[2].addElement(new Factor[] { factors[i],
								that.factors[j] });
						break;
					}
				}
				if (j == that.factors.length) {
					vector[0].addElement(factors[i]);
				}
			}
		}

		for (int j = 0; j < that.factors.length; ++j) {
			if (that.factors[j].power != 0
					&& !that.factors[j].baseUnit.isDimensionless()) {
				int i;

				for (i = 0; i < factors.length; ++i) {
					if (factors[i].power != 0
							&& factors[i].baseUnit
									.equals(that.factors[j].baseUnit)) {
						break;
					}
				}
				if (i == factors.length) {
					vector[1].addElement(that.factors[j]);
				}
			}
		}

		return vector;
	}

	static abstract class Op {
		public DerivedUnit multOp(final DerivedUnit d1, final DerivedUnit d2) {
			final Vector[] comm = d1.common(d2);
			final int n0 = comm[0].size();
			final int n1 = comm[1].size();
			final int n2 = comm[2].size();
			final Factor[] factors = new Factor[n0 + n1 + n2];
			int k = 0;

			for (int i = 0; i < n0; ++i) {
				factors[k++] = (Factor) comm[0].elementAt(i);
			}

			for (int i = 0; i < n1; ++i) {
				factors[k++] = op((Factor) comm[1].elementAt(i));
			}

			for (int i = 0; i < n2; ++i) {
				final Factor[] facts = (Factor[]) comm[2].elementAt(i);
				factors[k++] = op(facts[0], facts[1]);
			}

			return new DerivedUnit(factors);
		}

		protected abstract Factor op(Factor factor);

		protected abstract Factor op(Factor f1, Factor f2);
	}

	private static final class AddPow extends Op {
		@Override
		protected Factor op(final Factor factor) {
			return factor;
		}

		@Override
		protected Factor op(final Factor f1, final Factor f2) {
			return new Factor(f1.baseUnit, f1.power + f2.power);
		}
	}

	private static final class SubPow extends Op {
		@Override
		protected Factor op(final Factor factor) {
			return new Factor(factor.baseUnit, -factor.power);
		}

		@Override
		protected Factor op(final Factor f1, final Factor f2) {
			return new Factor(f1.baseUnit, f1.power - f2.power);
		}
	}

	/**
	 * Instantiations of the inner "helper" classes.
	 */
	private static AddPow	addPow	= new AddPow();
	private static SubPow	subPow	= new SubPow();

	/**
	 * Multiply a derived unit by another unit.
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
		return that instanceof DerivedUnit
				? multiply((DerivedUnit) that)
				: that.multiply(this);
	}

	/**
	 * Multiply a derived unit by a derived unit.
	 * 
	 * @param that
	 *            The derived unit with which to multiply this unit.
	 * @return The product of the two units.
	 * @promise Neither unit has been modified.
	 */
	public Unit multiply(final DerivedUnit that) {
		return addPow.multOp(this, that);
	}

	/**
	 * Divide a derived unit by another unit.
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
		return that instanceof DerivedUnit
				? divide((DerivedUnit) that)
				: that.divideInto(this);
	}

	/**
	 * Divide a derived unit by a derived unit.
	 * 
	 * @param that
	 *            The derived unit to divide into this unit.
	 * @return The quotient of the two units.
	 * @promise Neither unit has been modified.
	 */
	Unit divide(final DerivedUnit that) {
		return subPow.multOp(this, that);
	}

	/**
	 * Divide a derived unit into another unit.
	 * 
	 * @param that
	 *            The unit to be divided by this unit.
	 * @return The quotient of the two units.
	 * @promise Neither unit has been modified.
	 * @throws UnitException
	 *             Meaningless operation.
	 */
	@Override
	protected Unit divideInto(final Unit that) throws UnitException {
		return that.divide(this);
	}

	/**
	 * Indicate whether or not this unit has the same dimensionality as a
	 * derived unit.
	 * 
	 * @param that
	 *            The derived unit.
	 * @return <code>true</code> if and only if both units have the same
	 *         dimensionality (e.g. Length/Time).
	 */
	boolean sameDimensionality(final DerivedUnit that) {
		final Vector[] comm = common(that);

		if (comm[0].size() != 0 || comm[1].size() != 0) {
			return false;
		}

		final int n2 = comm[2].size();

		for (int i = 0; i < n2; ++i) {
			final Factor[] factors = (Factor[]) comm[2].elementAt(i);

			if (factors[0].power != factors[1].power) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Indicate whether or not this unit has the reciprocal dimensionality of a
	 * derived unit.
	 * 
	 * @param that
	 *            The derived unit.
	 * @return <code>true</code> if and only if the unit dimensionalities are
	 *         reciprocals of each other (e.g. Length/Time and Time/Length).
	 */
	boolean reciprocalDimensionality(final DerivedUnit that) {
		final Vector[] comm = common(that);

		if (comm[0].size() != 0 || comm[1].size() != 0) {
			return false;
		}

		final int n2 = comm[2].size();

		for (int i = 0; i < n2; ++i) {
			final Factor[] factors = (Factor[]) comm[2].elementAt(i);

			if (factors[0].power != -factors[1].power) {
				return false;
			}
		}

		return true;
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
		if (that instanceof PromiscuousUnit) {
			final double[] newValues = (copy)
					? (double[]) values.clone()
					: values;
			return newValues;
		}
		return that instanceof DerivedUnit
				? toThis(values, (DerivedUnit) that, copy)
				: that.toThat(values, this);
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
	public float[] toThis(final float[] values, final Unit that,
			final boolean copy) throws UnitException {
		if (that instanceof PromiscuousUnit) {
			final float[] newValues = (copy)
					? (float[]) values.clone()
					: values;
			return newValues;
		}
		return that instanceof DerivedUnit
				? toThis(values, (DerivedUnit) that, copy)
				: that.toThat(values, this);
	}

	/**
	 * Convert values to this unit from a derived unit.
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
	double[] toThis(final double[] values, final DerivedUnit that)
			throws UnitException {
		return toThis(values, that, true);
	}

	/**
	 * Convert values to this unit from a derived unit.
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
	float[] toThis(final float[] values, final DerivedUnit that)
			throws UnitException {
		return toThis(values, that, true);
	}

	/**
	 * Convert values to this unit from a derived unit.
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
	double[] toThis(final double[] values, final DerivedUnit that,
			final boolean copy) throws UnitException {
		double[] newValues;

		if (sameDimensionality(that)) {
			newValues = (copy)
					? (double[]) values.clone()
					: values;

		}
		else if (reciprocalDimensionality(that)) {
			newValues = (copy)
					? (double[]) values.clone()
					: values;

			for (int i = 0; i < values.length; ++i) {
				if (values[i] == values[i]) {
					newValues[i] = 1.0 / values[i];
				}
				else {
					newValues[i] = Double.NaN;
				}
			}
		}
		else {
			throw new UnitException("Attempt to convert from unit \"" + that
					+ "\" to unit \"" + this + "\"");
		}

		return newValues;
	}

	/**
	 * Convert values to this unit from a derived unit.
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
	float[] toThis(final float[] values, final DerivedUnit that,
			final boolean copy) throws UnitException {
		float[] newValues;

		if (sameDimensionality(that)) {
			newValues = (copy)
					? (float[]) values.clone()
					: values;
		}
		else if (reciprocalDimensionality(that)) {
			newValues = (copy)
					? (float[]) values.clone()
					: values;

			for (int i = 0; i < values.length; ++i) {
				newValues[i] = 1.0f / values[i];
				if (values[i] == values[i]) {
					newValues[i] = 1.0f / values[i];
				}
				else {
					newValues[i] = Float.NaN;
				}
			}
		}
		else {
			throw new UnitException("Attempt to convert from unit \"" + that
					+ "\" to unit \"" + this + "\"");
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
	 * Convert values from this unit to a derived unit.
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
	double[] toThat(final double values[], final DerivedUnit that)
			throws UnitException {
		return that.toThis(values, this, true);
	}

	/**
	 * Convert values from this unit to a derived unit.
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
	float[] toThat(final float values[], final DerivedUnit that)
			throws UnitException {
		return that.toThis(values, this, true);
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
	 * @require The units are convertible.
	 * @promise Neither unit has been modified.
	 * @throws UnitException
	 *             The units are not convertible.
	 */
	@Override
	public double[] toThat(final double values[], final Unit that,
			final boolean copy) throws UnitException {
		if (that instanceof PromiscuousUnit) {
			final double[] newValues = (copy)
					? (double[]) values.clone()
					: values;
			return newValues;
		}
		return that instanceof DerivedUnit
				? toThat(values, (DerivedUnit) that, copy)
				: that.toThis(values, this, copy);
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
	 * @require The units are convertible.
	 * @promise Neither unit has been modified.
	 * @throws UnitException
	 *             The units are not convertible.
	 */
	@Override
	public float[] toThat(final float values[], final Unit that,
			final boolean copy) throws UnitException {
		if (that instanceof PromiscuousUnit) {
			final float[] newValues = (copy)
					? (float[]) values.clone()
					: values;
			return newValues;
		}
		return that instanceof DerivedUnit
				? toThat(values, (DerivedUnit) that, copy)
				: that.toThis(values, this, copy);
	}

	/**
	 * Convert values from this unit to a derived unit.
	 * 
	 * @param values
	 *            The values to be converted in units of this unit.
	 * @param that
	 *            The unit to which to convert the values.
	 * @param copy
	 *            if false and <code>that</code> equals this, return
	 *            <code>values</code>, else return a new array
	 * @return The converted values.
	 * @require The units are convertible.
	 * @promise Neither unit has been modified.
	 * @throws UnitException
	 *             The units are not convertible.
	 */
	double[] toThat(final double values[], final DerivedUnit that,
			final boolean copy) throws UnitException {
		return that.toThis(values, this, copy);
	}

	/**
	 * Convert values from this unit to a derived unit.
	 * 
	 * @param values
	 *            The values to be converted in units of this unit.
	 * @param that
	 *            The unit to which to convert the values.
	 * @param copy
	 *            if false and <code>that</code> equals this, return
	 *            <code>values</code>, else return a new array
	 * @return The converted values.
	 * @require The units are convertible.
	 * @promise Neither unit has been modified.
	 * @throws UnitException
	 *             The units are not convertible.
	 */
	float[] toThat(final float values[], final DerivedUnit that,
			final boolean copy) throws UnitException {
		return that.toThis(values, this, copy);
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
		boolean isConvertible;
		if (unit == null) {
			isConvertible = false;
		}
		else {
			if (unit instanceof DerivedUnit) {
				final DerivedUnit that = (DerivedUnit) unit;
				isConvertible = sameDimensionality(that)
						|| reciprocalDimensionality(that);
			}
			else {
				isConvertible = unit.isConvertible(this);
			}
		}
		return isConvertible;
	}

	// added by WLH 11 Feb 98
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
		if (!(unit instanceof DerivedUnit)) {
			return unit == null ? false : unit.equals(this);
		}
		final int n = factors.length;
		if (n != ((DerivedUnit) unit).factors.length) {
			return false;
		}
		final boolean[] mark = new boolean[n];
		for (int j = 0; j < n; j++) {
			mark[j] = false;
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (!mark[j]) {
					if (factors[i].equals(((DerivedUnit) unit).factors[j])) {
						mark[j] = true;
						break;
					}
				}
			}
		}
		for (int j = 0; j < n; j++) {
			if (!mark[j]) {
				return false;
			}
		}
		return true;
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
			for (int i = 0; i < factors.length; i++) {
				hashCode ^= factors[i].hashCode();
			}
		}
		return hashCode;
	}

	@Override
	public DerivedUnit getDerivedUnit() {
		return this;
	}
}
