package visad.data.in;

import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/**
 * An instance can be mutable or immutable -- depending on the concrete
 * subclass of the instance.  The default is to be immutable.
 */
public abstract class VirtualTuple
    extends VirtualData
{
    protected final List	components;
    protected boolean		allReals = true;
    private MathType		mathType = null;

    /**
     * @label components
     * @supplierCardinality 1..*
     * @directed 
     */
    private VirtualData lnkVirtualData;

    /**
     * Uses the actual list argument.
     */
    protected VirtualTuple(List components)
	throws VisADException
    {
	this.components = components;
	allReals = isAllReals(components);
	setMathType();
    }

    /**
     * If only one component, then returns the MathType of the component;
     * otherwise, returns a {@link RealTupleType} or a {@link TupleType}.
     */
    public MathType getMathType()
    {
	return mathType;
    }

    /**
     */
    public void add(VirtualData other)
	throws NotMergeableException, VisADException
    {
	throw new NotMergeableException(
	    getClass().getName() + ".add(VirtualData): " +
	    "Can't add a VirtualData to this instance");
    }

    public VirtualData getComponent(int index)
    {
	return (VirtualData)components.get(index);
    }

    /**
     * Returns the single component if appropriate.
     */
    public synchronized Data getData()
	throws VisADException, RemoteException
    {
	Data[]	datums = new Data[components.size()];
	for (int i = 0; i < datums.length; ++i)
	    datums[i] = getComponent(i).getData();
	return newData(datums);
    }

    protected static List newList(VirtualData[] datums)
    {
	ArrayList	list = new ArrayList(datums.length);
	for (int i = 0; i < datums.length; ++i)
	    list.add(datums[i]);
	return list;
    }

    protected static boolean isAllReals(List datums)
    {
	boolean	allReals = true;
	for (Iterator iter = datums.iterator(); iter.hasNext() && allReals; )
	    allReals &= ((Range)iter.next()).getMathType() instanceof RealType;
	return allReals;
    }

    /**
     * Doesn't clone data.
     */
    protected Data newData(Data[] datums)
	throws VisADException, RemoteException
    {
	Data	data;
	if (datums.length == 0)
	{
	    data = null;
	}
	else if (datums.length == 1)
	{
	    data = datums[0];
	}
	else if (allReals)
	{
	    data =
		new RealTuple(
		    (RealTupleType)mathType,
		    (Real[])datums,
		    (CoordinateSystem)null);
	}
	else
	{
	    data = new Tuple(datums, /*copy=*/false);
	}
	return data;
    }

    /**
     * Sets the {@link MathType} of this virtual data according to the actual
     * components in the tuple.  If no components, then the MathType is set to
     * <code>null</code>; otherwise; if only one component, then the MathType is
     * set to that of the component; otherwise, the MathType is set to a {@link
     * RealTupleType} or a {@link TupleType}.
     */
    protected void setMathType()
	throws VisADException
    {
	int	count = components.size();

	if (count == 0)
	{
	    mathType = null;
	}
	else if (count == 1)
	{
	    mathType = getComponent(0).getMathType();
	}
	else if (allReals)
	{
	    RealType[]	mathTypes = new RealType[count];
	    for (int i = 0; i < mathTypes.length; ++i)
		mathTypes[i] = (RealType)getComponent(i).getMathType();
	    mathType = new RealTupleType(mathTypes);
	}
	else
	{
	    MathType[]	mathTypes = new MathType[count];
	    for (int i = 0; i < mathTypes.length; ++i)
		mathTypes[i] = getComponent(i).getMathType();
	    mathType = new TupleType(mathTypes);
	}
    }
}
