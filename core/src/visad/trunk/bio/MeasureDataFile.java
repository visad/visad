//
// MeasureDataFile.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.Color;
import java.io.*;
import java.util.*;
import visad.*;

/** MeasureDataFile maintains a 2-D matrix of measurements. */
public class MeasureDataFile {

  /** Filename containing the measurement data. */
  private File file;

  /** Constructs a measurement data file. */
  public MeasureDataFile(File file) { this.file = file; }

  /** Writes the specified measurement matrix to the data file. */
  public void writeMatrix(MeasureMatrix mm) throws IOException {
    writeMatrix(mm, Double.NaN, Double.NaN);
  }

  /**
   * Writes the specified measurement matrix to the data file,
   * using the given conversion values between pixels and microns.
   */
  public void writeMatrix(MeasureMatrix mm, double mpp, double sd)
    throws IOException
  {
    // CTR - TODO - mpp and sd
    MeasureList[][] lists = mm.getMeasureLists();
    Vector lines = new Vector();
    Vector points = new Vector();
    int numIndices = lists.length;
    int numSlices = lists[0].length;
    int numStd = MeasureToolbar.maxId;
    MData[][][] stdData = new MData[numStd][numIndices][numSlices];

    for (int index=0; index<numIndices; index++) {
      MeasureList[] listsIndex = lists[index];
      for (int slice=0; slice<numSlices; slice++) {
        MeasureList list = listsIndex[slice];
        Measurement[] measure = list.getMeasurements();
        for (int i=0; i<measure.length; i++) {
          // get measurement data
          Measurement m = measure[i];
          double[][] vals = m.doubleValues();
          double x1 = vals[0][0];
          double y1 = vals[1][0];
          int groupId = m.getGroup().getId();
          int stdId = m.getStdId();
          boolean point = m.isPoint();

          MData data;
          if (m.isPoint()) {
            // add to points list
            data = new MData(index, slice, stdId, groupId, x1, y1);
            points.add(data);
          }
          else {
            // get additional line data
            Color color = m.getColor();
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            double dist = m.getDistance();
            double x2 = vals[0][1];
            double y2 = vals[1][1];

            // add to lines list
            data = new MData(index, slice, stdId, groupId,
              r, g, b, dist, x1, y1, x2, y2);
            lines.add(data);
          }

          if (data.stdId >= 0) {
            // add to standard measurement list
            stdData[data.stdId][index][slice] = data;
          }
        }
      }
    }

    // write file
    PrintWriter fout = new PrintWriter(new FileWriter(file));
    fout.println("# BioVisAD measurement tool data file");
    fout.println();
    fout.println();
    fout.println("# Standard measurements");
    for (int std=0; std<numStd; std++) {
      MData d = stdData[std][0][0];
      if (d == null) continue;
      boolean ln = !d.point;
      int gid = d.groupId;
      LineGroup group = (LineGroup) LineGroup.groups.elementAt(gid);
      fout.println();
      fout.println("[" + std + "] " + group.name);
      String tabs = "";
      for (int slice=0; slice<numSlices; slice++) tabs = tabs + "\t";
      if (ln) {
        fout.println("distance" + tabs +
          "x1" + tabs + "y1" + tabs + "x2" + tabs + "y2");
      }
      else fout.println("x" + tabs + "y");
      for (int index=0; index<numIndices; index++) {
        // distance
        if (ln) {
          for (int slice=0; slice<numSlices; slice++) {
            fout.print(stdData[std][index][slice].dist + "\t");
          }
        }
        // x1
        for (int slice=0; slice<numSlices; slice++) {
          fout.print(stdData[std][index][slice].x1 + "\t");
        }
        // y1
        for (int slice=0; slice<numSlices; slice++) {
          fout.print(stdData[std][index][slice].y1);
          if (ln || slice < numSlices - 1) fout.print("\t");
        }
        if (ln) {
          // x2
          for (int slice=0; slice<numSlices; slice++) {
            fout.print(stdData[std][index][slice].x2 + "\t");
          }
          // y2
          for (int slice=0; slice<numSlices; slice++) {
            fout.print(stdData[std][index][slice].y2);
            if (slice < numSlices - 1) fout.print("\t");
          }
        }
        fout.println();
      }
    }
    fout.println();
    fout.println();
    fout.println("# Groups");
    fout.println();
    fout.println("No\tName\tDescription");
    int numGroups = LineGroup.groups.size();
    for (int g=0; g<numGroups; g++) {
      LineGroup group = (LineGroup) LineGroup.groups.elementAt(g);
      fout.println(g + "\t" + group.name + "\t" + group.description);
    }
    fout.println();
    fout.println();
    fout.println("# All measurement lines");
    fout.println();
    fout.println("Timestep\tImage slice\tStandard id\tGroup number\t" +
      "Red\tGreen\tBlue\tDistance\tx1\ty1\tx2\ty2");
    int size = lines.size();
    for (int i=0; i<size; i++) {
      MData line = (MData) lines.elementAt(i);
      fout.println(line);
    }
    fout.println();
    fout.println();
    fout.println("# All measurement markers");
    fout.println();
    fout.println("Timestep\tImage slice\tStandard id\tGroup number\tx1\ty1");
    size = points.size();
    for (int i=0; i<size; i++) {
      MData point = (MData) points.elementAt(i);
      fout.println(point);
    }
    fout.close();
  }

  /** Sets the given measurement matrix to match data from the data file. */
  public void readMatrix(MeasureMatrix mm) throws IOException, VisADException {
    readMatrix(mm, Double.NaN, Double.NaN);
  }

  /**
   * Sets the given measurement matrix to match data from the data file,
   * using the given conversion values between pixels and microns.
   */
  public void readMatrix(MeasureMatrix mm, double mpp, double sd)
    throws IOException, VisADException
  {
    // CTR - TODO - mpp and sd
    BufferedReader fin = new BufferedReader(new FileReader(file));
    String line = "";

    // read in group data
    while (!line.equals("# Groups")) line = fin.readLine();
    fin.readLine();
    fin.readLine();
    Vector groups = new Vector();
    while (true) {
      line = fin.readLine();
      if (line == null || line.equals("")) break;
      groups.add(line);
    }

    // read in measurement line data
    while (!line.equals("# All measurement lines")) line = fin.readLine();
    fin.readLine();
    fin.readLine();
    Vector lines = new Vector();
    while (true) {
      line = fin.readLine();
      if (line == null || line.equals("")) break;
      lines.add(new MData(line));
    }

    // read in measurement point data
    while (!line.equals("# All measurement markers")) line = fin.readLine();
    fin.readLine();
    fin.readLine();
    Vector points = new Vector();
    while (true) {
      line = fin.readLine();
      if (line == null || line.equals("")) break;
      points.add(new MData(line));
    }

    fin.close();

    // clear old group data
    int size = groups.size();
    LineGroup.groups.removeAllElements();

    // set up new groups
    for (int i=0; i<size; i++) {
      String g = (String) groups.elementAt(i);
      StringTokenizer st = new StringTokenizer(g, "\t");
      int id = Integer.parseInt(st.nextToken());
      String name = st.nextToken();
      String desc = st.hasMoreTokens() ? st.nextToken() : "";
      LineGroup group = new LineGroup(name);
      group.setDescription(desc);
      group.id = id;
      if (id >= LineGroup.maxId) LineGroup.maxId = id + 1;
    }

    // clear old measurements
    MeasureList[][] lists = mm.getMeasureLists();
    for (int i=0; i<lists.length; i++) {
      for (int j=0; j<lists[i].length; j++) {
        lists[i][j].removeAllMeasurements(false);
      }
    }

    // set up lines
    size = lines.size();
    for (int i=0; i<size; i++) {
      MData data = (MData) lines.elementAt(i);
      MeasureList list = lists[data.index][data.slice];
      RealType[] types = list.getTypes();
      RealTuple[] values = new RealTuple[2];
      Real[][] reals = new Real[2][2];
      reals[0][0] = new Real(types[0], data.x1);
      reals[0][1] = new Real(types[1], data.y1);
      reals[1][0] = new Real(types[0], data.x2);
      reals[1][1] = new Real(types[1], data.y2);
      values[0] = new RealTuple(reals[0]);
      values[1] = new RealTuple(reals[1]);
      Color color = new Color(data.r, data.g, data.b);
      LineGroup group = (LineGroup) LineGroup.groups.elementAt(data.groupId);
      Measurement m = new Measurement(mm, values, color, group);
      m.setStdId(data.stdId);
      list.addMeasurement(m, false);
    }

    // set up points
    size = points.size();
    for (int i=0; i<size; i++) {
      MData data = (MData) points.elementAt(i);
      MeasureList list = lists[data.index][data.slice];
      RealType[] types = list.getTypes();
      RealTuple[] values = new RealTuple[1];
      Real[] reals = new Real[2];
      reals[0] = new Real(types[0], data.x1);
      reals[1] = new Real(types[1], data.y1);
      values[0] = new RealTuple(reals);
      LineGroup group = (LineGroup) LineGroup.groups.elementAt(data.groupId);
      Measurement m = new Measurement(mm, values, Color.white, group);
      m.setStdId(data.stdId);
      list.addMeasurement(m, false);
    }

    mm.refresh();
  }

  /** Measurement data structure. */
  public class MData {
    public int index, slice;
    public int stdId, groupId;
    public int r, g, b;
    public double dist;
    public double x1, y1;
    public double x2, y2;
    public boolean point;

    /** Constructor from line of text. */
    public MData(String line) {
      StringTokenizer st = new StringTokenizer(line, "\t");
      int count = st.countTokens();
      index = Integer.parseInt(st.nextToken());
      slice = Integer.parseInt(st.nextToken());
      stdId = Integer.parseInt(st.nextToken());
      groupId = Integer.parseInt(st.nextToken());
      if (count == 6) {
        // point data
        x1 = Double.parseDouble(st.nextToken());
        y1 = Double.parseDouble(st.nextToken());
        point = true;
      }
      else if (count == 12) {
        // line data
        r = Integer.parseInt(st.nextToken());
        g = Integer.parseInt(st.nextToken());
        b = Integer.parseInt(st.nextToken());
        dist = Double.parseDouble(st.nextToken());
        x1 = Double.parseDouble(st.nextToken());
        y1 = Double.parseDouble(st.nextToken());
        x2 = Double.parseDouble(st.nextToken());
        y2 = Double.parseDouble(st.nextToken());
        point = false;
      }
    }

    /** Point constructor. */
    public MData(int index, int slice, int stdId, int groupId,
      double x1, double y1)
    {
      this(index, slice, stdId, groupId, -1, -1, -1, Double.NaN,
        x1, y1, Double.NaN, Double.NaN, true);
    }

    /** Line constructor. */
    public MData(int index, int slice, int stdId, int groupId,
      int r, int g, int b, double dist, double x1, double y1,
      double x2, double y2)
    {
      this(index, slice, stdId, groupId, r, g, b, dist, x1, y1, x2, y2, false);
    }

    private MData(int index, int slice, int stdId, int groupId,
      int r, int g, int b, double dist, double x1, double y1,
      double x2, double y2, boolean point)
    {
      this.index = index;
      this.slice = slice;
      this.stdId = stdId;
      this.groupId = groupId;
      this.r = r;
      this.g = g;
      this.b = b;
      this.dist = dist;
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
      this.point = point;
    }

    /** Gets a tab-delimited string representation. */
    public String toString() {
      String s = "" + index + "\t" + slice + "\t" +
        stdId + "\t" + groupId + "\t";
      if (point) s = s + x1 + "\t" + y1;
      else {
        s = s + r + "\t" + g + "\t" + b + "\t" + dist + "\t" +
          x1 + "\t" + y1 + "\t" + x2 + "\t" + y2;
      }
      return s;
    }
  };

}
