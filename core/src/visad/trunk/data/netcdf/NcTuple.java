package visad.data.netcdf;


import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


/**
 * Class for adapting a tuple of netCDF data objects to a VisAD Tuple.
 */
class
NcTuple
    extends	NcData
{
    /**
     * The netCDF data objects.
     */
    protected final NcData[]	ncDatas;


    /**
     * Construct from an array of netCDF data objects.
     */
    NcTuple(NcData[] ncDatas)
	throws VisADException
    {
	this.ncDatas = ncDatas;

	int		numComponents = ncDatas.length;
	MathType[]	mathTypes = new MathType[numComponents];

	for (int i = 0; i < numComponents; ++i)
	    mathTypes[i] = ncDatas[i].getMathType();

	initialize(new TupleType(mathTypes));
    }


    /**
     * Return the VisAD data object corresponding to this netCDF data object.
     */
    DataImpl
    getData()
	throws VisADException, IOException
    {
	DataImpl[]	datas = new DataImpl[ncDatas.length];

	for (int i = 0; i < ncDatas.length; ++i)
	    datas[i] = ncDatas[i].getData();

	return new Tuple(datas);
    }
}
