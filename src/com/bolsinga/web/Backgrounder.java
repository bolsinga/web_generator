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

  private static final int DEFAULT_POOL_THREAD_COUNT = 3;
  private static final int sPoolThreadCount = Integer.getInteger("web.poolthreadcount", Backgrounder.DEFAULT_POOL_THREAD_COUNT);
  
  private final ExecutorService fExec = Executors.newFixedThreadPool(Backgrounder.sPoolThreadCount);
  
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
