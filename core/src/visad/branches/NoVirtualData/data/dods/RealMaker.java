package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.BadFormException;

public class RealMaker
    extends	DataMaker
{
    private final RealType	realType;
    private final Valuator	valuator;

    private RealMaker(RealType realType, AttributeTable table)
	throws BadFormException
    {
	this.realType = realType;
	valuator = Valuator.instance(table);
    }

    public static RealMaker instance(BaseType variable, AttributeTable table)
	throws BadFormException
    {
	return new RealMaker(DODSUtil.getRealType(variable, table), table);
    }

    public MathType getMathType()
    {
	return realType;
    }

    public Data data(DBoolean var)
	throws VisADException
    {
	return new Real(realType, var.getValue() ? 1 : 0);
    }

    public Data data(DByte var)
	throws VisADException
    {
	return new Real(realType, valuator.process(var.getValue()));
    }

    public Data data(DInt16 var)
	throws VisADException
    {
	return new Real(realType, valuator.process(var.getValue()));
    }

    public Data data(DInt32 var)
	throws VisADException
    {
	return new Real(realType, valuator.process(var.getValue()));
    }

    public Data data(DFloat32 var)
	throws VisADException
    {
	return new Real(realType, valuator.process(var.getValue()));
    }

    public Data data(DFloat64 var)
	throws VisADException
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
