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
import visad.*;
import visad.data.*;
import visad.data.units.Parser;

/**
 * Provides support for adapting DODS objects to the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class Adapter
{
    private static final CacheStrategy	cacheStrategy = new CacheStrategy();

    /**
     * Returns the VisAD scalar-name equivalent to a DODS name.
     *
     * @param name		The DODS name.
     * @return			The VisAD scalar-name equivalent to the DODS
     *				name.
     * @see ScalarType
     */
    protected static String scalarName(String name)
    {
	return name
	    .replace('.', '-')
	    .replace(' ', '_')
	    .replace('(', '<')
	    .replace(')', '>');
    }

    /**
     * Indicates if a given VisAD {@link MathType} is "flat" (i.e. comprises a
     * {@link Real}, a {@link RealTuple}, or a {@link Tuple} of {@link Real}s
     * and {@link RealTuple}s.
     *
     * @param mathType		The VisAD mathtype to be investigated.
     * @return			<code>true</code> if and only if the given
     *				mathtype is "flat".
     */
    protected static boolean isFlat(MathType mathType)
    {
	return
	    mathType instanceof RealType ||
	    mathType instanceof RealTupleType ||
	    (mathType instanceof TupleType && ((TupleType)mathType).getFlat());
    }

    /**
     * Returns the VisAD {@link RealType} corresponding to a DODS variable.
     *
     * @param variable		The DODS variable.  Must be one for which a 
     *				RealType is possible.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The VisAD RealType corresponding to the
     *				variable and attribute table.
     */
    protected static RealType realType(BaseType variable, DAS das)
    {
	return realType(variable.getName(), attributeTable(das, variable));
    }

    /**
     * Returns the VisAD {@link RealType} corresponding to a DODS variable.
     *
     * @param variable		The DODS variable.  Must be one for which a 
     *				RealType is possible.
     * @param table		The DODS attribute table for the variable.
     *				May be <code>null</code>.
     * @return			The VisAD RealType corresponding to the
     *				variable and attribute table.
     */
    protected static RealType realType(BaseType variable, AttributeTable table)
    {
	return realType(variable.getName(), table);
    }

    /**
     * Returns the VisAD {@link RealType} corresponding to a name.
     *
     * @param name		The name.
     * @param das		The DODS DAS in which the information on the
     *				name is embedded.
     * @return			The VisAD RealType corresponding to the
     *				name and metadata.
     */
    protected static RealType realType(String name, DAS das)
    {
	return realType(name, attributeTable(das, name));
    }

    /**
     * Returns the VisAD {@link RealType} corresponding to a name.
     *
     * @param name		The name.
     * @param table		The DODS attribute table for the name.
     *				May be <code>null</code>.
     * @return			The VisAD RealType corresponding to the
     *				name and attribute table.
     */
    protected static RealType realType(String name, AttributeTable table)
    {
	Unit	unit;
	if (table == null)
	{
	    unit = null;
	}
	else
	{
	    Attribute	attr = table.getAttribute("units");
	    if (attr == null)
	    {
		attr = table.getAttribute("unit");
		if (attr == null)
		{
		    attr = table.getAttribute("UNITS");
		    if (attr == null)
			attr = table.getAttribute("UNIT");
		}
	    }
	    if (attr == null)
	    {
		unit = null;
	    }
	    else
	    {
		if (attr.getType() != Attribute.STRING)
		{
		    unit = null;
		}
		else
		{
		    String	unitSpec = attr.getValueAt(0);
		    /*
		     * Remove extraneous quotes.
		     */
		    if (unitSpec.startsWith("\"") && unitSpec.endsWith("\""))
			unitSpec = unitSpec.substring(1, unitSpec.length()-1);
		    try
		    {
			unit = Parser.instance().parse(unitSpec);
		    }
		    catch (Exception e)
		    {
			System.err.println(
			    "visad.data.dods.Adapter.realType(String,...): " +
			    "Ignoring non-decodable unit-specification \"" +
			    unitSpec + "\" of variable \"" + name + "\"");
			unit = null;
		    }
		}
	    }
	}
	return RealType.getRealType(scalarName(name), unit);
    }

    /**
     * Returns the attribute table corresponding to a DODS variable.
     *
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @param baseType		The type of the sub-component.  May not be
     *				<code>null</code>.
     * @return			The attribute table corresponding to the
     *				variable.  Will be <code>null</code> if no such
     *				table exists.
     */
    protected static AttributeTable attributeTable(DAS das, BaseType baseType)
    {
	return das.getAttributeTable(baseType.getName());
    }

    /**
     * Returns the attribute table corresponding to a name.
     *
     * @param das		The DODS DAS in which information on the name
     *				is embedded.
     * @param baseType		The name to lookup in the DAS.
     * @return			The attribute table corresponding to the
     *				name.  Will be <code>null</code> if no such
     *				table exists.
     */
    protected static AttributeTable attributeTable(DAS das, String name)
    {
	return das.getAttributeTable(name);
    }

    /**
     * Returns the VisAD {@link MathType} corresponding to an array of 
     * MathTypes.
     *
     * @param mathTypes		The array of mathTypes.
     * @return			The MathType of the input aggregate.  Will be
     *				<code>null</code> if the array has zero length.
     *				Will be the first element of a one-element
     *				array.  Will be either a {@link RealTuple} or a
     *				{@link Tuple} -- as appropriate -- for a
     *				multi-element array.
     * @throws VisADException	VisAD failure.
     */
    protected static MathType mathType(MathType[] mathTypes)
	throws VisADException
    {
	MathType	mathType;
	if (mathTypes.length == 0)
	{
	    mathType = null;
	}
	else if (mathTypes.length == 1)
	{
	    mathType = mathTypes[0];
	}
	else
	{
	    boolean	allReals = true;
	    for (int i = 0; i < mathTypes.length && allReals; ++i)
		allReals &= mathTypes[i] instanceof RealType;
	    if (allReals)
	    {
		RealType[]	realTypes = new RealType[mathTypes.length];
		for (int i = 0; i < realTypes.length; ++i)
		    realTypes[i] = (RealType)mathTypes[i];
		mathType = new RealTupleType(realTypes);
	    }
	    else
	    {
		mathType = new TupleType(mathTypes);
	    }
	}
	return mathType;
    }

    /**
     * Returns the {@link visad.data.FileFlatField} cacheing strategy for DODS
     * adapters.  This may be used by DODS adapters during the creation of
     * FileFlatField-s.
     *
     * @return			The FileFlatField cacheing strategy.
     */
    protected CacheStrategy getCacheStrategy()
    {
	return cacheStrategy;
    }
}
