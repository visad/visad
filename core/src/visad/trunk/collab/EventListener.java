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

import java.rmi.RemoteException;

import java.util.Iterator;

import visad.Control;

import visad.util.ThreadPool;

public abstract class EventListener
  implements Runnable
{
  String Name;

  private boolean dead = false;

  private Object tableLock = new Object();
  private boolean haveThread = false;

  private EventTable table = null;
  private EventTable diverted = null;

  /**
   * The event delivery thread pool and its lock.
   */
  private transient static ThreadPool listenerPool = null;
  private static Object listenerPoolLock = new Object();

  EventListener(String name)
  {
    Name = name;
    table = getNewEventTable();
  }

  public void addEvent(MonitorEvent evt)
  {
    synchronized (tableLock) {
      if (haveThread) {
        if (diverted == null) {
          diverted = getNewEventTable();
        }

        MonitorEvent oldEvt = diverted.add(evt);
      } else {
        MonitorEvent oldEvt = table.add(evt);
        startDelivery();
      }
    }
  }

  /**
   * Attempt to deliver the queued events.
   *
   * @exception RemoteException If a connection could not be made to the
   * 				remote listener.
   */
  abstract void deliverEventTable(EventTable tbl)
    throws RemoteException;

  public String getName() { return Name; }
  /**
   * Build an <TT>EventTable</TT> for this listener.
   */
  abstract EventTable getNewEventTable();

  public boolean hasControlEventQueued(Control ctl)
  {
    if (ctl == null) {
      return true;
    }

    if (table != null &&
        ((MonitorEventTable )table).hasControlEventQueued(ctl))
    {
      return true;
    }
    if (diverted != null &&
        ((MonitorEventTable )diverted).hasControlEventQueued(ctl))
    {
      return true;
    }

    return false;
  }

  /**
   * Check to see if the connection is dead.
   *
   * @return <TT>true</TT> if the connection is dead.
   */
  public boolean isDead() { return dead; }

public boolean isThreadRunning() { return haveThread;}

  public MonitorEvent removeEvent(MonitorEvent evt)
  {
    MonitorEvent dEvt, tEvt;

    synchronized (tableLock) {
      if (diverted == null) {
        dEvt = null;
      } else {
        dEvt = (MonitorEvent )diverted.removeEvent(evt);
      }
      tEvt = (MonitorEvent )table.removeEvent(evt);
    }

    return (tEvt != null ? tEvt : dEvt);
  }

  /**
   * Delivers events to the remote listener.
   */
  public void run()
  {
    boolean delivered = false;

    try {
      int attempts = 0;

      while (!delivered) {
        try {
          deliverEventTable(table);
          delivered = true;
        } catch (RemoteException re) {
          if (attempts++ < 10) {
            // wait a bit, then try again to deliver the events
            try { Thread.sleep(500); } catch (InterruptedException ie) { }
          } else {
            // if we failed to connect for 10 times, give up
            break;
          }
        }

        if (delivered) {
          synchronized (tableLock) {
            if (!undivertEvents()) {
              haveThread = false;
              break;
            }

            delivered = false;
          }
        }
      }
    } finally {
      // indicate that the thread hass exited
      synchronized (tableLock) {
        haveThread = false;
      }
    }

    // mark this listener as dead
    if (!delivered) {
      dead = true;
    }
  }

  /**
   * Increases the maximum number of threads allowed for the thread pool.
   *
   * @param num The new maximum number of threads.
   *
   * @exception Exception If there was a problem with the specified value.
   */
  public static void setThreadPoolMaximum(int num)
    throws Exception
  {
    if (listenerPool == null) {
      startThreadPool();
    }
    listenerPool.setThreadMaximum(num);
  }

  /**
   * Start event delivery.
   */
  private void startDelivery()
  {
    synchronized (tableLock) {
      if (haveThread) {
        return;
      }

      // remember that we've started a thread
      haveThread = true;
    }

    if (listenerPool == null) {
      startThreadPool();
    }
    listenerPool.queue(this);
  }

  /**
   * Creates the shared thread pool.
   */
  private static void startThreadPool()
  {
    synchronized (listenerPoolLock) {
      if (listenerPool == null) {
        // ...fill the pool; die if pool wasn't created
        try {
          listenerPool = new ThreadPool("ListenerThread");
        } catch (Exception e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
          System.exit(1);
        }
      }
    }
  }

  /**
   * Destroys all threads after they've drained the job queue.
   */
  public static void stopThreadPool()
  {
    if (listenerPool != null) {
      listenerPool.stopThreads();
      listenerPool = null;
    }
  }


  /**
   * Returns <TT>true</TT> if there were diverted events
   *  to be delivered
   */
  private boolean undivertEvents()
  {
    final boolean undivert;
    synchronized (tableLock) {
      // if there are events queued, restore them to the main table
      undivert = (diverted != null);
      if (undivert) {
        table = diverted;
        diverted = null;
      }
    }

    return undivert;
  }
}
