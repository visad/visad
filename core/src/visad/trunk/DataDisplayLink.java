//
// DataDisplayLink.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

/**
   DataDisplayLink objects define connections between DataReference
   objects and Display objects. It extends ReferenceActionLink which
   is the more general link between ThingReference (extended by
   DataReference) and Action (extended by Display).<P>
*/
public class DataDisplayLink extends ReferenceActionLink {

  /** ShadowType created for data */
  private ShadowType shadow;

  /** cached copy of linked Data, used by prepareData() */
  private Data data;

  /** ConstantMaps specific to this Data */
  private Vector ConstantMapVector = new Vector();

  /** DataRenderer associated with this Data
      (may be multiple Data per DataRenderer) */
  private DataRenderer renderer;

  /** Vector of ScalarMaps applying to this Data */
  private Vector SelectedMapVector = new Vector();

  /** default values for DisplayRealTypes, determined by:
      1. this.ConstantMapVector
      2. Display.ConstantMapVector
      3. DisplayRealType.DefaultValue */
  private float[] defaultValues;

  /** flag per Control to indicate need for transform when
      Control changes, index by Control.getIndex() */
  boolean[] isTransform;

  /** value of System.currentTimeMillis() when doTransform() started */
  public long start_time;

  /** flag indicating current doTransform() has taken more than 500 ms */
  public boolean time_flag;

  /**
   * construct a DataDisplayLink linking a DataReference to a Display
   * @param ref the DataReference to link
   * @param local_d if d is DisplayImpl, then d; if d is RemoteDisplay, then
   *                its adapted DisplayImpl
   * @param d the Display
   * @param constant_maps array of ConstantMaps specific to this Data
   * @param rend DataRenderer that creates Data depictions
   * @param jd - unique ID among ReferenceActionLinks attached to Action
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DataDisplayLink(DataReference ref, DisplayImpl local_d, Display d,
                  ConstantMap[] constant_maps, DataRenderer rend, long jd)
                  throws VisADException, RemoteException {
    super(ref, local_d, d, jd);
    renderer = rend;
    setConstantMaps(constant_maps, true);
  }

  /**
   * Change ConstantMaps[] array specific to this DataDisplayLink
   * Note this call should occur between
   * display.disableAction()
   * and
   * display.enableAction()
   *
   * there are two ways for an application to get a DataDisplayLink:
   * given a DisplayImpl and a DataReference:
   *  DataDisplayLink link = (DataDisplayLink) display.findReference(ref);
   * given a DataRenderer (assuming it has only one DataReference):
   *  DataDisplayLink link = renderer.getLinks()[0];
   *
   * @param constant_maps array of ConstantMaps specific to this Data
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void setConstantMaps(ConstantMap[] constant_maps)
                  throws VisADException, RemoteException {
    setConstantMaps(constant_maps, false);
  }

  private void setConstantMaps(ConstantMap[] constant_maps, boolean init)
                  throws VisADException, RemoteException {
    Enumeration maps;

    synchronized (ConstantMapVector) {
      if (!init) {
        maps = ConstantMapVector.elements();
        while(maps.hasMoreElements()) {
          ConstantMap map = (ConstantMap) maps.nextElement();
          map.nullDisplay();
        }
        ConstantMapVector.removeAllElements();
      }

      DisplayImpl local_d = (DisplayImpl) getLocalAction();
      Display d = (Display) getAction();
  
      if (constant_maps != null) {
        for (int i=0; i<constant_maps.length; i++) {
          maps = ((Vector) ConstantMapVector.clone()).elements();
          while(maps.hasMoreElements()) {
            ScalarMap map = (ScalarMap) maps.nextElement();
            if (map.getDisplayScalar().equals(constant_maps[i].getDisplayScalar())) {
              throw new DisplayException("DataDisplayLink: two ConstantMaps have" +
                                         " the same DisplayScalar");
            }
          }
  
          if (constant_maps[i].getDisplay() != null &&
              !ConstantMap.getAllowMultipleUseKludge()) {
            throw new DisplayException(constant_maps[i] + " already has a display\n" +
                        "If this Exception breaks an existing app add a call to:\n" +
                        "ConstantMap.setAllowMultipleUseKludge(true) at the " +
                        "start of your app \n  OR you can stop reusing ConstantMaps");
          }
  
          constant_maps[i].setDisplay(local_d);
          ConstantMapVector.addElement(constant_maps[i]);
          local_d.addDisplayScalar(constant_maps[i]);
        }
      }
      if (!init) {
        getThingReference().incTick();
      }
    }
  }

  /**
   * @return the local DisplayImpl for the linked Display
   */
  public DisplayImpl getDisplay() {
    return (DisplayImpl) local_action;
  }

  /**
   * @return the DataRenderer that creates Data depictions
   */
  public DataRenderer getRenderer() {
    return renderer;
  }

  /**
   * @return a clone of Vector of ScalarMaps applying to this Data
   */
  public Vector getSelectedMapVector() {
    return (Vector) SelectedMapVector.clone();
  }

  /**
   * add a ScalarMap applying to this Data
   * @param map ScalarMap to add
   */
  public void addSelectedMapVector(ScalarMap map) {
    if (renderer == null) return;
    // 'synchronized' unnecessary
    // (since prepareData is a single Thread, but ...)
    synchronized (SelectedMapVector) {
      if (!SelectedMapVector.contains(map)) {
        SelectedMapVector.addElement(map);
      }
    }
  }

  /**
   * clear Vectors of ScalarMaps applying to this Data and
   * of ConstantMaps; also clear other instance variables
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void clearMaps()
    throws RemoteException, VisADException
  {
    Enumeration maps;

    synchronized (ConstantMapVector) {
      maps = ConstantMapVector.elements();
      while(maps.hasMoreElements()) {
        ConstantMap map = (ConstantMap) maps.nextElement();
        map.nullDisplay();
      }
      ConstantMapVector.removeAllElements();

      SelectedMapVector.removeAllElements();
      shadow = null;
      data = null;
      renderer = null;
    }
  }

  /**
   * Prepare to render data (include feasibility check);
   * @return false if infeasible
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public boolean prepareData()
         throws VisADException, RemoteException {
    if (renderer == null) return false;
    int[] indices;
    int[] display_indices;
    int[] value_indices;
    int levelOfDifficulty;

    data = ((DataReference) ref).getData();
    if (data == null) {
      renderer.clearExceptions();
      renderer.addException(
        // new DisplayException("Data is null: DataDisplayLink.prepareData"));
        new DisplayException("Data is null"));
      return false;
    }
    MathType type = data.getType();

    SelectedMapVector.removeAllElements();

    // calculate default values for DisplayRealTypes
    // lowest priority: DisplayRealType.DefaultValue
    int n = ((DisplayImpl) local_action).getDisplayScalarCount();
    defaultValues = new float[n];
    GraphicsModeControl mode =
      ((DisplayImpl) local_action).getGraphicsModeControl();
    for (int i=0; i<n; i++) {
      DisplayRealType dreal =
        (DisplayRealType) ((DisplayImpl) local_action).getDisplayScalar(i);
      defaultValues[i] = (float) dreal.getDefaultValue();
      if (Display.PointSize.equals(dreal)) {
        defaultValues[i] = mode.getPointSize();
      }
      else if (Display.LineWidth.equals(dreal)) {
        defaultValues[i] = mode.getLineWidth();
      }
      else if (Display.LineStyle.equals(dreal)) {
        defaultValues[i] = mode.getLineStyle();
      }
      else if (Display.PolygonMode.equals(dreal)) {
        defaultValues[i] = mode.getPolygonMode();
      }
      else if (Display.PolygonOffset.equals(dreal)) {
        defaultValues[i] = mode.getPolygonOffset();
      }
      else if (Display.PolygonOffsetFactor.equals(dreal)) {
        defaultValues[i] = mode.getPolygonOffsetFactor();
      }
      else if (Display.ColorMode.equals(dreal)) {
        defaultValues[i] = mode.getColorMode();
      }
      else if (Display.CurvedSize.equals(dreal)) {
        defaultValues[i] = mode.getCurvedSize();
      }
      else if (Display.MissingTransparent.equals(dreal)) {
        defaultValues[i] = (mode.getMissingTransparent()) ? 1 : -1;
      }
      else if (Display.TextureEnable.equals(dreal)) {
        defaultValues[i] = (mode.getTextureEnable()) ? 1 : -1;
      }
      else if (Display.AdjustProjectionSeam.equals(dreal)) {
        defaultValues[i] = (mode.getAdjustProjectionSeam()) ? 1 : -1;
      }
/* WLH 21 Aug 98
      defaultValues[i] = (float) (((DisplayRealType)
        ((DisplayImpl) local_action).getDisplayScalar(i)).getDefaultValue());
*/
    }
    // middle priority: DisplayImpl.ConstantMapVector
    Vector temp =
      (Vector) ((DisplayImpl) local_action).getConstantMapVector().clone();
    Enumeration maps = temp.elements();
/* WLH 13 July 98
    Enumeration maps =
      ((DisplayImpl) local_action).getConstantMapVector().elements();
*/
    while(maps.hasMoreElements()) {
      ConstantMap map = (ConstantMap) maps.nextElement();
      defaultValues[map.getDisplayScalarIndex()] = (float) map.getConstant();

    }
    // highest priority: this.ConstantMapVector
    // WLH 13 July 98
    maps =((Vector) ConstantMapVector.clone()).elements();
    while(maps.hasMoreElements()) {
      ConstantMap map = (ConstantMap) maps.nextElement();
      // WLH 10 Aug 2001
      int index = map.getDisplayScalarIndex();
      if (index >= 0) defaultValues[index] = (float) map.getConstant();
      // defaultValues[map.getDisplayScalarIndex()] = (float) map.getConstant();
    }

    try {
      renderer.clearExceptions();

      DisplayImpl local_dpy = (DisplayImpl )local_action;

      shadow = type.buildShadowType(this, null);
      ShadowType adaptedShadow = shadow.getAdaptedShadowType();
      indices = ShadowType.zeroIndices(local_dpy.getScalarCount());
      display_indices = ShadowType.zeroIndices(
                  local_dpy.getDisplayScalarCount());
      value_indices = ShadowType.zeroIndices(local_dpy.getValueArrayLength());
      final int numControls = local_dpy.getNumberOfControls();
      isTransform = new boolean[numControls];
      for (int i=0; i<numControls; i++) isTransform[i] = false;
      levelOfDifficulty =
        shadow.checkIndices(indices, display_indices, value_indices,
                              isTransform, ShadowType.NOTHING_MAPPED);
      if (levelOfDifficulty == ShadowType.LEGAL) {
        // every Control isTransform for merely LEGAL
        // (i.e., the 'dots') rendering
        for (int i=0; i<numControls; i++) isTransform[i] = true;
      }
      renderer.checkDirect();
    }
    catch (BadMappingException e) {
      data = null;
      renderer.addException(e);
      return false;
    }
    catch (UnimplementedException e) {
      data = null;
      renderer.addException(e);
      return false;
    }
    catch (RemoteException e) {
      data = null;
      renderer.addException(e);
      return false;
    }
    // can now render data
    return true;
  }

  /**
   * @return ShadowType generated from MathType of linked Data
   */
  public ShadowType getShadow() {
    return shadow;
  }

  /**
   * @return linked Data (note Data is cached until
   *         clearData() is called)
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public Data getData()
         throws VisADException, RemoteException {
    if (renderer == null) return null;
    Data data_copy = data;
    if (data_copy == null) {
      data_copy = ((DataReference) ref).getData();
    }
    data = data_copy;
    return data_copy;
  }

  /**
   * clear cached copy of linked Data
   */
  public void clearData() {
    data = null;
  }

  /**
   * @return MathType of linked Data
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public MathType getType()
         throws VisADException, RemoteException {
    Data d = getData();
    return (d == null) ? null : d.getType();
  }

  /**
   * @return default values for DisplayRealTypes
   */
  public float[] getDefaultValues() {
    return defaultValues;
  }

  /**
   * @return linked DataReference
   */
  public DataReference getDataReference() {
    return (DataReference) getThingReference();
  }

  /**
   * @return Vector of ConstantMaps specific to this Data
   */
  public Vector getConstantMaps()
  {
    return ConstantMapVector;
  }

  /**
   * @return Vector of ScalarMaps that apply to this Data
   */
  public Vector getScalarMaps()
  {
    return SelectedMapVector;
  }

  /**
   * Indicates whether or not this instance is equal to an object
   * @param o the object in question.
   * @return <code>true</code> if and only if this instance equals o.
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof DataDisplayLink)) {
      return false;
    }

    DataDisplayLink ddl = (DataDisplayLink )o;
    if (!getDataReference().equals(ddl.getDataReference())) {
      return false;
    }
    if (!getDisplay().equals(ddl.getDisplay())) {
      return false;
    }

    return true;
  }
}

