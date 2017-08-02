package com.memoria.workers

import java.util.concurrent.BlockingQueue

import com.memoria.{Upload, Cache}

class QueueWorker(queue: BlockingQueue[Upload]) extends Runnable {
  def run() {
    while (true) {
      Cache.add(queue.take)
    }
  }
}
