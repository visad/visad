package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import experiment.*;
import visad.*;
import java.rmi.*;

public class eosGrid {

  int grid_id;
  private String grid_name;
 
  dimensionSet  D_Set = null;
  variableSet  DV_Set;
     shapeSet  DV_shapeSet;

  static int DFACC_READ = 1;
  static int D_FIELDS = 4;
  static int N_DIMS = 0;
  static String D_TYPE = "Data Fields";

  eosGrid ( int file_id, String name ) {

     grid_name = name;
     grid_id = library.Lib.GDattach( file_id, name );
     System.out.println( "grid_id= "+grid_id);
     System.out.println( "grid_name= "+name);

     if ( grid_id < 0 ) 
     {
        System.out.println("cannot attach");
     }


/**-  Now make dimensionSet:  - - - - - - - - - - -  -*/

      int[] stringSize = new int[1];
      stringSize[0] = 0;

      dimensionSet D_Set = new dimensionSet();

      int n_dims = library.Lib.GDnentries( grid_id, N_DIMS, stringSize );
 
      if ( n_dims > 0 ) 
      {

        String[] dimensionList = {"empty"};
        int[] lengths = new int[ n_dims ];

        n_dims = library.Lib.GDinqdims( grid_id, stringSize[0], dimensionList, lengths );

        if ( n_dims <= 0 ) 
        {
           System.out.println(" error: no dimensions ");
        }

        StringTokenizer listElements = 
                new StringTokenizer( dimensionList[0], ",", false );

        int cnt = 0;

        while ( listElements.hasMoreElements() ) {

          name = (String) listElements.nextElement();
          int len = lengths[cnt];
          namedDimension obj = new namedDimension( grid_id, name, len, null );

          D_Set.add( obj );
          cnt++;
        }
      }

      this.D_Set = D_Set;


/**-  Done, now make variableSets:  - - - - - - - - -*/

       int n_flds = library.Lib.GDnentries( grid_id, D_FIELDS, stringSize );

       if ( n_flds <= 0 )  {
         /* throw exception */
            System.out.println(" no Data Fields ");
       }
            System.out.println(" # of Data Fields: "+n_flds );

       String[] D_List = {"empty"};

         int[] dumA = new int[ n_flds ];
         int[] dumB = new int[ n_flds ];
            System.out.println("size= "+stringSize[0]);

       n_flds = library.Lib.GDinqfields( grid_id, stringSize[0], D_List, dumA, dumB);

       if ( n_flds < 0 ) {
         /* throw new VisADException("no data fields in grid # "+struct_id); */
       }

       this.makeVariables( D_List[0] );


/**-  Done, now make shapeSet for data fields: - - - - - - - - - */

      DV_shapeSet = new shapeSet( DV_Set );


 } /**-  end eosGrid constuctor  - - - - - - - - - - - - -*/



  private void makeVariables( String fieldList )  {

      namedDimension n_dim;
      int cnt;

      StringTokenizer listElements = new StringTokenizer( fieldList, ",", false );

      variableSet varSet = new variableSet();

      while ( listElements.hasMoreElements() ) 
      {

          String field = (String)listElements.nextElement();

             System.out.println(" field: "+field);

          String[] dim_list = {"empty"};

          int[] stringSize = new int[1];

          int n_dims = library.Lib.GDfdims( grid_id, field, stringSize ); 

            if ( n_dims <= 0 ) {
               System.out.println(" no dimensions for variable:"+field );
            }

          int[] rank = new int[ 1 ];
          int[] lengths = new int[ n_dims ];
          int[] type = new int[ 1 ];

          int stat = library.Lib.GDfieldinfo( grid_id, field, stringSize[0], dim_list, rank, lengths, type );
 
            if ( stat < 0 ) {
              System.out.println(" GDfieldinfo, stat < 1 for: "+field );
            }

          StringTokenizer dimListElements = new StringTokenizer( dim_list[0], ",", false );

          Vector dims = new Vector();
          dimensionSet newSet = new dimensionSet();

          cnt = 0;
          while ( dimListElements.hasMoreElements() ) 
          {
              String dimName = (String) dimListElements.nextElement();

              n_dim = D_Set.getByName( dimName );

              if ( n_dim == null ) {
       
                n_dim = new namedDimension( grid_id, dimName, lengths[cnt], null);
                D_Set.add( n_dim );
              }

              if ( n_dim.isUnlimited() )  {
                n_dim.setLength( lengths[ cnt ] );
              }

              newSet.add( n_dim );
              cnt++;
          }
              newSet.setToFinished();

          variable obj = new variable(  field, newSet, rank[0], type[0] );
          varSet.add( obj );

      }

          varSet.setToFinished();

          DV_Set = varSet;
   
  }

  public MathType getVisADMathType() throws VisADException {

     return getFileDataSet().getVisADMathType();
  }

  public DataImpl getVisADDataObject() throws VisADException, RemoteException {

     return getFileDataSet().getVisADDataObject();
  }


   fileDataSet getFileDataSet() {

    shape S_obj;
    dimensionSet D_set;
    dimensionSet F_dims;
    dimensionSet G_dims;
    namedDimension dim;
    metaDomainGen m_domain;
    metaDomainSimple m_domainS = null;
    metaDomainMap FF_domain = null;
    metaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int idx;

    fileDataSet file_data = new fileDataSet();

             // FunctionLink topNode = FunctionLink.getLink( null, null );


    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) {


      S_obj = (shape)e_out.nextElement();     // this particular data variable group

      D_set = S_obj.getShape();     // dimension set of this variable group

      D_size = D_set.getSize();     // # of dimensions in the set


      F_dims = new dimensionSet();
      G_dims = new dimensionSet();
 
      for ( idx = 0; idx < D_size; idx++ ) {   // separate dimensions first

        dim = D_set.getElement( idx );

        if( ((dim.getName()).equals("XDim")) || ((dim.getName()).equals("YDim")) ) {

          G_dims.add( dim );
        }
        else {
 
          F_dims.add( dim );
        }
      }

      if ( G_dims.getSize() != 2 ) {
 
         /* throw exception */
      }
      else {

        FF_domain = new metaDomainMap( grid_id );
        FF_domain.addDim( G_dims.getElement(0) );
        FF_domain.addDim( G_dims.getElement(1) );
      }
      
      System.out.println( "< < < < < < < < < < < < < < < < < < < ");
      System.out.println( F_dims.toString() );
      System.out.println( G_dims.toString() );

      variableSet range_var = S_obj.getVariables();
      metaFlatField m_FField = new metaFlatField( grid_id, FF_domain, range_var );

      if ( F_dims.getSize() == 0 ) {

         file_data.add( (fileData) m_FField );
      }
      else {

      for (  idx = 0; idx < F_dims.getSize(); idx++ ) {  // examine non-geo dimensions

        dim = F_dims.getElement( idx );

        shape c_var = DV_shapeSet.getCoordVar( dim );
 
        if ( c_var != null ) {

           int n_vars = c_var.getNumberOfVars();

           if ( n_vars == 1 ) {   // coordinate variable, factorable dimension 
  
             variableSet Vset = c_var.getVariables();
             m_domain = new metaDomainGen( grid_id );
             m_domain.addDim( dim );
             m_domain.addVar( Vset.getElement( 0 ) ); 
             allm_domain.addElement( m_domain );
           }
           else {

              // not yet implemented
           }
        }
        else {   // no coordinate variable present

          m_domainS = new metaDomainSimple( grid_id );
          m_domainS.addDim( dim );
         // allm_domain.addElement( m_domain );
        }

      }

 
//- make metaFunction objects - - - - - - - - - -
    
      Vector t_vector = new Vector();
      t_vector.addElement( m_domainS );

      Enumeration enum = t_vector.elements();

      S_link = metaField.getLink( enum, m_FField );

      file_data.add( (fileData) S_link );

           /**
               topNode = FunctionLink.mergeLink( topNode, S_link );
 
            **/
      }


    } // end outer for loop

    return file_data;

  }
}
