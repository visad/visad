/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcField.java,v 1.3 1998-09-15 21:55:28 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.FunctionType;
import visad.MathType;
import visad.VisADException;


/**
 * Adapts netCDF variables to a VisAD Field.
 *
 * Instances are mutable.
 */
public abstract class
NcField
    extends	NcData
{
    /**
     * The domain of the field.
     */
    private NcDomain	domain;


    /**
     * Constructs from a domain.
     *
     * @param domain		The domain of the Field.
     */
    protected
    NcField(NcDomain domain)
    {
	this.domain = domain;
    }


    /**
     * Adds a data object to this field.
     *
     * @param data		The data object to be added
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    public NcData
    tryCombine(NcData data)
	throws VisADException, IOException
    {
	return data instanceof NcNestedField
		? tryCombine((NcNestedField)data)
		: data instanceof NcFlatField
		    ? tryCombine((NcFlatField)data)
		    : this;
    }


    /**
     * Adds an NcNestedField to this data object.
     *
     * @param field		The NcNestedField to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	I/O failure.
     */
    public abstract NcData
    tryCombine(NcNestedField field)
	throws VisADException, IOException;


    /**
     * Adds an NcFlatField to this data object.
     *
     * @param field		The NcFlatField to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     *				if and only if RETURN_VALUE constains both
     *				this data object and <code>data</code>.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException      Data access I/O failure.
     */
    public abstract NcData
    tryCombine(NcFlatField field)
	throws VisADException, IOException;


    /**
     * Gets the domain of the field.
     *
     * @return		The adapted domain of the field.
     */
    public NcDomain
    getDomain()
    {
	return domain;
    }


    /**
     * Gets the VisAD MathType of this field.  Uses the Template design pattern.
     *
     * @return			The VisAD MathType of this field.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public MathType
    getMathType()
	throws VisADException
    {
	return new FunctionType(domain.getType(), getRangeMathType());
    }


    /**
     * Gets the VisAD MathType of the range of this field.
     *
     * @return			The VisAD MathType of the range of this field.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected abstract MathType
    getRangeMathType()
	throws VisADException;


    /**
     * Gets the range of this field.
     *
     * @return			The range of this field.
     */
    public abstract NcRange
    getRange();


    /**
     * Gets a string representation of this field.
     */
    public String
    toString()
    {
	try
	{
	    return getMathType().toString();
	}
	catch (VisADException e)
	{
	    return e.getMessage();
	}
    }
}
