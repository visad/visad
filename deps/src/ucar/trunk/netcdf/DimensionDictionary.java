/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import java.io.Serializable;

/**
 * DimensionDictionary is package private implementation of DimensionSet.
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */

class
DimensionDictionary
	implements DimensionSet, Serializable
{
	/**
	 */
	DimensionDictionary()
	{
		this.dimensions = new NamedDictionary(0);
	}

	/**
	 */
	DimensionDictionary(final DimensionSet ss)
	{
		dimensions = new NamedDictionary(ss.size());
		final DimensionIterator iter = ss.iterator();
		while(iter.hasNext())
		{
			put(iter.next());
		}
	}

	/**
	 */
	DimensionDictionary(ProtoVariable [] varArray)
	{
		// varArray synchronized in caller
		this.dimensions = new NamedDictionary(0);
		for(int jj = 0; jj < varArray.length; jj++)
		{
			varArray[jj].connectDims(this);
		}
	}

	/**
	 * Returns the number of elements contained within the Dictionary. 
	 */
	public int
	size()
	{
		return dimensions.size();
	}

	/**
	 * Returns an iterator of the elements. Use the Iterator methods 
	 * on the returned object to fetch the elements sequentially.
	 * @see java.util.Iterator
	 */
	public DimensionIterator
	iterator()
	{
		return new DimensionIterator() {
			final java.util.Enumeration ee = dimensions.elements();
			
			public boolean hasNext() {
				return ee.hasMoreElements();
			}

			public Dimension next() {
				return (Dimension) ee.nextElement();
			}

		};
	}

	/**
	 * @returns a new Array containing (clones of) elements of this set.
	 */
	synchronized public Dimension []
	toArray() {
		final Dimension [] aa = new Dimension[this.size()];
		final DimensionIterator ee = this.iterator();
		for(int ii = 0; ee.hasNext(); ii++)
		{
			final Dimension dim = ee.next();
			aa[ii] = (Dimension) dim.clone();
		}
		return aa;
	}

	/**
	 * Gets the dimension associated with the specified name.
	 * @param name the name of the dimension
	 * @returns the dimension, or null if not found
	 */
	public Dimension
	get(String name) {
		return (Dimension) dimensions.get(name);
	}

	/**
	 * Tests if the Dimension identified by <code>name</code>
	 * is in this set.
	 * @param name String which identifies the desired dimension
	 * @return <code>true</code> if and only if this set contains
	 * the named Dimension.
	 */
	public boolean
	contains(String name) {
		return dimensions.contains(name);
	}

	/**
	 * Tests if the argument is in this set.
	 * @param oo some Object
	 * @return <code>true</code> if and only if this set contains
	 * <code>oo</code>
	 */
	public boolean
	contains(Object oo) {
		return dimensions.contains(oo);
	}

	/**
	 * Format as CDL.
	 * @param buf StringBuffer into which to write
	 */
	public void
	toCdl(StringBuffer buf)
	{
		buf.append("dimensions:\n");
		for (DimensionIterator iter = this.iterator();
				iter.hasNext() ;) {
			buf.append("\t");
			iter.next().toCdl(buf);
			buf.append("\n");
		}
	}

	/**
	 * @return a CDL string of this.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		toCdl(buf);
		return buf.toString();
	}

	/**
	 * Ensures that this set contains a Dimension which is
	 * equal() to the argument.
	 * If such an element exists, it is returned.
	 * Otherwise, a clone is created, added to the dictionary,
	 * and the clone is returned.
	 * NOTE: this is different than the usual container.put() 
	 * return!
         * <p>
	 * If a different (not equal()) Dimension with the same name
	 * was in the set, throw IllegalArgumentException.
	 *
	 * @param dim the Dimension to be added to this set.
	 * @returns Dimension added or matching member from the set.
	 */
	synchronized Dimension
	put(Dimension dim) {
		final String dname = dim.getName();
		final Dimension found = get(dname);
		if(found != null)
		{
			if(found.equals(dim))
				return found; // Normal return
			// else
			throw new IllegalArgumentException(
					"Duplicate dimension name"); 
		}
		// else
		final Dimension copy = (Dimension) dim.clone();
		dimensions.put(copy);
		return copy;
	}

	/**
	 * Add a Dimension instance to this dictionary.
	 * <>
	 * Use this form when initializing from a an existing
	 * data set and you want instances (and thus UnlimitedDimension
	 * values) preserved.
	 */
	void
	initialPut(Dimension dim)
	{
		if(contains(dim.getName()))
			throw new IllegalArgumentException(
					"Duplicate dimension name \""
				+ dim.getName() + "\""); 
		// else
		dimensions.put(dim);
	}

	/**
	 * Delete the Dimension specified by name from this set.
	 *
	 * @param name String identifying the Dimension to be removed.
	 * @returns true if the Set changed as a result of this call.
	 */
	boolean
	remove(String name) {
		final Named oo = dimensions.remove(name);
		return oo != null ? true : false;
	}

	/**
	 * Searches for the specified object, starting from the first position
	 * and returns an index to it.
	 * @param elem the desired element
	 * @return the index of the element, or -1 if it was not found.
	 */
	int
	indexOf(Dimension elem) {
		return dimensions.indexOf(elem);
	}

	private final NamedDictionary dimensions;
}
