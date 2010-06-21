/****************************************************************************
 * NCSA HDF5                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/

package ncsa.hdf.hdf5lib.exceptions;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

/**
 *  <p>
 *  The class HDF5LibraryException returns errors raised by the HDF5
 *  library.
 *  <p>
 *  Each major error code from the HDF-5 Library is represented
 *  by a sub-class of this class, and by default the 'detailedMessage'
 *  is set according to the minor error code from the HDF-5 Library.
 *  <p>
 *  For major and minor error codes, see <b>H5Epublic.h</b> in the HDF-5
 *  library.
 *  <p>
 */


public class HDF5LibraryException extends HDF5Exception {

	/**
	 * Constructs an <code>HDF5LibraryException</code> with 
	 * no specified detail message.
	 */
	 public HDF5LibraryException() {
		super();

		// this code forces the loading of the HDF-5 library
		// to assure that the native methods are available
		try { H5.H5open(); } catch (Exception e) {};

		detailMessage = getMinorError(getMinorErrorNumber());
	}

	/**
	 * Constructs an <code>HDF5LibraryException</code> with 
	 * the specified detail message.
	 *
	 * @param   s   the detail message.
	 */
	public HDF5LibraryException(String s) {
		super(s);
		// this code forces the loading of the HDF-5 library
		// to assure that the native methods are available
		try { H5.H5open(); } catch (Exception e) {};
	}

	/**
	 * Get the major error number of the first error on the 
	 * HDF5 library error stack.
	 *
	 * @return the major error number
	 */
	public native int getMajorErrorNumber();

   	/**
	 * Get the minor error number of the first error on the 
	 * HDF5 library error stack.
	 *
	 * @return the minor error number
	 */
	public native int getMinorErrorNumber();

    /**
     *  Return a error message for the minor error number.
     * <p>
     *  These messages come from <b>H5Epublic.h</b>.
     *
     *  @param min_num the minor error number
     *
     *  @return the string of the minor error
     */
	public String getMinorError(int min_num)
	{
		switch (min_num)
		{
			case HDF5Constants.H5E_NONE_MINOR:
				return "no error";
			case HDF5Constants.H5E_UNINITIALIZED:
				return "information is unitialized";
			case HDF5Constants.H5E_UNSUPPORTED:
				return "feature is unsupported";
			case HDF5Constants.H5E_BADTYPE:
				return "incorrect type found";
			case HDF5Constants.H5E_BADRANGE:
				return "argument out of range";
			case HDF5Constants.H5E_BADVALUE:
				return "bad value for argument";
			case HDF5Constants.H5E_NOSPACE:
				return "no space available for allocation";
			case HDF5Constants.H5E_CANTCOPY:
				return "unable to copy object";
			case HDF5Constants.H5E_FILEEXISTS:
				return "file already exists";
			case HDF5Constants.H5E_FILEOPEN:
				return "file already open";
			case HDF5Constants.H5E_CANTCREATE:
				return "Can't create file";
			case HDF5Constants.H5E_CANTOPENFILE:
				return "Can't open file";
			case HDF5Constants.H5E_NOTHDF5:
				return "not an HDF5 format file";
			case HDF5Constants.H5E_BADFILE:
				return "bad file ID accessed";
			case HDF5Constants.H5E_TRUNCATED:
				return "file has been truncated";
			case HDF5Constants.H5E_MOUNT:
				return "file mount error";
			case HDF5Constants.H5E_SEEKERROR:
				return "seek failed";
			case HDF5Constants.H5E_READERROR:
				return "read failed";
			case HDF5Constants.H5E_WRITEERROR:
				return "write failed";
			case HDF5Constants.H5E_CLOSEERROR:
				return "close failed";
			case HDF5Constants.H5E_OVERFLOW:
				return "address overflowed";
			case HDF5Constants.H5E_CANTINIT:
				return "Can't initialize";
			case HDF5Constants.H5E_ALREADYINIT:
				return "object already initialized";
			case HDF5Constants.H5E_BADATOM:
				return "Can't find atom information";
			case HDF5Constants.H5E_CANTREGISTER:
				return "Can't register new atom";
			case HDF5Constants.H5E_CANTFLUSH:
				return "Can't flush object from cache";
			case HDF5Constants.H5E_CANTLOAD:
				return "Can't load object into cache";
			case HDF5Constants.H5E_PROTECT:
				return "protected object error";
			case HDF5Constants.H5E_NOTCACHED:
				return "object not currently cached";
			case HDF5Constants.H5E_NOTFOUND:
				return "object not found";
			case HDF5Constants.H5E_EXISTS:
				return "object already exists";
			case HDF5Constants.H5E_CANTENCODE:
				return "Can't encode value";
			case HDF5Constants.H5E_CANTDECODE:
				return "Can't decode value";
			case HDF5Constants.H5E_CANTSPLIT:
				return "Can't split node";
			case HDF5Constants.H5E_CANTINSERT:
				return "Can't insert object";
			case HDF5Constants.H5E_CANTLIST:
				return "Can't list node";
			case HDF5Constants.H5E_LINKCOUNT:
				return "bad object header link count";
			case HDF5Constants.H5E_VERSION:
				return "wrong version number";
			case HDF5Constants.H5E_ALIGNMENT:
				return "alignment error";
			case HDF5Constants.H5E_BADMESG:
				return "unrecognized message";
			case HDF5Constants.H5E_CANTDELETE:
				return "Can't delete message";
			case HDF5Constants.H5E_CANTOPENOBJ:
				return "Can't open object";
			case HDF5Constants.H5E_COMPLEN:
				return "name component is too long";
			case HDF5Constants.H5E_CWG:
				return "problem with current working group";
			case HDF5Constants.H5E_LINK:
				return "link count failure";
			case HDF5Constants.H5E_SLINK:
				return "symbolic link error";
			case HDF5Constants.H5E_MPI:
				return "some MPI function failed";
			default:
				return "undefined error";
		}
	}


	/**
	 * Prints this <code>HDF5LibraryException</code>,
	 * the HDF-5 Library error stack, and
	 * and the Java stack trace to the standard error stream.
	 */
	public void printStackTrace() {
		System.err.println(this);
		printStackTrace0(null); // the HDF-5 Library error stack
		super.printStackTrace(); // the Java stack trace
	}

	/**
	 * Prints this <code>HDF5LibraryException</code> 
	 * the HDF-5 Library error stack, and
	 * and the Java stack trace to the 
	 * specified print stream. 
	 *
	 */
	public void printStackTrace(java.io.File f) {
		if (f==null || !f.exists() || f.isDirectory() || !f.canWrite())
			printStackTrace();
		else
		{
		    try {
			java.io.FileOutputStream o = new java.io.FileOutputStream(f);
			java.io.PrintWriter p = new java.io.PrintWriter(o);
			p.println(this);
			p.close();
		    } catch (Exception ex) {
			System.err.println(this);
		    };
		    // the HDF-5 Library error stack
		    printStackTrace0(f.getPath()); 
		    super.printStackTrace(); // the Java stack trace
		}
	}

	/*
	 *  This private method calls the HDF-5 library to extract
	 *  the error codes and error stack.
	 */
	private native void printStackTrace0(String s);

}
