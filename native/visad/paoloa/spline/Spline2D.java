
//
// Spline2D.java
//

package visad.paoloa.spline;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.util.VisADSlider;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Spline2D {

  boolean toggle = true;
  Real nothing = new Real(-1000.0);

  DataReference lambda_ref;
  DataReference noise_ref;
  DataReference noise_button_ref;
  DataReference spline_ref;
  DataReference spline_fieldRef;
  DataReference true_fieldRef;
  DataReference gcv_fieldRef;

  int n_samples;
  float[] domain_valuesx;
  float[] domain_valuesy;
  double[] range_values;
  double[] return_values;
  double[] true_values;
  double[] range_values_pass;
  double[] true_values_pass;
  double[] s_values;
  double[] x_values;
  double[] y_values;
  double[] noise;
  double[][] f_range = new double[1][];
  double[] spline_range;
  double[] values = new double[3];
  double val;
  double noise_fac;
  double last_noise_fac;
  int mode;
  int dim1 = 11;
  int dim2 = 11;

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
    System.loadLibrary("Spline2D");
    Spline2D spline = new Spline2D("file");
  }

  public Spline2D(String filename)
         throws VisADException, RemoteException, IOException
  {

    lambda_ref = new DataReferenceImpl("lambda_ref");
    noise_ref = new DataReferenceImpl("noise_ref");
    noise_button_ref = new DataReferenceImpl("noise_button_ref");
    spline_ref = new DataReferenceImpl("spline_ref");

    //- get data
    // Paolo
    dim1 = 11;
    dim2 = 11;
    int pp = 0;
    n_samples = dim1*dim2;
    true_values = new double[ n_samples ];
    range_values = new double[ n_samples ];
    return_values = new double[ n_samples ];
    s_values = new double[ n_samples ];
    x_values = new double[ n_samples ];
    y_values = new double[ n_samples ];
    noise = new double[ n_samples ];
    domain_valuesx = new float[ n_samples ];
    domain_valuesy = new float[ n_samples ];
    spline_range = new double[ n_samples ];
    range_refs = new DataReferenceImpl[ n_samples ];
    tuples = new RealTuple[ n_samples ];
    reals = new Real[ n_samples ];
    cmaps = new ConstantMap[ n_samples ][];
    double x_c = 0.0;
    double y_c = 0.0;
    noise_fac = 0.1;
    last_noise_fac = noise_fac;
    // Paolo
    pp = 0;
    for ( int ii = 0; ii < dim1; ii++ ) {
       y_c=0.0;
       for ( int jj = 0; jj < dim2; jj++ ) {
         noise[pp]=(2*Math.random()-1);
         true_values[pp]=x_c*x_c-y_c*y_c;
         s_values[pp]=y_c*y_c;
         range_values[pp]=true_values[pp]+noise_fac*noise[pp];
         y_values[pp]=y_c;
         x_values[pp]=x_c;
         y_c += .1;
         pp += 1;
       }
       x_c += .1;
    }
    for ( int ii = 0; ii < n_samples; ii++ ) {
      domain_valuesx[ii] = (float) (ii % dim1);
      domain_valuesy[ii] = (float) (ii / dim1);
      values[0] = (double) domain_valuesx[ii];
      values[1] = (double) domain_valuesy[ii];
      values[2] = (double) range_values[ii];
      tuples[ii] = new RealTuple( RealTupleType.SpatialCartesian3DTuple, values );
      reals[ii] = new Real( RealType.ZAxis, values[2] );
      range_refs[ii] = new DataReferenceImpl("rangeRef_"+ii);
    //range_refs[ii].setData( tuples[ii] );
      range_refs[ii].setData( reals[ii] );
    }

    float[][] samples = new float[2][n_samples];
    samples[0] = domain_valuesx;
    samples[1] = domain_valuesy;
    RealTupleType domain_tuple =
      new RealTupleType(RealType.XAxis, RealType.YAxis);
    domainSet = new Gridded2DSet( domain_tuple, samples, dim1, dim2 );
    f_type = new FunctionType( domain_tuple, RealType.ZAxis );
    spline_field = new FlatField( f_type, domainSet );
    gcv_field = new FlatField( f_type, domainSet );
    true_field = new FlatField( f_type, domainSet );
    double[][] d_array = new double[1][];
    d_array[0] = true_values;
    true_field.setSamples(d_array);

    JFrame frame = new JFrame("Spline2D VisAD Application");
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
      display1 = new DisplayImplJ3D("image display");
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

      JButton noise_button = new JButton("noise");
      noise_button.addActionListener(this);
      noise_button.setActionCommand("noise");
      panel_d.add(noise_button);

      JButton GCV_button = new JButton("GCV");
      GCV_button.addActionListener(this);
      GCV_button.setActionCommand("GCV");
      panel_d.add(GCV_button);

      JButton toggle_button = new JButton("toggle");
      toggle_button.addActionListener(this);
      toggle_button.setActionCommand("toggle");
      panel_d.add(toggle_button);

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

      ConstantMap[] cmaps = new ConstantMap[6];
      cmaps[0] = new ConstantMap(1.0, Display.Red);
      cmaps[1] = new ConstantMap(0.0, Display.Green);
      cmaps[2] = new ConstantMap(0.0, Display.Blue);
      cmaps[3] = new ConstantMap(3.0, Display.PointSize);



      ScalarMap map_x = new ScalarMap( RealType.XAxis, Display.XAxis);
      map_x.setRange( 0., (double) (dim1 - 1));
      ScalarMap map_y = new ScalarMap( RealType.YAxis, Display.YAxis);
      map_y.setRange( 0., (double) (dim2 - 1));
      ScalarMap map_z = new ScalarMap( RealType.ZAxis, Display.ZAxis);
      display1.addMap( map_x );
      display1.addMap( map_y );
      display1.addMap( map_z );

      double display_valuex;
      double display_valuey;
      double[] scale_offsetx = new double[2];
      double[] scale_offsety = new double[2];
      double[] data_range = new double[2];
      double[] display = new double[2];
      map_x.getScale( scale_offsetx, data_range, display );
      map_y.getScale( scale_offsety, data_range, display );

      for ( int ii = 0; ii < n_samples; ii++ ) {
        display_valuex =
          ((double)domain_valuesx[ii])*scale_offsetx[0] + scale_offsetx[1];
        display_valuey =
          ((double)domain_valuesy[ii])*scale_offsety[0] + scale_offsety[1];
        cmaps[4] = new ConstantMap(  display_valuex,
                                     Display.XAxis );
        cmaps[5] = new ConstantMap(  display_valuey,
                                     Display.YAxis );
        display1.addReferences(new DirectManipulationRendererJ3D(),
                               range_refs[ii], cmaps );
      }

      spline_fieldRef = new DataReferenceImpl("spline_fieldRef");
      gcv_fieldRef = new DataReferenceImpl("gcv_fieldRef");
      true_fieldRef = new DataReferenceImpl("true_fieldRef");
      spline_fieldRef.setData( spline_field );
      gcv_fieldRef.setData( gcv_field );
      true_fieldRef.setData( true_field );
      // display1.addReference(spline_fieldRef);
      display1.addReference(gcv_fieldRef, green);
      display1.addReference(true_fieldRef, blue);

/*
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
*/

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
            range_refs[ii].setData( new Real(RealType.ZAxis, range_values[ii]));
          }

          int[] dimen = {dim1, dim2};
          System.arraycopy(range_values, 0, return_values, 0, n_samples);
          tpspline_c(x_values, y_values, s_values, true_values,
                     return_values, dimen);

          f_range[0] = return_values;
          gcv_field.setSamples( f_range );
          last_noise_fac = noise_fac;
        }
      };
      noise_cell.addReference( noise_button_ref );

    }

    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("noise")) {
        try {
          noise_button_ref.setData(new Real(0.0));
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
      if (cmd.equals("GCV")) {
        try {
          for ( int ii = 0; ii < n_samples; ii++ ) {
            range_values[ii] = ((Real)range_refs[ii].getData()).getValue();
          }
          val = ((Real)lambda_ref.getData()).getValue();
          val = Math.pow(10.0, val);
          mode = 2;
          // getspline_c( range_values, spline_range, val, mode);
          int[] dimen = {dim1, dim2};
          System.arraycopy(range_values, 0, return_values, 0, n_samples);
          tpspline_c(x_values, y_values, s_values, true_values,
                     return_values, dimen);
          f_range[0] = return_values;
          gcv_field.setSamples( f_range );
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
    }

  } // end class SplinePanel

  // public native void getspline_c( double[] y, double[] y_s0, double val, int mode );
  public native void tpspline_c( double[] x_array, double[] y_array,
                                       double[] s_array, double[] ytrue,
                                       double[] y, int[] dimen );
}
