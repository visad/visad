/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Sounding.java,v 1.2 1998-10-21 15:27:59 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SI;
import visad.Set;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.units.Parser;


/**
 * Provides support for atmospheric soundings.
 *
 * Instances are mutable.
 */
public class
Sounding
{
    /*
     * The temperature sounding.
     */
    private FlatField		temperature;

    /*
     * The dew-point sounding.
     */
    private FlatField		dewPoint;


    /**
     * Constructs from a FlatField and types for the pressure, temperature,
     * and dew-point variables.
     *
     * @param field			A sounding FlatField.  Domain must
     *					be 1-D pressure.
     * @param pressureType		The type for the pressure domain.
     * @param temperatureType		The type for the temperature
     *					variable.
     * @param dewPointType		The type for the dew-point variable.
     * @throws SoundingException	<code>field</code> is not a sounding.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     */
    public
    Sounding(FlatField field, RealType pressureType, RealType temperatureType,
	    RealType dewPointType)
	throws	SoundingException, VisADException, RemoteException
    {
	FunctionType	fieldType = (FunctionType)field.getType();
	RealType[]	fieldDomain = fieldType.getDomain().getRealComponents();

	if (fieldDomain.length != 1)
	    throw new SoundingException("Field domain isn't 1-D");

	if (!fieldDomain[0].equalsExceptNameButUnits(CommonTypes.PRESSURE))
	    throw new SoundingException("Field domain isn't pressure");

	RealType[]	fieldRangeTypes =
	    fieldType.getFlatRange().getRealComponents();

	boolean	dewPointSeen = false;
	boolean	temperatureSeen = false;

	temperature = null;
	dewPoint = null;

	for (int i = 0; i < fieldRangeTypes.length; ++i)
	{
	    RealType	rangeType = fieldRangeTypes[i];
	    String	name = rangeType.getName();

	    if (rangeType.equalsExceptNameButUnits(CommonTypes.TEMPERATURE))
	    {
		if (!dewPointSeen &&
		    name.regionMatches(/*ignoreCase=*/true, /*tooffset=*/0,
			"dew", /*ooffset=*/0, 3))
		{
		    FunctionType	funcType =
			new FunctionType(pressureType, dewPointType);
		    dewPoint = createSounding(funcType, field, i);
		    dewPointSeen = true;
		}
		else if (!temperatureSeen)
		{
		    FunctionType	funcType =
			new FunctionType(pressureType, temperatureType);
		    temperature = createSounding(funcType, field, i);
		    temperatureSeen = true;
		}
	    }

	    if (dewPointSeen && temperatureSeen)
	    {
		break;
	    }
	}
    }


    /**
     * Creates a single-parameter sounding.
     *
     * @param funcType		The type of the FlatField to create.
     * @param field		The input field from which to extract a
     *				single-parameter sounding.
     * @param index		The component of <code>field</code> to extract.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    protected static FlatField
    createSounding(FunctionType funcType, FlatField field, int index)
	throws VisADException, RemoteException
    {
	FlatField	profile = (FlatField)field.extract(index);
	FlatField	sounding = new FlatField(
	    funcType,
	    profile.getDomainSet(),
	    (CoordinateSystem)null,
	    (Set[])null,
	    profile.getDefaultRangeUnits());

	sounding.setSamples(profile.getValues(), false);

	return sounding;
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			The temperature sounding or <code>null</code>
     *				if none exists.
     */
    public FlatField
    getTemperature()
    {
	return temperature;
    }


    /**
     * Gets the dew-point sounding.
     *
     * @return			The dew-point sounding or <code>null</code>
     *				if none exists.
     */
    public FlatField
    getDewPoint()
    {
	return dewPoint;
    }
}
