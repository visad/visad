package visad.data.netcdf;


/**
 * Class for traversing the points of an array in order (i.e. an
 * odometer class).
 */
public abstract class
ArrayTraverser
{
    /**
     * The rank of the array.
     */
    protected final int		rank;

    /**
     * The shape of the array.
     */
    protected final int[]	shape;

    /**
     * The total number of points in the array.
     */
    protected final int		numPoints;


    /**
     * Construct.
     */
    public
    ArrayTraverser(int[] shape)
    {
	rank = shape.length;
	this.shape = shape;

	int	npts;

	if (rank == 0)
	    npts = 0;
	else
	{
	    npts = shape[0];
	    for (int i = 1; i < rank; ++i)
		npts *= shape[i];
	}

	numPoints = npts;
    }


    /**
     * Return the total number of points in the array.
     */
    public int
    numPoints()
    {
	return numPoints;
    }


    /**
     * Traverse the points of the array in order.
     */
    public void
    traverse(ArrayVisitor visitor)
    {
	int[]	indexes = new int[rank];

	for (int i = 0; i < rank; ++i)
	    indexes[i] = 0;

	recurse(0, indexes, visitor);
    }


    /**
     * Recursively traverse the points of the array.
     */
    protected void
    recurse(int idim, int[] indexes, ArrayVisitor visitor)
    {
	if (idim == rank-1)
	    visitor.visit(indexes);
	else
	{
	    for (int i = 0; i < shape[idim]; ++i)
	    {
		recurse(idim+1, indexes, visitor);
		indexes[idim]++;
	    }
	    indexes[idim] = 0;
	}
    }
}
