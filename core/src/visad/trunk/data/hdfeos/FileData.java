//
// FileData.java
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
import visad.data.CacheStrategy;
import visad.data.FileFlatField;
import visad.data.FileAccessor;
import visad.*;

abstract class FileData  {

  static CacheStrategy c_strategy = new CacheStrategy();

  public FileData()
  {

  }

  public abstract DataImpl getVisADDataObject( IndexSet i_set ) throws VisADException,
     RemoteException;

  public abstract MathType getVisADMathType() throws VisADException;

  public DataImpl getAdaptedVisADDataObject( IndexSet i_set ) throws VisADException,
     RemoteException
  {

        if ( this instanceof MetaField ) {

          return getVisADDataObject( i_set );
        }
        else if ( this instanceof MetaFlatFieldSimple ) {

          HdfeosAccessor accessor = new HdfeosAccessor( this, i_set );
        
          FileFlatField FF_field = new FileFlatField( (FileAccessor)accessor, c_strategy ); 

          return FF_field;
        }
        else if ( this instanceof MetaFlatField ) {

          FileFlatField[] FF_field = new FileFlatField[ ((MetaFlatField)this).getSize() ];

          for ( int ii = 0; ii < ((MetaFlatField)this).getSize(); ii++ ) {

            HdfeosAccessor accessor = 
                new HdfeosAccessor( ((MetaFlatField)this).getElement(ii), i_set );
 
            FF_field[ii] = new FileFlatField( (FileAccessor)accessor, c_strategy );
          }


           Tuple tuple = new Tuple( (TupleType)this.getVisADMathType(), FF_field, false );
  
           return tuple;

        }
 

    return null;
  }

}
