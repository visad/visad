/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingCoordinateSystem.java,v 1.1 1998-10-21 15:27:59 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.Data;
import visad.ErrorEstimate;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular1DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.SI;
import visad.Unit;
import visad.VisADException;


/**
 * Supports conversion between sounding (pressure, temperature, dew point)
 * and Display.SpatialCartesianTuple.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
SoundingCoordinateSystem
    extends	CoordinateSystem
{
    /**
     * The associated Skew-T coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;

    /**
     * The sounding.
     */
    private FlatField			sounding;

    /**
     * The type of the dependent variable.
     */
    private RealType			dependentType;

    /**
     * The unit of pressure in the sounding.
     */
    private Unit			soundingPressureUnit;

    /**
     * The unit of the dependent variable in the sounding.
     */
    private Unit			soundingRangeUnit;


    /**
     * Constructs from a Skew-T coordinate system and the type of the
     * dependent variable.
     *
     * @param skewTCoordSys	The Skew-T coordinate system.
     * @param units		The units for this coordinate system.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SoundingCoordinateSystem(SkewTCoordinateSystem skewTCoordSys, RealType type)
	throws VisADException
    {
	super(
	    skewTCoordSys.getReference(),
	    new Unit[] {
		skewTCoordSys.getPressureUnit(), type.getDefaultUnit(), null});

	this.skewTCoordSys = skewTCoordSys;
	dependentType = type;
    }


    /**
     * Sets the sounding.
     * @param sounding		The sounding or <code>null</code>.
     *				Must have 1-D domain and single range value
     *				with temperature unit.
     * @throws VisADException	Improper sounding.
     */
    public void
    setSounding(FlatField sounding)
	throws VisADException
    {
	this.sounding = sounding == null
				? null
				: vet(sounding);
    }


    /**
     * Vets a sounding.
     *
     * @param sounding		The temperature sounding.
     * @param units		The sounding units.  On return, units[0] and
     *				units[1] will contain, respectively, the
     *				pressure and temperature units of the sounding.
     * @throws VisADException	Improper sounding.
     */
    protected FlatField
    vet(FlatField sounding)
	throws VisADException
    {
	FunctionType	funcType = (FunctionType)sounding.getType();

	/*
	 * Vet domain.
	 */
	RealTupleType	domainSpace = funcType.getDomain();
	if (domainSpace.getDimension() != 1)
	    throw new VisADException("Sounding not 1-D");
	RealType	domainType = (RealType)domainSpace.getComponent(0);
	if (!domainType.equalsExceptNameButUnits(CommonTypes.PRESSURE))
	    throw new VisADException("Sounding domain not pressure");
	soundingPressureUnit = sounding.getDomainUnits()[0];

	/*
	 * Vet range.
	 */
	RealTupleType	rangeSpace = funcType.getFlatRange();
	if (rangeSpace.getDimension() != 1)
	    throw new VisADException("Sounding contains multiple variables");
	RealType	rangeType = (RealType)rangeSpace.getComponent(0);
	if (!rangeType.equalsExceptNameButUnits(dependentType))
	    throw new VisADException("Sounding range-type not compatible");
	soundingRangeUnit = sounding.getRangeUnits()[0][0];

	return sounding;
    }


    /**
     * Transforms coordinates to the reference space.
     *
     * @param coords    Coordinates.  On input, 
     *			<code>coords[0][i]</code>,
     *                  <code>coords[1][i]</code>, and
     *                  <code>coords[2][i] </code> are the
     *                  pressure, temperature, and dew-point
     *                  coordinates, respectively, of the
     *                  <code>i</code>th point of the sounding.
     *                  On output, <code>coords[0][i]</code>,
     *                  <code>coords[1][i]</code>, and
     *                  <code>coords[2][i] </code> are the corresponding
     *                  X, Y, and Z display coordinates, respectively.
     * @return		<code>coords</code>.
     */
    public double[][]
    toReference(double[][] coords)
	throws VisADException
    {
	skewTCoordSys.toReference(coords);	// (p,T,Td) -> (x,y,z=Td)

	double[]	z = coords[2];

	for (int i = 0; i < z.length; ++i)
	    z[i] = 0.0;

	return coords;
    }


    /**
     * Transforms coordinates from the reference space.
     *
     * @param coords    Coordinates: On input, <code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the X, Y,
     *                  and Z display coordinates, respectively,
     *                  of the <code>i</code>th point.  On
     *                  output, <code>coords[0][i]</code>,
     *                  <code>coords[1][i]</code>, and
     *                  <code>coords[2][i] </code> are the corresponding
     *                  pressure, temperature, and dew-point
     *                  coordinates, respectively, of the sounding.
     * @return		<code>coords</code>).
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public double[][]
    fromReference(double[][] coords)
	throws VisADException
    {
	skewTCoordSys.fromReference(coords);	// (x,y,z) -> (p,X,z)

	float[]		pressures = new float[coords[0].length];
	double[]	p = coords[0];

	for (int i = 0; i < pressures.length; ++i)
	    pressures[i] = (float)p[i];

	Irregular1DSet	domainSet = new Irregular1DSet(
	    CommonTypes.PRESSURE,
	    new float[][] {pressures},
	    (CoordinateSystem)null, 
	    new Unit[] {skewTCoordSys.getPressureUnit()}, 
	    (ErrorEstimate[])null);

	if (sounding == null)
	{
	    double[]	vals = coords[1];

	    for (int i = 0; i < vals.length; ++i)
		vals[i] = Double.NaN;
	}
	else
	{
	    resample(
		sounding, domainSet, pressures,
		soundingRangeUnit,
		getRangeUnit(), 
		coords[1]);
	}

	double[]	vals = coords[1];

	for (int i = 0; i < vals.length; ++i)
	    if (vals[i] != vals[i])
		p[i] = Double.NaN;

	return coords;
    }


    /**
     * Resamples a sounding.
     *
     * @param sounding		The sounding to be resampled.
     * @param domainSet		The points at which to resample.
     * @param domainPts		The original domain points.
     * @param soundingRangeUnit	The unit of the sounding.
     * @param displayUnit	The unit of the display.
     * @param coords		The output values.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static void
    resample(FlatField sounding, Irregular1DSet domainSet, float[] domainPts,
	    Unit soundingRangeUnit, Unit displayUnit, double[] values)
	throws VisADException
    {
	try
	{
	    Field	field = sounding.resample(
		domainSet, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
	    double[]	vals =
		soundingRangeUnit.toThat(field.getValues()[0], displayUnit);

	    for (int i = 0; i < values.length; ++i)
		values[i] = Double.NaN;

	    int[]	indexes = domainPts.length < 2
		? new int[] {0}
		: domainSet.valueToIndex(new float[][] {domainPts});

	    for (int i = 0; i < indexes.length; ++i)
	    {
		int	j = indexes[i];
		if (j >= 0 && j < values.length)
		    values[j] = vals[i];
	    }
	}
	catch (RemoteException e)
	{
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Gets the pressure unit.
     */
    public Unit
    getPressureUnit()
    {
	return getCoordinateSystemUnits()[0];
    }


    /**
     * Gets the dependent variable unit.
     */
    public Unit
    getRangeUnit()
    {
	return getCoordinateSystemUnits()[1];
    }


    /**
     * Gets the associated Skew T, Log P coordinate system.
     *
     * @return	The associated Skew T, Log P coordinate system.
     */
    public SkewTCoordinateSystem
    getSkewTCoordSys()
    {
	return skewTCoordSys;
    }


    /*
     * Indicates if this coordinate system equals an object.
     *
     * @param obj	The object to be compared with this one.
     * @return		<code>true</code> if and only if <code>obj</code> is
     *			semantically identical to this object.
     */
    public boolean
    equals(java.lang.Object obj)
    {
	if (!(obj instanceof SoundingCoordinateSystem))
	    return false;

	SoundingCoordinateSystem	that = (SoundingCoordinateSystem)obj;

	return 
	    skewTCoordSys.equals(that.skewTCoordSys) &&
	    sounding.equals(that.sounding) &&
	    dependentType.equals(that.dependentType);
    }
}
