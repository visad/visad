/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNestedField.java,v 1.3 1998-09-14 13:51:37 billh Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.Set;
import visad.VisADException;
import visad.data.FileAccessor;


/**
 * Adapts netCDF variables to a "nested" VisAD Field.  A nested Field 
 * comprises a mapping from a one-dimensional domain to, in general, a non-flat
 * range (e.g. t -> (x,y) -> v).
 *
 * Instances are mutable.
 */
public class
NcNestedField
    extends	NcField
{
    /**
     * The range of this field.
     */
    private final NcNestedRange	range;


    /**
     * Constructs from an adapted netCDF variable.
     *
     * @param var		The netCDF variable that defines the Field.
     * @precondition		<code>var.getRank() >= 1</code>
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    protected
    NcNestedField(NcVar var)
	throws VisADException, IOException
    {
      /* WLH 13 Sept 98 */
      super(new NcDomain(var, var.getDimension(0)));

/* WLH 13 Sept 98
        super(new NcDomain(var.getDimensions()));
*/

	if (var.getRank() == 0)
	    throw new VisADException("Variable is scalar");

	range = new NcNestedRange(var);
    }


    /**
     * Constructs from an adapted FlatField.
     *
     * @param field		The adapted FlatField.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    protected
    NcNestedField(NcFlatField field)
	throws VisADException, IOException
    {
	super(field.getDomain().getOuterDomain());
	range = new NcNestedRange(field.getRange());
    }


    /**
     * Adds an adapted FlatField to this adapted, nested Field.
     *
     * @param flatField		The adapted FlatField to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public NcData
    tryCombine(NcFlatField flatField)
	throws VisADException, IOException
    {
	return tryCombine(new NcNestedField(flatField));
    }


    /**
     * Adds an adapted, nested Field to this adapted, nested Field.
     *
     * @param field		The adapted, nested Field to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throw VisADException	Couldn't create necessary VisAD object.
     */
    public NcData
    tryCombine(NcNestedField field)
	throws VisADException
    {
	clearWasCombined();

	if (getDomain().equals(field.getDomain()))
	{
	    range.add(field.range);
	    setWasCombined();
	}

	return this;
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
     * Gets the VisAD Field corresponding to this data object.
     *
     * @return			The VisAD Field corresponding to this
     *				data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws VisADException, IOException
    {
	FunctionType	funcType = (FunctionType)getMathType();
	Set		domainSet = getDomain().getSet();
	FieldImpl	field = new FieldImpl(funcType, domainSet);
	int		n = domainSet.getLength();

	for (int i = 0; i < n; ++i)
	    field.setSample(i, range.getData(i), /*copy=*/false);

	return field;
    }


    /**
     * Gets a proxy for the VisAD Field corresponding to this data object.
     *
     * @return			The VisAD FileFlatField corresponding
     *				to this data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getProxy()
	throws VisADException, IOException
    {
	FunctionType	funcType = (FunctionType)getMathType();
	Set		domainSet = getDomain().getSet();
	FieldImpl	field = new FieldImpl(funcType, domainSet);
	int		n = domainSet.getLength();

	for (int i = 0; i < n; ++i)
	    field.setSample(i, range.getProxy(i), /*copy=*/false);

	return field;
    }


    /**
     * Gets the values of this data object as an array of doubles.
     *
     * @return			The values of the netCDF variables in this 
     *				range.  The outermost dimension is the
     *				component dimension.
     * @throws VisADException	Couldn't create necessary VisAD object.
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
	/**
	 * The range index of the FlatField.
	 */
	protected final int	rangeIndex;


	/**
	 * Construct from a range index.
	 */
	Accessor(int rangeIndex)
	{
	    this.rangeIndex = rangeIndex;
	}


	/*
	 * Write data into the backing file.  Not supported.
	 *
	 * @throws UnsupportedOperationException	Always thrown.
	 */
	public void
	writeFile(int[] fileLocations, Data range)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	/*
	 * Read data from the backing file.  Not supported.
	 *
	 * @throws UnsupportedOperationException	Always thrown.
	 */
	public double[][]
	readFlatField(FlatField template, int[] fileLocation)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	/*
	 * Write data to the backing file.  Not supported.
	 *
	 * @throws UnsupportedOperationException	Always thrown.
	 */
	public void
	writeFlatField(double[][] values, FlatField template, 
			int[] fileLocation)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	/*
	 * Return the FlatField.
	 *
	 * @throws VisADException	Couldn't create necessary VisAD object.
	 */
	public FlatField
	getFlatField()
	    throws VisADException
	{
	    return null;	// STUB
	}

	public FunctionType
	getFunctionType()
	{
	    return null;	// STUB
	}
    }
}
