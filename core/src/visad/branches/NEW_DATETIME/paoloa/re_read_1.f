
C       SUBROUTINE RE_READ_1
       SUBROUTINE RE_READ_1(I, data_b)
C**************************************************************
C
C      THIS SUBROUTINE MUST BE COMPILED WITH EXTERNF_IO
C
C**************************************************************
C
C      Paolo 05 Sep 1997
C      RE_READ_1 reads in the observed brightness temperatures
C
C**************************************************************
       IMPLICIT NONE


       INTEGER DAT
       PARAMETER (dat=2661)
       REAL LAT,G01
       REAL G02,G03,G04,G05,G06
       REAL G07,G08,G09,G10,G11
       REAL G12,G13,G14,G15,G16
       REAL G17,G18,data_b(19)
C       REAL ADREAL
       INTEGER i,j

C       write (6,*) 'reading BT data',i
C       I=INT(ADREAL(1))
       open (51,file='data_obs_1.dat',form='unformatted',
     & access='direct',status='old',recl=76)
C     & access='direct',status='old',recl=19)
C       write (6,*) 'file open'
       read (51, rec=i) LAT,G01,G02,G03,G04,G05,G06,G07,G08,G09,
     &            G10,G11,G12,G13,G14,G15,G16,G17,G18
C       write (6,*) 'i =',i
C       write (6,*) 'lat =',LAT
C       write (6,*) 'done BT data'
       data_b(1)=G01
       data_b(2)=G02
       data_b(3)=G03
       data_b(4)=G04
       data_b(5)=G05
       data_b(6)=G06
       data_b(7)=G07
       data_b(8)=G08
       data_b(9)=G09
       data_b(10)=G10
       data_b(11)=G11
       data_b(12)=G12
       data_b(13)=G13
       data_b(14)=G14
       data_b(15)=G15
       data_b(16)=G16
       data_b(17)=G17
       data_b(18)=G18
       data_b(19)=LAT

       close(51)
C       write(6,*) (data_b(j),j=1,18)
C       write(6,*) 'LAT  ',data_b(19)
C       CALL ADR1D (2,data_b,19)

       RETURN
       END

