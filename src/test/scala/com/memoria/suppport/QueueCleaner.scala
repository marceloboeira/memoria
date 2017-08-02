package com.memoria.suppport

import com.memoria.Queue
import org.scalatest.{BeforeAndAfterEach, Suite}

trait QueueCleaner extends BeforeAndAfterEach { this: Suite =>
  override def beforeEach {
    Queue.instance.clear
    super.beforeEach
  }
}
