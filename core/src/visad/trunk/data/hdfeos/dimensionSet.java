package visad.data.hdfeos;

import java.util.*;
import java.lang.*;

public class dimensionSet  {

  private Vector dimSet;
  private boolean finished = false;

  dimensionSet()  {

     dimSet = new Vector();
  }

  public void add( namedDimension obj )  {

    if (! finished ) 
    {
      dimSet.addElement( obj );
    }
    else 
    {
      /* throw Exception: obj finished  */
    }
  }

  public void setToFinished()  {

     finished = true;
  }

  public int getSize()  {

    int size = dimSet.size();
    return size;
  }

  public namedDimension getElement( int ii )  {

    namedDimension obj = (namedDimension)dimSet.elementAt( ii );

    return obj;
  }

  public boolean sameSetSameOrder( dimensionSet  dimSet )  {

    int size = this.getSize();

    if ( size != dimSet.getSize() ) {
      return false;
    }

    for ( int ii = 0; ii < size; ii++ ) {

      if ( ! (this.getElement(ii).equals( dimSet.getElement(ii)))  ) {
        return false;
      }
    }
     return true;
  }

  public boolean subsetOfThis( dimensionSet dimSet )  {

     int size = this.getSize();
     int size_arg = dimSet.getSize();


     if ( size_arg > size ) {
        return false;
     }
     else {

       for ( int ii = 0; ii < size_arg; ii++ )  {
   
         namedDimension obj = (namedDimension)dimSet.getElement( ii );
         boolean equal = false;

         for ( int jj = 0; jj < size; jj++ )  {

            if( obj.equals( (namedDimension)this.getElement( jj ) )) {
            
                equal = true;
            }
         }
        
         if ( !equal ) {
            return false;
         }

       }
     }

    return true;

  }

  public namedDimension getByName( String dimName )  {

    for ( int ii = 0; ii < this.getSize(); ii++ ) {

       namedDimension obj = (namedDimension)this.getElement(ii);

       String name = obj.getName();

      if ( name.equals( dimName )) {
        return obj;
      }

    }
        return null;
  }

  public boolean isMemberOf( namedDimension dim ) {

    String in_name = dim.getName();

    for ( int ii = 0; ii < this.getSize(); ii++ ) {

      namedDimension obj = (namedDimension)this.getElement(ii);

      String name = obj.getName();

      if ( (in_name).equals( name )) {
        return true;
      }

    }

    return false;
  }

  public String toString()  {

     String str = "dimensionSet: \n";
                
     for ( int ii = 0; ii < this.getSize(); ii++ ) 
     {
        str = str + "   "+((this.getElement(ii)).toString())+"\n";
     } 
 
     return str;
  }

}
