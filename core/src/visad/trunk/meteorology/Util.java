/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Util.java,v 1.2 1999-01-07 16:13:21 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for utility functions.
 *
 * @author Steven R. Emmerson
 */
public abstract class
Util
{
    /**
     * Gets the default units of the given range components of a FlatField.
     *
     * @param field		The field to be examined.
     * @param indexes		Indexes of the components in the range of the
     *				field.
     * @param types		Expected, compatible types for the components.
     * @throws IllegalArgumentException
     *				<code>field == null ||
     *				indexes.length != types.length</code>,
     *				or indexes out-of-bounds.
     * @throws TypeException	Type of component in range of field not
     *				compatible with expected type.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static Unit[]
    getDefaultUnits(FlatField field, int[] indexes, RealType[] types)
	throws IllegalArgumentException, TypeException, VisADException
    {
	if (field == null)
	    throw new IllegalArgumentException("Null field");

	if (indexes.length != types.length)
	    throw new IllegalArgumentException(
		"indexes.length != units.length");

	RealTupleType	rangeType = 
	    ((FunctionType)field.getType()).getFlatRange();
	int	rangeRank = rangeType.getDimension();
	Unit[]	units = new Unit[indexes.length];

	for (int i = 0; i < indexes.length; ++i)
	{
	    int		index = indexes[i];

	    if (index < 0 || index >= rangeRank)
		throw new IllegalArgumentException("Index out-of-bounds");

	    RealType	componentType = (RealType)rangeType.getComponent(index);

	    if (!componentType.equalsExceptNameButUnits(types[i]))
		throw new TypeException(
		    "Actual type (" + componentType + 
		    ") not compatible with expected type (" + types[i] + ")");

	    units[i] = componentType.getDefaultUnit();
	}

	return units;
    }
}
