package visad.data.hdfeos;

import java.util.*;
import java.rmi.*;
import visad.*;

  public class fileDataSet 
  {

    Vector dataSet;
    private boolean finished = false;

    fileDataSet() 
    {
    
       dataSet = new Vector();
    }

    public void add( fileData f_data ) 
    {

       if ( ! finished ) 
       {
         dataSet.addElement( f_data );
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
      int size = dataSet.size(); 
      return size;
    }

    public fileData getElement( int ii ) 
    {
      fileData obj = (fileData)dataSet.elementAt( ii );
      return obj;
    }


    public boolean isEmpty() {

      return dataSet.isEmpty();
    }


    public Enumeration getEnum()
    {

      Enumeration e = dataSet.elements();
      return e;
    }

    public MathType getVisADMathType() throws VisADException 
    {

      fileData f_data = null;
      MathType[] M_type =  new MathType[ getSize() ];

      for ( int ii = 0; ii < getSize(); ii++ ) {
        f_data = getElement(ii);

        M_type[ii] = f_data.getVisADMathType();
      }
       
      TupleType T_type = new TupleType( M_type );

      return T_type;

    }

    public DataImpl getVisADDataObject() throws VisADException, RemoteException 
    {

       fileData f_data = null;
       DataImpl[] data = new DataImpl[ getSize() ];
       indexSet i_set = null;

       for ( int ii = 0; ii < getSize(); ii++ ) {
         f_data = getElement(ii);
         data[ii] = f_data.getAdaptedVisADDataObject( i_set );
       }
       
       TupleType T_type = (TupleType) getVisADMathType();

       Tuple tuple = new Tuple( T_type, data );

       return tuple;
    }

    public String toString() 
    {

      String str = "dataSet: \n";

      for ( int ii = 0; ii < this.getSize(); ii++ )
      {
         str = str + "  "+((this.getElement(ii)).toString())+"\n";
      }

      return str;
    }


  }
