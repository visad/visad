package dods.clients.matlab;

import dods.dap.*;
import java.lang.*;
import java.io.*;

public class MatlabURL extends DURL {

    public MatlabURL() {
	super();
    }

    public MatlabURL(String name) {
	super(name);
    }

    // This function had to be included because a bug in jdk1.1.8v3 
    // (included with matlab6 for linux) prevents DString::deserialize()
    // from working correctly.
    public synchronized void deserialize(DataInputStream source,
					 ServerVersion sv,
					 StatusUI statusUI)
	throws IOException, EOFException, DataReadException {
	int len = source.readInt();
	if (len < 0)
	    throw new DataReadException("Negative string length read.");
	int modFour = len%4;
	// number of bytes to pad
	int pad = (modFour != 0) ? (4-modFour) : 0;
	
	byte byteArray[] = new byte[len];
	
	// With blackdown JDK1.1.8v3 (comes with matlab 6) read() didn't always
	// finish reading a string.  readFully() insures that we get all <len>
	// characters we requested.  rph 06/21/01.
	
	//source.read(byteArray, 0, len);
	source.readFully(byteArray, 0, len);
	
	// pad out to a multiple of four bytes
	byte unused;
	for(int i=0; i<pad; i++)
	    unused = source.readByte();
	
	if(statusUI != null)
	    statusUI.incrementByteCount(4 + len + pad);
	
	// convert bytes to a new String using ISO8859_1 (Latin 1) encoding.
	// This was chosen because it converts each byte to its Unicode value
	// with no translation (the first 256 glyphs in Unicode are ISO8859_1)
	try {
	    setValue(new String(byteArray, 0, len, "ISO8859_1"));
	}
	catch (UnsupportedEncodingException e) {
	    // this should never happen
	    System.err.println("ISO8859_1 encoding not supported by this VM!");
	    System.exit(1);
	}
    }
}
