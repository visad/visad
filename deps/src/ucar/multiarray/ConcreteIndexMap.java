// $Id: ConcreteIndexMap.java,v 1.3 2002-05-29 20:32:38 steve Exp $
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
package ucar.multiarray;
import java.lang.reflect.Array; // used by ZZMap

/**
 * Base class which provides framework for implementations of
 * IndexMap. This class contains two instances of the inner class ZZMap,
 * which are used to implement the two transformations required by IndexMap.
 * <p>
 * This class also supports a functional composition framework via
 * the link() initializer.
 * <p>
 * All methods except contructors are final.
 * Specialization in subclasses occurs by subclassing
 * the ZZMap inner class.
 * Subclasses provide different ZZMaps during construction.
 * <p>
 * This specialization strategy results in an implementation oddity.
 * When instances of a subclass of this are being constructed,
 * this class's constructor (as super(...)) must complete before
 * instances of the subclass inner class may be created.
 * "Can't reference this before the superclass constructor
 *  has been called." So, rather that initializing the ZZMap
 *  members contained by this in the constructors, we provide
 *  init() and link() members functions. One or the other of
 *  these should be called by every subclass constructor.
 *
 * @author $Author: steve $
 * @version $Revision: 1.3 $ $Date: 2002-05-29 20:32:38 $
 */
public class
ConcreteIndexMap
	implements IndexMap
{

/**
 * An Map of int by int key. (Z is the math symbol for integers).
 * A Map maps keys to values.
 * A Map cannot contain duplicate keys;
 * each key can map to at most one value.
 * For this implementation, the keys are restricted to non-negative
 * integers, and we only use non-negative integers as values.
 * <p>
 * A ZZMap is like a readonly 1-d array of int.
 * The <code>size()</code> method returns the array length.
 * The <code>get(int ii)</code> method returns the int stored at
 * position <code>ii</code>;
 * <p>
 * This class also supports a functional composition framework via
 * the setPrev() method. The implementation of get(int) and size()
 * provided here is simply the identity composed with whatever.
 * Subclasses will override this functionality.
 *
 */
protected class
ZZMap
{
	/**
	 */
	protected
	ZZMap()
	{
		rebind((int []) null);
	}

	/**
	 * Construct a ZZMap form the functional composition
	 * of the new Map with another.
	 * rebind(int [] range) calls prev.rebind(range),
	 * get(int key) is composed as get(prev.get(key))
	 * @param prev ZZMap this is composed with.
	 */
	protected
	ZZMap(ZZMap prev)
	{
		setPrev(prev);
	}

	/**
	 * Returns the value to which this Map maps the specified key.
	 * If you think of this as a 1-d  array of int, then
	 * ia.get(ii) is like ia[ii].
	 * @param key int
	 * @return int value
	 */
	synchronized int
	get(int key)
	{
		if(prev_ instanceof ZZMap)
			return ((ZZMap)prev_).get(key);
		// else
		try {
			return Array.getInt(prev_, key);
		}
		catch (RuntimeException ex) {
			/*
			 * rebind() was not called (ex isa NullPointerException)
			 * or called with a value which doesn't make sense for
			 * the key (ex isa ArrayIndexOutOfBoundsException)
			 */
			throw new IllegalArgumentException("Improper Binding");
		}
	}

	/**
	 * Rebind (redefine) the range of get(int)
	 * @param range int array which defines the get(int)
	 * member.
	 */
	synchronized final void
	rebind(int [] range)
	{
		if(prev_ instanceof ZZMap)
		{
			((ZZMap)prev_).rebind(range);
			return;
		}
		// else
		prev_ = range;
	}

	/**
	 * Returns the number of key-value mappings in this Map.
	 * If you think of this as a 1-d  array of int, then
	 * ia.size() is like ia.length.
	 * @return int size
	 */
	synchronized int
	size()
	{
		if(prev_ instanceof ZZMap)
			return ((ZZMap)prev_).size();
		// else
		try {
			return Array.getLength(prev_);
		}
		catch (NullPointerException npe) {
			return 0;
		}
	}

	/**
	 * Form the functional composition
	 * of this Map with another.
	 * rebind(int [] range) calls prev.rebind(range),
	 * get(int key) is composed as get(prev.get(key))
	 * @param prev ZZMap this is composed with.
	 */
	synchronized final void
	setPrev(ZZMap prev)
	{
		if(prev_ instanceof ZZMap)
		{
			((ZZMap)prev_).setPrev(prev);
			return;
		}
		// else
		prev_ = prev;
	}

	/**
	 * The range of the get(int) function.
	 * Either an array of ints or another ZZMap.
	 */
	private Object prev_;

	public String
	toString()
	{
		StringBuffer buf = new StringBuffer(
	    		super.toString()
		);
		final int sz = size();
		buf.append(" [");
		buf.append(sz);
		buf.append("]");
		buf.append(" {");
		final int last = sz -1;
		for(int ii = 0; ii < sz ; ii++)
		{
			buf.append(get(ii));
			if(ii == last)
				break; // normal loop exit
			buf.append(", ");
		}
		buf.append("}");
		return buf.toString();
	}

} /* End Inner Class ZZMap */

	/**
	 * Only constructor is protected.
	 * This is a base class, clients only
	 * create instances of the subclasses.
	 */
	protected
	ConcreteIndexMap() {}


	/**
	 * Called by subclass constructors to initialize.
	 * Used for standalone or "leaf" instances.
	 * See "implementation oddity" above.
	 * @param iMap ZZMap defining the forward transform.
	 * @param lengthsMap ZZMap defining the reverse transform.
	 */
	protected final void
	init(ZZMap iMap, ZZMap lengthsMap)
	{
		iMap_ = iMap;
		lengthsMap_ = lengthsMap;
	}

	/**
	 * Called by subclass constructors to initialize.
	 * Used for standalone or "leaf" instances when
	 * the reverse transformation (lengthsMap) is the
	 * identity.
	 * See "implementation oddity" above.
	 * @param iMap ZZMap defining the forward transform.
	 */
	protected final void
	init(ZZMap iMap)
	{
		init(iMap, new ZZMap());
	}

	/**
	 * Called by subclass constructors to initialize.
	 * Used when nested constructors are used to form
	 * functional composition of IndexMaps.
	 * See "implementation oddity" above.
	 * @param prev ConcreteIndexMap this is composed with.
	 * @param iMap ZZMap defining the forward transform.
	 * @param lengthsMap ZZMap defining the reverse transform.
	 */
	protected final void
	link(ConcreteIndexMap prev, ZZMap iMap, ZZMap lengthsMap)
	{
		iMap_ = prev.iMap_;
		lengthsMap_ = lengthsMap;

		iMap_.setPrev(iMap);
		lengthsMap_.setPrev(prev.lengthsMap_);
	}

	/**
	 * Called by subclass constructors to initialize.
	 * Used when nested constructors are used to form
	 * functional composition of IndexMaps.
	 * This form is used when the reverse transform (lengthsMap)
	 * is the identity
	 * See "implementation oddity" above.
	 * @param prev ConcreteIndexMap this is composed with.
	 * @param iMap ZZMap defining the forward transform.
	 */
	protected final void
	link(ConcreteIndexMap prev, ZZMap iMap)
	{
		link(prev, iMap,
			 new ZZMap()); // TODO: can we use prev.lengthsMap_?
	}

/* Begin IndexMap impl */

	public final synchronized int
	getOutputLength()
	{
		return iMap_.size();
	}

	public final synchronized void
	setInput(int [] input)
	{
		iMap_.rebind(input);
	}

	public final synchronized int []
	getTransformed(int [] output)
	{
		final int sz = getOutputLength();
		for(int ii = 0; ii < sz; ii++)
			output[ii] = iMap_.get(ii);
		return output;
	}

	public final synchronized int []
	transform(int [] output, int [] input)
	{
		setInput(input);
		return getTransformed(output);
	}

	public final synchronized int
	getRank()
	{
		return lengthsMap_.size();
	}

	public final synchronized void
	setLengths(int [] lengths)
	{
		lengthsMap_.rebind(lengths);
		if(getRank() < 0)
			throw new IllegalArgumentException("rank < 0");
	}

	public final synchronized int []
	getLengths(int [] output)
	{
		final int sz = lengthsMap_.size();
		for(int ii = 0; ii < sz; ii++)
			output[ii] = lengthsMap_.get(ii);
		return output;
	}

 /* End IndexMap Impl */

	public String
	toString()
	{
		StringBuffer buf = new StringBuffer(
	    		super.toString() + "\n\t"
		);
		buf.append(iMap_.toString() + "\n\t");
		buf.append(lengthsMap_.toString());
		return buf.toString();
	}

	/*
	 * Implementation note. See "implementation oddity" above.
         * "Can't reference this before the superclass constructor
	 *  has been called."
	 * ==> Can't be final.
	 */
	/**
	 * Supports the forward tranform.
	 */
	protected /* final */ ZZMap iMap_;
	/**
	 * Supports the reverse tranform.
	 */
	protected /* final */ ZZMap lengthsMap_;

 /* Begin Test */
	private static void
	testZZMap()
	{
		System.out.println("Testing Inner Class ZZMap");
		ConcreteIndexMap im = new ConcreteIndexMap();

		System.out.println("Unbound:");
		ZZMap zm = im. new ZZMap();
		System.out.println("\t" + zm);
		ZZMap next = im. new ZZMap(zm);
		System.out.println("\t" + next);

		System.out.println("Bernoulli");
		int [] ia = {1, 1, 2, 3, 5, 8, 13};
		zm.rebind(ia);
		System.out.println("\t" + zm);
		System.out.println("\t" + next);

		System.out.println("Rebound");
		int [] ia2 = {1, 2, 4, 8};
		next.rebind(ia2);
		System.out.println("\t" + zm);
		System.out.println("\t" + next);

		System.out.println("End ZZMap Test");
	}

	private static void
	testInit()
	{
		System.out.println("Testing init() and link() ");
		ConcreteIndexMap im = new ConcreteIndexMap();
		im.init(im. new ZZMap(), im. new ZZMap());
		System.out.println("Unbound:        " + im);
		ConcreteIndexMap next = new ConcreteIndexMap();
		next.link(im, next. new ZZMap(), next .new ZZMap());
		System.out.println("Next Unbound:   " + next);

		int [] ia = {1, 1, 2, 3, 5, 8, 13};
		int [] ia2 = {1, 2, 4, 8};
		next.setInput(ia);
		next.setLengths(ia2);
		System.out.println("forward  :      " + im);
		System.out.println("Next forward  : " + next);

		next.setInput(ia2);
		next.setLengths(ia);
		System.out.println("reversed:       " + im);
		System.out.println("Next reversed:  " + next);

		System.out.println("End init(), link() test");
	}

	public static void
	main(String[] args)
	{
		testZZMap();
		testInit();
		// TODO more complete
	}

 /* Test output java ucar.multiarray.ConcreteIndexMap
Testing Inner Class ZZMap
Unbound:
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce7bc [0] {}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce7ec [0] {}
Bernoulli
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce7bc [7] {1, 1, 2, 3, 5, 8, 13}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce7ec [7] {1, 1, 2, 3, 5, 8, 13}
Rebound
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce7bc [4] {1, 2, 4, 8}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce7ec [4] {1, 2, 4, 8}
End ZZMap Test
Testing init() and link()
Unbound:        ucar.multiarray.ConcreteIndexMap@8ce890
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88f [0] {}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88e [0] {}
Next Unbound:   ucar.multiarray.ConcreteIndexMap@8ce8ac
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88f [0] {}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce8aa [0] {}
forward  :      ucar.multiarray.ConcreteIndexMap@8ce890
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88f [7] {1, 1, 2, 3, 5, 8, 13}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88e [4] {1, 2, 4, 8}
Next forward  : ucar.multiarray.ConcreteIndexMap@8ce8ac
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88f [7] {1, 1, 2, 3, 5, 8, 13}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce8aa [4] {1, 2, 4, 8}
reversed:       ucar.multiarray.ConcreteIndexMap@8ce890
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88f [4] {1, 2, 4, 8}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88e [7] {1, 1, 2, 3, 5, 8, 13}
Next reversed:  ucar.multiarray.ConcreteIndexMap@8ce8ac
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce88f [4] {1, 2, 4, 8}
	ucar.multiarray.ConcreteIndexMap$ZZMap@8ce8aa [7] {1, 1, 2, 3, 5, 8, 13}
End init(), link() test
  */
 /* End Test */
}
