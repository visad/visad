//
// NastiInstrument.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

  public NastiInstrument( String[] scalar_names,  //- names of retrieval/foward algorith parameters
                          String[] units,         //- units of the above
                          double[] model_parms    //- initial values of the above
                                                )
         throws VisADException, RemoteException
  {
    super(scalar_names, units, model_parms);
  }

  float[][] computeRetrieval(float[] radiances, double[] model_parms)
  {


    //-nasti_retrvl_c(  );
    return null;
  }

  float[] computeFoward(float[][] rtvl, double[] model_parms)
  {


    //-nastirte_c(   );
    return null;
  }

  private native void nastirte_c( float a, float b, int c, float d,
                                  float[] p, float[] t, float[] wv, float[] o,
                                  int[] u, double[] vn, double[] tb, double[] rr );

  private native void nasti_retrvl_c( int opt, int opt2, int rec,
                                      float gamt, float gamw, float gamts, float emis,
                                      float[] tair, float[] rr, float[] pout );
}
