package com.memoria.workers

import java.time.Instant

import org.scalatest._
import com.memoria.{Cache, Upload, UploadStatistics}

class CacheWorkerSpec extends FunSpec with Matchers with BeforeAndAfter {
  before {
    Cache.destroyAll
    Cache.add(Upload(1, Instant.now.getEpochSecond - 120))
    Cache.add(Upload(1, Instant.now.getEpochSecond - 65))
    Cache.add(Upload(8, Instant.now.getEpochSecond - 48))
    Cache.add(Upload(10, Instant.now.getEpochSecond - 1))


    (new Thread(new CacheWorker(1))).start
  }

  it("removes old entries from the cache") {
    Cache.count shouldBe 2
  }

  it("generates the statistics") {
    Cache.uploadStatistics.shouldBe(UploadStatistics(2, 18, 8, 10, 9.0))
  }
}
