  package visad.data.hdfeos;

  import java.lang.*;
  import java.util.*;


  public class geoMapSet {

    Vector mapSet;
  
    public geoMapSet() {
      mapSet = new Vector(); 
    }

    public void add( geoMap obj ) {

      mapSet.addElement( obj );
    }

    public int getSize()  {

       int size = mapSet.size();
       return size;
    }

    public geoMap getElement( int ii )  {

       if ( mapSet.size() == 0 ) {

         return null;
       }
       else {

         geoMap obj = (geoMap) mapSet.elementAt(ii);
         return obj;
       }
    }

    public geoMap getGeoMap( namedDimension obj ) {

      String name = obj.getName();

      return getGeoMap( name );
    }

    public geoMap getGeoMap( String name ) {


      int size = this.getSize();

      if ( size == 0 )  {

         return null;
      }
      else {

        for ( int ii = 0; ii < size; ii++ ) {
       
          geoMap obj = (geoMap) mapSet.elementAt(ii);

          if(( obj.toDim.equals( name ) ) || ( obj.fromDim.equals( name ) )) {
                 
            return obj;
          }

        }
        return null;
      }
    }

  }
