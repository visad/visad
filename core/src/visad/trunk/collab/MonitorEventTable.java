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

package visad.collab;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import visad.Control;

public class MonitorEventTable
  implements EventTable
{
  private String Name;
  HashMap tbl;

  public MonitorEventTable(String name)
  {
    Name = name;
    tbl = new HashMap();
  }

  public MonitorEvent add(MonitorEvent evt)
  {
    MonitorEvent oldEvt = null;

    Object key = getKey(evt);
    if (key == null) {
      System.err.println("Couldn't get key for MonitorEvent " + evt);
    } else {
      synchronized (tbl) {
        oldEvt = (MonitorEvent )tbl.put(key, evt);
      }
    }

    return oldEvt;
  }

  public void dump(java.io.PrintStream stream)
  {
    dump(stream, "");
  }

  public void dump(java.io.PrintStream stream, String indent)
  {
    Iterator iter = keyIterator();
    int num = 0;
    while (iter.hasNext()) {
      Object key = iter.next();

      stream.println(indent+Name+"["+num+"] "+tbl.get(key));
      num++;
    }
  }

  /**
   * Get a key for the specified <TT>MonitorEvent</TT>
   *
   * @param evt The event.
   */
  public Object getKey(MonitorEvent evt)
  {
    if (evt instanceof ControlMonitorEvent) {
      return getKey((ControlMonitorEvent )evt);
    } else if (evt instanceof MapMonitorEvent) {
      return getKey((MapMonitorEvent )evt);
    } else if (evt instanceof ReferenceMonitorEvent) {
      return getKey((ReferenceMonitorEvent )evt);
    }

    return null;
  }

  /**
   * Get a key for the specified <TT>ControlMonitorEvent</TT>
   *
   * @param evt The event.
   */
  private Object getKey(ControlMonitorEvent evt)
  {
    return new ControlEventKey(evt.getControl());
  }

  /**
   * Get a key for the specified <TT>MapMonitorEvent</TT>
   *
   * @param evt The event.
   */
  private Object getKey(MapMonitorEvent evt)
  {
    Object key = evt.getMap();
    if (key == null) {
      if (evt.getType() != MonitorEvent.MAPS_CLEARED) {
        System.err.println("Got null map for " + evt);
        return null;
      }

      key = "null";
    }

    return key;
  }

  /**
   * Get a key for the specified <TT>ReferenceMonitorEvent</TT>
   *
   * @param evt The event.
   */
  private Object getKey(ReferenceMonitorEvent evt)
  {
    return evt.getLink();
  }

  public boolean hasControlEventQueued(Control ctl)
  {
    Class ctlClass = ctl.getClass();

    synchronized (tbl) {
      Iterator iter = tbl.keySet().iterator();
      while (iter.hasNext()) {
        Object key = iter.next();

        if (!(key instanceof ControlEventKey)) {
          continue;
        }

        ControlEventKey eKey = (ControlEventKey )key;

        if (eKey.equals(ctl)) {
          return true;
        }
      }
    }

    return false;
  }

  public Iterator keyIterator()
  {
    return new KeyIterator(tbl);
  }

  public MonitorEvent remove(Object key)
  {
    synchronized (tbl) {
      return (MonitorEvent )tbl.remove(key);
    }
  }

  public MonitorEvent removeEvent(MonitorEvent evt)
  {
    if (!tbl.containsValue(evt)) {
      return null;
    }

    MonitorEvent oldEvt = null;

    synchronized (tbl) {
      Iterator iter = tbl.entrySet().iterator();
      while (iter.hasNext()) {
        Entry e = (Entry )iter.next();

        if (e.getValue().equals(evt)) {
          oldEvt = (MonitorEvent )tbl.remove(e.getKey());
          break;
        }
      }
    }

    return oldEvt;
  }

  public MonitorEvent restore(Object key, MonitorEvent evt)
  {
    synchronized (tbl) {
      return (MonitorEvent )tbl.put(key, evt);
    }
  }

  public int size() { return tbl.size(); }

  public String toString()
  {
    return "MonitorEventTable["+Name+"#"+tbl.size()+"]";
  }

  /**
   * Used as key for ControlEvents in listener queue
   */
  class ControlEventKey
  {
    private Control control;
    private Class cclass;
    private int instance;

    ControlEventKey(Control ctl)
    {
      control = ctl;
      cclass = ctl.getClass();
      instance = ctl.getInstanceNumber();
    }

    public boolean equals(ControlEventKey key)
    {
      return instance == key.instance && cclass.equals(key.cclass);
    }

    public boolean equals(Object obj)
    {
      if (!(obj instanceof ControlEventKey)) {
        return false;
      }
      return equals((ControlEventKey )obj);
    }

    public boolean equals(Control ctl)
    {
      return cclass.equals(ctl.getClass());
    }

    public int hashCode()
    {
      return cclass.hashCode() + instance;
    }

    public String toString()
    {
      StringBuffer buf = new StringBuffer(control.toString());
      if (buf.length() > 48) {
        buf.setLength(0);
      }
      buf.insert(0, ':');
      buf.insert(0, instance);
      buf.insert(0, '#');
      buf.insert(0, cclass.getName());
      return buf.toString();
    }
  }

  /**
   * <TT>EventComparator</TT> is used to sort the event table just
   * before delivering it to a remote <TT>DisplayMonitorListener</TT>.
   */
  class EventComparator
    implements Comparator
  {
    private HashMap table;

    /**
     * Creates a new comparison class for the given <TT>HashMap</TT>
     *
     * @param tbl The <TT>HashMap</TT> to use for comparisons.
     */
    EventComparator(HashMap tbl)
    {
      table = tbl;
    }

    /**
     * Sorts in reverse order of creation.
     *
     * @param o1 the first object
     * @param o2 the second object
     */
    public int compare(Object o1, Object o2)
    {
      return (((MonitorEvent )(table.get(o1))).getSequenceNumber() -
              ((MonitorEvent )(table.get(o2))).getSequenceNumber());
    }

    /**
     * Returns <TT>true</TT> if the specified object is
     * an <TT>EventComparator</TT>.
     *
     * @param obj The object to examine.
     */
    public boolean equals(Object obj)
    {
      return (obj instanceof EventComparator);
    }
  }

  class KeyIterator
    implements Iterator
  {
    private Object[] list;
    private int current = -1;

    KeyIterator(HashMap table)
    {
      synchronized (table) {
        list = new Object[table.size()];

        // build the array of events
        Iterator iter = table.keySet().iterator();
        for (int i = list.length - 1; i >= 0; i--) {
          if (iter.hasNext()) {
            list[i] = iter.next();
          } else {
            list[i] = null;
          }
        }
      }

      // sort events by order of creation
      Arrays.sort(list, new EventComparator(table));
    }

    public boolean hasNext()
    {
      return (current + 1 < list.length);
    }

    public Object next()
      throws NoSuchElementException
    {
      current++;
      if (current >= list.length) {
        current--;
        throw new NoSuchElementException("No more elements");
      }

      return list[current];
    }

    public void remove()
    {
      throw new NoSuchElementException("Nope");
    }
  }
}
