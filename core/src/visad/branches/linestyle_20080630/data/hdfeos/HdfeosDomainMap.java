//
// HdfeosDomainMap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import visad.Set;
import visad.VisADException;

public class HdfeosDomainMap extends HdfeosDomain
{
  private GctpMap gridMap;
  private Set set = null;

  public HdfeosDomainMap( EosStruct struct,
                          DimensionSet dimSet,
                          GctpMap gridMap  )
         throws VisADException
  {
    super(struct, dimSet, gridMap.getVisADCoordinateSystem(),
                          gridMap.getUnits());
    this.gridMap = gridMap;
  }

  public Set getData( )
         throws VisADException
  {
    set = gridMap.getVisADSet(mathtype);
    return set;
  }

  public Set getData( int[] indexes )
         throws VisADException
  {
    if ( set == null )
    {
      set = gridMap.getVisADSet(mathtype);
    }
    return set;
  }
}
