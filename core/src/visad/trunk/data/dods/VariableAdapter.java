package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;

/**
 * Instances are immutable;
 */
public abstract class VariableAdapter
    extends	Adapter
{
    private static final SimpleSet[]		defaultRepSets =
	new SimpleSet[] {null};

    public abstract MathType getMathType();

    public DataImpl data(BaseType baseType)
	throws VisADException, RemoteException
    {
	DataImpl	data;
	if (baseType instanceof DString)
	    data = data((DString)baseType);
	else if (baseType instanceof DBoolean)
	    data = data((DBoolean)baseType);
	else if (baseType instanceof DByte)
	    data = data((DByte)baseType);
	else if (baseType instanceof DUInt16)
	    data = data((DUInt16)baseType);
	else if (baseType instanceof DInt16)
	    data = data((DInt16)baseType);
	else if (baseType instanceof DUInt32)
	    data = data((DUInt32)baseType);
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
	else if (baseType instanceof DSequence)
	    data = data((DSequence)baseType);
	else if (baseType instanceof DArray)
	    data = data((DArray)baseType);
	else if (baseType instanceof DGrid)
	    data = data((DGrid)baseType);
	else
	    throw new BadFormException(
		getClass().getName() + ".data(BaseType): " +
		"Unknown DODS type: " + baseType.getTypeName());
	return data;
    }

    public DataImpl data(DString var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DString): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DBoolean var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DBoolean): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DByte var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DByte): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DUInt16 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DUInt16): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DInt16 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DInt16): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DUInt32 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DUInt32): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DInt32 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DInt32): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DFloat32 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DFloat32): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DFloat64 var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DFloat64): " +
	    "Can't make VisAD data object");
    }

    public DataImpl data(DStructure var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DStructure): " +
	    "Can't make VisAD data object from DODS DStructure");
    }

    public DataImpl data(DList var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DList): " +
	    "Can't make VisAD data object from DODS DList");
    }

    public DataImpl data(DArray var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DArray): " +
	    "Can't make VisAD data object from DODS DArray");
    }

    public DataImpl data(DGrid var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DGrid): " +
	    "Can't make VisAD data object from DODS DGrid");
    }

    public DataImpl data(DSequence var)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DSequence): " +
	    "Can't make VisAD data object from DODS DSequence");
    }

    /**
     * Override in appropriate subclasses.
     * @return			The VisAD sets used to represent the values
     *				of this data, where appropriate.  Will never
     *				be <code>null</code> -- though an individual
     *				elements might be (e.g. for TextType objects).
     */
    public SimpleSet[] getRepresentationalSets()
	throws VisADException
    {
	return defaultRepSets;
    }

    protected static MathType mathType(VariableAdapter[] adapters)
	throws VisADException, RemoteException
    {
	MathType[]	mathTypes = new MathType[adapters.length];
	for (int i = 0; i < mathTypes.length; ++i)
	    mathTypes[i] = adapters[i].getMathType();
	return mathType(mathTypes);
    }
}
