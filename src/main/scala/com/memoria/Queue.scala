package com.memoria

import java.util.concurrent.ConcurrentLinkedQueue

object Queue {
  val queue = new ConcurrentLinkedQueue[Upload]

  def instance = { queue }
}
