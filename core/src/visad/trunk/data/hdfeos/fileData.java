package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import java.rmi.*;
import experiment.*;
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
