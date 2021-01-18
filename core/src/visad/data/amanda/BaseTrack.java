/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.Gridded3DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.SetType;
import visad.VisADException;

import visad.util.Util;

public abstract class BaseTrack
  implements Comparable
{
  public static final RealType indexType =
    RealType.getRealType("Track_Index");

  private static final RealType energyType =
    RealType.getRealType("Track_Energy");

  public static FunctionType functionType;
  public static FunctionType timeSequenceType;
  public static FieldImpl missing;

  private static FunctionType indexTupleType;

  static {
    try {
      functionType =
        new FunctionType(AmandaFile.xyzType,
                         new RealTupleType(RealType.Time, energyType));

      timeSequenceType = new FunctionType(RealType.Time,
                                          new SetType(AmandaFile.xyzType));

      Gridded1DSet set = new Gridded1DSet(RealType.Time, new float[1][1], 1);
      missing = new FieldImpl(timeSequenceType, set);
    } catch (VisADException ve) {
      ve.printStackTrace();
      functionType = null;
      indexTupleType = null;
      timeSequenceType = null;
      missing = null;
    }
  }

  private static final float LENGTH_SCALE = 1000.0f;

  private static final int X_SAMPLE = 0;
  private static final int Y_SAMPLE = 1;
  private static final int Z_SAMPLE = 2;

  private float xstart;
  private float ystart;
  private float zstart;
  private float zenith;
  private float azimuth;
  private float length;
  private float energy;
  private float time;
  private float maxLength;

  private float[] timeSteps;
  private float[][] samples;

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

    timeSteps = null;
    samples = null;
  }

  private static final int compareFloat(float f0, float f1)
  {
    if (Util.isApproximatelyEqual(f0, f1)) {
      return 0;
    }

    return (f0 < f1 ? -1 : 1);
  }

  public int compareTo(Object obj)
  {
    if (obj instanceof BaseTrack) {
      return compareTo((BaseTrack )obj);
    }

    return getClass().getName().compareTo(obj.getClass().getName());
  }

  public int compareTo(BaseTrack t)
  {
    int cmp = compareFloat(time, t.time);
    if (cmp == 0) {
      cmp = compareFloat(xstart, t.xstart);
      if (cmp == 0) {
        cmp = compareFloat(ystart, t.ystart);
        if (cmp == 0) {
          cmp = compareFloat(zstart, t.zstart);
          if (cmp == 0) {
            cmp = compareFloat(zenith, t.zenith);
            if (cmp == 0) {
              cmp = compareFloat(azimuth, t.azimuth);
              if (cmp == 0) {
                // negate this since we want to prefer higher-energy tracks
                cmp = -compareFloat(energy, t.energy);
                if (cmp == 0) {
                  // negate this since we want to prefer longer tracks
                  cmp = -compareFloat(length, t.length);
                }
              }
            }
          }
        }
      }
    }

    return cmp;
  }

  final void computeSamples(float[] timeSteps)
  {
    final double degrees2radians = Data.DEGREES_TO_RADIANS;

    final double sinZenith = Math.sin(zenith * degrees2radians);
    final double cosZenith = Math.cos(zenith * degrees2radians);

    final double sinAzimuth = Math.sin(azimuth * degrees2radians);
    final double cosAzimuth = Math.cos(azimuth * degrees2radians);

    // speed of light (.3 m/nanosecond)
    final double SPEED_OF_LIGHT = 0.3 /* * 1000.0 */;

    final float timeOrigin, xOrigin, yOrigin, zOrigin;
    if (timeSteps.length == 0) {
      timeOrigin = time;
      xOrigin = xstart;
      yOrigin = ystart;
      zOrigin = zstart;
      samples = null;
    } else {
      timeOrigin = timeSteps[0];

      final double length = (timeOrigin - time) * SPEED_OF_LIGHT;

      xOrigin = xstart + (float )(length * sinZenith * cosAzimuth);
      yOrigin = ystart + (float )(length * sinZenith * sinAzimuth);
      zOrigin = zstart + (float )(length * cosZenith);

      samples = new float[timeSteps.length + 1][3];

      samples[0][X_SAMPLE] = xOrigin;
      samples[0][Y_SAMPLE] = yOrigin;
      samples[0][Z_SAMPLE] = zOrigin;
    }

    for (int i = 0; i < timeSteps.length; i++) {

      final double length = (timeSteps[i] - timeOrigin) * SPEED_OF_LIGHT;

      float xDelta = (float )(length * sinZenith * cosAzimuth);
      float yDelta = (float )(length * sinZenith * sinAzimuth);
      float zDelta = (float )(length * cosZenith);

      samples[i][X_SAMPLE] = xOrigin + xDelta;
      samples[i][Y_SAMPLE] = yOrigin + yDelta;
      samples[i][Z_SAMPLE] = zOrigin + zDelta;
    }

    this.timeSteps = timeSteps;
  }

  public boolean equals(Object obj) { return compareTo(obj) == 0; }

  public final float getEnergy() { return energy; }
  public final float getLength() { return length; }

  private final float getMaxSample(int sample, float dfltValue)
  {
    if (samples == null) {
      System.err.println("BaseTrack.getMaxSample() called before " +
                         "BaseTrack.computeSamples()");
      Thread.dumpStack();
      return dfltValue;
    }

    float max = samples[0][sample];
    for (int i = 1; i < samples.length; i++) {
      if (samples[i][sample] > max) max = samples[i][sample];
    }

    return max;
  }

  private final float getMinSample(int sample, float dfltValue)
  {
    if (samples == null) {
      System.err.println("BaseTrack.getMinSample() called before " +
                         "BaseTrack.computeSamples()");
      Thread.dumpStack();
      return dfltValue;
    }

    float min = samples[0][sample];
    for (int i = 1; i < samples.length; i++) {
      if (samples[i][sample] < min) min = samples[i][sample];
    }

    return min;
  }

  public final float getXMax() { return getMaxSample(X_SAMPLE, xstart); }
  public final float getXMin() { return getMinSample(X_SAMPLE, xstart); }
  public final float getYMax() { return getMaxSample(Y_SAMPLE, xstart); }
  public final float getYMin() { return getMinSample(Y_SAMPLE, ystart); }
  public final float getZMax() { return getMaxSample(Z_SAMPLE, xstart); }
  public final float getZMin() { return getMinSample(Z_SAMPLE, zstart); }

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
    Gridded3DSet set = new Gridded3DSet(AmandaFile.xyzType, locs, 2);
    FlatField field = new FlatField(functionType, set);
    float[][] values = {{time, time}, {fldEnergy, fldEnergy}};

    try {
      field.setSamples(values, false);
    } catch (RemoteException re) {
      re.printStackTrace();
      return null;
    }

    return field;
  }

  final FieldImpl makeTimeSequence(float[] timeSteps)
  {
    if (timeSteps == null || timeSteps.length == 0) {
      return null;
    }

    final double degrees2radians = Data.DEGREES_TO_RADIANS;

    // zenith value is the direction from which
    // the muon came, not the direction in which it's going,
    // so it's 180 degrees off
    final double z2 = ((double )zenith + 180.0) % 360.0;

    final double a2 = (double )azimuth;

    final double sinZenith = Math.sin(z2 * degrees2radians);
    final double cosZenith = Math.cos(z2 * degrees2radians);

    final double sinAzimuth = Math.sin(a2 * degrees2radians);
    final double cosAzimuth = Math.cos(a2 * degrees2radians);

    // speed of light (.3 m/nanosecond)
    final double SPEED_OF_LIGHT = 0.3;

    Gridded3DSet[] sets = new Gridded3DSet[timeSteps.length];
    Gridded3DSet missingSet = null;

    final float timeOrigin = timeSteps[0];
    final float timeFinal = timeSteps[timeSteps.length - 1];

    final double baseTime = (timeOrigin - time);
    final double baseLength = baseTime * SPEED_OF_LIGHT;

    final float xOrigin, yOrigin, zOrigin;
    xOrigin = xstart + (float )(baseLength * sinZenith * cosAzimuth);
    yOrigin = ystart + (float )(baseLength * sinZenith * sinAzimuth);
    zOrigin = zstart + (float )(baseLength * cosZenith);

    final double preTime = baseTime - ((timeFinal - timeOrigin) / 2.0);
    final double preLength = preTime * SPEED_OF_LIGHT;

    final float xPreOrigin, yPreOrigin, zPreOrigin;
    xPreOrigin = xstart + (float )(preLength * sinZenith * cosAzimuth);
    yPreOrigin = ystart + (float )(preLength * sinZenith * sinAzimuth);
    zPreOrigin = zstart + (float )(preLength * cosZenith);

    for (int i = 0; i < timeSteps.length; i++) {

      final double length = (timeSteps[i] - timeOrigin) * SPEED_OF_LIGHT;

      final float xEndpoint, yEndpoint, zEndpoint;
      xEndpoint = xOrigin + (float )(length * sinZenith * cosAzimuth);
      yEndpoint = yOrigin + (float )(length * sinZenith * sinAzimuth);
      zEndpoint = zOrigin + (float )(length * cosZenith);

      float[][] locs = {
        { xPreOrigin, xEndpoint },
        { yPreOrigin, yEndpoint },
        { zPreOrigin, zEndpoint },
      };

      Gridded3DSet subSet;
      try {
        subSet = new Gridded3DSet(AmandaFile.xyzType, locs, 2);
      } catch (VisADException ve) {
        ve.printStackTrace();

        if (missingSet == null) {
          try {
            missingSet =
              new Gridded3DSet(AmandaFile.xyzType, new float[3][1], 1);
          } catch (VisADException ve2) {
            ve2.printStackTrace();
          }
        }
        subSet = missingSet;
      }

      sets[i] = subSet;
    }

    Gridded1DSet set;
    try {
      set = new Gridded1DSet(RealType.Time,
                             new float[][] { timeSteps },
                             timeSteps.length);
    } catch (VisADException ve) {
      ve.printStackTrace();
      set = null;
    }

    FieldImpl fld;
    try {
      fld = new FieldImpl(timeSequenceType, set);
      fld.setSamples(sets, false);
    } catch (VisADException ve) {
      ve.printStackTrace();
      fld = missing;
    } catch (RemoteException re) {
      re.printStackTrace();
      fld = missing;
    }

    return fld;
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
      " LA#" + zenith + " LO#" + azimuth + " LE#" + length + " NRG#" +
      energy + " TIM#" + time + "]";
  }
}
