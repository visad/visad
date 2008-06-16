/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;

/**
 * Provides support for accessing a DODS primitive vector as a VisAD {@link
 * visad.data.FileFlatField}.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class VectorAccessor
    extends	FileAccessor
{
    private final FunctionType		funcType;
    private final VectorAdapter		vectorAdapter;
    private final SampledSet		domain;
    private final PrimitiveVector	vector;

    /**
     * Constructs from a function-type, a vector adapter, a domain and a DODS
     * primitive vector.
     *
     * @param funcType		The function-type for the FlatField.
     * @param vectorAdapter	The vector adapter corresponding to the DODS
     *				primitive vector.
     * @param domain		The domain for the FileFlatField.
     * @param vector		The DODS primitive vector.
     */
    public VectorAccessor(
	FunctionType funcType,
	VectorAdapter vectorAdapter,
	SampledSet domain,
	PrimitiveVector vector)
    {
	this.funcType = funcType;
	this.vectorAdapter = vectorAdapter;
	this.domain = domain;
	this.vector = vector;
    }

    /*
     * Returns the VisAD {@link FunctionType} of this instance.
     *
     * @return			The FunctionType of this instance.
     */
    public FunctionType getFunctionType()
    {
	return funcType;
    }

    /**
     * Returns a VisAD {@link FlatField} corresponding to this instance.
     *
     * @return			A FlatField corresponding to the
     *				construction arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public FlatField getFlatField()
	throws VisADException, RemoteException
    {
	FlatField	field =
	    new FlatField(
		funcType,
		domain,
		(CoordinateSystem[])null,
		vectorAdapter.getRepresentationalSets(false),
		(Unit[])null);
	vectorAdapter.setField(vector, field, false);
	return field;
    }

    /**
     * Throws a VisADError.
     *
     * @param values		Some values.
     * @param template		A template FlatField.
     * @param fileLocation	An array of positional parameters.
     * @throws VisADError	This method does nothing and should not
     *				have been invoked.  Always thrown.
     */
    public void writeFlatField(
	double[][] values, FlatField template, int[] fileLocation)
    {
	throw new VisADError(
	    getClass().getName() + ".writeFlatField(...): " +
	    "Unimplemented method");
    }

    /**
     * Throws a VisADError.
     *
     * @param template		A template FlatField.
     * @param fileLocation	An array of positional parameters.
     * @return			<code>null</code>.
     * @throws VisADError	This method does nothing and should not
     *				have been invoked.  Always thrown.
     */
    public double[][] readFlatField(FlatField template, int[] fileLocation)
    {
	throw new VisADError(
	    getClass().getName() + ".readFlatField(...): " +
	    "Unimplemented method");
    }

    /**
     * Throws a VisADError.
     *
     * @param fileLocation	An array of positional parameters.
     * @param range		The range of a FlatField.
     * @throws VisADError	This method does nothing and should not
     *				have been invoked.  Always thrown.
     */
    public void writeFile(int[] fileLocation, Data range)
    {
	throw new VisADError(
	    getClass().getName() + ".writeFile(...): " +
	    "Unimplemented method");
    }
}
