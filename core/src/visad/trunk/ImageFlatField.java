//
// ImageFlatField.java
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

package visad;

import java.awt.image.*;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * ImageFlatField is a VisAD FlatField backed by a java.awt.Image object,
 * instead of the usual float[][] or double[][] samples array.
 */
public class ImageFlatField extends FlatField {

  // -- Fields --

  /** The image backing this FlatField. */
  protected BufferedImage image;

  /** Dimensions of the image. */
  protected int num, width, height;


  // -- Constructors --

  public ImageFlatField(FunctionType type) throws VisADException {
    this(type, type.getDomain().getDefaultSet(), null, null, null, null);
  }

  public ImageFlatField(FunctionType type, Set domain_set)
                        throws VisADException {
    this(type, domain_set, null, null, null, null);
  }

  public ImageFlatField(FunctionType type, Set domain_set,
                        CoordinateSystem range_coord_sys, Set[] range_sets,
                        Unit[] units) throws VisADException {
    this(type, domain_set, range_coord_sys, null, range_sets, units);
  }

  public ImageFlatField(FunctionType type, Set domain_set,
                        CoordinateSystem[] range_coord_syses, Set[] range_sets,
                        Unit[] units) throws VisADException {
    this(type, domain_set, null, range_coord_syses, range_sets, units);
  }

  public ImageFlatField(FunctionType type, Set domain_set,
                        CoordinateSystem range_coord_sys,
                        CoordinateSystem[] range_coord_syses,
                        Set[] range_sets, Unit[] units) throws VisADException {
    super(type, domain_set, range_coord_sys,
      range_coord_syses, range_sets, units);

    RealTupleType domain = type.getDomain();
    if (domain.getNumberOfRealComponents() != 2) {
      throw new VisADException(
        "FunctionType domain must be flat with 2 components");
    }
    MathType range = type.getRange();
    if (range instanceof RealType) num = 1;
    else if (range instanceof RealTupleType) {
      num = ((RealTupleType) range).getNumberOfRealComponents();
    }
    if (num != 1 && num != 3 && num != 4) {
      throw new VisADException(
        "FunctionType range must be flat with 1, 3 or 4 components");
    }
    if (domain_set instanceof Gridded2DSet) {
      int[] len = ((Gridded2DSet) domain_set).getLengths();
      width = len[0];
      height = len[1];
    }
    else {
      throw new VisADException(
        "Domain set must be Gridded2DSet");
    }
  }


  // -- ImageFlatField API methods --

  /** Gets the image backing this FlatField. */
  public BufferedImage getImage() {
    pr ("getImage");
    return image;
  }

  /** Sets the image backing this FlatField. */
  public void setImage(BufferedImage image) throws VisADException {
//    pr ("setImage");
    if (image == null) throw new VisADException("image cannot be null");
    if (image.getWidth() != width || image.getHeight() != height) {
      throw new VisADException("Image dimensions do not match domain set");
    }
    if (image.getRaster().getNumBands() != num) {
      throw new VisADException(
        "Image component count does not match FunctionType range");
    }
    this.image = image;
    clearMissing();
  }
 

  // -- FlatField API methods --

  public void setSamples(Data[] range, boolean copy)
    throws VisADException, RemoteException
  {
    throw new VisADException("Use setImage(Image) for ImageFlatField");
  }

  /**
   * This method has been overridden to avoid a call to
   * unpackValues or unpackFloats during range computation.
   */
  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
    throws VisADException
  {
    if (isMissing()) return shadow;

    ShadowRealTupleType domainType = ((ShadowFunctionType) type).getDomain();
    int n = domainType.getDimension();
    double[][] ranges = new double[2][n];
    // DomainSet.computeRanges handles Reference
    shadow = DomainSet.computeRanges(domainType, shadow, ranges, true);
    ShadowRealTupleType shadRef;
    // skip range if no range components are mapped
    int[] indices = ((ShadowFunctionType) type).getRangeDisplayIndices();
    boolean anyMapped = false;
    for (int i=0; i<TupleDimension; i++) {
      if (indices[i] >= 0) anyMapped = true;
    }
    if (!anyMapped) return shadow;

    // check for any range coordinate systems
    boolean anyRangeRef = (RangeCoordinateSystem != null);
    if (RangeCoordinateSystems != null) {
      for (int i=0; i<RangeCoordinateSystems.length; i++) {
        anyRangeRef |= (RangeCoordinateSystems[i] != null);
      }
    }
    ranges = anyRangeRef ? new double[2][TupleDimension] : null;

    // get image raster
    WritableRaster raster = image.getRaster();

    double[] min = new double[TupleDimension];
    Arrays.fill(min, Double.MAX_VALUE);
    double[] max = new double[TupleDimension];
    Arrays.fill(max, -Double.MAX_VALUE);
    double[] vals = new double[TupleDimension];
    for (int y=0; y<height; y++) {
      for (int x=0; x<width; x++) {
        raster.getPixel(x, y, vals);
        for (int i=0; i<TupleDimension; i++) {
          min[i] = Math.min(min[i], vals[i]);
          max[i] = Math.max(max[i], vals[i]);
        }
      }
    }
    for (int i=0; i<TupleDimension; i++) {
      int k = indices[i];
      if (k >= 0 || anyRangeRef) {
        Unit dunit = ((RealType) ((FunctionType)
          Type).getFlatRange().getComponent(i)).getDefaultUnit();
        if (dunit != null && !dunit.equals(RangeUnits[i])) {
          min[i] = dunit.toThis(min[i], RangeUnits[i]);
          max[i] = dunit.toThis(max[i], RangeUnits[i]);
        }
        if (anyRangeRef) {
          ranges[0][i] = Math.min(ranges[0][i], min[i]);
          ranges[1][i] = Math.max(ranges[1][i], max[i]);
        }
        if (k >= 0 && k < shadow.ranges[0].length) {
          shadow.ranges[0][k] = Math.min(shadow.ranges[0][k], min[i]);
          shadow.ranges[1][k] = Math.max(shadow.ranges[1][k], max[i]);
        }
      }
    }
    if (RangeCoordinateSystem != null) {
      // computeRanges for Reference (relative to range) RealTypes
      ShadowRealTupleType rangeType =
        (ShadowRealTupleType) ((ShadowFunctionType) type).getRange();
      shadRef = rangeType.getReference();
      shadow = computeReferenceRanges(rangeType, RangeCoordinateSystem,
        RangeUnits, shadow, shadRef, ranges);
    }
    else if (RangeCoordinateSystems != null) {
      TupleType rangeTupleType = (TupleType) ((FunctionType) Type).getRange();
      int j = 0;
      for (int i=0; i<RangeCoordinateSystems.length; i++) {
        MathType component = rangeTupleType.getComponent(i);
        if (component instanceof RealType) j++;
        else { // (component instanceof RealTupleType)
          int m = ((RealTupleType) component).getDimension();
          if (RangeCoordinateSystems[i] != null) {
            // computeRanges for Reference (relative to range
            // component) RealTypes
            double[][] subRanges = new double[2][m];
            Unit[] subUnits = new Unit[m];
            for (int k=0; k<m; k++) {
              subRanges[0][k] = ranges[0][j];
              subRanges[1][k] = ranges[1][j];
              subUnits[k] = RangeUnits[j];
              j++;
            }
            ShadowRealTupleType rangeType = (ShadowRealTupleType)
              ((ShadowTupleType) ((ShadowFunctionType)
              type).getRange()).getComponent(i);
            shadRef = rangeType.getReference();
            shadow = computeReferenceRanges(rangeType,
              RangeCoordinateSystems[i], subUnits,
              shadow, shadRef, subRanges);
          }
          else { // (RangeCoordinateSystems[i] == null)
            j += m;
          }
        } // end if (component instanceof RealTupleType)
      } // end for (int i=0; i<RangeCoordinateSystems.length; i++)
    } // end if (RangeCoordinateSystems != null)
    return shadow;
  }

  /**
   * Unpacks an array of doubles from field sample values.
   *
   * @param copy            Ignored (always returns a copy).
   */
  protected double[][] unpackValues(boolean copy) throws VisADException {
    pr ("unpackValues(" + copy + ")");
    // copy flag is ignored
    Raster r = image.getRaster();
    double[][] samps = new double[num][width * height];
    for (int c=0; c<num; c++) r.getSamples(0, 0, width, height, c, samps[c]);
    return samps;
  }

  /**
   * Unpacks an array of floats from field sample values.
   *
   * @param copy            Ignored (always returns a copy).
   */
  protected float[][] unpackFloats(boolean copy) throws VisADException {
    pr ("unpackFloats(" + copy + ")");
    // copy flag is ignored
    Raster r = image.getRaster();
    float[][] samps = new float[num][width * height];
    for (int c=0; c<num; c++) r.getSamples(0, 0, width, height, c, samps[c]);
    return samps;
  }

  protected double[] unpackValues(int s_index) throws VisADException {
    pr ("unpackValues(" + s_index + ")");
    Raster r = image.getRaster();
    double[] samps = new double[num];
    r.getPixel(s_index % width, s_index / width, samps);
    return samps;
  }

  protected float[] unpackFloats(int s_index) throws VisADException {
    pr ("unpackFloats(" + s_index + ")");
    Raster r = image.getRaster();
    float[] samps = new float[num];
    r.getPixel(s_index % width, s_index / width, samps);
    return samps;
  }

  protected double[] unpackOneRangeComp(int comp) throws VisADException {
    pr ("unpackOneRangeComp(" + comp + ")");
    Raster r = image.getRaster();
    double[] samps = new double[width * height];
    r.getSamples(0, 0, width, height, comp, samps);
    return samps;
  }

  protected void pr(String message) {
//    new Exception(hashCode () + " " + getClass().getName() +
//      "  " + message).printStackTrace();
  }


  // -- FlatFieldIface API methods --

  public void setSamples(double[][] range, boolean copy)
    throws RemoteException, VisADException
  {
    throw new VisADException("Use setImage(Image) for ImageFlatField");
  }

  public void setSamples(float[][] range, boolean copy)
    throws RemoteException, VisADException
  {
    throw new VisADException("Use setImage(Image) for ImageFlatField");
  }

  public void setSamples(double[][] range, ErrorEstimate[] errors,
    boolean copy) throws RemoteException, VisADException
  {
    throw new VisADException("Use setImage(Image) for ImageFlatField");
  }

  public void setSamples(int start, double[][] range)
    throws RemoteException, VisADException
  {
    throw new VisADException("Use setImage(Image) for ImageFlatField");
  }

  public void setSamples(float[][] range, ErrorEstimate[] errors, boolean copy)
    throws RemoteException, VisADException
  {
    throw new VisADException("Use setImage(Image) for ImageFlatField");
  }

  public byte[][] grabBytes() {
    pr ("grabBytes");
    WritableRaster raster = image.getRaster();
    if (raster.getTransferType() == DataBuffer.TYPE_BYTE) {
      DataBuffer buffer = raster.getDataBuffer();
      if (buffer instanceof DataBufferByte) {
        SampleModel model = raster.getSampleModel();
        if (model instanceof BandedSampleModel) {
          // fastest way to extract bytes; no copy
          byte[][] data = ((DataBufferByte) buffer).getBankData();
          return data;
        }
        else if (model instanceof ComponentSampleModel) {
          // medium speed way to extract bytes; direct array copy
          byte[][] data = ((DataBufferByte) buffer).getBankData();
          ComponentSampleModel csm = (ComponentSampleModel) model;
          int[] bandOffsets = csm.getBandOffsets();
          int[] bankIndices = csm.getBankIndices();
          int pixelStride = csm.getPixelStride();
          int scanlineStride = csm.getScanlineStride();
          int numBands = bandOffsets.length;
          int numPixels = width * height;
          byte[][] bytes = new byte[numBands][numPixels];
          for (int c=0; c<numBands; c++) {
            for (int h=0; h<height; h++) {
              for (int w=0; w<width; w++) {
                int ndx = width * h + w;
                int q = bandOffsets[c] + h * scanlineStride + w * pixelStride;
                bytes[c][ndx] = data[bankIndices[c]][q];
              }
            }
          }
          return bytes;
        } // model instanceof ComponentSampleModel
      } // buffer instanceof DataBufferByte
    } // raster.getTransferType() == DataBuffer.TYPE_BYTE

    // slower, more general way to extract bytes; use PixelGrabber
    int numPixels = width * height;
    int[] words = new int[numPixels];
    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);
    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    byte[] redPix = new byte[numPixels];
    byte[] greenPix = new byte[numPixels];
    byte[] bluePix = new byte[numPixels];
    byte[] alphaPix = new byte[numPixels];
    for (int i=0; i<numPixels; i++) {
      redPix[i] = (byte) cm.getRed(words[i]);
      greenPix[i] = (byte) cm.getGreen(words[i]);
      bluePix[i] = (byte) cm.getBlue(words[i]);
      alphaPix[i] = (byte) cm.getAlpha(words[i]);
    }
    return new byte[][] {redPix, greenPix, bluePix, alphaPix};
  }

}
