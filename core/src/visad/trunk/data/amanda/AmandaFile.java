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

import java.io.BufferedReader;
/*
import java.io.FileInputStream;
*/
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
import java.net.URL;
*/

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded3DSet;
import visad.Integer1DSet;
import visad.MathType;
import visad.RealTuple;
import visad.RealType;
import visad.RealTupleType;
import visad.Tuple;
import visad.TupleType;
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

class Module
  implements Comparable
{
  private int number;
  private float x, y, z;
  private int string, stringOrder;

  Module(int number)
  {
    this(number, Float.NaN, Float.NaN, Float.NaN, -1, -1);
  }

  Module(int number, float x, float y, float z, int string, int stringOrder)
  {
    this.number = number;
    this.x = x;
    this.y = y;
    this.z = z;
    this.string = string;
    this.stringOrder = stringOrder;
  }

  public int compareTo(Object obj)
  {
    if (obj == null) {
      return 1;
    }

    if (!(obj instanceof Module)) {
      return getClass().toString().compareTo(obj.getClass().toString());
    }

    return compareTo((Module )obj);
  }

  public int compareTo(Module mod)
  {
    return (number - mod.number);
  }

  public boolean equals(Object o) { return (compareTo(o) == 0); }

  int getNumber() { return number; }
  float getX() { return x; }
  float getY() { return y; }
  float getZ() { return z; }

  public String toString()
  {
    return "Module#" + number + "[" + x + "," + y + "," + z +
      "]Str#" + string + "/" + stringOrder;
  }
}

class ModuleList
{
  private ArrayList list;
  private Module[] sortedArray;

  public ModuleList()
  {
    list = null;
    sortedArray = null;
  }

  public void add(Module mod)
  {
    if (list == null) {
      list = new ArrayList();
    }

    list.add(mod);
  }

  public void dump(java.io.PrintStream out)
  {
    // if there are modules to be sorted, do it now
    if (list != null && list.size() > 0) {
      sort();
    }

    // if there are no modules, we're done dumping
    if (sortedArray == null) {
      return;
    }

    final int nMods = sortedArray.length;
    for (int i = 0; i < nMods; i++) {
      out.println(sortedArray[i]);
    }
  }

  public Module find(int number)
  {
    // if there are modules to be sorted, do it now
    if (list != null && list.size() > 0) {
      sort();
    }

    // if one or more sorted modules exist...
    if (sortedArray != null) {

      // look for the specified module number...
      int idx = Arrays.binarySearch(sortedArray, new Module(number));
      if (idx >= 0) {

        // return the desired module
        return sortedArray[idx];
      }
    }

    // couldn't find a module with that number
    return null;
  }

  public Module get(int i)
  {
    // if there are modules to be sorted, do it now
    if (list != null && list.size() > 0) {
      sort();
    }

    if (i < 0 || sortedArray == null || i >= sortedArray.length) {
      return null;
    }

    return sortedArray[i];
  }

  public final boolean isInitialized()
  {
    return (sortedArray != null || (list != null && list.size() > 0));
  }

  public int size()
  {
    int len = 0;

    if (list != null) {
      len += list.size();
    }

    if (sortedArray != null) {
      len += sortedArray.length;
    }

    return len;
  }

  private void sort()
  {
    // if some modules have been sorted...
    if (sortedArray != null) {

      // merge in previously sorted list of modules
      for (int i = 0; i < sortedArray.length; i++) {
        list.add(sortedArray[i]);
      }
    }

    // sort modules
    sortedArray = (Module[] )list.toArray(new Module[list.size()]);
    Arrays.sort(sortedArray);

    // out with the old
    list.clear();
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer("ModuleList[");

    boolean isEmpty = true;

    if (list != null && list.size() > 0) {
      buf.append("unsorted=");
      buf.append(list.size());
      isEmpty = false;
    }

    if (sortedArray != null) {
      buf.append("sorted=");
      buf.append(sortedArray.length);
      isEmpty = false;
    }

    if (isEmpty) buf.append("empty");

    buf.append(']');
    return buf.toString();
  }
}

abstract class BaseTrack
{
  private static RealTupleType xyzType;
  private static FunctionType funcType;

  private static final float LENGTH_SCALE = 1000.0f;

  private float xstart;
  private float ystart;
  private float zstart;
  private float zenith;
  private float azimuth;
  private float length;
  private float energy;
  private float time;
  private float maxLength;

  BaseTrack(float xstart, float ystart, float zstart, float zenith,
            float azimuth, float length, float energy, float time)
  {
    this.xstart = xstart;
    this.ystart = ystart;
    this.zstart = zstart;
    this.zenith = zenith;
    this.azimuth = azimuth;
    this.length = length;
    this.energy = energy;
    this.time = time;
  }

  static void initTypes(RealTupleType xyz, FunctionType trackFunctionType)
  {
    xyzType = xyz;
    funcType = trackFunctionType;
  }

  abstract FlatField makeData()
    throws VisADException;

  final FlatField makeData(float maxLength)
    throws VisADException
  {
    float fldLength = length;
    if (fldLength > maxLength) {
      fldLength = maxLength;
    } else if (fldLength != fldLength) {
      fldLength = -1.0f;
    }

    float fldEnergy = energy;
    if (fldEnergy != fldEnergy) {
      fldEnergy = 1.0f;
    }

    float zs = (float) Math.sin(zenith * Data.DEGREES_TO_RADIANS);
    float zc = (float) Math.cos(zenith * Data.DEGREES_TO_RADIANS);
    float as = (float) Math.sin(azimuth * Data.DEGREES_TO_RADIANS);
    float ac = (float) Math.cos(azimuth * Data.DEGREES_TO_RADIANS);
    float zinc = fldLength * zc;
    float xinc = fldLength * zs * ac;
    float yinc = fldLength * zs * as;

    float[][] locs = {{xstart - LENGTH_SCALE * xinc,
                       xstart + LENGTH_SCALE * xinc},
                      {ystart - LENGTH_SCALE * yinc,
                       ystart + LENGTH_SCALE * yinc},
                      {zstart - LENGTH_SCALE * zinc,
                       zstart + LENGTH_SCALE * zinc}};
    // construct Field for fit
    Gridded3DSet set = new Gridded3DSet(xyzType, locs, 2);
    FlatField field = new FlatField(funcType, set);
    float[][] values = {{time, time}, {fldEnergy, fldEnergy}};

    try {
      field.setSamples(values, false);
    } catch (RemoteException re) {
      re.printStackTrace();
      return null;
    }

    return field;
  }

  public String toString()
  {
    String fullName = getClass().getName();
    int pt = fullName.lastIndexOf('.');
    final int ds = fullName.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    String className = fullName.substring(pt == -1 ? 0 : pt + 1);

    return className + "[" + xstart + "," + ystart + "," + zstart +
      " LA#" + zenith + " LO#" + azimuth + "]";
  }
}

class MCTrack
  extends BaseTrack
{
  MCTrack(float xstart, float ystart, float zstart, float zenith,
          float azimuth, float length, float energy, float time)
  {
    super(xstart, ystart, zstart, zenith, azimuth, length, energy, time);
  }

  final FlatField makeData()
    throws VisADException
  {
    return makeData(1000.0f);
  }
}

class FitTrack
  extends BaseTrack
{
  FitTrack(float xstart, float ystart, float zstart, float zenith,
           float azimuth, float length, float energy, float time)
  {
    super(xstart, ystart, zstart, zenith, azimuth, length, energy, time);
  }

  final FlatField makeData()
    throws VisADException
  {
    return makeData(10000.0f);
  }
}

class Hit
{
  private static RealTupleType tupleType;

  private Module mod;
  private float amplitude, leadEdgeTime, timeOverThreshold;

  Hit(Module mod, float amplitude, float leadEdgeTime,
      float timeOverThreshold)
  {
    this.mod = mod;
    this.amplitude = amplitude;
    this.leadEdgeTime = leadEdgeTime;
    this.timeOverThreshold = timeOverThreshold;
  }

  static void initTypes(RealTupleType hitType) { tupleType = hitType; }

  final RealTuple makeData()
    throws VisADException
  {
    double[] values = {mod.getX(), mod.getY(), mod.getZ(),
                       amplitude, leadEdgeTime, timeOverThreshold};

    // construct Tuple for hit
    RealTuple rt;
    try {
      rt = new RealTuple(tupleType, values);
    } catch (RemoteException re) {
      re.printStackTrace();
      rt = null;
    }

    return rt;
  }

  public String toString()
  {
    return "Hit[Mod#" + mod.getNumber() + " amp " + amplitude +
      " let " + leadEdgeTime + " tot " + timeOverThreshold + "]";
  }
}

class Event
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

  static final RealType getTrackIndexType() { return trackIndexType; }

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

public class AmandaFile
{
  private static boolean typesCreated = false;
  private static RealType xType, yType, zType;
  private static RealType amplitudeType;
  private static RealType letType;
  private static RealType eventIndexType;
  private static RealType moduleIndexType;
  private static FunctionType eventsFunctionType;
  private static FunctionType moduleFunctionType;

  private double xmin = Double.MAX_VALUE;
  private double xmax = Double.MIN_VALUE;
  private double ymin = Double.MAX_VALUE;
  private double ymax = Double.MIN_VALUE;
  private double zmin = Double.MAX_VALUE;
  private double zmax = Double.MIN_VALUE;

  private ModuleList modules = new ModuleList();
  private ArrayList events = new ArrayList();

  private HashMap lastCache = new HashMap();

  AmandaFile(InputStream is)
    throws BadFormException, VisADException
  {
    this(new BufferedReader(new InputStreamReader(is)));
  }

  AmandaFile(BufferedReader br)
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

    createTypes();

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
        if (module == null) {
          continue;
        }

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

  private void createTypes()
    throws VisADException
  {
    if (typesCreated) {
      return;
    }

    // right handed coordinate system
    xType = RealType.XAxis; // positive eastward (along 0 longitude?)
    yType = RealType.YAxis; // positive along -90 longitude (?)
    zType = RealType.ZAxis; // positive up
    // zenith = 0.0f toward -z (this is the "latitude")
    // azimuth = 0.0f toward +x (this is the "longitude")
    RealType timeType = RealType.Time;
    RealType trackIndexType = RealType.getRealType("track_index");
    RealType energyType = RealType.getRealType("energy"); // track energy
    RealType hitIndexType = RealType.getRealType("hit_index");
    amplitudeType = RealType.getRealType("amplitude"); // hit amplitude
    RealType totType = RealType.getRealType("tot"); // hit time-over-threshold
    letType = RealType.getRealType("let"); // hit leading-edge-time
    eventIndexType = RealType.getRealType("event_index");
    moduleIndexType = RealType.getRealType("module_index");

    RealTupleType xyzType = new RealTupleType(xType, yType, zType);
    RealTupleType trackRange = new RealTupleType(timeType, energyType);
    FunctionType trackFunctionType = new FunctionType(xyzType, trackRange);
    RealType[] hitReals =
      {xType, yType, zType, amplitudeType, letType, totType};
    RealTupleType hitType = new RealTupleType(hitReals);
    FunctionType tracksFunctionType =
      new FunctionType(trackIndexType, trackFunctionType);
    FunctionType hitsFunctionType = 
      new FunctionType(hitIndexType, hitType);

    TupleType eventsFunctionRange = new TupleType(new MathType[]
      {tracksFunctionType, hitsFunctionType});
    eventsFunctionType =
      new FunctionType(eventIndexType, eventsFunctionRange);

    moduleFunctionType =
      new FunctionType(moduleIndexType, xyzType);

    BaseTrack.initTypes(xyzType, trackFunctionType);
    Hit.initTypes(hitType);
    Event.initTypes(trackIndexType, hitIndexType,
                    tracksFunctionType, hitsFunctionType);

    typesCreated = true;
  }

  private final void dump(java.io.PrintStream out)
  {
    final int nEvents = events.size();
    for (int i = 0; i < nEvents; i++) {
      ((Event )events.get(i)).dump(out);
    }

    modules.dump(out);
  }

  public static final RealType getAmplitudeType() { return amplitudeType; }
  public static final RealType getEventIndexType() { return eventIndexType; }
  public static final RealType getLeadEdgeTimeType() { return letType; }

  public final double getXMax() { return xmax; }
  public final double getXMin() { return xmin; }
  public static final RealType getXType() { return xType; }

  public final double getYMax() { return ymax; }
  public final double getYMin() { return ymin; }
  public static final RealType getYType() { return yType; }

  public final double getZMax() { return zmax; }
  public final double getZMin() { return zmin; }
  public static final RealType getZType() { return zType; }

  public final Tuple makeData()
    throws VisADException
  {
    // Field of Tuples of track and hit Fields
    final int nevents = events.size();
    Integer1DSet eventsSet =
      new Integer1DSet(eventIndexType, (nevents == 0 ? 1 : nevents));
    FieldImpl eventsField =
      new FieldImpl(eventsFunctionType, eventsSet);
    if (nevents > 0) {
      Tuple[] eventTuples = new Tuple[nevents];
      for (int e = 0; e < nevents; e++) {
        eventTuples[e] = ((Event )events.get(e)).makeData();
      }
      try {
        eventsField.setSamples(eventTuples, false);
      } catch (RemoteException re) {
        re.printStackTrace();
      }
    }
    // return eventsField;

    final int nmodules = modules.size();
    Integer1DSet moduleSet = new Integer1DSet(moduleIndexType, nmodules);
    FlatField moduleField =
      new FlatField(moduleFunctionType, moduleSet);
    float[][] msamples = new float[3][nmodules];
    for (int i = 0; i < nmodules; i++) {
      Module mod = modules.get(i);
      msamples[0][i] = mod.getX();
      msamples[1][i] = mod.getY();
      msamples[2][i] = mod.getZ();
    }
    try {
      moduleField.setSamples(msamples);
    } catch (RemoteException re) {
      re.printStackTrace();
      return null;
    }

    Tuple t;
    try {
      t = new Tuple(new Data[] {eventsField, moduleField});
    } catch (RemoteException re) {
      re.printStackTrace();
      t = null;
    }

    return t;
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

      // skip next two tokens
      tok.nextToken();
      tok.nextToken();

      leadEdgeTime = parseFloat("htLet", tok.nextToken());
      timeOverThreshold = parseFloat("htTot", tok.nextToken());
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
