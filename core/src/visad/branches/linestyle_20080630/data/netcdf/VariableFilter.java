/*
 * Copyright 1998-2001, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VariableFilter.java,v 1.3 2001-09-11 16:39:09 steve Exp $
 */

package visad.data.netcdf;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfWrapper;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;

/**
 * Wrapper class for restricting the {@link Netcdf#iterator()} and {@link
 * Netcdf#size()} methods to only those variables that are also in a
 * client-specified list.  All other methods of the {@link Netcdf} API
 * are those of {@link NetcdfWrapper}.  Thus, it is possible to bypass
 * the variable-filtering provided by this class by directly invoking
 * the single-variable methods
 * (e.g. {@link Netcdf#get(String) Netcdf.get(String)},
 * {@link Netcdf#contains(Object) Netcdf.contains(Object)}).
 *
 * @author Steven R. Emmerson
 * @version $Revision: 1.3 $ $Date: 2001-09-11 16:39:09 $
 */
final class VariableFilter
    extends NetcdfWrapper
{
    /**
     * The set of variables to reveal.  Every name corresponds to a variable 
     * that exists in the wrapped netCDF database.
     */
    private Set names;  // not "final" to accomodate JDK 1.2 bug

    /**
     * Constructs from a netCDF dataset and a set of variable names.  The
     * dataset will appear to only contain the set of variables that is the
     * intersection of the variables in the dataset and the named variables.
     * @param netcdf               The netCDF dataset to be wrapped.
     * @param names                A set of variable names.
     * @param NullPointerException if either argument is <code>null</code>.
     * @param ClassCastException   if an element of the set isn't a {@link
     *                             String}.
     */
    protected VariableFilter(Netcdf netcdf, Set names)
        throws NullPointerException
    {
        super(netcdf);
        if (names == null)
            throw new NullPointerException();
        this.names = new TreeSet(names);
        for (Iterator iter = this.names.iterator(); iter.hasNext(); )
            if (!netcdf.contains((String)iter.next()))
                iter.remove();
    }

    /**
     * Returns the number of variables.  This is the number of variables that
     * will be returned by the iterator obtained from the {@link #iterator()}
     * method.
     * @return                    The number of variables
     */
    public int size()
    {
        return names.size();
    }

    /**
     * Returns an iterator over the variables.  The iterator will only return
     * those variables in the underlying netCDF dataset whose names were in the
     * set of names used during construction.  The number of such variables
     * equals the return value of {@link #size()}.
     * @return                    An iterator over the variables.
     */
    public VariableIterator iterator()
    {
        return new WrappedVariableIterator();
    }

    private class WrappedVariableIterator
        implements VariableIterator
    {
        private final Iterator iter;
        WrappedVariableIterator()   { iter = names.iterator(); }
        public boolean hasNext()    { return iter.hasNext(); }
        public Variable next()      { return get((String)iter.next()); }
    }
}
