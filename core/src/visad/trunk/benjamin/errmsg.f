c============================================================================
      subroutine errmsg(ierr)
c============================================================================
      print *, '---error---error---error---error---error---error'
      if(ierr.eq.1) then
         print *, 'Need to readjust value of NEL, NOTHER, or NTOT in'
         print *, 'parameter statements (check *.h files)!'
      else if (ierr.eq.2) then
         print *, 'Number of ions or temperature points in rates.table'
         print *, 'not = to NIONS or NTEMP in *.h file!'
      else if (ierr.eq.3) then
	 print *, 'Temperature lies out of array bounds.'
      else if (ierr.eq.4) then
	 print *, 'Temperature arrays not spaced uniformly in log T'
      else if (ierr.eq.5) then
         print *, 'Exceeded MAXIT iterations in equil'
      else if (ierr.eq.6) then
	 print *, 'Need to increase NWORK in balance'
      else if (ierr.eq.7) then
         print *, 'Photoionization cross section is nonzero below'
         print *, 'ionization threshhold in subroutine heating.'
      else if (ierr.eq.8) then
         print *, 'Exceeded maximum iterations in THBAL'
      end if
      print *, '---error---error---error---error---error---error'
      stop
      end
c============================================================================
