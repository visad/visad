package visad.data.netcdf;


/**
 * Interface for visiting the points of an array in order.
 */
public interface
ArrayVisitor
{
    /**
     * Visit a point in an array.
     */
    public void
    visit(int[] indexes);
}
