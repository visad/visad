/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DefaultView.java,v 1.5 2000-06-08 19:13:44 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.NoSuchElementException;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.FunctionType;
import visad.MathType;
import visad.SampledSet;
import visad.ScalarType;
import visad.SimpleSet;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.QuantityDB;


/**
 * Provides support for the default view of a netCDF dataset as documented
 * in the netCDF User's Guide.
 */
public class
DefaultView
    extends	View
{
    /**
     * NetCDF dataset-specific utility methods.
     */
    private /*final*/ Util	util;


    /**
     * Constructs from a netCDF dataset and a quantity database.
     *
     * @param netcdf		The netCDF dataset.
     * @param quantityDB	The quantity database to use to map netCDF
     *				variables to VisAD Quantity-s.
     */
    public
    DefaultView(Netcdf netcdf, QuantityDB quantityDB)
    {
	super(netcdf);
	util = Util.newUtil(netcdf, quantityDB);
    }


    /**
     * Gets an iterator over the virtual VisAD data objects determined by
     * this view.
     *
     * @return			An iterator for the virtual VisAD data objects
     *				in the view.
     */
    public VirtualDataIterator
    getVirtualDataIterator()
    {
	return new DefaultVirtualDataIterator();
    }


    /**
     * Gets the type of the values of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected ScalarType
    getRangeType(Variable var)
	throws VisADException
    {
	return util.getScalarType(var);
    }


    /**
     * Gets the sampling set of the values of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected SimpleSet
    getRangeSet(Variable var)
	throws VisADException
    {
	return util.getValueSet(var);
    }


    /**
     * Gets the unit of the values of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @return			The unit of the values of <code>var</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected Unit
    getUnit(Variable var)
	throws VisADException
    {
	return util.getUnit(var);
    }


    /**
     * Gets the value-vetter of a netCDF variable.
     *
     * @param var		A netCDF variable.
     */
    protected Vetter
    getVetter(Variable var)
    {
	return new Vetter(var);
    }


    /**
     * Gets the outer dimension of a netCDF variable.
     *
     * @param var		A netCDF variable.
     */
    protected Dimension
    getOuterDimension(Variable var)
    {
	return var.getDimensionIterator().next();
    }


    /**
     * Gets the type of the inner domain of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected MathType
    getInnerDomainType(Variable var)
	throws VisADException
    {
	return util.getDomainType(getInnerDimensions(var));
    }


    /**
     * Gets the domain set of the inner domain of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    protected SampledSet
    getInnerDomainSet(Variable var)
	throws VisADException, IOException
    {
	return util.getDomainSet(getInnerDimensions(var));
    }


    /**
     * Gets the type of the outer domain of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @return			The VisAD MathType of the outer domain of
     *				<code>var</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected MathType
    getOuterDomainType(Variable var)
	throws VisADException
    {
	return util.getRealType(getOuterDimension(var));
    }


    /**
     * Gets the domain set of the outer domain of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    protected SampledSet
    getOuterDomainSet(Variable var)
	throws VisADException, IOException
    {
	return util.getDomainSet(getOuterDimension(var));
    }


    /**
     * Gets the type of the domain of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @return			The VisAD MathType of the domain of
     *				<code>var</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected MathType
    getDomainType(Variable var)
	throws VisADException
    {
	return util.getDomainType(getDimensions(var));
    }


    /**
     * Gets the domain set of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    protected SampledSet
    getDomainSet(Variable var)
	throws VisADException, IOException
    {
	return util.getDomainSet(getDimensions(var));
    }


    /**
     * Indicates whether or not a netCDF dimension refers to time.
     *
     * @param dim		A netCDF dimension.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    protected boolean
    isTime(Dimension dim)
	throws VisADException, IOException
    {
	return util.isTime(dim);
    }


    /**
     * Gets the inner dimensions of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @return			The inner dimensions of <code>var</code> in
     *				netCDF order.
     */
    protected Dimension[]
    getInnerDimensions(Variable var)
    {
	Dimension[]	dims = getDimensions(var);
	int		rank = var.getRank();
	Dimension[]	innerDims = new Dimension[rank-1];

	System.arraycopy(dims, 1, innerDims, 0, innerDims.length);

	return innerDims;
    }


    /**
     * Gets the dimensions of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @return			The dimensions of <code>var</code> in
     *				netCDF order.
     */
    protected Dimension[]
    getDimensions(Variable var)
    {
	int			rank = var.getRank();
	Dimension[]		dims = new Dimension[rank];
	DimensionIterator	iter = var.getDimensionIterator();

	for (int i = 0; i < rank; ++i)
	    dims[i] = iter.next();

	return dims;
    }


    /**
     * Supports iteration over the default virtual VisAD data objects in
     * a netCDF dataset.
     */
    public class
    DefaultVirtualDataIterator
	extends	VirtualDataIterator
    {
	/**
	 * The netCDF variable iterator.
	 */
	private final VariableIterator	varIter;


	/**
	 * Constructs from a view of a netCDF dataset.
	 *
	 * @param view		A view of a netCDF dataset.
	 */
	public
	DefaultVirtualDataIterator()
	{
	    super(DefaultView.this);

	    varIter = netcdf.iterator();
	}


	/**
	 * Returns a clone of the next virtual VisAD data object.
	 *
         * @return                      A clone of the next virtual VisAD data
         *                              object or <code> null</code> if there is
         *                              none.
	 * @throws VisADException	Couldn't create necessary VisAD object.
	 */
	protected VirtualData
	getData()
	    throws VisADException, IOException
	{
	    VirtualData	data = null;

	    while (varIter.hasNext())
	    {
		Variable	var = varIter.next();

		// TODO: support text
		if (var.getComponentType().equals(char.class))
		    continue;

		int		rank = var.getRank();

		if (util.isCoordinateVariable(var))
		    continue;

		/*
		 * The netCDF variable is not a coordinate variable.
		 */

		VirtualScalar	scalar =
		    new VirtualScalar(getRangeType(var),
				      var,
				      getRangeSet(var),
				      getUnit(var),
				      getVetter(var));

		if (rank == 0)
		{
		    /*
		     * The variable represents a true scalar.
		     */
		    data = scalar;
		}
		else if (rank == 1 || !isTime(getOuterDimension(var)))
		{
		    /*
		     * There's nothing special about the variable.
		     * Construct a simple Field.
		     */
		    data = new VirtualFlatField(
			new FunctionType(getDomainType(var),
					 getRangeType(var)),
			getDomainSet(var),
			new VirtualTuple(scalar));
		}
		else
		{
		    /*
                     * The variable represents a time series of array
                     * data.  Facilitate annimation by factoring out the
                     * time dimension through construction of a Field of
                     * Fields.
		     */
		    FunctionType	innerFieldType =
			new FunctionType(getInnerDomainType(var),
					 getRangeType(var));
		    VirtualField	innerField =
			VirtualField.newVirtualField(
			    innerFieldType,
			    getInnerDomainSet(var),
			    new VirtualTuple(scalar));

		    data = VirtualField.newVirtualField(
			new FunctionType(getOuterDomainType(var),
					 innerFieldType),
			getOuterDomainSet(var),
			new VirtualTuple(innerField));
		}

		break;
	    }			// variable iteration loop

	    return data;
	}
    }
}
