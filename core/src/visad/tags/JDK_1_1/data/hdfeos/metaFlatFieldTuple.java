//
// metaFlatFieldTuple.java
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
import java.rmi.*;
import experiment.*;
import visad.*;

class metaFlatFieldTuple extends fileData  {

   int struct_id;
   metaDomain domainSet;
   variable range_var;

   MathType M_type = null;

   namedDimension n_dim;
   int v_rank;
   int n_fields;
   int[] num_type;
   String[] F_name;
   dimensionSet d_set;

   int[] start;
   int[] edge;
   int[] stride;

  public metaFlatFieldTuple( int struct_id, metaDomain m_dom, variableSet range_vars )  
  {
    super();

    this.struct_id = struct_id;
    this.domainSet = m_dom;
    this.range_var = range_var;
   
    n_fields = range_vars.getSize();
    num_type = new int[ n_fields ];
    F_name = new String[ n_fields ];
  
    v_rank = range_vars.getElement(0).getRank();
    d_set = range_vars.getElement(0).getDimSet();

    for ( int ii = 0; ii < n_fields; ii++ ) {
      num_type[ii] = range_vars.getElement(ii).getNumberType();
      F_name[ii] = range_vars.getElement(ii).getName();
    }

    start = new int[ v_rank ];
    edge = new int[ v_rank ];
    stride = new int[ v_rank ];

    for ( int ii = 0; ii < v_rank; ii++ ) {

      n_dim = d_set.getElement(ii);

      start[ii] = 0;
      edge[ii] = n_dim.getLength();
      stride[ii] = 1;

    }

  }

  public DataImpl getVisADDataObject( indexSet i_set ) throws VisADException, RemoteException
  {
    int ii;

    Set D_set = this.domainSet.getVisADSet( i_set );
   
    FunctionType F_type = (FunctionType) getVisADMathType();

    FlatField F_field = new FlatField( F_type, D_set );

    int stat;
    int samples = 1;

    if ( i_set != null ) {

      for ( ii = 0; ii < i_set.getSize(); ii++ ) {

        n_dim = i_set.getDim(ii);

        if ( d_set.isMemberOf( n_dim ) ) {
          
          start[ii] = i_set.getIndex( n_dim );
          edge[ii] = 1;
        }
        else {
                // Exception

        }
      }

    }

    for ( ii = 0; ii < v_rank; ii++ ) {

      samples = samples*edge[ii];
      System.out.println( "start: "+start[ii]);
      System.out.println( "edge: "+edge[ii]);
      System.out.println( "stride: "+stride[ii]);
    }


    float[][] data = new float[ n_fields ][ samples ];
 
    stat = -1;

    for ( ii = 0; ii < n_fields; ii++ ) {

      stat = eos.SWreadfield( struct_id, F_name[ii], start, stride, edge, num_type[ii], data[ii] );
    }

    F_field.setSamples( data );

    return (DataImpl)F_field; 

  }

  public MathType getVisADMathType() throws VisADException 
  {

    MathType M_type = null;

    if ( this.M_type != null ) 
    {
      return this.M_type;
    }
    else
    {

      MathType D_type = domainSet.getVisADMathType();

      RealType[] R_type = new RealType[ n_fields ];

      for ( int ii = 0; ii < n_fields; ii++ ) {

        String name = F_name[ii];
        R_type[ii]  = new RealType( name, null, null );
      }

      RealTupleType T_type = new RealTupleType( R_type, null, null );

      FunctionType F_type = new FunctionType( D_type, T_type );

      this.M_type = (MathType) F_type;
      return  (MathType)F_type;
     }
  }

}
