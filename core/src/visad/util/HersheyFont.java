//
// HersheyFont.java
//

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
*/

/*****************************************************************************/
//
//  Copyright (c) James P. Buzbee 1996
//  House Blend Software
//
//  jbuzbee@nyx.net
//  Version 1.1 Dec 11 1996
//  Version 1.2 Sep 18 1997
//  Version 1.3 Feb 28 1998
//  Version 1.4 Aug 13 2000 : J++ bug workaround by  Paul Emory Sullivan
//  Version 1.x Sep 20 2001 - adapted to visad/PlotText usage
//
// Permission to use, copy, modify, and distribute this software
// for any use is hereby granted provided
// this notice is kept intact within the source file
// This is freeware, use it as desired !
//
// Very loosly based on code with authors listed as :
// Alan Richardson, Pete Holzmann, James Hurt
/*****************************************************************************/

package visad.util;

import java.io. *;
import java.net.URL;

/**
HersheyFont supports the Hershey Fonts for VisAD.
Adapted from the original code by Buzbee (see source)
*/
public class HersheyFont {

   private final static int MAX_CHARACTERS = 256;
   private final static int MAX_POINTS = 300;
   protected final static int X = 0;
   protected final static int Y = 1;
   private String copyright = "Copyright (c) James P. Buzbee Mar 30, 1996";
   protected String name;
   protected char characterVectors[][][] = new char[MAX_CHARACTERS][2][MAX_POINTS];
   protected int numberOfPoints[] = new int[MAX_CHARACTERS];
   protected int characterMinX[];
   protected int characterMaxX[];
   protected int characterSetMinY;
   protected int characterSetMaxY;
   protected int characterSetMinX;
   protected int characterSetMaxX;
   protected int charactersInSet;
   protected boolean fixedWidth = false;

   /** Default constructor.  Use the font 'futural'
    *
    * Supplied fonts are:
    *   futural, timesr, timesrb, cursive, futuram, rowmans,
    *   rowmant, meteorology
    *
    * All other fonts supplied by Bizbee are available at the
    * VisAD web site.
    *
    * More info at: http://batbox.org/font.html
    *
    */
   public HersheyFont() {
     this("futural");
   }

   /**
    * Get a Hershey Font by name.  This constructor form will
    * add a .jhf extension to the name given and read it as a
    * Resourse from the visad/util directory.  
    *
    * Supplied fonts are:
    *   futural, timesr, timesrb, cursive, futuram, rowmans,
    *   rowmant, meteorology
    *
    * All other fonts supplied by Bizbee are available at the
    * VisAD web site.
    *
    * @param fontName name of the Hershey font to use
    * 
    */
   public HersheyFont (String fontName)
   {
      name = fontName;
      try
      {
         // open the font file
         //InputStream fontStream = new FileInputStream (fontName);
         String fn = fontName+".jhf";

         InputStream fontStream = HersheyFont.class.getResourceAsStream(fn);
         // load the font file
         LoadHersheyFont (fontName, fontStream);
         // close the font file
         fontStream.close ();
      }
      catch (Exception e)
      {
         System.out.println ("Error processing HersheyFont named "+fontName+": "+e);
      }
      return;
   }


   /**
    * Get a Hershey Font by URL and name.  You must give the
    * complete filename (e.g., futural.jhf) as well as the URL
    *
    * @param base is the base URL of the file
    * @param fontName the name of the fontfile (include .jhf extension)
    * 
    */
   public HersheyFont (URL base, String fontName)
   {
      name = fontName;
      try
      {
         // open the font file
         InputStream fontStream = new URL (base, fontName).openStream ();
         // load the font file
         LoadHersheyFont (fontName, fontStream);
         // close the font file
         fontStream.close ();
      }
      catch (Exception e)
      {
         System.out.println ("Error processing font "+fontName+": "+e);
      }
      return;
   }


   /**
    * Get a Hershey Font by URL.  You must give the
    * complete filename (e.g., futural.jhf) as part of the URL
    *
    * @param base is the URL of the file (e.g.,
    * http://www.ssec.wisc.edu/visad/futural.jhf)
    * 
    */
   public HersheyFont (URL base)
   {
      name = base.toString ();
      try
      {
         // open the font file
         InputStream fontStream = base.openStream ();
         // load the font file
         LoadHersheyFont (name, fontStream);
         // close the font file
         fontStream.close ();
      }
      catch (Exception e)
      {
         System.out.println ("Error processing font "+name+": "+e);
      }
      return;
   }
   
   /**
    * See if o is equal to this
    * @return true if they have the same name
    */
   public boolean equals(Object o) {
	   if (!(o instanceof HersheyFont)) {
		   return false;
	   }
	   HersheyFont that = (HersheyFont) o;
	   return this.name.equals(that.name);
   }
   
   /**
    * Get the hashcode
    * @return the hashcode
    */
   public int hasCode() {
	   return name.hashCode();
   }


   /** get the maximum number of points (segments) allowed
   *
   * @return value of max number of segments allowed.
   *
   */
   public int getMaxPoints() {
     return MAX_POINTS;
   }
   /** get the minimum X coordinate values for all characters
   *
   * @return array of minimum X coordinates
   *
   */
   public int[] getCharacterMinX () { return characterMinX; }

   /** get the maximum X coordinate values for all characters
   *
   * @return array of maximum X coordinates
   *
   */
   public int[] getCharacterMaxX () { return characterMaxX; }

   /** get the minimum Y coordiante value for all characters
   *
   * @return minimum Y coordinate value for all characters in thie font
   *
   */
   public int getCharacterSetMinY() { return characterSetMinY; }


   /** get the maximum Y coordiante value for all characters
   *
   * @return maximum Y coordinate value for all characters in thie font
   *
   */
   public int getCharacterSetMaxY() { return characterSetMaxY; }

   /** get the minimum X coordiante value for all characters
   *
   * @return minimum X coordinate value for all characters in thie font
   *
   */
   public int getCharacterSetMinX() { return characterSetMinX; }

   /** get the maximum X coordiante value for all characters
   *
   * @return maximum X coordinate value for all characters in thie font
   *
   */
   public int getCharacterSetMaxX() { return characterSetMaxX; }

   /** get the minimum Y coordiante value for all characters
   *
   * @return minimum Y coordinate value for all characters in thie font
   *
   */
   public int getCharactersInSet() { return charactersInSet; }

   /** get the number of points for the i-th character
   *
   * @param i the index of the character
   *
   * @return the number of data points for this character
   *
   */
   public int getNumberOfPoints(int i) {
     if (i < characterVectors.length) {
       return (numberOfPoints[i]); 
     } else {
       return 0;
     }

   }

   /** get the vector of X and Y coordinates for the i-th character
   *
   * @param i the index of the character

   * @return array[2][number_points] for this character
   *
   */
   public char[][] getCharacterVector(int i) {
     if (i < characterVectors.length) {
       return (characterVectors[i]); 
     } else {
       return null;
     }

   }

   /** set whether this font is 'fixed width' or not (not =
   *   proportional spacing)
   *
   *   Right now, only the 'wmo' font is defaulted to fixed.
   */
   public void setFixedWidth(boolean fw) {
     fixedWidth = fw;
     return;
   }
   /** indicate whether this is a fixed-width font
   *
   * @return true if name indicates fixed with (wmo...)
   *
   */
   public boolean getFixedWidth() {
     fixedWidth = false;
     if (name.toLowerCase().startsWith("wmo")) fixedWidth = true;
      // others?
     return fixedWidth;
   }

   /** indicate whether this is a cursive font
   *
   * @return true if name contains "cursive"
   *
   */
   public boolean getIsCursive() {
     return ( (name.toLowerCase().indexOf("cursive") != -1) );
   }

   private int getInt (InputStream file, int n) throws IOException
   {
      if (file == null) return (-1);

      char[] buf;
      int c;
      int j = 0;
        buf = new char[n];
      // for the specified number of characters
      for (int i = 0; i < n; i++)
      {
         c = file.read ();
         // get character and discard spare newlines
         while ((c == '\n') || (c == '\r'))
         {
            c = file.read ();
         }
         // if we hit end of file
         if (c == -1)
         {
            // return an error
            return (c);
         }
         // if this is not a blank
         if ((char) c != ' ')
         {
            // save the character
            buf[j++] = (char) c;
         }
      }
      // return the decimal equivilent of the string
      return (Integer.parseInt (String.copyValueOf (buf, 0, j)));
   }
/******************************************************************************/
/***********************************************************/
   private int fontAdjustment (String fontname)
   {
      int xadjust = 0;
      // if we do not have a script type font
      if (fontname.indexOf ("scri") < 0 )
      {
         // if we have a gothic font
         if (fontname.indexOf ("goth") >= 0)
         {
            xadjust = 2;
         }
         else
         {
            xadjust = 3;
         }
      }
      return (xadjust);
   }

/******************************************************************************/
   private void LoadHersheyFont (String fontname, InputStream fontStream)
   {
      if (fontStream == null) return;
      int character, n;
      int c;
      int xadjust = fontAdjustment (fontname);
        try
      {
         // loop through the characters in the file ...
         character = 0;
         // while we have not processed all of the characters
         while (true)
         {
            // if we cannot read the next field
            if (getInt (fontStream, 5) < 1)
            {
               // we are done, set the font specification for num chars
               charactersInSet = character;
               // break the read loop
               break;
            }
            else
            {
               // get the number of vertices in this character
               n = getInt (fontStream, 3);
               // save it
               numberOfPoints[character] = n;
               // read in the vertice coordinates ...
               for (int i = 0; i < n; i++)
               {
                  // if we are at the end of the line
                  if ((i == 32) || (i == 68) || (i == 104) || (i == 140))
                  {
                     // skip the carriage return
                     fontStream.read ();
                  }
                  // get the next character
                  c = fontStream.read ();
                  // if this is a return ( we have a DOS style file )
                  if (c == '\n')
                  {
                     // throw it away and get another
                     c = fontStream.read ();
                  }
                  // get the x coordinate
                  characterVectors[character][X][i] = (char) c;
                  // read the y coordinate
                  characterVectors[character][Y][i] = (char) fontStream.read ();
               }
               // skip the carriage return
               fontStream.read ();
               // increment the character counter
               character++;
            }
         }
         // determine the size of each character ...
         characterMinX = new int[charactersInSet];
         characterMaxX = new int[charactersInSet];
         // initialize ...
         characterSetMinY = 1000;
         characterSetMaxY = -1000;
         characterSetMinX = 1000;
         characterSetMaxX = -1000;
         // loop through each character ( except the space character )
         for (int j = 1; j < charactersInSet; j++)
         {
            // calculate the size
            calculateCharacterSize (j, xadjust);
         }
         // handle the space character - if the 'a' character is defined
         if (((int) 'a' - (int) ' ') <= charactersInSet)
         {
            // make the space character the same size as the 'a'
            characterMinX[0] = characterMinX[(int) 'a' - (int) ' '];
            characterMaxX[0] = characterMaxX[(int) 'a' - (int) ' '];
         }
         else
         {
            // make the space char the same size as the last char
            characterMinX[0] = characterMinX[charactersInSet - 1];
            characterMaxX[0] = characterMaxX[charactersInSet - 1];
         }
      }
      catch (IOException e)
      {
         System.out.println (e);
      }
      return;
   }
/*************************************************************************************/
   protected void calculateCharacterSize (int j, int xadj)
   {
      int cx,cy;
      characterMinX[j] = 1000;
      characterMaxX[j] = -1000;
      // for all the vertices in the character
      for (int i = 1; i < numberOfPoints[j]; i++)
      {
        cx = (int) characterVectors[j][X][i];   
        cy = (int) characterVectors[j][Y][i]; 
         // if this is not a "skip"
         if (cx != ' ')
         {
            // if this is less than our current minimum
            if (cx < characterMinX[j])
            {
               // save it
               characterMinX[j] = cx;
            }
            if (cx < characterSetMinX) characterSetMinX = cx;

            // if this is greater than our current maximum
            if ( cx > characterMaxX[j])
            {
               // save it
               characterMaxX[j] = cx;
            }
            if (cx > characterSetMaxX) characterSetMaxX = cx;

            // if this is less than our current minimum
            if (cy < characterSetMinY)
            {
               // save it
               characterSetMinY = cy;
            }
            // if this is greater than our current maximum
            if (cy > characterSetMaxY)
            {
               // save it
               characterSetMaxY = cy;
            }
         }
      }
      characterMinX[j] -= xadj;
      characterMaxX[j] += xadj;
   }
/******************************************************************************/
   public String toString()
   {
       return( "HersheyFont: "+name );
   }   
}
