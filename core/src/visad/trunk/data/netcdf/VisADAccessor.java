/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VisADAccessor.java,v 1.1 1998-03-10 19:49:39 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Data;
import visad.Field;
import visad.Real;
import visad.Text;
import visad.Tuple;


/**
 * Interface for accessing data in a VisAD data object that's been 
 * adapted to a netCDF variable API.
 */
interface
VisADAccessor
{
    /**
     * Return the number of netCDF dimensions at the current level.
     */
    int
    getRank();


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     */
    Dimension[]
    getDimensions();


    /**
     * Return the netCDF dimensional lengths.
     */
    int[]
    getLengths();


    /**
     * Return a datum given its location as netCDF indexes.
     */
    Object
    get(int[] indexes)
	throws IOException;
}


/**
 * The trivial VisADAccessor.  It is used to terminate the linked-list of
 * data accessors at the outermost, VisAD data object.
 */
class
TrivialAccessor
    implements	VisADAccessor
{
    /**
     * The VisAD data object.
     */
    protected final Data	data;


    /**
     * Construct from a VisAD data object.
     */
    protected
    TrivialAccessor(Data data)
    {
	this.data = data;
    }


    /**
     * Return the number of netCDF dimensions at the current level.
     */
    public int
    getRank()
    {
	return 0;
    }


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     */
    public Dimension[]
    getDimensions()
    {
	return new Dimension[0];
    }


    /**
     * Return the netCDF dimensional lengths.
     */
    public int[]
    getLengths()
    {
	return new int[0];
    }


    /**
     * Return a datum given its location as netCDF indexes.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	return data;
    }
}


/**
 * Class for accessing data in a VisAD data object that's been adapted
 * to a netCDF variable API.
 */
abstract class
DataAccessor
    implements	VisADAccessor
{
    /**
     * The number of netCDF dimensions in the VisAD data object.
     */
    protected final int			localRank;

    /**
     * The netCDF Dimensions of the VisAD data object.
     */
    protected final Dimension[]		localDims;

    /**
     * The netCDF indexes for the Field.
     */
    protected volatile int[]		localIndexes;

    /**
     * The VisADAccessor of the outer VisAD data object.
     */
    protected final VisADAccessor	outerAccessor;

    /**
     * The number of netCDF dimensions in the outer VisAD data object.
     */
    protected final int			outerRank;

    /**
     * The netCDF indexes for the outer VisAD data object.
     */
    protected volatile int[]		outerIndexes;


    /**
     * Construct from an outer VisADAccessor and netCDF Dimensions.
     *
     * @precondition	<code>outerAccessor != null</code>
     */
    protected
    DataAccessor(Dimension[] localDims, VisADAccessor outerAccessor)
    {
	localRank = localDims.length;
	outerRank = outerAccessor.getRank();
	this.localDims = localDims;
	localIndexes = new int[localRank];
	outerIndexes = new int[outerRank];
	this.outerAccessor = outerAccessor;
    }


    /**
     * Return the number of netCDF dimensions at the current level.
     */
    public int
    getRank()
    {
	return outerRank + localRank;
    }


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     */
    public Dimension[]
    getDimensions()
    {
	Dimension[]	dims = new Dimension[getRank()];

	if (outerAccessor != null)
	    System.arraycopy(outerAccessor.getDimensions(), 0,
		dims, 0, outerRank);
	System.arraycopy(localDims, 0, dims, outerRank, localRank);

	return dims;
    }


    /**
     * Return the netCDF dimensional lengths.
     */
    public int[]
    getLengths()
    {
	int[]		lengths = new int[getRank()];
	Dimension[]	outerDims = getDimensions();

	for (int i = 0; i < lengths.length; ++i)
	    lengths[i] = outerDims[i].getLength();

	return lengths;
    }


    /**
     * Return a datum given its location as netCDF indexes.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	System.arraycopy(indexes, 0, outerIndexes, 0, outerRank);
	System.arraycopy(indexes, outerRank, localIndexes, 0, localRank);

	return get();
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected abstract Object
    get()
	throws IOException;
}


/**
 * Class for accessing data in a VisAD Text that's been adapted to a 
 * netCDF API.
 */
class
TextAccessor
    extends	DataAccessor
{
    /**
     * The missing-value character.
     */
    protected final Character		space = new Character(' ');


    /**
     * Construct from a netCDF Dimension and an outer VisADAccessor.
     */
    protected
    TextAccessor(Dimension charDim, VisADAccessor outerAccessor)
    {
	super(new Dimension[] {charDim}, outerAccessor);
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException, StringIndexOutOfBoundsException
    {
	return new Character(((Text)outerAccessor.get(outerIndexes)).
	    getValue().charAt(localIndexes[0]));
    }
}


/**
 * Class for accessing data in a VisAD Real that's been adapted to a
 * netCDF API.
 */
class
RealAccessor
    extends	DataAccessor
{
    /**
     * Construct from an outer accessor.
     */
    protected
    RealAccessor(VisADAccessor outerAccessor)
    {
	super(new Dimension[0], outerAccessor);
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException
    {
	try
	{
	    return new Double(((Real)outerAccessor.get(outerIndexes)).
		getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}


/**
 * Class for accessing data in a VisAD Tuple that's been adapted to a
 * netCDF API.
 */
class
TupleAccessor
    extends	DataAccessor
{
    /**
     * The index of the relevant component.
     */
    protected final int		index;


    /**
     * Construct from a component index and an outer VisADAccessor.
     */
    protected
    TupleAccessor(int index, VisADAccessor outerAccessor)
    {
	super(new Dimension[0], outerAccessor);
	this.index = index;
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException
    {
	try
	{
	    return ((Tuple)outerAccessor.get(outerIndexes)).getComponent(index);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}


/**
 * Class for accessing data in a VisAD Field that's been adapted to a
 * netCDF API.
 */
class
FieldAccessor
    extends	DataAccessor
{
    /**
     * The shape of the netCDF variables of the VisAD Field in netCDF order
     * (i.e. outermost dimension first).
     */
    protected final int[]	shape;


    /**
     * Construct from netCDF Dimensions, and an outer VisADAccessor.
     */
    protected
    FieldAccessor(Dimension[] localDims, VisADAccessor outerAccessor)
    {
	super(localDims, outerAccessor);

	shape = new int[localRank];
	for (int idim = 0; idim < localRank; ++idim)
	    shape[idim] = localDims[localRank-1-idim].getLength();
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException
    {
	int	visadIndex = localIndexes[0];

	for (int i = 1; i < localRank; ++i)
	    visadIndex = visadIndex * shape[i] + localIndexes[i];

	try
	{
	    return ((Field)outerAccessor.get(outerIndexes)).
		getSample(visadIndex);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}
