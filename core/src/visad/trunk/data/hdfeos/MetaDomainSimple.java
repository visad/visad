//
// MetaDomainSimple.java
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
import visad.RealType;
import visad.GriddedSet;
import visad.Gridded1DSet;
import visad.IntegerNDSet;
import visad.TypeException;
import visad.VisADException;
import visad.RealTupleType;


  class MetaDomainSimple {

    private EosStruct struct;

    private MathType M_type = null;

  /*
    The dimensions of the sample domain
    - - - - - - - - - - - - - - - - - -  */
    private DimensionSet dimSet;  

    public MetaDomainSimple( EosStruct struct ) 
    {
      this.struct = struct;
      dimSet = new DimensionSet();
    }

    public DimensionSet getDimSet() 
    {
      return dimSet;
    }

    public void addDim( NamedDimension dim ) 
    {
       dimSet.add( dim );
    }

    Set getVisADSet( IndexSet i_set ) throws VisADException 
    {

      Set VisADset = null;
      MathType M_type = null;
      Variable var;
      String R_name;
      DimensionSet varDimSet;
      NamedDimension v_dim;
      NamedDimension dim;
      int subDims;
      int len;
      int idx;
      int v_idx;
      int jj;
      int ii;
      int op;
      int n_dims;
       
      if ( this.M_type != null ) 
      {
        M_type = this.M_type;
      }
      else 
      {
        M_type = getVisADMathType();
        this.M_type = M_type;
      }

      int rank = dimSet.getSize();
      int[] lengths = new int[ rank ];   

      for( ii = 0; ii < rank; ii++ ) 
      {
        lengths[ii] = dimSet.getElement(ii).getLength();
      }

      VisADset = (Set) new IntegerNDSet( M_type, lengths, null, null, null );

      return VisADset;
    }

    public MathType getVisADMathType() 
           throws VisADException 
    {

      MathType M_type = null;
      RealType R_type = null;

      if ( this.M_type != null ) 
      { 
        return this.M_type;
      }
      else 
      {
        int rank = dimSet.getSize();
        RealType[] R_types = new RealType[ rank ];

        for ( int ii = 0; ii < rank; ii++ ) 
        {
          String name = dimSet.getElement(ii).getName();

          try
          {
            R_type = new RealType( name, null, null );
          }
          catch ( VisADException e )
          {
            if ( e instanceof TypeException )
            {
              R_type = RealType.getRealTypeByName( name );
            }
            else
            {
              throw e;
            }
          }

          R_types[ii] = R_type;
        }
     
        if ( rank == 1 ) 
        {
          M_type = (MathType) R_types[0];
        }
        else if ( rank > 1 )
        {
          M_type = (MathType) new RealTupleType( R_types );
        }
        this.M_type = M_type;
      }
        return M_type;
    }

 }
