
C      SUBROUTINE  GOESRTE_2
      SUBROUTINE  GOESRTE_2(GZEN, TSKIN, T_COPY, W_COPY,
     +                      O_COPY, P_COPY, WFN, TBCX)
C=============================================================
C   THIS PROGRAM COMPUTES WEIGHTING FUNCTIONS FROM STANDARD
C   ATMOSPHERE
C=============================================================
C
C     Adapted to VISAD by Paolo Antonelli
C     Last update 11 Nov 1997

      PARAMETER (NL=40,NC=25,NLM1=NL-1,INL=100)
      REAL TAU(NL)
      REAL PT,DT,PW,DW
C      REAL PP(NL),WFS(NL)
      REAL PLG(INL)
      REAL WF(NL)
C      REAL O_W,T_W,W_W
      REAL DBDT(NL)
C      INTEGER NCHNIN, NLIN
      REAL P_COPY(NL),T_COPY(NL),W_COPY(NL),O_COPY(NL)
      COMMON/PLNCGO/WNUM(NC),FK1(NC),FK2(NC),TC(2,NC)
      COMMON/ATMOS/P(NL),T(NL),W(NL),O(NL)
      COMMON/REFATM/PREF(NL),TREF(NL),WREF(NL),OREF(NL)
C
      PARAMETER(NCHN=18)
      REAL TBC(NCHN),WFN(NL,NCHN)
      REAL TBCX(NL,NCHN)
C      REAL TEMPX(NL,NCHN),MIXRX(NL,NCHN),OZONEX(NL,NCHN)
C
      DATA ISAT/8/
      DATA IS,PSFC,EMISS/40,1000.,1./
C
C---VISAD INTERFACE PARAMETERS
c     GZEN=35.
C      GZEN=ADREAL(1)
c     TSKIN=300.
C      TSKIN=ADREAL(2)
C      WRITE (6,*) 'TSKIN',TSKIN
C      CALL ADM1D(3, 1, NL, T, NLIN)
C      CALL ADM1D(3, 2, NL, W, NLIN)
C      CALL ADM1D(3, 3, NL, O, NLIN)
C      CALL ADM1D(3, 4, NL, P, NLIN)
C      CALL ADM2D(4, 1, NCHN*NL, WFN, NCHNIN, NLIN)
C added by WLH
       DO 1 K=1,NL
       P(K)=P_COPY(K)
       T(K)=T_COPY(K)
       W(K)=W_COPY(K)
1      O(K)=O_COPY(K)
C end of added by WLH
C
C---COMPUTE TEMPS OF STANDARD ATMOSPHERE USING PREF
      DO 30 K=1,NL
         PLG(K)=ALOG(P(K))
C        WRITE(6,330)K,P(K),T(K),W(K)
C330     FORMAT(1X,I2,' P,T,W:',3E13.5)
 30   CONTINUE
C
C---PERTURBATE T AND W PROFILES
      IF(PT.GE.2..AND.PT.LE.39..AND.DT.NE.0.)THEN
         ILEV=IFIX(PT+.5)
         IP1=ILEV+1
         IM1=ILEV-1
         T(IM1)=T(IM1)+(DT*.303)
         T(ILEV)=T(ILEV)+DT
         T(IP1)=T(IP1)+(DT*.303)
      ENDIF
      IF(PW.GE.2..AND.PW.LE.39..AND.DW.NE.0.)THEN
         ILEV=IFIX(PW+.5)
         IP1=ILEV+1
         IM1=ILEV-1
         W(IM1)=W(IM1)+(W(IM1)*DW*.01*.303)
         W(ILEV)=W(ILEV)+W(ILEV)*DW*.01
         W(IP1)=W(IP1)+(W(IM1)*DW*.01*.303)
      ENDIF
C
C---INITIALIZE CONSTANTS
      ISAT=8
      CALL PFCGIM(ISAT)
C
C---TEMPERATURE AND MOISTURE PROFILES ARE NOW INTERPOLATED TO
C   STANDARD 40 LEVEL ARRAY.
C
C---LOOP OVER CHANNELS
      DO 100 N=1,NCHN
      KCHAN=N
C     WRITE(6,'('' PROCESSING CHANNEL '',I2)')KCHAN
      CALL TAUGIM(T,W,O,GZEN,ISAT,KCHAN,TAU)
C     IF(IBUG.EQ.1)THEN
C        WRITE(6,300)(JS,TAU(JS),JS=1,NL)
C300     FORMAT(' TAU-',I2,1X,E12.4)
C     ENDIF
      CALL GIMRTE(TAU,T,TSKIN,RAD,DBDT,TBC(N),DB,KCHAN,IS,PSFC,
     * DBDTS,EMISS)
C
C---SET UP WEIGHTING FUNCTION
      TAUL=TAU(1)
      DO I=2,IS
         TAUN=AMIN1(TAU(I),TAUL)
         TAUL=TAUN
         TAU(I)=TAUN
      ENDDO
      DO I=1,IS
         TAU(I)=AMAX1(TAU(I),0.)
      ENDDO
C
C---FIND ORDINARY SHAPE FUNCTION
      ISIGN=1
      WFSAV=0.
      MAX=IS
      IF(MAX.EQ.NL)MAX=NLM1
      WF(1)=0.
      DO I=2,IS
         WF(I)=-(TAU(I)-TAU(I-1))/(PLG(I)-PLG(I-1))
      ENDDO
C
C---MULTIPLY THE TEMPERATURE DEPENDENCE
      DO I=2,NLM1
         WF(I)=WF(I)*DBDT(I)/DB
      ENDDO
      WF(1)=WF(2)*DBDT(1)/DB
      WF(IS)=WF(IS)*DBDTS/DB
      DO  I=2,IS
C
C---LOOK FOR MAX
         IF (WF(I) .GT. WFSAV) THEN
            WFSAV = WF(I)
            IMAX = I
         ENDIF
      ENDDO
      IF (IMAX .EQ. IS) IMAX=IMAX-1
C
C---DO NOT ALLOW SWITCH BACK BEFORE MAX
      DO I=2,IMAX
         IF (WF(I).LT.WF(I-1))WF(I)=0.5*(WF(I-1)+WF(I+1))
      ENDDO
      DO I=IS-1,IMAX
         IF (WF(I).LT.WF(I-1))WF(I)=0.5*(WF(I-1)+WF(I+1))
      ENDDO
C
C---AVERAGE EVERYBODY UP TO 10 PERCENT OF MAX
      WFLIM=0.1*WFSAV
      DO I=2,MAX
         IF (WF(I) .LE. WFLIM) THEN
            WF(I)=0.5*(WF(I-1)+WF(I+1))
         ENDIF
      ENDDO
C
C---LOOK FOR ZERO CROSSING AT TOP AND BOTTOM
      ITOP = 0
      DO I=11,IS
         IF (WF(I) .GT. 1.E-5) THEN
            IF (ITOP .EQ. 0) ITOP=I
         ENDIF
      ENDDO
      IF (ITOP .GT. 11) THEN
        ITOP=ITOP-1
      ENDIF
      IBOT = 0
      DO I=IS,1,-1
         IF (WF(I) .GT. 1.E-5) THEN
            IF (IBOT .EQ. 0) IBOT=I
         ENDIF
      ENDDO
      NIS=IBOT-ITOP+1
      DO I=1,IS
         WFN(I,N)=WF(I)
      ENDDO
 100  continue
      do 666 i=1,nl
      DO 666 J=1,NCHN
666   TBCX(i,J) = TBC(J)

C      CALL ADRM2D(4, 1, WFN, NCHNIN, NLIN)
C      CALL ADRM2D(4, 2, TBCX, NCHNIN, NLIN)
C
C      WRITE (6,*) 'TSKIN OUT',TSKIN
      RETURN
      END
      SUBROUTINE GIMRTE(TAU,TEMP,TSKIN,RAD,DBDT,TBB,DBDTBB,KCHAN,LSFC,
     * PSFC,DBDTS,EMISS)
c  12 dec 94  correct error in sfc tau extrapolation
C $ SUBROUTINE GIMRTE(TAU,TEMP,TSFC,RAD,DBDT,TBB,DBDTBB,KCHAN,LSFC,
C $   * PSFC,DBDTS,EMISS)
C $ INTEGRATE R-T-E AND GET PLANCK-WEIGHTING PARAMETERS
C $ TAU = (R) INPUT  PROFILE OF TRANSMITTANCE
C $ TEMP = (R) INPUT  TEMPERATURE PROFILE
C $ TSKIN = (R) INPUT  SURFACE SKIN TEMPERATURE
C $ RAD = (R) OUTPUT  RADIANCE
C $ KCHAN = (I) INPUT  CHANNEL NUMBER (SOUNDER = 1-18, IMAGER = 22-25)
C $ LSFC = (I) INPUT  LEVEL SUBSCRIPT AT OR BELOW SURFACE
C $ PSFC = (R) PRESSURE AT SURFACE (CLOUD)
C $ DBDT = (R) OUTPUT  PROFILE OF DERIVATIVE OF PLANCK RADIANCE
C $                     WITH RESPECT TO ATMOSPHERIC TEMPERATURE
C $ TBB = (R) OUTPUT  BLACKBODY TEMPERATURE
C $ DBDTBB = (R) OUTPUT  DERIVATIVE OF PLANCK RADIANCE W.R.T.TBB
C $ DBDTS = (R) OUTPUT  SURFACE DBDT
C $ EMISS = (R) INPUT  SURFACE EMISSIVITY
C $$ GIMRTO = COMPUTAT,SOUNDER,GOES
      PARAMETER (NC=25,NL=40)
      DIMENSION TAU(40),TEMP(40),DBDT(40)
      COMMON/ATMOS/P(NL),T(NL),W(NL),OZO(NL)
      COMMON/ATMRAD/ATRD,TAUS,REFL
      COMMON /PLNCGO/ WNUM(NC),FK1(NC),FK2(NC),TC(2,NC)
C      CHARACTER*12 CFI,CFF
C
C     PREPARE SURFACE CORRECTION
      DP = ALOG(PSFC/P(LSFC))/ALOG(P(LSFC)/P(LSFC-1))
C     DP IS NEGATIVE TO INTERPOLATE, POSITIVE TO EXRAPOLATE (PSFC>NL)
      DTAU = TAU(LSFC-1)-TAU(LSFC)
      TAUS = TAU(LSFC)-DTAU*DP
      REFL = 0.
      F1 = FK1(KCHAN)
      F2 = FK2(KCHAN)
      Q = F2/F1
      T1 = TEMP(1)
      B1 = PLANGO(T1,KCHAN)
      BB = B1*B1
      TT = T1*T1
      EX = Q*EXP(F2/T1)
      DBDT(1) = EX*BB/TT
      TAU1 = TAU(1)
      RAD = 0.
      DO 110 I = 2,LSFC
      T2 = TEMP(I)
      B2 = PLANGO(T2,KCHAN)
      BB = B2*B2
      TT = T2*T2
      EX = Q*EXP(F2/T2)
      DBDT(I) = EX*BB/TT
      TAU2 = TAU(I)
      DTAU = TAU1-TAU2
      DR = 0.5*(B1+B2)*DTAU
      RAD = RAD+DR
      IF (TAUS .GT. 0.1.AND.EMISS .LT. 1.00)THEN
C     DO NOT ADD REFLECTED FOR LAST LEVEL UNLESS PSFC .GT. 1050.
      IF (I .EQ. LSFC.AND.PSFC .LE. 1050.)GO TO 105
      TAUB = 0.5*(TAU1+TAU2)
      TAUFAC = TAUS/TAUB
      REFL = REFL+TAUFAC*TAUFAC*DR
      ENDIF
  105 B1 = B2
  110 TAU1 = TAU2
C     ADD (SUBTRACT) INCREMENT OF ATMOS RADIANCE TO REACH SURFACE
C     DP WILL BE NEGATIVE IF PSFC < 1000 MB
C     DR FALLS OUT AS THE DELTA RADIANCE OF LAYER
      RAD = RAD+DR*DP
C     ADD INCREMENT OF REFLECTED RADIANCE FOR LAYER DOWN TO SURFACE
      IF (TAUS .GT. 0.1.AND.EMISS .LT. 1.00)THEN
         IF (PSFC .LT. 1050.)THEN
         TAUB = 0.5*(TAU(LSFC-1)+TAUS)
C     CHANGE DP TO INCREMENT RATHER THAN DECREMENT
         DP = 1.+DP
         ELSE
         TAUB = 0.5*(TAU(LSFC)+TAUS)
         ENDIF
      TAUFAC = TAUS/TAUB
      REFL = REFL+TAUFAC*TAUFAC*DR*DP
      ENDIF
      ATRD = RAD
      RAD = RAD+(1.-EMISS)*REFL
      BS = PLANGO(TSKIN,KCHAN)
c     CALL SDEST('TSKIN IS '//CFF(DBLE(TSKIN),2),0)
c     CALL SDEST(' TAUS REFL ATRD BS'//CFF(DBLE(TAUS),3)
c    * //CFF(DBLE(REFL),3)//CFF(DBLE(ATRD),3)
c    * //CFF(DBLE(BS),3)//CFF(DBLE(EMISS),2),0)
      RAD = RAD+EMISS*BS*TAUS
      RAD = AMAX1(RAD,.001)
      TT = TSKIN*TSKIN
      BB = BS*BS
      EX = Q*EXP(F2/TSKIN)
      DBDTS = EX*BB/TT
      TBB = BRITGO(RAD,KCHAN)
      TT = TBB*TBB
      BB = RAD*RAD
      EX = Q*EXP(F2/TBB)
      DBDTBB = EX*BB/TT
      RETURN
      END
      subroutine taugim(t,w,o,theta,ngoes,kan,tau)
c * GOES/I-M TAU via McMillin-Fleming-Woolf-Eyre regression model
c   Input temperatures ('T'), water-vapor mixing ratios ('W'),
c        and ozone mixing ratios ('O') must be at the 'STANDARD'
c        40 levels used for radiative-transfer calculations.
c   If ozone profile is not available, pass a zero-filled array.  This
c        will cause the reference profile to be used.
c   Units -- T: degrees Kelvin; W: grams/kilogram; O: ppm by volume.
c   Logical unit number 15 is used for the coefficient file.
c      THETA = local (satellite) zenith angle in degrees
c      NGOES = GOES satellite number, e.g. 8 (GOES/I)
c        KAN = channel number (1,...,25)
c               1 thru 18 are SOUNDER
c              22 thru 25 are IMAGER
c   To get Planck-function, band correction, and TSKIN coefficients,
c    plus gammas, deltas, epsilons, and 'use' flags,
c        user *MUST* call PFCGIM!
c   Total transmittance is returned via the formal parameter TAU.
c   Separate DRY/WET/OZO transmittances are returned via COMMON.
c
      parameter (nc=3,nk=25,nl=40,nlm1=nl-1,nt=2,nv=20,nx=9,nxp1=nx+1)
      parameter (iuc=15,lenc=nxp1*nl*nc,lencb=lenc*4)
      common/refatm/pref(nl),tref(nl),wref(nl),oref(nl)
      common/taudwo/taud(nl),tauw(nl),tauo(nl)
      dimension t(nl),w(nl),o(nl),tau(nl),tl(nl),wl(nl),ol(nl)
      dimension delo(nl),delt(nl),delw(nl)
      dimension sumo(nl),sums(nl),sumt(nl),sumw(nl)
C      dimension cbuf(lenc)
      dimension dp(nl),prsq(nl),xx(nx,nl,nc)
      dimension coef(nxp1,nl,nc,nk),tauc(nl,nc),sqp(nc)
      dimension wdep(nl),sqw(nl),odep(nl),sqo(nl),sqd(nl),sqdep(nl,nc)
      equivalence (sqd(1),sqdep(1,1)),(taud(1),tauc(1,1))
      equivalence (sqw(1),sqdep(1,2)),(tauw(1),tauc(1,2))
      equivalence (sqo(1),sqdep(1,3)),(tauo(1),tauc(1,3))
C      character*8 cfile/'GOESRTCF'/
      character*8 cfile/'goesrtcf'/
c
      logical oldatm,oldang
      data tl/nl*0./,wl/nl*0./,ol/nl*0./,init/0/
      data sqd/nl*1./,trap/-999.99/
      secant(z) = 1./cos(0.01745329*z)
c
      if(kan.gt.18.and.kan.lt.22) go to 200
      if(init.eq.ngoes) go to 100
      open(iuc,file=cfile,recl=lencb,access='direct',status='old')
      irec=(ngoes-8)*nk
      do l=1,nk
         irec=irec+1
         read(iuc,rec=irec) (((coef(i,j,k,l),i=1,nxp1),j=1,nl),k=1,nc)
      enddo
      close(iuc)
      prsq(1)=pref(1)**2
      dp(1)=pref(1)
      do j=1,nlm1
         jp1=j+1
         prsq(jp1)=pref(jp1)**2
         dp(jp1)=pref(jp1)-pref(j)
      enddo
      thetl=trap
      init=ngoes
c
  100 dt=0.
      dw=0.
      do=0.
      if(o(1).eq.0.) then
         do j=1,nl
            o(j)=oref(j)
         enddo
      endif
      do j=1,nl
crma     dt=dt+abs(t(j)-tl(j))
         dt=dt+t(j)-tl(j)
         tl(j)=t(j)
crma     dw=dw+abs(w(j)-wl(j))
         dw=dw+w(j)-wl(j)
         wl(j)=w(j)
crma     do=do+abs(o(j)-ol(j))
         do=do+o(j)-ol(j)
         ol(j)=o(j)
      enddo
      dtwo=dt+dw+do
      oldatm=dtwo.eq.0.
      if(oldatm) go to 110
      st=0.
      ss=0.
      sw=0.
      so=0.
      do j=1,nl
         delt(j)=t(j)-tref(j)
         delw(j)=w(j)-wref(j)
         delo(j)=o(j)-oref(j)
         odep(j)=dp(j)*o(j)
         wdep(j)=dp(j)*w(j)
         if(j.gt.1) then
            jm1=j-1
            delt(j)=0.5*(delt(j)+t(jm1)-tref(jm1))
            delw(j)=0.5*(delw(j)+w(jm1)-wref(jm1))
            delo(j)=0.5*(delo(j)+o(jm1)-oref(jm1))
            odep(j)=dp(j)*0.5*(o(j)+o(jm1))
            if(j.gt.nv) then
               wdep(j)=dp(j)*0.5*(w(j)+w(jm1))
            else
               wdep(j)=wdep(jm1)+dp(j)*0.5*(w(j)+w(jm1))
            endif
         endif
         sqo(j)=sqrt(odep(j))
         sqw(j)=sqrt(wdep(j))
         dtdp=delt(j)*dp(j)
         st=st+dtdp
         sumt(j)=st/pref(j)
         ss=ss+dtdp*pref(j)
         sums(j)=2.*ss/prsq(j)
         dwdp=delw(j)*dp(j)
         sw=sw+dwdp*pref(j)
         sumw(j)=2.*sw/prsq(j)
         dodp=delo(j)*dp(j)
         so=so+dodp*pref(j)
         sumo(j)=2.*so/prsq(j)
      enddo
  110 oldang=theta.eq.thetl
      if(.not.oldang) then
         if(theta.eq.0.) then
            path=1.
            sqrtp=1.
         else
            path=secant(theta)
            sqrtp=sqrt(path)
         endif
         pathm1=path-1.
         sqp(1)=1.
         sqp(2)=sqrtp
         sqp(3)=sqrtp
         thetl=theta
      endif
      if(oldatm.and.oldang) go to 120
      do j=1,nl
         dt=delt(j)
         ss=sums(j)
         st=sumt(j)
c **** DRY
         xx(1,j,1)=dt*path
         xx(2,j,1)=dt*dt*path
         xx(3,j,1)=st*path
         xx(4,j,1)=ss*path
         xx(5,j,1)=pathm1
         xx(6,j,1)=pathm1*pathm1
         xx(7,j,1)=st*pathm1
         xx(8,j,1)=ss*pathm1
         xx(9,j,1)=dt*pathm1
c **** WET
         sqpw=sqrtp*sqw(j)
         dw=delw(j)
         sw=sumw(j)
         xx(1,j,2)=dt
         xx(2,j,2)=ss
         xx(3,j,2)=dw
         xx(4,j,2)=sw
         xx(5,j,2)=dt*sqpw
         xx(6,j,2)=dt*dt*sqpw
         xx(7,j,2)=dw*sqpw
         xx(8,j,2)=dw*dw*sqpw
         xx(9,j,2)=dw*dt*sqpw
c **** OZO
         sqpo=sqrtp*sqo(j)
         do=delo(j)
         so=sumo(j)
         xx(1,j,3)=dt
         xx(2,j,3)=ss
         xx(3,j,3)=do
         xx(4,j,3)=so
         xx(5,j,3)=dt*sqpo
         xx(6,j,3)=dt*dt*sqpo
         xx(7,j,3)=do*sqpo
         xx(8,j,3)=do*do*sqpo
         xx(9,j,3)=do*dt*sqpo
      enddo
  120 do k=1,nc
         do j=1,nl
            tauc(j,k)=1.0
         enddo
      enddo
c
      l=kan
      do k=1,nc
         taul=1.
         do 130 j=1,nl
         if(taul.eq.0.) go to 130
         yy=coef(nxp1,j,k,l)
         if(yy.eq.trap) then
            taul=0.
            go to 130
         endif
         do i=1,nx
            yy=yy+coef(i,j,k,l)*xx(i,j,k)
         enddo
         yy=amin1(yy,0.)
         tauy=taul*exp(yy*sqdep(j,k)*sqp(k))
         taul=tauy
  130    tauc(j,k)=taul
      enddo
      do j=1,nl
         tau(j)=taud(j)*tauw(j)*tauo(j)
      enddo
  200 return
      end
      block data refpro
c $ Transmittance-Model Reference = U.S. Standard Atmosphere, 1976
      parameter (nl=40)
      common/refatm/pref(nl),tref(nl),wref(nl),oref(nl)
      data pref/ .1,.2,.5,1.,1.5,2.,3.,4.,5.,7.,10.,15.,20.,25.,30.,
     + 50.,60.,70.,85.,100.,115.,135.,150.,200.,250.,300.,350.,400.,
     + 430.,475.,500.,570.,620.,670.,700.,780.,850.,920.,950.,1000./
      data tref/
     +  231.70, 245.22, 263.35, 270.63, 264.07, 257.93, 249.51, 243.65,
     +  239.24, 232.64, 228.07, 225.00, 223.13, 221.72, 220.54, 217.28,
     +  216.70, 216.70, 216.70, 216.70, 216.70, 216.70, 216.70, 216.72,
     +  220.85, 228.58, 235.38, 241.45, 244.81, 249.48, 251.95, 258.32,
     +  262.48, 266.40, 268.61, 274.21, 278.74, 282.97, 284.71, 287.50/
      data wref/
     +   0.003,  0.003,  0.003,  0.003,  0.003,  0.003,  0.003,  0.003,
     +   0.003,  0.003,  0.003,  0.003,  0.003,  0.003,  0.003,  0.003,
     +   0.003,  0.003,  0.003,  0.003,  0.003,  0.004,  0.005,  0.014,
     +   0.036,  0.089,  0.212,  0.331,  0.427,  0.588,  0.699,  1.059,
     +   1.368,  1.752,  1.969,  2.741,  3.366,  3.976,  4.255,  4.701/
c    +   0.004,  0.005,  0.005,  0.005,  0.005,  0.005,  0.005,  0.005,
c    +   0.005,  0.005,  0.005,  0.005,  0.005,  0.004,  0.004,  0.004,
c    +   0.004,  0.004,  0.004,  0.004,  0.005,  0.006,  0.008,  0.022,
c    +   0.057,  0.143,  0.340,  0.533,  0.687,  0.946,  1.125,  1.704,
c    +   2.202,  2.819,  3.168,  4.411,  5.416,  6.397,  6.846,  7.564/
      data oref/
     + 0.65318,1.04797,2.13548,3.82386,5.26768,6.11313,7.35964,7.75004,
     + 7.82119,7.56126,6.92006,6.10266,5.55513,5.15298,4.59906,2.86792,
     + 2.29259,1.80627,1.28988,0.93973,0.72277,0.54848,0.46009,0.29116,
     + 0.16277,0.09861,0.06369,0.05193,0.04718,0.04097,0.03966,0.03614,
     + 0.03384,0.03342,0.03319,0.03249,0.03070,0.02878,0.02805,0.02689/
      end
      subroutine pfcgim(ngoes)
c * Input GOES/I-M Planck-function, band-cor'n & TSKIN coeff's
c        plus gammas, deltas, epsilons, and 'use' flags
c        NGOES = GOES satellite number, e.g. 8 (GOES/I)
      parameter (iuc=15,lenc=1200,nk=25,nt=2,lenp=nk*(nt+3),lent=30)
      parameter (leng=nk*3,lenu=nk*nt,lenr=lenc*4)
      parameter (mbg=200,mbu=300,nrc=20)
      common/gimgde/gbuf(leng)
      common/plncgo/pbuf(lenp)
      common/tskcof/tbuf(lent)
      common/use/ibuf(lenu)
      dimension cbuf(lenc)
C      character*8 cfile/'GOESRTCF'/
      character*8 cfile/'goesrtcf'/
c
      open(iuc,file=cfile,recl=lenr,access='direct',status='old')
      irc=(ngoes-8)*nk+nrc
      read(iuc,rec=irc) cbuf
      close(iuc)
      do l=1,lenp
         pbuf(l)=cbuf(l)
      enddo
      m=lenp
      do l=1,lent
         m=m+1
         tbuf(l)=cbuf(m)
      enddo
      m=mbg
      do l=1,leng
         m=m+1
         gbuf(l)=cbuf(m)
      enddo
      m=mbu
      do l=1,lenu
         m=m+1
         ibuf(l)=cbuf(m)
      enddo
      return
      end
      FUNCTION BRITGO(R,K)
C $ GOES/I-M BRIGHTNESS TEMPERATURE FROM RADIANCE IN CHANNEL 'K' (HMW)
C * CALL 'COPCOF'TO LOAD COMMON FROM MDB  BEFORE USING THIS OR 'PLANGO'
C      OR 'DBDTGO'
      COMMON/PLNCGO/WNUM(25),FK1(25),FK2(25),TC(2,25)
      EXPN=FK1(K)/R+1.
      TT=FK2(K)/ALOG(EXPN)
      BRITGO=(TT-TC(1,K))/TC(2,K)
      RETURN
      END
      FUNCTION PLANGO(T,K)
C $ GOES/I-M RADIANCE FROM BRIGHTNESS TEMPERATURE IN CHANNEL 'K' (HMW)
C * TO LOAD COMMON BEFORE USING THIS OR 'BRITGO' OR 'DBDTGO'
      COMMON/PLNCGO/WNUM(25),FK1(25),FK2(25),TC(2,25)
      TT=TC(1,K)+TC(2,K)*T
      EXPN=EXP(FK2(K)/TT) - 1.
      PLANGO=FK1(K)/EXPN
      RETURN
      END

