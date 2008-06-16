/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Context.java,v 1.2 2000-06-08 19:13:43 steve Exp $
 */

package visad.data.netcdf.in;


/**
 * Provides support for the context in which data values are retrieved.
 *
 * Instances are mutable.
 */
public class
Context
{
    /**
     * The indexes of this context.
     */
    private int[]	indexes;


    /**
     * Constructs from nothing.
     */
    public
    Context()
    {
	indexes = new int[0];
    }


    /**
     * Constructs from the number of indexes.
     */
    private
    Context(int n)
    {
	indexes = new int[n];
    }


    /**
     * Gets a new (sub) context based on this context.
     *
     * @return			A (sub) context.
     * @postcondition		<code>depth() == </code>INITIAL(<code>depth()
     *				</code>)<code> + 1</code>
     */
    public Context
    newSubContext()
    {
	Context	subContext = new Context(indexes.length + 1);

	System.arraycopy(indexes, 0, subContext.indexes, 0, indexes.length);
	subContext.indexes[indexes.length] = 0;

	return subContext;
    }


    /**
     * Sets the current (sub) context.
     *
     * @param index		The current (sub) context.
     */
    public void
    setSubContext(int index)
    {
	indexes[indexes.length-1] = index;
    }


    /**
     * Returns the current context.
     *
     * @return			The current context.
     */
    public int[]
    getContext()
    {
	int[]	context = new int[indexes.length];

	System.arraycopy(indexes, 0, context, 0, indexes.length);

	return context;
    }


    /**
     * Returns a string representation of this context.
     */
    public String
    toString()
    {
	StringBuffer	buf = new StringBuffer(128);

	buf.append("{");

	for (int i = 0; i < indexes.length; ++i)
	{
	    if (i > 0)
		buf.append(", ");

	    buf.append(indexes[i]);
	}

	buf.append("}");

	return buf.toString();
    }


    /**
     * Returns a clone of this instance.
     *
     * @return			A clone of this instance.
     */
    public Object clone()
    {
	Context	clone = new Context();

	clone.indexes = (int[])indexes.clone();
	return clone;
    }
}
