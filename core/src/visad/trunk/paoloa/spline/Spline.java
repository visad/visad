
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
import visad.util.LabeledRGBWidget;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Spline {


  boolean java2d = true;
  DataReference lambda_ref;
  DataReference noise_ref;
  DataReference spline_ref;
  DataReference spline_fieldRef;
  DataReference true_fieldRef;
  DataReference gcv_fieldRef;

  int n_samples;
  float[] domain_values;
  double[] range_values;
  double[] true_values;
  double[] noise;
  double[][] f_range = new double[1][];
  double[] spline_range;
  double[] values = new double[2]; 
  double val;
  double noise_fac;
  double last_noise_fac;
  int mode;

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

    lambda_ref = new DataReferenceImpl("lambda_ref");
    noise_ref = new DataReferenceImpl("noise_ref");
    spline_ref = new DataReferenceImpl("spline_ref");

    //- get data

    n_samples = 50;
    true_values = new double[ n_samples ];
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
    for ( int ii = 0; ii < n_samples; ii++ )
    {
      noise[ii]=(2*Math.random()-1);
      true_values[ii]=Math.sin(x_c);
      range_values[ii]=true_values[ii]+noise_fac*noise[ii];
      x_c += .02*3.1415;
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

    SplinePanel spline_panel = new SplinePanel();

    // add image-spectrum interfaces to the JFrame
    panel.add(spline_panel);

    frame.getContentPane().add(spline_panel);
    frame.setSize(500, 500);
    frame.setVisible(true);
  }

  class SplinePanel extends JPanel
        implements ActionListener
  {
    JPanel panel_a, panel_b, panel_c, panel_d;
    Border etchedBorder5 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(5, 5, 5, 5));

    SplinePanel() throws VisADException, RemoteException 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentY(JPanel.TOP_ALIGNMENT);
      setAlignmentX(JPanel.LEFT_ALIGNMENT);

      panel_a = new JPanel();
      panel_a.setLayout(new BoxLayout(panel_a, BoxLayout.X_AXIS));
      panel_a.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_a.setAlignmentX(JPanel.LEFT_ALIGNMENT);

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
      GraphicsModeControl mode1 = display1.getGraphicsModeControl();
      mode1.setScaleEnable(true);

      panel_a.add( display1.getComponent() );
      panel_a.setBorder(etchedBorder5);
      add( panel_a );

      panel_b = new JPanel();
      panel_b.setLayout(new BoxLayout(panel_b, BoxLayout.X_AXIS));
      panel_b.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_b.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      panel_c = new JPanel();
      panel_c.setLayout(new BoxLayout(panel_c, BoxLayout.Y_AXIS));
      panel_c.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_c.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      VisADSlider noise_slider = 
        new VisADSlider(noise_ref, 0f, 1f, .1f, RealType.Generic, "noise");
      panel_c.add(noise_slider);

      VisADSlider lambda_slider = 
        new VisADSlider(lambda_ref, -10f, 0f, .5f, RealType.Generic, "lambda");
      panel_c.add(lambda_slider);
      panel_b.add( panel_c );

      panel_d = new JPanel();
      panel_d.setLayout(new BoxLayout(panel_d, BoxLayout.Y_AXIS));
      panel_d.setAlignmentY(JPanel.TOP_ALIGNMENT);
      panel_d.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      JButton spline_button = new JButton("spline");
      spline_button.addActionListener(this);
      spline_button.setActionCommand("spline");
      panel_d.add(spline_button);

      JButton GCV_button = new JButton("GCV");
      GCV_button.addActionListener(this);
      GCV_button.setActionCommand("GCV");
      panel_d.add(GCV_button);

      panel_b.add(panel_d);

      panel_b.setBorder(etchedBorder5);
  
      add( panel_b );
      
      ConstantMap[] blue =
      {
        new ConstantMap(0.0, Display.Red),
        new ConstantMap(0.0, Display.Green),
        new ConstantMap(1.0, Display.Blue), 
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
      display1.addMap( map_x );
      display1.addMap( map_y );

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
          display1.addReferences(new DirectManipulationRendererJ2D(),
                                 range_refs[ii], cmaps );
        }
      }
      else {
        for ( int ii = 0; ii < n_samples; ii++ ) {
          cmaps[4] = new ConstantMap( new Real(RealType.XAxis,(double) domain_values[ii]),
                                      Display.XAxis );
          display1.addReferences(new DirectManipulationRendererJ3D(),
                                 range_refs[ii], cmaps );
        }
      }

      spline_fieldRef = new DataReferenceImpl("spline_fieldRef");
      gcv_fieldRef = new DataReferenceImpl("gcv_fieldRef");
      true_fieldRef = new DataReferenceImpl("true_fieldRef");
      spline_fieldRef.setData( spline_field );
      gcv_fieldRef.setData( gcv_field );
      true_fieldRef.setData( true_field );
      display1.addReference(spline_fieldRef);
      display1.addReference(gcv_fieldRef, green);
      display1.addReference(true_fieldRef, blue);

      CellImpl lambda_cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);
          mode = 1;
          getspline_c( range_values, spline_range, val, mode);
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
          getspline_c( range_values, spline_range, val, mode);
          f_range[0] = spline_range;
          spline_field.setSamples( f_range ); 

          mode = 2;
          getspline_c( range_values, spline_range, val, mode);
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
          last_noise_fac = noise_fac;
        }
      };
      noise_cell.addReference( noise_ref );

    }

    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("spline")) {
        try {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);
          mode = 1;
          getspline_c( range_values, spline_range, val, mode);
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
          getspline_c( range_values, spline_range, val, mode);
          f_range[0] = spline_range;
          gcv_field.setSamples( f_range );
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
    }

  } // end class SplinePanel
  
  public native void getspline_c( double[] y, double[] y_s0, double val, int mode );
}
