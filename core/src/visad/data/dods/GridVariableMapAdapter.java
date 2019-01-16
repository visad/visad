/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.dods;

import dods.dap.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.rmi.RemoteException;
import visad.*;

/**
 * Provides support for adapting the map vectors of a DODS {@link DGrid}
 * variable to the {@link visad.data.in} context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class GridVariableMapAdapter
    extends	VariableAdapter
{
    private final VectorAdapter		vectorAdapter;
    private static final Map		setMap = new WeakHashMap();

    private GridVariableMapAdapter(
	    DArray array,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	vectorAdapter = factory.vectorAdapter(array.getPrimitiveVector(), das);
    }

    public static GridVariableMapAdapter gridVariableMapAdapter(
	    DArray array, DAS das, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	if (array.numDimensions() != 1)
	    throw new VisADException(
	"visad.data.dods.GridVariableMapAdapter.gridVariableMapAdapter(...): " +
		"Array not one-dimensional");
	return new GridVariableMapAdapter(array, das, factory);
    }

    public MathType getMathType()
    {
	return vectorAdapter.getMathType();
    }

    /**
     * Returns a VisAD data object corresponding to a map vector of a DODS grid.
     *
     * @param array	An array that contains data and is compatible with
     *			the array used during construction.
     * @param copy	If true, then data values are copied.
     * @return		The VisAD data object corresponding to the adapted
     *			map vector.  The (super)class of the returned object
     *			is {@link GriddedSet}.
     */
    public DataImpl data(DArray array, boolean copy)
	throws VisADException, RemoteException
    {
	GriddedSet	newSet =
	    vectorAdapter.griddedSet(array.getPrimitiveVector());
	WeakReference	ref = (WeakReference)setMap.get(newSet);
	if (ref == null)
	{
	    setMap.put(newSet, new WeakReference(newSet));
	}
	else
	{
	    GriddedSet	oldSet = (GriddedSet)ref.get();
	    if (oldSet == null)
		setMap.put(newSet, new WeakReference(newSet));
	    else
		newSet = oldSet;
	}
	return newSet;
    }
}
