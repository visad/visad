//
// CacheStrategy.java
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

package visad.data;

import visad.*;
import java.lang.*;
import java.rmi.*;

public class CacheStrategy 
{

   public CacheStrategy()
   {

   }

   public int allocate( FlatField[] adaptedFlatFields,
                        boolean[] adaptedFlatFieldDirty,
                        long[] adaptedFlatFieldSizes,
                        long[] adaptedFlatFieldTimes )
   {

      int adaptedFlatFieldIndex = 0;
      long oldest = adaptedFlatFieldTimes[0];

      for ( int ii = 0; ii < adaptedFlatFields.length; ii++ ) 
      {

         if ( adaptedFlatFields[ii] == null ) 
         {
           adaptedFlatFieldIndex = ii;
           return adaptedFlatFieldIndex;
         }
         else if ( adaptedFlatFieldTimes[ii] < oldest ) 
         {
           oldest = adaptedFlatFieldTimes[ii];
           adaptedFlatFieldIndex = ii;
         }
      }

      return adaptedFlatFieldIndex;
   }

}
