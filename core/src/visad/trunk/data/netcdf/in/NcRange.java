/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcRange.java,v 1.1 1998-09-11 15:00:54 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Vector;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Provides support for the range of a VisAD field.
 *
 * An instance of this class is mutable.
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
     */
    public void
    add(NcVar var)
	throws VisADException
    {
	vars.add(var);
	computeMathType();
    }


    /**
     * Adds the components of another range to this range.
     *
     * @param range		The other range to be added to this one.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public void
    add(NcRange range)
	throws VisADException
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
     * @param index	The index of the component.
     * @precondition	<code>index >= 0 && index < size()</code>
     * @return		The <code>index</code>th component.
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
     */
    protected void
    computeMathType()
	throws VisADException
    {
	MathType[]	types = new MathType[size()];

	for (int i = 0; i < types.length; ++i)
	    types[i] = getMathType(get(i));

	mathType = NcTuple.newMathType(types);
    }


    /**
     * Gets the VisAD MathType of the given netCDF variable.
     */
    protected abstract MathType
    getMathType(NcVar var)
	throws VisADException;


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
