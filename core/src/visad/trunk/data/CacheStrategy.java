//
// CacheStrategy.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data;

import visad.*;
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
