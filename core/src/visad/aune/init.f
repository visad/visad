      SUBROUTINE INIT(NSTEP,DKU,DKV,DKH,IL,JL)
C===================================================================
C   THIS ROUTINE INITIALIZES MODEL PARAMETERS AND FIELDS, AND OUTPUT
C   GRAPHICS PARAMETERS
C===================================================================
C
      REAL DKU(IL,*),DKV(IL,*),DKH(IL,*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
C---COMPUTE MODEL CONSTANTS
      OVDX2=1./(DELX*DELX)
      OV2DX=1./(2.*DELX)
      OV12DX=1./(12.*DELX)
      DELT2=DELT*2.
C
C---ASSIGN DIFFUSION ARRAY
      IF(NSTEP .GT. 1) GOTO 90
      DO 10 J=1,JL
      DO 10 I=1,IL
C        DIFF(I,J)=ADIFF
         DKU(I,J)=0.
         DKV(I,J)=0.
         DKH(I,J)=0.
 10   CONTINUE
 90   CONTINUE
C
C---NORTH-SOUTH BOUNDARY GETS SPONGE
CCC   NBND=4
CCC   DO 20 N=1,NBND
CCC      ABND=ADIFF*(8.-FLOAT(N-1)*2.)
CCC      NN=NY-N+1
CCC      NS=N
CCC      DO 40 I=1,IL
CCC         DIFF(I,NN)=ABND
C40         DIFF(I,NS)=ABND
C20   CONTINUE
C
C---COMPUTE GRID CONSTANTS
      I1=1
      I2=2
      I3=3
      I4=4
      I5=5
      J1=1
      J2=2
      J3=3
      J4=4
      J5=5
      NXM1=IL-1
      NXM2=IL-2
      NXM3=IL-3
      NXM4=IL-4
      NXM5=IL-5
      NXM6=IL-6
      NYM1=JL-1
      NYM2=JL-2
      NYM3=JL-3
      NYM4=JL-4
      NYM5=JL-5
      NYM6=JL-6
C
      RETURN
      END
