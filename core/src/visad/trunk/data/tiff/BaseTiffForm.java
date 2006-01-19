//
// BaseTiffForm.java
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

package visad.data.tiff;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import visad.*;
import visad.data.*;
import visad.data.bio.OMEReader;
import visad.data.bio.OMETools;
import visad.java2d.DisplayImplJ2D;
import visad.util.*;

/**
 * BaseTiffForm is the superclass for VisAD data forms compatible with
 * or derived from the TIFF 6.0 file format.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public abstract class BaseTiffForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Fields --

  /** Filename of the current TIFF. */
  protected String currentId;

  /** Random access file for the current TIFF. */
  protected RandomAccessFile in;

  /** List of IFDs for the current TIFF. */
  protected Hashtable[] ifds;

  /** Number of images in the current TIFF stack. */
  protected int numImages;

  /** Hashtable containing metadata for the current TIFF. */
  protected Hashtable metadata;

  /** OME root node for OME-XML metadata. */
  protected Object ome;

  /** Percent complete with current operation. */
  protected double percent;


  // -- Constructor --

  /** Constructs a new BaseTiffForm. */
  public BaseTiffForm(String name) {
    super(name);
  }


  // -- Static BaseTiffForm API methods --

  /**
   * A utility method for test reading a file from the command line,
   * and displaying the results in a VisAD display.
   */
  public static void testRead(FormNode form, String format, String[] args)
    throws VisADException, IOException
  {
    String className = form.getClass().getName();
    if (args == null || args.length < 1) {
      System.out.println("To test read a file in " + format + " format, run:");
      System.out.println("  java " + className + " in_file");
      return;
    }
    String id = args[0];

    if (form instanceof FormFileInformer) {
      FormFileInformer infoForm = (FormFileInformer) form;

      // check type
      System.out.print("Checking " + format + " format ");
      System.out.println(infoForm.isThisType(id) ? "[yes]" : "[no]");
    }

    if (form instanceof MetadataReader) {
      MetadataReader metaForm = (MetadataReader) form;

      // read metadata
      System.out.print("Reading " + id + " metadata ");
      Hashtable meta = metaForm.getMetadata(id);
      System.out.println("[done]");

      // output metadata
      String[] keys = (String[]) meta.keySet().toArray(new String[0]);
      Arrays.sort(keys);
      for (int i=0; i<keys.length; i++) {
        System.out.print(keys[i] + ": ");
        System.out.print(metaForm.getMetadataValue(id, keys[i]) + "\n");
      }
      System.out.println();
    }

    if (form instanceof OMEReader) {
      OMEReader omeForm = (OMEReader) form;

      // output OME-XML
      Object root = null;
      try {
        root = omeForm.getOMENode(id);
      }
      catch (BadFormException exc) { }
      if (root == null) {
        System.out.println("OME-XML functionality not available " +
          "(package loci.ome.xml not installed)");
        System.out.println();
      }
      else {
        System.out.println(OMETools.dumpXML(root));
        System.out.println();
      }
    }

    // read pixels
    System.out.print("Reading " + id + " pixel data ");
    Data data = form.open(args[0]);
    System.out.println("[done]");
    System.out.println("MathType =\n" + data.getType());

    // extract types
    FunctionType ftype = (FunctionType) data.getType();
    RealTupleType domain = ftype.getDomain();
    RealType[] xy = domain.getRealComponents();
    RealType time = null;
    if (xy.length == 1) {
      // assume multiple images over time
      time = xy[0];
      ftype = (FunctionType) ftype.getRange();
      domain = ftype.getDomain();
      xy = domain.getRealComponents();
    }
    MathType range = ftype.getRange();
    RealType[] values = range instanceof RealType ?
      new RealType[] {(RealType) range} :
      ((RealTupleType) range).getRealComponents();

    // configure display
    DisplayImpl display = new DisplayImplJ2D("display");
    ScalarMap timeMap = null;
    if (time != null) {
      timeMap = new ScalarMap(time, Display.Animation);
      display.addMap(timeMap);
    }
    display.addMap(new ScalarMap(xy[0], Display.XAxis));
    display.addMap(new ScalarMap(xy[1], Display.YAxis));
    ScalarMap colorMap = null;
    if (values.length == 2 || values.length == 3) {
      // do separate mappings to Red, Green and Blue
      display.addMap(new ScalarMap(values[0], Display.Red));
      display.addMap(new ScalarMap(values[1], Display.Green));
      if (values.length == 3) {
        display.addMap(new ScalarMap(values[2], Display.Blue));
      }
    }
    else {
      // use the first component only
      colorMap = new ScalarMap(values[0], Display.RGB);
      display.addMap(colorMap);
    }
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(data);
    display.addReference(ref);
    display.getGraphicsModeControl().setScaleEnable(true);

    // pop up frame
    JFrame frame = new JFrame(format + " Results");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPanel p = new JPanel();
    frame.setContentPane(p);
    p.setLayout(new BorderLayout());
    p.add(display.getComponent());
    if (timeMap != null) {
      AnimationWidget aw = new AnimationWidget(timeMap);
      p.add(BorderLayout.SOUTH, aw);
    }
    if (colorMap != null) {
      RangeWidget rw = new RangeWidget(colorMap);
      LabeledColorWidget lcw = new LabeledColorWidget(colorMap);
      p.add(BorderLayout.NORTH, rw);
      p.add(BorderLayout.EAST, lcw);
    }
    frame.pack();
    frame.setLocation(300, 300);
    frame.show();
  }


  // -- BaseTiffForm API methods --

  /** Gets the dimensions of the given (possibly multi-page) TIFF file, */
  public int[] getTiffDimensions(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    if (ifds == null || ifds.length == 0) return null;
    return new int[] {
      TiffTools.getIFDIntValue(ifds[0], TiffTools.IMAGE_WIDTH, false, -1),
      TiffTools.getIFDIntValue(ifds[0], TiffTools.IMAGE_LENGTH, false, -1),
      numImages
    };
  }


  // -- Internal BaseTiffForm API methods --

  /** Initializes the given TIFF file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");
    ifds = TiffTools.getIFDs(in);
    if (ifds == null) throw new BadFormException("No IFDs found");
    numImages = ifds.length;
    metadata = new Hashtable();
    initMetadata();
  }

  /** Populates the metadata hashtable and OME root node. */
  protected void initMetadata() {
    initStandardMetadata();
    initOMEMetadata();
  }

  /** Parses standard metadata. */
  protected void initStandardMetadata() {
    Hashtable ifd = ifds[0];
    put("ImageWidth", ifd, TiffTools.IMAGE_WIDTH);
    put("ImageLength", ifd, TiffTools.IMAGE_LENGTH);

    put("BitsPerSample", ifd, TiffTools.BITS_PER_SAMPLE);

    int comp = TiffTools.getIFDIntValue(ifd, TiffTools.COMPRESSION);
    String compression = null;
    switch (comp) {
      case TiffTools.UNCOMPRESSED:
        compression = "None"; break;
      case TiffTools.CCITT_1D:
        compression = "CCITT Group 3 1-Dimensional Modified Huffman"; break;
      case TiffTools.GROUP_3_FAX:
        compression = "CCITT T.4 bilevel encoding"; break;
      case TiffTools.GROUP_4_FAX:
        compression = "CCITT T.6 bilevel encoding"; break;
      case TiffTools.LZW:
        compression = "LZW"; break;
      case TiffTools.JPEG:
        compression = "JPEG"; break;
      case TiffTools.PACK_BITS:
        compression = "PackBits"; break;
    }
    put("Compression", compression);

    int photo = TiffTools.getIFDIntValue(ifd,
      TiffTools.PHOTOMETRIC_INTERPRETATION);
    String photoInterp = null;
    switch (photo) {
      case TiffTools.WHITE_IS_ZERO:
        photoInterp = "WhiteIsZero"; break;
      case TiffTools.BLACK_IS_ZERO:
        photoInterp = "BlackIsZero"; break;
      case TiffTools.RGB:
        photoInterp = "RGB"; break;
      case TiffTools.RGB_PALETTE:
        photoInterp = "Palette"; break;
      case TiffTools.TRANSPARENCY_MASK:
        photoInterp = "Transparency Mask"; break;
      case TiffTools.CMYK:
        photoInterp = "CMYK"; break;
      case TiffTools.Y_CB_CR:
        photoInterp = "YCbCr"; break;
      case TiffTools.CIE_LAB:
        photoInterp = "CIELAB"; break;
    }
    put("PhotometricInterpretation", photoInterp);

    putInt("CellWidth", ifd, TiffTools.CELL_WIDTH);
    putInt("CellLength", ifd, TiffTools.CELL_LENGTH);
//      putInt("StripOffsets", ifd, TiffTools.STRIP_OFFSETS);

    int or = TiffTools.getIFDIntValue(ifd, TiffTools.ORIENTATION);
    String orientation = null;
    // there is no case 0
    switch (or) {
      case 1: orientation = "1st row -> top; 1st column -> left"; break;
      case 2: orientation = "1st row -> top; 1st column -> right"; break;
      case 3: orientation = "1st row -> bottom; 1st column -> right"; break;
      case 4: orientation = "1st row -> bottom; 1st column -> left"; break;
      case 5: orientation = "1st row -> left; 1st column -> top"; break;
      case 6: orientation = "1st row -> right; 1st column -> top"; break;
      case 7: orientation = "1st row -> right; 1st column -> bottom"; break;
      case 8: orientation = "1st row -> left; 1st column -> bottom"; break;
    }
    put("Orientation", orientation);

    putInt("SamplesPerPixel", ifd, TiffTools.SAMPLES_PER_PIXEL);
//      putInt("RowsPerStrip", ifd, TiffTools.ROWS_PER_STRIP);
//      putInt("StripByteCounts", ifd, TiffTools.STRIP_BYTE_COUNTS);
    put("Software", ifd, TiffTools.SOFTWARE);
    put("DateTime", ifd, TiffTools.DATE_TIME);
    put("Artist", ifd, TiffTools.ARTIST);

    put("HostComputer", ifd, TiffTools.HOST_COMPUTER);
    put("Copyright", ifd, TiffTools.COPYRIGHT);

    put("NewSubfileType", ifd, TiffTools.NEW_SUBFILE_TYPE);

    int thresh = TiffTools.getIFDIntValue(ifd, TiffTools.THRESHHOLDING);
    String threshholding = null;
    switch (thresh) {
      case 1: threshholding = "No dithering or halftoning"; break;
      case 2: threshholding = "Ordered dithering or halftoning"; break;
      case 3: threshholding = "Randomized error diffusion"; break;
    }
    put("Threshholding", threshholding);

    int fill = TiffTools.getIFDIntValue(ifd, TiffTools.FILL_ORDER);
    String fillOrder = null;
    switch (fill) {
      case 1:
        fillOrder = "Pixels with lower column values are stored " +
          "in the higher order bits of a byte";
        break;
      case 2:
        fillOrder = "Pixels with lower column values are stored " +
          "in the lower order bits of a byte";
        break;
    }
    put("FillOrder", fillOrder);

//      put("DocumentName", ifd, TiffTools.DOCUMENT_NAME);
//      put("ImageDescription", ifd, TiffTools.IMAGE_DESCRIPTION);
    putInt("Make", ifd, TiffTools.MAKE);
    putInt("Model", ifd, TiffTools.MODEL);
    putInt("MinSampleValue", ifd, TiffTools.MIN_SAMPLE_VALUE);
    putInt("MaxSampleValue", ifd, TiffTools.MAX_SAMPLE_VALUE);
    putInt("XResolution", ifd, TiffTools.X_RESOLUTION);
    putInt("YResolution", ifd, TiffTools.Y_RESOLUTION);

    int planar = TiffTools.getIFDIntValue(ifd,
      TiffTools.PLANAR_CONFIGURATION);
    String planarConfig = null;
    switch (planar) {
      case 1: planarConfig = "Chunky"; break;
      case 2: planarConfig = "Planar"; break;
    }
    put("PlanarConfiguration", planarConfig);

//      putInt("PageName", ifd, TiffTools.PAGE_NAME);
    putInt("XPosition", ifd, TiffTools.X_POSITION);
    putInt("YPosition", ifd, TiffTools.Y_POSITION);
    putInt("FreeOffsets", ifd, TiffTools.FREE_OFFSETS);
    putInt("FreeByteCounts", ifd, TiffTools.FREE_BYTE_COUNTS);
    putInt("GrayResponseUnit", ifd, TiffTools.GRAY_RESPONSE_UNIT);
    putInt("GrayResponseCurve", ifd, TiffTools.GRAY_RESPONSE_CURVE);
    putInt("T4Options", ifd, TiffTools.T4_OPTIONS);
    putInt("T6Options", ifd, TiffTools.T6_OPTIONS);

    int res = TiffTools.getIFDIntValue(ifd, TiffTools.RESOLUTION_UNIT);
    String resUnit = null;
    switch (res) {
      case 1: resUnit = "None"; break;
      case 2: resUnit = "Inch"; break;
      case 3: resUnit = "Centimeter"; break;
    }
    put("ResolutionUnit", resUnit);

    putInt("PageNumber", ifd, TiffTools.PAGE_NUMBER);
    putInt("TransferFunction", ifd, TiffTools.TRANSFER_FUNCTION);

    int predict = TiffTools.getIFDIntValue(ifd, TiffTools.PREDICTOR);
    String predictor = null;
    switch (predict) {
      case 1: predictor = "No prediction scheme"; break;
      case 2: predictor = "Horizontal differencing"; break;
    }
    put("Predictor", predictor);

    putInt("WhitePoint", ifd, TiffTools.WHITE_POINT);
    putInt("PrimaryChromacities", ifd, TiffTools.PRIMARY_CHROMATICITIES);
//      putInt("ColorMap", ifd, TiffTools.COLOR_MAP);
    putInt("HalftoneHints", ifd, TiffTools.HALFTONE_HINTS);
    putInt("TileWidth", ifd, TiffTools.TILE_WIDTH);
    putInt("TileLength", ifd, TiffTools.TILE_LENGTH);
    putInt("TileOffsets", ifd, TiffTools.TILE_OFFSETS);
    putInt("TileByteCounts", ifd, TiffTools.TILE_BYTE_COUNTS);

    int ink = TiffTools.getIFDIntValue(ifd, TiffTools.INK_SET);
    String inkSet = null;
    switch (ink) {
      case 1: inkSet = "CMYK"; break;
      case 2: inkSet = "Other"; break;
    }
    put("InkSet", inkSet);

    putInt("InkNames", ifd, TiffTools.INK_NAMES);
    putInt("NumberOfInks", ifd, TiffTools.NUMBER_OF_INKS);
    putInt("DotRange", ifd, TiffTools.DOT_RANGE);
    put("TargetPrinter", ifd, TiffTools.TARGET_PRINTER);
    putInt("ExtraSamples", ifd, TiffTools.EXTRA_SAMPLES);

    int format = TiffTools.getIFDIntValue(ifd, TiffTools.SAMPLE_FORMAT);
    String sampleFormat = null;
    switch (format) {
      case 1: sampleFormat = "unsigned integer"; break;
      case 2: sampleFormat = "two's complement signed integer"; break;
      case 3: sampleFormat = "IEEE floating point"; break;
      case 4: sampleFormat = "undefined"; break;
    }
    put("SampleFormat", sampleFormat);

    putInt("SMinSampleValue", ifd, TiffTools.S_MIN_SAMPLE_VALUE);
    putInt("SMaxSampleValue", ifd, TiffTools.S_MAX_SAMPLE_VALUE);
    putInt("TransferRange", ifd, TiffTools.TRANSFER_RANGE);

    int jpeg = TiffTools.getIFDIntValue(ifd, TiffTools.JPEG_PROC);
    String jpegProc = null;
    switch (jpeg) {
      case 1: jpegProc = "baseline sequential process"; break;
      case 14: jpegProc = "lossless process with Huffman coding"; break;
    }
    put("JPEGProc", jpegProc);

    putInt("JPEGInterchangeFormat", ifd, TiffTools.JPEG_INTERCHANGE_FORMAT);
    putInt("JPEGRestartInterval", ifd, TiffTools.JPEG_RESTART_INTERVAL);

    putInt("JPEGLosslessPredictors",
      ifd, TiffTools.JPEG_LOSSLESS_PREDICTORS);
    putInt("JPEGPointTransforms", ifd, TiffTools.JPEG_POINT_TRANSFORMS);
    putInt("JPEGQTables", ifd, TiffTools.JPEG_Q_TABLES);
    putInt("JPEGDCTables", ifd, TiffTools.JPEG_DC_TABLES);
    putInt("JPEGACTables", ifd, TiffTools.JPEG_AC_TABLES);
    putInt("YCbCrCoefficients", ifd, TiffTools.Y_CB_CR_COEFFICIENTS);

    int ycbcr = TiffTools.getIFDIntValue(ifd,
      TiffTools.Y_CB_CR_SUB_SAMPLING);
    String subSampling = null;
    switch (ycbcr) {
      case 1:
        subSampling = "chroma image dimensions = luma image dimensions";
        break;
      case 2:
        subSampling = "chroma image dimensions are " +
          "half the luma image dimensions";
        break;
      case 4:
        subSampling = "chroma image dimensions are " +
          "1/4 the luma image dimensions";
        break;
    }
    put("YCbCrSubSampling", subSampling);

    putInt("YCbCrPositioning", ifd, TiffTools.Y_CB_CR_POSITIONING);
    putInt("ReferenceBlackWhite", ifd, TiffTools.REFERENCE_BLACK_WHITE);
  }

  /** Parses OME-XML metadata. */
  protected void initOMEMetadata() {
    final String unknown = "unknown";
    Hashtable ifd = ifds[0];
    try {
      ome = OMETools.createRoot();
      if (ome == null) return; // OME-XML functionality is not available

      OMETools.setAttribute(ome, "Pixels", "SizeX", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.IMAGE_WIDTH));
      OMETools.setAttribute(ome, "Pixels", "SizeY", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.IMAGE_LENGTH));
      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" + 1);
      OMETools.setAttribute(ome, "Pixels", "SizeT", "" + ifds.length);
      OMETools.setAttribute(ome, "Pixels", "SizeC", "" + 1);
//      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" +
//        TiffTools.getIFDIntValue(ifd, TiffTools.X_RESOLUTION));
//      OMETools.setAttribute(ome, "Pixels", "SizeT", "" +
//        TiffTools.getIFDIntValue(ifd, TiffTools.Y_RESOLUTION));
//      OMETools.setAttribute(ome, "Pixels", "SizeC", "" +
//        TiffTools.getIFDIntValue(ifd, TiffTools.RESOLUTION_UNIT));
//      OMETools.setAttribute(ome, "Pixels", "DimensionOrder", "XYZTC");

      boolean little = TiffTools.isLittleEndian(ifd);
      OMETools.setAttribute(ome,
        "Pixels", "BigEndian", little ? "false" : "true");

      OMETools.setAttribute(ome, "Experimenter", "FirstName", "" +
        TiffTools.getIFDValue(ifd, TiffTools.ARTIST, false, String.class));
      OMETools.setAttribute(ome, "Experimenter", "LastName", "" +
        TiffTools.getIFDValue(ifd, TiffTools.ARTIST, false, String.class));

      String email = TiffTools.getIFDValue(ifd, TiffTools.ARTIST, false,
        String.class) + "@" + TiffTools.getIFDValue(ifd,
        TiffTools.HOST_COMPUTER, false, String.class);

      OMETools.setAttribute(ome, "Experimenter", "Email", email);

      OMETools.setAttribute(ome, "Group", "Name", "OME");

      OMETools.setAttribute(ome, "Image", "Description", "" +
        TiffTools.getIFDValue(ifd, TiffTools.IMAGE_DESCRIPTION,
        false, String.class));
//      OMETools.setAttribute(ome, "Image", "Name", "" +
//        TiffTools.getIFDIntValue(ifd, TiffTools.DOCUMENT_NAME, true, -1));
      OMETools.setAttribute(ome, "Image", "PixelSizeX", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.CELL_WIDTH, false, 0));
      OMETools.setAttribute(ome, "Image", "PixelSizeY", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.CELL_LENGTH, false, 0));
      OMETools.setAttribute(ome, "Image", "PixelSizeZ", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.ORIENTATION, false, 0));
      OMETools.setAttribute(ome, "Image", "Created", "" +
        TiffTools.getIFDValue(ifd, TiffTools.DATE_TIME, false, String.class));

      int sample = TiffTools.getIFDIntValue(ifd, TiffTools.SAMPLE_FORMAT);
      String pixelType;
      switch (sample) {
        case 1: pixelType = "int"; break;
        case 2: pixelType = "Uint"; break;
        case 3: pixelType = "float"; break;
        default: pixelType = unknown;
      }
      if (pixelType.indexOf("int") >= 0) { // int or Uint
        int bps = TiffTools.getIFDIntValue(ifd,
          TiffTools.BITS_PER_SAMPLE);
        pixelType += bps;
      }
      OMETools.setAttribute(ome, "Image", "PixelType", pixelType);

      OMETools.setAttribute(ome, "ChannelInfo", "SamplesPerPixel", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.SAMPLES_PER_PIXEL));

      int photoInterp2 = TiffTools.getIFDIntValue(ifd,
        TiffTools.PHOTOMETRIC_INTERPRETATION, true, 0);
      String photo2;
      switch (photoInterp2) {
        case 0: photo2 = "monochrome"; break;
        case 1: photo2 = "monochrome"; break;
        case 2: photo2 = "RGB"; break;
        case 3: photo2 = "monochrome"; break;
        case 4: photo2 = "RGB"; break;
        default: photo2 = unknown;
      }
      OMETools.setAttribute(ome, "ChannelInfo",
        "PhotometricInterpretation", photo2);

      OMETools.setAttribute(ome, "StageLabel", "X", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.X_POSITION));
      OMETools.setAttribute(ome, "StageLabel", "Y", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.Y_POSITION));

      OMETools.setAttribute(ome, "Instrument", "Model", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.MODEL));
      OMETools.setAttribute(ome, "Instrument", "SerialNumber", "" +
        TiffTools.getIFDIntValue(ifd, TiffTools.MAKE));
    }
    catch (BadFormException exc) { exc.printStackTrace(); }
  }


  // -- FormNode API methods --

  /**
   * Opens an existing TIFF-compatible file from the given filename.
   *
   * @return VisAD Data object containing TIFF data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    percent = 0;
    int nImages = getBlockCount(id);
    FieldImpl[] fields = new FieldImpl[nImages];
    for (int i=0; i<nImages; i++) {
      fields[i] = (FieldImpl) open(id, i);
      percent = (double) (i + 1) / nImages;
    }
    DataImpl data;
    if (nImages == 1) data = fields[0];
    else {
      // combine data stack into time function
      RealType time = RealType.getRealType("time");
      FunctionType timeFunction = new FunctionType(time, fields[0].getType());
      Integer1DSet timeSet = new Integer1DSet(nImages);
      FieldImpl timeField = new FieldImpl(timeFunction, timeSet);
      timeField.setSamples(fields, false);
      data = timeField;
    }
    close();
    percent = Double.NaN;
    return data;
  }

  /**
   * Saves a VisAD Data object to TIFF-compatible format at the given location.
   *
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("BaseTiffForm.save");
  }

  /**
   * Adds data to an existing TIFF-compatible file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("BaseTiffForm.add");
  }

  /**
   * Opens an existing TIFF-compatible file from the given URL.
   *
   * @return VisAD Data object containing Fluoview TIFF data.
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("BaseTiffForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given TIFF file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    return TiffTools.getImage(ifds[block_number], in);
  }

  /** Determines the number of images in the given TIFF file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return numImages;
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (in != null) in.close();
    in = null;
    ifds = null;
    currentId = null;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for a TIFF file. */
  public boolean isThisType(String name) {
    String s = name.toLowerCase();
    return s.endsWith(".tif") || s.endsWith(".tiff");
  }

  /** Checks if the given block is a valid header for a TIFF file. */
  public boolean isThisType(byte[] block) {
    return TiffTools.isValidHeader(block);
  }

  /** Returns the default file suffixes for the TIFF file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"tif", "tiff"};
  }


  // -- FormProgressInformer API methods --

  /** Gets the percentage complete of the form's current operation. */
  public double getPercentComplete() {
    return percent;
  }


  // -- MetadataReader API methods --

  /**
   * Obtains the specified metadata field's value for the given file.
   *
   * @param field the name associated with the metadata field
   * @return the value, or null should the field not exist
   */
  public Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata.get(field);
  }

  /**
   * Obtains a hashtable containing all metadata field/value pairs from
   * the given file.
   *
   * @param id the filename
   * @return the hashtable containing all metadata associated with the file
   */
  public Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata;
  }


  // -- OMEReader API methods --

  /**
   * Obtains a loci.ome.xml.OMENode object representing the
   * file's metadata as an OME-XML DOM structure.
   *
   * @throws BadFormException if the loci.ome.xml package is not present
   */
  public Object getOMENode(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    if (ome == null) {
      throw new BadFormException(
        "This functionality requires the LOCI OME-XML package " +
        "available at http://www.loci.wisc.edu/ome/");
    }
    return ome;
  }


  // -- Helper methods --

  protected void put(String key, Object value) {
    if (value == null) return;
    metadata.put(key, value);
  }

  protected void put(String key, int value) {
    if (value == -1) return; // indicates missing value
    metadata.put(key, new Integer(value));
  }

  protected void put(String key, boolean value) {
    put(key, new Boolean(value));
  }
  protected void put(String key, byte value) { put(key, new Byte(value)); }
  protected void put(String key, char value) {
    put(key, new Character(value));
  }
  protected void put(String key, double value) { put(key, new Double(value)); }
  protected void put(String key, float value) { put(key, new Float(value)); }
  protected void put(String key, long value) { put(key, new Long(value)); }
  protected void put(String key, short value) { put(key, new Short(value)); }

  protected void put(String key, Hashtable ifd, int tag) {
    put(key, TiffTools.getIFDValue(ifd, tag));
  }

  protected void putInt(String key, Hashtable ifd, int tag) {
    put(key, TiffTools.getIFDIntValue(ifd, tag));
  }

}
