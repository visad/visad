//
// ThreadPool.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.util.ListIterator;
import java.util.Vector;

/** A pool of threads (with minimum and maximum limits on the number
 *  of threads) which can be used to execute any Runnable tasks.
 */
public class ThreadPool
{
  private static final String DEFAULT_PREFIX = "Minnow";

/* WLH 11 April 2001
there's a deadlock possibility here, if running ActionImpls are
waiting for other ActionImpls to run
*/
  private static final int DEFAULT_MIN_THREADS = 5; // WLH 11 April 2001
  private static final int DEFAULT_MAX_THREADS = 10;

  // maximum number of tasks which can be queued before a new thread is created
  private int maxQueuedTasks = 3;

  // minimum and maximum number of threads to create
  private int minThreads;
  private int maxThreads;

  // generic lock object
  private Object threadLock = new Object();

  // object used to signal task completion
  private Object doneLock = new Object();

  // 'true' if all threads should exit
  private boolean terminateThread = false;

  // list of threads in the pool
  private Vector threads = new Vector();

  // list of queued tasks
  private Vector tasks = new Vector();

  // WLH 20 Feb 2001
  // list of busy tasks - to prevent more than one Thread
  // from running the same ActionImpl
  private Vector busy_tasks = new Vector();

  // variables used to name threads
  private String prefix;
  private int nextID = 0;

  private class ThreadMinnow
    extends Thread
  {
    private ThreadPool parent = null;

    public ThreadMinnow(ThreadPool p)
    {
      parent = p;
      start();
    }

    public void run()
    {
      while (true) {
        // try to find something to do...
        Runnable r = parent.getTask();
        if (r != null) {
          try {
            r.run();
          } catch (Throwable t) {
            t.printStackTrace();
          }

          parent.releaseTask(r); // WLH 20 Feb 2001

          // wake-up another ThreadMinnow after releaseTask() in case
          // the only task in the queue equaled the one this ThreadMinnow
          // was running
          synchronized (threadLock) { // WLH 21 Feb 2001
            threadLock.notify();
          }

          synchronized (doneLock) {
            doneLock.notifyAll();
          }
        } else {

          // if we're supposed to stop, break out of the infinite loop
          if (terminateThread) {
            return;
          }

          // NOTE:
          //   the 'terminateThread' check only happens when there's no
          //   work to be done.  This is to ensure that all outstanding
          //   tasks are completed.


          // wait until there's work to be done
          try {
            synchronized (threadLock) {
              threadLock.wait();
            }
          } catch (InterruptedException e) {
            // ignore interrupts ...
          }
        }
      }
    }
  }

  /** Build a thread pool with the default thread name prefix
   *  and the default minimum and maximum numbers of threads
   */
  public ThreadPool()
    throws Exception
  {
    this(DEFAULT_PREFIX, DEFAULT_MIN_THREADS, DEFAULT_MAX_THREADS);
  }

  /** Build a thread pool with the specified thread name prefix, and
   *  the default minimum and maximum numbers of threads
   */
  public ThreadPool(String prefix)
    throws Exception
  {
    this(prefix, DEFAULT_MIN_THREADS, DEFAULT_MAX_THREADS);
  }

  /** Build a thread pool with the specified maximum number of
   *  threads, and the default thread name prefix and minimum number
   *  of threads
   */
  public ThreadPool(int max)
    throws Exception
  {
    this(DEFAULT_MIN_THREADS, max);
  }

  /** Build a thread pool with the specified minimum and maximum
   *  numbers of threads, and the default thread name prefix
   */
  public ThreadPool(int min, int max)
    throws Exception
  {
    this(DEFAULT_PREFIX, min, max);
  }

  /** Build a thread pool with the specified thread name prefix and
   *  minimum and maximum numbers of threads
   */
  public ThreadPool(String prefix, int min, int max)
    throws Exception
  {
    minThreads = min;
    maxThreads = max;

    if (minThreads > maxThreads) {
      throw new Exception("Maximum number of threads (" + maxThreads +
                          ") is less than minimum number of threads (" +
                          minThreads + ")");
    }

    this.prefix = prefix;

    for (int i = 0; i < minThreads; i++) {
      ThreadMinnow minnow = new ThreadMinnow(this);
      minnow.setName(prefix + "-" + nextID++);
      threads.addElement(minnow);
    }
  }


   
 /** return the number of tasks in the queue and that are running 
  * @return number of queued and active tasks
  */
  public int getTaskCount() 
  {
    int count = 0;
    if(tasks!=null) count+= tasks.size();
    if(busy_tasks!=null) count+= busy_tasks.size();
    return count;
  }

  // WLH 17 Dec 2001
  public void remove(Runnable r) {
    synchronized (tasks) {
      tasks.removeElement(r);
      busy_tasks.removeElement(r);
    }
  }

  /**
   * Has the thread pool been closed?
   *
   * @return <tt>true</tt> if the pool has been terminated.
   */
  public boolean isTerminated()
  {
    return terminateThread;
  }

  /** Add a task to the queue; tasks are executed as soon as a thread
   *  is available, in the order in which they are submitted
   */
  public void queue(Runnable r)
  {
    // don't queue new tasks after the pool has been shut down
    if (terminateThread) {
      throw new Error("Task queued after threads stopped");
    }

    // add this task to the queue
    int numTasks = 0;
    synchronized (tasks) {
      if (!tasks.contains(r)) { // WLH 20 Feb 2001
/*
System.out.println("queue new r " + ((ActionImpl) r).getName());
int n = tasks.size();
for (int i=0; i<n; i++) {
  ActionImpl a = (ActionImpl) tasks.elementAt(i);
  System.out.println("  " + i + " = " + a.getName());
}
*/
        tasks.addElement(r);
        numTasks = tasks.size();
      }
      else {
// System.out.println("queue already contains r");
      }
    }

    // make sure one or more threads are told to deal with the new task
    synchronized (threadLock) {
      // if all the threads appear to be busy...
      if (numTasks > maxQueuedTasks) {

        // ...and we haven't created too many threads...
        if (threads != null && threads.size() < maxThreads) {

          // ...spawn a new thread and tell it to deal with this
          try {
            Thread t = new ThreadMinnow(this);
            t.setName(prefix + "-" + nextID++);
            threads.addElement(t);
            threadLock.notify();
          } catch (SecurityException e) {
            // can't spawn a thread from this ThreadGroup...
            // wait until something's queued from the main thread
          }
        } else {

          // try to wake up all waiting threads to deal with the backlog
          threadLock.notifyAll();
        }
      } else {

        // not all threads are busy; notify one of the waiting threads
        threadLock.notify();
      }
    }
  }

  /** Get the next task on the queue.<BR>
   *  This method is intended only for the use of client threads and
   *  should never be called by external objects.
   */
  Runnable getTask()
  {
    Runnable thisTask = null;

    synchronized (tasks) {
/*
int n = tasks.size();
for (int i=0; i<n; i++) {
  ActionImpl a = (ActionImpl) tasks.elementAt(i);
  System.out.println("  getTask " + i + " = " + a.getName());
}
*/

/* WLH 20 Feb 2001
      if (tasks.size() > 0) {
        thisTask = (Runnable )tasks.elementAt(0);
        tasks.removeElementAt(0);
      }
*/
      int n = tasks.size();
      for (int i=0; i<n; i++) {
        thisTask = (Runnable )tasks.elementAt(i);
        if (busy_tasks.contains(thisTask)) {
          thisTask = null;
        }
        else {
          tasks.removeElementAt(i);
          busy_tasks.addElement(thisTask);
          break;
        }
      }

    } // end synchronized (tasks)

    return thisTask;
  }

  // WLH 20 Feb 2001
  void releaseTask(Runnable r) {
    synchronized (tasks) {
      busy_tasks.removeElement(r);
    }
  }

  /** wait for currently-running tasks to finish */
  public boolean waitForTasks()
  {
    // give all the current tasks a chance to finish
    int timeout = tasks.size();

    // don't allow thread to wait for itself
    if (Thread.currentThread() instanceof ThreadMinnow) {
      try { Thread.sleep(15000); } catch (InterruptedException ie) { }
      return false;
    }

    while (tasks.size() > 0) {
      try {
        synchronized (doneLock) {
          doneLock.wait();
        }
      } catch (InterruptedException e) {
        // ignore interrupts ...
      }

      if (timeout-- == 0) {
        break;
      }
    }

    return (timeout > 0);
  }

  /** increase the maximum number of pooled threads */
  public void setThreadMaximum(int num)
    throws Exception
  {
    if (num < maxThreads) {
      throw new Exception("Cannot decrease maximum number of threads");
    }
    maxThreads = num;
  }

  /** Stop all threads as soon as all queued tasks are completed */
  public void stopThreads()
  {
    if (terminateThread) {
      return;
    }

    terminateThread = true;
    synchronized (threadLock) {
      threadLock.notifyAll();
    }

    Vector oldthreads;
    ListIterator i;
    synchronized (threads) {
      oldthreads = threads;
      threads = null;
      i = oldthreads.listIterator();
    }

    while (i.hasNext()) {
      Thread t = (Thread )i.next();
      while (true) {
        synchronized (oldthreads) {
          oldthreads.notifyAll();
        }
        try {
          t.join();
          break;
        } catch (InterruptedException e) {
        }
      }
      i.remove();
    }
  }
}

/*
 * Here's a simple test program for the ThreadPool code.  Save it to
 * 'SimpleTask.java':
 *
 * import java.util.Random;
 *
 * import visad.util.ThreadPool;
 *
 * public class SimpleTask implements Runnable {
 *   private static Random rand = new Random();
 *
 *   private int count = 0;
 *
 *   public SimpleTask() { }
 *
 *   public void run()
 *   {
 *     count++;
 *     try { Thread.sleep((rand.nextInt() % 10), 0); } catch (Throwable t) { }
 *   }
 *
 *   public int getCount() { return count; }
 *
 *   public static void main(String[] args)
 *   {
 *     ThreadPool pool;
 *     try {
 *       pool = new ThreadPool();
 *     } catch (Exception e) {
 *       System.err.println("Couldn't build ThreadPool: " + e.getMessage());
 *       System.exit(1);
 *       return;
 *     }
 *
 *     // give threads a chance to start up
 *     try { Thread.sleep(100, 0); } catch (Throwable t) { }
 *
 *     SimpleTask[] task = new SimpleTask[4];
 *     for (int i = 0; i < task.length; i++) {
 *       task[i] = new SimpleTask();
 *     }
 *
 *     for (int i = 0; i < 10; i++) {
 *       for (int j = 0; j < task.length; j++) {
 *         pool.queue(task[j]);
 *       }
 *       try { Thread.sleep(10, 0); } catch (Throwable t) { }
 *     }
 *
 *     pool.stopThreads();
 *
 *     boolean success = true;
 *     for (int i = 0; i < task.length; i++) {
 *       int c = task[i].getCount();
 *       if (c != 10) {
 *         System.err.println("Got " + c + " for task#" + i + ", expected 10");
 *         success = false;
 *       }
 *     }
 *
 *     if (success) System.out.println("Success!");
 *   }
 * }
 *
 */
