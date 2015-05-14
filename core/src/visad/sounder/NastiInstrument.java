//
// NastiInstrument.java
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

package visad.sounder;

import visad.*;
import java.rmi.RemoteException;


/**
   NastiInstrument is the VisAD class for the NAST-I sounding instrument.<p>
*/
public class NastiInstrument extends SounderInstrument
{
  static double[][] pressures = {
    { 
      50.,60.,70.,75.,80.,85.,90.,100.,125.,150.,175.,200.,
      250.,300.,350.,400.,450.,500.,550.,600.,620.,640.,660.,680.,
      700.,720.,740.,760.,780.,800.,820.,840.,860.,880.,900.,920.,
      940.,960.,980.,1000. 
    }
  };

  static float[] wavelengths = new float[9127];
  static 
  { 
    for ( int i = 0; i < wavelengths.length; i++ ) {
      wavelengths[i] = i;
    }
  }

  static String[] scalar_names =
  {
    "gamma_t",
    "gamma_w",
    "emissivity"
  };

  static String[] default_units = new String[3];
  static double[] default_model_parms = new double[3];


  //-- use default model parameters
  //
  public NastiInstrument()
         throws VisADException, RemoteException
  {
    super(scalar_names, default_units, default_model_parms);
  }

  //-- trusted, supply model paramters in correct order/units
  //
  public NastiInstrument( double[] model_parms )
         throws VisADException, RemoteException
  {
    super(scalar_names, default_units, model_parms);
  }


  float[][] computeRetrieval(float[][] radiances, double[][] model_parms)
  {






    //-nasti_retrvl_c(  );
    return null;
  }

  float[][] computeFoward(float[][] rtvl, double[][] model_parms)
  {






    //-nastirte_c(   );
    return null;
  }

  Sounding makeSounding()
           throws VisADException, RemoteException
  {
    return new Sounding((Set.doubleToFloat(pressures))[0], null, null);
  }

  Sounding makeSounding(Sounding sounding)
  {
    try {
      return (Sounding) sounding.resample((makeSounding()).getDomainSet());
    }
    catch (VisADException e) {
      throw new VisADError(e.getMessage());
    }
    catch (RemoteException e) {
      throw new VisADError(e.getMessage());
    }
  }

  Spectrum makeSpectrum()
           throws VisADException, RemoteException
  {
    return new Spectrum(wavelengths, null, null, null);
  }

  public void setGamma_t( double gamma_t ) {
    model_parms[0][0] = gamma_t; 
  }

  public void setGamma_w( double gamma_w ) {
    model_parms[0][1] = gamma_w;
  }

  public void setEmissivity( double emiss ) {
    model_parms[0][2] = emiss;
  }

  private native void nastirte_c( float a, float b, int c, float d,
                                  float[] p, float[] t, float[] wv, float[] o,
                                  int[] u, double[] vn, double[] tb, double[] rr );

  private native void nasti_retrvl_c( int opt, int opt2, int rec,
                                      float gamt, float gamw, float gamts, float emis,
                                      float[] tair, float[] rr, float[] pout );
}
