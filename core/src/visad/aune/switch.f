      SUBROUTINE SWITCH(U,V,H,CC,IL,JL)
C============================================================
C   THIS ROUTINE ASSIGNS UPDATED FIELDS TO ARRAY INDEX 1
C============================================================
C
      REAL U(IL,JL,*),V(IL,JL,*),H(IL,JL,*),CC(IL,JL,*)
C
      DO 10 J=1,JL
      DO 10 I=1,IL
         U(I,J,1)=U(I,J,2)
         U(I,J,2)=U(I,J,3)
         V(I,J,1)=V(I,J,2)
         V(I,J,2)=V(I,J,3)
         H(I,J,1)=H(I,J,2)
         H(I,J,2)=H(I,J,3)
         CC(I,J,1)=CC(I,J,2)
         CC(I,J,2)=CC(I,J,3)
 10   CONTINUE
C
      RETURN
      END
