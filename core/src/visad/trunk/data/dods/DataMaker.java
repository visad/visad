package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;

/**
 * Instances are immutable;
 */
public abstract class DataMaker
{
    /**
     * Protected because abstract.
     */
    protected DataMaker()
    {}

    public abstract MathType getMathType();

    public Data data(BaseType baseType)
	throws VisADException, RemoteException
    {
	Data	data;
	if (baseType instanceof DString)
	    data = data((DString)baseType);
	else if (baseType instanceof DBoolean)
	    data = data((DBoolean)baseType);
	else if (baseType instanceof DByte)
	    data = data((DByte)baseType);
	else if (baseType instanceof DInt16)
	    data = data((DInt16)baseType);
	else if (baseType instanceof DInt32)
	    data = data((DInt32)baseType);
	else if (baseType instanceof DFloat32)
	    data = data((DFloat32)baseType);
	else if (baseType instanceof DFloat64)
	    data = data((DFloat64)baseType);
	else if (baseType instanceof DStructure)
	    data = data((DStructure)baseType);
	else if (baseType instanceof DList)
	    data = data((DList)baseType);
	else if (baseType instanceof DArray)
	    data = data((DArray)baseType);
	else if (baseType instanceof DGrid)
	    data = data((DGrid)baseType);
	else if (baseType instanceof DSequence)
	    data = data((DSequence)baseType);
	else
	    throw new BadFormException(
		getClass().getName() + ".data(BaseType): " +
		"Unknown DODS type: " + baseType.getTypeName());
	return data;
    }

    public Data data(DString var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DString): " +
	    "Can't make VisAD data object");
    }

    public Data data(DBoolean var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DBoolean): " +
	    "Can't make VisAD data object");
    }

    public Data data(DByte var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DByte): " +
	    "Can't make VisAD data object");
    }

    public Data data(DInt16 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DInt16): " +
	    "Can't make VisAD data object");
    }

    public Data data(DInt32 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DInt32): " +
	    "Can't make VisAD data object");
    }

    public Data data(DFloat32 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DFloat32): " +
	    "Can't make VisAD data object");
    }

    public Data data(DFloat64 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DFloat64): " +
	    "Can't make VisAD data object");
    }

    public Data data(DStructure var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DStructure): " +
	    "Can't make VisAD data object from DODS DStructure");
    }

    public Data data(DList var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DList): " +
	    "Can't make VisAD data object from DODS DList");
    }

    public Data data(DArray var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DArray): " +
	    "Can't make VisAD data object from DODS DArray");
    }

    public Data data(DGrid var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DGrid): " +
	    "Can't make VisAD data object from DODS DGrid");
    }

    public Data data(DSequence var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DSequence): " +
	    "Can't make VisAD data object from DODS DSequence");
    }

    protected static MathType mathType(DataMaker[] dataMakers)
	throws VisADException, RemoteException
    {
	MathType[]	mathTypes = new MathType[dataMakers.length];
	for (int i = 0; i < mathTypes.length; ++i)
	    mathTypes[i] = dataMakers[i].getMathType();
	return DODSUtil.mathType(mathTypes);
    }
}
