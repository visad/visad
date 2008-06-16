/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InMemoryStrategy.java,v 1.1 2001-12-19 21:02:40 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.DataImpl;

/**
 * <p>An import strategy that attempts to read the entire netCDF dataset into
 * memory.</p>
 *
 * <p>Instances are immutable.</p>
 *
 * @author Steven R. Emmerson
 */
public class InMemoryStrategy extends Strategy
{
    /**
     * The single instance of this class.
     */
    private static final InMemoryStrategy INSTANCE;

    static
    {
        INSTANCE = new InMemoryStrategy();
    }

    /**
     * Constructs from nothing.
     */
    private InMemoryStrategy()
    {}

    /**
     * Returns an instance of this class.
     *
     * @return                      An instance of this class.
     */
    public static InMemoryStrategy instance()
    {
        return INSTANCE;
    }

    /**
     * <p>Returns a VisAD data object corresponding to the netCDF dataset.</p>
     *
     * <p>This implementation uses the data-merging of {@link 
     * Merger#instance()}.</p>
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
    public DataImpl
    getData(NetcdfAdapter adapter)
        throws IOException, VisADException, RemoteException,
            BadFormException, OutOfMemoryError
    {
        return
            adapter.importData(
                adapter.getView(), Merger.instance(), DataFactory.instance());
    }
}
