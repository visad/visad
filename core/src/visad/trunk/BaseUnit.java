
//
// BaseUnit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: BaseUnit.java,v 1.9 1999-12-14 19:24:25 steve Exp $
 */

package visad;

import java.util.Vector;
import java.io.Serializable;

/**
 * A class that represents the base units of a system of units.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class BaseUnit
    extends	Unit
    implements	Serializable
{
    /**
     * Name of the unit (e.g. "meter").
     */
    private final String		unitName;

    /**
     * Quantity of the unit (e.g. "Length").
     */
    private final String		quantityName;

    /**
     * Derived unit associated with base unit (for computational efficiency).
     */
    final DerivedUnit			derivedUnit;

    /**
     * Global database of base units (to prevent multiple base units for the
     * same quantity).
     */
    private static final Vector	baseUnits = new Vector(9);


    /**
     * Raise a base unit to a power.
     *
     * @param power	The power to raise this unit by.
     * @return		The unit resulting from raising this unit to 
     *			<code>power</code>.
     * @promise		This unit has not been modified.
     */
    public Unit pow(int power)
    {
	return derivedUnit.pow(power);
    }

    /**
     * Raise a unit to a power.
     *
     * @param power	The power to raise this unit by.  The value must be
     *			integral.
     * @return		The unit resulting from raising this unit to 
     *			<code>power</code>.
     * @throws IllegalArgumentException
     *			<code>power</code> has a non-integral value.
     * @promise		The unit has not been modified.
     */
    public Unit pow(double power)
	throws IllegalArgumentException
    {
	return derivedUnit.pow(power);
    }

    /**
     * Return the name of this unit.
     *
     * @return          The name of this unit (e.g. "meter").
     */
    public String unitName()
    {
	return unitName;
    }

    /**
     * Return the symbol of this unit.  This is the same as the identifier.
     *
     * @return          The symbol of this unit (e.g. "m").
     */
    public String unitSymbol()
    {
	return getIdentifier();
    }

    /**
     * Return the name of the quantity associated with this unit.
     *
     * @return          The name this units quantity (e.g. "Length").
     */
    public String quantityName()
    {
	return quantityName;
    }

    /**
     * Create a new base unit from the name of a quantity and the name
     * of a unit.  The unit abbreviation will be the same as the unit name.
     *
     * @param quantityName	The name of the associated quantity (e.g. 
     *				"Length").
     * @param unitName		The name for the unit (e.g. "meter").
     * @return          	A new base unit or the previously created one
     *				with the same names.
     * @require			The arguments are non-null.  The quantity
     *				name has not been used before or the unit name
     *				is the same as before.
     * @promise			The new quantity and unit has been added to the
     *				database.
     * @throws UnitException	Name, abbreviation, or quantity name is <code>
     *				null</code> or attempt to redefine the base unit
     *				associated with <code>quantityName</code>.
     */
    public static BaseUnit addBaseUnit(String quantityName, String unitName)
	throws UnitException
    {
	return addBaseUnit(quantityName, unitName, unitName);
    }

    /**
     * Create a new base unit from from the name of a quantity, the name of
     * a unit, and the unit's abbreviation.
     *
     * @param quantityName	The name of the associated quantity (e.g. 
     *				"Length").
     * @param unitName		The name for the unit (e.g. "meter").
     * @param abbreviation	The abbreviation for the unit (e.g. "m").
     * @return          	A new base unit or the previously created one
     *				with the same names.
     * @require			The arguments are non-null.  The quantity
     *				name has not been used before or the unit name
     *				is the same as before.
     * @promise			The new quantity and unit has been added to the
     *				database.
     * @throws UnitException	Name, abbreviation, or quantity name is <code>
     *				null</code> or attempt to redefine the base unit
     *				associated with <code>quantityName</code>.
     */
    public static synchronized BaseUnit addBaseUnit(String quantityName,
						    String unitName,
						    String abbreviation)
	throws UnitException
    {
	BaseUnit	baseUnit = quantityNameToUnit(quantityName);

	if (baseUnit == null)
	    return new BaseUnit(unitName, abbreviation, quantityName);

	if (baseUnit.unitName.equals(unitName) && 
	    baseUnit.getIdentifier().equals(abbreviation))
	{
	    return baseUnit;
	}

	throw new UnitException("Attempt to redefine quantity \"" + 
	    quantityName + "\" base unit from \"" + 
	    baseUnit.unitName + "(" + baseUnit.getIdentifier() + ")" + 
	    "\" to \"" + 
	    unitName + "(" + abbreviation + ")" + "\"");
    }

    /**
     * Find the base unit with the given name.
     *
     * @param unitName		The name of the unit (e.g. "meter").
     * @return          	The existing base unit with the given name
     *				or <code>null</code> if no such units exists.
     * @require			The argument is non-null.
     */
    public static synchronized BaseUnit unitNameToUnit(String unitName)
    {
	for (int i = 0; i < baseUnits.size(); ++i)
	{
	    BaseUnit	baseUnit = (BaseUnit)baseUnits.elementAt(i);

	    if (baseUnit.unitName.equals(unitName))
		return baseUnit;
	}

	return null;
    }

    /**
     * Find the base unit for the given quantity.
     *
     * @param unitName		The name of the quantity (e.g. "Length").
     * @return          	The existing base unit for the given quantity
     *				or <code>null</code> if no such unit exists.
     * @require			The argument is non-null.
     */
    public static synchronized BaseUnit quantityNameToUnit(String quantityName)
    {
	for (int i = 0; i < baseUnits.size(); ++i)
	{
	    BaseUnit	baseUnit = (BaseUnit)baseUnits.elementAt(i);

	    if (baseUnit.quantityName.equals(quantityName))
		return baseUnit;
	}

	return null;
    }


    /**
     * Test this class.
     *
     * @param args		Arguments (ignored).
     * @exception UnitException	A problem occurred.
     */
    public static void main(String[] args)
	throws	UnitException
    {
	BaseUnit	meter = BaseUnit.addBaseUnit("Length", "meter", "m");

	System.out.println("meter=\"" + meter + "\"");
	System.out.println("(Unit)meter=\"" + (Unit)meter + "\"");
	System.out.println("meter^2=\"" + meter.pow(2) + "\"");
	System.out.println("((Unit)meter)^2=\"" + ((Unit)meter).pow(2) + "\"");

	BaseUnit	second = BaseUnit.addBaseUnit("Time", "second", "s");

	System.out.println("meter*second=\"" + meter.multiply(second) + "\"");
	System.out.println("meter/(Unit)second=\"" + 
	    meter.divide((Unit)second) + "\"");

	System.out.println("meter.toThis(5,meter)=" + meter.toThis(5,meter));
	System.out.println("meter.toThat(5,meter)=" + meter.toThat(5,meter));

	System.out.println("meter.toThis(5,(Unit)meter)=" +
	    meter.toThis(5,(Unit)meter));
	System.out.println("meter.toThat(5,(Unit)meter)=" +
	    meter.toThat(5,(Unit)meter));

	double[] values = meter.toThis(new double[] {1,2},meter);

	System.out.println("meter.toThis({1,2},meter)=" + 
	    values[0] + "," + values[1]);

	values = meter.toThat(new double[] {1,2},meter);

	System.out.println("meter.toThat({1,2},meter)=" + 
	    values[0] + "," + values[1]);

	values = meter.toThis(new double[] {1,2},(Unit)meter);

	System.out.println("meter.toThis({1,2},(Unit)meter)=" + 
	    values[0] + "," + values[1]);

	values = meter.toThat(new double[] {1,2},(Unit)meter);

	System.out.println("meter.toThat({1,2},(Unit)meter)=" + 
	    values[0] + "," + values[1]);

	System.out.println("Checking exceptions:");
	try
	{
	    meter.toThis(5,second);
	    System.err.println("ERROR: second -> meter");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
	try
	{
	    meter.toThat(5,second);
	    System.err.println("ERROR: meter -> second");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
	try
	{
	    BaseUnit	foo = BaseUnit.addBaseUnit("Length", "foot", "ft");
	    System.err.println("ERROR: \"foot\" added");
	    System.exit(1);
	}
	catch (UnitException e)
	{
	    System.out.println(e.getMessage());
	}
    }

    /**
     * Convert values to this unit from a base unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, BaseUnit that)
	throws UnitException
    {
	if (equals(that))
	{
	    double[]	newValues = new double[values.length];

	    for (int i = 0; i < values.length; ++i)
		newValues[i] = values[i];

	    return newValues;
	}

	throw new UnitException("Attempt to convert from unit \"" +
	    that + "\" to unit \"" + this + "\"");
    }

    float[] toThis(float[] values, BaseUnit that)
        throws UnitException
    {
        if (equals(that))
        {
            float[]    newValues = new float[values.length];
 
            for (int i = 0; i < values.length; ++i)
                newValues[i] = values[i];
 
            return newValues;
        }
 
        throw new UnitException("Attempt to convert from unit \"" +
            that + "\" to unit \"" + this + "\"");
    }

    /**
     * Convert values to this unit from a derived unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, DerivedUnit that)
	throws UnitException
    {
	return derivedUnit.toThis(values, that);
    }

    float[] toThis(float[] values, DerivedUnit that)
        throws UnitException
    {
        return derivedUnit.toThis(values, that);
    }

    /**
     * Convert values to this unit from a scaled unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, ScaledUnit that)
	throws UnitException
    {
	return that.toThat(values, derivedUnit);
    }

    float[] toThis(float[] values, ScaledUnit that)
        throws UnitException
    {
        return that.toThat(values, derivedUnit);
    }

    /**
     * Convert values to this unit from a offset unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, OffsetUnit that)
	throws UnitException
    {
	return that.toThat(values, derivedUnit);
    }

    float[] toThis(float[] values, OffsetUnit that)
        throws UnitException
    {
        return that.toThat(values, derivedUnit);
    }

    /**
     * Convert values from this unit to a base unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double[] values, BaseUnit that)
	throws UnitException
    {
	if (equals(that))
	{
	    double[]	newValues = new double[values.length];

	    for (int i = 0; i < values.length; ++i)
		newValues[i] = values[i];

	    return newValues;
	}

	throw new UnitException("Attempt to convert from unit \"" +
	    this + "\" to unit \"" + that + "\"");
    }

    float[] toThat(float[] values, BaseUnit that)
        throws UnitException
    {
        if (equals(that))
        {
            float[]    newValues = new float[values.length];
 
            for (int i = 0; i < values.length; ++i)
                newValues[i] = values[i];
 
            return newValues;
        }
 
        throw new UnitException("Attempt to convert from unit \"" +
            this + "\" to unit \"" + that + "\"");
    }

    /**
     * Convert values from this unit to a derived unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double[] values, DerivedUnit that)
	throws UnitException
    {
	return derivedUnit.toThat(values, that);
    }

    float[] toThat(float[] values, DerivedUnit that)
        throws UnitException
    {
        return derivedUnit.toThat(values, that);
    }

    /**
     * Convert values from this unit to a scaled unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double[] values, ScaledUnit that)
	throws UnitException
    {
	return that.toThis(values, derivedUnit);
    }

    float[] toThat(float[] values, ScaledUnit that)
        throws UnitException
    {
        return that.toThis(values, derivedUnit);
    }

    /**
     * Convert values from this unit to a offset unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are identical.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double[] values, OffsetUnit that)
	throws UnitException
    {
	return that.toThis(values, derivedUnit);
    }

    float[] toThat(float[] values, OffsetUnit that)
        throws UnitException
    {
        return that.toThis(values, derivedUnit);
    }

    /**
     * Construct a base unit from the names for the quantity and unit, and
     * from the unit abbreviation.
     *
     * @param unitName		Name of the unit (e.g. "meter").
     * @param abbreviation	The abbreviation for the unit (e.g. "m").
     * @param quantityName	Name of the quantity (e.g. "Length").
     * @throws UnitException	Name, abbreviation, or quantity name is <code>
     *				null</code>.
     */
    private BaseUnit(String unitName, String abbreviation, String quantityName)
	throws UnitException
    {
	super(abbreviation);
	if (unitName == null || abbreviation == null || quantityName == null)
	  throw new UnitException(
	    "Base unit name, abbreviation, or quantity name is null");
	this.unitName = unitName;
	this.quantityName = quantityName;
	baseUnits.addElement(this);
	derivedUnit = new DerivedUnit(this);
    }

    /**
     * Returns the definition of this unit.  The definition of a BaseUnit is the
     * same as the BaseUnit's identifier.
     *
     * @return		The definition of this unit.  Won't be <code>null
     *			</code> but may be empty.
     */
    public String getDefinition()
    {
      return getIdentifier();
    }

    /**
     * Clones this unit, changing the identifier.  This method always throws
     * an exception because base units may not be cloned.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     * @throws UnitException	Base units may not be cloned.  Always thrown.
     */
    protected Unit protectedClone(String identifier)
      throws UnitException
    {
      throw new UnitException("Base units may not be cloned");
    }
      

  /** added by WLH 11 Feb 98 */
  public boolean equals(Unit unit) {
    return (unit instanceof BaseUnit) &&
           unitName.equals(((BaseUnit) unit).unitName) &&
           quantityName.equals(((BaseUnit) unit).quantityName);
  }

    /**
     * Multiply a base unit by another unit.
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
	      : that.multiply(derivedUnit);
    }

    /**
     * Multiply a base unit by a derived unit.
     *
     * @param that	The derived unit with which to multiply this unit.
     * @return		The product of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit multiply(DerivedUnit that)
    {
	return derivedUnit.multiply(that);
    }

    /**
     * Divide a base unit by another unit.
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
		: that.divideInto(derivedUnit);
    }

    /**
     * Divide a base unit by a derived unit.
     *
     * @param that      The derived unit to divide into this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     */
    public Unit divide(DerivedUnit that)
    {
	return derivedUnit.divide(that);
    }

    /**
     * Divide a base unit into another unit.
     *
     * @param that      The unit to divided this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     * @throws UnitException	Meaningless operation.
     */
    protected Unit divideInto(Unit that)
	throws UnitException
    {
	return that.divide(derivedUnit);
    }

    /**
     * Indicate whether this unit is convertible with another unit.  If one unit
     * is convertible with another, then the <code>toThis(...)</code>/ and
     * <code>toThat(...)</code> methods will not throw a UnitException.  Unit A
     * is convertible with unit B if and only if unit B is convertible with unit
     * A; hence, calling-order is irrelevant.
     *
     * @param unit	The other unit.
     * @return		True if and only if this unit is convertible with the
     *			other unit.
     */
    public boolean isConvertible(Unit unit)
    {
      return derivedUnit.isConvertible(unit);
    }

/*
   added by Bill Hibbard for VisAD
   so that BaseUnits from different JVM's can be equal
*/
/*
  public boolean equals(Unit unit) {
    if (!(unit instanceof BaseUnit)) return false;
    return quantityName.equals(((BaseUnit) unit).quantityName) &&
           unitName.equals(((BaseUnit) unit).unitName);
  }
*/

}
