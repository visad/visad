      SUBROUTINE RFLCTN(U,V,H,CC,IL,JL)
C=================================================================
C   THIS ROUTINE ASSIGNS BOUNDARY VALUES FOR REFLECTIVE CONDITIONS
C=================================================================
C
      REAL U(IL,JL,*),V(IL,JL,*),H(IL,JL,*),CC(IL,JL,*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
C---EAST-WEST BC
      DO 2 J=1,JL
         U(1,J,3)=-U(2,J,3)
         U(IL,J,3)=-U(NXM1,J,3)
CCC      V(1,J,3)=-V(2,J,3)
CCC      V(IL,J,3)=-V(NXM1,J,3)
         V(1,J,3)=V(2,J,3)
         V(IL,J,3)=V(NXM1,J,3)
         H(1,J,3)=H(2,J,3)
         H(IL,J,3)=H(NXM1,J,3)
         CC(1,j,3)=CC(2,j,3)
         CC(IL,j,3)=CC(NXM1,j,3)
 2    CONTINUE
C
C---NORTH-SOUTH BC
      DO 6 I=1,IL
CCC      U(I,1,3)=-U(I,2,3)
CCC      U(I,JL,3)=-U(I,NYM1,3)
         U(I,1,3)=U(I,2,3)
         U(I,JL,3)=U(I,NYM1,3)
         V(I,1,3)=-V(I,2,3)
         V(I,JL,3)=-V(I,NYM1,3)
         H(I,1,3)=H(I,2,3)
         H(I,JL,3)=H(I,NYM1,3)
         CC(I,1,3)=CC(I,2,3)
         CC(I,JL,3)=CC(I,NYM1,3)
 6    CONTINUE
C
      RETURN
      END
