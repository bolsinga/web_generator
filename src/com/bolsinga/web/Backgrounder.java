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

public class Backgrounder {

  private static Backgrounder sBackgrounder = null;
  private static final Object sBackgrounderLock = new Object();

  private static final int DEFAULT_POOL_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int sPoolThreadCount = Integer.getInteger("web.poolthreadcount", Backgrounder.DEFAULT_POOL_THREAD_COUNT);
  
  private final ExecutorService fExec = Executors.newFixedThreadPool(Backgrounder.sPoolThreadCount);
  
  private final java.util.concurrent.atomic.AtomicInteger fFailures = new java.util.concurrent.atomic.AtomicInteger();

  private final Set<Backgroundable> fClientSet = new HashSet<Backgroundable>();
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
  //  Executor. A task that throws anything is reported and counted, so
  //  awaitTermination() can say the run failed.
  public void execute(Backgroundable backgroundable, final Runnable task) {
    if (!hasInterest()) {
      throw new Error("Excecuting a Backgrounder that has no interest!");
    }
    if (!fExec.isShutdown()) {
      fExec.execute(new Runnable() {
        public void run() {
          try {
            task.run();
          } catch (Throwable t) {
            fFailures.incrementAndGet();
            System.err.println("Background task failed: " + t);
            t.printStackTrace();
          }
        }
      });
    }
  }

  // Waits for every task to finish once the last Backgroundable has been
  //  removed, and returns how many tasks failed.
  public int awaitTermination() throws InterruptedException {
    if (!fExec.isShutdown()) {
      throw new IllegalStateException("Backgrounder is still running; remove every interest first.");
    }
    fExec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    return fFailures.get();
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
