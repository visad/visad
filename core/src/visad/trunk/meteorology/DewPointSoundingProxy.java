/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DewPointSoundingProxy.java,v 1.3 1999-01-07 16:13:17 steve Exp $
 */

package visad.meteorology;

import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.Real;
import visad.RealType;
import visad.Unit;
import visad.VisADException;


/**
 * Adapts a FlatField to a DewPointSounding API.
 *
 * Instances are mutable.
 *
 * @author Steven R. Emmerson
 */
public class
DewPointSoundingProxy
    extends	SingleSoundingProxy
    implements	DewPointSounding
{
    /**
     * The range type.
     */
    private static final RealType       rangeType = DEW_POINT_TYPE;


    /**
     * Constructs from a FlatField and the index of the dew-point component
     * of the field.
     *
     * @param field		The FlatField to be adapted.
     * @param index		Range-index of the dew-point component of the
     *				field.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IllegalArgumentException
     *				<code>field == null ||
     *				index < 0 || index >= field.getRangeDimension()
     *				||</code> <unit of component not temperature>.
     */
    public
    DewPointSoundingProxy(FlatField field, int index)
	throws VisADException
    {
	super(rangeType, field, index);
    }


    /**
     * Gets the sounding dew point at a given pressure.
     * @param pressure		The pressure at which to get the dew point.
     * @return			The dew point at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getDewPoint(Real pressure)
	throws VisADException
    {
	return null;
    }


    /**
     * Gets the sounding dew point at given pressures.
     * @param pressure		The pressures at which to get the dew point.
     * @return			The dew point at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Gridded1DSet
    getDewPoint(Gridded1DSet pressure)
	throws VisADException
    {
	return null;
    }
}
