c
      subroutine profile(itype,NPROF_PT,X,Y,Z,xprof,yprof)
c     Given coordinates (X,Y,Z) of pt in galaxy profile returns
c     two 1D arrays, xprof, yprof with NPROF_PT values each.
c     itype=1 gives xprof=distance from sun, yprof=density
c     itype=2 will give xprof=velocity, yprof=emission measure


cRAB 9/7/99
c     Modified to give intensity vs velocity if you call it with
c     itype=2.

      parameter(XSUN=0.,YSUN=8.5,ZSUN=0.)
      parameter(NPTMAX=400)
      dimension xprof(*),yprof(*)

      dimension sI(NPTMAX),sV(NPTMAX)

      parameter(vFWHM=10.)
      parameter(t4=0.8)

      if (NPROF_PT.gt.NPTMAX) then
         print *, 'Increase NPROF_PT in call to profile'
         stop
      end if


      do is=1,NPROF_PT
	 xprof(is)=0.
	 yprof(is)=0.
      end do

      if (itype .eq.1) then
       ds=1./(NPROF_PT-1.)
       do is=1,NPROF_PT
         s=(is-1)*ds
         xs=XSUN+(X-XSUN)*s
	 ys=YSUN+(Y-YSUN)*s
         zs=ZSUN+(Z-ZSUN)*s
         call tcdensity(xs,ys,zs,dne1,dne2,dnea,dnegum)
         xprof(is)=sqrt(xs*xs+ys*ys+zs*zs)
         yprof(is)=dne1+dne2+dnea
         write(7,*) xprof(is),yprof(is)
       end do
      else if (itype.eq.2) then

c     Calculate the density squared (sI) and
c     line-of-sight velocity (sV)
       ds=1./(NPTMAX-1)
       sVmin=1e20
       sVmax=-1e20
       do is=1,NPTMAX
         s=(is-1)*ds
         xs=XSUN+(X-XSUN)*s
	 ys=YSUN+(Y-YSUN)*s
         zs=ZSUN+(Z-ZSUN)*s
         call tcdensity(xs,ys,zs,dne1,dne2,dnea,dnegum)
         sI(is)=(dne1+dne2+dnea+dnegum)**2.
         call rotv(xs,ys,zs,vxs,vys,vzs)
         sV(is)=vlos_fcn(xs,ys,zs,vxs,vys,vzs)
         sVmin=min(sVmin,sV(is))
         sVmax=max(sVmax,sV(is))
       end do

       sVmin=sVmin-1.5*vFWHM
       sVmax=sVmax+1.5*vFWHM


       nv=NPROF_PT
       dv=(sVmax-sVmin)/(nv-1)
       dL=1000.*ds

       do iV=1,nv
          V=sVmin+(iV-1)*dV
          xprof(iV)=V
          yprof(iV)=0
          do is=1,NPTMAX
             fac=(V-sV(is))*(V-sV(is))/vFWHM/vFWHM
             if (fac.lt.20) then
             yprof(iV)=yprof(iV)+sI(is)*
     >                 exp(-fac)
             else
             yprof(iV)=yprof(iV)
             end if
          end do
          yprof(iV)=yprof(iV)*dL/2.75/(t4**0.9)
       end do


      else
         print *, 'bad itype in profile'
         stop
      end if

c      print *, '--------------------------------'
c      write(6,6000) itype
c      write(6,6001) xprof(1),xprof(NPROF_PT),xprof(2)-xprof(1)
c      write(6,6002) yprof(1),yprof(NPROF_PT)
c6000  format('Profile type: ', i3)
c6001  format('X: ',3f8.3)
c6002  format('Y: ',2f8.3)

      return
      end

      subroutine rotv(X,Y,Z,vX,vY,vZ)
      R=sqrt(X*X+Y*Y)
c     Old version used preset rotation curve...
      twistsc=10
      vrot=rcur1(R)*exp(-Z/twistsc)
      vX=Y/R*vrot
      vY=-X/R*vrot
      vZ=0
      return
      end

      function rcur1(R)
      rcur1=220*(1.0074*(R/8.5)**(0.0382)+0.00698)
      return
      end

c=======================================================================
      function vlos_fcn(X,Y,Z,v_X,v_Y,v_z)
c=======================================================================
      dist=sqrt(X*X+(Y-8.5)*(Y-8.5)+Z*Z)
c     v_X1 is motion relative to sun using Burton curve
      v_X1=v_X-rcur1(8.5)
      if (dist.ne.0) then
      vlos_fcn=(X*v_X1+(Y-8.5)*v_Y+Z*v_Z)/dist
      else
      vlos_fcn=0.
      end if
      return
      end
c=======================================================================


