/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitTable.java,v 1.5 1998-12-16 16:08:28 steve Exp $
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
 * Provides support for a table of units.
 *
 * @author Steven R. Emmerson
 */
public class
UnitTable
    implements	java.io.Serializable
{
    /**
     * Unit table.
     */
    protected final Hashtable	names;
    protected final Hashtable	symbols;


    /**
     * Construct.
     *
     * @param num	The initial size of the table.  The table will grow,
     *			as necessary, only after this number is exceeded.
     * @require		<code>num</code> >= 0.
     * @exception IllegalArgumentException	<code>num</code> < 0.
     */
    public
    UnitTable(int numNames)
	throws IllegalArgumentException
    {
	this(numNames, 0);
    }


    /**
     * Construct.
     *
     * @param num	The initial size of the table.  The table will grow,
     *			as necessary, only after this number is exceeded.
     * @require		<code>num</code> >= 0.
     * @exception IllegalArgumentException	<code>num</code> < 0.
     */
    public
    UnitTable(int numNames, int numSymbols)
	throws IllegalArgumentException
    {
	if (numNames < 0 || numSymbols < 0)
	    throw new IllegalArgumentException("Negative hashtable size");

	names = new Hashtable(numNames);
	symbols = new Hashtable(numSymbols);
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
	Unit	unit = null;	// default

	if (name.length() == 0)
	{
	    // Return a unity, dimensionless unit.
	    unit = new DerivedUnit();
	}
	else
	{
	    /*
	     * Try the name table.
	     */
	    NamedUnit	namedUnit = getByName(name);

	    if (namedUnit == null)
	    {
		/*
		 * Try the symbol table.
		 */
		namedUnit = getBySymbol(name);
	    }

	    if (namedUnit != null)
		unit = namedUnit.getUnit();
	}

	return unit;
    }


    /**
     * Get a unit by name.
     *
     * @param name	The name of the unit to be retrieved.  If it is
     *			the empty string, then the dimensionless, unity unit
     *			will be returned.
     * @return		The unit of the matching entry or null if not found.
     * @require		<code>name</code> is non-null.
     */
    protected NamedUnit
    getByName(String name)
    {
	name = name.toLowerCase();

	NamedUnit	namedUnit = (NamedUnit)names.get(name);

	if (namedUnit == null)
	{
	    int	lastPos = name.length() - 1;

	    if (lastPos > 0 && name.charAt(lastPos) == 's')
	    {
		/*
		 * Input name is possibly plural. Try singular form.
		 */
		namedUnit = (NamedUnit)names.get(name.substring(0, lastPos));

		if (namedUnit != null && !namedUnit.hasPlural())
		    namedUnit = null;
	    }
	}

	return namedUnit;
    }


    /**
     * Get a unit by symbol.
     *
     * @param symbol	The exact symbol of the unit to be retrieved.  If it is
     *			the empty string, then the dimensionless, unity unit
     *			will be returned.
     * @return		The unit of the matching entry or null if not found.
     * @require		<code>name</code> is non-null.
     */
    protected NamedUnit
    getBySymbol(String symbol)
    {
	return (NamedUnit)symbols.get(symbol);
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

	NamedUnit	namedUnit = (NamedUnit)names.put(name.toLowerCase(), 
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

	return namedUnit.isCaseSensitive()
		? (NamedUnit)symbols.put(namedUnit.getName(), namedUnit)
		: (NamedUnit)names.put(namedUnit.getName().toLowerCase(),
				       namedUnit);
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
	    protected Enumeration	nameEnum = names.elements();
	    protected Enumeration	symbolEnum = symbols.elements();

	    public boolean
	    hasMoreElements()
	    {
		return nameEnum.hasMoreElements() || 
		       symbolEnum.hasMoreElements();
	    }

	    public Object
	    nextElement()
	    {
		return nameEnum.hasMoreElements()
			? nameEnum.nextElement()
			: symbolEnum.nextElement();
	    }
	};
    }


    /**
     * Return a string representation of this instance.
     */
    public String
    toString()
    {
	return names.toString() + symbols.toString();
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
