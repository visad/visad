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
import java.util.Vector;

/** MeasureDataFile maintains a 2-D matrix of measurements. */
public class MeasureDataFile {

  /** Filename containing the measurement data. */
  private File file;

  /** Constructs a measurement data file. */
  public MeasureDataFile(File file) { this.file = file; }

  /** Writes the specified measurement matrix to the data file. */
  public void writeMatrix(MeasureMatrix mm) throws IOException {
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

  /** Reads the measurement matrix from the data file. */
  public MeasureMatrix readMatrix() throws IOException {
    // CTR: TODO: readMatrix
    // BufferedReader fin = new BufferedReader(new FileReader(file));
    // fin.close();
    return null;
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
