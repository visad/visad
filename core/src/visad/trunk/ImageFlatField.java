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
import visad.util.ImageHelper;

/**
 * ImageFlatField is a VisAD FlatField backed by a java.awt.Image object,
 * instead of the usual float[][] or double[][] samples array.
 */
public class ImageFlatField extends FlatField {

  // -- Fields --

  /** The image backing this FlatField. */
  protected BufferedImage image;
  protected int width, height;


  // -- Constructors --

  public ImageFlatField(FunctionType type) throws VisADException {
    super(type);
  }

  public ImageFlatField(FunctionType type, Set domain_set)
                        throws VisADException {
    super(type, domain_set);
  }

  public ImageFlatField(FunctionType type, Set domain_set,
                        CoordinateSystem range_coord_sys, Set[] range_sets,
                        Unit[] units) throws VisADException {
    super(type, domain_set, range_coord_sys, range_sets, units);
  }

  public ImageFlatField(FunctionType type, Set domain_set,
                        CoordinateSystem[] range_coord_syses, Set[] range_sets,
                        Unit[] units) throws VisADException {
    super(type, domain_set, range_coord_syses, range_sets, units);
  }

  public ImageFlatField(FunctionType type, Set domain_set,
                        CoordinateSystem range_coord_sys,
                        CoordinateSystem[] range_coord_syses,
                        Set[] range_sets, Unit[] units)
          throws VisADException {
    super(type, domain_set, range_coord_sys,
      range_coord_syses, range_sets, units);
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
    ImageHelper ih = new ImageHelper();

    // determine image height and width
    width = -1;
    height = -1;
    while (true) {
      if (width < 0) width = image.getWidth(ih);
      if (height < 0) height = image.getHeight(ih);
      if (ih.badImage || (width >= 0 && height >= 0)) break;
      try { Thread.sleep(100); } catch (InterruptedException e) { }
    }
    if (ih.badImage) throw new VisADException("Not an image");

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

  protected double[][] unpackValues(boolean copy) throws VisADException {
    pr ("unpackValues(" + copy + ")");
    // copy flag is ignored
    int numPixels = width * height;
    int[] words = new int[numPixels];
    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);
    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    double[] redPix = new double[numPixels];
    double[] greenPix = new double[numPixels];
    double[] bluePix = new double[numPixels];
    for (int i=0; i<numPixels; i++) {
      redPix[i] = cm.getRed(words[i]);
      greenPix[i] = cm.getGreen(words[i]);
      bluePix[i] = cm.getBlue(words[i]);
    }

    double[][] samps = new double[3][];
    samps[0] = redPix;
    samps[1] = greenPix;
    samps[2] = bluePix;
    return samps;
  }

  protected float[][] unpackFloats(boolean copy) throws VisADException {
    pr ("unpackFloats(" + copy + ")");
    // copy flag is ignored
    int numPixels = width * height;
    int[] words = new int[numPixels];
    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);
    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    float[] redPix = new float[numPixels];
    float[] greenPix = new float[numPixels];
    float[] bluePix = new float[numPixels];
    for (int i=0; i<numPixels; i++) {
      redPix[i] = cm.getRed(words[i]);
      greenPix[i] = cm.getGreen(words[i]);
      bluePix[i] = cm.getBlue(words[i]);
    }

    float[][] samps = new float[3][];
    samps[0] = redPix;
    samps[1] = greenPix;
    samps[2] = bluePix;
    return samps;
  }

  protected double[] unpackValues(int s_index) throws VisADException {
    pr ("unpackValues(" + s_index + ")");
    int numPixels = width * height;
    int[] words = new int[numPixels];
    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);
    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    double[] samps = new double[3];
    samps[0] = cm.getRed(words[s_index]);
    samps[1] = cm.getGreen(words[s_index]);
    samps[2] = cm.getBlue(words[s_index]);
    return samps;
  }

  protected float[] unpackFloats(int s_index) throws VisADException {
    pr ("unpackFloats(" + s_index + ")");
    int numPixels = width * height;
    int[] words = new int[numPixels];
    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);
    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    float[] samps = new float[3];
    samps[0] = cm.getRed(words[s_index]);
    samps[1] = cm.getGreen(words[s_index]);
    samps[2] = cm.getBlue(words[s_index]);
    return samps;
  }

  protected double[] unpackOneRangeComp(int comp) throws VisADException {
    pr ("unpackOneRangeComp(" + comp + ")");
    int numPixels = width * height;
    int[] words = new int[numPixels];
    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);
    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    double[] samps = new double[numPixels];
    for (int i=0; i<numPixels; i++) {
      if (comp == 0) samps[i] = cm.getRed(words[i]);
      else if (comp == 1) samps[i] = cm.getGreen(words[i]);
      else if (comp == 2) samps[i] = cm.getBlue(words[i]);
    }
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
    for (int i=0; i<numPixels; i++) {
      redPix[i] = (byte) cm.getRed(words[i]);
      greenPix[i] = (byte) cm.getGreen(words[i]);
      bluePix[i] = (byte) cm.getBlue(words[i]);
    }

    byte[][] samps = new byte[3][];
    samps[0] = redPix;
    samps[1] = greenPix;
    samps[2] = bluePix;
    return samps;
  }

}
