
//
// AreaFile.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package edu.wisc.ssec.mcidas;

import java.applet.Applet;
import java.io.*;
import java.lang.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.awt.event.*;
import java.awt.Frame;
import edu.wisc.ssec.mcidas.adde.GetAreaGUI;

/** 
 * AreaFile interface with McIDAS 'area' file format image data.
 * This will allow 'area' format data to be read from disk; the
 * navigation block is made available (see GVARnav for example).
 *
 * This implementation does not do calibration (other than
 * accounting for its presence in the data).  Also, the 'valcode'
 * is not checked on each line.
 *
 * @authors - Tom Whittaker & Tommy Jasmin at SSEC
 * 
 */

public class AreaFile {

  // indeces used by this and client classes

  /** AD_STATUS - old status field, now used as position num in ADDE */
  public static final int AD_STATUS     = 0;
  /** AD_VERSION - McIDAS area file format version number */
  public static final int AD_VERSION    = 1;
  /** AD_SENSORID - McIDAS sensor identifier */
  public static final int AD_SENSORID   = 2;
  /** AD_IMGDATE - nominal year and day of the image, YYYDDD format */
  public static final int AD_IMGDATE    = 3;
  /** AD_IMGTIME - nominal time of the image, HHMMSS format */
  public static final int AD_IMGTIME    = 4;
  /** AD_STLINE - upper left image line coordinate */
  public static final int AD_STLINE     = 5;
  /** AD_STELEM - upper left image element coordinate */
  public static final int AD_STELEM     = 6;
  /** AD_NUMLINES - number of lines in the image */
  public static final int AD_NUMLINES   = 8;
  /** AD_NUMELEMS - number of data points per line */
  public static final int AD_NUMELEMS   = 9;
  /** AD_DATAWIDTH - number of bytes per data point */
  public static final int AD_DATAWIDTH  = 10;
  /** AD_LINERES - data resolution in line direction */
  public static final int AD_LINERES    = 11;
  /** AD_ELEMRES - data resolution in element direction */
  public static final int AD_ELEMRES    = 12;
  /** AD_NUMBANDS - number of spectral bands, or channels, in image */
  public static final int AD_NUMBANDS   = 13;
  /** AD_PFXSIZE - length in bytes of line prefix section */
  public static final int AD_PFXSIZE    = 14;
  /** AD_PROJNUM - SSEC project number used in creating image */
  public static final int AD_PROJNUM    = 15;
  /** AD_CRDATE - year and day image was created, CCYYDDD format */
  public static final int AD_CRDATE     = 16;
  /** AD_CRTIME - time image was created, HHMMSS format */
  public static final int AD_CRTIME     = 17;
  /** AD_BANDMAP - spectral band map, bit set for each of 32 bands present */
  public static final int AD_BANDMAP    = 18;
  /** AD_DATAOFFSET - byte offset to start of data block */
  public static final int AD_DATAOFFSET = 33;
  /** AD_NAVOFFSET - byte offset to start of navigation block */
  public static final int AD_NAVOFFSET  = 34;
  /** AD_VALCODE - validity code */
  public static final int AD_VALCODE    = 35;
  /** AD_STARTDATE - actual image start year and Julian day, yyyddd format */
  public static final int AD_STARTDATE  = 45;
  /** AD_STARTTIME - actual image start time, hhmmss; 
   *  in milliseconds for POES data */
  public static final int AD_STARTTIME  = 46;
  /** AD_STARTSCAN - starting scan number (sensor based) of image */
  public static final int AD_STARTSCAN  = 47;
  /** AD_DOCLENGTH - length in bytes of line prefix documentation */
  public static final int AD_DOCLENGTH  = 48;
  /** AD_CALLENGTH - length in bytes of line prefix calibration information */
  public static final int AD_CALLENGTH  = 49;
  /** AD_LEVLENGTH - length in bytes of line prefix level section */
  public static final int AD_LEVLENGTH  = 50;
  /** AD_SRCTYPE - McIDAS source type (ascii, satellite specific) */
  public static final int AD_SRCTYPE    = 51;
  /** AD_CALTYPE - McIDAS calibration type (ascii, satellite specific) */
  public static final int AD_CALTYPE    = 52;
  /** AD_AVGSMPFLAG - data is averaged (1), or sampled (0) */
  public static final int AD_AVGSMPFLAG = 53;
  /** AD_SRCTYPEORIG - original source type (ascii, satellite specific) */
  public static final int AD_SRCTYPEORIG    = 56;
  /** AD_AUXOFFSET - byte offset to start of auxilliary data section */
  public static final int AD_AUXOFFSET  = 59;
  /** AD_CALOFFSET - byte offset to start of calibration section */
  public static final int AD_CALOFFSET  = 62;
  /** AD_DIRSIZE - size in 4 byte words of an image directory block */
  public static final int AD_DIRSIZE    = 64;

  /** VERSION_NUMBER - version number for a valid AREA file (since 1985) */
  public static final int VERSION_NUMBER = 4;

  private static boolean handlerLoaded = false;

  // load protocol handler for ADDE URLs
  // See java.net.URL for explanation of URL handling
  static 
  {
      try 
      {
          String handlers = System.getProperty("java.protocol.handler.pkgs");
          String newProperty = null;
          if (handlers == null)
              newProperty = "edu.wisc.ssec.mcidas";
          else if (handlers.indexOf("edu.wisc.ssec.mcidas") < 0)
              newProperty = "edu.wisc.ssec.mcidas | " + handlers;
          if (newProperty != null)  // was set above
              System.setProperty("java.protocol.handler.pkgs", newProperty);
          handlerLoaded = true;
      }
      catch (Exception e)
      {
          System.out.println(
              "Unable to set System Property: java.protocol.handler.pkgs"); 
      }

      handlerLoaded = true;
  }

  public static boolean isURLHandlerLoaded()
  {
    return handlerLoaded;
  }

  private boolean flipwords;
  private boolean fileok;
  private boolean hasReadData = false;
  private DataInputStream af;
  private int status=0;
  private int navLoc, calLoc, auxLoc, datLoc;
  private int navbytes, calbytes, auxbytes;
  private int linePrefixLength, lineDataLength, lineLength, numberLines;
  private long position;
  private int skipByteCount;
  private long newPosition;
  private int numBands;
  int[] dir;
  int[] nav;
  int[] cal;
  int[] aux;
  int[][][] data;
  private AreaDirectory areaDirectory;
  private String imageSource;
  private AREAnav areaNav;
  
  /**
   * creates an AreaFile object that allows reading
   * of McIDAS 'area' file format image data.  allows reading
   * either from a disk file, or a server via ADDE.
   *
   * @param imageSource the file name or ADDE URL to read from
   *
   * @exception AreaFileException if file cannot be opened
   *
   */
 
  public AreaFile(String source) throws AreaFileException {
    
    imageSource = source;
    if (imageSource.startsWith("adde://") && (
      imageSource.endsWith("image?") || imageSource.endsWith("imagedata?") )) {

      GetAreaGUI gag = new GetAreaGUI((Frame)null, true, "Get data", false, true);
      gag.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
          imageSource = e.getActionCommand();
          }
      });
      gag.show();
    }
    
    // try as a disk file first
    try {
      af = new DataInputStream (
        new BufferedInputStream(new FileInputStream(imageSource), 2048)
      );
    } catch (IOException eIO) {
      // if opening as a file failed, try as a URL
      URL url;
      try {
        url = new URL(imageSource);
        URLConnection urlc = url.openConnection();
        InputStream is = urlc.getInputStream();
        af = new DataInputStream( new BufferedInputStream(is));
      }
      catch (Exception e) {
        fileok = false;
        throw new AreaFileException("Error opening AreaFile: " + e);
      }
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

    URL url;
    try {
      url = new URL(parent.getDocumentBase(), filename);
    } catch (MalformedURLException e) {
      fileok = false;
      throw new AreaFileException("Error opening URL for AreaFile:"+e);
    }

    try { 
      af = new DataInputStream(new BufferedInputStream(url.openStream()));
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
      af = new DataInputStream(new BufferedInputStream(url.openStream()));
    } catch (IOException e) {
        fileok = false;
        throw new AreaFileException("Error opening URL for AreaFile:"+e);
    }
    fileok=true;
    position = 0;
    readMetaData();
  }

  /** 
   *  Read the metadata for an area file (directory, nav,  and cal). 
   *
   * @exception AreaFileException if there is a problem
   * reading any portion of the metadata.
   *
   */

  private void readMetaData() throws AreaFileException {
    
    int i;

    if (! fileok) {
      throw new AreaFileException("Error reading AreaFile directory");
    }

    dir = new int[AD_DIRSIZE];

    for (i=0; i < AD_DIRSIZE; i++) {
      try { dir[i] = af.readInt();
      } catch (IOException e) {
        status = -1;
        throw new AreaFileException("Error reading AreaFile directory:" + e);
      }
    }
    position += AD_DIRSIZE * 4;

    // see if the directory needs to be byte-flipped

    if (dir[AD_VERSION] != VERSION_NUMBER) {
      McIDASUtil.flip(dir,0,19);
      // check again
     if (dir[AD_VERSION] != VERSION_NUMBER)
         throw new AreaFileException(
             "Invalid version number - probably not an AREA file");
      // word 20 may contain characters -- if small integer, flip it...
      if ( (dir[20] & 0xffff) == 0) McIDASUtil.flip(dir,20,20);
      McIDASUtil.flip(dir,21,23);
      // words 24-31 contain memo field
      McIDASUtil.flip(dir,32,50);
      // words 51-2 contain cal info
      McIDASUtil.flip(dir,53,55);
      // word 56 contains original source type (ascii)
      McIDASUtil.flip(dir,57,63);
      flipwords = true;
    }

    areaDirectory = new AreaDirectory(dir);

    // pull together some values needed by other methods
    navLoc = dir[AD_NAVOFFSET];
    calLoc = dir[AD_CALOFFSET];
    auxLoc = dir[AD_AUXOFFSET];
    datLoc = dir[AD_DATAOFFSET];
    numBands = dir[AD_NUMBANDS];
    linePrefixLength = 
      dir[AD_DOCLENGTH] + dir[AD_CALLENGTH] + dir[AD_LEVLENGTH];
    if (dir[AD_VALCODE] != 0) linePrefixLength = linePrefixLength + 4;
    if (linePrefixLength != dir[AD_PFXSIZE]) 
      throw new AreaFileException("Invalid line prefix length in AREA file.");
    lineDataLength = numBands * dir[AD_NUMELEMS] * dir[AD_DATAWIDTH];
    lineLength = linePrefixLength + lineDataLength;
    numberLines = dir[AD_NUMLINES];

    if (datLoc > 0 && datLoc != McIDASUtil.MCMISSING) {
      navbytes = datLoc - navLoc;
      calbytes = datLoc - calLoc;
      auxbytes = datLoc - auxLoc;
    }
    if (auxLoc > 0 && auxLoc != McIDASUtil.MCMISSING) {
      navbytes = auxLoc - navLoc;
      calbytes = auxLoc - calLoc;
    }

    if (calLoc > 0 && calLoc != McIDASUtil.MCMISSING ) {
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

    // Read in aux block

    if (auxLoc > 0 && auxbytes > 0)
    {
        aux = new int[auxbytes/4];
        newPosition = (long) auxLoc;
        skipByteCount = (int) (newPosition - position);
        try
        {
            af.skipBytes(skipByteCount);
        }
        catch (IOException e)
        {
            status = -1;
            throw new AreaFileException("Error skipping AreaFile bytes: " + e);
        }
        for (i = 0; i < auxbytes/4; i++)
        {
            try
            {
                aux[i] = af.readInt();
            }
            catch (IOException e)
            {
                status = -1;
                throw new AreaFileException("Error reading AreaFile aux block:"+
                                             e);
            }
        }
        position = auxLoc + auxbytes;
    }


    // now return the Dir, as requested...
    status = 1;
    return;
  }

  /** 
   * Returns the directory block
   *
   * @return an integer array containing the area directory
   *
   * @exception AreaFileException if there was a problem
   *                              reading the directory
   *
   */
  public int[] getDir() throws AreaFileException 
  {
    if (status <= 0) 
    {
      throw new AreaFileException("Error reading AreaFile directory");
    }
    return dir;
  }


  /** 
   * Returns the AreaDirectory object for this AreaFile
   *
   * @return AreaDirectory
   *
   * @exception AreaFileException if there was a problem
   *                              reading the directory
   *
   */
  public AreaDirectory getAreaDirectory() throws AreaFileException 
  {
    if (status <= 0) 
    {
      throw new AreaFileException("Error reading AreaFile directory");
    }
    return areaDirectory;
  }

  
  /** 
   * Returns the navigation block
   *
   * @return an integer array containing the nav block data
   *
   * @exception AreaFileException if there is a problem
   *                              reading the navigation
   *
   */

  public int[] getNav() throws AreaFileException {


    if (status <= 0) {
      throw new AreaFileException("Error reading AreaFile navigation");
    }

    if (navLoc <= 0 || navLoc == McIDASUtil.MCMISSING) {
      throw new AreaFileException("Error reading AreaFile navigation");
    } 

    return nav;

  }

  /**
   * Get the navigation 
   * @return  AREAnav for this image  (may be null)
   */
  public AREAnav getNavigation()
      throws AreaFileException
  {
    if (areaNav == null) {
      // make the nav module
      try {
        areaNav = AREAnav.makeAreaNav(getNav());
      } catch (McIDASException excp) {
        areaNav = null;
      }
    }
    return areaNav;
  }

  /** 
   * Returns calibration block
   *
   * @return an integer array containing the nav block data
   *
   * @exception AreaFileException if there is a problem
   *                              reading the calibration
   *
   */

  public int[] getCal() throws AreaFileException {


    if (status <= 0) {
      throw new AreaFileException("Error reading AreaFile calibration");
    }

    if (calLoc <= 0 || calLoc == McIDASUtil.MCMISSING) {
      throw new AreaFileException("Error reading AreaFile calibration");
    } 

    return cal;

  }


  /** 
   * Returns AUX block
   *
   * @return an integer array containing the aux block data
   *
   * @exception AreaFileException if there is a problem
   *                              reading the aux block
   *
   */

  public int[] getAux() throws AreaFileException {


    if (status <= 0) {
      throw new AreaFileException("Error reading AreaFile aux block");
    }

    if (auxLoc <= 0 || auxLoc == McIDASUtil.MCMISSING) {
      throw new AreaFileException("Error reading AreaFile AUX block");
    } 

    return aux;

  }

  /**
   * Read the AREA file and return the entire contents
   *
   * @return int array[band][lines][elements]
   *
   * @exception AreaFileException if there is a problem
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
   *                   be put in array[0][j]
   * @param eleNumber  the file-relative image element number that will
   *                   be put into array[i][0] 
   * @param numLines   the number of lines to return
   * @param numEles    the number of elements to return for each line
   *
   * @return int array[lines][elements] with data values.
   *
   * @exception AreaFileException if the is a problem reading the file
   */
  public int[][] getData(int lineNumber, int eleNumber, int
         numLines, int numEles) throws AreaFileException {
   return getData(lineNumber, eleNumber, numLines, numEles, 1);
  }


  /**
   * Read the specified 2-dimensional array of
   * data values from the AREA file.  Values will always be returned
   * as int regardless of whether they are 1, 2, or 4 byte values.
   *
   * @param lineNumber the file-relative image line number that will
   *                   be put in array[0][j]
   * @param eleNumber  the file-relative image element number that will
   *                   be put into array[i][0] 
   * @param numLines   the number of lines to return
   * @param numEles    the number of elements to return for each line
   * @param bandNumber the spectral band to return
   *
   * @return int array[lines][elements] with data values.
   *
   * @exception AreaFileException if the is a problem reading the file
   */
  public int[][] getData(int lineNumber, int eleNumber, int
         numLines, int numEles, int bandNumber) throws AreaFileException {

    // note band numbers are 1-based, and data offsets are 0-based
    if (!hasReadData) readData();
    int[][] subset = new int[numLines][numEles];
    for (int i=0; i<numLines; i++) {
      int ii = i + lineNumber;
      for (int j=0; j<numEles; j++) {
        int jj = j + eleNumber;
        if (ii < 0 || ii > (dir[AD_NUMLINES] - 1) || 
            jj < 0 || jj > (dir[AD_NUMELEMS] - 1) ) {
          subset[i][j] = 0;
        } else {
          subset[i][j] = data[bandNumber - 1][ii][jj];
        }
      }
    }
    return subset;
  }

  private void readData() throws AreaFileException {

    int i,j,k;
    int numLines = dir[AD_NUMLINES], numEles = dir[AD_NUMELEMS];

    if (! fileok) {
      throw new AreaFileException("Error reading AreaFile data");
    }

    data = new int[numBands][numLines][numEles];
    short shdata;
    int intdata;

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

              if (dir[AD_DATAWIDTH] == 1) {
                data[k][i][j] = (int) af.readByte();
                if (data[k][i][j] < 0) data[k][i][j] += 256;
                position = position + 1;
              }

              if (dir[AD_DATAWIDTH] == 2) {
                shdata = af.readShort();
                if (flipwords) {
                  data[k][i][j] = (int) ( ((shdata >> 8) & 0xff) | 
                                          ((shdata << 8) & 0xff00) );
                } else {
                  data[k][i][j] = (int) shdata;
                }
                position = position + 2;
              }

              if (dir[AD_DATAWIDTH] == 4) {
                intdata = af.readInt();
                if (flipwords) {
                  data[k][i][j] = ( (intdata >>> 24) & 0xff) | 
                                  ( (intdata >>> 8) & 0xff00) | 
                                  ( (intdata & 0xff) << 24 )  | 
                                  ( (intdata & 0xff00) << 8);
                } else {
                  data[k][i][j] = intdata;
                }
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
   * selectively flip the bytes of words in nav block
   *
   * @param array[] of nav parameters
   *
   */
  private void flipnav(int[] nav) {

    // first word is always the satellite id in ASCII
    // check on which type:

    if (nav[0] == AREAnav.GVAR) {

      McIDASUtil.flip(nav,2,126);
      McIDASUtil.flip(nav,129,254);
      McIDASUtil.flip(nav,257,382);
      McIDASUtil.flip(nav,385,510);
      McIDASUtil.flip(nav,513,638);
    }

    else if (nav[0] == AREAnav.DMSP) {
      McIDASUtil.flip(nav,1,43);
      McIDASUtil.flip(nav,45,51);
    }

    else if (nav[0] == AREAnav.POES) {
      McIDASUtil.flip(nav,1,119);
    }

    else {
      McIDASUtil.flip(nav,1,nav.length-1);
    }

    return;
  }



}
