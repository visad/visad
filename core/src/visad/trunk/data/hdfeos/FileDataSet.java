//
// FileDataSet.java
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

  public class FileDataSet 
  {

    Vector dataSet;

    FileDataSet() 
    {
       dataSet = new Vector();
    }

    public void add( FileData f_data ) 
    {
      dataSet.addElement( f_data );
    }

    public int getSize() 
    {
      int size = dataSet.size(); 
      return size;
    }

    public FileData getElement( int ii ) 
    {
      FileData obj = (FileData)dataSet.elementAt( ii );
      return obj;
    }

    public boolean isEmpty() 
    {
      return dataSet.isEmpty();
    }

    public Enumeration getEnum()
    {
      Enumeration e = dataSet.elements();
      return e;
    }

    public MathType getVisADMathType() throws VisADException 
    {
      FileData f_data = null;
      MathType[] M_type =  new MathType[ getSize() ];

      for ( int ii = 0; ii < getSize(); ii++ ) {
        f_data = getElement(ii);

        M_type[ii] = f_data.getVisADMathType();
      }
       
      TupleType T_type = new TupleType( M_type );

      return T_type;
    }

    public DataImpl getVisADDataObject() throws VisADException, RemoteException 
    {
       FileData f_data = null;
       DataImpl[] data = new DataImpl[ getSize() ];
       IndexSet i_set = null;

       for ( int ii = 0; ii < getSize(); ii++ ) {
         f_data = getElement(ii);
         data[ii] = f_data.getAdaptedVisADDataObject( i_set );
       }
       
       TupleType T_type = (TupleType) getVisADMathType();

       Tuple tuple = new Tuple( T_type, data );

       return tuple;
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
