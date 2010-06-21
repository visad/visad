/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InvalidContextException.java,v 1.1 1998-09-23 17:31:32 steve Exp $
 */

package visad.data.netcdf.in;

import visad.VisADException;


/**
 * Exception thrown when the I/O context is invalid for an operation.
 */
public class
InvalidContextException
    extends VisADException
{
    /**
     * Constructs from nothing.
     */
    public
    InvalidContextException()
    {
	super();
    }


    /**
     * Constructs from a message.
     */
    public
    InvalidContextException(String msg)
    {
	super(msg);
    }


    /**
     * Constructs from a context.
     */
    public
    InvalidContextException(Context context)
    {
	super(context.toString());
    }
}
