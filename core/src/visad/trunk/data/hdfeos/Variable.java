//
// Variable.java
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


  public class Variable  {

    String  name;
    int rank;
    int type;
    DimensionSet  dimSet;


    Variable( String name, DimensionSet set, int rank, int type ) 
    {
  
       if ( set.getSize() != rank ) 
       {
         /* throw Exception:  problem with DimensionSet size */
       }
 

       this.name = name;
       this.dimSet = set;
       this.type = type;
       this.rank = rank;
    }

    public String getName() 
    {

      String name = this.name;
      return name;
    }

    public int getRank() 
    {
       return rank;
    }

    public boolean equals( Variable obj ) 
    {

      if( this.name.equals( obj.getName()) ) {

         return true;
       }
       else {

         return false;
       }
    }

    public DimensionSet getDimSet()
    {
       return dimSet;
    }

    public NamedDimension getDim( int ii )
    {
       return dimSet.getElement( ii );

    }

    public int getNumberType()
    {
       return this.type;
    }

    public String toString()  {

       String str = "Variable:  "+name+"\n"+
                    "    rank:  "+rank+"\n"+
                    "    type:  "+type+"\n"+"  "+dimSet.toString()+"\n";
       return str;
    }

  }
