package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.VirtualScalar;

public class TextMaker
    extends	DataMaker
{
    private final TextType	textType;

    private TextMaker(TextType textType)
    {
	this.textType = textType;
    }

    public static TextMaker instance(DString string)
    {
	return
	    new TextMaker(
		TextType.getTextType(
		    VirtualScalar.scalarName(string.getName())));
    }

    public MathType getMathType()
    {
	return textType;
    }

    public Data data(DString var)
	throws VisADException
    {
	return new Text(textType, var.getValue());
    }
}
