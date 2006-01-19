//
// RemoteDisplayImpl.java
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

package visad;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.*;
import java.rmi.*;

import visad.collab.DisplayMonitor;
import visad.collab.DisplayMonitorImpl;
import visad.collab.DisplaySync;
import visad.collab.DisplaySyncImpl;
import visad.collab.RemoteDisplayMonitor;
import visad.collab.RemoteDisplayMonitorImpl;
import visad.collab.RemoteDisplaySync;
import visad.collab.RemoteDisplaySyncImpl;

/**
   RemoteDisplayImpl is the VisAD class for remote access to
   Display-s.<P>
*/
public class RemoteDisplayImpl extends RemoteActionImpl
       implements RemoteDisplay {
  // and RemoteActionImpl extends UnicastRemoteObject

  public RemoteDisplayImpl(DisplayImpl d) throws RemoteException {
    super(d);
  }


  // CTR - begin code for slaved displays

  /** links a slave display to this display */
  public void addSlave(RemoteSlaveDisplay display)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new VisADException("RemoteDisplayImpl.addSlave(): " +
                               "AdaptedAction is null");
    }
    if (!(AdaptedAction instanceof DisplayImpl)) {
      throw new VisADException("RemoteDisplayImpl.addSlave(): " +
                               "AdaptedAction must be DisplayImpl");
    }
    DisplayImpl d = (DisplayImpl) AdaptedAction;
    ((DisplayImpl) AdaptedAction).addSlave(display);
  }

  /** removes a link between a slave display and this display */
  public void removeSlave(RemoteSlaveDisplay display)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new VisADException("RemoteDisplayImpl.removeSlave(): " +
                               "AdaptedAction is null");
    }
    if (!(AdaptedAction instanceof DisplayImpl)) {
      throw new VisADException("RemoteDisplayImpl.removeSlave(): " +
                               "AdaptedAction must be DisplayImpl");
    }
    DisplayImpl d = (DisplayImpl) AdaptedAction;
    d.removeSlave(display);
  }

  /** removes all links between slave displays and this display */
  public void removeAllSlaves() throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new VisADException("RemoteDisplayImpl.removeAllSlaves(): " +
                               "AdaptedAction is null");
    }
    if (!(AdaptedAction instanceof DisplayImpl)) {
      throw new VisADException("RemoteDisplayImpl.removeAllSlaves(): " +
                               "AdaptedAction must be DisplayImpl");
    }
    DisplayImpl d = (DisplayImpl) AdaptedAction;
    d.removeAllSlaves();
  }

  /** whether there are any slave displays linked to this display */
  public boolean hasSlaves() throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new VisADException("RemoteDisplayImpl.hasSlaves(): " +
                               "AdaptedAction is null");
    }
    if (!(AdaptedAction instanceof DisplayImpl)) {
      throw new VisADException("RemoteDisplayImpl.removeAllSlaves(): " +
                               "AdaptedAction must be DisplayImpl");
    }
    DisplayImpl d = (DisplayImpl) AdaptedAction;
    return d.hasSlaves();
  }

  /** sends a mouse event to this remote display's associated display */
  public void sendMouseEvent(MouseEvent e)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new VisADException("RemoteDisplayImpl.sendMouseEvent(): " +
                               "AdaptedAction is null");
    }
    if (!(AdaptedAction instanceof DisplayImpl)) {
      throw new VisADException("RemoteDisplayImpl.sendMouseEvent(): " +
                               "AdaptedAction must be DisplayImpl");
    }
    DisplayImpl d = (DisplayImpl) AdaptedAction;
    MouseBehavior mb = d.getMouseBehavior();
    if (mb == null) {
      throw new VisADException("RemoteDisplayImpl.sendMouseEvent(): " +
                               "MouseBehavior is null");
    }
    MouseHelper mh = mb.getMouseHelper();
    if (mh == null) {
      throw new VisADException("RemoteDisplayImpl.sendMouseEvent(): " +
                               "MouseHelper is null");
    }

    // tweak MouseEvent to have proper associated Component
    Component c = d.getComponent();
    int id = e.getID();
    long when = e.getWhen();
    int mods = e.getModifiers();
    int x = e.getX();
    int y = e.getY();
    int clicks = e.getClickCount();
    boolean popup = e.isPopupTrigger();
    MouseEvent ne = new MouseEvent(c, id, when, mods, x, y, clicks, popup);

    // send mouse event with remote source flag set
    mh.processEvent(ne, VisADEvent.UNKNOWN_REMOTE_SOURCE);
  }

  // CTR - end code for slaved displays


  /** link ref to this Display; this method may only be invoked
      after all links to ScalarMaps have been made */
  public void addReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof DataReference)) {
      throw new ReferenceException("RemoteDisplayImpl.addReference: ref " +
                                   "must be DataReference");
    }
    addReference((DataReference) ref, null);
  }

  /** link ref to this Display; must be RemoteDataReference; this
      method may only be invoked after all links to ScalarMaps have
      been made; the ConstantMap array applies only to rendering ref */
  public void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException {
    if (!(ref instanceof RemoteDataReference)) {
      throw new RemoteVisADException("RemoteDisplayImpl.addReference: requires " +
                                     "RemoteDataReference");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.addReference: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).adaptedAddReference(
                     (RemoteDataReference) ref, (RemoteDisplay) this,
                     constant_maps);
  }

  /** link ref to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      this is a method of DisplayImpl and RemoteDisplayImpl rather
      than Display - see Section 6.1 of the Developer's Guide for
      more information */
  public void addReferences(DataRenderer renderer, DataReference ref)
         throws VisADException, RemoteException {
    addReferences(renderer, new DataReference[] {ref}, null);
  }

  /** link ref to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      the maps array applies only to rendering ref;
      this is a method of DisplayImpl and RemoteDisplayImpl rather
      than Display - see Section 6.1 of the Developer's Guide for
      more information */
  public void addReferences(DataRenderer renderer, DataReference ref,
                            ConstantMap[] constant_maps)
         throws VisADException, RemoteException {
    addReferences(renderer, new DataReference[] {ref},
                  new ConstantMap[][] {constant_maps});
  }

  /** link refs to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made; this is a method of DisplayImpl and
      RemoteDisplayImpl rather than Display - see Section 6.1 of the
      Developer's Guide for more information */
  public void addReferences(DataRenderer renderer, DataReference[] refs)
         throws VisADException, RemoteException {
    addReferences(renderer, refs, null);
  }

  /** link refs to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      the maps[i] array applies only to rendering refs[i];
      this is a method of DisplayImpl and RemoteDisplayImpl rather
      than Display - see Section 6.1 of the Developer's Guide for
      more information */
  public void addReferences(DataRenderer renderer, DataReference[] refs,
         ConstantMap[][] constant_maps) throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.addReferences: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).adaptedAddReferences(renderer, refs,
                     (RemoteDisplay) this, constant_maps);
  }

  /** remove link to a DataReference;
      because DataReference array input to adaptedAddReferences may be a
      mix of local and remote, we tolerate either here */
  public void removeReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof DataReference)) {
      throw new ReferenceException("RemoteDisplayImpl.addReference: ref " +
                                   "must be DataReference");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.removeReference: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).adaptedDisplayRemoveReference((DataReference) ref);
  }

  /** add a ScalarMap to this Display */
  public void addMap(ScalarMap map)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.addMap: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).addMap(map);
  }

  /** remove a ScalarMap from this Display */
  public void removeMap(ScalarMap map)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.removeMap: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).removeMap(map);
  }

  /** clear set of ScalarMap-s associated with this display */
  public void clearMaps() throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.clearMaps: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).clearMaps();
  }

  /** destroy this display */
  public void destroy() throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.destroy: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).destroy();
  }

  public String getDisplayClassName() throws RemoteException {
    return AdaptedAction.getClass().getName();
  }

  public int getDisplayAPI() throws RemoteException, VisADException {
    return ((DisplayImpl) AdaptedAction).getAPI();
  }

  public String getDisplayRendererClassName() throws RemoteException {
    DisplayRenderer dr = ((DisplayImpl )AdaptedAction).getDisplayRenderer();
    return dr.getClass().getName();
  }

  public Vector getMapVector()
	throws VisADException, RemoteException
  {
    if (AdaptedAction == null) {
      throw new RemoteVisADException(getClass().getName() + ".getMapVector: " +
                                     "AdaptedAction is null");
    }

    return ((DisplayImpl) AdaptedAction).getMapVector();
  }


  public Vector getConstantMapVector()
	throws VisADException, RemoteException
  {
    if (AdaptedAction == null) {
      throw new RemoteVisADException(getClass().getName() + ".getConstantMapVector: " +
                                     "AdaptedAction is null");
    }

    return ((DisplayImpl) AdaptedAction).getConstantMapVector();
  }

  public GraphicsModeControl getGraphicsModeControl()
	throws VisADException, RemoteException
  {
    if (AdaptedAction == null) {
      throw new RemoteVisADException(getClass().getName() + ".getGraphicsModeControl: " +
                                     "AdaptedAction is null");
    }

    return ((DisplayImpl) AdaptedAction).getGraphicsModeControl();
  }

  public Vector getReferenceLinks()
	throws VisADException, RemoteException
  {
    Vector links = new Vector();

    Vector rv = ((DisplayImpl )AdaptedAction).getRenderers();
    Enumeration e = rv.elements();
    while (e.hasMoreElements()) {
      DataRenderer dr = (DataRenderer )e.nextElement();

      DataDisplayLink[] dl = dr.getLinks();
      if (dl != null) {
	for (int i = 0; i < dl.length; i++) {
          try {
            links.addElement(new RemoteReferenceLinkImpl(dl[i]));
          } catch (RemoteException re) {
            // skip remote links
          }
        }
      }
    }

    return links;
  }

  public RemoteDisplayMonitor getRemoteDisplayMonitor()
        throws RemoteException
  {
    DisplayMonitor dpyMon = ((DisplayImpl )AdaptedAction).getDisplayMonitor();
    return new RemoteDisplayMonitorImpl((DisplayMonitorImpl )dpyMon);
  }

  /**
   * Returns a remotely-usable wrapper for the associated Display's
   * synchronization object.
   *
   */
  public DisplaySync getDisplaySync()
        throws RemoteException
  {
    return ((DisplayImpl )AdaptedAction).getDisplaySync();
  }

  /**
   * Returns a remotely-usable wrapper for the associated Display's
   * synchronization object.
   */
  public RemoteDisplaySync getRemoteDisplaySync()
        throws RemoteException
  {
    DisplaySync dpySync = ((DisplayImpl )AdaptedAction).getDisplaySync();
    return new RemoteDisplaySyncImpl((DisplaySyncImpl )dpySync);
  }

  /**
   * Send a message to all </tt>MessageListener</tt>s.
   *
   * @param msg Message being sent.
   */
  public void sendMessage(MessageEvent msg)
    throws RemoteException
  {
    ((DisplayImpl )AdaptedAction).sendMessage(msg);
  }
}
