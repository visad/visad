      SUBROUTINE TANFLT(A,EP,IL,JL)
C============================================================
C   THIS ROUTINE COMPUTES DIFFUSION
C============================================================
C
      REAL A(IL,*),BBB(200),XANS(200)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
      REAL V(200),X(200),GAMN(200),GAMN1(200)
     1    ,GAMN2(200),YN(200),YN1(200),YN2(200)
C
      DATA IJUMP/0/
C
C---APPLY HORIZONTAL FILTER ALONG GRID COLS
      ICHK=0
      BBB(J1)=0.0
      BBB(JL)=0.0
C     DO 3335 I=I2,NXM1
      DO 3335 I=I3,NXM2
         DO 4445 K=1,JL
            XANS(K)=0.0
 4445    CONTINUE
         BBB(J2)=EP*(A(I,J1)-2.*A(I,J2)+A(I,J3))
         BBB(J3)=EP*(-1*(A(I,J1)+A(I,J5))+4.*(A(I,J2)
     &          +A(I,J4))-6.*A(I,J3))
         BBB(NYM1)=EP*(A(I,NYM2)-2.*A(I,NYM1)+A(I,JL))
         BBB(NYM2)=EP*(-1.*(A(I,JL)+A(I,NYM4))
     &           +4.*(A(I,NYM1)+A(I,NYM3))-6.*A(I,NYM2))
         DO 3334 J=J4,NYM3
            BBB(J)=EP*((A(I,J-3)+A(I,J+3))
     &            -6.*(A(I,J-2)+A(I,J+2))+15.*(A(I,J-1)
     &            +A(I,J+1))-20.*A(I,J))
 3334    CONTINUE
         ICHK=ICHK+1
         CALL INVERT(BBB,JL,XANS,EP,ICHK)
C        DO 3333 J=J3,NYM2
         DO 3333 J=J1,JL
            A(I,J)=A(I,J)+XANS(J)
 3333    CONTINUE
 3335 CONTINUE
      DO 1135 J=1,JL
         A(1,J)=A(NXM3,J)
         A(2,J)=A(NXM2,J)
         A(NXM1,J)=A(3,J)
         A(IL,J)=A(4,J)
1135   CONTINUE
C
C---APPLY CYCLIC BOUNDARY CONDITIONS
       ICHK=0
       DO 3338 J=J2,NYM1
C      DO 3338 J=J1,JL
         BBB(1)=EP*((A(NXM6,J)+A(4,J))
     %         -6.*(A(NXM5,J)+A(3,J))+15.*(A(NXM4,J)
     %         +A(2,J))-20.*A(1,J))
         BBB(2)=EP*((A(NXM5,J)+A(5,J))
     %         -6.*(A(NXM4,J)+A(4,J))+15.*(A(NXM3,J)
     %         +A(3,J))-20.*A(2,J))
         BBB(3)=EP*((A(NXM4,J)+A(6,J))
     %         -6.*(A(NXM3,J)+A(5,J))+15.*(A(NXM2,J)
     %         +A(4,J))-20.*A(3,J))
         BBB(NXM2)=BBB(2)
         BBB(NXM1)=BBB(3)
         DO 3336 I=I4,NXM3
            BBB(I)=EP*((A(I-3,J)+A(I+3,J))-6.*(A(I-2,J)
     &            +A(I+2,J))+15.*(A(I-1,J)+A(I+1,J))
     &            -20.*A(I,J))
 3336    CONTINUE
         BBB(IL)=BBB(4)
         DO 2444 K=1,JL
            XANS(K)=0.0
 2444    CONTINUE
         DO 2334 I=I3,NXM2
         BBB(I-2)=BBB(I)
 2334    CONTINUE
         ICHK=ICHK+1
         CALL INVER2(BBB,NXM4,XANS,EP,ICHK)
         DO 2333 I=I3,NXM2
            A(I,J)=A(I,J)+XANS(I-2)
2333     CONTINUE
3338     CONTINUE
         DO 2222 J=1,JL
         A(1,J)=A(NXM3,J)
         A(2,J)=A(NXM2,J)
         A(NXM1,J)=A(3,J)
         A(IL,J)=A(4,J)
 2222   CONTINUE
C
      RETURN
      END
      SUBROUTINE INVERT(BB,N,XANS,EP,ICHECK)
C=========================================================
C
      REAL BB(*),XANS(*)
      REAL A(200),B(200),C(200),D(200),E(200),
     1     DELTA(200),BETA(200),W(200),GAM(200),H(200),
     2     PI(200),AP(200),F(200),Z(200)
C
      NN=N-1
      NNN=N-2
      NNNN=N-3
      OPEP=1.+EP
      OMEP=1.-EP
      IF(ICHECK.GT.1)GOTO 100
C     PRINT *,' INITIALISE CONSTANTS IN INVER2'
      DO 10 I=4,NNNN
         Z(I)=OMEP
         A(I)=6.*OPEP
         B(I)=15.*OMEP
         C(I)=20.*OPEP
         D(I)=B(I)
         E(I)=A(I)
         F(I)=Z(I)
 10   CONTINUE
      Z(1)=0.0
      Z(2)=0.0
      Z(3)=0.0
      A(1)=0.0
      A(2)=0.0
      A(3)=OPEP
      B(1)=0.0
      B(2)=OMEP
      B(3)=4.*OMEP
      C(1)=1.
      C(2)=2.*OPEP
      C(3)=6.*OPEP
CRMA  D(1)=0.0
      D(1)=-1.
      D(2)=OMEP
      D(3)=4.*OMEP
      E(1)=0.0
      E(2)=0.0
      E(3)=OPEP
      F(1)=0.0
      F(2)=0.0
      F(3)=0.0
      A(NNN)=OPEP
      A(NN)=0.0
      A(N)=0.0
      B(NNN)=4.*OMEP
      B(NN)=OMEP
CRMA  B(N)=0.0
      B(N)=1.
      C(NNN)=6.*OPEP
      C(NN)=2.*OPEP
CRMA  C(N)=1.
      C(N)=-1.
      D(NNN)=4.*OMEP
      D(NN)=OMEP
      D(N)=0.0
      E(NNN)=OPEP
      E(NN)=0.0
      E(N)=0.0
      F(N)=0.0
      F(NN)=0.0
      F(NNN)=0.0
      Z(NNN)=0.0
      Z(NN)=0.0
      Z(N)=0.0
      DELTA(1)=0.0
      BETA(1)=D(1)/C(1)
      DELTA(2)=B(2)
      W(1)=C(1)
      PI(1)=F(1)/W(1)
      AP(1)=0.0
      AP(2)=0.0
      AP(3)=A(3)
      W(2)=C(2)-DELTA(2)*BETA(1)
      GAM(1)=E(1)/C(1)
      BETA(2)=(D(2)-DELTA(2)*GAM(1))/W(2)
      GAM(2)=(E(2)-PI(1)*DELTA(2))/W(2)
      PI(2)=F(2)/W(2)
      DELTA(3)=(B(3)-AP(3)*BETA(1))
      W(3)=C(3)-DELTA(3)*BETA(2)-AP(3)*GAM(1)
      BETA(3)=(D(3)-AP(3)*PI(1)-DELTA(3)*GAM(2))/W(3)
      GAM(3)=(E(3)-DELTA(3)*PI(2))/W(3)
      PI(3)=F(3)/W(3)
 120  DO 20 I=4,N
         AP(I)=A(I)-Z(I)*BETA(I-3)
         DELTA(I)=B(I)-AP(I)*BETA(I-2)-Z(I)*GAM(I-3)
         W(I)=C(I)-AP(I)*GAM(I-2)-DELTA(I)*BETA(I-1)
     &       -Z(I)*PI(I-3)
         BETA(I)=(D(I)-AP(I)*PI(I-2)-DELTA(I)*GAM(I-1))/W(I)
         GAM(I)=(E(I)-DELTA(I)*PI(I-1))/W(I)
         PI(I)=F(I)/W(I)
 20   CONTINUE
100   CONTINUE
      H(1)=BB(1)/W(1)
      H(2)=(BB(2)-DELTA(2)*H(1))/W(2)
      H(3)=(BB(3)-DELTA(3)*H(2)-AP(3)*H(1))/W(3)
      DO 30 I=4,N
         H(I)=(BB(I)-DELTA(I)*H(I-1)-AP(I)*H(I-2)
     &       -Z(I)*H(I-3))/W(I)
 30   CONTINUE
      XANS(N)=H(N)
      XANS(NN)=H(NN)-BETA(NN)*XANS(N)
      XANS(NNN)=H(NNN)-BETA(NNN)*XANS(NN)-GAM(NNN)
     &         *XANS(N)
      DO 40 II=1,NNNN
         I=NNN-II
         XANS(I)=H(I)-BETA(I)*XANS(I+1)-GAM(I)*XANS(I+2)
     &          -PI(I)*XANS(I+3)
 40   CONTINUE
C
      RETURN
      END
      SUBROUTINE INVER2(BB,N,XANS,EP,ICHECK)
C===============================================================
C
      REAL XANS(*),BB(*)
      REAL A(200),B(200),C(200),D(200),E(200),
     1     DELTA(200),BETA(200),W(200),GAM(200),H(200),
     2     PI(200),AP(200),F(200),Z(200)
C
      N1=N-1
      N2=N-2
      N3=N-3
      IF(ICHECK.GT.1)GOTO 100
C
C---INITIALIZE CONSTANTS IN INVER2
      DO 10 I=1,N
         Z(I)=1.-EP
         A(I)=6.*(1.+EP)
         B(I)=15.*(1.-EP)
         C(I)=20.*(1.+EP)
         D(I)=B(I)
         E(I)=A(I)
         F(I)=Z(I)
 10   CONTINUE
      BETA(1)=D(1)/C(1)
      DELTA(2)=B(2)
      W(1)=C(1)
      PI(1)=F(1)/W(1)
      AP(1)=0.0
      AP(2)=0.0
      AP(3)=A(3)
      W(2)=C(2)-DELTA(2)*BETA(1)
      GAM(1)=E(1)/C(1)
      BETA(2)=(D(2)-DELTA(2)*GAM(1))/W(2)
      GAM(2)=(E(2)-PI(1)*DELTA(2))/W(2)
      PI(2)=F(2)/W(2)
      DELTA(3)=(B(3)-AP(3)*BETA(1))
      W(3)=C(3)-DELTA(3)*BETA(2)-AP(3)*GAM(1)
      BETA(3)=(D(3)-AP(3)*PI(1)-DELTA(3)*GAM(2))/W(3)
      GAM(3)=(E(3)-DELTA(3)*PI(2))/W(3)
      PI(3)=F(3)/W(3)
      DO 20 I=4,N
         IM1=I-1
         IM2=I-2
         IM3=I-3
         AP(I)=A(I)-Z(I)*BETA(IM3)
         DELTA(I)=B(I)-AP(I)*BETA(IM2)-Z(I)*GAM(IM3)
         W(I)=C(I)-AP(I)*GAM(IM2)-DELTA(I)*BETA(IM1)
     1       -Z(I)*PI(IM3)
         BETA(I)=(D(I)-AP(I)*PI(IM2)-DELTA(I)*GAM(IM1))/W(I)
         GAM(I)=(E(I)-DELTA(I)*PI(IM1))/W(I)
         PI(I)=F(I)/W(I)
 20   CONTINUE
 100  CONTINUE
      H(1)=BB(1)/W(1)
      H(2)=(BB(2)-DELTA(2)*H(1))/W(2)
      H(3)=(BB(3)-DELTA(3)*H(2)-AP(3)*H(1))/W(3)
      DO 30 I=4,N
         H(I)=(BB(I)-DELTA(I)*H(I-1)-AP(I)*H(I-2)
     1       -Z(I)*H(I-3))/W(I)
30    CONTINUE
      XANS(N)=H(N)
      XANS(N1)=H(N1)-BETA(N1)*XANS(N)
      XANS(N2)=H(N2)-BETA(N2)*XANS(N1)-GAM(N2)
     1        *XANS(N)
      DO 40 II=1,N3
         I=N2-II
         XANS(I)=H(I)-BETA(I)*XANS(I+1)-GAM(I)*XANS(I+2)
     1          -PI(I)*XANS(I+3)
40    CONTINUE
C
      RETURN
      END
