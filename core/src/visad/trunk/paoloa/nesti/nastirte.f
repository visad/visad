      SUBROUTINE NAST_I_RTE( TSKIN, PSFC, LSFC, AZEN, P, T, W, O, 
     $                       NBUSE, VN_OUT, TB_OUT, RR_OUT ) 

C ?  NAST-I Forward Model (RTE) 
C ?  7 Feb/98 : Input file is "output of sat2ac.f" 
C ?   Res   IFGM scan regions (cm) in band 1; band 2; band 3
C ?         0-2.07 (2199); 0-2.07(3858); 0-2.07(3070)
      PARAMETER (NB=3,NL=40,MAXCHA=3858)
      PARAMETER (LENG=145)
      REAL*8 VN, VN_OUT(9127), DWN(NB),FREQ(NB,MAXCHA),VXNAST
      REAL*8 TB_OUT(9127), RR_OUT(9127)
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
        TB_OUT(cnt) = TB(N,K)
        RR_OUT(cnt) = RR
        cnt = cnt + 1
  130 CONTINUE
      ENDIF
  150 CONTINUE
       NRX=NRX+1

Cpaolo TBs are the output Bri. Temp. 

       DO 170 N=1,NB
      IF(NBUSE(N).NE.0) THEN 
       WRITE(LUO(N),REC=NRX) (TB(N,K),K=1,NCH(N)) 
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
C **
	real*8 function vxnast(iban,ipnt)
c * NAST-I: wavenumber for given band and point
c .... version of 24.02.98

c .... FIRST CALL MUST BE with iban,ipnt = 0,0 to initialize!

	implicit real*8 (v)
	parameter (nban=3)
	parameter (vlas=15799.d0,vnum=65536.d0)
	parameter (vdel=vlas/vnum)
	common/nastvn/vbeg(nban),vend(nban),npts(nban)
	dimension vnb(nban),vne(nban)
	data vnb/620.d0,1150.d0,2080.d0/
	data vne/1149.9d0,2079.9d0,2820.d0/

	if((iban*ipnt).eq.0) then
	   do kb=1,nban
	      vb=vnb(kb)/vdel
	      nb=vb
	      vbeg(kb)=vdel*dfloat(nb+1)
	      ve=vne(kb)/vdel
	      ne=ve
	      np=ne-nb
	      vend(kb)=vbeg(kb)+vdel*dfloat(np)
	      npts(kb)=np+1
	   enddo
	   vxnast=0.d0
	   return
	endif

	if(ipnt.gt.npts(iban)) then
	   write(*,'('' VXNAST: point '',i4,
     *             '' is out of range ...'')') ipnt
	   write(*,'(9x,''maximum for band '',i1,'' is '',i4)')
     *             iban,npts(iban)
	   write(*,'(10x,''A value of 0. is being returned!'')')
	   vxnast=0.d0
	   return
	endif

	vxnast=vbeg(iban)+vdel*dfloat(ipnt-1)
	return
	end
C **
      subroutine trannast(iban,ipib,kcom,apre,azen,
     *                    temp,wvmr,ozmr, taut,*)
c * NAST-I dry/wet/ozo transmittance
c .... version of 26.02.98
c		iban = band (1,2,3)
c		ipib = point within band
c			maximum per band: 1/2199; 2/3858; 3/3070
c			if out of range, all tau-arrays are filled with 1.0
c		kcom = component-transmittance switch ... see NOTE 2
c		apre = aircraft flight-level pressure ... see NOTE 1
c		azen = local zenith angle in degrees
c		temp = temperature profile (degK)
c		wvmr = water-vapor mixing-ratio profile (g/kg)
c		ozmr = ozone mixing-ratio profile (ppmv)
c		taut = total transmittance .............. see NOTE 3
c		   * = alternate return, taken if file(s) not available

c * Strow-Woolf-VanDelst regression model based on LBLRTM line-by-line transmittances.
c	Input temperatures, and water-vapor and ozone mixing ratios, must be defined
c	at the pressure levels in array 'pstd' ... see 'block data acrefatm'.

c	Logical-unit numbers 40-99 are used for coefficient files ... see NOTE 1.

c * NOTE 1
c	There are four sets of coefficient files, corresponding to
c	'top-of-the-atmosphere' pressures of 50, 60, 70, and 75 mb.
c	'apre', the pressure in millibars at the aircraft altitude,
c	is used to select the appropriate set of files.

c * NOTE 2
c	kcom = 0 => no contribution from component => tau(p) = 1.0
c	       1 => calculate contribution from given distribution
c	kcom(1) is for dry, result in 'taud'
c	kcom(2) is for wet, result in 'tauw'
c	kcom(3) is for ozo, result in 'tauo'

c  NOTE 3
c     Component tau's are returned through common;
c     their product is returned in 'taut'.

      parameter (nb=3,nk=3,mk=5,nl=40,nm=nl-1,nt=4,lfac=4)
      parameter (nxc= 4,ncc=nxc+1,lencc=ncc*nm,lenccb=lencc*lfac)
      parameter (nxd= 8,ncd=nxd+1,lencd=ncd*nm,lencdb=lencd*lfac)
      parameter (nxo= 9,nco=nxo+1,lenco=nco*nm,lencob=lenco*lfac)
      parameter (nxl= 2,ncl=nxl+1,lencl=ncl*nm,lenclb=lencl*lfac)
      parameter (nxs=11,ncs=nxs+1,lencs=ncs*nm,lencsb=lencs*lfac)
      parameter (nxw=nxl+nxs)
      common/stdatm/pstd(nl),tstd(nl),wstd(nl),ostd(nl)
      common/taudwo/taud(nl),tauw(nl),tauo(nl)
      dimension kcom(*),temp(*),wvmr(*),ozmr(*),taut(*)
      dimension coefd(ncd,nm),coefo(nco,nm),coefl(ncl,nm)
	dimension coefs(ncs,nm),coefc(ncc,nm)
	dimension pavg(nm),tref(nm),wref(nm),oref(nm)
	dimension tavg(nm),wamt(nm),oamt(nm),secz(nm)
	dimension tauc(nl),tlas(nl),wlas(nl),olas(nl)
	dimension xdry(nxd,nm),xozo(nxo,nm),xwet(nxw,nm),xcon(nxc,nm)
      dimension iud(nb,nt),iuc(mk,nb,nt)
      character*12 cfile/'nastbcom.tpp'/
      character*3 comp(mk)/'dry','ozo','wts','wtl','wco'/
      character*2 ctop(nt)/'50','60','70','75'/
	character*1 cban
	integer kend(nb)/2199,3858,3070/
	integer kind(nk)/1,3,2/
      integer length(mk)/lencdb,lencob,lencsb,lenclb,lenccb/
      data init/1/,iud/40,45,50, 55,60,65, 70,75,80, 85,90,95/
      logical here,newang,newatm,openc(mk,nb,nt)
	data tlas/nl*0./,wlas/nl*0./,olas/nl*0./,zlas/-999./
	secant(z)=1./cos(0.01745329*z)

      if(init.ne.0) then
         do k=1,nt
            do j=1,nb
               iux=iud(j,k)-1
               do i=1,mk
                  openc(i,j,k)=.false.
                  iux=iux+1
                  iuc(i,j,k)=iux
               enddo
            enddo
         enddo
         call conpir(pstd,tstd,wstd,ostd,nl,1,pavg,tref,wref,oref)
         init=0
	endif

c * use aircraft pressure to select coefficient files
	if(apre.lt.60.) then
	   itop=1
      elseif(apre.ge.60..and.apre.lt.70.) then
	   itop=2
      elseif(apre.ge.70..and.apre.lt.75.) then
	   itop=3
      else
	   itop=4
      endif
	ntop=itop+4

	write(cban,'(i1)') iban
      cfile(05:05)=cban
      cfile(11:12)=ctop(itop)
      do 100 icom=1,mk
         if(openc(icom,iban,itop)) go to 100
         openc(icom,iban,itop)=.true.
         cfile(6:8)=comp(icom)
         inquire(file=cfile,exist=here)
         if(.not.here) go to 200
         iucc=iuc(icom,iban,itop)
         lencf=length(icom)
         open(iucc,file=cfile,recl=lencf,access='direct',status='old',
     *        err=200)
100	continue

	dt=0.
	dw=0.
	do=0.
      do j=1,nl
	   dt=dt+abs(temp(j)-tlas(j))
	   tlas(j)=temp(j)
	   dw=dw+abs(wvmr(j)-wlas(j))
	   wlas(j)=wvmr(j)
	   do=do+abs(ozmr(j)-olas(j))
	   olas(j)=ozmr(j)
         taud(j)=1.0
         tauw(j)=1.0
         tauc(j)=1.0
         tauo(j)=1.0
         taut(j)=1.0
      enddo
	datm=dt+dw+do
	newatm=datm.ne.0.
	if(newatm) then
         call conpir(pstd,temp,wvmr,ozmr,nl,1,pavg,tavg,wamt,oamt)
	endif

	newang=azen.ne.zlas
	if(newang) then
	   zsec=secant(azen)
	   do l=1,nm
	      secz(l)=zsec
	   enddo
	   zlas=azen
	endif

	if(newang.or.newatm) then
	   call calpir(tref,wref,oref,tavg,wamt,oamt,pavg,secz,
     *	 nm,nxd,nxw,nxo,nxc,xdry,xwet,xozo,xcon)
	endif

	if(ipib.gt.kend(iban)) return
	krec=ipib

c * dry
      l=1
      if(kcom(l).ne.0) then
	   k=kind(l)
  	   read(iuc(k,iban,itop),rec=krec)((coefd(i,j),i=1,ncd),j=1,nm)
         call taudoc(ncd,nxd,nm,coefd,xdry,taud)
      endif

c * ozo
      l=3
      if(kcom(l).ne.0) then
	   k=kind(l)
	   read(iuc(k,iban,itop),rec=krec)((coefo(i,j),i=1,nco),j=1,nm)
         call taudoc(nco,nxo,nm,coefo,xozo,tauo)
      endif

c * wet
      l=2
      if(kcom(l).ne.0) then
	   k=kind(l)
	   read(iuc(k,iban,itop),rec=krec)((coefs(i,j),i=1,ncs),j=1,nm)
         k=k+1
	   read(iuc(k,iban,itop),rec=krec)((coefl(i,j),i=1,ncl),j=1,nm)
c ..... other than continuum
         call tauwtr(ncs,ncl,nxs,nxl,nxw,nm,coefs,coefl,xwet,tauw)
         k=k+1
	   read(iuc(k,iban,itop),rec=krec)((coefc(i,j),i=1,ncc),j=1,nm)
c ..... continuum only
         call taudoc(ncc,nxc,nm,coefc,xcon,tauc)
c ..... total water vapor
	   do j=1,nl
	      tauw(j)=tauw(j)*tauc(j)
	   enddo
      endif

c * total
      do j=1,nl
         taut(j)=taud(j)*tauo(j)*tauw(j)
      enddo
      return
  200 return1
      end

      block data acrefatm
      parameter (nl=40)
c * Reference Atmosphere is 1976 U.S. Standard
      common/stdatm/pstd(nl),tstd(nl),wstd(nl),ostd(nl)
      data pstd/50.,60.,70.,75.,80.,85.,90.,100.,125.,150.,175.,200.,
     *   250.,300.,350.,400.,450.,500.,550.,600.,620.,640.,660.,680.,
     *   700.,720.,740.,760.,780.,800.,820.,840.,860.,880.,900.,920.,
     *   940.,960.,980.,1000./
      data tstd/
     +  217.28, 216.70, 216.70, 216.70, 216.70, 216.70, 216.70, 216.70,
     +  216.70, 216.70, 216.71, 216.72, 220.85, 228.58, 235.38, 241.45,
     +  246.94, 251.95, 256.58, 260.86, 262.48, 264.08, 265.64, 267.15,
     +  268.61, 270.07, 271.49, 272.87, 274.21, 275.54, 276.85, 278.12,
     +  279.36, 280.59, 281.79, 282.97, 284.14, 285.28, 286.40, 287.50/
      data wstd/
     +   0.002,  0.002,  0.002,  0.002,  0.002,  0.002,  0.002,  0.002,
     +   0.003,  0.005,  0.010,  0.014,  0.035,  0.089,  0.211,  0.331,
     +   0.500,  0.699,  0.961,  1.248,  1.368,  1.525,  1.678,  1.825,
     +   1.969,  2.170,  2.365,  2.556,  2.741,  2.925,  3.105,  3.280,
     +   3.456,  3.633,  3.806,  3.975,  4.162,  4.346,  4.525,  4.701/
      data ostd/
     + 2.86792,2.29259,1.80627,1.62277,1.45112,1.28988,1.16673,0.93973,
     + 0.63214,0.46009,0.36957,0.29116,0.16277,0.09861,0.06369,0.05193,
     + 0.04434,0.03966,0.03710,0.03474,0.03384,0.03367,0.03350,0.03334,
     + 0.03319,0.03301,0.03283,0.03266,0.03249,0.03196,0.03145,0.03095,
     + 0.03042,0.02986,0.02931,0.02878,0.02829,0.02781,0.02735,0.02689/
      end
C **
      function wnmrad(vn,tau,tem,ts,ls)
c * monochromatic radiance calculation
      real*8 vn
      dimension tau(*),tem(*)
      tau1=tau(1)
      t1=tem(1)
      b1=wnplan(vn,t1)
      rad=0.
      do i=2,ls
         tau2=tau(i)
         t2=tem(i)
         b2=wnplan(vn,t2)
         rad=rad+.5*(b1+b2)*(tau1-tau2)
         tau1=tau2
         b1=b2
      enddo
	if(ts.ne.0.) then
         bsts=wnplan(vn,ts)*tau1
         rad=rad+bsts
	endif
      wnmrad=rad
      return
      end
C **
      function wnbrit(vn,rad)
c * Radiance to brightness temperature
      implicit real*8 (a-h,o-z)
      real rad,wnbrit
      parameter (h = 6.626176d-27, c = 2.997925d+10, b = 1.380662d-16)
      parameter (c1 = 2.d0*h*c*c)
      parameter (c2 = h*c/b)
      tnb(x,y,z)=y/dlog(x/z+1.d0)
c
      f1=c1*vn**3
      f2=c2*vn
      r=rad
      tbb=tnb(f1,f2,r)
      wnbrit=tbb
      return
      end
c **** SUBROUTINES CALLED BY 'TRANNAST'

      subroutine calpir(t_avg_ref,  amt_wet_ref, amt_ozo_ref,
     +                        t_avg,      amt_wet,     amt_ozo,
     +                        p_avg,      sec_theta,   n_layers,
     +                        n_dry_pred, n_wet_pred,  n_ozo_pred,
     +                        n_con_pred,
     +                        pred_dry,   pred_wet,    pred_ozo,
     +                        pred_con)
c ... version of 19.09.96

c  PURPOSE:

c    Routine to calculate the predictors for the dry (temperature), 
c      wet and ozone components of a fast transmittance model for a
c      scanning satellite based instrument.

c  REFERENCES:

c    AIRS FTC package science notes and software, S. Hannon and L. Strow,
c      Uni. of Maryland, Baltimore County (UMBC)

c  CREATED:

c    19-Sep-1996 HMW

c  ARGUMENTS:

c      Input
c    -----------
c     t_avg_ref  - REAL*4 reference layer average temperature array (K)

c    amt_wet_ref - REAL*4 reference water vapour amount array (k.mol)/cm^2

c    amt_ozo_ref - REAL*4 reference ozone amount array (k.mol)/cm^2

c      t_avg     - REAL*4 layer average temperature array (K)

c     amt_wet    - REAL*4 water vapour amount array (k.mol)/cm^2

c     amt_ozo    - REAL*4 ozone amount array (k.mol)/cm^2

c      p_avg     - REAL*4 layer average pressure array (mb)

c    sec_theta   - REAL*4 secant of the zenith angle array

c     n_layers   - INT*4 Number of atmospheric layers

c    n_dry_pred  - INT*4 number of dry (temperature) predictors

c    n_wet_pred  - INT*4 number of water vapour predictors

c    n_ozo_pred  - INT*4 number of ozone predictors

c    n_con_pred  - INT*4 number of water vapour continuum predictors

c      Output
c    -----------
c     pred_dry   - REAL*4 dry gas (temperature) predictor matrix

c     pred_wet   - REAL*4 water vapour predictor matrix

c     pred_ozo   - REAL*4 ozone predictor matrix

c     pred_con   - REAL*4 water vapour continuum predictor matrix

c  COMMENTS:

c    Levels or Layers?
c    -----------------
c      Profile data is input at a number of *LAYERS*.

c    Layer Numbering pt. A
c    ---------------------
c      Layer 1   => Atmosphere between LEVELs 1 & 2
c      Layer 2   => Atmosphere between LEVELs 2 & 3
c                        .
c                        .
c                        .
c      Layer L-1 => Atmosphere between LEVELs L-1 & L

c    Layer Numbering pt. B
c    ---------------------
c      For the HIS instrument, Layer 1 is at the top of the atmosphere
c        and Layer L-1 is at the surface.    

c    Layer Numbering pt. C
c    ---------------------
c      In this routine the number of *LAYERS* is passed in the argument
c        list, _not_ the number of LEVELS.  This was done to improve
c        the readability of this code, i.e. loop from 1->L(ayers) 
c        rather than from 1->L(evels)-1.

c=======================================================================

c-----------------------------------------------------------------------
c                 Turn off implicit type declaration
c-----------------------------------------------------------------------

       implicit none

c------------------------------------------------------------------------
c                             Arguments
c------------------------------------------------------------------------

c -- Input

      integer*4 n_layers,
     +          n_dry_pred, n_wet_pred, n_ozo_pred, n_con_pred

      real*4    t_avg_ref(*), amt_wet_ref(*), amt_ozo_ref(*),
     +          t_avg(*),     amt_wet(*),     amt_ozo(*),
     +          p_avg(*),     sec_theta(*)

c -- Output

      real*4    pred_dry(n_dry_pred, *),
     +          pred_wet(n_wet_pred, *),
     +          pred_ozo(n_ozo_pred, *),
     +          pred_con(n_con_pred, *)

c------------------------------------------------------------------------
c                           Local variables
c------------------------------------------------------------------------

c -- Parameters

      integer*4 max_layers
      parameter ( max_layers = 41 )

      integer*4 max_dry_pred, max_wet_pred, max_ozo_pred, max_con_pred
      parameter ( max_dry_pred = 8,
     +            max_wet_pred = 13,
     +            max_ozo_pred = 9,
     +            max_con_pred = 4 )

c -- Scalars

      integer*4 l

c -- Arrays

c     ....Pressure
      real*4    p_dp(max_layers),
     +          p_norm(max_layers)

c     ....Temperature
      real*4    delta_t(max_layers),
     +          t_ratio(max_layers),
     +          pw_t_ratio(max_layers)      ! Pressure weighted

c     ....Water vapour
      real*4    wet_ratio(max_layers),
     +          pw_wet(max_layers),         ! Pressure weighted
     +          pw_wet_ref(max_layers),     ! Pressure weighted
     +          pw_wet_ratio(max_layers)    ! Pressure weighted

c     ....Ozone
      real*4    ozo_ratio(max_layers), 
     +          pw_ozo_ratio(max_layers),   ! Pressure weighted
     +          pow_t_ratio(max_layers)     ! Pressure/ozone weighted

c************************************************************************
c                         ** Executable code **
c************************************************************************

c------------------------------------------------------------------------
c                   -- Check that n_layers is o.k. --
c------------------------------------------------------------------------

      if( n_layers .gt. max_layers )then
        write(*,'(/10x,''*** calpir : n_layers > max_layers'')')
        stop
      end if 

c------------------------------------------------------------------------
c         -- Check that numbers of predictors is consistent --
c------------------------------------------------------------------------

c     ---------------------------------
c     # of dry (temperature) predictors
c     ---------------------------------

      if( n_dry_pred .ne. max_dry_pred )then
        write(*,'(/10x,''*** calpir : invalid n_dry_pred'')')
        stop
      end if 

c     ----------------------------
c     # of water vapour predictors
c     ----------------------------

      if( n_wet_pred .ne. max_wet_pred )then
        write(*,'(/10x,''*** calpir : invalid n_wet_pred'')')
        stop
      end if 

c     ---------------------
c     # of ozone predictors
c     ---------------------

      if( n_ozo_pred .ne. max_ozo_pred )then
        write(*,'(/10x,''*** calpir : invalid n_ozo_pred'')')
        stop
      end if 

c     --------------------------------------
c     # of water vapour continuum predictors
c     --------------------------------------

      if( n_con_pred .ne. max_con_pred )then
        write(*,'(/10x,''*** calpir : invalid n_con_pred'')')
        stop
      end if 

c------------------------------------------------------------------------
c         -- Calculate ratios, offsets, etc, for top layer --
c------------------------------------------------------------------------

c     ------------------
c     Pressure variables
c     ------------------

      p_dp(1)   = p_avg(1) * ( p_avg(2) - p_avg(1) )
      p_norm(1) = 0.0

c     ---------------------
c     Temperature variables
c     ---------------------

      delta_t(1)    = t_avg(1) - t_avg_ref(1)
      t_ratio(1)    = t_avg(1) / t_avg_ref(1)
      pw_t_ratio(1) = 0.0

c     ----------------
c     Amount variables
c     ----------------

c     ....Water vapour
 
      wet_ratio(1)    = amt_wet(1) / amt_wet_ref(1)
      pw_wet(1)       = p_dp(1) * amt_wet(1)
      pw_wet_ref(1)   = p_dp(1) * amt_wet_ref(1)
      pw_wet_ratio(1) = wet_ratio(1)

c     ....Ozone

      ozo_ratio(1)    = amt_ozo(1) / amt_ozo_ref(1)
      pw_ozo_ratio(1) = 0.0
      pow_t_ratio(1)  = 0.0

c------------------------------------------------------------------------
c         -- Calculate ratios, offsets, etc, for all layers --
c------------------------------------------------------------------------

      do l = 2, n_layers

c       ------------------
c       Pressure variables
c       ------------------

        p_dp(l) = p_avg(l) * ( p_avg(l) - p_avg(l-1) )
        p_norm(l) = p_norm(l-1) + p_dp(l)

c       ---------------------
c       Temperature variables
c       ---------------------

        delta_t(l)    = t_avg(l) - t_avg_ref(l)
        t_ratio(l)    = t_avg(l) / t_avg_ref(l)
        pw_t_ratio(l) = pw_t_ratio(l-1) + ( p_dp(l) * t_ratio(l-1) )

c       ----------------
c       Amount variables
c       ----------------

c       ..Water vapour

        wet_ratio(l)  = amt_wet(l) / amt_wet_ref(l)
        pw_wet(l)     = pw_wet(l-1) + ( p_dp(l) * amt_wet(l) )
        pw_wet_ref(l) = pw_wet_ref(l-1) + ( p_dp(l) * amt_wet_ref(l) )
        
c       ..Ozone

        ozo_ratio(l)    = amt_ozo(l) / amt_ozo_ref(l)
        pw_ozo_ratio(l) = pw_ozo_ratio(l-1) +
     +                      ( p_dp(l) * ozo_ratio(l-1) )
        pow_t_ratio(l)  = pow_t_ratio(l-1) +
     +                      ( p_dp(l) * ozo_ratio(l-1) * delta_t(l-1) )

      end do

c------------------------------------------------------------------------
c              -- Scale the pressure dependent variables --
c------------------------------------------------------------------------

      do l = 2, n_layers

        pw_t_ratio(l)   = pw_t_ratio(l) / p_norm(l)
        pw_wet_ratio(l) = pw_wet(l) / pw_wet_ref(l)
        pw_ozo_ratio(l) = pw_ozo_ratio(l) / p_norm(l)
        pow_t_ratio(l)  = pow_t_ratio(l) / p_norm(l)
 
      end do

c------------------------------------------------------------------------
c                     -- Load up predictor arrays --
c------------------------------------------------------------------------

      do l = 1, n_layers

c       ----------------------
c       Temperature predictors
c       ----------------------

        pred_dry(1,l) = sec_theta(l)
        pred_dry(2,l) = sec_theta(l) * sec_theta(l)
        pred_dry(3,l) = sec_theta(l) * t_ratio(l)
        pred_dry(4,l) = pred_dry(3,l) * t_ratio(l)
        pred_dry(5,l) = t_ratio(l)
        pred_dry(6,l) = t_ratio(l) * t_ratio(l)
        pred_dry(7,l) = sec_theta(l) * pw_t_ratio(l)
        pred_dry(8,l) = pred_dry(7,l) / t_ratio(l) 

c       -----------------------
c       Water vapour predictors
c       -----------------------

        pred_wet(1,l)  = sec_theta(l) * wet_ratio(l)
        pred_wet(2,l)  = sqrt( pred_wet(1,l) )
        pred_wet(3,l)  = pred_wet(1,l) * delta_t(l)
        pred_wet(4,l)  = pred_wet(1,l) * pred_wet(1,l)
        pred_wet(5,l)  = abs( delta_t(l) ) * delta_t(l) * pred_wet(1,l)
        pred_wet(6,l)  = pred_wet(1,l) * pred_wet(4,l)
        pred_wet(7,l)  = sec_theta(l) * pw_wet_ratio(l)
        pred_wet(8,l)  = pred_wet(2,l) * delta_t(l)
        pred_wet(9,l)  = sqrt( pred_wet(2,l) )
        pred_wet(10,l) = pred_wet(7,l) * pred_wet(7,l)
        pred_wet(11,l) = sqrt( pred_wet(7,l) )
        pred_wet(12,l) = pred_wet(1,l)
        pred_wet(13,l) = pred_wet(2,l)

c       ----------------
c       Ozone predictors
c       ----------------

        pred_ozo(1,l) = sec_theta(l) * ozo_ratio(l)
        pred_ozo(2,l) = sqrt( pred_ozo(1,l) )
        pred_ozo(3,l) = pred_ozo(1,l) * delta_t(l)
        pred_ozo(4,l) = pred_ozo(1,l) * pred_ozo(1,l)
        pred_ozo(5,l) = pred_ozo(2,l) * delta_t(l)
        pred_ozo(6,l) = sec_theta(l) * pw_ozo_ratio(l)
        pred_ozo(7,l) = sqrt( pred_ozo(6,l) ) * pred_ozo(1,l)
        pred_ozo(8,l) = pred_ozo(1,l) * pred_wet(1,l)
        pred_ozo(9,l) = sec_theta(l) * pow_t_ratio(l) * pred_ozo(1,l)

c       ---------------------------------
c       Water vapour continuum predictors
c       ---------------------------------

        pred_con(1,l) = sec_theta(l) * wet_ratio(l) /
     *	 	    ( t_ratio(l) * t_ratio(l) )  
        pred_con(2,l) = pred_con(1,l) * pred_con(1,l) / sec_theta(l)
        pred_con(3,l) = sec_theta(l) * wet_ratio(l) / t_ratio(l) 
        pred_con(4,l) = pred_con(3,l) * wet_ratio(l)

      end do
         
      return
      end
ccccccccc
      subroutine conpir( p, t, w, o, n_levels, i_dir,
     +                         p_avg, t_avg, w_amt, o_amt)
c ... version of 19.09.96

c  PURPOSE:

c    Function to convert atmospheric water vapour (g/kg) and ozone (ppmv)
c      profiles specified at n_levels layer BOUNDARIES to n_levels-1
c      integrated layer amounts of units (k.moles)/cm^2.  The average
c      LAYER pressure and temperature are also returned.

c  REFERENCES:

c    AIRS LAYERS package science notes, S. Hannon and L. Strow, Uni. of
c      Maryland, Baltimore County (UMBC)

c  CREATED:

c    19-Sep-1996 HMW

c  ARGUMENTS:

c     Input
c    --------
c       p     - REAL*4 pressure array (mb)

c       t     - REAL*4 temperature profile array (K)

c       w     - REAL*4 water vapour profile array (g/kg)

c       o     - REAL*4 ozone profile array (ppmv)

c    n_levels - INT*4 number of elements used in passed arrays

c     i_dir   - INT*4 direction of increasing layer number

c                 i_dir = +1, Level(1) == p(top)         } satellite/AC
c                             Level(n_levels) == p(sfc)  }    case

c                 i_dir = -1, Level(1) == p(sfc)         } ground-based
c                             Level(n_levels) == p(top)  }    case

c     Output
c    --------
c     p_avg   - REAL*4 average LAYER pressure array (mb)

c     t_avg   - REAL*4 average LAYER temperature (K)

c     w_amt   - REAL*4 integrated LAYER water vapour amount array (k.moles)/cm^2

c     o_amt   - REAL*4 integrated LAYER ozone amount array (k.moles)/cm^2

c  ROUTINES:

c    Subroutines:
c    ------------
c      gphite      - calculates geopotential height given profile data.

c    Functions:
c    ----------
c      NONE

c  COMMENTS:

c    Levels or Layers?
c    -----------------
c      Profile data is input at a number of *LEVELS*.  Number densitites
c        are calculated for *LAYERS* that are bounded by these levels.
c        So, for L levels there are L-1 layers.

c    Layer Numbering
c    ---------------
c      Layer 1   => Atmosphere between LEVELs 1 & 2
c      Layer 2   => Atmosphere between LEVELs 2 & 3
c                        .
c                        .
c                        .
c      Layer L-1 => Atmosphere between LEVELs L-1 & L

c=======================================================================

c-----------------------------------------------------------------------
c              -- Prevent implicit typing of variables --
c-----------------------------------------------------------------------

      implicit none

c-----------------------------------------------------------------------
c                           -- Arguments --
c-----------------------------------------------------------------------

c -- Arrays

      real*4    p(*), t(*), w(*), o(*), 
     +          p_avg(*), t_avg(*), w_amt(*), o_amt(*)

c -- Scalars

      integer*4 n_levels, i_dir

c-----------------------------------------------------------------------
c                         -- Local variables --
c-----------------------------------------------------------------------

c -- Parameters

      integer*4 max_levels
      parameter ( max_levels = 50 )         ! Maximum number of layers

      real*4    r_equator, r_polar, r_avg
      parameter ( r_equator = 6.378388e+06, ! Earth radius at equator
     +            r_polar   = 6.356911e+06, ! Earth radius at pole
     +            r_avg     = 0.5*(r_equator+r_polar) )

      real*4    g_sfc
      parameter ( g_sfc = 9.80665 )         ! Gravity at surface

      real*4    rho_ref
      parameter ( rho_ref = 1.2027e-12 )    ! Reference air "density"

      real*4    mw_dryair, mw_h2o, mw_o3
      parameter ( mw_dryair = 28.97,        ! Molec. wgt. of dry air (g/mol)
     +            mw_h2o    = 18.0152,      ! Molec. wgt. of water
     +            mw_o3     = 47.9982 )     ! Molec. wgt. of ozone

      real*4    R_gas, R_air
      parameter ( R_gas = 8.3143,           ! Ideal gas constant (J/mole/K)
     +            R_air = 0.9975*R_gas )    ! Gas constant for air (worst case) 

c -- Scalars

      integer*4 l, l_start, l_end, l_indx

      real*4    rho1, rho2, p1, p2, w1, w2, o1, o2, z1, z2,
     +          c_avg, g_avg, z_avg, w_avg, o_avg,
     +          dz, dp, r_hgt, wg, og, A, B

c -- Arrays

      real*4    z(max_levels),              ! Pressure heights (m)
     +          g(max_levels),              ! Acc. due to gravity (m/s/s)
     +          mw_air(max_levels),         ! Molec. wgt. of air (g/mol)
     +          rho_air(max_levels),        ! air mass density (kg.mol)/m^3
     +          c(max_levels),              ! (kg.mol.K)/(N.m)
     +          w_ppmv(max_levels)          ! h2o LEVEL amount (ppmv)

c***********************************************************************
c                         ** Executable code **
c***********************************************************************

c-----------------------------------------------------------------------
c           -- Calculate initial values of pressure heights --
c-----------------------------------------------------------------------

      call gphite( p, t, w, 0.0, n_levels, i_dir, z)

c-----------------------------------------------------------------------
c      -- Set loop bounds for direction sensitive calculations --
c      -- so loop iterates from surface to the top             --
c-----------------------------------------------------------------------

      if( i_dir .gt. 0 )then

c       --------------------
c       Data stored top down
c       --------------------

        l_start = n_levels
        l_end   = 1

      else

c       ---------------------
c       Data stored bottom up
c       ---------------------

        l_start = 1
        l_end   = n_levels

      end if

c-----------------------------------------------------------------------
c          -- Air molecular mass and density, and gravity --
c          -- as a function of LEVEL                      --
c-----------------------------------------------------------------------

c     -----------------------
c     Loop from bottom to top
c     -----------------------

      do l = l_start, l_end, -1*i_dir

c       ---------------------------------
c       Convert water vapour g/kg -> ppmv
c       ---------------------------------

        w_ppmv(l) = 1.0e+03 * w(l) * mw_dryair / mw_h2o

c       -----------------------------------------
c       Calculate molecular weight of air (g/mol)
c       ----------------------------------------

        mw_air(l) = ( ( 1.0 - (w_ppmv(l)/1.0e+6) ) * mw_dryair ) +
     +              ( ( w_ppmv(l)/1.0e+06 ) * mw_h2o )

c       ----------------
c       Air mass density
c       ----------------

        c(l) = 0.001 * mw_air(l) / R_air    ! 0.001 factor for g -> kg
        rho_air(l) = c(l) * p(l) / t(l)

c       -------
c       Gravity
c       -------

        r_hgt = r_avg + z(l)                !  m
        g(l) = g_sfc -                      !  m/s^2
     +         g_sfc*( 1.0 - ( (r_avg*r_avg)/(r_hgt*r_hgt) ) )

      end do
 
c-----------------------------------------------------------------------
c                        -- LAYER quantities --
c-----------------------------------------------------------------------

c     -----------------------
c     Loop from bottom to top
c     -----------------------

      do l = l_start, l_end+i_dir, -1*i_dir

c       -------------------------------------------------------
c       Determine output array index.  This is done so that the
c       output data is always ordered from 1 -> L-1 regardless
c       of the orientation of the input data.  This is true by
c       default only for the bottom-up case.  For the top down
c       case no correction would give output layers from 2 -> L
c       -------------------------------------------------------

        if( i_dir .gt. 0 )then

          l_indx = l - 1

        else

          l_indx = l

        end if

c       ---------------------------------------
c       Assign current layer boundary densities
c       ---------------------------------------
 
        rho1 = rho_air(l)
        rho2 = rho_air(l-i_dir)
 
c       ---------
c       Average c
c       ---------

        c_avg = ( (rho1*c(l)) + (rho2*c(l-i_dir)) ) / ( rho1 + rho2 )

c       ---------
c       Average t
c       ---------

        t_avg(l_indx) = 
     +          ( (rho1*t(l)) + (rho2*t(l-i_dir)) ) / ( rho1 + rho2 )

c       ---------
c       Average p
c       ---------

        p1 = p(l)
        p2 = p(l-i_dir)

        z1 = z(l)
        z2 = z(l-i_dir)

        dp = p2 - p1

        A = log(p2/p1) / (z2-z1)
        B = p1 / exp(A*z1)

        p_avg(l_indx) = dp / log(p2/p1)

c       ------------------------------------------------
c       LAYER thickness (rather long-winded as it is not
c       assumed the layers are thin) in m. Includes
c       correction for altitude/gravity.
c       ------------------------------------------------

c       ...Initial values
        g_avg = g(l)
        dz = -1.0 * dp * t_avg(l_indx) / ( g_avg*c_avg*p_avg(l_indx) )

c       ...Calculate z_avg
        z_avg = z(l) + ( 0.5*dz )

c       ...Calculate new g_avg
        r_hgt = r_avg + z_avg 
        g_avg = g_sfc - g_sfc*( 1.0 - ( (r_avg*r_avg)/(r_hgt*r_hgt) ) )

c       ...Calculate new dz
        dz = -1.0 * dp * t_avg(l_indx) / ( g_avg*c_avg*p_avg(l_indx) )

c       ----------------------------------------
c       Calculate LAYER amounts for water vapour
c       ----------------------------------------

        w1 = w_ppmv(l)
        w2 = w_ppmv(l-i_dir)

        w_avg =  ( (rho1*w1) + (rho2*w2) ) / ( rho1+rho2 )

        w_amt(l_indx) =
     +       rho_ref * w_avg * dz * p_avg(l_indx) / t_avg(l_indx)

c       ---------------------------------
c       Calculate LAYER amounts for ozone
c       ---------------------------------

        o1 = o(l)
        o2 = o(l-i_dir)

        o_avg =  ( (rho1*o1) + (rho2*o2) ) / ( rho1+rho2 )

        o_amt(l_indx) = 
     +       rho_ref * o_avg * dz * p_avg(l_indx) / t_avg(l_indx)

      end do

      return
      end
ccccccccc
      subroutine gphite( p, t, w, z_sfc, n_levels, i_dir, z)
c ... version of 19.09.96

c PURPOSE:

c  Routine to compute geopotential height given the atmospheric state.
c    Includes virtual temperature adjustment.

c CREATED:

c  19-Sep-1996 Received from Hal Woolf

c  ARGUMENTS:

c     Input
c    --------
c       p     - REAL*4 pressure array (mb)

c       t     - REAL*4 temperature profile array (K)

c       w     - REAL*4 water vapour profile array (g/kg)

c     z_sfc   - REAL*4 surface height (m).  0.0 if not known.

c    n_levels - INT*4 number of elements used in passed arrays

c     i_dir   - INT*4 direction of increasing layer number

c                 i_dir = +1, Level(1) == p(top)         } satellite/AC
c                             Level(n_levels) == p(sfc)  }    case

c                 i_dir = -1, Level(1) == p(sfc)         } ground-based
c                             Level(n_levels) == p(top)  }    case

c     Output
c    --------
c       z     - REAL*4 pressure level height array (m)

c COMMENTS:

c   Dimension of height array may not not be the same as that of the
c     input profile data.

c=======================================================================

c-----------------------------------------------------------------------
c              -- Prevent implicit typing of variables --
c-----------------------------------------------------------------------

      implicit none

c-----------------------------------------------------------------------
c                           -- Arguments --
c-----------------------------------------------------------------------

c -- Arrays

      real*4    p(*), t(*), w(*), 
     +          z(*)

c -- Scalars

      integer*4 n_levels, i_dir

      real*4    z_sfc

c-----------------------------------------------------------------------
c                         -- Local variables --
c-----------------------------------------------------------------------

c -- Parameters

      real*4    rog, fac
      parameter ( rog = 29.2898, 
     +            fac = 0.5 * rog )

c -- Scalars

      integer*4 i_start, i_end, l

      real*4    v_lower, v_upper, algp_lower, algp_upper, hgt

c***********************************************************************
c                         ** Executable code **
c***********************************************************************

c-----------------------------------------------------------------------
c  -- Calculate virtual temperature adjustment and exponential       --
c  -- pressure height for level above surface.  Also set integration --
c  -- loop bounds                                                    --
c-----------------------------------------------------------------------

      if( i_dir .gt. 0 )then

c       --------------------
c       Data stored top down
c       --------------------

        v_lower = t(n_levels) * ( 1.0 + ( 0.00061 * w(n_levels) ) )

        algp_lower = alog( p(n_levels) )

        i_start = n_levels-1
        i_end   = 1

      else

c       ---------------------
c       Data stored bottom up
c       ---------------------

        v_lower = t(1) * ( 1.0 + ( 0.00061 * w(1) ) )

        algp_lower = alog( p(1) )

        i_start = 2
        i_end   = n_levels

      end if

c-----------------------------------------------------------------------
c                     -- Assign surface height --
c-----------------------------------------------------------------------

      hgt = z_sfc

c-----------------------------------------------------------------------
c             -- Loop over layers always from sfc -> top --
c-----------------------------------------------------------------------

      do l = i_start, i_end, -1*i_dir

c       ----------------------------------------------------
c       Apply virtual temperature adjustment for upper level
c       ----------------------------------------------------

        v_upper = t(l)
        if( p(l) .ge. 300.0 )
     +    v_upper = v_upper * ( 1.0 + ( 0.00061 * w(l) ) )

c       ----------------------------------------------------- 
c       Calculate exponential pressure height for upper layer
c       ----------------------------------------------------- 

        algp_upper = alog( p(l) )

c       ----------------
c       Calculate height
c       ----------------

        hgt = hgt + ( fac*(v_upper+v_lower)*(algp_lower-algp_upper) )

c       -------------------------------
c       Overwrite values for next layer
c       -------------------------------

        v_lower = v_upper
        algp_lower = algp_upper

c       ---------------------------------------------
c       Store heights in same direction as other data
c       ---------------------------------------------

        z(l) = hgt

      end do

      return
      end
ccccccccc
      subroutine taudoc(nc,nx,ny,cc,xx,tau)
c * Strow-Woolf model ... for dry, ozo(ne), and wco (water-vapor continuum)
c .... version of 02.02.98
      dimension cc(nc,ny),xx(nx,ny),tau(*)
      data trap/-999.99/

	taul=1.
	tau(1)=taul
      do 100 j=1,ny
      if(taul.eq.0.) go to 100
      yy=cc(nc,j)
      if(yy.eq.trap) then
         taul=0.
         go to 100
      endif
      do i=1,nx
         yy=yy+cc(i,j)*xx(i,j)
      enddo
      tauy=taul*exp(-yy)
      taul=amin1(tauy,taul)
  100 tau(j+1)=taul
      return
      end
ccccccccc
      subroutine tauwtr(ncs,ncl,nxs,nxl,nxw,ny,ccs,ccl,xx,tau)
c * Strow-Woolf model ... for 'wet' (water-vapor other than continuum)
c .... version of 02.02.98
      dimension ccs(ncs,ny),ccl(ncl,ny),xx(nxw,ny),tau(*)
      data trap/-999.99/

	odsum=0.
	taul=1.
	tau(1)=taul
      do 100 j=1,ny
      if(taul.eq.0.) go to 100
	if(odsum.lt.5.) then
         yy=ccs(ncs,j)
         if(yy.eq.trap) then
            taul=0.
            go to 100
         endif
         do i=1,nxs
            yy=yy+ccs(i,j)*xx(i,j)
         enddo
	   odsum=odsum+abs(yy)
	else
	   yy=ccl(ncl,j)
	   if(yy.eq.trap) then
	      taul=0.
            go to 100
         endif
         do i=1,nxl
            yy=yy+ccl(i,j)*xx(i+11,j)
         enddo
	   odsum=odsum+abs(yy)
	endif
      tauy=taul*exp(-yy)
      taul=amin1(tauy,taul)
  100 tau(j+1)=taul
      return
      end
C *
      function wnplan(vn,tem)
c * Temperature to Planck radiance
      implicit real*8 (a-h,o-z)
      real tem,wnplan
      parameter (h = 6.626176d-27, c = 2.997925d+10, b = 1.380662d-16)
      parameter (c1 = 2.d0*h*c*c)
      parameter (c2 = h*c/b)
      bnt(x,y,z)=x/(dexp(y/z)-1.d0)
c
      f1=c1*vn**3
      f2=c2*vn
      t=tem
      rad=bnt(f1,f2,t)
      wnplan=rad
      return
      end
