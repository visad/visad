package visad.data.hdfeos;

import java.util.*;

  public class indexSet  {

    dimensionSet  D_Set;
    Vector indices;

    public indexSet( namedDimension dim, int index, indexSet i_set )
    {
  
       D_Set = new dimensionSet();
       indices = new Vector();

       if ( i_set == null ) 
       {
          D_Set.add( dim );

          int[] i_array = new int[1]; 
          i_array[0] = index;
          indices.addElement( i_array );
       }
       else 
       {
         int size = i_set.getSize();

         for ( int ii = 0; ii < size; ii++ ) 
         {
           this.D_Set.add( i_set.getDim(ii) );
           int[] i_array = new int[1];  
           i_array[0] = i_set.getIndex(ii);
           this.indices.addElement( i_array );
           i_array = null;
         }

         int[] i_array = new int[1];
         i_array[0] = index;
         
         this.D_Set.add( dim );
         indices.addElement( i_array );

       }
 

    }

    public int getSize() 
    {
      return this.D_Set.getSize();
    }
 
    public namedDimension getDim( int ii ) 
    {
      return D_Set.getElement( ii );

    }

    public int getIndex( namedDimension dim )
    {
      int size = this.getSize();

      for ( int ii = 0; ii < size; ii++ ) {
 
        if( D_Set.isMemberOf( dim ) ) {
     
          int[] i_array = (int[]) indices.elementAt( ii );
          return i_array[0];
        }

      }

      return -1;
    }

    public int getIndex( int ii )
    {
      int[] i_array = (int[]) indices.elementAt( ii );
      return i_array[0];
    }

    public boolean isMemberOf( namedDimension dim ) 
    {

      return D_Set.isMemberOf( dim );
    }

    public String toString()  {

      String str;

       return str = "null";
    }

  }
