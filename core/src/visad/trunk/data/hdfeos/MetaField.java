//
// MetaField.java
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
import visad.FieldImpl;
import visad.TupleType;
import visad.Tuple;
import visad.DataImpl;
import visad.GriddedSet;
import visad.Gridded1DSet;
import visad.TypeException;
import visad.VisADException;
import visad.RealTupleType;


class MetaField extends FileData  {

  MetaDomainSimple  domain;
  Set VisADset = null;
  FileData  leaf;
  Vector next;

  public MetaField( MetaDomainSimple domain, FileData leaf, MetaField next ) {

    super();

    this.next = new Vector();
    this.domain = domain;
    this.leaf = leaf;
    if ( next != null ) {
      this.next.addElement( next );
    }
  }

  static MetaField getLink( Enumeration enum, FileData m_field ) {

    MetaField link;
    MetaField u_link;
    MetaDomainSimple m_domain;

    if ( ( enum == null ) && ( m_field == null ) ) {  // top node

      link = new MetaField( null, null, null );
      return link;
    }

    if ( enum.hasMoreElements() ) {

       m_domain = (MetaDomainSimple) enum.nextElement();
          
       link = getLink( enum, m_field );
       u_link = new MetaField( m_domain, null, link );
       return u_link;
    }
    else {
       
      link = new MetaField( null, m_field, null );
      return link;
    }
  }

/**--  TO DO   - - - - - - - - - - -

  static FunctionLink mergeLink( FunctionLink link, FunctionLink n_link ) {

      FunctionLink t_link;
      FunctionLink m_link;
      t_link = link;
      m_link = n_link;
      boolean match;
      int count;

      while ( !(t_link.next.isEmpty()) ||
              !(m_link.next.isEmpty()) ) {

        count = t_link.getNextSize();
        match = false;
        for ( int ii = 0; ii < count; ii++ ) {

          if ( t_link.next[ii].dim.equals( m_link.next[0].dim ) ) {

            match = true;
            t_link = t_link.next[ii];
            m_link = m_link.next[0];
          }
        }

        if ( match ) {
            t_link.next.addElement( m_link.next[0] );
            return link;
        }

      }

      return link;  //  Exception: "branch already exists"
    }
*/


  public int getNextSize() {

    return next.size();
  }


  public MetaField getNext( int ii ) {

    return (MetaField)next.elementAt( ii );
  }

  public MetaDomainSimple getBranch() {

    return this.domain;
  } 

  public FileData getLeaf() {

    return this.leaf;
  }

  public Set getVisADSet() throws VisADException {

    if ( this.VisADset != null ) {
      return this.VisADset;
    }
    else {
     this.VisADset = this.domain.getVisADSet( null ); 
     return this.VisADset;
    } 
  }

  static MathType getVisADMathType( MetaField link ) throws VisADException { 

      int n_next = link.getNextSize();

      if ( n_next == 0 ) {
 
        MathType M_type = link.getLeaf().getVisADMathType();

        return M_type;
      }
      else { 

        MathType D_type = link.getBranch().getVisADMathType();
        MathType[] T_type = new MathType[ n_next ];

        for ( int ii = 0; ii < n_next; ii++ ) {
 
          T_type[ii] = getVisADMathType( link.getNext(ii) );
        }

        MathType R_type = new TupleType( T_type );
  
        MathType F_type = (MathType) new FunctionType( D_type, R_type ); 
   
        return F_type;

      }
  }

  public MathType getVisADMathType() throws VisADException {

     MathType M_type = MetaField.getVisADMathType( this );
     return M_type;
  }

  public DataImpl getVisADDataObject( IndexSet i_set ) throws VisADException, RemoteException {

     MathType M_type = MetaField.getVisADMathType( this );
     DataImpl data = MetaField.getVisADDataObject( null, M_type, this);
     return data;
  }

  static DataImpl getVisADDataObject( IndexSet i_set, MathType M_type, MetaField link )
        throws VisADException, RemoteException
  {

     int size;
     int n_next = link.getNextSize();
     FunctionType F_type;
     TupleType R_type;

     if ( n_next == 0 ) {

       DataImpl datum = link.getLeaf().getAdaptedVisADDataObject( i_set );
 
       return datum;

     }
     else {

       Set domainSet = link.getVisADSet();
       size = domainSet.getLength();
       F_type = (FunctionType) M_type;
       R_type = (TupleType)F_type.getRange();

       FieldImpl F_func = new FieldImpl( (FunctionType)M_type, domainSet ); 

       DataImpl[] range = new DataImpl[ n_next ];


       for ( int ii = 0; ii < size; ii++ ) {

         NamedDimension n_dim = link.domain.getDimSet().getElement(0);

         IndexSet idx = new IndexSet( n_dim, ii, i_set );

         for ( int jj = 0; jj < n_next; jj++ ) 
         {
           range[jj] = getVisADDataObject( idx, R_type.getComponent(jj),
                                             link.getNext(jj) );
         }

         Tuple r_tuple = new Tuple( R_type, range, false );

         F_func.setSample( ii, r_tuple );

       }

         return (DataImpl) F_func;

     }

  }

}
