package com.memoria.workers

import java.time.Instant

import org.scalatest._
import com.memoria.{Cache, Upload, Queue}

class QueueWorkerSpec extends FunSpec with Matchers with BeforeAndAfter {
  before {
    Cache.destroyAll
    Queue.instance.add(Upload(8, Instant.now.getEpochSecond - 48))
    Queue.instance.add(Upload(10, Instant.now.getEpochSecond - 1))

    (new Thread(new QueueWorker(Queue.instance))).start
  }

  it("consumes the queue") {
    Queue.instance.size shouldBe 0
  }

  it("persists the uploads on the cache") {
    Cache.count shouldBe 2
  }
}

