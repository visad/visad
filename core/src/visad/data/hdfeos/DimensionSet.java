//
// DimensionSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.hdfeos;

import java.util.*;

public class DimensionSet
{
  private Vector dimSet;
  private boolean finished = false;

  DimensionSet()
  {
    dimSet = new Vector();
  }

  DimensionSet( NamedDimension[] dims )
  {
    dimSet = new Vector();
    for ( int ii = 0; ii < dims.length; ii++ ) {
      dimSet.add(dims[ii]);
    }
  }

  public void add( NamedDimension obj )
  {
    if (! finished )
    {
      dimSet.addElement( obj );
    }
    else
    {
      /* throw Exception: obj finished  */
    }
  }

  public void setToFinished()
  {
    finished = true;
  }

  public int getSize()
  {
    int size = dimSet.size();
    return size;
  }

  public NamedDimension getElement( int ii )
  {
    NamedDimension obj = (NamedDimension)dimSet.elementAt( ii );

    return obj;
  }

  public NamedDimension[] getElements()
  {
    NamedDimension[] array = new NamedDimension[getSize()];
    for ( int ii = 0; ii < getSize(); ii++ ) {
      array[ii] = getElement(ii);
    }
    return array;
  }

  public int getIndexOf( NamedDimension dim )
  {
    for ( int ii = 0; ii < getSize(); ii++ ) {
      if ( (getElement(ii)).equals(dim) ) {
        return ii;
      }
    }
    return -1;
  }

  public boolean sameSetSameOrder( DimensionSet  dimSet )
  {
    int size = this.getSize();

    if ( size != dimSet.getSize() ) {
      return false;
    }

    for ( int ii = 0; ii < size; ii++ )
    {
      if ( ! (this.getElement(ii).equals( dimSet.getElement(ii)))  ) {
        return false;
      }
    }
     return true;
  }

  public boolean subsetOfThis( DimensionSet dimSet )
  {
     int size = this.getSize();
     int size_arg = dimSet.getSize();

     if ( size_arg > size ) {
        return false;
     }
     else {

       for ( int ii = 0; ii < size_arg; ii++ )  {

         NamedDimension obj = (NamedDimension)dimSet.getElement( ii );
         boolean equal = false;

         for ( int jj = 0; jj < size; jj++ )  {

            if( obj.equals( (NamedDimension)this.getElement( jj ) )) {

                equal = true;
            }
         }

         if ( !equal ) {
            return false;
         }

       }
     }

    return true;
  }

  public NamedDimension getByName( String dimName )
  {
    for ( int ii = 0; ii < this.getSize(); ii++ )
    {
      NamedDimension obj = (NamedDimension)this.getElement(ii);

      String name = obj.getName();

      if ( name.equals( dimName )) {
        return obj;
      }
    }
    return null;
  }

  public boolean isMemberOf( NamedDimension dim )
  {
    String in_name = dim.getName();

    for ( int ii = 0; ii < this.getSize(); ii++ ) {

      NamedDimension obj = (NamedDimension)this.getElement(ii);

      String name = obj.getName();

      if ( (in_name).equals( name )) {
        return true;
      }
    }
    return false;
  }

  public String toString()
  {
     String str = "DimensionSet: \n";

     for ( int ii = 0; ii < this.getSize(); ii++ )
     {
        str = str + "   "+((this.getElement(ii)).toString())+"\n";
     }
     return str;
  }
}
