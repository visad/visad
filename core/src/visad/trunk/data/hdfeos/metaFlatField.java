package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import java.rmi.*;
import experiment.*;
import visad.*;

class metaFlatField extends fileData  {

   int struct_id;
   int n_fields;
   metaDomain domainSet;
   variableSet range_vars;
   Vector m_simple = new Vector();

   MathType M_type = null;

  public metaFlatField( int struct_id, metaDomain m_dom, variableSet range_vars )  
  {

    super();

    this.struct_id = struct_id;
    this.domainSet = m_dom;
    this.range_vars = range_vars;

    n_fields = range_vars.getSize();
  
    for ( int ii = 0; ii < n_fields; ii++ ) {

       metaFlatFieldSimple obj = new metaFlatFieldSimple( struct_id, m_dom, 
                                 range_vars.getElement(ii) );

       this.m_simple.addElement(obj);
    }


  }

  public int getSize() {
 
    return this.m_simple.size();
  }

  public metaFlatFieldSimple getElement( int ii ) {

     return (metaFlatFieldSimple) this.m_simple.elementAt(ii);
  }

  public DataImpl getVisADDataObject( indexSet i_set ) throws VisADException, RemoteException
  {
    int ii;

    Set D_set = this.domainSet.getVisADSet( i_set );
   
    TupleType T_type = (TupleType) getVisADMathType();

    FlatField[] F_field = new FlatField[ n_fields ];
 
    for ( ii = 0 ; ii < n_fields; ii++ ) {

      F_field[ii] = (FlatField) (this.getElement(ii)).getVisADDataObject( i_set );
    }

      Tuple T_fields = new Tuple( T_type, F_field );

      return (DataImpl)T_fields;
  }

  public MathType getVisADMathType() throws VisADException 
  {

    MathType M_type = null;

    if ( this.M_type != null ) 
    {
      return this.M_type;
    }
    else
    {
  
      MathType D_type = domainSet.getVisADMathType();

      MathType[] F_type = new MathType[ n_fields ];

      for ( int ii = 0; ii < n_fields; ii++ ) {

        F_type[ii]  = (MathType) (this.getElement(ii)).getVisADMathType();
      }

      TupleType T_type = new TupleType( F_type );
      this.M_type = (MathType) T_type;
      return  (MathType)T_type;

    }
  }

}
