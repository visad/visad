/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DataAdapter.java,v 1.7 1998-03-17 20:48:42 steve Exp $
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
import visad.Gridded1DSet;
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
 * The DataAdapter class adapts a VisAD data object to the AbstractNetcdf API.
 */
public class
DataAdapter
    extends	AbstractNetcdf
{
    /**
     * Construct from a generic VisAD data object.
     *
     * @param data	The VisAD data object to be adapted to a netCDF
     *			API
     * @exception UnimplementedException	Something that should be
     *			implemented isn't yet.
     * @exception BadFormException	The VisAD data object cannot be
     *			adapted to a netCDF API
     * @exception VisADException	Problem in core VisAD.  Some VisAD
     *			object probably couldn't be created.
     * @exception RemoteException	Remote data access failure.
     * @exception IOException		Data access failure.
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
     *
     * @param data		The VisAD data object to be visited.
     * @param outerAccessor	The means for accessing the individual VisAD
     *				<code>data</code> objects of the enclosing
     *				VisAD data object.
     * @exception UnimplementedException	Something that should be
     *		implemented isn't yet.
     * @exception BadFormException		The VisAD data object cannot be
     *		adapted to a netCDF API
     * @exception VisADException		Problem in core VisAD.
     *		Probably some VisAD object couldn't be created.
     * @exception RemoteException		Remote data access failure.
     * @exception IOException			Data access failure.
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
     *
     * @param text		The VisAD Text object to be visited.
     * @param outerAccessor	The means for accessing the individual VisAD
     *				<code>Text</code> objects of the enclosing
     *				VisAD data object.
     * @exception BadFormException		The VisAD data object cannot be
     *		adapted to a netCDF API
     * @exception IOException			Data access failure.
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
     *
     * @param real		The VisAD Real object to be visited.
     * @param outerAccessor	The means for accessing the individual VisAD
     *				<code>Real</code> objects of the enclosing
     *				VisAD data object.
     * @exception BadFormException		The VisAD data object cannot be
     *		adapted to a netCDF API
     * @exception VisADException		Problem in core VisAD.
     *		Probably some VisAD object couldn't be created.
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
     *
     * @param tuple		The VisAD Tuple object to be visited.
     * @param outerAccessor	The means for accessing the individual VisAD
     *				<code>Tuple</code> objects of the enclosing
     *				VisAD data object.
     * @exception VisADException		Problem in core VisAD.
     *		Probably some VisAD object couldn't be created.
     * @exception RemoteException		Remote data access failure.
     * @exception IOException			Data access failure.
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
     *
     * @param field		The VisAD Field to be visited
     * @param outerAccessor	The means for accessing the individual VisAD
     *				<code>Field</code> objects of the enclosing
     *				VisAD data object.
     * @exception UnimplementedException	Something that should be
     *		implemented isn't yet.
     * @exception BadFormException		The VisAD data object cannot be
     *		adapted to a netCDF API
     * @exception VisADException		Problem in core VisAD.
     *		Probably some VisAD object couldn't be created.
     * @exception RemoteException		Remote data access failure.
     * @exception IOException			Data access failure.
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
			 * The domain sampling has associated coordinate 
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
	    if (rank == 1)
	    {
		/*
                 * The domain set is 1-dimensional but not an arithmetic
                 * progression.  The way to handle this in the standard
                 * netCDF data model is to associate a coordinate
                 * variable with the sample points.
		 */

		/*
		 * Create the domain dimension.
		 */
		Dimension	dim = 
		    new Dimension(dimNames[0], set.getLength());

		dims = new Dimension[] { dim };

		/*
		 * Define the coordinate variable.
		 */
		{
		    CoordVar var = new CoordVar(dimNames[0], dim, dimUnits[0],
			(Gridded1DSet)set);

		    try
		    {
			add(var, var);
		    }
		    catch (Exception e)
		    {
			throw new BadFormException(e.getMessage());
		    }
		}
	    }
	    else
	    {
		/*
                 * The domain set is multi-dimensional and not a
                 * cross-product of arithmetic progressions.  The
                 * simplest way to handle this in the standard netCDF
                 * data model is to represent *both* the independent and
                 * dependent data as ordinary, one-dimensional netCDF
                 * variables of an artificial "index" dimension.
		 */

		Dimension	dim;

		/*
		 * Create the index dimension.
		 */
		{
		    int		len = dimNames[0].length();

		    for (int idim = 1; idim < rank; ++idim)
			len += 1 + dimNames[idim].length();
		    len += 4;

		    StringBuffer	dimName = new StringBuffer(len);

		    dimName.append(dimNames[0]);

		    for (int idim = 1; idim < rank; ++idim)
		    {
			dimName.append("_");
			dimName.append(dimNames[idim]);
		    }
		    dimName.append("_ndx");

		    dim = new Dimension(dimName.toString(), set.getLength());
		}

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
	     * objects by visiting the first sample.  The dimension array
	     * is converted to netCDF order (outermost dimension first).
	     */
	    // System.out.println("visit(Field,...): RangeType: " +
		// field.getSample(0).getType());
	    visit(field.getSample(0),
		new FieldAccessor(reverse(dims), outerAccessor));
	}				// domain set is a GriddedSet
    }


    /**
     * Reverse the dimensions in a 1-D array.
     */
    protected Dimension[]
    reverse(Dimension[] inDims)
    {
	Dimension[]	outDims = new Dimension[inDims.length];

	for (int i = 0; i < inDims.length; ++i)
	    outDims[i] = inDims[inDims.length - 1 - i];
    
	return outDims;
    }


    /**
     * Return a MultiArray Accessor for a variable.
     *
     * This method is part of the AbstractNetcdf class and should never
     * be called -- so it always throws an error.
     */
    public Accessor
    ioFactory(ProtoVariable protoVar)
    {
	throw new UnsupportedOperationException();
    }
}
