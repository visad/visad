
//
// UnitExistsException.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitExistsException.java,v 1.1 1999-09-20 20:22:04 steve Exp $
 */

package visad;

/**
 * Provides support for attempting to define a unit with a previously-used
 * identifier.
 *
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emmerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class UnitExistsException 
    extends UnitException
{
    /**
     * Creates an exception from a unit identifier.
     */
    public UnitExistsException(String id)
    {
	super("Unit \"" + id + "\" already exists");
    }
}
