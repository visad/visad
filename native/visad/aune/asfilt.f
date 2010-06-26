      SUBROUTINE ASFILT(U,V,H,CC,IL,JL,TFILT,TFILTM)
C==============================================
C   APPLY ASSELIN TIME FILTER
C==============================================
C
      REAL U(IL,JL,*),V(IL,JL,*),H(IL,JL,*),CC(IL,JL,*)
C
      DO 80 J=1,JL
      DO 80 I=1,IL
         U(I,J,2)=TFILT*U(I,J,2)+TFILTM*(U(I,J,3)+U(I,J,1))
         V(I,J,2)=TFILT*V(I,J,2)+TFILTM*(V(I,J,3)+V(I,J,1))
         H(I,J,2)=TFILT*H(I,J,2)+TFILTM*(H(I,J,3)+H(I,J,1))
         CC(I,J,2)=TFILT*CC(I,J,2)+TFILTM*(CC(I,J,3)+CC(I,J,1))
 80   CONTINUE
C
      RETURN
      END
