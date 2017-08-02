package com.memoria

import java.util.concurrent.Executors
import com.memoria.workers.{CacheWorker, QueueWorker}
import com.twitter.util.Await
import com.twitter.finagle.Http

object Server extends App {
  val cacheRefreshInterval = 500
  val pool = Executors.newFixedThreadPool(2)

  def startWorkers = {
    pool.submit(new QueueWorker(Queue.instance))
    pool.submit(new CacheWorker(cacheRefreshInterval))
  }

  def startServer = {
    Await.ready(Http.server.serve(":9000", api.endpoints))
  }

  startWorkers
  startServer
}
