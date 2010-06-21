// $Id: IndexMap.java,v 1.2 2002-05-29 20:32:39 steve Exp $
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

/**
 * This interface defines the services required by
 * MultiArrayProxy to manipulate indexes and the dimensions
 * of a MultiArray. There are two transformations represented.
 * Each goes from <code>int []</code> to <code>int []</code>.
 * (The <code>int</code> values should actually be non-negative,
 * as array indexes or array sizes.)
 * <p>
 * The transform most often used takes a MultiArrayProxy index
 * and returns an index suitable for accessing the hidden 'backing'
 * MultiArray. The
 * <code>int [] transform(int [] output, int [] input)</code>
 * does this. Refer to this as the forward transform.
 * The <code>setInput(int [] input)</code> procedure binds the
 * input array reference to the forward transformation. The transformation
 * of values in the input array is obtained by calling
 * <code>int [] getTransformed(int [] output)</code>.
 * Note that the reference to input is bound, not a copy.
 * This allows changes in values of the input array to be
 * reflected in subsequent calls to <code>getTransformed</code>
 * with needing to call <code>setInput</code> again.
 * <p>
 * The other transform is used to determine the proxy shape.
 * It takes the shape of the backing MultiArray as input.
 * It goes in the opposite direction as the forward transform;
 * refer to this as the reverse transform. (Note: it is not
 * an inverse.) The
 * <code>setLengths(int [] lengths)</code>
 * is analogous to <code>setInput()</code> above. It is typically
 * called once during MultiArrayProxy initialization.
 * The function 
 * <code>int [] getLengths(int [] output)</code> is analogous to
 * <code>getTransformed</code>. It is used to implement the
 * proxy getLengths() method.
 *
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:39 $
 */

public interface
IndexMap
{
	/**
	 * Return the length needed for an output vector.
	 * Will throw an exception if called before <code>setInput()</code>.
	 */
	public int
	getOutputLength();

	/**
	 * Rebind the domain of <code>getTransformed()</code>
	 * @param input int array domain reference member.
	 */
	void
	setInput(int [] input);

	/**
	 * Transform the current input, placing the results in
	 * <code>output</output>.
	 * @param output int array storage for the result.
	 * 	The elements of <code>output</output> are usually
	 *	modified by this call.
	 * @return output
	 */
	int []
	getTransformed(int [] output);

	/**
	 * Perform the forward transform.
	 * <p>
	 * This function is equivalent to
	 * <code>
	 *	setInput(input);
	 *	return getTransformed(output);
	 * </code>
	 *
	 * @param output int array storage for the result.
	 * 	The elements of <code>output</output> are usually
	 *	modified by this call.
	 * @param input int array which is the index to be transformed.
	 * @return output
	 */ 
	int []
	transform(int [] output, int [] input);

	/**
	 * Return the length of input vectors.
	 * Will throw an exception if called before <code>setLengths()</code>.
	 */
	int
	getRank();

	/**
	 * Initialize or reinitialize the IndexMap.
	 * Binds the domain of <code>getLengths()</code>,
	 * <code>getRank()</code>.
	 * @param lengths int array representing the shape on the forward
	 * transform output.
	 */
	void
	setLengths(int [] lengths);

	/**
	 * Reverse transform the lengths, placing the results in
	 * <code>output</output>.
	 * Will throw an exception if called before <code>setLengths()</code>.
	 *
	 * @param output int array storage for the result.
	 * 	The elements of <code>output</output> are usually
	 *	modified by this call.
	 * @return output
	 */
	int []
	getLengths(int [] output);
}
