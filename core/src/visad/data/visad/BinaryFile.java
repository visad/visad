/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.visad;

/**
 * Constant values used by both
 * {@link visad.data.visad.BinaryReader BinaryReader}
 * and
 * {@link visad.data.visad.BinaryWriter BinaryWriter}<br>
 * <br>
 * <tt>MAGIC_STR</tt> and <tt>FORMAT_VERSION</tt> are used
 * to mark the file as a VisAD binary file.<br>
 * <tt>OBJ_</tt> constants indicate the type of the next
 * object in the file.<br>
 * <tt>FLD_</tt> constants indicate the type of the next
 * field for the current object in the file.<br>
 * <tt>MATH_</tt> constants indicate the type of <tt>FLD_MATH</tt>
 * objects.<br>
 * <tt>DATA_</tt> constants indicate the type of <tt>FLD_DATA</tt>
 * objects.
 */
public interface BinaryFile
{
  String MAGIC_STR = "VisADBin";
  int FORMAT_VERSION = 1;

  byte OBJ_COORDSYS = 1;
  byte OBJ_DATA = 2;
  byte OBJ_DATA_SERIAL = 3;
  byte OBJ_ERROR = 4;
  byte OBJ_MATH = 5;
  byte OBJ_MATH_SERIAL = 6;
  byte OBJ_UNIT = 7;

  byte FLD_FIRSTS = 1;
  byte FLD_LASTS = 2;
  byte FLD_LENGTHS = 3;
  byte FLD_FLOAT_LIST = 4;
  byte FLD_SAMPLE = 5;
  byte FLD_FLOAT_SAMPLES = 6;
  byte FLD_DOUBLE_SAMPLES = 7;
  byte FLD_DATA_SAMPLES = 8;
  byte FLD_REAL_SAMPLES = 9;
  byte FLD_TRIVIAL_SAMPLES = 10;
  byte FLD_SET_SAMPLES = 11;
  byte FLD_SET = 12;
  byte FLD_LINEAR_SETS = 13;
  byte FLD_INTEGER_SETS = 14;
  byte FLD_SET_LIST = 15;

  byte FLD_COORDSYS_SERIAL = 20;
  byte FLD_DELAUNAY_SERIAL = 21;

  byte FLD_INDEX_UNIT = 30;
  byte FLD_INDEX_ERROR = 31;
  byte FLD_INDEX_COORDSYS = 32;

  byte FLD_INDEX_UNITS = 40;
  byte FLD_INDEX_ERRORS = 41;

  byte FLD_RANGE_COORDSYSES = 50;

  byte FLD_DELAUNAY = 60;
  byte FLD_DELAUNAY_TRI = 61;
  byte FLD_DELAUNAY_VERTICES = 62;
  byte FLD_DELAUNAY_WALK = 63;
  byte FLD_DELAUNAY_EDGES = 64;
  byte FLD_DELAUNAY_NUM_EDGES = 65;

  byte FLD_SET_FOLLOWS_TYPE = 70;

  byte FLD_END = 80;

  byte MATH_FUNCTION = 1;
  byte MATH_REAL = 2;
  byte MATH_REAL_TUPLE = 3;
  byte MATH_SET = 4;
  byte MATH_TEXT = 5;
  byte MATH_TUPLE = 6;
  byte MATH_QUANTITY = 7;
  // byte MATH_DISPLAY_TUPLE = 8;
  // byte MATH_REAL_VECTOR = 9;
  // byte MATH_EARTH_VECTOR = 10;
  // byte MATH_GRID_VECTOR = 11;
  // byte MATH_DISPLAY_REAL = 12;

  // byte DATA_SCALAR = 1;
  byte DATA_TEXT = 2;
  byte DATA_REAL = 3;

  byte DATA_TUPLE = 10;
  byte DATA_REAL_TUPLE = 11;

  byte DATA_FIELD = 20;
  byte DATA_FLAT_FIELD = 21;

  // byte DATA_SET = 30;
  // byte DATA_SIMPLE_SET = 31;
  byte DATA_DOUBLE_SET = 32;
  byte DATA_FLOAT_SET = 33;
  byte DATA_LIST1D_SET = 34;
  // byte DATA_SAMPLED_SET = 35;
  byte DATA_SINGLETON_SET = 36;
  byte DATA_UNION_SET = 37;
  byte DATA_PRODUCT_SET = 38;
  byte DATA_IRREGULAR_SET = 39;
  byte DATA_IRREGULAR_1D_SET = 40;
  byte DATA_IRREGULAR_2D_SET = 41;
  byte DATA_IRREGULAR_3D_SET = 42;
  byte DATA_GRIDDED_SET = 43;
  byte DATA_GRIDDED_1D_SET = 44;
  byte DATA_GRIDDED_2D_SET = 45;
  byte DATA_GRIDDED_3D_SET = 46;
  byte DATA_GRIDDED_1D_DOUBLE_SET = 47;
  byte DATA_GRIDDED_2D_DOUBLE_SET = 48;
  byte DATA_GRIDDED_3D_DOUBLE_SET = 49;
  byte DATA_LINEAR_1D_SET = 50;
  byte DATA_LINEAR_2D_SET = 51;
  byte DATA_LINEAR_3D_SET = 52;
  byte DATA_LINEAR_ND_SET = 53;
  byte DATA_LINEAR_LATLON_SET = 54;
  byte DATA_INTEGER_1D_SET = 55;
  byte DATA_INTEGER_2D_SET = 56;
  byte DATA_INTEGER_3D_SET = 57;
  byte DATA_INTEGER_ND_SET = 58;

  byte DATA_NONE = 60;

  boolean DEBUG_RD_CSYS = false;
  boolean DEBUG_RD_DATA = false;
  boolean DEBUG_RD_DATA_DETAIL = false;
  boolean DEBUG_RD_ERRE = false;
  boolean DEBUG_RD_MATH = false;
  boolean DEBUG_RD_STR = false;
  boolean DEBUG_RD_TIME = false;
  boolean DEBUG_RD_UNIT = false;

  boolean DEBUG_WR_CSYS = false;
  boolean DEBUG_WR_DATA = false;
  boolean DEBUG_WR_DATA_DETAIL = false;
  boolean DEBUG_WR_ERRE = false;
  boolean DEBUG_WR_MATH = false;
  boolean DEBUG_WR_STR = false;
  boolean DEBUG_WR_TIME = false;
  boolean DEBUG_WR_UNIT = false;
}
