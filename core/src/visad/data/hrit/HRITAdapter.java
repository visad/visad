//
// HRITAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.hrit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.wisc.ssec.mcidas.AREAnav;
import edu.wisc.ssec.mcidas.Calibrator;
import edu.wisc.ssec.mcidas.CalibratorException;
import edu.wisc.ssec.mcidas.CalibratorMsg;

import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.Unit;
import visad.VisADException;

import visad.util.Util;

/** 
 * This is an adapter for HRIT format data files 
 * At present, it will only work on MSG2 data, but with 
 * some work should be able to handle most HRIT data.
 */

public class HRITAdapter {

  private FlatField field = null;
  private static final int HEADER_TYPE_PRIMARY_HEADER = 0;
  private static final int HEADER_TYPE_IMAGE_STRUCTURE = 1;
  private static final int HEADER_TYPE_IMAGE_NAVIGATION = 2;
  private static final int PRIMARY_HEADER_LENGTH = 16;
  
  // record sizes for prologue file sections - used to offset to cal
  private static final int SAT_STAT_LEN = 60134;
  private static final int IMG_ACQ_LEN = 700;
  private static final int CEL_EVENTS_LEN = 326058;
  private static final int IMG_DESC_LEN = 101;
  private static final int CAL_OFFS = 72;
  
  private static final int SPACECRAFT_ID_MSG2 = 322;
  private static final int SPACECRAFT_ID_MSG3 = 323;

  /**
   * Create a VisAD FlatField from local HRIT file(s).  This constructor 
   * is included for backward compatibility but should not be used and 
   * should be phased out in future revisions since it does not make a 
   * valid band number determination.
   * 
   * @param filenames array of file names
   * @param magFactor magnification factor
   * @exception IOException if there was a problem reading the file(s).
   * @exception VisADException if an unexpected problem occurs.
   */
  
  public HRITAdapter(String [] filenames, int magFactor) 
  	throws IOException, VisADException
  {
	  this (
		 filenames, magFactor, Calibrator.CAL_BRIT, 1
	  );
  }

  /** 
   * Create a VisAD FlatField from local HRIT file(s).
   * @param filenames names of local files.
   * @param magFactor magnification factor
   * @param calType calibration type
   * @param bandNum band number
   * @exception IOException if there was a problem reading the file(s).
   * @exception VisADException if an unexpected problem occurs.
   */
  
  public HRITAdapter(String [] filenames, int magFactor, int calType, int bandNum)
	throws IOException, VisADException
  {
	  // set new mag factor if necessary
	  if ((magFactor != 1) &&
	      (magFactor != 2) &&
	      (magFactor != 4) &&
	      (magFactor != 8) &&
	      (magFactor != 16)) {
		  throw new VisADException("Invalid magnification factor for HRIT: " + magFactor);
	  }
	  
	  // Initial sanity checks on input file names
	  
	  // null parameter
	  if (filenames == null) {
		  throw new IOException("No filenames specified");
	  }
	  
	  // TODO: practical limit on number of input files?
	  
	  // HRIT filename syntax checks.  If those pass, do basic file integrity check
	  for (int i = 0; i < filenames.length; i++) {
		  
		  // have to do the null check here too, since a single array element could be
		  if (filenames[i] == null) {
			  throw new IOException("File name in array position " + (i + 1) + " is null");
		  }
		  
		  // TODO: determine the correct regular expression here
		  //if (! filenames[i].matches("IMG.*")) {
			//  throw new IOException("File: " + filenames[i] + " violates HRIT naming convention");
		  //}
		  
		  // make sure each file exists - there is almost no I/O overhead to do this check
		  File f = new File(filenames[i]);
		  if (! f.exists()) {
			  throw new IOException("File in array position " + (i + 1) + " does not exist");
		  }
		  
	  }
	  
	  // at this point we have file(s) that initially look ok, time to look closer
	  int [] imageSegmentLines = new int[filenames.length];
	  int [] imageSegmentElements = new int[filenames.length];
	  int [] imageBitsPerPixel = new int[filenames.length];
	  int [] lengthAllHeaders = new int[filenames.length];
	  int [] lineOffset = new int[filenames.length];
	  int minLineOffset = Integer.MAX_VALUE;
	  int columnOffset = -1;
	  int lineScalingFactor = -1;
	  int columnScalingFactor = -1;
	  
	  // only used if we find 10 bit data in a file
	  byte [] tenBitInputArray = null;
	  short [] tenBitOutputArray = null;
	  for (int i = 0; i < filenames.length; i++) {
		  
		  // open a stream to the file 
		  File f = new File(filenames[i]);
		  FileInputStream fis = new FileInputStream(f);
		  
		  // try to pull out the primary header
		  byte [] primaryHeader = new byte[PRIMARY_HEADER_LENGTH];
		  int bytesRead = fis.read(primaryHeader);
		  if ((bytesRead < 0) || (bytesRead != PRIMARY_HEADER_LENGTH)) {
			  fis.close();
			  throw new IOException("File " + filenames[i] + " is not an HRIT file");
		  }
		  
		  // validate primary header contents
		  int headerSize = bytesToShort(primaryHeader, 1);
		  if (headerSize != PRIMARY_HEADER_LENGTH) {
			  fis.close();
			  throw new IOException("File " + filenames[i] + " is not a valid HRIT file");
		  }
		  
		  // make sure file is at least as long as the claimed length of all headers
		  lengthAllHeaders[i] = bytesToInt(primaryHeader, 4);
		  if (f.length() < lengthAllHeaders[i]) {
			  fis.close();
			  throw new IOException("File " + filenames[i] + " is not a valid HRIT file");
		  }
		  
		  // dumpHeader(primaryHeader);
		  // ok, we got the primary header, moving along to the other headers...
		  int headerBytesConsumed = PRIMARY_HEADER_LENGTH;
		  byte [] headerType = new byte[1];
		  byte [] headerLength = new byte[2];
		  while (headerBytesConsumed < lengthAllHeaders[i]) {
			  bytesRead = fis.read(headerType);
			  headerBytesConsumed += bytesRead;
			  bytesRead = fis.read(headerLength);
			  headerBytesConsumed += bytesRead;
			  headerSize = bytesToShort(headerLength, 0);
			  byte [] header = new byte[headerSize - 3];
			  bytesRead = fis.read(header);
			  headerBytesConsumed += bytesRead;
			  // System.out.println("Header type: " + unsignedByteToInt(headerType[0]));
			  // System.out.println("Length of this header: " + headerSize);
			  // for image structure headers, pull out image size
			  if (Util.unsignedByteToInt(headerType[0]) == HEADER_TYPE_IMAGE_STRUCTURE) {
				  imageSegmentLines[i] = bytesToShort(header, 3);
				  imageSegmentElements[i] = bytesToShort(header, 1);
				  imageBitsPerPixel[i] = Util.unsignedByteToInt(header[0]);
				  // System.out.println("Image bits per pixel: " + imageBitsPerPixel[i]);
				  // System.out.println("Image #Lines: " + imageSegmentLines[i] + ", #Elements: " + imageSegmentElements[i]);
			  }
			  // for navigation headers, print relevant data
			  if (Util.unsignedByteToInt(headerType[0]) == HEADER_TYPE_IMAGE_NAVIGATION) {
				  String projectionName = new String(header, 0, 32);
				  projectionName = projectionName.trim();
				  columnScalingFactor = bytesToInt(header, 32);
				  if (columnScalingFactor < 0) columnScalingFactor = -columnScalingFactor;
				  lineScalingFactor = bytesToInt(header, 36);
				  if (lineScalingFactor < 0) lineScalingFactor = -lineScalingFactor;
				  columnOffset = bytesToInt(header, 40);
				  lineOffset[i] = bytesToInt(header, 44);
				  // keep track of minimum line offset seen
				  if (minLineOffset > lineOffset[i]) {
					  minLineOffset = lineOffset[i];
				  }
				  // System.out.println("Projection: " + projectionName + 
				  //	  ", lsf: " + lineScalingFactor + ", csf: " + columnScalingFactor +
				  //	  ", co: " + columnOffset + ", lo: " + lineOffset[i]);
			  }
		  }
		  
		  fis.close();
	  }
	  
	  // make the VisAD RealTypes for the dimension variables
	  RealType line = RealType.getRealType("ImageLine", null, null);
	  RealType element = RealType.getRealType("ImageElement", null, null);
	  
	  // the domain is (element,line) since elements (X) vary fastest
	  RealType[] domainComponents = {element, line};
	  int resMultiplier = 3;
	  if (filenames[0].contains("HRV")) {
		  resMultiplier = 1;
	  }
	  int [] iparms = new int[6];
	  iparms[0] = AREAnav.GEOS;
	  iparms[1] = columnOffset * resMultiplier * 10;
	  iparms[2] = columnOffset * resMultiplier * 10;	  
	  iparms[3] = lineScalingFactor * resMultiplier * 10;
	  iparms[4] = columnScalingFactor * resMultiplier * 10;
	  // XXX FIXME TJJ - hardcoding for now: 0 for MSG.  Should be able
	  // to pull this out of the signal/segment data
	  iparms[5] = 0;
	  int [] dir = new int[64];
	  //dir[5] = resMultiplier * minLineOffset;
	  dir[5] = resMultiplier * (minLineOffset - 464) + 5568 + 1;
	  if (filenames[0].contains("HRV")) {
		  //dir[6] = 11136 - ((resMultiplier * columnOffset) + 5568);
		  dir[6] = columnOffset + 1;
	  } else {
		  dir[6] = 1;
	  }
	  dir[8] = imageSegmentLines[0] * filenames.length;
	  dir[9] = imageSegmentElements[0];
	  dir[11] = resMultiplier;
	  dir[12] = resMultiplier;
	  CoordinateSystem cs = new HRITCoordinateSystem(iparms, dir, false);
	  RealTupleType imageDomain = new RealTupleType(domainComponents, cs, null);
	  
	  // create calibration object
	  double [][] calBlock = makeMSGCal(filenames[0]);
	  CalibratorMsg cmsg = null;
	  try {
	    cmsg = new CalibratorMsg(calBlock);
	  } catch (CalibratorException ce) {
		ce.printStackTrace();
	  }
	  
	  //  Image numbering is usually the first line is at the "top"
	  //  whereas in VisAD, it is at the bottom.  So define the
	  //  domain set of the FlatField to map the Y axis accordingly

	  Linear2DSet domainSet = new Linear2DSet(imageDomain,
	                                0, (imageSegmentElements[0] - 1), imageSegmentElements[0] / magFactor,
	                                ((imageSegmentLines[0] * filenames.length) - 1), 
	                                0, (imageSegmentLines[0] * filenames.length) / magFactor);
	  // the range of the FunctionType is the band(s)
	  int numBands = 1;
	  RealType[] bands = new RealType[numBands];
	  bands[0] = RealType.getRealType("Band" + bandNum);
	  RealTupleType rtt = new RealTupleType(bands);
	  FunctionType imageType = new FunctionType(imageDomain, rtt);
	  Unit[] rangeUnits = null;
	  field = new FlatField (
		imageType,
        domainSet,
        (CoordinateSystem) null, null,
        rangeUnits
      );
	  
	  for (int i = 0; i < filenames.length; i++) {
		  
		  // open a stream to the file 
		  File f = new File(filenames[i]);
		  FileInputStream fis = new FileInputStream(f);
		  fis.skip(lengthAllHeaders[i]);
		  
		  // if we found 10 bit data, we'll need to allocate input and output arrays to decompress
		  tenBitInputArray = new byte[(int) f.length() - lengthAllHeaders[i] + 2];
		  tenBitOutputArray = new short[imageSegmentLines[i] * imageSegmentElements[i]];
		  
		  double[][] samples = new double[numBands][imageSegmentElements[i]/magFactor * imageSegmentLines[i]/magFactor];
		  byte[] sampleTwoByte = new byte[2];
		  byte[] sampleOneByte = new byte[1];
		  
		  // set samples for one or two byte data
		  if (imageBitsPerPixel[i] != 10) {
			  for (int b = 0; b < numBands; b++) {
				  for (int l=0; l < imageSegmentLines[i]; l++) {
					  for (int j=0; j < imageSegmentElements[i]; j++) {
						  if (imageBitsPerPixel[i] == 16) {
							  fis.read(sampleTwoByte);
							  samples[b][j + (imageSegmentElements[i] * l) ] = 
								  (float) (bytesToShort(sampleTwoByte, 0));
						  } else {
							  fis.read(sampleOneByte);
							  samples[b][j + (imageSegmentElements[i] * l) ] = 
								  (float) (Util.unsignedByteToInt(sampleOneByte[0]));
						  }
					  }
				  }
			  }
		  } else {
			  int numRead = fis.read(tenBitInputArray, 0, tenBitInputArray.length - 2);
			  // System.out.println("Count wanted: " + (tenBitInputArray.length - 2) + " , count got: " + numRead);
			  if (numRead == tenBitInputArray.length - 2) {
				int convert = Util.tenBitToTwoByte(tenBitInputArray, tenBitOutputArray);
				if (convert == 0) {
				// System.out.println("10 bit to 16 bit conversion successful!");
					  int idx = 0;
					  for (int b = 0; b < numBands; b++) {
						  for (int l = imageSegmentLines[i]/magFactor - 1; l >= 0; l--) {
							  for (int j = imageSegmentElements[i]/magFactor - 1; j >= 0; j--) {
								  samples[b][j + ((imageSegmentElements[i]/magFactor) * l) ] = 
									  cmsg.calibrateFromRaw((float) (tenBitOutputArray[idx]), bandNum, calType);
								  idx += magFactor;
							  }
							  idx += imageSegmentElements[i] * (magFactor - 1);
						  }
					  }
				}
			  }
		  }
		  field.setSamples((samples[0].length * (filenames.length - (i + 1))), samples);
		  fis.close();
		  
	  }
	  
  }
  
  /**
   * Attempt to build a McIDAS-style calibration block.
   * If unsuccessful, a warning is popped up that calibration 
   * will be approximated, and should not be considered accurate.
   * @param s an image segment file name for the data request
   * @return a McIDAS-style calibration block
   */
  
  private double[][] makeMSGCal(String s) {
	  
	  double [][] msgCal = new double[12][6];
	  double [] waveNumMSG1  = new double[12];
	  double [] waveNumMSG2  = new double[12];
	  double [] waveNumMSG3  = new double[12];
	  double [] alphaMSG1    = new double[12];
	  double [] alphaMSG2    = new double[12];
	  double [] alphaMSG3    = new double[12];
	  double [] betaMSG1     = new double[12];
	  double [] betaMSG2     = new double[12];
	  double [] betaMSG3     = new double[12];
	  double [] gain     = new double[12];
	  double [] offset   = new double[12];

	  // various constants.  I know this doesn't look good... for now we are
	  // only covering MSG-1 and MSG-2.  Needs work, but at present this is
	  // no different than the core McIDAS and ADDE server code!
	  waveNumMSG1[0] = 0.0d;
	  waveNumMSG1[1] = 0.0d;
	  waveNumMSG1[2] = 0.0d;
	  waveNumMSG1[3] = 2567.33d;
	  waveNumMSG1[4] = 1598.103d;
	  waveNumMSG1[5] = 1362.081d;
	  waveNumMSG1[6] = 1149.069d;
	  waveNumMSG1[7] = 1034.343d;
	  waveNumMSG1[8] = 930.647d;
	  waveNumMSG1[9] = 839.660d;
	  waveNumMSG1[10] = 752.387d;
	  waveNumMSG1[11] = 0.0d;
	  
	  waveNumMSG2[0] = 0.0d;
	  waveNumMSG2[1] = 0.0d;
	  waveNumMSG2[2] = 0.0d;
	  waveNumMSG2[3] = 2568.832d;
	  waveNumMSG2[4] = 1600.548d;
	  waveNumMSG2[5] = 1360.330d;
	  waveNumMSG2[6] = 1148.620d;
	  waveNumMSG2[7] = 1035.289d;
	  waveNumMSG2[8] = 931.700d;
	  waveNumMSG2[9] = 836.445d;
	  waveNumMSG2[10] = 751.792d;
	  waveNumMSG2[11] = 0.0d;
	  
	  waveNumMSG3[0] = 0.0d;
	  waveNumMSG3[1] = 0.0d;
	  waveNumMSG3[2] = 0.0d;
	  waveNumMSG3[3] = 2547.771d;
	  waveNumMSG3[4] = 1595.621d;
	  waveNumMSG3[5] = 1360.377d;
	  waveNumMSG3[6] = 1148.130d;
	  waveNumMSG3[7] = 1034.715d;
	  waveNumMSG3[8] = 929.842d;
	  waveNumMSG3[9] = 838.659d;
	  waveNumMSG3[10] = 751.792d;
	  waveNumMSG3[11] = 0.0d;

	  alphaMSG1[0] = 0.0d;
	  alphaMSG1[1] = 0.0d;
	  alphaMSG1[2] = 0.0d;
	  alphaMSG1[3] = 0.9956d;
	  alphaMSG1[4] = 0.9962d;
	  alphaMSG1[5] = 0.9991d;
	  alphaMSG1[6] = 0.9996d;
	  alphaMSG1[7] = 0.9999d;
	  alphaMSG1[8] = 0.9983d;
	  alphaMSG1[9] = 0.9988d;
	  alphaMSG1[10] = 0.9981d;
	  alphaMSG1[11] = 0.0d;
	  
	  alphaMSG2[0] = 0.0d;
	  alphaMSG2[1] = 0.0d;
	  alphaMSG2[2] = 0.0d;
	  alphaMSG2[3] = 0.9954d;
	  alphaMSG2[4] = 0.9963d;
	  alphaMSG2[5] = 0.9991d;
	  alphaMSG2[6] = 0.9996d;
	  alphaMSG2[7] = 0.9999d;
	  alphaMSG2[8] = 0.9983d;
	  alphaMSG2[9] = 0.9988d;
	  alphaMSG2[10] = 0.9981d;
	  alphaMSG2[11] = 0.0d;
	  
	  alphaMSG3[0] = 0.0d;
	  alphaMSG3[1] = 0.0d;
	  alphaMSG3[2] = 0.0d;
	  alphaMSG3[3] = 0.9915d;
	  alphaMSG3[4] = 0.9960d;
	  alphaMSG3[5] = 0.9991d;
	  alphaMSG3[6] = 0.9996d;
	  alphaMSG3[7] = 0.9999d;
	  alphaMSG3[8] = 0.9983d;
	  alphaMSG3[9] = 0.9988d;
	  alphaMSG3[10] = 0.9982d;
	  alphaMSG3[11] = 0.0d;

	  betaMSG1[0] = 0.0d;
	  betaMSG1[1] = 0.0d;
	  betaMSG1[2] = 0.0d;
	  betaMSG1[3] = 3.410d;
	  betaMSG1[4] = 2.218d;
	  betaMSG1[5] = 0.478d;
	  betaMSG1[6] = 0.179d;
	  betaMSG1[7] = 0.060d;
	  betaMSG1[8] = 0.625d;
	  betaMSG1[9] = 0.397d;
	  betaMSG1[10] = 0.578d;
	  betaMSG1[11] = 0.0d;
	  
	  betaMSG2[0] = 0.0d;
	  betaMSG2[1] = 0.0d;
	  betaMSG2[2] = 0.0d;
	  betaMSG2[3] = 3.438d;
	  betaMSG2[4] = 2.185d;
	  betaMSG2[5] = 0.470d;
	  betaMSG2[6] = 0.179d;
	  betaMSG2[7] = 0.056d;
	  betaMSG2[8] = 0.640d;
	  betaMSG2[9] = 0.408d;
	  betaMSG2[10] = 0.561d;
	  betaMSG2[11] = 0.0d;
	  
	  betaMSG3[0] = 0.0d;
	  betaMSG3[1] = 0.0d;
	  betaMSG3[2] = 0.0d;
	  betaMSG3[3] = 2.9002d;
	  betaMSG3[4] = 2.0337d;
	  betaMSG3[5] = 0.4340d;
	  betaMSG3[6] = 0.1714d;
	  betaMSG3[7] = 0.0527d;
	  betaMSG3[8] = 0.6084d;
	  betaMSG3[9] = 0.3882d;
	  betaMSG3[10] = 0.5390d;
	  betaMSG3[11] = 0.0d;
	  
	  // initialize with approximate values - this will get you a
	  // pretty picture but should not be considered accurate
	  gain[0] = 0.2331010000E-01d;
	  gain[1] = 0.2540430000E-01d;
	  gain[2] = 0.2187850000E-01d;
	  gain[3] = 0.3742751227E-02d;
	  gain[4] = 0.4641033727E-01d;
	  gain[5] = 0.8197182308E-01d;
	  gain[6] = 0.1256206112E+00d;
	  gain[7] = 0.1523276370E+00d;
	  gain[8] = 0.1959369086E+00d;
	  gain[9] = 0.2145945762E+00d;
	  gain[10] = 0.2091678681E+00d;
	  gain[11] = 0.2800300000E-01d;
	  
	  offset[0] = -0.1188810000E+01d;
	  offset[1] = -0.1295620000E+01d;
	  offset[2] = -0.1115800000E+01d;
	  offset[3] = -0.1908803126E+00d;
	  offset[4] = -0.2366927201E+01d;
	  offset[5] = -0.4180562977E+01d;
	  offset[6] = -0.6406651170E+01d;
	  offset[7] = -0.7768709489E+01d;
	  offset[8] = -0.9992782340E+01d;
	  offset[9] = -0.1094432338E+02d;
	  offset[10] = -0.1066756128E+02d;
	  offset[11] = -0.1428150000E+01d;	 
	  
	  // for now, assume we calibrate based on MSG-2, unless we detect otherwise
	  int scId = SPACECRAFT_ID_MSG2;
	  
	  // now the real work - try to convert the image segment file name
	  // to an MSG prologue file name (file with the cal slopes and offsets)
	  boolean accurateCal = false;
	  // to build the filename for the matching prologue file, swap out channel and segment
	  // number sections with underscores and -PRO
	  String plFileName = s.replaceFirst("......___-0000\\d\\d___", "_________-PRO______");
	  File f = new File(plFileName);
	  try {

		  FileInputStream fis = new FileInputStream(f);
		  // try to pull out the primary header
		  byte [] primaryHeader = new byte[PRIMARY_HEADER_LENGTH];
		  int bytesRead = fis.read(primaryHeader);
		  if ((bytesRead < 0) || (bytesRead != PRIMARY_HEADER_LENGTH)) {
			  fis.close();
			  throw new IOException("File " + s + " is not an HRIT file");
		  }
		  // validate primary header contents
		  int headerSize = bytesToShort(primaryHeader, 1);
		  if (headerSize != PRIMARY_HEADER_LENGTH) {
			  fis.close();
			  throw new IOException("File " + s + " is not a valid HRIT file");
		  }
		  // make sure file is at least as long as the claimed length of all headers
		  int lengthAllHeaders = -1;
		  lengthAllHeaders = bytesToInt(primaryHeader, 4);
		  if (f.length() < lengthAllHeaders) {
			  fis.close();
			  throw new IOException("File " + s + " is not a valid HRIT file");
		  }
		  // ok, we got the primary header, moving along to the other headers...
		  int headerBytesConsumed = PRIMARY_HEADER_LENGTH;
		  byte [] headerType = new byte[1];
		  byte [] headerLength = new byte[2];
		  while (headerBytesConsumed < lengthAllHeaders) {
			  bytesRead = fis.read(headerType);
			  headerBytesConsumed += bytesRead;
			  bytesRead = fis.read(headerLength);
			  headerBytesConsumed += bytesRead;
			  headerSize = bytesToShort(headerLength, 0);
			  byte [] header = new byte[headerSize - 3];
			  bytesRead = fis.read(header);
			  headerBytesConsumed += bytesRead;
		  }
		  // two-byte utility array for pulling out shorts
		  byte [] b2 = new byte[2];
		  // spacecraft id - will be used to further improve cal, as time permits
		  fis.read(b2);
		  scId = bytesToShort(b2, 0);
		  long n = fis.skip((SAT_STAT_LEN - 2) + IMG_ACQ_LEN + CEL_EVENTS_LEN + IMG_DESC_LEN + CAL_OFFS);
		  if (n != (SAT_STAT_LEN - 2) + IMG_ACQ_LEN + CEL_EVENTS_LEN + IMG_DESC_LEN + CAL_OFFS) {
			  fis.close();
			  throw new IOException("Failed to read calibration coefficients, corrupt file?");
		  }
		  for (int i = 0; i < 12; i++) {
			  byte [] d1 = new byte[8];
			  byte [] d2 = new byte[8];
			  int count = fis.read(d1);
			  if (count != 8) {
				  fis.close();
				  throw new IOException("Failed to read calibration coefficients, corrupt file?");
			  }
			  count = fis.read(d2);
			  if (count != 8) {
				  fis.close();
				  throw new IOException("Failed to read calibration coefficients, corrupt file?");
			  }
			  long l1 = bytesToLong(d1, 0);
			  long l2 = bytesToLong(d2, 0);
			  gain[i] = Double.longBitsToDouble(l1);
			  offset[i] = Double.longBitsToDouble(l2);
			  // TODO: should probably add a sanity check on gain/offset values,
			  // to make sure we found and will be using reasonable numbers.
		  }
		  // if we got this far, assume we have accurate calibration coefficients
		  accurateCal = true;
		  fis.close();

	  } catch (FileNotFoundException e) {
		  // Do nothing - we just won't have accurate calibration
	  } catch (IOException e) {
		  // Do nothing - we just won't have accurate calibration
	  }
	  
	  if (! accurateCal) {
		  System.err.println("WARNING: Data will be displayed, but calibration is approximate.");
	  }

	  double w  = 0.0d;
	  double c1w3 = 0.0d;
	  double c2w = 0.0d;
	  double PLANCK = 6.626176E-34;
	  double LIGHT  = 2.99792458E8;
	  double BOLTZMAN = 1.380662E-23;
	  double c1 = 2.0E5 * PLANCK * (LIGHT * LIGHT);
	  double c2 = PLANCK * LIGHT / BOLTZMAN;
	  for (int band = 0; band < 12; band++) {	 
		  if (scId == SPACECRAFT_ID_MSG2) {
			  w  = 1.0E2 * waveNumMSG2[band];
			  msgCal[band][2] = alphaMSG2[band];
			  msgCal[band][3] = betaMSG2[band];
		  } else if (scId == SPACECRAFT_ID_MSG3) {
			  w  = 1.0E2 * waveNumMSG3[band];
			  msgCal[band][2] = alphaMSG3[band];
			  msgCal[band][3] = betaMSG3[band];
		  } else {
		      w  = 1.0E2 * waveNumMSG1[band];
		      msgCal[band][2] = alphaMSG1[band];
		      msgCal[band][3] = betaMSG1[band];
		  }
	      c1w3 = c1 * w * w * w;
	      c2w  = c2 * w;
	      msgCal[band][0] = c1w3;
	      msgCal[band][1] = c2w;
	      msgCal[band][4] = gain[band];
	      msgCal[band][5] = offset[band];
	  }
	  
	  return msgCal;
}

  private void dumpHeader(byte [] header) {
	  if ((header != null) && (header.length >= 3)) {
		  System.out.println("Header type: " + header[0]);
		  System.out.println("Length of this header: " + bytesToShort(header, 1));
	  }

	  switch (header[0]) {
	  case HEADER_TYPE_PRIMARY_HEADER:
		  System.out.println("Length of all headers: " + bytesToInt(header, 4));
		  break;
	  default:
		  break;
	  }
  }

  public FlatField getData() {
	  return field;
  }
  
  /**
   * Converts a set of four consecutive bytes into a single long.
   * @param data An array of bytes
   * @param offset The array index to begin with
   * @return The resulting int
   */
  public static long bytesToLong(byte[] data, int offset) {
          long l = 0;
          l += Util.unsignedByteToLong(data[offset + 0]) << 56;
          l += Util.unsignedByteToLong(data[offset + 1]) << 48;
          l += Util.unsignedByteToLong(data[offset + 2]) << 40;
          l += Util.unsignedByteToLong(data[offset + 3]) << 32;
          l += Util.unsignedByteToLong(data[offset + 4]) << 24;
          l += Util.unsignedByteToLong(data[offset + 5]) << 16;
          l += Util.unsignedByteToLong(data[offset + 6]) << 8;
          l += Util.unsignedByteToLong(data[offset + 7]);
          return l;
  }

  /**
   * Converts a set of four consecutive bytes into a single int.
   * @param data An array of bytes
   * @param offset The array index to begin with
   * @return The resulting int
   */
  public static int bytesToInt(byte[] data, int offset) {
          int i = 0;
          i += Util.unsignedByteToInt(data[offset]) << 24;
          i += Util.unsignedByteToInt(data[offset + 1]) << 16;
          i += Util.unsignedByteToInt(data[offset + 2]) << 8;
          i += Util.unsignedByteToInt(data[offset + 3]);
          return i;
  }

  /**
   * Converts a set of two consecutive bytes into a single int.
   * @param data An array of bytes
   * @param offset The array index to begin with
   * @return The resulting int
   */
  public static int bytesToShort(byte[] data, int offset) {
          int i = 0;
          i += Util.unsignedByteToInt(data[offset]) << 8;
          i += Util.unsignedByteToInt(data[offset + 1]);
          return i;
  }
  
}
