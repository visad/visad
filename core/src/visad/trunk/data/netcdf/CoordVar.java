package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Linear1DSet;
import visad.Unit;
import visad.data.BadFormException;




/*
 * Class for coordinate variables.
 */
class
CoordVar
    extends Var
{
    /**
     * The linear, sampling domain set.
     */
    protected final Linear1DSet	set;


    /**
     * Construct.
     */
    protected
    CoordVar(String name, Dimension dim, Unit unit, Linear1DSet set)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, unit);
	this.set = set;
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
	    return new Float(set.indexToValue(new int[] {index})[0][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }

    /**
     * Indicate whether or not this co-ordinate variable is the same as
     * another co-ordinate variable.
     */
    public boolean
    equals(CoordVar that)
    {
	return getName().equals(that.getName()) && 
		getRank() == that.getRank() &&
		getLengths()[0] == that.getLengths()[0] &&
		unit.equals(that.unit) &&
		set.equals(that.set);
    }
}
