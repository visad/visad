// $Id: Dimension.java,v 1.4 2002-05-29 18:31:33 steve Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
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
import java.io.Serializable;

/**
 * A Dimension object is used to contain an array length which is
 * named for use in multiple netcdf variables.
 * <p>
 * This class supports construction, retrieval of the name and retrieval 
 * of the length value. The name is constant over the lifetime of the object.
 * Also note that change of the dimension length value is not
 * allowed. In the subclass UnlimitedDimension, the length may be increased.
 * <p>
 * Instances which have same name and same value are equal.
 * We override hashCode() and equals() to be consistent with
 * this semantic.
 *
 * @see UnlimitedDimension
 *
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:33 $
 */

public class
Dimension
	implements Named, Serializable, Cloneable
{

 /* Begin Constructors */

	/**
	 * @param name  String which is to be the name of this Dimension
	 * @param length  int length of this Dimension
	 */
	public
	Dimension(String name, int length) {
		this.name = name;
		this.length = length;
	}

 /* End Constructors */
 /* Begin Overrides */

	/**
	 * Instances which have same name and same value are equal.
	 * Overrides Object.hashCode() to be consistent with this semantic.
	 */
	public int
	hashCode()
	{
		return (name.hashCode() ^ length);
	}

	/**
	 * Instances which have same name and same value are equal.
	 * Overrides Object.hashCode() to be consistent with this semantic.
	 */
	public boolean
	equals(Object oo)
	{
		if(this == oo) return true;
		if((oo != null) && (oo instanceof Dimension)
			&& !(oo instanceof UnlimitedDimension))
		{
			final Dimension aDim = (Dimension)oo;
			return ((length == aDim.getLength())
				&& name.equals(aDim.getName()));
		}
		return false;
	}
	
	public Object
	clone()
	{
		/*
		 * Since this is immutable, just return it.
		 * Overridden in mutable subclass.
		 */
		return this;
	}

	/**
	 * @return a string representation of the object.
	 */
	public String
	toString() {
		StringBuffer buf = new StringBuffer();
		toCdl(buf);
		return buf.toString();
	}

 /* End Overrides */

	/**
	 * Returns the name of this Dimension.
	 * @return String which identifies this Dimension.
	 */
	public final String
	getName() {
		return name;
	}

	/**
	 * Retrieve the length.
	 * @return int which is the length of this Dimension
	 */
	public final int
	getLength() {
		return length;
	}

	/**
	 * Format as CDL.
	 * @param buf StringBuffer into which to write
	 */
	public void
	toCdl(StringBuffer buf)
	{
		buf.append(getName());
		buf.append(" = ");
		buf.append(getLength());
		buf.append(" ;");
	}

	/**
	 * The length. Immutable in this class.
	 * @serial
	 */
	protected int length;

	/**
	 * @serial
	 */
	private final String name;
}
