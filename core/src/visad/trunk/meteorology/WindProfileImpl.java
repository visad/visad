/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: WindProfileImpl.java,v 1.2 1998-11-16 18:23:50 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.Integer1DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
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
}
