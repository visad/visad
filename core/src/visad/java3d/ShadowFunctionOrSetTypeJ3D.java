//
// ShadowFunctionOrSetTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;

import visad.*;
import visad.util.ThreadManager;

import javax.media.j3d.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.rmi.*;

import java.awt.image.*;


/**
   The ShadowFunctionOrSetTypeJ3D is an abstract parent for
   ShadowFunctionTypeJ3D and ShadowSetTypeJ3D.<P>
*/
public class ShadowFunctionOrSetTypeJ3D extends ShadowTypeJ3D {

  ShadowRealTupleTypeJ3D Domain;
  ShadowTypeJ3D Range; // null for ShadowSetTypeJ3D

  private Vector AccumulationVector = new Vector();

  boolean doTrajectory = false;
  boolean isAnimation1d = false;
  int domainLength = 0;
  int dataDomainLength = 0;
  Set anim1DdomainSet;
  Set domainSet;
  boolean post = false;

  double trajVisibilityTimeWindow;
  double trajRefreshInterval;
  double trajLifetime;
  int numIntrpPts;
  int trajSkip;
  TrajectoryParams.SmoothParams smoothParams;
  int direction;
  float[][] startPts = null;
  boolean trajDoIntrp = true;
  float trcrSize = 1f;

  List<BranchGroup> branches = null;
  Switch swit = null;

  Switch switB = null;

  SwitchListener switListen = null;

  public ShadowFunctionOrSetTypeJ3D(MathType t, DataDisplayLink link,
                                    ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);
    if (this instanceof ShadowFunctionTypeJ3D) {
      Domain = (ShadowRealTupleTypeJ3D)
               ((FunctionType) Type).getDomain().buildShadowType(link, this);
      Range = (ShadowTypeJ3D)
              ((FunctionType) Type).getRange().buildShadowType(link, this);
      adaptedShadowType =
        new ShadowFunctionType(t, link, getAdaptedParent(parent),
                       (ShadowRealTupleType) Domain.getAdaptedShadowType(),
                       Range.getAdaptedShadowType());
    }
    else {
      Domain = (ShadowRealTupleTypeJ3D)
               ((SetType) Type).getDomain().buildShadowType(Link, this);
      Range = null;
      adaptedShadowType =
        new ShadowSetType(t, link, getAdaptedParent(parent),
                       (ShadowRealTupleType) Domain.getAdaptedShadowType());
    }
  }

  public ShadowRealTupleTypeJ3D getDomain() {
    return Domain;
  }

  public ShadowTypeJ3D getRange() {
    return Range;
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
    if (this instanceof ShadowFunctionTypeJ3D) {
      Range.preProcess();
    }
  }

  /** transform data into a Java3D scene graph;
      add generated scene graph components as children of group;
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process */
  public boolean doTransform(Object group, Data data, final float[] value_array,
                             final float[] default_values, final DataRenderer renderer)
         throws VisADException, RemoteException {

    //boolean post = true; // FIXME what value for animation?
    //boolean isAnimation1d = false;
    boolean isTerminal = adaptedShadowType.getIsTerminal();
    
    ScalarMap timeMap = null; // used in the animation case to get control
    DataDisplayLink[] link_s = renderer.getLinks();
    DataDisplayLink link = link_s[0];
    Vector scalarMaps = link.getSelectedMapVector();

    
    // only determine if it's an animation if non-terminal. isTerminal will
    // only be determined if there are scalar maps - defaults to false
    if (!isTerminal && !scalarMaps.isEmpty()) {
      // determine if it's an animation
      MathType mtype = data.getType();
      if (mtype instanceof FunctionType) {
        int ani_map_idx = 0;
        FunctionType function = (FunctionType) mtype;
        RealTupleType functionD = function.getDomain();
        for (int kk = 0; kk < scalarMaps.size(); kk++) {
          ScalarMap scalarMap = (ScalarMap) scalarMaps.elementAt(kk);
          String scalar_name = scalarMap.getScalarName();
          if (scalar_name.equals(((RealType) functionD.getComponent(0)).getName())) {
            if (((scalarMap.getDisplayScalar()).equals(Display.Animation))
                && (functionD.getDimension() == 1)) {
              isAnimation1d = true;
              ani_map_idx = kk;
            }
          }
        }
        // animation domain
        timeMap = (ScalarMap) scalarMaps.elementAt(ani_map_idx);
      }

      // check for trajectory
      for (int kk=0; kk<scalarMaps.size(); kk++) {
        ScalarMap scalarMap = (ScalarMap) scalarMaps.elementAt(kk);
        DisplayRealType dspType = scalarMap.getDisplayScalar();
        if (dspType.equals(Display.Flow1X) || dspType.equals(Display.Flow1Y) || dspType.equals(Display.Flow1Z) ||
            dspType.equals(Display.Flow2X) || dspType.equals(Display.Flow2Y) || dspType.equals(Display.Flow2Z)) {

          FlowControl flwCntrl = (FlowControl) scalarMap.getControl();
          if (flwCntrl.trajectoryEnabled()) {
            doTrajectory = true;
            TrajectoryParams trajParams = flwCntrl.getTrajectoryParams();
            trajVisibilityTimeWindow = trajParams.getTrajVisibilityTimeWindow();
            trajRefreshInterval = trajParams.getTrajRefreshInterval();
            trajLifetime = trajRefreshInterval; // Default. Should be greater than or equal to refresh interval
            numIntrpPts = trajParams.getNumIntrpPts();
            trajSkip = trajParams.getStartSkip();
            smoothParams = trajParams.getSmoothParams();
            direction = trajParams.getDirection();
            startPts = trajParams.getStartPoints();
            trajDoIntrp = trajParams.getDoIntrp();
            trcrSize = trajParams.getMarkerSize();
            if (!trajDoIntrp) {
              numIntrpPts = 1;
            }
            break;
          }
          else {
            doTrajectory = false;
          }

        }
      }
    }

    // animation logic
    if (isAnimation1d) {
      
      // analyze data's domain (its a Field)
      domainSet = ((Field) data).getDomainSet();
      anim1DdomainSet = domainSet;

      // create and add switch with nodes for animation images
      domainLength = domainSet.getLength(); // num of domain nodes
      dataDomainLength = domainLength;
      swit = (Switch) makeSwitch(domainLength);
      AnimationControlJ3D control = (AnimationControlJ3D)timeMap.getControl();
      
      if (!doTrajectory) {
        addSwitch(group, swit, control, domainSet, renderer);
      }
      else {
        double[] times = Trajectory.getTimes((Gridded1DSet)anim1DdomainSet);
        java.util.Arrays.sort(times);
        int len = times.length;
        double avgTimeStep = (times[len-1] - times[0])/(len-1);
        int numNodes = (int) (trajVisibilityTimeWindow/avgTimeStep);
        int[] whichVisible = new int[numNodes];
        for (int i=0; i<numNodes; i++) whichVisible[i] = -((numNodes-1) - i);

        switListen = new SwitchListener(swit, domainLength, whichVisible);
        ((AVControlJ3D) control).addPair((Switch) switListen, domainSet, renderer);
        ((AVControlJ3D) control).init();
        BranchGroup branch = new BranchGroup();
        branch.setCapability(BranchGroup.ALLOW_DETACH);
        branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        branch.addChild((Switch) swit);
        ((Group) group).addChild(branch);

        // this node holds the trajectory tracer display geometry
        switB = (Switch) makeSwitch(domainLength);
        addSwitch(group, switB, control, domainSet, renderer);
      }
      

      /***
          Old code:
       // render frames
      for (int i=0; i<domainLength; i++) {
        BranchGroup node = (BranchGroup) swit.getChild(i);
        // not necessary, but perhaps if this is modified
        // int[] lat_lon_indices = renderer.getLatLonIndices();
        BranchGroup branch = (BranchGroup) makeBranch();
        recurseRange(branch, ((Field) data).getSample(i),
                     value_array, default_values, renderer);
        node.addChild(branch);
        // not necessary, but perhaps if this is modified
        // renderer.setLatLonIndices(lat_lon_indices);
      }
      ****/

      //jeffmc:First construct the branches
      branches = new ArrayList<BranchGroup>();
      for (int i=0; i<domainLength; i++) {
          BranchGroup node = (BranchGroup) swit.getChild(i);
          BranchGroup branch = (BranchGroup) makeBranch();
          branches.add(branch);
      }

      ThreadManager threadManager = new ThreadManager("animation rendering");
      for (int i=0; i<domainLength; i++) {
          final BranchGroup branch = (BranchGroup) branches.get(i);
          final Data sample  = ((Field) data).getSample(i);
          final BranchGroup node = (BranchGroup) swit.getChild(i);
          threadManager.addRunnable(new ThreadManager.MyRunnable() {
                  public void run()  throws Exception {
                      recurseRange(branch, sample,
                                   value_array, default_values, renderer);
                      if (!doTrajectory) {
                        node.addChild(branch);          
                      }
                  }
              });
      }

      if (doTrajectory) {
        post = false;
        threadManager.runSequentially();
      }
      else {
        post = false;
        threadManager.runInParallel();
      }
    }
    else {
      ShadowFunctionOrSetType shadow = (ShadowFunctionOrSetType)adaptedShadowType;
      post = shadow.doTransform(group, data, value_array, default_values, renderer, this); 
    }
    
    ensureNotEmpty(group);
    return post;
  }

  /**
   * Get the possibly adjusted texture width.
   * @param data_width The initial texture width.
   * @return If <code>DisplayImplJ3D.TEXTURE_NPOT</code> then return
   *  <code>data_width</code>, otherwise return the minimum power of two greater
   *  than <code>data_width</code>.
   * @see DisplayImplJ3D#TEXTURE_NPOT
   */
  public int textureWidth(int data_width) {
    if (DisplayImplJ3D.TEXTURE_NPOT) return data_width;
    // must be a power of 2 in Java3D
    int texture_width = 1;
    while (texture_width < data_width) texture_width *= 2;
    return texture_width;
  }

  /**
   * Get the possibly adjusted texture height.
   * @param data_height The initial texture height.
   * @return If <code>DisplayImplJ3D.TEXTURE_NPOT</code> then return
   *  <code>data_height</code>, otherwise return the minimum power of two greater
   *  than <code>data_height</code>.
   * @see DisplayImplJ3D#TEXTURE_NPOT
   */
  public int textureHeight(int data_height) {
    if (DisplayImplJ3D.TEXTURE_NPOT) return data_height;
    // must be a power of 2 in Java3D
    int texture_height = 1;
    while (texture_height < data_height) texture_height *= 2;
    return texture_height;
  }

  /**
   * Get the possibly adjusted texture depth.
   * @param data_depth The initial texture depth.
   * @return If <code>DisplayImplJ3D.TEXTURE_NPOT</code> then return
   *  <code>data_depth</code>, otherwise return the minimum power of two greater
   *  than <code>data_depth</code>.
   * @see DisplayImplJ3D#TEXTURE_NPOT
   */
  public int textureDepth(int data_depth) {
    if (DisplayImplJ3D.TEXTURE_NPOT) return data_depth;
    // must be a power of 2 in Java3D
    int texture_depth = 1;
    while (texture_depth < data_depth) texture_depth *= 2;
    return texture_depth;
  }

  public void adjustZ(float[] coordinates) {
    if (display.getDisplayRenderer().getMode2D()) {
      for (int i=2; i<coordinates.length; i+=3) {
        coordinates[i] = DisplayImplJ3D.BACK2D;
      }
    }
  }

  public int getImageComponentType(int buffImgType) {
    if (buffImgType == BufferedImage.TYPE_4BYTE_ABGR) {
      return ImageComponent2D.FORMAT_RGBA8;
    }
    else if (buffImgType == BufferedImage.TYPE_3BYTE_BGR) {
      return ImageComponent2D.FORMAT_RGB8;
    }
    else if (buffImgType == BufferedImage.TYPE_BYTE_GRAY) {
      return ImageComponent2D.FORMAT_CHANNEL8;
    }
    return ImageComponent2D.FORMAT_RGBA8;
  }

  public int getTextureType(int buffImgType) {
    if (buffImgType == BufferedImage.TYPE_4BYTE_ABGR) {
      return Texture2D.RGBA;
    }
    else if (buffImgType == BufferedImage.TYPE_3BYTE_BGR) {
      return Texture2D.RGB;
    }
    else if (buffImgType == BufferedImage.TYPE_BYTE_GRAY) {
      return Texture2D.LUMINANCE;
    }
    return Texture2D.RGBA;
  }

  public void setTexCoords(float[] texCoords, float ratiow, float ratioh) {
    setTexCoords(texCoords, ratiow, ratioh, false);
  }

  public void setTexCoords(float[] texCoords, float ratiow, float ratioh, boolean yUp) {
    if (!yUp) { // the default
      // corner 0
      texCoords[0] = 0.0f;
      texCoords[1] = 1.0f;
      // corner 1
      texCoords[2] = ratiow;
      texCoords[3] = 1.0f;
      // corner 2
      texCoords[4] = ratiow;
      texCoords[5] = 1.0f - ratioh;
      // corner 3
      texCoords[6] = 0.0f;
      texCoords[7] = 1.0f - ratioh;
    }
    else {  // yUp = true, for imageByReference=true
      // corner 0
      texCoords[0] = 0.0f;
      texCoords[1] = 0.0f;
      // corner 1
      texCoords[2] = 0.0f;
      texCoords[3] = ratioh;
      // corner 2
      texCoords[4] = ratiow;
      texCoords[5] = ratioh;
      // corner 3
      texCoords[6] = ratiow;
      texCoords[7] = 0.0f;
    }
  }

  public float[] setTex3DCoords(int length, int axis, float ratiow,
                                float ratioh, float ratiod) {
    // need to flip Y and Z in X and Y views?
    float[] texCoords = new float[12 * length];
    if (axis == 2) {
      for (int i=0; i<length; i++) {
        int i12 = i * 12;
        float depth = 0.0f + (ratiod - 0.0f) * i / (length - 1.0f);
        // corner 0
        texCoords[i12] = 0.0f;
        texCoords[i12 + 1] = 1.0f;
        texCoords[i12 + 2] = depth;
        // corner 1
        texCoords[i12 + 3] = ratiow;
        texCoords[i12 + 4] = 1.0f;
        texCoords[i12 + 5] = depth;
        // corner 2
        texCoords[i12 + 6] = ratiow;
        texCoords[i12 + 7] = 1.0f - ratioh;
        texCoords[i12 + 8] = depth;
        // corner 3
        texCoords[i12 + 9] = 0.0f;
        texCoords[i12 + 10] = 1.0f - ratioh;
        texCoords[i12 + 11] = depth;
      }
    }
    else if (axis == 1) {
      for (int i=0; i<length; i++) {
        int i12 = i * 12;
        float height = 1.0f - ratioh * i / (length - 1.0f);
        // corner 0
        texCoords[i12] = 0.0f;
        texCoords[i12 + 1] = height;
        texCoords[i12 + 2] = 0.0f;
        // corner 1
        texCoords[i12 + 3] = ratiow;
        texCoords[i12 + 4] = height;
        texCoords[i12 + 5] = 0.0f;
        // corner 2
        texCoords[i12 + 6] = ratiow;
        texCoords[i12 + 7] = height;
        texCoords[i12 + 8] = ratiod;
        // corner 3
        texCoords[i12 + 9] = 0.0f;
        texCoords[i12 + 10] = height;
        texCoords[i12 + 11] = ratiod;
      }
    }
    else if (axis == 0) {
      for (int i=0; i<length; i++) {
        int i12 = i * 12;
        float width = 0.0f + (ratiow - 0.0f) * i / (length - 1.0f);
        // corner 0
        texCoords[i12] = width;
        texCoords[i12 + 1] = 1.0f;
        texCoords[i12 + 2] = 0.0f;
        // corner 1
        texCoords[i12 + 3] = width;
        texCoords[i12 + 4] = 1.0f - ratioh;
        texCoords[i12 + 5] = 0.0f;
        // corner 2
        texCoords[i12 + 6] = width;
        texCoords[i12 + 7] = 1.0f - ratioh;
        texCoords[i12 + 8] = ratiod;
        // corner 3
        texCoords[i12 + 9] = width;
        texCoords[i12 + 10] = 1.0f;
        texCoords[i12 + 11] = ratiod;
      }
    }
    return texCoords;
  }

  // WLH 17 March 2000
  private static float EPS = 0.00f;

  public float[] setTexStackCoords(int length, int axis, float ratiow,
                                   float ratioh, float ratiod) {
    float[] texCoords = new float[8 * length];
    if (axis == 2) {
      for (int i=0; i<length; i++) {
        int i8 = i * 8;
        // corner 0
        texCoords[i8] = 0.0f + EPS;
        texCoords[i8 + 1] = 1.0f - EPS;
        // corner 1
        texCoords[i8 + 2] = ratiow - EPS;
        texCoords[i8 + 3] = 1.0f - EPS;
        // corner 2
        texCoords[i8 + 4] = ratiow - EPS;
        texCoords[i8 + 5] = 1.0f - ratioh + EPS;
        // corner 3
        texCoords[i8 + 6] = 0.0f + EPS;
        texCoords[i8 + 7] = 1.0f - ratioh + EPS;
      }
    }
    else if (axis == 1) {
      // WLH 23 Feb 2000 - flip Z
      for (int i=0; i<length; i++) {
        int i8 = i * 8;
        // corner 0
        texCoords[i8] = 0.0f + EPS;
        texCoords[i8 + 1] = 1.0f - EPS;
        // corner 1
        texCoords[i8 + 2] = ratiow - EPS;
        texCoords[i8 + 3] = 1.0f - EPS;
        // corner 2
        texCoords[i8 + 4] = ratiow - EPS;
        texCoords[i8 + 5] = 1.0f - ratiod + EPS;
        // corner 3
        texCoords[i8 + 6] = 0.0f + EPS;
        texCoords[i8 + 7] = 1.0f - ratiod + EPS;
      }
    }
    else if (axis == 0) {
      // WLH 23 Feb 2000 - flip Y and Z
      for (int i=0; i<length; i++) {
        int i8 = i * 8;
        // corner 0
        texCoords[i8] = 0.0f + EPS;
        texCoords[i8 + 1] = 1.0f - EPS;
        // corner 1
        texCoords[i8 + 2] = ratioh - EPS;
        texCoords[i8 + 3] = 1.0f - EPS;
        // corner 2
        texCoords[i8 + 4] = ratioh - EPS;
        texCoords[i8 + 5] = 1.0f - ratiod + EPS;
        // corner 3
        texCoords[i8 + 6] = 0.0f + EPS;
        texCoords[i8 + 7] = 1.0f - ratiod + EPS;
      }
    }
/* WLH 17 March 2000
    if (axis == 2) {
      for (int i=0; i<length; i++) {
        int i8 = i * 8;
        // corner 0
        texCoords[i8] = 0.0f;
        texCoords[i8 + 1] = 1.0f;
        // corner 1
        texCoords[i8 + 2] = ratiow;
        texCoords[i8 + 3] = 1.0f;
        // corner 2
        texCoords[i8 + 4] = ratiow;
        texCoords[i8 + 5] = 1.0f - ratioh;
        // corner 3
        texCoords[i8 + 6] = 0.0f;
        texCoords[i8 + 7] = 1.0f - ratioh;
      }
    }
    else if (axis == 1) {
      // WLH 23 Feb 2000 - flip Z
      for (int i=0; i<length; i++) {
        int i8 = i * 8;
        // corner 0
        texCoords[i8] = 0.0f;
        texCoords[i8 + 1] = 1.0f;
        // corner 1
        texCoords[i8 + 2] = ratiow;
        texCoords[i8 + 3] = 1.0f;
        // corner 2
        texCoords[i8 + 4] = ratiow;
        texCoords[i8 + 5] = 1.0f - ratiod;
        // corner 3
        texCoords[i8 + 6] = 0.0f;
        texCoords[i8 + 7] = 1.0f - ratiod;
      }
    }
    else if (axis == 0) {
      // WLH 23 Feb 2000 - flip Y and Z
      for (int i=0; i<length; i++) {
        int i8 = i * 8;
        // corner 0
        texCoords[i8] = 0.0f;
        texCoords[i8 + 1] = 1.0f;
        // corner 1
        texCoords[i8 + 2] = ratioh;
        texCoords[i8 + 3] = 1.0f;
        // corner 2
        texCoords[i8 + 4] = ratioh;
        texCoords[i8 + 5] = 1.0f - ratiod;
        // corner 3
        texCoords[i8 + 6] = 0.0f;
        texCoords[i8 + 7] = 1.0f - ratiod;
      }
    }
*/
    return texCoords;
  }

  public Vector getTextMaps(int i, int[] textIndices) {
    if (i < 0) {
      return ((ShadowTextTypeJ3D) Range).getSelectedMapVector();
    }
    else {
      ShadowTextTypeJ3D text = (ShadowTextTypeJ3D)
        ((ShadowTupleTypeJ3D) Range).getComponent(textIndices[i]);
      return text.getSelectedMapVector();
    }
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height, boolean byReference, boolean yUp, VisADImageTile tile) throws VisADException {
    textureToGroup(group, array, image, mode, constant_alpha, constant_color, texture_width, texture_height, byReference, yUp, tile, false);
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height) throws VisADException {
    textureToGroup(group, array, image, mode, constant_alpha, constant_color, texture_width, texture_height, false, false, null, false);
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height, 
                            boolean byReference, boolean yUp, VisADImageTile tile, boolean smoothen)
         throws VisADException {
    GeometryArray geometry = display.makeGeometry(array);
    // System.out.println("texture geometry");
    // create basic Appearance
    TransparencyAttributes c_alpha = null;

    if (constant_alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = null;
    }
    else if (constant_alpha == constant_alpha) {
      	//12NOV2012: GHANSHAM Use inversed alpha for 3 byte buffered images too
        int image_type = image.getType();
        boolean inversed_alpha = (image_type == BufferedImage.TYPE_3BYTE_BGR) || (image_type == BufferedImage.TYPE_BYTE_GRAY);
      	c_alpha = new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                (inversed_alpha)? (1.0f - constant_alpha): constant_alpha);
      c_alpha.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE); //REUSE GEOMETRY/COLORBYTE REQUIREMENT
    }
    else {
      c_alpha = new TransparencyAttributes();
      c_alpha.setTransparencyMode(TransparencyAttributes.BLENDED);
      c_alpha.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE); //REUSE GEOMETRY/COLORBYTE REQUIREMENT
    }
    ColoringAttributes c_color = null;
    if (constant_color != null && constant_color.length == 3) {
      c_color = new ColoringAttributes();
      c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
    }
    Appearance appearance =
      makeAppearance(mode, c_alpha, null, geometry, false);
    appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE); //REUSE GEOMETRY/COLORBYTE REQUIREMENT
    // create TextureAttributes
    TextureAttributes texture_attributes = new TextureAttributes();

    // WLH 20 June 2001
    if (smoothen) {
      texture_attributes.setTextureMode(TextureAttributes.MODULATE);
    } else {
      texture_attributes.setTextureMode(TextureAttributes.REPLACE);
    }

    texture_attributes.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
    appearance.setTextureAttributes(texture_attributes);
    // create Texture2D
// TextureLoader uses 1st argument = 1
/*
System.out.println("Texture.BASE_LEVEL = " + Texture.BASE_LEVEL); // 1
System.out.println("Texture.RGBA = " + Texture.RGBA); // 6
*/
    Texture2D texture = new Texture2D(Texture.BASE_LEVEL, getTextureType(image.getType()),
                                      texture_width, texture_height);
    texture.setCapability(Texture.ALLOW_IMAGE_READ);
    ImageComponent2D image2d =
      new ImageComponent2D(getImageComponentType(image.getType()), image, byReference, yUp);
    image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
    if (byReference) {
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
    }
    texture.setImage(0, image2d);

    //
    // from TextureLoader
    // TextureLoader uses 3 for both setMinFilter and setMagFilter
/*
System.out.println("Texture.FASTEST = " + Texture.FASTEST); // 0
System.out.println("Texture.NICEST = " + Texture.NICEST); // 1
System.out.println("Texture.BASE_LEVEL_POINT = " + Texture.BASE_LEVEL_POINT); // 2
System.out.println("Texture.BASE_LEVEL_LINEAR = " + Texture.BASE_LEVEL_LINEAR); // 3
*/
/* for interpolation:
    texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
    texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
*/
    if (smoothen) {
      texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
      texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
    } else {
      texture.setMinFilter(Texture.BASE_LEVEL_POINT);
      texture.setMagFilter(Texture.BASE_LEVEL_POINT);
    }

    texture.setEnable(true);
    // end of from TextureLoader
    //
    Shape3D shape = new Shape3D(geometry, appearance);
    shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);	//REUSE GEOMETRY/COLORBYTE REQUIREMENT
    shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    appearance.setTexture(texture);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);  //REUSE GEOMETRY/COLORBYTE REQUIREMENT

    // WLH 6 April 2000
    // ((Group) group).addChild(shape);
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.addChild(shape);
    if (((Group) group).numChildren() > 0) {
      ((Group) group).setChild(branch, 0);
    }
    else {
      ((Group) group).addChild(branch);
    }

    if (tile != null) {
      tile.setImageComponent(image2d);
    }
  }

  public void texture3DToGroup(Object group, VisADGeometryArray arrayX,
                    VisADGeometryArray arrayY, VisADGeometryArray arrayZ,
                    VisADGeometryArray arrayXrev,
                    VisADGeometryArray arrayYrev,
                    VisADGeometryArray arrayZrev,
                    BufferedImage[] images, GraphicsModeControl mode,
                    float constant_alpha, float[] constant_color,
                    int texture_width, int texture_height,
                    int texture_depth, DataRenderer renderer)
         throws VisADException {

    GeometryArray geometryX = display.makeGeometry(arrayX);
    GeometryArray geometryY = display.makeGeometry(arrayY);
    GeometryArray geometryZ = display.makeGeometry(arrayZ);
    GeometryArray geometryXrev = display.makeGeometry(arrayXrev);
    GeometryArray geometryYrev = display.makeGeometry(arrayYrev);
    GeometryArray geometryZrev = display.makeGeometry(arrayZrev);
    // System.out.println("texture geometry");
    // create basic Appearance
    TransparencyAttributes c_alpha = null;

    if (constant_alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = null;
    }
    else if (constant_alpha == constant_alpha) {
      // c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
      c_alpha = new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           constant_alpha);
    }
    else {
      c_alpha = new TransparencyAttributes();
      c_alpha.setTransparencyMode(TransparencyAttributes.BLENDED);
    }
    ColoringAttributes c_color = null;
    if (constant_color != null && constant_color.length == 3) {
      c_color = new ColoringAttributes();
      c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
    }
    Appearance appearance =
      makeAppearance(mode, c_alpha, null, geometryX, true);
    // create TextureAttributes
    TextureAttributes texture_attributes = new TextureAttributes();
    // texture_attributes.setTextureMode(TextureAttributes.REPLACE);
    texture_attributes.setTextureMode(TextureAttributes.MODULATE);
    texture_attributes.setPerspectiveCorrectionMode(
                          TextureAttributes.NICEST);
    appearance.setTextureAttributes(texture_attributes);
    // create Texture2D
// TextureLoader uses 1st argument = 1
/*
System.out.println("Texture.BASE_LEVEL = " + Texture.BASE_LEVEL); // 1
System.out.println("Texture.RGBA = " + Texture.RGBA); // 6
*/
    Texture3D texture = new Texture3D(Texture.BASE_LEVEL, Texture.RGBA,
                          texture_width, texture_height, texture_depth);
    texture.setCapability(Texture.ALLOW_IMAGE_READ);
    ImageComponent3D image3d =
      new ImageComponent3D(ImageComponent.FORMAT_RGBA, texture_width,
                           texture_height, texture_depth);
    image3d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
    for (int i=0; i<texture_depth; i++) {
      image3d.set(i, images[i]);
      images[i] = null; // take out the garbage
    }
    texture.setImage(0, image3d);
    //
    // from TextureLoader
    // TextureLoader uses 3 for both setMinFilter and setMagFilter
/*
System.out.println("Texture.FASTEST = " + Texture.FASTEST); // 0
System.out.println("Texture.NICEST = " + Texture.NICEST); // 1
System.out.println("Texture.BASE_LEVEL_POINT = " + Texture.BASE_LEVEL_POINT); // 2
System.out.println("Texture.BASE_LEVEL_LINEAR = " + Texture.BASE_LEVEL_LINEAR); // 3
*/
/* for interpolation: */
    texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
    texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
/* for sampling:
    texture.setMinFilter(Texture.BASE_LEVEL_POINT);
    texture.setMagFilter(Texture.BASE_LEVEL_POINT);
*/
    texture.setEnable(true);
    // end of from TextureLoader

    // OK to share appearance ??
    Shape3D shapeX = new Shape3D(geometryX, appearance);
    shapeX.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    Shape3D shapeY = new Shape3D(geometryY, appearance);
    shapeY.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    Shape3D shapeZ = new Shape3D(geometryZ, appearance);
    shapeZ.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    Shape3D shapeXrev = new Shape3D(geometryXrev, appearance);
    Shape3D shapeYrev = new Shape3D(geometryYrev, appearance);
    Shape3D shapeZrev = new Shape3D(geometryZrev, appearance);
    appearance.setTexture(texture);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);

    shapeX.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shapeX.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    shapeY.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shapeY.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    shapeZ.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shapeZ.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    shapeXrev.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shapeXrev.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    shapeYrev.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shapeYrev.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    shapeZrev.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shapeZrev.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    
    Switch swit = (Switch) makeSwitch();
    swit.addChild(shapeX);
    swit.addChild(shapeY);
    swit.addChild(shapeZ);
    swit.addChild(shapeXrev);
    swit.addChild(shapeYrev);
    swit.addChild(shapeZrev);

    // WLH 6 April 2000
    // ((Group) group).addChild(swit);
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.addChild(swit);
    if (((Group) group).numChildren() > 0) {
      ((Group) group).setChild(branch, 0);
    }
    else {
      ((Group) group).addChild(branch);
    }

    ProjectionControlJ3D control =
      (ProjectionControlJ3D) display.getProjectionControl();
    control.addPair(swit, renderer);
  }

  public void textureStackToGroup(Object group, VisADGeometryArray arrayX,
                    VisADGeometryArray arrayY, VisADGeometryArray arrayZ,
                    VisADGeometryArray arrayXrev,
                    VisADGeometryArray arrayYrev,
                    VisADGeometryArray arrayZrev,
                    BufferedImage[] imagesX,
                    BufferedImage[] imagesY,
                    BufferedImage[] imagesZ,
                    GraphicsModeControl mode,
                    float constant_alpha, float[] constant_color,
                    int texture_width, int texture_height,
                    int texture_depth, DataRenderer renderer)
         throws VisADException {

    GeometryArray[] geometryX = makeGeometrys(arrayX);
    GeometryArray[] geometryY = makeGeometrys(arrayY);
    GeometryArray[] geometryZ = makeGeometrys(arrayZ);
/* not needed ??
    GeometryArray[] geometryXrev = makeGeometrys(arrayXrev);
    GeometryArray[] geometryYrev = makeGeometrys(arrayYrev);
    GeometryArray[] geometryZrev = makeGeometrys(arrayZrev);
*/

    int nx = arrayX.coordinates.length;
    boolean flipX = (arrayX.coordinates[0] > arrayX.coordinates[nx-3]);
    int ny = arrayY.coordinates.length;
    boolean flipY = (arrayY.coordinates[1] > arrayY.coordinates[ny-2]);
    int nz = arrayZ.coordinates.length;
    boolean flipZ = (arrayZ.coordinates[2] > arrayZ.coordinates[nz-1]);
    // System.out.println("flipX = " + flipX + " flipY = " + flipY +
    //                    " flipZ = " + flipZ);

    // create Attributes for Appearances
    TransparencyAttributes c_alpha = null;
    if (constant_alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = null;
    }
    else if (constant_alpha == constant_alpha) {
      // c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
      c_alpha = new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           constant_alpha);
    }
    else {
      c_alpha = new TransparencyAttributes();
      c_alpha.setTransparencyMode(TransparencyAttributes.BLENDED);
    }
    ColoringAttributes c_color = null;
    if (constant_color != null && constant_color.length == 3) {
      c_color = new ColoringAttributes();
      c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
    }
    TextureAttributes texture_attributes = new TextureAttributes();

    // WLH 17 March 2000
    // texture_attributes.setTextureMode(TextureAttributes.MODULATE);
    texture_attributes.setTextureMode(TextureAttributes.REPLACE);

    texture_attributes.setPerspectiveCorrectionMode(
                          TextureAttributes.NICEST);

    int transparencyMode = mode.getTransparencyMode();

    OrderedGroup branchX = new OrderedGroup();
    branchX.setCapability(Group.ALLOW_CHILDREN_READ);
    int data_depth = geometryX.length;
    Shape3D[] shapeX = new Shape3D[data_depth];
    for (int ii=0; ii<data_depth; ii++) {
      int i = flipX ? data_depth-1-ii : ii;
      int width = imagesX[i].getWidth();
      int height = imagesX[i].getHeight();
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        width, height);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      ImageComponent2D image2d =
        new ImageComponent2D(ImageComponent.FORMAT_RGBA, imagesX[i]);
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      texture.setImage(0, image2d);
      Appearance appearance =
        makeAppearance(mode, c_alpha, null, geometryX[i], true);
      appearance.setTextureAttributes(texture_attributes);
      // WLH 17 March 2000
      if (transparencyMode == TransparencyAttributes.FASTEST) {
        texture.setMinFilter(Texture.BASE_LEVEL_POINT);
        texture.setMagFilter(Texture.BASE_LEVEL_POINT);
      }
      else {
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
      }
      texture.setEnable(true);
      appearance.setTexture(texture);
      appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      shapeX[i] = new Shape3D(geometryX[i], appearance);
      shapeX[i].setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shapeX[i].setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      branchX.addChild(shapeX[i]);
    }
    OrderedGroup branchXrev = new OrderedGroup();
    branchXrev.setCapability(Group.ALLOW_CHILDREN_READ);
    for (int ii=data_depth-1; ii>=0; ii--) {
      int i = flipX ? data_depth-1-ii : ii;
      int width = imagesX[i].getWidth();
      int height = imagesX[i].getHeight();
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        width, height);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      ImageComponent2D image2d =
        new ImageComponent2D(ImageComponent.FORMAT_RGBA, imagesX[i]);
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      texture.setImage(0, image2d);
      Appearance appearance =
        makeAppearance(mode, c_alpha, null, geometryX[i], true);
      appearance.setTextureAttributes(texture_attributes);
      // WLH 17 March 2000
      if (transparencyMode == TransparencyAttributes.FASTEST) {
        texture.setMinFilter(Texture.BASE_LEVEL_POINT);
        texture.setMagFilter(Texture.BASE_LEVEL_POINT);
      }
      else {
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
      }
      texture.setEnable(true);
      appearance.setTexture(texture);
      appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      shapeX[i] = new Shape3D(geometryX[i], appearance);
      shapeX[i].setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shapeX[i].setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      branchXrev.addChild(shapeX[i]);
    }
    shapeX = null;

    OrderedGroup branchY = new OrderedGroup();
    branchY.setCapability(Group.ALLOW_CHILDREN_READ);
    int data_height = geometryY.length;
    Shape3D[] shapeY = new Shape3D[data_height];
    for (int ii=0; ii<data_height; ii++) {
      int i = flipY ? data_height-1-ii : ii;
      int width = imagesY[i].getWidth();
      int height = imagesY[i].getHeight();
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        width, height);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      // flip texture on Y axis
      ImageComponent2D image2d =
        new ImageComponent2D(ImageComponent.FORMAT_RGBA, imagesY[i]);
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      texture.setImage(0, image2d);
      Appearance appearance =
        makeAppearance(mode, c_alpha, null, geometryY[i], true);
      appearance.setTextureAttributes(texture_attributes);
      // WLH 17 March 2000
      if (transparencyMode == TransparencyAttributes.FASTEST) {
        texture.setMinFilter(Texture.BASE_LEVEL_POINT);
        texture.setMagFilter(Texture.BASE_LEVEL_POINT);
      }
      else {
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
      }
      texture.setEnable(true);
      appearance.setTexture(texture);
      appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      shapeY[i] = new Shape3D(geometryY[i], appearance);
      shapeY[i].setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shapeY[i].setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      branchY.addChild(shapeY[i]);
    }
    OrderedGroup branchYrev = new OrderedGroup();
    branchYrev.setCapability(Group.ALLOW_CHILDREN_READ);
    for (int ii=data_height-1; ii>=0; ii--) {
      int i = flipY ? data_height-1-ii : ii;
      int width = imagesY[i].getWidth();
      int height = imagesY[i].getHeight();
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        width, height);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      // flip texture on Y axis
      ImageComponent2D image2d =
        new ImageComponent2D(ImageComponent.FORMAT_RGBA, imagesY[i]);
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      texture.setImage(0, image2d);
      Appearance appearance =
        makeAppearance(mode, c_alpha, null, geometryY[i], true);
      appearance.setTextureAttributes(texture_attributes);
      // WLH 17 March 2000
      if (transparencyMode == TransparencyAttributes.FASTEST) {
        texture.setMinFilter(Texture.BASE_LEVEL_POINT);
        texture.setMagFilter(Texture.BASE_LEVEL_POINT);
      }
      else {
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
      }
      texture.setEnable(true);
      appearance.setTexture(texture);
      appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      shapeY[i] = new Shape3D(geometryY[i], appearance);
      shapeY[i].setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shapeY[i].setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      branchYrev.addChild(shapeY[i]);
    }
    shapeY = null;

    OrderedGroup branchZ = new OrderedGroup();
    branchZ.setCapability(Group.ALLOW_CHILDREN_READ);
    int data_width = geometryZ.length;
    Shape3D[] shapeZ = new Shape3D[data_width];
    for (int ii=0; ii<data_width; ii++) {
      int i = flipZ ? data_width-1-ii : ii;
      int width = imagesZ[i].getWidth();
      int height = imagesZ[i].getHeight();
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        width, height);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      ImageComponent2D image2d =
        new ImageComponent2D(ImageComponent.FORMAT_RGBA, imagesZ[i]);
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      texture.setImage(0, image2d);
      Appearance appearance =
        makeAppearance(mode, c_alpha, null, geometryZ[i], true);
      appearance.setTextureAttributes(texture_attributes);
      // WLH 17 March 2000
      if (transparencyMode == TransparencyAttributes.FASTEST) {
        texture.setMinFilter(Texture.BASE_LEVEL_POINT);
        texture.setMagFilter(Texture.BASE_LEVEL_POINT);
      }
      else {
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
      }
      texture.setEnable(true);
      appearance.setTexture(texture);
      appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      shapeZ[i] = new Shape3D(geometryZ[i], appearance);
      shapeZ[i].setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shapeZ[i].setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      branchZ.addChild(shapeZ[i]);
    }
    OrderedGroup branchZrev = new OrderedGroup();
    branchZrev.setCapability(Group.ALLOW_CHILDREN_READ);
    for (int ii=data_width-1; ii>=0; ii--) {
      int i = flipZ ? data_width-1-ii : ii;
      int width = imagesZ[i].getWidth();
      int height = imagesZ[i].getHeight();
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        width, height);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      ImageComponent2D image2d =
        new ImageComponent2D(ImageComponent.FORMAT_RGBA, imagesZ[i]);
      image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      texture.setImage(0, image2d);
      Appearance appearance =
        makeAppearance(mode, c_alpha, null, geometryZ[i], true);
      appearance.setTextureAttributes(texture_attributes);
      // WLH 17 March 2000
      if (transparencyMode == TransparencyAttributes.FASTEST) {
        texture.setMinFilter(Texture.BASE_LEVEL_POINT);
        texture.setMagFilter(Texture.BASE_LEVEL_POINT);
      }
      else {
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
      }
      texture.setEnable(true);
      appearance.setTexture(texture);
      appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      shapeZ[i] = new Shape3D(geometryZ[i], appearance);
      shapeZ[i].setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shapeZ[i].setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      branchZrev.addChild(shapeZ[i]);
    }
    shapeZ = null;

    Switch swit = (Switch) makeSwitch();
    swit.addChild(branchX);
    swit.addChild(branchY);
    swit.addChild(branchZ);
    swit.addChild(branchXrev);
    swit.addChild(branchYrev);
    swit.addChild(branchZrev);

    // WLH 6 April 2000
    // ((Group) group).addChild(swit);
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.addChild(swit);
    if (((Group) group).numChildren() > 0) {
      ((Group) group).setChild(branch, 0);
    }
    else {
      ((Group) group).addChild(branch);
    }

    ProjectionControlJ3D control =
      (ProjectionControlJ3D) display.getProjectionControl();
    control.addPair(swit, renderer);
  }

/*
  GeometryArray[] makeGeometrys(VisADGeometryArray array)
                  throws VisADException {
    int count = array.vertexCount;
    int depth = count / 4;
    int color_length = array.colors.length / count;
    int tex_length = array.texCoords.length / count;

    GeometryArray[] geometrys = new GeometryArray[depth];
    for (int d=0; d<depth; d++) {
      int i12 = d * 4 * 3;
      int i4c = d * 4 * color_length;
      int i4t = d * 4 * tex_length;
      VisADQuadArray qarray = new VisADQuadArray();
      qarray.vertexCount = 4;
      qarray.coordinates = new float[12];
      qarray.texCoords = new float[tex_length * 4];
      qarray.colors = new byte[color_length * 4];
      qarray.normals = new float[12];
      for (int i=0; i<12; i++) {
        qarray.coordinates[i] = array.coordinates[i12 + i];
        qarray.normals[i] = array.normals[i12 + i];
      }
      for (int i=0; i<4*color_length; i++) {
        qarray.colors[i] = array.colors[i4c + i];
      }
      for (int i=0; i<4*tex_length; i++) {
        qarray.texCoords[i] = array.texCoords[i4t + i];
      }
      geometrys[d] = display.makeGeometry(qarray);
    }
    return geometrys;
  }
*/

  public GeometryArray[] makeGeometrys(VisADGeometryArray array)
                  throws VisADException {
    int count = array.vertexCount;
    int depth = count / 4;
    VisADGeometryArray[] qarrays = makeVisADGeometrys(array);
    GeometryArray[] geometrys = new GeometryArray[depth];
    for (int d=0; d<depth; d++) {
      geometrys[d] = display.makeGeometry(qarrays[d]);
    }
    return geometrys;
  }

  public VisADGeometryArray[] makeVisADGeometrys(VisADGeometryArray array)
                  throws VisADException {
    int count = array.vertexCount;
    int depth = count / 4;
    int color_length = array.colors.length / count;
    int tex_length = array.texCoords.length / count;

    VisADGeometryArray[] geometrys = new VisADGeometryArray[depth];
    for (int d=0; d<depth; d++) {
      int i12 = d * 4 * 3;
      int i4c = d * 4 * color_length;
      int i4t = d * 4 * tex_length;
      VisADQuadArray qarray = new VisADQuadArray();
      qarray.vertexCount = 4;
      qarray.coordinates = new float[12];
      qarray.texCoords = new float[tex_length * 4];
      qarray.colors = new byte[color_length * 4];
      qarray.normals = new float[12];
      for (int i=0; i<12; i++) {
        qarray.coordinates[i] = array.coordinates[i12 + i];
        qarray.normals[i] = array.normals[i12 + i];
      }
      for (int i=0; i<4*color_length; i++) {
        qarray.colors[i] = array.colors[i4c + i];
      }
      for (int i=0; i<4*tex_length; i++) {
        qarray.texCoords[i] = array.texCoords[i4t + i];
      }
      geometrys[d] = qarray;
    }
    return geometrys;
  }

  public Object makeSwitch() {
    Switch swit = new Switch(Switch.CHILD_MASK);
    swit.setCapability(Switch.ALLOW_SWITCH_READ);
    swit.setCapability(Switch.ALLOW_SWITCH_WRITE);
    swit.setCapability(BranchGroup.ALLOW_DETACH);
    swit.setCapability(Group.ALLOW_CHILDREN_READ);
    swit.setCapability(Group.ALLOW_CHILDREN_WRITE);
    return swit;
  }

  public Object makeSwitch(int length) throws VisADException {
    Switch swit = (Switch)makeSwitch();

//  -TDR
    swit.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    for (int i=0; i<length; i++) {
      BranchGroup node = new BranchGroup();
      node.setCapability(BranchGroup.ALLOW_DETACH);
      node.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      node.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      node.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      ensureNotEmpty(node);
      addToSwitch(swit, node);
    }
    return swit;
  }
  
  public Object makeBranch() {
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    return branch;
  }

  public void addToGroup(Object group, Object branch)
         throws VisADException {
/* WLH 18 Aug 98
   empty BranchGroup or Shape3D may cause NullPointerException
   from Shape3DRetained.setLive
*/
    ensureNotEmpty((BranchGroup) branch);
    ((BranchGroup) group).addChild((BranchGroup) branch);
  }

  public void addToSwitch(Object swit, Object branch)
         throws VisADException {
/* WLH 18 Aug 98
   empty BranchGroup or Shape3D may cause NullPointerException
   from Shape3DRetained.setLive
*/
    ensureNotEmpty((BranchGroup) branch);
    ((Switch) swit).addChild((BranchGroup) branch);
  }

  public void addSwitch(Object group, Object swit, Control control,
                        Set domain_set, DataRenderer renderer)
         throws VisADException {
    ((AVControlJ3D) control).addPair((Switch) swit, domain_set, renderer);
    ((AVControlJ3D) control).init();
    // WLH 06 Feb 06 - fix problem adding a new switch to an existing group
    // ((Group) group).addChild((Switch) swit);
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.addChild((Switch) swit);
    ((Group) group).addChild(branch);

  }

  public boolean recurseRange(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return Range.doTransform(group, data, value_array,
                             default_values, renderer);
  }

  public boolean wantIndexed() {
/* doesn't seem to matter to memory use
    return true;
*/
    return false;
  }

  public void postProcessTraj(Object group) throws VisADException {
    if (!doTrajectory) {
        return;
    }
    int numChildren = swit.numChildren();
    try {
       doTrajectory();
    } catch (Exception e) {
       e.printStackTrace();
    }
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Object group) throws VisADException {
    if (((ShadowFunctionOrSetType) adaptedShadowType).getFlat()) {
      int LevelOfDifficulty = getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowFunctionOrSetTypeJ3D.postProcess");
      }
      else {
        // includes !isTerminal
        // nothing to do
      }
    }
    else {
      if (this instanceof ShadowFunctionTypeJ3D) {
        Range.postProcess(group);
      }
    }
    AccumulationVector.removeAllElements();
  }

  private void doTrajectory() throws VisADException, RemoteException {
     /* Get start points, use first spatial_set locs for now. 
        Eventually want to include a time for start */
     ArrayList<FlowInfo> flowInfoList = Range.getAdaptedShadowType().getFlowInfo();
     FlowInfo info = flowInfoList.get(0);
     Gridded3DSet spatial_set0 = (Gridded3DSet) info.spatial_set;
     int manifoldDim = spatial_set0.getManifoldDimension();
     int[] lens = spatial_set0.getLengths();
     float[][] setLocs = spatial_set0.getSamples(false);
     GriddedSet spatialSetTraj = null;
     if (manifoldDim == 2) {
         spatialSetTraj = new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple,
                new float[][] {setLocs[0], setLocs[1]}, lens[0], lens[1]);
     } else {
         spatialSetTraj = spatial_set0;
     }

     byte[][] color_values = info.color_values;
     int clrDim = color_values.length;
     if (info.trajColors != null) color_values = info.trajColors;

     Trajectory.markGrid = new boolean[spatial_set0.getLength()];
     Trajectory.markGridTime = new int[spatial_set0.getLength()];
     java.util.Arrays.fill(Trajectory.markGrid, false);
     java.util.Arrays.fill(Trajectory.markGridTime, 0);

     byte[][] startClrs = new byte[color_values.length][];
     if (startPts == null) { //get from domain set
       startPts = new float[3][];
       Trajectory.getStartPointsFromDomain(trajSkip, spatial_set0, color_values, startPts, startClrs);
     }
     else {
       /* TODO: assuming earth navigated display coordinate system*/
       CoordinateSystem dspCoordSys = getLink().getRenderer().getDisplayCoordinateSystem();
       float[][] fltVals = new float[startPts.length][startPts[0].length];
       for (int i=0; i<fltVals.length; i++) System.arraycopy(startPts[i], 0, fltVals[i], 0, fltVals[i].length);
       startPts = dspCoordSys.toReference(fltVals);
       
       int[] clrIdxs = null;
       if (manifoldDim == 2) {
           clrIdxs = spatialSetTraj.valueToIndex(new float[][] {startPts[0], startPts[1]});
       } else {
           clrIdxs = spatialSetTraj.valueToIndex(startPts);
       }
       int num = clrIdxs.length;
       startClrs[0] = new byte[num];
       startClrs[1] = new byte[num];
       startClrs[2] = new byte[num];
       if (clrDim == 4) startClrs[3] = new byte[num];
       for (int i=0; i<num; i++) {
         int clrIdx = clrIdxs[i];
         startClrs[0][i] = color_values[0][clrIdx];
         startClrs[1][i] = color_values[1][clrIdx];
         startClrs[2][i] = color_values[2][clrIdx];
         if (clrDim == 4) startClrs[3][i] = color_values[3][clrIdx];
       }
     }

     int numTrajectories = startPts[0].length;
     ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

    
        Interpolation uInterp = new Interpolation(trajDoIntrp);
        Interpolation vInterp = new Interpolation(trajDoIntrp);
        Interpolation wInterp = new Interpolation(trajDoIntrp);

        float[][] values0 = null;
        float[][] values1 = null;
        float[][] values2 = null;
        float[][] values3 = null;
        float[][] values0_last = null;

        double[] times = Trajectory.getTimes((Gridded1DSet)anim1DdomainSet);
        double[] timeSteps = Trajectory.getTimeSteps((Gridded1DSet)anim1DdomainSet);
        double timeAccum = 0;
        
        VisADGeometryArray trcrArray = null;

        for (int k=0; k<dataDomainLength-1; k++) {
          int i = (direction < 0) ? ((dataDomainLength-1) - k) : k;

          info = Range.getAdaptedShadowType().getFlowInfo().get(i);
          color_values = info.color_values;
          Gridded3DSet spatial_set = (Gridded3DSet) info.spatial_set;
          manifoldDim = spatial_set.getManifoldDimension();
          lens = spatial_set.getLengths();
          setLocs = spatial_set.getSamples(false);
          if (manifoldDim == 2) {
              spatialSetTraj = new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple, 
                    new float[][] {setLocs[0], setLocs[1]}, lens[0], lens[1]);
          }
          else {
              spatialSetTraj = spatial_set;
          }

          
          float timeStep = (float) timeSteps[i]/numIntrpPts;

          if ((k==0) || (timeAccum >= trajRefreshInterval)) { // for non steady state trajectories (refresh frequency)
             trajectories = new ArrayList<Trajectory>();
             java.util.Arrays.fill(Trajectory.markGrid, false);
             if (direction > 0) {
               switListen.allOffBelow.add(i);
               Trajectory.makeTrajectories(direction*times[i], trajectories, startPts, startClrs);
             }
             else { //TODO: make this work eventually
               //switListen.allOffAbove.add(i);
               //Trajectory.makeTrajectories(direction*times[i], trajectories, trajSkip, color_values, setLocs, lens);
             }
             timeAccum = 0.0;
          }
          timeAccum += timeSteps[i];

          // commented out when not using markGrid logic for starting/ending trajs
          //Trajectory.makeTrajectories(times[i], trajectories, 6, color_values, setLocs, lens);
          /*
          Trajectory.checkTime(i); // for steady-state only
          if ((i % 4) == 0) { // use for steady-state wind field
            Trajectory.makeTrajectories(direction*times[i], trajectories, trajSkip, color_values, setLocs, lens);
          }
          */

          double x0 = (double) direction*i;
          double x1 = (double) direction*(i+direction*1);
          double x2 = (double) direction*(i+direction*2);


          // Even time steps: access fields, update interpolator and compute.
          // Odd time steps: just compute for second half of 3 point (2 gap) interval.
          if ((k % 2) == 0) {
            FlowInfo flwInfo;

            if (values0 == null) {
              flwInfo = flowInfoList.get(i);
              values0 = Trajectory.convertFlowUnit(flwInfo.flow_values, flwInfo.flow_units);
            }

            if (values1 == null) {
              flwInfo = flowInfoList.get(i+direction*1);
              values1 = Trajectory.convertFlowUnit(flwInfo.flow_values, flwInfo.flow_units);
            }

            // make sure we don't access more data than we have, but keep iterating and 
            // computing to the end to use the data we've already pulled in.
            if (k < dataDomainLength-3) {
                flwInfo = flowInfoList.get(i+direction*2);
                values2 = Trajectory.convertFlowUnit(flwInfo.flow_values, flwInfo.flow_units);

                // smoothing done here -----------------------
                flwInfo = flowInfoList.get(i+direction*3);
                values3 = Trajectory.convertFlowUnit(flwInfo.flow_values, flwInfo.flow_units);

                if (values0_last != null) {
                  values0 = Trajectory.smooth(values0_last, values0, values1, smoothParams);
                }
                values1 = Trajectory.smooth(values0, values1, values2, smoothParams);
                values2 = Trajectory.smooth(values1, values2, values3, smoothParams);
                // ------- end smoothing

                // update interpolator 
                uInterp.next(x0, x1, x2, values0[0], values1[0], values2[0]);
                vInterp.next(x0, x1, x2, values0[1], values1[1], values2[1]);
                //wInterp.next(x0, x1, x2, values0[2], values1[2], values2[2]);
            }
            
            if (k == dataDomainLength-3) { // make sure we smoothly handle the last three time steps
                uInterp.next(x0, x1, x2, values0[0], values1[0], values2[0]);
                vInterp.next(x0, x1, x2, values0[1], values1[1], values2[1]);
                //wInterp.next(x0, x1, x2, values0[2], values1[2], values2[2]);
            }
          }
          else {
            values0_last = values1;
            values0 = values2;
            values1 = values3;
            
            if (k == dataDomainLength-3) { // make sure we smootly handle the last three time steps
                uInterp.next(x0, x1, x2, values0[0], values1[0], values2[0]);
                vInterp.next(x0, x1, x2, values0[1], values1[1], values2[1]);
                //wInterp.next(x0, x1, x2, values0[2], values1[2], values2[2]);
            }
          }

          numTrajectories = trajectories.size();
          Trajectory.reset(numTrajectories*numIntrpPts, clrDim);

          for (int ti=0; ti<numIntrpPts; ti++) { // additional points per domain time step
            double dst = (x1 - x0)/numIntrpPts;
            double xt = x0 + dst*ti;
             
            float[] intrpU = new float[uInterp.numSpatialPts];
            float[] intrpV = new float[uInterp.numSpatialPts];
            float[] intrpW = new float[uInterp.numSpatialPts];
            uInterp.interpolate(xt, intrpU);
            vInterp.interpolate(xt, intrpV);
            //wInterp.interpolate(xt, intrpW);
            float[][] flow_values = Trajectory.adjustFlow(info, new float[][] {intrpU, intrpV, intrpW}, timeStep);

            for (int t=0; t<numTrajectories; t++) {
              Trajectory traj = trajectories.get(t);
              traj.currentTimeIndex = direction*i;
              traj.currentTime = direction*times[i];
              traj.forward(flow_values, color_values, spatialSetTraj, direction);
            }

          } // inner time loop (time interpolation)


          VisADLineArray array = Trajectory.makeGeometry();
          trajectories = Trajectory.clean(trajectories, trajLifetime);
          
          // something weird with this, everything being removed ?
          //array = (VisADLineArray) array.removeMissing();

          final BranchGroup branch = (BranchGroup) branches.get(i);
          final BranchGroup node = (BranchGroup) swit.getChild(i);

          trcrArray = Trajectory.makeTracerGeometry(trajectories, direction, trcrSize);
          
          GraphicsModeControl mode = (GraphicsModeControl) info.mode.clone();
          mode.setPointSize(4f, false); //make sure to use false or lest we fall into event loop
          BranchGroup branchB = (BranchGroup) makeBranch();
          addToGroup(branchB, trcrArray, mode, info.constant_alpha, info.constant_color);
          ((BranchGroup)switB.getChild(i)).addChild(branchB);

          addToGroup(branch, array, info.mode, info.constant_alpha, info.constant_color);
          node.addChild(branch);

        } // domain length (time steps) outer time loop
        
        if (switListen.whichVisible.length > 1) { //keep last tracer visible at the end if num visibility nodes > 1
            int idx = dataDomainLength-1;
            final BranchGroup branch = (BranchGroup) branches.get(idx);
            final BranchGroup node = (BranchGroup) swit.getChild(idx);
            FlowInfo finfo = flowInfoList.get(idx);
            GraphicsModeControl mode = (GraphicsModeControl) finfo.mode.clone();
            mode.setPointSize(4f, false); //make sure to use false or lest we fall into event loop
            BranchGroup branchB = (BranchGroup) makeBranch();
            addToGroup(branchB, trcrArray, mode, finfo.constant_alpha, finfo.constant_color);
            ((BranchGroup)switB.getChild(idx)).addChild(branchB);
        }
     }
  }

  class Trajectory {
     float startX;
     float startY;
     float startZ;

     float[] startPts = new float[3];
     float[]  stopPts = new float[3];

     byte[] startColor;
     byte[] stopColor;

     float[][] startPts2D = new float[2][1];
     float[][] startPts3D = new float[3][1];

     static int coordCnt = 0;
     static int colorCnt = 0;
     static int vertCnt = 0;

     int clrDim;

     boolean offGrid = false;

     private static float[] coordinates = null;
     private static byte[] colors = null;
     
     public static boolean[] markGrid;
     public static int[] markGridTime;

     public static int cnt=0;
     public static int[] o_j = new int[] {0, 0, 1, 1}; 
     public static int[] o_i = new int[] {0, 1, 0, 1}; 

     public int initialTimeIndex = 0;
     public int currentTimeIndex = 0;
 
     public double initialTime = 0;
     public double currentTime = 0;

     float[] uVecPath = new float[] {Float.NaN, Float.NaN, Float.NaN};

     public Trajectory(float startX, float startY, float startZ, byte[] startColor) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;

        startPts[0] = startX;
        startPts[1] = startY;
        startPts[2] = startZ;

        clrDim = startColor.length;
        stopColor = new byte[clrDim];

        this.startColor = startColor;
     }

     public static void makeTrajectories(double time, ArrayList<Trajectory> trajectories, float[][] startPts, byte[][] color_values) {
        int num = startPts[0].length;
        int clrDim = color_values.length;

        for (int k=0; k<num; k++) {
           // initialize a new trajectory
           float startX = startPts[0][k];
           float startY = startPts[1][k];
           float startZ = startPts[2][k];

           byte[] startColor = new byte[clrDim];
           startColor[0] = color_values[0][k];
           startColor[1] = color_values[1][k];
           startColor[2] = color_values[2][k];
           if (clrDim == 4) {
              startColor[3] = color_values[3][k];
           }

           Trajectory traj = new Trajectory(startX, startY, startZ, startColor);
           traj.initialTime = time;
           trajectories.add(traj);
        }
     }

     public static void getStartPointsFromDomain(int skip, Gridded3DSet spatial_set, byte[][] color_values, float[][] startPts, byte[][] startClrs) throws VisADException {
        int[] lens = spatial_set.getLengths();
        int lenX = lens[0];
        int lenY = lens[1];

        float[][] setLocs = spatial_set.getSamples(false);

        int clrDim = color_values.length;
        int m = cnt % 4;
        cnt++;

        int jA = 1+o_j[m]*(skip/2);
        int jB = lenY-skip;
        int iA = 1+o_i[m]*(skip/2);
        int iB = lenX-skip;

        int numJ = 1 + ((jB-1)-jA)/skip;
        int numI = 1 + ((iB-1)-iA)/skip;
        int num = numJ*numI;

        startPts[0] = new float[num];
        startPts[1] = new float[num];
        startPts[2] = new float[num];

        startClrs[0] = new byte[num];
        startClrs[1] = new byte[num];
        startClrs[2] = new byte[num];
        if (clrDim == 4) {
           startClrs[3] = new byte[num];
        }

        num = 0;
        for (int j=1+o_j[m]*(skip/2); j<lenY-skip; j+=skip) {
          for (int i=1+o_i[m]*(skip/2); i<lenX-skip; i+=skip) {

            int k = j*lenX + i;

            if (!markGrid[k]) {
              startPts[0][num] = setLocs[0][k];
              startPts[1][num] = setLocs[1][k];
              startPts[2][num] = setLocs[2][k];

              startClrs[0][num] = color_values[0][k];
              startClrs[1][num] = color_values[1][k];
              startClrs[2][num] = color_values[2][k];
              if (clrDim == 4) {
                startClrs[3][num] = color_values[3][k];
              }
            }
          
            num++;
          }
        }

        /*
        for (int k=0; k<markGrid.length; k++) {
           markGrid[k] = false;
        }
        */
     }

     public static void makeTrajectories(double time, ArrayList<Trajectory> trajectories, int skip, byte[][] color_values, float[][] startPts, int[] setLens) {
        int lenX = setLens[0];
        int lenY = setLens[1];

        int clrDim = color_values.length;
        int m = cnt % 4;
        cnt++;

        for (int j=1+o_j[m]*(skip/2); j<lenY-skip; j+=skip) {
          for (int i=1+o_i[m]*(skip/2); i<lenX-skip; i+=skip) {

            int k = j*lenX + i;

            if (!markGrid[k]) {
              // initialize a new trajectory
              float startX = startPts[0][k];
              float startY = startPts[1][k];
              float startZ = startPts[2][k];

              byte[] startColor = new byte[clrDim];
              startColor[0] = color_values[0][k];
              startColor[1] = color_values[1][k];
              startColor[2] = color_values[2][k];
              if (clrDim == 4) {
                startColor[3] = color_values[3][k];
              }

              Trajectory traj = new Trajectory(startX, startY, startZ, startColor);
              traj.initialTime = time;
              trajectories.add(traj);
            }

          }
        }

        /*
        for (int k=0; k<markGrid.length; k++) {
           markGrid[k] = false;
        }
        */
     }

     /* For steady-state trajectories (animated streamlines) only */
     public static void checkTime(int timeIdx) {
       for (int k=0; k<markGridTime.length; k++) {
         if ((timeIdx - markGridTime[k]) > 4) {
           markGridTime[k] = timeIdx;
           markGrid[k] = false;
         }
       }
     }

     /* Remove trajectories from list:
          (1) That have left the grid (marked offGrid).
          (2) That have time length (duration) greater than some threshold.
      */
     public static ArrayList<Trajectory> clean(ArrayList<Trajectory> trajectories, double threshold) {
       ArrayList<Trajectory> newList = new ArrayList<Trajectory>();
       Iterator<Trajectory> iter = trajectories.iterator();
       while (iter.hasNext() ) {
         Trajectory traj = iter.next();
         if (!traj.offGrid && ((traj.currentTime - traj.initialTime) < threshold)) {
           newList.add(traj);
         }
       }
       return newList;
     }

     public static ArrayList<Trajectory> clean(ArrayList<Trajectory> trajectories) {
        return Trajectory.clean(trajectories, -1.0);
     }

     /* Set internal counters to zero. Replace internal arrays and initialize to NaN. */
     public static void reset(int maxNumVerts, int clrDim) {
        coordCnt = 0; 
        colorCnt = 0;
        vertCnt = 0;
        maxNumVerts *= 2; // one each for start and stop
        coordinates = new float[3*maxNumVerts];
        colors = new byte[clrDim*maxNumVerts];
        java.util.Arrays.fill(coordinates, Float.NaN);
     }

     public static VisADLineArray makeGeometry() {
       VisADLineArray array = new VisADLineArray();
       float[] newCoords = new float[coordCnt];
       byte[] newColors = new byte[colorCnt];
       System.arraycopy(coordinates, 0, newCoords, 0, newCoords.length);
       System.arraycopy(colors, 0, newColors, 0, newColors.length);
       array.coordinates = newCoords;
       array.colors = newColors;
       array.vertexCount = vertCnt;

       return array;
     }

     public static VisADGeometryArray makeTracerGeometry(ArrayList<Trajectory> trajectories, int direction, float trcrSize) {
       int numTrajs = trajectories.size();
       VisADGeometryArray array = null;

       // make simple arrow -----------------------
       array = new VisADLineArray();
       int numPts = 4*numTrajs;
       float[] coords =  new float[3*numPts];
       byte[] colors = new byte[3*numPts];

       double barblen = trcrSize*0.034;

       float[] norm = new float[] {0, 0, 1f};
       float[] trj_u = new float[3];

       for (int k=0; k<numTrajs; k++) {
         Trajectory traj = trajectories.get(k);
         trj_u[0] = traj.uVecPath[0];
         trj_u[1] = traj.uVecPath[1];
         trj_u[2] = traj.uVecPath[2];

         float[] endPt = new float[3];
         endPt[0] = traj.startPts[0];
         endPt[1] = traj.startPts[1];
         endPt[2] = traj.startPts[2];

         float[] norm_x_trj = new float[] {
                    norm[1] * trj_u[2] - norm[2] * trj_u[1],
                  -(norm[0] * trj_u[2] - norm[2] * trj_u[0]),
                    norm[0] * trj_u[1] - norm[1] * trj_u[0] };

         float mag = (float) Math.sqrt(norm_x_trj[0] * norm_x_trj[0] +
                                       norm_x_trj[1] * norm_x_trj[1] +
		                       norm_x_trj[2] * norm_x_trj[2]);

	 // - normalize vector
         norm_x_trj[0] /= mag;
         norm_x_trj[1] /= mag;
         norm_x_trj[2] /= mag;
        
         float[] ptOnPath = new float[3];
         
         float len = (float) (barblen*Math.cos(Data.DEGREES_TO_RADIANS*22.0));
         ptOnPath[0] = -len*trj_u[0];
         ptOnPath[1] = -len*trj_u[1];
         ptOnPath[2] = -len*trj_u[2];
         
         ptOnPath[0] += endPt[0];
         ptOnPath[1] += endPt[1];
         ptOnPath[2] += endPt[2];
         
         float[] barbPtA = new float[3];
         float[] barbPtB = new float[3];
         len = (float) (barblen*Math.sin(Data.DEGREES_TO_RADIANS*22.0));
         
         barbPtA[0] = len*norm_x_trj[0];
         barbPtA[1] = len*norm_x_trj[1];
         barbPtA[2] = len*norm_x_trj[2];
         
         barbPtB[0] = -len*norm_x_trj[0];
         barbPtB[1] = -len*norm_x_trj[1];
         barbPtB[2] = -len*norm_x_trj[2];
         
         barbPtA[0] += ptOnPath[0];
         barbPtA[1] += ptOnPath[1]; 
         barbPtA[2] += ptOnPath[2]; 
         
         barbPtB[0] += ptOnPath[0];
         barbPtB[1] += ptOnPath[1];
         barbPtB[2] += ptOnPath[2];
         
         	  
         int t = k*12;
         int c = k*12;
         if (direction > 0) {
           coords[t] = traj.startPts[0];
           coords[t+=1] = traj.startPts[1];
           coords[t+=1] = traj.startPts[2];
         	     
           coords[t+=1] = barbPtA[0];
           coords[t+=1] = barbPtA[1];
           coords[t+=1] = barbPtA[2];
         
           coords[t+=1] = traj.startPts[0];
           coords[t+=1] = traj.startPts[1];
           coords[t+=1] = traj.startPts[2];
         
           coords[t+=1] = barbPtB[0];
           coords[t+=1] = barbPtB[1];
           coords[t+=1] = barbPtB[2];
         }
         else {// TODO: finish this
           coords[k*3] = traj.startX;
           coords[k*3 + 1] = traj.startY;
           coords[k*3 + 2] = traj.startZ;
         }
         
         colors[c] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];

         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
         
         colors[c+=1] = traj.startColor[0];
         colors[c+=1] = traj.startColor[1];
         colors[c+=1] = traj.startColor[2];
       }

       /*  point marker -----------------------------
       array = new VisADPointArray();
       numPts = numTrajs;
       float[] coords =  new float[3*numPts];
       byte[] colors = new byte[3*numPts];
	
       for (int k=0; k<numTrajs; k++) {
         Trajectory traj = trajectories.get(k);
         if (direction > 0) {
           coords[k*3] = traj.startPts[0];
           coords[k*3 + 1] = traj.startPts[1];
           coords[k*3 + 2] = traj.startPts[2];
         }
         else {
           coords[k*3] = traj.startX;
           coords[k*3 + 1] = traj.startY;
           coords[k*3 + 2] = traj.startZ;
         }
         
         colors[k*3] = traj.startColor[0];
         colors[k*3 + 1] = traj.startColor[1];
         colors[k*3 + 2] = traj.startColor[2];
       }
       -------------------------------------------*/

       array.vertexCount = numPts;
       array.coordinates = coords;
       array.colors = colors;

       for (int k=0; k<trajectories.size(); k++) {
         Trajectory traj = trajectories.get(k);
         traj.startX = traj.startPts[0];
         traj.startY = traj.startPts[1];
         traj.startZ = traj.startPts[2];
       }

       return array;
     }
 

     public void forward(float[][] flow_values, byte[][] color_values, GriddedSet spatial_set, int direction)
           throws VisADException {
        if (offGrid) return;

        int[][] indices = new int[1][];
        float[][] weights = new float[1][];
        float[] intrpFlow = new float[3];

        int manifoldDimension = spatial_set.getManifoldDimension();

        if (manifoldDimension == 2) {
          startPts2D[0][0] = startPts[0];
          startPts2D[1][0] = startPts[1];
          spatial_set.valueToInterp(startPts2D, indices, weights);
        }
        else if (manifoldDimension == 3) {
          startPts3D[0][0] = startPts[0];
          startPts3D[1][0] = startPts[1];
          startPts3D[2][0] = startPts[2];
          spatial_set.valueToInterp(startPts3D, indices, weights);
        }


        int clrDim = color_values.length;
        float[] intrpClr = new float[clrDim];

        intrpFlow[0] = 0f;
        intrpFlow[1] = 0f;
        intrpFlow[2] = 0f;

        intrpClr[0] = 0f;
        intrpClr[1] = 0f;
        intrpClr[2] = 0f;
        if (clrDim == 4) {
          intrpClr[3] = 0f;
        }

        if (indices[0] != null) {
           for (int j=0; j<indices[0].length; j++) {
              int idx = indices[0][j];
              intrpFlow[0] += weights[0][j]*(direction)*flow_values[0][idx];
              intrpFlow[1] += weights[0][j]*(direction)*flow_values[1][idx];
              intrpFlow[2] += weights[0][j]*(direction)*flow_values[2][idx];

              intrpClr[0] += weights[0][j]*color_values[0][idx];
              intrpClr[1] += weights[0][j]*color_values[1][idx];
              intrpClr[2] += weights[0][j]*color_values[2][idx];
              if (clrDim == 4) {
                intrpClr[3] += weights[0][j]*color_values[3][idx];
              }
              
              //markGrid[idx] = true;
              markGridTime[idx] = currentTimeIndex;
           }

           stopPts[0] = startPts[0] + intrpFlow[0];
           stopPts[1] = startPts[1] + intrpFlow[1];
           stopPts[2] = startPts[2] + intrpFlow[2];

           stopColor[0] = (byte) intrpClr[0];
           stopColor[1] = (byte) intrpClr[1];
           stopColor[2] = (byte) intrpClr[2];
           if (clrDim == 4) {
             stopColor[3] = (byte) intrpClr[3];
           }

           //addPair(startPts, stopPts, startColor, stopColor); // Just use first color for now.
           addPair(startPts, stopPts, startColor, startColor);

           uVecPath[0] = stopPts[0] - startPts[0];
           uVecPath[1] = stopPts[1] - startPts[1];
           uVecPath[2] = stopPts[2] - startPts[2];

           float mag = (float) Math.sqrt(uVecPath[0]*uVecPath[0] + uVecPath[1]*uVecPath[1] + uVecPath[2]*uVecPath[2]);
           uVecPath[0] /= mag;
           uVecPath[1] /= mag;
           uVecPath[2] /= mag;

           startPts[0] = stopPts[0];
           startPts[1] = stopPts[1];
           startPts[2] = stopPts[2];
          
           /* problem with colors.  Just use first color for now
           startColor[0] = stopColor[0];
           startColor[1] = stopColor[1];
           startColor[2] = stopColor[2];
           if (clrDim == 4) {
             startColor[3] = stopColor[3];
           }
           */
        }
        else {
           intrpFlow[0] = Float.NaN;
           intrpFlow[1] = Float.NaN;
           intrpFlow[2] = Float.NaN;
           offGrid = true;
        }
     }

     private void addPair(float[] startPt, float[] stopPt, byte[] startColor, byte[] stopColor) {
        coordinates[coordCnt++] = startPt[0];
        coordinates[coordCnt++] = startPt[1];
        coordinates[coordCnt++] = startPt[2];
        vertCnt++;

        coordinates[coordCnt++] =  stopPt[0];
        coordinates[coordCnt++] =  stopPt[1];
        coordinates[coordCnt++] =  stopPt[2];
        vertCnt++;

        int clrDim = startColor.length;

        colors[colorCnt++] = startColor[0];
        colors[colorCnt++] = startColor[1];
        colors[colorCnt++] = startColor[2];
        if (clrDim == 4) {
          colors[colorCnt++] = startColor[3];
        }

        colors[colorCnt++] = stopColor[0];
        colors[colorCnt++] = stopColor[1];
        colors[colorCnt++] = stopColor[2];
        if (clrDim == 4) {
          colors[colorCnt++] = stopColor[3];
        }
     }

     public static float[][] adjustFlow(FlowInfo info, float[][] flow_values, float timeStep) throws VisADException {
        return ShadowType.adjustFlowToEarth(info.which, flow_values, info.spatial_values, info.flowScale,
                               info.renderer, false, true, timeStep);
     }

     public static float[][] smooth(float[][] values0, float[][] values1, float[][] values2, TrajectoryParams.SmoothParams smoothParams) {

       float w0 = smoothParams.w0;
       float w1 = smoothParams.w1;
       float w2 = smoothParams.w2;

       int numPts = values0[0].length;
       float[][] new_values = new float[3][numPts];

       for (int k=0; k<numPts; k++) {
         new_values[0][k] = w0*values0[0][k] + w1*values1[0][k] + w2*values2[0][k];
         new_values[1][k] = w0*values0[1][k] + w1*values1[1][k] + w2*values2[1][k];
         new_values[2][k] = w0*values0[2][k] + w1*values1[2][k] + w2*values2[2][k];
       }

       return new_values;
     }

     public static double[] getTimeSteps(Gridded1DSet timeSet) throws VisADException {
        double[] timePts;
        if (timeSet instanceof Gridded1DDoubleSet) {
           timePts = (timeSet.getDoubles())[0];
        }
        else {
           timePts = (Set.floatToDouble(timeSet.getSamples()))[0];
        }

        double[] timeSteps = new double[timePts.length];
        Unit[] setUnits = timeSet.getSetUnits();
        timePts = CommonUnit.secondsSinceTheEpoch.toThis(timePts, setUnits[0]);
        for (int t=0; t<timePts.length-1; t++) { 
           timeSteps[t] = timePts[t+1]-timePts[t];
        }
        timeSteps[timePts.length-1] = timeSteps[timePts.length-2];
        return timeSteps;
     }

     public static double[] getTimes(Gridded1DSet timeSet) throws VisADException {
        double[] timePts;
        if (timeSet instanceof Gridded1DDoubleSet) {
           timePts = (timeSet.getDoubles())[0];
        }
        else {
           timePts = (Set.floatToDouble(timeSet.getSamples()))[0];
        }

        Unit[] setUnits = timeSet.getSetUnits();
        timePts = CommonUnit.secondsSinceTheEpoch.toThis(timePts, setUnits[0]);
        return timePts;
     }


     public static float[][] convertFlowUnit(float[][] values, Unit[] units) throws VisADException {

       float[] valsX = values[0];
       if (!CommonUnit.meterPerSecond.equals(units[0])) {
         valsX = CommonUnit.meterPerSecond.toThis(values[0], units[0]);
       }

       float[] valsY = values[1];
       if (!CommonUnit.meterPerSecond.equals(units[1])) {
         valsY = CommonUnit.meterPerSecond.toThis(values[1], units[1]);
       }

      /* 
       * FlowZ will have to be meters/second: Application will have to convert
       * or supply, maybe through a RangeCoordinateSystem, a transform to do this.
       *
       */

       return new float[][] {valsX, valsY, values[2]};
     }
  }

  class SwitchListener extends Switch {
    int numChildren;
    int[] whichVisible;
    Switch swit;
    java.util.BitSet bits;

    ArrayList<Integer> allOffBelow = new ArrayList<Integer>();

    ArrayList<Integer> allOffAbove = new ArrayList<Integer>();

    SwitchListener(Switch swit, int numChildren, int[] whichVisible) {
      super();
      this.numChildren = numChildren;
      this.bits = new java.util.BitSet(numChildren);
      this.swit = swit;
      this.whichVisible = whichVisible;
    }

    public int numChildren() {
      return numChildren;
    }

    public void setWhichChild(int index) {
      if (index == Switch.CHILD_NONE) {
        bits.clear();
        swit.setWhichChild(Switch.CHILD_NONE);
      }
      else if (index >= 0) {
        bits.clear();
        for (int t=0; t<whichVisible.length; t++) {
          int k_set = index + whichVisible[t];
          if (k_set >= 0) {
            bits.set(k_set);
          }
        }

        int offBelow = 0;
        for (int k=0; k<allOffBelow.size(); k++) {
          int idx = allOffBelow.get(k).intValue();
          if (index >= idx) {
            offBelow = idx;
          }
        }
        if (offBelow > 0) {
          bits.clear(0, offBelow);
        }

        /* TODO: not working
        bits.set(0, numChildren-1);
        int offAbove = 0;
        for (int k=0; k<allOffAbove.size(); k++) {
          int idx = allOffAbove.get(k).intValue();
          if (index <= idx) {
            offAbove = idx;
          }
        }
        if (offAbove > 0) {
          bits.clear(offAbove, numChildren-1);
        }
        */

        swit.setChildMask(bits);
      }
    }

    public void setChildMask(java.util.BitSet bits) {
      swit.setChildMask(bits);
    }
  }
