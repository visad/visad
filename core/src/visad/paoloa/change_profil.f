
      SUBROUTINE CHANGE_PROFIL(PT, DT, PW, DW, O_W, T_W,
     +                         W_W, T, W, O, P)

C     Paolo Antonelli
C     Last update 11 Nov 1997

      implicit none

      INTEGER NL,PT,PW
      PARAMETER (NL=40)
      REAL DT,DW
      REAL T(NL),O(NL),W(NL),P(NL)
C      REAL WF(NL)
      REAL O_W,T_W,W_W
C      REAL ADREAL
      INTEGER I
C      INTEGER NCHNIN, NLIN

       WRITE (6,*) PT, DT, PW, DW, O_W, T_W, W_W

C      PT=INT(ADREAL(1))
C      DT=ADREAL(2)
C      PW=INT(ADREAL(3))
C      DW=ADREAL(4)
C      O_W=ADREAL(5)
C      T_W=ADREAL(6)
C      W_W=ADREAL(7)
C      CALL ADM1D(8, 1, NL, T, NLIN)
C      CALL ADM1D(8, 2, NL, W, NLIN)
C      CALL ADM1D(8, 3, NL, O, NLIN)
C      CALL ADM1D(8, 4, NL, P, NLIN)

      T(pt-1) = T(pt-1)+.33*dt
      T(pt) = T(pt)+dt
      T(pt+1) = T(pt+1)+.33*dt
      W(pw-1) = W(pw-1)+.0033*dw*W(pw-1)*.01
      W(pw) = W(pw)+W(pw)*dw*.01
      W(pw+1) = W(pw+1)+.0033*W(pw+1)*dw*.01
      do i=1,nl
         T(i) = T(i)+T_W*T(i)*.01
         O(i) = O(i)+O_W*O(i)*.01
         W(i) = W(i)+W_W*W(i)*.01
      enddo !i

C      CALL ADRM1D(8, 1,  T, NLIN)
C      CALL ADRM1D(8, 2,  W, NLIN)
C      CALL ADRM1D(8, 3,  O, NLIN)
C      CALL ADRM1D(8, 4,  P, NLIN)

      RETURN
      END

