//
// MetaFlatFieldSimple.java
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
import visad.Set;
import visad.MathType;
import visad.RealType;
import visad.FunctionType;
import visad.TupleType;
import visad.Tuple;
import visad.DataImpl;
import visad.FlatField;
import visad.VisADException;
import visad.TypeException;


class MetaFlatFieldSimple extends FileData {

   int struct_id;
   MetaDomain domainSet;
   Variable range_var;

   MathType M_type = null;

   NamedDimension n_dim;
   int v_rank;
   int num_type;
   String F_name;
   DimensionSet d_set;

   int[] start;
   int[] edge;
   int[] stride;

  public MetaFlatFieldSimple( int struct_id, MetaDomain m_dom, Variable range_var )  
  {

    super();

    this.struct_id = struct_id;
    this.domainSet = m_dom;
    this.range_var = range_var;

    v_rank = range_var.getRank();
    num_type = range_var.getNumberType();
    F_name = range_var.getName();
    d_set = range_var.getDimSet();

    start = new int[ v_rank ];

    edge = new int[ v_rank ];
    stride = new int[ v_rank ];

    for ( int ii = 0; ii < v_rank; ii++ ) {

      n_dim = range_var.getDim(ii);

      start[ii] = 0;
      edge[ii] = n_dim.getLength();
      stride[ii] = 1;

    }

  }

  public DataImpl getVisADDataObject( IndexSet i_set ) throws VisADException, RemoteException
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

          throw new HdfeosException(" named dimension incompatible ");
        }
      }

    }

    for ( ii = 0; ii < v_rank; ii++ ) {

      samples = samples*edge[ii];
    }


    float[][] data = new float[ 1 ][ samples ];

    ReadSwathGrid.SWreadfield( struct_id, F_name, start, stride, edge, num_type, data[0] );

    F_field.setSamples( data );

    return F_field; 

  }

  public MathType getVisADMathType() throws VisADException 
  {

    MathType M_type = null;
    RealType R_type = null;

    if ( this.M_type != null ) 
    {
      return this.M_type;
    }
    else
    {
      MathType D_type = domainSet.getVisADMathType();

      String name = range_var.getName();

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
        
      FunctionType F_type = new FunctionType( D_type, R_type );

      this.M_type = (MathType) F_type;
      return  (MathType)F_type;
     }
  }

}
