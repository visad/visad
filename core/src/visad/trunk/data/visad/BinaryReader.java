package visad.data.visad;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import visad.*;

import visad.data.netcdf.Quantity;

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
  private static final boolean DEBUG_UNIT = false;

  private DataInputStream file;
  private int version;
  private char[] inbuf;
  BinaryObjectCache unitCache, errorCache, cSysCache, typeCache;

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
    this.file = new DataInputStream(new BufferedInputStream(stream));
    this.version = -1;
    this.inbuf = new char[1];

    this.unitCache = new BinaryObjectCache();
    this.errorCache = new BinaryObjectCache();
    this.cSysCache = new BinaryObjectCache();
    this.typeCache = new BinaryObjectCache();

    int version = readMagic(file);

    // validate the format version number
    if (version > FORMAT_VERSION) {
      throw new IOException("Don't understand VisAD Binary format version " +
                            version);
    }
  }

  private void cacheCoordinateSystem()
    throws IOException
  {
    // read the index number for this CoordinateSystem
    int index = file.readInt();
if(DEBUG_CSYS)System.err.println("cchCS: index (" + index + ")");

    byte cSysSerial = file.readByte();
    if (cSysSerial != FLD_COORDSYS_SERIAL) {
      throw new IOException("Corrupted file (no CoordinateSystem serial marker)");
    }
if(DEBUG_CSYS)System.err.println("cchCS: FLD_COORDSYS_SERIAL (" + FLD_COORDSYS_SERIAL + ")");

    // read the CoordinateSystem data
    CoordinateSystem cs = (CoordinateSystem )readSerializedObject();

    byte endByte = file.readByte();
if(DEBUG_CSYS)System.err.println("cchCS: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no CoordinateSystem end-marker)");
    }

    cSysCache.add(index, cs);
  }

  private void cacheErrorEstimate()
    throws IOException, VisADException
  {
    // read the index number for this ErrorEstimate
    int index = file.readInt();
if(DEBUG_ERRE)System.err.println("cchErrEst: index (" + index + ")");

    // read the ErrorEstimate data
    double errValue = file.readDouble();
if(DEBUG_ERRE)System.err.println("cchErrEst: errValue (" + errValue + ")");
    double mean = file.readDouble();
if(DEBUG_ERRE)System.err.println("cchErrEst: mean (" + mean + ")");
    long number = file.readLong();
if(DEBUG_ERRE)System.err.println("cchErrEst: number (" + number + ")");

    Unit u = null;

    boolean reading = true;
    while (reading) {
      byte directive = file.readByte();

      switch (directive) {
      case FLD_INDEX_UNIT:
        int uIndex = file.readInt();
if(DEBUG_ERRE&&DEBUG_UNIT)System.err.println("cchErrEst: unit index ("+uIndex+")");
        u = (Unit )unitCache.get(uIndex);
if(DEBUG_ERRE&&!DEBUG_UNIT)System.err.println("cchErrEst: unit index ("+uIndex+"="+u+")");
        break;
      case FLD_END:
if(DEBUG_ERRE)System.err.println("cchErrEst: FLD_END ("+FLD_END+")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown ErrorEstimate directive " + directive);
      }
    }

    ErrorEstimate err = new ErrorEstimate(errValue, mean, number, u);

    errorCache.add(index, err);
  }

  private MathType cacheMathType()
    throws IOException, VisADException
  {
    // read the index number for this MathType
    int index = file.readInt();
if(DEBUG_MATH)System.err.println("cchTy: index (" + index + ")");

    byte mathType = file.readByte();
    switch (mathType) {
    case MATH_FUNCTION:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_FUNCTION (" + MATH_FUNCTION + ")");
      return readFunctionType(index);
    case MATH_QUANTITY:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_QUANTITY (" + MATH_QUANTITY + ")");
      return readQuantity(index);
    case MATH_REAL_TUPLE:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_REAL_TUPLE (" + MATH_REAL_TUPLE + ")");
      return readRealTupleType(index);
    case MATH_REAL:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_REAL (" + MATH_REAL + ")");
      return readRealType(index);
    case MATH_SET:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_SET (" + MATH_SET + ")");
      return readSetType(index);
    case MATH_TEXT:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_TEXT (" + MATH_TEXT + ")");
      return readTextType(index);
    case MATH_TUPLE:
if(DEBUG_MATH)System.err.println("rdMthTy: MATH_TUPLE (" + MATH_TUPLE + ")");
      return readTupleType(index);
    default:
      throw new VisADException("Unknown Math type " + mathType);
    }
  }

  private void cacheUnit()
    throws IOException, VisADException
  {
    // read the index number for this Unit
    int index = file.readInt();
if(DEBUG_UNIT)System.err.println("cchU: index (" + index + ")");

    // read the Unit identifier
    String idStr = readString();
if(DEBUG_UNIT&&!DEBUG_STR)System.err.println("cchU: id (" + idStr + ")");

    // read the Unit description
    String defStr = readString();
if(DEBUG_UNIT&&!DEBUG_STR)System.err.println("cchU: definition (" + defStr + ")");

    byte endByte = file.readByte();
if(DEBUG_UNIT)System.err.println("cchU: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no Unit end-marker)");
    }

    Unit u;
    if (defStr.equals("promiscuous")) {
      u = CommonUnit.promiscuous;
    } else {
      try {
        u = visad.data.units.Parser.parse(defStr);
      } catch (Exception e) {
        throw new VisADException("Couldn't parse Unit specification \"" +
                                 defStr + "\"");
      }

      if (!(u instanceof BaseUnit)) {
        try {
          Unit namedUnit = u.clone(idStr);
          u = namedUnit;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    unitCache.add(index, u);
  }

  public void close()
    throws IOException
  {
    file.close();
  }

  public DataImpl getData()
    throws IOException, VisADException
  {
    while (true) {
      byte directive = file.readByte();

      switch (directive) {
      case OBJ_COORDSYS:
if(DEBUG_MATH)System.err.println("getData: OBJ_COORDSYS (" + OBJ_COORDSYS + ")");
        cacheCoordinateSystem();
        break;
      case OBJ_DATA:
if(DEBUG_MATH)System.err.println("getData: OBJ_DATA (" + OBJ_DATA + ")");
        return readData(file.readByte());
      case OBJ_DATA_SERIAL:
if(DEBUG_MATH)System.err.println("getData: OBJ_DATA_SERIAL (" + OBJ_DATA_SERIAL + ")");
        return (DataImpl )readSerializedObject();
      case OBJ_ERROR:
if(DEBUG_MATH)System.err.println("getData: OBJ_ERROR (" + OBJ_ERROR + ")");
        cacheErrorEstimate();
        break;
      case OBJ_MATH:
if(DEBUG_MATH)System.err.println("getData: OBJ_MATH (" + OBJ_MATH + ")");
        cacheMathType();
        break;
      case OBJ_UNIT:
if(DEBUG_MATH)System.err.println("getData: OBJ_UNIT (" + OBJ_UNIT + ")");
        cacheUnit();
        break;
      default:
        throw new IOException("Unknown directive " + directive);
      }
    }
  }

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

  private CoordinateSystem[] readCoordinateSystems()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_CSYS)System.err.println("rdCSysS: len ("+len+")");
    if (len < 1) {
      throw new IOException("Corrupted file" +
                            " (bad CoordinateSystem array length " + len +
                            ")");
    }

    CoordinateSystem[] cSys = new CoordinateSystem[len];
    for (int i = 0; i < len; i++) {
      int uIndex = file.readInt();
if(DEBUG_CSYS)System.err.println("rdCSysS: cSys index ("+uIndex+")");
      cSys[i] = (CoordinateSystem )cSysCache.get(uIndex);
if(DEBUG_CSYS)System.err.println("rdCSysS: === #"+i+": "+cSys[i]+")");
    }

    return cSys;
  }
  public DataImpl readData(byte dataType)
    throws IOException, VisADException
  {
    switch (dataType) {
    case DATA_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_DOUBLE_SET (" + DATA_DOUBLE_SET + ")");
      return readSimpleSet(dataType);
    case DATA_FIELD:
if(DEBUG_DATA)System.err.println("rdData: DATA_FIELD (" + DATA_FIELD + ")");
      return readFieldImpl();
    case DATA_FLAT_FIELD:
if(DEBUG_DATA)System.err.println("rdData: DATA_FLAT_FIELD (" + DATA_FLAT_FIELD + ")");
      return readFlatField();
    case DATA_FLOAT_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_FLOAT_SET (" + DATA_FLOAT_SET + ")");
      return readSimpleSet(dataType);
    case DATA_GRIDDED_1D_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_1D_DOUBLE_SET (" + DATA_GRIDDED_1D_DOUBLE_SET + ")");
      return readGriddedDoubleSet(dataType);
    case DATA_GRIDDED_2D_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_2D_DOUBLE_SET (" + DATA_GRIDDED_2D_DOUBLE_SET + ")");
      return readGriddedDoubleSet(dataType);
    case DATA_GRIDDED_3D_DOUBLE_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_2D_DOUBLE_SET (" + DATA_GRIDDED_3D_DOUBLE_SET + ")");
      return readGriddedDoubleSet(dataType);
    case DATA_GRIDDED_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_SET (" + DATA_GRIDDED_SET + ")");
      return readGriddedSet(dataType);
    case DATA_GRIDDED_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_1D_SET (" + DATA_GRIDDED_1D_SET + ")");
      return readGriddedSet(dataType);
    case DATA_GRIDDED_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_2D_SET (" + DATA_GRIDDED_2D_SET + ")");
      return readGriddedSet(dataType);
    case DATA_GRIDDED_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_GRIDDED_3D_SET (" + DATA_GRIDDED_3D_SET + ")");
      return readGriddedSet(dataType);
    case DATA_INTEGER_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_1D_SET (" + DATA_INTEGER_1D_SET + ")");
      return readIntegerSet(dataType);
    case DATA_INTEGER_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_2D_SET (" + DATA_INTEGER_2D_SET + ")");
      return readIntegerSet(dataType);
    case DATA_INTEGER_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_3D_SET (" + DATA_INTEGER_3D_SET + ")");
      return readIntegerSet(dataType);
    case DATA_INTEGER_ND_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_INTEGER_ND_SET (" + DATA_INTEGER_ND_SET + ")");
      return readIntegerSet(dataType);
    case DATA_IRREGULAR_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_1D_SET (" + DATA_IRREGULAR_1D_SET + ")");
      return readIrregularSet(dataType);
    case DATA_IRREGULAR_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_2D_SET (" + DATA_IRREGULAR_2D_SET + ")");
      return readIrregularSet(dataType);
    case DATA_IRREGULAR_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_3D_SET (" + DATA_IRREGULAR_3D_SET + ")");
      return readIrregularSet(dataType);
    case DATA_IRREGULAR_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_IRREGULAR_SET (" + DATA_IRREGULAR_SET + ")");
      return readIrregularSet(dataType);
    case DATA_LINEAR_1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_1D_SET (" + DATA_LINEAR_1D_SET + ")");
      return readLinearSet(dataType);
    case DATA_LINEAR_2D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_2D_SET (" + DATA_LINEAR_2D_SET + ")");
      return readLinearSet(dataType);
    case DATA_LINEAR_3D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_3D_SET (" + DATA_LINEAR_3D_SET + ")");
      return readLinearSet(dataType);
    case DATA_LINEAR_ND_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_ND_SET (" + DATA_LINEAR_ND_SET + ")");
      return readLinearSet(dataType);
    case DATA_LINEAR_LATLON_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LINEAR_LATLON_SET (" + DATA_LINEAR_LATLON_SET + ")");
      return readLinearSet(dataType);
    case DATA_LIST1D_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_LIST1D_SET (" + DATA_LIST1D_SET + ")");
      return readList1DSet();
    case DATA_PRODUCT_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_PRODUCT_SET (" + DATA_PRODUCT_SET + ")");
      return readProductSet();
    case DATA_REAL:
if(DEBUG_DATA)System.err.println("rdData: DATA_REAL (" + DATA_REAL + ")");
      return readReal();
    case DATA_REAL_TUPLE:
if(DEBUG_DATA)System.err.println("rdData: DATA_REAL_TUPLE (" + DATA_REAL_TUPLE + ")");
      return readRealTuple();
    case DATA_SINGLETON_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_SINGLETON_SET (" + DATA_SINGLETON_SET + ")");
      return readSingletonSet();
    case DATA_TEXT:
if(DEBUG_DATA)System.err.println("rdData: DATA_TEXT (" + DATA_TEXT + ")");
      return readText();
    case DATA_TUPLE:
if(DEBUG_DATA)System.err.println("rdData: DATA_TUPLE (" + DATA_TUPLE + ")");
      return readTuple();
    case DATA_UNION_SET:
if(DEBUG_DATA)System.err.println("rdData: DATA_UNION_SET (" + DATA_UNION_SET + ")");
      return readUnionSet();
    default:
      throw new IOException("Unknown Data type " + dataType);
    }
  }

  private Data[] readDataArray()
    throws IOException, VisADException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdDataRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad Data array length " +
                            len + ")");
    }

    Data[] array = new Data[len];
    for (int i = 0; i < len; i++) {
      array[i] = getData();
    }

    return array;
  }

  private Delaunay readDelaunay()
    throws IOException, VisADException
  {
    int[][] tri = null;
    int[][] verts = null;
    int[][] walk = null;
    int[][] edges = null;
    int numEdges = -1;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_DELAUNAY_TRI:
if(DEBUG_DATA)System.err.println("rdDel: FLD_DELAUNAY_TRI (" + FLD_DELAUNAY_TRI + ")");
        tri = readIntegerMatrix();
        break;
      case FLD_DELAUNAY_VERTICES:
if(DEBUG_DATA)System.err.println("rdDel: FLD_DELAUNAY_VERTICES (" + FLD_DELAUNAY_VERTICES + ")");
        verts = readIntegerMatrix();
        break;
      case FLD_DELAUNAY_WALK:
if(DEBUG_DATA)System.err.println("rdDel: FLD_DELAUNAY_WALK (" + FLD_DELAUNAY_WALK + ")");
        walk = readIntegerMatrix();
        break;
      case FLD_DELAUNAY_EDGES:
if(DEBUG_DATA)System.err.println("rdDel: FLD_DELAUNAY_EDGES (" + FLD_DELAUNAY_EDGES + ")");
        edges = readIntegerMatrix();
        break;
      case FLD_DELAUNAY_NUM_EDGES:
if(DEBUG_DATA)System.err.println("rdDel: FLD_DELAUNAY_NUM_EDGES (" + FLD_DELAUNAY_NUM_EDGES + ")");
        numEdges = file.readInt();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdDel: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown Delaunay directive " +
                              directive);
      }
    }

    return new DelaunayCustom(null, tri, verts, walk, edges, numEdges);
  }

  private double[] readDoubleArray()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdDblRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad double array length " +
                            len + ")");
    }

    double[] array = new double[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readDouble();
if(DEBUG_DATA_DETAIL)System.err.println("rdDblRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  private double[][] readDoubleMatrix()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdDblMtx: len (" + len + ")");
    if (len < 0) {
      return null;
    }

    double[][] matrix = new double[len][];
    for (int i = 0; i < len; i++) {
      final int len2 = file.readInt();
if(DEBUG_DATA)System.err.println("rdDblMtx: #" + i + " len (" + len2 + ")");
      matrix[i] = new double[len2];
      for (int j = 0; j < len2; j++) {
        matrix[i][j] = file.readDouble();
if(DEBUG_DATA_DETAIL)System.err.println("rdDblMtx: #" + i + "," + j +" (" + matrix[i][j] + ")");
      }
    }

    return matrix;
  }

  private ErrorEstimate[] readErrorEstimates()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_ERRE)System.err.println("rdErrEstS: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file" +
                            " (bad ErrorEstimate array length " + len + ")");
    }

    ErrorEstimate[] errs = new ErrorEstimate[len];
    for (int i = 0; i < len; i++) {
      int index = file.readInt();
if(DEBUG_ERRE)System.err.println("rdErrEstS:    #"+i+" index ("+index+")");

      if (index < 0) {
        errs[i] = null;
      } else {
        errs[i] = (ErrorEstimate )errorCache.get(index);
      }
if(DEBUG_ERRE)System.err.println("rdErrEstS:    === #"+i+" ErrorEstimate ("+errs[i]+")");
    }

    return errs;
  }

  public FieldImpl readFieldImpl()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdFldI: type index (" + typeIndex + ")");
    FunctionType ft = (FunctionType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdFldI: type index (" + typeIndex + "=" + ft + ")");

    Set set = null;
    Data[] samples = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET:
if(DEBUG_DATA)System.err.println("rdFldI: FLD_SET (" + FLD_SET + ")");
        set = (Set )getData();
        break;
      case FLD_DATA_SAMPLES:
if(DEBUG_DATA)System.err.println("rdFldI: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        samples = readDataArray();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdFldI: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown FieldImpl directive " +
                              directive);
      }
    }

    if (ft == null) {
      throw new IOException("No FunctionType found for FieldImpl");
    }

    FieldImpl fld = (set == null ? new FieldImpl(ft) :
                     new FieldImpl(ft, set));
    if (samples != null) {
      final int len = samples.length;
      for (int i = 0; i < len; i++) {
        fld.setSample(i, samples[i]);
      }
    }

    return fld;
  }

  public FlatField readFlatField()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdFlFld: type index (" + typeIndex + ")");
    FunctionType ft = (FunctionType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdFlFld: type index (" + typeIndex + "=" + ft + ")");

    Set domainSet = null;
    Data[] samples = null;
    CoordinateSystem cs = null;
    CoordinateSystem[] rangeCS = null;
    Set[] rangeSets = null;
    Unit[] units = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_SET (" + FLD_SET + ")");
        domainSet = (Set )getData();
        break;
      case FLD_DATA_SAMPLES:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        samples = readDataArray();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdFlFld: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdFlFld: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_COORDSYSES:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_INDEX_COORDSYSES (" + FLD_INDEX_COORDSYSES + ")");
        rangeCS = readCoordinateSystems();
        break;
      case FLD_SET_LIST:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_SET_LIST (" + FLD_SET_LIST + ")");
        rangeSets = readSetArray();
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdFlFld: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown FlatField directive " +
                              directive);
      }
    }

    if (ft == null) {
      throw new IOException("No FunctionType found for FlatField");
    }

    FlatField fld = new FlatField(ft, domainSet, rangeCS, rangeSets, units);
    if (samples != null) {
      final int len = samples.length;
      for (int i = 0; i < len; i++) {
        fld.setSample(i, samples[i]);
      }
    }

    return fld;
  }

  private float[] readFloatArray()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdFltRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad float array length " +
                            len + ")");
    }

    float[] array = new float[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readFloat();
if(DEBUG_DATA_DETAIL)System.err.println("rdFltRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  private float[][] readFloatMatrix()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdFltMtx: len (" + len + ")");
    if (len < 0) {
      return null;
    }

    float[][] matrix = new float[len][];
    for (int i = 0; i < len; i++) {
      final int len2 = file.readInt();
if(DEBUG_DATA)System.err.println("rdFltMtx: #" + i + " len (" + len2 + ")");
      matrix[i] = new float[len2];
      for (int j = 0; j < len2; j++) {
        matrix[i][j] = file.readFloat();
if(DEBUG_DATA_DETAIL)System.err.println("rdFltMtx: #" + i + "," + j +" (" + matrix[i][j] + ")");
      }
    }

    return matrix;
  }

  private FunctionType readFunctionType(int index)
    throws IOException, VisADException
  {
    int typeIndex;

    typeIndex = file.readInt();
if(DEBUG_MATH)System.err.println("rdFuTy: domain index (" + typeIndex + ")");
    MathType domain = (MathType )typeCache.get(typeIndex);
if(DEBUG_MATH)System.err.println("rdFuTy: === read domain " + domain);
    typeIndex = file.readInt();
if(DEBUG_MATH)System.err.println("rdFuTy: domain index (" + typeIndex + ")");
    MathType range = (MathType )typeCache.get(typeIndex);
if(DEBUG_MATH)System.err.println("rdFuTy: === read range " + domain);

    byte endByte = file.readByte();
if(DEBUG_MATH)System.err.println("rdFuTy: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no TupleType end-marker)");
    }

    FunctionType ft = new FunctionType(domain, range);

    typeCache.add(index, ft);

    return ft;
  }

  public GriddedSet readGriddedDoubleSet(byte dataType)
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdGrDblSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdGrDblSet: type index (" + typeIndex + "=" + st + ")");

    double[][] samples = null;
    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_DOUBLE_SAMPLES:
if(DEBUG_DATA)System.err.println("rdGrDblSet: FLD_DOUBLE_SAMPLES (" + FLD_DOUBLE_SAMPLES + ")");
        samples = readDoubleMatrix();
        break;
      case FLD_LENGTHS:
if(DEBUG_DATA)System.err.println("rdGrDblSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = readIntegerArray();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdGrDblSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdGrDblSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdGrDblSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdGrDblSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdGrDblSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdGrDblSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown GriddedDoubleSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for GriddedDoubleSet");
    }
    if (lengths == null) {
      throw new IOException("No lengths found for GriddedDoubleSet");
    }

    final int dim;
    switch (dataType) {
    case DATA_GRIDDED_1D_DOUBLE_SET:
      dim = 1;
      break;
    case DATA_GRIDDED_2D_DOUBLE_SET:
      dim = 2;
      break;
    case DATA_GRIDDED_3D_DOUBLE_SET:
      dim = 3;
      break;
    default:
      throw new IOException("Unknown GriddedDoubleSet type " + dataType);
    }

    if (samples != null && samples.length != dim) {
      throw new VisADException("Expected " + dim +
                               "D sample array, not " +
                               samples.length + "D");
    }

    switch (dataType) {
    case DATA_GRIDDED_1D_DOUBLE_SET:
      return new Gridded1DDoubleSet(st, samples, lengths[0], cs, units, errs);
    case DATA_GRIDDED_2D_DOUBLE_SET:
      if (lengths.length == 1) {
        return new Gridded2DDoubleSet(st, samples, lengths[0],
                                      cs, units, errs);
      } else {
        return new Gridded2DDoubleSet(st, samples, lengths[0], lengths[1],
                                      cs, units, errs);
      }
    case DATA_GRIDDED_3D_DOUBLE_SET:
      if (lengths.length == 1) {
        return new Gridded3DDoubleSet(st, samples, lengths[0],
                                      cs, units, errs);
      } else if (lengths.length == 2) {
        return new Gridded3DDoubleSet(st, samples, lengths[0], lengths[1],
                                      cs, units, errs);
      } else {
        return new Gridded3DDoubleSet(st, samples,
                                      lengths[0], lengths[1], lengths[2],
                                      cs, units, errs);
      }
    default:
      throw new IOException("Unknown GriddedDoubleSet type " + dataType);
    }
  }

  public GriddedSet readGriddedSet(byte dataType)
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdGrSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdGrSet: type index (" + typeIndex + "=" + st + ")");

    float[][] samples = null;
    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FLOAT_SAMPLES:
if(DEBUG_DATA)System.err.println("rdGrSet: FLD_FLOAT_SAMPLES (" + FLD_FLOAT_SAMPLES + ")");
        samples = readFloatMatrix();
        break;
      case FLD_LENGTHS:
if(DEBUG_DATA)System.err.println("rdGrSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = readIntegerArray();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdGrSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdGrSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdGrSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdGrSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdGrSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdGrSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown GriddedSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for GriddedSet");
    }
    if (lengths == null) {
      throw new IOException("No lengths found for GriddedSet");
    }

    final int dim;
    switch (dataType) {
    case DATA_GRIDDED_1D_SET:
      dim = 1;
      break;
    case DATA_GRIDDED_2D_SET:
      dim = 2;
      break;
    case DATA_GRIDDED_3D_SET:
      dim = 3;
      break;
    case DATA_GRIDDED_SET:
      dim = -1;
      break;
    default:
      throw new IOException("Unknown GriddedSet type " + dataType);
    }

    if (samples != null && dim > 0 && samples.length != dim) {
      throw new VisADException("Expected " + dim +
                               "D sample array, not " +
                               samples.length + "D");
    }

    switch (dataType) {
    case DATA_GRIDDED_1D_SET:
      return new Gridded1DSet(st, samples, lengths[0], cs, units, errs);
    case DATA_GRIDDED_2D_SET:
      if (lengths.length == 1) {
        return new Gridded2DSet(st, samples, lengths[0], cs, units, errs);
      } else {
        return new Gridded2DSet(st, samples, lengths[0], lengths[1],
                                cs, units, errs);
      }
    case DATA_GRIDDED_3D_SET:
      if (lengths.length == 1) {
        return new Gridded3DSet(st, samples, lengths[0], cs, units, errs);
      } else if (lengths.length == 2) {
        return new Gridded3DSet(st, samples, lengths[0], lengths[1],
                                cs, units, errs);
      } else {
        return new Gridded3DSet(st, samples,
                                lengths[0], lengths[1], lengths[2],
                                cs, units, errs);
      }
    case DATA_GRIDDED_SET:
      return new GriddedSet(st, samples, lengths, cs, units, errs);
    default:
      throw new IOException("Unknown GriddedSet type " + dataType);
    }
  }

  private int[] readIntegerArray()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdIntRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad double array length " +
                            len + ")");
    }

    int[] array = new int[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readInt();
if(DEBUG_DATA_DETAIL)System.err.println("rdIntRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  private int[][] readIntegerMatrix()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdIntMtx: len (" + len + ")");
    if (len < 0) {
      return null;
    }

    int[][] matrix = new int[len][];
    for (int i = 0; i < len; i++) {
      final int len2 = file.readInt();
if(DEBUG_DATA)System.err.println("rdIntMtx: #" + i + " len (" + len2 + ")");
      matrix[i] = new int[len2];
      for (int j = 0; j < len2; j++) {
        matrix[i][j] = file.readInt();
if(DEBUG_DATA_DETAIL)System.err.println("rdIntMtx: #" + i + "," + j +" (" + matrix[i][j] + ")");
      }
    }

    return matrix;
  }

  public GriddedSet readIntegerSet(byte dataType)
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdIntSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdIntSet: type index (" + typeIndex + "=" + st + ")");

    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;
    Integer1DSet[] comps = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_LENGTHS:
if(DEBUG_DATA)System.err.println("rdIntSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = readIntegerArray();
        break;
      case FLD_INTEGER_SETS:
if(DEBUG_DATA)System.err.println("rdIntSet: FLD_INTEGER_SETS (" + FLD_INTEGER_SETS + ")");
        comps = readInteger1DSets();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdIntSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdIntSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdIntSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdIntSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdIntSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdIntSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown IntegerSet directive " + directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for IntegerSet");
    }

    if (comps != null) {
      if (lengths != null) {
        throw new IOException("Both components and lengths found for IntegerSet");
      }

      switch (dataType) {
      case DATA_INTEGER_1D_SET:
        throw new IOException("Components specified for Integer1DSet");
      case DATA_INTEGER_2D_SET:
        return new Integer2DSet(st, comps, cs, units, errs);
      case DATA_INTEGER_3D_SET:
        return new Integer3DSet(st, comps, cs, units, errs);
      case DATA_INTEGER_ND_SET:
        return new IntegerNDSet(st, comps, cs, units, errs);
      default:
        throw new IOException("Unknown IntegerSet type " + dataType);
      }
    } else {
      if (lengths == null) {
        throw new IOException("No lengths found for IntegerSet");
      }

      final int dim;
      switch (dataType) {
      case DATA_INTEGER_1D_SET:
        dim = 1;
        break;
      case DATA_INTEGER_2D_SET:
        dim = 2;
        break;
      case DATA_INTEGER_3D_SET:
        dim = 3;
        break;
      default:
        dim = -1;
        break;
      }

      if (dim > 0 && lengths.length != dim) {
        throw new VisADException("Expected " + dim + " length" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lengths.length);
      }

      switch (dataType) {
      case DATA_INTEGER_1D_SET:
        return new Integer1DSet(st, lengths[0], cs, units, errs);
      case DATA_INTEGER_2D_SET:
        return new Integer2DSet(st, lengths[0], lengths[1], cs, units, errs);
      case DATA_INTEGER_3D_SET:
        return new Integer3DSet(st, lengths[0], lengths[1], lengths[2],
                                cs, units, errs);
      case DATA_INTEGER_ND_SET:
        return new IntegerNDSet(st, lengths, cs, units, errs);
      default:
        throw new IOException("Unknown IntegerSet type " + dataType);
      }
    }
  }

  private Integer1DSet[] readInteger1DSets()
    throws IOException, VisADException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdI1DSetS: len (" + len + ")");
    Integer1DSet[] sets = new Integer1DSet[len];
    for (int i = 0; i < len; i++) {
      sets[i] = (Integer1DSet )getData();
    }
    return sets;
  }

  public IrregularSet readIrregularSet(byte dataType)
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdIrrSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdIrrSet: type index (" + typeIndex + "=" + st + ")");

    float[][] samples = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;
    Delaunay delaunay = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FLOAT_SAMPLES:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_FLOAT_SAMPLES (" + FLD_FLOAT_SAMPLES + ")");
        samples = readFloatMatrix();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdIrrSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdIrrSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_DELAUNAY_SERIAL:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_DELAUNAY_SERIAL (" + FLD_DELAUNAY_SERIAL + ")");
        delaunay = (Delaunay )readSerializedObject();
        break;
      case FLD_DELAUNAY:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_DELAUNAY (" + FLD_DELAUNAY + ")");
        delaunay = readDelaunay();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdIrrSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown IrregularSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for IrregularSet");
    }

    final int dim;
    switch (dataType) {
    case DATA_IRREGULAR_1D_SET:
      dim = 1;
      break;
    case DATA_IRREGULAR_2D_SET:
      dim = 2;
      break;
    case DATA_IRREGULAR_3D_SET:
      dim = 3;
      break;
    case DATA_IRREGULAR_SET:
      dim = -1;
      break;
    default:
      throw new IOException("Unknown IrregularSet type " + dataType);
    }

    if (dim > 0 && samples.length != dim) {
      throw new VisADException("Expected " + dim +
                               "D sample array, not " +
                               samples.length + "D");
    }
    if (dataType == DATA_IRREGULAR_1D_SET && delaunay != null) {
      System.err.println("Delaunay ignored for Irregular1DSet");
    }

    switch (dataType) {
    case DATA_IRREGULAR_1D_SET:
      return new Irregular1DSet(st, samples, cs, units, errs);
    case DATA_IRREGULAR_2D_SET:
      return new Irregular2DSet(st, samples, cs, units, errs, delaunay);
    case DATA_IRREGULAR_3D_SET:
      return new Irregular3DSet(st, samples, cs, units, errs, delaunay);
    case DATA_IRREGULAR_SET:
      return new IrregularSet(st, samples, cs, units, errs, delaunay);
    default:
      throw new IOException("Unknown IrregularSet type " + dataType);
    }
  }

  public GriddedSet readLinearSet(byte dataType)
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdLinSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdLinSet: type index (" + typeIndex + "=" + st + ")");

    double[] firsts = null;
    double[] lasts = null;
    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;
    Linear1DSet[] comps = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FIRSTS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_FIRSTS (" + FLD_FIRSTS + ")");
        firsts = readDoubleArray();
        break;
      case FLD_LASTS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_LASTS (" + FLD_LASTS + ")");
        lasts = readDoubleArray();
        break;
      case FLD_LENGTHS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = readIntegerArray();
        break;
      case FLD_LINEAR_SETS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_LINEAR_SETS (" + FLD_LINEAR_SETS + ")");
        comps = readLinear1DSets();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdLinSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdLinSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown LinearSet directive " + directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for LinearSet");
    }

    if (comps != null) {
      if (firsts != null) {
        throw new IOException("Both components and firsts found for LinearSet");
      }
      if (lasts != null) {
        throw new IOException("Both components and lasts found for LinearSet");
      }
      if (lengths != null) {
        throw new IOException("Both components and lengths found for LinearSet");
      }

      switch (dataType) {
      case DATA_LINEAR_1D_SET:
        throw new IOException("Components specified for Linear1DSet");
      case DATA_LINEAR_2D_SET:
        return new Linear2DSet(st, comps, cs, units, errs);
      case DATA_LINEAR_3D_SET:
        return new Linear3DSet(st, comps, cs, units, errs);
      case DATA_LINEAR_ND_SET:
        return new LinearNDSet(st, comps, cs, units, errs);
      case DATA_LINEAR_LATLON_SET:
        return new LinearLatLonSet(st, comps, cs, units, errs);
      default:
        throw new IOException("Unknown LinearSet type " + dataType);
      }
    } else {

      if (firsts == null) {
        throw new IOException("No firsts found for LinearSet");
      }
      if (lasts == null) {
        throw new IOException("No lasts found for LinearSet");
      }
      if (lengths == null) {
        throw new IOException("No lengths found for LinearSet");
      }

      final int dim;
      switch (dataType) {
      case DATA_LINEAR_1D_SET:
        dim = 1;
        break;
      case DATA_LINEAR_2D_SET:
        dim = 2;
        break;
      case DATA_LINEAR_3D_SET:
        dim = 3;
        break;
      case DATA_LINEAR_LATLON_SET:
        dim = 2;
        break;
      default:
        dim = -1;
        break;
      }

      if (dim > 0 && firsts.length != dim) {
        throw new VisADException("Expected " + dim + " first value" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 firsts.length);
      }
      if (dim > 0 && lasts.length != dim) {
        throw new VisADException("Expected " + dim + " last value" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lasts.length);
      }
      if (dim > 0 && lengths.length != dim) {
        throw new VisADException("Expected " + dim + " length" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lengths.length);
      }

      switch (dataType) {
      case DATA_LINEAR_1D_SET:
        return new Linear1DSet(st, firsts[0], lasts[0], lengths[0], cs, units,
                               errs);
      case DATA_LINEAR_2D_SET:
        return new Linear2DSet(st, firsts[0], lasts[0], lengths[0],
                               firsts[1], lasts[1], lengths[1], cs, units, errs);
      case DATA_LINEAR_3D_SET:
        return new Linear3DSet(st, firsts[0], lasts[0], lengths[0],
                               firsts[1], lasts[1], lengths[1],
                               firsts[2], lasts[2], lengths[2], cs, units, errs);
      case DATA_LINEAR_ND_SET:
        return new LinearNDSet(st, firsts, lasts, lengths, cs, units, errs);
      case DATA_LINEAR_LATLON_SET:
        return new LinearLatLonSet(st, firsts[0], lasts[0], lengths[0],
                                   firsts[1], lasts[1], lengths[1],
                                   cs, units, errs);
      default:
        throw new IOException("Unknown LinearSet type " + dataType);
      }
    }
  }

  private Linear1DSet[] readLinear1DSets()
    throws IOException, VisADException
  {
    Linear1DSet[] sets = new Linear1DSet[file.readInt()];
    for (int i = 0; i < sets.length; i++) {
      sets[i] = (Linear1DSet )getData();
    }
    return sets;
  }

  private List1DSet readList1DSet()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdL1DSet: type index (" + typeIndex + ")");
    MathType mt = (MathType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdL1DSet: type index (" + typeIndex + "=" + mt + ")");

    float[] list = null;
    CoordinateSystem cs = null;
    Unit[] units = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FLOAT_LIST:
if(DEBUG_DATA)System.err.println("rdL1DSet: FLD_FLOAT_LIST (" + FLD_FLOAT_LIST + ")");
        list = readFloatArray();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdL1DSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdL1DSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdL1DSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdL1DSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdL1DSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown List1DSet directive " +
                              directive);
      }
    }

    if (mt == null) {
      throw new IOException("No MathType found for List1DSet");
    }
    if (list == null) {
      throw new IOException("No list found for List1DSet");
    }

    return new List1DSet(list, mt, cs, units);
  }

  private static int readMagic(DataInputStream stream)
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

  private MathType[] readMathTypes(int dim)
    throws IOException, VisADException
  {
    if (dim < 1) {
      throw new IOException("Corrupted file (bad MathType list length)");
    }

    MathType[] list = new MathType[dim];
    for (int i = 0; i < dim; i++) {
      int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdMTyS: type #" + i + " index (" + typeIndex + ")");
      list[i] = (MathType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdMTyS: type #" + i + " index (" + typeIndex + "=" + list[i] + ")");
    }

    return list;
  }

  private ProductSet readProductSet()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdPrSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdPrSet: type index (" + typeIndex + "=" + st + ")");

    SampledSet[] sets = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET_SAMPLES:
if(DEBUG_DATA)System.err.println("rdPrSet: FLD_SET_SAMPLES (" + FLD_SET_SAMPLES + ")");
        sets = readSampledSets();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdPrSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdPrSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdPrSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdPrSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdPrSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdPrSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown ProductSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for ProductSet");
    }
    if (sets == null) {
      throw new IOException("No sets found for ProductSet");
    }

    return new ProductSet(st, sets, cs, units, errs);
  }

  private Quantity readQuantity(int index)
    throws IOException, VisADException
  {
    // read the name
    String name = readString();
if(DEBUG_MATH&&!DEBUG_STR)System.err.println("rdQuant: name (" + name + ")");

    // read the name
    String unitSpec = readString();
if(DEBUG_MATH&&!DEBUG_STR)System.err.println("rdQuant: unitSpec (" + unitSpec + ")");

    boolean setFollowsType = false;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET_FOLLOWS_TYPE:
if(DEBUG_MATH)System.err.println("rdQuant: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        setFollowsType = true;
        break;
      case FLD_END:
if(DEBUG_MATH)System.err.println("rdQuant: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealType directive " + directive);
      }
    }

    Quantity q;
    try {
      q = Quantity.getQuantity(name, unitSpec, null);
    } catch (visad.data.units.ParseException pe) {
      throw new VisADException("Couldn't parse Quantity unitSpec \"" +
                               unitSpec + "\"");
    }

    typeCache.add(index, q);

    if (setFollowsType) {
      SimpleSet set = (SimpleSet )getData();
      try {
        q.setDefaultSet(set);
      } catch (TypeException te) {
        // ignore failure to set type
      }
    }

    return q;
  }

  private Real readReal()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdRl: type index (" + typeIndex + ")");
    RealType rt = (RealType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdRl: type index (" + typeIndex + "=" + rt + ")");

    // read the value
    double value = file.readDouble();
if(DEBUG_DATA)System.err.println("rdRl: value (" + value + ")");

    Unit u = null;
    ErrorEstimate error = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      int index;
      switch (directive) {
      case FLD_INDEX_UNIT:
if(DEBUG_DATA)System.err.println("rdRl: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        index = file.readInt();
if(DEBUG_DATA&&DEBUG_UNIT)System.err.println("rdRl: unit index (" + index + ")");
        u = (Unit )unitCache.get(index);
if(DEBUG_DATA&&!DEBUG_UNIT)System.err.println("rdRl: unit index (" + index + "=" + u + ")");
        break;
      case FLD_INDEX_ERROR:
if(DEBUG_DATA)System.err.println("rdRl: FLD_INDEX_ERROR (" + FLD_INDEX_ERROR + ")");
        index = file.readInt();
if(DEBUG_DATA&&DEBUG_ERRE)System.err.println("rdRl: error index (" + index + ")");
        error = (ErrorEstimate )errorCache.get(index);
if(DEBUG_DATA&&!DEBUG_ERRE)System.err.println("rdRl: error index (" + index + "=" + error + ")");
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdRl: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown Real directive " + directive);
      }
    }

    return new Real(rt, value, u, error);
  }

  private Real[] readRealArray()
    throws IOException, VisADException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdRlRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad Real array length " +
                            len + ")");
    }

    Real[] array = new Real[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Real )getData();
if(DEBUG_DATA_DETAIL)System.err.println("rdRlRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  private RealTuple readRealTuple()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdRlTpl: type index (" + typeIndex + ")");
    RealTupleType rtt = (RealTupleType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdRlTpl: type index (" + typeIndex + "=" + rtt + ")");

    Real[] components = null;
    double[] values = null;
    CoordinateSystem cs = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdRlTpl: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdRlTpl: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdRlTpl: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_REAL_SAMPLES:
if(DEBUG_DATA)System.err.println("rdRlTpl: FLD_REAL_SAMPLES (" + FLD_REAL_SAMPLES + ")");
        components = readRealArray();
        break;
      case FLD_TRIVIAL_SAMPLES:
if(DEBUG_DATA)System.err.println("rdRlTpl: FLD_TRIVIAL_SAMPLES (" + FLD_TRIVIAL_SAMPLES + ")");
        values = readDoubleArray();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdRlTpl: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealTuple directive " + directive);
      }
    }

    if (components != null && values != null) {
        throw new IOException("Found both RealTuple Real[] and double[] values");
    }

    if (values != null) {
      if (cs == null) {
        return new RealTuple(rtt, values);
      }

      // build a Real[] array from values,
      components = new Real[values.length];
      for (int i = 0; i < values.length; i++) {
        components[i] = new Real((RealType )rtt.getComponent(i), values[i],
                                 null, null);
      }
    }

    return new RealTuple(rtt, components, cs);
  }

  private RealTupleType readRealTupleType(int index)
    throws IOException, VisADException
  {
    int dim = file.readInt();
if(DEBUG_MATH)System.err.println("rdRlTuTy: dim (" + dim + ")");
    MathType[] mtList = readMathTypes(dim);

    RealType[] list = new RealType[mtList.length];
    for (int i = 0; i < mtList.length; i++) {
      list[i] = (RealType )mtList[i];
    }

    CoordinateSystem cs = null;
    boolean setFollowsType = false;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_INDEX_COORDSYS:
if(DEBUG_MATH)System.err.println("rdRlTuTy: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int csIndex = file.readInt();
if(DEBUG_MATH&&DEBUG_CSYS)System.err.println("rdRlTuTy: cSys index (" + csIndex + ")");
        cs = (CoordinateSystem )cSysCache.get(csIndex);
if(DEBUG_MATH&&!DEBUG_CSYS)System.err.println("rdRlTuTy: cSys index (" + csIndex + "=" + cs + ")");
        break;
      case FLD_SET_FOLLOWS_TYPE:
if(DEBUG_MATH)System.err.println("rdRlYuTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        setFollowsType = true;
        break;
      case FLD_END:
if(DEBUG_MATH)System.err.println("rdRlTuTy: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealTupleType directive " + directive);
      }
    }

    RealTupleType rtt = new RealTupleType(list, cs, null);

    typeCache.add(index, rtt);

    if (setFollowsType) {
      Set set = (Set )getData();
      try {
        rtt.setDefaultSet(set);
      } catch (TypeException te) {
        // ignore failure to set type
      }
    }

    return rtt;
  }

  private RealType readRealType(int index)
    throws IOException, VisADException
  {
    final int attrMask = file.readInt();
if(DEBUG_MATH)System.err.println("rdRlTy: attrMask (" + attrMask + ")");

    // read the name
    String name = readString();
if(DEBUG_MATH&&!DEBUG_STR)System.err.println("rdRlTy: name (" + name + ")");

    Unit u = null;
    boolean setFollowsType = false;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_INDEX_UNIT:
if(DEBUG_MATH)System.err.println("rdRlTy: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        int uIndex = file.readInt();
if(DEBUG_MATH&&DEBUG_UNIT)System.err.println("rdRlTy: unit index (" + index + ")");
        u = (Unit )unitCache.get(uIndex);
if(DEBUG_MATH&&!DEBUG_UNIT)System.err.println("rdRlTy: unit index (" + index + "=" + u + ")");
        break;
      case FLD_SET_FOLLOWS_TYPE:
if(DEBUG_MATH)System.err.println("rdRlTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        setFollowsType = true;
        break;
      case FLD_END:
if(DEBUG_MATH)System.err.println("rdRlTy: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealType directive " + directive);
      }
    }

    RealType rt = RealType.getRealType(name, u, null, attrMask);

    typeCache.add(index, rt);

    if (setFollowsType) {
      Set set = (Set )getData();
      try {
        rt.setDefaultSet(set);
      } catch (TypeException te) {
        // ignore failure to set type
      }
    }

    return rt;
  }

  private SampledSet[] readSampledSets()
    throws IOException, VisADException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdSplSetS: len (" + len + ")");
    SampledSet[] sets = new SampledSet[len];
    for (int i = 0; i < sets.length; i++) {
      sets[i] = (SampledSet )getData();
    }
    return sets;
  }

  private Object readSerializedObject()
    throws IOException
  {
    int len = file.readInt();
    if (len <= 0) {
      throw new IOException("Corrupted file (bad serialized object length)");
    }

    byte[] bytes = new byte[len];
    file.readFully(bytes);

    java.io.ByteArrayInputStream inBytes;
    inBytes = new java.io.ByteArrayInputStream(bytes);

    java.io.ObjectInputStream inStream;
    inStream = new java.io.ObjectInputStream(inBytes);

    Object obj;
    try {
      obj = inStream.readObject();
    } catch (ClassNotFoundException cnfe) {
      throw new IOException("Couldn't read serialized object: " +
                            cnfe.getMessage());
    }

    inStream.close();

    return obj;
  }

  private Set readSet()
    throws IOException, VisADException
  {
    byte serByte = file.readByte();
if(DEBUG_DATA)System.err.println("rdSet: read " + (serByte == OBJ_MATH_SERIAL ? "OBJ_MATH_SERIAL" : Integer.toString(serByte) + " (wanted OBJ_MATH_SERIAL)"));
    if (serByte != OBJ_MATH_SERIAL) {
      throw new VisADException("Invalid Set delimiter " + serByte);
    }

    return (Set )readSerializedObject();
  }

  private Set[] readSetArray()
    throws IOException, VisADException
  {
    final int len = file.readInt();
if(DEBUG_DATA)System.err.println("rdSetRA: len (" + len + ")");
    Set[] sets = new Set[len];
    for (int i = 0; i < sets.length; i++) {
      sets[i] = (Set )getData();
    }
    return sets;
  }

  private SetType readSetType(int index)
    throws IOException, VisADException
  {
    int dIndex = file.readInt();
if(DEBUG_MATH&&DEBUG_MATH)System.err.println("rdSetTy: domain index (" + dIndex + ")");
    MathType dom = (MathType )typeCache.get(dIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdSetTy: domain index (" + dIndex + "=" + dom + ")");

    byte endByte = file.readByte();
if(DEBUG_MATH)System.err.println("rdSetTy: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no SetType end-marker)");
    }

    SetType st = new SetType(dom);

    typeCache.add(index, st);

    return st;
  }

  private SimpleSet readSimpleSet(byte dataType)
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdSimSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdSimSet: type index (" + typeIndex + "=" + st + ")");

    CoordinateSystem cs = null;
    Unit[] units = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdSimSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdSimSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdSimSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdSimSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
if(DEBUG_DATA&&!DEBUG_UNIT){System.err.println("rdSimSet: array length ("+units.length+")");for(int i=0;i<units.length;i++)System.err.println("rdSimSet:    #"+i+" unit ("+units[i]+")");}
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdSimSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown SimpleSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for SimpleSet");
    }

    switch (dataType) {
    case DATA_FLOAT_SET:
      return new FloatSet(st, cs, units);
    case DATA_DOUBLE_SET:
      return new DoubleSet(st, cs, units);
    default:
      throw new IOException("Unknown SimpleSet type " + dataType);
    }
  }

  public SingletonSet readSingletonSet()
    throws IOException, VisADException
  {
    RealTuple sample = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SAMPLE:
        sample = (RealTuple )getData();
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_DATA)System.err.println("rdSglSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        int index = file.readInt();
if(DEBUG_DATA&&DEBUG_CSYS)System.err.println("rdSglSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("rdSglSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_DATA)System.err.println("rdSglSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = readUnits();
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_DATA)System.err.println("rdSglSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = readErrorEstimates();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdSglSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown SingletonSet directive " +
                              directive);
      }
    }

    if (sample == null) {
      throw new IOException("No sample found for SingletonSet");
    }

    return new SingletonSet(sample, cs, units, errs);
  }

  private String readString()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_STR)System.err.println("rdStr: len (" + len + ")");
    if (len < 0) {
      return null;
    } else if (len == 0) {
      return "";
    }

    char[] buf = new char[len];
    for (int i = 0; i < buf.length; i++) {
      buf[i] = file.readChar();
    }
if(DEBUG_STR)System.err.println("rdStr: str (" + buf + ")");

    return new String(buf);
  }

  private Text readText()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdTxt: type index (" + typeIndex + ")");
    TextType tt = (TextType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdTxt: type index (" + typeIndex + "=" + tt + ")");

    // read the value
    String value = readString();
if(DEBUG_DATA&&!DEBUG_STR)System.err.println("rdTxt: value (" + value + ")");

    byte endByte = file.readByte();
if(DEBUG_DATA)System.err.println("rdTxt: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no Text end-marker)");
    }

    return new Text(tt, value);
  }

  private TextType readTextType(int index)
    throws IOException, VisADException
  {
    // read the name
    String name = readString();
if(DEBUG_MATH&&!DEBUG_STR)System.err.println("rdTxTy: name (" + name + ")");

    byte endByte = file.readByte();
if(DEBUG_MATH)System.err.println("rdTxTy: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no TextType end-marker)");
    }

    TextType tt = TextType.getTextType(name);

    typeCache.add(index, tt);

    return tt;
  }

  private Tuple readTuple()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdTpl: type index (" + typeIndex + ")");
    TupleType tt = (TupleType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdTpl: type index (" + typeIndex + "=" + tt + ")");

    Data[] components = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_DATA_SAMPLES:
if(DEBUG_DATA)System.err.println("rdTpl: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        components = readDataArray();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdTpl: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown Tuple directive " + directive);
      }
    }

    return new Tuple(tt, components);
  }

  private TupleType readTupleType(int index)
    throws IOException, VisADException
  {
    int dim = file.readInt();
if(DEBUG_MATH)System.err.println("rdTuTy: dim (" + dim + ")");
    MathType[] list = readMathTypes(dim);

    byte endByte = file.readByte();
if(DEBUG_MATH)System.err.println("rdTuTy: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no TupleType end-marker)");
    }

    TupleType tt = new TupleType(list);

    typeCache.add(index, tt);

    return tt;
  }

  private UnionSet readUnionSet()
    throws IOException, VisADException
  {
    int typeIndex = file.readInt();
if(DEBUG_DATA&&DEBUG_MATH)System.err.println("rdUSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("rdUSet: type index (" + typeIndex + "=" + st + ")");

    SampledSet[] sets = null;

    boolean reading = true;
    while (reading) {
      byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET_SAMPLES:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_SET_SAMPLES (" + FLD_SET_SAMPLES + ")");
        sets = readSampledSets();
        break;
      case FLD_END:
if(DEBUG_DATA)System.err.println("rdLinSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown UnionSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for UnionSet");
    }
    if (sets == null) {
      throw new IOException("No sets found for UnionSet");
    }

    return new UnionSet(st, sets);
  }

  private Unit[] readUnits()
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_UNIT)System.err.println("rdUnits: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file" +
                            " (bad Unit array length " + len + ")");
    }

    Unit[] units = new Unit[len];
    for (int i = 0; i < len; i++) {
      int index = file.readInt();
if(DEBUG_UNIT)System.err.println("rdUnits:    #"+i+" index ("+index+")");

      if (index < 0) {
        units[i] = null;
      } else {
        units[i] = (Unit )unitCache.get(index);
      }
if(DEBUG_UNIT)System.err.println("rdUnits:    === #"+i+" Unit ("+units[i]+")");
    }

    return units;
  }
}
