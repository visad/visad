package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;

/**
 * Always behaves consonant with a VisAD RealTuple.
 */
public class RealTupleMaker
    extends	DataMaker
{
    private final RealTupleType	realTupleType;
    private final RealMaker[]	realMakers;
    private final Real[]	reals;

    public static RealTupleMaker instance(RealMaker[] realMakers)
	throws VisADException, RemoteException
    {
	return new RealTupleMaker(realMakers);
    }

    /**
     * Uses the actual array argument (i.e. doesn't copy or clone it).
     */
    private RealTupleMaker(RealMaker[] realMakers)
	throws VisADException, RemoteException
    {
	realTupleType = (RealTupleType)mathType(realMakers);
	this.realMakers = realMakers;
	reals = new Real[realMakers.length];
    }

    public MathType getMathType()
    {
	return realTupleType;
    }

    public Data data(DStructure var)
	throws VisADException, RemoteException
    {
	try
	{
	    for (int i = 0; i < reals.length; ++i)
		reals[i] = (Real)realMakers[i].data(var.getVar(i));
	    return new RealTuple(realTupleType, reals, (CoordinateSystem)null);
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".data(DStructure): " +
		"DStructure inquiry failure: " + e);
	}
    }
}
