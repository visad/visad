package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import experiment.*;
import visad.*;

public class eos
{

   final static int FLOAT = 5;
   final static int DOUBLE = 6;
   final static int INT = 24;
   final static int SHORT = 22;


   public static int SWreadfield( int swath_id,
                                  String f_name,
                                    int[] start,
                                   int[] stride,
                                     int[] edge,
                                   int num_type,
                                 float[] f_data )
   {

      int status = -9;
      int jj;
      int n_values = f_data.length;
         

      if ( num_type == FLOAT ) 
      {
         status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, f_data );
      }
      else if ( num_type == DOUBLE ) 
      {
         double[] d_data = new double[ n_values ];
         status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, d_data );
         for ( jj = 0; jj < n_values; jj++ ) {
           f_data[jj] = (float)d_data[jj];
         }
           d_data = null;
      }
      else if ( num_type == INT ) 
      {
        int[] i_data = new int[ n_values ];
        status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, i_data );
        for ( jj = 0; jj < n_values; jj++ ) {
          f_data[jj] = (float)i_data[jj];
        }
          i_data = null;
      }
      else if ( num_type == SHORT )
      {
        short[] s_data = new short[ n_values ];
        status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, s_data );
        for ( jj = 0; jj < n_values; jj++ ) {
          f_data[jj] = (float)s_data[jj];
        }
          s_data = null;
      }

     return status;
   }

   public static int SWreadfield( int swath_id,
                                  String f_name,
                                    int[] start,
                                   int[] stride,
                                     int[] edge,
                                   int num_type,
                                double[] d_data )
   {

     int jj;
     int status = -9;
     int n_values = d_data.length;

         if ( num_type == FLOAT ) 
         {
             float[] f_data = new float[ n_values ];
             status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, f_data );
             for ( jj = 0; jj < n_values; jj++ ) {
               d_data[jj] = (double) f_data[jj];
             }
             f_data = null;
          }
          else if ( num_type == DOUBLE ) 
          {
             status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, d_data );
          }
          else if ( num_type == INT ) 
          {
             int[] i_data = new int[ n_values ];
             status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, i_data );

             for ( jj = 0; jj < n_values; jj++ ) {
               d_data[jj] = (double)i_data[jj];
             }
             i_data = null;
          }
          else if ( num_type == SHORT )
          {
             short[] s_data = new short[ n_values ];
             status = library.Lib.SWreadfield( swath_id, f_name, start, stride, edge, s_data );
             for ( jj = 0; jj < n_values; jj++ ) {
               d_data[jj] = (double)s_data[jj];
             }
             s_data = null;
          }


     return status;
   }
}
