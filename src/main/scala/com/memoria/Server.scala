package com.memoria

import java.time.Instant
import java.util.concurrent.{BlockingQueue, Executors, LinkedBlockingQueue}

import com.memoria.workers.{CacheWorker, QueueWorker}

import com.twitter.util.Await
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

object Server extends App {
  val cores = 2
  val pool = Executors.newFixedThreadPool(cores)

  def postUpload: Endpoint[Unit] = post("upload" :: jsonBody[Upload]) { upload: Upload =>
    Option(ageOf(upload.timestamp)).filter(_ <= 60) match {
      case Some(x) => {
        Cache.queue.put(upload)
        Output.unit(Status.Created)
      }
      case None => {
        Output.unit(Status.NoContent)
      }
    }
  }

  def getStatistics: Endpoint[UploadStatistics] = get("statistics") {
    Ok(Cache.uploadStatistics)
  }

  private def ageOf(timestamp : Long): Long = { now - timestamp }
  private def now: Long = { Instant.now.getEpochSecond }

  def startWorkers = {
    pool.submit(new QueueWorker(Cache.queue))
    pool.submit(new CacheWorker(500))
  }

  startWorkers
  Await.ready(Http.server.serve(":9000", (postUpload :+: getStatistics).toService))
}
