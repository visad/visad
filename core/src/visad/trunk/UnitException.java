
//
// UnitException.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitException.java,v 1.2 1998-04-09 18:04:06 billh Exp $
 */

package visad;

/**
 * A class for exceptions in the units package.
 * @author Steve Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class UnitException 
    extends VisADException    // change by Bill Hibbard for VisAD
{
    /**
     * Create an exception with no detail message.
     */
    UnitException()
    {
	super();
    }

    /**
     * Create an exception with a detail message.
     */
    UnitException(String msg)
    {
	super(msg);
    }
}
