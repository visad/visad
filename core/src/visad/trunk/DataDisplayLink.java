//
// DataDisplayLink.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
   objects and Display objects.<P>
*/
public class DataDisplayLink extends ReferenceActionLink {

  /** ShadowType created for data */
  private ShadowType shadow;

  /** used by prepareData */
  private Data data;

  /** ConstantMap-s specific to this Data */
  private Vector ConstantMapVector = new Vector();

  /** DataRenderer associated with this Data
      (may be multiple Data per DataRenderer) */
  private DataRenderer renderer;

  /** Vector of ScalarMap-s applying to this Data */
  private Vector SelectedMapVector = new Vector();

  /** default values for DisplayIndices, determined by:
      1. this.ConstantMapVector
      2. Display.ConstantMapVector
      3. DisplayRealType.DefaultValue */
  private float[] defaultValues;

  /** flag per Control to indicate need for transform when
      Control changes */
  boolean[] isTransform;

  /** transform time-out hack */
  public long start_time; // System.currentTimeMillis() when doTransform started
  public boolean time_flag;

  public DataDisplayLink(DataReference ref, DisplayImpl local_d, Display d,
                  ConstantMap[] constant_maps, DataRenderer rend, long jd)
                  throws VisADException, RemoteException {
    super(ref, local_d, d, jd);
    renderer = rend;

    if (constant_maps != null) {
      for (int i=0; i<constant_maps.length; i++) {
        // WLH 13 July 98
        Enumeration maps = ((Vector) ConstantMapVector.clone()).elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          if (map.getDisplayScalar().equals(constant_maps[i].getDisplayScalar())) {
            throw new DisplayException("DataDisplayLink: two ConstantMap-s have" +
                                       " the same DisplayScalar");
          }
        }
        constant_maps[i].setDisplay(local_d);
        ConstantMapVector.addElement(constant_maps[i]);
        local_d.addDisplayScalar(constant_maps[i]);
      }
    }
  }

  public DisplayImpl getDisplay() {
    return (DisplayImpl) local_action;
  }

  public DataRenderer getRenderer() {
    return renderer;
  }

  public Vector getSelectedMapVector() {
    return (Vector) SelectedMapVector.clone();
  }

  public void addSelectedMapVector(ScalarMap map) {
    // 'synchronized' unnecessary
    // (since prepareData is a single Thread, but ...)
    synchronized (SelectedMapVector) {
      if (!SelectedMapVector.contains(map)) {
        SelectedMapVector.addElement(map);
      }
    }
  }

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
    }
  }

  /** Prepare to render data (include feasibility check);
      return false if infeasible */
  public boolean prepareData() throws VisADException, RemoteException {
    int[] indices;
    int[] display_indices;
    int[] value_indices;
    int levelOfDifficulty;

    data = ((DataReference) ref).getData();
    if (data == null) {
      renderer.clearExceptions();
      renderer.addException(
        new DisplayException("Data is null: DataDisplayLink.prepareData"));
      return false;
    }
    MathType type = data.getType();

    SelectedMapVector.removeAllElements();

    // calculate default values for DisplayRealType-s
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
      defaultValues[map.getDisplayScalarIndex()] = (float) map.getConstant();
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

  public ShadowType getShadow() {
    return shadow;
  }

  public Data getData()
         throws VisADException, RemoteException {
/* WLH 14 Feb 98 */
    if (data == null) {
      data = ((DataReference) ref).getData();
    }
    return data;
  }

/* WLH 14 Feb 98 */
  public void clearData() {
    data = null;
  }

/* WLH 14 Feb 98 */
  public MathType getType()
         throws VisADException, RemoteException {
    Data d = getData();
    return (d == null) ? null : d.getType();
  }

  public float[] getDefaultValues() {
    return defaultValues;
  }

  public DataReference getDataReference() {
    return (DataReference) getThingReference();
  }

  public Vector getConstantMaps()
  {
    return ConstantMapVector;
  }

  public Vector getScalarMaps()
  {
    return SelectedMapVector;
  }

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

