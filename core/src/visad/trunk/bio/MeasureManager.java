//
// MeasureManager.java
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
import java.rmi.RemoteException;
import java.util.*;
import javax.swing.*;
import visad.*;
import visad.util.Util;

/** MeasureManager is the class encapsulating VisBio's measurement logic. */
public class MeasureManager {

  // -- CONSTANTS --

  /** Variable names for each dimension. */
  private static final String[] VARIABLES = { "x", "y", "slice" };

  /** Label for beginning of measurement data in state file. */
  private static final String MEASURE_LABEL = "# Measurements";

  /** Label for beginning of export file. */
  private static final String BEGIN_LABEL = "# VisBio measurements";

  /** Label for line denoting unit type. */
  private static final String UNIT_LABEL = "Unit = ";

  /** Label for start of standard measurement tables. */
  private static final String STANDARD_LABEL = "# Standard measurements";

  /** Label for start of master group list. */
  private static final String GROUP_LABEL = "# Groups";

  /** Label for start of lines list in state file. */
  private static final String LINE_LABEL = "# Lines";

  /** Label for start of markers list in state file. */
  private static final String POINT_LABEL = "# Markers";

  /** Label for start of master measurement list. */
  private static final String ALL_LABEL = "# All measurements";


  // -- MEASUREMENT INFO --

  /** List of measurements for each timestep. */
  MeasureList[] lists;

  /** Measurement pool for 2-D display. */
  MeasurePool pool2;

  /** Measurement pool for 3-D display. */
  MeasurePool pool3;

  /** First free id number for measurement groups. */
  int maxId = 0;

  /** Measurement group list. */
  Vector groups = new Vector();

  /** Whether measurements have changed since last save. */
  boolean changed = false;


  // -- OTHER FIELDS --

  /** VisBio frame. */
  private VisBio bio;

  /** File chooser for loading and saving data. */
  private JFileChooser fileBox = new JFileChooser();


  // -- CONSTRUCTORS --

  /** Constructs a measurement manager. */
  public MeasureManager(VisBio biovis)
    throws VisADException, RemoteException
  {
    bio = biovis;

    // 2-D and 3-D measurement pools
    pool2 = new MeasurePool(bio, bio.display2, 2);
    if (bio.display3 != null) pool3 = new MeasurePool(bio, bio.display3, 3);
  }


  // -- API METHODS --

  /** Initializes the measurement lists. */
  public void initLists(int timesteps) throws VisADException, RemoteException {
    lists = new MeasureList[timesteps];
    for (int i=0; i<timesteps; i++) lists[i] = new MeasureList(bio);
    pool2.set(lists[0]);
    if (pool3 != null) pool3.set(lists[0]);
  }

  /** Clears all measurements from all image slices. */
  public void clear() {
    for (int i=0; i<lists.length; i++) lists[i].removeAll();
  }

  /** Gets measurement list for current index. */
  public MeasureList getList() { return lists[bio.sm.getIndex()]; }

  /**
   * Exports measurements to the given output stream in
   * an Excel-friendly text format.
   */
  public void export(File file) {
    if (bio.mm.lists == null) return;

    // prepare file for writing
    PrintWriter fout;
    try {
      fout = new PrintWriter(new FileWriter(file));
    }
    catch (IOException exc) {
      exc.printStackTrace();
      return;
    }

    // compile measurement data
    double[] m = getMicronDistances();
    boolean microns = m[0] == m[0] && m[1] == m[1] && m[2] == m[2];
    if (microns) {
      m[0] /= bio.sm.res_x;
      m[1] /= bio.sm.res_y;
    }
    else m[0] = m[1] = m[2] = 1;
    int[] lengths = new int[2];
    Vector v = new Vector();

    int numIndices = bio.mm.lists.length;
    int numSlices = bio.sm.getNumberOfSlices();
    int numStd = MeasureToolPanel.maxId;
    MData[][][] stdData = new MData[numStd][numIndices][numSlices];

    // compile measurement data
    int minLen = 2;
    int maxLen = 1;
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
          line.group.getId(), m[0], m[1], m[2], vals, r, g, b);
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
        int plen = point.lines.size();
        if (point.lines.size() > 0) {
          // skip points that aren't markers
          continue;
        }
        double[][] vals = { {point.x}, {point.y}, {point.z} };
        int r = point.color.getRed();
        int g = point.color.getGreen();
        int b = point.color.getBlue();
        MData data = new MData(index, point.stdId,
          point.group.getId(), m[0], m[1], m[2], vals, r, g, b);
        if (data.stdId >= 0) {
          // add to standard measurement list
          stdData[data.stdId][index][data.slice] = data;
        }
        v.add(data);
        minLen = 1;
      }
    }

    // write file
    fout.println(BEGIN_LABEL);
    fout.println();
    fout.println(UNIT_LABEL + (microns ?
      "microns (" + m[0] + ", " + m[1] + ", " + m[2] + ")" : "pixels"));
    fout.println();
    fout.println();

    // standard measurements
    if (numStd > 0) {
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
    }

    // output group information
    int numGroups = bio.mm.groups.size();
    if (numGroups > 1) {
      fout.println(GROUP_LABEL);
      fout.println();
      fout.println("No\tName\tDescription");
      for (int g=0; g<numGroups; g++) {
        MeasureGroup grp = (MeasureGroup) bio.mm.groups.elementAt(g);
        fout.println(g + "\t" + grp.getName() + "\t" + grp.getDescription());
      }
      fout.println();
      fout.println();
    }

    // output all measurement information
    if (maxLen >= minLen) {
      fout.println(ALL_LABEL);
      for (int i=lengths[0]; i<=lengths[1]; i++) {
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
    }
    fout.close();
  }


  // -- INTERNAL API METHODS --

  /** Writes the current measurements to the given output stream. */
  void saveState(PrintWriter fout) throws IOException, VisADException {
    if (bio.mm.lists == null) return;

    fout.println(MEASURE_LABEL);
    int numIndices = bio.mm.lists.length;
    fout.println(numIndices);

    // output micron information
    double[] m = getMicronDistances();
    boolean microns = m[0] == m[0] && m[1] == m[1] && m[2] == m[2];
    fout.println(UNIT_LABEL + (microns ?
      "microns (" + m[0] + ", " + m[1] + ", " + m[2] + ")" : "pixels"));

    // output group information
    fout.println(GROUP_LABEL);
    int numGroups = bio.mm.groups.size();
    for (int g=0; g<numGroups; g++) {
      MeasureGroup group = (MeasureGroup) bio.mm.groups.elementAt(g);
      fout.println(g);
      fout.println(group.getName());
      fout.println(group.getDescription());
    }

    // output marker information
    fout.println(POINT_LABEL);
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.mm.lists[index];
      if (list == null) {
        fout.println(0);
        continue;
      }
      Vector lines = list.getLines();
      Vector points = list.getPoints();
      int psize = points.size();
      fout.println(psize);
      for (int i=0; i<psize; i++) {
        MeasurePoint point = (MeasurePoint) points.elementAt(i);
        fout.println(point.x);
        fout.println(point.y);
        fout.println(point.z);
        fout.println(point.stdId);
        fout.println(point.group.getId());
        fout.println(point.color.getRed());
        fout.println(point.color.getGreen());
        fout.println(point.color.getBlue());
        int plen = point.lines.size();
        fout.println(plen);
        for (int j=0; j<plen; j++) {
          MeasureLine line = (MeasureLine) point.lines.elementAt(j);
          fout.println(lines.indexOf(line));
        }
      }
    }

    // output line information
    fout.println(LINE_LABEL);
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.mm.lists[index];
      if (list == null) {
        fout.println(0);
        continue;
      }
      Vector lines = list.getLines();
      Vector points = list.getPoints();
      int lsize = lines.size();
      fout.println(lsize);
      for (int i=0; i<lsize; i++) {
        MeasureLine line = (MeasureLine) lines.elementAt(i);
        fout.println(points.indexOf(line.ep1));
        fout.println(points.indexOf(line.ep2));
        fout.println(line.stdId);
        fout.println(line.group.getId());
        fout.println(line.color.getRed());
        fout.println(line.color.getGreen());
        fout.println(line.color.getBlue());
      }
    }
  }

  /** Restores the current measurements from the given input stream. */
  void restoreState(BufferedReader fin) throws IOException, VisADException {
    if (bio.mm.lists == null) return;

    if (!fin.readLine().equals(MEASURE_LABEL)) {
      throw new VisADException("MeasureManager: incorrect state format");
    }

    // clear any existing measurements
    for (int i=0; i<bio.mm.lists.length; i++) bio.mm.lists[i].removeAll();
    int numIndices = Integer.parseInt(fin.readLine());

    // read in micron information
    double mw = Double.NaN, mh = Double.NaN, sd = Double.NaN;
    boolean microns = false;
    String ln = "";
    while (!ln.startsWith(UNIT_LABEL)) ln = fin.readLine();
    if (ln.indexOf("microns") > 0) {
      microns = true;
      int left = ln.indexOf("(");
      int right = ln.indexOf(")");
      StringTokenizer st =
        new StringTokenizer(ln.substring(left + 1, right), ",");
      if (st.countTokens() == 3) {
        mw = Double.parseDouble(st.nextToken());
        mh = Double.parseDouble(st.nextToken());
        sd = Double.parseDouble(st.nextToken());
      }
      else microns = false;
    }

    // read in group information
    bio.mm.groups.removeAllElements();
    while (!ln.equals(GROUP_LABEL)) ln = fin.readLine();
    while (true) {
      ln = fin.readLine();
      if (ln.equals("") || ln.startsWith("#")) break;
      int id = Integer.parseInt(ln);
      String name = fin.readLine();
      String desc = fin.readLine();
      if (id == VisBio.noneGroup.getId()) bio.mm.groups.add(VisBio.noneGroup);
      else {
        MeasureGroup group = new MeasureGroup(bio, name);
        group.setDescription(desc);
        group.setId(id);
        if (id >= bio.mm.maxId) bio.mm.maxId = id + 1;
      }
    }

    // read in marker information
    while (!ln.equals(POINT_LABEL)) ln = fin.readLine();
    int[][][] endpts = new int[numIndices][][];
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.mm.lists[index];
      int psize = Integer.parseInt(fin.readLine());
      endpts[index] = new int[psize][];
      for (int i=0; i<psize; i++) {
        double x = Double.parseDouble(fin.readLine());
        double y = Double.parseDouble(fin.readLine());
        double z = Double.parseDouble(fin.readLine());
        int stdId = Integer.parseInt(fin.readLine());
        int gid = Integer.parseInt(fin.readLine());
        int r = Integer.parseInt(fin.readLine());
        int g = Integer.parseInt(fin.readLine());
        int b = Integer.parseInt(fin.readLine());
        int plen = Integer.parseInt(fin.readLine());
        endpts[index][i] = new int[plen];
        for (int j=0; j<plen; j++) {
          endpts[index][i][j] = Integer.parseInt(fin.readLine());
        }
        Color color = new Color(r, g, b);
        MeasureGroup group = (MeasureGroup) bio.mm.groups.elementAt(gid);
        MeasurePoint point = new MeasurePoint(x, y, z, color, group);
        point.setStdId(stdId);
        list.addMarker(point, false);
      }
    }

    // read in line information
    while (!ln.equals(LINE_LABEL)) ln = fin.readLine();
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.mm.lists[index];
      Vector points = list.getPoints();
      int lsize = Integer.parseInt(fin.readLine());
      for (int i=0; i<lsize; i++) {
        int ep1_ndx = Integer.parseInt(fin.readLine());
        int ep2_ndx = Integer.parseInt(fin.readLine());
        int stdId = Integer.parseInt(fin.readLine());
        int gid = Integer.parseInt(fin.readLine());
        int r = Integer.parseInt(fin.readLine());
        int g = Integer.parseInt(fin.readLine());
        int b = Integer.parseInt(fin.readLine());
        Color color = new Color(r, g, b);
        MeasureGroup group = (MeasureGroup) bio.mm.groups.elementAt(gid);
        MeasurePoint ep1 = (MeasurePoint) points.elementAt(ep1_ndx);
        MeasurePoint ep2 = (MeasurePoint) points.elementAt(ep2_ndx);
        MeasureLine line = new MeasureLine(ep1, ep2, color, group, false);
        list.addLine(line, false);
      }
    }

    // finish up marker configuration
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.mm.lists[index];
      Vector lines = list.getLines();
      Vector points = list.getPoints();
      for (int i=0; i<endpts[index].length; i++) {
        MeasurePoint point = (MeasurePoint) points.elementAt(i);
        int[] pts = endpts[index][i];
        for (int j=0; j<pts.length; j++) {
          point.lines.add((MeasureLine) lines.elementAt(pts[j]));
        }
      }
    }

    // refresh GUI components
    bio.mm.pool2.refresh(true);
    if (bio.mm.pool3 != null) bio.mm.pool3.refresh(true);
    bio.toolMeasure.updateInfo(microns, mw, mh, sd);
  }

  /** Gets the width, height and slice distance in microns. */
  double[] getMicronDistances() {
    double mw = Double.NaN, mh = Double.NaN, sd = Double.NaN;
    if (bio.toolAlign.getUseMicrons()) {
      mw = bio.toolAlign.getMicronWidth();
      mh = bio.toolAlign.getMicronHeight();
      sd = bio.toolAlign.getSliceDistance();
    }
    return new double[] {mw, mh, sd};
  }


  // -- UTILITY METHODS --

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
      if (len == 2) {
        double[] p = {values[0][0], values[1][0], values[2][0]};
        double[] q = {values[0][1], values[1][1], values[2][1]};
        double[] m = {mx, my, sd};
        this.dist = BioUtil.getDistance(p, q, m);
      }
      else this.dist = -1;

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
