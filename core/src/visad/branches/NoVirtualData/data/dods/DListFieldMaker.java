package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.BadFormException;

public class DListFieldMaker
    extends	FieldMaker
{
    private final Set			domain;
    private final AttributeTable	table;
    private final DataMakerFactory	factory;

    public static DListFieldMaker instance(
	    DList template, AttributeTable table, DataMakerFactory factory)
	throws BadFormException, VisADException
    {
	return new DListFieldMaker(template, table, factory);
    }

    private DListFieldMaker(
	    DList templateList, AttributeTable table, DataMakerFactory factory)
	throws VisADException
    {
	super(DListAdapter.functionType(templateList, table));
	domain = new Integer1DSet(templateList.getLength()));

	####
	this.table = table;
	this.factory = factory;

	####

	flatField = getFunctionType().getFlat();

	####

	VariableAdapter.primitiveVectorRange(
	    list.getPrimitiveVector(), table, factory));

	####

	VirtualField.instance(
	    domain = new Integer1DSet(templateList.getLength()),
	    primitiveVectorRange(
		templateList.getPrimitiveVector(), table, factory));
    }

    public Data data(DList list)
    {
	return
	    flatField
		? new FlatField(
		    getFunctionType()
	    new Field

	####

	return
	    VirtualField.instance(
		domain,
		primitiveVectorRange(list.getPrimitiveVector(), table, factory))
	    .getData();

	####

	DListAdapter.instance(domain, 

	####

	return
	    DListAdapter.instance(list, table, factory)
	    .getVirtualData().getData();

	####

	list.getPrimitiveVector()
    }
}
