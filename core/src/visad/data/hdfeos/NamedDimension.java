//
// NamedDimension.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

  public String getName()
  {
    return this.name;
  }

  public boolean equals( NamedDimension obj )
  {
    if( this.name.equals( obj.getName() ))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public void setLength( int len )
  {
    length = len;
    return;
  }

  public int getLength()
  {
    return length;
  }

  public GeoMap getGeoMap()
  {
    return g_map;
  }

  public boolean isGeoMapDefined()
  {
    if ( g_map == null ) {
      return false;
    }
    else {
     return true;
    }
  }

  public boolean isUnlimited()
  {
    return this.unLimitFlag;
  }

  public String toString()
  {
    String str = "dimension: "+name+"\n"+
                 "   length: "+length+"\n";
    return str;
  }
}
