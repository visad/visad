//
// shape.java
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


 public class shape 
 {


    private dimensionSet  dimSet;
    private variableSet  varSet;

    public shape( variable var  ) 
    {

       varSet = new variableSet();
       varSet.add( var );

       dimSet = var.getDimSet();
    }

    public void addVariable( variable var ) 
    {
       varSet.add( var );
    }

    public dimensionSet getShape() 
    {
       return dimSet;
    }

    public variableSet getVariables() {
  
       return varSet;
    }

    public int getNumberOfVars() {

       return varSet.getSize();
    }

    public boolean memberOf( variable var ) 
    {

       dimensionSet d_set = var.getDimSet();

       if( this.dimSet.sameSetSameOrder( d_set ) )
       {
          return true;
       }
       else 
       {
          return false;
       }

    }

    public String toString() 
    {
       String str = dimSet.toString()+"  variables: \n";

       for( int ii = 0; ii < varSet.getSize(); ii++ ) 
       {
         str = str + "       "+(varSet.getElement(ii)).getName() + "\n";
       }
         str = str + "- - - - - - - - - - - - - - - - - - - \n";
       
      return str;
    }
 }
