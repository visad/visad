package visad.data.in;

import java.rmi.RemoteException;
import java.util.*;
import visad.*;

public class FieldRange
    extends Range
{
    /**
     * @supplierCardinality 1..*
     * @label values
     * @directed 
     */
    private VirtualField[]	fields;

    private FunctionType	functionType;

    /**
     * Doesn't copy the array.
     * @param fields		The {@link VirtualField}-s that constitute the
     *				range.  There must be one virtual field for each
     *				point of the outer domain.  All the virtual
     *				fields must all be identical except for their
     *				range values.
     */
    public FieldRange(VirtualField[] fields)
	throws VisADException
    {
	super(fields.length);
	VirtualField	template = fields[0];
	functionType = template.getFunctionType();
	Domain		domain = template.getDomain();
	for (int i = 1; i < fields.length; ++i)
	{
	    VirtualField	field = fields[i];
	    if (!functionType.equals(field.getFunctionType()))
		throw new VisADException(
		    getClass().getName() + ".<init>(VirtualField[]): " +
		    "Non-identical function types");
	    if (!domain.equals(field.getDomain()))
		throw new VisADException(
		    getClass().getName() + ".<init>(VirtualField[]): " +
		    "Non-identical domains");
	}
	this.fields = fields;
    }

    public MathType getMathType()
    {
	return functionType;
    }

    public void add(Range range)
	throws NotMergeableException, VisADException
    {
	if (!(range instanceof FieldRange))
	    throw new NotMergeableException(
		getClass().getName() + ".add(Range): Range isn't a FieldRange");
	add((FieldRange)range);
    }

    public synchronized void add(FieldRange that)
	throws NotMergeableException, VisADException
    {
	if (!fields[0].getDomain().equals(that.fields[0].getDomain()))
	    throw new NotMergeableException(
		getClass().getName() + ".add(FieldRange): Unequal domains");
	VirtualField[]	thoseFields = that.fields;
	if (fields.length != thoseFields.length)
	    throw new NotMergeableException(
		getClass().getName() + ".add(FieldRange): " +
		"Unequal outer domain size");
	for (int i = 0; i < fields.length; ++i)
	    fields[i].add(thoseFields[i]);
    }

    public Data getDatum(int index)
	throws VisADException, RemoteException
    {
	return fields[index].getData();
    }

    /**
     * Doesn't copy range values.
     */
    public Range getSubRange(int offset, int length)
        throws VisADException, IndexOutOfBoundsException
    {
	Range	subRange;
	if (length == 1)
	{
	    subRange = fields[offset].getRange();
	}
	else
	{
	    VirtualField[]	subFields = new VirtualField[length];
	    System.arraycopy(fields, offset, subFields, 0, length);
	    subRange = new FieldRange(subFields);
	}
	return subRange;
    }
}
