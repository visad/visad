/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TemperatureSoundingProxy.java,v 1.3 1999-01-07 16:13:20 steve Exp $
 */

package visad.meteorology;

import visad.FlatField;
import visad.FunctionType;
import visad.Real;
import visad.RealType;
import visad.Unit;
import visad.VisADException;


/**
 * Adapts a FlatField to a TemperatureSounding API.
 *
 * Instances are mutable.
 *
 * @author Steven R. Emmerson
 */
public class
TemperatureSoundingProxy
    extends	SingleSoundingProxy
    implements	TemperatureSounding
{
    /**
     * The type of the range component.
     */
    private static final RealType	rangeType = TEMPERATURE_TYPE;


    /**
     * Constructs from a FlatField and the index of the temperature component
     * of the field.
     *
     * @param field		The FlatField to be adapted.
     * @param index		Range-index of the temperature component of the
     *				field.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IllegalArgumentException
     *				<code>field == null ||
     *				index < 0 || index >= field.getRangeDimension()
     *				||</code> <unit of component not temperature>.
     */
    public
    TemperatureSoundingProxy(FlatField field, int index)
	throws VisADException
    {
	super(rangeType, field, index);
    }


    /**
     * Gets the sounding temperature at a given pressure.
     * @param pressure		The pressure at which to get the temperature.
     * @return			The temperature at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getTemperature(Real pressure)
	throws VisADException
    {
	return null;
    }
}
