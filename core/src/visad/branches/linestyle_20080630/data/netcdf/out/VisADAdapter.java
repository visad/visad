/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VisADAdapter.java,v 1.7 2002-10-15 18:15:15 donm Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import java.rmi.RemoteException;
import ucar.multiarray.Accessor;
import ucar.multiarray.IndexIterator;
import ucar.netcdf.AbstractNetcdf;
import ucar.netcdf.Dimension;
import ucar.netcdf.ProtoVariable;
import visad.Data;
import visad.Field;
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
import visad.SetType;
import visad.Text;
import visad.Tuple;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * The VisADAdapter class adapts a VisAD data object to the AbstractNetcdf API.
 */
public class
VisADAdapter
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
    VisADAdapter(Data data)
	throws BadFormException, VisADException, RemoteException, IOException
    {
	try
	{
	    visit(data, new TrivialAccessor(data));
	}
	catch (UnimplementedException e)
	{
	    throw new BadFormException(e.getMessage());
	}
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
	int	charLen = 1;   // gotta have at least 1 character
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
     * @exception VisADException	Problem in core VisAD.  Probably some
     *					VisAD object couldn't be created.
     * @exception RemoteException	Remote data access failure.
     * @exception IOException		Local data access failure.
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
     * Define the netCDF dimensions and variables of a VisAD Field object.
     *
     * @param field		The VisAD Field to be visited
     * @param outerAccessor	The means for accessing the individual VisAD
     *				<code>Field</code> objects of the enclosing
     *				VisAD data object.
     * @exception UnimplementedException
     *					Something that should be implemented
     *					isn't yet.
     * @exception BadFormException	The VisAD data object cannot be adapted
     *					to a netCDF API
     * @exception VisADException	Problem in core VisAD.  Probably some
     *					VisAD object couldn't be created.
     * @exception RemoteException	Remote data access failure.
     * @exception IOException		Local data access failure.
     */
    protected void
    visit(Field field, VisADAccessor outerAccessor)
	throws RemoteException, VisADException, BadFormException,
	    UnimplementedException, IOException
    {
	Set		set = field.getDomainSet();
	Dimension[]	dims;

	if (set instanceof LinearSet)
	{
	    /*
	     * The domain set is a cross-product of arithmetic
	     * progressions.  This maps directly into the netCDF
	     * data model -- possibly with coordinate variables.
	     */
	    dims = defineLinearSetDims((GriddedSet)set);
	}				// the sample-domain is linear
	else
	if (set instanceof SampledSet)
	{
	    /*
	     * The domain set is not an arithmetic progression.
	     */
	    dims = new Dimension[] {defineSampledSetDim((SampledSet)set)};
	}
	else
	{
	    throw new BadFormException(
		"Can't handle a " + set.getClass().getName() + " domain set");
	}

	/*
	 * Continue the definition process on the inner, VisAD data
	 * objects by visiting the first sample.  The dimension array
	 * is converted to netCDF order (outermost dimension first).
	 */
	// System.out.println("visit(Field,...): RangeType: " +
	    // field.getSample(0).getType());
	visit(field.getSample(0),
	    new FieldAccessor(reverse(dims), outerAccessor));
    }


    /**
     * Define the netCDF dimensions of a VisAD LinearSet, including any
     * necessary coordinate variables..
     *
     * @param set	The VisAD GriddedSet to be examined, WHERE
     * 			<code>set instanceof LinearSet</code>.
     * @return		The netCDF dimensions of <code>set</code>.
     */
    protected Dimension[]
    defineLinearSetDims(GriddedSet set)
	throws VisADException, BadFormException
    {
	int		rank = set.getDimension();
	Dimension[]	dims = new Dimension[rank];
	RealTupleType	domainType = ((SetType)set.getType()).getDomain();
	Unit[]		units = set.getSetUnits();

	/*
	 * Define any necessary coordinate-variables.
	 */
	for (int idim = 0; idim < rank; ++idim)
	{
	    Linear1DSet	linear1DSet =
		((LinearSet)set).getLinear1DComponent(idim);
	    int		length = linear1DSet.getLength(0);
	    String	name =
		((RealType)domainType.getComponent(idim)).getName();

	    dims[idim] = new Dimension(name, length);

	    if (linear1DSet.getFirst() != 0.0 ||
		linear1DSet.getStep() != 1.0)
	    {
		/*
		 * The domain sampling has associated coordinate
		 * values; therefore, we define a corresponding
		 * netCDF coordinate-variable.
		 */
		CoordVar var =
		    new CoordVar(name, dims[idim], units[idim], linear1DSet);

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

	return dims;
    }


    /**
     * Define the netCDF dimensions and variables of a VisAD SampledSet.
     *
     * @param set	The VisAD SampledSet to be examined.
     * @return		The netCDF dimension of <code>set</code>.
     * @exception VisADException	Problem in core VisAD.
     * @exception BadFormException	<code>set</code> cannot be represented
     *					in a netCDF dataset.
     **/
    protected Dimension
    defineSampledSetDim(SampledSet set)
	throws VisADException, BadFormException
    {
	int	rank = set.getManifoldDimension();

	return rank == 1
		    ? define1DDim(set)
		    : defineNDDim(set);
    }


    /**
     * Define the netCDF dimension of a 1-D SampledSet.  This dimension will
     * have an associated, netCDF coordinate variable.
     *
     * @param set	The set to have a netCDF dimension defined for it.
     *			Precondition: <code>set.getDimension() == 1</code>.
     * @return		The netCDF dimension corresponding to the 1-D
     *			SampledSet.
     * @exception VisADException	Problem in core VisAD.
     * @exception BadFormException	<code>set</code> cannot be represented
     *					in a netCDF dataset.
     */
    protected Dimension
    define1DDim(SampledSet set)
	throws VisADException, BadFormException
    {
	RealTupleType	domainType = ((SetType)set.getType()).getDomain();
	String		name =
	    ((RealType)domainType.getComponent(0)).getName();
	Dimension	dim = new Dimension(name, set.getLength());
	Unit[]		units = set.getSetUnits();
	if (!(set instanceof Gridded1DSet))
	    throw new BadFormException("Domain set not Gridded1DSet");
	CoordVar	var =
	    new CoordVar(name, dim, units[0], (Gridded1DSet)set);
	try
	{
	    add(var, var);
	}
	catch (Exception e)
	{
	    throw new BadFormException(e.getMessage());
	}

	return dim;
    }


    /**
     * Define the netCDF dimension of a multi-dimensional SampledSet.
     * This will be an "index" dimension with associated netCDF variables
     * that represent the independent coordinates of the domain set.
     *
     * @param set	The VisAD SampledSet to be examined and have
     *			a corresponding netCDF "index" dimension created
     *			together with netCDF variables for the independent
     *			variables.  Precondition: <code>set.getDimension()
     *			> 1</code>.
     * @return		The netCDF dimension corresponding to the
     *			SampledSet.
     * @exception VisADException	Problem in core VisAD.
     * @exception BadFormException	<code>set</code> cannot be represented
     *					in a netCDF dataset.
     */
    protected Dimension
    defineNDDim(SampledSet set)
	throws VisADException, BadFormException
    {
	Dimension	dim;
	RealTupleType	domainType = ((SetType)set.getType()).getDomain();
	int		rank = domainType.getDimension();
	Unit[]		units = set.getSetUnits();
	String[]	names = new String[rank];

	/*
	 * Get the names.
	 */
	for (int idim = 0; idim < rank; ++idim)
	    names[idim] = ((RealType)domainType.getComponent(idim)).getName();

	/*
	 * Define the "index" dimension.
	 */
	{
	    int			len = names[0].length();

	    for (int idim = 1; idim < rank; ++idim)
		len += 1 + names[idim].length();
	    len += 4;

	    StringBuffer	name = new StringBuffer(len);

	    name.append(names[0]);

	    for (int idim = 1; idim < rank; ++idim)
	    {
		name.append("_");
		name.append(names[idim]);
	    }
	    name.append("_ndx");

	    dim = new Dimension(name.toString(), set.getLength());
	}

	/*
	 * Define the independent variables.
	 */
	for (int idim = 0; idim < rank; ++idim)
	{
	    IndependentVar	var =
		new IndependentVar(names[idim], dim, units[idim],
				    (SampledSet)set, idim);

	    try
	    {
		add(var, var);
	    }
	    catch (Exception e)
	    {
		throw new BadFormException(e.getMessage());
	    }
	}

	return dim;
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
