package com.memoria.suppport

import com.memoria.Cache
import org.scalatest.{BeforeAndAfterEach, Suite}

trait CacheCleaner extends BeforeAndAfterEach { this: Suite =>
  override def beforeEach {
    Cache.destroyAll
    super.beforeEach
  }
}
