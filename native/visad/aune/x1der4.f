      SUBROUTINE X1DER4(A,DDX,DDY,IL,JL)
C===================================================================
C   THIS ROUTINE COMPUTES FIRST DERIVATIVES USING 4TH ORDER CENTERED
C===================================================================
C
      REAL A(IL,*),DDX(IL,*),DDY(IL,*)
      COMMON/SCALAR/DELX,DELT,DELT2,OV12DX,OV2DX,OVDX2
     &             ,NXM1,NXM2,NXM3,NXM4,NXM5,NXM6
     &             ,NYM1,NYM2,NYM3,NYM4,NYM5,NYM6
     &             ,I1,I2,I3,I4,I5,J1,J2,J3,J4,J5
C
      DO 10 J=3,NYM2
         JP1=J+1
         JM1=J-1
         JP2=J+2
         JM2=J-2
      DO 10 I=3,NXM2
         DDX(I,J)=OV12DX*(8.*(A(I+1,J)-A(I-1,J))-(A(I+2,J)-A(I-2,J)))
         DDY(I,J)=OV12DX*(8.*(A(I,JP1)-A(I,JM1))-(A(I,JP2)-A(I,JM2)))
 10   CONTINUE
      DO 20 J=2,NYM1
         JP1=J+1
         JM1=J-1
         DDX(2,J)=OV2DX*(A(3,J)-A(1,J))
         DDY(2,J)=OV2DX*(A(2,JP1)-A(2,JM1))
         DDX(NXM1,J)=OV2DX*(A(IL,J)-A(NXM2,J))
         DDY(NXM1,J)=OV2DX*(A(NXM1,JP1)-A(NXM1,JM1))
 20   CONTINUE
      DO 30 I=2,NXM1
         IP1=I+1
         IM1=I-1
         DDX(I,2)=OV2DX*(A(IP1,2)-A(IM1,2))
         DDY(I,2)=OV2DX*(A(I,3)-A(I,1))
         DDX(I,NYM1)=OV2DX*(A(IP1,NYM1)-A(IM1,NYM1))
         DDY(I,NYM1)=OV2DX*(A(I,JL)-A(I,NYM2))
 30   CONTINUE
C
      RETURN
	end
