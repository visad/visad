package visad.data.hdfeos;

import java.util.*;

  public class variableSet 
  {

    Vector varSet;
    private boolean finished = false;

    variableSet() 
    {
    
       varSet = new Vector();
    }

    public void add( variable var ) 
    {

       if ( ! finished ) 
       {
         varSet.addElement( var );
       }
       else
       {
         /* throw Exception: finished  */
       }

    }

    public void setToFinished() 
    {

       finished = true;
    }

    public int getSize() 
    {
      int size = varSet.size(); 
      return size;
    }

    public variable getElement( int ii ) 
    {
      variable obj = (variable)varSet.elementAt( ii );
      return obj;
    }


    public variable getByName( String varName ) 
    {

      int size = this.getSize();

      for ( int ii = 0; ii < size; ii++ ) {

        variable obj = (variable) varSet.elementAt(ii);

        String name = obj.getName();

        if (  name.equals( varName ) ) {
          return obj;
        }

      }
      
      return null;
    }

    public variableSet getSubset( dimensionSet d_set )
    {

       variableSet v_set = new variableSet();

       for ( int ii = 0; ii < this.getSize(); ii++ ) {

         if( ((this.getElement(ii)).getDimSet()).sameSetSameOrder( d_set ) ) {

            v_set.add( this.getElement(ii) );
         }
       }
       

      if ( v_set.getSize() == 0 ) {
        return null;
      }
      else {
        return v_set;
      }
    }

    public boolean isEmpty() {

      return varSet.isEmpty();
    }


    public Enumeration getEnum()
    {

      Enumeration e = varSet.elements();
      return e;
    }


     public String toString() 
     {

       String str = "variableSet: \n";

       for ( int ii = 0; ii < this.getSize(); ii++ )
       {
          str = str + "  "+((this.getElement(ii)).toString())+"\n";
       }

       return str;
     }


  }
