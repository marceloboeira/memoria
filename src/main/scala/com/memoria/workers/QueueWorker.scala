package com.memoria.workers

import java.util.concurrent.ConcurrentLinkedQueue
import com.memoria.{Cache, Upload}

class QueueWorker(queue: ConcurrentLinkedQueue[Upload]) extends Runnable {
  def run() {
    while (true) {
      if (queue.size > 0) {
        Cache.add(queue.poll)
      }
    }
  }
}
