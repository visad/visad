//
// QuantityDBManager.java
//

/*
 * Copyright 1999, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDBManager.java,v 1.3 2001-04-03 19:12:26 steve Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import visad.VisADError;
import visad.VisADException;


/**
 * Provides support for managing a database of quantities.  This class
 * insulates the user from knowing or caring, for example, whether or not
 * the quantity database is unchanging or whether it's implemented using
 * singletons.
 *
 * @author Steven R. Emmerson
 */
public final class
QuantityDBManager
    implements	Serializable
{
    /**
     * The singleton instance of the current database.
     */
    private static QuantityDB	db;


    static
    {
	try
	{
	    db = defaultInstance();
	}
	catch (Exception e)
	{
	    if (e instanceof RuntimeException)
		throw (RuntimeException)e;
	    throw new VisADError(
		"visad.data.netcdf.QuantityDBManager.<clinit>: " +
		"Couldn't initialize class" + e);
	}
    }


    /**
     * Constructs from nothing.  Private to prevent instantiation.
     */
    private
    QuantityDBManager()
    {
    }


    /**
     * Returns the default quantity database.
     *
     * @return                  The default quantity database.
     * @throws VisADException   Couldn't create necessary VisAD object.
     */
    protected static QuantityDB
    defaultInstance()
        throws VisADException
    {
        return StandardQuantityDB.instance();
    }


    /**
     * Returns the current instance of the quantity database.
     *
     * @return			The current instance of the quantity database.
     */
    public static synchronized QuantityDB
    instance()
    {
	return db;
    }


    /**
     * Sets the current instance of the quantity database.
     *
     * @param db		The new current instance of the quantity
     *				database.  May be <code>null</code>, in which
     *				case the default instance is used.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public static synchronized void
    setInstance(QuantityDB db)
	throws VisADException
    {
	QuantityDBManager.db = db == null ? defaultInstance() : db;
    }


    /**
     * Tests this class by listing the contents -- one per line --
     * in the following format:
     *	    <name> ( <CannonicalName> ) in <PreferredUnit>
     * e.g.
     *	    VolumicElectricCharge (ElectricChargeDensity) in C/m3
     */
    public static void
    main(String[] args)
      throws	Exception
    {
	QuantityDB	db = QuantityDBManager.instance();

	for (java.util.Iterator iter = db.nameIterator(); iter.hasNext(); )
	{
	    String	name = (String)iter.next();
	    Quantity	quantity = db.get(name);
	    System.out.println(
		name + " (" + quantity.getName() + ") in " +
		quantity.getDefaultUnitString());
	}
    }
}
