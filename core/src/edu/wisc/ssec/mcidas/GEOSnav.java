//
// GEOSnav.java
//

/*
This source file is part of the edu.wisc.ssec.mcidas package and is
Copyright (C) 1998 - 2023 by Tom Whittaker, Tommy Jasmin, Tom Rink,
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

/*
This code translated to Java from the original McIDAS module: nvxgeos.dlm.
The following notice is included in that code:

C****************************************************************
C AUTHOR      : Oscar Alonso Lasheras
C COPYRIGHT GMV S.A. 2006
C PROPERTY OF GMV S.A.; ALL RIGHTS RESERVED
C
C PROJECT     : S3GICast
C FILE NAME   : nvxgeos.dlm
C LANGUAGE    : FORTRAN-77
C TYPE        : Funcion auxliar para creacion de navegacion GEOS
C DESCRIPTION : Navega los productos MPEF y OSIS, ademas de los productos
C               de los servidores FSDX y SAFN-HDF5 en McIDAS
C
*/

public class GEOSnav extends AREAnav {

  private static final long serialVersionUID = 1L;
  final int loff, coff, lfac, cfac, plon, bres;
  final int pixelOffsetFlag;
  final double radpol = 6356.5838;
  final double radeq = 6378.1690;
  final double X42 = 42164.0;

  private boolean isEastPositive = true;

  public GEOSnav(int[] iparms) throws IllegalArgumentException {

    if (iparms[0] != GEOS) 
      throw new IllegalArgumentException ("Invald navigation type " + iparms[0]);

    loff = iparms[1];
    coff = iparms[2];
    lfac = iparms[3];
    cfac = iparms[4];
    plon = iparms[5];
    //System.err.println("iparms.length: " + iparms.length);
    if ( iparms.length > 6 && iparms[6] > 0 ) {
    	bres = iparms[6];
    	//System.err.println("Using bres from iparms: " + bres);
    } else {
    	bres = 1;
    	//System.err.println("Using default bres of 1. iparms contained: " + iparms[6]);
    }
    
    // TJJ Dec 2017 - 1/2 pixel adjustment for AHI
    if ((iparms.length > 7) && (iparms[7] > 0)) {
        pixelOffsetFlag = iparms[7];
    } else {
        pixelOffsetFlag = 0;
    }
    
  }


  /**
  * @param latlon lat and lon of points (N and E are positive)
  */

  public double[][] toLinEle(double[][] latlon) {
    double xlat, xlon, xlin, xele, rlin, rele;
    double c_lat, cosc_lat, rn, r1, r2, r3, rl;
    double x,y;
    double lat,lon,splon;
    double ad2, bd, cd, delta2, halfsom, r_eq2, r_pol2;

    int number = latlon[0].length;
    double[][] linele = new double[2][number];

    for (int point=0; point < number; point++) {
      xlat = latlon[indexLat][point];
      xlon = latlon[indexLon][point];
      if (!isEastPositive) xlon = -xlon;


      // --- Coordinates are computed accroding EUMETSAT's LRIT/HRIT Global Spec
      // --- Doc No: CGMS 03

      // --- Coordinates are converted to Radians
      lat   = xlat*Math.PI/180.;
      lon   = xlon*Math.PI/180.0;
      splon = plon/10. * Math.PI/180.0;

      // --- Intermediate data
      c_lat=Math.atan(0.993243*Math.tan(lat));
      cosc_lat=Math.cos(c_lat);
      r_pol2= radpol * radpol;
      r_eq2 = radeq * radeq;
      rl=radpol/(Math.sqrt(1-((r_eq2-r_pol2)/r_eq2)*cosc_lat*cosc_lat));
      r1=X42-rl*cosc_lat*Math.cos(lon-splon);
      r2=-rl*cosc_lat*Math.sin(lon-splon);
      r3=rl*Math.sin(c_lat);
      rn=Math.sqrt(r1*r1+r2*r2+r3*r3);

      // --- Compute variables useful to check if pixel is visible
      ad2 = r1*r1 + r2*r2 + r3*r3*r_eq2 / r_pol2;
      bd = X42*r1;
      cd = X42*X42 - r_eq2;
      delta2 = bd*bd-ad2*cd;
      halfsom = bd*rn/ad2;

      if ((delta2 >= 0.) && (rn <= halfsom)) {
      // ------- Intermediate coordinates
        x = Math.atan(-r2/r1);
        y = Math.asin(-r3/rn);
        x = x * 180./Math.PI;
        y = y * 180./Math.PI;

        xele = coff/10. + x / Math.pow(2,16) * cfac/10.;
        xlin = loff/10. + y / Math.pow(2,16) * lfac/10.;
        
        // TJJ Dec 2017 - now checking pixel offset flag for AHI adjustment
        if (pixelOffsetFlag == 1) {
            rlin = (xlin * bres) - ((bres - 1) / 2.0);
            rele = (xele * bres) - ((bres - 1) / 2.0);            
        } else {
            rlin = (xlin * bres) - (bres - 1);
            rele = (xele * bres) - (bres - 1);
        }
        
      } else {

        rlin=Double.NaN;
        rele=Double.NaN;
      }

      linele[indexLine][point] = rlin;
      linele[indexEle][point] = rele;
    }

    return imageCoordToAreaCoord(linele, linele);

  }

  public double[][] toLatLon(double[][] linele) {


    double xlat, xlon, xlin, xele, rlin, rele;
    double x,y;
    double s1, s2, s3, sxy, sn, sd, sdd;
    double aux, aux2;
    double cosx, cosy, sinx, siny;



    // --- Coordinates are computed accroding EUMETSAT's LRIT/HRIT Global Spec
    // --- Doc No: CGMS 03

    int number = linele[0].length;
    double[][] latlon = new double[2][number];
    double[][] imglinele = areaCoordToImageCoord(linele);

    for (int point=0; point < number; point++ ) {

      rlin = imglinele[indexLine][point];
      rele = imglinele[indexEle][point];
      
      // use bres to adjust the coordinates
      // TJJ Dec 2017 - now checking pixel offset flag for AHI adjustment
      if (pixelOffsetFlag == 1) {
          xlin = (rlin + ((bres - 1) / 2.0)) / bres;
          xele = (rele + ((bres - 1) / 2.0)) / bres;
      } else {
          xlin = (rlin + (bres - 1)) / bres;
          xele = (rele + (bres - 1)) / bres;
      }

      // --- Intermediate coordinates
      x = (xele - coff/10.) * Math.pow(2,16) / (cfac/10.);
      y = (xlin - loff/10.) * Math.pow(2,16) / (lfac/10.);
      x = x * Math.PI/180.;
      y = y * Math.PI/180.;

      //c --- Intermediate data
      cosx=Math.cos(x);
      cosy=Math.cos(y);
      sinx=Math.sin(x);
      siny=Math.sin(y);

      aux=X42*cosx*cosy;
      aux2=cosy*cosy+1.006803*siny*siny;
      sdd=aux*aux-aux2*1737121856.0;
      if (sdd < 0.0) {
        xlat=Double.NaN;
        xlon=Double.NaN;
      } else {
 
        sd=Math.sqrt(sdd);
        sn=(aux-sd)/aux2;
        s1=X42 - sn*cosx*cosy;
        s2=sn*sinx*cosy;
        s3= -sn*siny;
        sxy=Math.sqrt(s1*s1+s2*s2);
 
        // --- Computation
        xlon = Math.atan(s2/s1);
        xlon = xlon * 180./Math.PI + plon/10.;
        xlat = Math.atan(1.006803*s3/sxy)* 180./Math.PI;
 
        // --- Longitudes in [-180,180]
        if(xlon >  180.0) xlon = xlon - 360.;
        if(xlon < -180.0) xlon = xlon + 360.;
      }

      if (!isEastPositive) xlon = -xlon;

      latlon[indexLat][point] = xlat;
      latlon[indexLon][point] = xlon;

    }

    return latlon;
  }
 
}
