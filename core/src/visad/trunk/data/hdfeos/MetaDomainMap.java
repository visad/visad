//
// MetaDomainMap.java
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
import java.lang.*;
import visad.Set;
import visad.MathType;
import visad.Gridded1DSet;
import visad.RealTupleType;
import visad.VisADException;

  class MetaDomainMap extends MetaDomain {

    private int struct_id;

    final static int FLOAT = 5;
    final static int DOUBLE = 6;
    final static int INT = 24;

    private MathType M_type = null;

    GctpMap gridMap;

    public MetaDomainMap( int struct_id, GctpMap gridMap  ) {

      super();

      this.struct_id = struct_id;
      this.gridMap = gridMap;
    }

    public Set getVisADSet( IndexSet i_set ) throws VisADException {

      Set VisADset = null;
      MathType M_type = null;
       
      if ( this.M_type != null ) {
     
        M_type = this.M_type;
      }
      else {

        M_type = gridMap.getVisADMathType();
        this.M_type = M_type;
      }

      VisADset = gridMap.getVisADSet();

      return VisADset;
    }

    public MathType getVisADMathType() throws VisADException {

      MathType M_type = null;

      if ( this.M_type != null ) 
      { 
        return this.M_type;
      }
      else 
      {

        M_type =  this.gridMap.getVisADMathType();

        Gridded1DSet defaultSet = new Gridded1DSet(M_type, null, 1);

        ((RealTupleType)M_type).setDefaultSet( (Set)defaultSet );

        this.M_type = M_type;

      }
        return M_type;
    }
 }
