//
// MetaFlatField.java
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
import visad.FunctionType;
import visad.TupleType;
import visad.Tuple;
import visad.DataImpl;
import visad.FlatField;
import visad.VisADException;

class MetaFlatField extends FileData  {

   int struct_id;
   int n_fields;
   MetaDomain domainSet;
   VariableSet range_vars;
   Vector m_simple = new Vector();

   MathType M_type = null;

  public MetaFlatField( int struct_id, MetaDomain m_dom, VariableSet range_vars )  
  {

    super();

    this.struct_id = struct_id;
    this.domainSet = m_dom;
    this.range_vars = range_vars;

    n_fields = range_vars.getSize();
  
    for ( int ii = 0; ii < n_fields; ii++ ) {

       MetaFlatFieldSimple obj = new MetaFlatFieldSimple( struct_id, m_dom, 
                                 range_vars.getElement(ii) );

       this.m_simple.addElement(obj);
    }


  }

  public int getSize() {
 
    return this.m_simple.size();
  }

  public MetaFlatFieldSimple getElement( int ii ) {

     return (MetaFlatFieldSimple) this.m_simple.elementAt(ii);
  }

  public DataImpl getVisADDataObject( IndexSet i_set ) throws VisADException, RemoteException
  {
    int ii;

    Set D_set = this.domainSet.getVisADSet( i_set );
   
    TupleType T_type = (TupleType) getVisADMathType();

    FlatField[] F_field = new FlatField[ n_fields ];
 
    for ( ii = 0 ; ii < n_fields; ii++ ) {

      F_field[ii] = (FlatField) (this.getElement(ii)).getVisADDataObject( i_set );
    }

      Tuple T_fields = new Tuple( T_type, F_field );

      return (DataImpl)T_fields;
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

      MathType[] F_type = new MathType[ n_fields ];

      for ( int ii = 0; ii < n_fields; ii++ ) {

        F_type[ii]  = (MathType) (this.getElement(ii)).getVisADMathType();
      }

      TupleType T_type = new TupleType( F_type );
      this.M_type = (MathType) T_type;
      return  (MathType)T_type;

    }
  }

}
