
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
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;
import com.sun.j3d.utils.applet.AppletFrame;


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

  /** ordered list of Renderer objects that render Data objects */
  private Vector RendererVector = new Vector();

  /** DisplayRenderer object for background and metadata rendering */
  private DisplayRenderer displayRenderer;

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


  /** these are needed for Early Access Java3D */
  DisplayApplet applet;
  AppletFrame frame;

  /** constructor with DefaultDisplayRenderer */
  public DisplayImpl(String name) throws VisADException, RemoteException {
    this(name, new DefaultDisplayRenderer());
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImpl(String name, DisplayRenderer renderer)
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
    // a ProjectionControl always exists
    ProjectionControl proj =
      (ProjectionControl) Display.XAxis.getControl().cloneButContents(this);
    ControlVector.addElement(proj);
    applet = new DisplayApplet(this);
    frame = new AppletFrame(applet, 256, 256);
  }

  /** create link to DataReference with DefaultRenderer;
      must be local DataReferenceImpl */
  public void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException {
/* DEBUG
    System.out.println("DisplayImpl " + Name + " addReference " + ref.getName());
*/
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
/* DEBUG
    System.out.println("DisplayImpl.adaptedAddReference " + Name + " " +
                       ref.getName() + " ref = " + ref);
*/
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
    if (refs.length != constant_maps.length) {
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
      links[i] =
        new DataDisplayLink(refs[i], this, this, constant_maps[i], renderer);
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
    if (refs.length != constant_maps.length) {
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
        links[i] =
          new DataDisplayLink(refs[i], this, this, constant_maps[i], renderer);
      }
      else {
        // refs[i] is remote
        links[i] =
          new DataDisplayLink(refs[i], this, display, constant_maps[i], renderer);
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
    initialize = true;
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
    // set tickFlag-s in changed Control-s
    Enumeration maps = MapVector.elements();
    while (maps.hasMoreElements()) {
      ((ScalarMap) maps.nextElement()).getControl().setTicks();
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
 
    // set valueToScalar array
    valueToScalar = new int[valueArrayLength];
    maps = MapVector.elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = ((ScalarMap) maps.nextElement());
      DisplayRealType dreal = map.getDisplayScalar();
      valueToScalar[map.getValueIndex()] = getDisplayScalarIndex(dreal);
    }

    DataShadow shadow = null;
    // invoke each Renderer (to prepare associated Data objects
    // for transformation)
    // clone RendererVector to avoid need for synchronized access
    Vector temp = ((Vector) RendererVector.clone());
    Enumeration renderers = temp.elements();
    while (renderers.hasMoreElements()) {
      Renderer renderer = (Renderer) renderers.nextElement();
      shadow = renderer.prepareAction(initialize, shadow);
    }
    initialize = false;

    if (shadow != null) {
      // apply RealType ranges and animationSampling
      maps = MapVector.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = ((ScalarMap) maps.nextElement());
        map.setRange(shadow);
      }
    }

    renderers = temp.elements();
    while (renderers.hasMoreElements()) {
      Renderer renderer = (Renderer) renderers.nextElement();
      renderer.doAction();
    }
    // clear tickFlag-s in Control-s
    maps = MapVector.elements();
    while(maps.hasMoreElements()) {
      ((ScalarMap) maps.nextElement()).getControl().resetTicks();
    }
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
    synchronized (MapVector) {
      Enumeration maps = MapVector.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        map.nullDisplay();
      }
      MapVector.removeAllElements();
      maps = ConstantMapVector.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        map.nullDisplay();
      }
    }
    ConstantMapVector.removeAllElements();
    // clear Control-s associated with this Display
    ControlVector.removeAllElements();
    ProjectionControl proj =
      (ProjectionControl) Display.XAxis.getControl().cloneButContents(this);
    ControlVector.addElement(proj);
    // clear RealType-s from RealTypeVector
    RealTypeVector.removeAllElements();
    synchronized (MapVector) {
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
         throws VisADException, RemoteException {

    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);

    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);

    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    FunctionType image_vis = new FunctionType(earth_location, vis_radiance);
    FunctionType image_ir = new FunctionType(earth_location, ir_radiance);

    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    System.out.println(image_tuple);
    System.out.println(ir_histogram);

    Integer2DSet Domain2dSet = new Integer2DSet(earth_location, 4, 4);
    Integer1DSet Domain1dSet = new Integer1DSet(ir_radiance, 4);

    FlatField imaget1 = new FlatField(image_tuple, Domain2dSet);
    FlatField imagev1 = new FlatField(image_vis, Domain2dSet);
    FlatField imager1 = new FlatField(image_ir, Domain2dSet);

    FlatField histogram1 = new FlatField(ir_histogram, Domain1dSet);

    System.out.println(imaget1);
    System.out.println(histogram1);

    DisplayImpl display1 = new DisplayImpl("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.RGB));
    display1.addMap(new ConstantMap(0.5, Display.Alpha));
    System.out.println(display1);
    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ImageT1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);
    // display1.displayData(ref_imaget1);

    DisplayImpl display2 = new DisplayImpl("display2");
    display2.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display2.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    display2.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display2.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display2);
    display2.addReference(ref_imaget1, null);
    // display2.displayData(ref_imaget1);

    DisplayImpl display3 = new DisplayImpl("display3");
    display3.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display3.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));
    display3.addMap(new ScalarMap(ir_radiance, Display.Radius));
    display3.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display3);
    display3.addReference(ref_imaget1, null);
    // display3.displayData(ref_imaget1);

    DisplayImpl display4 = new DisplayImpl("display4");
    display4.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    display4.addMap(new ScalarMap(RealType.Longitude, Display.Radius));
    display4.addMap(new ScalarMap(ir_radiance, Display.YAxis));
    display4.addMap(new ScalarMap(vis_radiance, Display.RGB));
    System.out.println(display4);
    display4.addReference(ref_imaget1, null);
    // display4.displayData(ref_imaget1);

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
    display2.removeReference(ref_imaget1);
    display3.removeReference(ref_imaget1);
    display4.removeReference(ref_imaget1);

    display1.stop();
    display2.stop();
    display3.stop();
    display4.stop();

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

