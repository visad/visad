
      subroutine profile(itype,NPROF_PT,X,Y,Z,xprof,yprof)
c     Given coordinates (X,Y,Z) of pt in galaxy profile returns
c     two 1D arrays, xprof, yprof with NPROF_PT values each.
c     itype=1 gives xprof=distance from sun, yprof=density
c     itype=2 will give xprof=velocity, yprof=emission measure 

      parameter(XSUN=0.,YSUN=8.5,ZSUN=0.)
      parameter(NPTMAX=1000)
      dimension xprof(*),yprof(*)


      if (NPROF_PT.gt.NPTMAX) then
         print *, 'Increase NPROF_PT in call to profile'
         stop
      end if

      do is=1,NPROF_PT
	 xprof(is)=0.
	 yprof(is)=0.
      end do

      ds=1./(NPROF_PT-1.)
c     print *, ds

      if (itype .eq.1) then
       do is=1,NPROF_PT  
         s=(is-1)*ds
         xs=XSUN+(X-XSUN)*s
	 ys=YSUN+(Y-YSUN)*s
         zs=ZSUN+(Z-ZSUN)*s
         call tcdensity(xs,ys,zs,dne1,dne2,dnea,dnegum) 
c        print *, 'pr:', s,xs,ys,zs,dne1+dne2+dnea
         xprof(is)=sqrt(xs*xs+ys*ys+zs*zs)
         yprof(is)=dne1+dne2+dnea
       end do
      else if (itype.eq.2) then
         print *, 'velocity map not constructed yet'
      else 
         print *, 'bad itype in profile'
         stop
      end if

      return
      end
      
