//
// Spectrum.java
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
import visad.data.netcdf.units.UnitsDB;
import visad.data.netcdf.units.DefaultUnitsDB;
import visad.data.netcdf.StandardQuantityDB;

import java.rmi.RemoteException;

/**
   Sounding is the VisAD class for atmospheric soundings.
   It extends FlatField.<p>
*/
public class Spectrum extends FlatField {

  static private UnitsDB udb;
  static private StandardQuantityDB qdb;

  static private RealType wavelength;
  static private RealType radiance;

  static private FunctionType spectrumType;

  static {
    try {
      udb = DefaultUnitsDB.instance();
      qdb = StandardQuantityDB.instance();
      wavelength = qdb.get("Wavelength");
      radiance = qdb.get("Radiance");
      spectrumType = new FunctionType(wavelength, radiance);
    }
    catch (VisADException e) {
      e.printStackTrace();
    }
  }

  //- this spectrum's display
  //
  Display display;

  Unit domain_unit;
  Unit range_unit;

  public Spectrum(float[] wavelength_domain,
                  Unit domain_unit,
                  float[] radiance_range,
                  Unit range_unit
                                            )
         throws VisADException, RemoteException
  {
    super(spectrumType, makeSet(wavelength_domain));

    int length = getLength();
    if ( radiance_range.length != length) {
      throw new VisADException("radiance_range should have same length as wavelengths");
    }
    if (radiance_range == null) {
      radiance_range = new float[length];
      for (int i=0; i<length; i++) {
        radiance_range[i] = Float.NaN;
      }
    }
    setSamples(new float[][] {radiance_range});
    display = null;
    this.domain_unit = domain_unit;
    this.range_unit = range_unit;
  }


  static private Gridded1DSet makeSet(float[] wavelength_domain)
         throws VisADException {
    if (wavelength_domain == null) {
      throw new SetException("wavelengths cannot be null");
    }
    return new Gridded1DSet(wavelength, new float[][] {wavelength_domain},
                            wavelength_domain.length,
                            null, new Unit[] {udb.get("Radiance")}, null);
  }

  public boolean addToDisplay( Display display )
  {
    if ( this.display != null ) {
      return false;
    }
    this.display = display;
    return true;
  }

  public boolean remove()
  {
    if ( this.display == null ) {
      return false;
    }
    return true;
  }

  public boolean restore()
  {
    return false;
  }

  public static void main(String args[]) {
  }
}
