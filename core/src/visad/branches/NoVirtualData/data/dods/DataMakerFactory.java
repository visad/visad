package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.VisADException;

public class DataMakerFactory
{
    private static final DataMakerFactory	instance =
	new DataMakerFactory();

    protected DataMakerFactory()
    {}

    public static DataMakerFactory instance()
    {
	return instance;
    }

    public static DataMaker dataMaker(BaseType variable, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	DataMaker	maker;
	if (variable instanceof DString)
	    maker = TextMaker.instance((DString)variable);
	else if (variable instanceof DBoolean)
	    maker = RealMaker.instance((DBoolean)variable, table);
	else if (variable instanceof DByte)
	    maker = RealMaker.instance((DByte)variable, table);
	else if (variable instanceof DInt16)
	    maker = RealMaker.instance((DInt16)variable, table);
	else if (variable instanceof DInt32)
	    maker = RealMaker.instance((DInt32)variable, table);
	else if (variable instanceof DFloat32)
	    maker = RealMaker.instance((DFloat32)variable, table);
	else if (variable instanceof DFloat64)
	    maker = RealMaker.instance((DFloat64)variable, table);
	else if (variable instanceof DStructure)
	    maker = dataMaker((DStructure)variable, table);
	/* TODO
	else if (variable instanceof DList)
	    maker = FieldMaker.instance((DList)variable, table);
	else if (variable instanceof DSequence)
	    maker = FieldMaker.instance((DSequence)variable, table);
	else if (variable instanceof DArray)
	    maker = FieldMaker.instance((DArray)variable, table);
	else if (variable instanceof DGrid)
	    maker = FieldMaker.instance((DGrid)variable, table);
	*/
	else 
	    throw new BadFormException(
		"DataMaker.dataMaker(BaseType,AttributeTable): " +
		"Unknown DODS type: " + variable.getTypeName());
	return maker;
    }

    protected static DataMaker dataMaker(
	    DStructure dStructure, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	int		count = dStructure.elementCount();
	DataMaker	dataMaker;
	try
	{
	    if (count == 1)
	    {
		BaseType	variable = dStructure.getVar(0);
		dataMaker = 
		    dataMaker(
			variable, DODSUtil.getAttributeTable(table, variable));
	    }
	    else
	    {
		DataMaker[]	dataMakers = new DataMaker[count];
		MathType[]	mathTypes = new MathType[count];
		boolean		allReals = true;
		for (int i = 0; i < count; ++i)
		{
		    BaseType	var = dStructure.getVar(i);
		    dataMakers[i] = 
			dataMaker(var, DODSUtil.getAttributeTable(table, var));
		    mathTypes[i] = dataMakers[i].getMathType();
		    allReals &= mathTypes[i] instanceof RealType;
		}
		if (allReals)
		{
		    RealMaker[]	realMakers = new RealMaker[dataMakers.length];
		    for (int i = 0; i < realMakers.length; ++i)
			realMakers[i] = (RealMaker)dataMakers[i];
		    dataMaker = RealTupleMaker.instance(realMakers);
		}
		else
		{
		    dataMaker = TupleMaker.instance(dataMakers);
		}
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"DataMakerFactory.dataMaker(...): No such variable: " + e);
	}
	return dataMaker;
    }
}
