/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VarDefiner.java,v 1.7 1998-02-23 15:58:32 steve Exp $
 */

package visad.data.netcdf;


import java.rmi.RemoteException;
import ucar.multiarray.Accessor;
import ucar.netcdf.Dimension;
import ucar.netcdf.ProtoVariable;
import ucar.netcdf.Variable;
import visad.FlatField;
import visad.FunctionType;
import visad.GriddedSet;
import visad.IntegerSet;
import visad.Linear1DSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.Set;
import visad.SetType;
import visad.TupleType;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.DataVisitor;


/**
 * Class for defining the netCDF variables in a VisAD data object.
 */
class
VarDefiner
    extends DataVisitor
{
    /**
     * The VisAD data object adapted to a netCDF dataset.
     */
    protected final VisADAdapter	adapter;


    /**
     * Construct.
     */
    VarDefiner(VisADAdapter adapter)
    {
	this.adapter = adapter;
    }


    /**
     * Visit a VisAD FlatField.
     *
     * @param field	The FlatField to be visited.
     * @precondition	<code>field</code> is non-null.
     * @return	<code>true</code> if and only if further traversal of the
     *		subcomponents of the FlatField is appropriate.  Used by
     *		visad.data.DataNode.
     * @exception BadFormException	This FlatField doesn't map to a netCDF
     *					dataset.
     * @exception VisADException	Problem in core VisAD (probably in the
     *					creation of a VisAD object).
     * @exception UnimplementedException	Some method that should (will
     *					be?) implemented isn't yet.
     * @see visad.data.DataVisitor
     * @see visad.data.DataNode
     */
    public boolean
    visit(FlatField field)
	throws BadFormException, UnimplementedException, VisADException,
	    RemoteException
    {
	Set	set = field.getDomainSet();

	if (!(set instanceof SampledSet))
	    throw new BadFormException("The netCDF data model can't " +
		"handle a non-sampled domain");

	if (!(set instanceof GriddedSet))
	{
	    int	rank = field.getDomainDimension();

	    // TODO
	    throw new UnimplementedException(
		"Can't yet handle an irregularly-sampled domain");
	}
	else
	{
	    /*
	     * The domain sample-set is a GriddedSet.
	     */

	    int			rank = field.getDomainDimension();
	    int			totalNvar = field.getRangeDimension();
	    MathType		domainMathType = ((SetType)set.getType()).
				    getDomain();
	    Unit[]		dimUnits = field.getDomainUnits();
	    String[]		dimNames = new String[rank];
	    Dimension[]		dims;


	    // TODO: handle more cases
	    if (domainMathType instanceof RealType && rank == 1)
		dimNames[0] = ((RealType)domainMathType).getName();
	    else
	    if (domainMathType instanceof RealTupleType)
	    {
		RealTupleType	realTupleType = (RealTupleType)domainMathType;

		for (int idim = 0; idim < rank; ++idim)
		    dimNames[idim] = 
			((RealType)realTupleType.getComponent(idim)).getName();
	    }
	    else
		throw new UnimplementedException(
		    "Unsupported domain-set: " + domainMathType.getClass());

	    if (set instanceof LinearSet)
	    {
		/*
		 * The sample domain is a cross-product of independent
		 * arithmetic progressions.  This maps directly into the
		 * netCDF data model.
		 */

		dims = new Dimension[rank];

		/*
		 * Define any necessary coordinate-variables.
		 */
		for (int idim = 0; idim < rank; ++idim)
		{
		    Linear1DSet	linear1DSet =
			((LinearSet)set).getLinear1DComponent(idim);
		    int		length = linear1DSet.getLength(0);
		    String	name = dimNames[idim];

		    dims[idim] = new Dimension(name, length);

		    if (linear1DSet.getFirst() != 0.0 ||
		        linear1DSet.getStep() != 1.0)
		    {
			/*
			 * The domain sampling has associated co-ordinate 
			 * values; therefore, we define a corresponding
			 * netCDF coordinate-variable.
			 */
			CoordVar var = new CoordVar(name, dims[idim],
			    dimUnits[idim], linear1DSet);

			adapter.add(var);
		    }
		}			// sample-domain dimension loop
	    }				// the sample-domain is linear
	    else
	    {
		/*
                 * The (GriddedSet) domain set is not a simple
                 * cross-product of arithmetic progressions.  The
                 * simplest way to handle this in the standard netCDF
                 * data model is to represent *both* the independent and
                 * dependent data as ordinary, one-dimensional netCDF
                 * variables of an artificial "index" dimension.
		 */

		/*
		 * Create the index dimension.
		 */
		Dimension	dim = new Dimension("index", set.getLength());

		dims = new Dimension[] { dim };

		/*
		 * Define the independent variables.
		 */
		for (int idim = 0; idim < rank; ++idim)
		{
		    IndependentVar	var = new IndependentVar(
			dimNames[idim], dim, dimUnits[idim], (GriddedSet)set,
			idim);

		    adapter.add(var);
		}
	    }				// the sample-domain is not linear

	    /*
	     * Define the dependent variables.
	     *
	     * NOTE: This is complicated by the fact that the range of a
	     * FlatField can be a Real, a RealTuple, or a Tuple of Reals
	     * and/or RealTuples.
	     */
	    {
		MathType	rangeType = 
		    ((FunctionType)field.getType()).getRange();
		Unit[][]	rangeUnits = field.getRangeUnits();

		if (rangeType instanceof RealType)
		{
		    RealVar	var = new RealVar(
			((RealType)rangeType).getName(),
			dims, rangeUnits[0][0], field, 
			((RealType)rangeType).getDefaultSet());

		    adapter.add(var);
		}
		else
		if (rangeType instanceof RealTupleType)
		{
		    RealTupleType	realTupleType =
			(RealTupleType)rangeType;
		    int	nvar = realTupleType.getDimension();

		    for (int ivar = 0; ivar < nvar; ++ivar)
		    {
			TupleVar	var = new TupleVar(
			    ((RealType)realTupleType.getComponent(ivar)).
				getName(),
			    dims, rangeUnits[ivar][0], field, ivar);

			adapter.add(var);
		    }
		}
		else
		if (rangeType instanceof TupleType)
		{
		    TupleType	tupleType = (TupleType)rangeType;
		    int		ncomp = tupleType.getDimension();
		    int		iunit = 0;

		    for (int icomp = 0; icomp < ncomp; ++icomp)
		    {
			MathType	compType 
			    = tupleType.getComponent(icomp);

			if (compType instanceof RealType)
			{
			    TupleVar	var = new TupleVar(
				((RealType)compType).getName(),
				dims, rangeUnits[iunit++][0], field,
				icomp);

			    adapter.add(var);
			}
			else
			if (compType instanceof RealTupleType)
			{
			    RealTupleType	realTupleType =
				(RealTupleType)compType;
			    int			 nvar =
				realTupleType.getDimension();

			    for (int ivar = 0; ivar < nvar; ++ivar)
			    {
				TupleRealVar	var = new TupleRealVar(
					((RealType)realTupleType.
					    getComponent(ivar)).getName(),
					dims, rangeUnits[iunit++][0], icomp,
					ivar, field);

				adapter.add(var);
			    }
			}
		    }			// Tuple-range component-loop
		}			// the FlatField range is a Tuple
	    }				// dependent-variable definition-block
	}				// the sample domain is a GriddedSet

	return false;		// further traversal of the VisAD FlatField
				// is inappropriate
    }
}
