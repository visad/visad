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
  private static final String[] VARIABLES = {
    "x", "y", "slice", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
    "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w"
  };

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

  /** Filename containing the measurement data. */
  private File file;


  // -- CONSTRUCTOR --

  /** Constructs a measurement data file. */
  public MeasureDataFile(BioVisAD biovis, File file) {
    bio = biovis;
    this.file = file;
  }


  // -- API METHODS --

  /** Writes the specified measurement lists to the data file. */
  public void write() throws IOException { write(Double.NaN, Double.NaN); }

  /**
   * Writes the specified measurement lists to the data file,
   * using the given conversion value between pixels and microns,
   * and distance between measurement slices.
   */
  public void write(double mpp, double sd) throws IOException {
    int numIndices = bio.lists.length;
    int numSlices = bio.getNumberOfSlices();
    int numStd = MeasureToolPanel.maxId;
    MData[][][] stdData = new MData[numStd][numIndices][numSlices];
    boolean microns = mpp == mpp && sd == sd;
    if (!microns) {
      mpp = 1;
      sd = 1;
    }
    Vector v = new Vector();

    // compile measurement data
    int minDim, maxDim, minLen, maxLen;
    minDim = maxDim = minLen = maxLen = 0;
    for (int index=0; index<numIndices; index++) {
      MeasureList list = bio.lists[index];
      Measurement[] measure = list.getMeasurements();
      for (int i=0; i<measure.length; i++) {
        Measurement m = measure[i];
        double[][] vals = m.doubleValues();
        int dim = vals.length;
        int len = vals[0].length;
        if (dim < minDim || minDim == 0) minDim = dim;
        if (dim > maxDim) maxDim = dim;
        if (len < minLen || minLen == 0) minLen = len;
        if (len > maxLen) maxLen = len;
        int groupId = m.getGroup().getId();
        int stdId = m.stdId;
        Color color = m.getColor();
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        MData data = new MData(index, stdId, groupId, mpp, sd, vals, r, g, b);
        if (data.stdId >= 0) {
          // add to standard measurement list
          stdData[data.stdId][index][data.slice] = data;
        }
        v.add(data);
      }
    }

    // write file
    PrintWriter fout = new PrintWriter(new FileWriter(file));
    fout.println(BEGIN_LABEL);
    fout.println();
    fout.println(UNIT_LABEL +
      (microns ? "microns (" + mpp + ", " + sd + ")" : "pixels"));
    fout.println();
    fout.println();

    // standard measurements
    fout.println(STANDARD_LABEL);
    for (int std=0; std<numStd; std++) {
      MData d = stdData[std][0][0];
      if (d == null) continue;
      int gid = d.groupId;
      MeasureGroup group = (MeasureGroup) bio.groups.elementAt(gid);
      fout.println();
      fout.println("[" + std + "] " + group.getName());
      String tabs = "";
      for (int slice=0; slice<numSlices; slice++) tabs = tabs + "\t";
      int ndx = d.values.length - 1;
      int len = d.values[0].length;
      if (len == 2) fout.print("distance" + tabs);
      for (int i=0; i<ndx; i++) {
        for (int j=0; j<len; j++) {
          fout.print(VARIABLES[i] + (j + 1));
          if (i < ndx - 1 || j < len - 1) fout.print(tabs);
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
        for (int i=0; i<ndx; i++) {
          for (int j=0; j<len; j++) {
            for (int slice=0; slice<numSlices; slice++) {
              fout.print(stdData[std][index][slice].values[i][j]);
              if (i < ndx - 1 || j < len - 1 || slice < numSlices - 1) {
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
    int numGroups = bio.groups.size();
    for (int g=0; g<numGroups; g++) {
      MeasureGroup group = (MeasureGroup) bio.groups.elementAt(g);
      fout.println(g + "\t" + group.getName() + "\t" + group.getDescription());
    }
    fout.println();
    fout.println();

    // output all measurement information
    fout.println(ALL_LABEL);
    for (int i=minDim; i<=maxDim; i++) {
      for (int j=minLen; j<=maxLen; j++) {
        int size = v.size();
        boolean header = true;
        for (int k=0; k<size; k++) {
          MData data = (MData) v.elementAt(k);
          int dim = data.values.length;
          int len = data.values[0].length;
          if (i != dim || j != len) continue;
          if (header) {
            fout.println();
            fout.println(getHeader(i, j, false));
            header = false;
          }
          fout.println(data);
        }
      }
    }
    fout.close();
  }

  /** Reads data from the data file into an array of measurement lists. */
  public void read() throws IOException, VisADException {
    double mpp = 1, sd = 1;
    BufferedReader fin = new BufferedReader(new FileReader(file));
    String line = "";

    // read in unit data
    while (!line.startsWith(UNIT_LABEL)) line = fin.readLine().trim();
    if (line.indexOf("microns") > 0) {
      int left = line.indexOf("(");
      int comma = line.indexOf(",");
      int right = line.indexOf(")");
      mpp = Double.parseDouble(line.substring(left + 1, comma).trim());
      sd = Double.parseDouble(line.substring(comma + 1, right).trim());
    }

    // read in group data
    while (!line.equals(GROUP_LABEL)) line = fin.readLine().trim();
    while (!line.startsWith("No")) line = fin.readLine().trim();
    Vector groups = new Vector();
    while (true) {
      line = fin.readLine().trim();
      if (line == null || line.equals("")) break;
      groups.add(line);
    }

    // read in measurement data
    while (!line.equals(ALL_LABEL)) line = fin.readLine().trim();
    Vector v = new Vector();
    int dim = 0, len = 0;
    while (true) {
      line = fin.readLine();
      if (line == null) break;
      line = line.trim();
      if (line.startsWith("Timestep")) {
        dim = 0;
        // determine dimensionality
        while (true) {
          int ndx = line.lastIndexOf(VARIABLES[dim] + "1");
          if (ndx >= 0) dim++;
          else break;
        }
        // determine number of endpoints
        len = 0;
        while (true) {
          int ndx = line.lastIndexOf(VARIABLES[0] + (len + 1));
          if (ndx >= 0) len++;
          else break;
        }
      }
      if (line.equals("") || line.startsWith("#") ||
        line.startsWith("Timestep"))
      {
        continue;
      }
      v.add(new MData(line, dim, len, mpp, sd));
    }
    fin.close();

    // clear old group data
    int size = groups.size();
    bio.groups.removeAllElements();

    // set up new groups
    for (int i=0; i<size; i++) {
      String g = (String) groups.elementAt(i);
      StringTokenizer st = new StringTokenizer(g, "\t");
      int id = Integer.parseInt(st.nextToken());
      String name = st.nextToken();
      String desc = st.hasMoreTokens() ? st.nextToken() : "";
      MeasureGroup group = new MeasureGroup(bio, name);
      group.setDescription(desc);
      group.setId(id);
      if (id >= bio.maxId) bio.maxId = id + 1;
    }

    // clear old measurements
    for (int i=0; i<bio.lists.length; i++) {
      bio.lists[i].removeAllMeasurements(true);
    }

    // set up measurements
    size = v.size();
    int index = bio.getIndex();
    for (int k=0; k<size; k++) {
      MData data = (MData) v.elementAt(k);
      MeasureList list = bio.lists[data.index];
      dim = data.values.length;
      len = data.values[0].length;
      RealTuple[] values = new RealTuple[len];
      for (int j=0; j<len; j++) {
        Real[] reals = new Real[dim];
        for (int i=0; i<dim; i++) {
          reals[i] = new Real(i < dim - 1 ?
            bio.dtypes[i] : BioVisAD.Z_TYPE, data.values[i][j]);
        }
        values[j] = new RealTuple(reals);
      }
      Color color = new Color(data.r, data.g, data.b);
      MeasureGroup group =
        (MeasureGroup) bio.groups.elementAt(data.groupId);
      Measurement m = new Measurement(values, color, group);
      m.stdId = data.stdId;
      list.addMeasurement(m, data.index == index);
    }
/*
    bio.pool2.refresh();
    if (bio.pool3 != null) bio.pool3.refresh();
*/
  }


  // -- HELPER METHODS --

  /** Gets a tab-delimited header of the MData string representation. */
  private static String getHeader(int dim, int len, boolean slice) {
    StringBuffer sb = new StringBuffer();
    sb.append("Timestep\t");
    if (slice) sb.append("Image slice\t");
    sb.append("Standard ID\tGroup number\tRed\tGreen\tBlue\t");
    if (len == 2) sb.append("Distance\t");
    int ndx = slice ? dim - 1 : dim;
    for (int i=0; i<ndx; i++) {
      for (int j=0; j<len; j++) {
        sb.append(VARIABLES[i] + (j + 1));
        if (i < ndx - 1 || j < len - 1) sb.append("\t");
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
    public MData(String line, int dim, int len, double mpp, double sd) {
      int ndx = dim - 1;
      StringTokenizer st = new StringTokenizer(line, "\t");
      index = Integer.parseInt(st.nextToken());
      stdId = Integer.parseInt(st.nextToken());
      groupId = Integer.parseInt(st.nextToken());
      r = Integer.parseInt(st.nextToken());
      g = Integer.parseInt(st.nextToken());
      b = Integer.parseInt(st.nextToken());
      if (len == 2) dist = Double.parseDouble(st.nextToken());
      values = new double[dim][len];
      for (int i=0; i<dim; i++) {
        for (int j=0; j<len; j++) {
          values[i][j] = Double.parseDouble(st.nextToken());
          // convert values from microns to pixels
          if (i < ndx) values[i][j] /= mpp;
        }
      }
    }

    /** Line constructor. */
    public MData(int index, int stdId, int groupId, double mpp, double sd,
      double[][] values, int r, int g, int b)
    {
      this.index = index;
      this.stdId = stdId;
      this.groupId = groupId;
      this.values = values;
      this.r = r;
      this.g = g;
      this.b = b;

      // compute slice
      int dim = values.length;
      int len = values[0].length;
      int ndx = dim - 1;
      this.slice = (int) values[ndx][0];
      for (int j=1; j<len; j++) {
        if ((int) values[ndx][j] != slice) {
          slice = -1;
          break;
        }
      }

      // compute distance
      this.dist = len == 2 ? Measurement.getDistance(values, mpp, sd) : -1;

      // convert measurement to microns
      for (int i=0; i<ndx; i++) {
        for (int j=0; j<len; j++) values[i][j] *= mpp;
      }
    }

    /** Gets a tab-delimited string representation. */
    public String toString() {
      int dim = values.length;
      int len = values[0].length;
      StringBuffer sb = new StringBuffer();
      sb.append(index + "\t");
      sb.append(stdId + "\t" + groupId + "\t" +
        r + "\t" + g + "\t" + b + "\t");
      if (len == 2) sb.append(dist + "\t");
      for (int i=0; i<dim; i++) {
        for (int j=0; j<len; j++) {
          sb.append(values[i][j]);
          if (i < dim - 1 || j < len - 1) sb.append("\t");
        }
      }
      return sb.toString();
    }

  };

}
