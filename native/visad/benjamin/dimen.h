
c     Define the number of elements, the number of other species of
c     interest, and the total number of species (where Ntot=all
c     atomic ions + molecules + electrons)

c     5+4+1=10
c     ----------------------------------
c     Array size parameters
c        NEL= no of elements
c        NIONS=total # of ions
c        NOTHER=# of non-ionic species
c        NTEMP=# of temperature zones
c              in atomic data tables
c        NPHOT=# of photoionization bins
c        NZR=# of spatial zones
c     -----------------------------------
      parameter(NEL=30)
      parameter(NIONS=495)
      parameter(NOTHER=4)
      parameter(NTOT=500)
      parameter(NTEMP=1)
      parameter(NPHOT=1000)
c      parameter(NPHOT=255)

      parameter (NTHERM=12)
      parameter (NZR=1)
      parameter (NZL=-1)


c     Cylindrical grid size
C WLH
      parameter (NZMAX=21)
c      parameter (NZMAX=1024)
c     parameter (NZMAX=2048)
C WLH
      parameter (NRMAX=46)
c      parameter (NRMAX=32)
c     Cartesian grid size  rho(-NXP:NXP,-NYP:NYP,-NZP:NZP)
      parameter (NXP=NRMAX-1,NYP=NXP,NZP=NZMAX-1)
c     All-sky map size
      parameter(NxpxMAX=364,NypxMAX=182)
C WLH
C     parameter (NgLMAX=360,NgBMAX=181)

c     -----------------------------------
c     Added for galactic equilibrium calculations
c     -----------------------------------
c     five phases (Mol,CNM,WNM,WIM,Hot)
      parameter(NPHASE=5)
      parameter(NDFCN=3)
      parameter(NDPAR=3)

c     five pressures (therm,kin,B,CR)
      parameter(NFORCE=4)
      parameter(NPFCN=3)
      parameter(NPPAR=5)

c     three components of gravity (bulge,disk,halo)
      parameter(NGRAV=3)
      parameter(NGFCN=3)
      parameter(NGPAR=3)

c     radiation pressure choices
      parameter(NRP=3)
      parameter(NRPPAR=7)

c     filling factor choices
      parameter(NFF=3)
      parameter(NFFPAR=5)

c     abundance pattern choices
      parameter(NAB=4)
      parameter(NABPAR=3)

c     output variables
      parameter(NNOUT=18)
      parameter(NSLICE=5)

c     mask parameter for 3D grid
      parameter(NMSK=5)






