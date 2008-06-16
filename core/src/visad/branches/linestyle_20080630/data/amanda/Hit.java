/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

import visad.util.Util;

public class Hit
  implements Comparable
{
  public static final RealType amplitudeType =
    RealType.getRealType("Hit_Amplitude");
  public static final RealType indexType =
    RealType.getRealType("Hit_Index");
  public static final RealType leadingEdgeTimeType =
    RealType.getRealType("Hit_Leading_Edge_Time");
  public static final RealType moduleType =
    RealType.getRealType("Hit_Module");
  private static final RealType timeOverThresholdType =
    RealType.getRealType("Hit_Time_Over_Threshold");

  public static RealTupleType tupleType;

  public static RealTuple missing;

  static {
    try {
      tupleType = new RealTupleType(new RealType[] {
        moduleType, RealType.XAxis, RealType.YAxis, RealType.ZAxis,
        amplitudeType, leadingEdgeTimeType, timeOverThresholdType
      });

      missing = new RealTuple(tupleType);
    } catch (VisADException ve) {
      ve.printStackTrace();
      tupleType = null;
      missing = null;
    }
  }

  private Module mod;
  private float amplitude, leadEdgeTime, timeOverThreshold;
  private RealTuple data;

  Hit(Module mod, float amplitude, float leadEdgeTime,
      float timeOverThreshold)
  {
    this.mod = mod;
    this.amplitude = amplitude;
    this.leadEdgeTime = leadEdgeTime;
    this.timeOverThreshold = timeOverThreshold;

    this.data = null;
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
    if (!(obj instanceof Hit)) {
      return getClass().toString().compareTo(obj.getClass().toString());
    }

    return compareTo((Hit )obj);
  }

  public int compareTo(Hit h)
  {
    int cmp = compareFloat(leadEdgeTime, h.leadEdgeTime);
    if (cmp == 0) {
      cmp = compareFloat(timeOverThreshold, h.timeOverThreshold);
      if (cmp == 0) {
        cmp = compareFloat(amplitude, h.amplitude);
        if (cmp == 0) {
          cmp = mod.compareTo(h.mod);
        }
      }
    }

    return cmp;
  }

  public boolean equals(Object obj) { return (compareTo(obj) == 0); }

  public final float getAmplitude() { return amplitude; }
  public final float getLeadingEdgeTime() { return leadEdgeTime; }
  public final Module getModule() { return mod; }
  public final float getTimeOverThreshold() { return timeOverThreshold; }

  public final RealTuple makeData()
  {
    if (data == null) {
      // construct Tuple for hit
      try {
        data = new RealTuple(tupleType,
                             new double[] {
                               mod.getNumber(),
                               mod.getX(), mod.getY(), mod.getZ(),
                               amplitude, leadEdgeTime, timeOverThreshold
                             });
      } catch (RemoteException re) {
        re.printStackTrace();
        data = missing;
      } catch (VisADException ve) {
        ve.printStackTrace();
        data = missing;
      }
    }

    return data;
  }

  public String toString()
  {
    return "Hit[Mod#" + mod.getNumber() + " amp " + amplitude +
      " let " + leadEdgeTime + " tot " + timeOverThreshold + "]";
  }
}
