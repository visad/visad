c     ----------------------------------
c     Atomic and mathematical constants
c     ----------------------------------
      parameter(eVTOerg=1.60210e-12)
      parameter(CLIGHT=2.997925e10)
      parameter(ELOG10=2.302585)
      parameter(PI=3.14159265359)
      parameter(eVTOnu=2.41629e14)
      parameter(BOLTZK=1.38054e-16)
      parameter(GRCON=6.67e-8)
      parameter(AW=1.6606e-24)
      parameter(RSUN=8.5)
      parameter(SUNL=3.9e33)

      parameter (T0=1.00e10)
      parameter (V0=1.18e6)
c     Solar
c      parameter (RHO0=2.27658539681219E-24)
c     Solar Morrison
c      parameter (RHO0=2.3822596E-24)
c     Junk solar
c      parameter (RHO0=1.6738875E-24)
c     1/10 solar
c      parameter (RHO0=2.243592876072399E-24)
c     Grevesse and Anders solar 30 elements
c      parameter (RHO0=2.3678660E-24)
c     Ferriere
c      parameter (RHO0=2.2720894E-24)
      parameter (RGAS=8.3143e7)
      parameter (PC1=3.09e18)
      parameter (YR1=3.15e7)


c     -------------------------------
c     Radiative transfer coefficients
c     Using 4 pt Gaussian quadrature
c     -------------------------------
      common /rad1/costh(4),wt(4)

c     ----------------------------
c     Tab character (ASCII(9)=^I)
c     ----------------------------
      character tab
      common /charset/tab
