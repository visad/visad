/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingImpl.java,v 1.1 1998-10-28 17:16:50 steve Exp $
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
 *
 * @author Steven R. Emmerson
 */
public class
SoundingImpl
    extends	AbstractSounding
{
    /*
     * The original field.
     */
    private final FlatField	field;

    /*
     * The temperature sounding.
     */
    private FlatField		temperature;

    /*
     * The dew-point sounding.
     */
    private FlatField		dewPoint;

    /*
     * The wind-velocity profile.
     */
    private FlatField		windProfile;

    /**
     * The index of the temperature range-component.
     */
    private int			temperatureIndex;

    /**
     * The index of the dew-point range-component.
     */
    private int			dewPointIndex;


    /**
     * Constructs from a FlatField and variable types.  The types are
     * specified so that they are known.  This allows well-known ScalarMap-s.
     *
     * @param field			A sounding FlatField.  Domain must
     *					be 1-D pressure.
     * @param pressureType		The type for the pressure domain.
     * @param temperatureType		The type for the temperature
     *					variable or <code>null</code>.
     * @param dewPointType		The type for the dew-point variable or
     *					<code>null</code>.
     * @param uvType			The type for the wind or <code>null
     *					</code>.
     * @throws SoundingException	<code>field</code> is not a sounding.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null</code> or a
     *					range type is inappropriate.
     */
    public
    SoundingImpl(FlatField field, RealType pressureType,
	    RealType temperatureType, RealType dewPointType,
	    RealTupleType uvType)
	throws	SoundingException, VisADException, RemoteException
    {
	super(field);

	this.field = field;

	if (field == null)
	    throw new IllegalArgumentException("Null input field");

	if (!pressureType.equalsExceptNameButUnits(CommonTypes.PRESSURE))
	    throw new IllegalArgumentException("Invalid pressure type");

	if (temperatureType != null &&
	    !temperatureType.equalsExceptNameButUnits(CommonTypes.TEMPERATURE))
	    throw new IllegalArgumentException("Invalid temperature type");

	if (dewPointType != null &&
	    !dewPointType.equalsExceptNameButUnits(CommonTypes.TEMPERATURE))
	    throw new IllegalArgumentException("Invalid dew-point type");

	if (uvType != null &&
	    !uvType.equalsExceptNameButUnits(
		new RealTupleType(CommonTypes.U, CommonTypes.V)))
	    throw new IllegalArgumentException("Invalid U/V wind type");

	FunctionType	fieldType = (FunctionType)field.getType();
	RealType[]	fieldDomain = fieldType.getDomain().getRealComponents();

	if (fieldDomain.length != 1)
	    throw new SoundingException("Field domain isn't 1-D");

	if (!fieldDomain[0].equalsExceptNameButUnits(CommonTypes.PRESSURE))
	    throw new SoundingException("Field domain isn't pressure");

	dewPointIndex = -1;
	temperatureIndex = -1;
	int	uIndex = -1;
	int	vIndex = -1;
	int	speedIndex = -1;
	int	directionIndex = -1;

	RealType[]	fieldRangeTypes =
	    fieldType.getFlatRange().getRealComponents();

	for (int i = 0; i < fieldRangeTypes.length; ++i)
	{
	    RealType	rangeType = fieldRangeTypes[i];
	    String	lowerName = rangeType.getName().toLowerCase();

	    if (uvType != null && directionIndex < 0 &&
		rangeType.equalsExceptNameButUnits(CommonTypes.DIRECTION))
	    {
		directionIndex = i;
	    }
	    else
	    if (rangeType.equalsExceptNameButUnits(CommonTypes.TEMPERATURE))
	    {
		if (dewPointIndex < 0 && lowerName.indexOf("dew") >= 0)
		    dewPointIndex = i;
		else if (temperatureIndex < 0)
		    temperatureIndex = i;
	    }
	    else
	    if (uvType != null &&
		rangeType.equalsExceptNameButUnits(CommonTypes.U))
	    {
		if (uIndex < 0 && (
		    lowerName.indexOf("east") >= 0 ||
		    lowerName.indexOf("west") >= 0 ||
		    lowerName.startsWith("u") ||
		    lowerName.startsWith("x")))
		{
		    uIndex = i;
		}
		else
		if (vIndex < 0 && (
		    lowerName.indexOf("north") >= 0 ||
		    lowerName.indexOf("south") >= 0 ||
		    lowerName.startsWith("v") ||
		    lowerName.startsWith("y")))
		{
		    vIndex = i;
		}
		else
		if (speedIndex < 0 && (
		    lowerName.indexOf("speed") >= 0 ||
		    lowerName.indexOf("spd") >= 0))
		{
		    speedIndex = i;
		}
	    }

	    if (temperatureIndex >= 0 && dewPointIndex >= 0 &&
		((uIndex >= 0 && vIndex >= 0) || 
		 (speedIndex >= 0 && directionIndex >= 0)))
	    {
		break;
	    }
	}

	if (temperatureIndex < 0)
	{
	    temperature = null;
	}
	else
	{
	    FunctionType	funcType =
		new FunctionType(pressureType, temperatureType);
	    temperature = createSounding(funcType, field, temperatureIndex);
	}

	if (dewPointIndex < 0)
	{
	    dewPoint = null;
	}
	else
	{
	    FunctionType	funcType =
		new FunctionType(pressureType, dewPointType);
	    dewPoint = createSounding(funcType, field, dewPointIndex);
	}

	if (uIndex >= 0 && vIndex >= 0)
	{
	    FunctionType	funcType =
		new FunctionType(pressureType, uvType);
	    windProfile =
		createWindProfileFromUV(funcType, field, uIndex, vIndex);
	}
	else
	if (speedIndex >= 0 && directionIndex >= 0)
	{
	    FunctionType	funcType =
		new FunctionType(pressureType, uvType);
	    windProfile =
		createWindProfileFromSpdDir(funcType, field, speedIndex, 
		    directionIndex);
	}
	else
	{
	    windProfile = null;
	}
    }


    /**
     * Constructs from a FlatField and variable types.  The types are
     * specified so that they are known.  This allows well-known ScalarMap-s.
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
    SoundingImpl(FlatField field, RealType pressureType,
	    RealType temperatureType, RealType dewPointType)
	throws	SoundingException, VisADException, RemoteException
    {
	this(field, pressureType, temperatureType, dewPointType, null);
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
     * Creates a wind profile from U and V components.
     *
     * @param funcType		The type of the FlatField to create.
     * @param field		The input field from which to extract a
     *				single-parameter sounding.
     * @param uIndex		The index of the U-component of the wind.
     * @param vIndex		The index of the V-component of the wind.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    protected static FlatField
    createWindProfileFromUV(FunctionType funcType, FlatField field, 
	    int uIndex, int vIndex)
	throws VisADException, RemoteException
    {
	FlatField	uProfile = (FlatField)field.extract(uIndex);
	FlatField	vProfile = (FlatField)field.extract(vIndex);
	FlatField	profile = new FlatField(
	    funcType,
	    field.getDomainSet(),
	    (CoordinateSystem)null,
	    (Set[])null,
	    new Unit[] {
		uProfile.getDefaultRangeUnits()[0],
		vProfile.getDefaultRangeUnits()[0]});

	profile.setSamples(
	    new double[][] {uProfile.getValues()[0], vProfile.getValues()[0]},
	    false);

	return profile;
    }


    /**
     * Creates a wind profile from speed and direction components.
     *
     * @param funcType		The type of the FlatField to create.
     * @param field		The input field from which to extract a
     *				single-parameter sounding.
     * @param speedIndex	The index of the speed-component of the wind.
     * @param directionIndex	The index of the direction-component of the
     *				wind.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    protected static FlatField
    createWindProfileFromSpdDir(FunctionType funcType, FlatField field, 
	    int speedIndex, int directionIndex)
	throws VisADException, RemoteException
    {
	FlatField	spdProfile = (FlatField)field.extract(speedIndex);
	FlatField	dirProfile = (FlatField)field.extract(directionIndex);
	Unit		spdUnit = spdProfile.getDefaultRangeUnits()[0];
	FlatField	profile = new FlatField(
	    funcType,
	    field.getDomainSet(),
	    (CoordinateSystem)null,
	    (Set[])null,
	    new Unit[] {spdUnit, spdUnit});

	double[]	comp1 = spdProfile.getValues()[0];
	double[]	comp2 = dirProfile.getValues()[0];
	int		npts = comp1.length;

	comp2 = dirProfile.getDefaultRangeUnits()[0].toThat(comp2, SI.radian);

	for (int i = 0; i < npts; ++i)
	{
	    double	speed = comp1[i];
	    double	direction = comp2[i];

	    /*
	     * Transposing sin() and cos() in the following converts
	     * the direction from meteorological to mathematical.
	     */
	    comp1[i] = speed * Math.sin(direction);	// U
	    comp2[i] = speed * Math.cos(direction);	// V
	}

	profile.setSamples(new double[][] {comp1, comp2}, false);

	return profile;
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			The temperature sounding or <code>null</code>
     *				if none exists.
     */
    public FlatField
    getTemperatureSounding()
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
    getDewPointSounding()
    {
	return dewPoint;
    }


    /**
     * Gets the wind profile.
     *
     * @return			The wind profile or <code>null</code>
     *				if none exists.
     */
    public FlatField
    getWindProfile()
    {
	return windProfile;
    }


    /**
     * Gets the index of the dew-point range-component.
     *
     * @return		The index of the dew-point range-component or -1
     *			if none exists.
     */
    public int
    getDewPointIndex()
    {
	return -1;
    }
}
