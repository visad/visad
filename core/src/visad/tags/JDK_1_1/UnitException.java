
//
// UnitException.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitException.java,v 1.1 1997-10-23 20:14:07 dglo Exp $
 */

package visad;

import java.io.Serializable;

/**
 * A class for exceptions in the units package.
 * @author Steve Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class UnitException 
    extends VisADException    // change by Bill Hibbard for VisAD
    implements Serializable
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
