
//
// F2000Form.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.amanda;

import visad.*;
import visad.java3d.*;
import visad.util.*;
import visad.data.*;
import java.io.*;
import java.rmi.RemoteException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   F2000Form is the VisAD data format adapter for
   F2000 files for Amanda events.<P>
*/
public class F2000Form extends Form implements FormFileInformer {

  private static int num = 0;

  private static RealType x = null;
  private static RealType y = null;
  private static RealType z = null;
  private static RealType time = null;
  private static RealType track_index = null;
  private static RealType energy = null;
  private static RealType hit_index = null;
  private static RealType amplitude = null;
  private static RealType tot = null;
  private static RealType let = null;
  private static RealType event_index = null;

  private double xmin = Double.MAX_VALUE;
  private double xmax = Double.MIN_VALUE;
  private double ymin = Double.MAX_VALUE;
  private double ymax = Double.MIN_VALUE;
  private double zmin = Double.MAX_VALUE;
  private double zmax = Double.MIN_VALUE;

  public F2000Form() {
    super("F2000Form" + num++);
  }

  public boolean isThisType(String name) {
    return name.endsWith(".r");
  }

  public boolean isThisType(byte[] block) {
    return false;
  }

  public String[] getDefaultSuffixes() {
    String[] suff = { "r" };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new BadFormException("F2000Form.save");
  }

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("F2000Form.add");
  }

  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {
    FileInputStream fileStream = new FileInputStream(id);
    return open(fileStream);
  }

  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    InputStream inputStream = url.openStream();
    return open(inputStream);
  }

  private synchronized DataImpl open(InputStream is)
         throws BadFormException, VisADException, IOException {

    if (x == null) {
      // right handed coordinate system
      x = RealType.XAxis; // positive eastward (along 0 longitude?)
      y = RealType.YAxis; // positive along -90 longitude (?)
      z = RealType.ZAxis; // positive up
      // zenith = 0.0f toward -z (this is the "latitude")
      // azimuth = 0.0f toward +x (this is the "longitude")
      time = RealType.Time;
      track_index = RealType.getRealType("track_index");
      energy = RealType.getRealType("energy"); // track energy
      hit_index = RealType.getRealType("hit_index");
      amplitude = RealType.getRealType("amplitude"); // hit amplitude
      tot = RealType.getRealType("tot"); // hit time-over-threshold
      let = RealType.getRealType("let"); // hit leading-edge-time
      event_index = RealType.getRealType("event_index");
    }

    RealTupleType xyz = new RealTupleType(x, y, z);
    RealTupleType track_range = new RealTupleType(time, energy);
    FunctionType track_function_type = new FunctionType(xyz, track_range);
    RealType[] hit_reals = {x, y, z, amplitude, tot, let};
    RealTupleType hit_type = new RealTupleType(hit_reals);
    FunctionType tracks_function_type =
      new FunctionType(track_index, track_function_type);
    FunctionType hits_function_type = 
      new FunctionType(hit_index, hit_type);

    TupleType events_function_range = new TupleType(new MathType[]
      {tracks_function_type, hits_function_type});
    FunctionType events_function_type =
      new FunctionType(event_index, events_function_range);

    // array for saving 'last' values for F2000 '*' notation
    int NLAST = 100;
    last_values = new double[NLAST];
    for (int i=0; i<NLAST; i++) last_values[i] = Double.NaN;

    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    String detector = null;
    float longitude = Float.NaN;
    float latitude = Float.NaN;
    float depth = Float.NaN;
    int nstrings = -1;
    int nmodule = -1;

    float[] om_x = null;
    float[] om_y = null;
    float[] om_z = null;
    int[] om_string = null;
    int[] om_ordinal_on_string = null;

    Vector em_events = new Vector();
    int event_number = 0;

    try {

      // read V record
      String[] tokens = getNext(br);
      if (!tokens[0].equals("v")) {
        throw new BadFormException("first line must start with v\n" + line);
      }

      // read ARRAY record
      while (true) {
        tokens = getNext(br);
        if (tokens[0].equals("array")) {
          try {
            detector = tokens[1];
            longitude = getFloat(tokens[2], 2);
            latitude = getFloat(tokens[3], 3);
            depth = getFloat(tokens[4], 4);
            nstrings = getInt(tokens[5], 5);
            nmodule = getInt(tokens[6], 6);
            if (nstrings < 1 || nmodule < 1) {
              throw new BadFormException("bad nstrings or nmodule\n" + line);
            }
System.out.println("array " + detector + " " + longitude + " " + latitude + " " +
                   depth + " " + nstrings + " " + nmodule + "\n" + line);
            om_x = new float[nmodule];
            om_y = new float[nmodule];
            om_z = new float[nmodule];
            om_string = new int[nmodule];
            om_ordinal_on_string = new int[nmodule];
            for (int i=0; i<nmodule; i++) {
              om_string[i] = -1;
              om_ordinal_on_string[i] = -1;
              om_x[i] = Float.NaN;
              om_y[i] = Float.NaN;
              om_z[i] = Float.NaN;
            }
          }
          catch(NumberFormatException e) {
            throw new BadFormException("bad number format with array\n" + line);
          }
          break;
        }
      }

      // read OM records
      while (true) {
        br.mark(1024); // mark br position in order to be able to
                       // backspace
        tokens = getNext(br);
        // first ES or EM marks end of OMs
        if (tokens[0].equals("es") || tokens[0].equals("em")) {
          br.reset(); // backspace
          break;
        }
        if (tokens[0].equals("om")) {
          int number = 0;
          try {
            // convert 1-based to 0-based
            number = getInt(tokens[1], 11) - 1;
            om_ordinal_on_string[number] = getInt(tokens[2], 12);
            om_string[number] = getInt(tokens[3], 13);
            om_x[number] = getFloat(tokens[4], 14);
            om_y[number] = getFloat(tokens[5], 15);
            om_z[number] = getFloat(tokens[6], 16);
          }
          catch(NumberFormatException e) {
            throw new BadFormException("bad number format with om\n" + line);
          }
System.out.println("om " + number + " " + om_ordinal_on_string[number] + " " +
                   om_string[number] + " " + om_x[number] + " " +
                   om_y[number] + " " + om_z[number] + "\n" + line);
        }
      }
/*
int nxmiss = 0;
int nymiss = 0;
int nzmiss = 0;
for (int i=0; i<nmodule; i++) {
  if (om_x[i] != om_x[i]) nxmiss++;
  if (om_y[i] != om_y[i]) nymiss++;
  if (om_z[i] != om_z[i]) nzmiss++;
}
System.out.println("nmodule = " + nmodule + " " + nxmiss + " " +
                   nymiss + " " + nzmiss);
*/
      for (int i=0; i<nmodule; i++) {
        if (om_x[i] == om_x[i]) {
          if (om_x[i] < xmin) xmin = om_x[i];
          if (om_x[i] > xmax) xmax = om_x[i];
        }
        if (om_y[i] == om_y[i]) {
          if (om_y[i] < ymin) ymin = om_y[i];
          if (om_y[i] > ymax) ymax = om_y[i];
        }
        if (om_z[i] == om_z[i]) {
          if (om_z[i] < zmin) zmin = om_z[i];
          if (om_z[i] > zmax) zmax = om_z[i];
        }
      }

      // read ES and EM events
      while (true) {
        tokens = getNext(br);
        if (tokens[0].equals("es")) {
          // ignore ES events for now
System.out.println("es IGNORE \n" + line);
        }
        else if (tokens[0].equals("em")) {
System.out.println(line);
          // assemble EM event
          try {
            int enr = getInt(tokens[1], 21);
            int year = getInt(tokens[2], 22);
            int day = getInt(tokens[3], 23);
            double em_time = getDouble(tokens[4], 24);
            // time shift in nsec of all times in event
            double em_time_shift = getDouble(tokens[5], 25) * 0.000000001;
          }
          catch(NumberFormatException e) {
            throw new BadFormException("bad number format with em\n" + line);
          }

          // initialize tracks and hits to empty
          Vector tracks = new Vector();
          Vector hits = new Vector();

          // read TR and HT records
          while (true) {
            tokens = getNext(br);

            if (tokens[0].equals("tr")) {
// TR nr parent type xstart ystart zstart zenith azimuth length energy time
              try {
                float xstart = getFloat(tokens[4], 34);
                float ystart = getFloat(tokens[5], 35);
                float zstart = getFloat(tokens[6], 36);
                float zenith = getFloat(tokens[7], 37); // 0.0f toward -z
                float azimuth = getFloat(tokens[8], 38); // 0.0f toward +x
                float length = getFloat(tokens[9], 39);
                float tr_energy = getFloat(tokens[10], 40);
                float tr_time = getFloat(tokens[11], 41);

                if (length > 1000.0f) length = 1000.0f;
                if (tr_energy != tr_energy) tr_energy = 1.0f;

                float zs = (float) Math.sin(zenith * Data.DEGREES_TO_RADIANS);
                float zc = (float) Math.cos(zenith * Data.DEGREES_TO_RADIANS);
                float as = (float) Math.sin(azimuth * Data.DEGREES_TO_RADIANS);
                float ac = (float) Math.cos(azimuth * Data.DEGREES_TO_RADIANS);
                float zinc = length * zc;
                float xinc = length * zs * ac;
                float yinc = length * zs * as;

                float[][] locs = {{xstart, xstart + xinc},
                                  {ystart, ystart + yinc},
                                  {zstart, zstart + zinc}};
/*
System.out.println("tr (" + xstart + ", " + ystart + ", " +
                   zstart + "), (" + xinc + ", " +
                   yinc + ", " + zinc + ")\n" + line);
*/
                Gridded3DSet track_set = new Gridded3DSet(xyz, locs, 2);
                FlatField track_field =
                  new FlatField(track_function_type, track_set);
                float[][] values = {{tr_time, tr_time}, {tr_energy, tr_energy}};
                track_field.setSamples(values, false);
                tracks.addElement(track_field);
              }
              catch(NumberFormatException e) {
                throw new BadFormException("bad number format with tr\n" + line);
              }
            }
            else if (tokens[0].equals("fit")) {
// FIT id type xstart ystart zstart zenith azimuth time length energy
              try {
                float xstart = getFloat(tokens[3], 54);
                float ystart = getFloat(tokens[4], 55);
                float zstart = getFloat(tokens[5], 56);
                float zenith = getFloat(tokens[6], 57); // 0.0f toward -z
                float azimuth = getFloat(tokens[7], 58); // 0.0f toward +x
                float length = getFloat(tokens[9], 59);
                float tr_energy = getFloat(tokens[10], 60);
                float tr_time = getFloat(tokens[8], 61);

                // if (length > 1000.0f) length = 1000.0f;
                if (length > 10000.0f) length = 10000.0f;
                if (tr_energy != tr_energy) tr_energy = 1.0f;

                float zs = (float) Math.sin(zenith * Data.DEGREES_TO_RADIANS);
                float zc = (float) Math.cos(zenith * Data.DEGREES_TO_RADIANS);
                float as = (float) Math.sin(azimuth * Data.DEGREES_TO_RADIANS);
                float ac = (float) Math.cos(azimuth * Data.DEGREES_TO_RADIANS);
                float zinc = length * zc;
                float xinc = length * zs * ac;
                float yinc = length * zs * as;

                float[][] locs = {{xstart, xstart + xinc},
                                  {ystart, ystart + yinc},
                                  {zstart, zstart + zinc}};
/*
System.out.println("fit (" + xstart + ", " + ystart + ", " +
                   zstart + "), (" + xinc + ", " +
                   yinc + ", " + zinc + ") " + length + " " +
                   event_number + "\n" + line);
*/
                Gridded3DSet track_set = new Gridded3DSet(xyz, locs, 2);
                FlatField track_field =
                  new FlatField(track_function_type, track_set);
                float[][] values = {{tr_time, tr_time}, {tr_energy, tr_energy}};
                track_field.setSamples(values, false);
                tracks.addElement(track_field);
              }
              catch(NumberFormatException e) {
                throw new BadFormException("bad number format with fit\n" + line);
              }
            }
            else if (tokens[0].equals("ht")) {
              try {
                // convert 1-based to 0-based
                int number = getInt(tokens[1], 71) - 1;
                float ht_amplitude = getFloat(tokens[2], 72);
                float ht_let = getFloat(tokens[5], 75);
                float ht_tot = getFloat(tokens[6], 76);
                double[] values = {om_x[number], om_y[number], om_z[number],
                                   ht_amplitude, ht_let, ht_tot};
                RealTuple hit_tuple = new RealTuple(hit_type, values);
                hits.addElement(hit_tuple);
              }
              catch(NumberFormatException e) {
                throw new BadFormException("bad number format with ht\n" + line);
              }
            }
            else if (tokens[0].equals("ee")) {
System.out.println("ee");
              // finish EM event
              int ntracks = tracks.size();
              int nhits = hits.size();
              if (ntracks == 0 && nhits == 0) break;
              Integer1DSet tracks_set = (ntracks == 0) ?
                new Integer1DSet(track_index, 1) :
                new Integer1DSet(track_index, ntracks);
              FieldImpl tracks_field =
                new FieldImpl(tracks_function_type, tracks_set);
              if (ntracks > 0) {
                FlatField[] track_fields = new FlatField[ntracks];
                for (int i=0; i<ntracks; i++) {
                  track_fields[i] = (FlatField) tracks.elementAt(i);
                }
                tracks_field.setSamples(track_fields, false);
              }
/*
System.out.println("tracks_field " + event_number + "\n" +
                   tracks_field);
*/
              Integer1DSet hits_set = (nhits == 0) ?
                new Integer1DSet(hit_index, 1) :
                new Integer1DSet(hit_index, nhits);
              FlatField hits_field =
                new FlatField(hits_function_type, hits_set);
              if (nhits > 0) {
                RealTuple[] hit_tuples = new RealTuple[nhits];
                for (int i=0; i<nhits; i++) {
                  hit_tuples[i] = (RealTuple) hits.elementAt(i);
                }
                hits_field.setSamples(hit_tuples, true);
              }

              Tuple em_tuple =
                new Tuple(new Data[] {tracks_field, hits_field});
              em_events.addElement(em_tuple);
              event_number++;
              break;
            }
          } // end while (true) { // read TR and HT records
        } // end else if (tokens[0].equals("em"))
      } // end while (true) { // read ES and EM events

    }
    catch (IOException e) {
System.out.println("IOException " + e.getMessage());
      // end of file - build Data object
      int nevents = em_events.size();
      Integer1DSet events_set = (nevents == 0) ? 
        new Integer1DSet(event_index, 1) :
        new Integer1DSet(event_index, nevents); 
      FieldImpl events_field =
        new FieldImpl(events_function_type, events_set);
      if (nevents > 0) {
        Tuple[] event_tuples = new Tuple[nevents];
        for (int i=0; i<nevents; i++) {
          event_tuples[i] = (Tuple) em_events.elementAt(i);
        }
        events_field.setSamples(event_tuples, false);
      }
      return events_field;
    }
  }

  private double[] last_values = null;

  private float getFloat(String token, int index)
          throws NumberFormatException {
    float value = Float.NaN;
    if (token != null) {
      if (token.equals("inf")) value = Float.POSITIVE_INFINITY;
      else if (token.equals("-inf")) value = Float.NEGATIVE_INFINITY;
      else if (token.equals("?")) value = Float.NaN;
      else if (token.equals("nan")) value = Float.NaN;
      else if (token.equals("*")) value = (float) last_values[index];
      else value = Float.parseFloat(token);
    }
    last_values[index] = value;
    return value;
  }

  private double getDouble(String token, int index)
          throws NumberFormatException {
    double value = Float.NaN;
    if (token != null) {
      if (token.equals("inf")) value = Double.POSITIVE_INFINITY;
      else if (token.equals("-inf")) value = Double.NEGATIVE_INFINITY;
      else if (token.equals("?")) value = Double.NaN;
      else if (token.equals("nan")) value = Double.NaN;
      else if (token.equals("*")) value = last_values[index];
      else value = Double.parseDouble(token);
    }
    last_values[index] = value;
    return value;
  }

  private int getInt(String token, int index)
          throws NumberFormatException {
    int value = -1;
    if (token != null) {
      if (token.equals("inf")) value = Integer.MAX_VALUE;
      else if (token.equals("-inf")) value = Integer.MIN_VALUE;
      else if (token.equals("?")) value = -1;
      else if (token.equals("nan")) value = -1;
      else if (token.equals("*")) value = (int) last_values[index];
      else value = Integer.parseInt(token);
    }
    last_values[index] = value;
    return value;
  }

  private String line = null;

  private String[] getNext(BufferedReader br) throws IOException {
    while (true) {
      line = br.readLine();
      if (line == null) throw new IOException("EOF");
      if (line.length() == 0) continue;
      line = line.toLowerCase();
      char fchar = line.charAt(0);
      if (fchar < 'a' || 'z' < fchar) continue; // skip comments
      StringTokenizer st = new StringTokenizer(line);
      int n = st.countTokens();
      if (n == 0) continue; // skip lines with zero tokens
      String[] tokens = new String[n];
      int i = 0;
      while (st.hasMoreTokens()) {
        tokens[i] = st.nextToken();
        i++;
      }
      if (tokens[0].equals("end")) throw new IOException("EOF");
      return tokens;
    }
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }

  /** run 'java visad.data.visad.F2000Form in_file out_file' to
      convert in_file to out_file in VisAD serialized data format */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args == null || args.length != 1) {
      System.out.println("to test read an F2000 file, run:");
      System.out.println("  'java visad.amanda.F2000Form in_file'");
    }
    F2000Form form = new F2000Form();
    Data temp = null;
    if (args[0].startsWith("http://")) {
      // with "ftp://" this throws "sun.net.ftp.FtpProtocolException: RETR ..."
      URL url = new URL(args[0]);
      temp = form.open(url);
    }
    else {
      temp = form.open(args[0]);
    }
    final Data amanda = temp;
    DisplayImpl display = new DisplayImplJ3D("amanda");
    ScalarMap xmap = new ScalarMap(x, Display.XAxis);
    display.addMap(xmap);
    xmap.setRange(form.xmin, form.xmax);
    ScalarMap ymap = new ScalarMap(y, Display.YAxis);
    display.addMap(ymap);
    ymap.setRange(form.ymin, form.ymax);
    ScalarMap zmap = new ScalarMap(z, Display.ZAxis);
    display.addMap(zmap);
    zmap.setRange(form.zmin, form.zmax);
    // ScalarMap eventmap = new ScalarMap(event_index, Display.SelectValue);
    // display.addMap(eventmap);
    ScalarMap trackmap = new ScalarMap(track_index, Display.SelectValue);
    display.addMap(trackmap);
    // ScalarMap energymap = new ScalarMap(energy, Display.RGB);
    // display.addMap(energymap);
    ScalarMap shapemap = new ScalarMap(amplitude, Display.Shape);
    display.addMap(shapemap);
    ScalarMap shape_scalemap = new ScalarMap(amplitude, Display.ShapeScale);
    display.addMap(shape_scalemap);
    shape_scalemap.setRange(-20.0, 50.0);
    // ScalarMap letmap = new ScalarMap(let, Display.RGB);
    ScalarMap letmap = new ScalarMap(tot, Display.RGB);
    display.addMap(letmap);

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);

    ShapeControl scontrol = (ShapeControl) shapemap.getControl();
    scontrol.setShapeSet(new Integer1DSet(amplitude, 1));

    VisADQuadArray cube = new VisADQuadArray();
    cube.coordinates = new float[]
      {0.1f,  0.1f, -0.1f,     0.1f, -0.1f, -0.1f,
       0.1f, -0.1f, -0.1f,    -0.1f, -0.1f, -0.1f,
      -0.1f, -0.1f, -0.1f,    -0.1f,  0.1f, -0.1f,
      -0.1f,  0.1f, -0.1f,     0.1f,  0.1f, -0.1f,

       0.1f,  0.1f,  0.1f,     0.1f, -0.1f,  0.1f,
       0.1f, -0.1f,  0.1f,    -0.1f, -0.1f,  0.1f,
      -0.1f, -0.1f,  0.1f,    -0.1f,  0.1f,  0.1f,
      -0.1f,  0.1f,  0.1f,     0.1f,  0.1f,  0.1f,

       0.1f,  0.1f,  0.1f,     0.1f,  0.1f, -0.1f,
       0.1f,  0.1f, -0.1f,     0.1f, -0.1f, -0.1f,
       0.1f, -0.1f, -0.1f,     0.1f, -0.1f,  0.1f,
       0.1f, -0.1f,  0.1f,     0.1f,  0.1f,  0.1f,

      -0.1f,  0.1f,  0.1f,    -0.1f,  0.1f, -0.1f,
      -0.1f,  0.1f, -0.1f,    -0.1f, -0.1f, -0.1f,
      -0.1f, -0.1f, -0.1f,    -0.1f, -0.1f,  0.1f,
      -0.1f, -0.1f,  0.1f,    -0.1f,  0.1f,  0.1f,

       0.1f,  0.1f,  0.1f,     0.1f,  0.1f, -0.1f,
       0.1f,  0.1f, -0.1f,    -0.1f,  0.1f, -0.1f,
      -0.1f,  0.1f, -0.1f,    -0.1f,  0.1f,  0.1f,
      -0.1f,  0.1f,  0.1f,     0.1f,  0.1f,  0.1f,

       0.1f, -0.1f,  0.1f,     0.1f, -0.1f, -0.1f,
       0.1f, -0.1f, -0.1f,    -0.1f, -0.1f, -0.1f,
      -0.1f, -0.1f, -0.1f,    -0.1f, -0.1f,  0.1f,
      -0.1f, -0.1f,  0.1f,     0.1f, -0.1f,  0.1f};

    cube.vertexCount = cube.coordinates.length / 3;
    cube.normals = new float[144];
    cube.normals = new float[144];
    for (int i=0; i<24; i+=3) {
      cube.normals[i]     =  0.0f;
      cube.normals[i+1]   =  0.0f;
      cube.normals[i+2]   = -1.0f;

      cube.normals[i+24]  =  0.0f;
      cube.normals[i+25]  =  0.0f;
      cube.normals[i+26]  =  1.0f;

      cube.normals[i+48]  =  1.0f;
      cube.normals[i+49]  =  0.0f;
      cube.normals[i+50]  =  0.0f;

      cube.normals[i+72]  = -1.0f;
      cube.normals[i+73]  =  0.0f;
      cube.normals[i+74]  =  0.0f;

      cube.normals[i+96]  =  0.0f;
      cube.normals[i+97]  =  1.0f;
      cube.normals[i+98]  =  0.0f;

      cube.normals[i+120] =  0.0f;
      cube.normals[i+121] = -1.0f;
      cube.normals[i+122] =  0.0f;
    }
    scontrol.setShapes(new VisADGeometryArray[] {cube});

    // fixes track display?
    // SelectValue bug?
    // amanda = ((FieldImpl) amanda).getSample(99);

    final DataReferenceImpl amanda_ref = new DataReferenceImpl("amanda");
    // amanda_ref.setData(amanda);
    display.addReference(amanda_ref);

System.out.println("amanda MathType\n" + amanda.getType());
// visad.jmet.DumpType.dumpDataType(amanda, System.out);


    final DataReference event_ref = new DataReferenceImpl("event");
    VisADSlider event_slider = new VisADSlider("event", 0, 99, 0, 1.0,
                                               event_ref, event_index);
    Cell cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        int index = (int) ((Real) event_ref.getData()).getValue();
        if (index < 0) index = 0;
        else if (index > 99) index = 99;
        amanda_ref.setData(((FieldImpl) amanda).getSample(index));
      }
    };
    // link cell to hour_ref to trigger doAction whenever
    // 'hour' value changes
    cell.addReference(event_ref);

    JFrame frame = new JFrame("VisAD AERI Viewer");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    frame.getContentPane().add(panel);

    JPanel widget_panel = new JPanel();
    widget_panel.setLayout(new BoxLayout(widget_panel, BoxLayout.Y_AXIS));
    Dimension d = null;
/*
    LabeledColorWidget energy_widget =
      new LabeledColorWidget(energymap);
    widget_panel.add(energy_widget);
    d = new Dimension(400, 250);
    energy_widget.setMaximumSize(d);
*/
    LabeledColorWidget let_widget =
      new LabeledColorWidget(letmap);
    widget_panel.add(let_widget);
    d = new Dimension(400, 250);
    let_widget.setMaximumSize(d);
    // widget_panel.add(new VisADSlider(eventmap));
    widget_panel.add(new VisADSlider(trackmap));
    widget_panel.add(event_slider);
    d = new Dimension(400, 600);
    widget_panel.setMaximumSize(d);
    panel.add(widget_panel);
    JPanel display_panel = (JPanel) display.getComponent();
    d = new Dimension(600, 600);
    display_panel.setPreferredSize(d);
    display_panel.setMinimumSize(d);
    panel.add(display_panel);

    int WIDTH = 1000;
    int HEIGHT = 600;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);
  }

}

