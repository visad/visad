//
// metaDomainGen.java
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
import visad.Set;
import visad.MathType;
import visad.RealType;
import visad.GriddedSet;
import visad.Gridded1DSet;
import visad.TypeException;
import visad.VisADException;
import visad.RealTupleType;

  class metaDomainGen extends metaDomain {

    private int struct_id;

    final static int HYBRID = 0;         // product of next two
    final static int FACTORED = 1;       // aligned with R^N
    final static int UNFACTORED = 2;     // non-aligned with R^N
    final static int SINGLE = 3;         // just one dim/var
    final static int UNDEF = 4;

  /*
    The dimensions of the sample domain
    - - - - - - - - - - - - - - - - - -  */
    private dimensionSet dimSet;  

  /*
    The variables which contain the sample values
    - - - - - - - - - - - - - - - - - - - - - - - -  */
    private variableSet varSet;

    private MathType M_type = null;

    public metaDomainGen( int struct_id ) {

      super();

      this.struct_id = struct_id;
      dimSet = new dimensionSet();
      varSet = new variableSet();
    }

    public dimensionSet getDimSet() {
  
      return dimSet;
    }
 
    public variableSet getVarSet() {
     return varSet;
    }

    public void addDim( namedDimension dim ) {
 
       dimSet.add( dim );
    }

    public void addVar( variable var ) {
 
       varSet.add( var );
    }

    public Set getVisADSet( indexSet i_set ) throws VisADException {

      Set VisADset = null;
      variable var;
      String R_name;
      dimensionSet varDimSet;
      namedDimension v_dim;
      namedDimension dim;
      int n_domDims;
      int n_domVars;
      int cnt;
      int n_samples;
      int subDims;
      int len;
      int idx;
      int v_idx;
      int jj;
      int ii;
      int op;
      int n_dims;
      int num_type;
      float[] f_data;
      double[] d_data;
      int[] i_data;
      MathType M_type = null;
       

      if ( this.M_type == null ) {

        this.M_type = getVisADMathType();
        M_type = this.M_type;
      }
      else {

        M_type = this.M_type;
      }

/**- determine case type: - - - - - - - - - - - - - - */

        n_domDims = dimSet.getSize();
        n_domVars = varSet.getSize();

        boolean all_1D = true;
        boolean one_1D = false;
  
        if ( n_domVars == 1 ) {
     
          op = SINGLE;
        }
        else {

          for ( ii = 0; ii < n_domVars; ii++ ) {

            n_dims = (varSet.getElement(ii)).getRank();

            if ( n_dims <= 0 ) {
            }
            if ( n_dims != 1 ) {
              all_1D = false;
            }
            else {
              one_1D = true;
            }
          }
         
          if ( all_1D ) {
            op = FACTORED;
          }
          else if ( !all_1D && one_1D ) {
            op = HYBRID; 
          }
          else if ( !all_1D && !one_1D ) {
            op = UNFACTORED;
          }
          else {
            op = UNDEF;
          }

        }
/**-  -  -  -  -  -  -  -  -  -  -  - */

        int[] lengths = new int[ n_domDims ];

        n_samples = 1;
          for ( ii = 0; ii < n_domDims; ii++ ) {
            len = (dimSet.getElement(ii)).getLength();
            lengths[ii] = len;
            n_samples = n_samples*len;
          }

        RealType[] R_types = new RealType[ n_domVars ];

        switch (op) {

        case SINGLE:

          R_name = (varSet.getElement(0)).getName();
          num_type = varSet.getElement(0).getNumberType();
          int[] start1 = new int[1];
          int[] edge1 = new int[1];
          int[] stride1 = new int[1];

          start1[0] = 0;
          stride1[0] = 1;
          edge1[0] = lengths[0];
 
          float[][] samples1D = new float[ n_domVars ][ n_samples ];


          eos.SWreadfield( struct_id, R_name, start1, stride1, edge1, num_type, samples1D[0] );

          R_types[0] = new RealType( R_name, null, null );
          VisADset = new Gridded1DSet( R_types[0], samples1D, lengths[0] );

        case UNFACTORED:


          float[][] samples = new float[ n_domVars ][ n_samples ];
 
          if ( n_domVars <= n_domDims ) {  // manifold dimension <= domain dimension

            for ( v_idx = 0; v_idx < n_domVars; v_idx++ ) {  // loop through variables

              var = varSet.getElement(v_idx);
              num_type = var.getNumberType();
              n_dims = var.getRank();
              R_name = var.getName();

              varDimSet = var.getDimSet();

              int[] start = new int[ n_dims ];
              int[] edge = new int[ n_dims ];
              int[] stride = new int[ n_dims ];

              cnt = 1;

              for ( int d_idx = 0; d_idx < n_dims; d_idx++ ) {  
             
                v_dim = varDimSet.getElement(d_idx);

                if ( dimSet.isMemberOf( v_dim ) ) {
 
                  start[ d_idx ] = 0;
                  edge[ d_idx ] = v_dim.getLength();
                  stride[ d_idx ] = 1;
                }
                else if ( i_set.isMemberOf( v_dim ) ) {

                  start[ d_idx ] = i_set.getIndex(v_dim);
                  edge[ d_idx ] = 1;
                  stride[ d_idx ] = 1;
                }
                else {
  
                   /* throw unrecognized dimension exception */
                }

                cnt = cnt*edge[ d_idx ];
              }



             eos.SWreadfield( struct_id, R_name, start, stride, edge, num_type, samples[v_idx] );

             start = null;
             stride = null;
             edge = null;

           }  // variable set loop 

           VisADset = new GriddedSet( M_type, samples, lengths ); 
         }
         else {     // manifold dimension > domain dimension 

            /* throw exception, variable dimension problems  */

         }

         case FACTORED:

/**- make GriddedSet, ignore space waste for now - - - - - - - - - */




         case HYBRID:

         case UNDEF:


      }  // end switch


       return VisADset;
    }

    public MathType getVisADMathType() throws VisADException {

      MathType M_type = null;
      RealType R_type = null;
      String name = null;

      if ( this.M_type != null ) 
      { 
        return this.M_type;
      }
      else 
      {
         int n_vars = varSet.getSize();
         RealType[] R_types = new RealType[ n_vars ];

         for ( int ii = 0; ii < n_vars; ii++ ) {

           variable var = varSet.getElement(ii);
           name = var.getName();

           try
           {
             R_type = new RealType( name, null, null );
           }
           catch ( VisADException e )
           {
             if ( e instanceof TypeException )
             {
               R_type = RealType.getRealTypeByName( name );
             }
             else
             {
               throw e;
             }
           }

           R_types[ii] = R_type;
         }

         M_type = (MathType) new RealTupleType( R_types);
     
 
         double[] d_vals = new double[ R_types.length ];

         for ( int ii = 0; ii < R_types.length; ii++ ) {
            d_vals[ii] = 0;
         }

         Gridded1DSet defaultSet = new Gridded1DSet(M_type, null, 1);
 
         ((RealTupleType)M_type).setDefaultSet( (Set)defaultSet );
 
         this.M_type = M_type;

         return M_type;
      }
    }

  }
