//
// RADRnav.java
//

/*
This code was modified from the original Fortran code on the
McIDAS system.  The code in this file is Copyright(C) 1999 by Don
Murray.  It is designed to be used with the VisAD system for 
interactive analysis and visualization of numerical data.  
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package edu.wisc.ssec.mcidas;

/**
 * Navigation class for Radar (RADR) type nav. This code was modified
 * from the original FORTRAN code (nvxradr.dlm) on the McIDAS system. It
 * only supports latitude/longitude to line/element transformations (LL) 
 * and vice/versa. Transform to 'XYZ' not implemented.
 * @see <A HREF="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
 *      McIDAS Programmer's Manual</A>
 *
 * @author  Don Murray
 */
public final class RADRnav extends AREAnav 
{

    private boolean isEastPositive = true;

    final double EARTH_RADIUS=6371.23; // earth equatorial radius (km)
    final int MISS = McIDASUtil.MCMISSING;

    int itype;
    double xrow;
    double xcol;
    double xlat;
    double xlon;
    double xrot;
    double xblat;
    double xblon;

    /**
     * Set up for the real math work.  Must pass in the int array
     * of the RADR nav 'codicil'.
     *
     * @param iparms  the nav block from the image file
     * @throws IllegalArgumentException
     *           if the nav block is not a RADR type.
     */
    public RADRnav (int[] iparms) 
        throws IllegalArgumentException
    {

/* No longer needed.  Kept for consistency with nvxradr.dlm
        if (ifunc != 1) 
        {
            if (iparms[0] == XY ) itype = 1;
            if (iparms[0] == LL ) itype = 2;
            return;
        }
*/

        if (iparms[0] != RADR ) 
            throw new IllegalArgumentException("Invalid navigation type" + 
                                                iparms[0]);
        itype = 2;
        xrow = iparms[1];
        xcol = iparms[2];
        xlat = McIDASUtil.integerLatLonToDouble(iparms[3]);
        xlon = McIDASUtil.integerLatLonToDouble(iparms[4]);
        double xspace = iparms[5]/1000.;
        double yspace = xspace;
        if (iparms[7] != 0 && iparms[7] != MISS)
            yspace = iparms[7]/1000.;
        xrot = -DEGREES_TO_RADIANS*iparms[6]/1000.;
        xblat = EARTH_RADIUS*DEGREES_TO_RADIANS/xspace;
        xblon = EARTH_RADIUS*DEGREES_TO_RADIANS/yspace;
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
        double xlin;
        double xele;
        double xdis;
        double xangl;
        double xange;
        double ylat;
        double ylon;

        int number = linele[0].length;
        double[][] latlon = new double[2][number];

        // Convert array to Image coordinates for computations
        double[][] imglinele = areaCoordToImageCoord(linele);

        for (int point=0; point < number; point++) 
        {
           xldif = xrow - imglinele[indexLine][point];
           xedif = xcol - imglinele[indexEle][point];
           xdis = Math.sqrt(xldif*xldif + xedif*xedif);
           if (xdis > 0.001)
           {
               xangl = Math.atan2(xldif, xedif) - 90.*DEGREES_TO_RADIANS;
               xange = Math.atan2(xldif, xedif) + 90.*DEGREES_TO_RADIANS;
               xldif = xdis*Math.cos(xrot+xangl);
               xedif = xdis*Math.sin(xrot+xange);
            }
            ylat = xlat + xldif/xblat;
            ylon = xlon + xedif/xblon/Math.cos(ylat* DEGREES_TO_RADIANS);

            // transform from McIDAS coordinates
            if (isEastPositive) ylon = -ylon;
            
            latlon[indexLat][point] = ylat;
            latlon[indexLon][point] = ylon;

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
        double zlat;
        double zlon;
        double xrlon;
        double xrlat;
        double xldif;
        double xedif;
        double xdis;
        double xangl;
        double xange;

        int number = latlon[0].length;
        double[][] linele = new double[2][number];

        for (int point=0; point < number; point++) 
        {

            zlat = latlon[indexLat][point];

            // transform to McIDAS (west positive longitude) coordinates
            zlon = isEastPositive 
                     ?  -latlon[indexLon][point]
                     : latlon[indexLon][point];
            xrlon = zlon - xlon;
            xrlat = zlat - xlat;
            xldif = xblat*xrlat;
            xedif = xrlon*xblon*Math.cos(zlat*DEGREES_TO_RADIANS);
            xdis = Math.sqrt(xldif*xldif + xedif*xedif);
            if (xdis > .001) 
            {
                xangl = Math.atan2(xldif, xedif)-90*DEGREES_TO_RADIANS;
                xange = Math.atan2(xldif, xedif)+90*DEGREES_TO_RADIANS;
                xldif = xdis*Math.cos(-xrot+xangl);
                xedif = xdis*Math.sin(-xrot+xange);
            }
            linele[indexLine][point] = xrow - xldif;
            linele[indexEle][point] = xcol - xedif;

        } // end point loop

        // Return in 'File' coordinates
        return imageCoordToAreaCoord(linele);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RADRnav)) return false;
        RADRnav that = (RADRnav) o;
        return (super.equals(o) &&
               that.xlat == xlat &&
               that.xlon == xlon &&
               that.xrow == xrow &&
               that.xcol == xcol);
    }
}
