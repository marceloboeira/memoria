package com.memoria.workers

import java.util.concurrent.BlockingQueue

import com.memoria.{Upload, Uploads}

class QueueWorker(queue: BlockingQueue[Upload]) extends Runnable {
  def run() {
    while (true) {
      Uploads.add(queue.take)
    }
  }
}
