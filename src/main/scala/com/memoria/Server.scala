package com.memoria

import java.time.Instant

import scala.collection.mutable._
import util.Try
import com.twitter.util.Await
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

case class Upload(count: Int, timestamp: Long)
case class Statistics(count: Int, sum: Int, min: Int, max: Int, avg: Double)

object Uploads {
  private[this] val memory: MutableList[Upload] = MutableList.empty[Upload]

  def add(item: Upload): Unit = synchronized { memory += item }
  def destroyAll: Unit = synchronized { memory.clear }
  def max: Int = synchronized { Try(memory.map(_.count).max).toOption.getOrElse(0) }
  def min: Int = synchronized { Try(memory.map(_.count).min).toOption.getOrElse(0) }
  def count: Int = synchronized { memory.length }
  def sum: Int = synchronized { memory.map(_.count).sum }
  def average: Double = synchronized { Try((sum / count).toDouble).toOption.getOrElse(0.0) }
  def statistics: Statistics = synchronized { Statistics(count, sum, min, max, average) }
}

object Server extends App {
  def postUpload: Endpoint[Unit] = post("upload" :: jsonBody[Upload]) { upload: Upload =>
    Option(ageOf(upload.timestamp)).filter(_ <= 60) match {
      case Some(x) => Uploads.add(upload); Output.unit(Status.Created)
      case None => Output.unit(Status.NoContent)
    }
  }

  def getStatistics: Endpoint[Statistics] = get("statistics") {
    Ok(Uploads.statistics)
  }

  private def ageOf(timestamp : Long): Long = { now - timestamp }
  private def now: Long = { Instant.now.getEpochSecond }

  Await.ready(Http.server.serve(":9000", (postUpload :+: getStatistics).toService))
}
