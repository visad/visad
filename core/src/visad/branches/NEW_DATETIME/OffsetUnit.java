
//
// OffsetUnit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: OffsetUnit.java,v 1.3.2.1 1999-05-11 17:02:41 steve Exp $
 */

package visad;

import java.io.Serializable;


/**
 * A class that represents a scaled unit with an offset.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class OffsetUnit
    extends	Unit
    implements	Serializable
{
    /**
     * The associated (unoffset) scaled unit.
     */
    final ScaledUnit	scaledUnit;

    /**
     * The offset for this unit (e.g. 273.15 for the celsius unit when
     * the kelvin unit is associated scaled unit).
     */
    final double	offset;


    /**
     * Construct an offset, dimensionless unit.  The identifier will be empty.
     *
     * @param offset	The amount of offset.
     *
     */
    public OffsetUnit(double offset)
    {
	this(offset, "");
    }

    /**
     * Construct an offset, dimensionless unit with an identifier.
     *
     * @param offset		The amount of offset.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     *
     */
    public OffsetUnit(double offset, String identifier)
    {
	super(identifier);
	this.offset = offset;
	scaledUnit = new ScaledUnit(1.0);
    }

    /**
     * Construct an offset unit from a base unit.  The identifier will be that
     * of the base unit if the offset is zero; otherwise, the identifier will
     * be <code>null</code>.
     *
     * @param offset	The amount of offset.
     * @param unit	The base unit.
     */
    public OffsetUnit(double offset, BaseUnit that)
    {
	this(offset, that, offset == 0 ? that.getIdentifier() : null);
    }

    /**
     * Construct an offset unit from a base unit and an identifier.
     *
     * @param offset		The amount of offset.
     * @param unit		The base unit.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    public OffsetUnit(double offset, BaseUnit that, String identifier)
    {
	super(identifier);
	this.offset = offset;
	scaledUnit = new ScaledUnit(1.0, that);
    }

    /**
     * Construct an offset unit from a derived unit.  The identifier will be
     * that of the derived unit if the offset is 0; otherwise; the identifier
     * will be <code>null</code>.
     *
     * @param offset	The amount of offset.
     * @param unit	The derived unit.
     */
    public OffsetUnit(double offset, DerivedUnit that)
    {
	this(offset, that, offset == 0 ? that.getIdentifier() : null);
    }

    /**
     * Construct an offset unit from a derived unit and an identifier.
     *
     * @param offset		The amount of offset.
     * @param unit		The derived unit.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    public OffsetUnit(double offset, DerivedUnit that, String identifier)
    {
	super(identifier);
	this.offset = offset;
	scaledUnit = new ScaledUnit(1.0, that);
    }

    /**
     * Construct an offset unit from a scaled unit.  The identifier will be
     * that of the scaled unit if the offset is 0; otherwise; the identifier
     * will be <code>null</code>.
     *
     * @param offset	The amount of offset.
     * @param unit	The scaled unit.
     */
    public OffsetUnit(double offset, ScaledUnit that)
    {
	this(offset, that, offset == 0 ? that.getIdentifier() : null);
    }

    /**
     * Construct an offset unit from a scaled unit and an identifier.
     *
     * @param offset		The amount of offset.
     * @param unit		The scaled unit.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    public OffsetUnit(double offset, ScaledUnit that, String identifier)
    {
	super(identifier);
	this.offset = offset;
	scaledUnit = that;
    }

    /**
     * Construct an offset unit from an offset unit.  The identifier will be
     * that of the offset unit if the offset is 0; otherwise; the identifier
     * will be <code>null</code>.
     *
     * @param offset	The amount of offset.
     * @param unit	The given unit.
     */
    public OffsetUnit(double offset, OffsetUnit that)
    {
	this(offset, that, offset == 0 ? that.getIdentifier() : null);
    }

    /**
     * Construct an offset unit from an offset unit and an identifier..
     *
     * @param offset		The amount of offset.
     * @param unit		The given unit.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    public OffsetUnit(double offset, OffsetUnit that, String identifier)
    {
	super(identifier);
	this.offset = offset + that.offset;
	scaledUnit = that.scaledUnit;
    }

    /**
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    public Unit clone(String identifier)
    {
	return new OffsetUnit(0, this, identifier);
    }

    /**
     * Raise an offset unit to a power.
     *
     * @param power	The power to raise this unit by.
     * @exception	UnitException	Always thrown because it's meaningless
     *					to raise an offset unit to a power.
     */
    public Unit pow(int power)
	throws UnitException
    {
	throw new UnitException("Attempt to raise offset unit to a power");
    }

    /**
     * Raise an offset unit to a power.
     *
     * @param power	The power to raise this unit by.
     * @exception	UnitException	Always thrown because it's meaningless
     *					to raise an offset unit to a power.
     */
    public Unit pow(double power)
	throws UnitException
    {
	throw new UnitException("Attempt to raise offset unit to a power");
    }

    /**
     * Return the definition of this unit.
     *
     * @return          The definition of this unit (e.g. "K @ 273.15" for
     *			degree celsius).
     */
    public String getDefinition()
    {
	String	scaledString = scaledUnit.toString();

	if (scaledString.indexOf(' ') != -1)
	    scaledString = "(" + scaledString + ")";
	return scaledString + " @ " + offset;
    }

    /**
     * Multiply an offset unit by a base unit.
     *
     * @param that	The base unit with which to multiply this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				multiply an offset unit.
     */
    Unit multiply(BaseUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to multiply offset unit");
    }

    /**
     * Multiply an offset unit by a derived unit.
     *
     * @param that	The derived unit with which to multiply this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				multiply an offset unit.
     */
    Unit multiply(DerivedUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to multiply offset unit");
    }

    /**
     * Multiply an offset unit by a scaled unit.
     *
     * @param that	The scaled unit with which to multiply this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				multiply an offset unit.
     */
    Unit multiply(ScaledUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to multiply offset unit");
    }

    /**
     * Multiply an offset unit by an offset unit.
     *
     * @param that	The offset unit with which to multiply this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				multiply an offset unit.
     */
    Unit multiply(OffsetUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to multiply offset unit");
    }

    /**
     * Divide an offset unit by a base unit.
     *
     * @param that	The base unit to divide into this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				divide an offset unit.
     */
    Unit divide(BaseUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to divide offset unit");
    }

    /**
     * Divide an offset unit by a derived unit.
     *
     * @param that	The derived unit to divide into this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				divide an offset unit.
     */
    Unit divide(DerivedUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to divide offset unit");
    }

    /**
     * Divide an offset unit by a scaled unit.
     *
     * @param that	The scaled unit to divide into this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				divide an offset unit.
     */
    Unit divide(ScaledUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to divide offset unit");
    }

    /**
     * Divide an offset unit by an offset unit.
     *
     * @param that	The offset unit to divide into this unit.
     * @exception UnitExcepting	Always thrown because it's meaningless to
     *				divide an offset unit.
     */
    Unit divide(OffsetUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to divide offset unit");
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
	double[]	newValues = scaledUnit.toThis(values, that);

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] -= offset;

	return newValues;
    }

    float[] toThis(float[] values, BaseUnit that)
        throws UnitException
    {
        float[]        newValues = scaledUnit.toThis(values, that);
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] -= (float) offset;
 
        return newValues;
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
	double[]	newValues = scaledUnit.toThis(values, that);

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] -= offset;

	return newValues;
    }

    float[] toThis(float[] values, DerivedUnit that)
        throws UnitException
    {
        float[]        newValues = scaledUnit.toThis(values, that);
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] -= (float) offset;
 
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
	double[]	newValues = scaledUnit.toThis(values, that);

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] -= offset;

	return newValues;
    }

    float[] toThis(float[] values, ScaledUnit that)
        throws UnitException
    {
        float[]        newValues = scaledUnit.toThis(values, that);
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] -= (float) offset;
 
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
	double[]	newValues = new double[values.length];

	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] = values[i] + that.offset;

	return toThis(newValues, that.scaledUnit);
    }

    float[] toThis(float[] values, OffsetUnit that)
        throws UnitException
    {
        float[]        newValues = new float[values.length];
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] = values[i] + (float) that.offset;
 
        return toThis(newValues, that.scaledUnit);
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
	double[]	newValues = new double[values.length];
	
	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] = values[i] + offset;

	return scaledUnit.toThat(newValues, that);
    }

    float[] toThat(float values[], BaseUnit that)
        throws UnitException
    {
        float[]        newValues = new float[values.length];
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] = values[i] + (float) offset;
 
        return scaledUnit.toThat(newValues, that);
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
	double[]	newValues = new double[values.length];
	
	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] = values[i] + offset;

	return scaledUnit.toThat(newValues, that);
    }

    float[] toThat(float values[], DerivedUnit that)
        throws UnitException
    {
        float[]        newValues = new float[values.length];
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] = values[i] + (float) offset;
 
        return scaledUnit.toThat(newValues, that);
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
	double[]	newValues = new double[values.length];
	
	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] = values[i] + offset;

	return scaledUnit.toThat(newValues, that);
    }

    float[] toThat(float values[], ScaledUnit that)
        throws UnitException
    {
        float[]        newValues = new float[values.length];
 
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] = values[i] + (float) offset;
 
        return scaledUnit.toThat(newValues, that);
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

    /**
     * Gets the absolute unit of this unit.  An interval in the underlying
     * physical quantity has the same numeric value in an absolute unit of a
     * unit as in the unit itself -- but an absolute unit is always referenced
     * to the physical origin of the underlying physical quantity.  For
     * example, the absolute unit corresponding to degrees celsius is degrees
     * kelvin -- and calling this method on a degrees celsius unit obtains a
     * degrees kelvin unit.
     * @return		The absolute unit corresponding to this unit.
     */
    public Unit
    getAbsoluteUnit()
    {
      return scaledUnit;
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
	BaseUnit	degK = BaseUnit.addBaseUnit("Temperature", "kelvin");
	Unit		degC = new OffsetUnit(273.15, degK);
	ScaledUnit	degR = new ScaledUnit(1/1.8, degK);
	Unit		degF = new OffsetUnit(459.67, degR);

	System.out.println("degC=\"" + degC + "\"");

	System.out.println("degF=\"" + degF + "\"");

	System.out.println("degF.toThis(0,degC)=" +
	    degF.toThis(0,degC));

	System.out.println("degF.toThat(32,degC)=" +
	    degF.toThat(32,degC));

	double[] values;

	values = degF.toThis(new double[] {0,100},degC);
	System.out.println("degF.toThis({0,100},degC)=" +
	    values[0] + "," + values[1]);

	values = degF.toThat(new double[] {32,212},degC);
	System.out.println("degF.toThat({32,212},degC)=" +
	    values[0] + "," + values[1]);

	System.out.println("Checking exceptions:");
	try
	{
	    degF.pow(2);
	    System.err.println("ERROR: degF.pow(2)");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}

	try
	{
	    degF.multiply(degC);
	    System.err.println("ERROR: degF.multiply(degC)");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
	try
	{
	    degC.multiply(degF);
	    System.err.println("ERROR: degC.multiply(degF)");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}

	try
	{
	    degF.divide(degC);
	    System.err.println("ERROR: degF.divide(degC)");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
	try
	{
	    degC.divide(degF);
	    System.err.println("ERROR: degC.divide(degF)");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
    }

  public boolean equals(Unit unit) {
    return (unit instanceof OffsetUnit) &&
           scaledUnit.equals(((OffsetUnit) unit).scaledUnit) &&
           (offset == ((OffsetUnit) unit).offset);
  }
}

