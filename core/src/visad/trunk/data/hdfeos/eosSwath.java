package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import experiment.*;
import visad.*;
import java.rmi.*;

public class eosSwath {

  int swath_id;
  private String swath_name;
 
  geoMapSet  G_Set;
  dimensionSet  D_Set;
  variableSet  DV_Set;
  variableSet  GV_Set;
     shapeSet  DV_shapeSet;
     shapeSet  GV_shapeSet;

  static int DFACC_READ = 1;
  static int G_MAPS = 1;
  static int D_FIELDS = 4;
  static int G_FIELDS = 3;
  static int N_DIMS = 0;
  static String G_TYPE = "Geolocation Fields";
  static String D_TYPE = "Data Fields";

  eosSwath ( int file_id, String name ) {

     swath_name = name;
     swath_id = library.Lib.SWattach( file_id, name );
     System.out.println( "swath_id= "+swath_id);

     if ( swath_id < 0 ) 
     {
        System.out.println("cannot attach");
     }


/**- make geoMapSet:  - - - - - - - - - - - - - - - - -*/

     int[] stringSize = new int[1];
     stringSize[0] = 0;

     int n_maps = library.Lib.SWnentries( swath_id, G_MAPS, stringSize ); 
 
     if ( n_maps > 0 ) 
     {

        int[] offset = new int[ n_maps ]; 
        int[] increment = new int[ n_maps ];
        String[] map_list = {"empty"};

        n_maps = library.Lib.SWinqmaps( swath_id, stringSize[0], map_list, offset, increment  );

        if ( n_maps > 0 )  {
          /*  throw exception  */
        }

        G_Set = new geoMapSet();

        StringTokenizer mapElements = new StringTokenizer( map_list[0], ",", false );

        int cnt = 0;

        while ( mapElements.hasMoreElements() ) 
        {
   
           String map = (String) mapElements.nextElement();

           StringTokenizer dims = new StringTokenizer( map, "/", false );

           String[] S_array = new String[2];

           int cnt2 = 0;
           while ( dims.hasMoreElements() ) 
           {

              S_array[cnt2] = (String) dims.nextElement();
              cnt2++;
           }
           String toDim = S_array[1];
           String fromDim = S_array[0];
           int off = offset[cnt];
           int inc = increment[cnt];

           geoMap obj = new geoMap( toDim, fromDim, off, inc );
           G_Set.add( obj );

        }

     } 
     else 
     {
        System.out.println("no geo maps specified");
        G_Set = new geoMapSet();
     }
        
/**-  Done, now make dimensionSet:  - - - - - - - - - - -  -*/


      int n_dims = library.Lib.SWnentries( swath_id, N_DIMS, stringSize );
 
      if ( n_dims <= 0 ) 
      {
         System.out.println(" error: no dimensions ");
      }

         System.out.println(" n_dims: "+n_dims);

      dimensionSet D_Set = new dimensionSet();

      String[] dimensionList = {"empty"};
      int[] lengths = new int[ n_dims ];

      n_dims = library.Lib.SWinqdims( swath_id, stringSize[0], dimensionList, lengths );

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
        geoMap g_map = G_Set.getGeoMap( name );
        namedDimension obj = new namedDimension( swath_id, name, len, g_map );

        D_Set.add( obj );
        cnt++;
      }

      this.D_Set = D_Set;


/**-  Done, now make variableSets:  - - - - - - - - -*/

       int n_flds = library.Lib.SWnentries( swath_id, D_FIELDS, stringSize );

       if ( n_flds <= 0 )  {
         /* throw exception */
            System.out.println(" no Data Fields ");
       }
            System.out.println(" # of Data Fields: "+n_flds );

       String[] D_List = {"empty"};

         int[] dumA = new int[ n_flds ];
         int[] dumB = new int[ n_flds ];
            System.out.println("size= "+stringSize[0]);

       n_flds = library.Lib.SWinqdatafields( swath_id, stringSize[0], D_List, dumA, dumB);

       if ( n_flds < 0 ) {
         /* throw new VisADException("no data fields in swath # "+struct_id); */
       }

       this.makeVariables( D_List[0], D_TYPE );

       n_flds = library.Lib.SWnentries( swath_id, G_FIELDS, stringSize );

       if ( n_flds <= 0 ) {
            System.out.println(" no Geo Fields ");
       }
            System.out.println(" # of Geo Fields: "+n_flds);

       String[] G_List = {"empty"};
      
         int[] dumC = new int[ n_flds ];
         int[] dumD = new int[ n_flds ];
          
            System.out.println("size= "+stringSize[0]);

       n_flds = library.Lib.SWinqgeofields( swath_id, stringSize[0], G_List, dumC, dumD );
         if ( n_flds < 0 ) {
         /*throw new VisADException("no data fields in swath # "+struct_id); */
         }
            System.out.println(" Geo Fields: "+G_List[0] );

       this.makeVariables( G_List[0], G_TYPE );


/**-  Done, now make shapeSets for both data and geo fields: - - - - - - - - - */

      DV_shapeSet = new shapeSet( DV_Set );

      GV_shapeSet = new shapeSet( GV_Set );


 } /**-  end eosSwath constuctor  - - - - - - - - - - - - -*/



  private void makeVariables( String fieldList, String f_type )  {

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

          int n_dims = library.Lib.SWfdims( swath_id, f_type, field, stringSize ); 

          int[] rank = new int[ 1 ];
          int[] lengths = new int[ n_dims ];
          int[] type = new int[ 1 ];

          int stat = library.Lib.SWfieldinfo( swath_id, field, stringSize[0], dim_list, rank, lengths, type );

          StringTokenizer dimListElements = new StringTokenizer( dim_list[0], ",", false );

          Vector dims = new Vector();
          dimensionSet newSet = new dimensionSet();

          cnt = 0;
          while ( dimListElements.hasMoreElements() ) 
          {
              String dimName = (String) dimListElements.nextElement();
              n_dim = D_Set.getByName( dimName );

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


          if ( f_type.equals( G_TYPE ))
          {
             GV_Set = varSet;
          }
          else 
          {
             DV_Set = varSet;
          }
   
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
    metaDomainGen FF_domain = null;
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

        if ( dim.isGeoMapDefined() ) {

          G_dims.add( dim );
        }
        else if ( GV_shapeSet.isMemberOf( dim ) ) {

          G_dims.add( dim );
        }
        else {
 
          F_dims.add( dim );
        }
      }
      
      System.out.println( "< < < < < < < < < < < < < < < < < < < ");
      System.out.println( F_dims.toString() );
      System.out.println( G_dims.toString() );

//- examine geo-dimension sets for this variable group - - - - - - - - - - -

      int size = G_dims.getSize();

      if ( size > 0 ) {

        FF_domain = new metaDomainGen( swath_id );

        for ( idx = 0; idx < size; idx++ ) {

          dim = G_dims.getElement( idx );

          shape c_var = GV_shapeSet.getCoordVar( dim );

          if ( c_var != null ) {


          }
          else {

            FF_domain.addDim( dim );
          }
        }


            FF_domain.addVar( GV_Set.getByName( "Latitude" ) );
            FF_domain.addVar( GV_Set.getByName( "Longitude" ) );


      }
      else { //-  no temporal, spatial dimension associations - - - - - - - -
         // just make big data blocks here for now, throw Exception
      }

      System.out.println(  FF_domain.getVarSet().toString() );
      variableSet range_var = S_obj.getVariables();

      metaFlatField m_FField = new metaFlatField( swath_id, FF_domain, range_var );


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
               m_domain = new metaDomainGen( swath_id );
               m_domain.addDim( dim );
               m_domain.addVar( Vset.getElement( 0 ) ); 
               allm_domain.addElement( m_domain );
             }
             else {

                // not yet implemented
             }
          }
          else {   // no coordinate variable present

            m_domainS = new metaDomainSimple( swath_id );
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
