      subroutine dmdsm(l,b,ndir,dmpsr,dist,limit,sm,smtau,smtheta)

c  Computes pulsar distance and scattering measure
c  from model of Galactic electron distribution.

c  Input: real l	(galactic longitude in radians)
c         real b	(galactic latitude in radians)
c         integer ndir  (>= 0 calculates dist from dmpsr;
c                         < 0 for dmpsr from dist)
c Input or output:
c	  real dmpsr	(dispersion measure in pc/cm^3)
c         real dist	(distance in kpc)

c  Output:
c	  char*1 limit	(set to '>' if only a lower distance limit can be
c			 given; otherwise set to ' ')
c         sm            (scattering measure, uniform weighting) (kpc/m^{20/3})
c         smtau         (scattering measure, weighting for pulse broadening)
c         smtheta       (scattering measure, weighting for angular broadening
c                        of galactic sources)

c       parameter(alpha = 11./3.)
c       parameter(pi = 3.14159)
c       parameter(c_sm = (alpha - 3.) / 2. * (2.*pi)**(4.-alpha) )
        parameter(c_sm = 0.181)         ! constant in sm definition
        parameter(c_u = 10.16)          ! units conversion for sm
        parameter(sm_factor = c_sm * c_u)

	logical first
	character*1 limit
        real l,n1h1,n2,na,ne1,ne2,nea,negum,ne
        common/params/n1h1,h1,A1,F1,n2,h2,A2,F2,na,ha,wa,Aa,Fa,Fg
	common/dxyz/dx0,dy0,dz0

	data R0/8.5/
	data rrmax/30.0/		! Max radius for reliable ne
	data zmax/1.76/			! Max |z|
        data dmax/30.0/                 ! maximum distance calculated
	data first/.true./
	save

	if(first) call tcdensity(0.0,8.5,0.0,ne1,ne2,nea,negum)
	first=.false.

	sl=sin(l)
	cl=cos(l)
	sb=sin(b)
	cb=cos(b)

	dm=0.0
        sm_sum1 = 0.                    ! sum of C_n^2
        sm_sum2 = 0.                    ! sum of C_n^2 * s
        sm_sum3 = 0.                    ! sum of C_n^2 * s^2

	limit=' '
	dstep=0.02			! Step size in kpc
        dstep = min(h1, h2) / 10.       ! step size in terms of scale heights
        if(ndir.lt.0) dtest=dist
c        if(ndir.ge.0) dtest=dmpsr/(n1h1/h1)   ! approximate test distance
        nstep = dtest / dstep	        ! approximate number of steps
        if(nstep.lt.10) dstep=dtest/10  ! make # steps >= 10

C  Integrate numerically until dm is reached (ndir >= 0) or dist is
C  reached (ndir < 0).
	d=-0.5*dstep
	do 10 i=1,9999
	d=d+dstep			! Distance from Sun in kpc
	r=d*cb
	x=r*sl
	y=R0-r*cl
	z=d*sb
	rr=sqrt(x**2 + y**2)		! Galactocentric radius
c	if(ndir.ge.0.and.
c     +    (d.gt.dmax.or.abs(z).gt.zmax.or.rr.gt.rrmax)) go to 20

	if(ndir.lt.3) call tcdensity(x,y,z,ne1,ne2,nea,negum)
c	if(ndir.ge.3) call tcdensity(x+dx0,y+dy0,z+dz0,ne1,ne2,nea,negum)
	ne=ne1+ne2+nea+negum		! Get total ne
	dmstep=1000.0*dstep*ne
	dm=dm+dmstep			! Add DM for this step

c        sm_term =
c     .       F1 * ne1**2 + F2 * ne2**2 + Fa * nea**2 + Fg * negum**2
c        sm_sum1 = sm_sum1 + sm_term
c        sm_sum2 = sm_sum2 + sm_term * d
c        sm_sum3 = sm_sum3 + sm_term * d**2

c	if(ndir.ge.0.and.dm.ge.dmpsr) go to 30	! Reached pulsar's DM?
	if(ndir.lt.0.and.d.ge.dist) go to 40	! Reached pulsar's dist?
10	continue
	stop 'loop limit'

20	limit='>'			! Only lower limit is possible
	dist=d-0.5*dstep
	go to 999

30	dist=d+0.5*dstep - dstep*(dm-dmpsr)/dmstep  ! Interpolate last step
	go to 999

40	dmpsr=dm-dmstep*(d+0.5*dstep-dist)/dstep

999	continue

cRAB
c        dsm = sm_term * (d+0.5*dstep - dist)
c        sm_sum1 = sm_sum1 - dsm
c        sm_sum2 = sm_sum2 - dsm * d
c        sm_sum3 = sm_sum3 - dsm * d**2
        sm=0
        smtau=0
        smtheta=0
c        sm = sm_factor * dstep * sm_sum1
c        smtau =
c     +     6. * sm_factor * dstep * (sm_sum2 / dist - sm_sum3 / dist**2)
c        smtheta =
c     +     3. * sm_factor * dstep * (sm_sum1 + sm_sum3 / dist**2 -
c     +     2. * sm_sum2 / dist)

        return
	end



