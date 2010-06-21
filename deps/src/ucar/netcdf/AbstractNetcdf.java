// $Id: AbstractNetcdf.java,v 1.4 2002-05-29 18:31:32 steve Exp $
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
import ucar.multiarray.Accessor;
import java.util.Hashtable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This abstract class provides a skeletal implementation
 * of the Netcdf interface.
 * <p>
 * A minimal concrete implementation
 * would provide a concrete implementation of method
 * <code>Accessor ioFactory(ProtoVariable proto)</code>.
 * It would also provide a constructor which takes a Schema
 * argument and calls super(Schema) to get this class to
 * hook everything up.
 * <p>
 * TODO: There is a lot more to be said.
 *
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:32 $
 */

public abstract class
AbstractNetcdf
	implements Netcdf
{
 /* Begin Constructors */

	/**
	 * Create an empty instance.
	 * This may be incrementally populated using the protected
	 * put methods below. Use this constructor when you don't
	 * have all the ProtoVariables, Dimensions, and global
	 * Attributes available initially, such as when constructing
	 * from a stream.
	 */
	protected
	AbstractNetcdf()
	{
		ctor = VariableCtor();
		delegate = new Schema();
		variables = new Hashtable();
	}

	/**
	 * Create an empty instance to be populated with instances
	 * of some subclass of Variable.
	 *
	 * @param varClass  Class object for some subclass of Variable.
	 * 	The class must implement a constructor of the form
	 *	<code>myVar(ProtoVariable proto, Accessor io)</code>
	 *  or NoSuchMethodException will be thrown.
	 */
	protected
	AbstractNetcdf(Class varClass)
		throws NoSuchMethodException
	{
		ctor = varClass.getDeclaredConstructor(
				varCtorParameterTypes()
			);
		delegate = new Schema();
		variables = new Hashtable();
	}

	/**
	 * Create an instance populated with instances
	 * of Variable.
	 *
	 * @param sc   the Schema to use. N.B. Not a copy.
	 *   May be empty, shouldn't be null.
	 *
	 * @param init if true, call initHashtable()
	 */
	protected
	AbstractNetcdf(Schema sc, boolean init)
	{
		ctor = VariableCtor();
		delegate = sc;
		variables = new Hashtable(delegate.size());
		if(init)
		{
			try {
				initHashtable();
			}
			catch (InstantiationException ie)
			{
				// Can't happen: Variable is concrete
				throw new Error();
			}
			catch (IllegalAccessException iae)
			{
				// Can't happen: Variable is accessable
				throw new Error();
			}
			catch (InvocationTargetException ite)
			{
				// all the possible target exceptions are
				// RuntimeException
				throw (RuntimeException)
					ite.getTargetException();
			}
		}
	}


	/**
	 * Create an instance populated with instances
	 * of some subclass of Variable.
	 *
	 * @param sc  the Schema used as a template.
	 *   May be empty, shouldn't be null.
	 *
	 * @param init if true, call initHashtable()
	 *
	 * @param varClass  Class object for some subclass of Variable.
	 * 	The class must implement a constructor of the form
	 *	<code>myVar(ProtoVariable proto, Accessor io)</code>
	 *  or NoSuchMethodException will be thrown.
	 */
	protected
	AbstractNetcdf(Schema sc, boolean init, Class varClass)
		throws NoSuchMethodException,
			 InstantiationException,
			 InvocationTargetException,
			 IllegalAccessException
	{
		ctor = varClass.getDeclaredConstructor(
				varCtorParameterTypes()
			);
		delegate = new Schema(sc);
		variables = new Hashtable(delegate.size());
		if(init)
			initHashtable();
	}

 /* End Constructors */

	/**
	 * Returns the number of variables
	 * @return int number of variables
	 */
	public int
	size()
	{
		// assert(delegate.size() == variables.size();
		return variables.size();
	}

	/**
	 * Returns VariableIterator for the elements.
	 * @return VariableIterator for the elements.
	 * @see VariableIterator
	 */
	public VariableIterator
	iterator()
	{
		return new VariableIterator() {
			final ProtoVariableIterator iter = delegate.iterator();
			
	    		public boolean hasNext() {
				return iter.hasNext();
	    		}
	
			public Variable next() {
				return (Variable) variables.get(
					iter.next().getName());
			}
	
		};
	}

	/**
	 * Retrieve the variable associated with the specified name.
	 * @param name String which identifies the desired variable
	 * @return the variable, or null if not found
	 */
	public Variable
	get(String name)
	{
		return (Variable) variables.get(name);
	}
	
	
	/**
	 * Tests if the Variable identified by <code>name</code>
	 * is in this set.
	 * @param name String which identifies the desired variable
	 * @return <code>true</code> if and only if this set contains
	 * the named variable.
	 */
	public boolean
	contains(String name)
	{
		/*
		 * assert(delegate.contains(name)
		 *	 == variables.containsKey(name)();
		 */
		return variables.containsKey(name);
	}

	/**
	 * Tests if the argument is in this set.
	 * @param oo some Object
	 * @return <code>true</code> if and only if this set contains
	 * <code>oo</code>
	 */
	public boolean
	contains(Object oo)
	{
		return variables.contains(oo);
	}

	/**
	 * Returns the set of dimensions associated with this, 
	 * the union of those used by each of the variables.
	 *
	 * @return DimensionSet containing dimensions used
	 * by any of the variables. May be empty. Won't be null.
	 */
	public DimensionSet
	getDimensions()
		{ return delegate.getDimensions(); }

	/**
	 * Returns the set of attributes associated with this, 
	 * also know as the "global" attributes.
	 * 
	 * @return AttributeSet. May be empty. Won't be null.
	 */
	public AttributeSet
	getAttributes()
		{ return delegate.getAttributes(); }

	/**
	 * Convenience function; look up global Attribute by name.
	 *
	 * @param name the name of the attribute
	 * @return the attribute, or null if not found
	 */
	public Attribute
	getAttribute(String name)
		{ return delegate.getAttribute(name); }

	/**
	 * Format as CDL.
	 * @param buf StringBuffer into which to write
	 */
	public void
	toCdl(StringBuffer buf)
		{ delegate.toCdl(buf); }
		
	/**
	 * @return a CDL string of this.
	 */
	public String
	toString()
	{
	 	StringBuffer buf = new StringBuffer();
		toCdl(buf);
		return buf.toString();
	}

	/**
	 * Used to compute 'dimension index' needed 
	 * in netcdf version 1 files.
	 */
	int
	indexOf(Dimension dim)
		{ return delegate.indexOf(dim); }

 /* implementation */

	/**
	 * Used when creating variables to populate this.
	 * Override in your implementation to provide the
	 * correct i/o functionality.
	 */
	protected abstract Accessor
	ioFactory(ProtoVariable proto)
		throws InvocationTargetException;

	/**
	 * Used for incremental initialization.
	 * Add a Dimension to the Netcdf.
	 */
	protected void
	putDimension(Dimension dim)
		{ delegate.putDimension(dim); }

	/**
	 * Used for incremental initialization.
	 * Add a (global) attribute to the Netcdf.
	 */
	protected void
	putAttribute(Attribute attr) {
		delegate.putAttribute(attr);
	}

	/**
	 * Used for incremental initialization.
	 * Add a variable to the Netcdf.
	protected void
	put(ProtoVariable proto, Variable var)
	{
		if(!proto.getName().equals(var.getName()))
			throw new IllegalArgumentException(
					proto.getName()
					+ " != " + var.getName());
		delegate.put(proto);	
		variables.put(var.getName(),var);
	}
	 */

	/**
	 * Used for incremental initialization.
	 * Add a variable to the Netcdf.
	 */
	protected void
	add(ProtoVariable proto, Accessor io)
		throws InstantiationException,
			InvocationTargetException,
			IllegalAccessException
	{
		delegate.put(proto);	
		final Object [] args = {proto, io};
		final Variable var = (Variable) ctor.newInstance(args);
		variables.put(var.getName(),var);
	}

 /* */
	/**
	 * These are the parameter types used for the
	 * Variable constructor in <code>initHashtable(Class)</code>
	 */
	static final Class []
	varCtorParameterTypes()
	{
		try {
			Class [] parameterTypes = {
				Class.forName("ucar.netcdf.ProtoVariable"),
				Class.forName("ucar.multiarray.Accessor")
			};
			return parameterTypes;
		}
		catch (ClassNotFoundException cnfe)
		{
			// Shouldn't happen
			throw new Error(
				"ucar.netcdf implementation error");
		}
	}

	protected void
	initHashtable()
		throws InstantiationException,
			InvocationTargetException,
			IllegalAccessException
	{
		for(ProtoVariableIterator iter = delegate.iterator();
				iter.hasNext();)
		{
			final ProtoVariable proto = iter.next();
			final Accessor io = ioFactory(proto);
			final Object [] args = {proto, io};
			final Variable var = (Variable) ctor.newInstance(args);
			// assert(var.getName() == proto.getName());
			if(variables.put(var.getName(), var) != null)
				throw new IllegalArgumentException(
					"Duplicate variable name");
		}
		// assert(delegate.size() == variables.size();
	}

	static private Constructor
	VariableCtor()
	{
		try {
			final Class vc = Class.forName("ucar.netcdf.Variable");
			return vc.getDeclaredConstructor(
				varCtorParameterTypes()
			);
			
		}
		catch (ClassNotFoundException cnfe)
		{
			// Can't happen: ucar.netcdf.Variable exists
			throw new Error();
		}
		catch (NoSuchMethodException cnfe)
		{
			// Can't happen: ucar.netcdf.Variable has this ctor
			throw new Error();
		}
	}


	/* package */ Schema
	getSchema()
		{ return delegate; }
	
	final private Constructor ctor;
        final private Schema delegate;
	final private Hashtable variables;
}
