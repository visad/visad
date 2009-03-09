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
public class ThreadUtil
{
    private static int maxThreads = 2;

    private List<VisADException>visadExceptions = new ArrayList<VisADException>();
    private List<RemoteException>remoteExceptions = new ArrayList<RemoteException>();
    private List<RuntimeException> runtimeExceptions = new ArrayList<RuntimeException>();

    private  int numThreadsRunning = 0;
    private Object MUTEX = new Object();
    private boolean running=false;
    private List<MyRunnable> runnables = new ArrayList<MyRunnable>();

    private int myMaxThreads;
    private static Hashtable<Integer,Integer[]>  times = new Hashtable<Integer,Integer[]>(); 

    public ThreadUtil() {
        if(maxThreads>1) {
            maxThreads = 1;
        } else {
            maxThreads = 11;
        }
        myMaxThreads = maxThreads;
    }



    public void addRunnable(MyRunnable runnable) {
        runnables.add(runnable);
    }

    public void handleException(Exception exc) {
        if(exc instanceof VisADException) {
            visadExceptions.add((VisADException)exc);
        } else if(exc instanceof RemoteException) {
            remoteExceptions.add((RemoteException)exc);
        } else if(exc instanceof RuntimeException) {
            runtimeExceptions.add((RuntimeException)exc);
        } else  {
            runtimeExceptions.add(new RuntimeException(exc));
        }
    }

    public void runnableStopped() {
        synchronized(MUTEX) {
            numThreadsRunning--;
        }
    }

    public void runnableStarted() {
        synchronized(MUTEX) {
            numThreadsRunning++;
        }
    }

    private void waitOnThreads() throws VisADException, RemoteException {
        while(true) {
            if(numThreadsRunning<myMaxThreads) break;
            try {Thread.currentThread().sleep(5);} catch (Throwable exc) {}
            checkErrors();
        }
        checkErrors();
    }

    public void runInParallel() throws VisADException, RemoteException {
        long t1 = System.currentTimeMillis();
        running = true;
        for(MyRunnable myRunnable: runnables) {
            runnableStarted();
            final MyRunnable theRunnable = myRunnable;
            Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            theRunnable.run();
                        } catch(Exception exc) {
                            handleException(exc);
                        } finally {
                            runnableStopped();
                        }
                    }
                };
            Thread t= new Thread(runnable);
            t.start();
            waitOnThreads();
        }
        while(numThreadsRunning>0) {
            try {Thread.currentThread().sleep(5);} catch (Throwable exc) {}
            checkErrors();
        }
        running = false;

      long t2 = System.currentTimeMillis();
      System.err.println("Time:" + (t2-t1) +" max threads:" +myMaxThreads);
      Integer[]tuple  = times.get(new Integer(myMaxThreads));
      if(tuple == null) {
          times.put(new Integer(myMaxThreads), tuple = new Integer[]{0,0});
      }
      tuple[0] = new Integer(tuple[0].intValue()+1);
      tuple[1] = new Integer(tuple[1].intValue()+(int)(t2-t1));
      System.err.print("   times:");
      for(Enumeration keys= times.keys();keys.hasMoreElements();) {
          Integer maxThreads = (Integer) keys.nextElement();
          Integer[]values = times.get(maxThreads);
          System.err.print("  #:" + maxThreads + " avg:" + (int)(values[1].intValue()/(double)values[0].intValue()));
      }
      System.err.println("");




    }



    private  void checkErrors() throws VisADException, RemoteException {
        try {
            if(visadExceptions.size()>0) throw visadExceptions.get(0);
            if(remoteExceptions.size()>0) throw remoteExceptions.get(0);
            if(runtimeExceptions.size()>0) throw runtimeExceptions.get(0);
        } finally {
            running = false;
        }
    }


    public interface MyRunnable {
        public void run() throws Exception;
    }


}
