/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNestedRange.java,v 1.1 1998-09-11 15:00:53 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Supports the range of a "nested" VisAD Field.
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
     */
    public
    NcNestedRange(NcVar var)
	throws VisADException, IOException
    {
	add(var);
    }


    /**
     * Constructs from another range.
     */
    public
    NcNestedRange(NcRange range)
	throws VisADException
    {
	add(range);
    }


    /**
     * Gets the VisAD MathType of the given netCDF variable.
     */
    protected MathType
    getMathType(NcVar var)
	throws VisADException
    {
	return ((NcNumber)var).getInnerMathType();
    }


    /**
     * Gets the VisAD MathType of the "inner" portion of this range.
     * In general, this will be a VisAD FunctionType.
     *
     * @return	the VisAD MathType of this range.
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
     * @param index	The point in the outermost dimension.
     * @precondition	<code>size() > 0</code>
     * @precondition	<code>index >= 0</code>
     * @precondition	<code>index < get(0).getDimension(0).getLength()</code>
     *			in the outermost dimension>
     * @return		The value of the range at <code>index</code>.
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
     * @param index	The point in the outermost dimension.
     * @precondition	<code>size() > 0</code>
     * @precondition	<code>index >= 0</code>
     * @precondition	<code>index < get(0).getDimension(0).getLength()</code>
     *			in the outermost dimension>
     * @return		A proxy for the value of the range at 
     *			<code>index</code>.
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
