//
// AreaDirectory.java
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

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/** 
 * AreaDirectory interface for the metadata of McIDAS 'area' file format 
 * image data.
 *
 * @author Don Murray
 * 
 */
public class AreaDirectory 
{

    private boolean flipwords = false;
    private int[] dir = new int[AreaFile.AD_DIRSIZE];   // single directory
    private Date nominalTime;          // time of the image
    private int lines;                 // number of lines in the image
    private int elements;              // number of elements in the image
    private int[] bands;               // array of the band numbers
    private int numbands;              // number of bands
    
    /**
     * Create an AreaDirectory from the raw block of data of
     * an AreaFile.  Byte-flipping will be handled.
     *
     * @param  dirblock   the integer block
     *
     * @exception  AreaFileException   not a valid directory
     */
    public AreaDirectory(int[] dirblock)
        throws AreaFileException
    {
        if (dirblock.length != AreaFile.AD_DIRSIZE)
            throw new AreaFileException("Directory is not the right size");
        dir = dirblock;
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
        // Pull out some of the important information
        nominalTime = 
            new Date(1000* McIDASUtil.mcDayTimeToSecs(
                    dir[AreaFile.AD_IMGDATE], 
                        dir[AreaFile.AD_IMGTIME]));
        lines = dir[AreaFile.AD_NUMLINES];
        elements = dir[AreaFile.AD_NUMELEMS];
        numbands = dir[AreaFile.AD_NUMBANDS];
        bands = new int[numbands];
        int j = 0;
        for (int i = 0; i < 32; i++)
        {
            int bandmask = 1 << i;
            if ( (bandmask & dir[AreaFile.AD_BANDMAP]) == bandmask)
            {
                bands[j] = i+1 ;
                j++;
            }
            if (j > numbands) break;
        }

    }

    /**
     * Create an AreaDirectory from another AreaDirectory object.
     *
     * @param  directory   the source AreaDirectory
     *
     * @exception  AreaFileException   not a valid directory
     */
    public AreaDirectory(AreaDirectory directory)
        throws AreaFileException
    {
        this(directory.getDirectoryBlock());
    }
    
    /**
     * Return a specific value from the directory
     *
     * @param  pointer   part of the directory you want returned.  
     *                   Use AreaFile static fields as pointers.
     *
     * @exception  AreaFileException  invalid pointer
     */
    public int getValue(int pointer)
        throws AreaFileException
    {
        if (pointer < 0 || pointer > AreaFile.AD_DIRSIZE)
            throw new AreaFileException("Invalid pointer " + pointer);
        return dir[pointer];
    }

    /**
     * Get the raw directory block
     *
     * @return integer array of the raw directory values
     */
    public int[] getDirectoryBlock()
    {
        return dir;
    }

    /**
     * returns the nominal time of the image
     *
     * @return the nominal time as a Date
     *
     */
    public Date getNominalTime()
    {
        return nominalTime;
    }

    /**
     * returns the number of lines in the image
     *
     * @return line number
     */
    public int getLines()
    {
        return lines;
    }

    /**
     * returns the number of elements in the image
     *
     * @return number of elements
     */
    public int getElements()
    {
        return elements;
    }

    /**
     * returns the bands in each of the images
     *
     * @return a array of bands 
     *
     * @exception AreaFileException if there was a problem
     *            reading the directory
     */
    public int[] getBands()
    {
        return bands;
    }

    /**
     * Prints out a formatted listing of the directory info
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf.applyPattern("yyyy-MMM-dd  HH:mm:ss");
        buf.append("    ");
        buf.append(sdf.format(nominalTime, 
            new StringBuffer(), new FieldPosition(0)).toString());
        buf.append("  ");
        buf.append(Integer.toString(lines));
        buf.append("    ");
        buf.append(Integer.toString(elements));
        buf.append("       ");
        for (int i = 0; i < bands.length; i++) buf.append(bands[i]);
        return buf.toString();
    }
}
