package com.memoria.workers

import com.memoria.Uploads

class CacheWorker(interval: Long) extends Runnable {
  def run() {
    while (true) {
      Uploads.removeOldEntries
      Uploads.refreshStatistics
      Thread.sleep(interval)
    }
  }
}
