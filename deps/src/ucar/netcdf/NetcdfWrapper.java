// $Id: NetcdfWrapper.java,v 1.2 2002-05-29 18:31:35 steve Exp $
/*
 * Copyright 2001 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package ucar.netcdf;

/**
 * Abstract "decorator" class for wrapping a {@link Netcdf} object.  All
 * method invocations of the {@link Netcdf} API are forwarded to a contained
 * {@link Netcdf} object.  This class is designed to be extended.
 *
 * @author Steven R. Emmerson
 * @version $Revision: 1.2 $ $Date: 2002-05-29 18:31:35 $
 */
public abstract class NetcdfWrapper
    implements Netcdf
{
    /**
     * The wrapped, {@link Netcdf} object.
     */
    private final Netcdf netcdf;

    /**
     * Constructs from a netCDF object.
     * @param netcdf               The netCDF dataset to be wrapped.
     * @param NullPointerException if the argument is <code>null</code>.
     */
    protected NetcdfWrapper(Netcdf netcdf)
        throws NullPointerException
    {
        if (netcdf == null)
            throw new NullPointerException();
        this.netcdf = netcdf;
    }

    /**
     * Returns the wrapped {@link Netcdf} object.
     * @return                    The wrapped, {@link Netcdf} object.
     */
    public final Netcdf getNetcdf()
    {
        return netcdf;
    }

    /**
     * Returns the number of variables.
     * @return                    The number of variables
     */
    public int size()
    {
        return netcdf.size();
    }

    /**
     * Returns an iterator over the variables.
     * @return                    An iterator over the variables.
     */
    public VariableIterator iterator()
    {
        return netcdf.iterator();
    }

    /**
     * Retrieve the variable associated with a name.  If no such variable 
     * exists, then <code>null</code> is returned.
     * @param name                Name of the desired variable.
     * @return                    The variable or <code>null</code>.
     */
    public Variable get(String name)
    {
        return netcdf.get(name);
    }
    
    /**
     * Tests if the Variable identified by <code>name</code> is in this dataset.
     * @param name                Name of the desired variable.
     * @return                    <code>true</code> if and only if this dataset
     *                            contains the named variable.
     */
    public boolean contains(String name)
    {
        return netcdf.contains(name);
    }

    /**
     * Tests an object is in this dataset.
     * @param oo                  An object.
     * @return                    <code>true</code> if and only if this dataset
     *                            contains <code>oo</code>.
     */
    public boolean contains(Object oo)
    {
        return netcdf.contains(oo);
    }

    /**
     * Returns all the netCDF dimensions in this dataset.
     * @return                    The union of all dimensions of all variables.
     *                            May be empty.
     */
    public DimensionSet getDimensions()
    {
        return netcdf.getDimensions();
    }

    /**
     * Returns the set of global, netCDF attributes in this dataset. 
     * @return                    All global attributes in this dataset.  May be
     *                            empty.
     */
    public AttributeSet getAttributes()
    {
        return netcdf.getAttributes();
    }

    /**
     * Returns a global, netCDF attribute by name.  If no such attribute exists,
     * then <code>null</code> is returned.
     * @param                    The name of the attribute.
     * @return                   The attribute or <code>null</code>.
     */
    public Attribute getAttribute(String name)
    {
        return netcdf.getAttribute(name);
    }
}
