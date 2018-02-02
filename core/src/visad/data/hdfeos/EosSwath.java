//
// EosSwath.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

public class EosSwath extends EosStruct
{
  int swath_id;
  int sd_id;
  private String swath_name;

  GeoMapSet  G_Set;
  DimensionSet  D_Set;
  VariableSet  DV_Set;
  VariableSet  GV_Set;
     ShapeSet  DV_shapeSet;
     ShapeSet  GV_shapeSet;

  EosSwath ( int file_id, int sd_id, String name )
           throws HdfeosException
  {
     super();
     swath_name = name;
     this.sd_id = sd_id;
     swath_id = HdfeosLib.SWattach( file_id, name );
     struct_id = swath_id;

     if ( swath_id < 0 )
     {
        throw new HdfeosException(" EosSwath cannot attach to swath: "+name );
     }

/**- make GeoMapSet:  - - - - - - - - - - - - - - - - -*/

     int[] stringSize = new int[1];
     stringSize[0] = 0;

     int n_maps = HdfeosLib.SWnentries( swath_id, HdfeosLib.G_MAPS, stringSize );

     if ( n_maps > 0 )
     {
        int[] offset = new int[ n_maps ];
        int[] increment = new int[ n_maps ];
        String[] map_list = {"empty"};

        n_maps = HdfeosLib.SWinqmaps( swath_id, stringSize[0], map_list, offset, increment  );

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
        G_Set = new GeoMapSet();
     }

/**-  Done, now make DimensionSet:  - - - - - - - - - - -  -*/

      int n_dims = HdfeosLib.SWnentries( swath_id, HdfeosLib.N_DIMS, stringSize );

      if ( n_dims <= 0 )
      {
        throw new HdfeosException("no dimension defined");
      }


      DimensionSet D_Set = new DimensionSet();

      String[] dimensionList = {"empty"};
      int[] lengths = new int[ n_dims ];

      n_dims = HdfeosLib.SWinqdims( swath_id, stringSize[0], dimensionList, lengths );

      if ( n_dims <= 0 )
      {
        throw new HdfeosException("no dimension defined");
      }

      StringTokenizer listElements =
              new StringTokenizer( dimensionList[0], ",", false );

      int cnt = 0;

      while ( listElements.hasMoreElements() )
      {
        name = (String) listElements.nextElement();
        int len = lengths[cnt];
        GeoMap g_map = G_Set.getGeoMap( name );
        NamedDimension obj = new NamedDimension( swath_id, name, len, g_map );

        D_Set.add( obj );
        cnt++;
      }

      this.D_Set = D_Set;


/**-  Done, now make VariableSets:  - - - - - - - - -*/

       int n_flds = HdfeosLib.SWnentries( swath_id, HdfeosLib.D_FIELDS, stringSize );

       if ( n_flds <= 0 )
       {
          throw new HdfeosException(" no Data Fields from SWnentries ");
       }

       String[] D_List = {"empty"};

         int[] dumA = new int[ n_flds ];
         int[] dumB = new int[ n_flds ];

       n_flds = HdfeosLib.SWinqdatafields( swath_id, stringSize[0], D_List, dumA, dumB);

       if ( n_flds < 0 )
       {
          throw new HdfeosException("no data fields in swath # "+swath_id);
       }

       this.makeVariables( D_List[0], D_TYPE );

       n_flds = HdfeosLib.SWnentries( swath_id, HdfeosLib.G_FIELDS, stringSize );

       String[] G_List = {"empty"};

         int[] dumC = new int[ n_flds ];
         int[] dumD = new int[ n_flds ];


       n_flds = HdfeosLib.SWinqgeofields( swath_id, stringSize[0], G_List, dumC, dumD );

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
    Calibration calibration;
    int cnt;
    int stat;
    boolean noAttr;
    boolean noAttrValue;

    StringTokenizer listElements = new StringTokenizer( fieldList, ",", false );

    VariableSet varSet = new VariableSet();

    String[] constantNames = CalibrationDefault.getNames();
    double[][] constants = new double[ constantNames.length ][];

    while ( listElements.hasMoreElements() )
    {
      noAttr = false;
      noAttrValue = false;
      String field = (String)listElements.nextElement();

      for ( int ii = 0; ii < constants.length; ii++ )
      {
         cnt = HdfeosLib.SDattrinfo( sd_id, field, constantNames[ii] );
         if ( cnt < 0 ) {
           noAttr = true;
           break;
         }
         else {
           constants[ii] = new double[ cnt ];
         }
      }
      if ( noAttr )
      {
        calibration = null;
      }
      else
      {
        for ( int ii = 0; ii < constants.length; ii++ )
        {
          stat = HdfeosLib.GetNumericAttr( sd_id, field, constantNames[ii], constants[ii] );
          if ( stat < 0 ) {
             noAttrValue = true;
             break;
          }
        }
        if ( noAttrValue ) {
          calibration = null;
        }
        else {
          calibration = new CalibrationDefault( constants );
        }
      }

      String[] dim_list = {"empty"};

      stat = HdfeosLib.SWfieldinfo( swath_id, field, dim_list, rank, lengths, type );

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

      Variable obj = new Variable(  field, newSet, rank[0], type[0], calibration );
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
