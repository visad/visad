/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Strategy.java,v 1.1 2001-12-19 21:00:49 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.DataImpl;

/**
 * <p>A strategy for importing a netCDF dataset.</p>
 *
 * @author Steven R. Emmerson
 */
public abstract class Strategy
{
    /**
     * An import strategy that only tries the {@link MaxFileFieldStrategy}
     * strategy.
     */
    public static final Strategy UNMERGED_FILE_FLAT_FIELDS;

    /**
     * An import strategy that first tries the {@link FileStrategy} strategy
     * and then tries the {@link #UNMERGED_FILE_FLAT_FIELDS} strategy.
     */
    public static final Strategy MERGED_FILE_FLAT_FIELDS;

    /**
     * An import strategy that first tries the {@link InMemoryStrategy} strategy
     * and then tries the {@link #MERGED_FILE_FLAT_FIELDS} strategy.
     */
    public static final Strategy IN_MEMORY;

    /**
     * The default import strategy.  The details of this strategy are 
     * unspecified and subject to change.  Currently, it is identical to the
     * {@link #MERGED_FILE_FLAT_FIELDS} strategy.
     */
    public static final Strategy DEFAULT;

    /**
     * The singleton instance of this class.
     */
    private static Strategy instance;

    static
    {
        UNMERGED_FILE_FLAT_FIELDS = MaxFileFieldStrategy.instance();
        MERGED_FILE_FLAT_FIELDS =
            CompositeStrategy.instance(new Strategy[] {
                FileStrategy.instance(),
                UNMERGED_FILE_FLAT_FIELDS,
            });
        IN_MEMORY = 
            CompositeStrategy.instance(new Strategy[] {
                InMemoryStrategy.instance(),
                FileStrategy.instance(),
                MERGED_FILE_FLAT_FIELDS,
            });
        DEFAULT = MERGED_FILE_FLAT_FIELDS;
    }


    /**
     * Constructs from nothing.
     */
    protected Strategy()
    {}


    /**
     * <p>Returns a VisAD data object corresponding to the netCDF dataset.</p>
     *
     * @param adapter           The netCDF-to-VisAD adapter.
     * @return                  The top-level, VisAD data object of the netCDF
     *                          dataset.
     * @throws VisADException   if a problem occurs in core VisAD -- probably 
     *                          because a VisAD object couldn't be created.
     * @throws IOException      if a data access I/O failure occurs.
     * @throws BadFormException if the netCDF dataset doesn't conform to
     *                          conventions implicit in constructing
     *                          View.
     * @throws OutOfMemoryError if the netCDF dataset couldn't be imported into 
     *                          memory.
     * @throws RemoteException  if a Java RMI failure occurs.
     */
    public abstract DataImpl getData(NetcdfAdapter adapter)
        throws IOException, VisADException, RemoteException,
            BadFormException, OutOfMemoryError;
}
