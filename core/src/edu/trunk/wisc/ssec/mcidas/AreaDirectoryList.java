//
// AreaDirectoryList.java
//

/*
The code in this file is Copyright(C) 1999 by Don
Murray.  It is designed to be used with the VisAD system for 
interactive analysis and visualization of numerical data.  
 
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

import edu.wisc.ssec.mcidas.adde.AddeURLConnection;
import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.ArrayList;


/** 
 * AreaDirectoryList interface for McIDAS 'area' file format image data.
 * Provides access to a list of one or more AreaDirectoy objects.
 *
 * @author Don Murray
 * 
 */
public class AreaDirectoryList
{

    // load protocol for ADDE URLs
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
        }
        catch (Exception e)
        {
            System.out.println(
                "Unable to set System Property: java.protocol.handler.pkgs"); 
        }
    }

    private boolean flipwords = false;
    private DataInputStream inputStream;  // input stream
    private int status=0;                 // read status
    private URLConnection urlc;           // URL connection
    private boolean isADDE = false;       // true if ADDE request
    private int[] dir;                    // single directory
    private Date[] nominalTimes;          // array of dates
    private int[] bands;                  // array of bands
    private int[] lines;                  // array of lines
    private int[] elements;               // array of elements
    private ArrayList dirs;               // list of directories
    private int numDirs = 0;              // number of directories
    private AreaDirectory ad;             // directory objects
    
    /**
     * creates an AreaDirectory object that allows reading
     * of McIDAS 'area' file format image data.  allows reading
     * either from a disk file, or a server via ADDE.  
     *
     * @param imageSource the file name or ADDE URL to read from
     *
     * @exception AreaFileException if file cannot be opened
     *
     */
    public AreaDirectoryList(String imageSource) 
        throws AreaFileException 
    {
   
        // try as a disk file first
        try 
        {
            inputStream = 
                new DataInputStream (
                    new BufferedInputStream(
                        new FileInputStream(imageSource), 2048));
        } 
        catch (IOException eIO) 
        {
            // if opening as a file failed, try as a URL
            URL url;
            try 
            {
                url = new URL(imageSource);
                urlc = url.openConnection();
                InputStream is = urlc.getInputStream();
                inputStream = new DataInputStream(is);
            }
            catch (Exception e) 
            {
                throw new AreaFileException("Error opening AreaFile: " + e);
            }
            if (url.getProtocol().equalsIgnoreCase("adde")) isADDE = true;
        }
        readDirectory();
    }
 
    /**
     * creates an AreaDirectory object that allows reading
     * of the directory of McIDAS 'area' file format image data from an applet
     *
     * @param filename the disk filename (incl path) to read from
     * @param parent the parent applet 
     *
     * @exception AreaFileException if file cannot be opened
     *
    public AreaDirectoryList(String filename, Applet parent) 
        throws AreaFileException 
    {
        URL url;
        try 
        {
            url = new URL(parent.getDocumentBase(), filename);
        } 
        catch (MalformedURLException e) 
        {
            throw new AreaFileException(e.toString());
        }

        try 
        { 
            inputStream = new DataInputStream(url.openStream());
        }
        catch (IOException e) 
        {
            throw new AreaFileException("Error opening AreaFile:"+e);
        }
        readDirectory();
    }
     */

    /**
     * creates an AreaDirectory object that allows reading
     * of the directory of McIDAS 'area' files from a URL
     *
     * @param URL - the URL to go after
     *
     * @exception AreaFileException if file cannot be opened
     *
     */
    public AreaDirectoryList(URL url) 
        throws AreaFileException 
    {
        try 
        { 
            inputStream = new DataInputStream(url.openStream());
        } 
        catch (IOException e) 
        {
            throw new AreaFileException("Error opening URL for AreaFile:"+e);
        }
        readDirectory();
    }
    
    /** 
     * Read the directory information for an area file or area directory
     * record.
     *
     * @exception   AreaFileException    if there is a problem reading 
     *                                   any portion of the metadata.
     *
     */
    private void readDirectory() 
        throws AreaFileException 
    {
        dirs = new ArrayList();
        int numBytes = 
            (isADDE) 
                ? ((AddeURLConnection) urlc).getInitialRecordSize() 
                : AreaFile.AD_DIRSIZE;
        while (numBytes > 0)
        {
            try
            {
                dir = new int[AreaFile.AD_DIRSIZE];
    
                // skip first int which is dataset area number if ADDE request
                if (isADDE) 
                {
                    int areaNumber = inputStream.readInt();
                    //System.out.println("Area number = " + areaNumber);
                    if (areaNumber == 0) break;
                }
        
                for (int i=0; i < AreaFile.AD_DIRSIZE; i++) 
                {
                    dir[i] = inputStream.readInt();
                }
                if (isADDE) dir[0] = 0;
        
                // see if the directory needs to be byte-flipped
                if (dir[AreaFile.AD_VERSION] > 255 || flipwords) 
                {
                    flipwords = true;
                    McIDASUtil.flip(dir,0,19);
                    // word 20 may contain characters -- if small int, flip it
                    if ( (dir[20] & 0xffff) == 0) McIDASUtil.flip(dir,20,20);
                    McIDASUtil.flip(dir,21,23);
                    // words 24-31 contain memo field
                    McIDASUtil.flip(dir,32,50);
                    // words 51-2 contain cal info
                    McIDASUtil.flip(dir,53,55);
                    // word 56 contains original source type (ascii)
                    McIDASUtil.flip(dir,57,63);
                }
    
    /*   Debug
                for (int i = 0; i < AreaFile.AD_DIRSIZE; i++)
                {
                    System.out.println("dir[" + i +"] = " + dir[i]);
                }
    */
        
                AreaDirectory ad = new AreaDirectory(dir);
    
                if (!isADDE) 
                {
                    numBytes = 0;
                }
                else
                {
                    // last word in trailer is the number of bytes for the
                    // next record so we need to read that
                    int skipBytesCount = numBytes - AreaFile.AD_DIRSIZE*4 - 4;
                    /*
                    int numCards = dir[AreaFile.AD_DIRSIZE -1];
                    System.out.println("Number of comment cards = " + numCards);
                    for (int i = 0; i < numCards; i++)
                    {
                        byte[] card = new byte[80];
                        for (int j = 0; j < 80; j++)
                        {
                            card[j] = inputStream.readByte();
                        }
                        System.out.println("card["+i+"] = " + new String(card));
                    }
                    */
                    inputStream.skipBytes(skipBytesCount);
                    numBytes = inputStream.readInt();
                 // System.out.println("Bytes in next record = " + numBytes);
                }
                dirs.add(ad);
            }
            catch (IOException e) 
            {
                status = -1;
                throw new AreaFileException(
                    "Error reading Area directory:" + e);
            }
            status = 1;
            numDirs++;
        } 
    }
    
    /** 
     * returns the directory blocks for the requested images.  
     * @see <A HREF="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
     *      McIDAS Programmer's Manual</A> for information on the parameters
     *      for each value.
     *
     * @return a ArrayList of AreaDirectorys 
     *
     * @exception AreaFileException if there was a problem
     *            reading the directory
     *
     */
    public ArrayList getDirs() 
        throws AreaFileException 
    {
        if (status <= 0 || dirs.size() <= 0) 
        {
            throw new AreaFileException(
                "Error reading directory information");
        }
        return dirs;
    }

    /**
     * Prints out a formatted listing of the directory info
     */
    public String toString()
    {
        if (status <=0 || numDirs <= 0)
        {
            return new String("No directory information available");
        }
        StringBuffer sb = new StringBuffer();
        sb.append("    Date         Time      Lines Elements    Bands \n");
        sb.append("    -------      ------    ----- --------    --------\n");
        for (int i = 0; i < dirs.size(); i++)
        {
            sb.append( ((AreaDirectory) dirs.get(i)).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args)
        throws Exception
    {
        if (args.length == 0)
        {
            System.out.println("Must supply a path or ADDE request to images");
            System.exit(1);
        }
        AreaDirectoryList adl = new AreaDirectoryList(args[0]);
        System.out.println(adl.toString());
    }
}
