//
// NamedDimension.java
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


  public class NamedDimension  
  {

    private String  name;
    private int  length;
    private GeoMap g_map;
    private boolean unLimitFlag = false;

    NamedDimension( int struct_id, String name, int length, GeoMap g_map ) 
    {

      this.name = name;
      if ( length == 0 ) {
        unLimitFlag = true;
      }
      this.length = length;
      this.g_map = g_map;
    }

    public String getName()  {

      return this.name;
    }

    public boolean equals( NamedDimension obj ) 
    {

      if( this.name.equals( obj.getName() )) {

         return true;
      }
      else {

         return false;
      }
    }

    public void setLength( int len ) {

      length = len;
      return;
    }

    public int getLength()
    {
      return length;
    }

    public GeoMap getGeoMap() {

      return g_map;
   }

   public boolean isGeoMapDefined() {

     if ( g_map == null ) {
       return false;
     }
     else {
       return true;
     }

   }

   public boolean isUnlimited() {

     return this.unLimitFlag;
   }

    public String toString() 
    {

       String str = "dimension: "+name+"\n"+
                    "   length: "+length+"\n";
       return str;
    }


  }
