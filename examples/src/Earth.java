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

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.FlatField;
import visad.FunctionType;
import visad.RealType;
import visad.RealTupleType;
import visad.ScalarMap;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.util.LabeledColorWidget;


/**
 * Draws a picture of the globe with topography and color.
 */
class Earth
{
    /*
     * Set altitude scaling.
     */
    private static double[]
    setAltitudeScaling(ScalarMap radiusMap)
        throws InterruptedException, VisADException, RemoteException
    {
        double[]        coeffs = new double[2];         // scale and offset
        double[]        altitudeRange = new double[2];  // data min and max
        double[]        radiusRange = new double[2];    // display min and max

        radiusMap.getScale(coeffs, altitudeRange, radiusRange);
        while (Double.isNaN(altitudeRange[0]))
        {
            Thread.sleep(1000);
            radiusMap.getScale(coeffs, altitudeRange, radiusRange);
        }

        double[]        newRadiusRange = {0.925, 1.075};
        double          newSlope = (newRadiusRange[1] - newRadiusRange[0]) /
            (altitudeRange[1] - altitudeRange[0]);
        double          newIntercept = newRadiusRange[0] -
            newSlope * altitudeRange[0];
        double          newMinAltitude = (radiusRange[0] - newIntercept) /
            newSlope;
        double          newMaxAltitude = (radiusRange[1] - newIntercept) /
            newSlope;

        radiusMap.setRange(newMinAltitude, newMaxAltitude);

        return altitudeRange;
    }


    /**
     * Set the color map.
     */
    private static void
    setColorMap(ScalarMap colorMap, double min, double max)
        throws VisADException, RemoteException
    {
/* WLH 11 Dec 98
        LabeledColorWidget lw =
            new LabeledColorWidget(colorMap, (float)min, (float)max);
*/
        LabeledColorWidget lw =
            new LabeledColorWidget(colorMap);

        Frame frame = new Frame("VisAD Color Widget");
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        frame.add(lw);
        frame.setSize(lw.getPreferredSize());
        frame.setVisible(true);
    }


    /**
     * Test this class.
     */
    public static void
    main(String[] args)
        throws Exception
    {
        /* CTR: 28 Sep 1998 */
        // print a nice error message if user doesn't specify a file
        if (args.length < 1) {
          System.out.println("Usage: \"java Earth file.nc\", " +
                             "where file.nc is a netCDF file.");
          System.out.println("This program is designed to work with the " +
                             "lowresTerrain.nc file available at:");
          System.out.println("  ftp://ftp.ssec.wisc.edu/pub/" +
                             "visad-2.0/lowresTerrain.nc");
          System.exit(0);
        }

        GeoDisplay      display = new GeoDisplay();
        FlatField       earth = (FlatField) new Plain().open(args[0]);
        FunctionType    earthType = (FunctionType) earth.getType();
        RealType        altitudeType = (RealType)earthType.getRange();

        /* WLH 11 Sept 98 - this works */
        RealTupleType domain = earthType.getDomain();
        RealType lon = (RealType) domain.getComponent(0);
        RealType lat = (RealType) domain.getComponent(1);
        display.addMap(new ScalarMap(lon, Display.Longitude));
        display.addMap(new ScalarMap(lat, Display.Latitude));

        ScalarMap       radiusMap = new ScalarMap(altitudeType, Display.Radius);
        ScalarMap       colorMap = new ScalarMap(altitudeType, Display.RGB);
        DataReference   earthRef = new DataReferenceImpl("earthRef");

        display.addMap(radiusMap);
        display.addMap(colorMap);

        earthRef.setData(earth);

        display.addReference(earthRef);

        double[]        altitudeRange = setAltitudeScaling(radiusMap);
        setColorMap(colorMap, altitudeRange[0], altitudeRange[1]);

    }
}
