package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import experiment.*;
import visad.*;

  class metaDomainSimple {

    private int struct_id;

    final static int FLOAT = 5;
    final static int DOUBLE = 6;
    final static int INT = 24;

    private MathType M_type = null;

  /*
    The dimensions of the sample domain
    - - - - - - - - - - - - - - - - - -  */
    private dimensionSet dimSet;  

    public metaDomainSimple( int struct_id ) {

      this.struct_id = struct_id;
      dimSet = new dimensionSet();
    }

    public dimensionSet getDimSet() {
  
      return dimSet;
    }

    public void addDim( namedDimension dim ) {
 
       dimSet.add( dim );
    }

    Set getVisADSet( indexSet i_set ) throws VisADException {

      Set VisADset = null;
      MathType M_type = null;
      variable var;
      String R_name;
      dimensionSet varDimSet;
      namedDimension v_dim;
      namedDimension dim;
      int subDims;
      int len;
      int idx;
      int v_idx;
      int jj;
      int ii;
      int op;
      int n_dims;
       
      if ( this.M_type != null ) {
     
        M_type = this.M_type;
      }
      else {

        M_type = getVisADMathType();
        this.M_type = M_type;
      }

      int rank = dimSet.getSize();
      int[] lengths = new int[ rank ];   

      for( ii = 0; ii < rank; ii++ ) 
      {
        lengths[ii] = dimSet.getElement(ii).getLength();
      }

      VisADset = (Set) new IntegerSet( M_type, lengths );

      return VisADset;
    }

    public MathType getVisADMathType() throws VisADException {

      MathType M_type = null;

      if ( this.M_type != null ) 
      { 
        return this.M_type;
      }
      else 
      {
        int rank = dimSet.getSize();
        RealType[] R_types = new RealType[ rank ];

        for ( int ii = 0; ii < rank; ii++ ) 
        {
          String name = dimSet.getElement(ii).getName();
          R_types[ii] = new RealType( name, null, null );
        }
     
        M_type = (MathType) new RealTupleType( R_types );
        this.M_type = M_type;

      }
        return M_type;
    }

 }
