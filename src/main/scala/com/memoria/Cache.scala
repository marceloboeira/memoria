package com.memoria

import java.time.Instant

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

object Cache {
  private[this] val memory: ArrayBuffer[Upload] = ArrayBuffer.empty[Upload]
  var uploadStatistics: UploadStatistics = UploadStatistics(0,0,0,0,0)

  def add(item: Upload): Unit = { memory += item }
  def destroyAll: Unit = synchronized { memory.clear }
  def max: Int = { Try(memory.map(_.count).max).toOption.getOrElse(0) }
  def min: Int = { Try(memory.map(_.count).min).toOption.getOrElse(0) }
  def count: Int = { memory.length }
  def sum: Int = { memory.map(_.count).sum }
  def average: Double = { Try((sum / count).toDouble).toOption.getOrElse(0.0) }
  def refreshStatistics = synchronized { uploadStatistics = UploadStatistics(count, sum, min, max, average) }
  def maxAge: Long = { Instant.now.getEpochSecond - 60 }
  def removeOldEntries: Unit = synchronized {  memory --= memory.filter(_.timestamp < maxAge) }
}
