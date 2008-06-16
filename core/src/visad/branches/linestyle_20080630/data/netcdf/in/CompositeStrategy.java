/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CompositeStrategy.java,v 1.1 2001-12-19 20:55:28 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.DataImpl;

/**
 * <p>Chains together import strategies for netCDF datasets.</p>
 *
 * <p>Instances of this class are immutable.</p>
 *
 * @author Steven R. Emmerson
 */
public final class CompositeStrategy extends Strategy
{
    /**
     * The set of import strategies to use.  Will have 2 or more elements.
     */
    private final Strategy[] strategies;

    /**
     * Constructs from an array of import strategies.
     *
     * @param strategies                The import strategies to use.
     * @throws NullPointerException     if the argument is <code>null</code>.
     * @throws IllegalArgumentException if the number of strategies is 1.
     */
    private CompositeStrategy(Strategy[] strategies)
    {
        if (strategies.length < 2)
            throw new IllegalArgumentException();
        this.strategies = (Strategy[])strategies.clone();
    }


    /**
     * Returns an import strategy.  The given strategies are tried in the order
     * they appear in the array.  This method returns when a strategy succeeds.
     *
     * @param strategies                The import strategies to use.
     * @return                          An import strategy.
     * @throws NullPointerException     if the argument is <code>null</code>.
     * @throws IllegalArgumentException if the number of strategies is 0.
     */
    public static Strategy instance(Strategy[] strategies)
    {
        return
            strategies.length == 1
                ? strategies[0]
                : new CompositeStrategy(strategies);
    }

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
     * @throws OutOfMemoryError if the netCDF dataset couldn't be imported due
     *                          to insufficient memory.
     * @throws RemoteException  if a Java RMI failure occurs.
     */
    public DataImpl getData(NetcdfAdapter adapter)
        throws IOException, VisADException, RemoteException,
            BadFormException, OutOfMemoryError
    {
        for (int i = 0; i < strategies.length - 1; i++) {
            System.gc();
            try
            {
                return strategies[i].getData(adapter);
            }
            catch (OutOfMemoryError memErr)
            {
                System.err.println(
                    "Couldn't import netCDF dataset due to " +
                    "insufficient memory.  Using different strategy...");
            }
        }
        return strategies[strategies.length - 1].getData(adapter);
    }
}
