/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ExportVar.java,v 1.5 1998-03-10 19:49:33 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import java.rmi.RemoteException;
import ucar.multiarray.Accessor;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import ucar.netcdf.ProtoVariable;
import visad.DoubleSet;
import visad.Field;
import visad.FloatSet;
import visad.FunctionType;
import visad.GriddedSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.Tuple;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/*
 * Superclass for adapting VisAD data to a netCDF Variable.
 */
abstract class
ExportVar
    extends	ProtoVariable
    implements	Accessor
{
    /**
     * Construct from broken-out information.
     */
    protected
    ExportVar(String name, Class type, Dimension[] dims, Attribute[] attrs)
	throws BadFormException
    {
	super(name, type, dims, attrs);
    }


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
     * Return an array element identified by position.
     *
     * This is the only method that needs to be implemented to support
     * the saving of VisAD data in a netCDF dataset.
     */
     public abstract Object
     get(int[] indexes)
	throws IOException;


    /**
     * Return an array element identified by position.
     */
     public boolean
     getBoolean(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public char
     getChar(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public byte
     getByte(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public short
     getShort(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public int
     getInt(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public long
     getLong(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public float
     getFloat(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public double
     getDouble(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return a MultiArray into a slice of the data.
     */
    public MultiArray
    copyout(int[] origin, int[] shape)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }
}
