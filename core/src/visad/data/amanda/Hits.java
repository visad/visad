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

package visad.data.amanda;

import java.rmi.RemoteException;

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
  public static FieldImpl missing;

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

      Gridded1DSet set = new Gridded1DSet(RealType.Time, new float[1][1], 1);
      missing = new FieldImpl(timeSequenceType, set);
    } catch (VisADException ve) {
      ve.printStackTrace();
      functionType = null;
      timeSequenceType = null;
      indexTupleType = null;
      missing = null;
    }
  }

  private float[] timeSteps = null;
  private Integer1DSet timeSubSet = null;
  private FlatField timeMissingFld = null;

  public Hits() { }

  public final void add(Hit hit)
  {
    super.add(hit);

    // need to recompute all data objects which depend on list of Hit objects
    timeSteps = null;
    timeSubSet = null;
    timeMissingFld = null;
  }

  private final boolean computeDataObjects()
  {
    final int numHits = size();

    // build some VisAD Data objects which will be used later
    boolean rtnval = true;
    try {
      timeSubSet = new Integer1DSet(indexType, numHits);
      timeMissingFld = new FlatField(indexTupleType, timeSubSet);
    } catch (VisADException ve) {
      ve.printStackTrace();
      timeSubSet = null;
      timeMissingFld = null;
      rtnval = false;
    }

    return rtnval;
  }

  /**
   * Build the array of timesteps
   */
  private final void computeTimeSteps()
  {
    final int numHits = size();

    float startTime = Float.MAX_VALUE;
    float endTime = Float.MIN_VALUE;

    float minLen = Float.MAX_VALUE;

    // gather info needed to compute number of timesteps
    for (int i = 0; i < numHits; i++) {
      Hit hit = (Hit )internalGet(i);

      final float st = hit.getLeadingEdgeTime();
      if (startTime > st) {
        startTime = st;
      }

      final float len = hit.getTimeOverThreshold();
      if (len < minLen) {
        minLen = len;
      }

      final float et = st + len;
      if (endTime < et) {
        endTime = et;
      }
    }

    final float totalTime = endTime - startTime;

    // figure out how many time steps we can fit into the total interval
    int steps = (int )(totalTime / minLen);
    if (steps < MIN_TIMESTEPS) {
      steps = MIN_TIMESTEPS;
    } else if (steps > MAX_TIMESTEPS) {
      steps = MAX_TIMESTEPS;
    }

    // compute amount of time for each step
    final float stepLen = totalTime / (float )steps;

    timeSteps = new float[steps+1];

    // build array of time steps
    timeSteps[0] = startTime;
    for (int i = 0; i < steps; i++) {
      timeSteps[i+1] = timeSteps[i] + stepLen;
    }
  }

  public final Hit get(int i) { return (Hit )super.internalGet(i); }

  final FlatField getHitsBeforeTime(float time)
  {
    if (timeSubSet == null || timeMissingFld == null) {
      if (!computeDataObjects()) {
        return null;
      }
    }

    final int numHits = size();

    RealTuple[] rt = new RealTuple[numHits];

    for (int i = 0; i < numHits; i++) {
      Hit hit = (Hit )internalGet(i);

      final float leadTime = hit.getLeadingEdgeTime();
      if (time < leadTime) {
        rt[i] = Hit.missing;
      } else {
        rt[i] = hit.makeData();
      }
    }

    FlatField fld;
    try {
      fld = new FlatField(indexTupleType, timeSubSet);
      fld.setSamples(rt, false);
    } catch (VisADException ve) {
      ve.printStackTrace();
      fld = timeMissingFld;
    } catch (RemoteException re) {
      re.printStackTrace();
      fld = timeMissingFld;
    }

    return fld;
  }

  final Gridded1DSet getTimeStepSet(RealType setType)
  {
    if (timeSteps == null) {
      computeTimeSteps();
    }

    Gridded1DSet set;
    try {
      set = new Gridded1DSet(setType,
                             new float[][] { timeSteps },
                             timeSteps.length);
    } catch (VisADException ve) {
      ve.printStackTrace();
      set = null;
    }

    return set;
  }

  final float[] getTimeSteps()
  {
    if (timeSteps == null) {
      computeTimeSteps();
    }

    return timeSteps;
  }

  final FieldImpl makeTimeSequence()
  {
    if (timeSteps == null) {
      computeTimeSteps();
    }

    FlatField[] data = new FlatField[timeSteps.length];

    for (int a = 0; a < timeSteps.length; a++) {
      data[a] = getHitsBeforeTime(timeSteps[a]);
    }

    FieldImpl fld;
    try {
      fld = new FieldImpl(timeSequenceType, getTimeStepSet(RealType.Time));
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
