//
// ShadowFunctionOrSetType.java
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

/*
MEM - memory use reduction strategy:

changes marked by MEM_WLH

1. if (isTexture) no assembleSpatial  NOT NEEDED

  but if (isTexture) then domain_values = null,
  so spatial display_values[i] = null so assembleSpatial
  does nothing (except construct a SingletonSet spatial_set)

2. byte[][] assembleColor  DONE

  this has a bad space / time tradeoff in makeIso*,
  so create a boolean in GMC to choose between
  (Irregular3DSet, Gridded3DSet) and their slower extensions

3. after use, set  display_values[i] = null;  DONE

  done in many ShadowType.assemble*  - marked by MEM_WLH

4. don't fill arrays that won't get used

  are there any of these ??

5. replace getValues() by getFloats(false)
  already done for getSamples

6. assembleColors first  DONE

7. boolean[] range_select

8. in-line byteToFloat and floatToByte  DONE
  VisADCanvasJ2D, Gridded3DSet, Irregular3DSet

N. iso-surface computation uses:

  Irregular3DSet.makeIsosurface:

float[len] fieldValues
float[3 or 4][len] auxValues (color_values)
float[3][nvertex] fieldVertices
float[3 or 4][nvertex] auxLevels
int[1][npolygons][3 or 4] polyToVert
int[1][nvertex][nverts[i]] vertToPoly

int[Delan.Tri.length][4] polys
int[Delan.NumEdges] globalToVertex
float[DomainDimension][Delan.NumEdges] edgeInterp
float[3 or 4][Delan.NumEdges] auxInterp




  Gridded3DSet.makeIsoSurface:

start with nvertex_estimate = 4 * npolygons + 100

int[num_cubes] ptFLAG
int[xdim_x_ydim_x_zdim] ptAUX
int[num_cubes+1] pcube
float[1][4 * npolygons] VX
float[1][4 * npolygons] VY
float[1][4 * npolygons] VZ
float[3 or 4][nvet] color_temps
float[3 or 4][len] cfloat
int[7 * npolygons] Vert_f_Pol
int[1][36 * npolygons] Pol_f_Vert

  number of bytes = 268 (or 284) * npolygons +
                    24 (or 28) * len

isosurf:

float[3][len] samples
float[3 or 4][nvertex_estimate] tempaux

  number of bytes = 48 (or 64) * npolygons +
                    12 * len


float[npolygons] NxA, NxB, NyA, NyB, NzA, NzB
float[npolygons] Pnx, Pny, Pnz
float[nvertex] NX, NY, NZ

  number of bytes = 84 * npolygons

make_normals:

none


  total number of bytes = 36 (or 40) * len +
                          400 (to 432) * npolygons

so if len = 0.5M and npolygons = 0.1M
bytes = 20M + 43.2M = 63.2M

*/


package visad;

import java.awt.color.*;
import java.awt.image.*;
import java.rmi.*;
import java.text.*;
import java.util.*;

/**
   The ShadowFunctionOrSetType class is an abstract parent for
   classes that implement ShadowFunctionType or ShadowSetType.<P>
*/
public abstract class ShadowFunctionOrSetType extends ShadowType {

  ShadowRealTupleType Domain;
  ShadowType Range; // null for ShadowSetType

  /** RangeComponents is an array of ShadowRealType-s that are
      ShadowRealType components of Range or ShadowRealType
      components of ShadowRealTupleType components of Range;
      a non-ShadowRealType and non-ShadowTupleType Range is marked
      by null;
      components of a ShadowTupleType Range that are neither
      ShadowRealType nor ShadowRealTupleType are ignored */
  ShadowRealType[] RangeComponents; // null for ShadowSetType
  ShadowRealType[] DomainComponents;
  ShadowRealType[] DomainReferenceComponents;

  /** true if range is ShadowRealType or Flat ShadowTupleType
      not the same as FunctionType.Flat;
      also true for ShadowSetType */
  boolean Flat;

  /** value_indices from parent */
  int[] inherited_values;

  /** this constructor is a bit of a kludge to get around
      single inheritance problems */
  public ShadowFunctionOrSetType(MathType t, DataDisplayLink link, ShadowType parent,
                                 ShadowRealTupleType domain, ShadowType range)
      throws VisADException, RemoteException {
    super(t, link, parent);
    Domain = domain;
    Range = range;
    if (this instanceof ShadowFunctionType) {
      Flat = (Range instanceof ShadowRealType) ||
             (Range instanceof ShadowTextType) ||
             (Range instanceof ShadowTupleType &&
              ((ShadowTupleType) Range).isFlat());
      MultipleSpatialDisplayScalar = Domain.getMultipleSpatialDisplayScalar() ||
                                     Range.getMultipleSpatialDisplayScalar();
      MultipleDisplayScalar = Domain.getMultipleDisplayScalar() ||
                              Range.getMultipleDisplayScalar();
      MappedDisplayScalar = Domain.getMappedDisplayScalar() ||
                              Range.getMappedDisplayScalar();
      RangeComponents = getComponents(Range, true);
    }
    else if (this instanceof ShadowSetType) {
      Flat = true;
      MultipleDisplayScalar = Domain.getMultipleDisplayScalar();
      MappedDisplayScalar = Domain.getMappedDisplayScalar();
      RangeComponents = null;
    }
    else {
      throw new DisplayException("ShadowFunctionOrSetType: must be " +
                                 "ShadowFunctionType or ShadowSetType");
    }
    DomainComponents = getComponents(Domain, false);
    DomainReferenceComponents = getComponents(Domain.getReference(), false);
  }

  public boolean getFlat() {
    return Flat;
  }

  public ShadowRealType[] getRangeComponents() {
    return RangeComponents;
  }

  public ShadowRealType[] getDomainComponents() {
    return DomainComponents;
  }

  public ShadowRealType[] getDomainReferenceComponents() {
    return DomainReferenceComponents;
  }

  /** used by FlatField.computeRanges */
  int[] getRangeDisplayIndices() throws VisADException {
    if (!(this instanceof ShadowFunctionType)) {
      throw new DisplayException("ShadowFunctionOrSetType.getRangeDisplay" +
                                 "Indices: must be ShadowFunctionType");
    }
    int n = RangeComponents.length;
    int[] indices = new int[n];
    for (int i=0; i<n; i++) {
      indices[i] = RangeComponents[i].getIndex();
    }
    return indices;
  }

  public int[] getInheritedValues() {
    return inherited_values;
  }

  /** checkIndices: check for rendering difficulty, etc */
  public int checkIndices(int[] indices, int[] display_indices,
             int[] value_indices, boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from Domain
    int[] local_indices = Domain.sumIndices(indices);
    int[] local_display_indices = Domain.sumDisplayIndices(display_indices);
    int[] local_value_indices = Domain.sumValueIndices(value_indices);

    if (Domain.testTransform()) Domain.markTransform(isTransform);
    if (this instanceof ShadowFunctionType) {
      Range.markTransform(isTransform);
    }

    // get value_indices arrays used by doTransform
    inherited_values = copyIndices(value_indices);
    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(local_display_indices)) {
        levelOfDifficulty = NESTED;
      }
    }

    // test legality of Animation and SelectValue in Domain
    int avCount = checkAnimationOrValue(Domain.getDisplayIndices());
    if (Domain.getDimension() != 1) {
      if (avCount > 0) {
        throw new BadMappingException("Animation and SelectValue may only occur " +
                                      "in 1-D Function domain: " +
                                      "ShadowFunctionOrSetType.checkIndices");
      }
      else {
        // eventually ShadowType.testTransform is used to mark Animation,
        // Value or Range as isTransform when multiple occur in Domain;
        // however, temporary hack in Renderer.isTransformControl requires
        // multiple occurence of Animation and Value to throw an Exception
        if (avCount > 1) {
          throw new BadMappingException("only one Animation and SelectValue may " +
                                        "occur Set domain: " +
                                        "ShadowFunctionOrSetType.checkIndices");
        }
      }
    }

    if (Flat || this instanceof ShadowSetType) {
      if (this instanceof ShadowFunctionType) {
        if (Range instanceof ShadowTupleType) {
          local_indices =
            ((ShadowTupleType) Range).sumIndices(local_indices);
          local_display_indices =
            ((ShadowTupleType) Range).sumDisplayIndices(local_display_indices);
          local_value_indices =
            ((ShadowTupleType) Range).sumValueIndices(local_value_indices);
        }
        else if (Range instanceof ShadowScalarType) {
          ((ShadowScalarType) Range).incrementIndices(local_indices);
          local_display_indices = addIndices(local_display_indices,
            ((ShadowScalarType) Range).getDisplayIndices());
          local_value_indices = addIndices(local_value_indices,
            ((ShadowScalarType) Range).getValueIndices());
        }

        // test legality of Animation and SelectValue in Range
        if (checkAnimationOrValue(Range.getDisplayIndices()) > 0) {
          throw new BadMappingException("Animation and SelectValue may not " +
                                        "occur in Function range: " +
                                        "ShadowFunctionOrSetType.checkIndices");
        }
      } // end if (this instanceof ShadowFunctionType)
      anyContour = checkContour(local_display_indices);
      anyFlow = checkFlow(local_display_indices);
      anyShape = checkShape(local_display_indices);
      anyText = checkText(local_display_indices);

      LevelOfDifficulty =
        testIndices(local_indices, local_display_indices, levelOfDifficulty);
/*
System.out.println("ShadowFunctionOrSetType.checkIndices 1:" +
                   " LevelOfDifficulty = " + LevelOfDifficulty +
                   " isTerminal = " + isTerminal +
                   " Type = " + Type.prettyString());
*/
      // compute Domain type
      if (!Domain.getMappedDisplayScalar()) {
        Dtype = D0;
      }
      else if (Domain.getAllSpatial() && checkR4(Domain.getDisplayIndices())) {
        Dtype = D1;
      }
      else if (checkR1D3(Domain.getDisplayIndices())) {
        Dtype = D3;
      }
      else if (checkR2D2(Domain.getDisplayIndices())) {
        Dtype = D2;
      }
      else if (checkAnimationOrValue(Domain.getDisplayIndices()) > 0) {
        Dtype = D4;
      }
      else {
        Dtype = Dbad;
      }

      if (this instanceof ShadowFunctionType) {
        // compute Range type
        if (!Range.getMappedDisplayScalar()) {
          Rtype = R0;
        }
        else if (checkR1D3(Range.getDisplayIndices())) {
          Rtype = R1;
        }
        else if (checkR2D2(Range.getDisplayIndices())) {
          Rtype = R2;
        }
        else if (checkR3(Range.getDisplayIndices())) {
          Rtype = R3;
        }
        else if (checkR4(Range.getDisplayIndices())) {
          Rtype = R4;
        }
        else {
          Rtype = Rbad;
        }
      }
      else { // this instanceof ShadowSetType
        Rtype = R0; // implicit - Set has no range
      }

      if (LevelOfDifficulty == NESTED) {
        if (Dtype != Dbad && Rtype != Rbad) {
          if (Dtype == D4) {
            LevelOfDifficulty = SIMPLE_ANIMATE_FIELD;
          }
          else {
            LevelOfDifficulty = SIMPLE_FIELD;
          }
        }
        else {
          LevelOfDifficulty = LEGAL;
        }
      }
/*
System.out.println("ShadowFunctionOrSetType.checkIndices 2:" +
                   " LevelOfDifficulty = " + LevelOfDifficulty +
                   " Dtype = " + Dtype + " Rtype = " + Rtype);
*/
      adjustProjectionSeam = checkAdjustProjectionSeam();

      if (this instanceof ShadowFunctionType) {
        float[] default_values = getLink().getDefaultValues();
        boolean textureEnabled = 
          default_values[display.getDisplayScalarIndex(Display.TextureEnable)] > 0.5f;

        // test for texture mapping
        // WLH 30 April 99
        isTextureMap = !getMultipleDisplayScalar() &&
                       getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD &&
                       ((FunctionType) getType()).getReal() &&   // ??
                       Domain.getDimension() == 2 &&
                       Domain.getAllSpatial() &&
                       !Domain.getSpatialReference() &&
                       Display.DisplaySpatialCartesianTuple.equals(
                               Domain.getDisplaySpatialTuple() ) &&
                       checkColorAlphaRange(Range.getDisplayIndices()) &&
                       checkAny(Range.getDisplayIndices()) &&
                       textureEnabled &&
                       !display.getGraphicsModeControl().getPointMode();

        curvedTexture = getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD &&
                        Domain.getAllSpatial() &&
                        checkSpatialOffsetColorAlphaRange(Domain.getDisplayIndices()) &&
                        checkSpatialOffsetColorAlphaRange(Range.getDisplayIndices()) &&
                        checkAny(Range.getDisplayIndices()) &&
                        !display.getGraphicsModeControl().getPointMode();

        // WLH 15 March 2000
        // isTexture3D = !getMultipleDisplayScalar() &&
        isTexture3D =  getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD &&
                       ((FunctionType) getType()).getReal() &&   // ??
                       Domain.getDimension() == 3 &&
                       Domain.getAllSpatial() &&
                       // WLH 1 April 2000
                       // !Domain.getMultipleDisplayScalar() && // WLH 15 March 2000
                       checkSpatialRange(Domain.getDisplayIndices()) && // WLH 1 April 2000
                       !Domain.getSpatialReference() &&
                       Display.DisplaySpatialCartesianTuple.equals(
                               Domain.getDisplaySpatialTuple() ) &&
                       checkColorAlphaRange(Range.getDisplayIndices()) &&
                       checkAny(Range.getDisplayIndices()) &&
                       textureEnabled &&
                       !display.getGraphicsModeControl().getPointMode();

        // note GgraphicsModeControl.setTextureEnable(false) disables this
        isLinearContour3D =
                       getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD &&
                       ((FunctionType) getType()).getReal() &&   // ??
                       Domain.getDimension() == 3 &&
                       Domain.getAllSpatial() &&
                       !Domain.getMultipleDisplayScalar() &&
                       !Domain.getSpatialReference() &&
                       Display.DisplaySpatialCartesianTuple.equals(
                               Domain.getDisplaySpatialTuple() ) &&
                       checkContourColorAlphaRange(Range.getDisplayIndices()) &&
                       checkContour(Range.getDisplayIndices());

/*
System.out.println("checkIndices.isTexture3D = " + isTexture3D + " " +
                   (getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD) + " " +
                   ((FunctionType) getType()).getReal() + " " +
                   (Domain.getDimension() == 3) + " " +
                   Domain.getAllSpatial() + " " +
                   checkSpatialRange(Domain.getDisplayIndices()) + " " +
                   !Domain.getSpatialReference() + " " +
                   Display.DisplaySpatialCartesianTuple.equals(
                        Domain.getDisplaySpatialTuple() ) + " " +
                   checkColorAlphaRange(Range.getDisplayIndices()) + " " +
                   checkAny(Range.getDisplayIndices()) + " " +
                   textureEnabled &&
                   !display.getGraphicsModeControl().getPointMode() );
*/

/*
System.out.println("checkIndices.isTextureMap = " + isTextureMap + " " +
                   !getMultipleDisplayScalar() + " " +
                   (getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD) + " " +
                   ((FunctionType) getType()).getReal() + " " +
                   (Domain.getDimension() == 2) + " " +
                   Domain.getAllSpatial() + " " +
                   !Domain.getSpatialReference() + " " +
                   Display.DisplaySpatialCartesianTuple.equals(
                               Domain.getDisplaySpatialTuple() ) + " " +
                   checkColorAlphaRange(Range.getDisplayIndices()) + " " +
                   checkAny(Range.getDisplayIndices()) + " " +
                   textureEnabled &&
                   !display.getGraphicsModeControl().getPointMode() );

System.out.println("checkIndices.curvedTexture = " + curvedTexture + " " +
                    (getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD) + " " +
                    (Domain.getDimension() == 2) + " " +
                    Domain.getAllSpatial() + " " +
                    checkSpatialOffsetColorAlphaRange(Domain.getDisplayIndices()) + " " +
                    checkSpatialOffsetColorAlphaRange(Range.getDisplayIndices()) + " " +
                    checkAny(Range.getDisplayIndices()) + " " +
                    textureEnabled &&
                    !display.getGraphicsModeControl().getPointMode() );
*/
      }
    }
    else { // !Flat && this instanceof ShadowFunctionType
      if (levelOfDifficulty == NESTED) {
        if (!checkNested(Domain.getDisplayIndices())) {
          levelOfDifficulty = LEGAL;
        }
      }
      LevelOfDifficulty = Range.checkIndices(local_indices, local_display_indices,
                                             local_value_indices, isTransform,
                                             levelOfDifficulty);
/*
System.out.println("ShadowFunctionOrSetType.checkIndices 3:" +
                   " LevelOfDifficulty = " + LevelOfDifficulty);
*/
    }

    return LevelOfDifficulty;
  }

  public ShadowRealTupleType getDomain() {
    return Domain;
  }

  public ShadowType getRange() {
    return Range;
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    if (Range != null) Range.markTransform(isTransform);
  }

  /** transform data into a (Java3D or Java2D) scene graph;
      add generated scene graph components as children of group;
      group is Group (Java3D) or VisADGroup (Java2D);
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process */
  public boolean doTransform(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer,
                             ShadowType shadow_api)
         throws VisADException, RemoteException {

    // return if data is missing or no ScalarMaps
    if (data.isMissing()) return false;
    if (LevelOfDifficulty == NOTHING_MAPPED) return false;

    // if transform has taken more than 500 milliseconds and there is
    // a flag requesting re-transform, throw a DisplayInterruptException
    DataDisplayLink link = renderer.getLink();

// if (link != null) System.out.println("\nstart doTransform " + (System.currentTimeMillis() - link.start_time));

    if (link != null) {
      boolean time_flag = false;
      if (link.time_flag) {
        time_flag = true;
      }
      else {
        if (500 < System.currentTimeMillis() - link.start_time) {
          link.time_flag = true;
          time_flag = true;
        }
      }
      if (time_flag) {
        if (link.peekTicks()) {
          throw new DisplayInterruptException("please wait . . .");
        }
        Enumeration maps = link.getSelectedMapVector().elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          if (map.peekTicks(renderer, link)) {
            throw new DisplayInterruptException("please wait . . .");
          }
        }
      }
    } // end if (link != null)

    // get 'shape' flags
    boolean anyContour = getAnyContour();
    boolean anyFlow = getAnyFlow();
    boolean anyShape = getAnyShape();
    boolean anyText = getAnyText();

    boolean indexed = shadow_api.wantIndexed();

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

    // get values inherited from parent;
    // assume these do not include SelectRange, SelectValue
    // or Animation values - see temporary hack in
    // DataRenderer.isTransformControl
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }

    // check for only contours and only disabled contours
    if (getIsTerminal() && anyContour &&
        !anyFlow && !anyShape && !anyText) {   // WLH 13 March 99
      boolean any_enabled = false;
      for (int i=0; i<valueArrayLength; i++) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.IsoContour) && inherited_values[i] == 0) {
          // non-inherited IsoContour, so generate contours
          ContourControl control = (ContourControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          boolean[] bvalues = new boolean[2];
          float[] fvalues = new float[5];
          control.getMainContours(bvalues, fvalues);
          if (bvalues[0]) any_enabled = true;
        }
      }
      if (!any_enabled) return false;
    }

    Set domain_set = null;
    Unit[] dataUnits = null;
    CoordinateSystem dataCoordinateSystem = null;
    if (this instanceof ShadowFunctionType) {
      // currently only implemented for Field
      // must eventually extend to Function
      if (!(data instanceof Field)) {
        throw new UnimplementedException("data must be Field: " +
                                         "ShadowFunctionOrSetType.doTransform: ");
      }
      domain_set = ((Field) data).getDomainSet();
      dataUnits = ((Function) data).getDomainUnits();
      dataCoordinateSystem = ((Function) data).getDomainCoordinateSystem();
    }
    else if (this instanceof ShadowSetType) {
      domain_set = (Set) data;
      dataUnits = ((Set) data).getSetUnits();
      dataCoordinateSystem = ((Set) data).getCoordinateSystem();
    }
    else {
      throw new DisplayException(
          "must be ShadowFunctionType or ShadowSetType: " +
          "ShadowFunctionOrSetType.doTransform");
    }

    float[][] domain_values = null;
    double[][] domain_doubles = null;
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length;
    int domain_dimension;
    try {
      domain_length = domain_set.getLength();
      domain_dimension = domain_set.getDimension();
    }
    catch (SetException e) {
      return false;
    }

    // ShadowRealTypes of Domain
    ShadowRealType[] DomainComponents = getDomainComponents();

    int alpha_index = display.getDisplayScalarIndex(Display.Alpha);

    // array to hold values for Text mapping (can only be one)
    String[] text_values = null;
    // get any text String and TextControl inherited from parent
    TextControl text_control = shadow_api.getParentTextControl();
    String inherited_text = shadow_api.getParentText();
    if (inherited_text != null) {
      text_values = new String[domain_length];
      for (int i=0; i<domain_length; i++) {
        text_values[i] = inherited_text;
      }
    }

    boolean isTextureMap = getIsTextureMap() &&
                           // default_values[alpha_index] > 0.99 &&
                           renderer.isLegalTextureMap() &&
                           (domain_set instanceof Linear2DSet ||
                            (domain_set instanceof LinearNDSet &&
                             domain_set.getDimension() == 2));

    // DRM 2003-08-21
    //int curved_size = display.getGraphicsModeControl().getCurvedSize();
    int curved_size = (int)
      default_values[display.getDisplayScalarIndex(Display.CurvedSize)];
    /* DRM 2005-08-29 - default value now correct in link.prepareData()
    int curved_size =  
         (cMapCurveSize > 0)
             ? cMapCurveSize
             : display.getGraphicsModeControl().getCurvedSize();
    */

    float textureEnable =
      default_values[display.getDisplayScalarIndex(Display.TextureEnable)];
    boolean texture = textureEnable > 0.5f;
    /* DRM 2005-08-29 - default value now correct in link.prepareData()
    boolean texture = display.getGraphicsModeControl().getTextureEnable();
    if (textureEnable > -0.5f) {
      texture = (textureEnable > 0.5f);
    }
    */

    boolean curvedTexture = getCurvedTexture() &&
                            !isTextureMap &&
                            texture &&
                            curved_size > 0 &&
                            getIsTerminal() && // implied by getCurvedTexture()?
                            shadow_api.allowCurvedTexture() &&
                            //default_values[alpha_index] > 0.99 &&
                            renderer.isLegalTextureMap() &&
                            domain_set.getManifoldDimension() == 2 &&
                            domain_set instanceof GriddedSet;
    boolean domainOnlySpatial =
      Domain.getAllSpatial() && !Domain.getMultipleDisplayScalar();

    boolean isTexture3D = getIsTexture3D() &&
                           // default_values[alpha_index] > 0.99 &&
                           renderer.isLegalTextureMap() &&
                           (domain_set instanceof Linear3DSet ||
                            (domain_set instanceof LinearNDSet &&
                             domain_set.getDimension() == 3));
    int t3dm = (int)
      default_values[display.getDisplayScalarIndex(Display.Texture3DMode)];


    // WLH 1 April 2000
    boolean range3D = isTexture3D && anyRange(Domain.getDisplayIndices());

    boolean isLinearContour3D = getIsLinearContour3D() &&
                                domain_set instanceof Linear3DSet &&
                                shadow_api.allowLinearContour();

/*
System.out.println("doTransform.isTextureMap = " + isTextureMap + " " +
                   getIsTextureMap() + " " +
                   // (default_values[alpha_index] > 0.99) + " " +
                   renderer.isLegalTextureMap() + " " +
                   (domain_set instanceof Linear2DSet) + " " +
                   (domain_set instanceof LinearNDSet) + " " +
                   (domain_set.getDimension() == 2));

System.out.println("doTransform.curvedTexture = " + curvedTexture + " " +
                        getCurvedTexture() + " " +
                        !isTextureMap + " " +
                        (curved_size > 0) + " " +
                        getIsTerminal() + " " +
                        shadow_api.allowCurvedTexture() + " " +
                        (default_values[alpha_index] > 0.99) + " " +
                        renderer.isLegalTextureMap() + " " +
                        (domain_set instanceof Gridded2DSet) + " " +
                        (domain_set instanceof GriddedSet) + " " +
                        (domain_set.getDimension() == 2) );
*/

    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    byte[] colors = null;
    int data_width = 0;
    int data_height = 0;
    int data_depth = 0;
    int texture_width = 1;
    int texture_height = 1;
    int texture_depth = 1;
    float[] coordinatesX = null;
    float[] texCoordsX = null;
    float[] normalsX = null;
    byte[] colorsX = null;
    float[] coordinatesY = null;
    float[] texCoordsY = null;
    float[] normalsY = null;
    byte[] colorsY = null;
    float[] coordinatesZ = null;
    float[] texCoordsZ = null;
    float[] normalsZ = null;
    byte[] colorsZ = null;

    int[] volume_tuple_index = null;

// if (link != null) System.out.println("test isTextureMap " + (System.currentTimeMillis() - link.start_time));

    if (isTextureMap) {
      /***
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
      texture_width = shadow_api.textureWidth(data_width);
      texture_height = shadow_api.textureHeight(data_height);

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
      for (int i=0; i<DomainComponents.length; i++) {
        Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
        while (maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          DisplayRealType real = map.getDisplayScalar();
          DisplayTupleType tuple = real.getTuple();
          if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
            // scale values
            limits[i] = map.scaleValues(limits[i]);
            // get spatial index
            tuple_index[i] = real.getTupleIndex();
            break;
          }
        }
//      if (tuple == null ||
//          !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
//        throw new DisplayException("texture with bad tuple: " +
//                                   "ShadowFunctionOrSetType.doTransform");
//      }
//      if (maps.hasMoreElements()) {
//        throw new DisplayException("texture with multiple spatial: " +
//                                   "ShadowFunctionOrSetType.doTransform");
//      }
      } // end for (int i=0; i<DomainComponents.length; i++)
      // get spatial index not mapped from domain_set
      tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
      DisplayRealType real = (DisplayRealType)
        Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
      int value2_index = display.getDisplayScalarIndex(real);
      float value2 = default_values[value2_index];
      // float value2 = 0.0f;  WLH 30 Aug 99
      for (int i=0; i<valueArrayLength; i++) {
        if (inherited_values[i] > 0 &&
            real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
          value2 = value_array[i];
          break;
        }
      }

      coordinates = new float[12];
      // corner 0
      coordinates[tuple_index[0]] = limits[0][0];
      coordinates[tuple_index[1]] = limits[1][0];
      coordinates[tuple_index[2]] = value2;
      // corner 1
      coordinates[3 + tuple_index[0]] = limits[0][1];
      coordinates[3 + tuple_index[1]] = limits[1][0];
      coordinates[3 + tuple_index[2]] = value2;
      // corner 2
      coordinates[6 + tuple_index[0]] = limits[0][1];
      coordinates[6 + tuple_index[1]] = limits[1][1];
      coordinates[6 + tuple_index[2]] = value2;
      // corner 3
      coordinates[9 + tuple_index[0]] = limits[0][0];
      coordinates[9 + tuple_index[1]] = limits[1][1];
      coordinates[9 + tuple_index[2]] = value2;

      // move image back in Java3D 2-D mode
      shadow_api.adjustZ(coordinates);

      texCoords = new float[8];
      float ratiow = ((float) data_width) / ((float) texture_width);
      float ratioh = ((float) data_height) / ((float) texture_height);
      shadow_api.setTexCoords(texCoords, ratiow, ratioh);

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
//for (int i=0; i < 4; i++) {
//  System.out.println("i = " + i + " texCoords = " + texCoords[2 * i] + " " +
//                     texCoords[2 * i + 1]);
//  System.out.println(" coordinates = " + coordinates[3 * i] + " " +
//                     coordinates[3 * i + 1] + " " + coordinates[3 * i + 2]);
//  System.out.println(" normals = " + normals[3 * i]  + " " + normals[3 * i + 1] +
//                     " " + normals[3 * i + 2]);
//}
    ***/
    }
    else if (isTexture3D) {
      Linear1DSet X = null;
      Linear1DSet Y = null;
      Linear1DSet Z = null;
      if (domain_set instanceof Linear3DSet) {
        X = ((Linear3DSet) domain_set).getX();
        Y = ((Linear3DSet) domain_set).getY();
        Z = ((Linear3DSet) domain_set).getZ();
      }
      else {
        X = ((LinearNDSet) domain_set).getLinear1DComponent(0);
        Y = ((LinearNDSet) domain_set).getLinear1DComponent(1);
        Z = ((LinearNDSet) domain_set).getLinear1DComponent(2);
      }
      float[][] limits = new float[3][2];
      limits[0][0] = (float) X.getFirst();
      limits[0][1] = (float) X.getLast();
      limits[1][0] = (float) Y.getFirst();
      limits[1][1] = (float) Y.getLast();
      limits[2][0] = (float) Z.getFirst();
      limits[2][1] = (float) Z.getLast();

      // convert values to default units (used in display)
      limits = Unit.convertTuple(limits, dataUnits, domain_units);

      // get domain_set sizes
      data_width = X.getLength();
      data_height = Y.getLength();
      data_depth = Z.getLength();
      texture_width = shadow_api.textureWidth(data_width);
      texture_height = shadow_api.textureHeight(data_height);
      texture_depth = shadow_api.textureDepth(data_depth);

      int[] tuple_index = new int[3];
      if (DomainComponents.length != 3) {
        throw new DisplayException("texture3D domain dimension != 3:" +
                                   "ShadowFunctionOrSetType.doTransform");
      }
      for (int i=0; i<DomainComponents.length; i++) {
        Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();

        while (maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          DisplayRealType real = map.getDisplayScalar();
          DisplayTupleType tuple = real.getTuple();
          if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
            // scale values
            limits[i] = map.scaleValues(limits[i]);
            // get spatial index
            tuple_index[i] = real.getTupleIndex();
            break;
          }
        }
/*
        if (tuple == null ||
            !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
          throw new DisplayException("texture with bad tuple: " +
                                     "ShadowFunctionOrSetType.doTransform");
        }
        if (maps.hasMoreElements()) {
          throw new DisplayException("texture with multiple spatial: " +
                                     "ShadowFunctionOrSetType.doTransform");
        }
*/
      } // end for (int i=0; i<DomainComponents.length; i++)
      volume_tuple_index = tuple_index;

      coordinatesX = new float[12 * data_width];
      coordinatesY = new float[12 * data_height];
      coordinatesZ = new float[12 * data_depth];
      for (int i=0; i<data_depth; i++) {
        int i12 = i * 12;
        float depth = limits[2][0] +
          (limits[2][1] - limits[2][0]) * i / (data_depth - 1.0f);
        // corner 0
        coordinatesZ[i12 + tuple_index[0]] = limits[0][0];
        coordinatesZ[i12 + tuple_index[1]] = limits[1][0];
        coordinatesZ[i12 + tuple_index[2]] = depth;
        // corner 1
        coordinatesZ[i12 + 3 + tuple_index[0]] = limits[0][1];
        coordinatesZ[i12 + 3 + tuple_index[1]] = limits[1][0];
        coordinatesZ[i12 + 3 + tuple_index[2]] = depth;
        // corner 2
        coordinatesZ[i12 + 6 + tuple_index[0]] = limits[0][1];
        coordinatesZ[i12 + 6 + tuple_index[1]] = limits[1][1];
        coordinatesZ[i12 + 6 + tuple_index[2]] = depth;
        // corner 3
        coordinatesZ[i12 + 9 + tuple_index[0]] = limits[0][0];
        coordinatesZ[i12 + 9 + tuple_index[1]] = limits[1][1];
        coordinatesZ[i12 + 9 + tuple_index[2]] = depth;
      }

      for (int i=0; i<data_height; i++) {
        int i12 = i * 12;
        float height = limits[1][0] +
          (limits[1][1] - limits[1][0]) * i / (data_height - 1.0f);
        // corner 0
        coordinatesY[i12 + tuple_index[0]] = limits[0][0];
        coordinatesY[i12 + tuple_index[1]] = height;
        coordinatesY[i12 + tuple_index[2]] = limits[2][0];
        // corner 1
        coordinatesY[i12 + 3 + tuple_index[0]] = limits[0][1];
        coordinatesY[i12 + 3 + tuple_index[1]] = height;
        coordinatesY[i12 + 3 + tuple_index[2]] = limits[2][0];
        // corner 2
        coordinatesY[i12 + 6 + tuple_index[0]] = limits[0][1];
        coordinatesY[i12 + 6 + tuple_index[1]] = height;
        coordinatesY[i12 + 6 + tuple_index[2]] = limits[2][1];
        // corner 3
        coordinatesY[i12 + 9 + tuple_index[0]] = limits[0][0];
        coordinatesY[i12 + 9 + tuple_index[1]] = height;
        coordinatesY[i12 + 9 + tuple_index[2]] = limits[2][1];
      }

      for (int i=0; i<data_width; i++) {
        int i12 = i * 12;
        float width = limits[0][0] +
          (limits[0][1] - limits[0][0]) * i / (data_width - 1.0f);
        // corner 0
        coordinatesX[i12 + tuple_index[0]] = width;
        coordinatesX[i12 + tuple_index[1]] = limits[1][0];
        coordinatesX[i12 + tuple_index[2]] = limits[2][0];
        // corner 1
        coordinatesX[i12 + 3 + tuple_index[0]] = width;
        coordinatesX[i12 + 3 + tuple_index[1]] = limits[1][1];
        coordinatesX[i12 + 3 + tuple_index[2]] = limits[2][0];
        // corner 2
        coordinatesX[i12 + 6 + tuple_index[0]] = width;
        coordinatesX[i12 + 6 + tuple_index[1]] = limits[1][1];
        coordinatesX[i12 + 6 + tuple_index[2]] = limits[2][1];
        // corner 3
        coordinatesX[i12 + 9 + tuple_index[0]] = width;
        coordinatesX[i12 + 9 + tuple_index[1]] = limits[1][0];
        coordinatesX[i12 + 9 + tuple_index[2]] = limits[2][1];
      }

      float ratiow = ((float) data_width) / ((float) texture_width);
      float ratioh = ((float) data_height) / ((float) texture_height);
      float ratiod = ((float) data_depth) / ((float) texture_depth);

      if (t3dm == GraphicsModeControl.STACK2D) {
        texCoordsX =
          shadow_api.setTexStackCoords(data_width, 0, ratiow, ratioh, ratiod);
        texCoordsY =
          shadow_api.setTexStackCoords(data_height, 1, ratiow, ratioh, ratiod);
        texCoordsZ =
          shadow_api.setTexStackCoords(data_depth, 2, ratiow, ratioh, ratiod);
      } else {
        texCoordsX =
          shadow_api.setTex3DCoords(data_width, 0, ratiow, ratioh, ratiod);
        texCoordsY =
          shadow_api.setTex3DCoords(data_height, 1, ratiow, ratioh, ratiod);
        texCoordsZ =
          shadow_api.setTex3DCoords(data_depth, 2, ratiow, ratioh, ratiod);
      }

      normalsX = new float[12 * data_width];
      normalsY = new float[12 * data_height];
      normalsZ = new float[12 * data_depth];
      float n0, n1, n2, nlen;
      n0 = ((coordinatesX[3+2]-coordinatesX[0+2]) *
            (coordinatesX[6+1]-coordinatesX[0+1])) -
           ((coordinatesX[3+1]-coordinatesX[0+1]) *
            (coordinatesX[6+2]-coordinatesX[0+2]));
      n1 = ((coordinatesX[3+0]-coordinatesX[0+0]) *
            (coordinatesX[6+2]-coordinatesX[0+2])) -
           ((coordinatesX[3+2]-coordinatesX[0+2]) *
            (coordinatesX[6+0]-coordinatesX[0+0]));
      n2 = ((coordinatesX[3+1]-coordinatesX[0+1]) *
            (coordinatesX[6+0]-coordinatesX[0+0])) -
           ((coordinatesX[3+0]-coordinatesX[0+0]) *
            (coordinatesX[6+1]-coordinatesX[0+1]));
      nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
      n0 = n0 / nlen;
      n1 = n1 / nlen;
      n2 = n2 / nlen;
      for (int i=0; i<normalsX.length; i+=3) {
        normalsX[i] = n0;
        normalsX[i + 1] = n1;
        normalsX[i + 2] = n2;
      }

      n0 = ((coordinatesY[3+2]-coordinatesY[0+2]) *
            (coordinatesY[6+1]-coordinatesY[0+1])) -
           ((coordinatesY[3+1]-coordinatesY[0+1]) *
            (coordinatesY[6+2]-coordinatesY[0+2]));
      n1 = ((coordinatesY[3+0]-coordinatesY[0+0]) *
            (coordinatesY[6+2]-coordinatesY[0+2])) -
           ((coordinatesY[3+2]-coordinatesY[0+2]) *
            (coordinatesY[6+0]-coordinatesY[0+0]));
      n2 = ((coordinatesY[3+1]-coordinatesY[0+1]) *
            (coordinatesY[6+0]-coordinatesY[0+0])) -
           ((coordinatesY[3+0]-coordinatesY[0+0]) *
            (coordinatesY[6+1]-coordinatesY[0+1]));
      nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
      n0 = n0 / nlen;
      n1 = n1 / nlen;
      n2 = n2 / nlen;
      for (int i=0; i<normalsY.length; i+=3) {
        normalsY[i] = n0;
        normalsY[i + 1] = n1;
        normalsY[i + 2] = n2;
      }

      n0 = ((coordinatesZ[3+2]-coordinatesZ[0+2]) *
            (coordinatesZ[6+1]-coordinatesZ[0+1])) -
           ((coordinatesZ[3+1]-coordinatesZ[0+1]) *
            (coordinatesZ[6+2]-coordinatesZ[0+2]));
      n1 = ((coordinatesZ[3+0]-coordinatesZ[0+0]) *
            (coordinatesZ[6+2]-coordinatesZ[0+2])) -
           ((coordinatesZ[3+2]-coordinatesZ[0+2]) *
            (coordinatesZ[6+0]-coordinatesZ[0+0]));
      n2 = ((coordinatesZ[3+1]-coordinatesZ[0+1]) *
            (coordinatesZ[6+0]-coordinatesZ[0+0])) -
           ((coordinatesZ[3+0]-coordinatesZ[0+0]) *
            (coordinatesZ[6+1]-coordinatesZ[0+1]));
      nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
      n0 = n0 / nlen;
      n1 = n1 / nlen;
      n2 = n2 / nlen;
      for (int i=0; i<normalsZ.length; i+=3) {
        normalsZ[i] = n0;
        normalsZ[i + 1] = n1;
        normalsZ[i + 2] = n2;
      }

      colorsX = new byte[12 * data_width];
      colorsY = new byte[12 * data_height];
      colorsZ = new byte[12 * data_depth];
      for (int i=0; i<12*data_width; i++) colorsX[i] = (byte) 127;
      for (int i=0; i<12*data_height; i++) colorsY[i] = (byte) 127;
      for (int i=0; i<12*data_depth; i++) colorsZ[i] = (byte) 127;

/*
for (int i=0; i < 4; i++) {
  System.out.println("i = " + i + " texCoordsX = " + texCoordsX[3 * i] + " " +
                     texCoordsX[3 * i + 1]);
  System.out.println(" coordinatesX = " + coordinatesX[3 * i] + " " +
                     coordinatesX[3 * i + 1] + " " + coordinatesX[3 * i + 2]);
  System.out.println(" normalsX = " + normalsX[3 * i]  + " " +
                     normalsX[3 * i + 1] + " " + normalsX[3 * i + 2]);
}
*/
    }
    // WLH 1 April 2000
    // else { // !isTextureMap && !isTexture3D
    // WLH 16 July 2000 - add '&& !isLinearContour3D'
    if (!isTextureMap && (!isTexture3D || range3D) && !isLinearContour3D) {

// if (link != null) System.out.println("start domain " + (System.currentTimeMillis() - link.start_time));

      // get values from Function Domain
      // NOTE - may defer this until needed, if needed
      if (domain_dimension == 1) {
        domain_doubles = domain_set.getDoubles(false);
        domain_doubles = Unit.convertTuple(domain_doubles, dataUnits, domain_units);
        mapValues(display_values, domain_doubles, DomainComponents);
      }
      else {
        // do shallow clone and don't copy in convert DRM 2003-02-24
        domain_values = (float[][]) domain_set.getSamples(false).clone();

        // convert values to default units (used in display)
        // MEM & FREE
        //domain_values = Unit.convertTuple(domain_values, dataUnits, domain_units);
        domain_values = 
           Unit.convertTuple(domain_values, dataUnits, domain_units, false);
        // System.out.println("got domain_values: domain_length = " + domain_length);

        // map domain_values to appropriate DisplayRealType-s
        // MEM
        mapValues(display_values, domain_values, DomainComponents);
      }

      // System.out.println("mapped domain_values");

      ShadowRealTupleType domain_reference = Domain.getReference();

/*
      System.out.println("domain_reference = " + domain_reference);
      if (domain_reference != null) {
        System.out.println("getMappedDisplayScalar = " +
                           domain_reference.getMappedDisplayScalar());
      }
*/

// if (link != null) System.out.println("end domain " + (System.currentTimeMillis() - link.start_time));

      if (domain_reference != null && domain_reference.getMappedDisplayScalar()) {
        // apply coordinate transform to domain values
        RealTupleType ref = (RealTupleType) domain_reference.getType();
        // MEM
        float[][] reference_values = null;
        double[][] reference_doubles = null;
        if (domain_dimension == 1) {
          reference_doubles =
            CoordinateSystem.transformCoordinates(
              ref, null, ref.getDefaultUnits(), null,
              (RealTupleType) Domain.getType(), dataCoordinateSystem,
              domain_units, null, domain_doubles);
        }
        else {
          // this interferes with correct handling of missing data
          // if (curvedTexture && domainOnlySpatial) {
          if (false) {

 //if (link != null) System.out.println("start compute spline " + (System.currentTimeMillis() - link.start_time));

            int[] lengths = ((GriddedSet) domain_set).getLengths();
            data_width = lengths[0];
            data_height = lengths[1];
            texture_width = shadow_api.textureWidth(data_width);
            texture_height = shadow_api.textureHeight(data_height);

            int size = (data_width + data_height) / 2;
            curved_size = Math.max(1, Math.min(curved_size, size / 32));

            int nwidth = 2 + (data_width - 1) / curved_size;
            int nheight = 2 + (data_height - 1) / curved_size;
/*
System.out.println("data_width = " + data_width + " data_height = " + data_height +
     " texture_width = " + texture_width + " texture_height = " + texture_height +
     " nwidth = " + nwidth + " nheight = " + nheight);
*/
            int nn = nwidth * nheight;
            int[] is = new int[nwidth];
            int[] js = new int[nheight];
            for (int i=0; i<nwidth; i++) {
              is[i] = Math.min(i * curved_size, data_width - 1);
            }
            for (int j=0; j<nheight; j++) {
              js[j] = Math.min(j * curved_size, data_height - 1);
            }
            float[][] spline_domain =
              new float[domain_dimension][nwidth * nheight];
            int k = 0;
            for (int j=0; j<nheight; j++) {
              for (int i=0; i<nwidth; i++) {
                int ij = is[i] + data_width * js[j];
                spline_domain[0][k] = domain_values[0][ij];
                spline_domain[1][k] = domain_values[1][ij];
                if (domain_dimension == 3) spline_domain[2][k] = domain_values[2][ij];
                k++;
              }
            }
            float[][] spline_reference =
              CoordinateSystem.transformCoordinates(
                ref, null, ref.getDefaultUnits(), null,
                (RealTupleType) Domain.getType(), dataCoordinateSystem,
                domain_units, null, spline_domain);
            reference_values = new float[domain_dimension][domain_length];
            for (int i=0; i<domain_length; i++) {
              reference_values[0][i] = Float.NaN;
              reference_values[1][i] = Float.NaN;
              if (domain_dimension == 3) reference_values[2][i] = Float.NaN;
            }
            k = 0;
            for (int j=0; j<nheight; j++) {
              for (int i=0; i<nwidth; i++) {
                int ij = is[i] + data_width * js[j];
                reference_values[0][ij] = spline_reference[0][k];
                reference_values[1][ij] = spline_reference[1][k];
                if (domain_dimension == 3) reference_values[2][ij] = spline_reference[2][k];
                k++;
              }
            }
// if (link != null) System.out.println("end compute spline " + (System.currentTimeMillis() - link.start_time));
          }
          else { // if !(curvedTexture && domainOnlySpatial)
            reference_values =
              CoordinateSystem.transformCoordinates(
                ref, null, ref.getDefaultUnits(), null,
                (RealTupleType) Domain.getType(), dataCoordinateSystem,
                domain_units, null, domain_values);
          }
        } // end if !(domain_dimension == 1)

        // WLH 13 Macrh 2000
        // if (anyFlow) {
          renderer.setEarthSpatialData(Domain, domain_reference, ref,
                      ref.getDefaultUnits(), (RealTupleType) Domain.getType(),
                      new CoordinateSystem[] {dataCoordinateSystem},
                      domain_units);
        // WLH 13 Macrh 2000
        // }

        //
        // TO_DO
        // adjust any RealVectorTypes in range
        // see FlatField.resample and FieldImpl.resample
        //

// if (link != null) System.out.println("start map reference " + (System.currentTimeMillis() - link.start_time));

        // map reference_values to appropriate DisplayRealType-s
        ShadowRealType[] DomainReferenceComponents = getDomainReferenceComponents();
        // MEM
        if (domain_dimension == 1) {
          mapValues(display_values, reference_doubles, DomainReferenceComponents);
        }
        else {
          mapValues(display_values, reference_values, DomainReferenceComponents);
        }

// if (link != null) System.out.println("end map reference " + (System.currentTimeMillis() - link.start_time));

/*
for (int i=0; i<DomainReferenceComponents.length; i++) {
  System.out.println("DomainReferenceComponents[" + i + "] = " +
                     DomainReferenceComponents[i]);
  System.out.println("reference_values[" + i + "].length = " +
                     reference_values[i].length);
}
        System.out.println("mapped domain_reference values");
*/
        // FREE
        reference_values = null;
        reference_doubles = null;
      }
      else { // if !(domain_reference != null &&
             //      domain_reference.getMappedDisplayScalar())
        // WLH 13 March 2000
        // if (anyFlow) {
/* WLH 23 May 99
          renderer.setEarthSpatialData(Domain, null, null,
                      null, (RealTupleType) Domain.getType(),
                      new CoordinateSystem[] {dataCoordinateSystem},
                      domain_units);
*/
          RealTupleType ref = (domain_reference == null) ? null :
                              (RealTupleType) domain_reference.getType();
          Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
          renderer.setEarthSpatialData(Domain, domain_reference, ref,
                      ref_units, (RealTupleType) Domain.getType(),
                      new CoordinateSystem[] {dataCoordinateSystem},
                      domain_units);
        // WLH 13 March 2000
        // }
      }
      // FREE
      domain_values = null;
      domain_doubles = null;
    } // end if (!isTextureMap && (!isTexture3D || range3D) &&
      //         !isLinearContour3D)

    if (this instanceof ShadowFunctionType) {

// if (link != null) System.out.println("start range " + (System.currentTimeMillis() - link.start_time));

      // get range_values for RealType and RealTupleType
      // components, in defaultUnits for RealType-s
      // MEM - may copy (in convertTuple)
      float[][] range_values = ((Field) data).getFloats(false);

      // System.out.println("got range_values");

      if (range_values != null) {
        // map range_values to appropriate DisplayRealType-s
        ShadowRealType[] RangeComponents = getRangeComponents();
        // MEM
        mapValues(display_values, range_values, RangeComponents);

        // System.out.println("mapped range_values");

        //
        // transform any range CoordinateSystem-s
        // into display_values, then mapValues
        //
        int[] refToComponent = getRefToComponent();
        ShadowRealTupleType[] componentWithRef = getComponentWithRef();
        int[] componentIndex = getComponentIndex();

        if (refToComponent != null) {

          for (int i=0; i<refToComponent.length; i++) {
            int n = componentWithRef[i].getDimension();
            int start = refToComponent[i];
            float[][] values = new float[n][];
            for (int j=0; j<n; j++) values[j] = range_values[j + start];
            ShadowRealTupleType component_reference =
              componentWithRef[i].getReference();
            RealTupleType ref = (RealTupleType) component_reference.getType();
            Unit[] range_units;
            CoordinateSystem[] range_coord_sys;
            if (i == 0 && componentWithRef[i].equals(Range)) {
              range_units = ((Field) data).getDefaultRangeUnits();
              range_coord_sys = ((Field) data).getRangeCoordinateSystem();
            }
            else {
              Unit[] dummy_units = ((Field) data).getDefaultRangeUnits();
              range_units = new Unit[n];
              for (int j=0; j<n; j++) range_units[j] = dummy_units[j + start];
              range_coord_sys =
                ((Field) data).getRangeCoordinateSystem(componentIndex[i]);
            }

            float[][] reference_values = null;
            if (range_coord_sys.length == 1) {
              // MEM
              reference_values =
                CoordinateSystem.transformCoordinates(
                  ref, null, ref.getDefaultUnits(), null,
                  (RealTupleType) componentWithRef[i].getType(),
                  range_coord_sys[0], range_units, null, values);

              // WLH 13 March 2000
              // if (anyFlow) {
                renderer.setEarthSpatialData(componentWithRef[i],
                      component_reference, ref, ref.getDefaultUnits(),
                      (RealTupleType) componentWithRef[i].getType(),
                      range_coord_sys, range_units);
              // WLH 13 March 2000
              // }

            }
            else {
              // MEM
              reference_values = new float[n][domain_length];
              float[][] temp = new float[n][1];
              for (int j=0; j<domain_length; j++) {
                for (int k=0; k<n; k++) temp[k][0] = values[k][j];
                temp =
                  CoordinateSystem.transformCoordinates(
                    ref, null, ref.getDefaultUnits(), null,
                    (RealTupleType) componentWithRef[i].getType(),
                    range_coord_sys[j], range_units, null, temp);
                for (int k=0; k<n; k++) reference_values[k][j] = temp[k][0];
              }

              // WLH 13 March 2000
              // if (anyFlow) {
                renderer.setEarthSpatialData(componentWithRef[i],
                      component_reference, ref, ref.getDefaultUnits(),
                      (RealTupleType) componentWithRef[i].getType(),
                      range_coord_sys, range_units);
              // WLH 13 March 2000
              // }

            }

            // map reference_values to appropriate DisplayRealType-s
            // MEM
/* WLH 17 April 99
            mapValues(display_values, reference_values,
                      getComponents(componentWithRef[i], false));
*/
            mapValues(display_values, reference_values,
                      getComponents(component_reference, false));
            // FREE
            reference_values = null;
            // FREE (redundant reference to range_values)
            values = null;
          } // end for (int i=0; i<refToComponent.length; i++)
        } // end (refToComponent != null)

// if (link != null) System.out.println("end range " + (System.currentTimeMillis() - link.start_time));

        // setEarthSpatialData calls when no CoordinateSystem
        // WLH 13 March 2000
        // if (Range instanceof ShadowTupleType && anyFlow) {
        if (Range instanceof ShadowTupleType) {
          if (Range instanceof ShadowRealTupleType) {
            Unit[] range_units = ((Field) data).getDefaultRangeUnits();
            CoordinateSystem[] range_coord_sys =
              ((Field) data).getRangeCoordinateSystem();
/* WLH 23 May 99
            renderer.setEarthSpatialData((ShadowRealTupleType) Range,
                      null, null, null, (RealTupleType) Range.getType(),
                      range_coord_sys, range_units);
*/
            ShadowRealTupleType component_reference =
              ((ShadowRealTupleType) Range).getReference();
            RealTupleType ref = (component_reference == null) ? null :
                                (RealTupleType) component_reference.getType();
            Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
            renderer.setEarthSpatialData((ShadowRealTupleType) Range,
                      component_reference, ref, ref_units,
                      (RealTupleType) Range.getType(),
                      range_coord_sys, range_units);
          }
          else { // if (!(Range instanceof ShadowRealTupleType))
            Unit[] dummy_units = ((Field) data).getDefaultRangeUnits();
            int start = 0;
            int n = ((ShadowTupleType) Range).getDimension();
            for (int i=0; i<n ;i++) {
              ShadowType range_component =
                ((ShadowTupleType) Range).getComponent(i);
              if (range_component instanceof ShadowRealTupleType) {
                int m = ((ShadowRealTupleType) range_component).getDimension();
                Unit[] range_units = new Unit[m];
                for (int j=0; j<m; j++) range_units[j] = dummy_units[j + start];
                CoordinateSystem[] range_coord_sys =
                  ((Field) data).getRangeCoordinateSystem(i);
/* WLH 23 May 99
                renderer.setEarthSpatialData((ShadowRealTupleType)
                      range_component, null, null,
                      null, (RealTupleType) range_component.getType(),
                      range_coord_sys, range_units);
*/
                ShadowRealTupleType component_reference =
                  ((ShadowRealTupleType) range_component).getReference();
                RealTupleType ref = (component_reference == null) ? null :
                                    (RealTupleType) component_reference.getType();
                Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
                renderer.setEarthSpatialData((ShadowRealTupleType) range_component,
                      component_reference, ref, ref_units,
                      (RealTupleType) range_component.getType(),
                      range_coord_sys, range_units);
                start += ((ShadowRealTupleType) range_component).getDimension();
              }
              else if (range_component instanceof ShadowRealType) {
                start++;
              }
            }
          } // end if (!(Range instanceof ShadowRealTupleType))
        } // end if (Range instanceof ShadowTupleType)

        // FREE
        range_values = null;
      } // end if (range_values != null)

      if (anyText && text_values == null) {
        for (int i=0; i<valueArrayLength; i++) {
          if (display_values[i] != null) {
            int displayScalarIndex = valueToScalar[i];
            ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
            ScalarType real = map.getScalar();
            DisplayRealType dreal = display.getDisplayScalar(displayScalarIndex);
            if (dreal.equals(Display.Text) && real instanceof RealType) {
              text_control = (TextControl) map.getControl();
              text_values = new String[domain_length];
              NumberFormat format = text_control.getNumberFormat();
              if (display_values[i].length == 1) {
                String text = null;
                if (display_values[i][0] != display_values[i][0]) {
                  text = "";
                }
                else if (format == null) {
                  text = PlotText.shortString(display_values[i][0]);
                }
                else {
                  text = format.format(display_values[i][0]);
                }
                for (int j=0; j<domain_length; j++) {
                  text_values[j] = text;
                }
              }
              else {
                if (format == null) {
                  for (int j=0; j<domain_length; j++) {
                    if (display_values[i][j] != display_values[i][j]) {
                      text_values[j] = "";
                    }
                    else {
                      text_values[j] = PlotText.shortString(display_values[i][j]);
                    }
                  }
                }
                else {
                  for (int j=0; j<domain_length; j++) {
                    if (display_values[i][j] != display_values[i][j]) {
                      text_values[j] = ""; 
                    }
                    else {
                      text_values[j] = format.format(display_values[i][j]);
                    }
                  }
                }
              }
              break;
            }
          }
        }

        if (text_values == null) {
          String[][] string_values = ((Field) data).getStringValues();
          if (string_values != null) {
            int[] textIndices = ((FunctionType) getType()).getTextIndices();
            int n = string_values.length;
            if (Range instanceof ShadowTextType) {
              Vector maps = shadow_api.getTextMaps(-1, textIndices);
              if (!maps.isEmpty()) {
                text_values = string_values[0];
                ScalarMap map = (ScalarMap) maps.firstElement();
                text_control = (TextControl) map.getControl();
  /*
  System.out.println("Range is ShadowTextType, text_values[0] = " +
                     text_values[0] + " n = " + n);
  */
              }
            }
            else if (Range instanceof ShadowTupleType) {
              for (int i=0; i<n; i++) {
                Vector maps = shadow_api.getTextMaps(i, textIndices);
                if (!maps.isEmpty()) {
                  text_values = string_values[i];
                  ScalarMap map = (ScalarMap) maps.firstElement();
                  text_control = (TextControl) map.getControl();
  /*
  System.out.println("Range is ShadowTupleType, text_values[0] = " +
                     text_values[0] + " n = " + n + " i = " + i);
  */
                }
              }
            } // end if (Range instanceof ShadowTupleType)
          } // end if (string_values != null)
        } // end if (text_values == null)
      } // end if (anyText && text_values == null)
    } // end if (this instanceof ShadowFunctionType)

// if (link != null) System.out.println("start assembleSelect " + (System.currentTimeMillis() - link.start_time));

    //
    // NOTE -
    // currently assuming SelectRange changes require Transform
    // see DataRenderer.isTransformControl
    //
    // get array that composites SelectRange components
    // range_select is null if all selected
    // MEM
    boolean[][] range_select =
      shadow_api.assembleSelect(display_values, domain_length, valueArrayLength,
                     valueToScalar, display, shadow_api);
/*
if (range_select[0] != null) {
  int numforced = 0;
  for (int k=0; k<range_select[0].length; k++) {
    if (!range_select[0][k]) numforced++;
  }
  System.out.println("assembleSelect: numforced = " + numforced);
}
*/
    if (range_select[0] != null && range_select[0].length == 1 &&
        !range_select[0][0]) {
      // single missing value in range_select[0], so render nothing
      return false;
    }

// if (link != null) System.out.println("end assembleSelect " + (System.currentTimeMillis() - link.start_time));

    // System.out.println("assembleSelect");
 /*
System.out.println("doTerminal: isTerminal = " + getIsTerminal() +
                   " LevelOfDifficulty = " + LevelOfDifficulty);
*/
    if (getIsTerminal()) {
      if (!getFlat()) {
        throw new DisplayException("terminal but not Flat");
      }

      GraphicsModeControl mode = (GraphicsModeControl)
        display.getGraphicsModeControl().clone();
      float pointSize =
        default_values[display.getDisplayScalarIndex(Display.PointSize)];
      mode.setPointSize(pointSize, true);
      float lineWidth =
        default_values[display.getDisplayScalarIndex(Display.LineWidth)];
      mode.setLineWidth(lineWidth, true);
      int lineStyle = (int)
        default_values[display.getDisplayScalarIndex(Display.LineStyle)];
      mode.setLineStyle(lineStyle, true);
      int polygonMode = (int)
        default_values[display.getDisplayScalarIndex(Display.PolygonMode)];
      mode.setPolygonMode(polygonMode, true);
      float polygonOffset =
        default_values[display.getDisplayScalarIndex(Display.PolygonOffset)];
      mode.setPolygonOffset(polygonOffset, true);
      float polygonOffsetFactor = 
        default_values[
          display.getDisplayScalarIndex(Display.PolygonOffsetFactor)];
      mode.setPolygonOffsetFactor(polygonOffsetFactor, true);

      boolean pointMode = mode.getPointMode();

      float missingTransparent =
          default_values[display.getDisplayScalarIndex(Display.MissingTransparent)];
      boolean isMissingTransparent = missingTransparent > 0.5f;
      /* DRM: 2005-08-29 - default value now correct
      boolean isMissingTransparent = mode.getMissingTransparent();
      if (missingTransparent > -0.5f) {
        isMissingTransparent = (missingTransparent > 0.5f);
      }
      */

// if (link != null) System.out.println("start assembleColor " + (System.currentTimeMillis() - link.start_time));

      // MEM_WLH - this moved
      boolean[] single_missing = {false, false, false, false};
      // assemble an array of RGBA values
      // MEM
      byte[][] color_values =
        shadow_api.assembleColor(display_values, valueArrayLength, valueToScalar,
                      display, default_values, range_select,
                      single_missing, shadow_api);
/*
if (range_select[0] != null) {
  int numforced = 0;
  for (int k=0; k<range_select[0].length; k++) {
    if (!range_select[0][k]) numforced++;
  }
  System.out.println("assembleColor: numforced = " + numforced);
}
*/
/*
if (color_values != null) {
  System.out.println("color_values.length = " + color_values.length +
                     " color_values[0].length = " + color_values[0].length);
  System.out.println(color_values[0][0] + " " + color_values[1][0] +
                     " " + color_values[2][0]);
}
*/

      if (range_select[0] != null && range_select[0].length == 1 &&
          !range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

// if (link != null) System.out.println("end assembleColor " + (System.currentTimeMillis() - link.start_time));

      float[][] flow1_values = new float[3][];
      float[][] flow2_values = new float[3][];
      float[] flowScale = new float[2];
      // MEM
      shadow_api.assembleFlow(flow1_values, flow2_values, flowScale,
                   display_values, valueArrayLength, valueToScalar,
                   display, default_values, range_select, renderer,
                   shadow_api);
/*
if (range_select[0] != null) {
  int numforced = 0;
  for (int k=0; k<range_select[0].length; k++) {
    if (!range_select[0][k]) numforced++;
  }
  System.out.println("assembleFlow: numforced = " + numforced);
}
*/
      if (range_select[0] != null && range_select[0].length == 1 &&
          !range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

      // System.out.println("assembleFlow");

      // assemble an array of Display.DisplaySpatialCartesianTuple values
      // and possibly spatial_set
      float[][] spatial_values = new float[3][];

      // spatialDimensions[0] = spatialDomainDimension and
      // spatialDimensions[1] = spatialManifoldDimension
      int[] spatialDimensions = new int[2];
      // flags for swapping rows and columns in contour labels
      boolean[] swap = {false, false, false};

// if (link != null) System.out.println("start assembleSpatial " + (System.currentTimeMillis() - link.start_time));

      // WLH 29 April 99
      boolean[][] spatial_range_select = new boolean[1][];

      // MEM - but not if isTextureMap
      Set spatial_set =
        shadow_api.assembleSpatial(spatial_values, display_values, valueArrayLength,
                        valueToScalar, display, default_values,
                        inherited_values, domain_set, Domain.getAllSpatial(),
                        anyContour && !isLinearContour3D,
                        spatialDimensions, spatial_range_select,
                        flow1_values, flow2_values, flowScale, swap, renderer,
                        shadow_api);

      if (isLinearContour3D) {
        spatial_set = domain_set;
        spatialDimensions[0] = 3;
        spatialDimensions[1] = 3;
      }

      // WLH 29 April 99
      boolean spatial_all_select = true;
      if (spatial_range_select[0] != null) {
        spatial_all_select = false;
        if (range_select[0] == null) {
          range_select[0] = spatial_range_select[0];
        }
        else if (spatial_range_select[0].length == 1) {
          for (int j=0; j<range_select[0].length; j++) {
            range_select[0][j] =
              range_select[0][j] && spatial_range_select[0][0];
          }
        }
        else {
          for (int j=0; j<range_select[0].length; j++) {
            range_select[0][j] =
              range_select[0][j] && spatial_range_select[0][j];
          }
        }
      }
      spatial_range_select = null;

/*
if (range_select[0] != null) {
  int numforced = 0;
  for (int k=0; k<range_select[0].length; k++) {
    if (!range_select[0][k]) numforced++;
  }
  System.out.println("assembleSpatial: numforced = " + numforced);
}
*/
/*
System.out.println("assembleSpatial  (spatial_set == null) = " +
  (spatial_set == null));
if (spatial_set != null) {
  System.out.println("spatial_set.length = " + spatial_set.getLength());
}
System.out.println("  spatial_values lengths = " + spatial_values[0].length +
  " " + spatial_values[1].length + " " + spatial_values[2].length);
System.out.println("  isTextureMap = " + isTextureMap);
*/
      if (range_select[0] != null && range_select[0].length == 1 &&
          !range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

// if (link != null) System.out.println("end assembleSpatial " + (System.currentTimeMillis() - link.start_time));

      int spatialDomainDimension = spatialDimensions[0];
      int spatialManifoldDimension = spatialDimensions[1];

      // System.out.println("assembleSpatial");

      int spatial_length = Math.min(domain_length, spatial_values[0].length);

      int color_length = Math.min(domain_length, color_values[0].length);
      int alpha_length = color_values[3].length;
/*
      System.out.println("assembleColor, color_length = " + color_length +
                         "  " + color_values.length);
*/

// if (link != null) System.out.println("start missing color " + (System.currentTimeMillis() - link.start_time));

      float constant_alpha = Float.NaN;
      float[] constant_color = null;

      // note alpha_length <= color_length
      if (alpha_length == 1) {
/* MEM_WLH
        if (color_values[3][0] != color_values[3][0]) {
*/
        if (single_missing[3]) {
          // a single missing alpha value, so render nothing
          // System.out.println("single missing alpha");
          return false;
        }
        // System.out.println("single alpha " + color_values[3][0]);
        // constant alpha, so put it in appearance
/* MEM_WLH
        if (color_values[3][0] > 0.999999f) {
*/
        if (color_values[3][0] == -1) {  // = 255 unsigned
          constant_alpha = 0.0f;
          // constant_alpha = 1.0f; WLH 26 May 99
          // remove alpha from color_values
          byte[][] c = new byte[3][];
          c[0] = color_values[0];
          c[1] = color_values[1];
          c[2] = color_values[2];
          color_values = c;
        }
        else { // not opaque
/* TransparencyAttributes with constant alpha seems to have broken
   from The Java 3D API Specification: p. 118 transparency = 1 - alpha,
   p. 116 transparency 0.0 = opaque, 1.0 = clear */
/*
          broken alpha - put it back when alpha fixed
          constant_alpha =
            new TransparencyAttributes(TransparencyAttributes.NICEST,
                             1.0f - byteToFloat(color_values[3][0]));
   so expand constant alpha to variable alpha
   and note no alpha in Java2D:
*/
          byte v = color_values[3][0];
          color_values[3] = new byte[color_values[0].length];
          for (int i=0; i<color_values[0].length; i++) {
            color_values[3][i] = v;
          }
/*
System.out.println("replicate alpha = " + v + " " + constant_alpha +
                   " " + color_values[0].length + " " +
                   color_values[3].length);
*/
        } // end not opaque
/*
        broken alpha - put it back when alpha fixed
        // remove alpha from color_values
        byte[][] c = new byte[3][];
        c[0] = color_values[0];
        c[1] = color_values[1];
        c[2] = color_values[2];
        color_values = c;
*/
      } // end if (alpha_length == 1)
      if (color_length == 1) {
        if (spatialManifoldDimension == 1 ||
            shadow_api.allowConstantColorSurfaces()) {
/* MEM_WLH
          if (color_values[0][0] != color_values[0][0] ||
              color_values[1][0] != color_values[1][0] ||
              color_values[2][0] != color_values[2][0]) {
*/
          if (single_missing[0] || single_missing[1] ||
              single_missing[2]) {
            // System.out.println("single missing color");
            // a single missing color value, so render nothing
            return false;
          }
/* MEM_WLH
          constant_color = new float[] {color_values[0][0], color_values[1][0],
                                        color_values[2][0]};
*/
          constant_color = new float[] {byteToFloat(color_values[0][0]),
                                        byteToFloat(color_values[1][0]),
                                        byteToFloat(color_values[2][0])};
          color_values = null; // in this case, alpha doesn't matter
        }
        else {
          // constant color doesn't work for surfaces in Java3D
          // because of lighting
          byte[][] c = new byte[color_values.length][domain_length];
          for (int i=0; i<color_values.length; i++) {
            for (int j=0; j<domain_length; j++) {
              c[i][j] = color_values[i][0];
            }
          }
          color_values = c;
        }
      } // end if (color_length == 1)

// if (link != null) System.out.println("end missing color " + (System.currentTimeMillis() - link.start_time));

      if (range_select[0] != null && range_select[0].length == 1 &&
          !range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }
      if (LevelOfDifficulty == SIMPLE_FIELD) {
        // only manage Spatial, Contour, Flow, Color, Alpha and
        // SelectRange here
        //
        // TO_DO
        // Flow rendering trajectories, which will be tricky -
        // FlowControl must contain trajectory start points
        //

/* MISSING TEST
        for (int i=0; i<spatial_values[0].length; i+=3) {
          spatial_values[0][i] = Float.NaN;
        }
END MISSING TEST */

        //
        // TO_DO
        // missing color_values and range_select
        //
        // in Java3D:
        // NaN color component values are rendered as 1.0
        // NaN spatial component values of points are NOT rendered
        // NaN spatial component values of lines are rendered at infinity
        // NaN spatial component values of triangles are a mess ??
        //
/*
        System.out.println("spatialDomainDimension = " +
                           spatialDomainDimension +
                           " spatialManifoldDimension = " +
                           spatialManifoldDimension +
                           " anyContour = " + anyContour +
                           " pointMode = " + pointMode);
*/
        VisADGeometryArray array;

        boolean anyShapeCreated = false;
        VisADGeometryArray[] arrays =
          shadow_api.assembleShape(display_values, valueArrayLength, valueToMap,
                         MapVector, valueToScalar, display, default_values,
                         inherited_values, spatial_values, color_values,
                         range_select, -1, shadow_api);
/*
if (range_select[0] != null) {
  int numforced = 0;
  for (int k=0; k<range_select[0].length; k++) {
    if (!range_select[0][k]) numforced++;
  }
  System.out.println("assembleShape: numforced = " + numforced);
}
*/
        if (arrays != null) {
          for (int i=0; i<arrays.length; i++) {
            array = arrays[i];
            shadow_api.addToGroup(group, array, mode,
                                  constant_alpha, constant_color);
            array = null;
/* WLH 13 March 99 - why null in place of constant_alpha?
            appearance = makeAppearance(mode, null, constant_color, geometry);
*/
          }
          anyShapeCreated = true;
          arrays = null;
        }

        boolean anyTextCreated = false;
        if (anyText && text_values != null && text_control != null) {
          array = shadow_api.makeText(text_values, text_control, spatial_values,
                                      color_values, range_select);
          shadow_api.addTextToGroup(group, array, mode,
                                    constant_alpha, constant_color);
          array = null;
          anyTextCreated = true;
        }

        boolean anyFlowCreated = false;
        if (anyFlow) {
          // try Flow1

          arrays = shadow_api.makeStreamline(0, flow1_values, flowScale[0],
                        spatial_values, spatial_set, spatialManifoldDimension,
                        color_values, range_select,  valueArrayLength,
                        valueToMap, MapVector);
          if (arrays != null) {
            for (int i=0; i<arrays.length; i++) {
              if (arrays[i] != null) {
                shadow_api.addToGroup(group, arrays[i], mode,
                                      constant_alpha, constant_color);
                arrays[i] = null;
              }
            }
          }
          else {
            arrays = shadow_api.makeFlow(0, flow1_values, flowScale[0],
                              spatial_values, color_values, range_select);
            if (arrays != null) {
              for (int i=0; i<arrays.length; i++) {
                if (arrays[i] != null) {
                  shadow_api.addToGroup(group, arrays[i], mode,
                                        constant_alpha, constant_color);
                  arrays[i] = null;
                }
              }
            }
          }
          anyFlowCreated = true;

          // try Flow2

          arrays = shadow_api.makeStreamline(1, flow2_values, flowScale[1],
                          spatial_values, spatial_set, spatialManifoldDimension,
                          color_values, range_select, valueArrayLength,
                        valueToMap, MapVector);
          if (arrays != null) {
            for (int i=0; i<arrays.length; i++) {
              if (arrays[i] != null) {
                shadow_api.addToGroup(group, arrays[i], mode,
                                      constant_alpha, constant_color);
                arrays[i] = null;
              }
            }
          }
          else {
            arrays = shadow_api.makeFlow(1, flow2_values, flowScale[1],
                              spatial_values, color_values, range_select);
            if (arrays != null) {
              for (int i=0; i<arrays.length; i++) {
                if (arrays[i] != null) {
                  shadow_api.addToGroup(group, arrays[i], mode,
                                        constant_alpha, constant_color);
                  arrays[i] = null;
                }
              }
            }
          }
          anyFlowCreated = true;
        }

        boolean anyContourCreated = false;
        if (anyContour) {

/* Test01 at 64 x 64 x 64
domain 701, 491
range 20, 20
assembleColor 210, 201
assembleSpatial 130, 140
makeIsoSurface 381, 520
makeGeometry 350, 171
  all makeGeometry time in calls to Java3D constructors, setCoordinates, etc
*/

// if (link != null) System.out.println("start makeContour " + (System.currentTimeMillis() - link.start_time));
          anyContourCreated =
            shadow_api.makeContour(valueArrayLength, valueToScalar,
                       display_values, inherited_values, MapVector, valueToMap,
                       domain_length, range_select, spatialManifoldDimension,
                       spatial_set, color_values, indexed, group, mode,
                       swap, constant_alpha, constant_color, shadow_api);
// if (link != null) System.out.println("end makeContour " + (System.currentTimeMillis() - link.start_time));
        } // end if (anyContour)

        if (!anyContourCreated && !anyFlowCreated &&
            !anyTextCreated && !anyShapeCreated) {
          // MEM
          if (isTextureMap) {
            if (color_values == null) {
              // must be color_values array for texture mapping
              color_values = new byte[3][domain_length];
              for (int i=0; i<domain_length; i++) {
                color_values[0][i] = floatToByte(constant_color[0]);
                color_values[1][i] = floatToByte(constant_color[1]);
                color_values[2][i] = floatToByte(constant_color[2]);
              }
            }
            if (range_select[0] != null && range_select[0].length > 1) {
              int len = range_select[0].length;
                                                                                                                                    
/* can be misleading because of the way transparency composites
              float alpha =
                default_values[display.getDisplayScalarIndex(Display.Alpha)];
// System.out.println("alpha = " + alpha);
              if (constant_alpha == constant_alpha) {
                alpha = 1.0f - constant_alpha;
// System.out.println("constant_alpha = " + alpha);
              }
              if (color_values.length < 4) {
                byte[][] c = new byte[4][];
                c[0] = color_values[0];
                c[1] = color_values[1];
                c[2] = color_values[2];
                c[3] = new byte[len];
                for (int i=0; i<len; i++) c[3][i] = floatToByte(alpha);
                constant_alpha = Float.NaN;
                color_values = c;
              }
              for (int i=0; i<len; i++) {
                if (!range_select[0][i]) {
                  // make missing pixel invisible (transparent)
                  color_values[3][i] = 0;
                }
              }
*/
              // WLH 27 March 2000
              float alpha =
                default_values[display.getDisplayScalarIndex(Display.Alpha)];
// System.out.println("alpha = " + alpha);
              if (constant_alpha == constant_alpha) {
                alpha = 1.0f - constant_alpha;
// System.out.println("constant_alpha = " + alpha);
              }
              if (color_values.length < 4) {
                byte[][] c = new byte[4][];
                c[0] = color_values[0];
                c[1] = color_values[1];
                c[2] = color_values[2];
                c[3] = new byte[len];
                for (int i=0; i<len; i++) c[3][i] = floatToByte(alpha);
                constant_alpha = Float.NaN;
                color_values = c;
              }
              //if (mode.getMissingTransparent()) {
              if (isMissingTransparent) {
                for (int i=0; i<len; i++) {
                  if (!range_select[0][i]) {
                    // make missing pixel invisible (transparent)
                    color_values[3][i] = 0;
                  }
                }
              }
              else {
                for (int i=0; i<len; i++) {
                  if (!range_select[0][i]) {
                    // make missing pixel black
                    color_values[0][i] = 0;
                    color_values[1][i] = 0;
                    color_values[2][i] = 0;
                  }
                }
              }
            } // end if (range_select[0] != null)

//- begin texture split logic ------------------------------------------------------
            int[] lens = ((GriddedSet)domain_set).getLengths();
                                                                                                                             
            int limit = link.getDisplay().getDisplayRenderer().getTextureWidthMax();
                                                                                                                             
            int y_sub_len = lens[1];
            int n_y_sub   = 1;
            while( y_sub_len >= limit ) {
              y_sub_len /= 2;
              n_y_sub *= 2;
            }
            int[][] y_start_stop = new int[n_y_sub][2];
            for (int k = 0; k < n_y_sub-1; k++) {
              y_start_stop[k][0] = k*y_sub_len;
              y_start_stop[k][1] = (k+1)*y_sub_len - 1;
            }
            int k = n_y_sub-1;
            y_start_stop[k][0] = k*y_sub_len;
            y_start_stop[k][1] = lens[1] - 1;
                                                                                                                             
            int x_sub_len = lens[0];
            int n_x_sub   = 1;
            while( x_sub_len >= limit ) {
              x_sub_len /= 2;
              n_x_sub *= 2;
            }
            int[][] x_start_stop = new int[n_x_sub][2];
            for (k = 0; k < n_x_sub-1; k++) {
              x_start_stop[k][0] = k*x_sub_len;
              x_start_stop[k][1] = (k+1)*x_sub_len - 1;
            }
            k = n_x_sub-1;
            x_start_stop[k][0] = k*x_sub_len;
            x_start_stop[k][1] = lens[0] - 1;

            if (n_y_sub == 1 && n_x_sub == 1) { //- don't split texture
              buildLinearTexture(group, domain_set, dataUnits, domain_units, default_values, shadow_api,
                                 valueArrayLength, valueToScalar, value_array, color_values, 
                                 mode, constant_color, constant_alpha);
            }
            else {
              Object branch = shadow_api.makeBranch();
                                                                                                                             
              int start   = 0;
              int i_total = 0;
              for (int i=0; i<n_y_sub; i++) {
                int leny = y_start_stop[i][1] - y_start_stop[i][0] + 1;
                for (int j=0; j<n_x_sub; j++) {
                  int lenx = x_start_stop[j][1] - x_start_stop[j][0] + 1;
                  float[][] g00 = ((GriddedSet)domain_set).gridToValue(new float[][] {{x_start_stop[j][0]}, {y_start_stop[i][0]}});
                  float[][] g11 = ((GriddedSet)domain_set).gridToValue(new float[][] {{x_start_stop[j][1]}, {y_start_stop[i][1]}});
                  double x0 = g00[0][0];
                  double x1 = g11[0][0];
                  double y0 = g00[1][0];
                  double y1 = g11[1][0];
                  Set dset = new Linear2DSet(x0, x1, lenx, y0, y1, leny);
                  //- tile piece
                  byte[][] color_valuesW = null;
                  if (color_values.length == 3) color_valuesW = new byte[3][lenx*leny];
                  if (color_values.length == 4) color_valuesW = new byte[4][lenx*leny];
                  int cnt = 0;
                  for (k=0; k<leny; k++) {
                    start = x_start_stop[j][0] +  i_total*lens[0] +  k*lens[0];
                    System.arraycopy(color_values[0], start, color_valuesW[0], cnt, lenx);
                    System.arraycopy(color_values[1], start, color_valuesW[1], cnt, lenx);
                    System.arraycopy(color_values[2], start, color_valuesW[2], cnt, lenx);
                    if (color_values.length == 4) System.arraycopy(color_values[3], start, color_valuesW[3], cnt, lenx);
                    cnt += lenx;
                  }
                  Object branch1 = shadow_api.makeBranch();
                  buildLinearTexture(branch1, dset, dataUnits, domain_units, default_values, shadow_api,
                                     valueArrayLength, valueToScalar, value_array, color_valuesW,
                                     mode, constant_color, constant_alpha);
                  shadow_api.addToGroup(branch, branch1);
                }
                i_total += leny;
              }
              shadow_api.addToGroup(group, branch);
            }
//-- end texture split logic -------------------------------------------------------------
            //System.out.println("isTextureMap done");
            return false;
          } // end if (isTextureMap)
          else if (curvedTexture) {
// if (link != null) System.out.println("start texture " + (System.currentTimeMillis() - link.start_time));
            if (color_values == null) { // never true?
              // must be color_values array for texture mapping
              color_values = new byte[3][domain_length];
              for (int i=0; i<domain_length; i++) {
                color_values[0][i] = floatToByte(constant_color[0]);
                color_values[1][i] = floatToByte(constant_color[1]);
                color_values[2][i] = floatToByte(constant_color[2]);
              }
            }

            // DRM 10-Nov-2005 - copy logic from linear texture
            //if (range_select[0] != null) {
            if (range_select[0] != null && range_select[0].length > 1) {
              int len = range_select[0].length;
              float alpha =
                default_values[display.getDisplayScalarIndex(Display.Alpha)];
              if (constant_alpha == constant_alpha) {
                alpha = 1.0f - constant_alpha;
              }
              if (color_values.length < 4) {
                byte[][] c = new byte[4][];
                c[0] = color_values[0];
                c[1] = color_values[1];
                c[2] = color_values[2];
                c[3] = new byte[len];
                for (int i=0; i<len; i++) c[3][i] = floatToByte(alpha);
                constant_alpha = Float.NaN;
                color_values = c;
              }

              // DRM 10-Nov-2005
              //if (isMissingTransparent && color_values.length > 3) {
              if (isMissingTransparent) {
                for (int i=0; i<domain_length; i++) {
                  if (!range_select[0][i]) {
                    // make missing pixel invisible (transparent)
                    color_values[3][i] = 0;
                  }
                }
              }
              else {
                for (int i=0; i<domain_length; i++) {
                  if (!range_select[0][i]) {
                    color_values[0][i] = 0;
                    color_values[1][i] = 0;
                    color_values[2][i] = 0;
                  }
                }
              }
            }
//-- begin texture split logic  ----------------------------------------------------------
            // get domain_set sizes
            int[] lens = ((GriddedSet)domain_set).getLengths();
                                                                                                                                   
            int limit = link.getDisplay().getDisplayRenderer().getTextureWidthMax();
                                                                                                                                   
            int y_sub_len = lens[1];
            int n_y_sub   = 1;
            while( y_sub_len >= limit ) {
              y_sub_len /= 2;
              n_y_sub *= 2;
            }
            int[][] y_start_stop = new int[n_y_sub][2];
            for (int k = 0; k < n_y_sub-1; k++) {
              y_start_stop[k][0] = k*y_sub_len;
              y_start_stop[k][1] = (k+1)*y_sub_len - 1;
            }
            int k = n_y_sub-1;
            y_start_stop[k][0] = k*y_sub_len;
            y_start_stop[k][1] = lens[1] - 1;
                                                                                                                                   
                                                                                                                                   
            int x_sub_len = lens[0];
            int n_x_sub   = 1;
            while( x_sub_len >= limit ) {
              x_sub_len /= 2;
              n_x_sub *= 2;
            }
            int[][] x_start_stop = new int[n_x_sub][2];
            for (k = 0; k < n_x_sub-1; k++) {
              x_start_stop[k][0] = k*x_sub_len;
              x_start_stop[k][1] = (k+1)*x_sub_len - 1;
            }
            k = n_x_sub-1;
            x_start_stop[k][0] = k*x_sub_len;
            x_start_stop[k][1] = lens[0] - 1;

                                                                                                                                   
            if (n_y_sub == 1 && n_x_sub == 1) { //- don't split texture map
              buildCurvedTexture(group, domain_set, dataUnits, domain_units, default_values, shadow_api,
                                 valueArrayLength, valueToScalar, value_array, color_values, mode,
                                 constant_color, constant_alpha, curved_size, spatial_values,
                                 spatial_all_select, renderer, range_select, domainOnlySpatial,
                                 null, lens[0], lens[1], lens[0], lens[1]);
            }
            else {
              Object branch = shadow_api.makeBranch();
              float[][] samples = spatial_values;

              int start   = 0;
              int i_total = 0;
              for (int i=0; i<n_y_sub; i++) {
                int leny = y_start_stop[i][1] - y_start_stop[i][0] + 1;
                for (int j=0; j<n_x_sub; j++) {
                  int lenx = x_start_stop[j][1] - x_start_stop[j][0] + 1;

                  if (j > 0) {  // vertical stitch
                    float[][] samplesC = new float[3][4*leny];
                    byte[][] color_valuesC  = new byte[color_values.length][4*leny];
                    int cntv = 0;
                    int startv = x_start_stop[j][0] + i_total*lens[0];
                    for (int iv=0; iv < leny; iv++) {
                      samplesC[0][cntv] = samples[0][startv-2];
                      samplesC[0][cntv+1] = samples[0][startv-1];
                      samplesC[0][cntv+2] = samples[0][startv];
                      samplesC[0][cntv+3] = samples[0][startv+1];
                      samplesC[1][cntv] = samples[1][startv-2];
                      samplesC[1][cntv+1] = samples[1][startv-1];
                      samplesC[1][cntv+2] = samples[1][startv];
                      samplesC[1][cntv+3] = samples[1][startv+1];
                      samplesC[2][cntv] = samples[2][startv-2];
                      samplesC[2][cntv+1] = samples[2][startv-1];
                      samplesC[2][cntv+2] = samples[2][startv];
                      samplesC[2][cntv+3] = samples[2][startv+1];
                      color_valuesC[0][cntv] = color_values[0][startv-2];
                      color_valuesC[0][cntv+1] = color_values[0][startv-1];
                      color_valuesC[0][cntv+2] = color_values[0][startv];
                      color_valuesC[0][cntv+3] = color_values[0][startv+1];
                      color_valuesC[1][cntv] = color_values[1][startv-2];
                      color_valuesC[1][cntv+1] = color_values[1][startv-1];
                      color_valuesC[1][cntv+2] = color_values[1][startv];
                      color_valuesC[1][cntv+3] = color_values[1][startv+1];
                      color_valuesC[2][cntv] = color_values[2][startv-2];
                      color_valuesC[2][cntv+1] = color_values[2][startv-1];
                      color_valuesC[2][cntv+2] = color_values[2][startv];
                      color_valuesC[2][cntv+3] = color_values[2][startv+1];
                      if (color_valuesC.length == 4) {
                        color_valuesC[3][cntv] = color_values[3][startv-2];
                        color_valuesC[3][cntv+1] = color_values[3][startv-1];
                        color_valuesC[3][cntv+2] = color_values[3][startv];
                        color_valuesC[3][cntv+3] = color_values[3][startv+1];
                      }
                      cntv += 4;
                      startv += lens[0];
                    }
                    Object branchv = shadow_api.makeBranch();
                    buildCurvedTexture(branchv, null, dataUnits, domain_units, default_values, shadow_api,
                                       valueArrayLength, valueToScalar, value_array, color_valuesC, mode,
                                       constant_color, constant_alpha, curved_size, samplesC,
                                       spatial_all_select, renderer, range_select, domainOnlySpatial,
                                       null,
                                       4, leny, lens[0], lens[1]);
                    shadow_api.addToGroup(branch, branchv);
                  }
                  if (i > 0) {  // horz stitch
                    float[][] samplesC = new float[3][4*lenx];
                    byte[][] color_valuesC = new byte[color_values.length][4*lenx];
                    int starth = x_start_stop[j][0] + i_total*lens[0];
                    int cnth = 0;
                    System.arraycopy(samples[0], starth-2*lens[0], samplesC[0], cnth, lenx);
                    System.arraycopy(samples[1], starth-2*lens[0], samplesC[1], cnth, lenx);
                    System.arraycopy(samples[2], starth-2*lens[0], samplesC[2], cnth, lenx);
                    cnth += lenx;
                    System.arraycopy(samples[0], starth-1*lens[0], samplesC[0], cnth, lenx);
                    System.arraycopy(samples[1], starth-1*lens[0], samplesC[1], cnth, lenx);
                    System.arraycopy(samples[2], starth-1*lens[0], samplesC[2], cnth, lenx);
                    cnth += lenx;
                    System.arraycopy(samples[0], starth, samplesC[0], cnth, lenx);
                    System.arraycopy(samples[1], starth, samplesC[1], cnth, lenx);
                    System.arraycopy(samples[2], starth, samplesC[2], cnth, lenx);
                    cnth += lenx;
                    System.arraycopy(samples[0], starth+1*lens[0], samplesC[0], cnth, lenx);
                    System.arraycopy(samples[1], starth+1*lens[0], samplesC[1], cnth, lenx);
                    System.arraycopy(samples[2], starth+1*lens[0], samplesC[2], cnth, lenx);
                                                                                                                                   
                    cnth = 0;
                    System.arraycopy(color_values[0], starth-2*lens[0], color_valuesC[0], cnth, lenx);
                    System.arraycopy(color_values[1], starth-2*lens[0], color_valuesC[1], cnth, lenx);
                    System.arraycopy(color_values[2], starth-2*lens[0], color_valuesC[2], cnth, lenx);
                    if (color_values.length == 4) System.arraycopy(color_values[3], starth-2*lens[0], color_valuesC[3], cnth, lenx);
                    cnth += lenx;
                    System.arraycopy(color_values[0], starth-1*lens[0], color_valuesC[0], cnth, lenx);
                    System.arraycopy(color_values[1], starth-1*lens[0], color_valuesC[1], cnth, lenx);
                    System.arraycopy(color_values[2], starth-1*lens[0], color_valuesC[2], cnth, lenx);
                    if (color_values.length == 4) System.arraycopy(color_values[3], starth-1*lens[0], color_valuesC[3], cnth, lenx);
                    cnth += lenx;
                    System.arraycopy(color_values[0], starth, color_valuesC[0], cnth, lenx);
                    System.arraycopy(color_values[1], starth, color_valuesC[1], cnth, lenx);
                    System.arraycopy(color_values[2], starth, color_valuesC[2], cnth, lenx);
                    if (color_values.length == 4) System.arraycopy(color_values[3], starth, color_valuesC[3], cnth, lenx);
                    cnth += lenx;
                    System.arraycopy(color_values[0], starth+1*lens[0], color_valuesC[0], cnth, lenx);
                    System.arraycopy(color_values[1], starth+1*lens[0], color_valuesC[1], cnth, lenx);
                    System.arraycopy(color_values[2], starth+1*lens[0], color_valuesC[2], cnth, lenx);
                    if (color_values.length == 4) System.arraycopy(color_values[3], starth+1*lens[0], color_valuesC[3], cnth, lenx);
                                                                                                                                   
                                                                                                                                   
                    Object branchh = shadow_api.makeBranch();
                    buildCurvedTexture(branchh, null, dataUnits, domain_units, default_values, shadow_api,
                                       valueArrayLength, valueToScalar, value_array, color_valuesC, mode,
                                       constant_color, constant_alpha, curved_size, samplesC,
                                       spatial_all_select, renderer, range_select, domainOnlySpatial,
                                       null,
                                       lenx, 4, lens[0], lens[1]);

                    shadow_api.addToGroup(branch, branchh);
                  }
                  //- tile piece
                  byte[][] color_valuesW = null;
                  if (color_values.length == 3) color_valuesW = new byte[3][lenx*leny];
                  if (color_values.length == 4) color_valuesW = new byte[4][lenx*leny];

                  int cnt = 0;
                  for (k=0; k<leny; k++) {
                    start = x_start_stop[j][0] + i_total*lens[0] +  k*lens[0];
                    System.arraycopy(color_values[0], start, color_valuesW[0], cnt, lenx);
                    System.arraycopy(color_values[1], start, color_valuesW[1], cnt, lenx);
                    System.arraycopy(color_values[2], start, color_valuesW[2], cnt, lenx);
                    if (color_values.length == 4) System.arraycopy(color_values[3], start, color_valuesW[3], cnt, lenx); 
                    cnt += lenx;
                  }
                  Object branch1 = shadow_api.makeBranch();
                  buildCurvedTexture(branch1, null, dataUnits, domain_units, default_values, shadow_api,
                                     valueArrayLength, valueToScalar, value_array, color_valuesW, mode,
                                     constant_color, constant_alpha, curved_size, samples,
                                     spatial_all_select, renderer, range_select, domainOnlySpatial,
                                     new int[] {x_start_stop[j][0], y_start_stop[i][0]},
                                     lenx, leny, lens[0], lens[1]);

                  shadow_api.addToGroup(branch, branch1);
                }
                i_total += leny;
              }
              shadow_api.addToGroup(group, branch);
            }
//-- end texture split logic
            //System.out.println("curvedTexture done");
// if (link != null) System.out.println("end texture " + (System.currentTimeMillis() - link.start_time));
            return false;
          } // end if (curvedTexture)
          else if (isTexture3D) {
            if (color_values == null) {
              // must be color_values array for texture mapping
              color_values = new byte[3][domain_length];
              for (int i=0; i<domain_length; i++) {
                color_values[0][i] = floatToByte(constant_color[0]);
                color_values[1][i] = floatToByte(constant_color[1]);
                color_values[2][i] = floatToByte(constant_color[2]);
              }
            }
            if (range_select[0] != null && range_select[0].length > 1) {
              int len = range_select[0].length;

/* can be misleading because of the way transparency composites
WLH 15 March 2000 */
              float alpha =
                default_values[display.getDisplayScalarIndex(Display.Alpha)];
              if (constant_alpha == constant_alpha) {
                alpha = 1.0f - constant_alpha;
              }
              if (color_values.length < 4) {
                byte[][] c = new byte[4][];
                c[0] = color_values[0];
                c[1] = color_values[1];
                c[2] = color_values[2];
                c[3] = new byte[len];
                for (int i=0; i<len; i++) c[3][i] = floatToByte(alpha);
                constant_alpha = Float.NaN;
                color_values = c;
              }
              for (int i=0; i<len; i++) {
                if (!range_select[0][i]) {
                  // make missing pixel invisible (transparent)
                  color_values[3][i] = 0;

                  // WLH 15 March 2000
                  // make missing pixel black
                  color_values[0][i] = 0;
                  color_values[1][i] = 0;
                  color_values[2][i] = 0;
                }
              }
/* WLH 15 March 2000 */

/* WLH 15 March 2000
              for (int i=0; i<len; i++) {
                if (!range_select[0][i]) {
                  // make missing pixel black
                  color_values[0][i] = 0;
                  color_values[1][i] = 0;
                  color_values[2][i] = 0;
                }
              }
*/
            } // end if (range_select[0] != null)

            // MEM
            VisADQuadArray[] qarray =
              {new VisADQuadArray(), new VisADQuadArray(), new VisADQuadArray()};
            qarray[0].vertexCount = coordinatesX.length / 3;
            qarray[0].coordinates = coordinatesX;
            qarray[0].texCoords = texCoordsX;
            qarray[0].colors = colorsX;
            qarray[0].normals = normalsX;

            qarray[1].vertexCount = coordinatesY.length / 3;
            qarray[1].coordinates = coordinatesY;
            qarray[1].texCoords = texCoordsY;
            qarray[1].colors = colorsY;
            qarray[1].normals = normalsY;

            qarray[2].vertexCount = coordinatesZ.length / 3;
            qarray[2].coordinates = coordinatesZ;
            qarray[2].texCoords = texCoordsZ;
            qarray[2].colors = colorsZ;
            qarray[2].normals = normalsZ;

            /*
            // WLH 3 June 99 - until Texture3D works on NT (etc)
            if (t3dm = GraphicsModeControl.STACK2D) {
              BufferedImage[][] images = new BufferedImage[3][];
              for (int i=0; i<3; i++) {
                images[i] = createImages(i, data_width, data_height, data_depth,
                                 texture_width, texture_height, texture_depth,
                                 color_values);
              }
              BufferedImage[] imagesX = null;
              BufferedImage[] imagesY = null;
              BufferedImage[] imagesZ = null;
            }
            */

            VisADQuadArray qarrayX = null;
            VisADQuadArray qarrayY = null;
            VisADQuadArray qarrayZ = null;
            for (int i=0; i<3; i++) {
              if (volume_tuple_index[i] == 0) {
                qarrayX = qarray[i];
                //imagesX = images[i];
              }
              else if (volume_tuple_index[i] == 1) {
                qarrayY = qarray[i];
                //imagesY = images[i];
              }
              else if (volume_tuple_index[i] == 2) {
                qarrayZ = qarray[i];
                //imagesZ = images[i];
              }
            }
            VisADQuadArray qarrayXrev = reverse(qarrayX);
            VisADQuadArray qarrayYrev = reverse(qarrayY);
            VisADQuadArray qarrayZrev = reverse(qarrayZ);

            if (t3dm == GraphicsModeControl.STACK2D) {
      // WLH 3 June 99 - comment this out until Texture3D works on NT (etc)
              BufferedImage[][] images = new BufferedImage[3][];
              for (int i=0; i<3; i++) {
                images[i] = createImages(i, data_width, data_height, data_depth,
                                 texture_width, texture_height, texture_depth,
                                 color_values);
              }
              BufferedImage[] imagesX = null;
              BufferedImage[] imagesY = null;
              BufferedImage[] imagesZ = null;
              for (int i=0; i<3; i++) {
                if (volume_tuple_index[i] == 0) {
                  imagesX = images[i];
                }
                else if (volume_tuple_index[i] == 1) {
                  imagesY = images[i];
                }
                else if (volume_tuple_index[i] == 2) {
                  imagesZ = images[i];
                }
              }
              shadow_api.textureStackToGroup(group, qarrayX, qarrayY, qarrayZ,
                                      qarrayXrev, qarrayYrev, qarrayZrev,
                                      imagesX, imagesY, imagesZ,
                                      mode, constant_alpha, constant_color,
                                      texture_width, texture_height, texture_depth,
                                      renderer);
            } else {

              BufferedImage[] images =
                createImages(2, data_width, data_height, data_depth,
                             texture_width, texture_height, texture_depth,
                             color_values);
              shadow_api.texture3DToGroup(group, qarrayX, qarrayY, qarrayZ,
                                          qarrayXrev, qarrayYrev, qarrayZrev,
                                          images, mode, constant_alpha,
                                          constant_color, texture_width,
                                          texture_height, texture_depth, renderer);

            }
            // System.out.println("isTexture3D done");
            return false;
          } // end if (isTexture3D)
          else if (pointMode || spatial_set == null ||
                   spatialManifoldDimension == 0 ||
                   spatialManifoldDimension == 3) {
            if (range_select[0] != null) {
              int len = range_select[0].length;
              if (len == 1 || spatial_values[0].length == 1) {
                return false;
              }
              for (int j=0; j<len; j++) {
                // range_select[0][j] is either 0.0f or Float.NaN -
                // setting to Float.NaN will move points off the screen
                if (!range_select[0][j]) {
                  spatial_values[0][j] = Float.NaN;
                }
              }
              // CTR 13 Oct 1998 - call new makePointGeometry signature
              array = makePointGeometry(spatial_values, color_values, true);
              // System.out.println("makePointGeometry for some missing");
            }
            else {
              array = makePointGeometry(spatial_values, color_values);
              // System.out.println("makePointGeometry for pointMode");
            }
          }
          else if (spatialManifoldDimension == 1) {
            if (range_select[0] != null) {
              // WLH 27 March 2000
              //if (mode.getMissingTransparent()) {
              if (isMissingTransparent) {
                spatial_set.cram_missing(range_select[0]);
                spatial_all_select = false;
              }
              else {
                if (color_values == null) {
                  color_values = new byte[4][domain_length];
                  for (int i=0; i<domain_length; i++) {
                    color_values[0][i] = floatToByte(constant_color[0]);
                    color_values[1][i] = floatToByte(constant_color[1]);
                    color_values[2][i] = floatToByte(constant_color[2]);
                  }
                }
                for (int i=0; i<domain_length; i++) {
                  if (!range_select[0][i]) {
                    color_values[0][i] = 0;
                    color_values[1][i] = 0;
                    color_values[2][i] = 0;
                  }
                }
              }

            }
            array = spatial_set.make1DGeometry(color_values);
            if (array != null) {
              if (!spatial_all_select) array = array.removeMissing();
              if (getAdjustProjectionSeam()) {
                array = array.adjustLongitude(renderer);
                array = array.adjustSeam(renderer);
              }
            }
            // System.out.println("make1DGeometry");
          }
          else if (spatialManifoldDimension == 2) {
            if (range_select[0] != null) {
              // WLH 27 March 2000
              //if (mode.getMissingTransparent()) {
              if (isMissingTransparent) {
                spatial_set.cram_missing(range_select[0]);
                spatial_all_select = false;
              }
              else {
                if (color_values == null) {
                  color_values = new byte[4][domain_length];
                  for (int i=0; i<domain_length; i++) {
                    color_values[0][i] = floatToByte(constant_color[0]);
                    color_values[1][i] = floatToByte(constant_color[1]);
                    color_values[2][i] = floatToByte(constant_color[2]);
                  }
                }
                for (int i=0; i<domain_length; i++) {
                  if (!range_select[0][i]) {
                    color_values[0][i] = 0;
                    color_values[1][i] = 0;
                    color_values[2][i] = 0;
                  }
                }
              }

            }
            array = spatial_set.make2DGeometry(color_values, indexed);
            if (array != null) {
              if (!spatial_all_select) array = array.removeMissing();
              if (getAdjustProjectionSeam()) {
                array = array.adjustLongitude(renderer);
                array = array.adjustSeam(renderer);
              }
            }
            // System.out.println("make2DGeometry  vertexCount = " +
            //                    array.vertexCount);
          }
          else {
            throw new DisplayException("bad spatialManifoldDimension: " +
                                       "ShadowFunctionOrSetType.doTransform");
          }

          if (array != null && array.vertexCount > 0) {
            shadow_api.addToGroup(group, array, mode,
                                  constant_alpha, constant_color);
            // System.out.println("array.makeGeometry");
            //  FREE
            array = null;
/* WLH 25 June 2000
            if (renderer.getIsDirectManipulation()) {
              renderer.setSpatialValues(spatial_values);
            }
*/
          }
        } // end if (!anyContourCreated && !anyFlowCreated &&
          //         !anyTextCreated && !anyShapeCreated)

        // WLH 25 June 2000
        if (renderer.getIsDirectManipulation()) {
          renderer.setSpatialValues(spatial_values);
        }

// if (link != null) System.out.println("end doTransform " + (System.currentTimeMillis() - link.start_time));

        return false;
      } // end if (LevelOfDifficulty == SIMPLE_FIELD)
      else if (LevelOfDifficulty == SIMPLE_ANIMATE_FIELD) {

        Control control = null;
        Object swit = null;
        int index = -1;

        if (DomainComponents.length == 1) {
          RealType real = (RealType) DomainComponents[0].getType();
          for (int i=0; i<valueArrayLength; i++) {
            ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
            float[] values = display_values[i];
            if (values != null && real.equals(map.getScalar())) {
              int displayScalarIndex = valueToScalar[i];
              DisplayRealType dreal =
                display.getDisplayScalar(displayScalarIndex);
              if (dreal.equals(Display.Animation) ||
                  dreal.equals(Display.SelectValue)) {
                swit = shadow_api.makeSwitch();
                index = i;
                control = map.getControl();
                break;
              }
            } // end if (values != null && && real.equals(map.getScalar()))
          } // end for (int i=0; i<valueArrayLength; i++)
        } // end if (DomainComponents.length == 1)

        if (control == null) {
          throw new DisplayException("bad SIMPLE_ANIMATE_FIELD: " +
                                     "ShadowFunctionOrSetType.doTransform");
        }

        for (int i=0; i<domain_length; i++) {
          Object branch = shadow_api.makeBranch();
          if (range_select[0] == null || range_select[0].length == 1 ||
              range_select[0][i]) {
            VisADGeometryArray array = null;

            float[][] sp = new float[3][1];
            if (spatial_values[0].length > 1) {
              sp[0][0] = spatial_values[0][i];
              sp[1][0] = spatial_values[1][i];
              sp[2][0] = spatial_values[2][i];
            }
            else {
              sp[0][0] = spatial_values[0][0];
              sp[1][0] = spatial_values[1][0];
              sp[2][0] = spatial_values[2][0];
            }
            byte[][] co = new byte[3][1];
            if (color_values == null) {
              co[0][0] = floatToByte(constant_color[0]);
              co[1][0] = floatToByte(constant_color[1]);
              co[2][0] = floatToByte(constant_color[2]);
            }
            else if (color_values[0].length > 1) {
              co[0][0] = color_values[0][i];
              co[1][0] = color_values[1][i];
              co[2][0] = color_values[2][i];
            }
            else {
              co[0][0] = color_values[0][0];
              co[1][0] = color_values[1][0];
              co[2][0] = color_values[2][0];
            }
            boolean[][] ra = {{true}};

            boolean anyShapeCreated = false;
            VisADGeometryArray[] arrays =
              shadow_api.assembleShape(display_values, valueArrayLength,
                            valueToMap, MapVector, valueToScalar, display,
                            default_values, inherited_values,
                            sp, co, ra, i, shadow_api);
            if (arrays != null) {
              for (int j=0; j<arrays.length; j++) {
                array = arrays[j];
                shadow_api.addToGroup(branch, array, mode,
                                      constant_alpha, constant_color);
                array = null;
/* why null constant_alpha?
                appearance = makeAppearance(mode, null, constant_color, geometry);
*/
              }
              anyShapeCreated = true;
              arrays = null;
            }

            boolean anyTextCreated = false;
            if (anyText && text_values != null && text_control != null) {
              String[] te = new String[1];
              if (text_values.length > 1) {
                te[0] = text_values[i];
              }
              else {
                te[0] = text_values[0];
              }
              array = shadow_api.makeText(te, text_control, sp, co, ra);
              if (array != null) {
                shadow_api.addTextToGroup(branch, array, mode,
                                          constant_alpha, constant_color);
                array = null;
                anyTextCreated = true;
              }
            }

            boolean anyFlowCreated = false;
            if (anyFlow) {
	      if (flow1_values != null && flow1_values[0] != null) {
                // try Flow1
                float[][] f1 = new float[3][1];
                if (flow1_values[0].length > 1) {
                  f1[0][0] = flow1_values[0][i];
                  f1[1][0] = flow1_values[1][i];
                  f1[2][0] = flow1_values[2][i];
                }
                else {
                  f1[0][0] = flow1_values[0][0];
                  f1[1][0] = flow1_values[1][0];
                  f1[2][0] = flow1_values[2][0];
                }
                arrays = shadow_api.makeFlow(0, f1, flowScale[0], sp, co, ra);
                if (arrays != null) {
                  for (int j=0; j<arrays.length; j++) {
                    if (arrays[j] != null) {
                      shadow_api.addToGroup(branch, arrays[j], mode,
                                            constant_alpha, constant_color);
                      arrays[j] = null;
                    }
                  }
                }
                anyFlowCreated = true;
              }
    
              // try Flow2
              if (flow2_values != null && flow2_values[0] != null) {
                float[][] f2 = new float[3][1];
                if (flow2_values[0].length > 1) {
                  f2[0][0] = flow2_values[0][i];
                  f2[1][0] = flow2_values[1][i];
                  f2[2][0] = flow2_values[2][i];
                }
                else {
                  f2[0][0] = flow2_values[0][0];
                  f2[1][0] = flow2_values[1][0];
                  f2[2][0] = flow2_values[2][0];
                }
                arrays = shadow_api.makeFlow(1, f2, flowScale[1], sp, co, ra);
                if (arrays != null) {
                  for (int j=0; j<arrays.length; j++) {
                    if (arrays[j] != null) {
                      shadow_api.addToGroup(branch, arrays[j], mode,
                                            constant_alpha, constant_color);
                      arrays[j] = null;
                    }
                  }
                }
                anyFlowCreated = true;
              }
            }

            if (!anyShapeCreated && !anyTextCreated &&
                !anyFlowCreated) {
              array = new VisADPointArray();
              array.vertexCount = 1;
              coordinates = new float[3];
              coordinates[0] = sp[0][0];
              coordinates[1] = sp[1][0];
              coordinates[2] = sp[2][0];
              array.coordinates = coordinates;
              if (color_values != null) {
                colors = new byte[3];
                colors[0] = co[0][0];
                colors[1] = co[1][0];
                colors[2] = co[2][0];
                array.colors = colors;
              }
              shadow_api.addToGroup(branch, array, mode,
                                    constant_alpha, constant_color);
              array = null;
              // System.out.println("addChild " + i + " of " + domain_length);
            }
          }
          else { // if (range_select[0][i])
/* WLH 18 Aug 98
   empty BranchGroup or Shape3D may cause NullPointerException
   from Shape3DRetained.setLive
            // add null BranchGroup as child to maintain order
            branch.addChild(new Shape3D());
*/
            // System.out.println("addChild " + i + " of " + domain_length +
            //                    " MISSING");
          }
          shadow_api.addToSwitch(swit, branch);
        } // end for (int i=0; i<domain_length; i++)

        shadow_api.addSwitch(group, swit, control, domain_set, renderer);
        return false;
      }  // end if (LevelOfDifficulty == SIMPLE_ANIMATE_FIELD)
      else { // must be LevelOfDifficulty == LEGAL
        // add values to value_array according to SelectedMapVector-s
        // of RealType-s in Domain (including Reference) and Range
        //
        // accumulate Vector of value_array-s at this ShadowType,
        // to be rendered in a post-process to scanning data
        //
        // ** OR JUST EACH FIELD INDEPENDENTLY **
        //
/*
        return true;
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowFunctionOrSetType.doTransform");
      }
    }
    else { // !isTerminal
      // domain_values and range_values, as well as their References,
      // already converted to default Units and added to display_values

      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in Domain (including Reference), and
      // recursively call doTransform on Range values

      //
      // TO_DO
      // SelectRange (use boolean[][] range_select from assembleSelect),
      //   SelectValue, Animation
      // DataRenderer.isTransformControl temporary hack:
      // SelectRange.isTransform,
      // !SelectValue.isTransform, !Animation.isTransform
      //
      // may need to split ShadowType.checkAnimationOrValue
      // Display.Animation has no range, is single
      // Display.Value has no range, is not single
      //
      // see Set.merge1DSets

      boolean post = false;

      Control control = null;
      Object swit = null;
      int index = -1;

      if (DomainComponents.length == 1) {
        RealType real = (RealType) DomainComponents[0].getType();
        for (int i=0; i<valueArrayLength; i++) {
          ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
          float[] values = display_values[i];
          if (values != null && real.equals(map.getScalar())) {
            int displayScalarIndex = valueToScalar[i];
            DisplayRealType dreal =
              display.getDisplayScalar(displayScalarIndex);
            if (dreal.equals(Display.Animation) ||
                dreal.equals(Display.SelectValue)) {
              swit = shadow_api.makeSwitch();
              index = i;
              control = map.getControl();
              break;
            }
          } // end if (values != null && real.equals(map.getScalar()))
        } // end for (int i=0; i<valueArrayLength; i++)
      } // end if (DomainComponents.length == 1)

      if (control != null) {
        shadow_api.addSwitch(group, swit, control, domain_set, renderer);
/*
        group.addChild(swit);
        control.addPair(swit, domain_set, renderer);
*/
      }

      float[] range_value_array = new float[valueArrayLength];
      for (int j=0; j<display.getValueArrayLength(); j++) {
        range_value_array[j] = Float.NaN;
      }
      for (int i=0; i<domain_length; i++) {
        if (range_select[0] == null || range_select[0].length == 1 ||
            range_select[0][i]) {
          if (text_values != null && text_control != null) {
            shadow_api.setText(text_values[i], text_control);
          }
          else {
            shadow_api.setText(null, null);
          }
          for (int j=0; j<valueArrayLength; j++) {
            if (display_values[j] != null) {
              if (display_values[j].length == 1) {
                range_value_array[j] = display_values[j][0];
              }
              else {
                range_value_array[j] = display_values[j][i];
              }
            }
          }

          // push lat_index and lon_index for flow navigation
          int[] lat_lon_indices = renderer.getLatLonIndices();
          if (control != null) {
            Object branch = shadow_api.makeBranch();
            post |= shadow_api.recurseRange(branch, ((Field) data).getSample(i),
                                             range_value_array, default_values,
                                             renderer);
            shadow_api.addToSwitch(swit, branch);
            // System.out.println("addChild " + i + " of " + domain_length);
          }
          else {
            Object branch = shadow_api.makeBranch();
            post |= shadow_api.recurseRange(branch, ((Field) data).getSample(i),
                                             range_value_array, default_values,
                                             renderer);
            shadow_api.addToGroup(group, branch);
          }
          // pop lat_index and lon_index for flow navigation
          renderer.setLatLonIndices(lat_lon_indices);

        }
        else { // if (!range_select[0][i])
          if (control != null) {
            // add null BranchGroup as child to maintain order
            Object branch = shadow_api.makeBranch();
            shadow_api.addToSwitch(swit, branch);
            // System.out.println("addChild " + i + " of " + domain_length +
            //                    " MISSING");
          }
        }
      }

/* why later than addPair & addChild(swit) ??
      if (control != null) {
        // initialize swit child selection
        control.init();
      }
*/

      return post;
/*
      throw new UnimplementedException("ShadowFunctionOrSetType.doTransform: " +
                                       "not terminal");
*/
    } // end if (!isTerminal)
  }

  public BufferedImage createImage(int data_width, int data_height,
                       int texture_width, int texture_height,
                       byte[][] color_values) throws VisADException {
    return createImage(data_width, data_height,
      texture_width, texture_height, color_values, false);
  }

  public BufferedImage createImage(int data_width, int data_height,
                       int texture_width, int texture_height,
                       byte[][] color_values, boolean byRef)
                       throws VisADException {
    if (byRef) {
      if (data_width > texture_width || data_height > texture_height) {
        throw new VisADException(
          "Data dimensions cannot exceed texture dimensions");
      }
      int size = texture_width * texture_height;
      if (data_width != texture_width || data_height != texture_height) {
        // expand color_values array to match texture size
        byte[][] new_color_values =
          new byte[color_values.length][size];
        for (int c=0; c<color_values.length; c++) {
          for (int h=0; h<data_height; h++) {
            System.arraycopy(color_values[c], data_width * h,
              new_color_values[c], texture_width * h, data_width);
          }
        }
        color_values = new_color_values;
      }

      // CTR 17 Jan 2006 - create BufferedImage with TYPE_CUSTOM of the form
      // "TYPE_3BYTE_RGB" or "TYPE_4BYTE_RGBA", since those types can work with
      // Java3D texturing by reference
      ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      ColorModel colorModel = new ComponentColorModel(colorSpace,
        color_values.length > 3, false, ColorModel.TRANSLUCENT,
        DataBuffer.TYPE_BYTE);
      SampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_BYTE,
        texture_width, texture_height, color_values.length);
      DataBuffer buffer = new DataBufferByte(color_values, size);
      WritableRaster raster =
        Raster.createWritableRaster(sampleModel, buffer, null);
      return new BufferedImage(colorModel, raster, false, null);
    }

    BufferedImage image = null;
    if (color_values.length > 3) {
      ColorModel colorModel = ColorModel.getRGBdefault();
      WritableRaster raster =
        colorModel.createCompatibleWritableRaster(texture_width, texture_height);
      DataBuffer db = raster.getDataBuffer();
      if (!(db instanceof DataBufferInt)) {
        throw new UnimplementedException("getRGBdefault isn't DataBufferInt");
      }
      image = new BufferedImage(colorModel, raster, false, null);
      int[] intData = ((DataBufferInt)db).getData();
      int k = 0;
      int m = 0;
      int r, g, b, a;
      for (int j=0; j<data_height; j++) {
        for (int i=0; i<data_width; i++) {
          r = (color_values[0][k] < 0) ? color_values[0][k] + 256 :
                                         color_values[0][k];
          g = (color_values[1][k] < 0) ? color_values[1][k] + 256 :
                                         color_values[1][k];
          b = (color_values[2][k] < 0) ? color_values[2][k] + 256 :
                                         color_values[2][k];
          a = (color_values[3][k] < 0) ? color_values[3][k] + 256 :
                                         color_values[3][k];
          intData[m++] = ((a << 24) | (r << 16) | (g << 8) | b);
          k++;
        }
        for (int i=data_width; i<texture_width; i++) {
          intData[m++] = 0;
        }
      }
      for (int j=data_height; j<texture_height; j++) {
        for (int i=0; i<texture_width; i++) {
          intData[m++] = 0;
        }
      }
    }
    else { // (color_values.length == 3)
      ColorModel colorModel = ColorModel.getRGBdefault();
      WritableRaster raster =
        colorModel.createCompatibleWritableRaster(texture_width, texture_height);

      // WLH 2 Nov 2000
      DataBuffer db = raster.getDataBuffer();
      int[] intData = null;
      if (db instanceof DataBufferInt) {
        intData = ((DataBufferInt)db).getData();
        image = new BufferedImage(colorModel, raster, false, null);
      }
      else {
// System.out.println("byteData 3 1");
        image = new BufferedImage(texture_width, texture_height,
                                  BufferedImage.TYPE_INT_RGB);
        intData = new int[texture_width * texture_height];
/*
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB); 
        int[] nBits = {8, 8, 8};
        colorModel =
          new ComponentColorModel(cs, nBits, false, false, Transparency.OPAQUE, 0); 
        raster = 
          colorModel.createCompatibleWritableRaster(texture_width, texture_height);
*/
      }

      // image = new BufferedImage(colorModel, raster, false, null);
      // int[] intData = ((DataBufferInt)raster.getDataBuffer()).getData();
      int k = 0;
      int m = 0;
      int r, g, b, a;
      for (int j=0; j<data_height; j++) {
        for (int i=0; i<data_width; i++) {
          r = (color_values[0][k] < 0) ? color_values[0][k] + 256 :
                                         color_values[0][k];
          g = (color_values[1][k] < 0) ? color_values[1][k] + 256 :
                                         color_values[1][k];
          b = (color_values[2][k] < 0) ? color_values[2][k] + 256 :
                                         color_values[2][k];
          a = 255;
          intData[m++] = ((a << 24) | (r << 16) | (g << 8) | b);
          k++;
        }
        for (int i=data_width; i<texture_width; i++) {
          intData[m++] = 0;
        }
      }
      for (int j=data_height; j<texture_height; j++) {
        for (int i=0; i<texture_width; i++) {
          intData[m++] = 0;
        }
      }

      // WLH 2 Nov 2000
      if (!(db instanceof DataBufferInt)) {
// System.out.println("byteData 3 2");
        image.setRGB(0, 0, texture_width, texture_height, intData, 0, texture_width);
/*
        byte[] byteData = ((DataBufferByte)raster.getDataBuffer()).getData();
        k = 0;
        for (int i=0; i<intData.length; i++) {
          byteData[k++] = (byte) (intData[i] & 255);
          byteData[k++] = (byte) ((intData[i] >> 8) & 255);
          byteData[k++] = (byte) ((intData[i] >> 16) & 255);
        }
*/
/* WLH 4 Nov 2000, from com.sun.j3d.utils.geometry.Text2D
        // For now, jdk 1.2 only handles ARGB format, not the RGBA we want
        BufferedImage bImage = new BufferedImage(width, height,
                                                 BufferedImage.TYPE_INT_ARGB);
        Graphics offscreenGraphics = bImage.createGraphics();

        // First, erase the background to the text panel - set alpha to 0
        Color myFill = new Color(0f, 0f, 0f, 0f);
        offscreenGraphics.setColor(myFill);
        offscreenGraphics.fillRect(0, 0, width, height);

        // Next, set desired text properties (font, color) and draw String
        offscreenGraphics.setFont(font);
        Color myTextColor = new Color(color.x, color.y, color.z, 1f);
        offscreenGraphics.setColor(myTextColor);
        offscreenGraphics.drawString(text, 0, height - descent);
*/
      } // end if (!(db instanceof DataBufferInt))

    } // end if (color_values.length == 3)
    return image;
  }

  public BufferedImage[] createImages(int axis, int data_width_in,
           int data_height_in, int data_depth_in, int texture_width_in,
           int texture_height_in, int texture_depth_in, byte[][] color_values)
         throws VisADException {
    int data_width, data_height, data_depth;
    int texture_width, texture_height, texture_depth;
    int kwidth, kheight, kdepth;
    if (axis == 2) {
      kwidth = 1;
      kheight = data_width_in;
      kdepth = data_width_in * data_height_in;
      data_width = data_width_in;
      data_height = data_height_in;
      data_depth = data_depth_in;
      texture_width = texture_width_in;
      texture_height = texture_height_in;
      texture_depth = texture_depth_in;

    }
    else if (axis == 1) {
      kwidth = 1;
      kdepth = data_width_in;
      kheight = data_width_in * data_height_in;
      data_width = data_width_in;
      data_depth = data_height_in;
      data_height = data_depth_in;
      texture_width = texture_width_in;
      texture_depth = texture_height_in;
      texture_height = texture_depth_in;
    }
    else if (axis == 0) {
      kdepth = 1;
      kwidth = data_width_in;
      kheight = data_width_in * data_height_in;
      data_depth = data_width_in;
      data_width = data_height_in;
      data_height = data_depth_in;
      texture_depth = texture_width_in;
      texture_width = texture_height_in;
      texture_height = texture_depth_in;
    }
    else {
      return null;
    }
    BufferedImage[] images = new BufferedImage[texture_depth];
    for (int d=0; d<data_depth; d++) {
      if (color_values.length > 3) {
        ColorModel colorModel = ColorModel.getRGBdefault();
        WritableRaster raster =
          colorModel.createCompatibleWritableRaster(texture_width, texture_height);

        images[d] = new BufferedImage(colorModel, raster, false, null);
/* WLH 23 Feb 2000
        if (axis == 1) {
          images[(data_depth-1) - d] =
            new BufferedImage(colorModel, raster, false, null);
        }
        else {
          images[d] = new BufferedImage(colorModel, raster, false, null);
        }
*/
        DataBuffer db = raster.getDataBuffer();
        if (!(db instanceof DataBufferInt)) {
          throw new UnimplementedException("getRGBdefault isn't DataBufferInt");
        }
        int[] intData = ((DataBufferInt)db).getData();
        // int k = d * data_width * data_height;
        int kk = d * kdepth;
        int m = 0;
        int r, g, b, a;
        for (int j=0; j<data_height; j++) {
          int k = kk + j * kheight;
          for (int i=0; i<data_width; i++) {
            r = (color_values[0][k] < 0) ? color_values[0][k] + 256 :
                                           color_values[0][k];
            g = (color_values[1][k] < 0) ? color_values[1][k] + 256 :
                                           color_values[1][k];
            b = (color_values[2][k] < 0) ? color_values[2][k] + 256 :
                                           color_values[2][k];
            a = (color_values[3][k] < 0) ? color_values[3][k] + 256 :
                                           color_values[3][k];
            intData[m++] = ((a << 24) | (r << 16) | (g << 8) | b);
            // k++;
            k += kwidth;
          }
          for (int i=data_width; i<texture_width; i++) {
            intData[m++] = 0;
          }
        }
        for (int j=data_height; j<texture_height; j++) {
          for (int i=0; i<texture_width; i++) {
            intData[m++] = 0;
          }
        }
      }
      else { // (color_values.length == 3)
        ColorModel colorModel = ColorModel.getRGBdefault();
        WritableRaster raster =
          colorModel.createCompatibleWritableRaster(texture_width, texture_height);
        images[d] = new BufferedImage(colorModel, raster, false, null);
/* WLH 23 Feb 2000
        if (axis == 1) {
          images[(data_depth-1) - d] =
            new BufferedImage(colorModel, raster, false, null);
        }
        else {
          images[d] = new BufferedImage(colorModel, raster, false, null);
        }
*/
        DataBuffer db = raster.getDataBuffer();
        if (!(db instanceof DataBufferInt)) {
          throw new UnimplementedException("getRGBdefault isn't DataBufferInt");
        }
        int[] intData = ((DataBufferInt)db).getData();

        // int k = d * data_width * data_height;
        int kk = d * kdepth;
        int m = 0;
        int r, g, b, a;
        for (int j=0; j<data_height; j++) {
          int k = kk + j * kheight;
          for (int i=0; i<data_width; i++) {
            r = (color_values[0][k] < 0) ? color_values[0][k] + 256 :
                                           color_values[0][k];
            g = (color_values[1][k] < 0) ? color_values[1][k] + 256 :
                                           color_values[1][k];
            b = (color_values[2][k] < 0) ? color_values[2][k] + 256 :
                                           color_values[2][k];
            a = 255;
            intData[m++] = ((a << 24) | (r << 16) | (g << 8) | b);
            // k++;
            k += kwidth;
          }
          for (int i=data_width; i<texture_width; i++) {
            intData[m++] = 0;
          }
        }
        for (int j=data_height; j<texture_height; j++) {
          for (int i=0; i<texture_width; i++) {
            intData[m++] = 0;
          }
        }
      } // end if (color_values.length == 3)
    } // end for (int d=0; d<data_depth; d++)
    for (int d=data_depth; d<texture_depth; d++) {
      ColorModel colorModel = ColorModel.getRGBdefault();
      WritableRaster raster =
        colorModel.createCompatibleWritableRaster(texture_width, texture_height);
      images[d] = new BufferedImage(colorModel, raster, false, null);
      DataBuffer db = raster.getDataBuffer();
      if (!(db instanceof DataBufferInt)) {
        throw new UnimplementedException("getRGBdefault isn't DataBufferInt");
      }
      int[] intData = ((DataBufferInt)db).getData();

      for (int i=0; i<texture_width*texture_height; i++) {
        intData[i] = 0;
      }
    }
    return images;
  }

  public VisADQuadArray reverse(VisADQuadArray array) {
    VisADQuadArray qarray = new VisADQuadArray();
    qarray.coordinates = new float[array.coordinates.length];
    qarray.texCoords = new float[array.texCoords.length];
    qarray.colors = new byte[array.colors.length];
    qarray.normals = new float[array.normals.length];

    int count = array.vertexCount;
    qarray.vertexCount = count;
    int color_length = array.colors.length / count;
    int tex_length = array.texCoords.length / count;
    int i3 = 0;
    int k3 = 3 * (count - 1);
    int ic = 0;
    int kc = color_length * (count - 1);
    int it = 0;
    int kt = tex_length * (count - 1);
    for (int i=0; i<count; i++) {
      qarray.coordinates[i3] = array.coordinates[k3];
      qarray.coordinates[i3 + 1] = array.coordinates[k3 + 1];
      qarray.coordinates[i3 + 2] = array.coordinates[k3 + 2];
      qarray.texCoords[it] = array.texCoords[kt];
      qarray.texCoords[it + 1] = array.texCoords[kt + 1];
      if (tex_length == 3) qarray.texCoords[it + 2] = array.texCoords[kt + 2];
      qarray.normals[i3] = array.normals[k3];
      qarray.normals[i3 + 1] = array.normals[k3 + 1];
      qarray.normals[i3 + 2] = array.normals[k3 + 2];
      qarray.colors[ic] = array.colors[kc];
      qarray.colors[ic + 1] = array.colors[kc + 1];
      qarray.colors[ic + 2] = array.colors[kc + 2];
      if (color_length == 4) qarray.colors[ic + 3] = array.colors[kc + 3];
      i3 += 3;
      k3 -= 3;
      ic += color_length;
      kc -= color_length;
      it += tex_length;
      kt -= tex_length;
    }
    return qarray;
  }

  private void buildLinearTexture(Object group, Set domain_set, Unit[] dataUnits, Unit[] domain_units,
                                  float[] default_values, ShadowType shadow_api, int valueArrayLength,
                                  int[] valueToScalar, float[] value_array, byte[][] color_values,
                                  GraphicsModeControl mode, float[] constant_color, float constant_alpha) 
          throws VisADException {
    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    byte[] colors = null;
    int data_width = 0;
    int data_height = 0;
    int data_depth = 0;
    int texture_width = 1;
    int texture_height = 1;
    int texture_depth = 1;
                                                                                                                                
// if (link != null) System.out.println("test isTextureMap " + (System.currentTimeMillis() - link.start_time));
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
      data_width  = X.getLength();
      data_height = Y.getLength();
      texture_width = shadow_api.textureWidth(data_width);
      texture_height = shadow_api.textureHeight(data_height);
                                                                                                                                
      // WLH 27 Jan 2003
      float half_width = 0.5f / ((float) (data_width - 1));
      float half_height = 0.5f / ((float) (data_height - 1));
      half_width  = (limits[0][1] - limits[0][0]) * half_width;
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
      for (int i=0; i<DomainComponents.length; i++) {
        Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
        while (maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          DisplayRealType real = map.getDisplayScalar();
          DisplayTupleType tuple = real.getTuple();
          if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
            // scale values
            limits[i] = map.scaleValues(limits[i]);
            // get spatial index
            tuple_index[i] = real.getTupleIndex();
            break;
          }
        }
/*
        if (tuple == null ||
            !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
          throw new DisplayException("texture with bad tuple: " +
                                     "ShadowFunctionOrSetType.doTransform");
        }
        if (maps.hasMoreElements()) {
          throw new DisplayException("texture with multiple spatial: " +
                                     "ShadowFunctionOrSetType.doTransform");
        }
*/
      } // end for (int i=0; i<DomainComponents.length; i++)
      // get spatial index not mapped from domain_set
      tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
      DisplayRealType real = (DisplayRealType)
        Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
      int value2_index = display.getDisplayScalarIndex(real);
      float value2 = default_values[value2_index];
      // float value2 = 0.0f;  WLH 30 Aug 99
      for (int i=0; i<valueArrayLength; i++) {
        if (inherited_values[i] > 0 &&
            real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
          value2 = value_array[i];
          break;
        }
      }
      coordinates = new float[12];
      // corner 0
      coordinates[tuple_index[0]] = limits[0][0];
      coordinates[tuple_index[1]] = limits[1][0];
      coordinates[tuple_index[2]] = value2;
      // corner 1
      coordinates[3 + tuple_index[0]] = limits[0][1];
      coordinates[3 + tuple_index[1]] = limits[1][0];
      coordinates[3 + tuple_index[2]] = value2;
      // corner 2
      coordinates[6 + tuple_index[0]] = limits[0][1];
      coordinates[6 + tuple_index[1]] = limits[1][1];
      coordinates[6 + tuple_index[2]] = value2;
      // corner 3
      coordinates[9 + tuple_index[0]] = limits[0][0];
      coordinates[9 + tuple_index[1]] = limits[1][1];
      coordinates[9 + tuple_index[2]] = value2;
                                                                                                                                
      // move image back in Java3D 2-D mode
      shadow_api.adjustZ(coordinates);
                                                                                                                                
      texCoords = new float[8];
      float ratiow = ((float) data_width) / ((float) texture_width);
      float ratioh = ((float) data_height) / ((float) texture_height);
      shadow_api.setTexCoords(texCoords, ratiow, ratioh);
                                                                                                                                
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
                                                                                                                                    
      BufferedImage image =
         createImage(data_width, data_height, texture_width,
                     texture_height, color_values);
      shadow_api.textureToGroup(group, qarray, image, mode,
                                constant_alpha, constant_color,
                                texture_width, texture_height);

  }

  private void buildCurvedTexture(Object group, Set domain_set, Unit[] dataUnits, Unit[] domain_units,
                                  float[] default_values, ShadowType shadow_api, int valueArrayLength,
                                  int[] valueToScalar, float[] value_array, byte[][] color_values,
                                  GraphicsModeControl mode, float[] constant_color, float constant_alpha,
                                  int curved_size, float[][] spatial_values, boolean spatial_all_select,
                                  DataRenderer renderer, boolean[][] range_select, boolean domainOnlySpatial, 
                                  int[] start, int lenX, int lenY, int bigX, int bigY)
          throws VisADException 
  {
//System.out.println("ShadowFunctionOrSetType, start Curved texture:  " + System.currentTimeMillis());
      float[] coordinates = null;
      float[] texCoords = null;
      float[] normals = null;
      byte[] colors = null;
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
                                                                                                                                  
      data_width  = lengths[0];
      data_height = lengths[1];
      // texture sizes must be powers of two
      texture_width  = shadow_api.textureWidth(data_width);
      texture_height = shadow_api.textureHeight(data_height);
                                                                                                                                  
      // compute size of triangle array to mapped texture onto
      int size = (data_width + data_height) / 2;
      curved_size = Math.max(2, Math.min(curved_size, size / 32));
                                                                                                                                  
      int nwidth = 2 + (data_width - 1) / curved_size;
      int nheight = 2 + (data_height - 1) / curved_size;

      // WLH 14 Aug 2001
      if (range_select[0] != null && !domainOnlySpatial) {
      // System.out.println("force curved_size = 1");
        curved_size = 1;
        nwidth = data_width;
        nheight = data_height;
      }

// System.out.println("curved_size = " + curved_size + " nwidth = " + nwidth +
//                    " nheight = " + nheight);
                                                                                                                                               
      int nn = nwidth * nheight;
      coordinates = new float[3 * nn];
      int k = 0;
      int[] is = new int[nwidth];
      int[] js = new int[nheight];
      for (int i=0; i<nwidth; i++) {
         is[i] = Math.min(i * curved_size, data_width - 1);
      }
      for (int j=0; j<nheight; j++) {
         js[j] = Math.min(j * curved_size, data_height - 1);
      }
      //if (domain_set != null) {
      if (start == null) {
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
              int ij = is[i] + data_width * js[j];
              coordinates[k++] = spatial_values[0][ij];
              coordinates[k++] = spatial_values[1][ij];
              coordinates[k++] = spatial_values[2][ij];
/*
double size = Math.sqrt(spatial_values[0][ij] * spatial_values[0][ij] +
                        spatial_values[1][ij] * spatial_values[1][ij] +
                        spatial_values[2][ij] * spatial_values[2][ij]);
if (size < 0.2) {
  System.out.println("spatial_values " + is[i] + " " + js[j] + " " +
  spatial_values[0][ij] + " " + spatial_values[1][ij] + " " + spatial_values[2][ij]);
}
*/
          }
        }
      }
      else {
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
             int ij = is[i] + data_width * js[j];
             int x = ij % lenX;
             int y = ij / lenX;
             ij    = (start[0] + x) + (start[1] + y)*bigX;
             coordinates[k++] = spatial_values[0][ij];
             coordinates[k++] = spatial_values[1][ij];
             coordinates[k++] = spatial_values[2][ij];
          }
        }
      }

      normals = Gridded3DSet.makeNormals(coordinates, nwidth, nheight);
      colors = new byte[3 * nn];
      for (int i=0; i<3*nn; i++) colors[i] = (byte) 127;
                                                                                                                                             
      float ratiow = ((float) data_width) / ((float) texture_width);
      float ratioh = ((float) data_height) / ((float) texture_height);
                                                                                                                                             
      // WLH 27 Jan 2003
      float half_width = 0.5f / ((float) texture_width);
      float half_height = 0.5f / ((float) texture_height);
      float width = 1.0f / ((float) texture_width);
      float height = 1.0f / ((float) texture_height);
                                                                                                                                             
      int mt = 0;
      texCoords = new float[2 * nn];
// System.out.println("nwidth = " + nwidth + " nheight = " + nheight +
//                    " ratiow = " + ratiow + " ratioh = " + ratioh);
      for (int j=0; j<nheight; j++) {
         for (int i=0; i<nwidth; i++) {
/* WLH 27 Jan 2003
                texCoords[mt++] = ratiow * is[i] / (data_width - 1.0f);
                texCoords[mt++] = 1.0f - ratioh * js[j] / (data_height - 1.0f);
*/
            // WLH 27 Jan 2003
            float isfactor = is[i] / (data_width - 1.0f);
            float jsfactor = js[j] / (data_height - 1.0f);
            texCoords[mt++] = (ratiow - width) * isfactor + half_width;
            texCoords[mt++] = 1.0f - (ratioh - height) * jsfactor - half_height;
// System.out.println("texCoords = " + texCoords[mt-2] + " " + texCoords[mt-1] +
//                    " isfactor = " + isfactor + " jsfactor = " + jsfactor);
         }
      }

      VisADTriangleStripArray tarray = new VisADTriangleStripArray();
      tarray.stripVertexCounts = new int[nheight - 1];
      for (int i=0; i<nheight - 1; i++) {
         tarray.stripVertexCounts[i] = 2 * nwidth;
      }
      int len = (nheight - 1) * (2 * nwidth);
      tarray.vertexCount = len;
      tarray.normals = new float[3 * len];
      tarray.coordinates = new float[3 * len];
      tarray.colors = new byte[3 * len];
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

            tarray.colors[k] = colors[m];
            tarray.colors[k+1] = colors[m+1];
            tarray.colors[k+2] = colors[m+2];
            tarray.colors[k+3] = colors[m+nwidth3];
            tarray.colors[k+4] = colors[m+nwidth3+1];
            tarray.colors[k+5] = colors[m+nwidth3+2];

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

      if (!spatial_all_select) {
        tarray = (VisADTriangleStripArray) tarray.removeMissing();
      }

      if (getAdjustProjectionSeam()) {
        tarray = (VisADTriangleStripArray) tarray.adjustLongitude(renderer);
        tarray = (VisADTriangleStripArray) tarray.adjustSeam(renderer);
      }

      BufferedImage image =
         createImage(data_width, data_height, texture_width,
                     texture_height, color_values);

      shadow_api.textureToGroup(group, tarray, image, mode,
                                constant_alpha, constant_color,
                                texture_width, texture_height);
  }

}

