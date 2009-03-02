//
// RECTnav.java
//

/*
This source file is part of the edu.wisc.ssec.mcidas package and is
Copyright (C) 1998 - 2009 by Tom Whittaker, Tommy Jasmin, Tom Rink,
Don Murray, James Kelly, Bill Hibbard, Dave Glowacki, Curtis Rueden
and others.
 
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

package edu.wisc.ssec.mcidas;

/**
 * Navigation class for Radar (RECT) type nav. This code was modified
 * from the original FORTRAN code (nvxrect.dlm) on the McIDAS system. It
 * only supports latitude/longitude to line/element transformations (LL) 
 * and vice/versa. Transform to 'XYZ' not implemented.
 * @see <A HREF="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
 *      McIDAS Programmer's Manual</A>
 *
 * @author  Don Murray
 */
public final class RECTnav extends AREAnav 
{

    private boolean isEastPositive = true;

    int itype;
    int iwest;
    double xrow;
    double xcol;
    double zslat;
    double zslon;
    double zdlat;
    double zdlon;
    double xlin;
    double xele;
    double xldif;
    double xedif;
    double xlat;
    double xlon;

    /**
     * Set up for the real math work.  Must pass in the int array
     * of the RECT nav 'codicil'.
     *
     * @param iparms  the nav block from the image file
     * @throws IllegalArgumentException
     *           if the nav block is not a RECT type.
     */
    public RECTnav (int[] iparms) 
        throws IllegalArgumentException
    {

/* No longer needed.  Kept for consistency with nvxrect.dlm
        if (ifunc != 1) 
        {
            if (iparms[0] == XY ) itype = 1;
            if (iparms[0] == LL ) itype = 2;
            return;
        }
*/

        if (iparms[0] != RECT ) 
            throw new IllegalArgumentException("Invalid navigation type" + 
                                                iparms[0]);
        itype = 2;

        xrow = iparms[1];

        int ipowlat = iparms[11];
        if (ipowlat == 0) ipowlat = 4;
        zslat = iparms[2]/Math.pow(10.,ipowlat);

        xcol = iparms[3];

        int ipowlon = iparms[12];
        if (ipowlon == 0) ipowlon = 4;
        zslon = iparms[4]/Math.pow(10.,ipowlon);

        int ipowdlin = iparms[13];
        if (ipowdlin == 0) ipowdlin = 4;
        zdlat = iparms[5]/Math.pow(10.,ipowdlin);

        int ipowdele = iparms[14];
        if (ipowdele == 0) ipowdele = 4;
        zdlon = iparms[6]/Math.pow(10.,ipowdele);

        int ipowrad = iparms[15];
        if (ipowrad == 0) ipowrad = 3;
        double drad = iparms[7]/Math.pow(10.,ipowrad);

        int ipowecc = iparms[16];
        if (ipowecc == 0) ipowecc = 6;
        double decc = iparms[8]/Math.pow(10.,ipowecc);

        iwest = (iparms[10] >= 0) ? 1 : -1;
        if (iwest == 1) isEastPositive = false;

        xlin = 1;
        xele = 1;
        xldif = xrow - xlin;
        xedif = iwest*(xcol - xele);
        xlon = zslon + xedif*zdlon;
        xlat = zslat + xldif*zdlat;
        zslat = xlat;
        zslon = xlon;
        xrow = 1;
        xcol = 1;


    }

    /** converts from satellite coordinates to latitude/longitude
     *
     * @param  linele[][]  array of line/element pairs.  Where 
     *                     linele[indexLine][] is a 'line' and 
     *                     linele[indexEle][] is an element. These are in 
     *                     'file' coordinates (not "image" coordinates.)
     *
     * @return latlon[][]  array of lat/long pairs. Output array is 
     *                     latlon[indexLat][] of latitudes and 
     *                     latlon[indexLon][] of longitudes.
     *
     */
    public double[][] toLatLon(double[][] linele) 
    {

        double xldif;
        double xedif;
        double xlon;
        double xlat;

        int number = linele[0].length;
        double[][] latlon = new double[2][number];

        // Convert array to Image coordinates for computations
        double[][] imglinele = areaCoordToImageCoord(linele);

        for (int point=0; point < number; point++) 
        {
            xldif = xrow - imglinele[indexLine][point];
            xedif = iwest * (xcol - imglinele[indexEle][point]);
            xlon = zslon + xedif*zdlon;
            xlat = zslat + xldif*zdlat;
            if  (xlat > 90. || xlat < -90.)
            {
                xlat = Double.NaN;
            }
            if (xlon < -180.)
            {
                xlon = xlon + 360.;
                if (xlon < -180.) xlon = Double.NaN;
            }
            if (xlon > 180. && xlon != Double.NaN)
            {
                xlon = xlon - 360.;
                if (xlon > 180.) xlon = Double.NaN;
            }
            if (xlat == Double.NaN || xlon == Double.NaN)
            {
                latlon[indexLat][point] = Double.NaN;
                latlon[indexLon][point] = Double.NaN;
            }
            else
            {
                latlon[indexLat][point] = xlat;
                latlon[indexLon][point] = (iwest == 1) ? -xlon  : xlon;
            }

        } // end point for loop

        return latlon;

    }

    /**
     * toLinEle converts lat/long to satellite line/element
     *
     * @param  latlon[][] array of lat/long pairs. Where latlon[indexLat][]
     *                    are latitudes and latlon[indexLon][] are longitudes.
     *
     * @return linele[][] array of line/element pairs.  Where
     *                    linele[indexLine][] is a line and linele[indexEle][]
     *                    is an element.  These are in 'file' coordinates
     *                    (not "image" coordinates);
     */
    public double[][] toLinEle(double[][] latlon) 
    {
        double xlon;
        double xlat;

        int number = latlon[0].length;
        double[][] linele = new double[2][number];

        for (int point=0; point < number; point++) 
        {

            xlat = latlon[indexLat][point];

            // transform to McIDAS (west positive longitude) coordinates
            xlon = (iwest == 1) 
                   ? -latlon[indexLon][point]
                   : latlon[indexLon][point];
            if (iwest == -1 && xlon < zslon) xlon = xlon +360.;
            linele[indexLine][point] = xrow - (xlat - zslat)/zdlat;
            linele[indexEle][point]  = xcol - (xlon - zslon)/(zdlon*iwest);

        } // end point loop

        // Return in 'File' coordinates
        return imageCoordToAreaCoord(linele, linele);
    }
}
