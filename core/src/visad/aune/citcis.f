      SUBROUTINE CITCIS(U,V,H,CC,DUDX,DVDX,DHDX,DUDY,DVDY
     &                 ,DHDY,DKU,DKV,DKH,DCCDX,DCCDY,F
     &                 ,IL,JL,G)
C============================================================
C  THIS ROUTINE INTEGRATES CENTERED IN TIME (LEAPFROG)
C============================================================
C
      REAL U(IL,JL,*),V(IL,JL,*),H(IL,JL,*),CC(IL,JL,*)
      REAL DUDX(IL,*),DVDX(IL,*),DHDX(IL,*)
     &    ,DUDY(IL,*),DVDY(IL,*),DHDY(IL,*)
     &    ,DCCDX(IL,*),DCCDY(IL,*)
     &    ,DKU(IL,*),DKV(IL,*),DKH(IL,*)
      REAL F(*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
C---MAIN LOOP
      DO 30 J=J2,NYM1
      DO 30 I=I2,NXM1
         UTEN=-U(I,J,2)*DUDX(I,J)-V(I,J,2)*(DUDY(I,J)-F(J))
     &        -G*DHDX(I,J)+DKU(I,J)
         VTEN=-V(I,J,2)*DVDY(I,J)-U(I,J,2)*(DVDX(I,J)+F(J))
     &        -G*DHDY(I,J)+DKV(I,J)
         HTEN=-U(I,J,2)*DHDX(I,J)-H(I,J,2)*DUDX(I,J)
     &        -V(I,J,2)*DHDY(I,J)-H(I,J,2)*DVDY(I,J)+DKH(I,J)
         CCTEN=-U(I,J,2)*DCCDX(I,J)-V(I,J,2)*DCCDY(I,J)
         U(I,J,3)=U(I,J,1)+UTEN*DELT2
         V(I,J,3)=V(I,J,1)+VTEN*DELT2
         H(I,J,3)=H(I,J,1)+HTEN*DELT2
         CC(I,J,3)=CC(I,J,1)+CCTEN*DELT2
 30   CONTINUE
C
      RETURN
      END
