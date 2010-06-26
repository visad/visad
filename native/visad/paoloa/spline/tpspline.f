      subroutine tpspline(x_arr,y_arr,s_arr,ytrue,y,dimen)
c
c  Purpose: Test the gcvpack driver dtpss.f with VisAD
c
c
      integer maxobs, maxuni, maxpar, maxtbl, mxncts, lwa, liwa
      integer jJ
      parameter ( maxobs = 200 , maxtbl = 200, mxncts = 10,
     *  maxuni = 150 ,maxpar = maxuni+mxncts,
     *  lwa = maxuni*(2+mxncts+maxuni)+maxobs,
     *  liwa = 2*maxobs + maxuni)
c
      integer dimen(2)
      integer nobs,i,j,info,ntbl,ncov1,job,m,dim,iwork(liwa),iout(4)
      integer dimen1, dimen2
      double precision s(maxobs,1),s2(maxobs,1),lamlim(2)
      double precision trA,diag,adiag(maxobs),auxtbl(3,3),pderr,r
      double precision des(maxobs,4),des2(maxobs,4),y(maxobs)
      double precision ytrue(maxobs),tbl(maxtbl,3),coef(maxpar)
      double precision svals(maxobs),dout(5),pred(maxobs),work(lwa)
C      double precision s(maxobs,1),s2(maxobs,1),lamlim(2),
C     * des(maxobs,4),des2(maxobs,4),y(maxobs),adiag(maxobs),
C     * ytrue(maxobs),tbl(maxtbl,3),coef(maxpar),auxtbl(3,3),
C     * svals(maxobs),dout(5),pred(maxobs),work(lwa),pderr,r,trA,diag
      double precision x_arr(121),y_arr(121),s_arr(121)
c
      double precision dasum
c
      dimen1 = dimen(1)
      dimen2 = dimen(2)
      print *, dimen1, dimen2

      nobs=dimen1*dimen2

      print *, dimen1, dimen2, nobs

      dim=2
      ncov1=1
      m=2
      ntbl=200
      job=1010

      do i=1,nobs
         des(i,1)=x_arr(i)
         des(i,2)=y_arr(i)
         s(i,1)=s_arr(i)
      enddo

      do 20 i = 1,dim
          call dcopy(nobs,des(1,i),1,des2(1,i),1)
   20 continue
      do 30 i = 1,ncov1
      call dcopy(nobs,s(1,i),1,s2(1,i),1)
   30 continue
      call dcopy(nobs,ytrue,1,adiag,1)

      call dtpss(des,maxobs,nobs,dim,m,s,maxobs,ncov1,y,ntbl,adiag,
     * lamlim,dout,iout,coef,svals,tbl,maxtbl,auxtbl,work,lwa,iwork,
     * liwa,job,info)
      if (info .ne. 0) write(*,*) 'dtpss info',info
      do 35 i=1,nobs
C          write(*,*) 'y(',i,') = ',y(i)
   35 continue
      call dpred(des2,maxobs,nobs,dim,m,des,maxobs,iout(4),s2,maxobs,
     *  ncov1,0,coef,iout(2),pred,work,lwa,iwork,info)
      if (info .ne. 0) write(*,*) 'dpred info',info


c      write(*,*) 'lamlim = ',lamlim(1),lamlim(2)
c      write(*,*) 'dout:'
c      write(*,*) '      lamhat           ',dout(1)
c      write(*,*) '      penlty           ',dout(2)
c      write(*,*) '      rss              ',dout(3)
c      write(*,*) '      sqrt(rss/nobs)   ',sqrt(dout(3)/nobs)
c      write(*,*) '      tr(I-A)          ',dout(4)
c      write(*,*) '      ssqrep           ',dout(5)
c      write(*,*) 'iout:'
c      write(*,*) '      npsing           ',iout(1)
c      write(*,*) '      npar             ',iout(2)
c      write(*,*) '      nnull            ',iout(3)
c      write(*,*) '      nuobs            ',iout(4)
c      write(*,*) 'auxtbl'
c      do 40 i = 1,3
c           write(*,*) (auxtbl(i,j),j=1,3)
c   40 continue

c      write(*,*) 'Coefficient estimates',(coef(i),i=1,iout(2))
c      write(*,*) 'Singular values'
c      write(*,'(1p,7g11.3)') (svals(i), i = 1, iout(1))
      R=0.0d0
      do 50 i=1,nobs
          R=R+dble((ytrue(i)-y(i))**2)
          pderr = pred(i)-y(i)
          write(*,*) pred(i)
   50 continue
      R=R/dble(nobs)
      diag= dasum(nobs,adiag,1)
      trA=dble(nobs)-dout(4)
      if (abs(trA-diag) .gt. 1.0d-8) write(*,*) 'trA',trA,'diag',diag
C      if (abs(R - auxtbl(1,3)) .gt. 1.0d-8) then
C           write(*,*) 'R=',R,'auxtblR=',auxtbl(1,3)
C      endif
C      if (abs(pderr) .gt. 1.0d-8) write(*,*) 'pderr = ',pderr
C      write(*,*) 'pderr = ',pderr

      return
  999 continue
      end

c -----------------------------------------------------------
      subroutine  dcopy(n,dx,incx,dy,incy)
c
c     copies a vector, x, to a vector, y.
c     uses unrolled loops for increments equal to one.
c     jack dongarra, linpack, 3/11/78.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision dx(*),dy(*)
      integer i,incx,incy,ix,iy,m,mp1,n
c
      if(n.le.0)return
      if(incx.eq.1.and.incy.eq.1)go to 20
c
c        code for unequal increments or equal increments
c          not equal to 1
c
      ix = 1
      iy = 1
      if(incx.lt.0)ix = (-n+1)*incx + 1
      if(incy.lt.0)iy = (-n+1)*incy + 1
      do 10 i = 1,n
        dy(iy) = dx(ix)
        ix = ix + incx
        iy = iy + incy
   10 continue
      return
c
c        code for both increments equal to 1
c
c
c        clean-up loop
c
   20 m = mod(n,7)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dy(i) = dx(i)
   30 continue
      if( n .lt. 7 ) return
   40 mp1 = m + 1
      do 50 i = mp1,n,7
        dy(i) = dx(i)
        dy(i + 1) = dx(i + 1)
        dy(i + 2) = dx(i + 2)
        dy(i + 3) = dx(i + 3)
        dy(i + 4) = dx(i + 4)
        dy(i + 5) = dx(i + 5)
        dy(i + 6) = dx(i + 6)
   50 continue
      return
      end


c -----------------------------------------------------------
      subroutine dtpss(des,lddes,nobs,dim,m,s,lds,ncov,y,ntbl,adiag,
     * lamlim,dout,iout,coef,svals,tbl,ldtbl,auxtbl,work,lwa,
     * iwork,liwa,job,info)
      integer lddes,nobs,dim,m,lds,ncov,ntbl,iout(4),ldtbl,lwa,
     * liwa,iwork(liwa),job,info
      double precision des(lddes,dim),s(lds,*),y(nobs),
     * adiag(nobs),lamlim(2),dout(5),coef(*),svals(*),
     * tbl(ldtbl,3),auxtbl(3,3),work(lwa)
c
c Purpose: determine the generalized cross validation estimate of the
c 	smoothing parameter and fit model parameters for a thin plate
c 	smoothing spline.
c
c On Entry:
c   des(lddes,dim) 	design for the variables to be splined
c   lddes		leading dimension of des as declared in calling
c   			program
c   nobs		number of observations
c   dim			number of columns in des
c   m			order of the derivatives in the penalty
c   s(lds,ncov) 	design for the covariates. The covariates
c			must duplicate the replication structure of des.
c			See dptpss to handle covariates which do not.
c   lds			leading dimension of s as declared in calling
c   			program
c   ncov		number of covariates
c   y(nobs)		response vector
c   ntbl		number of evenly spaced values for
c			log10(nobs*lambda) to be used in the initial
c			grid search for lambda hat
c			if ntbl = 0 only a golden ratio search will be
c			done and tbl is not referenced, if ntbl > 0
c			there will be ntbl rows returned in tbl
c   adiag(nobs)	 	"true" y values on entry if predictive mse is
c			requested
c   lamlim(2)		limits on lambda hat search (in log10(nobs*
c			lambda)	scale) if user input limits are
c			requested if lamlim(1) = lamlim(2) then lamhat
c			is set to (10**lamlim(1))/nobs
c   ldtbl		leading dimension of tbl as declared in the
c			calling program
c   job			integer with decimal expansion abdc
c			if a is nonzero then predictive mse is computed
c			   using adiag as true y
c			if b is nonzero then user input limits on search
c			   for lambda hat are used
c			if c is nonzero then adiag will be calculated
c			if d is nonzero then there are replicates in the
c			   design
c
c On Exit:
c   des(lddes,dim)	sorted unique rows of des if job indicates that
c			there are replicates otherwise not changed
c   s(lds,ncov)		unique rows of s sorted to correspond to des
c   y(nobs)		predicted values
c   adiag(nobs)		diagonal elements of the hat matrix if requested
c   lamlim(2)		limits on lambda hat search
c			(in log10(nobs*lambda) scale)
c   dout(5)		contains:
c  			1  lamhat   generalized cross validation
c				    estimate of the smoothing parameter
c			2  penlty   smoothing penalty
c			3  rss	    residual sum of squares
c			4  tr(I-A)  trace of I - A
c   			5  ssqrep   sum of squares for replication
c   iout(4)		contains:
c			1  npsing   number of positive singular
c				    values (npsing = nuobs - ncts).
c				    if info indicates nonzero info in
c				    dsvdc then npsing contains info as
c				    it was returned from dsvdc.
c			2  npar	    number of parameters
c				    (npar = nuobs + ncts)
c			3  ncts     dimension of the polynomial space
c				    plus ncov
c				    ((m+dim-1 choose dim) + ncov)
c			4  nuobs    number of unique rows in des
c   coef(npar)		coefficient estimates [beta':alpha':delta']'
c			coef must have a dimension of at least nuobs+
c			ncts
c   svals(npar-nnull)	singular values of the matrix j2 if info = 0
c			if info indicates nonzero info from dsvdc then
c			svals is as it was returned from dsvdc.
c   tbl(ldtbl,3)	column	contains
c			  1 	grid of log10(nobs*lambda)
c			  2  	V(lambda)
c			  3     R(lambda) if requested
c   auxtbl(3,3)		auxiliary table
c			1st row contains:
c			    log10(nobs*lamhat), V(lamhat) and
c			    R(lamhat) if requested
c			    where lamhat is the gcv estimate of lambda
c			2nd row contains:
c			    0, V(0) and  R(0) if requested
c			3rd row contains:
c			    0, V(infinity) and R(infinity) if requested
c   info		error indicator
c			  0 : successful completion
c			 -1 : log10(nobs*lamhat) <= lamlim(1)
c			      (not fatal)
c			 -2 : log10(nobs*lamhat) >= lamlim(2)
c			      (not fatal)
c			  1 : dimension error
c			  2 : error in dreps, covariates do not
c			      duplicate the replication structure of des
c			  3 : lwa (length of work) is too small
c			  4 : liwa (length of iwork) is too small
c			  10 < info < 20 : 10 + nonzero info returned
c					   from dsetup
c			  100< info <200 : 100 + nonzero info returned
c					   from dsgdc1
c			  200< info <300 : 200 + nonzero info returned
c					   from dgcv1
c
c Work Arrays:
c   work(lwa)		double precision work vector
c   lwa			length of work as declared in the calling
c			program
c			Must be at least nuobs(2+ncts+nuobs)+nobs
c   iwork(liwa)		integer work vector
c   liwa		length of iwork as declared in the calling
c			program
c			Must be at least 2*nobs + nuobs - ncts
c
c Subprograms Called Directly:
c       Gcvpack - dreps duni dsuy dsetup dsgdc1 dgcv1
c       Other   - dprmut mkpoly
c
c Subprograms Called Indirectly:
c	Gcvpack - dcfcr1 drsap dvlop dsvtc dpdcr dpmse
c		  dvmin dvl dmaket dmakek ddiag
c	Linpack - dchdc dqrdc dqrsl dtrsl dsvdc
c	Blas    - ddot dcopy dgemv
c	Other 	- dprmut dset dftkf fact mkpoly
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer ncts,jrep,jadiag,p1,p1p1,p2,p2p1,p3,p3p1,ip1,ip1p1,ip2,
     * ip2p1,nuobs,p4,p4p1,j,i,lwa2,wsize,q1,q1p1,npsing,
     * jobgcv
      double precision ssqrep
      integer mkpoly
c
c
      info = 0
      ncts = mkpoly(m,dim) + ncov
      iout(3) = ncts
      jrep = mod(job,10)
      jadiag = mod(job,100)/10
      jobgcv = job/10
c			check dimensions
      if((nobs .le. 0) .or. (m .le. 0) .or. (dim .le. 0) .or.
     * (ntbl .lt. 0) .or. (ntbl .gt. ldtbl) .or.
     * (2*m-dim .le. 0)) then
         info = 1
         return
      endif
c			set up pointers for iwork vector
c  			first nobs positions of iwork contain order
      ip1 = nobs
c			next nobs positions of iwork contain xrep
      ip1p1 = ip1 + 1
      ip2 = ip1 + nobs
c			rest of iwork is a integer work vector
      ip2p1 = ip2 + 1
      if (jrep .ne. 0) then
         call dreps(des,lddes,nobs,dim,s,lds,ncov,0,work,iwork,nuobs,
     *    iwork(ip1p1),1,info)
         if (info .ne. 0) then
            info = 2
            return
         endif
c			put unique row of des into des
         call duni(des,lddes,nobs,dim,iwork(ip1p1),des,lddes)
c			put unique row of s into s
         call duni(s,lds,nobs,ncov,iwork(ip1p1),s,lds)
      endif
      if (jrep .eq. 0) then
         work(1)= 0
         nuobs = nobs
         ssqrep = 0.0d0
      endif
      iout(2) = nuobs + ncts
      iout(4) = nuobs
c			check size of work vectors
      wsize = nuobs*(2+ncts+nuobs)+nobs
      if (lwa .lt. wsize) then
         info = 3
         return
      endif
      wsize = 2*nobs + nuobs - ncts
      if (liwa .lt. wsize) then
         info = 4
         return
      endif
c			set up pointers for c1,[tu:su1],ku,fgaux in work
c
c		c1  	 runs from 1    to p1,   p1 = nuobs
c		[tu:su1] runs from p1p1 to p2,   p2 = p1 + nuobs*ncts
c		ku	 runs from p2p1 to p3,   p3 = p2 + nuobs**2
c		fgaux    runs from p3p1 to p4,   p4 = p3 + ncts
c		the rest of work is a work vector

c  			after the call to dsetup
c		f2'k f2  runs from q1p1 to p3, q1 = p2 + nuobs*ncts+ncts
c
      p1 = nuobs
      p1p1 = p1 + 1
      p2 = p1 + nuobs*ncts
      p2p1 = p2 + 1
      q1 = p2 + nuobs*ncts+ncts
      q1p1 = q1 + 1
      p3 = p2 + nuobs**2
      p3p1 = p3 + 1
      p4 = p3 + ncts
      p4p1 = p4 + 1
      lwa2 = lwa - (nuobs*(1+ncts+nuobs) + ncts)
c			set up structures needed for dsgdc1 and dgcv1
      call dsetup(des,lddes,s,lds,dim,m,ncov,nuobs,work,work(p1p1),
     * nuobs,ncts,work(p3p1),work(p2p1),nuobs,work(p4p1),iwork(ip2p1),
     * info)
      if (info .ne. 0) then
         info = info + 10
         return
      endif

c			decompose f2' k f2
      npsing = nuobs - ncts
      call dsgdc1(work(q1p1),nuobs,npsing,svals,iwork(ip2p1),
     * work(p4p1),lwa2,info)
      iout(1) = npsing
      if (info .gt. 0) then
         info = info + 100
         return
      endif
c			setup y
      if (jrep .ne. 0) then
         call dsuy(y,nobs,nuobs,adiag,work,iwork,iwork(ip1p1),
     *    ssqrep,jadiag)
      endif
      dout(5) = ssqrep
c			compute lambda hat and other parameters
      call dgcv1(work(p2p1),nuobs,y,nuobs,nobs,work(p1p1),nuobs,ncts,
     * work(p3p1),svals,adiag,lamlim,ssqrep,ntbl,dout,coef,tbl,ldtbl,
     * auxtbl,work(p4p1),lwa2,jobgcv,info)
      if (info .gt. 0) then
         info = info + 200
         return
      endif
c			if there are replicates then rescale the coef.
c			vector,	the predicted values, and diagonal of A
      if (nuobs .ne. nobs) then
         do 10 i = 1,nuobs
            coef(ncts+i) = coef(ncts+i) * work(i)
   10    continue
         j = nuobs
         do 20 i = nobs,1,-1
            y(i)=y(j)/work(j)
            if (jadiag .ne. 0) then
               adiag(i) = adiag(j)/work(j)**2
            endif
            if (iwork(ip1 + i) .eq. 0) then
               j = j - 1
            endif
   20    continue
      endif
c			undo the permutation of the predicted values and
c			adiag if necessary
      if (jrep .ne. 0) then
         call dprmut (y,nobs,iwork,1)
         if (jadiag .ne. 0) call dprmut (adiag,nobs,iwork,1)
      endif
      return
      end


c -----------------------------------------------------------
      double precision function dasum(n,dx,incx)
c
c     takes the sum of the absolute values.
c     jack dongarra, linpack, 3/11/78.
c     modified 3/93 to return if incx .le. 0.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision dx(*),dtemp
      integer i,incx,m,mp1,n,nincx
c
      dasum = 0.0d0
      dtemp = 0.0d0
      if( n.le.0 .or. incx.le.0 )return
      if(incx.eq.1)go to 20
c
c        code for increment not equal to 1
c
      nincx = n*incx
      do 10 i = 1,nincx,incx
        dtemp = dtemp + dabs(dx(i))
   10 continue
      dasum = dtemp
      return
c
c        code for increment equal to 1
c
c
c        clean-up loop
c
   20 m = mod(n,6)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dtemp = dtemp + dabs(dx(i))
   30 continue
      if( n .lt. 6 ) go to 60
   40 mp1 = m + 1
      do 50 i = mp1,n,6
        dtemp = dtemp + dabs(dx(i)) + dabs(dx(i + 1)) + dabs(dx(i + 2))
     *  + dabs(dx(i + 3)) + dabs(dx(i + 4)) + dabs(dx(i + 5))
   50 continue
   60 dasum = dtemp
      return
      end


c -----------------------------------------------------------
      subroutine dpred(pdes,ldpdes,npred,dim,m,desb,lddesb,ndesb,ps,
     * ldps,ncov1,ncov2,coef,npar,pred,work,lwa,iwork,info)
      integer ldpdes,npred,dim,m,lddesb,ndesb,ldps,ncov1,ncov2,npar,lwa,
     * iwork(dim),info
      double precision pdes(ldpdes,dim),desb(lddesb,dim),ps(ldps,*),
     * coef(npar),pred(npred),work(lwa)
c
c   Purpose: determine predicted values at the locations in pdes and ps
c
c  On Entry:
c   pdes(ldpdes,dim) 	prediction design for splined variables
c   ldpdes		leading dimension of pdes as declared in the
c			calling	program
c   npred		number of rows in pdes
c   desb(lddesb,dim) 	locations for the basis functions
c			(returned from dtpss and dptpss in the
c			variable des)
c   lddesb		leading dimension of desb as declared in the
c			calling	program
c   ndesb		number of rows in desb
c   dim			number of columns in desb
c   m			order of the derivatives in the penalty
c   ps(ldps,ncov1+ncov2) prediction covariates corresponding to pdes
c   ldps		leading dimension of ps as declared in the
c			calling	program
c   ncov1		number of covariates which duplicate the
c			replication structure of pdes
c   ncov2		number of covariates which do not duplicate the
c			replication structure of pdes
c   coef(npar)		coefficient estimates  [delta':xi']'
c   npar		ndesb + (m+dim-1 choose dim) + ncov1 + ncov2
c
c On Exit:
c   pred(npred)		predicted values
c   info		error indicator
c			  0 : successful completion
c			  1 : dimension error
c			  2 : error in npar,ncov1,ncov2,m or dim
c			  3 : lwa too small
c			  4 : error in dmaket
c
c
c Working Storage:
c   work(lwa)		double precision work vector
c   lwa			length of work vector
c			must be at least npred*(nct+ndesb)
c			where nct = (m+dim-1 choose dim)
c   iwork(dim)		integer work vector
c
c Subprograms Called Directly:
c    Gcvpack - dmaket dmakek
c    Blas    - dgemv
c
c Subprograms Called Indirectly:
c    Blas    - dcopy
c    Other   - fact mkpoly
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      double precision dummy
      integer nct,p1,p1p1,npoly
      integer mkpoly
c
      nct = mkpoly(m,dim)
      if ((ndesb .le. 0) .or. (nct .le. 0) .or. (m .le. 0) .or.
     * (dim .le. 0) .or. 2*m - dim .le. 0) then
	 info = 1
	 return
      endif
      if (npar .ne. ndesb + nct + ncov1 + ncov2) then
         info = 2
         return
      endif
      if (lwa .lt. npred*(nct+ndesb)) then
	 info = 3
	 return
      endif
c			first npred*nct positions of work contain t
      p1 = npred*nct
c			next npred*ndesb positions of work contain k
      p1p1 = p1 + 1
c
      call dmaket(m,npred,dim,pdes,ldpdes,dummy,1,0,npoly,work(1),npred,
     * iwork,info)
      if (info .ne. 0) then
         info = 4
         return
      endif
      call dmakek(m,npred,dim,pdes,ldpdes,ndesb,desb,lddesb,work(p1p1),
     * npred)
c			compute predicted values
      call dgemv('N',npred,nct,1.0d0,work,npred,coef,1,0.0d0,
     * pred,1)
      call dgemv('N',npred,ncov1+ncov2,1.0d0,ps,ldps,coef(nct+1),1,
     * 1.0d0,pred,1)
      call dgemv('N',npred,ndesb,1.0d0,work(p1+1),npred,
     * coef(nct+ncov1+ncov2+1),1,1.0d0,pred,1)
      return
      end


c -----------------------------------------------------------
      subroutine dreps(des,lddes,nobs,dim,s,lds,ncov1,ncov2,c1,order,
     * nuobs,xrep,job,info)
      integer lddes,nobs,dim,lds,ncov1,ncov2,order(nobs),nuobs,
     * xrep(nobs),info,job
      double precision des(lddes,dim),s(lds,*),c1(*)
c
c Purpose: sort des and s, compute degrees of freedom for replication
c	and c1.
c
c On Entry:
c   des(lddes,dim) 	design for the variables to be splined
c			should be entered in lexicographical order
c			(smallest to largest) if possible for efficient
c			computing
c   lddes		leading dimension of des as declared in the
c   			calling program
c   nobs		number of observations
c   dim			number of columns in des
c   s(lds,ncov1+ncov2) 	design for the covariates
c   lds			leading dimension of s as declared in the
c   			calling program
c   ncov1		number of covariates which duplicate the
c			replication structure of des
c   ncov2		number of covariates which do not duplicate
c			replication structure of des
c   job			if job is nonzero then c1 is computed
c   			if job = 0 then c1 is not referenced
c
c On Exit:
c   des(lddes,dim) 	des sorted lexicographically
c   s(lds,ncov1+ncov2) 	s, sorted to correspond to des
c   c1(nuobs)		if job is nonzero then c1(i) the square root of
c			the number of replicates of the ith sorted
c			design point
c   order(nobs)		order of the sorted des
c   nuobs		number of unique rows in des
c   xrep(nobs)		xrep(i) = 1 if the ith sorted design point is a
c			replicate, 0 if not
c   info		error indicator
c			   0 : successful completion
c			   1 : ncov1 is incorrect
c
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer sw,oldsw,itemp,i,j,k,cont,dfrep
      double precision temp,diff,one,machpr,denom,wmin,wmax
c
      info = 0
      one = 1.0d0
      machpr = 1.0d0
   10 machpr = machpr/2.0d0
      if (one .lt. 1.0d0 + machpr) goto 10
      machpr = machpr*2.0d0
c
      do 20 i = 1,nobs
          order(i) = i
 	  xrep(i) = 0
   20 continue
      if (job .ne. 0) call dset(nobs,0.0d0,c1,1)
c			sort des and s
      sw = nobs - 1
   30 if (sw .le. 0) goto 90
          oldsw = sw
          sw = 0
          do 80 i = 1,oldsw
	      cont = 1
	      k = 1
   40         if (cont .eq. 0) goto 70
                  if (k .le. dim) then
	              diff = des(i,k) - des(i+1,k)
                  else
	              diff = s(i,k-dim) - s(i+1,k-dim)
                  endif
		  if (diff .lt. 0.0d0) then
		      if (k .gt. dim) info = 1
		      cont = 0
		  else if (diff .gt. 0.0d0) then
		      if (k .gt. dim) info = 1
c		      switch the order of i and i+1
		      itemp = order(i)
		      order(i)=order(i+1)
		      order(i+1) = itemp
		      itemp = xrep(i)
		      xrep(i)= xrep(i+1)
		      xrep(i+1)= itemp
		      do 50 j = 1,dim
			  temp = des(i,j)
			  des(i,j) = des(i+1,j)
			  des(i+1,j) = temp
   50 		      continue
  		      do 60 j = 1,ncov1+ncov2
			  temp = s(i,j)
			  s(i,j) = s(i+1,j)
			  s(i+1,j) = temp
   60 		      continue
		      sw = i
		      cont = 0
                  else if (k .eq. dim + ncov1) then
		      xrep(i + 1) = 1
		      cont = 0
                  else
	    	      k = k + 1
                  endif
              goto 40
   70         continue
   80     continue
      goto 30
   90 continue
c			compute range of design
      denom=0.0d0
      do 120 j=1,dim
          wmin = des(1,j)
	  wmax = des(1,j)
          do 110 i=1,nobs
	      if (des(i,j) .lt. wmin) wmin = des(i,j)
	      if (des(i,j) .gt. wmax) wmax = des(i,j)
  110     continue
	  denom = denom + (wmax-wmin)**2
  120 continue

c			check for design points too close together
      do 140 i=1,nobs-1
	  if (xrep(i+1) .eq. 0) then
	     diff = 0.0d0
	     do 130 j=1,dim
	        diff = diff + (des(i,j)-des(i+1,j))**2
  130	     continue
	     if (abs(diff)/denom .lt. 100*machpr) xrep(i+1)=1
	  endif
  140 continue
c			compute dfrep and c1
      dfrep = 0
      j = 0
       do 150 i = 1,nobs
	   j = j + 1 - xrep(i)
	   if (job .ne. 0) c1(j) = xrep(i)*c1(j) + 1.0d0
	   dfrep = dfrep + xrep(i)
  150 continue
      nuobs = nobs - dfrep
      if (job .eq. 0 ) return
      do 160 i = 1,nuobs
	  c1(i) = sqrt(c1(i))
  160 continue
      return
      end


c -----------------------------------------------------------
      subroutine dsetup(des,lddes,su1,ldsu1,dim,m,ncov1,nuobs,c1,
     * tusu1,ldtu,ncts1,fgaux,ku,ldku,work,iwork,info)
      integer lddes,ldsu1,dim,m,ncov1,nuobs,ldtu,ncts1,ldku,
     * iwork(dim),info
      double precision des(lddes,dim),su1(ldsu1,*),c1(nuobs),
     * tusu1(ldtu,ncts1),fgaux(ncts1),ku(ldku,nuobs),work(ncts1)
c
c Purpose: set up [tu:su1] as f g and ku as f'c1 ku c1'f.
c
c On Entry:
c   des(lddes,dim)  	variables to be splined (unique rows)
c   lddes		leading dimension of des as declared in the
c			calling	program
c   su1(ldsu1,ncov1)	covariates (unique rows)
c   ldsu1		leading dimension of su1 as declared in the
c			calling	program
c   dim 		dimension of the variables to be splined
c   m			order of the derivatives in the penalty
c   ncov1		number of covariates
c   nuobs		number of unique rows in des
c   c1(nuobs)		c1(i) contains the square root of the number of
c			replicates of the ith sorted design point
c   ldtu		leading dimension of tusu1 as declared in the
c			calling	program
c   ldku		leading dimension of ku as declared in the
c			calling program
c
c On Exit:
c   tusu1(ldtu,ncts1)	the qr decomposition of [tu:su1]
c   ncts1		number of columns in [tu:su1] = npoly + ncov1
c   fgaux(ncts1)	the auxiliary info on the qr decomposition of
c			[tu:su1]
c   ku(p,p)  		f'ku f
c   info		error indicator
c			   0 : successful completion
c			   1 : error in dmaket
c
c Work Arrays:
c   work(ncts1)		double precision work vector
c   iwork(dim)		integer work vector
c
c Subroutines called directly
c	Gcvpack - dmaket dmakek
c	Linpack - dqrdc
c	Other   - dftkf mkpoly
c
c Subroutines called indirectly
c	Blas    - dcopy
c	Gcvpack - dqrsl
c	Other   - fact mkpoly
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer npoly,i,j
      integer mkpoly
c
      info = 0
      npoly=mkpoly(m,dim)
      ncts1=npoly+ncov1
c			make [tu:su1] and ku
      call dmaket(m,nuobs,dim,des,lddes,su1,ldsu1,ncov1,npoly,tusu1,
     * ldtu,iwork,info)
      if (info .ne. 0) then
	 return
      endif
      call dmakek(m,nuobs,dim,des,lddes,nuobs,des,lddes,ku,ldku)
      if (c1(1) .ne. 0) then
         do 30 i = 1,nuobs
            do 10 j = 1,npoly+ncov1
               tusu1(i,j) = tusu1(i,j) * c1(i)
   10       continue
            do 20 j = 1,nuobs
               ku(i,j) = ku(i,j) * c1(i) * c1(j)
   20       continue
   30    continue
      endif
c			decompose [tu:su1] into fg
      call dqrdc(tusu1,ldtu,nuobs,ncts1,fgaux,0,0.0d0,0)
c      			calculate f'ku f
      call dftkf(tusu1,ldtu,nuobs,ncts1,fgaux,ku,ldku,work)
      return
      end






c -----------------------------------------------------------
      integer function mkpoly(m,dim)
      integer m,dim
c
c  Purpose: compute the binomial coefficient of m + dim - 1 choose dim.
c  	This is the dimension of the space of polynomials which are in
c  	the null space of the smoothing penalty. Uses Andy Jaworski's
c	binomial coefficient algorithm that only requires integer
c	arithmetic.
c
c  On Entry:
c   m			order of derivatives in the penalty
c   dim	 		dimension of the variables to be splined
c
c  On Exit:
c   mkploy		(m + dim - 1) choose dim
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,k,k1,kcoef,n
c 			compute binomial coefficient
c			m + dim - 1 choose dim
      n = m + dim - 1
      k1 = dim
      if (k1 .gt. n .or. k1 .lt. 0) then
         mkpoly = 0
         return
      endif
      k = k1
      if ((n - k1) .lt. k) then
         k = n - k1
      endif
      kcoef = 1
      j = n - k
      do 10 i = 1, k
         j = j + 1
         kcoef = (kcoef * j) / i
   10 continue
      mkpoly = kcoef
      return
      end


c -----------------------------------------------------------
      subroutine dsuy(y,nobs,nuobs,ytrue,c1,order,xrep,ssqrep,
     * job)
      integer nobs,nuobs,order(nobs),xrep(nobs),job
      double precision y(nobs),ytrue(nobs),c1(nuobs),ssqrep
c
c Purpose: compute B1'y, B1'ytrue and ssq for replication.
c
c On Entry:
c   y(nobs)  		response vector
c   nobs		number of observations
c   nuobs		number of unique design points
c   ytrue(nobs)		"true" response, if job is nonzero 1
c			not referenced if job = 0
c   c1(nuobs)		c1(i) contains the square root of the number of
c			replicates of the ith sorted design point
c   order(nobs)		order of sorted des
c   xrep(nobs)		xrep(i) = 1 if the ith sorted design point is a
c			replicate, 0 if not
c   job 		job is nonzero if B1'ytrue should be calculated
c			job = 0 otherwise
c
c On Exit:
c   y(nuobs)  		B1'y
c   ytrue(nuobs)	B1'ytrue if job is nonzero
c   ssqrep		sum of squares for replication
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer first,i,j
      double precision accum
c
      accum = 0.0d0
      ssqrep = 0.0d0
      call dprmut (y,nobs,order,0)
      if (job .ne. 0) call dprmut (ytrue,nobs,order,0)
c			compute ssq for replication
      first = 0
      do 20 i = 1,nobs
	  if (xrep(i) .eq. 1) then
	      accum = accum + y(i)
	  else if (first .eq. i - 1) then
	      first = i
	      accum = y(i)
	  else
	      accum = accum/(i-first)
	      do 10 j = first,i-1
	          ssqrep = (y(j)-accum)**2 + ssqrep
   10         continue
	      first = i
	      accum = y(i)
          endif
   20 continue
      if (xrep(nobs) .eq. 1) then
          accum = accum/(nobs + 1 - first)
          do 30 j = first,nobs
              ssqrep = (y(j)-accum)**2 + ssqrep
   30     continue
      endif
c			compute B1'y and B1'ytrue
      j = 0
      do 40 i = 1,nobs
 	  if (xrep(i) .eq. 0) then
 	      if (j .ne. 0) then
 		  y(j) = y(j) / c1(j)
 		  if (job .ne. 0) ytrue(j) = ytrue(j) / c1(j)
              endif
 	      j = j + 1
	      y(j) = y(i)
	      if (job .ne. 0) ytrue(j) = ytrue(i)
	  else
	      y(j) = y(j) + y(i)
	      if (job .ne. 0) ytrue(j) = ytrue(j) + ytrue(i)
 	  endif
   40 continue
      y(j) = y(j) / c1(j)
      if (job .ne. 0) ytrue(j) = ytrue(j) / c1(j)
      return
      end


c -----------------------------------------------------------
      subroutine dmakek(m,n,dim,des,lddes,nb,desb,lddesb,kk,ldkk)
      integer m,n,dim,lddes,nb,lddesb,ldkk
      double precision des(lddes,dim),desb(lddesb,dim),kk(ldkk,nb)
c
c Purpose: create the k matrix.
c
c On Entry:
c   m			order of the derivatives in the penalty
c   n			number of rows in des
c   dim			dimension of the space to be splined
c   des(lddes,dim)	variables to be splined
c   lddes		leading dimension of des as declared in the
c			calling	program
c   nb			number of rows in desb
c   desb(lddesb,dim)	positions of unique design points or basis
c			functions
c   lddesb		leading dimension of desb as declared in the
c			calling	program
c   ldkk		leading dimension of kk as declared in the
c			calling	program
c On Exit:
c   kk(ldkk,nb)		k matrix
c
c Subprograms Called:
c	Other   - fact
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,k,fact
      double precision tauij,expo,theta,t,pi
c
c			t to be used in computation of theta
      pi = 4.0d0*atan(1.0d0)
      t = 2 ** (2*m) * pi**(dim/2.0d0) * fact(m-1)
c
c 			exponent for tauij
      expo = m - (dim / 2.0d0)
      if (dim .eq. 2*(dim/2)) then
c			1+dim odd
         theta = 1.0 / (0.5 * t * fact (m-dim/2))
         if ((2*m+dim) .eq. 4*((2*m+dim)/4)) theta = -theta
         do 30 i=1,n
            do 20 j=1,nb
               tauij = 0
               do 10 k=1,dim
                  tauij = tauij + (des(i,k)-desb(j,k))**2
   10          continue
               if (tauij .eq. 0.0d0) then
                  kk(i,j) = 0.0d0
               else
                  kk(i,j) = theta*tauij**expo * 0.5 * log(tauij)
               endif
   20       continue
   30    continue
      else
c			1+dim even
c			compute theta
c			compute gamma(dim/2 - m)
         j = (1 - (dim-2*m)) / 2
         theta = sqrt(pi)
         do 40 i=1,j
	      theta = -theta / (i - 0.5d0)
   40    continue
	 theta = theta / t

         do 70 i=1,n
            do 60 j=1,nb
               tauij = 0
               do 50 k=1,dim
                  tauij = tauij + (des(i,k)-desb(j,k))**2
   50          continue
               if (tauij .eq. 0.0d0) then
                  kk(i,j) = 0.0d0
               else
                  kk(i,j) = theta*tauij**expo
               endif
   60       continue
   70    continue
      endif
      end


c -----------------------------------------------------------
      subroutine dsgdc1(f2kf2,ldfkf,p,svals,iwork,work,lwa,info)
      integer ldfkf,p,iwork(p),lwa,info
      double precision f2kf2(ldfkf,p),svals(*),work(lwa)
c
c Purpose: form the singular value decomposition of the Cholesky factor
c 	of f2'k f2.
c
c On Entry:
c   f2kf2(ldfkf,p)	f2'k f2
c   ldfkf		leading dimension of f2'k f2 as declared in the
c			calling	program
c   p			number of rows and columns in f2'k f2
c
c On Exit:
c   f2kf2(p,p)  	overwritten with singular value decomposition
c 			of Cholesky factor of f2'k f2
c   svals(p) 		the singular values of the Cholesky factor of
c			f2'k f2 if info = 0.
c			if info = 3 then svals is as it was returned
c			from dsvdc.
c   info 	   	error indicator
c			  0 : successful completion
c			  1 : lwa too small
c			  2 : f2'k f2 is not of full rank
c			  3 : error in dsvdc
c   p			if info = 3 p contains info as it was returned
c			from dsvdc (otherwise unchanged)
c
c Work Arrays:
c   work(lwa)		double precision work vector
c   lwa			length of work as declared in the calling
c			program
c			must be at least 2*p
c   iwork(p)		integer work vector
c
c Subprograms Called:
c	Linpack - dchdc dsvdc
c	Blas    - dcopy
c	Other   - dset dprmut
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,pp1,locinf,k
      double precision dummy,one,machpr
c
      info = 0
      if (lwa .lt. 2*p) then
	  info = 1
	  return
      endif
      pp1 = p + 1
      call dset(p,0.0d0,svals,1)
c
      one = 1.0d0
      machpr = 1.0d0
   10 machpr = machpr/2.0d0
      if (one .lt. 1.0d0 + machpr) goto 10
      machpr = machpr*2.0d0
c			Cholesky decomposition of f2'k f2
      do 20 j = 1,p
         iwork(j) = 0
   20 continue
      call dchdc (f2kf2,ldfkf,p,work,iwork,1,locinf)
      do 30 i=1,locinf
         if ((f2kf2(i,i)/f2kf2(1,1))**2 .gt. machpr) k = i
   30 continue
      if (k .lt. p) then
	 info = 2
	 return
      endif
c    		copy f2kf2' into f2kf2
c    		svd of f2 k f2' = udv' return only u
      do 40 j = 1,p
         call dcopy(1+p-j,f2kf2(j,j),ldfkf,f2kf2(j,j),1)
         call dset(j-1,0.0d0,f2kf2(1,j),1)
   40 continue
      call dsvdc(f2kf2,ldfkf,p,p,svals,work,f2kf2,ldfkf,dummy,1,
     * work(pp1),20,info)
      if (info .ne. 0) then
	  p = info
	  info = 3
	  return
      endif
      do 50 j=1,p
         call dprmut(f2kf2(1,j),p,iwork,1)
   50 continue
      return
      end


c -----------------------------------------------------------
      subroutine duni(x,ldx,nobs,ncx,xrep,xu,ldxu)
      integer ldx,nobs,ncx,ldxu,xrep(nobs)
      double precision x(ldx,*),xu(ldxu,*)
c
c Purpose: compute xu.
c
c On Entry:
c   x(ldx,ncx)		a matrix to be reduced to unique rows
c   ldx			leading dimension of x as declared in the
c			calling	program
c   nobs		number of observations
c   ncx			number of columns in x
c   xrep(nobs)		xrep(i) contains 1 if ith row of x is a
c			replicate row, 0 if not
c   ldxu		leading dimension of xu as declared in the
c			calling	program
c On Exit:
c   xu(ldxu,ncx) 	unique rows of x
c			may be identified with x in the calling sequence
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,k
c
      j = 0
      do  20 i = 1,nobs
 	  if (xrep(i) .eq. 0) then
 	      j = j + 1
 	      do 10 k = 1,ncx
 	          xu(j,k) = x(i,k)
   10         continue
 	  endif
   20 continue
      return
      end


c -----------------------------------------------------------
      subroutine dmaket(m,n,dim,des,lddes,s1,lds1,ncov1,npoly,t,ldt,
     * wptr,info)
      integer m,n,dim,lddes,lds1,ncov1,npoly,ldt,wptr(dim),info
      double precision des(lddes,dim),s1(lds1,*),t(ldt,*)
c
c Purpose: create t matrix and append s1 to it.
c
c On Entry:
c   m			order of the derivatives in the penalty
c   n			number of rows in des
c   dim			number of columns in des
c   des(lddes,dim)	variables to be splined
c   lddes		leading dimension of des as declared in the
c			calling program
c   s1(lds1,ncov1)	covariates which duplicate the replication
c			structure of des
c   lds1		leading dimension of s1 as declared in the
c			calling program
c   ncov1		number of columns in s1
c   ldt			leading dimension of t as declared in the
c			calling program
c
c On Exit:
c   npoly		dimension of polynomial part of spline
c   t(ldt,npoly+ncov1)	[t:s1]
c   info 		error indication
c   			   0 : successful completion
c		 	   1 : error in creation of t
c Work Arrays:
c   wptr(dim)		integer work vector
c
c Subprograms Called Directly:
c	Blas  - dcopy
c	Other - mkpoly
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,k,tt,nt,bptr,eptr
      integer mkpoly
c
      info = 0
      npoly = mkpoly(m,dim)
      call dset(n,1.0d0,t(1,1),1)
      nt = 1
      if (npoly .gt. 1) then
          do 10 j=1,dim
             nt = j + 1
             wptr(j) = nt
             call dcopy(n,des(1,j),1,t(1,nt),1)
   10     continue
c
c     get cross products of x's in null space for m>2
c
c     WARNING: do NOT change next do loop unless you fully understand:
c              This first gets x1*x1, x1*x2, x1*x3, then
c              x2*x2, x2*x3, and finally x3*x3 for dim=3,n=3
c              wptr(1) is always at the beginning of the current
c	       level of cross products, hence the end of the
c	       previous level which is used for the next.
c	       wptr(j) is at the start of xj * (previous level)
c
          do 50 k=2,m-1
             do 40 j=1,dim
                bptr = wptr(j)
                wptr(j) = nt + 1
                eptr = wptr(1) - 1
                do 30 tt=bptr,eptr
                   nt = nt + 1
                   do 20 i=1,n
                      t(i,nt) = des(i,j) * t(i,tt)
   20              continue
   30           continue
   40        continue
   50     continue
          if (nt .ne. npoly) then
	      info = 1
	      return
          endif
      endif
c			append s1 to t
      do 60 i = 1,ncov1
         call dcopy(n,s1(1,i),1,t(1,nt+i),1)
   60 continue
      end


c -----------------------------------------------------------
      subroutine dprmut (x,npar,jpvt,job)
      integer npar,jpvt(npar),job
      double precision x(npar)
c
c Purpose: permute the elements of the array x according to the index
c	vector jpvt (either forward or backward permutation).
c
c On Entry:
c   x(npar)		array to be permuted
c   npar		size of x (and jpvt)
c   jpvt		indices of the permutation
c   job			indicator of forward or backward permutation
c			if job = 0 forward permutation
c				x(jpvt(i)) moved to x(i)
c			if job is nonzero backward permutation
c				x(i) moved to x(jpvt(i))
c On Exit:
c   x(npar)		array with permuted entries
c
c   Written:	Yin Ling	U. of Maryland, August,1978
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,k
      double precision t
c
      if (npar .le. 1) then
         return
      endif
      do 10 j = 1,npar
         jpvt(j) = -jpvt(j)
   10 continue
      if (job .eq. 0) then
c		forward permutation
         do 30 i = 1,npar
            if (jpvt(i) .gt. 0) then
               goto 30
            endif
            j = i
            jpvt(j) = -jpvt(j)
            k = jpvt(j)
c           while
   20       if (jpvt(k) .lt. 0) then
               t = x(j)
               x(j) = x(k)
               x(k) = t
               jpvt(k) = -jpvt(k)
               j = k
               k = jpvt(k)
               goto 20
c           endwhile
            endif
   30    continue
      endif
      if (job .ne. 0 ) then
c			backward permutation
         do 50 i = 1,npar
            if (jpvt(i) .gt. 0) then
               goto 50
            endif
            jpvt(i) = -jpvt(i)
            j = jpvt(i)
c           while
   40       if (j .ne. i) then
               t = x(i)
               x(i) = x(j)
               x(j) = t
               jpvt(j) = -jpvt(j)
               j = jpvt(j)
               goto 40
c           endwhile
            endif
   50    continue
      endif
      return
      end


Caveat receptor.  (Jack) dongarra@anl-mcs, (Eric Grosse) research!ehg
Compliments of netlib   Sun Jul  6 09:34:18 CDT 1986
C
C***********************************************************************
C
C     File of the  DOUBLE PRECISION  Level 2 BLAS routines:
C
C      DGEMV, DGBMV, DSYMV, DSBMV, DSPMV, DTRMV, DTBMV, DTPMV,
C      DGER , DSYR , DSPR ,
C      DSYR2, DSPR2,
C      DTRSV, DTBSV, DTPSV.
C
C     See:
C
C        Dongarra J. J., Du Croz J. J., Hammarling S. and Hanson R. J..
C        A proposal for an extended set of Fortran Basic Linear Algebra
C        Subprograms. Technical Memorandum No.41 (revision 1),
C        Mathematics and Computer Science Division, Argone National
C        Laboratory, 9700 South Cass Avenue, Argonne, Illinois 60439,
C        USA or NAG Technical Report TR4/85, Nuemrical Algorithms Group
C        Inc., 1101 31st Street, Suite 100, Downers Grove, Illinois
C        60515-1263, USA.
C
C***********************************************************************
C
      SUBROUTINE DGEMV ( TRANS, M, N, ALPHA, A, LDA, X, INCX,
     $                   BETA, Y, INCY )
      CHARACTER*1        TRANS
      INTEGER            M, N, LDA, INCX, INCY
      DOUBLE PRECISION   ALPHA, A( LDA, * ), X( * ), BETA, Y( * )
*
*  Purpose
*  =======
*
*  DGEMV  performs one of the matrix-vector operations
*
*     y := alpha*A*x + beta*y,   or   y := alpha*A'*x + beta*y,
*
*  where alpha and beta are scalars, x and y are vectors and A is an
*  m by n matrix.
*
*  Parameters
*  ==========
*
*  TRANS  - CHARACTER*1.
*           On entry, TRANS specifies the operation to be performed as
*           follows:
*
*              TRANS = 'N' or 'n'   y := alpha*A*x + beta*y.
*
*              TRANS = 'T' or 't'   y := alpha*A'*x + beta*y.
*
*              TRANS = 'C' or 'c'   y := alpha*A'*x + beta*y
*.
*           Unchanged on exit.
*
*  M      - INTEGER.
*           On entry, M specifies the number of rows of the matrix A.
*           M must be at least zero.
*           Unchanged on exit.
*
*  N      - INTEGER.
*           On entry, N specifies the number of columns of the matrix A.
*           N must be at least zero.
*           Unchanged on exit.
*
*  ALPHA  - REAL            .
*           On entry, ALPHA specifies the scalar alpha.
*           Unchanged on exit.
*
*  A      - DOUBLE PRECISION array of DIMENSION ( LDA, n ).
*           Before entry, the leading m by n part of the array A must
*           contain the matrix of coefficients.
*           Unchanged on exit.
*
*  LDA    - INTEGER.
*           On entry, LDA specifies the leading dimension of A as
*           declared in the calling (sub) program. LDA must be at least
*           m.
*           Unchanged on exit.
*
*  X      - DOUBLE PRECISION array of DIMENSION at least
*           ( 1 + ( n - 1 )*abs( INCX ) ) when TRANS = 'N' or 'n'
*           and at least
*           ( 1 + ( m - 1 )*abs( INCX ) ) otherwise.
*           Before entry, the incremented array X must contain the
*           vector x.
*           Unchanged on exit.
*
*  INCX   - INTEGER.
*           On entry, INCX specifies the increment for the elements of
*           X.
*           Unchanged on exit.
*
*  BETA   - REAL            .
*           On entry, BETA specifies the scalar beta. When BETA is
*           supplied as zero then Y need not be set on input.
*           Unchanged on exit.
*
*  Y      - DOUBLE PRECISION array of DIMENSION at least
*           ( 1 + ( m - 1 )*abs( INCY ) ) when TRANS = 'N' or 'n'
*           and at least
*           ( 1 + ( n - 1 )*abs( INCY ) ) otherwise.
*           Before entry with BETA non-zero, the incremented array Y
*           must contain the vector y. On exit, Y is overwritten by the
*           updated vector y.
*
*  INCY   - INTEGER.
*           On entry, INCY specifies the increment for the elements of
*           Y.
*           Unchanged on exit.
*
*
*  Note that TRANS, M, N and LDA must be such that the value of the
*  LOGICAL variable OK in the following statement is true.
*
*     OK = ( ( TRANS.EQ.'N' ).OR.( TRANS.EQ.'n' ).OR.
*    $       ( TRANS.EQ.'T' ).OR.( TRANS.EQ.'t' ).OR.
*    $       ( TRANS.EQ.'C' ).OR.( TRANS.EQ.'c' )     )
*    $     .AND.
*    $     ( M.GE.0 )
*    $     .AND.
*    $     ( N.GE.0 )
*    $     .AND.
*    $     ( LDA.GE.M )
*
*
*
*  Level 2 Blas routine.
*
*  -- Written on 30-August-1985.
*     Sven Hammarling, Nag Central Office.
*
      INTEGER            I     , IX    , IY    , J     , JX    , JY
      INTEGER            KX    , KY    , LENX  , LENY
      DOUBLE PRECISION   ONE   ,         ZERO
      PARAMETER        ( ONE   = 1.0D+0, ZERO  = 0.0D+0 )
      DOUBLE PRECISION   TEMP
*
*     Quick return if possible.
*
      IF( ( M.EQ.0 ).OR.
     $    ( N.EQ.0 ).OR.
     $    ( ( ALPHA.EQ.ZERO ).AND.( BETA.EQ.ONE ) ) )
     $   RETURN
*
*     Set LENX and LENY, the lengths of the vectors x and y.
*
      IF( ( TRANS.EQ.'N' ).OR.( TRANS.EQ.'n' ) )THEN
         LENX = N
         LENY = M
      ELSE
         LENX = M
         LENY = N
      END IF
*
*     Start the operations. In this version the elements of A are
*     accessed sequentially with one pass through A.
*
*     First form  y := beta*y  and set up the start points in X and Y if
*     the increments are not both unity.
*
      IF( ( INCX.EQ.1 ).AND.( INCY.EQ.1 ) )THEN
         IF( BETA.NE.ONE )THEN
            IF( BETA.EQ.ZERO )THEN
               DO 10, I = 1, LENY
                  Y( I ) = ZERO
   10          CONTINUE
            ELSE
               DO 20, I = 1, LENY
                  Y( I ) = BETA*Y( I )
   20          CONTINUE
            END IF
         END IF
      ELSE
         IF( INCX.GT.0 )THEN
            KX = 1
         ELSE
            KX = 1 - ( LENX - 1 )*INCX
         END IF
         IF( INCY.GT.0 )THEN
            KY = 1
         ELSE
            KY = 1 - ( LENY - 1 )*INCY
         END IF
         IF( BETA.NE.ONE )THEN
            IY = KY
            IF( BETA.EQ.ZERO )THEN
               DO 30, I = 1, LENY
                  Y( IY ) = ZERO
                  IY      = IY + INCY
   30          CONTINUE
            ELSE
               DO 40, I = 1, LENY
                  Y( IY ) = BETA*Y( IY )
                  IY      = IY + INCY
   40          CONTINUE
            END IF
         END IF
      END IF
      IF( ALPHA.EQ.ZERO )
     $   RETURN
      IF( ( TRANS.EQ.'N' ).OR.( TRANS.EQ.'n' ) )THEN
*
*        Form  y := alpha*A*x + y.
*
         IF( ( INCX.EQ.1 ).AND.( INCY.EQ.1 ) )THEN
            DO 60, J = 1, N
               IF( X( J ).NE.ZERO )THEN
                  TEMP = ALPHA*X( J )
                  DO 50, I = 1, M
                     Y( I ) = Y( I ) + TEMP*A( I, J )
   50             CONTINUE
               END IF
   60       CONTINUE
         ELSE
            JX = KX
            DO 80, J = 1, N
               IF( X( JX ).NE.ZERO )THEN
                  TEMP = ALPHA*X( JX )
                  IY   = KY
                  DO 70, I = 1, M
                     Y( IY ) = Y( IY ) + TEMP*A( I, J )
                     IY      = IY      + INCY
   70             CONTINUE
               END IF
               JX = JX + INCX
   80       CONTINUE
         END IF
      ELSE
*
*        Form  y := alpha*A'*x + y.
*
         IF( ( INCX.EQ.1 ).AND.( INCY.EQ.1 ) )THEN
            DO 100, J = 1, N
               TEMP = ZERO
               DO 90, I = 1, M
                  TEMP  = TEMP + A( I, J )*X( I )
   90          CONTINUE
               Y( J ) = Y( J ) + ALPHA*TEMP
  100       CONTINUE
         ELSE
            JY = KY
            DO 120, J = 1, N
               TEMP = ZERO
               IX   = KX
               DO 110, I = 1, M
                  TEMP  = TEMP + A( I, J )*X( IX )
                  IX    = IX   + INCX
  110          CONTINUE
               Y( JY ) = Y( JY ) + ALPHA*TEMP
               JY      = JY      + INCY
  120       CONTINUE
         END IF
      END IF
      RETURN
*
*     End of DGEMV .
*
      END


c -----------------------------------------------------------
      subroutine dgcv1(fkf,ldfkf,y,nuobs,nobs,fg,ldfg,ncts1,fgaux,svals,
     * adiag,lamlim,ssqrep,ntbl,dout,coef,tbl,ldtbl,auxtbl,work,lwa,
     * job,info)
      integer ldfkf,nuobs,nobs,ldfg,ncts1,ntbl,ldtbl,lwa,job,info
      double precision fkf(ldfkf,nuobs),y(nuobs),fg(ldfg,ncts1),
     * fgaux(ncts1),svals(*),adiag(nuobs),lamlim(2),ssqrep,dout(4),
     * coef(*),tbl(ldtbl,3),auxtbl(3,3),work(lwa)
c
c Purpose: determine the generalized cross validation estimate of the
c	smoothing parameter and fit model parameters for a semi-norm
c	thin plate spline model.
c
c On Entry:
c   fkf(ldfkf,nuobs) 	intermediate results as created by dsgdc1
c   ldfkf		leading dimension of fkf as declared in the
c			calling program
c   y(nuobs)		B1'y
c   nuobs		number of rows in fg
c   nobs		number of observations
c   fg(ldfg,ncts1)	qr decomposition of [t:s1]
c   ldfg		leading dimension of fg as
c			declared in the calling program
c   ncts1		number of columns in [t:s1]
c   fgaux(ncts1)	auxiliary information on the decomposition of
c			[t:s1]
c   svals(nuobs-ncts1) 	positive singular values of the Cholesky factor
c			of f2'k f2
c   adiag(nuobs)	B1'(true y) if predictive mse is requested
c   lamlim(2)		limits on lambda hat search (in log10(nobs*
c			lambda) scale) if user input limits are
c			requested. if lamlim(1) = lamlim(2) then lamhat
c			is set to (10**lamlim(1))/nobs
c   ssqrep		sum of squares for replication
c   ntbl		number of evenly spaced values for
c			log10(nobs*lambda) to be used in the initial
c			grid search for lambda hat
c			if ntbl = 0 only a golden ratio search will be
c			done and tbl is not referenced, if ntbl > 0
c			there will be ntbl rows returned in tbl
c   ldtbl		leading dimension of tbl as declared in the
c			calling program
c   job			integer with decimal expansion abc
c			if a is nonzero then predictive mse is computed
c			   using adiag as true y
c			if b is nonzero then user input limits on search
c			   for lambda hat are used
c			if c is nonzero then diagonal of the hat matrix
c			   is calculated
c
c On Exit:
c   y(nuobs)		B1'(predicted values)
c   adiag(nuobs)	diagonal elements of the hat matrix if requested
c   lamlim(2)		limits on lambda hat search
c			(in log10(nobs*lambda) scale)
c   dout(4)		contains:
c  			1  lamhat   generalized cross validation
c				    estimate of the smoothing parameter
c			2  penlty   smoothing penalty
c			3  rss	    residual sum of squares
c			4  tr(I-A)  trace of I - A
c   coef(nuobs+ncts1) 	estimated coefficients
c   tbl(ldtbl,3)	column	contains
c			  1 	grid of log10(nobs*lambda)
c			  2  	V(lambda)
c			  3     R(lambda) if requested
c   auxtbl(3,3)		auxiliary table
c			1st row contains:
c			    log10(nobs*lamhat), V(lamhat) and
c			    R(lamhat) if requested
c			    where lamhat is the gcv estimate of lambda
c			2nd row contains:
c			    0, V(0) and  R(0) if requested
c			3rd row contains:
c			    0, V(infinity) and R(infinity) if requested
c   info		error indicator
c			  0 : successful completion
c			 -1 : log10(nobs*lamhat) <= lamlim(1)
c			      (not fatal)
c			 -2 : log10(nobs*lamhat) >= lamlim(2)
c			      (not fatal)
c			  1 : dimension error
c			  2 : error in ntbl
c			  3 : lwa (length of work) is too small
c			  4 : lamlim(1) > lamlim(2)
c			 10 < info < 20 : 10 + nonzero info returned
c					  from dvlop
c			 20 < info < 30 : 20 + nonzero info returned
c					  from dcfcr1
c
c Working Storage:
c   work(lwa) 		double precision work vector
c   lwa			length of work as declared in the calling
c			program
c			must be at least nuobs-ncts1+nobs
c
c Subprograms Called Directly:
c       Gcvlib - drsap dvlop dpmse dcfcr1 dpdcr ddiag
c
c Subprograms Called Indirectly:
c	Gcvlib  - dvl vmin
c	Linpack - dqrsl dtrsl
c	Blas    - ddot dcopy dgemv
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      double precision addend,nlamht,ssqw2
      integer npsing,i,jpmse,jlaml,jadiag,nmnct,nctp1,sinfo
c
c
      sinfo = 0
      info = 0
      nmnct = nuobs - ncts1
      nctp1=ncts1+1
      jpmse = job/100
      jlaml = mod(job,100)/10
      jadiag = mod(job,10)
c			check dimensions
      if ((nuobs.le.0).or.(ncts1 .le. 0).or.(nmnct .le. 0)) then
         info = 1
         return
      endif
      if ((ntbl .lt. 0) .or. (ntbl .gt. ldtbl)) then
         info = 2
         return
      endif
      if (lwa .lt. nobs+nuobs - nmnct) then
         info = 3
         return
      endif
      if (jlaml .ne. 0 .and. (lamlim(1) .gt. lamlim(2))) then
         info = 4
         return
      endif
c			calculate npsing
      do 30 i=1,nmnct
         if (svals(i)**2 .gt. 0.0d0) npsing = i
   30 continue
c			apply rotations to y
      call drsap(fg,ldfg,nuobs,ncts1,fgaux,fkf(nctp1,nctp1),ldfkf,nmnct,
     * npsing,y,ssqw2,addend,work)
      addend = addend + ssqrep
      ssqw2 = ssqw2 + ssqrep
c			minimize V(lambda)
      call dvlop (y(nctp1),svals,nobs,ncts1,npsing,addend,ssqw2,
     * lamlim,ntbl,nlamht,tbl,ldtbl,auxtbl,dout(3),jlaml,info)
      dout(1)=nlamht/nobs
      if (info .gt. 0) then
         info = info + 10
         return
      endif
      if (info .lt. 0)  sinfo = info
c			calculate predictive mse
      if (jpmse .ne. 0) then
         call dpmse(fg,ldfg,nuobs,nobs,ncts1,fgaux,svals,npsing,
     *    fkf(nctp1,nctp1),ldfkf,y,y(nctp1),ntbl,adiag,tbl,ldtbl,
     *    auxtbl,work)
      endif
c			calculate coefficients
      call dcfcr1(fg,ldfg,ncts1,fgaux,fkf(nctp1,nctp1),ldfkf,
     * fkf(1,nctp1),ldfkf,nuobs,svals,npsing,nlamht,y,y(nctp1),coef,
     * dout(2),work,info)
      if (info .gt. 0) then
         info = info + 20
         return
      endif
      call dpdcr (fg,ldfg,nuobs,ncts1,fgaux,svals,npsing,
     * fkf(nctp1,nctp1),ldfkf,nlamht,y,y(nctp1),y,work)
      if (jadiag .ne. 0) then
          call ddiag(fg,ldfg,nuobs,ncts1,fgaux,svals,npsing,
     *     fkf(nctp1,nctp1),ldfkf,nlamht,adiag,work)
      endif
      if (sinfo .lt. 0) info = sinfo
      return
      end


c -----------------------------------------------------------
      subroutine dvlop(z,svals,nobs,nnull,npsing,inadd,ssqw2,
     * lamlim,ntbl,nlamht,tbl,ldtbl,auxtbl,dout,job,info)
      integer nobs,nnull,npsing,ntbl,ldtbl,job,info
      double precision z(npsing),svals(npsing),inadd,ssqw2,lamlim(2),
     * nlamht,tbl(ldtbl,3),auxtbl(3,3),dout(2)
c
c Purpose: determine the optimal lambda for the generalized cross
c	validation function given singular values and the data vector
c	in canonical coordinates.
c
c On Entry:
c   z(npsing)		data vector in canonical coordinates
c   svals(npsing)	singular values
c   nobs		number of observations
c   nnull		dimension of the null space of sigma
c   npsing		number of positive elements of svals
c   inadd		constant term in expression for V
c   ssqw2		squared length of w2
c   lamlim(2)		limits on lambda hat search (in log10(nobs*
c			lambda) scale) if user input limits are
c			requested. if lamlim(1) = lamlim(2) then nlamht
c			is set to 10**lamlim(1)
c   ntbl		number of evenly spaced values for
c			log10(nobs*lambda) to be used in the initial
c			grid search for lambda hat
c			if ntbl = 0 only a golden ratio search will be
c			done and tbl is not referenced, if ntbl > 0
c			there will be ntbl rows returned in tbl
c   ldtbl		leading dimension of tbl as declared in the
c			calling program
c   job      		if job is nonzero then user input limits on
c			lambda hat search are used
c
c On Exit:
c   lamlim(2)		limits on lambda hat search
c			(in log10(nobs*lambda) scale)
c   nlamht		nobs*(lambda hat) where lambda hat is the gcv
c			estimate of lambda
c   tbl(ldtbl,3)	column	contains
c			  1 	grid of log10(nobs*lambda)
c			  2  	V(lambda)
c   auxtbl(3,3)		auxiliary table
c			1st row contains:
c			    log10(nobs*lambda hat), V(lambda hat)
c			2nd row contains:
c			    0, V(0)
c			3rd row contains:
c			    0, V(infinity)
c   dout(2)		contains:
c			1  rss
c			2  tr(I-A)
c   info		error indicator
c			  0 : successful completion
c			 -1 : log10(nlamht) <= lamlim(1) (not fatal)
c			 -2 : log10(nlamht) >= lamlim(2) (not fatal)
c			  1 : svals(1) = 0.0d0
c			  2 : npsing is incorrect
c			  3 : lamlim(1) > lamlim(2)
c
c Subprograms Called Directly:
c	Gcvpack - dvmin
c
c Subprograms Called Indirectly:
c	Gcvpack - dvl
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,k
      double precision vlamht,w
      double precision dvmin
c
      common / gcvcom / addend,rss,tria,n,h
      integer n,h
      double precision addend,rss,tria,machpr,one
c
      info = 0
      one = 1.0d0
      machpr = 1.0d0
   10 machpr = machpr/2.0d0
      if (one .lt. 1.0d0 + machpr) goto 10
      machpr = machpr*2.0d0
c
      n=nobs
      h=nnull
      addend = inadd
      if (svals(1) .eq. 0.0d0) then
         info = 1
         return
      endif
      k = 0
      do 20 i = 1,npsing
         if (svals(i) .gt. 0) then
            k = i
         endif
   20 continue
      if (k .ne. npsing) then
	 info = 2
    	 return
      endif
      if (job .ne. 0 .and. (lamlim(1) .gt. lamlim(2))) then
         info = 3
         return
      endif
      if (job .eq. 0) then
         lamlim(2) = 2.0d0*dlog10(svals(1))+2.0d0
         lamlim(1) = 2.0d0*dlog10(svals(npsing))-2.0d0
      endif
      nlamht = dvmin (lamlim(1),lamlim(2),svals,z,npsing,ntbl,tbl,
     * ldtbl,vlamht,info)
      dout(1) = rss
      dout(2) = tria
c			compute auxtbl
      auxtbl(1,1)=nlamht
      auxtbl(1,2)=vlamht
c			lambda = 0
      auxtbl(2,1)=0.0d0
      auxtbl(2,2)=0.0d0
      if ((nobs-nnull) .ne. npsing) then
         auxtbl(2,2)=inadd*(nobs)/(nobs-nnull-npsing)**2
      endif
      if ((nobs-nnull) .eq. npsing) then
         w=0.0d0
         do 30 i=npsing,1,-1
c           w=w+(z(i)*svals(npsing)**2/(svals(i)**2))**2
            w=w+(z(i)*(svals(npsing)/svals(i))**2)**2
   30    continue
         auxtbl(2,2)=nobs*w
         w=0.0d0
         do 40 i=npsing,1,-1
            w=w+(svals(npsing)/svals(i))**2
   40    continue
         auxtbl(2,2)=auxtbl(2,2)/(w**2)
      endif
c			lambda = infinity
      auxtbl(3,1)=0.0d0
      auxtbl(3,2)=ssqw2/(nobs - nnull)
      nlamht = 10**nlamht
      return
      end


c -----------------------------------------------------------
      subroutine ddiag(fg,ldfg,nobs,nnull,fgaux,svals,npsing,u,ldu,
     * nlamht,adiag,work)
      integer ldfg,nobs,nnull,npsing,ldu
      double precision fg(ldfg,nnull),fgaux(nnull),svals(npsing),
     * u(ldu,npsing),nlamht,adiag(nobs),work(*)
c
c Purpose: determine the diagonal of hat matrix for nobs*lamhat
c
c On Entry:
c   fg(ldfg,nnull)	information on the Householder transformations
c			that define f and g
c   ldfg		leading dimension of fg as declared in the
c			calling	program
c   nobs		number of rows in f
c   nnull		number of columns in g
c   fgaux(nnull)	auxiliary information on the fg Householder
c			transformations
c   svals(npsing)	singular values
c   npsing		number of positive singular values
c   u(ldu,npsing)	left singular vectors corresponding to svals
c   ldu	    		leading dimension of u as declared in the
c			calling	program
c   nlamht		nobs*lambda hat
c
c On Exit:
c   adiag(nobs)		diagonal elements of the hat matrix if requested
c
c Work Arrays:
c   work(nobs+npsing)	double precision work vector
c
c Subprograms Called Directly:
c	Linpack - dqrsl
c	Blas    - ddot dgemv
c	Other   - dset
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,hp1,locinf,nmh,np1
      double precision dummy(1)
      double precision ddot
c
      np1 = nobs + 1
      hp1 = nnull + 1
      nmh = nobs - nnull
c			form adiag
      do 20 i = 1,nobs
         call dset(nobs,0.0d0,work,1)
         work(i)=1.0d0
         call dqrsl(fg,ldfg,nobs,nnull,fgaux,work,dummy,work,dummy,
     *    dummy,dummy,01000,locinf)
         adiag(i)=ddot(nnull,work,1,work,1)
	 call dgemv('T',nmh,npsing,1.0d0,u,ldu,work(hp1),1,0.0d0,
     *    work(np1),1)
         do 10 j=1,npsing
            work(nobs+j)=work(nobs+j)*svals(j)/dsqrt(svals(j)**2+nlamht)
   10    continue
         adiag(i)=adiag(i) + ddot(npsing,work(np1),1,work(np1),1)
   20 continue
      return
      end


c -----------------------------------------------------------
      subroutine drsap(fg,ldfg,nobs,nnull,fgaux,u,ldu,nmh,npsing,z,
     * ssqw2,addend,work)
      integer ldfg,nobs,nnull,ldu,nmh,npsing
      double precision fg(ldfg,nnull),fgaux(nnull),u(ldu,npsing),
     * z(nobs),ssqw2,addend,work(npsing)
c
c Purpose: apply Householder transformations to a response vector and
c	collect its inner product with u and the addend which are used
c	to define the generalized cross validation function with a
c	semi-norm.
c
c On Entry:
c   fg(ldfg,nnull)	information on the Householder transformations
c			that define f and g
c   ldfg		leading dimension of fg as declared in the
c			calling program
c   nobs		number of rows in fg
c   nnull		number of columns in fg
c   fgaux(nnull)	auxiliary information on the fg	Householder
c			transformations
c   u(ldu,npsing)		left singular vectors
c   ldu			leading dimension of u as declared in the
c			calling program
c   nmh	     		number of rows in u. nmh = nobs - nnull
c   npsing		number of columns in u (maximum of npar - nnull)
c   z(nobs)		response vector
c
c On Exit:
c   z(nobs)		the first nnull positions contain w1 and the
c			next npsing positions contain u'w2
c   ssqw2		the squared length of w2
c   addend		the squared length of z minus the squared length
c			of u'w2
c
c Work Arrays:
c   work(npsing) 	double precision work vector
c
c Subprograms Called Directly:
c      Linpack - dqrsl
c      Blas    - ddot dcopy dgemv
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer locinf,hp1
      double precision dummy(1)
      double precision ddot
c			apply Householder transformations
c			which define f
      call dqrsl (fg,ldfg,nobs,nnull,fgaux,z,dummy,z,dummy,dummy,dummy,
     * 01000,locinf)
c			w1 in first nnull positions of z,w2 in
c			last nmh
      hp1 = nnull + 1
      addend = ddot(nmh,z(hp1),1,z(hp1),1)
      ssqw2=addend
      call dgemv('T',nmh,npsing,1.0d0,u,ldu,z(hp1),1,0.0d0,work,1)
c			u'w2 in positions nnull+1 to
c			nnull+npsing of z
      call dcopy (npsing,work,1,z(hp1),1)
      addend = addend - ddot(npsing,z(hp1),1,z(hp1),1)
      return
      end


c -----------------------------------------------------------
      subroutine dftkf(fg,ldfg,nrf,ncg,fgaux,kk,ldkk,work)
      integer ldfg,nrf,ncg,ldkk
      double precision fg(ldfg,ncg),fgaux(ncg),kk(ldkk,nrf),work(nrf)
c
c Purpose: create f'k f.
c
c On Entry:
c   fg(ldfg,ncg)	qr decomposition of [t:s1]
c   ldfg		leading dimension of fg as declared in the
c   			calling program
c   nrf 		number of rows in f
c   ncg			number of columns in g
c   fgaux(ncg)		auxiliary information on the qr decomposition
c			of [t:s1]
c   kk(ldkk,nrf) 	k
c   ldkk		leading dimension of kk as declared in the
c   			calling program
c
c On Exit:
c   kk(ldkk,nrf)  	f'k f
c
c Work Array:
c   work(nrf)		double precision work vector
c
c Subprograms Called Directly:
c	Linpack - dqrsl
c	Blas    - dcopy
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      double precision dummy
      integer i,locinf
c	  		calculate k f, store in kk
      do 10 i=1,nrf
         call dcopy(nrf,kk(i,1),ldkk,work,1)
         call dqrsl(fg,ldfg,nrf,ncg,fgaux,work,dummy,work,dummy,dummy,
     *    dummy,01000,locinf)
         call dcopy(nrf,work,1,kk(i,1),ldkk)
   10 continue
c	  		calculate f'k f
      do 20 i=1,nrf
         call dqrsl(fg,ldfg,nrf,ncg,fgaux,kk(1,i),dummy,kk(1,i),dummy,
     *    dummy,dummy,01000,locinf)
   20 continue
      return
      end


c -----------------------------------------------------------
      subroutine dpmse(fg,ldfg,nuobs,nobs,nnull,fgaux,svals,npsing,u,
     * ldu,w1,z,ntbl,adiag,tbl,ldtbl,auxtbl,work)
      integer ldfg,nuobs,nobs,nnull,npsing,ldu,ntbl,ldtbl
      double precision fg(ldfg,nnull),fgaux(nnull),svals(npsing),
     * u(ldu,npsing),w1(nnull),z(npsing),adiag(nuobs),tbl(ldtbl,3),
     * auxtbl(3,3),work(npsing)
c
c Purpose: determine the predictive mean squared error for each lambda
c	value in tbl.
c
c On Entry:
c   fg(ldfg,nnull)	information on the Householder transformations
c			that define f and g
c   ldfg		leading dimension of fg as declared
c			in the calling program
c   nuobs		number of rows in f
c   nnull		number of columns in g
c   fgaux(nnull)	auxiliary information on the fg Householder
c			transformations
c   svals(npsing)	singular values
c   npsing		number of singular values
c   u(ldu,npsing)	left singular vectors corresponding to svals
c   ldu			leading dimension of u as declared in the
c			calling program
c   w1(nnull)		leading part of rotated response vector
c   z(npsing)		u'w2
c   ntbl		number of rows in tbl
c   adiag(nuobs)	"true" y values
c   tbl(ldtbl,3)	column	contains
c			  1 	grid of log10(nobs*lambda)
c   ldtbl		leading dimension of tbl as declared in the
c			calling program
c   auxtbl(3,3)		auxiliary table
c			auxtbl(1,1) contains log10(nobs*lamhat) where
c			lamhat is the gcv estimate of lambda
c
c On Exit:
c   tbl(ldtbl,3)	column	contains
c			  1 	grid of log10(nobs*lambda)
c			  3     R(lambda)
c   auxtbl(3,3)		auxiliary table
c			3rd column contains:
c			    [R(lamhat) , R(0), R(infinity)]'
c
c Work Arrays:
c   work(npsing)	double precision work vector
c
c Subprograms Called Directly:
c      Linpack - dqrsl
c      Blas    - ddot dgemv
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,nmh,k,locinf
      double precision dummy,nlam,wrk1,addtru,wrk
      double precision ddot
c
      nmh = nuobs - nnull
      addtru = 0.0d0
      call dqrsl(fg,ldfg,nuobs,nnull,fgaux,adiag,dummy,adiag,dummy,
     * dummy,dummy,01000,locinf)
c			the first nnull positions of adiag now contain
c			w1 true the last nuobs-nnull positions contain
c			w2 true
      do 10 i = 1,nnull
         addtru=addtru + (w1(i)-adiag(i))**2
   10 continue
      addtru = addtru + ddot(nmh,adiag(nnull+1),1,adiag(nnull+1),1)
      call dgemv('T',nmh,npsing,1.0d0,u,ldu,adiag(nnull+1),1,0.0d0,
     *  work,1)
      addtru = addtru - ddot(npsing,work,1,work,1)
c			addtru contains ||w1 - (w1 true)||**2 +
c			||w2 true||**2 - ||z true||**2
c			work contains z true
c
c			compute predictive mse for each lambda in tbl
      do 30 k = 1,ntbl
         nlam = 10**tbl(k,1)
         wrk=0.0d0
         do 20 i=1,npsing
            wrk1=(svals(i)**2)/(svals(i)**2+nlam)
            wrk = wrk + (work(i)-z(i)*wrk1)**2
   20    continue
         tbl(k,3)=(addtru+wrk)/nobs
   30 continue
c			add pred. mse for lambda hat to auxtbl
      wrk=0.0d0
      nlam=10**auxtbl(1,1)
      do 40 i=1,npsing
         wrk1=(svals(i)**2)/(svals(i)**2+nlam)
         wrk = wrk + (work(i)-z(i)*wrk1)**2
   40 continue
      auxtbl(1,3)=(addtru+wrk)/nobs
c			add pmse for lambda = 0
      wrk=0.0d0
      do 50 i=1,npsing
         wrk = wrk + (work(i)-z(i))**2
   50 continue
      auxtbl(2,3)=(addtru+wrk)/nobs
c			add pmse for lambda = infinity
      auxtbl(3,3)=(addtru+ddot(npsing,work,1,work,1))/nobs
      return
      end


c -----------------------------------------------------------
      subroutine  dset(n,da,dx,incx)
      integer n,incx
      double precision da,dx(*)
c
c Purpose : set vector dx to constant da. Unrolled loops are used for
c	increment equal to one.
c
c On Entry:
c   n			length of dx
c   da			any constant
c   incx		increment for dx
c
c On Exit:
c   dx(n)		vector with all n entries set to da
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,m,mp1,nincx
c
      if(n.le.0)return
      if(incx.eq.1)go to 20
c
c        code for increment not equal to 1
c
      nincx = n*incx
      do 10 i = 1,nincx,incx
        dx(i) = da
   10 continue
      return
c
c        code for increment equal to 1
c
c
c        clean-up loop
c
   20 m = mod(n,5)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dx(i) = da
   30 continue
      if( n .lt. 5 ) return
   40 mp1 = m + 1
      do 50 i = mp1,n,5
        dx(i) = da
        dx(i + 1) = da
        dx(i + 2) = da
        dx(i + 3) = da
        dx(i + 4) = da
   50 continue
      return
      end


c -----------------------------------------------------------
      subroutine dqrdc(x,ldx,n,p,qraux,jpvt,work,job)
      integer ldx,n,p,job
      integer jpvt(1)
      double precision x(ldx,1),qraux(1),work(1)
c
c     dqrdc uses householder transformations to compute the qr
c     factorization of an n by p matrix x.  column pivoting
c     based on the 2-norms of the reduced columns may be
c     performed at the users option.
c
c     on entry
c
c        x       double precision(ldx,p), where ldx .ge. n.
c                x contains the matrix whose decomposition is to be
c                computed.
c
c        ldx     integer.
c                ldx is the leading dimension of the array x.
c
c        n       integer.
c                n is the number of rows of the matrix x.
c
c        p       integer.
c                p is the number of columns of the matrix x.
c
c        jpvt    integer(p).
c                jpvt contains integers that control the selection
c                of the pivot columns.  the k-th column x(k) of x
c                is placed in one of three classes according to the
c                value of jpvt(k).
c
c                   if jpvt(k) .gt. 0, then x(k) is an initial
c                                      column.
c
c                   if jpvt(k) .eq. 0, then x(k) is a free column.
c
c                   if jpvt(k) .lt. 0, then x(k) is a final column.
c
c                before the decomposition is computed, initial columns
c                are moved to the beginning of the array x and final
c                columns to the end.  both initial and final columns
c                are frozen in place during the computation and only
c                free columns are moved.  at the k-th stage of the
c                reduction, if x(k) is occupied by a free column
c                it is interchanged with the free column of largest
c                reduced norm.  jpvt is not referenced if
c                job .eq. 0.
c
c        work    double precision(p).
c                work is a work array.  work is not referenced if
c                job .eq. 0.
c
c        job     integer.
c                job is an integer that initiates column pivoting.
c                if job .eq. 0, no pivoting is done.
c                if job .ne. 0, pivoting is done.
c
c     on return
c
c        x       x contains in its upper triangle the upper
c                triangular matrix r of the qr factorization.
c                below its diagonal x contains information from
c                which the orthogonal part of the decomposition
c                can be recovered.  note that if pivoting has
c                been requested, the decomposition is not that
c                of the original matrix x but that of x
c                with its columns permuted as described by jpvt.
c
c        qraux   double precision(p).
c                qraux contains further information required to recover
c                the orthogonal part of the decomposition.
c
c        jpvt    jpvt(k) contains the index of the column of the
c                original matrix that has been interchanged into
c                the k-th column, if pivoting was requested.
c
c     linpack. this version dated 08/14/78 .
c     g.w. stewart, university of maryland, argonne national lab.
c
c     dqrdc uses the following functions and subprograms.
c
c     blas daxpy,ddot,dscal,dswap,dnrm2
c     fortran dabs,dmax1,min0,dsqrt
c
c     internal variables
c
      integer j,jp,l,lp1,lup,maxj,pl,pu
      double precision maxnrm,dnrm2,tt
      double precision ddot,nrmxl,t
      logical negj,swapj
c
c
      pl = 1
      pu = 0
      if (job .eq. 0) go to 60
c
c        pivoting has been requested.  rearrange the columns
c        according to jpvt.
c
         do 20 j = 1, p
            swapj = jpvt(j) .gt. 0
            negj = jpvt(j) .lt. 0
            jpvt(j) = j
            if (negj) jpvt(j) = -j
            if (.not.swapj) go to 10
               if (j .ne. pl) call dswap(n,x(1,pl),1,x(1,j),1)
               jpvt(j) = jpvt(pl)
               jpvt(pl) = j
               pl = pl + 1
   10       continue
   20    continue
         pu = p
         do 50 jj = 1, p
            j = p - jj + 1
            if (jpvt(j) .ge. 0) go to 40
               jpvt(j) = -jpvt(j)
               if (j .eq. pu) go to 30
                  call dswap(n,x(1,pu),1,x(1,j),1)
                  jp = jpvt(pu)
                  jpvt(pu) = jpvt(j)
                  jpvt(j) = jp
   30          continue
               pu = pu - 1
   40       continue
   50    continue
   60 continue
c
c     compute the norms of the free columns.
c
      if (pu .lt. pl) go to 80
      do 70 j = pl, pu
         qraux(j) = dnrm2(n,x(1,j),1)
         work(j) = qraux(j)
   70 continue
   80 continue
c
c     perform the householder reduction of x.
c
      lup = min0(n,p)
      do 200 l = 1, lup
         if (l .lt. pl .or. l .ge. pu) go to 120
c
c           locate the column of largest norm and bring it
c           into the pivot position.
c
            maxnrm = 0.0d0
            maxj = l
            do 100 j = l, pu
               if (qraux(j) .le. maxnrm) go to 90
                  maxnrm = qraux(j)
                  maxj = j
   90          continue
  100       continue
            if (maxj .eq. l) go to 110
               call dswap(n,x(1,l),1,x(1,maxj),1)
               qraux(maxj) = qraux(l)
               work(maxj) = work(l)
               jp = jpvt(maxj)
               jpvt(maxj) = jpvt(l)
               jpvt(l) = jp
  110       continue
  120    continue
         qraux(l) = 0.0d0
         if (l .eq. n) go to 190
c
c           compute the householder transformation for column l.
c
            nrmxl = dnrm2(n-l+1,x(l,l),1)
            if (nrmxl .eq. 0.0d0) go to 180
               if (x(l,l) .ne. 0.0d0) nrmxl = dsign(nrmxl,x(l,l))
               call dscal(n-l+1,1.0d0/nrmxl,x(l,l),1)
               x(l,l) = 1.0d0 + x(l,l)
c
c              apply the transformation to the remaining columns,
c              updating the norms.
c
               lp1 = l + 1
               if (p .lt. lp1) go to 170
               do 160 j = lp1, p
                  t = -ddot(n-l+1,x(l,l),1,x(l,j),1)/x(l,l)
                  call daxpy(n-l+1,t,x(l,l),1,x(l,j),1)
                  if (j .lt. pl .or. j .gt. pu) go to 150
                  if (qraux(j) .eq. 0.0d0) go to 150
                     tt = 1.0d0 - (dabs(x(l,j))/qraux(j))**2
                     tt = dmax1(tt,0.0d0)
                     t = tt
                     tt = 1.0d0 + 0.05d0*tt*(qraux(j)/work(j))**2
                     if (tt .eq. 1.0d0) go to 130
                        qraux(j) = qraux(j)*dsqrt(t)
                     go to 140
  130                continue
                        qraux(j) = dnrm2(n-l,x(l+1,j),1)
                        work(j) = qraux(j)
  140                continue
  150             continue
  160          continue
  170          continue
c
c              save the transformation.
c
               qraux(l) = x(l,l)
               x(l,l) = -nrmxl
  180       continue
  190    continue
  200 continue
      return
      end


c -----------------------------------------------------------
      subroutine dchdc(a,lda,p,work,jpvt,job,info)
      integer lda,p,jpvt(1),job,info
      double precision a(lda,1),work(1)
c
c     dchdc computes the cholesky decomposition of a positive definite
c     matrix.  a pivoting option allows the user to estimate the
c     condition of a positive definite matrix or determine the rank
c     of a positive semidefinite matrix.
c
c     on entry
c
c         a      double precision(lda,p).
c                a contains the matrix whose decomposition is to
c                be computed.  onlt the upper half of a need be stored.
c                the lower part of the array a is not referenced.
c
c         lda    integer.
c                lda is the leading dimension of the array a.
c
c         p      integer.
c                p is the order of the matrix.
c
c         work   double precision.
c                work is a work array.
c
c         jpvt   integer(p).
c                jpvt contains integers that control the selection
c                of the pivot elements, if pivoting has been requested.
c                each diagonal element a(k,k)
c                is placed in one of three classes according to the
c                value of jpvt(k).
c
c                   if jpvt(k) .gt. 0, then x(k) is an initial
c                                      element.
c
c                   if jpvt(k) .eq. 0, then x(k) is a free element.
c
c                   if jpvt(k) .lt. 0, then x(k) is a final element.
c
c                before the decomposition is computed, initial elements
c                are moved by symmetric row and column interchanges to
c                the beginning of the array a and final
c                elements to the end.  both initial and final elements
c                are frozen in place during the computation and only
c                free elements are moved.  at the k-th stage of the
c                reduction, if a(k,k) is occupied by a free element
c                it is interchanged with the largest free element
c                a(l,l) with l .ge. k.  jpvt is not referenced if
c                job .eq. 0.
c
c        job     integer.
c                job is an integer that initiates column pivoting.
c                if job .eq. 0, no pivoting is done.
c                if job .ne. 0, pivoting is done.
c
c     on return
c
c         a      a contains in its upper half the cholesky factor
c                of the matrix a as it has been permuted by pivoting.
c
c         jpvt   jpvt(j) contains the index of the diagonal element
c                of a that was moved into the j-th position,
c                provided pivoting was requested.
c
c         info   contains the index of the last positive diagonal
c                element of the cholesky factor.
c
c     for positive definite matrices info = p is the normal return.
c     for pivoting with positive semidefinite matrices info will
c     in general be less than p.  however, info may be greater than
c     the rank of a, since rounding error can cause an otherwise zero
c     element to be positive. indefinite systems will always cause
c     info to be less than p.
c
c     linpack. this version dated 08/14/78 .
c     j.j. dongarra and g.w. stewart, argonne national laboratory and
c     university of maryland.
c
c
c     blas daxpy,dswap
c     fortran dsqrt
c
c     internal variables
c
      integer pu,pl,plp1,i,j,jp,jt,k,kb,km1,kp1,l,maxl
      double precision temp
      double precision maxdia
      logical swapk,negk
c
      pl = 1
      pu = 0
      info = p
      if (job .eq. 0) go to 160
c
c        pivoting has been requested. rearrange the
c        the elements according to jpvt.
c
         do 70 k = 1, p
            swapk = jpvt(k) .gt. 0
            negk = jpvt(k) .lt. 0
            jpvt(k) = k
            if (negk) jpvt(k) = -jpvt(k)
            if (.not.swapk) go to 60
               if (k .eq. pl) go to 50
                  call dswap(pl-1,a(1,k),1,a(1,pl),1)
                  temp = a(k,k)
                  a(k,k) = a(pl,pl)
                  a(pl,pl) = temp
                  plp1 = pl + 1
                  if (p .lt. plp1) go to 40
                  do 30 j = plp1, p
                     if (j .ge. k) go to 10
                        temp = a(pl,j)
                        a(pl,j) = a(j,k)
                        a(j,k) = temp
                     go to 20
   10                continue
                     if (j .eq. k) go to 20
                        temp = a(k,j)
                        a(k,j) = a(pl,j)
                        a(pl,j) = temp
   20                continue
   30             continue
   40             continue
                  jpvt(k) = jpvt(pl)
                  jpvt(pl) = k
   50          continue
               pl = pl + 1
   60       continue
   70    continue
         pu = p
         if (p .lt. pl) go to 150
         do 140 kb = pl, p
            k = p - kb + pl
            if (jpvt(k) .ge. 0) go to 130
               jpvt(k) = -jpvt(k)
               if (pu .eq. k) go to 120
                  call dswap(k-1,a(1,k),1,a(1,pu),1)
                  temp = a(k,k)
                  a(k,k) = a(pu,pu)
                  a(pu,pu) = temp
                  kp1 = k + 1
                  if (p .lt. kp1) go to 110
                  do 100 j = kp1, p
                     if (j .ge. pu) go to 80
                        temp = a(k,j)
                        a(k,j) = a(j,pu)
                        a(j,pu) = temp
                     go to 90
   80                continue
                     if (j .eq. pu) go to 90
                        temp = a(k,j)
                        a(k,j) = a(pu,j)
                        a(pu,j) = temp
   90                continue
  100             continue
  110             continue
                  jt = jpvt(k)
                  jpvt(k) = jpvt(pu)
                  jpvt(pu) = jt
  120          continue
               pu = pu - 1
  130       continue
  140    continue
  150    continue
  160 continue
      do 270 k = 1, p
c
c        reduction loop.
c
         maxdia = a(k,k)
         kp1 = k + 1
         maxl = k
c
c        determine the pivot element.
c
         if (k .lt. pl .or. k .ge. pu) go to 190
            do 180 l = kp1, pu
               if (a(l,l) .le. maxdia) go to 170
                  maxdia = a(l,l)
                  maxl = l
  170          continue
  180       continue
  190    continue
c
c        quit if the pivot element is not positive.
c
         if (maxdia .gt. 0.0d0) go to 200
            info = k - 1
c     ......exit
            go to 280
  200    continue
         if (k .eq. maxl) go to 210
c
c           start the pivoting and update jpvt.
c
            km1 = k - 1
            call dswap(km1,a(1,k),1,a(1,maxl),1)
            a(maxl,maxl) = a(k,k)
            a(k,k) = maxdia
            jp = jpvt(maxl)
            jpvt(maxl) = jpvt(k)
            jpvt(k) = jp
  210    continue
c
c        reduction step. pivoting is contained across the rows.
c
         work(k) = dsqrt(a(k,k))
         a(k,k) = work(k)
         if (p .lt. kp1) go to 260
         do 250 j = kp1, p
            if (k .eq. maxl) go to 240
               if (j .ge. maxl) go to 220
                  temp = a(k,j)
                  a(k,j) = a(j,maxl)
                  a(j,maxl) = temp
               go to 230
  220          continue
               if (j .eq. maxl) go to 230
                  temp = a(k,j)
                  a(k,j) = a(maxl,j)
                  a(maxl,j) = temp
  230          continue
  240       continue
            a(k,j) = a(k,j)/work(k)
            work(j) = a(k,j)
            temp = -a(k,j)
            call daxpy(j-k,temp,work(kp1),1,a(kp1,j),1)
  250    continue
  260    continue
  270 continue
  280 continue
      return
      end


c -----------------------------------------------------------
      subroutine dsvdc(x,ldx,n,p,s,e,u,ldu,v,ldv,work,job,info)
      integer ldx,n,p,ldu,ldv,job,info
      double precision x(ldx,1),s(1),e(1),u(ldu,1),v(ldv,1),work(1)
c
c
c     dsvdc is a subroutine to reduce a double precision nxp matrix x
c     by orthogonal transformations u and v to diagonal form.  the
c     diagonal elements s(i) are the singular values of x.  the
c     columns of u are the corresponding left singular vectors,
c     and the columns of v the right singular vectors.
c
c     on entry
c
c         x         double precision(ldx,p), where ldx.ge.n.
c                   x contains the matrix whose singular value
c                   decomposition is to be computed.  x is
c                   destroyed by dsvdc.
c
c         ldx       integer.
c                   ldx is the leading dimension of the array x.
c
c         n         integer.
c                   n is the number of rows of the matrix x.
c
c         p         integer.
c                   p is the number of columns of the matrix x.
c
c         ldu       integer.
c                   ldu is the leading dimension of the array u.
c                   (see below).
c
c         ldv       integer.
c                   ldv is the leading dimension of the array v.
c                   (see below).
c
c         work      double precision(n).
c                   work is a scratch array.
c
c         job       integer.
c                   job controls the computation of the singular
c                   vectors.  it has the decimal expansion ab
c                   with the following meaning
c
c                        a.eq.0    do not compute the left singular
c                                  vectors.
c                        a.eq.1    return the n left singular vectors
c                                  in u.
c                        a.ge.2    return the first min(n,p) singular
c                                  vectors in u.
c                        b.eq.0    do not compute the right singular
c                                  vectors.
c                        b.eq.1    return the right singular vectors
c                                  in v.
c
c     on return
c
c         s         double precision(mm), where mm=min(n+1,p).
c                   the first min(n,p) entries of s contain the
c                   singular values of x arranged in descending
c                   order of magnitude.
c
c         e         double precision(p),
c                   e ordinarily contains zeros.  however see the
c                   discussion of info for exceptions.
c
c         u         double precision(ldu,k), where ldu.ge.n.  if
c                                   joba.eq.1 then k.eq.n, if joba.ge.2
c                                   then k.eq.min(n,p).
c                   u contains the matrix of left singular vectors.
c                   u is not referenced if joba.eq.0.  if n.le.p
c                   or if joba.eq.2, then u may be identified with x
c                   in the subroutine call.
c
c         v         double precision(ldv,p), where ldv.ge.p.
c                   v contains the matrix of right singular vectors.
c                   v is not referenced if job.eq.0.  if p.le.n,
c                   then v may be identified with x in the
c                   subroutine call.
c
c         info      integer.
c                   the singular values (and their corresponding
c                   singular vectors) s(info+1),s(info+2),...,s(m)
c                   are correct (here m=min(n,p)).  thus if
c                   info.eq.0, all the singular values and their
c                   vectors are correct.  in any event, the matrix
c                   b = trans(u)*x*v is the bidiagonal matrix
c                   with the elements of s on its diagonal and the
c                   elements of e on its super-diagonal (trans(u)
c                   is the transpose of u).  thus the singular
c                   values of x and b are the same.
c
c     linpack. this version dated 08/14/78 .
c              correction made to shift 2/84.
c     g.w. stewart, university of maryland, argonne national lab.
c
c     dsvdc uses the following functions and subprograms.
c
c     external drot
c     blas daxpy,ddot,dscal,dswap,dnrm2,drotg
c     fortran dabs,dmax1,max0,min0,mod,dsqrt
c
c     internal variables
c
      integer i,iter,j,jobu,k,kase,kk,l,ll,lls,lm1,lp1,ls,lu,m,maxit,
     *        mm,mm1,mp1,nct,nctp1,ncu,nrt,nrtp1
      double precision ddot,t,r
      double precision b,c,cs,el,emm1,f,g,dnrm2,scale,shift,sl,sm,sn,
     *                 smm1,t1,test,ztest
      logical wantu,wantv
c
c
c     set the maximum number of iterations.
c
      maxit = 30
c
c     determine what is to be computed.
c
      wantu = .false.
      wantv = .false.
      jobu = mod(job,100)/10
      ncu = n
      if (jobu .gt. 1) ncu = min0(n,p)
      if (jobu .ne. 0) wantu = .true.
      if (mod(job,10) .ne. 0) wantv = .true.
c
c     reduce x to bidiagonal form, storing the diagonal elements
c     in s and the super-diagonal elements in e.
c
      info = 0
      nct = min0(n-1,p)
      nrt = max0(0,min0(p-2,n))
      lu = max0(nct,nrt)
      if (lu .lt. 1) go to 170
      do 160 l = 1, lu
         lp1 = l + 1
         if (l .gt. nct) go to 20
c
c           compute the transformation for the l-th column and
c           place the l-th diagonal in s(l).
c
            s(l) = dnrm2(n-l+1,x(l,l),1)
            if (s(l) .eq. 0.0d0) go to 10
               if (x(l,l) .ne. 0.0d0) s(l) = dsign(s(l),x(l,l))
               call dscal(n-l+1,1.0d0/s(l),x(l,l),1)
               x(l,l) = 1.0d0 + x(l,l)
   10       continue
            s(l) = -s(l)
   20    continue
         if (p .lt. lp1) go to 50
         do 40 j = lp1, p
            if (l .gt. nct) go to 30
            if (s(l) .eq. 0.0d0) go to 30
c
c              apply the transformation.
c
               t = -ddot(n-l+1,x(l,l),1,x(l,j),1)/x(l,l)
               call daxpy(n-l+1,t,x(l,l),1,x(l,j),1)
   30       continue
c
c           place the l-th row of x into  e for the
c           subsequent calculation of the row transformation.
c
            e(j) = x(l,j)
   40    continue
   50    continue
         if (.not.wantu .or. l .gt. nct) go to 70
c
c           place the transformation in u for subsequent back
c           multiplication.
c
            do 60 i = l, n
               u(i,l) = x(i,l)
   60       continue
   70    continue
         if (l .gt. nrt) go to 150
c
c           compute the l-th row transformation and place the
c           l-th super-diagonal in e(l).
c
            e(l) = dnrm2(p-l,e(lp1),1)
            if (e(l) .eq. 0.0d0) go to 80
               if (e(lp1) .ne. 0.0d0) e(l) = dsign(e(l),e(lp1))
               call dscal(p-l,1.0d0/e(l),e(lp1),1)
               e(lp1) = 1.0d0 + e(lp1)
   80       continue
            e(l) = -e(l)
            if (lp1 .gt. n .or. e(l) .eq. 0.0d0) go to 120
c
c              apply the transformation.
c
               do 90 i = lp1, n
                  work(i) = 0.0d0
   90          continue
               do 100 j = lp1, p
                  call daxpy(n-l,e(j),x(lp1,j),1,work(lp1),1)
  100          continue
               do 110 j = lp1, p
                  call daxpy(n-l,-e(j)/e(lp1),work(lp1),1,x(lp1,j),1)
  110          continue
  120       continue
            if (.not.wantv) go to 140
c
c              place the transformation in v for subsequent
c              back multiplication.
c
               do 130 i = lp1, p
                  v(i,l) = e(i)
  130          continue
  140       continue
  150    continue
  160 continue
  170 continue
c
c     set up the final bidiagonal matrix or order m.
c
      m = min0(p,n+1)
      nctp1 = nct + 1
      nrtp1 = nrt + 1
      if (nct .lt. p) s(nctp1) = x(nctp1,nctp1)
      if (n .lt. m) s(m) = 0.0d0
      if (nrtp1 .lt. m) e(nrtp1) = x(nrtp1,m)
      e(m) = 0.0d0
c
c     if required, generate u.
c
      if (.not.wantu) go to 300
         if (ncu .lt. nctp1) go to 200
         do 190 j = nctp1, ncu
            do 180 i = 1, n
               u(i,j) = 0.0d0
  180       continue
            u(j,j) = 1.0d0
  190    continue
  200    continue
         if (nct .lt. 1) go to 290
         do 280 ll = 1, nct
            l = nct - ll + 1
            if (s(l) .eq. 0.0d0) go to 250
               lp1 = l + 1
               if (ncu .lt. lp1) go to 220
               do 210 j = lp1, ncu
                  t = -ddot(n-l+1,u(l,l),1,u(l,j),1)/u(l,l)
                  call daxpy(n-l+1,t,u(l,l),1,u(l,j),1)
  210          continue
  220          continue
               call dscal(n-l+1,-1.0d0,u(l,l),1)
               u(l,l) = 1.0d0 + u(l,l)
               lm1 = l - 1
               if (lm1 .lt. 1) go to 240
               do 230 i = 1, lm1
                  u(i,l) = 0.0d0
  230          continue
  240          continue
            go to 270
  250       continue
               do 260 i = 1, n
                  u(i,l) = 0.0d0
  260          continue
               u(l,l) = 1.0d0
  270       continue
  280    continue
  290    continue
  300 continue
c
c     if it is required, generate v.
c
      if (.not.wantv) go to 350
         do 340 ll = 1, p
            l = p - ll + 1
            lp1 = l + 1
            if (l .gt. nrt) go to 320
            if (e(l) .eq. 0.0d0) go to 320
               do 310 j = lp1, p
                  t = -ddot(p-l,v(lp1,l),1,v(lp1,j),1)/v(lp1,l)
                  call daxpy(p-l,t,v(lp1,l),1,v(lp1,j),1)
  310          continue
  320       continue
            do 330 i = 1, p
               v(i,l) = 0.0d0
  330       continue
            v(l,l) = 1.0d0
  340    continue
  350 continue
c
c     main iteration loop for the singular values.
c
      mm = m
      iter = 0
  360 continue
c
c        quit if all the singular values have been found.
c
c     ...exit
         if (m .eq. 0) go to 620
c
c        if too many iterations have been performed, set
c        flag and return.
c
         if (iter .lt. maxit) go to 370
            info = m
c     ......exit
            go to 620
  370    continue
c
c        this section of the program inspects for
c        negligible elements in the s and e arrays.  on
c        completion the variables kase and l are set as follows.
c
c           kase = 1     if s(m) and e(l-1) are negligible and l.lt.m
c           kase = 2     if s(l) is negligible and l.lt.m
c           kase = 3     if e(l-1) is negligible, l.lt.m, and
c                        s(l), ..., s(m) are not negligible (qr step).
c           kase = 4     if e(m-1) is negligible (convergence).
c
         do 390 ll = 1, m
            l = m - ll
c        ...exit
            if (l .eq. 0) go to 400
            test = dabs(s(l)) + dabs(s(l+1))
            ztest = test + dabs(e(l))
            if (ztest .ne. test) go to 380
               e(l) = 0.0d0
c        ......exit
               go to 400
  380       continue
  390    continue
  400    continue
         if (l .ne. m - 1) go to 410
            kase = 4
         go to 480
  410    continue
            lp1 = l + 1
            mp1 = m + 1
            do 430 lls = lp1, mp1
               ls = m - lls + lp1
c           ...exit
               if (ls .eq. l) go to 440
               test = 0.0d0
               if (ls .ne. m) test = test + dabs(e(ls))
               if (ls .ne. l + 1) test = test + dabs(e(ls-1))
               ztest = test + dabs(s(ls))
               if (ztest .ne. test) go to 420
                  s(ls) = 0.0d0
c           ......exit
                  go to 440
  420          continue
  430       continue
  440       continue
            if (ls .ne. l) go to 450
               kase = 3
            go to 470
  450       continue
            if (ls .ne. m) go to 460
               kase = 1
            go to 470
  460       continue
               kase = 2
               l = ls
  470       continue
  480    continue
         l = l + 1
c
c        perform the task indicated by kase.
c
         go to (490,520,540,570), kase
c
c        deflate negligible s(m).
c
  490    continue
            mm1 = m - 1
            f = e(m-1)
            e(m-1) = 0.0d0
            do 510 kk = l, mm1
               k = mm1 - kk + l
               t1 = s(k)
               call drotg(t1,f,cs,sn)
               s(k) = t1
               if (k .eq. l) go to 500
                  f = -sn*e(k-1)
                  e(k-1) = cs*e(k-1)
  500          continue
               if (wantv) call drot(p,v(1,k),1,v(1,m),1,cs,sn)
  510       continue
         go to 610
c
c        split at negligible s(l).
c
  520    continue
            f = e(l-1)
            e(l-1) = 0.0d0
            do 530 k = l, m
               t1 = s(k)
               call drotg(t1,f,cs,sn)
               s(k) = t1
               f = -sn*e(k)
               e(k) = cs*e(k)
               if (wantu) call drot(n,u(1,k),1,u(1,l-1),1,cs,sn)
  530       continue
         go to 610
c
c        perform one qr step.
c
  540    continue
c
c           calculate the shift.
c
            scale = dmax1(dabs(s(m)),dabs(s(m-1)),dabs(e(m-1)),
     *                    dabs(s(l)),dabs(e(l)))
            sm = s(m)/scale
            smm1 = s(m-1)/scale
            emm1 = e(m-1)/scale
            sl = s(l)/scale
            el = e(l)/scale
            b = ((smm1 + sm)*(smm1 - sm) + emm1**2)/2.0d0
            c = (sm*emm1)**2
            shift = 0.0d0
            if (b .eq. 0.0d0 .and. c .eq. 0.0d0) go to 550
               shift = dsqrt(b**2+c)
               if (b .lt. 0.0d0) shift = -shift
               shift = c/(b + shift)
  550       continue
            f = (sl + sm)*(sl - sm) + shift
            g = sl*el
c
c           chase zeros.
c
            mm1 = m - 1
            do 560 k = l, mm1
               call drotg(f,g,cs,sn)
               if (k .ne. l) e(k-1) = f
               f = cs*s(k) + sn*e(k)
               e(k) = cs*e(k) - sn*s(k)
               g = sn*s(k+1)
               s(k+1) = cs*s(k+1)
               if (wantv) call drot(p,v(1,k),1,v(1,k+1),1,cs,sn)
               call drotg(f,g,cs,sn)
               s(k) = f
               f = cs*e(k) + sn*s(k+1)
               s(k+1) = -sn*e(k) + cs*s(k+1)
               g = sn*e(k+1)
               e(k+1) = cs*e(k+1)
               if (wantu .and. k .lt. n)
     *            call drot(n,u(1,k),1,u(1,k+1),1,cs,sn)
  560       continue
            e(m-1) = f
            iter = iter + 1
         go to 610
c
c        convergence.
c
  570    continue
c
c           make the singular value  positive.
c
            if (s(l) .ge. 0.0d0) go to 580
               s(l) = -s(l)
               if (wantv) call dscal(p,-1.0d0,v(1,l),1)
  580       continue
c
c           order the singular value.
c
  590       if (l .eq. mm) go to 600
c           ...exit
               if (s(l) .ge. s(l+1)) go to 600
               t = s(l)
               s(l) = s(l+1)
               s(l+1) = t
               if (wantv .and. l .lt. p)
     *            call dswap(p,v(1,l),1,v(1,l+1),1)
               if (wantu .and. l .lt. n)
     *            call dswap(n,u(1,l),1,u(1,l+1),1)
               l = l + 1
            go to 590
  600       continue
            iter = 0
            m = m - 1
  610    continue
      go to 360
  620 continue
      return
      end


c -----------------------------------------------------------
      subroutine dpdcr(fg,ldfg,nobs,nnull,fgaux,svals,npsing,u,ldu,
     * nlamht,w1,g,pred,work)
      integer ldfg,nobs,nnull,npsing,ldu
      double precision fg(ldfg,nnull),fgaux(nnull),svals(npsing),
     * u(ldu,npsing),nlamht,w1(nnull),g(npsing),pred(nobs),
     * work(*)
c
c Purpose: determine the predicted responses for a given value of
c	nobs*lamhat and vectors g and w1.
c
c On Entry:
c   fg(ldfg,nnull)	information on the Householder transformations
c			that define f and g
c   ldfg		leading dimension of fg as declared in the
c			calling	program
c   nobs		number of rows in f
c   nnull		number of columns in g
c   fgaux(nnull)	auxiliary information on the fg Householder
c			transformations
c   svals(npsing)	singular values
c   npsing		number of positive singular values
c   u(ldu,npsing)	left singular vectors corresponding to svals
c   ldu	    		leading dimension of u as declared in the
c			calling	program
c   nlamht		nobs*lambda hat
c   w1(nnull)		leading part of rotated response vector
c   g(npsing)		(D**2 + nlamht*I)*-1 Dz
c
c On Exit:
c   pred(nobs)		predicted responses
c
c Work Arrays:
c   work(nobs+npsing)	double precision work vector
c
c Subprograms Called Directly:
c	Linpack - dqrsl
c	Blas    - dcopy dgemv
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,locinf,nmh,np1
      double precision dummy(1)
c
c
      np1 = nobs + 1
      nmh = nobs - nnull
c			form the response vector
      call dcopy (nnull,w1,1,pred,1)
      call dcopy (npsing,g,1,work(np1),1)
      do 10 i = 1,npsing
         work(nobs+i) = work(nobs+i)*svals(i)
   10 continue
      call dgemv('N',nmh,npsing,1.0d0,u,ldu,work(np1),1,0.0d0,
     *  pred(nnull+1),1)
      call dqrsl (fg,ldfg,nobs,nnull,fgaux,pred,pred,dummy,dummy,dummy,
     * dummy,10000,locinf)
      return
      end


c -----------------------------------------------------------
      integer function fact(i)
      integer i
c
c Purpose: quick factorial function for the bspline routine
c	returns zero for negative i.
c
c On Entry:
c   i			a non-negative integer
c On Exit:
c   fact		i factorial
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer j
      fact = 0
      if (i .ge. 0) fact = 1
      if (i .le. 1) return
      do 10 j = 2,i
	 fact = fact*j
   10	continue
      return
      end


c -----------------------------------------------------------
      subroutine dcfcr1(fg,ldfg,ncts1,fgaux,u,ldu,f1kf2,ldfkf,nuobs,
     * svals,npsing,nlamht,w1,z,coef,penlty,work,info)
      integer ldfg,ldfkf,ncts1,ldu,nuobs,npsing,info
      double precision fg(ldfg,ncts1),fgaux(ncts1),u(ldu,*),
     * f1kf2(ldfkf,*),svals(npsing),nlamht,w1(ncts1),z(npsing),coef(*),
     * penlty,work(*)
c
c Purpose: determine the coefficients for a given value of nlamht
c	and vectors z and w1.
c
c On Entry:
c   fg(ldfg,ncts1)	information on the Householder transformations
c   			that define f and g
c   ldfg		leading dimension of fg as declared in the
c			calling	program
c   ncts1		number of columns in g
c   fgaux(ncts1)	auxiliary information on the fg Householder
c			transformations
c   u(ldu,npsing)	left singular vectors corresponding to svals
c   ldu	    		leading dimension of u as declared in the
c			calling	program
c   f1kf2(ldfkf,nuobs-ncts1) f1 k f2
c   ldfkf		leading dimension of f1kf2 as declared
c			in the calling program
c   nuobs		number of rows in fg
c   svals(npsing)	singular values of f2'k f2
c   npsing		number of positive singular
c   nlamht		nobs*(lambda hat)
c   w1(ncts1)		leading part of rotated response vector
c   z(npsing)		u'w2
c
c On Exit:
c   z(npsing)		g = [ (D**2 +nlamht)**-1 ] D z
c   coef(nuobs+ncts1)	estimated coefficients
c   penlty		smoothness penalty which equals	gamma'gamma
c   info		error indicator
c			  0 : successful completion
c			  1 : error in dtrco, g is singular
c
c Work Arrays:
c   work(nuobs-ncts1)  	double precision work vector
c
c   Subprograms Used:
c      Linpack - dqrsl dtrsl
c      Blas    - ddot dcopy dgemv
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer i,j,nmnct,nctp1,locinf
      double precision dummy,machpr,one,rcond
      double precision ddot
c
      info = 0
      one = 1.0d0
      machpr = 1.0d0
   10 machpr = machpr/2.0d0
      if (one .lt. 1.0d0 + machpr) goto 10
      machpr = machpr*2.0d0
c
      nmnct = nuobs - ncts1
      nctp1 = ncts1 + 1
c			form g and penalty
      do 20 j = 1,npsing
         z(j) = z(j)*svals(j)/(svals(j)**2 + nlamht)
   20 continue
      penlty = ddot(npsing,z,1,z,1)
c			z now contains g
c			compute xi
      do 30 i = 1,npsing
         coef(i) = z(i)/svals(i)
   30 continue
      call dgemv('N',nmnct,npsing,1.0d0,u,ldu,coef,1,0.0d0,
     *  work,1)
      do 40 j = 1,ncts1
         coef(ncts1+j) = 0.0d0
   40 continue
      call dcopy(nmnct,work,1,coef(2*ncts1+1),1)
      call dqrsl(fg,ldfg,nuobs,ncts1,fgaux,coef(nctp1),coef(nctp1),
     * dummy,dummy,dummy,dummy,10000,locinf)
c			compute beta
      call dcopy(ncts1,w1,1,coef,1)
      call dgemv('N',ncts1,nmnct,-1.0d0,f1kf2,ldfkf,work,1,1.0d0,
     *  coef,1)
c			check condition number of g
      call dtrco(fg,ldfg,ncts1,rcond,work,1)
      if (rcond .le. machpr*100) then
         info = 1
         return
      endif
      call dtrsl (fg,ldfg,ncts1,coef,01,info)
      return
      end


c -----------------------------------------------------------
      subroutine  drot (n,dx,incx,dy,incy,c,s)
c
c     applies a plane rotation.
c     jack dongarra, linpack, 3/11/78.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision dx(*),dy(*),dtemp,c,s
      integer i,incx,incy,ix,iy,n
c
      if(n.le.0)return
      if(incx.eq.1.and.incy.eq.1)go to 20
c
c       code for unequal increments or equal increments not equal
c         to 1
c
      ix = 1
      iy = 1
      if(incx.lt.0)ix = (-n+1)*incx + 1
      if(incy.lt.0)iy = (-n+1)*incy + 1
      do 10 i = 1,n
        dtemp = c*dx(ix) + s*dy(iy)
        dy(iy) = c*dy(iy) - s*dx(ix)
        dx(ix) = dtemp
        ix = ix + incx
        iy = iy + incy
   10 continue
      return
c
c       code for both increments equal to 1
c
   20 do 30 i = 1,n
        dtemp = c*dx(i) + s*dy(i)
        dy(i) = c*dy(i) - s*dx(i)
        dx(i) = dtemp
   30 continue
      return
      end


c -----------------------------------------------------------
      subroutine  dscal(n,da,dx,incx)
c
c     scales a vector by a constant.
c     uses unrolled loops for increment equal to one.
c     jack dongarra, linpack, 3/11/78.
c     modified 3/93 to return if incx .le. 0.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision da,dx(*)
      integer i,incx,m,mp1,n,nincx
c
      if( n.le.0 .or. incx.le.0 )return
      if(incx.eq.1)go to 20
c
c        code for increment not equal to 1
c
      nincx = n*incx
      do 10 i = 1,nincx,incx
        dx(i) = da*dx(i)
   10 continue
      return
c
c        code for increment equal to 1
c
c
c        clean-up loop
c
   20 m = mod(n,5)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dx(i) = da*dx(i)
   30 continue
      if( n .lt. 5 ) return
   40 mp1 = m + 1
      do 50 i = mp1,n,5
        dx(i) = da*dx(i)
        dx(i + 1) = da*dx(i + 1)
        dx(i + 2) = da*dx(i + 2)
        dx(i + 3) = da*dx(i + 3)
        dx(i + 4) = da*dx(i + 4)
   50 continue
      return
      end


c -----------------------------------------------------------
      subroutine daxpy(n,da,dx,incx,dy,incy)
c
c     constant times a vector plus a vector.
c     uses unrolled loops for increments equal to one.
c     jack dongarra, linpack, 3/11/78.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision dx(*),dy(*),da
      integer i,incx,incy,ix,iy,m,mp1,n
c
      if(n.le.0)return
      if (da .eq. 0.0d0) return
      if(incx.eq.1.and.incy.eq.1)go to 20
c
c        code for unequal increments or equal increments
c          not equal to 1
c
      ix = 1
      iy = 1
      if(incx.lt.0)ix = (-n+1)*incx + 1
      if(incy.lt.0)iy = (-n+1)*incy + 1
      do 10 i = 1,n
        dy(iy) = dy(iy) + da*dx(ix)
        ix = ix + incx
        iy = iy + incy
   10 continue
      return
c
c        code for both increments equal to 1
c
c
c        clean-up loop
c
   20 m = mod(n,4)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dy(i) = dy(i) + da*dx(i)
   30 continue
      if( n .lt. 4 ) return
   40 mp1 = m + 1
      do 50 i = mp1,n,4
        dy(i) = dy(i) + da*dx(i)
        dy(i + 1) = dy(i + 1) + da*dx(i + 1)
        dy(i + 2) = dy(i + 2) + da*dx(i + 2)
        dy(i + 3) = dy(i + 3) + da*dx(i + 3)
   50 continue
      return
      end


c -----------------------------------------------------------
      double precision function ddot(n,dx,incx,dy,incy)
c
c     forms the dot product of two vectors.
c     uses unrolled loops for increments equal to one.
c     jack dongarra, linpack, 3/11/78.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision dx(*),dy(*),dtemp
      integer i,incx,incy,ix,iy,m,mp1,n
c
      ddot = 0.0d0
      dtemp = 0.0d0
      if(n.le.0)return
      if(incx.eq.1.and.incy.eq.1)go to 20
c
c        code for unequal increments or equal increments
c          not equal to 1
c
      ix = 1
      iy = 1
      if(incx.lt.0)ix = (-n+1)*incx + 1
      if(incy.lt.0)iy = (-n+1)*incy + 1
      do 10 i = 1,n
        dtemp = dtemp + dx(ix)*dy(iy)
        ix = ix + incx
        iy = iy + incy
   10 continue
      ddot = dtemp
      return
c
c        code for both increments equal to 1
c
c
c        clean-up loop
c
   20 m = mod(n,5)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dtemp = dtemp + dx(i)*dy(i)
   30 continue
      if( n .lt. 5 ) go to 60
   40 mp1 = m + 1
      do 50 i = mp1,n,5
        dtemp = dtemp + dx(i)*dy(i) + dx(i + 1)*dy(i + 1) +
     *   dx(i + 2)*dy(i + 2) + dx(i + 3)*dy(i + 3) + dx(i + 4)*dy(i + 4)
   50 continue
   60 ddot = dtemp
      return
      end


c -----------------------------------------------------------
      subroutine  dswap (n,dx,incx,dy,incy)
c
c     interchanges two vectors.
c     uses unrolled loops for increments equal one.
c     jack dongarra, linpack, 3/11/78.
c     modified 12/3/93, array(1) declarations changed to array(*)
c
      double precision dx(*),dy(*),dtemp
      integer i,incx,incy,ix,iy,m,mp1,n
c
      if(n.le.0)return
      if(incx.eq.1.and.incy.eq.1)go to 20
c
c       code for unequal increments or equal increments not equal
c         to 1
c
      ix = 1
      iy = 1
      if(incx.lt.0)ix = (-n+1)*incx + 1
      if(incy.lt.0)iy = (-n+1)*incy + 1
      do 10 i = 1,n
        dtemp = dx(ix)
        dx(ix) = dy(iy)
        dy(iy) = dtemp
        ix = ix + incx
        iy = iy + incy
   10 continue
      return
c
c       code for both increments equal to 1
c
c
c       clean-up loop
c
   20 m = mod(n,3)
      if( m .eq. 0 ) go to 40
      do 30 i = 1,m
        dtemp = dx(i)
        dx(i) = dy(i)
        dy(i) = dtemp
   30 continue
      if( n .lt. 3 ) return
   40 mp1 = m + 1
      do 50 i = mp1,n,3
        dtemp = dx(i)
        dx(i) = dy(i)
        dy(i) = dtemp
        dtemp = dx(i + 1)
        dx(i + 1) = dy(i + 1)
        dy(i + 1) = dtemp
        dtemp = dx(i + 2)
        dx(i + 2) = dy(i + 2)
        dy(i + 2) = dtemp
   50 continue
      return
      end


c -----------------------------------------------------------
      DOUBLE PRECISION FUNCTION DNRM2 ( N, X, INCX )
*     .. Scalar Arguments ..
      INTEGER                           INCX, N
*     .. Array Arguments ..
      DOUBLE PRECISION                  X( * )
*     ..
*
*  DNRM2 returns the euclidean norm of a vector via the function
*  name, so that
*
*     DNRM2 := sqrt( x'*x )
*
*
*
*  -- This version written on 25-October-1982.
*     Modified on 14-October-1993 to inline the call to DLASSQ.
*     Sven Hammarling, Nag Ltd.
*
*
*     .. Parameters ..
      DOUBLE PRECISION      ONE         , ZERO
      PARAMETER           ( ONE = 1.0D+0, ZERO = 0.0D+0 )
*     .. Local Scalars ..
      INTEGER               IX
      DOUBLE PRECISION      ABSXI, NORM, SCALE, SSQ
*     .. Intrinsic Functions ..
      INTRINSIC             ABS, SQRT
*     ..
*     .. Executable Statements ..
      IF( N.LT.1 .OR. INCX.LT.1 )THEN
         NORM  = ZERO
      ELSE IF( N.EQ.1 )THEN
         NORM  = ABS( X( 1 ) )
      ELSE
         SCALE = ZERO
         SSQ   = ONE
*        The following loop is equivalent to this call to the LAPACK
*        auxiliary routine:
*        CALL DLASSQ( N, X, INCX, SCALE, SSQ )
*
         DO 10, IX = 1, 1 + ( N - 1 )*INCX, INCX
            IF( X( IX ).NE.ZERO )THEN
               ABSXI = ABS( X( IX ) )
               IF( SCALE.LT.ABSXI )THEN
                  SSQ   = ONE   + SSQ*( SCALE/ABSXI )**2
                  SCALE = ABSXI
               ELSE
                  SSQ   = SSQ   +     ( ABSXI/SCALE )**2
               END IF
            END IF
   10    CONTINUE
         NORM  = SCALE * SQRT( SSQ )
      END IF
*
      DNRM2 = NORM
      RETURN
*
*     End of DNRM2.
*
      END


c -----------------------------------------------------------
      subroutine dtrco(t,ldt,n,rcond,z,job)
      integer ldt,n,job
      double precision t(ldt,1),z(1)
      double precision rcond
c
c     dtrco estimates the condition of a double precision triangular
c     matrix.
c
c     on entry
c
c        t       double precision(ldt,n)
c                t contains the triangular matrix. the zero
c                elements of the matrix are not referenced, and
c                the corresponding elements of the array can be
c                used to store other information.
c
c        ldt     integer
c                ldt is the leading dimension of the array t.
c
c        n       integer
c                n is the order of the system.
c
c        job     integer
c                = 0         t  is lower triangular.
c                = nonzero   t  is upper triangular.
c
c     on return
c
c        rcond   double precision
c                an estimate of the reciprocal condition of  t .
c                for the system  t*x = b , relative perturbations
c                in  t  and  b  of size  epsilon  may cause
c                relative perturbations in  x  of size  epsilon/rcond .
c                if  rcond  is so small that the logical expression
c                           1.0 + rcond .eq. 1.0
c                is true, then  t  may be singular to working
c                precision.  in particular,  rcond  is zero  if
c                exact singularity is detected or the estimate
c                underflows.
c
c        z       double precision(n)
c                a work vector whose contents are usually unimportant.
c                if  t  is close to a singular matrix, then  z  is
c                an approximate null vector in the sense that
c                norm(a*z) = rcond*norm(a)*norm(z) .
c
c     linpack. this version dated 08/14/78 .
c     cleve moler, university of new mexico, argonne national lab.
c
c     subroutines and functions
c
c     blas daxpy,dscal,dasum
c     fortran dabs,dmax1,dsign
c
c     internal variables
c
      double precision w,wk,wkm,ek
      double precision tnorm,ynorm,s,sm,dasum
      integer i1,j,j1,j2,k,kk,l
      logical lower
c
      lower = job .eq. 0
c
c     compute 1-norm of t
c
      tnorm = 0.0d0
      do 10 j = 1, n
         l = j
         if (lower) l = n + 1 - j
         i1 = 1
         if (lower) i1 = j
         tnorm = dmax1(tnorm,dasum(l,t(i1,j),1))
   10 continue
c
c     rcond = 1/(norm(t)*(estimate of norm(inverse(t)))) .
c     estimate = norm(z)/norm(y) where  t*z = y  and  trans(t)*y = e .
c     trans(t)  is the transpose of t .
c     the components of  e  are chosen to cause maximum local
c     growth in the elements of y .
c     the vectors are frequently rescaled to avoid overflow.
c
c     solve trans(t)*y = e
c
      ek = 1.0d0
      do 20 j = 1, n
         z(j) = 0.0d0
   20 continue
      do 100 kk = 1, n
         k = kk
         if (lower) k = n + 1 - kk
         if (z(k) .ne. 0.0d0) ek = dsign(ek,-z(k))
         if (dabs(ek-z(k)) .le. dabs(t(k,k))) go to 30
            s = dabs(t(k,k))/dabs(ek-z(k))
            call dscal(n,s,z,1)
            ek = s*ek
   30    continue
         wk = ek - z(k)
         wkm = -ek - z(k)
         s = dabs(wk)
         sm = dabs(wkm)
         if (t(k,k) .eq. 0.0d0) go to 40
            wk = wk/t(k,k)
            wkm = wkm/t(k,k)
         go to 50
   40    continue
            wk = 1.0d0
            wkm = 1.0d0
   50    continue
         if (kk .eq. n) go to 90
            j1 = k + 1
            if (lower) j1 = 1
            j2 = n
            if (lower) j2 = k - 1
            do 60 j = j1, j2
               sm = sm + dabs(z(j)+wkm*t(k,j))
               z(j) = z(j) + wk*t(k,j)
               s = s + dabs(z(j))
   60       continue
            if (s .ge. sm) go to 80
               w = wkm - wk
               wk = wkm
               do 70 j = j1, j2
                  z(j) = z(j) + w*t(k,j)
   70          continue
   80       continue
   90    continue
         z(k) = wk
  100 continue
      s = 1.0d0/dasum(n,z,1)
      call dscal(n,s,z,1)
c
      ynorm = 1.0d0
c
c     solve t*z = y
c
      do 130 kk = 1, n
         k = n + 1 - kk
         if (lower) k = kk
         if (dabs(z(k)) .le. dabs(t(k,k))) go to 110
            s = dabs(t(k,k))/dabs(z(k))
            call dscal(n,s,z,1)
            ynorm = s*ynorm
  110    continue
         if (t(k,k) .ne. 0.0d0) z(k) = z(k)/t(k,k)
         if (t(k,k) .eq. 0.0d0) z(k) = 1.0d0
         i1 = 1
         if (lower) i1 = k + 1
         if (kk .ge. n) go to 120
            w = -z(k)
            call daxpy(n-kk,w,t(i1,k),1,z(i1),1)
  120    continue
  130 continue
c     make znorm = 1.0
      s = 1.0d0/dasum(n,z,1)
      call dscal(n,s,z,1)
      ynorm = s*ynorm
c
      if (tnorm .ne. 0.0d0) rcond = ynorm/tnorm
      if (tnorm .eq. 0.0d0) rcond = 0.0d0
      return
      end


c -----------------------------------------------------------
      subroutine dtrsl(t,ldt,n,b,job,info)
      integer ldt,n,job,info
      double precision t(ldt,1),b(1)
c
c
c     dtrsl solves systems of the form
c
c                   t * x = b
c     or
c                   trans(t) * x = b
c
c     where t is a triangular matrix of order n. here trans(t)
c     denotes the transpose of the matrix t.
c
c     on entry
c
c         t         double precision(ldt,n)
c                   t contains the matrix of the system. the zero
c                   elements of the matrix are not referenced, and
c                   the corresponding elements of the array can be
c                   used to store other information.
c
c         ldt       integer
c                   ldt is the leading dimension of the array t.
c
c         n         integer
c                   n is the order of the system.
c
c         b         double precision(n).
c                   b contains the right hand side of the system.
c
c         job       integer
c                   job specifies what kind of system is to be solved.
c                   if job is
c
c                        00   solve t*x=b, t lower triangular,
c                        01   solve t*x=b, t upper triangular,
c                        10   solve trans(t)*x=b, t lower triangular,
c                        11   solve trans(t)*x=b, t upper triangular.
c
c     on return
c
c         b         b contains the solution, if info .eq. 0.
c                   otherwise b is unaltered.
c
c         info      integer
c                   info contains zero if the system is nonsingular.
c                   otherwise info contains the index of
c                   the first zero diagonal element of t.
c
c     linpack. this version dated 08/14/78 .
c     g. w. stewart, university of maryland, argonne national lab.
c
c     subroutines and functions
c
c     blas daxpy,ddot
c     fortran mod
c
c     internal variables
c
      double precision ddot,temp
      integer case,j,jj
c
c     begin block permitting ...exits to 150
c
c        check for zero diagonal elements.
c
         do 10 info = 1, n
c     ......exit
            if (t(info,info) .eq. 0.0d0) go to 150
   10    continue
         info = 0
c
c        determine the task and go to it.
c
         case = 1
         if (mod(job,10) .ne. 0) case = 2
         if (mod(job,100)/10 .ne. 0) case = case + 2
         go to (20,50,80,110), case
c
c        solve t*x=b for t lower triangular
c
   20    continue
            b(1) = b(1)/t(1,1)
            if (n .lt. 2) go to 40
            do 30 j = 2, n
               temp = -b(j-1)
               call daxpy(n-j+1,temp,t(j,j-1),1,b(j),1)
               b(j) = b(j)/t(j,j)
   30       continue
   40       continue
         go to 140
c
c        solve t*x=b for t upper triangular.
c
   50    continue
            b(n) = b(n)/t(n,n)
            if (n .lt. 2) go to 70
            do 60 jj = 2, n
               j = n - jj + 1
               temp = -b(j+1)
               call daxpy(j,temp,t(1,j+1),1,b(1),1)
               b(j) = b(j)/t(j,j)
   60       continue
   70       continue
         go to 140
c
c        solve trans(t)*x=b for t lower triangular.
c
   80    continue
            b(n) = b(n)/t(n,n)
            if (n .lt. 2) go to 100
            do 90 jj = 2, n
               j = n - jj + 1
               b(j) = b(j) - ddot(jj-1,t(j+1,j),1,b(j+1),1)
               b(j) = b(j)/t(j,j)
   90       continue
  100       continue
         go to 140
c
c        solve trans(t)*x=b for t upper triangular.
c
  110    continue
            b(1) = b(1)/t(1,1)
            if (n .lt. 2) go to 130
            do 120 j = 2, n
               b(j) = b(j) - ddot(j-1,t(1,j),1,b(1),1)
               b(j) = b(j)/t(j,j)
  120       continue
  130       continue
  140    continue
  150 continue
      return
      end


c -----------------------------------------------------------
      subroutine drotg(da,db,c,s)
c
c     construct givens plane rotation.
c     jack dongarra, linpack, 3/11/78.
c
      double precision da,db,c,s,roe,scale,r,z
c
      roe = db
      if( dabs(da) .gt. dabs(db) ) roe = da
      scale = dabs(da) + dabs(db)
      if( scale .ne. 0.0d0 ) go to 10
         c = 1.0d0
         s = 0.0d0
         r = 0.0d0
         z = 0.0d0
         go to 20
   10 r = scale*dsqrt((da/scale)**2 + (db/scale)**2)
      r = dsign(1.0d0,roe)*r
      c = da/r
      s = db/r
      z = 1.0d0
      if( dabs(da) .gt. dabs(db) ) z = s
      if( dabs(db) .ge. dabs(da) .and. c .ne. 0.0d0 ) z = 1.0d0/c
   20 da = r
      db = z
      return
      end


c -----------------------------------------------------------
      double precision function dvmin(lower,upper,svals,z,npsing,
     * ntbl,tbl,ldtbl,vlamht,info)
      integer npsing,ntbl,ldtbl,info
      double precision lower,upper,svals(npsing),z(npsing),
     * tbl(ldtbl,3),vlamht
c
c Purpose: evaluate V(lambda) for a grid of ln(nobs*lambda) values
c	between	lower and upper, store these in the array tbl, and find
c	minimizer of v.
c
c On Entry:
c   lower		lower bound of interval (in nobs*ln(lambda)
c			scale) over which V(lambda) is to be minimized
c   upper		upper bound of interval (in nobs*ln(lambda)
c			scale) over which V(lambda) is to be minimized
c   svals(npsing)	singular values
c   z(npsing)		data vector in canonical coordinates
c   npsing		number of positive elements of svals
c   ntbl		number of evenly spaced values for
c			ln(nobs*lambda)	to be used in the initial grid
c			search for lambda hat
c			if ntbl = 0 only a golden ratio search will be
c			done and tbl is not referenced, if ntbl > 0
c			there will be ntbl rows returned in tbl
c   ldtbl		leading dimension of tbl as declared in the
c			calling program
c
c On Exit:
c   tbl(ldtbl,3)	column	contains
c			  1 	grid of ln(nobs*lambda)
c			  2  	V(lambda)
c   vlamht		V(lambda hat)
c   dvmin		ln(nobs*lambda hat)
c   info		error indicator
c			  0 : successful completion
c			 -1 : dvmin <= lower (not fatal)
c			 -2 : dvmin >= upper (not fatal)
c
c Subprograms Called Directly:
c	Gcvpack - dvl
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c

      double precision a,b,c,d,vc,vd,del,k1,k2,x,v
      integer j,jmin,k
      double precision dvl
c				null interval
      if (lower .eq. upper) then
	 dvmin = lower
	 info = -1
	 vlamht = dvl(lower,svals,z,npsing)
	 do 10 j = 1, ntbl
	    tbl(j,1) = lower
	    tbl(j,2) = vlamht
   10    continue
	 return
      end if
c				non-null interval
      info = 0
      a = lower
      b = upper
      if (ntbl .eq. 1) then
	 x = (a + b)/2
	 tbl(1,1) = x
	 tbl(1,2) = dvl(x,svals,z,npsing)
      else if (ntbl .ge. 2) then
c			do grid search
	 v=dvl(lower,svals,z,npsing)*2.0d0
	 del=(upper-lower)/(ntbl-1)
	 do 20 j = 1, ntbl
	    tbl(j,1) = lower + (j - 1) * del
	    tbl(j,2) = dvl(tbl(j,1),svals,z,npsing)
	    if (tbl(j,2) .le. v) then
	       jmin = j
	       v = tbl(j,2)
	    endif
   20    continue
	 a=tbl(jmin,1)-del
	 b=tbl(jmin,1)+del
      end if
c			do golden ratio search
      k1=(3.0d0-dsqrt(5.0d0))/2.0d0
      k2=(dsqrt(5.0d0)-1)/2.0d0
      c = a + k1*(b - a)
      d = a + k2*(b - a)
      vc = dvl(c,svals,z,npsing)
      vd = dvl(d,svals,z,npsing)
      do 30 k=1,50
	 if (vd .lt. vc) then
	    a = c
	    c = d
	    d = a + k2*(b - a)
	    vc = vd
	    vd = dvl(d,svals,z,npsing)
	 else
	    b = d
	    d = c
	    c = a + k1*(b - a)
	    vd = vc
	    vc = dvl(c,svals,z,npsing)
	 end if
   30 continue
      x=(a+b)/2
      if (x .le. lower) info = -1
      if (x .ge. upper) info = -2
      vlamht=dvl(x,svals,z,npsing)
      dvmin = x
      return
      end


c -----------------------------------------------------------
      subroutine dqrsl(x,ldx,n,k,qraux,y,qy,qty,b,rsd,xb,job,info)
      integer ldx,n,k,job,info
      double precision x(ldx,1),qraux(1),y(1),qy(1),qty(1),b(1),rsd(1),
     *                 xb(1)
c
c     dqrsl applies the output of dqrdc to compute coordinate
c     transformations, projections, and least squares solutions.
c     for k .le. min(n,p), let xk be the matrix
c
c            xk = (x(jpvt(1)),x(jpvt(2)), ... ,x(jpvt(k)))
c
c     formed from columnns jpvt(1), ... ,jpvt(k) of the original
c     n x p matrix x that was input to dqrdc (if no pivoting was
c     done, xk consists of the first k columns of x in their
c     original order).  dqrdc produces a factored orthogonal matrix q
c     and an upper triangular matrix r such that
c
c              xk = q * (r)
c                       (0)
c
c     this information is contained in coded form in the arrays
c     x and qraux.
c
c     on entry
c
c        x      double precision(ldx,p).
c               x contains the output of dqrdc.
c
c        ldx    integer.
c               ldx is the leading dimension of the array x.
c
c        n      integer.
c               n is the number of rows of the matrix xk.  it must
c               have the same value as n in dqrdc.
c
c        k      integer.
c               k is the number of columns of the matrix xk.  k
c               must nnot be greater than min(n,p), where p is the
c               same as in the calling sequence to dqrdc.
c
c        qraux  double precision(p).
c               qraux contains the auxiliary output from dqrdc.
c
c        y      double precision(n)
c               y contains an n-vector that is to be manipulated
c               by dqrsl.
c
c        job    integer.
c               job specifies what is to be computed.  job has
c               the decimal expansion abcde, with the following
c               meaning.
c
c                    if a.ne.0, compute qy.
c                    if b,c,d, or e .ne. 0, compute qty.
c                    if c.ne.0, compute b.
c                    if d.ne.0, compute rsd.
c                    if e.ne.0, compute xb.
c
c               note that a request to compute b, rsd, or xb
c               automatically triggers the computation of qty, for
c               which an array must be provided in the calling
c               sequence.
c
c     on return
c
c        qy     double precision(n).
c               qy conntains q*y, if its computation has been
c               requested.
c
c        qty    double precision(n).
c               qty contains trans(q)*y, if its computation has
c               been requested.  here trans(q) is the
c               transpose of the matrix q.
c
c        b      double precision(k)
c               b contains the solution of the least squares problem
c
c                    minimize norm2(y - xk*b),
c
c               if its computation has been requested.  (note that
c               if pivoting was requested in dqrdc, the j-th
c               component of b will be associated with column jpvt(j)
c               of the original matrix x that was input into dqrdc.)
c
c        rsd    double precision(n).
c               rsd contains the least squares residual y - xk*b,
c               if its computation has been requested.  rsd is
c               also the orthogonal projection of y onto the
c               orthogonal complement of the column space of xk.
c
c        xb     double precision(n).
c               xb contains the least squares approximation xk*b,
c               if its computation has been requested.  xb is also
c               the orthogonal projection of y onto the column space
c               of x.
c
c        info   integer.
c               info is zero unless the computation of b has
c               been requested and r is exactly singular.  in
c               this case, info is the index of the first zero
c               diagonal element of r and b is left unaltered.
c
c     the parameters qy, qty, b, rsd, and xb are not referenced
c     if their computation is not requested and in this case
c     can be replaced by dummy variables in the calling program.
c     to save storage, the user may in some cases use the same
c     array for different parameters in the calling sequence.  a
c     frequently occuring example is when one wishes to compute
c     any of b, rsd, or xb and does not need y or qty.  in this
c     case one may identify y, qty, and one of b, rsd, or xb, while
c     providing separate arrays for anything else that is to be
c     computed.  thus the calling sequence
c
c          call dqrsl(x,ldx,n,k,qraux,y,dum,y,b,y,dum,110,info)
c
c     will result in the computation of b and rsd, with rsd
c     overwriting y.  more generally, each item in the following
c     list contains groups of permissible identifications for
c     a single callinng sequence.
c
c          1. (y,qty,b) (rsd) (xb) (qy)
c
c          2. (y,qty,rsd) (b) (xb) (qy)
c
c          3. (y,qty,xb) (b) (rsd) (qy)
c
c          4. (y,qy) (qty,b) (rsd) (xb)
c
c          5. (y,qy) (qty,rsd) (b) (xb)
c
c          6. (y,qy) (qty,xb) (b) (rsd)
c
c     in any group the value returned in the array allocated to
c     the group corresponds to the last member of the group.
c
c     linpack. this version dated 08/14/78 .
c     g.w. stewart, university of maryland, argonne national lab.
c
c     dqrsl uses the following functions and subprograms.
c
c     blas daxpy,dcopy,ddot
c     fortran dabs,min0,mod
c
c     internal variables
c
      integer i,j,jj,ju,kp1
      double precision ddot,t,temp
      logical cb,cqy,cqty,cr,cxb
c
c
c     set info flag.
c
      info = 0
c
c     determine what is to be computed.
c
      cqy = job/10000 .ne. 0
      cqty = mod(job,10000) .ne. 0
      cb = mod(job,1000)/100 .ne. 0
      cr = mod(job,100)/10 .ne. 0
      cxb = mod(job,10) .ne. 0
      ju = min0(k,n-1)
c
c     special action when n=1.
c
      if (ju .ne. 0) go to 40
         if (cqy) qy(1) = y(1)
         if (cqty) qty(1) = y(1)
         if (cxb) xb(1) = y(1)
         if (.not.cb) go to 30
            if (x(1,1) .ne. 0.0d0) go to 10
               info = 1
            go to 20
   10       continue
               b(1) = y(1)/x(1,1)
   20       continue
   30    continue
         if (cr) rsd(1) = 0.0d0
      go to 250
   40 continue
c
c        set up to compute qy or qty.
c
         if (cqy) call dcopy(n,y,1,qy,1)
         if (cqty) call dcopy(n,y,1,qty,1)
         if (.not.cqy) go to 70
c
c           compute qy.
c
            do 60 jj = 1, ju
               j = ju - jj + 1
               if (qraux(j) .eq. 0.0d0) go to 50
                  temp = x(j,j)
                  x(j,j) = qraux(j)
                  t = -ddot(n-j+1,x(j,j),1,qy(j),1)/x(j,j)
                  call daxpy(n-j+1,t,x(j,j),1,qy(j),1)
                  x(j,j) = temp
   50          continue
   60       continue
   70    continue
         if (.not.cqty) go to 100
c
c           compute trans(q)*y.
c
            do 90 j = 1, ju
               if (qraux(j) .eq. 0.0d0) go to 80
                  temp = x(j,j)
                  x(j,j) = qraux(j)
                  t = -ddot(n-j+1,x(j,j),1,qty(j),1)/x(j,j)
                  call daxpy(n-j+1,t,x(j,j),1,qty(j),1)
                  x(j,j) = temp
   80          continue
   90       continue
  100    continue
c
c        set up to compute b, rsd, or xb.
c
         if (cb) call dcopy(k,qty,1,b,1)
         kp1 = k + 1
         if (cxb) call dcopy(k,qty,1,xb,1)
         if (cr .and. k .lt. n) call dcopy(n-k,qty(kp1),1,rsd(kp1),1)
         if (.not.cxb .or. kp1 .gt. n) go to 120
            do 110 i = kp1, n
               xb(i) = 0.0d0
  110       continue
  120    continue
         if (.not.cr) go to 140
            do 130 i = 1, k
               rsd(i) = 0.0d0
  130       continue
  140    continue
         if (.not.cb) go to 190
c
c           compute b.
c
            do 170 jj = 1, k
               j = k - jj + 1
               if (x(j,j) .ne. 0.0d0) go to 150
                  info = j
c           ......exit
                  go to 180
  150          continue
               b(j) = b(j)/x(j,j)
               if (j .eq. 1) go to 160
                  t = -b(j)
                  call daxpy(j-1,t,x(1,j),1,b,1)
  160          continue
  170       continue
  180       continue
  190    continue
         if (.not.cr .and. .not.cxb) go to 240
c
c           compute rsd or xb as required.
c
            do 230 jj = 1, ju
               j = ju - jj + 1
               if (qraux(j) .eq. 0.0d0) go to 220
                  temp = x(j,j)
                  x(j,j) = qraux(j)
                  if (.not.cr) go to 200
                     t = -ddot(n-j+1,x(j,j),1,rsd(j),1)/x(j,j)
                     call daxpy(n-j+1,t,x(j,j),1,rsd(j),1)
  200             continue
                  if (.not.cxb) go to 210
                     t = -ddot(n-j+1,x(j,j),1,xb(j),1)/x(j,j)
                     call daxpy(n-j+1,t,x(j,j),1,xb(j),1)
  210             continue
                  x(j,j) = temp
  220          continue
  230       continue
  240    continue
  250 continue
      return
      end


c -----------------------------------------------------------
      double precision function dvl(lgnlam,svals,z,npsing)
      integer npsing
      double precision lgnlam,svals(npsing),z(npsing)
c
c Purpose: evaluate the cross-validation function with a semi-norm.
c
c On Entry:
c   lgnlam		log10(nobs*lambda) where lambda is the value of
c			lambda for which V is evaluated
c   svals(npsing)	singular values
c   z(npsing)		data vector in canonical coordinates
c   npsing		number of positive svals
c
c On Exit:
c   dvl			V(lambda)
c
c $Header: /cvsroot/visad/paoloa/spline/tpspline.f,v 1.3 2000/04/26 15:46:27 dglo Exp $
c
      integer j
      double precision nlam,numrtr,denom,factor
c
      common / gcvcom / addend,rss,tria,n,h
      integer n,h
      double precision rss,tria,addend
c     			see dvlop for definition of common block
c			variables
c
      nlam = 10**lgnlam
      numrtr = addend
      denom = dble(n - h - npsing)
      do 10 j = 1,npsing
         factor = 1.0d0/(1.0d0 + (svals(j)**2)/nlam)
         numrtr = numrtr + (factor*z(j))**2
         denom = denom + factor
   10 continue
      rss=numrtr
      tria=denom
      dvl=dble(n)*numrtr/denom**2
      return
      end
