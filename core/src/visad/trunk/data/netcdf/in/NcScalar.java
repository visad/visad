/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcScalar.java,v 1.4 1998-04-02 20:49:46 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.DataImpl;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Scalar;
import visad.ScalarType;
import visad.TextType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


/**
 * The NcScalar class adapts scalars in a netCDF dataset to a VisAD API.  
 * A scalar can be a single datum or a collection of data defined over the
 * rank 0 domain (i.e. a VisAD Tuple).
 */
abstract class
NcScalar
    extends	NcData
{
    /**
     * Factory method for constructing an NcScalar from netCDF variables.
     *
     * @parm vars	The netCDF variables defined over the same, rank-0, 
     *			domain.
     * @precondition	The rank of every variable is zero.
     * @precondition	<code>vars.length >= 1</code>.
     * @return		The NcData corresponding to <code>vars</code>.
     */
    static NcData
    newNcScalar(NcVar[] vars)
	throws VisADException, IOException
    {
	return vars.length == 1
		    ? (NcData) new NcScalarVar(vars[0])
		    : (NcData) new NcScalarVars(vars);
    }


    /**
     * Construct from a given VisAD MathType.
     */
    protected
    NcScalar(MathType type)
    {
	super(type);
    }
}


/**
 * The NcScalarVar class adapts a scalar netCDF variable to a VisAD Scalar.
 */
class
NcScalarVar
    extends	NcScalar
{
    /**
     * The netCDF variable.
     */
    protected final NcVar	var;


    /**
     * Construct from an adapted netCDF variable.
     *
     * @parm var	The netCDF variable.
     * @precondition	<code>var.getRank() == 0</code>.
     */
    NcScalarVar(NcVar var)
	throws VisADException, IOException
    {
	super(var.getMathType());
	this.var = var;
    }


    /**
     * Return the corresponding VisAD data object.
     *
     * @return	The corresponding VisAD data object.
     */
    DataImpl
    getData()
	throws IOException, VisADException
    {
	return var.getData()[0];
    }


    /**
     * Return a proxy for the corresponding VisAD data object.
     *
     * @return	The corresponding VisAD data object.
     */
    DataImpl
    getProxy()
	throws IOException, VisADException
    {
	return getData();
    }
}


/**
 * The NcScalarVars class adapts scalar netCDF variables to a VisAD Tuple.
 */
class
NcScalarVars
    extends	NcScalar
{
    /**
     * The scalar netCDF variables.
     */
    protected final NcVar[]	vars;


    /**
     * Construct from adapted netCDF variables.
     *
     * @parm vars	The netCDF variables.
     * @precondition	<code>vars[i].getRank() == 0</code> for all 
     *			<code>i</code>.
     */
    NcScalarVars(NcVar[] vars)
	throws VisADException, IOException
    {
	super(getTupleType(vars));
	this.vars = vars;
    }


    /**
     * Return the TupleType of the given, scalar, netCDF variables.
     */
    private static TupleType
    getTupleType(NcVar[] vars)
	throws VisADException
    {
	TupleType	tupleType;
	boolean		allRealTypes = true;

	for (int i = 0; i < vars.length; ++i)
	{
	    if (vars[i].getMathType() instanceof TextType)
	    {
		allRealTypes = false;
		break;
	    }
	}

	if (allRealTypes)
	{
	    RealType[]	types = new RealType[vars.length];

	    for (int i = 0; i < vars.length; ++i)
	    {
		types[i] = (RealType)vars[i].getMathType();
	    }

	    tupleType = new RealTupleType(types);
	}
	else
	{
	    ScalarType[]	types = new ScalarType[vars.length];

	    for (int i = 0; i < vars.length; ++i)
	    {
		types[i] = vars[i].getMathType();
	    }

	    tupleType = new TupleType(types);
	}

	return tupleType;
    }


    /**
     * Return the corresponding VisAD data object.
     *
     * @return	The corresponding VisAD data object.
     */
    DataImpl
    getData()
	throws VisADException, RemoteException, IOException
    {
	Tuple	tuple;

	if (mathType instanceof RealTupleType)
	{
	    Real[]	values = new Real[vars.length];

	    for (int i = 0; i < vars.length; ++i)
		values[i] = (Real)vars[i].getData()[0];

	    tuple = new RealTuple(values);
	}
	else
	{
	    Scalar[]	values = new Scalar[vars.length];

	    for (int i = 0; i < vars.length; ++i)
		values[i] = (Scalar)vars[i].getData()[0];

	    tuple = new Tuple(values);
	}

	return tuple;
    }


    /**
     * Return a proxy for the corresponding VisAD data object.
     *
     * @return	The corresponding VisAD data object.
     */
    DataImpl
    getProxy()
	throws VisADException, RemoteException, IOException
    {
	return getData();
    }
}
