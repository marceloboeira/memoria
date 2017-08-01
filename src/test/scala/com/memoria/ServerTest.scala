package com.memoria

import java.time.Instant

import com.twitter.finagle.http.Status
import com.twitter.util.Throw
import io.finch.Error.NotParsed
import io.finch.Input
import io.finch.items.BodyItem
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpec, Matchers}

class ServerTest extends FunSpec with Matchers with BeforeAndAfterEach {
  import com.memoria.Server.postUpload

  def uploadInput(count : Int, secondsAgo : Int): Input = {
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
      }

      describe("and the timestamp is older than 60 seconds") {
        it("returns 204 - No Content") {
          postUpload(uploadInput(1, 65))
            .awaitOutputUnsafe()
            .map(_.status) shouldBe Some(Status.NoContent)
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
