/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: WindProfileProxy.java,v 1.2 1998-11-03 22:27:37 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.Data;
import visad.DataShadow;
import visad.FlatField;
import visad.FunctionType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.ShadowType;
import visad.Tuple;
import visad.Unit;
import visad.VisADException;


/**
 * Acts as a proxy for a WindProfile.
 *
 * Instances are mutable.
 *
 * @author Steven R. Emmerson
 */
public class
WindProfileProxy
    extends	SingleSoundingProxy
    implements	WindProfile
{
    /**
     * The type of the range component.
     */
    private static final RealTupleType	rangeType = WIND_TYPE;


    /**
     * Constructs from a FlatField and U and V component-indexes.
     *
     * @param field		The FlatField to be adapted.
     * @param uIndex		Range-index of the U-component of the wind.
     * @param vIndex		Range-index of the V-component of the wind.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IllegalArgumentException
     *				<code>field == null</code>, or indexes
     *				out-of-bounds, or components not speed.
     */
    public
    WindProfileProxy(FlatField field, int uIndex, int vIndex)
	throws VisADException
    {
	super(rangeType, field, new int[] {uIndex, vIndex});
    }
}
