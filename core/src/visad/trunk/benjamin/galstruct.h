c     --------------------------------
c     Galactic structure parameters
c     --------------------------------
      character*5 plab, phaselab, gravlab
      character*4 outlab
      common /gal0/  iden(NPHASE),ipres(NFORCE),igrav(NGRAV),irp(NRP),
     >               iff(NFF),iab(NAB)
      common /gal1/  idfcn(NPHASE,NDFCN),ipfcn(NFORCE,NPFCN),
     >               igfcn(NGRAV,NGFCN)
      common /gal2/  dpars(NPHASE,NDFCN,NDPAR),
     >               ppars(NFORCE,NPFCN,NPPAR),
     >               gpars(NGRAV,NGFCN,NGPAR),
     >               rppars(NRP,NRPPAR),
     >               ffpars(NFF,NFFPAR),
     >               abpars(NAB,NABPAR)
      common /gal3/  iout(NNOUT),ipout(NNOUT)
      common /gal4/  xslice(NSLICE),islice(NSLICE)
      common /gal5/  grinfo(5),igrinfo(2)
      common /gallab/ plab(NFORCE),phaselab(NPHASE),
     >                gravlab(NGRAV),outlab(NNOUT)
      common/galwim/ tcpars(3,5)




