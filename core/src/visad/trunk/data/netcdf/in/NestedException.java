/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NestedException.java,v 1.1 1998-09-11 15:00:55 steve Exp $
 */

package visad.data.netcdf.in;


import visad.VisADException;


/**
 * Exception thrown when assuming that that a nested data object isn't and
 * vice versa.
 *
 * @see NcNestedField
 */
public class
NestedException
    extends VisADException
{
    /**
     * Constructs from nothing.
     */
    public NestedException()
    {
	super();
    }


    /**
     * Constructs from a message.
     */
    public NestedException(String msg)
    {
	super(msg);
    }
}
