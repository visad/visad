/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/

package ncsa.hdf.hdf5lib;

/**
 *  Class HDF5CDataTypes contains C constants and enumerated
 *  types of HDF5 library which are set at runtime, the Java 
 *  constants need to be converted to C constants with the
 *  function call J2C(int) in HDF5Library class. Any constant 
 *  which starts its name with "JH5" need to be converted. Any 
 *  constant which start its name with "H5" has the same value 
 *  as its C constant. For example,
 *  <pre>
 *  h5.H5Tcopy(h5.J2C(HDF5CDatatypes.JH5T_NATIVE_INT)); // convert Java value to C value
 *  </pre>
 *
 *  <B>See also:</b> ncsa.hdf.hdf5lib.HDF5Library
 *  <B>See also:</b> ncsa.hdf.hdf5lib.HDF5Constants
 */
public class HDF5CDataTypes
{
	// Constants need to be mapped into runtime C constants
	// The values are arbitrary but MUST match the values in
        // native/hdf5lib/h5Constants.h
	public static final int JH5T_ALPHA_B16 = 1;
	public static final int JH5T_ALPHA_B32 = 2;
	public static final int JH5T_ALPHA_B64 = 3;
	public static final int JH5T_ALPHA_B8 = 4;
	public static final int JH5T_ALPHA_F32 = 5;
	public static final int JH5T_ALPHA_F64 = 6;
	public static final int JH5T_ALPHA_I16 = 7;
	public static final int JH5T_ALPHA_I32 = 8;
	public static final int JH5T_ALPHA_I64 = 9;
	public static final int JH5T_ALPHA_I8 = 10;
	public static final int JH5T_ALPHA_U16 = 11;
	public static final int JH5T_ALPHA_U32 = 12;
	public static final int JH5T_ALPHA_U64 = 13;
	public static final int JH5T_ALPHA_U8 = 14;
	public static final int JH5T_C_S1 = 15;
	public static final int JH5T_FORTRAN_S1 = 16;
	public static final int JH5T_IEEE_F32BE = 17;
	public static final int JH5T_IEEE_F32LE = 18;
	public static final int JH5T_IEEE_F64BE = 19;
	public static final int JH5T_IEEE_F64LE = 20;
	public static final int JH5T_INTEL_B16 = 21;
	public static final int JH5T_INTEL_B32 = 22;
	public static final int JH5T_INTEL_B64 = 23;
	public static final int JH5T_INTEL_B8 = 24;
	public static final int JH5T_INTEL_F32 = 25;
	public static final int JH5T_INTEL_F64 = 26;
	public static final int JH5T_INTEL_I16 = 27;
	public static final int JH5T_INTEL_I32 = 28;
	public static final int JH5T_INTEL_I64 = 29;
	public static final int JH5T_INTEL_I8 = 30;
	public static final int JH5T_INTEL_U16 = 31;
	public static final int JH5T_INTEL_U32 = 32;
	public static final int JH5T_INTEL_U64 = 33;
	public static final int JH5T_INTEL_U8 = 34;
	public static final int JH5T_MIPS_B16 = 35;
	public static final int JH5T_MIPS_B32 = 36;
	public static final int JH5T_MIPS_B64 = 37;
	public static final int JH5T_MIPS_B8 = 38;
	public static final int JH5T_MIPS_F32 = 39;
	public static final int JH5T_MIPS_F64 = 40;
	public static final int JH5T_MIPS_I16 = 41;
	public static final int JH5T_MIPS_I32 = 42;
	public static final int JH5T_MIPS_I64 = 43;
	public static final int JH5T_MIPS_I8 = 44;
	public static final int JH5T_MIPS_U16 = 45;
	public static final int JH5T_MIPS_U32 = 46;
	public static final int JH5T_MIPS_U64 = 47;
	public static final int JH5T_MIPS_U8 = 48;
	public static final int JH5T_NATIVE_B16 = 49;
	public static final int JH5T_NATIVE_B32 = 50;
	public static final int JH5T_NATIVE_B64 = 51;
	public static final int JH5T_NATIVE_B8 = 52;
	public static final int JH5T_NATIVE_CHAR = 53;
	public static final int JH5T_NATIVE_DOUBLE = 54;
	public static final int JH5T_NATIVE_FLOAT = 55;
	public static final int JH5T_NATIVE_HBOOL = 56;
	public static final int JH5T_NATIVE_HERR = 57;
	public static final int JH5T_NATIVE_HSIZE = 58;
	public static final int JH5T_NATIVE_HSSIZE = 59;
	public static final int JH5T_NATIVE_INT = 60;
	public static final int JH5T_NATIVE_INT_FAST16 = 61;
	public static final int JH5T_NATIVE_INT_FAST32 = 62;
	public static final int JH5T_NATIVE_INT_FAST64 = 63;
	public static final int JH5T_NATIVE_INT_FAST8 = 64;
	public static final int JH5T_NATIVE_INT_LEAST16 = 65;
	public static final int JH5T_NATIVE_INT_LEAST32 = 66;
	public static final int JH5T_NATIVE_INT_LEAST64 = 67;
	public static final int JH5T_NATIVE_INT_LEAST8 = 68;
	public static final int JH5T_NATIVE_INT16 = 69;
	public static final int JH5T_NATIVE_INT32 = 70;
	public static final int JH5T_NATIVE_INT64 = 71;
	public static final int JH5T_NATIVE_INT8 = 72;
	public static final int JH5T_NATIVE_LDOUBLE = 73;
	public static final int JH5T_NATIVE_LLONG = 74;
	public static final int JH5T_NATIVE_LONG = 75;
	public static final int JH5T_NATIVE_OPAQUE = 76;
	public static final int JH5T_NATIVE_SCHAR = 77;
	public static final int JH5T_NATIVE_SHORT = 78;
	public static final int JH5T_NATIVE_UCHAR = 79;
	public static final int JH5T_NATIVE_UINT = 80;
	public static final int JH5T_NATIVE_UINT_FAST16 = 81;
	public static final int JH5T_NATIVE_UINT_FAST32 = 82;
	public static final int JH5T_NATIVE_UINT_FAST64 = 83;
	public static final int JH5T_NATIVE_UINT_FAST8 = 84;
	public static final int JH5T_NATIVE_UINT_LEAST16 = 85;
	public static final int JH5T_NATIVE_UINT_LEAST32 = 86;
	public static final int JH5T_NATIVE_UINT_LEAST64 = 87;
	public static final int JH5T_NATIVE_UINT_LEAST8 = 88;
	public static final int JH5T_NATIVE_UINT16 = 89;
	public static final int JH5T_NATIVE_UINT32 = 90;
	public static final int JH5T_NATIVE_UINT64 = 91;
	public static final int JH5T_NATIVE_UINT8 = 92;
	public static final int JH5T_NATIVE_ULLONG = 93;
	public static final int JH5T_NATIVE_ULONG = 94;
	public static final int JH5T_NATIVE_USHORT = 95;
	public static final int JH5T_NCSET = 96;
	public static final int JH5T_NSTR = 97;
	public static final int JH5T_STD_B16BE = 98;
	public static final int JH5T_STD_B16LE = 99;
	public static final int JH5T_STD_B32BE = 100;
	public static final int JH5T_STD_B32LE = 101;
	public static final int JH5T_STD_B64BE = 102;
	public static final int JH5T_STD_B64LE = 103;
	public static final int JH5T_STD_B8BE = 104;
	public static final int JH5T_STD_B8LE = 105;
	public static final int JH5T_STD_I16BE = 106;
	public static final int JH5T_STD_I16LE = 107;
	public static final int JH5T_STD_I32BE = 108;
	public static final int JH5T_STD_I32LE = 109;
	public static final int JH5T_STD_I64BE = 110;
	public static final int JH5T_STD_I64LE = 111;
	public static final int JH5T_STD_I8BE = 112;
	public static final int JH5T_STD_I8LE = 113;
	public static final int JH5T_STD_REF_DSETREG = 114;
	public static final int JH5T_STD_REF_OBJ = 115;
	public static final int JH5T_STD_U16BE = 116;
	public static final int JH5T_STD_U16LE = 117;
	public static final int JH5T_STD_U32BE = 118;
	public static final int JH5T_STD_U32LE = 119;
	public static final int JH5T_STD_U64BE = 120;
	public static final int JH5T_STD_U64LE = 121;
	public static final int JH5T_STD_U8BE = 122;
	public static final int JH5T_STD_U8LE = 123;
	public static final int JH5T_UNIX_D32BE = 124;
	public static final int JH5T_UNIX_D32LE = 125;
	public static final int JH5T_UNIX_D64BE = 126;
	public static final int JH5T_UNIX_D64LE = 127;
}
