package com.memoria

import java.time.Instant
import com.twitter.finagle.http.Status
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

package object api {
  val postUpload: Endpoint[Unit] = post("upload" :: jsonBody[Upload]) { upload: Upload =>
    Option(ageOf(upload.timestamp)).filter(_ <= 60) match {
      case Some(x) => {
        Queue.instance.add(upload)
        Output.unit(Status.Created)
      }
      case None => {
        Output.unit(Status.NoContent)
      }
    }
  }

  val getStatistics: Endpoint[UploadStatistics] = get("statistics") {
    Ok(Cache.uploadStatistics)
  }

  val endpoints = (postUpload :+: getStatistics).toService

  private def ageOf(timestamp : Long): Long = { now - timestamp }
  private def now: Long = { Instant.now.getEpochSecond }
}
