package visad.data.netcdf;


import ucar.netcdf.Dimension;
import visad.FlatField;
import visad.GriddedSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.Set;
import visad.SetType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.DataVisitor;


/**
 * Class for defining the netCDF dimensions in a VisAD data object.  
 * Extends class visad.data.DataVisitor.
 */
class
DimDefiner
    extends	DataVisitor
{
    protected final DimSet	dimSet = new DimSet();


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
     * @see visad.data.DataVisitor
     * @see visad.data.DataNode
     */
    public boolean
    visit(FlatField field)
	throws BadFormException, VisADException
    {
	Set	set = field.getDomainSet();

	if (!(set instanceof SampledSet))
	    throw new BadFormException("The netCDF data model can't " +
		"handle a non-sampled domain");

	if (!(set instanceof GriddedSet))
	{
	    int	rank = field.getDomainDimension();

	    // TODO
	    throw new BadFormException(
		"Can't yet handle an irregularly-sampled domain");
	}
	else
	{
	    /*
	     * The domain sample-set is a GriddedSet.
	     */

	    int			rank = field.getDomainDimension();
	    MathType		domainMathType = ((SetType)set.getType()).
				    getDomain();
	    Unit[]		dimUnits = new Unit[rank];
	    String[]		dimNames = new String[rank];
	    Dimension[]		dims;

	    // TODO: handle more cases
	    // TODO: handle units
	    if (domainMathType instanceof RealType && rank == 1)
	    {
		dimNames[0] = ((RealType)domainMathType).getName();
		dimUnits[0] = null;
	    }
	    else
	    if (domainMathType instanceof RealTupleType)
	    {
		for (int idim = 0; idim < rank; ++idim)
		{
		    dimNames[idim] = ((RealType)((RealTupleType)domainMathType).
			getComponent(idim)).getName();
		    dimUnits[0] = null;
		}
	    }
	    else
		throw new BadFormException(
		    "Unsupported domain-set: " + domainMathType.getClass());

	    /*
	     * Create the netCDF dimensions.
	     */
	    if (set instanceof LinearSet)
	    {
		/*
		 * The sample domain is a cross-product of independent
		 * arithmetic progressions.  This maps directly into the
		 * netCDF data model.
		 */

		dims = new Dimension[rank];

		/*
		 * Create the netCDF dimensions.
		 */
		for (int idim = 0; idim < rank; ++idim)
		{
		    int	length = ((LinearSet)set).getLinear1DComponent(idim).
				    getLength(0);

		    dimSet.put(new Dimension(dimNames[idim], length));
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
		 * Define the index dimension.
		 */
		dimSet.put(new Dimension("index", set.getLength()));
	    }				// the sample-domain is not linear
	}				// the sample domain is a GriddedSet

	return false;			// no further tranversal necessary
    }

    
    /**
     * Return the set of netCDF dimensions.
     *
     * @return	The set of netCDF dimensions seen.
     */
    DimSet
    getSet()
    {
	return dimSet;
    }
}
