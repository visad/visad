package visad.data.hdfeos;

import java.util.*;

public class shapeSet {

     private Vector S_Set;

     shapeSet( variableSet varSet ) 
     {
        boolean found;

        this.S_Set = new Vector();

        for ( Enumeration e = varSet.getEnum(); e.hasMoreElements(); ) 
        {
           variable var = (variable) e.nextElement();
           int count = S_Set.size();

           if ( count == 0 ) 
           {
              found = true;
              shape s_obj  = new shape( var );
              S_Set.addElement( s_obj );
           }
           else 
           {
              found = false;

             for ( int ii = 0; ii < count; ii++ ) 
             {

                shape s_obj = (shape)S_Set.elementAt(ii);
                if( s_obj.memberOf( var ) ) 
                {
                   s_obj.addVariable( var );
                   found = true;
                }
             }
           }

           if ( !found ) 
           {
              shape s_obj = new shape( var );
              S_Set.addElement( s_obj );
           }
        }

      }

    public shape getElement( int ii )
    {
       shape obj = (shape)S_Set.elementAt( ii );
       return obj;
    }


    public int getSize()
    {
      int size = S_Set.size();
      return size;
    }

    Enumeration getEnum()
    {
      Enumeration e = S_Set.elements();
      return e;
    }


/*
    public shape getElement( namedDimension dim ) {

      int size = this.getSize();
      shape s_obj;
      dimensionSet D_set;
    
      for ( int ii=0; ii < size; ii++ ) {

        s_obj = this.getElement(ii);





      }

    }

*/
    public shape getCoordVar( namedDimension dim ) {

      return null;
    }

    public boolean isMemberOf( namedDimension dim ) {

      dimensionSet d_set;

      for ( int ii = 0; ii < this.getSize(); ii++ ) {
     
        d_set = (this.getElement(ii)).getShape();

        if ( d_set.isMemberOf( dim ) ) {
          return true;
        }

      }

      return false;
    }


    public String toString()
    {

       String str = " Shapes in this set:   \n";

       for ( int ii = 0; ii < this.getSize(); ii++ ) 
       {
          str = str + (this.getElement(ii)).toString() + "\n";
       }
       str = str + " - - - - - - - - - - - - - - - - - - \n";

       return str;
    }
}
