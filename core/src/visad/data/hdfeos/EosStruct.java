//
// EosStruct.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import visad.data.hdfeos.hdfeosc.HdfeosLib;

public abstract class EosStruct
{
  static String G_TYPE = "Geolocation Fields";
  static String D_TYPE = "Data Fields";

  int struct_id;

  public int getStructId()
  {
    return struct_id;
  }

  public void readData( String f_name, int[] start, int[] stride, int[] edge,
                        int num_type, Calibration cal, float[] f_data )
         throws HdfeosException
  {
    int status = 0;
    int jj;
    int n_values = f_data.length;

    if ( num_type == HdfeosLib.FLOAT )
    {
      if ( this instanceof EosGrid )
      {
        status = HdfeosLib.GDreadfield( struct_id, f_name, start, stride, edge, f_data );
      }
      else if ( this instanceof EosSwath )
      {
        status = HdfeosLib.SWreadfield( struct_id, f_name, start, stride, edge, f_data );
      }
    }
    else if ( num_type == HdfeosLib.DOUBLE )
    {
      double[] d_data = new double[ n_values ];
      if( this instanceof EosGrid )
      {
        status = HdfeosLib.GDreadfield( struct_id, f_name, start, stride, edge, d_data );
      }
      else if ( this instanceof EosSwath )
      {
        status = HdfeosLib.SWreadfield( struct_id, f_name, start, stride, edge, d_data );
      }

      for ( jj = 0; jj < n_values; jj++ )
      {
        f_data[jj] = (float)d_data[jj];
      }
      d_data = null;
    }
    else if ( num_type == HdfeosLib.INT )
    {
      int[] i_data = new int[ n_values ];
      if ( this instanceof EosGrid )
      {
        status = HdfeosLib.GDreadfield( struct_id, f_name, start, stride, edge, i_data );
      }
      else if ( this instanceof EosSwath )
      {
        status = HdfeosLib.SWreadfield( struct_id, f_name, start, stride, edge, i_data );
      }

      for ( jj = 0; jj < n_values; jj++ )
      {
        f_data[jj] = (float)i_data[jj];
      }
      i_data = null;
    }
    else if (( num_type == HdfeosLib.SHORT )||
              ( num_type == HdfeosLib.U_SHORT))
    {
      short[] s_data = new short[ n_values ];
      if ( this instanceof EosGrid )
      {
        status = HdfeosLib.GDreadfield( struct_id, f_name, start, stride, edge, s_data );
      }
      else if ( this instanceof EosSwath )
      {
        status = HdfeosLib.SWreadfield( struct_id, f_name, start, stride, edge, s_data );
      }

      if ( cal != null )
      {
        cal.fromCalibration( s_data, f_data );
      }
      else
      {
        for ( jj = 0; jj < n_values; jj++ )
        {
          f_data[jj] = (float)s_data[jj];
        }
      }
      s_data = null;
    }
    else if (( num_type == HdfeosLib.BYTE )||
             ( num_type == HdfeosLib.U_BYTE))
    {
      byte[] b_data = new byte[ n_values ];
      if ( this instanceof EosGrid )
      {
        status = HdfeosLib.GDreadfield( struct_id, f_name, start, stride, edge, b_data );
      }
      else if ( this instanceof EosSwath )
      {
        status = HdfeosLib.SWreadfield( struct_id, f_name, start, stride, edge, b_data );
      }

      if ( cal != null )
      {
        cal.fromCalibration( b_data, f_data );
      }
      else
      {
        for ( jj = 0; jj < n_values; jj++ )
        {
          f_data[jj] = (float)b_data[jj];
        }
      }
      b_data = null;
    }
    else
    {
      throw new HdfeosException(" number type not implemented: "+num_type );
    }
  }
}
