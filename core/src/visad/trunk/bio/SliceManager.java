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

import java.io.File;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;
import visad.*;
import visad.data.DefaultFamily;
import visad.util.DualRes;

/** SliceManager is the class encapsulating BioVisAD's slice logic. */
public class SliceManager implements ControlListener {

  // -- DATA TYPE CONSTANTS --

  /** RealType for mapping measurements to Z axis. */
  static final RealType Z_TYPE = RealType.getRealType("bio_line_z");

  /** RealType for mapping timestep values to animation. */
  private static final RealType TIME_TYPE = RealType.getRealType("bio_time");

  /** RealType for mapping to Red. */
  private static final RealType RED_TYPE = RealType.getRealType("bio_red");

  /** RealType for mapping to Green. */
  private static final RealType GREEN_TYPE = RealType.getRealType("bio_green");

  /** RealType for mapping to Blue. */
  private static final RealType BLUE_TYPE = RealType.getRealType("bio_blue");


  // -- MEMORY ALLOCATION CONSTANTS --

  /** Number of megabytes reserved for objects apart from image data. */
  private static final int RESERVED = 16;

  /** Number of bytes in a megabyte. */
  private static final int MEGA = 1 << 20;

  /** Number of bytes in a single image pixel. */
  private static final int BYTES_PER_PIXEL = 8; // double = 64 bits


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

  /** X and Y range of images. */
  double xRange, yRange;


  // -- SLICE CONTROLS --

  /** Animation control associated with 2-D animation mapping. */
  AnimationControl anim_control2;

  /** Animation control associated with 3-D animation mapping. */
  AnimationControl anim_control3;

  /** Value control associated with 2-D select value mapping. */
  ValueControl value_control2;


  // -- DISPLAY MAPPING INFORMATION --

  /** High-resolution field for current timestep and slice number. */
  private FieldImpl field;

  /** List of range component mappings for 2-D display. */
  private ScalarMap[] rmaps2;

  /** List of range component mappings for 3-D display. */
  private ScalarMap[] rmaps3;


  // -- DATA REFERENCES --

  /** Reference for image stack data. */
  private DataReferenceImpl ref;

  /** Data renderer for 2-D image stack data. */
  private DataRenderer renderer2;

  /** Data renderer for 3-D image stack data. */
  private DataRenderer renderer3;

  /** References for low-resolution image timestack data. */
  private DataReferenceImpl lowresRef;

  /** Data renderer for low-resolution image timestack data in 2-D. */
  private DataRenderer lowresRenderer2;

  /** Data renderer for low-resolution image timestack data in 3-D. */
  private DataRenderer lowresRenderer3;



  // -- THUMBNAIL-RELATED FIELDS --

  /** Maximum memory to use for low-resolution thumbnails, in megabytes. */
  private int thumbSize;

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

  /** List of files containing current data series. */
  private File[] files;

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

  /** Loader for opening data series. */
  private final DefaultFamily loader = new DefaultFamily("bio_loader");


  // -- CONSTRUCTORS --

  /** Constructs a slice manager. */
  public SliceManager(BioVisAD biovis, int thumbSize)
    throws VisADException, RemoteException
  {
    bio = biovis;
    this.thumbSize = thumbSize;
    lowres = false;
    doThumbs = true;
    autoSwitch = true;
    colorRange = new RealTupleType(
      new RealType[] {RED_TYPE, GREEN_TYPE, BLUE_TYPE});

    // image stack references
    ref = new DataReferenceImpl("bio_ref");
    lowresRef = new DataReferenceImpl("bio_lowres");
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
  public void setThumbnails(boolean thumbnails) { doThumbs = thumbnails; }

  /** Links the data series to the given list of files. */
  public void setSeries(File[] files) {
    this.files = files;
    setFile(0, true);
    bio.horiz.updateSlider(timesteps);
    bio.vert.updateSlider(slices);
  }


  // -- INTERNAL API METHODS --

  /** ControlListener method used for programmatically updating GUI. */
  public void controlChanged(ControlEvent e) {
    int index = anim_control2.getCurrent();
    if (this.index != index) bio.horiz.setValue(index + 1);
    int slice = (int) value_control2.getValue();
    if (this.slice != slice) bio.vert.setValue(slice + 1);
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

  /** Gets the color controls for 2-D range type color mappings. */
  ColorControl[] getColorControls2D() {
    if (rmaps2 == null) return null;
    ColorControl[] controls = new ColorControl[rmaps2.length];
    for (int i=0; i<rmaps2.length; i++) {
      controls[i] = (ColorControl) rmaps2[i].getControl();
    }
    return controls;
  }

  /** Gets the color controls for 3-D range type color mappings. */
  ColorControl[] getColorControls3D() {
    if (rmaps3 == null) return null;
    ColorControl[] controls = new ColorControl[rmaps3.length];
    for (int i=0; i<rmaps3.length; i++) {
      controls[i] = (ColorControl) rmaps3[i].getControl();
    }
    return controls;
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

          // create low-resolution thumbnails for timestep animation
          field = null;
          FieldImpl[][] thumbs = null;
          timesteps = f.length;
          double scale = Double.NaN;
          for (int i=0; i<timesteps; i++) {
            // set up index so that current timestep is done last
            if (!doThumbs) {
              // no need to create thumbnails; skip to proper timestep
              i = timesteps - 1;
            }
            int ndx = i == timesteps - 1 ? curfile :
              (i >= curfile ? i + 1 : i);
            field = loadData(f[ndx]);
            if (field == null) {
              throw new VisADException(f[ndx].getName() +
                " does not contain valid image stack data");
            }
            if (thumbs == null) {
              slices = field.getLength();
              mode_index = mode_slice = 0;
            }
            if (!doThumbs) {
              // no need to create thumbnails; done loading
              dialog.setPercent(100);
              break;
            }
            if (thumbs == null) thumbs = new FieldImpl[timesteps][slices];
            for (int j=0; j<slices; j++) {
              FieldImpl image = (FieldImpl) field.getSample(j);
              if (scale != scale) {
                // compute scale-down factor
                GriddedSet set = (GriddedSet) image.getDomainSet();
                int[] len = set.getLengths();
/*
                long tsBytes = BYTES_PER_PIXEL * slices * len[0] * len[1];
                long freeBytes = MEGA * (heapSize - RESERVED) - tsBytes;
                freeBytes /= 4; // use quarter available, for safety (TEMP?)
                if (freeBytes < BYTES_PER_PIXEL * timesteps * slices) {
                  throw new VisADException("Insufficient memory " +
                    "to compute image slice thumbnails");
                }
                scale = Math.sqrt((double) freeBytes / (timesteps * tsBytes));
*/
                long freeBytes = MEGA * thumbSize;
                long fullBytes = BYTES_PER_PIXEL *
                  slices * timesteps * len[0] * len[1];
                scale = Math.sqrt((double) freeBytes / fullBytes);
                if (scale > 0.5) scale = 0.5;
              }
              thumbs[ndx][j] = DualRes.rescale(image, scale);
              dialog.setPercent(
                100 * (slices * i + j + 1) / (timesteps * slices));
            }
          }
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
          FieldImpl lowresField = null;
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
              lowresField.setSample(j, step);
            }
          }

          dialog.setText("Configuring displays");

          // set new data
          ref.setData(field);
          if (doThumbs) lowresRef.setData(lowresField);

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
    dialog.checkException();
  }

  /**
   * Loads the data from the given file, and ensures
   * that the given data object is of the proper form.
   */
  private FieldImpl loadData(File file) {
    // load data from file
    Data data = null;
    try { data = loader.open(file.getPath()); }
    catch (VisADException exc) { exc.printStackTrace(); }

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
    if (f instanceof FlatField) {
      try {
        FunctionType func = new FunctionType(
          RealType.getRealType("slice"), f.getType());
        stack = new FieldImpl(func, new Integer1DSet(1));
        stack.setSample(0, f);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
    return stack;
  }

  /** Sets the given file as the current one. */
  private void setFile(int curFile, boolean initialize) {
    bio.setWaitCursor(true);
    try {
      if (initialize) init(files, 0);
      else {
        field = loadData(files[curFile]);
        if (field != null) ref.setData(field);
        else {
          bio.setWaitCursor(false);
          JOptionPane.showMessageDialog(bio,
            files[curFile].getName() + " does not contain an image stack",
            "Cannot load file", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    bio.setWaitCursor(false);
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
    ScalarMap x_map2 = new ScalarMap(dtypes[0], Display.XAxis);
    ScalarMap y_map2 = new ScalarMap(dtypes[1], Display.YAxis);
    ScalarMap slice_map2 = new ScalarMap(dtypes[2], Display.SelectValue);
    ScalarMap anim_map2 = new ScalarMap(TIME_TYPE, Display.Animation);
    ScalarMap r_map2 = new ScalarMap(RED_TYPE, Display.Red);
    ScalarMap g_map2 = new ScalarMap(GREEN_TYPE, Display.Green);
    ScalarMap b_map2 = new ScalarMap(BLUE_TYPE, Display.Blue);
    bio.display2.addMap(x_map2);
    bio.display2.addMap(y_map2);
    bio.display2.addMap(slice_map2);
    bio.display2.addMap(anim_map2);
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
    DisplayRenderer dr = bio.display2.getDisplayRenderer();
    boolean on = renderer2 == null ? true : renderer2.getEnabled();
    renderer2 = dr.makeDefaultRenderer();
    renderer2.toggle(on);
    bio.display2.addReferences(renderer2, ref);
    if (hasThumbs) {
      on = lowresRenderer2 == null ? false : lowresRenderer2.getEnabled();
      lowresRenderer2 = dr.makeDefaultRenderer();
      lowresRenderer2.toggle(on);
      bio.display2.addReferences(lowresRenderer2, lowresRef);
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
    if (bio.display3 != null) {
      x_map3 = new ScalarMap(dtypes[0], Display.XAxis);
      y_map3 = new ScalarMap(dtypes[1], Display.YAxis);
      z_map3a = new ScalarMap(dtypes[2], Display.ZAxis);
      z_map3b = new ScalarMap(Z_TYPE, Display.ZAxis);
      anim_map3 = new ScalarMap(TIME_TYPE, Display.Animation);
      r_map3 = new ScalarMap(RED_TYPE, Display.Red);
      g_map3 = new ScalarMap(GREEN_TYPE, Display.Green);
      b_map3 = new ScalarMap(BLUE_TYPE, Display.Blue);
      bio.display3.addMap(x_map3);
      bio.display3.addMap(y_map3);
      bio.display3.addMap(z_map3a);
      bio.display3.addMap(z_map3b);
      bio.display3.addMap(anim_map3);
      bio.display3.addMap(r_map3);
      bio.display3.addMap(g_map3);
      bio.display3.addMap(b_map3);

      // add color maps for all range components
      rmaps3 = new ScalarMap[rtypes.length];
      for (int i=0; i<rtypes.length; i++) {
        rmaps3[i] = new ScalarMap(rtypes[i], Display.RGB);
        bio.display3.addMap(rmaps3[i]);
      }

      // set up 3-D data references
      on = renderer3 == null ? true : renderer3.getEnabled();
      renderer3 = bio.display3.getDisplayRenderer().makeDefaultRenderer();
      renderer3.toggle(on);
      bio.display3.addReferences(renderer3, ref);
      if (hasThumbs) {
        on = lowresRenderer3 == null ? false : lowresRenderer3.getEnabled();
        lowresRenderer3 = dr.makeDefaultRenderer();
        lowresRenderer3.toggle(on);
        bio.display3.addReferences(lowresRenderer3, lowresRef);
      }
      bio.mm.pool3.init();
    }

    // set up 2-D ranges
    SampledSet set = (SampledSet)
      ((FieldImpl) field.getSample(0)).getDomainSet();
    float[] lo = set.getLow();
    float[] hi = set.getHi();

    // x-axis range
    float min_x = lo[0];
    float max_x = hi[0];
    xRange = Math.abs(max_x - min_x);
    if (min_x != min_x) min_x = 0;
    if (max_x != max_x) max_x = 0;
    x_map2.setRange(min_x, max_x);

    // y-axis range
    float min_y = lo[1];
    float max_y = hi[1];
    yRange = Math.abs(max_y - min_y);
    if (min_y != min_y) min_y = 0;
    if (max_y != max_y) max_y = 0;
    y_map2.setRange(min_y, max_y);

    // select value range
    float min_z = 0;
    float max_z = slices - 1;
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
    anim_control2 = (AnimationControl) anim_map2.getControl();
    if (bio.display3 != null) {
      anim_control3 = (AnimationControl) anim_map3.getControl();
    }
    bio.toolView.setControl(anim_control2);
    value_control2.addControlListener(this);
    anim_control2.addControlListener(this);

    // set up color table characteristics
    bio.toolView.doColorTable();
  }

  /** Refreshes the current image slice shown onscreen. */
  private void refresh(boolean new_slice, boolean new_index) {
    if (files == null || anim_control2 == null) return;

    // switch index values
    if (new_index) {
      if (!lowres) setFile(index, false);
      Measurement[] m = bio.mm.lists[index].getMeasurements();
      bio.mm.pool2.set(m);
      if (bio.mm.pool3 != null) bio.mm.pool3.set(m);
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

    // switch resolution
    if (lowres) {
      if (hasThumbs) {
        lowresRenderer2.toggle(true);
        if (bio.display3 != null) lowresRenderer3.toggle(true);
      }
      renderer2.toggle(false);
      if (bio.display3 != null) renderer3.toggle(false);
    }
    else {
      renderer2.toggle(true);
      if (bio.display3 != null) renderer3.toggle(true);
      if (hasThumbs) {
        lowresRenderer2.toggle(false);
        if (bio.display3 != null) lowresRenderer3.toggle(false);
      }
    }
  }

  /** Updates the animation controls. */
  private void updateAnimationControls() {
    // update animation controls
    try {
      anim_control2.setCurrent(index);
      if (anim_control3 != null) anim_control3.setCurrent(index);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

}
