
//
// SSMethod.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.ss;

import java.lang.reflect.Method;
import visad.ThingImpl;

/** Thing wrapper for java.lang.reflect.Method.<P> */
public class SSMethod extends ThingImpl {

  private Method method;

  /** constructor */
  public SSMethod(Method rt) {
    method = rt;
  }

  /** return the wrapper's Method */
  public Method getMethod() {
    return method;
  }

}

