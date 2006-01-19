//
// BioRadNote.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.bio;

import java.io.*;
import java.rmi.RemoteException;
import java.util.StringTokenizer;
import visad.*;

/** A Bio-Rad note object. */
public class BioRadNote {

  /** Distance unit. */
  public static final Unit MICRON = SI.meter.pow(-6);

  /** Time unit. */
  public static final Unit SECOND = SI.second;


  // Return types for analyze()

  /** Indicates that no information could be extracted from this note. */
  public static final int NO_INFORMATION = 0;

  /** Indicates that a VisAD Data object was computed. */
  public static final int METADATA = 1;

  /** Indicates that a Unit for the horizontal axis was computed. */
  public static final int HORIZ_UNIT = 2;

  /** Indicates that a Unit for the vertical axis was computed. */
  public static final int VERT_UNIT = 3;

  /** Indicates that this note is invalid. */
  public static final int INVALID_NOTE = -1;


  // Note types

  /** Information about live collection. */
  public static final int NOTE_TYPE_LIVE = 1;

  /** Note from image #1. */
  public static final int NOTE_TYPE_FILE1 = 2;

  /** Number in multiple image file. */
  public static final int NOTE_TYPE_NUMBER = 3;

  /** User notes generated notes. */
  public static final int NOTE_TYPE_USER = 4;

  /** Line mode info. */
  public static final int NOTE_TYPE_LINE = 5;

  /** Collect mode info. */
  public static final int NOTE_TYPE_COLLECT = 6;

  /** Note from image #2. */
  public static final int NOTE_TYPE_FILE2 = 7;

  /** Scale bar info. */
  public static final int NOTE_TYPE_SCALEBAR = 8;

  /** Merge info. */
  public static final int NOTE_TYPE_MERGE = 9;

  /** Thruview info. */
  public static final int NOTE_TYPE_THRUVIEW = 10;

  /** Arrow info. */
  public static final int NOTE_TYPE_ARROW = 11;

  /** Internal variable. */
  public static final int NOTE_TYPE_VARIABLE = 20;

  /** Again internal variable, except held as a structure. */
  public static final int NOTE_TYPE_STRUCTURE = 21;

  /** List of note types. */
  public static final String[] NOTE_NAMES = {
    "0", "LIVE", "FILE1", "NUMBER", "USER", "LINE", "COLLECT", "FILE2",
    "SCALEBAR", "MERGE", "THRUVIEW", "ARROW", "12", "13", "14", "15",
    "16", "17", "18", "19", "VARIABLE", "STRUCTURE"
  };


  // Note status types

  /** Whether note is displayed on all images. */
  public static final int NOTE_STATUS_ALL = 0x0100;

  /** Note is currently displayed. */
  public static final int NOTE_STATUS_DISPLAY = 0x0200;

  /** Note has been positioned by the user. */
  public static final int NOTE_STATUS_POSITION = 0x0400;


  // Note axis types

  /** Distance im microns. */
  public static final int AXT_D = 1;

  /** Time in sec. */
  public static final int AXT_T = 2;

  /** Angle in degrees. */
  public static final int AXT_A = 3;

  /** Intensity in grey levels. */
  public static final int AXT_I = 4;

  /** 4-bit merged image. */
  public static final int AXT_M4 = 5;

  /** Ratio. */
  public static final int AXT_R = 6;

  /** Log Ratio. */
  public static final int AXT_LR = 7;

  /** Product. */
  public static final int AXT_P = 8;

  /** Calibrated concentration/pH. */
  public static final int AXT_C = 9;

  /** Intensity in photons/sec. */
  public static final int AXT_PHOTON = 10;

  /** RGB type (mixer/channel/colour). */
  public static final int AXT_RGB = 11;

  /** SEQ type (eg "experiments" of a "method". */
  public static final int AXT_SEQ = 12;

  /** 6th level of axis "nesting", on top of RGB &amp; SEQ. */
  public static final int AXT_6D = 13;

  /** Time Course axis */
  public static final int AXT_TC = 14;

  /** Intensity sigmoid calibrated concentration/pH. */
  public static final int AXT_S = 15;

  /** Intensity log sigmoid calibrated concentration/pH. */
  public static final int AXT_LS = 16;

  /** Mask for axis TYPE. */
  public static final int AXT_MASK = 0xFF;

  /** Axis is XY, needs updating by LENS etc. */
  public static final int AXT_XY = 0x100;

  /** Axis is word, only corresponds to axis[0]. */
  public static final int AXT_WORD = 0x200;


  // MathTypes

  /** RealType for level note element. */
  public static final RealType RT_LEVEL =
    RealType.getRealType("biorad_level");

  /** RealType for num note element. */
  public static final RealType RT_NUM = RealType.getRealType("biorad_num");

  /** RealType for status note element. */
  public static final RealType RT_STATUS =
    RealType.getRealType("biorad_status");

  /** RealType for type note element. */
  public static final RealType RT_TYPE = RealType.getRealType("biorad_type");

  /** RealType for x note element. */
  public static final RealType RT_X = RealType.getRealType("biorad_x");

  /** RealType for y note element. */
  public static final RealType RT_Y = RealType.getRealType("biorad_y");

  /** TextType for text note element. */
  public static final TextType TT_TEXT = TextType.getTextType("biorad_text");

  /** MathType of a BioRad note. */
  public static final TupleType NOTE_TUPLE = makeNoteTuple();

  /** Creates BioRad note MathType. */
  protected static TupleType makeNoteTuple() {
    try {
      if (RT_LEVEL == null) System.out.println("level is null");
      if (RT_NUM == null) System.out.println("num is null");
      if (RT_STATUS == null) System.out.println("status is null");
      if (RT_TYPE == null) System.out.println("type is null");
      if (RT_X == null) System.out.println("x is null");
      if (RT_Y == null) System.out.println("y is null");
      if (TT_TEXT == null) System.out.println("text is null");
      return new TupleType(new MathType[] {
        RT_LEVEL, RT_NUM, RT_STATUS, RT_TYPE, RT_X, RT_Y, TT_TEXT
      });
    }
    catch (VisADException exc) {
      if (BioRadForm.DEBUG) exc.printStackTrace();
    }
    return null;
  }


  // Fields

  /** Level of this note. */
  protected int level;

  /** Image number for the display of this note. */
  protected int num;

  /** Status flag for this note. */
  protected int status;

  /** Type code for this note. */
  protected int type;

  /** X coordinate for the note. */
  protected int x;

  /** Y coordinate for the note. */
  protected int y;

  /** 80 characters of information. */
  protected String text;

  /** Metadata object constructed from this note, if any. */
  protected Data metadata;

  /** The origin (starting location) of the data (in microns or seconds). */
  protected double origin;

  /** The step size between samples (in microns or seconds). */
  protected double step;

  /** True if seconds, false if microns. */
  protected boolean time;


  /** Constructs a new Bio-Rad note object. */
  public BioRadNote(int level, int num,
    int status, int type, int x, int y, String text)
  {
    this.level = level;
    this.num = num;
    this.status = status;
    this.type = type;
    this.x = x;
    this.y = y;
    this.text = text;
    if (BioRadForm.DEBUG && BioRadForm.DEBUG_LEVEL >= 2) {
      System.err.println(toString());
    }
  }

  /** Gets a simple VisAD Data object representing this note. */
  public DataImpl getNoteData() {
    try {
      DataImpl[] d = new DataImpl[] {
        new Real(RT_LEVEL, level), new Real(RT_NUM, num),
        new Real(RT_STATUS, status), new Real(RT_TYPE, type),
        new Real(RT_X, x), new Real(RT_Y, y), new Text(TT_TEXT, text)
      };
      return new Tuple(NOTE_TUPLE, d, false);
    }
    catch (VisADException exc) {
      if (BioRadForm.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BioRadForm.DEBUG) exc.printStackTrace();
    }
    return null;
  }

  /** Whether this note has information about pixel spacing in microns. */
  public boolean hasUnitInfo() {
    if (type == NOTE_TYPE_VARIABLE) {
      // VARIABLE = value
      int sp = text.indexOf(" ");
      if (sp >= 0) {
        String v = text.substring(0, sp).trim();
        String value = text.substring(sp + 1).trim();
        if (v.equals("AXIS_2") || v.equals("AXIS_3")) return true;
      }
    }
    return false;
  }

  /** Extracts information from this Bio-Rad note. */
  public int analyze() {
    if (type == NOTE_TYPE_SCALEBAR) {
      // SCALEBAR = length angle
      StringTokenizer st = new StringTokenizer(text);
      if (st.countTokens() != 4) {
        warn();
        return INVALID_NOTE;
      }
      String v = st.nextToken();
      String eq = st.nextToken();
      int length = -1; // length of the scalebar in microns
      int angle = -1; // angle of the scalebar in degrees
      try {
        length = Integer.parseInt(st.nextToken());
        angle = Integer.parseInt(st.nextToken());
      }
      catch (NumberFormatException exc) {
        if (BioRadForm.DEBUG) exc.printStackTrace();
      }
      if (!v.equals("SCALEBAR") || !eq.equals("=") ||
        length < 0 || angle < 0)
      {
        warn();
        return INVALID_NOTE;
      }
      // CTR TODO - Handle SCALEBAR note
      if (BioRadForm.DEBUG && BioRadForm.DEBUG_LEVEL >= 1) {
        System.err.println("Warning: ignoring SCALEBAR information");
      }
      return NO_INFORMATION;
    }
    else if (type == NOTE_TYPE_THRUVIEW) {
      // ignore (reserved for Bio-Rad Image Processing Software ThruView)
      return INVALID_NOTE;
    }
    else if (type == NOTE_TYPE_ARROW) {
      // ARROW = lx ly angle fill_type
      StringTokenizer st = new StringTokenizer(text);
      if (st.countTokens() != 6) {
        warn();
        return INVALID_NOTE;
      }
      String v = st.nextToken();
      String eq = st.nextToken();
      int lx = -1; // width of the rectangle surrounding the arrow in pixels
      int ly = -1; // height of the rectangle surrounding the arrow in pixels
      int angle = -1; // angle in degrees of the arrow
      try {
        lx = Integer.parseInt(st.nextToken());
        ly = Integer.parseInt(st.nextToken());
        angle = Integer.parseInt(st.nextToken());
      }
      catch (NumberFormatException exc) {
        if (BioRadForm.DEBUG) exc.printStackTrace();
      }
      String fillType = st.nextToken(); // either "Fill" or "Outline"
      if (!v.equals("ARROW") || !eq.equals("=") ||
        lx < 0 || ly < 0 || angle < 0 ||
        !(fillType.equals("Fill") || fillType.equals("Outline")))
      {
        warn(v);
        return INVALID_NOTE;
      }
      // CTR TODO - Handle ARROW note
      if (BioRadForm.DEBUG && BioRadForm.DEBUG_LEVEL >= 1) {
        System.err.println("Warning: ignoring ARROW information");
      }
      return NO_INFORMATION;
    }
    else if (type == NOTE_TYPE_VARIABLE) {
      // VARIABLE = value
      int sp = text.indexOf(" ");
      if (sp < 0) {
        warn();
        return INVALID_NOTE;
      }
      String v = text.substring(0, sp).trim();
      String value = text.substring(sp + 1).trim();
      if (
        // calibration scale factor for microscope system that acquired image
        v.equals("SCALE_FACTOR") ||
        // floating point number of objective lens used to acquire image
        v.equals("LENS_MAGNIFICATION") ||
        // gamma factor applied to the LUT used to display image1
        v.equals("RAMP_GAMMA1") ||
        // gamma factor applied to the LUT used to display image2
        v.equals("RAMP_GAMMA2") ||
        // gamma factor applied to the LUT used to display image3
        v.equals("RAMP_GAMMA3") ||
        // ramp values used for brightness and contrast values for image1
        v.equals("RAMP1_MIN") ||
        // ramp values used for brightness and contrast values for image2
        v.equals("RAMP2_MIN") ||
        // ramp values used for brightness and contrast values for image3
        v.equals("RAMP3_MIN") ||
        // ramp values used for brightness and contrast values for image1
        v.equals("RAMP1_MAX") ||
        // ramp values used for brightness and contrast values for image2
        v.equals("RAMP2_MAX") ||
        // ramp values used for brightness and contrast values for image3
        v.equals("RAMP3_MAX") ||
        // version of the file
        v.equals("PIC_FF_VERSION") ||
        // correction factor for z depth
        v.equals("Z_CORRECT_FACTOR") ||
        // uncalibrated intensity axis information for upper bytes
        v.equals("AXIS_0") ||
        // grey-level lower byte axis information
        v.equals("AXIS_1") ||
        // image1 information
        v.equals("AXIS_4") ||
        // image2 information
        v.equals("AXIS_5") ||
        // image3 information
        v.equals("AXIS_6") ||
        // image4 information
        v.equals("AXIS_7") ||
        // image5 information
        v.equals("AXIS_8") ||
        // mixer information for z-series
        v.equals("AXIS_9") ||
        // calibrated intensity information
        v.equals("AXIS_21"))
      {
        double dv;
        try {
          dv = Double.parseDouble(value);
        }
        catch (NumberFormatException exc) {
          warn();
          return INVALID_NOTE;
        }
        metadata = new Real(RealType.getRealType(v), dv);
        return METADATA;
      }
      else if (v.equals("AXIS_2")) {
        // horizontal axis information
        StringTokenizer st = new StringTokenizer(value);
        if (st.countTokens() != 4) {
          warn(v);
          return INVALID_NOTE;
        }
        int ntype = -1; // AXT_D for distance in *m
        int norigin = -1; // position of left hand edge of image in *m
        int inc = -1; // pixel step size (increment) in *m
        try {
          ntype = Integer.parseInt(st.nextToken());
          norigin = Integer.parseInt(st.nextToken());
          inc = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException exc) {
          if (BioRadForm.DEBUG) exc.printStackTrace();
        }
        if (ntype < 0 || norigin < 0 || inc < 0) {
          warn(v);
          return INVALID_NOTE;
        }
        // get text string describing the image X axis calibration
        String label = st.nextToken();

        // compute axis unit
        if (ntype != AXT_D) {
          warn(v);
          return INVALID_NOTE;
        }
        origin = norigin;
        step = inc;
        time = false;
        return HORIZ_UNIT;
      }
      else if (v.equals("AXIS_3")) {
        // vertical axis information
        StringTokenizer st = new StringTokenizer(value);
        if (st.countTokens() != 4) {
          warn(v);
          return INVALID_NOTE;
        }
        int ntype = -1; // AXT_D for distance in *m (or AXT_T for time in s)
        int norigin = -1; // position of bottom edge of image in *m (or s)
        int inc = -1; // pixel step size (increment) in *m (or s)
        try {
          ntype = Integer.parseInt(st.nextToken());
          norigin = Integer.parseInt(st.nextToken());
          inc = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException exc) {
          if (BioRadForm.DEBUG) exc.printStackTrace();
        }
        if (ntype < 0 || norigin < 0 || inc < 0) {
          warn(v);
          return INVALID_NOTE;
        }
        // get text string describing the image X axis calibration
        String label = st.nextToken();

        // compute axis unit
        if (type == AXT_D) {
          origin = norigin;
          step = inc;
          time = false;
          return VERT_UNIT;
        }
        else if (type == AXT_T) {
          origin = norigin;
          step = inc;
          time = true;
          return VERT_UNIT;
        }
        warn();
        return INVALID_NOTE;
      }
      else {
        warn(v);
        return INVALID_NOTE;
      }
    }
    else if (type == NOTE_TYPE_STRUCTURE) {
      // CTR TODO - Handle STRUCTURE note
      if (BioRadForm.DEBUG && BioRadForm.DEBUG_LEVEL >= 1) {
        System.err.println("Warning: ignoring STRUCTURE information");
      }
      return NO_INFORMATION;
    }
    else {
      // One of LIVE, FILE1, FILE2, NUMBER, USER, LINE, COLLECT, or MERGE
      // CTR TODO Handle other note types
      if (BioRadForm.DEBUG && BioRadForm.DEBUG_LEVEL >= 1) {
        System.err.println("Warning: ignoring " +
          NOTE_NAMES[type] + " information");
      }
      return NO_INFORMATION;
    }
  }

  /** Gets metadata object computed by analyze method. */
  public Data getMetadata() { return metadata; }

  /**
   * Gets origin value computed by analyze method.
   * Origin (starting location) of the data (in microns or seconds).
   */
  public double getOrigin() { return origin; }

  /**
   * Gets step size value computed by analyze method.
   * Step size between samples (in microns or seconds).
   */
  public double getStep() { return step; }

  /**
   * Gets time flag value computed by analyze method.
   * True if seconds, false if microns.
   */
  public boolean getTime() { return time; }

  /**
   * Outputs this note to the given output stream.
   *
   * @param more Whether another note will be written
   *             to the stream after this one.
   */
  public void write(DataOutputStream out, boolean more) throws IOException {
    BioRadForm.writeShort(out, level);
    BioRadForm.writeInt(out, more ? 1 : 0);
    BioRadForm.writeShort(out, num);
    BioRadForm.writeShort(out, status);
    BioRadForm.writeShort(out, type);
    BioRadForm.writeShort(out, x);
    BioRadForm.writeShort(out, y);
    BioRadForm.writeString(out, text, 80);
  }

  /** Prints a warning about this note to the standard error stream. */
  private void warn() {
    warn(null);
  }

  /** Prints a warning about this note to the standard error stream. */
  private void warn(String subType) {
    if (BioRadForm.DEBUG && BioRadForm.DEBUG_LEVEL >= 1) {
      System.err.print("Warning: invalid " + NOTE_NAMES[type] + " ");
      if (subType != null) System.err.print("(" + subType + ") ");
      System.err.println("note: \"" + text + "\"");
    }
  }

  /** Gets a human-readable string representation of this note. */
  public String toString() {
    StringBuffer sb = new StringBuffer(100);
    sb.append("level=");
    sb.append(level);
    sb.append("; num=");
    sb.append(num);
    sb.append("; status=");
    boolean first = true;
    if ((status & NOTE_STATUS_ALL) != 0) {
      sb.append("NOTE_STATUS_ALL");
      first = false;
    }
    if ((status & NOTE_STATUS_DISPLAY) != 0) {
      if (!first) sb.append("|");
      sb.append("NOTE_STATUS_DISPLAY");
      first = false;
    }
    if ((status & NOTE_STATUS_POSITION) != 0) {
      if (!first) sb.append("|");
      sb.append("NOTE_STATUS_POSITION");
      first = false;
    }
    if (first) {
      sb.append("NONE");
    }
    sb.append("; type=");
    sb.append(NOTE_NAMES[type]);
    sb.append("; x=");
    sb.append(x);
    sb.append("; y=");
    sb.append(y);
    sb.append("; text=");
    sb.append(text == null ? "null" : text.trim());
    return sb.toString();
  }

  /** Converts a VisAD Unit and Linear1DSet to a BioRad note. */
  public static BioRadNote getUnitNote(Unit u,
    Linear1DSet set, boolean xAxis)
  {
    if (u == null || set == null) return null;
    boolean ntime = u.isConvertible(SECOND);
    if (!ntime && !u.isConvertible(MICRON)) return null;

    // extract info from Unit and Set
    int axisType = ntime ? AXT_T : AXT_D;
    double norigin, ninc;
    try {
      norigin = u.toThat(set.getFirst(), ntime ? SECOND : MICRON);
      ninc = u.toThat(set.getStep(), ntime ? SECOND : MICRON);
    }
    catch (UnitException exc) {
      if (BioRadForm.DEBUG) exc.printStackTrace();
      return null;
    }
    String label = "Calibration unknown";

    // fill in note fields
    int nlevel = 1;
    int nnum = 0;
    int nstatus = NOTE_STATUS_ALL | NOTE_STATUS_POSITION;
    int ntype = NOTE_TYPE_VARIABLE;
    int nx = 0;
    int ny = 0;
    String ntext = "AXIS_" + (xAxis ? "2" : "3") + " " +
      axisType + " " + norigin + " " + ninc + " " + label;

    return new BioRadNote(nlevel, nnum, nstatus, ntype, nx, ny, ntext);
  }

}
