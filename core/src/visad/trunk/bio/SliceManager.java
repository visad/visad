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
public class SliceManager {

  // -- DATA TYPE CONSTANTS --

  /** RealType for mapping measurements to Z axis. */
  static final RealType Z_TYPE = RealType.getRealType("bio_line_z");

  /** RealType for mapping to Red. */
  private static final RealType RED_TYPE = RealType.getRealType("bio_red");

  /** RealType for mapping to Green. */
  private static final RealType GREEN_TYPE = RealType.getRealType("bio_green");

  /** RealType for mapping to Blue. */
  private static final RealType BLUE_TYPE = RealType.getRealType("bio_blue");


  // -- MEMORY ALLOCATION CONSTANTS --

  /** Number of megabytes reserved for objects apart from image data. */
  private static final int RESERVED = 24;

  /** Number of bytes in a megabyte. */
  private static final int MEGA = 1024 * 1024;

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

  /** X and Y range of images. */
  double xRange, yRange;


  // -- DATA REFERENCES --

  /** Reference for image stack data. */
  private DataReferenceImpl ref;

  /** Data renderer for image stack data. */
  private DataRenderer renderer;

  /** References for low-resolution image timestack data. */
  private DataReferenceImpl[] lowresRefs;

  /** Data renderers for low-resolution image timestack data. */
  private DataRenderer[] lowresRenderers;


  // -- OTHER FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Maximum heap size in megabytes. */
  private int heapSize;

  /** Loader for opening data series. */
  private final DefaultFamily loader = new DefaultFamily("bio_loader");

  /** List of files containing current data series. */
  private File[] files;



  // -- CONSTRUCTORS --

  /** Constructs a slice manager. */
  public SliceManager(BioVisAD biovis, int heapSize)
    throws VisADException, RemoteException
  {
    bio = biovis;
    this.heapSize = heapSize;
    colorRange = new RealTupleType(
      new RealType[] {RED_TYPE, GREEN_TYPE, BLUE_TYPE});

    // image stack reference
    ref = new DataReferenceImpl("bio_ref");
  }


  // -- API METHODS --

  /** Gets the currently displayed timestep index. */
  public int getIndex() { return bio.horiz.getValue() - 1; }

  /** Gets the currently displayed image slice. */
  public int getSlice() { return bio.vert.getValue() - 1; }

  /** Gets the number of timestep indices. */
  public int getNumberOfIndices() { return bio.horiz.getMaximum(); }

  /** Gets the number of image slices. */
  public int getNumberOfSlices() { return bio.vert.getMaximum(); }

  /** Sets the display detail (low-resolution or full resolution). */
  public void setMode(boolean lowres) {
    // CTR - TODO - called by lo-res/hi-res toggles
  }

  /** Sets the currently displayed timestep index. */
  public void setIndex(int index) {
    if (files == null) return;
    setFile(index, false);
    Measurement[] m = bio.mm.lists[index].getMeasurements();
    bio.mm.pool2.set(m);
    bio.mm.pool3.set(m);
  }

  /** Sets the currently displayed image slice. */
  public void setSlice(int slice) {
    // CTR - TODO - called by ImageStackWidget
  }

  /** Links the data series to the given list of files. */
  public void setSeries(File[] files) {
    this.files = files;
    setFile(0, true);
    bio.horiz.updateSlider(files == null ? 0 : files.length);
  }

  /** Sets the displays to use the image stack timestep from the given file. */
  public boolean setData(File file) throws VisADException, RemoteException {
    FieldImpl field = loadData(file);
    if (field == null) return false;
    ref.setData(field);
    return true;
  }

  /**
   * Initializes the displays to use the image stack data
   * from the given files.
   */
  public void init(File[] files, int index) throws VisADException {
    final File[] f = files;
    final int curfile = index;
    final ProgressDialog dialog =
      new ProgressDialog(bio, "Loading data and creating thumbnails");

    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          // clear old displays
          bio.display2.removeAllReferences();
          bio.display2.clearMaps();
          if (bio.display3 != null) {
            bio.display3.removeAllReferences();
            bio.display3.clearMaps();
          }

          // reset measurements
          if (bio.mm.lists != null) bio.mm.clear();

          // create low-res thumbnails for timestep animation
          FieldImpl field = null;
          FieldImpl[][] thumbs = null;
          int slices = 0;
          int timesteps = f.length;
          double scale = Double.NaN;
          for (int i=0; i<timesteps; i++) {
            // set up index so that current timestep is done last
            int ndx = i == timesteps - 1 ?
              curfile : (i >= curfile ? i + 1 : i);
            field = loadData(f[ndx]);
            if (field == null) {
              throw new VisADException(f[ndx].getName() +
                " does not contain valid image stack data");
            }
            if (thumbs == null) {
              slices = field.getLength();
              thumbs = new FieldImpl[slices][timesteps];
            }
            for (int j=0; j<slices; j++) {
              FieldImpl image = (FieldImpl) field.getSample(j);
              if (scale != scale) {
                // compute scale-down factor
                GriddedSet set = (GriddedSet) image.getDomainSet();
                int[] len = set.getLengths();
                int tsBytes = BYTES_PER_PIXEL * slices * len[0] * len[1];
                int freeBytes = MEGA * (heapSize - RESERVED) - tsBytes;
                // CTR - TODO - probably warn if out of memory here
                scale = Math.sqrt((double) freeBytes / (timesteps * tsBytes));
                if (scale > 1) scale = 1;
                /* CTR - TEMP */ //System.out.println("scale=" + scale);
              }
              thumbs[j][ndx] = DualRes.rescale(image, scale);
              dialog.setPercent(
                100 * (slices * i + j + 1) / (timesteps * slices));
            }
          }

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
          RealType time_slice = (RealType) time_domain.getComponent(0);
          FunctionType image_function = (FunctionType) time_range;
          domain2 = image_function.getDomain();
          RealType[] image_dtypes = domain2.getRealComponents();
          if (image_dtypes.length < 2) {
            throw new VisADException("Data stack does not contain images");
          }
          dtypes = new RealType[] {
            image_dtypes[0], image_dtypes[1], time_slice
          };
          domain3 = new RealTupleType(dtypes);
          MathType range = image_function.getRange();
          if (!(range instanceof RealTupleType) &&
            !(range instanceof RealType))
          {
            throw new VisADException("Invalid field range");
          }
          dtypes = domain3.getRealComponents();
          RealType[] rtypes = range instanceof RealTupleType ?
            ((RealTupleType) range).getRealComponents() :
            new RealType[] {(RealType) range};

          // convert thumbnails into animation stacks
          Set lowres_set = new Integer1DSet(time_slice, timesteps);
          FieldImpl[] lowres = new FieldImpl[slices];
          lowresRefs = new DataReferenceImpl[slices];
          lowresRenderers = new DataRenderer[slices];
          DisplayRenderer dr = bio.display2.getDisplayRenderer();
          for (int j=0; j<slices; j++) {
            lowres[j] = new FieldImpl(time_function, lowres_set);
            lowres[j].setSamples(thumbs[j], false);
            lowresRefs[j] = new DataReferenceImpl("bio_lowres" + j);
            lowresRenderers[j] = dr.makeDefaultRenderer();
            lowresRenderers[j].toggle(false);
          }

          dialog.setText("Configuring displays");

          // set new data
          ref.setData(field);
          for (int j=0; j<slices; j++) lowresRefs[j].setData(lowres[j]);

          // set up mappings to 2-D display
          ScalarMap x_map2 = new ScalarMap(dtypes[0], Display.XAxis);
          ScalarMap y_map2 = new ScalarMap(dtypes[1], Display.YAxis);
          ScalarMap anim_map = new ScalarMap(time_slice, Display.Animation);
          ScalarMap r_map2 = new ScalarMap(RED_TYPE, Display.Red);
          ScalarMap g_map2 = new ScalarMap(GREEN_TYPE, Display.Green);
          ScalarMap b_map2 = new ScalarMap(BLUE_TYPE, Display.Blue);
          bio.display2.addMap(x_map2);
          bio.display2.addMap(y_map2);
          bio.display2.addMap(anim_map);
          bio.display2.addMap(r_map2);
          bio.display2.addMap(g_map2);
          bio.display2.addMap(b_map2);

          // CTR - TODO - full range component color support
          bio.display2.addMap(new ScalarMap(rtypes[0], Display.RGB));

          // set up 2-D data references
          renderer = bio.display2.getDisplayRenderer().makeDefaultRenderer();
          bio.display2.addReferences(renderer, ref);
          for (int j=0; j<slices; j++) {
            bio.display2.addReferences(lowresRenderers[j], lowresRefs[j]);
          }
          bio.mm.pool2.init();

          // set up mappings to 3-D display
          ScalarMap x_map3 = null, y_map3 = null,
            z_map3a = null, z_map3b = null;
          ScalarMap r_map3 = null, g_map3 = null, b_map3 = null;
          if (bio.display3 != null) {
            x_map3 = new ScalarMap(dtypes[0], Display.XAxis);
            y_map3 = new ScalarMap(dtypes[1], Display.YAxis);
            z_map3a = new ScalarMap(time_slice, Display.ZAxis);
            z_map3b = new ScalarMap(Z_TYPE, Display.ZAxis);
            r_map3 = new ScalarMap(RED_TYPE, Display.Red);
            g_map3 = new ScalarMap(GREEN_TYPE, Display.Green);
            b_map3 = new ScalarMap(BLUE_TYPE, Display.Blue);
            bio.display3.addMap(x_map3);
            bio.display3.addMap(y_map3);
            bio.display3.addMap(z_map3a);
            bio.display3.addMap(z_map3b);
            bio.display3.addMap(r_map3);
            bio.display3.addMap(g_map3);
            bio.display3.addMap(b_map3);

            // CTR - TODO - full range component color support
            bio.display3.addMap(new ScalarMap(rtypes[0], Display.RGB));

            // set up 3-D data references
            bio.display3.addReference(ref);
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

          // animation range
          float min_z = 0;
          float max_z = field.getLength() - 1;
          if (min_z != min_z) min_z = 0;
          if (max_z != max_z) max_z = 0;
          anim_map.setRange(min_z, max_z);

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

          bio.vert.setMap(anim_map);
          bio.toolView.doColorTable();

          // initialize measurement list array
          bio.mm.initLists(timesteps);
        }
        catch (VisADException exc) { dialog.setException(exc); }
        catch (RemoteException exc) {
          dialog.setException(
            new VisADException("RemoteException: " + exc.getMessage()));
        }

        dialog.kill();
      }
    });
    t.start();
    dialog.show();
    dialog.checkException();
  }


  // -- HELPER METHODS --

  /** Refreshes the current image slice shown onscreen. */
  private void refreshSlice(boolean lowres) {
    if (lowres) {
      int slice = getSlice();
      lowresRenderers[slice].toggle(true);
      for (int i=0; i<lowresRenderers.length; i++) {
        if (i == slice) continue;
        lowresRenderers[i].toggle(false);
      }
      renderer.toggle(false);
    }
    else {
      renderer.toggle(true);
      for (int i=0; i<lowresRenderers.length; i++) {
        lowresRenderers[i].toggle(false);
      }
    }
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
    FieldImpl field = null;
    if (data instanceof FieldImpl) field = (FieldImpl) data;
    else if (data instanceof Tuple) {
      Tuple tuple = (Tuple) data;
      Data[] d = tuple.getComponents();
      for (int i=0; i<d.length; i++) {
        if (d[i] instanceof FieldImpl) {
          field = (FieldImpl) d[i];
          break;
        }
      }
    }
    return field;
  }

  /** Sets the given file as the current one. */
  private void setFile(int curFile, boolean initialize) {
    bio.setWaitCursor(true);
    try {
      if (initialize) init(files, 0);
      else {
        boolean success = setData(files[curFile]);
        if (!success) {
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

}
