//
// DefaultNodeRendererAgent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.*;
import java.io.Serializable;

/**
   DefaultNodeRendererAgent is the abstract super-class for agents sent from
   client to nodes.<P>
*/
public class DefaultNodeRendererAgent extends NodeAgent {

  private RemoteNodeDataImpl data = null;

  private DisplayImplJ3D display = null;
  private NodeDisplayRendererJ3D ndr = null;
  private DataReferenceImpl ref = null;
  private NodeRendererJ3D nr = null;
  private ConstantMap[] cmaps = null;
  private ScalarMap[] smaps = null;
  private String[] controlSaves = null;
  private String projectionControlSave = null;
  private String graphicsModeControlSave = null;
  private String rendererControlSave = null;

  public DefaultNodeRendererAgent(RemoteClientAgent source) {
    super(source);
  }

  public void run() {
    Object o = (RemoteNodeDataImpl) getObject();
    if (o == null || !(o instanceof RemoteNodeDataImpl)) {
      System.out.println("DefaultNodeRendererAgent cannot run: " +
                         "object must be RemoteNodeDataImpl " + o);
      return;
    }
    data = (RemoteNodeDataImpl) o;

    try {
      ndr = new NodeDisplayRendererJ3D();
      display = new DisplayImplJ3D("dummy", ndr, DisplayImplJ3D.TRANSFORM_ONLY);

// must get ConstantMaps, ScalarMaps and Controls from client
      if (smaps == null || smaps.length == 0 ||
          controlSaves == null || controlSaves.length != smaps.length) {
        System.out.println("DefaultNodeRendererAgent cannot run: " +
                           "ScalarMap[] array is empty");
        return;
      }
      for (int i=0; i<smaps.length; i++) {
        display.addMap(smaps[i]);
        smaps[i].getControl().setSaveString(controlSaves[i]);
      }
      display.getProjectionControl().setSaveString(projectionControlSave);
      display.getGraphicsModeControl().setSaveString(graphicsModeControlSave);
      ndr.getRendererControl().setSaveString(rendererControlSave);

      ref = new DataReferenceImpl("dummy");
      ref.setData(data);
      nr = new NodeRendererJ3D(this);
      display.addReferences(nr, ref, cmaps);
    }
    catch (VisADException e) {
      System.out.println("DefaultNodeRendererAgent cannot run: " + e.toString());
      return;
    }
    catch (RemoteException e) {
      System.out.println("DefaultNodeRendererAgent cannot run: " + e.toString());
      return;
    }

    Thread me = Thread.currentThread();
    while (getAgentThread() == me) {



    }
  }

}

