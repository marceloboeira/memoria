package com.memoria

import com.twitter.finagle.http.Status
import io.finch.Input
import org.scalatest.{FunSpec, Matchers}

class ServerTest extends FunSpec with Matchers {
  import com.memoria.Server.root

  describe ("root endpoint") {
    it("returns 200") {
      root(Input.get("/")).awaitOutputUnsafe().map(_.status) shouldBe Some(Status.Ok)
    }

    it("returns 'Memoria' in the body") {
      root(Input.get("/")).awaitValueUnsafe() shouldBe Some("Memoria")
    }
  }
}
