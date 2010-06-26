
C      SUBROUTINE EV_DIFF_PROF
      SUBROUTINE EV_DIFF_PROF(TS, TDS, PS, ZS, TO, WO, OO, PO, diff)

C     Paolo Antonelli
C     Last update 11 Nov 1997

      implicit none

      integer NLS,NLO
C      integer NLINO,NLINS
      parameter (NLS=22,NLO=40)
      real TS(NLS),TDS(NLS),PS(NLS),ZS(NLS)
      real TO(NLO),WO(NLO),OO(NLO),PO(NLO),DIFF(NLO)
      integer i

C      CALL ADM1D(1, 1, NLS, TS, NLINS)
C      CALL ADM1D(1, 2, NLS, TDS, NLINS)
C      CALL ADM1D(1, 3, NLS, PS, NLINS)
C      CALL ADM1D(1, 4, NLS, ZS, NLINS)

C      CALL ADM1D(2, 1, NLO, TO, NLINO)
C      CALL ADM1D(2, 2, NLO, WO, NLINO)
C      CALL ADM1D(2, 3, NLO, OO, NLINO)
C      CALL ADM1D(2, 4, NLO, PO, NLINO)

      do i=1,NLS-1
         diff(NLO-NLS+i+1)=TO(NLO-NLS+i+1)-TS(i)
      enddo !i
      do i=1,NLO-NLS
         diff(i)=0.0
      enddo !i

C      CALL ADR1D(3,diff,NLINO)

      RETURN
      END

