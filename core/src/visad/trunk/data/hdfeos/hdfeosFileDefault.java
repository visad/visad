package visad.data.hdfeos; 

import java.util.*;
import java.lang.*;
import experiment.*;
import visad.*;

  public class hdfeosFileDefault  {

    String filename;
    int  file_id;
    int  n_grids;
    int  n_swaths;

    Vector allSwaths;
    Vector allGrids;
    Vector allPoints;

    static int DFACC_READ = 1;
    static int HDFE_mode = 4;

    hdfeosFileDefault( String filename )  {

      this.filename = filename;

      String[] swath_list = {"empty"};
      int n_swaths = library.Lib.SWinqswath( filename, swath_list );

           System.out.println( "n_swaths: "+n_swaths);
           System.out.println( "name_list: "+swath_list[0]);

      if ( n_swaths > 0 )  {

         allSwaths = new Vector();

         file_id = library.Lib.SWopen( filename, DFACC_READ );

            System.out.println( "file_id: "+file_id);

         
         eosSwath obj = new eosSwath( file_id, swath_list[0] );

         allSwaths.addElement( obj );
      } 


      String[] grid_list = {"empty"};
      int n_grids = library.Lib.GDinqgrid( filename, grid_list );

           System.out.println( "n_grids: "+n_grids);
           System.out.println( "name_list: "+grid_list[0]);


      if ( n_grids > 0 ) {

         allGrids = new Vector();

         file_id = library.Lib.GDopen( filename, DFACC_READ );

            System.out.println( "file_id: "+file_id);

         StringTokenizer grids = new StringTokenizer( grid_list[0], ",", false );
 
         while ( grids.hasMoreElements() ) {

           String grid = (String) grids.nextElement();

           eosGrid g_obj = new eosGrid( file_id, grid );
        
           allGrids.addElement( g_obj );
         }

      }

    }

    public DataImpl getDataObject() {

      DataImpl data = null;







      return data;
    }


/*
    public  close ()  {


    }
*/
}
