package visad;

import java.util.ArrayList;
import java.util.Timer;

import visad.DisplayImpl;

public class DisplayActivity
{
  private static final int DEFAULT_IDLE_SECONDS = 1;

  private static final boolean DEBUG = false;

  private transient DisplayImpl dpy;
  private int interval;

  private long lastBusyEvt;
  private boolean isBusy;
  private transient Timer busyTimer;
  private transient BusyTask busyTask;

  private transient ArrayList handlerList;

  /**
   * Manage busy/idle handlers for a Display, using a default
   * idle time of 1 second.
   *
   * @param dpy Display to manage.
   */
  public DisplayActivity(DisplayImpl dpy)
  {
    this(dpy, DEFAULT_IDLE_SECONDS);
  }

  /**
   * Manage busy/idle handlers for a Display, using the specified
   * idle time.
   *
   * @param dpy Display to manage.
   * @param seconds Number of seconds to wait before the Display
   *                is considered idle.
   */
  public DisplayActivity(DisplayImpl dpy, int seconds)
  {
    this.dpy = dpy;
    this.interval = seconds * 1000;

    lastBusyEvt = System.currentTimeMillis() - this.interval;
    isBusy = false;
    busyTimer = new Timer(true);
    busyTask = null;

    handlerList = new ArrayList();
  }

  /**
   * Stop the idle timer and any running tasks.
   */
  public void destroy()
  {
    synchronized (busyTimer) {
      if (busyTask != null) {
        busyTask.cancel();
        busyTask = null;
      }

      busyTimer.cancel();
    }
  }

  /**
   * Add a new activity handler.
   */
  public void addHandler(ActivityHandler ah)
    throws VisADException
  {
    if (handlerList == null) {
      throw new VisADException("No handler list found; was this object serialized?");
    }

    handlerList.add(ah);
  }

  /**
   * Remove an activity handler.
   */
  public void removeHandler(ActivityHandler ah)
    throws VisADException
  {
    if (handlerList == null) {
      throw new VisADException("No handler list found; was this object serialized?");
    }

    handlerList.remove(ah);
  }

  /**
   * Notify all handlers of a transition from idle to busy or vice versa.
   *
   * @param isBusy <tt>true</tt> if the Display is now busy.
   */
  private void notifyList(boolean isBusy)
  {
    if (handlerList != null && busyTimer != null) {
      synchronized (handlerList) {
        final int len = handlerList.size();
        for (int i = 0; i < len; i++) {
          ActivityHandler ah = (ActivityHandler )handlerList.get(i);
          if (isBusy) {
            ah.busyDisplay(dpy);
          } else {
            ah.idleDisplay(dpy);
          }
        }
      }
    }
  }

  /**
   * Notify the activity monitor that work has been done.
   */
  public void updateBusyStatus()
  {
    final long now = System.currentTimeMillis();

    if (!isBusy && (now < lastBusyEvt || now > lastBusyEvt + interval)) {
      // Display is doing something...
      isBusy = true;

      if (busyTask == null && busyTimer != null) {
        synchronized (busyTimer) {
          if (busyTask == null) {
            // create a busy task
            busyTask = new BusyTask();

            // have busy task called at the specified interval
            busyTimer.scheduleAtFixedRate(busyTask, interval, interval);
            if (DEBUG) System.err.println("STARTED");
          }
        }
      }

      if (DEBUG) System.err.println("BUSY");

      // let handlers know that Display is busy
      notifyList(true);
    }

    // remember when Display was last busy
    lastBusyEvt = now;
  }

  private class BusyTask
    extends java.util.TimerTask
  {
    private static final int MAX_IDLE = 10;

    private int idleCount = 0;

    BusyTask() { }

    public void run()
    {
      final long now = System.currentTimeMillis();

      if (!isBusy) {
        // another interval has passed where display wasn't busy
        idleCount++;

        // if it's been long enough...
        if (idleCount > MAX_IDLE) {

          // ... and there's a timer object...
          if (busyTimer != null) {

            // ...stop the timer
            synchronized (busyTimer) {
              cancel();
              busyTask = null;
              if (DEBUG) System.err.println("CANCELLED");
            }
          }
        }
      } else if (lastBusyEvt + interval <= now) {
        // if we're past the waiting period, we must be idle
        isBusy = false;
        idleCount = 0;

        if (DEBUG) System.err.println("IDLE");

        // let handlers know that Display is idle
        notifyList(false);
      }
    }
  }
}
