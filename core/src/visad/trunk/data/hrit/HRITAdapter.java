//
// HRITAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import java.io.IOException;

import edu.wisc.ssec.mcidas.AREAnav;
import edu.wisc.ssec.mcidas.CalibratorException;
import edu.wisc.ssec.mcidas.CalibratorMsg;

import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;

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
  private int magFactor = 1;

  /** Create a VisAD FlatField from local HRIT file(s).
    * @param filenames names of local files.
    * @exception IOException if there was a problem reading the file(s).
    * @exception VisADException if an unexpected problem occurs.
    */
  public HRITAdapter(String [] filenames, int magFactor)
	throws IOException, VisADException
  {
	  // set new mag factor if necessary
	  if ((magFactor == 1) ||
	      (magFactor == 2) ||
	      (magFactor == 4) ||
	      (magFactor == 8) ||
	      (magFactor == 16)) {
		  this.magFactor = magFactor;
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
			  throw new IOException("File " + filenames[i] + " is not an HRIT file");
		  }
		  
		  // validate primary header contents
		  int headerSize = bytesToShort(primaryHeader, 1);
		  if (headerSize != PRIMARY_HEADER_LENGTH) {
			  throw new IOException("File " + filenames[i] + " is not a valid HRIT file");
		  }
		  
		  // make sure file is at least as long as the claimed length of all headers
		  lengthAllHeaders[i] = bytesToInt(primaryHeader, 4);
		  if (f.length() < lengthAllHeaders[i]) {
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
			  if (unsignedByteToInt(headerType[0]) == HEADER_TYPE_IMAGE_STRUCTURE) {
				  imageSegmentLines[i] = bytesToShort(header, 3);
				  imageSegmentElements[i] = bytesToShort(header, 1);
				  imageBitsPerPixel[i] = unsignedByteToInt(header[0]);
				  // System.out.println("Image bits per pixel: " + imageBitsPerPixel[i]);
				  // System.out.println("Image #Lines: " + imageSegmentLines[i] + ", #Elements: " + imageSegmentElements[i]);
			  }
			  // for navigation headers, print relevant data
			  if (unsignedByteToInt(headerType[0]) == HEADER_TYPE_IMAGE_NAVIGATION) {
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
	  iparms[1] = 55720;
	  iparms[2] = 55720;
	  iparms[3] = lineScalingFactor * resMultiplier * 10;
	  iparms[4] = columnScalingFactor * resMultiplier * 10;
	  // hardcoding for now: 1400 for MTSAT, 0 for MSG
	  // iparms[5] = 1400;
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
	  double [][] calBlock = makeMSGCal();
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
	  bands[0] = RealType.getRealType("Band" + 1);
	  RealTupleType radiance = new RealTupleType(bands);
	  FunctionType imageType = new FunctionType(imageDomain, radiance);
	  Set[] rangeSets = new Set[numBands];
	  rangeSets[0] = new Integer1DSet(bands[0], 255);
	  Unit calUnit = null;
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
								  (float) (unsignedByteToInt(sampleOneByte[0]));
						  }
					  }
				  }
			  }
		  } else {
			  int numRead = fis.read(tenBitInputArray, 0, tenBitInputArray.length - 2);
			  // System.out.println("Count wanted: " + (tenBitInputArray.length - 2) + " , count got: " + numRead);
			  if (numRead == tenBitInputArray.length - 2) {
				int convert = tenBitToTwoByte(tenBitInputArray, tenBitOutputArray);
				if (convert == 0) {
				// System.out.println("10 bit to 16 bit conversion successful!");
					  int idx = 0;
					  for (int b = 0; b < numBands; b++) {
						  for (int l = imageSegmentLines[i]/magFactor - 1; l >= 0; l--) {
							  for (int j = imageSegmentElements[i]/magFactor - 1; j >= 0; j--) {
								  samples[b][j + ((imageSegmentElements[i]/magFactor) * l) ] = 
									  cmsg.calibrateFromRaw((float) (tenBitOutputArray[idx]), 1, CalibratorMsg.CAL_BRIT);
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
  
  private double[][] makeMSGCal() {
	  double [][] msgCal = new double[12][6];
	  double [] waveNum  = new double[12];
	  double [] alpha    = new double[12];
	  double [] beta     = new double[12];
	  double [] gain     = new double[12];
	  double [] offset   = new double[12];
	  
	  waveNum[0] = 0.0d;
	  waveNum[1] = 0.0d;
	  waveNum[2] = 0.0d;
	  waveNum[3] = 2568.832d;
	  waveNum[4] = 1600.548d;
	  waveNum[5] = 1360.330d;
	  waveNum[6] = 1148.620d;
	  waveNum[7] = 1035.289d;
	  waveNum[8] = 931.700d;
	  waveNum[9] = 836.445d;
	  waveNum[10] = 751.792d;
	  waveNum[11] = 0.0d;
	  
	  alpha[0] = 0.0d;
	  alpha[1] = 0.0d;
	  alpha[2] = 0.0d;
	  alpha[3] = 0.9954d;
	  alpha[4] = 0.9963d;
	  alpha[5] = 0.9991d;
	  alpha[6] = 0.9996d;
	  alpha[7] = 0.9999d;
	  alpha[8] = 0.9983d;
	  alpha[9] = 0.9988d;
	  alpha[10] = 0.9981d;
	  alpha[11] = 0.0d;
	  
	  beta[0] = 0.0d;
	  beta[1] = 0.0d;
	  beta[2] = 0.0d;
	  beta[3] = 3.438d;
	  beta[4] = 2.185d;
	  beta[5] = 0.470d;
	  beta[6] = 0.179d;
	  beta[7] = 0.056d;
	  beta[8] = 0.640d;
	  beta[9] = 0.408d;
	  beta[10] = 0.561d;
	  beta[11] = 0.0d;
	  
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
	  
	  double c1 = 0.0d;
	  double c2 = 0.0d;
	  double w  = 0.0d;
	  double c1w3 = 0.0d;
	  double c2w = 0.0d;
	  double PLANCK = 6.626176E-34;
	  double LIGHT  = 2.99792458E8;
	  double BOLTZMAN = 1.380662E-23;
	  for (int band = 0; band < 12; band++) {
	      c1 = 2.0E5 * PLANCK * (LIGHT*LIGHT);
	      c2 = PLANCK * LIGHT / BOLTZMAN;
	      w  = 1.0E2 * waveNum[band];
	      c1w3 = c1*w*w*w;
	      c2w  = c2*w;
	      msgCal[band][0] = c1w3;
	      msgCal[band][1] = c2w;
	      msgCal[band][2] = alpha[band];
	      msgCal[band][3] = beta[band];
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
   * Converts a set of four consecutive bytes into a single int.
   * @param data An array of bytes
   * @param offset The array index to begin with
   * @return The resulting int
   */
  public static int bytesToInt(byte[] data, int offset) {
          int i = 0;
          i += unsignedByteToInt(data[offset]) << 24;
          i += unsignedByteToInt(data[offset + 1]) << 16;
          i += unsignedByteToInt(data[offset + 2]) << 8;
          i += unsignedByteToInt(data[offset + 3]);
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
          i += unsignedByteToInt(data[offset]) << 8;
          i += unsignedByteToInt(data[offset + 1]);
          return i;
  }

  /**
   * Converts an (unsigned) byte to an unsigned int.
   * Since Java doesn't have an unsigned
   * byte type, this requires some foolery.
   * This solution based on information and code from
   * http://www.rgagnon.com/javadetails/java-0026.html
   * @param b The unsigned byte to convert
   * @return the unsigned int equivalent
   */
  public static int unsignedByteToInt(byte b) {
          return (int) b & 0xFF;
  }
  
  /**
   * 
   * @param input
   * @param output
   * @return 0 if no errors
   */
  public static int tenBitToTwoByte(byte [] input, short [] output) {

	  int total = output.length;
	  int index = 0;
	  int temp = 0;
	  int skip = 0;
	  int outputIndex = 0;

	  index = skip / 8;
	  skip = skip % 8;
	  //input = (unsigned char *) inp;

	  while (total > 0)
	  {
		  total--;

		  /*
		   * enumerated to avoid the more general need
		   * to always access 3 bytes
		   * which in reality is needed only for case 7
		   */
		  switch (skip)
		  {
		  case 0:
			  temp = 4 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF) / 64;
			  break;
		  case 1:
			  temp = 8 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF) / 32;
			  break;
		  case 2:
			  temp = 16 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF) / 16;
			  break;
		  case 3:
			  temp = 32 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF) / 8;
			  break;
		  case 4:
			  temp = 64 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF) / 4;
			  break;
		  case 5:
			  temp = 128 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF) / 2;
			  break;
		  case 6:
			  temp = 256 * (int) (input[index] & 0xFF) +  (int) (input[index + 1] & 0xFF);
			  break;
		  case 7:
			  temp = 512 *(1& (int) (input[index] & 0xFF)) + 2 * (int) (input[index + 1] & 0xFF)
			                                        + ( (int) (input[index + 2] & 0xFF) > 127 ? 1 : 0);
			  break;
		  }

		  output[outputIndex] = (short) (temp & 0x3ff);
		  outputIndex++;

		  /*
		   * these two statements together increment 10 bits on the input
		   */
		  index++;
		  skip += 2;

		  /*
		   * now normalize skip
		   */
		  if (skip > 7)
		  {
			  index++;
			  skip -= 8;
		  }
	  }

	  return 0;
  }
  
}
