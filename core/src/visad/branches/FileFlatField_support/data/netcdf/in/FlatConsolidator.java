/*
 * Copyright 2000, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FlatConsolidator.java,v 1.1.2.1 2000-06-07 20:38:01 steve Exp $
 */

package visad.data.netcdf.in;

import java.rmi.RemoteException;
import visad.*;


/**
 * Supports consolidation of virtual VisAD data objects with a strategy of
 * not consolidating the ranges of FlatField-s with identical domains.
 * This is useful in conjunction with FileFlatField implementations of 
 * FlatField-s in order to conserve memory.
 *
 * @author Steven R. Emmerson
 */
public class
FlatConsolidator
    extends	DefaultConsolidator
{
    /**
     * Adds a data item.  If the item is a virtual FlatField, then it will
     * not be merged.
     *
     * @param data		The virtual, VisAD data item to be added.
     * @throws TypeException	Unknown data item type.
     */
    public void
    add(VirtualData data)
	throws TypeException, VisADException
    {
	if (data instanceof VirtualField &&
	    ((VirtualField)data).getFunctionType().getFlat())
	{
	    addToTuple(data);
	}
	else
	{
	    super.add(data);
	}
    }
}
