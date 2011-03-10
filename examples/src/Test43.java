/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test43
  extends UISkeleton
{
  public Test43() { }

  public Test43(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = new DisplayImplJ2D("display1");
    dpys[1] = new DisplayImplJ2D("display2");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    GraphicsModeControl mode;

    int domain_flag = 0;

    int LengthX = 201;
    int LengthY = 201;
    int n_samples = LengthX*LengthY;
    int ii, jj;
    int index;
    FlatField d_field;
    Set  domainSet = null;
    RealType x_axis = RealType.getRealType( "x_axis", SI.meter );
    RealType y_axis = RealType.getRealType( "y_axis", SI.meter );
    MathType Domain = (MathType) new RealTupleType( x_axis, y_axis );

    MathType rangeTemp = (MathType) RealType.getRealType( "Temperature", SI.kelvin );

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

    dpys[0].addMap( new ScalarMap( (RealType)x_axis, Display.XAxis ));
    dpys[0].addMap( new ScalarMap( (RealType)y_axis, Display.YAxis ));
    dpys[0].addMap( new ScalarMap( (RealType)rangeTemp, Display.Green));
    dpys[0].addMap( new ConstantMap( 0.5, Display.Red));
    dpys[0].addMap( new ConstantMap( 0.5, Display.Blue));
    /**
    ScalarMap map1contour;
    map1contour = new ScalarMap( (RealType)rangeTemp, Display.IsoContour );
    dpys[0].addMap( map1contour );
    ContourControl control1contour;
    control1contour = (ContourControl) map1contour.getControl();

    control1contour.enableContours(true);
    control1contour.enableLabels(false);
     **/

    mode = dpys[0].getGraphicsModeControl();
    mode.setScaleEnable(true);

    dpys[1].addMap( new ScalarMap( (RealType)x_axis, Display.XAxis ));
    dpys[1].addMap( new ScalarMap( (RealType)y_axis, Display.YAxis ));
    dpys[1].addMap( new ScalarMap( (RealType)f_range, Display.Green));
    dpys[1].addMap( new ConstantMap( 0.5, Display.Red));
    dpys[1].addMap( new ConstantMap( 0.5, Display.Blue));
     /**
    map1contour = new ScalarMap( (RealType)f_range, Display.IsoContour );
    dpys[1].addMap( map1contour );
    control1contour = (ContourControl) map1contour.getControl();

    control1contour.enableContours(true);
    control1contour.enableLabels(false);
      **/

    mode = dpys[1].getGraphicsModeControl();
    mode.setScaleEnable(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData( f_field );
    dpys[0].addReference( ref_imaget1, null);

    DataReferenceImpl ref_imaget2 = new DataReferenceImpl("ref_imaget2");
    ref_imaget2.setData( d_field );
    dpys[1].addReference( ref_imaget2, null );
  }

  String getFrameTitle() { return "sinusoidal field    and    (d/dx)field"; }

  public String toString()
  {
    return ": Function.derivative with Linear2DSet in Java2D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test43(args);
  }
}
