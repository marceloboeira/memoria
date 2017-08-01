package com.memoria

import java.time.Instant

import com.twitter.finagle.http.Status
import com.twitter.util.Throw
import io.finch.Error.NotParsed
import io.finch.Input
import io.finch.items.BodyItem
import org.scalatest._
import com.memoria.Uploads

trait UploadsCleaner extends BeforeAndAfterEach { this: Suite =>
  override def beforeEach() {
    Uploads.destroyAll
    super.beforeEach()
  }
}

class ServerTest extends FunSpec with Matchers with UploadsCleaner {
  import com.memoria.Server.postUpload

  def uploadInput(count: Int, secondsAgo: Int): Input = {
    Input.post("/upload")
      .withBody("{ \"count\": %s, \"timestamp\": %s }"
        .format(count, Instant.now.getEpochSecond - secondsAgo))
  }

  def invalidUploadInput: Input = {
    Input.post("/upload")
      .withBody("{ \"invalid\": 1, \"timestamp\": 10000 }")
  }

  describe("POST /upload") {
    describe("when the parameters are valid") {
      describe("and the timestamp is not older than 60 seconds") {
        it("returns 201 - Created") {
          postUpload(uploadInput(1, 5))
            .awaitOutputUnsafe()
            .map(_.status) shouldBe Some(Status.Created)
        }

        it("creates the entry on memory") {
          postUpload(uploadInput(1, 5)).awaitOutputUnsafe()

          Uploads.count shouldBe 1
        }
      }

      describe("and the timestamp is older than 60 seconds") {
        it("returns 204 - No Content") {
          postUpload(uploadInput(1, 65))
            .awaitOutputUnsafe()
            .map(_.status) shouldBe Some(Status.NoContent)
        }

        it("does not create the entry on memory") {
          postUpload(uploadInput(1, 65)).awaitOutputUnsafe()

          Uploads.count shouldBe 0
        }
      }
    }

    describe("when the parameters are not valid") {
      it("returns with status 400 - Bad Request") {
        a[NotParsed] shouldBe thrownBy(postUpload(invalidUploadInput).awaitValueUnsafe())
      }
    }
  }
}
