      SUBROUTINE CHANEL(U,V,H,CC,IL,JL)
C==============================================================
C   THIS ROUTINE ASSIGNS BOUNDARY VALUES FOR CHANNEL CONDITIONS
C   NORTH-SOUTH BC'S ARE REFLECTIVE, EAST-WEST ARE CYCLIC
C==============================================================
C
      REAL U(IL,JL,*),V(IL,JL,*),H(IL,JL,*),CC(IL,JL,*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
C---EAST-WEST BC'S ARE CYCLIC
      DO 2 J=J2,NYM1
         U(1,J,3)=U(NXM3,J,3)
         U(2,J,3)=U(NXM2,J,3)
         U(NXM1,J,3)=U(3,J,3)
         U(IL,J,3)=U(4,J,3)
         V(1,J,3)=V(NXM3,J,3)
         V(2,J,3)=V(NXM2,J,3)
         V(NXM1,J,3)=V(3,J,3)
         V(IL,J,3)=V(4,J,3)
         H(1,J,3)=H(NXM3,J,3)
         H(2,J,3)=H(NXM2,J,3)
         H(NXM1,J,3)=H(3,J,3)
         H(IL,J,3)=H(4,J,3)
         CC(1,J,3)=CC(nxm3,j,3)
         CC(2,J,3)=CC(nxm2,j,3)
         CC(NXM1,J,3)=CC(3,J,3)
         CC(IL,J,3)=CC(4,J,3)
 2    CONTINUE
C
C---NORTH/SOUTH WHR RADIATIVE/TIME AVERAGED
      DO 6 I=1,IL
c        U(I,1,3)=2.*U(I,2,2)-U(I,3,1)
c        U(I,JL,3)=2.*U(I,NYM1,2)-U(I,NYM2,1)
c        V(I,1,3)=2.*V(I,2,2)-V(I,3,1)
c        V(I,JL,3)=2.*V(I,NYM1,2)-V(I,NYM2,1)
c        V(I,1,3)=-V(I,2,3)
c        V(I,JL,3)=-V(I,NYM1,3)
c        H(I,1,3)=H(I,2,1)
c        H(I,JL,3)=H(I,NYM1,1)
C
C---REFLECTIVE
         U(I,1,3)=U(I,2,3)
         U(I,JL,3)=U(I,NYM1,3)
         V(I,1,3)=-V(I,2,3)
         V(I,JL,3)=-V(I,NYM1,3)
         H(I,1,3)=H(I,2,3)
         H(I,JL,3)=H(I,NYM1,3)
         CC(I,1,3)=CC(I,2,3)
         CC(I,JL,3)=CC(I,NYM1,3)
C
C---ON/OFF FLOW
C        IF(V(I,2,3).LT.0.)THEN
C           V(I,1,3)=V(I,3,3)
C        ELSE
C           V(I,1,3)=V(I,2,3)
C        ENDIF
C        IF(V(I,NYM1,3).GT.0.)THEN
C           V(I,JL,3)=V(I,NYM2,3)
C        ELSE
C           V(I,JL,3)=V(I,NYM1,3)
C        ENDIF
C        H(I,1,3)=2.*H(I,2,3)-H(I,3,3)
C        H(I,JL,3)=2.*H(I,NYM1,3)-H(I,NYM2,3)
 6    CONTINUE
C
      RETURN
      END
