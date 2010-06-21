/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitsDB.java,v 1.3 2009-04-21 20:15:10 steve Exp $
 */

package visad.data.units;


import java.util.Enumeration;
import visad.BaseUnit;
import visad.Unit;


/**
 * The units database interface.
 *
 * This class exists to allow the user to construct their own units database.
 */
public interface
UnitsDB
{
    /**
     * Adds a base unit.
     *
     * @param baseUnit  The base unit to be added.
     * @throws java.lang.IllegalArgumentException
     *          The base unit argument is invalid.
     */
    void
    put(BaseUnit unit)
    throws IllegalArgumentException;


    /**
     * Adds a name and a unit to the name table.
     * @param name      The name to be added.
     * @param unit      The unit to be added.
     * @throws IllegalArgumentException Different unit with the same name is
     *                  already in the table.
     */
    void
    putName(String name, Unit unit)
    throws IllegalArgumentException;


    /**
     * Adds a symbol and a unit to the symbol table.
     * @param symbol        The symbol to be added.
     * @param unit      The unit to be added.
     * @throws IllegalArgumentException Different unit with the same symbol is
     *                  already in the table.
     */
    void
    putSymbol(String symbol, Unit unit)
    throws IllegalArgumentException;


    /**
     * Get a unit.
     *
     * @param name  The name of the unit to be retrieved from the database.
     * @return      The matching unit entry in the database.
     * @require     <code>name</code> shall be non-null.
     */
    Unit
    get(String name);


    /**
     * Get an enumeration of the unit names in the database.
     */
    Enumeration
    getNameEnumeration();


    /**
     * Get an enumeration of the unit symbols in the database.
     */
    Enumeration
    getSymbolEnumeration();


    /**
     * Get an enumeration of the units in the database.
     */
    Enumeration
    getUnitEnumeration();


    /**
     * List the units in the database.
     */
    void
    list();
}
