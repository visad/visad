package visad.data.hdfeos;

import java.util.*;


 public class shape 
 {


    private dimensionSet  dimSet;
    private variableSet  varSet;

    public shape( variable var  ) 
    {

       varSet = new variableSet();
       varSet.add( var );

       dimSet = var.getDimSet();
    }

    public void addVariable( variable var ) 
    {
       varSet.add( var );
    }

/**
    public Enumeration getDimEnum() 
    {
       Enumeration e = dimSet.getEnum();
       return e;
    }
**/

    public dimensionSet getShape() 
    {
       return dimSet;
    }

    public variableSet getVariables() {
  
       return varSet;
    }

    public int getNumberOfVars() {

       return varSet.getSize();
    }

/**
    public shape getSubsetOfThis( shapeSet s_set ) 
    {

      for ( Enumeration e = s_set.getEnum(); e.hasMoreElements; ) 
      {
 
        shape obj = (shape)e.nextElement();

          if (this.dimSet.isSubsetOfthis( obj.dimSet )) 
          {
             return obj;
          }
      }

      return null;
    }
**/

    public boolean memberOf( variable var ) 
    {

       dimensionSet d_set = var.getDimSet();

       if( this.dimSet.sameSetSameOrder( d_set ) )
       {
          return true;
       }
       else 
       {
          return false;
       }

    }

    public String toString() 
    {
       String str = dimSet.toString()+"  variables: \n";

       for( int ii = 0; ii < varSet.getSize(); ii++ ) 
       {
         str = str + "       "+(varSet.getElement(ii)).getName() + "\n";
       }
         str = str + "- - - - - - - - - - - - - - - - - - - \n";
       
      return str;
    }

/**

    public boolean equals( shape obj ) {

    }

   
    public boolean sameObject ( shape obj ) {
       return ( this == obj );
    }

**/

  }
