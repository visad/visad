      SUBROUTINE MAKDAT(U,V,H,CC,F,IL,JL,UBAR,VBAR,HBAR
     &                 ,HPRM,G,IOPT,ASEP)
C==================================================================
C   THIS ROUTINE GENERATES INITIAL U, V  AND H FIELDS FOR SHALOW.
C   THREE OPTIONS:
C   IOPT = 1 :1D SINE WAVE PERTURBATION ON MEAN FIELD.
C          2 :2D VORTEX.
C          3 :2D VORTEX WITH BALANCED WIND FIELD.
C          4 :2D STEP FUNCTION.
C          5 :2D ANAYLTIC SOLUTION (STEADY STATE)
C   NOTE: KRAD = CONTROLS RADIUS OF VORTEX
C==================================================================
C
      REAL U(IL,JL,*),V(IL,JL,*),H(IL,JL,*),CC(IL,JL,*)
     &    ,F(*),HPRM(*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
      DATA PI/3.14159/
C
      ICNTR=IL/2+1
      JCNTR=JL/2+1
CCC   KRAD=MIN0(IL/4,JL/4)
      KRAD=MIN0(IL/6,JL/6)
C
C---INITIAL WAVE
      IF(IOPT.EQ.1)THEN
         DO 10 J=1,JL
         DO 10 I=1,IL
            U(I,J,2)=UBAR
            V(I,J,2)=VBAR
 10         H(I,J,2)=HBAR
         DO 20 J=1,JL
         DO 20 I=9,25
 20       H(I,J,2)=HBAR+HPRM(1)*.5*(1.+SIN(FLOAT(I-9)/16.*2.*PI-PI/2.))
      ENDIF
C
C---INITIAL VORTEX
      IF(IOPT.EQ.2.OR.IOPT.EQ.3)THEN
C        VRAD=SQRT(FLOAT((ICNTR-KRAD)*(ICNTR-KRAD)
C    &       +(JCNTR-KRAD)*(JCNTR-KRAD)))
         VRAD=SQRT(FLOAT(2*KRAD*KRAD))
CCC      WRITE(6,'('' VRAD = '',E14.7)')VRAD
         RMAXW=3.5
         DO 30 J=1,JL
         DO 30 I=1,IL
            RIJ=SQRT(FLOAT((ICNTR-I)*(ICNTR-I)+(JCNTR-J)*(JCNTR-J)))
            IF(RIJ.GT.VRAD)RIJ=VRAD
            IF(RIJ.EQ.0.)THEN
               RFAC=0.
            ELSE
               RFAC=EXP(-RMAXW/RIJ)
            ENDIF
            H(I,J,2)=HBAR+HPRM(1)*(1.-RFAC)
            U(I,J,2)=UBAR
            V(I,J,2)=VBAR
 30      CONTINUE
      ENDIF
C
C---BALANCE WINDS
      IF(IOPT.EQ.3)THEN
         DO 32 J=2,NYM1
            JP1=J+1
            JM1=J-1
         DO 32 I=2,NXM1
            IP1=I+1
            IM1=I-1
            RIJ=SQRT(FLOAT((ICNTR-I)*(ICNTR-I)+(JCNTR-J)*(JCNTR-J)))
            U(I,J,2)=-G/F(J)*(H(I,JP1,2)-H(I,JM1,2))*OV2DX
            V(I,J,2)=G/F(J)*(H(IP1,J,2)-H(IM1,J,2))*OV2DX
C
C---GRADIENT WIND CORRECTION (CYCLONIC)
CCC         IF(HPRM(1).LT.0.)THEN
               GSPD=SQRT(U(I,J,2)*U(I,J,2)+V(I,J,2)*V(I,J,2))
               IF(GSPD.NE.0.)THEN
                  RF=RIJ*DELX*F(J)
                  GRSPD=-RF*.5+SQRT(RF*RF*.25+RF*GSPD)
                  U(I,J,2)=U(I,J,2)*GRSPD/GSPD+UBAR
                  V(I,J,2)=V(I,J,2)*GRSPD/GSPD+VBAR
               ELSE
                  U(I,J,2)=UBAR
                  V(I,J,2)=VBAR
               ENDIF
C           ENDIF
 32      CONTINUE
      ENDIF
C
C---STEP FUNCTION
      IF(IOPT.EQ.4)THEN
         IBEG=ICNTR-KRAD
         IEND=ICNTR+KRAD
         JBEG=JCNTR-KRAD
         JEND=JCNTR+KRAD
         DO 50 J=1,JL
         DO 50 I=1,IL
            U(I,J,2)=UBAR
            V(I,J,2)=VBAR
 50         H(I,J,2)=HBAR
         DO 52 J=1,JL
         DO 52 I=1,IL
            IF(I.GT.IBEG.AND.I.LT.IEND
     &      .AND.J.GT.JBEG.AND.J.LT.JEND)H(I,J,2)=HBAR+HPRM(1)
 52      CONTINUE
      ENDIF
C
C---ANALYTIC SOLUTION (WHR  11/17/93)
      IF(IOPT.EQ.5)THEN
      ALPH=1./(4.*DELX)
      ALPH2=ALPH*ALPH
      VMAX=1
      DO 60 I=1,IL
      DO 60 J=1,JL
         U(I,J,2)=0.0
         V(I,J,2)=0.0
         H(I,J,2)=0.0
 60   CONTINUE
      NLOOP=2
      DO 62 IJK=1,NLOOP
         DELZ=HPRM(IJK)
         newicnt=icntr
         newjcnt=jcntr
         if(nloop .eq. 1)go to 64
c---seperation parameter
c        asep=8.
         if(ijk.eq.1)newicnt=icntr-asep-30.*ubar/abs(ubar+.01)
         if(ijk.eq.1)newjcnt=jcntr
         if(ijk.eq.2)newicnt=icntr+asep-30.*ubar/abs(ubar+.01)
         if(ijk.eq.2)newjcnt=jcntr
 64      continue
         jbot=newjcnt-1-5
         jtop=newjcnt+1-5
         DO 66 I=1,IL
         sum=0
         DO 68 J=1,JL
C        CC(i,j,2)=0.0
         sum=SUM+(-F(J)*UBAR/2.*DELX/G)
         iDIFXX=I-newICNT
         iDIFYY=J-newJCNT
         RADUS2=(IDIFXX*IDIFXX+IDIFYY*IDIFYY)*delx*delx
         SSI=-VMAX*F(J)*(1.-(1.+4.*G*(-DELZ)
     &      *ALPH2*EXP(-ALPH2*RADUS2/2.)
     &      /(F(J)*F(J)))**(0.5))/2.
         U(I,J,2)=u(i,j,2)+ubar/2.-(IDIFYY*DELX)*SSI
         V(I,J,2)=v(i,j,2)+vbar/2.+(IDIFXX*DELX)*SSI
         H(I,J,2)=h(i,j,2)+HBAR/2.+DELZ*EXP(-ALPH2*RADUS2/2.)+SUM
         u(i,j,1)=u(i,j,2)
         v(i,j,1)=v(i,j,2)
         h(i,j,1)=h(i,j,2)
C        if(j .ge. jbot .and. j .le. jtop)CC(i,j,2)=50.0
C        CC(i,j,1)=CC(i,j,2)
 68      CONTINUE
 66   CONTINUE
 62   CONTINUE
      ENDIF
C
C---INITIALIZE CONCENTRATION
C        jbot=jcntr-1-5
C        jtop=jcntr+1-5
C        DO 70 J=1,JL
C        DO 70 I=1,IL
C           CC(i,j,2)=3.0
C           if(j .ge. jbot .and. j .le. jtop)CC(i,j,2)=33.0
C70   CONTINUE
         CCMAX=30.
         CCMIN=3.
         DO 70 J=1,JL
            CVAL=CCMIN+FLOAT(JL-J+1)/FLOAT(JL)*(CCMAX-CCMIN)
         DO 70 I=1,IL
            CC(i,j,2)=CVAL
 70   CONTINUE
C
C---EAST-WEST BC
      DO 34 J=1,JL
         U(1,J,2)=U(2,J,2)
         U(IL,J,2)=U(NXM1,J,2)
         V(1,J,2)=V(2,J,2)
         V(IL,J,2)=V(NXM1,J,2)
 34   CONTINUE
C
C---NORTH-SOUTH BC
      DO 36 I=1,IL
         U(I,1,2)=U(I,2,2)
         U(I,JL,2)=U(I,NYM1,2)
         V(I,1,2)=V(I,2,2)
         V(I,JL,2)=V(I,NYM1,2)
 36   CONTINUE
C     CALL DUMPR('   H',H(1,1,2),IL,JL,1,IL,1,JL,1.,0.
C    2          ,1,FLAG,IVAL,6)
C     CALL DUMPR('   U',U(1,1,2),IL,JL,1,IL,1,JL,1.,0.
C    2          ,1,FLAG,IVAL,6)
C     CALL DUMPR('   V',V(1,1,2),IL,JL,1,IL,1,JL,1.,0.
C    2          ,1,FLAG,IVAL,6)
C
C---ASSIGN INITIAL FIELDS FOR FIRST TIMESTEP
      DO 72 J=1,JL
      DO 72 I=1,IL
         U(I,J,1)=U(I,J,2)
         V(I,J,1)=V(I,J,2)
         H(I,J,1)=H(I,J,2)
         CC(I,J,1)=CC(I,J,2)
         U(I,J,3)=U(I,J,2)
         V(I,J,3)=V(I,J,2)
         H(I,J,3)=H(I,J,2)
         CC(I,J,3)=CC(I,J,2)
 72   CONTINUE
C
      RETURN
      END
