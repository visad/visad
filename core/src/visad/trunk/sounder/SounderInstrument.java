//
// SounderInstrument.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.netcdf.units.UnitsDB;
import visad.data.netcdf.units.DefaultUnitsDB;
import visad.data.netcdf.StandardQuantityDB;

import java.rmi.RemoteException;

/**
   SoundingInstrument is the VisAD abstract class for atmospheric
   sounding instruments.<p>
*/
public abstract class SounderInstrument 
{
  static private UnitsDB udb;
  static 
  {
    try {
      udb = DefaultUnitsDB.instance();
    }
    catch (VisADException e) {
      e.printStackTrace();
    }
  }

  double[] model_parms;
  int n_parms;
  RealType[] model_parm_types;
  DataReferenceImpl[] parm_refs;
  Sounding firstGuess;
  DataReferenceImpl firstGuess_ref;

  public SounderInstrument(String[] names, String[] units, double[] parms)
         throws VisADException, RemoteException
  {
    this(names, units, parms, null);
  }

  public SounderInstrument(String[] names, String[] units, 
                           double[] parms, Sounding first_guess)
         throws VisADException, RemoteException
  {
    n_parms = parms.length;

    if ( names.length != n_parms || units.length != n_parms ) { 
      throw new VisADException("# of names, units, parms must be equal"); 
    }

    model_parm_types = new RealType[n_parms];
    model_parms = new double[n_parms];
    parm_refs = new DataReferenceImpl[n_parms];
    firstGuess = first_guess;
    firstGuess_ref = new DataReferenceImpl("sounding_first_guess");

    CellImpl update_cell = new CellImpl()
    {
      public void doAction() throws VisADException, RemoteException
      {
        for ( int ii = 0; ii < n_parms; ii++ ) {
          model_parms[ii] = ((Real)parm_refs[ii].getData()).getValue();
        }
        firstGuess = (Sounding) firstGuess_ref.getData();
      }
    };

    for ( int ii = 0; ii < n_parms; ii++ ) {
      model_parm_types[ii] = new RealType(names[ii], udb.get(units[ii]), null);
      model_parms[ii] = parms[ii];
      parm_refs[ii] = new DataReferenceImpl(names[ii]);
      parm_refs[ii].setData(new Real(model_parm_types[ii], model_parms[ii]));
    }
    firstGuess_ref.setData(firstGuess);

    for ( int ii = 0; ii < n_parms; ii++ ) {
      update_cell.addReference(parm_refs[ii]);
    }
    update_cell.addReference(firstGuess_ref);
  }

  public DataReferenceImpl[] getParamReferences()
  {
    return parm_refs;
  }

  public DataReferenceImpl getFirstGuessReference()
  {
    return firstGuess_ref;
  }
   
  public abstract Sounding retrieval(Spectrum spectrum);

  public abstract Spectrum foward(Sounding sounding);
}
