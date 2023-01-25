//
// Sounding.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java3d.DirectManipulationRendererJ3D;
import visad.data.netcdf.units.UnitsDB;
import visad.data.netcdf.units.DefaultUnitsDB;
import visad.data.netcdf.StandardQuantityDB;

import java.util.Vector;

import java.rmi.RemoteException;

/**
   Sounding is the VisAD class for atmospheric soundings.
   It extends FlatField.<p>
*/
public class Sounding extends FlatField {

  static private UnitsDB udb;
  static private StandardQuantityDB qdb;

  static private RealType Pressure;
  static private RealType Temperature;
  static private RealType DewPoint;

  static private FunctionType soundingType;

  static {
    try {
      udb = DefaultUnitsDB.instance();
      qdb = StandardQuantityDB.instance();
      Pressure = qdb.get("Pressure");
      Temperature = qdb.get("Temperature");
      DewPoint = new RealType("DewPoint", udb.get("K"), null);
      RealTupleType ttd = new RealTupleType(Temperature, DewPoint);
      soundingType = new FunctionType(Pressure, ttd);
    }
    catch (VisADException e) {
      e.printStackTrace();
    }
  }

  //- this sounding's display_s
  //
  private Vector soundingDisplay_s = new Vector();

  //- this sounding's DataReference
  //
  private DataReferenceImpl sounding_ref;

  /** pressures in hPa, temperatures and dewpoints in K */
  public Sounding(float[] pressures, float[] temperatures, float[] dewpoints)
         throws VisADException, RemoteException {
    super(soundingType, makePressureSet(pressures));
    if (temperatures == null && dewpoints == null) {
      throw new VisADException("temperatures and dewpoints cannot both be null");
    }
    int length = getLength();
    if ((temperatures != null && temperatures.length != length) ||
        (dewpoints != null && dewpoints.length != length)) {
      throw new VisADException("temperatures and dewpoints cannot have " +
                               "different lengths");
    }
    if (temperatures == null) {
      temperatures = new float[length];
      for (int i=0; i<length; i++) {
        temperatures[i] = Float.NaN;
      }
    }
    if (dewpoints == null) {
      dewpoints = new float[length];
      for (int i=0; i<length; i++) {
        dewpoints[i] = Float.NaN;
      }
    }
    setSamples(new float[][] {temperatures, dewpoints});
    sounding_ref = new DataReferenceImpl("sounding reference");
    sounding_ref.setData(this);
  }

  static private Gridded1DSet makePressureSet(float[] pressures)
         throws VisADException {
    if (pressures == null) {
      throw new SetException("pressures cannot be null");
    }
    return new Gridded1DSet(Pressure, new float[][] {pressures}, pressures.length,
                            null, new Unit[] {udb.get("hPa")}, null);
  }

  public void addToDisplay( DisplayImpl display ) 
         throws VisADException, RemoteException
  {
     addToDisplay(display, null);
  }

  public void addToDisplayWithDirectManipulation( DisplayImpl display )
         throws VisADException, RemoteException
  {
     addToDisplay(display, new DirectManipulationRendererJ3D());
  }

  private void addToDisplay(DisplayImpl display, DataRenderer renderer)
          throws VisADException, RemoteException
  {
    if ( soundingDisplay_s.contains(display) ) {
      return;
    }
    else { 
      soundingDisplay_s.add(display);
      Vector mapVector = display.getMapVector();

      boolean presToYAxis = false;
      boolean tempToXAxis = false;
      boolean dewpToXAxis = false;

      for (int kk = 0; kk < mapVector.size(); kk++) { 
        ScalarMap smap = (ScalarMap) mapVector.elementAt(kk);
        ScalarType s_type = smap.getScalar();
        DisplayRealType d_type = smap.getDisplayScalar();
  
        if (Pressure.equals(smap) && d_type.equals(Display.YAxis)) {
          presToYAxis = true;
        }
        if (Temperature.equals(smap) && d_type.equals(Display.XAxis)) {
          tempToXAxis = true;
        }
        if (DewPoint.equals(smap) && d_type.equals(Display.XAxis)) {
          dewpToXAxis = true;
        }
      }
      
      if (presToYAxis && tempToXAxis && dewpToXAxis) {
        display.removeAllReferences();
        display.addReferences(renderer, sounding_ref);
      }
      else {
        display.removeAllReferences();
        display.clearMaps();

        display.addMap(new ScalarMap(Pressure, Display.YAxis));
        display.addMap(new ScalarMap(Temperature, Display.XAxis));
        display.addMap(new ScalarMap(DewPoint, Display.XAxis));
        display.addReferences(renderer, sounding_ref);
      }
      return;
    }
  }

  public void remove()
         throws VisADException, RemoteException
  {
    for ( int kk = 0; kk < soundingDisplay_s.size(); kk++ ) {
      DisplayImpl display = 
        (DisplayImpl) soundingDisplay_s.elementAt(kk); 
      display.removeReference(sounding_ref);
    }
    soundingDisplay_s.removeAllElements();
    return;
  }

  public boolean restore()
  {
    //- ?
    return false;
  }

  public static void main(String args[])
         throws VisADException, RemoteException {
    float[] p = {1000.0f, 500.0f, 100.0f};
    float[] t = {283.0f, 200.0f, 150.0f};
    float[] td = {273.0f, 180.0f, 100.0f};
    Sounding sounding = new Sounding(p, t, td);
    System.out.println("sounding = " + sounding);
  }

}
