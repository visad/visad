
      character*11 ylab

c     --------------------------------
c     Atomic data arrays
c     --------------------------------
      common /atdat0/  T_(NTEMP),ph_binc(NPHOT),ph_binw(NPHOT)
      common /atdat1/  no(NTOT),ncharge(NTOT),amu(NTOT),
     >                 gam(NTOT),abund(NTOT),Eioz(NTOT)
      common /atdat2/  rcol(NIONS,NTEMP),rrec(NIONS,NTEMP),
     >                 rcxhr(NIONS,NTEMP),rcxher(NIONS,NTEMP),
     >                 rcxhi(NIONS,NTEMP),rcxhei(NIONS,NTEMP)
      common /atdat3/  rcool(NIONS,NTEMP)
      common /atdat4/  sig_ph(NIONS,NPHOT)
      common /atdatl/  ylab(NTOT)
      common  /eldat1/ indexel(NEL),noel(NEL)
      common /eldat2/ RHO0
