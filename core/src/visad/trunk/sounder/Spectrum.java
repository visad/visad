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
  Display spectrumDisplay;

  //- this spectrum's DataReference
  //
  DataReferenceImpl spectrum_ref;

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
    if (radiance_range == null) {
      radiance_range = new float[length];
      for (int i=0; i<length; i++) {
        radiance_range[i] = Float.NaN;
      }
    }
    else if ( radiance_range.length != length ) {
      throw new VisADException("radiance_range should have same length as wavelenghts"); 
    }
    setSamples(new float[][] {radiance_range});
    spectrumDisplay = null;
    this.domain_unit = domain_unit;
    this.range_unit = range_unit;
    spectrum_ref = new DataReferenceImpl("Spectrum reference");
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
         throws VisADException, RemoteException
  {
    if ( spectrumDisplay != null ) {
      return false;
    }
    spectrumDisplay = display;

    spectrumDisplay.removeAllReferences();
    spectrumDisplay.clearMaps();
    spectrumDisplay.addMap(new ScalarMap(radiance, Display.YAxis));
    spectrumDisplay.addMap(new ScalarMap(wavelength, Display.XAxis));
    spectrum_ref.setData(this);
    spectrumDisplay.addReference(spectrum_ref);

    return true;
  }

  public boolean remove()
         throws VisADException, RemoteException
  {
    if ( spectrumDisplay == null ) {
      return false;
    }
    spectrumDisplay.removeReference(spectrum_ref);
    spectrumDisplay.clearMaps();
    spectrumDisplay = null;
    return true;
  }

  public boolean restore()
  {
    //- ?
    return false;
  }

  public static void main(String args[]) {
  }
}
