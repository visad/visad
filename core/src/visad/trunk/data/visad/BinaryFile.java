package visad.data.visad;

/**
 * Constant values used by both
 * {@link visad.data.visad.BinaryReader BinaryReader}
 * and
 * {@link visad.data.visad.BinaryWriter BinaryWriter}<br>
 * <br>
 * <tt>MAGIC_STR</tt> and <tt>FORMAT_VERSION</tt> are used
 * to mark the file as a VisAD binary file.<br>
 * <tt>FLD_</tt> constants indicate the type of the next
 * object in the file.<br>
 * <tt>MATH_</tt> constants indicate the type of <tt>FLD_MATH</tt>
 * objects.<br>
 * <tt>DATA_</tt> constants indicate the type of <tt>FLD_DATA</tt>
 * objects.
 */
public interface BinaryFile
{
  String MAGIC_STR = "VisADBin";
  int FORMAT_VERSION = 1;

  byte FLD_MATH = 1;
  byte FLD_MATH_SERIAL = 2;

  byte FLD_DATA = 10;
  byte FLD_DATA_SERIAL = 11;

  byte FLD_FIRSTS = 20;
  byte FLD_LASTS = 21;
  byte FLD_LENGTHS = 22;
  byte FLD_FLOAT_LIST = 23;
  byte FLD_SAMPLE = 24;
  byte FLD_FLOAT_SAMPLES = 25;
  byte FLD_DOUBLE_SAMPLES = 26;
  byte FLD_DATA_SAMPLES = 27;
  byte FLD_REAL_SAMPLES = 28;
  byte FLD_TRIVIAL_SAMPLES = 29;
  byte FLD_SET_SAMPLES = 30;
  byte FLD_SET = 31;
  byte FLD_LINEAR_SETS = 32;
  byte FLD_INTEGER_SETS = 33;
  byte FLD_SET_LIST = 34;

  byte FLD_COORDSYS_SERIAL = 40;
  byte FLD_DELAUNAY_SERIAL = 41;

  byte FLD_UNIT = 50;
  byte FLD_ERROR = 51;
  byte FLD_COORDSYS = 52;

  byte FLD_INDEX_UNIT = 60;
  byte FLD_INDEX_ERROR = 61;
  byte FLD_INDEX_COORDSYS = 62;

  byte FLD_INDEX_UNITS = 70;
  byte FLD_INDEX_ERRORS = 71;
  byte FLD_INDEX_COORDSYSES = 72;

  byte FLD_DELAUNAY = 80;
  byte FLD_DELAUNAY_TRI = 81;
  byte FLD_DELAUNAY_VERTICES = 82;
  byte FLD_DELAUNAY_WALK = 83;
  byte FLD_DELAUNAY_EDGES = 84;
  byte FLD_DELAUNAY_NUM_EDGES = 85;

  byte FLD_SET_FOLLOWS_TYPE = 90;

  byte FLD_END = 100;

  byte MATH_FUNCTION = 1;
  byte MATH_REAL = 2;
  byte MATH_REAL_TUPLE = 3;
  byte MATH_SET = 4;
  byte MATH_TEXT = 5;
  byte MATH_TUPLE = 6;
  byte MATH_QUANTITY = 7;
  // byte  MATH_DISPLAY_TUPLE = 8;
  // byte  MATH_REAL_VECTOR = 9;
  // byte  MATH_EARTH_VECTOR = 10;
  // byte  MATH_GRID_VECTOR = 11;
  // byte  MATH_DISPLAY_REAL = 12;

  byte DATA_SCALAR = 1;
  byte DATA_TEXT = 2;
  byte DATA_REAL = 3;

  byte DATA_TUPLE = 10;
  byte DATA_REAL_TUPLE = 11;

  byte DATA_FIELD = 20;
  byte DATA_FLAT_FIELD = 21;

  byte DATA_SET = 30;
  byte DATA_SIMPLE_SET = 31;
  byte DATA_DOUBLE_SET = 32;
  byte DATA_FLOAT_SET = 33;
  byte DATA_LIST1D_SET = 34;
  byte DATA_SAMPLED_SET = 35;
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
}
