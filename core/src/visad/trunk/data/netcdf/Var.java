package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import ucar.netcdf.MultiArray;
import ucar.netcdf.Variable;
import visad.Field;
import visad.GriddedSet;
import visad.Linear1DSet;
import visad.Real;
import visad.RealTuple;
import visad.Tuple;
import visad.Unit;
import visad.data.BadFormException;


/*
 * Superclass for adapting VisAD data to a netCDF Variable.
 */
abstract class
Var
    extends Variable
{
    /**
     * The units.
     */
    protected final Unit	unit;


    /**
     * Construct from broken-out information.
     */
    protected
    Var(String name, Class type, Dimension[] dims, Unit unit)
	throws BadFormException
    {
	super(name, type, reverse(dims), 
	    unit == null
		? new Attribute[]
		    { 
			new Attribute("_FillValue", Double.NaN)
		    }
		: new Attribute[]
		    { 
			new Attribute("units", unit.toString()),
			new Attribute("_FillValue", Double.NaN)
		    }
	);

	this.unit = unit;
    }


    /**
     * Reverse the order of an array of dimensions.
     */
    private static Dimension[]
    reverse(Dimension[] dims)
	throws BadFormException
    {
	Dimension[]	reversed = new Dimension[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    reversed[idim] = dims[dims.length-1-idim];

	return reversed;
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    set(int[] origin, MultiArray multiArray)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    set(int[] index,  Object value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setBoolean(int[] index,  boolean value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setChar(int[] index,  char value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setByte(int[] index,  byte value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setShort(int[] index, short value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setInt(int[] index, int value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setLong(int[] index, long value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setFloat(int[] index, float value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setDouble(int[] index, double value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     *
     * This is the only method that needs to be implemented to support
     * the saving of a VisAD data object in a netCDF dataset.
     */
     public abstract Object
     get(int[] indexes)
	throws IOException;


    /**
     * Return an array element identified by position.
     */
     public boolean
     getBoolean(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public char
     getChar(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public byte
     getByte(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public short
     getShort(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public int
     getInt(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public long
     getLong(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public float
     getFloat(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public double
     getDouble(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return a MultiArray into a slice of the data.
     */
    public MultiArray
    get(int[] origin, int[] shape)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }
}


/*
 * Class for independent variables.  An independent variable is a netCDF
 * variable that has been created because it was necessary to define
 * the netCDF variables in terms of an "index" co-ordinate.  This can
 * happen, for example, in a Fleld with a domain that's a GriddedSet
 * but not a LinearSet.
 */
class
IndependentVar
    extends Var
{
    /**
     * The dimension index of the "variable" in the domain.
     */
    protected final int		idim;


    /**
     * The sampling domain set.
     */
    protected final GriddedSet	set;


    /**
     * Construct from broken-out information.
     */
    protected
    IndependentVar(String name, Dimension dim, Unit unit,
	    GriddedSet set, int idim)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, unit);
	this.set = set;
	this.idim = idim;
    }


    /**
     * Return an array element identified by position.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	int	index = indexes[indexes.length-1];

	try
	{
	    return new Float(set.indexToValue(new int[] {index})[idim][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}


/**
 * Superclass for a netCDF variable that exists somewhere in the 
 * range of a Field.
 */
abstract class
RangeVar
    extends	Var
{
    /**
     * The field in which the data resides.
     */
    protected final Field	field;

    /**
     * The shape of the netCDF variable in netCDF order (i.e. big
     * endian).
     */
    protected /*final*/ int[]	shape;


    /**
     * Construct from broken-out information.
     */
    protected
    RangeVar(String name, Dimension[] dims, Unit unit, Field field)
	throws BadFormException
    {
	super(name, Double.TYPE, dims, unit);

	this.field = field;

	shape = new int[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    shape[idim] = dims[dims.length-1-idim].getLength();
    }


    /**
     * Convert a netCDF index vector to a VisAD scalar index.
     *
     * @param netcdfIndexes	Vector of netCDF indexes; innermost dimension
     *			last.
     * @return		VisAD index corresponding to netCDF index.
     * @precondition	<code>netcdfIndex</code> != null && 
     * 			<code>netcdfIndex.length</code> == domain rank
     *			&& indexed point lies within the sampling set.
     * @postcondition	Returned VisAD index lies within sampling set.
     */
    protected int
    visadIndex(int[] netcdfIndexes)
    {
	int	visadIndex = netcdfIndexes[0];

	for (int i = 1; i < shape.length; ++i)
	    visadIndex = visadIndex * shape[i] + netcdfIndexes[i];

	return visadIndex;
    }
}


/**
 * Inner class for a netCDF variable comprising a Field's range.
 */
class
RealVar
    extends RangeVar
{
    /**
     * Construct from broken-out information.
     */
    protected
    RealVar(String name, Dimension[] dims, Unit unit, Field field)
	throws BadFormException
    {
	super(name, dims, unit, field);
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return new Double(((Real)field.getSample(visadIndex(indexes))).
		getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}


/*
 * Class for a netCDF variable comprising a Real component of 
 * a Field's Tuple or RealTuple range.
 */
class
TupleVar
    extends RealVar
{
    /**
     * The index of the component in the Tuple range corresponding
     * to the variable.
     */
    protected final int		icomp;


    /**
     * Construct from broken-out information.
     */
    protected
    TupleVar(String name, Dimension[] dims, Unit unit, Field field, int icomp)
	throws BadFormException
    {
	super(name, dims, unit, field);
	this.icomp = icomp;
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return new Double(((Real)((Tuple)field.
		getSample(visadIndex(indexes))).getComponent(icomp)).
		    getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}


/*
 * Class for a netCDF variable comprising a Real component of 
 * a RealTuple of a Field's Tuple range.
 */
class
TupleRealVar
    extends RealVar
{
    /**
     * The index of the component in the Tuple range containing
     * the variable.
     */
    protected final int	tupleComp;


    /**
     * The index of the component in the RealTuple corresponding
     * to the variable.
     */
    protected final int	realTupleComp;


    /**
     * Construct from broken-out information.
     */
    protected
    TupleRealVar(String name, Dimension[] dims, Unit unit, int tupleComp,
	    int realTupleComp, Field field)
	throws BadFormException
    {
	super(name, dims, unit, field);
	this.tupleComp = tupleComp;
	this.realTupleComp = realTupleComp;
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return new Double(((Real)((RealTuple)((Tuple)field.
		getSample(visadIndex(indexes))).
		    getComponent(tupleComp)).getComponent(realTupleComp)).
		    getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
