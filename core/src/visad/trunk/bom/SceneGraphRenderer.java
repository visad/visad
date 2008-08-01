/*
  This work was originally done by Rob Hackett (R.Hackett@bom.gov.au) at the Australian Bureau of Meteorology
  in the au.gov.bom.aifs.osa.charts package
  Jeff McWhirter (jeffmc@unidata.ucar.edu) refactored it to remove dependencies on the charts package
  and have it be a stand-alone scene graph renderer for visad Displays

Copyright (C) 2008 Australian Bureau of Meteorology.

This library is free software; you can redistribute it and/or modify it under the terms of the
GNU Lesser General Public License as published by the Free Software Foundation; either version
2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with this
library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
Boston, MA 02111-1307 USA
*/




package visad.bom;

import visad.*;

import visad.java2d.DisplayImplJ2D;
import visad.java2d.DisplayRendererJ2D;

import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;


import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import java.rmi.RemoteException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import javax.media.j3d.*;

import javax.swing.*;

import javax.vecmath.Color3f;



/**
 * Render the non-texture components of a scene graph to a Graphics2D.
 * This does not handle any 3D aspect, rotation, etc. 
 */
public class SceneGraphRenderer {

    /** _more_          */
    private final int DEFAULT_WIDTH = 640;

    /** _more_          */
    private final int DEFAULT_HEIGHT = 512;

    /** _more_          */
    protected int width = DEFAULT_WIDTH;

    /** _more_          */
    protected int height = DEFAULT_HEIGHT;

    /** _more_          */
    public static final int MODE_2D = 2;

    /** _more_          */
    public static final int MODE_3D = 3;

    // Default to 3d mode

    /** _more_          */
    private int mode = MODE_3D;

    /** _more_          */
    protected float pixelWidth = 1.0f;

    /** _more_          */
    private Color pageColour;

    /** _more_          */
    private Color frameColour = Color.black;


    /** _more_          */
    Hatching hatching = new Hatching();



    /** _more_          */
    protected boolean monochrome;

    /** _more_          */
    protected boolean gradientFill = false;

    /** _more_          */
    protected boolean useTransparency = true;

    /** _more_          */
    protected ArrayList colours = new ArrayList();

    /** _more_          */
    private GeneralPath linePath;

    /** _more_          */
    protected boolean plotMap = true;

    /** _more_          */
    protected AffineTransform viewPort;

    CoordinateSystem coordSys;

    /** _more_          */
    private double lineThickness = 1;


    public SceneGraphRenderer() {
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private CoordinateSystem getCoordinateSystem() {
        return coordSys;
    }


    private AffineTransform getViewport(DisplayImpl displayImpl) {
        ProjectionControl pc =   displayImpl.getProjectionControl();
        MouseBehavior behavior =
            displayImpl.getDisplayRenderer().getMouseBehavior();

        double[] tstart = pc.getMatrix();
        double[] rotArray   = new double[3];
        double[] scaleArray = new double[3];
        double[] transArray = new double[3];
        behavior.instance_unmake_matrix(rotArray, scaleArray, transArray, tstart);

        float lScaleX = (float)scaleArray[0];
        float lScaleY = (float)scaleArray[1];
        float lTransX = (float)transArray[0];
        float lTransY = (float)transArray[1];


        // Get the panning/scanning of the location
        // Convert the 3D viewport from the Location, into a 2D
        // Affine transform. 
        // get translation, then append scale NOT other way round
        viewPort =
            AffineTransform.getTranslateInstance(lTransX, lTransY);
        AffineTransform scale =
            //            AffineTransform.getScaleInstance(lScale, lScale);
            AffineTransform.getScaleInstance(lScaleX, lScaleY);
        viewPort.concatenate(scale);

        // Create another transform which matches the viewport
        // coordinates to the device coordinates
        float deviceScaleX = (float)width / 2.0f;
        float deviceTransX = width / 2.0f;
        float deviceTransY = height / 2.0f;
        AffineTransform deviceTrans =
            AffineTransform.getTranslateInstance(deviceTransX,
                                                 deviceTransY);
        AffineTransform deviceScale =
            AffineTransform.getScaleInstance(deviceScaleX,
                                             -deviceScaleX);
        deviceTrans.concatenate(deviceScale);

        // Join the two together so that coordinates in viewport space
        // line up with the output coordinates
        viewPort.preConcatenate(deviceTrans);


		
        return viewPort;
    }



    /**
     * Return an array of Longitudes/Latitudes corresponding to the
     * pixels of the chart. Based on the Charts CoordinateSystem and
     * ViewPort.
     *      In the case of an A0 chart, the number of pixels may be
     * huge, so supply an X and Y size allowing a smaller subset to be used
     * if necessary
     * The purpose of this method is to allow an image to be
     * interpolated onto the chart as an overlay (eg. sat image)
     * @param xSize The size of the target X dimension
     * @param ySize The size of the target Y dimension
     * @return An array of Longitudes/Latitudes corresponding to the
     * pixels of the chart, sampled to match the target x/y size.
     * Based on the Charts CoordinateSystem and ViewPort
     */
    public float[][] getLonLatSamples(int xSize, int ySize) {
        float            xScale        = (float) width / (float) xSize;
        float            yScale        = (float) height / (float) ySize;
        CoordinateSystem coordSys      = getCoordinateSystem();
        float[][]        lonLatSamples = null;
        try {
            AffineTransform inverse = viewPort.createInverse();
            // Get the locations of each pixel of the chart
            float[] pixelSamples = getPixelSamples(xScale, yScale);
            // Normalise these using the inverse of the ViewPort
            float[] normSamples = new float[xSize * ySize * 2];
            inverse.transform(pixelSamples, 0, normSamples, 0, xSize * ySize);

            // Convert normal coordinates to lons/lats, using the
            // chart coordinate system
            float[][] xys = new float[3][normSamples.length / 2];
            for (int i = 0; i < normSamples.length / 2; i++) {
                xys[0][i] = normSamples[i * 2];
                xys[1][i] = normSamples[i * 2 + 1];
                xys[2][i] = 0.0f;
            }

            float[][] lonLats3d = coordSys.fromReference(xys);
            // Finally, filter out the extraneous 3rd dimension
            float[][] lonLats2D = {
                lonLats3d[1], lonLats3d[0]
            };
            lonLatSamples = lonLats2D;
        } catch (NoninvertibleTransformException e) {
            System.err.println("Chart.getLonLatSamples: " + e);
        } catch (VisADException e) {
            System.err.println("Chart.getLonLatSamples: " + e);
        }

        return lonLatSamples;
    }

    /**
     * Get the x/y coordinate of every pixel in the chart
     * @param width
     * @param height
     *
     * @param xScale _more_
     * @param yScale _more_
     * @return
     */
    private float[] getPixelSamples(float xScale, float yScale) {
        int     xSize   = (int) ((float) width / xScale);
        int     ySize   = (int) ((float) height / yScale);
        float[] samples = new float[xSize * ySize * 2];
        int     cnt     = 0;
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                samples[cnt]     = i * xScale;
                samples[cnt + 1] = j * yScale;
                cnt              += 2;
            }
        }

        return samples;
    }


    /**
     * Get a list of the colours used in the chart
     * This may be necessary if the chart is being plotted to a medium
     * with limited colours, eg. 8 bit PNG. The antialiasing and gradient
     * fill used in some charts can easily grab all the available colours
     * By taking the ones used specifically by the chart, the important
     * colours can be reserved in the colour table ensuring that any
     * colour loss has only minimal cosmetic effect
     * @return The colours used by this chart
     */
    public Color[] getColours() {
        Color[] colourArr = new Color[colours.size()];
        colours.toArray(colourArr);

        return colourArr;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public float getLineWidth() {
        return (float) lineThickness;
    }

    /**
     * Create a gradient fill. If this is set with graphics.setPaint()
     * then any shapes which are filled will have a slight gradient
     * from darker to lighter along the line specified by x1, y1 to x2, y2
     * @param colour The colour of the middle point of the gradient
     * @param x1 The X coordinate of the point where the darkest part of
     *  the gradient will be
     * @param y1 The Y coordinate of the point where the darkest part of
     *  the gradient will be
     * @param x2 The X coordinate of the point where the lightest part of
     *  the gradient will be
     * @param y2 The Y coordinate of the point where the lightest part of
     *  the gradient will be
     * @return
     */
    private GradientPaint makeGradient(Color colour, float x1, float y1,
                                       float x2, float y2) {
        float[] rgb = new float[4];
        colour.getRGBColorComponents(rgb);

        // Define the difference between the lightest and darkest
        // colours
        float   diff    = 0.05f;
        float[] darkRGB = new float[4];
        darkRGB[0] = rgb[0] * (1.0f - diff);
        darkRGB[1] = rgb[1] * (1.0f - diff);
        darkRGB[2] = rgb[2] * (1.0f - diff);
        darkRGB[3] = rgb[3];
        float[] lightRGB = new float[4];
        lightRGB[0] = rgb[0] * (1.0f + diff);
        lightRGB[1] = rgb[1] * (1.0f + diff);
        lightRGB[2] = rgb[2] * (1.0f + diff);
        lightRGB[3] = rgb[3];
        for (int i = 0; i < 4; i++) {
            if (lightRGB[i] > 1.0) {
                lightRGB[i] = 1.0f;
            }
        }
        Color dark = new Color(darkRGB[0], darkRGB[1], darkRGB[2]);
        Color light = new Color(lightRGB[0], lightRGB[1], lightRGB[2]);
        GradientPaint gradient = new GradientPaint(x1, y1, dark, x2, y2,
                                     light);

        return gradient;
    }



    /**
     * _more_
     *
     * @param graphics _more_
     */
    private void render(Graphics2D graphics, DisplayImpl display) {
        copyVisadDisplay(display, graphics);
    }


    /**
     * Implements the Plottable interface. This will plot the chart
     * to a vector graphics file
     * Don't use this method directly. It is called by the Plotter class
     * @param Graphics2D The java2d object used by the third party graphics
     * library to render the graphics file
     * @param pageFormat The shape/size of the target image file
     *
     * @param graphics _more_
     */
    public void plot(Graphics2D graphics, DisplayImpl display, CoordinateSystem cs, int width, int height) {
        this.width  = width;
        this.height = height;
        
        coordSys = cs;
        viewPort = getViewport(display);

        /*jeffmc: We don't do this for now
        // If a pre rendered background has been supplied
        if ( !plotMap) {
            // Add this to the start of the SVG
            graphics.setColor(Color.BLACK);
            //            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
        } else {
            // Otherwise 
            if (gradientFill) {
                GradientPaint oceanGradient = makeGradient(pageColour, width,
                                                  height, 0, 0);
                graphics.setPaint(oceanGradient);
            } else {
                graphics.setColor(pageColour);
            }
            graphics.fillRect(0, 0, width, height);
            }*/

        int lineThick = (int) lineThickness;
        graphics.setClip(0, 0, width, height);

        // Render the chart
        render(graphics, display);

        // Draw a frame around the page
        /*
        drawEdge((Graphics2D) graphics, width, height,
                 (float) lineThickness * 2);
        ((Graphics2D) graphics).setColor(frameColour);
        ((Graphics2D) graphics).setStroke(getStroke(lineThick));
        */
        colours.add(frameColour);
    }

    /**
     * _more_
     *
     * @param graphics _more_
     * @param pageFormat _more_
     * @param pageIndex _more_
     *
     * @return _more_
     */
    public int print(Graphics graphics, PageFormat pageFormat,
                     int pageIndex, DisplayImpl display, CoordinateSystem cs) {

        coordSys = cs;
        viewPort = getViewport(display);
        // Charts are ALL only 1 page
        // You MUST do this, otherwise it prints infinite copies
        // which can be a bad thing
        if (pageIndex > 0) {
            return Printable.NO_SUCH_PAGE;
        }

        // DO NOT use trasparency if chart is to be printed
        // It causes the image to be rasterized which takes
        // up lots of memory
        useTransparency = false;

        // Assuming a 2d graphics ...
        if (graphics instanceof Graphics2D) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            // Scale the chart to the page format
            float scale = scaleToPage(pageFormat, graphics2D, width, height);

            // Reduce the line thinkness to take advantage of the extra
            // resolution of the printer
            lineThickness = 1.0 / scale;

            graphics2D.setColor(pageColour);
            if (plotMap) {
                graphics2D.fillRect(0, 0, width, height);
            }
            graphics2D.setClip(0, 0, width, height);

            // Render the chart
            render(graphics2D, display);

            boolean transparent = !monochrome && useTransparency;
            float   legendScale = (float) lineThickness * 4;

            drawEdge(graphics2D, width, height, (float) lineThickness * 4);

            colours.add(frameColour);
        } else {
            System.err.println("Wrong graphics type!"
                               + " How did THAT happen?");
        }

        return Printable.PAGE_EXISTS;
    }

    /**
     * Draw a frame around the page
     * @param graphics
     * @param width
     * @param height
     * @param thickness
     */
    private void drawEdge(Graphics2D graphics, int width, int height,
                          float thickness) {
        graphics.setColor(frameColour);
        graphics.setStroke(getStroke(thickness));

        float top    = 0;
        float bottom = (float) height;
        float left   = 0;
        float right  = (float) width;

        // Cant use draw rect because we need float parameters
        GeneralPath linePath = new GeneralPath();
        linePath.moveTo(left, top);
        linePath.lineTo(right, top);
        linePath.lineTo(right, bottom);
        linePath.lineTo(left, bottom);
        linePath.lineTo(left, top);

        graphics.draw(linePath);
    }

    /**
     * Scale the chart so that it will fit onto the page
     * @param pageFormat The specifications of the page
     * @param graphics The graphics context
     * @param xSize The width of the chart
     * @param ySize The height of the chart
     * @return The amount that the chart has to be rescaled to match the
     * resolution of the printer
     */
    private float scaleToPage(PageFormat pageFormat, Graphics2D graphics,
                              int xSize, int ySize) {
        // Assume printers have a DPI of 300
        // TODO Can this be inferred from the PrintService?
        final int DPI = 300;

        // Find out the size of the imageable part of the paper
        // (in units of 1/72 inches)
        double pageWidth72  = pageFormat.getImageableWidth();
        double pageHeight72 = pageFormat.getImageableHeight();

        // Scale the chart to fit this
        double scaleX = pageWidth72 / xSize;
        double scaleY = pageHeight72 / ySize;
        // Choose the smallest of the 2 dimension scales 
        // Ensures that both dimensions will fit
        double scale = scaleX;
        if (scaleY < scaleX) {
            scale = scaleY;
        }

        // Translate past the non-imageable bit
        double transX = pageFormat.getImageableX() / scale;
        double transY = pageFormat.getImageableY() / scale;

        // Stretch the chart to match the dimensions of the paper
        graphics.scale(scale, scale);
        graphics.translate(transX, transY);

        // Calculate how small a pixel is relative to the basic
        // unit of size
        // There are 300 pixels per inch
        // One unit of size is 1/72 inch
        // Dots per unit tells us how much to shrink the line width down
        // to make it use a single dot
        float dotsPerUnit = (DPI / 72);

        return dotsPerUnit * (float) scale;
    }

    /**
     * _more_
     *
     * @param vertices _more_
     * @param colour _more_
     * @param width _more_
     * @param graphics _more_
     */
    public void drawShapeReprojected(float[][] vertices, Color colour,
                                     float width, Graphics2D graphics) {
        drawShapeReprojected(vertices, colour, width, false, graphics);
    }

    /**
     * _more_
     *
     * @param vertices _more_
     * @param colour _more_
     * @param width _more_
     * @param dashed _more_
     * @param graphics _more_
     */
    public void drawShapeReprojected(float[][] vertices, Color colour,
                                     float width, boolean dashed,
                                     Graphics2D graphics) {

        graphics.setColor(colour);
        if (dashed) {
            float[] dash = { 2.0f * pixelWidth, 6.0f * pixelWidth };
            graphics.setStroke(getStroke(width, dash));
        } else {
            graphics.setStroke(getStroke(width));
        }

        GeneralPath line = new GeneralPath();
        line.moveTo(vertices[0][0], vertices[1][0]);
        for (int j = 1; j < vertices[0].length; j++) {
            line.lineTo(vertices[0][j], vertices[1][j]);
        }
        line.transform(viewPort);
        line = clip(line);
        graphics.setColor(colour);
        //TODO: the colour have a 0 alpha
        graphics.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue()));
        graphics.draw(line);
    }

    /**
     * Draw the outline of a shape onto the chart
     * @param vertices The vertices of the shape. A 2-Dimensional array.
     * The first dimension contains the longitude values of the shape
     * The second dimension contains the latitude values of the shape
     * @param colour The colour of the shape
     * @param width The thickness of the outline
     * @param dashed Set to true of the outline is to be drawn as dashes
     * @param graphics The java2d graphics object supplied by the
     * plotting/printing medium
     * @throws VisADException
     */
    public void drawShape(float[][] vertices, Color colour, float width,
                          boolean dashed, Graphics2D graphics)
            throws VisADException {
        // Get the Coordinate System of the current location
        CoordinateSystem coordSys = getCoordinateSystem();

        // Filter out shapes which straddle the discontinuity of
        // a mercator projection. 
        boolean crosses = crossesDiscontinuity(coordSys, vertices);
        if ( !crosses) {
            // Reproject the coordinates to the native coordinate system
            // eg. Longitude, Latitude -> X, Y
            float[][] reprojected = coordSys.toReference(vertices);

            drawShapeReprojected(reprojected, colour, width, dashed,
                                 graphics);
        }
    }

    /**
     * Draw the outline of a shape onto the chart
     * @param vertices The vertices of the shape. A 2-Dimensional array.
     * The first dimension contains the longitude values of the shape
     * The second dimension contains the latitude values of the shape
     * @param colour The colour of the shape
     * @param width The thickness of the outline
     * @param graphics The java2d graphics object supplied by the
     * plotting/printing medium
     * @throws VisADException
     */
    public void drawShape(float[][] vertices, Color colour, float width,
                          Graphics2D graphics)
            throws VisADException {
        drawShape(vertices, colour, width, false, graphics);
    }

    /**
     * _more_
     *
     * @param vertices _more_
     * @param colour _more_
     * @param graphics _more_
     */
    public void fillShapeReprojected(float[][] vertices, Color colour,
                                     Graphics2D graphics) {
        if (gradientFill) {
            GradientPaint gradient = makeGradient(colour, 0, 0, width,
                                         height);
            graphics.setPaint(gradient);
        } else {
            graphics.setColor(colour);
        }
        GeneralPath shape = new GeneralPath();
        if (vertices[0].length > 0) {
            shape.moveTo(vertices[0][0], vertices[1][0]);
            for (int j = 1; j < vertices[0].length; j++) {
                shape.lineTo(vertices[0][j], vertices[1][j]);
            }
            shape.transform(viewPort);
            shape = clip(shape);
            graphics.fill(shape);
        }
    }

    /**
     * Fill a shape onto the chart
     * @param vertices  The vertices of the shape. A 2-Dimensional array.
     * The first dimension contains the longitude values of the shape
     * The second dimension contains the latitude values of the shape
     * @param colour The colour of the shape
     * @param graphics The java2d graphics object supplied by the
     * plotting/printing medium
     * @throws VisADException
     */
    public void fillShape(float[][] vertices, Color colour,
                          Graphics2D graphics)
            throws VisADException {
        // Reproject the lats/lons into device coordinates
        CoordinateSystem coordSys = getCoordinateSystem();

        // Filter out shapes which straddle the discontinuity of
        // a mercator projection.               
        boolean crosses = crossesDiscontinuity(coordSys, vertices);
        if ( !crosses) {
            float[][] reprojected = coordSys.toReference(vertices);

            // Fill the shape in device coordinates
            fillShapeReprojected(reprojected, colour, graphics);
        }
    }

    /**
     *  Test a polygon to see if it crosses the discontinuity of a
     *  Mercator projection. Shapes that do this, streak across the entire
     *  width of the chart and need to be filtered out
     * @param coordSys The CoordinateSystem of the chart
     * @param vertices The vertices of the polygon being tested
     * @return true if the shape crosses the discontinuity of a
     *  Mercator projection
     */
    private boolean crossesDiscontinuity(CoordinateSystem coordSys,
                                         float[][] vertices) {
        boolean crosses = false;
        /*TODO:
        if (coordSys instanceof visad.earthmap.MercatorCoordinateSystem) {
            visad.earthmap.MercatorCoordinateSystem merc =
                (visad.earthmap.MercatorCoordinateSystem) coordSys;
            double  disco    = merc.getCentreLongitude() + 180.0;

            float[] lonRange = range(vertices[0]);
            if ((lonRange[0] < disco) && (lonRange[1] > disco)) {
                return true;
            }
            }*/

        return crosses;
    }

    /**
     * Get the range of an array of floats
     * @param array
     * @return
     */
    private float[] range(float[] array) {
        int numPoints = array.length;

        if (numPoints <= 1) {
            float[] range = { array[0], array[0] };
            return range;
        }
        float[] clone = (float[]) array.clone();
        java.util.Arrays.sort(clone);

        float[] range = new float[2];
        range[0] = clone[0];
        range[1] = clone[clone.length - 1];

        return range;
    }

    /**
     * _more_
     *
     * @param vertices _more_
     * @param texture _more_
     * @param graphics _more_
     */
    public void fillShapeReprojected(float[][] vertices, int texture,
                                     Graphics2D graphics) {
        BufferedImage fillTexture = hatching.getPattern(texture);
        Rectangle     anchor      = new Rectangle(30, 30);
        TexturePaint  hatching    = new TexturePaint(fillTexture, anchor);
        graphics.setPaint(hatching);

        GeneralPath shape = new GeneralPath();
        shape.moveTo(vertices[0][0], vertices[1][0]);
        for (int j = 1; j < vertices[0].length; j++) {
            shape.lineTo(vertices[0][j], vertices[1][j]);
        }
        graphics.setPaint(hatching);
        shape.transform(viewPort);
        shape = clip(shape);
        graphics.fill(shape);
    }

    /**
     * Fill a shape onto the chart
     * @param vertices  The vertices of the shape. A 2-Dimensional array.
     * The first dimension contains the longitude values of the shape
     * The second dimension contains the latitude values of the shape
     *
     * @param data _more_
     * @param texture The hatching texture to fill the shape with
     * can be Hatching.DIAGONAL1, Hatching.DIAGONAL2,
     * Hatching.DIAGONAL_BOTH, Hatching.HORIZONTAL = 3, Hatching.VERTICAL
     * or Hatching.SQUARE
     * @param graphics The java2d graphics object supplied by the
     * plotting/printing medium
     * @throws VisADException
     */
    public void fillShape(float[][] data, int texture, Graphics2D graphics)
            throws VisADException {
        CoordinateSystem coordSys    = getCoordinateSystem();
        float[][]        reprojected = coordSys.toReference(data);

        fillShapeReprojected(reprojected, texture, graphics);
    }

    /**
     * Draw text onto the chart
     * @param text The text to draw onto the chart
     * @param font The font of the text
     * @param colour The colour of the text
     * @param x The x position of the text (longitude)
     * @param y The y position of the text (latitude)
     * @param graphics The java2d graphics object supplied by the
     * plotting/printing medium
     * @throws VisADException
     */
    public void drawString(String text, Font font, Color colour, float x,
                           float y, Graphics2D graphics)
            throws VisADException {
        if (text.length() < 1) {
            return;
        }

        graphics.setColor(colour);

        // Scale the font to match the pixel size of the display
        int        size       = font.getSize();
        Font       scaledFont = font.deriveFont(size * pixelWidth);

        double[][] coords     = new double[3][1];
        coords[0][0] = x;
        coords[1][0] = y;
        coords[2][0] = 0.0f;

        graphics.setFont(scaledFont);

        // Reproject the user coordinates to Normal X, Y coordinates
        CoordinateSystem coordSys    = getCoordinateSystem();
        double[][]       reprojected = coordSys.toReference(coords);

        double[]         normCoords = { reprojected[0][0],
                                        reprojected[1][0] };
        float[]          devCoords   = new float[2];
        viewPort.transform(normCoords, 0, devCoords, 0, 1);

        if ((devCoords[0] > 0) && (devCoords[0] < width)
                && (devCoords[1] > 0) && (devCoords[1] < height)) {
            graphics.drawString(text, devCoords[0], devCoords[1]);
        }

    }

    /**
     * Extract the Geometries from the Visad display and render them to the
     * Graphics2D
     *
     * @param display _more_
     * @param graphics _more_
     */
    private void copyVisadDisplay(DisplayImpl display, Graphics2D graphics) {
        int mode = -1;

        if (display instanceof DisplayImplJ3D) {
            mode = MODE_3D;
        } else if (display instanceof DisplayImplJ2D) {
            mode = MODE_2D;
        }
        DisplayRenderer displayRenderer = null;
        Object          root            = null;
        // If the display is 2D
        if (mode == MODE_2D) {
            // Get the root VisADGroup from the display renderer
            displayRenderer =
                (DisplayRendererJ2D) display.getDisplayRenderer();
            root = (VisADGroup) ((DisplayRendererJ2D) displayRenderer)
                .getRoot();
        } else {
            // Otherwise get the Java3d Group
            displayRenderer =
                (DisplayRendererJ3D) display.getDisplayRenderer();
            root = (Group) ((DisplayRendererJ3D) displayRenderer).getRoot();
        }
        /*
        try {
            displayRenderer.setBoxOn(false);
            displayRenderer.setScaleOn(false);
        } catch (VisADException e) {
            System.err.println("VectorPlotter.generate: " + e);
        } catch (RemoteException e) {
            System.err.println("VectorPlotter.generate: " + e);
        }
        */

        // Recurse the plotDisplay, converting all plotted
        // objects to vectors rendered through a Graphics2D object
        if (mode == MODE_2D) {
            copyGroup((VisADGroup) root, graphics);
        } else if (mode == MODE_3D) {
            copyGroup((Group) root, graphics);
        }
    }

    /**
     * Recursively process each VisAD group in the display, If the group is
     * a geometry array, stop recursing and plot it
     *
     * @param root -
     *                The root VisADGroup from which to recurse
     * @param graphics _more_
     */
    private void copyGroup(VisADGroup root, Graphics2D graphics) {
        // Loop over eah of the VisAD groups children
        for (int i = 0; i < root.numChildren(); i++) {
            // Get the next Child
            VisADSceneGraphObject child = root.getChild(i);

            // If this child is a VisADAppearance
            if (child instanceof VisADAppearance) {
                VisADAppearance appearance = (VisADAppearance) child;
                // Plot it's vertices
                VisADGeometryArray geometry  = appearance.array;
                Color[] colours = getColours(appearance, monochrome);
                float              thickness = appearance.lineWidth / 2.0f;

                plot(geometry, colours, thickness, graphics);
            }

            // If this child is a VisADGroup
            if (child instanceof VisADGroup) {
                // Recurse this group
                copyGroup((VisADGroup) child, graphics);
            }
        }
    }

    /**
     * Recursively process each Java3d group in the display, If the group is
     * a geometry array, stop recursing and plot it
     *
     * @param root -
     *                The root VisADGroup from which to recurse
     * @param graphics _more_
     */
    private void copyGroup(Group root, Graphics2D graphics) {
        int numChildren = 0;
        if (root.getCapability(Group.ALLOW_CHILDREN_READ)) {
            numChildren = root.numChildren();
        }

        // Check to see which children are rendered
        int rendered = -1;
        if (root instanceof Switch) {
            rendered = ((Switch) root).getWhichChild();
        }
        // Loop over each of the VisAD groups children
        for (int i = 0; i < numChildren; i++) {
            Node child = root.getChild(i);

            // Only render this node if it is Switched on
            if ((rendered >= 0) && (rendered != i)) {
                continue;
            }
            if (child instanceof Group) {
                // Todo
                // Check for Switches here to support layers?
                copyGroup((Group) child, graphics);
            } else  if (child instanceof Shape3D) {
                Shape3D shape    = (Shape3D) child;
                int     numGeoms = 0;
                if (shape.getCapability(Shape3D.ALLOW_GEOMETRY_READ)) {
                    numGeoms = shape.numGeometries();
                }

                Appearance appearance = shape.getAppearance();

                Color[]    colours    = getColours(appearance);
                float      thickness  = getLineThickness(appearance);
                int        lineStyle  = getLineStyle(appearance);
                Texture    texture    = appearance.getTexture();
                for (int j = 0; j < numGeoms; j++) {
                    GeometryArray geom = (GeometryArray) shape.getGeometry(j);
                    plot(geom, colours, thickness, texture, lineStyle,
                         graphics);
                }
            } else {
                //                System.err.println ("Unknown scene graph node:" + child.getClass().getName());
            }
        }
    }

    /**
     * _more_
     *
     * @param appearance _more_
     * @param monochrome _more_
     *
     * @return _more_
     */
    private Color[] getColours(VisADAppearance appearance,
                               boolean monochrome) {
        Color[]            colours  = null;
        VisADGeometryArray geometry = appearance.array;

        // If the geometry stores it's colours ...
        if (geometry.colors != null) {
            // Get each individual colour
            int numColors = geometry.colors.length;
            int numCoords = geometry.coordinates.length;
            // Get the ratio of colors to points to distinguish RGB
            // from RGBA. This is a hack until I understand why some
            // LineArrays from a 2D display contain Alpha values
            int cr = 3;
            if (numColors != numCoords) {
                cr = numColors / (numColors - numCoords);
            }
            colours = new Color[numColors / cr];
            for (int j = 0; j < numColors; j += cr) {
                float red   = 0.0f;
                float green = 0.0f;
                float blue  = 0.0f;
                if ( !monochrome) {
                    red   = byteToFloat(geometry.colors[j]);
                    green = byteToFloat(geometry.colors[j + 1]);
                    blue  = byteToFloat(geometry.colors[j + 2]);
                }
                colours[j / cr] = new Color(red, green, blue);
            }
        } else {
            // Otherwise fill the array with the global colour
            float red   = 0.0f;
            float green = 0.0f;
            float blue  = 0.0f;
            if ( !monochrome) {
                red   = appearance.red;
                green = appearance.green;
                blue  = appearance.blue;
            }
            colours    = new Color[1];
            colours[0] = new Color(red, green, blue);
        }

        return colours;
    }

    /**
     * _more_
     *
     * @param appearance _more_
     *
     * @return _more_
     */
    private Color[] getColours(Appearance appearance) {
        Color[]            colours    = null;
        ColoringAttributes colourAttr = appearance.getColoringAttributes();
        int                colourFlag = ColoringAttributes.ALLOW_COLOR_READ;

        if ((colourAttr != null) && (colourAttr.getCapability(colourFlag))) {
            Color3f color3f = new Color3f();
            colourAttr.getColor(color3f);
            colours    = new Color[1];
            colours[0] = color3f.get();
        }

        return colours;
    }

    /**
     * _more_
     *
     * @param appearance _more_
     *
     * @return _more_
     */
    private float getLineThickness(Appearance appearance) {
        float          thickness = 0.0f;
        LineAttributes lineAttr  = appearance.getLineAttributes();
        if (lineAttr.getCapability(LineAttributes.ALLOW_WIDTH_READ)) {
            thickness = lineAttr.getLineWidth();
        }

        return thickness;
    }

    /**
     * _more_
     *
     * @param appearance _more_
     *
     * @return _more_
     */
    private int getLineStyle(Appearance appearance) {
        LineAttributes lineAttr  = appearance.getLineAttributes();
        int            lineStyle = LineAttributes.PATTERN_SOLID;
        if (lineAttr.getCapability(LineAttributes.ALLOW_PATTERN_READ)) {
            lineStyle = lineAttr.getLinePattern();
        }

        return lineStyle;
    }

    /**
     * Convert an unsigned byte into a float
     *
     * @param byteVal -
     *                A number between 0 and 255
     * @returns the number converted to a signed floating point number
     *
     * @return _more_
     */
    private float byteToFloat(byte byteVal) {
        float floatVal = 0.0f;

        if (byteVal >= 0) {
            floatVal = ((float) byteVal) / 256.0f;
        } else {
            floatVal = ((float) (byteVal + 256)) / 256.0f;
        }

        return floatVal;
    }

    /**
     * Convert a geometry array into plottable vectors
     *
     * @param geometryArray -
     *                The definition of the shape of the object to be
     *                plotted
     * @param colours -
     *                A list of the colours of each vertex of the object
     * @param thickness -
     *                The line thickness with which to draw the object
     * @param graphics _more_
     */
    private void plot(VisADGeometryArray geometryArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        // Draw a VisADPointArray
        if (geometryArray instanceof VisADPointArray) {
            VisADPointArray pointArray = (VisADPointArray) geometryArray;
            plot(pointArray, colours, thickness, graphics);
            // Draw a VisADLineStripArray
        } else if (geometryArray instanceof VisADLineStripArray) {
            VisADLineStripArray lineArray =
                (VisADLineStripArray) geometryArray;
            plot(lineArray, colours, thickness, graphics);
            // Draw a VisADLineArray
        } else if (geometryArray instanceof VisADLineArray) {
            VisADLineArray lineArray = (VisADLineArray) geometryArray;
            plot(lineArray, colours, thickness, graphics);
            // Draw a VisADTriangleStripArray
        } else if (geometryArray instanceof VisADTriangleStripArray) {
            VisADTriangleStripArray triangleArray =
                (VisADTriangleStripArray) geometryArray;
            plot(triangleArray, colours, thickness, graphics);
            // Draw a VisADIndexedTriangleStripArray
        } else if (geometryArray instanceof VisADIndexedTriangleStripArray) {
            VisADIndexedTriangleStripArray triangleArray =
                (VisADIndexedTriangleStripArray) geometryArray;
            plot(triangleArray, colours, thickness, graphics);
            // Draw a VisADTriangleArray
        } else if (geometryArray instanceof VisADTriangleArray) {
            VisADTriangleArray triangleArray =
                (VisADTriangleArray) geometryArray;
            plot(triangleArray, colours, thickness, graphics);
        } else {
            // Other geometries go here
        }
    }

    /**
     * _more_
     *
     * @param geometryArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param texture _more_
     * @param lineStyle _more_
     * @param graphics _more_
     */
    private void plot(GeometryArray geometryArray, Color[] colours,
                      float thickness, Texture texture, int lineStyle,
                      Graphics2D graphics) {
        //        System.err.println ("plot:" + geometryArray.getClass().getName());
        if (geometryArray instanceof LineArray) {
            LineArray lineArray = (LineArray) geometryArray;
            plot(lineArray, colours, thickness, lineStyle, graphics);
        } else if (geometryArray instanceof TriangleArray) {
            TriangleArray triangleArray = (TriangleArray) geometryArray;
            plot(triangleArray, colours, thickness, graphics);
        } else if (geometryArray instanceof QuadArray) {
            QuadArray quadArray = (QuadArray) geometryArray;
            if(texture==null) {
                plot(quadArray, colours, thickness, graphics);
            } else {
                //Don't do textures
            }
        } else if (geometryArray instanceof LineStripArray) {
            LineStripArray lineStripArray = (LineStripArray) geometryArray;
            plot(lineStripArray, colours, thickness, lineStyle, graphics);
        } else if (geometryArray instanceof TriangleStripArray) {
            TriangleStripArray triangleArray =
                (TriangleStripArray) geometryArray;
            // Treat geometries with texture separately
            if (texture == null) {
                plot(triangleArray, colours, thickness, graphics);
            }
        } else if (geometryArray instanceof IndexedTriangleStripArray) {
            IndexedTriangleStripArray triangleArray =
                (IndexedTriangleStripArray) geometryArray;
            plot(triangleArray, colours, thickness, graphics);
        } else {
            // Other geometries go here
            //            System.err.println ("Unknown geometry:" + geometryArray.getClass().getName());
        }
    }

    /**
     * Plot a VisADPointArray into postscript format
     *
     * @param pointArray -
     *                The pointArray to be plotted
     * @param colours -
     *                The colour to plot the points
     * @param thickness -
     *                The size to plot the points
     * @param size _more_
     * @param graphics _more_
     */
    private void plot(VisADPointArray pointArray, Color[] colours,
                      float size, Graphics2D graphics) {
        graphics.setColor(colours[0]);
        graphics.setStroke(getStroke(size));
        float[] coordinates = pointArray.coordinates;

        // Loop over each point
        for (int i = 0; i < coordinates.length / 3; i++) {
            float     normalX  = coordinates[i * 3];
            float     normalY  = coordinates[i * 3 + 1];
            float[][] vertices = {
                { normalX, normalY }
            };

            drawShapeReprojected(vertices, colours[0], size, graphics);
        }
    }

    /**
     * Convert a VisADLineStripArray into plottable vectors
     *
     * @param lineArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(VisADLineStripArray lineArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        graphics.setColor(colours[0]);
        graphics.setStroke(getStroke(thickness));
        float[] coordinates = lineArray.coordinates;

        // Get the sizes of all the "chunks"
        int[] vertexCounts = lineArray.stripVertexCounts;

        int   base         = 0;

        // Loop over each chunk
        for (int i = 0; i < vertexCounts.length; i++) {
            int numCoords = vertexCounts[i];

            if (i < colours.length) {
                graphics.setColor(colours[i]);
            }

            GeneralPath path = new GeneralPath();
            // Store the starting position of this chunk
            path.moveTo(coordinates[base], coordinates[base + 1]);

            boolean visible = false;
            float   nxLast  = coordinates[base];
            float   nyLast  = coordinates[base + 1];
            // Loop over all the points in this chunk
            for (int j = 0; j < numCoords; j++) {
                // Get the (normalised) display coordinates
                float nX = coordinates[base + j * 3];
                float nY = coordinates[base + j * 3 + 1];
                // If the line is visible, draw it
                if (visible(nxLast, nyLast, nX, nY, graphics)) {
                    visible = true;
                }
                path.lineTo((float) nX, (float) nY);
                nxLast = nX;
                nyLast = nY;
            }
            if (visible) {
                // Convert display coordinates to device coords
                path.transform(viewPort);
                graphics.draw(path);
            }
            base += 3 * numCoords;

        }
    }

    /**
     * Convert a VisADLineArray into plottable vectors
     *
     * @param lineArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(VisADLineArray lineArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        graphics.setColor(colours[0]);
        graphics.setStroke(getStroke(thickness));
        float[] coordinates = lineArray.coordinates;
        int     numCoords   = lineArray.vertexCount;

        for (int j = 0; j < numCoords; j += 2) {
            if (j < colours.length) {
                graphics.setColor(colours[j]);
            }

            // Get (normalised) display coordinates
            float nX1 = coordinates[j * 3];
            float nY1 = coordinates[j * 3 + 1];
            float nX2 = coordinates[j * 3 + 3];
            float nY2 = coordinates[j * 3 + 4];

            // If the line is visible, draw it
            if (visible(nX1, nY1, nX2, nY2, graphics)) {
                GeneralPath path = new GeneralPath();
                path.moveTo(nX1, nY1);
                path.lineTo(nX2, nY2);

                // Convert them to device coords, and plot
                path.transform(viewPort);
                graphics.draw(path);
            }

        }

    }

    /**
     * _more_
     *
     * @param triangleArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(VisADTriangleStripArray triangleArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        graphics.setColor(colours[0]);
        graphics.setStroke(getStroke(thickness));
        float[] coordinates = triangleArray.coordinates;

        // Get the sizes of all the "chunks"
        int[] vertexCounts = triangleArray.stripVertexCounts;

        int   base         = 0;

        // Loop over each chunk
        for (int i = 0; i < vertexCounts.length; i++) {
            int numCoords = vertexCounts[i];

            // Store the starting position of this chunk
            float       normalLastX = coordinates[base];
            float       normalLastY = coordinates[base + 1];
            GeneralPath path        = new GeneralPath();
            path.moveTo(normalLastX, normalLastY);

            // Loop over all the points in this chunk
            for (int j = 0; j < numCoords; j++) {
                float normalX = coordinates[base + j * 3];
                float normalY = coordinates[base + j * 3 + 1];
                normalLastX = normalX;
                normalLastY = normalY;
                path.lineTo(normalX, normalY);
            }
            path.closePath();
            path.transform(viewPort);
            graphics.fill(path);

            base += 3 * numCoords;

        }

    }

    /**
     * _more_
     *
     * @param triangleArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(VisADTriangleArray triangleArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        float[] colour = new float[4];
        if (colours != null) {
            colour[0] = ((float) colours[0].getRed()) / 255.0f;
            colour[1] = ((float) colours[0].getGreen()) / 255.0f;
            colour[2] = ((float) colours[0].getBlue()) / 255.0f;
            colour[3] = ((float) colours[0].getAlpha()) / 255.0f;
            if ( !useTransparency) {
                colour[3] = 1.0f;
            }
        }

        graphics.setStroke(getStroke(thickness));

        int     vertexCount = triangleArray.vertexCount;
        float[] coordinates = triangleArray.coordinates;

        for (int i = 0; i < 3 * vertexCount; i += 9) {
            // If monochrome
            if (monochrome) {
                // Set everything except white to black
                monochromatise(colour);
            }
            Color color = new Color(colour[0], colour[1], colour[2],
                                    colour[3]);
            graphics.setColor(color);
            float[][] vertices = new float[3][3];
            vertices[0][0] = coordinates[i];
            vertices[1][0] = coordinates[i + 1];
            vertices[0][1] = coordinates[i + 3];
            vertices[1][1] = coordinates[i + 4];
            vertices[0][2] = coordinates[i + 6];
            vertices[1][2] = coordinates[i + 7];
            int clockwise = clockwise(vertices[0], vertices[1]);
            if (clockwise >= 0) {
                vertices[0] = reverseDirection(vertices[0]);
                vertices[1] = reverseDirection(vertices[1]);
            }
            fillShapeReprojected(vertices, color, graphics);
        }

    }

    /**
     * _more_
     *
     * @param triangleArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(VisADIndexedTriangleStripArray triangleArray,
                      Color[] colours, float thickness, Graphics2D graphics) {
        graphics.setColor(colours[0]);
        graphics.setStroke(getStroke(thickness));

        float[] coordinates       = triangleArray.coordinates;

        int[]   indices           = triangleArray.indices;
        int[]   stripVertexCounts = triangleArray.stripVertexCounts;

        int     base              = 0;

        for (int strip = 0; strip < stripVertexCounts.length; strip++) {
            if (strip < colours.length) {
                graphics.setColor(colours[strip]);
            }

            int         count   = stripVertexCounts[strip];
            int         index0  = indices[base];
            int         index1  = indices[base + 1];

            GeneralPath path    = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            boolean     visible = false;
            for (int i = base + 2; i < base + count; i++) {
                int   index2   = indices[i];

                float normalX0 = coordinates[3 * index0];
                float normalY0 = coordinates[3 * index0 + 1];
                float normalX1 = coordinates[3 * index1];
                float normalY1 = coordinates[3 * index1 + 1];
                float normalX2 = coordinates[3 * index2];
                float normalY2 = coordinates[3 * index2 + 1];

                // If any of the triangle is within the
                // area of interest, plot it
                //                              if (visible(xCoords, yCoords, graphics)) {
                path.moveTo(normalX0, normalY0);
                path.lineTo(normalX1, normalY1);
                path.lineTo(normalX2, normalY2);
                visible = true;
                //                              }

                index0  = index1;
                index1  = index2;
            }
            if (visible) {
                path.transform(viewPort);
                path.closePath();
                ////////                                graphics.fill(path);
            }
            base += count;
        }
    }

    /**
     * Convert a LineStripArray into plottable vectors
     *
     * @param lineArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param lineStyle _more_
     * @param graphics _more_
     */
    private void plot(LineStripArray lineArray, Color[] colours,
                      float thickness, int lineStyle, Graphics2D graphics) {
        if (lineStyle == LineAttributes.PATTERN_DASH) {
            // According to the Java3d docs, 8 pixels is the
            // ideal size for the dashes
            float[] dash = { 8.0f * pixelWidth, 8.0f * pixelWidth };
            graphics.setStroke(getStroke(thickness, dash));
        } else {
            graphics.setStroke(getStroke(thickness));
        }

        int     vertexCount = lineArray.getVertexCount();
        float[] coordinates = new float[vertexCount * 3];
        lineArray.getCoordinates(0, coordinates);

        int   numStrips    = lineArray.getNumStrips();
        int[] vertexCounts = new int[numStrips];
        lineArray.getStripVertexCounts(vertexCounts);

        int     base   = 0;

        float[] colour = new float[4];
        // If a colour was supplied, use it
        if (colours != null) {
            colour[0] = ((float) colours[0].getRed()) / 255.0f;
            colour[1] = ((float) colours[0].getGreen()) / 255.0f;
            colour[2] = ((float) colours[0].getBlue()) / 255.0f;
            colour[3] = ((float) colours[0].getAlpha()) / 255.0f;
        } else {
            lineArray.getColor(0, colour);
        }
        // Loop over each chunk
        for (int i = 0; i < vertexCounts.length; i++) {
            int numCoords = vertexCounts[i];

            // Attempt to get the color from the geometry
            if (colours == null) {
                lineArray.getColor(i, colour);
            }
            // If monochrome
            if (monochrome) {
                // Set everything except white to black
                monochromatise(colour);
            }
            if ( !useTransparency) {
                colour[3] = 1.0f;
            }
            Color color = new Color(colour[0], colour[1], colour[2],
                                    colour[3]);

            float[][] vertices = new float[2][numCoords];
            for (int j = 0; j < numCoords; j++) {
                vertices[0][j] = coordinates[base + j * 3];
                vertices[1][j] = coordinates[base + j * 3 + 1];
            }
            drawShapeReprojected(vertices, color, thickness, graphics);
            base += 3 * numCoords;
        }
    }

    /**
     * Convert a LineArray into plottable vectors
     *
     * @param lineArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param lineStyle _more_
     * @param graphics _more_
     */
    private void plot(LineArray lineArray, Color[] colours, float thickness,
                      int lineStyle, Graphics2D graphics) {
        int vertexFormat     = lineArray.getVertexFormat();
        int numColComponents = 3;
        if ((vertexFormat & LineArray.COLOR_4) == LineArray.COLOR_4) {
            numColComponents = 4;
        }
        // If a colour was supplied, use it
        float[] colour = new float[numColComponents];
        if (colours != null) {
            colour[0] = ((float) colours[0].getRed()) / 255.0f;
            colour[1] = ((float) colours[0].getGreen()) / 255.0f;
            colour[2] = ((float) colours[0].getBlue()) / 255.0f;
            if (numColComponents == 4) {
                colour[3] = ((float) colours[0].getAlpha()) / 255.0f;
            }
        } else {
            lineArray.getColor(0, colour);
        }

        // If monochrome
        if (monochrome) {
            // Set everything except white to black
            monochromatise(colour);
        }
        if (lineStyle == LineAttributes.PATTERN_DASH) {
            //                      float[] dash = { 24.0f * pixelWidth, 8.0f * pixelWidth };
            float size  = (float) Math.sqrt(width * width + height * height);
            float scale = size / 3000.0f;
            System.err.println(size + "/" + scale + "Width: " + width + "/"
                               + height);
            float[] dash = { 18.0f * scale, 6.0f * scale };
            graphics.setStroke(getStroke(thickness * .5f, dash));
        } else {
            graphics.setStroke(getStroke(thickness * .5f));
        }

        int     vertexCount = lineArray.getVertexCount();
        float[] coordinates = new float[vertexCount * 3];
        lineArray.getCoordinates(0, coordinates);

        Color lastColor = new Color(colour[0], colour[1], colour[2]);
        linePath = new GeneralPath();
        graphics.setColor(lastColor);
        for (int j = 0; j < vertexCount; j += 2) {
            if (colours == null) {
                lineArray.getColor(j, colour);
            }
            // If monochrome
            if (monochrome) {
                // Set everything except white to black
                monochromatise(colour);
            }

            Color color = null;
            if (numColComponents == 4) {
                color = new Color(colour[0], colour[1], colour[2], colour[3]);
                if ( !useTransparency) {
                    colour[3] = 1.0f;
                }
            } else {
                color = new Color(colour[0], colour[1], colour[2]);
            }
            // Draw lines of one colour in a sigle GeneralPath
            // This makes the resulting image MUCH more slick
            // when plotting many small lines (eg. observations)
            if ( !color.equals(lastColor)) {
                graphics.setColor(lastColor);
                lastColor = color;
                linePath.transform(viewPort);
                graphics.draw(linePath);
                linePath = new GeneralPath();
            }

            // Get the (normalised) display coordinates
            float nX1 = coordinates[j * 3];
            float nY1 = coordinates[j * 3 + 1];
            float nX2 = coordinates[j * 3 + 3];
            float nY2 = coordinates[j * 3 + 4];
            if ( !visible(nX1, nY1, nX2, nY2, graphics)) {
                continue;
            }
            linePath.moveTo(nX1, nY1);
            linePath.lineTo(nX2, nY2);
        }
        // Translate them to device coordinates and plot
        linePath.transform(viewPort);
        graphics.draw(linePath);
    }

    /**
     * _more_
     *
     * @param triangleArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(TriangleStripArray triangleArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        int vertexFormat = triangleArray.getVertexFormat();
        if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2)
                == GeometryArray.TEXTURE_COORDINATE_2) {
            return;
        }
        float[] colour = new float[4];
        if (colours != null) {
            colour[0] = ((float) colours[0].getRed()) / 255.0f;
            colour[1] = ((float) colours[0].getGreen()) / 255.0f;
            colour[2] = ((float) colours[0].getBlue()) / 255.0f;
            colour[3] = ((float) colours[0].getAlpha()) / 255.0f;
        } else {
            triangleArray.getColor(0, colour);
            if (triangleArray.getCapability(GeometryArray.COLOR_3)) {
                colour[3] = 1.0f;
            }
        }

        graphics.setStroke(getStroke(thickness));

        int     vertexCount = triangleArray.getVertexCount();
        float[] coordinates = new float[vertexCount * 3];

        triangleArray.getCoordinates(0, coordinates);
        int cCount = 0;

        // Find out how many strips
        int numStrips = triangleArray.getNumStrips();
        // Get the sizes of each strip
        int[] vertexCounts = new int[numStrips];
        triangleArray.getStripVertexCounts(vertexCounts);

        int base = 0;
        // Loop over each strip
        for (int i = 0; i < numStrips; i++) {
            int numCoords = vertexCounts[i];
            if (colours == null) {
                triangleArray.getColor(cCount++, colour);
            }
            // If monochrome
            if (monochrome) {
                // Set everything except white to black
                monochromatise(colour);
            }
            if ( !useTransparency) {
                colour[3] = 1.0f;
            }
            Color color = new Color(colour[0], colour[1], colour[2],
                                    colour[3]);
            graphics.setColor(color);

            float lastNormX2 = coordinates[base];
            float lastNormY2 = coordinates[base + 1];
            float lastNormX1 = coordinates[base + 3];
            float lastNormY1 = coordinates[base + 3 + 1];
            for (int j = 2; j < numCoords; j++) {

                float     normalX  = coordinates[base + j * 3];
                float     normalY  = coordinates[base + j * 3 + 1];
                float[][] triangle = new float[3][3];
                triangle[0][0] = lastNormX1;
                triangle[1][0] = lastNormY1;
                triangle[0][1] = lastNormX2;
                triangle[1][1] = lastNormY2;
                triangle[0][2] = normalX;
                triangle[1][2] = normalY;
                lastNormX2     = lastNormX1;
                lastNormX1     = normalX;
                lastNormY2     = lastNormY1;
                lastNormY1     = normalY;
                fillShapeReprojected(triangle, color, graphics);
            }
            base += 3 * numCoords;

        }
    }

    /**
     * _more_
     *
     * @param triangleArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(TriangleArray triangleArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        float[] colour = new float[4];
        if (colours != null) {
            colour[0] = ((float) colours[0].getRed()) / 255.0f;
            colour[1] = ((float) colours[0].getGreen()) / 255.0f;
            colour[2] = ((float) colours[0].getBlue()) / 255.0f;
            colour[3] = ((float) colours[0].getAlpha()) / 255.0f;
        }

        graphics.setStroke(getStroke(thickness));

        int     vertexCount = triangleArray.getVertexCount();
        float[] coordinates = new float[vertexCount * 3];

        triangleArray.getCoordinates(0, coordinates);

        for (int i = 0; i < 3 * vertexCount; i += 9) {
            if (colours == null) {
                if (i < vertexCount * 3) {
                    triangleArray.getColor(i / 3, colour);
                }
            }
            // If monochrome
            if (monochrome) {
                // Set everything except white to black
                monochromatise(colour);
            }
            if ( !useTransparency) {
                colour[3] = 1.0f;
            }
            Color color = new Color(colour[0], colour[1], colour[2],
                                    colour[3]);
            graphics.setColor(color);

            // Get the (normalised) display coordinates
            float[][] vertices = new float[2][3];
            vertices[0][0] = coordinates[i];
            vertices[1][0] = coordinates[i + 1];
            vertices[0][1] = coordinates[i + 3];
            vertices[1][1] = coordinates[i + 4];
            vertices[0][2] = coordinates[i + 6];
            vertices[1][2] = coordinates[i + 7];
            // Must be clockwise (batik screws up otherwise)
            int clockwise = clockwise(vertices[0], vertices[1]);
            if (clockwise >= 0) {
                vertices[0] = reverseDirection(vertices[0]);
                vertices[1] = reverseDirection(vertices[1]);
            }
            fillShapeReprojected(vertices, color, graphics);
        }
    }

    /**
     * _more_
     *
     * @param quadArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(QuadArray quadArray, Color[] colours,
                      float thickness, Graphics2D graphics) {
        float[] colour = new float[4];
        if (colours != null) {
            colour[0] = ((float) colours[0].getRed()) / 255.0f;
            colour[1] = ((float) colours[0].getGreen()) / 255.0f;
            colour[2] = ((float) colours[0].getBlue()) / 255.0f;
            colour[3] = ((float) colours[0].getAlpha()) / 255.0f;
        }

        graphics.setStroke(getStroke(thickness));

        int     vertexCount = quadArray.getVertexCount();
        float[] coordinates = new float[vertexCount * 3];

        quadArray.getCoordinates(0, coordinates);

        //        System.err.println("quad:" + vertexCount);
        for (int i = 0; i < 3 * vertexCount; i += 12) {
            if (colours == null) {
                if (i < vertexCount * 4) {
                    quadArray.getColor(i / 4, colour);
                }
            }
            // If monochrome
            if (monochrome) {
                // Set everything except white to black
                monochromatise(colour);
            }
            if ( !useTransparency) {
                colour[3] = 1.0f;
            }
            Color color = new Color(colour[0], colour[1], colour[2],
                                    colour[3]);
            graphics.setColor(color);

            // Get the (normalised) display coordinates
            float[][] vertices = new float[2][4];
            vertices[0][0] = coordinates[i];
            vertices[1][0] = coordinates[i + 1];
            vertices[0][1] = coordinates[i + 3];
            vertices[1][1] = coordinates[i + 4];
            vertices[0][2] = coordinates[i + 6];
            vertices[1][2] = coordinates[i + 7];
            vertices[0][3] = coordinates[i + 9];
            vertices[1][3] = coordinates[i + 10];
            // Must be clockwise (batik screws up otherwise)
            int clockwise = clockwise(vertices[0], vertices[1]);
            if (clockwise >= 0) {
                vertices[0] = reverseDirection(vertices[0]);
                vertices[1] = reverseDirection(vertices[1]);
            }
            fillShapeReprojected(vertices, color, graphics);
        }
    }



    /**
     * _more_
     *
     * @param triangleArray _more_
     * @param colours _more_
     * @param thickness _more_
     * @param graphics _more_
     */
    private void plot(IndexedTriangleStripArray triangleArray,
                      Color[] colours, float thickness,
                      Graphics2D graphics) {}

    /**
     * _more_
     *
     * @param thickness _more_
     *
     * @return _more_
     */
    private BasicStroke getStroke(float thickness) {
        BasicStroke stroke =
            new BasicStroke(thickness * (float) lineThickness,
                            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

        return stroke;
    }

    /**
     * _more_
     *
     * @param thickness _more_
     * @param dash _more_
     *
     * @return _more_
     */
    private BasicStroke getStroke(float thickness, float[] dash) {
        BasicStroke stroke =
            new BasicStroke(thickness * (float) lineThickness,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1.0f, dash, 5.0f);

        return stroke;
    }

    /**
     * Make colours monochromatic by setting everything except white to
     * black
     *
     * @param colour _more_
     */
    private void monochromatise(float[] colour) {
        if ((colour[0] < 0.8f) && (colour[1] < 0.8f) && (colour[2] < 0.8f)) {
            colour[0] = 0.0f;
            colour[1] = 0.0f;
            colour[2] = 0.0f;
        }

        // Remove transparency
        if (colour.length == 4) {
            colour[3] = 1.0f;
        }
    }

    /**
     * _more_
     *
     * @param xCoords _more_
     * @param yCoords _more_
     *
     * @return _more_
     */
    private int clockwise(float[] xCoords, float[] yCoords) {
        int ccw       = 0;
        int numCoords = xCoords.length;
        for (int i = 0; i < numCoords; i++) {
            int j = (i + 1) % numCoords;
            int k = (i + 2) % numCoords;
            float crossProduct = (xCoords[j] - xCoords[i])
                                 * (yCoords[k] - yCoords[j]);
            crossProduct -= (yCoords[j] - yCoords[i])
                            * (xCoords[k] - xCoords[j]);

            if (crossProduct > 0) {
                ccw++;
            } else if (crossProduct < 0) {
                ccw--;
            }
        }

        int clockwise = 0;
        if (ccw > 0) {
            clockwise = -1;
        } else if (ccw < 0) {
            clockwise = 1;
        }

        return clockwise;
    }

    /**
     * Test the vertices to see if they form a clockwise or anticlockwise
     * shape.
     * @param xCoords
     * @param yCoords
     * @return 1 means clockwise, -1 means anticlockwise, 0 means
     *  indeterminate
     */
    private int clockwise(int[] xCoords, int[] yCoords) {
        int ccw       = 0;
        int numCoords = xCoords.length;
        for (int i = 0; i < numCoords; i++) {
            int j = (i + 1) % numCoords;
            int k = (i + 2) % numCoords;
            float crossProduct = (xCoords[j] - xCoords[i])
                                 * (yCoords[k] - yCoords[j]);
            crossProduct -= (yCoords[j] - yCoords[i])
                            * (xCoords[k] - xCoords[j]);

            if (crossProduct > 0) {
                ccw++;
            } else if (crossProduct < 0) {
                ccw--;
            }
        }

        int clockwise = 0;
        if (ccw > 0) {
            clockwise = -1;
        } else if (ccw < 0) {
            clockwise = 1;
        }

        return clockwise;
    }

    /**
     * Reverse the order of the vertices in a triangle from clockwise to
     * anticlockwise and vice versa
     *
     * @param coords
     *                The x (or y) coordinates of the triangle
     *
     * @return _more_
     */
    private int[] reverseDirection(int[] coords) {
        int temp = coords[2];
        coords[2] = coords[1];
        coords[1] = temp;

        return coords;
    }

    /**
     * Reverse the order of the vertices in a polygon from clockwise to
     * anticlockwise and vice versa
     *
     * @param coords
     *                The x (or y) coordinates of the triangle
     *
     * @return _more_
     */
    private float[] reverseDirection(float[] coords) {
        int     numCoords = coords.length;
        float[] temp      = new float[numCoords];
        for (int i = 0; i < numCoords; i++) {
            temp[i] = coords[numCoords - i - 1];
        }

        return temp;
    }

    /**
     * Long winded way of checking if any part of an area is visible in an
     * area bounded by the graphics2d clipping region Significantly
     * increases the plotting time, but significantly decreases the size of
     * the resulting file Probably better to investigate ways of clipping
     * the lines using DelaunayCustom.clip()
     *
     * @param x1 -
     *                The X coordinate of the start of the line
     * @param y1 -
     *                The Y coordinate of the start of the line
     * @param x2 -
     *                The X coordinate of the end of the line
     * @param y2 -
     *                The Y coordinate of the end of the line
     * @returns a boolean flag, true means that the triangle is at least
     *          partially visible
     * @param graphics _more_
     *
     * @return _more_
     */
    private boolean visible(double x1, double y1, double x2, double y2,
                            Graphics2D graphics) {
        double[] coords = { x1, y1, x2, y2 };
        double[] device = new double[4];
        viewPort.transform(coords, 0, device, 0, 2);

        // Find the area occupied by the line
        double xMin = Math.min(device[0], device[2]);
        double xMax = Math.max(device[0], device[2]);
        double yMin = Math.min(device[1], device[3]);
        double yMax = Math.max(device[1], device[3]);

        int    x    = (int) xMin;
        int    y    = (int) yMin;
        // Must add 1 to these, because otherwise a vertical or
        // horizontal line will have 0 area, and be treated as
        // not intersecting, even when it does
        int width  = (int) (xMax - xMin) + 1;
        int height = (int) (yMax - yMin) + 1;

        // If any of this area intersects the clip, it is visible
        boolean visible = graphics.hitClip(x, y, width, height);

        return visible;
    }

    /**
     * _more_
     *
     * @param shape _more_
     *
     * @return _more_
     */
    private boolean visible(Shape shape) {
        boolean visible = shape.intersects(0, 0, width, height);

        return visible;
    }

    /**
     * Long winded way of checking if any part of an area is visible in an
     * area bounded by the graphics2s clipping area Significantly increases
     * the plotting time, but significantly decreases the size of the
     * resulting file Probably better to investigate ways of clipping the
     * lines using DelaunayCustom.clip()
     *
     * @param xCoords -
     *                The X coordinates of the area
     * @param yCoords -
     *                The Y coordinates of the area
     * @returns a boolean flag, true means that the triangle is at least
     *          partially visible
     * @param graphics _more_
     *
     * @return _more_
     */
    private boolean visible(int[] xCoords, int[] yCoords,
                            Graphics2D graphics) {
        int numVertices = xCoords.length;

        int xMin        = Integer.MAX_VALUE;
        int yMin        = Integer.MAX_VALUE;
        int xMax        = Integer.MIN_VALUE;
        int yMax        = Integer.MIN_VALUE;

        for (int i = 0; i < numVertices; i++) {
            float[] display = { xCoords[i], yCoords[i] };
            float[] device  = new float[2];
            // Convert the display coordinates to device coordinates
            viewPort.transform(display, 0, device, 0, 2);

            if (device[0] > xMax) {
                xMax = (int) device[0];
            }
            if (device[0] < xMin) {
                xMin = (int) device[0];
            }
            if (device[1] > yMax) {
                yMax = (int) device[1];
            }
            if (device[1] < yMin) {
                yMin = (int) device[1];
            }
        }

        int x = (int) xMin;
        int y = (int) yMin;
        // Must add 1 to these, because otherwise a vertical or
        // horizontal line will have 0 area, and be treated as
        // not intersecting, even when it does
        int width  = (int) (xMax - xMin) + 1;
        int height = (int) (yMax - yMin) + 1;

        // If any of this area intersects the clip, it is visible
        boolean visible = graphics.hitClip(x, y, width, height);

        return visible;
    }


    /**
     * Perform Sutherland-Hodgman Clipping on an array of vertices
     * Not the most efficient method, but it works
     * @param inVertexArray Array of vertices array[0] - x values,
     * array[1] y values
     * @return Clipped vertices
     */
    private int[][] clip(int[][] inVertexArray) {
        int[][] edge = new int[2][2];
        // Clip Left Edge
        edge[0][0]    = 0;
        edge[1][0]    = height;
        edge[0][1]    = 0;
        edge[1][1]    = 0;
        inVertexArray = SutherlandHodgmanPolygonClip(inVertexArray, edge);
        // Clip Bottom Edge
        edge[0][0]    = 0;
        edge[1][0]    = 0;
        edge[0][1]    = width;
        edge[1][1]    = 0;
        inVertexArray = SutherlandHodgmanPolygonClip(inVertexArray, edge);
        // Clip Right Edge
        edge[0][0]    = width;
        edge[1][0]    = 0;
        edge[0][1]    = width;
        edge[1][1]    = height;
        inVertexArray = SutherlandHodgmanPolygonClip(inVertexArray, edge);
        // Clip Top Edge
        edge[0][0]    = width;
        edge[1][0]    = height;
        edge[0][1]    = width;
        edge[1][1]    = height;
        inVertexArray = SutherlandHodgmanPolygonClip(inVertexArray, edge);

        return inVertexArray;
    }

    /**
     * Perform Sutherland-Hodgman Clipping on an array of vertices
     * Not the most efficient method, but it works
     * @param inVertexArray Array of vertices array[0] - x values,
     * array[1] y values
     *
     * @param path _more_
     * @return Clipped vertices
     */
    private GeneralPath clip(GeneralPath path) {
        float[][] edge = new float[2][2];
        // Clip Left Edge
        edge[0][0] = 0;
        edge[1][0] = (height * 10);
        edge[0][1] = 0;
        edge[1][1] = -(height * 10);
        path       = SutherlandHodgmanPolygonClip(path, edge);
        // Clip Bottom Edge
        edge[0][0] = -(width * 10);
        edge[1][0] = 0;
        edge[0][1] = (width * 10);
        edge[1][1] = 0;
        path       = SutherlandHodgmanPolygonClip(path, edge);
        // Clip Right Edge
        edge[0][0] = (width * 10);
        edge[1][0] = -(height * 10);
        edge[0][1] = (width * 10);
        edge[1][1] = (height * 10);
        path       = SutherlandHodgmanPolygonClip(path, edge);
        // Clip Top Edge
        edge[0][0] = (width * 10);
        edge[1][0] = (height * 10);
        edge[0][1] = -(width * 10);
        edge[1][1] = (height * 10);
        path       = SutherlandHodgmanPolygonClip(path, edge);

        return path;
    }

    /**
     * Perform Sutherland-Hodgman Clipping on an array of vertices
     * Not the most efficient method, but it works
     * @param inVertexArray Array of vertices array[0] - x values,
     * array[1] y values
     *
     * @param path _more_
     * @return Clipped vertices
     */
    private GeneralPath clip2(GeneralPath path) {
        float[][] edge = new float[2][2];
        // Clip Left Edge
        edge[0][0] = 0;
        edge[1][0] = height;
        edge[0][1] = 0;
        edge[1][1] = 0;
        path       = SutherlandHodgmanPolygonClip(path, edge);
        // Clip Bottom Edge
        edge[0][0] = 0;
        edge[1][0] = 0;
        edge[0][1] = width;
        edge[1][1] = 0;
        path       = SutherlandHodgmanPolygonClip(path, edge);
        // Clip Right Edge
        edge[0][0] = width;
        edge[1][0] = 0;
        edge[0][1] = width;
        edge[1][1] = height;
        path       = SutherlandHodgmanPolygonClip(path, edge);
        // Clip Top Edge
        edge[0][0] = width;
        edge[1][0] = height;
        edge[0][1] = 0;
        edge[1][1] = height;
        path       = SutherlandHodgmanPolygonClip(path, edge);

        return path;
    }

    /**
     * Perform Sutherland-Hodgman clipping against a single edge
     * @param inVertexArray Array of vertices
     * @param edge Edge definition - corners must be defined in
     * anticlockwise order
     * @return
     */
    private int[][] SutherlandHodgmanPolygonClip(int[][] inVertexArray,
            int[][] edge) {
        int     numVertices    = inVertexArray[0].length;
        int[][] outVertexArray = null;
        if (numVertices <= 1) {
            outVertexArray = new int[1][0];
            return outVertexArray;
        }
        ArrayList xList = new ArrayList();
        ArrayList yList = new ArrayList();

        int       xs    = inVertexArray[0][inVertexArray[0].length - 1];
        int       ys    = inVertexArray[1][inVertexArray[0].length - 1];
        for (int j = 0; j < inVertexArray[0].length; j++) {
            // Get the next point from the array
            int xp = inVertexArray[0][j];
            int yp = inVertexArray[1][j];
            // If the next point is within the clip
            if (insideEdge(xp, yp, edge)) {
                // If previous point was also within the clip
                if (insideEdge(xs, ys, edge)) {
                    xList.add(new Integer(xp));
                    yList.add(new Integer(yp));
                } else {
                    // Otherwise, intersect at the clip edge
                    int[] inter = intersect(new int[] { xs, ys },
                                            new int[] { xp,
                            yp }, edge);
                    xList.add(new Integer(inter[0]));
                    yList.add(new Integer(inter[1]));
                    xList.add(new Integer(xp));
                    yList.add(new Integer(yp));
                }
            } else {
                // Join the last point to the clip
                if (insideEdge(xs, ys, edge)) {
                    int[] inter = intersect(new int[] { xs, ys },
                                            new int[] { xp,
                            yp }, edge);
                    xList.add(new Integer(inter[0]));
                    yList.add(new Integer(inter[1]));
                }
            }
            xs = xp;
            ys = yp;
        }

        outVertexArray = new int[2][xList.size()];
        for (int i = 0; i < xList.size(); i++) {
            Integer xInt = (Integer) xList.get(i);
            Integer yInt = (Integer) yList.get(i);
            outVertexArray[0][i] = xInt.intValue();
            outVertexArray[1][i] = yInt.intValue();
        }

        return outVertexArray;
    }

    /**
     * Perform Sutherland-Hodgman clipping against a single edge
     * @param inVertexArray Array of vertices
     *
     * @param path _more_
     * @param edge Edge definition - corners must be defined in
     * anticlockwise order
     * @return
     */
    private GeneralPath SutherlandHodgmanPolygonClip(GeneralPath path,
            float[][] edge) {
        ArrayList                  xList    = new ArrayList();
        ArrayList                  yList    = new ArrayList();

        AffineTransform blank = AffineTransform.getScaleInstance(1.0, 1.0);
        java.awt.geom.PathIterator iterator = path.getPathIterator(blank);
        float[]                    coords   = new float[6];
        iterator.currentSegment(coords);
        float xs = coords[0];
        float ys = coords[1];
        iterator.next();
        if (insideEdge(xs, ys, edge)) {
            xList.add(new Float(xs));
            yList.add(new Float(ys));
        }
        while ( !iterator.isDone()) {
            // Get the next point from the array
            iterator.currentSegment(coords);
            float xp = coords[0];
            float yp = coords[1];

            // If the next point is within the clip
            if (insideEdge(xp, yp, edge)) {
                // If previous point was also within the clip
                if (insideEdge(xs, ys, edge)) {
                    xList.add(new Float(xp));
                    yList.add(new Float(yp));
                } else {
                    // Otherwise, intersect at the clip edge
                    float[] inter = intersect(new float[] { xs, ys },
                                        new float[] { xp,
                            yp }, edge);
                    xList.add(new Float(inter[0]));
                    yList.add(new Float(inter[1]));
                    xList.add(new Float(xp));
                    yList.add(new Float(yp));
                }
            } else {
                // Join the last point to the clip
                if (insideEdge(xs, ys, edge)) {
                    float[] inter = intersect(new float[] { xs, ys },
                                        new float[] { xp,
                            yp }, edge);
                    xList.add(new Float(inter[0]));
                    yList.add(new Float(inter[1]));
                }
            }
            xs = xp;
            ys = yp;
            iterator.next();
        }

        GeneralPath clippedPath = new GeneralPath();
        if (xList.size() < 1) {
            return clippedPath;
        }
        Float xInt = (Float) xList.get(0);
        Float yInt = (Float) yList.get(0);
        clippedPath.moveTo(xInt.floatValue(), yInt.floatValue());
        for (int i = 1; i < xList.size(); i++) {
            xInt = (Float) xList.get(i);
            yInt = (Float) yList.get(i);
            clippedPath.lineTo(xInt.floatValue(), yInt.floatValue());
        }

        return clippedPath;
    }

    /**
     * Test if a point is "within" and edge of a rectangle
     * @param x X coordinate of point
     * @param y Y coordinate of point
     * @param edge The edge to compare - must be defined in anticlockwise
     * order
     * @return True if the point is within the rectangle
     */
    private boolean insideEdge(int x, int y, int[][] edge) {
        final int X      = 0;
        final int Y      = 1;
        boolean   inside = false;
        // If bottom edge
        if (edge[X][1] > edge[X][0]) {
            if (y >= edge[Y][0]) {
                inside = true;
            }
        }
        // If Top Edge
        if (edge[X][1] < edge[X][0]) {
            if (y <= edge[Y][0]) {
                inside = true;
            }
        }
        // If Right Edge
        if (edge[Y][1] > edge[Y][0]) {
            if (x <= edge[X][1]) {
                inside = true;
            }
        }
        // If Left Edge
        if (edge[Y][1] < edge[Y][0]) {
            if (x >= edge[X][1]) {
                inside = true;
            }
        }

        return inside;
    }

    /**
     * Test if a point is "within" and edge of a rectangle
     * @param x X coordinate of point
     * @param y Y coordinate of point
     * @param edge The edge to compare - must be defined in anticlockwise
     * order
     * @return True if the point is within the rectangle
     */
    private boolean insideEdge(float x, float y, float[][] edge) {
        final int X      = 0;
        final int Y      = 1;
        boolean   inside = false;
        // If bottom edge
        if (edge[X][1] > edge[X][0]) {
            if (y >= edge[Y][0]) {
                inside = true;
            }
        }
        // If Top Edge
        if (edge[X][1] < edge[X][0]) {
            if (y <= edge[Y][0]) {
                inside = true;
            }
        }
        // If Right Edge
        if (edge[Y][1] > edge[Y][0]) {
            if (x <= edge[X][1]) {
                inside = true;
            }
        }
        // If Left Edge
        if (edge[Y][1] < edge[Y][0]) {
            if (x >= edge[X][1]) {
                inside = true;
            }
        }

        return inside;
    }

    /**
     * Intersect a line between two vertices which crosses the edge of the
     * clipping region
     * @param first First point - int[0] is x value, int[1] is y value
     * @param second  Second point - int[0] is x value, int[1] is y value
     * @param edge The edge definition - Must be defined in anticlockwise
     *  order
     * @return
     */
    private int[] intersect(int[] first, int[] second, int[][] edge) {
        final int X         = 0;
        final int Y         = 1;

        int[]     intersect = new int[2];

        // If the edge is horizontal
        if (edge[Y][0] == edge[Y][1]) {
            intersect[Y] = edge[Y][0];
            intersect[X] = first[X]
                           + (edge[Y][0] - first[Y]) * (second[X] - first[X])
                             / (second[Y] - first[Y]);
        } else {
            intersect[X] = edge[X][0];
            intersect[Y] = first[Y]
                           + (edge[X][0] - first[X]) * (second[Y] - first[Y])
                             / (second[X] - first[X]);
        }

        return intersect;
    }

    /**
     * Intersect a line between two vertices which crosses the edge of the
     * clipping region
     * @param first First point - int[0] is x value, int[1] is y value
     * @param second  Second point - int[0] is x value, int[1] is y value
     * @param edge The edge definition - Must be defined in anticlockwise
     *  order
     * @return
     */
    private float[] intersect(float[] first, float[] second, float[][] edge) {
        final int X         = 0;
        final int Y         = 1;

        float[]   intersect = new float[2];

        // If the edge is horizontal
        if (edge[Y][0] == edge[Y][1]) {
            intersect[Y] = edge[Y][0];
            intersect[X] = first[X]
                           + (edge[Y][0] - first[Y]) * (second[X] - first[X])
                             / (second[Y] - first[Y]);
        } else {
            intersect[X] = edge[X][0];
            intersect[Y] = first[Y]
                           + (edge[X][0] - first[X]) * (second[Y] - first[Y])
                             / (second[X] - first[X]);
        }

        return intersect;
    }

    /**
     * Class ChartException _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.2 $
     */
    public class ChartException extends Exception {

        /**
         * _more_
         *
         * @param reason _more_
         */
        public ChartException(String reason) {
            super(reason);
        }
    }

    /*
     * Provide a set of simple hatching patterns
     */

    /**
     * Class Hatching _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.2 $
     */
    public class Hatching {

        /** _more_          */
        public static final int NUM_PATTERNS = 6;

        /** _more_          */
        public static final int DIAGONAL1 = 0;

        /** _more_          */
        public static final int DIAGONAL2 = 1;

        /** _more_          */
        public static final int DIAGONAL_BOTH = 2;

        /** _more_          */
        public static final int HORIZONTAL = 3;

        /** _more_          */
        public static final int VERTICAL = 4;

        /** _more_          */
        public static final int SQUARE = 5;

        /** _more_          */
        private int width = 300;

        /** _more_          */
        private int height = 300;

        /**
         * _more_
         *
         * @param pattern _more_
         *
         * @return _more_
         */
        public BufferedImage getPattern(int pattern) {
            // Create a 1 bit (monochrome) image
            BufferedImage fillTexture = new BufferedImage(width, height,
                                            BufferedImage.TYPE_INT_RGB);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    setPoint(i, j, Color.WHITE, fillTexture);
                }
            }

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (isSet(pattern, i, j)) {
                        setPoint(i, j, Color.BLACK, fillTexture);
                    }
                }
            }

            return fillTexture;
        }

        /**
         * Test whether an x,y point for a given pattern is filled
         *
         *
         * @param pattern _more_
         * @param x
         *                The x location of the point
         * @param y
         *                The y location of the point
         * @param The
         *                pattern
         *
         * @return _more_
         */
        private boolean isSet(int pattern, int x, int y) {
            boolean isSet  = false;
            int     repeat = 3;

            if (pattern == DIAGONAL1) {
                int xx = width / repeat;
                int yy = height / repeat;
                if ((x % xx) == (y % yy)) {
                    isSet = true;
                }
            } else if (pattern == DIAGONAL2) {
                int xx = width / repeat;
                int yy = height / repeat;
                if ((x % xx) == (yy - (y % yy))) {
                    isSet = true;
                }
            } else if (pattern == DIAGONAL_BOTH) {
                int xx = width / repeat;
                int yy = height / repeat;
                if (((x % xx) == (y % yy)) || ((x % xx) == (yy - (y % yy)))) {
                    isSet = true;
                }
            } else if (pattern == HORIZONTAL) {
                int yy = height / repeat;
                if ((y % yy) == 0) {
                    isSet = true;
                }
            } else if (pattern == VERTICAL) {
                int xx = width / repeat;
                if ((x % xx) == 0) {
                    isSet = true;
                }
            } else if (pattern == SQUARE) {
                int xx = width / repeat;
                int yy = height / repeat;
                if (((x % xx) == 0) || ((y % yy) == 0)) {
                    isSet = true;
                }
            }

            return isSet;
        }

        /**
         * Set a point in a buffered image. Actually, set a 3x3 square,
         * it looks better.
         *
         * @param x
         *                The x location of the centre of the square
         * @param y
         *                The y location of the centre of the square
         * @color color The colour of the square
         * @image The buffered image to be plotted
         * @param color _more_
         * @param image _more_
         */
        private void setPoint(int x, int y, Color color,
                              BufferedImage image) {
            int rgb = color.getRGB();
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if ((i >= 0) && (i < width) && (j >= 0) && (j < height)) {
                        image.setRGB(i, j, rgb);
                    }
                }
            }
        }
    }
}

