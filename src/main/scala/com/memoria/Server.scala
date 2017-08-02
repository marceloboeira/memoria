package com.memoria

import java.time.Instant
import java.util.concurrent.{BlockingQueue, Executors, LinkedBlockingQueue}

import com.memoria.workers.{CacheWorker, QueueWorker}

import util.Try
import com.twitter.util.Await
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

import scala.collection.mutable.ArrayBuffer

case class Upload(count: Int, timestamp: Long)
case class UploadStatistics(count: Int, sum: Int, min: Int, max: Int, avg: Double)

object Cache {
  val queue = new LinkedBlockingQueue[Upload]
  private[this] val memory: ArrayBuffer[Upload] = ArrayBuffer.empty[Upload]
  var uploadStatistics: UploadStatistics = UploadStatistics(0,0,0,0,0)

  def add(item: Upload): Unit = { memory += item }
  def destroyAll: Unit = synchronized { memory.clear }
  def max: Int = { Try(memory.map(_.count).max).toOption.getOrElse(0) }
  def min: Int = { Try(memory.map(_.count).min).toOption.getOrElse(0) }
  def count: Int = { memory.length }
  def sum: Int = { memory.map(_.count).sum }
  def average: Double = { Try((sum / count).toDouble).toOption.getOrElse(0.0) }
  def refreshStatistics = { uploadStatistics = UploadStatistics(count, sum, min, max, average) }
  def maxAge: Long = { Instant.now.getEpochSecond - 60 }
  def removeOldEntries: Unit = synchronized {  memory --= memory.filter(_.timestamp < maxAge) }
}

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
