//
// BioRadNote.java
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

package visad.data.biorad;

import java.rmi.RemoteException;
import java.util.StringTokenizer;
import visad.*;

/** A Bio-Rad note object. */
public class BioRadNote {

  /** Micron unit. */
  public static final Unit micron = SI.meter.pow(-6);


  // Return types for analyze()

  /** Indicates that a VisAD Data object was computed. */
  public static final int METADATA = 0;

  /** Indicates that a Unit for the horizontal axis was computed. */
  public static final int HORIZ_UNIT = 1;

  /** Indicates that a Unit for the vertical axis was computed. */
  public static final int VERT_UNIT = 2;

  /** Indicates that this note is invalid. */
  public static final int INVALID_NOTE = -1;


  // Note types

  /** Information about live collection. */
  private static final int NOTE_TYPE_LIVE = 1;

  /** Note from image #1. */
  private static final int NOTE_TYPE_FILE1 = 2;

  /** Number in multiple image file. */
  private static final int NOTE_TYPE_NUMBER = 3;

  /** User notes generated notes. */
  private static final int NOTE_TYPE_USER = 4;

  /** Line mode info. */
  private static final int NOTE_TYPE_LINE = 5;

  /** Collect mode info. */
  private static final int NOTE_TYPE_COLLECT = 6;

  /** Note from image #2. */
  private static final int NOTE_TYPE_FILE2 = 7;

  /** Scale bar info. */
  private static final int NOTE_TYPE_SCALEBAR = 8;

  /** Merge info. */
  private static final int NOTE_TYPE_MERGE = 9;

  /** Thruview info. */
  private static final int NOTE_TYPE_THRUVIEW = 10;

  /** Arrow info. */
  private static final int NOTE_TYPE_ARROW = 11;

  /** Internal variable. */
  private static final int NOTE_TYPE_VARIABLE = 20;

  /** Again internal variable, except held as a structure. */
  private static final int NOTE_TYPE_STRUCTURE = 21;

  /** List of note types. */
  private static final String[] noteNames = {
    "0", "LIVE", "FILE1", "NUMBER", "USER", "LINE", "COLLECT", "FILE2",
    "SCALEBAR", "MERGE", "THRUVIEW", "ARROW", "12", "13", "14", "15",
    "16", "17", "18", "19", "VARIABLE", "STRUCTURE"
  };


  // Note status types

  /** Whether note is displayed on all images. */
  private static final int NOTE_STATUS_ALL = 0x0100;

  /** Note is currently displayed. */
  private static final int NOTE_STATUS_DISPLAY = 0x0200;

  /** Note has been positioned by the user. */
  private static final int NOTE_STATUS_POSITION = 0x0400;


  // Note axis types

  /** Distance im microns. */
  private static final int axt_D = 1;

  /** Time in sec. */
  private static final int axt_T = 2;

  /** Angle in degrees. */
  private static final int axt_A = 3;

  /** Intensity in grey levels. */
  private static final int axt_I = 4;

  /** 4-bit merged image. */
  private static final int axt_M4 = 5;

  /** Ratio. */
  private static final int axt_R = 6;

  /** Log Ratio. */
  private static final int axt_LR = 7;

  /** Product. */
  private static final int axt_P = 8;

  /** Calibrated concentration/pH. */
  private static final int axt_C = 9;

  /** Intensity in photons/sec. */
  private static final int axt_PHOTON = 10;

  /** RGB type (mixer/channel/colour). */
  private static final int axt_RGB = 11;

  /** SEQ type (eg "experiments" of a "method". */
  private static final int axt_SEQ = 12;

  /** 6th level of axis "nesting", on top of RGB &amp; SEQ. */
  private static final int axt_6D = 13;

  /** Time Course axis */
  private static final int axt_TC = 14;

  /** Intensity sigmoid calibrated concentration/pH. */
  private static final int axt_S = 15;

  /** Intensity log sigmoid calibrated concentration/pH. */
  private static final int axt_LS = 16;

  /** Mask for axis TYPE. */
  private static final int axt_MASK = 0xFF;

  /** Axis is XY, needs updating by LENS etc. */
  private static final int axt_XY = 0x100;

  /** Axis is word, only corresponds to axis[0]. */
  private static final int axt_WORD = 0x200;


  // Fields

  /** Level of this note. */
  int level;

  /** Image number for the display of this note. */
  int num;

  /** Status flag for this note. */
  int status;

  /** Type code for this note. */
  int type;

  /** X coordinate for the note. */
  int x;

  /** Y coordinate for the note. */
  int y;

  /** 80 characters of information. */
  String text;

  /** Metadata object constructed from this note, if any. */
  Data metadata;

  /**
   * If note has unit information, Unit measurement constructed from this note.
   * If note contains other Metadata requiring a Unit, this field will be used
   * in the construction of that metadata.
   */
  Unit unit;


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
    if (BioRadForm.DEBUG) {
      System.out.println("Note: level=" + level + ", num=" + num +
        ", status=" + status + ", type=" + noteNames[type] +
        ", x=" + x + ", y=" + y + ", text=" + text);
    }
  }

  static final TupleType noteTuple = makeNoteTuple();
  
  private static TupleType makeNoteTuple() {
    try {
      return new TupleType(new MathType[] {
        RealType.getRealType("level"),
        RealType.getRealType("num"),
        RealType.getRealType("status"),
        RealType.getRealType("type"),
        RealType.getRealType("x"),
        RealType.getRealType("y"),
        TextType.getTextType("text")
      });
    }
    catch (VisADException exc) { }
    return null;
  }

  /** Gets a simple VisAD Data object representing this note. */
  public DataImpl getNoteData() {
    DataImpl[] d = new DataImpl[] {
      new Real(level), new Real(num), new Real(status), new Real(type),
      new Real(x), new Real(y), new Text(text)
    };
    try {
      return new Tuple(noteTuple, d);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
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
      catch (NumberFormatException exc) { }
      if (!v.equals("SCALEBAR") || !eq.equals("=") ||
        length < 0 || angle < 0)
      {
        warn();
        return INVALID_NOTE;
      }
      // CTR: TODO: deal with extracted SCALEBAR information
    }
    else if (type == NOTE_TYPE_THRUVIEW) {
      // ignore (Reserved for Bio-Rad Image Processing Software ThruView)
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
      catch (NumberFormatException exc) { }
      String fill_type = st.nextToken(); // either "Fill" or "Outline"
      if (!v.equals("ARROW") || !eq.equals("=") ||
        lx < 0 || ly < 0 || angle < 0 ||
        !(fill_type.equals("Fill") || fill_type.equals("Outline")))
      {
        warn();
        return INVALID_NOTE;
      }
      // CTR: TODO: deal with extracted ARROW information
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
      if (v.equals("SCALE_FACTOR")) {
        // calibration scale factor for microscope system that acquired image
      }
      else if (v.equals("LENS_MAGNIFICATION")) {
        // floating point number of objective lens used to acquire image
      }
      else if (v.equals("RAMP_GAMMA1")) {
        // gamma factor applied to the LUT used to display image1
      }
      else if (v.equals("RAMP_GAMMA2")) {
        // gamma factor applied to the LUT used to display image2
      }
      else if (v.equals("RAMP_GAMMA3")) {
        // gamma factor applied to the LUT used to display image3
      }
      else if (v.equals("RAMP1_MIN")) {
        // ramp values used for brightness and contrast values for image1
      }
      else if (v.equals("RAMP2_MIN")) {
        // ramp values used for brightness and contrast values for image2
      }
      else if (v.equals("RAMP3_MIN")) {
        // ramp values used for brightness and contrast values for image3
      }
      else if (v.equals("RAMP1_MAX")) {
        // ramp values used for brightness and contrast values for image1
      }
      else if (v.equals("RAMP2_MAX")) {
        // ramp values used for brightness and contrast values for image2
      }
      else if (v.equals("RAMP3_MAX")) {
        // ramp values used for brightness and contrast values for image3
      }
      else if (v.equals("PIC_FF_VERSION")) {
        // version of the file
      }
      else if (v.equals("Z_CORRECT_FACTOR")) {
        // correction factor for z depth
      }
      else if (v.equals("AXIS_0")) {
        // uncalibrated intensity axis information for upper bytes
      }
      else if (v.equals("AXIS_1")) {
        // grey-level lower byte axis information
      }
      else if (v.equals("AXIS_2")) {
        // horizontal axis information
        StringTokenizer st = new StringTokenizer(value);
        if (st.countTokens() != 4) {
          warn("AXIS_2");
          return INVALID_NOTE;
        }
        int type = -1; // axt_D for distance in *m
        int origin = -1; // position of left hand edge of image in *m
        int inc = -1; // pixel step size (increment) in *m
        try {
          type = Integer.parseInt(st.nextToken());
          origin = Integer.parseInt(st.nextToken());
          inc = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException exc) { }
        if (type < 0 || origin < 0 || inc < 0) {
          warn("AXIS_2");
          return INVALID_NOTE;
        }
        // get text string describing the image X axis calibration
        String label = st.nextToken();

        // compute axis unit
        if (type != axt_D) {
          warn("AXIS_2");
          return INVALID_NOTE;
        }
        try {
          unit = micron.scale(inc).shift(origin);
          return HORIZ_UNIT;
        }
        catch (UnitException exc) { }
        return INVALID_NOTE;
      }
      else if (v.equals("AXIS_3")) {
        // vertical axis information
        // CTR: TODO: extract units
        StringTokenizer st = new StringTokenizer(value);
        if (st.countTokens() != 4) {
          warn("AXIS_3");
          return INVALID_NOTE;
        }
        int type = -1; // axt_D for distance in *m (or axt_T for time in s)
        int origin = -1; // position of bottom edge of image in *m (or s)
        int inc = -1; // pixel step size (increment) in *m (or s)
        try {
          type = Integer.parseInt(st.nextToken());
          origin = Integer.parseInt(st.nextToken());
          inc = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException exc) { }
        if (type < 0 || origin < 0 || inc < 0) {
          warn("AXIS_3");
          return INVALID_NOTE;
        }
        // get text string describing the image X axis calibration
        String label = st.nextToken();

        // compute axis unit
        if (type == axt_D) {
          try {
            unit = micron.scale(inc).shift(origin);
            return VERT_UNIT;
          }
          catch (UnitException exc) { }
        }
        else if (type == axt_T) {
          try {
            unit = SI.second.scale(inc).shift(origin);
            return VERT_UNIT;
          }
          catch (UnitException exc) { }
        }
        warn();
        return INVALID_NOTE;
      }
      else if (v.equals("AXIS_4")) {
        // image1 information
      }
      else if (v.equals("AXIS_5")) {
        // image2 information
      }
      else if (v.equals("AXIS_6")) {
        // image3 information
      }
      else if (v.equals("AXIS_7")) {
        // image4 information
      }
      else if (v.equals("AXIS_8")) {
        // image5 information
      }
      else if (v.equals("AXIS_9")) {
        // mixer information for z-series
      }
      else if (v.equals("AXIS_21")) {
        // calibrated intensity information
      }
      else {
        warn(v);
        return INVALID_NOTE;
      }
    }
    else if (type == NOTE_TYPE_STRUCTURE) {
      // CTR: TODO
    }
    else {
      // One of LIVE, FILE1, FILE2, NUMBER, USER, LINE, COLLECT, or MERGE
      // CTR: TODO: see Test45, but need Units first
    }
    return METADATA; // CTR: TEMP
  }

  /** Prints a warning about this note to the standard error stream. */
  private void warn() {
    warn(null);
  }

  /** Prints a warning about this note to the standard error stream. */
  private void warn(String subType) {
    System.err.print("Warning: invalid " + noteNames[type] + " ");
    if (subType != null) System.err.print("(" + subType + ") ");
    System.err.println("note: \"" + text + "\"");
  }

}
