package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DBoolean variables to the
 * {@link visad.data.in} context.
 */
public class BooleanVariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final SimpleSet[]	repSets;

    private BooleanVariableAdapter(DBoolean var, AttributeTable table)
	throws VisADException
    {
	realType = realType(var, table);
	repSets = new SimpleSet[] {new Integer1DSet(realType, 2)};
    }

    public static BooleanVariableAdapter booleanVariableAdapter (
	    DBoolean var, AttributeTable table)
	throws VisADException
    {
	return new BooleanVariableAdapter(var, table);
    }

    public MathType getMathType()
    {
	return realType;
    }

    public SimpleSet[] getRepresentationalSets()
	throws VisADException
    {
	return repSets;
    }

    public DataImpl data(DBoolean var)
    {
	return new Real(realType, var.getValue() ? 1 : 0);
    }
}
