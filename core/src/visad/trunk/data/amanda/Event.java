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
import visad.Integer1DSet;
import visad.MathType;
import visad.RealTuple;
import visad.RealType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;

public class Event
{
  public static final RealType indexType =
    RealType.getRealType("Event_Index");

  public static TupleType tupleType;

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
  public final int getNumber() { return number; }
  public final int getRun() { return run; }
  public final double getTime() { return time; }
  public final double getTimeShift() { return timeShift; }

  public final BaseTrack getTrack(int idx) { return tracks.get(idx); }

  public final int getYear() { return year; }

  final Tuple makeData()
    throws VisADException
  {
    // construct Tuple of all tracks and hits
    Tuple t;
    try {
      Data tData = tracks.makeData();
      Data hData = hits.makeData();
      if (tData == null && hData == null) {
        t = null;
      } else {
        t = new Tuple(new Data[] {tData, hData});
      }
    } catch (RemoteException re) {
      re.printStackTrace();
      t = null;
    }

    return t;
  }

  public String toString()
  {
    return "Event#" + number + "[Y" + year + "D" + day +
      " H" + hits + " T" + tracks + "]";
  }
}
