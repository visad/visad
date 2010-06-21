
//
// Spline.java
//

package visad.paoloa.spline;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.util.VisADSlider;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Spline {

  boolean toggle = true;
//  double[] nothing = new double[n_samples];
  Real nothing = new Real(-1000.0);

  boolean java2d = false;
  DataReference rlambda_ref;
  DataReference lambda_ref;
  DataReference wk_ref;
  DataReference noise_ref;
  DataReference spline_ref;
  DataReference spline_fieldRef;
  DataReference rlambda_fieldRef;
  DataReference true_fieldRef;
  DataReference gcv_fieldRef;

  int n_samples;
  float[] domain_values;
  double[] range_values;
  double[] true_values;
  double[] rlambda_values;
  double[] noise;
  double[] noise_a;
  double[][] d_range = new double[1][];
  double[][] f_range = new double[1][];
  double[][] g_range = new double[1][];
  double[] spline_range;
  double[] values = new double[2];
  double val;
  double[] wkvalue = new double[1];
  double noise_fac;
  double last_noise_fac;
  double wk_value;
  int mode;

  FlatField rlambda_field;
  FlatField spline_field;
  FlatField gcv_field;
  FlatField true_field;
  Set domainSet;
  FunctionType f_type;
  DataReference[] range_refs;
  RealTuple[] tuples;
  Real[] reals;
  ConstantMap[][] cmaps;

  public static void main(String args[])
         throws VisADException, RemoteException, IOException
  {
    System.loadLibrary("Spline");
    Spline spline = new Spline("file");
  }

  public Spline(String filename)
         throws VisADException, RemoteException, IOException
  {

    rlambda_ref = new DataReferenceImpl("rlambda_ref");
    wk_ref = new DataReferenceImpl("wk_ref");
    lambda_ref = new DataReferenceImpl("lambda_ref");
    noise_ref = new DataReferenceImpl("noise_ref");
    spline_ref = new DataReferenceImpl("spline_ref");

    //- get data

    n_samples = 50;
    true_values = new double[ n_samples ];
    rlambda_values = new double[ 50 ];
    range_values = new double[ n_samples ];
    noise = new double[ n_samples ];
    domain_values = new float[ n_samples ];
    spline_range = new double[ n_samples ];
    range_refs = new DataReferenceImpl[ n_samples ];
    tuples = new RealTuple[ n_samples ];
    reals = new Real[ n_samples ];
    cmaps = new ConstantMap[ n_samples ][];
    double x_c = 0.0;
    noise_fac = 0.1;
    last_noise_fac = noise_fac;
    int iset = 0;
    double gset = 0;
    double fac,r,v1,v2;
    double gasdev;
    for ( int ii = 0; ii < n_samples; ii++ )
    {
      if (iset == 0) {
        do {
           v1 = (2*Math.random()-1.0);
           v2 = (2*Math.random()-1.0);
           r = v1*v1+v2*v2;
        } while (r >= 1.0 || r == 0.0);
        fac = Math.sqrt(-2.0*Math.log(r)/r);
        gset = v1*fac;
        iset = 1;
        gasdev = v2*fac;
      } else {
        iset = 0;
        gasdev = gset;
      }
      noise[ii]=gasdev;
      true_values[ii]=Math.cos(x_c*3.1415926)*Math.exp(-3.0*x_c);
      range_values[ii]=true_values[ii]+noise_fac*noise[ii];
      x_c += .02;
    }

    for ( int ii = 0; ii < n_samples; ii++ )
    {
      domain_values[ii] = (float) ii;
      values[0] = (double) domain_values[ii];
      values[1] = (double) range_values[ii];
      tuples[ii] = new RealTuple( RealTupleType.SpatialCartesian2DTuple, values );
      reals[ii] = new Real( RealType.YAxis, values[1] );
      range_refs[ii] = new DataReferenceImpl("rangeRef_"+ii);
    //range_refs[ii].setData( tuples[ii] );
      range_refs[ii].setData( reals[ii] );
    }

    float[][] samples = new float[1][n_samples];
    samples[0] = domain_values;
    domainSet = new Gridded1DSet( RealType.XAxis, samples, n_samples );
    f_type = new FunctionType( RealType.XAxis, RealType.YAxis );
    spline_field = new FlatField( f_type, domainSet );
    //rlambda_field = new FlatField( f_type, domainSet );
    Linear1DSet dset = new Linear1DSet(RealType.XAxis, -10.0, 0.0, n_samples);
    rlambda_field = new FlatField( f_type, dset );
    gcv_field = new FlatField( f_type, domainSet );
    true_field = new FlatField( f_type, domainSet );
    double[][] d_array = new double[1][];
    d_array[0] = true_values;
    true_field.setSamples(d_array);
    JFrame frame = new JFrame("Spline VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });


    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    SplinePanel spline_panel0 = new SplinePanel();

    // add image-spectrum interfaces to the JFrame
    panel.add(spline_panel0);

    frame.getContentPane().add(panel);
    frame.setSize(900, 900);
    frame.setVisible(true);
  }


  class SplinePanel extends JPanel
        implements ActionListener
  {
    JPanel panel_a, panel_b, panel_c, panel_d, panel_e, panel_left;
    Border etchedBorder5 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(5, 5, 5, 5));

    SplinePanel() throws VisADException, RemoteException
    {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setAlignmentY(JPanel.TOP_ALIGNMENT);
      setAlignmentX(JPanel.LEFT_ALIGNMENT);

       // create left hand side JPanel for sliders and text
       JPanel left = new JPanel(); // FlowLayout and double buffer
       left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
       left.setAlignmentY(JPanel.TOP_ALIGNMENT);
       left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
       add(left);

    // construct JLabels
       // (JTextArea does not align in BoxLayout well, so use JLabels)
       left.add(new JLabel("Smoothing Spline"));
       left.add(new JLabel("using VisAD  -  see:"));
       left.add(new JLabel("  "));
       left.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
       left.add(new JLabel("  "));
       left.add(new JLabel("for more information about VisAD."));
       left.add(new JLabel("  "));
       left.add(new JLabel("William Hibbard, Tom Rink"));
       left.add(new JLabel("Paolo Antonelli and Giulia Panegrossi"));
       left.add(new JLabel("Space Science and Engineering Center"));
       left.add(new JLabel("University of Wisconsin - Madison"));
       left.add(new JLabel("  "));
       left.add(new JLabel("  "));
       left.add(new JLabel("Select different functions using the buttons"));
       left.add(new JLabel("  "));
       // construct JLabels

       // create left hand side JPanel for sliders and text
       JPanel leftbuttons = new JPanel(); // FlowLayout and double buffer
       leftbuttons.setLayout(new BoxLayout(leftbuttons, BoxLayout.X_AXIS));
       leftbuttons.setAlignmentY(JPanel.TOP_ALIGNMENT);
       leftbuttons.setAlignmentX(JPanel.LEFT_ALIGNMENT);

       JButton fun0_button = new JButton("fun0");
       fun0_button.addActionListener(this);
       fun0_button.setActionCommand("fun0");
       leftbuttons.add(fun0_button);

       JButton fun1_button = new JButton("fun1");
       fun1_button.addActionListener(this);
       fun1_button.setActionCommand("fun1");
       leftbuttons.add(fun1_button);

       JButton fun2_button = new JButton("fun2");
       fun2_button.addActionListener(this);
       fun2_button.setActionCommand("fun2");
       leftbuttons.add(fun2_button);

       JButton fun3_button = new JButton("fun3");
       fun3_button.addActionListener(this);
       fun3_button.setActionCommand("fun3");
       leftbuttons.add(fun3_button);

       JButton fun4_button = new JButton("fun4");
       fun4_button.addActionListener(this);
       fun4_button.setActionCommand("fun4");
       leftbuttons.add(fun4_button);


       left.add(leftbuttons);

       left.add(new JLabel("  "));
       left.add(new JLabel("fun0: cos(x)exp(-3x)    x in [0,1]"));
       left.add(new JLabel("fun1: sin(x)exp(-3x)    x in [0,1]"));
       left.add(new JLabel("fun2: xsin(1/(x+1))exp(-3x)   x in [0,1]"));
       left.add(new JLabel("fun3: 2xxsin(x)          x in [0,1]"));
       left.add(new JLabel("fun4: sin(x)log(x+1)   x in [0,1]"));
       left.add(new JLabel("  "));


       // create sliders JPanel
       JPanel sliders = new JPanel();
       sliders.setName("Smoothing Spline Sliders");
       sliders.setFont(new Font("Dialog", Font.PLAIN, 12));
       sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
       sliders.setAlignmentY(JPanel.TOP_ALIGNMENT);
       sliders.setAlignmentX(JPanel.LEFT_ALIGNMENT);
       left.add(sliders);


       left.add(new JLabel("  "));
       left.add(new JLabel("The noise slider changes the value of sigma"));
       left.add(new JLabel("  "));

       VisADSlider noise_slider =
         new VisADSlider(noise_ref, 0f, .5f, .1f, RealType.Generic, "noise");
       left.add(noise_slider);

       left.add(new JLabel("  "));
       left.add(new JLabel("Manual tuning of the smoothing parameter"));
       left.add(new JLabel("  "));

       VisADSlider lambda_slider =
         new VisADSlider(lambda_ref, -10f, 0f, .5f, RealType.Generic, "lambda");
       left.add(lambda_slider);

       left.add(new JLabel("  "));
       left.add(new JLabel("Top Display:"));
       left.add(new JLabel("Blue Curve: True function"));
       left.add(new JLabel("Red Dots: noisy observations"));
       left.add(new JLabel("Green Curve: GCV solution"));
       left.add(new JLabel("White Curve: Solution for a fixed value of lambda  "));
       left.add(new JLabel("  "));
       left.add(new JLabel("Bottom Display: "));
       left.add(new JLabel("Green Curve: V(lambda)"));
       left.add(new JLabel("Red Dot: value of lambda selected by GCV"));
       left.add(new JLabel("  "));

       // create right hand side JPanel for display
       JPanel right = new JPanel(); // FlowLayout and double buffer
       right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
       right.setAlignmentY(JPanel.TOP_ALIGNMENT);
       right.setAlignmentX(JPanel.LEFT_ALIGNMENT);
       add(right);

      DisplayImpl displays = null;
      DisplayImpl display2 = null;

      if (!java2d) {
        try {
          displays = new DisplayImplJ3D("display1",
                                        new TwoDDisplayRendererJ3D());
          display2 = new DisplayImplJ3D("display2",
                                        new TwoDDisplayRendererJ3D());
        }
        catch (UnsatisfiedLinkError e) {
          // java2d = true;
        }
      }
      if (java2d) {
        displays = new DisplayImplJ2D("display1");
        display2 = new DisplayImplJ2D("display2");
      }
      GraphicsModeControl mode1 = displays.getGraphicsModeControl();
      mode1.setScaleEnable(true);
      GraphicsModeControl mode2 = display2.getGraphicsModeControl();
      mode2.setScaleEnable(true);

      panel_a = new JPanel();
      panel_a.setLayout(new BoxLayout(panel_a, BoxLayout.Y_AXIS));
      panel_a.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_a.setAlignmentX(JPanel.LEFT_ALIGNMENT);


      panel_a.add( displays.getComponent() );
      panel_a.setBorder(etchedBorder5);
      right.add( panel_a );

      panel_b = new JPanel();
      panel_b.setLayout(new BoxLayout(panel_b, BoxLayout.X_AXIS));
      panel_b.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_b.setAlignmentX(JPanel.LEFT_ALIGNMENT);

//      panel_c = new JPanel();
//      panel_c.setLayout(new BoxLayout(panel_c, BoxLayout.Y_AXIS));
//      panel_c.setAlignmentY(JPanel.TOP_ALIGNMENT);
//      panel_c.setAlignmentX(JPanel.LEFT_ALIGNMENT);

//      panel_b.add( panel_c );

      panel_d = new JPanel();
      panel_d.setLayout(new BoxLayout(panel_d, BoxLayout.X_AXIS));
//    panel_d.setAlignmaentY(JPanel.TOP_ALIGNMENT);
      panel_d.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      JButton spline_button = new JButton("spline");
      spline_button.addActionListener(this);
      spline_button.setActionCommand("spline");
      panel_d.add(spline_button);

      JButton GCV_button = new JButton("GCV");
      GCV_button.addActionListener(this);
      GCV_button.setActionCommand("GCV");
      panel_d.add(GCV_button);

      JButton nreset_button = new JButton("reset noise");
      nreset_button.addActionListener(this);
      nreset_button.setActionCommand("nreset");
      panel_d.add(nreset_button);

      JButton rlambda_button = new JButton("R(lambda)");
      rlambda_button.addActionListener(this);
      rlambda_button.setActionCommand("rlambda");
      panel_d.add(rlambda_button);

      JButton toggle_button = new JButton("toggle");
      toggle_button.addActionListener(this);
      toggle_button.setActionCommand("toggle");
      panel_d.add(toggle_button);

      panel_b.add(panel_d);

      panel_b.setBorder(etchedBorder5);

      right.add( panel_b );

      ConstantMap[] blue =
      {
        new ConstantMap(0.0, Display.Red),
        new ConstantMap(0.0, Display.Green),
        new ConstantMap(1.0, Display.Blue),
      };
      ConstantMap[] rred =
      {
        new ConstantMap(1.0, Display.Red),
        new ConstantMap(0.0, Display.Green),
        new ConstantMap(0.0, Display.Blue),
        new ConstantMap(4.0, Display.PointSize),
      };
      ConstantMap[] rgreen =
      {
        new ConstantMap(0.0, Display.Red),
        new ConstantMap(1.0, Display.Green),
        new ConstantMap(0.0, Display.Blue),
      };
      ConstantMap[] green =
      {
        new ConstantMap(0.0, Display.Red),
        new ConstantMap(1.0, Display.Green),
        new ConstantMap(0.0, Display.Blue),
      };

      ConstantMap[] cmaps = new ConstantMap[5];
      cmaps[0] = new ConstantMap(1.0, Display.Red);
      cmaps[1] = new ConstantMap(0.0, Display.Green);
      cmaps[2] = new ConstantMap(0.0, Display.Blue);
      cmaps[3] = new ConstantMap(3.0, Display.PointSize);


      ScalarMap map_x = new ScalarMap( RealType.XAxis, Display.XAxis);
      // map_x.setRange( 0., 50.);
      map_x.setRange( 0., (double) (n_samples - 1));
      ScalarMap map_y = new ScalarMap( RealType.YAxis, Display.YAxis);
//      map_y.setScaleColor(new float[] {0.0f, 0.0f, 1.0f});
      displays.addMap( map_x );
      displays.addMap( map_y );

      double display_value;
      double[] scale_offset = new double[2];
      double[] data_range = new double[2];
      double[] display = new double[2];
      map_x.getScale( scale_offset, data_range, display );

      if (java2d) {
        for ( int ii = 0; ii < n_samples; ii++ ) {
          display_value = ((double)domain_values[ii])*scale_offset[0] + scale_offset[1];
          cmaps[4] = new ConstantMap(  display_value,
                                       Display.XAxis );
          displays.addReferences(new DirectManipulationRendererJ2D(),
                                 range_refs[ii], cmaps );
        }
      }
      else {
       for ( int ii = 0; ii < n_samples; ii++ ) {
          display_value = ((double)domain_values[ii])*scale_offset[0] + scale_offset[1];
          cmaps[4] = new ConstantMap(  display_value,
                                       Display.XAxis );
          displays.addReferences(new DirectManipulationRendererJ3D(),
                                 range_refs[ii], cmaps );
        }
      }



      ScalarMap map_rx = new ScalarMap( RealType.XAxis, Display.XAxis);
      map_rx.setRange( -10.0, 0.0);
      ScalarMap map_ry = new ScalarMap(RealType.YAxis , Display.YAxis);
      display2.addMap( map_rx );
      display2.addMap( map_ry );


      panel_e = new JPanel();
      panel_e.setLayout(new BoxLayout(panel_e, BoxLayout.Y_AXIS));
      panel_e.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_e.setAlignmentX(JPanel.LEFT_ALIGNMENT);


      panel_e.add( display2.getComponent() );
      panel_e.setBorder(etchedBorder5);
      right.add( panel_e );



      spline_fieldRef = new DataReferenceImpl("spline_fieldRef");
      gcv_fieldRef = new DataReferenceImpl("gcv_fieldRef");
      true_fieldRef = new DataReferenceImpl("true_fieldRef");
      rlambda_fieldRef = new DataReferenceImpl("rlambda_fieldRef");
      rlambda_fieldRef.setData( rlambda_field );
      spline_fieldRef.setData( spline_field );
      gcv_fieldRef.setData( gcv_field );
      true_fieldRef.setData( true_field );
      displays.addReference(spline_fieldRef);
      displays.addReference(gcv_fieldRef, green);
      displays.addReference(true_fieldRef, blue);
      display2.addReference(rlambda_fieldRef, rgreen);
      display2.addReference(wk_ref, rred);

      CellImpl lambda_cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);
          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );
        }
      };
      lambda_cell.addReference( lambda_ref );

      CellImpl noise_cell = new CellImpl() {
        double[] noise_a = new double[ n_samples ];
        public void doAction() throws VisADException, RemoteException {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
            if ( last_noise_fac == 0 ) {
              noise_a[ii] = noise[ii];
            }
            else {
              noise_a[ii] = (range_values[ii] - true_values[ii])/last_noise_fac;
            }
            range_values[ii] = true_values[ii] + noise_fac*noise_a[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );


          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wkvalue[0]));


          last_noise_fac = noise_fac;

          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
                mode = 1;
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );

        }

      };
      noise_cell.addReference( noise_ref );
    }


    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("fun0")) {
        try {
         if (toggle) {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          double x_c = 0.0;
          for ( int ii = 0; ii < n_samples; ii++ ) {
            true_values[ii]=Math.cos(x_c*3.1415926)*Math.exp(-3.0*x_c);
            range_values[ii] = true_values[ii] + noise_fac*noise[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
            x_c += .02;
          }
          d_range[0] = true_values;
          true_field.setSamples(d_range);

          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );


          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));


          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
                mode = 1;
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
         }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
      if (cmd.equals("fun1")) {
        try {
         if (toggle) {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          double x_c = 0.0;
          for ( int ii = 0; ii < n_samples; ii++ ) {
            true_values[ii]=Math.sin(x_c*3.1415926)*Math.exp(-3.0*x_c);
            range_values[ii] = true_values[ii] + noise_fac*noise[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
            x_c += .02;
          }
          d_range[0] = true_values;
          true_field.setSamples(d_range);

          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );


          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));


          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
                mode = 1;
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
         }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }

      if (cmd.equals("fun2")) {
        try {
         if (toggle) {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          double x_c = 0.0;
          for ( int ii = 0; ii < n_samples; ii++ ) {
            true_values[ii]=x_c*Math.sin(1.0/(x_c+1.0))*Math.exp(-3.0*x_c);
            range_values[ii] = true_values[ii] + noise_fac*noise[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
            x_c += .02;
          }
          d_range[0] = true_values;
          true_field.setSamples(d_range);

          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );


          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));


          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
                mode = 1;
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
         }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }

      if (cmd.equals("fun3")) {
        try {
         if (toggle) {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          double x_c = 0.0;
          for ( int ii = 0; ii < n_samples; ii++ ) {
            true_values[ii]=2.0*x_c*x_c*Math.sin(x_c*3.1415926);
            range_values[ii] = true_values[ii] + noise_fac*noise[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
            x_c += .02;
          }
          d_range[0] = true_values;
          true_field.setSamples(d_range);

          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );


          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));


          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
                mode = 1;
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
         }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }

      if (cmd.equals("fun4")) {
        try {
         if (toggle) {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          double x_c = 0.0;
          for ( int ii = 0; ii < n_samples; ii++ ) {
            true_values[ii]=Math.sin(x_c*3.1415926)*Math.log(x_c+1.0);
            range_values[ii] = true_values[ii] + noise_fac*noise[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
            x_c += .02;
          }
          d_range[0] = true_values;
          true_field.setSamples(d_range);

          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );


          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));


          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
                mode = 1;
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
         }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }

      if (cmd.equals("toggle")) {
        try {
          if (toggle) {
            toggle = false;
            true_fieldRef.setData( nothing );
          }
          else {
            toggle = true;
            true_fieldRef.setData( true_field );
          }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }


      if (cmd.equals("spline")) {
        try {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);
          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );
        }
        catch (VisADException exc) {
        }
        catch (RemoteException exc) {
        }
      }
      if (cmd.equals("GCV")) {
        try {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);
          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
      if (cmd.equals("nreset")) {
        try {
          double noise_fac = ((Real)noise_ref.getData()).getValue();
          int iset = 0;
          double gset = 0;
          double fac,r,v1,v2;
          double gasdev;
          for ( int ii = 0; ii < n_samples; ii++ ) {
             if (iset == 0) {
               do {
                  v1 = (2*Math.random()-1.0);
                  v2 = (2*Math.random()-1.0);
                  r = v1*v1+v2*v2;
               } while (r >= 1.0 || r == 0.0);
               fac = Math.sqrt(-2.0*Math.log(r)/r);
               gset = v1*fac;
               iset = 1;
               gasdev = v2*fac;
            } else {
               iset = 0;
               gasdev = gset;
            }
            noise[ii]=gasdev;
            range_values[ii] = true_values[ii] + noise_fac*noise[ii];
            range_refs[ii].setData( new Real(RealType.YAxis, range_values[ii]));
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);

          mode = 1;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range );

          mode = 2;
          getspline_c( range_values, spline_range, val, mode, wkvalue);
          System.out.print(wkvalue[0] + "\n");
          wk_value=wkvalue[0];
          wk_ref.setData(new Real(RealType.XAxis, wk_value));
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );


          double tval = -10.0;
          mode = 1;
          for ( int ii = 0; ii < 50; ii++ ) {
                rlambda_values[ii] = 0.0;
                val = Math.pow(10.0, tval);
                getspline_c( range_values, spline_range, val, mode, wkvalue);
                for ( int kk = 0; kk < n_samples; kk++ ) {
                  rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
                }
                tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }


      if (cmd.equals("rlambda")) {
        try {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          double tval = -10.0;
          for ( int ii = 0; ii < 50; ii++ ) {
            rlambda_values[ii] = 0.0;
            val = Math.pow(10.0, tval);
            mode = 1;
            getspline_c( range_values, spline_range, val, mode, wkvalue);
            for ( int kk = 0; kk < n_samples; kk++ ) {
              rlambda_values[ii] += (spline_range[kk]-true_values[kk])*(spline_range[kk]-true_values[kk]);
            }
            tval += .2;
          }
          g_range[0] = rlambda_values;
          rlambda_field.setSamples( g_range );
        }
        catch (VisADException exc) {
        }
        catch (RemoteException exc) {
        }
      }
    }

  } // end class SplinePanel

  public native void getspline_c( double[] y, double[] y_s0, double val, int mode, double[] wkvalue );
}
