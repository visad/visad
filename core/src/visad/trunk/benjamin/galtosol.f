
      subroutine galtosol(X,Y,Z,LBD)
c     Convert galactic X,Y,Z to sun_centered l,b,distance
c     Sun is at X=0, y=+8.5, Z=0

      parameter(XSUN=0.,YSUN=8.5,ZSUN=0.)
      parameter( PI=3.14159265359 )
      real LBD(3)

      dist = sqrt(X*X+(Y-8.5)*(Y-8.5)+Z*Z)
      if (dist.eq.0) then
        print *, 'Cant give l/b for R=Rsun!'
        stop
      end if

      gb = asin(Z/dist)/PI*180
      if (Y.ne.8.5) then
        gl = atan(X/(8.5-Y))/PI*180
        if ((8.5-Y).lt.0) then
          gl = gl + 180
        else if ( (X.lt.0).and.( (8.5-Y).gt.0)) then
          gl = 360 + gl
        end if
      else if (X.gt.0) then
      gl = 90
      else if (X.lt.0) then
      gl = 270
      end if
      LBD(1) = gl
      LBD(2) = gb
      LBD(3) = dist
      return
      end
