//
// SliceManager.java
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

package visad.bio;

import java.io.*;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;
import visad.*;
import visad.data.*;
import visad.util.DualRes;

/** SliceManager is the class encapsulating BioVisAD's slice logic. */
public class SliceManager
  implements ControlListener, DisplayListener, PlaneListener
{

  // -- DATA TYPE CONSTANTS --

  /** RealType for mapping measurements to Z axis. */
  static final RealType Z_TYPE = RealType.getRealType("bio_line_z");

  /** RealType for mapping timestep values to animation. */
  static final RealType TIME_TYPE = RealType.getRealType("bio_time");

  /** RealType for mapping slice values to select value and Z axis. */
  static final RealType SLICE_TYPE = RealType.getRealType("bio_slice");

  /** RealType for mapping to Red. */
  static final RealType RED_TYPE = RealType.getRealType("bio_red");

  /** RealType for mapping to Green. */
  static final RealType GREEN_TYPE = RealType.getRealType("bio_green");

  /** RealType for mapping to Blue. */
  static final RealType BLUE_TYPE = RealType.getRealType("bio_blue");


  // -- DATA TYPE INFORMATION --

  /** Domain type for 2-D image stack data. */
  RealTupleType domain2;

  /** Domain type for 3-D image stack data. */
  RealTupleType domain3;

  /** Tuple type for fields with (r, g, b) range. */
  RealTupleType colorRange;

  /** List of domain type components for image stack data. */
  RealType[] dtypes;

  /** List of range type components for image stack data. */
  RealType[] rtypes;

  /** Domain mappings for 2-D slice display. */
  ScalarMap x_map2, y_map2;

  /** X, Y and Z bounds for the data. */
  float min_x, max_x, min_y, max_y, min_z, max_z;

  /** X and Y resolution of image data. */
  int res_x, res_y;

  /** X and Y resolution for arbitrary slices. */
  int sliceRes_x, sliceRes_y;


  // -- SLICE-RELATED FIELDS --

  /** Animation control associated with 2-D animation mapping. */
  AnimationControl anim_control2;

  /** Animation control associated with 3-D animation mapping. */
  AnimationControl anim_control3;

  /** Value control associated with 2-D select value mapping. */
  ValueControl value_control2;

  /** Plane selection object. */
  private PlaneSelector ps;

  /** Is arbitrary plane selection on? */
  private boolean planeSelect;

  /** Should arbitrary plane be updated every time it changes? */
  private boolean continuous;

  /** Has arbitrary plane moved since last right mouse button press? */
  private boolean planeChanged;

  /** Is volume rendering display mode on? */
  private boolean volume;


  // -- DISPLAY MAPPING INFORMATION --

  /** High-resolution field for current timestep. */
  private FieldImpl field;

  /** Low-resolution field for all timesteps. */
  private FieldImpl lowresField;

  /**
   * Collapsed field for current timestep, used
   * with arbitrary slicing and volume rendering.
   */
  private FieldImpl collapsedField;

  /** List of range component mappings for 2-D display. */
  private ScalarMap[] rmaps2;

  /** List of range component mappings for 3-D display. */
  private ScalarMap[] rmaps3;


  // -- DATA REFERENCES --

  /** Reference for image stack data for 2-D display. */
  private DataReferenceImpl ref2;

  /** Reference for image stack data for 3-D display. */
  private DataReferenceImpl ref3;

  /** Reference for low-resolution image timestack data for 2-D display. */
  private DataReferenceImpl lowresRef2;

  /** Reference for low-resolution image timestack data for 3-D display. */
  private DataReferenceImpl lowresRef3;

  /** Reference for arbitrary plane data. */
  private DataReferenceImpl planeRef;

  /** Data renderer for 2-D image stack data. */
  private DataRenderer renderer2;

  /** Data renderer for 3-D image stack data. */
  private DataRenderer renderer3;

  /** Data renderer for arbitrary plane data in 2-D. */
  private DataRenderer planeRenderer2;

  /** Data renderer for low-resolution image timestack data in 2-D. */
  private DataRenderer lowresRenderer2;

  /** Data renderer for low-resolution image timestack data in 3-D. */
  private DataRenderer lowresRenderer3;


  // -- THUMBNAIL-RELATED FIELDS --

  /** Resolution of thumbnails. */
  private int[] thumbSize;

  /** Should low-resolution slices be displayed? */
  private boolean lowres;

  /** Should low-resolution thumbnails be created? */
  private boolean doThumbs;

  /** Does current data have low-resolution thumbnails? */
  private boolean hasThumbs;

  /** Automatically switch resolution when certain events occur? */
  private boolean autoSwitch;



  // -- OTHER FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Loader for opening data series. */
  private DefaultFamily loader;

  /** List of files containing current data series. */
  private File[] files;

  /** Should each file be interpreted as a slice rather than a timestep? */
  private boolean filesAsSlices;

  /** Number of timesteps in data series. */
  private int timesteps;

  /** Number of slices in data series. */
  private int slices;

  /** Current index in data series. */
  private int index;

  /** Current slice in data series. */
  private int slice;

  /** Timestep of data at last resolution switch. */
  private int mode_index;

  /** Slice number of data at last resolution switch. */
  private int mode_slice;


  // -- CONSTRUCTORS --

  /** Constructs a slice manager. */
  public SliceManager(BioVisAD biovis) throws VisADException, RemoteException {
    bio = biovis;
    lowres = false;
    doThumbs = true;
    autoSwitch = true;
    planeSelect = false;
    continuous = false;
    planeChanged = false;
    colorRange = new RealTupleType(
      new RealType[] {RED_TYPE, GREEN_TYPE, BLUE_TYPE});

    loader = new DefaultFamily("bio_loader");

    // data references
    ref2 = new DataReferenceImpl("bio_ref2");
    ref3 = new DataReferenceImpl("bio_ref3");
    lowresRef2 = new DataReferenceImpl("bio_lowresRef2");
    lowresRef3 = new DataReferenceImpl("bio_lowresRef3");
    planeRef = new DataReferenceImpl("bio_planeRef");
  }


  // -- API METHODS --

  /** Gets the currently displayed timestep index. */
  public int getIndex() { return index; }

  /** Gets the currently displayed image slice. */
  public int getSlice() { return slice; }

  /** Gets the number of timestep indices. */
  public int getNumberOfIndices() { return timesteps; }

  /** Gets the number of image slices. */
  public int getNumberOfSlices() { return slices; }

  /** Gets whether the currently loaded data has low-resolution thumbnails. */
  public boolean hasThumbnails() { return hasThumbs; }

  /** Sets the display detail (low-resolution or full resolution). */
  public void setMode(boolean lowres) {
    bio.toolView.setMode(lowres);
    if (this.lowres == lowres) return;
    this.lowres = lowres;
    refresh(mode_slice != slice, mode_index != index);
    mode_index = index;
    mode_slice = slice;
  }

  /** Sets the currently displayed timestep index. */
  public void setIndex(int index) {
    if (this.index == index ||
      bio.horiz.isBusy() && !lowres && !autoSwitch)
    {
      return;
    }
    boolean doRefresh = true;
    if (autoSwitch && !lowres) {
      setMode(true);
      doRefresh = false;
    }
    this.index = index;
    if (autoSwitch && index == mode_index && lowres) {
      setMode(false);
      doRefresh = false;
    }
    if (doRefresh) refresh(false, true);
    else updateAnimationControls();
  }

  /** Sets the currently displayed image slice. */
  public void setSlice(int slice) {
    if (this.slice == slice) return;
    this.slice = slice;
    refresh(true, false);
  }

  /** Sets whether to auto-switch resolutions when certain events occur. */
  public void setAutoSwitch(boolean value) { autoSwitch = value; }

  /** Sets whether to create low-resolution thumbnails of the data. */
  public void setThumbnails(boolean thumbnails, int xres, int yres) {
    doThumbs = thumbnails;
    thumbSize = new int[] {xres, yres};
  }

  /** Sets whether to do arbitrary plane selection. */
  public void setPlaneSelect(boolean value) {
    if (bio.display3 == null) return;
    planeSelect = value;
    ps.toggle(value);
    planeRenderer2.toggle(value);
    renderer2.toggle(!value);
  }

  /** Sets whether arbitrary plane is continuously updated. */
  public void setPlaneUpdate(boolean continuous) {
    this.continuous = continuous;
  }

  /** Sets whether 3-D display should use image stack or volume rendering. */
  public void setVolumeRender(boolean volume) { this.volume = volume; }

  /** Links the data series to the given list of files. */
  public void setSeries(File[] files) { setSeries(files, false); }
  
  /**
   * Links the data series to the given list of files, treating
   * each file as a slice (instead of a timestep) if specified.
   */
  public void setSeries(File[] files, boolean filesAsSlices) {
    this.files = files;
    this.filesAsSlices = filesAsSlices;
    if (filesAsSlices) doThumbs = false;
    index = 0;
    boolean success = false;
    try {
      setFile(true);
      success = true;
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    if (success) {
      bio.horiz.updateSlider(timesteps);
      bio.vert.updateSlider(slices);
      bio.state.saveState(true);
    }
  }

  /** Exports the stack of images at the current timestep. */
  public void exportImageStack(Form saver, String file)
    throws VisADException
  {
    // CTR - TODO - exportImageStack is obsolete
    setMode(false);
    final Form fsaver = saver;
    final String f = file;
    final ProgressDialog dialog = new ProgressDialog(bio,
      "Exporting image stack");
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          // save image stack data to file
          fsaver.save(f, field, true);
        }
        catch (VisADException exc) { dialog.setException(exc); }
        catch (Exception exc) {
          dialog.setException(new VisADException(
            exc.getClass() + ": " + exc.getMessage()));
        }
        dialog.setPercent(100);
        dialog.kill();
      }
    });
    t.start();
    dialog.show();
    dialog.checkException();
  }

  /** Exports an animation of the current slice across all timesteps. */
  public void exportSliceAnimation(Form saver, String file)
    throws VisADException
  {
    // CTR - TODO - exportSliceAnimation is obsolete
    final Form fsaver = saver;
    final String ff = file;
    final ProgressDialog dialog = new ProgressDialog(bio,
      "Compiling animation data");
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          // compile high-resolution animation data
          FieldImpl data = null;
          for (int i=0; i<timesteps; i++) {
            FieldImpl image;
            FieldImpl f = filesAsSlices ? field : loadData(files[i], true);
            if (i == 0) {
              FunctionType image_type =
                (FunctionType) f.getSample(0).getType();
              FunctionType anim_type = new FunctionType(TIME_TYPE, image_type);
              Integer1DSet set = new Integer1DSet(TIME_TYPE, timesteps);
              data = new FieldImpl(anim_type, set);
            }
            if (planeSelect) {
              image = (FieldImpl) ps.extractSlice((FieldImpl)
                f.domainMultiply(), res_x, res_y, res_x, res_y);
            }
            else image = (FieldImpl) f.getSample(slice);
            data.setSample(i, image, false);
            dialog.setPercent(100 * (i + 1) / timesteps);
          }

          // save animation data to file
          dialog.setText("Exporting animation");
          fsaver.save(ff, data, true);
        }
        catch (VisADException exc) { dialog.setException(exc); }
        catch (Exception exc) {
          dialog.setException(new VisADException(
            exc.getClass() + ": " + exc.getMessage()));
        }
        dialog.kill();
      }
    });
    t.start();
    dialog.show();
    dialog.checkException();
  }


  // -- INTERNAL API METHODS --

  /** ControlListener method used for programmatically updating GUI. */
  public void controlChanged(ControlEvent e) {
    if (anim_control2 != null) {
      int index = anim_control2.getCurrent();
      if (this.index != index) bio.horiz.setValue(index + 1);
    }
    int slice = (int) value_control2.getValue();
    if (this.slice != slice) bio.vert.setValue(slice + 1);
  }

  /** DisplayListener method used for detecting mouse activity. */
  public void displayChanged(DisplayEvent e) {
    if (e.getId() != DisplayEvent.MOUSE_RELEASED_RIGHT) return;
    if (e.getDisplay() != bio.display3) return;
    bio.state.saveState(planeSelect && planeChanged);
    if (planeSelect && planeChanged && !continuous) updateSlice();
    planeChanged = false;
  }

  /** PlaneListener method used for detecting PlaneSelector changes. */
  public void planeChanged() {
    planeChanged = true;
    if (continuous) updateSlice();
  }

  /** Ensures slices are set up properly for animation. */
  void startAnimation() {
    // switch to low resolution
    if (!lowres) {
      lowres = true;
      bio.toolView.setMode(true);
      setMode(true);
    }
  }

  /** Sets the arbitrary slice resolution. */
  void setSliceRange(int x, int y) {
    sliceRes_x = x;
    sliceRes_y = y;
    bio.state.saveState(true);
  }

  /** Gets the color controls for 2-D range type color mappings. */
  BaseColorControl[] getColorControls2D() {
    if (rmaps2 == null) return null;
    BaseColorControl[] controls = new BaseColorControl[rmaps2.length];
    for (int i=0; i<rmaps2.length; i++) {
      controls[i] = (BaseColorControl) rmaps2[i].getControl();
    }
    return controls;
  }

  /** Gets the color controls for 3-D range type color mappings. */
  BaseColorControl[] getColorControls3D() {
    if (rmaps3 == null) return null;
    BaseColorControl[] controls = new BaseColorControl[rmaps3.length];
    for (int i=0; i<rmaps3.length; i++) {
      controls[i] = (BaseColorControl) rmaps3[i].getControl();
    }
    return controls;
  }

  /** Writes the current program state to the given output stream. */
  void saveState(PrintWriter fout) throws IOException, VisADException {
    fout.println(files.length);
    for (int i=0; i<files.length; i++) fout.println(files[i].getPath());
    fout.println(hasThumbs);
    fout.println(thumbSize[0]);
    fout.println(thumbSize[1]);
    fout.println(sliceRes_x);
    fout.println(sliceRes_y);
    ps.saveState(fout);
  }

  /** Restores the current program state from the given input stream. */
  void restoreState(BufferedReader fin) throws IOException, VisADException {
    int len = Integer.parseInt(fin.readLine().trim());
    File[] files = new File[len];
    for (int i=0; i<len; i++) files[i] = new File(fin.readLine().trim());
    boolean thumbs = fin.readLine().trim().equals("true");
    int thumbX = Integer.parseInt(fin.readLine().trim());
    int thumbY = Integer.parseInt(fin.readLine().trim());
    int sliceX = Integer.parseInt(fin.readLine().trim());
    int sliceY = Integer.parseInt(fin.readLine().trim());
    setThumbnails(thumbs, thumbX, thumbY);
    setSeries(files);
    bio.toolView.setSliceRange(sliceX, sliceY);
    ps.restoreState(fin);
  }


  // -- HELPER METHODS --

  /**
   * Initializes the displays to use the image stack data
   * from the given files.
   */
  private void init(File[] files, int index) throws VisADException {
    final File[] f = files;
    final int curfile = index;
    final ProgressDialog dialog = new ProgressDialog(bio,
      "Loading data" + (doThumbs ? " and creating thumbnails" : ""));

    Thread t = new Thread(new Runnable() {
      public void run() {
        bio.display2.disableAction();
        if (bio.display3 != null) bio.display3.disableAction();
        try {
          clearDisplays();

          // reset measurements
          if (bio.mm.lists != null) bio.mm.clear();

          field = null;
          collapsedField = null;
          FieldImpl[][] thumbs = null;
          mode_index = mode_slice = 0;

          if (filesAsSlices) {
            // load data at all indices and compile into a single timestep
            slices = f.length;
            timesteps = 1;
            for (int i=0; i<slices; i++) {
              FieldImpl image = loadData(f[i], false);
              if (image == null) return;
              if (field == null) {
                FunctionType stack_type =
                  new FunctionType(SLICE_TYPE, image.getType());
                field = new FieldImpl(stack_type, new Integer1DSet(slices));
              }
              field.setSample(i, image);
              dialog.setPercent(100 * (i + 1) / slices);
            }
            doThumbs = false;
          }
          else if (doThumbs) {
            // load data at all indices and create thumbnails
            timesteps = f.length;
            for (int i=0; i<timesteps; i++) {
              // do current timestep last
              int ndx = i == timesteps - 1 ? curfile :
                (i >= curfile ? i + 1 : i);

              // dump old dataset (for garbage collection)
              field = null;
              System.gc();

              field = loadData(f[ndx], true);
              if (field == null) return;
              if (thumbs == null) {
                slices = field.getLength();
                thumbs = new FieldImpl[timesteps][slices];
              }
              for (int j=0; j<slices; j++) {
                FieldImpl image = (FieldImpl) field.getSample(j);
                thumbs[ndx][j] = DualRes.rescale(image, thumbSize);
                dialog.setPercent(
                  100 * (slices * i + j + 1) / (timesteps * slices));
              }
            }
          }
          else {
            // load data at current index only
            timesteps = f.length;
            field = loadData(f[curfile], true);
            if (field == null) return;
            slices = field.getLength();
            dialog.setPercent(100);
          }
          if (field == null) return;

          hasThumbs = doThumbs;
          autoSwitch = hasThumbs;

          dialog.setText("Analyzing data");

          // The FieldImpl must be in one of the following forms:
          //     (index -> ((x, y) -> range))
          //     (index -> ((x, y) -> (r1, r2, ..., rn)))
          //
          // dtypes = {x, y, index}; rtypes = {r1, r2, ..., rn}

          // extract types
          FunctionType time_function = (FunctionType) field.getType();
          RealTupleType time_domain = time_function.getDomain();
          MathType time_range = time_function.getRange();
          if (time_domain.getDimension() > 1 ||
            !(time_range instanceof FunctionType))
          {
            throw new VisADException("Field is not an image stack");
          }
          RealType slice_type = (RealType) time_domain.getComponent(0);
          FunctionType image_function = (FunctionType) time_range;
          domain2 = image_function.getDomain();
          RealType[] image_dtypes = domain2.getRealComponents();
          if (image_dtypes.length < 2) {
            throw new VisADException("Data stack does not contain images");
          }
          dtypes = new RealType[] {
            image_dtypes[0], image_dtypes[1], slice_type
          };
          domain3 = new RealTupleType(dtypes);
          MathType range = image_function.getRange();
          if (!(range instanceof RealTupleType) &&
            !(range instanceof RealType))
          {
            throw new VisADException("Invalid field range");
          }
          rtypes = range instanceof RealTupleType ?
            ((RealTupleType) range).getRealComponents() :
            new RealType[] {(RealType) range};

          // convert thumbnails into animation stacks
          lowresField = null;
          if (doThumbs) {
            FunctionType slice_function =
              new FunctionType(slice_type, image_function);
            FunctionType lowres_function =
              new FunctionType(TIME_TYPE, slice_function);
            lowresField = new FieldImpl(lowres_function,
              new Integer1DSet(TIME_TYPE, timesteps));
            Set lowres_set = new Integer1DSet(slice_type, slices);
            for (int j=0; j<timesteps; j++) {
              FieldImpl step = new FieldImpl(slice_function, lowres_set);
              step.setSamples(thumbs[j], false);
              lowresField.setSample(j, step, false);
            }
          }

          dialog.setText("Configuring displays");

          // set new data
          ref2.setData(field);
          ref3.setData(field);
          if (doThumbs) {
            lowresRef2.setData(lowresField);
            lowresRef3.setData(lowresField);
          }

          bio.toolView.guessTypes();
          configureDisplays();

          // initialize measurement list array
          bio.mm.initLists(timesteps);
        }
        catch (VisADException exc) { dialog.setException(exc); }
        catch (RemoteException exc) {
          dialog.setException(
            new VisADException("RemoteException: " + exc.getMessage()));
        }

        bio.display2.enableAction();
        if (bio.display3 != null) bio.display3.enableAction();
        dialog.kill();
      }
    });
    t.start();
    dialog.show();
    try { dialog.checkException(); }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(bio,
        "Cannot import data from " + files[index].getName() + "\n" +
        exc.getMessage(), "Cannot load file", JOptionPane.ERROR_MESSAGE);
      throw exc;
    }
  }

  /**
   * Loads the data from the given file, and ensures that the
   * resulting data object is of the proper form, converting
   * image data into single-slice stack data if specified.
   */
  private FieldImpl loadData(File file, boolean makeStack)
    throws VisADException, RemoteException
  {
    // load data from file
    Data data = loader.open(file.getPath());

    // convert data to field
    FieldImpl f = null;
    if (data instanceof FieldImpl) f = (FieldImpl) data;
    else if (data instanceof Tuple) {
      Tuple tuple = (Tuple) data;
      Data[] d = tuple.getComponents();
      for (int i=0; i<d.length; i++) {
        if (d[i] instanceof FieldImpl) {
          f = (FieldImpl) d[i];
          break;
        }
      }
    }

    // convert single image to single-slice stack
    FieldImpl stack = f;
    if (f instanceof FlatField && makeStack) {
      FunctionType func = new FunctionType(SLICE_TYPE, f.getType());
      stack = new FieldImpl(func, new Integer1DSet(1));
      stack.setSample(0, f, false);
    }
    return stack;
  }

  /** Sets the current file to match the current index. */
  private void setFile(boolean initialize)
    throws VisADException, RemoteException
  {
    bio.setWaitCursor(true);
    try {
      if (initialize) init(files, 0);
      else if (!filesAsSlices) {
        // dump old dataset (for garbage collection)
        field = new FieldImpl((FunctionType) field.getType(),
          field.getDomainSet());
        ref2.setData(field);
        ref3.setData(field);
        System.gc();

        // load new data
        field = loadData(files[index], true);
        collapsedField = null;
        if (field != null) {
          ref2.setData(field);
          ref3.setData(field);
        }
        else {
          bio.setWaitCursor(false);
          JOptionPane.showMessageDialog(bio,
            files[index].getName() + " does not contain an image stack",
            "Cannot load file", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    finally {
      bio.setWaitCursor(false);
    }
  }

  /** Clears display mappings and references. */
  private void clearDisplays() throws VisADException, RemoteException {
    bio.display2.removeAllReferences();
    bio.display2.clearMaps();
    if (bio.display3 != null) {
      bio.display3.removeAllReferences();
      bio.display3.clearMaps();
    }
  }

  /** Configures display mappings and references. */
  private void configureDisplays() throws VisADException, RemoteException {
    // set up mappings to 2-D display
    x_map2 = new ScalarMap(dtypes[0], Display.XAxis);
    y_map2 = new ScalarMap(dtypes[1], Display.YAxis);
    ScalarMap slice_map2 = new ScalarMap(dtypes[2], Display.SelectValue);
    ScalarMap anim_map2 = null;
    ScalarMap r_map2 = new ScalarMap(RED_TYPE, Display.Red);
    ScalarMap g_map2 = new ScalarMap(GREEN_TYPE, Display.Green);
    ScalarMap b_map2 = new ScalarMap(BLUE_TYPE, Display.Blue);
    bio.display2.addMap(x_map2);
    bio.display2.addMap(y_map2);
    bio.display2.addMap(slice_map2);
    if (hasThumbs) {
      anim_map2 = new ScalarMap(TIME_TYPE, Display.Animation);
      bio.display2.addMap(anim_map2);
    }
    bio.display2.addMap(r_map2);
    bio.display2.addMap(g_map2);
    bio.display2.addMap(b_map2);

    // add color maps for all range components
    rmaps2 = new ScalarMap[rtypes.length];
    for (int i=0; i<rtypes.length; i++) {
      rmaps2[i] = new ScalarMap(rtypes[i], Display.RGB);
      bio.display2.addMap(rmaps2[i]);
    }

    // set up 2-D data references
    DisplayRenderer dr2 = bio.display2.getDisplayRenderer();
    boolean on = renderer2 == null ? true : renderer2.getEnabled();
    renderer2 = dr2.makeDefaultRenderer();
    renderer2.toggle(on);
    bio.display2.addReferences(renderer2, ref2);
    on = planeRenderer2 == null ? false : planeRenderer2.getEnabled();
    planeRenderer2 = dr2.makeDefaultRenderer();
    planeRenderer2.suppressExceptions(true);
    planeRenderer2.toggle(on);
    bio.display2.addReferences(planeRenderer2, planeRef);
    if (hasThumbs) {
      on = lowresRenderer2 == null ? false : lowresRenderer2.getEnabled();
      lowresRenderer2 = dr2.makeDefaultRenderer();
      lowresRenderer2.toggle(on);
      bio.display2.addReferences(lowresRenderer2, lowresRef2);
    }
    bio.mm.pool2.init();

    // set up mappings to 3-D display
    ScalarMap x_map3 = null;
    ScalarMap y_map3 = null;
    ScalarMap z_map3a = null;
    ScalarMap z_map3b = null;
    ScalarMap anim_map3 = null;
    ScalarMap r_map3 = null;
    ScalarMap g_map3 = null;
    ScalarMap b_map3 = null;
    DisplayRenderer dr3 = null;
    if (bio.display3 != null) {
      x_map3 = new ScalarMap(dtypes[0], Display.XAxis);
      y_map3 = new ScalarMap(dtypes[1], Display.YAxis);
      z_map3a = new ScalarMap(dtypes[2], Display.ZAxis);
      z_map3b = new ScalarMap(Z_TYPE, Display.ZAxis);
      if (hasThumbs) anim_map3 = new ScalarMap(TIME_TYPE, Display.Animation);
      r_map3 = new ScalarMap(RED_TYPE, Display.Red);
      g_map3 = new ScalarMap(GREEN_TYPE, Display.Green);
      b_map3 = new ScalarMap(BLUE_TYPE, Display.Blue);
      bio.display3.addMap(x_map3);
      bio.display3.addMap(y_map3);
      bio.display3.addMap(z_map3a);
      bio.display3.addMap(z_map3b);
      if (hasThumbs) bio.display3.addMap(anim_map3);
      bio.display3.addMap(r_map3);
      bio.display3.addMap(g_map3);
      bio.display3.addMap(b_map3);

      // add color maps for all range components
      rmaps3 = new ScalarMap[rtypes.length];
      for (int i=0; i<rtypes.length; i++) {
        rmaps3[i] = new ScalarMap(rtypes[i], Display.RGBA);
        bio.display3.addMap(rmaps3[i]);
      }

      // set up 3-D data references
      dr3 = bio.display3.getDisplayRenderer();
      on = renderer3 == null ? true : renderer3.getEnabled();
      renderer3 = bio.display3.getDisplayRenderer().makeDefaultRenderer();
      renderer3.toggle(on);
      bio.display3.addReferences(renderer3, ref3);
      if (hasThumbs) {
        on = lowresRenderer3 == null ? false : lowresRenderer3.getEnabled();
        lowresRenderer3 = dr3.makeDefaultRenderer();
        lowresRenderer3.toggle(on);
        bio.display3.addReferences(lowresRenderer3, lowresRef3);
      }
      bio.mm.pool3.init();
    }

    // set up 2-D ranges
    GriddedSet set = (GriddedSet)
      ((FieldImpl) field.getSample(0)).getDomainSet();
    float[] lo = set.getLow();
    float[] hi = set.getHi();
    int[] lengths = set.getLengths();
    res_x = lengths[0];
    res_y = lengths[1];

    // x-axis range
    min_x = lo[0];
    max_x = hi[0];
    if (min_x != min_x) min_x = 0;
    if (max_x != max_x) max_x = 0;
    x_map2.setRange(min_x, max_x);

    // y-axis range
    min_y = lo[1];
    max_y = hi[1];
    if (min_y != min_y) min_y = 0;
    if (max_y != max_y) max_y = 0;
    y_map2.setRange(min_y, max_y);

    // select value range
    min_z = 0;
    max_z = slices - 1;
    slice_map2.setRange(min_z, max_z);

    // color ranges
    r_map2.setRange(0, 255);
    g_map2.setRange(0, 255);
    b_map2.setRange(0, 255);

    // set up 3-D ranges
    if (bio.display3 != null) {
      // x-axis and y-axis ranges
      x_map3.setRange(min_x, max_x);
      y_map3.setRange(min_y, max_y);

      // z-axis range
      z_map3a.setRange(min_z, max_z);
      z_map3b.setRange(min_z, max_z);

      // color ranges
      r_map3.setRange(0, 255);
      g_map3.setRange(0, 255);
      b_map3.setRange(0, 255);
    }

    // set up animation mapping
    if (value_control2 != null) value_control2.removeControlListener(this);
    if (anim_control2 != null) anim_control2.removeControlListener(this);
    value_control2 = (ValueControl) slice_map2.getControl();
    if (hasThumbs) {
      anim_control2 = (AnimationControl) anim_map2.getControl();
      bio.toolView.setControl(anim_control2);
      anim_control2.addControlListener(this);
      if (bio.display3 != null) {
        anim_control3 = (AnimationControl) anim_map3.getControl();
      }
    }
    value_control2.addControlListener(this);

    // initialize plane selector
    if (bio.display3 != null) {
      if (ps == null) {
        ps = new PlaneSelector(bio.display3);
        ps.addListener(this);
      }
      ps.init(dtypes[0], dtypes[1], dtypes[2],
        min_x, min_y, min_z, max_x, max_y, max_z);
    }

    // adjust display aspect ratio
    bio.setAspect(res_x, res_y, Double.NaN);

    // set up display listeners
    bio.display2.addDisplayListener(this);
    bio.display3.addDisplayListener(this);

    // set up color table characteristics
    bio.toolView.doColorTable();

    // update arbitrary slice range with a reasonable default setting
    double range_x = max_x - min_x;
    double range_y = max_y - min_y;
    bio.toolView.setSliceRange((int) range_x + 1, (int) range_y + 1);
  }

  /** Refreshes the current image slice shown onscreen. */
  private void refresh(boolean new_slice, boolean new_index) {
    if (files == null) return;

    // switch index values
    if (new_index) {
      if (!lowres) {
        try { setFile(false); }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
      MeasureList list = bio.mm.lists[index];
      bio.mm.pool2.set(list);
      if (bio.mm.pool3 != null) bio.mm.pool3.set(list);
      updateAnimationControls();
    }

    // switch slice values
    if (new_slice) {
      bio.mm.pool2.setSlice(slice);

      // update value control
      try { value_control2.setValue(slice); }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }

    // switch resolution in 2-D display
    if (planeSelect) {
      collapsedField = null;
      updateSlice();
    }
    else if (lowres) {
      if (hasThumbs) lowresRenderer2.toggle(true);
      renderer2.toggle(false);
    }
    else {
      renderer2.toggle(true);
      if (hasThumbs) lowresRenderer2.toggle(false);
    }

    // switch resolution in 3-D display
    if (bio.display3 != null) {
      if (lowres) {
        if (hasThumbs) lowresRenderer3.toggle(true);
        renderer3.toggle(false);
      }
      else {
        renderer3.toggle(true);
        if (hasThumbs) lowresRenderer3.toggle(false);
      }
    }
  }

  /** Updates the animation controls. */
  private void updateAnimationControls() {
    try {
      if (anim_control2 != null) anim_control2.setCurrent(index);
      if (anim_control3 != null) anim_control3.setCurrent(index);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Updates 2-D display of arbitrary plane slice. */
  private void updateSlice() {
    bio.setWaitCursor(true);
    try {
      if (collapsedField == null) {
        FieldImpl f = lowres ?
          (FieldImpl) lowresField.getSample(index) : field;
        collapsedField = (FieldImpl) f.domainMultiply();
      }
      planeRef.setData(ps.extractSlice(collapsedField,
        sliceRes_x, sliceRes_y, res_x, res_y));
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    bio.setWaitCursor(false);
  }

}
