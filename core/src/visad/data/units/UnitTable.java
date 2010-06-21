/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitTable.java,v 1.6 2009-04-21 20:15:10 steve Exp $
 */

package visad.data.units;


import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import visad.BaseUnit;
import visad.DerivedUnit;
import visad.OffsetUnit;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;


/**
 * Provides support for a table of units.
 *
 * @author Steven R. Emmerson
 */
public class
UnitTable
    implements  UnitsDB, java.io.Serializable
{
    /**
     * Name-to-unit map.
     * @serial
     */
    private final Hashtable nameMap;

    /**
     * Symbol-to-unit map.
     * @serial
     */
    private final Hashtable symbolMap;

    /**
     * The unit set.
     * @serial
     */
    private final SortedSet unitSet;


    /**
     * Construct.
     *
     * @param numNames      Anticipated minimum number of names in the
     *              database.
     * @throws IllegalArgumentException <code>numNames < 0</code>.
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
     * @param numNames      Anticipated minimum number of names in the
     *              database.
     * @param numSymbols    Anticipated minimum number of symbols in the
     *              database.
     * @throws IllegalArgumentException <code>numNames < 0 || numSymbols < 0
     *                  </code>.
     */
    public
    UnitTable(int numNames, int numSymbols)
    throws IllegalArgumentException
    {
    if (numNames < 0 || numSymbols < 0)
        throw new IllegalArgumentException("Negative hashtable size");
    nameMap = new Hashtable(numNames);
    symbolMap = new Hashtable(numSymbols);
    unitSet =
        Collections.synchronizedSortedSet(
        new TreeSet(
            new Comparator()
            {
            public int
            compare(Object o1, Object o2)
            {
                return ((Unit)o1).toString().compareToIgnoreCase(
                ((Unit)o2).toString());
            }
            }));
    }


    /**
     * Get a unit.
     *
     * @param name  The exact name of the unit to be retrieved.  If it is
     *          the empty string, then the dimensionless, unity
     *          unit
     *          will be returned.
     * @return      The unit of the matching entry or null if not found.
     * @require     <code>name</code> is non-null.
     */
    public Unit
    get(String name)
    {
    Unit    unit = null;    // default
    if (name.length() == 0)
    {
        // Return a unity, dimensionless unit.
        unit = new DerivedUnit();
    }
    else
    {
        /*
         * Try the symbol table first because symbols are case-sensitive.
         */
        unit = getBySymbol(name);
        if (unit == null)
        unit = getByName(name);
    }
    return unit;
    }


    /**
     * Get a unit by name.
     *
     * @param name  The name of the unit to be retrieved.  If it is
     *          the empty string, then the dimensionless, unity
     *          unit
     *          will be returned.
     * @return      The unit of the matching entry or null if not found.
     * @require     <code>name</code> is non-null.
     */
    protected Unit
    getByName(String name)
    {
    return (Unit)nameMap.get(name.toLowerCase());
    }


    /**
     * Get a unit by symbol.
     *
     * @param symbol    The exact symbol of the unit to be retrieved.  If it is
     *          the empty string, then the dimensionless, unity
     *          unit
     *          will be returned.
     * @return      The unit of the matching entry or null if not found.
     * @require     <code>name</code> is non-null.
     */
    protected Unit
    getBySymbol(String symbol)
    {
    return (Unit)symbolMap.get(symbol);
    }


    /**
     * Adds a base unit.
     *
     * @param baseUnit  The base unit to be added.
     * @throws IllegalArgumentException
     *          The base unit argument is invalid.
     */
    public void
    put(BaseUnit unit)
    throws IllegalArgumentException
    {
    String  name = unit.unitName();
    putName(name, unit);
    putName(makePlural(name), unit);
    putSymbol(unit.unitSymbol(), unit);
    }


    /**
     * Returns the plural form of a name.  Regular rules are used to generate
     * the plural form.
     * @param name      The name.
     * @return          The plural form of the name.
     */
    protected String
    makePlural(String name)
    {
    String  plural;
    int length = name.length();
    char    lastChar = name.charAt(length-1);
    if (lastChar != 'y')
    {
        plural = name +
        (lastChar == 's' || lastChar == 'x' ||
         lastChar == 'z' || name.endsWith("ch")
            ? "es"
            : "s");
    }
    else
    {
        if (length == 1)
        {
        plural = name + "s";
        }
        else
        {
        char    penultimateChar = name.charAt(length-2);
        plural =
            (penultimateChar == 'a' || penultimateChar == 'e' ||
             penultimateChar == 'i' || penultimateChar == 'o' ||
             penultimateChar == 'u')
            ? name + "s"
            : name.substring(0, length-1) + "ies";
        }
    }
    return plural;
    }


    /**
     * Adds a name and a unit to the name table.
     * @param name      The name to be added.
     * @param unit      The unit to be added.
     * @throws IllegalArgumentException Invalid argument.
     */
    public void
    putName(String name, Unit unit)
    {
    if (name == null)
        throw new IllegalArgumentException(this.getClass().getName() +
        ".putName(String,Unit): <null> unit name");
    if (unit == null)
        throw new IllegalArgumentException(this.getClass().getName() +
        ".putName(String,Unit): <null> unit");
    name = name.toLowerCase();
    String[]    names =
        (name.indexOf(' ') == -1 && name.indexOf('_') == -1)
        ? new String[] {name}
        : new String[] {
            name.replace('_', ' '),
            name.replace(' ', '_')};
    for (int i = 0; i < names.length; i++)
    {
        Unit    prevUnit = (Unit)nameMap.get(names[i]);
        if (prevUnit != null && !prevUnit.equals(unit))
        throw new IllegalArgumentException(
            "Attempt to replace unit \"" + prevUnit + " with unit \"" +
            unit + '"');
        nameMap.put(names[i], unit);
    }
    unitSet.add(unit);
    }


    /**
     * Adds a symbol and a unit to the symbol table.
     * @param symbol        The symbol to be added.
     * @param unit      The unit to be added.
     * @throws IllegalArgumentException Invalid argument.
     */
    public void
    putSymbol(String symbol, Unit unit)
    {
    if (symbol == null)
        throw new IllegalArgumentException(this.getClass().getName() +
        ".putName(String,Unit): <null> unit symbol");
    if (unit == null)
        throw new IllegalArgumentException(this.getClass().getName() +
        ".putName(String,Unit): <null> unit");
    Unit    prevUnit = (Unit)symbolMap.get(symbol);
    if (prevUnit != null && !prevUnit.equals(unit))
        throw new IllegalArgumentException(
        "Attempt to replace unit \"" + prevUnit + " with unit \"" +
        unit + '"');
    symbolMap.put(symbol, unit);
    unitSet.add(unit);
    }


    /**
     * Get an enumeration of the unit names in the table.  The Object returned
     * by nextElement() is a String.
     */
    public Enumeration
    getNameEnumeration()
    {
    return nameMap.keys();
    }


    /**
     * Get an enumeration of the unit symbols in the table.  The Object returned
     * by nextElement() is a String.
     */
    public Enumeration
    getSymbolEnumeration()
    {
    return symbolMap.keys();
    }


    /**
     * Get an enumeration of the units in the table.  The Object returned
     * by nextElement() is a Unit.
     */
    public Enumeration
    getUnitEnumeration()
    {
    return new Enumeration()
    {
        private final Iterator  iter = unitSet.iterator();
        public boolean hasMoreElements()
        {
        return iter.hasNext();
        }
        public Object nextElement()
        {
        return iter.next();
        }
    };
    }


    /**
     * Return a string representation of this instance.
     */
    public String
    toString()
    {
    return nameMap.toString() + symbolMap.toString();
    }


    /**
     * Test this class.
     * @exception java.lang.Exception   A problem occurred.
     */
    public static void main(String[] args)
    throws Exception
    {
    UnitTable   db = new UnitTable(13);
    db.put(SI.ampere);
    db.put(SI.candela);
    db.put(SI.kelvin);
    db.put(SI.kilogram);
    db.put(SI.meter);
    db.put(SI.mole);
    db.put(SI.second);
    db.put(SI.radian);
    db.putName("amp", SI.ampere);   // alias
    db.putName("celsius", new OffsetUnit(273.15, SI.kelvin));
    db.putName("newton",  
        SI.kilogram.multiply(SI.meter).divide(SI.second.pow(2)));
    db.putName("rankine", new ScaledUnit(1/1.8, SI.kelvin));
    db.putName("fahrenheit",
        new OffsetUnit(459.67, (ScaledUnit)db.get("rankine")));
    System.out.println("db:");
    System.out.println(db.toString());
    }


    /**
     * List the units in the database.
     */
    public void
    list()
    {
    Enumeration en = getUnitEnumeration();
    
    while (en.hasMoreElements())
    {
        Unit    unit = (Unit)en.nextElement();
        System.out.println(unit.getIdentifier() + " = " + 
        unit.getDefinition());
    }
    }
}
