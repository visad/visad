/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcData.java,v 1.6 1998-09-11 16:33:48 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Adapting an existing netCDF data object to a VisAD data object.
 */
abstract class
NcData
{
    /**
     * Whether or not the previous <code>tryCombine()</code> succeeded.
     */
    private boolean	wasCombined = false;


    /**
     * Constructs from nothing.  Protected to ensure use by subclasses only.
     */
    protected
    NcData()
    {
    }


    /**
     * Factory method for constructing the proper type of NcData from an
     * adapted, netCDF variable.
     *
     * @param var		The adapted netCDF variable.
     * @return			The NcData object corresponding to the adapted
     *				netCDF variable.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     * @see NcNestedField
     */
    static NcData
    newNcData(NcVar var)
	throws VisADException, IOException
    {
	int	rank = var.getRank();
	NcData	data;

	if (rank == 0)
	    data = (NcData)new NcScalar(var);
	else
	{
	    data = var.getRank() >= 2 && var.getDimension(0).isTime()
		    ? (NcData)new NcNestedField(var)
		    : (NcData)new NcFlatField(var);
	}

	return data;
    }


    /**
     * Trys to add another data object to this one.
     *
     * @param data		The data to be added to this one.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true
     *				</code> if an only if <code>data</code> 
     *				was added to this data object.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException      Data access I/O failure.
     */
    public NcData
    tryCombine(NcData data)
	throws VisADException, IOException
    {
	wasCombined = false;

	if (data instanceof NcField)
	    return tryCombine((NcField)data);

	if (data instanceof NcScalar)
	    return tryCombine((NcScalar)data);

	return this;
    }


    /**
     * Adds an NcField to this data object.  Overridden in appropriate
     * subclasses.
     *
     * @param field		The field to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == false</code>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException      Data access I/O failure.
     */
    public NcData
    tryCombine(NcField field)
	throws VisADException, IOException
    {
	wasCombined = false;
	return this;
    }


    /**
     * Adds an NcScalar to this data object.  Overridden in appropriate
     * subclasses.
     *
     * @param scalar		The NcScalar to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == false</code>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public NcData
    tryCombine(NcScalar Scalar)
	throws VisADException
    {
	wasCombined = false;
	return this;
    }


    /**
     * Set the indicator of whether or not the previous 
     * <code>tryCombine()</code> succeeded.
     *
     * @postcondition	<code>wasCombined() == true</code>
     */
    protected void
    setWasCombined()
    {
	wasCombined = true;
    }


    /**
     * Clear the indicator of whether or not the previous 
     * <code>tryCombine()</code> succeeded.
     *
     * @postcondition	<code>wasCombined() == false</code>
     */
    protected void
    clearWasCombined()
    {
	wasCombined = false;
    }


    /**
     * Gets the indicator of whether or not the previous 
     * <code>tryCombine()</code> succeeded.
     *
     * @return		Whether or not the previous <code>tryCombine()</code>
     *			succeeded (true <=> yes).
     */
    public boolean
    wasCombined()
    {
	return wasCombined;
    }


    /**
     * Gets the VisAD MathType of this data object.
     *
     * @return	The VisAD MathType of the adapted netCDF data object.
     */
    protected abstract MathType
    getMathType()
	throws VisADException;


    /**
     * Gets the VisAD data object corresponding to this netCDF data object.
     *
     * @return			The VisAD data object corresponding to the 
     *				netCDF data object.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public abstract DataImpl
    getData()
	throws VisADException, IOException;


    /**
     * Gets a proxy for the VisAD data object corresponding to this 
     * netCDF data object.
     *
     * @return			The VisAD data object corresponding to the 
     *				netCDF data object.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public abstract DataImpl
    getProxy()
	throws VisADException, IOException;


    /**
     * Gets the values of the data object as an array of doubles.
     *
     * @return			The values of the data object.  
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException      Data access I/O failure.
     */
    public abstract double[][]
    getDoubles()
	throws VisADException, IOException;
}
