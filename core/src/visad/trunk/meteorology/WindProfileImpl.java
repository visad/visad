/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: WindProfileImpl.java,v 1.3 1999-01-07 16:13:21 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.Integer1DSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.SI;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric wind soundings.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
WindProfileImpl
    extends	SingleSoundingImpl
    implements	WindProfile
{
    /**
     * The range type.
     */
    private static final RealTupleType	rangeType = WIND_TYPE;


    /**
     * Constructs an empty wind profile from nothing.  The data will be missing.
     *
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    WindProfileImpl()
	throws VisADException, IllegalArgumentException
    {
	this(new Integer1DSet(DOMAIN_TYPE, 2), CommonUnits.KNOT);
    }


    /**
     * Constructs from a domain set and range unit.  The data will be missing.
     *
     * @param domainSet			The domain set of the function.
     * @param speedUnit			The unit for the wind speed.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    WindProfileImpl(Set domainSet, Unit speedUnit)
	throws VisADException, IllegalArgumentException
    {
	super(rangeType, domainSet, new Unit[] {speedUnit, speedUnit});
    }


    /**
     * Constructs from a FlatField and a component index.
     *
     * @param field		The VisAD FlatField to emulate.
     * @param index		The range-index of the wind component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected
    WindProfileImpl(FlatField field, int index)
	throws VisADException, RemoteException
    {
	super(rangeType, field, index);
    }


    /**
     * Gets the wind profile.
     *
     * @return			<code>this</code>.
     */
    public WindProfile
    getWindProfile()
    {
	return this;
    }


    /**
     * Gets the profile wind speed at a given pressure.
     * @param pressure		The pressure at which to get the wind speed.
     * @return			The wind speed at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getSpeed(Real pressure)
	throws VisADException, RemoteException
    {
	RealTuple	velocity = (RealTuple)getValue(pressure);
	double		u = ((Real)velocity.getComponent(0)).getValue();
	double		v = ((Real)velocity.getComponent(1)).getValue();
	double		value = Math.sqrt(u*u + v*v);
	return new Real(
	    CommonTypes.SPEED,
	    value,
	    ((Real)velocity.getComponent(0)).getUnit());
    }


    /**
     * Gets the profile wind direction at a pressure.
     * @param pressure		The pressure at which to get the wind direction.
     * @return			The wind direction at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getDirection(Real pressure)
	throws VisADException, RemoteException
    {
	RealTuple	velocity = (RealTuple)getValue(pressure);
	double		u = ((Real)velocity.getComponent(0)).getValue();
	double		v = ((Real)velocity.getComponent(1)).getValue();
	/*
	 * Negating and transposing the U and V components in the following
	 * converts the direction from mathematical to meteorological.
	 */
	double		value = Math.atan2(-u, -v);
	if (value < 0)
	    value += 2*Math.PI;
	return new Real(CommonTypes.DIRECTION, value, SI.radian);
    }
}
