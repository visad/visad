
//
// Nesti.java
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

package visad.paoloa.nesti;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.util.VisADSlider;
import visad.util.LabeledRGBWidget;
import visad.util.SelectRangeWidget;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Nesti {

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
  Field spectrum_field;
  // index of spectrum in nasti range tuple
  int spectrum_index;

  // flag to use Java2D
  boolean java2d = true;

  // RealTypes for data
  RealType time;
  RealType wnum1;
  RealType atmosphericRadiance;
  RealType image_line;
  RealType image_element;

  // range of wave numbers form file
  float wnum_low;
  float wnum_hi;
  int wnum_low_idx;
  int wnum_hi_idx;

  // sample set for image pixels
  Linear2DSet image_set;
  // MathTypes for image
  RealTupleType image_domain;
  FunctionType image_type;

  // MathType for red_bar overlaid on spectrum display
  FunctionType red_bar_type;

  RealType pressure;
  RealType watervapor;
  RealType temperature;
  RealType ozone;
  RealType waveNumber;
  RealType radiance;
  FlatField field_tt;
  FlatField field_wv;
  FlatField field_oz;
  FlatField field_rr;
  int type = 1;
  int[] nbuse = new int[3];
  float[] tskin = new float[1];
  float[] psfc = new float[1];
  int[] lsfc = new int[1];
  float[] azen = new float[1];
  float[] p = new float[ 40 ];
  float[] tt = new float[ 40 ];
  float[] wv = new float[ 40 ];
  float[] oz = new float[ 40 ];
  double[] vn = new double[9127];
  double[] tb = new double[9127];
  double[] rr = new double[9127];
  double[][] rr_values;
  double[][] rr_values_sub;


  // type 'java Nasti' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args.length < 1) {
      return;
    }
    System.loadLibrary("Nesti");
    Nesti nasti = new Nesti(args[0]);
  }

  public Nesti(String filename)
         throws VisADException, RemoteException, IOException {
 //*----------
    readProf_c( type, tskin, psfc, lsfc, azen, p, tt, wv, oz );

    nbuse[0] = 1;
    nbuse[1] = 1;
    nbuse[2] = 1;
    nastirte_c( tskin[0], psfc[0], lsfc[0], azen[0], p, tt, wv, oz,
                nbuse, vn, tb, rr ); 

    pressure = new RealType("pressure", null, null);
    temperature = new RealType("temperature", null, null);
    watervapor = new RealType("watervapor", null, null);
    ozone = new RealType("ozone", null, null);

    waveNumber = new RealType("wavenumber", null, null);
    radiance = new RealType("Radiance", null, null);

    FunctionType press_tt = new FunctionType( pressure, temperature );
    FunctionType press_wv = new FunctionType( pressure, watervapor );
    FunctionType press_oz = new FunctionType( pressure, ozone );
    FunctionType wave_rad = new FunctionType( waveNumber, radiance );

    float[][] samples = new float[1][40];
    samples[0] = p;
    int n_samples = 40;
    Gridded1DSet domain = new Gridded1DSet( pressure, samples, n_samples );

    field_tt = new FlatField( press_tt, domain );
    field_wv = new FlatField( press_wv, domain );
    field_oz = new FlatField( press_oz, domain );

    samples = new float[1][9127];
    double[][] vn_a = new double[1][];
    vn_a[0] = vn;
    samples = Set.doubleToFloat(vn_a);
    int n_wnum = 9127;
    domain = new Gridded1DSet( waveNumber, samples, n_wnum );
    field_rr = new FlatField( wave_rad, domain ); 
    rr_values = new double[1][9127]; 
    
    float[][] tt_values = new float[1][40];
    float[][] wv_values = new float[1][40];
    float[][] oz_values = new float[1][40];

    tt_values[0] = tt;
    wv_values[0] = wv;
    oz_values[0] = oz;
    rr_values[0] = rr;

    field_tt.setSamples( tt_values );
    field_wv.setSamples( wv_values );
    field_oz.setSamples( oz_values );
    field_rr.setSamples( rr_values );

 //*------
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
    spectrum_index = 86;
    FunctionType spectrum_type =
      (FunctionType) nasti_range_type.getComponent(spectrum_index);
    wnum1 = (RealType) ((RealTupleType) spectrum_type.getDomain()).getComponent(0);
    atmosphericRadiance = (RealType) spectrum_type.getRange();

    // build red_bar_type
    red_bar_type = new FunctionType(atmosphericRadiance, wnum1);

    // get first spectrum and its sampling
 // Field spectrum0 =
 //    (Field) ((Tuple) nasti.getSample(0)).getComponent(spectrum_index);
 // Gridded1DSet spectrum_set = (Gridded1DSet) spectrum0.getDomainSet();
    Gridded1DSet spectrum_set = null;
    // System.out.println("spectrum_set = " + spectrum_set);

//------------------
    FunctionType f_type = new FunctionType( time, spectrum_type );
    spectrum_field = new FieldImpl( f_type, time_set );
    double[][] range_values;
    for ( int i = 0; i < ntimes; i++ )
    {
      Field spectrum0 =
         (Field) ((Tuple) nasti.getSample(i)).getComponent(spectrum_index);
      spectrum_set = (Gridded1DSet) spectrum0.getDomainSet();
      int len = spectrum_set.getLength();
      float[][] spectrum_samples = spectrum_set.getSamples(false);
      float[][] new_samples = new float[1][len];
      float[][] temp_ranges = new float[1][len];
      range_values = spectrum0.getValues();
      int cnt = 0;
      for ( int ii = 0; ii < spectrum_samples[0].length; ii++ ) 
      {
        if (( spectrum_samples[0][ii] >= 620.04 ) &&
            ( spectrum_samples[0][ii] <= 2820.08 ))
        {
          new_samples[0][cnt] = spectrum_samples[0][ii];
          temp_ranges[0][cnt] = (float) range_values[0][ii];
          cnt++;
        }
      }
      float[][] new_spectrum = new float[1][cnt];
      float[][] new_ranges = new float[1][cnt];
      for ( int ii = 0; ii < cnt; ii++ ) {
        new_spectrum[0][ii] = new_samples[0][ii];
        new_ranges[0][ii] = temp_ranges[0][ii];
      }
      spectrum_set = new Gridded1DSet(spectrum_set.getType(), new_spectrum, cnt );
      FlatField f_field = new FlatField( spectrum_type, spectrum_set );
      f_field.setSamples( new_ranges );
      spectrum_field.setSample(i, f_field);
    }
//-----------------------
    float[] lows = spectrum_set.getLow();
    float[] his = spectrum_set.getHi();
    int spectrum_set_length = spectrum_set.getLength();

    // range of wave numbers
    wnum_low = lows[0];
    wnum_hi = his[0];
    System.out.println(wnum_low);
    System.out.println(wnum_hi);
    System.out.println(spectrum_set_length);

    // set up image
    // image_set = new Integer2DSet(nelements, nlines);
    image_set = new Linear2DSet(-48.75, 48.75, 13,
                                -0.5, (double) (nlines - 0.5), nlines);
    image_line = new RealType("image_line");
    image_element = new RealType("image_element");
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

    frame.getContentPane().add(panel);

    // create two image-spectrum interfaces (each have
    // interacting image and spectrum displays)
    ChannelImage channel_image1 = new ChannelImage();
//  ChannelImage channel_image2 = new ChannelImage();
    FowardRadiance foward_radiance = new FowardRadiance();

    // add image-spectrum interfaces to the JFrame
    panel.add(channel_image1);
 // panel.add(channel_image2);
 // panel.add(foward_radiance);

    // set size of JFrame and make it visible
       frame.setSize(400, 900);
//  frame.setSize(800, 900);
    frame.setVisible(true);

    JFrame frame2 = new JFrame("Foward Radiance");
    frame2.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e2) {System.exit(0);}
    });

    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
    panel2.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel2.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    frame2.getContentPane().add(panel2);
    frame2.setSize(400, 900);
    panel2.add(foward_radiance);
    frame2.setVisible(true);
  }

  /** this make an image of one NAST-I channel, with a JTextField
      for channel selection, a LabeledRGBWidget for pixel colors
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

    // cursor display coordinates
    double[] cur = null;
    double[] scale_offset = new double[2];
    double[][] scale_s = new double[2][2];
    double[] dum_1 = new double[2];
    double[] dum_2 = new double[2];

    DisplayImpl display1;
    RealTuple init_white_cursor;

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
      init_white_cursor =
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

      line_map.getScale( scale_offset, dum_1, dum_2 );
      scale_s[1][0] = scale_offset[0];
      scale_s[1][1] = scale_offset[1];
      element_map.getScale( scale_offset, dum_1, dum_2 );
      scale_s[0][0] = scale_offset[0];
      scale_s[0][1] = scale_offset[1];

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
      //display1.addReferences(new DirectManipulationRendererJ2D(),
      //                       white_cursor_ref, wmaps);
        cur = new double[2];
      }
      else {
      //display1.addReferences(new DirectManipulationRendererJ3D(),
      //                       white_cursor_ref, wmaps);
        cur = new double[3];
      }

      display1.addReference( white_cursor_ref, wmaps);

      display1.addDisplayListener( new CursorClick() );
 
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
      LabeledRGBWidget lw = new LabeledRGBWidget(radiance_map1);
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
           // (Field) ((Tuple) nasti.getSample(i)).getComponent(spectrum_index);
              (Field) spectrum_field.getSample(i);
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
        //(Field) ((Tuple) nasti.getSample(i)).getComponent(spectrum_index);
          (Field) spectrum_field.getSample(i);
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

    class CursorClick implements DisplayListener
    {
      RealTuple w_tuple;
      double real_x;
      double real_y;
      public void displayChanged( DisplayEvent e )
      {
        if ( e.getId() == DisplayEvent.MOUSE_PRESSED_CENTER )
        {
          cur = display1.getDisplayRenderer().getCursor();
          real_x = (cur[0] - scale_s[0][1])/scale_s[0][0];
          real_y = (cur[1] - scale_s[1][1])/scale_s[1][0];
          try {
            w_tuple = 
              new RealTuple(new Real[] {new Real(image_element, real_x),
                                        new Real(image_line, real_y)});
            white_cursor_ref.setData( w_tuple );
          }
          catch ( VisADException e3 ) {
          }
          catch ( RemoteException e3 ) {
          }
        }
      }
    }

  } // end class ChannelImage

  class FowardRadiance extends JPanel
        implements ActionListener
  {

    DataReferenceImpl field_ttRef; 
    DataReferenceImpl field_wvRef; 
    DataReferenceImpl field_ozRef; 
    DataReferenceImpl field_rrRef;

    double[][] tt_last;
    double[][] wv_last;
    double[][] oz_last;
    float[][] tt_last_f;
    float[][] wv_last_f;
    float[][] oz_last_f;
 
    JPanel dpanel1, dpanel2, bpanel, w_panel, s_panel;
    JButton compute;
    ScalarMap wnum_map;
    ScalarMap radiance_map;


    FowardRadiance() throws VisADException, RemoteException
    {
      ConstantMap[] red = {  new ConstantMap(0.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(0.0, Display.Green) };
      ConstantMap[] blue = {  new ConstantMap(0.0, Display.Blue),
                              new ConstantMap(0.0, Display.Red),
                              new ConstantMap(1.0, Display.Green) };
      ConstantMap[] green = {  new ConstantMap(1.0, Display.Blue),
                               new ConstantMap(0.0, Display.Red),
                               new ConstantMap(0.0, Display.Green) };

      //*- GUI layout

      setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentY(JPanel.TOP_ALIGNMENT);
      setAlignmentX(JPanel.LEFT_ALIGNMENT);

      field_ttRef = new DataReferenceImpl( "tt_profile_ref" );
      field_wvRef = new DataReferenceImpl( "wv_profile_ref" );
      field_ozRef = new DataReferenceImpl( "oz_profile_ref" );
      field_rrRef = new DataReferenceImpl( "radiance_ref" );

      field_ttRef.setData( field_tt );
      field_wvRef.setData( field_wv );
      field_ozRef.setData( field_oz );
      field_rrRef.setData( field_rr );

      DisplayImpl display1 = null;
      if ( java2d ) {
         display1 = new DisplayImplJ2D("sounding display");
      }

      ScalarMap pres_Y = new ScalarMap( pressure, Display.YAxis );
      pres_Y.setRange( 1000., 50.);
      display1.addMap( pres_Y );
      display1.addMap( new ScalarMap( temperature, Display.XAxis ));
      display1.addMap( new ScalarMap( watervapor, Display.XAxis ));
      display1.addMap( new ScalarMap( ozone, Display.XAxis ));
      GraphicsModeControl mode1 = display1.getGraphicsModeControl();
      mode1.setScaleEnable(true);

      display1.addReferences( new DirectManipulationRendererJ2D(),
                              field_ttRef, red );
      display1.addReferences( new DirectManipulationRendererJ2D(),
                              field_wvRef, blue );
      display1.addReferences( new DirectManipulationRendererJ2D(),
                              field_ozRef, green );

      double[] scale_offset = new double[2];
      double[] data = new double[2];
      double[] display = new double[2];
      pres_Y.getScale( scale_offset, data, display );
      System.out.println( scale_offset[0]+" "+data[0]+" "+display[0] );
      System.out.println( scale_offset[1]+" "+data[1]+" "+display[1] );
      dpanel1 = new JPanel();
      dpanel1.setLayout(new BoxLayout(dpanel1, BoxLayout.X_AXIS));
      dpanel1.add(display1.getComponent());
      dpanel1.add(Box.createHorizontalStrut(0));
      Border etchedBorder5 = new CompoundBorder(new EtchedBorder(),
                                                new EmptyBorder(5,5,5,5));
      dpanel1.setBorder(etchedBorder5);
      add(dpanel1);

      // create button for spectrum compute
      bpanel = new JPanel();
      bpanel.setLayout(new BoxLayout(bpanel, BoxLayout.X_AXIS));
      JButton compute = new JButton("compute");
      compute.addActionListener(this);
      compute.setActionCommand("compute");
      bpanel.add(compute);
      add(bpanel);

      // create spectrum Display using Java3D in 2-D mode
      DisplayImpl display2 = null;
      if (java2d) {
        display2 = new DisplayImplJ2D("spectrum display");
      }
      else {
        display2 = new DisplayImplJ3D("spectrum display",
                                      new TwoDDisplayRendererJ3D());
      }
      wnum_map = new ScalarMap(waveNumber, Display.XAxis);
      wnum_map.setRange((double) wnum_low, (double) wnum_hi);
      display2.addMap(wnum_map);
      radiance_map = new ScalarMap(radiance, Display.YAxis);
    //radiance_map.setRange( 0., 200. );
      display2.addMap(radiance_map);

    //ScalarMap sr_map = new ScalarMap(waveNumber, Display.SelectRange ); 
    //ScalarMap sr_map = new ScalarMap(radiance, Display.SelectRange ); 
    //display2.addMap( sr_map );

    //radiance_map.addScalarMapListener(this);

      // always autoscale YAxis to range of radiances
    //display1.setAlwaysAutoScale(true);

      // turn on scales
      GraphicsModeControl mode2 = display2.getGraphicsModeControl();
      mode2.setScaleEnable(true);

      // link spectrum to display
      display2.addReference(field_rrRef);

      radiance_map.getScale( scale_offset, data, display );
      System.out.println( scale_offset[0]+" "+data[0]+" "+display[0] );
      System.out.println( scale_offset[1]+" "+data[1]+" "+display[1] );
      // create panel for display with border
      dpanel2 = new JPanel();
      dpanel2.setLayout(new BoxLayout(dpanel2, BoxLayout.X_AXIS));
      dpanel2.add(display2.getComponent());
      dpanel2.add(Box.createHorizontalStrut(0));
      dpanel2.setBorder(etchedBorder5);
      add(dpanel2);

   /**
      SelectRangeWidget sr_widget = new SelectRangeWidget(sr_map);
      w_panel = new JPanel();
      w_panel.setLayout(new BoxLayout(w_panel, BoxLayout.X_AXIS));
      w_panel.add( sr_widget );
      add(w_panel);
    **/

      s_panel = new JPanel();
      s_panel.setLayout( new BoxLayout(s_panel, BoxLayout.X_AXIS) );
      s_panel.setBorder( etchedBorder5 );
      JButton all = new JButton("ALL");
      all.addActionListener(this);
      all.setActionCommand("ALL");
      s_panel.add( all );
      JButton co2_1 = new JButton("CO2_1");
      co2_1.addActionListener(this);
      co2_1.setActionCommand("CO2_1");
      s_panel.add( co2_1 );
      JButton o3 = new JButton("O3");
      o3.addActionListener(this);
      o3.setActionCommand("O3");
      s_panel.add( o3 );
      JButton h2o = new JButton("H2O");
      h2o.addActionListener(this);
      h2o.setActionCommand("H2O");
      s_panel.add( h2o );
      JButton co2_2 = new JButton("CO2_2");
      co2_2.addActionListener(this);
      co2_2.setActionCommand("CO2_2");
      s_panel.add( co2_2 );

      add(s_panel);

/**
      CellImpl profileChanged_cell = new CellImpl() {
          public void doAction() throws VisADException, RemoteException {

            field_tt = (FlatField) field_ttRef.getData();
            field_wv = (FlatField) field_ttRef.getData();
            field_oz = (FlatField) field_ttRef.getData();

            tt_last = field_tt.getValues();
            wv_last = field_wv.getValues();
            oz_last = field_oz.getValues();
         }
      };
      profileChanged_cell.addReference( field_ttRef );
      profileChanged_cell.addReference( field_wvRef );
      profileChanged_cell.addReference( field_ozRef );
 **/

/**
      CellImpl computeRadiance_cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
    
        }
      };
 **/
    }

    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("compute")) {

        field_tt = (FlatField) field_ttRef.getData();
        field_wv = (FlatField) field_wvRef.getData();
        field_oz = (FlatField) field_ozRef.getData();
        try {
          tt_last = field_tt.getValues();
          tt_last_f = Set.doubleToFloat(tt_last);
          wv_last = field_wv.getValues();
          wv_last_f = Set.doubleToFloat(wv_last);
          oz_last = field_oz.getValues();
          oz_last_f = Set.doubleToFloat(oz_last);
        }
        catch ( VisADException e1 ) {
        }

        nastirte_c( tskin[0], psfc[0], lsfc[0], azen[0], p, tt_last_f[0],
                    wv_last_f[0], oz_last_f[0], nbuse, vn, tb, rr );
        rr_values[0] = rr;
        try {
          field_rr.setSamples(rr_values);
        }
        catch ( VisADException e2 ) {
        }
        catch ( RemoteException e3 ) {
        }
      }
      else
        
      if (cmd.equals("CO2_1") || cmd.equals("O3") || 
          cmd.equals("H2O") || cmd.equals("CO2_2") || cmd.equals("ALL") ) {
        try {
          setBand( cmd );
        }
        catch ( VisADException e4 )  {
        }
        catch ( RemoteException e5 ) {
        }
  
      }
    }

    synchronized void setBand( String band ) 
  //void setBand( String band ) 
      throws VisADException, RemoteException
    {
      double CO2_1_lo = 700;
      double CO2_1_hi = 800;
      double CO2_2_lo = 2395;
      double CO2_2_hi = 2400;
      double O3_lo = 1025;
      double O3_hi = 1075;
      double H2O_lo = 1200;
      double H2O_hi = 1600;

      if ( band.equals("CO2_1") ) {
        wnum_map.setRange( CO2_1_lo, CO2_1_hi );
      }
      if ( band.equals("CO2_2") ) {
        wnum_map.setRange( CO2_2_lo, CO2_2_hi );
      }
      if ( band.equals("O3") ) {
        wnum_map.setRange( O3_lo, O3_hi );
      }
      if ( band.equals("H2O") ) {
        wnum_map.setRange( H2O_lo, H2O_hi );
      }
      if ( band.equals("ALL") ) {
        wnum_map.setRange( (double) wnum_low, (double) wnum_hi );
      }
    }
  } // end class FowardRadiance

  private native void readProf_c( int i, float[] a, float[] b, int[] c, float[] d, 
                                  float[] p, float[] t, float[] wv, float[] o );

  private native void nastirte_c( float a, float b, int c, float d,
                                    float[] p, float[] t, float[] wv, float[] o, 
                                    int[] u, double[] vn, double[] tb, double[] rr );
}
