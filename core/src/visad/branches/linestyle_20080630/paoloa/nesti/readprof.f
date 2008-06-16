      SUBROUTINE READ_PROF(I, P_FLG, TSKIN, PSFC, LSFC,
     +                     AZEN, P_DUM, T, W, O )


C     Read in T,WV,O profiles, ST, PS, from a data file
C     The profiles should be passed to a visad application
C     and from the application to the nast-i forward model.
C     Paolo Antonelli,  Wed Sep 23 09:25:31 DT  1998.

      IMPLICIT NONE

      INTEGER NL,LENG, I, p_flg, LSFC(1)
      INTEGER NR,J,L,LUI,LENI
      PARAMETER (NL=40)
      PARAMETER (LENG=145)
      CHARACTER*64 PROFF
      REAL*4 BUF(LENG)
      REAL*4 TSKIN(1),PSFC(1),AZEN(1), pp
      REAL*4 P(NL), P_DUM(NL), T(NL),W(NL),O(NL)
      INTEGER lsurface, lout
      DATA LUI/15/

      DATA    P/50.,60.,70.,75.,80.,85.,90.,100.,125.,150.,175.,200.,
     *   250.,300.,350.,400.,450.,500.,550.,600.,620.,640.,660.,680.,
     *   700.,720.,740.,760.,780.,800.,820.,840.,860.,880.,900.,920.,
     *   940.,960.,980.,1000./


      if ( p_flg .EQ. 1) then
        PROFF='./raob980913_alt.ac.2237z'
      else
        PROFF='./camx97ax.two.psfc'
      endif

      LENI=LENG*4
      OPEN(LUI,RECL=LENI,FILE=PROFF,STATUS='OLD',ACCESS='DIRECT')

C     NR refers to a specific profile (it is the record number
C     corresponding to that profile)

      NR=1

      READ(LUI,REC=NR) (BUF(J),J=1,LENG)
      DO L=1,NL
       T(L)=BUF(L)
       W(L)=BUF(NL+L)
       O(L)=BUF(2*NL+L)
      ENDDO
      TSKIN(1)=BUF(121)
      PSFC(1)=BUF(122)
      AZEN(1)=BUF(140)
      if(PSFC(1).gt.1000.) PSFC(1)=1000.
      pp =  PSFC(1)
      lout = lsurface( nl, p, pp, 700., 1000. )
      LSFC(1) = lout

C     WRITE(*,'('' RECORD='',I8,'' Psfc;TSKIN;ZEN;Lsfc='',3f9.3,i6)')
C    $  NR,PSFC,TSKIN,AZEN,LSFC
      IF(MOD(NR,100).EQ.1) THEN
C     write(*,'('' pressure  temperature  water vapor     ozone'')')
      do l=1,nl
       P_DUM(l) = P(l)
C      write(*,'(1x,f8.1,2x,f11.2,2x,f11.3,2x,f11.4)')
C    $ p(l),t(l),w(l),o(l)
      enddo
      ENDIF

      CLOSE(LUI)

      RETURN
      END

c
      function lsurface ( numlev, pres, psurf, plow, phigh )
      implicit none
      integer*4  lsurface
c***********************************************************************
c
c        Routine to find the pointer to the bottom of atmosphere.
c
c***********************************************************************
      integer*4 numlev
      real*4    pres(*), Psurf, Plow, Phigh

C     local variables
c     ---------------

      integer *4 L

c***********************************************************************
c***********************************************************************
c
c     The bottom layer is at least 5 mb thick
c
c     Surface pressure between plow, phigh
c     ------------------------------------

      if ( psurf .gt. plow .and. psurf .le. phigh ) then

         do L = numlev, 1, -1
            if ( psurf .ge. pres(L-1)+5.0 ) then
               lsurface = L
               goto 990
            end if
         end do
      end if

c     surface pressure exceeded limits
c     --------------------------------

      if(psurf.le.plow) then
        lsurface = 1
        do L = 1, numlev
          if(psurf.lt.pres(L)) lsurface = L
        enddo
      else
        lsurface = numlev
        do L = numlev,1,-1
          if(psurf.gt.pres(L)+5.0) lsurface = L
        enddo
      endif

      print 100, plow, psurf, phigh, lsurface, pres(lsurface)

  990 return
  100 format('lsurface: ',f7.2,' <= (psurf=',f7.2,') <= ',f7.2,
     1    ' pres(',i3,')=',f7.2)
      end
C **

