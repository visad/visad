/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TemperatureSoundingImpl.java,v 1.3 1999-01-07 16:13:20 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.Data;
import visad.ErrorEstimate;
import visad.Field;
import visad.FlatField;
import visad.Gridded1DSet;
import visad.Integer1DSet;
import visad.Real;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric temperature soundings.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
TemperatureSoundingImpl
    extends	SingleSoundingImpl
    implements	TemperatureSounding
{
    /**
     * The range type.
     */
    private static final RealType	rangeType = TEMPERATURE_TYPE;


    /**
     * Constructs from nothing.  The data will be missing.
     *
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    TemperatureSoundingImpl()
	throws VisADException, IllegalArgumentException
    {
	this(new Integer1DSet(DOMAIN_TYPE, 2), rangeType.getDefaultUnit());
    }


    /**
     * Constructs from a domain set and range unit.  The data will be missing.
     *
     * @param domainSet			The domain set of the function.
     * @param temperatureUnit		The unit for the temperature values.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    TemperatureSoundingImpl(Set domainSet, Unit temperatureUnit)
	throws VisADException, IllegalArgumentException
    {
	super(rangeType, domainSet, temperatureUnit);
    }


    /**
     * Constructs from a FlatField and a component index.
     *
     * @param field		The VisAD FlatField to emulate.
     * @param index		The range-index of the temperature component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected
    TemperatureSoundingImpl(FlatField field, int index)
	throws VisADException, RemoteException
    {
	super(rangeType, field, index);
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			<code>this</code>.
     */
    public TemperatureSounding
    getTemperatureSounding()
    {
	return this;
    }


    /**
     * Gets the sounding temperature at a given pressure.
     * @param pressure		The pressure at which to get the temperature.
     * @return			The temperature at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getTemperature(Real pressure)
	throws VisADException, RemoteException
    {
	return (Real)getValue(pressure);
    }


    /**
     * Gets the sounding temperature at given pressures.
     * @param pressure		The pressures at which to get the temperature.
     * @return			The temperature at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Gridded1DSet
    getTemperature(Gridded1DSet pressure)
	throws VisADException, RemoteException
    {
	return (Gridded1DSet)getValue(pressure);
    }
}
