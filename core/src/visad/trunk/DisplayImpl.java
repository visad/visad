
//
// DisplayImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
RandomMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;
import java.io.*;
import com.sun.j3d.utils.applet.AppletFrame;
import javax.media.j3d.*;
import java.vecmath.*;

// GUI handling
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

import visad.data.netcdf.plain.Plain;

/**
   DisplayImpl is the VisAD class for displays.  It is runnable.<P>

   DisplayImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImpl extends ActionImpl implements Display {

  /** instance variables */
  /** a Vector of ScalarMap objects;
      does not include ConstantMap objects */
  private Vector MapVector = new Vector();

  /** a Vector of ConstantMap objects */
  private Vector ConstantMapVector = new Vector();

  /** a Vector of RealType objects occuring in MapVector */
  private Vector RealTypeVector = new Vector();

  /** a Vector of DisplayRealType objects occuring in MapVector */
  private Vector DisplayRealTypeVector = new Vector();

  /** list of Control objects linked to ScalarMap objects in MapVector;
      the Control objects may be linked to UI widgets, or just computed */
  private Vector ControlVector = new Vector();
  private ProjectionControl projection;
  private GraphicsModeControl mode;

  /** ordered list of Renderer objects that render Data objects */
  private Vector RendererVector = new Vector();

  /** DisplayRenderer object for background and metadata rendering */
  private DisplayRenderer displayRenderer;

  /** a Vector of BadMappingException and UnimplementedException
      Strings generated during the last invocation of doAction */
  private Vector ExceptionVector = new Vector();

  /** basic graphics api for DisplayImpl */
  private int graphicsApi;
  /** legal values for graphicsApi */
  public static final int JPANEL_JAVA3D = 1;
  public static final int APPLETFRAME_JAVA3D = 2;
  /** these are used for APPLETFRAME_JAVA3D */
  private DisplayApplet applet;
  private AppletFrame frame;
  /** these are used for JPANEL_JAVA3D */
  private DisplayPanel panel;

  /** set to indicate need to compute ranges of RealType-s
      and sampling for Animation */
  private boolean initialize = true;

  /** length of ValueArray of distinct DisplayRealType values;
      one per Single DisplayRealType that occurs in a ScalarMap,
      plus one per ScalarMap per non-Single DisplayRealType;
      ScalarMap.valueIndex is an index into ValueArray */
  int valueArrayLength;

  /** mapping from ValueArray to DisplayScalar */
  int[] valueToScalar;

  /** mapping from ValueArray to MapVector */
  int[] valueToMap;

  /** constructor with DefaultDisplayRenderer */
  public DisplayImpl(String name)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRenderer(), JPANEL_JAVA3D);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImpl(String name, DisplayRenderer renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL_JAVA3D);
  }

  /** constructor with DefaultDisplayRenderer */
  public DisplayImpl(String name, int api)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRenderer(), api);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImpl(String name, DisplayRenderer renderer, int api)
         throws VisADException, RemoteException {
    super(name);
    // put system intrinsic DisplayRealType-s in DisplayRealTypeVector
    for (int i=0; i<DisplayRealArray.length; i++) {
      DisplayRealTypeVector.addElement(DisplayRealArray[i]);
    }
    displayRenderer = renderer;
    displayRenderer.setDisplay(this);
    // initialize ScalarMap's, ShadowDisplayReal's and Control's
    clearMaps();
    // a GraphicsModeControl always exists
    mode = new GraphicsModeControl(this);
    ControlVector.addElement(mode);
    // a ProjectionControl always exists
    projection =
      (ProjectionControl) Display.XAxis.getControl().cloneButContents(this);
    ControlVector.addElement(projection);

    graphicsApi = api;
    if (api == APPLETFRAME_JAVA3D) {
      applet = new DisplayApplet(this);
      frame = new AppletFrame(applet, 256, 256);
      frame.setTitle(name);
    }
    else if (api == JPANEL_JAVA3D) {
      panel = new DisplayPanel(this);
    }
    else {
      throw new DisplayException("DisplayImpl: bad graphicsApi");
    }
  }

  public JPanel getPanel() {
    return panel;
  }

  /** create link to DataReference with DefaultRenderer;
      must be local DataReferenceImpl */
  public void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException {
    if (!(ref instanceof DataReferenceImpl)) {
      throw new RemoteVisADException("DisplayImpl.addReference: requires " +
                                     "DataReferenceImpl");
    }
    if (findReference(ref) != null) {
      throw new TypeException("DisplayImpl.addReference: link already exists");
    }
    Renderer renderer = new DefaultRenderer();
    DataDisplayLink[] links =
      {new DataDisplayLink(ref, this, this, constant_maps, renderer)};
    addLink(links[0]);
    renderer.setLinks(links, this);
    RendererVector.addElement(renderer);
    initialize = true;
    notifyAction();
  }

  /** method for use by RemoteActionImpl.addReference that adapts this
      ActionImpl */
  void adaptedAddReference(RemoteDataReference ref, RemoteDisplay display,
       ConstantMap[] constant_maps) throws VisADException, RemoteException {
    if (findReference(ref) != null) {
      throw new TypeException("DisplayImpl.adaptedAddReference: " +
                              "link already exists");
    }
    Renderer renderer = new DefaultRenderer();
    DataDisplayLink[] links =
      {new DataDisplayLink(ref, this, display, constant_maps, renderer)};
    addLink(links[0]);
    renderer.setLinks(links, this);
    RendererVector.addElement(renderer);
    initialize = true;
    notifyAction();
  }

  /** create link to set of DataReference objects with non-DefaultRenderer;
      must be local DataReferenceImpl */
  public void addReferences(Renderer renderer, DataReference[] refs,
                            ConstantMap[][] constant_maps)
         throws VisADException, RemoteException {
    if (refs.length < 1) {
      throw new DisplayException("DisplayImpl.addReferences: must have at " +
                                 "least one DataReference");
    }
    if (constant_maps != null && refs.length != constant_maps.length) {
      throw new DisplayException("DisplayImpl.addReferences: constant_maps " +
                                 "length must match refs length");
    }
    DataDisplayLink[] links = new DataDisplayLink[refs.length];
    for (int i=0; i< refs.length; i++) {
      if (!(refs[i] instanceof DataReferenceImpl)) {
        throw new RemoteVisADException("DisplayImpl.addReferences: requires " +
                                       "DataReferenceImpl");
      }
      if (findReference(refs[i]) != null) {
        throw new TypeException("DisplayImpl.addReferences: link already exists");
      }
      if (constant_maps == null) {
        links[i] =
          new DataDisplayLink(refs[i], this, this, null, renderer);
      }
      else {
        links[i] =
          new DataDisplayLink(refs[i], this, this, constant_maps[i], renderer);
      }
      addLink(links[i]);
    }
    renderer.setLinks(links, this);
    RendererVector.addElement(renderer);
    initialize = true;
    notifyAction();
  }

  /** method for use by RemoteActionImpl.addReferences that adapts this
      ActionImpl; this allows a mix of local and remote refs */
  void adaptedAddReferences(Renderer renderer, DataReference[] refs,
       RemoteDisplay display, ConstantMap[][] constant_maps)
       throws VisADException, RemoteException {
    if (refs.length < 1) {
      throw new DisplayException("DisplayImpl.addReferences: must have at " +
                                 "least one DataReference");
    }
    if (constant_maps != null && refs.length != constant_maps.length) {
      throw new DisplayException("DisplayImpl.addReferences: constant_maps " +
                                 "length must match refs length");
    }
    DataDisplayLink[] links = new DataDisplayLink[refs.length];
    for (int i=0; i< refs.length; i++) {
      if (findReference(refs[i]) != null) {
        throw new TypeException("DisplayImpl.addReferences: link already exists");
      }
      if (refs[i] instanceof DataReferenceImpl) {
        // refs[i] is local
        if (constant_maps == null) {
          links[i] =
            new DataDisplayLink(refs[i], this, this, null, renderer);
        }   
        else {
          links[i] =
            new DataDisplayLink(refs[i], this, this, constant_maps[i], renderer);
        }
      }
      else {
        // refs[i] is remote
        if (constant_maps == null) {
          links[i] =
            new DataDisplayLink(refs[i], this, display, null, renderer);
        }   
        else {
          links[i] =
            new DataDisplayLink(refs[i], this, display, constant_maps[i], renderer);
        }
      }
      addLink(links[i]);
    }
    renderer.setLinks(links, this);
    RendererVector.addElement(renderer);
    initialize = true;
    notifyAction();
  }

  /** remove link to a DataReference;
      must be local DataReferenceImpl */
  public void removeReference(DataReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof DataReferenceImpl)) {
      throw new RemoteVisADException("ActionImpl.removeReference: requires " +
                                     "DataReferenceImpl");
    }
    adaptedDisplayRemoveReference(ref);
  }

  /** remove link to a DataReference;
      method for use by RemoteActionImpl.removeReference that adapts this
      ActionImpl;
      because DataReference array input to adaptedAddReferences may be a
      mix of local and remote, we tolerate either here */
  void adaptedDisplayRemoveReference(DataReference ref)
       throws VisADException, RemoteException {
    DataDisplayLink link = (DataDisplayLink) findReference(ref);
    // don't throw an Exception if link is null: users may try to
    // remove all DataReferences added by a call to addReferences
    if (link == null) return;
    Renderer renderer = link.getRenderer();
    renderer.clearScene();
    DataDisplayLink[] links = renderer.getLinks();
    RendererVector.removeElement(renderer);
    removeLinks(links);
    initialize = true;
  }

  /** used by Control-s to notify this DisplayImpl that
      they have changed */
  public void controlChanged() {
    synchronized (this) {
      notify();
    }
  }

  /** a Display is runnable;
      doAction is invoked by any event that requires a re-transform */
  public void doAction() throws VisADException, RemoteException {
    ExceptionVector.removeAllElements();
    // set tickFlag-s in changed Control-s
    Enumeration maps = MapVector.elements();
    while (maps.hasMoreElements()) {
      Control control = ((ScalarMap) maps.nextElement()).getControl();
      if (control != null) control.setTicks();
    }

    // set ScalarMap.valueIndex-s and valueArrayLength
    int n = getDisplayScalarCount();
    int[] scalarToValue = new int[n];
    for (int i=0; i<n; i++) scalarToValue[i] = -1;
    valueArrayLength = 0;
    maps = MapVector.elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = ((ScalarMap) maps.nextElement());
      DisplayRealType dreal = map.getDisplayScalar();
      if (dreal.isSingle()) {
        int j = getDisplayScalarIndex(dreal);
        if (scalarToValue[j] < 0) {
          scalarToValue[j] = valueArrayLength;
          valueArrayLength++;
        }
        map.setValueIndex(scalarToValue[j]);
      }
      else {
        map.setValueIndex(valueArrayLength);
        valueArrayLength++;
      }
    }
 
    // set valueToScalar and valueToMap arrays
    valueToScalar = new int[valueArrayLength];
    valueToMap = new int[valueArrayLength];
    maps = MapVector.elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = ((ScalarMap) maps.nextElement());
      DisplayRealType dreal = map.getDisplayScalar();
      valueToScalar[map.getValueIndex()] = getDisplayScalarIndex(dreal);
      valueToMap[map.getValueIndex()] = MapVector.indexOf(map);
    }

    DataShadow shadow = null;
    // invoke each Renderer (to prepare associated Data objects
    // for transformation)
    // clone RendererVector to avoid need for synchronized access
    Vector temp = ((Vector) RendererVector.clone());
    Enumeration renderers = temp.elements();
    boolean badScale = false;
    while (renderers.hasMoreElements()) {
      Renderer renderer = (Renderer) renderers.nextElement();
      shadow = renderer.prepareAction(initialize, shadow);
      badScale |= renderer.getBadScale();
    }
    initialize = badScale;

    if (shadow != null) {
      // apply RealType ranges and animationSampling
      maps = MapVector.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = ((ScalarMap) maps.nextElement());
        map.setRange(shadow);
      }
    }

    ScalarMap.equalizeFlow(MapVector, Display.DisplayFlow1Tuple);
    ScalarMap.equalizeFlow(MapVector, Display.DisplayFlow2Tuple);

    renderers = temp.elements();
    while (renderers.hasMoreElements()) {
      Renderer renderer = (Renderer) renderers.nextElement();
      renderer.doAction();
    }
    // clear tickFlag-s in Control-s
    maps = MapVector.elements();
    while(maps.hasMoreElements()) {
      Control control = ((ScalarMap) maps.nextElement()).getControl();
      if (control != null) control.resetTicks();
    }
  }

  /** add message from BadMappingException or
      UnimplementedException to ExceptionVector */
  public void addException(String error_string) {
    ExceptionVector.addElement(error_string);
  }

  /** get a clone of ExceptionVector to avoid
      concurrent access by Display thread */
  public Vector getExceptionVector() {
    return (Vector) ExceptionVector.clone();
  }

  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  public int getDisplayScalarCount() {
    return DisplayRealTypeVector.size();
  }

  public DisplayRealType getDisplayScalar(int index) {
    return (DisplayRealType) DisplayRealTypeVector.elementAt(index);
  }

  public int getDisplayScalarIndex(DisplayRealType dreal) {
    int dindex;
    synchronized (DisplayRealTypeVector) {
      dindex = DisplayRealTypeVector.indexOf(dreal);
      if (dindex < 0) {
        DisplayRealTypeVector.addElement(dreal);
        dindex = DisplayRealTypeVector.indexOf(dreal);
      }
    }
    return dindex;
  }

  public int getScalarCount() {
    return RealTypeVector.size();
  }

  public RealType getScalar(int index) {
    return (RealType) RealTypeVector.elementAt(index);
  }

  public int getScalarIndex(RealType real) throws RemoteException {
    return RealTypeVector.indexOf(real);
  }

  /** add a ScalarMap to this Display;
      can only be invoked when no DataReference-s are
      linked to this Display */
  public void addMap(ScalarMap map)
         throws VisADException, RemoteException {
    int index;
    if (!RendererVector.isEmpty()) {
      throw new DisplayException("DisplayImpl.addMap: RendererVector " +
                                 "must be empty");
    }
    if (displayRenderer.getMode2D() &&
        Display.ZAxis.equals(map.getDisplayScalar())) {
      throw new DisplayException("DisplayImpl.addMap: cannot map to " +
                                 "ZAxis in 2D mode");
    }
    map.setDisplay(this);

    if (map instanceof ConstantMap) {
      synchronized (ConstantMapVector) {
        Enumeration maps = ConstantMapVector.elements();
        while(maps.hasMoreElements()) {
          ConstantMap map2 = (ConstantMap) maps.nextElement();
          if (map2.getDisplayScalar().equals(map.getDisplayScalar())) {
            throw new DisplayException("Display.addMap: two ConstantMap-s have" +
                                       " the same DisplayScalar");
          }
        }
        ConstantMapVector.addElement(map);
      }
    }
    else { // !(map instanceof ConstantMap)
      // add to RealTypeVector and set ScalarIndex
      RealType real = map.getScalar();
      DisplayRealType dreal = map.getDisplayScalar();
      synchronized (MapVector) {
        Enumeration maps = MapVector.elements();
        while(maps.hasMoreElements()) {
          ScalarMap map2 = (ScalarMap) maps.nextElement();
          if (real == map2.getScalar() && dreal == map2.getDisplayScalar()) {
            throw new DisplayException("Display.addMap: two ScalarMap-s " +
                                       "with the same RealType & DisplayRealType");
          }
          if (dreal == Display.Animation &&
              map2.getDisplayScalar() == Display.Animation) {
            throw new DisplayException("Display.addMap: two RealType-s are " +
                                       "mapped to Animation");
          }
        }
        MapVector.addElement(map);
      }
      synchronized (RealTypeVector) {
        index = RealTypeVector.indexOf(real);
        if (index < 0) {
          RealTypeVector.addElement(real);
          index = RealTypeVector.indexOf(real);
        }
      }
      map.setScalarIndex(index);
      map.setControl();
    }
    addDisplayScalar(map);
  }

  void addDisplayScalar(ScalarMap map) {
    int index;
    synchronized (DisplayRealTypeVector) {
      DisplayRealType dreal = map.getDisplayScalar();
      index = DisplayRealTypeVector.indexOf(dreal);
      if (index < 0) {
        DisplayRealTypeVector.addElement(dreal);
        index = DisplayRealTypeVector.indexOf(dreal);
      }
    }
    map.setDisplayScalarIndex(index);
  }

  /** clear set of SalarMap-s associated with this display;
      can only be invoked when no DataReference-s are
      linked to this Display */
  public void clearMaps() throws VisADException, RemoteException {
    if (!RendererVector.isEmpty()) {
      throw new DisplayException("DisplayImpl.clearMaps: RendererVector " +
                                 "must be empty");
    }
    Enumeration maps;
    synchronized (MapVector) {
      maps = MapVector.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        map.nullDisplay();
      }
      MapVector.removeAllElements();
    }
    synchronized (ConstantMapVector) {
      maps = ConstantMapVector.elements();
      while(maps.hasMoreElements()) {
        ConstantMap map = (ConstantMap) maps.nextElement();
        map.nullDisplay();
      }
      ConstantMapVector.removeAllElements();
    }
    synchronized (ControlVector) {
      // clear Control-s associated with this Display
      ControlVector.removeAllElements();
      // one each GraphicsModeControl and ProjectionControl always exists
      mode = new GraphicsModeControl(this);
      ControlVector.addElement(mode);
      projection =
        (ProjectionControl) Display.XAxis.getControl().cloneButContents(this);
      ControlVector.addElement(projection);
    }
    // clear RealType-s from RealTypeVector
    // removeAllElements is synchronized
    RealTypeVector.removeAllElements();
    synchronized (DisplayRealTypeVector) {
      // clear DisplayRealType-s from DisplayRealTypeVector
      DisplayRealTypeVector.removeAllElements();
      // put system intrinsic DisplayRealType-s in DisplayRealTypeVector
      for (int i=0; i<DisplayRealArray.length; i++) {
        DisplayRealTypeVector.addElement(DisplayRealArray[i]);
      }
    }
  }

  public Vector getMapVector() {
    return MapVector;
  }

  public Vector getConstantMapVector() {
    return ConstantMapVector;
  }

  public void addControl(Control control) {
    ControlVector.addElement(control);
    control.setIndex(ControlVector.indexOf(control));
  }

  /** only called for Control objects associated with 'single'
      DisplayRealType-s */
  public Control getControl(Class c) {
    synchronized (ControlVector) {
      Enumeration controls = ControlVector.elements();
      while(controls.hasMoreElements()) {
        Control control = (Control) controls.nextElement();
        if (c.equals(control.getClass())) return control;
      }
    }
    return null;
  }

  public Vector getControlVector() {
    return ControlVector;
  }

  public int getValueArrayLength() {
    return valueArrayLength;
  }

  public int[] getValueToScalar() {
    return valueToScalar;
  }

  public int[] getValueToMap() {
    return valueToMap;
  }

  public ProjectionControl getProjectionControl() {
    return projection;
  }

  public GraphicsModeControl getGraphicsModeControl() {
    return mode;
  }

  /** given their complexity, its reasonable that DisplayImpl
      objects are only equal to themselves */
  public boolean equals(Object obj) {
    return (obj == this);
  }

  public String toString() {
    return toString("");
  }

  public String toString(String pre) {
    String s = pre + "Display\n";
    Enumeration maps = MapVector.elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      s = s + map.toString(pre + "    ");
    }
    maps = ConstantMapVector.elements();
    while(maps.hasMoreElements()) {
      ConstantMap map = (ConstantMap) maps.nextElement();
      s = s + map.toString(pre + "    ");
    }
    return s;
  }

  /** run 'java visad.DisplayImpl' to test the DisplayImpl class */
  public static void main(String args[])
         throws IOException, VisADException, RemoteException {


    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);

    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);

    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);

    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    FunctionType image_vis = new FunctionType(earth_location, vis_radiance);
    FunctionType image_ir = new FunctionType(earth_location, ir_radiance);

    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);

    System.out.println(time_images);
    System.out.println(grid_tuple);
    System.out.println(image_tuple);
    System.out.println(ir_histogram);

    FlatField imagev1 = FlatField.makeField(image_vis, 4);
    FlatField imager1 = FlatField.makeField(image_ir, 4);

    // use 'java visad.DisplayImpl' for size = 256 (implicit -mx16m)
    // use 'java -mx40m visad.DisplayImpl' for size = 512
    int size = 64;
    int size3d = 16;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size);
    FlatField imaget1 = FlatField.makeField(image_tuple, size);
    FlatField grid1 = FlatField.makeField(grid_tuple, size3d);

    int ntimes = 4;
    Set time_set =
      new Linear1DSet(time_type, 0.0, (double) (ntimes - 1.0), ntimes);
    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FlatField temp = imaget1;
    Real[] reals = {new Real(vis_radiance, 1.0), new Real(ir_radiance, 2.0)};
    RealTuple val = new RealTuple(reals);
    for (int i=0; i<ntimes; i++) {
      image_sequence.setSample(i, imaget1);
      temp = (FlatField) temp.add(val);
    }
    Real[] reals2 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                     new Real(vis_radiance, 1.0)};
    // RealTuple direct = new RealTuple(reals2);
    Real direct = new Real(ir_radiance, 2.0);


    DisplayImpl display1 = new DisplayImpl("display1", APPLETFRAME_JAVA3D);
    display1.addMap(new ScalarMap(vis_radiance, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display1.addMap(new ScalarMap(count, Display.ZAxis));
/*
    display1.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    display1.addMap(new ScalarMap(vis_radiance, Display.Radius));
*/
/*
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.Green));
    display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
    // display1.addMap(new ScalarMap(vis_radiance, Display.IsoContour));
    display1.addMap(new ScalarMap(ir_radiance, Display.Alpha));
    // display1.addMap(new ConstantMap(0.5, Display.Alpha));
*/


/* code to load a GIF image into imaget1 */
/*
    double[][] data = imaget1.getValues();
    DisplayApplet applet = new DisplayApplet();
    data[1] = applet.getValues("file:/home/billh/java/visad/billh.gif", size);
    imaget1.setSamples(data);
*/
/*
    Plain plain = new Plain();
    FlatField netcdf_data = (FlatField) plain.open("pmsl.nc");
    // System.out.println("netcdf_data = " + netcdf_data);
    // prints: FunctionType (Real): (lon, lat) -> P_msl
    //
    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) netcdf_data.getType();
    RealTupleType dtype = ftype.getDomain();
    MathType rtype = ftype.getRange();
    int n = dtype.getDimension();
    display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                  Display.XAxis));
    if (n > 1) {
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                    Display.YAxis));
    }
    if (n > 2) {
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                    Display.ZAxis));
    }
    if (rtype instanceof RealType) {
      display1.addMap(new ScalarMap((RealType) rtype, Display.Green));
      if (n <= 2) {
        display1.addMap(new ScalarMap((RealType) rtype, Display.ZAxis));
      }
    }
    else if (rtype instanceof RealTupleType) {
      int m = ((RealTupleType) rtype).getDimension();
      RealType rr = (RealType) ((RealTupleType) rtype).getComponent(0);
      display1.addMap(new ScalarMap(rr, Display.Green));
      if (n >= 2) {
        if (m > 1) {
          rr = (RealType) ((RealTupleType) rtype).getComponent(1);
        }
        display1.addMap(new ScalarMap(rr, Display.ZAxis));
      }
    }
    display1.addMap(new ConstantMap(0.5, Display.Red));
    display1.addMap(new ConstantMap(0.0, Display.Blue));
*/


    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);
/*
    mode.setProjectionPolicy(View.PARALLEL_PROJECTION);
java.lang.RuntimeException: PARALLEL_PROJECTION is not yet implemented
        at javax.media.j3d.View.setProjectionPolicy(View.java:423)
*/

    System.out.println(display1);

/*
    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);
*/

/*
    DataReferenceImpl ref_val = new DataReferenceImpl("ref_val");
    ref_val.setData(val);
    DataReference[] refs = {ref_val};
    display1.addReferences(new DirectManipulationRenderer(), refs, null);
*/

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refs2 = {ref_direct};
    display1.addReferences(new DirectManipulationRenderer(), refs2, null);

    DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = {ref_histogram1};
    display1.addReferences(new DirectManipulationRenderer(), refs3, null);

/*
    DataReferenceImpl ref_netcdf = new DataReferenceImpl("ref_netcdf");
    ref_netcdf.setData(netcdf_data);
    display1.addReference(ref_netcdf, null);
*/
/*
    DataReferenceImpl ref_image_sequence =
      new DataReferenceImpl("ref_image_sequence");
    ref_image_sequence.setData(image_sequence);
    display1.addReference(ref_image_sequence, null);
*/
/*
    DataReferenceImpl ref_grid1 = new DataReferenceImpl("ref_grid1");
    ref_grid1.setData(grid1);
    display1.addReference(ref_grid1, null);
*/

    DisplayImpl display2 = new DisplayImpl("display2", APPLETFRAME_JAVA3D);
    display2.addMap(new ScalarMap(vis_radiance, Display.XAxis));
    display2.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display2.addMap(new ScalarMap(count, Display.ZAxis));

    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setPointSize(5.0f);
    mode2.setPointMode(false);

    display2.addReferences(new DirectManipulationRenderer(), refs2, null);
    display2.addReferences(new DirectManipulationRenderer(), refs3, null);

/*
    DisplayImpl display5 = new DisplayImpl("display5", APPLETFRAME_JAVA3D);
    display5.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display5.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    display5.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display5.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display5);
    display5.addReference(ref_imaget1, null);

    DisplayImpl display3 = new DisplayImpl("display3", APPLETFRAME_JAVA3D);
    display3.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display3.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));
    display3.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display3.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display3);
    display3.addReference(ref_imaget1, null);

    DisplayImpl display4 = new DisplayImpl("display4", APPLETFRAME_JAVA3D);
    display4.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display4.addMap(new ScalarMap(RealType.Longitude, Display.Radius));
    display4.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display4.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display4);
    display4.addReference(ref_imaget1, null);

    delay(1000);
    System.out.println("\ndelay\n");

    ref_imaget1.incTick();

    delay(1000);
    System.out.println("\ndelay\n");

    ref_imaget1.incTick();

    delay(1000);
    System.out.println("\ndelay\n");

    ref_imaget1.incTick();
 
    System.out.println("\nno delay\n");
 
    ref_imaget1.incTick();
 
    System.out.println("\nno delay\n");
 
    ref_imaget1.incTick();
 
    delay(2000);
    System.out.println("\ndelay\n");


    display1.removeReference(ref_imaget1);
    display5.removeReference(ref_imaget1);
    display3.removeReference(ref_imaget1);
    display4.removeReference(ref_imaget1);

    display1.stop();
    display5.stop();
    display3.stop();
    display4.stop();
*/

    while (true) {
      delay(5000);
      System.out.println("\ndelay\n");
    }

    // Applications that export remote objects may not exit (according
    // to the JDK 1.1 release notes).  Here's the work around:
    //
    // System.exit(0);

  }

  public static void delay(int millis) throws VisADException {
    try {
      Real r = new Real(0.0);
      synchronized(r) {
        r.wait(millis);
      }
    }
    catch(InterruptedException e) {
    }
  }

/* Here's the output:

110% java visad.Display
FunctionType (Real): (Latitude(degrees), Longitude(degrees)) -> (vis_radiance, ir_radiance)
FunctionType (Real): (ir_radiance) -> count
FlatField  missing

FlatField  missing

Display
    ScalarMap: Latitude(degrees) -> DisplayXAxis
    ScalarMap: Longitude(degrees) -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayZAxis
    ScalarMap: vis_radiance -> DisplayRGB
    ConstantMap: 0.5 -> DisplayAlpha

Display
    ScalarMap: Latitude(degrees) -> DisplayLatitude
    ScalarMap: Longitude(degrees) -> DisplayLongitude
    ScalarMap: ir_radiance -> DisplayRadius
    ScalarMap: vis_radiance -> DisplayRGB

Display
    ScalarMap: Latitude(degrees) -> DisplayXAxis
    ScalarMap: Longitude(degrees) -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayRadius
    ScalarMap: vis_radiance -> DisplayRGB

Display
    ScalarMap: Latitude(degrees) -> DisplayXAxis
    ScalarMap: Longitude(degrees) -> DisplayRadius
    ScalarMap: ir_radiance -> DisplayYAxis
    ScalarMap: vis_radiance -> DisplayRGB

LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s

delay

LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s

delay

LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s

delay

 
no delay
 
 
no delay
 
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = false
ShadowRealTupleType: mapped to multiple spatial DisplayTupleType-s
 
delay

111%

*/

}

