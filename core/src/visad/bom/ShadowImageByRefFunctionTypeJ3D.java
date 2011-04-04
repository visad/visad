//
// ShadowImageByRefFunctionTypeJ3D.java
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

package visad.bom;

import visad.*;
import visad.data.CachedBufferedByteImage;
import visad.java3d.*;
import visad.data.mcidas.BaseMapAdapter;
import visad.data.mcidas.AreaAdapter;
import visad.data.gif.GIFForm;
import visad.util.Util;
import visad.util.ThreadManager;

import javax.media.j3d.*;

import java.io.*;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.rmi.*;

import java.net.URL;

import java.awt.event.*;
import javax.swing.*;
import java.awt.color.*;
import java.awt.image.*;

/**
   The ShadowImageFunctionTypeJ3D class shadows the FunctionType class for
   ImageRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowImageByRefFunctionTypeJ3D extends ShadowFunctionTypeJ3D {

  private static final int MISSING1 = Byte.MIN_VALUE;      // least byte

  private VisADImageNode imgNode = null;
  private VisADImageNode prevImgNode = null;
  private int prevDataWidth = -1;
  private int prevDataHeight = -1;
  private int prevNumImages = -1;

  //- Ghansham (New variables introduced to preserve scaled values and colorTables)
  private byte scaled_Bytes[][];  //scaled byte values 
  private float scaled_Floats[][];  //scaled Float Values

  private byte[][] itable; //For single band
  private byte[][][] threeD_itable; //for multiband

  private float[][] color_values; //special case
  private boolean first_time; //This variable indicates the first tile of the image.
  //------------------------------------------------------------------------------

  AnimationControlJ3D animControl = null;

  private boolean reuse = false;
  private boolean reuseImages = false;

  int[] inherited_values = null;
  ShadowFunctionOrSetType adaptedShadowType = null;
  int levelOfDifficulty = -1;

  public ShadowImageByRefFunctionTypeJ3D(MathType t, DataDisplayLink link, ShadowType parent) 
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public ShadowImageByRefFunctionTypeJ3D(MathType t, DataDisplayLink link, ShadowType parent,
                  int[] inherited_values, ShadowFunctionOrSetType adaptedShadowType, int levelOfDifficulty)
         throws VisADException, RemoteException {
    super(t, link, parent);
    this.inherited_values  = inherited_values;
    this.adaptedShadowType = adaptedShadowType;
    this.levelOfDifficulty = levelOfDifficulty;
  }

  // transform data into a depiction under group
  public boolean doTransform(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    DataDisplayLink link = renderer.getLink();

    // return if data is missing or no ScalarMaps
    if (data.isMissing()) {
      ((ImageRendererJ3D) renderer).markMissingVisADBranch();
      return false;
    }

    if (levelOfDifficulty == -1) {
      levelOfDifficulty = getLevelOfDifficulty();
    }

    if (levelOfDifficulty == NOTHING_MAPPED) return false;


    if (group instanceof BranchGroup && ((BranchGroup) group).numChildren() > 0) {
       Node g = ((BranchGroup) group).getChild(0);
        // WLH 06 Feb 06 - support switch in a branch group.
        if (g instanceof BranchGroup && ((BranchGroup) g).numChildren() > 0) {
            reuseImages = true;
        }
    }

    DisplayImpl display = getDisplay();

    int cMapCurveSize = (int)
       default_values[display.getDisplayScalarIndex(Display.CurvedSize)];

    int curved_size =
       (cMapCurveSize > 0)
          ? cMapCurveSize
          : display.getGraphicsModeControl().getCurvedSize();
  

     prevImgNode = ((ImageRendererJ3D)renderer).getImageNode();

     BranchGroup bgImages = null;

     if (!reuse) {

       BranchGroup branch = new BranchGroup();
       branch.setCapability(BranchGroup.ALLOW_DETACH);
       branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
       branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
       branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
       Switch swit = (Switch) makeSwitch();

       imgNode = new VisADImageNode();

       bgImages = new BranchGroup();
       bgImages.setCapability(BranchGroup.ALLOW_DETACH);
       bgImages.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
       bgImages.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
       bgImages.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

       swit.addChild(bgImages);
       swit.setWhichChild(0);

       branch.addChild(swit);

       imgNode.setBranch(branch);
       imgNode.setSwitch(swit);
       ((ImageRendererJ3D)renderer).setImageNode(imgNode);

       if ( ((BranchGroup) group).numChildren() > 0 ) {
         ((BranchGroup)group).setChild(branch, 0);
       }
       else {
         ((BranchGroup)group).addChild(branch);
         /*
         // make sure group is live.  group not empty (above addChild)
         if (group instanceof BranchGroup) {
           ((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);
         }
         */
       }

     } 
     else {
       imgNode = ((ImageRendererJ3D)renderer).getImageNode();
     } 


    if (adaptedShadowType == null) {
      adaptedShadowType = (ShadowFunctionOrSetType) getAdaptedShadowType();
    }

    GraphicsModeControl mode = (GraphicsModeControl)
          display.getGraphicsModeControl().clone();

    // get 'shape' flags
    boolean anyContour = adaptedShadowType.getAnyContour();
    boolean anyFlow = adaptedShadowType.getAnyFlow();
    boolean anyShape = adaptedShadowType.getAnyShape();
    boolean anyText = adaptedShadowType.getAnyText();

    if (anyContour || anyFlow || anyShape || anyText) {
      throw new BadMappingException("no contour, flow, shape or text allowed");
    }

    // get some precomputed values useful for transform
    // length of ValueArray
    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    // mapping from ValueArray to MapVector
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    // array to hold values for various mappings
    float[][] display_values = new float[valueArrayLength][];

    if (inherited_values == null) {
      inherited_values = adaptedShadowType.getInheritedValues();
    }

    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }


    Set domain_set = ((Field) data).getDomainSet();
    Unit[] dataUnits = null;
    CoordinateSystem dataCoordinateSystem = null;
    int numImages = 1;
    FlatField imgFlatField = null;

    ShadowRealType[] DomainComponents = adaptedShadowType.getDomainComponents();

    if (!adaptedShadowType.getIsTerminal()) {

      Vector domain_maps = DomainComponents[0].getSelectedMapVector();
      ScalarMap amap = null;
      if (domain_set.getDimension() == 1 && domain_maps.size() == 1) {
        ScalarMap map = (ScalarMap) domain_maps.elementAt(0);
        if (Display.Animation.equals(map.getDisplayScalar())) {
          amap = map;
        }
      }
      if (amap == null) {
        throw new BadMappingException("time must be mapped to Animation");
      }
      animControl = (AnimationControlJ3D) amap.getControl();

      double[][] values = domain_set.getDoubles();
      double[] times = values[0];
      int len = times.length;
      double delta = Math.abs((times[len-1] - times[0]) / (1000.0 * len));
      numImages = len;

      Switch swit = new SwitchNotify(imgNode, len);  
      ((AVControlJ3D) animControl).addPair((Switch) swit, domain_set, renderer);
      ((AVControlJ3D) animControl).init();

      adaptedShadowType = (ShadowFunctionOrSetType) adaptedShadowType.getRange();
      DomainComponents = adaptedShadowType.getDomainComponents();
      imgFlatField = (FlatField) ((FieldImpl)data).getSample(0);
    }
    else {
      imgFlatField = (FlatField)data;
    }


    domain_set = imgFlatField.getDomainSet();
    dataUnits = ((Function) imgFlatField).getDomainUnits();
    dataCoordinateSystem =
      ((Function) imgFlatField).getDomainCoordinateSystem();

    int domain_length = domain_set.getLength();
    int[] lengths = ((GriddedSet) domain_set).getLengths();
    int data_width = lengths[0];
    int data_height = lengths[1];

    imgNode.numImages = numImages;
    imgNode.data_width = data_width;
    imgNode.data_height = data_height;

    int texture_width_max = link.getDisplay().getDisplayRenderer().getTextureWidthMax();
    int texture_height_max = link.getDisplay().getDisplayRenderer().getTextureWidthMax();

    Mosaic mosaic = new Mosaic(data_height, texture_height_max, data_width, texture_width_max);

    int texture_width = textureWidth(data_width);
    int texture_height = textureHeight(data_height);


    if (reuseImages) {
      if (prevImgNode.numImages != numImages || 
          prevImgNode.data_width != data_width || prevImgNode.data_height != data_height) {
        reuseImages = false;
      }
    }

    if (reuseImages) {
      imgNode.numChildren = prevImgNode.numChildren;
      imgNode.imageTiles = prevImgNode.imageTiles;
    }
    else {
      for (Iterator iter = mosaic.iterator(); iter.hasNext();) {
        Tile tile = (Tile) iter.next();
        imgNode.addTile(new VisADImageTile(numImages, tile.height, tile.y_start, tile.width, tile.x_start));
      }
    }

    prevImgNode = imgNode;


    ShadowRealTupleType Domain = adaptedShadowType.getDomain();
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    float constant_alpha = Float.NaN;
    float[] constant_color = null;

    // check that domain is only spatial
    if (!Domain.getAllSpatial() || Domain.getMultipleDisplayScalar()) {
      throw new BadMappingException("domain must be only spatial");
    }

    // check that range is single RealType mapped to RGB only
    ShadowRealType[] RangeComponents = adaptedShadowType.getRangeComponents();
    int rangesize = RangeComponents.length;
    if (rangesize != 1 && rangesize != 3) {
      throw new BadMappingException("image values must single or triple");
    }
    ScalarMap cmap  = null;
    ScalarMap[] cmaps = null;
    int[] permute = {-1, -1, -1};
    boolean hasAlpha = false;
    if (rangesize == 1) {
      Vector mvector = RangeComponents[0].getSelectedMapVector();
      if (mvector.size() != 1) {
        throw new BadMappingException("image values must be mapped to RGB only");
      }
      cmap = (ScalarMap) mvector.elementAt(0);
      if (Display.RGB.equals(cmap.getDisplayScalar())) {
        
      }
      else if (Display.RGBA.equals(cmap.getDisplayScalar())) {
        hasAlpha = true;
      }
      else {
        throw new BadMappingException("image values must be mapped to RGB or RGBA");
      }
    }
    else {
      cmaps = new ScalarMap[3];
      for (int i=0; i<3; i++) {
        Vector mvector = RangeComponents[i].getSelectedMapVector();
        if (mvector.size() != 1) {
          throw new BadMappingException("image values must be mapped to color only");
        }
        cmaps[i] = (ScalarMap) mvector.elementAt(0);
        if (Display.Red.equals(cmaps[i].getDisplayScalar())) {
          permute[0] = i;
        }
        else if (Display.Green.equals(cmaps[i].getDisplayScalar())) {
          permute[1] = i;
        }
        else if (Display.Blue.equals(cmaps[i].getDisplayScalar())) {
          permute[2] = i;
        }
        else if (Display.RGB.equals(cmaps[i].getDisplayScalar())) { //Inserted by Ghansham for Mapping all the three scalarMaps to Display.RGB (starts here) 
                permute[i] = i;
        }
        else {               ////Inserted by Ghansham for Mapping all the three scalarMaps to Display.RGB(ends here)
          throw new BadMappingException("image values must be mapped to Red, " +
                                          "Green or Blue only");
        }
      }
      if (permute[0] < 0 || permute[1] < 0 || permute[2] < 0) {
        throw new BadMappingException("image values must be mapped to Red, " +
                                      "Green and Blue");
      }
      //Inserted by Ghansham for Checking that all should be mapped to Display.RGB or not even a single one should be mapped to Display.RGB(starts here)
        //This is to check if there is a single Display.RGB ScalarMap
        int indx = -1;
        for (int i = 0; i < 3; i++) {
                if (cmaps[i].getDisplayScalar().equals(Display.RGB)) {
                        indx = i;
                        break;
                }
        }

        if (indx != -1){        //if there is a even a single Display.RGB ScalarMap, others must also Display.RGB only
                for (int i = 0; i < 3; i++) {
                        if (i !=indx && !(cmaps[i].getDisplayScalar().equals(Display.RGB))) {
                                throw new BadMappingException("image values must be mapped to (Red, Green, Blue) or (RGB,RGB,RGB) only");
                        }
                }
        }
        //Inserted by Ghansham for Checking that all should be mapped to Display.RGB or not even a single one should be mapped to Display.RGB(Ends here)        
    }

    constant_alpha =
        default_values[display.getDisplayScalarIndex(Display.Alpha)];
    int color_length;
    ImageRendererJ3D imgRenderer = (ImageRendererJ3D) renderer;
    int imageType = imgRenderer.getSuggestedBufImageType();
    if (imageType == BufferedImage.TYPE_4BYTE_ABGR) {
      color_length = 4;
      if (!hasAlpha) {
        color_length = 3;
        imageType = BufferedImage.TYPE_3BYTE_BGR;
      }
    } else if (imageType == BufferedImage.TYPE_3BYTE_BGR) {
      color_length = 3;
    } else if (imageType == BufferedImage.TYPE_USHORT_GRAY) {
      color_length = 2;
    } else if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
      color_length = 1;
    } else {
      // we shouldn't ever get here because the renderer validates the 
      // imageType before we get it, but just in case...
      throw new VisADException("renderer returned unsupported image type");
    }


    byte[][] color_bytes = null;

    // check domain and determine whether it is square or curved texture
    boolean isTextureMap = adaptedShadowType.getIsTextureMap() &&
                             (domain_set instanceof Linear2DSet ||
                              (domain_set instanceof LinearNDSet &&
                               domain_set.getDimension() == 2)) &&
                             (domain_set.getManifoldDimension() == 2);


    boolean curvedTexture = adaptedShadowType.getCurvedTexture() &&
                            !isTextureMap &&
                            curved_size > 0 &&
                            (domain_set instanceof Gridded2DSet ||
                             (domain_set instanceof GriddedSet &&
                              domain_set.getDimension() == 2)) &&
                             (domain_set.getManifoldDimension() == 2);

    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    byte[] colors = null;
    float[] coordinatesX = null;
    float[] texCoordsX = null;
    float[] normalsX = null;
    byte[] colorsX = null;
    float[] coordinatesY = null;
    float[] texCoordsY = null;
    float[] normalsY = null;
    byte[] colorsY = null;

    if (color_length == 4) constant_alpha = Float.NaN; // WLH 6 May 2003

    first_time =true; //Ghansham: this variable just indicates to makeColorBytes whether it's the first tile of the image
    boolean branch_added = false;
    if (isTextureMap) { // linear texture

        if (imgNode.getNumTiles() == 1) {
          VisADImageTile tile = imgNode.getTile(0);
          makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                      color_bytes, data_width, data_height, imageType, tile, 0);

          buildLinearTexture(bgImages, domain_set, dataUnits, domain_units, default_values, DomainComponents,
                             valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha, 
                             value_array, constant_color, display, tile);
        }
        else {
          BranchGroup branch = new BranchGroup();
          branch.setCapability(BranchGroup.ALLOW_DETACH);
          branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
          branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

          for (Iterator iter = imgNode.getTileIterator(); iter.hasNext();) {
             VisADImageTile tile = (VisADImageTile) iter.next();

                makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                      color_bytes, data_width, data_height, imageType, tile, 0);
                first_time = false; //Ghansham: setting 'first_time' variable false after the first tile has been generated

              float[][] g00 =
                ((GriddedSet)domain_set).gridToValue(
                   new float[][] {{tile.xStart}, {tile.yStart}});
              float[][] g11 =
                ((GriddedSet)domain_set).gridToValue(
                   new float[][] {{tile.xStart+tile.width-1}, {tile.yStart+tile.height-1}});

              double x0 = g00[0][0];
              double x1 = g11[0][0];
              double y0 = g00[1][0];
              double y1 = g11[1][0];
              Set dset = new Linear2DSet(x0, x1, tile.width, y0, y1, tile.height);

              BranchGroup branch1 = new BranchGroup();
              branch1.setCapability(BranchGroup.ALLOW_DETACH);
              branch1.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
              branch1.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
              branch1.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

              buildLinearTexture(branch1, dset, dataUnits, domain_units, default_values, DomainComponents,
                                 valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha, 
                                 value_array, constant_color, display, tile);
              branch.addChild(branch1);
          }
          // group: top level
          if (((Group) bgImages).numChildren() > 0) {
            ((Group) bgImages).setChild(branch, 0);
          }
          else {
            ((Group) bgImages).addChild(branch);
          }
        }
      } // end if (isTextureMap)
      else if (curvedTexture) {

        int[] lens = ((GriddedSet)domain_set).getLengths();
        int[] domain_lens = lens;

        if (imgNode.getNumTiles() == 1) {
          VisADImageTile tile = imgNode.getTile(0);

          makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                               color_bytes, data_width, data_height, imageType, tile,  0);

          buildCurvedTexture(bgImages, domain_set, dataUnits, domain_units, default_values, DomainComponents,
                             valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha,
                             value_array, constant_color, display, curved_size, Domain,
                             dataCoordinateSystem, renderer, adaptedShadowType, new int[] {0,0},
                             domain_lens[0], domain_lens[1], null, domain_lens[0], domain_lens[1], tile);
        }
        else
        {
          float[][] samples = ((GriddedSet)domain_set).getSamples(false);

          BranchGroup branch = new BranchGroup();
          branch.setCapability(BranchGroup.ALLOW_DETACH);
          branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
          branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);


          for (Iterator iter = imgNode.getTileIterator(); iter.hasNext();) {
             VisADImageTile tile = (VisADImageTile) iter.next();

             makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                                  color_bytes, data_width, data_height, imageType, tile, 0);

             first_time = false; //Ghansham: setting 'first_time' variable false after the first tile has been generated

             BranchGroup branch1 = new BranchGroup();
             branch1.setCapability(BranchGroup.ALLOW_DETACH);
             branch1.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
             branch1.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
             branch1.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

             buildCurvedTexture(branch1, null, dataUnits, domain_units, default_values, DomainComponents,
                                valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha,
                                value_array, constant_color, display, curved_size, Domain,
                                dataCoordinateSystem, renderer, adaptedShadowType, 
                                new int[] {tile.xStart,tile.yStart}, tile.width, tile.height,
                                samples, domain_lens[0], domain_lens[1], tile);

             branch.addChild(branch1);
           }

          // group: top level
          if (((Group) bgImages).numChildren() > 0) {
            ((Group) bgImages).setChild(branch, 0);
          }
          else {
            ((Group) bgImages).addChild(branch);
          }
        }
      } // end if (curvedTexture)
      else { // !isTextureMap && !curvedTexture
        throw new BadMappingException("must be texture map or curved texture map");
      }


      // make sure group is live.  group not empty (above addChild)
      if (group instanceof BranchGroup) {
        ((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);
      }


      for (int k=1; k<numImages; k++) {
        FlatField ff = (FlatField) ((Field)data).getSample(k);
        CoordinateSystem dcs = ff.getDomainCoordinateSystem();
        GriddedSet domSet = (GriddedSet) ff.getDomainSet();
        int[] lens = domSet.getLengths();

        // if image dimensions, or dataCoordinateSystem not equal to first image, resample to first
        if ( (lens[0] != data_width || lens[1] != data_height) || !(dcs.equals(dataCoordinateSystem)) ) {
          ff = (FlatField) ff.resample(imgFlatField.getDomainSet(), Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
        }

        first_time = true;
        scaled_Bytes = null; //scaled byte values 
        scaled_Floats = null; //scaled Float Values
        itable = null; //For single band
        threeD_itable = null; //for multiband
        color_values = null; //special case

        for (Iterator iter = imgNode.getTileIterator(); iter.hasNext();) {
          VisADImageTile tile = (VisADImageTile) iter.next();

          makeColorBytesDriver(ff, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                color_bytes, data_width, data_height, imageType, tile, k);
           first_time = false;
          //image.bytesChanged(byteData);
        }
      }
      first_time = true;
      scaled_Bytes = null; //scaled byte values 
      scaled_Floats = null; //scaled Float Values
      itable = null; //For single band
      threeD_itable = null; //for multiband
      color_values = null; //special case

    ensureNotEmpty(bgImages);
    return false;
  }


// This function calls makeColorBytes function (Ghansham)
public void makeColorBytesDriver(Data imgFlatField, ScalarMap cmap, ScalarMap[] cmaps, float constant_alpha,
              ShadowRealType[] RangeComponents, int color_length, int domain_length, int[] permute,
              byte[][] color_bytes, int data_width, int data_height,
              int imageType, VisADImageTile tile, int image_index) throws VisADException, RemoteException {
        BufferedImage image = null;
        byte byteData[] = null;
        int tile_width = tile.width;
        int tile_height = tile.height;
        int xStart = tile.xStart;
        int yStart = tile.yStart;
        int texture_width = textureWidth(tile_width);
        int texture_height = textureHeight(tile_height);

       if (!reuseImages) {
         image = createImageByRef(texture_width, texture_height, imageType);
         tile.setImage(image_index, image);
       } else {
         //image = (CachedBufferedByteImage) tile.getImage(0);
         image = (BufferedImage) tile.getImage(image_index);
       }

       java.awt.image.Raster raster = image.getRaster();
       DataBuffer db = raster.getDataBuffer();
       byteData = ((DataBufferByte)db).getData();
       makeColorBytes(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                      color_bytes, byteData,
                      data_width, data_height, tile_width, tile_height, xStart, yStart, texture_width, texture_height);
}

/*  New version contributed by Ghansham (ISRO)
 This function scales the flatfield values and the colortable for the first tile only using the first_time variable. Rest of the time it only
 uses scaled values and color table to generate colorbytes for respective tile. Just see the first_time variable use. That is the only difference between
 this function and earlier function makeColorBytes(). Some new class variables have been introduced to preserve scaled values and colortable.
 They are made null after all the tiles for a single image has been generated. At the end of doTransform(), they are made null.
*/
public void makeColorBytes(Data data, ScalarMap cmap, ScalarMap[] cmaps, float constant_alpha,
              ShadowRealType[] RangeComponents, int color_length, int domain_length, int[] permute,
              byte[][] color_bytes, byte[] byteData,
              int data_width, int data_height, int tile_width, int tile_height, int xStart, int yStart,
              int texture_width, int texture_height)
                throws VisADException, RemoteException {

        if (cmap != null) {
                // build texture colors in color_bytes array
                BaseColorControl control = (BaseColorControl) cmap.getControl();
                float[][] table = control.getTable();
                Set rset = null;
                boolean is_default_unit = false;

                if (data instanceof FlatField) {
                        // for fast byte color lookup, need:
                        // 1. range data values are packed in bytes
                        //bytes = ((FlatField) data).grabBytes();
                        if (first_time) {
                                scaled_Bytes = ((FlatField) data).grabBytes();
                        }
                        // 2. range set is Linear1DSet
                        Set[] rsets = ((FlatField) data). getRangeSets();
                        if (rsets != null) rset = rsets[0];
                        // 3. data Unit equals default Unit
                        RealType rtype = (RealType) RangeComponents[0].getType();
                        Unit def_unit = rtype.getDefaultUnit();
                        if (def_unit == null) {
                                is_default_unit = true;
                        } else {
                                Unit[][] data_units = ((FlatField) data).getRangeUnits();
                                Unit data_unit = (data_units == null) ? null : data_units[0][0];
                                is_default_unit = def_unit.equals(data_unit);
                        }
                }
                if (table != null) {
                        // combine color table RGB components into ints
                        if (first_time) {
                                itable = new byte[table[0].length][4];
                                // int r, g, b, a = 255;
                                int r, g, b;
                                int c = (int) (255.0 * (1.0f - constant_alpha));
                                int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                for (int j=0; j<table[0].length; j++) {
                                        c = (int) (255.0 * table[0][j]);
                                        r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                        c = (int) (255.0 * table[1][j]);
                                        g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                        c = (int) (255.0 * table[2][j]);
                                        b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                        if (color_length == 4) {
                                                c = (int) (255.0 * table[3][j]);
                                                a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                        }
                                        itable[j][0] = (byte) r;
                                        itable[j][1] = (byte) g;
                                        itable[j][2] = (byte) b;
                                        itable[j][3] = (byte) a;
                                }
                        }
                        int tblEnd = table[0].length - 1;
                        // get scale for color table
                        int table_scale = table[0].length;
                        if (data instanceof ImageFlatField && scaled_Bytes != null && is_default_unit) {
                                if (ImageFlatField.DEBUG) {
                                        System.err.println("ShadowImageFunctionTypeJ3D.doTransform: " + "cmap != null: looking up color values");
                                }
                                // avoid unpacking floats for ImageFlatFields
                                if (first_time) {
                                        scaled_Bytes[0]= cmap.scaleValues(scaled_Bytes[0], table_scale);
                                }
                                // fast lookup from byte values to color bytes
                                byte[] bytes0 = scaled_Bytes[0];

                                int k =0;
                                for (int y=0; y<tile_height; y++) {
                                        int image_col_factor = (y+yStart)*data_width + xStart;
                                        for (int x=0; x<tile_width; x++) {
                                                int i = x + image_col_factor;
                                                int j = bytes0[i] & 0xff; // unsigned
                                                k = x + y*texture_width;
                                                k *= color_length;
                                                // clip to table
                                                int ndx = j < 0 ? 0 : (j > tblEnd ? tblEnd : j);

                                                if (color_length == 4) {
                                                        byteData[k] = itable[ndx][3];
                                                        byteData[k+1] = itable[ndx][2];
                                                        byteData[k+2] = itable[ndx][1];
                                                        byteData[k+3] = itable[ndx][0];
                                                }
                                                if (color_length == 3) {
                                                        byteData[k] = itable[ndx][2];
                                                        byteData[k+1] = itable[ndx][1];
                                                        byteData[k+2] = itable[ndx][0];
                                                }
                                                if (color_length == 1) {
                                                        byteData[k] = itable[ndx][0];
                                                }
                                        }
                                }
                        } else if (scaled_Bytes != null && scaled_Bytes[0] != null && is_default_unit && rset != null && rset instanceof Linear1DSet) {
                                // fast since FlatField with bytes, data Unit equals default
                                // Unit and range set is Linear1DSet
                                // get "scale and offset" for Linear1DSet
                                if (first_time) {
                                        double first = ((Linear1DSet) rset).getFirst();
                                        double step = ((Linear1DSet) rset).getStep();
                                        // get scale and offset for ScalarMap
                                        double[] so = new double[2];
                                        double[] da = new double[2];
                                        double[] di = new double[2];
                                        cmap.getScale(so, da, di);
                                        double scale = so[0];
                                        double offset = so[1];
                                        // combine scales and offsets for Set, ScalarMap and color table
                                        float mult = (float) (table_scale * scale * step);
                                        float add = (float) (table_scale * (offset + scale * first));

                                        // build table for fast color lookup
                                        itable = null;
                                        itable = new byte[256][];
                                        for (int j=0; j<256; j++) {
                                                int index = j - 1;
                                                if (index >= 0) { // not missing
                                                        int k = (int) (add + mult * index);
                                                        // clip to table
                                                        int ndx = k < 0 ? 0 : (k > tblEnd ? tblEnd : k);
                                                        itable[j] = itable[ndx];
                                                }
                                        }
                                }
                                // now do fast lookup from byte values to color bytes
                                byte[] bytes0 = scaled_Bytes[0];

                                int k = 0;
                                for (int y=0; y<tile_height; y++) {
                                        int image_col_factor = (y+yStart)*data_width + xStart;
                                        for (int x=0; x<tile_width; x++) {
                                                int i = x + image_col_factor;
                                                int ndx = ((int) bytes0[i]) - MISSING1;
                                                k = x + y*texture_width;
                                                k *= color_length;

                                                if (color_length == 4) {
                                                        byteData[k] = itable[ndx][3];
                                                        byteData[k+1] = itable[ndx][2];
                                                        byteData[k+2] = itable[ndx][1];
                                                        byteData[k+3] = itable[ndx][0];
                                                }
                                                if (color_length == 3) {
                                                        byteData[k] = itable[ndx][2];
                                                        byteData[k+1] = itable[ndx][1];
                                                        byteData[k+2] = itable[ndx][0];
                                                }
                                                if (color_length == 1) {
                                                        byteData[k] = itable[ndx][0];
                                                }
                                        }
                                }
                        } else {
                                // medium speed way to build texture colors
                                if (first_time) {
                                        scaled_Bytes = null;
                                        scaled_Floats = ((Field) data).getFloats(false);
                                        scaled_Floats[0] = cmap.scaleValues(scaled_Floats[0]);
                                }
                                // now do fast lookup from byte values to color bytes
                                float[] values0 = scaled_Floats[0];
                                int k = 0;
                                for (int y=0; y<tile_height; y++) {
                                        int image_col_factor =  (y+yStart)*data_width + xStart;
                                        for (int x=0; x<tile_width; x++) {
                                                int i = x + image_col_factor;
                                                k = x + y*texture_width;
                                                k *= color_length;

                                                if (!Float.isNaN(values0[i])) { // not missing
                                                        int j = (int) (table_scale * values0[i]);
                                                        // clip to table
                                                        int ndx = j < 0 ? 0 : (j > tblEnd ? tblEnd : j);
                                                        if (color_length == 4) {
                                                                byteData[k] = itable[ndx][3];
                                                                byteData[k+1] = itable[ndx][2];
                                                                byteData[k+2] = itable[ndx][1];
                                                                byteData[k+3] = itable[ndx][0];
                                                        }
                                                        if (color_length == 3) {
                                                                byteData[k] = itable[ndx][2];
                                                                byteData[k+1] = itable[ndx][1];
                                                                byteData[k+2] = itable[ndx][0];
                                                        }
                                                        if (color_length == 1) {
                                                                byteData[k] = itable[ndx][0];
                                                        }
                                                }
                                        }
                                }
                        }
                } else { // if (table == null)
                        // slower, more general way to build texture colors
                        if (first_time) {
                                // call lookupValues which will use function since table == null
                                scaled_Bytes = null;
                                itable = null;
                                scaled_Floats = ((Field) data).getFloats(false);
                                scaled_Floats[0] = cmap.scaleValues(scaled_Floats[0]);
                                color_values = control.lookupValues(scaled_Floats[0]);
                        }

                        // combine color RGB components into bytes
                        // int r, g, b, a = 255;
                        int r, g, b;
                        int c = (int) (255.0 * (1.0f - constant_alpha));
                        int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                        int k = 0;
                        for (int y=0; y<tile_height; y++) {
                                int image_col_factor = (y+yStart)*data_width + xStart;
                                for (int x=0; x<tile_width; x++) {
                                        int i = x + image_col_factor;
                                        k = x + y*texture_width;
                                        k *= color_length;

                                        if (!Float.isNaN(scaled_Floats[0][i])) { // not missing
                                                c = (int) (255.0 * color_values[0][i]);
                                                r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                c = (int) (255.0 * color_values[1][i]);
                                                g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                c = (int) (255.0 * color_values[2][i]);
                                                b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                if (color_length == 4) {
                                                        c = (int) (255.0 * color_values[3][i]);
                                                        a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                }
                                                if (color_length == 4) {
                                                        byteData[k] = (byte) a;
                                                        byteData[k+1] = (byte) b;
                                                        byteData[k+2] = (byte) g;
                                                        byteData[k+3] = (byte) r;
                                                }
                                                if (color_length == 3) {
                                                        byteData[k] = (byte) b;
                                                        byteData[k+1] = (byte) g;
                                                        byteData[k+2] = (byte) r;
                                                }
                                                if (color_length == 1) {
                                                        byteData[k] = (byte) b;
                                                }
                                        }
                                }
                        }
                }
        } else if (cmaps != null) {
                if (data instanceof ImageFlatField) {
                        if (first_time) {
                                scaled_Bytes = ((FlatField) data).grabBytes();
                        }
                }


                boolean isRGBRGBRGB = ((cmaps[0].getDisplayScalar() == Display.RGB) && (cmaps[1].getDisplayScalar() == Display.RGB) && (cmaps[2].getDisplayScalar() == Display.RGB));

                int r, g, b, c;
                int tableEnd = 0;
                if (first_time) {
                        if  (isRGBRGBRGB) { //Inserted by Ghansham (starts here)
                                int map_indx;
                                threeD_itable = new byte[cmaps.length][][];
                                for (map_indx = 0; map_indx < cmaps.length; map_indx++) {
                                        BaseColorControl basecolorcontrol = (BaseColorControl)cmaps[map_indx].getControl();
                                        float color_table[][] = basecolorcontrol.getTable();
                                        threeD_itable[map_indx] = new byte[color_table[0].length][3];
                                        int table_indx;
                                        for(table_indx = 0; table_indx < threeD_itable[map_indx].length; table_indx++) {
                                                c = (int) (255.0 * color_table[0][table_indx]);
                                                r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                c = (int) (255.0 * color_table[1][table_indx]);
                                                g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                c = (int) (255.0 * color_table[2][table_indx]);
                                                b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                threeD_itable[map_indx][table_indx][0] = (byte) r;
                                                threeD_itable[map_indx][table_indx][1] = (byte) g;
                                                threeD_itable[map_indx][table_indx][2] = (byte) b;
                                        }
                                }
                        }
                }

                if (scaled_Bytes != null) {
                        // grab bytes directly from ImageFlatField
                        if (ImageFlatField.DEBUG) {
                                System.err.println("ShadowImageFunctionTypeJ3D.doTransform: " + "cmaps != null: grab bytes directly");
                        }
                        color_bytes = new byte[4][];
                        //Inserted by Ghansham starts here
                        if  (cmaps[0].getDisplayScalar() == Display.RGB && cmaps[1].getDisplayScalar() == Display.RGB && cmaps[2].getDisplayScalar() == Display.RGB) {
                                int map_indx = 0;
                                for (map_indx = 0; map_indx < cmaps.length; map_indx++) {
                                        int table_length = threeD_itable[0].length;
                                        int color_indx = permute[map_indx];
                                        if (first_time) {
                                                scaled_Bytes[color_indx] = cmaps[color_indx].scaleValues(scaled_Bytes[color_indx], table_length);
                                        }
                                        int domainLength =  scaled_Bytes[color_indx].length;
                                        int tblEnd = table_length - 1;
                                        color_bytes[map_indx] = new byte[domain_length];
                                        int data_indx;

                                        for (data_indx = 0; data_indx < domainLength; data_indx++) {
                                                int j = scaled_Bytes[color_indx][data_indx] & 0xff; // unsigned
                                                // clip to table
                                                int ndx = j < 0 ? 0 : (j > tblEnd ? tblEnd : j);
                                                color_bytes[map_indx][data_indx] = threeD_itable[map_indx][ndx][map_indx];
                                        }
                                 }
                        } else { //Inserted by Ghansham (Ends here)
                                color_bytes[0] = scaled_Bytes[0];
                                color_bytes[1] = scaled_Bytes[1];
                                color_bytes[2] = scaled_Bytes[2];
                        }
                        c = (int) (255.0 * (1.0f - constant_alpha));
                        color_bytes[3] = new byte[domain_length];
                        Arrays.fill(color_bytes[3], (byte) c);
                } else {
                        if (first_time) {
                                float[][] values = ((Field) data).getFloats(false);
                                scaled_Floats = new float[3][];

                                /** Multi Threading
                                final ScalarMap cmap0 = cmaps[permute[0]];
                                final float[] values0 = values[permute[0]];
                                ThreadManager threadManager = new ThreadManager(java.lang.Runtime.getRuntime().availableProcessors());
                                threadManager.addRunnable(new ThreadManager.MyRunnable() {
                                        public void run() throws Exception {
                                          scaled_Floats[0] = cmap0.scaleValues(values0);
                                        }
                                   });

                                final ScalarMap cmap1 = cmaps[permute[1]];
                                final float[] values1 = values[permute[1]];
                                threadManager.addRunnable(new ThreadManager.MyRunnable() {
                                        public void run() throws Exception {
                                          scaled_Floats[1] = cmap1.scaleValues(values1);
                                        }
                                   });

                                final ScalarMap cmap2 = cmaps[permute[2]];
                                final float[] values2 = values[permute[2]];
                                threadManager.addRunnable(new ThreadManager.MyRunnable() {
                                        public void run() throws Exception {
                                          scaled_Floats[2] = cmap2.scaleValues(values2);
                                        }
                                   });

                                long time1 = System.currentTimeMillis();
                                threadManager.runInParallel();
                                long time2 = System.currentTimeMillis();
                                */
                                scaled_Floats[0] = cmaps[permute[0]].scaleValues(values[permute[0]]);
                                scaled_Floats[1] = cmaps[permute[1]].scaleValues(values[permute[1]]);
                                scaled_Floats[2] = cmaps[permute[2]].scaleValues(values[permute[2]]);
                                values = null;
                        }
                        c = (int) (255.0 * (1.0f - constant_alpha));
                        int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                        int m = 0;
                        if  (isRGBRGBRGB) {
                                tableEnd = threeD_itable[0].length - 1;
                        }
                        //System.err.println("Alpha:"+ constant_alpha + " color length:"+ color_length + " a:"+  a);
                        int k = 0;
                        for (int y=0; y<tile_height; y++) {
                                int image_col_factor = (y+yStart)*data_width + xStart;
                                for (int x=0; x<tile_width; x++) {
                                        int i = x + image_col_factor;
                                        k = x + y*texture_width;
                                        k *= color_length;

                                        if (!Float.isNaN(scaled_Floats[0][i]) && !Float.isNaN(scaled_Floats[1][i]) && !Float.isNaN(scaled_Floats[2][i])) { // not missing
                                                if (isRGBRGBRGB) { //Inserted by Ghansham (start here)
                                                        int indx = (int)(tableEnd * scaled_Floats[0][i]);
                                                        indx = (indx < 0) ? 0 : ((indx > tableEnd) ? tableEnd : indx);
                                                        r = threeD_itable[0][indx][0];
                                                        indx = (int)(tableEnd * scaled_Floats[1][i]);
                                                        indx = (indx < 0) ? 0 : ((indx > tableEnd) ? tableEnd : indx);
                                                        g = threeD_itable[1][indx][1];
                                                        indx = (int)(tableEnd * scaled_Floats[2][i]);
                                                        indx = (indx < 0) ? 0 : ((indx > tableEnd) ? tableEnd : indx);
                                                        b = threeD_itable[2][indx][2];
                                                } else { //Inserted by Ghansham (ends here)

                                                        c = (int) (255.0 * scaled_Floats[0][i]);
                                                        r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                        c = (int) (255.0 * scaled_Floats[1][i]);
                                                        g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                        c = (int) (255.0 * scaled_Floats[2][i]);
                                                        b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                }

                                                if (color_length == 4) {
                                                        byteData[k] = (byte) a;
                                                        byteData[k+1] = (byte) b;
                                                        byteData[k+2] = (byte) g;
                                                        byteData[k+3] = (byte) r;
                                                } if (color_length == 3) {
                                                        byteData[k] = (byte) b;
                                                        byteData[k+1] = (byte) g;
                                                        byteData[k+2] = (byte) r;
                                                }
                                                if (color_length == 1) {
                                                        byteData[k] = (byte) b;
                                                }
                                        }
                                }
                        }
                }
        } else {
                throw new BadMappingException("cmap == null and cmaps == null ??");
        }
}


  public void buildCurvedTexture(Object group, Set domain_set, Unit[] dataUnits, Unit[] domain_units,
                                 float[] default_values, ShadowRealType[] DomainComponents,
                                 int valueArrayLength, int[] inherited_values, int[] valueToScalar,
                                 GraphicsModeControl mode, float constant_alpha, float[] value_array, 
                                 float[] constant_color, DisplayImpl display,
                                 int curved_size, ShadowRealTupleType Domain, CoordinateSystem dataCoordinateSystem,
                                 DataRenderer renderer, ShadowFunctionOrSetType adaptedShadowType,
                                 int[] start, int lenX, int lenY, float[][] samples, int bigX, int bigY,
                                 VisADImageTile tile)
         throws VisADException, DisplayException {
// System.out.println("start curved texture " + (System.currentTimeMillis() - link.start_time));
    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    int data_width = 0;
    int data_height = 0;
    int texture_width = 1;
    int texture_height = 1;

    int[] lengths = null;
                                                                                                                     
    // get domain_set sizes
    if (domain_set != null) {
      lengths = ((GriddedSet) domain_set).getLengths();
    }
    else {
      lengths = new int[] {lenX, lenY};
    }

    data_width = lengths[0];
    data_height = lengths[1];

    // texture sizes must be powers of two on older graphics cards.
    texture_width = textureWidth(data_width);
    texture_height = textureHeight(data_height);
                                                                                                                   
    // transform for any CoordinateSystem in data (Field) Domain
    ShadowRealTupleType domain_reference = Domain.getReference();
    ShadowRealType[] DC = DomainComponents;

    if (domain_reference != null &&
        domain_reference.getMappedDisplayScalar()) {
      RealTupleType ref = (RealTupleType) domain_reference.getType();
      renderer.setEarthSpatialData(Domain, domain_reference, ref,
                  ref.getDefaultUnits(), (RealTupleType) Domain.getType(),
                  new CoordinateSystem[] {dataCoordinateSystem},
                  domain_units);

      // ShadowRealTypes of DomainReference
      DC = adaptedShadowType.getDomainReferenceComponents();
    }
    else {
      RealTupleType ref = (domain_reference == null) ? null :
                          (RealTupleType) domain_reference.getType();
      Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
      renderer.setEarthSpatialData(Domain, domain_reference, ref,
                  ref_units, (RealTupleType) Domain.getType(),
                  new CoordinateSystem[] {dataCoordinateSystem},
                  domain_units);
    }
                                                                                                                   
    int[] tuple_index = new int[3];
    int[] spatial_value_indices = {-1, -1, -1};
    ScalarMap[] spatial_maps = new ScalarMap[3];
                                                                                                                   
    DisplayTupleType spatial_tuple = null;
    for (int i=0; i<DC.length; i++) {
      Enumeration maps =
        DC[i].getSelectedMapVector().elements();
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType real = map.getDisplayScalar();
      spatial_tuple = real.getTuple();
      if (spatial_tuple == null) {
        throw new DisplayException("texture with bad tuple: " +
                                   "ShadowImageFunctionTypeJ3D.doTransform");
      }
      // get spatial index
      tuple_index[i] = real.getTupleIndex();
      spatial_value_indices[tuple_index[i]] = map.getValueIndex();
      spatial_maps[tuple_index[i]] = map;
      if (maps.hasMoreElements()) {
        throw new DisplayException("texture with multiple spatial: " +
                                   "ShadowImageFunctionTypeJ3D.doTransform");
      }
    } // end for (int i=0; i<DC.length; i++)


    // get spatial index not mapped from domain_set
    tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
    DisplayRealType real =
      (DisplayRealType) spatial_tuple.getComponent(tuple_index[2]);
    int value2_index = display.getDisplayScalarIndex(real);
    float value2 = default_values[value2_index];
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0 &&
          real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
        value2 = value_array[i];
        break;
      }
    }
                                                                                                                   
    boolean useLinearTexture = false;
    double[] scale = null;
    double[] offset = null;
    CoordinateSystem coord = null;

    if (spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
// inside 'if (anyFlow) {}' in ShadowType.assembleSpatial()
      renderer.setEarthSpatialDisplay(null, spatial_tuple, display,
               spatial_value_indices, default_values, null);
    }
    else {
      coord = spatial_tuple.getCoordinateSystem();

      if (coord instanceof CachingCoordinateSystem) {
        coord = ((CachingCoordinateSystem)coord).getCachedCoordinateSystem();
      }

      if (coord instanceof InverseLinearScaledCS) {
        InverseLinearScaledCS invCS = (InverseLinearScaledCS)coord;
        useLinearTexture = (invCS.getInvertedCoordinateSystem()).equals(dataCoordinateSystem);
        scale = invCS.getScale();
        offset = invCS.getOffset();
      }

// inside 'if (anyFlow) {}' in ShadowType.assembleSpatial()
      renderer.setEarthSpatialDisplay(coord, spatial_tuple, display,
               spatial_value_indices, default_values, null);
    }

    if (useLinearTexture) {
      float scaleX = (float) scale[0];
      float scaleY = (float) scale[1];
      float offsetX = (float) offset[0];
      float offsetY = (float) offset[1];

      float[][] xyCoords = null;
      if (domain_set != null) {
           xyCoords = getBounds(domain_set, data_width, data_height, scaleX, offsetX, scaleY, offsetY);
      } else {
                //If there is tiling in linear texture domain set is coming null if number of tiles is greater than 1
                //Code inserted by Ghansham (starts here)
                int indx0 = (start[0] ) + (start[1])*bigX;
                int indx1 = (start[0]) + (start[1] + lenY-1)*bigX;
                int indx2 = (start[0] + lenX -1) + (start[1] + lenY - 1)*bigX;
                int indx3 = (start[0] + lenX -1 ) + (start[1])*bigX;

                float x0 = samples[0][indx0];
                float y0 = samples[1][indx0];
                float x1 = samples[0][indx1];
                float y1 = samples[1][indx1];
                float x2 = samples[0][indx2];
                float y2 = samples[1][indx2];
                float x3 = samples[0][indx3];
                float y3 = samples[1][indx3];

                xyCoords = new float[2][4];
                xyCoords[0][0] = (x0 - offsetX)/scaleX;
                xyCoords[1][0] = (y0 - offsetY)/scaleY;

                xyCoords[0][1] = (x1 - offsetX)/scaleX;
                xyCoords[1][1] = (y1 - offsetY)/scaleY;

                xyCoords[0][2] = (x2 - offsetX)/scaleX;
                xyCoords[1][2] = (y2 - offsetY)/scaleY;

                xyCoords[0][3] = (x3 - offsetX)/scaleX;
                xyCoords[1][3] = (y3 - offsetY)/scaleY;
                //Code inserted by Ghansham (Ends here)
      }


      // create VisADQuadArray that texture is mapped onto
      coordinates = new float[12];
      // corner 0 (-1,1)
      coordinates[tuple_index[0]] = xyCoords[0][0];
      coordinates[tuple_index[1]] = xyCoords[1][0];
      coordinates[tuple_index[2]] = value2;
      // corner 1 (-1,-1)
      coordinates[3+tuple_index[0]] = xyCoords[0][1];
      coordinates[3+tuple_index[1]] = xyCoords[1][1];
      coordinates[3 + tuple_index[2]] = value2;
      // corner 2 (1, -1)
      coordinates[6+tuple_index[0]] = xyCoords[0][2];
      coordinates[6+tuple_index[1]] = xyCoords[1][2];
      coordinates[6 + tuple_index[2]] = value2;
      // corner 3 (1,1)
      coordinates[9+tuple_index[0]] = xyCoords[0][3];
      coordinates[9+tuple_index[1]] = xyCoords[1][3];
      coordinates[9 + tuple_index[2]] = value2;


      // move image back in Java3D 2-D mode
      adjustZ(coordinates);

      texCoords = new float[8];
      float ratiow = ((float) data_width) / ((float) texture_width);
      float ratioh = ((float) data_height) / ((float) texture_height);

      boolean yUp = true;
      setTexCoords(texCoords, ratiow, ratioh, yUp);

      normals = new float[12];
      float n0 = ((coordinates[3+2]-coordinates[0+2]) *
                  (coordinates[6+1]-coordinates[0+1])) -
                 ((coordinates[3+1]-coordinates[0+1]) *
                  (coordinates[6+2]-coordinates[0+2]));
      float n1 = ((coordinates[3+0]-coordinates[0+0]) *
                  (coordinates[6+2]-coordinates[0+2])) -
                 ((coordinates[3+2]-coordinates[0+2]) *
                  (coordinates[6+0]-coordinates[0+0]));
      float n2 = ((coordinates[3+1]-coordinates[0+1]) *
                  (coordinates[6+0]-coordinates[0+0])) -
                 ((coordinates[3+0]-coordinates[0+0]) *
                  (coordinates[6+1]-coordinates[0+1]));
      float nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
      n0 = n0 / nlen;
      n1 = n1 / nlen;
      n2 = n2 / nlen;

      // corner 0
      normals[0] = n0;
      normals[1] = n1;
      normals[2] = n2;
      // corner 1
      normals[3] = n0;
      normals[4] = n1;
      normals[5] = n2;
      // corner 2
      normals[6] = n0;
      normals[7] = n1;
      normals[8] = n2;
      // corner 3
      normals[9] = n0;
      normals[10] = n1;
      normals[11] = n2;

      VisADQuadArray qarray = new VisADQuadArray();
      qarray.vertexCount = 4;
      qarray.coordinates = coordinates;
      qarray.texCoords = texCoords;
      qarray.normals = normals;

      if (!reuse) {
         BufferedImage image = tile.getImage(0);
         textureToGroup(group, qarray, image, mode, constant_alpha,
                        constant_color, texture_width, texture_height, true, true, tile);
      }
      else {
        if (animControl == null) {
          imgNode.setCurrent(0);
        }
      }

    }
    else {
      // compute size of triangle array to map texture onto
      int size = (data_width + data_height) / 2;
      curved_size = Math.max(2, Math.min(curved_size, size / 32));

      int nwidth = 2 + (data_width - 1) / curved_size;
      int nheight = 2 + (data_height - 1) / curved_size;

      // compute locations of triangle vertices in texture
      int nn = nwidth * nheight;
      int[] is = new int[nwidth];
      int[] js = new int[nheight];
      for (int i=0; i<nwidth; i++) {
        is[i] = Math.min(i * curved_size, data_width - 1);
      }
      for (int j=0; j<nheight; j++) {
        js[j] = Math.min(j * curved_size, data_height - 1);
      }

      // get spatial coordinates at triangle vertices
      int k = 0;
      float[][] spline_domain = null;
      if (domain_set == null) {
	//Ghansham: We generate the indices for the samples directly from 'is' and 'js' array
	spline_domain = new float[2][nn];
	int kk = 0;
	int ndx = 0;
	int col_factor = 0;
	for (int j = 0; j < nheight; j++) {
            col_factor = (start[1] + js[j]) * bigX;
            for (int i = 0; i < nwidth; i++) {
                ndx = (start[0] + is[i]) + col_factor;
		spline_domain[0][kk] = samples[0][ndx];
		spline_domain[1][kk] = samples[1][ndx];
                kk++;
            }
        }
      }
      else {
	int[] indices = new int[nn]; //Ghansham:Calculate indices only if there is a single tile in the full image
      	k=0;
	int col_factor;
      	for (int j=0; j<nheight; j++) {
        	col_factor = data_width * js[j];
        	for (int i=0; i<nwidth; i++) {
          		indices[k] = is[i] + col_factor;
          		k++;
        	} 
      	}
        spline_domain = domain_set.indexToValue(indices);
      }

      spline_domain =
          Unit.convertTuple(spline_domain, dataUnits, domain_units, false);


       if (domain_reference != null
             && domain_reference.getMappedDisplayScalar()) {
           RealTupleType ref = (RealTupleType) domain_reference.getType();

            spline_domain =
                   CoordinateSystem.transformCoordinates(
                   ref, null, ref.getDefaultUnits(), null,
                   (RealTupleType) Domain.getType(), dataCoordinateSystem,
                   domain_units, null, spline_domain);
       }



       float[][] spatial_values = new float[3][];
       spatial_values[tuple_index[0]] = spline_domain[0];
       spatial_values[tuple_index[1]] = spline_domain[1];
       spatial_values[tuple_index[2]] = new float[nn];            
       Arrays.fill(spatial_values[tuple_index[2]], value2);
            
       for (int i = 0; i < 3; i++) {                
          if (spatial_maps[i] != null) {
             spatial_values[i] = spatial_maps[i].scaleValues(spatial_values[i], false);
          }
       }

       if (!spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
          spatial_values = coord.toReference(spatial_values);
       }


    // break from ShadowFunctionOrSetType
    coordinates = new float[3 * nn];
    k = 0;
    for (int i=0; i<nn; i++) {
      coordinates[k++] = spatial_values[0][i];
      coordinates[k++] = spatial_values[1][i];
      coordinates[k++] = spatial_values[2][i];
    }
                                                                                                                   
    boolean spatial_all_select = true;
    for (int i=0; i<3*nn; i++) {
      if (Float.isNaN(coordinates[i])) {
        spatial_all_select = false;
        break;
      }
    }
                                                                                                                   
    normals = Gridded3DSet.makeNormals(coordinates, nwidth, nheight);
                                                                                                                   
    float ratiow = ((float) data_width) / ((float) texture_width);
    float ratioh = ((float) data_height) / ((float) texture_height);
                                                                                                                   
    // WLH 27 Jan 2003
    float half_width = 0.5f / ((float) texture_width);
    float half_height = 0.5f / ((float) texture_height);
    float width = 1.0f / ((float) texture_width);
    float height = 1.0f / ((float) texture_height);
                                                                                                                   
    int mt = 0;
    texCoords = new float[2 * nn];
    for (int j=0; j<nheight; j++) {
      float jsfactor = js[j] / (data_height - 1.0f);
      for (int i=0; i<nwidth; i++) {
        float isfactor = is[i] / (data_width - 1.0f);
        texCoords[mt++] = (ratiow - width) * isfactor + half_width;
        // yUp = true
        texCoords[mt++] = (ratioh - height) * jsfactor + half_height;
        /* yUp = false;
        texCoords[mt++] = 1.0f - (ratioh - height) * jsfactor - half_height;
        */
      }
    }

    VisADTriangleStripArray tarray = new VisADTriangleStripArray();
    tarray.stripVertexCounts = new int[nheight - 1];
    java.util.Arrays.fill(tarray.stripVertexCounts, 2 * nwidth);

    int len = (nheight - 1) * (2 * nwidth);
    tarray.vertexCount = len;
    tarray.normals = new float[3 * len];
    tarray.coordinates = new float[3 * len];
    tarray.texCoords = new float[2 * len];
                                                                                                                   
    // shuffle normals into tarray.normals, etc
    k = 0;
    int kt = 0;
    int nwidth3 = 3 * nwidth;
    int nwidth2 = 2 * nwidth;
    for (int i=0; i<nheight-1; i++) {
      int m = i * nwidth3;
      mt = i * nwidth2;
      for (int j=0; j<nwidth; j++) {
        tarray.coordinates[k] = coordinates[m];
        tarray.coordinates[k+1] = coordinates[m+1];
        tarray.coordinates[k+2] = coordinates[m+2];
        tarray.coordinates[k+3] = coordinates[m+nwidth3];
        tarray.coordinates[k+4] = coordinates[m+nwidth3+1];
        tarray.coordinates[k+5] = coordinates[m+nwidth3+2];
                                                                                                                   
        tarray.normals[k] = normals[m];
        tarray.normals[k+1] = normals[m+1];
        tarray.normals[k+2] = normals[m+2];
        tarray.normals[k+3] = normals[m+nwidth3];
        tarray.normals[k+4] = normals[m+nwidth3+1];
        tarray.normals[k+5] = normals[m+nwidth3+2];

        tarray.texCoords[kt] = texCoords[mt];
        tarray.texCoords[kt+1] = texCoords[mt+1];
        tarray.texCoords[kt+2] = texCoords[mt+nwidth2];
        tarray.texCoords[kt+3] = texCoords[mt+nwidth2+1];
                                                                                                                   
        k += 6;
        m += 3;
        kt += 4;
        mt += 2;
      }
    }
    // do surgery to remove any missing spatial coordinates in texture
    if (!spatial_all_select) {
      tarray = (VisADTriangleStripArray) tarray.removeMissing();
    }
                                                                                                                   
    // do surgery along any longitude split (e.g., date line) in texture
    if (adaptedShadowType.getAdjustProjectionSeam()) {
      tarray = (VisADTriangleStripArray) tarray.adjustLongitude(renderer);
      tarray = (VisADTriangleStripArray) tarray.adjustSeam(renderer);
    }
                                                                                                                   
    // add texture as sub-node of group in scene graph
    if (!reuse) {
       BufferedImage image = tile.getImage(0);
       textureToGroup(group, tarray, image, mode, constant_alpha,
                      constant_color, texture_width, texture_height, true, true, tile);
    }
    else {
      if (animControl == null) {
        imgNode.setCurrent(0);
      }
    }

   }
// System.out.println("end curved texture " + (System.currentTimeMillis() - link.start_time));
  }

  public void buildLinearTexture(Object group, Set domain_set, Unit[] dataUnits, Unit[] domain_units,
                                 float[] default_values, ShadowRealType[] DomainComponents,
                                 int valueArrayLength, int[] inherited_values, int[] valueToScalar,
                                 GraphicsModeControl mode, float constant_alpha,
                                 float[] value_array, float[] constant_color, DisplayImpl display,
                                 VisADImageTile tile)
         throws VisADException, DisplayException {

    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    byte[] colors = null;
    int data_width = 0;
    int data_height = 0;
    int texture_width = 1;
    int texture_height = 1;

    Linear1DSet X = null;
    Linear1DSet Y = null;
    if (domain_set instanceof Linear2DSet) {
      X = ((Linear2DSet) domain_set).getX();
      Y = ((Linear2DSet) domain_set).getY();
    }
    else {
      X = ((LinearNDSet) domain_set).getLinear1DComponent(0);
      Y = ((LinearNDSet) domain_set).getLinear1DComponent(1);
    }
    float[][] limits = new float[2][2];
    limits[0][0] = (float) X.getFirst();
    limits[0][1] = (float) X.getLast();
    limits[1][0] = (float) Y.getFirst();
    limits[1][1] = (float) Y.getLast();
                                                                                                                       
    // get domain_set sizes
    data_width = X.getLength();
    data_height = Y.getLength();
    // texture sizes must be powers of two on older graphics cards
    texture_width = textureWidth(data_width);
    texture_height = textureHeight(data_height);
                                                                                                                       
                                                                                                                       
    // WLH 27 Jan 2003
    float half_width = 0.5f / ((float) (data_width - 1));
    float half_height = 0.5f / ((float) (data_height - 1));
    half_width = (limits[0][1] - limits[0][0]) * half_width;
    half_height = (limits[1][1] - limits[1][0]) * half_height;
    limits[0][0] -= half_width;
    limits[0][1] += half_width;
    limits[1][0] -= half_height;
    limits[1][1] += half_height;
                                                                                                                       
                                                                                                                       
    // convert values to default units (used in display)
    limits = Unit.convertTuple(limits, dataUnits, domain_units);

    int[] tuple_index = new int[3];
    if (DomainComponents.length != 2) {
      throw new DisplayException("texture domain dimension != 2:" +
                                 "ShadowFunctionOrSetType.doTransform");
    }
                                                                                                                       
    // find the spatial ScalarMaps
    for (int i=0; i<DomainComponents.length; i++) {
      Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
      ScalarMap map = (ScalarMap) maps.nextElement();
      // scale values
      limits[i] = map.scaleValues(limits[i]);
      DisplayRealType real = map.getDisplayScalar();
      DisplayTupleType tuple = real.getTuple();
      if (tuple == null ||
          !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
        throw new DisplayException("texture with bad tuple: " +
                                   "ShadowFunctionOrSetType.doTransform");
      }
      // get spatial index
      tuple_index[i] = real.getTupleIndex();
      if (maps.hasMoreElements()) {
        throw new DisplayException("texture with multiple spatial: " +
                                   "ShadowFunctionOrSetType.doTransform");
      }
    } // end for (int i=0; i<DomainComponents.length; i++)


    // get spatial index not mapped from domain_set
    tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
    DisplayRealType real = (DisplayRealType)
      Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
    int value2_index = display.getDisplayScalarIndex(real);
    float value2 = default_values[value2_index];
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0 &&
          real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
        // value for unmapped spatial dimension
        value2 = value_array[i];
        break;
      }
    }

    // create VisADQuadArray that texture is mapped onto
    coordinates = new float[12];
    // corner 0
    coordinates[tuple_index[0]] = limits[0][0];
    coordinates[tuple_index[1]] = limits[1][0];
    coordinates[tuple_index[2]] = value2;
    // corner 1
    //coordinates[3 + tuple_index[0]] = limits[0][1];
    //coordinates[3 + tuple_index[1]] = limits[1][0];
    coordinates[3 + tuple_index[0]] = limits[0][0];
    coordinates[3 + tuple_index[1]] = limits[1][1];
    coordinates[3 + tuple_index[2]] = value2;
    // corner 2
    coordinates[6 + tuple_index[0]] = limits[0][1];
    coordinates[6 + tuple_index[1]] = limits[1][1];
    coordinates[6 + tuple_index[2]] = value2;
    // corner 3
    //coordinates[9 + tuple_index[0]] = limits[0][0];
    //coordinates[9 + tuple_index[1]] = limits[1][1];
    coordinates[9 + tuple_index[0]] = limits[0][1];
    coordinates[9 + tuple_index[1]] = limits[1][0];
    coordinates[9 + tuple_index[2]] = value2;
                                                                                                                       
    // move image back in Java3D 2-D mode
    adjustZ(coordinates);
                                                                                                                       
    texCoords = new float[8];
    float ratiow = ((float) data_width) / ((float) texture_width);
    float ratioh = ((float) data_height) / ((float) texture_height);

    boolean yUp = true;
    setTexCoords(texCoords, ratiow, ratioh, yUp);
                                                                                                                       
    normals = new float[12];
    float n0 = ((coordinates[3+2]-coordinates[0+2]) *
                (coordinates[6+1]-coordinates[0+1])) -
               ((coordinates[3+1]-coordinates[0+1]) *
                (coordinates[6+2]-coordinates[0+2]));
    float n1 = ((coordinates[3+0]-coordinates[0+0]) *
                (coordinates[6+2]-coordinates[0+2])) -
               ((coordinates[3+2]-coordinates[0+2]) *
                (coordinates[6+0]-coordinates[0+0]));
    float n2 = ((coordinates[3+1]-coordinates[0+1]) *
                (coordinates[6+0]-coordinates[0+0])) -
               ((coordinates[3+0]-coordinates[0+0]) *
                (coordinates[6+1]-coordinates[0+1]));
                                                                                                                       
    float nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
    n0 = n0 / nlen;
    n1 = n1 / nlen;
    n2 = n2 / nlen;

    // corner 0
    normals[0] = n0;
    normals[1] = n1;
    normals[2] = n2;
    // corner 1
    normals[3] = n0;
    normals[4] = n1;
    normals[5] = n2;
    // corner 2
    normals[6] = n0;
    normals[7] = n1;
    normals[8] = n2;
    // corner 3
    normals[9] = n0;
    normals[10] = n1;
    normals[11] = n2;
                                                                                                                       
    colors = new byte[12];
    for (int i=0; i<12; i++) colors[i] = (byte) 127;
                                                                                                                       
    VisADQuadArray qarray = new VisADQuadArray();
    qarray.vertexCount = 4;
    qarray.coordinates = coordinates;
    qarray.texCoords = texCoords;
    qarray.colors = colors;
    qarray.normals = normals;
                                                                                                                       
    // add texture as sub-node of group in scene graph
    if (!reuse) {
      BufferedImage image = tile.getImage(0);
      textureToGroup(group, qarray, image, mode, constant_alpha,
                     constant_color, texture_width, texture_height, true, true, tile);
    }
    else {
      if (animControl == null) {
        imgNode.setCurrent(0);
      }
    }

  }

  //public CachedBufferedByteImage createImageByRef(final int texture_width, final int texture_height, final int imageType) {
  public BufferedImage createImageByRef(final int texture_width, final int texture_height, final int imageType) {
      return new BufferedImage(texture_width, texture_height, imageType);
  }

  public static float[][] getBounds(Set domain_set, float data_width, float data_height,
           float scaleX, float offsetX, float scaleY, float offsetY)
      throws VisADException
  {
    float[][] xyCoords = new float[2][4];

    float[][] coords0 = ((GriddedSet)domain_set).gridToValue(new float[][] {{0f},{0f}});
    float[][] coords1 = ((GriddedSet)domain_set).gridToValue(new float[][] {{0f},{(float)(data_height-1)}});
    float[][] coords2 = ((GriddedSet)domain_set).gridToValue(new float[][] {{(data_width-1f)},{(data_height-1f)}});
    float[][] coords3 = ((GriddedSet)domain_set).gridToValue(new float[][] {{(data_width-1f)},{0f}});

    float x0 = coords0[0][0];
    float y0 = coords0[1][0];
    float x1 = coords1[0][0];
    float y1 = coords1[1][0];
    float x2 = coords2[0][0];
    float y2 = coords2[1][0];
    float x3 = coords3[0][0];
    float y3 = coords3[1][0];

    xyCoords[0][0] = (x0 - offsetX)/scaleX;
    xyCoords[1][0] = (y0 - offsetY)/scaleY;

    xyCoords[0][1] = (x1 - offsetX)/scaleX;
    xyCoords[1][1] = (y1 - offsetY)/scaleY;

    xyCoords[0][2] = (x2 - offsetX)/scaleX;
    xyCoords[1][2] = (y2 - offsetY)/scaleY;

    xyCoords[0][3] = (x3 - offsetX)/scaleX;
    xyCoords[1][3] = (y3 - offsetY)/scaleY;

    return xyCoords;
  }

}


class SwitchNotify extends Switch {
  VisADImageNode imgNode;
  int numChildren;
  Switch swit;

  SwitchNotify(VisADImageNode imgNode, int numChildren) {
    super();
    this.imgNode = imgNode;
    this.numChildren = numChildren;
    this.swit = imgNode.getSwitch();
  }

  public int numChildren() {
    return numChildren;
  }

  public void setWhichChild(int index) {
    if (index == Switch.CHILD_NONE) {
      swit.setWhichChild(Switch.CHILD_NONE);
    }
    else if (index >= 0) {
      if ( swit.getWhichChild() == Switch.CHILD_NONE) {
        swit.setWhichChild(0);
      }
      imgNode.setCurrent(index);
    }
  }
}


class Mosaic {

  Tile[][] tiles;
  ArrayList<Tile>  tileList = new ArrayList<Tile>();

  int n_x_sub = 1;
  int n_y_sub = 1;

  Mosaic(int lenY, int limitY, int lenX, int limitX) {

    int y_sub_len = limitY;
    n_y_sub = lenY/y_sub_len;
    if (n_y_sub == 0) n_y_sub++;
    if ((lenY - n_y_sub*y_sub_len) > 4) n_y_sub += 1;

    int[][] y_start_stop = new int[n_y_sub][2];
    for (int k = 0; k < n_y_sub-1; k++) {
       y_start_stop[k][0] = k*y_sub_len - k;
       // +1: tiles overlap to fill texture gap
       // y_start_stop[k][1] = ((k+1)*y_sub_len - 1) + 1;
       y_start_stop[k][1] = ((k+1)*y_sub_len - 1) - k;
       // check that we don't exceed limit
       if ( ((y_start_stop[k][1]-y_start_stop[k][0])+1) > limitY) {
         y_start_stop[k][1] -= 1; //too big, take away gap fill
       }
    }
    int k = n_y_sub-1;
    y_start_stop[k][0] = k*y_sub_len - k;
    y_start_stop[k][1] = lenY - 1 - k;

    int x_sub_len = limitX;
    n_x_sub = lenX/x_sub_len;
    if (n_x_sub == 0) n_x_sub++;
    if ((lenX - n_x_sub*x_sub_len) > 4) n_x_sub += 1;

    int[][] x_start_stop = new int[n_x_sub][2];
    for (k = 0; k < n_x_sub-1; k++) {
      x_start_stop[k][0] = k*x_sub_len - k;
      // +1: tiles overlap to fill texture gap
      // x_start_stop[k][1] = ((k+1)*x_sub_len - 1) + 1;
      x_start_stop[k][1] = ((k+1)*x_sub_len - 1) - k;
      // check that we don't exceed limit
      if ( ((x_start_stop[k][1]-x_start_stop[k][0])+1) > limitX) {
        x_start_stop[k][1] -= 1; //too big, take away gap fill
      }
    }
    k = n_x_sub-1; 
    x_start_stop[k][0] = k*x_sub_len - k;
    x_start_stop[k][1] = lenX - 1 - k;

    tiles = new Tile[n_y_sub][n_x_sub];

    for (int j=0; j<n_y_sub; j++) {
      for (int i=0; i<n_x_sub; i++) {
         tiles[j][i] =
           new Tile(y_start_stop[j][0], y_start_stop[j][1], x_start_stop[i][0], x_start_stop[i][1]);
         tileList.add(tiles[j][i]);
      }
    }
  }

  Iterator iterator() {
    return tileList.iterator();
  }
}

class Tile {
   int y_start;
   int x_start;
   int y_stop;
   int x_stop;
 
   int height;
   int width;

   Tile(int y_start, int y_stop, int x_start, int x_stop) {
     this.y_start = y_start;
     this.y_stop = y_stop;
     this.x_start = x_start;
     this.x_stop = x_stop;
     
     height = y_stop - y_start + 1;
     width = x_stop - x_start + 1;
   }
}
