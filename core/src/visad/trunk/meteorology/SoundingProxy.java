/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingProxy.java,v 1.2 1998-11-03 22:27:36 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.MathType;
import visad.VisADException;


/**
 * Acts as a proxy for sounding data in a FlatField.  This differs from
 * SoundingImpl in that there is no local data; instead, lightweight proxies
 * are created that access the data in the original FlatField.
 *
 * Instances are mutable.
 *
 * @author Steven R. Emmerson
 */
public class
SoundingProxy
    extends	AbstractSounding
{
    /**
     * The temperature sounding proxy.
     */
    private /*final*/ TemperatureSoundingProxy	temperatureSounding;

    /**
     * The dew-point sounding proxy.
     */
    private /*final*/ DewPointSoundingProxy	dewPointSounding;

    /**
     * The wind profile proxy.
     */
    private /*final*/ WindProfileProxy		windProfile;


    /**
     * Constructs from nothing.
     *
     * @param field		The FlatField to be adapted.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public 
    SoundingProxy(FlatField field)
	throws VisADException
    {
	super(field);

	try
	{
	    temperatureSounding = new TemperatureSoundingProxy(field, 0);
	}
	catch (IllegalArgumentException e)
	{
	    temperatureSounding = null;
	}

	try
	{
	    dewPointSounding = new DewPointSoundingProxy(field, 0);
	}
	catch (IllegalArgumentException e)
	{
	    dewPointSounding = null;
	}

	try
	{
	    windProfile = new WindProfileProxy(field, 0, 1);
	}
	catch (IllegalArgumentException e)
	{
	    windProfile = null;
	}
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			The temperature sounding.  NB: Not a copy.
     */
    public TemperatureSounding
    getTemperatureSounding()
    {
	return temperatureSounding;
    }


    /**
     * Gets the dew-point sounding.
     *
     * @return			The dew-point sounding.  NB: Not a copy.
     */
    public DewPointSounding
    getDewPointSounding()
    {
	return dewPointSounding;
    }


    /**
     * Gets the wind profile.
     *
     * @return			The wind profile.  NB: Not a copy.
     */
    public WindProfile
    getWindProfile()
    {
	return windProfile;
    }


    /**
     * Gets the single sounding of the given component.
     *
     * @param componentType     The type of the component.
     * @return                  The single sounding of the <code>componentType
     *                          </code> component or <code>null</code> if no
     *                          such component.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws RemoteException  Java RMI failure.
     */
    public SingleSounding
    getSingleSounding(MathType componentType)
        throws RemoteException, VisADException
    {
        int     index = findComponent(componentType);
        return index == -1
                ? null
                : new SingleSoundingProxy(componentType, this, index);
    }
}
