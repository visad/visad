import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test43
	extends TestSkeleton
{
  public Test43() { }

  public Test43(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    int domain_flag = 0;

    int LengthX = 201;
    int LengthY = 201;
    int n_samples = LengthX*LengthY;
    int ii, jj;
    int index;
    FlatField d_field;
    Set  domainSet = null;
    RealType x_axis = new RealType( "x_axis", SI.meter, null );
    RealType y_axis = new RealType( "y_axis", SI.meter, null );
    MathType Domain = (MathType) new RealTupleType( x_axis, y_axis );

    MathType rangeTemp = (MathType) new RealType( "Temperature", SI.kelvin, null );

    FunctionType domain_temp = new FunctionType( Domain, rangeTemp );

    if ( domain_flag == 0 )
    {
       domainSet = (Set) new Linear2DSet( Domain, 0.d, 1000.d, LengthX,
                                          0.d, 1000.d, LengthY );
    }
    else if ( domain_flag == 1 )
    {
      float[][] d_samples = new float[2][n_samples];

      index = 0;
      for ( ii = 0; ii < LengthY; ii++ ) {
        for ( jj = 0; jj < LengthX; jj++ ) {
          d_samples[0][index] = jj*5f;
          d_samples[1][index] = ii*5f;
          index++;
        }
      }
      domainSet = (Set) new Gridded2DSet( Domain, d_samples, LengthX, LengthY,
                                          null, null, null );
    }
    else if ( domain_flag == 3)
    {

    }

    FlatField f_field = new FlatField( domain_temp, domainSet );

    double[][] samples = new double[1][n_samples];

    index = 0;
    double wave_number = 2;
    double PI = Math.PI;
    for ( ii = 0; ii < LengthY; ii++ )
    {
      for ( jj = 0; jj < LengthX; jj++ )
      {
        samples[0][index] =  (50)*Math.sin( ((wave_number*2d*PI)/1000)*5*jj )*
                                  Math.sin( ((wave_number*2d*PI)/1000)*5*ii );
        index++;
      }
    }
    f_field.setSamples( samples );

    System.out.println("Starting derivative computation...");
      d_field = (FlatField) f_field.derivative( x_axis, Data.NO_ERRORS );
    System.out.println("...derivative done");

    RealType f_range = (RealType) ((FunctionType)d_field.getType()).getRange();

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap( new ScalarMap( (RealType)x_axis, Display.XAxis ));
    display1.addMap( new ScalarMap( (RealType)y_axis, Display.YAxis ));
    display1.addMap( new ScalarMap( (RealType)rangeTemp, Display.Green));
    display1.addMap( new ConstantMap( 0.5, Display.Red));
    display1.addMap( new ConstantMap( 0.5, Display.Blue));
    /**
    ScalarMap map1contour;
    map1contour = new ScalarMap( (RealType)rangeTemp, Display.IsoContour );
    display1.addMap( map1contour );
    ContourControl control1contour;
    control1contour = (ContourControl) map1contour.getControl();

    control1contour.enableContours(true);
    control1contour.enableLabels(false);
     **/
    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setScaleEnable(true);

    DisplayImpl display2 = new DisplayImplJ2D("display2");
    display2.addMap( new ScalarMap( (RealType)x_axis, Display.XAxis ));
    display2.addMap( new ScalarMap( (RealType)y_axis, Display.YAxis ));
    display2.addMap( new ScalarMap( (RealType)f_range, Display.Green));
    display2.addMap( new ConstantMap( 0.5, Display.Red));
    display2.addMap( new ConstantMap( 0.5, Display.Blue));
     /**
    map1contour = new ScalarMap( (RealType)f_range, Display.IsoContour );
    display2.addMap( map1contour );
    control1contour = (ContourControl) map1contour.getControl();

    control1contour.enableContours(true);
    control1contour.enableLabels(false);
      **/

    mode = display2.getGraphicsModeControl();
    mode.setScaleEnable(true);

    JFrame jframe;
    jframe = new JFrame("   sinusoidal field    and    (d/dx)field");
    jframe.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData( f_field );
    display1.addReference( ref_imaget1, null);

    DataReferenceImpl ref_imaget2 = new DataReferenceImpl("ref_imaget2");
    ref_imaget2.setData( d_field );
    display2.addReference( ref_imaget2, null );

    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
    big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(display1.getComponent());
    big_panel.add(display2.getComponent());
    jframe.setContentPane(big_panel);
    jframe.setSize(800, 400);
    jframe.setVisible(true);

    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = display1;
    dpys[1] = display2;

    return dpys;
  }

  public String toString()
  {
    return ": Function.derivative test with Linear2DSet in Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test43 t = new Test43(args);
  }
}
