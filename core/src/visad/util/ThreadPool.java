/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2013 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A pool of threads which can be used to execute any Runnable tasks. Internally
 * this class uses a java.util.concurrent.ThreadPoolExecutor, but originally it
 * did not because the original class predates java.util.concurrent. As the
 * internals of this class have evolved, an effort has been made to preserve the
 * original API. Note that a java.util.concurrent.ThreadPoolExecutor does not
 * support the notion of minimum and maximum threads so minimum threads is
 * ignored, and maximum threads is simply the size of the thread pool.
 */
public class ThreadPool {

	/** Default prefix */
	private static final String DEFAULT_PREFIX = ThreadPool.class.toString();

	/** Thread pool from core Java */
	private final ThreadPoolExecutor exec;

	/**
	 * We just need a thread-safe, lock-free, high-performance bag. It does not
	 * matter that it is a queue.
	 */
	private final Collection<Future<?>> bagOfFutures = new ConcurrentLinkedQueue<Future<?>>();

	/** waitForTasks requires that we lock this class for a moment */
	private final Object mutex = new Object();

	/**
	 * The available processors on the system. Java Concurrency in Practice, 8.2
	 * has more information on tuning this number
	 */
	private static final int PROCESSORS = Runtime.getRuntime()
			.availableProcessors() + 1;

	/** The prefix for this thread pool. */
	private final String prefix;

	/**
	 * Build a thread pool with the default thread name prefix and the default
	 * minimum and maximum numbers of threads.
	 * 
	 * @throws Exception
	 */
	public ThreadPool() throws Exception {
		this(DEFAULT_PREFIX, 0, PROCESSORS);
	}

	/**
	 * Build a thread pool with the specified thread name prefix, and the default
	 * minimum and maximum numbers of threads
	 * 
	 * @param prefix
	 * 
	 * @throws Exception
	 */
	public ThreadPool(String prefix) throws Exception {
		this(prefix, 0, PROCESSORS);
	}

	/**
	 * Build a thread pool with the specified maximum number of threads, and the
	 * default thread name prefix and minimum number of threads
	 * 
	 * @param max
	 * 
	 * @throws Exception
	 */
	public ThreadPool(int max) throws Exception {
		this(DEFAULT_PREFIX, 0, max);
	}

	/**
	 * Build a thread pool with the specified minimum and maximum numbers of
	 * threads, and the default thread name prefix.
	 * 
	 * @param min
	 * @param max
	 * 
	 * @throws Exception
	 */
	public ThreadPool(int min, int max) throws Exception {
		this(DEFAULT_PREFIX, min, max);
	}

	/**
	 * Build a thread pool with the specified thread name prefix and minimum and
	 * maximum numbers of threads
	 * 
	 * @param prefix
	 * @param min
	 * @param max
	 * 
	 * @throws Exception
	 */
	public ThreadPool(String prefix, int min, int max) throws Exception {
		this.prefix = prefix;

		// Tom & Don TODO:
		// I would say it is never advisable to go below PROCESSORS constant. The
		// test I ran assume this. But I also want to keep the API as it stands now.
		// I'll let you make the final call here.

		// Could check for this: exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(max < PROCESSORS ? PROCESSORS : max);
		exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(max);
	}

	/**
	 * Return the number of tasks in the queue that are running. Note this
	 * number is constantly changing so check than act is not really recommended.
	 * 
	 * @return a rough number of queued and active tasks
	 */
	public int getTaskCount() {
		synchronized (mutex) {
			for (Future<?> f : bagOfFutures) {
				if (f.isDone()) {
					bagOfFutures.remove(f);
				}
			}
			return bagOfFutures.size();
		}
	}

	// WLH 17 Dec 2001

	/**
	 * Remove this task from the tread pool.
	 * 
	 * @param r
	 *          the runnable to remove from the queue
	 */
	public void remove(Runnable r) {
		exec.remove(r);
	}

	/**
	 * Has the thread pool been closed?
	 * 
	 * @return <tt>true</tt> if the pool has been terminated.
	 */
	public boolean isTerminated() {
		return exec.isTerminated();
	}

	/**
	 * Utility method to print out the tasks
	 */

	public void printPool() {
		System.err.println("Busy Tasks:");
		for (Future<?> f : bagOfFutures) {
			if (!f.isDone()) {
				System.out.println(f.toString());
			}
		}
		System.err.println("Completed Tasks:");
		for (Future<?> f : bagOfFutures) {
			if (f.isDone()) {
				System.out.println(f.toString());
			}
		}
	}

	/**
	 * Add a task to the queue; tasks are executed as soon as a thread is
	 * available, in the order in which they are submitted
	 * 
	 * @param r
	 *          the runnable that will be executed by this thread pool.
	 */
	public void queue(Runnable r) {
		Future<?> submit = exec.submit(r);
		bagOfFutures.add(submit);

		// While we are at it, clean out the bag of completed tasks.
		for (Future<?> f : bagOfFutures) {
			if (f.isDone()) {
				bagOfFutures.remove(f);
			}
		}
	}

	/**
	 * Wait for currently-running tasks to finish. Blocks while the internal queue
	 * of tasks is completed.
	 * 
	 * @return true
	 */
	public boolean waitForTasks() {
		// Lock the thread pool and drain all pending task.
		synchronized (mutex) {
			for (Future<?> f : bagOfFutures) {
				try {
					f.get();
					// Tom & Don TODO: handle the exception in whatever way you think is
					// best. The TheadPool previously just swallowed exceptions so that is 
					// how I am leaving it, but in principle, this is not advisable.
				} catch (InterruptedException e) {
					// http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					// The runnable threw exception.
					//e.printStackTrace();
				}
			}
			// We are now clear of futures.
			bagOfFutures.clear();
		}
		return true;
	}

	/**
	 * Set the maximum number of pooled threads
	 * 
	 * @param num
	 *          the number of threads
	 * 
	 * @throws Exception
	 */
	public void setThreadMaximum(int num) throws Exception {
		exec.setCorePoolSize(num);
	}

	/** Shut down this thread pool. */
	public void stopThreads() {
		exec.shutdown();
	}
}
