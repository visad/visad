//
// eosGrid.java
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
import java.rmi.*;

public class eosGrid {

  int grid_id;
  int file_id;
  private String grid_name;
 
  dimensionSet  D_Set = null;
   variableSet  DV_Set;
      shapeSet  DV_shapeSet;
       gctpMap  gridMap;

  static int DFACC_READ = 1;
  static int D_FIELDS = 4;
  static int N_DIMS = 0;
  static String D_TYPE = "Data Fields";

  eosGrid ( int file_id, String name ) {

     this.file_id = file_id;
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
         /* throw new VisADException("no data fields in grid # "+grid_id); */
       }

       this.makeVariables( D_List[0] );


/**-  Done, now make shapeSet for data fields: - - - - - - - - - */

        DV_shapeSet = new shapeSet( DV_Set );

/**-  Retrieve map projection type and paramters: - - - - - - -  */

        int[] projcode = new int[1];
        int[] zonecode = new int[1];
        int[] sphrcode = new int[1];
        double[] projparm = new double[16];
        
        int stat = library.Lib.GDprojinfo( grid_id, projcode, zonecode, sphrcode, projparm );  

          if ( stat < 0 ) {
             System.out.println(" problem: GDprojinfo ");
          }
          else {
             System.out.println(" projcode: "+projcode[0]);
             System.out.println(" zonecode: "+zonecode[0]);
             System.out.println(" sphrcode: "+sphrcode[0]);
 
             for ( int ii = 0; ii < 16; ii++ ) {
               System.out.println(" projparm["+ii+"]: "+projparm[ii] );
             }
          }
 
            int[] xdimsize = new int[1];
            int[] ydimsize = new int[1];
         double[] uprLeft = new double[2];
         double[] lwrRight = new double[2];

         stat = library.Lib.GDgridinfo( grid_id, xdimsize, ydimsize, uprLeft, lwrRight );

           if ( stat < 0 ) {
              System.out.println(" problem: GDgridinfo ");
           }
           else {
              System.out.println(" uprLeft: "+uprLeft[0]+"  "+uprLeft[1]);
              System.out.println(" lwrRight: "+lwrRight[0]+"  "+lwrRight[1]);
           }

         gridMap = new gctpMap( projcode[0], zonecode[0], sphrcode[0],
                                xdimsize[0], ydimsize[0], projparm, uprLeft, lwrRight ); 

 } /**-  end eosGrid constuctor  - - - - - - - - - - - - -*/


  public int getGridId() {
     return grid_id;
  }

  public gctpMap getMap() {
     return gridMap;
  }

  public shapeSet getShapeSet() {
    return DV_shapeSet;
  }

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

}
