package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;

/**
 * Always behaves consonant with a VisAD Tuple.
 */
public class TupleMaker
    extends	DataMaker
{
    private final TupleType	tupleType;
    private final DataMaker[]	dataMakers;
    private final Data[]	datums;

    public static TupleMaker instance(DataMaker[] dataMakers)
	throws VisADException, RemoteException
    {
	return new TupleMaker(dataMakers);
    }

    /**
     * Uses the actual array argument (i.e. doesn't copy or clone it).
     */
    private TupleMaker(DataMaker[] dataMakers)
	throws VisADException, RemoteException
    {
	tupleType = (TupleType)mathType(dataMakers);
	this.dataMakers = dataMakers;
	datums = new Data[dataMakers.length];
    }

    public MathType getMathType()
    {
	return tupleType;
    }

    public Data data(DStructure dStructure)
	throws RemoteException, BadFormException, VisADException
    {
	try
	{
	    for (int i = 0; i < datums.length; ++i)
		datums[i] = dataMakers[i].data(dStructure.getVar(i));
	    return new Tuple(tupleType, datums, /*copy=*/false);
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".data(DStructure): " + 
		"DStructure inquiry failure: " + e);
	}
    }
}
