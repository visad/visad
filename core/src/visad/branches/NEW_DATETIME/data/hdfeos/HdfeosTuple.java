//
// HdfeosTuple.java
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
import java.rmi.*;
import visad.*;
import visad.data.FileFlatField;

public class HdfeosTuple extends HdfeosData
{
  Vector dataSet;
  HdfeosData[] elements = null;
  int length;
  private MathType mathtype;
  private DataImpl tuple = null;
  private DataImpl[] datas = null;

  HdfeosTuple( HdfeosData[] elements ) 
             throws VisADException, RemoteException
  {
    dataSet = new Vector();
    length = elements.length;
    this.elements = new HdfeosData[length];
    this.datas = new DataImpl[length];
    System.arraycopy( elements, 0, this.elements, 0, length );

    MathType[] m_types = new MathType[length];
    for ( int ii = 0; ii < length; ii++ ) {
      m_types[ii] = elements[ii].getType();
    }
    mathtype = (MathType) new TupleType( m_types );
  }

  public MathType getType()
         throws VisADException
  {
    return mathtype;
  }

  public DataImpl getData()
         throws VisADException, RemoteException
  {
    if ( tuple == null ) 
    {
      for ( int ii = 0; ii < length; ii++ ) {
        datas[ii] = elements[ii].getData();
      }
      tuple = (DataImpl) new Tuple( datas );
    }
    return tuple;
  }

  public DataImpl getData( int[] indexes ) 
         throws VisADException, RemoteException
  {
    for ( int ii = 0; ii < length; ii++ ) {
      datas[ii] = elements[ii].getData( indexes );
    }
    Tuple tuple = new Tuple( datas );
    return tuple;
  }

  public DataImpl getAdaptedData()
         throws VisADException, RemoteException
  {
    if ( tuple == null )
    {
      for ( int ii = 0; ii < length; ii++ ) {
        datas[ii] = elements[ii].getAdaptedData();
      }
      tuple = (DataImpl) new Tuple( (TupleType)mathtype, datas, false );
    }
    return tuple;
  }

  public DataImpl getAdaptedData( int[] indexes )
         throws VisADException, RemoteException
  {
    for ( int ii = 0; ii < length; ii++ ) {
      datas[ii] = elements[ii].getAdaptedData( indexes );
    }
    Tuple tuple = new Tuple( (TupleType)mathtype, datas, false );
    return tuple;
  }

  public int getSize() 
  {
    return length;
  }

  public HdfeosData getElement( int ii ) 
  {
    HdfeosData data = elements[ii];
    return data;
  }

  public Enumeration getEnum()
  {
    Enumeration e = dataSet.elements();
    return e;
  }

  public String toString() 
  {
    String str = "dataSet: \n";
    for ( int ii = 0; ii < this.getSize(); ii++ )
    {
       str = str + "  "+((this.getElement(ii)).toString())+"\n";
    }
    return str;
  }
}
