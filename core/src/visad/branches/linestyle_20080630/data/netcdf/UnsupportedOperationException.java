/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnsupportedOperationException.java,v 1.4 1998-04-03 20:35:17 visad Exp $
 */

package visad.data.netcdf;


/**
 * The UnsupportedOperationException provides a way to flag methods that
 * are not implemented.
 */
public class
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
