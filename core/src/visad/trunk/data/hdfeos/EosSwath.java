//
// EosSwath.java
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

public class EosSwath extends EosStruct {

  int swath_id;
  private String swath_name;
 
  GeoMapSet  G_Set;
  DimensionSet  D_Set;
  VariableSet  DV_Set;
  VariableSet  GV_Set;
     ShapeSet  DV_shapeSet;
     ShapeSet  GV_shapeSet;

  static int DFACC_READ = 1;
  static int G_MAPS = 1;
  static int D_FIELDS = 4;
  static int G_FIELDS = 3;
  static int N_DIMS = 0;
  static String G_TYPE = "Geolocation Fields";
  static String D_TYPE = "Data Fields";

  EosSwath ( int file_id, String name ) 
  throws HdfeosException 
  {
     super();
     swath_name = name;
     swath_id = Library.Lib.SWattach( file_id, name );

     if ( swath_id < 0 ) 
     {
        throw new HdfeosException(" EosSwath cannot attach to swath: "+name );
     }


/**- make GeoMapSet:  - - - - - - - - - - - - - - - - -*/

     int[] stringSize = new int[1];
     stringSize[0] = 0;

     int n_maps = Library.Lib.SWnentries( swath_id, G_MAPS, stringSize ); 
 
     if ( n_maps > 0 ) 
     {

        int[] offset = new int[ n_maps ]; 
        int[] increment = new int[ n_maps ];
        String[] map_list = {"empty"};

        n_maps = Library.Lib.SWinqmaps( swath_id, stringSize[0], map_list, offset, increment  );

        if ( n_maps > 0 )  {
          /*  throw exception  */
        }

        G_Set = new GeoMapSet();

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

           GeoMap obj = new GeoMap( toDim, fromDim, off, inc );
           G_Set.add( obj );

        }

     } 
     else 
     {
        System.out.println("no geo maps specified");
        G_Set = new GeoMapSet();
     }
        
/**-  Done, now make DimensionSet:  - - - - - - - - - - -  -*/


      int n_dims = Library.Lib.SWnentries( swath_id, N_DIMS, stringSize );
 
      if ( n_dims <= 0 ) 
      {
         System.out.println(" error: no dimensions ");
      }

         System.out.println(" n_dims: "+n_dims);

      DimensionSet D_Set = new DimensionSet();

      String[] dimensionList = {"empty"};
      int[] lengths = new int[ n_dims ];

      n_dims = Library.Lib.SWinqdims( swath_id, stringSize[0], dimensionList, lengths );

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
        GeoMap g_map = G_Set.getGeoMap( name );
        NamedDimension obj = new NamedDimension( swath_id, name, len, g_map );

        D_Set.add( obj );
        cnt++;
      }

      this.D_Set = D_Set;


/**-  Done, now make VariableSets:  - - - - - - - - -*/

       int n_flds = Library.Lib.SWnentries( swath_id, D_FIELDS, stringSize );

       if ( n_flds <= 0 )  {
         /* throw exception */
            System.out.println(" no Data Fields ");
       }
            System.out.println(" # of Data Fields: "+n_flds );

       String[] D_List = {"empty"};

         int[] dumA = new int[ n_flds ];
         int[] dumB = new int[ n_flds ];
            System.out.println("size= "+stringSize[0]);

       n_flds = Library.Lib.SWinqdatafields( swath_id, stringSize[0], D_List, dumA, dumB);

       if ( n_flds < 0 ) {
         /* throw new VisADException("no data fields in swath # "+struct_id); */
       }

       this.makeVariables( D_List[0], D_TYPE );

       n_flds = Library.Lib.SWnentries( swath_id, G_FIELDS, stringSize );

       if ( n_flds <= 0 ) {
            System.out.println(" no Geo Fields ");
       }
            System.out.println(" # of Geo Fields: "+n_flds);

       String[] G_List = {"empty"};
      
         int[] dumC = new int[ n_flds ];
         int[] dumD = new int[ n_flds ];
          
            System.out.println("size= "+stringSize[0]);

       n_flds = Library.Lib.SWinqgeofields( swath_id, stringSize[0], G_List, dumC, dumD );
         if ( n_flds < 0 ) {
         /*throw new VisADException("no data fields in swath # "+struct_id); */
         }
            System.out.println(" Geo Fields: "+G_List[0] );

       this.makeVariables( G_List[0], G_TYPE );


/**-  Done, now make ShapeSets for both data and geo fields: - - - - - - - - - */

      DV_shapeSet = new ShapeSet( DV_Set );

      GV_shapeSet = new ShapeSet( GV_Set );


 } /**-  end EosSwath constuctor  - - - - - - - - - - - - -*/

  public int getStructId() {
     return swath_id;
  }

  public ShapeSet getDV_shapeSet() {
    return DV_shapeSet;
  }

  public ShapeSet getGV_shapeSet() {
    return GV_shapeSet;
  }

  private void makeVariables( String fieldList, String f_type ) 
               throws HdfeosException
  {

      int[] rank = new int[ 1 ];
      int[] type = new int[ 1 ];
      int[] lengths = new int[ 10 ];

      NamedDimension n_dim;
      int cnt;

      StringTokenizer listElements = new StringTokenizer( fieldList, ",", false );

      VariableSet varSet = new VariableSet();

      while ( listElements.hasMoreElements() ) 
      {

          String field = (String)listElements.nextElement();

             System.out.println(" field: "+field);

          String[] dim_list = {"empty"};

          int stat = Library.Lib.SWfieldinfo( swath_id, field, dim_list, rank, lengths, type );

          StringTokenizer dimListElements = new StringTokenizer( dim_list[0], ",", false );

          Vector dims = new Vector();
          DimensionSet newSet = new DimensionSet();

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

          Variable obj = new Variable(  field, newSet, rank[0], type[0] );
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

  public void readData( String field, int[] start, int[] stride,
                                      int[] edge, int type, float[] data )
    throws HdfeosException
  {
     ReadSwathGrid.readData( this, field, start, stride, edge, type, data);
  }

  public void readData( String field, int[] start, int[] stride,
                                      int[] edge, int type, double[] data )
    throws HdfeosException
  {
     ReadSwathGrid.readData( this, field, start, stride, edge, type, data);
  }


}
