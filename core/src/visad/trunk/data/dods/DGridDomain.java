package visad.data.dods;

import dods.dap.*;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.*;

public class DGridDomain
    extends	Domain
{
    public DGridDomain(DGrid grid, AttributeTable table)
	throws BadFormException, VisADException
    {
	super(virtualSet(grid, table));
    }

    protected static VirtualSet virtualSet(DGrid grid, AttributeTable table)
	throws BadFormException, VisADException
    {
	try
	{
	    DArray	array = (DArray)grid.getVar(0);
	    int		rank = array.numDimensions();
	    Gridded1DSet[]	sets = new Gridded1DSet[rank];
	    for (int i = 1; i <= rank; ++i)
	    {
		PrimitiveVector	vector =
		    ((DArray)grid.getVar(i)).getPrimitiveVector();
		BaseType		template = vector.getTemplate();
		sets[rank-i] =	// reverse dimension order
		    SetMaker.instance(
			template, DODSUtil.getAttributeTable(table, template))
		    .set(vector);
	    }
	    return
		sets.length == 1
		    ? new VirtualSet(sets[0])
		    : new VirtualSet(new ProductSet(sets));
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"DGridDomain.virtualSet(...): DGrid inquiry failure: " + e);
	}
    }
}
