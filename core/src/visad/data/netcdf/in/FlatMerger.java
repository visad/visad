/*
 * Copyright 2000, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FlatMerger.java,v 1.3 2001-11-27 22:29:35 dglo Exp $
 */

package visad.data.netcdf.in;

import visad.*;


/**
 * Provides support for merging of virtual data objects.  This class maximizes
 * the number of virtual flat-fields by not merging them.  Consequently, this
 * class supports a FileFlatField caching strategy.
 *
 * @author Steven R. Emmerson
 */
public class
FlatMerger
    extends	Merger
{
    /**
     * The singleton instance.
     */
    private static FlatMerger	instance;


    /**
     * Constructs from nothing.
     */
    protected FlatMerger()
    {}


    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static Merger instance()
    {
	if (instance == null)
	{
	    synchronized(FlatMerger.class)
	    {
		if (instance == null)
		    instance = new FlatMerger();
	    }
	}

	return instance;
    }


    /**
     * Does not merge a virtual flat-field with a virtual field.
     *
     * @param field1		The virtual flat-field.
     * @param field2		The virtual field.
     * @return			A virtual field comprising the merger of
     *				the input objects if possible; otherwise 
     *				<code>null</code>.
     * throws VisADException	VisAD failure.
     * @see #merge(VirtualField, VirtualField)
     */
    protected VirtualField merge(VirtualFlatField field1, VirtualField field2)
	throws VisADException
    {
	return null;
    }


    /**
     * Does not merge a virtual flat-field with another virtual flat-field.
     *
     * @param field1		The virtual flat-field.
     * @param field2		The other virtual flat-field.
     * @return			A virtual flat-field comprising the merger of
     *				the input objects if possible; otherwise 
     *				<code>null</code>.  May be <code>field1</code>.
     * throws VisADException	VisAD failure.
     * @see #merge(VirtualFlatField, VirtualFlatField)
     */
    protected VirtualFlatField merge(
	    VirtualFlatField field1, VirtualFlatField field2)
	throws VisADException
    {
	return null;
    }
}
