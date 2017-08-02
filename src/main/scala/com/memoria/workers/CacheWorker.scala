package com.memoria.workers

import com.memoria.Cache

class CacheWorker(interval: Long) extends Runnable {
  def run() {
    while (true) {
      Cache.removeOldEntries
      Cache.refreshStatistics
      Thread.sleep(interval)
    }
  }
}
