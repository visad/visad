/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitTable.java,v 1.3 1998-02-23 15:59:04 steve Exp $
 */

package visad.data.netcdf.units;


import java.util.Enumeration;
import java.util.Hashtable;
import visad.DerivedUnit;
import visad.OffsetUnit;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;
import visad.UnitException;


/**
 * Table of units.
 *
 * This is a helper class for DefaultUnitsDB.
 */
public class
UnitTable
    implements	java.io.Serializable
{
    /**
     * Unit table.
     */
    protected final Hashtable	table;


    /**
     * Construct.
     *
     * @param num	The initial size of the table.  The table will grow,
     *			as necessary, only after this number is exceeded.
     * @require		<code>num</code> >= 0.
     * @exception IllegalArgumentException	<code>num</code> < 0.
     */
    public
    UnitTable(int num)
	throws IllegalArgumentException
    {
	if (num < 0)
	    throw new IllegalArgumentException("Negative hashtable size");

	table = new Hashtable(num);
    }


    /**
     * Get a unit.
     *
     * @param name	The exact name of the unit to be retrieved.  If it is
     *			the empty string, then the dimensionless, unity unit
     *			will be returned.
     * @return		The unit of the matching entry or null if not found.
     * @require		<code>name</code> is non-null.
     */
    public Unit
    get(String name)
    {
	Unit	unit = null;

	if (name.length() == 0)
	{
	    // Return a unity, dimensionless unit.
	    unit = new DerivedUnit();
	}
	else
	{
	    NamedUnit	namedUnit = (NamedUnit)table.get(name);

	    if (namedUnit != null)
		unit = namedUnit.getUnit();
	    else
	    {
		int	lastPos = name.length() - 1;

		if (lastPos > 0 && name.charAt(lastPos) == 's')
		{
		    /*
		     * Input name is possibly plural. Try singular form.
		     */
		    namedUnit = (NamedUnit)table.get(name.substring(0,
			lastPos));

		    if (namedUnit != null && namedUnit.hasPlural())
			unit = namedUnit.getUnit();
		}
	    }
	}

	return unit;
    }


    /**
     * Add a unit.
     *
     * @param name	The name of the unit to be added.
     * @param unit	The unit to be added.
     * @param hasPlural	Whether or not the name of the unit has a plural form
     *			that ends with an `s'.
     * @return		The previously matching unit or null if no such entry.
     * @require		All argument shall be non-null.  The name shall be non-
     *			empty.
     * @exception java.lang.IllegalArgumentException
     *			An argument was null or the name was empty.
     */
    public Unit
    put(String name, Unit unit, boolean hasPlural)
	throws IllegalArgumentException
    {
	if (name == null || name.length() == 0 || unit == null)
	    throw new IllegalArgumentException("Invalid name or unit");

	NamedUnit	namedUnit = (NamedUnit)table.put(name, 
	    hasPlural
		? (NamedUnit)new PluralUnit(name, unit)
		: (NamedUnit)new SingleUnit(name, unit));

	return namedUnit == null
		? null
		: namedUnit.getUnit();
    }


    /**
     * Add a named unit.
     *
     * @param namedUnit	The named unit to be added.
     * @return		The previous entry or null.
     * @require		<code>namedUnit</code> shall be non-null.
     * @exception java.lang.IllegalArgumentException
     *			<code>namedUnit</code> was null.
     */
    public NamedUnit
    put(NamedUnit namedUnit)
	throws IllegalArgumentException
    {
	if (namedUnit == null)
	    throw new IllegalArgumentException("Null named unit");

	return (NamedUnit)table.put(namedUnit.getName(), namedUnit);
    }


    /**
     * Get an enumeration of the units in the table.  The Object returned
     * by nextElement() is a NamedUnit.
     */
    public Enumeration
    enumeration()
    {
	return new Enumeration()
	{
	    protected Enumeration	enum = table.elements();

	    public boolean
	    hasMoreElements()
	    {
		return enum.hasMoreElements();
	    }

	    public Object
	    nextElement()
	    {
		return enum.nextElement();
	    }
	};
    }


    /**
     * Return a string representation of this instance.
     */
    public String
    toString()
    {
	return table.toString();
    }


    /**
     * Test this class.
     * @exception java.lang.Exception	A problem occurred.
     */
    public static void main(String[] args)
	throws Exception
    {
	UnitTable	db = new UnitTable(13);

	db.put(new PluralUnit("ampere",		SI.ampere));
	db.put(new PluralUnit("candela",	SI.candela));
	db.put(new PluralUnit("kelvin",		SI.kelvin));
	db.put(new PluralUnit("kilogram",	SI.kilogram));
	db.put(new PluralUnit("meter",		SI.meter));
	db.put(new PluralUnit("mole",		SI.mole));
	db.put(new PluralUnit("second",		SI.second));
	db.put(new PluralUnit("radian",		SI.radian));
	db.put(new PluralUnit("amp",		SI.ampere));	// alias
	db.put(new SingleUnit("celsius", new OffsetUnit(273.15, SI.kelvin)));
	db.put(new PluralUnit("newton",  
	    SI.kilogram.multiply(SI.meter).divide(SI.second.pow(2))));
	db.put(new PluralUnit("rankine", new ScaledUnit(1/1.8, SI.kelvin)));
	db.put(new PluralUnit("fahrenheit", new OffsetUnit(459.67,
	    (ScaledUnit)db.get("rankine"))));

	System.out.println("db:");
	System.out.println(db.toString());
    }
}
