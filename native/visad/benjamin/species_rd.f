c===========================================================================
      subroutine species_rd(nunit)
c===========================================================================
c     Reads in a file that contains generic information on atomic data
c     There is very little error checking. Beware.
c     INPUT :            atomic.inp
c     INCLUDE FILES:     atomic.h
c     FORMAT OF INPUT FILE:
c     Line 1   :   Text
c     Line 2   :   number of elements, number of molecules
c     Line 3   :   Text
c     Line 4-x :   x=number of elements+number of molecules
c                  1) Label    (4 characters max)
c                  2) Atomic # (0 for molecules)
c                  3) Charge   (0 for atom)
c                  4) Mass     (in atomic units)
c                  5) Gam      (=Cp/Cv; 5/3 for ideal monatomic gas)
c                              (        7/5 for diatomic molecules )
c                  6) Abundance(=1 for molecules                   )
c     OUTPUT:  fills up arrays in common block atdat1
c
c---------------------------------------------------------------------------

      include "dimen.h"
      include "const.h"
      include "atomic.h"
      character*4 label_
      character*80 explan_text

      character*6 roman(31)
      data roman/'I     ','II    ','III   ','IV    ','V     ',
     >           'VI    ','VII   ','VIII  ','IX    ','X     ',
     >           'XI    ','XII   ','XIII  ','XIV   ','XV    ',
     >           'XVI   ','XVII  ','XVIII ','XIX   ','XX    ',
     >           'XXI   ','XXII  ','XXIII ','XXIV  ','XXV   ',
     >           'XXVI  ','XXVII ','XXVIII','XXIX  ','XXX   ',
     >           'XXXI  '/
c----------------------------------------------------------------------------


      read(nunit,1001) explan_text
1001  format(a80)
      read(nunit,*) Nel_,Nother_

      if ( (Nel_.ne.NEL).or.(Nother_.ne.NOTHER) ) call errmsg(1)

c     ------------------------
c     Fill up element arrays
c     ------------------------
      i=1
      read(nunit,1001) explan_text
      do 10 iel=1,NEL
          read(nunit,1002) label_, no_, ncharge_,amu_, gam_, abund_
1002      format(a4,2x,i3,6x,i3,3x,f9.4,f8.4,e9.2 )

          indexel(iel)=i
          noel(iel)=no_

          do 20 jion=1,no_+1
              no(i)=no_
              ylab(i)=label_//roman(jion)
              ncharge(i)=jion-1
              amu(i)=amu_
              gam(i)=gam_
              abund(i)=abund_
              i=i+1
 20       continue

 10   continue

c     Fill up molecule/other arrays
      do 30 iot=1,Nother_
          read(nunit, 1002) label_, no_, ncharge_, amu_, gam_, abund_
          no(i)=no_
          ylab(i)=label_//'      '
          ncharge(i)=ncharge_
          amu(i)=amu_
          gam(i)=gam_
          abund(i)=abund_
          i=i+1
 30   continue

c     Fill up arrays for electron
      ylab(i)='e-        '
      ncharge(i)=-1
      amu(i)=5.486e-4
      gam(i)=1.6667
      abund(i)=1.00


      if (i.ne.NTOT) call errmsg(1)

c     -------------------------------
c     Calculate conversion from particle
c     to mass density
c     Save as RHO0...
c     -------------------------------
      dmass=0.
      do 35 i=1,NEL
 35      dmass=dmass+amu(indexel(i))*abund(indexel(i))
      dmass=dmass*AW


      RHO0=dmass

      end
c============================================================================
