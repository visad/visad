// $Id: AttributeDictionary.java,v 1.4 2002-05-29 18:31:33 steve Exp $
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
 * AttributeDictionary is the machinery to implement AttributeSet.
 * It wraps NamedDictionary so that the NamedDictionary can only
 * contain Attributes. Note that AttributeDictionary is mutable,
 * it has methods for adding new elements and removing elements.
 * These methods should be exposed in the ProtoVariable and Schema
 * implementations, and _not_ exposed in the Variable and Netcdf
 * implementations.
 * <p>
 * Note: no public constructor.
 *
 * @see AttributeSet
 *
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:33 $
 */

class AttributeDictionary implements AttributeSet, Serializable {

    AttributeDictionary() {
	this.attributes = new NamedDictionary(0);
    }

    AttributeDictionary(Attribute [] attrArray) {
	this.attributes = new NamedDictionary(attrArray);
    }

    AttributeDictionary(final AttributeSet ss) {
	this.attributes = new NamedDictionary(ss.size(),
		// The cost of type safety at the interface level.
		new java.util.Enumeration () {
			final AttributeIterator ee = ss.iterator();
	    		public boolean hasMoreElements() {
				return ee.hasNext();
	    		}
			public Object nextElement() {
				return (Object) ee.next();
			}
		}
	);
    }

    /**
     * Returns the number of elements contained within the Dictionary. 
     */
    public int size() {
	return attributes.size();
    }

    /**
     * Returns an iterator for the elements. Use the Iterator methods 
     * on the returned object to fetch the elements sequentially.
     * @see java.util.Iterator
     */
    public AttributeIterator iterator() {
	return new AttributeIterator() {
		final java.util.Enumeration ee = attributes.elements();
		
    		public boolean hasNext() {
			return ee.hasMoreElements();
    		}

		public Attribute next() {
			return (Attribute) ee.nextElement();
		}

	};
    }

    /**
     * @return a new Array containing the elements of this set.
     */
    public Attribute [] toArray() {
	final Attribute [] aa = new Attribute[this.size()];
	final AttributeIterator ee = this.iterator();
	for(int ii = 0; ee.hasNext(); ii++)
		aa[ii] = ee.next();
	return aa;
    }

    /**
     * Gets the attribute associated with the specified name.
     * @param name the name of the attribute
     * @return the attribute, or null if not found
     */
    public Attribute get(String name) {
	return (Attribute) attributes.get(name);
    }

    /**
     * Tests if the Attribute identified by <code>name</code>
     * is in this set.
     * @param name String which identifies the desired attribute
     * @return <code>true</code> if and only if this set contains
     * the named Attribute.
     */
    public boolean contains(String name) {
	return attributes.contains(name);
    }

    /**
     * Tests if the argument is in this set.
     * @param oo some Object
     * @return <code>true</code> if and only if this set contains
     * <code>oo</code>
     */
    public boolean contains(Object oo) {
	return attributes.contains(oo);
    }

// Begin Methods used when mutable

    /**
     * Ensures that this set contains the specified Attribute.
     * If a different Attribute with the same name, was in the set,
     * it is returned, otherwise null is returned.
     *
     * @param attr the Attribute to be added to this set.
     * @return Attribute replaced or null if not a replacement
     */
    public Attribute put(Attribute attr) {
	return (Attribute) attributes.put(attr);
    }

    
    /**
     * Delete the Attribute specified by name from this set.
     *
     * @param name String identifying the Attribute to be removed.
     * @return true if the Set changed as a result of this call.
     */
    public boolean remove(String name) {
	final Named oo = attributes.remove(name);
	return oo != null ? true : false;
    }


    /**
     * Delete the Attribute specified from this set.
     *
     * @param oo Attribute to be removed.
     * @return true if the Set changed as a result of this call.
     */
    public boolean remove(Object oo) {
	if(this.contains(oo))
	{
		return this.remove(((Named)oo).getName());
	}
	return false;
    }

// End Methods used when mutable

    /**
     * Format as CDL.
     * @param buf StringBuffer into which to write
     */
    public void
    toCdl(StringBuffer buf)
    {
	for (AttributeIterator iter = this.iterator();
			iter.hasNext() ;) {
		buf.append("\t\t");
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

    protected final NamedDictionary attributes;
}
