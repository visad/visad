package visad.data.netcdf.in;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


public interface
NestedData
{
    /**
     * Return the VisAD data object corresponding to this netCDF data object
     * at a point in the outermost dimension.
     *
     * @param index		The position in the outermost dimension.
     * @precondition		<code>index >= 0 && index <</code> <the number
     *				of points in the outermost dimension>
     * @return			The VisAD data object corresponding to the
     *				netCDF data object.
     * @throws NestedException	Data object is not in the range of a nested
     *				Field
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData(int index)
	throws NestedException, VisADException, IOException;


    /**
     * Return a proxy for the VisAD data object corresponding to this 
     * netCDF data object at a point in the outermost dimension.
     *
     * @param index		The position in the outermost dimension.
     * @precondition		<code>index >= 0 && index <</code> <the number
     *				of points in the outermost dimension>
     * @return			A proxy for the VisAD data object corresponding
     *				to the netCDF data object.
     * @throws NestedException	Data object is not in the range of a nested
     *				Field
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getProxy(int index)
	throws NestedException, VisADException, IOException;


    /*
     * Gets the VisAD MathType of the "inner" data object.
     */
    public MathType
    getInnerMathType()
	throws VisADException;
}
