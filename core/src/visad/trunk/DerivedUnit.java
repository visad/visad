
//
// DerivedUnit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DerivedUnit.java,v 1.4 1998-03-14 12:56:59 billh Exp $
 */

package visad;

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
    private /*final*/ Factor[]	factors;


    /**
     * Construct a dimensionless derived unit.
     */
    public DerivedUnit()
    {
	factors = new Factor[0];
    }

    /**
     * Construct a derived unit from a base unit.
     *
     * @param baseUnit	The base unit.
     */
    public DerivedUnit(BaseUnit baseUnit)
    {
	factors = new Factor[1];
	factors[0] = new Factor(baseUnit, 1);
    }

    /**
     * Construct a derived unit from an array base units and powers.
     *
     * @param baseUnits	The array of base units (e.g. {meter, second}).
     * @param powers	The array of powers (e.g. {1, -1} to create a
     *			meter/second unit).
     */
    public DerivedUnit(BaseUnit[] baseUnits, int[] powers)
    {
	factors = new Factor[baseUnits.length];

	for (int i = 0; i < baseUnits.length; ++i)
	    factors[i] = new Factor(baseUnits[i], powers[i]);
    }

    /**
     * Construct a derived unit from a derived unit.
     *
     * @param that	The derived unit.
     */
    public DerivedUnit(DerivedUnit that)
    {
	factors = new Factor[that.factors.length];

	for (int i = 0; i < that.factors.length; ++i)
	    factors[i] = new Factor(that.factors[i].baseUnit, 
				    that.factors[i].power);
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
	DerivedUnit	newUnit = new DerivedUnit(factors.length);

	for (int i = 0; i < factors.length; ++i)
	    newUnit.factors[i] = new Factor(factors[i].baseUnit,
					   factors[i].power * power);

	return newUnit;
    }

    /**
     * Return a string representation of this unit.
     *
     * @return          A string representation of this unit (e.g. 
     *			"meter/second").
     * @promise		The unit has not been modified.
     */
    public String toString()
    {
	StringBuffer	buf = new StringBuffer(80);

	for (int i = 0; i < factors.length; ++i)
	{
	    if (factors[i].power == 1)
		buf.append(factors[i].baseUnit.unitName() + " ");
	    else
	    if (factors[i].power != 0)
		buf.append(factors[i].baseUnit.unitName() + "^" + 
		    factors[i].power + " ");
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
	BaseUnit	meter = BaseUnit.addBaseUnit("Length", "meter");
	BaseUnit	second = BaseUnit.addBaseUnit("Time", "second");
	Unit		speed = new DerivedUnit(new BaseUnit[] {meter, second},
						new int[] {1, -1});

	System.out.println("speed=\"" + speed + "\"");
	System.out.println("speed.pow(2)=\"" + speed.pow(2) + "\"");

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
			     BaseUnit.addBaseUnit("Mass", "kilogram"));

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
    }

    /**
     * Construct a derived unit from the number of base units it will contain.
     */
    DerivedUnit(int n)
    {
	factors = new Factor[n];
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
		    if (factors[i].baseUnit.equals(that.factors[j].baseUnit))
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
		    if (factors[i].baseUnit.equals(that.factors[j].baseUnit))
			break;
		if (i == factors.length)
		    vector[1].addElement(that.factors[j]);
	    }
	}

	return vector;
    }

    private static abstract class Op
    {
	public DerivedUnit multOp(DerivedUnit d1, DerivedUnit d2)
	{
	    Vector[]	comm = d1.common(d2);
	    int		n0 = comm[0].size();
	    int		n1 = comm[1].size();
	    int		n2 = comm[2].size();
	    DerivedUnit	newUnit	= new DerivedUnit(n0 + n1 + n2);
	    int		k = 0;

	    for (int i = 0; i < n0; ++i)
		newUnit.factors[k++] = (Factor)comm[0].elementAt(i);

	    for (int i = 0; i < n1; ++i)
		newUnit.factors[k++] = op((Factor)comm[1].elementAt(i));

	    for (int i = 0; i < n2; ++i)
	    {
		Factor[]	factors = (Factor[])comm[2].elementAt(i);

		newUnit.factors[k++] = op(factors[0], factors[1]);
	    }

	    return newUnit;
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
     * Multiply a derived unit by a base unit.
     *
     * @param that	The base unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    Unit multiply(BaseUnit that)
    {
	return multiply(new DerivedUnit(that));
    }

    /**
     * Multiply a derived unit by a derived unit.
     *
     * @param that	The derived unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    Unit multiply(DerivedUnit that)
    {
	return addPow.multOp(this, that);
    }

    /**
     * Multiply a derived unit by a scaled unit.
     *
     * @param that	The scaled unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    Unit multiply(ScaledUnit that)
    {
	return that.multiply(this);
    }

    /**
     * Divide a derived unit by a base unit.
     *
     * @param that      The base unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    Unit divide(BaseUnit that)
    {
	return divide(new DerivedUnit(that));
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
     * Divide a derived unit by a scaled unit.
     *
     * @param that      The scaled unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    Unit divide(ScaledUnit that)
    {
	return that.divideInto(this);
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
    if (!(unit instanceof DerivedUnit)) return false;
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

