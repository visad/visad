/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DewPointSoundingProxy.java,v 1.1 1998-10-28 17:16:48 steve Exp $
 */

package visad.meteorology;

import visad.FlatField;
import visad.FunctionType;
import visad.RealType;
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
    extends	SoundingComponentProxy
    implements	DewPointSounding
{
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
	super(field, new int[] {index});
    }
}
