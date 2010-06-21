      subroutine nastimlretrwlmsx(opt,ireal,kr,gamt,gamw,gamts,emis,
     $                            tair, rrfwd, pout)
C***********************************************************************
C July 22, 1998:  Include shortwave; also bais correction
C***********************************************************************
C April 29, 1998: Apply to real data retrieval
C***********************************************************************
C Feb.09, Routine Generated for NASTI Retrieval Simulation Studies
C***********************************************************************
	parameter (mm=4196)
	parameter    (nl=40,leng=3*nl+1,lenp=nl*3+25)
        parameter    (nch1=2199,nch2=3858,nch3=3070)
        PARAMETER ( NB=3, NCONS=3)
        parameter    (nnew=nch1+nch2+nch3)
	parameter     (nchb1=112,nchb2=101)
	parameter     (maxcha = 3858)
	parameter    (nw=5,nt=11,nz=1,ntot=nt+nw+nz+1)
C *****
	parameter    (iterat=50)
C *****
c	parameter    (nselrec=26)
	parameter    (nselrec=2)
	dimension idxrec(nselrec)
C *****
        dimension nb1idx(188),nb1iox(18),nb2idx(287)
        DIMENSION freqx(nnew),buf(nb,mm)
        DIMENSION KDO(NCONS),nbuse(nb),rrmin(nb)
	REAL*8 vn,dwn(nb),freq(nb,maxcha),vxnast
        dimension xnos(nb),innt(nb),kry(leng)
	dimension inns(nb),inss(nb),inmod(nb)
C***********************************************************************
c -------------------------------------
c Radiances and brightness temperatures
c -------------------------------------
	real	     tbbo(nnew)
	real	     deltb(nnew), wf(leng,nnew)
	real	     del(nnew),tbbc(nnew)
c --------------------
	dimension coef0(ntot),coef1(ntot)
	dimension phssm(leng,ntot),dbdtb(nnew),dbdtbr(nnew)
	dimension awf(nnew,ntot),atauw(nl),dtau(nl)
c       dimension datauw(nl),dbdt(nl),tair(lenp)
	dimension datauw(nl),dbdt(nl)
	dimension gamval(ntot,ntot),xit(ntot,nnew),dtbeof(nnew)
        real*8    xtx(ntot,ntot),xiv(ntot,ntot)
	dimension prtv0(leng),profm(lenp),fin(leng)
	dimension preg(leng)
	dimension tdair(nl),tdges(nl),tdrtv(nl)
	integer*4 b1idx(nchb1),b2idx(nchb2)
	integer*4 irx(nnew),opt
c----------------------
        DIMENSION nch(nb),kuse(nnew),kusem(nnew)
        DIMENSION pobs(nl)
	REAL*4 pout(lenp), tair(*), rrfwd(*)
	DIMENSION trtv(nl),h2ortv(nl),o3rtv(nl)
        DIMENSION cortv(nl),n2ortv(nl),ch4rtv(nl)
	DIMENSION prtvf(leng),prtvfm(leng)
	DIMENSION twf(nl),qwf(nl),owf(nl)
	DIMENSION tau(nl)
	DIMENSION esr(nnew),rmse1(leng),rmse2(leng)
	dimension rmse1p(leng),rmse2p(leng)
	dimension brtbuf(nb,maxcha),radnos(nb,maxcha)
	dimension bais(nb,maxcha)
	dimension rmsmod(nb,maxcha)
	character*84 rtvfile,inbrt(nb),innos(nb),trufile
	character*84 inbrt1,inbrt2,inbrt3,nosb1f,nosb2f,nosb3f
	character*84 inbrts(nb), modrmsbiasf(nb)
	character*84 modrmsbiasf1,modrmsbiasf2,modrmsbiasf3
	character*84 twcoeff,inevf
        common/taudwo/taud(nl),tauw(nl),tauo(nl)
        data xnos/0.1,0.15,0.15/
	data init/0/
	data tbref/250.0/,ebig/0.0/,rmax/0.99/,rmin/0.05/
	data innt/14,15,16/,inns/17,18,19/,inmod/3,4,5/
        data lrtv/12/,ltru/13/
      DATA KDO/NCONS*1/,MONO/0/
      DATA nbuse/1,1,1/
      DATA rrmin/1.0,0.01,0.0001/
      DATA kxx/47577593/,kxxx/36649505/
C * New prep coordinate system
      DATA POBS/50.,60.,70.,75.,80.,85.,90.,100.,125.,150.,175.,200.,
     * 250.,300.,350.,400.,450.,500.,550.,600.,620.,640.,660.,680.,
     * 700.,720.,740.,760.,780.,800.,820.,840.,860.,880.,900.,920.,
     * 940.,960.,980.,1000./
C********************************************************************
        data inbrts(1)/'./bt0913.2237z.b1'/
        data inbrts(2)/'./bt0913.2237z.b2'/
        data inbrts(3)/'./bt0913.2237z.b3'/
C ****
c        data modrmsbiasf(1)/'../980711/rmsmod980711.b1'/
c        data modrmsbiasf(2)/'../980711/rmsmod980711.b2'/
c        data modrmsbiasf(3)/'../980711/rmsmod980711.b3'/
C *** RMSE & BAIS  !!!
c        data modrmsbiasf(1)/'../980711/bais.wff.b1'/
c        data modrmsbiasf(2)/'../980711/bais.wff.b2'/
c        data modrmsbiasf(3)/'../980711/bais.wff.b3'/
C *** RMSE & BAIS  !!!
c        data modrmsbiasf(1)/'../980711/bais.b1'/
c        data modrmsbiasf(2)/'../980711/bais.b2'/
c        data modrmsbiasf(3)/'../980711/bais.b3'/
C ***
	data inss/7,8,9/
c**********************************************************************
c	data idxrec/677,690,703,833,859,872,911,925,963,989,
c     &              991,1002,1119,1145,1158,1185,1197,1223,1251,1263,
c     &              1275,1289,1301,1315,1327,1353/
	data idxrec/1275,1301/
c***********************************************************************
C Optimal CHANNELS
       data nb1idx/195,201,206,213,220,226,233,239,246,253,
     $   259,266,273,279,286,293,299,306,310,316,
     $   322,329,337,343,351,358,361,365,368,371,
     $   374,378,381,385,392,398,405,411,420,423,
     $   428,434,440,447,455,460,467,474,480,486,
     $   491,498,506,511,517,523,529,536,542,548,
     $   554,562,567,574,579,586,592,598,604,610,
     $   616,621,628,634,641,645,647,651,653,657,
     $   659,665,672,678,681,683,691,699,705,709,
     $   712,714,718,721,723,726,729,732,735,739,
     $   743,748,753,757,759,763,769,776,780,783,
     $   789,798,802,814,823,832,842,850,856,863,
     $   867,875,885,891,895,900,909,913,918,922,
     $   928,934,943,950,951,954,959,963,967,970,
     $   975,980,993,1001,1013,1017,1022,1032,1038,1044,
     $   1049,1057,1067,1071,1078,1089,1094,1101,1108,1130,
     $   1141,1152,1168,1181,1194,1200,1212,1230,1243,1255,
     $   1260,1266,1275,1292,1319,1327,1350,1361,1381,1400,
     $   1419,1477,1498,1520,1530,1540,1550,1560/
       data nb1iox/1636,1642,1651,1661,1677,1693,1702,1711,1718,1725,
     $   1745,1755,1761,1767,1774,1780,1785,1792/
       data nb2idx/228,232,234,240,244,249,252,257,260,265,
     $   270,278,283,286,290,295,299,302,307,310,
     $   314,318,324,331,338,347,351,360,363,367,
     $   372,376,380,386,390,393,399,404,407,411,
     $   417,423,426,429,432,436,439,444,448,453,
     $   457,460,467,482,494,503,507,512,518,526,
     $   531,542,547,555,565,575,583,590,597,612,
     $   620,628,635,644,649,655,662,672,677,682,
     $   688,694,697,699,704,709,717,723,731,737,
     $   742,748,753,759,767,773,780,784,791,794,
     $   799,804,811,825,843,848,860,870,875,887,
     $   902,908,918,931,944,948,956,965,978,983,
     $   993,1005,1013,1018,1027,1033,1037,1048,1057,1060,
     $   1070,1083,1085,1092,1101,1107,1111,1116,1120,1121,
     $   1129,1135,1138,1146,1154,1161,1164,1167,1173,1181,
     $   1185,1189,1192,1205,1220,1235,1239,1244,1255,1263,
     $   1272,1279,1285,1295,1305,1309,1323,1334,1338,1341,
     $   1344,1349,1353,1356,1366,1374,1378,1384,1391,1395,
     $   1399,1404,1412,1415,1423,1431,1435,1438,1442,1446,
     $   1448,1455,1461,1464,1473,1478,1483,1489,1491,1499,
     $   1505,1508,1513,1516,1520,1523,1526,1531,1535,1538,
     $   1544,1549,1555,1560,1567,1572,1579,1585,1588,1595,
     $   1603,1610,1617,1623,1628,1630,1634,1642,1649,1657,
     $   1662,1665,1675,1678,1684,1690,1697,1703,1706,1715,
     $   1723,1729,1738,1743,1752,1760,1766,1770,1776,1786,
     $   1801,1814,1825,1829,1834,1839,1842,1846,1849,1853,
     $   1861,1871,1874,1878,1882,1895,1908,1935,1940,1965,
     $   1984,2010,2057,2082,2095,2128,2156,2176,2212,2222,
     $   2262,2280,2306,2326,2352,2362,2377/
C *********************************************************************
C **** Select from real data spectral
	data b1idx/377,382,386,393,397,400,407,418,422,430,
     &             437,446,452,458,465,471,478,484,490,497,
     &             509,513,519,525,532,535,541,546,550,556,
     &             563,569,575,581,587,594,600,606,613,618,
     &             623,628,633,640,647,652,659,670,684,699,
     &             714,723,732,740,751,763,777,783,789,799,
     &             809,828,837,856,863,879,889,895,905,914,
     &             921,930,944,954,963,968,975,1011,1020,1031,
     &   1044,1058,1073,1087,1102,1110,1134,1163,1179,1200,
     &   1210,1229,1245,1255,1261,1282,1290,1299,1307,1315,
     &   1323,1331,1346,1360,1368,1375,1384,1402,1408,1419,
     &   1425,1431/
	data b2idx/653,668,676,684,691,698,715,724,734,744,
     &             755,767,785,814,827,848,858,870,877,882,
     &             905,925,936,954,980,989,1000,1012,1036,1055,
     &   1060,1077,1093,1105,1121,1139,1149,1164,1170,1184,
     &   1194,1214,1234,1244,1254,1262,1285,1302,1312,1328,
     &   1348,1355,1372,1382,1418,1431,1441,1464,1507,1530,
     &   1577,1603,1646,1668,1712,1728,1755,1776,1814,1824,
     &   1851,1865,1873,1881,1908,1928,1944,2000,2042,2050,
     &   2111,2141,2171,2193,2245,2319,2370,2375,2385,2392,
     &   2402,2433,2454,2508,2529,2540,2556,2571,2591,2606,
     &   2624/
C ****************************************************************
c removed for multiple calls
c	data fin/15*30.0,25*1.0,
c     $           40*1.0,
c     $           40*1000.0,
c     $              1.0/
C -----------------------------
C	NAMELIST
C --------------------------
        namelist /nlnastimlretrwlmsx/  trufile, rtvfile,
     $     inbrt1, inbrt2, inbrt3, nosb1f, nosb2f, nosb3f,
     $     modrmsbiasf1,modrmsbiasf2,modrmsbiasf3,twcoeff,inevf,
     $     kobs,nstart,nend,nprt,inos,irtv,isw,ibias,ireal,iges,
     $     facnos, gamt, gamw, gamts,
     $     emis, ref, cossun
c***********************************************************************
C *** Initialization !
C ***********************************************************************
	do L=1,15
	fin(L)=30.0
	enddo
	do L=16,40
	fin(L)=1.0
	enddo
	do L=1,nl
	fin(L+nl)=1.0
	fin(L+2*nl)=1000.0
	enddo
	fin(3*nl+1)=1.0

	do L=1,leng
	preg(L)=0.0
	prtv0(L)=0.0
	enddo
	do L=1,lenp
	pout(L)=0.0
	enddo
	do k=1,nnew
	tbbo(k)=0.0
	enddo
C **********************************************************************
	do n=1,nnew
	irx(n)=2*n+4453897
	enddo
c***********************************************************************
c * Assume a/c pressure is 50 mb
c        acp =50.
        write(*,'('' Entering nastimlretrwlmsx.f ....'')')
C	open(5,file='nastimlretrwlmsx.nl',status='old')
C        read(5,nlnastimlretrwlmsx)
C        print nlnastimlretrwlmsx
C	close(5)
C paolo 9 Dec 98
        kobs = 143
        nstart =  143
        nend =  143
        nprt =  55
        inos =  1
        facnos = 1.0
c        gamt = 20
c        gamw = 40
c        gamts = 0.0001
        irtv =  1
        isw = 1
        ibias= 1
c       ireal= 0
        iges =  1
c        emis = 1.0
        ref = 0.0
        cossun = 1.0
        trufile = 'raob980913_alt.ac.2237z'
        rtvfile = 'testrtvl.bin'
        inbrt1 = 'test_c3.ahw'
        inbrt2 = 'test_c2.ahw'
        inbrt3 = 'test_c1.ahw'
        nosb1f = 'test_rms_c3.ahw'
        nosb2f = 'test_rms_c2.ahw'
        nosb3f = 'test_rms_c1.ahw'
        modrmsbiasf1 = 'bias_2237.b1'
        modrmsbiasf2 = 'bias_2237.b2'
        modrmsbiasf3 = 'bias_2237.b3'
        twcoeff = 'camx97ax.evtwo.b1coef.fac2.0.nosn980913'
        inevf = 'camx97ax.ev.newnb1'

c *******************************
	modrmsbiasf(1)=modrmsbiasf1
	modrmsbiasf(2)=modrmsbiasf2
	modrmsbiasf(3)=modrmsbiasf3
C *******************
        do l=1,nl
	  fin(l)=gamt*fin(l)
	enddo
        do l=nl+1,2*nl
	  fin(l)=gamw*fin(l)
	enddo
	  fin(leng)=gamts*fin(leng)
C *******************
        nch(1)=nch1
        nch(2)=nch2
        nch(3)=nch3
	vn=vxnast(0,0)
	do n=1,nb
	do k=1,nch(n)
	freq(n,k)=vxnast(n,k)
	dwn(n)=freq(n,2)-freq(n,1)
c 	WRITE(*,'(''BAND ; CHAN & FREQ :'',2i9,f10.3)')
c     $    N,K,FREQ(N,k)
	enddo
	enddo
C ***
      NX=0
      DO N=1,NB
       DO K=1,NCH(N)
       NX=NX+1
       FREQX(NX)=freq(n,k)
	kuse(nx)=0
	kusem(nx)=0
C **** BAND 1
C *** August 07, 1998 ; more band 1 channels
	if(freqx(nx).gt.670..and.freqx(nx).lt.770.)then
	kuse(nx)=1
	kusem(nx)=1
	endif
C *
	if(freqx(nx).gt.1075..and.freqx(nx).lt.1150.)then
	kuse(nx)=1
	kusem(nx)=1
	endif
C **** BAND 2
	if(freqx(nx).ge.1750..and.freqx(nx).le.1950.)then
	kuse(nx)=1
	kusem(nx)=1
	endif
C **** BAND 3
C	if(isw.eq.1) then
C	if(freqx(nx).ge.2090..and.freqx(nx).le.2220.)then
C	kuse(nx)=1
C	kusem(nx)=1
C	endif
C *
C	if(freqx(nx).ge.2387..and.freqx(nx).le.2393.)then
C	kuse(nx)=1
C	kusem(nx)=1
C	endif
C	endif
C *****
       ENDDO
      ENDDO
C **** Optimal Channels
C Band 1 CO2
c	do i=1,nchb1
c         k=b1idx(i)
c         kuse(k)=1
c         kusem(k)=1
c        enddo
C Band 2 H2O
	if(isw.eq.1.or.isw.eq.0) then
        do i=1,nchb2
        k=nb2idx(i)+nch1
        kuse(k)=1
        kusem(k)=1
        enddo
	endif
C **** Total channel statistics
	ksum=0
        do k=1,nnew
        ksum=ksum+kuse(k)
        enddo
c
	if(isw.eq.1) then
	write(*,'('' Use Band 1,2,3; total channels:'',i6)')ksum
	endif
	if(isw.eq.0) then
	write(*,'('' Use Band 1,2; total channels:'',i6)')ksum
	endif
	if(isw.eq.-1) then
	write(*,'('' Use Band 1 only; total channels:'',i6)')ksum
	endif
C**************************************************
      if(nx.ne.nnew) then
      write(*,'('' nx & nnew :'',2i6)') nx,nnew
      stop
      endif
C * GET TRACE GASES' PROFILE
      call tragac(6,trtv,h2ortv,o3rtv,n2ortv,cortv,ch4rtv)
	do l=1,nl
	profm(l)=trtv(l)
	profm(l+nl)=h2ortv(l)
	profm(l+2*nl)=o3rtv(l)
	enddo
	profm(3*nl+1)=trtv(nl)+0.5
ccc
        inbrt(1)=inbrt1
        inbrt(2)=inbrt2
        inbrt(3)=inbrt3
	if(inos.eq.1) then
        innos(1)=nosb1f
        innos(2)=nosb2f
        innos(3)=nosb3f
	endif
C **** Using model noise !!!
	if(ibias.eq.1) then
        do n=1,nb
        lenyy=nch(n)*4
        open(inmod(n),file=modrmsbiasf(n),recl=lenyy,access='direct',
     $    status='old')
        enddo
	endif
C *** For simulations !!!
	if(ireal.eq.0) then
        do n=1,nb
        lenyy=nch(n)*4
        open(inss(n),file=inbrts(n),recl=lenyy,access='direct',
     $    status='old')
        enddo
	endif
ccc
        do n=1,nb
        lenyy=mm*4
C *** Open the noise file
	if(inos.eq.1) then
        open(inns(n),file=innos(n),recl=lenyy,access='direct',
     $    status='old')
	endif
C *** Open the observation file
        open(innt(n),file=inbrt(n),recl=lenyy,access='direct',
     $    status='old')
        enddo
cccc
        nx=0
        do n=1,nb
         do k=1,nch(n)
            nx=nx+1
	    call hdbdtb ( 250., dbdtbr(nx), freqx(nx) )
         enddo
        enddo
cccc
        ksum=0
        do n=1,nx
         ksum=ksum+kuse(n)
        enddo
        write(*,'('' number of tem. & water vapor eigenvectors used :'',
     $  2i4)') nt,nw
        write(*,'('' Total channel Input='',i6)') ksum
        write(*,'('' facnos ; gamt;gamw;gamts:'',f7.2,3f9.4)')
     $	facnos,gamt,gamw,gamts
C ** Basis functions
	call tigrf(phssm,nl,leng,nt,nw,nz,ntot)
	do l=1,ntot
	sum=0.0
	do lx=1,leng
	sum=sum+phssm(lx,l)**2
	enddo
	write(*,'('' SUM=:'',f15.5)') sum
	enddo
ccc ----- ADD ML
	do l=1,ntot
	do ll=1,ntot
	gamval(l,ll)=0.0
	do lx=1,leng
	gamval(l,ll)=gamval(l,ll)+phssm(lx,l)*fin(lx)*phssm(lx,ll)
	enddo
	enddo
	enddo
ccc -----
c        write(*,'('' gamma :'',10e8.2)') gamval
c * open input true  profile
	lenr=lenp*4
	open(ltru,file=trufile,access='direct',status='old',
     $      recl=lenr)
c * open output retrieval profile
	open(lrtv,file=rtvfile,access='direct',status='unknown'
     $  ,recl=lenr)
c
        do l=1,leng
         rmse1(l) = 0.
         rmse2(l) = 0.
         kry(l)= 0
         rmse1p(l) = 0.
         rmse2p(l) = 0.
        enddo
C **** Get the forward model noise spectral
	if(ibias.eq.1) then
          do n=1,nb
           read(inmod(n),rec=1) (rmsmod(n,k),k=1,nch(n))
           read(inmod(n),rec=2) (bais(n,k),k=1,nch(n))
	  enddo
	endif
C **** Get the noise spectral
	if(inos.eq.1) then
          do n=1,nb
           read(inns(n),rec=1) (buf(n,k),k=1,mm)
	  enddo
          call getnosb123(buf,radnos,mm,maxcha,nb)
	endif
C     					            ------------------
C                                                    Loop over profiles
C                                                    ------------------
	knadir = 0
Cpaolo	do 3000 kr = nstart,nend
C ***** Selected records
c	do 3000 kkkk=1,nselrec
c	kr=idxrec(kkkk)
C *****
	write(*,'('' Observation Number:'',i8)')kr

ccc read in observation Radiances
        opt = 1
        if (opt.EQ.1) then
          do n=1,nb
           read(innt(n),rec=kr) (buf(n,k),k=1,mm)
	  enddo
        else
          cnt = 0
          do n=1,nb
           do k=1,mm
             if (k.LE.nch(n)) then
               cnt = cnt + 1
               buf(n,k) = rrfwd(cnt)
             else
               buf(n,k) = 0.
             endif
           enddo
          enddo
        endif
C ****************************************
	alat=buf(1,51)
	alon=buf(1,52)
C *** Feet to meter
	ach=buf(1,53)*0.3048
	achkm=ach/1000.0
	viewang=buf(1,3)
	time=buf(1,24)
	write(*,'('' ALAT & ALON:'',2f10.3)')alat,alon
	write(*,'('' ACH & ACHKM:'',2f10.3)')ach,achkm
	call hrtopm(alat,alon, ielev,itype,111,111)
	go to 222
111	write(*,'('' Error in HRTOPM'')')
	stop
222	continue
	ACP = acaltpre(achkm)
	zsfc=float(ielev)
	psurf = pfromz(zsfc)
C *** Wallops flight !!! (Fix to 1000 mb)
	psurf = pfromz(zsfc)+20.0
C ***
	if(PSURF.ge.1000.0) PSURF=1000.0
	write(*,'('' TIME:'',f12.2)') time
	write(*,'('' REAL ACP:'',f10.3)')ACP
	write(*,'('' REAL Surface Pressure:'',f10.3)')PSURF
	write(*,'('' IELEV & ITYPE:'',2i9)')ielev,itype
	ACP=55.0
C ****************************************
C *** Nadir only
c	if(viewang.ne.0.0) go to 3000
c	knadir=knadir + mod(kr,13)-7+1
c	if(knadir.le.20) go to 3000
c	write(*,'('' KNADIR:'',i6)')knadir

C **** Local Zenith Angle
        azen = acnadzen(achkm,viewang)
	write(*,'('' Viewing & Zenith angle:'',2f10.3)')
     &      viewang,azen

c	read(ltru,rec=kr) tair
C **** For Radiosonde !!!!
c       read(ltru,rec=1) tair

c
 	do k=1,nnew
 	kuse(k)=kusem(k)
 	enddo
c
         lsfc = lsurface ( nl, pobs, psurf, 500., 1000. )
		do l=1,lsfc
		kry(l)=kry(l)+1
		kry(l+nl)=kry(l+nl)+1
		kry(l+2*nl)=kry(l+2*nl)+1
		enddo
		kry(leng)=kry(leng)+1
	write(*,'('' Lsfc:'',i5)')lsfc
C ****
          call getb123(buf,brtbuf,mm,maxcha,nb)
	  call nasticldchk(brtbuf,nb,maxcha,tb12,tb11,cflag)
C *** For simulation !!!
	if(ireal.eq.0) then
	psurf=1000.0
	lsfc=nl
	azen=0.0
	acp=50.0
          do n=1,nb
           read(inss(n),rec=1) (brtbuf(n,k),k=1,nch(n))
	  enddo
	cflag=0.0
	endif
C ****
C **** Fix to clear
	write(*,'('' cflag:'',f10.3)')cflag
C **** Do all retrievals !!!!
c	if(cflag.ne.0.0) go to 3000

C ****
C **** Bais adjusted regression !!!! (Calculation + basi or Observation - bais)
	do n=1,nb
	 do k=1,nch(n)
	  brtbuf(n,k)=brtbuf(n,k)-bais(n,k)
	 enddo
	enddo
C ****
         ky=0
         do n=1,nb
          do k=1,nch(n)
             ky=ky+1
             tbbo(ky)=brtbuf(n,k)
          enddo
         enddo
C ****
c	if(isw.eq.1.or.isw.eq.0) then
c        call evregnr(twcoeff,inevf,brtbuf,nb,maxcha,psurf,azen,preg,
c     &               leng,init)
c	else
        call evregnb1r(twcoeff,inevf,brtbuf,nb,maxcha,psurf,azen,preg,
     &               leng,init)
c	endif
C **** Water Vapor check !!!
	do L=1,nl
	if(preg(L+nl).le.0.003) preg(L+nl)=0.003
	enddo
ccc Add random noise to observations
          n=0
          do nbb=1,nb
            do nx=1, nch(nbb)
C ****
            n=n+1
	    if(tbbo(n).ge.100.0) then
	    call hdbdtb ( tbbo(n), dbdtb(n), freqx(n) )
C *** For assumed instrument noise
	    if(inos.eq.1) then
	    etx = facnos * radnos(nbb,nx) / dbdtb(n)
	    if(etx.le.0.001)etx=0.001
C *** Using Forward model noise + instrument noise !!!
	       if(ibias.eq.1) then
	         etx=sqrt(etx*etx+rmsmod(nbb,nx)*rmsmod(nbb,nx))/2.0
	       endif
	    else
C *** For assumed instrument noise
	    etx = facnos * xnos(nbb) * dbdtbr(n) / dbdtb(n)
      	    etx = sqrt(etx*etx+0.2*0.2)
	    endif
c ***
	    endif
C **** For simulation !!!
	    if(ireal.eq.0) then
	     tbbo(n)=tbbo(n)+anoise(irx(n),etx,0.0)
	    endif
c*
	    esr(n) = 1. / etx
	    if(kr.eq.1093) then
	     write(*,'('' Freq, TBB,RMSE, Bais:'',f8.2,3f10.3)')
     &        freqx(n),tbbo(n),rmsmod(nbb,nx),bais(nbb,nx)
	    endif
            enddo
          enddo
cccc
        dtbrmsg1 = 0.
        dtbrmsg2 = 0.
20000	continue

	   do l=1,nl
 	      trtv(l)=preg(l)
		if(iges.eq.1) trtv(l)=tair(l)
c	      if(meanoff.eq.0) trtv(l)=profm(l)
       	      prtv0(l)=trtv(l)
 	      h2ortv(l)=preg(l+nl)
		if(iges.eq.1) h2ortv(l)=tair(l+nl)
c	      if(meanoff.eq.0) h2ortv(l)=profm(l+nl)
	      prtv0(l+nl)=h2ortv(l)
	      o3rtv(l)=preg(l+2*nl)
c	      if(meanoff.eq.0) o3rtv(l)=profm(l+2*nl)
	      prtv0(l+2*nl)=o3rtv(l)
	   enddo
C **** Using Window Channel
c	   tsurfrtv = (tb12+tb11)/2.0
C **** Using Regression
 	      tsurfrtv=preg(3*nl+1)
C *** Window's skin temperature !!!
	tsurfrtv=0.0
	kkxx=0.0
	do n=1,nnew
	if(freqx(n).ge.1123..and.freqx(n).le.1133.) then
	kkxx=kkxx+1
	tsurfrtv=tsurfrtv+tbbo(n)
	endif
	enddo
	tsurfrtv=2.0+tsurfrtv/float(kkxx)
	write(*,'('' Window Skin and Profile Skin:'',2f10.3)')
     &  	tsurfrtv,preg(3*nl+1)
C **** Using surface air temperature !!!
c 	      tsurfrtv=trtv(lsfc)
c	      if(meanoff.eq.0) tsurfrtv=profm(3*nl+1)
	      prtv0(leng)=tsurfrtv

	      acp=55.0

c	write(*,'('' PRESSURE, T, W, O'')')
	do L=1,nl
c	write(*,'(1x,f8.1,3f10.3)')pobs(L),trtv(l),h2ortv(l),o3rtv(l)
	if(trtv(L).le.185.0) go to 3000
	enddo
	write(*,'('' Skin temperature:'',f10.3)')tsurfrtv
	write(*,'('' Zenith Angle:'',f10.3)')azen

		do l=1,leng
		prtvf(l)=prtv0(l)
		enddo
C ********* DO regression only !!!!
	    if(irtv.eq.0) go to 6666
C *********
C                                                ---------------------
C                                                Loop over the channels
C                                                ----------------------
c           write(*,'('' begin loop 2300 :'')')
	do lxx=1,ntot
	coef0(lxx)=0.0
	coef1(lxx)=0.0
	enddo
	deof0=9999.0
	mpass=0
	ifail=0
	gamf=1.0
10000	continue

           n=0
	   do 2300 nbb = 1, nb
	    if(nbuse(nbb).eq.0) go to 2300
            do 2200 nx=1, nch(nbb)
            n=n+1
            if(kuse(n).eq.0) go to 2200
C                                                ----------------------
C                                                Compute transmittances
C                                                ----------------------
             vn=freq(nbb,nx)
C * NASTI RTE calculations
        CALL TRANNAST(nbb,nx,KDO,ACP,AZEN,trtv,h2ortv,o3rtv,TAU,*2400)
        rr=WNMRAD(vn,TAU,trtv,tsurfrtv,lsfc)
	rr=amax1(rr,rrmin(nbb))
        tbbc(n)=WNBRIT(vn,rr)
c        write(*,'(1x,F9.3,3f10.3)') VN,TBBC(N),RR
C                                                     -----------------
C                                                     Compute radiances
C                                                     -----------------
c
C ********************************************************************
C					    ---------------------------
C                                           Compute weighting functions
C                                           ---------------------------
	  call hdbdtpx(trtv,dbdt,freqx(n),nl)
	     do l=1,nl
		dbdt(l)=dbdt(l)/dbdtb(n)
        	enddo
c            write(*,'('' dbdt  :'',8f7.3)') dbdt
c * w.f. of temperature profile
	do l=1,nl
	twf(l)=0.0
	enddo
c		call deltau(tau,dtau,nl)
		call deltau(taud,dtau,nl)
	   do l=1,nl
c	    twf(l)=dbdt(l)*dtau(l)
	    twf(l)=dbdt(l)*tauw(l)*dtau(l)
	    enddo
c * w.f. of Water Vapor
	 do l=1,nl
	   qwf(l)=0.0
	enddo
	call getwq(qwf,trtv,tau,nl,lsfc,dbdt,tsurfrtv)
		do l=1,nl
		if(tauw(l).lt.0.001) then
			atauw(l)=alog(0.001)
		else
			atauw(l)=alog(tauw(l))
		endif
		enddo
	call deltau(atauw,datauw,nl)
	do l=1,nl
	qwf(l)=qwf(l)*datauw(l)
	enddo
	qwf(1)=0.0
	qwf(2)=0.0
c * w.f. of Ozone
	 do l=1,nl
	   owf(l)=0.0
	enddo
	call getwq(owf,trtv,tau,nl,lsfc,dbdt,tsurfrtv)
		do l=1,nl
		if(tauo(l).lt.0.001) then
			atauw(l)=alog(0.001)
		else
			atauw(l)=alog(tauo(l))
		endif
		enddo
	call deltau(atauw,datauw,nl)
	do l=1,nl
	owf(l)=owf(l)*datauw(l)
	enddo
	owf(1)=0.0
c * w.f. of surface temperature
	tswf=tau(lsfc)*dbdt(lsfc)

		do l=1,nl
		 wf(l,n)=twf(l)
		 wf(l+nl,n)=qwf(l)
		 wf(l+2*nl,n)=owf(l)
		enddo
		 wf(leng,n)=tswf
c	write(*,'('' CHANNEL: T-WF, W-WF, O-WF'')')
c	write(*,'(1x,2i9)')nbb,nx
c	do L=1,nl
c	write(*,'(1x,f8.2,3f12.5)')pobs(L),wf(L,n),wf(nl+L,n),
c     &       wf(2*nl+L,n)
c	enddo
	do l=1,ntot
	awf(n,l)=0.0
		do lk=1,leng
		awf(n,l)=awf(n,l)+wf(lk,n)*phssm(lk,l)
		enddo
	enddo
 2200  continue
 2300  continue
	go to 2500
2400    write(*,'('' ERROR IN READING ACPTRANX'')')
	stop
2500	continue
	     dtbrms10=0.0
	     dtbrms20=0.0
	     ksum1=0
	     ksum2=0
	     do 2600 k=1,nnew
	      del(k) = tbbo(k) - tbbc(k)
C **** Put more constrains
c	      if(mpass.eq.0) then
c	        if(abs(del(k)).ge.7.0) kuse(k)=0
C *** For band 2
c	        if(freqx(k).ge.950..and.abs(del(k)).ge.5.0) kuse(k)=0
C ***
c	      endif
c	write(*,'('' N, TBBO, TBBC, DELTA TB:'',i5,3f10.3)')
c     &   k,tbbo(k),tbbc(k),del(k)
c***
	       if(k.le.nch1) then
	       ksum1=ksum1+kuse(k)
	       dtbrms10 = dtbrms10+del(k)*del(k)*float(kuse(k))
	       else
	       ksum2=ksum2+kuse(k)
	       dtbrms20 = dtbrms20+del(k)*del(k)*float(kuse(k))
	       endif
               deltb(k)=del(k) * esr(k) * float(kuse(k))
		do l=1,ntot
		awf(k,l)=awf(k,l) * esr(k) * float(kuse(k))
		enddo
		dtbeof(k)=0.0
		do l=1,ntot
		dtbeof(k)=dtbeof(k)+awf(k,l)*coef0(l)
		enddo
	        deltb(k)=deltb(k)+dtbeof(k)
2600    continue
	     dtbrms0=sqrt((dtbrms10+dtbrms20)/float(ksum1+ksum2))
	     dtbrms10 = sqrt(dtbrms10/float(ksum1))
	     dtbrms20 = sqrt(dtbrms20/float(ksum2))
c             if(mpass.eq.0) then
C *** March 09, 1998, Jun Li
             if(mpass.eq.0.and.ifail.eq.0) then
	       write(*,'('' Number of Channel used='',i6)') ksum
c	       write(*,'('' DTBRMS10 & 20='',2f10.3)') dtbrms10,dtbrms20
	       write(*,'('' DTBRMS0 ='',f10.3)') dtbrms0
	         dtbrmsg=dtbrms0
	         dtbrmsg1=dtbrms10
	         dtbrmsg2=dtbrms20
	     endif
c * compute solution matrix
	call RTVSOL(awf,gamval,gamf,xtx,xiv,xit,ntot,nnew)
c * compute retrieval delt & delw & deltsfc
	 do i=1,ntot
	  sum=0.0
	  do k=1,nnew
	   sum=sum+xit(i,k)*deltb(k)
	  enddo
          coef1(i)=sum
		enddo
C ---------- Add ML
	do i=1,ntot
	do j=1,ntot
	do k=1,ntot
	coef1(i)=coef1(i)+xiv(i,j)*gamval(j,k)*coef0(k)*gamf
	enddo
	enddo
	enddo
C ----------
	deof=0.0
		do l=1,ntot
	deof=deof+(coef1(l)-coef0(l))*(coef1(l)-coef0(l))
	enddo
	deof=sqrt(deof)
c	if(deof.ge.deof0) then
c	write(*,'('' EXIT:CONVERGE (Coef) FAIL AT MPASS='',i3)')mpass
c	ifail=1
c	go to 24000
c	endif
c	write(*,'('' CONVERGE: OK,ITERATIN CONTINUE!!!'')')
         go to 25000
24000     continue
		do l=1,ntot
		coef1(l)=coef0(l)
		enddo
25000    continue
	do j=1,leng
	prtvfm(j)=prtvf(j)
	enddo
		do j=1,leng
		sum=0.0
		do jx=1,ntot
			sum=sum+phssm(j,jx)*coef1(jx)
		enddo
c
	  if(j.le.nl .or. j.eq.leng) then
		prtvf(j)=prtv0(j)+sum
		else
		prtvf(j)=prtv0(j)*exp(sum)
               endif
	      enddo
C **** Do Water Vapor Correction Again
c	do L=1,9
c	prtvf(L+nl)=0.003
c	enddo
C ****
         do l=1,nl
          trtv(l)=prtvf(l)
          h2ortv(l)=prtvf(l+nl)
          o3rtv(l)=prtvf(l+2*nl)
         enddo
         tsurfrtv=prtvf(leng)
C ******** CALCULATE THE RESIDUAL AFTER EACH ITERATION
c           write(*,'('' begin loop 2222 :'')')
           n=0
	   do 2222 nbb = 1, nb
	    if(nbuse(nbb).eq.0) go to 2222
            do 1111 nx=1, nch(nbb)
            n=n+1
            if(kuse(n).eq.0) go to 1111
             vn=freq(nbb,nx)
C * NASTI RTE calculations
        CALL TRANNAST(nbb,nx,KDO,ACP,AZEN,trtv,h2ortv,o3rtv,TAU,*2400)
        rr=WNMRAD(vn,TAU,trtv,tsurfrtv,lsfc)
	rr=amax1(rr,rrmin(nbb))
        tbbc(n)=WNBRIT(vn,rr)
1111        continue
2222        continue
C ********
            dtbrms11=0.
            dtbrms21=0.
	    ksum1=0
	    ksum2=0
            do k=1,nnew
             delx=tbbo(k) - tbbc(k)
	    	if(k.le.nch1) then
	     ksum1=ksum1+kuse(k)
	     dtbrms11 = dtbrms11 + delx * delx * float(kuse(k))
	        else
	     ksum2=ksum2+kuse(k)
	     dtbrms21 = dtbrms21 + delx * delx * float(kuse(k))
	        endif
	    enddo
	    dtbrms1=sqrt((dtbrms11+dtbrms21)/float(ksum1+ksum2))
	    dtbrms11 = sqrt(dtbrms11/float(ksum1))
	    dtbrms21 = sqrt(dtbrms21/float(ksum2))
c	write(*,'('' DTBRES11 & DTBRES21: '',2f15.7)')dtbrms11,dtbrms21
c	mpass=mpass+1
C ********
C	----------------------------------------------------
C	Bill's idea
C	----------------------------------------------------
	if(dtbrms1.lt.2.0.and.dtbrms1.lt.dtbrms0.
     &   and.abs(dtbrms1-dtbrms0).lt.0.05) go to 26000
C ********
	if(dtbrms1.gt.dtbrms0) then
	write(*,'('' DIVERGES !!!'')')
	ifail=ifail+1
	gamf=gamf*3.0
	do j=1,leng
	prtvf(j)=prtvfm(j)
	enddo
        do l=1,nl
          trtv(l)=prtvf(l)
          h2ortv(l)=prtvf(l+nl)
          o3rtv(l)=prtvf(l+2*nl)
         enddo
         tsurfrtv=prtvf(leng)
	do l=1,ntot
	coef1(l)=coef0(l)
	enddo
	dtbrms1=dtbrms0
c	if(ifail.ge.3.or.mpass.gt.iterat) go to 26000
	if(ifail.ge.10.or.mpass.gt.iterat) go to 26000
	else
c*** ah 10/1/97
	ifail=0
c***
	write(*,'('' CONVERGES !!!'')')
	mpass=mpass+1
	gamf=0.5*gamf
c	dtbrms0=dtbrms1
	endif
C ********
	write(*,'('' DTBRES0 & DTBRES1: '',2f15.7)')dtbrms0,dtbrms1
c	if(ifail.eq.1.or.mpass.gt.iterat) go to 26000
	if(ifail.ge.3.or.mpass.gt.iterat) go to 26000
c	write(*,'('' DEOF0= '',f15.7,'' ; DEOF1= '',f15.7)')
c     $  deof0,deof
	write(*,'('' GAMF:'',f15.7)') gamf
	write(*,'('' DEOF0= '',f15.7)') deof0
	do l=1,ntot
	coef0(l)=coef1(l)
	enddo
c	ifail=0
	deof0=deof
c	write(*,'('' coef : '')')
c	write(*,'(1x, 5e14.2)') coef1
C ************* Output prtvf for the current iteration here !!!
C *************
	go to 10000
26000	write(*,'('' EXIT AT MPASS & IFAIL=:'', 2i4)') mpass,ifail
C ********
6666	continue
C ********
c	if(mpass.le.1) then
c        write(*,'('' Retrieval set to guess !!!'')')
c	do j=1,leng
c	prtvf(j)=prtv0(j)
c	enddo
c	endif
ccccc
C	-----------------------------------------
C	Bill's idea
C	-------------------------------------------
	do j=1,nl
	if(prtvf(j+nl).ge.20.0)prtvf(j+nl)=20.0
c	if(prtvf(j+nl).le.0.003)prtvf(j+nl)=0.003
	enddo
c		do j=1,nl
c		if(j.ge.10) then
c		jy=j+nl
c		ws=wsat(pobs(j),prtvf(j))
c		wmax=ws*rmax
c		wmin=ws*rmin
c		prtvf(jy)=chop(prtvf(jy),wmin,wmax)
c		endif
c		enddo
	 do l=1,leng-1
	  pout(l)=prtvf(l)
	 enddo
          pout(3*nl+1)=prtvf(leng)
	  dtbrms=dtbrms1
            dtbdif=dtbrms-dtbrmsg
777      continue
        write(*,'('' rmse of dtbg ; dtbr & dif: '',3f9.2)')
     $  dtbrmsg,dtbrms,dtbdif
        rmsedtbg=rmsedtbg+dtbrmsg
        rmsedtbr=rmsedtbr+dtbrms
	write(*,'('' Press T-Raob guess rtv; W-Raob guess rtv  '')')
		do l=1,lsfc

C*** Chop for water vapor !!!
c	if(L.ge.10) then
c         ws=wsat(pobs(l),prtvf(l))
c         xx=prtvf(L+nl)/ws
c         prtvf(L+nl)=chop(xx,rmin,rmax)*ws
c	endif
cccc
ccc***** (RH)
c		ws0=wsat(pobs(l),prtv0(l))
c		ws=wsat(pobs(l),tair(l))
c		wsrtv=wsat(pobs(l),prtvf(l))
c	prtv0(l+nl)=prtv0(l+nl)/ws0
c	prtvf(l+nl)=prtvf(l+nl)/wsrtv
c	tair(l+nl)=tair(l+nl)/ws
c        prtv0(l+nl)=100.0*chop(prtv0(l+nl),0.05,0.95)
c        prtvf(l+nl)=100.0*chop(prtvf(l+nl),0.05,0.95)
c        tair(l+nl)=100.0*chop(tair(l+nl),0.05,0.95)
ccc***** (TD)
c         tx=tair(l)
c         rhx=tair(l+nl)
c         tair(l+nl)=dewpt(pobs(l),tx,rhx)
c         tx=prtvf(l)
c         rhx=prtvf(l+nl)
c         prtvf(l+nl)=dewpt(pobs(l),tx,rhx)
c         tx=prtv0(l)
c         rhx=prtv0(l+nl)
c         prtv0(l+nl)=dewpt(pobs(l),tx,rhx)
C	--------------------------------------------------
C	August 29, 1998: print out the comparison with raob !!!
C	--------------------------------------------------
		if(kr.eq.nprt) then
          tx=tair(l)
          rhx=tair(l+nl)
          tdair(l)=dewpt(pobs(l),tx,rhx)
          tx=prtvf(l)
          rhx=prtvf(l+nl)
          tdrtv(l)=dewpt(pobs(l),tx,rhx)
          tx=prtv0(l)
          rhx=prtv0(l+nl)
          tdges(l)=dewpt(pobs(l),tx,rhx)
		endif
ccc*****
	         lx=nl+l
                 dt1 = tair(l) - prtv0(l)
                 dt2 = tair(l) - prtvf(l)
                 dw1 = tair(lx) - prtv0(lx)
                 dw2 = tair(lx) - prtvf(lx)
                 rmse1(l) = rmse1(l) + dt1 * dt1
                 rmse2(l) = rmse2(l) + dt2 * dt2
                 rmse1(lx) = rmse1(lx) + dw1 * dw1
                 rmse2(lx) = rmse2(lx) + dw2 * dw2
		 write(*,'(1x,f8.2,6f11.3)')
     $		 pobs(l),tair(l),dt1,dt2,tair(lx),dw1,dw2
		enddo
C	--------------------------------------------------
C	August 29, 1998: print out the comparison with raob !!!
C	--------------------------------------------------
		if(kr.eq.nprt) then
		do l=1,nl
		 write(*,'(1x,f8.2,6f11.3)')
     $		 pobs(l),tair(l),prtv0(l),prtvf(l),tdair(l),
     $           tdges(l),tdrtv(l)
		enddo
		endif
c	write(*,'('' Press     W-Raob    1st-guess  Final-rtv  '')')
c		do l=nl+1,nl+lsfc
c                 dt1 = tair(l) - prtv0(l)
c                 dt2 = tair(l) - prtvf(l)
c                 rmse1(l) = rmse1(l) + dt1 * dt1
c                 rmse2(l) = rmse2(l) + dt2 * dt2
c		 dt1p=100.0*dt1/tair(l)
c		 dt2p=100.0*dt2/tair(l)
c                 rmse1p(l) = rmse1p(l) + dt1p * dt1p
c                 rmse2p(l) = rmse2p(l) + dt2p * dt2p
c		 write(*,'(1x,f8.2,4f10.2)')
c     $		 pobs(l-nl),tair(l),dt1,dt2
c		enddo
c	write(*,'('' Press     O-Raob    1st-guess  Final-rtv  '')')
c		do l=2*nl+1,2*nl+lsfc
c                 dt1 = tair(l) - prtv0(l)
c                 dt2 = tair(l) - prtvf(l)
c                 rmse1(l) = rmse1(l) + dt1 * dt1
c                 rmse2(l) = rmse2(l) + dt2 * dt2
c		 write(*,'(1x,f8.2,4f10.2)')
c     $		 pobs(l-2*nl),tair(l),dt1,dt2
c		enddo
	dts1 = tair(3*nl+1) - prtv0(leng)
	dts2 = tair(3*nl+1) - prtvf(leng)
	rmse1(leng) = rmse1(leng) + dts1 * dts1
	rmse2(leng) = rmse2(leng) + dts2 * dts2
	write(*,'('' Tskin-Raob  1st-guess & Final-rtv :'',3f8.3)')
     $     tair(3*nl+1), dts1, dts2

c				  ----------------------------
c				       write  rtv profile
c				  ----------------------------
C **** Put some information for DISPLAY
C * View angle
	pout(126)=viewang
C * Cloud check
	pout(127)=cflag
C * Surface pressure
	pout(128)=psurf
C * Surface level
	pout(129)=lsfc
C * Latitude
	pout(133)=buf(1,51)
C * Longitude
	pout(134)=buf(1,52)
C * Time
	pout(135)=buf(1,24)
C * Year
	pout(136)=buf(1,17)
C * Month
	pout(137)=buf(1,18)
C * Day
	pout(138)=buf(1,19)
C * Pressure (replace ozone with pressure !!!)
	do L=1,nl
	pout(2*nl+L)=pobs(L)
	enddo
Cpaolo the recordlenght is 145
Cpaolo	     write(lrtv,rec=kr) pout
C ****
 2900 continue
 3000 continue

 8000 continue
        do l = 1, leng
	 xx=float(kry(l))
	if(xx.ne.0.0) then
         rmse1(l) = sqrt ( rmse1(l) / xx )
         rmse2(l) = sqrt ( rmse2(l) / xx )
         rmse1p(l) = sqrt ( rmse1p(l) / xx )
         rmse2p(l) = sqrt ( rmse2p(l) / xx )
	endif
        enddo
c**
C * Mean rmse below 0.1 mb
         rmse1mx = 0.
         rmse2mx = 0.
	rmse1mw = 0.0
	rmse2mw = 0.0
	rmse1mo = 0.0
	rmse2mo = 0.0
	 do l = 1, nl
          rmse1mx = rmse1mx + rmse1(l)
          rmse2mx = rmse2mx + rmse2(l)
          rmse1mo = rmse1mo + rmse1(l+2*nl)
          rmse2mo = rmse2mo + rmse2(l+2*nl)
         enddo
         do l= 1, nl
          rmse1mw = rmse1mw + rmse1(l+nl)
          rmse2mw = rmse2mw + rmse2(l+nl)
         enddo
         rmse1mx = rmse1mx /float(nl)
         rmse2mx = rmse2mx /float(nl)
         rmse1mw =rmse1mw/float(nl)
         rmse2mw =rmse2mw/float(nl)
         rmse1mo =rmse1mo/30.0
         rmse2mo =rmse2mo/30.0
	 xobs=float(kry(1))
         rmsedtbg = rmsedtbg / xobs
         rmsedtbr = rmsedtbr / xobs
        write(*,'('' pressure  T-rmse1  T-rmse2 '')')
        do l=1,nl
           write(*,'(1x,f8.1,2x,f7.3,2x,f7.3)')
     $     pobs(l),rmse1(l),rmse2(l)
        enddo
        write(*,'('' pressure  W-rmse1  W-rmse2  W-rmse1%  W-rmse2%'')')
        do l=1,nl
	lx=nl+l
           write(*,'(1x,f8.1,2x,f7.3,2x,f7.3,2x,f7.3,2x,f7.3)')
     $     pobs(l),rmse1(lx),rmse2(lx),rmse1p(lx),rmse2p(lx)
        enddo
c        write(*,'('' pressure  O-rmse1  O-rmse2 '')')
c        do l=1,nl
c	lx=2*nl+l
c           write(*,'(1x,f8.1,2x,f7.3,2x,f7.3)')
c     $     pobs(l),rmse1(lx),rmse2(lx)
c        enddo
        write(*,'('' tsurfc : '',2f8.3)') rmse1(leng),rmse2(leng)
        write(*,'('' Below 1 mb : '')')
        write(*,'('' rmse1mt & rmse2mt : '',2f8.3)') rmse1mx,rmse2mx
        write(*,'('' rmse1mw & rmse2mw : '',2f8.4)') rmse1mw,rmse2mw
        write(*,'('' rmse1mo & rmse2mo : '',2f8.4)') rmse1mo,rmse2mo
        write(*,'('' rmsedtbg & rmsedtbr : '',2f8.2)') rmsedtbg,rmsedtbr
cc***
        write(*,'('' nastimlretrwlmsx done !!'')')
	close(lrtv)
	close(ltru)
	do nbb=1,nb
	close(innt(nbb))
	if(inos.eq.1) then
	close(inns(nbb))
	endif
	if(ibias.eq.1) then
	close(inmod(nbb))
	endif
	enddo
        return
        end
c
C
	SUBROUTINE HDBDTB(TBB,DBDTB,WN)
C $ OBTAIN DB/DTB (HLH)
C * PLANCK'S CONSTANT
	PARAMETER (H = 6.6237E-27)
C * VELOCITY OF LIGHT
	PARAMETER (C = 2.99791E+10)
C * BOLTZMANN'S CONSTANT
	PARAMETER (B = 1.38024E-16)
C
	PARAMETER (C1 = 2.*H*C*C)
	PARAMETER (C2 = H*C/B)
C
	BNT(X,Y,Z)=X/(EXP(Y/Z)-1.)
	F1=C1*WN**3
	F2=C2*WN
	Q=F2/F1
	T=TBB
	BT=BNT(F1,F2,T)
	BB=BT*BT
	TT=T*T
	EX=Q*EXP(F2/T)
	DBDTB=EX*BB/TT
	RETURN
	END
c
	SUBROUTINE HDBDTPX(TEMP,DBDT,WN,NL)
C $ OBTAIN DB/DT PROFILE (HLH)
C * PLANCK'S CONSTANT
	PARAMETER (H = 6.6237E-27)
C * VELOCITY OF LIGHT
	PARAMETER (C = 2.99791E+10)
C * BOLTZMANN'S CONSTANT
	PARAMETER (B = 1.38024E-16)
C
	PARAMETER (C1 = 2.*H*C*C)
	PARAMETER (C2 = H*C/B)
C
	DIMENSION TEMP(*),DBDT(*)
	BNT(X,Y,Z)=X/(EXP(Y/Z)-1.)
	F1=C1*WN**3
	F2=C2*WN
	Q=F2/F1
	DO 120 I=1,NL
	T=TEMP(I)
	BT=BNT(F1,F2,T)
	BB=BT*BT
	TT=T*T
	EX=Q*EXP(F2/T)
	DBDT(I)=EX*BB/TT
  120 CONTINUE
	RETURN
	END
c
	SUBROUTINE DELTAU(TAU,DTAU,NL)
C $ (JLI)
	DIMENSION TAU(*),DTAU(*)
c	DTAU(1)=0.5*(TAU(1)-TAU(2))
	DTAU(1)=0.5*(1.-TAU(1))
	DO 100 I=2,NL-1
	DTAU(I)=0.5*(TAU(I-1)-TAU(I+1))
  100 CONTINUE
	DTAU(NL)=0.5*(TAU(NL-1)-TAU(NL))
	RETURN
	END
c***
	SUBROUTINE RTVSOL(WF,GAM,GAMF,ATA,ANV,AIT,MLYR,NW)
C *   M.I.F. or M.L.H. SOLUTION
C     MLYR = NO. OF LEVELS OF RETRIEVAL
C     NW = NO. OF CHANNELS USED
C	IMPLICIT REAL*8 (A-H,O-Z)
        REAL*8 SUM
	REAL*4 WF(NW,MLYR),GAM(MLYR,MLYR),AIT(MLYR,NW)
	REAL*8 ATA(MLYR,MLYR),ANV(MLYR,MLYR)

	DO 110 J=1,MLYR
	DO 110 I=1,J
	SUM=0.0D0
	DO 100 K=1,NW
  100 SUM=SUM+DBLE(WF(K,J)*WF(K,I))
       ATA(I,J)=SUM
       ATA(J,I)=SUM
  110 CONTINUE
C
c	 DO 115 J=1,MLYR
c  115  ATA(J,J)=ATA(J,J)+DBLE(GAM(J))
C --------------
	do i=1,mlyr
	do j=1,mlyr
	ata(i,j)=ata(i,j)+DBLE(gamf*gam(i,j))
	enddo
	enddo
C ----------------
	CALL SYMVRT(ATA,ANV,MLYR,MLYR)
C    **********INVERSE* A TRANSPOSE
	DO 200 I=1,MLYR
	DO 200 J=1,NW
	SUM=0.0D0
	DO 250 k=1,MLYR
	SUM=SUM+ANV(I,K)*DBLE(WF(J,K))
250	CONTINUE
	AIT(I,J)=SNGL(SUM)
200	CONTINUE
	RETURN
	END
CCCCCC
	SUBROUTINE GETWQ(WQ,TEM,TAUT,NL,LSTA,DBDT,TSWQ)
	DIMENSION WQ(NL),TEM(NL),TAUT(NL),DBDT(NL)
	WQ1=DBDT(LSTA)*(TEM(LSTA)-TSWQ)*TAUT(LSTA)
	DO 10 I=2,LSTA
	WQ2=0.0
	IF(I.EQ.LSTA) GO TO 100
	PHD1=DBDT(I)*TAUT(I)
	TEM1=TEM(I)
	DO 20 IP=I+1,LSTA
	PHD2=DBDT(IP)*TAUT(IP)
	TEM2=TEM(IP)
	WQ2=WQ2+0.5*(PHD1+PHD2)*(TEM2-TEM1)
	PHD1=PHD2
	TEM1=TEM2
20	CONTINUE
100	WQ(I)=WQ1-WQ2
10	CONTINUE
	RETURN
	END
ccc
	subroutine tigrf(phss,nl,ns,nt,nw,nz,ntot)
	parameter (iut=21,iuw=22,iuz=23,nvt=40,nvw=40,nvz=40)
	parameter (lenw=8*nvw,lent=8*nvt,lenz=8*nvz)
	real*8 tbuf,wbuf,zbuf
	character*80 tfile,wfile,zfile
c	data tfile/'../data/noaaevteac'/
c	data wfile/'../data/noaaevwvac'/
c	data zfile/'../data/noaaevozac'/
	data tfile/'camx97ax.evtac'/
	data wfile/'camx97ax.evwac'/
	data zfile/'camx97ax.evoac'/
	dimension phss(ns,ntot)
	dimension tbuf(nvt),wbuf(nvw),zbuf(nvz)
      open(iut,file=tfile,recl=lent,access='direct',status='old')
      open(iuw,file=wfile,recl=lenw,access='direct',status='old')
      open(iuz,file=zfile,recl=lenz,access='direct',status='old')
c
	do i=1,ns
	do j=1,ntot
	phss(i,j)=0.0
	enddo
	enddo
c
c	TIGR BASED FUNCTION GENERATION
	do 10 i=1,nt
	   read(iut,rec=i) tbuf
	do j=1,nl
		phss(j,i)=tbuf(j)
	enddo
10	continue
c
	do 20 i=1,nw
	ii=i+nt
	read(iuw,rec=i) wbuf
	    do j=41,80
		phss(j,ii)=wbuf(j-40)
		enddo
20	continue
c
	do 30 i=1,nz
	ii=i+nt+nw
	read(iuz,rec=i) zbuf
	    do j=81,120
		phss(j,ii)=zbuf(j-80)
		enddo
30	continue
c
	phss(ns,ntot)=1.0
c
	close(iut)
	close(iuw)
	close(iuz)
	return
	end
c
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
C
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
C
	function acaltpre(altkm)
c * Obtain pressure in MB at aircraft altitude in KM (gt 11)
c .... version of 03.02.98

	parameter (c=-0.15769,dt=0.5,rg=0.029271,t=216.65)
	parameter (pfix=0.001,p18=75.048,p20=54.748)

	if(altkm.le.20.) then
	   e=c*(altkm-18.)
	   pb=p18
	else
	   a=altkm-20.
	   d=rg*(t+a*dt)
	   e=-a/d
	   pb=p20
	endif

	acaltpre=pb*exp(e)+pfix
	return
	end
C
      function acnadzen(altkm,scang)
c * Convert aircraft nadir (scan) angle to local zenith angle
c .... version of 03.02.98
c	   altkm = altitude   (km)
c	   scang = scan angle (deg)

      parameter (radius=6371.03,dtr=.01745329)

      arg=scang*dtr
      fac=1.+altkm/radius
      azen=asin(sin(arg)*fac)
      acnadzen=azen/dtr
      return
      end
C
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
c    +          dz, dp, r_hgt, wg, og, A, B
     +          dz, dp, r_hgt, A, B

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
C
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
C
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
C
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
C
      subroutine tragac(model,atem,ah2o,ao3,an2o,aco,ach4)
c * Select fascode model-atmos temp, h2o, and trace gas profiles
c          formatted for HIS/AIRCRAFT, thin pressure slicing.
c * model: 1 = Tropical
c          2 = Midlatitude Summer
c          3 = Midlatitude Winter
c          4 = Subarctic Summer
c          5 = Subarctic Winter
c          6 = U.S. Standard (1976)
c * Pressures (for information):
c     data p /50.,75.,100.,125.,150.,175.,200.,250.,300.,350.,400.,
c    * 450.,500.,550.,600.,620.,640.,660.,680.,700.,720.,740.,760.,
c    * 780.,800.,820.,840.,860.,880.,900.,910.,920.,930.,940.,950.,
c    * 960.,970.,980.,990.,1000./
      parameter (na=6,nl=40)
      dimension atem(*),ah2o(*),ao3(*),an2o(*),aco(*),ach4(*)
      dimension qtem(nl,na),qh2o(nl,na),qo3(nl,na)
      dimension qn2o(nl,na),qco(nl,na),qch4(nl,na)
c * Tropical
      data (qtem(i,1),i=1,nl)/
     +  209.70, 199.97, 195.64, 201.59, 208.75, 215.30, 220.96, 230.67,
     +  239.25, 246.59, 253.14, 259.07, 264.45, 269.45, 274.11, 275.88,
     +  277.60, 279.30, 280.94, 282.53, 283.93, 284.86, 285.76, 286.64,
     +  287.49, 288.65, 289.90, 291.12, 292.31, 293.47, 294.05, 294.62,
     +  295.19, 295.76, 296.32, 296.87, 297.41, 297.95, 298.49, 299.02/
      data (qh2o(i,1),i=1,nl)/
     +   0.003,  0.003,  0.003,  0.004,  0.006,  0.009,  0.021,  0.083,
     +   0.266,  0.568,  0.986,  1.544,  2.258,  3.188,  3.969,  4.258,
     +   4.816,  5.867,  6.886,  7.876,  8.996, 10.554, 12.070, 13.547,
     +  14.986, 16.001, 16.863, 17.705, 18.527, 19.331, 19.864, 20.482,
     +  21.094, 21.699, 22.298, 22.890, 23.476, 24.056, 24.631, 25.199/
      data (qo3 (i,1),i=1,nl)/
     + 1.69985,0.63460,0.20944,0.13151,0.10984,0.09597,0.08405,0.06529,
     + 0.05392,0.04764,0.04366,0.04150,0.03961,0.03795,0.03650,0.03595,
     + 0.03556,0.03541,0.03527,0.03514,0.03494,0.03457,0.03421,0.03385,
     + 0.03351,0.03311,0.03272,0.03233,0.03195,0.03157,0.03134,0.03107,
     + 0.03080,0.03054,0.03027,0.03002,0.02976,0.02951,0.02926,0.02901/
      data (qn2o(i,1),i=1,nl)/
     + 0.22368,0.26279,0.28191,0.29229,0.29861,0.30355,0.30762,0.31432,
     + 0.31845,0.31972,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000/
      data (qco (i,1),i=1,nl)/
     + 0.01257,0.01841,0.02712,0.03667,0.04770,0.06031,0.07237,0.09046,
     + 0.10296,0.11346,0.12113,0.12599,0.12899,0.13011,0.13081,0.13105,
     + 0.13153,0.13247,0.13338,0.13426,0.13519,0.13635,0.13747,0.13857,
     + 0.13964,0.14071,0.14177,0.14281,0.14382,0.14480,0.14529,0.14577,
     + 0.14625,0.14672,0.14718,0.14764,0.14809,0.14855,0.14899,0.14943/
      data (qch4(i,1),i=1,nl)/
     + 1.37228,1.50874,1.56414,1.59777,1.62107,1.64017,1.65519,1.67582,
     + 1.68773,1.69478,1.69785,1.69931,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000/
c * Midlatitude Summer
      data (qtem(i,2),i=1,nl)/
     +  220.55, 217.36, 215.70, 215.70, 215.70, 215.79, 220.45, 230.07,
     +  238.24, 245.33, 251.68, 257.36, 262.43, 266.86, 271.02, 272.59,
     +  274.13, 275.63, 277.09, 278.51, 279.89, 281.24, 282.55, 283.83,
     +  285.08, 286.05, 286.97, 287.87, 288.75, 289.61, 290.04, 290.47,
     +  290.89, 291.30, 291.71, 292.12, 292.52, 292.92, 293.31, 293.70/
      data (qh2o(i,2),i=1,nl)/
     +   0.003,  0.003,  0.003,  0.003,  0.005,  0.008,  0.023,  0.125,
     +   0.323,  0.543,  0.846,  1.221,  1.656,  2.185,  3.235,  3.651,
     +   4.148,  4.692,  5.220,  5.733,  6.408,  7.239,  8.048,  8.836,
     +   9.604, 10.454, 11.295, 12.116, 12.918, 13.703, 14.159, 14.628,
     +  15.092, 15.551, 16.005, 16.454, 16.899, 17.339, 17.775, 18.206/
      data (qo3 (i,2),i=1,nl)/
     + 2.46410,1.25525,0.66705,0.52482,0.44729,0.32016,0.24487,0.16974,
     + 0.12153,0.10001,0.08397,0.07209,0.06225,0.05562,0.05072,0.04892,
     + 0.04729,0.04578,0.04433,0.04291,0.04161,0.04043,0.03927,0.03815,
     + 0.03705,0.03627,0.03553,0.03482,0.03412,0.03344,0.03313,0.03283,
     + 0.03253,0.03223,0.03194,0.03165,0.03137,0.03108,0.03080,0.03053/
      data (qn2o(i,2),i=1,nl)/
     + 0.13003,0.20050,0.24836,0.26960,0.27908,0.28514,0.29144,0.30099,
     + 0.31268,0.31809,0.31977,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000/
      data (qco (i,2),i=1,nl)/
     + 0.01232,0.01753,0.02680,0.03725,0.04893,0.06180,0.07405,0.09159,
     + 0.10411,0.11448,0.12182,0.12638,0.12911,0.13022,0.13087,0.13111,
     + 0.13177,0.13270,0.13360,0.13447,0.13547,0.13660,0.13769,0.13876,
     + 0.13980,0.14086,0.14191,0.14293,0.14393,0.14490,0.14538,0.14585,
     + 0.14632,0.14678,0.14723,0.14768,0.14813,0.14857,0.14901,0.14944/
      data (qch4(i,2),i=1,nl)/
     + 1.14272,1.30156,1.36720,1.41406,1.44747,1.47497,1.49976,1.54923,
     + 1.59554,1.62282,1.63971,1.65842,1.67507,1.68616,1.69336,1.69598,
     + 1.69746,1.69821,1.69894,1.69965,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000/
c * Midlatitude Winter
      data (qtem(i,3),i=1,nl)/
     +  215.20, 215.76, 216.68, 217.39, 217.97, 218.47, 218.90, 219.61,
     +  225.79, 232.02, 237.54, 242.52, 247.06, 251.24, 255.10, 256.58,
     +  258.03, 259.43, 260.79, 261.94, 262.70, 263.44, 264.16, 264.87,
     +  265.55, 266.23, 266.89, 267.54, 268.17, 268.78, 269.09, 269.39,
     +  269.69, 269.99, 270.28, 270.57, 270.86, 271.14, 271.43, 271.71/
      data (qh2o(i,3),i=1,nl)/
     +   0.004,  0.004,  0.005,  0.005,  0.005,  0.006,  0.008,  0.026,
     +   0.056,  0.114,  0.229,  0.456,  0.686,  0.941,  1.235,  1.399,
     +   1.593,  1.782,  1.965,  2.136,  2.288,  2.437,  2.581,  2.721,
     +   2.856,  2.984,  3.110,  3.233,  3.352,  3.475,  3.550,  3.625,
     +   3.698,  3.772,  3.844,  3.915,  3.986,  4.056,  4.126,  4.194/
      data (qo3 (i,3),i=1,nl)/
     + 3.16918,1.75195,1.11336,0.86217,0.74765,0.60758,0.46038,0.25869,
     + 0.15587,0.10257,0.07960,0.06241,0.05211,0.04425,0.03682,0.03513,
     + 0.03425,0.03339,0.03256,0.03176,0.03100,0.03025,0.02953,0.02883,
     + 0.02844,0.02835,0.02825,0.02816,0.02807,0.02799,0.02798,0.02796,
     + 0.02794,0.02792,0.02790,0.02788,0.02786,0.02785,0.02783,0.02781/
      data (qn2o(i,3),i=1,nl)/
     + 0.15048,0.22037,0.26025,0.27528,0.28326,0.29005,0.29567,0.30775,
     + 0.31635,0.31953,0.31999,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000/
      data (qco (i,3),i=1,nl)/
     + 0.01287,0.02029,0.03043,0.04351,0.05758,0.07140,0.08263,0.09789,
     + 0.10954,0.11883,0.12453,0.12799,0.12964,0.13053,0.13111,0.13174,
     + 0.13263,0.13350,0.13434,0.13524,0.13633,0.13739,0.13842,0.13942,
     + 0.14042,0.14140,0.14237,0.14330,0.14422,0.14512,0.14556,0.14599,
     + 0.14642,0.14684,0.14726,0.14768,0.14809,0.14849,0.14890,0.14929/
      data (qch4(i,3),i=1,nl)/
     + 1.19260,1.32696,1.38849,1.43297,1.46620,1.49444,1.52128,1.57260,
     + 1.61522,1.63006,1.64845,1.66748,1.68041,1.68956,1.69601,1.69744,
     + 1.69816,1.69886,1.69954,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000/
c * Subarctic Summer
      data (qtem(i,4),i=1,nl)/
     +  225.20, 225.20, 225.20, 225.20, 225.20, 225.20, 225.20, 225.20,
     +  230.54, 237.97, 244.53, 250.46, 255.93, 260.79, 264.41, 265.77,
     +  267.11, 268.41, 269.68, 270.90, 272.12, 273.31, 274.46, 275.59,
     +  276.69, 277.78, 278.85, 279.89, 280.90, 281.90, 282.41, 282.91,
     +  283.41, 283.90, 284.39, 284.87, 285.34, 285.82, 286.28, 286.74/
      data (qh2o(i,4),i=1,nl)/
     +   0.005,  0.004,  0.004,  0.004,  0.004,  0.005,  0.007,  0.029,
     +   0.109,  0.352,  0.706,  1.129,  1.689,  2.366,  3.144,  3.453,
     +   3.811,  4.157,  4.493,  4.820,  5.256,  5.681,  6.094,  6.496,
     +   6.892,  7.286,  7.671,  8.047,  8.413,  8.821,  9.120,  9.416,
     +   9.708,  9.997, 10.284, 10.567, 10.847, 11.125, 11.399, 11.671/
      data (qo3 (i,4),i=1,nl)/
     + 2.75529,1.46408,0.92611,0.70775,0.58224,0.48272,0.40198,0.24128,
     + 0.15314,0.10010,0.08052,0.07007,0.06065,0.05220,0.04650,0.04448,
     + 0.04301,0.04159,0.04021,0.03887,0.03772,0.03660,0.03552,0.03446,
     + 0.03347,0.03258,0.03172,0.03087,0.03005,0.02920,0.02872,0.02823,
     + 0.02776,0.02729,0.02682,0.02636,0.02590,0.02545,0.02500,0.02456/
      data (qn2o(i,4),i=1,nl)/
     + 0.13378,0.19456,0.22297,0.23512,0.24438,0.25046,0.25529,0.26280,
     + 0.26907,0.27491,0.28076,0.28743,0.29537,0.30310,0.30679,0.30801,
     + 0.30853,0.30903,0.30952,0.31000,0.31000,0.31000,0.31000,0.31000,
     + 0.31000,0.31000,0.31000,0.31000,0.31000,0.31000,0.31000,0.31000,
     + 0.31000,0.31000,0.31000,0.31000,0.31000,0.31000,0.31000,0.31000/
      data (qco (i,4),i=1,nl)/
     + 0.01232,0.01795,0.02775,0.03896,0.05265,0.06651,0.07902,0.09512,
     + 0.10708,0.11690,0.12328,0.12725,0.12941,0.13041,0.13102,0.13139,
     + 0.13231,0.13320,0.13406,0.13490,0.13603,0.13713,0.13820,0.13924,
     + 0.14027,0.14130,0.14231,0.14329,0.14425,0.14519,0.14565,0.14610,
     + 0.14655,0.14700,0.14744,0.14788,0.14831,0.14874,0.14916,0.14958/
      data (qch4(i,4),i=1,nl)/
     + 0.98355,1.20005,1.31562,1.38657,1.44057,1.47772,1.50875,1.56232,
     + 1.60647,1.62653,1.64443,1.66332,1.67806,1.68827,1.69497,1.69715,
     + 1.69790,1.69862,1.69932,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000/
c * Subarctic Winter
      data (qtem(i,5),i=1,nl)/
     +  214.19, 215.73, 216.82, 217.20, 217.20, 217.20, 217.20, 217.20,
     +  218.48, 223.08, 229.02, 234.45, 239.43, 244.02, 248.12, 249.32,
     +  250.49, 251.62, 252.71, 253.40, 254.07, 254.72, 255.36, 255.98,
     +  256.59, 257.18, 257.76, 258.33, 258.89, 258.90, 258.74, 258.59,
     +  258.43, 258.28, 258.12, 257.97, 257.82, 257.68, 257.53, 257.39/
      data (qh2o(i,5),i=1,nl)/
     +   0.005,  0.005,  0.005,  0.005,  0.004,  0.006,  0.009,  0.022,
     +   0.031,  0.076,  0.170,  0.247,  0.389,  0.596,  0.821,  0.912,
     +   0.999,  1.084,  1.167,  1.223,  1.278,  1.331,  1.383,  1.432,
     +   1.467,  1.502,  1.537,  1.570,  1.602,  1.593,  1.576,  1.558,
     +   1.541,  1.524,  1.507,  1.491,  1.474,  1.458,  1.442,  1.426/
      data (qo3 (i,5),i=1,nl)/
     + 3.70528,2.15019,1.38778,0.96152,0.66060,0.41453,0.36047,0.28088,
     + 0.17023,0.09235,0.06541,0.04413,0.03941,0.03549,0.03212,0.03095,
     + 0.02982,0.02872,0.02766,0.02673,0.02583,0.02495,0.02409,0.02330,
     + 0.02279,0.02230,0.02182,0.02135,0.02090,0.02044,0.02021,0.01999,
     + 0.01977,0.01955,0.01933,0.01912,0.01891,0.01870,0.01849,0.01828/
      data (qn2o(i,5),i=1,nl)/
     + 0.16164,0.23086,0.26533,0.27844,0.28575,0.29316,0.29779,0.31102,
     + 0.31750,0.31968,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000/
      data (qco (i,5),i=1,nl)/
     + 0.01329,0.02251,0.03395,0.04803,0.06317,0.07730,0.08723,0.10170,
     + 0.11281,0.12079,0.12574,0.12888,0.12998,0.13071,0.13151,0.13240,
     + 0.13326,0.13410,0.13491,0.13599,0.13704,0.13806,0.13905,0.14002,
     + 0.14100,0.14195,0.14287,0.14378,0.14466,0.14552,0.14594,0.14635,
     + 0.14676,0.14717,0.14757,0.14796,0.14836,0.14874,0.14913,0.14951/
      data (qch4(i,5),i=1,nl)/
     + 1.08277,1.26271,1.35958,1.42477,1.46943,1.50397,1.53446,1.58665,
     + 1.62025,1.63640,1.65482,1.67277,1.68375,1.69159,1.69725,1.69797,
     + 1.69867,1.69935,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000/
c * U.S. Standard (1976)
      data (qtem(i,6),i=1,nl)/
     +  217.28, 216.70, 216.70, 216.70, 216.70, 216.70, 216.72, 220.85,
     +  228.58, 235.38, 241.45, 246.94, 251.95, 256.56, 260.85, 262.48,
     +  264.08, 265.64, 267.15, 268.61, 270.07, 271.49, 272.87, 274.21,
     +  275.53, 276.84, 278.12, 279.36, 280.58, 281.77, 282.37, 282.97,
     +  283.55, 284.14, 284.71, 285.28, 285.84, 286.40, 286.95, 287.50/
      data (qh2o(i,6),i=1,nl)/
     +   0.004,  0.004,  0.004,  0.005,  0.008,  0.014,  0.022,  0.057,
     +   0.143,  0.340,  0.533,  0.803,  1.125,  1.498,  2.000,  2.202,
     +   2.455,  2.700,  2.937,  3.168,  3.487,  3.804,  4.111,  4.411,
     +   4.705,  4.994,  5.277,  5.553,  5.823,  6.090,  6.244,  6.397,
     +   6.549,  6.698,  6.846,  6.993,  7.138,  7.281,  7.424,  7.564/
      data (qo3 (i,6),i=1,nl)/
     + 2.86792,1.61138,0.93973,0.62074,0.46009,0.35878,0.29116,0.16277,
     + 0.09861,0.06369,0.05193,0.04424,0.03966,0.03718,0.03466,0.03384,
     + 0.03367,0.03350,0.03334,0.03319,0.03301,0.03283,0.03266,0.03249,
     + 0.03221,0.03160,0.03100,0.03041,0.02984,0.02928,0.02903,0.02878,
     + 0.02854,0.02829,0.02805,0.02782,0.02758,0.02735,0.02712,0.02689/
      data (qn2o(i,6),i=1,nl)/
     + 0.22550,0.26631,0.28563,0.29551,0.30168,0.30642,0.31037,0.31643,
     + 0.31922,0.31994,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,
     + 0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000,0.32000/
      data (qco (i,6),i=1,nl)/
     + 0.01267,0.01943,0.02942,0.04160,0.05514,0.06869,0.08037,0.09586,
     + 0.10769,0.11735,0.12351,0.12738,0.12944,0.13042,0.13101,0.13136,
     + 0.13227,0.13316,0.13402,0.13485,0.13595,0.13704,0.13811,0.13914,
     + 0.14016,0.14119,0.14219,0.14317,0.14412,0.14506,0.14552,0.14597,
     + 0.14643,0.14687,0.14732,0.14775,0.14819,0.14862,0.14904,0.14946/
      data (qch4(i,6),i=1,nl)/
     + 1.37960,1.51874,1.57563,1.60924,1.63289,1.65084,1.66452,1.68124,
     + 1.69160,1.69650,1.69862,1.69965,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,
     + 1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000,1.70000/
c
      m=model
      do 100 l=1,nl
      atem(l)=qtem(l,m)
      ah2o(l)=qh2o(l,m)
      ao3(l)=qo3(l,m)
      an2o(l)=qn2o(l,m)
      aco(l)=qco(l,m)
      ach4(l)=qch4(l,m)
  100 continue
      return
      end
C ****
	subroutine nasticldchk(brt,nb,mch,tb12,tb11,cflag)
C $ April 27 : Modify to subroutine form
C $ March 17/98 : NASTI cloud contaminaion check
        dimension brt(nb,mch)
        xm11m10=-99999.
        xm12m11=-99999.
        xmdif=-99999.
c * Window Brightness Temp. (Averaged)
c * 895 1/cm - 905 1/cm (1142-1184)
          tbw=0.
          kc=0
          do k=1142,1184
             kc=kc+1
             tbw=tbw+brt(1,k)
          enddo
          tbw=tbw/float(kc)
c * 12 microns Brightness Temp. (Averaged)
c * 828 1/cm - 838 1/cm (864-905)
          tb12=0.
          kc=0
          do k=864,905
             kc=kc+1
             tb12=tb12+brt(1,k)
          enddo
          tb12=tb12/float(kc)
c * 11 microns Brightness Temp. (Averaged)
c * 904 1/cm - 914 1/cm (1179-1220)
          tb11=0.
          kc=0
          do k=1179,1220
             kc=kc+1
             tb11=tb11+brt(1,k)
          enddo
          tb11=tb11/float(kc)
c * 10 microns Brightness Temp. (Averaged)
c * 995 1/cm - 1005 1/cm (1557-1598)
          tb10=0.
          kc=0
          do k=1557,1598
             kc=kc+1
             tb10=tb10+brt(1,k)
          enddo
          tb10=tb10/float(kc)
c * Cloud detection
c * (0-clear ; 1-water cloud ; 2-ice cloud ; 3-Mixed cloud ; 4-opaque cloud)
        tb11m10=abs(tb11-tb10)
        tb12m11=abs(tb12-tb11)
        tbdif=abs(tb11m10-tb12m11)
c * Clear (0)
        if(tb11m10.le.2.0 .and. tb12m11.le.2.0) then
          cflag=0.
        else
c * Water cloud (1)
        if(tb11m10.lt.1.5 .and. tb12m11.gt.2.0) then
          cflag=1.
        else
c * Ice cloud (2)
        if(tb12m11.lt.1.0 .and. tb11m10.gt.2.5) then
          cflag=2.
        else
c * Mixed phase cloud (3)
        if(tb11m10.gt.1.8.and.tb12m11.gt.1.8) then
          cflag=3.
        else
c * Opaque cloud (4)
        if(tbw.lt.250.) then
          cflag=4.
        else
c * Undecided
          cflag=-1.
        endif
        endif
        endif
        endif
        endif
c        if(cflag.ne.0.) then
c        write(*,'('' Tb_win;Tb_12;Tb-11;Tb_10:'',4f9.2)')
c     $  tbw,tb12,tb11,tb10
c        write(*,'(''   tb11m10;tb12m11;tbdif;cflag:'',3f9.2,f5.0)')
c     $  tb11m10,tb12m11,tbdif,cflag
c        endif
c       write(*,'('' Maximum of tb11m10;tb12m11;tbdif:'',3f9.3)')
c    $  xm11m10,xm12m11,xmdif
	return
        end
C **************************************************************
      SUBROUTINE SYMVRT(A,S,KN,N)
C $ SUBROUTINE SYMVRT(A,S,KN,N)                   (BTR)
C $ INVERTS SYMMETRIC MATRIX
C $ A = (R) INPUT  INPUT MATRIX
C $ S = (R) OUTPUT  OUTPUT MATRIX
C $ KN = (I) INPUT  NUMBER OF ROWS IN MATRICES
C $ N = (I) INPUT  NUMBER OF COLUMNS IN MATRICES
C $$ SYMVRT = VAS,UTILITY
      REAL*8 A(KN,KN),S(KN,KN),SUM
      S(1,1)=1.0D0/A(1,1)
      IF(N-1)110,110,120
  110 RETURN
  120 DO 130 J=2,N
      S(1,J)=A(1,J)
  130 CONTINUE
c      WRITE(*,'('' FINISHING LOOP 130 !'')')
      DO 160 I=2,N
      IM=I-1
      DO 150 J=I,N
      SUM=0.0D0
      DO 140 L=1,IM
      SUM=SUM+S(L,I)*S(L,J)*S(L,L)
  140 CONTINUE
      S(I,J)=A(I,J)-SUM
  150 CONTINUE
c      WRITE(*,'('' I & SUM : '',I4,1X,D15.2)') I,SUM
      S(I,I)=1.0D0/S(I,I)
  160 CONTINUE
c      WRITE(*,'('' FINISHING LOOP 160 !'')')
      DO 170 I=2,N
      IM=I-1
      DO 170 J=1,IM
  170 S(I,J)=0.0D0
c      WRITE(*,'('' FINISHING LOOP 170 !'')')
      NM=N-1
      DO 180 II=1,NM
      IM=N-II
      I=IM+1
      DO 180 J=1,IM
      SUM=S(J,I)*S(J,J)
      DO 180 K=I,N
      S(K,J)=S(K,J)-S(K,I)*SUM
  180 CONTINUE
c      WRITE(*,'('' FINISHING LOOP 180 !'')')
      DO 210 J=2,N
      JM=J-1
      JP=J+1
      DO 210 I=1,JM
      S(I,J)=S(J,I)
      IF(JP-N)190,190,210
  190 DO 200 K=JP,N
      S(I,J)=S(I,J)+S(K,I)*S(K,J)/S(K,K)
  200 CONTINUE
  210 CONTINUE
c      WRITE(*,'('' FINISHING LOOP 210 !'')')
      DO 250 I=1,N
      IP=I+1
      SUM=S(I,I)
      IF(IP-N)220,220,240
  220 DO 230 K=IP,N
      SUM=SUM+S(K,I)*S(K,I)/S(K,K)
  230 CONTINUE
  240 S(I,I)=SUM
  250 CONTINUE
c      WRITE(*,'('' FINISHING LOOP 250 !'')')
      DO 260 I=1,N
      DO 260 J=I,N
      S(J,I)=S(I,J)
  260 CONTINUE
      RETURN
      END
C **************************
        subroutine getb123(buf,btc,mm,nchm,nb)
C * April 06, read nasti real data, then reformat to FW format
        parameter (maxcha=4096,nhw=100)
	parameter (nch1=2199,nch2=3858,nch3=3070)
	dimension buf(nb,mm)
	integer*4 nch(3),noff(3)
        dimension brt(3,maxcha),btc(nb,nchm),freqc(3,nch2)
	real*8 vxnast,vn
C **** Wallops flight !!!
	data noff/659,-166,519/
c***********************************************************************
	nch(1)=nch1
	nch(2)=nch2
	nch(3)=nch3
c
       	VN=VXNAST(0,0)
c
      	DO n=1,3
      	DO K=1,NCH(n)
       	FREQC(n,K)=VXNAST(n,K)
      	ENDDO
      	ENDDO
c
	do n=1,nb    !  BAND LOOP !!!
	   do L=nhw+1,mm
	    LX=L-nhw
	    brt(n,LX)=buf(n,L)
	   enddo
	do k=1,nch(n)
	kx=k+noff(n)
	btc(n,k)=0.0
	if(kx.gt.0) then
	 btc(n,k)=brt(n,kx)
         VN=FREQC(n,K)
	 rr=amax1(btc(n,k),0.00001)
         btc(n,K)=WNBRIT(VN,RR)
	endif
	enddo
	enddo 	  !  BAND LOOP END !!!
C
	return
        end
c
	subroutine hrtopm(flat,flon, ielev,itype,*,*)
c .... version of 12.05.98
c ** Input
c  	flat  = latitude  (0-090,+N,-S)
c  	flon  = longitude (0-180,+E,-W, or 0-360 +E)
c ** Output
c  	ielev = elevation (meters)
c  	itype = sfc type  (0 = water, 1 = land)
c ** Alternate returns
c	return1 = error opening file
c	return2 = error reading file

	parameter (hf2m=30.48,mrec=109,nloc=900,nrec=2592)
	parameter (nwdb=30,lenb=nwdb*4,lent=7200,iun=29)
	integer mbit(nwdb),mbuf(nwdb,nrec),numbs(nrec)
	integer lrec/-1/
	byte lbuf(lent)
	character*12 bfile/'mapbitls.dat'/
	character*12 tfile/'maptopog.dat'/
	character*12 cfile

c ** On first entry: open, read, and close bitmap file, then open height file.
	if(lrec.lt.0) then
	   cfile=bfile
	   open(iun,file=bfile,recl=lenb,access='direct',
     +	status='old',iostat=ios,err=191)
	   if(ios.ne.0) go to 191
	   numb=0
	   do krec=1,nrec
	      read(iun,rec=krec,iostat=ios,err=193) mbit
	      if(ios.ne.0) go to 193
	      numbs(krec)=numb
	      numb=numb+mbit(nwdb)
	      do i=1,nwdb
	         mbuf(i,krec)=mbit(i)
	      enddo
	   enddo
	   close(iun)

	   cfile=tfile
	   open(iun,file=tfile,recl=lent,access='direct',
     +	status='old',iostat=ios,err=191)
	   if(ios.ne.0) go to 191

	   lrec=0
	endif

	flax=flat+90.
	if(flon.lt.0.) then
	   flox=flon+360.
	else
	   flox=flon
	endif
	irow=flax/5.
	icol=flox/5.+1.
	krec=(irow*72)+icol
	krec=min0(krec,nrec)
	do i=1,nwdb
	   mbit(i)=mbuf(i,krec)
	enddo

	filat=amod(flax,5.)
	filon=amod(flox,5.)
	klat=filat
	klon=filon
	inclat=(filat-float(klat))*6.+0.49
	inclon=(filon-float(klon))*6.+0.49
	inclat=min0(inclat,5)
	inclon=min0(inclon,5)
	ilat=(klat*6)+inclat
	ilon=(klon*6)+inclon
	loc=(ilat*30)+ilon+1
	loc=min0(loc,nloc)

	itype=lbit(loc,mbit)
	if(itype.eq.0) then
c * sea
	   ielev=0
	   return
	endif

	numb=0
	do ibit=1,loc
	   numb=numb+lbit(ibit,mbit)
	enddo
	numb=numb+numbs(krec)
	irec=numb/lent
	iwrd=mod(numb,lent)
	if(iwrd.eq.0) then
	   iwrd=lent
	else
	   irec=irec+1
	endif
	irec=min0(irec,mrec)
	if(irec.ne.lrec) then
	   read(iun,rec=irec,iostat=ios,err=193) lbuf
	   if(ios.ne.0) go to 193
	   lrec=irec
	endif
	ielev=lbuf(iwrd)+128
c * convert from 100's of feet to meters
	elev=hf2m*float(ielev)
	ielev=nint(elev)
	return

191	write(*,192) ios,cfile
192	format('OPEN error IOSTAT =',i8,' FILE = ',a12)
	return1
193	write(*,194) ios,cfile,irec
194	format('READ error IOSTAT =',i8,' FILE = ',a12,
     *       ' REC# = ',i4)
	return2
	end
C *********
	function pfromz(zsfc)
c * Estimate surface pressure from elevation,
c	based on standard atmosphere.
c .... version of 13.04.98

	t = 288. - .0065*zsfc
	p = 1013.25 * (t/288.) ** 5.2549964
	pfromz = p

	return
	end
C *************
      function lbit(j,array)
c **** version of 06.12.84
      integer*4 array(*)
      logical*4 btest
      jw=(j-1)/32
      nbit=j-jw*32
      jword=array(jw+1)
      ibit=32-nbit
      jout=0
      if(btest(jword,ibit)) jout=1
      lbit=jout
      return
      end
C **************************************************************
        subroutine evregnr(twcoeff,inevf,brt,nband,mch,psurf,zena,y,
     &                     leng,init)
C * April 28, 1998
C * Band 1 Use only 730 - 1070 (458 - 1868 ) -> 1411
C * Band 2 Use only 1400- 1500 (1038- 1453 ) ->  416
C ***************************************************************
c        parameter (nl=40,nall=1660+1453)
C * April 28, 1998
c        parameter (nl=40,nall=1411+416)
C * May 12, 1998
	parameter (nchb1=112+130,nchb2=101)
        parameter (nl=40,nall=nchb1+nchb2)

        parameter (maxcha=3858,nuse=nall)
c * Use nfov * 100 EV + local zenith + surface pressure as predictors
        parameter (nxx=100,nx=nxx+1+1)
c * nty : Tem (40) ; H2O (40) ; O3 (40) ; Tskin (1)
	parameter (ntwx=nx,ntwy=3*nl+1)
        dimension brt(nband,mch)
        character*80 inevf,twcoeff
        dimension coeftw(ntwx,ntwy)
        dimension xtw(ntwx),ytw(ntwy)
        dimension y(leng),ytwb(ntwy)
        dimension nbeg(2),nend(2),bt(nuse)
        dimension cc(nxx),vcv(nuse,nxx)
	real*8 work(nuse)
	integer*4 b1idx(nchb1),b2idx(nchb2),nchb(2)
	data iuctw/11/
	data iev/10/
c        data nbeg/209,416/,nend/1868,1868/
C * April 28, 1998
        data nbeg/458,1038/,nend/1868,1453/

C *** coeff from real noise !
c      data twcoeff/ '../data/camx97ax.evtwo.b12coef.fac1.0.nosn980711'/
C ***
c        data inevf/'../data/camx97ax.ev.newn'/
C *********************************************************
	data b1idx/130*0,
     & 	           377,382,386,393,397,400,407,418,422,430,
     &             437,446,452,458,465,471,478,484,490,497,
     &             509,513,519,525,532,535,541,546,550,556,
     &             563,569,575,581,587,594,600,606,613,618,
     &             623,628,633,640,647,652,659,670,684,699,
     &             714,723,732,740,751,763,777,783,789,799,
     &             809,828,837,856,863,879,889,895,905,914,
     &             921,930,944,954,963,968,975,1011,1020,1031,
     &   1044,1058,1073,1087,1102,1110,1134,1163,1179,1200,
     &   1210,1229,1245,1255,1261,1282,1290,1299,1307,1315,
     &   1323,1331,1346,1360,1368,1375,1384,1402,1408,1419,
     &   1425,1431/
	data b2idx/653,668,676,684,691,698,715,724,734,744,
     &             755,767,785,814,827,848,858,870,877,882,
     &             905,925,936,954,980,989,1000,1012,1036,1055,
     &   1060,1077,1093,1105,1121,1139,1149,1164,1170,1184,
     &   1194,1214,1234,1244,1254,1262,1285,1302,1312,1328,
     &   1348,1355,1372,1382,1418,1431,1441,1464,1507,1530,
     &   1577,1603,1646,1668,1712,1728,1755,1776,1814,1824,
     &   1851,1865,1873,1881,1908,1928,1944,2000,2042,2050,
     &   2111,2141,2171,2193,2245,2319,2370,2375,2385,2392,
     &   2402,2433,2454,2508,2529,2540,2556,2571,2591,2606,
     &   2624/
c***********************************************************************
	do L=1,130
	b1idx(L)=228+L
	enddo
C ***********************************
	if(init.eq.0) then
	init=1
	lentw=ntwy*4
	open(iuctw,file=twcoeff,recl=lentw,access='direct',
     $    status='old')
	lenxy=nuse*8
	open(iev,file=inevf,recl=lenxy,access='direct',
     $    status='old')
cccc
        do i=1,nxx
        read(iev,rec=i)work
        sum=0.0
        do j=1,nuse
        vcv(j,i)=work(j)
        sum=sum+vcv(j,i)**2
        enddo
c * Make sure the sum of (Vec)*(Vec)Transport are all ones
c        write(*,'('' SUM of E-V='',f10.3)')sum
        enddo
cccc
	write(*,'('' Begin to read coef. of T and YTB !!!'')')
	do i=1,ntwx
	read(iuctw,rec=i) ytw
	do j=1,ntwy
	coeftw(i,j)=ytw(j)
	enddo
	enddo
c
	read(iuctw,rec=ntwx+1) ytwb
	close(iuctw)
	close(iev)
	endif
cccc
	nchb(1)=nchb1
	nchb(2)=nchb2
	   kx=0
	   do n=1,2
c           do k=nbeg(n),nend(n)
           do kj=1,nchb(n)
	    if(n.eq.1)k=b1idx(kj)
	    if(n.eq.2)k=b2idx(kj)
            kx=kx+1
            bt(kx)=brt(n,k)
           enddo
          enddo
        if(nuse.ne.kx) then
          write(*,'('' I/O problem ; nuse;kx='',2i8)') nuse,kx
          stop
        endif
c *********
C * Transform Tbs to Eigen-Vector domain
        do i=1,nxx
        cc(i)=0.0
        do j=1,nuse
        cc(i)=cc(i)+bt(j)*vcv(j,i)
        enddo
        xtw(i)=cc(i)
        enddo
c * Surface pressure (mb)
        xtw(nxx+1) = psurf
c * Local zenith angle (degree)
        xtw(nxx+2)=100.0 /cos(zena*0.01745329)
C *********
	do i=1,ntwy
	sum=0.0
	do j=1,ntwx
	sum=sum+xtw(j)*coeftw(j,i)
	enddo
	y(i)=sum+ytwb(i)
        if(i.le.3*nl .and. i.gt.nl) then
        y(i)=exp(y(i))
        endif
	enddo
c
	return
        end
c
        subroutine getnosb123(buf,btc,mm,nchm,nb)
C * April 06, read nasti real data, then reformat to FW format
        parameter (maxcha=4096,nhw=100)
	parameter (nch1=2199,nch2=3858,nch3=3070)
	dimension buf(nb,mm)
	integer*4 nch(3),noff(3)
        dimension brt(3,maxcha),btc(nb,nchm),freqc(3,nch2)
	real*8 vxnast,vn
C **** Theoretical
C	data noff/659,-166,519/
C **** Dryden flight !!!
c	data noff/660,-164,522/
C **** Wallops flight
	data noff/659,-166,519/
c***********************************************************************
	nch(1)=nch1
	nch(2)=nch2
	nch(3)=nch3
c
       	VN=VXNAST(0,0)
c
      	DO n=1,3
      	DO K=1,NCH(n)
       	FREQC(n,K)=VXNAST(n,K)
      	ENDDO
      	ENDDO
c
	do n=1,nb    !  BAND LOOP !!!
	   do L=nhw+1,mm
	    LX=L-nhw
	    brt(n,LX)=buf(n,L)
	   enddo
	do k=1,nch(n)
	kx=k+noff(n)
	btc(n,k)=0.0
	if(kx.gt.0) then
	 btc(n,k)=brt(n,kx)
         VN=FREQC(n,K)
	 rr=amax1(btc(n,k),0.00001)
         btc(n,K)=rr
	endif
	enddo
	enddo 	  !  BAND LOOP END !!!
C
	return
        end
C ***
      FUNCTION CHOP(X,YMIN,YMAX)
C $ FUNCTION CHOP(X,YMIN,YMAX)            (BTR)
C $ FORCE A VALUE TO LIE BETWEEN A MINIMUM AND MAXIMUM
C $ X = (R) INPUT  VALUE
C $ YMIN = (R) INPUT  MINIMUM
C $ YMAX = (R) INPUT  MAXIMUM
C $$ CHOP=VAS,UTILITY
      Y=X
      Y=AMAX1(Y,YMIN)
      Y=AMIN1(Y,YMAX)
      CHOP=Y
      RETURN
      END
C ***
      FUNCTION WSAT(P,T)
C $ FUNCTION WSAT(P,T)                   (BTR)
C $ GET MIXING RATIO, GIVEN P, T (ASSUME DEWPOINT DEPRESSION = 0)
C $ P = (R) INPUT  PRESSURE
C $ T = (R) INPUT  TEMPERATURE
C $$ WSAT = VAS,COMPUTATION
      CALL WMIX(P,T,0.,WS,1)
      WSAT=WS
      RETURN
      END
C ******************
      SUBROUTINE WMIX(P,T,DD,W,NL)
C $ GET MIXING RATIO, GIVEN P, T, TD
C $ P = (R) INPUT  PRESSURE
C $ T = (R) INPUT  TEMPERATURE
C $ DD = (R) INPUT  DEWPOINT DEPRESSION
C $ W = (R) OUTPUT  MIXING RATIO
C $ NL = (I) INPUT  NUMBER OF LEVELS
C $$ WMIX = VAS,COMPUTATION
CXR   SATVAP,VPICE
C *** 'DD' IS DEWPOINT DEPRESSION
      DIMENSION P(1),T(1),DD(1),W(1)
      DO 10 I=1,NL
      TD=T(I)-DD(I)
      IF(T(I).GT.253.) GO TO 3
CCCCC ES=VPICE(TD)
      ES=SVPICE(TD)
      GO TO 7
CCCC3 ES=SATVAP(TD)
    3 ES=SVPWAT(TD)
    7 W(I)=622.*ES/P(I)
   10 CONTINUE
      RETURN
      END
C
      FUNCTION SVPICE(TEMP)
C     SATURATION VAPOR PRESSURE OVER ICE
C **** TEMP MUST BE IN DEGREES KELVIN
      REAL*8 T,A0,A1,A2,A3,A4,A5
      DATA A0/.7859063157D0/,A1/.3579242320D-1/,A2/-.1292820828D-3/,
     *     A3/.5937519208D-6/,A4/.4482949133D-9/,A5/.2176664827D-10/
      T=TEMP-273.16
      E=A0+T*(A1+T*(A2+T*(A3+T*(A4+T*A5))))
      SVPICE=10.**E
      RETURN
      END
C ***
      FUNCTION SVPWAT(TEMP)
C     SATURATION VAPOR PRESSURE OVER WATER
C **** TEMP MUST BE IN DEGREES KELVIN
      REAL*8 B,S,T,A0,A1,A2,A3,A4,A5,A6,A7,A8,A9
      DATA A0/.999996876D0/,A1/-.9082695004D-2/,A2/.7873616869D-4/,
     *  A3/-.6111795727D-6/,A4/.4388418740D-8/,A5/-.2988388486D-10/,
     *  A6/.2187442495D-12/,A7/-.1789232111D-14/,A8/.1111201803D-16/,
     *  A9/-.3099457145D-19/,B/.61078D+1/
      T=TEMP-273.16
      S=A0+T*(A1+T*(A2+T*(A3+T*(A4+T*(A5+T*(A6+T*(A7+T*(A8+T*A9))))))))
      S=B/S**8
      SVPWAT=S
      RETURN
      END
C ***********************************************
        FUNCTION ANOISE(IX,SD,XM)
C
	X=URAND(IX)
        A=0.
        DO 1 I=1,12
		A=A+URAND(IX)
1       CONTINUE
        ANOISE=XM+(A-6.)*SD
        RETURN
        END
C **************************************************
      FUNCTION URAND(IY)
C   TAKEN FROM FORSYTHE,MALCOLM,MOLER: "COMPUTER METHODS
C   FOR MATHEMATICAL COMPUTATIONS," PRENTICE HALL,1977,259PP.
C   THE ROUTINE CAN BE FOUND IN LAST CHAPTER, P246.
C   PROGRAMMER: LOUIS GARAND, JULY 1983
C
C   URAND IS A UNIFORM RANDOM NUMBER GENERATOR.  THE 'SEED' IY SHOULD
C   BE INITIALIZED TO AN ARBITRARY INTEGER PRIOR TO THE FIRST CALL
C   TO URAND.  THE CALLING PROGRAM SHOULD NOT ALTER THE VALUE OF IY
C   WHICH IS CHANGED INTERNALLY BY URAND ITSELF.
C   VALUES OF URAND ARE RETURNED IN THE RANGE 0. TO 1.
C
      REAL*8 HALFM,DATAN,DSQRT
      DATA M2/0/,ITWO/2/
      IF(M2.NE.0) GO TO 120
C
C * ON FIRST ENTRY, COMPUTE MACHINE INTEGER WORD LENGTH
      M=1
  110 M2=M
      M=ITWO*M2
      IF(M.GT.M2) GO TO 110
      HALFM=M2
C
C * COMPUTE MULTIPLIER AND INCREMENT FOR LINEAR CONGRUENTIAL METHOD
      IA=8*IDINT(HALFM*DATAN(1.D0)/8.D0) + 5
      IC=2*IDINT(HALFM*(0.5D0-DSQRT(3.D0)/6.D0)) +1
      MIC=(M2-IC)+M2
C * OBTAIN SCALE FACTOR FOR CONVERTING TO FLOATING POINT
      S=0.5/HALFM
C
C * COMPUTE NEXT RANDOM NUMBER
  120 IY=IY*IA
C * FOR COMPUTERS NOT ALLOWING INTEGER OVERFLOW ON ADDITION:
      IF(IY.GT.MIC) IY=(IY-M2)-M2
      IY=IY+IC
C * FOR COMPUTERS WITH WORD LENGTH FOR ADDITION GREATER THAN FOR
C    MULTIPLICATION:
      IF(IY/2.GT.M2) IY=(IY-M2)-M2
C * FOR COMPUTERS WHERE INTEGER OVERFLOW AFFECTS THE SIGN BIT:
      IF(IY.LT.0) IY=(IY+M2)+M2
      URAND=FLOAT(IY)*S
      RETURN
      END
C **************************************************************
        subroutine evregnb1r(twcoeff,inevf,brt,nband,mch,psurf,zena,y,
     $                       leng,init)
C * April 28, 1998
C * Band 1 Use only 730 - 1070 (458 - 1868 ) -> 1411
C * Band 2 Use only 1400- 1500 (1038- 1453 ) ->  416
C ***************************************************************
C * May 12, 1998
	parameter (nchb1=112+130,nchb2=101)
c        parameter (nl=40,nall=nchb1+nchb2)
C *** B1 only; August 09, 1998
        parameter (nl=40,nall=nchb1+0)

        parameter (maxcha=3858,nuse=nall)
c * Use nfov * 100 EV + local zenith + surface pressure as predictors
        parameter (nxx=100,nx=nxx+1+1)
c * nty : Tem (40) ; H2O (40) ; O3 (40) ; Tskin (1)
	parameter (ntwx=nx,ntwy=3*nl+1)
        dimension brt(nband,mch)
        character*80 inevf,twcoeff
        dimension coeftw(ntwx,ntwy)
        dimension xtw(ntwx),ytw(ntwy)
        dimension y(leng),ytwb(ntwy)
        dimension nbeg(2),nend(2),bt(nuse)
        dimension cc(nxx),vcv(nuse,nxx)
	real*8 work(nuse)
	integer*4 b1idx(nchb1),b2idx(nchb2),nchb(2)
	data iuctw/11/
	data iev/10/
c        data nbeg/209,416/,nend/1868,1868/
C * April 28, 1998
        data nbeg/458,1038/,nend/1868,1453/

C *** coeff from real noise !
c      data twcoeff/ '../data/camx97ax.evtwo.b1coef.fac2.0.nosn980711.wff'/
C ***
c        data inevf/'../data/camx97ax.ev.newnb1'/
C *********************************************************
	data b1idx/130*0,
     & 	           377,382,386,393,397,400,407,418,422,430,
     &             437,446,452,458,465,471,478,484,490,497,
     &             509,513,519,525,532,535,541,546,550,556,
     &             563,569,575,581,587,594,600,606,613,618,
     &             623,628,633,640,647,652,659,670,684,699,
     &             714,723,732,740,751,763,777,783,789,799,
     &             809,828,837,856,863,879,889,895,905,914,
     &             921,930,944,954,963,968,975,1011,1020,1031,
     &   1044,1058,1073,1087,1102,1110,1134,1163,1179,1200,
     &   1210,1229,1245,1255,1261,1282,1290,1299,1307,1315,
     &   1323,1331,1346,1360,1368,1375,1384,1402,1408,1419,
     &   1425,1431/
	data b2idx/653,668,676,684,691,698,715,724,734,744,
     &             755,767,785,814,827,848,858,870,877,882,
     &             905,925,936,954,980,989,1000,1012,1036,1055,
     &   1060,1077,1093,1105,1121,1139,1149,1164,1170,1184,
     &   1194,1214,1234,1244,1254,1262,1285,1302,1312,1328,
     &   1348,1355,1372,1382,1418,1431,1441,1464,1507,1530,
     &   1577,1603,1646,1668,1712,1728,1755,1776,1814,1824,
     &   1851,1865,1873,1881,1908,1928,1944,2000,2042,2050,
     &   2111,2141,2171,2193,2245,2319,2370,2375,2385,2392,
     &   2402,2433,2454,2508,2529,2540,2556,2571,2591,2606,
     &   2624/
c***********************************************************************
	do L=1,130
	b1idx(L)=228+L
	enddo
C ***********************************
	if(init.eq.0) then
	init=1
	lentw=ntwy*4
	open(iuctw,file=twcoeff,recl=lentw,access='direct',
     $    status='old')
	lenxy=nuse*8
	open(iev,file=inevf,recl=lenxy,access='direct',
     $    status='old')
cccc
        do i=1,nxx
        read(iev,rec=i)work
        sum=0.0
        do j=1,nuse
        vcv(j,i)=work(j)
        sum=sum+vcv(j,i)**2
        enddo
c * Make sure the sum of (Vec)*(Vec)Transport are all ones
c        write(*,'('' SUM of E-V='',f10.3)')sum
        enddo
cccc
	write(*,'('' Begin to read coef. of T and YTB !!!'')')
	do i=1,ntwx
	read(iuctw,rec=i) ytw
	do j=1,ntwy
	coeftw(i,j)=ytw(j)
	enddo
	enddo
c
	read(iuctw,rec=ntwx+1) ytwb
	close(iuctw)
	close(iev)
	endif
cccc
	nchb(1)=nchb1
	nchb(2)=nchb2
	   kx=0
	   do 2400 n=1,2
C **** B1 only; August 09, 1998
		if(n.eq.2) go to 2400
c           do k=nbeg(n),nend(n)
           do kj=1,nchb(n)
	    if(n.eq.1)k=b1idx(kj)
	    if(n.eq.2)k=b2idx(kj)
            kx=kx+1
            bt(kx)=brt(n,k)
           enddo
2400	continue
        if(nuse.ne.kx) then
          write(*,'('' I/O problem ; nuse;kx='',2i8)') nuse,kx
          stop
        endif
c *********
C * Transform Tbs to Eigen-Vector domain
        do i=1,nxx
        cc(i)=0.0
        do j=1,nuse
        cc(i)=cc(i)+bt(j)*vcv(j,i)
        enddo
        xtw(i)=cc(i)
        enddo
c * Surface pressure (mb)
        xtw(nxx+1) = psurf
c * Local zenith angle (degree)
        xtw(nxx+2)=100.0 /cos(zena*0.01745329)
C *********
	do i=1,ntwy
	sum=0.0
	do j=1,ntwx
	sum=sum+xtw(j)*coeftw(j,i)
	enddo
	y(i)=sum+ytwb(i)
        if(i.le.3*nl .and. i.gt.nl) then
        y(i)=exp(y(i))
        endif
	enddo
c
	return
        end
C
      FUNCTION DEWPT(P,T,W)
C $ FUNCTION DEWPT(P,T,W)        (BTR)
C $ DETERMINE DEWPOINT GIVEN PRESSURE, TEMPERATURE AND MIXING RATIO
C $ P = (R) INPUT   PRESSURE
C $ T = (R) INPUT  TEMPERATURE
C $ W = (R) INPUT  MIXING RATIO
C $$ DEWPT=VAS,COMPUTATION
      SATV=W*P/622.
      IF(T.GT.253.) GO TO 10
      TD=TVPICE(SATV)
      GO TO 20
   10 TD=TEMSAT(SATV)
   20 IF(TD.EQ.0.) TD=T-40.
      TD=AMIN1(TD,T)
      DEWPT=TD
      RETURN
      END
C
      FUNCTION TEMSAT(PRESS)
C $ TEMSAT(PRESS)  (N)
C $ GET KELVIN TEMPERATURE AT SPECIFIED SATURATION VAPOR PRESSURE (MB)
C $   OVER WATER.  0 IS RETURNED IF GIVEN VAPOR PRESSURE IS OUT OF
C $   RANGE OF THE APPROXIMATION (.0636 TO 123.3972)
C $ PRESS = (R) INPUT  SATURATION VAPOR PRESSURE (MB)
C $$ TEMSAT = VAS,COMPUTATION
C
      REAL*8 DVP
C
      IF(PRESS.LT. .0636 .OR. PRESS.GT.123.3972) GO TO 10
      DVP = ALOG10(PRESS)
      TEMSAT = -.225896152438D+2 + DVP*(.261012286592D+2
     1  + DVP*(.30206720594D+1 + DVP*(.370219024579D+0
     2  + .72838702401D-1 * DVP))) + 273.16D+0
      RETURN
C
10    TEMSAT = 0.
      RETURN
      END
C
      FUNCTION TVPICE(PRESS)
C $ FUNCTION TVPICE(PRESS)                     (BTR)
C $ COMPUTE TEMP AT WHICH THE GIVEN VAPOR PRESSURE OVER ICE IS AT
C $   SATURATION.  THIS IS THE INVERSE OF 'VPICE'.
C $ PRESS = (R) INPUT  VAPOR PRESSURE
C $$ TVPICE = VAS, COMPUTATION
      REAL*8 V,A0,A1,A2,A3,A4,TV
C
      DATA A0/-.2031888177D+2/, A1/.2394167436D+2/
      DATA A2/.2252719878D+1/, A3/.1914055442D+0/, A4/.9636593860D-2/
C
      V = PRESS
      IF(V.LT.1.403D-5 .OR. V.GT.6.108D0) GO TO 900
      V = DLOG10(V)
      TV = A0 + V*(A1 + V*(A2 + V*(A3 + V*A4)))
      TVPICE = TV + 273.16D+0
      RETURN
C
  900 TVPICE = 0.
      RETURN
      END
