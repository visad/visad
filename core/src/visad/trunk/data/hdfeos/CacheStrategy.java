package visad.data.hdfeos;

import visad.*;
import java.lang.*;
import java.rmi.*;

public class CacheStrategy 
{

   public CacheStrategy()
   {

   }

   public int allocate( FlatField[] adaptedFlatFields,
                        boolean[] adaptedFlatFieldDirty,
                        long[] adaptedFlatFieldSizes,
                        long[] adaptedFlatFieldTimes )
   {

      int adaptedFlatFieldIndex = 0;
      long oldest = adaptedFlatFieldTimes[0];

      for ( int ii = 0; ii < adaptedFlatFields.length; ii++ ) 
      {

         if ( adaptedFlatFields[ii] == null ) 
         {
           adaptedFlatFieldIndex = ii;
           return adaptedFlatFieldIndex;
         }
         else if ( adaptedFlatFieldTimes[ii] < oldest ) 
         {
           oldest = adaptedFlatFieldTimes[ii];
           adaptedFlatFieldIndex = ii;
         }
      }

      return adaptedFlatFieldIndex;
   }

}
