
//
// Nasti.java
//

/*

TO DO list:

1. click on pixel and see spectrum
2. click on pixel and do retrieval
3. display linear combinations of channels
4. Fortran retrieval subroutine takes
   arrays of radiances and wave numbers, or ??
5. change which channels are in retrieval
6. compare selected-channel retrieval with
   standard retrieval
7. see weighting functions associated with
   each channel - possibly in 3-D like Aune's
   (turning satellite images on their side)
8. compare area of overlap of weighting
   functions of selected channels, with
   area of non-overlap

*/

package visad.paoloa;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.util.LabeledColorWidget;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Nasti {

  // number of times in file
  int ntimes;
  // size of image array generated from file
  int nlines;
  int nelements;
  // pointers from time index to image sample index
  int[] time_to_sample;
  // pointers from image line, element to time index
  int[][] sample_to_time;
  // VisAD Field data object created from file
  Field nasti;
  // index of spectrum in nasti range tuple
  int spectrum_index;

  // flag to use Java2D
  boolean java2d = false;

  // RealTypes for data
  RealType time;
  RealType wnum1;
  RealType atmosphericRadiance;
  RealType image_line;
  RealType image_element;

  // range of wave numbers form file
  float wnum_low;
  float wnum_hi;

  // sample set for image pixels
  Linear2DSet image_set;
  // MathTypes for image
  RealTupleType image_domain;
  FunctionType image_type;

  // MathType for red_bar overlaid on spectrum display
  FunctionType red_bar_type;

  // type 'java visad.paoloa.Nasti file.nc' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args.length < 1) {

      /* CTR: 29 September 1998 */
      System.out.println("To run this program, type " +
                         "\"java visad.paoloa.Nasti file.nc\"");
      System.out.println("where file.nc is a netCDF file containing a " +
                         "NAST-I file.");

      return;
    }
    Nasti nasti = new Nasti(args[0]);
  }

  public Nasti(String filename)
         throws VisADException, RemoteException, IOException {
    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing a NAST-I file
    Tuple nasti_tuple = (Tuple) plain.open(filename);
    plain = null;

    // extract the time sequence of spectra
    nasti = (Field) nasti_tuple.getComponent(2);

    // extract the type of image and use
    // it to determine how images are displayed
    FunctionType nasti_type = (FunctionType) nasti.getType();
    TupleType nasti_range_type = (TupleType) nasti_type.getRange();
    int angle_index = nasti_range_type.getIndex("sceneMirrorAngle");
    time = (RealType) ((RealTupleType) nasti_type.getDomain()).getComponent(0);

    // compute how times map to image pixels
    Gridded1DSet time_set = (Gridded1DSet) nasti.getDomainSet();
    float[][] t = time_set.getSamples();
    float[] times = t[0];
    ntimes = time_set.getLength();
    double[] angles = new double[ntimes];
    int[] elements = new int[ntimes];
    int[] lines = new int[ntimes];
    int line = 0;
    int max_element = 0;
    for (int i=0; i<ntimes; i++) {
      // sceneMirrorAngle, scans between -45 and +45 and definme scan lines
      angles[i] =
        ((Real) ((Tuple) nasti.getSample(i)).getComponent(angle_index)).getValue();
/*
      System.out.println("sceneMirrorAngle[" + i + "] = " + angles[i] +
                         " time = " + times[i]);
*/
      if (i > 0 && angles[i] < angles[i-1]) line++;
      lines[i] = line;
      elements[i] = (int) Math.round((angles[i] + 45.0) / 7.5);
      if (elements[i] < 0) {
        System.out.println("negative element " + elements[i]);
        System.exit(0);
      }
      if (elements[i] > 12) {
        System.out.println("element > 12: " + elements[i]);
        System.exit(0);
      }
      if (elements[i] > max_element) max_element = elements[i];
    }
    if (max_element > 12) {
      System.out.println("max_element = " + max_element + " too large");
      System.exit(0);
    }
    // size of image
    nlines = line + 1;
    nelements = 13;

System.out.println("nlines = " + nlines + " nelements = " + nelements);

    // set up mappings between times and image
    time_to_sample = new int[ntimes];
    sample_to_time = new int[nlines][nelements];
    for (int i=0; i<nlines; i++) {
      for (int j=0; j<nelements; j++) {
        sample_to_time[i][j] = -1;
      }
    }
    for (int i=0; i<ntimes; i++) {
      time_to_sample[i] = elements[i] + nelements * lines[i];
      sample_to_time[lines[i]][elements[i]] = i;
    }

    // get spectrum and types
    spectrum_index = nasti_range_type.getDimension() - 1;
    FunctionType spectrum_type =
      (FunctionType) nasti_range_type.getComponent(spectrum_index);
    wnum1 = (RealType) ((RealTupleType) spectrum_type.getDomain()).getComponent(0);
    atmosphericRadiance = (RealType) spectrum_type.getRange();

    // build red_bar_type
    red_bar_type = new FunctionType(atmosphericRadiance, wnum1);

    // get first spectrum and its sampling
    Field spectrum0 =
      (Field) ((Tuple) nasti.getSample(0)).getComponent(spectrum_index);
    Gridded1DSet spectrum_set = (Gridded1DSet) spectrum0.getDomainSet();
    // System.out.println("spectrum_set = " + spectrum_set);
    float[] lows = spectrum_set.getLow();
    float[] his = spectrum_set.getHi();
    int spectrum_set_length = spectrum_set.getLength();

    // range of wave numbers
    wnum_low = lows[0];
    wnum_hi = his[0];

    // set up image
    // image_set = new Integer2DSet(nelements, nlines);
    image_set = new Linear2DSet(-48.75, 48.75, 13,
                                -0.5, (double) (nlines - 0.5), nlines);
    image_line = RealType.getRealType("image_line");
    image_element = RealType.getRealType("image_element");
    image_domain = new RealTupleType(image_element, image_line);
    image_type = new FunctionType(image_domain, atmosphericRadiance);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("Nasti VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // create two image-spectrum interfaces (each have
    // interacting image and spectrum displays)
    ChannelImage channel_image1 = new ChannelImage();
    ChannelImage channel_image2 = new ChannelImage();

    // add image-spectrum interfaces to the JFrame
    panel.add(channel_image1);
    panel.add(channel_image2);

    frame.getContentPane().add(panel);
    // set size of JFrame and make it visible
    // frame.setSize(400, 900);
    frame.setSize(800, 900);
    frame.setVisible(true);
  }

  /** this make an image of one NAST-I channel, with a JTextField
      for channel selection, a LabeledColorWidget for pixel colors
      and a spectrum display */
  class ChannelImage extends JPanel
        implements ActionListener, ItemListener, ScalarMapListener {
    // array for image radiances
    double[][] radiances;
    // image data object for display
    FlatField image;

    // declare DataReferences for displaying white_cursor, red_cursor,
    // spectrum and red_bar
    DataReferenceImpl image_ref;
    DataReferenceImpl white_cursor_ref;
    DataReferenceImpl red_cursor_ref;
    DataReferenceImpl spectrum_ref;
    DataReferenceImpl red_bar_ref;

    // ScalarMap for atmosphericRadiance in the spectrum display
    ScalarMap radiance_map2;

    // ScalarMap for wnum1 in the spectrum display
    ScalarMap wnum_map;

    // some GUI components
    JPanel wpanel;
    JLabel wnum_label;
    JTextField wnum_field;
    JPanel zpanel;
    JCheckBox wnum_zoom;
    JButton recenter;
    JPanel dpanel1, dpanel2;

    // last valid wave number from text field
    float wnum_last;

    // true to zoom whum1 range in spectrum display
    boolean wzoom;

    // flag to skip one red_cursor_cell event
    boolean skip_red = false;

    // construct a image-spectrum interface
    ChannelImage() throws VisADException, RemoteException {

      // GUI layout
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentY(JPanel.TOP_ALIGNMENT);
      setAlignmentX(JPanel.LEFT_ALIGNMENT);

      // construct DataReferences for displaying white_cursor, red_cursor,
      // spectrum and red_bar
      white_cursor_ref = new DataReferenceImpl("white_cursor_ref");
      red_cursor_ref = new DataReferenceImpl("red_cursor_ref");
      spectrum_ref = new DataReferenceImpl("spectrum_ref");
      red_bar_ref = new DataReferenceImpl("red_bar_ref");

      // create text field for entering wave number
      wpanel = new JPanel();
      wpanel.setLayout(new BoxLayout(wpanel, BoxLayout.X_AXIS));
      wnum_label = new JLabel("wave number:");
      wnum_field = new JTextField("---");

      // WLH 2 Dec 98
      Dimension msize = wnum_field.getMaximumSize();
      Dimension psize = wnum_field.getPreferredSize();
      msize.height = psize.height;
      wnum_field.setMaximumSize(msize);

      wnum_field.addActionListener(this);
      wnum_field.setActionCommand("wavenum");
      wnum_field.setEnabled(true);
      wpanel.add(wnum_label);
      wpanel.add(wnum_field);
      wpanel.add(Box.createRigidArea(new Dimension(10, 0)));
      add(wpanel);

      // initial wave number in middle of spectrum
      wnum_last = (wnum_low + wnum_hi) / 2.0f;
      wnum_field.setText(PlotText.shortString(wnum_last));

      // white_cursor in image display for selecting spectrum
      RealTuple init_white_cursor =
        new RealTuple(new Real[] {new Real(image_element, 0.0),
                                  new Real(image_line, 0.0)});
      white_cursor_ref.setData(init_white_cursor);

      // create image data object for display and initialize radiance
      // array to missing
      image = new FlatField(image_type, image_set);
      radiances = new double[1][nelements * nlines];
      for (int i=0; i<nelements * nlines; i++) {
        radiances[0][i] = Double.NaN;
      }
      image_ref = new DataReferenceImpl("image_ref");
      image_ref.setData(image);

      // create red_cursor in spectrum display for setting wave number
      Real init_red_cursor = new Real(wnum1, (double) wnum_last);
      red_cursor_ref.setData(init_red_cursor);

      // initialize image to initial wave number
      do_image(wnum_last);

      // create image Display using Java3D in 2-D mode
      DisplayImpl display1 = null;
      if (!java2d) {
        try {
          display1 = new DisplayImplJ3D("image display",
                                        new TwoDDisplayRendererJ3D());
        }
        catch (UnsatisfiedLinkError e) {
          java2d = true;
        }
      }
      if (java2d) {
        display1 = new DisplayImplJ2D("image display");
      }
      ScalarMap line_map = new ScalarMap(image_line, Display.YAxis);
      display1.addMap(line_map);
      line_map.setRange(12.5, -0.5);
      ScalarMap element_map = new ScalarMap(image_element, Display.XAxis);
      display1.addMap(element_map);
      element_map.setRange(-48.75, 48.75);
      ScalarMap radiance_map1 = new ScalarMap(atmosphericRadiance, Display.RGB);
      display1.addMap(radiance_map1);

      // always autoscale color map to range of radiances
      display1.setAlwaysAutoScale(true);

      // turn on scales for image line and element
      GraphicsModeControl mode1 = display1.getGraphicsModeControl();
      // mode1.setScaleEnable(true);

      // link image to display
      display1.addReference(image_ref);

      // make white_cursor and link to display with direct manipulation
      // (so white_cursor can select spectrum)
      ConstantMap[] wmaps = {new ConstantMap(1.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(1.0, Display.Green),
                             new ConstantMap(4.0, Display.PointSize)};
      if (java2d) {
        display1.addReferences(new DirectManipulationRendererJ2D(),
                               white_cursor_ref, wmaps);
      }
      else {
        display1.addReferences(new DirectManipulationRendererJ3D(),
                               white_cursor_ref, wmaps);
      }
      // create panel for display with border
      dpanel1 = new JPanel();
      dpanel1.setLayout(new BoxLayout(dpanel1, BoxLayout.X_AXIS));
      dpanel1.add(display1.getComponent());
      dpanel1.add(Box.createHorizontalStrut(0));
      Border etchedBorder5 =
        new CompoundBorder(new EtchedBorder(),
                           new EmptyBorder(5, 5, 5, 5));
      dpanel1.setBorder(etchedBorder5);
      add(dpanel1);

      // create color widget for atmosphericRadiance
      LabeledColorWidget lw = new LabeledColorWidget(radiance_map1);
      Dimension d = new Dimension(400, 200);
      lw.setMaximumSize(d);
      JPanel lpanel = new JPanel();
      lpanel.setLayout(new BoxLayout(lpanel, BoxLayout.X_AXIS));
      lpanel.add(lw);
      lpanel.setBorder(etchedBorder5);
      add(lpanel);

      // create buttons for zooming and center spectrum
      zpanel = new JPanel();
      zpanel.setLayout(new BoxLayout(zpanel, BoxLayout.X_AXIS));
      wnum_zoom = new JCheckBox("wave number zoom", false);
      wnum_zoom.addItemListener(this);
      recenter = new JButton("Recenter");
      recenter.addActionListener(this);
      recenter.setActionCommand("recenter");
      zpanel.add(wnum_zoom);
      zpanel.add(recenter);
      add(zpanel);

      // create spectrum Display using Java3D in 2-D mode
      DisplayImpl display2 = null;
      if (java2d) {
        display2 = new DisplayImplJ2D("spectrum display");
      }
      else {
        display2 = new DisplayImplJ3D("spectrum display",
                                      new TwoDDisplayRendererJ3D());
      }
      wnum_map = new ScalarMap(wnum1, Display.XAxis);
      display2.addMap(wnum_map);
      radiance_map2 = new ScalarMap(atmosphericRadiance, Display.YAxis);
      display2.addMap(radiance_map2);
      // get autoscale events for atmosphericRadiance, to set length
      // of red_bar
      radiance_map2.addScalarMapListener(this);

      // always autoscale YAxis to range of radiances
      display1.setAlwaysAutoScale(true);

      // turn on scales for image line and element
      GraphicsModeControl mode2 = display2.getGraphicsModeControl();
      mode2.setScaleEnable(true);

      // link spectrum to display
      display2.addReference(spectrum_ref);

      // link red_bar for display
      ConstantMap[] bmaps = {new ConstantMap(0.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(0.0, Display.Green)};
      display2.addReference(red_bar_ref, bmaps);

      // link red_cursor to display with direct manipulation
      // (so red_cursor can select wave number)
      ConstantMap[] rmaps = {new ConstantMap(-1.0, Display.YAxis),
                             new ConstantMap(0.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(0.0, Display.Green),
                             new ConstantMap(4.0, Display.PointSize)};
      if (java2d) {
        display2.addReferences(new DirectManipulationRendererJ2D(),
                               red_cursor_ref, rmaps);
      }
      else {
        display2.addReferences(new DirectManipulationRendererJ3D(),
                               red_cursor_ref, rmaps);
      }
      // create panel for display with border
      dpanel2 = new JPanel();
      dpanel2.setLayout(new BoxLayout(dpanel2, BoxLayout.X_AXIS));
      dpanel2.add(display2.getComponent());
      dpanel2.add(Box.createHorizontalStrut(0));
      dpanel2.setBorder(etchedBorder5);
      add(dpanel2);

      // CellImpl to change spectrum when user moves white_cursor
      CellImpl white_cursor_cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          int i;
          red_bar_ref.setData(null);
          RealTuple white_cursor = (RealTuple) white_cursor_ref.getData();
          float elem = (float) ((Real) white_cursor.getComponent(0)).getValue();
          int element =
            (int) Math.round((elem + 45.0) / 7.5);
          int line =
            (int) Math.round( ((Real) white_cursor.getComponent(1)).getValue() );
          if (0 <= line && line < nlines && 0 <= element && element < nelements) {
            i = sample_to_time[line][element];
          }
          else {
            i = -1;
          }
          if (i >= 0) {
            Field spectrum =
              (Field) ((Tuple) nasti.getSample(i)).getComponent(spectrum_index);
            spectrum_ref.setData(spectrum);
          }
          else {
            spectrum_ref.setData(null);
          }
        }
      };
      // link white_cursor to white_cursor_cell
      white_cursor_cell.addReference(white_cursor_ref);

      // CellImpl to change wave number when user moves red_cursor
      CellImpl red_cursor_cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          int i;
          if (skip_red) {
            skip_red = false;
            return;
          }
          Real red_cursor = (Real) red_cursor_ref.getData();
          if (red_cursor == null) return;
          float wnum = (float) red_cursor.getValue();

          if (wnum < wnum_low) {
            wnum = wnum_low;
          }
          if (wnum > wnum_hi) {
            wnum = wnum_hi;
          }
          try {
            do_image(wnum);
            wnum_last = wnum;
            do_red_bar(wnum);
            wnum_field.setText(PlotText.shortString(Math.abs(wnum)));
          }
          catch (VisADException exc) {
          }
          catch (RemoteException exc) {
          }
        }
      };
      // link red_cursor to red_cursor_cell
      red_cursor_cell.addReference(red_cursor_ref);

    }

    /** update image based on wave number */
    void do_image(float wnum) throws VisADException, RemoteException {
      double radiance;
      for (int i=0; i<ntimes; i++) {
        Field spectrum =
          (Field) ((Tuple) nasti.getSample(i)).getComponent(spectrum_index);
        try {
          radiance =
            ((Real) spectrum.evaluate(new Real(wnum1, wnum))).getValue();
        }
        catch (VisADException e1) {
          radiance = Double.NaN;
        }
        radiances[0][time_to_sample[i]] = radiance;
      }
      image.setSamples(radiances);
    }

    /** update red_bar based on wave number */
    synchronized void do_red_bar(float wnum)
                 throws VisADException, RemoteException {
      double[] rads = radiance_map2.getRange();
      if (rads[0] == rads[0] && rads[1] == rads[1]) {
        float[][] red_bar_set_samples = {{(float) rads[0], (float) rads[1]}};
        Gridded1DSet red_bar_set =
          new Gridded1DSet(atmosphericRadiance, red_bar_set_samples, 2);
        FlatField red_bar = new FlatField(red_bar_type, red_bar_set);
        float[][] red_bar_samples = {{wnum, wnum}};
        red_bar.setSamples(red_bar_samples);
        red_bar_ref.setData(red_bar);
      }
    }

    synchronized void do_wzoom() throws VisADException, RemoteException {
      if (wzoom) {
        wnum_map.setRange((double) (wnum_last - 10.0),
                          (double) (wnum_last + 10.0));
      }
      else {
        wnum_map.setRange((double) wnum_low, (double) wnum_hi);
      }
    }

    /** respond to autoscale of atmosphericRadiance */
    public void mapChanged(ScalarMapEvent e) {
      if (radiance_map2.equals(e.getScalarMap())) {
        try {
          do_red_bar(wnum_last);
        }
        catch (VisADException e2) {
        }
        catch (RemoteException e2) {
        }
      }
    }

    /** ignore changes to ScalarMap control */
    public void controlChanged(ScalarMapControlEvent evt) { }

    /** respond to user type-ins of wave number */
    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("wavenum")) {
        float wnum = Float.NaN;
        try {
          wnum = Float.valueOf(wnum_field.getText()).floatValue();
        }
        catch (NumberFormatException exc) {
          wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
        }

        if (wnum == wnum) {
          if (wnum < wnum_low) {
            wnum = wnum_low;
            wnum_field.setText(PlotText.shortString(Math.abs(wnum)));
          }
          if (wnum > wnum_hi) {
            wnum = wnum_hi;
            wnum_field.setText(PlotText.shortString(Math.abs(wnum)));
          }
          try {
            do_image(wnum);
            wnum_last = wnum;
            do_red_bar(wnum);
            do_wzoom();

            skip_red = true;
            Real red_cursor = new Real(wnum1, (double) wnum_last);
            red_cursor_ref.setData(red_cursor);
          }
          catch (VisADException exc) {
            wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
          }
          catch (RemoteException exc) {
            wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
          }

        }
        else wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
      } // end if (cmd.equals("wavenum"))
      if (cmd.equals("recenter")) {
        try {
          do_wzoom();
        }
        catch (VisADException exc) {
        }
        catch (RemoteException exc) {
        }
      }
    }

    public void itemStateChanged(ItemEvent e) {
      Object o = e.getItemSelectable();
      boolean on = (e.getStateChange() == ItemEvent.SELECTED);
      if (o == wnum_zoom) {
        try {
          wzoom = on;
          do_wzoom();
        }
        catch (VisADException e2) {
        }
        catch (RemoteException e2) {
        }
      }
    }

  } // end class ChannelImage
}

