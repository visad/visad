//
// MeasureDataFile.java
//

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

package visad.bio;

import java.awt.Color;
import java.io.*;
import java.util.*;
import visad.*;

/**
 * MeasureDataFile represents a file on disk containing 2-D measurement data.
 */
public class MeasureDataFile {

  // -- CONSTANTS --

  /** Variable names for each dimension. */
  private static final String[] VARIABLES = { "x", "y", "slice" };

  /** Data file label for beginning of file. */
  private static final String BEGIN_LABEL = "# BioVisAD measurement data file";

  /** Data file label for line denoting unit type. */
  private static final String UNIT_LABEL = "Unit = ";

  /** Data file label for start of standard measurement matrices. */
  private static final String STANDARD_LABEL = "# Standard measurements";

  /** Data file label for start of master group list. */
  private static final String GROUP_LABEL = "# Groups";

  /** Data file label for start of master measurement list. */
  private static final String ALL_LABEL = "# All measurements";


  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** File containing the measurement data. */
  private File file;


  // -- CONSTRUCTOR --

  /** Constructs a measurement data file. */
  public MeasureDataFile(BioVisAD biovis, File file) {
    bio = biovis;
    this.file = file;
  }


  // -- API METHODS --

  /** Writes the specified measurement lists to the data file. */
  public void write() throws IOException {
    write(Double.NaN, Double.NaN, Double.NaN);
  }

  /**
   * Writes the specified measurement lists to the data file,
   * using the given conversion values between pixels and microns,
   * and distance between measurement slices.
   */
  public void write(double mx, double my, double sd) throws IOException {
    int numIndices = bio.mm.lists.length;
    int numSlices = bio.sm.getNumberOfSlices();
    int numStd = MeasureToolPanel.maxId;
    MData[][][] stdData = new MData[numStd][numIndices][numSlices];
    boolean microns = mx == mx && my == my && sd == sd;
    if (!microns) mx = my = sd = 1;
    Vector v = new Vector();

    // compile measurement data
    int minLen = 2, maxLen = 1;
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.mm.lists[index];
      Vector lines = list.getLines();
      Vector points = list.getPoints();
      int lsize = lines.size();
      for (int i=0; i<lsize; i++) {
        MeasureLine line = (MeasureLine) lines.elementAt(i);
        double[][] vals = {
          {line.ep1.x, line.ep2.x},
          {line.ep1.y, line.ep2.y},
          {line.ep1.z, line.ep2.z}
        };
        int r = line.color.getRed();
        int g = line.color.getGreen();
        int b = line.color.getBlue();
        MData data = new MData(index, line.stdId,
          line.group.getId(), mx, my, sd, vals, r, g, b);
        if (data.stdId >= 0) {
          // add to standard measurement list
          stdData[data.stdId][index][data.slice] = data;
        }
        v.add(data);
        maxLen = 2;
      }
      int psize = points.size();
      for (int i=0; i<psize; i++) {
        MeasurePoint point = (MeasurePoint) points.elementAt(i);
        if (!point.lines.isEmpty()) {
          // skip points that aren't markers
          continue;
        }
        double[][] vals = { {point.x}, {point.y}, {point.z} };
        int r = point.color.getRed();
        int g = point.color.getGreen();
        int b = point.color.getBlue();
        MData data = new MData(index, point.stdId,
          point.group.getId(), mx, my, sd, vals, r, g, b);
        if (data.stdId >= 0) {
          // add to standard measurement list
          stdData[data.stdId][index][data.slice] = data;
        }
        v.add(data);
        minLen = 1;
      }
    }

    // write file
    PrintWriter fout = new PrintWriter(new FileWriter(file));
    fout.println(BEGIN_LABEL);
    fout.println();
    fout.println(UNIT_LABEL +
      (microns ? "microns (" + mx + ", " + my + ", " + sd + ")" : "pixels"));
    fout.println();
    fout.println();

    // standard measurements
    fout.println(STANDARD_LABEL);
    for (int std=0; std<numStd; std++) {
      MData d = stdData[std][0][0];
      if (d == null) continue;
      int gid = d.groupId;
      MeasureGroup group = (MeasureGroup) bio.mm.groups.elementAt(gid);
      fout.println();
      fout.println("[" + std + "] " + group.getName());
      String tabs = "";
      for (int slice=0; slice<numSlices; slice++) tabs = tabs + "\t";
      int len = d.values[0].length;
      if (len == 2) fout.print("distance" + tabs);
      for (int i=0; i<2; i++) {
        for (int j=0; j<len; j++) {
          fout.print(VARIABLES[i] + (j + 1));
          if (i < 1 || j < len - 1) fout.print(tabs);
        }
      }
      fout.println();
      for (int index=0; index<numIndices; index++) {
        // distance
        if (len == 2) {
          for (int slice=0; slice<numSlices; slice++) {
            fout.print(stdData[std][index][slice].dist + "\t");
          }
        }
        // values
        for (int i=0; i<2; i++) {
          for (int j=0; j<len; j++) {
            for (int slice=0; slice<numSlices; slice++) {
              fout.print(stdData[std][index][slice].values[i][j]);
              if (i < 1 || j < len - 1 || slice < numSlices - 1) {
                fout.print("\t");
              }
            }
          }
        }
        fout.println();
      }
    }
    fout.println();
    fout.println();

    // output group information
    fout.println(GROUP_LABEL);
    fout.println();
    fout.println("No\tName\tDescription");
    int numGroups = bio.mm.groups.size();
    for (int g=0; g<numGroups; g++) {
      MeasureGroup group = (MeasureGroup) bio.mm.groups.elementAt(g);
      fout.println(g + "\t" + group.getName() + "\t" + group.getDescription());
    }
    fout.println();
    fout.println();

    // output all measurement information
    fout.println(ALL_LABEL);
    for (int i=minLen; i<=maxLen; i++) {
      int size = v.size();
      boolean doHeader = true;
      for (int j=0; j<size; j++) {
        MData data = (MData) v.elementAt(j);
        int len = data.values[0].length;
        if (i != len) continue;
        if (doHeader) {
          fout.println();
          fout.println(getHeader(i));
          doHeader = false;
        }
        fout.println(data);
      }
    }
    fout.close();
  }

  /** Reads data from the data file into an array of measurement lists. */
  public void read() throws IOException, VisADException {
    double mpp = Double.NaN;
    double mx = 1, my = 1, sd = 1;
    boolean microns = false;
    BufferedReader fin = new BufferedReader(new FileReader(file));
    String ln = "";

    // read in unit data
    while (!ln.startsWith(UNIT_LABEL)) ln = fin.readLine().trim();
    if (ln.indexOf("microns") > 0) {
      microns = true;
      int left = ln.indexOf("(");
      int right = ln.indexOf(")");
      StringTokenizer st =
        new StringTokenizer(ln.substring(left + 1, right), ",");
      int count = st.countTokens();
      if (count == 2) {
        mpp = Double.parseDouble(st.nextToken().trim());
        sd = Double.parseDouble(st.nextToken().trim());
      }
      else if (count == 3) {
        mx = Double.parseDouble(st.nextToken().trim());
        my = Double.parseDouble(st.nextToken().trim());
        sd = Double.parseDouble(st.nextToken().trim());
      }
      else microns = false;
    }

    // read in group data
    while (!ln.equals(GROUP_LABEL)) ln = fin.readLine().trim();
    while (!ln.startsWith("No")) ln = fin.readLine().trim();
    Vector groups = new Vector();
    while (true) {
      ln = fin.readLine().trim();
      if (ln == null || ln.equals("")) break;
      groups.add(ln);
    }

    // read in measurement data
    while (!ln.equals(ALL_LABEL)) ln = fin.readLine().trim();
    Vector v = new Vector();
    int len = 0;
    while (true) {
      ln = fin.readLine();
      if (ln == null) break;
      ln = ln.trim();
      if (ln.startsWith("Timestep")) {
        // determine number of endpoints
        int ndx = ln.lastIndexOf("slice2");
        len = ndx >= 0 ? 2 : 1;
      }
      if (ln.equals("") || ln.startsWith("#") ||
        ln.startsWith("Timestep"))
      {
        continue;
      }
      v.add(new MData(ln, len, mx, my, sd));
    }
    fin.close();

    // clear old group data
    int gsize = groups.size();
    bio.mm.groups.removeAllElements();

    // set up new groups
    for (int i=0; i<gsize; i++) {
      String g = (String) groups.elementAt(i);
      StringTokenizer st = new StringTokenizer(g, "\t");
      int id = Integer.parseInt(st.nextToken());
      String name = st.nextToken();
      String desc = st.hasMoreTokens() ? st.nextToken() : "";
      if (id == BioVisAD.noneGroup.getId()) {
        bio.mm.groups.add(BioVisAD.noneGroup);
      }
      else {
        MeasureGroup group = new MeasureGroup(bio, name);
        group.setDescription(desc);
        group.setId(id);
        if (id >= bio.mm.maxId) bio.mm.maxId = id + 1;
      }
    }

    // clear old measurements
    for (int i=0; i<bio.mm.lists.length; i++) bio.mm.lists[i].removeAll();

    // set up measurements
    int size = v.size();
    int index = bio.sm.getIndex();
    for (int k=0; k<size; k++) {
      MData data = (MData) v.elementAt(k);
      MeasureList list = bio.mm.lists[data.index];
      len = data.values[0].length;
      RealTuple[] values = new RealTuple[len];
      for (int j=0; j<len; j++) {
        Real[] reals = new Real[3];
        for (int i=0; i<3; i++) {
          reals[i] = new Real(i < 2 ?
            bio.sm.dtypes[i] : SliceManager.Z_TYPE, data.values[i][j]);
        }
        values[j] = new RealTuple(reals);
      }
      Color color = new Color(data.r, data.g, data.b);
      MeasureGroup group =
        (MeasureGroup) bio.mm.groups.elementAt(data.groupId);
      if (data.stdId >= bio.toolMeasure.maxId) {
        bio.toolMeasure.maxId = data.stdId + 1;
      }
      if (len == 1) {
        double x = data.values[0][0];
        double y = data.values[1][0];
        double z = data.values[2][0];
        MeasurePoint point = new MeasurePoint(x, y, z, color, group);
        point.setStdId(data.stdId);
        list.addMarker(point, false);
      }
      else if (len == 2) {
        double x1 = data.values[0][0];
        double y1 = data.values[1][0];
        double z1 = data.values[2][0];
        double x2 = data.values[0][1];
        double y2 = data.values[1][1];
        double z2 = data.values[2][1];
        MeasurePoint ep1 = new MeasurePoint(x1, y1, z1, color, group);
        MeasurePoint ep2 = new MeasurePoint(x2, y2, z2, color, group);
        MeasureLine line = new MeasureLine(ep1, ep2, color, group, false);
        line.setStdId(data.stdId);
        list.addLine(line, false);
      }
    }

    // refresh GUI components
    bio.mm.pool2.refresh(true);
    if (bio.mm.pool3 != null) bio.mm.pool3.refresh(true);
    bio.toolMeasure.updateInfo(microns, mx, my, sd);
  }


  // -- HELPER METHODS --

  /** Gets a tab-delimited header of the MData string representation. */
  private static String getHeader(int len) {
    StringBuffer sb = new StringBuffer();
    sb.append("Timestep\tStandard ID\tGroup number\tRed\tGreen\tBlue\t");
    if (len == 2) sb.append("Distance\t");
    for (int i=0; i<3; i++) {
      for (int j=0; j<len; j++) {
        sb.append(VARIABLES[i] + (j + 1));
        if (i < 2 || j < len - 1) sb.append("\t");
      }
    }
    return sb.toString();
  }

  // -- HELPER CLASSES --

  /** Measurement data structure. */
  public class MData {
    public int index, slice;
    public int stdId, groupId;
    public double[][] values;
    public int r, g, b;
    public double dist;

    /** Constructor from line of text. */
    public MData(String line, int len, double mx, double my, double sd) {
      StringTokenizer st = new StringTokenizer(line, "\t");
      index = Integer.parseInt(st.nextToken());
      stdId = Integer.parseInt(st.nextToken());
      groupId = Integer.parseInt(st.nextToken());
      r = Integer.parseInt(st.nextToken());
      g = Integer.parseInt(st.nextToken());
      b = Integer.parseInt(st.nextToken());
      if (len == 2) dist = Double.parseDouble(st.nextToken());
      values = new double[3][len];
      for (int i=0; i<3; i++) {
        for (int j=0; j<len; j++) {
          values[i][j] = Double.parseDouble(st.nextToken());
          // convert values from microns to pixels
          if (i == 0) values[i][j] /= mx;
          else if (i == 1) values[i][j] /= my;
        }
      }
    }

    /** Line constructor. */
    public MData(int index, int stdId, int groupId, double mx,
      double my, double sd, double[][] values, int r, int g, int b)
    {
      this.index = index;
      this.stdId = stdId;
      this.groupId = groupId;
      this.values = values;
      this.r = r;
      this.g = g;
      this.b = b;

      // compute slice
      int len = values[0].length;
      this.slice = (int) values[2][0];
      for (int j=1; j<len; j++) {
        if ((int) values[2][j] != slice) {
          slice = -1;
          break;
        }
      }

      // compute distance
      this.dist = len == 2 ?
        BioUtil.getDistance(values[0][0], values[1][0], values[2][0],
        values[0][1], values[1][1], values[2][1], mx, my, sd) : -1;

      // convert measurement to microns
      for (int j=0; j<len; j++) {
        values[0][j] *= mx;
        values[1][j] *= my;
      }
    }

    /** Gets a tab-delimited string representation. */
    public String toString() {
      int len = values[0].length;
      StringBuffer sb = new StringBuffer();
      sb.append(index + "\t");
      sb.append(stdId + "\t" + groupId + "\t" +
        r + "\t" + g + "\t" + b + "\t");
      if (len == 2) sb.append(dist + "\t");
      for (int i=0; i<3; i++) {
        for (int j=0; j<len; j++) {
          sb.append(values[i][j]);
          if (i < 2 || j < len - 1) sb.append("\t");
        }
      }
      return sb.toString();
    }

  };

}
