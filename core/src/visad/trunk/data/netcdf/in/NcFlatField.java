/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcFlatField.java,v 1.2 1998-09-11 16:33:49 steve Exp $
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
     * @throws IOException	I/O failure.
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
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public NcData
    tryCombine(NcFlatField flatField)
	throws VisADException
    {
	clearWasCombined();

	if (getDomain().equals(flatField.getDomain()))
	{
	    range.add(flatField.range);
	    setWasCombined();
	}

	return this;
    }


    /**
     * Adds a nested field to this data object.
     *
     * @param field		The nested field to be added to this one.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	I/O failure.
     */
    public NcData
    tryCombine(NcNestedField field)
	throws VisADException, IOException
    {
	/*
	 * It is easier to add a flat field to a nested field than vice versa.
	 */
	return field.tryCombine(this);
    }


    /**
     * Gets the range of this field.
     *
     * @return			The range of this field.
     */
    public NcRange
    getRange()
    {
	return range;
    }


    /**
     * Gets the VisAD MathType of the range of this field.
     *
     * @return			The VisAD MathType of the range of this field.
     */
    protected MathType
    getRangeMathType()
	throws VisADException
    {
	return range.getMathType();
    }


    /**
     * Gets the VisAD FlatField corresponding to this data object.
     *
     * @return			The VisAD FlatField corresponding to this
     *				data object.
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
     * Gets a proxy for the VisAD FlatField corresponding to this data object.
     *
     * @return			The VisAD FileFlatField corresponding to this
     *				data object.
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
     *
     * @return			The values of this data object as an array 
     *				of doubles.
     */
    public double[][]
    getDoubles()
	throws VisADException
    {
	throw new VisADException("Method not applicable");
    }


    /**
     * Provides FileAccessor support for this data object.
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
