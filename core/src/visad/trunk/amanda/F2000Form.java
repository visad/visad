
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
    RealType x = RealType.XAxis;
    RealType y = RealType.YAxis;
    RealType z = RealType.ZAxis;
    RealType time = RealType.Time;
    RealType energy =  RealType.getRealType("energy"); // track energy
    RealType amplitude =  RealType.getRealType("amplitude"); // hit amplitude
    RealType tot =  RealType.getRealType("tot"); // hit time-over-threshold
    RealType let =  RealType.getRealType("let"); // hit leading-edge-time

    RealTupleType xyz = new RealTupleType(x, y, z);
    RealType[] hit_reals = {x, y, z, amplitude, tot, let};
    RealTupleType hit = new RealTupleType(hit_reals);


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
            longitude = Float.parseFloat(tokens[2]);
            latitude = Float.parseFloat(tokens[3]);
            depth = Float.parseFloat(tokens[4]);
            nstrings = Integer.parseInt(tokens[5]);
            nmodule = Integer.parseInt(tokens[6]);
            if (nstrings < 1 || nmodule < 1) {
              throw new BadFormException("bad nstrings or nmodule\n" + line);
            }
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
        tokens = getNext(br);
        br.mark(1024); // mark br position in order to be able to
                       // backspace
        tokens = getNext(br);
        // first ES or EM marks end of OMs
        if (tokens[0].equals("es") || tokens[0].equals("em")) {
          br.reset(); // backspace
          break;
        }
        if (tokens[0].equals("om")) {
          try {
            // convert 1-based to 0-based
            int number = Integer.parseInt(tokens[1]) - 1;
            om_ordinal_on_string[number] = Integer.parseInt(tokens[2]);
            om_string[number] = Integer.parseInt(tokens[3]);
            om_x[number] = Float.parseFloat(tokens[4]);
            om_y[number] = Float.parseFloat(tokens[5]);
            om_z[number] = Float.parseFloat(tokens[6]);
          }
          catch(NumberFormatException e) {
            throw new BadFormException("bad number format with om\n" + line);
          }
        }
      }

      Vector em_events = new Vector();

      // read ES and EM events
      while (true) {
        tokens = getNext(br);
        if (tokens[0].equals("es")) {
          // ignore ES events for now
        }
        else if (tokens[0].equals("em")) {
          // assemble EM event
          try {
            int enr = Integer.parseInt(tokens[1]);
            int year = Integer.parseInt(tokens[2]);
            int day = Integer.parseInt(tokens[3]);
            double em_time = Double.parseDouble(tokens[4]);
            // time shift in nsec of all times in event
            double em_time_shift = Double.parseDouble(tokens[5]) * 0.000000001;
          }
          catch(NumberFormatException e) {
            throw new BadFormException("bad number format with em\n" + line);
          }
          Vector tracks = new Vector();
          Vector hits = new Vector();
          while (true) {
            tokens = getNext(br);
            if (tokens[0].equals("tr")) {
              try {
                float xstart = Float.parseFloat(tokens[4]);
                float ystart = Float.parseFloat(tokens[5]);
                float zstart = Float.parseFloat(tokens[6]);
                float zenith = Float.parseFloat(tokens[7]);
                float azimuth = Float.parseFloat(tokens[8]);
                float length = Float.parseFloat(tokens[9]);
                float tr_energy = Float.parseFloat(tokens[10]);
                double tr_time = Double.parseDouble(tokens[11]);

              }
              catch(NumberFormatException e) {
                throw new BadFormException("bad number format with tr\n" + line);
              }
            }
            else if (tokens[0].equals("ht")) {
              try {
                // convert 1-based to 0-based
                int number = Integer.parseInt(tokens[1]) - 1;
                float ht_amplitude = Float.parseFloat(tokens[2]);
                float ht_let = Float.parseFloat(tokens[5]);
                float ht_tot = Float.parseFloat(tokens[6]);
                // om_x[number] ...


              }
              catch(NumberFormatException e) {
                throw new BadFormException("bad number format with ht\n" + line);
              }
            }
            else if (tokens[0].equals("ee")) {
              break;
            }
          }
          // finish EM event

        }
      }

    }
    catch (IOException e) {
      // end of file - build Data object

    }
    return null;
  }

  private static double[] last_values = new double[32];

  private float getFloat(String token, int index)
          throws NumberFormatException {
    float value = Float.NaN;
    if (token != null) {
      if (token.equals("inf")) value = Float.POSITIVE_INFINITY;
      else if (token.equals("-inf")) value = Float.NEGATIVE_INFINITY;
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
      else if (token.equals("*")) value = last_values[index];
      else value = Double.parseDouble(token);
    }
    last_values[index] = value;
    return value;
  }

  private int getInteger(String token, int index)
          throws NumberFormatException {
    int value = -1;
    if (token != null) {
      if (token.equals("inf")) value = Integer.MAX_VALUE;
      else if (token.equals("-inf")) value = Integer.MIN_VALUE;
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
      if (line == null || line.length() == 0) continue;
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
    if (args[0].startsWith("http://")) {
      // with "ftp://" this throws "sun.net.ftp.FtpProtocolException: RETR ..."
      URL url = new URL(args[0]);
      form.open(url);
    }
    else {
      form.open(args[0]);
    }
    System.exit(0);
  }

}

