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
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.Integer1DSet;
import visad.RealTuple;
import visad.RealType;
import visad.VisADException;

public class Hits
  extends EventList
{
  public static FunctionType functionType;
  public static FunctionType timeSequenceType;

  public static final RealType indexType =
    RealType.getRealType("Hits_Index");

  private static final int MIN_TIMESTEPS = 20;
  private static final int MAX_TIMESTEPS = 50;

  private static FunctionType indexTupleType;

  static {
    try {
      functionType = new FunctionType(Hit.indexType, Hit.tupleType);

      indexTupleType = new FunctionType(indexType, Hit.tupleType);

      timeSequenceType = new FunctionType(RealType.Time, indexTupleType);

    } catch (VisADException ve) {
      ve.printStackTrace();
      functionType = null;
      timeSequenceType = null;
      indexTupleType = null;
    }
  }

  public Hits() { }

  public final void add(Hit hit) { super.add(hit); }

  public final Hit get(int i) { return (Hit )super.internalGet(i); }

  final FieldImpl makeTimeSequence()
  {
    final int numHits = size();

    // build some VisAD Data objects which will be used later
    Integer1DSet subSet;
    FlatField missingFld;
    try {
      subSet = new Integer1DSet(indexType, numHits);
      missingFld = new FlatField(indexTupleType, subSet);
    } catch (VisADException ve) {
      ve.printStackTrace();
      return null;
    }

    float startTime = Float.MAX_VALUE;
    float endTime = Float.MIN_VALUE;

    float totLen = 0.0F;

    // gather info needed to compute number of timesteps
    for (int i = 0; i < numHits; i++) {
      Hit hit = (Hit )internalGet(i);

      final float st = hit.getLeadingEdgeTime();
      if (startTime > st) {
        startTime = st;
      }

      final float len = hit.getTimeOverThreshold();
      totLen += len;

      final float et = st + len;
      if (endTime < et) {
        endTime = et;
      }
    }

    final float dist = endTime - startTime;
    final float avgLen = totLen / (float )numHits;

    int steps = (int )(dist / avgLen);
    if (steps < MIN_TIMESTEPS) {
      steps = MIN_TIMESTEPS;
    } else if (steps > MAX_TIMESTEPS) {
      steps = MAX_TIMESTEPS;
    }

    final float stepLen = dist / (float )steps;

    float[] timeSteps = new float[steps+1];

    timeSteps[0] = startTime;
    for (int i = 0; i < steps; i++) {
      timeSteps[i+1] = timeSteps[i] + stepLen;
    }

    FlatField[] data = new FlatField[timeSteps.length];

    RealTuple[] rt = new RealTuple[numHits];
    for (int a = 0; a < timeSteps.length; a++) {
      for (int i = 0; i < numHits; i++) {
        Hit hit = (Hit )internalGet(i);

        final float leadTime = hit.getLeadingEdgeTime();
        if (timeSteps[a] < leadTime) {
          rt[i] = Hit.missing;
        } else {
          rt[i] = hit.makeData();
        }
      }

      FlatField fld;
      try {
        fld = new FlatField(indexTupleType, subSet);
        fld.setSamples(rt, false);
      } catch (VisADException ve) {
        ve.printStackTrace();
        fld = missingFld;
      } catch (RemoteException re) {
        re.printStackTrace();
        fld = missingFld;
      }
      data[a] = fld;
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
      fld.setSamples(data, false);
    } catch (VisADException ve) {
      ve.printStackTrace();
      fld = null;
    } catch (RemoteException re) {
      re.printStackTrace();
      fld = null;
    }

    return fld;
  }
}
