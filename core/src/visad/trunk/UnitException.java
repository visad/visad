
//
// UnitException.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitException.java,v 1.3 1998-09-24 21:53:40 steve Exp $
 */

package visad;

/**
 * A class for exceptions in the units package.
 * @author Steve Emmerson
 *
 * This is part of Steve Emmerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class UnitException 
    extends VisADException    // change by Bill Hibbard for VisAD
{
    /**
     * Create an exception with no detail message.
     */
    public UnitException()
    {
	super();
    }

    /**
     * Create an exception with a detail message.
     */
    public UnitException(String msg)
    {
	super(msg);
    }
}
