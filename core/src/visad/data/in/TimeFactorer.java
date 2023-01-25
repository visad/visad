/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

$Id: TimeFactorer.java,v 1.12 2009-03-02 23:35:49 curtis Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * Converts incoming VisAD Fields whose outermost dimension is time and
 * can be factored out into a field-of-fields.  Sends the field-of-fields to the
 * downstream data sink.  Passes all other VisAD data objects to the downstream
 * data sink unchanged.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class TimeFactorer
    extends	DataInputFilter
{
    /**
     * Constructs from a upstream data source.
     *
     * @param source		The upstream data source.  May not be
     *				<code>null</code>.
     * @throws VisADException	The upstream data source is <code>null</code>.
     */
    public TimeFactorer(DataInputStream source)
	throws VisADException
    {
	super(source);
    }

    /**
     * Returns the next VisAD data object in the input stream.	If the next
     * object is a field and the field's outermost dimension is time and can be
     * factored out, then the field will be converted into a field-of-fields
     * (with time as the single dimension of the outermost field) before being
     * returned.  Returns <code>null</code> if there are no more objects.
     *
     * @return			A VisAD data object or <code>null</code> if 
     *				there are no more such objects.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized DataImpl readData()
	throws VisADException, RemoteException
    {
	DataImpl	data = getSource().readData();
	if (data instanceof FieldImpl)
	{
	    FieldImpl	field = (FieldImpl)data;
	    RealTupleType	domainType =
		((FunctionType)field.getType()).getDomain();
	    int		dimensionCount = domainType.getDimension();
	    if (dimensionCount > 1)
	    {
		RealType	outerDimensionType = (RealType)
		    domainType.getComponent(dimensionCount - 1);
		if (RealType.Time.equalsExceptNameButUnits(
			outerDimensionType) ||
		      RealType.TimeInterval.equalsExceptNameButUnits(
			outerDimensionType))
		{
		    Set		domain = field.getDomainSet();
		    if (domain instanceof ProductSet ||
			domain instanceof LinearSet)
		    {
			try
			{
			    field = (FieldImpl)
				field.domainFactor(outerDimensionType);
			}
			catch (DomainException e)
			{}	// the domain of the field isn't factorable
		    }
		}
	    }
	    data = field;
	}
	return data;
    }
}
