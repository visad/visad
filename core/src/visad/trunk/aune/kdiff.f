      SUBROUTINE KDIFF(A,D,IL,DK)
C============================================================
C   THIS ROUTINE COMPUTES DIFFUSION
C============================================================
C
      REAL A(IL,*),DK(IL,*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
      DO 10 J=2,NYM1
         JP1=J+1
         JM1=J-1
      DO 10 I=2,NXM1
         IP1=I+1
         IM1=I-1
         DK(I,J)=D*(A(IP1,J)+A(IM1,J)+A(I,JP1)+A(I,JM1)
     &          -4.*A(I,J))*OVDX2
 10   CONTINUE
C
      RETURN
      END
