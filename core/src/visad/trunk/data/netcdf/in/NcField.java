/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcField.java,v 1.1 1998-09-11 15:00:52 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.FunctionType;
import visad.MathType;
import visad.VisADException;


/**
 * Adapts netCDF variables to a VisAD Field.
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
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected
    NcField(NcDomain domain)
	throws VisADException
    {
	this.domain = domain;
    }


    /**
     * Adds a data object to this field.
     */
    public NcData
    tryAddData(NcData data)
	throws VisADException, IOException
    {
	return data instanceof NcNestedField
		? tryAddData((NcNestedField)data)
		: data instanceof NcFlatField
		    ? tryAddData((NcFlatField)data)
		    : this;
    }


    /**
     * Adds an NcNestedField to this data object.
     *
     * @param field		The NcNestedField to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasAdded() == true</code>
     *				<=> <the data object was added>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public abstract NcData
    tryAddData(NcNestedField field)
	throws VisADException, IOException;


    /**
     * Adds an NcFlatField to this data object.
     *
     * @param field		The NcFlatField to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasAdded() == true</code>
     *				<=> <the data object was added>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException      Data access I/O failure.
     */
    public abstract NcData
    tryAddData(NcFlatField field)
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
     * Gets the VisAD MathType of this field.
     *
     * Template design pattern.
     */
    public MathType
    getMathType()
	throws VisADException
    {
	return new FunctionType(domain.getType(), getRangeMathType());
    }


    /**
     * Gets the VisAD MathType of the range of this field.
     */
    protected abstract MathType
    getRangeMathType()
	throws VisADException;


    /**
     * Gets the range of this field.
     */
    public abstract NcRange
    getRange();
}
