package visad.data.dods;

import dods.dap.*;
import dods.dap.Server.InvalidParameterException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for adapting the map vectors of a DODS {@link DGrid}
 * variable to the {@link visad.data.in} context.
 */
public class GridVariableMapAdapter
    extends	VariableAdapter
{
    private final VectorAdapter		vectorAdapter;
    private static final Map		setMap = new WeakHashMap();

    private GridVariableMapAdapter(
	    DArray array,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	vectorAdapter =
	    factory.vectorAdapter(array.getPrimitiveVector(), table);
    }

    public static GridVariableMapAdapter gridVariableMapAdapter(
	    DArray array, AttributeTable table, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	if (array.numDimensions() != 1)
	    throw new VisADException(
	"visad.data.dods.GridVariableMapAdapter.gridVariableMapAdapter(...): " +
		"Array not one-dimensional");
	return new GridVariableMapAdapter(array, table, factory);
    }

    public MathType getMathType()
    {
	return vectorAdapter.getMathType();
    }

    /**
     * @return		The VisAD data object corresponding to the adapted
     *			map vector.
     */
    public DataImpl data(DArray array)
	throws VisADException, RemoteException
    {
	SampledSet	newSet =
	    vectorAdapter.griddedSet(array.getPrimitiveVector());
	WeakReference	ref = (WeakReference)setMap.get(newSet);
	if (ref == null)
	{
	    setMap.put(newSet, new WeakReference(newSet));
	}
	else
	{
	    SampledSet	oldSet = (SampledSet)ref.get();
	    if (oldSet == null)
		setMap.put(newSet, new WeakReference(newSet));
	    else
		newSet = oldSet;
	}
	return newSet;
    }
}
