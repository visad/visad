
//
// ScaledUnit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ScaledUnit.java,v 1.7 1999-09-20 19:22:28 steve Exp $
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
    extends	Unit
    implements	Serializable
{
    /**
     * The amount of the associated derived unit.
     */
    final double	amount;

    /**
     * The associated derived unit.
     */
    final DerivedUnit	derivedUnit;


    /**
     * Construct a dimensionless scaled unit.  The identifier will be empty.
     *
     * @param amount	The given amount of this unit.
     */
    public ScaledUnit(double amount)
    {
	this(amount, "");
    }

    /**
     * Construct a dimensionless scaled unit with an identifier.
     *
     * @param amount		The given amount of this unit.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
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
     * @param amount	The given amount of the base unit (e.g. 0.9144 to
     *			create a yard unit if <code>unit</code> represents a
     *			meter).
     * @param unit	The given base unit.
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
     * @param unit		The given base unit.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
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
     * @param amount	The given amount of the derived unit (e.g. 0.44704 to
     *			create a mile/hour unit if <code>unit</code> represents
     *			a meter/second.
     * @param unit	The given derived unit.
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
     * @param unit		The given derived unit.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
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
     * @param amount	The given amount of the scaled unit (e.g. 3.0 to
     *			create a yard unit if <code>unit</code> represents
     *			a foot.
     * @param unit	The given scaled unit.
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
     * @param unit		The given scaled unit.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
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
     * @param amount	The given amount of the scaled unit (e.g. 3.0 to
     *			create a yard unit if <code>unit</code> represents
     *			a foot.
     * @param unit	The given unit.
     * @throws UnitException	Can't create Scaled Unit from <code>unit</code>.
     */
    public static ScaledUnit
    create(double amount, Unit unit)
       throws UnitException
    {
	ScaledUnit	result;
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
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    protected Unit protectedClone(String identifier)
    {
	return new ScaledUnit(amount, derivedUnit, identifier);
    }

    /**
     * Raise a scaled unit to a power.
     *
     * @param power	The power to raise this unit by.
     * @return		The unit resulting from raising this unit to 
     *			<code>power</code>.
     * @promise		This unit has not been modified.
     */
    public Unit pow(int power)
    {
	return new ScaledUnit(Math.pow(amount, power),
			      (DerivedUnit)derivedUnit.pow(power));
    }

    /**
     * Raise a scaled unit to a power.
     *
     * @param power	The power to raise this unit by.  If this unit is
     *			not dimensionless, then the value must be integral.
     * @return		The unit resulting from raising this unit to 
     *			<code>power</code>.
     * @throws IllegalArgumentException
     *			This unit is not dimensionless and <code>power</code>
     *			has a non-integral value.
     * @promise		The unit has not been modified.
     */
    public Unit pow(double power)
	throws IllegalArgumentException
    {
	return new ScaledUnit(Math.pow(amount, power),
			      (DerivedUnit)derivedUnit.pow(power));
    }

    /**
     * Return the definition of this unit.
     *
     * @return          The definition of this unit (e.g. "0.9144 m" for a
     *			yard).
     */
    public String getDefinition()
    {
	String	definition;
	if (derivedUnit == null)
	{
	    /* Probably exception thrown during construction */
	    definition = "<unconstructed ScaledUnit>";
	}
	else
	{
	    String	derivedString = derivedUnit.toString();
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
     * Multiply a scaled unit by another unit.
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
		: that instanceof ScaledUnit
		    ? multiply((ScaledUnit)that)
		    : that.multiply(this);
    }

    /**
     * Multiply a scaled unit by a derived unit.
     *
     * @param that	The derived unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit multiply(DerivedUnit that)
    {
	return new ScaledUnit(amount, (DerivedUnit)derivedUnit.multiply(that));
    }

    /**
     * Multiply a scaled unit by a scaled unit.
     *
     * @param that	The scaled unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit multiply(ScaledUnit that)
    {
	return new ScaledUnit(amount*that.amount,
		  (DerivedUnit)derivedUnit.multiply(that.derivedUnit));
    }

    /**
     * Divide a scaled unit by another unit.
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
		: that instanceof ScaledUnit
		    ? divide((ScaledUnit)that)
		    : that.divideInto(this);
    }

    /**
     * Divide a scaled unit by a derived unit.
     *
     * @param that      The derived unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit divide(DerivedUnit that)
    {
	return new ScaledUnit(amount, (DerivedUnit)derivedUnit.divide(that));
    }

    /**
     * Divide a scaled unit by a scaled unit.
     *
     * @param that      The scaled unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit divide(ScaledUnit that)
    {
	return new ScaledUnit(amount/that.amount, 
		      (DerivedUnit)derivedUnit.divide(that.derivedUnit));
    }

    /**
     * Divide a scaled unit into another unit.
     *
     * @param that      The unit to be divided by this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     * @throws UnitException	Meaningless operation.
     */
    protected Unit divideInto(Unit that)
	throws UnitException
    {
	return
	    that instanceof DerivedUnit
		? divideInto((DerivedUnit)that)
		: that instanceof ScaledUnit
		    ? ((ScaledUnit)that).divide(this)
		    : that.divide(this);
    }

    /**
     * Divide a scaled unit into a derived unit.
     *
     * @param that      The derived unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    protected Unit divideInto(DerivedUnit that)
    {
	return
	  new ScaledUnit(1.0/amount, (DerivedUnit)that.divide(derivedUnit));
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
	double[]	newValues = derivedUnit.toThis(values, that);

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] /= amount;

	return newValues;
    }

    float[] toThis(float[] values, DerivedUnit that)
        throws UnitException
    {
        float[]        newValues = derivedUnit.toThis(values, that);
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] /= (float) amount;
 
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
	double[]	newValues = derivedUnit.toThis(values,
						       that.derivedUnit);
	double		factor;
	
	if (derivedUnit.sameDimensionality(that.derivedUnit))
	    factor = that.amount/amount;
	else
	if (derivedUnit.reciprocalDimensionality(that.derivedUnit))
	    factor = 1.0/(that.amount*amount);
	else
	    throw new UnitException("Internal error");

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] *= factor;

	return newValues;
    }

    float[] toThis(float[] values, ScaledUnit that)
        throws UnitException
    {
        float[]        newValues = derivedUnit.toThis(values,
                                                       that.derivedUnit);
        float          factor;
 
        if (derivedUnit.sameDimensionality(that.derivedUnit))
            factor = (float) (that.amount/amount);
        else
        if (derivedUnit.reciprocalDimensionality(that.derivedUnit))
            factor = (float) (1.0/(that.amount*amount));
        else
            throw new UnitException("Internal error");
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] *= factor;
 
        return newValues;
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
	return toThat(values, new DerivedUnit(that));
    }

    float[] toThat(float values[], BaseUnit that)
        throws UnitException
    {
        return toThat(values, new DerivedUnit(that));
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
	double[]	newValues = derivedUnit.toThat(values, that);
	double		factor;
	
	if (derivedUnit.sameDimensionality(that))
	    factor = amount;
	else
	if (derivedUnit.reciprocalDimensionality(that))
	    factor = 1.0/amount;
	else
	    throw new UnitException("Internal error");

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] *= factor;

	return newValues;
    }

    float[] toThat(float values[], DerivedUnit that)
        throws UnitException
    {
        float[]        newValues = derivedUnit.toThat(values, that);
        float          factor;
 
        if (derivedUnit.sameDimensionality(that))
            factor = (float) amount;
        else
        if (derivedUnit.reciprocalDimensionality(that))
            factor = (float) (1.0/amount);
        else
            throw new UnitException("Internal error");
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] *= factor;
 
        return newValues;
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
    double[] toThat(double[] values, ScaledUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

    float[] toThat(float[] values, ScaledUnit that)
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
    double[] toThat(double[] values, OffsetUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

    float[] toThat(float[] values, OffsetUnit that)
        throws UnitException
    {
        return that.toThis(values, this);
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
	DerivedUnit	meterPerSec = new DerivedUnit(
			    new BaseUnit[] {meter, second}, new int[] {1, -1});
	Unit		milePerHour = new ScaledUnit(0.44704, meterPerSec);

	BaseUnit	kg = BaseUnit.addBaseUnit("Mass", "kilogram");
	DerivedUnit	kgPerSec = new DerivedUnit(new BaseUnit[] {kg, second},
					           new int[] {1, -1});
	Unit		poundPerSec = new ScaledUnit(0.453592, kgPerSec);

	System.out.println("milePerHour=\"" + milePerHour + "\"");
	System.out.println("milePerHour.pow(2)=\"" + milePerHour.pow(2) + "\"");

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

  public boolean equals(Unit unit) {
    if (unit instanceof BaseUnit)
      return equals(new ScaledUnit(1, (BaseUnit)unit));

    if (unit instanceof DerivedUnit)
      return equals(new ScaledUnit(1, (DerivedUnit)unit));

    if (!(unit instanceof ScaledUnit))
      return unit.equals(this);

    return derivedUnit.equals(((ScaledUnit) unit).derivedUnit) &&
           amount == ((ScaledUnit) unit).amount;
  }

}

