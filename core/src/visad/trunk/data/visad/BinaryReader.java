/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Reader;

import visad.*;

import visad.data.FileAccessor;

import visad.data.netcdf.Quantity;

import visad.data.visad.object.*;

/**
 * Read a {@link visad.Data Data} object in VisAD's binary format.
 *
 * @see <a href="http://www.ssec.wisc.edu/~dglo/binary_file_format.html">Binary File Format Spec</a>
 */
public class BinaryReader
  implements BinaryFile
{
  private static final boolean DEBUG_CSYS = false;
  private static final boolean DEBUG_DATA = false;
  private static final boolean DEBUG_DATA_DETAIL = false;
  private static final boolean DEBUG_ERRE = false;
  private static final boolean DEBUG_MATH = false;
  private static final boolean DEBUG_STR = false;
  private static final boolean DEBUG_TIME = false;
  private static final boolean DEBUG_UNIT = false;

  private DataInput file;

  private int version;

  private BinaryObjectCache unitCache, errorCache, cSysCache, typeCache;

  /**
   * Open the named file.
   * <br><br>
   * The first few bytes will be read to verify that the file starts
   * with the appropriate <tt>MAGIC_STR</tt> characters and that this
   * class can read the format version used by the file.
   *
   * @param name Name of file to be read.
   *
   * @exception IOException If the file cannot be opened.
   */
  public BinaryReader(String name)
    throws IOException
  {
    this(new File(name));
  }

  /**
   * Open the referenced file.
   * <br><br>
   * The first few bytes will be read to verify that the file starts
   * with the appropriate <tt>MAGIC_STR</tt> characters and that this
   * class can read the format version used by the file.
   *
   * @param ref File to be read.
   * @param randomAccess <tt>true</tt> if file is to be opened for
   *                     random access
   *
   * @exception IOException If the file cannot be opened.
   */
  public BinaryReader(File ref)
    throws IOException
  {
    this(new FileInputStream(ref));
  }

  /**
   * Prepare to read a binary object from the specified stream.
   * <br><br>
   * The first few bytes will be read to verify that the stream starts
   * with the appropriate <tt>MAGIC_STR</tt> characters and that this
   * class can read the format version used by the file.
   *
   * @param stream Stream to read.
   *
   * @exception IOException If the file cannot be opened.
   */
  public BinaryReader(InputStream stream)
    throws IOException
  {
    file = new DataInputStream(new BufferedInputStream(stream));

    version = checkMagic(file);

    unitCache = new BinaryObjectCache();
    errorCache = new BinaryObjectCache();
    cSysCache = new BinaryObjectCache();
    typeCache = new BinaryObjectCache();
  }

  private int checkMagic(DataInput file)
    throws IOException
  {

    final int version = readMagic(file);
    if (version < 1) {
      throw new IOException("File is not in VisAD binary format");
    }

    // validate the format version number
    if (version > FORMAT_VERSION) {
      throw new IOException("Don't understand VisAD Binary format version " +
                            version);
    }

    return version;
  }

  public void close()
    throws IOException
  {
    if (file instanceof InputStream) {
      ((InputStream )file).close();
    } else if (file instanceof RandomAccessFile) {
      ((RandomAccessFile )file).close();
    } else {
      throw new IOException("Unknown file class \"" +
                            file.getClass().getName() + "\"");
    }
  }

  public DataImpl getData()
    throws IOException, VisADException
  {
long totStart, csTime, dTime, dsTime, eTime, mTime, uTime;
totStart = csTime = dTime = dsTime = eTime = mTime = uTime = 0;

totStart = System.currentTimeMillis();
    DataImpl data = null;
    while (data == null) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        break;
      }

long tmpStart = System.currentTimeMillis();
      switch (directive) {
      case OBJ_COORDSYS:
if(DEBUG_MATH)System.err.println("getData: OBJ_COORDSYS (" + OBJ_COORDSYS + ")");
        BinaryCoordinateSystem.read(this);
if(DEBUG_TIME)csTime += System.currentTimeMillis() - tmpStart;
        break;
      case OBJ_DATA:
if(DEBUG_MATH)System.err.println("getData: OBJ_DATA (" + OBJ_DATA + ")");
        data = readData();
if(DEBUG_TIME)dTime += System.currentTimeMillis() - tmpStart;
        break;
      case OBJ_DATA_SERIAL:
if(DEBUG_MATH)System.err.println("getData: OBJ_DATA_SERIAL (" + OBJ_DATA_SERIAL + ")");
        data = (DataImpl )BinarySerializedObject.read(file);
if(DEBUG_TIME)dsTime += System.currentTimeMillis() - tmpStart;
        break;
      case OBJ_ERROR:
if(DEBUG_MATH)System.err.println("getData: OBJ_ERROR (" + OBJ_ERROR + ")");
        BinaryErrorEstimate.read(this);
if(DEBUG_TIME)eTime += System.currentTimeMillis() - tmpStart;
        break;
      case OBJ_MATH:
if(DEBUG_MATH)System.err.println("getData: OBJ_MATH (" + OBJ_MATH + ")");
        BinaryMathType.read(this);
if(DEBUG_TIME)mTime += System.currentTimeMillis() - tmpStart;
        break;
      case OBJ_UNIT:
if(DEBUG_MATH)System.err.println("getData: OBJ_UNIT (" + OBJ_UNIT + ")");
        BinaryUnit.read(this);
if(DEBUG_TIME)uTime += System.currentTimeMillis() - tmpStart;
        break;
      default:
        throw new IOException("Unknown directive " + directive);
      }
    }

if(DEBUG_TIME){
  long totTime = System.currentTimeMillis() - totStart;
  if (totTime > 0 && totTime != dTime) {
    System.err.print("gD: tot "+totTime);
    if (csTime > 0) System.err.print(" cs "+csTime);
    if (dTime > 0) System.err.print(" d "+dTime);
    if (dsTime > 0) System.err.print(" ds "+dsTime);
    if (eTime > 0) System.err.print(" e "+eTime);
    if (mTime > 0) System.err.print(" m "+mTime);
    if (uTime > 0) System.err.print(" u "+uTime);
    System.err.println();
  }
}
    return data;
  }

  public final BinaryObjectCache getCoordinateSystemCache() { return cSysCache; }
  public final BinaryObjectCache getErrorEstimateCache() { return errorCache; }
  public final DataInput getInput() { return file; }
  public final BinaryObjectCache getTypeCache() { return typeCache; }
  public final BinaryObjectCache getUnitCache() { return unitCache; }

  public static boolean isMagic(byte[] block)
  {
    DataInputStream dis;
    java.io.ByteArrayInputStream bs;
    bs = new java.io.ByteArrayInputStream(block);
    dis = new java.io.DataInputStream(bs);
    try {
      return (readMagic(dis) <= FORMAT_VERSION);
    } catch (IOException ioe) {
      return false;
    }
  }

  public DataImpl readData()
    throws IOException, VisADException
  {
long totStart, dsTime, fTime, ffTime, fsTime;
long g1dsTime, g2dsTime, g3dsTime, gsTime, g1sTime, g2sTime, g3sTime;
long i1sTime, i2sTime, i3sTime, iNsTime;
long ir1sTime, ir2sTime, ir3sTime, irsTime;
long l1sTime, l2sTime, l3sTime, lNsTime, llsTime;
long liTime, psTime, rTime, rtTime, ssTime, tTime, tuTime, usTime;

totStart = dsTime = fTime = ffTime = fsTime = 0;
g1dsTime = g2dsTime = g3dsTime = gsTime = g1sTime = g2sTime = g3sTime = 0;
i1sTime = i2sTime = i3sTime = iNsTime = 0;
ir1sTime = ir2sTime = ir3sTime = irsTime = 0;
l1sTime = l2sTime = l3sTime = lNsTime = llsTime = 0;
liTime = psTime = rTime = rtTime = ssTime = tTime = tuTime = usTime = 0;

totStart = System.currentTimeMillis();
    final int objLen = file.readInt();
    final byte dataType = file.readByte();

long tmpStart = System.currentTimeMillis();
    DataImpl data;
    switch (dataType) {
    case DATA_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_DOUBLE_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinarySimpleSet.read(this, dataType);
if(DEBUG_TIME)dsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_FIELD:
if(DEBUG_DATA)System.err.println("rdData: DATA_FIELD (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryFieldImpl.read(this);
if(DEBUG_TIME)fTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_FLAT_FIELD:
if(DEBUG_DATA)System.err.println("rdData: DATA_FLAT_FIELD (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryFlatField.read(this);
if(DEBUG_TIME)ffTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_FLOAT_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_FLOAT_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinarySimpleSet.read(this, dataType);
if(DEBUG_TIME)fsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_1D_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_1D_DOUBLE_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedDoubleSet.read(this, dataType);
if(DEBUG_TIME)g1dsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_2D_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_2D_DOUBLE_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedDoubleSet.read(this, dataType);
if(DEBUG_TIME)g2dsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_3D_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_3D_DOUBLE_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedDoubleSet.read(this, dataType);
if(DEBUG_TIME)g3dsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedSet.read(this, dataType);
if(DEBUG_TIME)gsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_1D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedSet.read(this, dataType);
if(DEBUG_TIME)g1sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_2D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedSet.read(this, dataType);
if(DEBUG_TIME)g2sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_GRIDDED_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_3D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryGriddedSet.read(this, dataType);
if(DEBUG_TIME)g3sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_INTEGER_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_1D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIntegerSet.read(this, dataType);
if(DEBUG_TIME)i1sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_INTEGER_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_2D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIntegerSet.read(this, dataType);
if(DEBUG_TIME)i2sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_INTEGER_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_3D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIntegerSet.read(this, dataType);
if(DEBUG_TIME)i3sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_INTEGER_ND_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_ND_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIntegerSet.read(this, dataType);
if(DEBUG_TIME)iNsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_IRREGULAR_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_1D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIrregularSet.read(this, dataType);
if(DEBUG_TIME)ir1sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_IRREGULAR_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_2D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIrregularSet.read(this, dataType);
if(DEBUG_TIME)ir2sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_IRREGULAR_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_3D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIrregularSet.read(this, dataType);
if(DEBUG_TIME)ir3sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_IRREGULAR_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryIrregularSet.read(this, dataType);
if(DEBUG_TIME)irsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_LINEAR_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_1D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryLinearSet.read(this, dataType);
if(DEBUG_TIME)l1sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_LINEAR_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_2D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryLinearSet.read(this, dataType);
if(DEBUG_TIME)l2sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_LINEAR_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_3D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryLinearSet.read(this, dataType);
if(DEBUG_TIME)l3sTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_LINEAR_ND_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_ND_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryLinearSet.read(this, dataType);
if(DEBUG_TIME)lNsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_LINEAR_LATLON_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_LATLON_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryLinearSet.read(this, dataType);
if(DEBUG_TIME)llsTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_LIST1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LIST1D_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryList1DSet.read(this);
if(DEBUG_TIME)liTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_PRODUCT_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_PRODUCT_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryProductSet.read(this);
if(DEBUG_TIME)psTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_REAL:
if(DEBUG_DATA)System.err.println("rdData: DATA_REAL (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryReal.read(this);
if(DEBUG_TIME)rTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_REAL_TUPLE:
if(DEBUG_DATA)System.err.println("rdData: DATA_REAL_TUPLE (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryRealTuple.read(this);
if(DEBUG_TIME)rtTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_SINGLETON_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_SINGLETON_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinarySingletonSet.read(this);
if(DEBUG_TIME)ssTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_TEXT:
if(DEBUG_DATA)System.err.println("rdData: DATA_TEXT (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryText.read(this);
if(DEBUG_TIME)tTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_TUPLE:
if(DEBUG_DATA)System.err.println("rdData: DATA_TUPLE (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryTuple.read(this);
if(DEBUG_TIME)tuTime += System.currentTimeMillis() - tmpStart;
      break;
    case DATA_UNION_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_UNION_SET (" + dataType + ")\nrdData: objLen (" + objLen + ")");
      data = BinaryUnionSet.read(this);
if(DEBUG_TIME)usTime += System.currentTimeMillis() - tmpStart;
      break;
    default:
      throw new IOException("Unknown Data type " + dataType);
    }

if(DEBUG_TIME){
  long totTime = System.currentTimeMillis() - totStart;
  if (totTime > 0) {
    System.err.print("rD: tot "+totTime);
    if (dsTime > 0) System.err.print(" ds "+dsTime);
    if (fTime > 0) System.err.print(" f "+fTime);
    if (ffTime > 0) System.err.print(" ff "+ffTime);
    if (fsTime > 0) System.err.print(" fs "+fsTime);
    if (g1dsTime > 0) System.err.print(" g1ds "+g1dsTime);
    if (g2dsTime > 0) System.err.print(" g2ds "+g2dsTime);
    if (g3dsTime > 0) System.err.print(" g3ds "+g3dsTime);
    if (gsTime > 0) System.err.print(" gs "+gsTime);
    if (g1sTime > 0) System.err.print(" g1s "+g1sTime);
    if (g2sTime > 0) System.err.print(" g2s "+g2sTime);
    if (g3sTime > 0) System.err.print(" g3s "+g3sTime);
    if (i1sTime > 0) System.err.print(" i1s "+i1sTime);
    if (i2sTime > 0) System.err.print(" i2s "+i2sTime);
    if (i3sTime > 0) System.err.print(" i3s "+i3sTime);
    if (iNsTime > 0) System.err.print(" iNs "+iNsTime);
    if (ir1sTime > 0) System.err.print(" ir1s "+ir1sTime);
    if (ir2sTime > 0) System.err.print(" ir2s "+ir2sTime);
    if (ir3sTime > 0) System.err.print(" ir3s "+ir3sTime);
    if (irsTime > 0) System.err.print(" irs "+irsTime);
    if (l1sTime > 0) System.err.print(" l1s "+l1sTime);
    if (l2sTime > 0) System.err.print(" l2s "+l2sTime);
    if (l3sTime > 0) System.err.print(" l3s "+l3sTime);
    if (lNsTime > 0) System.err.print(" lNs "+lNsTime);
    if (llsTime > 0) System.err.print(" lls "+llsTime);
    if (liTime > 0) System.err.print(" li "+liTime);
    if (psTime > 0) System.err.print(" ps "+psTime);
    if (rTime > 0) System.err.print(" r "+rTime);
    if (rtTime > 0) System.err.print(" rt "+rtTime);
    if (ssTime > 0) System.err.print(" ss "+ssTime);
    if (tTime > 0) System.err.print(" t "+tTime);
    if (tuTime > 0) System.err.print(" tu "+tuTime);
    if (usTime > 0) System.err.print(" us "+usTime);
    System.err.println();
  }
}
    return data;
  }

  private final static int readMagic(DataInput stream)
    throws IOException
  {
    byte[] magic = MAGIC_STR.getBytes();

    // try to read magic chars from beginning of file
    try {
      for (int i = 0; i < magic.length; i++) {
        if (stream.readByte() != magic[i]) {
          return -1;
        }
      }
    } catch (IOException ioe) {
      return -1;
    }

    // read the format version number
    try {
      return stream.readInt();
    } catch (IOException ioe) {
      return -1;
    }
  }
}
