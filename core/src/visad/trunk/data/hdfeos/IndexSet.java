//
// IndexSet.java
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

package visad.data.hdfeos;

import java.util.*;

  public class IndexSet  {

    DimensionSet  D_Set;
    Vector indices;

    public IndexSet( NamedDimension dim, int index, IndexSet i_set )
    {
  
       D_Set = new DimensionSet();
       indices = new Vector();

       if ( i_set == null ) 
       {
          D_Set.add( dim );

          int[] i_array = new int[1]; 
          i_array[0] = index;
          indices.addElement( i_array );
       }
       else 
       {
         int size = i_set.getSize();

         for ( int ii = 0; ii < size; ii++ ) 
         {
           this.D_Set.add( i_set.getDim(ii) );
           int[] i_array = new int[1];  
           i_array[0] = i_set.getIndex(ii);
           this.indices.addElement( i_array );
           i_array = null;
         }

         int[] i_array = new int[1];
         i_array[0] = index;
         
         this.D_Set.add( dim );
         indices.addElement( i_array );

       }
 

    }

    public int getSize() 
    {
      return this.D_Set.getSize();
    }
 
    public NamedDimension getDim( int ii ) 
    {
      return D_Set.getElement( ii );

    }

    public int getIndex( NamedDimension dim )
    {
      int size = this.getSize();

      for ( int ii = 0; ii < size; ii++ ) {
 
        if( D_Set.isMemberOf( dim ) ) {
     
          int[] i_array = (int[]) indices.elementAt( ii );
          return i_array[0];
        }

      }

      return -1;
    }

    public int getIndex( int ii )
    {
      int[] i_array = (int[]) indices.elementAt( ii );
      return i_array[0];
    }

    public boolean isMemberOf( NamedDimension dim ) 
    {

      return D_Set.isMemberOf( dim );
    }

    public String toString()  {

      String str;

       return str = "null";
    }

  }
