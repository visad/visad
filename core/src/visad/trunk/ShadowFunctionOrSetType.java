
//
// ShadowFunctionOrSetType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/


/*
MEM - memory use reduction strategy:

check out Java WorkShop http://www.sun.com/workshop/
check out http://www.optimizeit.com/

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

import java.util.*;
import java.rmi.*;
import java.awt.image.BufferedImage;

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

      if (this instanceof ShadowFunctionType) {
        // test for texture mapping
        isTextureMap = !getMultipleDisplayScalar() &&
                       getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD &&
                       ((FunctionType) getType()).getReal() &&
                       Domain.getDimension() == 2 &&
                       Domain.getAllSpatial() &&
                       !Domain.getSpatialReference() &&
                       Display.DisplaySpatialCartesianTuple.equals(
                               Domain.getDisplaySpatialTuple() ) &&
                       // checkColorOrAlpha(Range.getDisplayIndices()) &&
                       checkColor(Range.getDisplayIndices()) &&
                       checkAny(Range.getDisplayIndices()) &&
                       display.getGraphicsModeControl().getTextureEnable();
/*
System.out.println("isTextureMap = " + isTextureMap + " " +
                   !getMultipleDisplayScalar() + " " +
                   (getLevelOfDifficulty() == ShadowType.SIMPLE_FIELD) + " " +
                   ((FunctionType) getType()).getReal() + " " +
                   (Domain.getDimension() == 2) + " " +
                   Domain.getAllSpatial() + " " +
                   !Domain.getSpatialReference() + " " +
                   Display.DisplaySpatialCartesianTuple.equals(
                               Domain.getDisplaySpatialTuple() ) + " " +
                   // checkColorOrAlpha(Range.getDisplayIndices()) + " " +
                   checkColor(Range.getDisplayIndices()) + " " +
                   checkAny(Range.getDisplayIndices()));
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

    boolean indexed = wantIndexed();

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
/* not needed - over-write inherited_values?  need to copy?
    int[] inherited_values =
      ((ShadowFunctionOrSetType) adaptedShadowType).getInheritedValues();
*/
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
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length;
    try {
      domain_length = domain_set.getLength();
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
                           default_values[alpha_index] > 0.99 &&
                           (domain_set instanceof Linear2DSet ||
                            (domain_set instanceof LinearNDSet &&
                             domain_set.getDimension() == 2));
/*
System.out.println("isTextureMap = " + isTextureMap + " " +
                   getIsTextureMap() + " " +
                   (default_values[alpha_index] > 0.99) + " " +
                   (domain_set instanceof Linear2DSet) + " " +
                   (domain_set instanceof LinearNDSet) + " " +
                   (domain_set.getDimension() == 2));
*/
    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    byte[] colors = null;
    int data_width = 0;
    int data_height = 0;
    int texture_width = 1;
    int texture_height = 1;
    if (isTextureMap) {
      if (!renderer.isLegalTextureMap()) {
        throw new DisplayException("illegal DataRenderer for texture map: " +
                                   "ShadowFunctionOrSetType.doTransform");
      }
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

      float value2 = 0.0f;
      // convert values to default units (used in display)
      limits = Unit.convertTuple(limits, dataUnits, domain_units);

      // get domain_set sizes
      data_width = X.getLength();
      data_height = Y.getLength();
      texture_width = shadow_api.textureWidth(data_width);
      texture_height = shadow_api.textureHeight(data_height);

      int[] tuple_index = new int[3];
      if (DomainComponents.length != 2) {
        throw new DisplayException("texture domain dimension != 2:" +
                                   "ShadowFunctionOrSetType.doTransform");
      }
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
/*
for (int i=0; i < 4; i++) {
  System.out.println("i = " + i + " texCoords = " + texCoords[2 * i] + " " +
                     texCoords[2 * i + 1]);
  System.out.println(" coordinates = " + coordinates[3 * i] + " " +
                     coordinates[3 * i + 1] + " " + coordinates[3 * i + 2]);
  System.out.println(" normals = " + normals[3 * i]  + " " + normals[3 * i + 1] +
                     " " + normals[3 * i + 2]);
}
*/
    }
    else { // !isTextureMap
      // get values from Function Domain
      // NOTE - may defer this until needed, if needed
      domain_values = domain_set.getSamples(false);

      // convert values to default units (used in display)
      // MEM & FREE
      domain_values = Unit.convertTuple(domain_values, dataUnits, domain_units);
      // System.out.println("got domain_values: domain_length = " + domain_length);

      // map domain_values to appropriate DisplayRealType-s
      // MEM
      mapValues(display_values, domain_values, DomainComponents);
   
      // System.out.println("mapped domain_values");
  
      ShadowRealTupleType domain_reference = Domain.getReference();

/*
      System.out.println("domain_reference = " + domain_reference);
      if (domain_reference != null) {
        System.out.println("getMappedDisplayScalar = " +
                           domain_reference.getMappedDisplayScalar());
      }
*/
      if (domain_reference != null && domain_reference.getMappedDisplayScalar()) {
        // apply coordinate transform to domain values
        RealTupleType ref = (RealTupleType) domain_reference.getType();
        // MEM
        float[][] reference_values =
          CoordinateSystem.transformCoordinates(
            ref, null, ref.getDefaultUnits(), null,
            (RealTupleType) Domain.getType(), dataCoordinateSystem,
            domain_units, null, domain_values);
 
        //
        // TO_DO
        // adjust any RealVectorTypes in range
        // see FlatField.resample and FieldImpl.resample
        //
  
        // map reference_values to appropriate DisplayRealType-s
        ShadowRealType[] DomainReferenceComponents = getDomainReferenceComponents();
        // MEM
        mapValues(display_values, reference_values, DomainReferenceComponents);
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
      }
      // FREE
      domain_values = null;
    } // end if (!isTextureMap)

    if (this instanceof ShadowFunctionType) {

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
            }
   
            // map reference_values to appropriate DisplayRealType-s
            // MEM
            mapValues(display_values, reference_values,
                      getComponents(componentWithRef[i], false));
            // FREE
            reference_values = null;
            // FREE (redundant reference to range_values)
            values = null;
          } // end for (int i=0; i<refToComponent.length; i++)
        } // end (refToComponent != null)
        // FREE
        range_values = null;
      } // end if (range_values != null)

      if (anyText && text_values == null) {
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
      } // end if (anyText)
    } // end if (this instanceof ShadowFunctionType)

    //
    // NOTE -
    // currently assuming SelectRange changes require Transform
    // see DataRenderer.isTransformControl
    //
    // get array that composites SelectRange components
    // range_select is null if all selected
    // MEM
    boolean[][] range_select =
      assembleSelect(display_values, domain_length, valueArrayLength,
                     valueToScalar, display);

    if (range_select[0] != null && range_select[0].length == 1 &&
        !range_select[0][0]) {
      // single missing value in range_select[0], so render nothing
      return false;
    }

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

      boolean pointMode = mode.getPointMode();


      // MEM_WLH - this moved
      boolean[] single_missing = {false, false, false, false};
      // assemble an array of RGBA values
      // MEM
      byte[][] color_values =
        assembleColor(display_values, valueArrayLength, valueToScalar,
                      display, default_values, range_select,
                      single_missing);
 /*
if (color_values != null) {
  System.out.println("color_values.length = " + color_values.length +
                     " color_values[0].length = " + color_values[0].length);
  System.out.println(color_values[0][0] + " " + color_values[1][0] +
                     " " + color_values[2][0]);
}
*/


      float[][] flow1_values = new float[3][];
      float[][] flow2_values = new float[3][];
      float[] flowScale = new float[2];
      // MEM
      assembleFlow(flow1_values, flow2_values, flowScale,
                   display_values, valueArrayLength, valueToScalar,
                   display, default_values, range_select);
 
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

      // MEM - but not if isTextureMap
      Set spatial_set = 
        assembleSpatial(spatial_values, display_values, valueArrayLength,
                        valueToScalar, display, default_values,
                        inherited_values, domain_set, Domain.getAllSpatial(),
                        anyContour, spatialDimensions, range_select,
                        flow1_values, flow2_values, flowScale, swap);
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

      int spatialDomainDimension = spatialDimensions[0];
      int spatialManifoldDimension = spatialDimensions[1];

      // System.out.println("assembleSpatial");
 
      int spatial_length = Math.min(domain_length, spatial_values[0].length);

/* MEM_WLH - move this up
      boolean[] single_missing = {false, false, false, false};
      // assemble an array of RGBA values
      // MEM
      byte[][] color_values =
        assembleColor(display_values, valueArrayLength, valueToScalar,
                      display, default_values, range_select,
                      single_missing);

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

      int color_length = Math.min(domain_length, color_values[0].length);
      int alpha_length = color_values[3].length;
/*
      System.out.println("assembleColor, color_length = " + color_length +
                         "  " + color_values.length);
*/

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
            shadow_api instanceof visad.java2d.ShadowTypeJ2D) {
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
          assembleShape(display_values, valueArrayLength, valueToMap, MapVector,
                        valueToScalar, display, default_values, inherited_values,
                        spatial_values, color_values, range_select, -1);
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
          array = makeText(text_values, text_control, spatial_values,
                           color_values, range_select);
          shadow_api.addToGroup(group, array, mode,
                                constant_alpha, constant_color);
          array = null;
          anyTextCreated = true;
        }

        boolean anyFlowCreated = false;
        if (anyFlow) {
          // try Flow1
          array = makeFlow(flow1_values, flowScale[0], spatial_values,
                           color_values, range_select);
          shadow_api.addToGroup(group, array, mode,
                                constant_alpha, constant_color);
          array = null;
          anyFlowCreated = true;

          // try Flow2
          array = makeFlow(flow2_values, flowScale[1], spatial_values,
                           color_values, range_select);
          shadow_api.addToGroup(group, array, mode,
                                constant_alpha, constant_color);
          array = null;
          anyFlowCreated = true;
        }

        boolean anyContourCreated = false;
        if (anyContour) {
          for (int i=0; i<valueArrayLength; i++) {
            int displayScalarIndex = valueToScalar[i];
            DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
            if (real.equals(Display.IsoContour) &&
                display_values[i] != null &&
                display_values[i].length == domain_length &&
                inherited_values[i] == 0) {
              // non-inherited IsoContour, so generate contours
              array = null;
              ContourControl control = (ContourControl)
                ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
              boolean[] bvalues = new boolean[2];
              float[] fvalues = new float[5];
              control.getMainContours(bvalues, fvalues);
              if (bvalues[0]) {
                if (range_select[0] != null) {
                  int len = range_select[0].length;
                  if (len == 1 || display_values[i].length == 1) break;
                  for (int j=0; j<len; j++) {
                    if (!range_select[0][j]) {
                      display_values[i][j] = Float.NaN;
                    }
                  }
                }
                if (spatialManifoldDimension == 3) {
                  if (fvalues[0] == fvalues[0]) {
                    if (spatial_set != null) {
                      // System.out.println("makeIsoSurface at " + fvalues[0]);
                      array = spatial_set.makeIsoSurface(fvalues[0],
                                  display_values[i], color_values, indexed);
                      // System.out.println("makeIsoSurface " + array.vertexCount);
                      shadow_api.addToGroup(group, array, mode,
                                            constant_alpha, constant_color);
                      array = null;
                    }
                  }
                  anyContourCreated = true;
                }
                else if (spatialManifoldDimension == 2) {
                  if (spatial_set != null) {
                    arrays =
                      spatial_set.makeIsoLines(fvalues[1], fvalues[2], fvalues[3],
                                               fvalues[4], display_values[i],
                                               color_values, swap);
// System.out.println("makeIsoLines");
                    if (arrays != null && arrays.length > 0 && arrays[0] != null &&
                        arrays[0].vertexCount > 0) {
                      shadow_api.addToGroup(group, arrays[0], mode,
                                            constant_alpha, constant_color);
                      arrays[0] = null;
                      if (bvalues[1] && arrays[2] != null) {
/*
System.out.println("makeIsoLines with labels arrays[2].vertexCount = " +
                   arrays[2].vertexCount);
*/
                        // draw labels
                        array = arrays[2];
                        //  FREE
                        arrays = null;
                      }
                      else if ((!bvalues[1]) && arrays[1] != null) {
/*
System.out.println("makeIsoLines without labels arrays[1].vertexCount = " +
                   arrays[1].vertexCount);
*/
                        // fill in contour lines in place of labels
                        array = arrays[1];
                        //  FREE
                        arrays = null;
                      }
                      else {
                        array = null;
                      }
                      if (array != null) {
                        shadow_api.addToGroup(group, array, mode,
                                              constant_alpha, constant_color);
                        array = null;
                      }
                    }
                  } // end if (spatial_set != null)
                  anyContourCreated = true;
                } // end if (spatialManifoldDimension == 3 or 2)
              } // end if (bvalues[0])
            } // end if (real.equals(Display.IsoContour) && not inherited)
          } // end for (int i=0; i<valueArrayLength; i++)
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
/* Java3D way using alpha, which doesn't work (yet?) for texture mapping
              float alpha =
                default_values[display.getDisplayScalarIndex(Display.Alpha)];
              if (constant_alpha == constant_alpha) {
                alpha = constant_alpha;
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
              for (int i=0; i<len; i++) {
                if (!range_select[0][i]) {
                  // make missing pixel black
                  color_values[0][i] = 0;
                  color_values[1][i] = 0;
                  color_values[2][i] = 0;
                }
              }
            } // end if (range_select[0] != null)

            // MEM
            VisADQuadArray qarray = new VisADQuadArray();
            qarray.vertexCount = 4;
            qarray.coordinates = coordinates;
            qarray.texCoords = texCoords;
            qarray.colors = colors;
            qarray.normals = normals;

            BufferedImage image = null;
            int[] rgbArray = new int[texture_width * texture_height];
            if (color_values.length > 3) {
              int k = 0;
              int r, g, b, a;
              image = new BufferedImage(texture_width, texture_height,
                                        BufferedImage.TYPE_INT_ARGB);
              for (int j=0; j<data_height; j++) {
                for (int i=0; i<data_width; i++) {
/* MEM_WLH
                  r = (int) (color_values[0][k] * 255.0);
                  r = (r < 0) ? 0 : (r > 255) ? 255 : r;
                  g = (int) (color_values[1][k] * 255.0);
                  g = (g < 0) ? 0 : (g > 255) ? 255 : g;
                  b = (int) (color_values[2][k] * 255.0);
                  b = (b < 0) ? 0 : (b > 255) ? 255 : b;
                  a = (int) (color_values[3][k] * 255.0);
                  a = (a < 0) ? 0 : (a > 255) ? 255 : a;
*/
                  r = (color_values[0][k] < 0) ? color_values[0][k] + 256 :
                                                 color_values[0][k];
                  g = (color_values[1][k] < 0) ? color_values[1][k] + 256 :
                                                 color_values[1][k];
                  b = (color_values[2][k] < 0) ? color_values[2][k] + 256 :
                                                 color_values[2][k];
                  a = (color_values[3][k] < 0) ? color_values[3][k] + 256 :
                                                 color_values[3][k];
                  // image.setRGB(i, j, ((r << 24) | (g << 16) | (b << 8) | a));
                  image.setRGB(i, j, ((a << 24) | (r << 16) | (g << 8) | b));
                  k++;
                }
                for (int i=data_width; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              for (int j=data_height; j<texture_height; j++) {
                for (int i=0; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              // transparency does not work yet
            }
            else { // (color_values.length == 3)
              int k = 0;
              int r, g, b, a;
              image = new BufferedImage(texture_width, texture_height,
                                        BufferedImage.TYPE_INT_ARGB);
              for (int j=0; j<data_height; j++) {
                for (int i=0; i<data_width; i++) {
/* MEM_WLH
                  r = (int) (color_values[0][k] * 255.0);
                  r = (r < 0) ? 0 : (r > 255) ? 255 : r;
                  g = (int) (color_values[1][k] * 255.0);
                  g = (g < 0) ? 0 : (g > 255) ? 255 : g;
                  b = (int) (color_values[2][k] * 255.0);
                  b = (b < 0) ? 0 : (b > 255) ? 255 : b;
*/
                  r = (color_values[0][k] < 0) ? color_values[0][k] + 256 :
                                                 color_values[0][k];
                  g = (color_values[1][k] < 0) ? color_values[1][k] + 256 :
                                                 color_values[1][k];
                  b = (color_values[2][k] < 0) ? color_values[2][k] + 256 :
                                                 color_values[2][k];
                  a = 255;

                  // image.setRGB(i, j, ((r << 24) | (g << 16) | (b << 8) | a));
                  image.setRGB(i, j, ((a << 24) | (r << 16) | (g << 8) | b));
                  k++;
                }
                for (int i=data_width; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              for (int j=data_height; j<texture_height; j++) {
                for (int i=0; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }

//
// this doesn't work - why not?
//            image.setRGB(0, 0, texture_width, texture_height,
//                         rgbArray, 0, texture_width);
//
// this doesn't work either - why not?
//            for (int j=0; j<texture_height; j++) {
//              image.setRGB(0, j, texture_width, 1,
//                           rgbArray, j*texture_width, texture_width);
//            }
//

            } // end if (color_values.length == 3)

            shadow_api.textureToGroup(group, qarray, image, mode,
                                      constant_alpha, constant_color,
                                      texture_width, texture_height);

            // System.out.println("isTextureMap done");
            return false;
          } // end if (isTextureMap)
          else if (range_select[0] != null) {
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
            /* CTR: 13 Oct 1998 - call new makePointGeometry signature */
            array = makePointGeometry(spatial_values, color_values, true);
            // System.out.println("makePointGeometry for some missing");
          }
          else if (pointMode) {
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry for pointMode");
          }
          else if (spatial_set == null) {
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry for spatial_set == null");
          }
          else if (spatialManifoldDimension == 1) {
            //
            // TO_DO
            // test make1DGeometry (I think I did already,
            //   but test it again just to make sure)
            //
            array = spatial_set.make1DGeometry(color_values);
            // System.out.println("make1DGeometry");
          }
          else if (spatialManifoldDimension == 2) {
            array = spatial_set.make2DGeometry(color_values, indexed);
            // System.out.println("make2DGeometry  vertexCount = " +
            //                    array.vertexCount);
          }
          else if (spatialManifoldDimension == 3) {
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry  for 3D");
            //
            // when make3DGeometry is implemented:
            // array = spatial_set.make3DGeometry(color_values);
            //
          }
          else if (spatialManifoldDimension == 0) {
            array = spatial_set.makePointGeometry(color_values);
            // System.out.println("makePointGeometry  for 0D");
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
            if (renderer.getIsDirectManipulation()) {
              renderer.setSpatialValues(spatial_values);
            }
          }
        } // end if (!anyContourCreated && !anyFlowCreated &&
          //         !anyTextCreated && !anyShapeCreated) 
        return false;
      } // end if (LevelOfDifficulty == SIMPLE_FIELD)
      else if (LevelOfDifficulty == SIMPLE_ANIMATE_FIELD) {

        Control control = null;
        Object swit = null;
        int index = -1;
  
        for (int i=0; i<valueArrayLength; i++) {
          float[] values = display_values[i];
          if (values != null) {
            int displayScalarIndex = valueToScalar[i];
            DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
            if (real.equals(Display.Animation) ||
                real.equals(Display.SelectValue)) {
              swit = shadow_api.makeSwitch();
              index = i;
              control =
                ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
              break;
            }
          } // end if (values != null)
        } // end for (int i=0; i<valueArrayLength; i++)
  
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
            if (color_values[0].length > 1) {
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
              assembleShape(display_values, valueArrayLength, valueToMap, MapVector,
                            valueToScalar, display, default_values, inherited_values,
                            sp, co, ra, i);
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
              array = makeText(te, text_control, spatial_values, co, ra);
              if (array != null) {
                shadow_api.addToGroup(branch, array, mode,
                                    constant_alpha, constant_color);
                array = null;
                anyTextCreated = true;
              }
            }

            if (!anyShapeCreated && !anyTextCreated) {
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

      for (int i=0; i<valueArrayLength; i++) {
        float[] values = display_values[i];
        if (values != null) {
          int displayScalarIndex = valueToScalar[i];
          DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
          if (real.equals(Display.Animation) ||
              real.equals(Display.SelectValue)) {
            swit = shadow_api.makeSwitch();
            index = i;
            control =
              ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
            break;
          }
        } // end if (values != null)
      } // end for (int i=0; i<valueArrayLength; i++)

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
          if (control != null) {
            Object branch = shadow_api.makeBranch();
            post |= shadow_api.recurseRange(branch, ((Field) data).getSample(i),
                                             range_value_array, default_values,
                                             renderer);
            shadow_api.addToSwitch(swit, branch);
            // System.out.println("addChild " + i + " of " + domain_length);
          }
          else {
            post |= shadow_api.recurseRange(group, ((Field) data).getSample(i),
                                             range_value_array, default_values,
                                             renderer);
          }
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


}

