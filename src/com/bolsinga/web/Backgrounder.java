//
//  Backgrounder.java
//  web_generator
//
//  Created by Greg Bolsinga on 8/7/06.
//  Copyright 2006 Bolsinga Software. All rights reserved.
//
package com.bolsinga.web;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class BackgrounderThreadPoolExecutor extends ThreadPoolExecutor {
  // This class is equivalent to Executors.newFixedThreadPool (see the source Executors.java)
  // However it can keep stats about its performance times.
  private final AtomicLong fClassStartTime = new AtomicLong();
  private final ThreadLocal<Long> fTaskStartTime = new ThreadLocal<Long>();
  private final AtomicLong fTaskTotalTime = new AtomicLong();
  
  public BackgrounderThreadPoolExecutor(int poolSize) {
    super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    fClassStartTime.set(System.currentTimeMillis());
  }
  
  protected void beforeExecute(Thread t, Runnable r) {
    try {
      fTaskStartTime.set(System.currentTimeMillis());
    } finally {
      // Call super last
      super.beforeExecute(t, r);
    }
  }
  
  protected void afterExecute(Runnable r, Throwable t) {
    // Call super first
    super.afterExecute(r, t);
  
    long taskTime = System.currentTimeMillis() - fTaskStartTime.get();
    
    fTaskTotalTime.addAndGet(taskTime);
  }
  
  protected void terminated() {
    try {
      if (System.getProperty("site.times") != null) {
        // This time is the time taken adding up all threads across all CPUs.
        // It will be longer than the 'class time' below. This will get 
        // shorter as the tasks become more efficient.
        System.out.println("Task Total Time: " + fTaskTotalTime.get());
        
        // This time is the total time this class is alive, which is the wall clock time.
        // It more closely matches single threaded timings.
        long classTotal = System.currentTimeMillis() - fClassStartTime.get();
        System.out.println("Class Total Time: " + classTotal);
      }
    } finally {
      // Call super last
      super.terminated();
    }
  }
}

public class Backgrounder {

  private static Backgrounder sBackgrounder = null;
  private static final Object sBackgrounderLock = new Object();

  private static final int DEFAULT_POOL_THREAD_COUNT = 3;
  private static final int sPoolThreadCount = Integer.getInteger("web.poolthreadcount", Backgrounder.DEFAULT_POOL_THREAD_COUNT);
  
  private final ExecutorService fExec = new BackgrounderThreadPoolExecutor(Backgrounder.sPoolThreadCount);
  
  private final Set fClientSet = new HashSet<Backgroundable>();
  private final Object fClientSetLock = new Object();
  
  public static Backgrounder getBackgrounder() {
    synchronized (sBackgrounderLock) {
      if (sBackgrounder == null) {
        sBackgrounder = new Backgrounder();
      }
    }
    
    return sBackgrounder;
  }
  
  Backgrounder() {
  }

  // Add backgroundable if not present.
  public void addInterest(Backgroundable backgroundable) {
    synchronized (fClientSetLock) {
      fClientSet.add(backgroundable);
    }
  }
  
  // Remove backgroundable, return whether Backgrounder has no clients.
  private boolean remove(Backgroundable backgroundable) {
    boolean empty = false;
    synchronized (fClientSetLock) {
      fClientSet.remove(backgroundable);
      empty = fClientSet.isEmpty();
    }
    return empty;
  }
  
  private boolean hasInterest() {
    boolean hasInterest = false;
    synchronized (fClientSetLock) {
      hasInterest = !fClientSet.isEmpty();
    }
    return hasInterest;
  }
  
  // Registers the Backgroundable with the Backgrounder (if it hasn't
  //  been registered already). Adds the Runnable to the Backgrounder's
  //  Executor.
  public void execute(Backgroundable backgroundable, Runnable task) {
    if (!hasInterest()) {
      Thread.dumpStack();
      System.exit(1);
    }
    if (!fExec.isShutdown()) {
      fExec.execute(task);
    }
  }
  
  // Unregisters the Backgroundable from the Backgrounder. This
  //  will stop the Backgrounder if there are no more Backgroundables.
  public void removeInterest(Backgroundable backgroundable) {
    boolean empty = remove(backgroundable);
    if (empty) {
      if (!fExec.isShutdown()) {
        fExec.shutdown();
      }
    }
  }
}
