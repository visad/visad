/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingImpl.java,v 1.3 1998-11-16 18:23:50 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SI;
import visad.ScalarType;
import visad.Set;
import visad.Tuple;
import visad.TupleType;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
SoundingImpl
    extends	AbstractSounding
{
    /**
     * Whether or not to convert the wind.
     */
    private /*final*/ boolean	convertWind;

    /**
     * The canonical type of the range of this field.
     */
    private static final TupleType	canonicalRangeType;


    static
    {
	TupleType	crt = null;
	try
	{
	    crt = new TupleType(
		    new MathType[] {
			TEMPERATURE_TYPE,
			DEW_POINT_TYPE,
			UV_WIND_TYPE});
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();
	    System.err.println("Couldn't initialize class SoundingImpl" +
		(reason == null ? "" : (": " + reason)));
	    e.printStackTrace();
	}
	canonicalRangeType = crt;
    }


    /**
     * Constructs from nothing.
     *
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null</code>.
     */
    public
    SoundingImpl()
	throws VisADException
    {
	super(
	    canonicalRangeType,
	    new Integer1DSet(DOMAIN_TYPE, 2),
	    new Unit[] {
		TEMPERATURE_TYPE.getDefaultUnit(),
		DEW_POINT_TYPE.getDefaultUnit(),
		UV_WIND_TYPE.getDefaultUnits()[0],
		UV_WIND_TYPE.getDefaultUnits()[1]});

	    convertWind = false;
    }


    /**
     * Constructs from a FlatField and component indexes.  The range
     * of the FlatField shall be RealType or RealTupleType.
     *
     * @param field			A sounding FlatField.  Domain must
     *					be 1-D pressure.
     * @param temperatureIndex		The index of the temperature
     *					range-component or <code>-1</code>.
     * @param humidityIndex		The index of the dew-point range-
     *					component or <code>-1</code>.
     * @param USpdIndex			The index of the U-component of the
     *					wind, the speed of the wind, or
     *					<code>-1</code>.
     * @param VDirIndex			The index of the V-component of the
     *					wind, the direction of the wind, or
     *					<code>-1</code>.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null</code>.
     */
    public
    SoundingImpl(FlatField field, int temperatureIndex, int humidityIndex,
	    int USpdIndex, int VDirIndex)
	throws	VisADException, RemoteException
    {
	super(
	    getRangeType(
		field,
		temperatureIndex,
		humidityIndex,
		USpdIndex,
		VDirIndex),
	    field.getDomainSet(),
	    getRangeUnits(
		field,
		temperatureIndex,
		humidityIndex,
		USpdIndex,
		VDirIndex));

	convertWind = !isValidIndex(USpdIndex) || !isValidIndex(VDirIndex)
	    ? false
	    : POLAR_WIND_TYPE.equalsExceptNameButUnits(
		getWindType(
		    getRangeTupleType(field),
		    USpdIndex,
		    VDirIndex));

	double[][]	values = new double[getRangeDimension()][];

	int	i = 0;				// flattened new-range index
	if (isValidIndex(temperatureIndex))
	    values[i++] = getTemperatureValues(field, temperatureIndex);
	if (isValidIndex(humidityIndex))
	    values[i++] =
		getDewPointValues(field, humidityIndex, temperatureIndex);
	if (areValidIndexes(USpdIndex, VDirIndex))
	{
	    System.arraycopy(
		getWindValues(field, USpdIndex, VDirIndex), 0, values, i, 2);
	    // i += 2;
	}

	setSamples(values, /*copy=*/false);
    }


    /**
     * Gets the type of the FlatField range formed from the
     * specified range components of another FlatField.
     *
     * @param field			The FlatField containing the range
     *					components to be extracted.
     * @param temperatureIndex		The index of the temperature
     *					range-component or <code>-1</code>.
     * @param humidityIndex		The index of the dew-point range-
     *					component or <code>-1</code>.
     * @param USpdIndex			The index of the U-component of the
     *					wind, the speed of the wind, or
     *					<code>-1</code>.
     * @param VDirIndex			The index of the V-component of the
     *					wind, the direction of the wind, or
     *					<code>-1</code>.
     * @throws TypeException		A range-component is incompatible.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null</code>.
     */
    protected static MathType
    getRangeType(FlatField field, int temperatureIndex, int humidityIndex,
		int USpdIndex, int VDirIndex)
	throws TypeException, VisADException, RemoteException
    {
	if (field == null)
	    throw new IllegalArgumentException("Null field");

	boolean	validTemperature = isValidIndex(temperatureIndex);
	boolean	validDewPoint = isValidIndex(humidityIndex);
	boolean	validWind = areValidIndexes(USpdIndex, VDirIndex);

	/*
	 * Vet the soon-to-be-extracted components.
	 */
	int		count = 0;
	TupleType	rangeTupleType = getRangeTupleType(field);
	if (validTemperature)
	{
	    if (!TEMPERATURE_TYPE.equalsExceptNameButUnits(
		getTemperatureType(rangeTupleType, temperatureIndex)))
	    {
		throw new TypeException(
		    "Incompatible \"temperature\" component");
	    }
	    count++;
	}
	if (validDewPoint)
	{
	    if (!DEW_POINT_TYPE.equalsExceptNameButUnits(
		getDewPointType(
		    rangeTupleType, humidityIndex, temperatureIndex)))
	    {
		throw new TypeException("Incompatible \"dew-point\" component");
	    }
	    count++;
	}
	if (validWind)
	{
	    RealTupleType	windType =
		getWindType(rangeTupleType, USpdIndex, VDirIndex);
	    if (!UV_WIND_TYPE.equalsExceptNameButUnits(windType) &&
		!POLAR_WIND_TYPE.equalsExceptNameButUnits(windType))
	    {
		throw new TypeException("Incompatible \"wind\" component");
	    }
	    count++;
	}

	MathType[]	rangeTypes = new MathType[count];

	int	i = 0;				// new-range component index
	if (validTemperature)
	    rangeTypes[i++] = TEMPERATURE_TYPE;
	if (validDewPoint)
	    rangeTypes[i++] = DEW_POINT_TYPE;
	if (validWind)
	    rangeTypes[i++] = UV_WIND_TYPE;

	return getSimplestForm(rangeTypes);
    }


    /**
     * Gets the type of the temperature range component.
     *
     * @param tupleType		The field range to examine.
     * @param index		The index of the temperature component in
     *				<code>field </code>.
     * @throws TypeException	The components are incompatible with the
     *				expected type.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static RealType
    getTemperatureType(TupleType tupleType, int index)
	throws VisADException, TypeException
    {
	return (RealType)tupleType.getComponent(index);
    }


    /**
     * Gets the type of the dew-point range component.
     *
     * @param tupleType		The field range to examine.
     * @param humidityIndex	The index of the humidity component in
     *				<code>field </code>.
     * @param temperatureIndex	The index of the temperature component in
     *				<code>field </code>.
     * @throws TypeException	The components are incompatible with the
     *				expected type.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static RealType
    getDewPointType(TupleType tupleType, int humidityIndex,
	    int temperatureIndex)
	throws VisADException, TypeException
    {
	return (RealType)tupleType.getComponent(humidityIndex);
    }


    /**
     * Gets the type of the wind range component.
     *
     * @param tupleType		The field range to examine.
     * @param USpdIndex		The index of the U-component or 
     *				the speed-compnent in <code>field </code>.
     * @param USpdIndex		The index of the V-component or 
     *				the direction-compnent in <code>field </code>.
     * @throws TypeException	The components are incompatible with the
     *				expected type.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static RealTupleType
    getWindType(TupleType tupleType, int USpdIndex, int VDirIndex)
	throws VisADException, TypeException
    {
	return new RealTupleType(
	    (RealType)tupleType.getComponent(USpdIndex),
	    (RealType)tupleType.getComponent(VDirIndex));
    }


    /**
     * Get the units of the range components of a FlatField whose range
     * consists of the specified range components extracted from another
     * FlatField.
     *
     * @param field			The FlatField containing the range
     *					components to be extracted.  Its domain
     *					must be compatible with <code>domainType
     *					</code>.
     * @param temperatureIndex		The index of the temperature
     *					range-component or <code>-1</code>.
     * @param humidityIndex		The index of the dew-point range-
     *					component or <code>-1</code>.
     * @param USpdIndex			The index of the U-component of the
     *					wind, the speed of the wind, or
     *					<code>-1</code>.
     * @param VDirIndex			The index of the V-component of the
     *					wind, the direction of the wind, or
     *					<code>-1</code>.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null || domainType == 
     *					null || indexes == null ||
     *					rangeTypes == null || indexes.length !=
     *					rangeTypes.length </code>.
     */
    protected static Unit[]
    getRangeUnits(FlatField field, int temperatureIndex, int humidityIndex,
	    int USpdIndex, int VDirIndex)
	throws TypeException, VisADException, RemoteException
    {
	if (field == null)
	    throw new IllegalArgumentException("Null field");

	int	count =
	    (isValidIndex(temperatureIndex) ? 1 : 0) +
	    (isValidIndex(humidityIndex) ? 1 : 0) +
	    (areValidIndexes(USpdIndex, VDirIndex) ? 2 : 0);

	Unit[]	units = new Unit[count];
	Unit[]	fieldRangeUnits = field.getDefaultRangeUnits();

	int	i = 0;				// flattened, new-range index
	if (isValidIndex(temperatureIndex))
	    units[i++] =
		getTemperatureUnit(fieldRangeUnits, temperatureIndex);
	if (isValidIndex(humidityIndex))
	    units[i++] = getDewPointUnit(
		fieldRangeUnits, humidityIndex, temperatureIndex);
	if (areValidIndexes(USpdIndex, VDirIndex))
	{
	    Unit[]	windUnits =
		getWindUnits(fieldRangeUnits, USpdIndex, VDirIndex);
	    units[i++] = windUnits[0];		// U
	    units[i++] = windUnits[0];		// USpdIndex in any case
	}

	return units;
    }


    /**
     * Gets the unit of the temperature range component.
     *
     * @param rangeUnits	The units of the flattened field range.
     * @param index		The index of the temperature component in
     *				<code>rangeUnits</code>.
     */
    protected static Unit
    getTemperatureUnit(Unit[] rangeUnits, int index)
    {
	return rangeUnits[index];
    }


    /**
     * Gets the unit of the dew-point range component.
     *
     * @param rangeUnits	The units of the flattened field range.
     * @param index		The index of the dew-point component in
     *				<code>rangeUnits</code>.
     */
    protected static Unit
    getDewPointUnit(Unit[] rangeUnits, int humidityIndex, int temperatureIndex)
    {
	return rangeUnits[humidityIndex];
    }


    /**
     * Gets the units of the wind range component.
     *
     * @param rangeUnits	The units of the flattened field range.
     * @param USpdIndex		The index of the U-component of the
     *				wind or the speed of the wind.
     * @param VDirIndex		The index of the V-component of the
     *				wind or the direction of the wind.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static Unit[]
    getWindUnits(Unit[] rangeUnits, int USpdIndex, int VDirIndex)
	throws VisADException
    {
	Unit	USpdUnit = rangeUnits[USpdIndex];
	Unit	VDirUnit = rangeUnits[VDirIndex];

	return new Unit[] {USpdUnit, VDirUnit};
    }


    /**
     * Gets the temperature values from a FlatField.
     *
     * @param field		The field to extract the values from.
     * @param index		The index of the temperature component in
     *				<code>field </code>.
     * @return			The temperature values from the <code>index
     *				</code> component of the range of <code>field
     *				</code> in that component's default units.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected double[]
    getTemperatureValues(FlatField field, int index)
	throws VisADException, RemoteException
    {
	return field.extract(index).getValues()[0];
    }


    /**
     * Gets the dew-point values from a FlatField.
     *
     * @param field		The field to extract the values from.
     * @param humidityIndex	The index of the dew-point component in
     *				<code>field </code>.
     * @param temperatureIndex	The index of the temperature component in
     *				<code>field </code>.
     * @return			The dew-point values from the <code>index
     *				</code> component of the range of <code>field
     *				</code> in that component's default units.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected double[]
    getDewPointValues(FlatField field, int humidityIndex, int temperatureIndex)
	throws VisADException, RemoteException
    {
	return field.extract(humidityIndex).getValues()[0];
    }


    /**
     * Gets the wind values from a FlatField.
     *
     * @param field		The field to extract the values from.
     * @param USpdIndex		The index of the U-component of the
     *				wind or the speed of the wind.
     * @param VDirIndex		The index of the V-component of the
     *				wind or the direction of the wind.
     * @return			The U/V wind values from the <code>USpdIndex
     *				</code> and <code>VDirIndex</code> components
     *				of the range of <code>field</code>.  If the
     *				wind in the range is U/V, then the units are
     *				the default units of the components; otherwise,
     *				the units are the default unit of the speed
     *				component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected double[][]
    getWindValues(FlatField field, int USpdIndex, int VDirIndex)
	throws RemoteException, VisADException
    {
	double[][]	wind = new double[][] {
	    field.extract(USpdIndex).getValues()[0],
	    field.extract(VDirIndex).getValues()[0]};

	if (convertWind)
	{
	    double[]	dirs = wind[1];
	    Unit	dirUnit = getWindUnits(
		field.getDefaultRangeUnits(), USpdIndex, VDirIndex)[1];
	    dirs = SI.radian.toThis(dirs, dirUnit);
	    double[]	us = wind[0];
	    double[]	vs = wind[1];

	    for (int i = 0; i < dirs.length; ++i)
	    {
		/*
		 * In the following code
		 *	1.  Negating the speed converts the direction from 
		 *	    upwind to downwind; and
		 * 	2.  Transposing sin() and cos() converts the direction
		 *	    from meteorological to mathematical.
		 */
		double	dir = dirs[i];
		double	speed = -us[i];
		us[i] = speed*Math.sin(dir);
		vs[i] = speed*Math.cos(dir);
	    }
	}

	return wind;
    }


    /**
     * Constructs from individual soundings.
     *
     * @param soundings		The individual soundings.  They must all
     *				have the same domain set.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SoundingImpl(SingleSounding[] soundings)
	throws VisADException, RemoteException
    {
	super(
	    getRangeType(soundings),
	    soundings[0].getDomainSet(),
	    getDefaultUnits(soundings));

	Set	domainSet = getDomainSet();

	for (int i = 1; i < soundings.length; ++i)
	    if (!domainSet.equals(soundings[i].getDomainSet()))
		throw new VisADException("Unequal domain sets");

	double[][]	values = new double[getRangeDimension()][];
	int		i = 0;		// flattened-range component-index

	for (int j = 0; j < soundings.length; ++j)	// component index
	{
	    double[][] componentValues = soundings[j].getValues();

	    for (int k = 0; k < componentValues.length; ++k)
		values[i++] = componentValues[k];
	}

	setSamples(values, /*copy=*/false);
    }


    /**
     * Gets the type of the range comprising the given individual soundings.
     *
     * @param soundings		The individual soundings.
     * @return			The type of the range comprising <code>
     *				soundings</code>.  In the most general case,
     *				this will be a Tuple.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static MathType
    getRangeType(SingleSounding[] soundings)
	throws VisADException
    {
	MathType	rangeType;

	if (soundings.length == 1)
	{
	    rangeType = soundings[0].getRangeType();
	}
	else
	{
	    MathType[]	rangeTypes = new MathType[soundings.length];

	    for (int i = 0; i < soundings.length; ++i)
		rangeTypes[i] = soundings[i].getRangeType();

	    rangeType = new TupleType(rangeTypes);
	}

	return rangeType;
    }


    /**
     * Gets the default units of the range components of an array of single-
     * parameter soundings.
     *
     * @param soundings		Single-parameter soundings.
     * @return			Array of default units for the range components
     *				of <code>soundings</code>.
     */
    protected static Unit[]
    getDefaultUnits(SingleSounding[] soundings)
    {
	int	count = 0;

	for (int i = 0; i < soundings.length; ++i)
	    count += soundings[i].getRangeDimension();

	Unit[]	defaultUnits = new Unit[count];
	int	k = 0;			// flattened-range component-index

	for (int i = 0; i < soundings.length; ++i)
	{
	    Unit[]	units = soundings[i].getDefaultRangeUnits();
	    System.arraycopy(units, 0, defaultUnits, k, units.length);
	    k += units.length;
	}

	return defaultUnits;
    }


    /**
     * Gets the simplest form of a MathType.
     *
     * @param mathType		A MathType.
     * @return			The simplest form of <code>mathType</code>.
     *				May return <code>mathType</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static MathType
    getSimplestForm(MathType mathType)
	throws VisADException
    {
	MathType	simplestForm;

	if (mathType instanceof TupleType && 
	    ((TupleType)mathType).getDimension() == 1)
	{
	    simplestForm = 
		getSimplestForm(((TupleType)mathType).getComponent(0));
	}
	else
	{
	    simplestForm = mathType;
	}

	return simplestForm;
    }


    /**
     * Gets the simplest form of an array of MathType-s.
     *
     * @param mathTypes		A array of MathTypes.
     * @return			The MathType corresponding to the simplest form
     *				of <code>mathTypes</code>
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    protected static MathType
    getSimplestForm(MathType[] mathTypes)
	throws VisADException
    {
	MathType	simplestForm;

	if (mathTypes.length == 1)
	{
	    simplestForm = getSimplestForm(mathTypes[0]);
	}
	else
	{
	    boolean	allReals = true;
	    for (int i = 0; allReals && i < mathTypes.length; ++i)
		if (!(getSimplestForm(mathTypes[i]) instanceof RealType))
		    allReals = false;

	    MathType[]	types = allReals
		? new RealType[mathTypes.length]
		: new MathType[mathTypes.length];

	    for (int i = 0; i < types.length; ++i)
		types[i] = getSimplestForm(mathTypes[i]);

	    simplestForm = allReals
		? (TupleType)new RealTupleType((RealType[])types)
		: new TupleType(types);
	}

	return simplestForm;
    }


    /**
     * Converts a MathType to a TupleType.
     *
     * @param mathType		A MathType.
     * @return			The TupleType corresponding to <code>mathType
     *				</code>.  May return <code>mathType</code>.
     * @throws IllegalArgumentException
     *				<code>mathType</code> can't be converted to a
     *				TupleType.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static TupleType
    makeTupleType(MathType mathType)
	throws VisADException
    {
	TupleType	tupleType;

	if (mathType instanceof RealType)
	{
	    tupleType = new RealTupleType((RealType)mathType);
	}
	else
	if (mathType instanceof ScalarType)
	{
	    tupleType = new TupleType(new MathType[] {(ScalarType)mathType});
	}
	else
	if (mathType instanceof TupleType)
	{
	    tupleType = (TupleType)mathType;
	}
	else
	{
	    throw new IllegalArgumentException(
		"Can't convert mathType to TupleType");
	}

	return tupleType;
    }


    /**
     * Counts the number of components in a MathType.
     *
     * @param mathType		A MathType.
     * @return			The number of components in <code>mathType
     *				</code>.
     * @throws IllegalArgumentException
     *				Can't count components in <code>mathType</code>.
     */
    protected static int
    getComponentCount(MathType mathType)
    {
	int	count;

	if (mathType instanceof ScalarType)
	{
	    count = 1;
	}
	else
	if (mathType instanceof TupleType)
	{
	    count = ((TupleType)mathType).getDimension();
	}
	else
	{
	    throw new IllegalArgumentException(
		"Can't count components in mathType");
	}

	return count;
    }


    /**
     * Gets the default units of a MathType.
     *
     * @param mathType		A MathType.
     * @return			The default units of <code>mathType</code>.
     * @throws IllegalArgumentException
     *				Can't get default units of <code>mathType
     *				</code>.
     */
    protected static Unit[]
    getDefaultUnits(MathType mathType)
    {
	Unit[]	units;

	if (mathType instanceof RealType)
	{
	    units = new Unit[] {((RealType)mathType).getDefaultUnit()};
	}
	else
	if (mathType instanceof RealTupleType)
	{
	    units = ((RealTupleType)mathType).getDefaultUnits();
	}
	else
	{
	    throw new IllegalArgumentException(
		"Can't get default units of mathType");
	}

	return units;
    }


    /**
     * Gets the single sounding of the given component.
     *
     * @param componentType	The type of the component.
     * @return			The single sounding of the <code>componentType
     *				</code> component or <code>null</code> if no
     *				such component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public SingleSounding
    getSingleSounding(MathType componentType)
	throws RemoteException, VisADException
    {
	int	index = findComponent(componentType);
	return index == -1
		? null
		: new SingleSoundingImpl(componentType, this, index);
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			The temperature sounding or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public TemperatureSounding
    getTemperatureSounding()
	throws VisADException, RemoteException
    {
	int	index = findComponent(TEMPERATURE_TYPE);
	return index == -1
		? null
		: new TemperatureSoundingImpl(this, index);
    }


    /**
     * Gets the dew-point sounding.
     *
     * @return			The dew-point sounding or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public DewPointSounding
    getDewPointSounding()
	throws VisADException, RemoteException
    {
	int	index = findComponent(DEW_POINT_TYPE);
	return index == -1
		? null
		: new DewPointSoundingImpl(this, index);
    }


    /**
     * Gets the wind profile.
     *
     * @return			The wind profile or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public WindProfile
    getWindProfile()
	throws VisADException, RemoteException
    {
	int	index = findComponent(UV_WIND_TYPE);
	return index == -1
		? null
		: new WindProfileImpl(this, index);
    }


    protected static boolean
    isValidIndex(int index)
    {
	return index != -1;
    }


    protected static boolean
    areValidIndexes(int index1, int index2)
    {
	return isValidIndex(index1) && isValidIndex(index2);
    }


    protected static TupleType
    getRangeTupleType(FlatField field)
	throws VisADException
    {
	return makeTupleType(((FunctionType)field.getType()).getRange());
    }
}
