
//
// DerivedUnit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DerivedUnit.java,v 1.9 1999-08-26 20:43:15 steve Exp $
 */

package visad;

import java.text.ChoiceFormat;
import java.util.Vector;
import java.io.Serializable;


/**
 * A class that represents a unit consisting of zero or more base units.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class DerivedUnit
    extends	Unit
    implements	Serializable
{
    /**
     * The array of individual base-unit factors that make up this unit.
     *
     * The following is effectively "final" in that it is only set in
     * constructors and never altered.  Unfortunately, the JDK 1.1.2
     * javac(1) compiler on our SunOS 5.5 systems doesn't recognize this
     * fact; hence, the "final" is commented-out.
     */
    /*final*/ Factor[]	factors;


    /**
     * Construct a dimensionless derived unit.  The identifier of the unit will
     * be empty.
     */
    public DerivedUnit()
    {
	this(new BaseUnit[] {}, new int[] {}, "");
    }

    /**
     * Construct a dimensionless derived unit with an identifier.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     */
    public DerivedUnit(String identifier)
    {
	this(new BaseUnit[] {}, new int[] {}, identifier);
    }

    /**
     * Construct a derived unit from a base unit.  The identifier of the unit
     * will be that of the base unit.
     *
     * @param baseUnit	The base unit.
     */
    public DerivedUnit(BaseUnit baseUnit)
    {
	this(new BaseUnit[] {baseUnit}, new int[] {1},
	    baseUnit.getIdentifier());
    }

    /**
     * Construct a derived unit from an array base units and powers.  The 
     * identifier of the unit with be that of the base unit if there's only
     * one base unit; otherwise, the identifier will be <code>null</code>.
     *
     * @param baseUnits	The array of base units (e.g. {m, s}).
     * @param powers	The array of powers (e.g. {1, -1} to create a
     *			m/s unit).
     */
    public DerivedUnit(BaseUnit[] baseUnits, int[] powers)
    {
	this(newFactors(baseUnits, powers), 
	  baseUnits.length == 1 ? baseUnits[0].getIdentifier() : null);
    }

    /**
     * Construct a derived unit from an array base units, powers, and an
     * identifier.
     *
     * @param baseUnits		The array of base units (e.g. {m, s}).
     * @param powers		The array of powers (e.g. {1, -1} to create a
     *				m/s unit).
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     */
    public DerivedUnit(BaseUnit[] baseUnits, int[] powers, String identifier)
    {
	this(newFactors(baseUnits, powers), identifier);
    }

    /**
     * Creates an array of Factor-s from arrays of base units and powers.
     *
     * @param baseUnits		The array of base units (e.g. {m, s}).
     * @param powers		The array of powers (e.g. {1, -1} to create a
     *				m/s unit).
     */
    protected static Factor[]
    newFactors(BaseUnit[] baseUnits, int[] powers)
    {
	Factor[]	factors = new Factor[baseUnits.length];
	for (int i = 0; i < baseUnits.length; ++i)
	    factors[i] = new Factor(baseUnits[i], powers[i]);
	return factors;
    }

    /**
     * Construct a derived unit from a derived unit.  The identifier will be
     * that of the input derived unit.
     *
     * @param that	The derived unit.
     */
    public DerivedUnit(DerivedUnit that)
    {
	this(that.factors, that.getIdentifier());
    }

    /**
     * Construct a derived unit from a derived unit and an identifier.
     *
     * @param that		The derived unit.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     */
    public DerivedUnit(DerivedUnit that, String identifier)
    {
	this(that.factors, identifier);
    }

    /**
     * Constructs from an array of factors.  The identifier of the unit will be
     * <code>null</code>.
     *
     * @param facts		The factors for the DerivedUnit.  Every factor
     *				with a power of zero will be ignored.
     */
    private DerivedUnit(Factor[] facts)
    {
	this(facts, null);
    }

    /**
     * Constructs from an array of factors and an identifier.
     *
     * @param facts		The factors for the DerivedUnit.  Every factor
     *				with a power of zero will be ignored.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     */
    private DerivedUnit(Factor[] facts, String identifier)
    {
	super(identifier);
	int	n = 0;
	for (int i = 0; i < facts.length; ++i)
	{
	    Factor	fact = facts[i];
	    if (fact != null && fact.power != 0)
		n++;
	}
	factors = new Factor[n];
	n = 0;
	for (int i = 0; i < facts.length; ++i)
	{
	    Factor	fact = facts[i];
	    if (fact != null && fact.power != 0)
		factors[n++] = fact;
	}
    }

    /**
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    public Unit clone(String identifier)
    {
      return new DerivedUnit(this, identifier);
    }

    /**
     * Raise a derived unit to a power.
     *
     * @param power	The power to raise this unit by.
     * @return		The unit resulting from raising this unit to 
     *			<code>power</code>.
     * @promise		The unit has not been modified.
     */
    public Unit pow(int power)
    {
	DerivedUnit	result;
	if (power == 1)
	{
	  result = this;
	}
	else
	{
	  Factor[]	newFactors = new Factor[factors.length];
	  for (int i = 0; i < factors.length; ++i)
	  {
	      Factor	factor = factors[i];
	      newFactors[i] = new Factor(factor.baseUnit, factor.power*power);
	  }
	  result = new DerivedUnit(newFactors);
	}
	return result;
    }

    /**
     * Raise a derived unit to a power.
     *
     * @param power		The power to raise this unit by.  If this unit 
     *				is not dimensionless, then the value must be
     *				integral.
     * @return			The unit resulting from raising this unit to 
     *				<code>power</code>.
     * @throws IllegalArgumentException
     *				This unit is not dimensionless and <code>power
     *				</code> has a non-integral value.
     * @promise			The unit has not been modified.
     */
    public Unit pow(double power)
	throws IllegalArgumentException
    {
	Unit	result;
	if (factors.length == 0)
	{
	    result = this;
	}
	else
	{
	    double	intVal = Math.rint(power);
	    if (power < ChoiceFormat.previousDouble(intVal) ||
		power > ChoiceFormat.nextDouble(intVal))
	    {
		throw new IllegalArgumentException(this.getClass().getName() +
		    ".pow(double): non-integral power");
	    }
	    result = pow((int)intVal);
	}
	return result;
    }

    /**
     * Return the definition of this unit.
     *
     * @return          The definition of this unit (e.g. "m.s-1").
     */
    public String getDefinition()
    {
	StringBuffer	buf = new StringBuffer(80);

	for (int i = 0; i < factors.length; ++i)
	{
	    if (factors[i].power == 1)
		buf.append(factors[i].baseUnit.toString() + ".");
	    else
	    if (factors[i].power != 0)
		buf.append(factors[i].baseUnit.toString() +
		    factors[i].power + ".");
	}

	if (buf.length() > 0)
	    buf.setLength(buf.length()-1);

	return buf.toString();
    }

    /**
     * Test this class.
     *
     * @param args		Arguments (ignored).
     * @exception UnitException	A problem occurred.
     */
    public static void main(String[] args)
	throws UnitException
    {
	BaseUnit	meter = SI.meter;
	BaseUnit	second = SI.second;
	DerivedUnit	speed = new DerivedUnit(new BaseUnit[] {meter, second},
						new int[] {1, -1});

	System.out.println("speed=\"" + speed + "\"");
	System.out.println("speed.pow(2)=\"" + speed.pow(2) + "\"");
	System.out.println("speed.pow(2.0+ULP)=\"" + 
	    speed.pow(ChoiceFormat.nextDouble(2.0)) + "\"");

	System.out.println("speed*meter=\"" + speed.multiply(meter) + "\"");
	System.out.println("meter*speed=\"" + meter.multiply(speed) + "\"");

	System.out.println("speed/meter=\"" + speed.divide(meter) + "\"");
	System.out.println("meter/speed=\"" + meter.divide(speed) + "\"");

	System.out.println("speed.toThis(5,speed)=" +
	    speed.toThis(5,speed));

	System.out.println("speed.toThat(5,speed)=" +
	    speed.toThat(5,speed));

	double[] values;

	values = speed.toThis(new double[] {1,2},speed);
	System.out.println("speed.toThis({1,2},speed)=" +
	    values[0] + "," + values[1]);

	values = speed.toThat(new double[] {1,2},speed);
	System.out.println("speed.toThat({1,2},speed)=" +
	    values[0] + "," + values[1]);

	DerivedUnit	energy = (DerivedUnit)speed.pow(2).multiply(
			     SI.kilogram);

	System.out.println("energy=\"" + energy + "\"");

	System.out.println("Checking exceptions:");
	try
	{
	    speed.toThis(5,energy);
	    System.err.println("ERROR: energy -> speed");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
	try
	{
	    System.out.println("speed.pow(2+2*ULP)=\"" + 
		speed.pow(
		  ChoiceFormat.nextDouble(ChoiceFormat.nextDouble(2.0))) 
		+ "\"");
	    System.err.println("ERROR: IllegalArgumentException not thrown!");
	    System.exit(1);
	}
	catch (IllegalArgumentException e)
	{
	    System.out.println(e.getMessage());
	}
    }

    /*
     * PROMISE: None of the returned factors will have a power of zero.
     */
    private Vector[] common(DerivedUnit that)
    {
	Vector[]	vector = new Vector[3];
	int		max = factors.length + that.factors.length;

	vector[0] = new Vector(max);
	vector[1] = new Vector(max);
	vector[2] = new Vector(max);

	for (int i = 0; i < factors.length; ++i)
	{
	    if (factors[i].power != 0)
	    {
		int	j;

		for (j = 0; j < that.factors.length; ++j)
		{
		    if (that.factors[j].power != 0 &&
			factors[i].baseUnit.equals(that.factors[j].baseUnit))
		    {
			vector[2].addElement(new Factor[] {factors[i],
							   that.factors[j]});
			break;
		    }
		}
		if (j == that.factors.length)
		    vector[0].addElement(factors[i]);
	    }
	}

	for (int j = 0; j < that.factors.length; ++j)
	{
	    if (that.factors[j].power != 0)
	    {
		int	i;

		for (i = 0; i < factors.length; ++i)
		{
		    if (factors[i].power != 0 &&
			factors[i].baseUnit.equals(that.factors[j].baseUnit))
		    {
			break;
		    }
		}
		if (i == factors.length)
		    vector[1].addElement(that.factors[j]);
	    }
	}

	return vector;
    }

    static abstract class Op
    {
	public DerivedUnit multOp(DerivedUnit d1, DerivedUnit d2)
	{
	    Vector[]	comm = d1.common(d2);
	    int		n0 = comm[0].size();
	    int		n1 = comm[1].size();
	    int		n2 = comm[2].size();
	    Factor[]	factors = new Factor[n0+n1+n2];
	    int		k = 0;

	    for (int i = 0; i < n0; ++i)
		factors[k++] = (Factor)comm[0].elementAt(i);

	    for (int i = 0; i < n1; ++i)
		factors[k++] = op((Factor)comm[1].elementAt(i));

	    for (int i = 0; i < n2; ++i)
	    {
		Factor[]	facts = (Factor[])comm[2].elementAt(i);
		factors[k++] = op(facts[0], facts[1]);
	    }

	    return new DerivedUnit(factors);
	}

	protected abstract Factor op(Factor factor);

	protected abstract Factor op(Factor f1, Factor f2);
    }

    private static final class AddPow
	extends	Op
    {
	protected Factor op(Factor factor)
	{
	    return factor;
	}

	protected Factor op(Factor f1, Factor f2)
	{
	    return new Factor(f1.baseUnit, f1.power + f2.power);
	}
    }

    private static final class SubPow
	extends	Op
    {
	protected Factor op(Factor factor)
	{
	    return new Factor(factor.baseUnit, -factor.power);
	}

	protected Factor op(Factor f1, Factor f2)
	{
	    return new Factor(f1.baseUnit, f1.power - f2.power);
	}
    }

    /**
     * Instantiations of the inner "helper" classes.
     */
    private static AddPow	addPow = new AddPow();
    private static SubPow	subPow = new SubPow();

    /**
     * Multiply a derived unit by another unit.
     *
     * @param that	The unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     * @throws UnitException	Meaningless operation.
     */
    public Unit multiply(Unit that)
	throws UnitException
    {
	return
	  that instanceof DerivedUnit
	    ? multiply((DerivedUnit)that)
	    : that.multiply(this);
    }

    /**
     * Multiply a derived unit by a derived unit.
     *
     * @param that	The derived unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit multiply(DerivedUnit that)
    {
	return addPow.multOp(this, that);
    }

    /**
     * Divide a derived unit by another unit.
     *
     * @param that      The unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     * @throws UnitException	Meaningless operation.
     */
    public Unit divide(Unit that)
	throws UnitException
    {
	return
	    that instanceof DerivedUnit
		? divide((DerivedUnit)that)
		: that.divideInto(this);
    }

    /**
     * Divide a derived unit by a derived unit.
     *
     * @param that      The derived unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    Unit divide(DerivedUnit that)
    {
	return subPow.multOp(this, that);
    }

    /**
     * Divide a derived unit into another unit.
     *
     * @param that      The unit to be divided by this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     * @throws UnitException	Meaningless operation.
     */
    protected Unit divideInto(Unit that)
	throws UnitException
    {
	return that.divide(this);
    }

    /**
     * Indicate whether or not this unit has the same dimensionality as
     * a derived unit.
     *
     * @param that	The derived unit.
     * @return		<code>true</code> if and only if both units have the
     *			same dimensionality (e.g. Length/Time).
     */
    boolean sameDimensionality(DerivedUnit that)
    {
	Vector[]	comm = common(that);

	if (comm[0].size() != 0 || comm[1].size() != 0)
	    return false;

	int	n2 = comm[2].size();

	for (int i = 0; i < n2; ++i)
	{
	    Factor[]	factors =	(Factor[])comm[2].elementAt(i);

	    if (factors[0].power != factors[1].power)
		return false;
	}

	return true;
    }

    /**
     * Indicate whether or not this unit has the reciprocal dimensionality of
     * a derived unit.
     *
     * @param that	The derived unit.
     * @return		<code>true</code> if and only if the unit 
     *			dimensionalities are reciprocals of each other
     *			(e.g. Length/Time and Time/Length).
     */
    boolean reciprocalDimensionality(DerivedUnit that)
    {
	Vector[]	comm = common(that);

	if (comm[0].size() != 0 || comm[1].size() != 0)
	    return false;

	int	n2 = comm[2].size();

	for (int i = 0; i < n2; ++i)
	{
	    Factor[]	factors =	(Factor[])comm[2].elementAt(i);

	    if (factors[0].power != -factors[1].power)
		return false;
	}

	return true;
    }

    /**
     * Convert values to this unit from a base unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, BaseUnit that)
	throws UnitException
    {
	return toThis(values, new DerivedUnit(that));
    }

    float[] toThis(float[] values, BaseUnit that)
        throws UnitException
    {
        return toThis(values, new DerivedUnit(that));
    }

    /**
     * Convert values to this unit from a derived unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, DerivedUnit that)
	throws UnitException
    {
	double[] newValues;

	if (sameDimensionality(that))
	{
	    newValues = new double[values.length];

	    for (int i = 0; i < values.length; ++i)
		newValues[i] = values[i];
	}
	else if (reciprocalDimensionality(that))
	{
	    newValues = new double[values.length];

	    for (int i = 0; i < values.length; ++i)
		newValues[i] = 1.0 / values[i];
	}
	else
	    throw new UnitException("Attempt to convert from unit \"" +
		that + "\" to unit \"" + this + "\"");

	return newValues;
    }

    float[] toThis(float[] values, DerivedUnit that)
        throws UnitException
    {
        float[] newValues;
 
        if (sameDimensionality(that))
        {
            newValues = new float[values.length];
 
            for (int i = 0; i < values.length; ++i)
                newValues[i] = values[i];
        }
        else if (reciprocalDimensionality(that))
        {
            newValues = new float[values.length];
 
            for (int i = 0; i < values.length; ++i)
                newValues[i] = 1.0f / values[i];
        }
        else
            throw new UnitException("Attempt to convert from unit \"" +
                that + "\" to unit \"" + this + "\"");
 
        return newValues;
    }

    /**
     * Convert values to this unit from a scaled unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, ScaledUnit that)
	throws UnitException
    {
	return that.toThat(values, this);
    }

    float[] toThis(float[] values, ScaledUnit that)
        throws UnitException
    {
        return that.toThat(values, this);
    }

    /**
     * Convert values to this unit from a offset unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, OffsetUnit that)
	throws UnitException
    {
	return that.toThat(values, this);
    }

    float[] toThis(float[] values, OffsetUnit that)
        throws UnitException
    {
        return that.toThat(values, this);
    }

    /**
     * Convert values from this unit to a base unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double values[], BaseUnit that)
	throws UnitException
    {
	return new DerivedUnit(that).toThis(values, this);
    }

    float[] toThat(float values[], BaseUnit that)
        throws UnitException
    {
        return new DerivedUnit(that).toThis(values, this);
    }

    /**
     * Convert values from this unit to a derived unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double values[], DerivedUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

    float[] toThat(float values[], DerivedUnit that)
        throws UnitException
    {
        return that.toThis(values, this);
    }

    /**
     * Convert values from this unit to a scaled unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double values[], ScaledUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

    float[] toThat(float values[], ScaledUnit that)
        throws UnitException
    {
        return that.toThis(values, this);
    }

    /**
     * Convert values from this unit to a offset unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double values[], OffsetUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

    float[] toThat(float values[], OffsetUnit that)
        throws UnitException
    {
        return that.toThis(values, this);
    }

  /** added by WLH 11 Feb 98 */
  public boolean equals(Unit unit) {
    if (unit == null)
      return false;

    if (unit instanceof BaseUnit)
      return equals(new DerivedUnit((BaseUnit)unit));

    if (!(unit instanceof DerivedUnit))
      return unit.equals(this);

    int n = factors.length;
    if (n != ((DerivedUnit) unit).factors.length) return false;
    boolean[] mark = new boolean[n];
    for (int j=0; j<n; j++) mark[j] = false;
    for (int i=0; i<n; i++) {
      for (int j=0; j<n; j++) {
	if (!mark[j]) {
	  if (factors[i].equals(((DerivedUnit) unit).factors[j])) {
	    mark[j] = true;
	    break;
	  }
	}
      }
    }
    for (int j=0; j<n; j++) {
      if (!mark[j]) return false;
    }
    return true;
  }

}

