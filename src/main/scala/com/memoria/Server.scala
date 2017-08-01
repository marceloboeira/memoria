package com.memoria

import java.time.Instant

import scala.collection.mutable
import com.twitter.util.Await
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

case class Upload(count: Int, timestamp: Long)

object Server extends App {
  def postUpload: Endpoint[Unit] = post("upload" :: jsonBody[Upload]) { upload: Upload =>
    Option(ageOf(upload.timestamp)).filter(_ <= 60) match {
      case Some(x) => Output.unit(Status.Created)
      case None => Output.unit(Status.NoContent)
    }
  }

  private def ageOf(timestamp : Long): Long = { val a = now - timestamp; a }
  private def now: Long = { Instant.now.getEpochSecond }

  Await.ready(Http.server.serve(":9000", (postUpload).toService))
}
