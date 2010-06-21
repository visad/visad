
C WLH - to do list
C
C
C fixed Sun dot
C
C 3-D widget driving a galactic location
C plot density (or any grid state variable) versus
C distance and versus velocity (two 2-D plots)
C
C subroutine ??(x, y, z, dist[], quant_dist[], speed[], quant_speed[])
C
C to distance from Sun and along ray
C
C sky map algorithm
C
C more steering controls
C
C grid coordinate units
C
C pick up NRMAX etc from a Fortrab function
C
C

C WLH
      SUBROUTINE GETCON(NS)
      DIMENSION NS(4)
      include "dimen.h"
      NS(1) = NxpxMAX
      NS(2) = NypxMAX
      NS(3) = NRMAX
      NS(4) = NZMAX
      RETURN
      END

C WLH
C      SUBROUTINE ISMGSC(NS, dHxyz, dHsky, gL, gB)
      SUBROUTINE ISMGSC(PARAMS, ISIZES, dGRID, dIMAGE, dLON, dLAT)
      DIMENSION PARAMS(13)
      DIMENSION ISIZES(5)
C dimension dHxyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)
C parameter (NXP=NRMAX-1,NYP=NXP,NZP=NZMAX-1)
C parameter (NRMAX=46)
C parameter (NZMAX=21)
C
C dimension dHsky(NxpxMAX,NypxMAX)
C parameter(NxpxMAX=364,NypxMAX=182)
C

c NEED 2) TO EXAMINE ROTATION CURVES AS A FUNCTION OF HALO PARAMETERS
c      3) TO PUT IN OTHER SOURCES OF WEIGHT/PRESSURE
c      4) SQUARE WITH BOULARES AND COX
c      5) DO L.O.S. CALCULATIONS
c==========================================================================
c     Bob Benjamin's ISM Galactic Structure Code
c     --------------------------------------------
c     Calculates various physical parameters as a function of
c     galactic coordinates

c     This program represents a compilation of various global models
c     of the galaxy (abundance gradients, velocity fields, mass
c     distributions, etc for the Milky Way Galaxy)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"


c     ----------------------------
c     Positional coordinates
c     for cylindrically symmetric
c     quantities
c     ----------------------------
      dimension zcyl(NZMAX),rcyl(NRMAX)

c     ---------------------------------
c     Scalar field on axisymmetric grid
c     ---------------------------------
      dimension prz(NRMAX,NZMAX)
      dimension dHrz(NRMAX,NZMAX)

c     -------------------------
c     Subcomponents of density
c     -------------------------
      dimension dHrzp(NRMAX,NZMAX,NPHASE)
      dimension dtotrzp(NRMAX,NZMAX,NPHASE)
      dimension ffrzp(NRMAX,NZMAX,NPHASE)
      dimension sigrzp(NRMAX,NZMAX,NPHASE)
      dimension Trzp(NRMAX,NZMAX,NPHASE)

c     -------------------------
c     Subcomponents of pressure
c     -------------------------
      dimension przp(NRMAX,NZMAX,NFORCE)

c     -------------------------
c     gravitation field
c     -------------------------
      dimension gzrz(NRMAX,NZMAX)
      dimension gRrz(NRMAX,NZMAX)
      dimension phirz(NRMAX,NZMAX)

      dimension radrz(NRMAX,NZMAX)
      dimension geffrz(NRMAX,NZMAX)

      dimension vrrz(NRMAX,NZMAX)
      dimension wtrz(NRMAX,NZMAX)

      dimension pwtrz(NRMAX,NZMAX)
      dimension pdef(NRMAX,NZMAX)

      dimension Bavgrz(NRMAX,NZMAX)

      dimension outrz(NRMAX,NZMAX)
      dimension vrprz(NRMAX,NZMAX)
      dimension vtrmrz(NRMAX,NZMAX)
C      dimension rhogrz(NRMAX,NZMAX)
      dimension rhoglid(NRMAX)



c     -----------------------------
c     Array of abundances
c     -----------------------------
      dimension abrzZ(NRMAX,NZMAX,NEL)

c     -----------------------
c     Cartesian coordinates
c     (Rcyl(1) *must* be zero)
c     ----------------------
      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)
      dimension dHxyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)
      dimension dGRID((2*NXP+1)*(2*NYP+1)*(2*NZP+1))
      dimension xyzmask(-NXP:NXP,-NYP:NYP,-NZP:NZP)

      dimension imask(NMSK),pmask(NMSK)

c      dimension dumrz(NRMAX,NZMAX)
c      dimension dumxyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)

c     ----------------------------
c     Parameters for Aitoff grid
c     (must match values in Aitofgrid
c     ----------------------------
      parameter(NLON=13,NLAT=37)
      parameter(NLOGRID=13,NLAGRID=5)

C WLH 30 degree spaced lat & lon lines
      dimension ilonx(NLOGRID,NLAT),ilony(NLOGRID,NLAT)
      dimension ilatx(NLAGRID,NLON),ilaty(NLAGRID,NLON)
c     ----------------------
c     All -sky maps
c     ----------------------
C WLH gL = longitude, gB = latitude
      dimension gL(NxpxMAX,NypxMAX),gB(NxpxMAX,NypxMAX)
      dimension dHsky(NxpxMAX,NypxMAX)
      dimension dIMAGE(NxpxMAX*NypxMAX)
      dimension dLON(NxpxMAX*NypxMAX),dLAT(NxpxMAX*NypxMAX)
C was
C      dimension gL(NgLMAX),gB(NgBMAX)
C      dimension dHsky(NgLMAX,NgBMAX)

c     ----------------------
c     Input and output files
c     ----------------------
      character*40 abfile
      character*17 slicefile
      character*15 blockfile
      character*40 switchfile,parfile,descrfile
      character*2 ccase
      character*1 cslice
      character*7 dirname
c--------------------------------------------------------------------------


C
C      print *, 'What is the case number?'
C      read *, icase
      icase = 1
      if ((icase.lt.0).or.(icase.gt.99)) then
         print *, 'Out of range!!'
         stop
      end if

      izero=0
      if (icase.lt.10) then
         write(ccase(1:1),990) izero
         write(ccase(2:2),990) icase
      else
         write(ccase(1:2),991) icase
      end if
 990  format(i1)
 991  format(i2)

      dirname='Case'//ccase//'/'

      switchfile='switch.inp'
C WLH
C      parfile=dirname//'switch.out'
      parfile='switch.out'

c     -------------------------------
c     Set up file with info about the
c     run... close file at end of
c     program...
c     -------------------------------
C WLH
C      descrfile=dirname//'descrip.out'
      descrfile='descrip.out'
      ndes=12
      open(unit=ndes,file=descrfile,status='unknown')

c     -----------------------------
c     Set up choices for functional
c     forms to use (passed in common)
c     -----------------------------
      call switch_init(PARAMS,ndes,switchfile,parfile,descrfile,abfile)

c     -----------------------
c     Set up geometrical grid
c     in R/Z for galaxy
c     -----------------------
      call rzgrid_init(ndes,nr,nz,Rcyl,Zcyl)

c     --------------------------
c     Set up abundance gradients
c     --------------------------
      call abcalc(nr,nz,Rcyl,Zcyl,abrzZ,abfile)

c     ------------------------
c     Set up gas densities on
c     grid using...
c     ------------------------
      call dHcalc(nr,nz,Rcyl,Zcyl,dHrz,dHrzp)
      call dtotcalc(nr,nz,dHrzp,dtotrzp)
      call ffcalc(nr,nz,Rcyl,Zcyl,dtotrzp,ffrzp)

c     Note: velocity dispersion and temperature
c           are set in parameters for pressure
      call vdispcalc(nr,nz,Rcyl,Zcyl,sigrzp)
      call Tcalc(nr,nz,Rcyl,Zcyl,Trzp)
c     -----------------------------
c     Calculate pressure components
c     on grid
c     -----------------------------
      call pcalc(nr,nz,Rcyl,Zcyl,dHrzp,dtotrzp,Trzp,
     >           sigrzp,przp,prz)


c     ---------------------------
c     Convert magnetic pressure to
c     field strengths using assumption
c     set given in routine
c     ------------------------------

      iBgeom=1
      call Bavgcalc(iBgeom,nr,nz,Rcyl,Zcyl,przp,Bavgrz)

c     ------------------------
c     Calculate gravity
c     ------------------------


      call gcalc(nr,nz,Rcyl,Zcyl,gRrz,gzrz,phirz)

      do ir=1,nr
         do iz=1,nz
            if (gRrz(ir,iz).lt.0) then
               print *, 'gR less than 0:', Rcyl(ir),Zcyl(iz)
               stop
            else if (gzrz(ir,iz).lt.0) then
               print *, 'gz less than 0:', Rcyl(ir),Zcyl(iz)
               stop
            end if
          end do
      end do


c     ----------------------------
c     Calculate radiation pressure
c     (vertical component only)
c     ----------------------------

      call rpcalc(nr,nz,Rcyl,Zcyl,abrzZ,radrz)

c     ----------------------------
c     Calculate "effective gravity"
c     (gravity - radiation pressure)
c     ----------------------------

      call grzsum(nr,nz,gzrz,radrz,geffrz)

c     ------------------------
c     Calculate column or rho*g
c     above the top limit of grid
c     ------------------------
      do i=1,NRMAX
         rhoglid(i)=0
      end do

c     ilid=1 is for column density, ilid=2 for int_rho*g_eff
      ilid=2
c      call grzlid(ilid,abfile,Zcyl(nz),ztop,nr,Rcyl,rhoglid)
c     ------------------------
c     Calculate rho*g for grid
c     ------------------------
c      call grzmult(nr,nz,dHrz,geffrz,rhogrz)

c     ----------------------
c     integrate array rhogrz
c     ----------------------

c      call zinteg(nr,nz,Rcyl,Zcyl,rhoglid,rhogrz,wtrz)


      do j=1,NZMAX
         do i=1,NRMAX
            wtrz(i,j)=wtrz(i,j)*RHO0
         end do
      end do

c     -----------------------------
c     Percent diff between pressure
c     and weight
c     -----------------------------
c      call pwtcalc(nr,nz,prz,wtrz,pwtrz)

c     ---------------------------
c     Calculate terminal velocity
c     ---------------------------
      call vtrmcalc(nr,nz,gzrz,dHrz,vtrmrz)

c     ------------------------
c     Pressure deficit/excess
c     in (p/k)
c     ------------------------
      call pdefex(nr,nz,prz,wtrz,pdef)

c     -------------------------
c     Calculate d(wt)/dR
c     -------------------------
c      call ddR(nr,nz,Rcyl,Zcyl,dHrz,dwtdr)

c     -------------------------
c     Calculate d(wt)/dz
c     -------------------------
c      call ddz(nr,nz,Rcyl,Zcyl,dHrz,dwtdz)

c     ------------------------
c     Calculate rotation speed
c     ------------------------
      call vrotcalc(nr,nz,Rcyl,Zcyl,gRrz,vrrz)


c     ---------------
c     Cartesian stuff
c     ---------------
      call xyzgrid_init(nr,nz,Rcyl,Zcyl,
     >                  nxx,nyy,nzz,Xx,Yy,Zz)



      imask(1)=1
      pmask(1)=Rcyl(nr)
      call xyzmkmask(imask,pmask,nxx,nyy,nzz,
     >               Xx,Yy,Zz,xyzmask)

c     -----------------------
c     Normally would convert
c     cylindrical coord to
c     cartesian. right now
c     skip to new density fcn
c     -----------------------
c      do iz=1,nz
c      do ir=1,nr
c         dumrz(ir,iz)=Rcyl(ir)
c      end do
c      end do
c      call cyl2xyz(nr,nz,Rcyl,Zcyl,dumrz,
c     >             nxx,nyy,nzz,Xx,Yy,Zz,dumxyz)


      call tcden(nxx,nyy,nzz,Xx,Yy,Zz,dHxyz,xyzmask)

c     ----------------------------
c     Call all-sky survey
c     ----------------------------
C WLH could be a slider
C but res=1.25 is very slow
      res=2.5
      intk=1
      intj=1
      center=0

      Nxpx=int(1820/10/RES)
      Nypx=int(910/10/RES)
      dxpx=10*RES
      dypx=10*RES

      do ix=1,NxpxMAX
         do iy=1,NypxMAX
            gL(ix,iy)=-500
            gB(ix,iy)=-500
c RAB 1Oct98 12:30pm
            dHsky(ix,iy)=-1000
         end do
      end do

      do ix=1,Nxpx
         do iy=1,Nypx
            i=int(((ix-1)*dxpx)/10)
            j=int(((iy-1)*dypx)/10)

            call PIXLB(I,J,CENTER,INTK,INTJ,glon,glat,IERR)
            if (ierr.eq.0) then
              gL(ix,iy)=glon
              gB(ix,iy)=glat
            else
              gL(ix,iy)=-500.
              gB(ix,iy)=-500.
            end if
         end do
      end do


c     produce longitude (ilonx,ilony) and latitude (ilatx,ilaty)
c     gridlines
      call Aitoffgrid(intk,intj,center,ilonx,ilony,ilatx,ilaty)


c     choose positions to get projected quantities
c      call skysamp(NSKYMAX,nskypt,res,gL,gB)

c     convert l and b to pixel number in map projection
c      call Aitoffproj(intk,intj,center,nskypt,gL,gB,iprox,iproy)

c     calculate
      call tcsky(nxx,nyy,nzz,Xx,Yy,Zz,dHxyz,xyzmask,
     >                 Nxpx,Nypx,gL,gB,dHsky)


c     ------------
c     Output loop
c     ------------
c     Loop through possible things to print out...
      do i=1,NNOUT
         if (iout(i).ne.0) then
C WLH
C           blockfile=dirname//outlab(i)//'.blk'
           blockfile=outlab(i)//'.blk'
           call outstuff(i,ipout(i),nr,nz,outrz,
     >                   dHrz,     prz  ,wtrz,
     >                   gzrz, grRz,phirz,vrrz,
     >                   vrprz,pwtrz,abrzZ,dHrzp,
     >                   ffrzp,sigrzp,Trzp,przp,
     >                   radrz,Bavgrz)
           call prtblk(nr,nz,Rcyl,Zcyl,outrz,blockfile)

           do j=1,NSLICE
             if (islice(j).ne.0) then
               write(cslice(1:1),990) j
               if (islice(j).eq.1) then
C WLH
C                slicefile=dirname//outlab(i)//'R'//cslice//'.slc'
                slicefile=outlab(i)//'R'//cslice//'.slc'
               else if (islice(j).eq.2) then
C WLH
C                slicefile=dirname//outlab(i)//'Z'//cslice//'.slc'
                slicefile=outlab(i)//'Z'//cslice//'.slc'
               end if
               xslice(j)=xslice(j)+0.0001
               call prtslice(islice(j),xslice(j),nr,nz,
     >                          Rcyl,Zcyl,outrz,slicefile)
             end if
           end do
         end if
      end do

c     -------------------------
c     close description file...
c     -------------------------
      close(unit=ndes)

      ISIZES(1) = Nxpx
      ISIZES(2) = Nypx
      ISIZES(3) = 1+2*nxx
      ISIZES(4) = 1+2*nyy
      ISIZES(5) = 1+2*nzz

      i=1
      do iz=-nzz,nzz
        do iy=-nyy,nyy
          do ix=-nxx,nxx
            dGRID(i) = dHxyz(ix,iy,iz)
            i=i+1
          end do
        end do
      end do

      do iy=1,Nypx
        do ix=1,Nxpx
          if (gL(ix,iy) .gt. 180.0) then
            gL(ix,iy) = gL(ix,iy) - 360.0
          endif
        end do
        ifirst = 0
        ilast = 0
        do ix=1,Nxpx
          if (gL(ix,iy) .gt. -400) then
            if (ifirst .eq. 0) ifirst = ix
            ilast = ix
          endif
        end do
        if (ifirst .gt. 1 .and. ifirst .ne. ilast) then
          diffL = gL(ifirst,iy) - gL(ifirst+1,iy)
          do ix=ifirst,2,-1
            gL(ix-1,iy) = gL(ix,iy) + diffL
          enddo
        endif
        if (ilast .lt. Nxpx .and. ifirst .ne. ilast) then
          diffL = gL(ilast,iy) - gL(ilast-1,iy)
          if (diffL .gt. 0.0) then
            gL(ilast,iy) = gL(ilast,iy) - 360.0
            diffL = gL(ilast,iy) - gL(ilast-1,iy)
          endif
          if (diffL .lt. 0.0) then
            do ix=ilast,Nxpx-1
              gL(ix+1,iy) = gL(ix,iy) + diffL
            enddo
          endif
          if (diffL .gt. 0.0) then
            gL(ilast,iy) = gL(ilast-1,iy)
          endif
        endif
      end do

      i=1
C      do iy=Nypx,1,-1
      do iy=1,Nypx
        do ix=1,Nxpx
          dLON(i)=gL(ix,iy)
          dLAT(i)=gB(ix,iy)
          i=i+1
        end do
      end do

      i=1
      do iy=1,Nypx
        do ix=1,Nxpx
          dIMAGE(i)=dHsky(ix,iy)
          i=i+1
        end do
      end do

      return
      end
c==========================================================================


c                  **********************************
c                       INITIALIZATION ROUTINES
c                  **********************************

c==========================================================================
      subroutine rzgrid_init(ndes,nr,nz,R,Z,descrfile)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      character*40 descrfile
      dimension R(NRMAX),Z(NZMAX)


      rmin=grinfo(1)
      rmax=grinfo(2)
      dr=grinfo(3)
      zmin=grinfo(4)
      zmax=grinfo(5)

      izlog=igrinfo(1)
      nz=igrinfo(2)
      nr=int((rmax-rmin)/dr)+1


      if ((izlog.eq.1).and.(zmin.eq.0)) then
         zmin=0.001
         zeroflag=1
      end if

      if (izlog.eq.0) then
        dz=(zmax-zmin)/(nz-1)
      else
        zmilog=log10(zmin)
        zmalog=log10(zmax)
        if (zeroflag.eq.1) then
         dzlog=(zmalog-zmilog)/(nz-2)
        else
         dzlog=(zmalog-zmilog)/(nz-1)
        end if
      end if


      if (nz.gt.NZMAX) then
         print *, 'Need to increase NZMAX in program'
         stop
      else if (nr.gt.NRMAX) then
         print *, 'Need to increase NRMAX in program'
         stop
      end if

      do i=1,nz
         if (izlog.eq.0) then
          z(i)=zmin+(i-1)*dz
         else
          if (zeroflag.eq.1) then
           if (i.eq.1) then
            z(i)=0.
           else
            z(i)=10**(zmilog+(i-2)*dzlog)
           end if
           else
            z(i)=10**(zmilog+(i-1)*dzlog)
          end if
         end if
      end do

      do i=1,nr
         r(i)=rmin+(i-1)*dr
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine xyzgrid_init(nr,nz,Rcyl,Zcyl,
     >                        nxx,nyy,nzz,Xx,Yy,Zz)
c==========================================================================
      include "dimen.h"
      dimension Rcyl(NRMAX),Zcyl(NZMAX)
      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)

      if ((Zcyl(1).ne.0).or.(Rcyl(1).ne.0)) then
         print *, 'To use cartesian coordinates,'
         print *, 'R(1) and Z(1) must be zero'
         stop
      end if

c     Note: r and z go from 1 to nr/nz
c           xx,yy,zz go from -nxx,nyy,nzz to +nxx,nyy,nzz
      nxx=nr-1
      nyy=nr-1
      nzz=nz-1

      Xx(0)=0.
      Yy(0)=0.
      Zz(0)=0.
      do ir=2,nr
         Xx( ir)= Rcyl(ir)
         Xx(-ir)=-Rcyl(ir)
         Yy( ir)= Rcyl(ir)
         Yy(-ir)=-Rcyl(ir)
      end do
      do iz=2,nz
         Zz( iz)= Zcyl(iz)
         Zz(-iz)=-Zcyl(iz)
      end do

      return
      end

c==========================================================================

c==========================================================================
      subroutine xyzmkmask(imask,pmask,nxx,nyy,nzz,
     >                     Xx,Yy,Zz,xyzmask)
c==========================================================================
      include "dimen.h"
      include "const.h"
      dimension imask(NMSK),pmask(NMSK)
      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)
      dimension xyzmask(-NXP:NXP,-NYP:NYP,-NZP:NZP)

      call xyzzero(xyzmask)

c     ----------------------------------
c     imask=1: only cylinder with R<Rmax
c     ----------------------------------

      if (imask(1).eq.1) then
        Rmax=pmask(1)
        do  ix=-nxx,nxx
         do iy=-nyy,nyy
           R=sqrt(Xx(ix)**2.+Yy(iy)**2.)
           if (R.le.Rmax) then
             do iz=-nzz,nzz
               xyzmask(ix,iy,iz)=1.
             end do
           end if
         end do
        end do
       else
       end if

       return
       end
c==========================================================================

c==========================================================================
      subroutine cyl2xyz(nr,nz,Rcyl,Zcyl,Arz,
     >             nxx,nyy,nzz,Xx,Yy,Zz,Axyz)
c==========================================================================
      include "dimen.h"
      dimension Rcyl(NRMAX),Zcyl(NZMAX)
      dimension Arz(NRMAX,NZMAX)
      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)
      dimension Axyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)

      dimension ilo(-NXP:NXP,-NYP:NYP),tl(-NXP:NXP,-NYP:NYP)


c     This routine interpolates a xyz grid from an rz grid
c     The first loop calculates the indices of the r array
c     just below the grid point of interest, and the distance
c     to the next grid point divided by delta x.

c     Note: the spacing in x and y is assumed to be uniform!!
c        *AND* the z spacing in cyl and cartesian grid is assumed
c        to be the same...
      if ((Xx(2)-Xx(1)).ne.(Yy(2)-Yy(1))) then
         print *, 'X and Y grid have different spacing!'
         print *, 'cyl2xyz will fail!'
         stop
      end if

      if ((Zz(nzz).ne.Zcyl(nz)).or.(Zz(0).ne.Zcyl(0))) then
         print *, 'Z spacing not right: cyl2xyz'
         stop
      end if

      do iy=-nyy,nyy
       do ix=-nxx,nxx
          xc=real(ix)
          yc=real(iy)
          ilo(ix,iy)=int(sqrt(xc*xc+yc*yc))
          tl(ix,iy)=sqrt(xc*xc+yc*yc)-ilo(ix,iy)
          ilo(ix,iy)=ilo(ix,iy)+1
       end do
      end do

c     if the grid point is within the bounds of the cylindrical
c     array,do linear interpolation
      do iy=-nyy,nyy
       do ix=-nxx,nxx
        do iz=-nzz,nzz
         indx=ilo(ix,iy)
         t=tl(ix,iy)
         izc=abs(nzz)+1
         if (indx+t.le.nr) then
            Axyz(ix,iy,iz)=Arz(indx,izc)*(1-t)+Arz(indx+1,izc)*t
         else
          Axyz(ix,iy,iz)=0.
         end if
        end do
       end do
      end do

      return
      end
c==========================================================================


c==========================================================================
      subroutine xyzzero(Axyz)
c==========================================================================
      include "dimen.h"
      dimension Axyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)

      do iz=-NZP,NZP
       do iy=-NYP,NYP
        do ix=-NXP,NXP
         Axyz(ix,iy,iz)=0.
        end do
       end do
      end do

      return
      end
c==========================================================================

c==========================================================================
c      function trilin(x,y,z,nxx,nyy,nzz,Xx,Yy,Zz,Axyz)
c==========================================================================
c      include "dimen.h"
c      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)
c      dimension Axyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)
c      dimension

c      call 3dbrac(x,y,z,nxx,nyy,nzz,
c      call hunt(Xx,nxx,x,jx)
c      call hunt(Yy,nyy,y,jy)
c      call hunt(Zz,nzz,z,jz)

c      if ((jx.eq.0).or.(jy.eq.0).or.(jz.eq.0))


c==========================================================================
      subroutine tcsky(nxx,nyy,nzz,Xx,Yy,Zz,dHxyz,xyzmask,
     >                 Nxpx,Nypx,gL,gB,dHsky)
c==========================================================================
      include "dimen.h"
      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)
      dimension dHxyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)
      dimension xyzmask(-NXP:NXP,-NYP:NYP,-NZP:NZP)
      dimension gL(NxpxMAX,NypxMAX),gB(NxpxMAX,NypxMAX)
      dimension dHsky(NxpxMAX,NypxMAX)
      character*1 lim

      rad=57.2957795
      dist=6
      ndir=-1
      do ix=1,Nxpx
         do iy=1,Nypx
           if (gL(ix,iy).gt.-499) then
            rlon=gL(ix,iy)/rad
            rlat=gB(ix,iy)/rad
            call dmdsm(rlon,rlat,ndir,dmpsr,dist,lim,sm,smtau,smtheta)
            dHsky(ix,iy)=dmpsr
           else
            dHsky(ix,iy)=0.
           end if
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine tcden(nxx,nyy,nzz,Xx,Yy,Zz,dHxyz,xyzmask)
c==========================================================================
      include "dimen.h"
      dimension Xx(-NXP:NXP),Yy(-NYP:NYP),Zz(-NZP:NZP)
      dimension dHxyz(-NXP:NXP,-NYP:NYP,-NZP:NZP)
      dimension xyzmask(-NXP:NXP,-NYP:NYP,-NZP:NZP)
      common/bomb/ii

C WLH
C      print *, 'Density model leaves out gum nebula for now'

      call xyzzero(dHxyz)

      ii = 1
      do iz=-nzz,nzz
       do iy=-nyy,nyy
        do ix=-nxx,nxx
         if (xyzmask(ix,iy,iz).ne.0.) then
            call tcdensity(Xx(ix),Yy(iy),Zz(iz),dne1,dne2,dnea,dnegum)
            dHxyz(ix,iy,iz)=dne1+dne2+dnea
            ii = 0
         end if
        end do
       end do
      end do

      return
      end
c==========================================================================


c==========================================================================
      subroutine switch_init(PARAMS, ndes,switchfile,parfile,
     * descrfile,abfile)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension PARAMS(13)
      character*40 switchfile,parfile,descrfile,abfile
      character*80 comment
c     set switchfile=unit 10
c         parfile=unit 11

c      character*5 plab(NFORCE)
      data plab/'p_th','p_kin','p_CR','p_B'/

c      character*5 phaselab(NPHASE)
      data phaselab/'Molec','CNM  ','WNM  ','WIM  ','Hot  '/

c      character*5 gravlab(NGRAV)
      data gravlab/'Bulge','Disk ','Halo '/

c      character*4 outlab(NNOUT)
      data outlab/'nH__','rho_','ptot','wt__',
     >            'gz__','gR__','phi_','vr__',
     >            'vr-p','pdef','ab__','np__',
     >            'ff__','sig_','T___','pnth',
     >            'rad_','Bavg'/

      character*30 dflab(NPHASE,NDFCN)
      character*30 pflab(NFORCE,NPFCN)
      character*30 gflab(NGRAV,NGFCN)
      character*30 fflab(NFF)
      character*30 rplab(NRP)
      character*30 ablab(NAB)

c    loops 3 sets of 5phases
      data dflab/'Ferriere 98',
     >         'Ferriere 98',
     >         'Ferriere 98',
     >         'Ferriere 98',
     >         'Ferriere 98',
     >         'Dame 9X',
     >         'df22',
     >         'df32',
     >         'Rand 97',
     >         'Wolfire 95',
     >         'df13',
     >         'Constant',
     >         'Double expon',
     >         'df43',
     >         'Pietz 98'/

      data pflab/'Ferriere 98',
     >         'Ferriere 98',
     >         'Ferriere 98',
     >         'Ferriere 98',
     >         'pf12',
     >         'pf22',
     >         'pf32',
     >         'pf42',
     >         'pf13',
     >         'pf23',
     >         'pf33',
     >         'pf43'/

      data gflab/'Wolfire 95',
     >         'Wolfire 95',
     >         'Wolfire 95',
     >         'Point mass',
     >         'Ferriere 98',
     >         'Ferriere 98',
     >         'Constant',
     >         'Kuijken (local)',
     >         'Sackett'/

      data fflab/'Unity ',
     >            'Manual Set',
     >            'P_th bal'/

      data rplab/'Thick disk ',
     >           'Thin disk ',
     >           'Blank     '/

      data ablab/'Input file',
     >           'Input file+ lin gradient',
     >           'All abunds=1',
     >           'Input file+ exp gradient'/

      character*4 dparlab(NPHASE,NDFCN,NDPAR)
      character*4 pparlab(NFORCE,NPFCN,NPPAR)
      character*4 gparlab(NGRAV,NGFCN,NGPAR)
      character*4 rpparlab(NRP,NRPPAR)
      character*4 ffparlab(NFF,NFFPAR)
      character*4 abparlab(NAB,NABPAR)

c     in the following data statement, 5 phases per line,
c     each line is a function, each block is a parameter.
c     Ex: parameter 2 of phase=3,function=2 would be
c     the third entry of the block 2/line2

      data abparlab/'a1  ','a1  ','a1  ','a1  ',
     >              'a2  ','a2  ','a2  ','a2  ',
     >              'a3  ','a3  ','a3  ','a3  '/

      data rpparlab/'a1  ','a1  ','a1  ',
     >              'a2  ','a2  ','a2  ',
     >              'a3  ','a3  ','a3  ',
     >              'a4  ','a4  ','a4  ',
     >              'a5  ','a5  ','a5  ',
     >              'a6  ','a6  ','a6  ',
     >              'a7  ','a7  ','a7  '/

      data ffparlab/'a1  ','a1  ','a1  ',
     >              'a2  ','a2  ','a2  ',
     >              'a3  ','a3  ','a3  ',
     >              'a4  ','a4  ','a4  ',
     >              'a5  ','a5  ','a5  '/

      data dparlab/'a1  ','a1  ','a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ','a1  ','a1  ',

     >             'a2  ','a2  ','a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ','a2  ','a2  ',

     >             'a3  ','a3  ','a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ','a3  ','a3  '/

      data pparlab/'a1  ','a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ','a1  ',

     >             'a2  ','a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ','a2  ',

     >             'a3  ','a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ','a3  '/

      data gparlab/'a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ',
     >             'a1  ','a1  ','a1  ',

     >             'a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ',
     >             'a2  ','a2  ','a2  ',

     >             'a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  ',
     >             'a3  ','a3  ','a3  '/

      open(unit=10,file=switchfile,status='unknown')
      open(unit=11,file=parfile,status='new')

c     skip over header...
      read(10,*)
      read(10,*)
      read(10,*)
      read(10,*)
      write(11,*)
      write(11,*)
      write(11,*)
      write(11,*)

c     Get grid info.....
      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
      read(10,*) grinfo(1),grinfo(2),grinfo(3)
      read(10,*) grinfo(4),grinfo(5),igrinfo(1),igrinfo(2)

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     density switches
      read(10,*) i1,i2,i3
      write(11,*) i1,i2,i3
      if ((i1.ne.NPHASE).or.(i2.ne.NDFCN).or.(i3.ne.NDPAR)) then
         print *, 'switch.inp has wrong number of switches for code'
      end if

      do i1=1,NPHASE
         read(10,*) iden(i1)
         write(11,*) iden(i1)
         do i2=1,NDFCN
           read(10,*) idfcn(i1,i2)
           write(11,*) idfcn(i1,i2)
         end do
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     pressure switches
      read(10,*) i1,i2,i3
      write(11,*) i1,i2,i3
      if ((i1.ne.NFORCE).or.(i2.ne.NPFCN).or.(i3.ne.NPPAR)) then
         print *, 'switch.inp has wrong number of switches for code'
      end if

      do i1=1,NFORCE
         read(10,*) ipres(i1)
         write(11,*) ipres(i1)
         do i2=1,NDFCN
           read(10,*) ipfcn(i1,i2)
           write(11,*) ipfcn(i1,i2)
         end do
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     gravity switches
      read(10,*) i1,i2,i3
      write(11,*) i1,i2,i3
      if ((i1.ne.NGRAV).or.(i2.ne.NGFCN).or.(i3.ne.NGPAR)) then
         print *, 'switch.inp has wrong number of switches for code'
      end if

      do i1=1,NGRAV
         read(10,*) igrav(i1)
         write(11,*) igrav(i1)
         do i2=1,NGFCN
           read(10,*) igfcn(i1,i2)
           write(11,*) igfcn(i1,i2)
         end do
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     radiation pressure switches
      read(10,*) i1,i2
      write(11,*) i1,i2
      if ((i1.ne.NRP).or.(i2.ne.NRPPAR)) then
         print *, 'switch.inp has wrong number of switches for code'
      end if

      do i1=1,NRP
         read(10,*) irp(i1)
         write(11,*) irp(i1)
      end do


      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     filling factor switches
      read(10,*) i1,i2
      write(11,*) i1,i2
      if ((i1.ne.NFF).or.(i2.ne.NFFPAR)) then
         print *, 'switch.inp has wrong number of switches for code'
      end if

      do i1=1,NFF
         read(10,*) iff(i1)
         write(11,*) iff(i1)
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     abundance switches
      read(10,*) i1,i2
      write(11,*) i1,i2
      if ((i1.ne.NAB).or.(i2.ne.NABPAR)) then
         print *, 'switch.inp has wrong number of switches for code'
      end if

      do i1=1,NAB
         read(10,*) iab(i1)
         write(11,*) iab(i1)
      end do
      read(10,8080) abfile
      write(11,8080) abfile
 8080 format(6x,a40)


      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     output switches
      read(10,*) i1
      write(11,*) i1
      if (i1.ne.NNOUT) then
         print *, 'switch.inp has wrong number of switches for code'
      end if
      do i=1,NNOUT
        read(10,*) iout(i),ipout(i)
        write(11,*) iout(i),ipout(i)
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     slice switches
      read(10,*) i1
      write(11,*) i1
      if (i1.ne.NSLICE) then
         print *, 'switch.inp has wrong number of switches for code'
      end if
      if (i1.gt.9) then
         print *, 'Need to modify filenaming if nslice>9'
         stop
      end if
      do i=1,NSLICE
         read(10,*) islice(i),xslice(i)
         write(11,*) islice(i),xslice(i)
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     Density parameters
      do i1=1,NPHASE
         do i2=1,NDFCN
            read(10,*) (dpars(i1,i2,i3),i3=1,NDPAR)
            write(11,*) (dpars(i1,i2,i3),i3=1,NDPAR)
         end do
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment

c
c     Pressure parameters
      do i1=1,NFORCE
         do i2=1,NPFCN
            read(10,*) (ppars(i1,i2,i3),i3=1,NPPAR)
            write(11,*) (ppars(i1,i2,i3),i3=1,NPPAR)
         end do
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     Gravity parameters
      do i1=1,NGRAV
         do i2=1,NGFCN
            read(10,*) (gpars(i1,i2,i3),i3=1,NGPAR)
            write(11,*) (gpars(i1,i2,i3),i3=1,NGPAR)
         end do
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     Radiation pressure parameters
      do i1=1,NRP
            read(10,*) (rppars(i1,i2),i2=1,NRPPAR)
            write(11,*) (rppars(i1,i2),i2=1,NRPPAR)
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     Filling factor parameters
      do i1=1,NFF
            read(10,*) (ffpars(i1,i2),i2=1,NFFPAR)
            write(11,*) (ffpars(i1,i2),i2=1,NFFPAR)
      end do

      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     Abundance parameters
      do i1=1,NAB
            read(10,*) (abpars(i1,i2),i2=1,NABPAR)
            write(11,*) (abpars(i1,i2),i2=1,NABPAR)
      end do

c     The below is a little jury rigged...
      read(10,'(a80)') comment
      write(11,'(a80)') comment
C      write(6,'(a80)') comment
c     Talylor Cordes model parameters
C WLH here are the 13 interactive model paramters
      tcpars(1,1)=PARAMS(1)
      tcpars(1,2)=PARAMS(2)
      tcpars(1,3)=PARAMS(3)
      tcpars(1,4)=PARAMS(4)
      tcpars(2,1)=PARAMS(5)
      tcpars(2,2)=PARAMS(6)
      tcpars(2,3)=PARAMS(7)
      tcpars(2,4)=PARAMS(8)
      do i1=1,2
C WLH
C            read(10,*) (tcpars(i1,i2),i2=1,4)
            write(11,*) (tcpars(i1,i2),i2=1,4)
      end do
      tcpars(3,1)=PARAMS(9)
      tcpars(3,2)=PARAMS(10)
      tcpars(3,3)=PARAMS(11)
      tcpars(3,4)=PARAMS(12)
      tcpars(3,5)=PARAMS(13)
C WLH
C      read(10,*) (tcpars(3,i2),i2=1,5)
      write(11,*) (tcpars(3,i2),i2=1,5)



c     Print out text info on functions/parameters chosen....

      nrr=int((grinfo(2)-grinfo(1))/grinfo(3))+1

      write(ndes,1121) nrr, grinfo(1), grinfo(2), grinfo(3)
 1121 format(i5,' R points from R=',f8.3,' to ',f8.3, 'kpc  dr=',f8.3)

      if (igrinfo(1).eq.0) then
         write(ndes,1122) igrinfo(2),grinfo(4),grinfo(5)
      else if (igrinfo(1).eq.1) then
         write(ndes,1123) igrinfo(2),grinfo(4),grinfo(5)
      end if

 1122 format(i5,' points linearly spaced: z=',f7.3,' to', f7.3,' kpc')
 1123 format(i5,' points log spaced: z=',f7.3,' to', f7.3,' kpc')


      write(ndes,*)

c     Density info
      write(ndes,1201)
      do i1=1,NPHASE
         if (iden(i1).eq.1) then
            write(ndes,1301) phaselab(i1)
            do i2=1,NDFCN
            if (idfcn(i1,i2).eq.1) then
               write(ndes,1401) dflab(i1,i2)
               if ((dpars(i1,i2,1).ne.0).or.(dpars(i1,i2,2).ne.0).or.
     >             (dpars(i1,i2,3).ne.0)) then
               write(ndes,1601) (dparlab(i1,i2,k),dpars(i1,i2,k),
     >                           k=1,NDPAR)
               end if
            end if
            end do
         else
            write(ndes,1501) phaselab(i1)
         end if
      end do

c     Pressure info
      write(ndes,1202)
      do i1=1,NFORCE
         if (ipres(i1).eq.1) then
            write(ndes,1302) plab(i1)
            do i2=1,NPFCN
            if (ipfcn(i1,i2).eq.1) then
               write(ndes,1402) pflab(i1,i2)
               if ((ppars(i1,i2,1).ne.0).or.(ppars(i1,i2,2).ne.0).or.
     >             (ppars(i1,i2,3).ne.0).or.(ppars(i1,i2,4).ne.0).or.
     >             (ppars(i1,i2,5).ne.0)) then
               write(ndes,1602) (pparlab(i1,i2,k),ppars(i1,i2,k),
     >                           k=1,NPPAR)
               end if
            end if
            end do
         else
            write(ndes,1502) plab(i1)
         end if
      end do

c     Gravity info
      write(ndes,1203)
      do i1=1,NGRAV
         if (igrav(i1).eq.1) then
            write(ndes,1303) gravlab(i1)
            do i2=1,NGFCN
            if (igfcn(i1,i2).eq.1) then
               write(ndes,1403) gflab(i1,i2)
c     if all parameters are zero, don't print out parameter line
               if ((gpars(i1,i2,1).ne.0).or.(gpars(i1,i2,2).ne.0).or.
     >             (gpars(i1,i2,3).ne.0)) then
               write(ndes,1603) (gparlab(i1,i2,k),gpars(i1,i2,k),
     >                           k=1,NGPAR)
               end if
            end if
            end do
         else
            write(ndes,1503) gravlab(i1)
         end if
      end do

c   Radiation pressure info
      irpon=0
      do i1=1,NRP
         if (irp(i1).eq.1) then
            irpon=1
            go to 444
         end if
      end do
 444  if (irpon.eq.0) then
      write(ndes,1999)
      write(ndes,1998)
      else
      write(ndes,1999)
      do i1=1,NRP
         if (irp(i1).eq.1) then
            write(ndes,2001) rplab(i1)
               if ((rppars(i1,1).ne.0).or.(rppars(i1,2).ne.0).or.
     >             (rppars(i1,3).ne.0).or.(rppars(i1,4).ne.0).or.
     >             (rppars(i1,5).ne.0).or.(rppars(i1,6).ne.0).or.
     >             (rppars(i1,7).ne.0)) then
               write(ndes,2002) (rpparlab(i1,k),rppars(i1,k),
     >                           k=1,NRPPAR)
               end if
         end if
      end do
      end if




c    Filling factor info
      write(ndes,2000)
      do i1=1,NFF
         if (iff(i1).eq.1) then
            write(ndes,2001) fflab(i1)
               if ((ffpars(i1,1).ne.0).or.(ffpars(i1,2).ne.0).or.
     >             (ffpars(i1,3).ne.0).or.(ffpars(i1,4).ne.0).or.
     >             (ffpars(i1,5).ne.0)) then
               write(ndes,2002) (ffparlab(i1,k),ffpars(i1,k),
     >                           k=1,NFFPAR)
               end if
         end if
      end do

c     Abundance info
      write(ndes,2010)
      write(ndes,1222) abfile
 1222 format('Abundance table: ', a40)
      do i1=1,NAB
         if (iab(i1).eq.1) then
            write(ndes,2001) ablab(i1)
               if ((abpars(i1,1).ne.0).or.(abpars(i1,2).ne.0).or.
     >             (abpars(i1,3).ne.0) ) then
                     write(ndes,2002) (abparlab(i1,k),abpars(i1,k),
     >                           k=1,NABPAR)
               end if
         end if
      end do

 1998 format('Radiation Pressure turned OFF')
 1999 format('*******RADIATION PRESSURE INFO*******')
 2000 format('*******FILLING FACTOR INFO*********')
 2010 format('*******ABUNDANCE INFO*********')
 2001 format(1x,a30)
 2002 format(3x,5(a4,'=',f9.2,1x))


 1201 format('*******DENSITY INFO*********')
 1301 format(a5,2x,'phase turned ON')
 1401 format(1x,a30)
 1501 format(a5,2x,'phase turned OFF')
 1601 format(3x,3(a4,'=',f9.2,1x))

 1202 format('*******PRESSURE INFO*********')
 1302 format(a5,2x,'phase turned ON')
 1402 format(1x,a30)
 1502 format(a5,2x,'phase turned OFF')
 1602 format(3x,5(a4,'=',f9.2,1x))

 1203 format('*******GRAVITY INFO*********')
 1303 format(a5,2x,'phase turned ON')
 1403 format(1x,a30)
 1503 format(a5,2x,'phase turned OFF')
 1603 format(3x,3(a4,'=',f9.2,1x))

      close(unit=10)
      close(unit=11)

      return
      end
c==========================================================================

c                 *********************************
c                        ABUNDANCE ROUTINES
c                  Calculates abundance patterns of
c                  different elements as a function
c                  of position in the Galaxy
c                 *********************************
c==========================================================================
      subroutine abcalc(nr,nz,R,Z,abrzZ,abfile)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"

      dimension R(NRMAX),Z(NZMAX)
      dimension abrzZ(NRMAX,NZMAX,NEL)
      character*40 abfile


c     Read in abundance file...
      nunab=15
      open(unit=nunab,file=abfile,status='unknown')
      call species_rd(nunab)
      close(unit=nunab)


      do izZ=1,NEL
         do iz=1,NZMAX
            do ir=1,NRMAX
               abrzZ(ir,iz,izZ)=0
            end do
         end do
      end do

c     Use abundances the same all over the Galaxy,
c     and assume they come from input table
      if (iab(1).eq.1) then
        do izZ=1,NEL
           do iz=1,nz
              do ir=1,nr
                 abrzZ(ir,iz,izZ)=abund(indexel(izZ))
              end do
           end do
        end do
c     Use slope of oxygen abundance gradient for all
c     elements *except* H, He, and molecules designed to
c     match input file at solar circle....
      else if (iab(2).eq.1) then
        if (abund(indexel(8)).eq.0) then
           print *, 'Cant use abundance gradient if oxygen'
           print *, 'abundance is zero'
           stop
        end if
        rm=abpars(2,1)
        b=alog10(abund(indexel(8)))-rm*RSUN
        do iz=1,nz
          do ir=1,nr
            fac=10**(rm*R(ir)+b)
            do izZ=1,NEL
              if((indexel(izZ).le.2).or.(indexel(izZ).ge.27)) then
                abrzZ(ir,iz,izZ)=abund(indexel(izZ))
              else
                abrzZ(ir,iz,izZ)=fac*abund(indexel(izZ))
              end if
            end do
          end do
        end do
c     Set all abundances = 1....
      else if (iab(3).eq.1) then
        do izZ=1,NEL
           do iz=1,nz
              do ir=1,nr
                 abrzZ(ir,iz,izZ)=1.
              end do
           end do
        end do
       else if (iab(4).eq.1) then
        if (abund(indexel(8)).eq.0) then
           print *, 'Cant use abundance gradient if oxygen'
           print *, 'abundance is zero'
           stop
        end if
        do iz=1,nz
           do ir=1,nr
           fac=exp(abpars(4,1)*(R(ir)-RSUN))
            do izZ=1,NEL
              if((indexel(izZ).le.2).or.(indexel(izZ).ge.27)) then
                abrzZ(ir,iz,izZ)=abund(indexel(izZ))
              else
                abrzZ(ir,iz,izZ)=fac*abund(indexel(izZ))
              end if
            end do
           end do
        end do
      end if

      return
      end
c==========================================================================

c                ***********************************
c                      GAS DENSITY ROUTINES
c                  gives gas density as a function
c                           of R and Z
c                ***********************************

c==========================================================================
      subroutine dHcalc(nr,nz,Rr,Zz,drz,drzp)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"
c     * R in kpc, Z in kpc
c     * returns particle density of hydrogen nuclei
c     * iden is 5 element array, 0= off, 1=on
c              iden(1)=molecules
c              iden(2)=cnm
c              iden(3)=wnm
c              iden(4)=wim
c              iden(5)=hm

      dimension Rr(NRMAX),Zz(NZMAX)
      dimension drz(NRMAX,NZMAX)
      dimension drzp(NRMAX,NZMAX,NPHASE)

c     ALL NEW DENSITY FUNCTION MUST BE DECLARED
c     EXTERNAL HERE!!!
c     Note that the number of functions must
c     be equal to the maximum size of the
c     array NFCN(phase,functions) in the
c     common block
      external df11,df12,df13
      external df21,df22,df23
      external df31,df32,df33
      external df41,df42,df43
      external df51,df52,df53

c     -----------------------------
c     Check limits
c     ----------------------------
c      if (Rr(1).lt.3.5) then
c         print *, 'Density unknown for R<3.5 kpc'
c         stop
c      else if (Rr(nr).gt.20) then
c         print *, 'Unsure of density for R>20 kpc'
c         stop
c      end if

c     -----------------------
c     Make sure Z is positive
c     -----------------------
      do iz=1,nz
         if (Zz(iz).lt.0) then
            print *, 'Warning: density formulae not'
            print *, 'checked for negative z yet!'
            stop
         end if
      end do

      do ip=1,NPHASE
        do iz=1,NZMAX
           do ir=1,NRMAX
              drzp(ir,iz,ip)=0
           end do
        end do
      end do

      do iz=1,NZMAX
         do ir=1,NRMAX
            drz(ir,iz)=0.
         end do
      end do


c     -----------------------
c     Go through phases, for each
c     phase, add as many density functions
c     as wanted (controlled by idfcn)
c     (Note: we must do an "if" statement
c     for every density function, since not
c     all of them are mutually exclusive...)
c     -----------------------

      if (iden(1).eq.1) then
         if (idfcn(1,1).eq.1) call df(df11,nr,nz,Rr,Zz,drzp(1,1,1))
         if (idfcn(1,2).eq.1) call df(df12,nr,nz,Rr,Zz,drzp(1,1,1))
         if (idfcn(1,3).eq.1) call df(df13,nr,nz,Rr,Zz,drzp(1,1,1))
      end if

      if (iden(2).eq.1) then
         if (idfcn(2,1).eq.1) call df(df21,nr,nz,Rr,Zz,drzp(1,1,2))
         if (idfcn(2,2).eq.1) call df(df22,nr,nz,Rr,Zz,drzp(1,1,2))
         if (idfcn(2,3).eq.1) call df(df23,nr,nz,Rr,Zz,drzp(1,1,2))
      end if

      if (iden(3).eq.1) then
         if (idfcn(3,1).eq.1) call df(df31,nr,nz,Rr,Zz,drzp(1,1,3))
         if (idfcn(3,2).eq.1) call df(df32,nr,nz,Rr,Zz,drzp(1,1,3))
         if (idfcn(3,3).eq.1) call df(df33,nr,nz,Rr,Zz,drzp(1,1,3))
      end if

      if (iden(4).eq.1) then
         if (idfcn(4,1).eq.1) call df(df41,nr,nz,Rr,Zz,drzp(1,1,4))
         if (idfcn(4,2).eq.1) call df(df42,nr,nz,Rr,Zz,drzp(1,1,4))
         if (idfcn(4,3).eq.1) call df(df43,nr,nz,Rr,Zz,drzp(1,1,4))
      end if

      if (iden(5).eq.1) then
         if (idfcn(5,1).eq.1) call df(df51,nr,nz,Rr,Zz,drzp(1,1,5))
         if (idfcn(5,2).eq.1) call df(df52,nr,nz,Rr,Zz,drzp(1,1,5))
         if (idfcn(5,3).eq.1) call df(df53,nr,nz,Rr,Zz,drzp(1,1,5))
      end if


       do iz=1,nz
        do ir=1,nr
         do ip=1,NPHASE
           drz(ir,iz)=drz(ir,iz)+drzp(ir,iz,ip)
         end do
        end do
       end do



      return
      end
c==========================================================================

c==========================================================================
      subroutine ffcalc(nr,nz,Rcyl,Zcyl,dtotrzp,ffrzp)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      dimension Rcyl(NRMAX),Zcyl(NZMAX)
      dimension dtotrzp(NRMAX,NZMAX,NPHASE)
      dimension ffrzp(NRMAX,NZMAX,NPHASE)
      dimension pT(NPHASE),is(NPHASE),R(NPHASE-1)

      do ip=1,NPHASE
         do iz=1,NZMAX
            do ir=1,NRMAX
               ffrzp(ir,iz,ip)=0
            end do
         end do
      end do

      if (iff(1).eq.1) then
         do ip=1,NPHASE
            do iz=1,nz
               do ir=1,nr
                  ffrzp(ir,iz,ip)=1
               end do
            end do
         end do
      else if (iff(2).eq.1) then
         do ip=1,NPHASE
            do iz=1,nz
               do ir=1,nr
                  ffrzp(ir,iz,ip)=ffpars(2,ip)
               end do
            end do
         end do
      else if (iff(3).eq.1) then
         print *, 'filling factor may have bug'
         print *, 'need to verify'
         pause
         do iz=1,nz
            do ir=1,nr

c              calculate partial pressures
               do ip=1,NPHASE
                  if (iden(ip).eq.1) then
                     pT(ip)=dtotrzp(ir,iz,ip)*ffpars(3,ip)
                  else
                     pT(ip)=0
                  end if
               end do
c              sort partial pressures
               call indexx(NPHASE,pT,is)
c              figure out where we are nonzero
               ik=1
 40            if (pT(is(ik)).eq.0) then
                   ik=ik+1
                   go to 40
               end if
               iks=ik

               if (iks.eq.NPHASE) then
                  ffrzp(ir,iz,is(iks))=1
               else
                 sum=1
                 prod=1
                 do ik=NPHASE-1,iks,-1
                    R(ik)=pT(is(ik+1))/pT(is(ik))
                    prod=prod*R(ik)
                    sum=sum+prod
                 end do

                 ftop=1/(sum)
                 ffrzp(ir,iz,is(NPHASE))=ftop
                 do ik=NPHASE-1,iks,-1
                   ffrzp(ir,iz,is(ik))=ffrzp(ir,iz,is(ik+1))/R(ik)
                 end do
                 do ik=1,iks-1
                    ffrzp(ir,iz,is(ik))=0.
                 end do


               end if





            end do
         end do
      else
         print *, 'iff not set correctly'
         stop
      end if
      return
      end

c==========================================================================


c==========================================================================
      subroutine dtotcalc(nr,nz,dHrzp,dtotrzp)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"

      dimension dHrzp(NRMAX,NZMAX,NPHASE)
      dimension dtotrzp(NRMAX,NZMAX,NPHASE)

      do ip=1,NPHASE
         do iz=1,NZMAX
            do ir=1,NRMAX
               dtotrzp(ir,iz,ip)=0
            end do
         end do
      end do

c     --------------------------------------
c     Calculate total ionic particle density
c     assuming all elements are atomic
c     (will subtract for molecules or
c      add in electrons later)
c     --------------------------------------
      ab=0
      do i=1,NEL
         ab=ab+abund(indexel(i))
      end do

c     convert to total particle density
c     MM=all H in H2, everything else atomic
      do iz=1,nz
         do ir=1,nr
          dtotrzp(ir,iz,1)=dHrzp(ir,iz,1)*(ab-0.5*abund(indexel(1)))
          dtotrzp(ir,iz,2)=dHrzp(ir,iz,2)*ab
          dtotrzp(ir,iz,3)=dHrzp(ir,iz,3)*ab
c         WIM=all H ionized, everything else neutral
          dtotrzp(ir,iz,4)=dHrzp(ir,iz,4)*(ab+abund(indexel(1)))
c         HM=all H and He ionized, everything else neutral
          dtotrzp(ir,iz,5)=dHrzp(ir,iz,5)*(ab+abund(indexel(1))+
     >          2*abund(indexel(2)))
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine df(dfunc,nr,nz,Rr,Zz,drz)
c==========================================================================
      include "dimen.h"
      dimension Rr(NRMAX),Zz(NZMAX)
      dimension drz(NRMAX,NZMAX)



      do iz=1,nz
         do ir=1,nr
            drz(ir,iz)=drz(ir,iz)+dfunc(Rr(ir),Zz(iz))
         end do
      end do

      return
      end
c==========================================================================


c==========================================================================
      function df11(R,Z)
c==========================================================================
c     Molecular component (from Ferriere 98)
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      if (R.gt.3.0) then
      h=(81*(R/RSUN)**0.58)/1000.
      rfac= (R/RSUN)**(-0.58)
      else
      h=(81*(3.0/RSUN)**0.58)/1000.
      rfac=(3.0/RSUN)**(-0.58)
      end if

      df11=0.58*exp(-( (R-4.5)**2-(RSUN-4.5)**2 )/(2.9)**2)*
     >         rfac*exp(-(Z/h)**2)
      return
      end
c==========================================================================
c==========================================================================
      function df12(R,Z)
c==========================================================================
c     Molecular component (try to add in Dame disk)
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

c                        l=40      l=30    l=50
c                        interarm  Scutum  Sagitt
c     midplane density   x 0.14    x 0.10  x 0.22
c     scaleheight        x 2.6     x 3.1   x 3.3
c     column diff        x 0.38    x 0.34  x 0.73

      facn=dpars(1,2,1)
      facH=dpars(1,2,2)

      if (R.gt.3.0) then
      h=facH*(81*(R/RSUN)**0.58)/1000
      else
      h=facH*(81*(3.0/RSUN)**0.58)/1000
      end if
      df12=facn*0.58*exp(-( (R-4.5)**2-(RSUN-4.5)**2 )/(2.9)**2)*
     >          (R/RSUN)**(-0.58)*exp(-(Z/h)**2)

      return
      end
c==========================================================================
c==========================================================================
      function df13(R,Z)
c==========================================================================
c     Molecular component
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      df13=0
      return
      end
c==========================================================================


c==========================================================================
      function df21(R,Z)
c==========================================================================
c     CNM (from Ferriere 98)
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      if (R.lt.RSUN) then
         alpha=1.
      else
         alpha=R/RSUN
      end if
      h1=(127*alpha)/1000.
      h2=(318*alpha)/1000.
      h3=(403*alpha)/1000.
      df21=0.340/(alpha*alpha)*
     >         (0.859*exp(-(Z/H1)**2)+
     >          0.047*exp(-(Z/H2)**2)+
     >          0.094*exp(-Z/H3)       )

c     Arbitrary cutoff interior to some radius....
      if (R.lt.3.0) df21=df21*exp(-3.0+R)
      return
      end
c==========================================================================
c==========================================================================
      function df22(R,Z)
c==========================================================================
c     CNM component
      include "const.h"
      include "dimen.h"
      include "galstruct.h"


      df22=0
      return
      end
c==========================================================================
c==========================================================================
      function df23(R,Z)
c==========================================================================
c     constant density
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      df23=dpars(2,3,1)
      return
      end
c==========================================================================


c==========================================================================
      function df31(R,Z)
c==========================================================================
c     WNM
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

        if (R.lt.RSUN) then
           alpha=1.
        else
           alpha=R/RSUN
        end if
        h1=(127*alpha)/1000.
        h2=(318*alpha)/1000.
        h3=(403*alpha)/1000.
        df31=0.226/(alpha)*
     >         ((1.745-1.289/alpha)*exp(-(Z/H1)**2)+
     >          (0.473-0.070/alpha)*exp(-(Z/H2)**2)+
     >          (0.283-0.142/alpha)*exp(-Z/H3)       )
        if (R.lt.3.0) df31=df31*exp(-3.0+R)
        return
        end
c==========================================================================
c==========================================================================
      function df32(R,Z)
c==========================================================================
c     WNM component
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

c     Crudely estimated parameters of
c     Kalberla layer  (from abstract of Kalberla et al
c     Local Bubble meeting)

      df32=1.03e-3*exp(-Z/4.4)

      return
      end
c==========================================================================
c==========================================================================
      function df33(R,Z)
c==========================================================================
c     Simple exponential
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      df33=dpars(3,3,1)*exp(-R/dpars(3,3,2))*exp(-Z/dpars(3,3,3))

      return
      end
c==========================================================================



c==========================================================================
      function df41(R,Z)
c==========================================================================
c     WIM
      include "const.h"
      include "dimen.h"
      include "galstruct.h"


      h1=1.0
      h2=(150./1000.)
      df41=0.0237*exp(-(R*R-RSUN*RSUN)/(37*37))*exp(-z/h1)+
     >     0.0013*exp(-((R-4)*(R-4)-(RSUN-4)*(RSUN-4))/4)*exp(-z/h2)
      return
      end
c==========================================================================
c==========================================================================
      function df42(R,Z)
c==========================================================================
c     Attempt to include possible extended WIM component...
c     Take the Ferriere formulation of the ISM thick disk
c     and decrease midplane density by x5 and increase
c     scaleheight by 5
c     This all assumes that filling factor is 1, so provides
c     maximum estimate of what might be there...

      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      fac1=dpars(4,2,1)
      fac2=dpars(4,2,2)

      h1=1.0
      df42=fac1*0.0237*exp(-(R*R-RSUN*RSUN)/(37*37))*
     >                 exp(-z/(fac2*h1))

      return
      end
c==========================================================================

c==========================================================================
      function df43(R,Z)
c==========================================================================
c     WIM component
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      dn1=0
      h1=1
      A1=1
      c1=dn1*(sech(R/A1)/sech(8.5/A1))**2.*(sech(Z/h1))**2.

      dn2=0
      h2=1
      A2=1
      c2=dn2*exp(-((R-A2)/1.8)**2.)*(sech(Z/h2))**2

      dna=0
      ha=0

      df43=0
      return
      end
c==========================================================================


c==========================================================================
      function df51(R,Z)
c==========================================================================
c     HM
      include "const.h"
      include "dimen.h"
      include "galstruct.h"

      Hh=1.5*(R/RSUN)**(1.65)
      df51=4.8e-4*(R/RSUN)**(-1.65)*exp(-Z/Hh)
     >           * (0.12*exp(-(R-RSUN)/4.9)+
     >    0.88*exp(-((R-4.5)*(R-4.5)-(RSUN-4.5)*(RSUN-4.5))/(2.9*2.9)))

      return
      end
c==========================================================================
c==========================================================================
      function df52(R,Z)
c==========================================================================
c     HIM component
      include "const.h"
      include "dimen.h"
      include "galstruct.h"


c      z in pc; density in cm-3
      zkpc=z/1000.
      T=1e6
      T6=1
      df52=2250*sqrt(T6)*(1+(zkpc*zkpc)/19.6)**(-1.35/T6)/T/2.
      return
      end
c==========================================================================

c==========================================================================
      function df53(R,Z)
c==========================================================================
c     HIM component
      include "const.h"
      include "dimen.h"
      include "galstruct.h"
      dimension gvec(3)
      parameter(THALO=0.135)
      parameter(den0=1.3e-3)
      parameter(A1=15)

c     Convert from kT to T (K)
      T=(THALO/0.0861)*1e6

      if (Z.lt.0.4) then
         df53=0
      else
         g1=(sech((R/A1)))**2/(sech((RSUN/A1)))**2
         call gf23(R,Z,gvec)
         phi=gvec(3)/1e10
         df53=den0*g1*exp(-120*phi/T)
      end if

      return
      end
c==========================================================================



c                ****************************
c                    PRESSURE ROUTINES
c                ****************************

c==========================================================================
      subroutine pcalc(nr,nz,R,Z,dHrzp,dtotrzp,Trzp,sigrzp,przp,prz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"
c     * R in kpc, Z in kpc

      dimension R(NRMAX),Z(NZMAX)
      dimension dHrzp(NRMAX,NZMAX,NPHASE),dtotrzp(NRMAX,NZMAX,NPHASE)
      dimension Trzp(NRMAX,NZMAX,NPHASE),sigrzp(NRMAX,NZMAX,NPHASE)
      dimension prz(NRMAX,NZMAX)
      dimension przp(NRMAX,NZMAX,NFORCE)

c     ALL NEW PRESSURE FUNCTION MUST BE DECLARED
c     EXTERNAL HERE!!!
c     Note that the number of functions must
c     be equal to the maximum size of the
c     array NFCN(phase,functions) in the
c     common block
      external pf11,pf12,pf13
      external pf21,pf22,pf23
      external pf31,pf32,pf33
      external pf41,pf42,pf43

      do if=1,NFORCE
        do iz=1,NZMAX
          do ir=1,NRMAX
             przp(ir,iz,if)=0.
          end do
        end do
      end do

      do iz=1,NZMAX
         do ir=1,NRMAX
            prz(ir,iz)=0
         end do
      end do

      if (ipres(1).eq.1) then
         call pfa(pf11,nr,nz,R,Z,dtotrzp,Trzp,przp(1,1,1))
      end if

      if (ipres(2).eq.1) then
         call pfa(pf21,nr,nz,R,Z,dHrzp,sigrzp,przp(1,1,2))
      end if

      if (ipres(3).eq.1) then
         if (ipfcn(3,1).eq.1) call pfb(pf31,nr,nz,R,Z,przp(1,1,3))
         if (ipfcn(3,2).eq.1) call pfb(pf32,nr,nz,R,Z,przp(1,1,3))
         if (ipfcn(3,3).eq.1) call pfb(pf33,nr,nz,R,Z,przp(1,1,3))
      end if

      if (ipres(4).eq.1) then
         if (ipfcn(4,1).eq.1) call pfb(pf41,nr,nz,R,Z,przp(1,1,4))
         if (ipfcn(4,2).eq.1) call pfb(pf42,nr,nz,R,Z,przp(1,1,4))
         if (ipfcn(4,3).eq.1) call pfb(pf43,nr,nz,R,Z,przp(1,1,4))
      end if


       do iz=1,nz
        do ir=1,nr
         prz(ir,iz)=0.
         do if=1,NFORCE
           prz(ir,iz)=prz(ir,iz)+przp(ir,iz,if)
         end do
        end do
       end do


      return
      end
c==========================================================================

c==========================================================================
      subroutine pfa(pfunc,nr,nz,R,Z,den,sig,prz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      dimension R(NRMAX),Z(NZMAX)
      dimension den(NRMAX,NZMAX,NPHASE)
      dimension sig(NRMAX,NZMAX,NPHASE)
      dimension dzone(NPHASE),sigone(NPHASE)
      dimension prz(NRMAX,NZMAX)

      do iz=1,nz
         do ir=1,nr
            do ip=1,NPHASE
               dzone(ip)=den(ir,iz,ip)
               sigone(ip)=sig(ir,iz,ip)
            end do
            prz(ir,iz)=prz(ir,iz)+pfunc(dzone,sigone,R(ir),Z(iz))
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine pfb(pfunc,nr,nz,R,Z,prz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      dimension R(NRMAX),Z(NZMAX)
      dimension prz(NRMAX,NZMAX)

      do iz=1,nz
         do ir=1,nr
            prz(ir,iz)=prz(ir,iz)+pfunc(R(ir),Z(iz))
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      function pf11(den,Tph,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      dimension den(NPHASE),Tph(NPHASE)
c     ppars is temperature in K

      pf11=0
      do i=1,NPHASE
         pf11=pf11+den(i)*BOLTZK*Tph(i)
      end do

      return
      end
c==========================================================================

c==========================================================================
      function pf12(den,Tph,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      dimension den(NPHASE),Tph(NPHASE)
      pf12=0

      return
      end
c==========================================================================
c==========================================================================
      function pf13(den,Tph,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      dimension den(NPHASE),Tph(NPHASE)
      pf13=0

      return
      end
c==========================================================================


c==========================================================================
      function pf21(den,sig,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"

      dimension den(NPHASE),sig(NPHASE)
c     ppars is velocity dispersion in km/s

      pf21=0
      do i=1,NPHASE
         pf21=pf21+RHO0*den(i)*(sig(i)*1e5)**2
      end do

      return
      end
c==========================================================================
c==========================================================================
      function pf22(den,sig,R,Z)
c==========================================================================
      include "dimen.h"

      include "const.h"
      include "galstruct.h"
      dimension den(NPHASE),sig(NPHASE)
      pf22=0

      return
      end
c==========================================================================
c==========================================================================
      function pf23(den,sig,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      dimension den(NPHASE),sig(NPHASE)

      pf23=0

      return
      end
c==========================================================================


c==========================================================================
      function pf31(R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      rn=rnCRfer(R)
      b=bCRfer(R)
      h1=255./1000.
      pf31=10.3e-13*(0.46*exp(-(R-RSUN)/2.8)*(sech(z/h1))**(rn)+
     >       0.54*exp(-(R-RSUN)/3.3)*(sech(z/h1))**(b))**(1/1.875)
      return
      end
c==========================================================================
c==========================================================================
      function pf32(R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf32=0

      return
      end
c==========================================================================
c==========================================================================
      function pf33(den,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf33=0

      return
      end
c==========================================================================


c==========================================================================
      function pf41(R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      rn=rnCRfer(R)
      b=bCRfer(R)
      h1=255./1000.
      pf41=9.6e-13*(0.46*exp(-(R-RSUN)/2.8)*(sech(z/h1))**(rn)+
     >       0.54*exp(-(R-RSUN)/3.3)*(sech(z/h1))**(b))**(1/1.875)
      return
      end
c==========================================================================
c==========================================================================
      function pf42(R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf42=0

      return
      end
c==========================================================================
c==========================================================================
      function pf43(den,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf43=0

      return
      end
c==========================================================================


c==========================================================================
      function pf51(R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf51=0.
      return
      end
c==========================================================================
c==========================================================================
      function pf52(den,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf52=0

      return
      end
c==========================================================================
c==========================================================================
      function pf53(den,R,Z)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"

      pf53=0

      return
      end
c==========================================================================


c==========================================================================
      function sech(x)
c==========================================================================
      sech=1/cosh(x)
      return
      end
c==========================================================================



c==========================================================================
      function bCRfer(R)
c==========================================================================
c     Fit to numerical curves output by program sech.f
c     Beyond a certain radius, warn user to use more exact values
c     (which could be input in tabular form from sech.f)
c--------------------------------------------------------------------------
      bCRfer=0.6256*exp(-R/7.067)
      if (R.gt.20) then
         print *, 'Warning! Fit to b(R) worse than 20%'
      end if

      return
      end
c==========================================================================


c==========================================================================
      function rnCRfer(R)
c==========================================================================
c     Fit to numerical curves output by program sech.f
c     Beyond a certain radius, warn user to use more exact values
c     (which could be input in tabular form from sech.f)
c--------------------------------------------------------------------------
      rnCRfer=30.98*exp(-R/4.4565)
      if (R.gt.16) then
         print *, 'Warning! Fit to n(R) worse than 20%'
      end if

      return
      end

c==========================================================================

c==========================================================================
      subroutine Bavgcalc(iBgeom,nr,nz,Rcyl,Zcyl,przp,Bavgrz)
c==========================================================================
c     Returns B array in microgauss
      include "dimen.h"
      include "const.h"
      dimension Rcyl(NRMAX)
      dimension Zcyl(NZMAX)
      dimension przp(NRMAX,NZMAX,NFORCE)
      dimension Bavgrz(NRMAX,NZMAX)

      do iz=1,NZMAX
         do ir=1,NRMAX
            Bavgrz(ir,iz)=0.
         end do
      end do

c     Assume pressure converts straightforwardly to B field
c     p_B=B^2/(8*PI)

      if (iBgeom.eq.1) then
      do iz=1,nz
         do ir=1,nr
            Bavgrz(ir,iz)=1e6*sqrt(8*PI*przp(ir,iz,3))
         end do
      end do
      else
         print *, 'Bad iBgeom!'
         stop
      end if

      return
      end
c==========================================================================


c==========================================================================
      subroutine vdispcalc(nr,nz,Rcyl,Zcyl,sigrzp)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension Rcyl(NRMAX),Zcyl(NZMAX)
      dimension sigrzp(NRMAX,NZMAX,NPHASE)

      if (ipfcn(2,1).eq.1) then
       do ip=1,NPHASE
        do iz=1,nz
         do ir=1,nr
           sigrzp(ir,iz,ip)=ppars(2,1,ip)
         end do
        end do
       end do
      else if (ipfcn(2,2).eq.1) then
       print *, 'No function yet!'
       stop
      else if (ipfcn(2,3).eq.1) then
       print *, 'No function yet!'
       stop
      else
       print *, 'vdispcalc: error'
       stop
      end if


      return
      end
c==========================================================================

c==========================================================================
      subroutine Tcalc(nr,nz,Rcyl,Zcyl,Trzp)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension Rcyl(NRMAX),Zcyl(NZMAX)
      dimension Trzp(NRMAX,NZMAX,NPHASE)

      if (ipfcn(1,1).eq.1) then
       do ip=1,NPHASE
        do iz=1,nz
         do ir=1,nr
           Trzp(ir,iz,ip)=ppars(1,1,ip)
         end do
        end do
       end do
      else if (ipfcn(1,2).eq.1) then
       print *, 'No function yet!'
       stop
      else if (ipfcn(1,3).eq.1) then
       print *, 'No function yet!'
       stop
      else
       print *, 'Tcalc: error'
       stop
      end if


      return
      end
c==========================================================================


c     ***********************************
c      Gravity calculating routines
c     ***********************************


c==========================================================================
      subroutine gcalc(nr,nz,R,Z,gRrz,gzrz,phirz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"
c     * R in kpc, Z in kpc

      dimension R(NRMAX),Z(NZMAX)
      dimension gRrz(NRMAX,NZMAX),gzrz(NRMAX,NZMAX),phirz(NRMAX,NZMAX)

      dimension gRrzp(NRMAX,NZMAX,NGRAV)
      dimension gzrzp(NRMAX,NZMAX,NGRAV)
      dimension phirzp(NRMAX,NZMAX,NGRAV)


c     ALL NEW GRAVITY FUNCTIONS MUST BE DECLARED
c     EXTERNAL HERE!!!
c     Note that the number of functions must
c     be equal to the maximum size of the
c     array NFCN(phase,functions) in the
c     common block
      external gf11,gf12,gf13
      external gf21,gf22,gf23
      external gf31,gf32,gf33


      do ig=1,NGRAV
        do iz=1,NZMAX
          do ir=1,NRMAX
             gRrzp(ir,iz,ig)=0.
             gzrzp(ir,iz,ig)=0.
             phirzp(ir,iz,ig)=0.
          end do
        end do
      end do

      do iz=1,NZMAX
         do ir=1,NRMAX
            gRrz(ir,iz)=0
            gzrz(ir,iz)=0
            phirz(ir,iz)=0
         end do
      end do


      if (igrav(1).eq.1) then
         if (igfcn(1,1).eq.1) call gf(gf11,nr,nz,R,Z,gRrzp(1,1,1),
     >                         gzrzp(1,1,1),phirzp(1,1,1))
         if (igfcn(1,2).eq.1) call gf(gf12,nr,nz,R,Z,gRrzp(1,1,1),
     >                         gzrzp(1,1,1),phirzp(1,1,1))
         if (igfcn(1,3).eq.1) then
           call gf(gf13,nr,nz,R,Z,gRrzp(1,1,1),
     >                         gzrzp(1,1,1),phirzp(1,1,1))
         end if
      end if


      if (igrav(2).eq.1) then
         if (igfcn(2,1).eq.1) call gf(gf21,nr,nz,R,Z,gRrzp(1,1,2),
     >                         gzrzp(1,1,2),phirzp(1,1,2))
         if (igfcn(2,2).eq.1) call gf(gf22,nr,nz,R,Z,gRrzp(1,1,2),
     >                         gzrzp(1,1,2),phirzp(1,1,2))
         if (igfcn(2,3).eq.1) call gf(gf23,nr,nz,R,Z,gRrzp(1,1,2),
     >                         gzrzp(1,1,2),phirzp(1,1,2))
      end if

      if (igrav(3).eq.1) then
         if (igfcn(3,1).eq.1) call gf(gf31,nr,nz,R,Z,gRrzp(1,1,3),
     >                         gzrzp(1,1,3),phirzp(1,1,3))
         if (igfcn(3,2).eq.1) call gf(gf32,nr,nz,R,Z,gRrzp(1,1,3),
     >                         gzrzp(1,1,3),phirzp(1,1,3))
         if (igfcn(3,3).eq.1) call gf(gf33,nr,nz,R,Z,gRrzp(1,1,3),
     >                         gzrzp(1,1,3),phirzp(1,1,3))
      end if


       do iz=1,nz
        do ir=1,nr
         gRrz(ir,iz)=0.
         gzrz(ir,iz)=0.
         phirz(ir,iz)=0.
         do ig=1,NGRAV
           gRrz(ir,iz)=gRrz(ir,iz)+gRrzp(ir,iz,ig)
           gzrz(ir,iz)=gzrz(ir,iz)+gzrzp(ir,iz,ig)
           phirz(ir,iz)=phirz(ir,iz)+phirzp(ir,iz,ig)
         end do
        end do
       end do


      return
      end
c==========================================================================

c==========================================================================
      subroutine gf(gsub,nr,nz,R,Z,gR,gz,phi)
c==========================================================================
      include "dimen.h"
      dimension R(NRMAX),Z(NZMAX)
      dimension gR(NRMAX,NZMAX)
      dimension gz(NRMAX,NZMAX)
      dimension phi(NRMAX,NZMAX)
      dimension gvec(NGRAV)

      do iz=1,nz
         do ir=1,nr
            call gsub(R(ir),Z(iz),gvec)
            gR(ir,iz)=gR(ir,iz)+gvec(1)
            gz(ir,iz)=gz(ir,iz)+gvec(2)
            phi(ir,iz)=phi(ir,iz)+gvec(3)
         end do
      end do

      return
      end
c==========================================================================



c==========================================================================
      subroutine gf11(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

      parameter (vc=225)
      parameter (UNIT1=1e10/3.09e21)
      parameter (UNIT2=1e10)
      C2=gpars(1,1,1)
      a2=gpars(1,1,2)

      if ((R.eq.0).and.(Z.eq.0)) then
        gvec(1)=0
        gvec(2)=0
      else
      gvec(1)=UNIT1*(C2*vc**2*R/
     >    (a2+sqrt(z*z+R*R))**2/
     >    sqrt(z*z+R*R))
      gvec(2)=UNIT1*(C2*vc**2*z/
     >    (a2+sqrt(z*z+R*R))**2/
     >    sqrt(z*z+R*R))
      end if
      gvec(3)=UNIT2*(C2*vc**2/(a2+sqrt(z*z+R*R)))

      return
      end

c==========================================================================

c==========================================================================
      subroutine gf12(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

      gvec(1)=0.
      gvec(2)=0.
      gvec(3)=0.

      return
      end
c==========================================================================

c==========================================================================
      subroutine gf13(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

      gvec(1)=gpars(1,3,1)
      gvec(2)=gpars(1,3,2)
      gvec(3)=gpars(1,3,3)

      return
      end
c==========================================================================


c==========================================================================
      subroutine gf21(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

c      parameter (C1=8.887)
c      parameter (a1=6.5)
c      parameter (b1=0.26)
      parameter (vc=225)
      parameter (UNIT1=1e10/3.09e21)
      parameter (UNIT2=1e10)

      C1=gpars(2,1,1)
      a1=gpars(2,1,2)
      b1=gpars(2,1,3)

      gvec(1)=UNIT1*(C1*vc**2*R/
     >    (R*R+(a1+sqrt(z*z+b1*b1))**2)**(1.5))

      gvec(2)=UNIT1*(C1*vc**2*z*(1+a1/sqrt(z*z+b1*b1))/
     >    (R*R+(a1+sqrt(z*z+b1*b1))**2)**(1.5))

      gvec(3)=UNIT2*(C1*vc**2/sqrt(R**2+(a1+sqrt(z*z+b1*b1))**2))

      return
      end

c==========================================================================
c==========================================================================
      subroutine gf22(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      dimension gvec(NGRAV)



c     Calculate rotational term...
      if (R.lt.3) then
         vrot=(R/3)*165
         Gsh=0
         print *, 'Probable error in Ferr formula'
         stop

      else if ((R.gt.3).and.(R.lt.5)) then
         vrot=27.5*R+82.5
         vrot=vrot
         Gsh=-82.5
         print *, 'Probable error in Ferr formula'
         stop
      else
         vrot=220
         Gsh=-220
      end if
      if (R.ne.0) then
         om=(vrot*1e5)/(R*1000*PC1)
         Gsh=(Gsh*1e5)/(R*1000*PC1)
      else
         om=0
         Gsh=0
      end if

c     set radial component and phi = 0
      gvec(1)=0.
      gvec(2)=4.4e-9*exp(-(R-RSUN)/4.9)*Z/sqrt(Z*Z+0.2*0.2)
      gvec(3)=0.

c     subtract off rotation term that I don't understand...
      gvec(2)=gvec(2)-2*om*(om+Gsh)*(Z*1000*PC1)

      return
      end
c==========================================================================


c==========================================================================
      subroutine gf23(R,Z,gvec)
c==========================================================================
c     No radial dependence...
c     ONLY CALCULATES VERTICAL G_Z
c     formula from lockman and gehman apj 1991, 382, 182

      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

      zpc=z*1000.

      D=180.
      E=9.75e-3
      F=2.25e-6
      gkg=2*PI*GRCON*zpc*(E/sqrt(zpc*zpc+D*D)+2*F)
      phikg=2*PI*GRCON*(E*sqrt(zpc*zpc+D*D)+F*zpc*zpc)*PC1


      gvec(1)=0
      gvec(2)=gkg
      gvec(3)=phikg

      return
      end
c==========================================================================


c==========================================================================
      subroutine gf31(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

      parameter (vc=225)

      parameter (UNIT1=1e10/3.09e21)
      parameter (UNIT2=1e10)
      C3=gpars(3,1,1)
      a3=gpars(3,1,2)
      rh=gpars(3,1,3)

      gvec(1)=UNIT1*(2*C3*vc*vc*R/
     >    (a3*a3+R*R+z*z))

      gvec(2)=UNIT1*(2*C3*vc*vc*z/
     >    (a3*a3+R*R+z*z))

      gvec(3)=UNIT2*(C3*vc*vc*log(a3*a3+R*R+z*z))

c     For very large distances replace gvec(3) with
c     below expression...
      top=sqrt(1+(a3*a3+R*R+z*z)/(rh*rh))-1
      bot=sqrt(1+(a3*a3+R*R+z*z)/(rh*rh))+1
      phi3mod=C3*vc*vc*log(top/bot)

      return
      end
c==========================================================================



c==========================================================================
      subroutine gf32(R,Z,gvec)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      dimension gvec(NGRAV)

c     Calculate rotational term...
      if (R.lt.3) then
         vrot=(R/3)*165
         Gsh=0
         print *, 'Probable error in Ferr formula'
         stop

      else if ((R.gt.3).and.(R.lt.5)) then
         vrot=27.5*R+82.5
         vrot=vrot
         Gsh=-82.5
         print *, 'Probable error in Ferr formula'
         stop

      else
         vrot=220
         Gsh=-220
      end if
      if (R.ne.0) then
         om=(vrot*1e5)/(R*1000*PC1)
         Gsh=(Gsh*1e5)/(R*1000*PC1)
      else
         om=0
         Gsh=0
      end if

      gvec(1)=0
      gvec(2)=1.7e-9*(RSUN*RSUN+2.2*2.2)/(R*R+2.2*2.2)*Z
      gvec(3)=0.

c     subtract off rotation term that I don't understand...
      gvec(2)=gvec(2)-2*om*(om+Gsh)*(Z*1000*PC1)

      return
      end
c==========================================================================


c==========================================================================
      subroutine gf33(Rin,Zin,gvecout)
c==========================================================================
c     This routine must be run in double precision...
      include "dimen.h"
      include "galstruct.h"
      dimension gvecout(NGRAV)

      double precision R, Z
      double precision UNIT1,UNIT2
      double precision q,RC,vH
      double precision a,b,c,h,gamm,rmu,rnu
      double precision gvec(NGRAV)
      double precision rtil,rsq

      R=DBLE(Rin)
      Z=DBLE(Zin)

      UNIT1=1e10/3.09e21
      UNIT2=1e10


      q=dble(gpars(3,3,1))
      RC=DBLE(gpars(3,3,2))

c     Rotation speed set to match gR at solar location
      vH=DBLE(gpars(3,3,3))


c     Core radius of 5 set by using BRC gravity
      RC=5.d0

c     Rotation speed set to match gR at solar location
      vH=168.1d0



c     If flattened halo...
      if (q.ne.1.0) then

      if (zin.eq.0) z=0.0001


      a=(1-q*q)*rc*rc
      b=z*z+R*R+(1-q*q)*rc*rc
      c=z*z
      if ((b*b-4*a*c).gt.0) then
        h=sqrt(b*b-4*a*c)
      else
        print *, 'Bad h in gravsac'
      end if
      gamm=sqrt(1.-q*q)/q
      rmu=sqrt(2*c/(b+h))

c     Have to break gravity into different cases...
      if ((R.eq.0).and.(z.eq.0)) then
       gvec(1)=0.
       gvec(2)=0.
      else if (R.eq.0) then
       rnu=sqrt(2*c/(b-h))
       gvec(1)=0.
       gvec(2)=-vH*vH*z*gamm/(h*atan(gamm))*
     >      ( atan(gamm*rmu)/(gamm*rmu)-
     >        atan(gamm*rnu)/(gamm*rnu) )
      else
       rnu=sqrt(2*c/(b-h))
       gvec(2)=-vH*vH*z*gamm/(h*atan(gamm))*
     >      ( atan(gamm*rmu)/(gamm*rmu)-
     >        atan(gamm*rnu)/(gamm*rnu) )


       gvec(1)=-vH*vH*R*gamm/(h*atan(gamm))*
     >       (rmu*rmu/(rmu*rmu-1)*
     >          (atan(gamm*rmu)/(gamm*rmu)-
     >           atan(gamm)/(gamm))
     >       -rnu*rnu/(rnu*rnu-1)*
     >          (atan(gamm*rnu)/(gamm*rnu)-
     >           atan(gamm)/(gamm))        )


      end if

c     formula on phi comes from integrating
c     formula (5) in Sackett et al 1994 ApJ 436,629
c     Set = to zero for time being...

      gvec(3)=0


c     if round halo....
      else

      rsq=R*R+z*z
      rtil=sqrt(rsq)/rc
      if (rsq.ne.0) then
       gvec(1)=-vH*vH*R/rsq*(1-atan(rtil)/rtil)
       gvec(2)=-vH*vH*z/rsq*(1-atan(rtil)/rtil)
       gvec(3)=0.5*vH*vH*(log(rtil*rtil+1)-2+2*atan(rtil)/rtil)
      else
       gvec(1)=0
       gvec(2)=0
       gvec(3)=0
      end if

      end if

c     ----------------------
c     Set to the right units
c     ----------------------


      gvecout(1)=real(-gvec(1)*UNIT1)
      gvecout(2)=real(-gvec(2)*UNIT1)
      gvecout(3)=real(gvec(3)*UNIT2)

      return
      end
c==========================================================================




c==========================================================================
      subroutine rpcalc(nr,nz,R,Z,abrzZ,radrz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      include "atomic.h"

      dimension R(NRMAX),Z(NZMAX)
      dimension radrz(NRMAX,NZMAX)
      dimension abrzZ(NRMAX,NZMAX,NEL)

c     Parameters for both rad pressures
      parameter(ALPHA=-0.15)
c      parameter(OPFAC=2.9e-8)
      OPFAC=rppars(1,1)

c     parameters for thick disk
c      parameter(B=1)
      B=rppars(1,2)
c      parameter(QpF=1e-2)
      QpF=rppars(1,3)
c      parameter(H_STAR=0.150)
      H_STAR=rppars(1,4)
c      parameter(R_STAR=6.)
      R_STAR=rppars(1,5)

c     parameters for thin disk
c      parameter(Qp=1.026)
      Qp=rppars(2,2)
c      parameter(galL=9.9e6)
      galL=rppars(2,3)
c      parameter(G0=0.25)
      G0=rppars(2,4)
c      parameter(GSCL=3.5)
      GSCL=rppars(2,5)
c      parameter(ZCUT=0.03)
      ZCUT=rppars(2,6)

c     Sum each of the radiation pressure components into
c     the following array...


      do iz=1,NZMAX
         do ir=1,NRMAX
            radrz(ir,iz)=0.
         end do
      end do

      if (irp(1).ne.0) then
      solab=abund(indexel(8))
      if (solab.eq.0) then
         print *, 'Oxygen abundance must be non zero to calculate'
         print *, 'radiation pressure (due to metallicity depend)'
         stop
      end if
      do iz=1,nz
         ztil=Z(iz)/H_STAR
         do ir=1,nr
           gamf=(1-tanh(ztil))/(B+tanh(ztil))
           abfac=abrzZ(ir,iz,8)/solab
c           AA=OPFAC*exp(ALPHA*(R(ir)-RSUN))
           AA=OPFAC*abfac
           radrz(ir,iz)=radrz(ir,iz)-
     >                  AA*QpF*exp((RSUN-R(ir))/R_STAR)*
     >                  (1-gamf)/(1+gamf)
         end do
      end do
      end if

      if (irp(2).ne.0) then
      solab=abund(indexel(8))
      if (solab.eq.0) then
         print *, 'Oxygen abundance must be non zero to calculate'
         print *, 'radiation pressure (due to metallicity depend)'
         stop
      end if
      do iz=1,nz
         Zval=max(ZCUT,Z(iz))
         do ir=1,nr
           abfac=abrzZ(ir,iz,8)/solab
c           AA=OPFAC*exp(ALPHA*(R(ir)-RSUN))
           AA=OPFAC*abfac
           dim=sunL/PC1/PC1/1000./1000.
           term=-AA*Qp*dim*galL/(4*PI*Zval*Zval)*
     >         (G0*exp(-(R(ir)-4.0)/GSCL))
           radrz(ir,iz)=radrz(ir,iz)+term
         end do
      end do
      end if

      if (irp(3).ne.0) then
         print *, 'No choice for third option in rad pres yet!'
         stop
      end if


      return
      end
c==========================================================================

c==========================================================================
      subroutine vrotcalc(nr,nz,R,Z,gRrz,vrotrz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      dimension R(NRMAX),Z(NZMAX)
      dimension gRrz(NRMAX,NZMAX)
      dimension vrotrz(NRMAX,NZMAX)

      do iz=1,NZMAX
         do ir=1,NRMAX
           vrotrz(ir,iz)=0
         end do
      end do


      do iz=1,nz
         do ir=1,nr
            vrotrz(ir,iz)=sqrt(gRrz(ir,iz)*R(ir)*PC1*1000)/1e5
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine vtrmcalc(nr,nz,gzrz,dHrz,vtrmrz)
c==========================================================================
      include "dimen.h"
      dimension gzrz(NRMAX,NZMAX),dHrz(NRMAX,NZMAX)
      dimension vtrmrz(NRMAX,NZMAX)
      parameter(COLDEN=1e19)
      parameter(CD=1.)
      parameter(VESC=500)

      do iz=1,NZMAX
         do ir=1,NRMAX
            vtrmrz(ir,iz)=0.
         end do
      end do


      do iz=1,nz
         do ir=1,nr
            if (dHrz(ir,iz).ne.0) then
            vtrmrz(ir,iz)=sqrt(2*COLDEN*gzrz(ir,iz)/CD/dHrz(ir,iz))/1e5
            else
            vtrmrz(ir,iz)=VESC
            end if

            vtrmrz(ir,iz)=min(VESC,vtrmrz(ir,iz))
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine zinteg(nr,nz,R,Z,Alid,A,Aint)
c==========================================================================
      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      include "atomic.h"

      dimension A(NRMAX,NZMAX)
      dimension R(NRMAX),Z(NZMAX)
      dimension Aint(NRMAX,NZMAX)
      dimension Alid(NRMAX)


      print *, 'BEWARE!!!!'
      print *, 'zintegration has no convergence check!'
      print *, 'or boundary check. Must do by hand!'

      do ir=1,NRMAX
         do iz=1,NZMAX
            Aint(ir,iz)=0
         end do
      end do

      do ir=1,nr
         Aint(ir,nz)=Alid(ir)
         do iz=nz-1,1,-1
               dz=0.5*(z(iz+1)-z(iz))
               wgt=dz*A(ir,iz)+
     >               dz*A(ir,iz+1)
            Aint(ir,iz)=Aint(ir,iz+1)+wgt
         end do
      end do

      do ir=1,nr
         do iz=1,nz
            Aint(ir,iz)=Aint(ir,iz)*(PC1*1000.)
         end do
      end do

      return
      end
c==========================================================================




c==========================================================================
      subroutine pwtcalc(nr,nz,prz,wtrz,pdiff)
c==========================================================================
      include "dimen.h"
      include "const.h"

      dimension prz(NRMAX,NZMAX)
      dimension wtrz(NRMAX,NZMAX)
      dimension pdiff(NRMAX,NZMAX)

      do iz=1,nz
         do ir=1,nr
            if (wtrz(ir,iz).ne.0) then
               pdiff(ir,iz)=100*(prz(ir,iz)-wtrz(ir,iz))/wtrz(ir,iz)
            else
               pdiff(ir,iz)=0.
            end if
         end do
      end do

      return
      end
c==========================================================================
c==========================================================================
      subroutine pdefex(nr,nz,prz,wtrz,pdef)
c==========================================================================
      include "dimen.h"
      include "const.h"

      dimension prz(NRMAX,NZMAX)
      dimension wtrz(NRMAX,NZMAX)
      dimension pdef(NRMAX,NZMAX)

      do iz=1,NZMAX
         do ir=1,NRMAX
            pdef(ir,iz)=0
         end do
      end do

      do iz=1,nz
         do ir=1,nr
            if (wtrz(ir,iz).ne.0) then
               pdef(ir,iz)=(prz(ir,iz)-wtrz(ir,iz))/BOLTZK
            else
               pdef(ir,iz)=0.
            end if
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine ddR(nr,nz,R,Z,A,dAdR)
c==========================================================================
c     Units are in input units/kpc
      include "dimen.h"
      include "const.h"
      dimension R(NRMAX)
      dimension Z(NZMAX)
      dimension A(NRMAX,NZMAX),dAdR(NRMAX,NZMAX)


      do iz=1,NZMAX
         do ir=1,NRMAX
            dAdR(ir,iz)=0.
         end do
      end do

c     Calculate second order derivative
c     Derivatives at boundaries set equal
c     to derivative one zone in

      do iz=1,nz
         do ir=2,nr-1
            dAdr(ir,iz)=0.5*((A(ir+1,iz)-A(ir,iz))/
     >                       (R(ir+1)-R(ir))) +
     >                  0.5*((A(ir,iz)-A(ir-1,iz))/
     >                       (R(ir)-R(ir-1)))
         end do
      end do

      do iz=1,nz
         dAdr(1,iz)=dAdr(2,iz)
         dAdr(nr,iz)=dAdr(nr-1,iz)
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine ddz(nr,nz,R,Z,A,dAdz)
c==========================================================================
c     Units are in input units/kpc
      include "dimen.h"
      include "const.h"
      dimension R(NRMAX)
      dimension Z(NZMAX)
      dimension A(NRMAX,NZMAX),dAdz(NRMAX,NZMAX)

      do iz=1,NZMAX
         do ir=1,NRMAX
            dAdz(ir,iz)=0.
         end do
      end do


c     Calculate second order derivative
c     Derivatives at boundaries set equal
c     to derivative one zone in

      do ir=1,nr
         do iz=2,nz-1
            dAdz(ir,iz)=0.5*((A(ir,iz+1)-A(ir,iz))/
     >                       (Z(iz+1)-Z(iz))) +
     >                  0.5*((A(ir,iz)-A(ir,iz-1))/
     >                       (Z(iz)-Z(iz-1)))
         end do
      end do

      do ir=1,nr
         dAdz(ir,1)=dAdz(ir,2)
         dAdz(ir,nz)=dAdz(ir,nz-1)
      end do

      return
      end
c==========================================================================


c==========================================================================
      subroutine prtblk(nr,nz,r,z,A,outfile)
c==========================================================================
      include "dimen.h"
      include "const.h"
      dimension R(NRMAX),Z(NZMAX)
      dimension A(NRMAX,NZMAX)

      character*40 outfile

      nun=40
      open(unit=nun,file=outfile,status='unknown')

      write(nun,*) nr,nz
      do i=1,nr
         write(nun,*) R(i)
      end do
      do i=1,nz
         write(nun,*) Z(i)
      end do

      do j=1,nz
         do i=1,nr
            write(nun,*) a(i,j)
         end do
      end do

      close(unit=nun)
      return
      end
c==========================================================================


c==========================================================================
      subroutine prtslice(irz,val,nr,nz,R,Z,frz,outfile)
c==========================================================================

      include "dimen.h"
      include "const.h"
      dimension Z(NZMAX),R(NRMAX)
      dimension frz(NRMAX,NZMAX)
      character*40 outfile

      nun=40
      open(unit=nun,file=outfile,status='unknown')

      if (irz.eq.1) then
         call hunt(R,nr,val,islice)
         if ((islice.eq.0).or.(islice.eq.nr)) then
            print *, 'Out of bounds in prtslice'
            print *, 'val=',val,'R(1)=',R(1),'R(nr)=',R(nr)
            print *, 'irz=1 : islice=',islice
            stop
         end if
         dlo=abs(R(islice)-val)
         dhi=abs(R(islice+1)-val)
      else if (irz.eq.2) then
         call hunt(Z,nz,val,islice)
         if ((islice.eq.0).or.(islice.eq.nz)) then
            print *, 'Out of bounds in prtslice'
            print *, 'val=',val
            print *, 'val=',val,'Z(1)=',Z(1),'Z(nz)=',Z(nz)
            print *, 'irz=2 : islice=',islice
            stop
         end if
         dlo=abs(Z(islice)-val)
         dhi=abs(Z(islice+1)-val)
      else
         print *, 'bad irz in prtslice'
         stop
      end if

c     choose closer of two grid points
      if (dlo.lt.dhi) then
         islice=islice
      else
         islice=islice+1
      end if

      if (irz.eq.1) then
         write(nun,1001) R(islice)
         do i=1,nz
            write(nun,1003) Z(i),frz(islice,i)
         end do
 1001    format('Slice at R= ',f9.3,' kpc')
 1003    format(f9.3,2x,1pe12.3)
      else
         write(nun,2001) Z(islice)
         do i=1,nr
            write(nun,1003) R(i),frz(i,islice)
         end do
 2001    format('Slice at Z= ',f9.3,' kpc')
      end if

      close(unit=nun)

      return
      end
c==========================================================================

c==========================================================================
      subroutine grzmult(nr,nz,Arz,Brz,Crz)
c==========================================================================
      include "dimen.h"
      dimension Arz(NRMAX,NZMAX), Brz(NRMAX,NZMAX)
      dimension Crz(NRMAX,NZMAX)

      do iz=1,nz
         do ir=1,nr
            Crz(ir,iz)=Arz(ir,iz)*Brz(ir,iz)
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine scmult(nr,nz,fac,Arz,Brz)
c==========================================================================
      include "dimen.h"
      dimension Arz(NRMAX,NZMAX),Brz(NRMAX,NZMAX)

      do iz=1,nz
         do ir=1,nr
            Brz(ir,iz)=fac*Arz(ir,iz)
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine grzsum(nr,nz,Arz,Brz,Crz)
c==========================================================================
      include "dimen.h"
      dimension Arz(NRMAX,NZMAX), Brz(NRMAX,NZMAX)
      dimension Crz(NRMAX,NZMAX)

      do iz=1,nz
         do ir=1,nr
            Crz(ir,iz)=Arz(ir,iz)+Brz(ir,iz)
         end do
      end do

      return
      end
c==========================================================================

c==========================================================================
      subroutine grzlid(ilid,abfile,zmin,ztop,nr,Rcyl,Alid)
c==========================================================================
c     integrate quantity corresponding to parameter ilid
c     from zmin to ztop (infinity if ztop=0)
c     the resultant integral is placed in the array Alid
c     which includes all R points...
c     ilid=1: H particle density
c     ilid=2: denH*g=(rho/mean molecular weight)
c
      include "dimen.h"
      include "const.h"
      include "atomic.h"
      include "galstruct.h"
      dimension Rcyl(NRMAX)
      dimension Alid(NRMAX)
      character*40 abfile
c     control maximum number of z points/iteration
c     if NZ2MAX=2048, NZ2IT=3 --> NZINIT=128
c                           4 --> NZINIT=64
      parameter(NZ2MAX=2048,NZ2IT=4)
      parameter(NZINIT=NZ2MAX/(2**(NZ2IT+1)))

c     control number of grid doublings....
      parameter(NTOP=5)

c     converged answer to within 10%
      parameter(CONV=0.1)

      dimension Z2a(NZMAX),A2a(NRMAX,NZMAX)
      dimension Z2b(NZMAX),A2b(NRMAX,NZMAX)
      dimension A2ap(NRMAX,NZMAX,NPHASE),A2bp(NRMAX,NZMAX,NPHASE)

      dimension rh(NRMAX,NZMAX),gz(NRMAX,NZMAX)
      dimension gunk1(NRMAX,NZMAX),gunk2(NRMAX,NZMAX)

      dimension radz(NRMAX,NZMAX)
      dimension abrzZ(NRMAX,NZMAX,NEL)

      dimension suma(NRMAX),sumb(NRMAX)
      dimension sum1(NRMAX),sum2(NRMAX)
      dimension sumpd(NRMAX)

c---------------------------------------------------------------------------
      ndum=NZ2MAX
      if (NZMAX.lt.ndum) then
         print *, 'NZ2MAX must be less than NZMAX!'
         stop
      end if

c     Start with number of z point equal to 100.
c     If doesn't converge, double each time.
      nza=NZINIT
      nzb=2*NZINIT

c     -------------------------
c     Set up integration
c     boundaries, and step size
c     -------------------------
      itop=1

 500  if (ztop.eq.0) then
         zmax=zmin*2**(itop)
      else
         zmax=ztop
      end if

      iiter=0
c     -------------------------
c     Resolution loop
c     -------------------------
 400  continue
      iiter=iiter+1

c     -----------------------------------
c     Process A array (lower resolution...)
c     -----------------------------------
      if (iiter.eq.1) then

      dza=(zmax-zmin)/(nza-1)

      do iz=1,nza
        Z2a(iz)=zmin+(iz-1)*dza
      end do

c     Don't forget to add new ilid choices to
c     the other call...
      if (ilid.eq.1) then
        call dHcalc(nr,nza,Rcyl,Z2a,A2a,A2ap)
      else if (ilid.eq.2) then
        call dHcalc(nr,nza,Rcyl,Z2a,rh,A2ap)
        call gcalc(nr,nza,Rcyl,Z2a,gunk1,gz,gunk2)
        call abcalc(nr,nza,Rcyl,Z2a,abrzZ,abfile)
        call rpcalc(nr,nza,Rcyl,Z2a,abrzZ,radz)
        call grzsum(nr,nz,gz,radz,gz)

        call grzmult(nr,nza,rh,gz,A2a)
      else
        print *, 'Bad ilid'
        stop
      end if

      do ir=1,nr
         suma(ir)=0
         do iz=nza-1,1,-1
               suma(ir)=suma(ir)+
     >                  0.5*dza*(A2a(ir,iz)+A2a(ir,iz+1))
         end do
      end do


      end if


c     -----------------------------------
c     Process B array (higher resolution)
c     -----------------------------------
      dzb=(zmax-zmin)/(nzb-1)

      do iz=1,nzb
        Z2b(iz)=zmin+(iz-1)*dzb
      end do

c     Don't forget to add new ilid choices to
c     the other call...
      if (ilid.eq.1) then
        call dHcalc(nr,nzb,Rcyl,Z2b,A2b,A2bp)
      else if (ilid.eq.2) then
        call dHcalc(nr,nzb,Rcyl,Z2b,rh,A2bp)
        call gcalc(nr,nzb,Rcyl,Z2b,gunk1,gz,gunk2)
        call abcalc(nr,nzb,Rcyl,Z2b,abrzZ,abfile)
        call rpcalc(nr,nzb,Rcyl,Z2b,abrzZ,radz)
        call grzsum(nr,nz,gz,radz,gz)

        call grzmult(nr,nzb,rh,gz,A2b)
      else
        print *, 'Bad ilid'
        stop
      end if

      do ir=1,nr
         sumb(ir)=0
         do iz=nzb-1,1,-1
               sumb(ir)=sumb(ir)+
     >                  0.5*dzb*(A2b(ir,iz)+A2b(ir,iz+1))
         end do
      end do

c     -----------------------------------
c     Compare results of two arrays....
c     -----------------------------------
      summax=0
      do ir=1,nr
         sumpd(ir)=abs( (sumb(ir)-suma(ir))/suma(ir))
         summax=max(summax,sumpd(ir))
      end do

c     -----------------------------------
c     if not converged over the interval,
c     move array b to array a,
c     double number of steps and try again...
c     -----------------------------------
      if ((summax.gt.CONV).and.(iiter.lt.(NZ2IT-1))) then
         print *, 'increasing resolution x 2'
         do ir=1,nr
            suma(ir)=sumb(ir)
         end do

         nzb=2*nzb
         go to 400
      end if
c     -----------------------------------------------------------



      if (iiter.eq.NZ2IT-1) then
         print *, 'Reached max no of iterations'
         print *, 'grzlid:  iiter>NZ2IT'
         stop
      end if

      do ir=1,nr
         Alid(ir)=sumb(ir)
      end do
c----------------------------------------------------------------

c     ----------------------------------------------
c     For loose upper boundary condition, double the upper
c     distance and see if you get the same answer
c     ----------------------------------------------
      if (ztop.eq.0) then

        if (itop.eq.1) then
c         store sum and do the calculation a second time...
          print *, 'doubling height to check for convergence'
          do ir=1,nr
             sum1(ir)=sumb(ir)
          end do
          itop=itop+1
c         since we are doubling the size, double # of steps
c         to get same resolution
          nza=2*nzb
          nzb=2*nza
          go to 500
        else
c   store sum to sum2 and compare results of calculation

          do ir=1,nr
             sum2(ir)=sumb(ir)
          end do

          summax=0
          do ir=1,nr
            sumpd(ir)=abs( (sum2(ir)-sum1(ir))/sum1(ir))
            summax=max(summax,sumpd(ir))
          end do

          if ((summax.gt.CONV).and.(itop.lt.NTOP)) then
             print *, 'not converged: doubling height'
             do ir=1,nr
                sum1(ir)=sum2(ir)
             end do
             itop=itop+1
             nza=2*nzb
             nzb=2*nza
             go to 500
          end if
        end if


c       ------------------------------
c       convert to quantity integrated
c       over pc rather than cm
c       ------------------------------
        if (ilid.eq.2) then
           fac=RHO0*(PC1*1000)
        else
           fac=(PC1*1000)
        end if

        do ir=1,nr
          Alid(ir)=fac*sumb(ir)
        end do

      end if

      if (itop.eq.NTOP) then
         print *, 'Too many grid doublings'
         print *, 'grzlid: itop > NTOP'
         stop
      end if

      return
      end
c==========================================================================

c==========================================================================
      SUBROUTINE HUNT(XX,N,X,JLO)
c==========================================================================
      DIMENSION XX(N)
      LOGICAL ASCND
      ASCND=XX(N).GT.XX(1)
      IF(JLO.LE.0.OR.JLO.GT.N)THEN
        JLO=0
        JHI=N+1
        GO TO 3
      ENDIF
      INC=1
      IF(X.GE.XX(JLO).EQV.ASCND)THEN
1       JHI=JLO+INC
        IF(JHI.GT.N)THEN
          JHI=N+1
        ELSE IF(X.GE.XX(JHI).EQV.ASCND)THEN
          JLO=JHI
          INC=INC+INC
          GO TO 1
        ENDIF
      ELSE
        JHI=JLO
2       JLO=JHI-INC
        IF(JLO.LT.1)THEN
          JLO=0
        ELSE IF(X.LT.XX(JLO).EQV.ASCND)THEN
          JHI=JLO
          INC=INC+INC
          GO TO 2
        ENDIF
      ENDIF
3     IF(JHI-JLO.EQ.1)RETURN
      JM=(JHI+JLO)/2
      IF(X.GT.XX(JM).EQV.ASCND)THEN
        JLO=JM
      ELSE
        JHI=JM
      ENDIF
      GO TO 3
      END
c==========================================================================

c==========================================================================
      subroutine outstuff(ioutt,ipar2,nr,nz,outrz,
     >                    dHrz,     prz  ,wtrz,
     >                    gzrz, gRrz,phirz,vrrz,
     >                    vrprz,pwtrz,abrzZ,dHrzp,
     >                    ffrzp,sigrzp,Trzp,przp,
     >                    radrz,Bavgrz)
c==========================================================================

      include "dimen.h"
      include "const.h"
      include "galstruct.h"
      include "atomic.h"

c      '------------------------------------------------'
c      '| 1=n_H     : 2=rho     : 3=p     : 4=wt        '
c      '| 5=g_z     : 6=g_R     : 7=phi   : 8=vr,no p  '
c      '| 9=vr,w p  :10=p_def    :11=ab(Z) :12=n_H(phase)'
c      '|13=ff(pha) :14=sig(pha):15=T(pha):16=rad pot   '
c      '------------------------------------------------'

      dimension outrz(NRMAX,NZMAX)

c     blocks of possible output...
      dimension dHrz(NRMAX,NZMAX)
      dimension rhorz(NRMAX,NZMAX)
      dimension prz(NRMAX,NZMAX)
      dimension wtrz(NRMAX,NZMAX)

      dimension gzrz(NRMAX,NZMAX)
      dimension gRrz(NRMAX,NZMAX)
      dimension phirz(NRMAX,NZMAX)
      dimension vrrz(NRMAX,NZMAX)

      dimension vrprz(NRMAX,NZMAX)
      dimension pwtrz(NRMAX,NZMAX)
      dimension abrzZ(NRMAX,NZMAX,NEL)
      dimension dHrzp(NRMAX,NZMAX,NPHASE)

      dimension ffrzp(NRMAX,NZMAX,NPHASE)
      dimension sigrzp(NRMAX,NZMAX,NPHASE)
      dimension Trzp(NRMAX,NZMAX,NPHASE)
      dimension przp(NRMAX,NZMAX,NFORCE)

      dimension radrz(NRMAX,NZMAX)
      dimension Bavgrz(NRMAX,NZMAX)


      if (ioutt.eq.1) then
         call stuff(nr,nz,outrz,dHrz)
      else if (ioutt.eq.2) then
         do iz=1,nz
            do ir=1,nr
               rhorz(ir,iz)=RHO0*dHrz(ir,iz)
            end do
         end do
         call stuff(nr,nz,outrz,rhorz)
      else if (ioutt.eq.3) then
         call stuff(nr,nz,outrz,prz)
      else if (ioutt.eq.4) then
         call stuff(nr,nz,outrz,wtrz)

      else if (ioutt.eq.5) then
         call stuff(nr,nz,outrz,gzrz)
      else if (ioutt.eq.6) then
         call stuff(nr,nz,outrz,gRrz)
      else if (ioutt.eq.7) then
         call stuff(nr,nz,outrz,phirz)
      else if (ioutt.eq.8) then
         call stuff(nr,nz,outrz,vrrz)

      else if (ioutt.eq.9) then
         call stuff(nr,nz,outrz,vrprz)
      else if (ioutt.eq.10) then
         call stuff(nr,nz,outrz,pwtrz)
      else if (ioutt.eq.11) then
         call stuff(nr,nz,outrz,abrzZ(1,1,ipar2))
      else if (ioutt.eq.12) then
         call stuff(nr,nz,outrz,dHrzp(1,1,ipar2))
      else if (ioutt.eq.13) then
         call stuff(nr,nz,outrz,ffrzp(1,1,ipar2))
      else if (ioutt.eq.14) then
         call stuff(nr,nz,outrz,sigrzp(1,1,ipar2))
      else if (ioutt.eq.15) then
         call stuff(nr,nz,outrz,Trzp(1,1,ipar2))
      else if (ioutt.eq.16) then
         call stuff(nr,nz,outrz,przp(1,1,ipar2))
      else if (ioutt.eq.17) then
         call stuff(nr,nz,outrz,radrz)
      else if (ioutt.eq.18) then
         call stuff(nr,nz,outrz,Bavgrz)
      else
         print *, 'Bad ioutt'
         stop
      end if


      return
      end
c==========================================================================

c==========================================================================
      subroutine stuff(nr,nz,outrz,Xrz)
c==========================================================================
      include "dimen.h"
      include "const.h"
      dimension outrz(NRMAX,NZMAX)
      dimension Xrz(NRMAX,NZMAX)
      do iz=1,nz
         do ir=1,nr
            outrz(ir,iz)=Xrz(ir,iz)
         end do
      end do

      return
      end
c==========================================================================


      SUBROUTINE INDEXX(N,ARRIN,INDX)
      DIMENSION ARRIN(N),INDX(N)
      DO 11 J=1,N
        INDX(J)=J
11    CONTINUE
      L=N/2+1
      IR=N
10    CONTINUE
        IF(L.GT.1)THEN
          L=L-1
          INDXT=INDX(L)
          Q=ARRIN(INDXT)
        ELSE
          INDXT=INDX(IR)
          Q=ARRIN(INDXT)
          INDX(IR)=INDX(1)
          IR=IR-1
          IF(IR.EQ.1)THEN
            INDX(1)=INDXT
            RETURN
          ENDIF
        ENDIF
        I=L
        J=L+L
20      IF(J.LE.IR)THEN
          IF(J.LT.IR)THEN
            IF(ARRIN(INDX(J)).LT.ARRIN(INDX(J+1)))J=J+1
          ENDIF
          IF(Q.LT.ARRIN(INDX(J)))THEN
            INDX(I)=INDX(J)
            I=J
            J=J+J
          ELSE
            J=IR+1
          ENDIF
        GO TO 20
        ENDIF
        INDX(I)=INDXT
      GO TO 10
      END
