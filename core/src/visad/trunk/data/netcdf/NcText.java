package visad.data.netcdf;


import ucar.netcdf.Variable;
import visad.data.BadFormException;
import visad.MathType;
import visad.Text;
import visad.TextType;
import visad.VisADException;


/**
 * Adaptor/decorator class for netCDF textual variables.
 */
public class
NcText
    extends NcVar
{
    /**
     * Construct.
     */
    public
    NcText(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Return the VisAD math type of this variable.
     */
    public MathType
    getMathType()
	throws VisADException
    {
	return new TextType(name);
    }


    /**
     * Indicate if this variable is textual.
     */
    public boolean
    isText()
    {
	return true;
    }


    /**
     * Return the rank of the I/O vector.
     */
    protected int
    getIORank()
    {
	int	rank = var.getRank();

	return rank < 2
		? 1
		: rank-1;
    }


    /**
     * Create the buffer to hold the values.
     */
    protected Object
    createValues()
    {
	int	rank = var.getRank();
	int[]	shape = var.getLengths();
	int	npts;

        if (rank < 2)
            npts = 1;
        else
        {
            npts = shape[0];
            for (int i = 1; i < rank-1; ++i)
                npts *= shape[i];
        }

	return new Text[npts];
    }


    /**
     * Return an unraveler.
     */
    public Unraveler
    getUnraveler(Object values)
	throws BadFormException
    {
	return new TextUnraveler(values);
    }


    /**
     * Vet the values.
     */
    protected void
    vet(Object values)
    {
    }


    /**
     * Unpack the values.
     */
    protected void
    unpack(Object values)
    {
    }


    /**
     * Inner class for unraveling a nested, netCDF, textual array.
     */
    public class
    TextUnraveler
	extends Unraveler
    {
	/**
	 * The value buffer
	 */
	protected final Text[]	buf;


	/**
	 * Construct.
	 */
	protected
	TextUnraveler(Object values)
	    throws BadFormException
	{
	    super();

	    buf = (Text[])values;

	    int		rank = var.getRank();
	    int[]	shape = var.getLengths();

	    ioShape = new int[rank];

	    for (int i = 0; i < rank-1; ++i)
		ioShape[i] = 1;
	    ioShape[rank-1] = shape[rank-1];
	}


	/**
	 * Copy values into the value buffer.
	 */
	protected void
	copy(Object array)
	{
	    buf[next++] = new Text(new String((char[])array));
	}
    }
}
