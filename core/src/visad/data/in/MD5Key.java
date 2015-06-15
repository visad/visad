/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

$Id: MD5Key.java,v 1.9 2009-03-02 23:35:48 curtis Exp $
*/

package visad.data.in;

import java.io.*;
import java.security.MessageDigest;

/**
 * Provides support for computing an MD5 key for an object.  Such a key may be
 * used to obtain a hash code for an object and also supports implementation of
 * a fast {@link #equals(Object)} method for objects whose natural method might
 * be computationally expensive.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class MD5Key
{
    private final byte[]		checksum;

    private static MessageDigest	digester;

    static
    {
	try
	{
	    digester = MessageDigest.getInstance("MD5");
	}
	catch (Exception e)
	{
	    System.err.println(
		"MD5Key.<clinit>: Couldn't initialize class: " + e);
	    System.exit(1);
	}
    }

    /**
     * Constructs from an object.
     *
     * @param obj		The object to have an MD5 checksum computed for
     *				it.
     * @throws IOException	Couldn't compute MD5 checksum.
     */
    public MD5Key(Object obj)
	throws IOException
    {
	this(new Object[] {obj});
    }

    /**
     * Constructs from an array of objects.
     *
     * @param objs		The objects to have an MD5 checksum computed for
     *				them.
     * @throws IOException      Couldn't compute MD5 checksum.
     */
    public MD5Key(Object[] objs)
	throws IOException
    {
	ByteArrayOutputStream	byteArrayOutputStream =
	    new ByteArrayOutputStream();
	ObjectOutputStream	objectOutputStream =
	    new ObjectOutputStream(byteArrayOutputStream);
	for (int i = 0; i < objs.length; ++i)
	    objectOutputStream.writeObject(objs[i]);
	objectOutputStream.flush();
	byteArrayOutputStream.flush();
	digester.update(byteArrayOutputStream.toByteArray());
	checksum = digester.digest();
	objectOutputStream.close();
	byteArrayOutputStream.close();
    }

    /**
     * Indicates if this instance is semantically identical to another object.
     * This will be the case if the other object is also of this class and its
     * MD5 checksum is identical to this instances MD5 checksum.
     *
     * @param obj		The other object.
     * @return			<code>true</code> if and only if this instance
     *				is semantically identical to the other object.
     */
    public boolean equals(Object obj)
    {
	boolean	equals;
	if (!getClass().isInstance(obj))
	{
	    equals = false;
	}
	else
	{
	    MD5Key	that = (MD5Key)obj;
	    equals = this == that || checksum.equals(that.checksum);
	}
	return equals;
    }

    /**
     * Returns the hash code of this instance.  The hash code is the hash code
     * of the MD5 checksum.
     *
     * @return			The hash code of this instance.
     */
    public int hashCode()
    {
	return checksum.hashCode();
    }
}
