package visad.data.in;

import java.io.*;
import java.security.MessageDigest;

/**
 * Instances are immutable.
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

    public MD5Key(Object obj)
	throws IOException
    {
	this(new Object[] {obj});
    }

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

    public boolean equals(Object obj)
    {
	boolean	equals;
	if (!(obj instanceof MD5Key))
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

    public int hashCode()
    {
	return checksum.hashCode();
    }
}
