/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNestedRange.java,v 1.3 1998-09-15 21:55:30 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Provides support for the logical range of a VisAD Field that is 
 * contained in a "nested", adapted data object.
 *
 * @see NcNestedField
 *
 * Instances are mutable.
 */
public class
NcNestedRange
    extends	NcRange
    implements	NestedData
{
    /**
     * Constructs from an adapted, netCDF variable.
     *
     * @param var		The adapted, netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    public
    NcNestedRange(NcVar var)
	throws VisADException, IOException
    {
	add(var);
    }


    /**
     * Constructs from another range.
     *
     * @param range		The other range.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public
    NcNestedRange(NcRange range)
	throws VisADException, IOException
    {
	add(range);
    }


    /**
     * Gets the VisAD MathType of the given netCDF variable.
     *
     * @param var		The adapted, netCDF variable to be examined.
     * @return			The VisAD MathType of <code>var</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    protected MathType
    getMathType(NcVar var)
	throws VisADException, IOException
    {
	return ((NcNumber)var).getInnerMathType();
    }


    /**
     * Gets the VisAD MathType of the "inner" portion of this range.
     * In general, this will be a VisAD FunctionType.
     *
     * @return			The VisAD MathType of this range.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public MathType
    getInnerMathType()
	throws VisADException
    {
	return getMathType();
    }


    /**
     * Gets the value of the range at a point in the outermost dimension.
     *
     * @param index		The point in the outermost dimension.
     * @precondition		<code>size() > 0</code>
     * @precondition		<code>index >= 0</code> &&
     *				index < 
     *				get(0).getDimension(0).getLength()</code>
     * @return			The value of the range at <code>index</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    public DataImpl
    getData(int index)
	throws IOException, VisADException
    {
	if (size() == 0)
	    throw new VisADException("Empty range");

	if (index < 0 || index >= get(0).getDimension(0).getLength())
	    throw new VisADException("Index out of bounds");

	DataImpl[]	datas = new DataImpl[size()];

	for (int i = 0; i < datas.length; ++i)
	    // TODO: support text
	    datas[i] = ((NcNumber)get(i)).getData(index);

	return NcTuple.newData(datas);
    }


    /**
     * Gets a proxy for value of the range at a point in the outermost
     * dimension.
     *
     * @param index		The point in the outermost dimension.
     * @precondition		<code>size() > 0</code>
     * @precondition		<code>index >= 0</code>
     * @precondition		<code>index <
     *				get(0).getDimension(0).getLength()</code>
     * @return			A proxy for the value of the
     *				range at <code>index</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    public DataImpl
    getProxy(int index)
	throws VisADException, IOException
    {
	if (size() == 0)
	    throw new VisADException("Empty range");

	if (index < 0 || index >= get(0).getDimension(0).getLength())
	    throw new VisADException("Index out of bounds");

	DataImpl[]	datas = new DataImpl[size()];

	for (int i = 0; i < datas.length; ++i)
	    // TODO: support text
	    datas[i] = ((NcNumber)get(i)).getProxy(index);

	return NcTuple.newData(datas);
    }
}
