//
// UserDisplayRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.cluster;

import visad.*;
import visad.java3d.*;

import java.awt.event.*;

import java.util.*;


/**
 * <CODE>UserDisplayRendererJ3D</CODE> is the DisplayRenderer
 * for remote users connecting to a cluster via a proxy on the
 * client.<P>
 */
public class UserDisplayRendererJ3D extends DefaultDisplayRendererJ3D {

  RemoteProxyAgent agent = null;

  long time_out = 10000;

  public UserDisplayRendererJ3D (RemoteProxyAgent a, long to) {
    super();
    agent = a;
    time_out = to;
  }

  public DataRenderer makeDefaultRenderer() {
    return new UserRendererJ3D(agent, time_out);
  }

  public boolean legalDataRenderer(DataRenderer renderer) {
    return (renderer instanceof UserRendererJ3D);
  }

}

