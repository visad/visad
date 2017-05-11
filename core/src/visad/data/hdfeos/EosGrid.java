//
// EosGrid.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.hdfeos;

import java.util.*;
import visad.data.hdfeos.hdfeosc.HdfeosLib;

public class EosGrid extends EosStruct {

  int grid_id;
  int file_id;
  int sd_id;
  private String grid_name;

  DimensionSet  D_Set = null;
   VariableSet  DV_Set;
      ShapeSet  DV_shapeSet;
       GctpMap  gridMap;

  EosGrid ( int file_id, int sd_id, String name )
  throws HdfeosException
  {
     super();
     this.file_id = file_id;
     this.sd_id = sd_id;
     grid_name = name;
     grid_id = HdfeosLib.GDattach( file_id, name );
     struct_id = grid_id;

     if ( grid_id < 0 )
     {
       throw new HdfeosException("EosGrid cannot attatch Grid: "+name );
     }


/**-  Now make dimensionSet:  - - - - - - - - - - -  -*/

      int[] stringSize = new int[1];
      stringSize[0] = 0;

      DimensionSet D_Set = new DimensionSet();

      int n_dims = HdfeosLib.GDnentries( grid_id, HdfeosLib.N_DIMS, stringSize );

      if ( n_dims > 0 )
      {

        String[] dimensionList = {"empty"};
        int[] lengths = new int[ n_dims ];

        n_dims = HdfeosLib.GDinqdims( grid_id, stringSize[0], dimensionList, lengths );

        if ( n_dims <= 0 )
        {
           throw new HdfeosException("GDinqdims status: "+n_dims);
        }

        StringTokenizer listElements =
                new StringTokenizer( dimensionList[0], ",", false );

        int cnt = 0;

        while ( listElements.hasMoreElements() ) {

          name = (String) listElements.nextElement();
          int len = lengths[cnt];
          NamedDimension obj = new NamedDimension( grid_id, name, len, null );

          D_Set.add( obj );
          cnt++;
        }
      }

      this.D_Set = D_Set;


/**-  Done, now make VariableSets:  - - - - - - - - -*/

       int n_flds = HdfeosLib.GDnentries( grid_id, HdfeosLib.D_FIELDS, stringSize );

       if ( n_flds <= 0 )
       {
         throw new HdfeosException(" no data fields  ");
       }

       String[] D_List = {"empty"};

       int[] dumA = new int[ n_flds ];
       int[] dumB = new int[ n_flds ];

       n_flds = HdfeosLib.GDinqfields( grid_id, stringSize[0], D_List, dumA, dumB);

       if ( n_flds < 0 )
       {
          throw new HdfeosException("no data fields in grid struct: "+grid_id);
       }

       this.makeVariables( D_List[0] );


/**-  Done, now make ShapeSet for data fields: - - - - - - - - - */

        DV_shapeSet = new ShapeSet( DV_Set );

/**-  Retrieve map projection type and paramters: - - - - - - -  */

        int[] projcode = new int[1];
        int[] zonecode = new int[1];
        int[] sphrcode = new int[1];
        double[] projparm = new double[16];

        int stat = HdfeosLib.GDprojinfo( grid_id, projcode, zonecode, sphrcode, projparm );

        if ( stat < 0 )
        {
            throw new HdfeosException(" GDprojinfo, status: "+stat);
        }

            int[] xdimsize = new int[1];
            int[] ydimsize = new int[1];
         double[] uprLeft = new double[2];
         double[] lwrRight = new double[2];

         stat = HdfeosLib.GDgridinfo( grid_id, xdimsize, ydimsize, uprLeft, lwrRight );

         if ( stat < 0 )
         {
             throw new HdfeosException(" GDgridinfo, status: "+stat);
         }

         gridMap = new GctpMap( projcode[0], zonecode[0], sphrcode[0],
                                xdimsize[0], ydimsize[0], projparm, uprLeft, lwrRight );

 } /**-  end EosGrid constuctor  - - - - - - - - - - - - -*/


  public int getStructId() {
     return grid_id;
  }

  public GctpMap getMap() {
     return gridMap;
  }

  public ShapeSet getShapeSet() {
    return DV_shapeSet;
  }

  private void makeVariables( String fieldList )
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

          String[] dim_list = {"empty"};

          int stat = HdfeosLib.GDfieldinfo( grid_id, field, dim_list, rank, lengths, type );

          if ( stat < 0 )
          {
            throw new HdfeosException(" GDfieldinfo, stat < 1 for: "+field );
          }

          StringTokenizer dimListElements = new StringTokenizer( dim_list[0], ",", false );

          Vector dims = new Vector();
          DimensionSet newSet = new DimensionSet();

          cnt = 0;
          while ( dimListElements.hasMoreElements() )
          {
              String dimName = (String) dimListElements.nextElement();

              n_dim = D_Set.getByName( dimName );

              if ( n_dim == null ) {

                n_dim = new NamedDimension( grid_id, dimName, lengths[cnt], null);
                D_Set.add( n_dim );
              }

              if ( n_dim.isUnlimited() )  {
                n_dim.setLength( lengths[ cnt ] );
              }

              newSet.add( n_dim );
              cnt++;
          }
              newSet.setToFinished();


          Variable obj = new Variable(  field, newSet, rank[0], type[0], null );
          varSet.add( obj );

      }

          varSet.setToFinished();

          DV_Set = varSet;

  }
}
