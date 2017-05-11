/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/

package visad.data.hdf5.hdf5objects;

import ncsa.hdf.hdf5lib.*;
import ncsa.hdf.hdf5lib.exceptions.*;

/**
 *  <p>
 *  This class provides provides a mechanism to describe the positions of the
 *  elements of a dataset.
 *  <p>
 *  A dataspace describes the locations that dataset elements are located at.
 *  A dataspace is either a regular N-dimensional array of data points, called
 *  a simple dataspace, or a more general collection of data points organized
 *  in another manner, called a complex dataspace.
 *  <P>
 *  For details of the HDF5 libraries, see the HDF5 Documentation at:
 *  <a href="http://hdf.ncsa.uiuc.edu/HDF5/doc/">http://hdf.ncsa.uiuc.edu/HDF5/doc/</a>
 */

public class HDF5Dataspace extends HDF5Object
{
	/** the rank of the dataset */
	private int rank;

	/** the dimensions of the dataset */
	private long[] dims;

	/** the maximum dimensions of the dataset */
	private long[] maxdims;

	/** the starting position of the selected subset */
	private long[] start;

	/** the stride of the selected subset */
	private long[] stride;

	/** the selected subset of the dataset */
	private long[] count;

	/** Constructs an HDF5Dataspace*/
	public HDF5Dataspace ()
	{
		super();

		type = DATASPACE;
	}

	/**
	 * Creates a new HDF5Dataspace
	 * @param space_type The type of dataspace to be created.
	 */
	public HDF5Dataspace (int space_type)
	{
		super();

		type = DATASPACE;

		try {
			id = H5.H5Screate(space_type);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Dataspace: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) {
			System.err.println("HDF5Dataspace.init(): "+e);
		}
	}

	/**
	 * Creates a new HDF5Dataspace
	 * @param rank Number of dimensions of dataspace.
	 * @param dims An array of the size of each dimension.
	 * @param maxdims An array of the maximum size of each dimension.
	 */
	public HDF5Dataspace (int rank, long[] dims, long[] maxdims)
	{
		super();

		type = DATASPACE;

		try {
			id = H5.H5Screate_simple(rank, dims, maxdims);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Dataspace: "+e);
			id = -1;
		}

		try { init(); }
		catch (HDF5Exception e) {
			System.err.println("HDF5Dataspace.init(): "+e);
		}
	}


	/** initialize the HDF5Dataspace:
	    <OL>
			<LI> open HDF5 library.
			<LI> Set up data ranks and dimensions.
			<LI> Set up start, stride and count.
		</OL>
	 */
	public void init () throws HDF5Exception
	{
		if (id < 0) return;

		rank = H5.H5Sget_simple_extent_ndims(id);
		dims = new long[rank];
		maxdims = new long[rank];
		start = new long[rank];
		stride = new long[rank];
		count = new long[rank];

		for (int i=0; i<rank; i++) {
			start[i] = 0;
			stride[i] = 1;
		}

		int status = H5.H5Sget_simple_extent_dims(id, dims, maxdims);
		if (status < 0) return;

		for (int i=0; i<rank; i++)
			count[i] = dims[i];
	}

	/** select count[] points starting at start[] with stride[]
	 *  @param start the starting points.
	 *  @param stride the stride
	 *  @param count the number of points.
	 */
	public void select(long[] start, long[] stride, long[] count)
	throws HDF5LibraryException, NullPointerException, IllegalArgumentException
	{
		this.start = start;
		this.stride = stride;
		this.count = count;

		long[] block = new long[rank];
		for (int i=0; i<rank; i++)
			block[i] = 1;

		try {
			H5.H5Sselect_hyperslab(id, HDF5Constants.H5S_SELECT_SET, start,stride, count, block);
		}
		catch (HDF5Exception exc) {
			HDF5LibraryException e = new HDF5LibraryException();
			e.initCause(exc);
			throw e;
		}
	}


	/** Returns the rank of the dataspace */
	public int getRank() { return rank; }

	/** Returns the dimensions of the dataspace */
	public long[] getDims() { return dims; }

	/** Returns the maximum dimensions of the dataspace */
	public long[] getMaxdims() { return maxdims; }

	/** Returns the selected counts of the data */
	public long[] getCount() { return count; }

	/**
	 * Converts this object to a String representation.
	 * @return a string representation of this object
	 */
	public synchronized String toString() {
		String d_str="";

		for (int i=0; i<rank; i++)
			d_str += dims[i]+"x";

		int l = d_str.length();
		if (l > 1) d_str = d_str.substring(0, l-1);

		return getClass().getName() + "[rank=" + rank + ",dimensions=" + d_str + "]";
	}

	/**
	 * finalize() is called by the garbage collector on the object when garbage
	 * collection determines that there are no more references to the object. It
	 * is used to dispose of system resources or to perform other cleanup as C++
	 * destructors
	 */
	protected void finalize() throws Throwable
	{
		try { super.finalize(); }
		finally { H5.H5Sclose(id); }
	}
}


