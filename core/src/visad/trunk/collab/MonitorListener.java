/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad.collab;

import java.rmi.RemoteException;

import java.util.Iterator;

import visad.util.ObjectCache;

class MonitorListener
  extends EventListener
{
  private DisplayMonitorListener listener;
  private int id;

  private EventCache cache;

  MonitorListener(String name, DisplayMonitorListener listener, int id)
  {
    super(name + ":MonL");

    this.listener = listener;
    this.id = id;

    cache = new EventCache(name + ":Cache#" + id);
  }

  EventTable getNewEventTable() { return new MonitorEventTable(Name); }

  /**
   * Attempt to deliver the queued events.
   *
   * @exception RemoteException If a connection could not be made to the
   * 					remote listener.
   */
  void deliverEventTable(EventTable tbl)
    throws RemoteException
  {
    MonitorEventTable mTable = (MonitorEventTable )tbl;

    // deliver events
    Iterator iter = mTable.keyIterator();
    while (iter.hasNext()) {
      Object key = iter.next();

      MonitorEvent evt = (MonitorEvent )mTable.remove(key);

      MonitorEvent e2 = (MonitorEvent )evt.clone();
      e2.setOriginator(id);

      try {
        listener.stateChanged(e2);
      } catch (RemoteException re) {
        // restore failed event to table
        mTable.restore(key, evt);
        // let caller handle RemoteExceptions
        throw re;
      } catch (Throwable t) {
        // whine loudly about all other Exceptions
        t.printStackTrace();
      }
    }
  }

  /**
   * Check to see if this listener has already seen the event.
   *
   * @param evt The event to examine.
   *
   * @return <TT>true</TT> if the event has been seen.
   */
  boolean eventSeen(MonitorEvent evt)
  {
    // if we've already delivered this object, ignore it
    if (cache.isCached(evt)) {
      return true;
    }

    // remember this event for future reference
    cache.add(evt);
    return false;
  }

  /**
   * Get the unique identifier.
   *
   * @return the unique identifier.
   */
  int getID() { return id; }

  /**
   * Get the <TT>DisplayMonitorListener</TT>.
   *
   * @return the listener.
   */
  DisplayMonitorListener getListener() { return listener; }

  private String description = null;

  public String toString()
  {
    if (description == null) {
      StringBuffer buf = new StringBuffer("MonitorListener[");
      buf.append(Name);
      buf.append("=#");
      buf.append(id);
      buf.append(']');
      description = buf.toString();
    }
    return description;
  }

  /**
   * Cache to track recently seen events
   */
  class EventCache
  {
    String name;
    ObjectCache cache;

    public EventCache(String name)
    {
      this.name = name;
      cache = new ObjectCache(name);
    }

    public void add(MonitorEvent evt)
    {
      // HACK ALERT!!!
      //  If this is a MAPS_CLEARED event, delete all added map events
      //  If this is a MAP_ADDED event, delete any clear events
      switch (evt.getType()) {
      case MonitorEvent.MAPS_CLEARED:
        deleteEvents(MonitorEvent.MAP_ADDED);
        break;
      case MonitorEvent.MAP_ADDED:
        deleteEvents(MonitorEvent.MAPS_CLEARED);
        break;
      }
      Object obj = getClonedObject(evt);
      cache.add(obj);
    }

    private void deleteEvents(int type)
    {
      Iterator iter = cache.keys();
      while (iter.hasNext()) {
        Object key = iter.next();
        Object obj = cache.get(key);
        if (obj instanceof MonitorEvent) {
          MonitorEvent evt = (MonitorEvent )obj;
          if (evt.getType() == type) {
            cache.remove(key);
          }
        }
      }
    }

    private Object getClonedObject(MonitorEvent evt)
    {
      if (evt instanceof ControlMonitorEvent) {
        return ((ControlMonitorEvent )evt).getControl().clone();
      }

      return evt.clone();
    }

    private Object getObject(MonitorEvent evt)
    {
      if (evt instanceof ControlMonitorEvent) {
        return ((ControlMonitorEvent )evt).getControl();
      }

      return evt;
    }

    public boolean isCached(MonitorEvent evt)
    {
      Object obj = getObject(evt);
      if (obj == null) {
        // assume other side hasn't seen null events
        return false;
      }

      return cache.isCached(obj);
    }
  }
}
