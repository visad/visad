/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcTuple.java,v 1.3 1998-03-12 22:03:11 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


/**
 * The NcTuple class adapts a tuple of netCDF data objects to a VisAD Tuple.
 */
class
NcTuple
    extends	NcData
{
    /**
     * The netCDF data objects.
     */
    protected final NcData[]	ncDatas;


    /**
     * Construct from an array of netCDF data objects.
     *
     * @param ncDatas	The netCDF data objects constituting the tuple.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcTuple(NcData[] ncDatas)
	throws VisADException
    {
	this.ncDatas = ncDatas;

	int		numComponents = ncDatas.length;
	MathType[]	mathTypes = new MathType[numComponents];

	for (int i = 0; i < numComponents; ++i)
	    mathTypes[i] = ncDatas[i].getMathType();

	initialize(new TupleType(mathTypes));
    }


    /**
     * Return the VisAD data object corresponding to this netCDF data object.
     *
     * @return		The VisAD data object corresponding to the netCDF
     *			data object.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    DataImpl
    getData()
	throws VisADException, IOException
    {
	DataImpl[]	datas = new DataImpl[ncDatas.length];

	for (int i = 0; i < ncDatas.length; ++i)
	    datas[i] = ncDatas[i].getData();

	return new Tuple(datas);
    }
}
