package visad;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import javax.swing.Timer;

public class DisplayActivity
{
  private static final int DEFAULT_IDLE_MILLISECONDS = 250;

  private static final boolean DEBUG = false;

  private transient DisplayImpl dpy;
  private int interval;

  private long lastBusyEvt;
  private boolean isBusy;
  private transient Timer busyTimer;
  private transient BusyAction busyAction;

  private transient ArrayList handlerList;

  /**
   * Manage busy/idle handlers for a Display, using a default
   * idle time of 0.25 second.
   *
   * @param dpy Display to manage.
   */
  public DisplayActivity(DisplayImpl dpy)
  {
    this(dpy, DEFAULT_IDLE_MILLISECONDS);
  }

  /**
   * Manage busy/idle handlers for a Display, using the specified
   * idle time, with the minimum idle time being 100 milliseconds.
   *
   * @param dpy Display to manage.
   * @param milliseconds Number of milliseconds to wait before
   *                     the Display is considered idle.
   */
  public DisplayActivity(DisplayImpl dpy, int milliseconds)
  {
    this.dpy = dpy;
    if (milliseconds < 100) {
      this.interval = 100;
    } else {
      this.interval = milliseconds;
    }

    lastBusyEvt = System.currentTimeMillis() - this.interval;
    isBusy = false;

    busyAction = new BusyAction();
    busyTimer = new Timer(this.interval, busyAction);

    busyTimer.removeActionListener(busyAction);
    busyAction = null;

    handlerList = new ArrayList();
  }

  /**
   * Stop the idle timer and any running tasks.
   */
  public void destroy()
  {
    synchronized (busyTimer) {
      if (busyAction != null) {
        busyTimer.removeActionListener(busyAction);
        busyAction = null;
      }

      busyTimer.stop();
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

    if (!busyTimer.isRunning()) {
      busyTimer.restart();
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

    if (handlerList.size() == 0 && busyTimer.isRunning()) {
      busyTimer.stop();
    }
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

      if (busyAction == null && busyTimer != null) {
        synchronized (busyTimer) {
          if (busyAction == null) {
            // create a busy task
            busyAction = new BusyAction();

            // have busy action called at the specified interval
            busyTimer.addActionListener(busyAction);
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

  private class BusyAction
    implements ActionListener
  {
    private static final int MAX_IDLE = 10;

    private int idleCount = 0;

    BusyAction() { }

    public void actionPerformed(ActionEvent evt)
    {
      if (!isBusy) {
        // another interval has passed where display wasn't busy
        idleCount++;

        // if it's been long enough...
        if (idleCount > MAX_IDLE) {

          // ... and there's a timer object...
          if (busyTimer != null) {

            // ...stop the timer
            synchronized (busyTimer) {
              busyTimer.removeActionListener(busyAction);
              busyAction = null;
              if (DEBUG) System.err.println("CANCELLED");
            }
          }
        }
      } else {
        final long now = System.currentTimeMillis();

        if (lastBusyEvt + interval <= now) {
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
}
