package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.BadFormException;

public abstract class FieldMaker
    extends	DataMaker
{
    private final FunctionType	functionType;

    protected FieldMaker(FunctionType functionType)
    {
	this.functionType = functionType ;
    }

    private FieldMaker(DList list, AttributeTable table)
	throws VisADException
    {
	functionType = DListAdapter.functionType(list, table);
    }

    private FieldMaker(DArray array, AttributeTable table)
	throws BadFormException, VisADException
    {
	functionType = DArrayAdapter.functionType(array, table);
    }

    private FieldMaker(DGrid grid, AttributeTable table)
	throws BadFormException, VisADException
    {
	functionType = DGridAdapter.functionType(grid, table);
    }

    private FieldMaker(DSequence sequence, AttributeTable table)
	throws BadFormException, VisADException
    {
	functionType = DSequenceAdapter.functionType(sequence, table);
    }

    /* TODO
    public static FieldMaker instance(BaseType variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	FieldMaker	fieldMaker;
	if (variable instanceof DList)
	    fieldMaker = new FieldMaker((DList)variable, table);
	else if (variable instanceof DArray)
	    fieldMaker = new FieldMaker((DArray)variable, table);
	else if (variable instanceof DGrid)
	    fieldMaker = new FieldMaker((DGrid)variable, table);
	else if (variable instanceof DSequence)
	    fieldMaker = new FieldMaker((DSequence)variable, table);
	else
	    throw new BadFormException(
		"FieldMaker.instance(BaseType,AttributeTable): " +
		"Invalid DODS type: " + variable.getTypeName());
	return fieldMaker;
    }
    */

    public final MathType getMathType()
    {
	return functionType;
    }

    public final FunctionType getFunctionType()
    {
	return functionType;
    }
}
