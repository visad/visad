//
// ThreadUtil.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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


import visad.VisADException;

import java.util.ArrayList;

import java.rmi.RemoteException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;


/**
 *
 */
public class ThreadUtil {

  /**           */
  private String name = "TreadUtil";

  /**           */
  private static int maxThreads = -1;

  /**           */
  private List<VisADException> visadExceptions =
    new ArrayList<VisADException>();

  /**           */
  private List<RemoteException> remoteExceptions =
    new ArrayList<RemoteException>();

  /**           */
  private List<RuntimeException> runtimeExceptions =
    new ArrayList<RuntimeException>();

  /**           */
  private int numThreadsRunning = 0;

  /**           */
  private Object MUTEX = new Object();

  /**           */
  private boolean running = false;

  /**           */
  private List<MyRunnable> runnables = new ArrayList<MyRunnable>();

  /**           */
  private int myMaxThreads;

  /**           */
  private static Hashtable<Integer, Integer[]> times = new Hashtable<Integer,
                                                         Integer[]>();


  /**
   * 
   */
  public ThreadUtil() {
    this("ThreadUtil");
  }

  /**
   * 
   *
   * @param name 
   */
  public ThreadUtil(String theName) {
    this.name = theName;
    if (maxThreads <= 0) {
      maxThreads = Runtime.getRuntime().availableProcessors();
    }
    myMaxThreads = maxThreads;
  }


  /**
   * 
   *
   * @param maxThreads 
   */
  public ThreadUtil(int maxThreads) {
    myMaxThreads = maxThreads;
  }

  /**
   * 
   *
   * @param max 
   */
  public static void setGlobalMaxThreads(int max) {
    ThreadUtil.maxThreads = max;
  }

  /**
   * 
   */
  public static void clearTimes() {
    times = new Hashtable<Integer, Integer[]>();
  }


  /**
   * 
   *
   * @param runnable 
   */
  public void addRunnable(MyRunnable runnable) {
    runnables.add(runnable);
  }

  /**
   * 
   *
   * @param exc 
   */
  public void handleException(Exception exc) {
    if (exc instanceof VisADException) {
      visadExceptions.add((VisADException)exc);
    }
    else if (exc instanceof RemoteException) {
      remoteExceptions.add((RemoteException)exc);
    }
    else if (exc instanceof RuntimeException) {
      runtimeExceptions.add((RuntimeException)exc);
    }
    else {
      runtimeExceptions.add(new RuntimeException(exc));
    }
  }

  /**
   * 
   */
  public void runnableStopped() {
    synchronized (MUTEX) {
      numThreadsRunning--;
    }
  }

  /**
   * 
   */
  public void runnableStarted() {
    synchronized (MUTEX) {
      numThreadsRunning++;
    }
  }


  /**
   * 
   *
   * @return 
   */
  public int getNumRunnables() {
    return runnables.size();
  }


  /**
   * 
   *
   * @param maxThreads 
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public void runInParallel(int maxThreads)
          throws VisADException, RemoteException {
    myMaxThreads = maxThreads;
    runInParallel();
  }

  /**
   * 
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public void runSequentially() throws VisADException, RemoteException {
    myMaxThreads = 1;
    runInParallel();
  }


  /**
   * 
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public void runAllParallel() throws VisADException, RemoteException {
    runInParallel(runnables.size());
  }


  /**
   * 
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public void runInParallel() throws VisADException, RemoteException {
    runInParallel(true);
  }


  /**           */
  public static final int MAX_THREADS = 32;

  /**
   * 
   *
   * @param doAverage 
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public void runInParallel(boolean doAverage)
          throws VisADException, RemoteException {
    myMaxThreads = Math.max(myMaxThreads, 1);
    myMaxThreads = Math.min(myMaxThreads, MAX_THREADS);
    int max = Math.min(myMaxThreads, runnables.size());
    int min = max;
    ThreadPool pool;
    try {
      pool = new ThreadPool("thread util", min, max);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
    long t1 = System.currentTimeMillis();
    running = true;
    int tcnt = 0;
    final String tlbl = "thread:" + (tcnt++);
    
    for (MyRunnable myRunnable : runnables) {
      runnableStarted();
      final MyRunnable theRunnable = myRunnable;
      Runnable runnable = new Runnable() {
        public void run() {
          try {
              //            System.err.println ("start:" + tlbl);
            theRunnable.run();
            //            System.err.println ("end:" + tlbl);
          }
          catch (Exception exc) {
            handleException(exc);
          }
          finally {
            runnableStopped();
          }
        }
      };
      pool.queue(runnable);
    }

    try {
        //      System.err.println ("calling waitForTasks");
      pool.waitForTasks();
      //      System.err.println ("after calling waitForTasks");
      //            System.err.println ("waiting");
      //            while(numThreadsRunning>0) {
      //                try {Thread.currentThread().sleep(5);} catch (Throwable exc) {}
      //                checkErrors();
      //            }
      checkErrors();
    }  finally {
      try {
          //        System.err.println ("calling stopThreads");
        pool.stopThreads();
        //        System.err.println ("after calling stopThreads");
      }
      catch (Exception ignoreThis) {
      }
    }


    running = false;

    long t2 = System.currentTimeMillis();
    System.err.println(
                       name + " time:" + (t2 - t1) + " max threads:" + myMaxThreads);
    if (doAverage && false) {
      Integer[] tuple = times.get(new Integer(myMaxThreads));
      if (tuple == null) {
        times.put(new Integer(myMaxThreads), tuple = new Integer[] {0, 0});
      }
      tuple[0] = new Integer(tuple[0].intValue() + 1);
      tuple[1] = new Integer(tuple[1].intValue() + (int)(t2 - t1));
      System.err.print("   times:");
      for (Enumeration keys = times.keys(); keys.hasMoreElements(); ) {
        Integer maxThreads = (Integer)keys.nextElement();
        Integer[] values = times.get(maxThreads);
        System.err.print(
          "  #:" + maxThreads + " avg:" +
          (int)(values[1].intValue() / (double)values[0].intValue()));
      }
      System.err.println("");
    }



  }



  /**
   * 
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  private void checkErrors() throws VisADException, RemoteException {
    try {
      if (visadExceptions.size() > 0) throw visadExceptions.get(0);
      if (remoteExceptions.size() > 0) throw remoteExceptions.get(0);
      if (runtimeExceptions.size() > 0) throw runtimeExceptions.get(0);
    }
    finally {
      running = false;
    }
  }


  /**
   * MyRunnable 
   */
  public interface MyRunnable {

    /**
     * 
     *
     * @throws Exception 
     */
    public void run() throws Exception;

  }


}

