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

import java.util.StringTokenizer;
import visad.*;

/** A Bio-Rad note object. */
public class BioRadNote {

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
  private static final int NOTE_TYPE_COLLECT  = 6;

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
    "", "LIVE", "FILE1", "NUMBER", "USER", "LINE", "COLLECT", "FILE2",
    "SCALEBAR", "MERGE", "THRUVIEW", "ARROW", "VARIABLE", "STRUCTURE"
  };


  // Note status types

  /** Whether note is displayed on all images. */
  private static final int NOTE_STATUS_ALL = 0x0100;

  /** Note is currently displayed. */
  private static final int NOTE_STATUS_DISPLAY = 0x0200;
  
  /** Note has been positioned by the user. */
  private static final int NOTE_STATUS_POSITION = 0x0400;


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
  }

  /** Converts this Bio-Rad note into a VisAD Data object. */
  public DataImpl getNoteAsData() {
    if (type == NOTE_TYPE_SCALEBAR) {
      // SCALEBAR = length angle
      StringTokenizer st = new StringTokenizer(text);
      if (st.countTokens() != 4) {
        warn();
        return null;
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
        return null;
      }
      // CTR: TODO: deal with extracted SCALEBAR information
    }
    else if (type == NOTE_TYPE_THRUVIEW) {
      // ignore (Reserved for Bio-Rad Image Processing Software ThruView)
      return null;
    }
    else if (type == NOTE_TYPE_ARROW) {
      // ARROW = lx ly angle fill_type
      StringTokenizer st = new StringTokenizer(text);
      if (st.countTokens() != 6) {
        warn();
        return null;
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
        return null;
      }
      // CTR: TODO: deal with extracted ARROW information
    }
    else if (type == NOTE_TYPE_VARIABLE) {
      // VARIABLE = value
      int eq = text.indexOf("=");
      if (eq < 0) {
        warn();
        return null;
      }
      String v = text.substring(0, eq).trim();
      String value = text.substring(eq + 1).trim();
      if (v.equals("RAMP_GAMMA1")) {
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
        // CTR: TODO: extract units
      }
      else if (v.equals("AXIS_3")) {
        // vertical axis information
        // CTR: TODO: extract units
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
        warn();
        return null;
      }
    }
    else if (type == NOTE_TYPE_STRUCTURE) {
      // CTR: TODO
    }
    else {
      // One of LIVE, FILE1, FILE2, NUMBER, USER, LINE, COLLECT, or MERGE
      // CTR: TODO: see Test45, but need Units first
    }
    return null; // CTR: TEMP
  }

  private void warn() {
    System.err.println(
      "Warning: invalid " + noteNames[type] + " note: \"" + text + "\"");
  }

}
