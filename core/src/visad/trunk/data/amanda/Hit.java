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

import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

public class Hit
{
  public static final RealType amplitudeType =
    RealType.getRealType("Hit_Amplitude");
  public static final RealType indexType =
    RealType.getRealType("Hit_Index");
  public static final RealType leadingEdgeTimeType =
    RealType.getRealType("Hit_Leading_Edge_Time");

  private static final RealType timeOverThresholdType =
    RealType.getRealType("Hit_Time_Over_Threshold");

  public static RealTupleType tupleType;

  static {
    try {
      tupleType = new RealTupleType(new RealType[] {
        RealType.XAxis, RealType.YAxis, RealType.ZAxis,
        amplitudeType, leadingEdgeTimeType, timeOverThresholdType
      });
    } catch (VisADException ve) {
      ve.printStackTrace();
      tupleType = null;
    }
  }

  private Module mod;
  private float amplitude, leadEdgeTime, timeOverThreshold;

  Hit(Module mod, float amplitude, float leadEdgeTime,
      float timeOverThreshold)
  {
    this.mod = mod;
    this.amplitude = amplitude;
    this.leadEdgeTime = leadEdgeTime;
    this.timeOverThreshold = timeOverThreshold;
  }

  final RealTuple makeData()
    throws VisADException
  {
    double[] values = {mod.getX(), mod.getY(), mod.getZ(),
                       amplitude, leadEdgeTime, timeOverThreshold};

    // construct Tuple for hit
    RealTuple rt;
    try {
      rt = new RealTuple(tupleType, values);
    } catch (RemoteException re) {
      re.printStackTrace();
      rt = null;
    }

    return rt;
  }

  public String toString()
  {
    return "Hit[Mod#" + mod.getNumber() + " amp " + amplitude +
      " let " + leadEdgeTime + " tot " + timeOverThreshold + "]";
  }
}
