
C       SUBROUTINE SO_READ_1
       SUBROUTINE SO_READ_1(I, T, TD, P, Z)
C******************************************************
C
C      THIS SUBROUTINE MUST BE COMPILED WITH EXTERNF_IO
C
C******************************************************
C
C      Paolo 05 Sep 1997
C      SO_READ_1 reads in sounder profiles
C
C******************************************************
       IMPLICIT NONE


       INTEGER DAT
       PARAMETER (dat=22)
       REAL T(dat),TD(dat),P(dat),Z(dat)
C       REAL RT(dat)
       REAL tempT(dat),tempTD(dat),tempP(dat),tempZ(dat)
C       REAL data_b(dat)
C       REAL ADREAL
       INTEGER i
C       INTEGER j

       write (6,*) 'reading data'
C       I=INT(ADREAL(1))
       open (51,file='data_sou_1.dat',form='unformatted',
     & access='direct',status='old',recl=352)
C     & access='direct',status='old',recl=88)
       write (6,*) 'i =',i
       read (51, rec=i) T,TD,P,Z
       write (6,*) 'done profile'

       do i=0,dat-1
         tempT(dat-i)=T(i+1)
         tempTD(dat-i)=TD(i+1)
         tempP(dat-i)=P(i+1)
         tempZ(dat-i)=Z(i+1)
       enddo !i
       do i=1,dat
         T(i)=tempT(i)
         TD(i)=tempTD(i)
         P(i)=tempP(i)
         Z(i)=tempZ(i)
       enddo !i
       close(51)
C       write(6,*) (T(j),j=1,dat)
C       CALL ADRM1D (2,1,T,dat)
C       CALL ADRM1D (2,2,TD,dat)
C       CALL ADRM1D (2,3,P,dat)
C       CALL ADRM1D (2,4,Z,dat)

       RETURN
       END

