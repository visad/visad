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

import java.util.Vector;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;


/**
 *  <p>
 *  This class is a container for the parameters to the HDF5 Group Object.
 *  <P>
 *  HDF5 group is a grouping structure containing instances of zero or more
 *  groups or datasets, together with supporting metadata.
 *  <P>
 *  Working with groups and group members is similar in many ways to working
 *  with directories and files in UNIX. As with UNIX directories and files,
 *  objects in an HDF5 file are often described by giving their full (or
 *  absolute) path names
 *  <p>
 *  For details of the HDF5 libraries, see the HDF5 Documentation at:
 *  <a href="http://hdf.ncsa.uiuc.edu/HDF5/doc/">http://hdf.ncsa.uiuc.edu/HDF5/doc/</a>
 */

public class HDF5Group extends HDF5Object
{
	/** members of the group */
	protected Vector members;

	/** the parent group */
	protected HDF5Group parent;

	/** Constructs an HDF5Group */
	public HDF5Group()
	{
		super();

		type = GROUP;
		members = new Vector();
	}

	/** Creates a new HDF5 Group.
	 *  @param loc_id The file or group identifier.
	 *  @param gname The absolute or relative name of the new group.
	 *  @param name_length The maximum length of the name.
	 */
	public HDF5Group(int loc_id, String gname, int name_length)
	{
		super(gname);

		type = GROUP;
		members = new Vector();

		if (name_length <=0)
			name_length = gname.length();

		try {
			id = H5.H5Gcreate(loc_id, name, name_length);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Group: "+e);
			id = -1;
		}
	}

	/** Opens an existing HDF5 Group.
	 *  @param loc_id The file or group identifier..
	 *  @param name The absolute or relative name of the new group..
	 */
	public HDF5Group(int loc_id, String name)
	{
		super(name);

		type = GROUP;
		members = new Vector();

		try {
			id = H5.H5Gopen(loc_id, name);
		} catch (HDF5Exception e) {
			System.err.println("HDF5Group: "+e);
			id = -1;
		}
	}

	/** Sets the parent of this group
	 *  @param p the parent of the HDF5Group
	 */
	public void setParent(HDF5Group p)
	{
		this.parent = p;
	}

	/** Tests if the specified object is a member of this group.
	 *
	 *  @param member a member
	 *  @return true if the specified object is a member of the group;
	 *  false otherwise.
	 */
	public boolean contains(Object member)
	{
		return members.contains(member);
	}

	/** Adds a new member to the group.
	 *  @param member the new member to be added to the group
	 */
	public void addMember(Object member)
	{
		if (!contains(member))
			members.addElement(member);
	}

	/** Deletes the component at the specified index. */
	public void removeMemberAt(int index) {
		members.removeElementAt(index);
	}

	/** Removes the member from this group
	 *  @param member the member to be removed.
	 *  @return true if the member was a component of this vector;
	 *  false otherwise.
	 */
	public boolean removeMember(Object member) {
		return members.removeElement(member);
	}

	/** Returns the members of the group */
	public Vector getMembers() {
		return members;
	}

	/** Returns the parent of the group */
	public HDF5Group getParent() {
		return parent;
	}

	/** Returns the member at index memberIndex
	 *  @param memberIndex the index of the group member
	 */
	public Object getMemberAt(int memberIndex) {
		return members.elementAt(memberIndex);
	}

	/** Returns the number of members the HDF5Group contains */
	public int getMemberCount() {
		if (members == null)
			return -1;
		else
			return members.size();
	}

	/** Returns true if the HDF5Group has no member */
	public boolean isEmpty() {
		return (members == null || members.size()<=0);
	}

	/** Returns true if the HDF5Group is the root group */
	public boolean isRoot() {
		return (parent == null);
	}

	/**
	 * Converts this object to a String representation.
	 * @return a string representation of this object
	 */
	public synchronized String toString() {
		return getClass().getName() + "[name=" + name+
			",members=" + members.toString()+"]";
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
		finally { H5.H5Gclose(id); }
	}

}


