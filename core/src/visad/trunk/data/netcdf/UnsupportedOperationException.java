/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnsupportedOperationException.java,v 1.3 1998-03-12 22:03:18 steve Exp $
 */

package visad.data.netcdf;


/**
 * The UnsupportedOperationException provides a way to flag methods that
 * are not implemented.
 */
class
UnsupportedOperationException
    extends NoSuchMethodError
{
    public
    UnsupportedOperationException()
    {
	super();
    }

    /**
     * Construct from a message.
     *
     * @param msg	The message.
     */
    public
    UnsupportedOperationException(String msg)
    {
	super(msg);
    }
}
