//
// fileData.java
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
import visad.*;

abstract class fileData  {

  static CacheStrategy c_strategy = new CacheStrategy();

  public fileData()
  {

  }

  public abstract DataImpl getVisADDataObject( indexSet i_set ) throws VisADException,
     RemoteException;

  public abstract MathType getVisADMathType() throws VisADException;

  public DataImpl getAdaptedVisADDataObject( indexSet i_set ) throws VisADException,
     RemoteException
  {

        if ( this instanceof metaField ) {

          return getVisADDataObject( i_set );
        }
        else if ( this instanceof metaFlatFieldSimple ) {

          hdfeosAccessor accessor = new hdfeosAccessor( this, i_set );
        
          FileFlatField FF_field = new FileFlatField( accessor, c_strategy ); 

          return FF_field;
        }
        else if ( this instanceof metaFlatField ) {

          FileFlatField[] FF_field = new FileFlatField[ ((metaFlatField)this).getSize() ];

          for ( int ii = 0; ii < ((metaFlatField)this).getSize(); ii++ ) {

            hdfeosAccessor accessor = 
                new hdfeosAccessor( ((metaFlatField)this).getElement(ii), i_set );
 
            FF_field[ii] = new FileFlatField( accessor, c_strategy );
          }


           Tuple tuple = new Tuple( (TupleType)this.getVisADMathType(), FF_field, false );
  
           return tuple;

        }
 

    return null;
  }

}
