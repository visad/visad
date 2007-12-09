package edu.wisc.ssec.mcidas;


//  THIS DOESN'T WORK YET!

public class ABISnav extends AREAnav {
  int itype, lpsi2;
  double h,re,a,rp,pi,cdr,crd,deltax,deltay,rflon;
  int[] ioff = new int[3];
  double sublon;

  /**
   * Set up for the real math work.  Must pass in the int array
   * of the ABIS nav 'codicil'.
   *
   * @param iparms the nav block from the image file
   * @throws IllegalArgumentException
   *           if the nav block is not a ABIS type.
   */
  public ABISnav (int[] iparms) 
      throws IllegalArgumentException
  {
    this(1, iparms);
  }

  /**
   * Set up for the real math work.  Must pass in the int array
   * of the ABIS nav 'codicil'.
   * @deprecated  Since ifunc must be 1, replaced with #ABISnav(int[] iparms).
   *              If ifunc != 1, ifunc is set to 1.
   *
   * @param  ifunc   the function to do (always 1 for now)
   * @param  iparms   the nav block from the image file
   * @throws IllegalArgumentException
   *           if the nav block is not a ABIS type.
   */
  public ABISnav (int ifunc, int[] iparms) 
      throws IllegalArgumentException
  {
    // Only type 1 supported
    if (ifunc != 1) ifunc = 1;

    // This is not used.  Left over from nvxgvar.dlm code for Cartesian
    // transformations
    if (ifunc != 1) {
      if (iparms[0] == LL ) itype = 1;
      if (iparms[0] == XY ) itype = 2;
      return ;
    }

    if (iparms[0] != ABIS ) 
        throw new IllegalArgumentException("Invalid navigation type" + 
                                            iparms[0]);
    if (ifunc  == 1) {
         for (int i = 0; i < 3; i++) {
             ioff[i] = iparms[3+i];
         }
         h=42164-6378.155;
         re=6378.155;
         a=1./297.;
         rp=re/(1.+a);
         pi=3.141592653;
         cdr=pi/180.;
         crd=180./pi;
         lpsi2=1;
         deltax=17.76/5535.;
         deltay=17.76/5535.;
         rflon=0.0;
         sublon=McIDASUtil.mcPackedIntegerToDouble(iparms[6]);
    }
  }

  /** converts from satellite coordinates to latitude/longitude
   *
   * @param  linele[][]  array of line/element pairs.  Where 
   *                     linele[indexLine][] is a 'line' and 
   *                     linele[indexEle][] is an element. These are in 
   *                     'file' coordinates (not "image" coordinates.)
   *
   * @return latlon[][]  array of lat/lon pairs. Output array is 
   *                     latlon[indexLat][] of latitudes and 
   *                     latlon[indexLon][] of longitudes.
   *
   */
  public double[][] toLatLon(double[][] linele) { 

    int number = linele[0].length;
    double[][] latlon = new double[2][number];
    //  transform line/pixel to geographic coordinates:
    double imglinele[][] = areaCoordToImageCoord(linele); 
    double xlin,xele,xele2,xlin2,x,y,xr,yr,rs,tanx,tany,val1,val2,yk;
    double vmu,cosrf,sinrf,xt,yt,zt,teta,xfi,xla,ylat,ylon;

    for (int point=0; point<number; point++) {

      //FUNCTION NVXSAE(XLIN,XELE,XDUM,XFI,XLA,Z)
      xlin = imglinele[indexLine][point];
      xele = imglinele[indexEle][point];
      xele2=xele/4.;
      xlin2=xlin/4.;
      x=(5535./2.)-xele2;
      y=ioff[2]-(xlin2+ioff[1]-ioff[0]);
      xr=x;
      yr=y;
      x=xr*lpsi2*deltax*cdr;
      y=yr*lpsi2*deltay*cdr;
      rs=re+h;
      tanx=Math.tan(x);
      tany=Math.tan(y);
      val1=1.+tanx*tanx;
      val2=1.+(tany*tany)*((1.+a)*(1.+a));
      yk=rs/re;
      if ((val1*val2) > ((yk*yk)/(yk*yk-1))) {
         latlon[indexLat][point] = Double.NaN;
         latlon[indexLon][point] = Double.NaN;
         continue;
      }
      vmu=(rs-(re*(Math.sqrt((yk*yk)-(yk*yk-1)*val1*val2))))/(val1*val2);
      cosrf=Math.cos(rflon*cdr);
      sinrf=Math.sin(rflon*cdr);
      xt=(rs*cosrf)+(vmu*(tanx*sinrf-cosrf));
      yt=(rs*sinrf)-(vmu*(tanx*cosrf+sinrf));
      zt=vmu*tany/Math.cos(x);
      teta=Math.asin(zt/rp);
      xfi=(Math.atan(((Math.tan(teta))*re)/rp))*crd;
      xla=-Math.atan(yt/xt)*crd;
//
//-- CHANGE LONGITUDE FOR CORRECT SUBPOINT
//
      xla=xla+sublon;
      //if(itype == 1) {
         ylat=xfi;
         ylon=xla;
         //call nllxyz(ylat,ylon,xfi,xla,z)
      //}
      latlon[indexLat][point] = ylat;
      latlon[indexLon][point] = -ylon;
   }
   return latlon;
 }


  /**
   * toLinEle converts lat/long to satellite line/element
   *
   * @param  latlon[][]  array of lat/long pairs. Where latlon[indexLat][]
   *                     are latitudes and latlon[indexLon][] are longitudes.
   *
   * @return linele[][]  array of line/element pairs.  Where
   *                     linele[indexLine][] is a line and linele[indexEle][] 
   *                     is an element.  These are in 'file' coordinates
   *                     (not "image" coordinates);
   */
  public double[][] toLinEle(double[][] latlon) {

    int number = latlon[0].length;
    double[][] linele = new double[2][number];
    //  transform line/pixel to geographic coordinates:
    double x1,y1,xfi,xla,rom,y,r1,r2,rs,reph,rpph,coslo,sinlo,teta,xt,yt;
    double zt,px,py,xr,yr;

    for (int point=0; point<number; point++) {
      //FUNCTION NVXEAS(VFI,VLA,Z,YR,XR,XDUM)
      x1=latlon[indexLat][point];
      y1=-latlon[indexLon][point];
      /*
      if(itype == 1) {
         x=vfi;
         y=vla;
         //call nxyzll(x,y,z,x1,y1)
         y1=-y1;
      }
      */
//
//-- CORRECT FOR SUBLON
//
      y1=y1+sublon;
      xfi=x1*cdr;
      xla=y1*cdr;
      rom=(re*rp)/Math.sqrt(rp*rp*Math.cos(xfi)*Math.cos(xfi)+re*re*Math.sin(xfi)*Math.sin(xfi));
      y=Math.sqrt(h*h+rom*rom-2*h*rom*Math.cos(xfi)*Math.cos(xla));
      r1=y*y+rom*rom;
      r2=h*h;
      if(r1 > r2) {
        linele[indexLine][point] = Double.NaN;
        linele[indexEle][point] = Double.NaN;
        continue;
      }
      rs=re+h;
      reph=re;
      rpph=rp;
      coslo=Math.cos(rflon*cdr);
      sinlo=Math.sin(rflon*cdr);
      teta=Math.atan((rpph/reph)*Math.tan(xfi));
      xt=reph*Math.cos(teta)*Math.cos(xla);
      yt=reph*Math.cos(teta)*Math.sin(xla);
      zt=rpph*Math.sin(teta);
      px=Math.atan((coslo*(yt-rs*sinlo)-(xt-rs*coslo)*sinlo)/
                (sinlo*(yt-rs*sinlo)+(xt-rs*coslo)*coslo));
      py=Math.atan(zt*((Math.tan(px)*sinlo-coslo)/(xt-rs*coslo))*Math.cos(px));
      px=px*crd;
      py=py*crd;
      xr=px/(deltax*lpsi2);
      yr=py/(deltay*lpsi2);
      xr=(5535/2.)-xr;
      yr=yr+ioff[2]+ioff[1]-ioff[0];
      xr=xr*4;
      yr=22141-yr*4;
      linele[indexLine][point] = yr;
      linele[indexEle][point] = xr;
   }
   return imageCoordToAreaCoord(linele, linele);
 }
}
