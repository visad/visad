
//
// Factor.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Factor.java,v 1.3 1999-01-20 18:31:25 steve Exp $
 */

package visad;

import java.io.Serializable;

/**
 * A class that represents a factor in the dimensionality of a unit.
 * A factor is a base unit and a power.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
final class Factor
    implements Serializable
{
    /**
     * The power of the base unit.
     */
    final int		power;

    /**
     * The base unit.
     */
    final BaseUnit	baseUnit;


    /**
     * Construct a factor from a base unit and a power.
     *
     * @param baseUnit	The base unit.
     * @param power	The power to raise the base unit by.
     */
    Factor(BaseUnit baseUnit, int power)
    {
	this.power = power;
	this.baseUnit = baseUnit;
    }

    /**
     * Return a string representation of this factor.
     *
     * @return	A string representation of this factor (e.g. "m-2").
     */
    public String toString()
    {
	return power == 1
		? baseUnit.toString()
		: baseUnit.toString() + power;
    }

  public boolean equals(Factor factor) {
    return baseUnit.equals(factor.baseUnit) &&
           (power == factor.power);
  }

}

