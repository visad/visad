/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcFlatField.java,v 1.1 1998-09-11 15:00:52 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.CoordinateSystem;
import visad.Data;
import visad.DataImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.VisADException;
import visad.data.CacheStrategy;
import visad.data.FileAccessor;
import visad.data.FileFlatField;
import visad.data.netcdf.UnsupportedOperationException;


/**
 * Adapts netCDF variables to a VisAD FlatField.
 *
 * Instances are mutable.
 */
class
NcFlatField
    extends	NcField
{
    /**
     * The range of this field.
     */
    private final NcFlatRange	range;


    /**
     * Constructs from an adapted netCDF variable.
     *
     * @param var		The netCDF variable that constitutes the 
     *				FlatField.
     * @precondition		<code>var.getRank() >= 1</code>
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    NcFlatField(NcVar var)
	throws VisADException, IOException
    {
	super(new NcDomain(var.getDimensions()));

	if (var.getRank() == 0)
	    throw new VisADException("Variable is scalar");

	range = new NcFlatRange(var);
    }


    /**
     * Adds an adapted FlatField to this adapted FlatField.
     *
     * @param flatField		The FlatField to be added to this one.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasAdded() == true</code>
     *				<=> <the data object was added>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public NcData
    tryAddData(NcFlatField flatField)
	throws VisADException
    {
	clearWasAdded();

	if (getDomain().equals(flatField.getDomain()))
	{
	    range.add(flatField.range);
	    setWasAdded();
	}

	return this;
    }


    /**
     * Adds a nested field to this data object.
     *
     * @param field		The nested field to be added to this one.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasAdded() == true</code>
     *				<=> <the data object was added>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public NcData
    tryAddData(NcNestedField field)
	throws VisADException, IOException
    {
	/*
	 * It is easier to add a flat field to a nested field than vice versa.
	 */
	return field.tryAddData(this);
    }


    /**
     * Gets the range of this field.
     */
    public NcRange
    getRange()
    {
	return range;
    }


    /**
     * Gets the VisAD MathType of the range of this field.
     */
    protected MathType
    getRangeMathType()
	throws VisADException
    {
	return range.getMathType();
    }


    /**
     * Gets the VisAD data object corresponding to this field.
     *
     * @return			The VisAD data object corresponding to the 
     *				function.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws IOException, VisADException
    {
	FlatField	field = new FlatField(
	    (FunctionType)getMathType(),
	    getDomain().getSet(),
	    (CoordinateSystem)null,
	    range.getSets(),
	    range.getUnits());
	double[][]	values = range.getDoubles();

	field.setSamples(values, /*copy=*/false);

	return field;
    }


    /**
     * Gets a proxy for the VisAD data object corresponding to this function.
     *
     * @return			The VisAD data object corresponding to the 
     *				function.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getProxy()
	throws IOException, VisADException
    {
	return new FileFlatField(new Accessor(), new CacheStrategy());
    }


    /**
     * Gets the values of this data object as an array of doubles.
     */
    public double[][]
    getDoubles()
	throws VisADException
    {
	throw new VisADException("Method not applicable");
    }


    /**
     * FileAccessor for regular, adapted, netCDF functions.
     */
    protected class
    Accessor
	extends	FileAccessor
    {
	public void
	writeFile(int[] fileLocations, Data range)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	public double[][]
	readFlatField(FlatField template, int[] fileLocation)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	public void
	writeFlatField(double[][] values, FlatField template, 
			int[] fileLocation)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	public FlatField
	getFlatField()
	    throws VisADException
	{
	    try
	    {
		return (FlatField)getData();
	    }
	    catch (Exception e)
	    {
		throw new VisADException(e.getMessage());
	    }
	}

	public FunctionType
	getFunctionType()
	    throws VisADException
	{
	    return (FunctionType)getMathType();
	}
    }
}
