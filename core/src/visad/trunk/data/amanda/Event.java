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

import java.util.ArrayList;

import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.RealTuple;
import visad.RealType;
import visad.Tuple;
import visad.VisADException;

public class Event
{
  private static RealType hitIndexType;
  private static FunctionType hitsFunctionType;

  private static RealType trackIndexType;
  private static FunctionType tracksFunctionType;

  private int number, run, year, day;
  private double time, timeShift;
  private ArrayList tracks, hits;

  Event(int number, int run, int year, int day, double time, double timeShift)
  {
    this.number = number;
    this.run = run;
    this.year = year;
    this.day = day;
    this.time = time;
    this.timeShift = timeShift;

    this.tracks = new ArrayList();
    this.hits = new ArrayList();
  }

  final void add(Hit hit) { hits.add(hit); }
  final void add(FitTrack track) { tracks.add(track); }
  final void add(MCTrack track) { tracks.add(track); }

  final void dump(java.io.PrintStream out)
  {
    out.println(this);

    final int nHits = hits.size();
    for (int i = 0; i < nHits; i++) {
      out.println("  " + hits.get(i));
    }

    final int nTracks = tracks.size();
    for (int i = 0; i < nTracks; i++) {
      out.println("  " + tracks.get(i));
    }
  }

  public final int getDay() { return day; }
  public final int getNumber() { return number; }
  public final int getRun() { return run; }
  public final double getTime() { return time; }
  public final double getTimeShift() { return timeShift; }

  public final BaseTrack getTrack(int idx)
  {
    if (tracks != null && idx >= 0 && idx < tracks.size()) {
      return (BaseTrack )tracks.get(idx);
    }

    return null;
  }

  static final RealType getTrackIndexType() { return trackIndexType; }

  public final int getYear() { return year; }

  static void initTypes(RealType trackIndex, RealType hitIndex,
                        FunctionType tracksFunc,
                        FunctionType hitsFunc)
  {
    trackIndexType = trackIndex;
    tracksFunctionType = tracksFunc;
    hitIndexType = hitIndex;
    hitsFunctionType = hitsFunc;
  }

  final Tuple makeData()
    throws VisADException
  {
    // finish EM event
    final int ntracks = tracks.size();
    final int nhits = hits.size();

    // if no tracks or hits were found, we're done
    if (ntracks == 0 && nhits == 0) {
      return null;
    }

    // construct parent Field for all tracks
    Integer1DSet tracksSet =
      new Integer1DSet(trackIndexType, (ntracks == 0 ? 1 : ntracks));
    FieldImpl tracksField =
      new FieldImpl(tracksFunctionType, tracksSet);
    if (ntracks > 0) {
      FlatField[] trackFields = new FlatField[ntracks];
      for (int t = 0; t < ntracks; t++) {
        trackFields[t] = ((BaseTrack )tracks.get(t)).makeData();
      }
      try {
        tracksField.setSamples(trackFields, false);
      } catch (RemoteException re) {
        re.printStackTrace();
      }
    }

    // construct parent Field for all hits
    Integer1DSet hitsSet =
      new Integer1DSet(hitIndexType, (nhits == 0 ? 1 : nhits));
    FlatField hitsField =
      new FlatField(hitsFunctionType, hitsSet);
    if (nhits > 0) {
      RealTuple[] hitTuples = new RealTuple[nhits];
      for (int h = 0; h < nhits; h++) {
        hitTuples[h] = ((Hit )hits.get(h)).makeData();
      }
      try {
        hitsField.setSamples(hitTuples, true);
      } catch (RemoteException re) {
        re.printStackTrace();
      }
    }

    // construct Tuple of all tracks and hits
    Tuple t;
    try {
      t = new Tuple(new Data[] {tracksField, hitsField});
    } catch (RemoteException re) {
      re.printStackTrace();
      t = null;
    }

    return t;
  }

  public String toString()
  {
    return "Event#" + number + "[Y" + year + "D" + day +
      " H" + hits.size() + " T" + tracks.size() + "]";
  }
}
