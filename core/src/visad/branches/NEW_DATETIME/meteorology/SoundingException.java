/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingException.java,v 1.1 1998-10-21 15:27:59 steve Exp $
 */

package visad.meteorology;

import visad.VisADException;


/**
 * Exception thrown when a VisAD data object is not a sounding
 */
public class
SoundingException
    extends VisADException
{
    /**
     * Constructs from nothing.
     */
    public
    SoundingException()
    {
	super();
    }


    /**
     * Constructs from a message.
     */
    public
    SoundingException(String msg)
    {
	super(msg);
    }
}
