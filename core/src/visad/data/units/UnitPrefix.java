/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitPrefix.java,v 1.1 2000-11-17 18:54:46 dglo Exp $
 */

package visad.data.units;


/**
 * Class for representing unit prefixes.
 */
public class
UnitPrefix
    implements	java.io.Serializable
{
    /**
     * The name of the prefix:
     */
    public final String	name;

    /**
     * The value of the prefix:
     */
    public final double	value;


    /**
     * Construct.
     *
     * @param name	The name of the prefix (e.g. "mega", "M").
     * @param value	The value of the prefix (e.g. 1e6).
     * @require		The arguments shall be non-null.
     * @exception IllegalArgumentException	One of the arguments was null.
     */
    public
    UnitPrefix(String name, double value)
    {
	if (name == null)
	    throw new IllegalArgumentException("Null prefix name");

	this.name = name;
	this.value = value;
    }
}
