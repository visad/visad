//
// CalibrationDefault.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

class CalibrationDefault extends Calibration
{
  private static String scaleFactorName = "scale_factor";
  private static String offsetName = "add_offset";
  private static String fillValueName = "_FillValue";
  private static String validRange = "valid_range";

  private static String[] names = new String[4];

  static
  {
    names[0] = scaleFactorName;
    names[1] = offsetName;
    names[2] = fillValueName;
    names[3] = validRange;
  }

  double scale_factor;
  double offset;
  double fillvalue;
  double v_range_low;
  double v_range_high;

  CalibrationDefault( double[][] constants )
  {
    this.scale_factor = constants[0][0];
    this.offset = constants[1][0];
    this.fillvalue = constants[2][0];
    if ( constants[3][0] < constants[3][1] )
    {
      this.v_range_low = constants[3][0];
      this.v_range_high = constants[3][1];
    }
    else
    {
      this.v_range_low = constants[3][1];
      this.v_range_high = constants[3][0];
    }
  }

  public static String[] getNames()
  {
    return names;
  }

  public void fromCalibration( short[] values, double[] out )
  {
    double d_value;

    for ( int ii = 0; ii < values.length; ii++ )
    {
      d_value = (double) values[ii];

      if ( d_value == fillvalue )
      {
        d_value = Double.NaN;
      }
      else if ( (d_value < v_range_low)&&( d_value > v_range_high) )
      {
        d_value = Double.NaN;
      }
      else
      {
        d_value = (values[ii] - offset)*scale_factor;
      }

      out[ii] = d_value;
    }
  }

  public void fromCalibration( short[] values, float[] out )
  {
    float f_value;
    float offset = (float)this.offset;
    float scale_factor = (float)this.scale_factor;
    float v_range_low = (float)this.v_range_low;
    float v_range_high = (float)this.v_range_high;

    for ( int ii = 0; ii < values.length; ii++ )
    {
      f_value = (float) values[ii];

      if ( f_value == ((float) fillvalue) )
      {
        f_value = Float.NaN;
      }
      else if ( (f_value < v_range_low)&&(f_value > v_range_high) )
      {
        f_value = Float.NaN;
      }
      else
      {
        f_value = (values[ii] - offset)*(scale_factor);
      }

      out[ii] = f_value;
    }
  }
  public void fromCalibration( byte[] values, float[] out )
  {
    float f_value;
    float offset = (float)this.offset;
    float scale_factor = (float)this.scale_factor;
    float v_range_low = (float)this.v_range_low;
    float v_range_high = (float)this.v_range_high;

    for ( int ii = 0; ii < values.length; ii++ )
    {
      f_value = (float) values[ii];

      if ( f_value == ((float) fillvalue) )
      {
        f_value = Float.NaN;
      }
      else if ( (f_value < v_range_low)&&(f_value > v_range_high) )
      {
        f_value = Float.NaN;
      }
      else
      {
        f_value = (values[ii] - offset)*(scale_factor);
      }

      out[ii] = f_value;
    }
  }
}
