package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DString variables to the
 * {@link visad.data.in} context.
 */
public class StringVariableAdapter
    extends	VariableAdapter
{
    private final TextType	textType;

    private StringVariableAdapter(DString var, AttributeTable table)
	throws VisADException
    {
	textType = TextType.getTextType(scalarName(var.getName()));
    }

    public static StringVariableAdapter stringVariableAdapter(
	    DString var, AttributeTable table)
	throws VisADException
    {
	return new StringVariableAdapter(var, table);
    }

    public MathType getMathType()
    {
	return textType;
    }

    public DataImpl data(DString var)
	throws VisADException
    {
	return new Text(textType, var.getValue());
    }
}
