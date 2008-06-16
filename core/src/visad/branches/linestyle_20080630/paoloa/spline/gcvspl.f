      SUBROUTINE getspline( Y, S0, VAL, MODE, wkvalue )
c--------editorial remark by Eric Grosse, 14 Jun 87-------------
c  If you plan to use this to automatically mininize GCV, read the
c  comments very carefully and run tests.  For example, on a problem
c  where the optimal smoothing parameter was about .69 I found that
c       spar=.7
c       gcvspl(...,2,spar,...)
c  failed, but
c       spar=.7
c       gcvspl(...,1,spar,...)
c       gcvspl(...,-2,spar,...)
c  worked fine.
c  On Unix systems, some of the less portable syntax can be removed by
c  filtering with the following:
c	sed '/!.*/s///
c		/TYPE/s//PRINT/
c		/ACCEPT/s//READ/
c		/^D/d'
c--------------------------------------------------------------------
C GCV.FOR, 1986-02-11
C
C Author: H.J. Woltring
C
C Organizations: University of Nijmegen, and
C                Philips Medical Systems, Eindhoven
C                (The Netherlands)
C
C**********************************************************************
C
C       Testprogramme for generalized cross-validatory spline smoothing
C       with subroutine GCVSPL and function SPLDER using the data of
C       C.L. Vaughan, Smoothing and differentiation of displacement-
C       time data: an application of splines and digital filtering.
C       International Journal of Bio-Medical Computing 13(1982)375-382.
C
C       The only subprogrammes to be conventionally called by a user
C       are subroutine GCVSPL for calculating the spline parameters,
C       and function SPLDER for calculating the spline function and its
C       derivatives within the knot range.  See the comments in the
C       headers of these subprogrammes for further details.
C
C       The programme types out statistics on the estimation procedure
C       and on the estimated second derivatives. If the DEBUG-lines are
C       compiled, also the raw data and the estimated spline values,
C       first and second derivatives at the knot positions are typed.
C
C**********************************************************************
      IMPLICIT REAL*8 (A-H,O-Z), LOGICAL (L)
      PARAMETER ( K=1, NN=50, MM=10, MM2=MM*2, NWK=NN+6*(NN*MM+1) )
      REAL*8 S0(NN)
      DIMENSION X(NN), Y(NN), WX(NN), C(NN), WK(NWK), Q(0:MM), V(MM2)
      DIMENSION wkvalue(1)
      DATA WX/50*1D0/, WY/1D0/, AT/9.85D-3/   !Weights, sampling interval
      SCALE = 125D-3 / DATAN(1D0)             !1/(2*PI)
C
C***  Create time array (knot array)
C
      DO 10 IX=1,NN
         X(IX) = AT * (IX - 1)
   10 CONTINUE
c      print *,y
c      print *,s0
c      print *,val
c      print *,mode
c      print *,wkvalue
C
C***  Get parameters (see comments in subroutine GCVSPL) or exit
C
      M=2
c      MODE=1
c     VAL=.2
      N=50
C
C***  Assess spline coefficients and type resulting statistics
C
      CALL GCVSPL ( X, Y, NN, WX, WY, M, N, K, MODE, VAL, C, NN,
     1             WK, IER)
      IF (IER.EQ.0) THEN
         VAR = WK(6)
         IF (WK(4).EQ.0D0) THEN
            FRE = 5D-1 / AT
         ELSE
            FRE = SCALE * (WK(4)*AT)**(-0.5/M)
         ENDIF
      ENDIF
C
C***  Reconstruct data, type i, x(i), y(i), s(i), s'(i), s''(i) [D]
C
      IDM = MIN0(2,M)
      DACCAV = 0D0
      DACCSD = 0D0
      Q(2)   = 0D0
      DO 40 I=1,N
         J = I
         DO 30 IDER=0,IDM
            Q(IDER) = SPLDER ( IDER, M, N, X(I), X, C, J, V )
   30    CONTINUE
         S0(I) = Q(0)
C         write(*,*) S0(i)
  750    FORMAT(I4,':',5F13.6)
         DACCAV = DACCAV + Q(2)
         DACCSD = DACCSD + Q(2)*Q(2)
   40 CONTINUE
      ACCAV = DACCAV / N
      ACCSD = DSQRT((DACCSD - ACCAV*DACCAV)/(N-1))
C
      if (mode.eq.1) then
         wkvalue(1) = 0
      endif

      if (WK(4).le.10e-10) then
         wkvalue(1)=-10
      else
         wkvalue(1) = log10(WK(4))
      endif
C      write (*,*) 'this is wkvalue',wkvalue(1)
      RETURN
      END
C GCVSPL.FOR, 1986-05-12
C
C***********************************************************************
C
C SUBROUTINE GCVSPL (REAL*8)
C
C Purpose:
C *******
C
C       Natural B-spline data smoothing subroutine, using the Generali-
C       zed Cross-Validation and Mean-Squared Prediction Error Criteria
C       of Craven & Wahba (1979). Alternatively, the amount of smoothing
C       can be given explicitly, or it can be based on the effective
C       number of degrees of freedom in the smoothing process as defined
C       by Wahba (1980). The model assumes uncorrelated, additive noise
C       and essentially smooth, underlying functions. The noise may be
C       non-stationary, and the independent co-ordinates may be spaced
C       non-equidistantly. Multiple datasets, with common independent
C       variables and weight factors are accomodated.
C
C
C Calling convention:
C ******************
C
C       CALL GCVSPL ( X, Y, NY, WX, WY, M, N, K, MD, VAL, C, NC, WK, IER )
C
C Meaning of parameters:
C *********************
C
C       X(N)    ( I )   Independent variables: strictly increasing knot
C                       sequence, with X(I-1).lt.X(I), I=2,...,N.
C       Y(NY,K) ( I )   Input data to be smoothed (or interpolated).
C       NY      ( I )   First dimension of array Y(NY,K), with NY.ge.N.
C       WX(N)   ( I )   Weight factor array; WX(I) corresponds with
C                       the relative inverse variance of point Y(I,*).
C                       If no relative weighting information is
C                       available, the WX(I) should be set to ONE.
C                       All WX(I).gt.ZERO, I=1,...,N.
C       WY(K)   ( I )   Weight factor array; WY(J) corresponds with
C                       the relative inverse variance of point Y(*,J).
C                       If no relative weighting information is
C                       available, the WY(J) should be set to ONE.
C                       All WY(J).gt.ZERO, J=1,...,K.
C                       NB: The effective weight for point Y(I,J) is
C                       equal to WX(I)*WY(J).
C       M       ( I )   Half order of the required B-splines (spline
C                       degree 2*M-1), with M.gt.0. The values M =
C                       1,2,3,4 correspond to linear, cubic, quintic,
C                       and heptic splines, respectively.
C       N       ( I )   Number of observations per dataset, with N.ge.2*M.
C       K       ( I )   Number of datasets, with K.ge.1.
C       MD      ( I )   Optimization mode switch:
C                       |MD| = 1: Prior given value for p in VAL
C                                 (VAL.ge.ZERO). This is the fastest
C                                 use of GCVSPL, since no iteration
C                                 is performed in p.
C                       |MD| = 2: Generalized cross validation.
C                       |MD| = 3: True predicted mean-squared error,
C                                 with prior given variance in VAL.
C                       |MD| = 4: Prior given number of degrees of
C                                 freedom in VAL (ZERO.le.VAL.le.N-M).
C                        MD  < 0: It is assumed that the contents of
C                                 X, W, M, N, and WK have not been
C                                 modified since the previous invoca-
C                                 tion of GCVSPL. If MD < -1, WK(4)
C                                 is used as an initial estimate for
C                                 the smoothing parameter p.
C                       Other values for |MD|, and inappropriate values
C                       for VAL will result in an error condition, or
C                       cause a default value for VAL to be selected.
C                       After return from MD.ne.1, the same number of
C                       degrees of freedom can be obtained, for identical
C                       weight factors and knot positions, by selecting
C                       |MD|=1, and by copying the value of p from WK(4)
C                       into VAL. In this way, no iterative optimization
C                       is required when processing other data in Y.
C       VAL     ( I )   Mode value, as described above under MD.
C       C(NC,K) ( O )   Spline coefficients, to be used in conjunction
C                       with function SPLDER. NB: the dimensions of C
C                       in GCVSPL and in SPLDER are different! In SPLDER,
C                       only a single column of C(N,K) is needed, and the
C                       proper column C(1,J), with J=1...K should be used
C                       when calling SPLDER.
C       NC       ( I )  First dimension of array C(NC,K), NC.ge.N.
C       WK(IWK) (I/W/O) Work vector, with length IWK.ge.6*(N*M+1)+N.
C                       On normal exit, the first 6 values of WK are
C                       assigned as follows:
C
C                       WK(1) = Generalized Cross Validation value
C                       WK(2) = Mean Squared Residual.
C                       WK(3) = Estimate of the number of degrees of
C                               freedom of the residual sum of squares
C                               per dataset, with 0.lt.WK(3).lt.N-M.
C                       WK(4) = Smoothing parameter p, multiplicative
C                               with the splines' derivative constraint.
C                       WK(5) = Estimate of the true mean squared error
C                               (different formula for |MD| = 3).
C                       WK(6) = Gauss-Markov error variance.
C
C                       If WK(4) -->  0 , WK(3) -->  0 , and an inter-
C                       polating spline is fitted to the data (p --> 0).
C                       A very small value > 0 is used for p, in order
C                       to avoid division by zero in the GCV function.
C
C                       If WK(4) --> inf, WK(3) --> N-M, and a least-
C                       squares polynomial of order M (degree M-1) is
C                       fitted to the data (p --> inf). For numerical
C                       reasons, a very high value is used for p.
C
C                       Upon return, the contents of WK can be used for
C                       covariance propagation in terms of the matrices
C                       B and WE: see the source listings. The variance
C                       estimate for dataset J follows as WK(6)/WY(J).
C
C       IER     ( O )   Error parameter:
C
C                       IER = 0:        Normal exit
C                       IER = 1:        M.le.0 .or. N.lt.2*M
C                       IER = 2:        Knot sequence is not strictly
C                                       increasing, or some weight
C                                       factor is not positive.
C                       IER = 3:        Wrong mode  parameter or value.
C
C Remarks:
C *******
C
C       (1) GCVSPL calculates a natural spline of order 2*M (degree
C       2*M-1) which smoothes or interpolates a given set of data
C       points, using statistical considerations to determine the
C       amount of smoothing required (Craven & Wahba, 1979). If the
C       error variance is a priori known, it should be supplied to
C       the routine in VAL, for |MD|=3. The degree of smoothing is
C       then determined to minimize an unbiased estimate of the true
C       mean squared error. On the other hand, if the error variance
C       is not known, one may select |MD|=2. The routine then deter-
C       mines the degree of smoothing to minimize the generalized
C       cross validation function. This is asymptotically the same
C       as minimizing the true predicted mean squared error (Craven &
C       Wahba, 1979). If the estimates from |MD|=2 or 3 do not appear
C       suitable to the user (as apparent from the smoothness of the
C       M-th derivative or from the effective number of degrees of
C       freedom returned in WK(3) ), the user may select an other
C       value for the noise variance if |MD|=3, or a reasonably large
C       number of degrees of freedom if |MD|=4. If |MD|=1, the proce-
C       dure is non-iterative, and returns a spline for the given
C       value of the smoothing parameter p as entered in VAL.
C
C       (2) The number of arithmetic operations and the amount of
C       storage required are both proportional to N, so very large
C       datasets may be accomodated. The data points do not have
C       to be equidistant in the independant variable X or uniformly
C       weighted in the dependant variable Y. However, the data
C       points in X must be strictly increasing. Multiple dataset
C       processing (K.gt.1) is numerically more efficient dan
C       separate processing of the individual datasets (K.eq.1).
C
C       (3) If |MD|=3 (a priori known noise variance), any value of
C       N.ge.2*M is acceptable. However, it is advisable for N-2*M
C       be rather large (at least 20) if |MD|=2 (GCV).
C
C       (4) For |MD| > 1, GCVSPL tries to iteratively minimize the
C       selected criterion function. This minimum is unique for |MD|
C       = 4, but not necessarily for |MD| = 2 or 3. Consequently,
C       local optima rather that the global optimum might be found,
C       and some actual findings suggest that local optima might
C       yield more meaningful results than the global optimum if N
C       is small. Therefore, the user has some control over the
C       search procedure. If MD > 1, the iterative search starts
C       from a value which yields a number of degrees of freedom
C       which is approximately equal to N/2, until the first (local)
C       minimum is found via a golden section search procedure
C       (Utreras, 1980). If MD < -1, the value for p contained in
C       WK(4) is used instead. Thus, if MD = 2 or 3 yield too noisy
C       an estimate, the user might try |MD| = 1 or 4, for suitably
C       selected values for p or for the number of degrees of
C       freedom, and then run GCVSPL with MD = -2 or -3. The con-
C       tents of N, M, K, X, WX, WY, and WK are assumed unchanged
C       if MD < 0.
C
C       (5) GCVSPL calculates the spline coefficient array C(N,K);
C       this array can be used to calculate the spline function
C       value and any of its derivatives up to the degree 2*M-1
C       at any argument T within the knot range, using subrou-
C       tines SPLDER and SEARCH, and the knot array X(N). Since
C       the splines are constrained at their Mth derivative, only
C       the lower spline derivatives will tend to be reliable
C       estimates of the underlying, true signal derivatives.
C
C       (6) GCVSPL combines elements of subroutine CRVO5 by Utre-
C       ras (1980), subroutine SMOOTH by Lyche et al. (1983), and
C       subroutine CUBGCV by Hutchinson (1985). The trace of the
C       influence matrix is assessed in a similar way as described
C       by Hutchinson & de Hoog (1985). The major difference is
C       that the present approach utilizes non-symmetrical B-spline
C       design matrices as described by Lyche et al. (1983); there-
C       fore, the original algorithm by Erisman & Tinney (1975) has
C       been used, rather than the symmetrical version adopted by
C       Hutchinson & de Hoog.
C
C References:
C **********
C
C       P. Craven & G. Wahba (1979), Smoothing noisy data with
C       spline functions. Numerische Mathematik 31, 377-403.
C
C       A.M. Erisman & W.F. Tinney (1975), On computing certain
C       elements of the inverse of a sparse matrix. Communications
C       of the ACM 18(3), 177-179.
C
C       M.F. Hutchinson & F.R. de Hoog (1985), Smoothing noisy data
C       with spline functions. Numerische Mathematik 47(1), 99-106.
C
C       M.F. Hutchinson (1985), Subroutine CUBGCV. CSIRO Division of
C       Mathematics and Statistics, P.O. Box 1965, Canberra, ACT 2601,
C       Australia.
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori (1983), Fortran
C       subroutines for computing smoothing and interpolating natural
C       splines. Advances in Engineering Software 5(1), 2-5.
C
C       F. Utreras (1980), Un paquete de programas para ajustar curvas
C       mediante funciones spline. Informe Tecnico MA-80-B-209, Depar-
C       tamento de Matematicas, Faculdad de Ciencias Fisicas y Matema-
C       ticas, Universidad de Chile, Santiago.
C
C       Wahba, G. (1980). Numerical and statistical methods for mildly,
C       moderately and severely ill-posed problems with noisy data.
C       Technical report nr. 595 (February 1980). Department of Statis-
C       tics, University of Madison (WI), U.S.A.
C
C Subprograms required:
C ********************
C
C       BASIS, PREP, SPLC, BANDET, BANSOL, TRINV
C
C***********************************************************************
C
      SUBROUTINE GCVSPL ( X, Y, NY, WX, WY, M, N, K, MD, VAL, C, NC,
     1                   WK, IER )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      PARAMETER ( RATIO=2D0, TAU=1.618033983D0, IBWE=7,
     1           ZERO=0D0, HALF=5D-1 , ONE=1D0, TOL=1D-6,
     2           EPS=1D-15, EPSINV=ONE/EPS )
      DIMENSION X(N), Y(NY,K), WX(N), WY(K), C(NC,K), WK(N+6*(N*M+1))
      SAVE M2, NM1, EL
      DATA M2, NM1, EL / 2*0, 0D0 /
C
C***  Parameter check and work array initialization
C
      IER = 0
C***  Check on mode parameter
      IF ((IABS(MD).GT.4) .OR.(  MD.EQ. 0  ) .OR.
     1  ((IABS(MD).EQ.1).AND.( VAL.LT.ZERO)).OR.
     2  ((IABS(MD).EQ.3).AND.( VAL.LT.ZERO)).OR.
     3  ((IABS(MD).EQ.4).AND.((VAL.LT.ZERO) .OR.(VAL.GT.N-M)))) THEN
         IER = 3      !Wrong mode value
         RETURN
      ENDIF
C***  Check on M and N
      IF (MD.GT.0) THEN
         M2  = 2 * M
         NM1 = N - 1
      ELSE
         IF ((M2.NE.2*M).OR.(NM1.NE.N-1)) THEN
            IER = 3      !M or N modified since previous call
            RETURN
         ENDIF
      ENDIF
      IF ((M.LE.0).OR.(N.LT.M2)) THEN
         IER = 1      !M or N invalid
         RETURN
      ENDIF
C***  Check on knot sequence and weights
      IF (WX(1).LE.ZERO) IER = 2
      DO 10 I=2,N
         IF ((WX(I).LE.ZERO).OR.(X(I-1).GE.X(I))) IER = 2
         IF (IER.NE.0) RETURN
   10 CONTINUE
      DO 15 J=1,K
         IF (WY(J).LE.ZERO) IER = 2
         IF (IER.NE.0) RETURN
   15 CONTINUE
C
C***  Work array parameters (address information for covariance
C***  propagation by means of the matrices STAT, B, and WE). NB:
C***  BWE cannot be used since it is modified by function TRINV.
C
      NM2P1 = N*(M2+1)
      NM2M1 = N*(M2-1)
C     ISTAT = 1            !Statistics array STAT(6)
C     IBWE  = ISTAT + 6      !Smoothing matrix BWE( -M:M  ,N)
      IB    = IBWE  + NM2P1      !Design matrix    B  (1-M:M-1,N)
      IWE   = IB    + NM2M1      !Design matrix    WE ( -M:M  ,N)
C     IWK   = IWE   + NM2P1      !Total work array length N + 6*(N*M+1)
C
C***  Compute the design matrices B and WE, the ratio
C***  of their L1-norms, and check for iterative mode.
C
      IF (MD.GT.0) THEN
         CALL BASIS ( M, N, X, WK(IB), R1, WK(IBWE) )
         CALL PREP  ( M, N, X, WX, WK(IWE), EL )
         EL = EL / R1      !L1-norms ratio (SAVEd upon RETURN)
      ENDIF
      IF (IABS(MD).NE.1) GO TO 20
C***     Prior given value for p
         R1 = VAL
         GO TO 100
C
C***  Iterate to minimize the GCV function (|MD|=2),
C***  the MSE function (|MD|=3), or to obtain the prior
C***  given number of degrees of freedom (|MD|=4).
C
   20 IF (MD.LT.-1) THEN
         R1 = WK(4)      !User-determined starting value
      ELSE
         R1 = ONE / EL      !Default (DOF ~ 0.5)
      ENDIF
      R2 = R1 * RATIO
      GF2 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R2,EPS,C,NC,
     1          WK,WK(IB),WK(IWE),EL,WK(IBWE))
   40 GF1 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R1,EPS,C,NC,
     1          WK,WK(IB),WK(IWE),EL,WK(IBWE))
      IF (GF1.GT.GF2) GO TO 50
         IF (WK(4).LE.ZERO) GO TO 100            !Interpolation
         R2  = R1
         GF2 = GF1
         R1  = R1 / RATIO
         GO TO 40
   50 R3 = R2 * RATIO
   60 GF3 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R3,EPS,C,NC,
     1          WK,WK(IB),WK(IWE),EL,WK(IBWE))
      IF (GF3.GT.GF2) GO TO 70
         IF (WK(4).GE.EPSINV) GO TO 100      !Least-squares polynomial
         R2  = R3
         GF2 = GF3
         R3  = R3 * RATIO
         GO TO 60
   70 R2  = R3
      GF2 = GF3
      ALPHA = (R2-R1) / TAU
      R4 = R1 + ALPHA
      R3 = R2 - ALPHA
      GF3 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R3,EPS,C,NC,
     1          WK,WK(IB),WK(IWE),EL,WK(IBWE))
      GF4 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R4,EPS,C,NC,
     1          WK,WK(IB),WK(IWE),EL,WK(IBWE))
   80 IF (GF3.LE.GF4) THEN
         R2  = R4
         GF2 = GF4
         ERR = (R2-R1) / (R1+R2)
         IF ((ERR*ERR+ONE.EQ.ONE).OR.(ERR.LE.TOL)) GO TO 90
         R4  = R3
         GF4 = GF3
         ALPHA = ALPHA / TAU
         R3  = R2 - ALPHA
         GF3 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R3,EPS,C,NC,
     1             WK,WK(IB),WK(IWE),EL,WK(IBWE))
      ELSE
         R1  = R3
         GF1 = GF3
         ERR = (R2-R1) / (R1+R2)
         IF ((ERR*ERR+ONE.EQ.ONE).OR.(ERR.LE.TOL)) GO TO 90
         R3  = R4
         GF3 = GF4
         ALPHA = ALPHA / TAU
         R4 = R1 + ALPHA
         GF4 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R4,EPS,C,NC,
     1             WK,WK(IB),WK(IWE),EL,WK(IBWE))
      ENDIF
      GO TO 80
   90 R1 = HALF * (R1+R2)
C
C***  Calculate final spline coefficients
C
  100 GF1 = SPLC(M,N,K,Y,NY,WX,WY,MD,VAL,R1,EPS,C,NC,
     1          WK,WK(IB),WK(IWE),EL,WK(IBWE))
C
C***  Ready
C
      RETURN
      END
C BASIS.FOR, 1985-06-03
C
C***********************************************************************
C
C SUBROUTINE BASIS (REAL*8)
C
C Purpose:
C *******
C
C       Subroutine to assess a B-spline tableau, stored in vectorized
C       form.
C
C Calling convention:
C ******************
C
C       CALL BASIS ( M, N, X, B, BL, Q )
C
C Meaning of parameters:
C *********************
C
C       M               ( I )   Half order of the spline (degree 2*M-1),
C                               M > 0.
C       N               ( I )   Number of knots, N >= 2*M.
C       X(N)            ( I )   Knot sequence, X(I-1) < X(I), I=2,N.
C       B(1-M:M-1,N)    ( O )   Output tableau. Element B(J,I) of array
C                               B corresponds with element b(i,i+j) of
C                               the tableau matrix B.
C       BL              ( O )   L1-norm of B.
C       Q(1-M:M)        ( W )   Internal work array.
C
C Remark:
C ******
C
C       This subroutine is an adaptation of subroutine BASIS from the
C       paper by Lyche et al. (1983). No checking is performed on the
C       validity of M and N. If the knot sequence is not strictly in-
C       creasing, division by zero may occur.
C
C Reference:
C *********
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori, Fortran subroutines
C       for computing smoothing and interpolating natural splines.
C       Advances in Engineering Software 5(1983)1, pp. 2-5.
C
C***********************************************************************
C
      SUBROUTINE BASIS ( M, N, X, B, BL, Q )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      PARAMETER ( ZERO=0D0, ONE=1D0 )
      DIMENSION X(N), B(1-M:M-1,N), Q(1-M:M)
C
      IF (M.EQ.1) THEN
C***         Linear spline
         DO 3 I=1,N
            B(0,I) = ONE
    3    CONTINUE
         BL = ONE
         RETURN
      ENDIF
C
C***  General splines
C
      MM1 = M - 1
      MP1 = M + 1
      M2  = 2 * M
      DO 15 L=1,N
C***     1st row
         DO 5 J=-MM1,M
            Q(J) = ZERO
    5    CONTINUE
         Q(MM1) = ONE
         IF ((L.NE.1).AND.(L.NE.N))
     1      Q(MM1) = ONE / ( X(L+1) - X(L-1) )
C***     Successive rows
         ARG = X(L)
         DO 13 I=3,M2
            IR = MP1 - I
            V  = Q(IR)
            IF (L.LT.I) THEN
C***               Left-hand B-splines
               DO 6 J=L+1,I
                  U     = V
                  V     = Q(IR+1)
                  Q(IR) = U + (X(J)-ARG)*V
                  IR    = IR + 1
    6          CONTINUE
            ENDIF
            J1 = MAX0(L-I+1,1)
            J2 = MIN0(L-1,N-I)
            IF (J1.LE.J2) THEN
C***               Ordinary B-splines
               IF (I.LT.M2) THEN
                  DO 8 J=J1,J2
                     Y     = X(I+J)
                     U     = V
                     V     = Q(IR+1)
                     Q(IR) = U + (V-U)*(Y-ARG)/(Y-X(J))
                     IR = IR + 1
    8             CONTINUE
               ELSE
                  DO 10 J=J1,J2
                     U     = V
                     V     = Q(IR+1)
                     Q(IR) = (ARG-X(J))*U + (X(I+J)-ARG)*V
                     IR    = IR + 1
   10             CONTINUE
               ENDIF
            ENDIF
            NMIP1 = N - I + 1
            IF (NMIP1.LT.L) THEN
C***           Right-hand B-splines
               DO 12 J=NMIP1,L-1
                  U     = V
                  V     = Q(IR+1)
                  Q(IR) = (ARG-X(J))*U + V
                  IR    = IR + 1
   12          CONTINUE
            ENDIF
   13    CONTINUE
         DO 14 J=-MM1,MM1
            B(J,L) = Q(J)
   14    CONTINUE
   15 CONTINUE
C
C***  Zero unused parts of B
C
      DO 17 I=1,MM1
         DO 16 K=I,MM1
            B(-K,    I) = ZERO
            B( K,N+1-I) = ZERO
   16    CONTINUE
   17 CONTINUE
C
C***  Assess L1-norm of B
C
      BL = 0D0
      DO 19 I=1,N
         DO 18 K=-MM1,MM1
            BL = BL + ABS(B(K,I))
   18    CONTINUE
   19 CONTINUE
      BL = BL / N
C
C***  Ready
C
      RETURN
      END
C PREP.FOR, 1985-07-04
C
C***********************************************************************
C
C SUBROUTINE PREP (REAL*8)
C
C Purpose:
C *******
C
C       To compute the matrix WE of weighted divided difference coeffi-
C       cients needed to set up a linear system of equations for sol-
C       ving B-spline smoothing problems, and its L1-norm EL. The matrix
C       WE is stored in vectorized form.
C
C Calling convention:
C ******************
C
C       CALL PREP ( M, N, X, W, WE, EL )
C
C Meaning of parameters:
C *********************
C
C       M               ( I )   Half order of the B-spline (degree
C                               2*M-1), with M > 0.
C       N               ( I )   Number of knots, with N >= 2*M.
C       X(N)            ( I )   Strictly increasing knot array, with
C                               X(I-1) < X(I), I=2,N.
C       W(N)            ( I )   Weight matrix (diagonal), with
C                               W(I).gt.0.0, I=1,N.
C       WE(-M:M,N)      ( O )   Array containing the weighted divided
C                               difference terms in vectorized format.
C                               Element WE(J,I) of array E corresponds
C                               with element e(i,i+j) of the matrix
C                               W**-1 * E.
C       EL              ( O )   L1-norm of WE.
C
C Remark:
C ******
C
C       This subroutine is an adaptation of subroutine PREP from the paper
C       by Lyche et al. (1983). No checking is performed on the validity
C       of M and N. Division by zero may occur if the knot sequence is
C       not strictly increasing.
C
C Reference:
C *********
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori, Fortran subroutines
C       for computing smoothing and interpolating natural splines.
C       Advances in Engineering Software 5(1983)1, pp. 2-5.
C
C***********************************************************************
C
      SUBROUTINE PREP ( M, N, X, W, WE, EL )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      PARAMETER ( ZERO=0D0, ONE=1D0 )
      DIMENSION X(N), W(N), WE((2*M+1)*N)      !WE(-M:M,N)
C
C***  Calculate the factor F1
C
      M2   = 2 * M
      MP1  = M + 1
      M2M1 = M2 - 1
      M2P1 = M2 + 1
      NM   = N - M
      F1   = -ONE
      IF (M.NE.1) THEN
         DO 5 I=2,M
            F1 = -F1 * I
    5    CONTINUE
         DO 6 I=MP1,M2M1
            F1 = F1 * I
    6    CONTINUE
      END IF
C
C***  Columnwise evaluation of the unweighted design matrix E
C
      I1 = 1
      I2 = M
      JM = MP1
      DO 17 J=1,N
         INC = M2P1
         IF (J.GT.NM) THEN
            F1 = -F1
            F  =  F1
         ELSE
            IF (J.LT.MP1) THEN
                INC = 1
                F   = F1
            ELSE
                F   = F1 * (X(J+M)-X(J-M))
            END IF
         END IF
         IF ( J.GT.MP1) I1 = I1 + 1
         IF (I2.LT.  N) I2 = I2 + 1
         JJ = JM
C***     Loop for divided difference coefficients
         FF = F
         Y = X(I1)
         I1P1 = I1 + 1
         DO 11 I=I1P1,I2
            FF = FF / (Y-X(I))
   11    CONTINUE
         WE(JJ) = FF
         JJ = JJ + M2
         I2M1 = I2 - 1
         IF (I1P1.LE.I2M1) THEN
            DO 14 L=I1P1,I2M1
               FF = F
               Y  = X(L)
               DO 12 I=I1,L-1
                  FF = FF / (Y-X(I))
   12          CONTINUE
               DO 13 I=L+1,I2
                  FF = FF / (Y-X(I))
   13          CONTINUE
               WE(JJ) = FF
               JJ = JJ + M2
   14       CONTINUE
         END IF
         FF = F
         Y = X(I2)
         DO 16 I=I1,I2M1
            FF = FF / (Y-X(I))
   16    CONTINUE
         WE(JJ) = FF
         JJ = JJ + M2
         JM = JM + INC
   17 CONTINUE
C
C***  Zero the upper left and lower right corners of E
C
      KL = 1
      N2M = M2P1*N + 1
      DO 19 I=1,M
         KU = KL + M - I
         DO 18 K=KL,KU
            WE(    K) = ZERO
            WE(N2M-K) = ZERO
   18    CONTINUE
         KL = KL + M2P1
   19 CONTINUE
C
C***  Weighted matrix WE = W**-1 * E and its L1-norm
C
   20 JJ = 0
      EL = 0D0
      DO 22 I=1,N
         WI = W(I)
         DO 21 J=1,M2P1
            JJ     = JJ + 1
            WE(JJ) = WE(JJ) / WI
            EL     = EL + ABS(WE(JJ))
   21    CONTINUE
   22 CONTINUE
      EL = EL / N
C
C***  Ready
C
      RETURN
      END
C SPLC.FOR, 1985-12-12
C
C Author: H.J. Woltring
C
C Organizations: University of Nijmegen, and
C                Philips Medical Systems, Eindhoven
C                (The Netherlands)
C
C***********************************************************************
C
C FUNCTION SPLC (REAL*8)
C
C Purpose:
C *******
C
C       To assess the coefficients of a B-spline and various statistical
C       parameters, for a given value of the regularization parameter p.
C
C Calling convention:
C ******************
C
C       FV = SPLC ( M, N, K, Y, NY, WX, WY, MODE, VAL, P, EPS, C, NC,
C       1           STAT, B, WE, EL, BWE)
C
C Meaning of parameters:
C *********************
C
C       SPLC            ( O )   GCV function value if |MODE|.eq.2,
C                               MSE value if |MODE|.eq.3, and absolute
C                               difference with the prior given number of
C                               degrees of freedom if |MODE|.eq.4.
C       M               ( I )   Half order of the B-spline (degree 2*M-1),
C                               with M > 0.
C       N               ( I )   Number of observations, with N >= 2*M.
C       K               ( I )   Number of datasets, with K >= 1.
C       Y(NY,K)         ( I )   Observed measurements.
C       NY              ( I )   First dimension of Y(NY,K), with NY.ge.N.
C       WX(N)           ( I )   Weight factors, corresponding to the
C                               relative inverse variance of each measure-
C                               ment, with WX(I) > 0.0.
C       WY(K)           ( I )   Weight factors, corresponding to the
C                               relative inverse variance of each dataset,
C                               with WY(J) > 0.0.
C       MODE            ( I )   Mode switch, as described in GCVSPL.
C       VAL             ( I )   Prior variance if |MODE|.eq.3, and
C                               prior number of degrees of freedom if
C                               |MODE|.eq.4. For other values of MODE,
C                               VAL is not used.
C       P               ( I )   Smoothing parameter, with P >= 0.0. If
C                               P.eq.0.0, an interpolating spline is
C                               calculated.
C       EPS             ( I )   Relative rounding tolerance*10.0. EPS is
C                               the smallest positive number such that
C                               EPS/10.0 + 1.0 .ne. 1.0.
C       C(NC,K)         ( O )   Calculated spline coefficient arrays. NB:
C                               the dimensions of in GCVSPL and in SPLDER
C                               are different! In SPLDER, only a single
C                               column of C(N,K) is needed, and the proper
C                               column C(1,J), with J=1...K, should be used
C                               when calling SPLDER.
C       NC              ( I )   First dimension of C(NC,K), with NC.ge.N.
C       STAT(6)         ( O )   Statistics array. See the description in
C                               subroutine GCVSPL.
C       B (1-M:M-1,N)   ( I )   B-spline tableau as evaluated by subroutine
C                               BASIS.
C       WE( -M:M  ,N)   ( I )   Weighted B-spline tableau (W**-1 * E) as
C                               evaluated by subroutine PREP.
C       EL              ( I )   L1-norm of the matrix WE as evaluated by
C                               subroutine PREP.
C       BWE(-M:M,N)     ( O )   Central 2*M+1 bands of the inverted
C                               matrix ( B  +  p * W**-1 * E )**-1
C
C Remarks:
C *******
C
C       This subroutine combines elements of subroutine SPLC0 from the
C       paper by Lyche et al. (1983), and of subroutine SPFIT1 by
C       Hutchinson (1985).
C
C References:
C **********
C
C       M.F. Hutchinson (1985), Subroutine CUBGCV. CSIRO division of
C       Mathematics and Statistics, P.O. Box 1965, Canberra, ACT 2601,
C       Australia.
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori, Fortran subroutines
C       for computing smoothing and interpolating natural splines.
C       Advances in Engineering Software 5(1983)1, pp. 2-5.
C
C***********************************************************************
C
      FUNCTION SPLC( M, N, K, Y, NY, WX, WY, MODE, VAL, P, EPS, C, NC,
     1              STAT, B, WE, EL, BWE)
C
      IMPLICIT REAL*8 (A-H,O-Z)
      PARAMETER ( ZERO=0D0, ONE=1D0, TWO=2D0 )
      DIMENSION Y(NY,K), WX(N), WY(K), C(NC,K), STAT(6),
     1         B(1-M:M-1,N), WE(-M:M,N), BWE(-M:M,N)
C
C***  Check on p-value
C
      DP = P
      STAT(4) = P
      PEL = P * EL
C***  Pseudo-interpolation if p is too small
      IF (PEL.LT.EPS) THEN
         DP = EPS / EL
         STAT(4) = ZERO
      ENDIF
C***  Pseudo least-squares polynomial if p is too large
      IF (PEL*EPS.GT.ONE) THEN
         DP = ONE / (EL*EPS)
         STAT(4) = DP
      ENDIF
C
C***  Calculate  BWE  =  B  +  p * W**-1 * E
C
      DO 40 I=1,N
         KM = -MIN0(M,I-1)
         KP =  MIN0(M,N-I)
         DO 30 L=KM,KP
            IF (IABS(L).EQ.M) THEN
               BWE(L,I) =          DP * WE(L,I)
            ELSE
               BWE(L,I) = B(L,I) + DP * WE(L,I)
            ENDIF
   30    CONTINUE
   40 CONTINUE
C
C***  Solve BWE * C = Y, and assess TRACE [ B * BWE**-1 ]
C
      CALL BANDET ( BWE, M, N )
      CALL BANSOL ( BWE, Y, NY, C, NC, M, N, K )
      STAT(3) = TRINV ( WE, BWE, M, N ) * DP      !trace * p = res. d.o.f.
      TRN = STAT(3) / N
C
C***  Compute mean-squared weighted residual
C
      ESN = ZERO
      DO 70 J=1,K
         DO 60 I=1,N
            DT = -Y(I,J)
            KM = -MIN0(M-1,I-1)
            KP =  MIN0(M-1,N-I)
            DO 50 L=KM,KP
               DT = DT + B(L,I)*C(I+L,J)
   50       CONTINUE
            ESN = ESN + DT*DT*WX(I)*WY(J)
   60    CONTINUE
   70 CONTINUE
      ESN = ESN / (N*K)
C
C***  Calculate statistics and function value
C
      STAT(6) = ESN / TRN             !Estimated variance
      STAT(1) = STAT(6) / TRN         !GCV function value
      STAT(2) = ESN                   !Mean Squared Residual
C     STAT(3) = trace [p*B * BWE**-1] !Estimated residuals' d.o.f.
C     STAT(4) = P                     !Normalized smoothing factor
      IF (IABS(MODE).NE.3) THEN
C***     Unknown variance: GCV
         STAT(5) = STAT(6) - ESN
         IF (IABS(MODE).EQ.1) SPLC = ZERO
         IF (IABS(MODE).EQ.2) SPLC = STAT(1)
         IF (IABS(MODE).EQ.4) SPLC = DABS( STAT(3) - VAL )
      ELSE
C***     Known variance: estimated mean squared error
         STAT(5) = ESN - VAL*(TWO*TRN - ONE)
         SPLC = STAT(5)
      ENDIF
C
      RETURN
      END
C BANDET.FOR, 1985-06-03
C
C***********************************************************************
C
C SUBROUTINE BANDET (REAL*8)
C
C Purpose:
C *******
C
C       This subroutine computes the LU decomposition of an N*N matrix
C       E. It is assumed that E has M bands above and M bands below the
C       diagonal. The decomposition is returned in E. It is assumed that
C       E can be decomposed without pivoting. The matrix E is stored in
C       vectorized form in the array E(-M:M,N), where element E(J,I) of
C       the array E corresponds with element e(i,i+j) of the matrix E.
C
C Calling convention:
C ******************
C
C       CALL BANDET ( E, M, N )
C
C Meaning of parameters:
C *********************
C
C       E(-M:M,N)       (I/O)   Matrix to be decomposed.
C       M, N            ( I )   Matrix dimensioning parameters,
C                               M >= 0, N >= 2*M.
C
C Remark:
C ******
C
C       No checking on the validity of the input data is performed.
C       If (M.le.0), no action is taken.
C
C***********************************************************************
C
      SUBROUTINE BANDET ( E, M, N )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      DIMENSION E(-M:M,N)
C
      IF (M.LE.0) RETURN
      DO 40 I=1,N
         DI = E(0,I)
         MI = MIN0(M,I-1)
         IF (MI.GE.1) THEN
            DO 10 K=1,MI
               DI = DI - E(-K,I)*E(K,I-K)
   10       CONTINUE
            E(0,I) = DI
         ENDIF
         LM = MIN0(M,N-I)
         IF (LM.GE.1) THEN
            DO 30 L=1,LM
               DL = E(-L,I+L)
               KM = MIN0(M-L,I-1)
               IF (KM.GE.1) THEN
                  DU = E(L,I)
                  DO 20 K=1,KM
                     DU = DU - E(  -K,  I)*E(L+K,I-K)
                     DL = DL - E(-L-K,L+I)*E(  K,I-K)
   20             CONTINUE
                  E(L,I) = DU
               ENDIF
               E(-L,I+L) = DL / DI
   30       CONTINUE
         ENDIF
   40 CONTINUE
C
C***  Ready
C
      RETURN
      END
C BANSOL.FOR, 1985-12-12
C
C***********************************************************************
C
C SUBROUTINE BANSOL (REAL*8)
C
C Purpose:
C *******
C
C       This subroutine solves systems of linear equations given an LU
C       decomposition of the design matrix. Such a decomposition is pro-
C       vided by subroutine BANDET, in vectorized form. It is assumed
C       that the design matrix is not singular.
C
C Calling convention:
C ******************
C
C       CALL BANSOL ( E, Y, NY, C, NC, M, N, K )
C
C Meaning of parameters:
C *********************
C
C       E(-M:M,N)       ( I )   Input design matrix, in LU-decomposed,
C                               vectorized form. Element E(J,I) of the
C                               array E corresponds with element
C                               e(i,i+j) of the N*N design matrix E.
C       Y(NY,K)         ( I )   Right hand side vectors.
C       C(NC,K)         ( O )   Solution vectors.
C       NY, NC, M, N, K ( I )   Dimensioning parameters, with M >= 0,
C                               N > 2*M, and K >= 1.
C
C Remark:
C ******
C
C       This subroutine is an adaptation of subroutine BANSOL from the
C       paper by Lyche et al. (1983). No checking is performed on the
C       validity of the input parameters and data. Division by zero may
C       occur if the system is singular.
C
C Reference:
C *********
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori, Fortran subroutines
C       for computing smoothing and interpolating natural splines.
C       Advances in Engineering Software 5(1983)1, pp. 2-5.
C
C***********************************************************************
C
      SUBROUTINE BANSOL ( E, Y, NY, C, NC, M, N, K )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      DIMENSION E(-M:M,N), Y(NY,K), C(NC,K)
C
C***  Check on special cases: M=0, M=1, M>1
C
      NM1 = N - 1
      IF (M-1) 10,40,80
C
C***  M = 0: Diagonal system
C
   10 DO 30 I=1,N
         DO 20 J=1,K
            C(I,J) = Y(I,J) / E(0,I)
   20    CONTINUE
   30 CONTINUE
      RETURN
C
C***  M = 1: Tridiagonal system
C
   40 DO 70 J=1,K
         C(1,J) = Y(1,J)
         DO 50 I=2,N            !Forward sweep
            C(I,J) =  Y(I,J) - E(-1,I)*C(I-1,J)
   50      CONTINUE
         C(N,J) = C(N,J) / E(0,N)
         DO 60 I=NM1,1,-1      !Backward sweep
            C(I,J) = (C(I,J) - E( 1,I)*C(I+1,J)) / E(0,I)
   60    CONTINUE
   70 CONTINUE
      RETURN
C
C***  M > 1: General system
C
   80 DO 130 J=1,K
         C(1,J) = Y(1,J)
         DO 100 I=2,N            !Forward sweep
            MI = MIN0(M,I-1)
            D  = Y(I,J)
            DO 90 L=1,MI
               D = D - E(-L,I)*C(I-L,J)
   90       CONTINUE
            C(I,J) = D
  100    CONTINUE
         C(N,J) = C(N,J) / E(0,N)
         DO 120 I=NM1,1,-1      !Backward sweep
            MI = MIN0(M,N-I)
            D  = C(I,J)
            DO 110 L=1,MI
               D = D - E( L,I)*C(I+L,J)
  110       CONTINUE
            C(I,J) = D / E(0,I)
  120    CONTINUE
  130 CONTINUE
      RETURN
C
      END
C TRINV.FOR, 1985-06-03
C
C***********************************************************************
C
C FUNCTION TRINV (REAL*8)
C
C Purpose:
C *******
C
C       To calculate TRACE [ B * E**-1 ], where B and E are N * N
C       matrices with bandwidth 2*M+1, and where E is a regular matrix
C       in LU-decomposed form. B and E are stored in vectorized form,
C       compatible with subroutines BANDET and BANSOL.
C
C Calling convention:
C ******************
C
C       TRACE = TRINV ( B, E, M, N )
C
C Meaning of parameters:
C *********************
C
C       B(-M:M,N)       ( I ) Input array for matrix B. Element B(J,I)
C                             corresponds with element b(i,i+j) of the
C                             matrix B.
C       E(-M:M,N)       (I/O) Input array for matrix E. Element E(J,I)
C                             corresponds with element e(i,i+j) of the
C                             matrix E. This matrix is stored in LU-
C                             decomposed form, with L unit lower tri-
C                             angular, and U upper triangular. The unit
C                             diagonal of L is not stored. Upon return,
C                             the array E holds the central 2*M+1 bands
C                             of the inverse E**-1, in similar ordering.
C       M, N            ( I ) Array and matrix dimensioning parameters
C                             (M.gt.0, N.ge.2*M+1).
C       TRINV           ( O ) Output function value TRACE [ B * E**-1 ]
C
C Reference:
C *********
C
C       A.M. Erisman & W.F. Tinney, On computing certain elements of the
C       inverse of a sparse matrix. Communications of the ACM 18(1975),
C       nr. 3, pp. 177-179.
C
C***********************************************************************
C
      REAL*8 FUNCTION TRINV ( B, E, M, N )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      PARAMETER ( ZERO=0D0, ONE=1D0 )
      DIMENSION B(-M:M,N), E(-M:M,N)
C
C***  Assess central 2*M+1 bands of E**-1 and store in array E
C
      E(0,N) = ONE / E(0,N)      !Nth pivot
      DO 40 I=N-1,1,-1
         MI = MIN0(M,N-I)
         DD  = ONE / E(0,I)      !Ith pivot
C***     Save Ith column of L and Ith row of U, and normalize U row
         DO 10 K=1,MI
            E( K,N) = E( K,  I) * DD      !Ith row of U (normalized)
            E(-K,1) = E(-K,K+I)      !Ith column of L
   10    CONTINUE
         DD = DD + DD
C***     Invert around Ith pivot
         DO 30 J=MI,1,-1
            DU = ZERO
            DL = ZERO
            DO 20 K=1,MI
               DU = DU - E( K,N)*E(J-K,I+K)
               DL = DL - E(-K,1)*E(K-J,I+J)
   20       CONTINUE
            E( J,  I) = DU
            E(-J,J+I) = DL
            DD = DD - (E(J,N)*DL + E(-J,1)*DU)
   30    CONTINUE
         E(0,I) = 5D-1 * DD
   40 CONTINUE
C
C***  Assess TRACE [ B * E**-1 ] and clear working storage
C
      DD = ZERO
      DO 60 I=1,N
         MN = -MIN0(M,I-1)
         MP =  MIN0(M,N-I)
         DO 50 K=MN,MP
            DD = DD + B(K,I)*E(-K,K+I)
   50    CONTINUE
   60 CONTINUE
      TRINV = DD
      DO 70 K=1,M
         E( K,N) = ZERO
         E(-K,1) = ZERO
   70 CONTINUE
C
C***  Ready
C
      RETURN
      END
C SPLDER.FOR, 1985-06-11
C
C***********************************************************************
C
C FUNCTION SPLDER (REAL*8)
C
C Purpose:
C *******
C
C       To produce the value of the function (IDER.eq.0) or of the
C       IDERth derivative (IDER.gt.0) of a 2M-th order B-spline at
C       the point T. The spline is described in terms of the half
C       order M, the knot sequence X(N), N.ge.2*M, and the spline
C       coefficients C(N).
C
C Calling convention:
C ******************
C
C       SVIDER = SPLDER ( IDER, M, N, T, X, C, L, Q )
C
C Meaning of parameters:
C *********************
C
C       SPLDER  ( O )   Function or derivative value.
C       IDER    ( I )   Derivative order required, with 0.le.IDER
C                       and IDER.le.2*M. If IDER.eq.0, the function
C                       value is returned; otherwise, the IDER-th
C                       derivative of the spline is returned.
C       M       ( I )   Half order of the spline, with M.gt.0.
C       N       ( I )   Number of knots and spline coefficients,
C                       with N.ge.2*M.
C       T       ( I )   Argument at which the spline or its deri-
C                       vative is to be evaluated, with X(1).le.T
C                       and T.le.X(N).
C       X(N)    ( I )   Strictly increasing knot sequence array,
C                       X(I-1).lt.X(I), I=2,...,N.
C       C(N)    ( I )   Spline coefficients, as evaluated by
C                       subroutine GVCSPL.
C       L       (I/O)   L contains an integer such that:
C                       X(L).le.T and T.lt.X(L+1) if T is within
C                       the range X(1).le.T and T.lt.X(N). If
C                       T.lt.X(1), L is set to 0, and if T.ge.X(N),
C                       L is set to N. The search for L is facili-
C                       tated if L has approximately the right
C                       value on entry.
C       Q(2*M)  ( W )   Internal work array.
C
C Remark:
C ******
C
C       This subroutine is an adaptation of subroutine SPLDER of
C       the paper by Lyche et al. (1983). No checking is performed
C       on the validity of the input parameters.
C
C Reference:
C *********
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori, Fortran subroutines
C       for computing smoothing and interpolating natural splines.
C       Advances in Engineering Software 5(1983)1, pp. 2-5.
C
C***********************************************************************
C
      REAL*8 FUNCTION SPLDER ( IDER, M, N, T, X, C, L, Q )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      PARAMETER ( ZERO=0D0, ONE=1D0 )
      DIMENSION X(N), C(N), Q(2*M)
C
C***  Derivatives of IDER.ge.2*M are alway zero
C
      M2 =  2 * M
      K  = M2 - IDER
      IF (K.LT.1) THEN
         SPLDER = ZERO
         RETURN
      ENDIF
C
C***  Search for the interval value L
C
      CALL SEARCH ( N, X, T, L )
C
C***  Initialize parameters and the 1st row of the B-spline
C***  coefficients tableau
C
      TT   = T
      MP1  =  M + 1
      NPM  =  N + M
      M2M1 = M2 - 1
      K1   =  K - 1
      NK   =  N - K
      LK   =  L - K
      LK1  = LK + 1
      LM   =  L - M
      JL   =  L + 1
      JU   =  L + M2
      II   =  N - M2
      ML   = -L
      DO 2 J=JL,JU
         IF ((J.GE.MP1).AND.(J.LE.NPM)) THEN
            Q(J+ML) = C(J-M)
         ELSE
            Q(J+ML) = ZERO
         ENDIF
    2 CONTINUE
C
C***  The following loop computes differences of the B-spline
C***  coefficients. If the value of the spline is required,
C***  differencing is not necessary.
C
      IF (IDER.GT.0) THEN
         JL = JL - M2
         ML = ML + M2
         DO 6 I=1,IDER
            JL = JL + 1
            II = II + 1
            J1 = MAX0(1,JL)
            J2 = MIN0(L,II)
            MI = M2 - I
            J  = J2 + 1
            IF (J1.LE.J2) THEN
               DO 3 JIN=J1,J2
                  J  =  J - 1
                  JM = ML + J
                  Q(JM) = (Q(JM) - Q(JM-1)) / (X(J+MI) - X(J))
    3          CONTINUE
            ENDIF
            IF (JL.GE.1) GO TO 6
               I1 =  I + 1
               J  = ML + 1
               IF (I1.LE.ML) THEN
                  DO 5 JIN=I1,ML
                     J    =  J - 1
                     Q(J) = -Q(J-1)
    5             CONTINUE
               ENDIF
    6    CONTINUE
         DO 7 J=1,K
            Q(J) = Q(J+IDER)
    7    CONTINUE
      ENDIF
C
C***  Compute lower half of the evaluation tableau
C
      IF (K1.GE.1) THEN      !Tableau ready if IDER.eq.2*M-1
         DO 14 I=1,K1
            NKI  =  NK + I
            IR   =   K
            JJ   =   L
            KI   =   K - I
            NKI1 = NKI + 1
C***        Right-hand B-splines
            IF (L.GE.NKI1) THEN
               DO 9 J=NKI1,L
                  Q(IR) = Q(IR-1) + (TT-X(JJ))*Q(IR)
                  JJ    = JJ - 1
                  IR    = IR - 1
    9          CONTINUE
            ENDIF
C***        Middle B-splines
            LK1I = LK1 + I
            J1 = MAX0(1,LK1I)
            J2 = MIN0(L, NKI)
            IF (J1.LE.J2) THEN
               DO 11 J=J1,J2
                  XJKI  = X(JJ+KI)
                  Z     = Q(IR)
                  Q(IR) = Z + (XJKI-TT)*(Q(IR-1)-Z)/(XJKI-X(JJ))
                  IR    = IR - 1
                  JJ    = JJ - 1
   11          CONTINUE
            ENDIF
C***        Left-hand B-splines
            IF (LK1I.LE.0) THEN
               JJ    = KI
               LK1I1 =  1 - LK1I
               DO 13 J=1,LK1I1
                  Q(IR) = Q(IR) + (X(JJ)-TT)*Q(IR-1)
                  JJ    = JJ - 1
                  IR    = IR - 1
   13          CONTINUE
            ENDIF
   14    CONTINUE
      ENDIF
C
C***  Compute the return value
C
      Z = Q(K)
C***  Multiply with factorial if IDER.gt.0
      IF (IDER.GT.0) THEN
         DO 16 J=K,M2M1
            Z = Z * J
   16    CONTINUE
      ENDIF
      SPLDER = Z
C
C***  Ready
C


      RETURN
      END
C SEARCH.FOR, 1985-06-03
C
C***********************************************************************
C
C SUBROUTINE SEARCH (REAL*8)
C
C Purpose:
C *******
C
C       Given a strictly increasing knot sequence X(1) < ... < X(N),
C       where N >= 1, and a real number T, this subroutine finds the
C       value L such that X(L) <= T < X(L+1).  If T < X(1), L = 0;
C       if X(N) <= T, L = N.
C
C Calling convention:
C ******************
C
C       CALL SEARCH ( N, X, T, L )
C
C Meaning of parameters:
C *********************
C
C       N       ( I )   Knot array dimensioning parameter.
C       X(N)    ( I )   Stricly increasing knot array.
C       T       ( I )   Input argument whose knot interval is to
C                       be found.
C       L       (I/O)   Knot interval parameter. The search procedure
C                       is facilitated if L has approximately the
C                       right value on entry.
C
C Remark:
C ******
C
C       This subroutine is an adaptation of subroutine SEARCH from
C       the paper by Lyche et al. (1983). No checking is performed
C       on the input parameters and data; the algorithm may fail if
C       the input sequence is not strictly increasing.
C
C Reference:
C *********
C
C       T. Lyche, L.L. Schumaker, & K. Sepehrnoori, Fortran subroutines
C       for computing smoothing and interpolating natural splines.
C       Advances in Engineering Software 5(1983)1, pp. 2-5.
C
C***********************************************************************
C
      SUBROUTINE SEARCH ( N, X, T, L )
C
      IMPLICIT REAL*8 (A-H,O-Z)
      DIMENSION X(N)
C
      IF (T.LT.X(1)) THEN
C***     Out of range to the left
         L = 0
         RETURN
      ENDIF
      IF (T.GE.X(N)) THEN
C***     Out of range to the right
         L = N
         RETURN
      ENDIF
C***  Validate input value of L
      L = MAX0(L,1)
      IF (L.GE.N) L = N-1
C
C***  Often L will be in an interval adjoining the interval found
C***  in a previous call to search
C
      IF (T.GE.X(L)) GO TO 5
      L = L - 1
      IF (T.GE.X(L)) RETURN
C
C***  Perform bisection
C
      IL = 1
    3 IU = L
    4 L = (IL+IU) / 2
      IF (IU-IL.LE.1) RETURN
      IF (T.LT.X(L)) GO TO 3
      IL = L
      GO TO 4
    5 IF (T.LT.X(L+1)) RETURN
      L = L + 1
      IF (T.LT.X(L+1)) RETURN
      IL = L + 1
      IU = N
      GO TO 4
C
      END
