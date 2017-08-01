package com.memoria

import com.twitter.util.Await
import com.twitter.finagle.Http

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

object Server extends App {
   def root: Endpoint[String] = get(*) {
     Ok("Memoria")
   }

   Await.ready(Http.server.serve(":9000", root.toService))
}
