/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: MaxFileFieldStrategy.java,v 1.5 2001-12-19 20:55:19 steve Exp $
 */

package visad.data.netcdf.in;

import visad.*;
import visad.data.netcdf.*;


/**
 * <p>Provides support for importing netCDF datasets using the strategy of
 * employing FileFlatField-s wherever possible and not merging them so as to
 * keep the number of FileFlatField-s to a maximum.</p>
 *
 * <p>Instances are immutable.</p>
 *
 * @author Steven R. Emmerson
 */
public class MaxFileFieldStrategy
    extends     FileStrategy
{
    /**
     * The singleton instance of this class.
     */
    private static MaxFileFieldStrategy instance = new MaxFileFieldStrategy();


    /**
     * Returns an instance of this class.
     *
     * @return                  An instance of this class.
     */
    public static Strategy instance()
    {
        return instance;
    }


    /**
     * Constructs from nothing.  Protected to ensure use of 
     * <code>instance()</code> method.
     *
     * @see #instance()
     */
    protected MaxFileFieldStrategy()
    {}


    /**
     * Returns the Merger for cosolidating virtual data objects together.  The
     * Merger returned by this method is that returned by {@link
     * FlatMerger#instance()} -- which doesn't merge FlatFields together.
     * @return                  The Merger for cosolidating virtual data 
     *                          objects together.
     * @see Merger
     */
    protected Merger getMerger()
    {
        return FlatMerger.instance();
    }
}
