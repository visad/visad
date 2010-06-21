

c=====================================================================================
      subroutine Aitoffproj(intk,intj,center,npts,gall,galb,iprox,iproy)
c=====================================================================================
      dimension gall(*),galb(*)
      dimension iprox(*),iproy(*)

      do i=1,npts
      call lbpix(gall(i),galb(i),center,intk,intj,iprox(i),iproy(i))
      end do
      return
      end
c=====================================================================================






c=====================================================================================
      subroutine skysamp(NSKYMAX,nskypt,res,gall,galb)
c=====================================================================================
      dimension gall(*),galb(*)


      parameter (PI=3.141519)

      iskypt=1
      gb=-90+res
      gl=0

 100  if (gl.lt.360) then
         gall(iskypt)=gl
         galb(iskypt)=gb

         iskypt=iskypt+1
         gl=gl+res/cos(gb*PI/180)
         go to 100
      end if
      if (gb.lt.90) then
         gl=0
         gb=gb+res
         go to 100
      end if

      nskypt=iskypt
      if (nskypt.gt.NSKYMAX) then
         print *, 'Must increase NSKYMAX'
         stop
      end if
      return
      end
c=====================================================================================



c=====================================================================================
      subroutine Aitoffgrid(intk,intj,center,ilonx,ilony,ilatx,ilaty)
c=====================================================================================

      parameter(NLON=13,NLAT=37)
      parameter(NLOGRID=13,NLAGRID=5)
      dimension ipixx(NLON,NLAT),ipixy(NLON,NLAT)
      dimension ilonx(NLOGRID,NLAT),ilony(NLOGRID,NLAT)
      dimension ilatx(NLAGRID,NLON),ilaty(NLAGRID,NLON)

      dlon=360/(NLON-1)
      dlat=180/(NLAT-1)

      edge=(center+180)
      if (edge.ge.360) edge=edge-360
      eps=0.1

      il=1
      glon=edge-eps
      do ib=1,NLAT
           glat=(ib-1)*dlat-90
           call lbpix(glon,glat,center,intk,intj,
     >                ipixx(il,ib),ipixy(il,ib))
      end do

      do il=2,NLON-1
         glon=edge-(il-1)*dlon
         if (glon.le.0) glon=glon+360.
         do ib=1,NLAT
           glat=(ib-1)*dlat-90
           call lbpix(glon,glat,center,intk,intj,
     >                ipixx(il,ib),ipixy(il,ib))
         end do
      end do

      il=NLON
      glon=edge+eps
      do ib=1,NLAT
         glat=(ib-1)*dlat-90
         call lbpix(glon,glat,center,intk,intj,
     >                ipixx(il,ib),ipixy(il,ib))
      end do

      do il=1,NLON
         do ib=1,NLAT
            ilonx(il,ib)=ipixx(il,ib)
            ilony(il,ib)=ipixy(il,ib)
         end do
      end do

      do ib=7,31,6
        do il=1,NLON
           ind=(ib-1)/6
           ilatx(ind,il)=ipixx(il,ib)
           ilaty(ind,il)=ipixy(il,ib)
        end do
      end do



      return
      end
c=====================================================================================

cRAB I've removed all but the relevant programs since several of the others didn't
c    compile properly
c=====================================================================================
C This file contains eight utility programs designed for use with the Wisconsin
C all-sky soft X-ray survey maps.  They are: PIXLB, a subroutine that returns
C galactic coordinates of any pixel in an Aitoff-projection map; LBPIX, a
C subroutine that returns the pixel number for an Aitoff projection of the given
C galactic coordinate; XY, a subroutine used by LBPIX that calculates the X and Y
C coordinates of the given galactic coordinates for an Aitoff projection;
C XYPOLE, a subroutine that calculates the X and Y coordinates of the given
C galactic coordinates for an equal-area polar projection; WRMAP, the subroutine
C that wrote the export tape map files; RDMAP, a subroutine to read the export
C tape map files  add a map to the map currently residing in memory; and
C MAPPRT, a subroutine to print a map in our standard format; and REMOVE,
C a subroutine that zeroes map pixels suspected of contamination in the
C B and C band maps (REMOVE only works for the 0-centered Aitoff maps).
C These programs are written in SPERRY-UNIVAC ASCII FORTRAN.  Subroutines
C WRMAP  and RDMAP, in particular, contain code that is probably specific
C to SPERRY-UNIVAC and may be specific to MACC.
C
C
C  SUBROUTINE: LBPIX
C  AUTHOR:  DAVE BURROWS
C  DATE:  OCT 16, 1981
C
C  DESCRIPTION:  THIS SUBROUTINE RETURNS THE PIXEL COORDINATES
C      OF THE AITOFF MAP PIXEL WHICH CONTAINS THE GIVEN GALACTIC POSITION.
C
C  USAGE:  CALL LBPIX(GLONG,GLAT,CENTER,INTK,INTJ,IX,IY)
C      GLONG = GALACTIC LONGITUDE IN DEGREES (REAL INPUT VARIABLE)
C      GLAT = GALACTIC LATITUDE IN DEGREES (REAL INPUT VARIABLE)
C      CENTER = CENTER LONGITUDE IN DEGREES (REAL INPUT VARIABLE)
C      INTK = CONTRACTION FACTOR OF MAP IN HORIZONTAL DIRECTION FROM 181 PIXELS
C              (INTEGER INPUT VARIABLE)
C      INTJ = CONTRACTION FACTOR OF MAP IN VERTICAL DIRECTION FROM 91 PIXELS
C              (INTEGER INPUT VARIABLE)
C      IX,IY = OUTPUT VARIABLES: COORDINATES OF PIXEL WHICH CONTAINS
C              THE SPECIFIED POINT (INTEGERS).
C
C  COMMON BLOCKS:
C      NONE
C
C  SUBROUTINES CALLED BY LBPIX:
C      XY
C
       SUBROUTINE LBPIX(GLONG,GLAT,CENTER,INTK,INTJ,IX,IY)
       REAL L,B
cRAB 7-30-98
       ifac=10
       L = AMOD((720.+GLONG-CENTER),360.)
       B = GLAT
       CALL XY(B,L,X,Y)
       X = X/5.
       Y = Y/5.
cRAB 7-30-98
       IX = ifac*((180*X + 181)/(2*INTK) + 1.0)
       IY = ifac*((180*Y + 91)/(2*INTJ) + 1.0)
       RETURN
       END
C
C  SUBROUTINE: XY
C  AUTHOR: RICK BORKEN
C  DATE: DEC. 1975
C  MODIFIED: 6/2/82 BY DNB TO CORRECTLY HANDLE ALL LONGITUDE VALUES
C      BETWEEN -3600 DEGREES AND +3600 DEGREES.
C
C  DESCRIPTION:
C      XY CALCULATES THE X,Y POSITION OF A POINT ON AN AITOFF
C      PROJECTION, GIVEN THE LATITUDE AND LONGITUDE.
C      THE PROJECTION IS 5.0 UNITS HIGH AND 10.0 UNITS WIDE,
C      CENTERED AT 0.,0.
C
C  USAGE:  CALL XY(GLAT,GLONG,X,Y)
C      GLAT = REAL INPUT VARIABLE: LATITUDE OF POINT IN DEGREES
C              (-90. < GLAT < 90.)
C      GLONG = REAL INPUT VARIABLE: LONGITUDE OF POINT IN DEGREES
C              (-3600. < GLONG < 3600.)
C      X,Y = REAL OUTPUT VARIABLES: LOCATION OF POINT ON PROJECTION
C             IN IMAGE UNITS (  -5. < X < 5.    -2.5 < Y < 2.5  ).
C
C  COMMON BLOCKS:
C      NONE
C
C  SUBROUTINES CALLED BY XY:
C      NONE
C
      SUBROUTINE XY(A,B,X,Y)
      DATA RADIAN/57.29578/
      DATA TWORAD/114.59156/
      DATA PI/3.14159/
      DATA PIO2/1.570796/
      ALPHA=(-A+90.0)/RADIAN
      GLONG=AMOD(B+3600.,360.)
      IF(GLONG.GT.180.)GLONG=GLONG-360.
      BETA=GLONG/TWORAD
      CBETA=COS(BETA)
      SBETA=SIN(BETA)
      CALPHA=COS(ALPHA)
      SALPHA=SIN(ALPHA)
      SC=-CALPHA
      SS=SALPHA*SBETA
      CAP=SALPHA*CBETA
      IF(CAP.GT.1.0) CAP=1.0
      SAP=SQRT(1.0-CAP**2)
      IF(SAP.LT.0.001) GO TO 1
      CBP=SC/SAP
      SBP=SS/SAP
      R=SQRT(1.0-CAP)
      X=R*SBP
      Y=-(R*CBP)/2.0
      X=X*5.
      Y=Y*5.
      X=-1.*X
      RETURN
    1 X=0.0
      Y=0.0
      RETURN
      END

C  SUBROUTINE: PIXLB
C  MODIFIED:  6/10/82 BY D. PFRANG, ADDING RADIUS CHECK TO
C      CHANGE ALTERNATE RETURN.  FOR PIXELS WHOSE CENTERS ARE
C      OUTSIDE THE AITOFF MAP LIMITS, PIXLB WILL GO AHEAD AND
C      CALCULATE L AND B COORDINATES BUT WILL EXECUTE THE
C      ALTERNATE RETURN.  THIS IS SO PIXELS WHOSE CENTERS FALL
C      OUTSIDE THE MAP, BUT WHICH MAY HAVE A CORNER OR EDGE
C      INSIDE THE MAP WILL HAVE L AND B COORDINATES CALCULATED
C      FOR THEM ANYWAY.  IN PRACTICE, USE OF THESE COORDINATES
C      SHOULD NOT PRESENT A PROBLEM AS THEY ARE USUALLY FAIRLY
C      CLOSE (TYPICALLY WITHIN 1.5 DEGREES) TO THE CORRECT VALUES.
C
C  DESCRIPTION:
C  COMPUTES LII,BII  FOR PIXEL (IX,IY) OF A ZERO OFFSET
C   AITOFF ARRAY WITH ANY LONGITUDE AT THE CENTER.
C   IT ASSUMES THAT THE INPUT MAP IS CONTRACTED BY A FACTOR
C   OF INTK IN THE X-DIMENSION AND INTJ IN THE Y-DIMENSION
C   FROM A 181X91 ELEMENT AITOFF MAP.  THIS FEATURE IS USED
C   WHEN MAPS ARE BINNED INTO LARGER PIXEL SIZES FOR
C   IMPROVED STATISTICS.
C
C  USAGE: CALL PIXLB(IERR,IX,IY,CENTER,INTK,INTJ,L,B)
C      IX,IY = COORDINATES OF MAP PIXEL ON AITOFF PROJECTION (INTEGERS).
C              0 < IX < 182, 0 < IY < 92, BOTTOM LEFT CORNER OF AITOFF
C              PROJECTION IS PIXEL (1,1), TOP RIGHT CORNER OF AITOFF
C              PROJECTION IS PIXEL (181,91). (INPUT VARIABLES.)
C      CENTER = CENTER LONGITUDE OF PROJECTION (REAL INPUT VARIABLE.)
C      INTK = FACTOR BY WHICH THE INPUT MAP IS CONTRACTED FROM A 181X91
C              AITOFF IN THE X DIRECTION (INTEGER INPUT VARIABLE).
C      INTJ = FACTOR BY WHICH THE INPUT MAP IS CONTRACTED FROM A 181X91
C              AITOFF IN THE Y DIRECTION (INTEGER INPUT VARIABLE).
C      L,B = OUTPUT VARIABLES: REAL LONGITUDE AND LATITUDE OF PIXEL IX,IY
c              (in degrees).
C      IERR = FLAG FOR ALTERNATE ERROR RETURN
C              (USUALLY INDICATES A POINT OUTSIDE THE BOUNDARIES OF THE AITOFF)
C              NOTE: SEE DOCUMENTATION ABOVE REGARDING MODIFICATION.
C
C  COMMON BLOCKS:
C      NONE
C
C  SUBROUTINES CALLED BY PIXLB:
C      NONE
C
C
      SUBROUTINE PIXLB(I,J,CENTER,INTK,INTJ,L,B,IERR)
       REAL L,LL
       DATA RAD/57.2957795/
       IERR=0
       X=(INTK*(2*I-1)-181)/180.
       Y=(INTJ*(2*J-1)-91)/180.
       RADIUS = SQRT(X**2 + 4*(Y**2))         !DP
       SINALP=SQRT((4*Y**2.-1.)**2.+(X*Y*2.)**2.)
C      IF(ABS(SINALP) .GT. 1.00) return 1
       IF(ABS(SINALP) .GT. 1.00) GO TO 17       !DP
       ALPHA= ASIN(SINALP)*RAD
       B=90.-ALPHA
        IF(Y.LT.0.) B=-B
        IF(SINALP.LE.1.E-4) GO TO 11
       COSBET=(1.-X**2.-4.*Y**2.)/SINALP
C      IF(ABS(COSBET) .GT. 1.00) return 1
       IF(ABS(COSBET) .GT. 1.00) GO TO 18       !DP
        BETA=ACOS(COSBET)*RAD
       LL=2.*BETA
       L=LL
        IF(X.GT.0.) L=360.-LL
       GO TO 15
   11  L=180.
   15  L = AMOD(L+CENTER,360.)
C
       IF (RADIUS .LE. 1.00) RETURN             !DP
C
       IERR=1
       return                                  !DP
C
C
17     CONTINUE                                 !DP
       L = 0.0
       B = 0.0
       IERR=1
       return
18     CONTINUE                                 !DP
       L = 0.0
       B = 0.0
       IERR=1
       return
       END





