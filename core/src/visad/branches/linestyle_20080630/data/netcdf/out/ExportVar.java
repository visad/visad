/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ExportVar.java,v 1.4 2001-11-27 22:29:38 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.multiarray.Accessor;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import ucar.netcdf.ProtoVariable;
import visad.data.BadFormException;


/*
 * The ExportVar class provides an abstract class for adapting VisAD data to
 * a netCDF Variable.
 */
abstract class
ExportVar
    extends	ProtoVariable
    implements	Accessor
{
    /**
     * Construct from broken-out information.
     *
     * @param name	The name of the netCDF variable.
     * @param type	The type of the netCDF variable (i.e. Double.TYPE,
     *			Byte.TYPE, Character.TYPE, etc.).
     * @param dims	The dimensions of the netCDF variable.
     * @param attrs	The attributes of the netCDF variable.
     * @exception BadFormException
     *			The VisAD data object cannot be adapted to a netCDF API.
     */
    protected
    ExportVar(String name, Class type, Dimension[] dims, Attribute[] attrs)
	throws BadFormException
    {
	super(name, type, dims, attrs);
    }


    /**
     * Return an array element identified by position.  This is the only
     * method that needs to be implemented to support the saving of
     * VisAD data in a netCDF dataset.
     *
     * @param indexes		The position of the array element as netCDF
     *				indexes.
     * @exception IOException	Data access I/O failure.
     */
     public abstract Object
     get(int[] indexes)
	throws IOException;


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    copyin(int[] origin, MultiArray multiArray)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    set(int[] index,  Object value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setBoolean(int[] index,  boolean value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setChar(int[] index,  char value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setByte(int[] index,  byte value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setShort(int[] index, short value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setInt(int[] index, int value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setLong(int[] index, long value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setFloat(int[] index, float value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setDouble(int[] index, double value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public boolean
     getBoolean(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public char
     getChar(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public byte
     getByte(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public short
     getShort(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public int
     getInt(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public long
     getLong(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public float
     getFloat(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.  Not supported.
     */
     public double
     getDouble(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return a MultiArray into a slice of the data.  Not supported.
     */
    public MultiArray
    copyout(int[] origin, int[] shape)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Convert values to an array.  Not supported.
     */
    public Object
    toArray()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Convert values to an array.  Not supported.
     */
    public Object
    toArray(Object obj, int[] dummy1, int[] dummy2)
    {
	throw new UnsupportedOperationException();
    }
}
