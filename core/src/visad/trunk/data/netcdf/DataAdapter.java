/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DataAdapter.java,v 1.3 1998-03-10 19:49:31 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import java.rmi.RemoteException;
import ucar.multiarray.Accessor;
import ucar.multiarray.IndexIterator;
import ucar.netcdf.AbstractNetcdf;
import ucar.netcdf.Dimension;
import ucar.netcdf.ProtoVariable;
import visad.Data;
import visad.Field;
import visad.FunctionType;
import visad.GriddedSet;
import visad.Linear1DSet;
import visad.LinearSet;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.ScalarType;
import visad.Set;
import visad.Text;
import visad.Tuple;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * Class for adapting a VisAD data object to a netCDF API.
 */
public class
DataAdapter
    extends	AbstractNetcdf
{
    /**
     * Construct from a generic VisAD data object.
     */
    public
    DataAdapter(Data data)
	throws UnimplementedException, BadFormException, VisADException,
	    RemoteException, IOException
    {
	visit(data, new TrivialAccessor(data));
    }


    /**
     * Visit a VisAD data object.
     */
    protected void
    visit(Data data, VisADAccessor outerAccessor)
	throws UnimplementedException, BadFormException, VisADException,
	    RemoteException, IOException
    {
	/*
	 * Watch the ordering in the following because the first match 
	 * will be used.
	 */
	if (data instanceof Text)
	    visit((Text)data, outerAccessor);
	else
	if (data instanceof Real)
	    visit((Real)data, outerAccessor);
	else
	if (data instanceof Tuple)
	    visit((Tuple)data, outerAccessor);
	else
	if (data instanceof Field)
	    visit((Field)data, outerAccessor);
	else
	    throw new UnimplementedException(
		"VisAD data type not yet supported: " + 
		data.getClass().getName());
    }


    /**
     * Visit a VisAD Text object.
     */
    protected void
    visit(Text text, VisADAccessor outerAccessor)
	throws BadFormException, IOException
    {
	/*
         * Because netCDF text variables must have a "character length"
         * dimension, we traverse all the strings in order to determine
         * the maximum length.
	 */
	int	charLen = 0;
	for (IndexIterator index = 
		new IndexIterator(outerAccessor.getLengths());
	     index.notDone();
	     index.incr())
	{
	    int	len = ((Text)outerAccessor.get(index.value()))
		.getValue().length();
	    if (len > charLen)
		charLen = len;
	}

	/*
	 * Define the new character dimension.
	 */
	String		dimName = ((ScalarType)text.getType()).getName() +
	    "_len"; 
	Dimension	charDim = new Dimension(dimName, charLen);
	putDimension(charDim);

	/*
	 * Define the new netCDF character variable.
	 */
	DependentTextVar	var = new DependentTextVar(text,
	    new TextAccessor(charDim, outerAccessor));
	try
	{
	    add(var, var);
	}
	catch (Exception e)
	{
	    throw new BadFormException(e.getMessage());
	}
    }


    /**
     * Visit a VisAD Real object.
     */
    protected void
    visit(Real real, VisADAccessor outerAccessor)
	throws BadFormException, VisADException
    {
	DependentRealVar	var = new DependentRealVar(real,
	    new RealAccessor(outerAccessor));

	try
	{
	    add(var, var);
	}
	catch (Exception e)
	{
	    throw new BadFormException(e.getMessage());
	}
    }


    /**
     * Visit a VisAD Tuple object.
     */
    protected void
    visit(Tuple tuple, VisADAccessor outerAccessor)
	throws VisADException, RemoteException, IOException
    {
	int	componentCount = tuple.getDimension();

	for (int i = 0; i < componentCount; ++i)
	    visit(tuple.getComponent(i), new TupleAccessor(i, outerAccessor));
    }


    /**
     * Visit a VisAD Field object.
     */
    protected void
    visit(Field field, VisADAccessor outerAccessor)
	throws RemoteException, VisADException, BadFormException,
	    UnimplementedException, IOException
    {
	Set	set = field.getDomainSet();

	if (!(set instanceof SampledSet))
	    throw new BadFormException("The netCDF data model can't " +
		"handle a non-sampled domain");

	// ELSE

	if (!(set instanceof GriddedSet))
	{
	    int	rank = field.getDomainDimension();

	    // TODO
	    throw new UnimplementedException(
		"Can't yet handle an irregularly-sampled domain");
	}

	// ELSE

	{
	    /*
	     * The domain sample-set is a GriddedSet.
	     */

	    int			rank = set.getDimension();
	    RealTupleType	domainMathType = 
		((FunctionType)field.getType()).getDomain();
	    Unit[]		dimUnits = field.getDomainUnits();
	    String[]		dimNames = new String[rank];
	    Dimension[]		dims;

	    for (int idim = 0; idim < rank; ++idim)
		dimNames[idim] = 
		    ((RealType)domainMathType.getComponent(idim)).getName();

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

			try
			{
			    add(var, var);
			}
			catch (Exception e)
			{
			    throw new BadFormException(e.getMessage());
			}
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

		    try
		    {
			add(var, var);
		    }
		    catch (Exception e)
		    {
			throw new BadFormException(e.getMessage());
		    }
		}
	    }				// domain set is not LinearSet

	    /*
	     * Continue the definition process on the inner, VisAD data 
	     * objects by visiting the first sample.
	     */
	    visit(field.getSample(0), new FieldAccessor(dims, 
		outerAccessor));
	}				// domain set is a GriddedSet
    }


    /**
     * Return a MultiArray Accessor for a variable.
     *
     * This method should never be called -- so it always throws an error.
     */
    public Accessor
    ioFactory(ProtoVariable protoVar)
    {
	throw new UnsupportedOperationException();
    }
}
