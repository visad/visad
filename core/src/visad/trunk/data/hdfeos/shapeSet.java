//
// shapeSet.java
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

public class shapeSet {

     private Vector S_Set;

     shapeSet( variableSet varSet ) 
     {
        boolean found;

        this.S_Set = new Vector();

        for ( Enumeration e = varSet.getEnum(); e.hasMoreElements(); ) 
        {
           variable var = (variable) e.nextElement();
           int count = S_Set.size();

           if ( count == 0 ) 
           {
              found = true;
              shape s_obj  = new shape( var );
              S_Set.addElement( s_obj );
           }
           else 
           {
              found = false;

             for ( int ii = 0; ii < count; ii++ ) 
             {

                shape s_obj = (shape)S_Set.elementAt(ii);
                if( s_obj.memberOf( var ) ) 
                {
                   s_obj.addVariable( var );
                   found = true;
                }
             }
           }

           if ( !found ) 
           {
              shape s_obj = new shape( var );
              S_Set.addElement( s_obj );
           }
        }

      }

    public shape getElement( int ii )
    {
       shape obj = (shape)S_Set.elementAt( ii );
       return obj;
    }


    public int getSize()
    {
      int size = S_Set.size();
      return size;
    }

    Enumeration getEnum()
    {
      Enumeration e = S_Set.elements();
      return e;
    }


    public shape getCoordVar( namedDimension dim ) {

      return null;
    }

    public boolean isMemberOf( namedDimension dim ) {

      dimensionSet d_set;

      for ( int ii = 0; ii < this.getSize(); ii++ ) {
     
        d_set = (this.getElement(ii)).getShape();

        if ( d_set.isMemberOf( dim ) ) {
          return true;
        }

      }

      return false;
    }


    public String toString()
    {

       String str = " Shapes in this set:   \n";

       for ( int ii = 0; ii < this.getSize(); ii++ ) 
       {
          str = str + (this.getElement(ii)).toString() + "\n";
       }
       str = str + " - - - - - - - - - - - - - - - - - - \n";

       return str;
    }
}
