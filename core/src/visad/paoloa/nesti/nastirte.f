      SUBROUTINE NAST_I_RTE( TSKIN, PSFC, LSFC, AZEN, P, T, W, O,
     $                       NBUSE, VN_OUT, TB_OUT, RR_OUT )

C ?  NAST-I Forward Model (RTE)
C ?  7 Feb/98 : Input file is "output of sat2ac.f"
C ?   Res   IFGM scan regions (cm) in band 1; band 2; band 3
C ?         0-2.07 (2199); 0-2.07(3858); 0-2.07(3070)
      PARAMETER (NB=3,NL=40,MAXCHA=3858)
      PARAMETER (LENG=145)
C     REAL*8 VN, VN_OUT(9127), DWN(NB),FREQ(NB,MAXCHA),VXNAST
      REAL*8 VN, VN_OUT(*), DWN(NB),FREQ(NB,MAXCHA),VXNAST
C     REAL*8 TB_OUT(9127), RR_OUT(9127)
      REAL*8 TB_OUT(*), RR_OUT(*)
      INTEGER cnt
Crink DIMENSION BUF(LENG)
      DIMENSION TAUT(NL),NBUSE(NB),RRMIN(NB)
      DIMENSION KDO(NB),TB(NB,MAXCHA),NCH(NB)
      DIMENSION P(NL),T(NL),W(NL),O(NL)
      DIMENSION LUO(NB),LUIA(NB),LUIB(NB)
      COMMON /taudwo/TAUD(NL),TAUW(NL),TAUZ(NL)
Crink CHARACTER*64 TBF(NB)
      DATA KDO/NB*1/
      DATA LUO/11,12,13/,LUI/15/
      DATA LUIA/24,25,26/,LUIB/27,28,29/
      DATA NCH/2199,3858,3070/
      DATA RRMIN/1.,0.01,0.0001/
Crink DATA    P/50.,60.,70.,75.,80.,85.,90.,100.,125.,150.,175.,200.,
Crink*   250.,300.,350.,400.,450.,500.,550.,600.,620.,640.,660.,680.,
Crink*   700.,720.,740.,760.,780.,800.,820.,840.,860.,880.,900.,920.,
Crink*   940.,960.,980.,1000./
Cpaolo      WRITE(*,'('' ENTER INPUT PROFILE FILE : '')')
Cpaolo      READ(*,'(A)') PROFF
Cpaolo      WRITE(*,*) PROFF
Cpaolo      WRITE(*,'('' ENTER BEGINING NUMBER OF RECORD TO PROCESS :'')')
Cpaolo      READ(*,*) NBEG
Cpaolo      WRITE(*,*) NBEG
Cpaolo      WRITE(*,'('' ENTER ENDING NUMBER OF RECORD TO PROCESS :'')')
Cpaolo      READ(*,*) NREC
Cpaolo     WRITE(*,*) NREC
Cpaolo      write(*,'('' ENTER A/C LEVEL PRESSURE (MB) :'')')
Cpaolo      READ(*,*) ACP
Cpaolo      WRITE(*,*) ACP
Cpaolo      WRITE(*,'('' ENTER BAND USE FALG (0 for NOT USED) :'')')
Cpaolo      READ(*,*) NBUSE
Cpaolo      WRITE(*,*) NBUSE
Cpaolo      WRITE(*,'('' ENTER OUTPUT Brightness Tem FILE (all 3 Bands): ''
Cpaolo     $ )')
Cpaolo      READ(*,'(A)') TBF(1)
Cpaolo      WRITE(*,*) TBF(1)
Cpaolo      READ(*,'(A)') TBF(2)
Cpaolo      WRITE(*,*) TBF(2)
Cpaolo      READ(*,'(A)') TBF(3)
Cpaolo      WRITE(*,*) TBF(3)
Crink PROFF='../data/camx97ax.two.psfc'
      do 72 I = 1, NL
        print *, P(I), T(I), W(I), O(I)
72    enddo
      print *, NBUSE(1)
      print *, NBUSE(2)
      print *, NBUSE(3)

      NBEG=1
      NREC=1
      ACP=50
C     NBUSE(1)=1
C     NBUSE(2)=1
C     NBUSE(3)=1
C     TBF(1)='../data/test.b1'
C     TBF(2)='../data/test.b2'
C     TBF(3)='../data/test.b3'
       VN=VXNAST(0,0)
      DO N=1,NB
      DO K=1,NCH(N)
       FREQ(N,K)=VXNAST(N,K)
      ENDDO
       DWN(N)=FREQ(N,2)-FREQ(N,1)
       WRITE(*,'(''BAND '',I2,'' WNBEG;WNEND;DWN&NCH :'',3F9.2,i6)')
     $ N,FREQ(N,1),FREQ(N,NCH(N)),DWN(N),NCH(N)
      ENDDO
      LENI=LENG*4
Crink OPEN(LUI,RECL=LENI,FILE=PROFF,STATUS='OLD',ACCESS='DIRECT')
C     DO N=1,NB
C     IF(NBUSE(N).NE.0) THEN
C     LENO=NCH(N)*4
C     OPEN(LUO(N),RECL=LENO,FILE=TBF(N),STATUS='UNKNOWN',
C    $ ACCESS='DIRECT')
C     ENDIF
Crink ENDDO
      NRX=0
c      DO 180 NR=1,NREC

Cpaolo Read in T,WV,O profile

Crink DO 180 NR=NBEG,NREC
Crink READ(LUI,REC=NR) (BUF(J),J=1,LENG)
C     DO L=1,NL
C      T(L)=BUF(L)
C      W(L)=BUF(NL+L)
C      O(L)=BUF(2*NL+L)
C     ENDDO
C     TSKIN=BUF(121)
C     PSFC=BUF(122)
C     if(PSFC.gt.1000.) PSFC=1000.
C     LSFC=lsurface ( nl, p, psfc, 700., 1000. )
C     AZEN=BUF(140)
C     WRITE(*,'('' RECORD='',I8,'' Psfc;TSKIN;ZEN;Lsfc='',3f9.3,i6)')
C    $  NR,PSFC,TSKIN,AZEN,LSFC
C     IF(MOD(NR,100).EQ.1) THEN
C     write(*,'('' pressure  temperature  water vapor     ozone'')')
C     do l=1,nl
C      write(*,'(1x,f8.1,2x,f11.2,2x,f11.3,2x,f11.4)')
C    $ p(l),t(l),w(l),o(l)
C     enddo
Crink ENDIF
C
      NR = 1
      cnt = 1
      DO 150 N=1,NB
      IF(NBUSE(N).NE.0) THEN
       DO 130 K=1,NCH(N)
         VN=FREQ(N,K)
C * DO RADIATIVE-TRANSFER CALCULATIONS
	CALL TRANNAST(N,K,KDO,ACP,AZEN,T,W,O, TAUT,*200)
        RR=WNMRAD(VN,TAUT,T,TSKIN,LSFC)
        IF(RR.LE.RRMIN(N)) THEN
         RR=RRSAV
        ELSE
         RRSAV=RR
        ENDIF
        TB(N,K)=WNBRIT(VN,RR)
C       if(nr.eq.1) write(*,'(1x,2i7,F9.3,3f10.3)')n,k,VN,TB(N,K),RR
        VN_OUT(cnt) = VN
        TB_OUT(cnt) = TB(N,K)*1.D0
        RR_OUT(cnt) = RR*1.D0
        cnt = cnt + 1
  130 CONTINUE
      ENDIF
  150 CONTINUE
       NRX=NRX+1

Cpaolo TBs are the output Bri. Temp.

       DO 170 N=1,NB
      IF(NBUSE(N).NE.0) THEN
C      WRITE(LUO(N),REC=NRX) (TB(N,K),K=1,NCH(N))
       IF(MOD(NRX,100).EQ.1) THEN
        WRITE(*,'('' RECORD='',I8,'' TSKIN='',f9.3)') NRX,TSKIN
        WRITE(*,'(1X,9F9.3)') (TB(N,K),K=1,9)
        WRITE(*,'(1X,9F9.3)') (TB(N,K),K=NCH(N)-8,NCH(N))
       ENDIF
      ENDIF
  170 CONTINUE
Cr180 CONTINUE
      GO TO 300
  200 CONTINUE
      WRITE(*,'('' ERROR IN READING TRANNAST !!'')')
  300 CONTINUE
C     DO N=1,NB
C     IF(NBUSE(N).NE.0) THEN
C     WRITE(*,'(1X,I6,'' RECORDS WROTE TO FILE '',A48)') NRX,TBF(N)
C     CLOSE(LUO(N))
C     ENDIF
Crink ENDDO
      RETURN
      END
c
c
