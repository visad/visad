// $Id: Schema.java,v 1.5 2002-05-29 18:31:37 steve Exp $
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
 * Schema collects the metadata which describes or defines a Netcdf.
 * This is a set of ProtoVariable, the DimensionSet which is
 * the union of the Dimensions used, and any global Attributes.
 * <p>
 * Instances are used as templates in creation of new Netcdf datasets.
 * <p>
 * Variable descriptions in form of ProtoVariable instances can be
 * added to, overwritten, or deleted from a Schema. The associated set of
 * global attributes may also be modified.
 *
 * @see ProtoVariable
 * @see DimensionSet
 * @see AttributeSet
 * @see Netcdf
 * @see java.util.Collection
 *
 * @author $Author: steve $
 * @version $Revision: 1.5 $ $Date: 2002-05-29 18:31:37 $
 */

public class Schema implements java.io.Serializable {

    /**
     * Create an empty Schema
     */
    public
    Schema()
    {
	this.variables = new NamedDictionary(0);
	this.dimensions = new DimensionDictionary();
	this.attributes = new AttributeDictionary();
    }

    /**
     * Copy constructor.
     *  @param sc Schema to copy from. May be empty, shouldn't be null.
     */
    public
    Schema(Schema sc)
    {
	// Use clone instead??
	synchronized (sc)
	{
		this.dimensions = new DimensionDictionary(sc.getDimensions());
		this.variables = new NamedDictionary(sc.size());
		for(ProtoVariableIterator iter = sc.iterator(); iter.hasNext();)
		{
			if(this.put(new ProtoVariable(iter.next())) != null)
				throw new IllegalArgumentException(
					"Duplicate variable name");
		}
		this.attributes = new AttributeDictionary(sc.getAttributes());
	}
    }

    /**
     * Create a Schema initialized by an existing Netcdf.
     * <p>
     * This would be the first step in making a copy of
     * a Netcdf, making a partial copy, or using and existing
     * Netcdf basis for another.
     * @param nc Netcdf from which to generate the Schema.
     */
    public
    Schema(Netcdf nc)
    {
	this.dimensions = new DimensionDictionary(nc.getDimensions());
	this.variables = new NamedDictionary(nc.size());
	for(VariableIterator iter = nc.iterator(); iter.hasNext();)
	{
		if(this.put(new ProtoVariable(iter.next())) != null)
			throw new IllegalArgumentException(
				"Duplicate variable name");
	}
	this.attributes = new AttributeDictionary(nc.getAttributes());
    }

    /**
     * Create a Schema initialized by an array of ProtoVariable
     * and an array of Attributes.
     * @param varArray ProtoVariable [] to initialize the Schema.
     *   May be null or length 0.
     * @param attrArray ProtoVariable [] to initialize the (global) Attributes.
     *   May be null or length 0.
     */
    public
    Schema(ProtoVariable [] varArray, Attribute [] attrArray)
    {
	if(varArray == null) {
		this.variables = new NamedDictionary(0);
		this.dimensions = new DimensionDictionary();
	}
	else synchronized (varArray) {
		this.dimensions = new DimensionDictionary(varArray);
		this.variables = new NamedDictionary(varArray);
	}
	this.attributes = new AttributeDictionary(attrArray);
    }

/* Begin ProtoVariableSet */

    /**
     * Returns the number of ProtoVariable objects in this set
     * @return int number of elements in the set
     */
    public int
    size() {
	return variables.size();
    }

    /**
     * Returns ProtoVariableIterator for the elements.
     * @return ProtoVariableIterator for the elements.
     * @see ProtoVariableIterator
     */
    public ProtoVariableIterator
    iterator() {
	return new ProtoVariableIterator() {
		final java.util.Enumeration ee = variables.elements();

    		public boolean hasNext() {
			return ee.hasMoreElements();
    		}

		public ProtoVariable next() {
			return (ProtoVariable) ee.nextElement();
		}

	};
    }

    /**
     * Returns a new Array containing the elements of this set.
     * @return a new Array containing the elements of this set.
     */
    public ProtoVariable []
    toArray() {
	final ProtoVariable [] aa = new ProtoVariable[this.size()];
	final ProtoVariableIterator ee = this.iterator();
	for(int ii = 0; ee.hasNext(); ii++)
		aa[ii] = ee.next();
	return aa;
    }

    /**
     * Retrieve the variable associated with the specified name.
     * @param name String which identifies the desired variable
     * @return the variable, or null if not found
     */
    public ProtoVariable
    get(String name) {
	return (ProtoVariable) variables.get(name);
    }

    /**
     * Tests if the ProtoVariable identified by <code>name</code>
     * is in this set.
     * @param name String which identifies the desired variable
     * @return <code>true</code> if and only if this set contains
     * the named ProtoVariable.
     */
    public boolean
    contains(String name) {
	return variables.contains(name);
    }

    /**
     * Tests if the argument is in this set.
     * @param oo some Object
     * @return <code>true</code> if and only if this set contains
     * <code>oo</code>
     */
    public boolean
    contains(Object oo) {
	return variables.contains(oo);
    }

    /**
     * Ensures that this set contains the specified ProtoVariable.
     * If a different ProtoVariable with the same name, was in the set,
     * it is returned, otherwise null is returned.
     *
     * @param var the ProtoVariable to be added to this set.
     * @return ProtoVariable replaced or null if not a replacement
     */
    public synchronized ProtoVariable
    put(ProtoVariable var) {
	final ProtoVariable hit = this.get(var.getName());
	if(hit != null)
	{
		if(hit == var)
			return null; // Nothing to do
		// else
		this.remove(hit);
	}
	variables.put(var);
	var.connectDims(dimensions);
	return hit;
    }


    /**
     * Delete the ProtoVariable specified by name from this set.
     *
     * @param name String identifying the ProtoVariable to be removed.
     * @return true if the Set changed as a result of this call.
     */
    public synchronized boolean remove(String name) {
	final ProtoVariable var = (ProtoVariable) variables.remove(name);
	if(var == null)
		return false; // not found
	// else, reconcile dimensions
	// TODO: better algorithm
	for(DimensionIterator checkiter = var.getDimensionIterator();
		checkiter.hasNext();)
	{
		boolean hit = false;
		final Dimension check = checkiter.next();
		final String checkStr = check.getName();
		for(ProtoVariableIterator viter = this.iterator();
				viter.hasNext();)
		{
			for(DimensionIterator diter
				 = viter.next().getDimensionIterator();
					diter.hasNext();)
			{
				final Dimension ref = diter.next();
				if(ref.getName().equals(checkStr))
				{
					hit = true;
					break;
				}
			}
			if(hit)
				break;
		}
		if(!hit)
			dimensions.remove(checkStr);
	}
	return true;
    }


    /**
     * Delete the ProtoVariable specified from this set.
     *
     * @param oo ProtoVariable to be removed.
     * @return true if the Set changed as a result of this call.
     */
    public boolean remove(Object oo) {
	if(this.contains(oo))
	{
		return this.remove(((Named)oo).getName());
	}
	return false;
    }

/* End ProtoVariableSet */
/* Begin VSMixIn */

    /**
     * Returns the set of dimensions associated with this,
     * the union of those used by each of the variables.
     *
     * @return DimensionSet containing dimensions used
     * by any of the variables. May be empty. Won't be null.
     */
    public DimensionSet
    getDimensions() {
	return (DimensionSet) dimensions;
    }

    /**
     * Returns the set of attributes associated with this,
     * also know as the "global" attributes.
     *
     * @return AttributeSet. May be empty. Won't be null.
     */
    public AttributeSet
    getAttributes() {
	return (AttributeSet) attributes;
    }

    /**
     * Convenience function; look up (global) Attribute by name.
     *
     * @param name the name of the attribute
     * @return the attribute, or null if not found
     */
    public Attribute getAttribute(String name) {
	return attributes.get(name);
    }

/* End VSMixIn */

    /**
     * Convenience function; add global attribute.
     * @see AttributeSet#put
     * @param attr the Attribute to be added to this set.
     * @return Attribute replaced or null if not a replacement
     */
    public Attribute
    putAttribute(Attribute attr) {
	return attributes.put(attr);
    }

    /**
     * Format as CDL.
     * @param buf StringBuffer into which to write
     */
    public void
    toCdl(StringBuffer buf)
    {
	buf.append("{\n");
	dimensions.toCdl(buf);
	buf.append("variables:\n");
	for (ProtoVariableIterator iter = this.iterator();
			iter.hasNext() ;) {
		buf.append("\t");
		iter.next().toCdl(buf);
	}
	if(attributes.size() > 0) {
		buf.append("\n// global attributes:\n");
		attributes.toCdl(buf);
	}
	buf.append("\n}");
    }

    /**
     * @return a CDL string of this.
     */
    public String toString() {
	StringBuffer buf = new StringBuffer();
	toCdl(buf);
	return buf.toString();
    }

    int
    indexOf(Dimension dim)
	{ return dimensions.indexOf(dim); }

    void
    putDimension(Dimension dim)
	{ dimensions.initialPut(dim); }

    /**
     * @serial
     */
    private final DimensionDictionary dimensions;
    /**
     * @serial
     */
    private final AttributeDictionary attributes;
    /**
     * @serial
     */
    private final NamedDictionary variables;
}
