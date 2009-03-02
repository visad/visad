/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Component;
import java.awt.event.*;
import java.net.URL;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.JLabel;

import visad.*;
import visad.data.mcidas.AreaAdapter;
import visad.data.mcidas.BaseMapAdapter;
import visad.georef.EarthLocation;
import visad.georef.EarthLocationTuple;
import visad.georef.MapProjection;
import visad.java3d.*;
import visad.jmet.GRIBCoordinateSystem;

/**
 * Example class to show how to create a Display side CoordinateSystem
 * for supporting various map projections.  Also shows how to create
 * a component for displaying the EarthLocation of the cursor.
 */
public class MapProjectionDisplay extends DisplayImplJ3D
{

    CoordinateSystem coordinateSystem = null;
    ScalarMap latitudeMap = null;
    ScalarMap longitudeMap = null;
    ScalarMap altitudeMap = null;
    JLabel locationLabel = new JLabel("Lat: Lon: Alt:");
    DisplayRendererJ3D displayRenderer;

    /**
     * Construct a new MapProjectionDisplay using an image for
     * the projection.  Will be displayed on a 2D surface
     * @param imageSource image to use
     */
    public MapProjectionDisplay(String imageSource)
        throws Exception
    {
        this(imageSource, false);
    }

    /**
     * Construct a new MapProjectionDisplay using an image for
     * the projection.
     * @param imageSource image to use for MapProjection
     * @param do3D  construct as a 3D display if true
     * @see visad.georef.MapProjection
     */
    public MapProjectionDisplay(String imageSource, boolean do3D)
        throws Exception
    {
        super("MapProjectionDisplay",
            (do3D == true)
                ? (DisplayRendererJ3D) new DefaultDisplayRendererJ3D()
                : (DisplayRendererJ3D) new TwoDDisplayRendererJ3D());

        displayRenderer = (DisplayRendererJ3D) getDisplayRenderer();

        if (imageSource != null) {
            // get an image and use the domain CS as the map projection
            AreaAdapter aa = new AreaAdapter(imageSource);
            coordinateSystem = 
                new MapProjectionAdapter( (MapProjection) aa.getCoordinateSystem());
        } else {  // use another known MapProjection class
            coordinateSystem = 
                // Lambert grid
                new MapProjectionAdapter(new GRIBCoordinateSystem(211));
                //LatLon Grid
                //new MapProjectionAdapter(
                //    new GRIBCoordinateSystem(0, 73, 73, -90, -30, 90, 240, 5, 2.5));
        }

        // create new DisplayTupleType
        // These are the DisplayTypes that Latitude/Longitude/Altitude
        // are mapped to.
        DisplayRealType displayLatitudeType =
            new DisplayRealType(
                "ProjectionLat",
                true, -90.0, 90.0, 0.0,
                CommonUnit.degree);
        DisplayRealType displayLongitudeType =
            new DisplayRealType(
                "ProjectionLon",
                true, -180.0, 180.0, 0.0,
                CommonUnit.degree);
        DisplayRealType displayAltitudeType =
            new DisplayRealType(
                "ProjectionAlt",
                true, -1.0, 1.0, -1.0, null);  // default of -1 sets at bottom
        DisplayTupleType displayTupleType =
            new DisplayTupleType(
                new DisplayRealType[] {
                    displayLatitudeType,
                    displayLongitudeType,
                    displayAltitudeType},
                    coordinateSystem);

        /* 
         * Add in the ScalarMaps
         *    RealType.Latitude  -> displayLatitudeType
         *    RealType.Longitude -> displayLongitudeType
         *    RealType.Altitude  -> displayAltitudeType
         * Thus any data that we add to the display that has lat/lon/alt
         * will get displayed automagically.
         *
         * We also set up ScalarMaps of:
         *    RealType.XAxis  -> Display.XAxis
         *    RealType.YAxis  -> Display.YAxis
         *    RealType.ZAxis  -> Display.ZAxis
         * to set the bounds for these axes.
         */
        latitudeMap = new ScalarMap(RealType.Latitude, displayLatitudeType);
        addMap(latitudeMap);
        latitudeMap.setRangeByUnits();

        longitudeMap = new ScalarMap(RealType.Longitude, displayLongitudeType);
        addMap(longitudeMap);
        longitudeMap.setRangeByUnits();

        ScalarMap map = new ScalarMap(RealType.XAxis, Display.XAxis);
        map.setRange(-1.0, 1.0);
        addMap(map);

        map = new ScalarMap(RealType.YAxis, Display.YAxis);
        map.setRange(-1.0, 1.0);
        addMap(map);

        if (do3D) {
            altitudeMap =
                new ScalarMap( RealType.Altitude, displayAltitudeType);
            altitudeMap.setRange(0, 16000);
            addMap(altitudeMap);

            map = new ScalarMap(RealType.ZAxis, Display.ZAxis);
            map.setRange(-1.0, 1.0);
            addMap(map);
        }

        /* uncomment if you want to see the image as well. 
        if (imageSource != null) {
            FlatField imgData = aa.getData();
            FunctionType ftype = (FunctionType) imgData.getType();
            RealTupleType rtype = (RealTupleType)ftype.getRange();
            RealType imageType = (RealType) rtype.getComponent(0);

            DataReference imageRef = new DataReferenceImpl("ref");
            imageRef.setData(imgData);
            ConstantMap[] zMap = 
                new ConstantMap[] { new ConstantMap(0.0, Display.ZAxis) };
            addReference(imageRef, zMap);
        }
        */
    
        DataReference mapRef = new DataReferenceImpl("maplines");
        BaseMapAdapter bma = 
            new BaseMapAdapter(
                new URL("ftp://ftp.ssec.wisc.edu/pub/visad-2.0/OUTLSUPW"));
        mapRef.setData(bma.getData());
        addReference(mapRef);

        // Enable the MOUSE_MOVED event so we can display the cursor's
        // EarthLocation
        enableEvent(DisplayEvent.MOUSE_MOVED);
        addDisplayListener(new DisplayListener() {
            public void displayChanged(DisplayEvent event) {
                int id = event.getId();
                try {
                    if (id == event.MOUSE_MOVED) {
                        pointerMoved(event.getX(), event.getY());
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        });

        // Add a KeyboardBehavior so we can use Ctrl-R to reset the
        // display and use other keyboard controls for zoom/pan
        displayRenderer.addKeyboardBehavior(
            new KeyboardBehaviorJ3D(displayRenderer));
    }

    /**
     * Test with 'java -Xmx64m MapProjectionDisplay <do3D> <image>'.
     * @param do3D   "true" if you want a 3D display, X for 2D
     * @param image  image for MapProjection (file or ADDE URL)
     */
    public static void main(String[] args)
        throws Exception
    {
        boolean do3D = 
            (args.length > 0)
              ? args[0].equalsIgnoreCase("true")
              : false;
        String imageSource = 
            (args.length > 1) 
                ? args[1] 
                : null;
        MapProjectionDisplay display = 
            new MapProjectionDisplay(imageSource, do3D);
        JFrame frame = new JFrame("Map Projection test");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        frame.getContentPane().add(display.getComponent());
        frame.getContentPane().add("South", display.getLocationIndicator());
        frame.pack();
        frame.show();
    }

    /**
     * Get the center lat/lon/alt of the projection.
     * @return center location
     */
    public EarthLocation getCenterPoint() {
        return getEarthLocation(new double[] {0, 0, 0});
    }

    /**
     * Get the EarthLocation of a point in XYZ space
     * @param  xyz  RealTuple with MathType 
     *              RealTupleType.SpatialCartesian3DTuple)
     * @return point in lat/lon/alt space.
     */
    public EarthLocation getEarthLocation(RealTuple xyz)
    {
        EarthLocation el = null;
        try
        {
            el = getEarthLocation(
                new double[] { ((Real) xyz.getComponent(0)).getValue(),
                               ((Real) xyz.getComponent(1)).getValue(),
                               ((Real) xyz.getComponent(2)).getValue()});
        }
        catch (VisADException e)
        { e.printStackTrace();}      // can't happen
        catch (RemoteException e)
        { e.printStackTrace();}      // can't happen
        return el;
    }

    /**
     * Get the EarthLocation of a point in XYZ space
     * @param  xyz  double[3] of x,y,z coords.
     * @return point in lat/lon/alt space.
     */
    public EarthLocation getEarthLocation(double[] xyz)
    {
        EarthLocationTuple value = null;
        try
        {
            float[][] numbers = visad.Set.doubleToFloat(
                coordinateSystem.fromReference(
                    new double[][] {
                        new double[] {xyz[0]},
                        new double[] {xyz[1]},
                        new double[] {xyz[2]}}));
            Real lat = new Real(
                RealType.Latitude,
                getScaledValue( latitudeMap, numbers[0][0]),
                coordinateSystem.getCoordinateSystemUnits()[0]);
            Real lon = new Real(
                RealType.Longitude,
                getScaledValue( longitudeMap, numbers[1][0]),
                coordinateSystem.getCoordinateSystemUnits()[1]);
            Real alt = null;
            if (isDisplay3D())
            {
                Real fakeAltitude = 
                    new Real( RealType.ZAxis,
                              getScaledValue( altitudeMap, numbers[2][0]),
                              coordinateSystem.getCoordinateSystemUnits()[2]);
                alt = new Real(RealType.Altitude, fakeAltitude.getValue()); 
            }
            else
            {
                alt = new Real(RealType.Altitude, 0);
            }
            value = new EarthLocationTuple(lat, lon, alt);
        }
        catch (VisADException e)
        { e.printStackTrace();}      // can't happen
        catch (RemoteException e)
        { e.printStackTrace();}      // can't happen
        return value;
    }

    /**
     * Returns the spatial (XYZ) coordinates of the particular EarthLocation
     * @return  RealTuple of display coordinates.
     */
    public RealTuple getSpatialCoordinates(EarthLocation el)
    {
        if (el == null)
            throw new NullPointerException(
              "MapProjectionDisplay.getSpatialCoorindate():  " + 
              "null input EarthLocation");
        RealTuple spatialLoc = null;
        try
        {
            float[][] temp = 
                coordinateSystem.toReference(
                    new float[][] {
                        latitudeMap.scaleValues(
                            new double[] { el.getLatitude().getValue(CommonUnit.degree) }),
                        longitudeMap.scaleValues(
                            new double[] { el.getLongitude().getValue(CommonUnit.degree) }),
                        (isDisplay3D() == true)
                            ? altitudeMap.scaleValues(
                               new double[] { el.getAltitude().getValue(CommonUnit.meter) })
                            : new float[] {0}
                    });
            double[] xyz = new double[3];
            xyz[0] = temp[0][0];
            xyz[1] = temp[1][0];
            xyz[2] = temp[2][0];
            spatialLoc = 
                new RealTuple(RealTupleType.SpatialCartesian3DTuple, xyz);

        }
        catch (VisADException e)
        { e.printStackTrace();}      // can't happen
        catch (RemoteException e)
        { e.printStackTrace();}      // can't happen
        return spatialLoc;
    }

    /** return a scaled value from the ScalarMap */
    private float getScaledValue(ScalarMap map, float value)
    {
        return (map != null) 
                  ? map.inverseScaleValues(new float[] {value})[0]
                  : 0.f;
    }

    /**
     * See if this is a 2D or 3D display.
     * @return  true if 3D
     */
    public boolean isDisplay3D() {
        return !displayRenderer.getMode2D();
    }

    /**
     * Handles a change in the position of the mouse-pointer.
     */
    private void pointerMoved(int x, int y)
            throws UnitException, VisADException, RemoteException {

        /*
         * Convert from (pixel, line) Java Component coordinates to (latitude,
         * longitude)
         */
        VisADRay ray        =
            displayRenderer.getMouseBehavior().findRay(x, y);
        EarthLocation el = 
            getEarthLocation(
                new double[] {ray.position[0], ray.position[1], ray.position[2] });
        locationLabel.setText(el.toString());
    }

    /**
     * Get the component that will show the location.
     * @return a component to show the cursor location.
     */
    public Component getLocationIndicator() {
        return locationLabel;
    }

    /**
     * An adapter for MapProjection coordinate systems (ie: ones with
     * a reference of Lat/Lon).  Allows for the conversion from lat/lon
     * to Display.DisplaySpatialCartesianTuple (XYZ).  Altitude (z) values
     * are held constant.
     */
    protected class MapProjectionAdapter extends CoordinateSystem
    {
        private final MapProjection mapProjection;
        private final int latIndex;
        private final int lonIndex;
        private final double scaleX;
        private final double scaleY;
        private final double offsetX;
        private final double offsetY;
    
        /**
         * Construct a new CoordinateSystem which uses a MapProjection for
         * the transformations between x,y and lat/lon.
         *
         * @param  mapProjection  CoordinateSystem that transforms from xy
         *                        in the data space to lat/lon.
         * @exception  VisADException  can't create the necessary VisAD object
         */
        public MapProjectionAdapter(MapProjection mapProjection)
            throws VisADException
        {
            super(
                Display.DisplaySpatialCartesianTuple, 
                new Unit[] 
                    {CommonUnit.degree, CommonUnit.degree, null});
            this.mapProjection = mapProjection;
            latIndex = mapProjection.getLatitudeIndex();
            lonIndex = mapProjection.getLongitudeIndex();
            java.awt.geom.Rectangle2D bounds = 
                    mapProjection.getDefaultMapArea();
            /*
            System.out.println("X = " + bounds.getX() +
                               " Y = "+ bounds.getY() +
                               " width = "+ bounds.getWidth() +
                               " height = "+ bounds.getHeight());
            */
            scaleX  = bounds.getWidth()/2.0;
            scaleY  = bounds.getHeight()/2.0;
            offsetX = bounds.getX() + scaleX;
            offsetY = bounds.getY() + scaleY;
        }
            
        /**
         * Transform latitude/longitude/altitude value to XYZ
         *
         * @param  latlonalt   array of latitude, longitude, altitude values
         * @return array of display xyz values.
         *
         * @exception VisADException  can't create the necessary VisAD object
         */
        public double[][] toReference(double[][] latlonalt) 
            throws VisADException 
        {
            if (latlonalt == null || latlonalt[0].length < 1) 
                throw new VisADException(
                    "MapProjection.toReference: null input array");
            int numpoints = latlonalt[0].length;
            double[][] t2 = new double[2][numpoints];
            for (int i = 0; i < numpoints; i++)
            {
                t2[latIndex][i] = latlonalt[0][i];
                t2[lonIndex][i] = latlonalt[1][i];
            }
            t2 = mapProjection.fromReference(t2);
            if (t2 == null) 
                throw new VisADException(
                    "MapProjection.toReference: " + 
                    "Can't do (lat,lon) to (x,y) transformation");
            for (int i = 0; i < numpoints; i++)
            {
                latlonalt[0][i] = (t2[0][i]-offsetX)/scaleX;
                latlonalt[1][i] = (t2[1][i]-offsetY)/scaleY;
            }
            return latlonalt;
        }
         
        /**
         * Transform display XYZ values to latitude/longitude/altitude
         *
         * @param  xyz   array of Display.DisplaySpatialCartesianTuple XYZ values
         * @return array of display lat/lon/alt values.
         *
         * @exception VisADException  can't create the necessary VisAD object
         */
        public double[][] fromReference(double[][] xyz) 
            throws VisADException 
        {
            if (xyz == null || xyz[0].length < 1) 
                throw new VisADException(
                    "MapProjection.fromReference: null input array");
            int numpoints = xyz[0].length;
            double[][] t2 = new double[2][numpoints];
            for (int i = 0; i < numpoints; i++)
            {
                t2[0][i] = xyz[0][i]*scaleX + offsetX;
                t2[1][i] = xyz[1][i]*scaleY + offsetY;
            }
            t2 = mapProjection.toReference(t2);
            if (t2 == null) 
                throw new VisADException(
                    "MapProjection.toReference: " + 
                    "Can't do (x,y) to (lat,lon) transformation");
            for (int i = 0; i < numpoints; i++)
            {
                xyz[0][i] = t2[latIndex][i];
                xyz[1][i] = t2[lonIndex][i];
            }
            return xyz;
        }
    
        public boolean equals(Object obj)
        {
            if (!(obj instanceof MapProjectionAdapter))
                return false;
            MapProjectionAdapter that = 
                (MapProjectionAdapter) obj;
            return
                (that.mapProjection).equals(mapProjection);
        }

        public String toString() {
            return "Using " + mapProjection.toString();
        }
    }
}
