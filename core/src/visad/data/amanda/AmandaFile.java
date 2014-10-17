/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.RealType;
import visad.RealTupleType;
import visad.VisADException;

import visad.data.BadFormException;

class DoubleCache
{
  private double value;

  DoubleCache() { this(Double.NaN); }
  DoubleCache(double v) { value = v; }
  double getValue() { return value; }
  void setValue(double v) { value = v; }
}

class FloatCache
{
  private float value;

  FloatCache() { this(Float.NaN); }
  FloatCache(float v) { value = v; }
  float getValue() { return value; }
  void setValue(float v) { value = v; }
}

class IntCache
{
  private int value;

  IntCache() { this(-1); }
  IntCache(int v) { value = v; }
  int getValue() { return value; }
  void setValue(int v) { value = v; }
}

public class AmandaFile
{
  public static final RealType moduleIndexType =
    RealType.getRealType("Module_Index");

  public static RealTupleType xyzType;

  static {
    try {
      xyzType =
        new RealTupleType(RealType.XAxis, RealType.YAxis, RealType.ZAxis);
    } catch (VisADException ve) {
      ve.printStackTrace();
      xyzType = null;
    }
  }

  private double xmin = Double.MAX_VALUE;
  private double xmax = Double.MIN_VALUE;
  private double ymin = Double.MAX_VALUE;
  private double ymax = Double.MIN_VALUE;
  private double zmin = Double.MAX_VALUE;
  private double zmax = Double.MIN_VALUE;

  private ModuleList modules = new ModuleList();
  private ArrayList events = new ArrayList();

  private HashMap lastCache = new HashMap();

  public AmandaFile(String id)
    throws BadFormException, IOException, VisADException
  {
    FileReader rdr = new FileReader(id);
    try {
      loadFile(new BufferedReader(rdr));
    } finally {
      try { rdr.close(); } catch (IOException ioe) { }
    }
  }

  public AmandaFile(URL url)
    throws BadFormException, IOException, VisADException
  {
    InputStream is = url.openStream();
    try {
      loadFile(new BufferedReader(new InputStreamReader(is)));
    } finally {
      try { is.close(); } catch (IOException ioe) { }
    }
  }

  private void loadFile(BufferedReader br)
    throws BadFormException, VisADException
  {
    // read V record
    String firstLine;
    try {
      firstLine = nextLine(br);
    } catch (IOException ioe) {
      throw new BadFormException("Unreadable file");
    }

    if (firstLine == null || firstLine.length() <= 1 ||
        firstLine.charAt(0) != 'v' ||
        !Character.isSpaceChar(firstLine.charAt(1)))
    {
      throw new BadFormException("Bad first line \"" + firstLine + "\"");
    }

    Event currentEvent = null;
    boolean inSlowEvent = false;

    while (true) {
      String line;
      try {
        line = nextLine(br);
      } catch (IOException ioe) {
        throw new BadFormException("Unreadable file");
      }

      // end loop if we've reached the end of the file
      if (line == null) {
        break;
      }

      // ignore blank lines
      if (line.length() == 0) {
        continue;
      }

      StringTokenizer tok = new StringTokenizer(line);

      String keyword = tok.nextToken();

      if (keyword.equals("array")) {
        if (modules.isInitialized()) {
          System.err.println("Warning: Multiple ARRAY lines found");
        }

        int nmodules = readArrayLine(line, tok);

        continue;
      }

      if (keyword.equals("om")) {
        Module module = readOMLine(line, tok);
        if (module != null) {
          modules.add(module);

          final float x = module.getX();
          if (x == x) {
            if (x < xmin) xmin = x;
            if (x > xmax) xmax = x;
          }
          final float y = module.getY();
          if (y == y) {
            if (y < ymin) ymin = y;
            if (y > ymax) ymax = y;
          }
          final float z = module.getZ();
          if (z == z) {
            if (z < zmin) zmin = z;
            if (z > zmax) zmax = x;
          }
        }

        continue;
      }

      if (keyword.equals("es")) {
        // ignore ES events for now
        if (inSlowEvent) {
          System.err.println("Warning: Missing EE for slow event");
        }

        inSlowEvent = true;

        continue;
      }

      if (keyword.equals("em")) {
        if (currentEvent != null) {
          System.err.println("Warning: Missing EE for " + currentEvent);
        }

        currentEvent = startEvent(line, tok);

        continue;
      }

      // read TR and HT records
      if (keyword.equals("tr")) {
        MCTrack track = readTrack(line, tok);
        if (track != null) {
          if (currentEvent == null) {
            System.err.println("Found TRACK " + track +
                               " outside event");
          } else {
            currentEvent.add(track);
          }
        }

        continue;
      }

      if (keyword.equals("fit")) {
        FitTrack fit = readFit(line, tok);
        if (fit != null) {
          if (currentEvent == null) {
            System.err.println("Found FIT " + fit +
                               " outside event");
          } else {
            currentEvent.add(fit);
          }
        }

        continue;
      }

      if (keyword.equals("ht")) {
        Hit hit = readHit(line, tok);
        if (hit != null) {
          if (currentEvent == null) {
            System.err.println("Found HIT " + hit +
                               " outside event");
          } else {
            currentEvent.add(hit);
          }
        }

        continue;
      }

      if (keyword.equals("ee")) {
        if (currentEvent == null) {
          if (inSlowEvent) {
            inSlowEvent = false;
          } else {
            System.err.println("Found EE outside event");
          }
        } else {
          events.add(currentEvent);
          currentEvent = null;
        }

        continue;
      }
    }

    // remove cached values since they're no longer needed
    lastCache.clear();
  }

  private final void dump(java.io.PrintStream out)
  {
    final int nEvents = events.size();
    for (int i = 0; i < nEvents; i++) {
      ((Event )events.get(i)).dump(out);
    }

    modules.dump(out);
  }

  public final Event getEvent(int index)
  {
    return (Event )events.get(index);
  }

  public final int getNumberOfEvents() { return events.size(); }

  public final double getXMax() { return xmax; }
  public final double getXMin() { return xmin; }

  public final double getYMax() { return ymax; }
  public final double getYMin() { return ymin; }

  public final double getZMax() { return zmax; }
  public final double getZMin() { return zmin; }

  public final FieldImpl makeEventData()
  {
    final int num = events.size();
    Integer1DSet set;
    try {
      set = new Integer1DSet(Event.indexType, (num == 0 ? 1 : num));
    } catch (VisADException ve) {
      ve.printStackTrace();
      return null;
    }

    FunctionType funcType;
    FieldImpl fld;
    try {
      funcType = new FunctionType(Event.indexType, Hits.timeSequenceType);
      fld = new FieldImpl(funcType, set);
    } catch (VisADException ve) {
      ve.printStackTrace();
      return null;
    }

    if (num > 0) {
      Data[] samples = new Data[num];
      for (int e = 0; e < num; e++) {
        samples[e] = ((Event )events.get(e)).makeHitSequence();
      }
      try {
        fld.setSamples(samples, false);
      } catch (RemoteException re) {
        re.printStackTrace();
      } catch (VisADException ve) {
        ve.printStackTrace();
      }
    }

    return fld;
  }

  public final FlatField makeModuleData()
    throws RemoteException, VisADException
  {
    // Field of modules
    final int num = modules.size();
    Integer1DSet set;
    try {
      set = new Integer1DSet(moduleIndexType, num);
    } catch (VisADException ve) {
      ve.printStackTrace();
      return null;
    }

    FunctionType funcType;
    FlatField fld;
    try {
      funcType = new FunctionType(moduleIndexType, xyzType);
      fld = new FlatField(funcType, set);
    } catch (VisADException ve) {
      ve.printStackTrace();
      return null;
    }

    if (num > 0) {
      float[][] samples = new float[3][num];
      for (int i = 0; i < num; i++) {
        Module mod = modules.get(i);
        samples[0][i] = mod.getX();
        samples[1][i] = mod.getY();
        samples[2][i] = mod.getZ();
      }
      try {
        fld.setSamples(samples);
      } catch (RemoteException re) {
        re.printStackTrace();
      }
    }

    return fld;
  }

  private String nextLine(BufferedReader rdr)
    throws IOException
  {
    String line = rdr.readLine();
    if (line != null) {
      line = line.trim().toLowerCase();
    }

    return line;
  }

  private int parseChannel(String tokenName, String token)
    throws NumberFormatException
  {
    final int dotIdx = token.indexOf('.');
    if (dotIdx >= 0) {
      token = "-" + token.substring(dotIdx + 1);
    }

    return parseInt(tokenName, token);
  }

  private double parseDouble(String tokenName, String token)
    throws NumberFormatException
  {
    double value;
    if (token == null) {
      value = Double.NaN;
    } else if (token.equals("inf")) {
      value = Double.POSITIVE_INFINITY;
    } else if (token.equals("-inf")) {
      value = Double.NEGATIVE_INFINITY;
    } else if (token.equals("?")) {
      value = Double.NaN;
    } else if (token.equals("nan")) {
      value = Double.NaN;
    } else if (token.equals("*")) {
      DoubleCache cval = (DoubleCache )lastCache.get(tokenName);
      if (cval == null) {
        value = Double.NaN;
      } else {
        value = cval.getValue();
      }
    } else {
      value = Double.parseDouble(token);
    }

    // save value in case next reference uses '*' to access it
    DoubleCache cache = (DoubleCache )lastCache.get(tokenName);
    if (cache == null) {
      lastCache.put(tokenName, new DoubleCache(value));
    } else {
      cache.setValue(value);
    }

    return value;
  }

  private float parseFloat(String tokenName, String token)
    throws NumberFormatException
  {
    float value;
    if (token == null) {
      value = Float.NaN;
    } else if (token.equals("inf")) {
      value = Float.POSITIVE_INFINITY;
    } else if (token.equals("-inf")) {
      value = Float.NEGATIVE_INFINITY;
    } else if (token.equals("?")) {
      value = Float.NaN;
    } else if (token.equals("nan")) {
      value = Float.NaN;
    } else if (token.equals("*")) {
      FloatCache cval = (FloatCache )lastCache.get(tokenName);
      if (cval == null) {
        value = Float.NaN;
      } else {
        value = cval.getValue();
      }
    } else {
      value = Float.parseFloat(token);
    }

    // save value in case next reference uses '*' to access it
    FloatCache cache = (FloatCache )lastCache.get(tokenName);
    if (cache == null) {
      lastCache.put(tokenName, new FloatCache(value));
    } else {
      cache.setValue(value);
    }

    return value;
  }

  private int parseInt(String tokenName, String token)
    throws NumberFormatException
  {
    int value;
    if (token == null) {
      value = -1;
    } else if (token.equals("inf")) {
      value = Integer.MAX_VALUE;
    } else if (token.equals("-inf")) {
      value = Integer.MIN_VALUE;
    } else if (token.equals("?")) {
      value = -1;
    } else if (token.equals("nan")) {
      value = -1;
    } else if (token.equals("*")) {
      IntCache cval = (IntCache )lastCache.get(tokenName);
      if (cval == null) {
        value = -1;
      } else {
        value = cval.getValue();
      }
    } else {
      value = Integer.parseInt(token);
    }

    // save value in case next reference uses '*' to access it
    IntCache cache = (IntCache )lastCache.get(tokenName);
    if (cache == null) {
      lastCache.put(tokenName, new IntCache(value));
    } else {
      cache.setValue(value);
    }

    return value;
  }

  private int readArrayLine(String line, StringTokenizer tok)
    throws BadFormException
  {
    String detector = tok.nextToken();
    int nstrings, nmodules;
    try {
      float longitude = parseFloat("raLon", tok.nextToken());
      float latitude = parseFloat("raLat", tok.nextToken());
      float depth = parseFloat("raDepth", tok.nextToken());
      nstrings = parseInt("raNStr", tok.nextToken());
      nmodules = parseInt("raNMod", tok.nextToken());
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad ARRAY line \"" + line + "\": " +
                                 e.getMessage());
    }

    if (nstrings < 1 || nmodules < 1) {
      throw new BadFormException("Bad ARRAY line \"" + line + "\": " +
                                 (nstrings < 1 ? "nstrings < 1" :
                                  "nmodule < 1"));
    }

    return nmodules;
  }

  private final FitTrack readFit(String line, StringTokenizer tok)
    throws VisADException
  {
    // FIT id type xstart ystart zstart zenith azimuth time length energy
    float xstart, ystart, zstart, zenith, azimuth, length, energy, time;

    // skip ID field
    tok.nextToken();
    tok.nextToken();

    try {
      xstart = parseFloat("fitXStart", tok.nextToken());
      ystart = parseFloat("fitYStart", tok.nextToken());
      zstart = parseFloat("fitZStart", tok.nextToken());
      zenith = parseFloat("fitZenith", tok.nextToken()); // 0.0f toward -z
      azimuth = parseFloat("fitAzimuth", tok.nextToken()); // 0.0f toward +x
      time = parseFloat("fitTime", tok.nextToken());
      length = parseFloat("fitLength", tok.nextToken());
      energy = parseFloat("fitEnergy", tok.nextToken());
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad FIT line \"" + line + "\": " +
                                 e.getMessage());
    }

    return new FitTrack(xstart, ystart, zstart, zenith, azimuth, length,
                        energy, time);
  }

  private final Hit readHit(String line, StringTokenizer tok)
    throws VisADException
  {
    String chanStr = tok.nextToken();
    int number = parseChannel("htNum", chanStr);
    if (number < 0) {
      System.err.println("Warning: Ignoring HIT for secondary channel \"" +
                         chanStr + "\"");
      return null;
    }

    // find this module
    Module mod = modules.find(number);
    if (mod == null) {
      System.err.println("Warning: Module not found for HIT line \"" +
                         line + "\"; hit ignored");
      return null;
    }

    float amplitude, leadEdgeTime, timeOverThreshold;
    try {
      amplitude = parseFloat("htAmp", tok.nextToken());

      // skip pulse id & parent track
      tok.nextToken();
      tok.nextToken();

      leadEdgeTime = parseFloat("htLet", tok.nextToken());
      timeOverThreshold = parseFloat("htTot", tok.nextToken());

      // ignore number of TDC edges
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad HIT line \"" + line + "\": " +
                                 e.getMessage());
    }

    return new Hit(mod, amplitude, leadEdgeTime, timeOverThreshold);
  }

  private final Module readOMLine(String line, StringTokenizer tok)
    throws BadFormException
  {
    String numStr = tok.nextToken();

    int number;
    try {
      number = parseInt("omNum", numStr);
    } catch(NumberFormatException e) {
      throw new BadFormException("unparseable module number \"" + numStr +
                                 "\" in \"" + line + "\"");
    }

    if (number < 0) {
      throw new BadFormException("bad module number \"" + numStr +
                                 "\" in \"" + line + "\"");
    }

    int stringOrder, string;
    float x, y, z;
    try {
      stringOrder = parseInt("modOrd", tok.nextToken());
      string = parseInt("modStr", tok.nextToken());
      x = parseFloat("modX", tok.nextToken());
      y = parseFloat("modY", tok.nextToken());
      z = parseFloat("modZ", tok.nextToken());
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad OM line \"" + line + "\": " +
                                 e.getMessage());
    }

    return new Module(number, x, y, z, string, stringOrder);
  }

  private final MCTrack readTrack(String line, StringTokenizer tok)
    throws VisADException
  {
    // TR nr parent type xstart ystart zstart zenith azimuth length energy time
    // skip first three fields
    for (int i = 0; i < 3; i++) {
      tok.nextToken();
    }

    float xstart, ystart, zstart, zenith, azimuth, length, energy, time;
    try {
      xstart = parseFloat("trXStart", tok.nextToken());
      ystart = parseFloat("trYStart", tok.nextToken());
      zstart = parseFloat("trZStart", tok.nextToken());
      zenith = parseFloat("trZenith", tok.nextToken()); // 0.0f toward -z
      azimuth = parseFloat("trAzimuth", tok.nextToken()); // 0.0f toward +x
      length = parseFloat("trLength", tok.nextToken());
      energy = parseFloat("trEnergy", tok.nextToken());
      time = parseFloat("trTime", tok.nextToken());
    } catch(NumberFormatException e) {
      throw new BadFormException("bad TRACK line \"" + line + "\": " +
                                 e.getMessage());
    }

    return new MCTrack(xstart, ystart, zstart, zenith, azimuth, length,
                       energy, time);
  }

  private final Event startEvent(String line, StringTokenizer tok)
    throws BadFormException
  {
    // assemble EM event
    int evtNum, runNum, year, day;
    double time, timeShift;
    try {
      evtNum = parseInt("emNum", tok.nextToken());
      runNum = parseInt("emRun", tok.nextToken());
      year = parseInt("emYear", tok.nextToken());
      day = parseInt("emDay", tok.nextToken());
      time = parseDouble("emTime", tok.nextToken());
      if (!tok.hasMoreTokens()) {
        timeShift = Double.NaN;
      } else {
        // time shift in nsec of all times in event
        timeShift = parseDouble("emTimeShift", tok.nextToken()) * 0.000000001;
      }
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad EM line \"" + line + "\": " +
                                 e.getMessage());
    }

    return new Event(evtNum, runNum, year, day, time, timeShift);
  }
}
