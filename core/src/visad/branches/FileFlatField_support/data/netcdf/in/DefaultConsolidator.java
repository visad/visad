/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DefaultConsolidator.java,v 1.2.2.1 2000-06-07 20:38:00 steve Exp $
 */

package visad.data.netcdf.in;

import java.rmi.RemoteException;
import java.util.Vector;
import java.io.IOException;
import visad.Data;
import visad.DataImpl;
import visad.FunctionType;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.ScalarType;
import visad.Text;
import visad.TextType;
import visad.Tuple;
import visad.TupleType;
import visad.TypeException;
import visad.VisADException;


/**
 * Supports default consolidation of VisAD data items.
 */
public class
DefaultConsolidator
    extends	Consolidator
{
    /**
     * The data item collection.
     */
    private final VirtualTuple	topTuple = new VirtualTuple();


    /**
     * Adds a data item.
     *
     * @param data		The virtual, VisAD data item to be added.
     * @throws TypeException	Unknown data item type.
     */
    public void
    add(VirtualData data)
	throws TypeException, VisADException
    {
	topTuple.merge(data);
    }


    /**
     * Gets the VisAD MathType of the consolidated data items.
     *
     * @return			The VisAD MathType of the consolidated data
     *				items or <code>null</code> if no data items.
     */
    public MathType
    getType()
	throws VisADException
    {
	return topTuple.getType();
    }


    /**
     * Gets the consolidated, VisAD data object.
     *
     * @return			The consolidated, VisAD data object or
     *				<code>null</code> if there is no data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     * @throws IOException	I/O failure.
     */
    public DataImpl
    getData()
	throws VisADException, RemoteException, IOException
    {
	return topTuple.getData();
    }


    /**
     * Clears the consolidated, VisAD data object.
     */
    public void
    clear()
    {
	topTuple.clear();
    }


    /**
     * Gets a proxy for the consolidated, VisAD data object.
     *
     * @return			A proxy for the consolidated, VisAD data object.
     */
    public DataImpl
    getProxy()
    {
	return null;	// TODO
    }


    /**
     * Adds a data object to the top-level tuple.
     *
     * @param data		The data object to be added.
     */
    protected void addToTuple(VirtualData data)
    {
	topTuple.add(data);
    }
}
