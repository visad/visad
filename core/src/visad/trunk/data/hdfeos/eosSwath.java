//
// eosSwath.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
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

  public int getSwathId() {
     return swath_id;
  }

  public shapeSet getDV_shapeSet() {
    return DV_shapeSet;
  }

  public shapeSet getGV_shapeSet() {
    return GV_shapeSet;
  }

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

}
