//
// AsciiArcGridAAdapter
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.gis;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.zip.GZIPInputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
import visad.*;
import visad.data.units.Parser;
import java.rmi.RemoteException;
import java.awt.geom.Rectangle2D;

/**
 * AsciiArcGridAdapter converts an ASCII ArcGrid file into
 * a VisAD Data object.
 * @author Don Murray
 */
public class ArcAsciiGridAdapter {


  /** Key for the western edge of the grid (lower left X position) */
  private static final String XLLCORNER = "XLLCORNER";

  /** Key for the southern edge of the grid (lower left Y position) */
  private static final String YLLCORNER = "YLLCORNER";

  /** Key for the center X position of the lower left grid cell */
  private static final String XLLCENTER = "XLLCENTER";

  /** Key for the center X position of the lower left grid cell */
  private static final String YLLCENTER = "YLLCENTER";

  /** Key for the number of columns in the grid */
  private static final String NCOLS = "NCOLS";

  /** Key for the number of rows in the grid */
  private static final String NROWS = "NROWS";

  /** Key for the resolution of the grid */
  private static final String CELLSIZE = "CELLSIZE";

  private static final String XCELLSIZE = "XCELLSIZE";

  private static final String YCELLSIZE = "YCELLSIZE";

  /** Alternate key for the missing data value */
  private static final String NODATA = "NODATA";

  /** Key for the missing data value */
  private static final String NODATA_VALUE = "NODATA_VALUE";

  /** Arrays of all the keys */
  private static final String[] KNOWN_KEYS = { XLLCORNER, YLLCORNER,
                                               XLLCENTER, YLLCENTER,
                                               NCOLS,     NROWS,
                                               CELLSIZE, XCELLSIZE, YCELLSIZE, NODATA, NODATA_VALUE };

  /** Default spatial type */
  public static final RealTupleType DEFAULT_SPATIAL_TYPE =
      RealTupleType.SpatialCartesian2DTuple;

  /** Default data type */
  public static final RealType DEFAULT_DATA_TYPE = RealType.Altitude;

  /** type for data */
  private RealTupleType spatialType = DEFAULT_SPATIAL_TYPE;

  /** type for data */
  private RealType dataType = DEFAULT_DATA_TYPE;

  /** unit for data */
  private Unit dataUnit = CommonUnit.meter;

  /** A BufferedReader used to read from ASCIIGRID files */
  private BufferedReader in;
  
  /** Number of rows of profiles in the ASCIIGRID */
  private int numRows;
  
  /** Number of columns in the ASCIIGRID */
  private int numColumns;
  
  /** Size of the grid cell along x axis*/
  private float cellSizeX;

  /** Size of the grid cell along y axis*/
  private float cellSizeY;
  
  /** lower left corner X position */
  private float xllCorner;
  
  /** lower left corner Y position */
  private float yllCorner;
  
  /** missing data value*/
  private float missingData = -9999f;   // seems to be the accepted default

  /** header table */
  private Hashtable headerTable;

  private String filename;
  private DecimalFormat formatter =  new DecimalFormat();
  private int numHeaderLines = 0;
  private float[][] rangeVals;
  private boolean readHeader = false;

  /**
   * Create an ArcAsciiGridAdapter for the particular file.
   * Data are assumed to be elevation values in meters
   *
   * @param filename  name of file to read
   * @throws VisADException couldn't create VisAD object
   */
  public ArcAsciiGridAdapter(String filename)
      throws VisADException {
    this(filename, DEFAULT_DATA_TYPE);
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the RealType specified for the data metadata.  Data are 
   * assumed to be in the default units of the RealType.
   * 
   * @param filename  name of file to read
   * @param dataType  RealType to use for range units
   * @throws VisADException couldn't create VisAD object
   */
  public ArcAsciiGridAdapter(String filename, RealType dataType)
      throws VisADException {
    this(filename, DEFAULT_SPATIAL_TYPE, dataType);
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the RealType specified for the data metadata.  Data are 
   * assumed to be in the default units of the RealType.
   * 
   * @param filename  name of file to read
   * @param spatialType  RealTupleType to use for spatial domain
   * @throws VisADException couldn't create VisAD object
   */
  public ArcAsciiGridAdapter(String filename, RealTupleType spatialType)
      throws VisADException {
    this(filename, spatialType, DEFAULT_DATA_TYPE);
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the RealType specified for the data metadata.  Data are 
   * assumed to be in the default units of the RealType.
   * 
   * @param filename  name of file to read
   * @param spatialType  RealTupleType to use for the spatial domain.
   * @param dataType  RealType to use for range units
   * @throws VisADException couldn't create VisAD object
   */
  public ArcAsciiGridAdapter(String filename, RealTupleType spatialType, 
                             RealType dataType) 
        throws VisADException {
    this(filename, spatialType, dataType, dataType.getDefaultUnit());
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use units specified for the data.  Data are assumed to be
   * altitudes.
   * 
   * @param filename  name of file to read
   * @param dataUnit  Unit of data 
   * @throws VisADException if unit is incompatible
   */
  public ArcAsciiGridAdapter(String filename, Unit dataUnit)
      throws VisADException {
    this(filename, DEFAULT_SPATIAL_TYPE, DEFAULT_DATA_TYPE, dataUnit);
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the supplied RealType for the data values, and units
   * if different from default from RealType.
   * 
   * @param filename  name of file to read
   * @param dataName  name to use for creating RealType
   * @throws VisADException if unit is incompatible, or problem with file
   */
  public ArcAsciiGridAdapter(String filename, String dataName)
      throws VisADException {
    this(filename, RealType.getRealType(dataName));
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the supplied RealType for the data values, and units
   * if different from default from RealType.
   * 
   * @param filename  name of file to read
   * @param dataName  name for the data
   * @param unitSpec  valid Unit specification
   * @throws VisADException if unit is incompatible, or problem with file
   */
  
  public ArcAsciiGridAdapter(String filename, String dataName, String unitSpec)
      throws VisADException {
    this(filename, RealType.getRealType(dataName, makeUnit(unitSpec)),
         makeUnit(unitSpec));
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the supplied RealType for the data values, and units
   * if different from default from RealType.
   * 
   * @param filename  name of file to read
   * @param dataType  RealType to use for range units
   * @param dataUnit  Unit of data if different from <code>dataType</code>
   *                  default units.
   * @throws VisADException if unit is incompatible, or problem with file
   */
  public ArcAsciiGridAdapter(String filename, RealType dataType, Unit dataUnit)
      throws VisADException {
    this(filename, DEFAULT_SPATIAL_TYPE, dataType, dataUnit);
  }

  /**
   * Create an ArcAsciiGridAdapter for the particular file and
   * use the supplied RealType for the data values, and units
   * if different from default from RealType.
   * 
   * @param filename  name of file to read
   * @param spatialType  RealTupleType to use for the spatial domain.
   * @param dataType  RealType to use for range units
   * @param dataUnit  Unit of data if different from <code>dataType</code>
   *                  default units.
   * @throws VisADException if unit is incompatible, or problem with file
   */
  public ArcAsciiGridAdapter(String filename, RealTupleType spatialType, 
                             RealType dataType, Unit dataUnit)
      throws VisADException {
    if (!Unit.canConvert(dataType.getDefaultUnit(), dataUnit))
      throw new VisADException("dataUnit incompatible with dataType");
    this.filename = filename;
    this.spatialType = spatialType;
    this.dataType = dataType;
    this.dataUnit = dataUnit;
    readHeader();
  }

  private void makeStream() throws VisADException {
    try {
      //ungzip
      if (filename.endsWith(".gz"))
        in = new BufferedReader(
          new InputStreamReader(
            new GZIPInputStream(new FileInputStream(filename))));
      else {
	  in = new BufferedReader(new FileReader(filename));
      }
    } catch (IOException e) {
      throw new VisADException("Couldn't open file: " + filename);
    }
  }

  private void readHeader() throws VisADException {
    if (readHeader) return;
    makeStream();
    headerTable = new Hashtable();
    boolean inHeader = true;
    String line;
    numHeaderLines = 0;

    try {
      while (inHeader) {
        if ((line = in.readLine()) != null) {
          StringTokenizer tok = new StringTokenizer(line);
          if (tok.countTokens() == 2) {
            String key = tok.nextToken().trim().toUpperCase();
            if (isKnownKey(key)) {
              String s = tok.nextToken().trim().toUpperCase();
              try {
                headerTable.put(key, new Float(parseValue(s)));
                numHeaderLines++;
              } catch (ParseException E) {
                throw new VisADException(
                    "Unable to parse value for key " + key + " " + s);
              }
            } else { // invalid key
              throw new VisADException("Unknown header key " + key);
            }
          } else { // too many tokens, into data
            inHeader = false;
          }
        } else { // null line
          inHeader = false;
        }
      }
      in.close();
    } catch (IOException ioe) {
      throw new VisADException("Problem reading in header line" + ioe);
    }
    if (!checkHeader()) {
      throw new VisADException("Unable to find enough metadata " + headerTable);
    }
    // System.out.println(headerTable.toString());
    readHeader = true;
  }


  private void readData(Gridded2DSet spatialSet) throws VisADException {
    if(rangeVals!=null) return;
    if (!readHeader) readHeader();
    rangeVals = new float[1][spatialSet.getLength()];
    makeStream();
    String line;
    try {
      long t1 = System.currentTimeMillis();
      //skip header
      for (int i = 0; i < numHeaderLines; i++) line = in.readLine();
      int index =0;
      float[]tmpArray =rangeVals[0];
      StreamTokenizer tok = new StreamTokenizer(in);
      for (int row = 0; row < numRows; row++) {
	  int colsRead = 0;
	  for (int col = 0; col < numColumns; col++) {
	      colsRead++;
	      int nextTok = tok.nextToken();
	      if(nextTok == StreamTokenizer.TT_EOF) break;
	      if(nextTok != StreamTokenizer.TT_NUMBER) {
		  throw new VisADException("Unknown value:" + tok.sval);
	      }
	      float value = (float)tok.nval;
	      if (value != missingData) {
		  tmpArray[index++] = value;
	      } else {
		  tmpArray[index++] = Float.NaN;
	      }	
	  }
          if (numColumns != colsRead) {
	      throw new VisADException(
				       "Number of values ("+ colsRead + 
				       ") < number of columns (" + numColumns + ")");
	      
	  }
      }
      if (index != numColumns*numRows) {
	  throw new VisADException("Number of values read (" + index +") != expected ("+ (numColumns* numRows));
	      
      }
      long t2 = System.currentTimeMillis();
      //      System.err.println("time:" + (t2-t1));
      in.close();
    } catch (IOException ioe) {
      throw new VisADException("Error reading data: " + ioe);
    }
    //System.out.println("minimum value = " + minimumValue);
    //System.out.println("maximum value = " + maximumValue);
  }





  private float parseValue(String value) 
      throws ParseException {
    return formatter.parse(value).floatValue();
  }

  private boolean isKnownKey(String key) {
    for (int i = 0; i < KNOWN_KEYS.length; i++) {
      if (KNOWN_KEYS[i].equals(key)) return true;
    } 
    return false;
  }

  private static Unit makeUnit(String unitSpec) throws VisADException {
    try {
      return Parser.parse(unitSpec);
    } catch (Exception e) {
      throw new VisADException("Invalid unit specification " + unitSpec);
    }
  }

  private boolean checkHeader() {
    boolean hasCellSize = headerTable.containsKey(CELLSIZE);
    if (!hasCellSize) {
        hasCellSize = headerTable.containsKey(XCELLSIZE) && headerTable.containsKey(YCELLSIZE);
    }      
    if (!(headerTable.containsKey(NCOLS) &&
          headerTable.containsKey(NROWS) &&
          hasCellSize)
       ) return false;
    numRows = ((Float)headerTable.get(NROWS)).intValue();
    numColumns = ((Float)headerTable.get(NCOLS)).intValue();
    if(headerTable.containsKey(CELLSIZE)) {
        cellSizeY = cellSizeX = ((Float)headerTable.get(CELLSIZE)).floatValue();
    } else {
        cellSizeX = ((Float)headerTable.get(XCELLSIZE)).floatValue();
        cellSizeY = ((Float)headerTable.get(YCELLSIZE)).floatValue();
    }
    if (headerTable.containsKey(NODATA)) {
       missingData = ((Float)headerTable.get(NODATA)).floatValue();
    } else if (headerTable.containsKey(NODATA_VALUE)) {
      missingData = ((Float)headerTable.get(NODATA_VALUE)).floatValue();
    } else {
      missingData = Float.NaN;
    }
    if (headerTable.containsKey(XLLCORNER) &&
        headerTable.containsKey(YLLCORNER)) {
      xllCorner = ((Float)headerTable.get(XLLCORNER)).floatValue();
      yllCorner = ((Float)headerTable.get(YLLCORNER)).floatValue();
    } else if (headerTable.containsKey(XLLCENTER) &&
               headerTable.containsKey(YLLCENTER)) {
      xllCorner = 
        ((Float)headerTable.get(XLLCENTER)).floatValue() - cellSizeX/2;
      yllCorner = 
        ((Float)headerTable.get(YLLCENTER)).floatValue() - cellSizeY/2;
    } else {
      return false;
    }
    return true;
  }

  private Linear2DSet makeSpatialSet() throws VisADException {
      return makeSpatialSet(getSpatialType());
  }

  private Linear2DSet makeSpatialSet(RealTupleType spatialType) 
        throws VisADException {
    if (!readHeader) readHeader();
    Linear2DSet spatialSet =
      new Linear2DSet(spatialType, 
                      xllCorner, xllCorner+(cellSizeX*(numColumns-1)), numColumns,
                      yllCorner+(cellSizeY*(numRows-1)), yllCorner, numRows,
                      (CoordinateSystem) null,
                      (Unit[]) null,
                      (ErrorEstimate[]) null,
                      true /*cache*/);
    //System.out.println("spatialSet = " + spatialSet);
    return spatialSet;
  }

  /**
   * Make a FlatField using the default data types
   */
  private FlatField makeFlatField() throws VisADException {
    return makeFlatField(getSpatialType(), getDataType());
  }

  /**
   * Make a FlatField using the specified MathType.
   * @param mathType  type to use.  If it's a FunctionType, it defines
   *                  the return objects function type.  If it's a
   *                  RealTupleType, it defines the spatial domain type,
   *                  if it's a RealType, it defines the data type.
   */
  private FlatField makeFlatField(MathType mathType) throws VisADException {

    if (mathType instanceof FunctionType) {
      FunctionType ft = (FunctionType) mathType;
      return makeFlatField(ft.getDomain(), (RealType)ft.getRange());
    } else if (mathType instanceof RealTupleType) {
      return makeFlatField((RealTupleType)mathType, getDataType());
    } else if (mathType instanceof RealType) {
      return makeFlatField(getSpatialType(), (RealType)mathType);
    } else {
      throw new VisADException("Unable to return data with type " + mathType);
    }
  }

  /**
   * Make a FlatField using the specified spatial domain and range types.
   */
  private FlatField makeFlatField(RealTupleType spatialType, RealType rangeType)
      throws VisADException {
    Gridded2DSet spatialSet = makeSpatialSet(spatialType);
    readData(spatialSet);  // if already done, will just return
    FunctionType ft = new FunctionType(spatialType, rangeType);
    FlatField ff = new FlatField(ft, spatialSet,
                                 (CoordinateSystem) null, 
                                 (Set[]) null,
                                 new Unit[] {dataUnit});
    try {
      ff.setSamples(rangeVals, false);
    } catch (RemoteException re) {} // can't happen
    return ff;
  }

  /**
   * Get the ASCIIGRID as a VisAD data object
   * @return a FlatField of type ((getSpatialType()) -> getDataType())
   */
  public FieldImpl getData() throws VisADException {
    return makeFlatField(getSpatialType(), getDataType());
  }
  
  /**
   * Get the ASCIIGRID as a VisAD data object with the specified spatial domain
   * and range.
   * @param spatialType  type for spatial domain
   * @param dataType   type for range
   * @return a FlatField of type ((spatialType) -> dataType)
   */
  public FieldImpl getData(RealTupleType spatialType, RealType dataType) 
      throws VisADException {
    return makeFlatField(spatialType, dataType);
  }
  
  /**
   * Get the ASCIIGRID as a VisAD data object with the specified domain
   * and range.
   * @param mathType  type to use.  If it's a FunctionType, it defines
   *                  the return objects function type.  If it's a
   *                  RealTupleType, it defines the domain type,
   *                  if it's a RealType, it defines the data type.
   * @return a FlatField based on <code>mathType</code>
   */
  public FieldImpl getData(MathType mathType)
      throws VisADException {
    return makeFlatField(mathType);
  }
  
  /**
   * Get the domain set for this DEM as a Longitude, Latitude set
   * @return Gridded2DSet for domain
   */
  public Gridded2DSet getSpatialSet() throws VisADException {
    return getSpatialSet(getSpatialType());
  }
  
  /**
   * Get the spatial domain set for this ASCIIGRID with the specified type.
   * @param spatialType  RealTupleType (dimension 2) to use for this domain
   * @return Gridded2DSet for spatial domain
   */
  public Gridded2DSet getSpatialSet(RealTupleType spatialType) 
      throws VisADException {
    return getSpatialSet(spatialType);
  }
  
  /**
   * Get the x value of the lower left corner of the grid.
   * @return x value of lower left corner.
   */
  public float getXLLCorner() {
    return xllCorner;
  }

  /**
   * Get the y value of the lower left corner of the grid.
   * @return y value of lower left corner.
   */
  public float getYLLCorner() {
    return yllCorner;
  }

  /**
   * Get the cell size of this grid
   * @return cell size
   * @deprecated Use getCellSizeX and getCellSizeY
   */
  public float getCellSize() {
    return cellSizeX;
  }

  /**
   * Get the cell size of this grid
   * @return cell size
   */
  public float getCellSizeX() {
    return cellSizeX;
  }

  /**
   * Get the cell size of this grid
   * @return cell size
   */
  public float getCellSizeY() {
    return cellSizeY;
  }

  /**
   * Get the missing data value for this grid
   * @return missing data value (or NaN if not specified)
   */
  public float getNoDataValue() {
    return missingData;
  }

  /**
   * Get the number of rows in this grid
   * @return number of rows
   */
  public int getRows() {
    return numRows;
  }

  /**
   * Get the number of columns in this grid
   * @return number of columns
   */
  public int getColumns() {
    return numColumns;
  }

  /**
   * Get the spatial domain type.
   * @return type of the spatial set.
   */
  public RealTupleType getSpatialType() {
    return spatialType;
  }

  /**
   * Set the spatial domain type.
   * @param newSpatialType  new type for 
   */
  public void setSpatialType(RealTupleType newSpatialType) {
    spatialType = newSpatialType;
  }

  /**
   * Set the range type.
   * @param newType  new type for range
   */
  public void setDataType(RealType newType) {
    dataType = newType;
    if (!Unit.canConvert(getDataUnit(), dataType.getDefaultUnit())) {
       dataUnit = dataType.getDefaultUnit();
    }
  }

  /**
   * Get the type of the Data.
   * @return type of the data
   */
  public RealType getDataType() {
    return dataType;
  }

  /**
   * Set the data units
   * @param newUnit new units for data
   */
  public void setDataUnit(Unit newUnit) {
    dataUnit = newUnit;
  }

  /**
   * Get the data units
   * @return units of the data
   */
  public Unit getDataUnit() {
    return dataUnit;
  }

  /**
   * Get the bounds of this grid
   * @return bounds as a rectangle.
   */
  public Rectangle2D getBounds() {
    return new Rectangle2D.Float(xllCorner, yllCorner, 
                                 cellSizeX*numColumns, cellSizeY*numRows);
  }

  /**
   * Return a string representation of this grid as constructed.
   * @return String representation:
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("File: ");
    buf.append(filename);
    buf.append("\n");
    buf.append("Cell size X");
    buf.append(getCellSizeX());
    buf.append("Cell size Y");
    buf.append(getCellSizeY());
    buf.append("\n");
    buf.append("Missing value: ");
    buf.append(getNoDataValue());
    buf.append("\n");
    buf.append("Bounds: x=");
    buf.append(getXLLCorner());
    buf.append(" y=");
    buf.append(getYLLCorner());
    buf.append(" width=");
    buf.append(getCellSizeX()*getColumns());
    buf.append(" height=");
    buf.append(getCellSizeY()*getRows());
    buf.append("\nData type: " );
    try {
      buf.append(new FunctionType(getSpatialType(), getDataType()));
    } catch (Exception excp) {
      buf.append(getSpatialType());
      buf.append(" -> ");
      buf.append(getDataType());
    }
    return buf.toString();

  }

  /** test this class "java visad.data.gis.ArcAsciiGridAdpater <filename>" */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("must supply Arc ASCIIGRID file name");
      System.exit(1);
    }
    ArcAsciiGridAdapter aga = 
      (args.length == 1) 
         ? new ArcAsciiGridAdapter(args[0])
         : (args.length == 2)
           ? new ArcAsciiGridAdapter(args[0], args[1])
           : new ArcAsciiGridAdapter(args[0], args[1], args[2]);
    System.out.println(aga);
    aga.makeFlatField();
  }

}
