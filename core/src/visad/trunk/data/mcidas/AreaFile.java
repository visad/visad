package visad.data.mcidas;

import java.applet.Applet;
import java.io.*;
import java.lang.*;
import java.net.MalformedURLException;
import java.net.URL;

/** 
 * AreaFile interface with McIDAS 'area' file format image data.
 * This will allow 'area' format data to be read from disk; the
 * navigation block is made available (see navGVAR for example).
 *
 * This implementation does not do calibration (other than
 * accounting for its presence in the data).  Also, the 'valcode'
 * is not checked on each line.
 *
 * @authors - Tom Whittaker & Tommy Jasmin at SSEC
 * 
 */

public class AreaFile {
  private boolean flipwords;
  private boolean fileok;
  private boolean hasReadData = false;
  private DataInputStream af;
  private int status=0;
  private int navLoc, calLoc, auxLoc, datLoc;
  private int navbytes, calbytes, auxbytes;
  private int linePrefixLength, lineDataLength, lineLength, numberLines;
  private final int McMISSING = 0x80808080;
  private long position;
  private int skipByteCount;
  private long newPosition;
  private URL url;
  private int numBands;
  int[] dir;
  int[] nav;
  int[] cal;
  int[][][] data;
  final int DMSP = 0x444d5250;
  final int GVAR = 0x47564152;
  final int POES = 0x5449524f;
  
  /**
   * creates an AreaFile object that allows reading
   * of McIDAS 'area' file format image data
   *
   * @param filename the disk filename (incl path) to read from
   *
   * @exception AreaFileException if file cannot be opened
   *
   */

  public AreaFile(String filename) throws AreaFileException {

    try { 
      af = new DataInputStream (
        new BufferedInputStream(new FileInputStream(filename), 2048) 
      );
    } catch (IOException e) {
      fileok = false;
      throw new AreaFileException("Error opening AreaFile: " + e);
    }
    fileok=true;
    position = 0;
    readMetaData();
  }

  /**
   * creates an AreaFile object that allows reading
   * of McIDAS 'area' file format image data from an applet
   *
   * @param filename the disk filename (incl path) to read from
   * @param parent the parent applet 
   *
   * @exception AreaFileException if file cannot be opened
   *
   */

  public AreaFile(String filename, Applet parent) throws AreaFileException {

    try {
      url = new URL(parent.getDocumentBase(), filename);
    } catch (MalformedURLException e) {
      System.out.println(e);
    }

    try { 
      af = new DataInputStream(url.openStream());
    } catch (IOException e) {
	fileok = false;
	throw new AreaFileException("Error opening AreaFile:"+e);
    }
    fileok=true;
    position = 0;
    readMetaData();
 
  }

  /**
   * creates an AreaFile object that allows reading
   * of McIDAS 'area' file format image data from a URL
   *
   * @param URL - the URL to go after
   *
   * @exception AreaFileException if file cannot be opened
   *
   */

  public AreaFile(URL url) throws AreaFileException {

    try { 
      af = new DataInputStream(url.openStream());
    } catch (IOException e) {
	fileok = false;
	throw new AreaFileException("Error opening URL for AreaFile:"+e);
    }
    fileok=true;
    position = 0;
    readMetaData();
  }
    
  /** Read the metadata for an area file (directory, nav,
   *  and cal). 
   *
   * @exception AreaFileException if there is a problem
   * reading any portion of the metadata.
   *
   */

  public void readMetaData() throws AreaFileException {
    
    int i;

    if (! fileok) {
      throw new AreaFileException("Error reading AreaFile directory");
    }

    dir = new int[64];

    for (i=0; i<64; i++) {
      try { dir[i] = af.readInt();
	   //  System.out.println("Area Dir["+i+"] = "+dir[i]);
      } catch (IOException e) {
	status = -1;
	throw new AreaFileException("Error reading AreaFile directory:" + e);
      }
    }
    position += 64*4;

    // see if the directory needs to be byte-flipped

    if (dir[1] >255) {
      flip(dir,0,19);
      // word 20 may contain characters -- if small integer, flip it...
      if ( (dir[20] & 0xffff) == 0) flip(dir,20,20);
      flip(dir,21,23);
      // words 24-31 contain memo field
      flip(dir,32, 50);
      // words 51-2 contain cal info
      flip(dir,53,55);
      // word 56 contains original source type (ascii)
      flip(dir,57,63);
      flipwords = true;
    }

    // pull together some values needed by other methods
    navLoc = dir[34];
    calLoc = dir[62];
    auxLoc = dir[59];
    datLoc = dir[33];
    numBands = dir[13];
    linePrefixLength = dir[48] + dir[49] + dir[50];
    if (dir[35] != 0) linePrefixLength = linePrefixLength + 4;
    if (linePrefixLength != dir[14]) throw new
           AreaFileException("Invalid line prefix length in AREA file.");
    lineDataLength = numBands * dir[9] * dir[10];
    lineLength = linePrefixLength + lineDataLength;
    numberLines = dir[8];

    if (datLoc > 0 && datLoc != McMISSING) {
      navbytes = datLoc - navLoc;
      calbytes = datLoc - calLoc;
      auxbytes = datLoc - auxLoc;
    }
    if (auxLoc > 0 && auxLoc != McMISSING) {
      navbytes = auxLoc - navLoc;
      calbytes = auxLoc - calLoc;
    }

    if (calLoc > 0 && calLoc != McMISSING ) {
      navbytes = calLoc - navLoc;
    }


    // Read in nav block

    if (navLoc > 0 && navbytes > 0) {

      nav = new int[navbytes/4];

      newPosition = (long) navLoc;
      skipByteCount = (int) (newPosition - position);
      try {
        af.skipBytes(skipByteCount);
      } catch (IOException e) {
	status = -1;
	throw new AreaFileException("Error skipping AreaFile bytes: " + e);
      }

      for (i=0; i<navbytes/4; i++) {
	try { nav[i] = af.readInt();
	} catch (IOException e) {
	  status = -1;
	  throw new AreaFileException("Error reading AreaFile navigation:"+e);
	}
      }
      if (flipwords) flipnav(nav);
      position = navLoc + navbytes;
    }


    // Read in cal block

    if (calLoc > 0 && calbytes > 0) {

      cal = new int[calbytes/4];

      newPosition = (long)calLoc;
      skipByteCount = (int) (newPosition - position);
      try {
        af.skipBytes(skipByteCount);
      } catch (IOException e) {
	status = -1;
	throw new AreaFileException("Error skipping AreaFile bytes: " + e);
      }

      for (i=0; i<calbytes/4; i++) {
	try { cal[i] = af.readInt();
	} catch (IOException e) {
	  status = -1;
	  throw new AreaFileException("Error reading AreaFile calibration:"+e);
	}
      }
      // if (flipwords) flipcal(cal);
      position = calLoc + calbytes;
    }


    // now return the Dir, as requested...
    status = 1;
    return;
  }

  /** returns the directory block
   *
   * @return an integer array containing the area directory
   *
   * @exception AreaFileException if there was a problem
   * reading the directory
   *
   */

  public int[] getDir() throws AreaFileException {


    if (status <= 0) {
      throw new AreaFileException("Error reading AreaFile directory");
    }

    return dir;

  }

  /** returns the navigation block
   *
   * @return an integer array containing the nav block data
   *
   * @exception AreaFileException if there is a problem
   * reading the navigation
   *
   */

  public int[] getNav() throws AreaFileException {


    if (status <= 0) {
      throw new AreaFileException("Error reading AreaFile navigation");
    }

    if (navLoc <= 0 || navLoc == McMISSING) {
      throw new AreaFileException("Error reading AreaFile navigation");
    } 

    return nav;

  }

  /** Returns calibration block
   *
   * @return an integer array containing the nav block data
   *
   * @exception AreaFileException if there is a problem
   * reading the navigation
   *
   */

  public int[] getCal() throws AreaFileException {


    if (status <= 0) {
      throw new AreaFileException("Error reading AreaFile calibration");
    }

    if (navLoc <= 0 || navLoc == McMISSING) {
      throw new AreaFileException("Error reading AreaFile calibration");
    } 

    return cal;

  }
  /**
   * Read the AREA file and return the entire contents
   *
   * @exception AreaFileException if there is a problem
   *
   * @return int array[band][lines][elements]
   *
   */

  public int[][][] getData() throws AreaFileException {
    if (!hasReadData) readData();
    return data;
  }

  /**
   * Read the specified 2-dimensional array of
   * data values from the AREA file.  Values will always be returned
   * as int regardless of whether they are 1, 2, or 4 byte values.
   *
   * @param lineNumber the file-relative image line number that will
   * be put in array[0][j]

   * @param eleNumber the file-relative image element number that will
   * be put into array[i][0] 
   *
   * @param numLines the number of lines to return
   *
   * @param numEles the number of elements to return for each line
   *
   * @param bandNumber the spectral band to return (def=1)
   *
   * @exception AreaFileException if the is a problem reading the file
   *
   * @return int array[lines][elements] with data values.
   *
   */

  public int[][] getData(int lineNumber, int eleNumber, int
	 numLines, int numEles) throws AreaFileException {
   return getData(lineNumber, eleNumber, numLines, numEles,1);
  }


  public int[][] getData(int lineNumber, int eleNumber, int
         numLines, int numEles, int bandNumber) throws AreaFileException {

    if (!hasReadData) readData();
    int[][] subset = new int[numLines][numEles];
    for (int i=0; i<numLines; i++) {
      int ii = i + lineNumber;
      for (int j=0; j<numEles; j++) {
	int jj = j + eleNumber;
	if (ii<0 || ii > (dir[8]-1) || jj < 0 || jj > (dir[9]-1) ) {
	  subset[i][j] = 0;
	} else {
	  subset[i][j] = data[bandNumber][ii][jj];
	}
      }
    }
    return subset;
  }

  private void readData() throws AreaFileException {

    int i,j,k;
    int numLines = dir[8], numEles = dir[9];

    if (! fileok) {
      throw new AreaFileException("Error reading AreaFile data");
    }

    data = new int[numBands][numLines][numEles];

    for (i = 0; i<numLines; i++) {

      try {
        newPosition = (long) (datLoc +
	       linePrefixLength + i*lineLength) ;
        skipByteCount = (int) (newPosition - position);
        af.skipBytes(skipByteCount);
        position = newPosition;

      } catch (IOException e) {
	 for (j = 0; j<numEles; j++) {
	   for (k=0; k<numBands; k++) {data[k][i][j] = 0;}
	 }
        break;
      }

      for (j = 0; j<numEles; j++) {

	for (k=0; k<numBands; k++) {

	  if (j > lineDataLength) {
	    data[k][i][j] = 0;
	  } else {

	    try {
	      if (dir[10] == 1) {
		data[k][i][j] = (int) af.readByte();
		if (data[k][i][j] < 0) data[k][i][j] += 256;
		position = position + 1;
	      }
	      if (dir[10] == 2) {
		data[k][i][j] = (int) af.readShort();
		position = position + 2;
	      }
	      if (dir[10] == 4) {
		data[k][i][j] = af.readInt();
		position = position + 4;
	      }
	    } 
	    catch (IOException e) {data[k][i][j] = 0;}
	  }
	}
      }

    }

    return ;

  } // end of areaReadData method

  /**
   * getData as bytes...
   *
  public byte[][] getDataBytes(int lineNumber, int eleNumber, int
                   numLines, int numEles) throws AreaFileException {
    if (!hasReadData) readData();
    return;
  }
   */

  //public int areaReadCal(int[])
  //public int areaReadLine(byte line[], int lineNumber, int start, int number)

  /**
   *  flip the bytes of an integer array
   *
   * @param array[] array of integers to be flipped
   * @param first starting element of the array
   * @param last last element of array to flip
   *
   */
  private void flip(int array[], int first, int last) {
    int i,k;
    for (i=first; i<=last; i++) {
      k = array[i];
      array[i] = ( (k >>> 24) & 0xff) | ( (k >>> 8) & 0xff00) |
		 ( (k & 0xff) << 24 )  | ( (k & 0xff00) << 8);
    }
  }

  /**
   * selectively flip the bytes of words in nav block
   *
   * @param array[] of nav parameters
   *
   */

  private void flipnav(int[] nav) {

    // first word is always the satellite id in ASCII
    // check on which type:

    if (nav[0] == GVAR) {

      flip(nav,2,126);
      flip(nav,129,254);
      flip(nav,257,382);
      flip(nav,385,510);
      flip(nav,513,638);
    }

    else if (nav[0] == DMSP) {
      flip(nav,1,43);
      flip(nav,45,51);
    }

    else if (nav[0] == POES) {
      flip(nav,1,119);
    }

    else {
      flip(nav,1,nav.length-1);
    }

    return;
  }



}
