//
// QuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.5 1999-01-07 17:01:29 steve Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import visad.Unit;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Provides support for a database of quantities.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public abstract class
QuantityDB
    implements	Serializable
{
    /**
     * Return the quantity in the database whose name matches a 
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantity in the loal database with name
     *			<code>name</code>.
     */
    public abstract Quantity get(String name);


    /**
     * Return all quantities in the database whose default unit is
     * convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The quantities in the database whose unit is 
     *			convertible with <code>unit</code>.
     */
    public abstract Quantity[] get(Unit unit);
}
