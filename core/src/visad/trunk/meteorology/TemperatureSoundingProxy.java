/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TemperatureSoundingProxy.java,v 1.2 1998-11-03 22:27:37 steve Exp $
 */

package visad.meteorology;

import visad.FlatField;
import visad.FunctionType;
import visad.RealType;
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
}
