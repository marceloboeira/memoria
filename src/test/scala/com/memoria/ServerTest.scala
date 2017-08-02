package com.memoria

import java.time.Instant

import com.twitter.finagle.http.Status
import io.finch.Error.NotParsed
import io.finch.Input
import org.scalatest._

trait UploadsCleaner extends BeforeAndAfterEach { this: Suite =>
  override def beforeEach {
    Uploads.queue.clear
    Uploads.destroyAll
    super.beforeEach
  }
}

class ServerTest extends FunSpec with Matchers with UploadsCleaner {
  import com.memoria.Server.postUpload
  import com.memoria.Server.getStatistics

  def uploadInput(count: Int, secondsAgo: Int): Input = {
    Input.post("/upload")
      .withBody("{ \"count\": %s, \"timestamp\": %s }"
        .format(count, Instant.now.getEpochSecond - secondsAgo))
  }

  def invalidUploadInput: Input = {
    Input.post("/upload")
      .withBody("{ \"invalid\": 1, \"timestamp\": 10000 }")
  }

  def populateUploadsWith(count: Int, secondsAgo: Int): Unit = {
    Uploads.add(Upload(count, Instant.now.getEpochSecond - secondsAgo))
  }

  describe("POST /upload") {
    describe("when the parameters are valid") {
      describe("and the timestamp is not older than 60 seconds") {
        it("returns 201 - Created") {
          postUpload(uploadInput(1, 5))
            .awaitOutputUnsafe()
            .map(_.status) shouldBe Some(Status.Created)
        }

        it("enques the creation of the upload") {
          postUpload(uploadInput(1, 5)).awaitOutputUnsafe()

          Uploads.queue.size shouldBe 1
        }
      }

      describe("and the timestamp is older than 60 seconds") {
        it("returns 204 - No Content") {
          postUpload(uploadInput(1, 65))
            .awaitOutputUnsafe()
            .map(_.status) shouldBe Some(Status.NoContent)
        }

        it("does not enqueue a new entry") {
          postUpload(uploadInput(1, 65)).awaitOutputUnsafe()

          Uploads.queue.size shouldBe 0
        }
      }
    }

    describe("when the parameters are not valid") {
      it("returns with status 400 - Bad Request") {
        a[NotParsed] shouldBe thrownBy(postUpload(invalidUploadInput).awaitValueUnsafe())
      }
    }
  }

  describe ("GET /statistics") {
    describe("when there is no data") {
      it("returns 200 - Success") {
        Uploads.refreshStatistics

        getStatistics(Input.get("/statistics"))
          .awaitOutputUnsafe()
          .map(_.status) shouldBe Some(Status.Ok)
      }

      it("returns 0 for all the stats") {
        Uploads.refreshStatistics

        getStatistics(Input.get("/statistics"))
          .awaitValueUnsafe() shouldBe Some(UploadStatistics(0,0,0,0,0))
      }
    }

    describe("when there is data") {
      it("returns 200 - Success") {
        populateUploadsWith(5, 10)

        getStatistics(Input.get("/statistics"))
          .awaitOutputUnsafe()
          .map(_.status) shouldBe Some(Status.Ok)
      }

      it("returns the expected value for the stats") {
        populateUploadsWith(1, 12)
        populateUploadsWith(10, 13)
        populateUploadsWith(8, 14)

        Uploads.refreshStatistics

        getStatistics(Input.get("/statistics")).awaitValueUnsafe() shouldBe Some(UploadStatistics(3,19,1,10,6.0))
      }
    }
  }
}
