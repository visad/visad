//
// F2000Form.java
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

package visad.data.amanda;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
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
import visad.VisADQuadArray;

import visad.data.BadFormException;
import visad.data.Form;
import visad.data.FormNode;
import visad.data.FormFileInformer;


abstract class BaseCache
{
  private static ArrayList allCache = new ArrayList();

  BaseCache() { allCache.add(this); }

  static void clearAll()
  {
    final int len = allCache.size();
    for (int i = 0; i < len; i++) {
      ((BaseCache )allCache.get(i)).clearValue();
    }
  }

  abstract void clearValue();
}

class DoubleCache
  extends BaseCache
{
  private double value;

  DoubleCache() { this(Double.NaN); }
  DoubleCache(double v) { value = v; }
  void clearValue() { value = Double.NaN; }
  double getValue() { return value; }
  void setValue(double v) { value = v; }
}

class FloatCache
  extends BaseCache
{
  private float value;

  FloatCache() { this(Float.NaN); }
  FloatCache(float v) { value = v; }
  void clearValue() { value = Float.NaN; }
  float getValue() { return value; }
  void setValue(float v) { value = v; }
}

class IntCache
  extends BaseCache
{
  private int value;

  IntCache() { this(-1); }
  IntCache(int v) { value = v; }
  void clearValue() { value = -1; }
  int getValue() { return value; }
  void setValue(int v) { value = v; }
}

class Module
{
  private float x, y, z;
  private int string, stringOrder;

  Module(float x, float y, float z, int string, int stringOrder)
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.string = string;
    this.stringOrder = stringOrder;
  }

  float getX() { return x; }
  float getY() { return y; }
  float getZ() { return z; }
}

/**
   F2000Form is the VisAD data format adapter for
   F2000 files for Amanda events.<P>
*/
public class F2000Form
  extends Form
  implements FormFileInformer
{
  private static final float LENGTH_SCALE = 1000.0f;
  private static final float CUBE = 0.05f;

  private static int num = 0;

  private static boolean typesCreated = false;
  private static RealType xType, yType, zType;
  private static RealType trackIndexType;
  private static RealType hitIndexType;
  private static RealType amplitudeType;
  private static RealType totType;
  private static RealType letType;
  private static RealType eventIndexType;
  private static RealType moduleIndexType;

  private static RealTupleType xyz, hitType;

  private static FunctionType trackFunctionType;
  private static FunctionType tracksFunctionType;
  private static FunctionType hitsFunctionType;
  private static FunctionType eventsFunctionType;
  private static FunctionType moduleFunctionType;

  private double xmin = Double.MAX_VALUE;
  private double xmax = Double.MIN_VALUE;
  private double ymin = Double.MAX_VALUE;
  private double ymax = Double.MIN_VALUE;
  private double zmin = Double.MAX_VALUE;
  private double zmax = Double.MIN_VALUE;

  private HashMap lastCache = new HashMap();

  public F2000Form()
  {
    super("F2000Form#" + num++);
  }

  public synchronized void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("F2000Form.add");
  }

  private final Tuple buildData(ArrayList emEvents, Module[] om)
    throws VisADException
  {
    // Field of Tuples of track and hit Fields
    final int nevents = emEvents.size();
    Integer1DSet eventsSet =
      new Integer1DSet(eventIndexType, (nevents == 0 ? 1 : nevents));
    FieldImpl events_field =
      new FieldImpl(eventsFunctionType, eventsSet);
    if (nevents > 0) {
      Tuple[] event_tuples = (Tuple[] )emEvents.toArray(new Tuple[nevents]);
      try {
        events_field.setSamples(event_tuples, false);
      } catch (RemoteException re) {
        re.printStackTrace();
      }
    }
    // return events_field;

    final int nmodules = om.length;
    Integer1DSet moduleSet = new Integer1DSet(moduleIndexType, nmodules);
    FlatField module_field =
      new FlatField(moduleFunctionType, moduleSet);
    float[][] msamples = new float[3][nmodules];
    for (int i = 0; i < nmodules; i++) {
      if (om[i] == null) {
        msamples[0][i] = msamples[1][i] = msamples[2][i] = Float.NaN;
      } else {
        msamples[0][i] = om[i].getX();
        msamples[1][i] = om[i].getY();
        msamples[2][i] = om[i].getZ();
      }
    }
    try {
      module_field.setSamples(msamples);
    } catch (RemoteException re) {
      re.printStackTrace();
      return null;
    }

    Tuple t;
    try {
      t = new Tuple(new Data[] {events_field, module_field});
    } catch (RemoteException re) {
      re.printStackTrace();
      t = null;
    }

    return t;
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
    trackIndexType = RealType.getRealType("track_index");
    RealType energyType = RealType.getRealType("energy"); // track energy
    hitIndexType = RealType.getRealType("hit_index");
    amplitudeType = RealType.getRealType("amplitude"); // hit amplitude
    totType = RealType.getRealType("tot"); // hit time-over-threshold
    letType = RealType.getRealType("let"); // hit leading-edge-time
    eventIndexType = RealType.getRealType("event_index");
    moduleIndexType = RealType.getRealType("module_index");

    xyz = new RealTupleType(xType, yType, zType);
    RealTupleType trackRange = new RealTupleType(timeType, energyType);
    trackFunctionType = new FunctionType(xyz, trackRange);
    RealType[] hitReals =
      {xType, yType, zType, amplitudeType, letType, totType};
    hitType = new RealTupleType(hitReals);
    tracksFunctionType =
      new FunctionType(trackIndexType, trackFunctionType);
    hitsFunctionType = 
      new FunctionType(hitIndexType, hitType);

    TupleType eventsFunctionRange = new TupleType(new MathType[]
      {tracksFunctionType, hitsFunctionType});
    eventsFunctionType =
      new FunctionType(eventIndexType, eventsFunctionRange);

    moduleFunctionType =
      new FunctionType(moduleIndexType, xyz);

    typesCreated = true;
  }

  private final Tuple finishEvent(ArrayList emTracks, ArrayList emHits)
    throws VisADException
  {
    // finish EM event
    final int ntracks = emTracks.size();
    final int nhits = emHits.size();

    // if no tracks or hits were found, we're done
    if (ntracks == 0 && nhits == 0) {
      return null;
    }

    // construct parent Field for all tracks
    Integer1DSet tracksSet =
      new Integer1DSet(trackIndexType, (ntracks == 0 ? 1 : ntracks));
    FieldImpl tracks_field =
      new FieldImpl(tracksFunctionType, tracksSet);
    if (ntracks > 0) {
      FlatField[] track_fields =
        (FlatField[] )emTracks.toArray(new FlatField[ntracks]);
      try {
        tracks_field.setSamples(track_fields, false);
      } catch (RemoteException re) {
        re.printStackTrace();
      }

      emTracks.clear();
    }

    // construct parent Field for all hits
    Integer1DSet hitsSet =
      new Integer1DSet(hitIndexType, (nhits == 0 ? 1 : nhits));
    FlatField hits_field =
      new FlatField(hitsFunctionType, hitsSet);
    if (nhits > 0) {
      RealTuple[] hit_tuples =
        (RealTuple[] )emHits.toArray(new RealTuple[nhits]);
      try {
        hits_field.setSamples(hit_tuples, true);
      } catch (RemoteException re) {
        re.printStackTrace();
      }

      emHits.clear();
    }

    // construct Tuple of all tracks and hits
    Tuple t;
    try {
      t = new Tuple(new Data[] {tracks_field, hits_field});
    } catch (RemoteException re) {
      re.printStackTrace();
      t = null;
    }

    return t;
  }

  public final RealType getAmplitude() { return amplitudeType; }

  public static final VisADQuadArray[] getCubeArray()
  {
    VisADQuadArray cube = new VisADQuadArray();
    cube.coordinates = new float[]
      {CUBE,  CUBE, -CUBE,     CUBE, -CUBE, -CUBE,
       CUBE, -CUBE, -CUBE,    -CUBE, -CUBE, -CUBE,
       -CUBE, -CUBE, -CUBE,    -CUBE,  CUBE, -CUBE,
       -CUBE,  CUBE, -CUBE,     CUBE,  CUBE, -CUBE,

       CUBE,  CUBE,  CUBE,     CUBE, -CUBE,  CUBE,
       CUBE, -CUBE,  CUBE,    -CUBE, -CUBE,  CUBE,
       -CUBE, -CUBE,  CUBE,    -CUBE,  CUBE,  CUBE,
       -CUBE,  CUBE,  CUBE,     CUBE,  CUBE,  CUBE,

       CUBE,  CUBE,  CUBE,     CUBE,  CUBE, -CUBE,
       CUBE,  CUBE, -CUBE,     CUBE, -CUBE, -CUBE,
       CUBE, -CUBE, -CUBE,     CUBE, -CUBE,  CUBE,
       CUBE, -CUBE,  CUBE,     CUBE,  CUBE,  CUBE,

       -CUBE,  CUBE,  CUBE,    -CUBE,  CUBE, -CUBE,
       -CUBE,  CUBE, -CUBE,    -CUBE, -CUBE, -CUBE,
       -CUBE, -CUBE, -CUBE,    -CUBE, -CUBE,  CUBE,
       -CUBE, -CUBE,  CUBE,    -CUBE,  CUBE,  CUBE,

       CUBE,  CUBE,  CUBE,     CUBE,  CUBE, -CUBE,
       CUBE,  CUBE, -CUBE,    -CUBE,  CUBE, -CUBE,
       -CUBE,  CUBE, -CUBE,    -CUBE,  CUBE,  CUBE,
       -CUBE,  CUBE,  CUBE,     CUBE,  CUBE,  CUBE,

       CUBE, -CUBE,  CUBE,     CUBE, -CUBE, -CUBE,
       CUBE, -CUBE, -CUBE,    -CUBE, -CUBE, -CUBE,
       -CUBE, -CUBE, -CUBE,    -CUBE, -CUBE,  CUBE,
       -CUBE, -CUBE,  CUBE,     CUBE, -CUBE,  CUBE};

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

    return new VisADQuadArray[] {cube};
  }

  public String[] getDefaultSuffixes()
  {
    String[] suff = { "r" };
    return suff;
  }

  public final RealType getEventIndex() { return eventIndexType; }

  public synchronized FormNode getForms(Data data)
  {
    return null;
  }

  public final RealType getLet() { return letType; }
  public final RealType getTrackIndex() { return trackIndexType; }

  public final RealType getX() { return xType; }
  public final double getXMax() { return xmax; }
  public final double getXMin() { return xmin; }

  public final RealType getY() { return yType; }
  public final double getYMax() { return ymax; }
  public final double getYMin() { return ymin; }

  public final RealType getZ() { return zType; }
  public final double getZMax() { return zmax; }
  public final double getZMin() { return zmin; }

  public boolean isThisType(String name)
  {
    return name.endsWith(".r");
  }

  public boolean isThisType(byte[] block)
  {
    return false;
  }

  private final FlatField makeField(float xstart, float ystart, float zstart,
                                    float zenith, float azimuth, float length,
                                    float energy, float time, float maxLength)
    throws VisADException
  {
    if (length > maxLength) {
      length = maxLength;
    } else if (length != length) {
      length = -1.0f;
    }
    if (energy != energy) {
      energy = 1.0f;
    }

    float zs = (float) Math.sin(zenith * Data.DEGREES_TO_RADIANS);
    float zc = (float) Math.cos(zenith * Data.DEGREES_TO_RADIANS);
    float as = (float) Math.sin(azimuth * Data.DEGREES_TO_RADIANS);
    float ac = (float) Math.cos(azimuth * Data.DEGREES_TO_RADIANS);
    float zinc = length * zc;
    float xinc = length * zs * ac;
    float yinc = length * zs * as;

    float[][] locs = {{xstart - LENGTH_SCALE * xinc,
                       xstart + LENGTH_SCALE * xinc},
                      {ystart - LENGTH_SCALE * yinc,
                       ystart + LENGTH_SCALE * yinc},
                      {zstart - LENGTH_SCALE * zinc,
                       zstart + LENGTH_SCALE * zinc}};
    // construct Field for fit
    Gridded3DSet set = new Gridded3DSet(xyz, locs, 2);
    FlatField field =
      new FlatField(trackFunctionType, set);
    float[][] values = {{time, time}, {energy, energy}};

    try {
      field.setSamples(values, false);
    } catch (RemoteException re) {
      re.printStackTrace();
      return null;
    }

    return field;
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

  public synchronized DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    FileInputStream fileStream = new FileInputStream(id);
    return open(fileStream);
  }

  public synchronized DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    InputStream inputStream = url.openStream();
    return open(inputStream);
  }

  private synchronized DataImpl open(InputStream is)
    throws BadFormException, VisADException, IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));

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

    BaseCache.clearAll();

    Module[] om = null;

    ArrayList emEvents = new ArrayList();

    // initialize tracks and hits to empty
    ArrayList emTracks = new ArrayList();
    ArrayList emHits = new ArrayList();

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
        if (om != null) {
          System.err.println("Warning: Multiple ARRAY lines found," +
                             " some EM data will be lost");
        }

        int nmodules = readArrayLine(line, tok);

        om = new Module[nmodules];

        // initialize modules
        for (int i = 0; i < nmodules; i++) {
          om[i] = null;
        }

        continue;
      }

      if (keyword.equals("om")) {
        Module module = readOMLine(line, tok, om);
        if (module == null) {
          continue;
        }

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
        continue;
      }

      if (keyword.equals("em")) {
        if (emTracks.size() != 0 || emHits.size() != 0) {
          System.err.println("Warning: EM data discarded!");
          emTracks.clear();
          emHits.clear();
        }

        startEvent(line, tok);

        continue;
      }

      // read TR and HT records
      if (keyword.equals("tr")) {
        FlatField track = readTrack(line, tok);
        if (track != null) {
          emTracks.add(track);
        }

        continue;
      }

      if (keyword.equals("fit")) {
        FlatField fit = readFit(line, tok);
        if (fit != null) {
          emTracks.add(fit);
        }

        continue;
      }

      if (keyword.equals("ht")) {
        RealTuple hit = readHit(line, tok, om);
        if (hit != null) {
          emHits.add(hit);
        }

        continue;
      }

      if (keyword.equals("ee")) {
        Tuple event = finishEvent(emTracks, emHits);
        if (event != null) {
          emEvents.add(event);
        }

        continue;
      }
    }

    try { br.close(); } catch (IOException ioe) { }

    return buildData(emEvents, om);
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
      value = ((DoubleCache )lastCache.get(tokenName)).getValue();
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
      value = ((FloatCache )lastCache.get(tokenName)).getValue();
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
      value = ((IntCache )lastCache.get(tokenName)).getValue();
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

  private final FlatField readFit(String line, StringTokenizer tok)
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

    return makeField(xstart, ystart, zstart, zenith, azimuth, length,
                     energy, time, 10000.f);
  }

  private final RealTuple readHit(String line, StringTokenizer tok,
                                  Module[] om)
    throws VisADException
  {
    String chanStr = tok.nextToken();
    int number = parseChannel("htNum", chanStr);
    if (number < 0) {
      System.err.println("Warning: Ignoring HIT for secondary channel \"" +
                         chanStr + "\"");
      return null;
    }

    // convert module index (1-based) to array index (0-based)
    number--;

    float amplitude, let, tot;
    try {
      amplitude = parseFloat("htAmp", tok.nextToken());

      // skip next two tokens
      tok.nextToken();
      tok.nextToken();

      let = parseFloat("htLet", tok.nextToken());
      tot = parseFloat("htTot", tok.nextToken());
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad HIT line \"" + line + "\": " +
                                 e.getMessage());
    }

    RealTuple rt;
    if (om[number] == null) {
      System.err.println("Warning: Module not found for HIT line \"" +
                         line + "\"");
      rt = null;
    } else {
      double[] values = {om[number].getX(), om[number].getY(),
                         om[number].getZ(),
                         amplitude, let, tot};

      // construct Tuple for hit
      try {
        rt = new RealTuple(hitType, values);
      } catch (RemoteException re) {
        re.printStackTrace();
        rt = null;
      }
    }

    return rt;
  }

  private final Module readOMLine(String line, StringTokenizer tok, Module[] om)
    throws BadFormException
  {
    String numStr = tok.nextToken();

    int number;
    try {
      number = parseInt("omNum", numStr) - 1;
    } catch(NumberFormatException e) {
      throw new BadFormException("unparseable module number \"" + numStr +
                                 "\" in \"" + line + "\"");
    }

    if (number < 0 || number >= om.length) {
      throw new BadFormException("bad module number \"" + numStr +
                                 "\" in \"" + line + "\"");
    } else if (om[number] != null) {
      System.err.println("Warning: Ignoring duplicate module #" + number +
                         " in \"" + line + "\"");
      return null;
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

    om[number] = new Module(x, y, z, string, stringOrder);

    return om[number];
  }

  private final FlatField readTrack(String line, StringTokenizer tok)
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

    return makeField(xstart, ystart, zstart, zenith, azimuth, length,
                     energy, time, 1000.f);
  }

  public synchronized void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new BadFormException("F2000Form.save");
  }

  private final void startEvent(String line, StringTokenizer tok)
    throws BadFormException
  {
    // assemble EM event
/* XXX don't do anything, since it's all thrown away
    try {
      int number = parseInt("emNum", tok.nextToken());
      int year = parseInt("emYear", tok.nextToken());
      int day = parseInt("emDay", tok.nextToken());
      double em_time = parseDouble("emTime", tok.nextToken());
      // time shift in nsec of all times in event
      double em_time_shift = parseDouble("emTimeShift", tok.nextToken()) * 0.000000001;
    } catch(NumberFormatException e) {
      throw new BadFormException("Bad EM line \"" + line + "\": " +
                                 e.getMessage());
    }
*/
  }
}
