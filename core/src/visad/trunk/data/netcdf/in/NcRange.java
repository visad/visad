/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcRange.java,v 1.3 1998-09-15 21:55:32 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Vector;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Provides support for the logical range of a VisAD Field that is contained
 * in an adapted data object.
 *
 * Instances are mutable.
 */
public abstract class
NcRange
{
    /**
     * The array of adapted, netCDF variables that constitute the range.
     */
    protected final Vector	vars = new Vector(1);


    /**
     * The VisAD MathType of the range.
     */
    private MathType		mathType;


    /**
     * Adds an adapted, netCDF variable to the range.
     *
     * @param			The adapted, netCDF variable to be added.
     * @postcondition		<code>size() == </code>(PRE)<code>size()
     *				+ 1</code>
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public void
    add(NcVar var)
	throws VisADException, IOException
    {
	vars.add(var);
	computeMathType();
    }


    /**
     * Adds the components of another range to this range.
     *
     * @param range		The other range to be added to this one.
     * @postcondition		<code>size() == </code>(PRE)<code>size() +
     *				range.size()</code>
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public void
    add(NcRange range)
	throws VisADException, IOException
    {
	vars.addAll(range.vars);
	computeMathType();
    }


    /**
     * Gets the number of components in the range.
     *
     * @return		The number of components.
     */
    public int
    size()
    {
	return vars.size();
    }


    /**
     * Gets a component of the range.
     *
     * @param index		The index of the component.
     * @precondition		<code>index >= 0 && index < size()</code>
     * @return			The <code>index</code>th component.
     * @throws VisADException	<code>index> is out-of-bounds.
     */
    protected NcVar
    get(int index)
	throws VisADException
    {
	if (index < 0 || index >= size())
	    throw new VisADException("Index out of bounds");

	return (NcVar)vars.get(index);
    }


    /**
     * Computes and sets the VisAD MathType of the range.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    protected void
    computeMathType()
	throws VisADException, IOException
    {
	MathType[]	types = new MathType[size()];

	for (int i = 0; i < types.length; ++i)
	    types[i] = getMathType(get(i));

	mathType = NcTuple.newMathType(types);
    }


    /**
     * Gets the VisAD MathType of the given netCDF variable.
     *
     * @param var		The adapted, netCDF variable to be examined.
     * @return			The VisAD MathType of <code>var</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    protected abstract MathType
    getMathType(NcVar var)
	throws VisADException, IOException;


    /**
     * Gets the VisAD MathType of this range.
     *
     * @return	the VisAD MathType of this range.
     */
    public MathType
    getMathType()
    {
	return mathType;
    }
}
