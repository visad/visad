/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;

/**
 * A Dimension object is used to contain an array length which is
 * named for use in multiple netcdf variables.
 * For UnlimitedDimensions, the array length value may grow.
 * An UnlimitedDimension may appear as the most slowly varying dimension
 * of a Variable.
 * <p>
 * This class supports construction, retrieval of the name, retrieval 
 * of the length value, and increase of the length value.
 * <p>
 * Instances are only distiguished by name.
 * Override hashCode() and equals() to create this semantic.
 * <p>
 * @see Dimension
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.3 $ $Date: 2000-08-28 21:45:47 $
 */

public class
UnlimitedDimension
	extends Dimension
{
 /* Begin Constructors */

	/**
	 * The usual constructor defaults initial length to 0.
	 *
	 * @param name  String which is to be the
	 *	name of this UnlimitedDimension
	 */
	public 
	UnlimitedDimension(String name)
	{
		this(name, 0);
	}

	/**
	 * Constructing from other data sets with initial length.
	 * Package private.
	 * 
	 * @param name  String which is to be the
	 *	name of this UnlimitedDimension
	 * @param length  int which is the initial length
	 */
	UnlimitedDimension(String name, int length)
	{
		super(name, length);
	}

 /* End Constructors */
 /* Begin Overrides */

	/**
	 * Instances are only distiguished by name.
	 * Override super.hashCode() to be consistent with this semantic.
	 */
	public int
	hashCode()
	{
		return getName().hashCode();
	}

	/**
	 * Instances are only distiguished by name.
	 * Override super.equals() to to be consistent with this semantic.
	 */
	public boolean
	equals(Object oo)
	{
		if(this == oo) return true;
		if((oo != null) && (oo instanceof UnlimitedDimension))
			return getName().equals(
				((UnlimitedDimension)oo).getName());
		return false;
	}

	public Object
	clone()
	{
		return new UnlimitedDimension(getName());
	}

	/**
	 * Format as CDL.
	 * @param buf StringBuffer into which to write
	 */
	public void
	toCdl(StringBuffer buf)
	{
		buf.append(this.getName());
		buf.append(" = UNLIMITED ;");
		buf.append(" // (");
		buf.append(this.getLength());
		buf.append(" currently)");
	}

 /* End Overrides */

	/**
	 * Set the length to be at least newLength
	 * Should be Package private.
	 *
	 * @param newLength int which is the minimum new length
	 * @return int amount by which this grew to satisfy the request.
	 */
	synchronized public int
	setLength(int newLength)
	{
		final int diff = newLength - length;
		if(diff <= 0)
			return 0;
		this.length = newLength;
		return diff;
	}
}
