
//
// DisplayImpl.java
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

import java.awt.*;
import java.awt.image.*;
import java.net.*;

/**
   DisplayImpl is the abstract VisAD superclass for display
   implementations.  It is runnable.<P>

   DisplayImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DisplayImpl extends ActionImpl implements Display {

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

  /** ordered list of DataRenderer objects that render Data objects */
  private Vector RendererVector = new Vector();

  /** DisplayRenderer object for background and metadata rendering */
  private DisplayRenderer displayRenderer;

  /** Component where data depictions are rendered;
      must be set by concrete subclass constructor */
  Component component;


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
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component c) {
    component = c;
  }

  public void addReference(DataReference ref)
         throws VisADException, RemoteException {
    throw new DisplayException("DisplayImpl.addReference: ConstantMap[]" +
                               " argument required");
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
    DataRenderer renderer = displayRenderer.makeDefaultRenderer();
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
    DataRenderer renderer = displayRenderer.makeDefaultRenderer();
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
  public void addReferences(DataRenderer renderer, DataReference[] refs,
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
  void adaptedAddReferences(DataRenderer renderer, DataReference[] refs,
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
    DataRenderer renderer = link.getRenderer();
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
    // invoke each DataRenderer (to prepare associated Data objects
    // for transformation)
    // clone RendererVector to avoid need for synchronized access
    Vector temp = ((Vector) RendererVector.clone());
    Enumeration renderers = temp.elements();
    boolean badScale = false;
    while (renderers.hasMoreElements()) {
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
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
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
      renderer.doAction();
    }
    // clear tickFlag-s in Control-s
    maps = MapVector.elements();
    while(maps.hasMoreElements()) {
      Control control = ((ScalarMap) maps.nextElement()).getControl();
      if (control != null) control.resetTicks();
    }
  }

  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  /** get a clone of RendererVector to avoid
      concurrent access by Display thread */
  public Vector getRendererVector() {
    return (Vector) RendererVector.clone();
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
    if (!displayRenderer.legalDisplayScalar(map.getDisplayScalar())) {
      throw new BadMappingException("DisplayImpl.addMap: " +
            map.getDisplayScalar() + " illegal for this DisplayRenderer");
    }
    map.setDisplay(this);

    if (map instanceof ConstantMap) {
      synchronized (ConstantMapVector) {
        Enumeration maps = ConstantMapVector.elements();
        while(maps.hasMoreElements()) {
          ConstantMap map2 = (ConstantMap) maps.nextElement();
          if (map2.getDisplayScalar().equals(map.getDisplayScalar())) {
            throw new BadMappingException("Display.addMap: two ConstantMaps " +
                              "have the same DisplayScalar");
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
            throw new BadMappingException("Display.addMap: two ScalarMaps " +
                                   "with the same RealType & DisplayRealType");
          }
          if (dreal == Display.Animation &&
              map2.getDisplayScalar() == Display.Animation) {
            throw new BadMappingException("Display.addMap: two RealTypes " +
                                       "are mapped to Animation");
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
      Control control = (Control) getGraphicsModeControl();
      if (control != null) addControl(control);
      control = (Control) getProjectionControl();
      if (control != null) addControl(control);
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
    if (control != null) {
      ControlVector.addElement(control);
      control.setIndex(ControlVector.indexOf(control));
    }
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

  public abstract ProjectionControl getProjectionControl();
 
  public abstract GraphicsModeControl getGraphicsModeControl(); 

  /** wait for millis milliseconds */
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

  /** RealTypes used for getImage adapter */
  private final static RealType line =
    new RealType("ImageLine", null, true);
  private final static RealType element =
    new RealType("ImageElement", null, true);
  private static RealType[] domain_components = {line, element};
  private final static RealTupleType image_domain =
    new RealTupleType(domain_components, true);
  private final static RealType radiance =
    new RealType("ImageRadiance", null, true);
  private final static FunctionType image_type =
    new FunctionType(image_domain, radiance, true);

  //
  // TO_DO
  // make this independent of Component using BufferedImage in JDK 1.2
  //

  /** this is an adapter for GIF and other images at the
      URL specified by string */
  public FlatField getImage(String string) throws VisADException {
 
    URL url = null;
    try {
      url = new URL(string);
    }
    catch (MalformedURLException e) {
      System.out.println("MalformedURLException");
      return null;
    }

    // System.out.println("url = " + url);

    Object object = null;
    try {
      object = url.getContent();
    }
    catch (java.io.IOException e) {
      System.out.println("IOException = " + e);
      return null;
    }

    // System.out.println("object.getClass = " + object.getClass());

    if (object == null) {
      System.out.println("object is null");
      return null;
    }
 
    ImageProducer producer = (ImageProducer) object;
 
    if (component == null) return null;
    Image image = component.createImage(producer);
    int height = image.getHeight(component);
    int width = image.getWidth(component);
    while (height < 0 || width < 0) {
      try { Thread.sleep(1); }
      catch (InterruptedException e) {
        System.out.println("Bad status");
        return null;
      }
      height = image.getHeight(component);
      width = image.getWidth(component);
    }

    // System.out.println("width = " + width + "  height = " + height);

    int[] pix = new int[width * height];
    float[] data0 = new float[width * height];
 
    // java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
 
    java.awt.image.PixelGrabber pg =
      new java.awt.image.PixelGrabber(producer, 0, 0, width, height,
                                      pix, 0, width);
 
    // System.out.println("pg created");
 
    // pg.setColorModel(cm); // unnecessary
 
    try { pg.grabPixels(); }
    catch (InterruptedException e) {
      System.out.println("Bad grabPixels");
      return null;
    }
 
    // System.out.println("grabPixels");
 
    try { while ((pg.status() & component.ALLBITS) == 0) Thread.sleep(1); }
    catch (InterruptedException e) {
      System.out.println("Bad status");
      return null;
    }
 
    // System.out.println("grabPixels DONE");
 
    for (int i=0; i<width * height; i++) {
      data0[i] = pix[i] & 255;
    }

    // System.out.println("Pixels copied");
 
    Linear2DSet domain_set =
      new Linear2DSet(image_domain, 0.0, (float) (width - 1.0), width,
                                    0.0, (float) (height - 1.0), height);
    FlatField field = new FlatField(image_type, domain_set);
    float[][] data = new float[1][];
    data[0] = data0;
    try {
      field.setSamples(data, false);
    }
    catch (RemoteException e) {
      field = null;
    }
    return field;
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

}

