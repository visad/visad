/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingImpl.java,v 1.2 1998-11-03 22:27:36 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
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
 * Instances are mutable.
 *
 * @author Steven R. Emmerson
 */
public class
SoundingImpl
    extends	AbstractSounding
{
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
	    new TupleType(
		new MathType[] {
		    TEMPERATURE_TYPE,
		    DEW_POINT_TYPE,
		    WIND_TYPE}),
	    new Integer1DSet(domainType, 2),
	    new Unit[] {
		TEMPERATURE_TYPE.getDefaultUnit(),
		DEW_POINT_TYPE.getDefaultUnit(),
		WIND_TYPE.getDefaultUnits()[0],
		WIND_TYPE.getDefaultUnits()[1]});
    }


    /**
     * Constructs from a FlatField and component indexes.
     *
     * @param field			A sounding FlatField.  Domain must
     *					be 1-D pressure.
     * @param temperatureIndex		The index of the temperature
     *					range-component or <code>-1</code>.
     * @param dewPointIndex		The index of the dew-point range-
     *					component or <code>-1</code>.
     * @param uIndex			The index of the U-component of the
     *					wind or <code>-1</code>.
     * @param vIndex			The index of the V-component of the
     *					wind or <code>-1</code>.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null</code>.
     */
    public
    SoundingImpl(FlatField field, int temperatureIndex, int dewPointIndex,
	    int uIndex, int vIndex)
	throws	VisADException, RemoteException
    {
	super(
	    getRangeType(
		field,
		new int[][] {
		    new int[] {temperatureIndex},
		    new int[] {dewPointIndex},
		    new int[] {uIndex, vIndex}},
		new MathType[] {
		    TEMPERATURE_TYPE,
		    DEW_POINT_TYPE,
		    WIND_TYPE}),
	    field.getDomainSet(),
	    getRangeUnits(
		field,
		new int[] {temperatureIndex, dewPointIndex, uIndex, vIndex}));

	int[]		indexes =
	    new int[] {temperatureIndex, dewPointIndex, uIndex, vIndex};
	double[][]	values = new double[getRangeDimension()][];

	int	count = 0;
	for (int i = 0; i < indexes.length; ++i)
	{
	    int	index = indexes[i];
	    if (index != -1)
	    {
		values[count] = field.extract(index).getValues()[0];
		count++;
	    }
	}

	setSamples(values, /*copy=*/false);
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
     * Gets the type of the FlatField range formed from the
     * specified range components of another FlatField.
     *
     * @param field			The FlatField containing the range
     *					components to be extracted.
     * @param indexes			Indexes of the range components in
     *					<code>field</code> to extract.  A
     *					value of <code>-1</code> is ignored.
     * @param rangeTypes		The types for the extracted range
     *					components.  <code>rangeTypes[i]</code>
     *					must be compatible with the <code>
     *					indexes[i]</code>-th range components of
     *					<code>field</code> if <code>
     *					indexes[i][0] != -1</code>.
     * @throws TypeException		A range-component is incompatible.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null ||
     *					indexes == null ||
     *					rangeTypes == null || indexes.length !=
     *					rangeTypes.length </code>.
     */
    protected static MathType
    getRangeType(FlatField field, int[][] indexes, MathType[] rangeTypes)
	throws TypeException, VisADException, RemoteException
    {
	if (field == null || indexes == null || rangeTypes == null)
	    throw new IllegalArgumentException("Null argument");

	if (indexes.length != rangeTypes.length)
	    throw new IllegalArgumentException("Array length mismatch");

	FunctionType	fieldType = (FunctionType)field.getType();

	int	newRangeComponentCount = 0;
	for (int i = 0; i < indexes.length; ++i)
	    if (indexes[i][0] != -1)
		newRangeComponentCount++;

	TupleType	fieldRangeTuple = makeTupleType(fieldType.getRange());

	MathType[]	newRangeTypes = new MathType[newRangeComponentCount];
	for (int i = 0; i < indexes.length; ++i)
	{
	    int[]	ints = indexes[i];
	    if (ints[0] != -1)
		newRangeTypes[i] = getComponentType(field, ints, rangeTypes[i]);
	}

	return getSimplestForm(newRangeTypes);
    }


    /**
     * Gets the type of the given components of a given field.
     *
     * @param field		The field to examine.
     * @param indexes		The indexes of the components in <code>field
     *				</code>.
     * @param expectedType	The expected type.
     * @throws IllegalArgumentException
     *				<code>field == null || indexes == null ||
     *				expectedType == null</code>.
     * @throws TypeException	The components are incompatible with the
     *				expected type.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static MathType
    getComponentType(FlatField field, int[] indexes, MathType expectedType)
	throws VisADException, TypeException
    {
	if (field == null || indexes == null || expectedType == null)
	    throw new IllegalArgumentException("Null argument");

	TupleType	rangeTupleType =
	    makeTupleType(((FunctionType)field.getType()).getRange());
	RealType[]	componentTypes = new RealType[indexes.length];

	for (int i = 0; i < indexes.length; ++i)
	    componentTypes[i] = (RealType)rangeTupleType.getComponent(i);

	MathType	newComponentType =
	    getSimplestForm(new RealTupleType(componentTypes));

	if (!expectedType.equalsExceptNameButUnits(newComponentType))
	    throw new TypeException("Unexpected incompatible component");

	return expectedType;
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
     * Get the units of the range components of a FlatField whose range
     * consists of the specified range components extracted from another
     * FlatField.
     *
     * @param field			The FlatField containing the range
     *					components to be extracted.  Its domain
     *					must be compatible with <code>domainType
     *					</code>.
     * @param indexes			Indexes of the range components in
     *					<code>field</code> to extract.  A
     *					value of <code>-1</code> is ignored.
     * @throws VisADException		Couldn't create necessary VisAD object.
     * @throws RemoteException		Remote data access failure.
     * @throws IllegalArgumentException	<code>field == null || domainType == 
     *					null || indexes == null ||
     *					rangeTypes == null || indexes.length !=
     *					rangeTypes.length </code>.
     */
    protected static Unit[]
    getRangeUnits(FlatField field, int[] indexes)
	throws TypeException, VisADException, RemoteException
    {
	if (field == null || indexes == null)
	    throw new IllegalArgumentException("Null argument");

	TupleType	fieldRangeTuple =
	    makeTupleType(((FunctionType)field.getType()).getRange());

	int	count = 0;
	for (int i = 0; i < indexes.length; ++i)
	{
	    int	index = indexes[i];
	    if (index != -1)
		count += getComponentCount(fieldRangeTuple.getComponent(index));
	}

	Unit[]	units = new Unit[count];
	int	k = 0;			// flattened-range component index
	for (int i = 0; i < indexes.length; ++i)
	{
	    int	index = indexes[i];
	    if (index != -1)
	    {
		Unit[]	defaultUnits = 
		    getDefaultUnits(fieldRangeTuple.getComponent(indexes[i]));
		System.arraycopy(
		    defaultUnits, 0, units, k, defaultUnits.length);
		k += defaultUnits.length;
	    }
	}

	return units;
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
	int	index = findComponent(WIND_TYPE);
	return index == -1
		? null
		: new WindProfileImpl(this, index);
    }
}
