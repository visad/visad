/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.amanda;

import java.rmi.RemoteException;

import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded3DSet;
import visad.RealTupleType;
import visad.VisADException;

public abstract class BaseTrack
{
  private static RealTupleType xyzType;
  private static FunctionType funcType;

  private static final float LENGTH_SCALE = 1000.0f;

  private float xstart;
  private float ystart;
  private float zstart;
  private float zenith;
  private float azimuth;
  private float length;
  private float energy;
  private float time;
  private float maxLength;

  BaseTrack(float xstart, float ystart, float zstart, float zenith,
            float azimuth, float length, float energy, float time)
  {
    this.xstart = xstart;
    this.ystart = ystart;
    this.zstart = zstart;
    this.zenith = zenith;
    this.azimuth = azimuth;
    this.length = length;
    this.energy = energy;
    this.time = time;
  }

  public final float getEnergy() { return energy; }
  public final float getLength() { return length; }

  static void initTypes(RealTupleType xyz, FunctionType trackFunctionType)
  {
    xyzType = xyz;
    funcType = trackFunctionType;
  }

  abstract FlatField makeData()
    throws VisADException;

  final FlatField makeData(float maxLength)
    throws VisADException
  {
    float fldLength = length;
    if (fldLength > maxLength) {
      fldLength = maxLength;
    } else if (fldLength != fldLength) {
      fldLength = -1.0f;
    }

    float fldEnergy = energy;
    if (fldEnergy != fldEnergy) {
      fldEnergy = 1.0f;
    }

    float zs = (float) Math.sin(zenith * Data.DEGREES_TO_RADIANS);
    float zc = (float) Math.cos(zenith * Data.DEGREES_TO_RADIANS);
    float as = (float) Math.sin(azimuth * Data.DEGREES_TO_RADIANS);
    float ac = (float) Math.cos(azimuth * Data.DEGREES_TO_RADIANS);
    float zinc = fldLength * zc;
    float xinc = fldLength * zs * ac;
    float yinc = fldLength * zs * as;

    float[][] locs = {{xstart - LENGTH_SCALE * xinc,
                       xstart + LENGTH_SCALE * xinc},
                      {ystart - LENGTH_SCALE * yinc,
                       ystart + LENGTH_SCALE * yinc},
                      {zstart - LENGTH_SCALE * zinc,
                       zstart + LENGTH_SCALE * zinc}};
    // construct Field for fit
    Gridded3DSet set = new Gridded3DSet(xyzType, locs, 2);
    FlatField field = new FlatField(funcType, set);
    float[][] values = {{time, time}, {fldEnergy, fldEnergy}};

    try {
      field.setSamples(values, false);
    } catch (RemoteException re) {
      re.printStackTrace();
      return null;
    }

    return field;
  }

  public String toString()
  {
    String fullName = getClass().getName();
    int pt = fullName.lastIndexOf('.');
    final int ds = fullName.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    String className = fullName.substring(pt == -1 ? 0 : pt + 1);

    return className + "[" + xstart + "," + ystart + "," + zstart +
      " LA#" + zenith + " LO#" + azimuth + "]";
  }
}
