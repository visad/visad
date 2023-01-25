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

import visad.Data;
import visad.FieldImpl;
import visad.MathType;
import visad.RealType;
import visad.ScalarMap;
import visad.TupleType;
import visad.VisADException;

public class Event
{
  public static final RealType indexType =
    RealType.getRealType("Event_Index");

  public static TupleType tupleType;
  public static Data missing = Hits.missing;

  static {
    try {
      tupleType = new TupleType(new MathType[] {
        Tracks.functionType, Hits.functionType
      });
    } catch (VisADException ve) {
      ve.printStackTrace();
      tupleType = null;
    }
  }

  private int number, run, year, day;
  private double time, timeShift;
  private Hits hits;
  private Tracks tracks;

  Event(int number, int run, int year, int day, double time, double timeShift)
  {
    this.number = number;
    this.run = run;
    this.year = year;
    this.day = day;
    this.time = time;
    this.timeShift = timeShift;

    this.hits = new Hits();
    this.tracks = new Tracks();
  }

  final void add(Hit hit) { hits.add(hit); }
  final void add(FitTrack track) { tracks.add(track); }
  final void add(MCTrack track) { tracks.add(track); }

  final void dump(java.io.PrintStream out)
  {
    out.println(this);
    hits.dump(out);
    tracks.dump(out);
  }

  public final int getDay() { return day; }

  public final Hit getHit(int idx) { return hits.get(idx); }

  public final int getNumber() { return number; }

  public final int getNumberOfHits() { return hits.size(); }
  public final int getNumberOfTracks() { return tracks.size(); }

  public final int getRun() { return run; }
  public final double getTime() { return time; }
  public final double getTimeShift() { return timeShift; }

  public final BaseTrack getTrack(int idx) { return tracks.get(idx); }

  public final int getYear() { return year; }

  public final float[][] makeHistogram(ScalarMap xMap, ScalarMap yMap,
                                       ScalarMap cMap, ScalarMap dpyColorMap)
  {
    float[] timeSteps = hits.getTimeSteps();

    // create bins
    int[] bin = new int[timeSteps.length - 1];
    for (int i = 0; i < bin.length; i++) {
      bin[i] = 0;
    }

    // fill bins with count of hits in that bin
    final int hitsLen = hits.size();
    for (int i = 0; i < hitsLen; i++) {
      final float time = hits.get(i).getLeadingEdgeTime();

      // look for the proper bin, and increment its count
      boolean found = false;
      for (int j = 0; !found && j < bin.length; j++) {
        if (time < timeSteps[j]) {
          bin[j]++;
          found = true;
        }
      }

      // if it wasn't found, toss it in the last bin
      if (!found) {
        bin[bin.length - 1]++;
      }
    }

    // calculate maximum bin value
    float binMax = bin[0];
    for (int i = 1; i < bin.length; i++) {
      final int val = bin[i];
      if (val > binMax) {
        binMax = val;
      }
    }

    // build list of point data
    float[] x = new float[bin.length * 4];
    float[] y = new float[bin.length * 4];
    int idx = 0;
    for (int i = 0; i < bin.length; i++) {
      x[idx] = timeSteps[i];
      y[idx] = 0;
      idx++;

      x[idx] = timeSteps[i];
      y[idx] = bin[i];
      idx++;

      x[idx] = timeSteps[i + 1];
      y[idx] = bin[i];
      idx++;

      x[idx] = timeSteps[i + 1];
      y[idx] = 0;
      idx++;
    }

    // set the scalarmap ranges
    try {
      xMap.setRange(0.0, (double )binMax);
      yMap.setRange((double )timeSteps[0],
                    (double )timeSteps[timeSteps.length - 1]);
      cMap.setRange((double )timeSteps[0],
                    (double )timeSteps[timeSteps.length - 1]);
      dpyColorMap.setRange((double )timeSteps[0],
                           (double )timeSteps[timeSteps.length - 1]);
    } catch (RemoteException re) {
      System.err.println("Couldn't set histogram ScalarMap ranges");
      re.printStackTrace();
    } catch (VisADException ve) {
      System.err.println("Couldn't set histogram ScalarMap ranges");
      ve.printStackTrace();
    }

    // return the new data
    return new float[][] { x, y };
  }

  public final FieldImpl makeHitSequence()
  {
    return hits.makeTimeSequence();
  }

  public final FieldImpl makeTrackSequence(int idx)
  {
    return tracks.get(idx).makeTimeSequence(hits.getTimeSteps());
  }

  public String toString()
  {
    return "Event#" + number + "[Y" + year + "D" + day +
      " H" + hits + " T" + tracks + "]";
  }
}
