//
// QuantityDBManager.java
//

/*
 * Copyright 1999, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDBManager.java,v 1.2 2000-04-26 15:45:14 dglo Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
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
	catch (VisADException e)
	{
	    System.err.println("Couldn't initialize class QuantityDBManager");
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
     * @return			The default quantity database.
     * @throws VisADException	Couldn't create necessary VisAD object.
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
	QuantityDBManager.db = db != null
		    ? db
		    : defaultInstance();
    }
}
